#!/bin/bash
# ============================================================
#  DEPLOY TU DONG - GitHub Actions ssh vao goi file nay sau moi push len main
#
#  Chay tay cung duoc:  sudo /opt/kpah/vps/deploy-auto.sh
#  Log rieng:           /opt/kpah/log/autodeploy.log
#
#  Khac vps/update.sh o hai diem:
#    1. KHONG restart neu commit moi chi doi file khong anh huong server dang chay
#       (.md, .sql, src/*.java...). Push tai lieu thi khong ai bi kick.
#    2. Neu PHAI restart: bat co 5h_systems.baotri_thongbao=0 de server chay marquee
#       dem nguoc 60 giay cho nguoi dang online, roi moi systemctl stop.
#
#  CAU HINH: /opt/kpah/server.ini duoc SINH RA tu vps/server.ini.tmpl (co trong git)
#  cong voi bi mat GitHub gui qua stdin. Xem vps/sinh-server-ini.sh. Cau hinh doi
#  thi cung phai restart, ke ca khi khong file nao khac doi.
#
#  Thu tu khi phai restart:
#    bao truoc 60s -> systemctl stop (SIGTERM -> shutdown hook luu du lieu)
#    -> git pull --ff-only -> sinh server.ini -> kiem jar -> bat lai -> kiem cong 19129
# ============================================================
set -u

DIR=/opt/kpah
USER_SV=kpah
SV_GAME=kpah-game
SV_LOGIN=kpah-login
LOG=$DIR/log/autodeploy.log
GIAY_BAO_TRUOC=60

# Nhung duong dan ma khi doi thi BUOC phai restart moi co tac dung.
# src/*.java KHONG nam trong day: server chay jar, sua .java ma chua build lai thi
# restart cung vo nghia.
CAN_RESTART_REGEX='^(KPAH2_patched\.jar|lib/|map/|cMap/|loginServer/|vps/server\.ini\.tmpl|vps/sinh-server-ini\.sh)'

# ---- Bi mat tu GitHub, gui qua STDIN ----
# Khoa SSH cua Actions bi ep chay dung lenh nay (command="..." trong authorized_keys)
# va sshd chi AcceptEnv LANG LC_*, nen khong truyen duoc bien moi truong. Stdin thi
# qua duoc. Doc TRUOC moi thu khac, va khong bao gio in gia tri ra log.
doc_bi_mat() {
    [ -t 0 ] && return 0          # chay tay tu terminal -> khong doc, tranh treo
    local k v
    while IFS='=' read -r k v; do
        v=${v%$'\r'}
        case "$k" in
            DB_USER|DB_PASSWORD|DB_USER_NAP|DB_PASS_NAP|DB_USER1|DB_PASSWORD1)
                export "$k=$v" ;;
        esac
    done
}
doc_bi_mat

mkdir -p "$DIR/log"
# Vua ghi log tren VPS vua tra ve stdout, de xem duoc ngay trong log cua GitHub Actions.
exec > >(tee -a "$LOG") 2>&1
echo
echo "===== $(date '+%F %T') deploy tu dong"

# Khong cho hai lan deploy chay chong nhau (push lien tiep 2 commit).
exec 9>/tmp/kpah-deploy.lock
if ! flock -n 9; then
    echo "Dang co mot lan deploy khac chay, bo qua lan nay."
    exit 0
fi

cd "$DIR" || { echo "[LOI] Khong vao duoc $DIR"; exit 1; }

# SSH bi ngat giua duong (SIGHUP) khong duoc de server nam tat.
CAN_BAT_LAI=0
trap 'echo "[NGAT] bi ngat giua duong"; [ "$CAN_BAT_LAI" = 1 ] && systemctl start $SV_GAME; exit 1' HUP INT TERM PIPE

# Sinh lai server.ini. Tra ve: 0 khong doi, 3 da doi (phai restart), 1 loi.
sinh_cau_hinh() {
    "$DIR/vps/sinh-server-ini.sh"
    return $?
}

