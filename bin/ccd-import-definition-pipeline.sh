#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
filepath=${1}
filename=$(basename ${filepath})DISPOSER_Master_Definition.xlsx
uploadFilename="DISPOSER_Master_Definition.xlsx"
echo filepath =$filepath

userToken=$(${dir}/idam-lease-user-token.sh ${DEFINITION_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${DEFINITION_IMPORTER_PASSWORD:-Password12})
serviceToken=$(${dir}/idam-lease-service-token.sh)

echo DEFINITION_STORE_HOST = $DEFINITION_STORE_HOST
uploadResponse=$(curl --insecure --silent -w "\n%{http_code}" --show-error -X POST \
  ${DEFINITION_STORE_HOST:-http://localhost:4451}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '504' ]]; then
  for try in {1..20}
  do
    sleep 5
    echo "Checking status of ${filename} (${uploadFilename}) upload (Try ${try})"
    audit_response=$(curl --insecure --silent --show-error -X GET \
      ${DEFINITION_STORE_HOST:-http://localhost:4451}/api/import-audits \
      -H "Authorization: Bearer ${userToken}" \
      -H "ServiceAuthorization: Bearer ${serviceToken}")

    if [[ ${audit_response} == *"${uploadFilename}"* ]]; then
      echo "${filename} (${uploadFilename}) uploaded"
      exit 0
    fi
  done
else
  if [[ "${upload_response_content}" == 'Case Definition data successfully imported' ]]; then
    echo "${filename} (${uploadFilename}) uploaded"
    exit 0
  fi
fi

echo "${filename} (${uploadFilename}) upload failed (${upload_response_content})"
exit 1;
