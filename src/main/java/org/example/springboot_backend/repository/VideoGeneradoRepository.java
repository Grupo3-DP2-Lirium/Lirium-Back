package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.VideoGenerado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoGeneradoRepository extends JpaRepository<VideoGenerado, Long> {
}
