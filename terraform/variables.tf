# ============================================================================
# GLOBAL VARIABLES
# ============================================================================
# Variables principales para configurar la infraestructura de Lirium
# ============================================================================

# ------------------------------------------------------------------------------
# GENERAL
# ------------------------------------------------------------------------------

variable "project_name" {
  description = "Nombre del proyecto (usado en nombres de recursos)"
  type        = string
  default     = "lirium"

  validation {
    condition     = can(regex("^[a-z0-9]{3,12}$", var.project_name))
    error_message = "Project name must be 3-12 lowercase alphanumeric characters."
  }
}

variable "environment" {
  description = "Entorno de despliegue (dev, staging, prod)"
  type        = string

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod."
  }
}

variable "location" {
  description = "Región de Azure para los recursos"
  type        = string
  default     = "East US 2"
}

variable "tags" {
  description = "Tags comunes para todos los recursos"
  type        = map(string)
  default     = {}
}

# ------------------------------------------------------------------------------
# SQL SERVER
# ------------------------------------------------------------------------------

variable "sql_admin_username" {
  description = "Usuario administrador de SQL Server"
  type        = string
  default     = "liriumadmin"

  validation {
    condition     = can(regex("^[a-zA-Z][a-zA-Z0-9]{2,19}$", var.sql_admin_username))
    error_message = "SQL admin username must start with a letter and be 3-20 alphanumeric characters."
  }
}

variable "sql_admin_password" {
  description = "Contraseña del administrador de SQL Server"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.sql_admin_password) >= 12
    error_message = "SQL admin password must be at least 12 characters."
  }
}

variable "sql_database_sku" {
  description = "SKU de la base de datos SQL (Basic, S0, S1, S2, etc.)"
  type        = string
  default     = "Basic"

  validation {
    condition     = contains(["Basic", "S0", "S1", "S2", "S3", "P1", "P2"], var.sql_database_sku)
    error_message = "SQL SKU must be one of: Basic, S0, S1, S2, S3, P1, P2."
  }
}

variable "sql_database_max_size_gb" {
  description = "Tamaño máximo de la base de datos en GB"
  type        = number
  default     = 2

  validation {
    condition     = var.sql_database_max_size_gb >= 1 && var.sql_database_max_size_gb <= 1024
    error_message = "SQL database max size must be between 1 and 1024 GB."
  }
}

# ------------------------------------------------------------------------------
# STORAGE ACCOUNT
# ------------------------------------------------------------------------------

variable "storage_account_tier" {
  description = "Tier de la Storage Account (Standard, Premium)"
  type        = string
  default     = "Standard"

  validation {
    condition     = contains(["Standard", "Premium"], var.storage_account_tier)
    error_message = "Storage tier must be Standard or Premium."
  }
}

variable "storage_replication_type" {
  description = "Tipo de replicación (LRS, GRS, RAGRS, ZRS)"
  type        = string
  default     = "LRS"

  validation {
    condition     = contains(["LRS", "GRS", "RAGRS", "ZRS"], var.storage_replication_type)
    error_message = "Replication type must be one of: LRS, GRS, RAGRS, ZRS."
  }
}

variable "storage_container_name" {
  description = "Nombre del container de Blob Storage"
  type        = string
  default     = "lirium-files"
}

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY
# ------------------------------------------------------------------------------

variable "acr_sku" {
  description = "SKU del Azure Container Registry (Basic, Standard, Premium)"
  type        = string
  default     = "Basic"

  validation {
    condition     = contains(["Basic", "Standard", "Premium"], var.acr_sku)
    error_message = "ACR SKU must be one of: Basic, Standard, Premium."
  }
}

# ------------------------------------------------------------------------------
# CONTAINER APP
# ------------------------------------------------------------------------------

variable "container_app_cpu" {
  description = "CPU asignada al Container App (0.25, 0.5, 1.0, 2.0)"
  type        = number
  default     = 0.5

  validation {
    condition     = contains([0.25, 0.5, 1.0, 1.5, 2.0], var.container_app_cpu)
    error_message = "Container CPU must be one of: 0.25, 0.5, 1.0, 1.5, 2.0."
  }
}

variable "container_app_memory" {
  description = "Memoria asignada al Container App (0.5Gi, 1Gi, 2Gi, 4Gi)"
  type        = string
  default     = "1Gi"

  validation {
    condition     = contains(["0.5Gi", "1Gi", "2Gi", "3Gi", "4Gi"], var.container_app_memory)
    error_message = "Container memory must be one of: 0.5Gi, 1Gi, 2Gi, 3Gi, 4Gi."
  }
}

variable "container_app_min_replicas" {
  description = "Número mínimo de réplicas"
  type        = number
  default     = 0

  validation {
    condition     = var.container_app_min_replicas >= 0 && var.container_app_min_replicas <= 10
    error_message = "Min replicas must be between 0 and 10."
  }
}

variable "container_app_max_replicas" {
  description = "Número máximo de réplicas"
  type        = number
  default     = 3

  validation {
    condition     = var.container_app_max_replicas >= 1 && var.container_app_max_replicas <= 30
    error_message = "Max replicas must be between 1 and 30."
  }
}

# ------------------------------------------------------------------------------
# APPLICATION SECRETS (sensibles)
# ------------------------------------------------------------------------------

variable "jwt_secret" {
  description = "Secreto para JWT tokens"
  type        = string
  sensitive   = true
  default     = ""
}

variable "sendgrid_api_key" {
  description = "API Key de SendGrid para emails"
  type        = string
  sensitive   = true
  default     = ""
}

variable "openrouter_api_key" {
  description = "API Key de OpenRouter para AI"
  type        = string
  sensitive   = true
  default     = ""
}

variable "firebase_credentials_json" {
  description = "Credenciales de Firebase en JSON (una línea)"
  type        = string
  sensitive   = true
  default     = ""
}

variable "google_analytics_credentials_json" {
  description = "Credenciales de Google Analytics en JSON"
  type        = string
  sensitive   = true
  default     = ""
}

variable "paypal_client_id" {
  description = "PayPal Client ID"
  type        = string
  default     = ""
}

variable "paypal_client_secret" {
  description = "PayPal Client Secret"
  type        = string
  sensitive   = true
  default     = ""
}
