#!/bin/bash
# ============================================================================================================
#  LEN TAT CA - build + kiem + dua len VPS trong MOT lenh: may chu game, SQL, ban do (cMap), web, client
#  (jar / jar nhe / APK / PC), dich vu Huan luyen vien AI (bot), may chu dang nhap moi (tuy chon).
#
#  Chu game tu chay khi muon (chay tu thu muc goc repo htbb):
#
#    bash tools/phat-hanh/len-tat-ca.sh                    # build + suite + cong kiem staging + len het (hoi xac nhan)
#    bash tools/phat-hanh/len-tat-ca.sh --chi-build        # chi build + kiem tren may, KHONG dong vao VPS
#    bash tools/phat-hanh/len-tat-ca.sh --bo-client        # khong build / khong tai client (giu ban dang phat hanh)
#    bash tools/phat-hanh/len-tat-ca.sh --bo-web           # khong build / khong len web
#    bash tools/phat-hanh/len-tat-ca.sh --bo-may-chu       # khong len jar may chu + SQL (vd chi tai client moi)
#    bash tools/phat-hanh/len-tat-ca.sh --bot              # cai / cap nhat dich vu bot AI (htbb-hlv-ai); so bot chinh o /admin/hlv-ai
#    bash tools/phat-hanh/len-tat-ca.sh --dang-nhap-moi    # chuyen VPS sang may chu dang nhap moi (htbb-login.jar)
#    bash tools/phat-hanh/len-tat-ca.sh --bo-suite         # bo bo test may chu tren may (KHONG khuyen)
#    bash tools/phat-hanh/len-tat-ca.sh --cong-day-du      # cong kiem DAY DU (~1 gio, moi nhom) thay vi cong nhanh mac dinh
#    bash tools/phat-hanh/len-tat-ca.sh --bo-cong-kiem     # bo cong kiem staging: phai GO DUNG "LEN KHONG KIEM"
#    bash tools/phat-hanh/len-tat-ca.sh --co               # khong hoi lai truoc khi len production
#
#  Bien moi truong (tuy chon):
#    VPS=root@<ip>                 mac dinh: host trong MAY_CHU_CHINH= cua tools/phat-hanh/may_chu.conf
#    JAVA8_HOME=...                JDK 8 cho build jar J2ME (mac dinh /usr/lib/jvm/java-8-openjdk-amd64)
#    NODE22=/duong/node22/bin      Node >= 22 cho web (mac dinh tim trong ~/.nvm)
#    HTBB_KEY_PROPERTIES=...       khoa ky APK (mac dinh ~/.htbb-keys/htbb-release.properties)
#    LUONG=<n>                     so lan bot cong kiem chay cung luc (STG_BOT_LAN_NHANH / STG_BOT_LAN tren VPS; mac dinh
#                                  de cong kiem tu chon: nhanh 9, day du 4)
#
#  Thu tu: (1) build + kiem tren may -> (2) tai ung vien len staging + cong kiem NHANH (~10 phut; --cong-day-du = 32 nhom
#  ~1 gio) -> (3) production:
#  sao luu + bao nguoi choi 62 giay + nang cap CSDL + jar moi (vps/len-production.sh) -> ban do cMap doi -> web ->
#  client + trang tai game -> bot AI -> dang nhap moi. Buoc nao hong la DUNG, khong lam buoc sau.
#  Khong bao gio in mat khau / bi mat. Log day du: ../len_tat_ca_logs/<gio>.log
# ============================================================================================================
set -u
GOC=$(cd "$(dirname "$0")/../.." && pwd)
cd "$GOC"
. tools/phat-hanh/phien_ban.conf
[ -f tools/phat-hanh/may_chu.conf ] && . tools/phat-hanh/may_chu.conf
SO=${PHIEN_BAN%%-*}

