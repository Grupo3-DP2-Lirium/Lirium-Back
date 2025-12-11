# 📘 Manual de Instalación y Documento de Pase a Producción

## Lirium Backend - Spring Boot Application

---

**Versión del Documento:** 1.0  
**Fecha:** Diciembre 2024  
**Proyecto:** Lirium - Plataforma de Memoriales Digitales  
**Repositorio:** [Grupo3-DP2-Lirium/Lirium-Back](https://github.com/Grupo3-DP2-Lirium/Lirium-Back)

---

## 📑 Tabla de Contenidos

1. [Información General del Proyecto](#1-información-general-del-proyecto)
2. [Requisitos del Sistema](#2-requisitos-del-sistema)
3. [Arquitectura del Sistema](#3-arquitectura-del-sistema)
4. [Instalación en Entorno Local](#4-instalación-en-entorno-local-desarrollo)
5. [Configuración de Servicios Externos](#5-configuración-de-servicios-externos)
6. [Pase a Producción](#6-pase-a-producción)
7. [Variables de Entorno y Secretos](#7-variables-de-entorno-y-secretos)
8. [Troubleshooting](#8-troubleshooting)
9. [Rollback y Recuperación](#9-rollback-y-recuperación)
10. [Contactos y Soporte](#10-contactos-y-soporte)

---

## 1. Información General del Proyecto

### 1.1 Descripción

**Lirium** es una plataforma de memoriales digitales que permite a los usuarios crear, gestionar y compartir memoriales con fotos, videos y cápsulas del tiempo. El backend está desarrollado en **Spring Boot 3.5.5** con **Java 21**.

### 1.2 Tecnologías Principales

| Componente | Tecnología | Versión |
|------------|------------|---------|
| **Lenguaje** | Java | 21 (Temurin) |
| **Framework** | Spring Boot | 3.5.5 |
| **Build Tool** | Gradle | 8.x |
| **Base de Datos** | Azure SQL Server | Latest |
| **Almacenamiento** | Azure Blob Storage | - |
| **Contenedor** | Docker (Alpine) | - |
| **Hosting** | Azure Container Apps | - |
| **CI/CD** | GitHub Actions | - |
| **IaC** | Terraform | ≥1.6.0 |

### 1.3 Dependencias Principales

```
- Spring Boot Starter Web, Data JPA, Security, Validation
- Spring WebSocket (STOMP)
- Firebase Admin SDK 9.3.0
- Azure Storage Blob 12.25.1
- SendGrid Java 4.10.2
- Google Analytics Data API
- JWT (jjwt 0.12.3)
- SpringDoc OpenAPI 2.7.0
- FFmpeg (para generación de documentales)
```

### 1.4 Funcionalidades Clave

- ✅ Autenticación JWT con Firebase
- ✅ Gestión de memoriales y cápsulas del tiempo
- ✅ Subida y procesamiento de imágenes/videos
- ✅ Generación automática de documentales (FFmpeg)
- ✅ Clasificación de contenido con IA (OpenRouter)
- ✅ Integración con PayPal para suscripciones
- ✅ Notificaciones por email (SendGrid)
- ✅ Analytics con Google Analytics
- ✅ WebSockets para actualizaciones en tiempo real

---

## 2. Requisitos del Sistema

### 2.1 Requisitos de Hardware

#### Desarrollo Local

| Recurso | Mínimo | Recomendado |
|---------|--------|-------------|
| CPU | 2 cores | 4+ cores |
| RAM | 4 GB | 8+ GB |
| Disco | 10 GB libres | 20+ GB SSD |

#### Producción (Azure Container Apps)

| Recurso | Dev | Staging | Prod |
|---------|-----|---------|------|
| CPU | 0.5 cores | 0.5 cores | 1.0 cores |
| Memoria | 1 Gi | 1 Gi | 2 Gi |
| Réplicas | 0-2 | 1-2 | 1-5 |

### 2.2 Requisitos de Software

#### Desarrollo Local

| Software | Versión | Instalación |
|----------|---------|-------------|
| **JDK** | 21 (Temurin) | [Adoptium](https://adoptium.net/) |
| **Git** | Latest | [Git SCM](https://git-scm.com/) |
| **Docker Desktop** | Latest | [Docker](https://www.docker.com/products/docker-desktop) |
| **IDE** | VS Code o IntelliJ | - |
| **Azure CLI** | Latest | `winget install Microsoft.AzureCLI` |
| **Terraform** | ≥1.6.0 | [Terraform](https://www.terraform.io/downloads) |

#### Verificación de Instalación

```powershell
# Verificar Java
java -version
# Esperado: openjdk version "21.x.x"

# Verificar Gradle (wrapper incluido)
./gradlew --version

# Verificar Docker
docker --version

# Verificar Azure CLI
az --version

# Verificar Terraform
terraform --version
```

### 2.3 Servicios Cloud Requeridos

| Servicio | Propósito | Cuenta Requerida |
|----------|-----------|------------------|
| **Azure** | Hosting, DB, Storage | Suscripción activa |
| **Firebase** | Autenticación, Push Notifications | Proyecto Firebase |
| **SendGrid** | Envío de emails | Cuenta SendGrid |
| **PayPal** | Pagos y suscripciones | Cuenta Business |
| **OpenRouter** | API de IA para clasificación | Cuenta OpenRouter |
| **Google Analytics** | Analytics de usuarios | Cuenta GA4 |

---

## 3. Arquitectura del Sistema

### 3.1 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AZURE CLOUD                                     │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Resource Group (rg-lirium-{env})                  │   │
│  │                                                                      │   │
│  │   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐       │   │
│  │   │  SQL Server  │     │   Storage    │     │     ACR      │       │   │
│  │   │  + Database  │     │   Account    │     │  (Registry)  │       │   │
│  │   │              │     │  + Blob      │     │              │       │   │
│  │   └──────┬───────┘     └──────┬───────┘     └──────┬───────┘       │   │
│  │          │                    │                    │                │   │
│  │          └────────────────────┼────────────────────┘                │   │
│  │                               │                                      │   │
│  │                    ┌──────────▼──────────┐                          │   │
│  │                    │  Container Apps Env  │                          │   │
│  │                    │                      │                          │   │
│  │                    │  ┌────────────────┐ │                          │   │
│  │                    │  │ Lirium Backend │ │ ◄──── HTTPS (443)        │   │
│  │                    │  │  Spring Boot   │ │                          │   │
│  │                    │  │  + FFmpeg      │ │                          │   │
│  │                    │  └────────────────┘ │                          │   │
│  │                    └─────────────────────┘                          │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ HTTPS
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SERVICIOS EXTERNOS                                 │
│                                                                              │
│   ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌─────────┐ │
│   │  Firebase │  │ SendGrid  │  │  PayPal   │  │ OpenRouter│  │   GA4   │ │
│   │   Auth    │  │   Email   │  │  Payments │  │    AI     │  │Analytics│ │
│   └───────────┘  └───────────┘  └───────────┘  └───────────┘  └─────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Flujo de Deployment

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   GitHub    │────▶│   GitHub    │────▶│    Azure    │────▶│  Container  │
│  Push/PR    │     │   Actions   │     │     ACR     │     │    Apps     │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                          │
                          ▼
                    ┌─────────────┐
                    │   Docker    │
                    │    Build    │
                    │  + Tests    │
                    └─────────────┘
```

---

## 4. Instalación en Entorno Local (Desarrollo)

### 4.1 Clonar el Repositorio

```powershell
# Clonar el repositorio
git clone https://github.com/Grupo3-DP2-Lirium/Lirium-Back.git

# Navegar al directorio
cd Lirium-Back
```

### 4.2 Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto (no commitear):

```bash
# Database
DATABASE_URL=jdbc:sqlserver://localhost:1433;database=lirium_dev;encrypt=false
DATABASE_USERNAME=sa
DATABASE_PASSWORD=YourLocalPassword123!

# Azure Storage (usar emulador o cuenta de dev)
AZURE_STORAGE_CONNECTION_STRING=UseDevelopmentStorage=true
AZURE_STORAGE_CONTAINER_NAME=lirium-files

# JWT
JWT_SECRET=myLocalDevSecretKeyThatIsAtLeast32BytesLong

# SendGrid (opcional para dev)
SENDGRID_API_KEY=SG.your-dev-key
SENDGRID_ENABLED=false

# OpenRouter
OPENROUTER_API_KEY=sk-or-v1-your-key

# Firebase (path al archivo JSON)
FIREBASE_CREDENTIALS_PATH=src/main/resources/firebase.json
```

### 4.3 Configurar Base de Datos Local

#### Opción A: SQL Server con Docker

```powershell
# Ejecutar SQL Server en Docker
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" `
  -p 1433:1433 --name sql-lirium `
  -d mcr.microsoft.com/mssql/server:2022-latest

# Crear la base de datos
docker exec -it sql-lirium /opt/mssql-tools18/bin/sqlcmd `
  -S localhost -U sa -P "YourStrong@Passw0rd" -C `
  -Q "CREATE DATABASE lirium_dev"
```

#### Opción B: Usar Azure SQL Database de desarrollo

Configurar las credenciales en `application-dev.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://your-dev-server.database.windows.net:1433;database=lirium-dev;encrypt=true
spring.datasource.username=your-admin
spring.datasource.password=your-password
```

### 4.4 Configurar Credenciales de Firebase

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Seleccionar tu proyecto → Configuración → Cuentas de servicio
3. Generar nueva clave privada (JSON)
4. Guardar como `src/main/resources/firebase.json`

### 4.5 Ejecutar la Aplicación

```powershell
# Dar permisos de ejecución a gradlew (si es necesario)
# En PowerShell no es necesario, en Git Bash: chmod +x gradlew

# Ejecutar en modo desarrollo
./gradlew bootRunDev

# O con el profile específico
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4.6 Verificar Instalación

```powershell
# Health Check
curl http://localhost:8080/actuator/health

# Swagger UI
# Abrir en navegador: http://localhost:8080/swagger-ui.html

# API Docs
curl http://localhost:8080/v3/api-docs
```

**Resultado esperado:**
- ✅ Aplicación arranca sin errores
- ✅ Swagger UI accesible
- ✅ Conexión a base de datos exitosa

---

## 5. Configuración de Servicios Externos

### 5.1 Azure SQL Database

#### Crear desde Azure Portal

1. Ir a [Azure Portal](https://portal.azure.com)
2. Crear recurso → SQL Database
3. Configurar:
   - **Nombre:** `lirium-{env}`
   - **Servidor:** Crear nuevo o usar existente
   - **Compute + Storage:** Basic (dev) / S1 (prod)
4. Configurar reglas de firewall para permitir servicios de Azure

#### String de Conexión

```
jdbc:sqlserver://{server-name}.database.windows.net:1433;
database={database-name};
encrypt=true;
trustServerCertificate=false;
loginTimeout=30
```

### 5.2 Azure Blob Storage

#### Crear desde Azure Portal

1. Crear Storage Account
   - **Nombre:** `stlirium{env}` (sin guiones, minúsculas)
   - **Redundancia:** LRS (dev) / GRS (prod)
2. Crear Container: `lirium-files`
3. Configurar CORS si es necesario

#### Connection String

Obtener desde: Storage Account → Access Keys → Connection String

### 5.3 Firebase

#### Configuración Inicial

1. Crear proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Habilitar Authentication con Email/Password
3. Ir a Project Settings → Service Accounts
4. Generate New Private Key
5. Guardar JSON de forma segura

#### Formato del JSON (para GitHub Secrets)

El JSON debe estar en una sola línea para usarlo como secret:

```bash
# Comprimir JSON a una línea
cat firebase.json | jq -c '.'
```

### 5.4 SendGrid

#### Crear API Key

1. Ir a [SendGrid Dashboard](https://app.sendgrid.com/)
2. Settings → API Keys → Create API Key
3. Dar permisos de "Mail Send"
4. Copiar la API Key (solo se muestra una vez)

#### Verificar Sender

1. Settings → Sender Authentication
2. Verificar el dominio o email de envío
3. Usar `no-reply@tudominio.com` como remitente

### 5.5 PayPal

#### Configuración Sandbox (Desarrollo)

1. Ir a [PayPal Developer](https://developer.paypal.com/)
2. Dashboard → My Apps & Credentials
3. Crear App en modo Sandbox
4. Obtener Client ID y Secret

#### Configuración Live (Producción)

1. Cambiar a modo "Live"
2. Crear App de producción
3. Completar verificación de cuenta Business
4. Actualizar credenciales

### 5.6 OpenRouter AI

1. Crear cuenta en [OpenRouter](https://openrouter.ai/)
2. Ir a API Keys → Create Key
3. Configurar límites de uso si es necesario

---

## 6. Pase a Producción

### 6.1 Lista de Verificación Pre-Producción

#### ✅ Código y Tests

- [ ] Todos los tests pasan (`./gradlew test`)
- [ ] Sin errores de compilación
- [ ] Code review completado
- [ ] Merge a branch `main` aprobado

#### ✅ Configuración

- [ ] Todos los secrets configurados en GitHub
- [ ] Variables de entorno verificadas
- [ ] Credenciales de producción actualizadas
- [ ] PayPal en modo "Live" (si aplica)

#### ✅ Infraestructura

- [ ] Recursos de Azure aprovisionados
- [ ] Firewall de SQL Server configurado
- [ ] ACR accesible
- [ ] DNS/dominio configurado (si aplica)

#### ✅ Seguridad

- [ ] JWT secret fuerte (32+ bytes)
- [ ] Contraseñas de BD robustas
- [ ] HTTPS habilitado
- [ ] Secrets no expuestos en código

### 6.2 Aprovisionamiento de Infraestructura con Terraform

#### Paso 1: Crear Backend para tfstate

```powershell
# Login en Azure
az login

# Crear Resource Group para tfstate
az group create --name rg-lirium-tfstate --location "East US 2"

# Crear Storage Account
az storage account create `
  --name stliriumtfstate `
  --resource-group rg-lirium-tfstate `
  --location "East US 2" `
  --sku Standard_LRS

# Crear Container
az storage container create `
  --name tfstate `
  --account-name stliriumtfstate
```

#### Paso 2: Crear Service Principal

```powershell
# Crear Service Principal para Terraform
az ad sp create-for-rbac `
  --name "sp-lirium-terraform" `
  --role contributor `
  --scopes /subscriptions/{subscription-id} `
  --sdk-auth

# Guardar el JSON output como AZURE_CREDENTIALS en GitHub Secrets
```

#### Paso 3: Configurar Secrets en GitHub

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `AZURE_CREDENTIALS` | JSON del Service Principal | `{"clientId":"...",...}` |
| `AZURE_CLIENT_ID` | ID del SP | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `AZURE_CLIENT_SECRET` | Secret del SP | `xxxxxxxxx` |
| `AZURE_SUBSCRIPTION_ID` | ID de suscripción | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `AZURE_TENANT_ID` | ID del tenant | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `SQL_ADMIN_PASSWORD` | Password de SQL Server | `SecureP@ss2024!` |
| `SENDGRID_API_KEY` | API Key de SendGrid | `SG.xxxxxxx` |
| `OPENROUTER_API_KEY` | API Key de OpenRouter | `sk-or-v1-xxx` |
| `FIREBASE_CREDENTIALS_JSON` | JSON de Firebase (1 línea) | `{"type":"service_account",...}` |
| `GOOGLE_ANALYTICS_CREDENTIALS_JSON` | JSON de GA (1 línea) | `{"type":"service_account",...}` |
| `PAYPAL_CLIENT_SECRET` | Secret de PayPal | `xxxxxxxxx` |
| `JWT_SECRET` | Secret para JWT | `myVerySecretKey...` |

#### Paso 4: Ejecutar Terraform

**Opción A: Usando GitHub Actions (Recomendado)**

1. Ir a **Actions** → **🏗️ Terraform Infrastructure**
2. Click **Run workflow**
3. Seleccionar:
   - Environment: `prod`
   - Action: `plan`
4. Revisar el plan
5. Ejecutar de nuevo con Action: `apply`

**Opción B: Ejecución Local**

```powershell
cd terraform

# Copiar backend config
Copy-Item environments/prod/backend.tf .

# Inicializar
terraform init

# Plan
terraform plan -var-file="environments/prod/terraform.tfvars" `
  -var="sql_admin_password=$env:SQL_ADMIN_PASSWORD"

# Aplicar
terraform apply -var-file="environments/prod/terraform.tfvars" `
  -var="sql_admin_password=$env:SQL_ADMIN_PASSWORD"
```

### 6.3 Despliegue de la Aplicación

#### Método Automático: Push a Main

El workflow `deploy-backend.yml` se ejecuta automáticamente con cada push a `main`:

```powershell
git add .
git commit -m "feat: nueva funcionalidad"
git push origin main
```

#### Método Manual: GitHub Actions

1. Ir a **Actions** → **Deploy Lirium Backend to Azure Container Apps**
2. Seleccionar el workflow run más reciente
3. Verificar que todos los pasos pasen

### 6.4 Verificación Post-Deployment

#### Health Check

```powershell
# Verificar que la aplicación responde
curl https://lirium-backend.{region}.azurecontainerapps.io/actuator/health

# Resultado esperado
{"status":"UP"}
```

#### Verificar Logs

```powershell
# Ver logs del Container App
az containerapp logs show `
  --name ca-lirium-prod `
  --resource-group rg-lirium-prod `
  --follow
```

#### Verificar Swagger

Abrir en navegador:
```
https://lirium-backend.{region}.azurecontainerapps.io/swagger-ui.html
```

#### Pruebas de Humo

| Endpoint | Método | Esperado |
|----------|--------|----------|
| `/actuator/health` | GET | 200 OK |
| `/v3/api-docs` | GET | 200 OK + JSON |
| `/api/auth/login` | POST | 401/200 |
| `/api/memorials` | GET | 401 (sin auth) |

---

## 7. Variables de Entorno y Secretos

### 7.1 Configuración por Entorno

#### Desarrollo (`application-dev.properties`)

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `spring.jpa.hibernate.ddl-auto` | `update` | Actualiza schema |
| `spring.jpa.show-sql` | `true` | Muestra queries |
| `sendgrid.enabled` | `false` | Emails deshabilitados |
| `paypal.base.url` | `api.sandbox.paypal.com` | PayPal Sandbox |

#### Producción (`application-prod.properties`)

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `spring.jpa.hibernate.ddl-auto` | `update` | Solo actualiza |
| `spring.jpa.show-sql` | `false` | Sin logs de SQL |
| `sendgrid.enabled` | `true` | Emails activos |
| `paypal.base.url` | `api.paypal.com` | PayPal Live |

### 7.2 Secretos de GitHub (Referencia Completa)

```yaml
# Azure Authentication
AZURE_CREDENTIALS          # JSON completo del Service Principal
AZURE_CLIENT_ID            # Client ID del SP
AZURE_CLIENT_SECRET        # Client Secret del SP
AZURE_SUBSCRIPTION_ID      # ID de la suscripción
AZURE_TENANT_ID            # ID del tenant

# Azure Resources
ACR_LOGIN_SERVER           # acr{name}.azurecr.io
CONTAINER_APP_NAME         # ca-lirium-prod
RESOURCE_GROUP             # rg-lirium-prod

# Database
DATABASE_URL               # jdbc:sqlserver://...
DATABASE_USERNAME          # admin username
DATABASE_PASSWORD          # admin password
SQL_ADMIN_PASSWORD         # mismo que DATABASE_PASSWORD

# Storage
AZURE_STORAGE_CONNECTION_STRING  # DefaultEndpointsProtocol=https;...
AZURE_STORAGE_CONTAINER_NAME     # lirium-files
STORAGE_PROVIDER           # azure

# Services
SENDGRID_API_KEY           # SG.xxxxx
OPENROUTER_API_KEY         # sk-or-v1-xxxxx
FIREBASE_CREDENTIALS       # JSON en una línea
GOOGLE_ANALYTICS_CREDENTIALS # JSON en una línea
PAYPAL_CLIENT_SECRET       # xxxxx
JWT_SECRET                 # string de 32+ caracteres
```

---

## 8. Troubleshooting

### 8.1 Errores Comunes de Compilación

#### Error: "Cannot find symbol"

```
Causa: Dependencia faltante o código incompleto
Solución: ./gradlew clean build --refresh-dependencies
```

#### Error: "Java version mismatch"

```
Causa: JDK incorrecto
Solución: Verificar JAVA_HOME apunta a JDK 21
  - Windows: $env:JAVA_HOME
  - Linux/Mac: echo $JAVA_HOME
```

### 8.2 Errores de Arranque

#### Error: "Failed to configure datasource"

```
Causa: Credenciales de BD incorrectas o BD no accesible
Solución:
1. Verificar DATABASE_URL, USERNAME, PASSWORD
2. Verificar firewall de Azure SQL permite la IP
3. Probar conexión con Azure Data Studio
```

#### Error: "Bean creation failed: azureBlobStorageService"

```
Causa: Property faltante de storage
Solución: Agregar en properties:
  app.storage.max-file-size=104857600
  app.storage.provider=azure
```

#### Error: "Firebase initialization failed"

```
Causa: Credenciales de Firebase incorrectas o malformadas
Solución:
1. Verificar que firebase.json existe
2. Verificar JSON válido (usar jq para validar)
3. Regenerar credenciales en Firebase Console
```

### 8.3 Errores de Deployment

#### Error: "ACR login failed"

```powershell
# Verificar login manual
az acr login --name acrvacapp

# Si falla, verificar permisos del Service Principal
az role assignment create `
  --assignee {client-id} `
  --role AcrPush `
  --scope /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.ContainerRegistry/registries/{acr}
```

#### Error: "Container App update failed"

```powershell
# Ver logs del Container App
az containerapp logs show --name ca-lirium-prod --resource-group rg-lirium-prod

# Verificar imagen existe en ACR
az acr repository show-tags --name acrvacapp --repository lirium-backend
```

### 8.4 Errores de Runtime

#### Error: "NullPointerException on memoryIds"

```
Causa: Race condition entre commit de transacción y proceso async
Solución: Ya corregido - pasar memoryIds como parámetro
Archivo: CapsuleProcessingService.java
```

#### Error: "FFmpeg not found"

```
Causa: FFmpeg no instalado en el container
Solución: Verificar Dockerfile incluye:
  RUN apk add --no-cache ffmpeg
```

### 8.5 Comandos Útiles de Diagnóstico

```powershell
# Ver estado del Container App
az containerapp show --name ca-lirium-prod --resource-group rg-lirium-prod

# Ver métricas
az containerapp revision list --name ca-lirium-prod --resource-group rg-lirium-prod

# Restart del Container App
az containerapp revision restart `
  --name ca-lirium-prod `
  --resource-group rg-lirium-prod `
  --revision {revision-name}

# Ver configuración actual
az containerapp show --name ca-lirium-prod --resource-group rg-lirium-prod --query properties.configuration
```

---

## 9. Rollback y Recuperación

### 9.1 Rollback de Aplicación

#### Volver a Revisión Anterior

```powershell
# Listar revisiones
az containerapp revision list `
  --name ca-lirium-prod `
  --resource-group rg-lirium-prod `
  --query "[].name"

# Activar revisión anterior
az containerapp ingress traffic set `
  --name ca-lirium-prod `
  --resource-group rg-lirium-prod `
  --revision-weight {old-revision}=100
```

#### Re-deploy de Commit Específico

1. Ir a **Actions** → Seleccionar workflow anterior exitoso
2. Click "Re-run all jobs"

### 9.2 Rollback de Infraestructura

```powershell
cd terraform

# Revertir a estado anterior
terraform apply -target=module.{module_name} -var-file="environments/prod/terraform.tfvars"

# En caso extremo, destruir y recrear
# ⚠️ CUIDADO: Esto elimina datos
terraform destroy -target=module.{module_name}
terraform apply -var-file="environments/prod/terraform.tfvars"
```

### 9.3 Backup y Restore de Base de Datos

#### Crear Backup Manual

```powershell
# Desde Azure Portal: SQL Database → Export
# O usando Azure CLI:
az sql db export `
  --admin-password $SQL_PASSWORD `
  --admin-user $SQL_ADMIN `
  --storage-key $STORAGE_KEY `
  --storage-key-type StorageAccessKey `
  --storage-uri "https://stlirium.blob.core.windows.net/backups/lirium.bacpac" `
  --name lirium `
  --resource-group rg-lirium-prod `
  --server sql-lirium-prod
```

#### Restore desde Backup

```powershell
az sql db import `
  --admin-password $SQL_PASSWORD `
  --admin-user $SQL_ADMIN `
  --storage-key $STORAGE_KEY `
  --storage-key-type StorageAccessKey `
  --storage-uri "https://stlirium.blob.core.windows.net/backups/lirium.bacpac" `
  --name lirium-restored `
  --resource-group rg-lirium-prod `
  --server sql-lirium-prod
```

---

## 10. Contactos y Soporte

### 10.1 Equipo de Desarrollo

| Rol | Nombre | Contacto |
|-----|--------|----------|
| **Tech Lead** | [Nombre] | [email] |
| **Backend Developer** | [Nombre] | [email] |
| **DevOps** | [Nombre] | [email] |

### 10.2 Recursos de Soporte

| Recurso | URL |
|---------|-----|
| **Repositorio** | https://github.com/Grupo3-DP2-Lirium/Lirium-Back |
| **Issues** | https://github.com/Grupo3-DP2-Lirium/Lirium-Back/issues |
| **Azure Portal** | https://portal.azure.com |
| **Firebase Console** | https://console.firebase.google.com |

### 10.3 Documentación Adicional

- [Terraform README](../terraform/README.md)
- [API Documentation](./README.md)
- [Modelo de Datos](./modelo-datos.puml)

---

## 📝 Historial de Cambios del Documento

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 1.0 | Dic 2024 | Equipo Lirium | Versión inicial |

---

**Fin del Documento**
