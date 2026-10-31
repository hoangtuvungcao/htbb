#!/bin/sh
# xay.sh <goc monorepo> <thu muc ra> : bien dich dich vu Huan luyen vien AI (lop may chu + tools/pokemon/kiem_tai +
# tools/pokemon/hlv_ai) - goi boi vps/htbb-hlv-ai.service (ExecStartPre) va may thu. Ma thoat khac 0 = khong bien dich duoc.
set -eu
GOC=${1:?goc monorepo}
RA=${2:?thu muc ra}
command -v javac >/dev/null 2>&1 || { echo "[hlv-ai] thieu javac (apt install openjdk-17-jdk-headless)"; exit 2; }
TAM=$(mktemp -d)
trap 'rm -rf "$TAM"' EXIT
LIBCP="$GOC/server/lib/jxl-2.6.jar:$GOC/server/lib/mysql-connector-java-5.1.49.jar"
find "$GOC/server/src" -name '*.java' > "$TAM/sv.txt"
mkdir -p "$TAM/cls"
javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$LIBCP" -sourcepath "$GOC/server/src" -d "$TAM/cls" @"$TAM/sv.txt" 2>&1 | grep -v '^Note' || true
javac -encoding UTF-8 -nowarn -cp "$TAM/cls:$LIBCP" -d "$TAM/cls" "$GOC"/tools/pokemon/kiem_tai/*.java "$GOC"/tools/pokemon/hlv_ai/*.java 2>&1 | grep -v '^Note' || true
[ -f "$TAM/cls/DichVuHlvAi.class" ] || { echo "[hlv-ai] bien dich hong"; exit 1; }
mkdir -p "$RA"
rm -rf "$RA.moi"
mv "$TAM/cls" "$RA.moi"
rm -rf "$RA"
mv "$RA.moi" "$RA"
echo "[hlv-ai] da bien dich vao $RA"
