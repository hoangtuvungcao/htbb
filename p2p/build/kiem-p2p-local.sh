#!/bin/bash
# ============================================================================
#  KIEM P2P TREN MAY DEV: stack rieng (MariaDB tam + login + game co Peerova nhung) tu CAY LAM VIEC hien tai,
#  roi bot nguoi choi thuong (khong GM) vao game QUA PEEROVA (p2p/test/KiemDangNhapP2P.java: 3 nguoi choi dang nhap,
#  vao game, chat; tunnel moi + dang nhap lai; ticket hong), lop Connector that cua HTBB-PC.jar (KiemConnectorP2P,
#  native giai nen tu jar) va ma Unity PC (P2PPeerova.cs + C# binding) chay tren .NET 8 (p2p/test/unity-net).
#
#      bash p2p/build/kiem-p2p-local.sh [--giu]
#
#  Giong htbb_cong_cu/kiem_truoc.sh buoc 1-3 (CSDL tu sql/htbb.sql viet lai ten CSDL staging, server.ini bang
#  vps/staging/sinh-server-ini.sh, game nghe 127.0.0.1) + them sv.p2p=1. Khong dung toi CSDL / cong production.
#  Can: /opt/lampp (MariaDB), JDK, p2p/out/dist/linux-x86_64/libpeerova_jni.so (p2p/build/build-peerova.sh linux).
#  Ket qua: p2p/out/kiem/<thoi diem>/ (logs/game.out, bot.txt). Ma thoat 0 = DAT.
# ============================================================================
set -uo pipefail
R=$(cd "$(dirname "$0")/../.." && pwd)
LAMPP=${LAMPP:-/opt/lampp}
GIU=0; [ "${1:-}" = --giu ] && GIU=1
RUN="$R/p2p/out/kiem/$(date '+%F_%H%M%S')"
mkdir -p "$RUN"
exec > >(tee -a "$RUN/kiem.log") 2>&1
buoc() { echo; echo "== $(date '+%T') $*"; }
DA_LAY=" "
cong_trong() { local c=$1; while ss -ltun | awk '{print $5}' | grep -qE "[:.]$c\$" || [[ "$DA_LAY" == *" $c "* ]]; do c=$((c + 1)); done; DA_LAY="$DA_LAY$c "; CONG=$c; }
cong_trong 3740; CONG_DB=$CONG
cong_trong 18740; CONG_GAME=$CONG
cong_trong $((CONG_GAME + 1)); CONG_LOGIN=$CONG
cong_trong $((CONG_LOGIN + 1)); CONG_NAP=$CONG
cong_trong 47740; CONG_P2P=$CONG
SOCK="$RUN/db/mysql.sock"
STG="$RUN/stg"
echo "   cong: MariaDB $CONG_DB, game $CONG_GAME (127.0.0.1), login $CONG_LOGIN, Peerova UDP/TCP $CONG_P2P"
PID_DB=""; PID_LOGIN=""; PID_GAME=""
dung() { [ -n "$1" ] && kill "$1" 2>/dev/null && for _ in $(seq 1 "$2"); do kill -0 "$1" 2>/dev/null || return 0; sleep 1; done; [ -n "$1" ] && kill -9 "$1" 2>/dev/null; return 0; }
don() {
    if [ "$GIU" = 1 ]; then echo "   --giu: stack van chay (game $CONG_GAME, ticket $STG/server/p2p/ticket.txt)"; return; fi
    dung "$PID_GAME" 20; dung "$PID_LOGIN" 10; dung "$PID_DB" 60; rm -rf "$RUN/db"; echo "   da tat stack (log giu o $RUN)"
}
trap don EXIT
loi() { echo "[LOI] $*"; exit 2; }
JNI="$R/p2p/out/dist/linux-x86_64/libpeerova_jni.so"
[ -f "$JNI" ] || sh "$R/p2p/build/build-peerova.sh" linux || loi "build peerova"
sh "$R/p2p/build/sync-java.sh" --check || loi "ban chep Java p2p lech nguon (sh p2p/build/sync-java.sh)"

buoc "1/5 build game server tu cay lam viec (javac --release 8)"
mkdir -p "$STG/server/loginServer" "$STG/server/p2p" "$STG/logs" "$STG/sql"
sh "$R/p2p/build/build-server.sh" "$STG/server/htbb-server.jar" || loi "build server"
cp "$R/server/loginServer/htbb-login.jar" "$STG/server/loginServer/"
for d in cMap map lib; do ln -sfn "$R/server/$d" "$STG/server/$d"; done
cp "$JNI" "$STG/server/p2p/"

