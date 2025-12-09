package org.example.springboot_backend.scheduler;

import org.example.springboot_backend.entity.Reminder;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.ReminderRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.example.springboot_backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Component
public class ReminderScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);
    
    @Autowired
    private ReminderRepository reminderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * ✅ Se ejecuta cada minuto
     * Ventana: 30 segundos atrás y 30 segundos adelante
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkPendingReminders() {
        logger.debug("🔔 Checking for pending reminders...");
        
        try {
            LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
            
            // ✅ Ventana de búsqueda: -30s a +30s
            LocalDateTime startWindow = nowUtc.minusSeconds(30);
            LocalDateTime endWindow = nowUtc.plusSeconds(30);
            
            logger.debug("🌍 Current UTC time: {}", nowUtc);
            logger.debug("🕐 Checking window: {} to {}", startWindow, endWindow);
            
            List<Reminder> allReminders = reminderRepository.findAll();
            logger.debug("📊 Total reminders in DB: {}", allReminders.size());
            
            int sentCount = 0;
            for (Reminder reminder : allReminders) {
                logger.debug("🔍 Checking reminder ID {}: active={}, date={}", 
                           reminder.getIdReminder(), 
                           reminder.isActive(), 
                           reminder.getNotificationDate());
                
                // ✅ Buscar en ventana de ±30 segundos
                if (reminder.isActive() && 
                    reminder.getNotificationDate().isAfter(startWindow) && 
                    reminder.getNotificationDate().isBefore(endWindow)) {
                    
                    Optional<User> userOpt = userRepository.findByIdUser(reminder.getUserId());
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        notificationService.notifyReminder(
                            user,
                            reminder.getTitle(),
                            reminder.getIdReminder()
                        );
                        
                        sentCount++;
                        logger.info("✅ Sent reminder notification to user: {} for reminder: {}", 
                                   user.getEmail(), reminder.getTitle());
                        
                        // Desactivar el recordatorio después de enviarlo
                        reminder.setActive(false);
                        reminderRepository.save(reminder);
                    } else {
                        logger.warn("⚠️ User not found for reminder ID: {}", reminder.getIdReminder());
                    }
                }
            }
            
            if (sentCount > 0) {
                logger.info("📤 Total reminders sent: {}", sentCount);
            } else {
                logger.debug("📭 No reminders to send at this time");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error checking pending reminders", e);
        }
    }
}