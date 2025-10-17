# 🚀 Guía Completa CRUD - Memoriales, Recuerdos y Archivos

## 📥 Importar la Colección

1. **Abrir Postman**
2. **Importar** → **Upload Files** → Seleccionar `Postman_Collection_CRUD_Complete.json`
3. La colección aparecerá con 5 secciones principales

## 🔐 1. Autenticación

### Login (Obligatorio primero)
```
POST {{base_url}}/auth/login
```
**Body:**
```json
{
    "email": "tu_email@ejemplo.com",
    "password": "tu_password"
}
```

### Registro (Si necesitas crear usuario)
```
POST {{base_url}}/auth/register
```
**Body:**
```json
{
    "firstName": "Juan",
    "lastName": "Pérez", 
    "email": "juan.perez@ejemplo.com",
    "password": "password123",
    "birthDate": "1990-05-15",
    "gender": "Masculino"
}
```

## 🏛️ 2. Memoriales - CRUD

### Crear Memorial (Solo Datos)
```
POST {{base_url}}/memorials/create
```
**Form Data:**
- `memorial` (text):
```json
{
    "name": "María González",
    "nickname": "Abuela Mary",
    "birthDate": "1935-03-15",
    "gender": "Femenino",
    "description": "Mi querida abuela María...",
    "relationType": "Abuela",
    "collaborative": true,
    "journal": false
}
```

### Crear Memorial (Con Foto de Perfil)
```
POST {{base_url}}/memorials/create
```
**Form Data:**
- `memorial` (text): [JSON como arriba]
- `file` (file): Seleccionar imagen JPG/PNG

### Tipos de Memoriales

#### Memorial Colaborativo
```json
{
    "collaborative": true,
    "journal": false
}
```

#### Memorial Tipo Diario
```json
{
    "collaborative": false,
    "journal": true
}
```

#### Memorial Personal
```json
{
    "collaborative": false,
    "journal": false
}
```

### Listar Memoriales
```
GET {{base_url}}/memorials/my-memorials
```

## 📝 3. Recuerdos - CRUD

### Crear Recuerdo (Solo Texto)
```
POST {{base_url}}/memories
```
**Form Data:**
- `memory` (text):
```json
{
    "memorialId": "{{memorial_id}}",
    "type": "PERSONAL",
    "title": "Un recuerdo especial",
    "description": "Descripción detallada del recuerdo...",
    "photoDate": "2023-12-25",
    "location": "Casa familiar",
    "visible": true,
    "tags": ["familia", "navidad", "especial"],
    "associatedQuestion": "¿Cuál es tu recuerdo favorito?"
}
```

### Crear Recuerdo (Con Archivos)

#### Con Imágenes
**Form Data:**
- `memory` (text): [JSON como arriba]
- `files` (file): imagen1.jpg
- `files` (file): imagen2.png

#### Con Video
**Form Data:**
- `memory` (text): [JSON como arriba]
- `files` (file): video.mp4

#### Con Audio
**Form Data:**
- `memory` (text): [JSON como arriba]
- `files` (file): audio.mp3

#### Mixto (Fotos + Video + Audio)
**Form Data:**
- `memory` (text): [JSON como arriba]
- `files` (file): foto1.jpg
- `files` (file): foto2.png
- `files` (file): video.mp4
- `files` (file): audio.wav

### Actualizar Recuerdo
```
PUT {{base_url}}/memories/{{memory_id}}
```

#### Agregar Archivos
**Form Data:**
- `memory` (text): JSON con campos a actualizar
- `files` (file): nuevos archivos

#### Eliminar Archivos
**Form Data:**
- `memory` (text): JSON con campos a actualizar
- `filesToDelete` (text):
```json
[
    {"id": "file-uuid-1", "path": "ruta/archivo1.jpg"},
    {"id": "file-uuid-2", "path": "ruta/archivo2.mp4"}
]
```

### Listar Recuerdos
```
GET {{base_url}}/memories?memorialId={{memorial_id}}&page=0&size=10
GET {{base_url}}/memories/my-memories
```

## 🎯 4. Visualizar Recuerdos Organizados

### Ver Todos
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=all
```

### Filtrar por Tipo
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=images
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=videos
GET {{base_url}}/memories/memorial/{{memorial_id}}/organized?filterType=documents
```

### Organizar por Categorías
```
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-type
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-timeline
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-themes
GET {{base_url}}/memories/memorial/{{memorial_id}}/by-moments
```

## 📁 5. Gestión de Archivos

### Subir Archivo Individual
```
POST {{base_url}}/files/upload
```
**Form Data:**
- `file` (file): archivo a subir
- `memoryId` (text): ID del recuerdo

