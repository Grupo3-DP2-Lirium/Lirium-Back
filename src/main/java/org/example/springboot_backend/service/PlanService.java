package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<Plan> getAllActivePlans() {
        return planRepository.findAllByActiveTrue();
    }

    public Plan getPlanById(java.util.UUID idPlan) {
        return planRepository.findById(idPlan)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + idPlan));
    }
}
