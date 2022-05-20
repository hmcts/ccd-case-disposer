#!/usr/bin/env bash
# Generates ServiceAuthorization header
#

set -eu

curl --insecure --fail --show-error --silent -X POST \
  ${IDAM_S2S_URL:-http://localhost:4502}/testing-support/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'ccd_case_disposer'"
  }'
