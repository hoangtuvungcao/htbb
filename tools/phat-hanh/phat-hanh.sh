#!/bin/sh
# Phat hanh ban moi len https://nqsh-h2.top/tai/ (xem phat-hanh/README.md).
#   sh phat-hanh/phat-hanh.sh android ["ghi chu"]   -> APK tu Android277 (ban TeaMobi), tu cap nhat
#   sh phat-hanh/phat-hanh.sh pc      ["ghi chu"]   -> ban PC tu Decompiled/KPAH_276 (Unity), tu cap nhat
#   sh phat-hanh/phat-hanh.sh jar     ["ghi chu"]   -> jar J2ME tu _mod276/jar (ban -release), chi dat link
#   BAN=1425 sh phat-hanh/phat-hanh.sh android     -> tu dat so ban (mac dinh: ban dang phat hanh + 1)
#   THU=1 sh phat-hanh/phat-hanh.sh jar            -> chi build + ghi phat-hanh/tam/, KHONG day len VPS
# Cac buoc: lay phienban.json tren web -> build -> day file len /var/www/tai/ -> ghi phienban.json.
# Can cai 1 lan truoc: sh phat-hanh/cai-dat-vps.sh
set -e
cd "$(dirname "$0")/.."
LOAI=$1
GHI_CHU=${2:-}
VPS=${VPS:-root@180.93.127.26}
WEB_DIR=/var/www/tai
URL=https://nqsh-h2.top/tai
UNITY=${UNITY:-"/c/Program Files/Unity/Hub/Editor/6000.5.9f1/Editor/Unity.exe"}
JAR_DIR=${JAR_DIR:-"$HOME/Desktop/KPAH-All/_mod276/jar"}
TAM=phat-hanh/tam
case "$LOAI" in pc|android|jar) ;; *) echo "dung: sh phat-hanh/phat-hanh.sh pc|android|jar [\"ghi chu\"]"; exit 1 ;; esac

rm -rf $TAM && mkdir -p $TAM
curl -sf -A Mozilla/5.0 "$URL/phienban.json?t=$(date +%s)" -o $TAM/phienban.json || echo "{}" > $TAM/phienban.json
if [ -z "$BAN" ]; then
    BAN=$(python -I phat-hanh/phienban.py ban-ke-tiep $TAM/phienban.json $LOAI)
    if [ "$LOAI" = jar ]; then
        # so ban jar noi tiep vNN da build trong _mod276/jar/out (khong ghi de jar cu)
        CUOI=$(ls "$JAR_DIR/out" | sed -n 's/.*-v\([0-9]*\)-release\.jar$/\1/p' | sort -n | tail -1)
        [ -n "$CUOI" ] && [ "$CUOI" -ge "$BAN" ] && BAN=$((CUOI + 1))
    fi
fi
echo "== Phat hanh $LOAI ban $BAN"

if [ "$LOAI" = android ]; then
    TEN=NQSH-H2-$BAN.apk
    (cd Android277 && BAN=$BAN OUT=$TEN sh build.sh | grep -v WARNING)
    cp Android277/out/$TEN $TAM/$TEN
    BAN_ON_DINH=NQSH-H2.apk                   # link co dinh de gui nguoi choi moi
elif [ "$LOAI" = jar ]; then
    TEN=NQSH-H2-v$BAN.jar
    (cd "$JAR_DIR" && sh build.sh $BAN | tail -2)
    # ban -release (ProGuard); mapping-vNN.txt GIU KIN, khong day len
    cp "$JAR_DIR/out/KPAH_276 (1)-automenu-v$BAN-release.jar" $TAM/$TEN
    BAN_ON_DINH=NQSH-H2.jar
else
    TEN=NQSH-H2-PC-$BAN.zip
    OUT_DIR=$(cygpath -w "$PWD/$TAM/pc")
    KPAH_VERSION_CODE=$BAN KPAH_OUT="$OUT_DIR\\NQSH-H2.exe" "$UNITY" -batchmode -nographics -accept-apiupdate \
        -projectPath "$(cygpath -w "$PWD/Decompiled/KPAH_276")" -executeMethod BuildCli.Win64 \
        -logFile "$(cygpath -w "$PWD/$TAM/unity-build.log")" || true
    grep -q "BUILD RESULT: Succeeded" $TAM/unity-build.log || { grep -E "error CS|BUILD RESULT" $TAM/unity-build.log; echo "Build PC loi"; exit 1; }
    rm -rf "$TAM/pc/NQSH-H2_BurstDebugInformation_DoNotShip"
    powershell -NoProfile -Command "Compress-Archive -Path '$OUT_DIR\\*' -DestinationPath '$(cygpath -w "$PWD/$TAM")\\$TEN' -Force"
    BAN_ON_DINH=NQSH-H2-PC.zip
fi

python -I phat-hanh/phienban.py ghi $TAM/phienban.json $LOAI $BAN "$URL/$TEN" $TAM/$TEN "$GHI_CHU"
if [ -n "$THU" ]; then
    echo "== THU=1: khong day len VPS. File: $TAM/$TEN, $TAM/phienban.json"
    exit 0
fi
# day file truoc, phienban.json sau cung (doi ten nguyen tu) de client khong bao gio thay link chua co file
scp -q $TAM/$TEN "$VPS:$WEB_DIR/$TEN.tmp"
scp -q $TAM/phienban.json "$VPS:$WEB_DIR/phienban.json.tmp"
ssh "$VPS" "cd $WEB_DIR && mv -f $TEN.tmp $TEN && cp -f $TEN $BAN_ON_DINH.tmp && mv -f $BAN_ON_DINH.tmp $BAN_ON_DINH && mv -f phienban.json.tmp phienban.json && chmod 644 * && ls -la"
echo "== Xong: $URL/$TEN  (link co dinh: $URL/$BAN_ON_DINH)"
