package org.example.springboot_backend.repository;

import org.example.springboot_backend.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    Page<AuditLog> findByAdminIdOrderByTimestampDesc(String adminId, Pageable pageable);
    
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);
    
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:adminId IS NULL OR a.adminId = :adminId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByFilters(
        @Param("adminId") String adminId,
        @Param("action") String action,
        @Param("entityType") String entityType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