# Tat -> (pull neu can) -> sinh cau hinh -> kiem jar -> bat lai -> doi cong 19129.
khoi_dong_lai() {
    local can_pull=$1 sha_cu=$2

    echo "== Bao truoc bao tri ${GIAY_BAO_TRUOC}s cho nguoi dang online"
    # Server doc co nay moi 5 giay (BaoTriDaily -> Database.baoTriThongBaoCMD) roi chay
    # marquee dem nguoc. Thieu dong nay trong DB thi bo qua, khong sao.
    if ! mysql -u root -e "UPDATE kpah_db.5h_systems SET status = 0 WHERE command = 'baotri_thongbao';" 2>/dev/null; then
        echo "   [CANH BAO] khong dat duoc co bao tri, van tiep tuc (nguoi choi se bi out khong bao truoc)"
    fi
    sleep "$GIAY_BAO_TRUOC"

    echo "== Tat $SV_GAME (toi da 180s de luu du lieu)"
    CAN_BAT_LAI=1
    systemctl stop "$SV_GAME"

    if [ "$can_pull" = 1 ]; then
        echo "== Keo code moi"
        if ! sudo -u "$USER_SV" git -C "$DIR" pull --ff-only; then
            echo "[LOI] git pull that bai. Bat lai ban cu de khong mat server."
            systemctl start "$SV_GAME"
            exit 1
        fi
        chown -R "$USER_SV":"$USER_SV" "$DIR"
        chmod +x "$DIR"/vps/*.sh 2>/dev/null

        echo "== Sinh lai server.ini tu ban mau"
        sinh_cau_hinh
        if [ $? = 1 ]; then
            echo "[LOI] cau hinh moi khong dung duoc. Bat lai voi server.ini cu."
            systemctl start "$SV_GAME"
            exit 1
        fi
    fi

    echo "== Kiem tra jar"
    if command -v unzip >/dev/null 2>&1; then
        if ! unzip -t "$DIR/KPAH2_patched.jar" >/dev/null 2>&1; then
            echo "[LOI] KPAH2_patched.jar hong. Server VAN DANG TAT - thua tat con hon chay ban hong."
            echo "      Lay lai ban truoc:  sudo -u $USER_SV git -C $DIR checkout $sha_cu -- KPAH2_patched.jar && systemctl start $SV_GAME"
            exit 1
        fi
    fi

    chown -R "$USER_SV":"$USER_SV" "$DIR"
    chmod +x "$DIR"/vps/*.sh 2>/dev/null

    echo "== Bat lai"
    systemctl restart "$SV_LOGIN"
    sleep 2
    systemctl start "$SV_GAME"
    CAN_BAT_LAI=0
    trap - HUP INT TERM PIPE

    for i in $(seq 1 20); do
        sleep 3
        if ss -lntp 2>/dev/null | grep -q 19129; then
            echo "== XONG: cong 19129 dang LISTEN, commit $(sudo -u "$USER_SV" git -C "$DIR" log -1 --format='%h %s')"
            exit 0
        fi
    done

    echo "[CANH BAO] sau 60s chua thay cong 19129. Xem log:  tail -n 100 $DIR/log/game.out"
    exit 1
}

echo "== Kiem tra repo co bi sua tay khong"
# Bo qua file tam CHUA DUOC THEO DOI cua trinh soan thao (nano/vim). Mot phien nano
# dang mo /opt/kpah/server.ini de lai .server.ini.swp, va the la ca deploy dung lai
# du khong ai sua gi that. Da vap 29/08 (web/config.local.php) va 30/08/2026
# (server.ini). Dong nay chi bo qua dong "??" - file da theo doi ma bi sua van chan.
DIRTY=$(sudo -u "$USER_SV" git -C "$DIR" status --porcelain \
        | grep -vE '^\?\? .*(\.swp|\.swo|\.sw[a-p]|~)$')
if [ -n "$DIRTY" ]; then
    echo "$DIRTY"
    echo "[DUNG LAI] Co file bi sua tay tren VPS, git pull se that bai."
    echo "  Bo het thay doi tren VPS:  sudo -u $USER_SV git -C $DIR fetch origin && sudo -u $USER_SV git -C $DIR reset --hard origin/main"
    exit 1
fi

if ! sudo -u "$USER_SV" git -C "$DIR" fetch --quiet origin; then
    echo "[LOI] git fetch that bai (mang / deploy key?). Server van chay binh thuong."
    exit 1
fi

CU=$(sudo -u "$USER_SV" git -C "$DIR" rev-parse HEAD)
MOI=$(sudo -u "$USER_SV" git -C "$DIR" rev-parse origin/main)

if [ "$CU" = "$MOI" ]; then
    echo "Khong co commit moi ($(echo "$CU" | cut -c1-7))."
    # Bi mat tren GitHub co the doi ma KHONG co commit nao (workflow_dispatch).
    # Van phai thu sinh lai cau hinh de thay doi do di vao duoc.
    sinh_cau_hinh
    RC=$?
    [ "$RC" = 1 ] && exit 1
    [ "$RC" = 0 ] && { echo "Cau hinh cung khong doi. Khong lam gi."; exit 0; }
    echo "== Cau hinh doi -> phai khoi dong lai"
    khoi_dong_lai 0 "$CU"
fi

echo "== $(echo "$CU" | cut -c1-7) -> $(echo "$MOI" | cut -c1-7), file doi:"
DOI=$(sudo -u "$USER_SV" git -C "$DIR" diff --name-only "$CU" "$MOI")
echo "$DOI" | sed 's/^/   /'

if ! echo "$DOI" | grep -qE "$CAN_RESTART_REGEX"; then
    echo "== Khong co file nao doi hoi restart -> chi keo code"
    sudo -u "$USER_SV" git -C "$DIR" pull --ff-only || { echo "[LOI] git pull that bai."; exit 1; }
    chown -R "$USER_SV":"$USER_SV" "$DIR"
    chmod +x "$DIR"/vps/*.sh 2>/dev/null

    # Ban mau khong doi thi buoc nay thuong tra ve 0 ngay. Van phai goi, vi bi mat
    # tren GitHub co the doi trong cung lan push do.
    sinh_cau_hinh
    RC=$?
    [ "$RC" = 1 ] && exit 1
    if [ "$RC" = 0 ]; then
        echo "   xong, server khong bi gian doan"
        exit 0
    fi
    echo "== Nhung cau hinh doi -> van phai khoi dong lai"
    khoi_dong_lai 0 "$CU"
fi

khoi_dong_lai 1 "$CU"
