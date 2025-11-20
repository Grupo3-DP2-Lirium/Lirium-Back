# Sistema de Logs de Auditoría - Lirium

## Descripción

Sistema completo de auditoría que registra todas las operaciones administrativas realizadas en el panel de administración web. Cumple con el requisito HU50 de mantener una bitácora de todas las operaciones administrativas.

## Características

- ✅ Registro automático de todas las acciones administrativas
- ✅ Captura de información del administrador (ID, email)
- ✅ Registro de dirección IP del origen de la acción
- ✅ Almacenamiento de detalles adicionales en formato JSON
- ✅ Interfaz web para visualizar y filtrar logs
- ✅ Paginación y búsqueda avanzada
- ✅ Soporte para auditorías y trazabilidad completa

## Instalación

### 1. Base de Datos

Ejecuta el script SQL para crear la tabla de auditoría:

```bash
# Conectarse a la base de datos SQL Server
sqlcmd -S <servidor> -d <base_datos> -U <usuario> -P <password> -i create_audit_logs_table.sql
```

O ejecuta manualmente el contenido del archivo `create_audit_logs_table.sql` en tu cliente SQL Server.

### 2. Backend (Spring Boot)

Los archivos ya están creados en el proyecto:

**Nuevos archivos:**
- `model/AuditLog.java` - Entidad JPA
- `repository/AuditLogRepository.java` - Repositorio con queries personalizadas
- `dto/AuditLogResponse.java` - DTO para respuestas
- `service/AuditLogService.java` - Lógica de negocio
- `controller/AuditLogController.java` - Endpoints REST

**Archivos modificados:**
- `controller/AdminUserController.java` - Agregado registro de logs en operaciones de usuarios

### 3. Frontend (Next.js)

Los archivos ya están creados en el proyecto:

**Nuevos archivos:**
- `types/audit.types.ts` - Tipos TypeScript
- `services/audit.service.ts` - Servicio para consumir API
- `app/audit-logs/AuditLogsManagement.tsx` - Componente principal
- `app/audit-logs/page.tsx` - Página Next.js

**Archivos modificados:**
- `components/Sidebar/Sidebar.tsx` - Agregado icono de logs
- `services/index.ts` - Exportación del servicio
- `types/index.ts` - Exportación de tipos

## Uso

### Backend - Registrar un Log

```java
@Autowired
private AuditLogService auditLogService;

// Registro simple
auditLogService.logUserAction(
    "ENABLE_USER",
    userId.toString(),
    "Usuario habilitado: " + user.getEmail()
);

// Registro con detalles adicionales
auditLogService.logUserAction(
    "DISABLE_USER",
    userId.toString(),
    "Usuario deshabilitado: " + user.getEmail(),
    Map.of(
        "previousStatus", "ACTIVE",
        "newStatus", "SUSPENDED",
        "userEmail", user.getEmail()
    )
);
```

### API Endpoints

#### Obtener todos los logs (paginado)
```
GET /api/admin/audit-logs?page=0&size=20
```

#### Filtrar logs
```
GET /api/admin/audit-logs/filter?adminId=admin@lirium.com&action=ENABLE_USER&page=0&size=20
```

**Parámetros de filtro:**
- `adminId` - Email del administrador
- `action` - Tipo de acción (ENABLE_USER, DISABLE_USER, etc.)
- `entityType` - Tipo de entidad (USER, MEMORY, MEMORIAL)
- `startDate` - Fecha inicio (ISO 8601)
- `endDate` - Fecha fin (ISO 8601)
- `page` - Número de página (default: 0)
- `size` - Tamaño de página (default: 20)

### Frontend - Acceso

1. Inicia sesión como administrador
2. En el menú lateral, haz clic en "Logs de Auditoría"
3. Usa los filtros para buscar logs específicos
4. Haz clic en "Ver" para ver detalles completos de un log

## Tipos de Acciones Registradas

Actualmente se registran las siguientes acciones:

- `ENABLE_USER` - Habilitar usuario
- `DISABLE_USER` - Deshabilitar usuario
- `UPDATE_USER` - Actualizar información de usuario
- `DELETE_USER` - Eliminar usuario
- `CREATE_USER` - Crear nuevo usuario
- `VIEW_USER` - Ver detalles de usuario

## Estructura de la Tabla

```sql
audit_logs (
    id BIGINT PRIMARY KEY,
    admin_id NVARCHAR(255),
    admin_email NVARCHAR(255),
    action NVARCHAR(100),
    entity_type NVARCHAR(50),
    entity_id NVARCHAR(255),
    description NVARCHAR(500),
    ip_address NVARCHAR(45),
    timestamp DATETIME2,
    details NVARCHAR(MAX)
)
```

## Seguridad

- ✅ Solo usuarios con rol ADMIN pueden acceder a los logs
- ✅ Los logs son de solo lectura (no se pueden modificar ni eliminar)
- ✅ Se registra la IP de origen de cada acción
- ✅ Timestamps automáticos con zona horaria

## Próximas Mejoras

- [ ] Exportar logs a CSV/Excel
- [ ] Alertas automáticas para acciones críticas
- [ ] Dashboard con estadísticas de auditoría
- [ ] Retención automática de logs (archivar logs antiguos)
- [ ] Integración con sistemas de monitoreo externos

## Soporte

Para cualquier duda o problema, contacta al equipo de desarrollo.
