package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.ReactionType;

import java.time.LocalDateTime;

@Entity
public class Reaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReaccion;

    @ManyToOne(optional = false)
    private Memoria memoria;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private ReactionType tipo;

    private LocalDateTime fechaCreacion;

    // getters and setters
    public Long getIdReaccion() { return idReaccion; }
    public void setIdReaccion(Long idReaccion) { this.idReaccion = idReaccion; }
    public Memoria getMemoria() { return memoria; }
    public void setMemoria(Memoria memoria) { this.memoria = memoria; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public ReactionType getTipo() { return tipo; }
    public void setTipo(ReactionType tipo) { this.tipo = tipo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
