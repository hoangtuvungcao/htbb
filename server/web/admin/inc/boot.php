<?php
/**
 * ============================================================================
 *  Nền móng cho Trang quản trị (web/admin/) — đăng nhập, phân quyền, nhật ký.
 * ============================================================================
 *
 *  AI VÀO ĐƯỢC?
 *  Đăng nhập bằng chính TÀI KHOẢN GAME (bảng account.team_user, so khớp mật khẩu
 *  bằng hàm PASSWORD() y hệt login server), và tài khoản đó phải có tên trong
 *  danh sách ADMIN_ACCOUNTS ở web/config.local.php. Danh sách rỗng = KHÔNG AI
 *  vào được (khoá mặc định, không phải mở mặc định) — để khi quên cấu hình thì
 *  trang tự đóng lại chứ không mở toang.
 *
 *  VÌ SAO KHÔNG DÙNG SẴN tob_char.isAdmin?
 *  Vì trong game quyền admin được quyết định theo TÊN NHÂN VẬT trong
 *  sv.adminAccounts của server.ini (CharNapDuLieu.java:381-393), còn cột
 *  tob_char.isAdmin bị ghi đè mỗi lần nạp nhân vật nên không đáng tin. Danh sách
 *  riêng cho web vừa chắc chắn, vừa cho phép tách bạch: người làm GM trong game
 *  chưa chắc được quyền khoá tài khoản trên web.
 */

require_once __DIR__ . '/../../inc/functions.php';

/**
 * Lưới an toàn cuối cùng. Không có nó, một lỗi không bắt được (hay gặp nhất là
 * MySQL tắt) sẽ in thẳng vết gọi của PDO ra trình duyệt — kèm host, tên
 * database và tên người dùng — cho bất kỳ ai mở trang, kể cả khi chưa đăng nhập.
 * Chi tiết luôn được ghi vào log lỗi của PHP, còn trên màn hình thì chỉ người đã
 * đăng nhập mới thấy nội dung lỗi.
 */
set_exception_handler(static function (Throwable $ex): void {
    error_log('[htbb-admin] ' . get_class($ex) . ': ' . $ex->getMessage()
        . ' @ ' . $ex->getFile() . ':' . $ex->getLine());

    // Ghi luôn vào nhật ký để trang Chẩn đoán nhìn thấy. Bọc try/catch vì lỗi
    // hay gặp nhất chính là mất kết nối database — lúc đó việc ghi cũng hỏng
    // nốt, và một lỗi ném ra từ trong trình xử lý lỗi là lỗi nghiêm trọng thật.
    try {
        ghiNhatKy('loi_web', basename($ex->getFile()) . ':' . $ex->getLine(),
            get_class($ex) . ': ' . $ex->getMessage());
    } catch (Throwable $boQua) {
    }

    if (!headers_sent()) {
        http_response_code(500);
        header('Content-Type: text/html; charset=utf-8');
    }
    echo '<div style="font-family:system-ui,sans-serif;max-width:640px;margin:60px auto;'
       . 'background:#1e1813;color:#ece3d6;border:1px solid #b8302b;border-radius:12px;padding:20px 24px;">'
       . '<h2 style="color:#e0453f;margin-top:0;">Trang quản trị gặp lỗi</h2>'
       . '<p>Thường là do MySQL chưa chạy hoặc sai thông tin kết nối trong '
       . '<code>web/config.local.php</code>.</p>';
    if (!empty($_SESSION['htbb_admin'])) {
        echo '<p style="color:#9a8c78;font-size:.9rem;">Chi tiết: '
           . htmlspecialchars($ex->getMessage(), ENT_QUOTES, 'UTF-8') . '</p>';
    } else {
        echo '<p style="color:#9a8c78;font-size:.9rem;">Chi tiết đã được ghi vào log lỗi của PHP.</p>';
    }
    echo '<p><a href="index.php" style="color:#e8b45a;">Quay lại</a></p></div>';
});

