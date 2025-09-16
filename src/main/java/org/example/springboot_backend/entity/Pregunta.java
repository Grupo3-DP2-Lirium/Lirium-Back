package org.example.springboot_backend.entity;

import jakarta.persistence.*;

@Entity
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPregunta;

    @Column(nullable = false, length = 500)
    private String texto;

    // Si es una pregunta predefinida, tiene categoría; si es propia, la categoría es null y esPropia=true
    @ManyToOne
    private CategoriaPregunta categoria;

    @Column(nullable = false)
    private boolean esPropia = false;

    // autor opcional para propias
    @ManyToOne
    private Usuario autor;

    // getters and setters
    public Long getIdPregunta() { return idPregunta; }
    public void setIdPregunta(Long idPregunta) { this.idPregunta = idPregunta; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public CategoriaPregunta getCategoria() { return categoria; }
    public void setCategoria(CategoriaPregunta categoria) { this.categoria = categoria; }
    public boolean isEsPropia() { return esPropia; }
    public void setEsPropia(boolean esPropia) { this.esPropia = esPropia; }
    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }
}
