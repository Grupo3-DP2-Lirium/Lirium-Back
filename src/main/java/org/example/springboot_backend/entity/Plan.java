package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @Column(name = "id_plan", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID idPlan;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "paypal_plan_id")
    private String paypalPlanId;

    @Column(name = "storage_limit_gb")
    private Integer storageLimitGb;

    @Column(name = "max_files")
    private Integer maxFiles;

    @Column(name = "max_collaborations")
    private Integer maxCollaborations;

    @Column(name = "max_documentaries_per_month")
    private Integer maxDocumentariesPerMonth;

    @Column(name = "support_level")
    private String supportLevel; // BASIC, STANDARD, PRIORITY

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "plan_permission",
        joinColumns = @JoinColumn(name = "plan_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    public Plan() {
        this.idPlan = UUID.randomUUID();
    }

    // Getters y setters
    public UUID getIdPlan() { return idPlan; }
    public void setIdPlan(UUID idPlan) { this.idPlan = idPlan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }

    public String getPaypalPlanId() { return paypalPlanId; }
    public void setPaypalPlanId(String paypalPlanId) { this.paypalPlanId = paypalPlanId; }

    public Integer getStorageLimitGb() { return storageLimitGb; }
    public void setStorageLimitGb(Integer storageLimitGb) { this.storageLimitGb = storageLimitGb; }

    public Integer getMaxFiles() { return maxFiles; }
    public void setMaxFiles(Integer maxFiles) { this.maxFiles = maxFiles; }

    public Integer getMaxCollaborations() { return maxCollaborations; }
    public void setMaxCollaborations(Integer maxCollaborations) { this.maxCollaborations = maxCollaborations; }

    public Integer getMaxDocumentariesPerMonth() { return maxDocumentariesPerMonth; }
    public void setMaxDocumentariesPerMonth(Integer maxDocumentariesPerMonth) { this.maxDocumentariesPerMonth = maxDocumentariesPerMonth; }

    public String getSupportLevel() { return supportLevel; }
    public void setSupportLevel(String supportLevel) { this.supportLevel = supportLevel; }

}
