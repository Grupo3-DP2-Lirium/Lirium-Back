package org.example.springboot_backend.controller;

import org.example.springboot_backend.entity.Permission;
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
        List<Plan> plans = planService.getAllActivePlans();

        // Imprimir detalles de cada plan y sus permisos
        for (Plan plan : plans) {
            System.out.println("Plan:");
            System.out.println("  ID: " + plan.getIdPlan());
            System.out.println("  Name: " + plan.getName());
            System.out.println("  Description: " + plan.getDescription());
            System.out.println("  Price: " + plan.getPrice());
            System.out.println("  Currency: " + plan.getCurrency());
            System.out.println("  PayPal Plan ID: " + plan.getPaypalPlanId());

            System.out.println("  Permissions:");
            if (plan.getPermissions() != null) {
                for (Permission p : plan.getPermissions()) {
                    System.out.println("    - " + p.getName());
                }
            } else {
                System.out.println("    Ninguno");
            }
        }

        return plans;
    }


    // Obtener un plan por su ID
    @GetMapping("/{idPlan}")
    public Plan getPlanById(@PathVariable UUID idPlan) {
        return planService.getPlanById(idPlan);
    }
}
