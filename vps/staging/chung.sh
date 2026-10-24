# shellcheck shell=bash
# ============================================================================
#  Cau hinh + ham dung chung cua STAGING va CONG KIEM (source, khong chay truc tiep).
#
#  Moi gia tri doi duoc bang bien moi truong cung ten - de chay thu tren may dev (xem docs/trien-khai.md,
#  muc "Staging va cong kiem"). Tren VPS de mac dinh.
#
#  CACH LY voi production (cung VPS):
#    CSDL     htbb_staging + htbb_staging_account, user MariaDB RIENG htbb_staging (chi co quyen tren 2 CSDL nay,
#             mat khau ngau nhien trong /etc/htbb/staging.env) - staging khong the doc/ghi htbb_db / account.
#    cong     game 29129, login 28023, nap 29153: game nghe 127.0.0.1 (sv.ipLangNghe), ca hai unit
#             IPAddressDeny=any + IPAddressAllow=localhost, ufw chan them 29129/28023.
#    thu muc  /opt/htbb-staging/{server,sql,logs}, unit htbb-staging-game / htbb-staging-login.
#    tai nguyen CPUQuota / MemoryMax / Nice / IO idle; heap 768 MB; KHONG bat cung may (cong kiem bat -> tat).
# ============================================================================

HTBB_GOC=${HTBB_GOC:-/opt/htbb}                        # production (ban checkout monorepo)
STG_GOC=${STG_GOC:-/opt/htbb-staging}
STG_CONG_GAME=${STG_CONG_GAME:-29129}
STG_CONG_LOGIN=${STG_CONG_LOGIN:-28023}
STG_CONG_NAP=${STG_CONG_NAP:-29153}
STG_DB=${STG_DB:-htbb_staging}
STG_DB_TK=${STG_DB_TK:-htbb_staging_account}
STG_DB_USER=${STG_DB_USER:-htbb_staging}
STG_DB_HOST=${STG_DB_HOST:-127.0.0.1}
STG_DB_PORT=${STG_DB_PORT:-3306}
STG_ENV=${STG_ENV:-/etc/htbb/staging.env}              # STG_DB_PASS=... (root 0600, cai-dat.sh sinh)
STG_USER=${STG_USER:-htbb}                             # user he thong chay staging (cung user voi prod, khac thu muc)
STG_UNIT_GAME=${STG_UNIT_GAME:-htbb-staging-game}
STG_UNIT_LOGIN=${STG_UNIT_LOGIN:-htbb-staging-login}
BACKUP_DIR=${BACKUP_DIR:-$HTBB_GOC/backups}            # vps/backup_db.sh (= /usr/local/sbin/htbb-backup-db.sh)
CONG_KIEM_DIR=${CONG_KIEM_DIR:-/var/lib/htbb/cong-kiem}  # ket qua cong kiem (root 0700)
CONG_KIEM_KHOA=${CONG_KIEM_KHOA:-/etc/htbb/cong-kiem.key} # khoa HMAC ky ket qua (root 0600)
# Tien to tai khoan bot (GM <tt>001..040, thuong <tt>101..199, mat khau 123456 - CHI ton tai trong CSDL staging).
STG_BOT_TIEN_TO=${STG_BOT_TIEN_TO:-kt}
SYSTEMCTL=${SYSTEMCTL:-systemctl}

# Lenh mysql: mac dinh root qua unix_socket (Ubuntu). May dev: MYSQL="mysql -S /duong/sock -uroot".
if [ -n "${MYSQL:-}" ]; then
    read -r -a MY <<< "$MYSQL"
else
    MY=(mysql)
fi
my() { "${MY[@]}" --default-character-set=utf8mb4 "$@"; }
myq() { my -N -B -e "$1"; }

loi() { echo "[LOI] $*" >&2; exit 1; }
buoc() { echo; echo "== $(date '+%T') $*"; }

# Doc MOT khoa tu tep env (khong source - gia tri co the chua ky tu shell).
doc_env() { [ -r "$2" ] && sed -n -E "s/^$1=(.*)$/\1/p" "$2" | tail -n 1 | sed -E "s/^[\"']//; s/[\"']\$//"; }

stg_mat_khau() {
    local mk
    mk=${STG_DB_PASS:-$(doc_env STG_DB_PASS "$STG_ENV" || true)}
    [ -n "$mk" ] || loi "chua co STG_DB_PASS trong $STG_ENV - chay vps/staging/cai-dat.sh truoc"
    printf '%s' "$mk"
}

# Tep cnf 0600 (mysql --defaults-file) cho user staging - bot va phep thu ket noi dung, mat khau khong len dong lenh.
stg_tao_cnf() {
    local f=$1
    ( umask 077; printf '[client]\nhost=%s\nport=%s\nuser=%s\npassword=%s\n' \
        "$STG_DB_HOST" "$STG_DB_PORT" "$STG_DB_USER" "$(stg_mat_khau)" > "$f" )
}

# sha256 cua tep (chi phan hex).
sha() { sha256sum "$1" | cut -d' ' -f1; }

# Cong TCP dang LISTEN?
dang_nghe() { ss -ltn 2>/dev/null | awk '{print $4}' | grep -qE "[:.]$1\$"; }

# Doi toi khi cong $1 LISTEN, toi da $2 giay.
doi_cong() {
    local i
    for i in $(seq 1 "$2"); do
        dang_nghe "$1" && return 0
        sleep 1
    done
    return 1
}

# Viet lai sql/htbb.sql (ten CSDL ghi cung `htbb_db` / `account`) sang ten staging. Chi doi ten TRONG DAU `...`:
# tep khong co cot / bang nao ten `account` hay `htbb_db` (kiem: grep -o '`account`[^.]' chi ra CREATE DATABASE / USE).
viet_lai_sql_staging() {
    local vao=$1 ra=$2
    sed -e "s/\`htbb_db\`/\`$STG_DB\`/g" -e "s/\`account\`/\`$STG_DB_TK\`/g" "$vao" > "$ra"
    # Chot an toan: ban viet lai KHONG duoc con nhac toi CSDL that.
    if grep -q '`htbb_db`\|`account`' "$ra"; then
        loi "viet lai SQL staging con sot \`htbb_db\` / \`account\` - DUNG (khong nap gi)"
    fi
    grep -q "^USE \`$STG_DB\`;" "$ra" || loi "SQL staging thieu 'USE \`$STG_DB\`;' - tep nguon la. DUNG"
}

# Ky ket qua cong kiem: chu ky = sha256(khoa || sha256(khoa || noi dung)) (dang HMAC long nhau), khoa root 0600.
# Doc noi dung tu stdin, in chu ky hex. Khoa KHONG bao gio len dong lenh (ps thay duoc) - di qua pipe.
ky_hmac() {
    [ -r "$CONG_KIEM_KHOA" ] || { echo "khong doc duoc khoa $CONG_KIEM_KHOA" >&2; return 1; }
    local trong
    trong=$( { cat "$CONG_KIEM_KHOA"; cat; } | sha256sum | cut -d' ' -f1 )
    { cat "$CONG_KIEM_KHOA"; printf '%s' "$trong"; } | sha256sum | cut -d' ' -f1
}
