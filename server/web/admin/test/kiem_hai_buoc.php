<?php
/**
 * Kiểm thử xác thực hai bước TOTP (web/admin/inc/hai_buoc.php).
 *
 * Chạy KHÔNG cần MySQL: chỉ kiểm phần thuật toán và phần thuần, không chạm bảng.
 *
 * Cột sống của tệp này là BỘ VECTOR CHÍNH THỨC TRONG RFC 6238 phụ lục B. Một cài
 * đặt TOTP sai vẫn "chạy được" một cách hoàn hảo — nó sinh ra mã 6 số đẹp đẽ, đổi
 * mỗi 30 giây, và khớp với chính nó nên tự kiểm thì luôn đúng. Chỉ tới khi người
 * dùng mở Google Authenticator ra mới phát hiện hai bên ra hai số khác nhau. Đối
 * chiếu với vector của RFC là cách duy nhất biết mình khớp với PHẦN CÒN LẠI CỦA
 * THẾ GIỚI chứ không chỉ khớp với chính mình.
 *
 * Chạy: php web/admin/test/kiem_hai_buoc.php
 */
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

require_once __DIR__ . '/../inc/hai_buoc.php';

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
echo "\n== Base32 (RFC 4648)\n";
// ===========================================================================

// Vector trong RFC 4648 muc 10.
kt('ma hoa "f"',      hbMaHoaBase32('f'),      'MY');
kt('ma hoa "fo"',     hbMaHoaBase32('fo'),     'MZXQ');
kt('ma hoa "foo"',    hbMaHoaBase32('foo'),    'MZXW6');
kt('ma hoa "foob"',   hbMaHoaBase32('foob'),   'MZXW6YQ');
kt('ma hoa "fooba"',  hbMaHoaBase32('fooba'),  'MZXW6YTB');
kt('ma hoa "foobar"', hbMaHoaBase32('foobar'), 'MZXW6YTBOI');

kt('giai "MZXW6YTBOI"', hbGiaiBase32('MZXW6YTBOI'), 'foobar');
kt('giai lai duoc chuoi ban dau', hbGiaiBase32(hbMaHoaBase32('foobar')), 'foobar');

// Khoa in ra cho nguoi doc duoc chia nhom 4, va nguoi ta hay dan lai ca khoang trang.
kt('giai duoc khoa co dau cach', hbGiaiBase32('MZXW 6YTB OI'), 'foobar');
kt('giai duoc khoa chu thuong',  hbGiaiBase32('mzxw6ytboi'),   'foobar');
kt('giai duoc khoa co dau "="',  hbGiaiBase32('MZXW6YTBOI='),  'foobar');
kt('ky tu la -> null',           hbGiaiBase32('MZXW6YTB!!'),   null);
// '0', '1', '8', '9' KHONG nam trong bang chu Base32 - de lan voi O, I, B, g.
kt('chu so 0 khong hop le',      hbGiaiBase32('MZXW0YTB'),     null);
kt('chuoi rong -> null',         hbGiaiBase32(''),             null);

// ===========================================================================
echo "\n== TOTP: vector chinh thuc trong RFC 6238 phu luc B\n";
// ===========================================================================

// RFC dung khoa ASCII "12345678901234567890" (20 byte, dung co cua SHA1).
$khoaAscii = '12345678901234567890';
$khoa = hbMaHoaBase32($khoaAscii);
kt('khoa cua RFC ra dung Base32', $khoa, 'GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ');
kt('giai lai ra dung chuoi ASCII', hbGiaiBase32($khoa), $khoaAscii);

// Bang trong RFC ghi ma 8 chu so; ban 6 chu so la 6 ky tu cuoi cua no.
//   T (giay)      ma 8 so     ma 6 so
//   59            94287082    287082
//   1111111109    07081804    081804
//   1111111111    14050471    050471
//   1234567890    89005924    005924
//   2000000000    69279037    279037
//   20000000000   65353130    353130
$vector = [
    [59,          '287082'],
    [1111111109,  '081804'],
    [1111111111,  '050471'],
    [1234567890,  '005924'],
    [2000000000,  '279037'],
    [20000000000, '353130'],
];
foreach ($vector as [$giay, $mongDoi]) {
    kt("T=$giay", hbMaCuaBuoc($khoaAscii, hbBuoc($giay)), $mongDoi);
}

// Buoc phai la floor(T / 30) - lech mot buoc la lech ca ma.
kt('buoc cua T=59',         hbBuoc(59),         1);
kt('buoc cua T=1111111109', hbBuoc(1111111109), 37037036);

