#!/usr/bin/env bash

set -eu

pr=${1}

echo 'export ENVIRONMENT=preview'

# urls
echo "export IDAM_S2S_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
echo "export IDAM_API_URL=https://idam-api.aat.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
echo "export DEFINITION_STORE_HOST=https://ccd-definition-store-ccd-case-disposer-pr-${pr}.service.core-compute-preview.internal"
