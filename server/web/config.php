<?php
/**
 * ============================================================
 *  CẤU HÌNH WEBSITE KPAH  —  SỬA CÁC GIÁ TRỊ DƯỚI ĐÂY
 * ============================================================
 *  - Website này dùng CHUNG MySQL/MariaDB với game server.
 *  - Có 2 database:
 *      + account : chứa bảng team_user (tài khoản đăng nhập) - login server dùng
 *      + kpah_db : dữ liệu game (nhân vật, kích hoạt, bảng xếp hạng)
 *  - Mật khẩu lưu bằng hàm PASSWORD() của MySQL/MariaDB để KHỚP với login server.
 *    (Login server kiểm tra: ... and `password`=password('...'))
 * ============================================================
 */

/**
 * ---- Bí mật riêng của từng máy (DB pass, số TK ngân hàng, API key...) ----
 * Các giá trị THẬT được đặt trong web/config.local.php (KHÔNG commit vào git,
 * xem .gitignore + web/config.local.php.example). Sửa tay config.php trực tiếp
 * trên VPS sẽ làm `git status` không sạch và chặn deploy-auto.sh lần sau
 * (script đó từ chối "git pull" khi repo bị sửa tay). Luôn sửa config.local.php.
 */
if (file_exists(__DIR__ . '/config.local.php')) {
    require __DIR__ . '/config.local.php';
}

// ---- Kết nối database (giá trị mặc định cho máy dev/XAMPP; VPS đặt thật trong config.local.php) ----
if (!defined('DB_HOST')) define('DB_HOST', '127.0.0.1');
if (!defined('DB_PORT')) define('DB_PORT', 3306);
if (!defined('DB_USER')) define('DB_USER', 'root');
if (!defined('DB_PASS')) define('DB_PASS', '');   // mật khẩu MySQL, để trống nếu không có

const DB_ACCOUNT = 'account';     // DB chứa bảng team_user (đúng như loginServer/server.ini)
const DB_GAME    = 'kpah_db';     // DB game (đúng như server.ini: db.name)

// ---- Thiết lập website ----
const SITE_NAME   = 'KPAH - Khí Phách Anh Hùng';
const SITE_TAGLINE = 'Máy chủ private - Hồi ức tuổi thơ';

// Link tải game (sửa thành link của bạn)
const LINK_ANDROID = '#';
const LINK_PC      = '#';
const LINK_IOS     = '#';

// Nhóm hỗ trợ (Zalo/Facebook...) - sửa hoặc để '#'
const LINK_GROUP   = '#';

// Quy tắc tài khoản (khớp với server: 4-20 ký tự, chỉ chữ và số)
const USERNAME_MIN = 4;
const USERNAME_MAX = 20;
const PASSWORD_MIN = 4;
const PASSWORD_MAX = 20;

/**
 * Tự động kích hoạt tài khoản ngay khi đăng ký.
 *   true  = đăng ký xong đăng nhập game được luôn (tiện cho test / server nhỏ)
 *   false = phải bấm "Kích hoạt" ở trang tài khoản (giống trang gốc)
 * Lưu ý: chỉ có tác dụng khi game bật kiểm tra kích hoạt (sv.EnableAvtiveCheck=1).
 */
const AUTO_ACTIVATE = true;

/**
 * ============================================================
 *  NẠP TIỀN THEO MỐC (49k / 99k / 299k) - QR VietQR + TPBank
 * ============================================================
 *  - QR được tạo bằng dịch vụ ảnh công khai img.vietqr.io (không cần đăng ký),
 *    tương thích quét bằng app TPBank / app ngân hàng bất kỳ hỗ trợ VietQR.
 *  - TPBANK_BIN = mã ngân hàng theo chuẩn VietQR (napas), TPBank cố định là 970423.
 *  - SỬA TPBANK_ACCOUNT_NO / TPBANK_ACCOUNT_NAME (đúng số TK + tên chủ TK, KHÔNG DẤU,
 *    IN HOA) trong web/config.local.php trên VPS, KHÔNG sửa trực tiếp ở đây.
 *
 * SePay (https://sepay.vn) liên kết TPBank để BÁO TỰ ĐỘNG khi có tiền vào (webhook),
 * không cần thao tác gì thêm. Cách bật:
 *   1. Đăng ký SePay, thêm tài khoản ngân hàng TPBank của bạn vào SePay.
 *   2. Vào SePay > Cấu hình > Webhooks, thêm Webhook trỏ tới:
 *          https://ten-mien-cua-ban/webhook_sepay.php
 *      Đặt API Key ở mục Webhook và dán y hệt vào SEPAY_WEBHOOK_APIKEY (config.local.php).
 * Nếu để trống '', hệ thống KHÔNG xác nhận tự động - phải dùng trang
 * admin_naptien.php để admin tự xác nhận thủ công sau khi kiểm tra TPBank.
 */
const TPBANK_BIN = '970423'; // mã ngân hàng theo chuẩn VietQR, TPBank luôn là 970423
if (!defined('TPBANK_ACCOUNT_NO')) define('TPBANK_ACCOUNT_NO', '0000000000');
if (!defined('TPBANK_ACCOUNT_NAME')) define('TPBANK_ACCOUNT_NAME', 'NGUYEN VAN A');
if (!defined('SEPAY_WEBHOOK_APIKEY')) define('SEPAY_WEBHOOK_APIKEY', '');

/** Mật khẩu trang xác nhận nạp tiền thủ công (admin_naptien.php). Đặt thật trong config.local.php! */
if (!defined('ADMIN_NAPTIEN_PASSWORD')) define('ADMIN_NAPTIEN_PASSWORD', 'doi-mat-khau-nay');

/**
 * ============================================================
 *  TRANG QUẢN TRỊ (web/admin/)
 * ============================================================
 *  Danh sách TÊN TÀI KHOẢN ĐĂNG NHẬP (cột account.team_user.username, KHÔNG phải
 *  tên nhân vật) được phép vào khu quản trị: xem thống kê, khoá tài khoản, tạo
 *  giftcode, đọc log. Ngăn nhau bằng dấu phẩy.
 *
 *      define('ADMIN_ACCOUNTS', 'hoahv,admin');
 *
 *  Người vào vẫn phải gõ đúng mật khẩu tài khoản game của mình — danh sách này
 *  chỉ quyết định AI ĐƯỢC PHÉP, không thay cho mật khẩu.
 *
 *  Để TRỐNG là đóng cửa với tất cả mọi người. Đó là mặc định có chủ ý: quên cấu
 *  hình thì trang tự khoá lại, chứ không mở toang cho bất kỳ ai có tài khoản.
 *  Đặt giá trị thật trong web/config.local.php, đừng sửa ở đây.
 */
if (!defined('ADMIN_ACCOUNTS')) define('ADMIN_ACCOUNTS', '');
