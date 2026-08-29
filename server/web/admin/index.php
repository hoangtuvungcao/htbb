<?php
/**
 * Trang quản trị — đăng nhập + bảng tổng quan (thống kê người chơi).
 *
 * Chưa đăng nhập  -> form đăng nhập bằng tài khoản game nằm trong ADMIN_ACCOUNTS.
 * Đã đăng nhập    -> thống kê tài khoản / nhân vật / giftcode.
 */
require_once __DIR__ . '/inc/layout.php';

$loi = [];
$tenDaGo = '';

// ---------------------------------------------------------------------------
//  Đăng nhập
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST' && !adminUser()) {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        $loi[] = 'Phiên làm việc hết hạn, hãy thử lại.';
    } elseif (dangBiChanDoSai()) {
        $loi[] = 'Đã gõ sai quá nhiều lần. Chờ 15 phút rồi thử lại.';
    } else {
        $tenDaGo  = strtolower(p('username'));
        $matKhau  = (string)($_POST['password'] ?? '');

        if ($tenDaGo === '' || $matKhau === '') {
            $loi[] = 'Nhập đủ tên tài khoản và mật khẩu.';
        } elseif (!adminAccounts()) {
            $loi[] = 'Chưa cấu hình ADMIN_ACCOUNTS nên trang quản trị đang đóng với tất cả mọi người.';
        } else {
            try {
                // So khớp mật khẩu bằng PASSWORD() y hệt login server của game
                // (CheckLoginSocket: select * from team_user where username=... and password=password(...)).
                $st = dbAccount()->prepare(
                    'SELECT id, username, ban FROM team_user WHERE username = ? AND `password` = PASSWORD(?) LIMIT 1'
                );
                $st->execute([$tenDaGo, $matKhau]);
                $row = $st->fetch();

                if (!$row) {
                    ghiNhatKyDangNhapThatBai($tenDaGo, 'sai tên hoặc mật khẩu');
                    $loi[] = 'Sai tên tài khoản hoặc mật khẩu.';
                } elseif (!laTaiKhoanAdmin((string)$row['username'])) {
                    // Mật khẩu ĐÚNG nhưng không nằm trong danh sách admin — đáng ghi lại.
                    ghiNhatKyDangNhapThatBai($tenDaGo, 'đúng mật khẩu nhưng không có quyền admin');
                    $loi[] = 'Tài khoản này không có quyền vào trang quản trị.';
                } elseif ((int)$row['ban'] === 1) {
                    ghiNhatKyDangNhapThatBai($tenDaGo, 'tài khoản admin đang bị khoá');
                    $loi[] = 'Tài khoản này đang bị khoá.';
                } else {
                    session_regenerate_id(true);
                    $_SESSION['kpah_admin'] = [
                        'id'       => (int)$row['id'],
                        'username' => (string)$row['username'],
                        'last'     => time(),
                    ];
                    ghiNhatKy('dang_nhap', (string)$row['username'], 'vào trang quản trị');
                    header('Location: index.php');
                    exit;
                }
            } catch (Throwable $ex) {
                // Khối này bắt CẢ hai loại lỗi, và chúng cần cách sửa khác hẳn nhau:
                //   - không kết nối được  -> MySQL chưa chạy, hoặc sai DB_USER/DB_PASS
                //   - kết nối được nhưng câu lệnh hỏng -> hay gặp nhất là máy chủ dùng
                //     MySQL 8, nơi hàm PASSWORD() đã bị gỡ (login server cũng sẽ hỏng theo)
                // Trước đây chỉ báo "không kết nối được" cho cả hai, làm người sửa đi
                // nhầm hướng. Giờ ghi nguyên văn ra log lỗi PHP và nói rõ chỗ để xem.
                error_log('[kpah-admin] dang nhap that bai: ' . get_class($ex) . ': ' . $ex->getMessage());
                $maLoi = ($ex instanceof PDOException && $ex->getCode() !== '') ? ' [' . $ex->getCode() . ']' : '';
                $loi[] = 'Cơ sở dữ liệu báo lỗi' . $maLoi . ' với '
                       . DB_USER . '@' . DB_HOST . ' (mật khẩu ' . strlen(DB_PASS) . ' ký tự).';
                // Mã 1045 "Access denied for user 'root'" gần như không bao giờ là
                // nguyên nhân thật — nó chỉ là hệ quả của việc không đọc được
                // server.ini rồi rơi xuống mặc định. Nói thẳng điều đó ra.
                $goiY = function_exists('kpahGoiYLoiKetNoi') ? kpahGoiYLoiKetNoi() : '';
                if ($goiY !== '') {
                    $loi[] = $goiY;
                }
                $loi[] = 'Soi kỹ bằng: sudo -u www-data php web/admin/test/chan_doan_ket_noi.php';
            }
        }
    }
}

