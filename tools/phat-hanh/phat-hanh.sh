#!/bin/sh
# Build + phát hành Huyền Thoại Bảo Bối (HTBB) trên MÁY NÀY lên GitHub Releases của repo phát hành HTBB.
# (Không cần máy: tab Actions -> workflow "Phát hành HTBB" - .github/workflows/phat-hanh-htbb.yml, tools/phat-hanh/README.md.)
#
#   REPO=<chu>/<repo-phat-hanh-htbb> sh tools/phat-hanh/phat-hanh.sh jar     ["ghi chu"] -> jar thường + nhẹ (ProGuard)
#   REPO=...                         sh tools/phat-hanh/phat-hanh.sh android ["ghi chu"] -> APK :pokemon, ký bằng KEY_DIR
#   REPO=...                         sh tools/phat-hanh/phat-hanh.sh pc      ["ghi chu"] -> bản PC (Unity, máy Windows)
#   REPO=...                         sh tools/phat-hanh/phat-hanh.sh tat-ca  ["ghi chu"]
#   THU=1 sh tools/phat-hanh/phat-hanh.sh jar     -> chỉ build vào tools/phat-hanh/tam/, KHÔNG đẩy lên GitHub (không cần REPO)
#
# Cấu hình (không có giá trị nào cứng trong script):
#   tools/phat-hanh/phien_ban.conf  PHIEN_BAN (vd 0.9.0-opentest) + BAN (số bản build, chỉ tăng) - đưa lên git
#   tools/phat-hanh/may_chu.conf    MAY_CHU_CHINH=host:cổng production (BẮT BUỘC) + CAP_NHAT_URL (tự cập nhật PC,
#                             rỗng = tắt) - gitignore, mẫu may_chu.conf.example; biến môi trường cùng tên thắng
#   REPO                      repo phát hành HTBB (chủ server tạo)
#   Android: KEY_DIR (ngoài repo) + HTBB_KEYSTORE + HTBB_KEY_ALIAS + HTBB_KS_PASS [+ HTBB_KEY_PASS]
#            -> client_apk/ky-ban-phat-hanh.sh (zipalign + apksigner). THU=1 không có khoá = APK chưa ký.
#   PC: UNITY (đường dẫn Unity.exe 6000.5.9f1)
# Tên tệp: HTBB-<PHIEN_BAN>-thuong.jar, -nhe.jar, -android.apk, -PC.zip; phienban.json mục jar / jar-nhe / android / pc.

set -e
cd "$(dirname "$0")/../.."
LOAI=$1
GHI_CHU=${2:-}
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) PY=python; WIN=1 ;;
    *) PY=python3; WIN= ;;
esac
loi() { echo "LOI: $*" >&2; exit 1; }
doc_conf() { [ -f "tools/phat-hanh/$1" ] && sed -n "s/^$2=//p" "tools/phat-hanh/$1" | tail -1 | tr -d '\r" ' || true; }

if [ "$LOAI" = tat-ca ]; then
    for L in jar android pc; do sh tools/phat-hanh/phat-hanh.sh $L "$GHI_CHU"; done
    exit 0
fi
case "$LOAI" in pc|android|jar) ;; *) echo "dung: sh tools/phat-hanh/phat-hanh.sh pc|android|jar|tat-ca [\"ghi chu\"]"; exit 1 ;; esac
UNITY=${UNITY:-"/c/Program Files/Unity/Hub/Editor/6000.5.9f1/Editor/Unity.exe"}
TAM=tools/phat-hanh/tam
rm -rf $TAM && mkdir -p $TAM

# ============================================================ Huyền Thoại Bảo Bối
PHIEN_BAN=${PHIEN_BAN:-$(doc_conf phien_ban.conf PHIEN_BAN)}
BAN=${BAN:-$(doc_conf phien_ban.conf BAN)}
[ -n "$PHIEN_BAN" ] || loi "thieu PHIEN_BAN= trong tools/phat-hanh/phien_ban.conf"
case "$BAN" in ''|*[!0-9]*) loi "BAN='$BAN' khong hop le (tools/phat-hanh/phien_ban.conf)" ;; esac
MAY_CHU_CHINH=${MAY_CHU_CHINH:-$(doc_conf may_chu.conf MAY_CHU_CHINH)}
CAP_NHAT_URL=${CAP_NHAT_URL:-$(doc_conf may_chu.conf CAP_NHAT_URL)}
[ -n "$MAY_CHU_CHINH" ] || loi "chua co may chu production: MAY_CHU_CHINH=host:cong hoac tools/phat-hanh/may_chu.conf (mau: may_chu.conf.example)"
# Rao chan: "nqsh-h2" la ten mien / repo kenh phat hanh cu cua game goc - giu chuoi de con nhan ra va tu choi.
case "$MAY_CHU_CHINH$CAP_NHAT_URL" in *nqsh-h2*) loi "may chu / CAP_NHAT_URL tro ve kenh cu cua game goc (nqsh-h2) - day la ban HTBB" ;; esac
if [ -z "$THU" ]; then
    [ -n "$REPO" ] || loi "chua dat REPO=<chu>/<repo phat hanh HTBB> (THU=1 de chi build)"
    case "$REPO" in *nqsh-h2*) loi "REPO=$REPO la kenh cu cua game goc (nqsh-h2) - ban HTBB phai co repo phat hanh rieng" ;; esac
