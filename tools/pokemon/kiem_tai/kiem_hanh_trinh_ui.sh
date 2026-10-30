#!/bin/sh
# Hanh trinh nguoi choi THAT + soi giao dien (client_pc/test/KiemHanhTrinhUI) vao mot may chu dang chay (stack thu /
# staging): dang ky trong game, tao Pokemon, huong dan tan thu, HUD, NPC, Menu... chup 7 co + bao cao loi giao dien.
#
#   sh tools/pokemon/kiem_tai/kiem_hanh_trinh_ui.sh <host> <cong game> <thu muc ra> [tai khoan GM, mk 123456]
#
# Ma thoat: 0 = khong HONG (NHAC chi bao), 1 = co HONG, 2 = khong build / bien dich duoc.
# Bien: HANH_CO=apk720x1600d2,j2me240x320 (chi chup cac co nay), HANH_BUOC=<chuoi> (chi chup buoc co chuoi nay).
set -u
[ $# -ge 3 ] || { sed -n '2,9p' "$0"; exit 2; }
HOST=$1; CONG=$2; RA=$3; GM=${4:-}
GOC=$(cd "$(dirname "$0")/../../.." && pwd)
cd "$GOC" || exit 2
mkdir -p "$RA"
RA=$(cd "$RA" && pwd)
APP_IMAGE=0 timeout -k 10 900 sh client_pc/build.sh > "$RA/build.log" 2>&1 || { tail -20 "$RA/build.log"; exit 2; }
mkdir -p "$RA/cls"
javac -encoding UTF-8 -nowarn -cp client_pc/out/build/cls -sourcepath client_pc/test:server/src -d "$RA/cls" \
    client_pc/test/KiemHanhTrinhUI.java > "$RA/javac.log" 2>&1 || { cat "$RA/javac.log"; exit 2; }
CP="client_pc/out/build/cls:client_pc/out/build/res:$RA/cls"
OPT="-Djava.awt.headless=true -Dhtbb.mayChu=$GOC/server -Dhanh.host=$HOST"
[ -n "${HANH_CO:-}" ] && OPT="$OPT -Dhanh.co=$HANH_CO"
[ -n "${HANH_BUOC:-}" ] && OPT="$OPT -Dhanh.buoc=$HANH_BUOC"
KQ=0
# shellcheck disable=SC2086
timeout -k 10 2400 java -Xmx1000m $OPT -cp "$CP" KiemHanhTrinhUI "$CONG" "$RA/nguoi_choi" > "$RA/nguoi_choi.log" 2>&1 || KQ=1
tail -1 "$RA/nguoi_choi.log"
if [ -n "$GM" ]; then
    # shellcheck disable=SC2086
    timeout -k 10 2400 java -Xmx1000m $OPT -Dhanh.gm="$GM" -cp "$CP" KiemHanhTrinhUI "$CONG" "$RA/gm" > "$RA/gm.log" 2>&1 || KQ=1
    tail -1 "$RA/gm.log"
fi
exit $KQ
