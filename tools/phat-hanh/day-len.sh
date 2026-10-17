#!/bin/sh
# Day 1 ban da build len GitHub Releases cua repo public kpah-server/nqsh-h2-tai roi ghi phienban.json.
#   sh phat-hanh/day-len.sh <pc|android|jar> <so ban> <file> ["ghi chu"]
# Dung chung cho phat-hanh.sh (may Windows) va .github/workflows/phat-hanh.yml (Linux).
# Can GitHub CLI da dang nhap, hoac bien GH_TOKEN (token co quyen ghi repo).
set -e
cd "$(dirname "$0")/.."
LOAI=$1
BAN=$2
FILE=$3
GHI_CHU=${4:-}
REPO=${REPO:-kpah-server/nqsh-h2-tai}
export REPO
# Nhanh pokemon/chuyen-doi build ra Huyen Thoai Bao Boi: KHONG duoc day vao kenh tu cap nhat cua NQSH-H2
# (client NQSH-H2 cua nguoi choi cu se tu tai ban Pokemon). Dat REPO=<repo phat hanh HTBB> (va doi LINK
# trong pc/.../CapNhat.cs cho khop); chi khi CHAC CHAN dang phat hanh NQSH-H2 moi dat CHO_PHEP_NQSH=1.
if [ "$REPO" = kpah-server/nqsh-h2-tai ] && [ "${CHO_PHEP_NQSH:-0}" != 1 ] \
    && { echo "${GITHUB_REF_NAME:-}"; git rev-parse --abbrev-ref HEAD 2>/dev/null; } | grep -q '^pokemon/'; then
    echo "TU CHOI: nhanh pokemon/* dang day vao repo NQSH-H2 ($REPO). Dat REPO=... (repo HTBB) hoac CHO_PHEP_NQSH=1."
    exit 1
fi
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) PY=python; GH=${GH:-"/c/Program Files/GitHub CLI/gh.exe"} ;;
    *) PY=python3; GH=${GH:-gh} ;;
esac
case "$LOAI" in
    pc) ON_DINH=NQSH-H2-PC.zip ;;
    android) ON_DINH=NQSH-H2.apk ;;
    jar) ON_DINH=NQSH-H2.jar ;;
    *) echo "dung: sh phat-hanh/day-len.sh pc|android|jar <ban> <file> [ghi chu]"; exit 1 ;;
esac
[ -f "$FILE" ] || { echo "Khong thay file: $FILE"; exit 1; }
"$GH" auth status >/dev/null 2>&1 || { echo "Chua dang nhap GitHub CLI (gh auth login) / chua co GH_TOKEN"; exit 1; }

TEN=$(basename "$FILE")
URL=https://github.com/$REPO/releases/download/$LOAI
TAM=$(mktemp -d)
cp "$FILE" "$TAM/$ON_DINH"

# doc phienban.json NGAY TRUOC khi ghi (khong de 2 lan phat hanh de len nhau)
$PY -I phat-hanh/phienban.py tai-ve "$TAM/phienban.json"
$PY -I phat-hanh/phienban.py ghi "$TAM/phienban.json" "$LOAI" "$BAN" "$URL/$TEN" "$FILE" "$GHI_CHU"
# day file truoc, phienban.json sau cung de client khong bao gio thay link chua co file.
# Moi loai 1 release co dinh (tag pc / android / jar): file theo so ban + file ten co dinh (ban moi nhat).
"$GH" release view "$LOAI" -R "$REPO" >/dev/null 2>&1 || "$GH" release create "$LOAI" -R "$REPO" \
    --title "NQSH-H2 $LOAI" --notes "Ban moi nhat: $URL/$ON_DINH" --latest=false
# file theo so ban: KHONG ghi de (trung so ban = loi, khong lang le thay file cua ban da phat hanh)
"$GH" release upload "$LOAI" -R "$REPO" "$FILE"
"$GH" release upload "$LOAI" -R "$REPO" "$TAM/$ON_DINH" --clobber
$PY -I phat-hanh/phienban.py day-len "$TAM/phienban.json" "Phat hanh $LOAI $BAN"
rm -rf "$TAM"
echo "== Xong: $URL/$TEN  (link co dinh: $URL/$ON_DINH)"
