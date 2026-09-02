<?php
/**
 * Kiểm thử bảng quy đổi cấp độ (web/admin/inc/capdo.php).
 *
 * Chạy KHÔNG cần MySQL: capDoRaExp() chỉ là một bảng tra cứu thuần, không đụng
 * CSDL. Đối chiếu TỪNG SỐ với real.LevelDetail.expMain nằm ở
 * test/KiemBangCapDo.java (bên Java, vì cần gọi được lớp Java thật).
 *
 * Chạy: php web/admin/test/kiem_capdo.php
 */
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

require_once __DIR__ . '/../inc/capdo.php';

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
echo "\n== Cau truc bang\n";
// ===========================================================================
$bang = capDoRaExp();
ktDung('capDoRaExp() tra ve mang khong rong', count($bang) > 0);
kt('dung 91 muc (0..90, khop do dai real.LevelDetail.expMain)', count($bang), 91);
kt('cap 1 -> exp 0 (vua tao nhan vat)', $bang[0], 0);
ktDung('exp tang dan don dieu (cap sau luon >= cap truoc)', (function () use ($bang) {
    for ($i = 1; $i < count($bang); $i++) {
        if ($bang[$i] < $bang[$i - 1]) {
            return false;
        }
    }
    return true;
})());

// ===========================================================================
echo "\n== CAPDO_TOI_DA\n";
// ===========================================================================
// Doi chieu du lieu THAT tren VPS luc viet tinh nang nay (31/08/2026):
// SELECT MAX(lastLv) FROM tob_char WHERE del=1  ->  90.
kt('CAPDO_TOI_DA = 90 (khop cap cao nhat da ghi nhan tren VPS)', CAPDO_TOI_DA, 90);
// PHAI thap hon do dai bang mot don vi - real.LevelDetail.getXpFromLevel() ben
// Java co dieu kien "level < expMain.length" sai rieng cho dung level bang do
// dai mang (loi lech-mot trong chinh code goc, khong phai o day). Dat tran thap
// hon mot don vi la tranh dung toi bien do vinh vien.
kt('CAPDO_TOI_DA = so muc trong bang - 1 (tranh loi lech-mot cua Java goc)',
    CAPDO_TOI_DA, count($bang) - 1);
ktDung('capDoRaExp() co du phan tu de tra cuu CAPDO_TOI_DA (chi so CAPDO_TOI_DA-1 ton tai)',
    isset($bang[CAPDO_TOI_DA - 1]));

// ===========================================================================
echo "\n== Vai gia tri doi chieu tay (cung dung de kiem tra khi doc lai code sau nay)\n";
// ===========================================================================
kt('cap 50 -> 492247436',        $bang[49], 492247436);
kt('cap 80 -> 870584557573',     $bang[79], 870584557573);
kt('cap 90 -> 50202292519620',   $bang[89], 50202292519620);

// ===========================================================================
echo "\n== Kieu cot tob_char.xp phai chua duoc gia tri lon nhat\n";
// ===========================================================================
// xp la BIGINT UNSIGNED (da doi chieu SHOW COLUMNS tren VPS) - toi da khoang
// 1.8e19. Gia tri cap 90 (~5e13) con rat xa tran, nhung kiem luon cho chac neu
// sau nay tran cap duoc nang len.
$expLonNhat = max($bang);
ktDung('gia tri exp lon nhat con nam trong nguong BIGINT UNSIGNED (< 1.8e19)',
    $expLonNhat < 18000000000000000000);

// ===========================================================================
echo "\n== Tep sinh: dau vet bat buoc\n";
// ===========================================================================
$noiDung = (string)file_get_contents(__DIR__ . '/../inc/capdo.php');
ktDung('co ghi ro DUNG SUA TAY', strpos($noiDung, 'DUNG SUA TAY') !== false);
ktDung('co ghi cach sinh lai (SinhBangCapDo)', strpos($noiDung, 'SinhBangCapDo') !== false);
ktDung('co ghi ro moi chi so khac tu tinh tu lastLv (khong duoc dong tay cot khac)',
    strpos($noiDung, 'SelectCharHandler') !== false);

// ===========================================================================
echo "\n== nhanvat.php: da noi dung, gui lenh cho server game, khong ghi thang tob_char\n";
// ===========================================================================
$nv = (string)@file_get_contents(__DIR__ . '/../nhanvat.php');
ktDung('co require capdo.php', strpos($nv, "inc/capdo.php") !== false);
ktDung('co viec dat_cap_do', strpos($nv, "'dat_cap_do'") !== false);
ktDung('co kiem tra cap do trong khoang 1..CAPDO_TOI_DA',
    strpos($nv, 'CAPDO_TOI_DA') !== false && strpos($nv, '$capDoMoi < 1') !== false);
// 02/09/2026: web KHONG con UPDATE tob_char truc tiep. Nhan vat dang online giu ban cu
// trong RAM va lan tu luu ke tiep ghi de lai, nen web chi gui lenh qua bang
// web_lenh_nhan_vat (inc/lenh_nhan_vat.php); server game (src/data/LenhWeb.java) doc va tu
// ap dung - online thi cong thang vao RAM roi luu ngay, offline thi UPDATE nhu cu.
ktDung('nhanvat.php KHONG con UPDATE lastLv/xp truc tiep (nguyen nhan mat buff khi online)',
    !preg_match('/UPDATE tob_char SET lastLv/', $nv));
ktDung('nhanvat.php nap inc/lenh_nhan_vat.php va gui lenh qua guiLenhCapDo()',
    strpos($nv, "inc/lenh_nhan_vat.php") !== false && strpos($nv, 'guiLenhCapDo(') !== false);
$java = (string)@file_get_contents(__DIR__ . '/../../../src/data/LenhWeb.java');
// Server cung KHONG UPDATE tob_char: luc dang nhap server doc cap/exp tu cot pInfo, hai cot
// lastLv/xp chi cho bang xep hang - ghi chung la vo dung. Nhan vat offline thi lenh nam cho
// (trang_thai 4) va duoc cong qua duong len cap that khi nguoi choi vao game.
ktDung("server (LenhWeb.java) KHONG UPDATE tob_char (dang nhap doc pInfo, cot lastLv/xp vo dung)",
    $java !== '' && !preg_match('/UPDATE tob_char/', $java));
ktDung('server co trang thai "cho vao game" cho nhan vat offline', strpos($java, 'CHO_DANG_NHAP = 4') !== false);
ktDung('trang giai thich co che moi: dung duoc ca khi dang online',
    strpos($nv, 'Dùng được cả khi nhân vật đang online') !== false);
ktDung('form co xac nhan (confirm) truoc khi gui', strpos($nv, "onsubmit=\"return confirm(") !== false);
ktDung('co ghi nhat ky thao tac', strpos($nv, "'dat_cap_do_nhan_vat'") !== false);

// ===========================================================================
echo "\n===================================\n";
if ($hong === 0) {
    echo "DAT: $dat kiem tra\n";
    exit(0);
}
echo "DAT: $dat   HONG: $hong\n";
exit(1);
