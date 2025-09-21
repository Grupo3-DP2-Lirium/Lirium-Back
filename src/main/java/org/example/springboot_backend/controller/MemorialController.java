package org.example.springboot_backend.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.example.springboot_backend.dto.MemorialRequest;
import org.example.springboot_backend.dto.MemorialResponse;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.MemorialRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.IMemorialService;
import org.example.springboot_backend.service.IMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> createMemorial(
            @RequestPart("memorial") String memorialJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        try {
            // Parseamos el JSON a un DTO MemorialRequest
            MemorialRequest request = objectMapper.readValue(memorialJson, MemorialRequest.class);

            // Obtenemos al usuario autenticado
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Llamamos al service en lugar de usar directamente el repository
            MemorialResponse response = memorialService.createMemorial(request, file, user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating memorial: " + e.getMessage());
        }
    }

}
