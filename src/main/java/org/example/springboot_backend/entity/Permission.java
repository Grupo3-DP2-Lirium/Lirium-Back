package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPermission;

    @Column(nullable = false, unique = true)
    private String name; // CREATE_MEMORY, DELETE_MEMORIAL, etc.

    private String description;

    public Long getIdPermission() { return idPermission; }
    public void setIdPermission(Long idPermission) { this.idPermission = idPermission; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}