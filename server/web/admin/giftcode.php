<?php
/**
 * Quản lý mã quà tặng (bảng kpah_db.giftcode + giftcode_log).
 *
 * Bốn điều server làm, quyết định mọi thứ trên trang này
 * (DatabaseGiftCode.java:301-600):
 *   1. Câu tìm mã là `select * from giftcode where giftcode='...' and type = 0`
 *      → type khác 0 là mã vô hình với người chơi.
 *   2. `limit_use` là số lượt CÒN LẠI. Bằng 0 thì báo hết lượt. Chỉ trừ đi khi
 *      `limit >= 1`, nên số âm là không giới hạn thật sự (không trừ tiếp).
 *   3. Chặn nhận lại tính theo TÀI KHOẢN (cột `giftcode_log.userId`, sửa 09/09/2026) —
 *      một tài khoản chỉ nhận được một mã đúng một lần dù có bao nhiêu nhân vật.
 *   4. Cột `expire` chưa bao giờ được đọc. Muốn hết hạn thì tự đặt limit_use = 0.
 *
 * VÌ SAO PHẢI LỌC KÝ TỰ CHẶT: server ghép thẳng cả tên mã lẫn chuỗi phần thưởng
 * vào câu SQL (`insert into giftcode_log set ... giftcode = '<mã>', item = '<item>'`),
 * không dùng tham số. Một dấu nháy đơn lọt vào đây là hỏng câu lệnh của server.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/thuong.php';
require_once __DIR__ . '/inc/danh_muc_thuong.php';
requireAdmin();

$TRANG = 'giftcode.php';

/**
 * In cột "Phần thưởng": chuỗi thô để đối chiếu/copy, cộng bản dịch dễ đọc ngay
 * dưới — dùng chính `dmMoTaChuoiThuong()` đã dựng cho bộ chọn, nên tên hiện ra
 * ở đây LUÔN khớp với tên hiện trong bộ chọn phía trên.
 *
 * Không bao giờ ném lỗi ra bảng: một mã cũ với cú pháp lạ chỉ nên hiện dòng
 * "? ..." ở đúng chỗ của nó, không được làm vỡ toàn bộ danh sách giftcode.
 */
