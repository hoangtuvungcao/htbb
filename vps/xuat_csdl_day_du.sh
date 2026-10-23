#!/bin/bash
# ============================================================
#  XUAT LAI phan A (CSDL day du) cua sql/htbb.sql tu mot CSDL dang chay.
#
#      sudo bash /opt/htbb/vps/xuat_csdl_day_du.sh [tep_ra]        # mac dinh: <repo>/sql/htbb.sql
#
#  sql/htbb.sql la MOT tep cho ca CSDL moi lan CSDL dang chay (xem sql/README.md):
#    A. CSDL day du (tep nay SINH lai phan nay tu CSDL):
#       1. Schema MOI bang cua account + htbb_db (mysqldump --no-data), CREATE TABLE IF NOT EXISTS:
#          bang da co giu nguyen ca du lieu.
#       2. Du lieu CHI cua bang thiet ke / cau hinh (danh sach BANG_DU_LIEU duoi day + moi bang data_*), nap vao
#          bang TAM (CREATE TEMPORARY TABLE trong `account`) roi chi CHEP sang htbb_db khi CSDL MOI
#          (htbb_db.data_item rong luc bat dau) va bang dich con rong. CSDL dang chay: bang tam bi bo, khong dong gi.
#          Bang nguoi choi / nhat ky / don nap / phien... chi co schema (RONG).
#       3. Mot tai khoan admin (CHI CSDL moi) voi mat khau ngau nhien khong ai biet - doi ngay sau khi nap
#          (docs/trien-khai.md). Xuat lai giu nguyen chuoi ngau nhien cua tep cu (git diff gon).
#    B. NANG CAP CSDL dang chay (viet tay + khoi sinh, chay lai duoc): chep NGUYEN VAN tu tep nguon (mac dinh
#       <repo>/sql/htbb.sql, bien HTBB_SQL_NGUON), tu dong "-- ######## B. NANG CAP" toi het tep.
#  KHONG xuat du lieu nguoi choi. Doc lai 'git diff' truoc khi commit (gia tri cau hinh, ma giftcode cu).
#
#  Bien moi truong: MYSQLDUMP / MYSQL (lenh, mac dinh mysqldump / mysql qua root unix_socket), HTBB_SQL_NGUON.
#  Them bang thiet ke moi: them ten vao BANG_DU_LIEU (bang data_* tu dong co du lieu).
# ============================================================
set -euo pipefail

DIR=$(cd "$(dirname "$0")/.." && pwd)
RA=${1:-$DIR/sql/htbb.sql}
NGUON=${HTBB_SQL_NGUON:-$DIR/sql/htbb.sql}
MOC='-- ######## B. NANG CAP'
read -r -a DUMP <<< "${MYSQLDUMP:-mysqldump}"
read -r -a MY <<< "${MYSQL:-mysql}"

[ -r "$NGUON" ] && grep -q "^$MOC" "$NGUON" \
    || { echo "[LOI] $NGUON khong co dong '$MOC' (phan nang cap can giu) - KHONG ghi"; exit 1; }

# Bang co du lieu thiet ke / cau hinh (ngoai data_*). Moi bang khac: chi schema.
BANG_DU_LIEU="5h_systems admin_msg bad_word cau_hinh cau_hinh_danh_muc cau_hinh_nhom cau_hinh_phien_ban
cau_hinh_khoi_dong_lai dieu_khien_server giftcode nap_cau_hinh nap_cua_hang nap_goi tob_event tob_event_world_cup_state
tob_fruit tob_tubinh"

CHUNG=(--default-character-set=utf8 --skip-lock-tables --no-tablespaces --skip-comments --skip-dump-date
       --single-transaction --skip-triggers --skip-add-drop-table)

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
case " $CO_DU_LIEU " in *" data_item "*) ;; *) echo "[LOI] htbb_db khong co data_item"; exit 1 ;; esac

# Mat khau admin: giu chuoi ngau nhien cua tep nguon (neu co), khong thi sinh moi.
MK=$(sed -n "1,/^$MOC/p" "$NGUON" | sed -n -E "s/.*PASSWORD\('([0-9a-f]{24})'\).*/\1/p" | head -n 1)
[ -n "$MK" ] || MK=$(od -An -N12 -tx1 /dev/urandom | tr -d ' \n')

