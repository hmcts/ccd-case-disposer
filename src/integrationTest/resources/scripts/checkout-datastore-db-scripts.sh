#!/bin/bash
cd src/integrationTest/resources/scripts/
#Clean up previous scripts
rm -rf ../db/migration/*

#Checkout latest data store migration scripts
git init
git config core.sparseCheckout true
git remote add -f origin https://github.com/hmcts/ccd-data-store-api.git
git config core.sparseCheckout true
git sparse-checkout init
git sparse-checkout set migration
git pull origin master

# Move all sql scripts to db/migration folder
find . -name "*.sql" -exec mv {} ../db/migration/ \;

#Remove all hidden folders excluding checkout-datastore-db-scripts.sh
find . ! -name 'checkout-datastore-db-scripts.sh' -type f -exec rm -f {} +


while [  -z "$(ls -A ../db/migration)"  ]
do
  echo "Sleep for 1 seconds to make sure that migration scripts are present"
  sleep 1
done