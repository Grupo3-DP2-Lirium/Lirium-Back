package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idUser;

    private String firstName;
    private String firstLastName;
    private String secondLastName;
    private String email;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_photo_id")
    private File profilePhoto;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "id_user"),
        inverseJoinColumns = @JoinColumn(name = "id_role")
    )
    private Set<Role> roles = new HashSet<>();

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private Double usedSpace;
    private Double totalCapacity;

    @Column(name = "documentaries_purchased")
    private Integer documentariesPurchased = 0;

    @Column(name = "documentaries_available")
    private Integer documentariesAvailable = 0;

    private LocalDate createdDate;
    private LocalDate updatedDate;
    private LocalDateTime lastSessionDate;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Subscription> subscriptions = new HashSet<>();

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    // getters and setters
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
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
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
    public String getFullName() { return firstName + " " + firstLastName; }
    public Integer getDocumentariesPurchased() { return documentariesPurchased; }
    public void setDocumentariesPurchased(Integer documentariesPurchased) { this.documentariesPurchased = documentariesPurchased    ; }
    public Integer getDocumentariesAvailable() { return documentariesAvailable; }
    public void setDocumentariesAvailable(Integer documentariesAvailable) { this.documentariesAvailable = documentariesAvailable; }
    public File getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(File profilePhoto) { this.profilePhoto = profilePhoto; }

}