#!/bin/sh
# Chay TOAN BO server Huyen Thoai Bao Boi tren NovaDB (may dev / test):
#   NovaDB (cong MySQL) -> nap sql/htbb_day_du.sql neu CSDL trong -> login server -> game server.
#
# Chay o thu muc HTBB/:
#   sh tools/pokemon/chay_tren_novadb.sh            # khoi dong
#   sh tools/pokemon/chay_tren_novadb.sh dung       # dung ca 3
#
# Bien moi truong (co gia tri mac dinh):
#   NOVADBD      duong dan novadbd (>= 0.3.0 co cong MySQL, PASSWORD(), NOVADB_MYSQL_TIME_ZONE)
#   NOVA_DATA    thu muc du lieu NovaDB             NOVA_PORT  cong MySQL (3318)
#   NOVA_USER / NOVA_PASS  tai khoan cong MySQL      MYSQL      lenh mysql client (de nap SQL)
#   CHAY         thu muc chua pid + log (mac dinh ./chay_novadb, nam trong .gitignore)
#
# Game server doc server.ini o HTBB/: script SINH server.ini tu vps/server.ini.tmpl (tro vao NovaDB,
# db.thamSoJdbc=useSSL=false). server.ini nam trong .gitignore.
set -e
NOVADBD=${NOVADBD:-$HOME/Downloads/github/nova-db/NovaDB/target/release/novadbd}
NOVA_DATA=${NOVA_DATA:-$(pwd)/../novadb-data}
NOVA_PORT=${NOVA_PORT:-3318}
NOVA_USER=${NOVA_USER:-kpah}
NOVA_PASS=${NOVA_PASS:-htbb}
MYSQL=${MYSQL:-mysql}
CHAY=${CHAY:-$(pwd)/chay_novadb}
CP="build/classes:lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:lib/NQSH_5h.jar"

dung() {
    for f in game login nova; do
        if [ -f "$CHAY/$f.pid" ]; then
            pid=$(cat "$CHAY/$f.pid")
            kill "$pid" 2>/dev/null || true
            # doi tien trinh thoat han (nha cong) truoc khi khoi dong lai
            i=0; while kill -0 "$pid" 2>/dev/null && [ $i -lt 30 ]; do i=$((i+1)); sleep 1; done
            rm -f "$CHAY/$f.pid"
            echo "da dung $f"
        fi
    done
}

if [ "$1" = "dung" ]; then dung; exit 0; fi

mkdir -p "$CHAY" "$NOVA_DATA" "$CHAY/login" "$CHAY/log"
m() { $MYSQL --no-defaults -h127.0.0.1 -P"$NOVA_PORT" -u"$NOVA_USER" -p"$NOVA_PASS" --skip-ssl --default-character-set=utf8mb4 "$@"; }

# 1. NovaDB: gio dia phuong nhu MySQL (NOW() = gio may), cong MySQL chi nghe tren loopback.
NOVADB_MYSQL_TIME_ZONE=SYSTEM NOVADB_MYSQL_PASSWORD="$NOVA_PASS" nohup "$NOVADBD" \
    --listen 127.0.0.1:8797 --database-path "$NOVA_DATA/relay.sqlite3" --data-dir "$NOVA_DATA" \
    --mysql-listen 127.0.0.1:"$NOVA_PORT" --mysql-user "$NOVA_USER" > "$CHAY/log/novadb.log" 2>&1 &
echo $! > "$CHAY/nova.pid"
i=0; until m -e "select 1" > /dev/null 2>&1; do i=$((i+1)); [ $i -gt 60 ] && { echo "NovaDB khong len"; exit 1; }; sleep 1; done
echo "NovaDB san sang (cong $NOVA_PORT, $(m -N -e 'select version()'))"

# 2. Nap CSDL day du neu chua co kpah_db.
if ! m -N -e "show databases" | grep -qx kpah_db; then
    echo "Nap sql/htbb_day_du.sql ..."
    m < sql/htbb_day_du.sql
fi

# 3. Bien dich server (neu chua co build/classes moi hon src).
if [ ! -d build/classes ] || [ -n "$(find src -newer build/classes -name '*.java' | head -1)" ]; then
    echo "Bien dich server ..."
    rm -rf build/classes && mkdir -p build/classes
    find src -name '*.java' > build/nguon.txt
    javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:lib/NQSH_5h.jar" \
        -sourcepath src -d build/classes @build/nguon.txt
fi

# 4. server.ini cho game server (tro vao NovaDB).
sed -e "s/^db\.host\(1\?\)=.*/db.host\1=127.0.0.1:$NOVA_PORT/" -e "s/^db\.user\(1\?\)=.*/db.user\1=$NOVA_USER/" \
    -e "s/^db\.password\(1\?\)=.*/db.password\1=$NOVA_PASS/" -e "s/^db\.userNap=.*/db.userNap=$NOVA_USER/" \
    -e "s/^db\.pass=.*/db.pass=$NOVA_PASS/" -e "s/^db\.thamSoJdbc=.*/db.thamSoJdbc=useSSL=false/" \
    -e "s|^sv\.pathLog=.*|sv.pathLog=$CHAY/log/|" -e "s|^db\.run=.*|db.run=/bin/true|" vps/server.ini.tmpl > server.ini

# 5. Login server (cong 8023) + game server (cong 19129).
cp loginServer/CheckLoginSocket.jar "$CHAY/login/"
printf 'db.url=jdbc:mysql://127.0.0.1:%s/account?useSSL=false\ndb.user=%s\ndb.password=%s\nport=8023\n' \
    "$NOVA_PORT" "$NOVA_USER" "$NOVA_PASS" > "$CHAY/login/server.ini"
(cd "$CHAY/login" && nohup java -jar CheckLoginSocket.jar > "$CHAY/log/login.log" 2>&1 & echo $! > "$CHAY/login.pid")
sleep 2
nohup java -Dfile.encoding=UTF-8 -cp "$CP" server.TeamServer > "$CHAY/log/game.log" 2>&1 &
echo $! > "$CHAY/game.pid"
i=0; until grep -q "Listen on port" "$CHAY/log/game.log" 2>/dev/null; do i=$((i+1)); [ $i -gt 120 ] && { echo "game server chua len, xem $CHAY/log/game.log"; exit 1; }; sleep 1; done
echo "Game server san sang: cong 19129. Log: $CHAY/log/game.log. Dung: sh tools/pokemon/chay_tren_novadb.sh dung"
