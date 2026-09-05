<?php
/**
 * Tạo NHIỀU tài khoản đăng nhập (account.team_user) cùng một lượt.
 *
 * Mỗi dòng trong ô nhập là một tài khoản: `tenTaiKhoan` hoặc `tenTaiKhoan:matKhau`
 * (bỏ trống mật khẩu thì tự sinh ngẫu nhiên). Xử lý TỪNG DÒNG ĐỘC LẬP — một dòng
 * sai định dạng hay trùng tên chỉ bị bỏ qua, không làm hỏng cả lượt, giống hệt cách
 * `giftcode.php` xử lý "xoá nhiều mã" (ghi log từng dòng, không gộp một transaction).
 *
 * DÙNG LẠI ĐÚNG CÂU INSERT của `register.php` (cột, PASSWORD(), NOW()...) để không
 * lệch với đường đăng ký công khai — hai nơi tạo tài khoản phải luôn khớp cấu trúc.
 *
 * KHÔNG DÙNG `biChanSpam()`/`ghiLanLam()` như register.php: đó là chặn spam cho form
 * PUBLIC (chưa đăng nhập), còn đây admin đã qua `requireAdmin()`. Thay vào đó giới
 * hạn cứng số dòng một lượt (TAO_HANG_LOAT_TOI_DA) để tránh dán nhầm một tệp khổng
 * lồ làm treo request.
 *
 * Mật khẩu chỉ hiện ĐÚNG MỘT LẦN trên trang kết quả — cột `team_user.password` chỉ
 * lưu qua hàm PASSWORD() một chiều, không đọc lại nguyên văn được nữa sau đó (giống
 * hệt lưu ý ở nút "Đổi mật khẩu" của taikhoan.php).
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$TRANG = 'tao_hang_loat.php';

/** Số dòng tối đa một lượt nhập — chặn dán nhầm danh sách khổng lồ làm treo trang. */
const TAO_HANG_LOAT_TOI_DA = 200;

/** Sinh mật khẩu ngẫu nhiên khi người dùng để trống. Bỏ ký tự dễ nhầm (0/O, 1/I/l). */
function taoMatKhauNgauNhien(int $doDai = 10): string
{
    $chars = 'abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789';
    $mk = '';
    for ($i = 0; $i < $doDai; $i++) {
        $mk .= $chars[random_int(0, strlen($chars) - 1)];
    }
    return $mk;
}

$ketQua = null; // null = chưa gửi form; mảng kết quả từng dòng sau khi xử lý

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $raw = (string)($_POST['danh_sach'] ?? '');
    $tuKichHoat = isset($_POST['tu_kich_hoat']);

    $dong = preg_split('/\r\n|\r|\n/', trim($raw));
    $dong = array_values(array_filter(array_map('trim', $dong), static fn($d) => $d !== '' && $d[0] !== '#'));

    if (!$dong) {
        veLai($TRANG, 'error', 'Chưa nhập tài khoản nào (mỗi dòng một tài khoản, dạng "tenTaiKhoan" hoặc "tenTaiKhoan:matKhau").');
    }
    if (count($dong) > TAO_HANG_LOAT_TOI_DA) {
        veLai($TRANG, 'error', 'Chỉ được tạo tối đa ' . TAO_HANG_LOAT_TOI_DA . ' tài khoản một lượt (đang nhập ' . count($dong) . ' dòng). Chia nhỏ ra rồi nhập nhiều lượt.');
    }

    $ketQua = [];
    $daGap  = []; // ten -> so dong dau tien gap: chan trung NGAY TRONG danh sach vua nhap
    $soTao  = 0;
    $acc    = dbAccount();

    foreach ($dong as $i => $line) {
        $soDong = $i + 1;
        $phan   = explode(':', $line, 2);
        $username = strtolower(trim($phan[0]));
        $matKhauNhap = isset($phan[1]) ? trim($phan[1]) : '';

        $hang = ['dong' => $soDong, 'username' => $username, 'matkhau' => '', 'trangthai' => '', 'loi' => true];

        if (!validUsername($username)) {
            $hang['trangthai'] = 'Bỏ qua — tên phải ' . USERNAME_MIN . '-' . USERNAME_MAX . ' ký tự, chỉ chữ thường và số';
            $ketQua[] = $hang;
            continue;
        }
        if (isset($daGap[$username])) {
            $hang['trangthai'] = 'Bỏ qua — trùng tên với dòng ' . $daGap[$username] . ' ở trên';
            $ketQua[] = $hang;
            continue;
        }
        $daGap[$username] = $soDong;

        $matKhau = $matKhauNhap !== '' ? $matKhauNhap : taoMatKhauNgauNhien();
        if (!validPassword($matKhau)) {
            $hang['trangthai'] = 'Bỏ qua — mật khẩu phải ' . PASSWORD_MIN . '-' . PASSWORD_MAX . ' ký tự';
            $ketQua[] = $hang;
            continue;
        }

        try {
            $chk = $acc->prepare('SELECT 1 FROM team_user WHERE username = ? LIMIT 1');
            $chk->execute([$username]);
            if ($chk->fetchColumn()) {
                $hang['trangthai'] = 'Bỏ qua — tên đã tồn tại trong hệ thống';
                $ketQua[] = $hang;
                continue;
            }

            // Cau INSERT giong het register.php: PASSWORD() de khop login server,
            // phone rong, ban=0, provider=0, fromgame=0, email luon NULL o day (bo
            // sung email hang loat khong nam trong pham vi tinh nang nay).
            $ins = $acc->prepare(
                'INSERT INTO team_user (`username`, `password`, `phone`, `regdate`, `ban`, `provider`, `fromgame`, `email`)
                 VALUES (?, PASSWORD(?), \'\', NOW(), 0, 0, 0, NULL)'
            );
            $ins->execute([$username, $matKhau]);
            $userId = (int)$acc->lastInsertId();

            $daKichHoat = false;
            if ($tuKichHoat) {
                $daKichHoat = activateAccount($userId, $username);
            }

            // KHONG ghi mat khau vao nhat ky - dung nguyen tac "khong ghi lai noi
            // dung" nhu doi_mat_khau o taikhoan.php.
            ghiNhatKy('tao_tai_khoan_hang_loat', $username, 'id ' . $userId . ($daKichHoat ? ', đã kích hoạt' : ', chưa kích hoạt'));

            $soTao++;
            $hang['matkhau']   = $matKhau;
            $hang['trangthai'] = 'Đã tạo (#' . $userId . ')' . ($daKichHoat ? ', đã kích hoạt' : '');
            $hang['loi']       = false;
        } catch (Throwable $ex) {
            $hang['trangthai'] = 'Lỗi: ' . $ex->getMessage();
        }
        $ketQua[] = $hang;
    }
}

