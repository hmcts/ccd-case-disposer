locals {
  // Staging DB used as a data-store-api DB for functional testts in preview.
  app_full_name  = "${var.product}-data-store-api-staging"

  // Preview Only
  is_aat     = "${(var.env == "aat" || var.env == "saat")}"
  instance_count = "${local.is_aat ? 1 : 0}"

  // Vault name
  vaultName      = "${var.raw_product}-${var.env}"

  // Shared Resource Group
  sharedResourceGroup = "${var.raw_product}-shared-${var.env}"
}

data "azurerm_key_vault" "ccd_shared_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.sharedResourceGroup}"
}

////////////////////////////////
// DB version 11              //
////////////////////////////////

module "data-store-staging-db-v11" {
  count           = "${local.instance_count}"
  source          = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product         = var.product
  component       = var.component
  name            = "${local.app_full_name}-postgres-db-v11"
  location        = "${var.location}"
  env             = "${var.env}"
  subscription    = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name   = "${var.database_name}"
  postgresql_version = "11"
  sku_name        = "${var.database_sku_name}"
  sku_tier        = "GeneralPurpose"
  sku_capacity    = "${var.database_sku_capacity}"
  storage_mb      = "${var.database_storage_mb}"
  common_tags     = "${var.common_tags}"
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  count        = local.instance_count
  name         = "${var.component}-POSTGRES-USER"
  value        = module.data-store-staging-db-v11[0].user_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  count        = local.instance_count
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.data-store-staging-db-v11[0].postgresql_password
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  count        = local.instance_count
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.data-store-staging-db-v11[0].host_name
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  count        = local.instance_count
  name         = "${var.component}-POSTGRES-PORT"
  value        = module.data-store-staging-db-v11[0].postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  count        = local.instance_count
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = module.data-store-staging-db-v11[0].postgresql_database
  key_vault_id = data.azurerm_key_vault.ccd_shared_key_vault.id
}

////////////////////////////////
// Network access to allow from Preview
////////////////////////////////

data "azurerm_virtual_network" "aks_preview_vnet" {
  provider            = azurerm.aks-preview
  name                = "cft-preview-vnet"
  resource_group_name = "cft-preview-network-rg"
}

data "azurerm_subnet" "aks-00-preview" {
  provider             = azurerm.aks-preview
  name                 = "aks-00"
  virtual_network_name = data.azurerm_virtual_network.aks_preview_vnet.name
  resource_group_name  = data.azurerm_virtual_network.aks_preview_vnet.resource_group_name
}

data "azurerm_subnet" "aks-01-preview" {
  provider             = azurerm.aks-preview
  name                 = "aks-01"
  virtual_network_name = data.azurerm_virtual_network.aks_preview_vnet.name
  resource_group_name  = data.azurerm_virtual_network.aks_preview_vnet.resource_group_name
}
