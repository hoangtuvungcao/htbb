<?php
/**
 * Phát quà đua top — 10 hạng, admin tự chọn nhân vật + phần thưởng cho từng hạng.
 *
 * VÌ SAO CÓ TRANG NÀY (yêu cầu người dùng 19/09/2026): trước đây "đua top" chỉ có
 * một cờ bật/tắt sự kiện (SuKien.DUA_TOP_HOA_LU), không gắn với phần thưởng nào —
 * muốn phát quà cho người thắng phải làm tay. Trang này ghi các dòng "hạng X ->
 * nhân vật Y -> phần thưởng Z" vào bảng `tob_top_thuong`; server đọc bảng này
 * MỖI LẦN người chơi mở NPC "Tổng Quản" (real/cmd/RequestNPCInfoHandler.java,
 * case -10) — CHỈ nhân vật có tên trong bảng và CHƯA nhận mới thấy dòng menu
 * "Nhận thưởng đua top" (xem real/SuKienDuaTop.java).
 *
 * BỘ CHỌN PHẦN THƯỞNG dùng LẠI đúng danh mục + cú pháp chuỗi của giftcode.php
 * (inc/thuong.php: kiemTraItem(), inc/danh_muc_thuong.php: dmDanhMuc()) — server
 * áp dụng bằng CHÍNH hàm data.DatabaseGiftCode.applyItemString() mà giftcode
 * đang dùng (tách ra từ đó, không viết lại ~60 loại phần thưởng).
 *
 * Một hạng có thể được tạo NHIỀU LẦN qua các đợt đua top khác nhau — bảng không
 * ràng buộc UNIQUE theo vi_tri, dòng của đợt trước (đã nhận hay chưa) vẫn giữ lại
 * để tra cứu lịch sử, giống cách giftcode_log giữ lại sau khi giftcode bị xoá.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/thuong.php';
require_once __DIR__ . '/inc/danh_muc_thuong.php';
requireAdmin();

const SO_HANG_DUA_TOP = 10;
const TEN_BANG_DUA_TOP = 'tob_top_thuong';
$TRANG = 'dua_top.php';

/**
 * Quà đua top CHỈ gồm những thứ "đáng công" (yêu cầu người dùng 19/09/2026) — bỏ hết
 * nhóm nguyên liệu (đá/ngọc GEM) và các nhóm không được nhắc tới. Đây là whitelist các
 * KHOÁ NHÓM trong dmDanhMuc() (inc/danh_muc_thuong.php) — nhóm nào không có tên ở đây bị
 * ẩn khỏi bộ chọn của TRANG NÀY, KHÔNG đụng gì tới danh mục dùng chung (giftcode.php vẫn
 * thấy đủ mọi nhóm như cũ).
 */
function duaTopNhomChoPhep(): array
{
    return [
        'trung_thu'    => true, // Trứng thú
        'trung_linh'   => true, // Trứng linh thú (rồng băng...)
        'trung_thuoc'  => true, // Trứng phượng hoàng / hoả kỳ lân / đương khang... (trứng "pet")
        'danh_hieu'    => true, // Danh hiệu
    ];
}

/**
 * Các loại phần thưởng KHÔNG có danh mục id (chỉ cần chọn "loại" rồi tự gõ số ngày/phút
 * trong ô đã chèn) mà trang này cho phép chèn nhanh — đúng các nhóm người dùng liệt kê:
 * thú cưỡi có ngày (phượng hoàng/rồng băng/sư tử/lân/đường khang/tuần lộc/hoả kỳ lân),
 * phi phong kháng, vũ khí thời trang, vũ khí thần binh, nguyệt linh trượng, mặt nạ.
 * Nguồn cú pháp/mô tả vẫn là loaiThuong() (inc/thuong.php) — chỉ lọc bớt danh sách hiện ra.
 */
function duaTopMaChenNhanh(): array
{
    return [
        // ---- Thú cưỡi có ngày ----
        'PHT', 'PHDST', 'PHMT', 'PHKT', 'PHTT', 'PHBT', 'PHDS',
        'RONGBANG', 'SUTU', 'SUTUBANG', 'LANT', 'DBT', 'DK', 'DKB', 'DKT', 'TL', 'HKL',
        // ---- Phi phong kháng ----
        'PPRK', 'PPVK',
        // ---- Vũ khí thời trang ----
        'VKTT', 'VKID', 'VKMAX',
        // ---- Vũ khí thần binh ----
        'VKTB', 'VKTBMAX', 'VKTBLV',
        // ---- Nguyệt linh trượng ----
        'NLT', 'NLTMAX',
        // ---- Mặt nạ ----
        'MATNA', 'MATNA2', 'MNT', 'MNC',
    ];
}

