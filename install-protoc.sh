#!/usr/bin/env bash

set -beEux -o pipefail

PROTO_VERSION="3.7.1"
PROTO_DIR="/tmp/proto$PROTO_VERSION"

# Can't check for presence of directory as cache auto-creates it.
if [ ! -f "$PROTO_DIR/bin/protoc" ]; then
  wget -O - "https://github.com/google/protobuf/archive/v${PROTO_VERSION}.tar.gz" | tar xz -C /tmp
  cd "/tmp/protobuf-${PROTO_VERSION}"
  ./autogen.sh
  ./configure --prefix="$PROTO_DIR" --disable-shared
  make -j 4
  make install
fi