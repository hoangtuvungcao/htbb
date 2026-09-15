<?php
/**
 * Cấu hình server (bảng kpah_db.cau_hinh) — 32 setting server.ini nạp lại được
 * lúc đang chạy, xem src/server/CauHinh.java.
 *
 * TRƯỚC 09/09/2026: chỉ sửa được bằng cách SSH vào VPS sửa tay server.ini, hoặc
 * vào game gõ chat "setting" (real/AdminMenu.java) — không sửa từ xa qua web được.
 *
 * NAY: trang này, bảng setting trong game, và server game đọc/ghi CHUNG một bảng SQL.
 * Server tự đọc lại bảng này mỗi 10 giây (server.CauHinh.batDau()) nên sửa ở đây thì
 * server áp dụng trong tối đa 10 giây — KHÔNG cần restart, không cần vào game gõ lệnh.
 *
 * KHÔNG PHẢI TOÀN BỘ server.ini nằm ở đây — chỉ đúng 32 khoá đã có sẵn cơ chế nạp lại
 * (xem CauHinh.java). Khoảng 20 khoá bắt buộc để kết nối CSDL/mở cổng (db.host, sv.port,
 * ...) và một số khoá gameplay khác vẫn đọc một lần lúc khởi động, buộc phải sửa qua
 * server.ini + restart như trước.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

const BANG_CAU_HINH = 'cau_hinh';

$TRANG = 'cauhinh.php';

/** Phải khớp K_* trong src/server/CauHinh.java. */
const K_TILE_XU = 'sv.tileXu';
const K_TILE_LUONG = 'sv.tileLuong';
const K_TILE_SUKIEN = 'sv.tileItemSuKien';
const K_TILE_POTION = 'sv.tilePotion';
const K_TILE_NGOC = 'sv.tileNgoc';
const K_TILE_ITEM = 'sv.tileItem';
const K_CHIA_XU = 'sv.chiaXuRoi';
const K_TILE_EXP_LV50 = 'sv.tileExpLv50';
const K_TILE_EXP_DUOI_LV50 = 'sv.tileExpDuoiLv50';
const K_TIEU_NGUONG_MAU = 'sv.tieuNguongMau';
const K_TIEU_HE_SO_EXP = 'sv.tieuHeSoExp';
const K_TIEU_PC_EXP_THEO_MAU = 'sv.tieuPcExpTheoMau';
const K_TIEU_PERMILLE_CAO_CAP = 'sv.tieuPermilleCaoCap';
const K_GIO_CT = 'sv.gioChiemThanh';
const K_PHUT_CT = 'sv.phutChiemThanh';
const K_NGAY_CT = 'sv.ngayChiemThanh';
const K_SK_TU_KHOI_DONG_LAI = 'sv.suKienTuKhoiDongLai';
const K_SK_GOM_GIAY = 'sv.suKienGomGiay';
const K_SK_BAO_TRUOC_GIAY = 'sv.suKienBaoTruocGiay';
const K_TAO_TEN_GM = 'sv.choPhepTaoTenGM';
const K_MAX_CHAR = 'sv.maxCharPerAccount';
const K_ADMIN_ACCOUNTS = 'sv.adminAccounts';
const K_WEBSITE = 'sv.website';
const K_LINK_TAI_GAME = 'sv.linkTaiGame';
const K_TEST_MODE = 'sv.testMode';
const K_TEST_CODE = 'sv.testCode';
const K_EXP_VANTIEU = 'sv.expvantieu';
const K_LUCKY_EXP = 'sv.Luckyexp';
const K_EXP_QUA = 'sv.ExpQua';

/** Tạo bảng nếu server game chưa từng khởi động (đúng định nghĩa CauHinh.taoBangNeuThieu). */
function taoBangCauHinhNeuThieu(): void
{
    dbGame()->exec(
        'CREATE TABLE IF NOT EXISTS `' . BANG_CAU_HINH . '` ('
        . '`khoa` varchar(64) NOT NULL,'
        . '`gia_tri` varchar(255) NOT NULL DEFAULT \'\','
        . '`sua_boi` varchar(50) DEFAULT NULL,'
        . '`sua_luc` datetime DEFAULT NULL,'
        . 'PRIMARY KEY (`khoa`)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
    );
}

/** Ghi nhiều khoá trong một lượt, sua_boi = admin đang đăng nhập. */
function luuCauHinh(array $capGiaTri): void
{
    taoBangCauHinhNeuThieu();
    $st = dbGame()->prepare(
        'INSERT INTO `' . BANG_CAU_HINH . '` (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, ?, NOW())
         ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), sua_boi = VALUES(sua_boi), sua_luc = VALUES(sua_luc)'
    );
    foreach ($capGiaTri as $khoa => $giaTri) {
        $st->execute([$khoa, (string)$giaTri, adminName()]);
    }
}