// ---------------------------------------------------------------------------
//  Chưa đăng nhập -> form
// ---------------------------------------------------------------------------
if (!adminUser()) {
    adminHead('tongquan', 'Đăng nhập quản trị');
    ?>
    <section class="auth-wrap">
        <div class="auth-card">
            <h1 class="auth-title">Khu quản trị</h1>
            <p class="auth-sub">Đăng nhập bằng tài khoản game có quyền admin.</p>

            <?php foreach ($loi as $l): ?>
                <div class="flash flash-error"><?= e($l) ?></div>
            <?php endforeach; ?>

            <?php if (!adminAccounts()): ?>
                <div class="luuy">
                    <p><strong>Chưa ai được cấp quyền.</strong> Mở <code>web/config.local.php</code> trên máy chủ và thêm:</p>
                    <p><code>define('ADMIN_ACCOUNTS', 'tenTaiKhoanCuaBan');</code></p>
                    <p>Đó là <em>tên tài khoản đăng nhập</em> (cột <code>team_user.username</code>), không phải tên nhân vật.
                       Nhiều người thì ngăn nhau bằng dấu phẩy.</p>
                </div>
            <?php endif; ?>

            <form method="post" action="index.php" autocomplete="off">
                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                <label>Tên tài khoản
                    <input type="text" name="username" value="<?= e($tenDaGo) ?>" required autofocus>
                </label>
                <label>Mật khẩu
                    <input type="password" name="password" required>
                </label>
                <button type="submit" class="btn btn-primary btn-block">Đăng nhập</button>
            </form>
        </div>
    </section>
    <?php
    adminFoot();
    exit;
}

// ---------------------------------------------------------------------------
//  Đã đăng nhập -> thống kê
// ---------------------------------------------------------------------------

/** Chạy một câu đếm, trả về null nếu bảng không tồn tại (để hiện "—" thay vì vỡ trang). */
function dem(PDO $pdo, string $sql, array $args = []): ?int
{
    try {
        $st = $pdo->prepare($sql);
        $st->execute($args);
        return (int)$st->fetchColumn();
    } catch (Throwable $ex) {
        return null;
    }
}

function dsach(PDO $pdo, string $sql, array $args = []): array
{
    try {
        $st = $pdo->prepare($sql);
        $st->execute($args);
        return $st->fetchAll();
    } catch (Throwable $ex) {
        return [];
    }
}

function hienSo(?int $n): string
{
    return $n === null ? '—' : so($n);
}

// Mở kết nối riêng ở đây thay vì để lưới an toàn ở boot.php đỡ: MySQL tắt là
// tình huống hay gặp nhất, và một thẻ giải thích rõ ràng hữu ích hơn trang lỗi.
try {
    $acc  = dbAccount();
    $game = dbGame();
} catch (Throwable $ex) {
    adminHead('tongquan', 'Tổng quan');
    echo '<div class="page-head"><div><h1>Tổng quan</h1></div></div>';
    echo '<div class="card"><h3>Không kết nối được cơ sở dữ liệu</h3>'
       . '<p>Kiểm tra MySQL/MariaDB đã chạy chưa, và <code>DB_HOST</code> / <code>DB_USER</code> / '
       . '<code>DB_PASS</code> trong <code>web/config.local.php</code> có đúng không.</p>'
       . '<p class="muted small">Chi tiết: ' . e($ex->getMessage()) . '</p></div>';
    adminFoot();
    exit;
}

