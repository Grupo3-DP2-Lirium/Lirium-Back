package org.example.springboot_backend.entity;

import java.io.Serializable;
import java.util.UUID;

public class PlanPermissionId implements Serializable {

    private UUID plan;
    private UUID permission;

    public PlanPermissionId() {}

    public PlanPermissionId(UUID plan, UUID permission) {
        this.plan = plan;
        this.permission = permission;
    }

    // equals() y hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanPermissionId)) return false;
        PlanPermissionId that = (PlanPermissionId) o;
        return plan.equals(that.plan) && permission.equals(that.permission);
    }

    @Override
    public int hashCode() {
        return plan.hashCode() + permission.hashCode();
    }

    // Getters y setters
    public UUID getPlan() { return plan; }
    public void setPlan(UUID plan) { this.plan = plan; }

    public UUID getPermission() { return permission; }
    public void setPermission(UUID permission) { this.permission = permission; }
}
