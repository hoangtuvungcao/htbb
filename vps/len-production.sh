#!/bin/bash
# ============================================================================
#  LEN PRODUCTION - cach DUY NHAT duoc phep thay game server dang chay (jar / SQL / web).
#
#      sudo bash /opt/htbb/vps/len-production.sh --jar <htbb-server.jar> --chi-jar
#      sudo bash /opt/htbb/vps/len-production.sh --jar <htbb-server.jar> --sql <htbb.sql> --co-sql
#      sudo bash /opt/htbb/vps/len-production.sh ... --web <web-build.tgz>      (them ban web cung lan)
#      sudo bash /opt/htbb/vps/len-production.sh --chi-web --web <web-build.tgz> (chi web, khong dung game)
#
#  CONG KIEM: tu choi neu vps/staging/kiem-truoc-khi-len.sh chua DAT cho DUNG sha256 jar + sql trong
#  CONG_KIEM_GIO gio (mac dinh 24). Ket qua doc tu /var/lib/htbb/cong-kiem/<sha jar>_<sha sql>.txt, chu ky HMAC
#  kiem lai bang /etc/htbb/cong-kiem.key (sua tay tep ket qua = chu ky sai = tu choi).
#    --co-sql  : sql ung vien phai la dung tep da qua cong kiem.
#    --chi-jar : cong kiem phai da chay voi SQL GIONG /opt/htbb/sql/htbb.sql dang cai (CSDL production khong doi).
#
#  Lam DUNG cac buoc van hanh tay (17-24/10/2026):
#    1. kiem jar (zip) / sql (phan B) / cong kiem / gio (KHONG chay thu Hai 04:00-05:00 - moc tuan - tru --force)
#    2. sao luu: CSDL (htbb-backup-db.sh) + jar dang chay + sql dang cai -> /opt/htbb/backups/len-production/<moc>/
#    3. UPDATE htbb_db.5h_systems SET status=0 WHERE command='baotri_thongbao'  -> doi 62 giay (nguoi choi thay bao)
#    4. systemctl stop htbb-game (shutdown hook luu nhan vat; toi da 180 s)
#    5. --co-sql: cai sql -> nang_cap_csdl.sh (BO_SAO_LUU=1: vua sao luu o buoc 2)
#    6. cai jar -> systemctl start htbb-game -> doi cong 19129 (toi da 240 s; khong len = TU QUAY LAI jar cu)
#    7. kiem-suc-khoe.sh (doi trang_thai.json moi) ; --web: htbb-deploy-web.sh nhan-build
#    8. in tom tat (sha cu / moi, sao luu, suc khoe, Exception tu luc bat) + ghi /opt/htbb/logs/len-production.log
#
#  Tuy chon khac: --force (bo qua khung gio thu Hai), --giay-bao N (mac dinh 62), --thu (chi kiem, khong lam gi),
#    --khan-cap "<ly do>" (BO QUA cong kiem - phai go xac nhan, ghi vao log; chi khi production dang hong).
#  Ngat giua chung (Ctrl-C / mat SSH) sau khi da tat game: script tu bat lai game bang jar dang co tren dia.
# ============================================================================
set -uo pipefail
DIR=$(cd "$(dirname "$0")/.." && pwd)
GOC=${HTBB_GOC:-/opt/htbb}
CONG_KIEM_DIR=${CONG_KIEM_DIR:-/var/lib/htbb/cong-kiem}
CONG_KIEM_KHOA=${CONG_KIEM_KHOA:-/etc/htbb/cong-kiem.key}
CONG_KIEM_GIO=${CONG_KIEM_GIO:-24}
SV_GAME=${SV_GAME:-htbb-game}
CONG_GAME=${CONG_GAME:-19129}
SYSTEMCTL=${SYSTEMCTL:-systemctl}
BACKUP_DB=${BACKUP_DB:-/usr/local/sbin/htbb-backup-db.sh}
DEPLOY_WEB=${DEPLOY_WEB:-/usr/local/sbin/htbb-deploy-web.sh}
LOG_TONG=${LOG_TONG:-$GOC/logs/len-production.log}
TZ_GAME=${TZ_GAME:-Asia/Ho_Chi_Minh}
USER_SV=${USER_SV:-htbb}
if [ -n "${MYSQL:-}" ]; then read -r -a MY <<< "$MYSQL"; else MY=(mysql); fi

