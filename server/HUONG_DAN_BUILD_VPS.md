# Dựng server KPAH chạy hẳn trên VPS `180.93.57.30` (Ubuntu 20.04)

Khác với [HUONG_DAN_VPS_FRP.md](HUONG_DAN_VPS_FRP.md): file đó để VPS làm **trạm chuyển tiếp**,
game vẫn chạy ở máy nhà. File này là **chuyển toàn bộ server lên VPS** — máy nhà tắt cũng
không ảnh hưởng người chơi.

```
Người chơi ──► 180.93.57.30:19129   game server (Java)
                              :80      Apache + PHP (web đăng ký, NQSH2.txt)
             VPS chạy tất cả: Java game server + login server + MariaDB + Apache
                              127.0.0.1:8023   login server  (KHÔNG mở ra ngoài)
```

## Trước khi bắt đầu — hai điều quyết định thành/bại

**1. Phải dùng MariaDB, KHÔNG dùng MySQL 8.**
Login server (`CheckLoginSocket.jar`) so mật khẩu bằng câu lệnh
`select * from team_user where username='...' and \`password\`=password('...')`.
Hàm `PASSWORD()` **đã bị MySQL 8 xoá bỏ** → cài MySQL 8 là không ai đăng nhập được, và
đó là loại lỗi rất khó đoán vì server không báo gì rõ ràng. Thêm nữa driver trong jar đó
là Connector/J 5.0.8, không nói chuyện được với cơ chế xác thực mặc định của MySQL 8.
MariaDB 10.3 trong repo Ubuntu 20.04 còn `PASSWORD()` và dùng `mysql_native_password` →
chạy đúng. Máy nhà của bạn cũng đang là MariaDB 10.4 (XAMPP) nên bản dump khớp luôn.

**2. VPS 1 GB RAM là chật.**
Cần cùng lúc: game server (~400–500 MB heap), login server (~50 MB), MariaDB (~200 MB),
Apache/PHP (~100 MB). Cộng lại là sát trần. Bắt buộc **tạo swap 2 GB** (bước B2) và giới
hạn heap như trong file service. Với vài chục người chơi thì đủ; đông hơn nên nâng VPS
lên 2 GB rồi sửa `-Xmx512m` thành `-Xmx1g`. Nói trước để bạn không mất buổi tối đi tìm
nguyên nhân server tự chết — hết RAM thì kernel giết tiến trình Java, log chỉ ghi
`Killed`.

Ngoài ra: DB game của bạn đang **79 MB / 84 bảng**, thư mục `map/` **16 MB** → upload
tổng khoảng 100 MB, vài phút là xong.

---

## PHẦN A — Chuẩn bị ở máy nhà (Windows)

### A1. Tắt server để dump DB sạch

Tắt game server (Admin Panel → **Bảo Trì**) và `loginServer`. MySQL vẫn để chạy.

### A2. Dump 2 database

Mở PowerShell tại `C:\Users\ADMIN\Desktop\KPAH`:

```powershell
& "C:\xampp\mysql\bin\mysqldump.exe" -u root --single-transaction --routines --events kpah_db > kpah_db.sql
& "C:\xampp\mysql\bin\mysqldump.exe" -u root --single-transaction --routines --events account > account.sql
```

Có mật khẩu MySQL thì thêm `-p` (nó sẽ hỏi). Kiểm tra file không rỗng:

```powershell
Get-Item kpah_db.sql, account.sql | Select-Object Name, Length
```

`kpah_db.sql` phải cỡ vài chục MB. Nếu chỉ vài KB là dump lỗi, đọc lại thông báo.

> Trong repo có `gamedb7_clean.sql` (cấu trúc + dữ liệu game, **không có** nhân vật) và
> `loginServer/account.sql`. Chỉ dùng 2 file đó khi bạn muốn **server mới trắng dữ liệu**.
> Muốn giữ nhân vật hiện tại thì phải dump như trên.

### A3. Gói file cần upload