### Subir Múltiples Archivos
```
POST {{base_url}}/files/upload-multiple
```
**Form Data:**
- `files` (file): archivo1
- `files` (file): archivo2
- `files` (file): archivo3
- `memoryId` (text): ID del recuerdo

### Obtener/Eliminar Archivo
```
GET {{base_url}}/files/{fileId}
DELETE {{base_url}}/files/{fileId}
```

## 📋 Tipos de Archivos Soportados

### Imágenes
- **Formatos**: JPG, JPEG, PNG, GIF, WEBP
- **Tamaño máximo**: Según configuración del servidor
- **Uso**: Fotos, capturas, ilustraciones

### Videos
- **Formatos**: MP4, AVI, MOV, WMV, MKV
- **Tamaño máximo**: Según configuración del servidor
- **Uso**: Videos familiares, grabaciones especiales

### Audio
- **Formatos**: MP3, WAV, AAC, OGG, M4A
- **Tamaño máximo**: Según configuración del servidor
- **Uso**: Mensajes de voz, música, grabaciones

### Documentos
- **Formatos**: PDF, DOC, DOCX, TXT
- **Tamaño máximo**: Según configuración del servidor
- **Uso**: Cartas, documentos importantes

## 🔄 Flujo de Trabajo Recomendado

### 1. Configuración Inicial
1. **Login** para obtener JWT token
2. **Listar memoriales** existentes
3. **Crear memorial** si es necesario

### 2. Crear Contenido
1. **Crear recuerdo** con datos básicos
2. **Subir archivos** (imágenes, videos, audio)
3. **Actualizar recuerdo** si es necesario

### 3. Visualizar y Organizar
1. **Ver todos los recuerdos** del memorial
2. **Filtrar por tipo** de contenido
3. **Organizar por categorías** (timeline, temas, momentos)

## 🎨 Ejemplos de Datos de Prueba

### Memorial de Abuela
```json
{
    "name": "María González",
    "nickname": "Abuela Mary",
    "birthDate": "1935-03-15",
    "gender": "Femenino",
    "description": "Mi querida abuela María, una mujer extraordinaria que nos enseñó el valor de la familia y la perseverancia.",
    "relationType": "Abuela",
    "collaborative": true,
    "journal": false
}
```

### Recuerdo de Navidad
```json
{
    "memorialId": "memorial-uuid-aqui",
    "type": "PERSONAL",
    "title": "Navidad en familia 2023",
    "description": "Una hermosa celebración navideña con toda la familia reunida. Compartimos risas, regalos y momentos inolvidables.",
    "photoDate": "2023-12-25",
    "location": "Casa de la abuela",
    "visible": true,
    "tags": ["navidad", "familia", "celebración", "2023"],
    "associatedQuestion": "¿Cuál fue tu momento favorito de esta navidad?"
}
```

### Recuerdo de Cumpleaños
```json
{
    "memorialId": "memorial-uuid-aqui",
    "type": "PERSONAL",
    "title": "Cumpleaños número 85",
    "description": "Celebramos el cumpleaños 85 de la abuela con una fiesta sorpresa. Su cara de alegría fue impagable.",
    "photoDate": "2023-03-15",
    "location": "Salón de eventos",
    "visible": true,
    "tags": ["cumpleaños", "sorpresa", "85años", "celebración"],
    "associatedQuestion": "¿Qué fue lo que más te gustó de la celebración?"
}
```

## 🔍 Variables de Postman

La colección incluye estas variables automáticas:

- `{{base_url}}`: URL base del API
- `{{jwt_token}}`: Token JWT (se guarda automáticamente al hacer login)
- `{{memorial_id}}`: ID del memorial (se guarda al crear memorial)
- `{{memory_id}}`: ID del recuerdo (se guarda al crear recuerdo)

## ⚠️ Notas Importantes

1. **Orden de ejecución**: Siempre hacer login primero
2. **Archivos**: Seleccionar archivos reales en tu sistema
3. **IDs**: Los IDs se guardan automáticamente en variables
4. **Tamaños**: Respetar límites de tamaño de archivos
5. **Formatos**: Usar formatos de archivo soportados
6. **Autenticación**: El token se incluye automáticamente

## 🐛 Troubleshooting

### Error 401 - Unauthorized
- Verificar que el token JWT esté válido
- Hacer login nuevamente

### Error 400 - Bad Request
- Verificar formato JSON en campos de texto
- Verificar que todos los campos requeridos estén presentes

### Error 413 - Payload Too Large
- Reducir tamaño de archivos
- Subir archivos de uno en uno

### Error 415 - Unsupported Media Type
- Verificar formato de archivo soportado
- Usar Content-Type correcto

¡Con esta guía completa puedes crear y gestionar todo el contenido de Lirium! 🎉