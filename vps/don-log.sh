#!/bin/bash
# ============================================================
#  DON LOG DINH KY - giu 30 ngay gan nhat
#
#  Chay thu (khong xoa gi, chi bao se xoa bao nhieu):
#      sudo /opt/htbb/vps/don-log.sh --thu
#
#  Chay that:
#      sudo /opt/htbb/vps/don-log.sh
#
#  Cai vao cron cho chay 03:20 moi dem:
#      sudo crontab -e
#      20 3 * * * /opt/htbb/vps/don-log.sh >/dev/null 2>&1
#
#  Chay luc 03:20 la co y: BaoTriDaily trong server tu tat va khoi dong lai o
#  mot moc NGAU NHIEN giua 04:30-05:30 (xem src/real/BaoTriDaily.java). Don
#  truoc do thi khong dam vao luc server dang luu du lieu.
#
#  File .txt va game.out KHONG do script nay don - logrotate lo, xem
#  vps/htbb-logrotate.conf. O day chi don thu logrotate khong voi toi duoc:
#  bang trong database, va file rac dat ten theo IP.
# ============================================================
set -u

# ---------- Cau hinh ----------

# So ngay giu lai. Dong nao cu hon moc nay thi xoa.
GIU_NGAY=${GIU_NGAY:-15}

# Xoa theo lot, moi lot bao nhieu dong.
#
# BAT BUOC phai chia lot: tob_log_all_item la MyISAM, ma MyISAM KHOA CA BANG
# khi xoa. Mot lenh DELETE quet 4 trieu dong se giu khoa hang phut - trong
# luc do moi INSERT log cua server xep hang cho, hang doi Logdata.query phinh
# to, va game khuc khich. Chia 20.000 dong mot lot, nghi giua cac lot, thi
# moi lot chi khoa duoi mot giay.
LO=${LO:-20000}
NGHI=${NGHI:-0.4}

# Tran an toan: du chua xoa het cung dung lai, de khong chiem may qua lau.
TOI_DA_LOT=${TOI_DA_LOT:-600}

DIR=/opt/htbb
INI=$DIR/server/server.ini
LOG=$DIR/log/don-log.log

THU=0
[ "${1:-}" = "--thu" ] && THU=1

# ---------- Chuan bi ----------

mkdir -p "$DIR/log"
exec > >(tee -a "$LOG") 2>&1

echo
echo "===== $(date '+%F %T') don log (giu $GIU_NGAY ngay)$([ $THU = 1 ] && echo ' [CHAY THU]')"

# Khong cho hai lan chay chong nhau.
exec 9>/tmp/htbb-don-log.lock
if ! flock -n 9; then
    echo "  co mot lan don khac dang chay, bo qua lan nay"
    exit 0
fi

if [ ! -r "$INI" ]; then
    echo "  [LOI] khong doc duoc $INI - dung"
    exit 1
fi

# Lay thong tin database tu chinh file cau hinh cua server, khong chep tay
# sang day. Chep tay la som muon cung lech nhau.
DB_USER=$(sed -n 's/^db\.user=//p'     "$INI" | tr -d '\r' | head -1)
DB_PASS=$(sed -n 's/^db\.password=//p' "$INI" | tr -d '\r' | head -1)
DB_NAME=$(sed -n 's/^db\.name=//p'     "$INI" | tr -d '\r' | head -1)

if [ -z "$DB_USER" ] || [ -z "$DB_NAME" ]; then
    echo "  [LOI] $INI thieu db.user hoac db.name - dung"
    exit 1
fi

export MYSQL_PWD="$DB_PASS"
sql() { mysql -u"$DB_USER" -N -B "$DB_NAME" -e "$1" 2>&1; }

if ! sql "SELECT 1" >/dev/null 2>&1; then
    echo "  [LOI] khong ket noi duoc database voi tai khoan '$DB_USER' - dung"
    exit 1
fi

MOC="$(date -d "-$GIU_NGAY days" '+%F %T')"
echo "  moc thoi gian: dong nao cu hon $MOC thi xoa"

