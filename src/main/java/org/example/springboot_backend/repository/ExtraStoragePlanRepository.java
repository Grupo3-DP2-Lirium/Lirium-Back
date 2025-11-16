package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ExtraStoragePlanRepository extends JpaRepository<ExtraStoragePlan, UUID> {
    Optional<ExtraStoragePlan> findByName(String name);
    Optional<ExtraStoragePlan> findByIdExtraPlan(UUID idExtraPlan);
}
