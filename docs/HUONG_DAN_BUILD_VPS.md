# Dựng server HTBB chạy hẳn trên VPS `180.93.57.30` (Ubuntu 20.04)

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

**2. RAM là thứ phải tính trước.**
Cần cùng lúc: game server (heap + ~250 MB phụ trợ của JVM), login server (~50 MB),
MariaDB (~250 MB), Apache/PHP (~100 MB), hệ điều hành (~150 MB).

| RAM VPS | `-Xmx` game server | Swap | Đủ cho |
|---|---|---|---|
| 1 GB | 512m | 2 GB **bắt buộc** | ~20-30 người, khá sát trần |
| **2 GB** | **1g** | 1 GB nên có | vài trăm người, thoải mái |
| 4 GB | 2g | không cần | |

Hết RAM thì kernel giết tiến trình Java, log chỉ ghi `Killed` — không có thông báo nào
giải thích, nên nói trước để bạn không mất buổi tối đi tìm nguyên nhân. Chi tiết ở bước
B2 và B2b.

Ngoài ra: DB game của bạn đang **79 MB / 84 bảng**, thư mục `map/` **16 MB** → upload
tổng khoảng 100 MB, vài phút là xong.

---

## PHẦN A — Chuẩn bị ở máy nhà (Windows)

Code lấy từ **Git** (`github.com/hoanghoa0903/KPAH`, repo private).

Database thì **không cần dump gì cả** — server mới bắt đầu trắng tinh, và 2 file cần thiết
đã nằm sẵn trong repo:

| File trong repo | Là gì |
|---|---|
| `sql/gamedb7_clean.sql` | 84 bảng + **toàn bộ dữ liệu game** (914 item, 138 monster, 162 bình, 148 shop, 49 quest, 198 attribute, 147 từ khoá lọc), **không có** một nhân vật/tài khoản nào |

> Làm mới từ VPS ngày **02/09/2026**: `data_item` 904 → **914** món (5 skin linh thú id
> 905-909, Phi phong vô hình id 910, 5 skin id 950-954) và `data_attribute` 141 → **198**
> dòng (id 0..186 liên tục + khối 200..210). Các bảng còn lại là bản chụp cũ nhưng **đã đối
> chiếu khớp production** (138 monster, 162 bình, 148 shop, 49 quest).
>
> Nhờ `data_attribute` đủ 187 dòng đầu, server dựng mới **không còn phải chạy tay** khối
> PHẦN 2C nữa (xem mục cạm bẫy phía dưới). `test/KiemBangThuocTinh.java` ghim điều kiện
> id 0..186 liên tục, để lần dump lại sau không âm thầm làm mất nó.
| `loginServer/account.sql` | cấu trúc bảng `team_user` |
| `vps/db_new_server.sql` | script dọn sạch + tạo tài khoản admin, mình vừa viết |

Nên PHẦN A gần như không có việc gì. Chỉ cần chắc mọi thứ đã push (bước A3).

### A1. Nếu sau này muốn mang dữ liệu cũ sang (tuỳ chọn, KHÔNG cần cho server mới)

Tắt game server (Admin Panel → **Bảo Trì**) và `loginServer`, rồi:

```powershell
& "C:\xampp\mysql\bin\mysqldump.exe" -u root --single-transaction --routines --events kpah_db > kpah_db.sql
& "C:\xampp\mysql\bin\mysqldump.exe" -u root --single-transaction --routines --events account > account.sql
scp kpah_db.sql account.sql root@180.93.57.30:/root/
```

Import 2 file này thay cho bước C3, rồi vẫn chạy `vps/db_new_server.sql` nếu muốn xoá
người chơi nhưng giữ mọi thiết lập khác.

### A2. Đặt mật khẩu admin cho server mới

Mở [vps/db_new_server.sql](../vps/db_new_server.sql), sửa 2 dòng đầu:

```sql
SET @admin_user = 'admin';
SET @admin_pass = '';
```

Không sửa thì script **dừng ngay từ đầu và không xoá gì** — cố ý làm vậy để bạn không tạo
ra tài khoản admin với mật khẩu mặc định.

### A3. Đảm bảo mọi thứ đã push lên Git

Jar đang chạy **có nằm trong Git** (`KPAH2_patched.jar`), nên VPS chỉ cần `git pull` là có
jar mới — không phải `scp` từng lần. Quy trình mỗi lần sửa code:

