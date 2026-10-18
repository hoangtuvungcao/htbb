#!/bin/sh
# ============================================================================
#  Chuyen CSDL may dev (NovaDB) kpah_db -> htbb_db, user NovaDB kpah -> htbb.
#  Xem HTBB/tools/doi_ten/KE_HOACH_DOI_TEN.md (muc "Du lieu may dev").
#
#  Chay o bat ky dau (tu do duong dan theo vi tri script):
#      sh HTBB/tools/doi_ten/chuyen_csdl.sh                 # CHAY THU: chi in se lam gi
#      sh HTBB/tools/doi_ten/chuyen_csdl.sh --apply         # dung stack -> doi ten -> sua ini (KHONG bat lai)
#      sh HTBB/tools/doi_ten/chuyen_csdl.sh --apply --khoi-dong   # ... roi bat lai NovaDB + login + chay_lap.sh
#      sh HTBB/tools/doi_ten/chuyen_csdl.sh --quay-lai      # tra lai kpah_db + ini cu (tu ban sao luu moi nhat)
#
#  Tuy chon them:
#      --ca-csdl-phu              doi ca kpah_db_* (kpah_db_bot, kpah_db_hst...) -> htbb_db_*
#      --bo-qua-tien-trinh-khac   van chay khi con TeamServer KHAC (khong phai chay_novadb/game.pid)
#                                 dang dung NovaDB - cac tien trinh do se mat ket noi (NovaDB bat lai,
#                                 user doi kpah -> htbb). Mac dinh: TU CHOI.
#  Bien: NOVADBD, NOVA_DATA, NOVA_PORT (3318), NOVA_PASS (htbb), NOVA_USER_MOI (htbb), MYSQL (mysql)
#
#  Lam lai nhieu lan khong sao: da co htbb_db.novadb va khong con kpah_db.novadb = bo qua buoc doi ten.
#  Chi giet PID ghi trong chay_novadb/*.pid (stack cua chinh repo nay). Khong pkill.
# ============================================================================
set -eu

HTBB=$(cd "$(dirname "$0")/../.." && pwd -P)
GOC=$(cd "$HTBB/.." && pwd -P)
WEB="$GOC/HTBB-Web"
# Monorepo htbb/: game server o htbb/server, website o htbb/web (repo cu: HTBB/ va ../HTBB-Web)
if [ -d "$HTBB/server/src" ]; then
    WEB="$HTBB/web"
    HTBB="$HTBB/server"
fi
NOVADBD=${NOVADBD:-$HOME/Downloads/github/nova-db/NovaDB/target/release/novadbd}
NOVA_DATA=${NOVA_DATA:-$GOC/novadb-data}
NOVA_PORT=${NOVA_PORT:-3318}
NOVA_PASS=${NOVA_PASS:-htbb}
NOVA_USER_MOI=${NOVA_USER_MOI:-htbb}
MYSQL=${MYSQL:-mysql}
CHAY="$HTBB/chay_novadb"

APPLY=0; KHOI_DONG=0; QUAY_LAI=0; CA_PHU=0; BO_QUA_KHAC=0
for a in "$@"; do
    case "$a" in
        --apply) APPLY=1 ;;
        --khoi-dong) KHOI_DONG=1 ;;
        --quay-lai) QUAY_LAI=1; APPLY=1 ;;
        --ca-csdl-phu) CA_PHU=1 ;;
        --bo-qua-tien-trinh-khac) BO_QUA_KHAC=1 ;;
        -h|--help) sed -n 2,24p "$0"; exit 0 ;;
        *) echo "Tuy chon la: $a"; exit 2 ;;
    esac
done

log() { echo "[chuyen_csdl] $*"; }
lam() { if [ "$APPLY" = 1 ]; then "$@"; else echo "   (thu) $*"; fi; }
pid_cua() { [ -f "$CHAY/$1.pid" ] && cat "$CHAY/$1.pid" || true; }
song() { [ -n "${1:-}" ] && kill -0 "$1" 2>/dev/null; }

