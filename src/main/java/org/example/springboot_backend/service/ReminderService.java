package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.ReminderRequest;
import org.example.springboot_backend.dto.ReminderResponse;
import org.example.springboot_backend.entity.Reminder;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    /**
     * Obtiene todos los recordatorios de un usuario
     */
    public List<ReminderResponse> getRemindersByUser(User user) {
        List<Reminder> reminders = reminderRepository.findByUserIdOrderByNotificationDateAsc(
            user.getIdUser()
        );
        
        return reminders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene recordatorios próximos (dentro de los próximos N días)
     */
    public List<ReminderResponse> getUpcomingReminders(User user, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        List<Reminder> reminders = reminderRepository.findUpcomingReminders(
            user.getIdUser(),
            now,
            futureDate
        );
        
        return reminders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene recordatorios activos próximos (para el carrusel del home)
     */
    public List<ReminderResponse> getActiveUpcomingReminders(User user, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        List<Reminder> reminders = reminderRepository.findActiveUpcomingReminders(
            user.getIdUser(),
            now,
            futureDate
        );
        
        return reminders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo recordatorio
     */
    @Transactional
    public ReminderResponse createReminder(ReminderRequest request, User user) {
        Reminder reminder = new Reminder();
        reminder.setUserId(user.getIdUser());
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setNotificationDate(request.getNotificationDate());
        reminder.setActive(true);
        reminder.setCreatedDate(LocalDateTime.now());
        
        Reminder savedReminder = reminderRepository.save(reminder);
        
        return toResponse(savedReminder);
    }

    /**
     * Actualiza un recordatorio existente
     */
    @Transactional
    public ReminderResponse updateReminder(Long reminderId, ReminderRequest request, User user) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + reminderId));
        
        // Verificar que el recordatorio pertenece al usuario
        if (!reminder.getUserId().equals(user.getIdUser())) {
            throw new RuntimeException("You don't have permission to update this reminder");
        }
        
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setNotificationDate(request.getNotificationDate());
        reminder.setUpdatedDate(LocalDateTime.now());
        
        Reminder updatedReminder = reminderRepository.save(reminder);
        
        return toResponse(updatedReminder);
    }

    /**
     * Activa o desactiva un recordatorio
     */
    @Transactional
    public ReminderResponse toggleReminderActive(Long reminderId, boolean active, User user) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + reminderId));
        
        // Verificar que el recordatorio pertenece al usuario
        if (!reminder.getUserId().equals(user.getIdUser())) {
            throw new RuntimeException("You don't have permission to modify this reminder");
        }
        
        reminder.setActive(active);
        reminder.setUpdatedDate(LocalDateTime.now());
        
        Reminder updatedReminder = reminderRepository.save(reminder);
        
        return toResponse(updatedReminder);
    }

    /**
     * Elimina un recordatorio
     */
    @Transactional
    public void deleteReminder(Long reminderId, User user) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + reminderId));
        
        // Verificar que el recordatorio pertenece al usuario
        if (!reminder.getUserId().equals(user.getIdUser())) {
            throw new RuntimeException("You don't have permission to delete this reminder");
        }
        
        reminderRepository.delete(reminder);
    }

    /**
     * Convierte una entidad Reminder a ReminderResponse
     */
    private ReminderResponse toResponse(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();
        response.setIdReminder(reminder.getIdReminder());
        response.setTitle(reminder.getTitle());
        response.setDescription(reminder.getDescription());
        response.setNotificationDate(reminder.getNotificationDate());
        response.setActive(reminder.isActive());
        response.setCreatedDate(reminder.getCreatedDate());
        response.setUpdatedDate(reminder.getUpdatedDate());
        return response;
    }
}