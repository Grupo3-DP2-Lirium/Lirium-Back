package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.Memory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    Page<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId, Pageable pageable);
}
