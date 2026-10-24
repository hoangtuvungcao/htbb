#!/bin/bash
# ============================================================================
#  BO HOI QUY BOT - diem vao DUY NHAT cua cong kiem (vps/staging/kiem-truoc-khi-len.sh).
#
#      tools/pokemon/kiem_tai/chay_hoi_quy.sh <host> <congGame> <congLogin> <dbConn>
#
#  <dbConn> = danh sach khoa=gia_tri ngan dau phay:
#      cnf=<tep mysql --defaults-file>   (VPS staging: user htbb_staging, mat khau khong len dong lenh)
#      port=<cong MariaDB>               (may thu: user htbb / htbb tren 127.0.0.1, nhu BotChucNang cu)
#      db=<CSDL game>                    (bat buoc de bot doi chieu CSDL; "-" = khong kiem CSDL)
#      dbtk=<CSDL tai khoan>             (mac dinh account; staging: htbb_staging_account)
#    vd: cnf=/run/htbb-cong-kiem/bot.cnf,db=htbb_staging,dbtk=htbb_staging_account
#        port=3399,db=htbb_db
#
#  Nhom chay doc tu hoi_quy.conf (cung thu muc). Bien:
#      HOI_QUY_NHOM="a b"   chi chay cac nhom nay        HOI_QUY_BO="c"  bo qua cac nhom nay
#      HOI_QUY_RA=<thu muc> noi ghi log tung nhom + tong_ket.txt (mac dinh mktemp)
#      HOI_QUY_NOI=<goc>    thu muc co server/cMap (mac dinh: monorepo chua script nay)
#      HOI_QUY_TIEN_TO=kt   tien to tai khoan bot      HTBB_BOT_CP=<classpath>  bot da bien dich san (khong can javac)
#      HOI_QUY_HE_SO=1      nhan gioi han thoi gian moi nhom (may cham)
#  Ma thoat: 0 = moi nhom DAT; 1 = co nhom HONG / LOI; 2 = sai doi so / khong bien dich duoc.
#  tong_ket.txt: moi dong "<DAT|HONG|LOI|TAT> <nhom> pass=<n> fail=<n> bo_qua=<n> giay=<n>".
# ============================================================================
set -uo pipefail
[ $# -eq 4 ] || { sed -n '2,25p' "$0"; exit 2; }
HOST=$1; CONG=$2; CONG_LOGIN=$3; DBCONN=$4
DIR=$(cd "$(dirname "$0")" && pwd)
GOC=$(cd "$DIR/../../.." && pwd)
CONF=${HOI_QUY_CONF:-$DIR/hoi_quy.conf}
TT=${HOI_QUY_TIEN_TO:-kt}
NOI=${HOI_QUY_NOI:-$GOC}
HE_SO=${HOI_QUY_HE_SO:-1}
RA=${HOI_QUY_RA:-$(mktemp -d "${TMPDIR:-/tmp}/hoi-quy.XXXXXX")}
mkdir -p "$RA"
TONG="$RA/tong_ket.txt"; : > "$TONG"

# ---- dbConn -> -D ----
DB_OPT=()
IFS=',' read -r -a CAP <<< "$DBCONN"
for kv in "${CAP[@]}"; do
    case "$kv" in
        cnf=*) DB_OPT+=("-Ddbcnf=${kv#cnf=}") ;;
        port=*) DB_OPT+=("-Ddbport=${kv#port=}") ;;
        db=-) ;;
        db=*) DB_OPT+=("-Ddb=${kv#db=}") ;;
        dbtk=*) DB_OPT+=("-Ddbtk=${kv#dbtk=}") ;;
        '') ;;
        *) echo "dbConn la: $kv"; exit 2 ;;
    esac
done

# ---- kiem cong ----
co_cong() { timeout 3 bash -c "exec 3<>/dev/tcp/$HOST/$1" 2>/dev/null; }
co_cong "$CONG" || { echo "[LOI] khong ket noi duoc game $HOST:$CONG"; exit 2; }
if [ "$HOST" = 127.0.0.1 ] || [ "$HOST" = localhost ]; then
    ss -ltn 2>/dev/null | awk '{print $4}' | grep -qE "[:.]$CONG_LOGIN\$" \
        || { echo "[LOI] login server khong nghe cong $CONG_LOGIN"; exit 2; }
fi

# ---- bien dich bot ----
if [ -n "${HTBB_BOT_CP:-}" ]; then
    CP=$HTBB_BOT_CP
