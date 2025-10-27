package org.example.springboot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

@Service
public class PayPalService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String secret;

    @Value("${paypal.base.url}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("")
            .build();

    // 1) Obtener access token
    public String getAccessToken() {
        Map tokenResponse = webClient.post()
                .uri(baseUrl + "/v1/oauth2/token")
                .headers(h -> h.setBasicAuth(clientId, secret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new RuntimeException("No se obtuvo access_token de PayPal");
        }
        return tokenResponse.get("access_token").toString();
    }

    // 2) Crear orden en PayPal sandbox o simulada
    public Map<String, Object> createOrder(String amount, boolean simulateFail) {
        if (simulateFail) {
            return Map.of("status", "ERROR_SIMULATED", "message", "Simulated payment creation failure");
        }

        String token = getAccessToken();

        List<Map<String, Object>> purchaseUnits = List.of(
                Map.of(
                        "amount", Map.of(
                                "currency_code", "USD",
                                "value", amount
                        )
                )
        );

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", purchaseUnits,
                "application_context", Map.of(
                        "brand_name", "LiriumPayment",
                        "landing_page", "NO_PREFERENCE",
                        "user_action", "PAY_NOW"
                )
        );

        Map response = webClient.post()
                .uri(baseUrl + "/v2/checkout/orders")
                .headers(h -> {
                    h.setBearerAuth(token);
                    h.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        Map<String, Object> result = new HashMap<>();
        result.put("raw", response);
        result.put("status", response != null ? response.get("status") : "UNKNOWN");
        return result;
    }

    // 3) Capturar orden
    public Map<String, Object> captureOrder(String orderId, boolean simulateFail, Long userId) {
        if (simulateFail) {
            return Map.of("status", "ERROR_SIMULATED", "message", "Simulated capture failure");
        }

        String token = getAccessToken();

        Map response = webClient.post()
                .uri(baseUrl + "/v2/checkout/orders/{id}/capture", orderId)
                .headers(h -> {
                    h.setBearerAuth(token);
                    h.set("Accept", MediaType.APPLICATION_JSON_VALUE);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), ClientResponse::createException)
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        // Ejemplo de actualización de suscripción (placeholder)
        if (response != null && "COMPLETED".equals(response.get("status"))) {
            // TODO: inyectar un UserService / SubscriptionService y activar plan real
            return Map.of("status", "captured", "order", response);
        } else {
            return Map.of("status", response != null ? response.get("status") : "UNKNOWN", "raw", response);
        }
    }
}
