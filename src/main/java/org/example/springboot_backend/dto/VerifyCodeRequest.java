package org.example.springboot_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyCodeRequest {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe proporcionar un email válido")
    private String email;
    
    @NotBlank(message = "El código es requerido")
    @Size(min = 6, max = 6, message = "El código debe tener exactamente 6 dígitos")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código debe contener solo dígitos")
    private String code;
    
    public VerifyCodeRequest() {
    }
    
    public VerifyCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}
