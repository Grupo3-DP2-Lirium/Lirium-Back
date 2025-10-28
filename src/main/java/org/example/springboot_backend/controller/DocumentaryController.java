package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot_backend.dto.CreateDocumentaryRequest;
import org.example.springboot_backend.dto.DocumentaryResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.DocumentaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentaries")
@CrossOrigin(origins = "*")
@Tag(name = "Documentaries", description = "Documentary generation and management")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentaryController {

    @Autowired
    private DocumentaryService documentaryService;

    @Autowired
    private UserRepository userRepository;  // <-- AGREGAR ESTO

    @PostMapping
    @Operation(summary = "Create a new documentary",
            description = "Creates a documentary from timeline memories of a memorial")
    public ResponseEntity<?> createDocumentary(
            @RequestBody CreateDocumentaryRequest request,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            DocumentaryResponse response = documentaryService.createDocumentary(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Documentary creation started",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/{documentaryId}")
    @Operation(summary = "Get documentary status",
            description = "Get the current status and details of a documentary")
    public ResponseEntity<?> getDocumentaryStatus(@PathVariable UUID documentaryId) {
        try {
            DocumentaryResponse response = documentaryService.getDocumentaryStatus(documentaryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/memorial/{memorialId}")
    @Operation(summary = "Get documentaries by memorial",
            description = "Get all documentaries created for a specific memorial")
    public ResponseEntity<?> getDocumentariesByMemorial(@PathVariable UUID memorialId) {
        try {
            List<DocumentaryResponse> responses = documentaryService.getDocumentariesByMemorial(memorialId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/my-documentaries")
    @Operation(summary = "Get my documentaries",
            description = "Get all documentaries created by the current user")
    public ResponseEntity<?> getMyDocumentaries(Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            List<DocumentaryResponse> responses = documentaryService.getDocumentariesByUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @PostMapping("/{documentaryId}/cancel")
    @Operation(summary = "Cancel documentary processing",
            description = "Cancel a documentary that is pending or processing")
    public ResponseEntity<?> cancelDocumentary(
            @PathVariable UUID documentaryId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            documentaryService.cancelDocumentary(documentaryId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary cancelled successfully",
                    "documentaryId", documentaryId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @DeleteMapping("/{documentaryId}")
    @Operation(summary = "Delete documentary",
            description = "Delete a documentary and its associated video file")
    public ResponseEntity<?> deleteDocumentary(
            @PathVariable UUID documentaryId,
            Authentication authentication) {
        try {
            UUID userId = getUserIdFromAuth(authentication);
            documentaryService.deleteDocumentary(documentaryId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Documentary deleted successfully",
                    "documentaryId", documentaryId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // Helper method para extraer userId del Authentication
    private UUID getUserIdFromAuth(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getIdUser();
    }
}