JAR=""; SQL=""; WEB=""; CHE_DO=""; FORCE=0; GIAY_BAO=62; THU=0; KHAN_CAP=""
while [ $# -gt 0 ]; do
    case "$1" in
        --jar) JAR=$2; shift 2 ;;
        --sql) SQL=$2; shift 2 ;;
        --web) WEB=$2; shift 2 ;;
        --chi-jar) CHE_DO=chi-jar; shift ;;
        --co-sql) CHE_DO=co-sql; shift ;;
        --chi-web) CHE_DO=chi-web; shift ;;
        --force) FORCE=1; shift ;;
        --giay-bao) GIAY_BAO=$2; shift 2 ;;
        --thu) THU=1; shift ;;
        --khan-cap) KHAN_CAP=$2; shift 2 ;;
        -h|--help) sed -n '2,32p' "$0"; exit 0 ;;
        *) echo "[LOI] tuy chon la: $1 (xem --help)"; exit 2 ;;
    esac
done

loi() { echo "[LOI] $*" >&2; ghi_log "THAT BAI: $*"; exit 1; }
buoc() { echo; echo "== $(date '+%T') $*"; }
ghi_log() { mkdir -p "$(dirname "$LOG_TONG")" 2>/dev/null; echo "$(date '+%F %T') [$(whoami)] $*" >> "$LOG_TONG" 2>/dev/null || true; }
sha() { sha256sum "$1" | cut -d' ' -f1; }
dang_nghe() { ss -ltn 2>/dev/null | awk '{print $4}' | grep -qE "[:.]$1\$"; }
my() { "${MY[@]}" --default-character-set=utf8mb4 "$@"; }

# ---------------------------------------------------------------- 1. kiem truoc
[ -n "$CHE_DO" ] || { echo "[LOI] chon mot: --chi-jar | --co-sql | --chi-web (xem --help)"; exit 2; }
[ "${HTBB_CHO_KHONG_ROOT:-0}" = 1 ] || [ "$(id -u)" = 0 ] || { echo "[LOI] chay bang root (sudo)"; exit 2; }
exec 9>"${KHOA_CHAY:-/run/lock/htbb-len-production.lock}"
flock -n 9 || { echo "[LOI] dang co lan len production khac"; exit 1; }

buoc "1/8 Kiem truoc"
# Khung gio cam: thu Hai 04:00-04:59 (gio game) - moc reset tuan 05:00, le 04:00.
THU_TUAN=$(TZ=$TZ_GAME date +%u); GIO=$(TZ=$TZ_GAME date +%H)
if [ "$THU_TUAN" = 1 ] && [ "$GIO" = 04 ]; then
    if [ "$FORCE" = 1 ]; then echo "   [CANH BAO] thu Hai 04:xx (truoc reset tuan) - --force: van chay"
    else loi "thu Hai 04:00-05:00 ($TZ_GAME) la khung reset tuan - doi sau 05:00 hoac --force"; fi
fi
case "$(TZ=$TZ_GAME date +%H%M)" in
    050[0-9]|05[12][0-9]|0530) echo "   [CANH BAO] dang trong khung bao tri hang ngay 05:00-05:30 - game co the dang tu khoi dong lai" ;;
esac

if [ "$CHE_DO" = chi-web ]; then
    [ -n "$WEB" ] || loi "--chi-web can --web <tgz>"
else
    [ -n "$JAR" ] && [ -r "$JAR" ] || loi "thieu / khong doc duoc --jar"
    JAR=$(readlink -f "$JAR")
    if command -v unzip >/dev/null 2>&1; then unzip -tq "$JAR" >/dev/null 2>&1 || loi "$JAR hong (zip)"; fi
    if [ "$CHE_DO" = co-sql ]; then
        [ -n "$SQL" ] && [ -r "$SQL" ] || loi "--co-sql can --sql <tep>"
        SQL=$(readlink -f "$SQL")
        grep -q '^-- ######## B. NANG CAP' "$SQL" || loi "$SQL thieu phan B - khong phai sql/htbb.sql"
        grep -q '^USE `htbb_db`;' "$SQL" || loi "$SQL khong dung CSDL htbb_db (ban viet lai cho staging?)"
        SQL_KIEM=$SQL
    else
        [ -z "$SQL" ] || loi "--chi-jar khong nhan --sql (muon nang cap CSDL thi dung --co-sql)"
        SQL_KIEM="$GOC/sql/htbb.sql"     # jar phai da qua cong kiem tren DUNG schema production dang co
    fi
fi
[ -z "$WEB" ] || { [ -r "$WEB" ] && gzip -t "$WEB" 2>/dev/null; } || loi "--web $WEB khong doc duoc / khong phai .tgz"

