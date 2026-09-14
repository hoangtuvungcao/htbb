<?php
/**
 * Ai đang online, đang ở map nào — đọc bảng `tob_vi_tri_online`.
 *
 * Bảng đó do server tự ghi (src/data/GhiViTriOnline.java): mỗi 30 giây XOÁ HẾT rồi
 * ghi lại toàn bộ người chơi đang online + map/toạ độ hiện tại. Vì vậy trang này
 * KHÔNG có khái niệm "trang sau" hay "log lịch sử" — nó luôn hiện đúng bức ảnh mới
 * nhất, chỉ có thể trễ tối đa 30 giây so với thực tế.
 *
 * Nếu bảng không tồn tại (server chưa từng khởi động lại từ khi có tính năng này) —
 * hiện thông báo hướng dẫn thay vì lỗi khó hiểu.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

const BANG_VI_TRI = 'tob_vi_tri_online';

$locTen = trim(g('locTen'));
$locMap = trim(g('locMap'));

$dong = [];
$loiDoc = '';
$bangChuaCo = false;
try {
    $dieuKien = [];
    $thamSo = [];
    if ($locTen !== '') {
        $dieuKien[] = 'charname LIKE ?';
        $thamSo[] = '%' . $locTen . '%';
    }
    if ($locMap !== '') {
        $dieuKien[] = 'map_ten LIKE ?';
        $thamSo[] = '%' . $locMap . '%';
    }
    $where = $dieuKien ? 'WHERE ' . implode(' AND ', $dieuKien) : '';
    $st = dbGame()->prepare(
        "SELECT charname, map_id, map_ten, khu, x, y, cap_nhat_luc FROM `" . BANG_VI_TRI . "` "
        . "$where ORDER BY map_ten, charname LIMIT 2000"
    );
    $st->execute($thamSo);
    $dong = $st->fetchAll();
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
    $bangChuaCo = stripos($loiDoc, "doesn't exist") !== false || stripos($loiDoc, 'exist') !== false;
}

// Gom theo map de hien so nguoi tren tung map ngay tren dau, khong phai doc tung dong.
$theoMap = [];
foreach ($dong as $r) {
    $ten = (string)$r['map_ten'] !== '' ? (string)$r['map_ten'] : ('(map id ' . $r['map_id'] . ')');
    $theoMap[$ten] = ($theoMap[$ten] ?? 0) + 1;
}
arsort($theoMap);

adminHead('online', 'Ai đang ở đâu');
?>
<div class="page-head">
    <div>
        <h1>Ai đang ở đâu</h1>
        <p>Vị trí (map + toạ độ) của mọi người chơi đang online — lấy mẫu lại mỗi 30 giây,
           có thể trễ tối đa 30 giây so với thực tế. Không phải log lịch sử, chỉ là ảnh chụp
           hiện tại.</p>
    </div>
    <a class="btn btn-xam btn-nho" href="online.php<?= $locTen !== '' || $locMap !== '' ? '?locTen=' . e(urlencode($locTen)) . '&locMap=' . e(urlencode($locMap)) : '' ?>">↻ Làm mới</a>
</div>

<?php if ($bangChuaCo): ?>
    <div class="flash flash-error">
        Chưa có bảng <code>tob_vi_tri_online</code> — server cần khởi động lại ít nhất một lần
        sau khi cập nhật bản có tính năng này (server tự tạo bảng lúc khởi động).
    </div>
<?php elseif ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<div class="card">
    <form method="get" action="online.php" class="ngang">
        <label class="o"><span>Tên nhân vật (gần đúng)</span>
            <input type="text" name="locTen" value="<?= e($locTen) ?>" placeholder="vd: hoang">
        </label>
        <label class="o"><span>Tên map (gần đúng)</span>
            <input type="text" name="locMap" value="<?= e($locMap) ?>" placeholder="vd: Trấn Danh">
        </label>
        <button class="btn btn-primary btn-nho">Lọc</button>
        <?php if ($locTen !== '' || $locMap !== ''): ?>
            <a class="btn btn-xam btn-nho" href="online.php">Bỏ lọc</a>
        <?php endif; ?>
    </form>
</div>

<div class="card">
    <h3>Tổng <?= count($dong) ?> người đang online<?= $locTen !== '' || $locMap !== '' ? ' (đã lọc)' : '' ?></h3>
    <?php if ($theoMap): ?>
        <p class="muted small">Theo map:
            <?php $phanTu = []; foreach ($theoMap as $ten => $sl) { $phanTu[] = e($ten) . ' (' . $sl . ')'; } ?>
            <?= implode(' · ', $phanTu) ?>
        </p>
    <?php endif; ?>
    <div class="tbl-wrap">
    <table class="tbl">
        <thead><tr>
            <th>Nhân vật</th><th>Map</th><th class="num">Khu</th>
            <th class="num">X</th><th class="num">Y</th><th>Cập nhật lúc</th>
        </tr></thead>
        <tbody>
        <?php foreach ($dong as $r): ?>
            <tr>
                <td class="nowrap"><a href="nhanvat.php?q=<?= e(urlencode((string)$r['charname'])) ?>"><?= e((string)$r['charname']) ?></a></td>
                <td class="nowrap"><?= e((string)$r['map_ten'] !== '' ? (string)$r['map_ten'] : '?') ?> (<?= e((string)$r['map_id']) ?>)</td>
                <td class="num"><?= e((string)$r['khu']) ?></td>
                <td class="num"><?= e((string)$r['x']) ?></td>
                <td class="num"><?= e((string)$r['y']) ?></td>
                <td class="nowrap"><?= e((string)$r['cap_nhat_luc']) ?></td>
            </tr>
        <?php endforeach; ?>
        <?php if (!$dong && $loiDoc === ''): ?>
            <tr><td colspan="6" class="muted">Không có ai khớp bộ lọc (hoặc không ai đang online).</td></tr>
        <?php endif; ?>
        </tbody>
    </table>
    </div>
</div>
<?php adminFoot(); ?>
