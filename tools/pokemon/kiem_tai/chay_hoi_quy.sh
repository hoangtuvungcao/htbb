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
#      HOI_QUY_NOI=<goc>    thu muc may chu dang chay: server/map, server/cMap, sql/htbb.sql (mac dinh: monorepo chua
#                           script nay). Ma nguon (server/src, du_lieu, client_pc) LUON doc tu monorepo chua script (-Dgoc)
#      HOI_QUY_TIEN_TO=kt   tien to tai khoan bot      HTBB_BOT_CP=<classpath>  bot da bien dich san (khong can javac)
#      HOI_QUY_HE_SO=1      nhan gioi han thoi gian moi nhom (may cham)
#      HOI_QUY_CHE_DO=day_du  ke hoach lan: day_du = dong "@lan" / "@rieng" (moi nhom, ~1 gio); nhanh = dong "@nhanh lan" /
#                           "@nhanh rieng" (bo nhom loi cot, ~8 phut - CONG KIEM MAC DINH tu 31/10/2026). Che do nhanh chi chay
#                           nhom co trong ke hoach @nhanh; nhom "~" (ban rut gon) chi chay o che do nhanh.
#      HOI_QUY_SONG_SONG=1  chay cac LAN (dong "@lan" trong hoi_quy.conf) cung luc; 0 = tung nhom mot theo thu tu conf
#      HOI_QUY_LAN_TOI_DA=4 so lan chay cung luc toi da (moi lan = 1 JVM bot = 1 ket noi CSDL, xem hoi_quy.conf)
#  Bien dich truoc (cong kiem chay song song voi lam moi staging):  chay_hoi_quy.sh --bien-dich <thu muc>
#      -> in classpath o dong cuoi (dung lai bang HTBB_BOT_CP=<classpath>); ma 0 = xong, 2 = loi bien dich.
#      HOI_QUY_THU_LAI=1    cho THU LAI MOT LAN nhom hong vi loi BAT DINH da biet (hoi_quy_bat_dinh.txt); 0 = khong
#      HOI_QUY_JAVA="-Xmx..." tuy chon JVM them cho moi bot (mac dinh -Xmx384m: VPS 4 GB, nhieu bot cung luc)
#  Ma thoat: 0 = moi nhom DAT; 1 = co nhom HONG / LOI; 2 = sai doi so / khong bien dich duoc.
#  tong_ket.txt: moi dong "<DAT|HONG|LOI|TAT> <nhom> pass=<n> fail=<n> bo_qua=<n> giay=<n>[ lan=<x>][ thu_lai=...]"
#    (thu tu dong = thu tu hoi_quy.conf; "thu_lai=1 lan1=HONG(...)" = nhom da chay lai mot lan, ket qua la cua lan 2).
# ============================================================================
set -uo pipefail
DIR=$(cd "$(dirname "$0")" && pwd)
GOC=$(cd "$DIR/../../.." && pwd)

