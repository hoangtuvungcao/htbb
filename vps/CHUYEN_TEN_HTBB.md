# Chuyển VPS sang monorepo `htbb` + đổi tên KPAH → HTBB

Sổ tay chạy tay trên VPS (180.93.57.30). Kế hoạch tổng: `tools/doi_ten/KE_HOACH_DOI_TEN.md`.
Tệp này **cố ý** còn tên cũ (`kpah_db`, `/opt/kpah`, `kpah-game`...) vì nó mô tả việc chuyển từ tên cũ;
script đổi tên bỏ qua tệp này.

Ý chính: **không sửa `/opt/kpah`**. Dựng bản checkout monorepo mới ở `/opt/htbb` (game chạy ở `/opt/htbb/server`),
chép dữ liệu chạy sang, đổi dịch vụ trong MỘT cửa sổ bảo trì. `/opt/kpah` + `kpah_db` để nguyên làm đường lui.

| Cũ | Mới |
|---|---|
| `/opt/kpah` (clone `hoanghoa0903/KPAH`, game ở gốc) | `/opt/htbb` (clone monorepo, sparse: `server vps sql web/deploy`), game ở `/opt/htbb/server` |
| CSDL `kpah_db` | `htbb_db` (chép bằng mysqldump; `kpah_db` giữ tới giai đoạn 2) |
| `kpah-game/kpah-login/kpah-web.service` | `htbb-*.service` (+ `Alias=kpah-*.service`) |
| user/nhóm Linux `kpah`, `kpah-web` | `htbb`, `htbb-web` (đổi tên, giữ UID/GID) |
| `KPAH2_patched.jar`, `lib/NQSH_5h.jar` | `server/htbb-server.jar`, `server/lib/htbb-core.jar` |
| web PHP `/opt/kpah/web` | **bỏ** — website duy nhất là Nuxt (`/opt/htbb-web`, Apache chỉ làm reverse proxy) |
| `/opt/kpah-web`, `/etc/kpah-web.env`, `KPAH_*` | `/opt/htbb-web` (+ symlink tên cũ), `/etc/htbb-web.env`, `HTBB_*` (web vẫn đọc `KPAH_*` nếu thiếu) |
| logrotate `kpah`, `90-kpah.cnf`, `/etc/ssl/kpah`, conf Apache `kpah-*`, `/etc/default/kpah-backup` | tên `htbb` tương ứng |
| log/sao lưu `/opt/kpah/log`, `/opt/kpah/backup` | `/opt/htbb/log`, `/opt/htbb/backup` (gốc checkout, gitignore) |

**Không đổi** (bên ngoài): tên miền `kpah-private.site`, repo `kpah-server/nqsh-h2-tai`.
**Chú ý khi bỏ web PHP:** client Unity tải `http://<máy chủ>/server.txt` (trước là tệp web PHP `web/server/NQSH2.txt`).
Nay Nuxt trả `/server.txt` từ biến `HTBB_SERVER_LIST` (đọc `KPAH_SERVER_LIST` nếu thiếu; thiếu cả hai -> 503) trong
`/etc/htbb-web.env` — đặt ở 1.4 trước khi gỡ conf PHP. Mọi đường dẫn khác (kể cả `/NQSH2.txt`, link cũ `/kpah/...`) đi
qua `ProxyPass /` sang Nuxt. Lớp chặn `/admin`, whitelist IP, ép HTTPS là `<Location>` trong
`/etc/apache2/htbb-web-bao-ve.d/` (sinh bởi `vps/chi-cho-server-txt.sh`, `bat-https.sh`, `mat-khau-admin.sh`; thứ tự nạp
quan trọng — xem đầu `chi-cho-server-txt.sh`).
**Trước khi merge việc bỏ `web/` vào `main` của repo cũ** (deploy-auto.sh của `/opt/kpah` sẽ `git pull` và xoá luôn tệp PHP
đang chạy, kể cả `web/server/NQSH2.txt` = `/server.txt`): Nuxt phải đã chạy sau proxy và trả được `/server.txt` trên VPS.

---

## Giai đoạn 0 — trước cửa sổ

