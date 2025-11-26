package org.example.springboot_backend.controller;

import jakarta.validation.Valid;
import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    
    @Autowired
    private ObjectMapper objectMapper;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(
        value = "/register",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> register(
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile profilePhoto
    ) {
        try {
            RegisterUserDTO registerRequest = objectMapper.readValue(userJson, RegisterUserDTO.class);

            UserResponseDTO created = authService.registerUser(registerRequest, profilePhoto);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logout successful");
    }
}