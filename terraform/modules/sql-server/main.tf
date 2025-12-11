# ============================================================================
# SQL SERVER MODULE
# ============================================================================
# Crea Azure SQL Server y Database
# ============================================================================

variable "name" {
  description = "Nombre del SQL Server"
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

variable "admin_username" {
  description = "Usuario administrador"
  type        = string
}

variable "admin_password" {
  description = "Contraseña del administrador"
  type        = string
  sensitive   = true
}

variable "database_name" {
  description = "Nombre de la base de datos"
  type        = string
}

variable "database_sku" {
  description = "SKU de la base de datos"
  type        = string
  default     = "Basic"
}

variable "database_max_size_gb" {
  description = "Tamaño máximo de la BD en GB"
  type        = number
  default     = 2
}

variable "tags" {
  description = "Tags para los recursos"
  type        = map(string)
  default     = {}
}

# ------------------------------------------------------------------------------
# SQL SERVER
# ------------------------------------------------------------------------------

resource "azurerm_mssql_server" "this" {
  name                         = var.name
  resource_group_name          = var.resource_group_name
  location                     = var.location
  version                      = "12.0"
  administrator_login          = var.admin_username
  administrator_login_password = var.admin_password
  minimum_tls_version          = "1.2"

  tags = var.tags
}

# ------------------------------------------------------------------------------
# SQL DATABASE
# ------------------------------------------------------------------------------

resource "azurerm_mssql_database" "this" {
  name         = var.database_name
  server_id    = azurerm_mssql_server.this.id
  collation    = "SQL_Latin1_General_CP1_CI_AS"
  license_type = "LicenseIncluded"
  sku_name     = var.database_sku
  max_size_gb  = var.database_max_size_gb

  tags = var.tags

  lifecycle {
    prevent_destroy = false
  }
}

# ------------------------------------------------------------------------------
# FIREWALL RULES
# ------------------------------------------------------------------------------

# Permitir servicios de Azure
resource "azurerm_mssql_firewall_rule" "azure_services" {
  name             = "AllowAzureServices"
  server_id        = azurerm_mssql_server.this.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# ------------------------------------------------------------------------------
# OUTPUTS
# ------------------------------------------------------------------------------

output "server_id" {
  description = "ID del SQL Server"
  value       = azurerm_mssql_server.this.id
}

output "server_name" {
  description = "Nombre del SQL Server"
  value       = azurerm_mssql_server.this.name
}

output "server_fqdn" {
  description = "FQDN del SQL Server"
  value       = azurerm_mssql_server.this.fully_qualified_domain_name
}

output "database_id" {
  description = "ID de la base de datos"
  value       = azurerm_mssql_database.this.id
}

output "database_name" {
  description = "Nombre de la base de datos"
  value       = azurerm_mssql_database.this.name
}

output "connection_string" {
  description = "Connection string JDBC para Spring Boot"
  value       = "jdbc:sqlserver://${azurerm_mssql_server.this.fully_qualified_domain_name}:1433;database=${azurerm_mssql_database.this.name};encrypt=true;trustServerCertificate=false;loginTimeout=30"
  sensitive   = true
}