# ---- 0. Tien trinh KHAC dang dung NovaDB? ----
GAME_PID=$(pid_cua game)
KHAC=""
for p in $(pgrep -f 'server\.TeamServer|htbb-server\.jar|KPAH2_patched\.jar' 2>/dev/null || true); do
    [ "$p" = "$GAME_PID" ] && continue
    [ "$p" = "$$" ] && continue
    KHAC="$KHAC $p"
done
if [ -n "$KHAC" ]; then
    log "CANH BAO: con game server KHAC dang chay (PID:$KHAC):"
    for p in $KHAC; do ps -o pid=,args= -p "$p" 2>/dev/null | cut -c1-160 | sed 's/^/      /'; done
    log "  NovaDB se tat/bat lai va user doi '$NOVA_USER_MOI' - cac tien trinh tren se mat CSDL."
    if [ "$BO_QUA_KHAC" != 1 ] && [ "$QUAY_LAI" != 1 ]; then
        log "  Dung lai. Tat chung truoc (nguoi chay chung tu tat), hoac them --bo-qua-tien-trinh-khac."
        [ "$APPLY" = 1 ] && exit 1
    fi
fi

# ---- 1. Dung stack cua repo nay (game qua chay_lap.sh, login, NovaDB) ----
dung() {
    log "Dung stack (chay_novadb/*.pid)"
    lam touch "$CHAY/DUNG_HAN"
    for f in chay_lap game login nova; do
        p=$(pid_cua $f)
        if song "$p"; then
            log "  kill $f PID $p"
            lam kill "$p"
            if [ "$APPLY" = 1 ]; then
                # game: shutdown hook luu nhan vat (systemd cho toi 180s) - cho du lau
                i=0; while song "$p" && [ $i -lt 180 ]; do i=$((i+1)); sleep 1; done
                if song "$p"; then log "  $f PID $p chua thoat sau 180s - dung lai, KHONG doi ten khi CSDL con mo"; exit 1; fi
            fi
        fi
    done
}

