package org.example.springboot_backend.dto;

import org.example.springboot_backend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para mostrar usuarios en la lista del panel de administración
 */
public class UserListDTO {
    private UUID idUser;
    private String firstName;
    private String firstLastName;
    private String secondLastName;
    private String email;
    private String profilePhotoUrl;
    private UserStatus status;
    private String primaryRole;
    private String planName;
    private LocalDate createdDate;
    private LocalDateTime lastSessionDate;
    private Double usedSpace;
    private Double totalCapacity;
    private Integer memorialsCount;
    private Integer memoriesCount;

    // Constructores
    public UserListDTO() {}

    public UserListDTO(UUID idUser, String firstName, String firstLastName, String secondLastName,
                       String email, String profilePhotoUrl, UserStatus status, String primaryRole,
                       String planName, LocalDate createdDate, LocalDateTime lastSessionDate,
                       Double usedSpace, Double totalCapacity, Integer memorialsCount, Integer memoriesCount) {
        this.idUser = idUser;
        this.firstName = firstName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
        this.status = status;
        this.primaryRole = primaryRole;
        this.planName = planName;
        this.createdDate = createdDate;
        this.lastSessionDate = lastSessionDate;
        this.usedSpace = usedSpace;
        this.totalCapacity = totalCapacity;
        this.memorialsCount = memorialsCount;
        this.memoriesCount = memoriesCount;
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

    public String getPrimaryRole() { return primaryRole; }
    public void setPrimaryRole(String primaryRole) { this.primaryRole = primaryRole; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastSessionDate() { return lastSessionDate; }
    public void setLastSessionDate(LocalDateTime lastSessionDate) { this.lastSessionDate = lastSessionDate; }

    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }

    public Double getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Double totalCapacity) { this.totalCapacity = totalCapacity; }

    public Integer getMemorialsCount() { return memorialsCount; }
    public void setMemorialsCount(Integer memorialsCount) { this.memorialsCount = memorialsCount; }

    public Integer getMemoriesCount() { return memoriesCount; }
    public void setMemoriesCount(Integer memoriesCount) { this.memoriesCount = memoriesCount; }

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
}