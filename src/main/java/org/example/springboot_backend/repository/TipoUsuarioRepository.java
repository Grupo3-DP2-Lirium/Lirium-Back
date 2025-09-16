package org.example.springboot_backend.repository;

import org.example.springboot_backend.entity.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Long> {
}
