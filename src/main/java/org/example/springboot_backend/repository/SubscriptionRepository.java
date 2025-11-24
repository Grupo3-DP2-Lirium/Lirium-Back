package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Plan;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserIdUser(UUID idUser); //     List<Memory> findByMemorialIdMemorial(UUID idMemorial);
    Optional<Subscription> findByUserIdUserAndPlanIdPlan(UUID idUser, UUID idPlan);
    boolean existsByUserIdUserAndStatus(UUID userId, SubscriptionStatus status);
    Optional<Subscription> findByUserAndPlan(User user, Plan plan);
    Optional<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDateTime date);
    boolean existsByUserAndStatusAndEndDateAfter(
        User user,
        SubscriptionStatus status,
        LocalDateTime now
    );
    List<Subscription> findByUserAndStatusAndEndDateAfter(User user, SubscriptionStatus status, LocalDateTime now);
}