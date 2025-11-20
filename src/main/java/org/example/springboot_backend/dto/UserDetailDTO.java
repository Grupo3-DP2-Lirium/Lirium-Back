package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para mostrar el detalle completo de un usuario en el panel de administración
 */
public class UserDetailDTO {
    private UUID idUser;
    private String firstName;
    private String firstLastName;
    private String secondLastName;
    private String email;
    private String profilePhotoUrl;
    private UserStatus status;
    private List<String> roles;
    private String planName;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private LocalDateTime lastSessionDate;
    private Double usedSpace;
    private Double totalCapacity;
    
    // Estadísticas del usuario
    private UserStatisticsDTO statistics;
    
    // Información de suscripción activa
    private ActiveSubscriptionDTO activeSubscription;

    // Constructores
    public UserDetailDTO() {}

    public UserDetailDTO(UUID idUser, String firstName, String firstLastName, String secondLastName,
                         String email, String profilePhotoUrl, UserStatus status, List<String> roles,
                         String planName, LocalDate createdDate, LocalDate updatedDate,
                         LocalDateTime lastSessionDate, Double usedSpace, Double totalCapacity,
                         UserStatisticsDTO statistics, ActiveSubscriptionDTO activeSubscription) {
        this.idUser = idUser;
        this.firstName = firstName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
        this.status = status;
        this.roles = roles;
        this.planName = planName;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.lastSessionDate = lastSessionDate;
        this.usedSpace = usedSpace;
        this.totalCapacity = totalCapacity;
        this.statistics = statistics;
        this.activeSubscription = activeSubscription;
    }

    // Getters y Setters
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

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }

    public LocalDateTime getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(LocalDateTime lastSessionDate) { this.lastSessionDate = lastSessionDate; }

    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }

    public Double getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Double totalCapacity) { this.totalCapacity = totalCapacity; }

    public UserStatisticsDTO getStatistics() { return statistics; }
    public void setStatistics(UserStatisticsDTO statistics) { this.statistics = statistics; }

    public ActiveSubscriptionDTO getActiveSubscription() { return activeSubscription; }
    public void setActiveSubscription(ActiveSubscriptionDTO activeSubscription) { this.activeSubscription = activeSubscription; }

    // Métodos útiles
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null) fullName.append(firstName);
        if (firstLastName != null) fullName.append(" ").append(firstLastName);
        if (secondLastName != null && !secondLastName.trim().isEmpty()) {
            fullName.append(" ").append(secondLastName);
        }
        return fullName.toString().trim();
    }

    public Double getUsedSpacePercentage() {
        if (totalCapacity == null || totalCapacity == 0 || usedSpace == null) {
            return 0.0;
        }
        return (usedSpace / totalCapacity) * 100;
    }

    public String getPrimaryRole() {
        if (roles != null && !roles.isEmpty()) {
            // Priorizar ADMIN > PREMIUM > USER
            if (roles.contains("ADMIN")) return "ADMIN";
            if (roles.contains("PREMIUM")) return "PREMIUM";
            return roles.get(0);
        }
        return "USER";
    }
}