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
// Chỉ chạy bằng dòng lệnh. Thư mục này nằm DƯỚI web root (/var/www/html/kpah
// là symlink tới /opt/kpah/web) nên không chặn thì ai trên Internet cũng gọi
// được: lộ đường dẫn tệp, tên bảng, tên cột, và tốn CPU máy chủ. Apache ở đây
// đặt AllowOverride None nên .htaccess bị bỏ qua — chốt phải nằm trong chính
// tệp PHP thì mới có tác dụng.
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

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

// ---------------------------------------------------------------------------
//  Thư mục test này nằm DƯỚI web root và phải tự chặn truy cập qua trình duyệt
// ---------------------------------------------------------------------------
//
// CẠM BẪY ĐÃ NỔ (29/08/2026): `/var/www/html/kpah` là symlink tới `/opt/kpah/web`,
// nên `web/admin/test/` mở được từ Internet. Gọi thử `kiem_doc_ini.php` qua HTTP
// thì nó chạy thật và in ra kết quả — lộ đường dẫn tệp, tên bảng, tên cột, cùng
// mọi dữ liệu mẫu trong bài kiểm, lại còn tốn CPU máy chủ cho bất kỳ ai gọi.
//
// KHÔNG chặn được bằng `.htaccess`: Apache trên VPS đặt `AllowOverride None`
// (apache2.conf, khối `<Directory /var/www/>`) nên tệp .htaccess bị bỏ qua hoàn
// toàn — đặt vào chỉ tạo cảm giác an toàn giả. Chốt phải nằm trong chính tệp PHP.
echo "\n== Moi tep trong thu muc nay phai tu chan truy cap qua trinh duyet\n";

$thieuChot = [];
$soBai = 0;
foreach (glob(__DIR__ . '/*.php') ?: [] as $f) {
    if (basename($f) === 'index.php') {
        continue;   // tệp chặn liệt kê thư mục, kiểm riêng ở dưới
    }
    $soBai++;
    if (strpos(file_get_contents($f), "PHP_SAPI !== 'cli'") === false) {
        $thieuChot[] = basename($f);
    }
}
if ($thieuChot) {
    $hong++;
    echo "  HONG thieu chot chan CLI: " . implode(', ', $thieuChot) . "\n";
} else {
    $dat++;
    echo "  OK   ca $soBai bai kiem deu co chot chan CLI\n";
}

// Không có tệp index thì Apache (đang bật `Options Indexes`) sẽ liệt kê sạch mọi
// tệp trong thư mục cho bất kỳ ai mở địa chỉ này.
foreach (['admin/assets', 'admin/inc', 'admin/test', 'assets', 'inc'] as $thuMuc) {
    $f = dirname(__DIR__, 2) . '/' . $thuMuc . '/index.php';
    $co = is_file($f) && strpos(file_get_contents($f), 'http_response_code(403)') !== false;
    if ($co) {
        $dat++;
        echo "  OK   web/$thuMuc co tep chan liet ke thu muc\n";
    } else {
        $hong++;
        echo "  HONG web/$thuMuc thieu index.php chan liet ke thu muc\n";
    }
}

echo "\n===================================\n";
echo "DAT: $dat   HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
