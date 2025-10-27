# Módulo de Enlaces Compartibles de Memoriales

## Descripción General
Este módulo permite crear y gestionar enlaces públicos compartibles para memoriales. Cualquier usuario puede acceder a un memorial compartido sin necesidad de autenticación, usando un enlace único generado por el sistema.

## Arquitectura del Módulo

### Componentes Creados

1. **Entidad: `MemorialShare`**
   - Ubicación: `src/main/java/org/example/springboot_backend/entity/MemorialShare.java`
   - Tabla: `memorial_shares`
   - Campos:
     - `id` (UUID): Identificador único del share
     - `memorial_id` (UUID): Relación con Memorial
     - `slug` (String): Código único de 10 caracteres alfanuméricos
     - `createdAt` (LocalDateTime): Fecha de creación
   - Características:
     - Genera automáticamente el slug usando `SecureRandom`
     - Índice único en el campo `slug`
     - Relación @ManyToOne con Memorial

2. **Repositorio: `MemorialShareRepository`**
   - Ubicación: `src/main/java/org/example/springboot_backend/repository/MemorialShareRepository.java`
   - Métodos:
     - `findBySlug(String slug)`: Busca un share por su slug único

3. **Servicio: `MemorialShareService`**
   - Ubicación: `src/main/java/org/example/springboot_backend/service/MemorialShareService.java`
   - Métodos:
     - `createShareLink(UUID memorialId)`: Crea un nuevo enlace compartible
     - `getPublicMemorialBySlug(String slug)`: Obtiene información pública del memorial

4. **DTOs:**
   - `PublicMemorialDto`: Información pública del memorial
     - Ubicación: `src/main/java/org/example/springboot_backend/dto/PublicMemorialDto.java`
     - Campos: idMemorial, name, nickname, description, coverURL, birthDate, gender
   
   - `ShareLinkResponse`: Respuesta al crear un enlace
     - Ubicación: `src/main/java/org/example/springboot_backend/dto/ShareLinkResponse.java`
     - Campos: url, slug

5. **Controlador: `MemorialShareController`**
   - Ubicación: `src/main/java/org/example/springboot_backend/controller/MemorialShareController.java`
   - Endpoints documentados con Swagger

## API Endpoints

### 1. Crear Enlace Compartible
```http
POST /api/memorials/{id}/share
```

**Descripción**: Genera un enlace público único para compartir un memorial.

**Autenticación**: Requerida (JWT)

**Parámetros**:
- `id` (path): UUID del memorial a compartir

**Respuesta Exitosa (201 Created)**:
```json
{
  "url": "https://jhairt.com/m/aB3xY9kL2m",
  "slug": "aB3xY9kL2m"
}
```

**Respuesta Error (404 Not Found)**:
- Memorial no encontrado

**Ejemplo con cURL**:
```bash
curl -X POST "http://localhost:8080/api/memorials/550e8400-e29b-41d4-a716-446655440000/share" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Ver Memorial Compartido
```http
GET /api/public/m/{slug}
```

**Descripción**: Obtiene la información pública de un memorial compartido.

**Autenticación**: No requerida (acceso público)

**Parámetros**:
- `slug` (path): Código único del enlace compartido (ej: "aB3xY9kL2m")

**Respuesta Exitosa (200 OK)**:
```json
{
  "idMemorial": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Juan Pérez",
  "nickname": "Juanito",
  "description": "Una persona maravillosa que siempre estará en nuestros corazones",
  "coverURL": "https://example.com/cover.jpg",
  "birthDate": "1950-05-15",
  "gender": "Masculino"
}
```

**Respuesta Error (404 Not Found)**:
- Enlace compartido no encontrado

**Ejemplo con cURL**:
```bash
curl -X GET "http://localhost:8080/api/public/m/aB3xY9kL2m"
```

**Ejemplo desde navegador**:
```
http://localhost:8080/api/public/m/aB3xY9kL2m
```

---

## Configuración

### application.properties
```properties
# URL base para enlaces compartidos (personalizable por entorno)
app.share.base-url=https://jhairt.com
```

Puedes sobrescribir esta configuración en diferentes entornos:
- `application-dev.properties`: `app.share.base-url=http://localhost:3000`
- `application-prod.properties`: `app.share.base-url=https://jhairt.com`

### Seguridad

La configuración de Spring Security ha sido actualizada en `SecurityConfig.java` para permitir:
- `/api/public/**` → Acceso público sin autenticación
- `/api/memorials/{id}/share` → Requiere autenticación (protegido)

