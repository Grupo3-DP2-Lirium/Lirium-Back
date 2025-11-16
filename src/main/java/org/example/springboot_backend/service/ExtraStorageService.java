package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.entity.UserExtraStorage;
import org.example.springboot_backend.enums.PaymentMethod;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.ExtraStoragePlanRepository;
import org.example.springboot_backend.repository.UserExtraStorageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExtraStorageService {

    private final UserExtraStorageRepository userExtraStorageRepository;
    private final ExtraStoragePlanRepository extraStoragePlanRepository;

    public ExtraStorageService(UserExtraStorageRepository userExtraStorageRepository,
                               ExtraStoragePlanRepository extraStoragePlanRepository) {
        this.userExtraStorageRepository = userExtraStorageRepository;
        this.extraStoragePlanRepository = extraStoragePlanRepository;
    }

    // Crear suscripción de almacenamiento extra
    public UserExtraStorage createExtraStorageSubscription(User user, UUID planId, String paypalSubscriptionId, PaymentMethod paymentMethod) {
        ExtraStoragePlan plan = extraStoragePlanRepository.findByIdExtraPlan(planId)
                .orElseThrow(() -> new RuntimeException("Plan de almacenamiento no encontrado"));

        Optional<UserExtraStorage> existing =
                userExtraStorageRepository.findByUserAndPlanAndEndDateIsNull(user, plan);

        if (existing.isPresent()) {
            throw new RuntimeException("Ya tienes este plan de almacenamiento activo.");
        }

        UserExtraStorage storage = new UserExtraStorage();
        storage.setUser(user);
        storage.setPlan(plan);
        storage.setStatus(SubscriptionStatus.ACTIVE);
        storage.setFrequency(plan.getFrequency()); // asumimos que ExtraStoragePlan tiene frequency
        storage.setCurrentPaymentMethod(paymentMethod);
        storage.setPaypalSubscriptionId(paypalSubscriptionId);
        storage.setStartDate(LocalDateTime.now());
        storage.setCreatedDate(LocalDateTime.now());
        storage.setUpdatedDate(LocalDateTime.now());

        return userExtraStorageRepository.save(storage);
    }

    // Cancelar suscripción de almacenamiento extra
    public UserExtraStorage cancelExtraStorageSubscription(UserExtraStorage storage) {
        if (storage.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Esta suscripción ya está cancelada o no está activa.");
        }

        // Calcular endDate según frecuencia
        LocalDateTime end = storage.getStartDate();
        LocalDateTime now = LocalDateTime.now();

        switch (storage.getFrequency()) {
            case "MONTHLY" -> {
                while (!end.isAfter(now)) {
                    end = end.plusMonths(1);
                }
            }
            case "YEARLY" -> {
                while (!end.isAfter(now)) {
                    end = end.plusYears(1);
                }
            }
            default -> throw new RuntimeException("Frecuencia desconocida");
        }

        storage.setEndDate(end);
        storage.setStatus(SubscriptionStatus.CANCELLED);
        storage.setUpdatedDate(LocalDateTime.now());

        return userExtraStorageRepository.save(storage);
    }

    // Obtener todos los add-ons activos
    public List<UserExtraStorage> getActiveExtraStorageSubscriptions(User user) {
        return userExtraStorageRepository.findAllByUserAndEndDateIsNull(user);
    }


    // Listar todos los planes de almacenamiento extra
    public List<ExtraStoragePlan> getAllExtraStoragePlans() {
        return extraStoragePlanRepository.findAll();
    }

    // Obtener la primera suscripción activa de un usuario (opcional)
    public UserExtraStorage getActiveExtraStorageSubscription(User user) {
        return userExtraStorageRepository.findByUserAndEndDateIsNull(user).orElse(null);
    }


}
