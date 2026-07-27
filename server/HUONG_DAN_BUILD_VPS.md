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

Code lấy từ **Git** (`github.com/hoanghoa0903/KPAH`, repo private). Database thì Git không
chứa nên vẫn phải dump tay — đó là toàn bộ nội dung phần này.

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

### A3. Đảm bảo mọi thứ đã push lên Git

Jar đang chạy **có nằm trong Git** (`KPAH2_patched.jar`), nên VPS chỉ cần `git pull` là có
jar mới — không phải `scp` từng lần. Quy trình mỗi lần sửa code:

```powershell
# 1. Build (đã làm) -> KPAH2_NEW.jar
# 2. Chạy capnhat.bat  -> KPAH2_patched.jar thành bản mới, server ở nhà chạy luôn bản đó
# 3. Đẩy lên Git
git add -A
git commit -m "mo ta thay doi"
git push
```

Kiểm tra đã push xong:

```powershell
git status -sb          # phải là "## main...origin/main" không có ahead
```

**Bước 2 quan trọng.** VPS lấy `KPAH2_patched.jar` từ Git, nên file đó trong repo phải là
bản mới nhất. Nếu chỉ commit `KPAH2_NEW.jar` mà quên chạy `capnhat.bat`, VPS pull về vẫn
chạy jar cũ và bạn sẽ ngồi tìm xem code mới đi đâu mất.

### A4. Đẩy DB lên VPS

Database là thứ duy nhất phải `scp` (Git không chứa dữ liệu người chơi):

```powershell
scp kpah_db.sql account.sql root@180.93.57.30:/root/
```

---

## PHẦN B — Chuẩn bị VPS

Vào VPS: `ssh root@180.93.57.30` (Linux không có Remote Desktop, `mstsc` sẽ luôn báo lỗi).

### B1. Cập nhật + gói cơ bản

```bash
apt update && apt upgrade -y
apt install -y openjdk-8-jre-headless mariadb-server unzip ufw git
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

## PHẦN D — Lấy source từ Git về VPS

### D1. Tạo deploy key (khoá chỉ-đọc cho riêng VPS này)

Đừng dán mật khẩu GitHub hay token cá nhân lên VPS. Deploy key là khoá SSH chỉ có quyền
với **một repo**, và đặt được thành read-only — VPS bị chiếm cũng không sửa được repo.

```bash
sudo -u kpah mkdir -p /opt/kpah/.ssh
sudo -u kpah ssh-keygen -t ed25519 -N "" -f /opt/kpah/.ssh/id_ed25519 -C "vps-180.93.57.30"
cat /opt/kpah/.ssh/id_ed25519.pub
```

Copy dòng `ssh-ed25519 AAAA...` rồi vào
**github.com/hoanghoa0903/KPAH → Settings → Deploy keys → Add deploy key**:
Title `VPS 180.93.57.30`, dán key vào, **KHÔNG tích** *Allow write access*.

Nhận diện host GitHub (làm một lần, tránh câu hỏi yes/no lúc clone):

```bash
sudo -u kpah bash -c 'ssh-keyscan github.com >> /opt/kpah/.ssh/known_hosts'
sudo -u kpah ssh -T git@github.com          # "You've successfully authenticated" là xong
```

### D2. Clone vào `/opt/kpah`

`/opt/kpah` đang là home của user `kpah` và đã có `.ssh` bên trong, nên không clone trực
tiếp vào đó được (git đòi thư mục rỗng). Clone ra chỗ khác rồi chuyển vào:

```bash
sudo -u kpah git clone git@github.com:hoanghoa0903/KPAH.git /tmp/kpah-src
sudo -u kpah cp -a /tmp/kpah-src/. /opt/kpah/
rm -rf /tmp/kpah-src