# ---------------------------------------------------------------- cong kiem
kiem_cong() {
    local sj=$1 ss=$2 f="$CONG_KIEM_DIR/${1}_${2}.txt" noi ky tinh luc tuoi kq
    [ -r "$f" ] || { echo "chua co ket qua cong kiem cho jar $sj + sql $ss (chay vps/staging/kiem-truoc-khi-len.sh)"; return 1; }
    [ -r "$CONG_KIEM_KHOA" ] || { echo "khong doc duoc khoa $CONG_KIEM_KHOA"; return 1; }
    noi=$(grep -v '^chu_ky=' "$f"); ky=$(sed -n 's/^chu_ky=//p' "$f")
    tinh=$( { cat "$CONG_KIEM_KHOA"; printf '%s\n' "$noi"; } | sha256sum | cut -d' ' -f1 )
    tinh=$( { cat "$CONG_KIEM_KHOA"; printf '%s' "$tinh"; } | sha256sum | cut -d' ' -f1 )
    [ -n "$ky" ] && [ "$ky" = "$tinh" ] || { echo "chu ky ket qua SAI ($f bi sua tay?)"; return 1; }
    [ "$(sed -n 's/^jar_sha256=//p' <<< "$noi")" = "$sj" ] && [ "$(sed -n 's/^sql_sha256=//p' <<< "$noi")" = "$ss" ] \
        || { echo "sha trong ket qua khong khop"; return 1; }
    if [ "$(sed -n 's/^thu=//p' <<< "$noi")" = 1 ] && [ "${HTBB_CHO_KET_QUA_THU:-0}" != 1 ]; then
        echo "ket qua cong kiem la CHE DO THU (bo hoi quy bi thay: $(sed -n 's/^doi_bo=//p' <<< "$noi")) - khong dung cho production"; return 1
    fi
    kq=$(sed -n 's/^ket_qua=//p' <<< "$noi")
    [ "$kq" = DAT ] || { echo "cong kiem ket qua $kq (khong phai DAT) - xem $(sed -n 's/^nhat_ky=//p' <<< "$noi")"; return 1; }
    luc=$(sed -n 's/^luc=//p' <<< "$noi"); tuoi=$(( ( $(date +%s) - ${luc:-0} ) / 3600 ))
    [ "$tuoi" -lt "$CONG_KIEM_GIO" ] || { echo "cong kiem DAT nhung da $tuoi gio (> $CONG_KIEM_GIO) - chay lai"; return 1; }
    echo "DAT luc $(sed -n 's/^luc_doc=//p' <<< "$noi") ($tuoi gio truoc)"
}

if [ "$CHE_DO" != chi-web ]; then
    SHA_JAR=$(sha "$JAR"); SHA_SQL=$(sha "$SQL_KIEM")
    echo "   jar $JAR sha256=$SHA_JAR"
    echo "   sql $SQL_KIEM sha256=$SHA_SQL ($CHE_DO)"
    if TB=$(kiem_cong "$SHA_JAR" "$SHA_SQL"); then
        echo "   cong kiem: $TB"
    elif [ -n "$KHAN_CAP" ]; then
        echo "   [KHAN CAP] cong kiem: $TB"
        echo "   Ly do: $KHAN_CAP"
        if [ "$THU" = 0 ]; then
            printf '   Go chinh xac "LEN KHONG KIEM" de tiep tuc: '
            read -r XN < /dev/tty || XN=""
            [ "$XN" = "LEN KHONG KIEM" ] || loi "khong xac nhan khan cap"
        fi
        ghi_log "KHAN CAP - BO QUA CONG KIEM jar=$SHA_JAR sql=$SHA_SQL ly do: $KHAN_CAP"
    else
        loi "CONG KIEM: $TB"
    fi
    SHA_JAR_CU=$(sha "$GOC/server/htbb-server.jar" 2>/dev/null || echo "-")
    [ "$SHA_JAR_CU" != "$SHA_JAR" ] || [ "$CHE_DO" = co-sql ] || [ -n "$WEB" ] || loi "jar nay DANG chay roi (sha giong) - khong co gi de len"
fi

if [ "$THU" = 1 ]; then
    echo; echo "== --thu: moi kiem DAT, khong lam gi. Bo --thu de len that."
    exit 0
fi
ghi_log "BAT DAU $CHE_DO jar=${SHA_JAR:-} sql=${SHA_SQL:-} web=${WEB:+$(sha "$WEB")}"
BAT_DAU=$(date +%s)

