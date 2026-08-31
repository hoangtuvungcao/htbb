<?php
/**
 * Kiểm thử danh mục + bộ chọn vật phẩm của trang Giftcode.
 *
 * Chạy KHÔNG cần MySQL: chỉ kiểm phần thuần (không gọi dmDanhMuc, vì hàm đó truy
 * vấn data_item / data_potion).
 *
 * PHÉP KIỂM QUAN TRỌNG NHẤT ở cuối tệp: mọi chuỗi bộ chọn CÓ THỂ sinh ra đều phải
 * qua được `kiemTraItem()`. Bộ chọn ghép chuỗi bằng JavaScript rồi chèn vào ô văn
 * bản; nếu nó ghép thiếu một phần thì người tạo mã thấy chuỗi trông đúng, bấm lưu,
 * và server sẽ ném lỗi GIỮA lúc phát quà — người chơi nhận được nửa số quà trong
 * khi dòng giftcode_log và việc trừ lượt đều chưa chạy, tức nhận lại được vô hạn.
 *
 * Chạy: php web/admin/test/kiem_danh_muc_thuong.php
 */
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

require_once __DIR__ . '/../inc/thuong.php';
require_once __DIR__ . '/../inc/danh_muc_thuong.php';

$GOC = dirname(__DIR__, 3) . '/';
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
           . ", mong doi " . var_export($mongDoi, true) . "\n";
    }
}

function ktDung(string $ten, bool $thucTe): void
{
    kt($ten, $thucTe, true);
}

// ===========================================================================
echo "\n== Gop ten xuong dong thanh mot dong\n";
// ===========================================================================
// Cot `name` cua game nay vua la ten vua la MO TA, xuong dong bang ky tu that.
// De nguyen thi the <option> dut lam nhieu dong.
kt('xuong dong -> dau giua',  dmMotDong("Skin linh thú\n[hệ Kim]"), 'Skin linh thú · [hệ Kim]');
kt('xuong dong kieu Windows', dmMotDong("A\r\nB"), 'A · B');
kt('gop nhieu khoang trang',  dmMotDong("A     B"), 'A B');
kt('cat khoang trang hai dau', dmMotDong("  A  "), 'A');
kt('null -> chuoi rong',      dmMotDong(null), '');

// ===========================================================================
echo "\n== Danh muc ngoc (tep sinh tu Java)\n";
// ===========================================================================
$ngoc = danhMucNgoc();
ktDung('doc duoc danh muc ngoc', count($ngoc) > 200);
kt('co dung 270 vien', count($ngoc), 270);

// Moi dong phai co du ba khoa, khong thi bo chon vo khi doc.
$duKhoa = true;
$tenRong = 0;
foreach ($ngoc as $id => $g) {
    if (!is_array($g) || !array_key_exists('ten', $g)
        || !array_key_exists('ban', $g) || !array_key_exists('nhom', $g)) {
        $duKhoa = false;
        break;
    }
    if (trim((string)$g['ten']) === '') {
        $tenRong++;
    }
}
ktDung('moi dong co du ten/ban/nhom', $duKhoa);
kt('khong dong nao ten rong', $tenRong, 0);

// 12 nhom nguyen lieu, moi nhom dung 5 he. Day la nhom nguoi van hanh goi bang ten
// (NLSC 6 / NLCC 6) va la ly do chinh co bo chon nay.
$demNhom = [];
foreach ($ngoc as $id => $g) {
    if ($g['nhom'] !== '') {
        $demNhom[$g['nhom']] = ($demNhom[$g['nhom']] ?? 0) + 1;
    }
}
kt('co dung 12 nhom nguyen lieu', count($demNhom), 12);
for ($c = 1; $c <= 6; $c++) {
    kt("NLSC$c co 5 he", $demNhom['NLSC' . $c] ?? 0, 5);
    kt("NLCC$c co 5 he", $demNhom['NLCC' . $c] ?? 0, 5);
}

