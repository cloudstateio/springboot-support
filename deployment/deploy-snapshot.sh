#!/bin/sh

# Deploy a versioned snapshot (stamped with commit distance and commit hash) to https://bintray.com/cloudstateio/snapshots

mvn --projects cloudstate-springboot-support -am \
  deploy --settings deployment/settings.xml -DskipTests \
  -Djgitver.config=.mvn/snapshot.jgitver.config.xml \
  -DaltDeploymentRepository=bintray-cloudstateio-snapshots::default::https://api.bintray.com/maven/cloudstateio/snapshots/cloudstate-springboot-support/;publish=1
