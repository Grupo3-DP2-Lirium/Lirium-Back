package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idPermission;

    @Column(nullable = false, unique = true)
    private String name; // CREATE_MEMORY, DELETE_MEMORIAL, etc.

    private String description;

    public UUID getIdPermission() { return idPermission; }
    public void setIdPermission(UUID idPermission) { this.idPermission = idPermission; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}