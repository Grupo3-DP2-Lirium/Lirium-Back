package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvento;

    @ManyToOne(optional = false)
    private Memorial memorial;

    private String titulo;
    private String descripcion;
    private LocalDateTime fecha;

    // getters and setters
    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }
    public Memorial getMemorial() { return memorial; }
    public void setMemorial(Memorial memorial) { this.memorial = memorial; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
