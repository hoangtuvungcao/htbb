#!/bin/bash
# ============================================================================
#  CONG KIEM - bat buoc truoc moi lan len production (vps/len-production.sh tu choi neu chua DAT).
#
#      sudo bash /opt/htbb/vps/staging/kiem-truoc-khi-len.sh --jar <htbb-server.jar> --sql <htbb.sql> [tuy chon]
#
#  Lam, theo thu tu (bat ky buoc nao hong = HONG, ma thoat 1):
#    1. lam-moi.sh: CSDL staging tu ban sao luu production moi nhat -> nang cap bang SQL ung vien -> jar ung vien ->
#       bat htbb-staging-login + htbb-staging-game (cong 29129 / 28023, chi localhost)
#    2. tools/pokemon/kiem_tai/chay_hoi_quy.sh 127.0.0.1 29129 28023 <dbConn>  (moi nhom bot phai DAT)
#    3. staging van song sau bot (unit active, cong mo)
#    4. log staging tu luc bat: KHONG co Exception / Error moi (tru mau trong vps/staging/loi-bo-qua.txt)
#    5. tat staging (tru --giu-chay) va GHI KET QUA KY: /var/lib/htbb/cong-kiem/<sha jar>_<sha sql>.txt
#       (sha256 jar + sql, DAT/HONG, thoi diem, nhom bot, chu ky HMAC bang /etc/htbb/cong-kiem.key).
#
#  Tuy chon:  --nguon D (tai nguyen server cua ban ung vien, mac dinh /opt/htbb)   --giu-csdl (khong nap lai CSDL)
#             --nhom "a b" (chi chay cac nhom bot nay - KET QUA KHONG DUNG DUOC de len production)
#             --giu-chay (de staging chay sau khi kiem)   --ghi-chu "..." (ghi vao ket qua)
#  Thoi gian: ~10 phut nap CSDL + 30-90 phut bot (tuy so nhom). Chay luc it nguoi choi.
# ============================================================================
set -uo pipefail
DIR_SCRIPT=$(cd "$(dirname "$0")" && pwd)
REPO_SCRIPT=$(cd "$DIR_SCRIPT/../.." && pwd)
# shellcheck source=vps/staging/chung.sh
. "$DIR_SCRIPT/chung.sh"

JAR=""; SQL=""; NGUON="$HTBB_GOC"; GIU_CSDL=0; NHOM=""; GIU_CHAY=0; GHI_CHU=""
while [ $# -gt 0 ]; do
    case "$1" in
        --jar) JAR=$2; shift 2 ;;
        --sql) SQL=$2; shift 2 ;;
        --nguon) NGUON=$2; shift 2 ;;
        --giu-csdl) GIU_CSDL=1; shift ;;
        --nhom) NHOM=$2; shift 2 ;;
        --giu-chay) GIU_CHAY=1; shift ;;
        --ghi-chu) GHI_CHU=$2; shift 2 ;;
        -h|--help) sed -n '2,24p' "$0"; exit 0 ;;
        *) loi "tuy chon la: $1 (xem --help)" ;;
    esac
done
[ -n "$JAR" ] && [ -n "$SQL" ] || loi "can --jar <tep> va --sql <tep>"
[ -r "$JAR" ] && [ -r "$SQL" ] || loi "khong doc duoc $JAR / $SQL"
[ "${HTBB_CHO_KHONG_ROOT:-0}" = 1 ] || [ "$(id -u)" = 0 ] || loi "chay bang root (sudo)"
[ -s "$CONG_KIEM_KHOA" ] || loi "chua co khoa ky $CONG_KIEM_KHOA - chay vps/staging/cai-dat.sh"
JAR=$(readlink -f "$JAR"); SQL=$(readlink -f "$SQL")

# Mot cong kiem mot luc (staging chi co mot).
exec 8>"${CONG_KIEM_KHOA_CHAY:-/run/lock/htbb-cong-kiem.lock}"
flock -n 8 || loi "dang co cong kiem khac chay"

SHA_JAR=$(sha "$JAR"); SHA_SQL=$(sha "$SQL")
LUC=$(date +%s); NGAY=$(date '+%F_%H%M%S')
KQ="$CONG_KIEM_DIR/nhat_ky/$NGAY"
install -d -m 700 "$CONG_KIEM_DIR" "$CONG_KIEM_DIR/nhat_ky" "$KQ"
TOM="$KQ/tom_tat.txt"
exec > >(exec 8>&-; tee -a "$KQ/cong_kiem.log") 2>&1   # tee khong giu khoa (fd 8)
echo "== CONG KIEM $(date '+%F %T')"
echo "   jar $JAR  sha256=$SHA_JAR"
echo "   sql $SQL  sha256=$SHA_SQL"

