#!/bin/bash

mvn clean

cd examples/cloudstate-springboot-example && mvn protobuf:compile
cd ../cloudstate-springboot-jsr330 && mvn protobuf:compile
cd ../../

mvn install