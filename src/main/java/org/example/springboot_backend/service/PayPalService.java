package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.BillingPeriod;
import org.example.springboot_backend.entity.PaymentAttempt;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.PaymentAttemptStatus;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.PaymentAttemptRepository;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PayPalService {

    private final WebClient webClient;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.base.url}")
    private String baseUrl;


     public PayPalService(
            @Value("${paypal.base.url}") String baseUrl,
            SubscriptionService subscriptionService,
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            PaymentAttemptRepository paymentAttemptRepository
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.subscriptionService = subscriptionService;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    // Obtener access token de PayPal
    private String getAccessToken() {
        Map<String, Object> response = webClient.post()
                .uri("/v1/oauth2/token")
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        return response != null ? response.get("access_token").toString() : null;
    }

    public Map<String, Object> getBalance() {
        String token = getAccessToken();

        Map<String, Object> response = webClient.get()
                .uri("/v1/reporting/balances")
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        return response;
    }

    // Crear una orden en PayPal
    public Map<String, Object> createOrder(String amount, boolean simulateFail, User user, Plan plan) {
        String token = getAccessToken();

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of("amount", Map.of("currency_code", "USD", "value", amount))
                },
                "application_context", Map.of(
                        "brand_name", "LiriumApp",
                        "landing_page", "LOGIN",
                        "user_action", "PAY_NOW",
                        "return_url", "http://10.0.2.2:8080/api/paypal/success",
                        "cancel_url", "http://10.0.2.2:8080/api/paypal/cancel"
                )
        );

        Map<String, Object> response = webClient.post()
                .uri("/v2/checkout/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        // Extraer approval link
        Object linkObj = response.get("links");
        String approvalLink = null;

        if (linkObj instanceof Iterable<?> links) {
            for (Object obj : links) {
                if (obj instanceof Map<?, ?> link) {
                    if ("approve".equals(link.get("rel"))) {
                        approvalLink = link.get("href").toString();
                        break;
                    }
                }
            }
        }

        // Guardar intento inicial
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setUser(user);
        attempt.setPlan(plan);
        attempt.setAmount(Double.parseDouble(amount));
        attempt.setStatus(PaymentAttemptStatus.CREATED);
        attempt.setCreatedDate(LocalDateTime.now());
        String paypalOrderId = response.get("id").toString();
        attempt.setTransactionId(paypalOrderId); // ID de PayPal
        attempt.setNotes("Orden PayPal creada");
        
        paymentAttemptRepository.save(attempt);

        return Map.of(
                "id", response.get("id"),
                "status", response.get("status"),
                "approvalLink", approvalLink
        );
    }

    // Capturar la orden en PayPal y crear en BD
    public Map<String, Object> captureOrder(
        String orderId,
        boolean simulateFail,
        User user,
        UUID planId,
        String frequency
    ) {
        String token = getAccessToken();

        // Buscar el intento de pago por orderId
        PaymentAttempt attempt = paymentAttemptRepository
                .findByTransactionId(orderId)
                .orElseThrow(() -> new RuntimeException("PaymentAttempt no encontrado para orderId " + orderId));


        // Llamada a la API de PayPal
        Map<String, Object> response = webClient.post()
                .uri("/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        System.out.println("Pago capturado: " + response);

        // Si el pago fue completado: registra la suscripción y actualiza el intento de pago
        if (response != null && "COMPLETED".equals(response.get("status"))) {
            attempt.setStatus(PaymentAttemptStatus.APPROVED);
            attempt.setUpdatedDate(LocalDateTime.now());
            attempt.setNotes("Pago aprobado");
            paymentAttemptRepository.save(attempt);
            // Convertir frecuencia a BillingPeriod
            BillingPeriod billingPeriod;
            try {
                billingPeriod = BillingPeriod.valueOf(frequency.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Frecuencia inválida: " + frequency);
            }

           subscriptionService.createSubscription(
                user,
                planId,
                orderId,
                PaymentMethod.PAYPAL,
                billingPeriod
           );
        } else { // Pago fallido o cancelado
            attempt.setStatus(PaymentAttemptStatus.REJECTED);
            attempt.setUpdatedDate(LocalDateTime.now());
            paymentAttemptRepository.save(attempt);
        }

        return Map.of(
                "status", "success",
                "paypalResponse", response,
                "userId", user.getIdUser(),
                "planId", planId,
                "frequency", frequency
        );
    }

    // Crear orden de suscripción en PayPal
    public Map<String, Object> createSubscription(User user, String planPaypalId, UUID planId) {
        // Verificar suscripción existente en BD
        Subscription existing = subscriptionRepository.findByUserIdUserAndPlanIdPlan(user.getIdUser(), planId)
            .orElse(null);
        
        if (existing != null && existing.getStatus() == SubscriptionStatus.ACTIVE) {
            // Ya tiene el plan activo
            throw new RuntimeException("El usuario ya tiene este plan activo");
        }
        
        // Si no tiene suscripción → crear orden en PayPal
        String token = getAccessToken();
        Map<String, Object> body = Map.of(
                "plan_id", planPaypalId,
                "subscriber", Map.of(
                        "name", Map.of(
                                "given_name", user.getFirstName(),
                                "surname", user.getFirstLastName()
                        ),
                        "email_address", user.getEmail()
                ),
                "application_context", Map.of(
                        "return_url", "http://10.0.2.2:8080/api/paypal/subscription-success",
                        "cancel_url", "http://10.0.2.2:8080/api/paypal/subscription-cancel"
                )
        );

        Map<String, Object> response = webClient.post()
                .uri("/v1/billing/subscriptions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        // Obtener approve link
        String approvalLink = null;
        for (Object linkObj : (Iterable<?>) response.get("links")) {
            Map<?, ?> link = (Map<?, ?>) linkObj;
            if ("approve".equals(link.get("rel"))) {
                approvalLink = (String) link.get("href");
                break;
            }
        }

        return Map.of(
                "subscriptionID", response.get("id"),
                "approvalLink", approvalLink
        );
    } 
    
    // Confirmar suscripción en PayPal y crear en BD
    public void confirmSubscription(String subscriptionId, UUID planId, User user) {
        String token = getAccessToken();

        // Consultar la orden en PayPal
        Map<String, Object> response = webClient.get()
                .uri("/v1/billing/subscriptions/" + subscriptionId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        String status = response.get("status").toString();

        if (!"ACTIVE".equals(status)) {
            throw new RuntimeException("Subscription not active: " + status);
        }

        // Se crea la suscripción en BD
        subscriptionService.createSubscription(
                user,
                planId, // tu UUID interno
                subscriptionId, // ID de PayPal
                PaymentMethod.PAYPAL,
                BillingPeriod.MONTHLY // o el que corresponda
        );
    }

    // Cancelar suscripción en PayPal y en BD
    public void cancelSubscriptionPaypal(String subscriptionId, User user) {
        // Cancelar en PayPal
        String token = getAccessToken();
        webClient.post()
            .uri("/v1/billing/subscriptions/" + subscriptionId + "/cancel")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("reason", '-'))
            .retrieve()
            .bodyToMono(Void.class)
            .block(Duration.ofSeconds(10));
    
        // Cancelar en BD
        subscriptionService.cancelSubscription(user);
    }

    public String createProduct() {
        String token = getAccessToken();

        Map<String, Object> body = Map.of(
            "name", "Lirium App Subscription",
            "description", "Memorial and remembrance platform subscription",
            "type", "SERVICE",
            "category", "SOFTWARE"
        );

        Map<String, Object> response = webClient.post()
            .uri("/v1/catalogs/products")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        return response.get("id").toString();
    }

    public String createPaypalPlan(String name, Double price, String interval) {
        String token = getAccessToken();

        Map<String, Object> body = Map.of(
            "product_id", "YOUR_PRODUCT_ID",
            "name", name + " " + interval,
            "billing_cycles", new Object[]{
                Map.of(
                    "frequency", Map.of(
                        "interval_unit", interval,
                        "interval_count", 1
                    ),
                    "tenure_type", "REGULAR",
                    "sequence", 1,
                    "total_cycles", 0,
                    "pricing_scheme", Map.of(
                        "fixed_price", Map.of(
                            "value", price,
                            "currency_code", "USD"
                        )
                    )
                )
            },
            "payment_preferences", Map.of(
                "auto_bill_outstanding", true,
                "setup_fee_failure_action", "CONTINUE",
                "payment_failure_threshold", 3
            )
        );

        Map<String, Object> response = webClient.post()
            .uri("/v1/billing/plans")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block();

        return response.get("id").toString();
    }

}
