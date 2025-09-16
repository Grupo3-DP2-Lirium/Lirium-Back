package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
}
