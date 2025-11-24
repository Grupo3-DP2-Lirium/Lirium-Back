package org.example.springboot_backend.service;

import java.time.LocalDateTime;

import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionValidationService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public boolean hasActiveSubscription(User user) {
        return subscriptionRepository.existsByUserAndStatusAndEndDateAfter(
            user, 
            SubscriptionStatus.ACTIVE,
            LocalDateTime.now()
        );
    }
}
