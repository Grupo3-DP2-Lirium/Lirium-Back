package org.example.springboot_backend.dto;

import java.util.List;
import java.util.UUID;

public class PublicMemorialDto {
    
    private UUID idMemorial;
    private String name;
    private String nickname;
    private String description;
    private String coverURL;
    private String birthDate;
    private String gender;
    private List<PublicMemoryDto> memories;
    
    // Constructors
    public PublicMemorialDto() {
    }
    
    public PublicMemorialDto(UUID idMemorial, String name, String nickname, String description, String coverURL) {
        this.idMemorial = idMemorial;
        this.name = name;
        this.nickname = nickname;
        this.description = description;
        this.coverURL = coverURL;
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
    
    public String getCoverURL() {
        return coverURL;
    }
    
    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }
    
    public String getBirthDate() {
        return birthDate;
    }
    
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public List<PublicMemoryDto> getMemories() {
        return memories;
    }
    
    public void setMemories(List<PublicMemoryDto> memories) {
        this.memories = memories;
    }
}
