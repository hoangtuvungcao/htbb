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
4. Mở **`web/config.php`** và sửa cho đúng:
   - `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`
   - `DB_ACCOUNT` (mặc định `account`), `DB_GAME` (mặc định `kpah_db`)
   - Link tải game `LINK_ANDROID` / `LINK_PC` / `LINK_IOS` / `LINK_GROUP`
   - `AUTO_ACTIVATE` (true = đăng ký xong vào game được luôn)
5. Truy cập: `http://localhost/kpah/` (hoặc IP máy bạn để người khác vào).

## Yêu cầu

- PHP 7.4+ (khuyến nghị 8.x) với extension **pdo_mysql**.
- **MariaDB** hoặc **MySQL 5.7** (vẫn còn hàm `PASSWORD()`).
  ⚠️ **MySQL 8.0 đã bỏ `PASSWORD()`** — nếu dùng MySQL 8 phải đổi cách băm cho khớp login server.
  Máy chủ của bạn đang dùng MariaDB 10.4 nên chạy bình thường.

## Lưu ý bảo mật

- Đã dùng **PDO prepared statement** (chống SQL injection) và **CSRF token** cho form.
- Hàm `PASSWORD()` của MySQL là băm yếu, nhưng **bắt buộc dùng** để khớp login server hiện tại.
  Nếu sau này bạn nâng cấp cơ chế đăng nhập của server, hãy đổi cả hai nơi cho đồng bộ.
- Để người ngoài truy cập: mở cổng 80 (Apache) và trỏ tên miền / IP public vào máy.

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
| **Nhật ký admin** | Mọi thao tác trên trang quản trị. Không xoá được từ giao diện |

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
    ├── nhatky.php      # Nhật ký thao tác của admin
    ├── dangxuat.php
    ├── assets/admin.css
    ├── inc/
    │   ├── boot.php    # Đăng nhập, phân quyền, ghi nhật ký, chặn dò mật khẩu
    │   ├── layout.php  # Khung trang + biểu đồ thuần CSS
    │   └── thuong.php  # Bảng 60 loại phần thưởng + kiểm tra cú pháp giftcode
    └── test/           # Kiểm thử, không cần MySQL
        ├── kiem_giftcode.php
        └── kiem_schema.php
```

### Kiểm thử (không cần MySQL chạy)

```bash
php web/admin/test/kiem_giftcode.php   # bộ kiểm tra cú pháp phần thưởng
php web/admin/test/kiem_schema.php     # đối chiếu mọi câu SQL với cấu trúc bảng thật
```

- `kiem_giftcode.php` chạy lại **14 mã giftcode thật đang có trong bản dump** để
  chắc chúng vẫn hợp lệ, và **đối chiếu bảng 60 loại phần thưởng với chính
  `src/data/DatabaseGiftCode.java`**. Thêm một loại thưởng mới bên Java mà quên
  cập nhật `inc/thuong.php` là test này báo đỏ ngay.
- `kiem_schema.php` đọc `sql/gamedb7_clean.sql` + `loginServer/account.sql`, rồi
  soi từng câu SQL trong `web/admin/` xem có bảng/cột nào gõ nhầm không. Lưu ý
  `kpah_db` **cũng** có một bảng tên `team_user` nhưng khác hẳn bảng
  `account.team_user` — test này biết chuyện đó và luôn dùng bảng ở DB `account`.
