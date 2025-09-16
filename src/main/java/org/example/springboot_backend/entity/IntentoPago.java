package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.EstadoIntentoPago;

import java.time.LocalDateTime;

@Entity
public class IntentoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIntentoPago;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Plan plan;

    private Double monto;

    @Enumerated(EnumType.STRING)
    private EstadoIntentoPago estado = EstadoIntentoPago.CREADO;

    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // getters and setters
    public Long getIdIntentoPago() { return idIntentoPago; }
    public void setIdIntentoPago(Long idIntentoPago) { this.idIntentoPago = idIntentoPago; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public EstadoIntentoPago getEstado() { return estado; }
    public void setEstado(EstadoIntentoPago estado) { this.estado = estado; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
