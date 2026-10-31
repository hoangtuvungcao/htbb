#!/bin/sh
# Build login server HTBB -> server/loginServer/htbb-login.jar (giong cach build htbb-server.jar: javac --release 8 + jar).
# Ma nguon: server/src/com/htbb/dangnhap/ (chi goi nay, khong keo lop game). Dong goi KEM Connector/J 5.1.49
# (server/lib) vao jar nhu ban cu, nen jar chay doc lap: chep mot tep la du (staging, chay_tren_novadb.sh...).
#
#   sh server/loginServer/build.sh          # chay o bat ky dau
#
# CheckLoginSocket.jar (ban cu) KHONG bi dong toi - giu lam ban quay lui (docs/dang-nhap.md muc 7).
set -eu
DIR=$(cd "$(dirname "$0")" && pwd)
SV=$(cd "$DIR/.." && pwd)
CONNECTOR="$SV/lib/mysql-connector-java-5.1.49.jar"
[ -f "$CONNECTOR" ] || { echo "[LOI] thieu $CONNECTOR"; exit 1; }
TAM=$(mktemp -d "${TMPDIR:-/tmp}/htbb-login.XXXXXX")
trap 'rm -rf "$TAM"' EXIT
mkdir -p "$TAM/cls"
find "$SV/src/com/htbb/dangnhap" -name '*.java' > "$TAM/src.txt"
javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$CONNECTOR" -d "$TAM/cls" @"$TAM/src.txt"
# Connector/J: lop + tai nguyen + META-INF/services/java.sql.Driver (DriverManager tu nap); bo MANIFEST/INDEX cua no
(cd "$TAM/cls" && unzip -q -o "$CONNECTOR" -x 'META-INF/MANIFEST.MF' 'META-INF/INDEX.LIST')
printf 'Main-Class: com.htbb.dangnhap.MayChuDangNhap\nImplementation-Title: HTBB login server\nImplementation-Version: %s\n' \
    "$(sed -n 's/.*PHIEN_BAN = "\(.*\)";/\1/p' "$SV/src/com/htbb/dangnhap/MayChuDangNhap.java")" > "$TAM/mf.txt"
jar cfm "$TAM/htbb-login.jar" "$TAM/mf.txt" -C "$TAM/cls" .
unzip -t "$TAM/htbb-login.jar" > /dev/null
mv "$TAM/htbb-login.jar" "$DIR/htbb-login.jar"
echo "ok: $DIR/htbb-login.jar ($(wc -c < "$DIR/htbb-login.jar") byte)"
