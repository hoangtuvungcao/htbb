# Triển khai lên VPS

Dựng và cập nhật máy chủ thật: game server, login server, MariaDB, website, nginx sau WAF. Vận hành hằng ngày (log,
sao lưu, bảo trì, GM): [van-hanh.md](van-hanh.md). Khoá cấu hình: [cau-hinh.md](cau-hinh.md). Tệp cài đặt nằm trong
[vps/](../vps/README.md) và `web/deploy/`.

Tài liệu này không ghi địa chỉ IP của VPS hay của WAF, không ghi mật khẩu: các giá trị đó chỉ nằm trên VPS
(`/etc/htbb/htbb.env`, `/etc/nginx/snippets/htbb-waf-ip.conf`) và trong GitHub Secrets / Variables.

## 1. Sơ đồ

```
Người chơi (jar / APK / PC)  ── TCP 19129 ──►  WAF / Nova agent ──►  htbb-game (Java, /opt/htbb/server)
                                                                         │ 127.0.0.1:8023
                                                                         ├──► htbb-login (CheckLoginSocket.jar)
                                                                         └──► MariaDB 127.0.0.1:3306 (htbb_db, account)
Trình duyệt ── HTTPS ──► WAF ──► nginx 80/443 ──► htbb-web (Nuxt, 127.0.0.1:3000) ──► MariaDB
                                       └── /downloads/ ──► /opt/htbb/downloads (bản tải game)
```

Tên miền: `https://huyenthoaibaoboi.fun/`. Website và game server dùng chung CSDL; web đọc `server.ini` của game để
lấy thông tin CSDL nếu không có `HTBB_DB_*`.

| Dịch vụ | Cổng | Ghi chú |
|---|---|---|
| `htbb-game` | TCP 19129 (mở ra ngoài) | `-Xms768m -Xmx1536m -Xss256k`, G1, `Restart=always`, `StartLimitBurst=5` / 300 s |
| `htbb-login` | TCP 8023 | **chỉ nội bộ**, ufw không mở |
| `htbb-web` | 127.0.0.1:3000 | Node 22, `.output` → `releases/<sha>` |
| nginx | 80, 443 | sau WAF; chứng chỉ origin tự ký; Host lạ → 444 |
| MariaDB | 127.0.0.1:3306 | không mở ra ngoài |
| Nova agent | 19130 | cổng của agent WAF |

ufw chỉ mở: **22, 80, 443, 19129, 19130**. Không mở 8023, 3306, 19153.

## 2. Bố cục trên VPS

| Đường dẫn | Là gì |
|---|---|
| `/opt/htbb` | bản checkout monorepo này, chủ sở hữu user hệ thống `htbb` |
| `/opt/htbb/server` | `htbb-server.jar`, `lib/`, `map/`, `cMap/`, `server.ini` (sinh từ `vps/server.ini.tmpl`), `loginServer/` (jar + `server.ini` tạo tay), `bao_tri.txt`, `trang_thai.json` |
| `/opt/htbb/web` | website: `.output` → `releases/<sha>/.output`, ảnh CMS ở `uploads/` |
| `/opt/htbb/downloads` | bản tải game, nginx phát ở `/downloads/` |
| `/opt/htbb/logs` | `game.out`, `login.out`, `gc.log`, `autodeploy.log`, `deploy-web.log`, `suc-khoe.log`, `nhat_ky/` |
| `/opt/htbb/backups` | bản sao lưu CSDL `*.sql.gz` (root, 0750), `server-ini/` (10 bản `server.ini` cũ) |
| `/etc/htbb/htbb.env` | bí mật + cấu hình (root, 0600), nạp bằng `EnvironmentFile` của `htbb-web` và `htbb-game` |
| `/etc/mysql/mariadb.conf.d/99-htbb.cnf` | cấu hình MariaDB (từ `vps/htbb-mariadb.cnf`) |
| `/etc/nginx/sites-available/htbb` | site nginx (từ `vps/htbb-nginx.conf`) |
| `/etc/nginx/snippets/htbb-waf-ip.conf` | dải IP của WAF, mỗi dòng `set_real_ip_from <CIDR>;` — **không** đưa vào git |
| `/etc/cron.d/htbb-backup`, `/usr/local/sbin/htbb-backup-db.sh` | sao lưu hằng ngày 03:30 |

