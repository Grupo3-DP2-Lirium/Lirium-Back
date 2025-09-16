package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.EstadoSuscripcion;
import org.example.springboot_backend.enums.MetodoPago;

import java.time.LocalDateTime;

@Entity
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSuscripcion;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    private EstadoSuscripcion estado = EstadoSuscripcion.NINGUNO;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPagoActual;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // getters and setters
    public Long getIdSuscripcion() { return idSuscripcion; }
    public void setIdSuscripcion(Long idSuscripcion) { this.idSuscripcion = idSuscripcion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
    public EstadoSuscripcion getEstado() { return estado; }
    public void setEstado(EstadoSuscripcion estado) { this.estado = estado; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public MetodoPago getMetodoPagoActual() { return metodoPagoActual; }
    public void setMetodoPagoActual(MetodoPago metodoPagoActual) { this.metodoPagoActual = metodoPagoActual; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
