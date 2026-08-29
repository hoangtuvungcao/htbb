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
$dau = strpos($nguon, 'function doiFile(');
if ($dau === false) {
    fwrite(STDERR, "Khong tim thay ham doiFile() trong chandoan.php\n");
    exit(1);
}
// Cắt tới dấu } ở đầu dòng — quy ước thụt lề của file này
$cuoi = strpos($nguon, "\n}\n", $dau);
if ($cuoi === false) {
    fwrite(STDERR, "Khong tim thay diem ket thuc cua doiFile()\n");
    exit(1);
}
eval(substr($nguon, $dau, $cuoi - $dau + 3));

echo "== 1. File NHO hon nguong: doc het ==\n";
$nho = tempnam(sys_get_temp_dir(), 'kpahlog');
file_put_contents($nho, "dong mot\ndong hai\ndong ba\n");
$r = doiFile($nho, 64000);
dung('tra ve mang', is_array($r));
kt('doc du 26 byte', strlen($r['noi_dung']), 26);
kt('co = kich thuoc that', $r['co'], 26);
kt('khong bi cat', $r['cat'], false);
dung('giu nguyen noi dung', strpos($r['noi_dung'], 'dong mot') === 0);

echo "\n== 2. File LON hon nguong: chi doc phan cuoi ==\n";
$lon = tempnam(sys_get_temp_dir(), 'kpahlog');
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

$conScript = tempnam(sys_get_temp_dir(), 'kpahcon');
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
$rong = tempnam(sys_get_temp_dir(), 'kpahlog');
file_put_contents($rong, '');
$r = doiFile($rong);
dung('file rong van tra ve mang', is_array($r));
kt('file rong: noi dung la chuoi rong', $r['noi_dung'], '');
kt('file rong: co = 0', $r['co'], 0);
kt('thu muc -> null', doiFile(sys_get_temp_dir()), null);

$r = doiFile($lon, 1000);
dung('nguong nho hon van ton trong', strlen($r['noi_dung']) <= 1000);

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
