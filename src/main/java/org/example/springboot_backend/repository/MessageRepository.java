package org.example.springboot_backend.repository;

import org.example.springboot_backend.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Buscar mensajes por estado
    List<Message> findByStatus(String status);
    Page<Message> findByStatus(String status, Pageable pageable);
    
    // Buscar mensajes por prioridad
    List<Message> findByPriority(String priority);
    
    // Buscar mensajes por categoría
    List<Message> findByCategory(String category);
    
    // Buscar mensajes por usuario remitente
    List<Message> findBySenderUserId(String senderUserId);
    
    // Buscar todos los mensajes ordenados por fecha de creación
    Page<Message> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Buscar mensajes no leídos
    @Query("SELECT m FROM Message m WHERE m.status = 'UNREAD' ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessages();
    
    // Contar mensajes por estado
    long countByStatus(String status);
    
    // Buscar mensajes por rango de fechas
    @Query("SELECT m FROM Message m WHERE m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt DESC")
    List<Message> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Buscar mensajes con filtros múltiples
    @Query("""
        SELECT m FROM Message m 
        WHERE (:status IS NULL OR m.status = :status)
        AND (:priority IS NULL OR m.priority = :priority)
        AND (:category IS NULL OR m.category = :category)
        AND (:search IS NULL OR :search = '' OR 
             LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(m.message) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(m.senderName) LIKE LOWER(CONCAT('%', :search, '%')) OR
             LOWER(m.senderEmail) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY m.createdAt DESC
    """)
    Page<Message> findMessagesWithFilters(
        @Param("status") String status,
        @Param("priority") String priority,
        @Param("category") String category,
        @Param("search") String search,
        Pageable pageable
    );
    
    // Estadísticas de mensajes
    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :date")
    long countMessagesAfterDate(@Param("date") LocalDateTime date);
}