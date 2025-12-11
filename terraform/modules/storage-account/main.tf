# ============================================================================
# STORAGE ACCOUNT MODULE
# ============================================================================
# Crea Azure Storage Account y Blob Container
# ============================================================================

variable "name" {
  description = "Nombre del Storage Account (sin guiones, máx 24 chars)"
  type        = string

  validation {
    condition     = can(regex("^[a-z0-9]{3,24}$", var.name))
    error_message = "Storage account name must be 3-24 lowercase alphanumeric characters."
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

variable "account_tier" {
  description = "Tier del Storage Account"
  type        = string
  default     = "Standard"
}

variable "replication_type" {
  description = "Tipo de replicación"
  type        = string
  default     = "LRS"
}

variable "container_name" {
  description = "Nombre del blob container"
  type        = string
  default     = "lirium-files"
}

variable "tags" {
  description = "Tags para los recursos"
  type        = map(string)
  default     = {}
}

# ------------------------------------------------------------------------------
# STORAGE ACCOUNT
# ------------------------------------------------------------------------------

resource "azurerm_storage_account" "this" {
  name                     = var.name
  resource_group_name      = var.resource_group_name
  location                 = var.location
  account_tier             = var.account_tier
  account_replication_type = var.replication_type

  # Configuración de seguridad
  min_tls_version           = "TLS1_2"
  allow_nested_items_to_be_public = true  # Necesario para URLs públicas de archivos

  # Blob properties
  blob_properties {
    cors_rule {
      allowed_headers    = ["*"]
      allowed_methods    = ["GET", "HEAD", "POST", "PUT", "DELETE"]
      allowed_origins    = ["*"]
      exposed_headers    = ["*"]
      max_age_in_seconds = 3600
    }
  }

  tags = var.tags
}

# ------------------------------------------------------------------------------
# BLOB CONTAINER
# ------------------------------------------------------------------------------

resource "azurerm_storage_container" "this" {
  name                  = var.container_name
  storage_account_id    = azurerm_storage_account.this.id
  container_access_type = "blob"  # Acceso público a blobs
}

# ------------------------------------------------------------------------------
# OUTPUTS
# ------------------------------------------------------------------------------

output "id" {
  description = "ID del Storage Account"
  value       = azurerm_storage_account.this.id
}

output "name" {
  description = "Nombre del Storage Account"
  value       = azurerm_storage_account.this.name
}

output "primary_connection_string" {
  description = "Connection string primaria"
  value       = azurerm_storage_account.this.primary_connection_string
  sensitive   = true
}

output "primary_access_key" {
  description = "Access key primaria"
  value       = azurerm_storage_account.this.primary_access_key
  sensitive   = true
}

output "primary_blob_endpoint" {
  description = "URL del endpoint de blobs"
  value       = azurerm_storage_account.this.primary_blob_endpoint
}

output "container_name" {
  description = "Nombre del container creado"
  value       = azurerm_storage_container.this.name
}
