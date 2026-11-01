#!/bin/sh
# Chép mã Java / C# dùng chung của nhánh p2p vào từng thành phần (mỗi bản build HTBB chỉ biên dịch thư mục nguồn của nó,
# nên chép thay vì thêm source root - build.bat / docs/build.md / CI / port-j2me không phải đổi):
#   p2p/peerova/bindings/java/src/dev/peerova/*.java  -> server/src, client_pc/src, client_apk/pokemon/src/main/java
#   p2p/java/htbb/p2p/KetNoiP2P.java                  -> client_pc/src, client_apk/pokemon/src/main/java
#   p2p/peerova/bindings/csharp/Peerova.cs            -> client_pc_ios/Assets/Plugins/Peerova/
#   sh p2p/build/sync-java.sh           chép
#   sh p2p/build/sync-java.sh --check   chỉ kiểm bản chép còn khớp nguồn (thoát 1 nếu lệch)
# SỬA Ở NGUỒN, không sửa bản chép.
set -e
cd "$(dirname "$0")/../.."
rc=0
chep() { # $1 nguồn, $2 đích
    if [ "$MODE" = --check ]; then
        if cmp -s "$1" "$2"; then :; else echo "LECH  $2 (nguon $1)"; rc=1; fi
    else
        mkdir -p "$(dirname "$2")"; cp "$1" "$2"
    fi
}
MODE=$1
for f in Peerova NativeLoader PeerovaTunnel; do
    for d in server/src client_pc/src client_apk/pokemon/src/main/java; do
        chep "p2p/peerova/bindings/java/src/dev/peerova/$f.java" "$d/dev/peerova/$f.java"
    done
done
for d in client_pc/src client_apk/pokemon/src/main/java; do
    chep p2p/java/htbb/p2p/KetNoiP2P.java "$d/htbb/p2p/KetNoiP2P.java"
done
# C# binding (Peerova.Tunnel) -> client Unity PC
chep p2p/peerova/bindings/csharp/Peerova.cs client_pc_ios/Assets/Plugins/Peerova/Peerova.cs
[ "$MODE" = --check ] && { [ $rc = 0 ] && echo "ok: ban chep Java p2p khop nguon"; exit $rc; }
echo "da chep ma Java p2p (dev.peerova + htbb.p2p)"
