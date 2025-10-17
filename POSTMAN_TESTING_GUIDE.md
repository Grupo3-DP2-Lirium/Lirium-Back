# 🚀 Guía de Testing con Postman - Visualizar Recuerdos

## 📥 Importar la Colección

1. **Abrir Postman**
2. **Importar** → **Upload Files** → Seleccionar `Postman_Collection_Visualizar_Recuerdos.json`
3. La colección aparecerá en tu workspace

## ⚙️ Configuración Inicial

### Variables de Entorno
La colección incluye estas variables que puedes modificar:

- `base_url`: `http://localhost:8080/api` (cambiar si tu servidor está en otro puerto)
- `jwt_token`: Se auto-completa al hacer login
- `memorial_id`: `11111111-1111-1111-1111-111111111111` (ID del memorial de prueba)

### Modificar Variables
1. **Click derecho** en la colección → **Edit**
2. **Variables** tab → Modificar valores según tu configuración

## 🔐 Paso 1: Autenticación

### Login
```
POST {{base_url}}/auth/login
```

**Body (JSON):**
```json
{
    "email": "tu_email@ejemplo.com",
    "password": "tu_password"
}
```

**Resultado esperado:**
- Status: `200 OK`
- El token JWT se guarda automáticamente en `{{jwt_token}}`

## 📋 Paso 2: Verificar Memoriales

### Listar Mis Memoriales
```
GET {{base_url}}/memorials/my-memorials
```

**Resultado esperado:**
- Lista de memoriales del usuario
- Copiar un `idMemorial` para usar en las pruebas

## 🎯 Paso 3: Probar Funcionalidades de Visualización

### 3.1 Recuerdos Organizados - Ver Todo
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=all&sortBy=date&sortOrder=desc&page=0&size=20
```

**Parámetros disponibles:**
- `filterType`: `all`, `images`, `videos`, `documents`
- `sortBy`: `date`, `type`, `moments`, `themes`
- `sortOrder`: `asc`, `desc`
- `page`: número de página (base 0)
- `size`: elementos por página

**Respuesta esperada:**
```json
{
    "memories": [...],
    "metadata": {
        "totalImages": 5,
        "totalVideos": 2,
        "totalDocuments": 1
    },
    "totalElements": 10,
    "totalPages": 1,
    "currentPage": 0,
    "filterType": "all",
    "sortBy": "date"
}
```

### 3.2 Recuerdos por Tipo de Archivo
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-type
```

**Respuesta esperada:**
```json
{
    "memoriesByType": {
        "image": [...],
        "video": [...],
        "text": [...]
    },
    "countByType": {
        "image": 3,
        "video": 1,
        "text": 6
    },
    "totalMemories": 10
}
```

### 3.3 Recuerdos por Timeline
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-timeline
```

**Con filtros:**
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-timeline?year=2020
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-timeline?year=2020&month=12
```

**Respuesta esperada:**
```json
{
    "memoriesByTimeline": {
        "2020": {
            "12": [...],
            "06": [...]
        },
        "2019": {
            "11": [...]
        }
    },
    "countByYear": {
        "2020": 5,
        "2019": 3
    },
    "countByMonth": {
        "2020": {
            "12": 2,
            "06": 3
        }
    },
    "totalMemories": 10
}
```

### 3.4 Recuerdos por Temas
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-themes
```

**Respuesta esperada:**
```json
{
    "memoriesByTheme": {
        "familia": [...],
        "tradiciones": [...],
        "viajes": [...]
    },
    "countByTheme": {
        "familia": 4,
        "tradiciones": 2,
        "viajes": 1
    },
    "availableThemes": ["familia", "tradiciones", "viajes", "cocina"],
    "totalMemories": 10
}
```

### 3.5 Recuerdos por Momentos
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-moments
```

**Respuesta esperada:**
```json
{
    "memoriesByMoment": {
        "Familia": [...],
        "Infancia": [...],
        "Viajes": [...],
        "Otros momentos": [...]
    },
    "countByMoment": {
        "Familia": 6,
        "Infancia": 2,
        "Viajes": 1,
        "Otros momentos": 1
    },
    "availableMoments": ["Familia", "Infancia", "Viajes", "Otros momentos"],
    "totalMemories": 10
}
```

## 🔍 Casos de Prueba Específicos

### Filtrar Solo Imágenes
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=images
```

### Filtrar Solo Videos
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=videos
```

### Ordenar por Tipo
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?sortBy=type&sortOrder=asc
```

### Paginación
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?page=0&size=5
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?page=1&size=5
```

## ❌ Casos de Error a Probar

### Sin Autenticación
- Quitar el header `Authorization`
- **Resultado esperado**: `401 Unauthorized`

### Memorial Inexistente
- Usar un UUID que no existe
- **Resultado esperado**: `404 Not Found` o `400 Bad Request`

### Memorial de Otro Usuario
- Usar el ID de un memorial que no pertenece al usuario
- **Resultado esperado**: `403 Forbidden` o lista vacía

## 📊 Verificación de Resultados

### ✅ Checklist de Funcionalidades

- [ ] **Login exitoso** y token guardado
- [ ] **Listar memoriales** del usuario
- [ ] **Ver todos los recuerdos** organizados
- [ ] **Filtrar por imágenes** solamente
- [ ] **Filtrar por videos** solamente
- [ ] **Agrupar por tipo** de archivo
- [ ] **Organizar por timeline** (años/meses)
- [ ] **Filtrar por año específico**
- [ ] **Filtrar por año y mes**
- [ ] **Agrupar por temas/tags**
- [ ] **Agrupar por momentos**
- [ ] **Paginación** funcionando
- [ ] **Ordenamiento** por fecha
- [ ] **Manejo de errores** (401, 404, etc.)

### 🔢 Datos Esperados (con los inserts de prueba)

Si ejecutaste los scripts SQL de prueba, deberías ver:

- **6 memoriales** en total
- **10 recuerdos** distribuidos entre los memoriales
- **Tags variados**: familia, tradiciones, viajes, etc.
- **Fechas diferentes**: 2015-2020
- **Tipos de contenido**: principalmente texto (ya que no subimos archivos)

## 🐛 Troubleshooting

### Error 401 - Unauthorized
- Verificar que el token JWT esté en el header
- Hacer login nuevamente si el token expiró

### Error 404 - Memorial not found
- Verificar que el `memorial_id` existe
- Usar un ID de memorial que pertenezca al usuario autenticado

### Respuesta vacía
- Verificar que el memorial tenga recuerdos
- Ejecutar los scripts SQL de prueba

### Error de conexión
- Verificar que el servidor Spring Boot esté corriendo
- Verificar la URL base en las variables de Postman

## 📝 Notas Importantes

1. **Orden de ejecución**: Siempre hacer login primero
2. **IDs de prueba**: Usar los IDs de los scripts SQL proporcionados
3. **Datos de prueba**: Los recuerdos de prueba son solo texto, no tienen archivos
4. **Paginación**: Los índices empiezan en 0
5. **Fechas**: Formato ISO para filtros de timeline (YYYY-MM-DD)

¡Con esta guía deberías poder probar completamente toda la funcionalidad de "Visualizar Recuerdos"! 🎉