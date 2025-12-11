# ============================================================================
# TERRAFORM VARIABLES - STAGING ENVIRONMENT
# ============================================================================
# Configuración específica para el entorno de staging/QA
# ============================================================================

# ------------------------------------------------------------------------------
# GENERAL
# ------------------------------------------------------------------------------

project_name = "lirium"
environment  = "staging"
location     = "East US 2"

tags = {
  Project     = "Lirium"
  Environment = "Staging"
  ManagedBy   = "Terraform"
  CostCenter  = "QA"
}

# ------------------------------------------------------------------------------
# SQL SERVER (configuración intermedia para staging)
# ------------------------------------------------------------------------------

sql_admin_username   = "liriumadmin"
# sql_admin_password se pasa como variable de entorno TF_VAR_sql_admin_password
sql_database_sku     = "S0"  # Standard tier para pruebas realistas
sql_database_max_size_gb = 10

# ------------------------------------------------------------------------------
# STORAGE ACCOUNT
# ------------------------------------------------------------------------------

storage_account_tier     = "Standard"
storage_replication_type = "LRS"
storage_container_name   = "lirium-files"

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY
# ------------------------------------------------------------------------------

acr_sku = "Standard"  # Mejor rendimiento que Basic

# ------------------------------------------------------------------------------
# CONTAINER APP (similar a prod pero menos réplicas)
# ------------------------------------------------------------------------------

container_app_cpu          = 0.5
container_app_memory       = "1Gi"
container_app_min_replicas = 1   # Siempre al menos 1 réplica
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