# ---------- 1. Cac bang nhat ky co cot thoi gian ----------
#
# Cap "bang cot_thoi_gian". Bang nao khong co cot thoi gian thi KHONG the don
# theo tuoi - khong dua vao day (giftcode_log la vi du: chi co ten nhan vat va
# ma, xoa theo tuoi khong duoc, ma cung chi vai tram dong).

BANG_LOG="
tob_log_all_item   timelog
tob_log_all_gem    timelog
tob_log_buy_shop   timeBuy
tob_log_buy_store  timeBuy
tob_log_xoso       timelog
tob_log_clan       timeLog
web_admin_log      thoi_gian
web_chan_spam      thoi_gian
history_win_xoso   time_win
"

echo "$BANG_LOG" | while read -r BANG COT; do
    [ -z "$BANG" ] && continue

    CO=$(sql "SELECT COUNT(*) FROM information_schema.TABLES
              WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='$BANG';")
    [ "$CO" = "1" ] || { echo "  - $BANG: khong co bang nay, bo qua"; continue; }

    CAN=$(sql "SELECT COUNT(*) FROM \`$BANG\` WHERE \`$COT\` < '$MOC';")
    case "$CAN" in ''|*[!0-9]*) echo "  - $BANG: khong dem duoc ($CAN)"; continue;; esac

    if [ "$CAN" = "0" ]; then
        echo "  - $BANG: khong co dong nao qua han"
        continue
    fi

    if [ $THU = 1 ]; then
        echo "  - $BANG: SE XOA $CAN dong"
        continue
    fi

    echo "  - $BANG: xoa $CAN dong theo lot $LO..."
    DA=0
    for _ in $(seq 1 "$TOI_DA_LOT"); do
        N=$(sql "DELETE FROM \`$BANG\` WHERE \`$COT\` < '$MOC' LIMIT $LO; SELECT ROW_COUNT();" | tail -1)
        case "$N" in ''|*[!0-9]*) echo "      [LOI] $N"; break;; esac
        [ "$N" = "0" ] && break
        DA=$((DA + N))
        sleep "$NGHI"
    done
    echo "      da xoa $DA dong"
done

# ---------- 1b. Cac loai log KHONG CON GHI - xoa not dong cu, khong cho qua han ----------
#
# Tu 02/09/2026 server CHI ghi vao tob_log_all_item nhung aclog nam trong danh sach
# giu (xem src/data/LocLogVatPham.java - danh sach o do la ban goc, day chi la ban
# chep; test/KiemLocLogVatPham.java ep hai ben phai khop). Moi dong KHONG thuoc danh
# sach giu la rac cu (rieng luckGOLD 3,2 trieu dong trong mot ngay) - xoa theo lo.
LOAI_GIU="matdo trade sitemu delI timeout inven2tui tui2inven uwea uride XOAPET XOATC itemtrungpet tichthu tichthuchoi logchoi thugay downlv caitaocaitrang hoahinhpet loiinvent loisave hackc hackR hackbitem hackchonchar hcm ngoairange ngoairange1 stGive ffbug hklbug scamchat iftrung naptien nhanlk mocNap mocnap nhanluong nhanxu logxu autosave lenlv lenlevel putGiftCode gcode quatop quatopct top100 tophl thuongchiemthanh salaryC salaryCN txs xoso idruongmm taothankhi nangcapthankhi chetaotrangbi hopnguyenlieu khanglan truxu truthieu tratieu motieu thaden petbachthu longdenrong2026 linhnguyettruong2026 doiNLT lansutu2026 tiendan2026"

