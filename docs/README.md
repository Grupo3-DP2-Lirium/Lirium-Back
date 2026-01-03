# 🌸 Lirium - Backend

<div align="center">

![Lirium Logo](https://cdn.discordapp.com/attachments/1301045138868670495/1456888861941563530/image.png?ex=695a00fc&is=6958af7c&hm=c43fcf56e60348775d1dd1a10544811efe286cf2a49d20066e5ed69b1f0c74ac&)

**Plataforma digital para la preservación de recuerdos y memoriales personales**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-green.svg)](package.json)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

</div>

---

## 📋 Tabla de Contenidos

- [Sobre el Proyecto](#-sobre-el-proyecto)
- [Características Principales](#-características-principales)
- [Arquitectura](#-arquitectura)
- [API Endpoints](#-api-endpoints)

---

## 🌟 Sobre el Proyecto

**Lirium** es una aplicación móvil y web innovadora orientada a la preservación de recuerdos y memorias personales mediante la creación de perfiles y memoriales digitales. Esta es la implementación del backend que proporciona toda la lógica de negocio, APIs REST y gestión de datos para la plataforma.

### Propósito

Lirium permite a los usuarios registrar, organizar y compartir recuerdos en múltiples formatos (texto, imágenes, audio y video) en un entorno seguro y colaborativo. La plataforma facilita la construcción de legados digitales significativos que pueden ser preservados y compartidos con las generaciones futuras.

---

## ✨ Características Principales

### 🎯 Funcionalidades Core

- **📝 Gestión de Recuerdos Multimedia**
  - Soporte para texto, imágenes, audio y video
  - Organización automática en líneas de tiempo
  - Categorización por temáticas y momentos
  - Etiquetado inteligente de contenido

- **👥 Perfiles y Memoriales Digitales**
  - Creación de perfiles personales
  - Memoriales dedicados a personas o eventos
  - Biografías completas y cronologías de vida
  - Control de privacidad granular

- **🤝 Colaboración y Compartición**
  - Memoriales colaborativos multi-usuario
  - Permisos de edición y visualización
  - Sistema de invitaciones
  - Comentarios y reacciones

- **🤖 Inteligencia Artificial**
  - Generación automática de contenido audiovisual
  - Sugerencias de organización
  - Reconocimiento de contenido

- **👨‍💼 Panel Administrativo**
  - Gestión completa de usuarios
  - Supervisión de memoriales
  - Administración de suscripciones
  - Métricas y analíticas de uso
  - Moderación de contenido

- **💳 Sistema de Suscripciones**
  - Planes freemium y premium
  - Gestión de pagos
  - Límites de almacenamiento por tier
  - Funcionalidades premium exclusivas

---

## 🏗️ Arquitectura

### Diagrama de Alto Nivel

```
┌─────────────────┐
│  Cliente Web/   │
│     Móvil       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│          API Gateway / Load Balancer     │
└────────┬─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         Capa de Controladores           │
│   (REST API - Express/NestJS/etc.)      │
└────────┬─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│        Capa de Servicios                │
│    (Lógica de Negocio)                  │
└────────┬─────────────────────────────────┘
         │
         ├──────────────┬──────────────┬───────────────┐
         ▼              ▼              ▼               ▼
    ┌─────────┐   ┌─────────┐   ┌──────────┐   ┌──────────┐
    │  Base   │   │  Cloud  │   │    AI    │   │  Cache   │
    │  Datos  │   │ Storage │   │ Service  │   │  (Redis) │
    └─────────┘   └─────────┘   └──────────┘   └──────────┘
```

### Patrones de Diseño

- **MVC/Layered Architecture**: Separación clara entre controladores, servicios y modelos
- **Repository Pattern**:  Abstracción de la capa de acceso a datos
- **Dependency Injection**: Gestión de dependencias y testabilidad
- **Factory Pattern**: Creación de objetos complejos (generadores de contenido)
- **Strategy Pattern**: Múltiples estrategias de almacenamiento y procesamiento

---

## 🛠️ Tecnologías

### Core Stack

```json
{
  "runtime": "JVM",
  "framework": "SpringBoot",
  "language": "Java",
  "database": "SQL Server 2019"
}
```

## 📡 API Endpoints

### Documentación Interactiva

Una vez que el servidor esté corriendo, accede a la documentación Swagger:

```
http://localhost:3000/api/docs
```

### Principales Endpoints

#### Autenticación

```
POST   /api/v1/auth/register          - Registrar nuevo usuario
POST   /api/v1/auth/login             - Iniciar sesión
POST   /api/v1/auth/logout            - Cerrar sesión
POST   /api/v1/auth/refresh           - Refrescar token
POST   /api/v1/auth/forgot-password   - Recuperar contraseña
POST   /api/v1/auth/reset-password    - Resetear contraseña
```

#### Usuarios

```
GET    /api/v1/users                  - Listar usuarios (admin)
GET    /api/v1/users/:id              - Obtener usuario por ID
GET    /api/v1/users/me               - Obtener perfil actual
PUT    /api/v1/users/me               - Actualizar perfil actual
DELETE /api/v1/users/: id              - Eliminar usuario (admin)
```

#### Memoriales

```
GET    /api/v1/memorials              - Listar memoriales
POST   /api/v1/memorials              - Crear memorial
GET    /api/v1/memorials/:id          - Obtener memorial
PUT    /api/v1/memorials/:id          - Actualizar memorial
DELETE /api/v1/memorials/:id          - Eliminar memorial
GET    /api/v1/memorials/: id/timeline - Obtener línea de tiempo
POST   /api/v1/memorials/:id/invite   - Invitar colaborador
```

#### Recuerdos

```
GET    /api/v1/memories               - Listar recuerdos
POST   /api/v1/memories               - Crear recuerdo
GET    /api/v1/memories/:id           - Obtener recuerdo
PUT    /api/v1/memories/:id           - Actualizar recuerdo
DELETE /api/v1/memories/:id           - Eliminar recuerdo
POST   /api/v1/memories/:id/media     - Subir archivo multimedia
GET    /api/v1/memories/search        - Buscar recuerdos
```

#### Suscripciones

```
GET    /api/v1/subscriptions/plans    - Listar planes
POST   /api/v1/subscriptions          - Crear suscripción
GET    /api/v1/subscriptions/current  - Suscripción actual
PUT    /api/v1/subscriptions/upgrade  - Actualizar plan
DELETE /api/v1/subscriptions          - Cancelar suscripción
POST   /api/v1/subscriptions/webhook  - Webhook de pagos
```

#### Admin

```
GET    /api/v1/admin/stats            - Estadísticas generales
GET    /api/v1/admin/users            - Gestión de usuarios
GET    /api/v1/admin/memorials        - Gestión de memoriales
GET    /api/v1/admin/reports          - Reportes y métricas
POST   /api/v1/admin/moderate         - Moderar contenido
```

---

## 🚢 Despliegue

### Azure / Docker

```bash
# Construir imagen
docker build -t lirium-back: latest .

# Subir a ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr. ecr.us-east-1.amazonaws.com
docker tag lirium-back:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/lirium-back:latest
docker push <account-id>.dkr. ecr.us-east-1.amazonaws.com/lirium-back:latest
```

### Consideraciones de Producción

- ✅ Configurar HTTPS/SSL
- ✅ Implementar rate limiting
- ✅ Configurar CORS adecuadamente
- ✅ Habilitar compression
- ✅ Configurar logging centralizado
- ✅ Implementar health checks
- ✅ Configurar backups automáticos
- ✅ Implementar monitoreo y alertas

---

<div align="center">

**Hecho con ❤️**

⭐ Si te gusta este proyecto, ¡dale una estrella en GitHub! ⭐

</div>
