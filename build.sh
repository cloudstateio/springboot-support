#!/bin/bash

set -x
mvn clean
cd cloudstate-springboot-starter && mvn install -DskipTests && cd ..
cd examples/cloudstate-springboot-example && mvn protobuf:compile && mvn install
cd ../cloudstate-springboot-jsr330 && mvn protobuf:compile && mvn install
cd ../../ && cd cloudstate-springboot-starter && mvn install