/** int, kẹp trong khoảng, mặc định khi không đọc được. */
function soKep(string $tho, int $min, int $max, int $macDinh): int
{
    $n = filter_var(trim($tho), FILTER_VALIDATE_INT);
    if ($n === false) {
        return $macDinh;
    }
    return max($min, min($max, $n));
}

/** float, kẹp trong khoảng, mặc định khi không đọc được. Cho phép số lẻ (vd 0.001). */
function soThapPhan(string $tho, float $min, float $max, float $macDinh): float
{
    $n = filter_var(trim($tho), FILTER_VALIDATE_FLOAT);
    if ($n === false) {
        return $macDinh;
    }
    return max($min, min($max, $n));
}

/** Đọc một khoá dạng CSV "a,b,c" từ $gt thành int[]; sai số phần tử thì dùng $macDinh. */
function mangTu(array $gt, string $k, array $macDinh): array
{
    $raw = trim(array_key_exists($k, $gt) ? (string)$gt[$k] : '');
    if ($raw === '') {
        return $macDinh;
    }
    $phan = array_map('intval', explode(',', $raw));
    return count($phan) === count($macDinh) ? $phan : $macDinh;
}

/** Ten 7 thu trong tuan dung cho checkbox + Java Date.toString(), theo dung thu tu Thu Hai truoc. */
const CAC_THU_TRONG_TUAN = [
    'Mon' => 'Thứ Hai', 'Tue' => 'Thứ Ba', 'Wed' => 'Thứ Tư', 'Thu' => 'Thứ Năm',
    'Fri' => 'Thứ Sáu', 'Sat' => 'Thứ Bảy', 'Sun' => 'Chủ Nhật',
];

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);
    $viec = p('viec');

    try {
        switch ($viec) {

            case 'luu_drop': {
                $m = [
                    K_TILE_XU => soKep(p('tileXu'), 0, 100, 0),
                    K_TILE_LUONG => soKep(p('tileLuong'), 0, 100, 0),
                    K_TILE_SUKIEN => soKep(p('tileItemSuKien'), 0, 100, 0),
                    K_TILE_POTION => soKep(p('tilePotion'), 0, 100, 0),
                    K_TILE_NGOC => soKep(p('tileNgoc'), 0, 100, 0),
                    K_TILE_ITEM => soKep(p('tileItem'), 0, 100, 0),
                    K_CHIA_XU => soKep(p('chiaXuRoi'), 1, 1000000, 10),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'ti_le_drop', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu tỷ lệ rơi đồ. Server áp dụng trong tối đa 10 giây.');
                break;
            }

            case 'luu_exp_quai': {
                $m = [
                    K_TILE_EXP_LV50 => soKep(p('tileExpLv50'), 0, 100, 10),
                    K_TILE_EXP_DUOI_LV50 => soKep(p('tileExpDuoiLv50'), 0, 100, 50),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'ti_le_exp_quai', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu tỉ lệ EXP quái. Server áp dụng trong tối đa 10 giây.');
                break;
            }

            case 'luu_tieu': {
                // Nguoi dung nhap % rieng cho 5 mau dau (Trang..Vang) - Tim la PHAN CON LAI,
                // khong nhap. Cong don thanh nguong (thang 100000) tu 5 % nay LUON tang dan
                // vi la tong don dieu cua so khong am - khong con can kiem "phai tang dan"
                // nhu truoc, chi can chan tong 5 mau khong vuot 100%.
                $phanTramMau = [
                    soThapPhan(p('tieuMau0'), 0, 100, 60.189),
                    soThapPhan(p('tieuMau1'), 0, 100, 29.2),
                    soThapPhan(p('tieuMau2'), 0, 100, 10.3),
                    soThapPhan(p('tieuMau3'), 0, 100, 0.3),
                    soThapPhan(p('tieuMau4'), 0, 100, 0.01),
                ];
                $tongPhanTramMau = round(array_sum($phanTramMau), 3);
                if ($tongPhanTramMau > 100) {
                    veLai($TRANG, 'error', 'Tổng % của Trắng+Xanh+Đỏ+Hoàn Mỹ+Vàng đang vượt quá '
                        . '100% (' . $tongPhanTramMau . '%) — không còn chỗ cho Tím. '
                        . 'Giảm bớt một trong 5 màu rồi lưu lại.');
                }
                $nguong = [];
                $congDon = 0;
                foreach ($phanTramMau as $pt) {
                    $congDon += (int)round($pt * 1000);
                    $nguong[] = min(100000, $congDon);
                }

                $heSoChung = soThapPhan(p('tieuHeSoChung'), 0, 10, 2.0);
                $heSoMau = [
                    soThapPhan(p('tieuHeSoMau0'), 0, 100, 0.5),
                    soThapPhan(p('tieuHeSoMau1'), 0, 100, 1.0),
                    soThapPhan(p('tieuHeSoMau2'), 0, 100, 1.5),
                    soThapPhan(p('tieuHeSoMau3'), 0, 100, 2.5),
                    soThapPhan(p('tieuHeSoMau4'), 0, 100, 2.5),
                    soThapPhan(p('tieuHeSoMau5'), 0, 100, 5.0),
                ];
                $pcExp = array_map(static fn($h) => (int)round($h * 100), $heSoMau);
                $pcExp[] = 0; // phan tu thu 7 - khong mau nao doc toi, giu 0 cho du do dai

                $permille = [];
                foreach ([0, 1, 2] as $khung) {
                    foreach ([0, 1, 2] as $mau) {
                        $pcCaoCap = soThapPhan(p("tieuPhanTramCaoCap{$khung}_{$mau}"), 0, 100, 0);
                        $permille[] = (int)round($pcCaoCap * 10);
                    }
                }

                $m = [
                    K_TIEU_NGUONG_MAU => implode(',', $nguong),
                    K_TIEU_HE_SO_EXP => (int)round($heSoChung * 100),
                    K_TIEU_PC_EXP_THEO_MAU => implode(',', $pcExp),
                    K_TIEU_PERMILLE_CAO_CAP => implode(',', $permille),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'ti_le_tieu', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu tỉ lệ Vận Tiêu. Server áp dụng trong tối đa 10 giây.');
                break;
            }

            case 'luu_chiemthanh': {
                $gio = trim(p('gioChiemThanh'));
                $ngayCuThe = trim(p('ngayCuThe'));
                if ($ngayCuThe !== '' && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $ngayCuThe)) {
                    veLai($TRANG, 'error', 'Ngày cụ thể phải đúng định dạng yyyy-MM-dd.');
                }
                if ($ngayCuThe !== '') {
                    // Ngay cu the ghi de moi lua chon thu trong tuan - chi ap dung DUNG mot lan.
                    $ngay = $ngayCuThe;
                } else {
                    $cacThu = [];
                    foreach (array_keys(CAC_THU_TRONG_TUAN) as $thu) {
                        if (p('thu_' . $thu) === '1') {
                            $cacThu[] = $thu;
                        }
                    }
                    $ngay = implode(',', $cacThu);
                }
                $m = [
                    K_GIO_CT => ($gio === '' ? -1 : soKep($gio, -1, 23, -1)),
                    K_PHUT_CT => soKep(p('phutChiemThanh'), 0, 59, 0),
                    K_NGAY_CT => substr($ngay, 0, 60),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'gio_chiem_thanh', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu giờ chiếm thành. Server áp dụng trong tối đa 10 giây.');
                break;
            }

            case 'luu_lichhen': {
                $m = [
                    K_SK_TU_KHOI_DONG_LAI => (p('tuKhoiDongLai') === '1' ? '1' : '0'),
                    K_SK_GOM_GIAY => soKep(p('gomGiay'), 30, 3600, 120),
                    K_SK_BAO_TRUOC_GIAY => soKep(p('baoTruocGiay'), 10, 600, 60),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'lich_hen_su_kien', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu lịch hẹn sự kiện. Server áp dụng trong tối đa 10 giây.');
                break;
            }

            case 'luu_khac': {
                $m = [
                    K_TAO_TEN_GM => (p('choPhepTaoTenGM') === '1' ? '1' : '0'),
                    K_MAX_CHAR => soKep(p('maxCharPerAccount'), 1, 3, 3),
                    K_ADMIN_ACCOUNTS => substr(trim(p('adminAccounts')), 0, 255),
                    K_WEBSITE => substr(trim(p('website')), 0, 255),
                    K_LINK_TAI_GAME => substr(trim(p('linkTaiGame')), 0, 255),
                    K_TEST_MODE => (p('testMode') === '1' ? '1' : '0'),
                    K_TEST_CODE => strtolower(substr(trim(p('testCode')), 0, 50)),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'khac', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu. Server áp dụng trong tối đa 10 giây.');
                break;
            }

            case 'luu_exp_phu': {
                $m = [
                    K_EXP_VANTIEU => (p('expVantieu') === '1' ? '1' : '0'),
                    K_LUCKY_EXP => (p('luckyExp') === '1' ? '1' : '0'),
                    K_EXP_QUA => (p('expQua') === '1' ? '1' : '0'),
                ];
                luuCauHinh($m);
                ghiNhatKy('sua_cau_hinh', 'exp_phu', json_encode($m, JSON_UNESCAPED_UNICODE));
                veLai($TRANG, 'success', 'Đã lưu. Server áp dụng trong tối đa 10 giây.');
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
$gt = [];      // khoa => gia_tri
$suaBoi = [];  // khoa => [sua_boi, sua_luc]
$loiDoc = '';
try {
    taoBangCauHinhNeuThieu();
    foreach (dbGame()->query('SELECT khoa, gia_tri, sua_boi, sua_luc FROM `' . BANG_CAU_HINH . '`')->fetchAll() as $r) {
        $gt[$r['khoa']] = (string)$r['gia_tri'];
        $suaBoi[$r['khoa']] = [(string)($r['sua_boi'] ?? ''), (string)($r['sua_luc'] ?? '')];
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

/** Giá trị hiện tại của một khoá, hoặc "" nếu chưa có (server chưa khởi động lần nào). */
function giaTri(array $gt, string $k, string $macDinh = ''): string
{
    return array_key_exists($k, $gt) ? $gt[$k] : $macDinh;
}

/** Dòng nhỏ "sửa bởi X lúc Y" dưới một nhóm khoá, lấy mốc mới nhất trong nhóm. */
function veSuaBoi(array $suaBoi, array $khoas): void
{
    $moiNhat = null;
    foreach ($khoas as $k) {
        if (isset($suaBoi[$k]) && $suaBoi[$k][1] !== '' && ($moiNhat === null || $suaBoi[$k][1] > $moiNhat[1])) {
            $moiNhat = $suaBoi[$k];
        }
    }
    if ($moiNhat === null) {
        echo '<p class="muted small">Chưa từng sửa qua CSDL — đang dùng giá trị khởi tạo lần đầu từ server.ini.</p>';
        return;
    }
    echo '<p class="muted small">Sửa lần cuối bởi <strong>' . e($moiNhat[0]) . '</strong> lúc ' . e($moiNhat[1]) . '.</p>';
}

adminHead('cauhinh', 'Cấu hình server');
?>
<div class="page-head">
    <div>
        <h1>Cấu hình server</h1>
        <p>32 setting nạp lại được lúc đang chạy — sửa xong, server tự áp dụng trong tối đa 10 giây, không cần restart.</p>
    </div>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<div class="luuy">
    <p>Đây <strong>không phải toàn bộ</strong> <code>server.ini</code> — chỉ 32 khoá đã có sẵn cơ chế nạp lại
       (xem <code>src/server/CauHinh.java</code>). Các khoá kết nối CSDL, cổng mạng, đường dẫn máy... vẫn phải
       sửa trực tiếp <code>server.ini</code> trên VPS rồi khởi động lại — không nằm ở đây vì bản thân chúng là
       thứ server cần để kết nối được tới CSDL này.</p>
</div>

<div class="card">
    <h3>Tỷ lệ rơi đồ khi train quái</h3>
    <p class="muted small">Phần trăm so với tỷ lệ gốc. 100 = như cũ, 0 = tắt hẳn.</p>
    <form method="post" action="cauhinh.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_drop">
        <div class="form-grid">
            <label class="o"><span>Xu (%)</span>
                <input type="number" name="tileXu" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_XU, '0')) ?>"></label>
            <label class="o"><span>Lượng (%)</span>
                <input type="number" name="tileLuong" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_LUONG, '0')) ?>"></label>
            <label class="o"><span>Đồ sự kiện (%)</span>
                <input type="number" name="tileItemSuKien" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_SUKIEN, '0')) ?>"></label>
            <label class="o"><span>Bình thuốc (%)</span>
                <input type="number" name="tilePotion" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_POTION, '0')) ?>"></label>
            <label class="o"><span>Ngọc (%)</span>
                <input type="number" name="tileNgoc" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_NGOC, '0')) ?>"></label>
            <label class="o"><span>Đồ thường (%)</span>
                <input type="number" name="tileItem" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_ITEM, '0')) ?>"></label>
            <label class="o"><span>Chia số xu rơi ra</span>
                <input type="number" name="chiaXuRoi" min="1" value="<?= e(giaTri($gt, K_CHIA_XU, '10')) ?>">
                <small>Số xu rơi ra = xu gốc chia cho số này. Càng lớn càng ít.</small></label>
        </div>
        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu tỷ lệ rơi đồ</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_TILE_XU, K_TILE_LUONG, K_TILE_SUKIEN, K_TILE_POTION, K_TILE_NGOC, K_TILE_ITEM, K_CHIA_XU]); ?>
