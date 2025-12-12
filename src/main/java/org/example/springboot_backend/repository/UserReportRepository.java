package org.example.springboot_backend.repository;

import org.example.springboot_backend.model.UserReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    
    // Buscar reportes por usuario reportado
    List<UserReport> findByReportedUserId(String reportedUserId);
    
    // Buscar reportes por usuario que reporta
    List<UserReport> findByReporterUserId(String reporterUserId);
    
    // Buscar reportes por estado
    List<UserReport> findByStatus(String status);
    
    // Buscar reportes por estado con paginación
    Page<UserReport> findByStatus(String status, Pageable pageable);
    
    // Buscar todos los reportes con paginación ordenados por fecha de creación
    Page<UserReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Buscar reportes pendientes
    @Query("SELECT ur FROM UserReport ur WHERE ur.status = 'PENDING' ORDER BY ur.createdAt DESC")
    List<UserReport> findPendingReports();
    
    // Contar reportes por estado
    long countByStatus(String status);
    
    // Buscar reportes por tipo de contenido
    List<UserReport> findByContentType(String contentType);
    
    // Buscar reportes por razón
    List<UserReport> findByReason(String reason);
    
    // Buscar reportes por usuario reportado y estado
    List<UserReport> findByReportedUserIdAndStatus(String reportedUserId, String status);
}