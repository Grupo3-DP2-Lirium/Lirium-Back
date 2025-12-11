# ============================================================================
# TERRAFORM VARIABLES - DEV ENVIRONMENT
# ============================================================================
# Configuración específica para el entorno de desarrollo
# ============================================================================

# ------------------------------------------------------------------------------
# GENERAL
# ------------------------------------------------------------------------------

project_name = "lirium"
environment  = "dev"
location     = "East US 2"

tags = {
  Project     = "Lirium"
  Environment = "Development"
  ManagedBy   = "Terraform"
  CostCenter  = "Development"
}

# ------------------------------------------------------------------------------
# SQL SERVER (configuración económica para dev)
# ------------------------------------------------------------------------------

sql_admin_username   = "liriumadmin"
# sql_admin_password se pasa como variable de entorno TF_VAR_sql_admin_password
sql_database_sku     = "Basic"
sql_database_max_size_gb = 2

# ------------------------------------------------------------------------------
# STORAGE ACCOUNT (configuración básica para dev)
# ------------------------------------------------------------------------------

storage_account_tier     = "Standard"
storage_replication_type = "LRS"  # Locally redundant (más económico)
storage_container_name   = "lirium-files"

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY (básico para dev)
# ------------------------------------------------------------------------------

acr_sku = "Basic"

# ------------------------------------------------------------------------------
# CONTAINER APP (recursos mínimos para dev)
# ------------------------------------------------------------------------------

container_app_cpu          = 0.5
container_app_memory       = "1Gi"
container_app_min_replicas = 0   # Puede escalar a 0 en dev
container_app_max_replicas = 2

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
