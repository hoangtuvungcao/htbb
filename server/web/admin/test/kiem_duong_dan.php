<?php
/**
 * Kiểm thử chốt chặn đọc file của trang Chẩn đoán.
 *
 * chandoan.php chỉ đọc file khi tên khớp CHÍNH XÁC một khoá trong fileLog():
 *
 *     if ($xemFile !== '' && array_key_exists($xemFile, fileLog())) { ... }
 *
 * Không có bước đó thì `?file=../config.local.php` sẽ đọc được mật khẩu
 * database, và `?file=../../etc/passwd` đọc được file hệ thống. Test này lấy
 * chính danh sách thật trong chandoan.php (không chép lại) rồi ném vào một loạt
 * đường dẫn độc, khẳng định không cái nào lọt.
 *
 * Chạy: php web/admin/test/kiem_duong_dan.php
 */

$nguon = file_get_contents(__DIR__ . '/../chandoan.php');

// Lấy đúng khối `return [...]` bên trong hàm fileLog()
$i = strpos($nguon, 'function fileLog(): array');
if ($i === false) {
    fwrite(STDERR, "Khong tim thay ham fileLog() trong chandoan.php\n");
    exit(1);
}
$batDau = strpos($nguon, 'return [', $i);
$ketThuc = strpos($nguon, '];', $batDau) + 2;
eval('$CHO_PHEP = ' . substr($nguon, $batDau + strlen('return '), $ketThuc - $batDau - strlen('return ')));

echo 'Danh sach cho phep co ' . count($CHO_PHEP) . " file:\n";
foreach (array_keys($CHO_PHEP) as $f) {
    echo "  - $f\n";
}

$dat = 0;
$hong = 0;

/** Đúng logic của chandoan.php. */
function duocDoc(string $xemFile, array $choPhep): bool
{
    return $xemFile !== '' && array_key_exists($xemFile, $choPhep);
}

echo "\n== Duong dan DOC phai bi tu choi ==\n";
$doc = [
    '../config.local.php',
    '../config.php',
    '../../server.ini',
    '../../loginServer/server.ini',
    '/etc/passwd',
    'C:/xampp/php/php.ini',
    '../admin/inc/boot.php',
    'errorlog.txt/../../config.php',
    './errorlog.txt',
    '.\\errorlog.txt',
    'errorlog.txt ',
    ' errorlog.txt',
    'ERRORLOG.TXT',
    'errorlog.txt%00',
    "errorlog.txt\0../config.php",
    '..%2Fconfig.local.php',
    '....//config.local.php',
    '',
    '.',
    '..',
    '/',
    'log/../../config.local.php',
    'php://filter/read=convert.base64-encode/resource=../config.php',
    'http://vidu.com/xau.txt',
];
foreach ($doc as $d) {
    if (duocDoc($d, $CHO_PHEP)) {
        $hong++;
        echo '  HONG LOT LUOI: ' . var_export($d, true) . "\n";
    } else {
        $dat++;
        echo '  OK   tu choi: ' . var_export($d, true) . "\n";
    }
}

echo "\n== Duong dan HOP LE phai duoc nhan ==\n";
foreach (array_keys($CHO_PHEP) as $f) {
    if (duocDoc($f, $CHO_PHEP)) {
        $dat++;
        echo "  OK   nhan: $f\n";
    } else {
        $hong++;
        echo "  HONG le ra phai nhan: $f\n";
    }
}

echo "\n== Moi file cho phep deu nam trong SERVER_DIR ==\n";
// Ghep xong khong duoc thoat ra khoi thu muc goc.
$goc = '/opt/kpah';
foreach (array_keys($CHO_PHEP) as $f) {
    $ghep = $goc . '/' . $f;
    // Chuan hoa thu cong (khong dung realpath vi file khong ton tai o day)
    $phan = [];
    foreach (explode('/', $ghep) as $p) {
        if ($p === '..') { array_pop($phan); } elseif ($p !== '.' && $p !== '') { $phan[] = $p; }
    }
    $chuan = '/' . implode('/', $phan);
    if (strpos($chuan, $goc . '/') === 0) {
        $dat++;
        echo "  OK   $f -> $chuan\n";
    } else {
        $hong++;
        echo "  HONG $f thoat ra ngoai: $chuan\n";
    }
}

echo "\n===================================\n";
echo "DAT: $dat   HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
