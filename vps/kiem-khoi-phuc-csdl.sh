#!/bin/bash
# ============================================================
#  KIEM TRA KHOI PHUC BAN SAO LUU (san sang production, 17/10/2026)
#
#  Ban sao luu chua tung khoi phuc thu = chua co ban sao luu. Script nay lay ban
#  <DB>_*.sql.gz MOI NHAT trong thu muc backup (vps/backup_db.sh tao), nap vao mot
#  CSDL TAM ten <DB>_kiem_khoi_phuc tren CUNG may chu MariaDB, roi so so bang va so
#  dong cua cac bang chinh voi CSDL dang chay. Xong thi XOA CSDL tam. Khong cham vao
#  CSDL that (chi SELECT COUNT).
#
#  Cai vao cron cua root, mot tuan mot lan, sau backup:
#      30 4 * * 1 /opt/kpah/vps/kiem-khoi-phuc-csdl.sh >> /opt/kpah/log/backup.log 2>&1
#
#  Bien moi truong (co mac dinh cho VPS):
#      OUT=/opt/kpah/backup     thu muc chua .sql.gz
#      DB=kpah_db               CSDL can kiem (chay them lan nua voi DB=account)
#      MYSQL="mysql"            lenh mysql (mat khau trong /root/.my.cnf)
#      BANG="tob_char tob_pet tob_clan tob_pokedex data_item"   bang so so dong
#  Ma thoat: 0 = khoi phuc duoc va so lieu hop ly; 1 = loi (cron gui mail neu co MAILTO).
#
#  KHOI PHUC THAT (khi su co) - lam tay, theo thu tu:
#      sudo systemctl stop kpah-game kpah-login
#      zcat /opt/kpah/backup/kpah_db_<NGAY>.sql.gz | mysql kpah_db
#      zcat /opt/kpah/backup/account_<NGAY>.sql.gz | mysql account
#      sudo systemctl start kpah-login kpah-game
#  (mysqldump khong co --databases nen file KHONG tu tao/xoa CSDL: nap de len CSDL dang co,
#   tung bang DROP TABLE IF EXISTS + CREATE lai.)
# ============================================================
set -euo pipefail

OUT=${OUT:-/opt/kpah/backup}
DB=${DB:-kpah_db}
MYSQL=${MYSQL:-mysql}
BANG=${BANG:-"tob_char tob_pet tob_clan tob_pokedex data_item"}
TAM="${DB}_kiem_khoi_phuc"

loi() { echo "$(date '+%F %T') KHOI PHUC LOI: $*" >&2; $MYSQL -e "DROP DATABASE IF EXISTS \`$TAM\`" 2>/dev/null || true; exit 1; }

F=$(ls -1t "$OUT"/"${DB}"_*.sql.gz 2>/dev/null | head -1 || true)
[ -n "$F" ] || loi "khong thay ban sao luu ${DB}_*.sql.gz trong $OUT"
TUOI=$(( ( $(date +%s) - $(stat -c %Y "$F") ) / 3600 ))
[ "$TUOI" -le 48 ] || echo "$(date '+%F %T') CANH BAO: ban moi nhat da $TUOI gio tuoi ($F) - backup hang ngay co chay khong?" >&2

BAT_DAU=$(date +%s)
$MYSQL -e "DROP DATABASE IF EXISTS \`$TAM\`; CREATE DATABASE \`$TAM\` CHARACTER SET utf8"
zcat "$F" | $MYSQL "$TAM" || loi "nap $F vao $TAM that bai"
GIAY=$(( $(date +%s) - BAT_DAU ))

SO_BANG_GOC=$($MYSQL -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB'")
SO_BANG_TAM=$($MYSQL -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$TAM'")
echo "$(date '+%F %T') $F -> $TAM: $SO_BANG_TAM bang (dang chay: $SO_BANG_GOC), nap mat ${GIAY}s"
[ "$SO_BANG_TAM" -gt 0 ] || loi "ban khoi phuc khong co bang nao"
# Server tu tao them bang theo ky (tob_log_use_luong_* ...) nen dang chay co the NHIEU hon ban luu
# vai bang; IT hon thi chac chan co van de.
[ "$SO_BANG_TAM" -ge $(( SO_BANG_GOC - 5 )) ] || loi "thieu bang: $SO_BANG_TAM / $SO_BANG_GOC"

for b in $BANG; do
    CO=$($MYSQL -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$TAM' AND table_name='$b'")
    [ "$CO" = 1 ] || { echo "  $b: (khong co trong ban luu, bo qua)"; continue; }
    N_TAM=$($MYSQL -N -e "SELECT COUNT(*) FROM \`$TAM\`.\`$b\`")
    N_GOC=$($MYSQL -N -e "SELECT COUNT(*) FROM \`$DB\`.\`$b\`" 2>/dev/null || echo "?")
    echo "  $b: ban luu $N_TAM dong / dang chay $N_GOC dong"
    # Dong dang chay chi duoc NHIEU HON (nguoi choi moi tu luc backup); ban luu = 0 ma dang chay > 0 la hong.
    if [ "$N_GOC" != "?" ] && [ "$N_TAM" -eq 0 ] && [ "$N_GOC" -gt 0 ]; then
        loi "bang $b trong ban luu RONG trong khi dang chay co $N_GOC dong"
    fi
done

$MYSQL -e "DROP DATABASE IF EXISTS \`$TAM\`"
echo "$(date '+%F %T') KHOI PHUC THU OK ($DB)"
