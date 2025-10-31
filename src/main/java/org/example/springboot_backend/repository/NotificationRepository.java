package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * ✅ CORREGIDO: Consulta que primero muestra NO LEÍDAS, luego LEÍDAS
     * Ordena por: isRead ASC (false=0, true=1) y luego por fecha DESC
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
           "ORDER BY n.isRead ASC, n.createdDate DESC")
    List<Notification> findByUserIdOrderByUnreadFirstThenDate(@Param("userId") UUID userId);
    
    /**
     * Obtiene solo notificaciones no leídas ordenadas por fecha
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedDateDesc(UUID userId);
    
    /**
     * Cuenta notificaciones no leídas
     */
    long countByUserIdAndIsReadFalse(UUID userId);
    
    /**
     * Obtiene todas las notificaciones de un usuario ordenadas por fecha
     */
    List<Notification> findByUserIdOrderByCreatedDateDesc(UUID userId);
}