buoc "2/5 MariaDB tam + sql/htbb.sql + tai khoan bot"
export HTBB_GOC="$R" STG_GOC="$STG" STG_USER="$(id -un)" STG_DB_HOST=127.0.0.1 STG_DB_PORT="$CONG_DB" \
       STG_CONG_GAME="$CONG_GAME" STG_CONG_LOGIN="$CONG_LOGIN" STG_CONG_NAP="$CONG_NAP" STG_DB_PASS="kp2p_$$" \
       STG_ENV=/dev/null MYSQL="$LAMPP/bin/mysql -S $SOCK -uroot" PATH="$LAMPP/bin:$PATH"
# shellcheck source=/dev/null
. "$R/vps/staging/chung.sh"
loi() { echo "[LOI] $*"; exit 2; }
mkdir -p "$RUN/db"
"$LAMPP/bin/mysql_install_db" --no-defaults --basedir="$LAMPP" --datadir="$RUN/db/data" --auth-root-authentication-method=normal \
    > "$RUN/db/install.log" 2>&1 || { tail -5 "$RUN/db/install.log"; loi "mysql_install_db"; }
"$LAMPP/sbin/mysqld" --no-defaults --basedir="$LAMPP" --datadir="$RUN/db/data" --port="$CONG_DB" --bind-address=127.0.0.1 \
    --socket="$SOCK" --pid-file="$RUN/db/pid" --log-error="$RUN/db/err.log" --skip-name-resolve --max-connections=300 \
    --innodb-buffer-pool-size=256M --character-set-server=utf8mb4 --sql-mode= > /dev/null 2>&1 &
PID_DB=$!
for _ in $(seq 1 60); do [ -S "$SOCK" ] && break; sleep 1; done
[ -S "$SOCK" ] || loi "MariaDB khong len"
myq "CREATE USER IF NOT EXISTS '$STG_DB_USER'@'127.0.0.1' IDENTIFIED BY '$STG_DB_PASS';
     CREATE USER IF NOT EXISTS '$STG_DB_USER'@'localhost' IDENTIFIED BY '$STG_DB_PASS';" || loi "tao user"
