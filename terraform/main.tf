# ============================================================================
# MAIN TERRAFORM CONFIGURATION
# ============================================================================
# Orquestador principal que invoca todos los módulos
# ============================================================================

# ------------------------------------------------------------------------------
# RESOURCE GROUP
# ------------------------------------------------------------------------------

module "resource_group" {
  source = "./modules/resource-group"

  name     = local.resource_group_name
  location = var.location
  tags     = local.common_tags
}

# ------------------------------------------------------------------------------
# SQL SERVER & DATABASE
# ------------------------------------------------------------------------------

module "sql_server" {
  source = "./modules/sql-server"

  name                 = local.sql_server_name
  resource_group_name  = module.resource_group.name
  location             = module.resource_group.location
  admin_username       = var.sql_admin_username
  admin_password       = var.sql_admin_password
  database_name        = local.sql_database_name
  database_sku         = var.sql_database_sku
  database_max_size_gb = var.sql_database_max_size_gb
  tags                 = local.common_tags

  depends_on = [module.resource_group]
}

# ------------------------------------------------------------------------------
# STORAGE ACCOUNT
# ------------------------------------------------------------------------------

module "storage_account" {
  source = "./modules/storage-account"

  name                = local.storage_account_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
  account_tier        = var.storage_account_tier
  replication_type    = var.storage_replication_type
  container_name      = var.storage_container_name
  tags                = local.common_tags

  depends_on = [module.resource_group]
}

# ------------------------------------------------------------------------------
# CONTAINER REGISTRY
# ------------------------------------------------------------------------------

module "container_registry" {
  source = "./modules/container-registry"

  name                = local.acr_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
  sku                 = var.acr_sku
  admin_enabled       = true
  tags                = local.common_tags

  depends_on = [module.resource_group]
}

# ------------------------------------------------------------------------------
# CONTAINER APP
# ------------------------------------------------------------------------------

module "container_app" {
  source = "./modules/container-app"

  environment_name    = local.container_env_name
  app_name            = local.container_app_name
  resource_group_name = module.resource_group.name
  location            = module.resource_group.location
  cpu                 = var.container_app_cpu
  memory              = var.container_app_memory
  min_replicas        = var.container_app_min_replicas
  max_replicas        = var.container_app_max_replicas

  # ACR Configuration
  acr_login_server = module.container_registry.login_server
  acr_username     = module.container_registry.admin_username
  acr_password     = module.container_registry.admin_password
  image_name       = "lirium-backend"
  image_tag        = "latest"

  # Environment variables
  env_vars = [
    { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
    { name = "SERVER_PORT", value = "8080" },
    { name = "SPRING_DATASOURCE_URL", value = module.sql_server.connection_string },
    { name = "SPRING_DATASOURCE_USERNAME", value = var.sql_admin_username },
    { name = "AZURE_STORAGE_CONTAINER_NAME", value = var.storage_container_name },
    { name = "APP_STORAGE_PROVIDER", value = "azure" },
    { name = "APP_STORAGE_MAX_FILE_SIZE", value = "104857600" },
    { name = "APP_SHARE_BASE_URL", value = local.app_share_base_url },
    { name = "PAYPAL_BASE_URL", value = local.paypal_base_url },
    { name = "PAYPAL_CLIENT_ID", value = var.paypal_client_id },
    { name = "OPENROUTER_URL", value = "https://openrouter.ai/api/v1/chat/completions" },
    { name = "OPENROUTER_MODEL", value = "mistralai/mistral-7b-instruct" },
    { name = "FFMPEG_PATH", value = "/usr/bin/ffmpeg" },
    { name = "DOCUMENTARY_TEMP_PATH", value = "/tmp/documentaries" },
    { name = "DOCUMENTARY_FONTS_PATH", value = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf" },
    { name = "SENDGRID_FROM_EMAIL", value = "no-reply@vacapp.work" },
    { name = "SENDGRID_FROM_NAME", value = "Lirium" },
    { name = "SENDGRID_ENABLED", value = "true" },
    { name = "GOOGLE_ANALYTICS_PROPERTY_ID", value = "510032575" },
    { name = "GOOGLE_ANALYTICS_ENABLED", value = "true" },
  ]

  # Secret environment variables
  secret_env_vars = [
    { name = "SPRING_DATASOURCE_PASSWORD", secret_name = "db-password", secret_value = var.sql_admin_password },
    { name = "AZURE_STORAGE_CONNECTION_STRING", secret_name = "storage-connection", secret_value = module.storage_account.primary_connection_string },
    { name = "JWT_SECRET", secret_name = "jwt-secret", secret_value = var.jwt_secret != "" ? var.jwt_secret : "myVerySecretKeyForJwtTokenGenerationThatIsAtLeast32BytesLong" },
    { name = "SENDGRID_API_KEY", secret_name = "sendgrid-key", secret_value = var.sendgrid_api_key },
    { name = "OPENROUTER_API_KEY", secret_name = "openrouter-key", secret_value = var.openrouter_api_key },
    { name = "FIREBASE_CREDENTIALS_JSON", secret_name = "firebase-creds", secret_value = var.firebase_credentials_json },
    { name = "GOOGLE_ANALYTICS_CREDENTIALS_JSON", secret_name = "ga-creds", secret_value = var.google_analytics_credentials_json },
    { name = "PAYPAL_CLIENT_SECRET", secret_name = "paypal-secret", secret_value = var.paypal_client_secret },
  ]

  tags = local.common_tags

  depends_on = [
    module.resource_group,
    module.sql_server,
    module.storage_account,
    module.container_registry
  ]
}