Windows 10 có sẵn `tar`:

```powershell
tar -czf kpah-deploy.tar.gz KPAH2_patched.jar lib map cMap server.ini loginServer vps *.txt
```

Danh sách này là **đủ và không thừa**:

| Mục | Vì sao cần |
|---|---|
| `KPAH2_patched.jar` | server. Manifest trỏ `Class-Path: lib/...` **tương đối theo vị trí jar** → jar và `lib/` phải nằm cùng thư mục |
| `lib/` | `NQSH_5h.jar` (thư viện đóng), driver MySQL, jxl |
| `map/`, `cMap/` | ảnh + dữ liệu map server gửi xuống client. Thiếu là client vào game bị trắng/treo |
| `server.ini` | cấu hình |
| `*.txt` | dữ liệu vận tiêu (`cuoptieu`, `doitieu`, `nhantieu`, `thetieu`, `tratieu`, `matketinfo`, `updateleaf`) |
| `loginServer/` | `CheckLoginSocket.jar` + `server.ini` của nó |
| `vps/` | 2 file systemd + `capnhat.sh` + `backup_db.sh` mình đã viết sẵn |

**Không** upload: `src/`, `build/`, `dist/`, `Client/`, `nbproject/`, `test/`, các file `.sql`
(trừ 2 file dump ở A2).

### A4. Đẩy lên VPS

```powershell
scp kpah-deploy.tar.gz kpah_db.sql account.sql root@180.93.57.30:/root/
```

---

## PHẦN B — Chuẩn bị VPS

Vào VPS: `ssh root@180.93.57.30` (Linux không có Remote Desktop, `mstsc` sẽ luôn báo lỗi).

### B1. Cập nhật + gói cơ bản

```bash
apt update && apt upgrade -y
apt install -y openjdk-8-jre-headless mariadb-server unzip ufw
```

Java 8 là đủ: jar server biên dịch ở class version 52 (Java 8), thư viện `NQSH_5h.jar` là
version 50 (Java 6). Máy có Java 11/17 sẵn cũng chạy được, nhưng 8 là bản khớp nhất.
Kiểm tra: `java -version`.

### B2. Swap 2 GB (bắt buộc với VPS 1 GB)

```bash
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
sysctl -w vm.swappiness=10
echo 'vm.swappiness=10' >> /etc/sysctl.conf
free -h        # phải thấy dòng Swap: 2.0Gi
```

### B3. Tạo user riêng cho server

Đừng chạy game server bằng `root`.

```bash
adduser --system --group --home /opt/kpah --shell /bin/bash kpah
mkdir -p /opt/kpah/log /opt/kpah/backup
```

### B4. Múi giờ

Giờ chiếm thành, vận tiêu, reset ngày đều theo giờ hệ điều hành:

```bash
timedatectl set-timezone Asia/Ho_Chi_Minh
date        # kiểm tra lại
```

Bỏ qua bước này là chiếm thành nổ lệch 7 tiếng.

---

## PHẦN C — MariaDB

### C1. Cấu hình trước khi import

```bash
nano /etc/mysql/mariadb.conf.d/50-server.cnf
```

Trong khối `[mysqld]`, thêm/sửa:

```ini
# Linux phan biet hoa thuong ten bang, Windows thi khong. Bat cai nay de code
# viet 'tob_char' hay 'TOB_CHAR' deu chay -> khoi loi "Table doesn't exist".
# PHAI dat TRUOC khi import.
lower_case_table_names = 1

character-set-server = utf8mb4
collation-server     = utf8mb4_general_ci

# VPS 1 GB: khong de MariaDB an het RAM cua Java
innodb_buffer_pool_size = 128M
performance_schema      = OFF
skip-name-resolve

max_connections = 200
```

`max_connections` phải cao hơn tổng pool trong `server.ini` (`db.maxco=100` + `db.maxNap=30`
+ `db.maxco1=1` + login server 8 + web) — thiếu là giờ đông người sẽ ăn lỗi
*Too many connections*.

