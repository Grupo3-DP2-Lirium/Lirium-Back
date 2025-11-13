package org.example.springboot_backend.controller;

import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.UserExtraStorage;
import org.example.springboot_backend.service.ExtraStorageService;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extra-storage")
@CrossOrigin(origins = "*")
public class ExtraStorageController {

    private final ExtraStorageService extraStorageService;
    private final UserRepository userRepository;

    public ExtraStorageController(ExtraStorageService extraStorageService, UserRepository userRepository) {
        this.extraStorageService = extraStorageService;
        this.userRepository = userRepository;
    }

    // Listar todos los planes de almacenamiento extra
    @GetMapping("/plans")
    public ResponseEntity<List<ExtraStoragePlan>> getAllExtraStoragePlans() {
        List<ExtraStoragePlan> plans = extraStorageService.getAllExtraStoragePlans();
        return ResponseEntity.ok(plans);
    }

    // Obtener la suscripción activa del usuario
    @GetMapping("/active")
    public ResponseEntity<UserExtraStorage> getActiveExtraStorageSubscription(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserExtraStorage activeSubscription = extraStorageService.getActiveExtraStorageSubscription(user);
        return ResponseEntity.ok(activeSubscription);
    }
}
