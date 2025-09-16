package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Respuesta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {
}
