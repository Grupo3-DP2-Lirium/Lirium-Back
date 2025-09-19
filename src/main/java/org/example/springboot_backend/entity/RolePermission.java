package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permission")
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRolePermission;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "permission_id")
    private Permission permission;

    public Long getIdRolePermission() { return idRolePermission; }
    public void setIdRolePermission(Long idRolePermission) { this.idRolePermission = idRolePermission; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
}