// NLSC6 / NLCC6 phai dung id ma ID_MATERIAL_LOW/HIGHT cap cuoi ghi trong Java.
// Ghi cung o day la co y: doi mang ben Java ma quen chay lai tools/SinhDanhMucNgoc
// thi phep kiem nay do, chu khong am tham lech.
$idNlsc6 = [];
$idNlcc6 = [];
foreach ($ngoc as $id => $g) {
    if ($g['nhom'] === 'NLSC6') { $idNlsc6[] = $id; }
    if ($g['nhom'] === 'NLCC6') { $idNlcc6[] = $id; }
}
sort($idNlsc6);
sort($idNlcc6);
kt('NLSC6 dung id 73,80,87,94,101',    $idNlsc6, [73, 80, 87, 94, 101]);
kt('NLCC6 dung id 108,115,122,129,136', $idNlcc6, [108, 115, 122, 129, 136]);

// Nguyen lieu CAP CAO NHAT khong ban trong cua hang - do dung la nhom chi phat duoc
// bang giftcode, va la nhom nguoi dung hoi ten dau tien (NLSC6 / NLCC6).
//
// KHONG doi hoi moi cap deu khong ban: doi chieu du lieu that thi NLCC4 va NLCC5 CO
// ban o shop (10 vien). Phep kiem dau tien o day viet la "khong vien nao ban" va no
// do - du lieu dung, phep kiem sai. Giu lai ghi chu nay de khong ai "sua" nguoc lai.
$capCaoBan = 0;
foreach ($ngoc as $g) {
    if (($g['nhom'] === 'NLSC6' || $g['nhom'] === 'NLCC6') && $g['ban']) {
        $capCaoBan++;
    }
}
kt('khong vien NLSC6/NLCC6 nao ban o shop', $capCaoBan, 0);

// ===========================================================================
echo "\n== Loai bo do che o tho ren than bi\n";
// ===========================================================================
$boDi = dmLoaiTypeDoChe();
kt('bo dung 18 type', count($boDi), 18);
// Vu khi 3..7 (Map.isWeapone), trang bi thu 14..18 (Map.isWearingAnimal),
// va ao/quan/non + nhan/day/giay/gang + ngoc trang bi.
foreach ([3, 4, 5, 6, 7] as $t) {
    ktDung("vu khi type $t bi bo", in_array($t, $boDi, true));
}
foreach ([14, 15, 16, 17, 18] as $t) {
    ktDung("trang bi thu type $t bi bo", in_array($t, $boDi, true));
}
foreach ([0, 1, 2, 8, 9, 10, 11, 12] as $t) {
    ktDung("trang bi type $t bi bo", in_array($t, $boDi, true));
}

// VA QUAN TRONG HON: nhung type bo chon SONG BANG thi KHONG duoc nam trong danh
// sach bo. Them nham mot so vao day la ca mot nhom bien mat khoi bo chon ma trang
// van chay binh thuong - khong ai phat hien tru khi di tim mon do.
foreach ([
    13 => 'cuoc/choi',
    19 => 'phi phong',
    21 => 'ho ve lenh + trung linh thu',
    25 => 'hoa ky lan',
    26 => 'trung thu',
    27 => 'thu nuoi',
    28 => 'dan/thao duoc',
    29 => 'manh cai trang',
    30 => 'skin linh thu',
] as $t => $ten) {
    ktDung("type $t ($ten) KHONG bi bo", !in_array($t, $boDi, true));
}

// Ten nhom phai phu het cac type con lai, khong thi nhan hien ra thanh "type 23".
$tenLoai = dmTenLoaiVatPham();
$thieuTen = [];
for ($t = 0; $t <= 30; $t++) {
    if (!in_array($t, $boDi, true) && !isset($tenLoai[$t])) {
        $thieuTen[] = $t;
    }
}
kt('moi type con lai deu co ten tieng Viet', $thieuTen, []);

