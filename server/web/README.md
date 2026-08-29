# Website KPAH (đăng ký / đăng nhập / kích hoạt)

Trang chủ + hệ thống tài khoản cho máy chủ private KPAH, viết bằng **PHP + MySQL/MariaDB**,
dùng chung database với game server.

## Kết nối vào đâu?

- **Đăng ký** → ghi vào bảng `team_user` trong DB **`account`** (đúng DB mà login server `CheckLoginSocket.jar` kiểm tra).
- Mật khẩu lưu bằng hàm **`PASSWORD()`** của MySQL/MariaDB để KHỚP với login server
  (login server so khớp: `... and \`password\` = password('...')`).
- **Kích hoạt** → thêm vào bảng `5h_active` trong DB **`kpah_db`** (đúng bảng mà server kiểm tra ở `check_active`).
- **Bảng xếp hạng / nhân vật** → đọc từ `kpah_db` (`tob_char`, `board_created`).

## Cài đặt (Windows + XAMPP — khớp môi trường của bạn)

1. Cài **XAMPP** (hoặc dùng MySQL/MariaDB sẵn có). Bật **Apache** + **MySQL**.
2. Import 2 database (nếu chưa có) qua phpMyAdmin:
   - Tạo DB `account`, import `loginServer/account.sql`.
   - Tạo DB `kpah_db`, import `sql/gamedb7_clean.sql` (bản dump đã lọc sạch dữ liệu
     người chơi, giữ nguyên cấu trúc 84 bảng + toàn bộ dữ liệu game). Đọc phần
     đầu file đó: sau khi import có 3 việc bắt buộc phải làm.
3. Chép **cả thư mục `web/`** vào `C:\xampp\htdocs\` (ví dụ `htdocs/kpah`).
4. Tạo **`web/config.local.php`** từ mẫu rồi điền giá trị thật:

   ```bash
   cp web/config.local.php.example web/config.local.php
   ```

   **Không sửa `web/config.php`** — nó nằm trong git, sửa tay trên VPS sẽ chặn
   deploy lần sau (xem mục "Bí mật riêng của từng máy" bên dưới).

   Thông tin database **thường không phải khai**: `config.php` tự đọc từ
   `server.ini` của game server (xem mục "Thông tin database" bên dưới). Chỉ khai
   `DB_*` khi chép riêng `web/` sang `htdocs` — lúc đó thêm cả `SERVER_DIR` trỏ về
   thư mục dự án để trang Chẩn đoán đọc được file log.

   Còn lại sửa trong `config.php` như bình thường (chúng không phải bí mật):
   link tải game `LINK_ANDROID` / `LINK_PC` / `LINK_IOS` / `LINK_GROUP`, và
   `AUTO_ACTIVATE` (true = đăng ký xong vào game được luôn).
5. Truy cập: `http://localhost/kpah/` (hoặc IP máy bạn để người khác vào).

## Yêu cầu

- PHP 7.4+ (khuyến nghị 8.x) với extension **pdo_mysql**.
- **MariaDB** hoặc **MySQL 5.7** (vẫn còn hàm `PASSWORD()`).
  ⚠️ **MySQL 8.0 đã bỏ `PASSWORD()`** — nếu dùng MySQL 8 phải đổi cách băm cho khớp login server.
  Máy chủ của bạn đang dùng MariaDB 10.4 nên chạy bình thường.

## Chặn làm hàng loạt (chống spam)

Chạy một lần để bật:

```bash
mysql -u root kpah_db < sql/them_bang_web_chan_spam.sql
```

**Token CSRF không phải là giới hạn số lần.** Một script chỉ cần tải trang để lấy
token rồi gửi form, lặp bao nhiêu lần tuỳ thích. Bảng `web_chan_spam` mới là chỗ đếm.

