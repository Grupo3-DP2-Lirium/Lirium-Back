# ============================================================================
# TERRAFORM BACKEND - STAGING ENVIRONMENT
# ============================================================================
# Configuración del backend remoto para almacenar el estado de Terraform
# ============================================================================

terraform {
  backend "azurerm" {
    resource_group_name  = "rg-lirium-tfstate"
    storage_account_name = "stliriumtfstate"
    container_name       = "tfstate"
    key                  = "staging.terraform.tfstate"
  }
}
