<?php
require_once __DIR__ . '/inc/functions.php';
requireLogin();

$page = 'naptien';
$u = currentUser();
$errors = [];
$don = null;

// Đang xem 1 đơn cụ thể (vừa tạo, hoặc quay lại bằng link cũ)?
if (isset($_GET['ma_don'])) {
    $don = layDonNapMoc((string)$_GET['ma_don']);
    if (!$don || (int)$don['userID'] !== (int)$u['id']) {
        $don = null;
        $errors[] = 'Không tìm thấy đơn nạp này.';
    }
}

// Tạo đơn mới
if (!$don && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'tao_don') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        $errors[] = 'Phiên làm việc hết hạn, vui lòng thử lại.';
    } else {
        $moc = (int)($_POST['moc'] ?? 0);
        if (!in_array($moc, [1, 2, 3], true)) {
            $errors[] = 'Mốc nạp không hợp lệ.';
        } else {
            $tao = taoDonNapMoc((int)$u['id'], $moc);
            header('Location: naptien.php?ma_don=' . urlencode($tao['ma_don']));
            exit;
        }
    }
}

$mocList = mocNapList();
require __DIR__ . '/inc/header.php';
?>
<section class="section">
    <h1 class="section-title">Nạp tiền theo mốc</h1>

    <?php foreach ($errors as $err): ?>
        <div class="flash flash-error"><?= e($err) ?></div>
    <?php endforeach; ?>

    <?php if ($don): ?>
        <?php
        $qr = vietQrUrl((int)$don['amount'], $don['ma_don']);
        $daXong = $don['trang_thai'] !== 'cho_thanh_toan';
        ?>
        <div class="qr-wrap">
            <div class="card qr-card">
                <h2>Mốc <?= (int)$don['moc'] ?> &middot; <?= number_format((int)$don['amount']) ?>đ</h2>

                <div id="qr-pending" style="<?= $daXong ? 'display:none' : '' ?>">
                    <img src="<?= e($qr) ?>" alt="Mã QR chuyển khoản TPBank" class="qr-img">
                    <p class="muted small">Quét mã bằng app TPBank (hoặc app ngân hàng bất kỳ hỗ trợ VietQR).</p>
                    <table class="qr-info">
                        <tr><td class="muted">Số tiền</td><td><strong><?= number_format((int)$don['amount']) ?>đ</strong></td></tr>
                        <tr><td class="muted">Nội dung CK</td><td><strong class="ma-don"><?= e($don['ma_don']) ?></strong></td></tr>
                        <tr><td class="muted">Ngân hàng</td><td>TPBank</td></tr>
                    </table>
                    <p class="warn-note">⚠️ Chuyển khoản đúng số tiền và giữ nguyên nội dung <strong><?= e($don['ma_don']) ?></strong> để hệ thống tự nhận ra đơn của bạn.</p>
                    <p class="muted small" id="qr-status">Đang chờ thanh toán... trang sẽ tự cập nhật.</p>
                </div>

                <div id="qr-done" style="<?= $daXong ? '' : 'display:none' ?>">
                    <p class="flash flash-success">✅ Đã nhận được thanh toán! Vào game, gặp <strong>NPC Tổng Quản</strong>, chọn <strong>"Nhận mốc nạp"</strong> để nhận quà.</p>
                </div>

                <a href="naptien.php" class="btn btn-outline" style="margin-top:14px;display:inline-block;">← Chọn mốc khác</a>
            </div>
        </div>
        <?php if (!$daXong): ?>
        <script>
        (function () {
            var maDon = <?= json_encode($don['ma_don']) ?>;
            function check() {
                fetch('order_status.php?ma_don=' + encodeURIComponent(maDon))
                    .then(function (r) { return r.json(); })
                    .then(function (data) {
                        if (data && data.trang_thai && data.trang_thai !== 'cho_thanh_toan') {
                            document.getElementById('qr-pending').style.display = 'none';
                            document.getElementById('qr-done').style.display = '';
                        } else {
                            setTimeout(check, 4000);
                        }
                    })
                    .catch(function () { setTimeout(check, 6000); });
            }
            setTimeout(check, 4000);
        })();
        </script>
        <?php endif; ?>
    <?php else: ?>
        <p class="muted" style="text-align:center;margin-bottom:26px;">
            Chọn 1 trong 3 mốc tân thủ bên dưới, quét mã QR để chuyển khoản qua TPBank.
            Sau khi thanh toán thành công, vào game gặp <strong>NPC Tổng Quản</strong> để nhận quà.
        </p>
        <div class="moc-grid">
            <?php foreach ($mocList as $moc => $info): ?>
                <div class="card moc-card">
                    <h3 class="moc-title">Mốc <?= $moc ?></h3>
                    <div class="moc-gia"><?= number_format($info['gia']) ?>đ</div>
                    <ul class="moc-items">
                        <?php foreach ($info['items'] as $it): ?>
                            <li><?= e($it) ?></li>
                        <?php endforeach; ?>
                    </ul>
                    <form method="post" action="naptien.php">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="action" value="tao_don">
                        <input type="hidden" name="moc" value="<?= $moc ?>">
                        <button type="submit" class="btn btn-primary btn-block">Nạp mốc <?= $moc ?></button>
                    </form>
                </div>
            <?php endforeach; ?>
        </div>
    <?php endif; ?>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
