# ============================================================================
# CONTAINER APP MODULE
# ============================================================================
# Crea Azure Container App Environment y Container App
# ============================================================================

variable "environment_name" {
  description = "Nombre del Container App Environment"
  type        = string
}

variable "app_name" {
  description = "Nombre del Container App"
  type        = string
}

variable "resource_group_name" {
  description = "Nombre del Resource Group"
  type        = string
}

variable "location" {
  description = "Ubicación de Azure"
  type        = string
}

variable "cpu" {
  description = "CPU asignada"
  type        = number
  default     = 0.5
}

variable "memory" {
  description = "Memoria asignada"
  type        = string
  default     = "1Gi"
}

variable "min_replicas" {
  description = "Réplicas mínimas"
  type        = number
  default     = 0
}

variable "max_replicas" {
  description = "Réplicas máximas"
  type        = number
  default     = 3
}

variable "acr_login_server" {
  description = "URL del Azure Container Registry"
  type        = string
}

variable "acr_username" {
  description = "Usuario del ACR"
  type        = string
}

variable "acr_password" {
  description = "Contraseña del ACR"
  type        = string
  sensitive   = true
}

variable "image_name" {
  description = "Nombre de la imagen Docker"
  type        = string
  default     = "lirium-backend"
}

variable "image_tag" {
  description = "Tag de la imagen Docker"
  type        = string
  default     = "latest"
}

variable "env_vars" {
  description = "Variables de entorno para la aplicación"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "secret_env_vars" {
  description = "Variables de entorno secretas"
  type = list(object({
    name         = string
    secret_name  = string
    secret_value = string
  }))
  default   = []
  sensitive = true
}

variable "tags" {
  description = "Tags para los recursos"
  type        = map(string)
  default     = {}
}

# ------------------------------------------------------------------------------
# CONTAINER APP ENVIRONMENT
# ------------------------------------------------------------------------------

resource "azurerm_container_app_environment" "this" {
  name                = var.environment_name
  resource_group_name = var.resource_group_name
  location            = var.location

  tags = var.tags
}

# ------------------------------------------------------------------------------
# CONTAINER APP
# ------------------------------------------------------------------------------

resource "azurerm_container_app" "this" {
  name                         = var.app_name
  container_app_environment_id = azurerm_container_app_environment.this.id
  resource_group_name          = var.resource_group_name
  revision_mode                = "Single"

  # Secretos (ACR credentials + app secrets)
  secret {
    name  = "acr-password"
    value = var.acr_password
  }

  dynamic "secret" {
    for_each = var.secret_env_vars
    content {
      name  = secret.value.secret_name
      value = secret.value.secret_value
    }
  }

  # Registry configuration
  registry {
    server               = var.acr_login_server
    username             = var.acr_username
    password_secret_name = "acr-password"
  }

  # Ingress configuration
  ingress {
    external_enabled = true
    target_port      = 8080
    transport        = "auto"

    traffic_weight {
      percentage      = 100
      latest_revision = true
    }
  }

  # Container template
  template {
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas

    container {
      name   = "backend"
      image  = "${var.acr_login_server}/${var.image_name}:${var.image_tag}"
      cpu    = var.cpu
      memory = var.memory

      # Variables de entorno normales
      dynamic "env" {
        for_each = var.env_vars
        content {
          name  = env.value.name
          value = env.value.value
        }
      }

      # Variables de entorno desde secretos
      dynamic "env" {
        for_each = var.secret_env_vars
        content {
          name        = env.value.name
          secret_name = env.value.secret_name
        }
      }

      # Liveness probe
      liveness_probe {
        path             = "/swagger-ui/index.html"
        port             = 8080
        transport        = "HTTP"
        initial_delay    = 60
        interval_seconds = 30
        timeout          = 5
        failure_count_threshold = 3
      }

      # Readiness probe
      readiness_probe {
        path             = "/swagger-ui/index.html"
        port             = 8080
        transport        = "HTTP"
        interval_seconds = 10
        timeout          = 5
        failure_count_threshold = 3
      }
    }
  }

  tags = var.tags
}

# ------------------------------------------------------------------------------
# OUTPUTS
# ------------------------------------------------------------------------------

output "environment_id" {
  description = "ID del Container App Environment"
  value       = azurerm_container_app_environment.this.id
}

output "environment_name" {
  description = "Nombre del Container App Environment"
  value       = azurerm_container_app_environment.this.name
}

output "app_id" {
  description = "ID del Container App"
  value       = azurerm_container_app.this.id
}

output "app_name" {
  description = "Nombre del Container App"
  value       = azurerm_container_app.this.name
}

output "app_fqdn" {
  description = "FQDN del Container App"
  value       = azurerm_container_app.this.latest_revision_fqdn
}

output "app_url" {
  description = "URL completa del Container App"
  value       = "https://${azurerm_container_app.this.ingress[0].fqdn}"
}
