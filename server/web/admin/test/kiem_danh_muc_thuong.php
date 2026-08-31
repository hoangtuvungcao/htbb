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
echo "\n== Cot 'type' KHONG khop hanh vi that - ba truong hop da xac minh\n";
// ===========================================================================
// Ca ba deu tim ra tu vong ra soat sau khi nguoi dung hoi "sao Skin thu lai nam
// trong Phi phong". Chung cung mot dang loi: id do that trong data_item, dung
// type do that, nhung ma nguon Java xu ly id do khac han y nghia cua type.

// ---- 950-954: type 19 (Phi phong) nhung ten la "Skin ..." ----
// KHONG phai vo dung nhu doan dau tien tuong: style=0 giong het "Phi phong Lang
// Van" (id 584) nen no chay duoc nhu mot phi phong THUONG. Van phai loai vi ten
// hua doi hinh thu cuoi ma no khong lam.
$rac = dmIdRac();
kt('dmIdRac dung 6 id (5 skin gia + 1 gay giang sinh)', count($rac), 6);
foreach ([950, 951, 952, 953, 954] as $id) {
    ktDung("id $id (Skin gia trong Phi phong) nam trong danh sach loai", in_array($id, $rac, true));
}

// ---- 728: type 27 (Thu nuoi) nhung la vu khi le hoi "Gay Giang Sinh" ----
// Moi thu nuoi that deu co atb0=5 (doi chieu 727,790,802,832,873); rieng 728 co
// atb0=atb1=0. Item.java co hang so GAY_GIANG_SINH=728 va nhanh "case 728" rieng,
// khong doc gi tu nhanh xu ly thu nuoi ca.
ktDung('id 728 (Gay Giang Sinh, vu khi le hoi) nam trong danh sach loai',
    in_array(728, $rac, true));

// ---- 898-902: type 29 (chung voi Manh cai trang) nhung la "Canh" - he rieng ----
// Item.isCanh() = id 898..902, KHONG lien quan cot type. Xac nhan bang so sanh
// hai file (Item.java + danh_muc_thuong.php) doc doc lap, tranh chep lai dung
// mot con so sai tu ca hai noi.
$idItemJava = (string)@file_get_contents($GOC . 'src/real/Item.java');
ktDung('doc duoc src/real/Item.java', $idItemJava !== '');
ktDung('Item.isCanh() dung dai 898..902',
    (bool)preg_match('/id\s*>=\s*898\s*&&.*id\s*<=\s*902/s', $idItemJava));
