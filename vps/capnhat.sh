#!/bin/bash
# ============================================================
#  CAP NHAT SERVER KPAH TREN VPS  (ban Linux cua capnhat.bat)
#
#  Dung o VPS:  sudo /opt/kpah/capnhat.sh
#
#  Viec no lam, dung thu tu:
#    1. Kiem tra co KPAH2_NEW.jar chua
#    2. Tat game server co trat tu (SIGTERM -> shutdown hook luu du lieu)
#    3. Backup jar dang chay
#    4. Thay jar moi
#    5. Bat lai
# ============================================================
set -u

DIR=/opt/kpah
NEW="$DIR/KPAH2_NEW.jar"
CUR="$DIR/KPAH2_patched.jar"
BAK="$DIR/KPAH2_patched_backup_truoc_capnhat.jar"
SV=kpah-game

if [ ! -f "$NEW" ]; then
    echo "[LOI] Khong thay $NEW"
    echo "      Upload jar moi tu may nha truoc:"
    echo "      scp KPAH2_NEW.jar kpah@180.93.57.30:/opt/kpah/"
    exit 1
fi

echo "== Kiem tra jar moi co hop le khong"
if ! unzip -t "$NEW" >/dev/null 2>&1; then
    echo "[LOI] $NEW hong (upload dut giua duong?). Huy cap nhat."
    exit 1
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
