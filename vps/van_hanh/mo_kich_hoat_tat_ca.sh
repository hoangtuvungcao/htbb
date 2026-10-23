#!/bin/bash
# ============================================================
#  MO KICH HOAT cho MOI tai khoan: da dang ky + dang ky moi sau nay (open test).
#  Chay (root tren VPS):  sudo bash /opt/htbb/vps/van_hanh/mo_kich_hoat_tat_ca.sh
#  Dao nguoc:             sudo bash /opt/htbb/vps/van_hanh/khoa_kich_hoat_tat_ca.sh
#
#  1. Bo khoa tinh nang cho tai khoan chua kich hoat (kich_hoat.chuc_nang rong), khong chan dang nhap.
#  2. Kich hoat moi tai khoan trong account.team_user toi 01/01/2037 (khong xoa dong nao: UPDATE + INSERT).
#  3. Dang ky moi (web + trong game) tu kich hoat: HTBB_AUTO_ACTIVATE=true, restart htbb-web.
#  4. Cron moi 5 phut kich hoat tai khoan moi tao toi 01/01/2037 (tu kich hoat chi 30 ngay).
#  An toan khi chay lai nhieu lan. Server game doc lai cau_hinh trong ~10 giay, khong can restart.
# ============================================================
set -euo pipefail
[ "$(id -u)" = 0 ] || { echo "Can chay bang root (sudo)."; exit 1; }
ENV=/etc/htbb/htbb.env
DB_ACC=$(grep -E '^HTBB_DB_ACCOUNT=' "$ENV" | tail -1 | cut -d= -f2- | tr -d "\"' ")
DB_ACC=${DB_ACC:-account}
HAN=2114380800   # 01/01/2037 00:00 UTC - cung la dau nhan "kich hoat do script nay" (khoa_kich_hoat dung no)
CRON=/etc/cron.d/htbb-kich-hoat-tat-ca

echo "== Sao luu CSDL"
/usr/local/sbin/htbb-backup-db.sh >/dev/null
echo "   xong"

echo "== 1. Bo khoa tinh nang cho tai khoan chua kich hoat"
mysql htbb_db -e "
INSERT INTO cau_hinh (khoa,gia_tri,sua_boi,sua_luc) VALUES
 ('kich_hoat.chuc_nang','','admin-opentest',NOW()),
 ('kich_hoat.chan_dang_nhap','0','admin-opentest',NOW())
ON DUPLICATE KEY UPDATE gia_tri=VALUES(gia_tri), sua_boi=VALUES(sua_boi), sua_luc=VALUES(sua_luc);"

echo "== 2. Kich hoat moi tai khoan da dang ky (CSDL tai khoan: $DB_ACC)"
SQL_KICH_HOAT="
UPDATE htbb_db.5h_active a JOIN \`$DB_ACC\`.team_user u ON a.userID=u.id
   SET a.time_end='$HAN', a.username=u.username
 WHERE a.time_end='-1' OR CAST(a.time_end AS UNSIGNED) < $HAN;
INSERT INTO htbb_db.5h_active (userID,username,time_end)
SELECT u.id,u.username,'$HAN' FROM \`$DB_ACC\`.team_user u
 WHERE NOT EXISTS (SELECT 1 FROM htbb_db.5h_active a WHERE a.userID=u.id);"
mysql -e "$SQL_KICH_HOAT"

echo "== 3. Dang ky moi tu kich hoat"
if grep -q '^HTBB_AUTO_ACTIVATE=' "$ENV"; then
    sed -i 's/^HTBB_AUTO_ACTIVATE=.*/HTBB_AUTO_ACTIVATE=true/' "$ENV"
else
    echo 'HTBB_AUTO_ACTIVATE=true' >> "$ENV"
fi
systemctl restart htbb-web

echo "== 4. Cron 5 phut: tai khoan moi tao -> kich hoat toi 01/01/2037"
echo "$SQL_KICH_HOAT" > /etc/htbb/kich_hoat_tat_ca.sql
chmod 600 /etc/htbb/kich_hoat_tat_ca.sql
cat > "$CRON" <<'EOF'
# HTBB open test: kich hoat moi tai khoan moi (tao boi mo_kich_hoat_tat_ca.sh; khoa_kich_hoat_tat_ca.sh xoa file nay)
*/5 * * * * root mysql < /etc/htbb/kich_hoat_tat_ca.sql >/dev/null 2>&1
EOF
chmod 644 "$CRON"

sleep 15
echo "== Ket qua"
mysql -N htbb_db -e "
SELECT CONCAT('tai khoan: ', (SELECT COUNT(*) FROM \`$DB_ACC\`.team_user));
SELECT CONCAT('da kich hoat con han: ', COUNT(*)) FROM 5h_active WHERE time_end<>'-1' AND CAST(time_end AS UNSIGNED)>UNIX_TIMESTAMP();
SELECT CONCAT(khoa,' = \"',gia_tri,'\"') FROM cau_hinh WHERE khoa IN ('kich_hoat.chuc_nang','kich_hoat.chan_dang_nhap','web.autoActivate');"
systemctl is-active htbb-web htbb-game