| Việc | Hạn mức | Đếm cái gì |
|---|---|---|
| `register.php` | 5 tài khoản / IP / giờ | chỉ đếm lần tạo **thành công** — gõ trùng tên vài lần là chuyện bình thường |
| `login.php` | 15 lần **sai** / IP / 15 phút | chỉ đếm lần sai, nên người đăng nhập đúng không bao giờ chạm hạn mức |
| `naptien.php` | 10 đơn / IP / giờ | mỗi lần bấm là một dòng trong `tob_nap_moc` |
| `admin/index.php` | 8 lần sai / IP / 15 phút | dùng bảng `web_admin_log` riêng |

Thiếu bảng thì website **vẫn chạy bình thường**, chỉ lùi về đếm trong session —
yếu hơn nhiều (xoá cookie là đếm lại từ đầu). Trang Chẩn đoán báo rõ đang ở chế độ
nào. Đếm hỏng vì lỗi database thì **cho qua**: thà bỏ lọt còn hơn chặn oan người
chơi thật.

`biChanSpam()` chỉ đếm, không sửa gì. Việc dọn rác nằm ở `ghiLanLam()` theo một hạn
giữ cố định 24 giờ — dọn theo cửa sổ của phép kiểm tra sẽ xoá mất lịch sử mà một
phép kiểm tra khác (cửa sổ dài hơn) trên cùng hành động vẫn còn cần.

## Lưu ý bảo mật

- Đã dùng **PDO prepared statement** (chống SQL injection) và **CSRF token** cho form.
- Hàm `PASSWORD()` của MySQL là băm yếu, nhưng **bắt buộc dùng** để khớp login server hiện tại.
  Nếu sau này bạn nâng cấp cơ chế đăng nhập của server, hãy đổi cả hai nơi cho đồng bộ.
- Để người ngoài truy cập: mở cổng 80 (Apache) và trỏ tên miền / IP public vào máy.

## Thông tin database: đọc thẳng từ `server.ini`

Website **không** giữ bản sao thông tin database. `config.php` đọc thẳng từ hai file
cấu hình mà game server dùng hằng ngày:

| File | Lấy ra |
|---|---|
| `<SERVER_DIR>/server.ini` | `db.host`, `db.name`, `db.user`, `db.password` → DB game |
| `<SERVER_DIR>/loginServer/server.ini` | `db.url` (chứa tên DB tài khoản), `db.user`, `db.password` → DB `account` |

`SERVER_DIR` mặc định là thư mục cha của `web/`, đúng sẵn cho VPS: server chạy với
`WorkingDirectory=/opt/kpah` còn web ở `/opt/kpah/web`.

Nhờ vậy đổi mật khẩu database chỉ phải sửa **một chỗ**. Trước đây chép tay sang
`config.local.php` là tạo bản sao thứ hai để lệch nhau — đổi mật khẩu cho server
là website chết mà không ai nhớ tại sao.

Thứ tự ưu tiên, cao xuống thấp:

1. `config.local.php` — khai tay thì luôn thắng
2. `server.ini` — nguồn thật, tự dò
3. mặc định trong `config.php` — `root` / không mật khẩu, cho máy dev XAMPP

Login server có thể dùng **người dùng database khác** game server; khi hai file ini
khai khác nhau, `inc/db.php` tự dùng đúng cặp cho mỗi database.

Đọc không được thì im lặng rơi xuống mặc định — hai file ini đó thường là
`chmod 640 chown kpah:kpah`, mà PHP chạy dưới `www-data`. Cấp quyền đọc:

```bash
usermod -a -G kpah www-data && systemctl restart php8.2-fpm
```

Trang **Chẩn đoán** có mục *"Nguồn thông tin database"* nói rõ đang đọc được từ
`server.ini` hay đang dùng giá trị khai tay, kèm đúng câu lệnh để sửa.

Giữ nguyên chữ `localhost` chứ không đổi thành `127.0.0.1`: MySQL phân quyền theo
máy khách, và `'kpah'@'localhost'` với `'kpah'@'127.0.0.1'` là **hai quyền khác
nhau** — đổi đi là nhận `Access denied` dù mật khẩu hoàn toàn đúng.

## Bí mật riêng của từng máy — `config.local.php`

