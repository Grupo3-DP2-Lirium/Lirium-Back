package org.example.springboot_backend.service;

import org.example.springboot_backend.dto.CreateMessageRequest;
import org.example.springboot_backend.dto.MessageResponse;
import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.model.Message;
import org.example.springboot_backend.repository.MessageRepository;
import org.example.springboot_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public MessageResponse createMessage(String senderUserId, CreateMessageRequest request) {
        Message message = new Message(senderUserId, request.getSubject(), request.getMessage());
        message.setPriority(request.getPriority());
        message.setCategory(request.getCategory());

        // Obtener información del usuario remitente
        try {
            UUID senderUUID = UUID.fromString(senderUserId);
            Optional<User> senderUser = userRepository.findById(senderUUID);
            if (senderUser.isPresent()) {
                User user = senderUser.get();
                message.setSenderName(user.getFirstName() + " " + user.getFirstLastName());
                message.setSenderEmail(user.getEmail());
            }
        } catch (IllegalArgumentException e) {
            // Si no es un UUID válido, usar el ID como está
            message.setSenderName(senderUserId);
        }

        Message savedMessage = messageRepository.save(message);
        return new MessageResponse(savedMessage);
    }

    public List<MessageResponse> getAllMessages() {
        List<Message> messages = messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 1000)).getContent();
        return messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
    }

    public Page<MessageResponse> getAllMessagesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messages = messageRepository.findAllByOrderByCreatedAtDesc(pageable);
        return messages.map(MessageResponse::new);
    }

    public Page<MessageResponse> getMessagesWithFilters(String status, String priority, String category, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findMessagesWithFilters(status, priority, category, search, pageable);
        return messages.map(MessageResponse::new);
    }

    public List<MessageResponse> getUnreadMessages() {
        List<Message> messages = messageRepository.findUnreadMessages();
        return messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getMessagesByStatus(String status) {
        List<Message> messages = messageRepository.findByStatus(status);
        return messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<MessageResponse> getMessageById(Long id) {
        return messageRepository.findById(id)
                .map(MessageResponse::new);
    }

    public MessageResponse markAsRead(Long id, String adminUserId) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        if ("UNREAD".equals(message.getStatus())) {
            message.setStatus("READ");
            message.setReadAt(LocalDateTime.now());
            
            // Obtener información del administrador
            try {
                UUID adminUUID = UUID.fromString(adminUserId);
                Optional<User> adminUser = userRepository.findById(adminUUID);
                if (adminUser.isPresent()) {
                    User user = adminUser.get();
                    message.setAdminUserId(adminUserId);
                    message.setAdminName(user.getFirstName() + " " + user.getFirstLastName());
                }
            } catch (IllegalArgumentException e) {
                message.setAdminUserId(adminUserId);
                message.setAdminName(adminUserId);
            }
        }

        Message updatedMessage = messageRepository.save(message);
        return new MessageResponse(updatedMessage);
    }

    public MessageResponse replyToMessage(Long id, String adminUserId, String response) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        message.setStatus("REPLIED");
        message.setAdminResponse(response);
        message.setRepliedAt(LocalDateTime.now());

        // Obtener información del administrador
        try {
            UUID adminUUID = UUID.fromString(adminUserId);
            Optional<User> adminUser = userRepository.findById(adminUUID);
            if (adminUser.isPresent()) {
                User user = adminUser.get();
                message.setAdminUserId(adminUserId);
                message.setAdminName(user.getFirstName() + " " + user.getFirstLastName());
            }
        } catch (IllegalArgumentException e) {
            message.setAdminUserId(adminUserId);
            message.setAdminName(adminUserId);
        }

        Message updatedMessage = messageRepository.save(message);
        return new MessageResponse(updatedMessage);
    }

    public MessageResponse updateMessageStatus(Long id, String status, String adminUserId) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        message.setStatus(status);

        // Obtener información del administrador
        try {
            UUID adminUUID = UUID.fromString(adminUserId);
            Optional<User> adminUser = userRepository.findById(adminUUID);
            if (adminUser.isPresent()) {
                User user = adminUser.get();
                message.setAdminUserId(adminUserId);
                message.setAdminName(user.getFirstName() + " " + user.getFirstLastName());
            }
        } catch (IllegalArgumentException e) {
            message.setAdminUserId(adminUserId);
            message.setAdminName(adminUserId);
        }

        Message updatedMessage = messageRepository.save(message);
        return new MessageResponse(updatedMessage);
    }

    public Map<String, Long> getMessageStats() {
        return Map.of(
            "total", messageRepository.count(),
            "unread", messageRepository.countByStatus("UNREAD"),
            "read", messageRepository.countByStatus("READ"),
            "replied", messageRepository.countByStatus("REPLIED"),
            "archived", messageRepository.countByStatus("ARCHIVED"),
            "today", messageRepository.countMessagesAfterDate(LocalDateTime.now().minusDays(1)),
            "thisWeek", messageRepository.countMessagesAfterDate(LocalDateTime.now().minusDays(7)),
            "thisMonth", messageRepository.countMessagesAfterDate(LocalDateTime.now().minusDays(30))
        );
    }

    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    public List<MessageResponse> getMessagesByUser(String userId) {
        List<Message> messages = messageRepository.findBySenderUserId(userId);
        return messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
    }

    public long getMessageCount() {
        return messageRepository.count();
    }

    public void createSampleMessages() {
        // Crear algunos mensajes de ejemplo para pruebas
        Message message1 = new Message("user123", "Problema con la aplicación", "Tengo un problema para acceder a mi cuenta");
        message1.setPriority("HIGH");
        message1.setCategory("SUPPORT");
        message1.setSenderName("Juan Pérez");
        message1.setSenderEmail("juan@example.com");
        messageRepository.save(message1);

        Message message2 = new Message("user456", "Sugerencia de mejora", "Me gustaría sugerir una nueva funcionalidad");
        message2.setPriority("NORMAL");
        message2.setCategory("SUGGESTION");
        message2.setSenderName("María García");
        message2.setSenderEmail("maria@example.com");
        messageRepository.save(message2);

        Message message3 = new Message("user789", "Error crítico", "La aplicación se cierra inesperadamente");
        message3.setPriority("URGENT");
        message3.setCategory("SUPPORT");
        message3.setSenderName("Carlos López");
        message3.setSenderEmail("carlos@example.com");
        messageRepository.save(message3);
    }
}