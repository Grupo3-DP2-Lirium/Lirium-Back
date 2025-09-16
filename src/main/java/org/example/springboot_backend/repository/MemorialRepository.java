package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Memorial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemorialRepository extends JpaRepository<Memorial, Long> {
}
