# Sistema de Logs de Auditoría - HU50

## Descripción
Sistema completo de auditoría que registra todas las operaciones administrativas del sistema para garantizar trazabilidad y soporte a auditorías.

## Características

### Backend

#### 1. Entidad AuditLog
- **Ubicación**: `entity/AuditLog.java`
- **Campos**:
  - `idAuditLog`: ID único del log
  - `action`: Tipo de acción realizada (enum)
  - `userEmail`: Email del usuario que realizó la acción
  - `userId`: ID del usuario
  - `ipAddress`: Dirección IP desde donde se realizó la acción
  - `entityType`: Tipo de entidad afectada
  - `entityId`: ID de la entidad afectada
  - `details`: Detalles adicionales de la operación
  - `createdAt`: Fecha y hora de la operación
  - `success`: Indica si la operación fue exitosa
  - `errorMessage`: Mensaje de error si la operación falló

#### 2. Acciones Auditables (AuditAction enum)
- **Usuarios**: LOGIN, LOGOUT, REGISTER, UPDATE, DELETE, DISABLE, ENABLE
- **Memoriales**: CREATE, UPDATE, DELETE, SHARE
- **Memorias**: CREATE, UPDATE, DELETE, UPLOAD
- **Suscripciones**: CREATE, UPDATE, CANCEL, PAYMENT_SUCCESS, PAYMENT_FAILED
- **Admin**: USER_DISABLE, USER_ENABLE, VIEW_LOGS
- **Sistema**: ERROR, WARNING

#### 3. Servicio de Auditoría
- **Ubicación**: `service/AuditLogService.java`
- **Métodos principales**:
  - `log()`: Registra una acción de auditoría
  - `getLogs()`: Obtiene logs con filtros
  - `getAllLogs()`: Obtiene todos los logs paginados

**Características**:
- Captura automática del usuario autenticado
- Captura automática de la IP del cliente
- No interrumpe el flujo principal si falla el registro
- Soporte para filtros avanzados

#### 4. API Endpoints

**Base URL**: `/api/admin/audit-logs`

**GET /api/admin/audit-logs**
- Descripción: Obtiene logs con filtros opcionales
- Requiere: Rol ADMIN
- Parámetros query:
  - `action` (opcional): Filtrar por tipo de acción
  - `userEmail` (opcional): Filtrar por email de usuario
  - `startDate` (opcional): Fecha inicio (ISO 8601)
  - `endDate` (opcional): Fecha fin (ISO 8601)
  - `page` (default: 0): Número de página
  - `size` (default: 20): Tamaño de página

**Ejemplo de request**:
```
GET /api/admin/audit-logs?action=USER_LOGIN&userEmail=test@example.com&page=0&size=20
```

**Ejemplo de response**:
```json
{
  "content": [
    {
      "idAuditLog": "uuid",
      "action": "ADMIN_USER_DISABLE",
      "userEmail": "admin@lirium.com",
      "userId": "uuid",
      "ipAddress": "192.168.1.1",
      "entityType": "User",
      "entityId": "uuid",
      "details": "Usuario test@example.com fue deshabilitado por un administrador",
      "createdAt": "2024-01-15T10:30:00",
      "success": true,
      "errorMessage": null
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

**GET /api/admin/audit-logs/actions**
- Descripción: Obtiene todas las acciones disponibles
- Requiere: Rol ADMIN
- Response: Array de strings con los nombres de las acciones

### Frontend (Lirium-Admin-Web)

#### 1. Pantalla de Logs
- **Ubicación**: `app/audit-logs/`
- **Ruta**: `/audit-logs`

**Características**:
- Tabla con todos los logs del sistema
- Filtros avanzados:
  - Por tipo de acción
  - Por email de usuario
  - Por rango de fechas
- Paginación
- Badges de colores según el tipo de acción
- Indicador de éxito/error
- Formato de fecha localizado

#### 2. Navegación
- Agregado al sidebar con icono de documento
- Accesible desde el menú principal

## Uso

### Registrar una acción de auditoría

```java
@Autowired
private AuditLogService auditLogService;

// Registro simple
auditLogService.log(
    AuditAction.USER_UPDATE,
    "User",
    userId.toString(),
    "Usuario actualizado correctamente"
);

// Registro con estado de error
auditLogService.log(
    AuditAction.PAYMENT_FAILED,
    "Payment",
    paymentId.toString(),
    "Intento de pago fallido",
    false,
    "Tarjeta rechazada"
);
```

### Consultar logs desde el frontend

```typescript
import { auditService } from '@/services/audit.service';

// Obtener logs con filtros
const logs = await auditService.getLogs({
  action: AuditAction.USER_LOGIN,
  userEmail: 'test@example.com',
  startDate: '2024-01-01T00:00:00',
  endDate: '2024-12-31T23:59:59',
  page: 0,
  size: 20
});
```

## Base de Datos

### Crear la tabla
Ejecutar el script SQL ubicado en:
`src/main/resources/db/migration/audit_logs_table.sql`

### Índices
La tabla incluye índices optimizados para:
- Búsqueda por acción
- Búsqueda por email de usuario
- Ordenamiento por fecha
- Búsqueda por entidad

## Seguridad

- Todos los endpoints requieren autenticación
- Solo usuarios con rol ADMIN pueden acceder
- Los logs son inmutables (solo inserción, no actualización ni eliminación)
- Captura automática de IP para trazabilidad

## Mejoras Futuras

1. Exportación de logs a CSV/Excel
2. Alertas automáticas para acciones críticas
3. Dashboard con gráficos de actividad
4. Retención automática de logs (eliminar logs antiguos)
5. Integración con sistemas externos de SIEM
6. Logs de cambios detallados (antes/después)

## Testing

### Probar el registro de logs
1. Realizar una acción administrativa (ej: deshabilitar usuario)
2. Verificar que se creó el log en la base de datos
3. Consultar el log desde la API

### Probar la interfaz
1. Acceder a `/audit-logs` en Lirium-Admin-Web
2. Aplicar diferentes filtros
3. Verificar la paginación
4. Verificar que los colores de badges sean correctos