```bash
systemctl restart mariadb
systemctl enable mariadb
```

### C2. Tạo database + user

```bash
mysql -u root
```

```sql
CREATE DATABASE kpah_db DEFAULT CHARSET utf8mb4;
CREATE DATABASE account DEFAULT CHARSET utf8mb4;

-- Mat khau: doi thanh chuoi cua ban, se dung o server.ini
CREATE USER 'kpah'@'localhost' IDENTIFIED BY 'MAT_KHAU_MANH_CUA_BAN';
GRANT ALL PRIVILEGES ON kpah_db.* TO 'kpah'@'localhost';
GRANT ALL PRIVILEGES ON account.* TO 'kpah'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### C3. Import

```bash
cd /root
mysql -u root kpah_db  < kpah_db.sql
mysql -u root account  < account.sql

# Kiem tra: phai ra 84 va 1
mysql -u root -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='kpah_db';"
mysql -u root -e "SELECT COUNT(*) FROM account.team_user;"
```

Nếu import báo lỗi *Unknown collation* thì bản MariaDB 10.3 trên VPS cũ hơn 10.4 ở nhà —
sửa nhanh: `sed -i 's/utf8mb4_0900_ai_ci/utf8mb4_general_ci/g; s/utf8mb3/utf8/g' kpah_db.sql`
rồi import lại.

### C4. Kích hoạt tài khoản

`sv.EnableAvtiveCheck=1` nên tài khoản nào không có dòng trong `kpah_db.5h_active` sẽ bị
chặn ngay ở màn đăng nhập:

```sql
INSERT INTO kpah_db.5h_active (userID, username, time_end)
SELECT id, username, '-1' FROM account.team_user
WHERE id NOT IN (SELECT userID FROM kpah_db.5h_active);
```

Hoặc đặt `sv.EnableAvtiveCheck=0` trong `server.ini`.

---

## PHẦN D — Cài server

### D1. Bung file

```bash
cd /opt/kpah
tar -xzf /root/kpah-deploy.tar.gz
cp vps/capnhat.sh vps/backup_db.sh /opt/kpah/
chmod +x /opt/kpah/capnhat.sh /opt/kpah/backup_db.sh
chown -R kpah:kpah /opt/kpah
ls -la          # phai thay KPAH2_patched.jar, lib/, map/, cMap/, server.ini, loginServer/
```

Kiểm tra tên thư mục **`cMap`** đúng chữ M hoa — Linux phân biệt hoa thường, `cmap` là
server không đọc được dữ liệu map.

### D2. Sửa `server.ini` cho Linux

```bash
nano /opt/kpah/server.ini
```

Ba dòng **bắt buộc** đổi (đường dẫn Windows sẽ không tồn tại trên Linux):

```ini
# Truoc: D:\\server\\nqsh\\log\\   — PHAI co dau / o cuoi
sv.pathLog=/opt/kpah/log/

# Truoc: D:\\nqsh30\\toolRestart.bat
db.run=/opt/kpah/capnhat.sh