# ---------------------------------------------------------------- chi web
deploy_web() {
    buoc "Web: $WEB"
    [ -x "$DEPLOY_WEB" ] || { echo "   [LOI] khong co $DEPLOY_WEB (web/deploy/cai-dat-vps.sh)"; return 1; }
    local s; s=$(sha "$WEB" | cut -c1-12)
    SSH_ORIGINAL_COMMAND="nhan-build $s" "$DEPLOY_WEB" < "$WEB"
}
if [ "$CHE_DO" = chi-web ]; then
    deploy_web && { ghi_log "XONG chi-web"; echo "== XONG web"; exit 0; }
    loi "deploy web that bai (htbb-deploy-web.sh tu quay lai ban truoc - xem $GOC/logs/deploy-web.log)"
fi

# ---------------------------------------------------------------- 2. sao luu
buoc "2/8 Sao luu"
MOC=$(date '+%F_%H%M%S')
BK="$GOC/backups/len-production/$MOC"
install -d -m 750 "$BK"
if [ -x "$BACKUP_DB" ]; then
    "$BACKUP_DB" || loi "sao luu CSDL that bai - DUNG, chua dung vao gi"
else
    bash "$DIR/vps/backup_db.sh" || loi "sao luu CSDL that bai - DUNG, chua dung vao gi"
