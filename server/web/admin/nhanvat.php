<?php
/**
 * Quản lý nhân vật (bảng kpah_db.tob_char).
 *
 * KHOÁ NHÂN VẬT là đặt tob_char.ban = 1 — đúng cột mà bảng điều khiển trong game
 * dùng (AdminPanel.handleBanAccount). Hệ quả trong game:
 *   · Nhân vật BIẾN MẤT khỏi màn hình chọn nhân vật (câu lấy danh sách lọc
 *     `AND ban=0 AND del=1`), người chơi sẽ tưởng bị xoá tài khoản.
 *   · Bị loại khỏi mọi bảng xếp hạng (các câu top đều có `and ban=0`).
 *   · Tài khoản VẪN đăng nhập được, chỉ là không còn nhân vật để vào.
 * Muốn chặn cả tài khoản thì khoá ở trang Tài khoản (team_user.ban).
 *
 * -----------------------------------------------------------------------
 *  ĐẶT CẤP ĐỘ — ĐI QUA SERVER GAME, KHÔNG GHI THẲNG tob_char
 * -----------------------------------------------------------------------
 * Khoá nhân vật AN TOÀN khi người chơi đang online vì cột `ban` không nằm
 * trong câu UPDATE lưu nhân vật định kỳ (DatabaseNhanVat.java) — server không
 * bao giờ ghi đè lại cột đó, nên khoá "chỉ ăn từ lần vào sau" đúng nghĩa: chắc
 * chắn ăn, chỉ là chưa ngay lập tức.
 *
 * `lastLv` và `xp` THÌ KHÁC: hai cột này nằm ngay trong câu lưu định kỳ đó.
 * Trước 02/09/2026 trang này UPDATE thẳng chúng; với nhân vật đang online thì
 * bản cũ trong RAM của tiến trình game bị lưu đè lại ngay lần tự lưu kế tiếp —
 * người chơi thoát ra vào lại vẫn thấy cấp cũ, không một cảnh báo nào. Web
 * không nhìn thấy RAM server nên không tự xử lý được.
 *
 * CÁCH LÀM HIỆN TẠI (inc/lenh_nhan_vat.php + src/data/LenhWeb.java): web chỉ
 * CHÈN MỘT DÒNG LỆNH vào bảng `web_lenh_nhan_vat`. Server đọc bảng mỗi giây:
 *   · nhân vật đang online  → cộng thẳng vào RAM bằng đúng đường lên cấp tự
 *     nhiên (Map.addXPForChar, như nút "Buff EXP" của AdminPanel) rồi lưu ngay;
 *   · nhân vật offline      → UPDATE lastLv/xp như web làm trước đây;
 *   · lỗi                   → ghi lý do vào ket_qua.
 * Trang web đợi tối đa vài giây rồi đọc lại trạng thái để báo cho người bấm.
 * Server đang tắt thì lệnh nằm chờ và được thực hiện khi server chạy lại.
 * Web KHÔNG tính exp nữa — server tính bằng chính real.LevelDetail; capdo.php
 * chỉ còn dùng để giới hạn cấp (CAPDO_TOI_DA) và cho bộ kiểm đối chiếu bảng.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/vatpham.php';
require_once __DIR__ . '/inc/capdo.php';
require_once __DIR__ . '/inc/lenh_nhan_vat.php';
requireAdmin();

$TRANG = 'nhanvat.php';

/** Tên tài khoản sở hữu một nhân vật, để không lỡ tay khoá nhân vật của admin. */
function chuNhan(int $userId): string
{
    try {
        $st = dbAccount()->prepare('SELECT username FROM team_user WHERE id = ? LIMIT 1');
        $st->execute([$userId]);
        return (string)($st->fetchColumn() ?: '');
    } catch (Throwable $ex) {
        return '';
    }
}

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $viec = p('viec');
    $id   = (int)p('id');

    $st = dbGame()->prepare('SELECT id, charname, userId, ban FROM tob_char WHERE id = ? LIMIT 1');
    $st->execute([$id]);
    $nv = $st->fetch();

    if (!$nv) {
        veLai($TRANG, 'error', 'Không tìm thấy nhân vật #' . $id . '.');
    }

    $ten = (string)$nv['charname'];
    $ve  = $TRANG . '?id=' . $id;

    if ($viec === 'dat_cap_do' || $viec === 'buff_level') {
        // Ca hai deu la MOT lenh "dat_cap_do" gui cho server game (xem inc/lenh_nhan_vat.php);
        // "Buff level kem %" chi khac o cho cho nhap them % vao cap. Port tu
        // server/Panel/ButtonEditor.java showBuffExpDialog ("Len Level/Phan tram"), nhung
        // AdminPanel desktop chi dung duoc khi nhan vat dang online - o day server tu biet
        // online hay offline va xu ly tuong ung, web khong con ghi thang tob_char.
        $laBuff   = $viec === 'buff_level';
        $capDoMoi = (int)p($laBuff ? 'cap_do_buff' : 'cap_do');
        $phanTram = $laBuff ? (float)p('phan_tram_buff') : 0.0;
        if ($capDoMoi < 1 || $capDoMoi > CAPDO_TOI_DA) {
            veLai($ve, 'error', 'Cấp độ phải từ 1 đến ' . CAPDO_TOI_DA . '.');
        }
        // Bang luu phan nghin (buoc 0,1%) nen chi nhan toi 99,9 - nhap 99,95 se bi lam tron
        // thanh 100,0% = sang cap sau, khong dung y nguoi bam.
        if ($phanTram < 0 || $phanTram > 99.9) {
            veLai($ve, 'error', '% vào cấp phải từ 0 đến 99,9 (bước 0,1).');
        }
        $phanNghin = phanTramRaPhanNghin($phanTram);

        $stCu = dbGame()->prepare('SELECT lastLv, xp FROM tob_char WHERE id = ? LIMIT 1');
        $stCu->execute([$id]);
        $cu = $stCu->fetch();

        $idLenh = guiLenhCapDo(dbGame(), $id, $ten, $capDoMoi, $phanNghin, adminName());
        $lenh   = choKetQuaLenh(dbGame(), $idLenh);
        $muc    = 'cấp ' . $capDoMoi . ($phanNghin > 0 ? ' (' . phanNghinRaChuoi($phanNghin) . ')' : '');
        [$loaiBao, $thongBao] = moTaKetQuaLenh($lenh, $ten, $muc);

        // Nhat ky ghi dung so server dung (phan nghin), khong ghi so tho nguoi dung go.
        ghiNhatKy($laBuff ? 'buff_level_nhan_vat' : 'dat_cap_do_nhan_vat', $ten,
            'lastLv: ' . (int)($cu['lastLv'] ?? '?') . ' -> ' . $capDoMoi
            . ($phanNghin > 0 ? ' (' . phanNghinRaChuoi($phanNghin) . ')' : '')
            . ' | xp cũ: ' . (string)($cu['xp'] ?? '?')
            . ' | lệnh #' . $idLenh . ': ' . tenTrangThaiLenh((int)($lenh['trang_thai'] ?? -1))
            . ($lenh && $lenh['ket_qua'] !== '' ? ' - ' . $lenh['ket_qua'] : ''));

        veLai($ve, $loaiBao, $thongBao);
    }

    if ($viec === 'huy_lenh') {
        $idLenh = (int)p('id_lenh');
        if (huyLenh(dbGame(), $idLenh, adminName())) {
            ghiNhatKy('huy_lenh_nhan_vat', $ten, 'lệnh #' . $idLenh);
            veLai($ve, 'success', 'Đã huỷ lệnh #' . $idLenh . ' cho ' . $ten . '.');
        }
        veLai($ve, 'error', 'Không huỷ được lệnh #' . $idLenh . ': server đã xử lý xong (hoặc lệnh không tồn tại).');
    }

    if ($viec === 'xoa_vat_pham') {
        // Cột phải nằm trong danh sách trắng — tên cột được ghép thẳng vào SQL bên dưới
        // (không tham số hoá được tên cột qua PDO) nên KHÔNG được nhận trực tiếp từ input.
        $cot = p('cot');
        $viTri = (int)p('vi_tri');
        if (!in_array($cot, LENH_COT_VAT_PHAM, true)) {
            veLai($ve, 'error', 'Cột vật phẩm không hợp lệ.');
        }

        // Web KHONG sua cot nua (nhan vat online se bi luu de). Chi doc chuoi hien tai de lay
        // DUNG mau tho cua mon do o vi tri da bam, roi gui cho server (inc/lenh_nhan_vat.php);
        // server tim mon khop tung ky tu trong RAM (online) hay trong CSDL (offline).
        $stCu = dbGame()->prepare("SELECT $cot FROM tob_char WHERE id = ? LIMIT 1");
        $stCu->execute([$id]);
        $chuoiCu = trim((string)($stCu->fetchColumn() ?: ''));
        if ($chuoiCu !== '' && $chuoiCu[0] === '>') {
            $chuoiCu = substr($chuoiCu, 1);
        }
        $cacMau = explode('>', $chuoiCu);
        if (!isset($cacMau[$viTri]) || trim($cacMau[$viTri]) === '') {
            veLai($ve, 'error', 'Không tìm thấy vật phẩm ở vị trí ' . $viTri . ' — dữ liệu đã đổi, tải lại trang rồi bấm lại.');
        }
        $mau = $cacMau[$viTri];

        // Doi chieu voi mon nguoi bam da NHIN THAY (template + plus gui kem tu form): neu server
        // vua luu lai giua luc mo trang va bam, vi tri nay co the da la mon khac.
        $moTa = 'vị trí ' . $viTri;
        $khop = false;
        foreach (docChuoiVatPham($mau) as $vp) {
            $tenId = tenVatPhamTheoId(dbGame(), [(int)$vp['template']]);
            $moTa = ($tenId[$vp['template']] ?? ('#' . $vp['template']))
                . ((int)$vp['plus'] > 0 ? ' +' . (int)$vp['plus'] : '');
            $khop = (int)p('template', '-1') === (int)$vp['template'] && (int)p('plus', '0') === (int)$vp['plus'];
            break;
        }
        if (!$khop) {
            veLai($ve, 'error', 'Vật phẩm ở vị trí ' . $viTri . ' hiện là "' . $moTa
                . '", không còn là món bạn bấm — dữ liệu đã đổi, tải lại trang rồi bấm lại.');
        }

        $idLenh = guiLenhXoaVatPham(dbGame(), $id, $ten, $cot, $mau, adminName());
        $lenh   = choKetQuaLenh(dbGame(), $idLenh);
        $muc    = 'xoá "' . $moTa . '" (' . $cot . ')';
        [$loaiBao, $thongBao] = moTaKetQuaLenh($lenh, $ten, $muc);

        ghiNhatKy('xoa_vat_pham_nhan_vat', $ten, "cột $cot, xoá: $moTa"
            . ' | lệnh #' . $idLenh . ': ' . tenTrangThaiLenh((int)($lenh['trang_thai'] ?? -1))
            . ($lenh && $lenh['ket_qua'] !== '' ? ' - ' . $lenh['ket_qua'] : ''));

        veLai($ve, $loaiBao, $thongBao);
    }

    if ($viec === 'xoa_nhan_vat') {
        // XOA VINH VIEN - khong hoan tac duoc, khac han "Khoa" (chi an di). Chan tai khoan
        // quan tri giong het "khoa" o duoi, cong them go dung ten de xac nhan (JS chi bat
        // nut, kiem tra that o day - phong khi ai do POST thang khong qua giao dien).
        $chu = chuNhan((int)$nv['userId']);
        if ($chu !== '' && laTaiKhoanAdmin($chu)) {
            veLai($ve, 'error', 'Nhân vật này thuộc tài khoản quản trị (' . $chu . ') nên không xoá được.');
        }
        if (p('xac_nhan_ten') !== $ten) {
            veLai($ve, 'error', 'Gõ đúng tên nhân vật "' . $ten . '" để xác nhận xoá.');
        }

        $idLenh = guiLenhXoaNhanVat(dbGame(), $id, $ten, adminName());
        $lenh   = choKetQuaLenh(dbGame(), $idLenh);
        [$loaiBao, $thongBao] = moTaKetQuaLenh($lenh, $ten, 'xoá vĩnh viễn', LENH_LOAI_XOA_NHAN_VAT);

        ghiNhatKy('xoa_nhan_vat', $ten,
            'lệnh #' . $idLenh . ': ' . tenTrangThaiLenh((int)($lenh['trang_thai'] ?? -1))
            . ($lenh && $lenh['ket_qua'] !== '' ? ' - ' . $lenh['ket_qua'] : ''));

        // Xoa xong thi nhan vat khong con nua - ve danh sach, khong ve lai trang chi tiet
        // id nay (se ra "khong tim thay nhan vat").
        if ($lenh !== null && (int)$lenh['trang_thai'] === LENH_XONG_ONLINE) {
            veLai($TRANG, $loaiBao, $thongBao);
        }
        veLai($ve, $loaiBao, $thongBao);
    }

    if ($viec !== 'khoa' && $viec !== 'mo_khoa') {
        veLai($TRANG, 'error', 'Không hiểu thao tác.');
    }

    if ($viec === 'khoa') {
        $chu = chuNhan((int)$nv['userId']);
        if ($chu !== '' && laTaiKhoanAdmin($chu)) {
            veLai($ve, 'error', 'Nhân vật này thuộc tài khoản quản trị (' . $chu . ') nên không khoá được.');
        }
    }

    $banMoi = $viec === 'khoa' ? 1 : 0;
    $up = dbGame()->prepare('UPDATE tob_char SET ban = ? WHERE id = ?');
    $up->execute([$banMoi, $id]);

    ghiNhatKy($viec === 'khoa' ? 'khoa_nhan_vat' : 'mo_nhan_vat', $ten,
        'ban: ' . (int)$nv['ban'] . ' -> ' . $banMoi);

    veLai($ve, 'success', $viec === 'khoa'
        ? 'Đã khoá nhân vật ' . $ten . '. Nhân vật sẽ biến mất khỏi màn chọn nhân vật của người chơi.'
        : 'Đã mở khoá nhân vật ' . $ten . '.');
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu
// ---------------------------------------------------------------------------
$q     = g('q');
$xemId = (int)g('id');
$ketQua = [];
$ct     = null;
$chuTk  = '';
$loiDoc = '';
$vpRow  = [];
$loiVatPham = '';

