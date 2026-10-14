#!/bin/sh
# Build + phat hanh tren MAY NAY (Windows, Git Bash) len GitHub Releases cua kpah-server/nqsh-h2-tai.
# (Khong can may: bam "Run workflow" o tab Actions, workflow "Phat hanh" - xem phat-hanh/README.md.)
#   sh phat-hanh/phat-hanh.sh android ["ghi chu"]   -> APK tu android/ (Gradle), tu cap nhat
#   sh phat-hanh/phat-hanh.sh pc      ["ghi chu"]   -> ban PC tu pc/ (Unity), tu cap nhat
#   sh phat-hanh/phat-hanh.sh jar     ["ghi chu"]   -> jar J2ME tu j2me/ (ban -release), chi dat link
#   sh phat-hanh/phat-hanh.sh tat-ca  ["ghi chu"]   -> ca 3
#   BAN=1425 sh phat-hanh/phat-hanh.sh android     -> tu dat so ban (mac dinh: ban dang phat hanh + 1)
#   THU=1 sh phat-hanh/phat-hanh.sh jar            -> chi build vao phat-hanh/tam/, KHONG day len GitHub
set -e
cd "$(dirname "$0")/.."
LOAI=$1
GHI_CHU=${2:-}
if [ "$LOAI" = tat-ca ]; then
    for L in jar android pc; do sh phat-hanh/phat-hanh.sh $L "$GHI_CHU"; done
    exit 0
fi
case "$LOAI" in pc|android|jar) ;; *) echo "dung: sh phat-hanh/phat-hanh.sh pc|android|jar|tat-ca [\"ghi chu\"]"; exit 1 ;; esac
UNITY=${UNITY:-"/c/Program Files/Unity/Hub/Editor/6000.5.9f1/Editor/Unity.exe"}
TAM=phat-hanh/tam
rm -rf $TAM && mkdir -p $TAM

# so ban ke tiep tu phienban.json tren repo (THU=1 ma chua dang nhap gh thi coi nhu {})
python -I phat-hanh/phienban.py tai-ve $TAM/phienban.json 2>/dev/null || echo "{}" > $TAM/phienban.json
if [ -z "$BAN" ]; then
    BAN=$(python -I phat-hanh/phienban.py ban-ke-tiep $TAM/phienban.json $LOAI)
    if [ "$LOAI" = jar ]; then
        # khong ghi de jar da build trong j2me/out
        CUOI=$(ls j2me/out 2>/dev/null | sed -n 's/.*-v\([0-9]*\)-release\.jar$/\1/p' | sort -n | tail -1)
        [ -n "$CUOI" ] && [ "$CUOI" -ge "$BAN" ] && BAN=$((CUOI + 1))
    fi
fi
echo "== Phat hanh $LOAI ban $BAN"

if [ "$LOAI" = android ]; then
    TEN=NQSH-H2-$BAN.apk
    BAN=$BAN OUT=$TEN sh android/build.sh
    cp android/out/$TEN $TAM/$TEN
elif [ "$LOAI" = jar ]; then
    TEN=NQSH-H2-v$BAN.jar
    sh j2me/build.sh $BAN | tail -2
    # ban -release (ProGuard); mapping-vNN.txt GIU KIN, khong day len
    cp "j2me/out/KPAH_276 (1)-automenu-v$BAN-release.jar" $TAM/$TEN
else
    TEN=NQSH-H2-PC-$BAN.zip
    KPAH_VERSION_CODE=$BAN KPAH_OUT="$(cygpath -w "$PWD/$TAM/pc")\\NQSH-H2.exe" "$UNITY" -batchmode -nographics -accept-apiupdate \
        -projectPath "$(cygpath -w "$PWD/pc")" -executeMethod BuildCli.Win64 \
        -logFile "$(cygpath -w "$PWD/$TAM/unity-build.log")" || true
    grep -q "BUILD RESULT: Succeeded" $TAM/unity-build.log || { grep -E "error CS|BUILD RESULT" $TAM/unity-build.log; echo "Build PC loi"; exit 1; }
    python -I phat-hanh/nen-zip.py $TAM/pc $TAM/$TEN
fi

if [ -n "$THU" ]; then
    echo "== THU=1: khong day len GitHub. File: $TAM/$TEN"
    exit 0
fi
sh phat-hanh/day-len.sh $LOAI $BAN $TAM/$TEN "$GHI_CHU"