### Tệp môi trường `/etc/htbb/htbb.env`

Một tệp dùng chung cho web, game và các script (mẫu đầy đủ: `web/.env.example`). Khoá chính:

| Khoá | Ý nghĩa |
|---|---|
| `HTBB_DB_HOST`, `HTBB_DB_PORT`, `HTBB_DB_USER`, `HTBB_DB_PASS`, `HTBB_DB_GAME`, `HTBB_DB_ACCOUNT` | CSDL cho web và script sao lưu |
| `DB_USER`, `DB_PASSWORD`, `DB_USER_NAP`, `DB_PASS_NAP` | bí mật điền vào `${...}` của `vps/server.ini.tmpl` (bộ sinh `vps/sinh-server-ini.sh`) |
| `HTBB_SECRET` | khoá ký token đăng nhập web — chuỗi ngẫu nhiên dài (`openssl rand -hex 32`) |
| `HTBB_SERVER_DIR=/opt/htbb/server` | thư mục game server (web đọc `server.ini`, ghi `bao_tri.txt`) |
| `HTBB_TRUSTED_PROXIES` | proxy tin cậy của web (nginx cục bộ) |
| `HTBB_SITE_URL=https://huyenthoaibaoboi.fun` | địa chỉ gốc website (canonical, sitemap) |
| `HTBB_UPLOAD_DIR`, `HTBB_TAI_GAME_DIR=/opt/htbb/downloads`, `HTBB_TAI_GAME_TOI_DA_MB` | ảnh CMS, bản tải game tải lên từ `/admin` |
| `HTBB_NAP_BAT=false`, `HTBB_NAP_SANDBOX` | nạp tiền — đang **tắt** (xem [van-hanh.md](van-hanh.md)) |
| `HTBB_ADMIN_2FA_BAT_BUOC` | bắt buộc 2FA cho khu quản trị (nên bật) |
| `HTBB_LOG_DIR` | (tuỳ chọn) thư mục log cho trang chẩn đoán, mặc định `<HTBB_SERVER_DIR>/../logs` |

Workflow deploy web gộp các secret GitHub vào tệp này (`htbb-deploy-web.sh nhan-env`): khoá nào secret để trống thì
giữ giá trị cũ trên VPS.

## 3. Dựng VPS lần đầu

Hệ điều hành Ubuntu 20.04+; gói: `openjdk-17-jre-headless`, `mariadb-server`, `nginx`, `ufw`, `git`, `unzip`, Node 22
(NodeSource). Kiểm thật sự đã cài: `which java node nginx git unzip && mysqld --version`.

### 3.1 Hệ thống

```bash
timedatectl set-timezone Asia/Ho_Chi_Minh          # giờ chiếm Gym, bảo trì, reset ngày theo giờ hệ điều hành
adduser --system --group --home /opt/htbb --shell /bin/bash htbb
install -d -o htbb -g htbb /opt/htbb/logs /opt/htbb/downloads
install -d -o root -g root -m 750 /opt/htbb/backups
ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw allow 19129/tcp && ufw allow 19130/tcp && ufw enable
```

Máy nhỏ thì tạo swap 1–2 GB với `vm.swappiness=10` (chỉ để chống bị kernel giết, không để chạy thường xuyên).

### 3.2 MariaDB

**Phải là MariaDB** (hoặc MySQL 5.7), không phải MySQL 8: login server `CheckLoginSocket.jar` so mật khẩu bằng
`PASSWORD()`, hàm này MySQL 8 đã bỏ — cài MySQL 8 là không ai đăng nhập được. Kiểm:
`mysql -u root -e "SELECT PASSWORD('abc');"` phải ra chuỗi `*…`.

Cài cấu hình **trước khi nạp schema** (`lower_case_table_names` đổi sau khi đã có bảng là hỏng CSDL):