1. Tạo repo GitHub mới cho monorepo (lệnh ở `tools/doi_ten/KE_HOACH_DOI_TEN.md` mục 4), push `main` + `pokemon/chuyen-doi`.
   `server-deploy.yml` KHÔNG chạy khi chưa có biến `HTBB_VPS_MONOREPO=1` → push không đụng VPS.
2. Thêm khoá deploy của VPS vào repo mới (Settings → Deploy keys, quyền đọc):
   `ssh root@180.93.57.30 cat /root/.ssh/id_ed25519.pub` (khoá `git pull` của user `kpah` — xem `sudo -u kpah ssh -T git@github.com`).
3. Tạo secrets/variables cho repo mới: `VPS_SSH_KEY`, `DB_USER`, `DB_PASSWORD`, `DB_USER_NAP`, `DB_PASS_NAP`, `DB_USER1`,
   `DB_PASSWORD1` (server) và `VPS_SSH_KEY_WEB` + `HTBB_*` (web) — xem mục cuối.
4. Chép sẵn bản checkout (không ảnh hưởng server đang chạy):

```bash
R=git@github.com:hoanghoa0903/htbb.git          # tên repo mới
sudo -u kpah git clone --filter=blob:none --no-checkout -b main "$R" /opt/htbb
sudo -u kpah git -C /opt/htbb sparse-checkout set server vps sql web/deploy
sudo -u kpah git -C /opt/htbb checkout main
ls /opt/htbb/server/htbb-server.jar /opt/htbb/vps/htbb-game.service
```

## Giai đoạn 1 — cửa sổ bảo trì (~30 phút, báo trước người chơi)

Chạy bằng root. Bản sao lưu ở `/root/doi_ten_backup/`.

```bash
set -eu
B=/root/doi_ten_backup; mkdir -p $B
```

### 1.1 Sao lưu + dừng

```bash
/opt/kpah/vps/backup_db.sh
crontab -l > $B/crontab.root; cp -a /etc/systemd/system/kpah-*.service* $B/
cp -a /etc/kpah-web.env /etc/default/kpah-backup /root/.ssh/authorized_keys $B/ 2>/dev/null || true
tar czf $B/etc.tgz /etc/apache2 /etc/logrotate.d/kpah /etc/mysql/mariadb.conf.d /etc/ssl/kpah 2>/dev/null || true
crontab -l | sed 's#^\([^#].*/opt/kpah/\)#\#DOI_TEN \1#' | crontab -
systemctl stop kpah-web kpah-game kpah-login        # kpah-game: shutdown hook lưu nhân vật (tối đa 180s)
systemctl is-active kpah-game kpah-login kpah-web || true
```

### 1.2 CSDL `kpah_db` → `htbb_db` (giữ `kpah_db`)

```bash
read CS CO < <(mysql -N -u root -e "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='kpah_db'")
mysql -u root -e "CREATE DATABASE htbb_db CHARACTER SET $CS COLLATE $CO"
mysqldump -u root --no-data --routines --events --triggers kpah_db | grep -c 'kpah_db' || true   # thường 0; >0 thì xem tay
mysqldump -u root --single-transaction --quick --routines --events --triggers --default-character-set=utf8mb4 kpah_db \
  | mysql -u root --default-character-set=utf8mb4 htbb_db
mysql -N -u root -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='kpah_db' AND TABLE_TYPE='BASE TABLE'" |
while read t; do
  a=$(mysql -N -u root -e "SELECT COUNT(*) FROM kpah_db.\`$t\`"); b=$(mysql -N -u root -e "SELECT COUNT(*) FROM htbb_db.\`$t\`")
  [ "$a" = "$b" ] || echo "LECH $t: $a != $b"
done; echo "doi chieu xong"
mysql -u root -e "CHECKSUM TABLE kpah_db.tob_char, htbb_db.tob_char, kpah_db.cau_hinh, htbb_db.cau_hinh"
# quyền: ai có quyền trên kpah_db thì có y vậy trên htbb_db
mysql -N -u root -e "SELECT DISTINCT CONCAT('\`',User,'\`@\`',Host,'\`') FROM mysql.db WHERE Db IN ('kpah_db','kpah\\\\_db')" |
while read u; do
  mysql -N -u root -e "SHOW GRANTS FOR $u" | grep -E 'kpah(\\\\)?_db' |
    sed -E 's/`kpah(\\\\)?_db`/`htbb_db`/; s/$/;/' | tee -a $B/grants_htbb.sql | mysql -u root
done; mysql -u root -e "FLUSH PRIVILEGES"
```

