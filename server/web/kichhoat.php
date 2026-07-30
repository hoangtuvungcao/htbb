<?php
require_once __DIR__ . '/inc/functions.php';
requireLogin();

$page = 'kichhoat';
$u = currentUser();

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'activate') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        setFlash('error', 'Phiên làm việc hết hạn, vui lòng thử lại.');
    } elseif (activateAccount((int)$u['id'], $u['username'])) {
        setFlash('success', 'Kích hoạt tài khoản thành công! Bạn có thể vào game ngay.');
    } else {
        setFlash('error', 'Kích hoạt thất bại. Vui lòng thử lại sau.');
    }
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
            <form method="post" action="kichhoat.php">
                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                <input type="hidden" name="action" value="activate">
                <button class="btn btn-primary btn-block" type="submit">Kích hoạt ngay</button>
            </form>
            <p class="muted small" style="text-align:center;margin-top:10px;">Kích hoạt xong là vào game được luôn, không cần chờ.</p>
        <?php endif; ?>
    </div>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