viet_lai_sql_staging "$R/sql/htbb.sql" "$STG/sql/htbb.sql"
my < "$STG/sql/htbb.sql" > "$RUN/db/nap.log" 2>&1 || { tail -5 "$RUN/db/nap.log"; loi "nap sql"; }
myq "GRANT ALL ON \`$STG_DB\`.* TO '$STG_DB_USER'@'127.0.0.1', '$STG_DB_USER'@'localhost';
     GRANT ALL ON \`$STG_DB_TK\`.* TO '$STG_DB_USER'@'127.0.0.1', '$STG_DB_USER'@'localhost';" || loi "grant"
{
    echo "SET @mk := PASSWORD('123456');"
    for i in $(seq 101 110); do
        echo "INSERT INTO \`$STG_DB_TK\`.team_user (username,password,phone,regdate,ban,provider,fromgame) VALUES ('kt$i',@mk,'',DATE_SUB(NOW(),INTERVAL 30 DAY),0,0,0) ON DUPLICATE KEY UPDATE password=@mk, ban=0;"
    done
    echo "INSERT INTO \`$STG_DB\`.\`5h_active\` (userID,username,time_end) SELECT u.id,u.username,'1894945812' FROM \`$STG_DB_TK\`.team_user u WHERE u.username REGEXP '^kt1[0-9]{2}\$' AND NOT EXISTS (SELECT 1 FROM \`$STG_DB\`.\`5h_active\` a WHERE a.username=u.username);"
    for KV in "sv.gioBaoTri=tat" "bao_tri.bat=0" "sv.banToiThieu=0" "sv.dangKyCsdlTaiKhoan=$STG_DB_TK"; do
        echo "INSERT INTO \`$STG_DB\`.cau_hinh (khoa,gia_tri,sua_boi,sua_luc) VALUES ('${KV%%=*}','${KV#*=}','kiem-p2p',NOW()) ON DUPLICATE KEY UPDATE gia_tri=VALUES(gia_tri);"
    done
} | my || loi "tai khoan bot"

buoc "3/5 server.ini (sinh-server-ini.sh + sv.p2p=1) + bat login / game"
cp "$R/vps/server.ini.linux.example" "$RUN/nguon_server.ini"
NGUON_INI="$RUN/nguon_server.ini" bash "$R/vps/staging/sinh-server-ini.sh" > "$RUN/sinh-ini.log" 2>&1 || { cat "$RUN/sinh-ini.log"; loi "sinh-server-ini.sh"; }
cat >> "$STG/server/server.ini" <<EOF2

# ==== kiem-p2p-local.sh: Peerova nhung (nhanh p2p) ====
sv.p2p=1
sv.p2p_port=$CONG_P2P
sv.p2p_upnp=0
sv.p2p_key=p2p/server.key
sv.p2p_ticket_file=p2p/ticket.txt
EOF2
( cd "$STG/server/loginServer" && exec java -Xmx96m -Djava.awt.headless=true -Dfile.encoding=UTF-8 -jar htbb-login.jar >> "$STG/logs/login.out" 2>&1 ) &
PID_LOGIN=$!
( cd "$STG/server" && exec java -Xms256m -Xmx768m -Djava.awt.headless=true -Dfile.encoding=UTF-8 -jar htbb-server.jar >> "$STG/logs/game.out" 2>&1 ) &
PID_GAME=$!
for _ in $(seq 1 240); do [ -s "$STG/server/p2p/ticket.txt" ] && ss -ltn | grep -q ":$CONG_GAME " && break; sleep 1; done
[ -s "$STG/server/p2p/ticket.txt" ] || { tail -40 "$STG/logs/game.out"; loi "khong co ticket P2P"; }
grep -a '\[P2P\]' "$STG/logs/game.out" | sed 's/^/   /' | cut -c1-200

buoc "4/5 bot nguoi choi thuong qua P2P (KiemDangNhapP2P)"
BOT="$RUN/bot-cls"; mkdir -p "$BOT"
javac -encoding UTF-8 -nowarn -Xlint:none -cp "$R/server/lib/jxl-2.6.jar:$R/server/lib/mysql-connector-java-5.1.49.jar" \
    -sourcepath "$R/tools/pokemon/kiem_tai:$R/server/src" -d "$BOT" "$R/tools/pokemon/kiem_tai/BotChucNang.java" \
    "$R/p2p/test/KiemDangNhapP2P.java" "$R/p2p/peerova/bindings/java/src/dev/peerova/"*.java > "$RUN/bot-javac.txt" 2>&1 || { cat "$RUN/bot-javac.txt"; loi "javac bot"; }
java -Dpeerova.jni.path="$JNI" -Dnoi="$STG" -Dgoc="$R" -Dgm=0 -cp "$BOT" KiemDangNhapP2P "@$STG/server/p2p/ticket.txt" "$CONG_GAME" kt101 kt102 kt103 \
    2>&1 | tee "$RUN/bot.txt"
KQ=${PIPESTATUS[0]}

buoc "5/5 lop ket noi cua client that qua P2P: PC Java (HTBB-PC.jar) + Unity PC (C# .NET 8)"
PCJ="$R/p2p/out/client_pc/HTBB-PC.jar"
if [ -f "$PCJ" ]; then
    KC="$RUN/kc-cls"; mkdir -p "$KC"
    javac --release 8 -nowarn -Xlint:none -cp "$PCJ" -d "$KC" "$R/p2p/test/KiemConnectorP2P.java" > /dev/null 2>&1 || { echo "   [HONG] javac KiemConnectorP2P"; KQ=1; }
    java -Djava.awt.headless=true -Dpeerova.extract.dir="$RUN/pc-native" -cp "$PCJ:$KC" KiemConnectorP2P "@$STG/server/p2p/ticket.txt" "$CONG_GAME" 2>&1 \
        | tee "$RUN/pc.txt" | grep -E "OK|HONG|DAT|thu vien" | sed 's/^/   /'
    [ "${PIPESTATUS[0]}" = 0 ] || KQ=1
else
    echo "   (bo qua PC: chua co $PCJ - P2P_TICKET_FILE=... HTBB_PC_OUT=$R/p2p/out/client_pc sh client_pc/build.sh)"
fi
if command -v dotnet > /dev/null 2>&1; then
    (cd "$R/p2p/test/unity-net" && dotnet build -c Release -o "$RUN/unity-net" > "$RUN/unity-build.txt" 2>&1) || { echo "   [HONG] dotnet build"; KQ=1; }
    PEEROVA_LIB="$R/p2p/out/dist/linux-x86_64/libpeerova.so" dotnet "$RUN/unity-net/KiemUnityP2P.dll" "@$STG/server/p2p/ticket.txt" "$CONG_GAME" 2>&1 \
        | tee "$RUN/unity.txt" | grep -E "OK|HONG|DAT" | sed 's/^/   /'
    [ "${PIPESTATUS[0]}" = 0 ] || KQ=1
else
    echo "   (bo qua Unity .NET: khong co dotnet)"
fi
sleep 2
echo "   --- may chu (dong P2P / IP that):"
grep -a '\[P2P\]' "$STG/logs/game.out" | tail -15 | sed 's/^/   /' | cut -c1-200
kill -0 "$PID_GAME" 2>/dev/null && echo "   game con song" || { echo "   [HONG] game chet"; KQ=1; }
[ "$KQ" = 0 ] && echo "KET QUA: DAT" || echo "KET QUA: HONG"
exit "$KQ"
