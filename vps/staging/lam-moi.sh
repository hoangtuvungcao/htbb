#!/bin/bash
# ============================================================================
#  LAM MOI STAGING: CSDL tu ban sao luu production moi nhat + nang cap SQL ung vien + jar ung vien. Chay lai duoc.
#
#      sudo bash /opt/htbb/vps/staging/lam-moi.sh --jar <htbb-server.jar> [--sql <htbb.sql>] [tuy chon]
#
#  Tuy chon:
#    --jar F            jar ung vien (bat buoc, tru khi --giu-jar)
#    --sql F            sql/htbb.sql ung vien (mac dinh /opt/htbb/sql/htbb.sql)
#    --nguon D          thu muc monorepo lay tai nguyen server (map/ cMap/ lib/ loginServer/CheckLoginSocket.jar);
#                       mac dinh /opt/htbb. Ban ung vien doi tai nguyen thi tro toi ban checkout cua no.
#    --ban-sao F        ban sao luu htbb_db_*.sql.gz cu the (account_* cung moc tu tim); mac dinh MOI NHAT
#    --tu-csdl-song     khong dung ban sao luu: mysqldump --single-transaction thang tu htbb_db / account (ton IO hon)
#    --giu-csdl         KHONG nap lai CSDL (chi nang cap SQL + thay jar) - nhanh, khi chay lai cong kiem cung ngay
#    --giu-jar          giu jar dang o staging
#    --khong-bat        nap xong khong bat unit
#    --giu-du-lieu-ca-nhan  bo buoc xoa email / so dien thoai / IP / mat khau that trong CSDL staging
#
#  Ban sao luu: vps/backup_db.sh (cai o /usr/local/sbin/htbb-backup-db.sh, cron 03:30) ghi
#    /opt/htbb/backups/htbb_db_<YYYY-MM-DD_HHMM>.sql.gz + account_<...>.sql.gz  (mysqldump MOT CSDL, khong --databases
#    -> khong co USE / CREATE DATABASE, nap vao CSDL ten khac duoc). Script tu choi dump co USE / CREATE DATABASE.
#
#  Khong bao gio ghi vao htbb_db / account: moi lenh ghi deu tro ten staging, SQL ung vien duoc viet lai ten
#  (chung.sh viet_lai_sql_staging) va nang_cap_csdl.sh tu choi tep khong khop CSDL.
# ============================================================================
set -euo pipefail
DIR_SCRIPT=$(cd "$(dirname "$0")" && pwd)
REPO_SCRIPT=$(cd "$DIR_SCRIPT/../.." && pwd)
# shellcheck source=vps/staging/chung.sh
. "$DIR_SCRIPT/chung.sh"

JAR=""; SQL="$HTBB_GOC/sql/htbb.sql"; NGUON="$HTBB_GOC"; BAN_SAO=""; SONG=0; GIU_CSDL=0; GIU_JAR=0; BAT=1; GIU_CN=0
while [ $# -gt 0 ]; do
    case "$1" in
        --jar) JAR=$2; shift 2 ;;
        --sql) SQL=$2; shift 2 ;;
        --nguon) NGUON=$2; shift 2 ;;
        --ban-sao) BAN_SAO=$2; shift 2 ;;
        --tu-csdl-song) SONG=1; shift ;;
        --giu-csdl) GIU_CSDL=1; shift ;;
        --giu-jar) GIU_JAR=1; shift ;;
        --khong-bat) BAT=0; shift ;;
        --giu-du-lieu-ca-nhan) GIU_CN=1; shift ;;
        -h|--help) sed -n '2,30p' "$0"; exit 0 ;;
        *) loi "tuy chon la: $1 (xem --help)" ;;
    esac
done

[ "${HTBB_CHO_KHONG_ROOT:-0}" = 1 ] || [ "$(id -u)" = 0 ] || loi "chay bang root (sudo)"
[ "$GIU_JAR" = 1 ] || [ -n "$JAR" ] || loi "thieu --jar <tep> (hoac --giu-jar)"
[ "$GIU_JAR" = 1 ] || [ -r "$JAR" ] || loi "khong doc duoc jar $JAR"
[ -r "$SQL" ] || loi "khong doc duoc sql $SQL"
[ -d "$STG_GOC/server" ] || loi "chua co $STG_GOC/server - chay vps/staging/cai-dat.sh truoc"
stg_mat_khau >/dev/null
BAT_DAU=$(date +%s)