mkdir -p /opt/kpah/log /opt/kpah/backup
chmod +x /opt/kpah/vps/*.sh
chown -R kpah:kpah /opt/kpah
```

Kiểm tra:

```bash
cd /opt/kpah
sudo -u kpah git log -1 --oneline      # phai ra commit moi nhat cua ban
ls -la                                  # KPAH2_patched.jar, lib/, map/, cMap/, loginServer/, vps/
du -sh map                              # ~16 MB
```

Tên thư mục **`cMap`** phải đúng chữ M hoa — Linux phân biệt hoa thường, `cmap` là server
không đọc được dữ liệu map. Git giữ đúng tên nên chỉ cần đừng tự đổi.

### D3. Tạo 2 file cấu hình (Git không có sẵn)

`server.ini` và `loginServer/server.ini` **nằm trong `.gitignore`** vì chứa mật khẩu DB.
Clone ra sẽ **không có** hai file này, server chạy sẽ chết ngay ở dòng đọc `server.ini`.
Trong repo đã có bản mẫu cho Linux, chỉ cần copy rồi sửa mật khẩu:

```bash
cd /opt/kpah
cp vps/server.ini.linux.example server.ini
cp vps/loginServer.server.ini.example loginServer/server.ini

# Doi DOI_MAT_KHAU_DB thanh mat khau tao o buoc C2 (4 cho trong server.ini, 1 cho o login)
sed -i 's/DOI_MAT_KHAU_DB/MAT_KHAU_MANH_CUA_BAN/g' server.ini loginServer/server.ini

chown kpah:kpah server.ini loginServer/server.ini
chmod 640 server.ini loginServer/server.ini
grep -n "password\|pathLog\|db.run" server.ini      # kiem tra lai
```

Bản mẫu [vps/server.ini.linux.example](vps/server.ini.linux.example) đã sửa sẵn cho Linux:
`sv.pathLog=/opt/kpah/log/` (có dấu `/` cuối), `db.run=/opt/kpah/vps/update.sh`, user DB
`kpah`, và giữ nguyên các tỉ lệ drop bạn đang dùng.

**Điểm hay của cách này:** `git pull` không bao giờ đè lên `server.ini`. Cấu hình riêng của
VPS — kể cả những gì bạn sửa bằng bảng setting trong game — được giữ nguyên qua mọi lần
cập nhật.

Các file `.txt` vận tiêu (`cuoptieu`, `doitieu`, `nhantieu`, `thetieu`, `tratieu`,
`matketinfo`) cũng bị `.gitignore` chặn nên clone ra không có — **không sao**, chúng chỉ là
log server tự ghi thêm, sẽ tự tạo khi chạy.

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

### Cập nhật server (luồng Git)

**Máy nhà:**

```powershell
# build -> KPAH2_NEW.jar, rồi:
.\capnhat.bat            # KPAH2_patched.jar = bản mới, server nhà chạy luôn để test
git add -A
git commit -m "mo ta thay doi"
git push
```

**VPS:**

```bash
sudo /opt/kpah/vps/update.sh
```

[vps/update.sh](vps/update.sh) làm đúng thứ tự an toàn: kiểm tra repo trên VPS có bị sửa tay
không (nếu có thì dừng và chỉ cách xử lý, chứ không pull nửa vời), tắt game server bằng
SIGTERM để shutdown hook lưu dữ liệu, `git pull --ff-only`, kiểm tra jar vừa kéo về có hỏng
không (`unzip -t`), `chown` lại, bật lại 2 service, in trạng thái + kiểm tra cổng 19129.
Nếu pull hoặc jar lỗi, nó **để server tắt** và nói rõ — thà tắt còn hơn chạy bản hỏng.

Đổi ý muốn quay lại bản trước:

```bash
cd /opt/kpah
sudo -u kpah git log --oneline -5          # chọn commit cũ
systemctl stop kpah-game
sudo -u kpah git checkout <ma_commit> -- KPAH2_patched.jar
systemctl start kpah-game
```

> **Cách 2 — không qua Git**, khi cần thử nhanh một bản jar chưa muốn commit:
> `scp KPAH2_NEW.jar kpah@180.93.57.30:/opt/kpah/` rồi `sudo /opt/kpah/vps/capnhat.sh`.
> Nhưng nhớ là lần `update.sh` sau sẽ báo `KPAH2_patched.jar` bị sửa tay và dừng lại —
> lúc đó chạy `sudo -u kpah git -C /opt/kpah checkout -- KPAH2_patched.jar` rồi update lại.

### Backup DB tự động

```bash
printf '[client]\nuser=root\npassword=MAT_KHAU_ROOT_MYSQL\n' > /root/.my.cnf
chmod 600 /root/.my.cnf
crontab -e
```

Thêm dòng:

```
0 4 * * * /opt/kpah/vps/backup_db.sh >> /opt/kpah/log/backup.log 2>&1
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

Bình thường nên build ở máy nhà rồi push Git. Nhưng `src/` cũng nằm trong repo nên build
ngay trên VPS được:

```bash
apt install -y openjdk-8-jdk
```

`src/` đã có sẵn sau khi clone, chạy luôn:

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
sudo /opt/kpah/vps/capnhat.sh          # doi jar tai cho, KHONG qua git
```

Build kiểu này làm `KPAH2_patched.jar` khác với bản trong Git → lần `update.sh` sau sẽ báo
"có file bị sửa tay" và dừng. Xử lý: `sudo -u kpah git -C /opt/kpah checkout -- KPAH2_patched.jar`
rồi `update.sh` lại. Vì vậy chỉ nên build trên VPS để **thử gấp**; sửa xong thì đưa code về
máy nhà, build, commit, push cho hai bên khớp nhau.

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

**`bash: /opt/kpah/vps/update.sh: /bin/bash^M: bad interpreter`**
→ File `.sh` bị lưu kiểu CRLF của Windows. Repo đã có [.gitattributes](.gitattributes) ép
`*.sh eol=lf` nên không xảy ra nữa; nếu vẫn gặp (file cũ đã commit trước đó):
`sed -i 's/\r$//' /opt/kpah/vps/*.sh`.

**`Permission denied` khi chạy `update.sh`**
→ Git trên Windows không lưu quyền thực thi. Đã set sẵn bit `+x` trong index cho 3 script,
nếu vẫn thiếu: `chmod +x /opt/kpah/vps/*.sh`.

**`git@github.com: Permission denied (publickey)`**
→ Chưa thêm deploy key vào GitHub, hoặc chạy git bằng user khác (`root` không có key của
`kpah`). Luôn chạy `sudo -u kpah git -C /opt/kpah ...`.

**`error: Your local changes to the following files would be overwritten by merge`**
→ Trên VPS có file bị sửa tay (thường là `KPAH2_patched.jar` do chạy `capnhat.sh` thủ công).
Bỏ thay đổi đó: `sudo -u kpah git -C /opt/kpah checkout -- <file>`, hoặc lấy sạch theo Git:
`sudo -u kpah git -C /opt/kpah fetch origin && sudo -u kpah git -C /opt/kpah reset --hard origin/main`
(`server.ini` và `log/` không bị ảnh hưởng vì nằm trong `.gitignore`).

**Server chạy được ở nhà nhưng trên VPS báo `server.ini (No such file or directory)`**
→ Bình thường: `server.ini` bị `.gitignore` chặn. Làm bước D3.

**`git pull` xong mà code mới không có tác dụng**
→ Bạn quên chạy `capnhat.bat` ở nhà nên `KPAH2_patched.jar` trong Git vẫn là bản cũ.
Kiểm tra trên VPS: `unzip -p KPAH2_patched.jar META-INF/MANIFEST.MF` và so ngày commit
`git log -1 --format=%cd -- KPAH2_patched.jar`.

---

## Bảo mật

### Repo phải giữ private

Mình kiểm tra `api.github.com/repos/hoanghoa0903/KPAH` trả về 404 khi không đăng nhập →
repo **đang private**, đúng như cần. Giữ nguyên như vậy, vì trong repo có:

- `lib/NQSH_5h.jar` — thư viện đóng của người khác, không phải code của bạn để phát hành
- `backup_account.sql` — có `INSERT INTO team_user` kèm **hash mật khẩu thật** của tài khoản
  `admin` (dạng `*A4B61573...` của hàm `PASSWORD()`, băm yếu, dò được)
- `backup_kpah_db_truoc_import.sql` — dữ liệu game

Nếu định mở public sau này, xoá 2 file dump khỏi **toàn bộ lịch sử** (không phải chỉ commit
mới nhất) bằng `git filter-repo`, và đổi mật khẩu tài khoản admin trước.

Deploy key trên VPS để **read-only** (không tích *Allow write access*): VPS bị chiếm cũng
không đẩy được code độc vào repo.

### Còn lại

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
