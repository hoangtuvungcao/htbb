<?php
/**
 * Nhật ký thao tác của chính các admin (bảng kpah_db.web_admin_log).
 *
 * Đây là thứ trả lời câu “ai đã khoá tài khoản này?”, “mã quà kia ở đâu ra?”.
 * Trang này CỐ Ý không có nút xoá: một cuốn nhật ký xoá được thì không còn là
 * bằng chứng. Muốn dọn dòng cũ thì chạy tay câu DELETE ghi trong
 * sql/them_bang_web_admin_log.sql.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$ai    = g('ai');
$viec  = g('viec');
$doi   = g('doi');
$trang = max(1, (int)g('trang', '1'));
$moi   = 50;

$dong = [];
$conTrangSau = false;
$dsViec = [];
$loiDoc = '';

if (!coBangNhatKy()) {
    adminHead('nhatky', 'Nhật ký admin');
    ?>
    <div class="page-head"><div><h1>Nhật ký admin</h1></div></div>
    <div class="card">
        <h3>Chưa có bảng nhật ký</h3>
        <p>Chạy một lần trên máy chủ, vào database <code><?= e(DB_GAME) ?></code>:</p>
        <p><code>mysql -u root <?= e(DB_GAME) ?> &lt; sql/them_bang_web_admin_log.sql</code></p>
        <p class="muted small">Cho tới lúc đó, mọi thao tác trên trang quản trị vẫn chạy bình thường
           nhưng không để lại dấu vết, và việc chặn dò mật khẩu chỉ đếm trong session — xoá cookie
           là hết, gần như vô hiệu.</p>
    </div>
    <?php
    adminFoot();
    exit;
}

try {
    $dsViec = dbGame()->query('SELECT DISTINCT hanh_dong FROM web_admin_log ORDER BY hanh_dong')
                      ->fetchAll(PDO::FETCH_COLUMN);

    $dieuKien = [];
    $thamSo   = [];
    if ($ai   !== '') { $dieuKien[] = 'admin_user = ?'; $thamSo[] = $ai; }
    if ($viec !== '') { $dieuKien[] = 'hanh_dong = ?';  $thamSo[] = $viec; }
    if ($doi  !== '') { $dieuKien[] = 'doi_tuong = ?';  $thamSo[] = $doi; }
    $where = $dieuKien ? 'WHERE ' . implode(' AND ', $dieuKien) : '';

    // Ghép LIMIT/OFFSET bằng số nguyên đã ép kiểu — PDO tắt giả lập prepare nên
    // truyền qua dấu ? sẽ thành chuỗi và MySQL báo lỗi cú pháp.
    $lay = $moi + 1;
    $bo  = ($trang - 1) * $moi;

    $st = dbGame()->prepare(
        "SELECT id, admin_user, hanh_dong, doi_tuong, chi_tiet, ip, thoi_gian
           FROM web_admin_log $where ORDER BY id DESC LIMIT $lay OFFSET $bo"
    );
    $st->execute($thamSo);
    $dong = $st->fetchAll();

    if (count($dong) > $moi) {
        $conTrangSau = true;
        array_pop($dong);
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

/** Việc nào đáng chú ý thì tô màu. */
function mauViec(string $hanhDong): string
{
    if ($hanhDong === 'dang_nhap_that_bai') return 'nhan-khoa';
    if (strpos($hanhDong, 'khoa_') === 0 || strpos($hanhDong, 'xoa_') === 0) return 'nhan-khoa';
    if (strpos($hanhDong, 'mo_') === 0 || strpos($hanhDong, 'tao_') === 0) return 'nhan-ok';
    return 'nhan-mo';
}

adminHead('nhatky', 'Nhật ký admin');
?>
<div class="page-head">
    <div>
        <h1>Nhật ký admin</h1>
        <p>Mọi thao tác trên trang quản trị, kèm người làm và địa chỉ IP. Không xoá được từ giao diện.</p>
    </div>
    <form method="get" action="nhatky.php" class="ngang">
        <label class="o"><span>Người làm</span>
            <input type="text" name="ai" value="<?= e($ai) ?>" placeholder="tất cả">
        </label>
        <label class="o"><span>Việc</span>
            <select name="viec">
                <option value="">Tất cả</option>
                <?php foreach ($dsViec as $v): ?>
                    <option value="<?= e($v) ?>" <?= $v === $viec ? 'selected' : '' ?>><?= e($v) ?></option>
                <?php endforeach; ?>
            </select>
        </label>
        <label class="o"><span>Đối tượng</span>
            <input type="text" name="doi" value="<?= e($doi) ?>" placeholder="tên tài khoản / mã">
        </label>
        <button class="btn btn-primary btn-nho">Lọc</button>
        <?php if ($ai !== '' || $viec !== '' || $doi !== ''): ?>
            <a class="btn btn-xam btn-nho" href="nhatky.php">Bỏ lọc</a>
        <?php endif; ?>
    </form>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<div class="card">
    <div class="tbl-wrap">
    <table class="tbl">
        <thead><tr>
            <th class="num">ID</th><th>Lúc</th><th>Người làm</th>
            <th>Việc</th><th>Đối tượng</th><th>Chi tiết</th><th>IP</th>
        </tr></thead>
        <tbody>
        <?php foreach ($dong as $r): ?>
            <tr>
                <td class="num"><?= (int)$r['id'] ?></td>
                <td class="nowrap muted small"><?= e((string)$r['thoi_gian']) ?></td>
                <td class="nowrap"><?= e((string)$r['admin_user']) ?: '<span class="muted">—</span>' ?></td>
                <td class="nowrap"><span class="nhan <?= e(mauViec((string)$r['hanh_dong'])) ?>"><?= e((string)$r['hanh_dong']) ?></span></td>
                <td class="nowrap"><?= e((string)$r['doi_tuong']) ?></td>
                <td class="info"><?= e((string)$r['chi_tiet']) ?></td>
                <td class="nowrap muted small"><?= e((string)$r['ip']) ?></td>
            </tr>
        <?php endforeach; ?>
        <?php if (!$dong): ?>
            <tr><td colspan="7" class="muted">Chưa có dòng nào khớp.</td></tr>
        <?php endif; ?>
        </tbody>
    </table>
    </div>

    <div class="phantrang">
        <?php $qs = 'ai=' . urlencode($ai) . '&viec=' . urlencode($viec) . '&doi=' . urlencode($doi); ?>
        <?php if ($trang > 1): ?>
            <a class="btn btn-xam btn-nho" href="nhatky.php?<?= e($qs) ?>&amp;trang=<?= $trang - 1 ?>">← Trang trước</a>
        <?php endif; ?>
        <span>Trang <?= $trang ?> · <?= count($dong) ?> dòng</span>
        <?php if ($conTrangSau): ?>
            <a class="btn btn-xam btn-nho" href="nhatky.php?<?= e($qs) ?>&amp;trang=<?= $trang + 1 ?>">Trang sau →</a>
        <?php endif; ?>
    </div>
</div>

<div class="luuy">
    <p>Lọc theo việc <code>dang_nhap_that_bai</code> để xem có ai đang dò mật khẩu trang này không.
       Cùng một IP gõ sai <?= (int)ADMIN_MAX_FAIL ?> lần trong <?= (int)(ADMIN_FAIL_WINDOW / 60) ?> phút
       sẽ bị chặn tạm.</p>
    <p>Dòng ghi “đúng mật khẩu nhưng không có quyền admin” là dấu hiệu <strong>nặng</strong>:
       có người biết mật khẩu thật của một tài khoản nào đó và đang thử vào khu quản trị.</p>
</div>
<?php adminFoot(); ?>
