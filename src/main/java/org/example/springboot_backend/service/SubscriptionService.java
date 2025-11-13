package org.example.springboot_backend.service;
import org.example.springboot_backend.entity.BillingPeriod;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.PlanPermissionRepository;
import org.example.springboot_backend.repository.PlanRepository;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlanPermissionRepository planPermissionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, PlanRepository planRepository, PlanPermissionRepository planPermissionRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.planPermissionRepository = planPermissionRepository;
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    // Crear una nueva suscripción para un usuario
    public Subscription createSubscription(User user, UUID planId, String subscriptionId, PaymentMethod method, BillingPeriod frequency) {
        Plan plan = planRepository.findByIdPlan(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // Buscar suscripción del mismo plan
        Subscription activeSubscription = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .orElse(null);

        if (activeSubscription != null) {
            throw new RuntimeException("Ya tienes una suscripción activa. Cancélala antes de crear una nueva.");
        }

        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setPlan(plan);
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setCurrentPaymentMethod(method);
        newSubscription.setFrequency(frequency.name());
        newSubscription.setStartDate(LocalDateTime.now());
        newSubscription.setPaypalSubscriptionId(subscriptionId);

        /*switch (frequency) { // No hay endDate hasta que cancele o expire
            case MONTHLY -> newSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
            case YEARLY -> newSubscription.setEndDate(LocalDateTime.now().plusYears(1));
        }*/

        newSubscription.setCreatedDate(LocalDateTime.now());
        newSubscription.setUpdatedDate(LocalDateTime.now());

        return subscriptionRepository.save(newSubscription);
    }

    public Subscription getActiveSubscription(User user) {
        // Buscar suscripción activa para el usuario (solo un plan activo por tipo)
        return subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElse(null); // null → significa que es Free
    }

    public List<String> getPlanPermissions(UUID planId) {
        return planPermissionRepository.findPermissionNamesByPlanId(planId);
    }

    public Subscription cancelSubscription(User user) {
        Subscription activeSub = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        if (activeSub == null) {
            throw new RuntimeException("No active subscription to cancel.");
        }

        activeSub.setStatus(SubscriptionStatus.CANCELLED);
        activeSub.setUpdatedDate(LocalDateTime.now());

        BillingPeriod frequency = BillingPeriod.valueOf(activeSub.getFrequency());
        LocalDateTime start = activeSub.getStartDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = start;

        // Avanzar ciclos hasta encontrar el siguiente corte real
        switch (frequency) {
            case MONTHLY -> {
                while (!end.isAfter(now)) {
                    end = end.plusMonths(1);
                }
            }
            case YEARLY -> {
                while (!end.isAfter(now)) {
                    end = end.plusYears(1);
                }
            }
            default -> throw new RuntimeException("Unknown billing frequency");
        }

        activeSub.setEndDate(end);
        activeSub.setStatus(SubscriptionStatus.CANCELLED);
        activeSub.setUpdatedDate(LocalDateTime.now());

        return subscriptionRepository.save(activeSub);
    }

}
