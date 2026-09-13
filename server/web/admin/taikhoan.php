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
require_once __DIR__ . '/inc/lenh_nhan_vat.php';
require_once __DIR__ . '/inc/xoa_tai_khoan.php';
requireAdmin();

$TRANG = 'taikhoan.php';

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $viec = p('viec');
    if ($viec === 'xoa_nhieu_tai_khoan') {
        if (p('xac_nhan_xoa') !== 'XOA DA CHON') {
            veLai($TRANG, 'error', 'Gõ XOA DA CHON để xác nhận xoá các tài khoản đã chọn.');
        }
        try {
            $ids = idsTaiKhoanDaChon($_POST['ids'] ?? null);
        } catch (InvalidArgumentException $ex) {
            veLai($TRANG, 'error', $ex->getMessage());
        }
        $kq = xoaNhieuTaiKhoan(dbAccount(), dbGame(), $ids, adminName());
        $msg = 'Đã xoá ' . count($kq['xoa']) . ' tài khoản';
        if ($kq['xoa']) $msg .= ': ' . implode(', ', $kq['xoa']);
        $msg .= '.';
        if ($kq['cho']) $msg .= ' Đang chờ xoá nhân vật: ' . implode(', ', $kq['cho'])
            . '. Khi server xử lý xong, chọn lại các tài khoản này và bấm xoá để hoàn tất.';
        if ($kq['bo_qua']) $msg .= ' Bỏ qua: ' . implode(', ', $kq['bo_qua']) . '.';
        if ($kq['loi']) $msg .= ' Chưa hoàn tất do lỗi: ' . implode(', ', $kq['loi']) . '. Có thể thử lại.';
        veLai($TRANG, ($kq['cho'] || $kq['loi'] || $kq['bo_qua']) ? 'warning' : 'success', $msg);
    }
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

        case 'xoa_tai_khoan':
            // XOA VINH VIEN - khong hoan tac duoc. Chan tai khoan quan tri giong het "khoa"
            // o tren, cong them go dung ten de xac nhan (JS chi bat nut, kiem tra that o
            // day - phong khi ai do POST thang khong qua giao dien).
            if ($laAdmin) {
                veLai($ve, 'error', 'Không xoá được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS trước nếu thật sự cần.');
            }
            if (p('xac_nhan_ten') !== $ten) {
                veLai($ve, 'error', 'Gõ đúng tên tài khoản "' . $ten . '" để xác nhận xoá.');
            }

            // Xoa VINH VIEN tung nhan vat truoc (di qua bang lenh vi co the dang online -
            // xem inc/lenh_nhan_vat.php). Cho toi da 2 giay/nhan vat de tong thoi gian mot
            // yeu cau web khong qua lau; con nhan vat nao chua xong thi tai khoan CHUA bi
            // xoa, bam lai nut nay sau se tiep tuc (moi buoc deu idempotent).
            $dsNV = dbGame()->prepare('SELECT id, charname FROM tob_char WHERE userId = ?');
            $dsNV->execute([$id]);
            $dsNV = $dsNV->fetchAll();

            $chiTietLenh = [];
            foreach ($dsNV as $nv2) {
                $idLenh = guiLenhXoaNhanVat(dbGame(), (int)$nv2['id'], (string)$nv2['charname'], adminName());
                $lenh   = choKetQuaLenh(dbGame(), $idLenh, 2.0);
                $chiTietLenh[] = (string)$nv2['charname'] . ': ' . tenTrangThaiLenh((int)($lenh['trang_thai'] ?? -1));
            }

            $conLai = dbGame()->prepare('SELECT COUNT(*) FROM tob_char WHERE userId = ?');
            $conLai->execute([$id]);
            $soConLai = (int)$conLai->fetchColumn();

            if ($soConLai > 0) {
                ghiNhatKy('xoa_tai_khoan_tam_dung', $ten,
                    'còn ' . $soConLai . '/' . count($dsNV) . ' nhân vật chưa xoá xong: ' . implode('; ', $chiTietLenh));
                veLai($ve, 'warning', 'Còn ' . $soConLai . ' nhân vật chưa xoá xong (' . implode('; ', $chiTietLenh)
                    . ') — có thể server game đang tắt hoặc chậm. Tài khoản CHƯA bị xoá; bấm "Xoá vĩnh viễn" lại '
                    . 'để tiếp tục khi tất cả nhân vật đã xoá xong.');
            }

            // Toan bo nhan vat da xoa xong -> xoa 5h_active (giong huy_kich_hoat) roi xoa
            // that tai khoan. team_user o DB rieng (account) ma server game khong dung toi
            // ngoai luc dang nhap, nen xoa thang o day an toan - giong cach "khoa" da lam.
            dbGame()->prepare('DELETE FROM `5h_active` WHERE username = ? OR userID = ?')->execute([$ten, $id]);
            dbAccount()->prepare('DELETE FROM team_user WHERE id = ?')->execute([$id]);

            ghiNhatKy('xoa_tai_khoan', $ten, 'đã xoá vĩnh viễn tài khoản (' . count($dsNV) . ' nhân vật đã xoá cùng lúc)');
            veLai($TRANG, 'success', 'Đã xoá vĩnh viễn tài khoản ' . $ten . ' và toàn bộ ' . count($dsNV) . ' nhân vật của nó.');
            break;

        case 'reset_trang':
            // RESET VE TRANG: giong het xoa_tai_khoan o tren (cung di qua bang lenh vi nhan vat
            // co the dang online) nhung KHONG dung toi 5h_active/team_user - tai khoan dang nhap
            // giu nguyen, chi mat het nhan vat, de dang nhap lai la nhu vua dang ky.
            if ($laAdmin) {
                veLai($ve, 'error', 'Không reset được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS trước nếu thật sự cần.');
            }
            if (p('xac_nhan_ten') !== $ten) {
                veLai($ve, 'error', 'Gõ đúng tên tài khoản "' . $ten . '" để xác nhận reset.');
            }

            $dsNV = dbGame()->prepare('SELECT id, charname FROM tob_char WHERE userId = ?');
            $dsNV->execute([$id]);
            $dsNV = $dsNV->fetchAll();

            if (!$dsNV) {
                veLai($ve, 'success', 'Tài khoản ' . $ten . ' đã trắng sẵn, chưa có nhân vật nào.');
            }

            $chiTietLenh = [];
            foreach ($dsNV as $nv2) {
                $idLenh = guiLenhXoaNhanVat(dbGame(), (int)$nv2['id'], (string)$nv2['charname'], adminName());
                $lenh   = choKetQuaLenh(dbGame(), $idLenh, 2.0);
                $chiTietLenh[] = (string)$nv2['charname'] . ': ' . tenTrangThaiLenh((int)($lenh['trang_thai'] ?? -1));
            }

            $conLai = dbGame()->prepare('SELECT COUNT(*) FROM tob_char WHERE userId = ?');
            $conLai->execute([$id]);
            $soConLai = (int)$conLai->fetchColumn();

            if ($soConLai > 0) {
                ghiNhatKy('reset_tai_khoan_tam_dung', $ten,
                    'còn ' . $soConLai . '/' . count($dsNV) . ' nhân vật chưa xoá xong: ' . implode('; ', $chiTietLenh));
                veLai($ve, 'warning', 'Còn ' . $soConLai . ' nhân vật chưa xoá xong (' . implode('; ', $chiTietLenh)
                    . ') — có thể server game đang tắt hoặc chậm. Tài khoản CHƯA reset xong; bấm "Reset về trắng" lại '
                    . 'để tiếp tục khi tất cả nhân vật đã xoá xong.');
            }

            ghiNhatKy('reset_tai_khoan', $ten, 'đã xoá ' . count($dsNV) . ' nhân vật, đưa tài khoản về trắng');
            veLai($ve, 'success', 'Đã reset tài khoản ' . $ten . ' về trắng (đã xoá ' . count($dsNV)
                . ' nhân vật). Tài khoản vẫn đăng nhập được bình thường và có thể tạo nhân vật mới ngay.');
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
$trang  = max(1, (int)g('trang', '1'));
$moi    = 30;
$ketQua = [];
$chiTiet = null;
$nhanVat = [];
$daKichHoat = false;
$conTrangSau = false;
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
        // Lay du 1 dong de biet con trang sau hay khong, khong COUNT(*) rieng (xem log.php).
        $lay = $moi + 1;
        $bo  = ($trang - 1) * $moi;
        $ketQua = dbAccount()->query(
            "SELECT id, username, email, phone, regdate, ban FROM team_user
             ORDER BY id DESC LIMIT $lay OFFSET $bo"
        )->fetchAll();
        if (count($ketQua) > $moi) {
            $conTrangSau = true;
            array_pop($ketQua);
        }
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

            <div class="card">
                <h3 style="color:#c0392b;">Reset về trắng</h3>
                <?php if ($laAdmin): ?>
                    <p class="muted small">Không reset được tài khoản quản trị.</p>
                <?php else: ?>
                    <div class="luuy">
                        <p><strong>KHÔNG THỂ HOÀN TÁC.</strong> Xoá vĩnh viễn toàn bộ
                           <strong><?= count($nhanVat) ?> nhân vật</strong> của tài khoản này (cùng dữ liệu
                           riêng của từng nhân vật: thú nuôi, trang trại, đá quý...) nhưng
                           <strong>giữ nguyên tài khoản đăng nhập</strong> — khác với "Xoá vĩnh viễn" bên
                           dưới. Đăng nhập lại được ngay và tạo nhân vật mới, y như tài khoản vừa đăng ký.
                           Nhân vật đang online sẽ bị đẩy ra khỏi game trước khi xoá. Nhật ký giao dịch/log
                           vẫn được giữ lại để tra cứu.</p>
                    </div>
                    <form method="post" action="taikhoan.php" class="ngang" style="margin-top:12px;"
                          onsubmit="return confirm('RESET tài khoản <?= e($ten) ?> về trắng — xoá toàn bộ <?= count($nhanVat) ?> nhân vật của nó?\n\nKhông thể hoàn tác.');">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="id" value="<?= (int)$chiTiet['id'] ?>">
                        <input type="hidden" name="viec" value="reset_trang">
                        <label class="o" style="min-width:220px;">
                            <span>Gõ đúng tên "<?= e($ten) ?>" để xác nhận</span>
                            <input type="text" name="xac_nhan_ten" autocomplete="off"
                                   oninput="document.getElementById('nutResetTK').disabled = this.value !== <?= e(json_encode($ten)) ?>;">
                        </label>
                        <button type="submit" id="nutResetTK" class="btn btn-do btn-nho" disabled style="align-self:flex-end;">Reset về trắng</button>
                    </form>
                <?php endif; ?>
            </div>

            <div class="card">
                <h3 style="color:#c0392b;">Xoá vĩnh viễn</h3>
                <?php if ($laAdmin): ?>
                    <p class="muted small">Không xoá được tài khoản quản trị.</p>
                <?php else: ?>
                    <div class="luuy">
                        <p><strong>KHÔNG THỂ HOÀN TÁC.</strong> Khác hẳn "Khoá đăng nhập" ở trên (chỉ chặn
                           đăng nhập, mở lại được) — xoá vĩnh viễn xoá hẳn tài khoản này khỏi cơ sở dữ
                           liệu, cùng với <strong>toàn bộ <?= count($nhanVat) ?> nhân vật</strong> của nó
                           (và dữ liệu riêng của từng nhân vật: thú nuôi, trang trại, đá quý...). Nhân vật
                           đang online sẽ bị đẩy ra khỏi game trước khi xoá. Nhật ký giao dịch/log vẫn
                           được giữ lại để tra cứu.</p>
                    </div>
                    <form method="post" action="taikhoan.php" class="ngang" style="margin-top:12px;"
                          onsubmit="return confirm('XOÁ VĨNH VIỄN tài khoản <?= e($ten) ?> và toàn bộ <?= count($nhanVat) ?> nhân vật của nó?\n\nKhông thể hoàn tác.');">
                        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                        <input type="hidden" name="id" value="<?= (int)$chiTiet['id'] ?>">
                        <input type="hidden" name="viec" value="xoa_tai_khoan">
                        <label class="o" style="min-width:220px;">
                            <span>Gõ đúng tên "<?= e($ten) ?>" để xác nhận</span>
                            <input type="text" name="xac_nhan_ten" autocomplete="off"
                                   oninput="document.getElementById('nutXoaTK').disabled = this.value !== <?= e(json_encode($ten)) ?>;">
                        </label>
                        <button type="submit" id="nutXoaTK" class="btn btn-do btn-nho" disabled style="align-self:flex-end;">Xoá vĩnh viễn</button>
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
        <h3><?= $q === '' ? 'Danh sách tài khoản' : 'Kết quả cho “' . e($q) . '”' ?></h3>
        <form method="post" action="taikhoan.php" id="xoaNhieuTaiKhoan">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="viec" value="xoa_nhieu_tai_khoan">
            <p class="muted small">Chọn tài khoản cần xoá trên trang này. Xoá vĩnh viễn cả tài khoản và toàn bộ nhân vật; không thể hoàn tác.</p>
            <div class="ngang" style="margin:12px 0;">
                <label><input type="checkbox" id="chonTatCaTK"> Chọn tất cả trên trang</label>
                <span id="soTaiKhoanChon" aria-live="polite">Đã chọn 0 tài khoản</span>
                <label class="o"><span>Gõ XOA DA CHON để xác nhận</span>
                    <input name="xac_nhan_xoa" id="xacNhanXoaNhieu" autocomplete="off" required pattern="XOA DA CHON">
                </label>
                <button type="submit" class="btn btn-do" id="nutXoaNhieu" disabled>Xoá tài khoản đã chọn</button>
            </div>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr>
                <th>Chọn</th><th class="num">ID</th><th>Tài khoản</th><th>Email</th>
                <th>Đăng ký</th><th>Trạng thái</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($ketQua as $r): ?>
                <tr>
                    <td><input type="checkbox" name="ids[]" value="<?= (int)$r['id'] ?>"
                        data-ten="<?= e($r['username']) ?>" aria-label="Chọn <?= e($r['username']) ?>"
                        <?= laTaiKhoanAdmin($r['username']) || strcasecmp($r['username'], adminName()) === 0 ? 'disabled' : '' ?>></td>
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
                <tr><td colspan="7" class="muted">Không có tài khoản nào khớp.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
        </form>
        <?php if ($q === ''): ?>
            <div class="phantrang">
                <?php if ($trang > 1): ?>
                    <a class="btn btn-xam btn-nho" href="taikhoan.php?trang=<?= $trang - 1 ?>">← Trang trước</a>
                <?php endif; ?>
                <span>Trang <?= $trang ?> · <?= count($ketQua) ?> dòng</span>
                <?php if ($conTrangSau): ?>
                    <a class="btn btn-xam btn-nho" href="taikhoan.php?trang=<?= $trang + 1 ?>">Trang sau →</a>
                <?php endif; ?>
            </div>
        <?php elseif (count($ketQua) >= 50): ?>
            <p class="muted small" style="margin-top:10px;">Chỉ hiện 50 kết quả đầu — gõ cụ thể hơn để thu hẹp.</p>
        <?php endif; ?>
    </div>

