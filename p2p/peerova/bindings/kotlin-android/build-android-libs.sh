#!/bin/sh
# Cross-compiles libpeerova.so for Android into peerova/src/main/jniLibs/<abi>/.
# Needs: rustup targets (aarch64-linux-android armv7-linux-androideabi x86_64-linux-android),
#        cargo-ndk (cargo install cargo-ndk), ANDROID_NDK_HOME (or NDK under $ANDROID_HOME/ndk/).
#   ./build-android-libs.sh [--api 21]
set -e
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$HERE/../..
API=21
[ "$1" = "--api" ] && API=$2
if [ -z "$ANDROID_NDK_HOME" ]; then
  SDK=${ANDROID_HOME:-$HOME/Android/Sdk}
  ANDROID_NDK_HOME=$(ls -d "$SDK"/ndk/* 2>/dev/null | sort -V | tail -1)
  export ANDROID_NDK_HOME
fi
[ -d "$ANDROID_NDK_HOME" ] || { echo "set ANDROID_NDK_HOME"; exit 1; }
command -v cargo-ndk >/dev/null || { echo "install cargo-ndk: cargo install cargo-ndk"; exit 1; }
rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android
cd "$ROOT"
# RUSTFLAGS: 16 KB page alignment for Android 15+.
RUSTFLAGS="${RUSTFLAGS} -C link-arg=-Wl,-z,max-page-size=16384" \
  cargo ndk --platform "$API" -t arm64-v8a -t armeabi-v7a -t x86_64 \
  -o "$HERE/peerova/src/main/jniLibs" build --release -p peerova-ffi
ls -l "$HERE"/peerova/src/main/jniLibs/*/libpeerova.so
