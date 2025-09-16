package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.EstadoVideo;

import java.time.LocalDateTime;

@Entity
public class VideoGenerado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReel;

    @ManyToOne(optional = false)
    private Memoria memoria;

    @ManyToOne(optional = false)
    private Usuario creador;

    @Column(columnDefinition = "TEXT")
    private String prompt;
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    private String videoURL;

    @Enumerated(EnumType.STRING)
    private EstadoVideo estado = EstadoVideo.PENDIENTE;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // getters and setters
    public Long getIdReel() { return idReel; }
    public void setIdReel(Long idReel) { this.idReel = idReel; }
    public Memoria getMemoria() { return memoria; }
    public void setMemoria(Memoria memoria) { this.memoria = memoria; }
    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getVideoURL() { return videoURL; }
    public void setVideoURL(String videoURL) { this.videoURL = videoURL; }
    public EstadoVideo getEstado() { return estado; }
    public void setEstado(EstadoVideo estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