// Lấy "hôm nay" theo đồng hồ của MySQL, không theo PHP: hai bên có thể lệch múi giờ
// và biểu đồ sẽ trống một cột đầu / cuối nếu trộn lẫn.
try {
    $homNay = (string)$acc->query('SELECT CURDATE()')->fetchColumn();
} catch (Throwable $ex) {
    $homNay = date('Y-m-d');
}

// ---- Tài khoản ----
$tkTong     = dem($acc, 'SELECT COUNT(*) FROM team_user');
$tkKhoa     = dem($acc, 'SELECT COUNT(*) FROM team_user WHERE ban = 1');
$tkChoEmail = dem($acc, 'SELECT COUNT(*) FROM team_user WHERE ban = 3');
$tkHomNay   = dem($acc, 'SELECT COUNT(*) FROM team_user WHERE regdate >= CURDATE()');
$tk7Ngay    = dem($acc, 'SELECT COUNT(*) FROM team_user WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)');
$tk30Ngay   = dem($acc, 'SELECT COUNT(*) FROM team_user WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 29 DAY)');

// Chưa kích hoạt: cần đối chiếu hai database. Nếu tài khoản MySQL không có quyền
// đọc chéo thì lùi về phép trừ gần đúng.
$dbGameTen = preg_replace('/[^A-Za-z0-9_]/', '', DB_GAME);
$tkChuaKH  = dem($acc, "SELECT COUNT(*) FROM team_user t
                         WHERE NOT EXISTS (SELECT 1 FROM `$dbGameTen`.`5h_active` a WHERE a.username = t.username)");
$tkChuaKHUocLuong = false;
if ($tkChuaKH === null) {
    $daKH = dem($game, 'SELECT COUNT(DISTINCT username) FROM `5h_active`');
    if ($tkTong !== null && $daKH !== null) {
        $tkChuaKH = max(0, $tkTong - $daKH);
        $tkChuaKHUocLuong = true;
    }
}

// ---- Nhân vật ----
$nvTong   = dem($game, 'SELECT COUNT(*) FROM tob_char');
$nvSong   = dem($game, 'SELECT COUNT(*) FROM tob_char WHERE ban = 0 AND del = 1');
$nvKhoa   = dem($game, 'SELECT COUNT(*) FROM tob_char WHERE ban <> 0');
$nv24h    = dem($game, 'SELECT COUNT(*) FROM tob_char WHERE lastLog >= DATE_SUB(NOW(), INTERVAL 1 DAY)');
$nv7Ngay  = dem($game, 'SELECT COUNT(*) FROM tob_char WHERE lastLog >= DATE_SUB(NOW(), INTERVAL 7 DAY)');
$nv30Ngay = dem($game, 'SELECT COUNT(*) FROM tob_char WHERE lastLog >= DATE_SUB(NOW(), INTERVAL 30 DAY)');

// ---- Giftcode ----
$gcTong    = dem($game, 'SELECT COUNT(*) FROM giftcode');
$gcConDung = dem($game, 'SELECT COUNT(*) FROM giftcode WHERE type = 0 AND limit_use <> 0');
$gcLuotTong = dem($game, 'SELECT COUNT(*) FROM giftcode_log');

// ---- Người chơi online ----
// Bảng tob_log_online do chính server game ghi, 5 phút một mẫu (data/GhiSoOnline.java).
// Bảng tự tạo lúc server khởi động, nên trước lần khởi động đầu tiên sau bản cập nhật
// này thì chưa có bảng — mọi truy vấn dưới đây trả null/[] và phần hiển thị sẽ nói rõ.
// null = chưa có bảng; 0 = có bảng nhưng server chưa ghi mẫu nào. Hai tình huống này
// cần hai lời hướng dẫn khác hẳn nhau nên phải phân biệt được.
$onSoMau  = dem($game, 'SELECT COUNT(*) FROM tob_log_online');
$onCoBang = $onSoMau !== null;

$onMoiNhat = dsach($game, 'SELECT thoi_gian, ket_noi, nhan_vat,
                                  TIMESTAMPDIFF(MINUTE, thoi_gian, NOW()) AS phut_truoc
                             FROM tob_log_online ORDER BY id DESC LIMIT 1');
$onGanNhat = $onMoiNhat[0] ?? null;

// Mẫu cũ hơn 15 phút (3 chu kỳ) thì con số không còn là "đang online" nữa — nhiều khả
// năng server đang tắt. Hiển thị phải nói ra điều đó chứ không đưa số cũ như số thật.
$onCu = $onGanNhat !== null && (int)$onGanNhat['phut_truoc'] > 15;

$onDinhHomNay = dem($game, 'SELECT MAX(nhan_vat) FROM tob_log_online WHERE thoi_gian >= CURDATE()');
$onDinh7Ngay  = dem($game, 'SELECT MAX(nhan_vat) FROM tob_log_online
                             WHERE thoi_gian >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)');
$onTbHomNay   = dem($game, 'SELECT ROUND(AVG(nhan_vat)) FROM tob_log_online WHERE thoi_gian >= CURDATE()');

// 24 giờ gần nhất, gom theo từng giờ và lấy ĐỈNH của giờ đó (không lấy trung bình:
// đỉnh mới là con số dùng để tính tải máy chủ).
$bdOnlineGio = [];
foreach (dsach($game, "SELECT DATE_FORMAT(thoi_gian, '%Y-%m-%d %H') AS gio,
                              DATE_FORMAT(thoi_gian, '%Hh')          AS nhan,
                              MAX(nhan_vat)                          AS n
                         FROM tob_log_online
                        WHERE thoi_gian >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
                        GROUP BY gio, nhan ORDER BY gio") as $r) {
    $bdOnlineGio[] = ['nhan' => (string)$r['nhan'], 'gt' => (int)$r['n']];
}

// ---- Biểu đồ 14 ngày ----
function chuoi14Ngay(string $homNay, array $rows): array
{
    $theoNgay = [];
    foreach ($rows as $r) {
        $theoNgay[(string)$r['d']] = (int)$r['n'];
    }
    $out = [];
    for ($i = 13; $i >= 0; $i--) {
        $ngay = date('Y-m-d', strtotime($homNay . " -$i day"));
        $out[] = ['nhan' => date('d/m', strtotime($ngay)), 'gt' => $theoNgay[$ngay] ?? 0];
    }
    return $out;
}

$bdDangKy = chuoi14Ngay($homNay, dsach($acc,
    'SELECT DATE(regdate) AS d, COUNT(*) AS n FROM team_user
      WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY d'));

$bdHoatDong = chuoi14Ngay($homNay, dsach($game,
    'SELECT DATE(lastLog) AS d, COUNT(*) AS n FROM tob_char
      WHERE lastLog >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY d'));

// Đỉnh online mỗi ngày, 14 ngày. Dùng lại chuoi14Ngay nên ngày không có mẫu ra 0
// thay vì bị khuyết cột.
$bdOnlineNgay = chuoi14Ngay($homNay, dsach($game,
    'SELECT DATE(thoi_gian) AS d, MAX(nhan_vat) AS n FROM tob_log_online
      WHERE thoi_gian >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY d'));

// ---- Phân bố nghề / cấp ----
$phanBoNghe = [];
foreach (dsach($game, 'SELECT `class` AS c, COUNT(*) AS n FROM tob_char
                        WHERE ban = 0 AND del = 1 GROUP BY `class` ORDER BY n DESC') as $r) {
    $phanBoNghe[] = ['nhan' => tenNghe($r['c']), 'gt' => (int)$r['n']];
}

$phanBoCap = [];
foreach (dsach($game, "SELECT CASE
                          WHEN lastLv < 20 THEN '1'
                          WHEN lastLv < 40 THEN '2'
                          WHEN lastLv < 60 THEN '3'
                          WHEN lastLv < 70 THEN '4'
                          WHEN lastLv < 80 THEN '5'
                          ELSE '6' END AS nhom, COUNT(*) AS n
                        FROM tob_char WHERE ban = 0 AND del = 1 GROUP BY nhom ORDER BY nhom") as $r) {
    $ten = ['1' => 'Dưới 20', '2' => '20 – 39', '3' => '40 – 59',
            '4' => '60 – 69', '5' => '70 – 79', '6' => 'Từ 80'];
    $phanBoCap[] = ['nhan' => $ten[(string)$r['nhom']] ?? '?', 'gt' => (int)$r['n']];
}

// ---- Bảng xếp hạng nhanh ----
$topXp = dsach($game, 'SELECT charname, lastLv, xp, `class` FROM tob_char
                        WHERE ban = 0 AND del = 1 ORDER BY xp DESC LIMIT 10');
$topLuong = dsach($game, 'SELECT charname, lastLv, luong, luonglock FROM tob_char
                           WHERE ban = 0 AND del = 1 ORDER BY luong DESC LIMIT 10');
$topNap = dsach($game, 'SELECT charname, luongnap, luongxai FROM tob_nap_xai_luong
                         WHERE luongnap > 0 ORDER BY luongnap DESC LIMIT 10');

// ---- Tài khoản mới nhất ----
$tkMoi = dsach($acc, 'SELECT id, username, email, regdate, ban FROM team_user
                       ORDER BY regdate DESC LIMIT 8');

// ---- Thao tác admin gần nhất ----
$nhatKyMoi = coBangNhatKy()
    ? dsach($game, "SELECT admin_user, hanh_dong, doi_tuong, thoi_gian FROM web_admin_log
                     WHERE hanh_dong <> 'dang_nhap_that_bai' ORDER BY id DESC LIMIT 8")
    : [];

adminHead('tongquan', 'Tổng quan');
?>
<div class="page-head">
    <div>
        <h1>Tổng quan</h1>
        <p>Số liệu đọc trực tiếp từ <code><?= e(DB_ACCOUNT) ?></code> và <code><?= e(DB_GAME) ?></code> ngay lúc mở trang.</p>
    </div>
    <div class="ngang">
        <a href="taikhoan.php" class="btn btn-outline btn-nho">Tìm tài khoản</a>
        <a href="giftcode.php" class="btn btn-primary btn-nho">Tạo giftcode</a>
    </div>
</div>

<h3 class="muted small" style="letter-spacing:1px;text-transform:uppercase;">Người chơi online</h3>
<?php if (!$onCoBang): ?>
    <div class="card">
        <p>Chưa có bảng <code>tob_log_online</code>, nên chưa vẽ được gì.</p>
        <p class="muted small" style="margin:6px 0 0;">
            Server game tự tạo bảng này lúc khởi động (<code>data/GhiSoOnline.java</code>).
            Khởi động lại server một lần là bắt đầu ghi, 5 phút một mẫu, và giữ lại 30 ngày.
            Muốn có bảng ngay thì chạy <code>sql/them_bang_tob_log_online.sql</code>.
        </p>
    </div>
<?php elseif ((int)$onSoMau === 0): ?>
    <div class="card">
        <p><strong>Đã có bảng <code>tob_log_online</code> nhưng chưa có một dòng nào.</strong>
           Nghĩa là bảng được tạo bằng tay, còn server game thì chưa hề ghi mẫu.</p>
        <p class="muted small" style="margin:6px 0 0;">
            Bộ lấy mẫu nằm trong <code>data/GhiSoOnline.java</code> — nó chỉ chạy khi
            <code>KPAH2_patched.jar</code> đã được build lại từ mã nguồn hiện tại và server
            đã khởi động lại. Sửa file <code>.java</code> thôi thì server đang chạy vẫn dùng
            jar cũ và không có gì thay đổi.
        </p>
        <p class="muted small" style="margin:6px 0 0;">
            Kiểm tra nhanh xem jar đã có lớp đó chưa:
            <code>jar tf KPAH2_patched.jar | grep GhiSoOnline</code> — không ra dòng nào
            tức là chưa build lại.
        </p>
    </div>
<?php else: ?>
<div class="stat-grid">
    <?php
    if ($onGanNhat === null) {
        theSo('Đang online', '—', 'chưa ghi được mẫu nào');
    } elseif ($onCu) {
        // Số cũ KHÔNG được trình bày như số hiện tại: mẫu ngừng lại gần như luôn có
        // nghĩa là server game đã tắt.
        theSo('Đang online', '—',
            'mẫu gần nhất cách đây ' . so((int)$onGanNhat['phut_truoc']) . ' phút — server game có thể đang tắt',
            'nguy-hiem');
    } else {
        theSo('Đang online', so((int)$onGanNhat['nhan_vat']),
            so((int)$onGanNhat['ket_noi']) . ' kết nối · đo ' . so((int)$onGanNhat['phut_truoc']) . ' phút trước',
            'tot');
    }
    theSo('Đỉnh hôm nay', hienSo($onDinhHomNay), 'lúc đông nhất kể từ 0h');
    theSo('Đỉnh 7 ngày', hienSo($onDinh7Ngay));
    theSo('Trung bình hôm nay', hienSo($onTbHomNay), 'bình quân các mẫu 5 phút');
    ?>
</div>

<div class="grid-2">
    <div class="card">
        <h3>Online · 24 giờ qua</h3>
        <?php bieuDoCot($bdOnlineGio, 'người'); ?>
        <p class="muted small" style="margin:10px 0 0;">
            Mỗi cột là <em>đỉnh</em> của giờ đó, không phải trung bình — đỉnh mới là con số
            quyết định tải máy chủ.
        </p>
    </div>
    <div class="card">
        <h3>Đỉnh online · 14 ngày</h3>
        <?php bieuDoCot($bdOnlineNgay, 'người'); ?>
        <p class="muted small" style="margin:10px 0 0;">
            Đếm <em>nhân vật đang trong game</em>. Số kết nối luôn cao hơn vì tính cả người
            còn ở màn hình đăng nhập hoặc chọn nhân vật.
        </p>
    </div>
</div>
<?php endif; ?>

<h3 class="muted small" style="letter-spacing:1px;text-transform:uppercase;">Tài khoản</h3>
<div class="stat-grid">
    <?php theSo('Tổng tài khoản', hienSo($tkTong)); ?>
    <?php theSo('Đăng ký hôm nay', hienSo($tkHomNay), '7 ngày: ' . hienSo($tk7Ngay) . ' · 30 ngày: ' . hienSo($tk30Ngay), 'tot'); ?>
    <?php theSo('Đang bị khoá', hienSo($tkKhoa), 'ban = 1, không đăng nhập được', ($tkKhoa ?: 0) > 0 ? 'nguy-hiem' : ''); ?>
    <?php theSo('Chưa kích hoạt', hienSo($tkChuaKH), $tkChuaKHUocLuong ? 'ước lượng (không đọc chéo được 2 DB)' : 'chưa có trong 5h_active', 'canh-bao'); ?>
    <?php if (($tkChoEmail ?: 0) > 0) { theSo('Chờ xác nhận email', hienSo($tkChoEmail), 'ban = 3', 'canh-bao'); } ?>
</div>

<h3 class="muted small" style="letter-spacing:1px;text-transform:uppercase;">Nhân vật</h3>
<div class="stat-grid">
    <?php theSo('Tổng nhân vật', hienSo($nvTong), 'đang chơi được: ' . hienSo($nvSong)); ?>
    <?php theSo('Vào trong 24 giờ', hienSo($nv24h), '7 ngày: ' . hienSo($nv7Ngay) . ' · 30 ngày: ' . hienSo($nv30Ngay), 'tot'); ?>
    <?php theSo('Nhân vật bị khoá', hienSo($nvKhoa), 'biến mất khỏi màn chọn nhân vật', ($nvKhoa ?: 0) > 0 ? 'nguy-hiem' : ''); ?>
    <?php theSo('Mã quà còn lượt', hienSo($gcConDung), 'trên tổng ' . hienSo($gcTong) . ' mã · đã phát ' . hienSo($gcLuotTong) . ' lượt'); ?>
</div>

<div class="grid-2">
    <div class="card">
        <h3>Tài khoản đăng ký · 14 ngày</h3>
        <?php bieuDoCot($bdDangKy, 'tài khoản'); ?>
    </div>
    <div class="card">
        <h3>Nhân vật vào game · 14 ngày</h3>
        <?php bieuDoCot($bdHoatDong, 'nhân vật'); ?>
        <p class="muted small" style="margin:10px 0 0;">
            Đếm theo cột <code>lastLog</code> — <em>lần vào cuối cùng</em> của mỗi nhân vật, nên một
            nhân vật chỉ nằm ở đúng một cột. Người chơi vào đều mỗi ngày sẽ luôn đứng ở cột hôm nay.
        </p>
    </div>
</div>

<div class="grid-2">
    <div class="card">
        <h3>Phân bố nghề</h3>
        <?php thanhNgang($phanBoNghe); ?>
    </div>
    <div class="card">
        <h3>Phân bố cấp độ</h3>
        <?php thanhNgang($phanBoCap); ?>
    </div>
</div>

<div class="grid-3">
    <div class="card">
        <h3>Cao cấp nhất</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Nhân vật</th><th class="num">Cấp</th></tr></thead>
            <tbody>
            <?php foreach ($topXp as $r): ?>
                <tr>
                    <td><a href="nhanvat.php?q=<?= e(urlencode($r['charname'])) ?>"><?= e($r['charname']) ?></a>
                        <div class="muted small"><?= e(tenNghe($r['class'])) ?></div></td>
                    <td class="num"><?= (int)$r['lastLv'] ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$topXp): ?><tr><td colspan="2" class="muted">Chưa có nhân vật.</td></tr><?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>

    <div class="card">
        <h3>Nhiều lượng nhất</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Nhân vật</th><th class="num">Lượng</th></tr></thead>
            <tbody>
            <?php foreach ($topLuong as $r): ?>
                <tr>
                    <td><a href="nhanvat.php?q=<?= e(urlencode($r['charname'])) ?>"><?= e($r['charname']) ?></a></td>
                    <td class="num"><?= so($r['luong']) ?>
                        <div class="muted small">khoá <?= so($r['luonglock']) ?></div></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$topLuong): ?><tr><td colspan="2" class="muted">Chưa có nhân vật.</td></tr><?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>

    <div class="card">
        <h3>Nạp nhiều nhất</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Nhân vật</th><th class="num">Đã nạp</th></tr></thead>
            <tbody>
            <?php foreach ($topNap as $r): ?>
                <tr>
                    <td><a href="nhanvat.php?q=<?= e(urlencode($r['charname'])) ?>"><?= e($r['charname']) ?></a></td>
                    <td class="num"><?= so($r['luongnap']) ?>
                        <div class="muted small">xài <?= so($r['luongxai']) ?></div></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$topNap): ?><tr><td colspan="2" class="muted">Bảng <code>tob_nap_xai_luong</code> chưa có dữ liệu.</td></tr><?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>
</div>

<div class="grid-2">
    <div class="card">
        <h3>Tài khoản mới nhất</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Tài khoản</th><th>Đăng ký</th><th>Trạng thái</th></tr></thead>
            <tbody>
            <?php foreach ($tkMoi as $r): ?>
                <tr>
                    <td><a href="taikhoan.php?id=<?= (int)$r['id'] ?>"><?= e($r['username']) ?></a>
                        <?php if (!empty($r['email'])): ?><div class="muted small"><?= e($r['email']) ?></div><?php endif; ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['regdate']) ?></td>
                    <td><?php if ((int)$r['ban'] === 1): ?>
                            <span class="nhan nhan-khoa">Bị khoá</span>
                        <?php else: ?>
                            <span class="nhan nhan-ok">Bình thường</span>
                        <?php endif; ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$tkMoi): ?><tr><td colspan="3" class="muted">Chưa có tài khoản nào.</td></tr><?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>

    <div class="card">
        <h3>Thao tác quản trị gần nhất</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Người làm</th><th>Việc</th><th>Lúc</th></tr></thead>
            <tbody>
            <?php foreach ($nhatKyMoi as $r): ?>
                <tr>
                    <td><?= e($r['admin_user']) ?></td>
                    <td><?= e($r['hanh_dong']) ?> <span class="muted"><?= e($r['doi_tuong']) ?></span></td>
                    <td class="nowrap muted small"><?= e((string)$r['thoi_gian']) ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$nhatKyMoi): ?>
                <tr><td colspan="3" class="muted">Chưa có thao tác nào được ghi.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
        <p style="margin:12px 0 0;"><a href="nhatky.php">Xem toàn bộ nhật ký →</a></p>
    </div>
</div>
<?php adminFoot(); ?>