# ---- bien dich bot: lop may chu TRUOC (bot dung truc tiep vai lop thuan - com.htbb.pokemon.ChieuPokemon / HePokemon - va
# HoiQuy dung com.htbb.pokemon.DuongMenu + real.MenuDoiThuong cho bat bien DUONG_MENU), roi bot. In classpath.
bien_dich() { # $1 = thu muc ra
    local ra=$1 sv="$1/sv-cls" cp="$1/bot-cls"
    command -v javac >/dev/null 2>&1 || { echo "[LOI] khong co javac (apt install openjdk-17-jdk-headless) va khong dat HTBB_BOT_CP"; return 2; }
    mkdir -p "$sv" "$cp"
    find "$GOC/server/src" -name '*.java' > "$ra/sv-nguon.txt"
    if ! javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$GOC/server/lib/jxl-2.6.jar:$GOC/server/lib/mysql-connector-java-5.1.49.jar" \
        -sourcepath "$GOC/server/src" -d "$sv" @"$ra/sv-nguon.txt" > "$ra/bien_dich_sv.log" 2>&1; then
        echo "[LOI] bien dich lop may chu hong:"; tail -n 20 "$ra/bien_dich_sv.log"; return 2
    fi
    if ! javac -encoding UTF-8 -nowarn -cp "$sv" -d "$cp" "$DIR"/*.java > "$ra/bien_dich.log" 2>&1; then
        echo "[LOI] bien dich bot hong:"; tail -n 20 "$ra/bien_dich.log"; return 2
    fi
    echo "$cp:$sv"
}
if [ "${1:-}" = --bien-dich ]; then
    [ $# -eq 2 ] || { echo "dung: $0 --bien-dich <thu muc>"; exit 2; }
    mkdir -p "$2" && bien_dich "$(cd "$2" && pwd)"; exit $?
fi

[ $# -eq 4 ] || { sed -n '2,33p' "$0"; exit 2; }
HOST=$1; CONG=$2; CONG_LOGIN=$3; DBCONN=$4
CONF=${HOI_QUY_CONF:-$DIR/hoi_quy.conf}
BAT_DINH=${HOI_QUY_BAT_DINH:-$DIR/hoi_quy_bat_dinh.txt}
TT=${HOI_QUY_TIEN_TO:-kt}
NOI=${HOI_QUY_NOI:-$GOC}
HE_SO=${HOI_QUY_HE_SO:-1}
SONG_SONG=${HOI_QUY_SONG_SONG:-1}
LAN_TOI_DA=${HOI_QUY_LAN_TOI_DA:-4}
THU_LAI=${HOI_QUY_THU_LAI:-1}
CHE_DO=${HOI_QUY_CHE_DO:-day_du}
case "$CHE_DO" in day_du|nhanh) ;; *) echo "[LOI] HOI_QUY_CHE_DO=$CHE_DO (day_du | nhanh)"; exit 2 ;; esac
read -r -a JAVA_THEM <<< "${HOI_QUY_JAVA:--Xmx384m}"
RA=${HOI_QUY_RA:-$(mktemp -d "${TMPDIR:-/tmp}/hoi-quy.XXXXXX")}
mkdir -p "$RA" "$RA/kq"
rm -f "$RA"/kq/*
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
    CP=$(bien_dich "$RA") || { echo "$CP"; exit 2; }
    CP=$(tail -n 1 <<< "$CP")
fi

# JDBC cho bot: BotChucNang.sql dung MOT ket noi CSDL / JVM (khong mo tien trinh mysql = ket noi moi cho moi truy van;
# cong kiem 25/10/2026 het ket noi MariaDB dung chung voi production). Thieu jar -> bot tu lui ve mysql CLI.
JDBC="$GOC/server/lib/mysql-connector-java-5.1.49.jar"
[ -r "$JDBC" ] && CP="$CP:$JDBC"

chon() { # nhom co duoc chay?
    local n=$1
    if [ -n "${HOI_QUY_NHOM:-}" ]; then case " $HOI_QUY_NHOM " in *" $n "*) ;; *) return 1 ;; esac; fi
    if [ -n "${HOI_QUY_BO:-}" ]; then case " $HOI_QUY_BO " in *" $n "*) return 1 ;; esac; fi
    return 0
}

CHUNG=("${DB_OPT[@]}" "-Dnoi=$NOI" "-Dgoc=$GOC" -Dtu=1 -Dden=40 -DtuThuong=101 -DdenThuong=199 -Dfile.encoding=UTF-8)
[ -n "${HOI_QUY_LOG_GAME:-}" ] && CHUNG+=("-Dlog=$HOI_QUY_LOG_GAME")

# ============================================================== doc hoi_quy.conf
# Dong nhom -> mang theo chi so (thu tu conf = thu tu tong_ket.txt). Dong "@lan" / "@rieng" -> ke hoach lan.
DS=()                       # ten nhom duoc chon (thu tu conf)
declare -A DONG=() TAT_N=() LAN_CUA=()
LANS=()                     # ten lan (thu tu khai)
declare -A LAN_GM=() LAN_TH=() LAN_GMTK=() LAN_NHOM=()
RIENG=" "
# Pass 1: ke hoach lan cua che do dang chay. day_du: "@lan" / "@rieng"; nhanh: "@nhanh lan" / "@nhanh rieng" (dong cua che
# do kia bo qua). Pass 2: dong nhom.
CO_KE_HOACH=0
while IFS= read -r D || [ -n "$D" ]; do
    [ "${D:0:1}" = "@" ] || continue
    D=${D%%#*}
    if [ "${D:0:7}" = "@nhanh " ]; then
        [ "$CHE_DO" = nhanh ] || continue
        D="@$(sed 's/^[[:space:]]*//' <<< "${D:7}")"
    elif [ "$CHE_DO" = nhanh ]; then
        continue
    fi
    TRAI=${D%%:*}; PHAI=""; [[ "$D" == *:* ]] && PHAI=${D#*:}
    read -r -a T <<< "$TRAI"
    CO_KE_HOACH=1
    case "${T[0]}" in
        @rieng) RIENG="$RIENG$PHAI " ;;
        @lan)
            L=${T[1]:-}; [ -n "$L" ] || { echo "[LOI] hoi_quy.conf: '@lan' thieu ten: $D"; exit 2; }
            LANS+=("$L"); LAN_GM[$L]=""; LAN_TH[$L]=""; LAN_GMTK[$L]=""
            for kv in "${T[@]:2}"; do
                case "$kv" in
                    gm=*-*) LAN_GM[$L]=${kv#gm=} ;;
                    thuong=*-*) LAN_TH[$L]=${kv#thuong=} ;;
                    gmtk=*) LAN_GMTK[$L]=${kv#gmtk=} ;;
                    *) echo "[LOI] hoi_quy.conf: '@lan $L' khoa la '$kv'"; exit 2 ;;
                esac
            done
            [ -n "${LAN_GM[$L]}" ] && [ -n "${LAN_TH[$L]}" ] || { echo "[LOI] hoi_quy.conf: '@lan $L' can gm=a-b thuong=c-d"; exit 2; }
            LAN_NHOM[$L]="$PHAI"
            for n in $PHAI; do LAN_CUA[$n]=$L; done ;;
        *) echo "[LOI] hoi_quy.conf: dong '@' la: $D"; exit 2 ;;
    esac
