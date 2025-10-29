package org.example.springboot_backend.service;
import org.example.springboot_backend.entity.BillingPeriod;
import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;
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

    public SubscriptionService(SubscriptionRepository subscriptionRepository, PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    // Crear una nueva suscripción para un usuario
    public Subscription createSubscription(User user, UUID planId, PaymentMethod method, BillingPeriod frequency) {
        Plan plan = planRepository.findByIdPlan(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE); // o PENDING si es pago pendiente
        subscription.setCurrentPaymentMethod(method);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setFrequency(frequency.name());

        // Calcular endDate según la frecuencia
        switch (frequency) {
            case MONTHLY -> subscription.setEndDate(LocalDateTime.now().plusMonths(1));
            case YEARLY -> subscription.setEndDate(LocalDateTime.now().plusYears(1));
            default -> subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }

        return subscriptionRepository.save(subscription);
    }
}
