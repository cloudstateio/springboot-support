#!/usr/bin/env bash

# Deploy a tagged release to https://bintray.com/cloudstateio/releases

mvn --projects cloudstate-springboot-support -am deploy --settings deployment/settings.xml -DskipTests

# Sync to Maven Central via Bintray

readonly tag=$(git describe --tags --exact-match)
readonly version=${tag#v}

[ -z "$version" ] && echo "No version tag for current commit" && exit 1

echo "Syncing cloudstate-springboot-support $version to Maven Central..."

readonly maven_central_sync="https://api.bintray.com/maven_central_sync/cloudstateio/releases/cloudstate-springboot-support/versions/$version"

curl -X POST --user $BINTRAY_USERNAME:$BINTRAY_PASSWORD -H "Content-Type: application/json" --data '@-' $maven_central_sync << EOF
{
  "username": "$SONATYPE_USERNAME",
  "password": "$SONATYPE_PASSWORD"
}
EOF
