package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    List<DeviceToken> findByUserId(UUID userId);
    
    Optional<DeviceToken> findByFcmToken(String fcmToken);
    
    void deleteByFcmToken(String fcmToken);
    /**
     * Elimina todos los tokens de un usuario (útil al eliminar cuenta)
     */
    void deleteByUserId(UUID userId);
}