// ===========================================================================
echo "\n== Kiem ma nguoi dung go vao\n";
// ===========================================================================

$luc = 1111111109;
$buocGio = hbBuoc($luc);
$maDung = hbMaCuaBuoc($khoaAscii, $buocGio);

kt('ma dung -> tra ve dung buoc', hbKiemMa($khoa, $maDung, 0, $luc), $buocGio);
kt('ma sai -> null',              hbKiemMa($khoa, '000000', 0, $luc), null);
kt('thieu chu so -> null',        hbKiemMa($khoa, '12345', 0, $luc),  null);
kt('thua chu so -> null',         hbKiemMa($khoa, '1234567', 0, $luc), null);
kt('rong -> null',                hbKiemMa($khoa, '', 0, $luc),        null);

// Dong ho dien thoai va dong ho may chu khong bao gio khop tuyet doi.
kt('ma cua buoc TRUOC van nhan (lech -30s)',
    hbKiemMa($khoa, hbMaCuaBuoc($khoaAscii, $buocGio - 1), 0, $luc), $buocGio - 1);
kt('ma cua buoc SAU van nhan (lech +30s)',
    hbKiemMa($khoa, hbMaCuaBuoc($khoaAscii, $buocGio + 1), 0, $luc), $buocGio + 1);
kt('nhung lech 2 buoc thi khong',
    hbKiemMa($khoa, hbMaCuaBuoc($khoaAscii, $buocGio + 2), 0, $luc), null);
kt('lech -2 buoc cung khong',
    hbKiemMa($khoa, hbMaCuaBuoc($khoaAscii, $buocGio - 2), 0, $luc), null);

// ---- CHONG DUNG LAI ----
// Phep thu quan trong: ai nhin qua vai hoac bat duoc goi tin (trang admin dang
// chay HTTP tran!) khong duoc dung lai ma do trong 90 giay con lai.
kt('ma da dung roi -> khong nhan lai',
    hbKiemMa($khoa, $maDung, $buocGio, $luc), null);
kt('va moi ma CU hon buoc da dung cung bi chan',
    hbKiemMa($khoa, hbMaCuaBuoc($khoaAscii, $buocGio - 1), $buocGio, $luc), null);
kt('ma cua buoc SAU van vao duoc sau khi da dung buoc nay',
    hbKiemMa($khoa, hbMaCuaBuoc($khoaAscii, $buocGio + 1), $buocGio, $luc), $buocGio + 1);

// Khoa hong thi phai tu choi, khong duoc nem loi ra man hinh dang nhap.
kt('khoa hong -> null', hbKiemMa('KHONG!!PHAI@@BASE32', $maDung, 0, $luc), null);
kt('khoa rong -> null', hbKiemMa('', $maDung, 0, $luc), null);

// ===========================================================================
echo "\n== Sinh khoa\n";
// ===========================================================================

$k1 = hbSinhKhoa();
$k2 = hbSinhKhoa();
kt('khoa dai 32 ky tu (160 bit)', strlen($k1), 32);
ktDung('hai lan sinh ra khac nhau', $k1 !== $k2);
ktDung('giai duoc ra 20 byte', hbGiaiBase32($k1) !== null && strlen((string)hbGiaiBase32($k1)) === 20);
ktDung('chi gom chu Base32', preg_match('/^[A-Z2-7]+$/', $k1) === 1);

kt('chia nhom 4 cho de doc',
    hbKhoaDeDoc('GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ'),
    'GEZD GNBV GY3T QOJQ GEZD GNBV GY3T QOJQ');
ktDung('khoa chia nhom van giai lai duoc',
    hbGiaiBase32(hbKhoaDeDoc($k1)) === hbGiaiBase32($k1));

// ===========================================================================
echo "\n== URI otpauth\n";
// ===========================================================================

$uri = hbUri('GEZDGNBVGY3TQOJQ', 'hoahv');
ktDung('bat dau bang otpauth://totp/', strpos($uri, 'otpauth://totp/') === 0);
ktDung('co khoa bi mat',   strpos($uri, 'secret=GEZDGNBVGY3TQOJQ') !== false);
ktDung('ghi ro thuat toan', strpos($uri, 'algorithm=SHA1') !== false);
ktDung('ghi ro so chu so',  strpos($uri, 'digits=6') !== false);
ktDung('ghi ro chu ky',     strpos($uri, 'period=30') !== false);
ktDung('co issuer rieng',   strpos($uri, 'issuer=') !== false);
// Ten tai khoan co ky tu la thi phai duoc ma hoa, khong duoc lam vo URI.
ktDung('ten co dau cach duoc ma hoa',
    strpos(hbUri('AAAA', 'ten co dau cach'), 'ten%20co%20dau%20cach') !== false);

