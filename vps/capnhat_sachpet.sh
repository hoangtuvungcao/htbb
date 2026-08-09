#!/bin/bash
# ============================================================
#  SUA MO TA "SACH KY NANG PET" (data_potion id=111)
#
#  Dung o VPS:  sudo bash /root/capnhat_sachpet.sh
#
#  Day CHI la sua ten/mo ta hien thi (1 dong trong data_potion) - KHONG
#  can tat server. Server doc DB moi khi hien tui binh nen chi can nap
#  lai bang mot trong hai cach:
#    - Dang nhap admin trong game, go lenh chat:  loadpotion
#    - Hoac cu de vay, lan restart tiep theo (deploy code, bao tri...)
#      se tu nhan gia tri moi.
#
#  Thu tu:
#    1. Sao luu rieng bang data_potion (nho, nhanh, khong dong toi ai)
#    2. Chay thu (khong ghi gi)
#    3. Ghi that
#    4. In lai dong 111 de doi chieu
# ============================================================
set -u

DIR=/opt/kpah
MIGRATE=/root/add_mo_ta_sach_pet.php

if [ ! -f "$MIGRATE" ]; then
    echo "[LOI] Khong thay $MIGRATE"
    exit 1
fi

echo "== B1. Sao luu rieng bang data_potion"
mkdir -p "$DIR/backup"
BAK="$DIR/backup/truoc_sua_mota_sachpet_$(date +%Y%m%d_%H%M%S).sql"
if ! mysqldump -u root --default-character-set=utf8mb4 kpah_db data_potion > "$BAK"; then
    echo "[DUNG LAI] Sao luu that bai, khong doi gi ca."
    exit 1
fi
gzip -f "$BAK"
echo "   $BAK.gz"

echo
echo "== B2. Chay thu (khong ghi gi)"
php "$MIGRATE" || { echo "[DUNG LAI] Chay thu bao loi."; exit 1; }

echo
echo "== B3. Ghi that"
if ! php "$MIGRATE" --apply; then
    echo "[LOI] Ghi that bai."
    echo "      Phuc hoi:  gunzip -c $BAK.gz | mysql -u root kpah_db"
    exit 1
fi

echo
echo "== B4. Doi chieu lai trong DB"
mysql -u root --default-character-set=utf8mb4 -t -e \
  "SELECT id, idImage, delay, isTrade, name, name2 FROM kpah_db.data_potion WHERE id=111;"

echo
echo "== XONG. Server KHONG bi tat."
echo "   De thay ngay trong game: dang nhap admin, go lenh chat 'loadpotion'."
echo "   Khong go lenh thi lan server restart tiep theo se tu nhan gia tri moi."
