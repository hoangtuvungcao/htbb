<?php
/**
 * Khung trang cho khu quản trị.
 *
 * Không dùng chung inc/header.php của trang công khai, và cũng KHÔNG nạp
 * assets/style.css của trang đó nữa: bảng màu vàng–nâu bên kia sinh ra để quảng
 * bá game, đọc bảng số liệu thì tương phản kém. Khu này có bộ giao diện riêng,
 * tự đủ, ở assets/admin.css.
 *
 * Bố cục: cột điều hướng dính bên trái + thanh trên mỏng chỉ đường + vùng nội
 * dung. Toàn bộ bằng CSS, không một dòng JavaScript — trang quản trị phải mở
 * được cả khi máy chủ không ra được Internet và khi trình duyệt chặn script.
 */
require_once __DIR__ . '/boot.php';

/**
 * Menu bên trái, chia nhóm.
 *
 * Khoá của từng mục phải khớp tham số $trang mà mỗi trang truyền cho adminHead().
 *
 * @return array<string, array<string, array{0:string,1:string,2:string}>>
 */
function adminMenu(): array
{
    return [
        'Theo dõi' => [
            'tongquan' => ['index.php', 'Tổng quan', 'bang'],
        ],
        'Người chơi' => [
            'giam_level' => ['giam_level.php', 'Giảm Level', 'nguoi'],
            'taikhoan'     => ['taikhoan.php',     'Tài khoản',   'nguoi'],
            'tao_hang_loat'=> ['tao_hang_loat.php', 'Tạo hàng loạt', 'nhieu'],
            'nhanvat'      => ['nhanvat.php',       'Nhân vật',    'khien'],
            'cammay'       => ['cammay.php',        'Cấm theo máy','khoa'],
            'reset_toanbo' => ['reset_toanbo.php',  'Reset toàn server', 'khoa'],
        ],
        'Vận hành' => [
            'sukien'   => ['sukien.php',   'Sự kiện', 'lich'],
            'giftcode' => ['giftcode.php', 'Giftcode', 'qua'],
            'cauhinh'  => ['cauhinh.php',  'Cấu hình server', 'nhip'],
            'server'   => ['server.php',   'Bật/tắt server', 'khoa'],
        ],
        'Ghi chép' => [
            'log'      => ['log.php',      'Log game',      'danhsach'],
            'chandoan' => ['chandoan.php', 'Chẩn đoán',     'nhip'],
            'nhatky'   => ['nhatky.php',   'Nhật ký admin', 'dongho'],
        ],
    ];
}

/**
 * Biểu tượng 16px dạng nét, nhúng thẳng vào HTML.
 *
 * Không dùng font biểu tượng hay ảnh ngoài: máy chủ game có thể không ra được
 * Internet, và một biểu tượng không tải được sẽ để lại ô vuông trống.
 */
function bieuTuong(string $ten): string
{
    $hinh = [
        'bang'     => '<rect x="2" y="2" width="5" height="5" rx="1.2"/><rect x="9" y="2" width="5" height="5" rx="1.2"/>'
                    . '<rect x="2" y="9" width="5" height="5" rx="1.2"/><rect x="9" y="9" width="5" height="5" rx="1.2"/>',
        'nguoi'    => '<circle cx="8" cy="5.2" r="2.6"/><path d="M2.7 14c.5-2.8 2.6-4.3 5.3-4.3s4.8 1.5 5.3 4.3"/>',
        'nhieu'    => '<circle cx="5.6" cy="5.4" r="2.3"/><path d="M1.4 14c.5-2.6 2.1-4 4.2-4s3.7 1.4 4.2 4"/>'
                    . '<circle cx="11.4" cy="4.7" r="1.7"/><path d="M9.7 6.5c.6-.4 1.3-.6 2-.5 1.7.2 2.9 1.4 3.2 3.3"/>',
        'khien'    => '<path d="M8 1.7l5.2 2v4c0 3.2-2.1 6-5.2 6.9C4.9 13.7 2.8 10.9 2.8 7.7v-4l5.2-2z"/>',
        'qua'      => '<rect x="2.2" y="6.6" width="11.6" height="7.4" rx="1.1"/><rect x="1.3" y="3.9" width="13.4" height="2.7" rx=".9"/>'
                    . '<path d="M8 3.9V14"/><path d="M8 3.9S7 1.6 5.6 1.6a1.5 1.5 0 100 2.3"/><path d="M8 3.9s1-2.3 2.4-2.3a1.5 1.5 0 010 2.3"/>',
        'danhsach' => '<path d="M6 4h8M6 8h8M6 12h8"/><circle cx="2.9" cy="4" r=".95" fill="currentColor" stroke="none"/>'
                    . '<circle cx="2.9" cy="8" r=".95" fill="currentColor" stroke="none"/>'
                    . '<circle cx="2.9" cy="12" r=".95" fill="currentColor" stroke="none"/>',
        'khoa'     => '<rect x="3" y="7" width="10" height="7.2" rx="1.4"/>'
                    . '<path d="M5.4 7V4.9a2.6 2.6 0 015.2 0V7"/>',
        'nhip'     => '<path d="M1.4 8h3.1l1.9-4.6L9 12.6l1.9-4.6h3.7"/>',
        'lich'     => '<rect x="2.1" y="3.2" width="11.8" height="10.6" rx="1.4"/>'
                    . '<path d="M2.1 6.6h11.8"/><path d="M5.4 1.8v2.6"/><path d="M10.6 1.8v2.6"/>'
                    . '<circle cx="8" cy="10.2" r="1.15" fill="currentColor" stroke="none"/>',
        'dongho'   => '<circle cx="8" cy="8" r="6.2"/><path d="M8 4.5V8l2.4 1.5"/>',
        'nha'      => '<path d="M2.4 7.3L8 2.7l5.6 4.6"/><path d="M3.9 8.4v5h8.2v-5"/>',
        'ra'       => '<path d="M6.4 2.7H3.3a1.2 1.2 0 00-1.2 1.2v8.2a1.2 1.2 0 001.2 1.2h3.1"/>'
                    . '<path d="M10.6 11.2L13.8 8l-3.2-3.2"/><path d="M13.8 8H6.4"/>',
    ];
    if (!isset($hinh[$ten])) {
        return '';
    }
    // width/height ghi thẳng vào thẻ, KHÔNG chỉ dựa vào CSS: một tệp SVG không có
    // kích thước gốc sẽ phình ra bằng cả khung chứa nếu vì lý do gì đó admin.css
    // không nạp được — lúc đó trang không chỉ mất định dạng mà không còn đọc nổi.
    return '<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor"'
         . ' stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
         . $hinh[$ten] . '</svg>';
}

