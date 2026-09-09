<?php
/**
 * RESET TOAN BO SERVER VE TRANG: xoá vĩnh viễn nhân vật của MỌI tài khoản (trừ
 * ADMIN_ACCOUNTS), giữ nguyên team_user/5h_active — cùng tinh thần với nút
 * "Reset về trắng" của một tài khoản trong taikhoan.php, nhưng áp dụng hàng loạt.
 *
 * KHÁC VỚI reset TỪNG TÀI KHOẢN: không chờ kết quả từng nhân vật một
 * (choKetQuaLenh) vì có thể có hàng chục/hàng trăm nhân vật — chờ tuần tự sẽ
 * treo trang nhiều phút. Thay vào đó CHÈN THẲNG toàn bộ lệnh xoá vào
 * web_lenh_nhan_vat trong một transaction rồi trả lời ngay; server game (luồng
 * nền LenhWeb, đọc bảng mỗi giây) tự xử lý dần ở phía sau. Trang này cho xem
 * lại tiến độ (còn bao nhiêu nhân vật/lệnh chưa xong) bằng cách tải lại.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/lenh_nhan_vat.php';
requireAdmin();

const RESET_TOANBO_CUM_TU = 'XOA TOAN BO';

$TRANG = 'reset_toanbo.php';

/** id (team_user.id) của các tài khoản quản trị — không đụng tới khi reset hàng loạt. */
function idTaiKhoanAdmin(): array
{
    $tens = adminAccounts();
    if (!$tens) {
        return [];
    }
    $cho = implode(',', array_fill(0, count($tens), '?'));
    $st = dbAccount()->prepare("SELECT id FROM team_user WHERE LOWER(username) IN ($cho)");
    $st->execute($tens);
    return array_map('intval', $st->fetchAll(PDO::FETCH_COLUMN));
}

/** Câu SQL + tham số loại trừ userId của admin, dùng chung cho đếm và cho xoá. */
function dieuKienNgoaiAdmin(array $idsAdmin): array
{
    if (!$idsAdmin) {
        return ['', []];
    }
    $cho = implode(',', array_fill(0, count($idsAdmin), '?'));
    return [" WHERE userId NOT IN ($cho)", $idsAdmin];
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    if (p('xac_nhan') !== RESET_TOANBO_CUM_TU) {
        veLai($TRANG, 'error', 'Gõ đúng cụm "' . RESET_TOANBO_CUM_TU . '" để xác nhận.');
    }

    $idsAdmin = idTaiKhoanAdmin();
    [$dk, $th] = dieuKienNgoaiAdmin($idsAdmin);

    $db = dbGame();
    $st = $db->prepare("SELECT id, charname, userId FROM tob_char$dk");
    $st->execute($th);
    $dsNV = $st->fetchAll();

    if (!$dsNV) {
        veLai($TRANG, 'success', 'Không có nhân vật nào ngoài tài khoản quản trị — không có gì để reset.');
    }

    taoBangLenhNhanVat($db);
    $insert = $db->prepare(
        'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, admin_user) VALUES (?, ?, ?, ?)'
    );
    $db->beginTransaction();
    $taiKhoan = [];
    foreach ($dsNV as $nv) {
        $insert->execute([LENH_LOAI_XOA_NHAN_VAT, (int)$nv['id'], substr((string)$nv['charname'], 0, 50), substr(adminName(), 0, 50)]);
        $taiKhoan[(int)$nv['userId']] = true;
    }
    $db->commit();

    ghiNhatKy('reset_toanbo', '', 'đã xếp hàng đợi xoá ' . count($dsNV) . ' nhân vật của ' . count($taiKhoan)
        . ' tài khoản (loại trừ ' . count($idsAdmin) . ' tài khoản quản trị)');
    veLai($TRANG, 'success', 'Đã xếp hàng đợi lệnh xoá cho ' . count($dsNV) . ' nhân vật của ' . count($taiKhoan)
        . ' tài khoản. Server game sẽ tự xử lý dần ở nền (khoảng 1 nhân vật/giây) — tải lại trang này để xem tiến độ.');
}

// ---------------------------------------------------------------------------
//  Số liệu hiển thị
// ---------------------------------------------------------------------------
$loiDoc = '';
$soNhanVat = 0;
$soTaiKhoan = 0;
$soLenhChoXuLy = 0;
$dsAdmin = adminAccounts();

