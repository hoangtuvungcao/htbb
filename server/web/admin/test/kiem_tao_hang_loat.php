<?php
/**
 * Kiểm thử trang "Tạo tài khoản hàng loạt" (web/admin/tao_hang_loat.php).
 *
 * Chạy KHÔNG cần MySQL: trang thật gọi requireAdmin() (đá về index.php / thoát nếu
 * chưa đăng nhập) và mở PDO thật ngay khi require — không thể require thẳng file đó
 * từ CLI mà không có phiên đăng nhập. Nên bài kiểm này đọc mã nguồn bằng
 * file_get_contents() rồi soi bằng chuỗi/regex, đúng khuôn của
 * web/admin/test/kiem_danh_muc_thuong.php.
 *
 * Chạy: php web/admin/test/kiem_tao_hang_loat.php
 */
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

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

$trang      = (string)@file_get_contents($GOC . 'web/admin/tao_hang_loat.php');
$dangKy     = (string)@file_get_contents($GOC . 'web/register.php');
$layout     = (string)@file_get_contents($GOC . 'web/admin/inc/layout.php');
$boot       = (string)@file_get_contents($GOC . 'web/admin/inc/boot.php');
$schemaTest = (string)@file_get_contents($GOC . 'web/admin/test/kiem_schema.php');

ktDung('doc duoc web/admin/tao_hang_loat.php', $trang !== '');
ktDung('doc duoc web/register.php (de doi chieu)', $dangKy !== '');

echo "\n== Vao trang phai qua requireAdmin() + CSRF ==\n";
ktDung('require inc/layout.php', strpos($trang, "require_once __DIR__ . '/inc/layout.php'") !== false);
ktDung('goi requireAdmin()', strpos($trang, 'requireAdmin();') !== false);
ktDung('POST phai qua chanCsrf($TRANG)', (bool)preg_match('/chanCsrf\(\s*\$TRANG\s*\)/', $trang));
ktDung('form co truong csrf', strpos($trang, "name=\"csrf\" value=\"<?= e(csrfToken())") !== false);

echo "\n== Gioi han so dong mot luot (chan dan nham danh sach khong lo) ==\n";
if (preg_match('/const\s+TAO_HANG_LOAT_TOI_DA\s*=\s*(\d+)\s*;/', $trang, $m)) {
    $gh = (int)$m[1];
    kt('co hang so TAO_HANG_LOAT_TOI_DA', true, true);
    ktDung('gioi han la mot so duong hop ly (1..2000)', $gh > 0 && $gh <= 2000);
} else {
    kt('co hang so TAO_HANG_LOAT_TOI_DA', false, true);
}
ktDung('co kiem tra vuot qua gioi han truoc khi xu ly', strpos($trang, 'count($dong) > TAO_HANG_LOAT_TOI_DA') !== false);
ktDung('danh sach rong bi tu choi', strpos($trang, '!$dong') !== false);
// Chi tim LOI GOI THAT (co dau nhay ngay sau dau ngoac, dung y het cach goi bien
// Chan Spam that trong tep khac), khong khop nham chinh doan chu thich giai thich
// VI SAO KHONG dung ham nay - doan chu thich do viet "bienSpam()" KHONG co dau nhay.
ktDung('KHONG dung biChanSpam()/ghiLanLam() (danh cho form cong khai, khong phai admin da dang nhap)',
    strpos($trang, "biChanSpam('") === false && strpos($trang, "ghiLanLam('") === false);

echo "\n== Moi dong xu ly DOC LAP - loi mot dong chi duoc BO QUA, khong duoc thoat ca vong lap ==\n";
ktDung('vong lap chinh dung foreach qua danh sach dong', (bool)preg_match('/foreach\s*\(\s*\$dong\s+as\s+\$i\s*=>\s*\$line\s*\)/', $trang));
ktDung('dong loi dung continue de bo qua (it nhat 4 cho: sai ten, trung trong lo, trung CSDL, sai mat khau)',
    substr_count($trang, 'continue;') >= 4);

echo "\n== Xac thuc dung ham co san, khong tu viet lai ==\n";
ktDung('dung validUsername() de kiem ten', strpos($trang, 'validUsername($username)') !== false);
ktDung('dung validPassword() de kiem mat khau', strpos($trang, 'validPassword($matKhau)') !== false);
ktDung('chan trung NGAY TRONG danh sach vua nhap (mang $daGap)', strpos($trang, '$daGap[$username]') !== false);
ktDung('kiem tra trung ten VOI CSDL truoc khi insert',
    (bool)preg_match('/SELECT 1 FROM team_user WHERE username = \? LIMIT 1/', $trang));