HONG=()
CNF=$(mktemp); chmod 600 "$CNF"
don_dep() {
    rm -f "$CNF"
    if [ "$GIU_CHAY" = 0 ]; then
        "$SYSTEMCTL" stop "$STG_UNIT_GAME" 2>/dev/null || true
        "$SYSTEMCTL" stop "$STG_UNIT_LOGIN" 2>/dev/null || true
    fi
}
trap don_dep EXIT

# ---- 1. lam moi staging ----
LOG_GAME="$STG_GOC/logs/game.out"
NK_DIR="$STG_GOC/logs/nhat_ky"
MOC_GAME=$(stat -c %s "$LOG_GAME" 2>/dev/null || echo 0)
TUY=(--jar "$JAR" --sql "$SQL" --nguon "$NGUON")
[ "$GIU_CSDL" = 1 ] && TUY+=(--giu-csdl)
if ! bash "$DIR_SCRIPT/lam-moi.sh" "${TUY[@]}"; then
    HONG+=("lam_moi_staging")
fi

# ---- 2. bot ----
if [ ${#HONG[@]} -eq 0 ]; then
    buoc "Bot hoi quy"
    # Nhat ky may chu theo ngay: nho kich thuoc moi tep luc bat dau de chi doc phan moi.
    declare -A MOC_NK=()
    for f in "$NK_DIR"/may_chu-*.log; do [ -e "$f" ] && MOC_NK["$f"]=$(stat -c %s "$f"); done
    sleep "${STG_CHO_AM:-20}"   # cho may chu nap xong du lieu nen (luong khoi dong)
    stg_tao_cnf "$CNF"
    # Canh gac ket noi CSDL (MariaDB dung chung voi production): moi STG_NHIP_KET_NOI giay doc Threads_connected; hai lan
    # lien tiep con < STG_DB_DU_TRU cho trong -> DUNG bot + tat game staging (tra ket noi cho production), cong kiem HONG.
    KN_LOG="$KQ/ket_noi_csdl.txt"; : > "$KN_LOG"
    KN_DINH=0; KN_GAN=0; KN_DUNG=0
    setsid env HOI_QUY_RA="$KQ/bot" HOI_QUY_NOI="$STG_GOC" HOI_QUY_LOG_GAME="$LOG_GAME" HOI_QUY_TIEN_TO="$STG_BOT_TIEN_TO" \
        HOI_QUY_NHOM="$NHOM" \
        bash "${HOI_QUY_LENH:-$REPO_SCRIPT/tools/pokemon/kiem_tai/chay_hoi_quy.sh}" 127.0.0.1 "$STG_CONG_GAME" "$STG_CONG_LOGIN" \
            "cnf=$CNF,db=$STG_DB,dbtk=$STG_DB_TK" &
    PID_HQ=$!
    while kill -0 "$PID_HQ" 2>/dev/null; do
        sleep "${STG_NHIP_KET_NOI:-10}"
        kill -0 "$PID_HQ" 2>/dev/null || break
        read -r KN_D KN_T KN_S < <(ket_noi_csdl)
        echo "$(date '+%T') Threads_connected=$KN_D max_connections=$KN_T staging=$KN_S" >> "$KN_LOG"
        [ "${KN_D:-0}" -gt "$KN_DINH" ] && KN_DINH=$KN_D
        if ket_noi_con_du >> "$KN_LOG"; then KN_GAN=0; else KN_GAN=$((KN_GAN + 1)); fi
        if [ "$KN_GAN" -ge 2 ]; then
            echo "   DUNG BOT: MariaDB gan het ket noi (Threads_connected=$KN_D / $KN_T, staging $KN_S) - tat staging de nhuong production"
            kill -TERM -- -"$PID_HQ" 2>/dev/null; sleep 5; kill -KILL -- -"$PID_HQ" 2>/dev/null
            "$SYSTEMCTL" stop "$STG_UNIT_GAME" 2>/dev/null || true
            KN_DUNG=1
            break
        fi
    done
    wait "$PID_HQ"
    MA=$?
    echo "   ket noi CSDL: dinh Threads_connected=$KN_DINH (nhat ky $KN_LOG)"
    [ "$KN_DUNG" = 0 ] || HONG+=("csdl_gan_het_ket_noi")
    [ "$MA" = 0 ] || HONG+=("bot_hoi_quy(ma=$MA)")

    # ---- 3. con song ----
    buoc "Staging con song?"
    if "$SYSTEMCTL" is-active --quiet "$STG_UNIT_GAME" && dang_nghe "$STG_CONG_GAME"; then
        echo "   ok"
    else
        HONG+=("staging_chet_sau_bot")
    fi

    # ---- 4. Exception moi trong log ----
    buoc "Loi moi trong log staging"
    MOI="$KQ/log_moi.txt"; : > "$MOI"
    tail -c +"$((MOC_GAME + 1))" "$LOG_GAME" 2>/dev/null >> "$MOI" || true
    for f in "$NK_DIR"/may_chu-*.log; do
        [ -e "$f" ] || continue
        tail -c +"$(( ${MOC_NK[$f]:-0} + 1 ))" "$f" >> "$MOI"
    done
    BO_QUA="$DIR_SCRIPT/loi-bo-qua.txt"
    LOI_MOI=$(grep -E 'Exception|Error[: ]' "$MOI" | grep -vE '\[MANG\]' \
              | { if [ -s "$BO_QUA" ]; then grep -vEf <(grep -vE '^\s*(#|$)' "$BO_QUA"); else cat; fi; } \
              || true)
    if [ -n "$LOI_MOI" ]; then
        # game.out va nhat_ky/may_chu-*.log chua cung dong (man hinh + tep) -> dem dong khac nhau.
        N=$(printf '%s\n' "$LOI_MOI" | sort -u | wc -l)
        echo "   $N dong loi moi (gom theo mau):"
        printf '%s\n' "$LOI_MOI" | sed -E 's/^[0-9-]+ [0-9:.]+ [A-Z] //; s/[0-9]+/N/g' | sort | uniq -c | sort -rn | head -10
        HONG+=("exception_moi($N)")
    else
        echo "   khong co"
    fi
fi

# ---- 5. ket qua ky ----
KET=DAT; [ ${#HONG[@]} -eq 0 ] || KET=HONG
[ -n "$NHOM" ] && [ "$KET" = DAT ] && KET=DAT_MOT_PHAN   # chi chay mot so nhom -> khong cho len production
# Bo hoi quy bi thay / cat bang bien moi truong -> cung khong du de len production (tru che do thu cuc bo, ghi ro).
DOI_BO=""
for B in HOI_QUY_CONF HOI_QUY_BO HOI_QUY_LENH HOI_QUY_NHOM; do [ -n "${!B:-}" ] && DOI_BO="$DOI_BO $B"; done
THU_CK=0
if [ -n "$DOI_BO" ] && [ "$KET" = DAT ]; then
    if [ "${HTBB_CONG_KIEM_THU:-0}" = 1 ]; then THU_CK=1; else KET=DAT_MOT_PHAN; fi
fi
SHA_CONF=$(sha "${HOI_QUY_CONF:-$REPO_SCRIPT/tools/pokemon/kiem_tai/hoi_quy.conf}" 2>/dev/null || echo "-")
F_KQ="$CONG_KIEM_DIR/${SHA_JAR}_${SHA_SQL}.txt"
NOI_DUNG=$(cat <<EOF
phien=1
jar_sha256=$SHA_JAR
sql_sha256=$SHA_SQL
ket_qua=$KET
luc=$LUC
luc_doc=$(date -d "@$LUC" '+%F %T %z')
hong=${HONG[*]:-}
bo_hoi_quy_sha256=$SHA_CONF
doi_bo=${DOI_BO# }
thu=$THU_CK
nhom_bot=$(tr '\n' ';' < "$KQ/bot/tong_ket.txt" 2>/dev/null)
jar=$JAR
sql=$SQL
nhat_ky=$KQ
ghi_chu=$GHI_CHU
EOF
)
CHU_KY=$(printf '%s\n' "$NOI_DUNG" | ky_hmac) || loi "khong ky duoc ket qua"
( umask 077; printf '%s\nchu_ky=%s\n' "$NOI_DUNG" "$CHU_KY" > "$F_KQ" )
ln -sfn "$F_KQ" "$CONG_KIEM_DIR/moi_nhat.txt"
# Giu 30 thu muc nhat ky gan nhat.
ls -1dt "$CONG_KIEM_DIR"/nhat_ky/*/ 2>/dev/null | tail -n +31 | xargs -r rm -rf

{
    echo
    echo "================ CONG KIEM: $KET ================"
    echo "jar sha256 $SHA_JAR"
    echo "sql sha256 $SHA_SQL"
    [ ${#HONG[@]} -eq 0 ] || echo "hong: ${HONG[*]}"
    [ -s "$KQ/bot/tong_ket.txt" ] && sed 's/^/  /' "$KQ/bot/tong_ket.txt"
    echo "ket qua ky: $F_KQ"
    echo "nhat ky   : $KQ"
    [ "$KET" = DAT ] && echo "-> len production:  sudo bash $HTBB_GOC/vps/len-production.sh --jar $JAR --sql $SQL --co-sql   (hoac --chi-jar)"
} | tee "$TOM"
[ "$KET" = DAT ]
