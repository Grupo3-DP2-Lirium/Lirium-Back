package org.example.springboot_backend.controller;

import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.service.PlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // Obtener todos los planes activos
    @GetMapping("/available")
    public List<Plan> getAvailablePlans() {
        return planService.getAllActivePlans();
    }

    // Obtener un plan por su ID
    @GetMapping("/{idPlan}")
    public Plan getPlanById(@PathVariable UUID idPlan) {
        return planService.getPlanById(idPlan);
    }
}
