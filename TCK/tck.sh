#!/usr/bin/env bash

set -x

USER_FUNCTION_VERSION="0.5.1"
USER_FUNCTION_NAME="user-function"
USER_FUNCTION_IMAGE="cloudstateio/samples-springboot-shopping-cart"

finally() {
  docker rm -f "cloudstate-proxy"
  docker rm -f "$USER_FUNCTION_NAME"
}
trap finally EXIT

docker run -d --name $USER_FUNCTION_NAME --net=host $USER_FUNCTION_IMAGE:$USER_FUNCTION_VERSION
docker run -d --name cloudstate-proxy --net=host -e USER_FUNCTION_PORT=8090 cloudstateio/cloudstate-proxy-dev-mode

docker run --rm --name cloudstate-tck --net=host cloudstateio/cloudstate-tck
tck_status=$?
if [ "$tck_status" -ne "0" ]; then
  docker logs "$USER_FUNCTION_NAME"
fi
exit $tck_status