package org.example.springboot_backend.repository;
import org.example.springboot_backend.entity.Memory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    List<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId);
    Page<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId, Pageable pageable);
    List<Memory> findByAuthorOrderByCreatedDateDesc(org.example.springboot_backend.entity.User author);

}
