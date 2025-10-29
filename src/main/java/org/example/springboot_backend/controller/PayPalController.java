package org.example.springboot_backend.controller;

import org.example.springboot_backend.service.PayPalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/paypal")
@CrossOrigin(origins = "*")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    // 🔹 Crear orden
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

    // 🔹 Capturar orden
    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("orderId") || body.get("orderId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "orderId es obligatorio"));
        }
        String orderId = body.get("orderId").toString();

        // userId opcional, poner un valor por defecto
        Long userId = 0L;
        if (body.containsKey("userId") && body.get("userId") != null) {
            userId = Long.parseLong(body.get("userId").toString());
        }

        boolean simulateFail = false;
        if (body.containsKey("simulateFail") && body.get("simulateFail") != null) {
            simulateFail = Boolean.parseBoolean(body.get("simulateFail").toString());
        }

        System.out.println("🟡 Capturando orden: " + orderId + " para usuario: " + userId);

        try {
            Map<String, Object> result = payPalService.captureOrder(orderId, simulateFail, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }


    // 🔹 Éxito (para redirección desde PayPal)
    @GetMapping("/success")
    public String success(@RequestParam String token, @RequestParam(required = false) String PayerID) {
        return "<h2>✅ Pago completado correctamente.</h2><p>Token: " + token + "</p>";
    }

    // 🔹 Cancelación (para redirección desde PayPal)
    @GetMapping("/cancel")
    public String cancel() {
        return "<h2>❌ Pago cancelado por el usuario.</h2>";
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

}
