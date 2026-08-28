<?php
/**
 * Quản lý tài khoản đăng nhập (bảng account.team_user).
 *
 * KHOÁ TÀI KHOẢN nghĩa là đặt team_user.ban = 1. Đó chính là cột mà login server
 * đọc: `select * from team_user where username=... and password=password(...)`
 * rồi `ban == 1 ? -2 : id`, và mã -2 làm client hiện "Tài khoản của bạn hiện
 * đang bị khoá". Không có đường nào khác chặn được một tài khoản ở tầng đăng nhập.
 *
 * Lưu ý quan trọng: khoá chỉ chặn LẦN ĐĂNG NHẬP SAU. Người đang online vẫn chơi
 * tiếp cho tới khi họ tự thoát — trang web không nói chuyện được với tiến trình
 * game đang chạy. Muốn đá ra ngay thì dùng bảng điều khiển trong game.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$TRANG = 'taikhoan.php';

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $viec = p('viec');
    $id   = (int)p('id');
    $ve   = $TRANG . '?id=' . $id;

    $st = dbAccount()->prepare('SELECT id, username, ban FROM team_user WHERE id = ? LIMIT 1');
    $st->execute([$id]);
    $tk = $st->fetch();

    if (!$tk) {
        veLai($TRANG, 'error', 'Không tìm thấy tài khoản #' . $id . '.');
    }

    $ten      = (string)$tk['username'];
    $laAdmin  = laTaiKhoanAdmin($ten);
    $laChinhMinh = strtolower($ten) === strtolower(adminName());

    switch ($viec) {

        case 'khoa':
        case 'mo_khoa':
            // Không cho khoá tài khoản admin — kể cả của chính mình. Đây là cái
            // chốt duy nhất ngăn việc tự nhốt mình ra ngoài, vì mở lại thì phải
            // sửa thẳng trong database.
            if ($laAdmin) {
                veLai($ve, 'error', 'Không khoá được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS trước nếu thật sự cần.');
            }
            $banMoi = $viec === 'khoa' ? 1 : 0;
            $up = dbAccount()->prepare('UPDATE team_user SET ban = ? WHERE id = ?');
            $up->execute([$banMoi, $id]);
            ghiNhatKy($viec === 'khoa' ? 'khoa_tai_khoan' : 'mo_tai_khoan', $ten,
                'ban: ' . (int)$tk['ban'] . ' -> ' . $banMoi);
            veLai($ve, 'success', $viec === 'khoa'
                ? 'Đã khoá tài khoản ' . $ten . '. Người đang online vẫn chơi tiếp tới khi thoát ra.'
                : 'Đã mở khoá tài khoản ' . $ten . '.');
            break;

        case 'kich_hoat':
            try {
                $ck = dbGame()->prepare('SELECT 1 FROM `5h_active` WHERE username = ? LIMIT 1');
                $ck->execute([$ten]);
                if ($ck->fetchColumn()) {
                    veLai($ve, 'error', 'Tài khoản ' . $ten . ' đã kích hoạt rồi.');
                }
                $in = dbGame()->prepare('INSERT INTO `5h_active` (userID, username) VALUES (?, ?)');
                $in->execute([$id, $ten]);
                ghiNhatKy('kich_hoat', $ten, 'thêm vào 5h_active');
                veLai($ve, 'success', 'Đã kích hoạt tài khoản ' . $ten . '.');
            } catch (Throwable $ex) {
                veLai($ve, 'error', 'Không kích hoạt được: ' . $ex->getMessage());
            }
            break;

        case 'huy_kich_hoat':
            try {
                // Xoá theo cả hai cột: một số dòng cũ chỉ có userID đúng, một số
                // chỉ có username đúng (bảng này không có ràng buộc nào cả).
                $del = dbGame()->prepare('DELETE FROM `5h_active` WHERE username = ? OR userID = ?');
                $del->execute([$ten, $id]);
                ghiNhatKy('huy_kich_hoat', $ten, 'xoá ' . $del->rowCount() . ' dòng khỏi 5h_active');
                veLai($ve, 'success', 'Đã huỷ kích hoạt ' . $ten . ' (' . $del->rowCount() . ' dòng).');
            } catch (Throwable $ex) {
                veLai($ve, 'error', 'Không huỷ được: ' . $ex->getMessage());
            }
            break;

        case 'doi_mat_khau':
            if ($laAdmin && !$laChinhMinh) {
                veLai($ve, 'error', 'Không đổi được mật khẩu của một tài khoản quản trị khác.');
            }
            $mk  = (string)($_POST['matkhau'] ?? '');
            $mk2 = (string)($_POST['matkhau2'] ?? '');
            if ($mk !== $mk2) {
                veLai($ve, 'error', 'Hai lần nhập mật khẩu không giống nhau.');
            }
            if (!validPassword($mk)) {
                veLai($ve, 'error', 'Mật khẩu phải dài ' . PASSWORD_MIN . '–' . PASSWORD_MAX . ' ký tự.');
            }
            try {
                // PASSWORD() để khớp đúng cách login server băm mật khẩu.
                $up = dbAccount()->prepare('UPDATE team_user SET `password` = PASSWORD(?) WHERE id = ?');
                $up->execute([$mk, $id]);
                ghiNhatKy('doi_mat_khau', $ten, 'đặt mật khẩu mới (không ghi lại nội dung)');
                veLai($ve, 'success', 'Đã đổi mật khẩu cho ' . $ten . '.');
            } catch (Throwable $ex) {
                veLai($ve, 'error', 'Không đổi được mật khẩu. Nếu máy chủ chạy MySQL 8 thì hàm PASSWORD() đã bị gỡ '
                    . 'và cả login server lẫn trang này đều phải đổi cách băm. Lỗi: ' . $ex->getMessage());
            }
            break;

        default:
            veLai($TRANG, 'error', 'Không hiểu thao tác.');
    }
    exit;
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu để hiển thị
// ---------------------------------------------------------------------------
$q      = g('q');
$xemId  = (int)g('id');
$ketQua = [];
$chiTiet = null;
$nhanVat = [];
$daKichHoat = false;
$loiDoc = '';

try {
    if ($xemId > 0) {
        $st = dbAccount()->prepare('SELECT * FROM team_user WHERE id = ? LIMIT 1');
        $st->execute([$xemId]);
        $chiTiet = $st->fetch() ?: null;

        if ($chiTiet) {
            $st = dbGame()->prepare(
                'SELECT id, charname, lastLv, `class`, xp, gold, luong, luonglock, ban, del, lastLog, idserver
                   FROM tob_char WHERE userId = ? ORDER BY lastLv DESC, id ASC'
            );
            $st->execute([$xemId]);
            $nhanVat = $st->fetchAll();

            $ck = dbGame()->prepare('SELECT 1 FROM `5h_active` WHERE username = ? OR userID = ? LIMIT 1');
            $ck->execute([$chiTiet['username'], $xemId]);
            $daKichHoat = (bool)$ck->fetchColumn();
        }
    } elseif ($q !== '') {
        if (ctype_digit($q)) {
            $st = dbAccount()->prepare('SELECT id, username, email, phone, regdate, ban FROM team_user
                                         WHERE id = ? OR username = ? LIMIT 50');
            $st->execute([(int)$q, $q]);
        } else {
            $st = dbAccount()->prepare('SELECT id, username, email, phone, regdate, ban FROM team_user
                                         WHERE username LIKE ? OR email LIKE ? OR phone LIKE ?
                                         ORDER BY id DESC LIMIT 50');
            $nhu = '%' . $q . '%';
            $st->execute([$nhu, $nhu, $nhu]);
        }
        $ketQua = $st->fetchAll();
    } else {
        $ketQua = dbAccount()->query('SELECT id, username, email, phone, regdate, ban FROM team_user
                                       ORDER BY id DESC LIMIT 30')->fetchAll();
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

function nhanTrangThaiBan(int $ban): string
{
    if ($ban === 1) return '<span class="nhan nhan-khoa">Bị khoá</span>';
    if ($ban === 3) return '<span class="nhan nhan-cho">Chờ email</span>';
    return '<span class="nhan nhan-ok">Bình thường</span>';
}

adminHead('taikhoan', 'Tài khoản');
?>
<div class="page-head">
    <div>
        <h1>Tài khoản</h1>
        <p>Khoá / mở khoá đăng nhập, kích hoạt, đổi mật khẩu.</p>
    </div>
    <form method="get" action="taikhoan.php" class="ngang">
        <label class="o" style="min-width:240px;">
            <span>Tìm theo tên, email, số điện thoại hoặc ID</span>
            <input type="text" name="q" value="<?= e($q) ?>" placeholder="ví dụ: hoahv">
        </label>
        <button type="submit" class="btn btn-primary btn-nho">Tìm</button>
    </form>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<?php if ($chiTiet):
    $ten     = (string)$chiTiet['username'];
    $laAdmin = laTaiKhoanAdmin($ten);
    $laMinh  = strtolower($ten) === strtolower(adminName());
    $ban     = (int)$chiTiet['ban'];
?>
    <p><a href="taikhoan.php">← Về danh sách</a></p>

    <div class="grid-side">
        <div>
            <div class="card">
                <h3><?= e($ten) ?> <?= nhanTrangThaiBan($ban) ?></h3>
                <dl class="dl">
                    <dt>ID</dt><dd><?= (int)$chiTiet['id'] ?></dd>
                    <dt>Email</dt><dd><?= e((string)($chiTiet['email'] ?? '')) ?: '<span class="muted">—</span>' ?></dd>
                    <dt>Điện thoại</dt><dd><?= e((string)($chiTiet['phone'] ?? '')) ?: '<span class="muted">—</span>' ?></dd>
                    <dt>Đăng ký</dt><dd><?= e((string)$chiTiet['regdate']) ?></dd>
                    <dt>Kích hoạt</dt><dd><?= $daKichHoat
                        ? '<span class="nhan nhan-ok">Đã kích hoạt</span>'
                        : '<span class="nhan nhan-cho">Chưa kích hoạt</span>' ?></dd>
                    <dt>Nhân vật</dt><dd><?= count($nhanVat) ?></dd>
                </dl>

                <?php if ($laAdmin): ?>
                    <div class="luuy" style="margin-top:14px;">
                        <p><strong>Đây là tài khoản quản trị</strong> (có tên trong <code>ADMIN_ACCOUNTS</code>)
                           nên không khoá được từ trang này.</p>
                    </div>
                <?php endif; ?>
            </div>

            <div class="card">
                <h3>Khoá đăng nhập</h3>
                <?php if ($ban === 1): ?>
                    <p class="muted small">Tài khoản đang bị khoá. Khi đăng nhập, người chơi thấy
                       “Tài khoản của bạn hiện đang bị khoá”.</p>
                    <form method="post" action="taikhoan.php" onsubmit="return confirm('Mở khoá tài khoản <?= e($ten) ?>?');">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="id" value="<?= (int)$chiTiet['id'] ?>">
                        <input type="hidden" name="viec" value="mo_khoa">
                        <button class="btn btn-primary btn-nho" <?= $laAdmin ? 'disabled' : '' ?>>Mở khoá</button>
                    </form>
                <?php else: ?>
                    <p class="muted small">Khoá sẽ chặn <em>lần đăng nhập sau</em>. Người đang online
                       vẫn chơi tiếp cho tới khi thoát ra.</p>
                    <form method="post" action="taikhoan.php" onsubmit="return confirm('Khoá tài khoản <?= e($ten) ?>? Người này sẽ không đăng nhập được nữa.');">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="id" value="<?= (int)$chiTiet['id'] ?>">
                        <input type="hidden" name="viec" value="khoa">
                        <button class="btn btn-do btn-nho" <?= $laAdmin ? 'disabled' : '' ?>>Khoá tài khoản</button>
                    </form>
                <?php endif; ?>
            </div>

            <div class="card">
                <h3>Kích hoạt</h3>
                <p class="muted small">Bảng <code>5h_active</code> — server chỉ chặn khi
                   <code>sv.EnableAvtiveCheck</code> đang bật.</p>
                <form method="post" action="taikhoan.php" class="inline">
                    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                    <input type="hidden" name="id" value="<?= (int)$chiTiet['id'] ?>">
                    <input type="hidden" name="viec" value="<?= $daKichHoat ? 'huy_kich_hoat' : 'kich_hoat' ?>">
                    <button class="btn <?= $daKichHoat ? 'btn-xam' : 'btn-primary' ?> btn-nho">
                        <?= $daKichHoat ? 'Huỷ kích hoạt' : 'Kích hoạt ngay' ?>
                    </button>
                </form>
            </div>

            <div class="card">
                <h3>Đổi mật khẩu</h3>
                <?php if ($laAdmin && !$laMinh): ?>
                    <p class="muted small">Không đổi được mật khẩu của một tài khoản quản trị khác.</p>
                <?php else: ?>
                    <form method="post" action="taikhoan.php" autocomplete="off">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="id" value="<?= (int)$chiTiet['id'] ?>">
                        <input type="hidden" name="viec" value="doi_mat_khau">
                        <label class="o"><span>Mật khẩu mới</span>
                            <input type="password" name="matkhau" required minlength="<?= PASSWORD_MIN ?>" maxlength="<?= PASSWORD_MAX ?>">
                        </label>
                        <label class="o" style="margin-top:10px;"><span>Nhập lại</span>
                            <input type="password" name="matkhau2" required minlength="<?= PASSWORD_MIN ?>" maxlength="<?= PASSWORD_MAX ?>">
                        </label>
                        <div class="form-actions">
                            <button class="btn btn-primary btn-nho">Đổi mật khẩu</button>
                        </div>
                        <p class="muted small" style="margin-top:8px;">Băm bằng <code>PASSWORD()</code> để khớp login server.</p>
                    </form>
                <?php endif; ?>
            </div>
        </div>

        <div class="card">
            <h3>Nhân vật của tài khoản này</h3>
            <div class="tbl-wrap">
            <table class="tbl">
                <thead><tr>
                    <th>Nhân vật</th><th class="num">Cấp</th><th class="num">Lượng</th>
                    <th class="num">Xu</th><th>Vào lần cuối</th><th>Trạng thái</th><th></th>
                </tr></thead>
                <tbody>
                <?php foreach ($nhanVat as $nv): ?>
                    <tr>
                        <td><strong><?= e($nv['charname']) ?></strong>
                            <div class="muted small"><?= e(tenNghe($nv['class'])) ?> · máy chủ <?= (int)$nv['idserver'] ?></div></td>
                        <td class="num"><?= (int)$nv['lastLv'] ?></td>
                        <td class="num"><?= so($nv['luong']) ?></td>
                        <td class="num"><?= so($nv['gold']) ?></td>
                        <td class="nowrap muted small"><?= e((string)$nv['lastLog']) ?></td>
                        <td class="nowrap">
                            <?php if ((int)$nv['ban'] !== 0): ?><span class="nhan nhan-khoa">Khoá</span>
                            <?php elseif ((int)$nv['del'] !== 1): ?><span class="nhan nhan-mo">Đã xoá</span>
                            <?php else: ?><span class="nhan nhan-ok">Bình thường</span><?php endif; ?>
                        </td>
                        <td><a class="btn btn-xam btn-nho" href="nhanvat.php?q=<?= e(urlencode($nv['charname'])) ?>">Mở</a></td>
                    </tr>
                <?php endforeach; ?>
                <?php if (!$nhanVat): ?>
                    <tr><td colspan="7" class="muted">Tài khoản này chưa tạo nhân vật nào.</td></tr>
                <?php endif; ?>
                </tbody>
            </table>
            </div>
        </div>
    </div>

<?php else: ?>

    <div class="card">
        <h3><?= $q === '' ? '30 tài khoản mới nhất' : 'Kết quả cho “' . e($q) . '”' ?></h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr>
                <th class="num">ID</th><th>Tài khoản</th><th>Email</th>
                <th>Đăng ký</th><th>Trạng thái</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($ketQua as $r): ?>
                <tr>
                    <td class="num"><?= (int)$r['id'] ?></td>
                    <td><strong><?= e($r['username']) ?></strong>
                        <?php if (laTaiKhoanAdmin($r['username'])): ?>
                            <span class="nhan nhan-cho">admin</span>
                        <?php endif; ?></td>
                    <td class="muted small"><?= e((string)($r['email'] ?? '')) ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['regdate']) ?></td>
                    <td><?= nhanTrangThaiBan((int)$r['ban']) ?></td>
                    <td><a class="btn btn-xam btn-nho" href="taikhoan.php?id=<?= (int)$r['id'] ?>">Xem</a></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$ketQua): ?>
                <tr><td colspan="6" class="muted">Không có tài khoản nào khớp.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
        <?php if (count($ketQua) >= 50): ?>
            <p class="muted small" style="margin-top:10px;">Chỉ hiện 50 kết quả đầu — gõ cụ thể hơn để thu hẹp.</p>
        <?php endif; ?>
    </div>

<?php endif; ?>
<?php adminFoot(); ?>
