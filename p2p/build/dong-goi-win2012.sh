#!/bin/sh
# Đóng gói máy chủ thử Windows Server 2012 (game server + Peerova nhúng) -> p2p/out/win2012/HTBB-P2P-Win2012.zip
#   sh p2p/build/dong-goi-win2012.sh
# Gồm: server/ (htbb-server.jar build nhánh p2p, lib, map, cMap, loginServer/htbb-login.jar, run-p2p.bat,
#      open-firewall.bat, server.ini.p2p.example, p2p/peerova_jni.dll + peerova.exe bản win7), sql/htbb.sql,
#      vps/server.ini.linux.example + vps/loginServer.server.ini.example (mẫu), docs/p2p-win2012.md.
# KHÔNG có server.ini / license / mật khẩu: điền trên máy chủ.
set -e
R=$(cd "$(dirname "$0")/../.." && pwd)
W=$R/p2p/out/dist/windows-x86_64-win7
[ -f "$W/peerova_jni.dll" ] || sh "$R/p2p/build/build-peerova.sh" win7
O=$R/p2p/out/win2012/HTBB-P2P-Win2012
rm -rf "$O" && mkdir -p "$O/server/p2p" "$O/server/loginServer" "$O/sql" "$O/vps" "$O/docs"
sh "$R/p2p/build/build-server.sh" "$O/server/htbb-server.jar"
cp -r "$R/server/lib" "$R/server/map" "$R/server/cMap" "$O/server/"
cp "$R/server/loginServer/htbb-login.jar" "$R/server/loginServer/run.bat" "$O/server/loginServer/"
cp "$R/p2p/win2012/run-p2p.bat" "$R/p2p/win2012/open-firewall.bat" "$R/p2p/win2012/server.ini.p2p.example" "$O/server/"
cp "$W/peerova_jni.dll" "$W/peerova.exe" "$O/server/p2p/"
cp "$R/sql/htbb.sql" "$O/sql/"
cp "$R/vps/server.ini.linux.example" "$R/vps/loginServer.server.ini.example" "$O/vps/"
cp "$R/docs/p2p-win2012.md" "$O/docs/"
(cd "$O/.." && rm -f HTBB-P2P-Win2012.zip && zip -qr HTBB-P2P-Win2012.zip HTBB-P2P-Win2012)
echo "OK $O.zip ($(du -h "$O.zip" 2>/dev/null | cut -f1 || true)) - $(cd "$O/.." && du -h HTBB-P2P-Win2012.zip | cut -f1)"
