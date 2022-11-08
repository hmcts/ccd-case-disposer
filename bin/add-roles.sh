#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/ccd-add-role.sh citizen
${dir}/ccd-add-role.sh caseworker-disposer_master-junior
${dir}/ccd-add-role.sh caseworker-disposer_jurisdiction_1
${dir}/ccd-add-role.sh caseworker-approver
${dir}/ccd-add-role.sh caseworker-disposer_master
${dir}/ccd-add-role.sh caseworker-caa
${dir}/ccd-add-role.sh caseworker-disposer_master-solicitor
${dir}/ccd-add-role.sh caseworker-disposer_master-manager
