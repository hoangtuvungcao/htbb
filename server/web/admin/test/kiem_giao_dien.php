<?php
/**
 * Kiểm thử bộ giao diện khu quản trị (web/admin/assets/admin.css).
 *
 * VÌ SAO CẦN: khu quản trị trước đây nạp kèm assets/style.css của trang giới
 * thiệu và mượn rất nhiều lớp ở đó (.btn, .card, .flash, .auth-card…). Nay
 * admin.css đứng một mình. Một lớp bị bỏ sót sẽ KHÔNG gây lỗi nào cả: trình
 * duyệt lặng lẽ bỏ qua, và chỗ đó chỉ đơn giản là mất định dạng — nút thành
 * chữ xanh gạch chân, thẻ thành khối trắng trơn. Không có dòng lỗi nào để lần.
 *
 * Nên bộ kiểm này quét mọi class="..." trong các trang PHP rồi đối chiếu với
 * danh sách bộ chọn có thật trong admin.css.
 *
 * Chạy: php web/admin/test/kiem_giao_dien.php
 */

$dat = 0;
$hong = 0;

function kt(string $ten, $thucTe, $mongDoi): void
{
    global $dat, $hong;
    if ($thucTe === $mongDoi) {
        $dat++;
        echo "  OK   $ten\n";
    } else {
        $hong++;
        echo "  HONG $ten: duoc " . var_export($thucTe, true)
            . ", can " . var_export($mongDoi, true) . "\n";
    }
}

function dung(string $ten, bool $dk): void
{
    kt($ten, $dk, true);
}

$GOC     = dirname(__DIR__, 3) . DIRECTORY_SEPARATOR;
$fCss    = $GOC . 'web/admin/assets/admin.css';
$fLayout = $GOC . 'web/admin/inc/layout.php';
$trang   = glob($GOC . 'web/admin/*.php') ?: [];
$incs    = glob($GOC . 'web/admin/inc/*.php') ?: [];

// Nạp trước MỌI lệnh echo: boot.php mở session, mà session không mở được sau khi
// đã có nội dung gửi đi.
require_once $GOC . 'web/admin/inc/boot.php';
require_once $fLayout;

echo "== Nguon\n";
dung('doc duoc assets/admin.css', is_file($fCss));
dung('tim thay cac trang admin', count($trang) >= 7);
if (!is_file($fCss)) {
    echo "\nKhong doc duoc admin.css, dung o day.\n";
    exit(1);
}
$css    = file_get_contents($fCss);
$layout = is_file($fLayout) ? file_get_contents($fLayout) : '';

// ---------------------------------------------------------------------------
//  1. Cú pháp CSS tối thiểu: ngoặc cân, không còn dấu ":" lạc chỗ
// ---------------------------------------------------------------------------
echo "\n== Cu phap CSS\n";

$khongChuThich = preg_replace('!/\*.*?\*/!s', '', $css);
kt('so ngoac { va } bang nhau',
    substr_count($khongChuThich, '{'), substr_count($khongChuThich, '}'));

// Mỗi khai báo trong một khối phải kết thúc bằng ";" hoặc là cái cuối cùng.
// Bắt lỗi kiểu "background: #465366: }" — trình duyệt bỏ cả dòng, không báo gì.
$khaiBaoHong = [];
if (preg_match_all('/\{([^{}]*)\}/', $khongChuThich, $khoi)) {
    foreach ($khoi[1] as $than) {
        foreach (explode(';', $than) as $kb) {
            $kb = trim($kb);
            if ($kb === '') {
                continue;
            }
            // Đúng một dấu ":" ngăn tên thuộc tính với giá trị. Nhiều hơn thì chỉ
            // hợp lệ khi nằm trong url(...) hoặc chuỗi — trong tệp này thì không có.
            if (substr_count($kb, ':') !== 1 && strpos($kb, 'url(') === false
                && strpos($kb, 'data:') === false && strpos($kb, '//') === false) {
                $khaiBaoHong[] = $kb;
            }
        }
    }
}
kt('khong co khai bao sai dau ":"', $khaiBaoHong, []);

// ---------------------------------------------------------------------------
//  2. Không còn phụ thuộc bảng màu của trang giới thiệu
// ---------------------------------------------------------------------------
echo "\n== Doc lap voi trang gioi thieu\n";