// ===========================================================================
echo "\n== So o thuoc khop giua PHP va Java\n";
// ===========================================================================
// PT ghi THANG vao mang: player.potions[id] += soLuong, khong kiem bien. Mot id
// ngoai dai lam server nem ArrayIndexOutOfBounds GIUA luc phat qua.
$charJava = @file_get_contents($GOC . 'src/real/Char.java');
if ($charJava === false) {
    echo "  (bo qua: khong doc duoc src/real/Char.java)\n";
} else {
    if (preg_match('/SO_O_THUOC\s*=\s*(\d+)/', $charJava, $m)) {
        kt('DM_SO_O_THUOC khop Char.SO_O_THUOC', DM_SO_O_THUOC, (int)$m[1]);
    } else {
        echo "  (bo qua: khong tim thay hang so SO_O_THUOC)\n";
    }
}

// ===========================================================================
echo "\n== Truong cua tung loai phan thuong\n";
// ===========================================================================
kt('ITEM co 1 truong (so luong)', count(dmTruongCuaLoai('ITEM')), 1);
kt('PT co 1 truong (so luong)',   count(dmTruongCuaLoai('PT')), 1);
kt('GEM co 2 truong (so luong + khoa)', count(dmTruongCuaLoai('GEM')), 2);
kt('loai la -> khong co truong nao', dmTruongCuaLoai('KHONGCO'), []);

// CHINH SACH: qua giftcode la qua khoa. GEM la loai duy nhat cho chon, nen mac
// dinh cua no phai la khoa.
$gem = dmTruongCuaLoai('GEM');
$oKhoa = null;
foreach ($gem as $t) {
    if ($t['ma'] === 'khoa') { $oKhoa = $t; }
}
ktDung('GEM co o khoa', $oKhoa !== null);
kt('GEM mac dinh KHOA (-1)', $oKhoa['macDinh'] ?? null, '-1');
ktDung('o khoa la o chon, khong phai o so tu do', isset($oKhoa['chon']));
ktDung('o khoa co ca hai lua chon', count($oKhoa['chon'] ?? []) === 2);

// ITEM khoa san ben Java nen KHONG duoc co o khoa - co thi nguoi dung tuong minh
// dieu khien duoc, ma thuc te khong.
$coKhoaItem = false;
foreach (dmTruongCuaLoai('ITEM') as $t) {
    if ($t['ma'] === 'khoa') { $coKhoaItem = true; }
}
ktDung('ITEM KHONG co o khoa (Java khoa san)', !$coKhoaItem);

// Doi chieu voi chinh ma nguon Java: createItemSoLuong phai con dat lock = 1.
$khoaSan = strpos((string)$charJava, 'newItem.lock = 1') !== false;
ktDung('Char.createItemSoLuong van khoa san (newItem.lock = 1)', $khoaSan);

// ===========================================================================
echo "\n== MOI CHUOI BO CHON SINH RA DEU PHAI QUA kiemTraItem()\n";
// ===========================================================================
// Day la phep kiem chinh. Dung lai dung phep ghep chuoi cua JavaScript:
//   phan = [loai, id, ...gia tri cac truong]  ->  noi bang dau hai cham
$bangLoai = loaiThuong();
foreach (['ITEM', 'PT', 'GEM'] as $loai) {
    $phan = [$loai, '848'];
    foreach (dmTruongCuaLoai($loai) as $t) {
        $phan[] = $t['macDinh'];
    }
    $chuoi = implode(':', $phan);

    [$chuan, $loi] = kiemTraItem($chuoi);
    kt("$loai: '$chuoi' khong loi", $loi, []);
    kt("$loai: chuoi khong bi doi khi chuan hoa", $chuan, $chuoi);

    // Va phai du so phan toi thieu server doi hoi.
    $toiThieu = $bangLoai[$loai][0];
    ktDung("$loai: co du $toiThieu phan (dang co " . count($phan) . ")",
        count($phan) >= $toiThieu);
}