## Flujo de Uso

### Caso de Uso: Compartir un Memorial

1. **Usuario autenticado crea un enlace**:
   ```
   POST /api/memorials/550e8400-e29b-41d4-a716-446655440000/share
   ```
   
2. **Sistema genera el slug y devuelve URL**:
   ```json
   {
     "url": "https://jhairt.com/m/aB3xY9kL2m",
     "slug": "aB3xY9kL2m"
   }
   ```

3. **Usuario comparte el enlace por email, redes sociales, etc.**

4. **Cualquier persona accede sin autenticación**:
   ```
   GET /api/public/m/aB3xY9kL2m
   ```

5. **Sistema devuelve información pública del memorial**

## Modelo de Datos

### Diagrama de Relaciones
```
Memorial (1) ──── (N) MemorialShare
   │
   └─ idMemorial (PK)
   
MemorialShare
   ├─ id (PK, UUID)
   ├─ memorial_id (FK → Memorial)
   ├─ slug (UNIQUE, 10 chars)
   └─ createdAt
```

### Script SQL Generado (automático con JPA)
```sql
CREATE TABLE memorial_shares (
    id UUID PRIMARY KEY,
    memorial_id UUID NOT NULL,
    slug VARCHAR(10) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (memorial_id) REFERENCES memorial(id_memorial)
);

CREATE UNIQUE INDEX idx_slug ON memorial_shares(slug);
```

## Pruebas Recomendadas

### 1. Crear un Share
```bash
# Paso 1: Login para obtener token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password"}'

# Paso 2: Crear share (reemplaza {MEMORIAL_ID} y {TOKEN})
curl -X POST "http://localhost:8080/api/memorials/{MEMORIAL_ID}/share" \
  -H "Authorization: Bearer {TOKEN}"
```

### 2. Acceder al Memorial Público
```bash
# No requiere autenticación
curl -X GET "http://localhost:8080/api/public/m/{SLUG}"
```

### 3. Probar en Swagger UI
1. Accede a: `http://localhost:8080/swagger-ui.html`
2. Busca la sección **Memorial Share**
3. Prueba los endpoints directamente desde la interfaz

## Características Técnicas

### Generación de Slugs
- **Algoritmo**: `SecureRandom` con alfabeto alfanumérico
- **Longitud**: 10 caracteres
- **Caracteres**: A-Z, a-z, 0-9
- **Unicidad**: Garantizada por índice UNIQUE en base de datos
- **Probabilidad de colisión**: ~1 en 62^10 (≈ 839 cuatrillones)

### Transaccionalidad
- `createShareLink()`: `@Transactional` (escritura)
- `getPublicMemorialBySlug()`: `@Transactional(readOnly = true)` (optimización)

### Manejo de Errores
- Memorial no encontrado → 404 Not Found
- Slug no encontrado → 404 Not Found
- Errores internos se propagan como `RuntimeException`

## Próximas Mejoras (Fase Posterior)

Las siguientes características NO están implementadas en esta versión básica:

1. **Expiración de enlaces**: 
   - Agregar campo `expiresAt` a `MemorialShare`
   - Validar expiración en `getPublicMemorialBySlug()`

2. **Control de privacidad**:
   - Campo `isActive` para activar/desactivar shares
   - Validación de permisos del usuario propietario

3. **Métricas y analytics**:
   - Contador de visitas
   - Registro de accesos (IP, fecha, etc.)

4. **Enlaces con contraseña**:
   - Campo `password` opcional
   - Validación antes de mostrar memorial

5. **Múltiples shares por memorial**:
   - Gestionar varios enlaces activos
   - Endpoint para listar todos los shares de un memorial

6. **Notificaciones**:
   - Avisar al propietario cuando alguien accede
   - Email al crear un nuevo share

## Dependencias Añadidas

No se requirieron dependencias adicionales. El módulo usa:
- Spring Data JPA (ya existente)
- Spring Security (ya existente)
- Spring Web (ya existente)
- Java Security (`SecureRandom` - parte de JDK)

## Testing

### Unit Tests (Pendiente)
```java
// MemorialShareServiceTest.java
// MemorialShareControllerTest.java
```

### Integration Tests (Pendiente)
```java
// MemorialShareIntegrationTest.java
```

## Autor
Módulo creado para Lirium-Back - Sistema de gestión de memoriales

---

**Versión**: 1.0.0  
**Fecha**: Octubre 2025  
**Estado**: ✅ Implementado y funcional