DS=""
for L in $LOAI_GIU; do DS="$DS${DS:+,}'$L'"; done
CO=$(sql "SELECT COUNT(*) FROM information_schema.TABLES
          WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='tob_log_all_item';")
if [ "$CO" = "1" ]; then
    CAN=$(sql "SELECT COUNT(*) FROM tob_log_all_item WHERE aclog NOT IN ($DS);")
    case "$CAN" in
        ''|*[!0-9]*) echo "  - loai khong con ghi: khong dem duoc ($CAN)";;
        0) echo "  - loai khong con ghi: khong con dong nao";;
        *)
            if [ $THU = 1 ]; then
                echo "  - loai khong con ghi (ngoai danh sach giu): SE XOA $CAN dong"
            else
                echo "  - loai khong con ghi (ngoai danh sach giu): xoa $CAN dong theo lot $LO..."
                DA=0
                for _ in $(seq 1 "$TOI_DA_LOT"); do
                    N=$(sql "DELETE FROM tob_log_all_item WHERE aclog NOT IN ($DS) LIMIT $LO; SELECT ROW_COUNT();" | tail -1)
                    case "$N" in ''|*[!0-9]*) echo "      [LOI] $N"; break;; esac
                    [ "$N" = "0" ] && break
                    DA=$((DA + N))
                    sleep "$NGHI"
                done
                echo "      da xoa $DA dong"
            fi;;
    esac
fi

# ---------- 2. Bang thong ke luong theo tuan (tob_log_use_luong_<tuan>_<thang>_<nam>) ----------
#
# Database.saveLogThongKeChiTieu ghi vao mot bang MOI moi tuan. Cac bang cu
# khong ai xoa - sau mot nam la 52 bang rac. Chung khong co cot thoi gian nen
# don theo UPDATE_TIME ma MyISAM tu ghi lai.
#
# Chi dong bang CO HAU TO (tob_log_use_luong_...), giu nguyen bang goc
# "tob_log_use_luong" khong hau to.

