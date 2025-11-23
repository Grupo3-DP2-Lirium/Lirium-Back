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
import org.example.springboot_backend.repository.UserExtraStorageRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlanPermissionRepository planPermissionRepository;
    private final UserRepository userRepository;
    private final UserExtraStorageRepository userExtraStorageRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, 
        PlanRepository planRepository, 
        PlanPermissionRepository planPermissionRepository, 
        UserRepository userRepository, UserExtraStorageRepository userExtraStorageRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.planPermissionRepository = planPermissionRepository;
        this.userRepository = userRepository;
        this.userExtraStorageRepository = userExtraStorageRepository;
        
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    private double calculateTotalBytes(User user, Plan plan) {
        double baseBytes = plan.getStorageLimitGb() * 1024d * 1024d * 1024d;

        double extraBytes = userExtraStorageRepository
            .findAllByUserAndStatus(user, SubscriptionStatus.ACTIVE)
            .stream()
            .mapToDouble(s -> s.getPlan().getAdditionalStorageGb() * 1024d * 1024d * 1024d)
            .sum();

        return baseBytes + extraBytes;
    }

    // Crear una nueva suscripción para un usuario
    public Subscription createSubscription(User user, UUID planId, String subscriptionId, PaymentMethod method, BillingPeriod frequency) {
        Plan plan = planRepository.findByIdPlan(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        Subscription activeSubscription = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        // Si existe una activa, evaluamos el tipo de cambio
        if (activeSubscription != null && activeSubscription.getEndDate() == null) {

            boolean isUpgrade = plan.getPrice() > activeSubscription.getPlan().getPrice();
            boolean isDowngrade = plan.getPrice() < activeSubscription.getPlan().getPrice();

            if (isUpgrade) {
                // Si quiere upgrade → se hace el cambio inmediato
                activeSubscription.setEndDate(LocalDateTime.now());
                activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
                activeSubscription.setUpdatedDate(LocalDateTime.now());
                subscriptionRepository.save(activeSubscription);
                double newCapacityBytes = calculateTotalBytes(user, plan);
                user.setTotalCapacity(newCapacityBytes);
                userRepository.save(user);
            } else if (isDowngrade) {
                // Si quiere downgrade → no se permite aún
                throw new RuntimeException("Podrás cambiar a este plan cuando finalice tu suscripción actual");
            } else {
                // Si intenta suscribirse al mismo plan
                throw new RuntimeException("Ya tienes este plan activo actualmente.");
            }
        }

        // Crear nueva suscripción (solo si es válido hacerlo)
        Subscription newSubscription = new Subscription();
        newSubscription.setUser(user);
        newSubscription.setPlan(plan);
        newSubscription.setCurrentPaymentMethod(method);
        newSubscription.setFrequency(frequency.name());
        newSubscription.setPaypalSubscriptionId(subscriptionId);
        newSubscription.setCreatedDate(LocalDateTime.now());
        newSubscription.setUpdatedDate(LocalDateTime.now());
        newSubscription.setStartDate(LocalDateTime.now());
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        double capacityBytes = plan.getStorageLimitGb().longValue() * 1024L * 1024L * 1024L;
        user.setTotalCapacity(capacityBytes);        userRepository.save(user);
        return subscriptionRepository.save(newSubscription);
        
        // Si quiere upgrade el plan se hace automáticamente el cambio de planes, el antiguo queda como CANCELLED con fecha de fin y la nueva activa
        // Si quiere downgrade se le dice que lo podra hacer despues de la fecha fin de su plan actual
        // Si cancelo su plan, se le dice que puede volver a suscribirse cuando termine su plan actual
    }

    // Obtener la suscripción activa de un usuario
    public Subscription getActiveSubscription(User user) {
        Subscription activeSub = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        if (activeSub == null) {
            // No tiene suscripción activa → es Free
            // Usuario sin suscripción → asignar plan FREE DESCUBRE_LIRIUM
            Plan freePlan = planRepository.findByName("DESCUBRE_LIRIUM")
                    .orElseThrow(() -> new RuntimeException("Plan DESCUBRE_LIRIUM no encontrado"));

            System.out.println("Usuario sin suscripción activa. Se asigna plan FREE");

            // Crear nueva Subscription temporal (no necesariamente guardada en BD)
            activeSub = new Subscription();
            activeSub.setUser(user);
            activeSub.setPlan(freePlan);
            activeSub.setStatus(SubscriptionStatus.NONE); // Plan FREE
            activeSub.setStartDate(LocalDateTime.now());
            activeSub.setEndDate(null); // ilimitado
            activeSub.setFrequency("FREE");
        }

        // Validar fecha de fin
        if (activeSub.getEndDate() != null && activeSub.getEndDate().isBefore(LocalDateTime.now())) {
            // Suscripción vencida → marcar como CANCELLED en BD si quieres
            //activeSub.setStatus(SubscriptionStatus.CANCELLED);
            //activeSub.setUpdatedDate(LocalDateTime.now());
            
            //subscriptionRepository.save(activeSub);
            //System.out.println(" plan FREE");

            // Retornar null o lanzar excepción
            //throw new RuntimeException("No tienes un plan premium activo");
            // O simplemente: return null;
        }
        // Suscripción activa y vigente
        System.out.println(">>> Suscripción activa:");
        if (activeSub.getPlan() != null) {
            System.out.println("Plan name: " + activeSub.getPlan().getName());
        }
        System.out.println("Status: " + activeSub.getStatus());
        System.out.println("Max Files: " + activeSub.getPlan().getMaxFiles());
        System.out.println("Max Collaborations: " + activeSub.getPlan().getMaxCollaborations());
        System.out.println("Max Documentaries: " + activeSub.getPlan().getMaxDocumentariesPerMonth());
        System.out.println("Storage Limit GB: " + activeSub.getPlan().getStorageLimitGb());

        return activeSub;
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

        //activeSub.setStatus(SubscriptionStatus.CANCELLED);
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
        activeSub.setUpdatedDate(LocalDateTime.now());

        return subscriptionRepository.save(activeSub);
    }

    // QUEDA PENDIENTE
    public Subscription changePlan(User user, UUID newPlanId) {
        Subscription activeSub = subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No hay suscripción activa"));

        Plan newPlan = planRepository.findByIdPlan(newPlanId)
                .orElseThrow(() -> new RuntimeException("Plan nuevo no encontrado"));

        // Si es un upgrade, aplica inmediatamente
        boolean isUpgrade = newPlan.getPrice() > activeSub.getPlan().getPrice();

        if (isUpgrade) {
            activeSub.setPlan(newPlan);
            activeSub.setUpdatedDate(LocalDateTime.now());
            return subscriptionRepository.save(activeSub);
        } else {
            // Si es downgrade, programa el cambio al final del ciclo
            LocalDateTime endOfCycle = calculateEndOfBillingCycle(activeSub);
            Subscription newSub = new Subscription();
            newSub.setUser(user);
            newSub.setPlan(newPlan);
            newSub.setStatus(SubscriptionStatus.ACTIVE); // pendiente hasta final de ciclo
            newSub.setStartDate(endOfCycle);
            newSub.setCreatedDate(LocalDateTime.now());
            newSub.setUpdatedDate(LocalDateTime.now());
            return subscriptionRepository.save(newSub);
        }
    }

    // Calcula el fin del ciclo actual de la suscripción
    private LocalDateTime calculateEndOfBillingCycle(Subscription sub) {
        BillingPeriod frequency = BillingPeriod.valueOf(sub.getFrequency());
        LocalDateTime start = sub.getStartDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = start;

        switch (frequency) {
            case MONTHLY -> {
                while (!end.isAfter(now)) end = end.plusMonths(1);
            }
            case YEARLY -> {
                while (!end.isAfter(now)) end = end.plusYears(1);
            }
            default -> throw new RuntimeException("Unknown billing frequency");
        }
        return end;
    }

}
