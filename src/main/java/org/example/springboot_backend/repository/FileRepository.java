package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.File;
import org.example.springboot_backend.entity.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    
    // Buscar archivos por memoria
    List<File> findByMemory(Memory memory);
    
    // Buscar archivos por ID de memoria
    List<File> findByMemory_IdMemory(Long memoryId);
    
    // Buscar archivos por tipo
    List<File> findByFileType(String fileType);
    
    // Buscar archivos por tipo MIME
    List<File> findByMimeType(String mimeType);
    
    // Buscar archivos por proveedor de almacenamiento
    List<File> findByStorageProvider(String storageProvider);
    
    // Buscar archivos de una memoria por tipo
    List<File> findByMemory_IdMemoryAndFileType(Long memoryId, String fileType);
    
    // Calcular el tamaño total de archivos de una memoria
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.memory.idMemory = :memoryId")
    Double calculateTotalFileSizeByMemoryId(@Param("memoryId") Long memoryId);
    
    // Contar archivos por memoria
    Long countByMemory_IdMemory(Long memoryId);
    
    // Buscar archivos huérfanos (sin memoria asociada)
    List<File> findByMemoryIsNull();
    
    // Buscar archivos por rango de tamaño
    @Query("SELECT f FROM File f WHERE f.fileSize BETWEEN :minSize AND :maxSize")
    List<File> findByFileSizeBetween(@Param("minSize") Double minSize, @Param("maxSize") Double maxSize);
}