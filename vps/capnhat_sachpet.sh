#!/bin/bash
# ============================================================
#  CHUYEN "SACH KY NANG PET" SANG POTION 111
#
#  Dung o VPS:  sudo bash /root/capnhat_sachpet.sh
#
#  Thu tu QUAN TRONG - doi du lieu TRUOC roi moi lay code moi:
#    1. Tat game server (shutdown hook luu du lieu nhan vat xuong DB)
#    2. Sao luu cac bang bi dong toi
#    3. Doi du lieu: item 722 + gem 269  ->  potion 111
#    4. Doi thanh cong moi git pull (jar moi)
#    5. Bat lai server
#
#  Neu buoc 3 that bai: script DUNG LAI truoc khi pull va bat lai server
#  bang code CU, du lieu van nguyen ven.
# ============================================================
set -u

DIR=/opt/kpah
USER_SV=kpah
SV_GAME=kpah-game
MIGRATE=/root/migrate_sach_pet.php

if [ "$(id -u)" != "0" ]; then
    echo "[LOI] Phai chay bang root:  sudo bash $0"
    exit 1
fi

if [ ! -f "$MIGRATE" ]; then
    echo "[LOI] Khong thay $MIGRATE"
    exit 1
fi

echo "== Kiem tra repo tren VPS co sach khong (git pull se that bai neu ban)"
DIRTY=$(sudo -u "$USER_SV" git -C "$DIR" status --porcelain)
if [ -n "$DIRTY" ]; then
    echo "$DIRTY"
    echo "[DUNG LAI] Co file bi sua tay tren VPS. Xu ly xong roi chay lai."
    exit 1
fi

echo
echo "== B1. Tat $SV_GAME"
systemctl stop "$SV_GAME"
sleep 3
echo "   trang thai: $(systemctl is-active $SV_GAME)"
if [ "$(systemctl is-active $SV_GAME)" = "active" ]; then
    echo "[DUNG LAI] Server chua tat han. Khong doi du lieu luc server con chay."
    exit 1
fi

echo
echo "== B2. Sao luu cac bang bi dong toi"
mkdir -p "$DIR/backup"
BAK="$DIR/backup/truoc_sachpet_$(date +%Y%m%d_%H%M%S).sql"
if ! mysqldump -u root --default-character-set=utf8mb4 --single-transaction \
        kpah_db tob_char tob_gem_new data_item data_potion giftcode > "$BAK"; then
    echo "[DUNG LAI] Sao luu that bai. Bat lai server bang code cu."
    systemctl start "$SV_GAME"
    exit 1
fi
gzip -f "$BAK"
echo "   $BAK.gz  ($(du -h "$BAK.gz" | cut -f1))"

echo
echo "== B3. Chay thu (khong ghi gi)"
php "$MIGRATE" || { echo "[DUNG LAI] Chay thu bao loi."; systemctl start "$SV_GAME"; exit 1; }

echo
echo "== B4. Doi du lieu that"
if ! php "$MIGRATE" --apply; then
    echo
    echo "[DUNG LAI] Doi du lieu that bai. CHUA pull code moi."
    echo "  Phuc hoi:  gunzip -c $BAK.gz | mysql -u root kpah_db"
    echo "  Roi bat lai: systemctl start $SV_GAME"
    exit 1
fi

echo
echo "== B5. Lay code moi (jar da build san tren may nha)"
if ! sudo -u "$USER_SV" git -C "$DIR" pull --ff-only; then
    echo "[LOI] git pull that bai. Server VAN DANG TAT."
    echo "      Du lieu DA doi xong - dung phuc hoi backup nua."
    echo "      Sua git xong thi:  systemctl start $SV_GAME"
    exit 1
fi
chown -R "$USER_SV":"$USER_SV" "$DIR"

echo
echo "== B6. Bat lai server"
systemctl start "$SV_GAME"
sleep 8
systemctl --no-pager --lines=10 status "$SV_GAME" || true

echo
echo "== Cong 19129 phai dang LISTEN:"
ss -lntp | grep 19129 || echo "   [CANH BAO] chua thay cong 19129 - xem log: tail -f $DIR/log/game.out"

echo
echo "== Kiem tra lai du lieu"
mysql -u root --default-character-set=utf8mb4 -t <<'SQL'
SELECT id, idImage, isTrade, name, name2 FROM kpah_db.data_potion WHERE id=111;
SELECT COUNT(*) AS con_item_722 FROM kpah_db.data_item WHERE id=722;
SELECT charname,
       SUBSTRING_INDEX(SUBSTRING_INDEX(potion, ',', 112), ',', -1) AS so_sach
  FROM kpah_db.tob_char ORDER BY id;
SQL

echo
echo "== XONG."
