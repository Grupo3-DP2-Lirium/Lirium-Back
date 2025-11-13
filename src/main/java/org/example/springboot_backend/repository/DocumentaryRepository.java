package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Documentary;
import org.example.springboot_backend.enums.DocumentaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentaryRepository extends JpaRepository<Documentary, UUID> {

    // Encontrar todos los documentales de un memorial
    List<Documentary> findByMemorial_IdMemorial(UUID memorialId);

    // Encontrar documentales por usuario creador
    List<Documentary> findByCreatedBy_IdUser(UUID userId);

    // Encontrar documentales por estado
    List<Documentary> findByStatus(DocumentaryStatus status);

    // Encontrar documentales en proceso (para monitoreo)
    List<Documentary> findByStatusIn(List<DocumentaryStatus> statuses);

    // Contar documentales por memorial
    long countByMemorial_IdMemorial(UUID memorialId);
}