kt('dmIdCanh khop dung 5 id 898-902', dmIdCanh(), [898, 899, 900, 901, 902]);

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
foreach (['ITEM', 'PT', 'GEM', 'TT', 'PET', 'PETV', 'TK', 'TKMAX', 'DH'] as $loai) {
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
echo "\n== Mo rong dot hai: TT / PET / PETV / TK / TKMAX / DH\n";
// ===========================================================================

// ---- TT: Char.createThoiTrang(p, id, soNgay, khoa) ----
$tt = dmTruongCuaLoai('TT');
kt('TT co dung 3 truong (sl, ngay, khoa)', count($tt), 3);
kt('TT truong 2 la "ngay", macDinh 0 (vinh vien)', $tt[1]['ma'] ?? null, 'ngay');
kt('TT truong ngay cho phep minSo=0 (khong bi kep len 1)', $tt[1]['minSo'] ?? null, '0');
kt('TT truong 3 la "khoa", macDinh 1 (khoa - dung chinh sach)', $tt[2]['macDinh'] ?? null, '1');
ktDung('TT khoa la o chon co ca hai lua chon', isset($tt[2]['chon']) && count($tt[2]['chon']) === 2);
// TT dung quy uoc NGUOC voi GEM: 1 = khoa (gan thang newItem.lock), khong phai
// so am nhu GEM. Neu ai do "sua cho giong GEM" se doi chinh sach cho ca nhom
// Thoi trang lan Canh ma khong biet.
ktDung('TT: gia tri "khoa" trong chon la 1 (KHAC quy uoc am cua GEM)',
    isset($tt[2]['chon']['1']) && !isset($tt[2]['chon']['-1']));

// ---- PET / PETV: id la CHI SO MANG petTemplate, khong phai id vat pham ----
$pet = dmTruongCuaLoai('PET');
kt('PET co dung 2 truong (sl, phut)', count($pet), 2);
kt('PET truong 2 la "phut"', $pet[1]['ma'] ?? null, 'phut');
kt('PETV khong co truong nao (chi id, khong vong lap so luong)', dmTruongCuaLoai('PETV'), []);

// ---- TK / TKMAX: truong an giu cho tham so chua dung cua server ----
foreach (['TK', 'TKMAX'] as $l) {
    $tk = dmTruongCuaLoai($l);
    kt("$l co dung 2 truong (1 an + 1 phut)", count($tk), 2);
    ktDung("$l truong dau AN va macDinh la dau gach duoi", ($tk[0]['an'] ?? false) === true
        && ($tk[0]['macDinh'] ?? null) === '_');
    kt("$l truong hien la \"phut\", macDinh 0 (vinh vien)", $tk[1]['ma'] ?? null, 'phut');
    kt("$l truong phut cho phep minSo=0", $tk[1]['minSo'] ?? null, '0');
}

// ---- DH: id=0 la nguyen chu "ngau nhien", CHI trong 34/48 danh hieu ----
$dh = dmTruongCuaLoai('DH');
kt('DH co dung 2 truong (sl, phut)', count($dh), 2);

// ===========================================================================
echo "\n== Nguon du lieu moi: petTemplate va DanhHieu deu ghi cung trong Java\n";
// ===========================================================================
kt('danhMucThu() co dung 30 con (chi so 0..29)', count(danhMucThu()), 30);
$thu = danhMucThu();
ktDung('chi so 0..29 lien tuc, khong thieu chi so nao', array_keys($thu) === range(0, 29));
// 5 va 28 trung ten that su (Map.petTemplate ghi hai dong giong het nhau) - xac
// nhan lai o day de khong ai tuong day la loi cua bo sinh.
kt('chi so 5 va 28 cung ten "Lồng đèn hoa sen" (trung That trong Java, khong phai loi)',
    $thu[5] ?? null, $thu[28] ?? null);

$dhMuc = danhMucDanhHieu();
kt('danhMucDanhHieu() co dung 48 danh hieu (khong tinh muc "ngau nhien" tu them)',
    count($dhMuc), 48);
$soNgauNhien = 0;
foreach ($dhMuc as $d) {
    if ($d['ngauNhien']) { $soNgauNhien++; }
}
kt('dung 34 danh hieu danh dau ngauNhien=true', $soNgauNhien, 34);
ktDung('danh hieu 8767 (Anh hung) la ngau nhien',
    ($dhMuc[8767]['ngauNhien'] ?? null) === true);
ktDung('danh hieu 8983 la CO THAT nhung KHONG ngau nhien duoc',
    isset($dhMuc[8983]) && $dhMuc[8983]['ngauNhien'] === false);
ktDung('khong co danh hieu 8999 ("TEST") trong danh muc', !isset($dhMuc[8999]));
for ($id = 8780; $id <= 8784; $id++) {
    ktDung("khong co danh hieu $id (ten rong trong Java) trong danh muc", !isset($dhMuc[$id]));
}

// ===========================================================================
echo "\n== Ma nguon: cac nhom moi phai dung, dung loai, dung id\n";
// ===========================================================================
$dmFile = (string)@file_get_contents($GOC . 'web/admin/inc/danh_muc_thuong.php');

// ---- Canh PHAI dung TT, khong con la ITEM (loi tu dot truoc, da sua) ----
ktDung("nhom canh dung loai 'TT' (KHONG con la 'ITEM')",
    (bool)preg_match("/'ten'\\s*=>\\s*'Cánh'\\s*,\\s*\\n\\s*'loai'\\s*=>\\s*'TT'/", $dmFile));

// ---- dmThoiTrang: dung atb9, dung type, LOAI type 29 (Canh) va 22/23/24 ----
ktDung('dmThoiTrang loc bang atb9 IN (1, 2)', strpos($dmFile, 'atb9 IN (1, 2)') !== false);
ktDung('dmThoiTrang gioi han type IN (0, 1, 2, 13)',
    strpos($dmFile, 'type IN (0, 1, 2, 13)') !== false);
// Doi chieu du lieu that: type 22/23/24 (The mua ban, Xa tieu lenh, Lenh bai tran
// yeu) CUNG co atb9=1 nhung KHONG phai thoi trang that - xac nhan chung KHONG
// nam trong danh sach type duoc dmThoiTrang lay.
foreach ([22, 23, 24, 29] as $t) {
    ktDung("dmThoiTrang KHONG lay type $t",
        !(bool)preg_match('/type IN \(0, 1, 2, 13[^)]*' . $t . '/', $dmFile));
}

// ---- Than khi: canh bao chinh sach khoa phai duoc ghi ro, khong duoc am di ----
ktDung('co canh bao thang khi khong bao gio bi khoa',
    strpos($dmFile, 'KHÔNG BAO GIỜ bị khoá') !== false);
ktDung('canh bao trich dung newItem.lock = 0 cua Char.createThanKhi',
    strpos($dmFile, 'newItem.lock = 0') !== false || strpos($dmFile, 'createThanKhi') !== false);

// ---- require dung ca hai tep sinh moi ----
ktDung("co require_once 'danh_muc_thu.php'", strpos($dmFile, "danh_muc_thu.php'") !== false);
ktDung("co require_once 'danh_muc_danh_hieu.php'",
    strpos($dmFile, "danh_muc_danh_hieu.php'") !== false);

// ---- Vat pham TRUNG TEN (khac id, khac level) - phai gan them cap de phan biet ----
// Bi hoi "sao co nhieu item trung lap nhung khac id". Doi chieu CSDL that: chi dung
// "Phi phong Lăng Vân" (584 Lv35 / 596 Lv95 / 597 Lv100) trung ten trong cac type bo
// chon dang dung - ba id nay la BA BAC KHAC NHAU that su trong game (khong co cot nao
// danh dau "ban chinh"), khong phai loi du lieu de "sua con mot". dmVatPham() phai
// CHI gan "(Lv N)" cho ten trung, KHONG gan tran lan cho moi mon co level > 0 - da xac
// nhan hau het vat pham khac (trung, skin, canh, manh suu tap) deu level thap hoac
// dung chung mot level cho ca nhom, gan vao do chi la nhieu chu khong phan biet duoc gi.
ktDung('dmVatPham dem so lan trung ten TRUOC khi gan nhan',
    strpos($dmFile, '$demTen[$ten]') !== false);
ktDung('dmVatPham CHI gan (Lv N) khi ten trung (demTen > 1), khong gan tran lan',
    (bool)preg_match('/if \(\$demTen\[\$ten\] > 1\)/', $dmFile));
ktDung('dmVatPham SELECT ca cot level de gan nhan', strpos($dmFile, 'name, type, level') !== false);

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

// ---- JS: ho tro truong an va minSo, khong thi TK/TKMAX va "0 = vinh vien" hong ----
ktDung('JS ho tro truong an (t.an) - can cho tham so chua dung cua TK/TKMAX', strpos($js, 't.an') !== false);
ktDung('JS ho tro minSo - can cho "0 = vinh vien" khong bi kep len 1', strpos($js, 't.minSo') !== false);
ktDung('JS truong an tao input type=hidden, khong hien o nhap', strpos($js, "an.type = 'hidden'") !== false);

// $dmFile da doc o dau tep (khoi "cac nhom moi phai dung"), dung lai chu khong doc lai.
ktDung('danh muc thuoc chi lay id trong dai hop le',
    strpos($dmFile, 'DM_SO_O_THUOC') !== false);
ktDung('danh muc thuoc bo dong khong ten',
    strpos($dmFile, "name <> ") !== false);

// ---- Nhom "Canh" phai tach rieng khoi "Manh cai trang" ----
ktDung("co nhom rieng \$dm['canh']", strpos($dmFile, "\$dm['canh']") !== false);
ktDung('nhom Canh dung ham dmIdCanh()', strpos($dmFile, 'dmIdCanh()') !== false);
ktDung('co go bo Canh ra khoi vong lap type 29 truoc khi dung cho Manh cai trang',
    strpos($dmFile, 'unset($muc[$id])') !== false);
ktDung('nhom bat het (vat_pham) co gan nhan rieng cho Canh, khong de lan "Manh cai trang"',
    strpos($dmFile, "'Cánh' : (\$tenLoai[\$type]") !== false);

// ---- ITEM: So luong phai dung duoc cho MOI id, khong con bi khoa ve 1 -----
// Char.createItemSoLuong (Char.java) da duoc sua 31/08/2026: id ngoai manh
// suu tap gio tao THAT SU nhieu item rieng le thay vi luon dung 1 - nen bo
// chon KHONG con ly do khoa o "So luong" nua. Xem test Java tuong ung trong
// test/KiemSoLuongItemGiftcode.java.
ktDung('co ham dmIdManhSuuTap() (van dung de biet id nao cong don vao 1 item)',
    strpos($dmFile, 'function dmIdManhSuuTap()') !== false);
ktDung('JS KHONG con khoa o So luong (capNhatKhoaSoLuong da bo, sua tan goc o Java)',
    strpos($gc, 'capNhatKhoaSoLuong') === false && strpos($gc, 'ID_MANH_SUU_TAP') === false);
ktDung('oMuc lang nghe "change" bang doiMuc chu khong phai truc tiep veXem',
    (bool)preg_match("/oMuc\\.addEventListener\\('change',\\s*doiMuc\\)/", $gc));

// ===========================================================================
echo "\n== Trung phuong hoang phai dung PT, KHONG dung EGG\n";
// ===========================================================================
// EGG:loai:soLuong goi Char.createTrungPhuongHoang(), ham do KHONG co nhanh else -
// loai ngoai 16..19 thi nguoi choi khong nhan gi ma van thay "Chuc mung... nhan duoc
// trung phuong hoang". No cung khong gui createCharInventoryMessage nen hanh trang
// khong tu lam moi. PT lam dung ca hai viec do va nhan MOI id, khong chi bon cai.
$dmNhom = null;
$rf = new ReflectionFunction('dmDanhMuc');
// Khong goi dmDanhMuc that (can PDO). Chi kiem tra bang cach doc ma nguon: nhom
// "trung_thuoc" phai gan loai 'PT', tuyet doi khong duoc la 'EGG'.
ktDung("nhom trung trong o thuoc gan loai 'PT'",
    (bool)preg_match("/\\\$dm\\['trung_thuoc'\\]\\s*=\\s*\\[[^\\]]*'loai'\\s*=>\\s*'PT'/s", $dmFile));
ktDung("khong nhom nao trong danh_muc_thuong.php dung loai 'EGG'",
    strpos($dmFile, "'loai' => 'EGG'") === false && strpos($dmFile, "=> 'EGG'") === false);

// Doi chieu voi chinh Char.java: bon anh xa nay phai con dung, khong thi ghi chu
// giai thich o tren sai theo.
$charJava = (string)@file_get_contents($GOC . 'src/real/Char.java');
ktDung('idPH 16 -> potions[86] (Phuong hoang)',
    strpos($charJava, 'idPH == 16') !== false && strpos($charJava, 'n = 86') !== false);
ktDung('idPH 17 -> potions[70] (Trung PH bang)',
    strpos($charJava, 'idPH == 17') !== false && strpos($charJava, 'n2 = 70') !== false);
ktDung('idPH 18 -> potions[71] (Trung hoa ky lan)',
    strpos($charJava, 'idPH == 18') !== false && strpos($charJava, 'n2 = 71') !== false);
// Cat rieng THAN ham createTrungPhuongHoang() ra roi moi tim trong do - tim tren ca
// tep se an vao chinh cau chu thich giai thich dieu nay, lam phep kiem luon "dat"
// bat ke ma nguon that su lam gi.
$viTriHam = strpos($charJava, 'public void createTrungPhuongHoang');
ktDung('tim thay ham createTrungPhuongHoang trong Char.java', $viTriHam !== false);
$thanHam = $viTriHam !== false ? substr($charJava, $viTriHam, 1200) : '';
ktDung('createTrungPhuongHoang KHONG lam moi hanh trang (khong goi createCharInventoryMessage)',
    $thanHam !== '' && strpos($thanHam, 'createCharInventoryMessage') === false);

// ===========================================================================
echo "\n== Cot 'Chi tiet': giai thich chuoi phan thuong thanh van ban doc duoc\n";
// ===========================================================================
// dmMoTaChuoiThuong() can mot PDO that (tra ten qua data_item/data_potion), nen
// theo dung quy uoc CUA CHINH TEP NAY (xem dau tep: "khong goi dmDanhMuc() vi ham
// do truy van CSDL") - kiem bang soi ma nguon, KHONG goi truc tiep. Hanh vi chay
// that da duoc doi chieu THU CONG qua 18 chuoi vi du tren CSDL that cua VPS luc
// viet ham nay (ITEM/PT/GEM/TT/PET/PETV/TK/DH/EGG/CAITRANG/MATNA/RONGBANG/HKL/
// loai la/id la deu ra dung); o day chi khoa lai cac bat bien quan trong.

ktDung('co ham dmMoTaChuoiThuong()', strpos($dmFile, 'function dmMoTaChuoiThuong(') !== false);
ktDung('co ham dmTenTheoLoai()', strpos($dmFile, 'function dmTenTheoLoai(') !== false);

// Khong duoc ném lỗi ra ngoai: mot doan chuoi cu hong khong duoc lam vo trang
// danh sach giftcode.
ktDung('dmMoTaChuoiThuong xu ly duoc loai KHONG ro (khong nam trong loaiThuong)',
    strpos($dmFile, 'không rõ loại') !== false);

// Danh sach loai co "id" that su can tra ten - doi chieu voi chinh danh sach da
// cai vao switch trong dmTenTheoLoaiKhongCache(). Thieu mot loai o day la mot
// loai bi rot ve nhanh "khong tim thay ten" oan, du id hoan toan hop le.
$phanThan = substr($dmFile,
    (int)strpos($dmFile, 'function dmTenTheoLoaiKhongCache'),
    (int)strpos($dmFile, 'function dmMoTaChuoiThuong') - (int)strpos($dmFile, 'function dmTenTheoLoaiKhongCache'));
ktDung('doc duoc than ham dmTenTheoLoaiKhongCache', strlen($phanThan) > 200);
foreach ([
    'ITEM', 'DB', 'TT', 'AOTT', 'AOSHOP', 'TRANGPHUC', 'MATNA2', 'HLT', 'VKID',
    'PPR', 'PPV', 'PPRK', 'PPVK', 'PPLUCK', 'HOVELAN', 'CHOI', 'TK', 'TKMAX',
    'TKF', 'TKMAXF', 'PT', 'GEM', 'PET', 'PETV', 'DH', 'DANHHIEU2', 'CAITRANG',
    'MATNA', 'EGG',
] as $loai) {
    ktDung("dmTenTheoLoaiKhongCache co xu ly loai $loai",
        strpos($phanThan, "'$loai'") !== false || strpos($phanThan, "case '$loai':") !== false);
}

// EGG: bon gia tri hop le phai khop CHINH XAC voi Char.createTrungPhuongHoang()
// (da doi chieu o khoi kiem "Trung phuong hoang" phia tren) - lech mot so la
// hien sai ten trong cot Chi tiet du giftcode van phat dung.
ktDung('EGG 16 -> Phuong hoang', strpos($dmFile, "16 => 'Phượng hoàng'") !== false);
ktDung('EGG 17 -> Trung phuong hoang bang', strpos($dmFile, "17 => 'Trứng phượng hoàng băng'") !== false);
ktDung('EGG 18 -> Trung Hoa ky lan', strpos($dmFile, "18 => 'Trứng Hoả kỳ lân'") !== false);
ktDung('EGG 19 co canh bao KHONG phai trung', strpos($dmFile, 'Sách kỹ năng pet (KHÔNG phải trứng)') !== false);

// CAITRANG: chi 903/904 co nghia, dung nhu Char.createItemCaiTrang.
ktDung('CAITRANG 903 -> Cai Trang Bat Gioi', strpos($dmFile, "903) { return 'Cải Trang Bát Giới'") !== false);
ktDung('CAITRANG 904 -> Cai Trang Sa Tang', strpos($dmFile, "904) { return 'Cải Trang Sa Tăng'") !== false);

// MATNA: option la CHI SO 0..4, -1 la ngau nhien - khong duoc tra thang id vat
// pham (se ra sai hoan toan, xem thuong.php ghi chu 'option -1 = ngau nhien').
ktDung('MATNA -1 la ngau nhien', strpos($dmFile, "\$id === -1") !== false);
ktDung('MATNA cong 745 vao chi so de ra id that (Char.ID_MAT_NA)',
    strpos($dmFile, '745 + $id') !== false);

// Don vi thoi han: cac loai da xac minh bang PHUT khong duoc lan sang bang NGAY
// va nguoc lai - doi chieu voi bang da dung trong khoi kiem truoc do trong tep nay.
$donVi = dmDonViThoiHanLoai();
foreach (['HKL', 'PET', 'TK', 'TKMAX', 'DH', 'DANHHIEU2', 'MATNA', 'MATNA2'] as $l) {
    kt("don vi cua $l la phut (da xac minh doc Char.java)", $donVi[$l] ?? null, 'phút');
}
foreach (['RONGBANG', 'SUTU', 'SUTUBANG', 'DKB', 'DKT', 'TL',
          'PHT', 'PHDST', 'PHMT', 'PHKT', 'PHTT', 'PHBT'] as $l) {
    kt("don vi cua $l la ngay (da xac minh doc Char.java)", $donVi[$l] ?? null, 'ngày');
}

// Co bo nho trong mot lan tai trang - trang danh sach goi ham nay cho MOI doan
// cua MOI ma, cung mot vat pham lap lai o nhieu ma khong duoc doi ra nhieu cau
// SELECT.
ktDung('dmTenTheoLoai co bo nho tam (static cache) trong mot lan tai trang',
    strpos($dmFile, 'static $boNho') !== false);

// ---- giftcode.php: da noi vao ca hai bang, khong lam vo trang khi loi ----
ktDung("giftcode.php co ham oPhanThuongChiTiet()", strpos($gc, 'function oPhanThuongChiTiet(') !== false);
ktDung('oPhanThuongChiTiet duoc goi trong bang danh sach ma',
    substr_count($gc, 'oPhanThuongChiTiet(') >= 2);
ktDung('oPhanThuongChiTiet boc try/catch quanh dmMoTaChuoiThuong (mot doan hong khong lam vo ca trang)',
    (bool)preg_match('/function oPhanThuongChiTiet.*?catch \(Throwable/s', $gc));
// Chuoi tho van con trong <code> de doi chieu/copy - khong chi thay bang ban dich.
ktDung('van giu chuoi tho trong <code> ben canh ban dich', strpos($gc, "'<code>' . e(\$chuoi)") !== false);

$css = (string)@file_get_contents($GOC . 'web/admin/assets/admin.css');
ktDung('admin.css co dinh nghia .chi-tiet-thuong', strpos($css, '.chi-tiet-thuong') !== false);

// ===========================================================================
echo "\n===================================\n";
if ($hong === 0) {
    echo "DAT: $dat kiem tra\n";
    exit(0);
}
echo "DAT: $dat   HONG: $hong\n";
exit(1);
