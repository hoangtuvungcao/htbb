<?php
/**
 * ============================================================================
 *  XÁC THỰC HAI BƯỚC (TOTP) CHO TRANG QUẢN TRỊ
 * ============================================================================
 *
 *  Cài đặt theo RFC 6238 (TOTP) + RFC 4226 (HOTP), thuật toán SHA1, 6 chữ số,
 *  bước 30 giây — đúng mặc định của Google Authenticator / Authy / 2FAS.
 *  Không dùng thư viện ngoài: `web/` không có composer và không được tải gì từ
 *  Internet, xem web/README.md.
 *
 *  ------------------------------------------------------------------------
 *   VÌ SAO GHI DANH PHẢI LÀM QUA SSH, KHÔNG PHẢI TRÊN WEB
 *  ------------------------------------------------------------------------
 *  Đây là chỗ dễ làm hỏng nhất của mọi hệ 2FA và cũng là lý do tồn tại của
 *  tệp này.
 *
 *  Nếu để người đã gõ đúng mật khẩu tự ghi danh trên web, thì kẻ trộm được
 *  mật khẩu cũng tự ghi danh được — nó quét mã QR bằng điện thoại CỦA NÓ rồi
 *  vào bình thường. Lúc đó 2FA không chặn ai cả, chỉ thêm một bước cho chủ nhà.
 *  Cái duy nhất làm 2FA có nghĩa là **khoá bí mật không bao giờ đi qua đường
 *  mà kẻ tấn công đang đứng**. Nên khoá được sinh bằng lệnh chạy trên máy chủ
 *  qua SSH (`php web/admin/tao_ma_2buoc.php <tên>`), và web KHÔNG có đường nào
 *  tạo hay đổi khoá.
 *
 *  ------------------------------------------------------------------------
 *   HAI MỨC BẮT BUỘC
 *  ------------------------------------------------------------------------
 *   - Tài khoản ĐÃ ghi danh: luôn bị hỏi mã, không có cách bỏ qua.
 *   - Tài khoản CHƯA ghi danh: mặc định vẫn vào được (kèm cảnh báo đỏ), để lần
 *     cập nhật đầu tiên không khoá bạn ra khỏi chính trang của mình. Sau khi
 *     đã ghi danh xong thì đặt trong web/config.local.php:
 *
 *         define('ADMIN_2FA_BAT_BUOC', true);
 *
 *     Từ đó tài khoản nào chưa có khoá là KHÔNG vào được, kể cả đúng mật khẩu.
 *     Đây mới là trạng thái đóng thật; đừng dừng ở bước ghi danh.
 *
 *  ------------------------------------------------------------------------
 *   MẤT ĐIỆN THOẠI THÌ SAO
 *  ------------------------------------------------------------------------
 *   1. Dùng một trong các mã dự phòng in ra lúc ghi danh (mỗi mã một lần).
 *   2. Hết mã dự phòng thì vào máy chủ bằng SSH và xoá khoá đi:
 *          DELETE FROM web_admin_2fa WHERE admin_user = 'tên';
 *      rồi ghi danh lại. Có SSH là luôn còn đường về — đó là chốt cuối, và nó
 *      cũng là lý do KHÔNG cần cửa hậu nào trên web.
 */

/** Số chữ số của mã. 6 = mặc định của mọi ứng dụng authenticator. */
const HB_SO_CHU_SO = 6;

/** Độ dài một bước thời gian (giây). */
const HB_BUOC_GIAY = 30;

/**
 * Cho phép lệch mấy bước về hai phía.
 *
 * 1 = ±30 giây. Cần có vì đồng hồ điện thoại và đồng hồ máy chủ gần như không
 * bao giờ khớp tuyệt đối. Đừng nâng lên cao: mỗi bước cho thêm là nhân đôi số
 * mã đang hợp lệ cùng lúc, tức là giảm đúng bấy nhiêu công sức đoán mò.
 */
const HB_LECH_BUOC = 1;

/** Số mã dự phòng sinh ra lúc ghi danh. */
const HB_SO_MA_DU_PHONG = 8;