/**
 * Đường dẫn một tệp tĩnh, kèm dấu phiên bản lấy từ thời điểm sửa tệp.
 *
 * CẠM BẪY ĐÃ NỔ MỘT LẦN (29/08/2026): Apache trên VPS không gửi `Cache-Control`
 * lẫn `Expires` cho tệp .css, chỉ có `Last-Modified`. Gặp vậy trình duyệt tự đoán
 * thời hạn (thường ~10% tuổi của tệp) và DÙNG LẠI BẢN CŨ mà không hỏi lại máy chủ.
 * Hôm đổi giao diện, người dùng nhận HTML mới nhưng vẫn xài admin.css cũ trong bộ
 * nhớ đệm — mà bản cũ chỉ là phần bổ sung cho style.css, không có body, không có
 * bố cục, không có màu liên kết. Kết quả: trang hiện ra trần trụi như chưa từng
 * có CSS, và không có lỗi nào để lần.
 *
 * Gắn ?v=<mtime> làm đổi hẳn địa chỉ mỗi lần tệp đổi, nên bộ nhớ đệm không khớp
 * được nữa. Không cần ai phải bấm Ctrl+F5.
 */
function tepTinh(string $duongDan): string
{
    $that = __DIR__ . '/../' . $duongDan;
    $mtime = is_file($that) ? (int)filemtime($that) : 0;
    return $duongDan . ($mtime > 0 ? '?v=' . $mtime : '');
}

/** Tên nhóm chứa trang đang mở — dùng cho dòng chỉ đường ở thanh trên. */
function nhomCuaTrang(string $trang): string
{
    foreach (adminMenu() as $tenNhom => $muc) {
        if (isset($muc[$trang])) {
            return $tenNhom;
        }
    }
    return '';
}

