package org.example.springboot_backend.dto;

import java.util.List;
import java.util.UUID;

public class PublicMemorialDto {
    
    private UUID idMemorial;
    private String name;
    private String nickname;
    private String description;
    private String profilePhotoUrl;
    private String backgroundUrl; // Para fondos por defecto
    private String birthDate;
    private String deathDate; // Fecha de fallecimiento (opcional)
    private String gender;
    private String relationType;
    private List<PublicMemoryDto> memories;
    
    // Constructors
    public PublicMemorialDto() {
    }
    
    public PublicMemorialDto(UUID idMemorial, String name, String nickname, String description, String profilePhotoUrl, String backgroundUrl) {
        this.idMemorial = idMemorial;
        this.name = name;
        this.nickname = nickname;
        this.description = description;
        this.profilePhotoUrl = profilePhotoUrl;
        this.backgroundUrl = backgroundUrl;
    }
    
    // Getters and Setters
    public UUID getIdMemorial() {
        return idMemorial;
    }
    
    public void setIdMemorial(UUID idMemorial) {
        this.idMemorial = idMemorial;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }
    
    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
    
    public String getBackgroundUrl() {
        return backgroundUrl;
    }
    
    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }
    
    public String getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getDeathDate() {
        return deathDate;
    }
    
    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getRelationType() {
        return relationType;
    }
    
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
    
    public List<PublicMemoryDto> getMemories() {
        return memories;
    }
    
    public void setMemories(List<PublicMemoryDto> memories) {
        this.memories = memories;
    }
}
