#!/bin/sh
# Build APK Huyền Thoại Bảo Bối (Pokémon) - code bản jar (client_jar/src + client_jar/shared/mod) chạy trên lớp MIDP Android.
#   sh build-pokemon.sh                         -> out/HTBB-android-thu.apk   (debug, server thử 10.0.2.2:19129)
#   MAY_CHU=192.168.1.5:19129 sh build-pokemon.sh  -> debug nối server khác (máy thật trong mạng LAN...)
#   KIEU=release [BAN=900] [MAY_CHU=host:cong] [KEY_DIR=...] sh build-pokemon.sh -> out/HTBB-android-900.apk
#       release BẮT BUỘC có máy chủ: MAY_CHU / MAY_CHU_CHINH hoặc MAY_CHU_CHINH= trong ../tools/phat-hanh/may_chu.conf
#       (thiếu là dừng). BAN mặc định = BAN= trong ../tools/phat-hanh/phien_ban.conf (versionName = PHIEN_BAN=).
# Cần: JDK 17/21 (JAVA_HOME), Android SDK (ANDROID_HOME, mặc định ~/Android/Sdk), python3.
# Khóa ký release (KEY_DIR/key.properties) KHÔNG đưa vào git; không có khóa thì APK release chưa ký.
set -e
cd "$(dirname "$0")"
KIEU=${KIEU:-debug}
[ -n "$BAN" ] || BAN=$([ -f ../tools/phat-hanh/phien_ban.conf ] && sed -n 's/^BAN=//p' ../tools/phat-hanh/phien_ban.conf | tail -1 | tr -d '\r" ')
BAN=${BAN:-1}
[ -n "$ANDROID_HOME" ] || export ANDROID_HOME="$HOME/Android/Sdk"
if [ -z "$JAVA_HOME" ]; then
    for j in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-17-openjdk-amd64; do
        [ -d "$j" ] && export JAVA_HOME="$j" && break
    done
fi
THAM="-PhtbbBan=$BAN"
[ -n "$MAY_CHU" ] && THAM="$THAM -PmayChu=$MAY_CHU"
[ -n "$KEY_DIR" ] && THAM="$THAM -PhtbbKeyDir=$KEY_DIR"
mkdir -p out
if [ "$KIEU" = release ]; then
    sh ./gradlew -q --no-daemon :pokemon:assembleRelease $THAM
    VAO=pokemon/build/outputs/apk/release/pokemon-release.apk
    [ -f "$VAO" ] || VAO=pokemon/build/outputs/apk/release/pokemon-release-unsigned.apk
    RA=${OUT:-HTBB-android-$BAN.apk}
else
    sh ./gradlew -q --no-daemon :pokemon:assembleDebug $THAM
    VAO=pokemon/build/outputs/apk/debug/pokemon-debug.apk
    RA=${OUT:-HTBB-android-thu.apk}
fi
cp "$VAO" "out/$RA"
echo "OK out/$RA ($(wc -c < "out/$RA") byte)"
