#!/bin/bash

mvn clean
cd cloudstate-springboot-starter && mvn install
cd examples/cloudstate-springboot-example && mvn protobuf:compile
cd ../cloudstate-springboot-jsr330 && mvn protobuf:compile
cd ../../

mvn install