$COT = 'id, charname, userId, lastLv, `class`, xp, gold, luong, luonglock, ban, del,
        lastLog, idserver, idClan, nhack, blacklist, tichluy, topNap';

/**
 * Nạp chi tiết một nhân vật, kèm ba cột vật phẩm.
 *
 * Trả về [$ct, $chuTk, $vpRow, $loiVatPham]; $ct = null nếu không có nhân vật đó.
 * Tách thành hàm riêng vì có hai lối vào: mở thẳng bằng ?id=, và tìm ra đúng một
 * nhân vật bằng ?q=.
 */
function docChiTiet(int $id, string $cot): array
{
    $st = dbGame()->prepare("SELECT $cot FROM tob_char WHERE id = ? LIMIT 1");
    $st->execute([$id]);
    $ct = $st->fetch() ?: null;
    if (!$ct) {
        return [null, '', [], ''];
    }
    // Ba cột này là TEXT dài (cả hành trang serialize vào đó) nên chỉ lấy ở trang chi
    // tiết một nhân vật, KHÔNG thêm vào $COT — $COT còn dùng cho câu liệt kê 50 dòng.
    try {
        $stV = dbGame()->prepare('SELECT equip, inven, bag FROM tob_char WHERE id = ? LIMIT 1');
        $stV->execute([$id]);
        return [$ct, chuNhan((int)$ct['userId']), $stV->fetch() ?: [], ''];
    } catch (Throwable $ex) {
        return [$ct, chuNhan((int)$ct['userId']), [], $ex->getMessage()];
    }
}

