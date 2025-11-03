package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    @Query("""
        SELECT p.name 
        FROM Permission p
        JOIN PlanPermission pp ON pp.permission = p
        WHERE pp.plan.id = :planId
    """)
    List<String> findPermissionNamesByPlanId(@Param("planId") UUID planId);
}