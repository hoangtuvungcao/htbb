<?php
/**
 * ============================================================
 *  CẤU HÌNH WEBSITE HTBB  —  SỬA CÁC GIÁ TRỊ DƯỚI ĐÂY
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

/**
 * Thư mục gốc của game server — nơi có server.ini, errorlog.txt, log/game.out...
 * Server chạy với WorkingDirectory=/opt/kpah (vps/kpah-game.service) còn web nằm
 * ở /opt/kpah/web, nên thư mục cha của file này chính là chỗ cần tìm. Chỉ phải
 * đặt lại trong config.local.php khi web không nằm cùng cây thư mục với server.
 */
if (!defined('SERVER_DIR')) define('SERVER_DIR', dirname(__DIR__));

/**
 * ============================================================
 *  KẾT NỐI DATABASE — ĐỌC THẲNG TỪ server.ini CỦA GAME SERVER
 * ============================================================
 *  Thông tin database đã nằm sẵn ở hai chỗ mà game server dùng hằng ngày:
 *
 *      /opt/kpah/server.ini              -> db.host, db.name, db.user, db.password
 *      /opt/kpah/loginServer/server.ini  -> db.url (chứa tên DB account), db.user, db.password
 *
 *  Chép tay sang config.local.php chỉ tạo thêm một bản sao để lệch nhau: đổi mật
 *  khẩu database là website chết mà không ai nhớ tại sao. Nên web đọc thẳng từ đó.
 *
 *  Thứ tự ưu tiên, cao xuống thấp:
 *      1. config.local.php   — khai tay thì luôn thắng (dùng khi web ở máy khác)
 *      2. server.ini         — nguồn thật, tự dò
 *      3. mặc định bên dưới  — cho máy dev XAMPP (root, không mật khẩu)
 *
 *  Đọc KHÔNG được thì im lặng bỏ qua và rơi xuống mặc định — hai file ini đó
 *  thường để chmod 640 chown kpah:kpah, mà PHP có thể chạy dưới người dùng khác
 *  (www-data). Muốn dùng cách này thì cho người chạy PHP đọc được, ví dụ:
 *      usermod -a -G kpah www-data && systemctl restart php*-fpm
 *  Trang Chẩn đoán có một mục nói rõ đang đọc được hay không.
 */
require_once __DIR__ . '/inc/doc_server_ini.php';

// Người dùng có tự khai DB_USER trong config.local.php không? Nếu có thì tôn
// trọng cho CẢ HAI database — đừng lấy người dùng của login server đè lên nửa chừng.
$htbbUserKhaiTay = defined('DB_USER');

$htbbIni = htbbCauHinhDb(SERVER_DIR);

if (!defined('DB_HOST') && $htbbIni['host'] !== null) define('DB_HOST', $htbbIni['host']);
if (!defined('DB_PORT') && $htbbIni['port'] !== null) define('DB_PORT', $htbbIni['port']);
if (!defined('DB_USER') && $htbbIni['user'] !== null) define('DB_USER', $htbbIni['user']);
if (!defined('DB_PASS') && $htbbIni['pass'] !== null) define('DB_PASS', $htbbIni['pass']);
if (!defined('DB_GAME') && $htbbIni['game'] !== null) define('DB_GAME', $htbbIni['game']);
if (!defined('DB_ACCOUNT') && $htbbIni['account'] !== null) define('DB_ACCOUNT', $htbbIni['account']);

// Login server CÓ THỂ dùng người dùng database khác game server. Chỉ khai riêng
// khi thật sự khác, để inc/db.php dùng đúng cặp cho mỗi database.
if (!defined('DB_ACCOUNT_USER') && !$htbbUserKhaiTay && $htbbIni['account_user'] !== null
    && (!defined('DB_USER') || $htbbIni['account_user'] !== DB_USER)) {
    define('DB_ACCOUNT_USER', $htbbIni['account_user']);
    define('DB_ACCOUNT_PASS', (string)$htbbIni['account_pass']);
}

unset($htbbIni, $htbbUserKhaiTay);

// ---- Mặc định cuối cùng: máy dev XAMPP, khi không có server.ini nào đọc được ----
if (!defined('DB_HOST')) define('DB_HOST', '127.0.0.1');
if (!defined('DB_PORT')) define('DB_PORT', 3306);
if (!defined('DB_USER')) define('DB_USER', 'root');
if (!defined('DB_PASS')) define('DB_PASS', '');   // mật khẩu MySQL, để trống nếu không có

if (!defined('DB_ACCOUNT')) define('DB_ACCOUNT', 'account');  // DB chứa bảng team_user
if (!defined('DB_GAME'))    define('DB_GAME', 'kpah_db');     // DB game

// ---- Thiết lập website ----
const SITE_NAME   = 'HTBB - Huyền Thoại Bảo Bối';
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
 *   false = chỉ admin mới cấp gói kích hoạt 30 ngày
 * Lưu ý: chỉ có tác dụng khi game bật kiểm tra kích hoạt (sv.EnableAvtiveCheck=1).
 */
const AUTO_ACTIVATE = false;

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

/**
 * SERVER_DIR đã được đặt ở đầu file (phải có sớm để đọc server.ini). Nó cũng là
 * thư mục mà trang Chẩn đoán tìm errorlog.txt, hack.txt, log/game.out — trang đó
 * chỉ ĐỌC, và chỉ đọc đúng danh sách tên file viết sẵn trong chandoan.php.
 *
 * Chỉ cần đặt lại trong config.local.php khi web KHÔNG nằm cùng cây thư mục với
 * server (ví dụ chép web/ sang C:\xampp\htdocs khi chạy thử):
 *
 *     define('SERVER_DIR', 'C:/Users/ADMIN/Desktop/HTBB');
 */