# Bo AUTO_INCREMENT=N: xuat -> nap -> xuat phai ra CUNG tep (tai khoan admin cua lan nap truoc khong day so len).
BO_AI='s/^(\).*) AUTO_INCREMENT=[0-9]+/\1/'
TAM=$(mktemp)
trap 'rm -f "$TAM"' EXIT
{
    echo "-- Huyen Thoai Bao Boi (HTBB) - MOT TEP CSDL (account + htbb_db) cho CA HAI truong hop, chay lai duoc:"
    echo "--   * CSDL MOI (MariaDB trong):  mysql -u root < sql/htbb.sql   -> moi bang, moi du lieu thiet ke, mot tai khoan admin."
    echo "--   * CSDL DANG CHAY (production): sudo bash /opt/htbb/vps/nang_cap_csdl.sh   (sao luu -> nap tep nay -> kiem)."
    echo "--     Bang da co giu nguyen; du lieu phan A CHI chep vao CSDL moi (htbb_db.data_item rong); phan B nang cap"
    echo "--     (them bang / cot / chi muc, bang thiet ke DROP + CREATE + INSERT, dong mac dinh INSERT IGNORE). Khong dong toi"
    echo "--     du lieu nguoi choi va gia tri admin da sua (cau_hinh, web_*, giftcode, nap_*...). Xem sql/README.md."
    echo "-- Phan A SINH bang vps/xuat_csdl_day_du.sh tu CSDL (sua du lieu tren trang quan tri / CSDL roi xuat lai);"
    echo "-- phan B viet tay + khoi sinh '-- >>> <khoi>.sql' (tools/*). KHONG du lieu nguoi choi."
    echo "-- ######## A. CSDL DAY DU"
    echo "-- ======== 1. Schema moi bang (CREATE TABLE IF NOT EXISTS: bang da co giu nguyen, ca du lieu)"
    "${DUMP[@]}" "${CHUNG[@]}" --no-data --databases account htbb_db \
        | sed -E "$BO_AI; s/^CREATE TABLE \`/CREATE TABLE IF NOT EXISTS \`/"
    echo "-- ======== 2. Du lieu thiet ke / cau hinh ($(echo $CO_DU_LIEU | wc -w) bang): bang TAM, CHI chep vao CSDL MOI"
    echo "SET @htbb_csdl_moi := (SELECT COUNT(*) = 0 FROM \`htbb_db\`.\`data_item\`);"
    echo "USE \`account\`;"
    # shellcheck disable=SC2086
    "${DUMP[@]}" "${CHUNG[@]}" --order-by-primary htbb_db $CO_DU_LIEU \
        | sed -E "$BO_AI; s/^CREATE TABLE \`/CREATE TEMPORARY TABLE \`/"
    echo "-- Chep bang tam -> htbb_db: chi CSDL moi + bang dich rong (data_item cuoi cung: nap do giua chung thi chay lai)."
    echo "SET @htbb_cu_sql_mode := @@SQL_MODE, @htbb_cu_fk := @@FOREIGN_KEY_CHECKS;"
    echo "SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO', FOREIGN_KEY_CHECKS = 0;"
    for t in $(echo $CO_DU_LIEU | tr ' ' '\n' | grep -vx data_item) data_item; do
        echo "SET @l := IF(@htbb_csdl_moi = 1 AND NOT EXISTS (SELECT 1 FROM \`htbb_db\`.\`$t\`), 'INSERT INTO \`htbb_db\`.\`$t\` SELECT * FROM \`account\`.\`$t\`', 'DO 0'); PREPARE l FROM @l; EXECUTE l; DEALLOCATE PREPARE l; DROP TEMPORARY TABLE \`account\`.\`$t\`;"
    done
    echo "SET SQL_MODE = @htbb_cu_sql_mode, FOREIGN_KEY_CHECKS = @htbb_cu_fk;"
    echo "USE \`htbb_db\`;"
    echo "-- ======== 3. Tai khoan admin (CHI CSDL moi; mat khau ngau nhien - DOI NGAY sau khi nap)"
    echo "INSERT INTO \`account\`.\`team_user\` (\`username\`, \`password\`, \`phone\`, \`regdate\`, \`ban\`, \`provider\`, \`fromgame\`)"
    echo "  SELECT 'admin', PASSWORD('$MK'), '', NOW(), 0, 0, 0 FROM DUAL"
    echo "  WHERE @htbb_csdl_moi = 1 AND NOT EXISTS (SELECT 1 FROM \`account\`.\`team_user\` WHERE \`username\` = 'admin');"
    echo "INSERT INTO \`htbb_db\`.\`5h_active\` (\`userID\`, \`username\`, \`time_end\`)"
    echo "  SELECT u.\`id\`, u.\`username\`, '1894945812' FROM \`account\`.\`team_user\` u WHERE @htbb_csdl_moi = 1 AND u.\`username\` = 'admin'"
    echo "  AND NOT EXISTS (SELECT 1 FROM \`htbb_db\`.\`5h_active\` a WHERE a.\`username\` = 'admin');"
    echo
    sed -n "/^$MOC/,\$p" "$NGUON"
} > "$TAM"
grep -q "^CREATE TEMPORARY TABLE \`cau_hinh_danh_muc\`" "$TAM" && grep -q "^INSERT INTO \`cau_hinh_danh_muc\`" "$TAM" \
    && grep -q "^$MOC" "$TAM" || { echo "[LOI] ban xuat thieu cau_hinh_danh_muc / phan B - KHONG ghi de $RA"; exit 1; }
mv "$TAM" "$RA"
trap - EXIT
echo "Da ghi $RA ($(wc -c < "$RA") byte, $(grep -c '^CREATE TABLE IF NOT EXISTS' "$RA") lenh CREATE TABLE IF NOT EXISTS, du lieu $(echo $CO_DU_LIEU | wc -w) bang)"
