#!/bin/bash
# Sao luu hang ngay 2 CSDL htbb_db + account ra /opt/htbb/backups, giu 14 ngay.
#
# Cai (root):
#   install -m 750 /opt/htbb/vps/backup_db.sh /usr/local/sbin/htbb-backup-db.sh
#   /etc/cron.d/htbb-backup:
#     SHELL=/bin/bash
#     PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
#     30 3 * * * root /usr/local/sbin/htbb-backup-db.sh >> /var/log/htbb-backup.log 2>&1
#
# Dang nhap CSDL - KHONG can /root/.my.cnf:
#   1. Chay bang root va MariaDB root dung unix_socket (mac dinh Ubuntu) -> khong can mat khau.
#   2. Khong thi lay HTBB_DB_HOST/PORT/USER/PASS tu /etc/htbb/htbb.env, ghi ra mot tep cnf tam
#      0600 trong thu muc mktemp -d (xoa khi thoat) roi goi mysqldump --defaults-file. Mat khau
#      khong bao gio nam tren dong lenh (ps thay duoc) hay trong log.
#
# Ban sao NGOAI MAY (khuyen nghi): dat HTBB_BACKUP_DICH="user@may-khac:/duong/dan/" trong
# /etc/default/htbb-backup -> rsync qua ssh key. Trong = bo qua.
set -euo pipefail

OUT=${HTBB_BACKUP_DIR:-/opt/htbb/backups}
ENV_FILE=${HTBB_ENV_FILE:-/etc/htbb/htbb.env}
NGAY=$(date +%F_%H%M)
GIU_NGAY=14

loi() { echo "$(date '+%F %T') LOI: $*" >&2; exit 1; }

mkdir -p "$OUT"
chmod 750 "$OUT"
umask 077

TAM=$(mktemp -d)
trap 'rm -rf "$TAM"' EXIT

# Doc MOT khoa tu tep env (khong source: tep do co the chua ky tu shell dac biet).
doc_env() { sed -n -E "s/^$1=(.*)$/\1/p" "$ENV_FILE" | tail -n 1 | sed -E "s/^[\"']//; s/[\"']$//"; }

TUY_CHON=()
if [ "$(id -u)" = 0 ] && mysql -N -e "select 1" >/dev/null 2>&1; then
    : # root qua unix_socket
elif [ -r "$ENV_FILE" ] && [ -n "$(doc_env HTBB_DB_PASS)" ]; then
    cat > "$TAM/my.cnf" <<CNF
[client]
host=$(doc_env HTBB_DB_HOST || true)
port=$(doc_env HTBB_DB_PORT || true)
user=$(doc_env HTBB_DB_USER || true)
password=$(doc_env HTBB_DB_PASS)
CNF
    chmod 600 "$TAM/my.cnf"
    # Thieu host/port/user thi bo dong rong (mysqldump bao loi voi "host=").
    sed -i -E '/^(host|port|user)=$/d' "$TAM/my.cnf"
    # --defaults-file chu khong phai --defaults-extra-file: ~/.my.cnf duoc doc SAU extra-file va de len.
    TUY_CHON=(--defaults-file="$TAM/my.cnf")
else
    loi "khong dang nhap duoc CSDL: khong phai root qua unix_socket va khong doc duoc HTBB_DB_PASS trong $ENV_FILE"
fi

for DB in htbb_db account; do
    F="$OUT/${DB}_${NGAY}.sql.gz"

    # 'set -o pipefail' o tren la dong quan trong nhat ca script nay. Khong co no,
    # mysqldump chet giua chung VAN cho ra mot file .gz hop le (rong) va ma thoat lay
    # tu gzip = 0 -> script bao "backup xong" roi xuong duoi xoa cac ban cu.
    if ! mysqldump ${TUY_CHON[@]+"${TUY_CHON[@]}"} --single-transaction --routines --events --triggers "$DB" | gzip > "$F"; then
        rm -f "$F"
        loi "mysqldump $DB that bai - GIU NGUYEN cac ban cu"
    fi

    # Kiem tra that su thay vi doan theo kich thuoc: file gz phai nguyen ven, va
    # mysqldump phai ghi duoc dong "Dump completed" o cuoi.
    gzip -t "$F" 2>/dev/null || { rm -f "$F"; loi "$F hong (gzip -t that bai) - GIU NGUYEN cac ban cu"; }
    CUOI=$(zcat "$F" | tail -n 5)
    case "$CUOI" in
        *"Dump completed"*) ;;
        *) rm -f "$F"; loi "$DB dump khong co dong 'Dump completed' -> dut giua chung - GIU NGUYEN cac ban cu" ;;
    esac

    echo "$(date '+%F %T') $DB -> $F ($(stat -c %s "$F") byte)"
done

# Chi xoa ban cu SAU KHI ca hai ban moi deu da qua kiem tra o tren.
find "$OUT" -maxdepth 1 -name "*.sql.gz" -mtime +"$GIU_NGAY" -delete

[ -r /etc/default/htbb-backup ] && . /etc/default/htbb-backup
if [ -n "${HTBB_BACKUP_DICH:-}" ]; then
    rsync -a --timeout=120 "$OUT"/ "$HTBB_BACKUP_DICH" \
        && echo "$(date '+%F %T') da chep ra ngoai -> $HTBB_BACKUP_DICH" \
        || echo "$(date '+%F %T') CANH BAO: chep ra ngoai that bai (ban tai cho van con)" >&2
fi

echo "$(date '+%F %T') backup xong -> $OUT"
