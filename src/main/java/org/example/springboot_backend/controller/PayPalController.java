package org.example.springboot_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.NotificationService;
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
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private NotificationService notificationService;

    public PayPalController(PayPalService payPalService, UserRepository userRepository) {
        this.payPalService = payPalService;
        this.userRepository = userRepository;
    }

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

    /**
     * ✅ ACTUALIZADO: Envía notificaciones según resultado del pago
     */
    @PostMapping("/capture-order")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> captureOrder(
            @RequestBody CaptureOrderRequest request,
            Authentication authentication) {
        try {
            // Obtener usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Obtener información del plan
            Plan plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            // Llamar al servicio de PayPal
            Map<String, Object> result = payPalService.captureOrder(
                request.getOrderId(),
                request.isSimulateFail(),
                user,
                request.getPlanId(),
                request.getFrequency()
            );

            // ✅ NUEVO: Notificar según resultado
            String status = (String) result.get("status");
            
            if ("COMPLETED".equals(status)) {
                // Pago exitoso
                double amount = plan.getPrice();
                String frequencyText = "MONTHLY".equals(request.getFrequency()) ? "Mensual" : "Anual";
                
                notificationService.notifyPaymentSuccess(
                    user, 
                    amount, 
                    plan.getName() + " (" + frequencyText + ")"
                );
                
                System.out.println("✅ Pago exitoso y notificación enviada");
            } else {
                // Pago fallido
                String errorReason = (String) result.getOrDefault("error", "Error desconocido");
                notificationService.notifyPaymentFailed(user, errorReason);
                
                System.out.println("❌ Pago fallido y notificación de error enviada");
            }

            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // En caso de excepción, también notificar
            try {
                String userEmail = authentication.getName();
                User user = userRepository.findByEmail(userEmail).orElse(null);
                
                if (user != null) {
                    notificationService.notifyPaymentFailed(user, e.getMessage());
                }
            } catch (Exception notifError) {
                System.err.println("Error al enviar notificación de fallo: " + notifError.getMessage());
            }
            
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    public static class CaptureOrderRequest {
        private String orderId;
        private UUID planId;
        private String frequency;
        private boolean simulateFail;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public UUID getPlanId() { return planId; }
        public void setPlanId(UUID planId) { this.planId = planId; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public boolean isSimulateFail() { return simulateFail; }
        public void setSimulateFail(boolean simulateFail) { this.simulateFail = simulateFail; }
    }

    @GetMapping("/success")
    public String success(@RequestParam String token, @RequestParam(required = false) String PayerID) {
        return "<h2>Pago completado correctamente.</h2><p>Token: " + token + "</p>";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "<h2>Pago cancelado por el usuario.</h2>";
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