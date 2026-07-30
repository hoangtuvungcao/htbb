<?php
require_once __DIR__ . '/functions.php';
$u = currentUser();
$flash = takeFlash();
$page = $page ?? '';
?>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= e(SITE_NAME) ?></title>
    <link rel="stylesheet" href="assets/style.css">
    <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><text y='.9em' font-size='90'>⚔️</text></svg>">
</head>
<body>
<header class="site-header">
    <div class="wrap header-inner">
        <a class="brand" href="index.php">
            <span class="brand-mark">⚔️</span>
            <span class="brand-text">
                <strong>KPAH</strong>
                <small><?= e(SITE_TAGLINE) ?></small>
            </span>
        </a>
        <nav class="main-nav">
            <a href="index.php" class="<?= $page === 'home' ? 'active' : '' ?>">Trang chủ</a>
            <a href="index.php#download">Tải game</a>
            <a href="index.php#moc-nap">Mốc tân thủ</a>
            <a href="index.php#rank">Xếp hạng</a>
            <a href="index.php#news">Tin tức</a>
            <?php if ($u): ?>
                <a href="naptien.php" class="btn-nav <?= $page === 'naptien' ? 'active' : '' ?>">Nạp tiền</a>
                <a href="account.php" class="<?= $page === 'account' ? 'active' : '' ?>">Tài khoản</a>
                <a href="logout.php" class="btn-nav ghost">Đăng xuất</a>
            <?php else: ?>
                <a href="login.php" class="btn-nav ghost <?= $page === 'login' ? 'active' : '' ?>">Đăng nhập</a>
                <a href="register.php" class="btn-nav <?= $page === 'register' ? 'active' : '' ?>">Đăng ký</a>
            <?php endif; ?>
        </nav>
    </div>
</header>
<main class="wrap">
<?php if ($flash): ?>
    <div class="flash flash-<?= e($flash['type']) ?>"><?= e($flash['msg']) ?></div>
<?php endif; ?>
