package org.example.springboot_backend.repository;
import org.example.springboot_backend.entity.PlanPermission;
import org.example.springboot_backend.entity.PlanPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlanPermissionRepository extends JpaRepository<PlanPermission, PlanPermissionId> {

    @Query("""
        SELECT pp.permission.name
        FROM PlanPermission pp
        WHERE pp.plan.id = :planId
    """)
    List<String> findPermissionNamesByPlanId(@Param("planId") UUID planId);
}
