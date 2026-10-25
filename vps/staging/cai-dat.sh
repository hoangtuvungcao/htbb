#!/bin/bash
# ============================================================================
#  CAI STAGING + CONG KIEM tren VPS - MOT LAN, chay lai duoc (idempotent).
#
#      sudo bash /opt/htbb/vps/staging/cai-dat.sh
#
#  Lam:
#    1. thu muc /opt/htbb-staging/{server,sql,logs} (htbb:htbb), /var/lib/htbb/cong-kiem (root 0700)
#    2. /etc/htbb/staging.env (root 0600): STG_DB_PASS ngau nhien - chi tao khi chua co
#       /etc/htbb/cong-kiem.key (root 0600): khoa ky ket qua cong kiem - chi tao khi chua co
#    3. user MariaDB htbb_staging@localhost/127.0.0.1, CHI quyen tren htbb_staging + htbb_staging_account
#       (khong cham htbb_db / account cua production)
#    4. unit htbb-staging-game / htbb-staging-login -> /etc/systemd/system, daemon-reload, KHONG enable
#    5. ufw (neu dang bat): deny 29129/tcp va 28023/tcp (game staging con nghe 127.0.0.1, unit chi localhost)
#    6. kiem java / javac (bot hoi quy can JDK: apt install openjdk-17-jdk-headless)
#
#  Khong khoi dong staging, khong nap CSDL: buoc sau la  sudo bash /opt/htbb/vps/staging/lam-moi.sh
#  --thu: chi in viec se lam.
# ============================================================================
set -euo pipefail
DIR_SCRIPT=$(cd "$(dirname "$0")" && pwd)
# shellcheck source=vps/staging/chung.sh
. "$DIR_SCRIPT/chung.sh"

THU=0
[ "${1:-}" = "--thu" ] && THU=1
lam() { if [ "$THU" = 1 ]; then echo "   (thu) $*"; else "$@"; fi; }

[ "$THU" = 1 ] || [ "${HTBB_CHO_KHONG_ROOT:-0}" = 1 ] || [ "$(id -u)" = 0 ] || loi "chay bang root (sudo)"
GOC_USER=${STG_ROOT_USER:-root}   # chu so huu tep bi mat (may thu khong co root: dat user minh)
id "$STG_USER" >/dev/null 2>&1 || loi "khong co user he thong $STG_USER (production chua dung?)"

buoc "1/6 Thu muc"
lam install -d -o "$STG_USER" -g "$STG_USER" -m 750 "$STG_GOC" "$STG_GOC/server" "$STG_GOC/server/loginServer" "$STG_GOC/logs"
lam install -d -o "$GOC_USER" -g "$GOC_USER" -m 750 "$STG_GOC/sql"
lam install -d -o "$GOC_USER" -g "$GOC_USER" -m 700 "$CONG_KIEM_DIR"
lam install -d -o "$GOC_USER" -g "$GOC_USER" -m 700 "$(dirname "$STG_ENV")"

buoc "2/6 Bi mat staging (khong bao gio in ra)"
if [ -s "$STG_ENV" ] && grep -q '^STG_DB_PASS=.\+' "$STG_ENV"; then
    echo "   $STG_ENV da co - giu nguyen"
else
    if [ "$THU" = 1 ]; then echo "   (thu) tao $STG_ENV"; else
        ( umask 077; printf 'STG_DB_PASS=%s\n' "$(openssl rand -hex 24)" > "$STG_ENV" )
        chown "$GOC_USER:$GOC_USER" "$STG_ENV"; chmod 600 "$STG_ENV"
        echo "   da tao $STG_ENV"
    fi
fi
if [ -s "$CONG_KIEM_KHOA" ]; then
    echo "   $CONG_KIEM_KHOA da co - giu nguyen (doi khoa = moi ket qua cong kiem cu mat hieu luc)"
else
    if [ "$THU" = 1 ]; then echo "   (thu) tao $CONG_KIEM_KHOA"; else
        ( umask 077; openssl rand -hex 32 > "$CONG_KIEM_KHOA" )
        chown "$GOC_USER:$GOC_USER" "$CONG_KIEM_KHOA"; chmod 600 "$CONG_KIEM_KHOA"
        echo "   da tao $CONG_KIEM_KHOA"
    fi
fi

