package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.PlanPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlanPermissionRepository extends JpaRepository<PlanPermission, UUID> {
}
