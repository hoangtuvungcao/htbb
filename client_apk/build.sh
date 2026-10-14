#!/bin/sh
# Build APK bản Android (KPAH 2.7.7 + mod NQSH-H2) từ source bằng Gradle.
#   BAN=1423 [OUT=NQSH-H2-1423.apk] [KEY_DIR=...] [TEN=NQSH-H2] sh build.sh
# KEY_DIR: thư mục chứa key.properties (keystore=, alias=, password=) + keystore - KHÔNG đưa vào git.
# Máy Windows (Git Bash) tự lấy JDK 17 + Android SDK của Unity nếu chưa đặt JAVA_HOME / ANDROID_HOME.
set -e
cd "$(dirname "$0")"
BAN=${BAN:?dat BAN=so ban (versionCode)}
OUT=${OUT:-NQSH-H2-$BAN.apk}
TEN=${TEN:-NQSH-H2}
KEY_DIR=${KEY_DIR:-"$HOME/Desktop/KPAH-All/_tmp_build/android-key"}
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
        U="C:/Program Files/Unity/Hub/Editor/6000.5.9f1/Editor/Data/PlaybackEngines/AndroidPlayer"
        [ -n "$JAVA_HOME" ] || export JAVA_HOME="$U/OpenJDK"
        [ -n "$ANDROID_HOME" ] || export ANDROID_HOME="$U/SDK"
        KEY_DIR=$(cygpath -m "$KEY_DIR")
        ;;
esac
[ -f "$KEY_DIR/key.properties" ] || { echo "Khong thay $KEY_DIR/key.properties (dat KEY_DIR)"; exit 1; }
sh ./gradlew -q --no-daemon :app:assembleRelease -PkpahBan="$BAN" -PkpahTen="$TEN" -PkpahKeyDir="$KEY_DIR"
mkdir -p out
cp app/build/outputs/apk/release/app-release.apk "out/$OUT"
# bảng tên R8 (giữ riêng, không đưa lên git / không phát kèm)
cp app/build/outputs/mapping/release/mapping.txt "out/mapping-$BAN.txt" 2>/dev/null || true
echo "OK out/$OUT"
