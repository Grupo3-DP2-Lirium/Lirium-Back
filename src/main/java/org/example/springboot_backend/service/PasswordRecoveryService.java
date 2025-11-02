package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.ForgotPasswordRequest;
import org.example.springboot_backend.dto.ResetPasswordRequest;
import org.example.springboot_backend.dto.VerifyCodeRequest;
import org.example.springboot_backend.entity.PasswordResetCode;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.exception.*;
import org.example.springboot_backend.repository.PasswordResetCodeRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class PasswordRecoveryService {
    
    private static final int CODE_EXPIRATION_MINUTES = 10;
    private static final int RESET_TOKEN_EXPIRATION_MINUTES = 30;
    private static final int MAX_ATTEMPTS = 3;
    private static final int RATE_LIMIT_SECONDS = 120; // 2 minutos
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    private final UserRepository userRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Random random;
    
    public PasswordRecoveryService(
            UserRepository userRepository,
            PasswordResetCodeRepository resetCodeRepository,
            EmailService emailService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.resetCodeRepository = resetCodeRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.random = new Random();
    }
    
    /**
     * Solicita un código de recuperación de contraseña
     * Implementa rate limiting de 2 minutos entre solicitudes
     */
    @Transactional
    public Map<String, Object> requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail();
        
        // 1. Buscar usuario por email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                    "No encontramos una cuenta con ese email"));
        
        // 2. Rate limiting: verificar si ya existe un código reciente no expirado
        LocalDateTime rateLimitTime = LocalDateTime.now().minusSeconds(RATE_LIMIT_SECONDS);
        resetCodeRepository.findFirstByUserIdAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                user.getIdUser(), LocalDateTime.now())
            .ifPresent(existingCode -> {
                if (existingCode.getCreatedAt().isAfter(rateLimitTime)) {
                    long secondsRemaining = Duration.between(LocalDateTime.now(), 
                        existingCode.getCreatedAt().plusSeconds(RATE_LIMIT_SECONDS)).getSeconds();
                    throw new TooManyRequestsException(
                        String.format("Por favor espera %d segundos antes de solicitar un nuevo código", 
                            secondsRemaining));
                }
            });
        
        // 3. Invalidar códigos anteriores no usados del usuario
        List<PasswordResetCode> oldCodes = resetCodeRepository.findByUserIdAndIsUsedFalse(user.getIdUser());
        oldCodes.forEach(PasswordResetCode::markAsUsed);
        if (!oldCodes.isEmpty()) {
            resetCodeRepository.saveAll(oldCodes);
        }
        
        // 4. Generar código aleatorio de 6 dígitos
        String code = String.format("%06d", random.nextInt(1000000));
        
        // 5. Calcular expiración
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        
        // 6. Guardar en BD
        PasswordResetCode resetCode = new PasswordResetCode(
            user.getIdUser(),
            email,
            code,
            expiresAt
        );
        resetCodeRepository.save(resetCode);
        
        // 7. Enviar email
        emailService.sendPasswordResetCode(email, code);
        
        // 8. Retornar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Código enviado al email");
        response.put("expiresIn", CODE_EXPIRATION_MINUTES * 60); // en segundos
        
        return response;
    }
    
    /**
     * Verifica el código de recuperación y genera un token temporal
     */
    @Transactional
    public Map<String, Object> verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        
        // 1. Buscar código activo
        PasswordResetCode resetCode = resetCodeRepository
                .findByEmailAndCodeAndIsUsedFalse(email, code)
                .orElseThrow(() -> new InvalidCodeException("Código incorrecto"));
        
        // 2. Verificar expiración
        if (resetCode.isExpired()) {
            resetCode.markAsUsed();
            resetCodeRepository.save(resetCode);
            throw new CodeExpiredException("Código expirado. Por favor solicita uno nuevo");
        }
        
        // 3. Incrementar intentos
        resetCode.incrementAttempts();
        
        // 4. Verificar máximo de intentos
        if (resetCode.getAttempts() >= MAX_ATTEMPTS) {
            resetCode.markAsUsed();
            resetCodeRepository.save(resetCode);
            throw new MaxAttemptsExceededException(
                "Máximo de intentos alcanzado. Por favor solicita un nuevo código");
        }
        
        // Si aún no alcanzó el máximo, guardar el incremento
        resetCodeRepository.save(resetCode);
        
        // 5. Si llegó aquí, el código es correcto - generar token temporal
        String resetToken = jwtService.generateResetToken(email, RESET_TOKEN_EXPIRATION_MINUTES);
        
        // 6. Marcar código como usado
        resetCode.markAsUsed();
        resetCodeRepository.save(resetCode);
        
        // 7. Retornar token
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Código verificado correctamente");
        response.put("resetToken", resetToken);
        
        return response;
    }
    
    /**
     * Restablece la contraseña usando el token temporal
     */
    @Transactional
    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        String resetToken = request.getResetToken();
        String newPassword = request.getNewPassword();
        
        // 1. Validar y extraer email del token
        String email;
        try {
            email = jwtService.extractEmailFromResetToken(resetToken);
        } catch (InvalidResetTokenException e) {
            throw new InvalidResetTokenException("Token inválido o expirado");
        }
        
        // 2. Validar longitud de contraseña
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException(
                String.format("La contraseña debe tener al menos %d caracteres", MIN_PASSWORD_LENGTH));
        }
        
        // 3. Buscar usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        
        // 4. Hashear nueva contraseña
        String hashedPassword = passwordEncoder.encode(newPassword);
        
        // 5. Actualizar contraseña
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        
        // 6. Retornar éxito
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contraseña actualizada exitosamente");
        
        return response;
    }
}
