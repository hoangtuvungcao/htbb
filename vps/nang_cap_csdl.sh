#!/bin/bash
# ============================================================
#  NANG CAP CSDL DANG CHAY len schema + du lieu thiet ke cua ban hien tai (nap sql/htbb.sql).
#
#      sudo bash /opt/htbb/vps/nang_cap_csdl.sh
#
#  Lam theo thu tu: (1) kiem may la MariaDB, (2) SAO LUU htbb_db + account bang vps/backup_db.sh
#  (hong la DUNG, khong nap gi), (3) nap sql/htbb.sql (account + htbb_db), (4) kiem 10 bang du lieu
#  thiet ke + danh muc cau hinh co du dong.
#
#  CHAY LAI DUOC: sql/htbb.sql phan A chi CREATE TABLE IF NOT EXISTS (du lieu day du chi chep vao CSDL MOI,
#  htbb_db.data_item rong - CSDL dang chay khong bi dong); phan B la CREATE TABLE IF NOT EXISTS / ADD COLUMN IF
#  NOT EXISTS / CREATE INDEX IF NOT EXISTS / INSERT IGNORE, hoac DROP+CREATE+INSERT cua bang thiet ke (data_*,
#  cau_hinh_danh_muc, cau_hinh_nhom - bang sinh, khong co du lieu nguoi choi). Bang nguoi choi chi duoc them
#  bang / them cot / noi cot. Gia tri admin da dat (cau_hinh, web_*, giftcode, nap_*...) KHONG bi dong toi.
#
#  Khong tu khoi dong lai server: nap xong thi deploy jar moi / systemctl restart htbb-game.
#  Jar cu van chay duoc tren CSDL da nang cap (bang/cot moi jar cu khong doc).
#
#  Bien moi truong (de thu tren may khac):
#    MYSQL="mysql ..."            lenh mysql (mac dinh: mysql, root qua unix_socket hoac /etc/htbb/htbb.env)
#    BO_SAO_LUU=1                 bo buoc sao luu (CHI dung tren CSDL thu, khong bao gio tren production)
# ============================================================
set -euo pipefail

DIR=$(cd "$(dirname "$0")/.." && pwd)
SQL="$DIR/sql/htbb.sql"
DB=htbb_db   # ten CSDL co dinh trong sql/htbb.sql (USE `htbb_db`)
MOC='-- ######## B. NANG CAP'
ENV_FILE=${HTBB_ENV_FILE:-/etc/htbb/htbb.env}

loi() { echo "[LOI] $*" >&2; exit 1; }
[ -r "$SQL" ] || loi "khong doc duoc $SQL"
grep -q "^$MOC" "$SQL" || loi "$SQL thieu phan B (dong '$MOC') - tep hong / ban cu"

TAM=$(mktemp -d)
trap 'rm -rf "$TAM"' EXIT

# Dang nhap giong vps/backup_db.sh: root qua unix_socket, khong thi tep cnf tam 0600 tu htbb.env.
if [ -n "${MYSQL:-}" ]; then
    read -r -a LENH <<< "$MYSQL"
elif [ "$(id -u)" = 0 ] && mysql -N -e "select 1" >/dev/null 2>&1; then
    LENH=(mysql)
elif [ -r "$ENV_FILE" ]; then
    doc_env() { sed -n -E "s/^$1=(.*)$/\1/p" "$ENV_FILE" | tail -n 1 | sed -E "s/^[\"']//; s/[\"']$//"; }
    {
        echo "[client]"
        [ -n "$(doc_env HTBB_DB_HOST || true)" ] && echo "host=$(doc_env HTBB_DB_HOST)"
        [ -n "$(doc_env HTBB_DB_PORT || true)" ] && echo "port=$(doc_env HTBB_DB_PORT)"
        [ -n "$(doc_env HTBB_DB_USER || true)" ] && echo "user=$(doc_env HTBB_DB_USER)"
        echo "password=$(doc_env HTBB_DB_PASS)"
    } > "$TAM/my.cnf"
    chmod 600 "$TAM/my.cnf"
    LENH=(mysql --defaults-file="$TAM/my.cnf")