### 1.3 User/nhóm Linux (giữ UID/GID)

```bash
grep -rn 'kpah' /etc/sudoers /etc/sudoers.d /var/spool/cron 2>/dev/null
usermod -l htbb kpah && groupmod -n htbb kpah
id kpah-web >/dev/null 2>&1 && usermod -l htbb-web kpah-web && groupmod -n htbb-web kpah-web
```

### 1.4 Dữ liệu chạy `/opt/kpah` → `/opt/htbb` (CHÉP, không di chuyển)

```bash
O=/opt/kpah; N=/opt/htbb
cp -a $O/log $N/log; cp -a $O/backup $N/backup 2>/dev/null || mkdir -p $N/backup
cp -a $O/loginServer/server.ini $N/server/loginServer/server.ini
for f in $O/*.txt $O/*.txt.* $O/trang_thai.json; do [ -e "$f" ] && cp -a "$f" $N/server/; done
cp -a $O/server.ini $N/server/server.ini          # sinh lại ở 1.6, bản này chỉ để đối chiếu
chown -R htbb:htbb $N
git config --global --add safe.directory /opt/htbb
sudo -u htbb git -C $N status --short | head        # phải sạch
mv /opt/kpah-web /opt/htbb-web && ln -s /opt/htbb-web /opt/kpah-web
[ -L /opt/htbb-web/current ] && ln -sfn "/opt/htbb-web/releases/$(basename "$(readlink /opt/htbb-web/current)")" /opt/htbb-web/current
mv /etc/kpah-web.env /etc/htbb-web.env && sed -i 's/^KPAH_/HTBB_/; s#/opt/kpah#/opt/htbb/server#g' /etc/htbb-web.env
grep -q '^HTBB_SERVER_DIR=' /etc/htbb-web.env || echo 'HTBB_SERVER_DIR="/opt/htbb/server"' >> /etc/htbb-web.env
# danh sách server cho client (/server.txt): trước là tệp web PHP web/server/NQSH2.txt -> biến của Nuxt
grep -q '^HTBB_SERVER_LIST=' /etc/htbb-web.env || printf 'HTBB_SERVER_LIST="%s"\n' "$(head -n1 $O/web/server/NQSH2.txt 2>/dev/null || echo 'Server 1:180.93.57.30:19129:0')" >> /etc/htbb-web.env
grep '^HTBB_SERVER_LIST=' /etc/htbb-web.env                 # phải ra "Server 1:<ip>:19129:0"
chown root:htbb-web /etc/htbb-web.env && chmod 640 /etc/htbb-web.env
[ -f /etc/default/kpah-backup ] && sed 's/^KPAH_/HTBB_/' /etc/default/kpah-backup > /etc/default/htbb-backup && chmod 600 /etc/default/htbb-backup
[ -d /etc/ssl/kpah ] && { mkdir -p /etc/ssl/htbb; cp -a /etc/ssl/kpah/kpah.crt /etc/ssl/htbb/htbb.crt; cp -a /etc/ssl/kpah/kpah.key /etc/ssl/htbb/htbb.key; chmod 600 /etc/ssl/htbb/htbb.key; }
[ -d /etc/kpah-cloudflare-web ] && cp -a /etc/kpah-cloudflare-web /etc/htbb-cloudflare-web
```

### 1.5 systemd (unit mới + alias tên cũ)

