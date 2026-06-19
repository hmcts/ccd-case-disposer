#!/usr/bin/env bash
# Generates Authorization header
#

set -eu

username=${1}
password=${2}
clientSecret=${API_GATEWAY_IDAM_SECRET:-ccd_gateway_secret}

curl --insecure --fail --show-error --silent \
  -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  "${IDAM_API_URL:-http://localhost:5000}/o/token" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "username=${username}" \
  --data-urlencode "password=${password}" \
  --data-urlencode "client_id=ccd_gateway" \
  --data-urlencode "client_secret=${clientSecret}" \
  --data-urlencode "scope=openid profile roles" \
  | docker run --rm --interactive hmctsprod.azurecr.io/imported/mikefarah/yq -r .access_token
