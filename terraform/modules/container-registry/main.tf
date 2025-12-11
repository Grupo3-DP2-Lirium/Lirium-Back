# ============================================================================
# CONTAINER REGISTRY MODULE
# ============================================================================
# Crea Azure Container Registry para imágenes Docker
# ============================================================================

variable "name" {
  description = "Nombre del ACR (sin guiones)"
  type        = string

  validation {
    condition     = can(regex("^[a-zA-Z0-9]{5,50}$", var.name))
    error_message = "ACR name must be 5-50 alphanumeric characters."
  }
}

variable "resource_group_name" {
  description = "Nombre del Resource Group"
  type        = string
}

variable "location" {
  description = "Ubicación de Azure"
  type        = string
}

variable "sku" {
  description = "SKU del ACR"
  type        = string
  default     = "Basic"
}

variable "admin_enabled" {
  description = "Habilitar usuario admin"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags para los recursos"
  type        = map(string)
  default     = {}
}

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY
# ------------------------------------------------------------------------------

resource "azurerm_container_registry" "this" {
  name                = var.name
  resource_group_name = var.resource_group_name
  location            = var.location
  sku                 = var.sku
  admin_enabled       = var.admin_enabled

  tags = var.tags
}

# ------------------------------------------------------------------------------
# OUTPUTS
# ------------------------------------------------------------------------------

output "id" {
  description = "ID del Container Registry"
  value       = azurerm_container_registry.this.id
}

output "name" {
  description = "Nombre del Container Registry"
  value       = azurerm_container_registry.this.name
}

output "login_server" {
  description = "URL del login server"
  value       = azurerm_container_registry.this.login_server
}

output "admin_username" {
  description = "Usuario admin"
  value       = azurerm_container_registry.this.admin_username
}

output "admin_password" {
  description = "Contraseña admin"
  value       = azurerm_container_registry.this.admin_password
  sensitive   = true
}
