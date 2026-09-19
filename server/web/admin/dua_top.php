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
        'thoi_trang'   => true, // Áo/mặt nạ/nón/chổi thời trang - có danh sách id chọn thật
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
        // ---- Vũ khí thời trang ----
        'VKTT', 'VKID', 'VKMAX',
        // ---- Vũ khí thần binh ----
        'VKTB', 'VKTBMAX', 'VKTBLV',
        // ---- Nguyệt linh trượng ----
        'NLT', 'NLTMAX',
        // ---- Mặt nạ theo hạn (khác 5 mặt ID_MAT_NA đã có danh sách chọn riêng) ----
        'MATNA2', 'MNT', 'MNC',
    ];
}

/**
 * "Mặt nạ" (MATNA) và "Phi phong kháng" (PPRK/PPVK) không có trong dmDanhMuc() dùng
 * chung (xem inc/danh_muc_thuong.php) vì mỗi loại chỉ có ĐÚNG MỘT lựa chọn ý nghĩa
 * (5 mặt nạ cố định Char.ID_MAT_NA, hoặc riêng id=750 cho kháng - xem loaiThuong()).
 * Ghép tay 2 nhóm này vào bộ chọn của TRANG NÀY để admin chọn qua dropdown thay vì
 * gõ tay - dmTruongCuaLoai() đã có sẵn field cho 'PPRK'/'PPVK'/'MATNA'.
 */
function duaTopNhomTuVe(): array
{
    return [
        'mat_na' => [
            'ten'    => 'Mặt nạ',
            'loai'   => 'MATNAID',
            'ghiChu' => 'Chọn ĐÚNG mẫu mặt nạ ở đây, rồi chọn RIÊNG hiệu ứng (option) ở ô bên dưới — '
                      . 'hai thứ độc lập nhau (khác MATNA/MATNA2 vốn ép chung một chỉ số).',
            'muc'    => [
                '745' => 'Mặt nạ bí ngô',
                '746' => 'Mặt nạ ngạ quỷ',
                '747' => 'Mặt nạ ngạ khuyển',
                '748' => 'Mặt nạ thi khiển',
                '749' => 'Mặt nạ dạ xoa',
            ],
        ],
        'pp_khang_ngau_nhien' => [
            'ten'    => 'Phi phong kháng — chỉ số ngẫu nhiên',
            'loai'   => 'PPRK',
            'ghiChu' => 'Chỉ id=750 (Phi Phong Huyền Vũ) mới thật sự áp dụng được tham số Kháng.',
            'muc'    => ['750' => 'Phi Phong Huyền Vũ'],
        ],
        'pp_khang_max' => [
            'ten'    => 'Phi phong kháng — chỉ số max',
            'loai'   => 'PPVK',
            'ghiChu' => 'Chỉ id=750 (Phi Phong Huyền Vũ) mới thật sự áp dụng được tham số Kháng.',
            'muc'    => ['750' => 'Phi Phong Huyền Vũ'],
        ],
    ];
}

/**
 * Tự tạo bảng nếu chưa có — cùng định nghĩa với real/SuKienDuaTop.java:taoBang().
 * Tự thêm cột `item_chon` (19/09/2026) nếu bảng đã tạo từ trước khi có cột này.
 */
function duaTopTaoBang(PDO $game): void
{
    $game->exec(
        'CREATE TABLE IF NOT EXISTS `' . TEN_BANG_DUA_TOP . '` ('
        . '`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,'
        . '`vi_tri` TINYINT NOT NULL,'
        . '`charname` VARCHAR(50) NOT NULL,'
        . '`item` TEXT NULL,'
        . '`item_chon` TEXT NULL,'
        . '`da_nhan` TINYINT NOT NULL DEFAULT 0,'
        . '`admin_user` VARCHAR(50) NOT NULL DEFAULT \'\','
        . '`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,'
        . '`nhan_luc` DATETIME NULL,'
        . 'PRIMARY KEY (`id`),'
        . 'KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
    );
    $co = $game->query(
        'SELECT COUNT(*) FROM information_schema.COLUMNS'
        . " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" . TEN_BANG_DUA_TOP . "' AND COLUMN_NAME = 'item_chon'"
    )->fetchColumn();
    if (!$co) {
        $game->exec('ALTER TABLE `' . TEN_BANG_DUA_TOP . '` ADD COLUMN `item_chon` TEXT NULL AFTER `item`');
    }
}

