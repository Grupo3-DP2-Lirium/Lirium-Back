package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComentario;

    @ManyToOne(optional = false)
    private Memoria memoria;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String cuerpo;

    private LocalDateTime fechaCreacion;

    // getters and setters
    public Long getIdComentario() { return idComentario; }
    public void setIdComentario(Long idComentario) { this.idComentario = idComentario; }
    public Memoria getMemoria() { return memoria; }
    public void setMemoria(Memoria memoria) { this.memoria = memoria; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
