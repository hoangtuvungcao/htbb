#!/bin/sh
# Chay MOT LAN tren may Windows nay (Git Bash) de mo https://nqsh-h2.top/tai/ tren VPS:
#   sh tools/phat-hanh/cai-dat-vps.sh
# Tao /var/www/tai, chep htbb-tai.conf vao Apache, kiem tra cau hinh roi moi reload.
set -e
cd "$(dirname "$0")"
VPS=${VPS:-root@180.93.127.26}

scp -q htbb-tai.conf "$VPS:/etc/apache2/conf-available/htbb-tai.conf"
ssh "$VPS" 'set -e
mkdir -p /var/www/tai
[ -f /var/www/tai/phienban.json ] || echo "{}" > /var/www/tai/phienban.json
a2enmod headers >/dev/null
a2enconf htbb-tai >/dev/null
apachectl configtest
systemctl reload apache2'
sleep 2
echo "Kiem tra:"
curl -s -A Mozilla/5.0 https://nqsh-h2.top/tai/phienban.json; echo
curl -s -o /dev/null -w "Trang chu: %{http_code}\n" -A Mozilla/5.0 https://nqsh-h2.top/