/**
 * Kiểm tra + chuẩn hoá chuỗi "cho người chơi tự chọn": "nhãn1=chuỗi1|nhãn2=chuỗi2|...".
 * Mỗi đoạn là MỘT lựa chọn hiện ra trong menu NPC — "chuỗi" theo đúng cú pháp phần
 * thưởng bình thường (đã qua kiemTraItem()), "nhãn" là tên hiển thị cho người chơi
 * chọn. Trả về [chuoiDaChuan, mangLoi] giống kiểu trả về của kiemTraItem().
 */
function duaTopKiemTraItemChon(string $raw): array
{
    $raw = trim($raw);
    if ($raw === '') {
        return ['', []];
    }
    $loi = [];
    $phan = [];
    foreach (explode('|', $raw) as $doan) {
        $doan = trim($doan);
        if ($doan === '') {
            continue;
        }
        $vt = strpos($doan, '=');
        if ($vt === false || $vt === 0 || $vt === strlen($doan) - 1) {
            $loi[] = '"' . $doan . '": phải đúng dạng "nhãn=phần thưởng" (có dấu "=").';
            continue;
        }
        $nhan = trim(substr($doan, 0, $vt));
        $chuoiTho = trim(substr($doan, $vt + 1));
        if ($nhan === '' || strpbrk($nhan, '|=') !== false) {
            $loi[] = '"' . $doan . '": nhãn không hợp lệ (không được rỗng hoặc chứa "|"/"=").';
            continue;
        }
        [$chuoi, $loiItem] = kiemTraItem($chuoiTho);
        if ($loiItem) {
            $loi[] = '"' . $nhan . '": ' . implode(' | ', $loiItem);
            continue;
        }
        if ($chuoi === '' || strpos($chuoi, ',') !== false) {
            // Moi lua chon la MOT phan thuong duy nhat - tranh nham lan dau phay (ngan
            // cach nhieu phan thuong trong "item" thuong) voi dau "|" (ngan cach cac
            // LUA CHON) khi doc lai.
            $loi[] = '"' . $nhan . '": mỗi lựa chọn chỉ được đúng MỘT phần thưởng (không dùng dấu phẩy).';
            continue;
        }
        $phan[] = $nhan . '=' . $chuoi;
    }
    return [implode('|', $phan), $loi];
}

