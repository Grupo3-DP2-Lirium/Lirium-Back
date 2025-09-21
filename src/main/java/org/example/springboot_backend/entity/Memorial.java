package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Memorial {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idMemorial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user; // owner/creator of the memorial

    private String name; // name of the person
    private String nickname;
    private LocalDate birthDate;
    private String gender;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String relationType; // free option
    private boolean isCollaborative;
    private boolean isJournal;
    private Double usedSpace;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_photo_id")
    private File profilePhoto;
    private String coverURL; // It's random, use some of the images uploaded in memories

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // getters and setters
    public UUID getIdMemorial() { return idMemorial; }
    public void setIdMemorial(UUID idMemorial) { this.idMemorial = idMemorial; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getCoverURL() { return coverURL; }
    public void setCoverURL(String coverURL) { this.coverURL = coverURL; }
    public boolean isCollaborative() { return isCollaborative; }
    public void setCollaborative(boolean collaborative) { this.isCollaborative = collaborative; }
    public boolean isJournal() { return isJournal; }
    public void setJournal(boolean journal) { this.isJournal = journal; }
    public Double getUsedSpace() { return usedSpace; }
    public void setUsedSpace(Double usedSpace) { this.usedSpace = usedSpace; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public File getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(File profilePhoto) { this.profilePhoto = profilePhoto; }
}
