#!/bin/bash
# ============================================================================
#  Sinh server.ini STAGING tu server.ini PRODUCTION (cung gameplay, khac cong / CSDL / thu muc / user).
#
#      sudo bash /opt/htbb/vps/staging/sinh-server-ini.sh          (lam-moi.sh goi)
#      sudo bash /opt/htbb/vps/staging/sinh-server-ini.sh --thu    in ban sinh ra (che bi mat), khong cai
#
#  Nguon: /opt/htbb/server/server.ini (do vps/sinh-server-ini.sh sinh tu vps/server.ini.tmpl). Moi khoa giu nguyen
#  TRU danh sach DOI ben duoi (dong cu bi bo, dong moi ghi o cuoi - moi khoa mot lan). Cung sinh
#  loginServer/server.ini (CSDL tai khoan staging, cong 28023).
#
#  KHONG BAO GIO in mat khau: --thu che db.password / db.pass / db.password1.
# ============================================================================
set -euo pipefail
DIR_SCRIPT=$(cd "$(dirname "$0")" && pwd)
# shellcheck source=vps/staging/chung.sh
. "$DIR_SCRIPT/chung.sh"

THU=0
[ "${1:-}" = "--thu" ] && THU=1
NGUON=${NGUON_INI:-$HTBB_GOC/server/server.ini}
DICH=$STG_GOC/server/server.ini
DICH_LOGIN=$STG_GOC/server/loginServer/server.ini
[ -r "$NGUON" ] || loi "khong doc duoc $NGUON (server.ini production)"

# GM staging = tai khoan bot <tt>001..<tt>040 (+ admin). <= 255 ky tu (cot cau_hinh.gia_tri).
GM="admin"
for i in $(seq 1 40); do GM="$GM,$(printf '%s%03d' "$STG_BOT_TIEN_TO" "$i")"; done

# Goc tai tep cap nhat trong game: GIU dung goc production clients nhan (sv.gocTaiCapNhat, trong = sv.website cua
# production) - staging doi sv.website sang 127.0.0.1 nhung qa_cap_nhat.tai phai tai DUNG link nguoi choi that nhan
# (vd ten mien sau WAF -> 403 thi cong kiem phai do). Dong cau_hinh cung khoa (CSDL ban sao) van thang.
gia_tri_nguon() { grep -E "^[[:space:]]*$1[[:space:]]*=" "$NGUON" | tail -n 1 | sed -E 's/^[^=]*=[[:space:]]*//; s/[[:space:]]+$//' || true; }
GOC_TAI=$(gia_tri_nguon 'sv\.gocTaiCapNhat')
[ -n "$GOC_TAI" ] || GOC_TAI=$(gia_tri_nguon 'sv\.website')

MK=$(stg_mat_khau)
DB_HOST="$STG_DB_HOST:$STG_DB_PORT"
# khoa=gia tri bi thay (thu tu = thu tu ghi ra).
DOI=(
    "sv.port=$STG_CONG_GAME"
    "sv.ipLangNghe=127.0.0.1"
    "sv.portnap=$STG_CONG_NAP"
    "sv.iplogin=127.0.0.1"
    "sv.portlogin=$STG_CONG_LOGIN"
    "db.host=$DB_HOST"
    "db.name=$STG_DB"
    "db.user=$STG_DB_USER"
    "db.password=$MK"
    "db.choPhepMatKhauTrong=0"
    "db.nameNap=$STG_DB"
    "db.userNap=$STG_DB_USER"
    "db.pass=$MK"
    "db.host1=$DB_HOST"
    "db.name1=$STG_DB"
    "db.user1=$STG_DB_USER"
    "db.password1=$MK"
    # Pool NHO: ConnPool mo san du so ket noi luc khoi dong; chep 100+30+30+1 cua production = vuot max_connections cua
    # MariaDB dung chung (cong kiem 25/10/2026: ERROR 1040). Xem STG_DB_* trong chung.sh.
    "db.maxco=$STG_DB_MAXCO"
    "db.maxNap=$STG_DB_MAXNAP"
    "db.maxco1=1"
    "sv.pathLog=$STG_GOC/logs/"
    "sv.dangKyCsdlTaiKhoan=$STG_DB_TK"
    "sv.adminAccounts=$GM"
    # Bot can GM slv / mto / tua (dong ho ao chi khi cho_phep_thu_nghiem=1). Staging khong co nguoi choi that.
    "sv.cho_phep_thu_nghiem=1"
    # Nhan vat bot GM ten = ten tai khoan (<tt>001..) nam trong sv.adminAccounts -> phai cho tao ten GM.
    "sv.choPhepTaoTenGM=1"
    # Khong tu bao tri / khoi dong lai giua luc bot chay.
    "sv.gioBaoTri=tat"
    "bao_tri.bat=0"
    # Bot gui so ban rieng (907/908/915...) - khong chan theo ban toi thieu cua production.
    "sv.banToiThieu=0"
    "sv.website=http://127.0.0.1/"
    "sv.gocTaiCapNhat=$GOC_TAI"
)

