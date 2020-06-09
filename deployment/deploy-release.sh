#!/bin/sh

# Deploy a tagged release to https://bintray.com/cloudstateio/releases

mvn --projects cloudstate-springboot-support -am deploy --settings deployment/settings.xml -DskipTests