<?php endif; ?>
<script>
(() => {
    const form = document.getElementById('xoaNhieuTaiKhoan');
    if (!form) return;
    const boxes = Array.from(form.querySelectorAll('input[name="ids[]"]:not(:disabled)'));
    const all = document.getElementById('chonTatCaTK');
    const confirmText = document.getElementById('xacNhanXoaNhieu');
    const button = document.getElementById('nutXoaNhieu');
    const selected = () => boxes.filter(box => box.checked);
    function update() {
        const n = selected().length;
        document.getElementById('soTaiKhoanChon').textContent = 'Đã chọn ' + n + ' tài khoản';
        all.checked = n > 0 && n === boxes.length;
        all.indeterminate = n > 0 && n < boxes.length;
        all.disabled = boxes.length === 0;
        button.disabled = n === 0 || confirmText.value !== 'XOA DA CHON';
    }
    all.addEventListener('change', () => { boxes.forEach(box => { box.checked = all.checked; }); update(); });
    boxes.forEach(box => box.addEventListener('change', update));
    confirmText.addEventListener('input', update);
    form.addEventListener('submit', event => {
        const chosen = selected();
        if (!chosen.length || confirmText.value !== 'XOA DA CHON' || !window.confirm(
            'Xoá vĩnh viễn ' + chosen.length + ' tài khoản và toàn bộ nhân vật của chúng?\n\n'
            + chosen.map(box => box.dataset.ten).join(', ') + '\n\nKhông thể hoàn tác.')) event.preventDefault();
    });
    update();
})();
</script>
<?php adminFoot(); ?>
