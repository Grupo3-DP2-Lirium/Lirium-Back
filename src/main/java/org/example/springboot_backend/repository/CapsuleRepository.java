package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Capsule;
import org.example.springboot_backend.enums.CapsuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {

    // Encontrar cápsulas por memorial
    List<Capsule> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId);

    // Encontrar cápsulas por memorial y estado
    List<Capsule> findByMemorial_IdMemorialAndStatusOrderByCreatedDateDesc(UUID memorialId, CapsuleStatus status);

    // Encontrar cápsulas por usuario creador
    List<Capsule> findByCreatedBy_IdUserOrderByCreatedDateDesc(UUID userId);

    // Encontrar cápsulas por usuario y estado
    List<Capsule> findByCreatedBy_IdUserAndStatusOrderByCreatedDateDesc(UUID userId, CapsuleStatus status);

    // Encontrar cápsulas por estado
    List<Capsule> findByStatus(CapsuleStatus status);

    // Contar cápsulas por memorial
    long countByMemorial_IdMemorial(UUID memorialId);

    // Contar por memorial y estado
    long countByMemorial_IdMemorialAndStatus(UUID memorialId, CapsuleStatus status);
}