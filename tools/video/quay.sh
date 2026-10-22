#!/bin/sh
# Quay video giới thiệu bằng CLIENT PC THẬT (headless) nối một MÁY CHỦ THỬ cục bộ (không bao giờ dùng máy chủ production).
#
#   MAY_CHU=127.0.0.1:19420 sh tools/video/quay.sh <thư mục ra> <tài khoản> <tên nhân vật> [cảnh,cảnh,...] [-Dkhoá=giá trị ...]
#
# Cần: client đã build (APP_IMAGE=0 MAY_CHU=... sh client_pc/build.sh), ffmpeg, JDK 9+. Máy chủ thử: sv.cho_phep_thu_nghiem=1,
# nhân vật chính (và bot GM nếu dùng -DgmBot=) có tên trong sv.adminAccounts để dàn cảnh bằng lệnh chat GM.
# Cảnh: 1..10 (video 2 phút), p1 p2 p3 (PvP), chu (biển chữ đề, cần -Dchu.ds=<tệp danh sách>). Thiếu = 1..10 + chu.
# Tuỳ chọn: -Dquay.w=2400 -Dquay.h=1080 (cỡ khung), -Dquay.pix=yuv444p -Dquay.crf=6 (ảnh tĩnh), -Dquay.khongCham=true,
#   -DmapHiem=8 -DchoHiem=600 (Pokémon hiếm), -DgmBot=kt001 -Dbots=namkhoa (nhân vật chính là người chơi thường).
# Ra: <ra>/doan/<cảnh>.mp4 (+ .moc mốc thời gian), ảnh chụp *.png, bot.log. Dựng: python3 -I tools/video/dung_video.py <json>.
set -e
cd "$(dirname "$0")/../.."
GOC=$(pwd)
RA=$1; TK=$2; TEN=$3; CANH=$4
[ -n "$RA" ] && [ -n "$TK" ] && [ -n "$TEN" ] || { sed -n '2,12p' "$0"; exit 1; }
shift 3; [ $# -gt 0 ] && shift
MAY_CHU=${MAY_CHU:-127.0.0.1:19420}
CLS=$GOC/client_pc/out/build/cls
[ -d "$CLS" ] || { echo "LOI: chua build client (APP_IMAGE=0 MAY_CHU=$MAY_CHU sh client_pc/build.sh)"; exit 1; }
mkdir -p "$RA"
T=$(mktemp -d)
javac -nowarn -encoding UTF-8 -cp "$CLS" -d "$T/quay" $(ls tools/video/*.java | grep -v BotVideo)
javac -nowarn -encoding UTF-8 -d "$T/bot" tools/pokemon/kiem_tai/*.java tools/video/BotVideo.java
java -Xmx3g -Djava.awt.headless=true -Dnoi="$GOC" -Dbot.cp="$T/bot" -Dbot.host="${MAY_CHU%:*}" -Dbot.cong="${MAY_CHU#*:}" "$@" \
    -cp "$CLS:$GOC/client_pc/out/build/res:$T/quay" KichBanVideo "$RA" "$MAY_CHU" "$TK" "$TEN" $CANH
