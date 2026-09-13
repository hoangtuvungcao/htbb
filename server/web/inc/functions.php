<?php
require_once __DIR__ . '/db.php';

if (session_status() === PHP_SESSION_NONE) {
    /*
     * Cứng hoá cookie phiên TRƯỚC khi mở phiên — đặt sau session_start() thì
     * cookie đã gửi đi rồi, không sửa được nữa.
     *
     *  httponly : JavaScript không đọc được cookie. Một lỗi XSS ở bất kỳ trang
     *             nào trong web/ (kể cả trang giới thiệu) sẽ không đánh cắp
     *             được phiên admin đang mở ở tab khác.
     *  samesite : trình duyệt không gửi cookie theo yêu cầu từ site khác. Đây là
     *             lớp thứ hai sau token CSRF — token bảo vệ form, cờ này bảo vệ
     *             cả những đường mà form không đi qua. 'Lax' chứ không 'Strict'
     *             vì Strict làm mất phiên khi bấm link từ ngoài vào.
     *  secure   : CHỈ đặt khi đang thật sự chạy HTTPS. Đặt cứng thành true trên
     *             máy chủ còn HTTP là trình duyệt không gửi cookie nữa và không
     *             ai đăng nhập được — kể cả bạn.
     */
    $dangHttps = (($_SERVER['HTTPS'] ?? '') !== '' && ($_SERVER['HTTPS'] ?? '') !== 'off')
        || (int)($_SERVER['SERVER_PORT'] ?? 0) === 443
        || strtolower((string)($_SERVER['HTTP_X_FORWARDED_PROTO'] ?? '')) === 'https';

    if (PHP_SAPI !== 'cli') {
        // Chống "session fixation": bỏ qua mã phiên do người gửi tự nghĩ ra thay
        // vì nhận lấy rồi dùng luôn. Kẻ tấn công không thể dúi trước một mã phiên
        // rồi đợi bạn đăng nhập vào chính mã đó.
        @ini_set('session.use_strict_mode', '1');
        @session_set_cookie_params([
            'lifetime' => 0,
            'path'     => '/',
            'httponly' => true,
            'samesite' => 'Lax',
            'secure'   => $dangHttps,
        ]);
    }
    session_start();
}

/** Escape HTML để in ra an toàn. */
function e(?string $s): string
{
    return htmlspecialchars((string)$s, ENT_QUOTES, 'UTF-8');
}

/** Tạo/đọc CSRF token. */
function csrfToken(): string
{
    if (empty($_SESSION['csrf'])) {
        $_SESSION['csrf'] = bin2hex(random_bytes(16));
    }
    return $_SESSION['csrf'];
}

function csrfCheck(?string $token): bool
{
    return is_string($token) && !empty($_SESSION['csrf']) && hash_equals($_SESSION['csrf'], $token);
}

/** Kiểm tra username: 4-20 ký tự, chỉ a-z 0-9 (khớp với server). */
function validUsername(string $u): bool
{
    return (bool)preg_match('/^[a-z0-9]{' . USERNAME_MIN . ',' . USERNAME_MAX . '}$/', $u);
}

function validPassword(string $p): bool
{
    $len = strlen($p);
    return $len >= PASSWORD_MIN && $len <= PASSWORD_MAX;
}

/** Tài khoản đang đăng nhập trên website (nếu có). */
function currentUser(): ?array
{
    return $_SESSION['user'] ?? null;
}

function requireLogin(): void
{
    if (!currentUser()) {
        header('Location: login.php');
        exit;
    }
}

/** Flash message qua session. */
function setFlash(string $type, string $msg): void
{
    $_SESSION['flash'] = ['type' => $type, 'msg' => $msg];
}

function takeFlash(): ?array
{
    if (!empty($_SESSION['flash'])) {
        $f = $_SESSION['flash'];
        unset($_SESSION['flash']);
        return $f;
    }
    return null;
}

/**
 * ============================================================
 *  CHẶN LÀM HÀNG LOẠT (bảng kpah_db.web_chan_spam)
 * ============================================================
 *  Token CSRF KHÔNG phải là giới hạn số lần: một script chỉ cần tải trang để
 *  lấy token rồi gửi form, lặp lại bao nhiêu lần tuỳ thích. Đây mới là chỗ đếm.
 *
 *  Thiếu bảng thì lùi về đếm trong session — yếu hơn nhiều (xoá cookie là đếm
 *  lại từ đầu) nhưng không bao giờ chặn người dùng thật vì lỗi kỹ thuật.
 */

