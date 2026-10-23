#!/bin/bash
# ============================================================
#  KHOA KICH HOAT (dao nguoc mo_kich_hoat_tat_ca.sh): tai khoan chua kich hoat bi khoa lai tinh nang,
#  dang ky moi KHONG tu kich hoat nua.
#  Chay (root tren VPS):  sudo bash /opt/htbb/vps/van_hanh/khoa_kich_hoat_tat_ca.sh
#        TAT_CA=1 sudo bash ...   -> huy kich hoat CA tai khoan tu kich hoat qua web (mac dinh chi huy cac
#                                    dong do mo_kich_hoat_tat_ca.sh tao, han 01/01/2037)
#
#  1. Xoa cron kich hoat tai khoan moi.
#  2. Dang ky moi khong tu kich hoat: HTBB_AUTO_ACTIVATE=false, restart htbb-web.
#  3. Khoa lai tinh nang cho tai khoan chua kich hoat (giao dich, chat the gioi, cho, chuyen luong, nhan tang).
#  4. Huy kich hoat (time_end = '-1', khong xoa dong).
# ============================================================
set -euo pipefail
[ "$(id -u)" = 0 ] || { echo "Can chay bang root (sudo)."; exit 1; }
ENV=/etc/htbb/htbb.env
HAN=2114380800
TAT_CA=${TAT_CA:-0}

echo "== Sao luu CSDL"
/usr/local/sbin/htbb-backup-db.sh >/dev/null
echo "   xong"

echo "== 1. Xoa cron kich hoat tai khoan moi"
rm -f /etc/cron.d/htbb-kich-hoat-tat-ca /etc/htbb/kich_hoat_tat_ca.sql

echo "== 2. Dang ky moi khong tu kich hoat"
if grep -q '^HTBB_AUTO_ACTIVATE=' "$ENV"; then
    sed -i 's/^HTBB_AUTO_ACTIVATE=.*/HTBB_AUTO_ACTIVATE=false/' "$ENV"
else
    echo 'HTBB_AUTO_ACTIVATE=false' >> "$ENV"
fi
systemctl restart htbb-web

echo "== 3. Khoa tinh nang cho tai khoan chua kich hoat"
mysql htbb_db -e "
INSERT INTO cau_hinh (khoa,gia_tri,sua_boi,sua_luc) VALUES
 ('kich_hoat.chuc_nang','giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang','admin-opentest',NOW()),
 ('kich_hoat.chan_dang_nhap','0','admin-opentest',NOW())
ON DUPLICATE KEY UPDATE gia_tri=VALUES(gia_tri), sua_boi=VALUES(sua_boi), sua_luc=VALUES(sua_luc);"

echo "== 4. Huy kich hoat"
if [ "$TAT_CA" = 1 ]; then
    mysql htbb_db -e "UPDATE 5h_active SET time_end='-1';"
else
    mysql htbb_db -e "UPDATE 5h_active SET time_end='-1' WHERE time_end='$HAN';"
fi

sleep 15
echo "== Ket qua"
mysql -N htbb_db -e "
SELECT CONCAT('da kich hoat con han: ', COUNT(*)) FROM 5h_active WHERE time_end<>'-1' AND CAST(time_end AS UNSIGNED)>UNIX_TIMESTAMP();
SELECT CONCAT(khoa,' = \"',gia_tri,'\"') FROM cau_hinh WHERE khoa IN ('kich_hoat.chuc_nang','kich_hoat.chan_dang_nhap','web.autoActivate');"
systemctl is-active htbb-web htbb-game
