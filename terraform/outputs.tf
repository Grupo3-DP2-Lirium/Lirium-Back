# ============================================================================
# GLOBAL OUTPUTS
# ============================================================================
# Outputs principales de la infraestructura
# ============================================================================

# ------------------------------------------------------------------------------
# RESOURCE GROUP
# ------------------------------------------------------------------------------

output "resource_group_name" {
  description = "Nombre del Resource Group creado"
  value       = module.resource_group.name
}

output "resource_group_id" {
  description = "ID del Resource Group"
  value       = module.resource_group.id
}

# ------------------------------------------------------------------------------
# SQL SERVER
# ------------------------------------------------------------------------------

output "sql_server_fqdn" {
  description = "FQDN del SQL Server"
  value       = module.sql_server.server_fqdn
}

output "sql_database_name" {
  description = "Nombre de la base de datos"
  value       = module.sql_server.database_name
}

output "sql_connection_string" {
  description = "Connection string JDBC (sin password)"
  value       = "jdbc:sqlserver://${module.sql_server.server_fqdn}:1433;database=${module.sql_server.database_name};encrypt=true;trustServerCertificate=false;loginTimeout=30"
}

# ------------------------------------------------------------------------------
# STORAGE
# ------------------------------------------------------------------------------

output "storage_account_name" {
  description = "Nombre del Storage Account"
  value       = module.storage_account.name
}

output "storage_blob_endpoint" {
  description = "URL del endpoint de blobs"
  value       = module.storage_account.primary_blob_endpoint
}

output "storage_container_name" {
  description = "Nombre del container de blobs"
  value       = module.storage_account.container_name
}

output "storage_connection_string" {
  description = "Connection string del Storage Account"
  value       = module.storage_account.primary_connection_string
  sensitive   = true
}

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY
# ------------------------------------------------------------------------------

output "acr_login_server" {
  description = "URL del Azure Container Registry"
  value       = module.container_registry.login_server
}

output "acr_admin_username" {
  description = "Usuario admin del ACR"
  value       = module.container_registry.admin_username
}

output "acr_admin_password" {
  description = "Contraseña admin del ACR"
  value       = module.container_registry.admin_password
  sensitive   = true
}

# ------------------------------------------------------------------------------
# CONTAINER APP
# ------------------------------------------------------------------------------

output "container_app_name" {
  description = "Nombre del Container App"
  value       = module.container_app.app_name
}

output "container_app_url" {
  description = "URL pública del Container App"
  value       = module.container_app.app_url
}

output "container_app_fqdn" {
  description = "FQDN del Container App"
  value       = module.container_app.app_fqdn
}

# ------------------------------------------------------------------------------
# SUMMARY (para fácil referencia)
# ------------------------------------------------------------------------------

output "deployment_summary" {
  description = "Resumen del deployment"
  value = {
    environment           = var.environment
    location              = var.location
    resource_group        = module.resource_group.name
    api_url               = module.container_app.app_url
    acr_server            = module.container_registry.login_server
    sql_server            = module.sql_server.server_fqdn
    storage_account       = module.storage_account.name
  }
}
