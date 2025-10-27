package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.entity.Memorial;
import org.springframework.http.MediaType;
import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.IMemorialService;
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

    // Create a memorial
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createMemorial(
            @RequestPart("memorial") String memorialJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        try {
            // Parse JSON to MemorialRequest
            MemorialRequest request = objectMapper.readValue(memorialJson, MemorialRequest.class);

            // Get authenticated user
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Call service to create memorial
            MemorialResponse response = memorialService.createMemorial(request, file, user);

            // Return success response
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error response
            return ResponseEntity.badRequest().body("Error creating memorial: " + e.getMessage());
        }
    }

    @GetMapping(value = "/getMemorials", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getMyMemorials(Authentication authentication) {
        try {
            String userEmail = authentication.getName(); // aquí obtienes al usuario del token
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<MemorialResponse> memorials = memorialService.getMyMemorials(user);
            return ResponseEntity.ok(memorials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching memorials: " + e.getMessage());
        }
    }

    // Get a memorial by ID
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getMemorialById(
            @PathVariable("id") String memorialId,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            MemorialResponse memorial = memorialService.getMemorialById(memorialId, user);
            return ResponseEntity.ok(memorial);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error fetching memorial: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Update a memorial by ID with optional image
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> updateMemorial(
            @PathVariable("id") String memorialId,
            @RequestPart("memorial") String memorialJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        try {
            // Parse JSON to MemorialRequest
            MemorialRequest request = objectMapper.readValue(memorialJson, MemorialRequest.class);

            // Get authenticated user
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Call service to update memorial
            MemorialResponse response = memorialService.updateMemorial(memorialId, request, file, user);

            // Return success response
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error updating memorial: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/getCollaborativeMemorials", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getCollaborativeMemorials(Authentication authentication) {
        try {
            String userEmail = authentication.getName(); // aquí obtienes al usuario del token
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<MemorialResponse> memorials = memorialService.getCollaborativeMemorials(user);
            return ResponseEntity.ok(memorials);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching collaborative memorials: " + e.getMessage());
        }
    }


    
    // HU02 - Obtener memoriales colaborativos
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

    // HU05 - Obtener preguntas predefinidas
    @GetMapping("/predefined-questions")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getPredefinedQuestions() {
        try {
            // Implementar lógica para obtener preguntas predefinidas
            // Por ahora retornamos una respuesta básica
            return ResponseEntity.ok("Predefined questions endpoint - to be implemented");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching predefined questions: " + e.getMessage());
        }
    }
}