else
    command -v javac >/dev/null 2>&1 || { echo "[LOI] khong co javac (apt install openjdk-17-jdk-headless) va khong dat HTBB_BOT_CP"; exit 2; }
    CP="$RA/bot-cls"; mkdir -p "$CP"
    if ! javac -encoding UTF-8 -nowarn -d "$CP" "$DIR"/*.java > "$RA/bien_dich.log" 2>&1; then
        echo "[LOI] bien dich bot hong:"; tail -n 20 "$RA/bien_dich.log"; exit 2
    fi
fi

chon() { # nhom co duoc chay?
    local n=$1
    if [ -n "${HOI_QUY_NHOM:-}" ]; then case " $HOI_QUY_NHOM " in *" $n "*) ;; *) return 1 ;; esac; fi
    if [ -n "${HOI_QUY_BO:-}" ]; then case " $HOI_QUY_BO " in *" $n "*) return 1 ;; esac; fi
    return 0
}

CHUNG=("${DB_OPT[@]}" "-Dnoi=$NOI" -Dtu=1 -Dden=40 -DtuThuong=101 -DdenThuong=199 -Dfile.encoding=UTF-8)
[ -n "${HOI_QUY_LOG_GAME:-}" ] && CHUNG+=("-Dlog=$HOI_QUY_LOG_GAME")
HONG=0; SO=0
BD_TONG=$(date +%s)
echo "== Hoi quy bot -> $HOST:$CONG (login $CONG_LOGIN), ket qua: $RA"
while IFS= read -r DONG || [ -n "$DONG" ]; do
    case "$DONG" in ''|'#'*) continue ;; esac
    TAT=0
    if [ "${DONG:0:1}" = "!" ]; then TAT=1; DONG=${DONG:1}; fi
    TRUOC=${DONG%% -- *}; SAU=""
    [[ "$DONG" == *" -- "* ]] && SAU=${DONG#* -- }
    [[ "$DONG" == *" --" ]] && TRUOC=${DONG% --}
    read -r -a T <<< "$TRUOC"
    NHOM=${T[0]}; KIEU=${T[1]}; LOP=${T[2]}; GH=${T[3]}; OPT=("${T[@]:4}")
    read -r -a DOI <<< "${SAU//@TT@/$TT}"
    chon "$NHOM" || continue
    if [ "$TAT" = 1 ]; then
        echo "TAT  $NHOM pass=0 fail=0 bo_qua=0 giay=0" >> "$TONG"; echo "-- $NHOM: TAM TAT trong hoi_quy.conf"; continue
    fi
    SO=$((SO + 1))
    LOG="$RA/$NHOM.log"
    GH=$(( GH * HE_SO ))
    echo "-- $NHOM ($LOP, toi da ${GH}s) ..."
    BD=$(date +%s)
    case "$KIEU" in
        cn) timeout -k 15 "$GH" java "${CHUNG[@]}" "${OPT[@]}" "-Dra=$RA/$NHOM.ketqua" -cp "$CP" "$LOP" "$HOST" "$CONG" "$TT" "${DOI[@]}" < /dev/null > "$LOG" 2>&1 ;;
        bt) timeout -k 15 "$GH" java "${CHUNG[@]}" "${OPT[@]}" -cp "$CP" "$LOP" "$HOST" "$CONG" "${DOI[@]}" < /dev/null > "$LOG" 2>&1 ;;
        *) echo "kieu la '$KIEU' o nhom $NHOM" > "$LOG"; false ;;
    esac
    MA=$?
    GIAY=$(( $(date +%s) - BD ))
    P=0; F=0; B=0; KQ=LOI
    case "$KIEU" in
        cn)
            D=$(grep -E '^=== TONG: PASS [0-9]+, FAIL [0-9]+, BO QUA [0-9]+' "$LOG" | tail -1)
            if [ -n "$D" ]; then
                P=$(sed -E 's/.*PASS ([0-9]+).*/\1/' <<< "$D"); F=$(sed -E 's/.*FAIL ([0-9]+).*/\1/' <<< "$D"); B=$(sed -E 's/.*BO QUA ([0-9]+).*/\1/' <<< "$D")
                if [ "$MA" = 0 ] && [ "$F" = 0 ]; then KQ=DAT; else KQ=HONG; fi
            fi ;;
        bt)
            V=$(grep -oE '^vao game [0-9]+/[0-9]+' "$LOG" | tail -1)
            L=$(grep -oE '^loi=[0-9]+' "$LOG" | tail -1)
            if [ -n "$V" ] && [ -n "$L" ]; then
                a=${V#vao game }; P=${a%/*}; n=${a#*/}; F=$(( n - P + ${L#loi=} ))
                if [ "$MA" = 0 ] && [ "$F" = 0 ]; then KQ=DAT; else KQ=HONG; fi
            fi ;;
    esac
    [ "$MA" = 124 ] || [ "$MA" = 137 ] && KQ=LOI && echo "   HET GIO sau ${GH}s" >> "$LOG"
    printf '%-4s %s pass=%s fail=%s bo_qua=%s giay=%s\n' "$KQ" "$NHOM" "$P" "$F" "$B" "$GIAY" | tee -a "$TONG"
    if [ "$KQ" != DAT ]; then
        HONG=1
        grep -E '^(FAIL|HONG)|ngoai le|Exception|HET GIO' "$LOG" | head -n 8 | sed 's/^/     /'
    fi
done < "$CONF"

echo "== $SO nhom, $(( $(date +%s) - BD_TONG ))s: $(grep -c '^DAT' "$TONG") DAT, $(grep -cE '^(HONG|LOI)' "$TONG") HONG/LOI, $(grep -c '^TAT' "$TONG") tam tat"
[ "$SO" -gt 0 ] || { echo "[LOI] khong co nhom nao duoc chay"; exit 2; }
# Chot: moi nhom duoc chon PHAI co dong tong ket (vong doc conf bi nuot stdin = mat nhom im lang).
CAN=0
while read -r T1 _; do
    case "$T1" in ''|'#'*) continue ;; esac
    chon "${T1#!}" && CAN=$((CAN + 1))
done < "$CONF"
if [ "$(grep -c . "$TONG")" != "$CAN" ]; then
    echo "[LOI] chi co $(grep -c . "$TONG")/$CAN nhom co ket qua"; exit 1
fi
exit $HONG
