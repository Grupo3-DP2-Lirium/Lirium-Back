package org.example.springboot_backend.controller;

import org.springframework.security.core.Authentication;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.PayPalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/paypal")
@CrossOrigin(origins = "*")
public class PayPalController {

    private final PayPalService payPalService;
    private final UserRepository userRepository;


    public PayPalController(PayPalService payPalService, UserRepository userRepository) {
        this.payPalService = payPalService;
        this.userRepository = userRepository;
    }

    // Crear orden
    public static class CreateOrderRequest {
        public String amount;
        public boolean simulateFail;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            System.out.println("🟢 Recibida solicitud para crear orden de PayPal por: $" + request.amount);
            Map<String, Object> result = payPalService.createOrder(request.amount, request.simulateFail);
            System.out.println("🟢 Orden creada exitosamente: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Capturar orden
    @PostMapping("/capture-order")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> captureOrder(
            @RequestBody CaptureOrderRequest request,
            Authentication authentication) {
        try {
            // Parse JSON: ya lo hace Spring automáticamente con @RequestBody
            // Obtener usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Llamar al servicio de PayPal
            Map<String, Object> result = payPalService.captureOrder(
                request.getOrderId(),
                request.isSimulateFail(),
                user,
                request.getPlanId(),
                request.getFrequency()
            );

            // Return success response
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Return error response
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // DTO
    public static class CaptureOrderRequest {
        private String orderId;
        private UUID planId;
        private String frequency; // "MONTHLY" o "YEARLY"
        private boolean simulateFail;

        // getters y setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public UUID getPlanId() { return planId; }
        public void setPlanId(UUID planId) { this.planId = planId; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public boolean isSimulateFail() { return simulateFail; }
        public void setSimulateFail(boolean simulateFail) { this.simulateFail = simulateFail; }
    }


    // Éxito (para redirección desde PayPal)
    @GetMapping("/success")
    public String success(@RequestParam String token, @RequestParam(required = false) String PayerID) {
        return "<h2> Pago completado correctamente.</h2><p>Token: " + token + "</p>";
    }

    // Cancelación (para redirección desde PayPal)
    @GetMapping("/cancel")
    public String cancel() {
        return "<h2> Pago cancelado por el usuario.</h2>";
    }

    @GetMapping("/receipt")
    public ResponseEntity<Map<String, Object>> getLastReceipt() {
        Map<String, Object> receipt = Map.of(
            "fecha", LocalDateTime.now().toString(),
            "monto", "5.00",
            "moneda", "USD",
            "descripcion", "Suscripción Premium LiriumApp",
            "transaccionId", "PAY-" + UUID.randomUUID()
        );

        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        try {
            Map<String, Object> balance = payPalService.getBalance();
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
