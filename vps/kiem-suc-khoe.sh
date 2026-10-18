#!/bin/bash
# ============================================================
#  KIEM TRA SUC KHOE HTBB (san sang production, 17/10/2026)
#
#  systemd Restart=always chi cuu duoc khi JVM CHET. Truong hop te hon la JVM con song,
#  cong 19129 van mo, nhung server TREO (deadlock, het ket noi CSDL, GC lien tuc): nguoi
#  choi vao khong duoc ma systemd khong biet. Script nay kiem tung tang:
#
#    1. cong game  : bat tay -40 THAT (gui goi -40, phai nhan lai goi -40 kem khoa) - khong
#                    phai chi "cong mo". Do luon thoi gian phan hoi.
#    2. JVM con chay vong lap: tep trang_thai.json (server/ChiSoMayChu ghi moi 30 giay) phai
#                    moi hon NGUONG_TREO giay.
#    3. login server: cong 8023 mo.
#    4. CSDL       : SELECT 1 (bo qua neu khong co lenh mysql / khong co quyen).
#    5. web        : GET $WEB_URL/api/trang-thai tra 200 (bo qua neu WEB_URL trong).
#
#  In mot dong ket qua; ma thoat 0 = khoe, 1 = co tang hong.
#  TU_KHOI_DONG_LAI=1 (chi root): game TREO 2 lan lien tiep (tang 1 hoac 2) -> systemctl restart
#  htbb-game (shutdown hook van luu nhan vat; TimeoutStopSec=180). Mac dinh TAT: chi bao.
#
#  Cai (root):  * * * * * TU_KHOI_DONG_LAI=1 /opt/htbb/vps/kiem-suc-khoe.sh >> /opt/htbb/logs/suc-khoe.log 2>&1
#  Bien: HOST=127.0.0.1 CONG_GAME=19129 CONG_LOGIN=8023 THU_MUC=/opt/htbb NGUONG_TREO=120
#        WEB_URL=http://127.0.0.1:3000 MYSQL="mysql" (chay bang root: unix_socket)
# ============================================================
set -u
HOST=${HOST:-127.0.0.1}
CONG_GAME=${CONG_GAME:-19129}
CONG_LOGIN=${CONG_LOGIN:-8023}
THU_MUC=${THU_MUC:-/opt/htbb}
NGUONG_TREO=${NGUONG_TREO:-120}
WEB_URL=${WEB_URL:-}
MYSQL=${MYSQL:-mysql}
TU_KHOI_DONG_LAI=${TU_KHOI_DONG_LAI:-0}
DEM_TEP=${DEM_TEP:-/tmp/htbb-suc-khoe.dem}

hong=""
ghi=""

# 1. Bat tay -40: goi = [lenh -40 = 0xD8][do dai 2 byte = 0]; server tra goi bat dau bang 0xD8.
t0=$(date +%s%N)
tra=$(timeout 5 bash -c "exec 3<>/dev/tcp/$HOST/$CONG_GAME && printf '\xd8\x00\x00' >&3 && head -c 1 <&3 | od -An -tx1" 2>/dev/null | tr -d ' \n')
ms=$(( ($(date +%s%N) - t0) / 1000000 ))
if [ "$tra" = "d8" ]; then ghi="$ghi game=ok(${ms}ms)"; else ghi="$ghi game=HONG"; hong="$hong game"; fi

# 2. Nhip tim JVM
TT="$THU_MUC/server/trang_thai.json"
if [ -f "$TT" ]; then
    tuoi=$(( $(date +%s) - $(stat -c %Y "$TT") ))
    online=$(grep -o '"nhanVat":[0-9]*' "$TT" | cut -d: -f2)
    if [ "$tuoi" -le "$NGUONG_TREO" ]; then ghi="$ghi nhip=ok(${tuoi}s,online=${online:-?})"; else ghi="$ghi nhip=TREO(${tuoi}s)"; hong="$hong nhip"; fi
else
    ghi="$ghi nhip=?(chua co $TT - server cu hoac sv.chiSoMayChu=0)"
fi

# 3. Login server
if timeout 3 bash -c "exec 3<>/dev/tcp/$HOST/$CONG_LOGIN" 2>/dev/null; then ghi="$ghi login=ok"; else ghi="$ghi login=HONG"; hong="$hong login"; fi

# 4. CSDL
if command -v ${MYSQL%% *} >/dev/null 2>&1; then
    if timeout 5 $MYSQL -N -e "SELECT 1" >/dev/null 2>&1; then ghi="$ghi csdl=ok"; else ghi="$ghi csdl=HONG"; hong="$hong csdl"; fi
fi

# 5. Web
if [ -n "$WEB_URL" ]; then
    ma=$(curl -s -o /dev/null -m 5 -w '%{http_code}' "$WEB_URL/api/trang-thai" || echo 000)
    if [ "$ma" = 200 ]; then ghi="$ghi web=ok"; else ghi="$ghi web=HONG($ma)"; hong="$hong web"; fi
fi

echo "$(date '+%F %T')$ghi"

# Tu khoi dong lai game khi treo 2 phut lien tiep (2 lan cron) - chi tang game/nhip.
case "$hong" in
    *game*|*nhip*)
        n=$(( $(cat "$DEM_TEP" 2>/dev/null || echo 0) + 1 )); echo "$n" > "$DEM_TEP"
        if [ "$TU_KHOI_DONG_LAI" = 1 ] && [ "$n" -ge 2 ] && systemctl is-active --quiet htbb-game; then
            echo "$(date '+%F %T') game treo $n lan lien tiep -> systemctl restart htbb-game"
            systemctl restart htbb-game
            echo 0 > "$DEM_TEP"
        fi ;;
    *) echo 0 > "$DEM_TEP" 2>/dev/null || true ;;
esac

[ -z "$hong" ]
