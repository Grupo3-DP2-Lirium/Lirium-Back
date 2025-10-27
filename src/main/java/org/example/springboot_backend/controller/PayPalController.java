package org.example.springboot_backend.controller;

import org.example.springboot_backend.service.PayPalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Map;

@RestController
@RequestMapping("api/paypal")
@CrossOrigin(origins = "*")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    // Crear orden
    public static class CreateOrderRequest {
        public String amount;
        public boolean simulateFail;
    }

    @PostMapping("/create-order")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        System.out.println("Llega request a backend: " + request.amount);
        try {
            Map<String, Object> result = payPalService.createOrder(request.amount, request.simulateFail);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // Capturar orden
    public static class CaptureOrderRequest {
        public String orderId;
        public Long userId;
        public boolean simulateFail;
    }

    @PostMapping("/capture-order")
    public Map<String, Object> captureOrder(@RequestBody Map<String, Object> body) throws Exception {
        String orderId = body.get("orderId").toString();
        Long userId = Long.parseLong(body.get("userId").toString());
        boolean simulateFail = Boolean.parseBoolean(body.get("simulateFail").toString());
        return payPalService.captureOrder(orderId, simulateFail, userId);
    }
}
