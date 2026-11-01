#!/bin/sh
# Compile-checks + runs the engine-agnostic core of the Unreal plugin (PeerovaCore.h) with g++,
# syntax-checks it with clang++ if available, and compiles ../examples/core_demo.cpp.
# Unreal itself is not needed. Native library: $PEEROVA_LIB_DIR, else ../../../target/release.
set -e
cd "$(dirname "$0")"
INC=../Peerova/Source/ThirdParty/PeerovaLibrary/include
PUB=../Peerova/Source/Peerova/Public
LIB=${PEEROVA_LIB_DIR:-../../../target/release}
if [ ! -f "$LIB/libpeerova.so" ]; then
  echo "libpeerova.so not found in $LIB (set PEEROVA_LIB_DIR)" >&2
  exit 1
fi
LIB=$(cd "$LIB" && pwd)
OUT=${TMPDIR:-/tmp}/peerova-unreal-check
mkdir -p "$OUT"
FLAGS="-std=c++17 -Wall -Wextra -Werror -fno-exceptions -I$INC -I$PUB"
g++ $FLAGS test_core.cpp -L"$LIB" -lpeerova -Wl,-rpath,"$LIB" -o "$OUT/test_core"
g++ $FLAGS ../examples/core_demo.cpp -L"$LIB" -lpeerova -Wl,-rpath,"$LIB" -o "$OUT/core_demo"
echo "g++: compiled (test_core, core_demo -> $OUT)"
if command -v clang++ >/dev/null; then
  clang++ $FLAGS -fsyntax-only test_core.cpp
  clang++ $FLAGS -fsyntax-only ../examples/core_demo.cpp
  echo "clang++: syntax OK"
fi
rm -f "$OUT/core.key"
"$OUT/test_core" "$OUT/core.key"