```powershell
# 1. Build (đã làm) -> HTBB_NEW.jar
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
bản mới nhất. Nếu chỉ commit `HTBB_NEW.jar` mà quên chạy `capnhat.bat`, VPS pull về vẫn
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

**Kiểm tra thật sự cài được** — dòng `apt install` hay bị `unattended-upgrades` giữ khoá
`dpkg` lúc VPS mới dựng và **thất bại im lặng**, sau đó bạn sẽ gặp
`Directory '/etc/mysql/mariadb.conf.d' does not exist` (thiếu MariaDB) hoặc
`Failed at step EXEC spawning /usr/bin/java: No such file or directory` (thiếu Java):

```bash
which java unzip ufw git
mysqld --version
```

Thiếu cái nào thì `apt install -y <tên gói>` lại cái đó. Báo `Could not get lock
/var/lib/dpkg/lock-frontend` thì chờ 2-3 phút rồi chạy lại.

**Nếu VPS này từng chạy `frps`** theo [HUONG_DAN_VPS_FRP.md](HUONG_DAN_VPS_FRP.md) thì phải
tắt, vì nó đang chiếm đúng cổng 19129 mà game server cần:

```bash
ss -lntp | grep 19129
systemctl disable --now frps 2>/dev/null
ss -lntp | grep 19129        # phai trong
```

Bỏ bước này thì game server không bind được cổng, mà systemd vẫn báo `active (running)`.

Java 8 là đủ: jar server biên dịch ở class version 52 (Java 8), thư viện `NQSH_5h.jar` là
version 50 (Java 6). Máy có Java 11/17 sẵn cũng chạy được, nhưng 8 là bản khớp nhất.
Kiểm tra: `java -version`.

### B2. Swap

**Kiểm tra trước** — nhiều nhà cung cấp đã tạo swap sẵn, tạo thêm là thừa:

```bash
free -h
swapon --show
df -h /            # còn đủ dung lượng đĩa cho file swap không
```

Nếu dòng `Swap:` đã khác 0 thì bỏ qua bước này.

| RAM VPS | Swap nên tạo | Vì sao |
|---|---|---|
| **1 GB** | **2 GB — bắt buộc** | Java + MariaDB + Apache cộng lại vượt 1 GB. Không có swap là kernel giết tiến trình Java giữa đêm |
| **2 GB** | **1 GB — nên có** | Vừa đủ chạy, swap chỉ làm lưới an toàn cho lúc cao điểm / lúc `mysqldump` backup / lúc `javac` build trên VPS |
| 4 GB+ | không cần | |

Lệnh (đổi `1G` thành `2G` nếu VPS 1 GB):

```bash
fallocate -l 1G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
sysctl -w vm.swappiness=10
echo 'vm.swappiness=10' >> /etc/sysctl.conf
free -h        # phải thấy dòng Swap khác 0
```

`vm.swappiness=10` là phần quan trọng, đừng bỏ. Swap ở đây để **chống bị kernel giết**, chứ
không phải để chạy thường xuyên: nếu heap của Java bị đẩy xuống swap thì người chơi thấy
giật/lag từng đợt — tệ hơn cả việc không có swap. Đặt 10 nghĩa là kernel chỉ dùng swap khi
thực sự bí.

### B2b. VPS 2 GB — KHÔNG làm gì ở đây, chỉ ghi nhớ 2 con số

Hai chỗ cần nới cho VPS 2 GB **nằm ở bước sau**, đừng cố sửa lúc này (file service chưa
tồn tại, repo mới clone ở PHẦN D — chạy `sed` bây giờ chỉ báo *No such file*):

| Cần đổi | Từ → đến | Làm ở bước |
|---|---|---|
| `innodb_buffer_pool_size` | 128M → **256M** | **C1**, lúc sửa `50-server.cnf` (guide đã ghi sẵn 256M) |
| `-Xmx` game server | 512m → **1g** | **D4**, ngay trước khi copy file service |

**Đừng đặt `-Xmx` vượt 1 GB trên VPS 2 GB.** Ngoài heap, JVM còn ăn thêm metaspace, stack
của từng thread, bộ đệm GC — thực tế tốn hơn `-Xmx` khoảng 200–300 MB. Cộng MariaDB ~250 MB
+ Apache ~100 MB + hệ điều hành ~150 MB là đã sát 2 GB.

Sau khi chạy được một ngày, đo thật rồi điều chỉnh:

```bash
ps -o rss=,cmd= -C java | awk '{printf "%.0f MB  %s\n", $1/1024, $2}'
free -h
grep -i "killed process" /var/log/syslog        # trống = chưa từng bị OOM-kill
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

> **Thứ tự chạy:** làm C0 → C1 → C2, rồi **nhảy sang PHẦN D** để clone code, sau đó quay
> lại C3 + C4. Lý do: 3 file SQL cần import đều nằm trong repo, phải clone xong mới có.

### C0. Kiểm tra đã cài đúng MariaDB chưa

Làm bước này trước, đừng bỏ qua. Nếu `nano` báo
`[ Directory '/etc/mysql/mariadb.conf.d' does not exist ]` thì gần như chắc chắn MariaDB
chưa cài, hoặc cài nhầm MySQL.

```bash
mysqld --version                      # phai co chu "MariaDB"
systemctl status mariadb --no-pager | head -5
dpkg -l | grep -E "mariadb-server|mysql-server" | awk '{print $1,$2,$3}'
ls /etc/mysql/
```

Đọc kết quả theo bảng này:

| Thấy gì | Nghĩa là | Làm gì |
|---|---|---|
| `mysqld: command not found`, `/etc/mysql/` không có | **Chưa cài gì** | `apt update && apt install -y mariadb-server` |
| Có `/etc/mysql/mariadb.conf.d/` | Đúng MariaDB | đi tiếp C1 |
| Có `/etc/mysql/mysql.conf.d/` và `mysqld --version` ghi `MySQL 8.x` | **Cài nhầm MySQL 8** — sẽ không ai đăng nhập được vì mất hàm `PASSWORD()` | xem khối lệnh dưới |
| `ii mariadb-server` nhưng thiếu thư mục | Cài dở/lỗi giữa đường | `apt install --reinstall -y mariadb-server` |

Gỡ MySQL 8 và cài MariaDB (chỉ chạy khi rơi vào trường hợp cài nhầm — **xoá luôn dữ liệu
MySQL đang có**, lúc này chưa import gì nên không mất gì):

```bash
systemctl stop mysql
apt purge -y "mysql-server*" "mysql-client*" "mysql-community*"
apt autoremove -y
rm -rf /var/lib/mysql /etc/mysql
apt update && apt install -y mariadb-server
mysqld --version                      # gio phai thay "MariaDB"
```

Vì sao phải là MariaDB: đọc lại mục "hai điều quyết định thành/bại" ở đầu file.

### C1. Cấu hình trước khi import

**KHÔNG sửa `50-server.cnf`.** MariaDB đọc mọi file `.cnf` trong `mariadb.conf.d/`, file số
lớn hơn thắng — nên tạo file riêng `99-kpah.cnf`. Hai cái lợi: không cần mở editor (dán một
khối là xong, không sợ gõ lẫn vào file mặc định), và `apt upgrade` có thay `50-server.cnf`
thì cấu hình của bạn vẫn còn nguyên.

Dán **một dòng** này (dài nhưng là một dòng — dán xong Enter là chạy ngay):

```bash
printf '[mysqld]\nlower_case_table_names = 1\ncharacter-set-server = utf8mb4\ncollation-server = utf8mb4_general_ci\ninnodb_buffer_pool_size = 256M\nperformance_schema = OFF\nskip-name-resolve\nmax_connections = 200\n' > /etc/mysql/mariadb.conf.d/99-kpah.cnf
```

> **Vì sao không dùng `cat > file <<'EOF'`.** Kiểu heredoc bắt shell chờ tới khi gặp dòng chỉ
> có `EOF`; dán qua PuTTY dễ bị mất dòng cuối, lúc đó dấu nhắc đổi thành `>` và **mọi lệnh
> bạn gõ tiếp đều bị ghi vào file thay vì chạy** — không có output nào, rất dễ tưởng là treo.
> Thấy dấu nhắc `>` thì bấm `Ctrl+C` rồi làm lại. `printf` một dòng thì không có trạng thái
> chờ đó.

Ý nghĩa từng dòng vừa ghi:

