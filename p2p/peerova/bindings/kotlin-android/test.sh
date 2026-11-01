#!/bin/sh
# JVM test of the Kotlin wrapper (Android uses the same code + NDK-built libs).
# Env: KOTLINC (default: kotlinc on PATH, else Android Studio's bundled one), COROUTINES_JAR (auto-found in ~/.gradle).
set -e
cd "$(dirname "$0")"
JAVA=../java
[ -f $JAVA/build/peerova.jar ] || $JAVA/build.sh build
KOTLINC=${KOTLINC:-$(command -v kotlinc || ls /snap/android-studio/*/plugins/Kotlin/kotlinc/bin/kotlinc 2>/dev/null | tail -1)}
[ -x "$KOTLINC" ] || { echo "kotlinc not found (set KOTLINC)"; exit 1; }
KLIB=$(dirname "$KOTLINC")/../lib
COROUTINES_JAR=${COROUTINES_JAR:-$(find "$HOME/.gradle" "$HOME/.m2" -name 'kotlinx-coroutines-core-jvm-1.*.jar' 2>/dev/null | sort -V | tail -1)}
[ -f "$COROUTINES_JAR" ] || { echo "kotlinx-coroutines-core-jvm jar not found (set COROUTINES_JAR)"; exit 1; }
mkdir -p build && rm -f build/*.jar
CP=$JAVA/build/peerova.jar:$COROUTINES_JAR
"$KOTLINC" -jvm-target 1.8 -cp "$CP" -d build/peerova-kt.jar peerova/src/main/kotlin/dev/peerova/kt/*.kt 2>&1 | grep -v '^warning: unable to find kotlin-stdlib' || true
[ -f build/peerova-kt.jar ]
"$KOTLINC" -cp "$CP:build/peerova-kt.jar" -d build/test.jar test/PeerovaKtTest.kt
java -Djava.library.path=$JAVA/build -cp "$CP:build/peerova-kt.jar:build/test.jar:$KLIB/kotlin-stdlib.jar" PeerovaKtTestKt