function ipKhach(): string
{
    // Không tin X-Forwarded-For trừ khi thật sự có proxy phía trước và proxy đó
    // ghi đè header. Tin nhầm = kẻ tấn công tự khai IP mới cho mỗi lần gửi.
    return substr((string)($_SERVER['REMOTE_ADDR'] ?? ''), 0, 45);
}

/** Bảng đếm đã được tạo chưa? Nhớ kết quả trong một lần chạy. */
function coBangChanSpam(): bool
{
    static $co = null;
    if ($co === null) {
        try {
            dbGame()->query('SELECT 1 FROM web_chan_spam LIMIT 1');
            $co = true;
        } catch (Throwable $ex) {
            $co = false;
        }
    }
    return $co;
}

/** Hạn giữ lịch sử trong session (giây). Dọn rác ở đây, không dọn lúc kiểm tra. */
const CHAN_SPAM_HAN_GIU = 86400;

/** Ghi nhận vừa làm một việc. */
function ghiLanLam(string $hanhDong): void
{
    if (!coBangChanSpam()) {
        // Dọn rác theo một hạn giữ CỐ ĐỊNH, không theo cửa sổ của phép kiểm tra:
        // dọn theo cửa sổ sẽ xoá mất lịch sử mà một phép kiểm tra khác (cửa sổ
        // dài hơn) trên cùng hành động vẫn còn cần.
        $moc = time() - CHAN_SPAM_HAN_GIU;
        $ds = array_values(array_filter(
            $_SESSION['chan_spam'][$hanhDong] ?? [],
            static fn($t) => $t > $moc
        ));
        $ds[] = time();
        $_SESSION['chan_spam'][$hanhDong] = $ds;
        return;
    }
    try {
        $st = dbGame()->prepare('INSERT INTO web_chan_spam (ip, hanh_dong) VALUES (?, ?)');
        $st->execute([ipKhach(), substr($hanhDong, 0, 30)]);
    } catch (Throwable $ex) {
    }
}

/**
 * Đã làm việc này quá nhiều lần trong khoảng thời gian vừa qua chưa?
 *
 * @param int $soLanToiDa  số lần cho phép
 * @param int $trongGiay   trong bao nhiêu giây gần nhất
 * @return bool true = ĐANG BỊ CHẶN
 */
function biChanSpam(string $hanhDong, int $soLanToiDa, int $trongGiay): bool
{
    if (!coBangChanSpam()) {
        // CHỈ ĐẾM, không sửa gì. Trước đây hàm này dọn luôn các mốc nằm ngoài cửa
        // sổ, nên gọi kiểm tra với cửa sổ ngắn sẽ xoá mất lịch sử mà lần kiểm tra
        // sau (cửa sổ dài hơn) còn cần — phép kiểm tra không được có tác dụng phụ.
        $moc = time() - $trongGiay;
        $n = 0;
        foreach ($_SESSION['chan_spam'][$hanhDong] ?? [] as $t) {
            if ($t > $moc) {
                $n++;
            }
        }
        return $n >= $soLanToiDa;
    }
    try {
        // Khoảng thời gian ghép thẳng bằng số nguyên đã ép kiểu: PDO đang tắt
        // giả lập prepare nên tham số gửi đi dạng chuỗi, mà MySQL không nhận
        // `INTERVAL '3600' SECOND`.
        $giay = (int)$trongGiay;
        $st = dbGame()->prepare(
            "SELECT COUNT(*) FROM web_chan_spam
              WHERE ip = ? AND hanh_dong = ?
                AND thoi_gian > DATE_SUB(NOW(), INTERVAL $giay SECOND)"
        );
        $st->execute([ipKhach(), $hanhDong]);
        return (int)$st->fetchColumn() >= $soLanToiDa;
    } catch (Throwable $ex) {
        // Đếm hỏng thì cho qua: thà bỏ lọt còn hơn chặn oan người chơi thật.
        return false;
    }
}

/** Tài khoản còn hạn kích hoạt trong 5h_active hay không. */
function isActivated(string $username): bool
{
    try {
                $st = dbGame()->prepare(
                        "SELECT 1 FROM `5h_active`
                         WHERE username = ? AND time_end <> '-1'
                             AND CAST(time_end AS UNSIGNED) > UNIX_TIMESTAMP()
                         LIMIT 1"
                );
        $st->execute([$username]);
        return (bool)$st->fetchColumn();
    } catch (Throwable $ex) {
        return false;
    }
}