else
    loi "khong dang nhap duoc CSDL: chay bang root (unix_socket) hoac co HTBB_DB_PASS trong $ENV_FILE"
fi
my() { "${LENH[@]}" --default-character-set=utf8mb4 "$@"; }

echo "== 1/4 Kiem may chu CSDL"
PB=$(my -N -e "SELECT VERSION()") || loi "khong ket noi duoc CSDL"
case "$PB" in
    *MariaDB*) echo "   $PB" ;;
    *) loi "can MariaDB (ADD COLUMN IF NOT EXISTS, CREATE INDEX IF NOT EXISTS); may nay: $PB" ;;
esac
my -N -e "SELECT 1 FROM information_schema.schemata WHERE schema_name='$DB'" | grep -q 1 \
    || loi "khong co CSDL $DB (CSDL moi: mysql -u root < sql/htbb.sql, khong can script nay)"
my -N -e "SELECT 1 FROM information_schema.tables WHERE table_schema='$DB' AND table_name='tob_char'" | grep -q 1 \
    || loi "$DB khong co bang tob_char - khong phai CSDL game HTBB"

echo "== 2/4 Sao luu"
if [ "${BO_SAO_LUU:-0}" = 1 ]; then
    echo "   BO QUA (BO_SAO_LUU=1)"
else
    bash "$DIR/vps/backup_db.sh" || loi "sao luu that bai - KHONG nap gi"
fi

echo "== 3/4 Nap $(basename "$SQL") (account + $DB)"
my "$DB" < "$SQL" || loi "nap that bai giua chung - xem loi tren; chay lai script (cac lenh chay lai duoc) hoac phuc hoi tu ban sao luu"

echo "== 4/4 Kiem"
SAI=0
for B in data_cap_do_lau_dai data_nang_cap_lau_dai data_kinh_te_lau_dai data_su_kien_nam data_hst_ho_cau \
         data_noi_dung_trang_bi cau_hinh_danh_muc cau_hinh_nhom data_npc data_map_ten; do
    N=$(my -N -e "SELECT COUNT(*) FROM \`$DB\`.\`$B\`" 2>/dev/null || echo 0)
    if [ "${N:-0}" -gt 0 ]; then echo "   ok  $B: $N dong"; else echo "   SAI $B: rong / thieu"; SAI=1; fi
done
# Moi khoa la mot dong INSERT trong phan B -> so dong cau_hinh_danh_muc phai bang dung so do.
CAN=$(sed -n "/^$MOC/,\$p" "$SQL" | grep -c "^INSERT INTO \`cau_hinh_danh_muc\`" || true)
CO=$(my -N -e "SELECT COUNT(*) FROM \`$DB\`.cau_hinh_danh_muc")
if [ "$CO" = "$CAN" ]; then echo "   ok  danh muc cau hinh: $CO khoa"; else echo "   SAI danh muc cau hinh: $CO khoa, tep co $CAN"; SAI=1; fi
my -N -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='$DB' AND table_name='cau_hinh_danh_muc' AND column_name='thu_tu'" | grep -q 1 \
    || { echo "   SAI cau_hinh_danh_muc thieu cot thu_tu"; SAI=1; }
# 0.9.4: phan loai khoa (lop) phai co trong danh muc SQL - may chu doc no de an / khoa khoa co dinh.
N_AN=$(my -N -e "SELECT COUNT(*) FROM \`$DB\`.cau_hinh_danh_muc WHERE lop <> 'chinh'" 2>/dev/null || echo loi)
case "$N_AN" in
    ''|loi) echo "   SAI cau_hinh_danh_muc thieu cot lop (phan loai 0.9.4)"; SAI=1 ;;
    *) echo "   ok  phan loai: $N_AN khoa an (co_dinh / luon_bat)" ;;
esac
[ "$SAI" = 0 ] || loi "kiem sau khi nap co loi (xem tren)"
echo "== XONG. Tiep: deploy jar moi / systemctl restart htbb-game, roi xem log: [DU LIEU SQL] da nap 6 bang, [CAU HINH] danh muc tu CSDL"