| Thiết lập | Vì sao cần |
|---|---|
| `lower_case_table_names = 1` | Linux phân biệt hoa thường tên bảng, Windows thì không → tránh lỗi `Table doesn't exist`. **Phải đặt TRƯỚC khi import** |
| `character-set-server = utf8mb4` | tên nhân vật tiếng Việt không thành `???` |
| `innodb_buffer_pool_size` | 1 GB → 128M, **2 GB → 256M**, 4 GB → 512M; không để MariaDB ăn hết RAM của Java |
| `performance_schema = OFF` | tiết kiệm ~80 MB RAM, server nhỏ không cần |
| `skip-name-resolve` | không tra DNS ngược mỗi lần kết nối, đăng nhập nhanh hơn |
| `max_connections = 200` | phải cao hơn tổng pool: 100 + 30 + 1 + login 8 + web |

Xem lại nội dung file rồi mới khởi động lại:

```bash
cat /etc/mysql/mariadb.conf.d/99-kpah.cnf
systemctl enable mariadb
systemctl restart mariadb
```

`max_connections` phải cao hơn tổng pool trong `server.ini` (`db.maxco=100` + `db.maxNap=30`
+ `db.maxco1=1` + login server 8 + web) — thiếu là giờ đông người sẽ ăn lỗi
*Too many connections*.

**Kiểm tra cấu hình đã có tác dụng** (bước này đừng bỏ — sửa file mà server không đọc là
lỗi rất khó thấy):

Mỗi lệnh là **một dòng**, chạy xong dòng này mới sang dòng kế tiếp:

```bash
mysql -u root -e "SHOW VARIABLES WHERE Variable_name IN ('lower_case_table_names','character_set_server','innodb_buffer_pool_size','max_connections');"
mysql -u root -e "SELECT PASSWORD('abc');"
```

> Dán lệnh `mysql -e "..."` bị **xuống dòng giữa hai dấu ngoặc kép** cũng làm shell chờ tiếp
> và hiện dấu nhắc `>`. Cùng một cái bẫy như heredoc: `Ctrl+C` rồi dán lại thành một dòng.
>
> Đừng gõ mật khẩu thật vào `SELECT PASSWORD(...)` — nó vào `~/.bash_history`. Dùng `'abc'`
> để thử là đủ, ta chỉ cần biết hàm đó còn tồn tại.

Phải thấy `lower_case_table_names = 1`, `character_set_server = utf8mb4`,
`max_connections = 200`, và lệnh cuối ra một chuỗi `*XXXX...`. Lệnh cuối là bài kiểm tra
quan trọng nhất của cả phần database: có `PASSWORD()` thì login server mới xác thực được.

### C2. Tạo database + user

```bash
mysql -u root
```

```sql
CREATE DATABASE kpah_db DEFAULT CHARSET utf8mb4;
CREATE DATABASE account DEFAULT CHARSET utf8mb4;

-- Mat khau: doi thanh chuoi cua ban, se dung o server.ini
CREATE USER 'kpah'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON kpah_db.* TO 'kpah'@'localhost';
GRANT ALL PRIVILEGES ON account.* TO 'kpah'@'localhost';

-- BAT BUOC phai co ca 3 dong nay, xem giai thich ben duoi
CREATE USER 'kpah'@'127.0.0.1' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON kpah_db.* TO 'kpah'@'127.0.0.1';
GRANT ALL PRIVILEGES ON account.* TO 'kpah'@'127.0.0.1';

FLUSH PRIVILEGES;
EXIT;
```

**Vì sao phải cấp quyền cho cả `localhost` và `127.0.0.1`.** `99-kpah.cnf` có
`skip-name-resolve` nên MariaDB không phân giải tên host nữa. Lúc đó `localhost` chỉ khớp
kết nối qua **unix socket**, còn kết nối **TCP** — đúng cái JDBC dùng
(`jdbc:mysql://localhost:3306/...`) — bị coi là đến từ host `127.0.0.1`. Thiếu dòng
`@'127.0.0.1'` thì:

| Ai kết nối | Đường đi | Kết quả |
|---|---|---|
| `mysql -u kpah -p...` (CLI) | unix socket → khớp `@'localhost'` | **chạy tốt** → làm bạn tưởng đã đúng |
| Game server / login server (Java) | TCP → cần `@'127.0.0.1'` | `Access denied`, pool DB rỗng |

Pool rỗng thì server ném `java.util.NoSuchElementException` ở `ConnPool.getConFromPool`,
rồi `ArrayIndexOutOfBoundsException: 0 >= 0` ở `loadAllTemplate`, và luồng `main` chết —
nhưng systemd vẫn báo `active (running)` vì JVM còn sống. Rất dễ mất thời gian.

Kiểm tra đúng đường mà Java đi — **phải có `-h 127.0.0.1`**, thiếu là nó lại chạy qua
socket và bài test thành vô nghĩa:

```bash
mysql -u kpah -p'' -h 127.0.0.1 -e "SHOW DATABASES;"
```

### C3. Import (làm SAU khi đã clone code ở PHẦN D — 3 file đều nằm trong repo)

```bash
cd /opt/kpah
mysql -u root kpah_db < sql/gamedb7_clean.sql
mysql -u root account < loginServer/account.sql
mysql -u root --default-character-set=utf8 -t < vps/db_new_server.sql
```

Lệnh thứ ba in ra bảng kiểm tra. **Đọc kỹ bảng đó trước khi bật server** — cột `phai_la`
nói rõ giá trị đúng:

```
+-------------------------------+---------+-----------+
| bang                          | so_dong | phai_la   |
+-------------------------------+---------+-----------+
| tob_char (nhan vat)           |       0 | 0         |
| tob_animal / tob_pet          |       0 | 0         |
| tob_clan / tob_market         |       0 | 0         |
| giftcode_log / board_created  |       0 | 0         |
| --- du lieu game phai CON --- |         |           |
| data_item                     |     904 | ~904      |
| data_monster                  |     138 | 138       |
| data_potion                   |     162 | 162       |
| data_shop                     |     148 | 148       |
| data_quest                    |      49 | 49        |
| data_attribute                |     187 | 187       |
| tob_tubinh (template)         |      16 | 16        |
| bad_word                      |     147 | 147       |
| --- cot luu hanh trang ---    |         |           |
| cot da la mediumtext          |       7 | 7         |
| --- tai khoan ---             |         |           |
| account.team_user             |       1 | 1 (admin) |
| 5h_active                     |       1 | 1         |
+-------------------------------+---------+-----------+
```

