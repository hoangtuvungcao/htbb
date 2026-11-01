#!/bin/sh
# Builds build/libpeerova_jni.so (+ copies libpeerova.so) and build/peerova.jar, then runs the test.
#   ./build.sh          build + test
#   ./build.sh build    build only
# Env: JAVA_HOME (auto-detected from javac), PEEROVA_LIB_DIR (directory containing libpeerova.so,
#      default ../../target/release), PEEROVA_INCLUDE (directory containing peerova.h, default ../c)
set -e
cd "$(dirname "$0")"
ROOT=../..
LIBDIR=${PEEROVA_LIB_DIR:-$ROOT/target/release}
if [ -z "$JAVA_HOME" ]; then JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")"); fi
case "$(uname -s)" in
  Darwin) OS=darwin; SO=dylib; RPATH="-Wl,-rpath,@loader_path" ;;
  *)      OS=linux;  SO=so;    RPATH="-Wl,-rpath,\$ORIGIN" ;;
esac
INC=${PEEROVA_INCLUDE:-../c}
[ -f "$LIBDIR/libpeerova.$SO" ] || { echo "libpeerova.$SO not found in $LIBDIR (set PEEROVA_LIB_DIR)"; exit 1; }
mkdir -p build/classes
cp "$LIBDIR/libpeerova.$SO" build/
gcc -O2 -Wall -Wextra -shared -fPIC peerova_jni.c \
    -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/$OS" -I"$INC" \
    -Lbuild -lpeerova $RPATH -o build/libpeerova_jni.$SO
# --release 8 keeps the jar usable on Java 8+ / Android.
javac --release 8 -Xlint:-options -d build/classes src/dev/peerova/Peerova.java
jar cf build/peerova.jar -C build/classes .
echo "built build/libpeerova_jni.$SO build/peerova.jar"
[ "$1" = build ] && exit 0
javac -cp build/peerova.jar -d build/test test/PeerovaTest.java
java -Djava.library.path=build -cp build/peerova.jar:build/test PeerovaTest