// ===========================================================================
//  BASE32 (RFC 4648) — định dạng mà ứng dụng authenticator nhận
// ===========================================================================

const HB_BANG_CHU_BASE32 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';

/** Mã hoá chuỗi byte thô thành Base32 (không có dấu '=' đệm). */
function hbMaHoaBase32(string $tho): string
{
    if ($tho === '') {
        return '';
    }
    $bit = '';
    for ($i = 0, $n = strlen($tho); $i < $n; $i++) {
        $bit .= str_pad(decbin(ord($tho[$i])), 8, '0', STR_PAD_LEFT);
    }
    // Đệm cho đủ nhóm 5 bit. Đệm bằng 0 ở CUỐI là đúng chuẩn.
    $bit = str_pad($bit, (int)(ceil(strlen($bit) / 5) * 5), '0', STR_PAD_RIGHT);

    $ra = '';
    foreach (str_split($bit, 5) as $nhom) {
        $ra .= HB_BANG_CHU_BASE32[bindec($nhom)];
    }
    return $ra;
}

/**
 * Giải Base32 về chuỗi byte thô. Trả về null nếu có ký tự không hợp lệ.
 *
 * Nhận cả chữ thường và cả dấu cách / gạch ngang, vì khoá được in ra theo nhóm
 * 4 ký tự cho dễ đọc và người ta hay dán lại nguyên cả khoảng trắng.
 */
function hbGiaiBase32(string $ma): ?string
{
    $ma = strtoupper(preg_replace('/[\s-]+/', '', $ma) ?? '');
    $ma = rtrim($ma, '=');
    if ($ma === '') {
        return null;
    }
    $bit = '';
    for ($i = 0, $n = strlen($ma); $i < $n; $i++) {
        $viTri = strpos(HB_BANG_CHU_BASE32, $ma[$i]);
        if ($viTri === false) {
            return null;
        }
        $bit .= str_pad(decbin($viTri), 5, '0', STR_PAD_LEFT);
    }
    $ra = '';
    // Bỏ phần bit lẻ ở cuối: đó là bit đệm, không thuộc dữ liệu.
    foreach (str_split(substr($bit, 0, (int)(floor(strlen($bit) / 8) * 8)), 8) as $nhom) {
        $ra .= chr(bindec($nhom));
    }
    return $ra;
}

// ===========================================================================
//  TOTP
// ===========================================================================

/** Bước thời gian hiện tại. Tách riêng để test tiêm được mốc thời gian. */
function hbBuoc(?int $luc = null): int
{
    return (int)floor(($luc ?? time()) / HB_BUOC_GIAY);
}

/**
 * Sinh mã cho một bước cụ thể. $khoaTho là khoá đã giải Base32 (byte thô).
 *
 * Đây là HOTP của RFC 4226: HMAC-SHA1 rồi "dynamic truncation" — lấy 4 bit
 * cuối làm chỉ số, đọc 4 byte từ đó, xoá bit dấu, chia lấy dư 10^6.
 */
function hbMaCuaBuoc(string $khoaTho, int $buoc): string
{
    // 'J' = unsigned 64-bit big-endian, đúng như RFC yêu cầu (8 byte counter).
    $bam = hash_hmac('sha1', pack('J', $buoc), $khoaTho, true);
    $chiSo = ord($bam[19]) & 0x0F;
    $so = ((ord($bam[$chiSo]) & 0x7F) << 24)
        | ((ord($bam[$chiSo + 1]) & 0xFF) << 16)
        | ((ord($bam[$chiSo + 2]) & 0xFF) << 8)
        | (ord($bam[$chiSo + 3]) & 0xFF);
    return str_pad((string)($so % (10 ** HB_SO_CHU_SO)), HB_SO_CHU_SO, '0', STR_PAD_LEFT);
}

/**
 * Kiểm tra một mã người dùng gõ vào.
 *
 * @param string $khoaBase32 khoá bí mật dạng Base32
 * @param string $maGoVao    mã 6 số người dùng gõ
 * @param int    $buocDaDung bước cao nhất đã dùng rồi (chống dùng lại)
 * @param int|null $luc      mốc thời gian, null = bây giờ (test tiêm vào)
 * @return int|null bước khớp, hoặc null nếu không khớp
 */
