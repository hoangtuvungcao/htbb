#!/bin/sh
# Đẩy 1 bản đã build lên GitHub Releases của repo phát hành rồi ghi phienban.json.
#   REPO=<chu>/<repo-phat-hanh-htbb> sh tools/phat-hanh/day-len.sh <pc|android|jar|jar-nhe> <so ban> <file> ["ghi chu"]
# Dùng chung cho phat-hanh.sh (máy) và .github/workflows/phat-hanh-htbb.yml / phat-hanh.yml (Linux).
# Cần GitHub CLI đã đăng nhập, hoặc biến GH_TOKEN (token có quyền ghi repo).
#
# HTBB (mặc định): REPO BẮT BUỘC (repo phát hành riêng của HTBB), tên cố định HTBB-PC.zip / HTBB.apk / HTBB.jar /
#   HTBB-nhe.jar. PHIEN_BAN (tools/phat-hanh/phien_ban.conf) ghi vào phienban.json.
set -e
cd "$(dirname "$0")/../.."
LOAI=$1
BAN=$2
FILE=$3
GHI_CHU=${4:-}
[ -n "$REPO" ] || { echo "LOI: chua dat REPO=<chu>/<repo phat hanh HTBB>"; exit 1; }
export REPO
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) PY=python; GH=${GH:-"/c/Program Files/GitHub CLI/gh.exe"} ;;
    *) PY=python3; GH=${GH:-gh} ;;
esac
TEN_GAME=HTBB
export TEN_GAME
case "$LOAI" in
    pc) ON_DINH=$TEN_GAME-PC.zip ;;
    android) ON_DINH=$TEN_GAME.apk ;;
    jar) ON_DINH=$TEN_GAME.jar ;;
    jar-nhe) ON_DINH=HTBB-nhe.jar ;;
    *) echo "dung: sh tools/phat-hanh/day-len.sh pc|android|jar|jar-nhe <ban> <file> [ghi chu]"; exit 1 ;;
esac
[ -f "$FILE" ] || { echo "Khong thay file: $FILE"; exit 1; }
"$GH" auth status >/dev/null 2>&1 || { echo "Chua dang nhap GitHub CLI (gh auth login) / chua co GH_TOKEN"; exit 1; }

TEN=$(basename "$FILE")
URL=https://github.com/$REPO/releases/download/$LOAI
TAM=$(mktemp -d)
cp "$FILE" "$TAM/$ON_DINH"

# doc phienban.json NGAY TRUOC khi ghi (khong de 2 lan phat hanh de len nhau)
$PY -I tools/phat-hanh/phienban.py tai-ve "$TAM/phienban.json"
$PY -I tools/phat-hanh/phienban.py ghi "$TAM/phienban.json" "$LOAI" "$BAN" "$URL/$TEN" "$FILE" "$GHI_CHU"
# day file truoc, phienban.json sau cung de client khong bao gio thay link chua co file.
# Moi loai 1 release co dinh (tag pc / android / jar): file theo so ban + file ten co dinh (ban moi nhat).
"$GH" release view "$LOAI" -R "$REPO" >/dev/null 2>&1 || "$GH" release create "$LOAI" -R "$REPO" \
    --title "$TEN_GAME $LOAI" --notes "Ban moi nhat: $URL/$ON_DINH" --latest=false
# file theo so ban: KHONG ghi de (trung so ban = loi, khong lang le thay file cua ban da phat hanh)
"$GH" release upload "$LOAI" -R "$REPO" "$FILE"
"$GH" release upload "$LOAI" -R "$REPO" "$TAM/$ON_DINH" --clobber
$PY -I tools/phat-hanh/phienban.py day-len "$TAM/phienban.json" "Phat hanh $LOAI $BAN"
rm -rf "$TAM"
echo "== Xong: $URL/$TEN  (link co dinh: $URL/$ON_DINH)"
