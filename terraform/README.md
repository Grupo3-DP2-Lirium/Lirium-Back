# 🏗️ Terraform Infrastructure - Lirium Backend

Este directorio contiene la infraestructura como código (IaC) para desplegar el backend de Lirium en Azure.

## 📋 Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Prerrequisitos](#prerrequisitos)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Configuración Inicial](#configuración-inicial)
- [Uso](#uso)
- [Entornos](#entornos)
- [Secretos](#secretos)
- [Troubleshooting](#troubleshooting)

## 🏛️ Arquitectura

La infraestructura despliega los siguientes recursos de Azure:

```
┌─────────────────────────────────────────────────────────────────┐
│                     Azure Resource Group                         │
│                     (rg-lirium-{env})                           │
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │   SQL Server     │  │ Storage Account  │  │     ACR       │ │
│  │   + Database     │  │   + Container    │  │  (Registry)   │ │
│  └────────┬─────────┘  └────────┬─────────┘  └───────┬───────┘ │
│           │                     │                     │         │
│           └──────────┬──────────┴─────────────────────┘         │
│                      │                                          │
│           ┌──────────▼──────────┐                               │
│           │  Container Apps Env  │                               │
│           │  ┌────────────────┐ │                               │
│           │  │  Container App │ │                               │
│           │  │   (Backend)    │ │                               │
│           │  └────────────────┘ │                               │
│           └─────────────────────┘                               │
└─────────────────────────────────────────────────────────────────┘
```

## ✅ Prerrequisitos

1. **Terraform** >= 1.6.0
2. **Azure CLI** instalado y autenticado
3. **Cuenta de Azure** con permisos para crear recursos
4. **Storage Account para tfstate** (ver configuración inicial)

## 📁 Estructura del Proyecto

```
terraform/
├── main.tf                 # Orquestador principal
├── variables.tf            # Variables de entrada
├── outputs.tf              # Outputs globales
├── providers.tf            # Configuración de providers
├── versions.tf             # Versiones de Terraform
├── locals.tf               # Variables locales/computadas
├── README.md               # Este archivo
├── .gitignore              # Archivos a ignorar
│
├── environments/           # Configuraciones por entorno
│   ├── dev/
│   │   ├── backend.tf      # Backend config para dev
│   │   └── terraform.tfvars
│   ├── staging/
│   │   ├── backend.tf
│   │   └── terraform.tfvars
│   └── prod/
│       ├── backend.tf
│       └── terraform.tfvars
│
└── modules/                # Módulos reutilizables
    ├── resource-group/
    ├── sql-server/
    ├── storage-account/
    ├── container-registry/
    └── container-app/
```

## ⚙️ Configuración Inicial

### 1. Crear Storage Account para el tfstate

Antes de usar Terraform, necesitas un Storage Account para almacenar el estado remoto:

```bash
# Login en Azure
az login

# Crear Resource Group para tfstate
az group create \
  --name rg-lirium-tfstate \
  --location "East US 2"

# Crear Storage Account
az storage account create \
  --name stliriumtfstate \
  --resource-group rg-lirium-tfstate \
  --location "East US 2" \
  --sku Standard_LRS \
  --encryption-services blob

# Crear Container
az storage container create \
  --name tfstate \
  --account-name stliriumtfstate
```

### 2. Crear Service Principal para GitHub Actions

```bash
# Crear Service Principal
az ad sp create-for-rbac \
  --name "sp-lirium-terraform" \
  --role contributor \
  --scopes /subscriptions/{subscription-id} \
  --sdk-auth

# Guardar el output JSON como secret en GitHub
```

### 3. Configurar Secrets en GitHub

Agrega los siguientes secrets en tu repositorio de GitHub:

| Secret | Descripción |
|--------|-------------|
| `AZURE_CLIENT_ID` | ID del Service Principal |
| `AZURE_CLIENT_SECRET` | Secret del Service Principal |
| `AZURE_SUBSCRIPTION_ID` | ID de la suscripción de Azure |
| `AZURE_TENANT_ID` | ID del tenant de Azure |
| `SQL_ADMIN_PASSWORD` | Contraseña del admin de SQL Server |
| `SENDGRID_API_KEY` | API Key de SendGrid |
| `OPENROUTER_API_KEY` | API Key de OpenRouter |
| `FIREBASE_CREDENTIALS_JSON` | JSON de credenciales de Firebase |
| `GOOGLE_ANALYTICS_CREDENTIALS_JSON` | JSON de credenciales de GA |
| `PAYPAL_CLIENT_SECRET` | Secret de PayPal |
| `JWT_SECRET` | Secret para JWT |

## 🚀 Uso

### Usando GitHub Actions (Recomendado)

1. Ve a **Actions** en tu repositorio
2. Selecciona **🏗️ Terraform Infrastructure**
3. Click en **Run workflow**
4. Selecciona:
   - **Environment**: dev, staging o prod
   - **Action**: plan, apply o destroy
   - **Auto-approve**: Solo para dev

### Uso Local

```bash
# Ir al directorio de terraform
cd terraform

# Copiar configuración del entorno
cp environments/dev/backend.tf .

# Inicializar Terraform
terraform init

# Ver el plan
terraform plan -var-file="environments/dev/terraform.tfvars"

# Aplicar cambios
terraform apply -var-file="environments/dev/terraform.tfvars"

# Destruir infraestructura
terraform destroy -var-file="environments/dev/terraform.tfvars"
```

## 🌍 Entornos

| Entorno | Propósito | SQL SKU | ACR SKU | Container App |
|---------|-----------|---------|---------|---------------|
| **dev** | Desarrollo | Basic | Basic | 0.5 CPU, 1Gi, 0-2 réplicas |
| **staging** | QA/Testing | S0 | Standard | 0.5 CPU, 1Gi, 1-2 réplicas |
| **prod** | Producción | S1 | Standard | 1 CPU, 2Gi, 1-5 réplicas |

## 🔐 Secretos

Los secretos nunca se almacenan en los archivos `.tfvars`. Se pasan como variables de entorno:

```bash
# Para uso local
export TF_VAR_sql_admin_password="tu-password"
export TF_VAR_sendgrid_api_key="tu-api-key"
# ... etc

# Luego ejecutar terraform
terraform plan -var-file="environments/dev/terraform.tfvars"
```

## 🔧 Troubleshooting

### Error: "Backend configuration changed"

```bash
terraform init -reconfigure
```

### Error: "State lock"

```bash
terraform force-unlock <lock-id>
```

### Error: "Resource already exists"

Importar el recurso existente:
```bash
terraform import module.resource_group.azurerm_resource_group.this /subscriptions/{sub-id}/resourceGroups/{rg-name}
```

### Revisar el estado actual

```bash
terraform state list
terraform state show <resource>
```

## 📊 Outputs

Después de aplicar, puedes ver los outputs:

```bash
terraform output

# Output específico
terraform output container_app_url
terraform output acr_login_server
```

## 🔄 CI/CD Integration

El workflow de Terraform se integra con el deployment del backend:

1. **Terraform Apply** → Crea/actualiza infraestructura
2. **Deploy Backend** → Construye y despliega la aplicación

Flujo recomendado:
1. Ejecutar Terraform para crear infraestructura
2. Ejecutar Deploy Backend para desplegar la aplicación

## 📚 Referencias

- [Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Azure Container Apps](https://learn.microsoft.com/azure/container-apps/)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)
