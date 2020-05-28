#!/bin/bash

mvn clean
cd cloudstate-springboot-starter && mvn install -DskipTests && cd ..
cd examples
cd cloudstate-springboot-example
mvn protobuf:compile
cd ..
cd cloudstate-springboot-jsr330
mvn protobuf:compile
cd ../../

mvn install