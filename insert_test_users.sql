-- Script SQL para insertar usuarios de prueba en tu base de datos
-- Ejecuta este script en tu base de datos para tener usuarios con los que probar

-- Insertar roles básicos si no existen
INSERT INTO roles (id_role, name) VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'USER'),
('550e8400-e29b-41d4-a716-446655440001', 'ADMIN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insertar usuarios de prueba
-- Contraseña: "password123" (deberás encriptarla con BCrypt)
INSERT INTO users (id_user, first_name, first_last_name, second_last_name, email, password, phone, date_birth, status, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Juan', 'Pérez', 'García', 'juan@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+123456789', '1990-01-01', 'ACTIVE', NOW()),
('550e8400-e29b-41d4-a716-446655440011', 'María', 'González', 'López', 'maria@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+987654321', '1995-05-15', 'ACTIVE', NOW()),
('550e8400-e29b-41d4-a716-446655440012', 'Test', 'User', 'Demo', 'test@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+555123456', '1985-12-25', 'ACTIVE', NOW());

-- Asignar rol USER a los usuarios
INSERT INTO user_roles (id_user, id_role) VALUES
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440000');

-- Nota: La contraseña encriptada corresponde a "password123"
-- Usuarios de prueba creados:
-- 1. Email: juan@example.com, Password: password123
-- 2. Email: maria@example.com, Password: password123  
-- 3. Email: test@example.com, Password: password123