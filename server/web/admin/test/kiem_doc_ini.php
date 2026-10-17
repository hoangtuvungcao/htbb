<?php
/**
 * Kiểm thử việc đọc thông tin database từ server.ini của game server
 * (web/inc/doc_server_ini.php).
 *
 * Đây là thứ quyết định website có kết nối được database hay không, nên nó phải
 * đúng với đủ mọi hình dạng file thật: bản Windows (CRLF), bản VPS Linux, có
 * chú thích, mật khẩu chứa dấu bằng, thiếu file...
 *
 * Chạy: php web/admin/test/kiem_doc_ini.php
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

require_once __DIR__ . '/../../inc/doc_server_ini.php';

$dat = 0;
$hong = 0;
$goc = sys_get_temp_dir() . '/htbb_kiem_ini_' . getmypid();

/** Dựng một thư mục server giả với nội dung hai file ini cho trước. */
function dungThuMuc(string $goc, ?string $noiDungGame, ?string $noiDungLogin): void
{
    if (is_dir($goc)) {
        @unlink($goc . '/server.ini');
        @unlink($goc . '/loginServer/server.ini');
        @rmdir($goc . '/loginServer');
        @rmdir($goc);
    }
    mkdir($goc . '/loginServer', 0777, true);
    if ($noiDungGame !== null) {
        file_put_contents($goc . '/server.ini', $noiDungGame);
    }
    if ($noiDungLogin !== null) {
        file_put_contents($goc . '/loginServer/server.ini', $noiDungLogin);
    }
}

function kt(string $ten, $thucTe, $mongDoi): void
{
    global $dat, $hong;
    if ($thucTe === $mongDoi) {
        $dat++;
        echo "  OK   $ten = " . var_export($thucTe, true) . "\n";
    } else {
        $hong++;
        echo "  HONG $ten: duoc " . var_export($thucTe, true)
           . ", mong doi " . var_export($mongDoi, true) . "\n";
    }
}

// ---------------------------------------------------------------------------
echo "== 1. Hinh dang VPS Linux that (vps/server.ini.linux.example) ==\n";
dungThuMuc($goc,
    "#Tien Len TeaM Server\nsv.port=19129\nlimit.ccu=20000\n\n"
  . "# ==== DATABASE ====\ndb.host=localhost:3306\ndb.name=kpah_db\n"
  . "db.user=kpah\ndb.password=MatKhauThat123\ndb.maxco=100\n",
    "db.url=jdbc:mysql://localhost:3306/account\ndb.user=kpah\ndb.password=MatKhauThat123\nport=8023\n");
$c = htbbCauHinhDb($goc);
kt('host', $c['host'], 'localhost');
kt('port', $c['port'], 3306);
kt('user', $c['user'], 'kpah');
kt('pass', $c['pass'], 'MatKhauThat123');
kt('game', $c['game'], 'kpah_db');
kt('account', $c['account'], 'account');
kt('account_user', $c['account_user'], 'kpah');

echo "\n== 2. Login server dung nguoi dung KHAC ==\n";
dungThuMuc($goc,
    "db.host=localhost:3306\ndb.name=kpah_db\ndb.user=kpah\ndb.password=mkGame\n",
    "db.url=jdbc:mysql://localhost:3306/account\ndb.user=kpah_login\ndb.password=mkLogin\n");
$c = htbbCauHinhDb($goc);
kt('user (game)', $c['user'], 'kpah');
kt('account_user', $c['account_user'], 'kpah_login');
kt('account_pass', $c['account_pass'], 'mkLogin');

echo "\n== 3. Ket thuc dong kieu Windows (CRLF) ==\n";
// Sua server.ini tren Windows roi tai len VPS -> moi dong con du ky tu \r.
// Khong cat bo thi mat khau thanh "mkGame\r" va MySQL bao Access denied.
dungThuMuc($goc,
    "db.host=localhost:3306\r\ndb.name=kpah_db\r\ndb.user=kpah\r\ndb.password=mkGame\r\n",
    "db.url=jdbc:mysql://localhost:3306/account\r\ndb.user=kpah\r\n");
$c = htbbCauHinhDb($goc);
kt('user khong dinh \\r', $c['user'], 'kpah');
kt('pass khong dinh \\r', $c['pass'], 'mkGame');
kt('game khong dinh \\r', $c['game'], 'kpah_db');
kt('account khong dinh \\r', $c['account'], 'account');

echo "\n== 4. Mat khau chua dau bang va khoang trang quanh dau bang ==\n";
dungThuMuc($goc,
    "db.host = localhost:3306 \n db.user = kpah \ndb.password=a=b=c==\ndb.name=kpah_db\n", null);
$c = htbbCauHinhDb($goc);
kt('host bo khoang trang', $c['host'], 'localhost');
kt('user bo khoang trang', $c['user'], 'kpah');
kt('pass giu nguyen dau bang', $c['pass'], 'a=b=c==');

echo "\n== 5. Mat khau RONG khac voi KHONG CO khoa ==\n";
dungThuMuc($goc, "db.host=127.0.0.1:3307\ndb.user=root\ndb.password=\n", null);
$c = htbbCauHinhDb($goc);
kt('pass rong tra ve chuoi rong', $c['pass'], '');
kt('port khac 3306', $c['port'], 3307);
kt('game khong co -> null', $c['game'], null);

echo "\n== 6. db.host khong ghi cong ==\n";
dungThuMuc($goc, "db.host=db.noibo\ndb.user=kpah\n", null);
$c = htbbCauHinhDb($goc);
kt('host', $c['host'], 'db.noibo');
kt('port -> null (ben goi tu dat mac dinh)', $c['port'], null);

echo "\n== 7. Chu thich va dong rac ==\n";
dungThuMuc($goc,
    "# day la chu thich\n; day cung la chu thich\n\n   \n"
  . "khong_co_dau_bang\ndb.user=kpah\n#db.password=dungLayCaiNay\ndb.password=that\n", null);
$c = htbbCauHinhDb($goc);
kt('bo qua dong chu thich', $c['pass'], 'that');
kt('user', $c['user'], 'kpah');

echo "\n== 8. db.url co tham so phia sau ==\n";
dungThuMuc($goc, null,
    "db.url=jdbc:mysql://localhost:3306/account?useUnicode=true&characterEncoding=UTF-8\ndb.user=kpah\n");
$c = htbbCauHinhDb($goc);
kt('account', $c['account'], 'account');

echo "\n== 9. Khong co file nao -> tat ca null ==\n";
dungThuMuc($goc, null, null);
$c = htbbCauHinhDb($goc);
foreach (['host', 'port', 'user', 'pass', 'game', 'account', 'account_user'] as $k) {
    kt("$k", $c[$k], null);
}

echo "\n== 10. Thu muc khong ton tai -> khong vo, tra null ==\n";
$c = htbbCauHinhDb($goc . '_khong_co_that');
kt('host', $c['host'], null);
kt('so khoa tra ve', count($c), 8);

// Don dep
@unlink($goc . '/server.ini');
@unlink($goc . '/loginServer/server.ini');
@rmdir($goc . '/loginServer');
@rmdir($goc);

echo "\n===================================\n";
echo "DAT: $dat   HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
