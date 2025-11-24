package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.BillingPeriod;
import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.example.springboot_backend.entity.PaymentAttempt;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.UserExtraStorage;
import org.example.springboot_backend.enums.PaymentAttemptStatus;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.ExtraStoragePlanRepository;
import org.example.springboot_backend.repository.PaymentAttemptRepository;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.example.springboot_backend.repository.UserExtraStorageRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PayPalService {

    private final WebClient webClient;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PlanRepository planRepository;
    private final UserExtraStorageRepository userExtraStorageRepository;
    private final ExtraStoragePlanRepository extraStoragePlanRepository;
    private final ExtraStorageService extraStorageService;
    private final UserRepository userRepository;

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
            PaymentAttemptRepository paymentAttemptRepository,
            PlanRepository planRepository,
            UserExtraStorageRepository userExtraStorageRepository,
            ExtraStoragePlanRepository extraStoragePlanRepository,
            ExtraStorageService extraStorageService
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.subscriptionService = subscriptionService;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.planRepository = planRepository;
        this.userExtraStorageRepository = userExtraStorageRepository;
        this.extraStoragePlanRepository = extraStoragePlanRepository;
        this.extraStorageService = extraStorageService;
        this.userRepository = userRepository;
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
        // Validar estado actual ANTES de crear en PayPal
        Subscription activeSub = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE).orElse(null);

        // Si tiene activa, revisar si fue cancelada o aún está vigente
        if (activeSub != null) {
            Plan nuevoPlan = planRepository.findByIdPlan(planId)
                    .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

            // Tengo sucripción activa sin cancelar
            if (activeSub.getEndDate() == null) {
                // Suscripción activa sin fecha de fin
                if (nuevoPlan.getPrice() > activeSub.getPlan().getPrice()) {
                    // Upgrade → cancelar actual y crear nueva
                    // Esto se maneja después de crear la suscripción en PayPal
                } else {
                    // Downgrade → bloquear
                    throw new RuntimeException("Tu plan actual sigue activo. Podrás cambiar a uno menor cuando finalice el ciclo actual.");
                }
            } else {
                // Suscripción ya fue cancelada pero todavía no llegó el endDate
                if (LocalDateTime.now().isBefore(activeSub.getEndDate())) {
                    if (nuevoPlan.getPrice() > activeSub.getPlan().getPrice()) {
                        // Upgrade permitido → crear nueva suscripción y usar la fecha de inicio actual o esperar?
                        // Aquí podrías iniciar nueva suscripción ahora o esperar a que termine la anterior
                    } else {
                        // Downgrade/Misma → bloquear hasta endDate
                        throw new RuntimeException("Tu plan actual fue cancelado, espera hasta que termine el ciclo para cambiar a un plan menor.");
                    }
                } else {
                    // Suscripción terminó → actualizar status a CANCELLED si no lo está
                    if (activeSub.getStatus() != SubscriptionStatus.CANCELLED) {
                        activeSub.setStatus(SubscriptionStatus.CANCELLED);
                        subscriptionRepository.save(activeSub);
                    }
                    // Ahora puede crear cualquier suscripción normalmente
                }
            }
        }

        // Si pasa las validaciones, recién creamos la orden en PayPal
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

        // Verificar suscripción pendiente
        Subscription pendingSubscription = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.PENDING)
            .orElse(null);
        if (pendingSubscription != null) {
            throw new RuntimeException("Ya tienes un cambio de plan pendiente. Espera a que se active.");
        }

        // Buscar suscripción activa previa en BD
        Subscription activeSubscription = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .orElse(null);

        if (activeSubscription != null && activeSubscription.getEndDate() == null) {
            try {
                // Cancelar en PayPal
                webClient.post()
                    .uri("/v1/billing/subscriptions/" + activeSubscription.getPaypalSubscriptionId() + "/cancel")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("reason", "Cambio de plan"))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(Duration.ofSeconds(10));

                // Cancelar en BD
                activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
                activeSubscription.setEndDate(LocalDateTime.now());
                subscriptionRepository.save(activeSubscription);
            } catch (Exception e) {
                throw new RuntimeException("Error al cancelar la suscripción anterior: " + e.getMessage());
            }
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

    public Map<String, Object> createExtraStorageSubscription(User user, String planPaypalId, UUID planId) {
        // Buscar si ya tiene una suscripción activa a ESE plan específico
        ExtraStoragePlan plan = extraStoragePlanRepository.findByIdExtraPlan(planId)
            .orElseThrow(() -> new RuntimeException("ExtraStoragePlan no encontrado"));

        Optional<UserExtraStorage> existing =
            userExtraStorageRepository.findByUserAndPlanAndEndDateIsNull(user, plan);

        if (existing.isPresent()) {
            throw new RuntimeException("Ya tienes este plan de almacenamiento activo.");
        }

        // De lo contrario, crear la suscripción en PayPal
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
                        "return_url", "http://10.0.2.2:8080/api/paypal/storage-success",
                        "cancel_url", "http://10.0.2.2:8080/api/paypal/storage-cancel"
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

    public void confirmExtraStorageSubscription(String subscriptionId, UUID planId, User user) {
        String token = getAccessToken();

        Map<String, Object> response = webClient.get()
                .uri("/v1/billing/subscriptions/" + subscriptionId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        if (!"ACTIVE".equals(response.get("status"))) {
            throw new RuntimeException("Subscription not active: " + response.get("status"));
        }

        // Se crea la suscripción en BD
        extraStorageService.createExtraStorageSubscription(
                user,
                planId, // tu UUID interno
                subscriptionId, // ID de PayPal
                PaymentMethod.PAYPAL
        );
    }

    public Map<String, Object> createExtraDocumentaryOrder(User user, int quantity, double amount) {
        String token = getAccessToken();

        // Body de la orden
        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", new Object[]{
                        Map.of(
                            "amount", Map.of("currency_code", "USD", "value", String.format("%.2f", amount)),
                            "description", quantity + " documentales extra"
                        )
                },
                "application_context", Map.of(
                        "brand_name", "LiriumApp",
                        "landing_page", "LOGIN",
                        "user_action", "PAY_NOW",
                        "return_url", "http://10.0.2.2:8080/api/paypal/storage-success",
                        "cancel_url", "http://10.0.2.2:8080/api/paypal/storage-cancel"
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

        // Extraer link de aprobación
        String approvalLink = null;
        Object linksObj = response.get("links");
        if (linksObj instanceof Iterable<?> links) {
            for (Object obj : links) {
                if (obj instanceof Map<?, ?> link && "approve".equals(link.get("rel"))) {
                    approvalLink = link.get("href").toString();
                    break;
                }
            }
        }

        // Guardar intento de pago
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setUser(user);
        attempt.setAmount(amount);
        attempt.setStatus(PaymentAttemptStatus.CREATED);
        attempt.setCreatedDate(LocalDateTime.now());
        attempt.setNotes(quantity + " documentales extra - Orden PayPal creada");
        attempt.setTransactionId(response.get("id").toString());
        paymentAttemptRepository.save(attempt);

        return Map.of(
                "orderId", response.get("id"),
                "approvalLink", approvalLink
        );
    }

    public Map<String, Object> captureExtraDocumentaryOrder(String orderId, User user, int quantity) {
        String token = getAccessToken();

        // Buscar intento de pago por orderId
        PaymentAttempt attempt = paymentAttemptRepository
                .findByTransactionId(orderId)
                .orElseThrow(() -> new RuntimeException("PaymentAttempt no encontrado para orderId " + orderId));

        // Llamada a la API de PayPal para capturar el pago
        Map<String, Object> response = webClient.post()
                .uri("/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));

        if (response != null && "COMPLETED".equals(response.get("status"))) {
            attempt.setStatus(PaymentAttemptStatus.APPROVED);
            attempt.setUpdatedDate(LocalDateTime.now());
            attempt.setNotes("Pago documentales extra aprobado");
            paymentAttemptRepository.save(attempt);

            // Sumar documentales al usuario según la cantidad enviada
            user.setDocumentariesPurchased((user.getDocumentariesPurchased() == null ? 0 : user.getDocumentariesPurchased()) + quantity);
            user.setDocumentariesAvailable((user.getDocumentariesAvailable() == null ? 0 : user.getDocumentariesAvailable()) + quantity);

            userRepository.save(user);

        } else {
            attempt.setStatus(PaymentAttemptStatus.REJECTED);
            attempt.setUpdatedDate(LocalDateTime.now());
            attempt.setNotes("Pago documentales extra rechazado");
            paymentAttemptRepository.save(attempt);
        }

        return Map.of(
                "status", "success",
                "paypalResponse", response,
                "userId", user.getIdUser(),
                "quantityAdded", quantity
        );
    }

}
