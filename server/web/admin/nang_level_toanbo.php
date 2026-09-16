<?php
/**
 * NANG LEVEL TOAN BO: cong THEM cung mot so level cho MOI nhan vat con hoat dong (del = 1)
 * trong server - dung cho su kien / bu dap toan server, khac han nang_level cua tung nhan
 * vat rieng le o nhanvat.php (dat ve MOT cap tuyet doi).
 *
 * CUNG CO CHE HANG DOI voi Giam Level / Reset toan bo (xem inc/lenh_nhan_vat.php): KHONG
 * UPDATE thang tob_char - nhan vat dang online giu ban RAM rieng, lan tu luu ke tiep se ghi
 * de; luc dang nhap server doc cap/exp tu cot pInfo chu khong doc lastLv/xp. Voi so luong
 * nhan vat co the len toi hang nghin, KHONG doi ket qua tung dong (nhu ham cho o inc/lenh_nhan_vat.php)
 * ma CHEN THANG toan bo lenh vao web_lenh_nhan_vat trong MOT transaction roi tra loi ngay; luong nen LenhWeb
 * (server game, doc bang moi giay, toi da 50 dong/vong) tu xu ly dan o phia sau. Trang nay
 * cho xem lai tien do (con bao nhieu lenh chua xong) bang cach tai lai.
 *
 * MOI NHAN VAT CONG THEM SO LEVEL CO DINH tu cap HIEN TAI cua rieng no, khong dat ve mot cap
 * tuyet doi chung - xem data.LenhWeb.xuLyTangCapDo/capSauKhiTang. Nhan vat da o cap toi da
 * (CAPDO_TOI_DA) se nhan loi rieng cho dong do, khong lam hong cac nhan vat khac.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/lenh_nhan_vat.php';
require_once __DIR__ . '/inc/capdo.php';
requireAdmin();

$TRANG = 'nang_level_toanbo.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    if (p('viec') === 'huy') {
        $id = (int)p('id_lenh');
        $ok = huyLenh(dbGame(), $id, adminName());
        if ($ok) ghiNhatKy('huy_lenh_nang_level_toanbo', '', 'lệnh #' . $id);
        veLai($TRANG, $ok ? 'success' : 'error', $ok ? 'Đã huỷ lệnh.' : 'Không huỷ được: lệnh đã xử lý.');
    }

    $db = dbGame();
    try {
        $soCap = filter_var(p('so_cap'), FILTER_VALIDATE_INT, ['options' => ['min_range' => 1, 'max_range' => CAPDO_TOI_DA]]);
        if ($soCap === false) throw new InvalidArgumentException('Số level phải là số nguyên từ 1 đến ' . CAPDO_TOI_DA . '.');

        taoBangLenhNhanVat($db);
        $dsNV = $db->query('SELECT id, charname FROM tob_char WHERE del = 1')->fetchAll();

        if (!$dsNV) {
            veLai($TRANG, 'success', 'Không có nhân vật nào đang hoạt động — không có gì để nâng level.');
        }

        $db->beginTransaction();
        $insert = $db->prepare('INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, admin_user) VALUES (?, ?, ?, ?, ?)');
        foreach ($dsNV as $nv) {
            $insert->execute([LENH_LOAI_TANG_CAP_DO, (int)$nv['id'], substr((string)$nv['charname'], 0, 50), $soCap, substr(adminName(), 0, 50)]);
        }
        $db->commit();

        ghiNhatKy('nang_level_toanbo', '', 'đã xếp hàng đợi tăng ' . $soCap . ' level cho ' . count($dsNV) . ' nhân vật');
        veLai($TRANG, 'success', 'Đã xếp hàng đợi tăng ' . $soCap . ' level cho ' . count($dsNV)
            . ' nhân vật. Nhân vật đang online sẽ được cộng ngay; offline sẽ được cộng khi vào game. '
            . 'Server game sẽ tự xử lý dần ở nền — tải lại trang này để xem tiến độ.');
    } catch (Throwable $ex) {
        if ($db->inTransaction()) $db->rollBack();
        veLai($TRANG, 'error', $ex->getMessage());
    }
}

// ---------------------------------------------------------------------------
//  So lieu hien thi
// ---------------------------------------------------------------------------
$loiDoc = '';
$soNhanVat = 0;
$soLenhChoXuLy = 0;
$lenhs = [];
try {
    $soNhanVat = (int)dbGame()->query('SELECT COUNT(*) FROM tob_char WHERE del = 1')->fetchColumn();

    if (coBangLenh(dbGame())) {
        $st = dbGame()->prepare('SELECT COUNT(*) FROM web_lenh_nhan_vat WHERE loai = ? AND trang_thai IN (?, ?)');
        $st->execute([LENH_LOAI_TANG_CAP_DO, LENH_CHO, LENH_CHO_DANG_NHAP]);
        $soLenhChoXuLy = (int)$st->fetchColumn();

        $st = dbGame()->prepare('SELECT * FROM web_lenh_nhan_vat WHERE loai = ? ORDER BY id DESC LIMIT 100');
        $st->execute([LENH_LOAI_TANG_CAP_DO]);
        $lenhs = $st->fetchAll();
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

adminHead('nang_level_toanbo', 'Nâng Level toàn server');
?>
<div class="page-head">
    <div>
        <h1>Nâng Level toàn bộ nhân vật</h1>
        <p>Cộng thêm cùng một số level cho MỌI nhân vật đang hoạt động trên server — dùng cho sự kiện / bù đắp toàn server.</p>
    </div>
</div>

<?php if ($loiDoc !== ''): ?><div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div><?php endif; ?>

<div class="card">
    <h3>Tình trạng hiện tại</h3>
    <dl class="dl">
        <dt>Nhân vật sẽ được nâng level</dt><dd><?= so($soNhanVat) ?></dd>
        <dt>Lệnh đang chờ server xử lý</dt><dd><?= so($soLenhChoXuLy) ?></dd>
    </dl>
    <p><a class="btn btn-xam btn-nho" href="<?= e($TRANG) ?>">Cập nhật số liệu</a></p>
</div>

<div class="card">
    <h3>Nâng level cho tất cả</h3>
    <form method="post" action="<?= e($TRANG) ?>"
          onsubmit="return confirm('Nâng level cho toàn bộ <?= (int)$soNhanVat ?> nhân vật đang hoạt động?');">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <label class="o"><span>Số level cộng thêm cho mỗi nhân vật</span>
            <input type="number" name="so_cap" min="1" max="<?= CAPDO_TOI_DA ?>" value="1" required></label>
        <p>Nhân vật đang online được cộng NGAY (kèm điểm cơ bản/kỹ năng của các cấp vừa lên, giống lên cấp tự nhiên);
           nhân vật offline sẽ được cộng khi vào game. Nhân vật đã đạt cấp tối đa (<?= CAPDO_TOI_DA ?>) sẽ báo lỗi
           riêng cho nhân vật đó, không ảnh hưởng đến các nhân vật khác.</p>
        <button class="btn btn-do">Nâng Level cho tất cả</button>
    </form>
</div>

<h2>100 lệnh nâng level toàn bộ gần nhất</h2>
<p><a class="btn btn-primary btn-nho" href="<?= e($TRANG) ?>">Cập nhật kết quả</a></p>
<div class="table-wrap"><table><thead><tr><th>Lệnh</th><th>Nhân vật</th><th>Tăng</th><th>Trạng thái</th><th>Kết quả</th><th>Thao tác</th></tr></thead><tbody>
<?php foreach ($lenhs as $l): ?>
<tr><td>#<?= (int)$l['id'] ?></td><td><?= e($l['charname']) ?></td><td><?= (int)$l['cap_do'] ?> level</td>
<td><?= e(tenTrangThaiLenh((int)$l['trang_thai'])) ?></td><td><?= e($l['ket_qua']) ?></td><td>
<?php if (in_array((int)$l['trang_thai'], [LENH_CHO, LENH_CHO_DANG_NHAP], true)): ?>
<form method="post"><input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>"><input type="hidden" name="viec" value="huy"><input type="hidden" name="id_lenh" value="<?= (int)$l['id'] ?>"><button class="btn btn-nho">Huỷ lệnh</button></form>
<?php endif; ?></td></tr>
<?php endforeach; ?>
<?php if (!$lenhs): ?><tr><td colspan="6">Chưa có lệnh nâng level toàn bộ.</td></tr><?php endif; ?>
</tbody></table></div>
<?php adminFoot(); ?>
