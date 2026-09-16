<?php
/**
 * NANG LEVEL TOAN BO: dua MOI nhan vat con hoat dong (del = 1) len DUNG mot cap do dich (0%),
 * dung cho su kien / bu dap toan server. Nhan vat da o cap dich tro len duoc GIU NGUYEN (khong
 * ha cap) - server tu bao loi rieng cho dong do, khong lam hong nhan vat khac.
 *
 * DUNG LAI y het loai lenh dat_cap_do da co san (nut "Đặt cấp độ" cua tung nhan vat rieng le o
 * nhanvat.php, xu ly boi data.LenhWeb.xuLyDatCapDo/apDungOnline) thay vi tao loai lenh moi:
 * dat_cap_do von da CHI NANG cap (tu choi neu exp dich khong cao hon exp hien tai), dung khop
 * yeu cau "dua len DUNG mot cap" ma khong can biet truoc cap that cua tung nhan vat (dac biet
 * nhan vat dang online chi song trong RAM, web khong doc duoc).
 *
 * PHAN BIET VOI cac lenh dat_cap_do GUI TU nhanvat.php (tung nhan vat mot): cot `tham_so`
 * (khong dung cho dat_cap_do o noi khac) duoc danh dau 'toanbo' de bang "lenh gan day" o duoi
 * chi liet ke dung cac lenh hang loat cua TRANG NAY, khong lan voi lenh sua tay tung nhan vat.
 *
 * CUNG CO CHE HANG DOI voi Giam Level / Reset toan bo (xem inc/lenh_nhan_vat.php): KHONG
 * UPDATE thang tob_char - nhan vat dang online giu ban RAM rieng, lan tu luu ke tiep se ghi
 * de; luc dang nhap server doc cap/exp tu cot pInfo chu khong doc lastLv/xp. Voi so luong
 * nhan vat co the len toi hang nghin, KHONG doi ket qua tung dong (nhu ham cho o inc/lenh_nhan_vat.php)
 * ma CHEN THANG toan bo lenh vao web_lenh_nhan_vat trong MOT transaction roi tra loi ngay; luong nen LenhWeb
 * (server game, doc bang moi giay, toi da 50 dong/vong) tu xu ly dan o phia sau. Trang nay
 * cho xem lai tien do (con bao nhieu lenh chua xong) bang cach tai lai.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/lenh_nhan_vat.php';
require_once __DIR__ . '/inc/capdo.php';
requireAdmin();

$TRANG = 'nang_level_toanbo.php';
const TANG_LEVEL_TOANBO_DANH_DAU = 'toanbo';

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
        $capDich = filter_var(p('cap_do_dich'), FILTER_VALIDATE_INT, ['options' => ['min_range' => 1, 'max_range' => CAPDO_TOI_DA]]);
        if ($capDich === false) throw new InvalidArgumentException('Cấp độ đích phải là số nguyên từ 1 đến ' . CAPDO_TOI_DA . '.');

        taoBangLenhNhanVat($db);
        $dsNV = $db->query('SELECT id, charname FROM tob_char WHERE del = 1')->fetchAll();

        if (!$dsNV) {
            veLai($TRANG, 'success', 'Không có nhân vật nào đang hoạt động — không có gì để nâng level.');
        }

        $db->beginTransaction();
        $insert = $db->prepare(
            'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, phan_nghin, tham_so, admin_user)
             VALUES (?, ?, ?, ?, 0, ?, ?)'
        );
        foreach ($dsNV as $nv) {
            $insert->execute([
                LENH_LOAI_DAT_CAP_DO, (int)$nv['id'], substr((string)$nv['charname'], 0, 50),
                $capDich, TANG_LEVEL_TOANBO_DANH_DAU, substr(adminName(), 0, 50),
            ]);
        }
        $db->commit();

        ghiNhatKy('nang_level_toanbo', '', 'đã xếp hàng đợi đưa ' . count($dsNV) . ' nhân vật lên cấp ' . $capDich);
        veLai($TRANG, 'success', 'Đã xếp hàng đợi đưa ' . count($dsNV) . ' nhân vật lên cấp ' . $capDich
            . ' (0%). Nhân vật đang online sẽ được nâng NGAY; offline sẽ được nâng khi vào game. '
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
        $st = dbGame()->prepare('SELECT COUNT(*) FROM web_lenh_nhan_vat WHERE loai = ? AND tham_so = ? AND trang_thai IN (?, ?)');
        $st->execute([LENH_LOAI_DAT_CAP_DO, TANG_LEVEL_TOANBO_DANH_DAU, LENH_CHO, LENH_CHO_DANG_NHAP]);
        $soLenhChoXuLy = (int)$st->fetchColumn();

        $st = dbGame()->prepare('SELECT * FROM web_lenh_nhan_vat WHERE loai = ? AND tham_so = ? ORDER BY id DESC LIMIT 100');
        $st->execute([LENH_LOAI_DAT_CAP_DO, TANG_LEVEL_TOANBO_DANH_DAU]);
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
        <p>Đưa MỌI nhân vật đang hoạt động trên server lên ĐÚNG một cấp độ chỉ định — dùng cho sự kiện / bù đắp toàn server.</p>
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
          onsubmit="return confirm('Đưa toàn bộ <?= (int)$soNhanVat ?> nhân vật đang hoạt động lên đúng cấp ' + document.getElementById('capDoDich').value + '?');">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <label class="o"><span>Cấp độ muốn đưa tất cả nhân vật lên</span>
            <input type="number" id="capDoDich" name="cap_do_dich" min="1" max="<?= CAPDO_TOI_DA ?>" value="<?= CAPDO_TOI_DA ?>" required></label>
        <p>Nhân vật đang online được nâng NGAY (kèm điểm cơ bản/kỹ năng của các cấp vừa lên, giống lên cấp tự nhiên);
           nhân vật offline sẽ được nâng khi vào game. Nhân vật đã ở cấp này trở lên sẽ báo lỗi riêng cho nhân vật
           đó (không bị hạ cấp), không ảnh hưởng đến các nhân vật khác.</p>
        <button class="btn btn-do">Nâng Level cho tất cả</button>
    </form>
</div>

<h2>100 lệnh nâng level toàn bộ gần nhất</h2>
<p><a class="btn btn-primary btn-nho" href="<?= e($TRANG) ?>">Cập nhật kết quả</a></p>
<div class="table-wrap"><table><thead><tr><th>Lệnh</th><th>Nhân vật</th><th>Cấp đích</th><th>Trạng thái</th><th>Kết quả</th><th>Thao tác</th></tr></thead><tbody>
<?php foreach ($lenhs as $l): ?>
<tr><td>#<?= (int)$l['id'] ?></td><td><?= e($l['charname']) ?></td><td>cấp <?= (int)$l['cap_do'] ?></td>
<td><?= e(tenTrangThaiLenh((int)$l['trang_thai'])) ?></td><td><?= e($l['ket_qua']) ?></td><td>
<?php if (in_array((int)$l['trang_thai'], [LENH_CHO, LENH_CHO_DANG_NHAP], true)): ?>
<form method="post"><input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>"><input type="hidden" name="viec" value="huy"><input type="hidden" name="id_lenh" value="<?= (int)$l['id'] ?>"><button class="btn btn-nho">Huỷ lệnh</button></form>
<?php endif; ?></td></tr>
<?php endforeach; ?>
<?php if (!$lenhs): ?><tr><td colspan="6">Chưa có lệnh nâng level toàn bộ.</td></tr><?php endif; ?>
</tbody></table></div>
<?php adminFoot(); ?>
