<?php
/**
 * Kiểm thử bộ đọc vật phẩm (web/admin/inc/vatpham.php).
 *
 * VÌ SAO CẦN: chuỗi vật phẩm là **33 trường ngăn nhau bằng dấu phẩy, không có tên**.
 * PHP đọc theo VỊ TRÍ. Nếu ai đó thêm một trường vào GIỮA `Item.getDBInfo()` bên Java,
 * mọi vị trí phía sau dịch đi một ô — và trang admin sẽ hiện sai TOÀN BỘ: nhầm màu,
 * nhầm cấp, nhầm khoá, nhầm tinh luyện. Không có lỗi nào cả, chỉ là số nào cũng sai.
 *
 * Nên phần quan trọng nhất của bộ kiểm này KHÔNG phải là mấy phép tách chuỗi, mà là
 * đối chiếu từng hằng số VP_* với chính `Item.initInfoFromDB()` trong mã Java.
 *
 * Chạy: php web/admin/test/kiem_vatpham.php
 */

require_once __DIR__ . '/../inc/vatpham.php';

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

/** Dựng một chuỗi dbInfo 33 trường, rồi đặt giá trị vào đúng vị trí muốn thử. */
function dbInfo(array $datVao): string
{
    $t = array_fill(0, 33, '0');
    $t[19] = '2024-01-01';   // dateCreate là chuỗi
    $t[15] = '';             // charSeal là chuỗi
    foreach ($datVao as $i => $v) {
        $t[$i] = (string)$v;
    }
    return implode(',', $t);
}

/** Một vật phẩm hoàn chỉnh: dbInfo | thuộc tính. */
function vatPham(array $datVao): string
{
    return dbInfo($datVao) . '|' . implode(',', array_fill(0, 70, '0'));
}

// ---------------------------------------------------------------------------
echo "== Chuoi rong / hong\n";

kt('null -> mang rong', docChuoiVatPham(null), []);
kt('chuoi rong -> mang rong', docChuoiVatPham(''), []);
kt('chi co dau > -> mang rong', docChuoiVatPham('>'), []);
kt('rac khong co dau phay -> bo qua', docChuoiVatPham('abc'), []);
kt('thieu ca id template -> bo qua', docChuoiVatPham('5'), []);

// Một mẩu hỏng KHÔNG được làm mất những mẩu lành khác.
$hh = docChuoiVatPham(vatPham([1 => 100]) . '>rac>' . vatPham([1 => 200]));
kt('mau hong o giua khong lam mat mau lanh', count($hh), 2);
kt('  ... va van dung thu tu', [$hh[0]['template'], $hh[1]['template']], [100, 200]);

// ---------------------------------------------------------------------------
echo "\n== Dau > o dau chuoi\n";

// Xảy ra khi nhân vật không có gì trong túi nhưng có thời trang đang mặc.
$d = docChuoiVatPham('>' . vatPham([1 => 777]));
kt('cat dau > o dau roi van doc duoc', count($d), 1);
kt('  ... dung id', $d[0]['template'], 777);

// ---------------------------------------------------------------------------
echo "\n== Doc dung tung truong\n";

$v = docChuoiVatPham(vatPham([
    VP_TEMPLATE     => 314,
    VP_PLACE        => 0,
    VP_PLUS         => 12,
    VP_LEVEL        => 70,
    VP_COLOR        => 4,
    VP_LOCK         => 1,
    VP_TINH_LUYEN   => 88,
    VP_MINUTE_EXIST => 4320,
]))[0];

kt('template', $v['template'], 314);
kt('plus', $v['plus'], 12);
kt('level', $v['level'], 70);
kt('mau', $v['mau'], 4);
kt('khoa', $v['khoa'], true);
kt('tinh luyen', $v['tinh_luyen'], 88);
kt('thue phut', $v['thue_phut'], 4320);
kt('ngay tao', $v['ngay_tao'], '2024-01-01');

$v0 = docChuoiVatPham(vatPham([VP_TEMPLATE => 1]))[0];
kt('lock = 0 thi khong khoa', $v0['khoa'], false);

// ---------------------------------------------------------------------------
echo "\n== Tach tui va thoi trang (place == 4)\n";