// GEM phai luon co DU BON PHAN. Thieu phan thu tu thi ben Java parseInt nem loi va
// khoi catch dat khoa = true - ket qua tinh co dung chinh sach, nhung hanh vi den
// tu mot khoi bat loi thi doi nghia ngay lan ai do sua cho do.
$phanGem = ['GEM', '108'];
foreach (dmTruongCuaLoai('GEM') as $t) {
    $phanGem[] = $t['macDinh'];
}
kt('GEM sinh ra dung 4 phan', count($phanGem), 4);
kt('GEM cho ngoc NLCC6 khoa', implode(':', $phanGem), 'GEM:108:1:-1');

// Nhieu phan thuong noi bang dau phay cung phai qua duoc.
$nhieu = 'ITEM:684:1,PT:70:1,GEM:108:5:-1';
[$chuanNhieu, $loiNhieu] = kiemTraItem($nhieu);
kt('ba phan thuong noi bang dau phay: khong loi', $loiNhieu, []);
kt('va khong bi doi', $chuanNhieu, $nhieu);

// Chuoi bo chon KHONG BAO GIO duoc sinh ra ky tu ngoai bang chu cho phep - ham
// kiemTraItem tu choi thang ca chuoi neu co, va loi do rat kho hieu voi nguoi dung.
foreach (dmTruongCuaLoai('GEM') as $t) {
    ktDung("gia tri mac dinh '{$t['macDinh']}' hop le trong chuoi",
        preg_match('/^-?[0-9]+$/', $t['macDinh']) === 1);
}

// ===========================================================================
echo "\n== Ma nguon: bo chon khong duoc thanh duong di thu hai\n";
// ===========================================================================
$gc = (string)@file_get_contents($GOC . 'web/admin/giftcode.php');
ktDung('trang van goi kiemTraItem truoc khi luu', strpos($gc, 'kiemTraItem(') !== false);
ktDung('o van ban van con ten truong "item"', strpos($gc, 'name="item"') !== false);
// Neu bo chon gui truong rieng cua no len server thi se co hai duong vao cung mot
// cot, va som muon hai ben lech luat kiem tra.
ktDung('bo chon KHONG co truong name= nao gui len server',
    strpos($gc, 'name="bcNhom"') === false && strpos($gc, 'name="bcMuc"') === false);
// Danh muc phai nhung san, khong goi AJAX - mot endpoint moi phai tu lo requireAdmin
// va CSRF, doi lay dung mot lan tai nhe hon.
//
// Chi quet TRONG khoi <script>: ca trang co nhieu cho goi $st->fetch() cua PDO, quet
// ca tep thi bat nham chinh cac cau truy van CSDL binh thuong (phep kiem dau tien o
// day da do vi ly do do).
$js = '';
if (preg_match_all('#<script>(.*?)</script>#s', $gc, $mJs)) {
    $js = implode("\n", $mJs[1]);
}
ktDung('co doc duoc khoi script', $js !== '');
ktDung('JS khong goi fetch()', strpos($js, 'fetch(') === false);
ktDung('JS khong goi XMLHttpRequest', strpos($js, 'XMLHttpRequest') === false);
ktDung('du lieu nhung bang json_encode co JSON_HEX_TAG',
    strpos($gc, 'JSON_HEX_TAG') !== false);

$dmFile = (string)@file_get_contents($GOC . 'web/admin/inc/danh_muc_thuong.php');
ktDung('danh muc thuoc chi lay id trong dai hop le',
    strpos($dmFile, 'DM_SO_O_THUOC') !== false);
ktDung('danh muc thuoc bo dong khong ten',
    strpos($dmFile, "name <> ") !== false);

// ===========================================================================
echo "\n===================================\n";
if ($hong === 0) {
    echo "DAT: $dat kiem tra\n";
    exit(0);
}
echo "DAT: $dat   HONG: $hong\n";
exit(1);
