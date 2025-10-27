# Ejemplo de Respuesta del Memorial Compartido

## Respuesta ANTES de los cambios

### GET /api/public/m/{slug}
```json
{
  "idMemorial": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Juan Pérez",
  "nickname": "Juanito",
  "description": "Una persona maravillosa",
  "coverURL": "https://example.com/cover.jpg",
  "birthDate": "1950-05-15",
  "gender": "Masculino"
}
```

❌ **Problema**: No incluía las memorias asociadas

---

## Respuesta DESPUÉS de los cambios ✅

### GET /api/public/m/{slug}
```json
{
  "idMemorial": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Juan Pérez",
  "nickname": "Juanito",
  "description": "Una persona maravillosa que siempre estará en nuestros corazones",
  "coverURL": "https://example.com/cover.jpg",
  "birthDate": "1950-05-15",
  "gender": "Masculino",
  "memories": [
    {
      "idMemory": "123e4567-e89b-12d3-a456-426614174000",
      "title": "Vacaciones en la playa",
      "description": "Un día inolvidable junto al mar",
      "photoDate": "2020-07-15",
      "location": "Cancún, México",
      "createdDate": "2024-01-10T14:30:00"
    },
    {
      "idMemory": "223e4567-e89b-12d3-a456-426614174001",
      "title": "Cumpleaños número 70",
      "description": "Celebración familiar",
      "photoDate": "2020-05-15",
      "location": "Casa familiar",
      "createdDate": "2024-01-05T10:15:00"
    }
  ]
}
```

✅ **Ahora incluye**:
- Lista completa de memorias asociadas al memorial
- Solo memorias con `visible = true`
- Ordenadas por fecha de creación (más recientes primero)

---

## Configuración de URL Base

### application.properties (Producción)
```properties
app.share.base-url=https://jhairt.com
```

### application-dev.properties (Desarrollo)
```properties
app.share.base-url=http://localhost:8080
```

### Ejemplo de uso:

**En desarrollo:**
```bash
POST /api/memorials/{id}/share

Response:
{
  "url": "http://localhost:8080/m/aB3xY9kL2m",
  "slug": "aB3xY9kL2m"
}
```

**En producción:**
```bash
POST /api/memorials/{id}/share

Response:
{
  "url": "https://jhairt.com/m/aB3xY9kL2m",
  "slug": "aB3xY9kL2m"
}
```

---

## Resumen de Cambios Implementados

✅ **1. Nuevo DTO: `PublicMemoryDto`**
   - Incluye: idMemory, title, description, photoDate, location, createdDate
   - Ubicación: `src/main/java/org/example/springboot_backend/dto/PublicMemoryDto.java`

✅ **2. Actualizado: `PublicMemorialDto`**
   - Agregado campo: `List<PublicMemoryDto> memories`
   - Ahora incluye las memorias asociadas

✅ **3. Actualizado: `MemorialShareService`**
   - Inyecta `MemoryRepository` para obtener las memorias
   - Método `getPublicMemorialBySlug()` ahora:
     - Obtiene las memorias del memorial usando `memoryRepository.findByMemorial_IdMemorialOrderByCreatedDateDesc()`
     - Filtra solo las memorias visibles (`.filter(Memory::isVisible)`)
     - Mapea cada memoria a `PublicMemoryDto`
   - Eliminado valor por defecto en `@Value("${app.share.base-url}")` 
     - ANTES: `@Value("${app.share.base-url:https://jhairt.com}")`
     - AHORA: `@Value("${app.share.base-url}")`

✅ **4. Actualizado: `application-dev.properties`**
   - Agregado: `app.share.base-url=http://localhost:8080`

✅ **5. `application.properties` ya tenía**:
   - `app.share.base-url=https://jhairt.com`

---

## Cómo Activar el Perfil de Desarrollo

### Opción 1: Gradle
```bash
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

### Opción 2: IntelliJ IDEA
1. Edit Configurations
2. En "Active profiles" agrega: `dev`

### Opción 3: Variable de entorno
```bash
$env:SPRING_PROFILES_ACTIVE="dev"
.\gradlew.bat bootRun
```

---

## Estado del Proyecto

✅ **Compilación**: BUILD SUCCESSFUL  
✅ **Errores**: 0  
✅ **Memorias incluidas**: Sí (solo visibles)  
✅ **URL configurable**: Sí (por perfil)  

**Todo listo para usar!** 🚀