$hon = vatPham([VP_TEMPLATE => 10, VP_PLACE => 0]) . '>'
     . vatPham([VP_TEMPLATE => 20, VP_PLACE => 4]) . '>'
     . vatPham([VP_TEMPLATE => 30, VP_PLACE => 0]) . '>'
     . vatPham([VP_TEMPLATE => 40, VP_PLACE => 4]);
$t = tachTuiVaThoiTrang($hon);

kt('do trong tui', array_column($t['tui'], 'template'), [10, 30]);
kt('thoi trang dang mac', array_column($t['thoi_trang'], 'template'), [20, 40]);
kt('null -> hai mang rong', $t = tachTuiVaThoiTrang(null), ['tui' => [], 'thoi_trang' => []]);

// ---------------------------------------------------------------------------
echo "\n== Ten mau\n";

kt('0 = Trang', tenMauVatPham(0), 'Trắng');
kt('1 = Xanh la', tenMauVatPham(1), 'Xanh lá');
kt('5 = Vang', tenMauVatPham(5), 'Vàng');
dung('mau la khong nem loi', tenMauVatPham(99) !== '');

// ---------------------------------------------------------------------------
//  PHẦN QUAN TRỌNG NHẤT: vị trí trường phải khớp với mã Java
// ---------------------------------------------------------------------------
echo "\n== Doi chieu vi tri truong voi Item.initInfoFromDB() ben Java\n";

$fItem = dirname(__DIR__, 3) . '/src/real/Item.java';
dung('doc duoc src/real/Item.java', is_file($fItem));

if (is_file($fItem)) {
    $java = file_get_contents($fItem);
    // Chỉ lấy thân hàm initInfoFromDB — đó là nơi định nghĩa thứ tự thật.
    $than = '';
    if (preg_match('/public void initInfoFromDB\(\).*?\n    \}/s', $java, $m)) {
        $than = $m[0];
    }
    dung('tim thay ham initInfoFromDB', $than !== '');

    // this.place = Byte.parseByte(data[2]);   /   this.dateCreate = data[19];
    $viTri = [];
    if (preg_match_all('/this\.(\w+)\s*=\s*(?:\w+\.parse\w+\()?\s*data\[(\d+)\]/', $than, $mm, PREG_SET_ORDER)) {
        foreach ($mm as $k) {
            $viTri[$k[1]] = (int)$k[2];
        }
    }
    // template đọc vào biến cục bộ chứ không phải this.<ten>
    if (preg_match('/itemTemplate\s*=\s*\w+\.parse\w+\(data\[(\d+)\]\)/', $than, $mt)) {
        $viTri['template'] = (int)$mt[1];
    }

    dung('rut duoc vi tri cac truong tu Java (' . count($viTri) . ' truong)', count($viTri) >= 20);

    $doiChieu = [
        'template'   => VP_TEMPLATE,
        'place'      => VP_PLACE,
        'durable'    => VP_DURABLE,
        'mDurable'   => VP_MDURABLE,
        'plus'       => VP_PLUS,
        'level'      => VP_LEVEL,
        'minuteExist' => VP_MINUTE_EXIST,
        'colorName'  => VP_COLOR,
        'lock'       => VP_LOCK,
        'dateCreate' => VP_DATE_CREATE,
        'TinhLuyen'  => VP_TINH_LUYEN,
        'clazz'      => VP_CLAZZ,
    ];
    foreach ($doiChieu as $tenJava => $viTriPhp) {
        if (!isset($viTri[$tenJava])) {
            kt("Java co truong $tenJava", false, true);
            continue;
        }
        kt("vi tri $tenJava khop voi Java", $viTri[$tenJava], $viTriPhp);
    }

    // place == 4 nghĩa là thời trang — quy tắc lấy từ CharTrangBi, phải còn nguyên ở đó.
    $fTrangBi = dirname(__DIR__, 3) . '/src/real/CharTrangBi.java';
    if (is_file($fTrangBi)) {
        dung('CharTrangBi van phan biet thoi trang bang place == 4',
            strpos(file_get_contents($fTrangBi), 'item.place == 4') !== false);
    }
}

// ---------------------------------------------------------------------------
echo "\n============================================================\n";
if ($hong === 0) {
    echo "  DAT: $dat/$dat kiem tra\n";
} else {
    echo "  HONG: $hong/" . ($dat + $hong) . " kiem tra that bai\n";
}
echo "============================================================\n";
exit($hong === 0 ? 0 : 1);