fi
cp -a "$GOC/server/htbb-server.jar" "$BK/htbb-server.jar" || loi "khong sao luu duoc jar dang chay"
cp -a "$GOC/sql/htbb.sql" "$BK/htbb.sql" 2>/dev/null || true
echo "   CSDL: $(ls -1t "$GOC"/backups/htbb_db_*.sql.gz 2>/dev/null | head -1)"
echo "   jar + sql cu: $BK"
# Giu 10 lan len gan nhat (jar ~ 10 MB).
ls -1dt "$GOC"/backups/len-production/*/ 2>/dev/null | tail -n +11 | xargs -r rm -rf

# ---------------------------------------------------------------- 3. bao truoc
buoc "3/8 Bao nguoi choi (${GIAY_BAO}s)"
# Dem dong CO (khong dung ROW_COUNT: co dang 0 san thi ROW_COUNT = 0 du dong ton tai).
N=$(my -N -B -e "UPDATE htbb_db.\`5h_systems\` SET status = 0 WHERE command = 'baotri_thongbao';
                 SELECT COUNT(*) FROM htbb_db.\`5h_systems\` WHERE command = 'baotri_thongbao';" 2>/dev/null | tail -1)
if [ "${N:-0}" -ge 1 ] 2>/dev/null; then echo "   da dat co baotri_thongbao (may chu doc trong <= 5 giay)"; else
    echo "   [CANH BAO] khong dat duoc co baotri_thongbao (dong khong co? dem=${N:-loi CSDL}) - nguoi choi se bi out khong bao truoc"; fi
sleep "$GIAY_BAO"

# ---------------------------------------------------------------- 4. tat
buoc "4/8 Tat $SV_GAME (luu nhan vat, toi da 180s)"
CAN_BAT_LAI=1
trap 'echo "[NGAT] bi ngat giua chung -> bat lai game"; [ "$CAN_BAT_LAI" = 1 ] && "$SYSTEMCTL" start "$SV_GAME"; ghi_log "NGAT giua chung"; exit 1' HUP INT TERM PIPE
"$SYSTEMCTL" stop "$SV_GAME" || echo "   [CANH BAO] systemctl stop tra loi"
for _ in $(seq 1 30); do dang_nghe "$CONG_GAME" || break; sleep 1; done
LOG_GAME="$GOC/logs/game.out"
MOC_LOG=$(stat -c %s "$LOG_GAME" 2>/dev/null || echo 0)

# ---------------------------------------------------------------- 5. SQL
if [ "$CHE_DO" = co-sql ]; then
    buoc "5/8 Nang cap CSDL"
    install -o "$USER_SV" -g "$USER_SV" -m 644 "$SQL" "$GOC/sql/htbb.sql.moi" && mv -f "$GOC/sql/htbb.sql.moi" "$GOC/sql/htbb.sql"
    if ! BO_SAO_LUU=1 bash "$DIR/vps/nang_cap_csdl.sh"; then
        echo "   [LOI] nang cap CSDL hong -> bat lai game bang jar CU (jar cu chay duoc tren CSDL nang cap do dang)"
        "$SYSTEMCTL" start "$SV_GAME"; CAN_BAT_LAI=0
        loi "nang_cap_csdl.sh that bai - game da bat lai jar cu; sao luu: $BK + $(ls -1t "$GOC"/backups/htbb_db_*.sql.gz | head -1)"
    fi
else
    buoc "5/8 SQL: bo qua (--chi-jar)"
fi

# ---------------------------------------------------------------- 6. jar + bat
buoc "6/8 Cai jar moi + bat"
install -o "$USER_SV" -g "$USER_SV" -m 644 "$JAR" "$GOC/server/htbb-server.jar.moi" && mv -f "$GOC/server/htbb-server.jar.moi" "$GOC/server/htbb-server.jar" \
    || { "$SYSTEMCTL" start "$SV_GAME"; CAN_BAT_LAI=0; loi "khong cai duoc jar - game da bat lai jar cu"; }
"$SYSTEMCTL" start "$SV_GAME"
CAN_BAT_LAI=0
trap - HUP INT TERM PIPE
LEN=0
for i in $(seq 1 "${CHO_BAT:-240}"); do
    if dang_nghe "$CONG_GAME"; then LEN=1; echo "   cong $CONG_GAME LISTEN sau ${i}s"; break; fi
    sleep 1
done
if [ "$LEN" = 0 ]; then
    echo "   [LOI] jar moi khong nghe cong $CONG_GAME sau ${CHO_BAT:-240}s. Log:"
    tail -c +"$((MOC_LOG + 1))" "$LOG_GAME" 2>/dev/null | tail -n 30 | sed 's/^/     /'
    echo "   -> QUAY LAI jar cu"
    "$SYSTEMCTL" stop "$SV_GAME"
    install -o "$USER_SV" -g "$USER_SV" -m 644 "$BK/htbb-server.jar" "$GOC/server/htbb-server.jar"
    "$SYSTEMCTL" start "$SV_GAME"
    loi "jar moi khong len - da quay lai jar cu ($BK/htbb-server.jar). CSDL ${CHE_DO/co-sql/DA nang cap (jar cu van chay duoc)}"
fi

# ---------------------------------------------------------------- 7. suc khoe + web
buoc "7/8 Suc khoe"
# trang_thai.json ghi moi 30 s: doi ban moi sau luc bat roi moi kiem.
TT="$GOC/server/trang_thai.json"; T0=$(date +%s)
for _ in $(seq 1 90); do
    [ -f "$TT" ] && [ "$(stat -c %Y "$TT")" -ge "$T0" ] && break
    sleep 2
done
SK=$(THU_MUC="$GOC" CONG_GAME="$CONG_GAME" bash "$DIR/vps/kiem-suc-khoe.sh" 2>&1); SK_MA=$?
echo "   $SK"
WEB_KQ="-"
if [ -n "$WEB" ]; then
    if deploy_web; then WEB_KQ="ok $(sha "$WEB" | cut -c1-12)"; else WEB_KQ="HONG (xem $GOC/logs/deploy-web.log)"; fi
fi

# ---------------------------------------------------------------- 8. tom tat
buoc "8/8 Tom tat"
LOI_MOI=$(tail -c +"$((MOC_LOG + 1))" "$LOG_GAME" 2>/dev/null | grep -E 'Exception|Error[: ]' | grep -vF '[MANG]' || true)
N_LOI=$(printf '%s' "$LOI_MOI" | grep -c . || true)
{
    echo "che do      : $CHE_DO   ($(( $(date +%s) - BAT_DAU ))s)"
    echo "jar         : $SHA_JAR_CU -> $SHA_JAR"
    [ "$CHE_DO" = co-sql ] && echo "sql         : $SHA_SQL (nang cap xong)"
    echo "sao luu     : $BK  +  $(ls -1t "$GOC"/backups/htbb_db_*.sql.gz 2>/dev/null | head -1)"
    echo "suc khoe    : $([ "$SK_MA" = 0 ] && echo OK || echo "CO TANG HONG") - $SK"
    echo "web         : $WEB_KQ"
    echo "Exception   : $N_LOI dong tu luc bat"
    [ "$N_LOI" = 0 ] || printf '%s\n' "$LOI_MOI" | sed -E 's/^[0-9-]+ [0-9:.]+ [A-Z] //; s/[0-9]+/N/g' | sort | uniq -c | sort -rn | head -5 | sed 's/^/              /'
    echo "quay lai    : sudo install -o $USER_SV -g $USER_SV -m 644 $BK/htbb-server.jar $GOC/server/htbb-server.jar && sudo systemctl restart $SV_GAME"
} | tee -a "$BK/tom_tat.txt"
ghi_log "XONG $CHE_DO jar=$SHA_JAR suc_khoe=$SK_MA exception=$N_LOI web=$WEB_KQ sao_luu=$BK"
echo "$SHA_JAR $(date '+%F %T') $CHE_DO" >> "$CONG_KIEM_DIR/da-len.txt" 2>/dev/null || true
[ "$SK_MA" = 0 ]
