#!/bin/sh
# Compile-checks + runs the engine-agnostic core of the Unreal plugin (PeerovaCore.h) with g++,
# and syntax-checks it with clang++ if available. Unreal itself is not needed.
set -e
cd "$(dirname "$0")"
ROOT=../../..
INC=$ROOT/crates/peerova-ffi/include
PUB=../Peerova/Source/Peerova/Public
LIB=$(cd $ROOT/target/release && pwd)
[ -f "$LIB/libpeerova.so" ] || (cd $ROOT && cargo build --release -p peerova-ffi)
OUT=${TMPDIR:-/tmp}/peerova-unreal-check
mkdir -p "$OUT"
g++ -std=c++17 -Wall -Wextra -Werror -fno-exceptions -I"$INC" -I"$PUB" test_core.cpp \
    -L"$LIB" -lpeerova -Wl,-rpath,"$LIB" -o "$OUT/test_core"
echo "g++: compiled"
if command -v clang++ >/dev/null; then
  clang++ -std=c++17 -Wall -Wextra -Werror -fno-exceptions -fsyntax-only -I"$INC" -I"$PUB" test_core.cpp
  echo "clang++: syntax OK"
fi
rm -f "$OUT/core.key"
"$OUT/test_core" "$OUT/core.key"
