<?php
/**
 * ============================================================================
 *  GHI DANH XÁC THỰC HAI BƯỚC CHO MỘT TÀI KHOẢN ADMIN
 * ============================================================================
 *
 *  Chạy trên máy chủ qua SSH:
 *
 *      cd /opt/kpah
 *      sudo -u www-data php web/admin/tao_ma_2buoc.php <tên tài khoản>
 *
 *  Vì sao phải là `sudo -u www-data`: tệp cấu hình `web/config.local.php` chỉ
 *  cho www-data đọc (chmod 640). Chạy bằng root cũng đọc được, nhưng chạy đúng
 *  người dùng của web là cách chắc chắn rằng chính trang web cũng đọc được —
 *  nếu lệnh này chạy được mà trang lại báo lỗi kết nối thì vấn đề nằm ở chỗ khác.
 *
 *  ------------------------------------------------------------------------
 *   VÌ SAO LỆNH NÀY KHÔNG MỞ ĐƯỢC QUA TRÌNH DUYỆT
 *  ------------------------------------------------------------------------
 *  Thư mục `web/admin/` nằm dưới web root (/var/www/html/kpah là symlink tới
 *  /opt/kpah/web), nên nếu không chặn thì bất kỳ ai trên Internet cũng gọi được
 *  `…/kpah/admin/tao_ma_2buoc.php` và **tự sinh khoá hai bước cho tài khoản của
 *  bạn**. Đó sẽ là một cửa hậu tệ hơn cả việc không có 2FA. Apache ở đây đặt
 *  `AllowOverride None` nên `.htaccess` bị bỏ qua — chốt PHẢI nằm trong chính
 *  tệp PHP mới có tác dụng, và đó là dòng `PHP_SAPI !== 'cli'` ngay dưới đây.
 *
 *  ------------------------------------------------------------------------
 *   SAU KHI GHI DANH XONG CHO CẢ ĐỘI
 *  ------------------------------------------------------------------------
 *  Thêm vào `web/config.local.php`:
 *
 *      define('ADMIN_2FA_BAT_BUOC', true);
 *
 *  Từ đó tài khoản admin nào chưa có khoá là KHÔNG vào được, kể cả đúng mật
 *  khẩu. Chưa đặt dòng này thì người chưa ghi danh vẫn vào được bằng mật khẩu —
 *  tức là kẻ trộm mật khẩu cũng vậy. **Đừng dừng ở bước ghi danh.**
 */
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

require_once __DIR__ . '/inc/boot.php';
require_once __DIR__ . '/inc/hai_buoc.php';

$ten = strtolower(trim((string)($argv[1] ?? '')));

if ($ten === '' || $ten === '-h' || $ten === '--help') {
    echo "\n";
    echo "GHI DANH XAC THUC HAI BUOC (TOTP) CHO TRANG QUAN TRI\n";
    echo "====================================================\n\n";
    echo "  Cach dung:\n";
    echo "    sudo -u www-data php web/admin/tao_ma_2buoc.php <ten tai khoan>\n\n";
    echo "  <ten tai khoan> la ten DANG NHAP (cot team_user.username),\n";
    echo "  khong phai ten nhan vat trong game.\n\n";
    echo "  Xem ai da bat hai buoc:\n";
    echo "    sudo -u www-data php web/admin/tao_ma_2buoc.php --danh-sach\n\n";
    echo "  Go hai buoc cua mot tai khoan (mat dien thoai, het ma du phong):\n";
    echo "    mysql -u root kpah_db\n";
    // Xuong dong de o echo RIENG, dung gop vao chuoi SQL: kiem_schema.php quet
    // moi chuoi co tu khoa SQL trong cac tep nay va doi chieu tung ten cot voi
    // schema thuc, nen mot "\n" dinh o cuoi cau se bi doc thanh ten cot 'n'.
    echo "    DELETE FROM web_admin_2fa WHERE admin_user = 'ten';";
    echo "\n\n";
    exit(1);
}