try {
    $idsAdmin = idTaiKhoanAdmin();
    [$dk, $th] = dieuKienNgoaiAdmin($idsAdmin);

    $st = dbGame()->prepare("SELECT COUNT(*) FROM tob_char$dk");
    $st->execute($th);
    $soNhanVat = (int)$st->fetchColumn();

    $st = dbGame()->prepare("SELECT COUNT(DISTINCT userId) FROM tob_char$dk");
    $st->execute($th);
    $soTaiKhoan = (int)$st->fetchColumn();

    if (coBangLenh(dbGame())) {
        $st = dbGame()->prepare('SELECT COUNT(*) FROM web_lenh_nhan_vat WHERE loai = ? AND trang_thai = ?');
        $st->execute([LENH_LOAI_XOA_NHAN_VAT, LENH_CHO]);
        $soLenhChoXuLy = (int)$st->fetchColumn();
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

adminHead('reset_toanbo', 'Reset toàn server');
?>
<div class="page-head">
    <div>
        <h1 style="color:#c0392b;">Reset toàn server về trắng</h1>
        <p>Xoá vĩnh viễn nhân vật của MỌI tài khoản (trừ quản trị) — dùng khi cần làm mới toàn bộ server.</p>
    </div>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<div class="card">
    <h3>Tình trạng hiện tại</h3>
    <dl class="dl">
        <dt>Nhân vật sẽ bị xoá</dt><dd><?= so($soNhanVat) ?></dd>
        <dt>Tài khoản bị ảnh hưởng</dt><dd><?= so($soTaiKhoan) ?></dd>
        <dt>Tài khoản quản trị được giữ lại</dt>
        <dd><?= $dsAdmin ? e(implode(', ', $dsAdmin)) : '<span class="muted">(ADMIN_ACCOUNTS đang rỗng)</span>' ?></dd>
        <dt>Lệnh xoá đang chờ server xử lý</dt><dd><?= so($soLenhChoXuLy) ?></dd>
    </dl>
    <p class="muted small" style="margin-top:8px;">
        Bấm "Cập nhật số liệu" bên dưới để xem tiến độ sau khi đã gửi lệnh — hai số đầu sẽ giảm dần khi
        server xử lý xong từng nhân vật (khoảng 1 nhân vật/giây).
    </p>
    <p><a class="btn btn-xam btn-nho" href="<?= e($TRANG) ?>">Cập nhật số liệu</a></p>
</div>

<div class="card">
    <h3 style="color:#c0392b;">Xoá tất cả</h3>
    <div class="luuy">
        <p><strong>KHÔNG THỂ HOÀN TÁC.</strong> Xoá vĩnh viễn toàn bộ <strong><?= so($soNhanVat) ?> nhân vật</strong>
           của <strong><?= so($soTaiKhoan) ?> tài khoản</strong> (cùng dữ liệu riêng: thú nuôi, trang trại, đá
           quý...). Các tài khoản trong <code>ADMIN_ACCOUNTS</code> được giữ nguyên, không tài khoản nào bị xoá —
           chỉ nhân vật bị xoá, ai đăng nhập lại đều thấy tài khoản trắng như vừa đăng ký. Nhân vật đang online
           sẽ bị đẩy ra khỏi game trước khi xoá. Nhật ký giao dịch/log vẫn được giữ lại để tra cứu.</p>
    </div>
    <?php if ($soNhanVat === 0): ?>
        <p class="muted small" style="margin-top:12px;">Không có nhân vật nào ngoài tài khoản quản trị — chưa
           cần reset.</p>
    <?php else: ?>
        <form method="post" action="<?= e($TRANG) ?>" class="ngang" style="margin-top:12px;"
              onsubmit="return confirm('RESET TOÀN SERVER — xoá vĩnh viễn <?= so($soNhanVat) ?> nhân vật của <?= so($soTaiKhoan) ?> tài khoản?\n\nKhông thể hoàn tác.');">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <label class="o" style="min-width:260px;">
                <span>Gõ đúng cụm "<?= e(RESET_TOANBO_CUM_TU) ?>" để xác nhận</span>
                <input type="text" name="xac_nhan" autocomplete="off"
                       oninput="document.getElementById('nutResetToanBo').disabled = this.value !== <?= e(json_encode(RESET_TOANBO_CUM_TU)) ?>;">
            </label>
            <button type="submit" id="nutResetToanBo" class="btn btn-do btn-nho" disabled style="align-self:flex-end;">Reset toàn server</button>
        </form>
    <?php endif; ?>
</div>
<?php adminFoot(); ?>