BANG_CU=$(sql "SELECT TABLE_NAME FROM information_schema.TABLES
               WHERE TABLE_SCHEMA='$DB_NAME'
                 AND TABLE_NAME LIKE 'tob\\_log\\_use\\_luong\\_%'
                 AND COALESCE(UPDATE_TIME, CREATE_TIME) < '$MOC';")

if [ -z "$BANG_CU" ]; then
    echo "  - bang thong ke luong theo tuan: khong co bang nao qua han"
else
    for B in $BANG_CU; do
        if [ $THU = 1 ]; then
            echo "  - SE DONG bang $B"
        else
            echo "  - dong bang $B"
            sql "DROP TABLE IF EXISTS \`$B\`;" >/dev/null
        fi
    done
fi

# ---------- 3. Doi lai cho o dia cua MyISAM ----------
#
# MyISAM xoa dong xong KHONG tra lai cho o dia - no de lai lo hong trong .MYD
# de lan insert sau lap vao. Bang van insert deu thi lo hong duoc tai su dung,
# nhung sau mot lan don lon (hang trieu dong) thi lo hong nhieu hon phan con
# dung, luc do phai OPTIMIZE moi thu nho file lai.
#
# OPTIMIZE KHOA CA BANG trong luc dung lai file, voi bang vai tram MB co the
# mat vai chuc giay. Chi lam khi that su du cho trong - nguong 200 MB.

NGUONG_MB=200
sql "SELECT TABLE_NAME, ROUND(DATA_FREE/1024/1024) FROM information_schema.TABLES
     WHERE TABLE_SCHEMA='$DB_NAME' AND ENGINE='MyISAM'
       AND DATA_FREE > $NGUONG_MB*1024*1024;" | while read -r B MB; do
    [ -z "$B" ] && continue
    if [ $THU = 1 ]; then
        echo "  - SE OPTIMIZE $B (dang bo trong ${MB} MB)"
    else
        echo "  - optimize $B (dang bo trong ${MB} MB), khoa bang trong luc nay..."
        T0=$(date +%s)
        sql "OPTIMIZE TABLE \`$B\`;" >/dev/null
        echo "      xong sau $(( $(date +%s) - T0 ))s"
    fi
done

# ---------- 4. File .txt le dat ten dong o thu muc goc ----------
#
# Hai cho trong server dat TEN FILE THEO DU LIEU CHAY, nen khong the liet ke
# truoc trong logrotate:
#
#   DatabaseNhanVat.java:770   Logger.logError(ex, p.charname + ".txt")
#       Moi lan LUU NHAN VAT THAT BAI o duong luu chinh, server de lai mot file
#       mang dung ten nhan vat ngay tai /opt/htbb. Bao nhieu nhan vat gap loi la
#       bay nhieu file. (Luu y: savechar.txt chi phu duong luu TU DONG, khong
#       phai duong nay.)
#
#   Database.java:11691        Logger.logString(result, fileName + ".txt")
#       Ket qua quet gem tu AdminPanel -> check_gem.txt
#
# Ngoai ra con file dat ten theo IP ket noi (vd 42.119.73.152.txt) - da co
# trong .gitignore vi chung lam "git status" ban va chan deploy-auto.sh.
#
# Cach quet: lay MOI *.txt o thu muc goc qua han, TRU hai nhom duoi day. Dao
# nguoc nhu vay thi ten dong moi nao cung tu dong duoc gom, khong phai sua
# script moi lan.

# (a) Server DOC NGUOC LAI nhu du lieu dau vao - xoa la mat du lieu.
BAO_VE="trangan.txt offline.txt top.txt"

# (b) logrotate da lo roi (xem vps/htbb-logrotate.conf) - de no tu xoay vong,
#     dung o day xoa tranh phan.
LOGROTATE_LO="hack.txt hackdo.txt errorlog.txt errorlogPet.txt errorAnima.txt errorSellITem.txt errorquayso.txt
savechar.txt saitenchar4.txt update.txt updateleaf.txt rUpdateleaf.txt
matketinfo.txt nhantieu.txt doitieu.txt thetieu.txt tratieu.txt cuoptieu.txt
longden.txt longdenchar.txt longdendaylog.txt datra4.txt charWedding.txt
AdminChat.txt logtopluong.txt"

BO_QUA=""
for T in $BAO_VE $LOGROTATE_LO; do
    BO_QUA="$BO_QUA ! -name $T"
done

# shellcheck disable=SC2086
LE=$(find "$DIR" -maxdepth 1 -type f -name '*.txt' -mtime "+$GIU_NGAY" $BO_QUA 2>/dev/null)
SO_FILE=$(printf '%s\n' "$LE" | grep -c . || true)

if [ "$SO_FILE" -gt 0 ]; then
    echo "$LE" | sed 's|^|      |' | head -20
    [ "$SO_FILE" -gt 20 ] && echo "      ... va $((SO_FILE - 20)) file nua"
    if [ $THU = 1 ]; then
        echo "  - SE XOA $SO_FILE file .txt le cu hon $GIU_NGAY ngay"
    else
        printf '%s\n' "$LE" | while read -r F; do [ -n "$F" ] && rm -f "$F"; done
        echo "  - da xoa $SO_FILE file .txt le"
    fi
else
    echo "  - file .txt le: khong co file nao qua han"
fi

# ---------- 5. Ban sao luu database qua han ----------
# vps/backup_db.sh da tu xoa ban cu hon 7 ngay, nhung no chi lam duoc khi
# chinh no chay. Neu cron cua no bi go ra thi thu muc backup nam do mai.

if [ -d "$DIR/backup" ]; then
    SO_BK=$(find "$DIR/backup" -name '*.sql.gz' -mtime +7 2>/dev/null | wc -l)
    if [ "$SO_BK" -gt 0 ]; then
        if [ $THU = 1 ]; then
            echo "  - SE XOA $SO_BK ban sao luu cu hon 7 ngay"
        else
            find "$DIR/backup" -name '*.sql.gz' -mtime +7 -delete
            echo "  - da xoa $SO_BK ban sao luu cu"
        fi
    fi
fi

# ---------- Tong ket ----------

echo "  --- con lai ---"
sql "SELECT CONCAT('  ', TABLE_NAME, ': ', FORMAT(TABLE_ROWS,0), ' dong, ',
            ROUND((DATA_LENGTH+INDEX_LENGTH)/1024/1024,1), ' MB')
     FROM information_schema.TABLES
     WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME LIKE '%log%'
     ORDER BY DATA_LENGTH+INDEX_LENGTH DESC LIMIT 5;"
echo "  dia: $(df -h / | awk 'NR==2{print $4" trong / "$2}')"
echo "===== $(date '+%F %T') xong"