// ---------------------------------------------------------------------------
//  Liệt kê
// ---------------------------------------------------------------------------
if ($ten === '--danh-sach' || $ten === '--list') {
    if (!hbCoBang()) {
        echo "Chua co bang " . HB_BANG . " - chua ai ghi danh.\n";
        exit(0);
    }
    $dong = dbGame()->query('SELECT admin_user, kich_hoat, tao_luc, dung_luc
                             FROM ' . HB_BANG . ' ORDER BY admin_user')->fetchAll();
    if (!$dong) {
        echo "Bang co nhung chua ai ghi danh.\n";
    }
    printf("\n%-20s %-8s %-20s %-20s %s\n", 'TAI KHOAN', 'BAT', 'GHI DANH LUC', 'DUNG LAN CUOI', 'MA DU PHONG');
    echo str_repeat('-', 92) . "\n";
    foreach ($dong as $d) {
        printf("%-20s %-8s %-20s %-20s %d con lai\n",
            $d['admin_user'],
            ((int)$d['kich_hoat'] === 1 ? 'co' : 'KHONG'),
            (string)($d['tao_luc'] ?? '-'),
            (string)($d['dung_luc'] ?? 'chua dung'),
            hbSoMaDuPhongConLai((string)$d['admin_user']));
    }
    echo "\n";
    echo hbBatBuocTatCa()
        ? "ADMIN_2FA_BAT_BUOC = true  -> tai khoan CHUA ghi danh se KHONG vao duoc.\n"
        : "ADMIN_2FA_BAT_BUOC chua dat -> tai khoan chua ghi danh VAN vao duoc bang mat khau.\n"
          . "  Ghi danh xong cho ca doi thi them vao web/config.local.php:\n"
          . "      define('ADMIN_2FA_BAT_BUOC', true);\n";
    echo "\n";
    exit(0);
}

// ---------------------------------------------------------------------------
//  Kiểm tra tài khoản có thật và có quyền admin
// ---------------------------------------------------------------------------
// Ghi danh cho một tên gõ sai chính tả sẽ tạo ra một dòng mà không đăng nhập nào
// đọc tới, và người ta sẽ ngồi thắc mắc vì sao quét mã xong vẫn không bị hỏi mã.
if (!laTaiKhoanAdmin($ten)) {
    echo "[DUNG LAI] '$ten' khong nam trong ADMIN_ACCOUNTS cua web/config.local.php.\n";
    echo "           Dang cho phep: " . (adminAccounts() ? implode(', ', adminAccounts()) : '(rong - khong ai vao duoc)') . "\n";
    echo "           Them ten vao do truoc, roi chay lai lenh nay.\n";
    exit(1);
}

try {
    $st = dbAccount()->prepare('SELECT username FROM team_user WHERE username = ? LIMIT 1');
    $st->execute([$ten]);
    if (!$st->fetch()) {
        echo "[DUNG LAI] Khong co tai khoan game nao ten '$ten' trong bang team_user.\n";
        exit(1);
    }
} catch (Throwable $ex) {
    echo "[LOI] Khong doc duoc bang team_user: " . $ex->getMessage() . "\n";
    echo "      Soi ky bang: sudo -u www-data php web/admin/test/chan_doan_ket_noi.php\n";
    exit(1);
}

// ---------------------------------------------------------------------------
//  Tạo bảng nếu chưa có
// ---------------------------------------------------------------------------
try {
    dbGame()->exec(
        'CREATE TABLE IF NOT EXISTS ' . HB_BANG . ' ('
        . '`admin_user`   VARCHAR(50) NOT NULL,'
        . '`khoa_bimat`   VARCHAR(64) NOT NULL,'
        . '`kich_hoat`    TINYINT     NOT NULL DEFAULT 0,'
        . '`buoc_da_dung` BIGINT      NOT NULL DEFAULT 0,'
        . '`ma_du_phong`  TEXT        NULL,'
        . '`tao_luc`      DATETIME    NULL,'
        . '`dung_luc`     DATETIME    NULL,'
        . 'PRIMARY KEY (`admin_user`)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
    );
} catch (Throwable $ex) {
    echo "[LOI] Khong tao duoc bang " . HB_BANG . ": " . $ex->getMessage() . "\n";
    exit(1);
}

// ---------------------------------------------------------------------------
//  Cảnh báo nếu đã ghi danh rồi
// ---------------------------------------------------------------------------
$daCo = hbDong($ten);
if ($daCo !== null && (int)$daCo['kich_hoat'] === 1) {
    echo "\n";
    echo "CANH BAO: '$ten' DA bat hai buoc tu " . (string)($daCo['tao_luc'] ?? '?') . ".\n";
    echo "          Tao khoa moi se LAM MAT hieu luc khoa cu va toan bo ma du phong cu.\n";
    echo "          Dien thoai dang dung se khong con vao duoc, phai quet lai ma moi.\n\n";
    echo "Go dung chu 'DONG Y' de tiep tuc: ";
    $traLoi = trim((string)fgets(STDIN));
    if ($traLoi !== 'DONG Y') {
        echo "Da huy, khong doi gi.\n";
        exit(1);
    }
    echo "\n";
}

// ---------------------------------------------------------------------------
//  Sinh khoá + mã dự phòng
// ---------------------------------------------------------------------------
$khoa = hbSinhKhoa();
$maDuPhong = hbSinhMaDuPhong();

try {
    $st = dbGame()->prepare(
        'INSERT INTO ' . HB_BANG . ' (admin_user, khoa_bimat, kich_hoat, buoc_da_dung, ma_du_phong, tao_luc)
         VALUES (?, ?, 1, 0, ?, NOW())
         ON DUPLICATE KEY UPDATE khoa_bimat = VALUES(khoa_bimat), kich_hoat = 1,
                                 buoc_da_dung = 0, ma_du_phong = VALUES(ma_du_phong),
                                 tao_luc = NOW(), dung_luc = NULL'
    );
    $st->execute([$ten, $khoa, hbBamMaDuPhong($maDuPhong)]);
} catch (Throwable $ex) {
    echo "[LOI] Khong ghi duoc vao " . HB_BANG . ": " . $ex->getMessage() . "\n";
    exit(1);
}

ghiNhatKy('2fa_ghi_danh', $ten, 'tao khoa hai buoc moi qua dong lenh');

// ---------------------------------------------------------------------------
//  In ra
// ---------------------------------------------------------------------------
$vach = str_repeat('=', 68);
echo "$vach\n";
echo "  DA BAT XAC THUC HAI BUOC CHO: $ten\n";
echo "$vach\n\n";

echo "BUOC 1 - Mo ung dung xac thuc tren dien thoai\n";
echo "  Google Authenticator, Microsoft Authenticator, Authy, 2FAS... deu duoc.\n\n";

echo "BUOC 2 - Them tai khoan bang cach NHAP KHOA THU CONG\n";
echo "  Chon \"Nhap khoa thiet lap\" / \"Enter setup key\", roi go:\n\n";
echo "      Ten     : KPAH Admin ($ten)\n";
echo "      Khoa    : " . hbKhoaDeDoc($khoa) . "\n";
echo "      Loai    : Theo thoi gian (Time based)\n\n";
echo "  Khoang trang trong khoa chi de de doc, go lien hay co cach deu duoc.\n\n";

echo "  Hoac neu ung dung nhan duong dan otpauth:// thi dan nguyen dong nay:\n\n";
echo "      " . hbUri($khoa, $ten) . "\n\n";

echo "BUOC 3 - LUU 8 MA DU PHONG NAY VAO CHO AN TOAN\n";
echo "  Moi ma dung DUNG MOT LAN, thay cho ma 6 so khi mat dien thoai.\n";
echo "  In ra giay hoac luu vao trinh quan ly mat khau - DUNG luu trong may chu.\n";
echo "  Sau khi dong cua so nay se KHONG XEM LAI DUOC (trong CSDL chi luu ban bam).\n\n";
foreach ($maDuPhong as $i => $m) {
    printf("      %d. %s\n", $i + 1, $m);
}
echo "\n";

echo "BUOC 4 - Thu dang nhap NGAY BAY GIO, o mot cua so trinh duyet khac\n";
echo "  Dang nhap thanh cong roi hay dong phien hien tai. Neu ung dung khong sinh\n";
echo "  dung ma, xoa khoa di roi lam lai:\n";
echo "      mysql -u root kpah_db\n";
echo "      DELETE FROM web_admin_2fa WHERE admin_user = '$ten';";
echo "\n\n";

if (!hbBatBuocTatCa()) {
    echo "$vach\n";
    echo "  CON MOT BUOC NUA, DUNG QUEN\n";
    echo "$vach\n";
    echo "  Hien tai tai khoan admin CHUA ghi danh van vao duoc chi bang mat khau -\n";
    echo "  ke trom duoc mat khau cung vay. Ghi danh xong cho tat ca moi nguoi trong\n";
    echo "  ADMIN_ACCOUNTS roi them vao web/config.local.php:\n\n";
    echo "      define('ADMIN_2FA_BAT_BUOC', true);\n\n";
    echo "  Xem ai con thieu:  php web/admin/tao_ma_2buoc.php --danh-sach\n";
    echo "$vach\n\n";
}

exit(0);