function hbKiemMa(string $khoaBase32, string $maGoVao, int $buocDaDung = 0, ?int $luc = null): ?int
{
    $maGoVao = preg_replace('/\D+/', '', $maGoVao) ?? '';
    if (strlen($maGoVao) !== HB_SO_CHU_SO) {
        return null;
    }
    $khoaTho = hbGiaiBase32($khoaBase32);
    if ($khoaTho === null || $khoaTho === '') {
        return null;
    }
    $buocGio = hbBuoc($luc);
    for ($d = -HB_LECH_BUOC; $d <= HB_LECH_BUOC; $d++) {
        $buoc = $buocGio + $d;
        // CHỐNG DÙNG LẠI. Không có dòng này thì ai đọc lén được mã (nhìn qua vai,
        // hoặc bắt được gói tin) vẫn dùng lại được trong 90 giây tiếp theo.
        if ($buoc <= $buocDaDung) {
            continue;
        }
        // hash_equals: so sánh thời gian hằng, không rò rỉ số ký tự khớp đầu.
        if (hash_equals(hbMaCuaBuoc($khoaTho, $buoc), $maGoVao)) {
            return $buoc;
        }
    }
    return null;
}

/** Sinh khoá bí mật mới: 20 byte ngẫu nhiên (160 bit, đúng cỡ SHA1). */
function hbSinhKhoa(): string
{
    return hbMaHoaBase32(random_bytes(20));
}

/** Chia khoá thành nhóm 4 ký tự cho người gõ tay đỡ sai. */
function hbKhoaDeDoc(string $khoaBase32): string
{
    return trim(chunk_split($khoaBase32, 4, ' '));
}

/**
 * URI otpauth:// để ứng dụng authenticator quét hoặc nhận qua dán.
 *
 * `issuer` vào cả nhãn và tham số là cố ý — cách cũ (chỉ nhãn) không phải app
 * nào cũng đọc đúng, và trong app sẽ hiện thành "HTBB (tên)" cho dễ tìm.
 */
function hbUri(string $khoaBase32, string $tenTaiKhoan, string $tenHeThong = 'HTBB Admin'): string
{
    $nhan = rawurlencode($tenHeThong) . ':' . rawurlencode($tenTaiKhoan);
    return 'otpauth://totp/' . $nhan
        . '?secret=' . $khoaBase32
        . '&issuer=' . rawurlencode($tenHeThong)
        . '&algorithm=SHA1&digits=' . HB_SO_CHU_SO . '&period=' . HB_BUOC_GIAY;
}

// ===========================================================================
//  MÃ DỰ PHÒNG
// ===========================================================================

/** Sinh danh sách mã dự phòng dạng "abcd-efgh" (dễ đọc, không có ký tự dễ lẫn). */
function hbSinhMaDuPhong(int $soLuong = HB_SO_MA_DU_PHONG): array
{
    // Bỏ 0/o/1/l/i để không ai gõ nhầm khi đang cuống vì mất điện thoại.
    $chu = 'abcdefghjkmnpqrstuvwxyz23456789';
    $ra = [];
    for ($i = 0; $i < $soLuong; $i++) {
        $ma = '';
        for ($j = 0; $j < 8; $j++) {
            if ($j === 4) {
                $ma .= '-';
            }
            $ma .= $chu[random_int(0, strlen($chu) - 1)];
        }
        $ra[] = $ma;
    }
    return $ra;
}

/** Băm danh sách mã dự phòng để lưu. Không bao giờ lưu dạng đọc được. */
function hbBamMaDuPhong(array $ma): string
{
    $ra = [];
    foreach ($ma as $m) {
        $ra[] = password_hash($m, PASSWORD_DEFAULT);
    }
    return implode("\n", $ra);
}

/**
 * Thử một mã dự phòng. Trả về chuỗi băm CÒN LẠI nếu khớp, null nếu không.
 * Mã đã dùng bị gỡ khỏi danh sách — mỗi mã đúng một lần.
 */
