#!/bin/bash
# ============================================================
#  CAP NHAT SERVER HTBB TREN VPS  (ban Linux cua capnhat.bat)
#
#  Dung o VPS:  sudo /opt/kpah/vps/capnhat.sh
#
#  Dung khi muon thu nhanh mot jar chua commit (scp truc tiep len VPS).
#  Deploy binh thuong thi dung vps/update.sh (keo tu git).
#
#  Viec no lam, dung thu tu:
#    1. Kiem tra co HTBB_NEW.jar chua
#    2. Tat game server co trat tu (SIGTERM -> shutdown hook luu du lieu)
#    3. Backup jar dang chay
#    4. Thay jar moi
#    5. Bat lai
# ============================================================
set -u

DIR=/opt/kpah
NEW="$DIR/HTBB_NEW.jar"
CUR="$DIR/KPAH2_patched.jar"
BAK="$DIR/KPAH2_patched_backup_truoc_capnhat.jar"
SV=kpah-game

if [ ! -f "$NEW" ]; then
    echo "[LOI] Khong thay $NEW"
    echo "      Upload jar moi tu may nha truoc:"
    echo "      scp HTBB_NEW.jar kpah@180.93.57.30:/opt/kpah/"
    exit 1
fi

echo "== Kiem tra jar moi co hop le khong"
# Thieu lenh unzip thi "unzip -t" that bai -> ket luan oan la jar hong. Kiem tra truoc.
if command -v unzip >/dev/null 2>&1; then
    if ! unzip -t "$NEW" >/dev/null 2>&1; then
        echo "[LOI] $NEW hong (upload dut giua duong?). Huy cap nhat."
        exit 1
    fi
elif command -v jar >/dev/null 2>&1; then
    if ! jar tf "$NEW" >/dev/null 2>&1; then
        echo "[LOI] $NEW hong (upload dut giua duong?). Huy cap nhat."
        exit 1
    fi
else
    echo "   [CANH BAO] khong co 'unzip' -> bo qua buoc kiem tra jar (apt install -y unzip)"
fi

echo "== Tat $SV (cho toi da 180s de no luu du lieu)"
systemctl stop "$SV"

echo "== Backup jar dang chay"
if [ -f "$CUR" ]; then
    cp -f "$CUR" "$BAK"
    echo "   ban cu: $BAK"
fi

echo "== Thay jar"
mv -f "$NEW" "$CUR"
chown kpah:kpah "$CUR"

echo "== Bat lai $SV"
systemctl start "$SV"
sleep 5
systemctl --no-pager --lines=15 status "$SV"

echo
echo "== Cong 19129 phai dang LISTEN:"
ss -lntp | grep 19129 || echo "   [CANH BAO] chua thay cong 19129 - xem log: tail -f /opt/kpah/log/game.out"
