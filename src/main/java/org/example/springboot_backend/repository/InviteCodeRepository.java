package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.InviteCode;
import org.example.springboot_backend.entity.Memorial;
import org.example.springboot_backend.enums.InviteCodeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, UUID> {
    Optional<InviteCode> findByCode(String code);
    List<InviteCode> findByMemorial(Memorial memorial);
    List<InviteCode> findByMemorialAndStatus(Memorial memorial, InviteCodeStatusEnum status);
    boolean existsByCode(String code);
}