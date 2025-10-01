-- Script para actualizar la contraseña del usuario existente
-- La contraseña será "rodrigo" encriptada con BCrypt

-- Paso 1: Actualizar la contraseña
UPDATE users 
SET password_hash = '$2a$10$EBlnpQrTkGJhf.SWl5K3zOIXOT4.xGKnfHFvzqDm0LYbPHpkUKo7y'
WHERE email = 'a20172844@pucp.edu.pe';

-- Paso 2: Asegurar que existe el rol USER
INSERT IGNORE INTO roles (id_role, name) VALUES 
(UUID(), 'USER');

-- Paso 3: Asignar rol USER al usuario (si no lo tiene)
INSERT IGNORE INTO user_roles (id_user, id_role)
SELECT u.id_user, r.id_role
FROM users u, roles r
WHERE u.email = 'a20172844@pucp.edu.pe' 
AND r.name = 'USER';

-- Verificar que todo esté correcto:
SELECT u.email, u.password_hash, r.name as role_name
FROM users u
LEFT JOIN user_roles ur ON u.id_user = ur.id_user
LEFT JOIN roles r ON ur.id_role = r.id_role
WHERE u.email = 'a20172844@pucp.edu.pe';

-- Ahora puedes hacer login con:
-- Email: a20172844@pucp.edu.pe
-- Password: rodrigo