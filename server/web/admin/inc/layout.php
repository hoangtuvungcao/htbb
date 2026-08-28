<?php
/**
 * Khung trang cho khu quản trị. Không dùng chung inc/header.php của trang công
 * khai vì header đó trỏ "assets/style.css" theo đường dẫn tương đối — nằm trong
 * thư mục con web/admin/ là hỏng — và vì menu ở đây hoàn toàn khác.
 */
require_once __DIR__ . '/boot.php';

function adminHead(string $trang, string $tieuDe): void
{
    $u = adminUser();
    $flash = takeFlash();
    $menu = [
        'tongquan' => ['index.php',     'Tổng quan'],
        'taikhoan' => ['taikhoan.php',  'Tài khoản'],
        'nhanvat'  => ['nhanvat.php',   'Nhân vật'],
        'giftcode' => ['giftcode.php',  'Giftcode'],
        'log'      => ['log.php',       'Log game'],
        'nhatky'   => ['nhatky.php',    'Nhật ký admin'],
    ];
    ?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title><?= e($tieuDe) ?> · Quản trị KPAH</title>
    <link rel="stylesheet" href="../assets/style.css">
    <link rel="stylesheet" href="assets/admin.css">
    <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>🛡️</text></svg>">
</head>
<body class="admin-body">
<header class="site-header">
    <div class="wrap header-inner">
        <a class="brand" href="index.php">
            <span class="brand-mark">🛡️</span>
            <span class="brand-text">
                <strong>QUẢN TRỊ</strong>
                <small><?= e(SITE_NAME) ?></small>
            </span>
        </a>
        <?php if ($u): ?>
        <nav class="main-nav">
            <?php foreach ($menu as $key => [$href, $nhan]): ?>
                <a href="<?= e($href) ?>" class="<?= $trang === $key ? 'active' : '' ?>"><?= e($nhan) ?></a>
            <?php endforeach; ?>
            <a href="../index.php" class="btn-nav ghost">Ra trang chủ</a>
            <a href="dangxuat.php" class="btn-nav">Thoát (<?= e($u['username']) ?>)</a>
        </nav>
        <?php endif; ?>
    </div>
</header>
<main class="wrap admin-main">
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
    ?>
</main>
<footer class="site-footer">
    <div class="wrap">
        <p class="muted small">Khu quản trị · mọi thao tác đều được ghi lại kèm tên người thực hiện và địa chỉ IP.</p>
    </div>
</footer>
</body>
</html>
<?php
}

/** Thẻ số liệu cho trang tổng quan. */
function theSo(string $nhan, string $giaTri, string $ghiChu = '', string $mau = ''): void
{
    ?>
    <div class="stat <?= e($mau) ?>">
        <div class="stat-so"><?= e($giaTri) ?></div>
        <div class="stat-nhan"><?= e($nhan) ?></div>
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
            '<div class="bar-row"><span class="bar-nhan">%s</span>'
            . '<span class="bar-track"><span class="bar-fill" style="width:%d%%"></span></span>'
            . '<span class="bar-gt">%s</span></div>',
            e($d['nhan']), $rong, e(so($d['gt']))
        );
    }
    echo '</div>';
}
