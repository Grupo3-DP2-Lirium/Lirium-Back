package org.example.springboot_backend.dto;

import java.util.UUID;

public class UserLiteResponse {
    private UUID idUser;
    private String name;
    private String profilePhotoUrl;

    public UserLiteResponse() {
    }

    public UserLiteResponse(UUID idUser, String fullName, String s) {
        this.idUser = idUser;
        this.name = fullName;
        this.profilePhotoUrl = s;
    }

    public UUID getIdUser() {
        return idUser;
    }

    public void setIdUser(UUID idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }
}