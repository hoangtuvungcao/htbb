<?php
/**
 * Kiểm thử bộ xác thực chuỗi phần thưởng giftcode (web/admin/inc/thuong.php).
 * Chạy: php test_thuong.php
 */
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

echo "\n===================================\n";
echo "DAT: $dat   HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
