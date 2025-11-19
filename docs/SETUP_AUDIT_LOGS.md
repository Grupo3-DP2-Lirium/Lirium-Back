# Configuración y Prueba del Sistema de Auditoría

## Pasos para configurar

### 1. Crear la tabla en la base de datos

Ejecuta el script SQL ubicado en:
```
Lirium-Back/src/main/resources/db/migration/audit_logs_table.sql
```

**Opción A: Desde Azure Data Studio o SQL Server Management Studio**
1. Conecta a tu base de datos
2. Abre el archivo `audit_logs_table.sql`
3. Ejecuta el script completo
4. Verifica que la tabla se creó: `SELECT * FROM audit_logs`

**Opción B: Desde la consola de SQL**
```sql
-- Copiar y pegar el contenido del archivo audit_logs_table.sql
```

### 2. Reiniciar el backend

Después de crear la tabla, reinicia el servidor Spring Boot para que cargue las nuevas clases.

### 3. Probar que funciona

#### Opción 1: Endpoint de prueba
Usa Postman o curl para crear un log de prueba:

```bash
POST http://localhost:8080/api/admin/audit-logs/test
Authorization: Bearer YOUR_TOKEN
```

Respuesta esperada:
```
Log de prueba creado exitosamente
```

#### Opción 2: Hacer login
1. Haz login desde la app móvil o web
2. Esto debería crear un log automáticamente
3. Verifica en la base de datos:
```sql
SELECT * FROM audit_logs ORDER BY created_at DESC
```

#### Opción 3: Deshabilitar un usuario
1. Ve a la página de usuarios en el admin web
2. Deshabilita un usuario
3. Verifica que se creó el log

### 4. Ver los logs en el admin web

1. Accede a: `http://localhost:3000/audit-logs`
2. Deberías ver los logs registrados
3. Prueba los filtros

## Verificar que todo funciona

### Checklist

- [ ] La tabla `audit_logs` existe en la base de datos
- [ ] El backend se inició sin errores
- [ ] No hay errores en la consola del backend relacionados con AuditLog
- [ ] El endpoint de prueba funciona
- [ ] Los logs se crean al hacer login
- [ ] Los logs se crean al deshabilitar usuarios
- [ ] La página de audit-logs muestra los datos
- [ ] Los filtros funcionan correctamente

## Troubleshooting

### No se crean logs

**Problema**: Los logs no aparecen en la base de datos

**Soluciones**:
1. Verifica que la tabla existe:
```sql
SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'audit_logs'
```

2. Verifica los logs del backend en la consola. Busca errores como:
```
Error al guardar audit log: ...
```

3. Verifica que el `AuditLogService` se está inyectando correctamente:
   - Busca en los logs del backend al iniciar: `Creating bean 'auditLogService'`

4. Prueba insertar un log manualmente en la BD:
```sql
INSERT INTO audit_logs (action, user_email, details, success)
VALUES ('USER_LOGIN', 'test@test.com', 'Test manual', 1)
```

### La página no muestra datos

**Problema**: La página de audit-logs está vacía

**Soluciones**:
1. Abre la consola del navegador (F12) y busca errores
2. Verifica que el backend esté corriendo
3. Verifica la URL del API en `.env.local`:
```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

4. Prueba el endpoint directamente:
```bash
GET http://localhost:8080/api/admin/audit-logs?page=0&size=20
Authorization: Bearer YOUR_TOKEN
```

### Error 403 Forbidden

**Problema**: No puedes acceder a los endpoints de audit-logs

**Solución**: Verifica que tu usuario tenga rol ADMIN:
```sql
SELECT u.email, r.name as role
FROM users u
JOIN user_roles ur ON u.id_user = ur.user_id
JOIN roles r ON ur.role_id = r.id_role
WHERE u.email = 'tu_email@example.com'
```

Si no tiene rol ADMIN, agrégalo:
```sql
-- Primero obtén el ID del rol ADMIN
SELECT id_role FROM roles WHERE name = 'ADMIN'

-- Luego asigna el rol al usuario
INSERT INTO user_roles (user_id, role_id)
VALUES ('TU_USER_ID', 'ADMIN_ROLE_ID')
```

## Logs que se registran automáticamente

Actualmente se registran automáticamente:

1. **USER_LOGIN** - Cuando un usuario hace login (exitoso o fallido)
2. **USER_REGISTER** - Cuando se registra un nuevo usuario
3. **USER_LOGOUT** - Cuando un usuario cierra sesión
4. **ADMIN_USER_DISABLE** - Cuando un admin deshabilita un usuario
5. **ADMIN_USER_ENABLE** - Cuando un admin habilita un usuario
6. **ADMIN_VIEW_LOGS** - Cuando un admin consulta los logs

## Agregar más logs

Para agregar logging a otros controladores, inyecta el servicio:

```java
@RestController
public class MiController {
    
    private final AuditLogService auditLogService;
    
    public MiController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    @PostMapping("/mi-endpoint")
    public ResponseEntity<?> miMetodo() {
        // Tu lógica aquí
        
        // Registrar en auditoría
        auditLogService.log(
            AuditAction.MEMORIAL_CREATE,
            "Memorial",
            memorial.getId().toString(),
            "Memorial creado: " + memorial.getName()
        );
        
        return ResponseEntity.ok(resultado);
    }
}
```

## Datos de ejemplo

El script SQL incluye 5 logs de ejemplo para testing. Si quieres más datos de prueba, ejecuta:

```sql
INSERT INTO audit_logs (action, user_email, ip_address, entity_type, details, success)
VALUES 
    ('MEMORIAL_CREATE', 'user@lirium.com', '192.168.1.10', 'Memorial', 'Memorial creado: Abuela María', 1),
    ('MEMORY_UPLOAD', 'user@lirium.com', '192.168.1.10', 'Memory', 'Foto subida al memorial', 1),
    ('SUBSCRIPTION_CREATE', 'user@lirium.com', '192.168.1.10', 'Subscription', 'Suscripción Premium activada', 1),
    ('PAYMENT_SUCCESS', 'user@lirium.com', '192.168.1.10', 'Payment', 'Pago procesado exitosamente: $29.99', 1),
    ('SYSTEM_ERROR', NULL, NULL, 'System', 'Error al procesar video', 0);
```