CLIENT=1; WEB=1; MAYCHU=1; BOT=0; DANGNHAP=0; SUITE=1; CONGKIEM=1; HOI=1; CHIBUILD=0; CONG_CHE_DO=nhanh
for a in "$@"; do
    case "$a" in
        --bo-client) CLIENT=0 ;; --bo-web) WEB=0 ;; --bo-may-chu) MAYCHU=0 ;; --bot) BOT=1 ;;
        --dang-nhap-moi) DANGNHAP=1 ;; --bo-suite) SUITE=0 ;; --bo-cong-kiem) CONGKIEM=0 ;; --co) HOI=0 ;;
        --chi-build) CHIBUILD=1 ;; --cong-day-du) CONG_CHE_DO=day-du ;;
        -h|--help) sed -n 2,33p "$0"; exit 0 ;;
        *) echo "Tham so la: $a (xem --help)"; exit 2 ;;
    esac
done

VPS=${VPS:-root@${MAY_CHU_CHINH%%:*}}
SSH="ssh -o ConnectTimeout=20 -o ServerAliveInterval=30"
SCP="scp -o ConnectTimeout=20"
JAVA8_HOME=${JAVA8_HOME:-/usr/lib/jvm/java-8-openjdk-amd64}
HTBB_KEY_PROPERTIES=${HTBB_KEY_PROPERTIES:-$HOME/.htbb-keys/htbb-release.properties}
export JAVA8_HOME HTBB_KEY_PROPERTIES
# LUONG cu (=6) tung duoc truyen thanh HOI_QUY_SONG_SONG=6 - bien do chi nhan 1 (song song) -> cong kiem chay TUAN TU.
# Nay: chi dat so lan cung luc khi nguoi chay ghi ro LUONG.
LUONG=${LUONG:-}
SHA=$(git rev-parse --short HEAD)
LOGD="$(dirname "$GOC")/len_tat_ca_logs"; mkdir -p "$LOGD"
LOG="$LOGD/$(date +%Y%m%d_%H%M)_$SHA.log"
TAM="$(dirname "$GOC")/len_tat_ca_tam"; mkdir -p "$TAM"

buoc() { echo; echo "== $(date +%T) $*" | tee -a "$LOG"; }
loi()  { echo "[DUNG] $*" | tee -a "$LOG"; exit 1; }
chay() { "$@" >>"$LOG" 2>&1 || loi "lenh hong: $* (xem $LOG)"; }

echo "LEN TAT CA - commit $SHA, client $PHIEN_BAN (BAN $BAN), VPS $VPS" | tee "$LOG"
echo "   client=$CLIENT web=$WEB may_chu=$MAYCHU bot=$BOT dang_nhap_moi=$DANGNHAP suite=$SUITE cong_kiem=$CONGKIEM" | tee -a "$LOG"
[ -z "$(git status --porcelain --untracked-files=no)" ] || echo "   CANH BAO: thu muc co thay doi chua commit - build dung trang thai hien tai" | tee -a "$LOG"

# ------------------------------------------------------------------ 1. build + kiem tren may
if [ $MAYCHU = 1 ] || [ $BOT = 1 ]; then
    buoc "1a. Build jar may chu (server/htbb-server.jar)"
    B=$(mktemp -d "$TAM/sv.XXXX")
    ( cd server && find src -name '*.java' > "$B/src.txt" && mkdir "$B/c" &&
      javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar" \
            -sourcepath src -d "$B/c" @"$B/src.txt" &&
      printf 'Main-Class: server.TeamServer\nClass-Path: lib/jxl-2.6.jar lib/mysql-connector-java-5.1.49.jar\n' > "$B/mf.txt" &&
      jar cfm "$B/htbb-server.jar" "$B/mf.txt" -C "$B/c" . && unzip -tq "$B/htbb-server.jar" >/dev/null &&
      cp "$B/htbb-server.jar" htbb-server.jar ) >>"$LOG" 2>&1 || loi "build jar may chu hong"
    rm -rf "$B"
    echo "   jar $(sha256sum server/htbb-server.jar | cut -c1-16)" | tee -a "$LOG"
    if [ -f server/loginServer/build.sh ]; then
        buoc "1b. Build may chu dang nhap (server/loginServer/htbb-login.jar)"
        chay sh server/loginServer/build.sh
    fi
    if [ $SUITE = 1 ]; then
        buoc "1c. Bo test may chu (Kiem) - khoang 25 phut"
        W="$TAM/suite"; rm -rf "$W"; git worktree add --detach "$W" HEAD >>"$LOG" 2>&1 || loi "khong tao duoc worktree suite"
        cp server/htbb-server.jar "$W/server/"
        ( cd "$W/server" && grep -v '^sv.he_sinh_thai=' ../vps/server.ini.linux.example > server.ini &&
          LIBCP="lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar" && T="$W/kc" && mkdir -p "$T" &&
          find src test -name '*.java' > "$T.txt" &&
          javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$LIBCP" -sourcepath "src:test" -d "$T" @"$T.txt" &&
          timeout -k 10 2400 java -Djava.awt.headless=true -cp "$T:$LIBCP" Kiem ) > "$LOGD/suite_$SHA.log" 2>&1
        KQ=$?
        git worktree remove --force "$W" >>"$LOG" 2>&1
        tail -3 "$LOGD/suite_$SHA.log" | tee -a "$LOG"
        [ $KQ = 0 ] || loi "bo test may chu HONG - xem $LOGD/suite_$SHA.log (dong 'HONG')"
    fi
