#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
xlsToJsonFolder=${1}

echo xlsToJsonFolder = $xlsToJsonFolder

echo Importing DISPOSER_Master_Definition_new.xlsx ....................
${dir}/ccd-import-definition-pipeline.sh "${xlsToJsonFolder}/DISPOSER_Master_Definition.xlsx"
