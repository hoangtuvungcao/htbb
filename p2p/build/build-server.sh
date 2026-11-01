#!/bin/sh
# Build htbb-server.jar (nhanh p2p: co com.htbb.p2p.MayChuP2P + dev.peerova) tu server/src - nhu docs/build.md muc 1.
#   sh p2p/build/build-server.sh [tep ra]      (mac dinh p2p/out/dist/server/htbb-server.jar)
# KHONG ghi de server/htbb-server.jar trong git (jar production cua main). Java 8 (class 52).
set -e
R=$(cd "$(dirname "$0")/../.." && pwd)
RA=${1:-$R/p2p/out/dist/server/htbb-server.jar}
sh "$R/p2p/build/sync-java.sh" --check
B=$R/p2p/out/build-server; rm -rf "$B"; mkdir -p "$B/cls" "$(dirname "$RA")"
cd "$R/server"
find src -name '*.java' > "$B/src.txt"
javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar" \
    -sourcepath src -d "$B/cls" @"$B/src.txt" > "$B/javac.log" 2>&1 || { grep -v '^Note' "$B/javac.log" | head -40; exit 1; }
printf 'Main-Class: server.TeamServer\nClass-Path: lib/jxl-2.6.jar lib/mysql-connector-java-5.1.49.jar\nImplementation-Version: p2p\n' > "$B/mf.txt"
jar cfm "$RA.tmp" "$B/mf.txt" -C "$B/cls" .
unzip -t "$RA.tmp" >/dev/null && mv "$RA.tmp" "$RA"
echo "OK $RA ($(wc -c < "$RA") byte, $(find "$B/cls" -name '*.class' | wc -l) class)"
