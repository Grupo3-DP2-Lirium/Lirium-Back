package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryRepository extends JpaRepository<Memory, Long> {
}