echo "\n== Cau INSERT phai khop voi register.php (hai noi tao tai khoan khong duoc lech cot) ==\n";
// Bat CA CUM VALUES (...) den dung dau ngoac dong cuoi truoc dau nhay don ket thuc
// chuoi PHP - khong the dung [^)]* don gian vi PASSWORD(?) co dau ngoac dong long
// vao giua, se cat cut mat phan sau.
$mocValues = '/INSERT INTO team_user \([^)]*\)\s*VALUES\s*\((.*?)\)\s*\'/s';
if (preg_match($mocValues, $dangKy, $mDk) && preg_match($mocValues, $trang, $mTh)) {
    $chuanHoa = static fn($s) => preg_replace('/\s+/', ' ', trim($s));
    $vDk = explode(',', $chuanHoa($mDk[1]));
    $vTh = explode(',', $chuanHoa($mTh[1]));
    kt('doc duoc du 8 cot o CA HAI ben (username,password,phone,regdate,ban,provider,fromgame,email)',
        [count($vDk), count($vTh)], [8, 8]);
    // 7 cot dau PHAI GIONG HET NHAU (chi cot email o cuoi duoc phep khac: dang ky
    // dung tham so ?, tao hang loat ghi cung NULL vi khong thu email hang loat).
    $bay7DauDk = array_map('trim', array_slice($vDk, 0, 7));
    $bay7DauTh = array_map('trim', array_slice($vTh, 0, 7));
    kt('7 cot dau (username..fromgame) giong het register.php', $bay7DauTh, $bay7DauDk);
    ktDung('cot password deu dung PASSWORD(?) de bam (khop login server)',
        trim($vDk[1]) === 'PASSWORD(?)' && trim($vTh[1]) === 'PASSWORD(?)');
    ktDung('cot regdate deu dung NOW()', trim($vDk[3]) === 'NOW()' && trim($vTh[3]) === 'NOW()');
} else {
    ktDung('tim thay cau INSERT INTO team_user (...) VALUES (...) trong CA HAI tep', false);
}

echo "\n== Mat khau khong duoc lo ra ngoai y muon ==\n";
ktDung('ghiNhatKy() KHONG ghi bien $matKhau (chi ghi id + trang thai kich hoat)',
    !preg_match('/ghiNhatKy\([^;]*\$matKhau/s', $trang));
ktDung('co ghi chu nhac mat khau chi bam mot chieu (PASSWORD(), khong doc lai duoc)',
    strpos($trang, 'PASSWORD()') !== false && strpos($trang, 'một chiều') !== false);

echo "\n== Kich hoat: dung ham activateAccount() da co, khong tu ghi thang vao 5h_active ==\n";
ktDung('dung activateAccount($userId, $username)', strpos($trang, 'activateAccount($userId, $username)') !== false);
ktDung('KHONG tu INSERT thang vao `5h_active` (phai di qua activateAccount)',
    stripos($trang, 'INSERT INTO `5h_active`') === false && stripos($trang, 'insert into `5h_active`') === false);

echo "\n== Da noi vao dieu huong + kiem schema ==\n";
ktDung("adminMenu() co muc 'tao_hang_loat' tro toi tao_hang_loat.php",
    (bool)preg_match("/'tao_hang_loat'\s*=>\s*\['tao_hang_loat\.php'/", $layout));
ktDung("co icon rieng ('nhieu') cho muc menu moi trong bieuTuong()",
    (bool)preg_match("/'nhieu'\s*=>\s*'</", $layout));
ktDung('kiem_schema.php co quet ca tao_hang_loat.php (chan go nham ten cot/bang)',
    strpos($schemaTest, "'web/admin/tao_hang_loat.php'") !== false);

echo "\n== requireAdmin()/chanCsrf()/ghiNhatKy() phai la ham THAT dang co trong boot.php ==\n";
ktDung('boot.php co dinh nghia requireAdmin()', strpos($boot, 'function requireAdmin()') !== false);
ktDung('boot.php co dinh nghia chanCsrf()', strpos($boot, 'function chanCsrf(') !== false);
ktDung('boot.php co dinh nghia ghiNhatKy()', strpos($boot, 'function ghiNhatKy(') !== false);

echo "\n===================================\n";
echo "DAT: $dat  HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