/** In các lựa chọn "cho người chơi tự chọn" (cột item_chon) dưới dạng danh sách nhãn + chuỗi thô. */
function duaTopLuaChonChiTiet(string $raw): void
{
    $raw = trim($raw);
    if ($raw === '') {
        return;
    }
    // Gom theo "ten nhom::nhan con" - GIONG HET cach doc cua real/SuKienDuaTop.java
    // (phanCapItemChon): nhom co >1 nhan con la menu 2 buoc that su (chon nhom truoc,
    // roi moi chon chi so), nhom co dung 1 la ap dung thang khi chon (khong hoi them).
    $nhom = [];
    foreach (explode('|', $raw) as $doan) {
        $doan = trim($doan);
        if ($doan === '') {
            continue;
        }
        $vt = strpos($doan, '=');
        if ($vt === false) {
            $nhom[$doan][] = ['', $doan];
            continue;
        }
        $nhanGoc = substr($doan, 0, $vt);
        $chuoi = substr($doan, $vt + 1);
        $vt2 = strpos($nhanGoc, '::');
        $tenNhom = $vt2 !== false ? substr($nhanGoc, 0, $vt2) : $nhanGoc;
        $nhanCon = $vt2 !== false ? substr($nhanGoc, $vt2 + 2) : $nhanGoc;
        $nhom[$tenNhom][] = [$nhanCon, $chuoi];
    }
    echo '<div class="muted small" style="margin-top:6px;"><strong>Người chơi tự chọn:</strong><ul class="chi-tiet-thuong">';
    foreach ($nhom as $tenNhom => $con) {
        if (count($con) === 1) {
            echo '<li>' . e($tenNhom) . ' — <code>' . e($con[0][1]) . '</code></li>';
            continue;
        }
        echo '<li>' . e($tenNhom) . ' (chọn tiếp 1 trong ' . count($con) . '):<ul class="chi-tiet-thuong">';
        foreach ($con as [$nhanCon, $chuoi]) {
            echo '<li>' . e($nhanCon) . ' — <code>' . e($chuoi) . '</code></li>';
        }
        echo '</ul></li>';
    }
    echo '</ul></div>';
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
                    [$itemChon, $loiChon] = duaTopKiemTraItemChon(p('itemchon' . $hang));
                    if ($loiChon) {
                        $loi[] = 'Hạng ' . $hang . ' (' . $ten . ', tự chọn): ' . implode(' | ', $loiChon);
                        continue;
                    }
                    if ($item === '' && $itemChon === '') {
                        $loi[] = 'Hạng ' . $hang . ' (' . $ten . '): chưa chọn phần thưởng nào.';
                        continue;
                    }
                    $lam[] = [$hang, $ten, $item, $itemChon];
                }
                if ($loi) {
                    veLai($TRANG, 'error', 'Chưa lưu gì cả, sửa lỗi rồi lưu lại: ' . implode(' ', $loi));
                }
                if (!$lam) {
                    veLai($TRANG, 'error', 'Chưa điền hạng nào (cần tên nhân vật + phần thưởng).');
                }
                $ins = dbGame()->prepare(
                    'INSERT INTO `' . TEN_BANG_DUA_TOP . '` (vi_tri, charname, item, item_chon, admin_user) VALUES (?, ?, ?, ?, ?)'
                );
                foreach ($lam as [$hang, $ten, $item, $itemChon]) {
                    $ins->execute([$hang, $ten, $item, $itemChon, adminName()]);
                    ghiNhatKy('tao_thuong_duatop', $ten, 'hạng ' . $hang . ': ' . $item
                        . ($itemChon !== '' ? ' | tự chọn: ' . $itemChon : ''));
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

$dmuc = array_intersect_key(dmDanhMuc(dbGame()), duaTopNhomChoPhep()) + duaTopNhomTuVe();

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
            <button type="button" class="btn btn-nho btn-xam" id="bcThemChon">Thêm vào "cho người chơi tự chọn"</button>
            <button type="button" class="btn btn-nho btn-xam" id="bcThemCaNhom">Thêm CẢ NHÓM vào "cho người chơi tự chọn"</button>
            <code class="bochon-xem" id="bcXem">—</code>
        </div>
        <p class="muted small" id="bcGhiChu"></p>
        <p class="muted small">"Cho người chơi tự chọn": nhân vật bấm "Nhận thưởng đua top" ở NPC Tổng Quản sẽ
           thấy một menu để TỰ CHỌN 1 trong các món đã thêm vào đây, thay vì admin chọn sẵn một món cố định.
           Nút "Thêm CẢ NHÓM" đưa HẾT các tổ hợp có thể có của nhóm đang chọn thành các lựa chọn trong một lần
           bấm — không chỉ duyệt hết các món (vd 5 mặt nạ), mà còn duyệt hết CẢ các ô "Hệ"/"Kháng"... đang là
           dropdown nhiều lựa chọn (vd Phi phong kháng ra đủ 2 Hệ × 6 Kháng = 12 lựa chọn); ô số lượng/thời hạn
           thì giữ nguyên giá trị đang đặt cho mọi lựa chọn.</p>
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
            <thead><tr>
                <th class="num">Hạng</th><th>Tên nhân vật</th><th>Phần thưởng cố định</th>
                <th>Cho người chơi tự chọn (tuỳ chọn)</th>
            </tr></thead>
            <tbody>
            <?php for ($h = 1; $h <= SO_HANG_DUA_TOP; $h++): ?>
                <tr>
                    <td class="num"><strong>Top <?= $h ?></strong></td>
                    <td><input type="text" name="ten<?= $h ?>" maxlength="50" placeholder="tên nhân vật"></td>
                    <td><textarea name="item<?= $h ?>" id="item<?= $h ?>" rows="2"
                                  placeholder="ITEM:848:1000,GEM:73:100:-1" onfocus="document.getElementById('bcHang').value='<?= $h ?>'"></textarea></td>
                    <td><textarea name="itemchon<?= $h ?>" id="itemchon<?= $h ?>" rows="2"
                                  placeholder="Mặt nạ bí ngô=MATNA:0:_:0|Mặt nạ dạ xoa=MATNA:4:_:0"
                                  onfocus="document.getElementById('bcHang').value='<?= $h ?>'"></textarea></td>
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
        var oThemChon    = document.getElementById('bcThemChon');
        var oThemCaNhom  = document.getElementById('bcThemCaNhom');
        if (!oNhom || !oMuc || !oHang) { return; }

        function oPTDich() { return document.getElementById('item' + oHang.value); }
        function oPTChonDich() { return document.getElementById('itemchon' + oHang.value); }

        /* Bo dau "|" / "=" trong nhan de khong pha dinh dang "nhan=chuoi|nhan=chuoi". */
        function lamSachNhan(s) { return String(s || '').replace(/[|=]/g, ' ').trim(); }

        function nhanHienTai() {
            var n = nhomHienTai();
            if (!n || !oMuc.value) { return ''; }
            return lamSachNhan(n.muc[oMuc.value] || oMuc.value);
        }

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

        /* "Cho nguoi choi tu chon": them DUNG mon dang chon vao o itemchonN, dang
           "nhan=chuoi", noi cach nhau bang "|" - moi doan la MOT lua chon trong menu
           NPC luc nguoi choi nhan thuong (xem real/SuKienDuaTop.doChonThuong). */
        if (oThemChon) {
            oThemChon.addEventListener('click', function () {
                var s = chuoi();
                if (s === '') { return; }
                var oPT = oPTChonDich();
                if (!oPT) { return; }
                var doan = nhanHienTai() + '=' + s;
                var cu = (oPT.value || '').trim().replace(/\|+\s*$/, '');
                oPT.value = (cu === '') ? doan : (cu + '|' + doan);
                oPT.focus();
            });
        }

        /* Them HET to hop co the co lam cac lua chon cho nguoi choi:
             - "chieu" dau tien la id (vat pham dang chon trong nhom, vd 5 mat na, hoac
               chi 1 id nhu Phi Phong Huyen Vu) - LUON duyet het.
             - moi truong (Truong) sau do: neu la o "chon" (dropdown) co NHIEU HON 1
               gia tri (vd "Kháng" 6 lua chon, "Hệ" 2 lua chon) thi CUNG duyet het -
               day chinh la cho truoc con thieu: nguoi choi se duoc tu chon ca Kháng
               lan Hệ thay vi bi ap san 1 gia tri. O con lai (so luong/thoi han, hoac
               chon chi co 1 gia tri) giu NGUYEN gia tri dang dat trong bo chon.
           Ket qua la TICH DESCARTES cua tat ca cac chieu - vd Phi phong kháng: 1 id
           x 2 hệ x 6 kháng = 12 lua chon trong 1 lan bam. */
        if (oThemCaNhom) {
            oThemCaNhom.addEventListener('click', function () {
                var n = nhomHienTai();
                if (!n) { return; }
                var oPT = oPTChonDich();
                if (!oPT) { return; }

                var idList = [], idNhan = [];
                for (var id in n.muc) {
                    if (!Object.prototype.hasOwnProperty.call(n.muc, id)) { continue; }
                    idList.push(id);
                    idNhan.push(n.muc[id]);
                }
                if (!idList.length) { return; }
                var chieu = [{giaTri: idList, nhan: idNhan}];

                var truongDefs = TRUONG[n.loai] || [];
                for (var i = 0; i < truongDefs.length; i++) {
                    var t = truongDefs[i];
                    if (t.an) {
                        chieu.push({giaTri: [t.macDinh], nhan: [null]});
                        continue;
                    }
                    // "khongCheo" (vd o "Khoá"): KHONG duoc coi la thu de nguoi choi tu
                    // chon du la dropdown nhieu gia tri - khoa hay khong la admin dat san
                    // cho ca phan thuong, khong hien ra cho nguoi choi (yeu cau nguoi dung
                    // 19/09/2026). Doc dung gia tri dang chon, giu nguyen nhu o so/an.
                    if (t.chon && !t.khongCheo && Object.keys(t.chon).length > 1) {
                        var gts = Object.keys(t.chon);
                        chieu.push({giaTri: gts, nhan: gts.map(function (gt) { return t.chon[gt]; })});
                        continue;
                    }
                    var oHienTai = oTruong.querySelector('[data-ma="' + t.ma + '"]');
                    var v = oHienTai ? (oHienTai.value || '').trim() : '';
                    if (v === '') { return; } // chua dien du o, khong lam gi
                    chieu.push({giaTri: [v], nhan: [null]});
                }

                var toHop = [{giaTri: [], nhan: []}];
                chieu.forEach(function (c) {
                    var moi = [];
                    toHop.forEach(function (truoc) {
                        for (var k = 0; k < c.giaTri.length; k++) {
                            moi.push({
                                giaTri: truoc.giaTri.concat([c.giaTri[k]]),
                                nhan: truoc.nhan.concat([c.nhan[k]])
                            });
                        }
                    });
                    toHop = moi;
                });

                var doans = toHop.map(function (h) {
                    // Nhan dau tien (id/vat pham, vd "Mat na bi ngo") la NHOM cap 1; cac nhan
                    // con lai (tu truong nhieu lua chon, vd "Kháng") gop thanh nhan con cap 2,
                    // noi bang "::" - server (SuKienDuaTop) tach theo dau nay de hien MENU 2
                    // BUOC: chon nhom (vd chon loai mat na) TRUOC, roi moi chon dong chi so
                    // trong nhom do (yeu cau nguoi dung 19/09/2026). Chi 1 nhan (khong co gi
                    // de chon them, vd Phi phong kháng chi 1 id) thi khong can noi "::".
                    var nhomLabel = lamSachNhan(h.nhan[0] || h.giaTri[0]);
                    var conLai = h.nhan.slice(1).filter(function (x) { return x; }).map(lamSachNhan);
                    var tenGop = conLai.length ? (nhomLabel + '::' + conLai.join(' - ')) : nhomLabel;
                    return tenGop + '=' + [n.loai].concat(h.giaTri).join(':');
                });
                var cu = (oPT.value || '').trim().replace(/\|+\s*$/, '');
                oPT.value = (cu === '') ? doans.join('|') : (cu + '|' + doans.join('|'));
                oPT.focus();
            });
        }

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
                <td class="info">
                    <?php duaTopPhanThuongChiTiet((string)($r['item'] ?? '')); ?>
                    <?php duaTopLuaChonChiTiet((string)($r['item_chon'] ?? '')); ?>
                </td>
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
                <td class="info">
                    <?php duaTopPhanThuongChiTiet((string)($r['item'] ?? '')); ?>
                    <?php duaTopLuaChonChiTiet((string)($r['item_chon'] ?? '')); ?>
                </td>
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
