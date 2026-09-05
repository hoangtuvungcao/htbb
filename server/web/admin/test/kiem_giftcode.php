<?php
/**
 * Kiểm thử bộ xác thực chuỗi phần thưởng giftcode (web/admin/inc/thuong.php).
 * Chạy: php test_thuong.php
 */
// Chỉ chạy bằng dòng lệnh. Thư mục này nằm DƯỚI web root (/var/www/html/kpah
// là symlink tới /opt/kpah/web) nên không chặn thì ai trên Internet cũng gọi
// được: lộ đường dẫn tệp, tên bảng, tên cột, và tốn CPU máy chủ. Apache ở đây
// đặt AllowOverride None nên .htaccess bị bỏ qua — chốt phải nằm trong chính
// tệp PHP thì mới có tác dụng.
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

require __DIR__ . '/../inc/thuong.php';

$dat = 0;
$hong = 0;

function kt(string $ten, string $item, bool $mongDoiHopLe, string $mongDoiChuoi = null): void
{
    global $dat, $hong;
    [$chuan, $loi] = kiemTraItem($item);
    $hopLe = empty($loi);

    $ok = ($hopLe === $mongDoiHopLe)
        && ($mongDoiChuoi === null || $chuan === $mongDoiChuoi);

    if ($ok) {
        $dat++;
        echo "  OK   $ten\n";
    } else {
        $hong++;
        echo "  HONG $ten\n";
        echo "       vao      : $item\n";
        echo "       hop le   : " . var_export($hopLe, true) . " (mong doi " . var_export($mongDoiHopLe, true) . ")\n";
        echo "       chuan hoa: $chuan" . ($mongDoiChuoi !== null ? " (mong doi $mongDoiChuoi)" : "") . "\n";
        if ($loi) {
            foreach ($loi as $l) echo "       loi: $l\n";
        }
    }
}

echo "== Chuoi rong va co ban ==\n";
kt('chuoi rong', '', true, '');
kt('mot mon dung', 'ITEM:848:1000', true, 'ITEM:848:1000');
kt('thieu tham so', 'ITEM:848', false);
kt('loai khong ton tai', 'BLAH:1:2', false);
kt('co dau nhay don', "ITEM:848:1000'", false);
kt('co dau nhay kep', 'ITEM:848:"1000"', false);
kt('co dau chấm phay', 'ITEM:848:1000;DROP', false);

echo "\n== Chuan hoa ==\n";
kt('viet thuong -> hoa', 'item:848:1000', true, 'ITEM:848:1000');
kt('bo khoang trang', ' ITEM : 848 : 1000 ', true, 'ITEM:848:1000');
kt('bo phan tu rong', 'ITEM:848:1000,,TL:30', true, 'ITEM:848:1000,TL:30');

echo "\n== Cac ma THAT dang co trong ban dump (deu phai hop le) ==\n";
$maThat = [
    'oke212'  => 'ITEM:848:30000:1,ITEM:849:1000:1,ITEM:850:10000:1,ITEM:851:1000:1,ITEM:852:1000:1,ITEM:853:1000:1,ITEM:854:1000:1,ITEM:855:1000:1,ITEM:856:1000:1,ITEM:857:1000:1,ITEM:858:1000:1,ITEM:859:1000:1',
    '400ksz'  => 'PPLUCK:593:1:1:1,PPLUCK:593:1:1:0',
    '123'     => 'DN:500000:500000',
    'ao4'     => 'AOSHOP:725:1:0,AOSHOP:726:1:0',
    'bm1'     => 'SUTU:1:300',
    'hvl'     => 'AOSHOP:741:1:43200',
    'NLT3'    => 'NLT:0:1:43200',
    'tbSz'    => 'VKTBLV:70:99:5000',
    'vk'      => 'VKTT:0:1:43200',
    'vailua'  => 'GEM:73:10000:1,GEM:108:10000:1',
    'tl'      => 'TL:30',
    't'       => 'NLTMAX:0:1:80000,TRANGPHUC:833:1:30,TRANGPHUC:834:1:30,TRANGPHUC:839:1:30,TRANGPHUC:791:1:30,TRANGPHUC:744:1:30,TRANGPHUC:736:1:30,TRANGPHUC:737:1:50',
    'hvl2'    => 'HOVELAN:882:5:43200',
    'canhvohinh' => 'ITEM:910:1',
];
foreach ($maThat as $ten => $item) {
    kt("ma '$ten'", $item, true);
}

echo "\n== Bat loi thieu tham so cho tung loai ==\n";
// Voi moi loai, cat bot mot phan so voi so toi thieu -> phai bao loi.
$soLoaiKiemTra = 0;
foreach (loaiThuong() as $loai => [$toiThieu, $cuPhap, $mota]) {
    if ($toiThieu <= 1) {
        continue; // loai khong can tham so nao, khong the thieu
    }
    $thieu = $loai . str_repeat(':1', $toiThieu - 2);
    [$c, $l] = kiemTraItem($thieu);
    $soLoaiKiemTra++;
    if (empty($l)) {
        $hong++;
        echo "  HONG $loai: '$thieu' le ra phai bao thieu tham so (can $toiThieu phan)\n";
    }

    $du = $loai . str_repeat(':1', $toiThieu - 1);
    [$c2, $l2] = kiemTraItem($du);
    if (!empty($l2)) {
        $hong++;
        echo "  HONG $loai: '$du' du tham so ma van bao loi: " . implode(' / ', $l2) . "\n";
    }
}
$dat++;
echo "  OK   da kiem $soLoaiKiemTra loai (moi loai: thieu 1 phan -> bao loi, du phan -> khong loi)\n";

