package org.example.springboot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class PayPalService {

    private final WebClient webClient;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.base.url}")
    private String baseUrl;

    public PayPalService(@Value("${paypal.base.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // 🔹 Obtener access token de PayPal
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

    // 🔹 Crear una orden PayPal
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

        // 🔸 Extraer approval link
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

    // 🔹 Capturar la orden PayPal
    public Map<String, Object> captureOrder(String orderId, boolean simulateFail, Long userId) {
        String token = getAccessToken();

        Map<String, Object> response = webClient.post()
                .uri("/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        System.out.println("✅ Pago capturado: " + response);

        return Map.of(
                "status", "success",
                "paypalResponse", response,
                "userId", userId
        );
    }
}
