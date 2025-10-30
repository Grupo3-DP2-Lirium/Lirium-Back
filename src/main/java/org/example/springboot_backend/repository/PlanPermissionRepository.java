package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.PlanPermission;
import org.example.springboot_backend.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanPermissionRepository extends JpaRepository<PlanPermission, UUID> {
}
