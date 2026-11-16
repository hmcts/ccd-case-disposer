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

curl -q -k -sS \
  -o /dev/null \
  -w 'HTTP: %{http_code}\nRemote IP: %{remote_ip}\n' \
  "${DEFINITION_STORE_HOST}/import"

echo "Exit code: $?"

echo "========== CCD IMPORT DEFINITION DEBUG =========="
echo "filepath: [${filepath}]"
echo "uploadFilename: [${uploadFilename}]"

echo "userToken set: $([ -n "${userToken:-}" ] && echo yes || echo no)"
echo "userToken length: ${#userToken}"
echo "serviceToken set: $([ -n "${serviceToken:-}" ] && echo yes || echo no)"
echo "serviceToken length: ${#serviceToken}"

echo "================================================="

set -x
uploadResponse=$(curl -q --insecure --silent -w "\n%{http_code}" --show-error -X POST \
  ${DEFINITION_STORE_HOST:-http://localhost:4451}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}")
set +x
upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

echo "upload_http_code - ${upload_http_code}"
echo "upload_response_content - ${upload_response_content}"

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
