#!/bin/sh
# Kiem hoi quy sau mot lan chay BotTai (docs/pokemon/TOI_UU_HIEU_NANG.md muc 5).
#
#   sh tools/pokemon/kiem_tai/kiem_hoi_quy.sh <bot.log> <game.log> <pid server> [quai ha toi thieu=1] [thread nen]
#
# DAT khi: bot khong rot ket noi, moi bot vao duoc game, co quai bi ha (bot GM danh quai), log server
# khong co Exception moi (bo qua loi da biet: RegionVantienTran NPE su kien Van Tien Tran), va - neu
# dua [thread nen] = so thread luc server rong truoc khi chay - sau khi bot thoat 15 s so thread
# tro ve <= nen + 20 (khong ro thread khi ngat ket noi).
BOT=$1; GAME=$2; PID=$3; QUAI_MIN=${4:-1}; NEN=$5
hong=0
kq=$(grep '=== KET QUA ===' "$BOT")
[ -z "$kq" ] && { echo "HONG: khong thay dong KET QUA trong $BOT"; exit 1; }
echo "$kq"
rot=$(echo "$kq" | sed -n 's/.*rot ket noi=\([0-9]*\).*/\1/p')
tb=$(echo "$kq" | sed -n 's/.*that bai vao=\([0-9]*\).*/\1/p')
ha=$(echo "$kq" | sed -n 's/.*quai ha=\([0-9]*\).*/\1/p')
[ "${rot:-1}" -eq 0 ] || { echo "HONG: rot ket noi=$rot"; hong=1; }
[ "${tb:-1}" -eq 0 ] || { echo "CANH BAO: $tb bot khong vao duoc (dang nhap cham? xem TRE dang nhap)"; }
[ "${ha:-0}" -ge "$QUAI_MIN" ] || { echo "HONG: quai ha=$ha < $QUAI_MIN"; hong=1; }
ex=$(grep 'Exception' "$GAME" | grep -v 'RegionVantienTran' | grep -v -F '[MANG]' | grep -c .)
[ "$ex" -eq 0 ] || { echo "HONG: $ex dong Exception moi trong $GAME:"; grep 'Exception' "$GAME" | grep -v 'RegionVantienTran' | grep -v -F '[MANG]' | sed -E 's/[0-9]+/N/g' | sort | uniq -c | sort -rn | head -5; hong=1; }
if [ -n "$NEN" ] && kill -0 "$PID" 2>/dev/null; then
    sleep 15
    th=$(ls /proc/"$PID"/task | wc -l)
    echo "thread sau khi bot thoat: $th (nen $NEN)"
    [ "$th" -le $((NEN + 20)) ] || { echo "HONG: thread khong ve nen (ro thread khi ngat ket noi?)"; hong=1; }
fi
[ $hong -eq 0 ] && echo "DAT" || echo "HONG"
exit $hong