</div>

<div class="card">
    <h3>Tỉ lệ EXP quái thường</h3>
    <p class="muted small">Phần trăm so với EXP gốc, tính theo <strong>cấp NHÂN VẬT</strong> đang nhận EXP
       (không phải cấp quái) — dù nhân vật ≥50 đi train ở quái cấp nào cũng bị giảm. Áp dụng cho cả Boss.
       100 = như gốc, 0 = tắt hẳn. Xem <code>Monster.apDungGiamExpTheoCapNV()</code>.</p>
    <form method="post" action="cauhinh.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_exp_quai">
        <div class="form-grid">
            <label class="o"><span>Nhân vật cấp ≥ 50 (%)</span>
                <input type="number" name="tileExpLv50" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_EXP_LV50, '10')) ?>"></label>
            <label class="o"><span>Nhân vật cấp &lt; 50 (%)</span>
                <input type="number" name="tileExpDuoiLv50" min="0" max="100" value="<?= e(giaTri($gt, K_TILE_EXP_DUOI_LV50, '50')) ?>"></label>
        </div>
        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu tỉ lệ EXP quái</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_TILE_EXP_LV50, K_TILE_EXP_DUOI_LV50]); ?>
</div>

<?php
// Doi tu dang luu (nguong cong don 0..100000 / % nguyen / phan nghin) sang dang de nhap
// (% rieng tung mau / he so nhan x.xx / % thay phan nghin) - xem case 'luu_tieu' o tren
// cho chieu nguoc lai.
$tieuNguong = mangTu($gt, K_TIEU_NGUONG_MAU, [60189, 89389, 99689, 99989, 99999]);
$tieuPc = mangTu($gt, K_TIEU_PC_EXP_THEO_MAU, [50, 100, 150, 250, 250, 500, 0]);
$tieuPer = mangTu($gt, K_TIEU_PERMILLE_CAO_CAP, [50, 70, 90, 35, 55, 75, 5, 25, 45]);

