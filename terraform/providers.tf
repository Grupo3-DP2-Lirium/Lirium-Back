# ============================================================================
# AZURE PROVIDER CONFIGURATION
# ============================================================================
# Configura el provider de Azure Resource Manager
# ============================================================================

provider "azurerm" {
  features {
    resource_group {
      prevent_deletion_if_contains_resources = false
    }
    key_vault {
      purge_soft_delete_on_destroy = true
    }
  }

  # Subscription se obtiene de:
  # 1. Variable de entorno ARM_SUBSCRIPTION_ID
  # 2. Azure CLI login
  # 3. Service Principal credentials
}

provider "random" {}
