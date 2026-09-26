<?php
require_once __DIR__ . '/inc/functions.php';

$page = 'register';
$errors = [];
$old = ['username' => '', 'email' => ''];
$daDangKy = null; // tên tài khoản vừa tạo thành công

/**
 * Số tài khoản một địa chỉ IP được tạo trong một giờ. Người thật hiếm khi cần
 * quá vài cái (bạn bè chung nhà, chung quán net), còn script thì tạo hàng nghìn.
 */
const DANG_KY_TOI_DA   = 5;
const DANG_KY_TRONG_GIAY = 3600;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        $errors[] = 'Phiên làm việc hết hạn, vui lòng thử lại.';
    } elseif (biChanSpam('dang_ky', DANG_KY_TOI_DA, DANG_KY_TRONG_GIAY)) {
        // Token CSRF không chặn được việc này: script chỉ cần tải trang lấy token
        // rồi gửi form, lặp bao nhiêu lần tuỳ thích. Mỗi lần là một dòng mới
        // trong account.team_user.
        $errors[] = 'Bạn đã tạo quá nhiều tài khoản từ địa chỉ này. Vui lòng thử lại sau một giờ.';
    } else {
        $username = strtolower(trim($_POST['username'] ?? ''));
        $password = (string)($_POST['password'] ?? '');
        $confirm  = (string)($_POST['confirm'] ?? '');
        $email    = trim($_POST['email'] ?? '');
        $old['username'] = $username;
        $old['email'] = $email;

        if (!validUsername($username)) {
            $errors[] = 'Tên tài khoản phải từ ' . USERNAME_MIN . '-' . USERNAME_MAX . ' ký tự, chỉ gồm chữ thường và số.';
        }
        if (!validPassword($password)) {
            $errors[] = 'Mật khẩu phải từ ' . PASSWORD_MIN . '-' . PASSWORD_MAX . ' ký tự.';
        }
        if ($password !== $confirm) {
            $errors[] = 'Mật khẩu nhập lại không khớp.';
        }
        if ($email !== '' && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $errors[] = 'Email không hợp lệ.';
        }

        if (!$errors) {
            try {
                $acc = dbAccount();
                // Kiểm tra trùng tên
                $chk = $acc->prepare('SELECT 1 FROM team_user WHERE username = ? LIMIT 1');
                $chk->execute([$username]);
                if ($chk->fetchColumn()) {
                    $errors[] = 'Tên tài khoản đã tồn tại, vui lòng chọn tên khác.';
                } else {
                    // Lưu mật khẩu bằng PASSWORD() để KHỚP với login server của game
                    $ins = $acc->prepare(
                        'INSERT INTO team_user (`username`, `password`, `phone`, `regdate`, `ban`, `provider`, `fromgame`, `email`)
                         VALUES (?, PASSWORD(?), \'\', NOW(), 0, 0, 0, ?)'
                    );
                    $ins->execute([$username, $password, ($email !== '' ? $email : null)]);
                    $userId = (int)$acc->lastInsertId();
                    // Chỉ đếm khi tạo THÀNH CÔNG: gõ trùng tên vài lần là chuyện
                    // bình thường của người dùng thật, không nên tính vào hạn mức.
                    ghiLanLam('dang_ky');

                    if (AUTO_ACTIVATE) {
                        activateAccount($userId, $username);
                    }

                    // Hiện kết quả ngay trên trang này thay vì setFlash + chuyển
                    // trang: flash nằm trong session, mà qua domain Nova WAF không
                    // chuyển tiếp cookie nên flash mất ở request sau.
                    $daDangKy = $username;
                }
            } catch (Throwable $ex) {
                $errors[] = 'Lỗi kết nối cơ sở dữ liệu. Kiểm tra lại config.php và MySQL đang chạy.';
            }
        }
    }
}

require __DIR__ . '/inc/header.php';
?>
<section class="auth-wrap">
    <div class="auth-card">
        <h1 class="auth-title">Đăng ký tài khoản</h1>
        <p class="auth-sub">Tạo tài khoản để bắt đầu hành trình Khí Phách Anh Hùng.</p>

        <?php if ($daDangKy !== null): ?>
            <div class="flash flash-success">
                Đăng ký thành công tài khoản "<?= e($daDangKy) ?>".
                <?php if (AUTO_ACTIVATE): ?>
                    Tài khoản đã được kích hoạt, bạn có thể vào game ngay.
                <?php else: ?>
                    Tài khoản cần được quản trị viên kích hoạt trước khi vào game — vui lòng liên hệ quản trị viên.
                <?php endif; ?>
            </div>
        <?php endif; ?>

        <?php foreach ($errors as $err): ?>
            <div class="flash flash-error"><?= e($err) ?></div>
        <?php endforeach; ?>

        <form method="post" action="register.php" autocomplete="off" novalidate>
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <label>Tên tài khoản
                <input type="text" name="username" value="<?= e($old['username']) ?>"
                       placeholder="4-20 ký tự, chữ thường + số" required>
            </label>
            <label>Mật khẩu
                <input type="password" name="password" placeholder="<?= PASSWORD_MIN ?>-<?= PASSWORD_MAX ?> ký tự" required>
            </label>
            <label>Nhập lại mật khẩu
                <input type="password" name="confirm" placeholder="Nhập lại mật khẩu" required>
            </label>
            <label>Email (không bắt buộc)
                <input type="email" name="email" value="<?= e($old['email']) ?>" placeholder="email@example.com">
            </label>
            <button type="submit" class="btn btn-primary btn-block">Đăng ký ngay</button>
        </form>
        <p class="auth-alt">Đã có tài khoản? <a href="login.php">Đăng nhập</a></p>
    </div>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
