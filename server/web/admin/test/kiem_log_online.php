<?php
/**
 * Kiểm thử phần "Người chơi online" của trang Tổng quan.
 *
 * VÌ SAO CẦN: bảng `tob_log_online` do **Java** tạo (src/data/GhiSoOnline.java) còn
 * **PHP** đọc (web/admin/index.php). Hai file, hai ngôn ngữ, không có gì ràng buộc
 * chúng với nhau. Đổi tên một cột bên Java mà quên sửa bên PHP thì:
 *
 *   - `dem()` và `dsach()` trong index.php **nuốt mọi lỗi** (bắt Throwable, trả
 *     null/[]) — cố ý như vậy để bảng thiếu không làm vỡ cả trang.
 *   - Nên trang vẫn mở bình thường, chỉ hiện "—" và biểu đồ trống.
 *   - Không có dòng lỗi nào ở đâu cả.
 *
 * Tức là lỗi kiểu này KHÔNG BAO GIỜ tự lộ ra. Bộ kiểm này đọc thẳng hai file rồi
 * đối chiếu tên bảng và tên cột.
 *
 * Chạy: php web/admin/test/kiem_log_online.php
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
            . ", can " . var_export($mongDoi, true) . "\n";
    }
}

function dung(string $ten, bool $dk): void
{
    kt($ten, $dk, true);
}

$GOC = dirname(__DIR__, 3) . DIRECTORY_SEPARATOR;
$fJava = $GOC . 'src/data/GhiSoOnline.java';
$fPhp  = $GOC . 'web/admin/index.php';

echo "== Nguon du lieu\n";
dung('doc duoc src/data/GhiSoOnline.java', is_file($fJava));
dung('doc duoc web/admin/index.php', is_file($fPhp));
if (!is_file($fJava) || !is_file($fPhp)) {
    echo "\nKhong doc duoc ma nguon, dung o day.\n";
    exit(1);
}
$java = file_get_contents($fJava);
$php  = file_get_contents($fPhp);

// ---------------------------------------------------------------------------
//  1. Tên bảng phải khớp
// ---------------------------------------------------------------------------
echo "\n== Ten bang\n";

preg_match('/TEN_BANG\s*=\s*"([^"]+)"/', $java, $m);
$tenBangJava = $m[1] ?? '';
kt('Java khai bao TEN_BANG', $tenBangJava, 'tob_log_online');
dung('PHP dung dung ten bang do', strpos($php, $tenBangJava) !== false);

// ---------------------------------------------------------------------------
//  2. Cột PHP đọc phải là cột Java tạo
// ---------------------------------------------------------------------------
echo "\n== Cot: PHP doc gi thi Java phai tao cai do\n";

// Lấy khối CREATE TABLE trong Java rồi rút tên cột trong dấu backtick.
$cotJava = [];
if (preg_match('/CREATE TABLE IF NOT EXISTS.*?\)\s*DEFAULT CHARSET/s', $java, $mm)) {
    // Bỏ phần PRIMARY KEY / KEY để chỉ còn định nghĩa cột.
    foreach (preg_split('/"\s*\+\s*"/', $mm[0]) as $manh) {
        if (preg_match('/^\s*`([a-z_]+)`\s+[A-Z]/', $manh, $c)) {
            $cotJava[] = $c[1];
        }
    }
}
sort($cotJava);
kt('Java tao dung 4 cot', $cotJava, ['id', 'ket_noi', 'nhan_vat', 'thoi_gian']);

// Rút mọi câu SQL trong PHP có nhắc tới bảng này.
$sqlOnline = [];
foreach (preg_split("/[\r\n]/", $php) as $dong) {
    if (strpos($dong, 'tob_log_online') !== false) {
        $sqlOnline[] = $dong;
    }
}
dung('PHP co truy van bang nay', count($sqlOnline) > 0);

// Các câu SQL trải trên nhiều dòng: gom cả khối cho chắc.
$khoiSql = '';
if (preg_match_all('/(?:dem|dsach)\(\$game,\s*[\'"](.*?)[\'"]\s*\)/s', $php, $q)) {
    foreach ($q[1] as $cau) {
        if (strpos($cau, 'tob_log_online') !== false) {
            $khoiSql .= "\n" . $cau;
        }
    }
}
dung('gom duoc cac cau SQL nhieu dong', strpos($khoiSql, 'tob_log_online') !== false);

// Mọi định danh snake_case trong các câu đó phải là cột đã tạo (hoặc tên bảng).
$choPhep = array_merge($cotJava, ['tob_log_online', 'phut_truoc', 'gio', 'nhan']);
$laKhoaSql = static function (string $tu): bool {
    static $tuKhoa = [
        'select', 'from', 'where', 'group', 'order', 'limit', 'desc', 'asc', 'and', 'or',
        'as', 'by', 'max', 'min', 'avg', 'sum', 'count', 'round', 'now', 'curdate',
        'date', 'date_sub', 'date_format', 'timestampdiff', 'interval', 'day', 'hour',
        'minute', 'is', 'not', 'null', 'inner', 'join', 'on',
    ];
    return in_array($tu, $tuKhoa, true);
};

$la = [];
if (preg_match_all('/\b([a-z][a-z0-9_]{2,})\b/', $khoiSql, $tu)) {
    foreach (array_unique($tu[1]) as $t) {
        if (!$laKhoaSql($t) && !in_array($t, $choPhep, true)) {
            $la[] = $t;
        }
    }
}
kt('khong co dinh danh la nao trong SQL (cot sai ten se lot vao day)', $la, []);

foreach (['thoi_gian', 'ket_noi', 'nhan_vat'] as $cot) {
    dung("PHP co doc cot $cot", strpos($khoiSql, $cot) !== false);
}

// ---------------------------------------------------------------------------
//  3. Quy tắc "mẫu cũ thì không được coi là số hiện tại"
// ---------------------------------------------------------------------------
echo "\n== Moc coi mau la cu\n";

// Chu kỳ lấy mẫu bên Java, tính ra phút.
preg_match('/CHU_KY_MS\s*=\s*(\d+)L\s*\*\s*(\d+)L\s*\*\s*(\d+)L/', $java, $ck);
$chuKyPhut = (isset($ck[1], $ck[2], $ck[3]))
    ? (int)((int)$ck[1] * (int)$ck[2] * (int)$ck[3] / 60000)
    : 0;
kt('Java lay mau 5 phut mot lan', $chuKyPhut, 5);

preg_match("/phut_truoc'\]\s*>\s*(\d+)/", $php, $mn);
$mocCu = isset($mn[1]) ? (int)$mn[1] : 0;
dung('PHP coi la cu khi qua ' . $mocCu . ' phut', $mocCu > 0);
// Mốc phải lớn hơn ít nhất 2 chu kỳ, nếu không một lần lỡ nhịp là báo "server tắt".
dung('moc cu (' . $mocCu . ') > 2 chu ky lay mau (' . (2 * $chuKyPhut) . ')',
    $mocCu > 2 * $chuKyPhut);

// ---------------------------------------------------------------------------
//  4. Bảng thiếu / mẫu cũ phải có nhánh xử lý riêng
// ---------------------------------------------------------------------------
echo "\n== Cac truong hop bien\n";

dung('co nhanh cho truong hop chua co bang', strpos($php, '$onCoBang') !== false);
dung('bang thieu thi noi ro chu khong de bieu do trong',
    strpos($php, 'Chưa có bảng') !== false);

// BA trang thai khac nhau, moi cai can mot loi huong dan khac han:
//   chua co bang        -> tao bang / khoi dong server
//   co bang, 0 dong     -> jar chua duoc build lai  (day la cho da lam nguoi dung roi)
//   co du lieu          -> ve bieu do
dung('phan biet duoc "co bang nhung 0 dong" voi "chua co bang"',
    strpos($php, '$onSoMau') !== false && strpos($php, '(int)$onSoMau === 0') !== false);
dung('truong hop 0 dong co chi ro nguyen nhan la jar chua build lai',
    strpos($php, 'build lại') !== false && strpos($php, 'KPAH2_patched.jar') !== false);
dung('va co dua ra lenh tu kiem tra jar',
    strpos($php, 'jar tf') !== false);
dung('chi cach tao bang bang tay', strpos($php, 'them_bang_tob_log_online.sql') !== false);
dung('mau cu thi KHONG hien so cu nhu so hien tai',
    (bool)preg_match("/\\\$onCu\)\s*\{\s*\/\/.*?\n.*?theSo\('Đang online', '—'/s", $php));
dung('mau cu duoc to mau canh bao', strpos($php, "'nguy-hiem'") !== false);

// ---------------------------------------------------------------------------
//  5. Giữ 30 ngày, khớp chính sách dọn log sẵn có
// ---------------------------------------------------------------------------
echo "\n== Don dong cu\n";

preg_match('/SO_NGAY_GIU\s*=\s*(\d+)/', $java, $ng);
kt('Java giu lai 30 ngay', isset($ng[1]) ? (int)$ng[1] : 0, 30);
dung('Java co cau DELETE don dong cu', strpos($java, 'DELETE FROM') !== false);
dung('don theo hang so SO_NGAY_GIU chu khong ghi cung so trong SQL',
    strpos($java, 'INTERVAL ? DAY') !== false);

// ---------------------------------------------------------------------------
//  6. Đếm người chơi thật, không đếm lẫn bot
// ---------------------------------------------------------------------------
echo "\n== Dem nguoi choi that\n";

// Linh thue (charHire) duoc tao voi isBot = -1 y het nguoi choi that, nen KHONG
// duoc loc bang isBot. Bot deu tao bang new Char(null) -> session null.
dung('dem bang getSession() != null', strpos($java, 'getSession() != null') !== false);

// Chỉ soi phần MÃ: file này có hẳn một đoạn chú thích giải thích vì sao không được
// lọc bằng isBot, nên tìm cả file thì lúc nào cũng thấy chữ đó.
$javaKhongChuThich = '';
foreach (preg_split("/[\r\n]/", $java) as $dong) {
    $t = ltrim($dong);
    if ($t === '' || strncmp($t, '//', 2) === 0 || strncmp($t, '*', 1) === 0
        || strncmp($t, '/*', 2) === 0) {
        continue;
    }
    $javaKhongChuThich .= $dong . "\n";
}
dung('phan MA khong he loc bang isBot (loc the se dem lan linh thue)',
    strpos($javaKhongChuThich, 'isBot') === false);
dung('ghi ca so ket noi lan so nhan vat',
    strpos($java, 'demKetNoi') !== false && strpos($java, 'demNhanVat') !== false);

// ---------------------------------------------------------------------------
//  7. Trả kết nối về pool đúng một lần
// ---------------------------------------------------------------------------
echo "\n== Tra ket noi ve pool\n";

$soLayKetNoi = substr_count($java, 'Database.instance.getConnection()');
$soTra = substr_count($java, 'tra(conn)');
kt('moi lan lay ket noi deu co dung mot lan tra', $soTra, $soLayKetNoi);
dung('tra ket noi trong finally', strpos($java, 'finally') !== false);
dung('chan null truoc khi free (free(null) van giam countConnection)',
    strpos($java, 'if (conn != null)') !== false);

// ---------------------------------------------------------------------------
echo "\n============================================================\n";
if ($hong === 0) {
    echo "  DAT: $dat/$dat kiem tra\n";
} else {
    echo "  HONG: $hong/" . ($dat + $hong) . " kiem tra that bai\n";
}
echo "============================================================\n";
exit($hong === 0 ? 0 : 1);
