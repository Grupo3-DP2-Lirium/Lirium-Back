package org.example.springboot_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para verificar el sistema de suspensión
 */
@RestController
@RequestMapping("/api/test")
public class TestSuspensionController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pong! El servidor está funcionando");
        response.put("user", authentication != null ? authentication.getName() : "anonymous");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        System.out.println("🏓 Test ping recibido de: " + (authentication != null ? authentication.getName() : "anonymous"));
        
        return ResponseEntity.ok(response);
    }
}
