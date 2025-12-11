# ============================================================================
# LOCAL VALUES
# ============================================================================
# Variables calculadas y convenciones de nombres
# ============================================================================

locals {
  # Prefijo base para nombres de recursos
  name_prefix = "${var.project_name}-${var.environment}"
  
  # Nombres de recursos (siguiendo convenciones de Azure)
  resource_group_name     = "rg-${local.name_prefix}"
  sql_server_name         = "sql-${local.name_prefix}"
  sql_database_name       = "${var.project_name}"
  storage_account_name    = "st${var.project_name}${var.environment}"  # Sin guiones, máx 24 chars
  acr_name                = "acr${var.project_name}${var.environment}" # Sin guiones
  container_env_name      = "cae-${local.name_prefix}"
  container_app_name      = "ca-${local.name_prefix}-backend"
  
  # Tags comunes para todos los recursos
  common_tags = merge(var.tags, {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
    Repository  = "Grupo3-DP2-Lirium/Lirium-Back"
  })

  # URLs base
  app_share_base_url = var.environment == "prod" ? "https://lirium-front-web.vercel.app" : "http://localhost:3000"
  
  # Configuración de PayPal según entorno
  paypal_base_url = var.environment == "prod" ? "https://api.paypal.com" : "https://api.sandbox.paypal.com"
}
