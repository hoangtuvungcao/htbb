<?php
require_once __DIR__ . '/inc/functions.php';

$page = 'login';
$errors = [];
$old = ['username' => ''];

/** Số lần gõ SAI mật khẩu tối đa từ một địa chỉ IP, trong 15 phút. */
const DANG_NHAP_SAI_TOI_DA = 15;
const DANG_NHAP_TRONG_GIAY = 900;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!csrfCheck($_POST['csrf'] ?? null)) {
        $errors[] = 'Phiên làm việc hết hạn, vui lòng thử lại.';
    } elseif (biChanSpam('dang_nhap_sai', DANG_NHAP_SAI_TOI_DA, DANG_NHAP_TRONG_GIAY)) {
        // Chỉ đếm lần SAI, nên người đăng nhập đúng không bao giờ chạm hạn mức
        // dù có vào ra bao nhiêu lần.
        $errors[] = 'Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 15 phút.';
    } else {
        $username = strtolower(trim($_POST['username'] ?? ''));
        $password = (string)($_POST['password'] ?? '');
        $old['username'] = $username;

        if ($username === '' || $password === '') {
            $errors[] = 'Vui lòng nhập tên tài khoản và mật khẩu.';
        } else {
            try {
                $acc = dbAccount();
                // So khớp mật khẩu bằng PASSWORD() giống login server của game
                $st = $acc->prepare('SELECT id, username, ban FROM team_user WHERE username = ? AND `password` = PASSWORD(?) LIMIT 1');
                $st->execute([$username, $password]);
                $row = $st->fetch();
                if (!$row) {
                    ghiLanLam('dang_nhap_sai');
                    $errors[] = 'Sai tên tài khoản hoặc mật khẩu.';
                } elseif ((int)$row['ban'] === 1) {
                    $errors[] = 'Tài khoản của bạn đã bị khóa.';
                } else {
                    session_regenerate_id(true);
                    $_SESSION['user'] = [
                        'id'       => (int)$row['id'],
                        'username' => $row['username'],
                    ];
                    setFlash('success', 'Đăng nhập thành công. Chào mừng ' . $row['username'] . '!');
                    header('Location: account.php');
                    exit;
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
        <h1 class="auth-title">Đăng nhập</h1>
        <p class="auth-sub">Đăng nhập để quản lý tài khoản và kích hoạt.</p>

        <?php foreach ($errors as $err): ?>
            <div class="flash flash-error"><?= e($err) ?></div>
        <?php endforeach; ?>

        <form method="post" action="login.php" autocomplete="off" novalidate>
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <label>Tên tài khoản
                <input type="text" name="username" value="<?= e($old['username']) ?>" required>
            </label>
            <label>Mật khẩu
                <input type="password" name="password" required>
            </label>
            <button type="submit" class="btn btn-primary btn-block">Đăng nhập</button>
        </form>
        <p class="auth-alt">Chưa có tài khoản? <a href="register.php">Đăng ký ngay</a></p>
    </div>
</section>
<?php require __DIR__ . '/inc/footer.php'; ?>