MOI=$(mktemp)
trap 'rm -f "$MOI" "$MOI.l" "$MOI.cnf"' EXIT
chmod 600 "$MOI"
{
    echo "# SINH TU DONG boi vps/staging/sinh-server-ini.sh tu $NGUON luc $(date '+%F %T'). KHONG sua tay."
    echo "# STAGING: cong $STG_CONG_GAME (127.0.0.1), CSDL $STG_DB / $STG_DB_TK, user $STG_DB_USER."
    # Bo dong cua moi khoa se thay (ke ca khoa khai hai lan o nguon).
    LOC=$(printf '%s\n' "${DOI[@]}" | sed -E 's/=.*//; s/[.]/\\./g' | paste -sd'|')
    grep -vE "^[[:space:]]*($LOC)[[:space:]]*=" "$NGUON" | grep -v '^# SINH TU DONG\|^# STAGING:' || true
    echo
    echo "# ==== STAGING (ghi de) ===="
    printf '%s\n' "${DOI[@]}"
} > "$MOI"

TRUNG=$(grep -vE '^[[:space:]]*#' "$MOI" | grep -oE '^[^=]+=' | sed -E 's/=$//; s/[[:space:]]+//g' | sort | uniq -d | tr '\n' ' ')
[ -z "$TRUNG" ] || loi "khoa khai hai lan trong ban sinh: $TRUNG"
# Chot cach ly: khong con dong lenh nao tro toi CSDL / cong / thu muc production.
if grep -vE '^[[:space:]]*#' "$MOI" | grep -qE '^db\.(name|nameNap|name1)=(htbb_db|account)$|^sv\.port=19129$|^sv\.portlogin=8023$|^sv\.pathLog=/opt/htbb/'; then
    loi "ban sinh con tro toi production - DUNG"
fi
# Chot ket noi: tong pool mo san (maxco + 2*maxNap + maxco1) phai nam duoi tran user staging, chua cho bot + login.
POOL=$(( $(grep -E '^db\.maxco=' "$MOI" | tail -n 1 | cut -d= -f2) + 2 * $(grep -E '^db\.maxNap=' "$MOI" | tail -n 1 | cut -d= -f2) \
         + $(grep -E '^db\.maxco1=' "$MOI" | tail -n 1 | cut -d= -f2) ))
[ "$POOL" -le $(( STG_DB_MAX_USER_CONN - 10 )) ] \
    || loi "pool CSDL staging $POOL ket noi >= tran user $STG_DB_MAX_USER_CONN - 10 (STG_DB_MAXCO / STG_DB_MAXNAP qua lon)"

{
    echo "# SINH TU DONG boi vps/staging/sinh-server-ini.sh - login server STAGING."
    echo "db.url=jdbc:mysql://$DB_HOST/$STG_DB_TK?characterEncoding=UTF-8"
    echo "db.user=$STG_DB_USER"
    echo "db.password=$MK"
    echo "port=$STG_CONG_LOGIN"
} > "$MOI.l"

if [ "$THU" = 1 ]; then
    sed -E 's/^(db\.(password|pass|password1))=.*/\1=<CHE>/' "$MOI"
    echo "---- loginServer/server.ini"
    sed -E 's/^(db\.password)=.*/\1=<CHE>/' "$MOI.l"
    exit 0
fi

# Thu ket noi bang chinh cau hinh moi.
stg_tao_cnf "$MOI.cnf"
mysql --defaults-file="$MOI.cnf" -N -e "SELECT 1" "$STG_DB" >/dev/null 2>&1 \
    || loi "user $STG_DB_USER khong vao duoc $STG_DB (chay cai-dat.sh? CSDL da nap?)"

install -d -o "$STG_USER" -g "$STG_USER" -m 750 "$(dirname "$DICH_LOGIN")"
install -o "$STG_USER" -g "$STG_USER" -m 640 "$MOI" "$DICH"
install -o "$STG_USER" -g "$STG_USER" -m 640 "$MOI.l" "$DICH_LOGIN"
echo "   da sinh $DICH + $DICH_LOGIN"
