#!/bin/sh
# Builds the Java SDK and runs the round-trip test.
#   ./build.sh          build + test (the test runs with `java -cp peerova.jar:...` only, no library path)
#   ./build.sh build    build only
#   ./build.sh dist     build only; fail unless all desktop platforms are in the jar (release packaging)
#
# Outputs (build/):
#   peerova.jar           single-file SDK: Java API + native library for every platform found below,
#                         extracted and loaded automatically at run time (see src/dev/peerova/NativeLoader.java)
#   libpeerova_jni.so     JNI glue for the two-library layout (-Djava.library.path), + a copy of libpeerova.so
#
# The jar's native library per platform is ONE file, peerova_java (.dll / .so): the JNI glue peerova_jni.c
# statically linked with the core static library, so no second DLL has to be found at load time.
#   linux-x86_64            from $PEEROVA_LIB_DIR/libpeerova.a        (gcc)
#   windows-x86_64          from $PEEROVA_WIN_LIB_DIR/libpeerova.a    (x86_64-w64-mingw32-gcc; only if set)
#   windows-x86_64-legacy   from $PEEROVA_WIN7_LIB_DIR/libpeerova.a   (Windows 7 / 8.1 / Server 2012; only if set)
# Android is not in the jar (it uses jniLibs, see ../kotlin-android).
#
# Env: JAVA_HOME (auto-detected from javac), PEEROVA_LIB_DIR (default ../../target/release),
#      PEEROVA_WIN_LIB_DIR, PEEROVA_WIN7_LIB_DIR (directories with the Windows static libraries),
#      PEEROVA_INCLUDE (directory containing peerova.h, default ../c),
#      JNI_WIN32_INCLUDE (directory with a Windows jni_md.h, default ./jni-win32), MINGW_CC,
#      PEEROVA_VERSION (names the per-user cache directory; the SHA-256 is part of it anyway).
set -e
cd "$(dirname "$0")"
ROOT=../..
MODE=${1:-test}
LIBDIR=${PEEROVA_LIB_DIR:-$ROOT/target/release}
WINDIR=${PEEROVA_WIN_LIB_DIR:-}
WIN7DIR=${PEEROVA_WIN7_LIB_DIR:-}
MINGW_CC=${MINGW_CC:-x86_64-w64-mingw32-gcc}
JNI_WIN32=${JNI_WIN32_INCLUDE:-jni-win32}
if [ -z "$JAVA_HOME" ]; then JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")"); fi
case "$(uname -s)" in
  Darwin) OS=darwin; SO=dylib; RPATH="-Wl,-rpath,@loader_path" ;;
  *)      OS=linux;  SO=so;    RPATH="-Wl,-rpath,\$ORIGIN" ;;
esac
INC=${PEEROVA_INCLUDE:-../c}
VERSION=${PEEROVA_VERSION:-sdk}

rm -rf build/classes build/jar-res build/test
mkdir -p build/classes build/jar-res/dev/peerova/native

# ---- two-library layout (java.library.path users, Kotlin JVM test) ----
if [ -f "$LIBDIR/libpeerova.$SO" ]; then
  cp "$LIBDIR/libpeerova.$SO" build/
  gcc -O2 -Wall -Wextra -shared -fPIC peerova_jni.c \
      -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/$OS" -I"$INC" \
      -Lbuild -lpeerova $RPATH -o build/libpeerova_jni.$SO
fi

# ---- single-file natives for peerova.jar ----
IDX=build/jar-res/dev/peerova/native/natives.properties
echo "version=$VERSION" > $IDX
missing=""
add_native() { # platform file
  sum=$(sha256sum "build/jar-res/dev/peerova/native/$1/$2" | cut -d' ' -f1)
  printf '%s.file=%s\n%s.sha256=%s\n' "$1" "$2" "$1" "$sum" >> $IDX
  echo "  jar native $1/$2 ($(wc -c < "build/jar-res/dev/peerova/native/$1/$2") bytes)"
}
if [ "$OS" = linux ] && [ -f "$LIBDIR/libpeerova.a" ]; then
  d=build/jar-res/dev/peerova/native/linux-x86_64; mkdir -p $d
  # --exclude-libs hides the core's Rust symbols; only JNI entry points (+ peerova_*) are exported.
  gcc -O2 -Wall -Wextra -shared -fPIC peerova_jni.c "$LIBDIR/libpeerova.a" \
      -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -I"$INC" \
      -Wl,--gc-sections -Wl,--exclude-libs,ALL -Wl,-z,noexecstack -s \
      -lpthread -ldl -lm -lrt -lutil -lgcc_s -lc -o $d/libpeerova_java.so
  add_native linux-x86_64 libpeerova_java.so
else missing="$missing linux-x86_64"; fi
win_native() { # platform libdir
  if [ -n "$2" ] && [ -f "$2/libpeerova.a" ] && command -v "$MINGW_CC" >/dev/null 2>&1; then
    d=build/jar-res/dev/peerova/native/$1; mkdir -p $d
    # Fully static (libgcc too): the DLL depends on Windows system DLLs only.
    "$MINGW_CC" -O2 -Wall -Wextra -shared peerova_jni.c "$2/libpeerova.a" \
        -I"$JAVA_HOME/include" -I"$JNI_WIN32" -I"$INC" \
        -static-libgcc -Wl,--gc-sections -s \
        -lws2_32 -liphlpapi -lbcrypt -ladvapi32 -lkernel32 -lntdll -luserenv -ldbghelp \
        -o $d/peerova_java.dll
    add_native "$1" peerova_java.dll
  else missing="$missing $1"; fi
}
win_native windows-x86_64 "$WINDIR"
win_native windows-x86_64-legacy "$WIN7DIR"
if [ -n "$missing" ]; then
  echo "jar natives missing for:$missing (build the core library for them first)"
  [ "$MODE" = dist ] && exit 1
fi

# --release 8 keeps the jar usable on Java 8+ / Android.
javac --release 8 -Xlint:-options -d build/classes src/dev/peerova/*.java
jar cf build/peerova.jar -C build/classes . -C build/jar-res .
echo "built build/peerova.jar${missing:+ (without:$missing)}"
[ "$MODE" = build ] || [ "$MODE" = dist ] && exit 0

javac -cp build/peerova.jar -d build/test test/PeerovaTest.java
# No -Djava.library.path: the jar alone must be enough.
env -u LD_LIBRARY_PATH java -cp build/peerova.jar:build/test PeerovaTest
