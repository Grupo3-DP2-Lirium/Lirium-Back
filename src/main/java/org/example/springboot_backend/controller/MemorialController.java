package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.entity.Memorial;
import org.springframework.http.MediaType;
import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.IMemorialService;
import org.example.springboot_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.example.springboot_backend.repository.MemorialRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/memorials")
@CrossOrigin(origins = "*")
public class MemorialController {

    @Autowired
    private IMemorialService memorialService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemorialRepository memorialRepository;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * ✅ ACTUALIZADO: Ahora crea notificación al crear memorial
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createMemorial(
            @RequestPart("memorial") String memorialJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        try {
            MemorialRequest request = objectMapper.readValue(memorialJson, MemorialRequest.class);

            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Crear memorial
            MemorialResponse response = memorialService.createMemorial(request, file, user);

            // ✅ NUEVO: Crear notificación automática
            Memorial memorial = memorialRepository.findById(response.getIdMemorial())
                    .orElseThrow(() -> new RuntimeException("Memorial not found after creation"));
            
            notificationService.notifyMemorialCreated(user, memorial);
            
            System.out.println("✅ Memorial creado y notificación enviada: " + memorial.getName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating memorial: " + e.getMessage());
        }
    }

    @GetMapping(value = "/getMemorials", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getMyMemorials(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<MemorialResponse> memorials = memorialService.getMyMemorials(user);
            return ResponseEntity.ok(memorials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching memorials: " + e.getMessage());
        }
    }

    @GetMapping(value = "/getCollaborativeMemorials", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getCollaborativeMemorials(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<MemorialResponse> memorials = memorialService.getCollaborativeMemorials(user);
            return ResponseEntity.ok(memorials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching collaborative memorials: " + e.getMessage());
        }
    }

    @GetMapping("/collaborative")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getCollaborativeMemorials() {
        try {
            List<Memorial> collaborativeMemorials = memorialRepository.findByIsCollaborativeTrue();
            return ResponseEntity.ok(collaborativeMemorials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching collaborative memorials: " + e.getMessage());
        }
    }

    @GetMapping("/predefined-questions")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getPredefinedQuestions() {
        try {
            return ResponseEntity.ok("Predefined questions endpoint - to be implemented");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching predefined questions: " + e.getMessage());
        }
    }
}