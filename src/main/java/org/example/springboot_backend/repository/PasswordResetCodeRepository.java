package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {
    
    /**
     * Busca un código activo (no usado) por email y código
     */
    Optional<PasswordResetCode> findByEmailAndCodeAndIsUsedFalse(String email, String code);
    
    /**
     * Busca todos los códigos no usados de un usuario
     */
    List<PasswordResetCode> findByUserIdAndIsUsedFalse(UUID userId);
    
    /**
     * Busca el código más reciente no expirado de un usuario
     * Útil para rate limiting
     */
    Optional<PasswordResetCode> findFirstByUserIdAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
        UUID userId, LocalDateTime currentTime);
    
    /**
     * Elimina códigos expirados (para limpieza manual si se necesita)
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