[vps/db_new_server.sql](../vps/db_new_server.sql) làm 6 việc: chặn lại nếu bạn chưa đặt mật
khẩu admin (chặn **trước khi** xoá bất cứ thứ gì), xoá sạch mọi bảng dữ liệu người chơi
(đọc từ `information_schema` nên không sót bảng log xoay vòng theo tháng), giữ nguyên 16
bảng dữ liệu game, **bù 2 thứ mà bản dump sạch bị thiếu** (xem dưới), xoá sạch tài khoản,
rồi tạo 1 tài khoản admin đã kích hoạt sẵn. Chạy lại nhiều lần không hỏng gì.

**Ba thứ bản dump sạch bị thiếu** — mình đối chiếu từng bảng giữa DB đang chạy và bản dump
mới tìm ra, script đã bù sẵn:

1. **`tob_tubinh` — template tứ bình 16 dòng** (Mai/Lan/Cúc/Trúc → Long/Lân/Quy/Phụng).
   Tên bắt đầu bằng `tob_` nhưng đây là **dữ liệu game**, không phải của người chơi. Để
   trống thì nhân vật lv ≥ 40 vào game sẽ ném `Duplicate entry ... for key PRIMARY` ở
   `addCharTubinh` và `ArrayIndexOutOfBounds` ở `createMsgCharTuBinh`.
