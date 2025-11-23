package org.example.springboot_backend.controller;

import org.example.springboot_backend.dto.UserExtraStorageResponse;
import org.example.springboot_backend.entity.BillingPeriod;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.UserExtraStorage;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.ExtraStorageService;
import org.example.springboot_backend.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final ExtraStorageService extraStorageService;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  UserRepository userRepository,
                                  SubscriptionRepository subscriptionRepository, 
                                  PlanRepository planRepository,
                                  ExtraStorageService extraStorageService) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.extraStorageService = extraStorageService;
    }

    // 1. Listar todos los planes
    @GetMapping("/plans")
    public ResponseEntity<List<Plan>> getAllPlans() {
        List<Plan> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    // 2. Crear una suscripción
    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(
            @RequestParam UUID planId,
            @RequestParam UUID userId,
            @RequestParam PaymentMethod method,
            @RequestParam BillingPeriod frequency
    ) {
        // Buscar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar si ya tiene una suscripción activa
        boolean hasActive = subscriptionRepository.existsByUserIdUserAndStatus(userId, SubscriptionStatus.ACTIVE);
        if (hasActive) {
            return ResponseEntity.badRequest().body("El usuario ya tiene una suscripción activa");
        }

        // Crear suscripción
        Subscription subscription = subscriptionService.createSubscription(user, planId, "-", method, frequency);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping(value = "/current-subscription", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getCurrentSubscription(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Subscription subscription = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                    .orElse(null);

            SubscriptionResponse response = new SubscriptionResponse();

            if (subscription == null) {
                Plan freePlan = planRepository.findByName("DESCUBRE_LIRIUM")
                    .orElseThrow(() -> new RuntimeException("Plan DESCUBRE_LIRIUM no encontrado"));

                System.out.println("Usuario sin suscripción activa. Se asigna plan FREE");
                System.out.println("Support level de freePlan: " + freePlan.getSupportLevel());

                // Usuario Free → status NONE
                response.setStatus(SubscriptionStatus.NONE);
                response.setFrequency(null);
                response.setStartDate(null);
                response.setEndDate(null);
                response.setPaymentMethod(null);
                response.setPlanId(freePlan.getIdPlan());
                response.setPlanName("DESCUBRE_LIRIUM");
                response.setPlanDescription("Plan gratuito");
                response.setPlanPrice(0.0);
                response.setPlanCurrency("USD");
                response.setStorageLimitGb(freePlan.getStorageLimitGb());
                response.setMaxFiles(freePlan.getMaxFiles());
                response.setMaxCollaborations(freePlan.getMaxCollaborations());
                response.setMaxDocumentariesPerMonth(freePlan.getMaxDocumentariesPerMonth());
                response.setSupportLevel(freePlan.getSupportLevel());
            } else {
                // Mapear suscripción y plan
                response.setSubscriptionId(subscription.getIdSubscription());
                response.setStatus(subscription.getStatus());
                response.setFrequency(subscription.getFrequency());
                response.setStartDate(subscription.getStartDate());
                response.setEndDate(subscription.getEndDate());
                response.setPaymentMethod(subscription.getCurrentPaymentMethod());

                Plan plan = subscription.getPlan();
                response.setPlanId(plan.getIdPlan());
                response.setPlanName(plan.getName());
                response.setPlanDescription(plan.getDescription());
                response.setPlanPrice(plan.getPrice());
                response.setPlanCurrency(plan.getCurrency());
                response.setStorageLimitGb(plan.getStorageLimitGb());
                response.setMaxFiles(plan.getMaxFiles());
                response.setMaxCollaborations(plan.getMaxCollaborations());
                response.setMaxDocumentariesPerMonth(plan.getMaxDocumentariesPerMonth());
                response.setSupportLevel(plan.getSupportLevel());
                //response.setPermissions(subscriptionService.getPlanPermissions(plan.getIdPlan()));
                
            }

            // --- Extra Storage ---
            List<UserExtraStorage> extraStorages = extraStorageService.getActiveExtraStorageSubscriptions(user);
            List<UserExtraStorageResponse> extraStorageDTOs = extraStorages.stream()
                    .map(us -> new UserExtraStorageResponse(
                            us.getPlan().getIdExtraPlan(),
                            us.getPlan().getName(),
                            us.getPlan().getAdditionalStorageGb(),
                            us.getStatus().name(),
                            us.getStartDate().toLocalDate()
                    ))
                    .toList();
            
            // Imprimir los detalles de las suscripciones de almacenamiento extra
            System.out.println("Suscripciones de almacenamiento extra activas:");
            for (UserExtraStorageResponse dto : extraStorageDTOs) {
                System.out.println("Plan: " + dto.getPlanName() +
                                ", Almacenamiento adicional: " + dto.getAdditionalStorageGb() + "GB" +
                                ", Estado: " + dto.getStatus());
            }
            response.setExtraStorageSubscriptions(extraStorageDTOs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching current subscription: " + e.getMessage());
        }
    }

    @PutMapping("/cancel")
    public ResponseEntity<String> cancelSubscription(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        subscriptionService.cancelSubscription(user);

        return ResponseEntity.ok("Subscription cancelled successfully");
    }

    @GetMapping("/{planId}/permissions")
    public List<String> getPlanPermissions(@PathVariable("planId") UUID planId) {
        return subscriptionService.getPlanPermissions(planId);
    }
}