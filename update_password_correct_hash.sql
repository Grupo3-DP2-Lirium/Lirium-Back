-- Script para actualizar la contraseña del usuario con el hash correcto
-- La contraseña será "rodrigo" con el hash generado por Python

UPDATE users 
SET password_hash = '$2b$12$muioXoVLY0rf7Gt4xUFttOGddXXTG3OLnBpGf1s3Me/M.8rJiuT.'
WHERE email = 'a20172844@pucp.edu.pe';

-- Verificar que el update funcionó:
SELECT email, password_hash FROM users WHERE email = 'a20172844@pucp.edu.pe';

-- Ahora puedes hacer login con:
-- Email: a20172844@pucp.edu.pe
-- Password: rodrigo