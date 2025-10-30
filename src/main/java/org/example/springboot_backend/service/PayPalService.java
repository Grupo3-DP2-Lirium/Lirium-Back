package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.BillingPeriod;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class PayPalService {

    private final WebClient webClient;
    private final SubscriptionService subscriptionService;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.base.url}")
    private String baseUrl;


     public PayPalService(
            @Value("${paypal.base.url}") String baseUrl,
            SubscriptionService subscriptionService,
            UserRepository userRepository // 🔹 Inyección
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.subscriptionService = subscriptionService;
    }

    // Obtener access token de PayPal
    private String getAccessToken() {
        Map<String, Object> response = webClient.post()
                .uri("/v1/oauth2/token")
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(Map.class)
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

    // Crear una orden PayPal
    public Map<String, Object> createOrder(String amount, boolean simulateFail) {
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
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        // Extraer approval link
        String approvalLink = null;
        var links = (Iterable<Map<String, Object>>) response.get("links");
        for (Map<String, Object> link : links) {
            if ("approve".equals(link.get("rel"))) {
                approvalLink = link.get("href").toString();
                break;
            }
        }

        return Map.of(
                "id", response.get("id"),
                "status", response.get("status"),
                "approvalLink", approvalLink
        );
    }

    // Capturar la orden PayPal
    public Map<String, Object> captureOrder(
        String orderId,
        boolean simulateFail,
        User user,
        UUID planId,
        String frequency
    ) {
        String token = getAccessToken();

        // Llamada a la API de PayPal
        Map<String, Object> response = webClient.post()
                .uri("/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        System.out.println("✅ Pago capturado: " + response);

        // 🔹 Si el pago fue completado, registrar la suscripción
        if (response != null && "COMPLETED".equals(response.get("status"))) {
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
                PaymentMethod.PAYPAL,
                billingPeriod
           );
        }

        return Map.of(
                "status", "success",
                "paypalResponse", response,
                "userId", user.getIdUser(),
                "planId", planId,
                "frequency", frequency
        );
  }
}
