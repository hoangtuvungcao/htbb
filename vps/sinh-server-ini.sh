#!/bin/bash
# ============================================================================
#  Sinh /opt/kpah/server.ini tu vps/server.ini.tmpl (co trong git) + bi mat.
#
#  Goi tu vps/deploy-auto.sh sau khi git pull. Chay tay de xem truoc cung duoc:
#      sudo /opt/kpah/vps/sinh-server-ini.sh --thu
#
#  Ma thoat:
#      0  cau hinh khong doi, khong lam gi
#      3  cau hinh DA DOI va da cai dat -> nguoi goi phai restart server
#      1  co loi -> KHONG cai gi ca, file dang chay giu nguyen
#
#  NGUYEN TAC: thua khong deploy con hon deploy mot cau hinh hong. Moi loi deu
#  giu nguyen /opt/kpah/server.ini hien tai, vi mot file server.ini sai lam
#  server chet lung chung ma systemd van bao "active" (su co 30/08/2026).
#
#  KHONG BAO GIO in gia tri bi mat ra stdout: log cua script nay chay thang ve
#  GitHub Actions, ai xem repo cung doc duoc.
# ============================================================================
set -uo pipefail

DIR=${KPAH_DIR:-/opt/kpah}
TMPL="$DIR/vps/server.ini.tmpl"
DICH="$DIR/server.ini"
LUU="$DIR/backup"
USER_SV=kpah
MAT_KHAU_MAU=DOI_MAT_KHAU_DB
THU=0
[ "${1:-}" = "--thu" ] && THU=1

loi() { echo "[LOI cau hinh] $*"; exit 1; }

[ -f "$TMPL" ] || { echo "Khong co $TMPL -> bo qua buoc sinh cau hinh."; exit 0; }

