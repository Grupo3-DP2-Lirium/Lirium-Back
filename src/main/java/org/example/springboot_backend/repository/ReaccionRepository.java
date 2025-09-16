package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Reaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaccionRepository extends JpaRepository<Reaccion, Long> {
}
