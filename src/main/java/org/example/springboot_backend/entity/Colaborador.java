package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idColaborador;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Memoria memoria;

    @Column(nullable = false)
    private String estadoInvitacion; // PENDIENTE, ACEPTADO, RECHAZADO, EXPIRADO (simple string for ahora)

    // getters and setters
    public Long getIdColaborador() { return idColaborador; }
    public void setIdColaborador(Long idColaborador) { this.idColaborador = idColaborador; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Memoria getMemoria() { return memoria; }
    public void setMemoria(Memoria memoria) { this.memoria = memoria; }
    public String getEstadoInvitacion() { return estadoInvitacion; }
    public void setEstadoInvitacion(String estadoInvitacion) { this.estadoInvitacion = estadoInvitacion; }
}