function oPhanThuongChiTiet(string $chuoi): void
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

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);
    $viec = p('viec');

    try {
        switch ($viec) {

            case 'tao':
            case 'sua': {
                $id    = (int)p('id');
                $ma    = p('giftcode');
                $xu    = (int)p('xu', '0');
                $luong = (int)p('luong', '0');
                $lock  = (int)p('luongLock', '0');
                $limit = (int)p('limit_use', '0');
                $type  = (int)p('type', '0');
                [$item, $loiItem] = kiemTraItem(p('item'));

                if (!preg_match('/^[A-Za-z0-9_]{2,50}$/', $ma)) {
                    veLai($TRANG, 'error', 'Tên mã chỉ được dùng chữ, số và gạch dưới, dài 2–50 ký tự.');
                }
                if ($loiItem) {
                    veLai($TRANG, 'error', 'Phần thưởng chưa hợp lệ: ' . implode(' | ', $loiItem));
                }
                if ($xu < 0 || $luong < 0 || $lock < 0) {
                    veLai($TRANG, 'error', 'Xu và lượng không được âm.');
                }

                // Trùng tên mã: server luôn lấy dòng đầu tiên (`limit 1`) nên dòng
                // thứ hai vĩnh viễn không ai nhận được.
                $ck = dbGame()->prepare('SELECT id FROM giftcode WHERE giftcode = ? AND id <> ? LIMIT 1');
                $ck->execute([$ma, $id]);
                if ($ck->fetchColumn()) {
                    veLai($TRANG, 'error', 'Đã có mã tên “' . $ma . '”. Đặt tên khác, vì server chỉ đọc dòng đầu tiên.');
                }

                if ($viec === 'tao') {
                    $st = dbGame()->prepare(
                        'INSERT INTO giftcode (giftcode, xu, luong, luongLock, item, expire, limit_use, type)
                         VALUES (?, ?, ?, ?, ?, 0, ?, ?)'
                    );
                    $st->execute([$ma, $xu, $luong, $lock, $item, $limit, $type]);
                    ghiNhatKy('tao_giftcode', $ma,
                        "xu=$xu luong=$luong luongLock=$lock limit=$limit type=$type item=$item");
                    veLai($TRANG, 'success', 'Đã tạo mã ' . $ma . '.');
                } else {
                    $st = dbGame()->prepare(
                        'UPDATE giftcode SET giftcode = ?, xu = ?, luong = ?, luongLock = ?,
                                             item = ?, limit_use = ?, type = ? WHERE id = ?'
                    );
                    $st->execute([$ma, $xu, $luong, $lock, $item, $limit, $type, $id]);
                    ghiNhatKy('sua_giftcode', $ma,
                        "xu=$xu luong=$luong luongLock=$lock limit=$limit type=$type item=$item");
                    veLai($TRANG, 'success', 'Đã lưu mã ' . $ma . '.');
                }
                break;
            }

            case 'tat': {
                $id = (int)p('id');
                $st = dbGame()->prepare('SELECT giftcode, limit_use FROM giftcode WHERE id = ?');
                $st->execute([$id]);
                $gc = $st->fetch();
                if (!$gc) {
                    veLai($TRANG, 'error', 'Không tìm thấy mã #' . $id . '.');
                }
                dbGame()->prepare('UPDATE giftcode SET limit_use = 0 WHERE id = ?')->execute([$id]);
                ghiNhatKy('tat_giftcode', (string)$gc['giftcode'], 'limit_use: ' . (int)$gc['limit_use'] . ' -> 0');
                veLai($TRANG, 'success', 'Đã tắt mã ' . $gc['giftcode'] . ' (đặt số lượt còn lại về 0).');
                break;
            }

            case 'xoa': {
                $id = (int)p('id');
                $st = dbGame()->prepare('SELECT giftcode FROM giftcode WHERE id = ?');
                $st->execute([$id]);
                $ten = (string)($st->fetchColumn() ?: '');
                if ($ten === '') {
                    veLai($TRANG, 'error', 'Không tìm thấy mã #' . $id . '.');
                }
                dbGame()->prepare('DELETE FROM giftcode WHERE id = ?')->execute([$id]);
                ghiNhatKy('xoa_giftcode', $ten, 'xoá hẳn khỏi bảng giftcode');
                veLai($TRANG, 'success', 'Đã xoá mã ' . $ten . '. Nhật ký ai đã nhận vẫn còn trong giftcode_log.');
                break;
            }

            case 'xoa_nhieu': {
                // Xoa nhieu ma mot luot. Doc thang $_POST vi p() chi tra ve chuoi.
                $tho = $_POST['ids'] ?? [];
                if (!is_array($tho)) {
                    $tho = [$tho];
                }
                // Loc ky: chi giu so nguyen duong, bo trung. Danh sach nay di thang vao
                // cau SQL duoi dang placeholder nen khong co duong tiem SQL, nhung van
                // loc de mot o rong hay chu la khong bien thanh id 0.
                $ids = [];
                foreach ($tho as $x) {
                    $n = filter_var($x, FILTER_VALIDATE_INT);
                    if ($n !== false && $n > 0) {
                        $ids[$n] = $n;
                    }
                }
                $ids = array_values($ids);
                if (!$ids) {
                    veLai($TRANG, 'error', 'Chưa chọn mã nào để xoá.');
                }

                $cho = implode(',', array_fill(0, count($ids), '?'));
                $st  = dbGame()->prepare('SELECT id, giftcode FROM giftcode WHERE id IN (' . $cho . ')');
                $st->execute($ids);
                $ten = [];
                foreach ($st->fetchAll() as $d) {
                    $ten[] = (string)$d['giftcode'];
                }
                if (!$ten) {
                    veLai($TRANG, 'error', 'Không tìm thấy mã nào trong số đã chọn.');
                }

                $xoa = dbGame()->prepare('DELETE FROM giftcode WHERE id IN (' . $cho . ')');
                $xoa->execute($ids);
                $soXoa = $xoa->rowCount();

                // Ghi nhat ky TUNG ma chu khong gop mot dong: sau nay tra cuu "ma X bien
                // di dau" thi tim theo ten ma van ra.
                foreach ($ten as $t) {
                    ghiNhatKy('xoa_giftcode', $t, 'xoá hàng loạt (' . $soXoa . ' mã một lượt)');
                }
                veLai($TRANG, 'success',
                    'Đã xoá ' . $soXoa . ' mã: ' . implode(', ', $ten)
                    . '. Nhật ký ai đã nhận vẫn còn trong giftcode_log.');
                break;
            }

            case 'cho_nhan_lai': {
                // Chan theo TAI KHOAN (userId) tu 09/09/2026, khong con theo ten nhan vat — xoa
                // theo userId de don sach het moi dong cua tai khoan do cho dung mot ma (ke ca
                // khi tai khoan co nhieu nhan vat cung nhan truoc do, du hiem sau khi sua).
                $ma     = p('ma');
                $userId = (int)p('userId');
                $player = p('player');
                $st = dbGame()->prepare('DELETE FROM giftcode_log WHERE giftcode = ? AND userId = ?');
                $st->execute([$ma, $userId]);
                ghiNhatKy('cho_nhan_lai_giftcode', $ma, 'tài khoản #' . $userId . ' (' . $player . '), xoá ' . $st->rowCount() . ' dòng');
                veLai($TRANG . '?tab=log', 'success',
                    'Tài khoản của ' . $player . ' có thể nhận lại mã ' . $ma . ' (' . $st->rowCount() . ' dòng đã xoá).');
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
$tab   = g('tab') === 'log' ? 'log' : 'ma';
$suaId = (int)g('sua');
$dangSua = null;
$dsMa  = [];
$demNhan = [];
$loiDoc = '';

// Trang nhật ký nhận mã
$logMa     = g('ma');
$logPlayer = g('player');
$logRows   = [];
$logTrang  = max(1, (int)g('trang', '1'));
$logMoi    = 40;
$conTrangSau = false;

try {
    if ($tab === 'ma') {
        $dsMa = dbGame()->query('SELECT * FROM giftcode ORDER BY id DESC')->fetchAll();
        foreach (dbGame()->query('SELECT giftcode, COUNT(*) AS n FROM giftcode_log GROUP BY giftcode')->fetchAll() as $r) {
            $demNhan[strtolower((string)$r['giftcode'])] = (int)$r['n'];
        }
        if ($suaId > 0) {
            $st = dbGame()->prepare('SELECT * FROM giftcode WHERE id = ?');
            $st->execute([$suaId]);
            $dangSua = $st->fetch() ?: null;
        }
    } else {
        $dieuKien = [];
        $thamSo   = [];
        if ($logMa !== '')     { $dieuKien[] = 'giftcode = ?'; $thamSo[] = $logMa; }
        if ($logPlayer !== '') { $dieuKien[] = 'player = ?';   $thamSo[] = $logPlayer; }
        $where = $dieuKien ? 'WHERE ' . implode(' AND ', $dieuKien) : '';

        // Lấy dư 1 dòng để biết có trang sau hay không, thay vì đếm COUNT(*)
        // trên một bảng có thể rất lớn.
        // LIMIT / OFFSET ghép thẳng bằng số nguyên đã ép kiểu: PDO đang tắt giả
        // lập prepare nên tham số gửi đi dạng chuỗi và `LIMIT '41'` là lỗi cú pháp.
        $lay = $logMoi + 1;
        $bo  = ($logTrang - 1) * $logMoi;
        $st = dbGame()->prepare("SELECT id, giftcode, player, userId, item, xu, luong, luongK
                                   FROM giftcode_log $where ORDER BY id DESC LIMIT $lay OFFSET $bo");
        $st->execute($thamSo);
        $logRows = $st->fetchAll();
        if (count($logRows) > $logMoi) {
            $conTrangSau = true;
            array_pop($logRows);
        }
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

$f = static function (?array $r, string $cot, string $macDinh = '') {
    return $r === null ? $macDinh : (string)$r[$cot];
};

adminHead('giftcode', 'Giftcode');
?>
<div class="page-head">
    <div>
        <h1>Giftcode</h1>
        <p>Tạo mã, nạp thêm lượt, tắt mã, và xem ai đã nhận gì.</p>
    </div>
    <div class="ngang">
        <a href="giftcode.php" class="btn <?= $tab === 'ma' ? 'btn-primary' : 'btn-xam' ?> btn-nho">Danh sách mã</a>
        <a href="giftcode.php?tab=log" class="btn <?= $tab === 'log' ? 'btn-primary' : 'btn-xam' ?> btn-nho">Ai đã nhận</a>
    </div>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<?php if ($tab === 'ma'): ?>

    <div class="card">
        <h3><?= $dangSua ? 'Sửa mã “' . e($dangSua['giftcode']) . '”' : 'Tạo mã mới' ?></h3>
        <form method="post" action="giftcode.php">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="viec" value="<?= $dangSua ? 'sua' : 'tao' ?>">
            <?php if ($dangSua): ?>
                <input type="hidden" name="id" value="<?= (int)$dangSua['id'] ?>">
            <?php endif; ?>

            <div class="form-grid">
                <label class="o"><span>Tên mã</span>
                    <input type="text" name="giftcode" required maxlength="50" pattern="[A-Za-z0-9_]{2,50}"
                           value="<?= e($f($dangSua, 'giftcode')) ?>" placeholder="tanthu2026">
                    <small>Chỉ chữ, số, gạch dưới. Không phân biệt hoa thường khi nhập trong game.</small>
                </label>
                <label class="o"><span>Xu</span>
                    <input type="number" name="xu" value="<?= e($f($dangSua, 'xu', '0')) ?>" min="0">
                </label>
                <label class="o"><span>Lượng</span>
                    <input type="number" name="luong" value="<?= e($f($dangSua, 'luong', '0')) ?>" min="0">
                </label>
                <label class="o"><span>Lượng khoá</span>
                    <input type="number" name="luongLock" value="<?= e($f($dangSua, 'luongLock', '0')) ?>" min="0">
                </label>
                <label class="o"><span>Số lượt còn lại</span>
                    <input type="number" name="limit_use" value="<?= e($f($dangSua, 'limit_use', '100')) ?>">
                    <small><strong>-1 = không giới hạn</strong> (số âm không bị trừ). 0 = mã đã tắt.</small>
                </label>
                <label class="o"><span>Trạng thái</span>
                    <select name="type">
                        <option value="0" <?= (int)$f($dangSua, 'type', '0') === 0 ? 'selected' : '' ?>>Đang bật (type 0)</option>
                        <option value="1" <?= (int)$f($dangSua, 'type', '0') === 1 ? 'selected' : '' ?>>Ẩn — người chơi không dùng được</option>
                    </select>
                </label>
            </div>

            <?php
            // -------------------------------------------------------------------
            //  BỘ CHỌN VẬT PHẨM
            // -------------------------------------------------------------------
            // Chỉ SINH CHUỖI rồi chèn vào ô văn bản bên dưới. Chuỗi vẫn đi qua
            // kiemTraItem() y như khi gõ tay, nên bộ chọn không mở ra một đường dữ
            // liệu thứ hai — xem đầu inc/danh_muc_thuong.php.
            //
            // Cả danh mục được nhúng thẳng vào trang, không gọi AJAX: tổng chỉ
            // ~1350 lựa chọn (914 vật phẩm + 162 thuốc + 270 ngọc). Một endpoint
            // AJAX ở đây sẽ là một đường vào mới phải tự lo xác thực và CSRF, để
            // đổi lấy đúng một lần tải nhẹ hơn.
            $dmuc = dmDanhMuc(dbGame());
            ?>
            <?php if ($dmuc): ?>
            <div class="bochon" id="boChon">
                <div class="bochon-dau">
                    <strong>Chọn vật phẩm</strong>
                    <span class="muted small">không cần nhớ id — chọn rồi bấm Thêm</span>
                </div>
                <div class="bochon-hang">
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
                    <button type="button" class="btn btn-nho btn-primary" id="bcThem">Thêm vào phần thưởng</button>
                    <code class="bochon-xem" id="bcXem">—</code>
                </div>
                <p class="muted small" id="bcGhiChu"></p>
            </div>
            <?php endif; ?>

            <label class="o" style="margin-top:12px;"><span>Phần thưởng vật phẩm</span>
                <textarea name="item" id="oPhanThuong" placeholder="ITEM:848:1000,TRANGPHUC:833:1:30"><?= e($f($dangSua, 'item')) ?></textarea>
                <small>Nhiều phần thưởng ngăn nhau bằng dấu phẩy. Để trống nếu chỉ cho xu / lượng.
                       Cú pháp được kiểm tra trước khi lưu — xem bảng bên dưới.
                       Vẫn gõ hoặc dán tay được như trước; bộ chọn chỉ ghép chuỗi giúp bạn.</small>
            </label>

            <div class="form-actions">
                <button class="btn btn-primary btn-nho"><?= $dangSua ? 'Lưu thay đổi' : 'Tạo mã' ?></button>
                <?php if ($dangSua): ?>
                    <a href="giftcode.php" class="btn btn-xam btn-nho">Huỷ</a>
                <?php endif; ?>
            </div>
        </form>

        <?php if ($dmuc): ?>
        <script>
        /*
         * Bộ chọn vật phẩm. Chỉ GHÉP CHUỖI rồi chèn vào ô "Phần thưởng vật phẩm" —
         * chuỗi đó vẫn đi qua kiemTraItem() phía máy chủ y như khi gõ tay, nên script
         * này không phải chốt an toàn và tắt JavaScript cũng không mở ra lối nào.
         *
         * Toàn bộ danh mục nhúng thẳng vào trang, không gọi AJAX: chỉ ~1350 lựa chọn,
         * mà một endpoint AJAX là một đường vào mới phải tự lo requireAdmin và CSRF.
         *
         * JS thuần, không thư viện: web/ không được tải gì từ Internet
         * (web/admin/test/kiem_giao_dien.php có phép kiểm giữ điều đó).
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
            var oNhom   = document.getElementById('bcNhom');
            var oTim    = document.getElementById('bcTim');
            var oMuc    = document.getElementById('bcMuc');
            var oTruong = document.getElementById('bcTruong');
            var oDem    = document.getElementById('bcDem');
            var oXem    = document.getElementById('bcXem');
            var oGhiChu = document.getElementById('bcGhiChu');
            var oThem   = document.getElementById('bcThem');
            var oPT     = document.getElementById('oPhanThuong');
            if (!oNhom || !oMuc || !oPT) { return; }

            function nhomHienTai() { return DM[oNhom.value] || null; }

            /* Dựng lại danh sách vật phẩm theo nhóm + từ khoá tìm. */
            function veDanhSach() {
                var n = nhomHienTai();
                oMuc.innerHTML = '';
                if (!n) { return; }
                var tu = (oTim.value || '').trim().toLowerCase();
                var dem = 0;
                for (var id in n.muc) {
                    if (!Object.prototype.hasOwnProperty.call(n.muc, id)) { continue; }
                    var ten = n.muc[id];
                    /* Tìm theo CẢ tên và id: người quen id cũ vẫn gõ số được. */
                    if (tu !== '' && ten.toLowerCase().indexOf(tu) === -1
                        && id.indexOf(tu) === -1) { continue; }
                    var o = document.createElement('option');
                    o.value = id;
                    o.textContent = id + ' — ' + ten;
                    oMuc.appendChild(o);
                    dem++;
                }
                if (dem > 0) { oMuc.selectedIndex = 0; }
                oDem.textContent = tu === '' ? ('(' + dem + ')')
                    : ('(' + dem + ' khớp “' + tu + '”)');
                if (dem === 0) {
                    var t = document.createElement('option');
                    t.textContent = 'không có món nào khớp';
                    t.disabled = true;
                    oMuc.appendChild(t);
                }
            }

            /* Dựng lại các ô số / ô chọn theo loại phần thưởng của nhóm. */
            function veTruong() {
                var n = nhomHienTai();
                oTruong.innerHTML = '';
                if (!n) { return; }
                (TRUONG[n.loai] || []).forEach(function (t) {
                    /*
                     * Trường ẩn (t.an === true): tham số server BẮT BUỘC PHẢI CÓ trong
                     * chuỗi (server truy cập theo chỉ số mảng cố định) nhưng KHÔNG hề
                     * đọc giá trị — ví dụ TK/TKMAX có một "_" chưa dùng ở giữa. Không
                     * hiện ô nhập, chỉ gắn sẵn giá trị cố định để chuoi() đọc được.
                     */
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
                        /*
                         * Mặc định sàn 1 (số lượng không được là 0), nhưng vài trường
                         * (số ngày/phút "0 = vĩnh viễn") khai rõ minSo='0' — ép cứng
                         * min='1' cho mọi ô số từng làm trình duyệt TỰ KẸP 0 thành 1
                         * ngay khi rời ô, biến "vĩnh viễn" thành "1 ngày/phút" âm thầm.
                         */
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

            /* Chuỗi sẽ được chèn, theo đúng thứ tự tham số của máy chủ. */
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

            function doiMuc() {
                veXem();
            }

            function doiNhom() {
                var n = nhomHienTai();
                oGhiChu.textContent = n ? (n.ghiChu || '') : '';
                veTruong();
                veDanhSach();
                doiMuc();
            }

            oThem.addEventListener('click', function () {
                var s = chuoi();
                if (s === '') { return; }
                var cu = (oPT.value || '').trim();
                /* Bỏ dấu phẩy lẻ ở cuối để không sinh ra phần rỗng — kiemTraItem()
                   bỏ qua phần rỗng, nhưng chuỗi lưu lại trông như bị lỗi. */
                cu = cu.replace(/,+\s*$/, '');
                oPT.value = (cu === '') ? s : (cu + ',' + s);
                oPT.focus();
            });

            oNhom.addEventListener('change', doiNhom);
            oTim.addEventListener('input', function () { veDanhSach(); doiMuc(); });
            oMuc.addEventListener('change', doiMuc);
            oTim.addEventListener('keydown', function (e) {
                /* Enter trong ô tìm KHÔNG được gửi biểu mẫu: người ta đang tìm món,
                   chưa chọn xong, mà Enter mặc định sẽ lưu luôn cả mã giftcode. */
                if (e.key === 'Enter') { e.preventDefault(); oMuc.focus(); }
            });

            doiNhom();
        })();
        </script>
        <?php endif; ?>

        <div class="luuy">
            <p><strong>Nhớ ba điều:</strong></p>
            <p>· Mỗi <em>tài khoản</em> chỉ nhận một mã một lần, dù có bao nhiêu nhân vật (chặn theo
               <code>giftcode_log.userId</code>, không còn theo tên nhân vật).</p>
            <p>· Cột <code>expire</code> trong bảng không có tác dụng gì cả; muốn hết hạn thì tự bấm “Tắt”.</p>
            <p>· Mã cho quá nhiều món một lúc dễ làm hành trang phình to; trước đây từng có nhân vật
               bị server tự khoá vì lưu hỏng. Xem <code>vps/sql-giftcode.md</code>.</p>
        </div>
    </div>

    <div class="card">
        <h3>Danh sách mã (<?= count($dsMa) ?>)</h3>

        <!-- Form xoa hang loat de NGOAI bang. Tung dong da co form "Tat"/"Xoa" rieng,
             ma HTML khong cho long form vao nhau, nen cac o chon ben duoi noi vao form
             nay bang thuoc tinh form="hangloat" thay vi nam trong no. -->
        <form id="hangloat" method="post" action="giftcode.php"
              onsubmit="return confirm('XOÁ HẲN các mã đã chọn? Không hoàn lại được.');">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="viec" value="xoa_nhieu">
        </form>

        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr>
                <th class="num"><input type="checkbox" id="chonTatCa" title="Chọn tất cả"></th>
                <th class="num">ID</th><th>Mã</th><th class="num">Xu</th><th class="num">Lượng</th>
                <th class="num">Lượng khoá</th><th>Phần thưởng</th><th class="num">Còn lượt</th>
                <th class="num">Đã phát</th><th>Trạng thái</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($dsMa as $gc):
                $conLuot = (int)$gc['limit_use'];
                $batTat  = (int)$gc['type'] === 0;
                $dung    = $batTat && $conLuot !== 0;
            ?>
                <tr>
                    <td class="num"><input type="checkbox" form="hangloat" name="ids[]"
                                           value="<?= (int)$gc['id'] ?>" class="oChon"></td>
                    <td class="num"><?= (int)$gc['id'] ?></td>
                    <td><strong><?= e($gc['giftcode']) ?></strong></td>
                    <td class="num"><?= so($gc['xu']) ?></td>
                    <td class="num"><?= so($gc['luong']) ?></td>
                    <td class="num"><?= so($gc['luongLock']) ?></td>
                    <td class="info"><?php oPhanThuongChiTiet((string)($gc['item'] ?? '')); ?></td>
                    <td class="num"><?= $conLuot < 0 ? '∞' : so($conLuot) ?></td>
                    <td class="num">
                        <a href="giftcode.php?tab=log&amp;ma=<?= e(urlencode($gc['giftcode'])) ?>">
                            <?= so($demNhan[strtolower((string)$gc['giftcode'])] ?? 0) ?>
                        </a>
                    </td>
                    <td><?php if ($dung): ?><span class="nhan nhan-ok">Dùng được</span>
                        <?php elseif (!$batTat): ?><span class="nhan nhan-mo">Ẩn</span>
                        <?php else: ?><span class="nhan nhan-cho">Hết lượt</span><?php endif; ?></td>
                    <td class="nowrap">
                        <a class="btn btn-xam btn-nho" href="giftcode.php?sua=<?= (int)$gc['id'] ?>">Sửa</a>
                        <?php if ($conLuot !== 0): ?>
                            <form method="post" action="giftcode.php" class="inline"
                                  onsubmit="return confirm('Tắt mã <?= e($gc['giftcode']) ?>?');">
                                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                                <input type="hidden" name="viec" value="tat">
                                <input type="hidden" name="id" value="<?= (int)$gc['id'] ?>">
                                <button class="btn btn-xam btn-nho">Tắt</button>
                            </form>
                        <?php endif; ?>
                        <form method="post" action="giftcode.php" class="inline"
                              onsubmit="return confirm('XOÁ HẲN mã <?= e($gc['giftcode']) ?>? Không hoàn lại được.');">
                            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                            <input type="hidden" name="viec" value="xoa">
                            <input type="hidden" name="id" value="<?= (int)$gc['id'] ?>">
                            <button class="btn btn-do btn-nho">Xoá</button>
                        </form>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$dsMa): ?>
                <tr><td colspan="11" class="muted">Chưa có mã nào.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>

        <?php if ($dsMa): ?>
        <p class="nowrap" style="margin-top:10px">
            <button class="btn btn-do btn-nho" form="hangloat">Xoá các mã đã chọn</button>
            <span class="muted" id="demChon">chưa chọn mã nào</span>
        </p>
        <script>
        // Chon tat ca + dem so ma dang chon. Khong co script nay thi tung o van bam tay
        // duoc va nut xoa van chay — script chi lam cho tien tay hon, khong phai dieu kien.
        (function () {
            var tatCa = document.getElementById('chonTatCa');
            var dem   = document.getElementById('demChon');
            var o     = Array.prototype.slice.call(document.querySelectorAll('.oChon'));
            function capNhat() {
                var n = o.filter(function (x) { return x.checked; }).length;
                dem.textContent = n === 0 ? 'chưa chọn mã nào' : ('đang chọn ' + n + ' mã');
                if (tatCa) {
                    tatCa.checked = n > 0 && n === o.length;
                    tatCa.indeterminate = n > 0 && n < o.length;
                }
            }
            if (tatCa) {
                tatCa.addEventListener('change', function () {
                    o.forEach(function (x) { x.checked = tatCa.checked; });
                    capNhat();
                });
            }
            o.forEach(function (x) { x.addEventListener('change', capNhat); });
            capNhat();
        })();
        </script>
        <?php endif; ?>
    </div>

    <div class="card">
        <h3>Các loại phần thưởng server hiểu</h3>
        <p class="muted small">Rút thẳng từ <code>src/data/DatabaseGiftCode.java</code>. Phần ghi <code>_</code>
           là tham số server không dùng tới nhưng <strong>vẫn phải có</strong> — thiếu là server ném lỗi
           giữa chừng, người chơi nhận nửa phần quà mà mã không bị trừ lượt. Cột "Ví dụ" bấm
           <strong>Chèn</strong> để nối thẳng vào ô "Phần thưởng vật phẩm" phía trên.</p>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Cú pháp</th><th>Là gì</th><th>Ví dụ</th><th></th></tr></thead>
            <tbody>
            <?php foreach (loaiThuong() as $loai => [$n, $cuPhap, $mota, $viDu]): ?>
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
        <script>
        // Chen mot vi du co san vao ngay cuoi o "Phan thuong vat pham" — dung LAI y het
        // logic noi chuoi cua bo chon phia tren (oThem.addEventListener), khong phai
        // ghi de: nguoi dung co the chen nhieu vi du lien tiep de ghep mot ma nhieu quà.
        (function () {
            var oPT = document.getElementById('oPhanThuong');
            if (!oPT) { return; }
            document.querySelectorAll('.oChenVd').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var s = btn.getAttribute('data-vd') || '';
                    if (s === '') { return; }
                    var cu = (oPT.value || '').trim().replace(/,+\s*$/, '');
                    oPT.value = (cu === '') ? s : (cu + ',' + s);
                    oPT.focus();
                });
            });
        })();
        </script>
    </div>

<?php else: ?>

    <div class="card">
        <h3>Ai đã nhận mã</h3>
        <form method="get" action="giftcode.php" class="ngang">
            <input type="hidden" name="tab" value="log">
            <label class="o"><span>Mã</span>
                <input type="text" name="ma" value="<?= e($logMa) ?>" placeholder="để trống = tất cả">
            </label>
            <label class="o"><span>Tên nhân vật</span>
                <input type="text" name="player" value="<?= e($logPlayer) ?>" placeholder="để trống = tất cả">
            </label>
            <button class="btn btn-primary btn-nho">Lọc</button>
            <?php if ($logMa !== '' || $logPlayer !== ''): ?>
                <a href="giftcode.php?tab=log" class="btn btn-xam btn-nho">Bỏ lọc</a>
            <?php endif; ?>
        </form>

        <div class="tbl-wrap" style="margin-top:14px;">
        <table class="tbl">
            <thead><tr>
                <th class="num">ID</th><th>Mã</th><th>Nhân vật</th>
                <th class="num">Xu</th><th class="num">Lượng</th><th class="num">Lượng khoá</th>
                <th>Phần thưởng</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($logRows as $r): ?>
                <tr>
                    <td class="num"><?= (int)$r['id'] ?></td>
                    <td><a href="giftcode.php?tab=log&amp;ma=<?= e(urlencode((string)$r['giftcode'])) ?>"><?= e((string)$r['giftcode']) ?></a></td>
                    <td><a href="nhanvat.php?q=<?= e(urlencode((string)$r['player'])) ?>"><?= e((string)$r['player']) ?></a></td>
                    <td class="num"><?= so($r['xu']) ?></td>
                    <td class="num"><?= so($r['luong']) ?></td>
                    <td class="num"><?= so($r['luongK']) ?></td>
                    <td class="info"><?php oPhanThuongChiTiet((string)($r['item'] ?? '')); ?></td>
                    <td class="nowrap">
                        <form method="post" action="giftcode.php" class="inline"
                              onsubmit="return confirm('Cho tài khoản của <?= e((string)$r['player']) ?> nhận lại mã <?= e((string)$r['giftcode']) ?>?');">
                            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                            <input type="hidden" name="viec" value="cho_nhan_lai">
                            <input type="hidden" name="ma" value="<?= e((string)$r['giftcode']) ?>">
                            <input type="hidden" name="userId" value="<?= (int)$r['userId'] ?>">
                            <input type="hidden" name="player" value="<?= e((string)$r['player']) ?>">
                            <button class="btn btn-xam btn-nho">Cho nhận lại</button>
                        </form>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$logRows): ?>
                <tr><td colspan="8" class="muted">Chưa có ai nhận mã nào khớp bộ lọc.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>

        <div class="phantrang">
            <?php $qs = 'tab=log&ma=' . urlencode($logMa) . '&player=' . urlencode($logPlayer); ?>
            <?php if ($logTrang > 1): ?>
                <a class="btn btn-xam btn-nho" href="giftcode.php?<?= e($qs) ?>&amp;trang=<?= $logTrang - 1 ?>">← Trang trước</a>
            <?php endif; ?>
            <span>Trang <?= $logTrang ?></span>
            <?php if ($conTrangSau): ?>
                <a class="btn btn-xam btn-nho" href="giftcode.php?<?= e($qs) ?>&amp;trang=<?= $logTrang + 1 ?>">Trang sau →</a>
            <?php endif; ?>
        </div>

        <p class="muted small" style="margin-top:10px;">
            “Cho nhận lại” xoá mọi dòng của <strong>tài khoản đó</strong> (theo <code>userId</code>, không
            chỉ riêng nhân vật đang hiện ở dòng này) với mã này trong <code>giftcode_log</code> — đúng thứ
            server dùng để chặn nhận trùng. Nó <strong>không</strong> cộng lại lượt cho mã.
        </p>
    </div>

<?php endif; ?>
<?php adminFoot(); ?>
