#!/bin/bash
# Backup 2 database ra /opt/kpah/backup, giu 7 ngay gan nhat.
#
# Cai vao cron cua root:  sudo crontab -e
#   0 4 * * * /opt/kpah/vps/backup_db.sh >> /opt/kpah/log/backup.log 2>&1
#
# Duong dan la /opt/kpah/vps/backup_db.sh chu KHONG phai /opt/kpah/backup_db.sh:
# deploy chep nguyen thu muc vps/ sang, khong bo file ra ngoai goc.
#
# Mat khau doc tu /root/.my.cnf (chmod 600), KHONG de trong file nay:
#   [client]
#   user=root
#   password=MAT_KHAU
set -euo pipefail

OUT=/opt/kpah/backup
NGAY=$(date +%F_%H%M)

loi() { echo "$(date '+%F %T') LOI: $*" >&2; exit 1; }

[ -r /root/.my.cnf ] || loi "khong doc duoc /root/.my.cnf -> mysqldump khong co mat khau"
mkdir -p "$OUT"

for DB in kpah_db account; do
    F="$OUT/${DB}_${NGAY}.sql.gz"

    # 'set -o pipefail' o tren la dong quan trong nhat ca script nay. Khong co no,
    # mysqldump chet giua chung VAN cho ra mot file .gz hop le (rong) va ma thoat lay
    # tu gzip = 0 -> script bao "backup xong" roi xuong duoi xoa cac ban cu hon 7 ngay.
    # Sau mot tuan la khong con ban nao dung ca, ma khong ai biet.
    if ! mysqldump --single-transaction --routines --events "$DB" | gzip > "$F"; then
        rm -f "$F"
        loi "mysqldump $DB that bai - GIU NGUYEN cac ban cu"
    fi

    # Kiem tra that su thay vi doan theo kich thuoc: file gz phai nguyen ven, va
    # mysqldump phai ghi duoc dong "Dump completed" o cuoi. CSDL account rat nho nen
    # nguong theo byte se bao dong gia; hai phep nay dung cho ca CSDL rong.
    gzip -t "$F" 2>/dev/null || { rm -f "$F"; loi "$F hong (gzip -t that bai) - GIU NGUYEN cac ban cu"; }
    CUOI=$(zcat "$F" | tail -5)
    case "$CUOI" in
        *"Dump completed"*) ;;
        *) rm -f "$F"; loi "$DB dump khong co dong 'Dump completed' -> dut giua chung - GIU NGUYEN cac ban cu" ;;
    esac

    echo "$(date '+%F %T') $DB -> $F ($(stat -c %s "$F") byte)"
done

# Chi xoa ban cu SAU KHI ca hai ban moi deu da qua kiem tra o tren.
find "$OUT" -name "*.sql.gz" -mtime +7 -delete

echo "$(date '+%F %T') backup xong -> $OUT"
