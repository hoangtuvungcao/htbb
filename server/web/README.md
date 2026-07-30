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
   - Tạo DB `kpah_db`, import `gamedb7_clean.sql` (bản dump đã lọc sạch dữ liệu
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

1. Import `sql/tob_nap_moc.sql` vào database `kpah_db` (bảng theo dõi trạng thái mốc nạp).
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
└── inc/
    ├── db.php          # Kết nối PDO
    ├── functions.php   # Helper (validate, csrf, kích hoạt)
    ├── header.php      # Header + menu
    └── footer.php
```