# Mat khau MariaDB vua tao o buoc C2
db.password=MAT_KHAU_MANH_CUA_BAN
db.pass=MAT_KHAU_MANH_CUA_BAN
db.password1=MAT_KHAU_MANH_CUA_BAN
```

Và đổi user DB `root` → `kpah` ở cả 4 chỗ: `db.user`, `db.userNap`, `db.user1`.

Giữ nguyên: `sv.port=19129`, `sv.server=1` (đặt 0 là bật chế độ local/test, tỉ lệ và boss
sẽ khác), `sv.iplogin=127.0.0.1`, `sv.portlogin=8023`.

### D3. Sửa `loginServer/server.ini`

```ini
db.url=jdbc:mysql://localhost:3306/account
db.user=kpah
db.password=MAT_KHAU_MANH_CUA_BAN
port=8023
```

### D4. Cài 2 service

```bash
cp /opt/kpah/vps/kpah-login.service /etc/systemd/system/
cp /opt/kpah/vps/kpah-game.service  /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now kpah-login
sleep 3
systemctl enable --now kpah-game
```

Xem có chạy không:

```bash
systemctl status kpah-login kpah-game --no-pager
tail -f /opt/kpah/log/game.out          # Ctrl+C để thoát
```

Trong log phải thấy `MAX CHAR PER ACCOUNT`, `TI LE DROP TRAIN QUAI`, `CHIEM THANH ->`,
và dòng `[ADMIN PANEL] Che do khong man hinh (headless)` — dòng cuối là đúng, không phải lỗi.

> **Tắt server đúng cách:** `systemctl stop kpah-game`. systemd gửi SIGTERM, JVM chạy
> shutdown hook trong `TeamServer.run()` để lưu nhân vật / bang hội / chợ trước khi thoát,
> `TimeoutStopSec=180` cho nó đủ thời gian. **Đừng `kill -9`** — mất dữ liệu chưa lưu.

---

## PHẦN E — Tường lửa

```bash
ufw allow 22/tcp        # SSH — LÀM ĐẦU TIÊN, quên là tự khoá mình ngoài
ufw allow 19129/tcp     # game
ufw allow 80/tcp        # web
ufw enable
ufw status
```

**Không mở 8023 và 19153.** 8023 là login server, chỉ game server gọi qua `127.0.0.1`.
19153 là socket lệnh admin, chỉ khoá bằng chuỗi hardcode trong mã nguồn công khai —
mở ra internet là mời người khác vào làm admin.

Nhà cung cấp VPS có firewall riêng trong panel (Security Group) thì mở y hệt 3 cổng đó.

---

## PHẦN F — Web đăng ký + file danh sách server

```bash
apt install -y apache2 php php-mysql
cp -r /opt/kpah/web /var/www/html/kpah      # nếu bạn có upload thư mục web
nano /var/www/html/kpah/config.php
```

Trong `config.php` sửa `DB_USER = 'kpah'`, `DB_PASS = 'MAT_KHAU_...'`; `DB_ACCOUNT` và
`DB_GAME` giữ nguyên.

File danh sách server mà client đọc:

```bash
echo 'Server 1:180.93.57.30:19129:0' > /var/www/html/NQSH2.txt
chown -R www-data:www-data /var/www/html
systemctl restart apache2
```

Định dạng: `tên:địa_chỉ:port:index`, nhiều server ngăn nhau bằng dấu phẩy. Sau này đổi IP
chỉ cần sửa file này, người chơi bấm **"Cập nhật"** trong client là xong — không phải
phát hành client mới.

Client đã trỏ đúng IP: [Client/ServerListScr.cs:36](Client/ServerListScr.cs#L36)
`SERVER_HOST = "180.93.57.30"`, port `19129`.
(Client lưu port bằng `short` nên không nhận port > 32767 — đừng đổi sang cổng cao.)

---

## PHẦN G — Kiểm tra

### G1. Trên VPS

```bash
ss -lntp | grep -E ':(19129|8023|80|3306)'
```

Phải thấy:
- `19129` — `0.0.0.0` (game, mở ra ngoài)
- `8023` — login server
- `80` — Apache
- `3306` — MariaDB, nên là `127.0.0.1`

### G2. Từ máy khác

```powershell
Test-NetConnection 180.93.57.30 -Port 19129
```

`TcpTestSucceeded : True` là thông. Mở `http://180.93.57.30/NQSH2.txt` phải ra đúng dòng
`Server 1:...`.

### G3. Vào game

Chạy client, chọn Server 1, đăng nhập bằng tài khoản có trong `account.team_user`.
Đăng nhập được nhưng vào game trắng màn hình → thiếu `map/` hoặc `cMap/`.

---

## PHẦN H — Vận hành

### Cập nhật jar mới

Ở máy nhà, build xong thì:

