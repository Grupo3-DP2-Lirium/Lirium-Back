package org.example.springboot_backend.controller;

import jakarta.validation.Valid;
import org.example.springboot_backend.dto.LoginRequest;
import org.example.springboot_backend.dto.LoginResponse;
import org.example.springboot_backend.dto.RegisterUserDTO;
import org.example.springboot_backend.dto.UserResponseDTO;
import org.example.springboot_backend.enums.AuditAction;
import org.example.springboot_backend.service.AuditLogService;
import org.example.springboot_backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    public AuthController(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            
            // Registrar login exitoso
            auditLogService.log(
                AuditAction.USER_LOGIN,
                "User",
                null,
                "Usuario " + request.getEmail() + " inició sesión exitosamente"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Registrar intento de login fallido
            auditLogService.log(
                AuditAction.USER_LOGIN,
                "User",
                null,
                "Intento de login fallido para " + request.getEmail(),
                false,
                e.getMessage()
            );
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserDTO registerRequest) {
        try {
            UserResponseDTO newUser = authService.registerUser(registerRequest);
            
            // Registrar registro exitoso
            auditLogService.log(
                AuditAction.USER_REGISTER,
                "User",
                newUser.getIdUser().toString(),
                "Nuevo usuario registrado: " + newUser.getEmail()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (Exception e) {
            // Registrar intento de registro fallido
            auditLogService.log(
                AuditAction.USER_REGISTER,
                "User",
                null,
                "Intento de registro fallido para " + registerRequest.getEmail(),
                false,
                e.getMessage()
            );
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            auditLogService.log(
                AuditAction.USER_LOGOUT,
                "User",
                null,
                "Usuario " + authentication.getName() + " cerró sesión"
            );
        }
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserResponseDTO user = authService.getUserInfo(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}