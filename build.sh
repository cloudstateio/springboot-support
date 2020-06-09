#!/usr/bin/env bash

set -x
set -e
mvn clean
mvn --projects cloudstate-springboot-support install -DskipTests
mvn --projects examples/cloudstate-springboot-example protobuf:compile install
mvn --projects examples/cloudstate-springboot-jsr330 protobuf:compile install
mvn --projects cloudstate-springboot-support install
