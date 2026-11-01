#!/bin/sh
# Copies the canonical C header into bindings that vendor it (C example, Swift package, Unreal plugin).
#   ./sync-headers.sh          copy
#   ./sync-headers.sh --check  only verify the copies match (exit 1 if not)
set -e
cd "$(dirname "$0")"
SRC=../crates/peerova-ffi/include/peerova.h
DESTS="c/peerova.h swift/Sources/CPeerova/include/peerova.h unreal/Peerova/Source/ThirdParty/PeerovaLibrary/include/peerova.h"
rc=0
for d in $DESTS; do
  if [ "$1" = --check ]; then
    if diff -q "$SRC" "$d" >/dev/null; then echo "ok    $d"; else echo "STALE $d"; rc=1; fi
  else
    mkdir -p "$(dirname "$d")"; cp "$SRC" "$d"; echo "copied $d"
  fi
done
exit $rc