kiem_jar() {
    if command -v unzip >/dev/null 2>&1; then unzip -tq "$1" >/dev/null 2>&1
    else python3 -c 'import sys,zipfile; sys.exit(zipfile.ZipFile(sys.argv[1]).testzip() is not None)' "$1"; fi
}
if [ "$GIU_JAR" = 0 ]; then
    kiem_jar "$JAR" || loi "$JAR hong (khong phai zip/jar hop le)"
fi
grep -q '^-- ######## B. NANG CAP' "$SQL" || loi "$SQL thieu phan B - khong phai sql/htbb.sql"

buoc "1/8 Tat staging"
"$SYSTEMCTL" stop "$STG_UNIT_GAME" 2>/dev/null || true
"$SYSTEMCTL" stop "$STG_UNIT_LOGIN" 2>/dev/null || true
# Unit Restart=no nhung van doi cong that su dong (tat luu nhan vat xong).
for _ in $(seq 1 60); do dang_nghe "$STG_CONG_GAME" || break; sleep 1; done
! dang_nghe "$STG_CONG_GAME" || loi "cong $STG_CONG_GAME van mo sau khi tat staging"

buoc "2/8 Tai nguyen server tu $NGUON"
[ -d "$NGUON/server/map" ] && [ -d "$NGUON/server/cMap" ] && [ -d "$NGUON/server/lib" ] \
    || loi "$NGUON/server thieu map/ cMap/ lib/"
for D in map cMap lib; do
    rsync -a --delete "$NGUON/server/$D/" "$STG_GOC/server/$D/"
done
install -d -o "$STG_USER" -g "$STG_USER" -m 750 "$STG_GOC/server/loginServer"
install -o "$STG_USER" -g "$STG_USER" -m 644 "$NGUON/server/loginServer/CheckLoginSocket.jar" "$STG_GOC/server/loginServer/"
chown -R "$STG_USER:$STG_USER" "$STG_GOC/server/map" "$STG_GOC/server/cMap" "$STG_GOC/server/lib"
# Tep trang thai cua lan chay truoc (bao tri / nhip / log .txt) - staging moi bat dau sach.
rm -f "$STG_GOC/server/bao_tri.txt" "$STG_GOC/server/trang_thai.json"
echo "   ok (map, cMap, lib, CheckLoginSocket.jar)"

buoc "3/8 CSDL staging"
nap_dump() { # $1 = tep .sql.gz, $2 = CSDL dich
    local f=$1 db=$2
    gzip -t "$f" || loi "$f hong (gzip -t)"
    # Dump co USE / CREATE DATABASE se nap vao CSDL THAT - tu choi.
    if zcat "$f" | grep -qE '^(USE |CREATE DATABASE)'; then
        loi "$f co USE / CREATE DATABASE (dump --databases?) - khong nap duoc vao $db an toan"
    fi
    myq "DROP DATABASE IF EXISTS \`$db\`; CREATE DATABASE \`$db\` CHARACTER SET utf8 COLLATE utf8_general_ci"
    zcat "$f" | nice -n 10 ionice -c3 "${MY[@]}" --default-character-set=utf8mb4 "$db" || loi "nap $f vao $db that bai"
    echo "   $(basename "$f") -> $db ($(myq "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db'") bang)"
}
if [ "$GIU_CSDL" = 1 ]; then
    myq "SELECT 1 FROM information_schema.tables WHERE table_schema='$STG_DB' AND table_name='tob_char'" | grep -q 1 \
        || loi "--giu-csdl nhung $STG_DB chua co du lieu - bo --giu-csdl"
    echo "   giu nguyen $STG_DB / $STG_DB_TK"
