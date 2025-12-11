# ============================================================================
# TERRAFORM VARIABLES - PROD ENVIRONMENT
# ============================================================================
# Configuración específica para el entorno de producción
# ============================================================================

# ------------------------------------------------------------------------------
# GENERAL
# ------------------------------------------------------------------------------

project_name = "lirium"
environment  = "prod"
location     = "East US 2"

tags = {
  Project     = "Lirium"
  Environment = "Production"
  ManagedBy   = "Terraform"
  CostCenter  = "Production"
  Critical    = "true"
}

# ------------------------------------------------------------------------------
# SQL SERVER (configuración robusta para producción)
# ------------------------------------------------------------------------------

sql_admin_username   = "liriumadmin"
# sql_admin_password se pasa como variable de entorno TF_VAR_sql_admin_password
sql_database_sku     = "S1"  # Standard S1 para producción
sql_database_max_size_gb = 50

# ------------------------------------------------------------------------------
# STORAGE ACCOUNT (con redundancia geográfica para prod)
# ------------------------------------------------------------------------------

storage_account_tier     = "Standard"
storage_replication_type = "GRS"  # Geo-redundant para producción
storage_container_name   = "lirium-files"

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY (Premium para mejor rendimiento)
# ------------------------------------------------------------------------------

acr_sku = "Standard"

# ------------------------------------------------------------------------------
# CONTAINER APP (recursos de producción)
# ------------------------------------------------------------------------------

container_app_cpu          = 1.0
container_app_memory       = "2Gi"
container_app_min_replicas = 1   # Siempre al menos 1 réplica activa
container_app_max_replicas = 5   # Auto-scale hasta 5 réplicas

# ------------------------------------------------------------------------------
# APPLICATION CONFIG
# ------------------------------------------------------------------------------

paypal_client_id = "AYhko78F1KkWLhQreC9ht18i-Jn6CxjrRpbyr3c_x2OgzEo82mhxNLIg-7pWB4G2bpkZYvTUD9LrbgVL"

# Los secretos se pasan como variables de entorno:
# TF_VAR_sql_admin_password
# TF_VAR_sendgrid_api_key
# TF_VAR_openrouter_api_key
# TF_VAR_firebase_credentials_json
# TF_VAR_google_analytics_credentials_json
# TF_VAR_paypal_client_secret
# TF_VAR_jwt_secret
