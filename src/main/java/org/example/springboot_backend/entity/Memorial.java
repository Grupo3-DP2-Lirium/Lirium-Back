package org.example.springboot_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Memorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMemorial;

    @ManyToOne(optional = false)
    private Usuario usuario; // propietario/creador del memorial

    private String nombre; // nombre de la persona
    private String apodo;
    private LocalDate fechaNacimiento;
    private String genero;
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    private String tipoRelacion; // opcion libre
    private String portadaURL;
    private boolean esColaborativo;
    private boolean esJournal;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // getters and setters
    public Long getIdMemorial() { return idMemorial; }
    public void setIdMemorial(Long idMemorial) { this.idMemorial = idMemorial; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApodo() { return apodo; }
    public void setApodo(String apodo) { this.apodo = apodo; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipoRelacion() { return tipoRelacion; }
    public void setTipoRelacion(String tipoRelacion) { this.tipoRelacion = tipoRelacion; }
    public String getPortadaURL() { return portadaURL; }
    public void setPortadaURL(String portadaURL) { this.portadaURL = portadaURL; }
    public boolean isEsColaborativo() { return esColaborativo; }
    public void setEsColaborativo(boolean esColaborativo) { this.esColaborativo = esColaborativo; }
    public boolean isEsJournal() { return esJournal; }
    public void setEsJournal(boolean esJournal) { this.esJournal = esJournal; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
