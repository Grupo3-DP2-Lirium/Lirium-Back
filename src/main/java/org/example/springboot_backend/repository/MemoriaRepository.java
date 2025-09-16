package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Memoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoriaRepository extends JpaRepository<Memoria, Long> {
}
