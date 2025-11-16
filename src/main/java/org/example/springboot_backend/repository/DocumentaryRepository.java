package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Documentary;
import org.example.springboot_backend.enums.DocumentaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentaryRepository extends JpaRepository<Documentary, UUID> {

    // Encontrar todos los documentales de un memorial
    List<Documentary> findByMemorial_IdMemorial(UUID memorialId);

    // Encontrar documentales por memorial y estado (para separar borradores y publicados)
    List<Documentary> findByMemorial_IdMemorialAndStatus(UUID memorialId, DocumentaryStatus status);

    // Encontrar documentales por usuario creador
    List<Documentary> findByCreatedBy_IdUser(UUID userId);

    // Encontrar documentales por usuario y estado
    List<Documentary> findByCreatedBy_IdUserAndStatus(UUID userId, DocumentaryStatus status);

    // Encontrar documentales por estado
    List<Documentary> findByStatus(DocumentaryStatus status);

    // Encontrar documentales en proceso (para monitoreo)
    List<Documentary> findByStatusIn(List<DocumentaryStatus> statuses);

    // Contar documentales por memorial
    long countByMemorial_IdMemorial(UUID memorialId);

    @Query("SELECT COUNT(d) FROM Documentary d WHERE d.createdBy.idUser = :userId AND d.createdDate BETWEEN :start AND :end")
    int countByUserAndDateRange(@Param("userId") UUID userId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    // Contar por memorial y estado
    long countByMemorial_IdMemorialAndStatus(UUID memorialId, DocumentaryStatus status);
}