package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.TipoPlan;

@Entity
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlan;

    @Enumerated(EnumType.STRING)
    private TipoPlan tipoPlan; // FREE, PREMIUM, MENSUAL, PREMIUM_ANUAL

    private String descripcion;
    private Double precio;
    private String moneda;
    private Boolean activo = true;

    // getters and setters
    public Long getIdPlan() { return idPlan; }
    public void setIdPlan(Long idPlan) { this.idPlan = idPlan; }
    public TipoPlan getTipoPlan() { return tipoPlan; }
    public void setTipoPlan(TipoPlan tipoPlan) { this.tipoPlan = tipoPlan; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