buoc "3/6 User MariaDB $STG_DB_USER (chi $STG_DB + $STG_DB_TK)"
PB=$(myq "SELECT VERSION()") || loi "khong vao duoc MariaDB bang root (unix_socket)"
case "$PB" in *MariaDB*) echo "   $PB" ;; *) loi "can MariaDB, may nay: $PB" ;; esac
if [ "$THU" = 1 ]; then
    echo "   (thu) CREATE USER IF NOT EXISTS + ALTER USER (MAX_USER_CONNECTIONS $STG_DB_MAX_USER_CONN) + GRANT ALL ON $STG_DB.*, $STG_DB_TK.*"
else
    MK=$(stg_mat_khau)
    # Mat khau qua stdin cua mysql (khong len dong lenh). Ten CSDL trong GRANT phai thoat dau _ (wildcard).
    my <<SQL
CREATE DATABASE IF NOT EXISTS \`$STG_DB\` CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE IF NOT EXISTS \`$STG_DB_TK\` CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE USER IF NOT EXISTS '$STG_DB_USER'@'localhost' IDENTIFIED BY '$MK';
CREATE USER IF NOT EXISTS '$STG_DB_USER'@'127.0.0.1' IDENTIFIED BY '$MK';
ALTER USER '$STG_DB_USER'@'localhost' IDENTIFIED BY '$MK';
ALTER USER '$STG_DB_USER'@'127.0.0.1' IDENTIFIED BY '$MK';
ALTER USER '$STG_DB_USER'@'localhost' WITH MAX_USER_CONNECTIONS $STG_DB_MAX_USER_CONN;
ALTER USER '$STG_DB_USER'@'127.0.0.1' WITH MAX_USER_CONNECTIONS $STG_DB_MAX_USER_CONN;
GRANT ALL PRIVILEGES ON \`${STG_DB//_/\\_}\`.* TO '$STG_DB_USER'@'localhost', '$STG_DB_USER'@'127.0.0.1';
GRANT ALL PRIVILEGES ON \`${STG_DB_TK//_/\\_}\`.* TO '$STG_DB_USER'@'localhost', '$STG_DB_USER'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
    # Chot: user staging KHONG duoc thay CSDL production.
    CNF=$(mktemp); trap 'rm -f "$CNF"' EXIT
    stg_tao_cnf "$CNF"
    THAY=$(mysql --defaults-file="$CNF" -N -B -e "SHOW DATABASES" 2>/dev/null | tr '\n' ' ')
    case " $THAY " in
        *" htbb_db "*|*" account "*) loi "user $STG_DB_USER THAY CSDL production ($THAY) - kiem lai GRANT" ;;
    esac
    case " $THAY " in *" $STG_DB "*) echo "   ok: $STG_DB_USER thay: $THAY" ;; *) loi "user $STG_DB_USER khong vao duoc $STG_DB" ;; esac
fi

buoc "4/6 Unit systemd (khong enable)"
for U in "$STG_UNIT_GAME" "$STG_UNIT_LOGIN"; do
    lam install -m 644 "$DIR_SCRIPT/$U.service" "${STG_UNIT_DIR:-/etc/systemd/system}/$U.service"
done
lam "$SYSTEMCTL" daemon-reload
echo "   bat/tat: sudo systemctl start|stop $STG_UNIT_LOGIN $STG_UNIT_GAME (cong kiem tu lam)"

buoc "5/6 Tuong lua"
if command -v ufw >/dev/null 2>&1 && ufw status 2>/dev/null | grep -q "Status: active"; then
    for C in "$STG_CONG_GAME" "$STG_CONG_LOGIN"; do
        if ufw status | grep -qE "^$C/tcp +DENY"; then echo "   $C/tcp da DENY"; else lam ufw deny "$C/tcp" comment 'htbb staging - chi localhost'; fi
    done
else
    echo "   ufw khong bat - staging van chi nghe localhost (sv.ipLangNghe + IPAddressDeny cua unit)"
fi

buoc "6/6 Java"
command -v java >/dev/null 2>&1 && java -version 2>&1 | head -1 | sed 's/^/   java: /' || echo "   [CANH BAO] khong co java"
if command -v javac >/dev/null 2>&1; then
    echo "   javac: co (bot hoi quy tu bien dich)"
else
    echo "   [CANH BAO] khong co javac -> bot hoi quy khong bien dich duoc tren VPS."
    echo "             Cai: sudo apt install -y openjdk-17-jdk-headless   (hoac dat HTBB_BOT_CP=<thu muc .class da build>)"
fi

echo
echo "== XONG. Tiep: sudo bash $DIR_SCRIPT/lam-moi.sh --jar $HTBB_GOC/server/htbb-server.jar --sql $HTBB_GOC/sql/htbb.sql"
