package org.example.springboot_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {
    @NotBlank(message = "El asunto es requerido")
    @Size(max = 255, message = "El asunto no puede exceder 255 caracteres")
    private String subject;

    @NotBlank(message = "El mensaje es requerido")
    @Size(max = 5000, message = "El mensaje no puede exceder 5000 caracteres")
    private String message;

    private String priority = "NORMAL";
    private String category = "SUPPORT";

    // Constructors
    public CreateMessageRequest() {}

    public CreateMessageRequest(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}