fi

if [ $WEB = 1 ]; then
    buoc "1d. Build web (Nuxt) + test web"
    NODE22=${NODE22:-$(ls -d "$HOME"/.nvm/versions/node/v22*/bin 2>/dev/null | tail -1)}
    [ -n "$NODE22" ] && [ -x "$NODE22/node" ] || loi "can Node 22 (dat NODE22=/duong/node22/bin)"
    ( cd web && export PATH="$NODE22:$PATH" && { [ -d node_modules ] || npm ci --no-audit --no-fund; } &&
      npm run build && node --test tests/*.test.ts ) >>"$LOG" 2>&1 || loi "build / test web hong"
    ( cd web && tar czf "$TAM/web_$SHA.tgz" .output ) || loi "khong nen duoc web"
    echo "   web $(du -h "$TAM/web_$SHA.tgz" | cut -f1)" | tee -a "$LOG"
fi

if [ $CLIENT = 1 ]; then
    buoc "1e. Build client $PHIEN_BAN: jar + jar nhe + APK (ky) + PC (Windows/Linux)"
    [ -n "${MAY_CHU_CHINH:-}" ] || loi "thieu MAY_CHU_CHINH trong tools/phat-hanh/may_chu.conf"
    chay sh client_jar/build.sh "$BAN"
    chay env GOI=nhe sh client_jar/build.sh "$BAN"
    chay env KIEU=release sh client_apk/build-pokemon.sh
    chay env KIEU=phat-hanh sh client_pc/dong_goi_windows.sh
    chay sh tools/phat-hanh/gom-ban.sh
    PH="$(dirname "$GOC")/phat_hanh"
    for f in "$PH"/HTBB-"$PHIEN_BAN".jar "$PH"/HTBB-"$PHIEN_BAN"-nhe.jar; do
        unzip -p "$f" META-INF/MANIFEST.MF | grep -q "HTBB-May-Chu: $MAY_CHU_CHINH" || loi "$f khong tro dung may chu $MAY_CHU_CHINH"
        unzip -p "$f" META-INF/MANIFEST.MF | grep -q "HTBB-Ban-Thu" && loi "$f la ban THU (HTBB-Ban-Thu)"
    done
    (cd "$PH" && sha256sum -c SHA256SUMS.txt >>"$LOG" 2>&1) || loi "SHA256SUMS khong khop"
    ls "$PH" | tee -a "$LOG"
fi

[ $CHIBUILD = 1 ] && { buoc "XONG (--chi-build): khong dong vao VPS. Log: $LOG"; exit 0; }

# ------------------------------------------------------------------ 2. cong kiem staging
UV=/opt/htbb-staging/ung-vien-tatca
if [ $MAYCHU = 1 ]; then
    if [ $CONGKIEM = 1 ]; then
        if [ "$CONG_CHE_DO" = nhanh ]; then
            buoc "2. Cong kiem staging NHANH (bo nhom loi cot, ~10 phut; --cong-day-du = 32 nhom ~1 gio)"
            CHO_GIAY=30; LAN_ENV=${LUONG:+STG_BOT_LAN_NHANH=$LUONG}
        else
            buoc "2. Cong kiem staging DAY DU (32 nhom bot, ~1 gio)"
            CHO_GIAY=120; LAN_ENV=${LUONG:+STG_BOT_LAN=$LUONG}
        fi
        git archive HEAD vps tools/pokemon/kiem_tai server/src | gzip > "$TAM/goi_$SHA.tgz"
        $SSH "$VPS" "mkdir -p $UV" || loi "khong ket noi duoc $VPS"
        $SCP server/htbb-server.jar sql/htbb.sql "$TAM/goi_$SHA.tgz" "$VPS:$UV/" >>"$LOG" 2>&1 || loi "tai ung vien len staging hong"
        $SSH "$VPS" "pgrep -f '^bash /opt/htbb/vps/staging/kiem-truoc' >/dev/null" && loi "dang co cong kiem khac chay tren staging"
        $SSH "$VPS" "cd /opt/htbb && tar --no-overwrite-dir -xzf $UV/goi_$SHA.tgz 2>/dev/null;
            $LAN_ENV setsid nohup bash /opt/htbb/vps/staging/kiem-truoc-khi-len.sh --$CONG_CHE_DO --jar $UV/htbb-server.jar \
            --sql $UV/htbb.sql --nguon /opt/htbb-staging/ung-vien/nguon --ghi-chu $SHA > /opt/htbb-staging/logs/cong-kiem-$SHA.log 2>&1 < /dev/null &" \
            || loi "khong chay duoc cong kiem"
        echo "   log tren VPS: /opt/htbb-staging/logs/cong-kiem-$SHA.log" | tee -a "$LOG"
        until $SSH "$VPS" "grep -q 'CONG KIEM: ' /opt/htbb-staging/logs/cong-kiem-$SHA.log" 2>/dev/null; do
            sleep $CHO_GIAY
            echo "   $(date +%T) $($SSH "$VPS" "grep -cE '^ *DAT ' /opt/htbb-staging/logs/cong-kiem-$SHA.log" 2>/dev/null) nhom DAT, $($SSH "$VPS" "grep -cE '^ *HONG ' /opt/htbb-staging/logs/cong-kiem-$SHA.log" 2>/dev/null) HONG"
        done
        $SSH "$VPS" "grep -E '^ *HONG|CONG KIEM:' /opt/htbb-staging/logs/cong-kiem-$SHA.log | tail -12" | tee -a "$LOG"
        $SSH "$VPS" "grep -q 'CONG KIEM: DAT' /opt/htbb-staging/logs/cong-kiem-$SHA.log" || loi "CONG KIEM HONG - khong len production"
    else
        echo; echo "Ban dang BO CONG KIEM staging. Go dung: LEN KHONG KIEM  (khac la huy)"
        read -r XN < /dev/tty
        [ "$XN" = "LEN KHONG KIEM" ] || loi "huy (khong go dung)"
        echo "   chu game xac nhan LEN KHONG KIEM luc $(date '+%F %T')" | tee -a "$LOG"
        $SSH "$VPS" "mkdir -p $UV" && $SCP server/htbb-server.jar sql/htbb.sql "$VPS:$UV/" >>"$LOG" 2>&1 || loi "tai ung vien hong"
    fi
fi

# ------------------------------------------------------------------ 3. production
if [ $HOI = 1 ]; then
    echo; echo "Sap len PRODUCTION ($VPS): may_chu=$MAYCHU web=$WEB client=$CLIENT bot=$BOT dang_nhap_moi=$DANGNHAP. Go 'co' de tiep:"
    read -r XN < /dev/tty
    [ "$XN" = "co" ] || loi "huy"
fi

if [ $MAYCHU = 1 ]; then
    buoc "3a. May chu game + SQL (sao luu, bao nguoi choi 62 giay, nang cap CSDL, kiem suc khoe)"
    # ban do doi so voi production: sao luu roi chep (len-production khong chep cMap)
    git archive HEAD server/cMap | tar -x -C "$TAM" && $SCP -r "$TAM/server/cMap" "$VPS:$UV/" >>"$LOG" 2>&1 || loi "tai cMap hong"
    $SSH "$VPS" "set -e; B=/opt/htbb/backup/cmap_\$(date +%Y%m%d_%H%M); mkdir -p \$B; n=0;
        for f in $UV/cMap/*; do t=/opt/htbb/server/cMap/\$(basename \$f);
            if ! cmp -s \$f \$t; then [ -f \$t ] && cp -a \$t \$B/; install -o htbb -g htbb -m 664 \$f \$t; n=\$((n+1)); fi; done;
        echo \"   cMap doi: \$n tep (sao luu \$B)\"" | tee -a "$LOG" || loi "chep cMap hong"
    $SSH "$VPS" "bash /opt/htbb/vps/len-production.sh --jar $UV/htbb-server.jar --sql $UV/htbb.sql --co-sql" 2>&1 | tee -a "$LOG" | tail -14
    $SSH "$VPS" "systemctl is-active htbb-game htbb-login" | tee -a "$LOG" | grep -vq inactive || loi "may chu game / dang nhap khong chay - xem lenh 'quay lai' o tren"
fi

if [ $WEB = 1 ]; then
    buoc "3b. Web (tu quay lai ban truoc neu /api/site khong tra 200)"
    $SSH "$VPS" "SSH_ORIGINAL_COMMAND='nhan-build $SHA' /usr/local/sbin/htbb-deploy-web.sh" < "$TAM/web_$SHA.tgz" 2>&1 | tee -a "$LOG" | tail -5
fi

if [ $CLIENT = 1 ]; then
    buoc "3c. Client $PHIEN_BAN len trang tai game + 'Co ban moi'"
    PH="$(dirname "$GOC")/phat_hanh"
    F_JAR="HTBB-$PHIEN_BAN.jar"; F_NHE="HTBB-$PHIEN_BAN-nhe.jar"; F_APK="HTBB-$PHIEN_BAN-android.apk"
    F_EXE="HTBB-PC-Setup-$SO.exe"; F_LIN="HTBB-PC-Linux-$SO.AppImage"
    $SCP "$PH/$F_JAR" "$PH/$F_NHE" "$PH/$F_APK" "$PH/$F_EXE" "$PH/$F_LIN" "$VPS:/opt/htbb/downloads/" >>"$LOG" 2>&1 || loi "tai client hong"
    NK=${GHI_CHU_BAN:-"Bản $SO: cập nhật mới, xem tin tức trên trang chủ."}
    $SCP tools/phat-hanh/cap_nhat_trang_tai.py "$VPS:/root/cap_nhat_trang_tai.py" >>"$LOG" 2>&1 || loi "tai script trang tai hong"
    $SSH "$VPS" "set -e; cd /opt/htbb/downloads; sha256sum $F_JAR $F_NHE $F_APK $F_EXE $F_LIN > SHA256SUMS.txt
        eval \"\$(grep -E '^HTBB_DB_(HOST|PORT|USER|PASS)=' /etc/htbb/htbb.env)\"; export MYSQL_PWD=\"\$HTBB_DB_PASS\"
        M=\"mysql -h\$HTBB_DB_HOST -P\$HTBB_DB_PORT -u\$HTBB_DB_USER htbb_db\"
        \$M -N --raw -e \"SELECT gia_tri FROM web_cai_dat WHERE khoa='taiGame'\" > /root/taiGame_truoc_$BAN.json
        NK='$NK' python3 /root/cap_nhat_trang_tai.py /root/taiGame_truoc_$BAN.json $BAN '$SO' $F_JAR $F_EXE $F_LIN $F_APK > /root/taiGame_$BAN.sql
        \$M < /root/taiGame_$BAN.sql
        for f in HTBB-*; do case \$f in $F_JAR|$F_NHE|$F_APK|$F_EXE|$F_LIN) ;; *) rm -f \"\$f\";; esac; done; ls" | tee -a "$LOG" || loi "cap nhat trang tai hong"
    for f in $F_JAR $F_NHE $F_APK $F_EXE $F_LIN; do
        echo "   $(curl -s -o /dev/null -r 0-99 --max-time 20 -w '%{http_code}' "http://${MAY_CHU_CHINH%%:*}/tai/$f") $f" | tee -a "$LOG"
    done
fi

if [ $BOT = 1 ]; then
    buoc "3d. Dich vu Huan luyen vien AI (htbb-hlv-ai) - so bot / bat tat chinh o /admin/hlv-ai"
    # dich vu bien dich tu /opt/htbb (lop may chu + bo bot) moi lan khoi dong: dua ma nguon commit nay len truoc
    git archive HEAD vps tools/pokemon/kiem_tai tools/pokemon/hlv_ai tools/phat-hanh/phien_ban.conf server/src | gzip > "$TAM/bot_$SHA.tgz"
    $SSH "$VPS" "mkdir -p $UV" && $SCP "$TAM/bot_$SHA.tgz" "$VPS:$UV/" >>"$LOG" 2>&1 || loi "tai ma nguon bot hong"
    $SSH "$VPS" "cd /opt/htbb && tar --no-overwrite-dir -xzf $UV/bot_$SHA.tgz" >>"$LOG" 2>&1 || loi "giai nen ma nguon bot hong"
    $SSH "$VPS" "set -e
        command -v javac >/dev/null || DEBIAN_FRONTEND=noninteractive apt-get install -y -q openjdk-17-jdk-headless >/dev/null
        install -m 644 /opt/htbb/vps/htbb-hlv-ai.service /etc/systemd/system/ && systemctl daemon-reload
        if [ ! -s /etc/htbb/hlv-ai.env ]; then install -m 640 -g htbb /dev/null /etc/htbb/hlv-ai.env
            echo \"HLV_AI_BI_MAT=\$(openssl rand -hex 24)\" > /etc/htbb/hlv-ai.env; fi
        if [ ! -s /etc/htbb-hlv-ai/hlv-ai.cnf ]; then
            eval \"\$(grep -E '^HTBB_DB_(HOST|PORT|USER|PASS)=' /etc/htbb/htbb.env)\"
            install -d -m 750 -g htbb /etc/htbb-hlv-ai; install -m 640 -g htbb /dev/null /etc/htbb-hlv-ai/hlv-ai.cnf
            printf '[client]\nhost=%s\nport=%s\nuser=%s\npassword=%s\n' \"\$HTBB_DB_HOST\" \"\$HTBB_DB_PORT\" \"\$HTBB_DB_USER\" \"\$HTBB_DB_PASS\" > /etc/htbb-hlv-ai/hlv-ai.cnf
        fi
        systemctl enable htbb-hlv-ai >/dev/null 2>&1; systemctl restart --no-block htbb-hlv-ai
        for i in $(seq 1 60); do systemctl is-active htbb-hlv-ai | grep -q activating || break; sleep 10; done; sleep 30
        systemctl is-active htbb-hlv-ai; tail -3 /opt/htbb/logs/hlv-ai.out" 2>&1 | tee -a "$LOG" || loi "cai dich vu bot hong"
fi

if [ $DANGNHAP = 1 ]; then
    buoc "3e. Chuyen sang may chu dang nhap moi (htbb-login.jar)"
    $SCP server/loginServer/htbb-login.jar "$VPS:/opt/htbb/server/loginServer/" >>"$LOG" 2>&1 || loi "tai htbb-login.jar hong"
    $SSH "$VPS" "chown htbb:htbb /opt/htbb/server/loginServer/htbb-login.jar; rm -f /etc/htbb/login.env
        install -m 644 /opt/htbb/vps/htbb-login.service /etc/systemd/system/ && systemctl daemon-reload && systemctl restart htbb-login
        sleep 4; systemctl is-active htbb-login; pgrep -af 'htbb-login.jar|CheckLoginSocket' | cut -c1-100" | tee -a "$LOG"
    echo "   quay lai ban cu: $SSH $VPS 'echo HTBB_LOGIN_JAR=CheckLoginSocket.jar > /etc/htbb/login.env && systemctl restart htbb-login'" | tee -a "$LOG"
fi

buoc "XONG. Log day du: $LOG"
