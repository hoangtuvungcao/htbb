#!/bin/bash
# ============================================================
#  CAP NHAT SERVER HTBB TU GIT
#
#  Dung o VPS:  sudo /opt/kpah/vps/update.sh
#
#  Luong lam viec:
#     May nha: build jar -> chay capnhat.bat (de KPAH2_patched.jar la ban moi)
#              -> git add -A && git commit && git push
#     VPS:     sudo /opt/kpah/vps/update.sh
#
#  Script lam:
#    1. Kiem tra repo co bi sua tay khong (tranh git pull that bai giua duong)
#    2. Tat game server co trat tu (SIGTERM -> shutdown hook luu du lieu)
#    3. git pull --ff-only
#    4. Kiem tra jar vua keo ve co hong khong
#    5. Bat lai + bao trang thai
#
#  KHONG bao gio de len: server.ini, loginServer/server.ini, log/, backup/
#  (deu nam trong .gitignore).
# ============================================================
set -u

DIR=/opt/kpah
USER_SV=kpah
SV_GAME=kpah-game
SV_LOGIN=kpah-login

cd "$DIR" || { echo "[LOI] Khong vao duoc $DIR"; exit 1; }

echo "== Nhanh hien tai"
sudo -u "$USER_SV" git -C "$DIR" rev-parse --abbrev-ref HEAD

echo "== Kiem tra file bi sua tay tren VPS"
DIRTY=$(sudo -u "$USER_SV" git -C "$DIR" status --porcelain)
if [ -n "$DIRTY" ]; then
    echo "$DIRTY"
    echo
    echo "[DUNG LAI] Co file bi sua tay tren VPS, git pull se that bai."
    echo "  - Neu day la file bo di:  sudo -u $USER_SV git -C $DIR checkout -- <file>"
    echo "  - Neu muon lay het theo git, BO moi thay doi tren VPS:"
    echo "      sudo -u $USER_SV git -C $DIR fetch origin"
    echo "      sudo -u $USER_SV git -C $DIR reset --hard origin/main"
    echo "    (server.ini va log/ khong bi anh huong vi nam trong .gitignore)"
    exit 1
fi

echo "== Tat $SV_GAME (cho toi da 180s de luu du lieu)"
systemctl stop "$SV_GAME"

echo "== Keo code moi"
if ! sudo -u "$USER_SV" git -C "$DIR" pull --ff-only; then
    echo "[LOI] git pull that bai. Server VAN DANG TAT."
    echo "      Sua xong thi bat lai: systemctl start $SV_GAME"
    exit 1
fi

echo "== Kiem tra jar"
# Phai kiem tra co lenh unzip khong. Thieu unzip thi "unzip -t" that bai va script
# se ket luan oan la jar hong.
if command -v unzip >/dev/null 2>&1; then
    if ! unzip -t "$DIR/KPAH2_patched.jar" >/dev/null 2>&1; then
        echo "[LOI] KPAH2_patched.jar hong. Server VAN DANG TAT."
        exit 1
    fi
elif command -v jar >/dev/null 2>&1; then
    if ! jar tf "$DIR/KPAH2_patched.jar" >/dev/null 2>&1; then
        echo "[LOI] KPAH2_patched.jar hong. Server VAN DANG TAT."
        exit 1
    fi
else
    echo "   [CANH BAO] khong co 'unzip' -> bo qua buoc kiem tra jar."
    echo "              Cai cho lan sau:  apt install -y unzip"
fi
echo "   jar OK: $(sudo -u "$USER_SV" git -C "$DIR" log -1 --format='%h %s')"

chown -R "$USER_SV":"$USER_SV" "$DIR"
chmod +x "$DIR"/vps/*.sh 2>/dev/null

echo "== Bat lai"
systemctl restart "$SV_LOGIN"
sleep 2
systemctl start "$SV_GAME"
sleep 6
systemctl --no-pager --lines=12 status "$SV_GAME"

echo
echo "== Cong 19129 phai dang LISTEN:"
ss -lntp | grep 19129 || echo "   [CANH BAO] chua thay cong 19129 - xem log: tail -f $DIR/log/game.out"
