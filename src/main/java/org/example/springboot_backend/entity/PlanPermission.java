package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_permission")
@IdClass(PlanPermissionId.class)
public class PlanPermission {

    @Id
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Id
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    public PlanPermission() {}

    public PlanPermission(Plan plan, Permission permission) {
        this.plan = plan;
        this.permission = permission;
    }

    // Getters y setters
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public Permission getPermission() { return permission; }
    public void setPermission(Permission permission) { this.permission = permission; }
}