function hbDungMaDuPhong(string $chuoiBam, string $maGoVao): ?string
{
    $maGoVao = strtolower(trim($maGoVao));
    if ($maGoVao === '' || $chuoiBam === '') {
        return null;
    }
    $conLai = [];
    $khop = false;
    foreach (explode("\n", $chuoiBam) as $bam) {
        $bam = trim($bam);
        if ($bam === '') {
            continue;
        }
        // Chỉ gỡ MỘT mã dù có trùng: khớp rồi thì phần còn lại giữ nguyên hết.
        if (!$khop && password_verify($maGoVao, $bam)) {
            $khop = true;
            continue;
        }
        $conLai[] = $bam;
    }
    return $khop ? implode("\n", $conLai) : null;
}

// ===========================================================================
//  LƯU TRỮ (bảng kpah_db.web_admin_2fa)
// ===========================================================================

const HB_BANG = 'web_admin_2fa';

/** Bảng đã tồn tại chưa. Nhớ trong một lần chạy. */
function hbCoBang(): bool
{
    static $co = null;
    if ($co === null) {
        try {
            dbGame()->query('SELECT 1 FROM ' . HB_BANG . ' LIMIT 1');
            $co = true;
        } catch (Throwable $ex) {
            $co = false;
        }
    }
    return $co;
}

/** Dòng 2FA của một tài khoản, hoặc null. */
function hbDong(string $tenTaiKhoan): ?array
{
    if (!hbCoBang()) {
        return null;
    }
    try {
        $st = dbGame()->prepare('SELECT admin_user, khoa_bimat, kich_hoat, buoc_da_dung,
                                        ma_du_phong, tao_luc, dung_luc
                                 FROM ' . HB_BANG . ' WHERE admin_user = ? LIMIT 1');
        $st->execute([strtolower($tenTaiKhoan)]);
        $d = $st->fetch();
        return $d ?: null;
    } catch (Throwable $ex) {
        return null;
    }
}

/** Tài khoản này có bắt buộc phải gõ mã hai bước không. */
function hbDaGhiDanh(string $tenTaiKhoan): bool
{
    $d = hbDong($tenTaiKhoan);
    return $d !== null && (int)$d['kich_hoat'] === 1;
}

/** Có bắt buộc MỌI tài khoản admin phải có 2FA không (config.local.php). */
function hbBatBuocTatCa(): bool
{
    return defined('ADMIN_2FA_BAT_BUOC') && (bool)constant('ADMIN_2FA_BAT_BUOC');
}

/** Ghi lại bước vừa dùng để không ai dùng lại được mã đó. */
function hbGhiBuocDaDung(string $tenTaiKhoan, int $buoc): void
{
    try {
        // `buoc_da_dung = GREATEST(...)`: hai phiên đăng nhập gần nhau có thể ghi
        // lệch thứ tự, và lùi con số này lại là mở lại đúng cái cửa vừa đóng.
        $st = dbGame()->prepare('UPDATE ' . HB_BANG . '
                                    SET buoc_da_dung = GREATEST(buoc_da_dung, ?),
                                        dung_luc = NOW()
                                  WHERE admin_user = ?');
        $st->execute([$buoc, strtolower($tenTaiKhoan)]);
    } catch (Throwable $ex) {
    }
}

/** Ghi lại danh sách mã dự phòng còn lại sau khi dùng một mã. */
function hbGhiMaDuPhong(string $tenTaiKhoan, string $chuoiBamConLai): void
{
    try {
        $st = dbGame()->prepare('UPDATE ' . HB_BANG . '
                                    SET ma_du_phong = ?, dung_luc = NOW()
                                  WHERE admin_user = ?');
        $st->execute([$chuoiBamConLai, strtolower($tenTaiKhoan)]);
    } catch (Throwable $ex) {
    }
}

/** Số mã dự phòng còn lại. */
function hbSoMaDuPhongConLai(string $tenTaiKhoan): int
{
    $d = hbDong($tenTaiKhoan);
    if ($d === null || (string)$d['ma_du_phong'] === '') {
        return 0;
    }
    $n = 0;
    foreach (explode("\n", (string)$d['ma_du_phong']) as $b) {
        if (trim($b) !== '') {
            $n++;
        }
    }
    return $n;
}
