package org.example.springboot_backend.scheduler;

import org.example.springboot_backend.entity.Reminder;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.NotificationType;
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
     * ✅ Se ejecuta cada minuto en UTC
     * Esto funciona sin importar la zona horaria del servidor
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkPendingReminders() {
        logger.debug("🔔 Checking for pending reminders...");
        
        try {
            // ✅ Usar UTC para todas las comparaciones
            LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime oneMinuteAgoUtc = nowUtc.minusMinutes(1);
            
            logger.debug("🌍 Current UTC time: {}", nowUtc);
            logger.debug("🕐 Checking window: {} to {}", oneMinuteAgoUtc, nowUtc.plusMinutes(1));
            
            // Obtener todos los recordatorios activos
            List<Reminder> allReminders = reminderRepository.findAll();
            logger.debug("📊 Total reminders in DB: {}", allReminders.size());
            
            int sentCount = 0;
            for (Reminder reminder : allReminders) {
                logger.debug("🔍 Checking reminder ID {}: active={}, date={}", 
                           reminder.getIdReminder(), 
                           reminder.isActive(), 
                           reminder.getNotificationDate());
                
                // ✅ notificationDate ya está en UTC (guardado desde el frontend)
                if (reminder.isActive() && 
                    reminder.getNotificationDate().isAfter(oneMinuteAgoUtc) && 
                    reminder.getNotificationDate().isBefore(nowUtc.plusMinutes(1))) {
                    
                    // Buscar usuario por UUID
                    Optional<User> userOpt = userRepository.findByIdUser(reminder.getUserId());
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        
                        // ✅ CORREGIDO: Usar el método genérico
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