$phanTramMauHienThi = [];
$truocDo = 0;
foreach ($tieuNguong as $ng) {
    $phanTramMauHienThi[] = round(($ng - $truocDo) / 1000, 3);
    $truocDo = $ng;
}
$phanTramTim = round((100000 - $truocDo) / 1000, 3);
$heSoChungHienThi = round((float)giaTri($gt, K_TIEU_HE_SO_EXP, '200') / 100, 2);
$heSoMauHienThi = array_map(static fn($v) => round($v / 100, 2), array_slice($tieuPc, 0, 6));

$TEN_MAU = ['Trắng', 'Xanh', 'Đỏ', 'Hoàn Mỹ', 'Vàng'];
$TEN_MAU_DAY_DU = ['Trắng', 'Xanh', 'Đỏ', 'Hoàn Mỹ', 'Vàng', 'Tím'];
$TEN_MAU_CAO_CAP = ['Hoàn Mỹ', 'Vàng', 'Tím'];
$TEN_KHUNG_CAP = ['Cấp 50-59', 'Cấp 60-69', 'Cấp 70+'];
?>
<div class="card">
    <h3>Tỉ lệ Vận Tiêu</h3>
    <p class="muted small">Khi nhận một con tiêu (Vận Tiêu) mới, server random ra 1 trong 6 màu theo % ở
       phần 1 — Trắng phổ biến nhất, Tím hiếm nhất. Khi TRẢ tiêu thành công, người chơi nhận EXP theo
       phần 2 (và phần 3 nếu là Hoàn Mỹ/Vàng/Tím từ cấp 50). Xem <code>Char.doInitMonter()</code>,
       <code>CharSuKien.doResponseTieu()</code>.</p>
    <form method="post" action="cauhinh.php" id="formTieu">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_tieu">

        <h4 style="margin:16px 0 4px;">1. Tỉ lệ ra màu khi nhận tiêu mới</h4>
        <p class="muted small">Nhập % cho 5 màu đầu (được nhập số lẻ, vd <code>0.001</code> để ra tỉ lệ cực
           hiếm) — <strong>Tím không nhập, tự động = phần trăm còn lại</strong> sau khi trừ 5 màu kia.</p>
        <div class="form-grid">
            <?php foreach ($TEN_MAU as $i => $ten): ?>
                <label class="o"><span><?= e($ten) ?> (%)</span>
                    <input type="number" name="tieuMau<?= $i ?>" min="0" max="100" step="0.001"
                           class="jsMauNhap" value="<?= e((string)$phanTramMauHienThi[$i]) ?>"></label>
            <?php endforeach; ?>
            <label class="o"><span>Tím (%) — tự tính</span>
                <input type="text" id="jsMauTim" readonly value="<?= e((string)$phanTramTim) ?>"
                       style="background:var(--muted-bg,#f3f3f3);"></label>
        </div>
        <p class="muted small" style="margin-top:6px;">Tổng Trắng→Vàng: <strong id="jsTongMau"><?= e((string)array_sum($phanTramMauHienThi)) ?></strong>%
           — còn lại cho Tím: <strong id="jsConLaiMau"><?= e((string)$phanTramTim) ?></strong>%.
           <span id="jsCanhBaoMau" style="color:#c00;display:none;">Tổng 5 màu đã vượt 100% — Tím sẽ về 0, lưu lại sẽ báo lỗi.</span></p>

        <h4 style="margin:20px 0 4px;">2. Hệ số EXP khi trả tiêu thành công</h4>
        <p class="muted small">Hệ số dạng "x lần" — 1 = giữ nguyên gốc, 2 = gấp đôi. Hai hệ số NHÂN VỚI
           NHAU: EXP cuối = EXP gốc × hệ số chung × hệ số theo màu. Chỉ áp dụng khi cấp mở tiêu dưới 50,
           hoặc màu Trắng/Xanh/Đỏ (Hoàn Mỹ/Vàng/Tím từ cấp 50 dùng công thức riêng ở phần 3).</p>
        <label class="o" style="max-width:200px;"><span>Hệ số chung (x...)</span>
            <input type="number" name="tieuHeSoChung" min="0" max="10" step="0.01"
                   value="<?= e((string)$heSoChungHienThi) ?>"></label>
        <div class="form-grid" style="margin-top:8px;">
            <?php foreach ($TEN_MAU_DAY_DU as $i => $ten): ?>
                <label class="o"><span>Hệ số <?= e($ten) ?> (x...)</span>
                    <input type="number" name="tieuHeSoMau<?= $i ?>" min="0" max="100" step="0.01"
                           value="<?= e((string)$heSoMauHienThi[$i]) ?>"></label>
            <?php endforeach; ?>
        </div>

        <h4 style="margin:20px 0 4px;">3. % EXP cần để lên cấp (riêng Hoàn Mỹ/Vàng/Tím, từ cấp 50)</h4>
        <p class="muted small">Vd 9.0 = trả tiêu xong được cộng thêm 9.0% lượng EXP cần để lên cấp kế
           tiếp. Chỉ 3 màu hiếm mới dùng bảng này; Trắng/Xanh/Đỏ luôn dùng công thức ở phần 2.</p>
        <table class="table" style="max-width:640px;">
            <thead><tr><th></th><?php foreach ($TEN_MAU_CAO_CAP as $ten): ?><th><?= e($ten) ?> (%)</th><?php endforeach; ?></tr></thead>
            <tbody>
            <?php foreach ($TEN_KHUNG_CAP as $khung => $tenKhung): ?>
                <tr>
                    <td><?= e($tenKhung) ?></td>
                    <?php foreach ($TEN_MAU_CAO_CAP as $mau => $tenMau): ?>
                        <td><input type="number" name="tieuPhanTramCaoCap<?= $khung ?>_<?= $mau ?>" min="0" max="100" step="0.1"
                                   style="max-width:90px;" value="<?= e((string)round($tieuPer[$khung * 3 + $mau] / 10, 1)) ?>"></td>
                    <?php endforeach; ?>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>

        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu tỉ lệ Vận Tiêu</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_TIEU_NGUONG_MAU, K_TIEU_HE_SO_EXP, K_TIEU_PC_EXP_THEO_MAU, K_TIEU_PERMILLE_CAO_CAP]); ?>
    <script>
    // Chi tinh Tim/tong hien thi cho de nhin - server luon tu tinh lai va chan tong > 100%
    // khi luu (case 'luu_tieu'), script nay khong phai chot an toan.
    (function () {
        var oMau = Array.prototype.slice.call(document.querySelectorAll('.jsMauNhap'));
        var oTim = document.getElementById('jsMauTim');
        var oTong = document.getElementById('jsTongMau');
        var oConLai = document.getElementById('jsConLaiMau');
        var oCanhBao = document.getElementById('jsCanhBaoMau');
        function capNhat() {
            var tong = oMau.reduce(function (t, o) { return t + (parseFloat(o.value) || 0); }, 0);
            tong = Math.round(tong * 1000) / 1000;
            var conLai = Math.round((100 - tong) * 1000) / 1000;
            oTong.textContent = String(tong);
            if (conLai < 0) {
                oTim.value = '0';
                oConLai.textContent = '0';
                oCanhBao.style.display = '';
            } else {
                oTim.value = String(conLai);
                oConLai.textContent = String(conLai);
                oCanhBao.style.display = 'none';
            }
        }
        oMau.forEach(function (o) { o.addEventListener('input', capNhat); });
    })();
    </script>