```powershell
scp KPAH2_NEW.jar kpah@180.93.57.30:/opt/kpah/
```

Trên VPS:

```bash
sudo /opt/kpah/capnhat.sh
```

Script làm đúng thứ tự như `capnhat.bat` bên Windows: kiểm tra jar có hỏng không (`unzip -t`),
tắt server có trật tự để lưu dữ liệu, backup jar cũ thành
`KPAH2_patched_backup_truoc_capnhat.jar`, thay jar, bật lại, rồi in trạng thái + kiểm tra
cổng 19129.

### Backup DB tự động

```bash
printf '[client]\nuser=root\npassword=MAT_KHAU_ROOT_MYSQL\n' > /root/.my.cnf
chmod 600 /root/.my.cnf
crontab -e
```

Thêm dòng:

```
0 4 * * * /opt/kpah/backup_db.sh >> /opt/kpah/log/backup.log 2>&1
```

4h sáng mỗi ngày, giữ 7 bản gần nhất trong `/opt/kpah/backup`. Tải về máy nhà:

```powershell
scp root@180.93.57.30:/opt/kpah/backup/kpah_db_*.sql.gz .
```

### Xem log

| Cần gì | Lệnh |
|---|---|
| Log game server | `tail -f /opt/kpah/log/game.out` |
| Log login server | `tail -f /opt/kpah/log/login.out` |
| Log systemd | `journalctl -u kpah-game -f` |
| Log của game (UserLogger) | `ls /opt/kpah/log/` |
| RAM còn bao nhiêu | `free -h` |
| Java đang ăn bao nhiêu | `ps -o pid,rss,cmd -C java` |

### Sửa cấu hình khi đang chạy

Bảng setting trong game vẫn hoạt động y như trên Windows: gõ `setting` → sửa → nó ghi
thẳng vào `/opt/kpah/server.ini`. Sửa tay file rồi gõ `setting` → `8` để nạp lại, không
cần restart. Nhớ file phải cho user `kpah` ghi được (`chown kpah:kpah /opt/kpah/server.ini`),
nếu không thông báo trong game sẽ nói "CHƯA lưu được server.ini".

---

## PHẦN I — Build lại jar **trên VPS** (nếu muốn sửa code ngay trên VPS)

Bình thường nên build ở máy nhà rồi `scp` lên. Nhưng nếu bạn muốn build trên VPS:

```bash
apt install -y openjdk-8-jdk git
```

Đưa `src/` lên (`scp -r src kpah@180.93.57.30:/opt/kpah/`) rồi:

```bash
cd /opt/kpah
mkdir -p out
find src -name '*.java' > srcs.txt
javac -encoding UTF-8 -nowarn \
  -cp "lib/NQSH_5h.jar:lib/json-20250107.jar:lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar" \
  -d out @srcs.txt
```

Dấu ngăn classpath trên Linux là **`:`**, không phải `;` như Windows.

**Bước quan trọng nhất — KHÔNG đóng jar từ đầu:**

```bash
cp KPAH2_patched.jar KPAH2_NEW.jar
jar uf KPAH2_NEW.jar -C out .
unzip -t KPAH2_NEW.jar >/dev/null && echo "jar OK"
sudo ./capnhat.sh
```

Lý do phải **chồng lớp mới lên jar cũ** thay vì đóng jar mới: trong jar có class
`util/Common` **không có mã nguồn** trong `src/`. Đóng jar từ đầu chỉ bằng `src` sẽ làm
mất class đó và server chết lúc chạy tới chỗ dùng nó. Đây cũng là cách mình build ở máy
nhà. Kiểm tra sau khi build:

```bash
# Danh sach class truoc/sau phai giong nhau (tru class bạn co tinh them)
unzip -l KPAH2_patched.jar | awk '{print $4}' | sort > /tmp/cu.txt
unzip -l KPAH2_NEW.jar     | awk '{print $4}' | sort > /tmp/moi.txt
diff /tmp/cu.txt /tmp/moi.txt
```