done < "$CONF"
if [ "$CHE_DO" = nhanh ]; then
    [ "$CO_KE_HOACH" = 1 ] || { echo "[LOI] hoi_quy.conf khong co dong '@nhanh lan' / '@nhanh rieng'"; exit 2; }
    TRONG_KH="$RIENG ${LAN_NHOM[*]} "
fi
declare -A DA_DINH_NGHIA=()
while IFS= read -r D || [ -n "$D" ]; do
    case "$D" in ''|'#'*|'@'*) continue ;; esac
    TAT=0
    if [ "${D:0:1}" = "!" ]; then TAT=1; D=${D:1}; fi
    # "?" = nhom DAI (chay dem): chi chay khi goi ten trong HOI_QUY_NHOM, khong thi bo qua im lang (khong tinh HONG/TAT)
    if [ "${D:0:1}" = "?" ]; then
        D=${D:1}
        case " ${HOI_QUY_NHOM:-} " in *" ${D%% *} "*) ;; *) continue ;; esac
    fi
    # "~" = ban RUT GON cho che do nhanh (che do day_du co nhom goc day du): day_du chi chay khi goi ten trong HOI_QUY_NHOM
    if [ "${D:0:1}" = "~" ]; then
        D=${D:1}
        DA_DINH_NGHIA[${D%% *}]=1
        if [ "$CHE_DO" != nhanh ]; then
            case " ${HOI_QUY_NHOM:-} " in *" ${D%% *} "*) ;; *) continue ;; esac
        fi
    fi
    N=${D%% *}
    DA_DINH_NGHIA[$N]=1
    # che do nhanh: chi nhom co trong ke hoach @nhanh
    if [ "$CHE_DO" = nhanh ]; then case "$TRONG_KH" in *" $N "*) ;; *) continue ;; esac; fi
    chon "$N" || continue
    DS+=("$N"); DONG[$N]=$D; TAT_N[$N]=$TAT
done < "$CONF"
if [ "$CHE_DO" = nhanh ]; then
    for n in $TRONG_KH; do [ -n "${DA_DINH_NGHIA[$n]:-}" ] || { echo "[LOI] ke hoach @nhanh goi nhom $n khong co trong hoi_quy.conf"; exit 2; }; done