</div>

<div class="card">
    <h3>Giờ chiếm thành</h3>
    <p class="muted small">Để trống giờ = dùng lịch chuẩn 21h thứ 3 và thứ 7. Sự kiện World Cup nay bật/tắt
       ở trang <a href="sukien.php">Sự kiện</a>, không còn ở đây nữa.</p>
    <form method="post" action="cauhinh.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_chiemthanh">
        <div class="form-grid">
            <label class="o"><span>Giờ (0-23, để trống = mặc định)</span>
                <input type="text" name="gioChiemThanh" placeholder="mặc định"
                       value="<?= (int)giaTri($gt, K_GIO_CT, '-1') < 0 ? '' : e(giaTri($gt, K_GIO_CT)) ?>"></label>
            <label class="o"><span>Phút (0-59)</span>
                <input type="number" name="phutChiemThanh" min="0" max="59" value="<?= e(giaTri($gt, K_PHUT_CT, '0')) ?>"></label>
        </div>

        <?php
        $ngayHienTai = giaTri($gt, K_NGAY_CT, '');
        $laNgayCuThe = (bool)preg_match('/^\d{4}-\d{2}-\d{2}$/', $ngayHienTai);
        $thuDaChon = $laNgayCuThe ? [] : array_filter(array_map('trim', explode(',', $ngayHienTai)));
        ?>
        <label class="o" style="margin-top:12px;"><span>Lặp lại vào các thứ trong tuần (không chọn thứ nào = mọi ngày)</span></label>
        <div class="ngang" style="flex-wrap:wrap;gap:14px;">
            <?php foreach (CAC_THU_TRONG_TUAN as $ma => $ten): ?>
                <label style="display:flex;align-items:center;gap:6px;">
                    <input type="checkbox" name="thu_<?= e($ma) ?>" value="1" style="width:auto;"
                           <?= in_array($ma, $thuDaChon, true) ? 'checked' : '' ?>>
                    <?= e($ten) ?>
                </label>
            <?php endforeach; ?>
        </div>

        <label class="o" style="margin-top:12px;max-width:280px;">
            <span>Hoặc một ngày cụ thể (yyyy-MM-dd) — điền vào đây thì bỏ qua các thứ chọn ở trên, chỉ chạy đúng 1 lần</span>
            <input type="text" name="ngayCuThe" placeholder="2026-10-25" value="<?= $laNgayCuThe ? e($ngayHienTai) : '' ?>">
        </label>

        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu giờ chiếm thành</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_GIO_CT, K_PHUT_CT, K_NGAY_CT]); ?>
