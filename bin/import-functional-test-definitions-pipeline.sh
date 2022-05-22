#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
xlsToJsonFolder=${1}

echo xlsToJsonFolder = $xlsToJsonFolder

echo Importing BEFTA_Master_Definition.xlsx ....................
${dir}/ccd-import-definition-pipeline.sh "${xlsToJsonFolder}/BEFTA_Master_Definition.xlsx"