Nếu VPS 1 GB, `javac` biên dịch 99 file cùng lúc có thể chạm trần RAM — có swap ở bước B2
thì vẫn xong, chỉ chậm.

---

## PHẦN J — Lỗi hay gặp

**Không ai đăng nhập được, log login server im lặng**
→ Bạn cài MySQL 8 chứ không phải MariaDB. `PASSWORD()` không còn tồn tại.
Kiểm tra: `mysql -u root -e "SELECT PASSWORD('abc');"` — báo lỗi hàm không tồn tại là đúng
bệnh. Cài lại MariaDB.

**`Table 'kpah_db.tob_char' doesn't exist` mà rõ ràng bảng có**
→ Sai hoa thường. Đặt `lower_case_table_names = 1` rồi restart MariaDB. Đã import với chữ
hoa rồi thì phải import lại.

**Server chạy vài tiếng rồi tự tắt, log chỉ có `Killed`**
→ Hết RAM, kernel OOM-killer giết Java. Kiểm tra: `dmesg | grep -i "killed process"`.
Giảm `-Xmx`, giảm `innodb_buffer_pool_size`, hoặc nâng VPS.

**`java.net.BindException: Address already in use`**
→ Còn tiến trình cũ giữ cổng: `ss -lntp | grep 19129` rồi `systemctl stop kpah-game`.

**Client báo kết nối được nhưng đứng ở màn hình chờ**
→ Login server chưa chạy. Game server gọi `127.0.0.1:8023`.
`systemctl status kpah-login`.

**Vào game trắng màn hình / không thấy map**
→ Thiếu `map/` hoặc `cMap/`, hoặc sai hoa thường tên thư mục, hoặc user `kpah` không có
quyền đọc: `chown -R kpah:kpah /opt/kpah`.

**Tên nhân vật tiếng Việt thành `???`**
→ Charset. Kiểm tra `mysql -e "SHOW VARIABLES LIKE 'character_set_server';"` phải là
`utf8mb4`. Code không ghi `characterEncoding` vào URL JDBC nên nó lấy theo mặc định của
server DB.

**Ghi log lỗi `FileNotFoundException /opt/kpah/log/...`**
→ Thiếu thư mục hoặc `sv.pathLog` không có dấu `/` ở cuối.
`mkdir -p /opt/kpah/log && chown kpah:kpah /opt/kpah/log`.

**Giờ chiếm thành / vận tiêu lệch**
→ Chưa đặt múi giờ (bước B4).

**`systemctl stop` treo rất lâu**
→ Đúng, nó đang lưu toàn bộ nhân vật online. Chờ. Đừng `kill -9`.

---

## Bảo mật

- Chuyển SSH sang xác thực bằng key, đổi cổng 22 sang cổng khác, tắt `PermitRootLogin`.
- MariaDB chỉ nghe `127.0.0.1` (mặc định của Ubuntu). Đừng mở 3306 ra ngoài.
- Chỉ mở 3 cổng: 22, 19129, 80. **Tuyệt đối không** mở 8023 và 19153.
- Game server chạy bằng user `kpah`, không phải `root`.
- Mật khẩu MariaDB nằm trong `server.ini` dạng chữ thường → `chmod 640 /opt/kpah/server.ini`
  và `chown kpah:kpah`.
- Đổi `sv.adminAccounts` trong `server.ini` nếu bạn đổi tên nhân vật admin. Tên chứa
  "admin" đã bị chặn không cho người chơi tạo.

---

## Chuyển đổi ngược lại nếu cần

Muốn quay về chạy ở nhà: chỉ cần sửa `NQSH2.txt` trên VPS trỏ về IP/tunnel của nhà, người
chơi bấm "Cập nhật" trong client. Nhớ dump DB từ VPS về trước:

```bash
mysqldump --single-transaction kpah_db | gzip > /root/kpah_db_tu_vps.sql.gz
```
