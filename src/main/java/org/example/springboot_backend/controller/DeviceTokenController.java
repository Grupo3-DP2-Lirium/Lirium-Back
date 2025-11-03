package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/device-tokens")
@CrossOrigin(origins = "*")
public class DeviceTokenController {
    
    @Autowired
    private FCMService fcmService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Registra un token FCM para recibir notificaciones
     */
    @PostMapping("/register")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> registerToken(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String fcmToken = request.get("fcmToken");
            String deviceType = request.getOrDefault("deviceType", "android");
            String deviceId = request.get("deviceId");
            
            fcmService.registerDeviceToken(user, fcmToken, deviceType, deviceId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Token registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error registering token: " + e.getMessage()));
        }
    }
    
    /**
     * Elimina un token FCM (logout)
     */
    @PostMapping("/unregister")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> unregisterToken(@RequestBody Map<String, String> request) {
        try {
            String fcmToken = request.get("fcmToken");
            fcmService.unregisterDeviceToken(fcmToken);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Token unregistered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error unregistering token: " + e.getMessage()));
        }
    }
}