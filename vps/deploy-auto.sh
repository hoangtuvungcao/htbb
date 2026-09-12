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
#    2. Neu PHAI restart va KHONG co DEPLOY_NOW: KHONG tu tat may. Xem "GIO RESTART"
#       ben duoi - day la thay doi 12/09/2026, doc ky truoc khi sua tiep file nay.
#
#  CAU HINH: /opt/kpah/server.ini duoc SINH RA tu vps/server.ini.tmpl (co trong git)
#  cong voi bi mat GitHub gui qua stdin. Xem vps/sinh-server-ini.sh. Cau hinh doi
#  cung tinh la "phai restart", giong nhu file thuoc CAN_RESTART_REGEX.
#
#  ==================== GIO RESTART (12/09/2026) ====================
#  Truoc day: co file doi hoi restart la BAO 60s -> systemctl stop -> pull -> bat
#  lai NGAY LAP TUC, bat ke gio nao trong ngay. Van de: server con tu bao tri rieng
#  moi ngay vao mot gio ngau nhien 4:30-5:59 (src/real/BaoTriDaily.java, tu random
#  lai gio moi lan JVM khoi dong). Vua push code vua trung ngay la nguoi choi bi
#  gian doan HAI LAN/ngay (mot luc deploy, mot luc 4-6h sang).
#
#  Gio: TACH RIENG "keo code" khoi "ap dung (restart)".
#    - Keo code + sinh cau hinh + kiem jar: LUON LAM NGAY khi co push, bat ke co
#      phai restart hay khong. Khong dung toi server dang chay (file moi nam yen
#      tren dia, JVM dang chay van dung ban jar CU da nap vao bo nho cho toi khi
#      no tu thoat that su - xem giai thich trong commit gan chu thich nay).
#    - Neu PHAI restart va KHONG co DEPLOY_NOW=1: KHONG dung dich vu. Chi ghi log
#      "code da san sang, se ap dung vao lan bao tri tu dong tiep theo". Lan
#      BaoTriDaily tu thoat (gio ngau nhien, hoac ai do bam nut "Bao Tri" tren web
#      admin) se duoc systemd (Restart=always) khoi dong lai VOI CODE DA PULL SAN
#      - do chinh la luc "deploy" thuc su duoc ap dung, trung voi gio bao tri.
#      BaoTriDaily.java (12/09/2026) da duoc sua de tu canh bao 60s truoc gio bao
#      tri ngau nhien, nen nguoi choi khong mat canh bao ho dang duoc huong hom nay.
#    - Neu can deploy KHAN (khong doi duoc toi gio bao tri): dat DEPLOY_NOW=1 (qua
#      stdin, giong bi mat DB - xem doc_bi_mat) roi chay lai script nay, hoac dung
#      workflow_dispatch tren GitHub Actions va tick "Deploy ngay". Luc do luong
#      CU (bao 60s -> systemctl stop -> bat lai -> kiem cong 19129) chay y nguyen
#      nhu truoc 12/09/2026.
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
#
# DEPLOY_NOW khong phai bi mat (khong nhay cam) nhung di chung duong nay vi day la
# duong DUY NHAT truyen duoc "bien moi truong" qua khoa SSH bi ep lenh.
doc_bi_mat() {
    [ -t 0 ] && return 0          # chay tay tu terminal -> khong doc, tranh treo
    local k v
    while IFS='=' read -r k v; do
        v=${v%$'\r'}
        case "$k" in
            DB_USER|DB_PASSWORD|DB_USER_NAP|DB_PASS_NAP|DB_USER1|DB_PASSWORD1|DEPLOY_NOW)
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

# SSH bi ngat giua duong (SIGHUP) khong duoc de server nam tat. Chi co tac dung
# trong nhanh DEPLOY_NOW=1 (nhanh duy nhat con tu goi systemctl stop).
CAN_BAT_LAI=0
trap 'echo "[NGAT] bi ngat giua duong"; [ "$CAN_BAT_LAI" = 1 ] && systemctl start $SV_GAME; exit 1' HUP INT TERM PIPE

# Sinh lai server.ini. Tra ve: 0 khong doi, 3 da doi (phai restart), con lai = loi.
#
# Goi qua "bash" chu khong chay truc tiep: file them moi tu Windows de mat bit thuc
# thi, luc do vo shell tra ve 126. Ngay 30/08/2026 dinh dung cai do - 126 khong khop
# nhanh loi nao nen bi hieu nham thanh "cau hinh doi" va server bi tat oan.
sinh_cau_hinh() {
    bash "$DIR/vps/sinh-server-ini.sh"
    return $?
}

# Chi 3 moi la "da doi". MOI ma khac 0 va 3 deu la loi, khong duoc doan mo.
la_loi_sinh() {
    [ "$1" != 0 ] && [ "$1" != 3 ]
}