</div>

<div class="card">
    <h3>Lịch hẹn sự kiện tự khởi động lại</h3>
    <p class="muted small">Khi bật/tắt sự kiện ở trang <a href="sukien.php">Sự kiện</a> cần khởi động lại, server tự
       gom các thay đổi rồi tự restart — không cần SSH. Cấu hình ở đây quyết định cách gom đó.</p>
    <form method="post" action="cauhinh.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_lichhen">
        <div class="form-grid">
            <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;"><span>Tự khởi động lại</span>
                <input type="checkbox" name="tuKhoiDongLai" value="1" style="width:auto;" <?= giaTri($gt, K_SK_TU_KHOI_DONG_LAI, '1') === '1' ? 'checked' : '' ?>>
            </label>
            <label class="o"><span>Gom thay đổi trong (giây, 30-3600)</span>
                <input type="number" name="gomGiay" min="30" max="3600" value="<?= e(giaTri($gt, K_SK_GOM_GIAY, '120')) ?>"></label>
            <label class="o"><span>Báo trước (giây, 10-600)</span>
                <input type="number" name="baoTruocGiay" min="10" max="600" value="<?= e(giaTri($gt, K_SK_BAO_TRUOC_GIAY, '60')) ?>"></label>
        </div>
        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu lịch hẹn sự kiện</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_SK_TU_KHOI_DONG_LAI, K_SK_GOM_GIAY, K_SK_BAO_TRUOC_GIAY]); ?>
