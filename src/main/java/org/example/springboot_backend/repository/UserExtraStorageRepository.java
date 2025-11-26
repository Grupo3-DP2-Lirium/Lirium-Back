package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.UserExtraStorage;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.entity.ExtraStoragePlan;
import org.example.springboot_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserExtraStorageRepository extends JpaRepository<UserExtraStorage, UUID> {
    Optional<UserExtraStorage> findByUserAndEndDateIsNull(User user);
    Optional<UserExtraStorage> findByUserAndPlanAndEndDateIsNull(User user, ExtraStoragePlan plan);
    List<UserExtraStorage> findAllByUserAndEndDateIsNull(User user);
    List<UserExtraStorage> findAllByUserAndStatus(User user, SubscriptionStatus status);
}
