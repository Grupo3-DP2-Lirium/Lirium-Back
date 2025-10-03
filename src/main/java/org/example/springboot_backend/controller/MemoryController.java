package org.example.springboot_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.springboot_backend.dto.MemoryCreateRequest;
import org.example.springboot_backend.dto.MemoryResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.IMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
@CrossOrigin(origins = "*")
public class MemoryController {

    @Autowired
    private IMemoryService memoryService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createMemory(
            @RequestPart("memory") String memoryJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) {
        
        try {
            // Parse JSON string to MemoryCreateRequest object
            MemoryCreateRequest request = objectMapper.readValue(memoryJson, MemoryCreateRequest.class);
            
            // Get user from database using email from JWT token
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            MemoryResponse response = memoryService.createMemory(request, files, user);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing request: " + e.getMessage());
        }
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Page<MemoryResponse>> listByMemorial(
            @RequestParam UUID memorialId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MemoryResponse> resp = memoryService.listByMemorial(memorialId, page, size);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/my-memories")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> listByAuthor(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var memories = memoryService.listByAuthor(user); // retorna List<MemoryResponse> con archivos en Base64
            return ResponseEntity.ok(memories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update memory
    @PutMapping(value = "/{memoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> updateMemory(
            @PathVariable UUID memoryId,
            @RequestPart("memory") String memoryJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) {
        try {
            // Parse JSON a MemoryCreateRequest
            MemoryCreateRequest request = objectMapper.readValue(memoryJson, MemoryCreateRequest.class);

            // Obtener usuario logueado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Llamar al servicio de actualización
            MemoryResponse response = memoryService.updateMemory(memoryId, request, files, user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating memory: " + e.getMessage());
        }
    }

}