</div>

<div class="card">
    <h3>Thưởng EXP phụ</h3>
    <p class="muted small">Trước 11/09/2026 nằm trong <code>server.ini</code> (<code>sv.expvantieu</code>,
       <code>sv.Luckyexp</code>, <code>sv.ExpQua</code>), phải SSH sửa tay + khởi động lại. Nay sửa ở đây,
       server tự áp dụng trong tối đa 10 giây, không cần restart, không cần đụng tới <code>server.ini</code>
       nữa.</p>
    <form method="post" action="cauhinh.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_exp_phu">
        <div class="form-grid">
            <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;">
                <span>EXP giao tiêu cá nhân</span>
                <input type="checkbox" name="expVantieu" value="1" style="width:auto;" <?= giaTri($gt, K_EXP_VANTIEU, '0') === '1' ? 'checked' : '' ?>>
            </label>
            <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;">
                <span>EXP trong Rương May Mắn (Bạc &amp; Vàng)</span>
                <input type="checkbox" name="luckyExp" value="1" style="width:auto;" <?= giaTri($gt, K_LUCKY_EXP, '0') === '1' ? 'checked' : '' ?>>
            </label>
            <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;">
                <span>EXP cây ăn quả</span>
                <input type="checkbox" name="expQua" value="1" style="width:auto;" <?= giaTri($gt, K_EXP_QUA, '0') === '1' ? 'checked' : '' ?>>
            </label>
        </div>
        <div class="luuy" style="margin-top:12px;">
            <p><strong>Tắt "EXP cây ăn quả" còn khoá luôn nút "Tăng tốc" cho MỌI cây ăn quả</strong>
               (kể cả cây ra đá quý/thuốc, không riêng cây EXP) — hiệu ứng phụ có sẵn trong mã nguồn
               (<code>FruitTemplate.canTangToc</code>), không phải do đợt chuyển đổi này gây ra.</p>
        </div>
        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_EXP_VANTIEU, K_LUCKY_EXP, K_EXP_QUA]); ?>
