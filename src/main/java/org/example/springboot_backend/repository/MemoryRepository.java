package org.example.springboot_backend.repository;
import org.example.springboot_backend.entity.Memory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    Page<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId, Pageable pageable);
    List<Memory> findByMemorial_IdMemorialOrderByCreatedDateDesc(UUID memorialId);
    List<Memory> findByAuthorOrderByCreatedDateDesc(org.example.springboot_backend.entity.User author);

    // Nuevos métodos para filtrar por tipo de archivo
    @Query("SELECT m FROM Memory m JOIN m.files f WHERE m.memorial.idMemorial = :memorialId AND f.fileType = :fileType ORDER BY m.createdDate DESC")
    Page<Memory> findByMemorialAndFileType(@Param("memorialId") UUID memorialId, @Param("fileType") String fileType, Pageable pageable);
    
    @Query("SELECT COUNT(DISTINCT m) FROM Memory m JOIN m.files f WHERE m.memorial.idMemorial = :memorialId AND f.fileType = :fileType")
    Long countByMemorialAndFileType(@Param("memorialId") UUID memorialId, @Param("fileType") String fileType);

}
