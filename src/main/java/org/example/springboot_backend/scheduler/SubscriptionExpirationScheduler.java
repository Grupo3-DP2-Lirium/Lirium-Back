package org.example.springboot_backend.scheduler;
import org.example.springboot_backend.entity.Subscription;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.SubscriptionStatus;
import org.example.springboot_backend.repository.SubscriptionRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.NotificationService;
import org.example.springboot_backend.repository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class SubscriptionExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionExpirationScheduler.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    //@Autowired
    //private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;
    
    // Se ejecuta cada 5 minutos
    @Scheduled(cron = "0 */1 * * * *")
    public void expireEndedSubscriptions() {
        logger.info("Running subscription expiration scheduler");

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Buscar subs activas ya vencidas
        List<Subscription> expiredSubs =
                subscriptionRepository.findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, now);

        if (expiredSubs.isEmpty()) {
            logger.info("No expired subscriptions found.");
            return;
        }

        logger.info("Found {} subscriptions to expire", expiredSubs.size());

        expiredSubs.forEach(sub -> {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setUpdatedDate(now);
            subscriptionRepository.save(sub);

            User user = sub.getUser();

            // Bytes del plan que expira
            double planBytes = sub.getPlan().getStorageLimitGb() * 1024.0 * 1024 * 1024;

            // Capacidad actual del usuario
            double currentBytes = user.getTotalCapacity();
            // Imprimir para depuración
            logger.info("User {} currentBytes: {} bytes, planBytes to subtract: {} bytes",
                        user.getEmail(), currentBytes, planBytes);


            // Restar plan expirado
            double newBytes = currentBytes - planBytes;

            // Aplicar la lógica que me explicaste
            if (newBytes <= 0) {
                newBytes = 15.0 * 1024 * 1024 * 1024; // mínimo 15GB
            } else {
                newBytes += 15.0 * 1024 * 1024 * 1024; // si queda >0, sumar 15GB
            }

            user.setTotalCapacity(newBytes);
            userRepository.save(user);

            logger.info("User {} capacity updated after plan expiration: {} bytes",
                    user.getEmail(), newBytes);

            logger.warn("Subscription expired: {} - Plan: {} - User: {}",
                    sub.getIdSubscription(),
                    sub.getPlan().getName(),
                    sub.getUser().getEmail()
            );

            // Crear notificación
            notificationService.notifySubscriptionExpired(sub.getUser(), sub.getPlan().getName());
           
            /*try {
                messagingTemplate.convertAndSendToUser(
                        sub.getUser().getEmail(),  // esto debe coincidir con lo que Flutter usa como "user"
                        "/topic/notifications",
                        "Tu suscripción ha expirado "
                );
                logger.info("Notificación enviada a {}", sub.getUser().getEmail());
            } catch (Exception e) {
                logger.error("Error enviando WS a {}: {}", sub.getUser().getEmail(), e.getMessage());
            }*/
        });

        logger.info("Expiration process finished.");
    }

    @Scheduled(cron = "0 0 2 * * *") // 2 AM todos los días
    // @Scheduled(fixedRate = 1000)
    public void cleanOldExpiredSubs() {

        LocalDateTime limit = LocalDateTime.now().minusMonths(6);

        List<Subscription> subs = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.EXPIRED, limit);

        for(Subscription s : subs){
            //memoryRepository.deleteByUser(s.getUser());
            System.out.println("Deleting expired subscription: " + s.getIdSubscription());
        }
    }

}
