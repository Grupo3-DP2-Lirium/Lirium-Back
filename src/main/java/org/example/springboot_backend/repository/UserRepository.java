package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.User;
import org.example.springboot_backend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIdUser(UUID idUser);

    /**
     * Búsqueda avanzada de usuarios con filtros múltiples
     */
    @Query("""
                SELECT DISTINCT u FROM User u
                LEFT JOIN FETCH u.roles r
                LEFT JOIN FETCH u.subscriptions s
                WHERE
                (:search IS NULL OR :search = '' OR
                 LOWER(CONCAT(u.firstName, ' ', u.firstLastName, COALESCE(CONCAT(' ', u.secondLastName), ''))) LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
                AND (:status IS NULL OR u.status = :status)
                AND (:role IS NULL OR :role = '' OR EXISTS (SELECT 1 FROM u.roles ur WHERE ur.name = :role))
                AND (:createdFrom IS NULL OR u.createdDate >= :createdFrom)
                AND (:createdTo IS NULL OR u.createdDate <= :createdTo)
                AND (:lastSessionFrom IS NULL OR u.lastSessionDate >= :lastSessionFrom)
                AND (:lastSessionTo IS NULL OR u.lastSessionDate <= :lastSessionTo)
                AND (:usedSpaceMin IS NULL OR u.usedSpace >= :usedSpaceMin)
                AND (:usedSpaceMax IS NULL OR u.usedSpace <= :usedSpaceMax)
            """)
    Page<User> findUsersWithFilters(
            @Param("search") String search,
            @Param("status") UserStatus status,
            @Param("role") String role,
            @Param("createdFrom") LocalDate createdFrom,
            @Param("createdTo") LocalDate createdTo,
            @Param("lastSessionFrom") LocalDateTime lastSessionFrom,
            @Param("lastSessionTo") LocalDateTime lastSessionTo,
            @Param("usedSpaceMin") Double usedSpaceMin,
            @Param("usedSpaceMax") Double usedSpaceMax,
            Pageable pageable);

    /**
     * Cuenta el número de memoriales creados por un usuario
     */
    @Query("SELECT COUNT(m) FROM Memorial m WHERE m.user.idUser = :userId")
    Long countMemorialsByUserId(@Param("userId") UUID userId);

    /**
     * Cuenta el número de memorias creadas por un usuario
     */
    @Query("SELECT COUNT(mem) FROM Memory mem WHERE mem.author.idUser = :userId")
    Long countMemoriesByUserId(@Param("userId") UUID userId);

    /**
     * Obtiene la fecha de la última actividad del usuario (última memoria creada)
     */
    @Query("SELECT MAX(mem.createdDate) FROM Memory mem WHERE mem.author.idUser = :userId")
    LocalDateTime findLastActivityByUserId(@Param("userId") UUID userId);

    /**
     * Cuenta el número total de archivos subidos por un usuario
     */
    @Query("SELECT COUNT(f) FROM File f JOIN f.memory mem WHERE mem.author.idUser = :userId")
    Long countFilesByUserId(@Param("userId") UUID userId);
}
