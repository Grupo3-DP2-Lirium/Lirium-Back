# Cómo Crear un Administrador en Lirium

## Opción 1: Script SQL Directo (Recomendado)

### Paso 1: Ejecutar en tu herramienta de base de datos
Copia y pega el siguiente código en tu herramienta de BD (SQL Server Management Studio, Azure Data Studio, etc.):

```sql
-- Insertar usuario administrador
INSERT INTO users (
    id_user,
    first_name,
    first_last_name,
    email,
    password_hash,
    status,
    used_space,
    total_capacity,
    documentaries_purchased,
    documentaries_available,
    created_date,
    updated_date
) VALUES (
    NEWID(),
    'Admin',
    'Sistema',
    'admin@lirium.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ACTIVE',
    0.0,
    999999.0,
    999,
    999,
    GETDATE(),
    GETDATE()
);

-- Crear rol ADMIN si no existe
IF NOT EXISTS (SELECT 1 FROM role WHERE name = 'ADMIN')
INSERT INTO role (id_role, name) VALUES (NEWID(), 'ADMIN');

-- Asignar rol ADMIN al usuario
INSERT INTO user_roles (id_user, id_role)
SELECT u.id_user, r.id_role
FROM users u, role r
WHERE u.email = 'admin@lirium.com' 
AND r.name = 'ADMIN';
```

### Credenciales creadas:
- **Email:** admin@lirium.com
- **Password:** password

---

## Opción 2: Usando la API (Para desarrollo)

### Paso 1: Iniciar el backend
Asegúrate de que el backend esté corriendo en `http://localhost:8080`

### Paso 2: Crear administrador via API
Ejecuta este comando en tu terminal o usa Postman:

```bash
curl -X POST http://localhost:8080/api/admin-setup/create-admin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@lirium.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "Sistema"
  }'
```

### Paso 3: Verificar que se creó
```bash
curl -X GET http://localhost:8080/api/admin-setup/check-admin
```

---

## Opción 3: Script SQL Completo

Ejecuta el archivo `create_admin_user.sql` que incluye:
- Creación de permisos
- Creación de roles
- Asignación de permisos a roles
- Creación del usuario administrador
- Verificación final

---

## Verificar la Creación

Ejecuta esta consulta para verificar que el administrador se creó correctamente:

```sql
SELECT 
    u.id_user,
    u.first_name + ' ' + u.first_last_name as nombre_completo,
    u.email,
    u.status,
    r.name as rol
FROM users u
LEFT JOIN user_roles ur ON u.id_user = ur.id_user
LEFT JOIN role r ON ur.id_role = r.id_role
WHERE u.email = 'admin@lirium.com';
```

---

## Cambiar Contraseña (Recomendado)

### Via SQL:
```sql
UPDATE users 
SET password_hash = '$2a$10$[NUEVO_HASH_BCRYPT]'
WHERE email = 'admin@lirium.com';
```

### Via API:
```bash
curl -X POST http://localhost:8080/api/admin-setup/reset-admin-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@lirium.com",
    "newPassword": "nueva_contraseña_segura"
  }'
```

---

## Notas Importantes

1. **Seguridad:** Cambia la contraseña después del primer login
2. **Permisos:** El administrador tendrá acceso completo al panel de administración
3. **Capacidad:** Se asigna capacidad ilimitada (999999.0)
4. **Documentales:** Se asignan 999 documentales disponibles

---

## Solución de Problemas

### Error: "Ya existe un usuario con ese email"
- El administrador ya fue creado anteriormente
- Usa la opción de cambiar contraseña si necesitas acceso

### Error: "Tabla no encontrada"
- Verifica que las migraciones de la base de datos se hayan ejecutado
- Asegúrate de estar conectado a la base de datos correcta

### Error de autenticación en el panel web
- Verifica que el rol 'ADMIN' esté asignado correctamente
- Revisa que el hash de la contraseña sea válido