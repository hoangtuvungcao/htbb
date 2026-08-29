<?php
/**
 * Chan doan LOI KET NOI DATABASE cua website. Chi ra dung nguyen nhan thay vi
 * de ban doan giua nam kha nang.
 *
 * Chay TREN MAY CHU, bang dong lenh:
 *     php /opt/kpah/web/admin/test/chan_doan_ket_noi.php
 *
 * Neu muon biet PHP cua trang web (thuong la www-data) co doc duoc gi khong -
 * chay duoi dung nguoi dung do, vi day moi la cai quyet dinh:
 *     sudo -u www-data php /opt/kpah/web/admin/test/chan_doan_ket_noi.php
 *
 * KHONG BAO GIO in mat khau ra man hinh, chi in DO DAI. Nhung no van in ten
 * nguoi dung database va thong bao loi cua MySQL, nen chi chay bang dong lenh -
 * file tu tu choi khi bi mo qua trinh duyet.
 */

if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../inc/doc_server_ini.php';

function tieuDe(string $s): void
{
    echo "\n" . str_repeat('=', 68) . "\n  $s\n" . str_repeat('=', 68) . "\n";
}

function dong(string $nhan, $giaTri): void
{
    printf("  %-34s %s\n", $nhan . ':', $giaTri);
}

// ---------------------------------------------------------------------------
tieuDe('1. PHP dang chay duoi ai');

$nguoiDung = function_exists('posix_geteuid')
    ? (posix_getpwuid(posix_geteuid())['name'] ?? ('uid ' . posix_geteuid()))
    : (get_current_user() ?: 'khong biet');
dong('Nguoi dung', $nguoiDung);
dong('Phien ban PHP', PHP_VERSION);
echo "\n  LUU Y: trang web chay duoi www-data (hoac tuong tu), KHONG phai root.\n";
echo "  Chay file nay bang root ma bao OK thi chua chac trang web cung OK.\n";
echo "  Chay lai bang:  sudo -u www-data php " . __FILE__ . "\n";

// ---------------------------------------------------------------------------
tieuDe('2. File cau hinh');

$fLocal = __DIR__ . '/../../config.local.php';
dong('config.local.php', is_file($fLocal)
    ? (is_readable($fLocal) ? 'co, doc duoc' : 'CO NHUNG KHONG DOC DUOC')
    : 'KHONG CO');

$khaiTay = [];
if (is_file($fLocal) && is_readable($fLocal)) {
    $noiDung = file_get_contents($fLocal);
    foreach (['DB_HOST', 'DB_PORT', 'DB_USER', 'DB_PASS', 'SERVER_DIR', 'ADMIN_ACCOUNTS'] as $hang) {
        // Chi tinh dong KHONG bi chu thich
        if (preg_match('/^[ \t]*define\s*\(\s*[\'"]' . $hang . '[\'"]/m', $noiDung)) {
            $khaiTay[] = $hang;
        }
    }
}
dong('Khai tay trong do', $khaiTay ? implode(', ', $khaiTay) : '(khong khai gi)');

dong('SERVER_DIR', SERVER_DIR);
$fGame  = rtrim(SERVER_DIR, '/\\') . '/server.ini';
$fLogin = rtrim(SERVER_DIR, '/\\') . '/loginServer/server.ini';
foreach ([$fGame, $fLogin] as $f) {
    $tt = !is_file($f) ? 'KHONG CO'
        : (is_readable($f) ? 'doc duoc' : 'CO NHUNG KHONG CO QUYEN DOC');
    dong(basename(dirname($f)) . '/' . basename($f), $tt . '   [' . $f . ']');
}

// ---------------------------------------------------------------------------
tieuDe('3. Gia tri server.ini doc ra duoc');

$ini = kpahCauHinhDb(SERVER_DIR);
foreach ($ini as $k => $v) {
    if ($k === 'pass' || $k === 'account_pass') {
        dong($k, $v === null ? '(khong doc duoc)' : ('(' . strlen($v) . ' ky tu)'));
    } else {
        dong($k, $v === null ? '(khong doc duoc)' : $v);
    }
}

// ---------------------------------------------------------------------------
tieuDe('4. Gia tri website DANG THAT SU dung');

dong('DB_HOST', DB_HOST);
dong('DB_PORT', DB_PORT);
dong('DB_USER', DB_USER);
dong('DB_PASS', '(' . strlen(DB_PASS) . ' ky tu)');
dong('DB_GAME', DB_GAME);
dong('DB_ACCOUNT', DB_ACCOUNT);
dong('DB_ACCOUNT_USER', defined('DB_ACCOUNT_USER') ? DB_ACCOUNT_USER : '(dung chung DB_USER)');

