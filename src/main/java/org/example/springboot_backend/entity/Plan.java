package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.PlanType;
import java.util.UUID;

@Entity
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false)
    private Boolean active = true;

    // getters and setters
    public UUID getIdPlan() { return idPlan; }
    public void setIdPlan(UUID idPlan) { this.idPlan = idPlan; }
    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
