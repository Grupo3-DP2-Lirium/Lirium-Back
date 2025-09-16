package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
}
