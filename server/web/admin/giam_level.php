<?php
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/lenh_nhan_vat.php';
require_once __DIR__ . '/inc/capdo.php';
requireAdmin();
$trang = 'giam_level.php';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($trang);
    if (p('viec') === 'huy') {
        $id = (int)p('id_lenh');
        $ok = huyLenh(dbGame(), $id, adminName());
        if ($ok) ghiNhatKy('huy_lenh_nhan_vat', '', 'lệnh #' . $id);
        veLai($trang, $ok ? 'success' : 'error', $ok ? 'Đã huỷ lệnh.' : 'Không huỷ được: lệnh đã xử lý.');
    }
    $db = dbGame();
    try {
        $soCap = filter_var(p('so_cap'), FILTER_VALIDATE_INT, ['options' => ['min_range' => 1, 'max_range' => CAPDO_TOI_DA]]);
        if ($soCap === false) throw new InvalidArgumentException('Số level phải là số nguyên từ 1 đến ' . CAPDO_TOI_DA . '.');
        $names = tachTenGiamLevel(p('ten_nhan_vat'));
        taoBangLenhNhanVat($db);
        $find = $db->prepare('SELECT id, charname FROM tob_char WHERE charname = ? AND del = 1 LIMIT 1');
        $players = [];
        $missing = [];
        foreach ($names as $name) {
            $find->execute([$name]);
            $player = $find->fetch();
            if (!$player) $missing[] = $name;
            else $players[(int)$player['id']] = $player;
        }
        if ($missing) throw new InvalidArgumentException('Chưa gửi lệnh. Không tìm thấy: ' . implode(', ', $missing));
        $db->beginTransaction();
        $insert = $db->prepare('INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, admin_user) VALUES (?, ?, ?, ?, ?)');
        foreach ($players as $player) {
            $insert->execute([LENH_LOAI_GIAM_CAP_DO, (int)$player['id'], $player['charname'], $soCap, adminName()]);
        }
        $db->commit();
        ghiNhatKy('giam_level_hang_loat', implode(', ', array_column($players, 'charname')), 'Gửi giảm ' . $soCap . ' level/nhân vật');
        veLai($trang, 'success', 'Đã gửi ' . count($players) . ' lệnh giảm ' . $soCap . ' level. Xem kết quả bên dưới; không gửi lại các lệnh đang chờ.');
    } catch (Throwable $ex) {
        if ($db->inTransaction()) $db->rollBack();
        veLai($trang, 'error', $ex->getMessage());
    }
}
$lenhs = [];
$loi = '';
try {
    if (coBangLenh(dbGame())) {
        $st = dbGame()->prepare('SELECT * FROM web_lenh_nhan_vat WHERE loai = ? ORDER BY id DESC LIMIT 100');
        $st->execute([LENH_LOAI_GIAM_CAP_DO]);
        $lenhs = $st->fetchAll();
    }
} catch (Throwable $ex) { $loi = $ex->getMessage(); }
adminHead('giam_level', 'Giảm Level');
?>
<div class="page-head"><div><h1>Giảm Level nhiều nhân vật</h1><p>Giảm cùng số level cho một hoặc nhiều nhân vật.</p></div></div>
<div class="card">
<form method="post">
    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
    <label class="o"><span>Tên nhân vật — mỗi dòng một tên, hoặc ngăn bằng dấu phẩy / chấm phẩy (tối đa 100)</span>
        <textarea name="ten_nhan_vat" rows="6" required placeholder="nhanvat1&#10;nhanvat2"></textarea></label>
    <label class="o"><span>Số level cần giảm cho mỗi nhân vật</span>
        <input type="number" name="so_cap" min="1" max="<?= CAPDO_TOI_DA ?>" value="1" required></label>
    <p>Cấp thấp nhất là 1, EXP về 0% cấp mới. Không đủ cấp sẽ báo lỗi riêng cho nhân vật đó. Tên trùng chỉ xử lý một lần.</p>
    <p>Giữ điểm đã nhận; luyện lại cấp cũ không cộng trùng điểm. Nhân vật offline sẽ được giảm khi vào game.</p>
    <button class="btn btn-do">Gửi lệnh giảm Level</button>
</form>
</div>
<h2>100 lệnh giảm Level gần nhất</h2>
<p><a class="btn btn-primary btn-nho" href="giam_level.php">Cập nhật kết quả</a></p>
<?php if ($loi !== ''): ?><p class="flash flash-error"><?= e($loi) ?></p><?php endif; ?>
<div class="table-wrap"><table><thead><tr><th>Lệnh</th><th>Nhân vật</th><th>Giảm</th><th>Trạng thái</th><th>Kết quả</th><th>Thao tác</th></tr></thead><tbody>
<?php foreach ($lenhs as $l): ?>
<tr><td>#<?= (int)$l['id'] ?></td><td><?= e($l['charname']) ?></td><td><?= (int)$l['cap_do'] ?> level</td>
<td><?= e(tenTrangThaiLenh((int)$l['trang_thai'])) ?></td><td><?= e($l['ket_qua']) ?></td><td>
<?php if (in_array((int)$l['trang_thai'], [LENH_CHO, LENH_CHO_DANG_NHAP], true)): ?>
<form method="post"><input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>"><input type="hidden" name="viec" value="huy"><input type="hidden" name="id_lenh" value="<?= (int)$l['id'] ?>"><button class="btn btn-nho">Huỷ lệnh</button></form>
<?php endif; ?></td></tr>
<?php endforeach; ?>
<?php if (!$lenhs): ?><tr><td colspan="6">Chưa có lệnh giảm level.</td></tr><?php endif; ?>
</tbody></table></div>
<?php adminFoot(); ?>