// Soi thẻ <link> thật, không soi cả tệp: layout.php có nhắc tên style.css trong
// phần chú thích giải thích vì sao bỏ nó, và nhắc tên thì không phải là nạp.
$conNap = [];
foreach (array_merge($trang, $incs) as $f) {
    if (preg_match('/<link[^>]*href=["\'][^"\']*style\.css/i', file_get_contents($f))) {
        $conNap[] = basename($f);
    }
}
kt('khong trang admin nao con the <link> toi style.css', $conNap, []);

// Và phải có ít nhất một nơi nạp admin.css, nếu không thì phép trên đúng một cách
// vô nghĩa (không nạp gì cả cũng đạt).
dung('layout co nap assets/admin.css', strpos($layout, "tepTinh('assets/admin.css')") !== false);

// Dau phien ban chong bo nho dem cu. Thieu no thi doi giao dien xong, nguoi dung
// van nhan CSS cu tu bo nho dem trong khi HTML da la ban moi -> trang tran trui.
echo "\n== Chong bo nho dem cu\n";

dung('co ham tepTinh() gan dau phien ban', strpos($layout, 'function tepTinh(') !== false);
dung('dau phien ban lay tu thoi diem sua tep', strpos($layout, 'filemtime(') !== false);
dung('the <link> di qua tepTinh() chu khong ghi cung duong dan',
    !preg_match('/<link[^>]*href="assets\/admin\.css"/', $layout));

// Chay that: phai ra dung dang "assets/admin.css?v=<so>".
$duong = tepTinh('assets/admin.css');
dung('tepTinh tra ve co dau ?v=', (bool)preg_match('#^assets/admin\.css\?v=\d+$#', $duong));
kt('dau phien ban dung bang mtime cua chinh tep do',
    $duong, 'assets/admin.css?v=' . filemtime($fCss));
kt('tep khong ton tai thi tra ve duong dan tran, khong vo',
    tepTinh('assets/khong-co-tep-nay.css'), 'assets/khong-co-tep-nay.css');

// admin.css phải tự khai báo biến màu, không mượn của nơi khác.
foreach (['--bg', '--surface', '--line', '--text', '--accent', '--ok', '--warn', '--danger'] as $bien) {
    dung("admin.css tu dinh nghia $bien", strpos($css, $bien . ':') !== false);
}
dung('khong con bien mau vang cua trang gioi thieu (--gold)',
    strpos($css, '--gold') === false);

// ---------------------------------------------------------------------------
//  3. Mọi lớp các trang dùng đều có trong admin.css
// ---------------------------------------------------------------------------
echo "\n== Lop nao duoc dung thi phai co dinh nghia\n";

/** Rút mọi tên lớp tĩnh trong class="..." của một tệp PHP. */
function lopTrongTep(string $noiDung): array
{
    $ra = [];
    if (!preg_match_all('/class="([^"]*)"/', $noiDung, $m)) {
        return $ra;
    }
    foreach ($m[1] as $chuoi) {
        // Bỏ mọi đoạn mã PHP nhúng giữa class="..." (lớp động) — chúng được liệt
        // kê tay ở $lopDong bên dưới. Lưu ý: KHÔNG viết dấu đóng thẻ PHP trong
        // chú thích "//", vì nó thật sự đóng thẻ và cắt đôi tệp này.
        $chuoi = preg_replace('/<\?.*?\?' . '>/s', ' ', $chuoi);
        foreach (preg_split('/\s+/', trim($chuoi)) as $lop) {
            if ($lop !== '' && preg_match('/^[a-z][a-z0-9_-]*$/i', $lop)) {
                $ra[] = $lop;
            }
        }
    }
    return $ra;
}

// Lớp sinh ra trong mã PHP chứ không viết thẳng trong class="..." — phải liệt kê
// bằng tay, vì không quét được.
$lopDong = [
    'active',                                        // mục menu đang mở
    'btn-primary', 'btn-xam', 'btn-do',              // nút đổi theo trạng thái
    'nhan-ok', 'nhan-khoa', 'nhan-cho', 'nhan-mo',   // mauViec(), $NHAN_MUC
    'flash-success', 'flash-error',                  // takeFlash()
    'tot', 'canh-bao', 'nguy-hiem',                  // tham số $mau của theSo()
    'num',                                           // cotSo() trong log.php
    'admin-body--auth',                              // thân trang lúc chưa đăng nhập
];