fi
[ ${#DS[@]} -gt 0 ] || { echo "[LOI] khong co nhom nao duoc chay"; exit 2; }

# Chot ke hoach lan: khoang tai khoan cac lan KHONG chong nhau, nam trong tai khoan lam-moi.sh tao (GM 001..040, thuong
# 101..199), GM chuan bi (gmtk, HoiQuy -DgmTk) khong nam trong khoang GM cua lan nao. Nhom co trong @lan nhung cung
# trong @rieng, hay o hai lan -> LOI cau hinh. Nhom KHONG o lan nao -> chay RIENG (an toan mac dinh).
declare -A HQ_LECH=()
kiem_lan() {
    local a b c d L M k x
    declare -A DUNG=()
    # HoiQuy.tenMoi: 6 chu so co so 10, lan k dung [k*200000, k*200000+200000) -> toi da 4 lan CO NHOM HoiQuy (hqLech 1..4;
    # lan khong co HoiQuy khong can bo dem ten)
    k=0
    for L in "${LANS[@]}"; do
        for x in ${LAN_NHOM[$L]}; do
            if [[ " ${DONG[$x]:-} " == *" HoiQuy "* ]]; then k=$((k + 1)); HQ_LECH[$L]=$k; break; fi
        done
    done
    [ "$k" -le 4 ] || { echo "[LOI] $k lan co nhom HoiQuy - toi da 4 (HoiQuy -DhqLech)"; return 1; }
    for L in "${LANS[@]}"; do
        a=${LAN_GM[$L]%-*}; b=${LAN_GM[$L]#*-}; c=${LAN_TH[$L]%-*}; d=${LAN_TH[$L]#*-}
        for x in "$a" "$b" "$c" "$d"; do [[ "$x" =~ ^[0-9]+$ ]] || { echo "[LOI] @lan $L: so la '$x'"; return 1; }; done
        [ "$a" -ge 1 ] && [ "$b" -le 40 ] && [ "$a" -le "$b" ] || { echo "[LOI] @lan $L: gm=$a-$b ngoai 1-40"; return 1; }
        [ "$c" -ge 101 ] && [ "$d" -le 199 ] && [ "$c" -le "$d" ] || { echo "[LOI] @lan $L: thuong=$c-$d ngoai 101-199"; return 1; }
        for ((k = a; k <= b; k++)); do [ -z "${DUNG[g$k]:-}" ] || { echo "[LOI] tai khoan GM $k o ca lan ${DUNG[g$k]} va $L"; return 1; }; DUNG[g$k]=$L; done
        for ((k = c; k <= d; k++)); do [ -z "${DUNG[t$k]:-}" ] || { echo "[LOI] tai khoan thuong $k o ca lan ${DUNG[t$k]} va $L"; return 1; }; DUNG[t$k]=$L; done
    done
    for L in "${LANS[@]}"; do
        M=${LAN_GMTK[$L]}
        [ -z "$M" ] && continue
        [[ "$M" =~ ^[0-9]+$ ]] && [ "$M" -ge 1 ] && [ "$M" -le 40 ] || { echo "[LOI] @lan $L: gmtk=$M ngoai 1-40"; return 1; }
        [ -z "${DUNG[g$M]:-}" ] || { echo "[LOI] @lan $L: gmtk=$M nam trong khoang GM cua lan ${DUNG[g$M]}"; return 1; }
        DUNG[g$M]="$L(gmtk)"
    done
    for n in $RIENG; do [ -z "${LAN_CUA[$n]:-}" ] || { echo "[LOI] nhom $n vua @rieng vua @lan ${LAN_CUA[$n]}"; return 1; }; done
    for L in "${LANS[@]}"; do for n in ${LAN_NHOM[$L]}; do [ "${LAN_CUA[$n]}" = "$L" ] || { echo "[LOI] nhom $n o hai lan"; return 1; }; done; done
    # nhom trong lan tu dat khoang tai khoan (-Dtu / -Dden / -DtuThuong / -DdenThuong / -DgmTk) = de len khoang cua lan
    # -> co the dung tai khoan cua lan khac cung luc. Nhom nhu vay phai chay @rieng.
    for n in "${!LAN_CUA[@]}"; do
        [ -n "${DONG[$n]:-}" ] || continue
        if [[ " ${DONG[$n]%% -- *} " =~ \ -D(tu|den|tuThuong|denThuong|gmTk|thuong)= ]]; then
            echo "[LOI] nhom $n (lan ${LAN_CUA[$n]}) tu dat khoang tai khoan (${BASH_REMATCH[0]# }) - chuyen sang @rieng"; return 1
        fi
    done
    return 0
}
kiem_lan || exit 2

# Tham so JVM cua lan: khoang tai khoan rieng + GM chuan bi rieng (HoiQuy -DgmTk; mac dinh kt040) + tai khoan thuong co
# dinh cua KichBanDS4.chat (-Dthuong, mac dinh kt101) = tai khoan thuong dau cua lan.
tham_so_lan() { # $1 = lan ("" = rieng: khoang day du)
    local L=$1
    [ -z "$L" ] && return 0
    local a=${LAN_GM[$L]%-*} b=${LAN_GM[$L]#*-} c=${LAN_TH[$L]%-*} d=${LAN_TH[$L]#*-}
    # -DhqLech: bo dem ten tai khoan moi cua HoiQuy rieng cho moi lan co HoiQuy (hai JVM cung luc khong trung ten)
    printf '%s\n' "-Dtu=$a" "-Dden=$b" "-DtuThuong=$c" "-DdenThuong=$d" "-Dthuong=$(printf '%s%03d' "$TT" "$c")" "-DhqLech=${HQ_LECH[$L]:-0}"
    [ -n "${LAN_GMTK[$L]}" ] && printf '%s\n' "-DgmTk=$(printf '%s%03d' "$TT" "${LAN_GMTK[$L]}")"
    return 0
}

# ============================================================== chay mot nhom
LOG_GAME=${HOI_QUY_LOG_GAME:-}
kich_thuoc_log() { if [ -n "$LOG_GAME" ] && [ -f "$LOG_GAME" ]; then stat -c %s "$LOG_GAME"; else echo 0; fi; }
BO_QUA_LOG=$GOC/vps/staging/loi-bo-qua.txt

# Exception may chu (nhu buoc 4 cong kiem) trong cua so log [tu, den) - in so dong.
loi_may_chu() {
    local tu=$1 den=$2
    [ -n "$LOG_GAME" ] && [ -f "$LOG_GAME" ] && [ "$den" -gt "$tu" ] || { echo 0; return; }
    tail -c +"$((tu + 1))" "$LOG_GAME" 2>/dev/null | head -c "$((den - tu))" | grep -E 'Exception|Error[: ]' | grep -vE '\[MANG\]' \
        | { if [ -s "$BO_QUA_LOG" ]; then grep -vEf <(grep -vE '^\s*(#|$)' "$BO_QUA_LOG"); else cat; fi; } | wc -l
}

# Muc FAIL cua log nhom: "<muc>" moi dong. BotChucNang: "FAIL   <muc> ..."; HoiQuy: "FAIL     <kich ban> <muc> ...".
muc_fail() { # $1 log $2 lop
    if [ "$2" = HoiQuy ]; then awk '$1 == "FAIL" {print $3}' "$1"; else awk '$1 == "FAIL" {print $2}' "$1"; fi
}

# Co duoc THU LAI khong? In ly do ra stdout; 0 = duoc. Chi khi: nhom HONG (co dong TONG, ma thoat 1 - khong het gio /
# sap / LOI), MOI muc FAIL khop mot mau BAT DINH cua dung nhom trong hoi_quy_bat_dinh.txt, KHONG muc nao la log may chu /
# bat bien / ngoai le / may chu chet / CSDL, dong FAIL khong chua ngoai le, va log may chu khong co Exception moi trong
# luc nhom chay. Loi log / BatBien / Exception KHONG BAO GIO duoc thu lai.
duoc_thu_lai() { # $1 nhom $2 kq $3 ma $4 log $5 lop $6 so loi may chu
    local n=$1 kq=$2 ma=$3 log=$4 lop=$5 loi_sv=$6 m mau ok
    [ "$THU_LAI" = 1 ] || { echo "tat (HOI_QUY_THU_LAI=0)"; return 1; }
    [ "$kq" = HONG ] && [ "$ma" = 1 ] || { echo "ket qua $kq ma=$ma (chi thu lai HONG ma 1)"; return 1; }
    [ -s "$BAT_DINH" ] || { echo "khong co $BAT_DINH"; return 1; }
    [ "$loi_sv" = 0 ] || { echo "$loi_sv dong Exception may chu trong luc nhom chay"; return 1; }
    if grep -E '^FAIL' "$log" | grep -qiE 'ngoai le|Exception|Error[: ]|\[SQL\] LOI'; then echo "dong FAIL co ngoai le / loi SQL"; return 1; fi
    local ds; ds=$(muc_fail "$log" "$lop" | sort -u)
    [ -n "$ds" ] || { echo "khong doc duoc muc FAIL"; return 1; }
    local goc=0 loai
    while read -r m; do
        if [[ "$m" =~ (log|bat_bien|ngoai_le|may_chu|con_song|csdl|bao_toan|exception) ]]; then echo "muc $m la log / bat bien / may chu / CSDL"; return 1; fi
        ok=0
        while read -r nn mau loai; do
            # ban rut gon cua che do nhanh (<nhom>_nhanh, <nhom>_nhanh1...) dung chung danh sach bat dinh cua nhom goc
            [ "$nn" = "$n" ] || [ "$nn" = "${n%_nhanh*}" ] || continue
            if [[ "$m" =~ $mau ]]; then ok=1; [ "$loai" = phu ] || goc=1; break; fi
        done < <(grep -vE '^\s*(#|$)' "$BAT_DINH" | sed -E 's/[[:space:]]+#.*$//')
        [ "$ok" = 1 ] || { echo "muc $m khong nam trong danh sach bat dinh"; return 1; }
    done <<< "$ds"
    # muc "phu" (hong KEO THEO muc goc, vd Hop PC them con khi bat hong) chi duoc thu lai khi muc goc cung hong
    [ "$goc" = 1 ] || { echo "chi co muc phu thuoc ($(tr '\n' ' ' <<< "$ds")) ma muc goc bat dinh khong hong"; return 1; }
    echo "moi muc FAIL bat dinh: $(tr '\n' ' ' <<< "$ds")"
    return 0
}

# Chay mot lan JVM cua nhom; dat bien KQ P F B MA GIAY.
chay_jvm() { # $1 nhom $2 log $3 ketqua $4 lan
    local n=$1 log=$2 kqf=$3 L=$4
    local D=${DONG[$n]} TRUOC SAU KIEU LOP GH
    TRUOC=${D%% -- *}; SAU=""
    [[ "$D" == *" -- "* ]] && SAU=${D#* -- }
    [[ "$D" == *" --" ]] && TRUOC=${D% --}
    local T OPT DOI LOPT
    read -r -a T <<< "$TRUOC"
    KIEU=${T[1]}; LOP=${T[2]}; GH=$(( ${T[3]} * HE_SO )); OPT=("${T[@]:4}")
    read -r -a DOI <<< "${SAU//@TT@/$TT}"
    mapfile -t LOPT < <(tham_so_lan "$L")
    local BD; BD=$(date +%s)
    case "$KIEU" in
        cn) timeout -k 15 "$GH" java "${JAVA_THEM[@]}" "${CHUNG[@]}" "${LOPT[@]}" "${OPT[@]}" "-Dra=$kqf" -cp "$CP" "$LOP" "$HOST" "$CONG" "$TT" "${DOI[@]}" < /dev/null > "$log" 2>&1 ;;
        bt) timeout -k 15 "$GH" java "${JAVA_THEM[@]}" "${CHUNG[@]}" "${LOPT[@]}" "${OPT[@]}" -cp "$CP" "$LOP" "$HOST" "$CONG" "${DOI[@]}" < /dev/null > "$log" 2>&1 ;;
        *) echo "kieu la '$KIEU' o nhom $n" > "$log"; false ;;
    esac
    MA=$?
    GIAY=$(( $(date +%s) - BD ))
    P=0; F=0; B=0; KQ=LOI
    case "$KIEU" in
        cn)
            local DD; DD=$(grep -E '^=== TONG: PASS [0-9]+, FAIL [0-9]+, BO QUA [0-9]+' "$log" | tail -1)
            if [ -n "$DD" ]; then
                P=$(sed -E 's/.*PASS ([0-9]+).*/\1/' <<< "$DD"); F=$(sed -E 's/.*FAIL ([0-9]+).*/\1/' <<< "$DD"); B=$(sed -E 's/.*BO QUA ([0-9]+).*/\1/' <<< "$DD")
                if [ "$MA" = 0 ] && [ "$F" = 0 ]; then KQ=DAT; else KQ=HONG; fi
            fi ;;
        bt)
            local V LL a nn
            V=$(grep -oE '^vao game [0-9]+/[0-9]+' "$log" | tail -1)
            LL=$(grep -oE '^loi=[0-9]+' "$log" | tail -1)
            if [ -n "$V" ] && [ -n "$LL" ]; then
                a=${V#vao game }; P=${a%/*}; nn=${a#*/}; F=$(( nn - P + ${LL#loi=} ))
                if [ "$MA" = 0 ] && [ "$F" = 0 ]; then KQ=DAT; else KQ=HONG; fi
            fi ;;
    esac
    if [ "$MA" = 124 ] || [ "$MA" = 137 ]; then KQ=LOI; echo "   HET GIO sau ${GH}s" >> "$log"; fi
    # Lan chay chia khoang tai khoan: nhom dung qua khoang -> quay vong = hai bot co the cung mot tai khoan (bao, khong giau).
    if [ -n "$L" ] && grep -q '^\[TK\] QUAY VONG' "$log"; then
        echo "   [CANH BAO] nhom $n dung het khoang tai khoan cua lan $L (quay vong) - mo rong khoang trong @lan" | tee -a "$log"
    fi
    LOP_N=$LOP
}

chay_nhom() { # $1 nhom $2 lan ("" = rieng)
    local n=$1 L=$2 nhan=${2:-rieng}
    local log="$RA/$n.log" kqf="$RA/$n.ketqua"
    if [ "${TAT_N[$n]}" = 1 ]; then
        echo "TAT  $n pass=0 fail=0 bo_qua=0 giay=0" > "$RA/kq/$n.dong"; echo "-- $n: TAM TAT trong hoi_quy.conf"
        return 0
    fi
    local GHD; GHD=$(read -r -a X <<< "${DONG[$n]}"; echo $(( ${X[3]} * HE_SO )))
    echo "-- [$nhan] $n (toi da ${GHD}s) ..."
    local L0 L1 BD0 THEM=""
    BD0=$(date +%s); L0=$(kich_thuoc_log)
    chay_jvm "$n" "$log" "$kqf" "$L"
    L1=$(kich_thuoc_log)
    local GIAY_TONG=$GIAY
    if [ "$KQ" = HONG ]; then
        local LY loi_sv
        loi_sv=$(loi_may_chu "$L0" "$L1")
        if LY=$(duoc_thu_lai "$n" "$KQ" "$MA" "$log" "$LOP_N" "$loi_sv"); then
            local KQ1=$KQ F1=$F MUC1
            MUC1=$(muc_fail "$log" "$LOP_N" | sort -u | paste -sd, -)
            mv "$log" "$RA/$n.lan1.log"; [ -f "$kqf" ] && mv "$kqf" "$RA/$n.lan1.ketqua"
            echo "   [THU LAI] $n lan 1 $KQ1 fail=$F1 ($MUC1) - $LY -> chay lai mot lan (log lan 1: $n.lan1.log)"
            echo "THU_LAI $n lan1=$KQ1 fail=$F1 muc=$MUC1 ly_do=$LY" >> "$RA/kq/thu_lai.txt"
            chay_jvm "$n" "$log" "$kqf" "$L"
            GIAY_TONG=$(( GIAY_TONG + GIAY ))
            THEM=" thu_lai=1 lan1=$KQ1(fail=$F1:$MUC1)"
        else
            echo "   [KHONG THU LAI] $n: $LY"
        fi
    fi
    echo "$BD0 $(date +%s) $n" >> "$RA/kq/thoi_gian.txt"
    local LAN_TXT=""; [ -n "$L" ] && LAN_TXT=" lan=$L"
    printf '%-4s %s pass=%s fail=%s bo_qua=%s giay=%s%s%s\n' "$KQ" "$n" "$P" "$F" "$B" "$GIAY_TONG" "$LAN_TXT" "$THEM" > "$RA/kq/$n.dong"
    cat "$RA/kq/$n.dong"
    if [ "$KQ" != DAT ]; then
        grep -E '^(FAIL|HONG)|ngoai le|Exception|HET GIO' "$log" | head -n 8 | sed 's/^/     /'
    fi
    return 0
}

# ============================================================== ke hoach
LAN_CHAY=()          # lan co it nhat mot nhom duoc chon
declare -A LAN_DS=()
DS_RIENG=()
for n in "${DS[@]}"; do
    L=${LAN_CUA[$n]:-}
    if [ "$SONG_SONG" = 1 ] && [ -n "$L" ]; then
        [ -n "${LAN_DS[$L]:-}" ] || LAN_CHAY+=("$L")
        LAN_DS[$L]="${LAN_DS[$L]:-} $n"
    else
        DS_RIENG+=("$n")
        if [ "$SONG_SONG" = 1 ] && [[ "$RIENG" != *" $n "* ]]; then
            echo "   [CANH BAO] nhom $n khong co trong @lan / @rieng cua hoi_quy.conf -> chay RIENG"
        fi
    fi
done

BD_TONG=$(date +%s)
echo "== Hoi quy bot ($CHE_DO) -> $HOST:$CONG (login $CONG_LOGIN), ket qua: $RA"
if [ ${#LAN_CHAY[@]} -gt 0 ]; then
    echo "== Pha 1: ${#LAN_CHAY[@]} lan song song (toi da $LAN_TOI_DA cung luc; moi lan 1 JVM bot = 1 ket noi CSDL)"
    for L in "${LAN_CHAY[@]}"; do echo "   lan $L (gm ${LAN_GM[$L]}, thuong ${LAN_TH[$L]}${LAN_GMTK[$L]:+, gmtk ${LAN_GMTK[$L]}}):${LAN_DS[$L]}"; done
    DANG=0
    for L in "${LAN_CHAY[@]}"; do
        if [ "$DANG" -ge "$LAN_TOI_DA" ]; then wait -n; DANG=$((DANG - 1)); fi
        ( for n in ${LAN_DS[$L]}; do chay_nhom "$n" "$L"; done ) 2>&1 | sed -u "s/^/[$L] /" &
        DANG=$((DANG + 1))
    done
    wait
    echo "== Pha 1 xong sau $(( $(date +%s) - BD_TONG ))s"
fi
if [ ${#DS_RIENG[@]} -gt 0 ]; then
    BD2=$(date +%s)
    [ ${#LAN_CHAY[@]} -gt 0 ] && echo "== Pha 2: ${#DS_RIENG[@]} nhom chay RIENG (dong ho ao / cau hinh toan may chu / chiem Gym...): ${DS_RIENG[*]}"
    for n in "${DS_RIENG[@]}"; do chay_nhom "$n" ""; done
    [ ${#LAN_CHAY[@]} -gt 0 ] && echo "== Pha 2 xong sau $(( $(date +%s) - BD2 ))s"
fi

# tong_ket.txt theo thu tu conf
for n in "${DS[@]}"; do [ -f "$RA/kq/$n.dong" ] && cat "$RA/kq/$n.dong" >> "$TONG"; done
SO=$(grep -cvE '^TAT' "$TONG")

# Nhom khong DAT chay CUNG LUC voi nhom nao (log may chu dung chung: loi log / Exception co the do nhom kia gay ra -
# van la HONG, chi de doc nhanh hon).
if [ -f "$RA/kq/thoi_gian.txt" ] && grep -qvE '^(DAT|TAT)' "$TONG"; then
    echo "== Nhom khong DAT va nhom chay cung luc:"
    while read -r K N _; do
        [ "$K" = DAT ] || [ "$K" = TAT ] && continue
        read -r A1 A2 _ < <(awk -v n="$N" '$3 == n' "$RA/kq/thoi_gian.txt")
        [ -n "${A1:-}" ] || continue
        CUNG=$(awk -v a="$A1" -v b="$A2" -v n="$N" '$3 != n && $1 < b && $2 > a {print $3}' "$RA/kq/thoi_gian.txt" | paste -sd' ' -)
        echo "   $K $N: ${CUNG:-(chay mot minh)}"
    done < "$TONG"
fi
if [ -s "$RA/kq/thu_lai.txt" ]; then
    echo "== THU LAI (loi bat dinh da biet, moi nhom toi da 1 lan; log lan 1 giu lai <nhom>.lan1.log):"
    sed 's/^/   /' "$RA/kq/thu_lai.txt"
    cp "$RA/kq/thu_lai.txt" "$RA/thu_lai.txt"
fi

echo "== $SO nhom, $(( $(date +%s) - BD_TONG ))s: $(grep -c '^DAT' "$TONG") DAT, $(grep -cE '^(HONG|LOI)' "$TONG") HONG/LOI, $(grep -c '^TAT' "$TONG") tam tat, $(grep -c 'thu_lai=1' "$TONG") thu lai"
[ "$SO" -gt 0 ] || [ "$(grep -c . "$TONG")" -gt 0 ] || { echo "[LOI] khong co nhom nao duoc chay"; exit 2; }
# Chot: moi nhom duoc chon PHAI co dong tong ket (lan chay chet giua chung = mat nhom im lang).
CAN=${#DS[@]}
if [ "$(grep -c . "$TONG")" != "$CAN" ]; then
    echo "[LOI] chi co $(grep -c . "$TONG")/$CAN nhom co ket qua"; exit 1
fi
grep -qE '^(HONG|LOI)' "$TONG" && exit 1
exit 0
