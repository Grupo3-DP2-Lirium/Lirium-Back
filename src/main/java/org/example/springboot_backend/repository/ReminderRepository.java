package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Encuentra todos los recordatorios de un usuario, ordenados por fecha de notificación
     */
    List<Reminder> findByUserIdOrderByNotificationDateAsc(UUID userId);

    @Query("SELECT r FROM Reminder r WHERE r.userId = :userId " +
        "AND r.notificationDate BETWEEN :startDate AND :endDate " +
        "ORDER BY r.notificationDate ASC")
    List<Reminder> findUpcomingReminders(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM Reminder r WHERE r.userId = :userId " +
        "AND r.active = true " +
        "AND r.notificationDate BETWEEN :startDate AND :endDate " +
        "ORDER BY r.notificationDate ASC")
    List<Reminder> findActiveUpcomingReminders(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    List<Reminder> findByUserIdAndActiveTrue(UUID userId);

    long countByUserIdAndActiveTrue(UUID userId);
}