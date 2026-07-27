#!/bin/bash
# Backup 2 database ra /opt/kpah/backup, giu 7 ngay gan nhat.
# Cai vao cron:  sudo crontab -e
#   0 4 * * * /opt/kpah/backup_db.sh
set -u

OUT=/opt/kpah/backup
NGAY=$(date +%F_%H%M)
mkdir -p "$OUT"

# Doc mat khau tu /root/.my.cnf (chmod 600) de khong de mat khau trong file nay:
#   [client]
#   user=root
#   password=MAT_KHAU
for DB in kpah_db account; do
    mysqldump --single-transaction --routines --events "$DB" | gzip > "$OUT/${DB}_${NGAY}.sql.gz"
done

# Xoa ban cu hon 7 ngay
find "$OUT" -name "*.sql.gz" -mtime +7 -delete

echo "$(date '+%F %T') backup xong -> $OUT"
