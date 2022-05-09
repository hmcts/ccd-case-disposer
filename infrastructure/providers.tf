provider "azurerm" {
  features {}
}

provider "azurerm" {
  alias           = "aks-preview"
  subscription_id = var.aks_preview_subscription_id
  features {}
}
