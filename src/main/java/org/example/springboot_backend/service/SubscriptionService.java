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
    /*public Subscription createSubscription(User user, UUID planId, PaymentMethod method, BillingPeriod frequency) {
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
*/
    /*public Subscription createSubscription(User user, UUID planId, PaymentMethod method, BillingPeriod frequency) {
        Plan plan = planRepository.findByIdPlan(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // Buscar suscripción existente para este plan
        Subscription subscription = subscriptionRepository.findByUserAndPlan(user, plan)
                .orElse(null);

        if (subscription == null) {
            // No existe → crear nueva
            subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPlan(plan);
            subscription.setCreatedDate(LocalDateTime.now());
        }

        // Actualizar o activar la suscripción
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCurrentPaymentMethod(method);
        subscription.setFrequency(frequency.name());
        subscription.setStartDate(LocalDateTime.now());

        // Calcular endDate según frecuencia
        switch (frequency) {
            case MONTHLY -> subscription.setEndDate(LocalDateTime.now().plusMonths(1));
            case YEARLY -> subscription.setEndDate(LocalDateTime.now().plusYears(1));
            default -> subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        }

        subscription.setUpdatedDate(LocalDateTime.now()); 

        return subscriptionRepository.save(subscription);
    }
*/

    public Subscription createSubscription(User user, UUID planId, PaymentMethod method, BillingPeriod frequency) {
        Plan plan = planRepository.findByIdPlan(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // Buscar la suscripción activa actual del usuario
        Subscription activeSubscription = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        if (activeSubscription != null) {
            // Misma suscripción, mismo plan → solo ACTUALIZAR (no crear nueva)
            if (activeSubscription.getPlan().getIdPlan().equals(planId)) {

                // Solo actualizamos si algo cambia
                boolean needsUpdate = false;

                if (!activeSubscription.getFrequency().equals(frequency.name())) {
                    activeSubscription.setFrequency(frequency.name());
                    needsUpdate = true;

                    switch (frequency) {
                        case MONTHLY -> activeSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
                        case YEARLY -> activeSubscription.setEndDate(LocalDateTime.now().plusYears(1));
                    }
                }

                if (activeSubscription.getCurrentPaymentMethod() != method) {
                    activeSubscription.setCurrentPaymentMethod(method);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    activeSubscription.setUpdatedDate(LocalDateTime.now());
                    return subscriptionRepository.save(activeSubscription);
                }

                throw new RuntimeException("Ya tienes este plan activo sin cambios.");
            }

            // Si es otro plan → cancelar la suscripción actual
            activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
            activeSubscription.setUpdatedDate(LocalDateTime.now());
            subscriptionRepository.save(activeSubscription);
        }

        // Crear nueva si es un plan diferente
        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setPlan(plan);
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setCurrentPaymentMethod(method);
        newSubscription.setFrequency(frequency.name());
        newSubscription.setStartDate(LocalDateTime.now());

        switch (frequency) {
            case MONTHLY -> newSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
            case YEARLY -> newSubscription.setEndDate(LocalDateTime.now().plusYears(1));
        }

        newSubscription.setCreatedDate(LocalDateTime.now());
        newSubscription.setUpdatedDate(LocalDateTime.now());

        return subscriptionRepository.save(newSubscription);
    }

    public Subscription getActiveSubscription(User user) {
        // Buscar suscripción activa para el usuario (solo un plan activo por tipo)
        return subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElse(null); // null → significa que es Free
    }


}
