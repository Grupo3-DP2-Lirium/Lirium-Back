package org.example.springboot_backend.controller;

import jakarta.validation.Valid;
import org.example.springboot_backend.dto.ForgotPasswordRequest;
import org.example.springboot_backend.dto.ResetPasswordRequest;
import org.example.springboot_backend.dto.VerifyCodeRequest;
import org.example.springboot_backend.service.PasswordRecoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordRecoveryController {
    
    private final PasswordRecoveryService passwordRecoveryService;
    
    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }
    
    /**
     * Endpoint 1: Solicitar código de recuperación
     * POST /api/auth/forgot-password
     * 
     * Request body:
     * {
     *   "email": "usuario@ejemplo.com"
     * }
     * 
     * Response (200 OK):
     * {
     *   "message": "Código enviado al email",
     *   "expiresIn": 600
     * }
     * 
     * Errores:
     * - 404: Email no encontrado
     * - 429: Demasiadas solicitudes (rate limiting)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, Object> response = passwordRecoveryService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint 2: Verificar código de recuperación
     * POST /api/auth/verify-code
     * 
     * Request body:
     * {
     *   "email": "usuario@ejemplo.com",
     *   "code": "123456"
     * }
     * 
     * Response (200 OK):
     * {
     *   "message": "Código verificado correctamente",
     *   "resetToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * 
     * Errores:
     * - 400: Código incorrecto (intentos restantes: X)
     * - 400: Código expirado
     * - 429: Máximo de intentos alcanzado
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @Valid @RequestBody VerifyCodeRequest request) {
        Map<String, Object> response = passwordRecoveryService.verifyCode(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint 3: Restablecer contraseña
     * POST /api/auth/reset-password
     * 
     * Request body:
     * {
     *   "resetToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "newPassword": "miNuevaPassword123"
     * }
     * 
     * Response (200 OK):
     * {
     *   "message": "Contraseña actualizada exitosamente"
     * }
     * 
     * Errores:
     * - 400: Token inválido o expirado
     * - 400: Contraseña muy corta (mínimo 6 caracteres)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> response = passwordRecoveryService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}
