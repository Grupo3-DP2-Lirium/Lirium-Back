package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "plan_permission")
public class PlanPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public PlanPermission() {}

    public PlanPermission(Plan plan, String permissionName) {
        this(plan, permissionName, true); // por defecto habilitado
    }

    public PlanPermission(Plan plan, String permissionName, boolean enabled) {
        this.plan = plan;
        this.permissionName = permissionName;
        this.enabled = enabled;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