function adminHead(string $trang, string $tieuDe): void
{
    $u     = adminUser();
    $flash = takeFlash();
    $nhom  = nhomCuaTrang($trang);
    ?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <meta name="color-scheme" content="dark">
    <title><?= e($tieuDe) ?> · Quản trị KPAH</title>
    <link rel="stylesheet" href="<?= e(tepTinh('assets/admin.css')) ?>">
    <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>🛡️</text></svg>">
</head>
<body class="admin-body<?= $u ? '' : ' admin-body--auth' ?>">
<?php if ($u): ?>
<div class="admin-shell">

    <aside class="side">
        <a class="side-brand" href="index.php">
            <span class="side-mark">🛡️</span>
            <span>
                <strong>Quản trị</strong>
                <small><?= e(SITE_NAME) ?></small>
            </span>
        </a>

        <nav class="side-nav">
            <?php foreach (adminMenu() as $tenNhom => $muc): ?>
                <div class="side-group"><?= e($tenNhom) ?></div>
                <?php foreach ($muc as $key => $m): ?>
                    <a href="<?= e($m[0]) ?>" class="side-link<?= $trang === $key ? ' active' : '' ?>"
                       <?= $trang === $key ? 'aria-current="page"' : '' ?>>
                        <?= bieuTuong($m[2]) ?><span><?= e($m[1]) ?></span>
                    </a>
                <?php endforeach; ?>
            <?php endforeach; ?>
        </nav>

        <div class="side-foot">
            <a class="side-link" href="../index.php"><?= bieuTuong('nha') ?><span>Ra trang chủ</span></a>
            <a class="side-link" href="dangxuat.php"><?= bieuTuong('ra') ?><span>Thoát</span></a>
            <div class="side-user"><span class="side-cham"></span><?= e($u['username']) ?></div>
        </div>
    </aside>

    <div class="admin-col">
        <header class="topbar">
            <div class="duongdan">
                <?php if ($nhom !== ''): ?>
                    <span class="duongdan-nhom"><?= e($nhom) ?></span><span>/</span>
                <?php endif; ?>
                <b><?= e($tieuDe) ?></b>
            </div>
            <div class="topbar-phai">
                <a class="btn btn-xam btn-nho" href="../index.php">Trang chủ</a>
                <a class="btn btn-xam btn-nho" href="dangxuat.php">Thoát</a>
            </div>
        </header>

        <main class="admin-main">
<?php else: ?>
    <main class="auth-wrap">
<?php endif; ?>

<?php if ($flash): ?>
    <div class="flash flash-<?= e($flash['type']) ?>"><?= e($flash['msg']) ?></div>
<?php endif; ?>
<?php if ($u && !coBangNhatKy()): ?>
    <div class="flash flash-error">
        Chưa có bảng <code>web_admin_log</code> nên mọi thao tác đang <strong>không được ghi nhật ký</strong>
        và trang đăng nhập chỉ chặn dò mật khẩu ở mức yếu. Chạy một lần:
        <code>mysql -u root kpah_db &lt; sql/them_bang_web_admin_log.sql</code>
    </div>
<?php endif; ?>
<?php
}

function adminFoot(): void
{
    if (adminUser()) {
        ?>
        </main>
        <footer class="admin-foot">
            Khu quản trị · mọi thao tác đều được ghi lại kèm tên người thực hiện và địa chỉ IP.
        </footer>
    </div><!-- /.admin-col -->
</div><!-- /.admin-shell -->
        <?php
    } else {
        ?>
    </main>
        <?php
    }
    ?>
</body>
</html>
<?php
}

/**
 * Thẻ số liệu cho trang tổng quan.
 *
 * Nhãn đứng TRƯỚC con số: đọc "Tài khoản" rồi mới tới "1.284" thì mắt không phải
 * quay ngược lên để biết con số vừa đọc là của cái gì.
 */
function theSo(string $nhan, string $giaTri, string $ghiChu = '', string $mau = ''): void
{
    ?>
    <div class="stat <?= e($mau) ?>">
        <div class="stat-nhan"><?= e($nhan) ?></div>
        <div class="stat-so"><?= e($giaTri) ?></div>
        <?php if ($ghiChu !== ''): ?><div class="stat-ghi"><?= e($ghiChu) ?></div><?php endif; ?>
    </div>
    <?php
}

/**
 * Biểu đồ cột đứng thuần CSS — không tải thư viện ngoài, để trang admin chạy
 * được cả khi máy chủ không ra được Internet.
 *
 * @param array $duLieu  [['nhan' => '25/08', 'gt' => 12], ...]
 */
function bieuDoCot(array $duLieu, string $donVi = ''): void
{
    $max = 0;
    foreach ($duLieu as $d) {
        $max = max($max, (int)$d['gt']);
    }
    if ($max <= 0) {
        echo '<p class="muted small">Chưa có số liệu trong khoảng thời gian này.</p>';
        return;
    }
    echo '<div class="chart">';
    foreach ($duLieu as $d) {
        $cao = max(2, (int)round((int)$d['gt'] * 100 / $max));
        printf(
            '<div class="chart-col" title="%s: %s%s"><div class="chart-bar" style="height:%d%%"></div>'
            . '<div class="chart-gt">%s</div><div class="chart-nhan">%s</div></div>',
            e($d['nhan']), e(so($d['gt'])), e($donVi === '' ? '' : ' ' . $donVi),
            $cao, e(so($d['gt'])), e($d['nhan'])
        );
    }
    echo '</div>';
}

/**
 * Thanh ngang có nhãn — dùng cho phân bố nghề / cấp độ, nơi nhãn dài hơn cột.
 *
 * @param array $duLieu [['nhan' => 'Kiếm khách', 'gt' => 120], ...]
 */
function thanhNgang(array $duLieu): void
{
    $max = 0;
    foreach ($duLieu as $d) {
        $max = max($max, (int)$d['gt']);
    }
    if ($max <= 0) {
        echo '<p class="muted small">Chưa có số liệu.</p>';
        return;
    }
    echo '<div class="bars">';
    foreach ($duLieu as $d) {
        $rong = max(1, (int)round((int)$d['gt'] * 100 / $max));
        printf(
            '<div class="bar-row"><span class="bar-nhan" title="%s">%s</span>'
            . '<span class="bar-track"><span class="bar-fill" style="width:%d%%"></span></span>'
            . '<span class="bar-gt">%s</span></div>',
            e($d['nhan']), e($d['nhan']), $rong, e(so($d['gt']))
        );
    }
    echo '</div>';
}