```bash
install -m 644 /opt/htbb/vps/htbb-mariadb.cnf /etc/mysql/mariadb.conf.d/99-htbb.cnf
systemctl restart mariadb
mysql -u root -e "SHOW VARIABLES WHERE Variable_name IN ('lower_case_table_names','character_set_server','max_connections','innodb_buffer_pool_size');"
```

Các điểm chính của [`vps/htbb-mariadb.cnf`](../vps/htbb-mariadb.cnf):

| Thiết lập | Vì sao |
|---|---|
| `bind-address = 127.0.0.1` | không mở 3306 ra ngoài |
| `character-set-server = utf8`, `collation-server = utf8_general_ci` | **không dùng utf8mb4**: Connector/J 5.0.8 trong `CheckLoginSocket.jar` gặp collation utf8mb4 (id 45 / 224) là báo `Unknown initial character set index` và không ai đăng nhập được. Bảng nào cần utf8mb4 tự khai trong DDL |
| `lower_case_table_names = 1` | mã nguồn dùng tên bảng lẫn hoa/thường |
| `skip-name-resolve` | không tra DNS ngược; user CSDL khai theo IP / `localhost` |
| `performance_schema = OFF` | tiết kiệm RAM |
| `max_connections = 300` | game mở sẵn ~137 kết nối (`db.maxco` + pool nạp + pool phụ), cộng login, web, sao lưu |
| `innodb_buffer_pool_size = 768M` | VPS 4 GB, JVM 1,5 GB, web ~400 MB |

Tạo CSDL và user `htbb` (mật khẩu ngẫu nhiên, ghi vào `/etc/htbb/htbb.env`). Vì `skip-name-resolve`, kết nối TCP
(JDBC) đến từ `127.0.0.1` chứ không khớp `localhost` — cấp quyền cho **cả hai**:

```sql
CREATE DATABASE htbb_db; CREATE DATABASE account;
CREATE USER 'htbb'@'localhost' IDENTIFIED BY '<mật-khẩu>';
CREATE USER 'htbb'@'127.0.0.1' IDENTIFIED BY '<mật-khẩu>';
GRANT ALL PRIVILEGES ON htbb_db.* TO 'htbb'@'localhost';  GRANT ALL PRIVILEGES ON account.* TO 'htbb'@'localhost';
GRANT ALL PRIVILEGES ON htbb_db.* TO 'htbb'@'127.0.0.1';  GRANT ALL PRIVILEGES ON account.* TO 'htbb'@'127.0.0.1';
FLUSH PRIVILEGES;
```

Kiểm đúng đường Java đi (có `-h 127.0.0.1`): `mysql -u htbb -p -h 127.0.0.1 -e "SHOW DATABASES;"`.

### 3.3 Mã nguồn

Tạo deploy key chỉ-đọc cho repo (Settings → Deploy keys, **không** tích *Allow write access*), rồi checkout vào
`/opt/htbb` bằng user `htbb` (thư mục home đã có `.ssh`, nên clone ra chỗ khác rồi chép vào):

```bash
sudo -u htbb git clone <repo> /tmp/htbb-src && sudo -u htbb cp -a /tmp/htbb-src/. /opt/htbb/ && rm -rf /tmp/htbb-src
chmod +x /opt/htbb/vps/*.sh
```

Luôn chạy git trên VPS bằng `sudo -u htbb git -C /opt/htbb …`. Không sửa tay tệp được theo dõi trên VPS (deploy dừng
lại khi thấy cây làm việc bẩn).

### 3.4 Nạp CSDL

```bash
mysql -u root < /opt/htbb/sql/htbb_day_du.sql            # tạo account + htbb_db, đủ dữ liệu Pokémon
mysql -u root htbb_db < /opt/htbb/sql/them_chi_muc_production.sql
```

`sql/htbb_day_du.sql` là tệp sinh (xem [sql/README.md](../sql/README.md)) — dựng sẵn tài khoản `admin` với mật khẩu
ngẫu nhiên không ai biết. Đặt mật khẩu admin ngay sau khi nạp:

