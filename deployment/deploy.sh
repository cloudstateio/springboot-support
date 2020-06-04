#!/bin/bash

if [[ "$TRAVIS_TAG" != "" ]]; then
  echo "Publish maven artifacts..."
  # analyze current branch and react accordingly
  gpg2 --keyring=$TRAVIS_BUILD_DIR/pubring.gpg --no-default-keyring --import deployment/signingkey.asc
  gpg2 --allow-secret-key-import --keyring=$TRAVIS_BUILD_DIR/secring.gpg --no-default-keyring --import deployment/signingkey.asc
  mvn clean deploy --settings deployment/settings.xml -Dgpg.executable=gpg2 -Dgpg.keyname=DCF2BFEEB9C58B48 -Dgpg.passphrase=$PASSPHRASE -Dgpg.publicKeyring=$TRAVIS_BUILD_DIR/pubring.gpg -Dgpg.secretKeyring=$TRAVIS_BUILD_DIR/secring.gpg
  exit 0
fi