`web/config.php` nằm trong git, còn DB password / số TK ngân hàng / API key / mật khẩu
admin thì **KHÁC NHAU giữa máy dev và VPS thật** → không được để trong file đã commit.
Chúng nằm ở `web/config.local.php` (đã thêm vào `.gitignore`, không commit):

```bash
cp web/config.local.php.example web/config.local.php
# rồi sửa config.local.php với giá trị THẬT
```

`config.php` tự `require` file này nếu nó tồn tại. **Không bao giờ sửa tay
`web/config.php` trực tiếp trên VPS** — `deploy-auto.sh` chạy `git status --porcelain`
trước mỗi lần deploy và **từ chối `git pull` nếu repo bị sửa tay**, làm job
"Goi deploy tren VPS" trên GitHub Actions báo lỗi đỏ ở mọi lần push sau đó.

**Nếu VPS đang bị dính lỗi này** (đã lỡ sửa tay `config.php` trên VPS): SSH vào VPS,
copy các giá trị thật đang có trong `config.php` sang `config.local.php` (theo mẫu
`config.local.php.example`), rồi trả `config.php` về đúng bản gốc trong git:

```bash
sudo -u kpah git -C /opt/kpah checkout -- web/config.php
sudo -u kpah git -C /opt/kpah status --porcelain   # phải ra rỗng
```

## Nạp tiền theo mốc (49k / 99k / 299k) + NPC Tổng Quản

Tính năng "3 mốc tân thủ": người chơi vào `naptien.php` chọn mốc, quét mã QR VietQR
chuyển khoản TPBank, sau khi thanh toán vào game gặp **NPC Tổng Quản** chọn
**"Nhận mốc nạp"** để nhận quà (mốc 3 được chọn Phượng Hoàng hoặc Phượng Hoàng Băng).

Cài đặt:

1. Bảng `tob_nap_moc` (theo dõi trạng thái mốc nạp) đã được tạo sẵn trong PHẦN 2I của
   [vps/db_new_server.sql](../vps/db_new_server.sql) khi dựng database `kpah_db`. Nếu server
   đã chạy từ trước lúc chưa có bảng này, chạy riêng khối `CREATE TABLE IF NOT EXISTS
   tob_nap_moc` ở đó vào `kpah_db`.
2. Mở `web/config.local.php` (xem mục "Bí mật riêng của từng máy" phía trên), điền đúng:
   - `TPBANK_ACCOUNT_NO`, `TPBANK_ACCOUNT_NAME` (số tài khoản + tên chủ TK TPBank nhận tiền).
   - `ADMIN_NAPTIEN_PASSWORD` (đổi mật khẩu mặc định trước khi public).