```bash
mysql -u root -e "UPDATE account.team_user SET password = PASSWORD('mật-khẩu-mạnh') WHERE username = 'admin';"
```
 Server tự tạo thêm các bảng theo kỳ và bảng phụ (`CREATE TABLE IF NOT EXISTS`) lúc chạy lần
đầu — bình thường.

Kiểm nhanh những bảng hay gây lỗi "im lặng" phía client:

```sql
SELECT COUNT(*) FROM data_attribute WHERE id BETWEEN 0 AND 186;   -- phải đúng 187 (id liên tục)
SELECT COUNT(*) AS so_dong, MAX(id) FROM data_item;                -- so_dong phải >= MAX(id)
SELECT COUNT(*) FROM data_potion;                                  -- phải đúng 162
```

- **`data_attribute`** phải có đủ id 0..186 **liên tục**: tên thuộc tính tra **theo vị trí** trong danh sách nạp lúc
  khởi động, gói đăng nhập chỉ gửi số dòng đang có, thiếu một id ở giữa là client đơ khi mở trang bị mà server không
  ghi log gì. `server/test/KiemBangThuocTinh.java` ghim điều kiện này trên bản seed.
- **`data_item`**: client cấp mảng theo số lượng nhưng ghi theo id, nên **không bao giờ `DELETE` một dòng ở giữa** —
  muốn bỏ vật phẩm thì đổi thành loại trơ và giữ dòng.
- **`data_potion`** luôn 162 dòng (`Char.SO_O_THUOC`): thuốc mới `UPDATE` vào ô trống, không `INSERT`.

### 3.5 Tệp cấu hình

1. `/etc/htbb/htbb.env` (root 0600) theo mục 2.
2. `server.ini` của game: `sudo /opt/htbb/vps/sinh-server-ini.sh` (đọc bí mật từ biến môi trường hoặc
   `/etc/htbb/htbb.env`, thử kết nối CSDL rồi mới ghi `/opt/htbb/server/server.ini`). Chi tiết: [cau-hinh.md](cau-hinh.md).
3. Login server: chép [`vps/loginServer.server.ini.example`](../vps/loginServer.server.ini.example) thành
   `/opt/htbb/server/loginServer/server.ini`, điền mật khẩu, `chown htbb:htbb`, `chmod 640`.

### 3.6 systemd, nginx, cron

```bash
install -m 644 /opt/htbb/vps/htbb-login.service /opt/htbb/vps/htbb-game.service /etc/systemd/system/
bash /opt/htbb/web/deploy/cai-dat-vps.sh        # htbb-web.service, /usr/local/sbin/htbb-deploy-web.sh, khoá SSH deploy web
systemctl daemon-reload
systemctl enable --now htbb-login htbb-game

install -m 644 /opt/htbb/vps/htbb-nginx.conf /etc/nginx/sites-available/htbb
ln -sf /etc/nginx/sites-available/htbb /etc/nginx/sites-enabled/htbb
touch /etc/nginx/snippets/htbb-waf-ip.conf      # điền dải IP WAF (set_real_ip_from <CIDR>;)
nginx -t && systemctl reload nginx

install -m 644 /opt/htbb/vps/htbb-logrotate.conf /etc/logrotate.d/htbb
install -m 750 /opt/htbb/vps/backup_db.sh /usr/local/sbin/htbb-backup-db.sh
```

nginx ([`vps/htbb-nginx.conf`](../vps/htbb-nginx.conf)): khối mặc định trả **444** cho Host lạ / truy cập thẳng IP
(không lộ site trên IP origin); site chính nghe 80 và 443 với chứng chỉ origin tự ký ở `/etc/ssl/htbb/` (WAF → origin
chế độ "full", không strict); lấy IP thật của khách qua `real_ip_header X-Forwarded-For` với các dải trong
`htbb-waf-ip.conf`; `/downloads/` phát thẳng từ `/opt/htbb/downloads`; còn lại chuyển về `127.0.0.1:3000`
(`client_max_body_size 350m` cho bản tải game upload từ `/admin`). Thiếu tệp snippet thì `nginx -t` báo lỗi.