</div>

<div class="card">
    <h3>Khác</h3>
    <form method="post" action="cauhinh.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="viec" value="luu_khac">
        <div class="form-grid">
            <label class="o"><span>Số nhân vật tối đa / tài khoản (1-3)</span>
                <input type="number" name="maxCharPerAccount" min="1" max="3" value="<?= e(giaTri($gt, K_MAX_CHAR, '3')) ?>"></label>
            <label class="o"><span>Tài khoản quản trị trong game (phân cách bằng dấu phẩy)</span>
                <input type="text" name="adminAccounts" maxlength="255" value="<?= e(giaTri($gt, K_ADMIN_ACCOUNTS)) ?>">
                <small>Đây là quyền GM trong game (chat lệnh, bảng setting...) — khác hẳn <code>ADMIN_ACCOUNTS</code>
                       của trang quản trị web này, hai danh sách không liên quan nhau.</small></label>
            <label class="o"><span>Website</span>
                <input type="text" name="website" maxlength="255" value="<?= e(giaTri($gt, K_WEBSITE)) ?>"></label>
            <label class="o"><span>Link tải game</span>
                <input type="text" name="linkTaiGame" maxlength="255" value="<?= e(giaTri($gt, K_LINK_TAI_GAME)) ?>"></label>
            <label class="o"><span>Mã test</span>
                <input type="text" name="testCode" maxlength="50" value="<?= e(giaTri($gt, K_TEST_CODE)) ?>"></label>
        </div>
        <div class="luuy" style="margin-top:12px;">
            <p><strong>Hai cờ dưới đây chỉ nên bật tạm thời rồi tắt lại ngay</strong> — để quên trên server thật
               là lỗ hổng (cho phép tạo nhân vật trùng tên GM / mã test cấp xu-lượng miễn phí).</p>
        </div>
        <div class="form-grid" style="margin-top:8px;">
            <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;"><span>Cho phép tạo tên trùng GM</span>
                <input type="checkbox" name="choPhepTaoTenGM" value="1" style="width:auto;" <?= giaTri($gt, K_TAO_TEN_GM, '0') === '1' ? 'checked' : '' ?>></label>
            <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;"><span>Bật testMode</span>
                <input type="checkbox" name="testMode" value="1" style="width:auto;" <?= giaTri($gt, K_TEST_MODE, '0') === '1' ? 'checked' : '' ?>></label>
        </div>
        <div class="form-actions"><button class="btn btn-primary btn-nho">Lưu</button></div>
    </form>
    <?php veSuaBoi($suaBoi, [K_MAX_CHAR, K_ADMIN_ACCOUNTS, K_WEBSITE, K_LINK_TAI_GAME, K_TEST_MODE, K_TEST_CODE, K_TAO_TEN_GM]); ?>
</div>
<?php adminFoot(); ?>
