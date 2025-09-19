# Documentación del Modelo de Datos - Lirium Back

## Descripción General
Este diagrama PlantUML representa el modelo de datos completo del backend de Lirium, una plataforma para crear memoriales digitales colaborativos.

## Archivos de Diagramas
- `modelo-datos.puml` - Diagrama completo del modelo de datos

## Cómo visualizar el diagrama

### Opción 1: VS Code con extensión PlantUML
1. Instalar la extensión "PlantUML" en VS Code
2. Abrir el archivo `modelo-datos.puml`
3. Usar Ctrl+Shift+P y buscar "PlantUML: Preview Current Diagram"

### Opción 2: PlantUML Online
1. Copiar el contenido del archivo `.puml`
2. Pegarlo en http://www.plantuml.com/plantuml/uml/
3. Ver el diagrama generado

### Opción 3: Exportar como imagen
Desde VS Code con la extensión PlantUML:
- Ctrl+Shift+P → "PlantUML: Export Current Diagram"
- Elegir formato (PNG, SVG, etc.)

## Estructura del Modelo

### Entidades Principales
- **Usuario**: Gestión de usuarios del sistema
- **Memorial**: Contenedor principal de memorias de una persona
- **Memoria**: Elementos individuales (texto, foto, audio, video)
- **Pregunta/Respuesta**: Sistema de preguntas guiadas

### Funcionalidades Sociales
- **Colaborador**: Invitaciones para memoriales colaborativos
- **Comentario**: Comentarios en memorias
- **Reaccion**: Reacciones tipo redes sociales

### Sistema de Suscripciones
- **Plan**: Planes de suscripción (FREE, PREMIUM, etc.)
- **Suscripcion**: Suscripciones activas
- **IntentoPago**: Tracking de pagos

### Funcionalidades Adicionales
- **VideoGenerado**: Videos generados por IA
- **Evento**: Eventos asociados a memoriales
- **Recordatorio**: Sistema de recordatorios

## Convenciones del Diagrama
- Los enums están claramente identificados
- Las relaciones muestran cardinalidad (1:1, 1:N, N:M)
- Los campos mantienen los nombres exactos del código
- Los tipos de datos están especificados según las entidades Java