// ===========================================================================
echo "\n== Ma du phong\n";
// ===========================================================================

$maDuPhong = hbSinhMaDuPhong();
kt('sinh dung 8 ma', count($maDuPhong), 8);
ktDung('dinh dang abcd-efgh', preg_match('/^[a-z2-9]{4}-[a-z2-9]{4}$/', $maDuPhong[0]) === 1);
// Bo 0/o/1/l/i: nguoi dang cuong vi mat dien thoai rat de go nham.
ktDung('khong co ky tu de lan (0 1 l i o)',
    preg_match('/[01lio]/', implode('', $maDuPhong)) === 0);
ktDung('8 ma khac nhau het', count(array_unique($maDuPhong)) === 8);

$bam = hbBamMaDuPhong($maDuPhong);
ktDung('ban bam KHONG chua ma doc duoc', strpos($bam, $maDuPhong[0]) === false);
kt('bam ra 8 dong', count(explode("\n", $bam)), 8);

$conLai = hbDungMaDuPhong($bam, $maDuPhong[3]);
ktDung('ma dung -> tra ve danh sach con lai', $conLai !== null);
kt('con lai 7 ma', count(explode("\n", (string)$conLai)), 7);
// MOI MA DUNG MOT LAN. Thieu phep nay thi mot ma bi lo dung duoc mai mai.
kt('dung lai chinh ma do -> khong duoc', hbDungMaDuPhong((string)$conLai, $maDuPhong[3]), null);
ktDung('cac ma khac van dung duoc', hbDungMaDuPhong((string)$conLai, $maDuPhong[0]) !== null);
kt('ma khong ton tai -> null', hbDungMaDuPhong($bam, 'aaaa-bbbb'), null);
kt('ma rong -> null',          hbDungMaDuPhong($bam, ''), null);
kt('danh sach rong -> null',   hbDungMaDuPhong('', $maDuPhong[0]), null);
// Nguoi ta hay go hoa hoac dan kem khoang trang.
ktDung('nhan ma go bang chu HOA',
    hbDungMaDuPhong($bam, strtoupper($maDuPhong[1])) !== null);
ktDung('nhan ma co khoang trang hai dau',
    hbDungMaDuPhong($bam, '  ' . $maDuPhong[2] . '  ') !== null);

// Dung het thi con chuoi rong, khong phai loi.
$hetDan = $bam;
foreach ($maDuPhong as $m) {
    $hetDan = (string)hbDungMaDuPhong($hetDan, $m);
}
kt('dung het 8 ma -> chuoi rong', $hetDan, '');

// ===========================================================================
echo "\n== Ma nguon: cac chot phai con nguyen\n";
// ===========================================================================

$hb = (string)file_get_contents(__DIR__ . '/../inc/hai_buoc.php');
ktDung('so sanh ma bang hash_equals (thoi gian hang)', strpos($hb, 'hash_equals') !== false);
ktDung('khoa sinh bang random_bytes', strpos($hb, 'random_bytes') !== false);
ktDung('ma du phong sinh bang random_int', strpos($hb, 'random_int') !== false);
// rand()/mt_rand() doan duoc, tuyet doi khong dung cho khoa bi mat.
ktDung('KHONG dung mt_rand/rand cho khoa',
    preg_match('/[^_a-z]m?t?_?rand\s*\(/', str_replace(['random_bytes', 'random_int'], '', $hb)) === 0);
ktDung('ma du phong duoc bam truoc khi luu', strpos($hb, 'password_hash') !== false);
ktDung('lech buoc chi 1 (khong noi long)', HB_LECH_BUOC === 1);
ktDung('6 chu so', HB_SO_CHU_SO === 6);
ktDung('buoc 30 giay', HB_BUOC_GIAY === 30);
// Web KHONG duoc co duong nao tao/doi khoa - ghi danh phai qua SSH.
$dangNhap = (string)file_get_contents(__DIR__ . '/../index.php');
ktDung('trang dang nhap KHONG sinh khoa bi mat',
    strpos($dangNhap, 'hbSinhKhoa') === false);

// ===========================================================================
echo "\n===================================\n";
if ($hong === 0) {
    echo "DAT: $dat kiem tra\n";
    exit(0);
}
echo "DAT: $dat   HONG: $hong\n";
exit(1);
