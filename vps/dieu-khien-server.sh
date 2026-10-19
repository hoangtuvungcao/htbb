#!/bin/bash
# ============================================================
#  DIEU KHIEN BAT/TAT SERVER GAME TU WEB ADMIN
#
#  Web admin (/admin/he-thong, khoi Bat/tat) khong SSH/exec duoc vao VPS - no chi GHI
#  "trang thai mong muon" vao bang dieu_khien_server (htbb_db). Script nay la
#  canh gac CHAY NGOAI JVM game, doc bang do roi tu goi systemctl thay ban.
#  Ly do bat buoc phai co canh gac rieng (khong the lam trong Java server):
#  khi server dang TAT HAN thi khong co gi trong JVM de ma poll DB nua - can
#  mot tien trinh song doc lap voi chinh server.
#
#  BAT BUOC chay bang CRON CUA ROOT (htbb-game.service chay bang user "htbb",
#  khong phai root - xem vps/htbb-game.service User=htbb - nen chi root moi
#  co quyen goi systemctl start/stop no).
#
#  Cai dat (mot lan):
#      sudo crontab -e
#      * * * * * /opt/htbb/vps/dieu-khien-server.sh >>/opt/htbb/logs/dieu-khien-server.log 2>&1
#
#  Chay thu bang tay de kiem tra:
#      sudo /opt/htbb/vps/dieu-khien-server.sh
#
#  AN TOAN: script CHI goi dung hai lenh co dinh "systemctl start htbb-game"
#  va "systemctl stop htbb-game" - ten service KHONG BAO GIO duoc ghep tu du
#  lieu doc trong database. Bang dieu_khien_server chi chua co/khong (0/1) va
#  mot moc gio, khong chua chuoi lenh nao ca.
# ============================================================
set -u

DIR=/opt/htbb
INI=$DIR/server/server.ini
SERVICE=htbb-game

# Khong cho hai lan chay chong nhau (cron moi phut, phong khi mot lan bi treo).
exec 9>/tmp/htbb-dieu-khien-server.lock
if ! flock -n 9; then
    echo "$(date '+%F %T') co mot lan dang chay, bo qua lan nay"
    exit 0
fi

if [ ! -r "$INI" ]; then
    echo "$(date '+%F %T') [LOI] khong doc duoc $INI - dung"
    exit 1
fi

# Lay thong tin database tu chinh file cau hinh cua server, giong don-log.sh -
# khong chep tay sang day de tranh som muon lech nhau.
DB_USER=$(sed -n 's/^db\.user=//p'     "$INI" | tr -d '\r' | head -1)
DB_PASS=$(sed -n 's/^db\.password=//p' "$INI" | tr -d '\r' | head -1)
DB_NAME=$(sed -n 's/^db\.name=//p'     "$INI" | tr -d '\r' | head -1)

if [ -z "$DB_USER" ] || [ -z "$DB_NAME" ]; then
    echo "$(date '+%F %T') [LOI] $INI thieu db.user hoac db.name - dung"
    exit 1
fi

export MYSQL_PWD="$DB_PASS"
sql() { mysql -u"$DB_USER" -N -B "$DB_NAME" -e "$1" 2>&1; }

# Bang chua co (chua chay sql/nang_cap.sql) thi im lang bo
# qua - KHONG coi la loi nghiem trong, vi day la lan dau cai dat watcher truoc
# khi ai kip tao bang.
CO_BANG=$(sql "SELECT COUNT(*) FROM information_schema.TABLES
               WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='dieu_khien_server';")
if [ "$CO_BANG" != "1" ]; then
    echo "$(date '+%F %T') chua co bang dieu_khien_server, bo qua (chay sql/nang_cap.sql truoc)"
    exit 0
fi

# Mot dong duy nhat: muon_chay (0/1), da_ap_dung (0/1), den_han (1 = khong hen
# gio HOAC da qua gio hen). So sanh gio hen ngay trong SQL cho chac chan cung
# mui gio voi may chu MySQL, khong tu tinh bang bash.
HANG=$(sql "SELECT muon_chay, da_ap_dung, (gio_hen IS NULL OR gio_hen <= NOW())
            FROM dieu_khien_server WHERE id=1;")
if [ -z "$HANG" ]; then
    echo "$(date '+%F %T') [LOI] bang dieu_khien_server rong (thieu dong id=1) - dung"
    exit 1
fi
read -r MUON_CHAY DA_AP_DUNG DEN_HAN <<< "$HANG"

TRANG_THAI=$(systemctl is-active "$SERVICE" 2>/dev/null || true)

# Da xu ly xong tu lan truoc, khong co gi moi phai lam - chi cap nhat nhip tim
# de trang web biet watcher con song.
if [ "$DA_AP_DUNG" = "1" ]; then
    sql "UPDATE dieu_khien_server SET trang_thai_thuc_te='$TRANG_THAI', lan_kiem_cuoi=NOW() WHERE id=1;" >/dev/null
    exit 0
fi

# Co yeu cau dang cho (da_ap_dung=0) nhung chua den gio hen - chi cap nhat
# nhip tim, GIU NGUYEN da_ap_dung=0 de lan sau con kiem tra tiep.
if [ "$DEN_HAN" != "1" ]; then
    sql "UPDATE dieu_khien_server SET trang_thai_thuc_te='$TRANG_THAI', lan_kiem_cuoi=NOW() WHERE id=1;" >/dev/null
    exit 0
fi

# Den luc xu ly: so sanh trang thai muon voi trang thai thuc, chi goi lenh
# khi that su can doi (tranh goi systemctl vo ich moi phut).
if [ "$MUON_CHAY" = "1" ] && [ "$TRANG_THAI" != "active" ]; then
    echo "$(date '+%F %T') bat $SERVICE (dang '$TRANG_THAI')"
    systemctl start "$SERVICE"
elif [ "$MUON_CHAY" = "0" ] && [ "$TRANG_THAI" = "active" ]; then
    echo "$(date '+%F %T') tat $SERVICE (dang '$TRANG_THAI')"
    systemctl stop "$SERVICE"
else
    echo "$(date '+%F %T') khong can lam gi, $SERVICE da dung trang thai muon (muon_chay=$MUON_CHAY, thuc te=$TRANG_THAI)"
fi

TRANG_THAI_MOI=$(systemctl is-active "$SERVICE" 2>/dev/null || true)
sql "UPDATE dieu_khien_server SET da_ap_dung=1, trang_thai_thuc_te='$TRANG_THAI_MOI', lan_kiem_cuoi=NOW() WHERE id=1;" >/dev/null
