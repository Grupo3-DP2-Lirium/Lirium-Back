package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idRole;

    @Column(nullable = false, unique = true)
    private String name; // ADMIN, USER, PREMIUM

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "id_role"),
        inverseJoinColumns = @JoinColumn(name = "id_permission")
    )
    private Set<Permission> permissions = new HashSet<>();

    public UUID getIdRole() { return idRole; }
    public void setIdRole(UUID idRole) { this.idRole = idRole; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}