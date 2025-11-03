package org.example.springboot_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe proporcionar un email válido")
    private String email;
    
    public ForgotPasswordRequest() {
    }
    
    public ForgotPasswordRequest(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