adminHead('tao_hang_loat', 'Tạo tài khoản hàng loạt');
?>
<div class="page-head">
    <div>
        <h1>Tạo tài khoản hàng loạt</h1>
        <p>Mỗi dòng một tài khoản, tối đa <?= TAO_HANG_LOAT_TOI_DA ?> dòng một lượt.</p>
    </div>
</div>

<div class="card">
    <h3>Danh sách cần tạo</h3>
    <form method="post" action="tao_hang_loat.php">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <label class="o"><span>Mỗi dòng: <code>tenTaiKhoan</code> hoặc <code>tenTaiKhoan:matKhau</code> — để trống mật khẩu thì tự sinh ngẫu nhiên. Dòng bắt đầu bằng <code>#</code> bị bỏ qua.</span>
            <textarea name="danh_sach" rows="10" placeholder="<?= e("banA\nbanB:matkhaurieng\n# dòng này là ghi chú, bỏ qua") ?>" style="font-family:monospace;"><?= e((string)($_POST['danh_sach'] ?? '')) ?></textarea>
        </label>
        <label class="o" style="display:flex;flex-direction:row;align-items:center;gap:8px;margin-top:10px;">
            <input type="checkbox" name="tu_kich_hoat" value="1" style="width:auto;"
                   <?= isset($_POST['tu_kich_hoat']) || $_SERVER['REQUEST_METHOD'] !== 'POST' ? 'checked' : '' ?>>
            <span style="margin:0;">Kích hoạt ngay (thêm vào <code>5h_active</code>) — bỏ chọn nếu tài khoản phải tự kích hoạt trên trang chủ.</span>
        </label>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Tạo tài khoản</button>
        </div>
    </form>
</div>

<?php if ($ketQua !== null):
    $tongDong = count($ketQua);
    $ketQuaTao = array_values(array_filter($ketQua, static fn($h) => !$h['loi']));
    $soTao = count($ketQuaTao);
?>
    <div class="card">
        <h3>Kết quả: đã tạo <?= $soTao ?>/<?= $tongDong ?> tài khoản</h3>

        <?php if ($ketQuaTao): ?>
            <div class="luuy" style="margin-bottom:14px;">
                <p><strong>Sao chép ngay danh sách dưới đây</strong> — mật khẩu chỉ hiện được lúc này.
                   Cột <code>team_user.password</code> chỉ lưu bản băm một chiều qua <code>PASSWORD()</code>,
                   tải lại trang là mất, không xem lại được nữa (chỉ có thể đặt mật khẩu MỚI ở trang Tài khoản).</p>
            </div>
            <label class="o"><span>Tài khoản vừa tạo (tenTaiKhoan:matKhau)</span>
                <textarea readonly rows="<?= min(15, max(3, $soTao)) ?>" style="font-family:monospace;" onclick="this.select();"><?php
                    foreach ($ketQuaTao as $h) {
                        echo e($h['username'] . ':' . $h['matkhau']) . "\n";
                    }
                ?></textarea>
            </label>
        <?php endif; ?>

        <div class="tbl-wrap" style="margin-top:14px;">
        <table class="tbl">
            <thead><tr>
                <th class="num">Dòng</th><th>Tài khoản</th><th>Mật khẩu</th><th>Trạng thái</th>
            </tr></thead>
            <tbody>
            <?php foreach ($ketQua as $h): ?>
                <tr>
                    <td class="num"><?= (int)$h['dong'] ?></td>
                    <td><strong><?= e($h['username']) ?></strong></td>
                    <td class="nowrap"><?= $h['matkhau'] !== '' ? '<code>' . e($h['matkhau']) . '</code>' : '<span class="muted">—</span>' ?></td>
                    <td><?= $h['loi'] ? '<span class="nhan nhan-khoa">' . e($h['trangthai']) . '</span>' : '<span class="nhan nhan-ok">' . e($h['trangthai']) . '</span>' ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    </div>
<?php endif; ?>
<?php adminFoot(); ?>