/** Phiên admin hết hạn sau bao lâu không thao tác (giây). */
if (!defined('ADMIN_IDLE_TIMEOUT')) define('ADMIN_IDLE_TIMEOUT', 3600);
/** Gõ sai mật khẩu quá số lần này trong ADMIN_FAIL_WINDOW giây thì chặn tạm. */
if (!defined('ADMIN_MAX_FAIL'))     define('ADMIN_MAX_FAIL', 8);
if (!defined('ADMIN_FAIL_WINDOW'))  define('ADMIN_FAIL_WINDOW', 900);

// ---------------------------------------------------------------------------
//  Danh sách tài khoản được vào trang quản trị
// ---------------------------------------------------------------------------

/**
 * Đọc ADMIN_ACCOUNTS (chuỗi ngăn cách bằng dấu phẩy) thành mảng tên viết thường.
 * Ví dụ trong config.local.php:  define('ADMIN_ACCOUNTS', 'hoahv,admin');
 */
function adminAccounts(): array
{
    if (!defined('ADMIN_ACCOUNTS')) {
        return [];
    }
    $out = [];
    foreach (explode(',', (string)constant('ADMIN_ACCOUNTS')) as $ten) {
        $ten = strtolower(trim($ten));
        if ($ten !== '') {
            $out[] = $ten;
        }
    }
    return array_values(array_unique($out));
}

function laTaiKhoanAdmin(string $username): bool
{
    return in_array(strtolower(trim($username)), adminAccounts(), true);
}

// ---------------------------------------------------------------------------
//  Phiên làm việc
// ---------------------------------------------------------------------------

