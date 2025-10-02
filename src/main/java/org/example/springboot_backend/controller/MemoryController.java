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

import java.util.List;
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
            @RequestParam(value = "files", required = false) List<MultipartFile> filesList,
            Authentication authentication) {
        
        try {
            // Convert List to Array for service compatibility
            MultipartFile[] files = null;
            if (filesList != null && !filesList.isEmpty()) {
                files = filesList.toArray(new MultipartFile[0]);
            }
            
            // DEBUG: Log information about received files
            System.out.println("DEBUG MemoryController - Received files list: " + (filesList != null ? filesList.size() + " files" : "null"));
            System.out.println("DEBUG MemoryController - Converted files array: " + (files != null ? files.length + " files" : "null"));
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    MultipartFile file = files[i];
                    System.out.println("DEBUG MemoryController - File " + i + ": " + 
                        (file != null ? file.getOriginalFilename() + " (size: " + file.getSize() + ")" : "null"));
                }
            }
            
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

}