2. **`history_win_xoso`** — [Database.java:1619](../src/data/Database.java#L1619) `INSERT` vào
   bảng này nhưng không có lệnh tạo bảng, dump cũng không có. Thiếu là ai thắng xổ số sẽ ăn
   `Table doesn't exist`.
3. **4 cột `tob_char.equip/inven/bag/tuido` còn là `TEXT`** (trần 65.535 byte) và 3 cột
   `tob_gem_new` còn là `varchar(1000)` — PHẦN 2D nới hết sang `MEDIUMTEXT`. Cả hành trang
   được nhét vào MỘT chuỗi trong `inven`, mỗi món ~470 byte kể từ khi `otherAtt` nới lên 126
   ô cho Thần Khí, mà túi chứa tới 42 × 5 = 210 ô → chỉ ~139 món là tràn. Tràn thì hoặc
   `UPDATE` ném `Data too long for column 'inven'` (sql_mode có `STRICT_TRANS_TABLES`) làm
   **lần lưu đó không lưu gì cả**, hoặc MySQL **cắt cụt âm thầm** phần đuôi. Cả hai đều ra
   triệu chứng "tạo món xong, logout vào lại là mất".

Còn bảng `tob_train_luong` (hạn mức lượng train/ngày) cũng không có trong dump nhưng
**không cần làm gì** — server tự `CREATE TABLE IF NOT EXISTS` lúc khởi động.

Nếu import báo lỗi *Unknown collation* thì bản MariaDB trên VPS cũ hơn ở nhà — sửa nhanh:
`sed -i 's/utf8mb4_0900_ai_ci/utf8mb4_general_ci/g; s/utf8mb3/utf8/g' sql/gamedb7_clean.sql`
rồi import lại.

### C4. Tài khoản và nhân vật admin

Script ở C3 đã tạo tài khoản `admin` (mật khẩu bạn đặt) và thêm dòng kích hoạt vào
`5h_active` nên đăng nhập được ngay — không cần sửa `sv.EnableAvtiveCheck`.

**Việc còn lại phải làm trong game:** quyền admin xét theo **TÊN NHÂN VẬT**, không phải tên
tài khoản (`server.ini` → `sv.adminAccounts=admin,admin2`). Nên sau khi vào game bằng tài
khoản `admin`, tạo nhân vật đặt tên đúng **`admin`** thì mới có quyền. Người chơi thường
không giành được tên này: mọi tên chứa chữ "admin" đã bị chặn ở
[Database.java:14632](../src/data/Database.java#L14632).

Người chơi mới đăng ký qua web (PHẦN F): `config.php` để `AUTO_ACTIVATE = true` là đăng ký
xong vào game được luôn. Nếu bạn tạo tài khoản bằng tay trong DB thì phải tự thêm dòng
`5h_active`:

```sql
INSERT INTO kpah_db.5h_active (userID, username, time_end)
SELECT id, username, UNIX_TIMESTAMP() + 2592000 FROM account.team_user
WHERE id NOT IN (SELECT userID FROM kpah_db.5h_active);
```

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
sudo -u kpah git clone git@github.com:hoanghoa0903/KPAH.git /tmp/htbb-src
sudo -u kpah cp -a /tmp/htbb-src/. /opt/kpah/
rm -rf /tmp/htbb-src

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

Bản mẫu [vps/server.ini.linux.example](../vps/server.ini.linux.example) đã sửa sẵn cho Linux:
`sv.pathLog=/opt/kpah/log/` (có dấu `/` cuối), `db.run=/opt/kpah/vps/update.sh`, user DB
`kpah`, và giữ nguyên các tỉ lệ drop bạn đang dùng.

**Điểm hay của cách này:** `git pull` không bao giờ đè lên `server.ini`. Cấu hình riêng của
VPS — kể cả những gì bạn sửa bằng bảng setting trong game — được giữ nguyên qua mọi lần
cập nhật.

Các file `.txt` vận tiêu (`cuoptieu`, `doitieu`, `nhantieu`, `thetieu`, `tratieu`,
`matketinfo`) cũng bị `.gitignore` chặn nên clone ra không có — **không sao**, chúng chỉ là
log server tự ghi thêm, sẽ tự tạo khi chạy.

### D3b. Quay lại làm C3 + C4 (import database)

Giờ đã có code, quay lên **PHẦN C3** import 3 file SQL rồi mới cài service. Bật server khi
database còn rỗng thì nó sẽ chạy nhưng không ai đăng nhập được.

### D4. Cài 2 service

```bash
# VPS 2 GB: nang heap 512m -> 1g TRUOC khi copy (bo qua neu VPS 1 GB)
sed -i 's/-Xmx1g/-Xmx1g/' /opt/kpah/vps/kpah-game.service
grep Xmx /opt/kpah/vps/kpah-game.service        # kiem tra lai

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

Client đã trỏ đúng IP: [ServerListScr.cs:36](../ClientUnity/Assets/Scripts/Game/ServerListScr.cs#L36)
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

Chạy client, chọn Server 1, đăng nhập bằng tài khoản `admin` + mật khẩu bạn đặt ở A2.
Tạo nhân vật đặt tên **`admin`** để có quyền GM.

Kiểm tra server mới đã sạch và đủ:

| Thử | Đúng thì thấy |
|---|---|
| Bảng xếp hạng | trống trơn (chưa ai chơi) |
| Vào shop NPC | có đủ hàng (`data_shop` 148 dòng) |
| Đánh quái | quái ra đúng (`data_monster` 138 dòng) |
| Gõ `setting` bằng nhân vật `admin` | ra ô nhập lệnh → quyền GM đã đúng |
| Nhân vật lv ≥ 40 | mở tứ bình không lỗi (`tob_tubinh` 16 dòng) |

Đăng nhập được nhưng vào game trắng màn hình → thiếu `map/` hoặc `cMap/`.
Đăng nhập báo sai mật khẩu dù chắc chắn đúng → xem PHẦN J, gần như luôn là do cài MySQL 8
thay vì MariaDB.

---

## PHẦN H — Vận hành

### Cập nhật server (luồng Git)

**Máy nhà:**

```powershell
# build -> HTBB_NEW.jar, rồi:
.\capnhat.bat            # KPAH2_patched.jar = bản mới, server nhà chạy luôn để test
git add -A
git commit -m "mo ta thay doi"
git push
```

**VPS:**

```bash
sudo /opt/kpah/vps/update.sh
```

[vps/update.sh](../vps/update.sh) làm đúng thứ tự an toàn: kiểm tra repo trên VPS có bị sửa tay
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
> `scp HTBB_NEW.jar kpah@180.93.57.30:/opt/kpah/` rồi `sudo /opt/kpah/vps/capnhat.sh`.
> Nhưng nhớ là lần `update.sh` sau sẽ báo `KPAH2_patched.jar` bị sửa tay và dừng lại —
> lúc đó chạy `sudo -u kpah git -C /opt/kpah checkout -- KPAH2_patched.jar` rồi update lại.

### Deploy tự động: `git push` là VPS tự cập nhật

Đã bật. Máy nhà chỉ cần:

```powershell
.\capnhat.bat            # BẮT BUỘC: KPAH2_patched.jar mới là file VPS chạy
git add -A && git commit -m "..." && git push
```

Không phải SSH vào VPS nữa. [.github/workflows/deploy.yml](../.github/workflows/deploy.yml) chạy
sau mỗi push lên `main`, SSH vào VPS gọi [vps/deploy-auto.sh](../vps/deploy-auto.sh).

**Nó không restart bừa.** Script so danh sách file giữa commit cũ và mới:

| Commit đổi gì | Hành động |
|---|---|
| `KPAH2_patched.jar`, `lib/`, `libs/`, `map/`, `cMap/`, `loginServer/` | báo trước 60s trong game → tắt → pull → bật |
| còn lại (`.md`, `.sql`, `vps/*.sh`, `src/*.java`…) | chỉ `git pull`, **không ai bị kick** |

`src/*.java` cố ý **không** nằm trong nhóm phải restart: server chạy jar, sửa `.java` mà chưa
`capnhat.bat` thì restart cũng vô nghĩa — đúng cái bẫy ở [mục trên](#L104).

**Báo trước bảo trì 60 giây** hoạt động qua cờ DB, không cần ai bấm nút:

```sql
UPDATE kpah_db.5h_systems SET status = 0 WHERE command = 'baotri_thongbao';
```

`BaoTriDaily` đọc cờ này mỗi 5 giây ([Database.baoTriThongBaoCMD](../src/data/Database.java))
rồi chạy **dòng thông báo trên đỉnh màn hình** cho mọi người đang online
([BaoTriDaily.baoTruocBaoTri](../src/real/BaoTriDaily.java)) — đúng loại tin mà nút "Bảo Trì" của
AdminPanel dùng ([AdminPanel.java:711](../src/server/AdminPanel.java#L711)). Cờ **chỉ báo, không
tự tắt server** — việc tắt để cho `systemctl stop` (SIGTERM → shutdown hook lưu dữ liệu). Chạy
câu SQL trên bất cứ lúc nào cũng được nếu muốn báo tay mà không deploy.

> **Hai cái bẫy của dòng thông báo này, đã nổ cả hai:**
>
> **1. Đừng gửi nhiều mốc đếm ngược.** Client nhét tin 38 vào hàng đợi **tĩnh**
> `GameScr.serverInfo`, và `paintServerInfo` **chỉ chạy khi người chơi đang ở màn hình game**
> ([GameScr.cs:4737](../ClientUnity/Assets/Scripts/Game/GameScr.cs#L4737)), mỗi tin mất 15-20 giây mới trôi hết
> ([Ticker.cs](../ClientUnity/Assets/Scripts/Game/Ticker.cs)). Hàng đợi **không bị xoá khi mất kết nối** → tin nào chưa
> trôi kịp lúc tắt server sẽ chạy tiếp **ngay sau khi họ đăng nhập lại**, hiện *"bảo trì sau 1
> phút"* đúng lúc bảo trì đã xong. Bản đầu gửi 4 mốc 60/30/20/10 nên mốc 10s gần như chắc chắn
> còn đọng. Một tin gửi trước 60 giây thì trôi xong từ lúc còn 40 giây.
>
> **2. Đừng gán `RealController.maquee`.** `LoginHandler`/`SelectCharHandler` gửi lại maquee
> cho **mọi người mới đăng nhập** nếu nó khác rỗng
> ([LoginHandler.java:650](../src/real/cmd/LoginHandler.java#L650)) — đường tồn thông báo thứ hai.
> Dùng `MessageCreator.createthongbao(text)` (truyền chuỗi trực tiếp) thay cho
> `createServerInfoMessage()` (đọc maquee) là tránh được.
>
> Nếu sau này vẫn muốn chắc chắn 100% không đọng: tin **37**
> (`createServerAlertMessage`) bật `Canvas.startOKDlg` ngay, không qua hàng đợi
> ([MessageHandler.cs:2065](../ClientUnity/Assets/Scripts/Game/MessageHandler.cs#L2065)) — nhưng nó là hộp thoại phải bấm
> OK, không phải dòng chạy.

**Khoá SSH bị giới hạn.** Khoá riêng `/root/.ssh/deploy_github` khai trong `authorized_keys`
kèm `command="/opt/kpah/vps/deploy-auto.sh"` + `no-pty,no-port-forwarding`, nên nếu secret
trên GitHub bị lộ thì kẻ lấy được cũng **chỉ chạy được đúng script deploy**, không mở được
shell. Đặt secret: repo → Settings → Secrets and variables → Actions → `VPS_SSH_KEY` =
nội dung `ssh root@180.93.57.30 "cat /root/.ssh/deploy_github"`.

Xem chuyện gì đã xảy ra:

```bash
tail -n 40 /opt/kpah/log/autodeploy.log     # trên VPS
```

hoặc tab **Actions** trên GitHub — output của script được `tee` nên hiện luôn ở đó.
Chạy tay lúc nào cũng được: `sudo /opt/kpah/vps/deploy-auto.sh` (không có commit mới thì
nó không làm gì).

Hai chỗ script tự bảo vệ: `flock` để hai push liên tiếp không deploy chồng nhau, và `trap`
SIGHUP để SSH đứt giữa lúc server đang tắt thì nó bật lại chứ không để nằm im.

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

### Bật/tắt server từ web admin (không cần SSH)

Trang [web/admin/server.php](../web/admin/server.php) ghi "trạng thái mong muốn" vào bảng
`dieu_khien_server` — cần một tiến trình canh gác riêng chạy **ngoài** JVM game để đọc bảng đó
rồi tự gọi `systemctl start/stop kpah-game` thay bạn (bắt buộc phải tách riêng: lúc server đã tắt
hẳn thì không còn gì bên trong Java để mà tự poll DB nữa). Cài một lần:

```bash
mysql -u root kpah_db < sql/them_bang_dieu_khien_server.sql
crontab -e
```

Thêm dòng:

```
* * * * * /opt/kpah/vps/dieu-khien-server.sh >>/opt/kpah/log/dieu-khien-server.log 2>&1
```

Mỗi phút kiểm tra một lần — bấm nút trên web thì chậm nhất khoảng 1 phút mới có tác dụng. An toàn
vì `vps/dieu-khien-server.sh` chỉ gọi đúng hai lệnh cố định (`systemctl start kpah-game` /
`systemctl stop kpah-game`), tên service không bao giờ lấy từ dữ liệu đọc trong database. Cơ chế
này **khác hẳn** cờ báo bảo trì phía trên: cờ đó chỉ hoạt động khi server đang sống (server tự
thoát rồi để `Restart=always` của systemd bật lại), còn trang này điều khiển được cả lúc server đã
tắt hẳn.

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

**Chỉ 17 khoá nạp lại được**, mọi khoá khác bắt buộc `systemctl restart kpah-game`. Và một
cạm bẫy phải biết: sai **một** dòng trong `server.ini` là **mọi dòng bên dưới bị bỏ qua**
mà server vẫn khởi động, chạy với cấu hình dở dang — dấu hiệu duy nhất là dòng
`Load configuration file error!` trong log.

Bảng tra đầy đủ từng khoá: **[cau-hinh-server-ini.md](cau-hinh-server-ini.md)**.

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
cp KPAH2_patched.jar HTBB_NEW.jar
jar uf HTBB_NEW.jar -C out .
unzip -t HTBB_NEW.jar >/dev/null && echo "jar OK"
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
unzip -l HTBB_NEW.jar     | awk '{print $4}' | sort > /tmp/moi.txt
diff /tmp/cu.txt /tmp/moi.txt
```

Nếu VPS 1 GB, `javac` biên dịch 99 file cùng lúc có thể chạm trần RAM — có swap ở bước B2
thì vẫn xong, chỉ chậm.

---

## PHẦN J — Lỗi hay gặp

**Bấm vào Thần Khí là đơ client, server không ghi log gì**
→ Bảng `data_attribute` thiếu id 141..186. Bản dump `sql/gamedb7_clean.sql` chỉ có 141 dòng
(id 0..140), trong khi module Thần Khí dùng tới id 186. Tên thuộc tính tra cứu **theo vị
trí** trong danh sách nạp lúc khởi động (`otherAtt[i] -> ALL_NAME_ATTRIBUTE_ITEM.get(i+61)`,
xem [Item.java:1031](../src/real/Item.java#L1031) và [Map.java:15658](../src/real/Map.java#L15658)),
nên id phải liên tục. Gói đăng nhập cmd 25 chỉ gửi đúng số dòng đang có, client tra key
"163" ra null rồi chết giữa lúc vẽ — nên **client đơ mà server im lặng**.
Kiểm tra: `mysql -u root kpah_db -e "SELECT COUNT(*) FROM data_attribute;"` — phải **≥ 187**,
và quan trọng hơn là **id 0..186 không được thiếu cái nào** (số dòng đủ mà hổng id ở giữa thì
vẫn đơ). Kiểm đúng cách:

```bash
mysql -u root kpah_db -e "SELECT COUNT(*) FROM data_attribute WHERE id BETWEEN 0 AND 186;"
# phải ra đúng 187
```

**Từ 02/09/2026 việc này đã được sửa tận gốc:** `sql/gamedb7_clean.sql` giờ có đủ 198 dòng
(id 0..186 liên tục + khối 200..210, lấy từ VPS), nên server dựng mới từ bản seed đó **không
còn phải chạy tay** khối PHẦN 2C nữa. `test/KiemBangThuocTinh.java` ghim điều kiện liên tục
này để lần dump lại sau không âm thầm làm mất. Nếu dựng bằng
[vps/db_new_server.sql](../vps/db_new_server.sql) thì PHẦN 2C vẫn tự nạp như cũ.

Bẫy hay dính (vẫn còn đúng): máy Windows ở nhà đã vá tay từ trước nên test ở nhà thấy chạy
tốt, lên VPS mới lại đơ — nên hãy kiểm bằng câu lệnh trên trên **chính VPS**.

**Tạo món (Thần Khí, đồ giftcode...) xong, logout vào lại là mất món đó**
Ba nguyên nhân khác nhau, cùng một triệu chứng. Kiểm tra theo thứ tự:

1. **Cột `inven` còn là `TEXT`** (trần 65.535 byte) — xem PHẦN 2D ở
   [vps/db_new_server.sql](../vps/db_new_server.sql). Món mới nằm ở cuối chuỗi nên chết đầu tiên.
   ```bash
   mysql -u root kpah_db -e "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA='kpah_db' AND TABLE_NAME='tob_char'
       AND COLUMN_NAME IN ('equip','inven','bag','tuido');"
   ```
   Còn `text` thì chạy khối PHẦN 2D trong [vps/db_new_server.sql](../vps/db_new_server.sql). Xem ai đang sát
   trần: `SELECT charname, LENGTH(inven) FROM tob_char ORDER BY LENGTH(inven) DESC LIMIT 5;`

2. **Trần đọc lại 130 entry** trong `setitemInvenBag` —
   [Char.java:12074](../src/real/Char.java#L12074). Đã sửa: trần giờ là
   `Char.MAX_ENTRY_INVEN_LOAD` = 42 × 5 + 16 = 226, đủ cho 210 ô túi + 3 ô đặc biệt + 6 món
   model. Trước đó mọi entry từ vị trí 130 trở đi bị **xoá vĩnh viễn mỗi lần login** — mất
   luôn phần model dính ở cuối chuỗi. Ai còn chạy jar cũ thì vẫn dính; kiểm tra:
   ```bash
   mysql -u root kpah_db -e "SELECT charname,
     LENGTH(inven)-LENGTH(REPLACE(inven,'>',''))+1 AS so_entry FROM tob_char
     ORDER BY so_entry DESC LIMIT 5;"
   ```
   Số nào > 130 là nhân vật đang bị cắt.

3. **Món vẫn còn nhưng bị tháo ra khỏi ô trang bị.** Đây là *thiết kế hiện tại*, không phải
   lỗi tràn: 3 ô đặc biệt `itemHoaKyLan` / `itemVukhiThoiTrang` / `itemThanKhi` được ghi vào
   cột `inven` chung với hành trang ([Database.java:12685](../src/data/Database.java#L12685))
   nhưng lúc đọc lại thì mọi entry đều vào `iItems`
   ([Char.java:12214](../src/real/Char.java#L12214)) — không có cờ nào đánh dấu "đang đeo". Nên
   sau mỗi lần login phải bấm đeo lại. Muốn giữ nguyên trạng thái đeo thì phải thêm một giá
   trị `place` riêng cho từng ô (place 4 đang dành cho model) rồi khôi phục theo `place`.

**Thêm option vào menu NPC nhưng bấm vào không hiển thị gì, log cũng không có gì**
→ Bẫy lớn nhất khi sửa menu: lệnh `-30` (chọn option menu) **không đi thẳng vào
`Map.doMenu`**. Nó vào `RealController` trước ([RealController.java:1024](../src/real/RealController.java#L1024)),
ở đó có một `switch (idNpc)` với bản **copy** logic menu của một số NPC (-65, -800, -50,
-51, -49, -8). NPC nào có case riêng ở đây và **không** set `defHandle = true` thì
`Map.doMenu` vĩnh viễn không nhận được NPC đó — code viết trong `Map.doMenu` cho NPC ấy là
**code chết, không lỗi, không log, im lặng hoàn toàn**. Chỉ NPC rơi vào `default` mới được
`defHandle = true` và chuyển tiếp xuống `Map.processMessage` → `Map.doMenu`.
Cách kiểm tra trước khi sửa: `grep -n "case -<id>:" src/real/RealController.java` trong vùng
`case -30`. Có → sửa ở `RealController`; không có → sửa ở `Map.doMenu`.
Ví dụ thực tế: mục "Hỗ Trợ" của NPC tổng tiêu đầu (-50) đặt sai chỗ nên bấm không ra gì
(commit `67cf92d`). NPC ảo của menu con (-519, -1000, -1001) không có case nên vẫn chạy tốt
ở `Map.doMenu`.

**Nhân vật mất thân (chỉ còn cái đầu), hành trang và ô trang bị nhìn như rỗng**
→ Bảng `data_item` bị **lỗ id**. Client cấp phát mảng template theo **số lượng** nhưng ghi
vào mảng theo **id** ([MessageHandler.cs:1647](../ClientUnity/Assets/Scripts/Game/MessageHandler.cs#L1647)):

    int soLuong = readShort();
    ItemTemplate[] array = new ItemTemplate[soLuong + 1];
    ...
    array[readShort()] = new ItemTemplate();      // ghi theo ID

nên **id lớn nhất phải ≤ số lượng**. Xoá một dòng ở giữa bảng (ví dụ
`DELETE FROM data_item WHERE id=722` trong migration sách pet → potion, đã chạy xong và xoá)
làm mảng
ngắn đi một ô → template có id lớn nhất bị `IndexOutOfRange`, mà client **bắt lỗi rồi bỏ qua
âm thầm**. Chuỗi hậu quả rất khó lần vì nó không giống lỗi vật phẩm:

- `Res.getItem(id)` trả `null`
- `Char.setWearingInfo()` ([Char.cs:2804](../ClientUnity/Assets/Scripts/Game/Char.cs#L2804)) nổ NPE giữa vòng lặp — mà
  hàm này đặt `bodyStyle`/`legStyle`/`hatStyle`/`weaponStyle` **= -1 trước vòng lặp** → mọi
  style sau chỗ nổ giữ nguyên -1 → `paintAvatar` bỏ qua thân, chân, nón, vũ khí →
  **nhân vật chỉ còn cái đầu**
- `paintItemIcon` / `ItemInInventory.paint` nổ NPE lúc vẽ → **ô hành trang và ô trang bị
  nhìn như rỗng**

Thực tế đã gặp: bảng có id 1..904 (thiếu id 0) = 904 dòng, vừa khít. Xoá dòng 722 còn 903
dòng → mất đúng template **904 = "Cải Trang Sa Tăng" (type 0 = áo)** → ai đang mặc hoặc giữ
món đó là mất thân.

Đã vá hai lớp: server gửi **đủ id 0..max**, id trống thì gửi ô rỗng (`MessageCreator`, khối
"Bang template vat pham"), và PHẦN 2G trong [vps/db_new_server.sql](../vps/db_new_server.sql)
bù lại dòng đã xoá. Kiểm tra
nhanh bất cứ lúc nào:

```sql
SELECT COUNT(*) AS so_dong, MAX(id) AS id_lon_nhat FROM data_item;  -- so_dong phai >= id_lon_nhat
```

Quy tắc rút ra: **không bao giờ DELETE một dòng ở giữa `data_item`** — muốn bỏ vật phẩm thì
đổi nó thành loại trơ (type 24, không icon, không bán) và giữ dòng lại.

**Cả server rỗng hành trang và trang bị sau khi thêm sprite thú cưỡi mới**
→ Thêm id vào `MessageCreator.id_eff_tool_ngu_long` mà **thiếu file `map/d_effect/<id>`**
(file dữ liệu frame, không có đuôi). `DataGame` nạp file đó lúc khởi động và **bỏ qua lỗi
nếu thiếu**, để lại `dataDynamicEffect[id] = null`. Gói `26` lúc đăng nhập ghi theo thứ tự:

    monster → effDataLocal → id_eff_tool → id_eff_pet_tool → id_eff_tool_ngu_long
    → dx_horse_tool → Dx_Dy_WP_tool → Dx_Dy_PP_tool → Dx_Dy_AVT_tool

`writeShort(dataDynamicEffect[id].length)` với `null` là **NPE giữa lúc dựng gói**, cả khối
`try` nhảy ra ngoài nên **bốn bảng toạ độ cuối không được ghi**. Client nhận gói cụt, đọc
lệch rồi không vẽ được vật phẩm → **mọi người chơi thấy hành trang và bảng trang bị rỗng**,
nhưng nhân vật vẫn chạy trên map bình thường nên rất dễ nghi oan sang chỗ khác. Hai dấu
hiệu nhận ra ngay: lỗi xảy ra với **mọi nhân vật** (vì đây là gói dùng chung), và trong
console server có dòng NPE ở `createMsgMonsterTemplate`.

Từ 2026-08 ba khối này đã bỏ qua id thiếu dữ liệu và in cảnh báo `thieu map/d_effect/<id>`
thay vì làm cụt gói. Nhưng vẫn phải nhớ: **sửa/thêm file trong `map/` thì phải bật lại
server** (toàn bộ `map/` chỉ đọc một lần lúc khởi động), và khi deploy phải đẩy **cả file
`.png` lẫn file dữ liệu không đuôi** cùng nhau.

**Sách kỹ năng pet (potion 111) không có tên, không có icon**
→ `data_potion` chỉ được đọc **một lần lúc khởi động** ([Map.java:3030](../src/real/Map.java#L3030)),
không phải "mỗi lần hiện túi bình". Sửa mô tả trong DB xong thì phải `loadpotion` (lệnh chat
admin) **hoặc** bật lại server — và vì client nhận danh sách thuốc trong **gói đăng nhập**
(`LoginHandler.createMsgLogin`), người chơi còn phải **đăng nhập lại** mới thấy tên/icon mới.
Ảnh `map/images_potion/95.png` cũng chỉ nạp lúc khởi động.

Thêm một ràng buộc dễ quên: nhân vật có **162 ô thuốc** (`Char.SO_O_THUOC`, cột
`tob_char.potion` lưu 162 trường) nhưng client cấp phát ô thuốc theo **số dòng của
`data_potion`**. Bảng thiếu dòng là người chơi giữ thuốc id cao (sách pet là **111**) sẽ làm
client nổ giữa lúc đọc gói hành trang → rỗng sạch cả túi. `Char.getAllPotion()` đã chặn
(bỏ qua id vượt trần + ghi log), và PHẦN 2H trong [vps/db_new_server.sql](../vps/db_new_server.sql)
bù đủ dòng 0..161.

**Không ai đăng nhập được, log login server im lặng**
→ Bạn cài MySQL 8 chứ không phải MariaDB. `PASSWORD()` không còn tồn tại.
Kiểm tra: `mysql -u root -e "SELECT PASSWORD('abc');"` — báo lỗi hàm không tồn tại là đúng
bệnh. Cài lại MariaDB theo bước C0.

**`nano` báo `Directory '/etc/mysql/mariadb.conf.d' does not exist`**
→ MariaDB chưa cài (hoặc `apt install` ở B1 lỗi giữa đường, hoặc cài nhầm MySQL).
**Đừng `mkdir` thư mục đó** — tạo tay xong MariaDB vẫn không đọc file trong đó vì
`/etc/mysql/my.cnf` chưa có dòng `!includedir`. Chạy bước C0 để xác định rồi
`apt install -y mariadb-server`.

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

**`OutOfMemoryError: unable to create new native thread` ở `Region.startSandVilage`**
→ Server tạo **một thread cho mỗi region của mỗi map** — đo thực tế trên VPS này là
**2936 thread**. Hai thứ chặn nó, phải sửa cả hai (đã có sẵn trong
[vps/kpah-game.service](../vps/kpah-game.service)):
`TasksMax=infinity` (systemd mặc định chỉ ~2250, đếm cả thread) và `-Xss256k` (mặc định
mỗi thread 1 MB stack → 2936 thread cần ~2,9 GB, vượt RAM; 256k còn ~730 MB).
Kiểm tra số thread thật: `ls /proc/$(pgrep -f KPAH2_patched.jar)/task | wc -l`

**Cổng 19129 bị chiếm bởi `frps`**
→ VPS còn chạy frp từ hướng dẫn cũ. `systemctl disable --now frps`. Nhận biết:
`ss -lntp | grep 19129` ra `users:(("frps",...))` thay vì `java`.

**`Exception in thread "AWT-EventQueue-0" java.awt.HeadlessException`**
→ Vô hại, bỏ qua. Toàn bộ code AWT trong `src` đã được chặn bằng
`GraphicsEnvironment.isHeadless()`, dòng này đến từ thư viện đóng `NQSH_5h.jar` (không có
mã nguồn để sửa). Nó chỉ giết luồng AWT, server chạy bình thường.

**`SQL ERROR=CREATE TABLE tob_log_use_luong_5_7_2026 LIKE tob_log_use_luong`**
→ Vô hại. Đây là bảng log xoay vòng theo tuần/tháng; server tạo bằng `CREATE TABLE ... LIKE`
**không có** `IF NOT EXISTS`, và [Database.java:5793](../src/data/Database.java#L5793) chỉ in
`SQL ERROR=` chứ không in lý do. Lần khởi động đầu tạo xong rồi, nên mọi lần restart sau
trong cùng tuần/tháng đều in lại dòng này. Xác nhận bảng đã có:
`mysql -u root -e "SHOW TABLES FROM kpah_db LIKE 'tob_log_use_luong%';"`

**`bash: /opt/kpah/vps/update.sh: /bin/bash^M: bad interpreter`**
→ File `.sh` bị lưu kiểu CRLF của Windows. Repo đã có [.gitattributes](../.gitattributes) ép
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
- (đã xoá khỏi repo) `backup_account.sql` — từng có `INSERT INTO team_user` kèm **hash mật
  khẩu thật** của tài khoản `admin` (dạng `*A4B61573...` của hàm `PASSWORD()`, băm yếu, dò
  được). Nội dung cũ vẫn còn trong lịch sử git — vẫn phải giữ repo private.

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