$dung = [];
foreach (array_merge($trang, $incs) as $f) {
    foreach (lopTrongTep(file_get_contents($f)) as $lop) {
        $dung[$lop] = true;
    }
}
foreach ($lopDong as $lop) {
    $dung[$lop] = true;
}
$dung = array_keys($dung);
sort($dung);
echo "  (quet duoc " . count($dung) . " ten lop trong cac trang)\n";

$thieu = [];
foreach ($dung as $lop) {
    if (!preg_match('/\.' . preg_quote($lop, '/') . '\b/', $css)) {
        $thieu[] = $lop;
    }
}
kt('khong lop nao bi bo quen khi bo style.css', $thieu, []);

// ---------------------------------------------------------------------------
//  4. Những thứ bố cục mới bắt buộc phải có
// ---------------------------------------------------------------------------
echo "\n== Bo cuc moi\n";

dung('layout dung khung cot trai + cot noi dung', strpos($layout, 'admin-shell') !== false);
dung('menu chia nhom', strpos($layout, 'side-group') !== false);
dung('moi muc menu co bieu tuong nhung thang (khong tai anh ngoai)',
    strpos($layout, 'bieuTuong(') !== false && strpos($layout, '<svg') !== false);
// Trừ xmlns của SVG — đó là định danh không gian tên do chuẩn quy định, trình
// duyệt không hề tải gì từ địa chỉ đó.
$layoutMang = str_replace("http://www.w3.org/2000/svg", '', $layout);
dung('khong tai anh/font/bieu tuong nao tu Internet',
    strpos($layoutMang, 'http://') === false && strpos($layoutMang, 'https://') === false);
dung('admin.css cung khong tai gi tu Internet',
    strpos($css, 'http://') === false && strpos($css, 'https://') === false
    && strpos($css, '@import') === false);

// Khoá menu phải khớp tham số $trang từng trang truyền vào, nếu không sẽ không
// mục nào sáng lên và người dùng mất dấu mình đang ở đâu.
$khoaMenu = [];
foreach (adminMenu() as $muc) {
    foreach ($muc as $khoa => $m) {
        $khoaMenu[$khoa] = $m[0];
    }
}
foreach ($trang as $f) {
    $noiDung = file_get_contents($f);
    if (!preg_match("/adminHead\(\s*'([a-z]+)'/", $noiDung, $m)) {
        continue;   // dangxuat.php không vẽ trang
    }
    $khoa = $m[1];
    dung(basename($f) . " dung khoa menu co that (\"$khoa\")", isset($khoaMenu[$khoa]));
    if (isset($khoaMenu[$khoa])) {
        kt(basename($f) . ' la trang menu tro toi', $khoaMenu[$khoa], basename($f));
    }
}

// Biểu tượng: tên nào menu gọi thì bieuTuong() phải vẽ được.
foreach (adminMenu() as $muc) {
    foreach ($muc as $m) {
        dung('co bieu tuong "' . $m[2] . '"', bieuTuong($m[2]) !== '');
    }
}
dung('ten bieu tuong khong co thi tra ve rong chu khong vo', bieuTuong('khongcothat') === '');

// Kích thước ghi thẳng vào thẻ <svg>, không chỉ trông vào CSS: một SVG không có
// kích thước gốc sẽ phình ra bằng cả khung chứa nếu admin.css lỡ không nạp được.
dung('bieu tuong co width/height ghi thang trong the',
    (bool)preg_match('/<svg width="16" height="16"/', bieuTuong('bang')));

// Số liệu phải hiện nhãn TRƯỚC con số.
ob_start();
theSo('Tài khoản', '1.284', '+9 hôm nay');
$html = ob_get_clean();
dung('the so: nhan dung truoc con so',
    strpos($html, 'stat-nhan') < strpos($html, 'stat-so'));
dung('the so van in du ghi chu', strpos($html, '+9 hôm nay') !== false);

// ---------------------------------------------------------------------------
echo "\n============================================================\n";
if ($hong === 0) {
    echo "  DAT: $dat/$dat kiem tra\n";
} else {
    echo "  HONG: $hong/" . ($dat + $hong) . " kiem tra that bai\n";
}
echo "============================================================\n";
exit($hong === 0 ? 0 : 1);