Cron của root (các dòng mẫu nằm ở đầu từng script):

| Lịch | Lệnh |
|---|---|
| `30 3 * * *` (`/etc/cron.d/htbb-backup`) | `/usr/local/sbin/htbb-backup-db.sh` — dump `htbb_db` + `account`, giữ 14 ngày |
| `30 4 * * 1` | `vps/kiem-khoi-phuc-csdl.sh` — nạp thử bản sao lưu mới nhất vào CSDL tạm, so số dòng |
| mỗi phút | `vps/kiem-suc-khoe.sh` — bắt tay `-40` với cổng game, `trang_thai.json`, login, CSDL, web |
| mỗi phút | `vps/dieu-khien-server.sh` — bật/tắt game theo lệnh từ `/admin/he-thong` (khối Bật/tắt) |
| `20 3 * * *` | `vps/don-log.sh` — dọn bảng nhật ký CSDL và tệp `.txt` lẻ |

Người chơi vào game qua WAF / Nova agent, nên game server chỉ thấy IP của proxy: thêm dải WAF vào `sv.proxyTinCay` ở
`/admin → Cấu hình hệ thống` (áp dụng ngay; mặc định chỉ `127.0.0.1,::1`), **không ghi IP WAF vào git**.

### 3.7 Tài khoản GM

Quyền GM xét theo **tên nhân vật** trong `sv.adminAccounts`. Tạo nhân vật đúng tên đó (tên chứa chữ "admin" bị chặn
với người chơi thường; bật tạm `sv.choPhepTaoTenGM=1` nếu cần), rồi có thể bật `sv.gmCanCoPInfo=1` để đòi thêm
`pInfo[16]=1`. Sổ tay GM: [van-hanh.md](van-hanh.md).

## 4. Cập nhật (deploy)

### 4.1 Game server — `server-deploy.yml` → `vps/deploy-auto.sh`

Mỗi push lên `main` có đổi `server/`, `vps/`, `sql/`: GitHub Actions SSH vào VPS bằng khoá bị ép lệnh
(`command="/opt/htbb/vps/deploy-auto.sh"` trong `authorized_keys`, `no-pty`) và gửi bí mật qua **stdin**. Workflow chỉ
chạy khi biến repo `HTBB_VPS_MONOREPO=1`; địa chỉ SSH nằm ở biến `HTBB_VPS_HOST`, khoá ở secret `VPS_SSH_KEY`.

[`vps/deploy-auto.sh`](../vps/deploy-auto.sh):

1. Dừng nếu cây làm việc trên VPS bị sửa tay (bỏ qua tệp tạm của trình soạn thảo).
2. `git pull --ff-only`, kiểm `unzip -t server/htbb-server.jar` (hỏng thì khôi phục jar cũ, server vẫn chạy bản cũ).
3. Sinh lại `server.ini` (`vps/sinh-server-ini.sh`; mã thoát 3 = đã đổi).
4. Nếu commit đổi `server/htbb-server.jar`, `server/lib/`, `server/map/`, `server/cMap/`, `server/loginServer/`,
   bản mẫu cấu hình hoặc `server.ini` đổi: **không** khởi động lại ngay mà để lần bảo trì tự động kế tiếp
   (`BaoTriDaily`) nạp bản mới. Đổi tài liệu, SQL, `.java` chỉ kéo code, không ai bị kick.
5. Cần lên ngay: chạy workflow bằng tay (*workflow_dispatch*) và tích **deploy_now**, hoặc bấm "Bảo trì" trên web
   admin. Khi đó script đặt cờ `5h_systems.baotri_thongbao` (báo người chơi 60 giây), `systemctl stop htbb-game`
   (shutdown hook lưu dữ liệu), bật lại và chờ cổng 19129.