elif [ "$SONG" = 1 ]; then
    for CAP in "htbb_db:$STG_DB" "account:$STG_DB_TK"; do
        TU=${CAP%%:*}; DEN=${CAP#*:}
        myq "DROP DATABASE IF EXISTS \`$DEN\`; CREATE DATABASE \`$DEN\` CHARACTER SET utf8 COLLATE utf8_general_ci"
        # mysqldump MOT CSDL (khong --databases) -> khong co USE, nap vao ten khac.
        "${MY[0]/mysql/mysqldump}" "${MY[@]:1}" --single-transaction --routines --triggers "$TU" \
            | nice -n 10 ionice -c3 "${MY[@]}" --default-character-set=utf8mb4 "$DEN" || loi "chep $TU -> $DEN that bai"
        echo "   $TU (dang chay) -> $DEN"
    done
else
    if [ -n "$BAN_SAO" ]; then F_GAME=$BAN_SAO; else
        F_GAME=$(ls -1t "$BACKUP_DIR"/htbb_db_*.sql.gz 2>/dev/null | head -1 || true)
    fi
    [ -n "$F_GAME" ] && [ -r "$F_GAME" ] || loi "khong thay ban sao luu htbb_db_*.sql.gz trong $BACKUP_DIR (chay /usr/local/sbin/htbb-backup-db.sh, hoac --tu-csdl-song)"
    MOC=$(basename "$F_GAME" .sql.gz); MOC=${MOC#htbb_db_}
    F_TK="$(dirname "$F_GAME")/account_$MOC.sql.gz"
    [ -r "$F_TK" ] || F_TK=$(ls -1t "$(dirname "$F_GAME")"/account_*.sql.gz 2>/dev/null | head -1 || true)
    [ -n "$F_TK" ] && [ -r "$F_TK" ] || loi "khong thay ban sao luu account_*.sql.gz"
    TUOI=$(( ( $(date +%s) - $(stat -c %Y "$F_GAME") ) / 3600 ))
    [ "$TUOI" -le 48 ] || echo "   [CANH BAO] ban sao luu da $TUOI gio tuoi"
    nap_dump "$F_GAME" "$STG_DB"
    nap_dump "$F_TK" "$STG_DB_TK"
fi

buoc "4/8 Bot du lieu ca nhan"
if [ "$GIU_CN" = 1 ] || [ "$GIU_CSDL" = 1 ]; then
    echo "   bo qua"
else
    # Mat khau that -> chuoi ngau nhien dung dinh dang PASSWORD() (khong ai dang nhap staging bang tai khoan that).
    myq "UPDATE \`$STG_DB_TK\`.team_user SET password = CONCAT('*', UPPER(SHA1(CONCAT(id, '-', RAND())))), email = NULL, phone = ''"
    myq "TRUNCATE TABLE \`$STG_DB\`.web_admin_2fa" 2>/dev/null || true
    # Moi cot ip / email / phone kieu chuoi trong hai CSDL staging -> rong.
    while IFS=$'\t' read -r S B C N; do
        [ -n "$C" ] || continue
        GT="''"; [ "$N" = YES ] && GT=NULL
        myq "UPDATE \`$S\`.\`$B\` SET \`$C\` = $GT WHERE \`$C\` IS NOT NULL AND \`$C\` <> ''" 2>/dev/null \
            || echo "   [CANH BAO] khong xoa duoc $S.$B.$C"
    done < <(myq "SELECT table_schema, table_name, column_name, is_nullable FROM information_schema.columns
                  WHERE table_schema IN ('$STG_DB','$STG_DB_TK') AND column_name IN ('ip','ip_address','email','phone','sdt')
                    AND data_type IN ('varchar','char','text','tinytext') AND NOT (table_name='team_user' AND column_name IN ('email','phone'))")
    echo "   team_user: mat khau ngau nhien, email/phone rong; cot ip/email/phone rong; web_admin_2fa rong"
fi

buoc "5/8 Nang cap CSDL staging bang SQL ung vien"
SQL_STG="$STG_GOC/sql/htbb_staging.sql"
install -m 640 "$SQL" "$STG_GOC/sql/htbb.sql"           # ban goc: may chu doc du lieu thiet ke tu ../sql/htbb.sql
viet_lai_sql_staging "$SQL" "$SQL_STG"
HTBB_SQL="$SQL_STG" HTBB_CSDL_GAME="$STG_DB" BO_SAO_LUU=1 MYSQL="${MY[*]}" bash "$REPO_SCRIPT/vps/nang_cap_csdl.sh"

buoc "6/8 Tai khoan bot + cau hinh staging"
{
    echo "SET @mk := PASSWORD('123456');"
    for i in $(seq 1 40) $(seq 101 199); do
        T=$(printf '%s%03d' "$STG_BOT_TIEN_TO" "$i")
        echo "INSERT INTO \`$STG_DB_TK\`.team_user (username,password,phone,regdate,ban,provider,fromgame) VALUES ('$T',@mk,'',DATE_SUB(NOW(),INTERVAL 30 DAY),0,0,0) ON DUPLICATE KEY UPDATE password=@mk, ban=0, regdate=LEAST(IFNULL(regdate,NOW()),DATE_SUB(NOW(),INTERVAL 30 DAY));"
    done
    echo "INSERT INTO \`$STG_DB\`.\`5h_active\` (userID,username,time_end) SELECT u.id,u.username,'1894945812' FROM \`$STG_DB_TK\`.team_user u WHERE u.username REGEXP '^${STG_BOT_TIEN_TO}[0-9]{3}\$' AND NOT EXISTS (SELECT 1 FROM \`$STG_DB\`.\`5h_active\` a WHERE a.username=u.username);"
    GM="admin"; for i in $(seq 1 40); do GM="$GM,$(printf '%s%03d' "$STG_BOT_TIEN_TO" "$i")"; done
    # Khoa trong bang cau_hinh THANG server.ini - ghi de cac khoa staging can.
    for KV in "sv.adminAccounts=$GM" "sv.choPhepTaoTenGM=1" "sv.gioBaoTri=tat" "bao_tri.bat=0" "sv.banToiThieu=0" "sv.dangKyCsdlTaiKhoan=$STG_DB_TK"; do
        echo "INSERT INTO \`$STG_DB\`.cau_hinh (khoa,gia_tri,sua_boi,sua_luc) VALUES ('${KV%%=*}','${KV#*=}','staging',NOW()) ON DUPLICATE KEY UPDATE gia_tri=VALUES(gia_tri), sua_boi='staging', sua_luc=NOW();"
    done
    # Co lenh production luc sao luu (bao tri / restart dang bat) khong duoc lam staging tu tat.
    echo "UPDATE \`$STG_DB\`.\`5h_systems\` SET status = 1 WHERE command IN ('baotri_thongbao','restart');"
} | my
echo "   GM ${STG_BOT_TIEN_TO}001..040, thuong ${STG_BOT_TIEN_TO}101..199 (mat khau 123456, chi trong $STG_DB_TK)"

buoc "7/8 Jar + server.ini"
if [ "$GIU_JAR" = 0 ]; then
    install -o "$STG_USER" -g "$STG_USER" -m 644 "$JAR" "$STG_GOC/server/htbb-server.jar"
fi
[ -r "$STG_GOC/server/htbb-server.jar" ] || loi "staging chua co jar"
bash "$DIR_SCRIPT/sinh-server-ini.sh"
{
    echo "jar_sha256=$(sha "$STG_GOC/server/htbb-server.jar")"
    echo "sql_sha256=$(sha "$SQL")"
    echo "luc=$(date '+%F %T')"
    echo "nguon=$NGUON"
} > "$STG_GOC/ung_vien.txt"
sed 's/^/   /' "$STG_GOC/ung_vien.txt"

buoc "8/8 Bat staging"
if [ "$BAT" = 0 ]; then
    echo "   --khong-bat: bat bang  sudo systemctl start $STG_UNIT_LOGIN $STG_UNIT_GAME"
else
    LOG="$STG_GOC/logs/game.out"
    MOC_LOG=$(stat -c %s "$LOG" 2>/dev/null || echo 0)
    "$SYSTEMCTL" start "$STG_UNIT_LOGIN"
    doi_cong "$STG_CONG_LOGIN" 30 || loi "login staging khong nghe cong $STG_CONG_LOGIN (xem $STG_GOC/logs/login.out)"
    "$SYSTEMCTL" start "$STG_UNIT_GAME"
    if ! doi_cong "$STG_CONG_GAME" "${STG_CHO_BAT:-240}"; then
        tail -c +"$((MOC_LOG + 1))" "$LOG" 2>/dev/null | tail -n 40
        loi "game staging khong nghe cong $STG_CONG_GAME sau ${STG_CHO_BAT:-240}s (log o tren)"
    fi
    echo "   game staging nghe $STG_CONG_GAME, login $STG_CONG_LOGIN"
fi
echo
echo "== XONG lam moi staging ($(( $(date +%s) - BAT_DAU ))s)"
