package org.example.springboot_backend.dto;

import java.time.LocalDate;

public class MemorialRequest {
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String gender;
    private String description;
    private String relationType;
    // private String coverURL; // It's random, use some of the images uploaded in memories
    private boolean collaborative;
    private boolean journal;

    // getters y setters
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
    // public String getCoverURL() { return coverURL; }
    // public void setCoverURL(String coverURL) { this.coverURL = coverURL; }
    public boolean isCollaborative() { return collaborative; }
    public void setCollaborative(boolean collaborative) { this.collaborative = collaborative; }
    public boolean isJournal() { return journal; }
    public void setJournal(boolean journal) { this.journal = journal; }
}
