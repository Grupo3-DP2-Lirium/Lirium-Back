package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.AuditLog;
import org.example.springboot_backend.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);
    
    Page<AuditLog> findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(String userEmail, Pageable pageable);
    
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:userEmail IS NULL OR LOWER(a.userEmail) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(
        @Param("action") AuditAction action,
        @Param("userEmail") String userEmail,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