try {
    if ($xemId > 0) {
        list($ct, $chuTk, $vpRow, $loiVatPham) = docChiTiet($xemId, $COT);
    } elseif ($q !== '') {
        if (ctype_digit($q)) {
            // Số: hiểu là userId (xem hết nhân vật của một tài khoản) hoặc id nhân vật.
            $st = dbGame()->prepare("SELECT $COT FROM tob_char WHERE userId = ? OR id = ?
                                      ORDER BY lastLv DESC LIMIT 50");
            $st->execute([(int)$q, (int)$q]);
        } else {
            $st = dbGame()->prepare("SELECT $COT FROM tob_char WHERE charname LIKE ?
                                      ORDER BY lastLv DESC LIMIT 50");
            $st->execute(['%' . $q . '%']);
        }
        $ketQua = $st->fetchAll();

        // Tìm ra đúng một nhân vật thì mở luôn trang chi tiết. Thiếu bước này thì bấm
        // tên nhân vật ở trang Log (liên kết dạng ?q=ten) sẽ dừng lại ở một bảng đúng
        // một dòng — không có phần vật phẩm — và người xem tưởng trang không hề có.
        if (count($ketQua) === 1) {
            list($ct, $chuTk, $vpRow, $loiVatPham) = docChiTiet((int)$ketQua[0]['id'], $COT);
        }
    } else {
        $ketQua = dbGame()->query("SELECT $COT FROM tob_char ORDER BY lastLog DESC LIMIT 30")->fetchAll();
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

// ---- Vật phẩm của nhân vật đang xem ----
$vpMac = docChuoiVatPham($vpRow['equip'] ?? null);
$vpTach = tachTuiVaThoiTrang($vpRow['inven'] ?? null);
$vpTui = $vpTach['tui'];
$vpThoiTrang = $vpTach['thoi_trang'];
$vpRuong = docChuoiVatPham($vpRow['bag'] ?? null);

$tenVP = [];
if ($ct) {
    $moiId = [];
    foreach ([$vpMac, $vpTui, $vpThoiTrang, $vpRuong] as $nhom) {
        foreach ($nhom as $v) {
            $moiId[] = (int)$v['template'];
        }
    }
    try {
        $tenVP = tenVatPhamTheoId(dbGame(), $moiId);
    } catch (Throwable $ex) {
        $tenVP = [];
    }
}

/** Một bảng vật phẩm. $cot/$idNhanVat null thì không có nút xoá (dùng cho nơi khác nếu cần). */
function bangVatPham(array $ds, array $ten, string $trong, ?string $cot = null, ?int $idNhanVat = null): void
{
    if (!$ds) {
        echo '<p class="muted small">' . e($trong) . '</p>';
        return;
    }
    $coXoa = $cot !== null && $idNhanVat !== null;
    echo '<div class="tbl-wrap"><table class="tbl"><thead><tr>'
       . '<th>Vật phẩm</th><th class="num">+</th><th>Màu</th><th>Ghi chú</th>'
       . ($coXoa ? '<th></th>' : '') . '</tr></thead><tbody>';
    foreach ($ds as $v) {
        $id = (int)$v['template'];
        $ghi = [];
        if ($v['khoa']) {
            $ghi[] = 'khoá';
        }
        if ((int)$v['tinh_luyen'] > 0) {
            $ghi[] = 'tinh luyện ' . (int)$v['tinh_luyen'] . '%';
        }
        if ((int)$v['thue_phut'] > 0) {
            $ghi[] = 'có hạn dùng';
        }
        if ((int)$v['level'] > 0) {
            $ghi[] = 'cấp ' . (int)$v['level'];
        }
        printf(
            '<tr><td>%s <span class="muted small">#%d</span></td>'
            . '<td class="num">%s</td><td>%s</td><td class="muted small">%s</td>',
            e($ten[$id] ?? '(không có trong data_item)'),
            $id,
            (int)$v['plus'] > 0 ? '+' . (int)$v['plus'] : '',
            e(tenMauVatPham((int)$v['mau'])),
            e(implode(' · ', $ghi))
        );
        if ($coXoa) {
            $tenMon = ($ten[$id] ?? ('#' . $id)) . ((int)$v['plus'] > 0 ? ' +' . (int)$v['plus'] : '');
            ?>
            <td class="nowrap">
                <form method="post" action="nhanvat.php" class="inline"
                      onsubmit="return confirm('Xoá vĩnh viễn <?= e($tenMon) ?> khỏi nhân vật?\n\nNhân vật đang online thì server gỡ ngay trong game; offline thì xoá trong CSDL.');">
                    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                    <input type="hidden" name="viec" value="xoa_vat_pham">
                    <input type="hidden" name="id" value="<?= $idNhanVat ?>">
                    <input type="hidden" name="cot" value="<?= e($cot) ?>">
                    <input type="hidden" name="vi_tri" value="<?= (int)$v['_i'] ?>">
                    <input type="hidden" name="template" value="<?= $id ?>">
                    <input type="hidden" name="plus" value="<?= (int)$v['plus'] ?>">
                    <button class="btn btn-do btn-nho">Xoá</button>
                </form>
            </td>
            <?php
        }
        echo '</tr>';
    }
    echo '</tbody></table></div>';
}

function nhanNhanVat(array $nv): string
{
    if ((int)$nv['ban'] !== 0) return '<span class="nhan nhan-khoa">Bị khoá</span>';
    if ((int)$nv['del'] !== 1) return '<span class="nhan nhan-mo">Đã xoá</span>';
    return '<span class="nhan nhan-ok">Bình thường</span>';
}

function nutKhoa(array $nv): void
{
    $khoa = (int)$nv['ban'] !== 0;
    ?>
    <form method="post" action="nhanvat.php" class="inline"
          onsubmit="return confirm('<?= $khoa ? 'Mở khoá' : 'Khoá' ?> nhân vật <?= e($nv['charname']) ?>?');">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="id" value="<?= (int)$nv['id'] ?>">
        <input type="hidden" name="viec" value="<?= $khoa ? 'mo_khoa' : 'khoa' ?>">
        <button class="btn <?= $khoa ? 'btn-primary' : 'btn-do' ?> btn-nho"><?= $khoa ? 'Mở khoá' : 'Khoá' ?></button>
    </form>
    <?php
}

adminHead('nhanvat', 'Nhân vật');
?>
<div class="page-head">
    <div>
        <h1>Nhân vật</h1>
        <p>Khoá / mở khoá từng nhân vật. Muốn chặn cả tài khoản thì làm ở trang <a href="taikhoan.php">Tài khoản</a>.</p>
    </div>
    <form method="get" action="nhanvat.php" class="ngang">
        <label class="o" style="min-width:240px;">
            <span>Tìm theo tên nhân vật, hoặc gõ số để tra theo ID tài khoản</span>
            <input type="text" name="q" value="<?= e($q) ?>" placeholder="ví dụ: tennhanvat">
        </label>
        <button type="submit" class="btn btn-primary btn-nho">Tìm</button>
    </form>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<?php if ($ct): ?>
    <p><a href="nhanvat.php">← Về danh sách</a></p>
    <div class="grid-side">
        <div class="card">
            <h3><?= e($ct['charname']) ?> <?= nhanNhanVat($ct) ?></h3>
            <dl class="dl">
                <dt>ID nhân vật</dt><dd><?= (int)$ct['id'] ?></dd>
                <dt>Tài khoản</dt><dd>
                    <?php if ($chuTk !== ''): ?>
                        <a href="taikhoan.php?id=<?= (int)$ct['userId'] ?>"><?= e($chuTk) ?></a>
                        <span class="muted small">(#<?= (int)$ct['userId'] ?>)</span>
                    <?php else: ?>
                        <span class="muted">không tìm thấy tài khoản #<?= (int)$ct['userId'] ?></span>
                    <?php endif; ?>
                </dd>
                <dt>Nghề</dt><dd><?= e(tenNghe($ct['class'])) ?></dd>
                <dt>Cấp độ</dt><dd><?= (int)$ct['lastLv'] ?> <span class="muted small">(<?= so($ct['xp']) ?> kinh nghiệm)</span></dd>
                <dt>Xu</dt><dd><?= so($ct['gold']) ?></dd>
                <dt>Lượng</dt><dd><?= so($ct['luong']) ?> <span class="muted small">+ <?= so($ct['luonglock']) ?> khoá</span></dd>
                <dt>Tích luỹ</dt><dd><?= so($ct['tichluy']) ?> <span class="muted small">· top nạp <?= so($ct['topNap']) ?></span></dd>
                <dt>Vào lần cuối</dt><dd><?= e((string)$ct['lastLog']) ?></dd>
                <dt>Máy chủ</dt><dd><?= (int)$ct['idserver'] ?></dd>
                <dt>Bang hội</dt><dd><?= (int)$ct['idClan'] === -1 ? '<span class="muted">không có</span>' : (int)$ct['idClan'] ?></dd>
                <dt>Cờ nghi vấn</dt><dd>nhack <?= (int)$ct['nhack'] ?> · blacklist <?= (int)$ct['blacklist'] ?></dd>
            </dl>

            <div class="form-actions"><?php nutKhoa($ct); ?></div>

            <?php if ((int)$ct['ban'] !== 0): ?>
                <div class="luuy" style="margin-top:14px;">
                    <p><strong>Nhân vật này đang bị khoá.</strong></p>
                    <p>Nếu bạn không phải người khoá nó thì rất có thể server tự khoá: khi lưu nhân vật
                       thất bại (ví dụ hành trang quá dài so với sức chứa của cột <code>inven</code>),
                       khối bắt lỗi trong <code>Database.java</code> tự đặt <code>ban = 1</code>.
                       Xem <code>vps/sql-giftcode.md</code>, mục “CẠM BẪY ĐÃ NỔ MỘT LẦN”.</p>
                    <p>Mở khoá lấy lại được nhân vật, nhưng <em>không</em> lấy lại phần dữ liệu đã mất
                       giữa lúc lưu hỏng.</p>
                </div>
            <?php endif; ?>

            <h4 style="margin:18px 0 10px;">Đặt cấp độ</h4>
            <form method="post" action="nhanvat.php" class="ngang"
                  onsubmit="return confirm('Đặt cấp độ ' + document.getElementById('capDoMoi').value + ' cho <?= e($ct['charname']) ?>?\n\nNhân vật đang online sẽ được cộng ngay; offline thì cộng ngay khi họ vào game. Chỉ NÂNG cấp, không hạ.');">
                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                <input type="hidden" name="viec" value="dat_cap_do">
                <input type="hidden" name="id" value="<?= (int)$ct['id'] ?>">
                <label class="o" style="max-width:140px;">
                    <span>Cấp độ mới (hiện tại: <?= (int)$ct['lastLv'] ?>)</span>
                    <input type="number" id="capDoMoi" name="cap_do" min="1" max="<?= CAPDO_TOI_DA ?>"
                           value="<?= (int)$ct['lastLv'] ?>" required>
                </label>
                <button class="btn btn-do btn-nho" style="align-self:flex-end;">Đặt cấp độ</button>
            </form>
            <div class="luuy" style="margin-top:12px;">
                <p><strong>Dùng được cả khi nhân vật đang online.</strong> Trang web không ghi thẳng
                   CSDL nữa mà gửi một lệnh vào bảng <code>web_lenh_nhan_vat</code>; server game đọc
                   mỗi giây và tự xử lý: nhân vật <strong>đang online</strong> được cộng ngay trong
                   game (đúng như lên cấp tự nhiên: có điểm cơ bản, điểm kỹ năng, hồi đầy HP/MP) rồi
                   lưu lại tức thì; nhân vật <strong>offline</strong> thì lệnh nằm chờ và server cộng
                   ngay khi họ vào game. Kết quả hiện ngay sau khi bấm.</p>
                <p class="small"><strong>Chỉ nâng cấp.</strong> Cấp/% mới phải cao hơn mức hiện tại,
                   nếu không server từ chối và báo lỗi — hạ cấp để lại điểm cơ bản/kỹ năng vượt mức,
                   chốt chống gian lận lúc đăng nhập sẽ xoá điểm và kỹ năng của người chơi. Server game
                   đang tắt thì lệnh nằm chờ và được thực hiện khi server chạy lại — không cần bấm lại;
                   theo dõi hoặc huỷ ở bảng "Lệnh đã gửi" bên dưới.</p>
            </div>

            <h4 style="margin:18px 0 10px;">Buff level kèm %</h4>
            <form method="post" action="nhanvat.php" class="ngang"
                  onsubmit="return confirm('Buff ' + document.getElementById('capMoiBuff').value + ' (' + document.getElementById('phanTramBuff').value + '%) cho <?= e($ct['charname']) ?>?\n\nNhân vật đang online sẽ được cộng ngay; offline thì cộng ngay khi họ vào game. Chỉ NÂNG cấp, không hạ.');">
                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                <input type="hidden" name="viec" value="buff_level">
                <input type="hidden" name="id" value="<?= (int)$ct['id'] ?>">
                <label class="o" style="max-width:140px;">
                    <span>Cấp độ</span>
                    <input type="number" id="capMoiBuff" name="cap_do_buff" min="1" max="<?= CAPDO_TOI_DA ?>"
                           value="<?= (int)$ct['lastLv'] ?>" required>
                </label>
                <label class="o" style="max-width:140px;">
                    <span>% vào cấp</span>
                    <input type="number" id="phanTramBuff" name="phan_tram_buff" min="0" max="99.9" step="0.1"
                           value="0" required>
                </label>
                <button class="btn btn-do btn-nho" style="align-self:flex-end;">Buff level</button>
            </form>
            <div class="luuy" style="margin-top:12px;">
                <p>Giống hệt "Đặt cấp độ" nhưng cho nhập thêm % tiến độ vào cấp đó (thay vì luôn
                   là 0%) — port từ tính năng "Buff EXP → Lên Level/Phần trăm" đã có sẵn trong
                   AdminPanel (ứng dụng chạy trên server). Cũng đi qua bảng lệnh như "Đặt cấp độ"
                   ở trên nên dùng được cả khi nhân vật đang online lẫn offline.</p>
            </div>

            <?php
            // Lenh da gui cho nhan vat nay (moi nhat truoc). Bang chua co -> rong, khong loi.
            $lenhGanDay = [];
            try {
                $lenhGanDay = docLenhCuaNhanVat(dbGame(), (int)$ct['id']);
            } catch (Throwable $exLenh) {
                $lenhGanDay = [];
            }
            ?>
            <h4 style="margin:18px 0 10px;">Lệnh đã gửi</h4>
            <?php if (!$lenhGanDay): ?>
                <p class="muted small">Chưa có lệnh nào cho nhân vật này.</p>
            <?php else: ?>
                <div class="tbl-wrap"><table class="tbl">
                    <thead><tr><th>#</th><th>Yêu cầu</th><th>Trạng thái</th><th>Kết quả</th><th>Người gửi</th><th>Lúc</th><th></th></tr></thead>
                    <tbody>
                    <?php foreach ($lenhGanDay as $l): $tt = (int)$l['trang_thai']; ?>
                        <tr>
                            <td class="muted small"><?= (int)$l['id'] ?></td>
                            <td><?= e(moTaYeuCauLenh($l)) ?></td>
                            <td><?= e(tenTrangThaiLenh($tt)) ?></td>
                            <td class="small"><?= e((string)$l['ket_qua']) ?></td>
                            <td class="small"><?= e((string)$l['admin_user']) ?></td>
                            <td class="muted small"><?= e((string)$l['tao_luc']) ?><?= $l['xu_ly_luc'] ? ' → ' . e((string)$l['xu_ly_luc']) : '' ?></td>
                            <td>
                                <?php // xoa_nhan_vat khong bao gio huy duoc (xem huyLenh() trong
                                      // inc/lenh_nhan_vat.php) - khong hien nut de tranh nguoi
                                      // dung tuong huy duoc trong khi thao tac that ra khong the
                                      // "huy tro" mot khi da xoa xong. ?>
                                <?php if (($tt === LENH_CHO || $tt === LENH_CHO_DANG_NHAP) && (string)$l['loai'] !== LENH_LOAI_XOA_NHAN_VAT): ?>
                                    <form method="post" action="nhanvat.php" style="display:inline"
                                          onsubmit="return confirm('Huỷ lệnh #<?= (int)$l['id'] ?>?');">
                                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                                        <input type="hidden" name="viec" value="huy_lenh">
                                        <input type="hidden" name="id" value="<?= (int)$ct['id'] ?>">
                                        <input type="hidden" name="id_lenh" value="<?= (int)$l['id'] ?>">
                                        <button class="btn btn-nho">Huỷ</button>
                                    </form>
                                <?php endif; ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table></div>
                <p class="muted small" style="margin-top:6px;">"Chờ vào game" = nhân vật đang offline, server sẽ cộng ngay khi họ đăng nhập.
                   "Chờ server" quá lâu = server game đang tắt; lệnh sẽ chạy khi server lên.</p>
            <?php endif; ?>

            <h4 style="margin:22px 0 10px;color:#c0392b;">Xoá vĩnh viễn</h4>
            <div class="luuy">
                <p><strong>KHÔNG THỂ HOÀN TÁC.</strong> Khác hẳn "Khoá" ở trên (chỉ ẩn đi, mở lại được) —
                   xoá vĩnh viễn xoá hẳn dòng <code>tob_char</code> này khỏi cơ sở dữ liệu, cùng với thú
                   nuôi, trang trại, đá quý, bạn bè, quest riêng của nhân vật. Nhân vật đang online sẽ bị
                   đẩy ra khỏi game trước khi xoá. Nhật ký giao dịch/log vẫn được giữ lại để tra cứu.
                   Nếu nhân vật đang trong bang hội, hãy tự rời bang trước — trang này không tự gỡ tên
                   khỏi danh sách thành viên bang hội.</p>
            </div>
            <form method="post" action="nhanvat.php" class="ngang" style="margin-top:12px;"
                  onsubmit="return confirm('XOÁ VĨNH VIỄN <?= e($ct['charname']) ?>?\n\nKhông thể hoàn tác.');">
                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                <input type="hidden" name="viec" value="xoa_nhan_vat">
                <input type="hidden" name="id" value="<?= (int)$ct['id'] ?>">
                <label class="o" style="min-width:220px;">
                    <span>Gõ đúng tên "<?= e($ct['charname']) ?>" để xác nhận</span>
                    <input type="text" name="xac_nhan_ten" autocomplete="off"
                           oninput="document.getElementById('nutXoaNV').disabled = this.value !== <?= json_encode($ct['charname']) ?>;">
                </label>
                <button type="submit" id="nutXoaNV" class="btn btn-do btn-nho" disabled style="align-self:flex-end;">Xoá vĩnh viễn</button>
            </form>
        </div>

        <div class="card">
            <h3>Khoá nhân vật làm gì</h3>
            <ul class="muted" style="font-size:.9rem;padding-left:18px;margin:0;">
                <li>Nhân vật <strong>biến mất khỏi màn hình chọn nhân vật</strong> — người chơi thường
                    hiểu nhầm là bị xoá.</li>
                <li>Bị loại khỏi mọi bảng xếp hạng.</li>
                <li>Tài khoản <strong>vẫn đăng nhập được</strong> và vẫn chơi được các nhân vật khác.</li>
                <li>Người đang online <strong>không bị đá ra</strong> — việc khoá ghi thẳng cột
                    <code>ban</code>, không đi qua bảng lệnh như "Đặt cấp độ". Khoá chỉ ăn từ lần vào sau.</li>
            </ul>
        </div>
    </div>

    <?php if ($loiVatPham !== ''): ?>
        <div class="flash flash-error">Không đọc được vật phẩm: <?= e($loiVatPham) ?></div>
    <?php endif; ?>

    <div class="card">
        <h3>Vật phẩm</h3>
        <p class="muted small" style="margin:0 0 14px;">
            Máy chủ này <strong>không có bảng <code>tob_item</code></strong> — trang bị, hành trang
            và rương được ghi thẳng vào ba cột <code>equip</code>, <code>inven</code>,
            <code>bag</code> của <code>tob_char</code>. Đây là ảnh chụp <em>lần lưu gần nhất</em>:
            người chơi đang online thì đồ thật nằm trong RAM của server, chỉ khớp sau khi họ
            thoát hoặc server tự lưu.
        </p>
        <div class="luuy" style="margin:0 0 14px;">
            <p><strong>Nút "Xoá" dùng được cả khi nhân vật đang online.</strong> Giống "Đặt cấp
               độ", lệnh xoá đi qua bảng lệnh cho server game: nhân vật đang online thì server tìm
               đúng món đó trong RAM, gỡ ra và lưu ngay; offline thì server xoá trong CSDL rồi đối
               chiếu lại một lần nữa khi nhân vật vào game. Server báo lỗi nếu món đồ đã đổi so với
               bảng đang xem (tải lại trang rồi bấm lại). Mọi thao tác xoá đều được ghi lại ở trang
               <a href="log.php">Log</a> và bảng "Lệnh đã gửi".</p>
        </div>

        <div class="grid-2">
            <div>
                <h4>Đang mặc <span class="muted small">(<?= count($vpMac) ?>)</span></h4>
                <?php bangVatPham($vpMac, $tenVP, 'Không mặc gì.', 'equip', (int)$ct['id']); ?>
            </div>
            <div>
                <h4>Thời trang <span class="muted small">(<?= count($vpThoiTrang) ?>)</span></h4>
                <?php bangVatPham($vpThoiTrang, $tenVP, 'Không có.', 'inven', (int)$ct['id']); ?>
            </div>
        </div>

        <h4 style="margin-top:18px;">Hành trang <span class="muted small">(<?= count($vpTui) ?>)</span></h4>
        <?php bangVatPham($vpTui, $tenVP, 'Túi rỗng.', 'inven', (int)$ct['id']); ?>

        <h4 style="margin-top:18px;">Rương <span class="muted small">(<?= count($vpRuong) ?>)</span></h4>
        <?php bangVatPham($vpRuong, $tenVP, 'Rương rỗng.', 'bag', (int)$ct['id']); ?>

        <p class="muted small" style="margin:14px 0 0;">
            Đá quý và nguyên liệu <em>không</em> nằm trong ba cột này nên không hiện ở đây —
            chúng ở bảng riêng.
        </p>
    </div>

<?php else: ?>

    <div class="card">
        <h3><?= $q === '' ? '30 nhân vật vào game gần nhất' : 'Kết quả cho “' . e($q) . '”' ?></h3>
        <p class="muted small" style="margin:0 0 12px;">
            Bấm <strong>tên nhân vật</strong> để mở trang chi tiết — ở đó mới có
            <strong>đồ đang mặc, thời trang, hành trang và rương</strong> của nhân vật đó.
            Bảng này chỉ liệt kê, không kèm vật phẩm.
        </p>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr>
                <th>Nhân vật</th><th class="num">Cấp</th><th class="num">Lượng</th>
                <th class="num">Xu</th><th>Vào lần cuối</th><th>Trạng thái</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($ketQua as $r): ?>
                <tr>
                    <td><a href="nhanvat.php?id=<?= (int)$r['id'] ?>"><strong><?= e($r['charname']) ?></strong></a>
                        <div class="muted small"><?= e(tenNghe($r['class'])) ?> · tài khoản #<?= (int)$r['userId'] ?></div></td>
                    <td class="num"><?= (int)$r['lastLv'] ?></td>
                    <td class="num"><?= so($r['luong']) ?></td>
                    <td class="num"><?= so($r['gold']) ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['lastLog']) ?></td>
                    <td><?= nhanNhanVat($r) ?></td>
                    <td class="nowrap"><?php nutKhoa($r); ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$ketQua): ?>
                <tr><td colspan="7" class="muted">Không có nhân vật nào khớp.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
        <?php if (count($ketQua) >= 50): ?>
            <p class="muted small" style="margin-top:10px;">Chỉ hiện 50 kết quả đầu — gõ cụ thể hơn để thu hẹp.</p>
        <?php endif; ?>
    </div>

<?php endif; ?>
<?php adminFoot(); ?>
