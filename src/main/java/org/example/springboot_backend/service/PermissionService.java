package org.example.springboot_backend.service;

import org.example.springboot_backend.entity.Permission;
import org.example.springboot_backend.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    public Permission createPermission(String name, String description) {
        if (permissionRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Permission already exists: " + name);
        }
        
        Permission permission = new Permission(name, description);
        return permissionRepository.save(permission);
    }

    public Permission createPermissionIfNotExists(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> {
                    Permission newPermission = new Permission(name, description);
                    return permissionRepository.save(newPermission);
                });
    }

    public long countPermissions() {
        return permissionRepository.count();
    }
}