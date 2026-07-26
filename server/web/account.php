<?php
require_once __DIR__ . '/inc/functions.php';
requireLogin();

$page = 'account';
$u = currentUser();

// Xử lý kích hoạt
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'activate') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        setFlash('error', 'Phiên làm việc hết hạn, vui lòng thử lại.');
    } elseif (activateAccount((int)$u['id'], $u['username'])) {
        setFlash('success', 'Kích hoạt tài khoản thành công! Bạn có thể vào game ngay.');
    } else {
        setFlash('error', 'Kích hoạt thất bại. Vui lòng thử lại sau.');
    }
    header('Location: account.php');
    exit;
}

$activated = isActivated($u['username']);

// Danh sách nhân vật của tài khoản (phòng thủ nếu lỗi)
$chars = [];
try {
    $st = dbGame()->prepare('SELECT charname, lastLv, xp, class FROM tob_char WHERE userId = ? ORDER BY xp DESC');
    $st->execute([(int)$u['id']]);
    $chars = $st->fetchAll();
} catch (Throwable $ex) {
    $chars = [];
}
$classNames = ['Đại phu', 'Đao khách', 'Kiếm khách', 'Cung thủ', 'Đấu sĩ'];

require __DIR__ . '/inc/header.php';
?>
<section class="panel">
    <h1 class="section-title">Tài khoản của tôi</h1>
    <div class="account-grid">
        <div class="card">
            <h2>Thông tin</h2>
            <p><span class="muted">Tên tài khoản:</span> <strong><?= e($u['username']) ?></strong></p>
            <p><span class="muted">Trạng thái:</span>
                <?php if ($activated): ?>
                    <span class="badge badge-ok">Đã kích hoạt</span>
                <?php else: ?>
                    <span class="badge badge-warn">Chưa kích hoạt</span>
                <?php endif; ?>
            </p>
            <?php if (!$activated): ?>
                <form method="post" action="account.php">
                    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                    <input type="hidden" name="action" value="activate">
                    <button class="btn btn-primary" type="submit">Kích hoạt tài khoản</button>
                </form>
                <p class="muted small">Kích hoạt để có thể đăng nhập vào game.</p>
            <?php endif; ?>
        </div>
        <div class="card">
            <h2>Nhân vật</h2>
            <?php if ($chars): ?>
                <table class="rank-table">
                    <thead><tr><th>Nhân vật</th><th>Lớp</th><th>Cấp</th></tr></thead>
                    <tbody>
                    <?php foreach ($chars as $c): ?>
                        <tr>
                            <td><?= e($c['charname']) ?></td>
                            <td><?= e($classNames[(int)$c['class']] ?? '—') ?></td>
                            <td><?= (int)$c['lastLv'] ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            <?php else: ?>
                <p class="muted">Chưa có nhân vật. Hãy vào game và tạo nhân vật đầu tiên của bạn!</p>
            <?php endif; ?>
        </div>
    </div>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
