<?php
/**
 * Kiểm thử doiFile() — hàm đọc phần cuối file log của trang Chẩn đoán.
 *
 * Vì sao cần hẳn một bộ kiểm riêng cho một hàm 15 dòng: bản đầu tiên dùng
 * `stream_get_contents($fh)` không giới hạn độ dài. Nó *trả về* đúng 64 KB sau
 * con trỏ, nên mọi phép thử "nội dung có đúng không" đều đạt — nhưng bên trong
 * PHP cấp phát bộ đệm theo **cỡ CẢ FILE**. Với `game.out` 146 MB, Apache
 * (`memory_limit` 128M) chết ngay: `PHP Fatal error: Allowed memory size
 * exhausted`, người dùng thấy HTTP 500.
 *
 * Cái bẫy thật sự: chạy dưới CLI thì lỗi ẨN HOÀN TOÀN, vì `memory_limit` của
 * CLI là -1. Nên bộ kiểm này **tự đặt lại memory_limit** rồi mới đo, chứ không
 * tin vào cấu hình đang chạy.
 *
 * Chạy: php web/admin/test/kiem_doc_log.php
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

function dung(string $ten, bool $dieuKien): void
{
    kt($ten, $dieuKien, true);
}

// --- Lấy đúng hàm doiFile() từ chandoan.php ---------------------------------
//
// Không `require` cả file được: nó gọi requireAdmin() và đụng vào database.
// Trích riêng thân hàm ra để kiểm — cách này cũng bảo đảm bộ kiểm luôn chạy
// trên đúng đoạn mã đang được deploy, không phải một bản chép tay đã lỗi thời.
$nguon = file_get_contents(__DIR__ . '/../chandoan.php');
if ($nguon === false) {
    fwrite(STDERR, "Khong doc duoc chandoan.php\n");
    exit(1);
}
foreach (['doiFile(', 'chiaMuc(', 'daoThuTu('] as $ham) {
    $dau = strpos($nguon, 'function ' . $ham);
    if ($dau === false) {
        fwrite(STDERR, "Khong tim thay ham $ham trong chandoan.php\n");
        exit(1);
    }
    // Cắt tới dấu } ở đầu dòng — quy ước thụt lề của file này
    $cuoi = strpos($nguon, "\n}\n", $dau);
    if ($cuoi === false) {
        fwrite(STDERR, "Khong tim thay diem ket thuc cua $ham\n");
        exit(1);
    }
    eval(substr($nguon, $dau, $cuoi - $dau + 3));
}

echo "== 1. File NHO hon nguong: doc het ==\n";
$nho = tempnam(sys_get_temp_dir(), 'htbblog');
file_put_contents($nho, "dong mot\ndong hai\ndong ba\n");
$r = doiFile($nho, 64000);
dung('tra ve mang', is_array($r));
kt('doc du 26 byte', strlen($r['noi_dung']), 26);
kt('co = kich thuoc that', $r['co'], 26);
kt('khong bi cat', $r['cat'], false);
dung('giu nguyen noi dung', strpos($r['noi_dung'], 'dong mot') === 0);

echo "\n== 2. File LON hon nguong: chi doc phan cuoi ==\n";
$lon = tempnam(sys_get_temp_dir(), 'htbblog');
$fh = fopen($lon, 'wb');
for ($i = 0; $i < 200000; $i++) {
    fwrite($fh, str_pad("dong so $i", 60, '.') . "\n");
}
fwrite($fh, "DONG_CUOI_CUNG\n");
fclose($fh);
$coThat = filesize($lon);
dung('file thu nghiem lon hon 10 MB', $coThat > 10 * 1024 * 1024);

$r = doiFile($lon, 64000);
kt('co = kich thuoc that', $r['co'], $coThat);
kt('bao la da bi cat', $r['cat'], true);
dung('doc KHONG qua nguong 64000', strlen($r['noi_dung']) <= 64000);
dung('doc duoc gan day nguong', strlen($r['noi_dung']) > 60000);
dung('lay dung PHAN CUOI file', strpos($r['noi_dung'], 'DONG_CUOI_CUNG') !== false);
dung('KHONG chua phan dau file', strpos($r['noi_dung'], 'dong so 0.') === false);
dung('bo dong dau bi cat giua chung', $r['noi_dung'][0] === 'd' || $r['noi_dung'][0] === 'D');

echo "\n== 3. Bo nho: day la phep kiem quan trong nhat ==\n";
//
// PHAI chay trong TIEN TRINH CON voi memory_limit thap.
//
// Do trong cung tien trinh bang memory_get_usage()/memory_get_peak_usage() la
// KHONG AN TOAN: PHP cap phat 11 MB roi tra lai ngay trong ham, nen do xong
// thay "tang 0 KB" va ban loi van DAT. Da thu dung cach do va no khong bat
// duoc gi. Cach duy nhat trung thuc la tai hien dung canh o Apache: hanh muc
// bo nho co han, file lon hon han muc do, va xem tien trinh co chet khong.

$conScript = tempnam(sys_get_temp_dir(), 'htbbcon');
file_put_contents($conScript, <<<'PHP'
<?php
// Trich doiFile() tu chandoan.php y het tien trinh cha
$nguon = file_get_contents($argv[1]);
$dau = strpos($nguon, 'function doiFile(');
$cuoi = strpos($nguon, "\n}\n", $dau);
eval(substr($nguon, $dau, $cuoi - $dau + 3));
$r = doiFile($argv[2], 64000);
echo strlen($r['noi_dung']);
PHP
);

$lenh = escapeshellarg(PHP_BINARY) . ' -d memory_limit=8M '
      . escapeshellarg($conScript) . ' '
      . escapeshellarg(realpath(__DIR__ . '/../chandoan.php')) . ' '
      . escapeshellarg($lon) . ' 2>&1';
exec($lenh, $raDong, $ma);
$ra = implode("\n", $raDong);
unlink($conScript);

printf("  (file %s MB, han muc bo nho 8M, ma thoat %d)\n", round($coThat / 1048576, 1), $ma);
if ($ma !== 0) {
    echo "  " . trim(substr($ra, 0, 200)) . "\n";
}
kt('tien trinh con khong chet vi het bo nho', $ma, 0);
// So byte chinh xac phu thuoc dong bi cat roi vao dau, nen kiem theo khoang.
$soByteDoc = ctype_digit(trim($ra)) ? (int)trim($ra) : -1;
dung('van doc duoc gan 64 KB du han muc chi 8M',
    $soByteDoc > 60000 && $soByteDoc <= 64000);

echo "\n== 4. Truong hop bien ==\n";
kt('file khong ton tai -> null', doiFile('/khong/co/file/nao.txt'), null);
$rong = tempnam(sys_get_temp_dir(), 'htbblog');
file_put_contents($rong, '');
$r = doiFile($rong);
dung('file rong van tra ve mang', is_array($r));
kt('file rong: noi dung la chuoi rong', $r['noi_dung'], '');
kt('file rong: co = 0', $r['co'], 0);
kt('thu muc -> null', doiFile(sys_get_temp_dir()), null);

$r = doiFile($lon, 1000);
dung('nguong nho hon van ton trong', strlen($r['noi_dung']) <= 1000);

echo "\n== 5. Dao thu tu: moi nhat len dau ==\n";
//
// Phep kiem quan trong o day KHONG phai "co dao khong" ma la "dao xong con doc
// duoc khong". Dao tung dong mot se lon nguoc ca vet goi Java thanh rac.

// (a) File co dau thoi gian dau dong - errorlog.txt, hack.txt...
$coGio = "2026-08-29 10:00:00 | A | 1.1.1.1 | hackc | su kien CU\n"
       . "2026-08-29 11:00:00 | B | 2.2.2.2 | hackR | su kien GIUA\n"
       . "2026-08-29 12:00:00 | C | 3.3.3.3 | ffbug | su kien MOI";
$muc = chiaMuc($coGio);
kt('3 dong co gio -> 3 muc', count($muc), 3);
$dao = daoThuTu($coGio);
dung('su kien MOI len dau', strpos($dao, 'su kien MOI') < strpos($dao, 'su kien GIUA'));
dung('su kien CU xuong cuoi', strpos($dao, 'su kien CU') > strpos($dao, 'su kien GIUA'));
dung('dong dau tien la cua su kien moi nhat',
    strpos($dao, '2026-08-29 12:00:00') === 0);

// (b) Vet goi Java NHIEU DONG - day la cho de hong nhat
$vetGoi = "2026/07/30 08:12:33\n"
        . "java.lang.NullPointerException\n"
        . "\tat real.RealController.processMessage(RealController.java:981)\n"
        . "\tat io.Session.run(Session.java:172)\n"
        . "\n"
        . "2026/07/30 09:00:00\n"
        . "java.lang.ArrayIndexOutOfBoundsException: -1\n"
        . "\tat data.Pet.doChangeThongThao(Pet.java:384)\n";
$muc = chiaMuc($vetGoi);
kt('2 vet goi -> 2 muc', count($muc), 2);
$dao = daoThuTu($vetGoi);
dung('vet goi MOI (Pet) len truoc', strpos($dao, 'Pet.java:384') < strpos($dao, 'RealController.java:981'));
dung('vet goi CU van con nguyen thu tu ben trong',
    strpos($dao, 'RealController.java:981') < strpos($dao, 'Session.java:172'));
dung('dong "java.lang.NullPointerException" van dung truoc vet goi cua no',
    strpos($dao, 'java.lang.NullPointerException') < strpos($dao, 'RealController.java:981'));

// (c) File KHONG co dau thoi gian nao - game.out la stdout tho.
//     Phai dua vao thut le moi giu duoc vet goi.
$khongGio = "java.lang.ArrayIndexOutOfBoundsException: 0\n"
          . "\tat java.util.Vector.get(Vector.java:753)\n"
          . "\tat real.Map.deletePotionAndItemOnGround(Map.java:3586)\n"
          . "LOI TRONG HAM RUN MAP loi thu NHAT\n"
          . "java.lang.NullPointerException\n"
          . "\tat real.Region.run(Region.java:186)\n"
          . "LOI TRONG HAM RUN MAP loi thu HAI\n";
$muc = chiaMuc($khongGio);
kt('khong co gio -> chia theo thut le, duoc 4 muc', count($muc), 4);
$dao = daoThuTu($khongGio);
dung('loi thu HAI len truoc loi thu NHAT',
    strpos($dao, 'loi thu HAI') < strpos($dao, 'loi thu NHAT'));
dung('vet goi Vector.get van dinh voi dong ngoai le cua no',
    strpos($dao, 'ArrayIndexOutOfBounds') < strpos($dao, 'Vector.java:753'));
dung('vet goi Region van dinh voi NullPointerException cua no',
    strpos($dao, 'java.lang.NullPointerException') < strpos($dao, 'Region.java:186'));

// (d) Khong duoc lam mat dong nao
$soDongTruoc = count(preg_split("/\n/", trim($khongGio)));
$soDongSau   = count(preg_split("/\n/", trim($dao)));
kt('dao xong khong mat dong nao', $soDongSau, $soDongTruoc);

// (e) Truong hop bien
kt('chuoi rong -> 0 muc', count(chiaMuc('')), 0);
kt('mot muc duy nhat thi giu nguyen', daoThuTu("chi mot dong"), "chi mot dong");

unlink($nho);
unlink($lon);
unlink($rong);

echo "\n===================================\n";
if ($hong === 0) {
    echo "DAT: $dat   HONG: 0\n";
    exit(0);
}
echo "DAT: $dat   HONG: $hong\n";
exit(1);