// Chi canh bao khi dung mac dinh VI KHONG BIET GI HON. Neu server.ini that su
// ghi root/mat khau rong (may dev XAMPP) thi day la dung, khong phai loi.
if (strlen(DB_PASS) === 0 && DB_USER === 'root' && $ini['user'] === null) {
    echo "\n  >>> DAY GAN NHU CHAC LA NGUYEN NHAN: dang dung 'root' voi mat khau RONG,\n";
    echo "      day chi la gia tri mac dinh cuoi cung - khong doc duoc server.ini VA\n";
    echo "      config.local.php cung khong khai DB_USER/DB_PASS. Xem lai muc 2.\n";
}

// ---------------------------------------------------------------------------
tieuDe('5. Thu ket noi that');

/** Thu mot bo thong tin, in ket qua ngan gon. */
function thu(string $ten, string $host, int $port, string $user, string $pass, string $db): bool
{
    $dsn = sprintf('mysql:host=%s;port=%d;dbname=%s;charset=utf8mb4', $host, $port, $db);
    try {
        new PDO($dsn, $user, $pass, [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_TIMEOUT => 5]);
        printf("  [ OK ]  %-42s %s@%s -> %s\n", $ten, $user, $host, $db);
        return true;
    } catch (Throwable $e) {
        printf("  [ HONG] %-42s %s@%s -> %s\n          %s\n",
            $ten, $user, $host, $db, $e->getMessage());
        return false;
    }
}

echo "\n  -- Bo dang duoc website dung --\n";
$okGame = thu('cau hinh hien tai (DB game)', DB_HOST, (int)DB_PORT, DB_USER, DB_PASS, DB_GAME);
$uAcc = defined('DB_ACCOUNT_USER') ? DB_ACCOUNT_USER : DB_USER;
$pAcc = defined('DB_ACCOUNT_USER') ? DB_ACCOUNT_PASS : DB_PASS;
$okAcc = thu('cau hinh hien tai (DB tai khoan)', DB_HOST, (int)DB_PORT, $uAcc, $pAcc, DB_ACCOUNT);

if ($ini['user'] !== null && $ini['pass'] !== null) {
    echo "\n  -- Bo lay thang tu server.ini, thu ca hai kieu may chu --\n";
    $cong = $ini['port'] ?? 3306;
    $dbG  = $ini['game'] ?? DB_GAME;
    thu('server.ini + host nhu trong file', (string)($ini['host'] ?? 'localhost'), $cong, $ini['user'], $ini['pass'], $dbG);
    thu('server.ini + localhost (qua socket)', 'localhost', $cong, $ini['user'], $ini['pass'], $dbG);
    thu('server.ini + 127.0.0.1 (qua TCP)', '127.0.0.1', $cong, $ini['user'], $ini['pass'], $dbG);
} else {
    echo "\n  -- Khong doc duoc server.ini nen khong thu duoc bo do --\n";
}

// ---------------------------------------------------------------------------
tieuDe('6. Ket luan');

if ($okGame && $okAcc) {
    echo "  Ca hai database deu ket noi duoc. Loi 1045 tren trinh duyet khong con\n";
    echo "  ly do gi de xuat hien nua - tru khi trang web chay duoi NGUOI DUNG KHAC\n";
    echo "  voi nguoi dung dang chay file nay. Chay lai bang:\n";
    echo "      sudo -u www-data php " . __FILE__ . "\n";
} else {
    echo "  Doc bang o muc 5: dong nao [ OK ] thi do chinh la bo thong tin can dung.\n";
    echo "  Chep dung bo do vao /opt/kpah/web/config.local.php, vi du:\n\n";
    echo "      define('DB_HOST', 'localhost');\n";
    echo "      define('DB_USER', '...');\n";
    echo "      define('DB_PASS', '...');\n\n";
    echo "  Neu KHONG dong nao [ OK ]:\n";
    echo "    - 1045 Access denied  -> sai mat khau, hoac quyen cua nguoi dung do chi\n";
    echo "                             cap cho 'localhost' ma dang noi qua 127.0.0.1\n";
    echo "                             (hoac nguoc lai). Xem: SELECT user, host FROM mysql.user;\n";
    echo "    - 1044 / Unknown database -> nguoi dung dung nhung khong co quyen tren\n";
    echo "                             database do. Xem: SHOW GRANTS FOR '...'@'localhost';\n";
    echo "    - 2002 Connection refused -> MariaDB chua chay: systemctl status mariadb\n";
}
echo "\n";
