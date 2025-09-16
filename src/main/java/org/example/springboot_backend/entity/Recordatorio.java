package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Recordatorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRecordatorio;

    @ManyToOne(optional = false)
    private Usuario usuario;

    private String titulo;
    private String descripcion;
    private LocalDateTime fechaAviso;
    private boolean activo = true;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // getters and setters
    public Long getIdRecordatorio() { return idRecordatorio; }
    public void setIdRecordatorio(Long idRecordatorio) { this.idRecordatorio = idRecordatorio; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaAviso() { return fechaAviso; }
    public void setFechaAviso(LocalDateTime fechaAviso) { this.fechaAviso = fechaAviso; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