/** Tự tạo bảng nếu chưa có — cùng định nghĩa với real/SuKienDuaTop.java:taoBang(). */
function duaTopTaoBang(PDO $game): void
{
    $game->exec(
        'CREATE TABLE IF NOT EXISTS `' . TEN_BANG_DUA_TOP . '` ('
        . '`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,'
        . '`vi_tri` TINYINT NOT NULL,'
        . '`charname` VARCHAR(50) NOT NULL,'
        . '`item` TEXT NULL,'
        . '`da_nhan` TINYINT NOT NULL DEFAULT 0,'
        . '`admin_user` VARCHAR(50) NOT NULL DEFAULT \'\','
        . '`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,'
        . '`nhan_luc` DATETIME NULL,'
        . 'PRIMARY KEY (`id`),'
        . 'KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
    );
}

/** In cột "Phần thưởng": chuỗi thô + bản dịch dễ đọc — giống oPhanThuongChiTiet() của giftcode.php. */
function duaTopPhanThuongChiTiet(string $chuoi): void
{
    $chuoi = trim($chuoi);
    if ($chuoi === '') {
        echo '<span class="muted">—</span>';
        return;
    }
    echo '<code>' . e($chuoi) . '</code>';
    try {
        $moTa = dmMoTaChuoiThuong(dbGame(), $chuoi);
    } catch (Throwable $ex) {
        $moTa = [];
    }
    if ($moTa) {
        echo '<ul class="chi-tiet-thuong">';
        foreach ($moTa as $dong) {
            echo '<li>' . e($dong) . '</li>';
        }
        echo '</ul>';
    }
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);
    $viec = p('viec');
    try {
        duaTopTaoBang(dbGame());
        switch ($viec) {

            case 'luu_dot': {
                $lam = [];
                $loi = [];
                for ($hang = 1; $hang <= SO_HANG_DUA_TOP; $hang++) {
                    $ten = trim(p('ten' . $hang));
                    if ($ten === '') {
                        continue; // hang nay bo trong - khong tao
                    }
                    if (!preg_match('/^[\p{L}0-9 _]{2,50}$/u', $ten)) {
                        $loi[] = 'Hạng ' . $hang . ': tên nhân vật không hợp lệ ("' . $ten . '").';
                        continue;
                    }
                    $ck = dbGame()->prepare('SELECT id FROM tob_char WHERE charname = ? AND del = 1 LIMIT 1');
                    $ck->execute([$ten]);
                    if (!$ck->fetchColumn()) {
                        $loi[] = 'Hạng ' . $hang . ': không tìm thấy nhân vật "' . $ten . '" đang hoạt động.';
                        continue;
                    }
                    [$item, $loiItem] = kiemTraItem(p('item' . $hang));
                    if ($loiItem) {
                        $loi[] = 'Hạng ' . $hang . ' (' . $ten . '): ' . implode(' | ', $loiItem);
                        continue;
                    }
                    if ($item === '') {
                        $loi[] = 'Hạng ' . $hang . ' (' . $ten . '): chưa chọn phần thưởng nào.';
                        continue;
                    }
                    $lam[] = [$hang, $ten, $item];
                }
                if ($loi) {
                    veLai($TRANG, 'error', 'Chưa lưu gì cả, sửa lỗi rồi lưu lại: ' . implode(' ', $loi));
                }
                if (!$lam) {
                    veLai($TRANG, 'error', 'Chưa điền hạng nào (cần tên nhân vật + phần thưởng).');
                }
                $ins = dbGame()->prepare(
                    'INSERT INTO `' . TEN_BANG_DUA_TOP . '` (vi_tri, charname, item, admin_user) VALUES (?, ?, ?, ?)'
                );
                foreach ($lam as [$hang, $ten, $item]) {
                    $ins->execute([$hang, $ten, $item, adminName()]);
                    ghiNhatKy('tao_thuong_duatop', $ten, 'hạng ' . $hang . ': ' . $item);
                }
                veLai($TRANG, 'success', 'Đã tạo phần thưởng cho ' . count($lam) . ' hạng. Nhân vật tương ứng sẽ '
                    . 'thấy mục "Nhận thưởng đua top" ở NPC Tổng Quản.');
                break;
            }

            case 'xoa': {
                $id = (int)p('id');
                $st = dbGame()->prepare('SELECT vi_tri, charname, da_nhan FROM `' . TEN_BANG_DUA_TOP . '` WHERE id = ?');
                $st->execute([$id]);
                $row = $st->fetch();
                if (!$row) {
                    veLai($TRANG, 'error', 'Không tìm thấy dòng #' . $id . '.');
                }
                if ((int)$row['da_nhan'] !== 0) {
                    veLai($TRANG, 'error', 'Dòng này đã được nhận, không huỷ được.');
                }
                dbGame()->prepare('DELETE FROM `' . TEN_BANG_DUA_TOP . '` WHERE id = ? AND da_nhan = 0')->execute([$id]);
                ghiNhatKy('xoa_thuong_duatop', (string)$row['charname'], 'huỷ hạng ' . $row['vi_tri'] . ' (chưa nhận)');
                veLai($TRANG, 'success', 'Đã huỷ phần thưởng hạng ' . $row['vi_tri'] . ' của ' . $row['charname'] . '.');
                break;
            }

            default:
                veLai($TRANG, 'error', 'Không hiểu thao tác.');
        }
    } catch (Throwable $ex) {
        veLai($TRANG, 'error', 'Lỗi: ' . $ex->getMessage());
    }
    exit;
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu
// ---------------------------------------------------------------------------
$dangCho = [];
$daNhan = [];
$loiDoc = '';
try {
    duaTopTaoBang(dbGame());
    $dangCho = dbGame()->query(
        'SELECT * FROM `' . TEN_BANG_DUA_TOP . '` WHERE da_nhan = 0 ORDER BY vi_tri, id'
    )->fetchAll();
    $daNhan = dbGame()->query(
        'SELECT * FROM `' . TEN_BANG_DUA_TOP . '` WHERE da_nhan = 1 ORDER BY nhan_luc DESC, id DESC LIMIT 100'
    )->fetchAll();
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

$dmuc = array_intersect_key(dmDanhMuc(dbGame()), duaTopNhomChoPhep());

$maChenNhanh = array_intersect_key(loaiThuong(), array_flip(duaTopMaChenNhanh()));

adminHead('dua_top', 'Đua top');
?>
<div class="page-head">
    <div>
        <h1>Phát quà đua top</h1>
        <p><?= SO_HANG_DUA_TOP ?> hạng. Điền tên nhân vật + chọn phần thưởng cho hạng nào cần phát — để trống hạng
           không dùng. Nhân vật được điền tên sẽ thấy mục "Nhận thưởng đua top" ở NPC Tổng Quản, chỉ đúng người đó
           mới nhận được đúng phần thưởng của mình.</p>
    </div>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<div class="card">
    <h3>Tạo đợt phát quà mới</h3>

    <?php if ($dmuc): ?>
    <div class="bochon" id="boChon">
        <div class="bochon-dau">
            <strong>Chọn vật phẩm</strong>
            <span class="muted small">chọn hạng muốn điền ở ô bên dưới rồi chọn vật phẩm, bấm Thêm</span>
        </div>
        <div class="bochon-hang">
            <label class="o"><span>Điền vào hạng</span>
                <select id="bcHang">
                    <?php for ($h = 1; $h <= SO_HANG_DUA_TOP; $h++): ?>
                        <option value="<?= $h ?>">Hạng <?= $h ?></option>
                    <?php endfor; ?>
                </select>
            </label>
            <label class="o"><span>Nhóm</span>
                <select id="bcNhom">
                    <?php foreach ($dmuc as $ma => $n): ?>
                        <option value="<?= e($ma) ?>"><?= e($n['ten']) ?></option>
                    <?php endforeach; ?>
                </select>
            </label>
            <label class="o"><span>Tìm theo tên hoặc id</span>
                <input type="text" id="bcTim" placeholder="ví dụ: trứng, cấp 6, 848">
            </label>
        </div>
        <div class="bochon-hang">
            <label class="o bochon-rong"><span>Vật phẩm <span class="muted small" id="bcDem"></span></span>
                <select id="bcMuc" size="8"></select>
            </label>
            <div class="bochon-so" id="bcTruong"></div>
        </div>
        <div class="bochon-chan">
            <button type="button" class="btn btn-nho btn-primary" id="bcThem">Thêm vào phần thưởng của hạng đã chọn</button>
            <code class="bochon-xem" id="bcXem">—</code>
        </div>
        <p class="muted small" id="bcGhiChu"></p>
    </div>
    <?php endif; ?>

    <?php if ($maChenNhanh): ?>
    <div class="bochon" id="chenNhanh">
        <div class="bochon-dau">
            <strong>Chèn nhanh — thú cưỡi, phi phong kháng, vũ khí thời trang/thần binh, nguyệt linh trượng, mặt nạ</strong>
            <span class="muted small">không có danh sách id để chọn, bấm Chèn rồi tự sửa số ngày/phút trong ô của hạng đang chọn ở "Điền vào hạng" bên trên</span>
        </div>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Cú pháp</th><th>Là gì</th><th>Mẫu chèn</th><th></th></tr></thead>
            <tbody>
            <?php foreach ($maChenNhanh as $loai => [$n, $cuPhap, $mota, $viDu]): ?>
                <tr>
                    <td class="nowrap"><code><?= e($cuPhap) ?></code></td>
                    <td class="muted small"><?= e($mota) ?></td>
                    <td class="nowrap"><code><?= e($viDu) ?></code></td>
                    <td class="nowrap"><button type="button" class="btn btn-xam btn-nho oChenVd" data-vd="<?= e($viDu) ?>">Chèn</button></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    </div>
    <script>
    // Chen mau co san vao dung o textarea cua hang dang chon o "#bcHang" — dung LAI
    // dung cach xac dinh dich (oPTDich) voi bo chon vat pham o tren, khong phai chot
    // an toan rieng: tat JavaScript thi chi mat nut tien loi nay, van go tay duoc.
    (function () {
        var oHang = document.getElementById('bcHang');
        if (!oHang) { return; }
        document.querySelectorAll('.oChenVd').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var s = btn.getAttribute('data-vd') || '';
                if (s === '') { return; }
                var oPT = document.getElementById('item' + oHang.value);
                if (!oPT) { return; }
                var cu = (oPT.value || '').trim().replace(/,+\s*$/, '');
                oPT.value = (cu === '') ? s : (cu + ',' + s);
                oPT.focus();
            });
        });
    })();
    </script>
    <?php endif; ?>

    <p class="muted small">Xem đầy đủ cú pháp gõ tay (61 loại) tại trang <a href="giftcode.php">Giftcode</a>.</p>

    <form method="post" action="dua_top.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_dot">

        <div class="tbl-wrap" style="margin-top:12px;">
        <table class="tbl">
            <thead><tr><th class="num">Hạng</th><th>Tên nhân vật</th><th>Phần thưởng</th></tr></thead>
            <tbody>
            <?php for ($h = 1; $h <= SO_HANG_DUA_TOP; $h++): ?>
                <tr>
                    <td class="num"><strong>Top <?= $h ?></strong></td>
                    <td><input type="text" name="ten<?= $h ?>" maxlength="50" placeholder="tên nhân vật"></td>
                    <td><textarea name="item<?= $h ?>" id="item<?= $h ?>" rows="2"
                                  placeholder="ITEM:848:1000,GEM:73:100:-1" onfocus="document.getElementById('bcHang').value='<?= $h ?>'"></textarea></td>
                </tr>
            <?php endfor; ?>
            </tbody>
        </table>
        </div>

        <div class="form-actions">
            <button class="btn btn-primary btn-nho">Lưu đợt phát quà</button>
        </div>
    </form>

    <?php if ($dmuc): ?>
    <script>
    /*
     * Bộ chọn vật phẩm cho 10 hạng đua top — cùng cơ chế với giftcode.php (chỉ
     * GHÉP CHUỖI rồi chèn vào ô textarea của hạng đang chọn ở "Điền vào hạng"),
     * khác đúng một chỗ: giftcode.php chỉ có MỘT ô đích cố định, ở đây có 10 ô
     * nên thêm ô chọn "Điền vào hạng" làm đích — mỗi ô textarea cũng tự đưa hạng
     * của nó lên làm đích khi được bấm vào (onfocus ở trên), để khỏi phải đổi ô
     * chọn bằng tay nếu chỉ đang gõ/xem lại một hạng có sẵn.
     *
     * Chuỗi vẫn đi qua kiemTraItem() phía máy chủ y như khi gõ tay khi lưu.
     */
    (function () {
        var DM = <?= json_encode($dmuc, JSON_UNESCAPED_UNICODE | JSON_HEX_TAG
            | JSON_HEX_AMP | JSON_HEX_APOS | JSON_HEX_QUOT) ?>;
        var TRUONG = <?php
            $tr = [];
            foreach ($dmuc as $n) {
                $tr[$n['loai']] = dmTruongCuaLoai($n['loai']);
            }
            echo json_encode($tr, JSON_UNESCAPED_UNICODE | JSON_HEX_TAG
                | JSON_HEX_AMP | JSON_HEX_APOS | JSON_HEX_QUOT);
        ?>;
        var oHang   = document.getElementById('bcHang');
        var oNhom   = document.getElementById('bcNhom');
        var oTim    = document.getElementById('bcTim');
        var oMuc    = document.getElementById('bcMuc');
        var oTruong = document.getElementById('bcTruong');
        var oDem    = document.getElementById('bcDem');
        var oXem    = document.getElementById('bcXem');
        var oGhiChu = document.getElementById('bcGhiChu');
        var oThem   = document.getElementById('bcThem');
        if (!oNhom || !oMuc || !oHang) { return; }

        function oPTDich() { return document.getElementById('item' + oHang.value); }

        function nhomHienTai() { return DM[oNhom.value] || null; }

        function veDanhSach() {
            var n = nhomHienTai();
            oMuc.innerHTML = '';
            if (!n) { return; }
            var tu = (oTim.value || '').trim().toLowerCase();
            var dem = 0;
            for (var id in n.muc) {
                if (!Object.prototype.hasOwnProperty.call(n.muc, id)) { continue; }
                var ten = n.muc[id];
                if (tu !== '' && ten.toLowerCase().indexOf(tu) === -1
                    && id.indexOf(tu) === -1) { continue; }
                var o = document.createElement('option');
                o.value = id;
                o.textContent = id + ' — ' + ten;
                oMuc.appendChild(o);
                dem++;
            }
            if (dem > 0) { oMuc.selectedIndex = 0; }
            oDem.textContent = tu === '' ? ('(' + dem + ')') : ('(' + dem + ' khớp “' + tu + '”)');
            if (dem === 0) {
                var t = document.createElement('option');
                t.textContent = 'không có món nào khớp';
                t.disabled = true;
                oMuc.appendChild(t);
            }
        }

        function veTruong() {
            var n = nhomHienTai();
            oTruong.innerHTML = '';
            if (!n) { return; }
            (TRUONG[n.loai] || []).forEach(function (t) {
                if (t.an) {
                    var an = document.createElement('input');
                    an.type = 'hidden';
                    an.value = t.macDinh;
                    an.setAttribute('data-ma', t.ma);
                    oTruong.appendChild(an);
                    return;
                }
                var nhan = document.createElement('label');
                nhan.className = 'o';
                var sp = document.createElement('span');
                sp.textContent = t.nhan;
                nhan.appendChild(sp);
                var o;
                if (t.chon) {
                    o = document.createElement('select');
                    for (var gt in t.chon) {
                        if (!Object.prototype.hasOwnProperty.call(t.chon, gt)) { continue; }
                        var op = document.createElement('option');
                        op.value = gt;
                        op.textContent = t.chon[gt];
                        o.appendChild(op);
                    }
                } else {
                    o = document.createElement('input');
                    o.type = 'number';
                    o.min = (t.minSo !== undefined && t.minSo !== null) ? t.minSo : '1';
                }
                o.value = t.macDinh;
                o.setAttribute('data-ma', t.ma);
                o.addEventListener('change', veXem);
                o.addEventListener('input', veXem);
                nhan.appendChild(o);
                oTruong.appendChild(nhan);
            });
        }

        function chuoi() {
            var n = nhomHienTai();
            if (!n || !oMuc.value) { return ''; }
            var phan = [n.loai, oMuc.value];
            var o = oTruong.querySelectorAll('[data-ma]');
            for (var i = 0; i < o.length; i++) {
                var v = (o[i].value || '').trim();
                if (v === '') { return ''; }
                phan.push(v);
            }
            return phan.join(':');
        }

        function veXem() {
            var s = chuoi();
            oXem.textContent = s === '' ? '—' : s;
            oThem.disabled = (s === '');
        }

        function doiNhom() {
            var n = nhomHienTai();
            oGhiChu.textContent = n ? (n.ghiChu || '') : '';
            veTruong();
            veDanhSach();
            veXem();
        }

        oThem.addEventListener('click', function () {
            var s = chuoi();
            if (s === '') { return; }
            var oPT = oPTDich();
            if (!oPT) { return; }
            var cu = (oPT.value || '').trim().replace(/,+\s*$/, '');
            oPT.value = (cu === '') ? s : (cu + ',' + s);
            oPT.focus();
        });

        oNhom.addEventListener('change', doiNhom);
        oTim.addEventListener('input', function () { veDanhSach(); veXem(); });
        oMuc.addEventListener('change', veXem);
        oHang.addEventListener('change', veXem);
        oTim.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); oMuc.focus(); }
        });

        doiNhom();
    })();
    </script>
    <?php endif; ?>