function adminIp(): string
{
    // Không tin X-Forwarded-For trừ khi web thật sự nằm sau proxy và proxy đã
    // được cấu hình ghi đè header này. Tin nhầm ở đây = kẻ tấn công tự đổi IP
    // để thoát giới hạn số lần gõ sai mật khẩu.
    return substr((string)($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45);
}

/** Admin đang đăng nhập, hoặc null. Tự hết hạn khi quá lâu không thao tác. */
function adminUser(): ?array
{
    $a = $_SESSION['htbb_admin'] ?? null;
    if (!$a) {
        return null;
    }
    if (time() - (int)($a['last'] ?? 0) > ADMIN_IDLE_TIMEOUT) {
        unset($_SESSION['htbb_admin']);
        return null;
    }
    // Rút quyền ngay lập tức nếu tên bị gỡ khỏi ADMIN_ACCOUNTS giữa chừng.
    if (!laTaiKhoanAdmin((string)$a['username'])) {
        unset($_SESSION['htbb_admin']);
        return null;
    }
    $_SESSION['htbb_admin']['last'] = time();
    return $a;
}

function requireAdmin(): void
{
    if (!adminUser()) {
        header('Location: index.php');
        exit;
    }
}

function adminName(): string
{
    $a = adminUser();
    return $a ? (string)$a['username'] : '';
}

// ---------------------------------------------------------------------------
//  Nhật ký thao tác (bảng kpah_db.web_admin_log)
// ---------------------------------------------------------------------------

/** Bảng nhật ký đã được tạo chưa? Nhớ kết quả trong một lần chạy. */
function coBangNhatKy(): bool
{
    static $co = null;
    if ($co === null) {
        try {
            dbGame()->query('SELECT 1 FROM web_admin_log LIMIT 1');
            $co = true;
        } catch (Throwable $ex) {
            $co = false;
        }
    }
    return $co;
}

/**
 * Ghi một dòng nhật ký. Không bao giờ ném lỗi ra ngoài: mất nhật ký thì tiếc,
 * nhưng để nó làm hỏng luôn thao tác chính thì còn tệ hơn.
 */
function ghiNhatKy(string $hanhDong, string $doiTuong = '', string $chiTiet = ''): void
{
    if (!coBangNhatKy()) {
        return;
    }
    try {
        $st = dbGame()->prepare(
            'INSERT INTO web_admin_log (admin_user, hanh_dong, doi_tuong, chi_tiet, ip)
             VALUES (?, ?, ?, ?, ?)'
        );
        $st->execute([
            substr(adminName(), 0, 50),
            substr($hanhDong, 0, 50),
            substr($doiTuong, 0, 100),
            substr($chiTiet, 0, 1000),
            adminIp(),
        ]);
    } catch (Throwable $ex) {
        // im lặng
    }
}

/** Riêng lần đăng nhập thất bại: chưa có admin nên ghi tên người đã gõ vào. */
function ghiNhatKyDangNhapThatBai(string $usernameGoVao, string $lyDo): void
{
    if (!coBangNhatKy()) {
        $_SESSION['admin_fail'] = (int)($_SESSION['admin_fail'] ?? 0) + 1;
        $_SESSION['admin_fail_time'] = time();
        return;
    }
    try {
        $st = dbGame()->prepare(
            'INSERT INTO web_admin_log (admin_user, hanh_dong, doi_tuong, chi_tiet, ip)
             VALUES (?, ?, ?, ?, ?)'
        );
        $st->execute(['', 'dang_nhap_that_bai', substr($usernameGoVao, 0, 100), $lyDo, adminIp()]);
    } catch (Throwable $ex) {
    }
}

/** Số lần gõ sai gần đây từ địa chỉ IP này. */
function soLanGoSai(): int
{
    if (!coBangNhatKy()) {
        // Lùi về đếm trong session. Yếu (xoá cookie là hết), nhưng còn hơn không.
        if (time() - (int)($_SESSION['admin_fail_time'] ?? 0) > ADMIN_FAIL_WINDOW) {
            return 0;
        }
        return (int)($_SESSION['admin_fail'] ?? 0);
    }
    try {
        // Khoảng thời gian ghép thẳng vào câu lệnh chứ không dùng dấu ?: PDO đang
        // tắt giả lập prepare nên tham số luôn được gửi dạng chuỗi, mà
        // `INTERVAL '900' SECOND` thì MySQL không nhận. Giá trị là hằng số nguyên
        // của chính mình nên ghép vào là an toàn.
        $cua = (int)ADMIN_FAIL_WINDOW;
        $st = dbGame()->prepare(
            "SELECT COUNT(*) FROM web_admin_log
              WHERE hanh_dong = 'dang_nhap_that_bai' AND ip = ?
                AND thoi_gian > DATE_SUB(NOW(), INTERVAL $cua SECOND)"
        );
        $st->execute([adminIp()]);
        return (int)$st->fetchColumn();
    } catch (Throwable $ex) {
        return 0;
    }
}

function dangBiChanDoSai(): bool
{
    return soLanGoSai() >= ADMIN_MAX_FAIL;
}

// ---------------------------------------------------------------------------
//  Tiện ích dùng chung cho các trang
// ---------------------------------------------------------------------------

/** Đọc một ô của form POST. */
function p(string $key, string $macDinh = ''): string
{
    return is_string($_POST[$key] ?? null) ? trim($_POST[$key]) : $macDinh;
}

/** Đọc một tham số trên đường dẫn. */
function g(string $key, string $macDinh = ''): string
{
    return is_string($_GET[$key] ?? null) ? trim($_GET[$key]) : $macDinh;
}

/**
 * Mọi POST phải qua đây. Sai token CSRF thì quay về trang gọi, không làm gì cả.
 * (CSRF = một trang web khác lừa trình duyệt của bạn tự gửi form sang đây trong
 * lúc bạn còn đang đăng nhập.)
 */
function chanCsrf(string $quayVe): void
{
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        setFlash('error', 'Phiên làm việc hết hạn hoặc yêu cầu không hợp lệ. Hãy thử lại.');
        header('Location: ' . $quayVe);
        exit;
    }
}

/** Về trang trước đó kèm một lời nhắn. */
function veLai(string $url, string $loai, string $msg): void
{
    setFlash($loai, $msg);
    header('Location: ' . $url);
    exit;
}

/** Số có dấu chấm ngăn cách hàng nghìn. */
function so($n): string
{
    return number_format((float)$n, 0, ',', '.');
}

/** Tên nghề theo cột tob_char.class (MapCheTao.java:930). */
function tenNghe($class): string
{
    $ten = ['Kiếm khách', 'Chiến binh', 'Pháp sư', 'Đấu sĩ', 'Cung thủ'];
    return $ten[(int)$class] ?? ('Nghề ' . (int)$class);
}
