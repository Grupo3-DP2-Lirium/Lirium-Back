package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "id_permission", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID idPermission;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "permissions")
    private Set<Plan> plans;

    public Permission() {
        this.idPermission = UUID.randomUUID();
    }

    public Permission(String name, String description) {
        this.idPermission = UUID.randomUUID();
        this.name = name;
        this.description = description;
    }

    // Getters y setters
    public UUID getIdPermission() { return idPermission; }
    public void setIdPermission(UUID idPermission) { this.idPermission = idPermission; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Plan> getPlans() { return plans; }
    public void setPlans(Set<Plan> plans) { this.plans = plans; }
}