echo "\n== Vi du (cot thu 4) cua MOI loai phai la chinh chuoi that qua duoc kiemTraItem() ==\n";
// Vi du trong bang giup nguoi tao ma co san mot dong dung cu phap de bam "Chen" (xem
// giftcode.php) - neu vi du sai cu phap thi nguoi dung se bi day sang huong sai ngay
// tu diem bat dau, con te hon la khong co vi du nao. Kiem qua CHINH kiemTraItem(), khong
// tu viet lai dieu kien: day la ham THAT server dung de xac nhan chuoi truoc khi luu.
$hongTruoc = $hong;
$soViDu = 0;
foreach (loaiThuong() as $loai => [$toiThieu, $cuPhap, $mota, $viDu]) {
    $soViDu++;
    if (!isset($viDu) || $viDu === '') {
        $hong++;
        echo "  HONG $loai: chua co vi du\n";
        continue;
    }
    [, $loi] = kiemTraItem($viDu);
    if (!empty($loi)) {
        $hong++;
        echo "  HONG $loai: vi du '$viDu' khong qua duoc kiemTraItem(): " . implode(' / ', $loi) . "\n";
    }
    // Vi du phai TU XUNG DANH dung loai cua chinh dong no dang o (vd viet nham
    // 'ITEM:...' o dong 'DB' se lot qua kiemTraItem nhung day nguoi doc sang loai khac).
    $tenTrongVd = strtoupper(explode(':', $viDu)[0]);
    if ($tenTrongVd !== $loai) {
        $hong++;
        echo "  HONG $loai: vi du '$viDu' lai ghi ten loai '$tenTrongVd'\n";
    }
}
if ($hong === $hongTruoc) {
    $dat++;
    echo "  OK   ca $soViDu vi du deu qua duoc kiemTraItem() va tu xung dung ten loai\n";
}

echo "\n== Bang loai thuong khop voi ma nguon Java ==\n";
$java = file_get_contents(__DIR__ . '/../../../src/data/DatabaseGiftCode.java');
preg_match_all('/tachTime\[0\]\.equals\("([A-Za-z0-9_]+)"\)/', $java, $m);
$trongJava = array_values(array_unique($m[1]));
$trongPhp  = array_keys(loaiThuong());

$thieuOPhp = array_diff($trongJava, $trongPhp);
$thuaOPhp  = array_diff($trongPhp, $trongJava);

if ($thieuOPhp) {
    $hong++;
    echo "  HONG thieu trong bang PHP: " . implode(', ', $thieuOPhp) . "\n";
} else {
    $dat++;
    echo "  OK   du ca " . count($trongJava) . " loai co trong Java\n";
}
if ($thuaOPhp) {
    $hong++;
    echo "  HONG PHP co loai ma Java khong hieu: " . implode(', ', $thuaOPhp) . "\n";
} else {
    $dat++;
    echo "  OK   khong co loai thua\n";
}


echo "\n== So phan khai trong PHP du cho chi so Java that su doc ==\n";
// Vi sao can: phep kiem "thieu 1 phan -> bao loi" o tren lay CHINH so khai bao lam
// chuan, nen no khong the tu phat hien khai SAI. Ma khai thieu la nguy hiem nhat:
// server doc tachTime[N] vuot mang -> nem ArrayIndexOutOfBounds GIUA luc phat qua,
// nguoi choi da nhan nua so qua ma dong giftcode_log lan viec tru luot deu khong
// chay -> nhan lai duoc vo han. Nen o day doi chieu voi chi so ma Java THAT SU doc.
$khoi = preg_split('/\} else if \(tachTime\[0\]\.equals\("/', $java);
$doc  = [];
foreach ($khoi as $k) {
    if (!preg_match('/^([A-Za-z0-9_]+)"\)/', $k, $mm)) {
        continue;
    }
    $loaiPT = $mm[1];
    // Bo cac doan nam trong try/catch: doc vuot mang o do bi bat lai nen phan do la
    // TUY CHON that su. Vi du GEM doc tachTime[3] trong try, chu thich ngay duoi ghi
    // ro "// GEM:ID:SL" — tuc 3 phan la du, phan thu 4 chi de chon ngoc khoa.
    $than = preg_replace('/try\s*\{.*?\}\s*catch/s', '', $k);
    preg_match_all('/tachTime\[(\d+)\]/', $than, $idx);
    $max = 0;
    foreach ($idx[1] as $i) {
        $max = max($max, (int)$i);
    }
    // Giu chi so LON NHAT neu mot loai xuat hien nhieu lan.
    $doc[$loaiPT] = max($doc[$loaiPT] ?? 0, $max);
}
$bangPhp = loaiThuong();
$lech = [];
foreach ($doc as $loaiPT => $maxIdx) {
    if (!isset($bangPhp[$loaiPT])) {
        continue; // da bao o phep kiem tren
    }
    $can = $maxIdx + 1;
    $khai = $bangPhp[$loaiPT][0];
    if ($khai < $can) {
        $lech[] = "$loaiPT: PHP khai $khai phan nhung Java doc toi tachTime[$maxIdx] (can $can)";
    }
}
if ($lech) {
    $hong++;
    echo "  HONG khai thieu so phan:\n";
    foreach ($lech as $d) {
        echo "    - $d\n";
    }
} else {
    $dat++;
    echo "  OK   da doi chieu " . count($doc) . " loai, khong loai nao khai thieu\n";
}
echo "\n===================================\n";
echo "DAT: $dat   HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
