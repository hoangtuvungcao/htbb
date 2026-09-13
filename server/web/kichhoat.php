<?php
require_once __DIR__ . '/inc/functions.php';
requireLogin();

$page = 'kichhoat';
$u = currentUser();

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'activate') {
    setFlash('error', 'Kích hoạt tài khoản do quản trị viên thực hiện.');
    header('Location: kichhoat.php');
    exit;
}

$activated = isActivated($u['username']);

require __DIR__ . '/inc/header.php';
?>
<section class="auth-wrap">
    <div class="auth-card">
        <h1 class="auth-title">Kích hoạt tài khoản</h1>
        <p class="auth-sub">Tài khoản: <strong><?= e($u['username']) ?></strong></p>

        <?php if ($activated): ?>
            <p style="text-align:center;">
                <span class="badge badge-ok">Đã kích hoạt</span>
            </p>
            <p class="muted" style="text-align:center;">Tài khoản của bạn đã sẵn sàng để đăng nhập vào game.</p>
        <?php else: ?>
            <p style="text-align:center;">
                <span class="badge badge-warn">Chưa kích hoạt</span>
            </p>
            <p class="muted small" style="text-align:center;margin-top:10px;">Vui lòng liên hệ quản trị viên để kích hoạt tài khoản trong 30 ngày.</p>
        <?php endif; ?>
    </div>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
