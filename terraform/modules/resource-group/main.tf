# ============================================================================
# RESOURCE GROUP MODULE
# ============================================================================
# Crea el Resource Group que contendrá todos los recursos
# ============================================================================

variable "name" {
  description = "Nombre del Resource Group"
  type        = string
}

variable "location" {
  description = "Ubicación/Región de Azure"
  type        = string
}

variable "tags" {
  description = "Tags para el Resource Group"
  type        = map(string)
  default     = {}
}

# ------------------------------------------------------------------------------
# RESOURCE
# ------------------------------------------------------------------------------

resource "azurerm_resource_group" "this" {
  name     = var.name
  location = var.location
  tags     = var.tags
}

# ------------------------------------------------------------------------------
# OUTPUTS
# ------------------------------------------------------------------------------

output "id" {
  description = "ID del Resource Group"
  value       = azurerm_resource_group.this.id
}

output "name" {
  description = "Nombre del Resource Group"
  value       = azurerm_resource_group.this.name
}

output "location" {
  description = "Ubicación del Resource Group"
  value       = azurerm_resource_group.this.location
}