fi
export REPO PHIEN_BAN MAY_CHU_CHINH CAP_NHAT_URL
GOC_TEN="HTBB-$PHIEN_BAN"
echo "== [HTBB] Phat hanh $LOAI $PHIEN_BAN (ban $BAN), may chu $MAY_CHU_CHINH"

DAY=""   # "<loai> <file>" từng dòng
if [ "$LOAI" = jar ]; then
    GOI=nhe sh client_jar/build.sh "$BAN" | tail -3
    cp "client_jar/out/HTBB-v$BAN-release.jar" "$TAM/$GOC_TEN-thuong.jar"
    cp "client_jar/out/HTBB-v$BAN-nhe.jar" "$TAM/$GOC_TEN-nhe.jar"
    for f in "$TAM/$GOC_TEN-thuong.jar" "$TAM/$GOC_TEN-nhe.jar"; do
        unzip -p "$f" META-INF/MANIFEST.MF | grep -q "^HTBB-May-Chu: $MAY_CHU_CHINH" || loi "$f khong gan may chu $MAY_CHU_CHINH"
        unzip -l "$f" | grep -q " GHI_CONG.txt$" || loi "$f thieu GHI_CONG.txt"
    done
    DAY="jar $TAM/$GOC_TEN-thuong.jar
jar-nhe $TAM/$GOC_TEN-nhe.jar"
elif [ "$LOAI" = android ]; then
    # build CHƯA ký (không truyền KEY_DIR cho Gradle), rồi ký bằng ky-ban-phat-hanh.sh: mật khẩu chỉ ở biến môi trường
    env -u KEY_DIR KIEU=release BAN="$BAN" MAY_CHU="$MAY_CHU_CHINH" OUT="$GOC_TEN-android-chuaky.apk" sh client_apk/build-pokemon.sh
    CHUA_KY="client_apk/out/$GOC_TEN-android-chuaky.apk"
    if [ -n "$KEY_DIR" ]; then
        sh client_apk/ky-ban-phat-hanh.sh "$CHUA_KY" "$TAM/$GOC_TEN-android.apk"
    elif [ -n "$THU" ]; then
        echo "CANH BAO: THU=1 khong co KEY_DIR - APK CHUA KY (khong cai duoc len may), chi de kiem tra"
        cp "$CHUA_KY" "$TAM/$GOC_TEN-android.apk"
    else
        loi "APK phat hanh phai ky: dat KEY_DIR + HTBB_KEYSTORE + HTBB_KEY_ALIAS + HTBB_KS_PASS (xem client_apk/ky-ban-phat-hanh.sh)"
    fi
    DAY="android $TAM/$GOC_TEN-android.apk"
else
    [ -n "$WIN" ] || loi "build PC can may Windows co Unity (hoac workflow phat-hanh-htbb.yml)"
    THAM_CN=""
    [ -n "$CAP_NHAT_URL" ] && THAM_CN="-htbbCapNhat $CAP_NHAT_URL"
    HTBB_OUT="$(cygpath -w "$PWD/$TAM/pc")\\HuyenThoaiBaoBoi.exe" "$UNITY" -batchmode -nographics -accept-apiupdate \
        -projectPath "$(cygpath -w "$PWD/pc")" -executeMethod BuildCli.Win64 -htbbBan "$BAN" -htbbMayChu "$MAY_CHU_CHINH" $THAM_CN \
        -logFile "$(cygpath -w "$PWD/$TAM/unity-build.log")" || true
    grep -q "BUILD RESULT: Succeeded" $TAM/unity-build.log || { grep -E "error CS|BUILD RESULT|HTBB LOI" $TAM/unity-build.log; loi "build PC loi"; }
    [ "$(cat $TAM/htbb-ban.txt 2>/dev/null)" = "$BAN" ] || loi "ban build PC mang so '$(cat $TAM/htbb-ban.txt 2>/dev/null)', can $BAN"
    [ -f "$TAM/pc/GHI_CONG.txt" ] || loi "ban PC thieu GHI_CONG.txt canh .exe"
    $PY -I tools/phat-hanh/nen-zip.py $TAM/pc "$TAM/$GOC_TEN-PC.zip"
    DAY="pc $TAM/$GOC_TEN-PC.zip"
fi

if [ -n "$THU" ]; then
    echo "== THU=1: khong day len GitHub. File:"
    echo "$DAY" | while read -r L F; do ls -l "$F"; done
    exit 0
fi
echo "$DAY" | while read -r L F; do
    sh tools/phat-hanh/day-len.sh "$L" "$BAN" "$F" "$GHI_CHU"
done
