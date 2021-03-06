#!/bin/bash
## Usage: ./ccd-add-role.sh role [classification]
##
## Options:
##    - role: Name of the role. Must be an existing IDAM role.
##    - classification: Classification granted to the role; one of `PUBLIC`,
##        `PRIVATE` or `RESTRICTED`. Default to `PUBLIC`.
##
## Add support for an IDAM role in CCD.

role=$1
classification=${2:-PUBLIC}

if [ -z "$role" ]
  then
    echo "Usage: ./ccd-add-role.sh role [classification]"
    exit 1
fi

case $classification in
  PUBLIC|PRIVATE|RESTRICTED)
    ;;
  *)
    echo "Classification must be one of: PUBLIC, PRIVATE or RESTRICTED"
    exit 1 ;;
esac

binFolder=$(dirname "$0")

userToken=$(${binFolder}/idam-lease-user-token.sh "${DEFINITION_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net}" "${DEFINITION_IMPORTER_PASSWORD:-Pa55word11}")

serviceToken=$(${binFolder}/idam-lease-service-token.sh)
ccdUrl=${DEFINITION_STORE_HOST:-http://localhost:4451}

curl --insecure --fail --show-error --silent --output /dev/null -X PUT \
  ${DEFINITION_STORE_HOST:-http://localhost:4451}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "'${classification}'"
  }'
