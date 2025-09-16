package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