# Bao 60s -> systemctl stop -> bat lai -> doi cong 19129. CHI goi khi DEPLOY_NOW=1:
# code/cau hinh/jar coi nhu da san sang tren dia TU TRUOC (xem than script ben duoi),
# ham nay chi lo phan "ap dung ngay" thay vi cho gio bao tri.
restart_ngay() {
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

CO_COMMIT_MOI=0
DOI=""
if [ "$CU" = "$MOI" ]; then
    echo "Khong co commit moi ($(echo "$CU" | cut -c1-7))."
else
    CO_COMMIT_MOI=1
    echo "== $(echo "$CU" | cut -c1-7) -> $(echo "$MOI" | cut -c1-7), file doi:"
    DOI=$(sudo -u "$USER_SV" git -C "$DIR" diff --name-only "$CU" "$MOI")
    echo "$DOI" | sed 's/^/   /'
fi

CAN_RESTART=0
if [ "$CO_COMMIT_MOI" = 1 ] && echo "$DOI" | grep -qE "$CAN_RESTART_REGEX"; then
    CAN_RESTART=1
fi

# ---- Keo code + sinh cau hinh + kiem jar NGAY - khong dung toi server dang chay ----
if [ "$CO_COMMIT_MOI" = 1 ]; then
    echo "== Keo code moi (khong dung server)"
    if ! sudo -u "$USER_SV" git -C "$DIR" pull --ff-only; then
        echo "[LOI] git pull that bai."
        exit 1
    fi

    echo "== Kiem tra jar"
    if command -v unzip >/dev/null 2>&1; then
        if ! unzip -t "$DIR/KPAH2_patched.jar" >/dev/null 2>&1; then
            echo "[LOI] KPAH2_patched.jar hong sau khi pull. Server VAN DANG CHAY BAN CU"
            echo "      (chua he dung toi) - phuc hoi ngay de khong ai lo phai bao tri xong roi moi vo"
            echo "      chay ban hong."
            sudo -u "$USER_SV" git -C "$DIR" checkout "$CU" -- KPAH2_patched.jar
            chown -R "$USER_SV":"$USER_SV" "$DIR"
            exit 1
        fi
    fi
fi

echo "== Sinh lai server.ini tu ban mau"
# Ban mau khong doi thi buoc nay thuong tra ve 0 ngay. Van phai goi du khong co
# commit moi, vi bi mat tren GitHub co the doi ma khong kem commit nao.
sinh_cau_hinh
RC=$?
if la_loi_sinh "$RC"; then
    echo "[LOI] sinh cau hinh that bai (ma thoat $RC). KHONG dong toi server."
    chown -R "$USER_SV":"$USER_SV" "$DIR"
    exit 1
fi
[ "$RC" = 3 ] && CAN_RESTART=1

# MOT LAN sau ca hai buoc tren, bat ke co commit moi va/hoac cau hinh doi hay khong -
# server.ini vua sinh lai (thuong tren) va file vua pull (neu co) deu phai thuoc ve
# $USER_SV, khong thi kpah-game.service (User=kpah) khong doc/ghi duoc lan sau.
chown -R "$USER_SV":"$USER_SV" "$DIR"
chmod +x "$DIR"/vps/*.sh 2>/dev/null

# DEPLOY_NOW=1 la lenh "restart NGAY", doc lap voi CAN_RESTART: mot lan chay TRUOC co
# the da pull code roi ma CHUA restart (dung y thiet ke - xem GIO RESTART tren dau
# file), nen lan chay NAY (vd. deploy-auto.sh chay tay lai de ep len ngay) hoan toan co
# the thay CU = MOI (khong con commit "moi" nao vi da pull tu truoc) va CAN_RESTART=0 -
# neu chi doc CAN_RESTART se ket luan nham "khong co gi de lam" va bo qua yeu cau ep.
if [ "${DEPLOY_NOW:-}" = 1 ]; then
    if [ "$CAN_RESTART" = 0 ]; then
        echo "== DEPLOY_NOW=1: khong co gi moi/can restart, nhung van ep ap dung theo yeu cau"
    else
        echo "== DEPLOY_NOW=1: ep ap dung ngay, khong cho gio bao tri"
    fi
    restart_ngay
fi

if [ "$CAN_RESTART" = 0 ]; then
    if [ "$CO_COMMIT_MOI" = 1 ]; then
        echo "== Khong co file nao doi hoi restart -> xong, server khong bi gian doan"
    else
        echo "Cau hinh cung khong doi. Khong lam gi."
    fi
    exit 0
fi

# Doc gio bao tri ngau nhien gan nhat ma BaoTriDaily da tu random va in ra luc JVM
# hien tai khoi dong (xem src/real/BaoTriDaily.java). Chi de tham khao trong log -
# khong dung de dieu khien logic, BaoTriDaily tu lo lay gio no da chon.
GIO_DU_KIEN=$(grep -o 'Thời gian bảo trì được set: [0-9]*:[0-9]*' "$DIR/log/game.out" 2>/dev/null \
              | tail -1 | sed 's/^.*: //')
echo "== Code moi da san sang tren dia (commit $(sudo -u "$USER_SV" git -C "$DIR" log -1 --format='%h %s'))."
if [ -n "$GIO_DU_KIEN" ]; then
    echo "   SE duoc ap dung vao lan bao tri tu dong tiep theo (gio da random gan nhat: $GIO_DU_KIEN)."
else
    echo "   SE duoc ap dung vao lan bao tri tu dong tiep theo (khong doc duoc gio du kien tu log)."
fi
echo "   Muon len NGAY, khong cho: bam 'Bao Tri' tren web admin, hoac chay lai deploy"
echo "   voi DEPLOY_NOW=1 (tren GitHub Actions: workflow_dispatch, tick 'Deploy ngay')."
exit 0
