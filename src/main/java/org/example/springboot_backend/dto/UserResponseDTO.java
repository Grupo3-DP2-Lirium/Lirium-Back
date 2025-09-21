package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UserResponseDTO {
    
    private UUID idUser;
    private String firstName;
    private String firstLastName;
    private String secondLastName;
    private String email;
    private Set<String> roles; // Solo nombres de roles
    private UserStatus status;
    private Double usedSpace;
    private Double totalCapacity;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private LocalDateTime lastSessionDate;

    // Constructors
    public UserResponseDTO() {}

    // Getters and Setters
    public UUID getIdUser() { return idUser; }
    public void setIdUser(UUID idUser) { this.idUser = idUser; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getFirstLastName() { return firstLastName; }
    public void setFirstLastName(String firstLastName) { this.firstLastName = firstLastName; }
    
    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }
    
    public Double getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Double totalCapacity) { this.totalCapacity = totalCapacity; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }
    
    public LocalDateTime getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(LocalDateTime lastSessionDate) { this.lastSessionDate = lastSessionDate; }
}