# ---- Doc gia tri dang chay, de lam nguon du phong ----
declare -A DANG_CHAY
if [ -f "$DICH" ]; then
    while IFS= read -r d || [ -n "$d" ]; do
        case "$d" in ''|'#'*) continue ;; *=*) ;; *) continue ;; esac
        k=${d%%=*}; k=${k//[[:space:]]/}
        DANG_CHAY["$k"]=${d#*=}
    done < "$DICH"
fi

# ---- Dung file moi tu ban mau ----
MOI=$(mktemp) || exit 1
THIEU=""
trap 'rm -f "$MOI" "${MOI}.cnf"' EXIT

while IFS= read -r dong || [ -n "$dong" ]; do
    case "$dong" in
        ''|'#'*) printf '%s\n' "$dong"; continue ;;
        *=*) ;;
        *) printf '%s\n' "$dong"; continue ;;
    esac
    khoa=${dong%%=*}
    gt=${dong#*=}

    if [[ "$gt" =~ ^\$\{([A-Za-z0-9_]+)\}$ ]]; then
        bien=${BASH_REMATCH[1]}
        tu_env=${!bien-}
        if [ -n "$tu_env" ]; then
            gt=$tu_env
        elif [ -n "${DANG_CHAY[$khoa]-}" ]; then
            # Chua dat secret tren GitHub -> giu nguyen cai dang chay. Nho vay lan
            # deploy dau tien sau khi them ban mau nay KHONG lam sap server.
            gt=${DANG_CHAY[$khoa]}
        else
            THIEU="$THIEU $khoa(\$$bien)"
            gt=""
        fi
    elif [ "$gt" = "@GIU_NGUYEN@" ]; then
        gt=${DANG_CHAY[$khoa]-}
    fi

    printf '%s=%s\n' "$khoa" "$gt"
done < "$TMPL" > "$MOI"

[ -z "$THIEU" ] || loi "thieu gia tri cho:$THIEU
  -> dat secret tuong ung trong GitHub: Settings > Secrets and variables > Actions"

# ---- Kiem tra truoc khi cai ----
lay() { grep -m1 -E "^$1=" "$MOI" | sed -E "s/^[^=]*=//"; }

# Chi xet dong lenh: phan huong dan dau ban mau co nhac chu ${TEN_BIEN} nen quet ca
# file thi luc nao cung bao dong gia.
if grep -vE '^[[:space:]]*#' "$MOI" | grep -q '\${'; then
    loi "con cho trong chua thay the trong ban sinh ra"
fi

# Dung 23 khoa nay di qua parseInt/parseByte/parseShort trong TeamServer. Thieu mot
# cai la ca khoi doc cau hinh nem loi va MOI DONG BEN DUOI bi bo qua, ma server
# van khoi dong. Xem docs/cau-hinh-server-ini.md muc 10.
for k in api.sv sv.port sv.server sv.portnap sv.portlogin limit.ccu sv.LG sv.range \
         sv.lang sv.me sv.createRace sv.expvantieu sv.Luckyexp sv.ExpQua sv.CodeTest \
         sv.EnableAvtiveCheck db.hour db.minute sv.isSuKienTrungThul2016 \
         sv.isSuKienTet2017 sv.isSuKienGioTo2016 sv.isSuKienNoel2023 \
         sv.isSuKienTetduonglich2024; do
    v=$(lay "$k")
    [ -n "$v" ] || loi "thieu khoa bat buoc $k (thieu la server doc cau hinh nua chung)"
done
for k in db.host db.name db.user db.password db.maxco; do
    [ -n "$(lay "$k")" ] || loi "thieu khoa CSDL $k"
done

MK=$(lay db.password)
[ "$MK" = "$MAT_KHAU_MAU" ] && loi "db.password van la chuoi mau $MAT_KHAU_MAU"

# ---- Thu ket noi that truoc khi cai ----
# Day la phep chan quan trong nhat: mat khau sai ma van cai thi server bat len
# roi chet lung chung, con systemd bao "active (running)".
HOST=$(lay db.host); CONG=3306
case "$HOST" in *:*) CONG=${HOST##*:}; HOST=${HOST%%:*} ;; esac
umask 077
cat > "${MOI}.cnf" <<CNF
[client]
host=$HOST
port=$CONG
user=$(lay db.user)
password=$MK
CNF
# PHAI la --defaults-file chu KHONG phai --defaults-extra-file: cai sau chi doc THEM,
# va /root/.my.cnf (tao cho backup_db.sh) duoc doc SAU nen de len, khien phep thu ket noi
# luon thanh cong du mat khau sai bet - dung cai bay nay da lam phep thu vo dung.
if ! mysql --defaults-file="${MOI}.cnf" -N -e "select 1" "$(lay db.name)" >/dev/null 2>&1; then
    loi "khong ket noi duoc CSDL bang cau hinh moi -> GIU NGUYEN file dang chay"
fi
rm -f "${MOI}.cnf"

# ---- Cai dat ----
if [ -f "$DICH" ] && cmp -s "$MOI" "$DICH"; then
    echo "Cau hinh khong doi."
    exit 0
fi

if [ "$THU" = 1 ]; then
    echo "(--thu) Cau hinh SE DOI. Khac biet (gia tri bi mat da bi che):"
    diff <(sed -E 's/^(db\.(password|pass|password1|user|userNap|user1))=.*/\1=<CHE>/' "$DICH" 2>/dev/null) \
         <(sed -E 's/^(db\.(password|pass|password1|user|userNap|user1))=.*/\1=<CHE>/' "$MOI") || true
    exit 3
fi

mkdir -p "$LUU"
if [ -f "$DICH" ]; then
    cp -a "$DICH" "$LUU/server.ini.$(date +%F_%H%M%S)"
    ls -1t "$LUU"/server.ini.* 2>/dev/null | tail -n +11 | xargs -r rm -f
fi
install -o "$USER_SV" -g "$USER_SV" -m 640 "$MOI" "$DICH"
echo "Da sinh lai $DICH tu ban mau (ban cu luu o $LUU)."
exit 3
