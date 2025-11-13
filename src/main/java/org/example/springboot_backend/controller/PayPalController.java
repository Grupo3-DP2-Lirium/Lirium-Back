package org.example.springboot_backend.controller;

import org.springframework.security.core.Authentication;
import org.example.springboot_backend.dto.CaptureOrderRequest;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.PayPalService;
import org.springframework.http.HttpStatus;
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
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PayPalController(PayPalService payPalService, UserRepository userRepository, PlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.payPalService = payPalService;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // Crear orden
    public static class CreateOrderRequest {
        public String amount;
        public boolean simulateFail;
        public String planId; 
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request, Authentication authentication) {
        try {
            // Obtener usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UUID planUuid = UUID.fromString(request.planId);
            Plan plan = planRepository.findById(planUuid)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

            System.out.println("Recibida solicitud para crear orden de PayPal por: $" + request.amount);
            System.out.println("Plan recibido del request: " + request.planId);
            Map<String, Object> result = payPalService.createOrder(request.amount, request.simulateFail, user, plan);
            System.out.println("Orden creada exitosamente: " + result);
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

    @PostMapping("/create-subscription")
    public ResponseEntity<?> createSubscription(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String planPaypalId = request.get("paypalPlanId"); // ID del plan de PayPal
            UUID planId = UUID.fromString(request.get("planId")); // Id del plan en base de datos

            // Obtener usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = payPalService.createSubscription(
                    user, planPaypalId, planId
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SUCCESS - PayPal Subscription
    @PostMapping("/subscription-success")
    public ResponseEntity<?> subscriptionSuccess(@RequestBody Map<String, String> request, Authentication auth) {
        String subscriptionId = request.get("subscriptionId");
        UUID planId = UUID.fromString(request.get("planId"));

        String userEmail = auth.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        payPalService.confirmSubscription(subscriptionId, planId, user);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // Cancelar Plan
    @PostMapping("/subscription-cancel")
    public ResponseEntity<?> subscriptionCancel(Authentication auth) {
        String userEmail = auth.getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription activeSub = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new RuntimeException("No active subscription to cancel."));

        String paypalSubscriptionId = activeSub.getPaypalSubscriptionId();

        payPalService.cancelSubscriptionPaypal(paypalSubscriptionId, user);

        return ResponseEntity.ok(Map.of("status", "cancelled"));
    }

    // EXTRAS STORAGE PLAN - Crear suscripción
    @PostMapping("/create-extra-storage-subscription")
    public ResponseEntity<?> createExtraStorageSubscription(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String paypalPlanId = request.get("paypalPlanId");
            UUID extraPlanId = UUID.fromString(request.get("extraPlanId"));

            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = payPalService.createExtraStorageSubscription(user, paypalPlanId, extraPlanId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}