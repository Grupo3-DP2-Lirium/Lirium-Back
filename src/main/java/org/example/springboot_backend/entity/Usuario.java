package org.example.springboot_backend.entity;

import jakarta.persistence.*;
import org.example.springboot_backend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String nombre;
    private String email;

    @ManyToOne(optional = false)
    private TipoUsuario tipo;

    private String passWordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDate fechaCreado;
    private LocalDate fechaActualizado;
    private LocalDateTime fechaUltimaSesion;

    // getters and setters
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }
    public String getPassWordHash() { return passWordHash; }
    public void setPassWordHash(String passWordHash) { this.passWordHash = passWordHash; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public LocalDate getFechaCreado() { return fechaCreado; }
    public void setFechaCreado(LocalDate fechaCreado) { this.fechaCreado = fechaCreado; }
    public LocalDate getFechaActualizado() { return fechaActualizado; }
    public void setFechaActualizado(LocalDate fechaActualizado) { this.fechaActualizado = fechaActualizado; }
    public LocalDateTime getFechaUltimaSesion() { return fechaUltimaSesion; }
    public void setFechaUltimaSesion(LocalDateTime fechaUltimaSesion) { this.fechaUltimaSesion = fechaUltimaSesion; }
}