```bash
systemctl disable kpah-game kpah-login kpah-web 2>/dev/null || true
rm -f /etc/systemd/system/kpah-game.service /etc/systemd/system/kpah-login.service /etc/systemd/system/kpah-web.service
for d in /etc/systemd/system/kpah-*.service.d; do [ -d "$d" ] && mv "$d" "/etc/systemd/system/htbb-${d#/etc/systemd/system/kpah-}"; done
install -m 644 /opt/htbb/vps/htbb-game.service /opt/htbb/vps/htbb-login.service /opt/htbb/web/deploy/htbb-web.service /etc/systemd/system/
systemctl daemon-reload && systemctl enable htbb-login htbb-game htbb-web     # tạo alias kpah-*.service
systemctl cat kpah-game | head -3                                            # -> htbb-game.service, WorkingDirectory=/opt/htbb/server
install -m 755 /opt/htbb/web/deploy/deploy-web.sh /opt/htbb-web/deploy/deploy-web.sh
sed -i 's#/opt/kpah-web/deploy/deploy-web.sh#/opt/htbb-web/deploy/deploy-web.sh#; s#/opt/kpah/vps/deploy-auto.sh#/opt/htbb/vps/deploy-auto.sh#' /root/.ssh/authorized_keys
grep -o 'command="[^"]*"' /root/.ssh/authorized_keys
```

### 1.6 Chuyển CSDL + sinh `server.ini`

```bash
mkdir -p /etc/htbb && touch /etc/htbb/da-chuyen-csdl
bash /opt/htbb/vps/sinh-server-ini.sh; echo "ma=$?"            # 3 = đã sinh /opt/htbb/server/server.ini
grep -E '^db\.name|^sv\.pathLog' /opt/htbb/server/server.ini   # htbb_db; /opt/htbb/log/
```

### 1.7 logrotate, MariaDB, Apache

```bash
mv /etc/logrotate.d/kpah $B/ && install -m 644 -o root -g root /opt/htbb/vps/htbb-logrotate.conf /etc/logrotate.d/htbb
logrotate -d /etc/logrotate.d/htbb 2>&1 | tail -3
[ -f /etc/mysql/mariadb.conf.d/90-kpah.cnf ] && mv /etc/mysql/mariadb.conf.d/90-kpah.cnf /etc/mysql/mariadb.conf.d/90-htbb.cnf
# Web PHP cũ BỎ: Apache chỉ còn reverse proxy sang Nuxt (127.0.0.1:3000). Gỡ MỌI DocumentRoot/<Directory> /opt/kpah/web
# (đường cũ vẫn còn trên đĩa -> để lại là lộ trang PHP cũ + lách được lớp chặn /admin).
a2disconf kpah-admin-https kpah-admin-basic-auth kpah-chan-tep-lo kpah-webroot kpah-server-txt-only kpah-web-proxy kpah-web-bao-ve kpah-tai 2>/dev/null || true
a2dissite kpah-ssl 2>/dev/null || true
grep -rln 'DocumentRoot /opt/kpah/web' /etc/apache2/sites-available | xargs -r sed -i 's#DocumentRoot /opt/kpah/web#DocumentRoot /var/www/html#'
grep -rln '/opt/kpah/web\|/var/www/html/kpah' /etc/apache2/sites-enabled /etc/apache2/conf-enabled || echo "het conf PHP"   # con thi sua tay
rm -f /var/www/html/kpah /var/www/html/NQSH2.txt
a2dismod php7.4 php8.1 php8.3 2>/dev/null || true          # không còn trang PHP nào
a2enmod proxy proxy_http headers remoteip >/dev/null
install -m 644 /opt/htbb/web/deploy/apache-htbb-web.conf /etc/apache2/conf-available/htbb-web-proxy.conf
a2enconf htbb-web-proxy
# Lớp bảo vệ cho Nuxt (<Location>, /etc/apache2/htbb-web-bao-ve.d/). chi-cho-server-txt.sh chạy ở 1.8 (tự kiểm cần Nuxt chạy).
/opt/htbb/vps/bat-https.sh [ten.mien]; /opt/htbb/vps/mat-khau-admin.sh
apachectl configtest && systemctl reload apache2
grep -rn '/opt/kpah\|kpah-' /etc/apache2/sites-enabled /etc/apache2/conf-enabled || echo "apache sach"
```

### 1.8 Cron, bật, kiểm

