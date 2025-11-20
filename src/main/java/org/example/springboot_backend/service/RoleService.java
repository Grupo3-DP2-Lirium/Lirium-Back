package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.Role;
import org.example.springboot_backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Role createRole(String name) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Role already exists: " + name);
        }
        
        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    public Role createRoleIfNotExists(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(name);
                    return roleRepository.save(newRole);
                });
    }

    public long countRoles() {
        return roleRepository.count();
    }

    public Iterable<Role> findAll() {
        return roleRepository.findAll();
    }

}