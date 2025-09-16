package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class TipoUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipo;

    @Column(nullable = false, unique = true)
    private String nombre; // FREE, PREMIUM, ADMIN

    // getters and setters
    public Long getIdTipo() { return idTipo; }
    public void setIdTipo(Long idTipo) { this.idTipo = idTipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