```bash
crontab -l | sed 's#^\#DOI_TEN ##; s#/opt/kpah/#/opt/htbb/#g' | crontab - && crontab -l | grep htbb
systemctl start htbb-login htbb-game htbb-web
sleep 20; systemctl is-active htbb-login htbb-game htbb-web
ss -lntp | grep -E ':(8023|19129|3000) '
/opt/htbb/vps/chi-cho-server-txt.sh       # whitelist IP <Location />; tự kiểm /server.txt=200, /=403
tail -n 80 /opt/htbb/log/game.out | grep -iE 'exception|access denied|listen on port'
curl -s -o /dev/null -w "api/site %{http_code}\n" http://127.0.0.1:3000/api/site
curl -s -o /dev/null -w "admin %{http_code}\n" http://127.0.0.1/admin/                # 301/401, KHÔNG 200
curl -s http://127.0.0.1/server.txt                                                  # "Server 1:...:19129:0"
sudo /opt/htbb/vps/deploy-auto.sh        # "Cau hinh khong doi", khong restart
mysql -u root -e "SELECT COUNT(*), MAX(id) FROM htbb_db.tob_char"
```

Đăng nhập thử trong game + trang admin web. Rồi bật deploy tự động: biến repo `HTBB_VPS_MONOREPO=1`
(`gh variable set HTBB_VPS_MONOREPO --body 1 -R hoanghoa0903/htbb`).

## Giai đoạn 2 — dọn (sau ≥ 7 ngày ổn)

```bash
/opt/htbb/vps/backup_db.sh
mysqldump -u root --single-transaction kpah_db | gzip > /root/kpah_db_cuoi_cung.sql.gz && mysql -u root -e "DROP DATABASE kpah_db"
tar czf /root/opt_kpah_cuoi_cung.tgz /opt/kpah && rm -rf /opt/kpah
rm -f /opt/kpah-web; rm -rf /etc/ssl/kpah /etc/default/kpah-backup /etc/kpah-cloudflare-web
```

Trong repo: xoá các dòng `TUONG-THICH-TEN-CU` (`git grep -n TUONG-THICH-TEN-CU`) và `Alias=kpah-*.service`, rồi
`systemctl reenable htbb-game htbb-login htbb-web`. Xoá secrets/variables `KPAH_*` của repo web cũ.

## Giai đoạn 3 (tuỳ chọn) — user CSDL `kpah` → `htbb`

```bash
mysql -u root -e "CREATE USER 'htbb'@'localhost' IDENTIFIED BY '<mat-khau-moi>';
  GRANT ALL PRIVILEGES ON htbb_db.* TO 'htbb'@'localhost'; GRANT SELECT,INSERT,UPDATE,DELETE ON account.* TO 'htbb'@'localhost';"
```

Đổi secrets `DB_USER/DB_PASSWORD`, `DB_USER_NAP/DB_PASS_NAP`, `DB_USER1/DB_PASSWORD1` + `/opt/htbb/server/loginServer/server.ini`,
deploy, kiểm, rồi `DROP USER 'kpah'@'localhost'` (xem `SHOW GRANTS FOR 'kpah'@'localhost'` để cấp đúng như cũ).

## Quay lại (trong giai đoạn 1)

`/opt/kpah` và `kpah_db` không bị đụng. Nếu đã có người chơi sau giờ chuyển, chép ngược `htbb_db` → `kpah_db` trước.

```bash
systemctl stop htbb-web htbb-game htbb-login; systemctl disable htbb-game htbb-login htbb-web
rm -f /etc/systemd/system/htbb-*.service && cp -a $B/kpah-*.service* /etc/systemd/system/
usermod -l kpah htbb && groupmod -n kpah htbb
id htbb-web >/dev/null 2>&1 && usermod -l kpah-web htbb-web && groupmod -n kpah-web htbb-web
rm /opt/kpah-web && mv /opt/htbb-web /opt/kpah-web; mv /etc/htbb-web.env /etc/kpah-web.env
cp -a $B/authorized_keys /root/.ssh/authorized_keys; crontab $B/crontab.root
tar xzf $B/etc.tgz -C / && ln -sfn /opt/kpah/web /var/www/html/kpah && apachectl configtest && systemctl reload apache2
systemctl daemon-reload && systemctl enable --now kpah-login kpah-game kpah-web
gh variable delete HTBB_VPS_MONOREPO -R hoanghoa0903/htbb        # chặn deploy tự động vào /opt/htbb
```
