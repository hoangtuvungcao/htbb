<?php
/**
 * Trang xác nhận nạp tiền THỦ CÔNG cho admin - dùng khi chưa cấu hình SePay
 * (hoặc muốn kiểm tra tay). Bảo vệ bằng ADMIN_NAPTIEN_PASSWORD trong config.php.
 */
require_once __DIR__ . '/inc/functions.php';

$page = 'admin_naptien';
$errors = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'login') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        $errors[] = 'Phiên làm việc hết hạn, vui lòng thử lại.';
    } elseif (!hash_equals(ADMIN_NAPTIEN_PASSWORD, (string)($_POST['password'] ?? ''))) {
        $errors[] = 'Sai mật khẩu.';
    } else {
        $_SESSION['admin_naptien_ok'] = true;
    }
}

if (($_GET['logout'] ?? '') === '1') {
    unset($_SESSION['admin_naptien_ok']);
    header('Location: admin_naptien.php');
    exit;
}

$daDangNhap = !empty($_SESSION['admin_naptien_ok']);

if ($daDangNhap && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'xac_nhan') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        $errors[] = 'Phiên làm việc hết hạn, vui lòng thử lại.';
    } else {
        $id = (int)($_POST['id'] ?? 0);
        $st = dbGame()->prepare("UPDATE tob_nap_moc SET trang_thai='da_thanh_toan', paid_at=NOW() WHERE id=? AND trang_thai='cho_thanh_toan'");
        $st->execute([$id]);
        setFlash($st->rowCount() > 0 ? 'success' : 'error', $st->rowCount() > 0 ? 'Đã xác nhận đơn #' . $id : 'Không tìm thấy đơn chờ thanh toán #' . $id);
        header('Location: admin_naptien.php');
        exit;
    }
}

$donCho = [];
$usernames = [];
if ($daDangNhap) {
    $donCho = dbGame()->query("SELECT * FROM tob_nap_moc WHERE trang_thai='cho_thanh_toan' ORDER BY created_at DESC")->fetchAll();
    $ids = array_unique(array_column($donCho, 'userID'));
    if ($ids) {
        $ph = implode(',', array_fill(0, count($ids), '?'));
        $st = dbAccount()->prepare("SELECT id, username FROM team_user WHERE id IN ($ph)");
        $st->execute(array_values($ids));
        foreach ($st->fetchAll() as $r) {
            $usernames[(int)$r['id']] = $r['username'];
        }
    }
}

require __DIR__ . '/inc/header.php';
?>
<section class="section">
    <h1 class="section-title">Xác nhận nạp tiền (Admin)</h1>

    <?php foreach ($errors as $err): ?>
        <div class="flash flash-error"><?= e($err) ?></div>
    <?php endforeach; ?>

    <?php if (!$daDangNhap): ?>
        <div class="auth-wrap">
            <div class="auth-card">
                <form method="post" action="admin_naptien.php">
                    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                    <input type="hidden" name="action" value="login">
                    <label>Mật khẩu admin
                        <input type="password" name="password" required autofocus>
                    </label>
                    <button type="submit" class="btn btn-primary btn-block">Vào trang xác nhận</button>
                </form>
            </div>
        </div>
    <?php else: ?>
        <p class="muted"><a href="admin_naptien.php?logout=1">Đăng xuất trang admin</a></p>
        <?php if (!$donCho): ?>
            <p class="muted">Không có đơn nào đang chờ thanh toán.</p>
        <?php else: ?>
            <div class="card">
                <table class="rank-table">
                    <thead>
                        <tr><th>#</th><th>Tài khoản</th><th>Mốc</th><th>Số tiền</th><th>Mã đơn</th><th>Thời gian</th><th></th></tr>
                    </thead>
                    <tbody>
                    <?php foreach ($donCho as $d): ?>
                        <tr>
                            <td><?= (int)$d['id'] ?></td>
                            <td><?= e($usernames[(int)$d['userID']] ?? ('userID ' . $d['userID'])) ?></td>
                            <td><?= (int)$d['moc'] ?></td>
                            <td><?= number_format((int)$d['amount']) ?>đ</td>
                            <td><strong><?= e($d['ma_don']) ?></strong></td>
                            <td><?= e($d['created_at']) ?></td>
                            <td>
                                <form method="post" action="admin_naptien.php" style="margin:0;">
                                    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                                    <input type="hidden" name="action" value="xac_nhan">
                                    <input type="hidden" name="id" value="<?= (int)$d['id'] ?>">
                                    <button type="submit" class="btn btn-primary" style="padding:6px 14px;font-size:.85rem;">Xác nhận đã nhận tiền</button>
                                </form>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        <?php endif; ?>
    <?php endif; ?>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
