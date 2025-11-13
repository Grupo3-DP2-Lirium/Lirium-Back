package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "extra_storage_plans")
public class ExtraStoragePlan {

    @Id
    @Column(name = "id_extra_plan", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID idExtraPlan;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // e.g. "EXTRA_10GB", "EXTRA_50GB", "EXTRA_100GB"

    @Column(name = "description")
    private String description; // e.g. "Espacio adicional de 10 GB"

    @Column(name = "additional_storage_gb", nullable = false)
    private Integer additionalStorageGb;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "paypal_plan_id")
    private String paypalPlanId;

    public ExtraStoragePlan() {
        this.idExtraPlan = UUID.randomUUID();
    }

    // Getters y setters
    public UUID getIdExtraPlan() { return idExtraPlan; }
    public void setIdExtraPlan(UUID idExtraPlan) { this.idExtraPlan = idExtraPlan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAdditionalStorageGb() { return additionalStorageGb; }
    public void setAdditionalStorageGb(Integer additionalStorageGb) { this.additionalStorageGb = additionalStorageGb; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getPaypalPlanId() { return paypalPlanId; }
    public void setPaypalPlanId(String paypalPlanId) { this.paypalPlanId = paypalPlanId; }
}