Có `flock` chống hai lần deploy chồng nhau và `trap` SIGHUP để bật lại server nếu SSH đứt giữa chừng. Log:
`/opt/htbb/logs/autodeploy.log` (cũng hiện trong tab Actions). Chạy tay: `sudo /opt/htbb/vps/deploy-auto.sh`.

Nhớ: VPS chạy **tệp jar đã commit**. Sửa `server/src` thì phải build lại `server/htbb-server.jar` ([build.md](build.md))
và commit cả jar.

Cập nhật tay không qua Actions: `sudo /opt/htbb/vps/update.sh` (tắt có trật tự → pull → kiểm jar → bật lại);
thay một jar chép tay để thử gấp: `sudo /opt/htbb/vps/capnhat.sh` (lần `update.sh` sau sẽ báo tệp bị sửa tay —
`sudo -u htbb git -C /opt/htbb checkout -- server/htbb-server.jar`).

Quay lại bản trước: `sudo -u htbb git -C /opt/htbb log --oneline -5`, rồi
`systemctl stop htbb-game && sudo -u htbb git -C /opt/htbb checkout <commit> -- server/htbb-server.jar && systemctl start htbb-game`.

### 4.2 Website — `web-deploy.yml` → `htbb-deploy-web.sh`

Actions build `web/.output` (Node 22), SSH bằng khoá riêng (secret `VPS_SSH_KEY_WEB`, ép lệnh
`/usr/local/sbin/htbb-deploy-web.sh`) với hai việc: `nhan-env` (gộp secret vào `/etc/htbb/htbb.env`) và
`nhan-build <sha>` (giải nén vào `/opt/htbb/web/releases/<sha>`, đổi symlink `.output`, khởi động lại `htbb-web`,
kiểm `/api/site` trả 200 — hỏng thì **quay lại bản trước**; giữ 5 bản cũ). Log: `/opt/htbb/logs/deploy-web.log`.

### 4.3 Bản tải game

Tải lên ở `/admin → Tải game` (ghi vào `/opt/htbb/downloads`, tối đa `HTBB_TAI_GAME_TOI_DA_MB`), nginx phát ở
`https://huyenthoaibaoboi.fun/downloads/<tệp>` (web cũng phát ở `/tai/<tệp>`). Build bản phát hành: [build.md](build.md).

Website phát đúng **5 bản tải**, mỗi nền tảng một tệp (`web/shared/utils/taiGame.ts`): Java J2ME `.jar` (bản
"thuong"), Windows bộ cài `.exe`, Linux `.AppImage` hoặc `.tar.gz`, Android `.apk`, iOS "Sắp ra mắt". Jar "nhẹ",
bản Windows `.zip` và `HTBB-PC-<bản>.jar` không còn hiện trên web (tệp trên đĩa không bị xoá). Giá trị CMS kiểu cũ
(mã `pc-java` / `pc` / `pc-jar`, dòng "tệp phụ" `/tai/…` trong ô "Có gì mới") được đổi tự động khi đọc.

## 5. Sao lưu và khôi phục

Sao lưu hằng ngày 03:30 ra `/opt/htbb/backups/htbb_db_<ngày>.sql.gz` và `account_<ngày>.sql.gz`
(`--single-transaction`, kiểm `gzip -t`, giữ 14 ngày; đăng nhập CSDL qua unix_socket của root hoặc `HTBB_DB_*` trong
env, mật khẩu không lên dòng lệnh). Bản sao **ngoài máy**: đặt `HTBB_BACKUP_DICH="user@may-khac:/duong/dan/"` trong
`/etc/default/htbb-backup` (rsync qua SSH key) — không có thì mất VPS là mất hết.

Khôi phục thật (làm tay, theo thứ tự):

```bash
sudo systemctl stop htbb-game htbb-login
zcat /opt/htbb/backups/htbb_db_<NGAY>.sql.gz | mysql htbb_db
zcat /opt/htbb/backups/account_<NGAY>.sql.gz | mysql account
sudo systemctl start htbb-login htbb-game
```

Chi tiết và diễn tập: [van-hanh.md](van-hanh.md).

## 6. Kiểm tra sau khi dựng / deploy

