package org.example.springboot_backend.repository;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.entity.Memory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    List<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId);
    Page<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId, Pageable pageable);
    List<Memory> findByAuthorOrderByCreatedDateDesc(org.example.springboot_backend.entity.User author);
    List<Memory> findByMemorialIdMemorial(UUID idMemorial);

    @Query(value = """
        SELECT m.* FROM memories m 
        WHERE m.memorial_id_memorial = :memorialId 
          AND m.es_linea_tiempo = 1 
          AND m.visible = 1 
        ORDER BY 
          CASE WHEN m.photo_date IS NULL THEN 1 ELSE 0 END,
          m.photo_date ASC,
          m.created_date ASC
        """,
            countQuery = """
        SELECT COUNT(*) FROM memories m 
        WHERE m.memorial_id_memorial = :memorialId 
          AND m.es_linea_tiempo = 1 
          AND m.visible = 1
        """,
            nativeQuery = true)
    Page<Memory> findTimelineMemories(@Param("memorialId") UUID memorialId, Pageable pageable);
    
    // For reflections - find memories by memorial and type
    Page<Memory> findByMemorial_IdMemorialAndTypeOrderByCreatedDateDesc(
        UUID memorialId, 
        org.example.springboot_backend.enums.MemoryOriginType type, 
        Pageable pageable
    );

}