mo_file() {  # in ra PID dang mo mot trong cac tep (quet /proc, khong can fuser/lsof)
    for d in /proc/[0-9]*/fd; do
        for fd in "$d"/*; do
            l=$(readlink "$fd" 2>/dev/null) || continue
            for f in "$@"; do [ "$l" = "$f" ] && { p=${d#/proc/}; echo " ${p%/fd}"; }; done
        done
    done | sort -u | tr -d '\n'
}

sua_ini() {  # $1 = tep, $2 = chieu (moi|cu)
    t=$1
    [ -f "$t" ] || return 0
    if [ "$2" = moi ]; then
        bt='s/^(db\.(name|nameNap)[0-9]*)=kpah_db(_[A-Za-z0-9]+)?$/\1=htbb_db\3/; s/^(db\.(user|userNap)[0-9]*)=kpah$/\1='"$NOVA_USER_MOI"'/; s#(jdbc:mysql://[^/]+/)kpah_db#\1htbb_db#'
    else
        bt='s/^(db\.(name|nameNap)[0-9]*)=htbb_db(_[A-Za-z0-9]+)?$/\1=kpah_db\3/; s/^(db\.(user|userNap)[0-9]*)='"$NOVA_USER_MOI"'$/\1=kpah/; s#(jdbc:mysql://[^/]+/)htbb_db#\1kpah_db#'
    fi
    if [ "$APPLY" = 1 ]; then
        sed -i -E "$bt" "$t"
        log "  sua $t"
    else
        echo "   (thu) sua $t:"; sed -E "$bt" "$t" | diff "$t" - | sed 's/^/        /' || true
    fi
}

TEP_INI="$HTBB/server.ini $CHAY/login/server.ini $HTBB/loginServer/server.ini"

# ---- QUAY LAI ----
if [ "$QUAY_LAI" = 1 ]; then
    LUU=$(ls -1dt "$NOVA_DATA"/sao_luu_doi_ten_* 2>/dev/null | head -1)
    [ -n "$LUU" ] || { log "Khong co ban sao luu sao_luu_doi_ten_* trong $NOVA_DATA"; exit 1; }
    dung
    [ -z "$(mo_file "$NOVA_DATA"/htbb_db*.novadb)" ] || { log "CSDL con dang mo - dung"; exit 1; }
    for f in "$NOVA_DATA"/htbb_db*.novadb; do
        [ -e "$f" ] || continue
        cu="$NOVA_DATA/kpah_db${f#"$NOVA_DATA"/htbb_db}"
        [ -e "$cu" ] && { log "Da co $cu - khong ghi de (xem tay)"; exit 1; }
        mv "$f" "$cu"; rm -f "$f-wal" "$f-shm"
        log "  $f -> $cu"
    done
    for t in $TEP_INI; do sua_ini "$t" cu; done
    log "Xong quay lai. Ban sao luu goc (neu can thay ca tep): $LUU. Bat lai: NOVA_USER=kpah sh tools/pokemon/chay_tren_novadb.sh"
    exit 0
fi

# ---- 2. Kiem tra ----
command -v python3 >/dev/null || { log "Thieu python3 (dung module sqlite3 de checkpoint WAL)"; exit 1; }
DS="kpah_db"
if [ "$CA_PHU" = 1 ]; then
    for f in "$NOVA_DATA"/kpah_db_*.novadb; do [ -e "$f" ] && DS="$DS $(basename "$f" .novadb)"; done
fi
CAN=""
for d in $DS; do
    cu="$NOVA_DATA/$d.novadb"; moi="$NOVA_DATA/htbb_db${d#kpah_db}.novadb"
    if [ -e "$cu" ] && [ -e "$moi" ]; then log "XUNG DOT: co ca $cu va $moi - xem tay"; exit 1; fi
    if [ -e "$cu" ]; then CAN="$CAN $d"; else log "  $d: da doi (hoac khong co) - bo qua"; fi
done
log "Se doi:${CAN:- (khong co gi)}"
log "Tep cau hinh: $TEP_INI"

# ---- 3. Dung + sao luu + doi ten ----
if [ -n "$CAN" ]; then
    dung
    if [ "$APPLY" = 1 ]; then
        for d in $CAN; do
            m=$(mo_file "$NOVA_DATA/$d.novadb" "$NOVA_DATA/$d.novadb-wal")
            [ -z "$m" ] || { log "$d.novadb van dang bi mo boi PID:$m - dung (NovaDB/tien trinh khac chua tat)"; exit 1; }
        done
    fi
    LUU="$NOVA_DATA/sao_luu_doi_ten_$(date +%Y%m%d_%H%M%S)"
    lam mkdir -p "$LUU"
    for d in $CAN; do
        for f in "$NOVA_DATA/$d.novadb" "$NOVA_DATA/$d.novadb-wal" "$NOVA_DATA/$d.novadb-shm"; do
            [ -e "$f" ] && lam cp -p "$f" "$LUU/"
        done
    done
    for t in $TEP_INI "$CHAY/chay_lap.sh"; do [ -f "$t" ] && lam cp -p "$t" "$LUU/$(echo "$t" | sed "s#^$GOC/##; s#/#__#g")"; done
    for d in $CAN; do
        cu="$NOVA_DATA/$d.novadb"; moi="$NOVA_DATA/htbb_db${d#kpah_db}.novadb"
        if [ "$APPLY" = 1 ]; then
            # Don WAL vao tep chinh roi moi doi ten: khong de lai -wal ten cu.
            python3 -I - "$cu" <<'PY'
import sqlite3, sys
c = sqlite3.connect(sys.argv[1])
ok = c.execute('PRAGMA quick_check').fetchone()[0]
if ok != 'ok':
    sys.exit('quick_check: %s' % ok)
r = c.execute('PRAGMA wal_checkpoint(TRUNCATE)').fetchone()
if r[0] != 0:
    sys.exit('checkpoint bi chan: %r' % (r,))
c.close()
PY
            mv "$cu" "$moi"
            rm -f "$cu-wal" "$cu-shm"
            log "  $cu -> $moi"
        else
            echo "   (thu) checkpoint WAL + mv $cu -> $moi"
        fi
    done
    log "Sao luu: $LUU"
fi

# ---- 4. Sua cau hinh may dev ----
for t in $TEP_INI; do sua_ini "$t" moi; done
if [ -f "$WEB/.env" ]; then
    if [ "$APPLY" = 1 ]; then
        sed -i -E 's/^KPAH_/HTBB_/; s/=kpah_db$/=htbb_db/' "$WEB/.env"; log "  sua $WEB/.env (KPAH_ -> HTBB_)"
    else
        echo "   (thu) sua $WEB/.env: KPAH_ -> HTBB_, kpah_db -> htbb_db"
    fi
fi

# ---- 5. Bat lai (tuy chon) ----
if [ "$KHOI_DONG" = 1 ] && [ "$APPLY" = 1 ]; then
    log "Bat NovaDB (user $NOVA_USER_MOI)"
    mkdir -p "$CHAY/log"
    NOVADB_MYSQL_TIME_ZONE=SYSTEM NOVADB_MYSQL_PASSWORD="$NOVA_PASS" nohup "$NOVADBD" \
        --listen 127.0.0.1:8797 --database-path "$NOVA_DATA/relay.sqlite3" --data-dir "$NOVA_DATA" \
        --mysql-listen 127.0.0.1:"$NOVA_PORT" --mysql-user "$NOVA_USER_MOI" >> "$CHAY/log/novadb.log" 2>&1 &
    echo $! > "$CHAY/nova.pid"
    m() { $MYSQL --no-defaults -h127.0.0.1 -P"$NOVA_PORT" -u"$NOVA_USER_MOI" -p"$NOVA_PASS" --skip-ssl "$@"; }
    i=0; until m -e "select 1" >/dev/null 2>&1; do i=$((i+1)); [ $i -gt 60 ] && { log "NovaDB khong len"; exit 1; }; sleep 1; done
    m -N -e "show databases" | grep -qx htbb_db || { log "NovaDB khong thay htbb_db"; exit 1; }
    log "  htbb_db: $(m -N htbb_db -e 'select count(*) from tob_char') nhan vat"
    log "Bat login server"
    (cd "$CHAY/login" && nohup java -jar CheckLoginSocket.jar >> "$CHAY/log/login.log" 2>&1 & echo $! > "$CHAY/login.pid")
    sleep 2
    rm -f "$CHAY/DUNG_HAN"
    LJ=lib/htbb-core.jar; [ -f "$HTBB/$LJ" ] || LJ=lib/NQSH_5h.jar
    if [ ! -d "$HTBB/build/classes" ] || [ -n "$(find "$HTBB/src" -newer "$HTBB/build/classes" -name '*.java' | head -1)" ]; then
        log "Bien dich server vao build/classes"
        (cd "$HTBB" && rm -rf build/classes && mkdir -p build/classes && find src -name '*.java' > build/nguon.txt &&
         javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:$LJ" \
             -sourcepath src -d build/classes @build/nguon.txt)
    fi
    log "Bat game qua chay_lap.sh"
    n=$(wc -l < "$CHAY/log/game.log" 2>/dev/null || echo 0)
    (cd "$HTBB" && nohup sh chay_novadb/chay_lap.sh > /dev/null 2>&1 & echo $! > "$CHAY/chay_lap.pid")
    i=0; until tail -n +"$((n+1))" "$CHAY/log/game.log" 2>/dev/null | grep -q "Listen on port"; do
        i=$((i+1)); [ $i -gt 180 ] && { log "game chua len, xem $CHAY/log/game.log"; exit 1; }; sleep 1
    done
    log "Game server san sang (PID $(cat "$CHAY/game.pid"))."
elif [ "$APPLY" = 1 ]; then
    log "Chua bat lai (them --khoi-dong, hoac: sh tools/pokemon/chay_tren_novadb.sh). Nho xoa chay_novadb/DUNG_HAN truoc khi chay chay_lap.sh."
fi
[ "$APPLY" = 1 ] || log "(Chay thu - chua lam gi. Them --apply.)"
