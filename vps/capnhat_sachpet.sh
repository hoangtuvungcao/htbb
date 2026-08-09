#!/bin/bash
# ============================================================
#  DOI DU LIEU "SACH KY NANG PET" SANG POTION 111
#
#  Dung o VPS:  sudo bash /root/capnhat_sachpet.sh
#
#  BOI CANH: code moi DA len VPS roi (GitHub Actions tu deploy khi push
#  len main, va vi KPAH2_patched.jar doi nen no da restart server luc
#  00:37). Nen o day KHONG git pull nua - chi con doi du lieu nguoi choi
#  va bat lai server de client nhan ten/icon cua potion 111.
#
#  Thu tu:
#    1. Tat game server  (SIGTERM -> shutdown hook luu du lieu xuong DB,
#       nguoi dang online bi ngat nhung khong mat do)
#    2. Sao luu cac bang bi dong toi
#    3. Chay thu, roi doi that: item 722 + gem 269  ->  potion 111
#    4. Bat lai server + kiem tra
#
#  Doi du lieu that bai thi script van bat lai server, va in san lenh
#  phuc hoi tu ban sao luu.
# ============================================================
set -u

DIR=/opt/kpah
SV_GAME=kpah-game
MIGRATE=/root/add_mo_ta_sach_pet.php

if [ "$(id -u)" != "0" ]; then
    echo "[LOI] Phai chay bang root:  sudo bash $0"
    exit 1
fi
if [ ! -f "$MIGRATE" ]; then
    echo "[LOI] Khong thay $MIGRATE"
    exit 1
fi

echo "== B1. Tat $SV_GAME"
echo "   (nguoi dang online se bi ngat, du lieu duoc luu truoc khi thoat)"
systemctl stop "$SV_GAME"
sleep 5
TT=$(systemctl is-active "$SV_GAME")
echo "   trang thai: $TT"
if [ "$TT" = "active" ]; then
    echo "[DUNG LAI] Server chua tat han. Khong doi du lieu luc server con chay,"
    echo "           vi server giu du lieu nhan vat trong RAM va se ghi de."
    exit 1
fi

echo
echo "== B2. Sao luu cac bang bi dong toi"
mkdir -p "$DIR/backup"
BAK="$DIR/backup/truoc_sachpet_$(date +%Y%m%d_%H%M%S).sql"
if ! mysqldump -u root --default-character-set=utf8mb4 --single-transaction \
        kpah_db tob_char tob_gem_new data_item data_potion giftcode > "$BAK"; then
    echo "[DUNG LAI] Sao luu that bai. Bat lai server, khong doi gi ca."
    systemctl start "$SV_GAME"
    exit 1
fi
gzip -f "$BAK"
echo "   $BAK.gz  ($(du -h "$BAK.gz" | cut -f1))"

echo
echo "== B3. Chay thu (khong ghi gi)"
if ! php "$MIGRATE"; then
    echo "[DUNG LAI] Chay thu bao loi. Bat lai server, khong doi gi ca."
    systemctl start "$SV_GAME"
    exit 1
fi

echo
echo "== B4. Doi du lieu that"
if php "$MIGRATE" --apply; then
    KQ=0
else
    KQ=1
    echo
    echo "[LOI] Doi du lieu that bai hoac chi xong mot phan."
    echo "      Phuc hoi:  gunzip -c $BAK.gz | mysql -u root kpah_db"
fi

echo
echo "== B5. Bat lai server"
systemctl start "$SV_GAME"
sleep 8
systemctl --no-pager --lines=8 status "$SV_GAME" || true
echo
ss -lntp | grep 19129 || echo "   [CANH BAO] chua thay cong 19129 - xem log: tail -f $DIR/log/game.out"

echo
echo "== B6. Kiem tra lai"
mysql -u root --default-character-set=utf8mb4 -t <<'SQL'
SELECT id, idImage, delay, isTrade, name, name2 FROM kpah_db.data_potion WHERE id=111;
SELECT COUNT(*) AS data_item_722_con_lai FROM kpah_db.data_item WHERE id=722;
SELECT charname,
       SUBSTRING_INDEX(SUBSTRING_INDEX(potion, ',', 112), ',', -1) AS so_sach_pet
  FROM kpah_db.tob_char ORDER BY id;
SELECT giftcode, item FROM kpah_db.giftcode WHERE giftcode LIKE 'sachpet%';
SQL

echo
if [ "$KQ" = "0" ]; then
    echo "== XONG. Vao game kiem tra: sach nam o tui binh (khong phai hanh trang),"
    echo "   xep chong, bam Su dung khi dang mang pet."
else
    echo "== KET THUC nhung CO LOI o buoc doi du lieu - xem lai phia tren."
fi
exit "$KQ"
