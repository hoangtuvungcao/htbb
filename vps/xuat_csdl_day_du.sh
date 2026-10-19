#!/bin/bash
# ============================================================
#  XUAT CSDL DAY DU tu mot CSDL dang chay ra MOT tep SQL (sql/htbb_day_du.sql).
#
#      sudo bash /opt/htbb/vps/xuat_csdl_day_du.sh [tep_ra]        # mac dinh: <repo>/sql/htbb_day_du.sql
#
#  Du lieu game nam trong CSDL va duoc sua tren trang quan tri; tep nay chup lai trang thai do de dua ve repo
#  (may moi chi can: mysql < sql/htbb_day_du.sql). Gom:
#    1. Schema MOI bang cua account + htbb_db (mysqldump --no-data --databases).
#    2. Du lieu CHI cua bang thiet ke / cau hinh (danh sach BANG_DU_LIEU duoi day + moi bang data_*).
#       Bang nguoi choi / nhat ky / don nap / phien... chi co schema (RONG).
#    3. Mot tai khoan admin voi mat khau NGAU NHIEN (khong ai biet - doi ngay sau khi nap, docs/trien-khai.md).
#  KHONG xuat du lieu nguoi choi. Doc lai 'git diff' truoc khi commit (gia tri cau hinh, ma giftcode cu).
#
#  Bien moi truong: MYSQLDUMP / MYSQL (lenh, mac dinh mysqldump / mysql qua root unix_socket).
#  Them bang thiet ke moi: them ten vao BANG_DU_LIEU (bang data_* tu dong co du lieu).
# ============================================================
set -euo pipefail

DIR=$(cd "$(dirname "$0")/.." && pwd)
RA=${1:-$DIR/sql/htbb_day_du.sql}
read -r -a DUMP <<< "${MYSQLDUMP:-mysqldump}"
read -r -a MY <<< "${MYSQL:-mysql}"

# Bang co du lieu thiet ke / cau hinh (ngoai data_*). Moi bang khac: chi schema.
BANG_DU_LIEU="5h_systems admin_msg bad_word cau_hinh cau_hinh_danh_muc cau_hinh_nhom cau_hinh_phien_ban
cau_hinh_khoi_dong_lai dieu_khien_server giftcode nap_cau_hinh nap_cua_hang nap_goi tob_event tob_event_world_cup_state
tob_fruit tob_tubinh"

CHUNG=(--default-character-set=utf8 --skip-lock-tables --no-tablespaces --skip-comments --skip-dump-date
       --single-transaction --skip-triggers)

DS=$("${MY[@]}" -N -e "SELECT table_name FROM information_schema.tables WHERE table_schema='htbb_db'
     AND table_type='BASE TABLE' ORDER BY table_name") || { echo "[LOI] khong doc duoc danh sach bang"; exit 1; }
CO_DU_LIEU=""
for t in $DS; do
    case " $(echo $BANG_DU_LIEU) " in *" $t "*) CO_DU_LIEU="$CO_DU_LIEU $t"; continue ;; esac
    case "$t" in data_*) CO_DU_LIEU="$CO_DU_LIEU $t" ;; esac
done
for t in $BANG_DU_LIEU; do
    echo "$DS" | grep -qx "$t" || echo "[CANH BAO] khong co bang $t trong htbb_db (bo qua)" >&2
done

MK=$(od -An -N12 -tx1 /dev/urandom | tr -d ' \n')
TAM=$(mktemp)
trap 'rm -f "$TAM"' EXIT
{
    echo "-- Huyen Thoai Bao Boi (HTBB) - CSDL DAY DU (account + htbb_db): MOI bang, MOI du lieu thiet ke, KHONG du lieu nguoi choi."
    echo "-- NGUON DUY NHAT cua du lieu game (khong con bo sinh). Sua du lieu: tren trang quan tri / CSDL, roi xuat lai tep nay"
    echo "-- bang vps/xuat_csdl_day_du.sh (xem sql/README.md). Nap: mysql < sql/htbb_day_du.sql"
    echo "-- ======== 1. Schema moi bang"
    "${DUMP[@]}" "${CHUNG[@]}" --no-data --databases account htbb_db
    echo "-- ======== 2. Du lieu thiet ke / cau hinh ($(echo $CO_DU_LIEU | wc -w) bang)"
    echo "USE \`htbb_db\`;"
    # shellcheck disable=SC2086
    "${DUMP[@]}" "${CHUNG[@]}" --no-create-info --order-by-primary htbb_db $CO_DU_LIEU
    echo "-- ======== 3. Tai khoan admin (mat khau ngau nhien - DOI NGAY sau khi nap)"
    echo "INSERT INTO \`account\`.\`team_user\` (\`username\`, \`password\`, \`phone\`, \`regdate\`, \`ban\`, \`provider\`, \`fromgame\`)"
    echo "  VALUES ('admin', PASSWORD('$MK'), '', NOW(), 0, 0, 0);"
    echo "INSERT INTO \`htbb_db\`.\`5h_active\` (\`userID\`, \`username\`, \`time_end\`)"
    echo "  SELECT u.\`id\`, u.\`username\`, '1894945812' FROM \`account\`.\`team_user\` u WHERE u.\`username\` = 'admin';"
} > "$TAM"
grep -q "^CREATE TABLE \`cau_hinh_danh_muc\`" "$TAM" && grep -q "^INSERT INTO \`cau_hinh_danh_muc\`" "$TAM" \
    || { echo "[LOI] ban xuat thieu cau_hinh_danh_muc - KHONG ghi de $RA"; exit 1; }
mv "$TAM" "$RA"
trap - EXIT
echo "Da ghi $RA ($(wc -c < "$RA") byte, $(grep -c '^CREATE TABLE' "$RA") bang, du lieu $(echo $CO_DU_LIEU | wc -w) bang)"