```bash
systemctl status htbb-login htbb-game htbb-web nginx mariadb --no-pager
ss -lntp | grep -E ':(19129|8023|3000|3306|80|443) '     # 8023/3000/3306 chỉ nghe 127.0.0.1
grep -i "configuration file error" /opt/htbb/logs/game.out | tail -n 3
/opt/htbb/vps/kiem-suc-khoe.sh                            # một dòng kết quả, mã thoát 0 = khoẻ
curl -fsS http://127.0.0.1:3000/api/site >/dev/null && echo web OK
```

Từ máy ngoài: mở `https://huyenthoaibaoboi.fun/`, đăng nhập game bằng tài khoản thử, vào bằng nhân vật GM và gõ
`setting` → `6` để xem cấu hình đang chạy.

## 7. Sự cố khi dựng

| Triệu chứng | Nguyên nhân thường gặp |
|---|---|
| Không ai đăng nhập được, login server im lặng | cài MySQL 8 (mất `PASSWORD()`), hoặc MariaDB đặt utf8mb4 (`Unknown initial character set index`) |
| `Access denied` trong `game.out` dù `mysql -u htbb` chạy được | thiếu quyền cho `'htbb'@'127.0.0.1'` (CLI đi qua socket, Java đi TCP) |
| `systemctl` báo *active* nhưng không cổng nào nghe | CSDL hỏng / sai mật khẩu → `kiemTraCSDL()` thoát; xem `game.out`. Unit `failed` sau 5 lần chết trong 300 s: sửa rồi `systemctl reset-failed htbb-game` |
| `Table '…' doesn't exist` mà bảng có | `lower_case_table_names` chưa đặt trước khi nạp — phải nạp lại |
| Client kết nối được nhưng đứng ở màn chờ | `htbb-login` chưa chạy |
| Vào game trắng màn / không thấy bản đồ | thiếu `server/map` hoặc `server/cMap`, hoặc user `htbb` không đọc được (`chown -R htbb:htbb /opt/htbb/server`) |
| Client đơ khi mở trang bị, server không log gì | `data_attribute` thiếu id trong 0..186 (mục 3.4) |
| Nhân vật chỉ còn cái đầu, hành trang rỗng | lỗ id trong `data_item` (mục 3.4) |
| Server tự tắt, log chỉ có `Killed` | hết RAM — giảm heap / buffer pool hoặc nâng VPS; `dmesg \| grep -i "killed process"` |
| `BindException: Address already in use` | còn tiến trình cũ giữ cổng 19129 |
| `/bin/bash^M: bad interpreter` | tệp `.sh` bị CRLF; repo ép `*.sh eol=lf`, sửa: `sed -i 's/\r$//' /opt/htbb/vps/*.sh` |
| `HeadlessException` trong log | vô hại (đến từ `lib/htbb-core.jar`, chỉ giết luồng AWT) |

Tắt server luôn bằng `systemctl stop htbb-game` (SIGTERM → shutdown hook lưu nhân vật, bang, chợ; `TimeoutStopSec=180`).
**Đừng `kill -9`** — mất dữ liệu chưa lưu.

## 8. Bảo mật

- Game server chạy bằng user `htbb`, không phải root. SSH chỉ dùng khoá; cân nhắc tắt `PermitRootLogin`.
- Hai khoá SSH của GitHub Actions đều bị ép lệnh (`command="…"`, `no-pty`, `no-port-forwarding`): lộ secret cũng chỉ
  chạy được đúng script deploy.
- `server.ini`, `loginServer/server.ini`, `/etc/htbb/htbb.env` chứa mật khẩu: `chmod 640`/`600`, không đưa vào git,
  không dán cho ai.
- Repo giữ private: `server/lib/htbb-core.jar` là thư viện đóng của bên thứ ba, lịch sử git từng có dữ liệu nhạy cảm.
- Bật `HTBB_ADMIN_2FA_BAT_BUOC=true`; giới hạn IP admin bằng `HTBB_ADMIN_IP_CHO_PHEP` nếu được.