/** Admin gia hạn tài khoản 30 ngày; nếu còn hạn thì cộng nối tiếp từ hạn cũ. */
function activateAccount(int $userId, string $username): bool
{
    $db = dbGame();
    try {
        $db->beginTransaction();
        $st = $db->prepare(
            "SELECT MAX(CASE WHEN time_end <> '-1' THEN CAST(time_end AS UNSIGNED) ELSE 0 END)
             FROM `5h_active` WHERE userID = ? OR username = ? FOR UPDATE"
        );
        $st->execute([$userId, $username]);
        $cu = (int)$st->fetchColumn();
        $batDau = max(time(), $cu);
        $timeEnd = $batDau + 30 * 86400;

        $del = $db->prepare('DELETE FROM `5h_active` WHERE userID = ? OR username = ?');
        $del->execute([$userId, $username]);
        $in = $db->prepare('INSERT INTO `5h_active` (userID, username, time_end) VALUES (?, ?, ?)');
        $in->execute([$userId, $username, (string)$timeEnd]);
        $db->commit();
        return true;
    } catch (Throwable $ex) {
        if ($db->inTransaction()) {
            $db->rollBack();
        }
        return false;
    }
}

/**
 * ============================================================
 *  NẠP TIỀN THEO MỐC (dùng bảng tob_nap_moc trong kpah_db, xem vps/db_new_server.sql PHAN 2I)
 * ============================================================
 */

/** Danh sách 3 mốc tân thủ: giá tiền (VNĐ) + phần thưởng hiển thị trên web. */
function mocNapList(): array
{
    return [
        1 => [
            'gia' => 49000,
            'items' => ['100.000 lượng mở', '200.000 lượng khóa'],
        ],
        2 => [
            'gia' => 99000,
            'items' => [
                '1 Trứng Yêu Tinh', '1 Trứng Cú', '2 Huyết Linh Thảo',
                '100.000 lượng mở', '200.000 lượng khóa',
            ],
        ],
        3 => [
            'gia' => 299000,
            'items' => [
                '1 Trứng Đại Bàng', '1 Trứng Rồng', '2 Huyết Linh Thảo',
                'Phượng Hoàng HOẶC Phượng Hoàng Băng (chọn khi nhận tại NPC Tổng Quản)',
                'Vũ khí thời trang max chỉ số (7 ngày)',
                '100.000 lượng mở', '200.000 lượng khóa',
            ],
        ],
    ];
}

/** Sinh mã đơn ngẫu nhiên dùng làm nội dung chuyển khoản, ví dụ KPX7F3AQ2. */
function taoMaDon(): string
{
    $chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; // bỏ ký tự dễ nhầm (0/O, 1/I)
    $code = '';
    for ($i = 0; $i < 8; $i++) {
        $code .= $chars[random_int(0, strlen($chars) - 1)];
    }
    return 'KP' . $code;
}

/** URL ảnh QR VietQR (tương thích quét bằng app TPBank) cho 1 số tiền + nội dung CK. */
function vietQrUrl(int $amount, string $maDon): string
{
    $path = TPBANK_BIN . '-' . rawurlencode(TPBANK_ACCOUNT_NO) . '-compact2.png';
    $query = http_build_query([
        'amount' => $amount,
        'addInfo' => $maDon,
        'accountName' => TPBANK_ACCOUNT_NAME,
    ]);
    return 'https://img.vietqr.io/image/' . $path . '?' . $query;
}

/** Tạo 1 đơn nạp mốc mới (trạng thái chờ thanh toán) cho tài khoản đang đăng nhập. */
function taoDonNapMoc(int $userID, int $moc): array
{
    $list = mocNapList();
    if (!isset($list[$moc])) {
        throw new InvalidArgumentException('Mốc không hợp lệ');
    }
    $amount = (int)$list[$moc]['gia'];

    do {
        $maDon = taoMaDon();
        $chk = dbGame()->prepare('SELECT 1 FROM tob_nap_moc WHERE ma_don = ?');
        $chk->execute([$maDon]);
    } while ($chk->fetchColumn());

    $ins = dbGame()->prepare('INSERT INTO tob_nap_moc (userID, moc, amount, ma_don) VALUES (?, ?, ?, ?)');
    $ins->execute([$userID, $moc, $amount, $maDon]);

    return ['ma_don' => $maDon, 'amount' => $amount, 'moc' => $moc];
}

/** Lấy 1 đơn nạp mốc theo mã đơn (để hiển thị QR / kiểm tra trạng thái). */
function layDonNapMoc(string $maDon): ?array
{
    $st = dbGame()->prepare('SELECT * FROM tob_nap_moc WHERE ma_don = ? LIMIT 1');
    $st->execute([$maDon]);
    $row = $st->fetch();
    return $row ?: null;
}