3. **Xác nhận chuyển khoản** — 2 cách, có thể dùng song song:
   - **Tự động (khuyến nghị)**: đăng ký [SePay](https://sepay.vn), liên kết tài khoản
     TPBank, thêm Webhook trỏ tới `https://ten-mien/webhook_sepay.php`, đặt API Key
     trên SePay và dán y hệt vào `SEPAY_WEBHOOK_APIKEY` trong `config.php`.
   - **Thủ công**: để `SEPAY_WEBHOOK_APIKEY` trống, vào `admin_naptien.php`
     (nhập `ADMIN_NAPTIEN_PASSWORD`) để tự kiểm tra TPBank rồi bấm xác nhận từng đơn.
4. Phía server game (Java) đã có sẵn: NPC Tổng Quản (npcType `-10`) tự thêm mục
   "Nhận mốc nạp" — không cần chỉnh gì thêm ở server, chỉ cần build lại và chạy.

## Trang quản trị — `web/admin/`

Khu riêng để **xem thống kê người chơi, khoá tài khoản, tạo giftcode và đọc log**.
Địa chỉ: `https://ten-mien/admin/` (không có liên kết nào từ trang công khai trỏ tới).

### Bật lên (2 bước)

1. **Tạo bảng nhật ký** — chạy một lần:

   ```bash
   mysql -u root kpah_db < sql/them_bang_web_admin_log.sql
   ```

   Bảng `web_admin_log` ghi lại mọi thao tác (ai, làm gì, lúc nào, từ IP nào) và
   là chỗ đếm số lần gõ sai mật khẩu để chặn dò. Thiếu bảng thì trang vẫn chạy
   nhưng không để lại dấu vết, và việc chặn dò chỉ đếm trong session — xoá cookie
   là hết.

2. **Cấp quyền** — mở `web/config.local.php` trên máy chủ và thêm:

   ```php
   define('ADMIN_ACCOUNTS', 'hoahv,admin');
   ```

   Đó là **tên tài khoản đăng nhập** (`account.team_user.username`), *không phải*
   tên nhân vật. Người vào vẫn phải gõ đúng mật khẩu tài khoản game của mình —
   danh sách chỉ quyết định ai được phép.

   Để trống = **không ai vào được**. Đây là mặc định có chủ ý: quên cấu hình thì
   trang tự khoá, chứ không mở toang cho mọi người có tài khoản.

### Có gì

| Trang | Làm được gì |
|---|---|
| **Tổng quan** | Tổng tài khoản / đăng ký hôm nay–7–30 ngày, số bị khoá, số chưa kích hoạt; nhân vật vào game 24h/7/30 ngày; biểu đồ 14 ngày (đăng ký + hoạt động); phân bố nghề và cấp độ; top cấp / top lượng / top nạp; tài khoản mới nhất; thao tác admin gần nhất |
| **Tài khoản** | Tìm theo tên / email / SĐT / ID · **khoá – mở khoá đăng nhập** · kích hoạt – huỷ kích hoạt · đổi mật khẩu · xem mọi nhân vật của tài khoản |
| **Nhân vật** | Tìm theo tên hoặc ID tài khoản · **khoá – mở khoá từng nhân vật** · xem cấp, lượng, xu, lần vào cuối, bang hội, cờ nghi vấn |
| **Giftcode** | Tạo / sửa / tắt / xoá mã · **kiểm tra cú pháp phần thưởng trước khi lưu** · xem ai đã nhận mã nào · cho một nhân vật nhận lại |
| **Log game** | 8 bảng log server tự ghi (vật phẩm, ngọc, mua shop, mua kho, bang hội, tiêu lượng, nạp/xài lượng, xổ số), lọc theo nhân vật |
| **Chẩn đoán & Lỗi** | 13 phép kiểm tra sức khoẻ, thống kê lỗi server ghi vào DB, xem cuối các file log của server, lỗi của chính trang web — xem mục dưới |
| **Nhật ký admin** | Mọi thao tác trên trang quản trị. Không xoá được từ giao diện |

### Trang Chẩn đoán gom bốn nguồn lỗi

| Nguồn | Ở đâu |
|---|---|
| Lỗi Java | `errorlog.txt` (`util.Logger.logError`) và `log/game.out` (systemd hứng stdout/stderr) |
| Lỗi server ghi vào DB | `tob_log_all_item` / `tob_log_all_gem`, cột `aclog`: `loiinvent`, `hackc`, `hackR`, `hackbitem`, `hackchonchar`, `black`, `ffbug`, `timeout` |
| Dữ liệu lệch | không ai báo — trang tự đi soi (xem danh sách dưới) |
| Lỗi trang web | `web_admin_log` với `hanh_dong = 'loi_web'` |

13 phép kiểm tra, xếp mức nặng lên trước, mỗi phép kèm câu SQL sửa:

1. **Sức chứa cột hành trang** — `inven`/`bag`/`tuido`/`equip` còn là `TEXT` (trần 65.535 byte ≈ 140 món) hay đã nới `MEDIUMTEXT`. Đây chính là cái bẫy đã nổ ngày 28/07/2026: lưu hỏng → server *tự* đặt `ban = 1` → nhân vật biến mất khỏi màn chọn.
2. **Nhân vật sắp đầy hành trang** — ai đã dùng quá 80% sức chứa, tức lần lưu kế tiếp có thể hỏng.
3. **Lỗi lưu hành trang** — nhân vật nào từng có dòng `loiinvent`.
4. **Nhân vật đang bị khoá** — nhắc rằng không phải cái nào cũng do admin khoá.
5. **Giftcode trùng tên** — server dùng `LIMIT 1` nên dòng sau vĩnh viễn chết.
6. **Cú pháp phần thưởng của các mã đang có** — chạy bộ kiểm tra lên *dữ liệu thật*.
7. **`5h_active` trùng dòng** · 8. **kích hoạt mồ côi** · 9. **nhân vật mồ côi tài khoản**.
10. **Giá trị `ban` lạ** — giá trị ngoài 0/1/3 rơi vào nhánh cuối của login server và được coi như *bình thường*: đặt nhầm là tưởng đã khoá mà thật ra chưa.
11. **Bảng phụ có đủ chưa** · 12. **cờ nghi vấn `nhack`/`blacklist`** · 13. **đọc được file log của server không**.

Đọc file log lấy khoảng **64 KB cuối** bằng `fseek` chứ không nạp cả file — `game.out` có thể vài trăm MB. Thư mục lấy từ hằng `SERVER_DIR`, mặc định là thư mục cha của `web/` (đúng cho VPS vì server chạy với `WorkingDirectory=/opt/kpah` còn web ở `/opt/kpah/web`). Chạy thử trên máy khác cây thư mục thì đặt lại trong `config.local.php`:

```php
define('SERVER_DIR', 'C:/Users/ADMIN/Desktop/KPAH');
```

Trang **chỉ đọc**, và chỉ đọc đúng 10 tên file viết sẵn trong mã nguồn — tên file không bao giờ lấy từ đường dẫn, nên `?file=../config.local.php` không lần ra mật khẩu database được.

### Khoá tài khoản khác gì khoá nhân vật

| | Khoá **tài khoản** | Khoá **nhân vật** |
|---|---|---|
| Cột | `account.team_user.ban = 1` | `kpah_db.tob_char.ban = 1` |
| Người chơi thấy | “Tài khoản của bạn hiện đang bị khoá” khi đăng nhập | Nhân vật **biến mất** khỏi màn chọn nhân vật |
| Còn đăng nhập được? | Không | Có — chỉ mất nhân vật đó |
| Xếp hạng | — | Bị loại khỏi mọi bảng xếp hạng |

**Cả hai đều chỉ ăn từ lần vào sau.** Người đang online vẫn chơi tiếp cho tới khi
họ tự thoát — trang web không nói chuyện được với tiến trình game đang chạy. Muốn
đá ra ngay thì dùng bảng điều khiển trong game (`AdminPanel`).

### Vài chốt an toàn đã cài sẵn

- Không khoá được tài khoản nằm trong `ADMIN_ACCOUNTS` — kể cả của chính mình.
  Đây là thứ duy nhất ngăn việc tự nhốt mình ra ngoài (mở lại phải sửa thẳng DB).
- Không đổi được mật khẩu của một admin khác.
- Gõ sai mật khẩu 8 lần trong 15 phút từ cùng một IP thì bị chặn tạm.
- Phiên tự hết hạn sau 60 phút không thao tác; gỡ tên khỏi `ADMIN_ACCOUNTS` là
  phiên đang mở mất quyền ngay lần bấm kế tiếp.
- Mọi form đều có CSRF token; mọi truy vấn đều dùng prepared statement.
- Tên mã giftcode và chuỗi phần thưởng bị lọc ký tự chặt, vì **server ghép thẳng
  hai thứ đó vào câu SQL** khi ghi `giftcode_log` — một dấu nháy đơn lọt vào là
  hỏng câu lệnh của server.

## Danh sách file

```
web/
├── config.php          # SỬA Ở ĐÂY: DB + thiết lập site
├── index.php           # Trang chủ (hero, tải game, BXH, tin tức)
├── register.php        # Đăng ký (ghi team_user + PASSWORD())
├── login.php           # Đăng nhập website
├── account.php         # Trang tài khoản + nút Kích hoạt
├── logout.php
├── naptien.php          # Nạp tiền theo mốc + hiển thị QR VietQR
├── order_status.php     # API polling trạng thái 1 đơn nạp mốc (JSON)
├── webhook_sepay.php    # Webhook SePay xác nhận chuyển khoản tự động
├── admin_naptien.php    # Trang admin xác nhận chuyển khoản thủ công
├── assets/style.css    # Giao diện tối võ hiệp
├── inc/
│   ├── db.php          # Kết nối PDO
│   ├── functions.php   # Helper (validate, csrf, kích hoạt)
│   ├── header.php      # Header + menu
│   └── footer.php
└── admin/              # KHU QUẢN TRỊ (xem mục ở trên)
    ├── index.php       # Đăng nhập + bảng tổng quan / thống kê
    ├── taikhoan.php    # Khoá, kích hoạt, đổi mật khẩu tài khoản
    ├── nhanvat.php     # Khoá / mở khoá nhân vật
    ├── giftcode.php    # Tạo & quản lý mã quà, xem ai đã nhận
    ├── log.php         # Xem 8 bảng log của game server
    ├── chandoan.php    # Chẩn đoán & lỗi: 13 phép kiểm tra + đọc file log server
    ├── nhatky.php      # Nhật ký thao tác của admin
    ├── dangxuat.php
    ├── assets/admin.css
    ├── inc/
    │   ├── boot.php    # Đăng nhập, phân quyền, ghi nhật ký, chặn dò mật khẩu
    │   ├── layout.php  # Khung trang + biểu đồ thuần CSS
    │   └── thuong.php  # Bảng 60 loại phần thưởng + kiểm tra cú pháp giftcode
    └── test/           # Kiểm thử, không cần MySQL
        ├── kiem_giftcode.php
        ├── kiem_schema.php
        ├── kiem_duong_dan.php
        ├── kiem_doc_ini.php
        └── kiem_chan_spam.php
```

### Kiểm thử (không cần MySQL chạy)

```bash
php web/admin/test/kiem_giftcode.php   # bộ kiểm tra cú pháp phần thưởng
php web/admin/test/kiem_schema.php     # đối chiếu mọi câu SQL với cấu trúc bảng thật
php web/admin/test/kiem_duong_dan.php  # chốt chặn đọc file của trang Chẩn đoán
php web/admin/test/kiem_doc_ini.php    # đọc server.ini ra thông tin database
php web/admin/test/kiem_chan_spam.php  # bộ đếm chặn làm hàng loạt
```

- `kiem_giftcode.php` chạy lại **14 mã giftcode thật đang có trong bản dump** để
  chắc chúng vẫn hợp lệ, và **đối chiếu bảng 60 loại phần thưởng với chính
  `src/data/DatabaseGiftCode.java`**. Thêm một loại thưởng mới bên Java mà quên
  cập nhật `inc/thuong.php` là test này báo đỏ ngay.
- `kiem_schema.php` đọc `sql/gamedb7_clean.sql` + `loginServer/account.sql`, rồi
  soi từng câu SQL trong `web/admin/` xem có bảng/cột nào gõ nhầm không. Lưu ý
  `kpah_db` **cũng** có một bảng tên `team_user` nhưng khác hẳn bảng
  `account.team_user` — test này biết chuyện đó và luôn dùng bảng ở DB `account`.
- `kiem_duong_dan.php` lấy **chính** danh sách file cho phép trong `chandoan.php`
  (không chép lại) rồi ném vào 24 đường dẫn độc — `../config.local.php`,
  `php://filter/...`, byte null, `%2F`… — khẳng định không cái nào lọt, và mọi
  file hợp lệ đều nằm trong `SERVER_DIR`.

### Một lỗi sửa ở phía server Java

`src/data/Database.java` có 4 chỗ bắt lỗi gọi `Logger.logError(e, "")` — tên file
**rỗng**. `new FileWriter("")` ném `IOException` ngay, nên vết gọi không bao giờ
tới `errorlog.txt`; nó rơi ra stdout và chỉ còn thấy được trong `log/game.out`.
Đã đổi thành `Logger.logError(e, "errorlog.txt")` để trang Chẩn đoán nhìn thấy.
**Cần build lại jar và cập nhật server thì thay đổi này mới có tác dụng.**