</div>

<div class="card">
    <h3>Đang chờ nhận (<?= count($dangCho) ?>)</h3>
    <div class="tbl-wrap">
    <table class="tbl">
        <thead><tr>
            <th class="num">Hạng</th><th>Nhân vật</th><th>Phần thưởng</th>
            <th>Người tạo</th><th class="num">Tạo lúc</th><th></th>
        </tr></thead>
        <tbody>
        <?php foreach ($dangCho as $r): ?>
            <tr>
                <td class="num"><strong>Top <?= (int)$r['vi_tri'] ?></strong></td>
                <td><a href="nhanvat.php?q=<?= e(urlencode((string)$r['charname'])) ?>"><?= e((string)$r['charname']) ?></a></td>
                <td class="info"><?php duaTopPhanThuongChiTiet((string)($r['item'] ?? '')); ?></td>
                <td><?= e((string)$r['admin_user']) ?></td>
                <td class="num nowrap"><?= e((string)$r['tao_luc']) ?></td>
                <td class="nowrap">
                    <form method="post" action="dua_top.php" class="inline"
                          onsubmit="return confirm('Huỷ phần thưởng hạng <?= (int)$r['vi_tri'] ?> của <?= e((string)$r['charname']) ?>?');">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="viec" value="xoa">
                        <input type="hidden" name="id" value="<?= (int)$r['id'] ?>">
                        <button class="btn btn-do btn-nho">Huỷ</button>
                    </form>
                </td>
            </tr>
        <?php endforeach; ?>
        <?php if (!$dangCho): ?>
            <tr><td colspan="6" class="muted">Không có ai đang chờ nhận.</td></tr>
        <?php endif; ?>
        </tbody>
    </table>
    </div>
</div>

<div class="card">
    <h3>Đã nhận (mới nhất 100 dòng)</h3>
    <div class="tbl-wrap">
    <table class="tbl">
        <thead><tr>
            <th class="num">Hạng</th><th>Nhân vật</th><th>Phần thưởng</th><th class="num">Nhận lúc</th>
        </tr></thead>
        <tbody>
        <?php foreach ($daNhan as $r): ?>
            <tr>
                <td class="num">Top <?= (int)$r['vi_tri'] ?></td>
                <td><a href="nhanvat.php?q=<?= e(urlencode((string)$r['charname'])) ?>"><?= e((string)$r['charname']) ?></a></td>
                <td class="info"><?php duaTopPhanThuongChiTiet((string)($r['item'] ?? '')); ?></td>
                <td class="num nowrap"><?= e((string)$r['nhan_luc']) ?></td>
            </tr>
        <?php endforeach; ?>
        <?php if (!$daNhan): ?>
            <tr><td colspan="4" class="muted">Chưa ai nhận thưởng.</td></tr>
        <?php endif; ?>
        </tbody>
    </table>
    </div>
</div>
<?php adminFoot(); ?>
