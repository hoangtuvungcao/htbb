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

## Danh sách file

```
web/
├── config.php          # SỬA Ở ĐÂY: DB + thiết lập site
├── index.php           # Trang chủ (hero, tải game, BXH, tin tức)
├── register.php        # Đăng ký (ghi team_user + PASSWORD())
├── login.php           # Đăng nhập website
├── account.php         # Trang tài khoản + nút Kích hoạt
├── logout.php
├── assets/style.css    # Giao diện tối võ hiệp
└── inc/
    ├── db.php          # Kết nối PDO
    ├── functions.php   # Helper (validate, csrf, kích hoạt)
    ├── header.php      # Header + menu
    └── footer.php
```
