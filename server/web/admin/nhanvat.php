<?php
/**
 * Quản lý nhân vật (bảng kpah_db.tob_char).
 *
 * KHOÁ NHÂN VẬT là đặt tob_char.ban = 1 — đúng cột mà bảng điều khiển trong game
 * dùng (AdminPanel.handleBanAccount). Hệ quả trong game:
 *   · Nhân vật BIẾN MẤT khỏi màn hình chọn nhân vật (câu lấy danh sách lọc
 *     `AND ban=0 AND del=1`), người chơi sẽ tưởng bị xoá tài khoản.
 *   · Bị loại khỏi mọi bảng xếp hạng (các câu top đều có `and ban=0`).
 *   · Tài khoản VẪN đăng nhập được, chỉ là không còn nhân vật để vào.
 * Muốn chặn cả tài khoản thì khoá ở trang Tài khoản (team_user.ban).
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$TRANG = 'nhanvat.php';

/** Tên tài khoản sở hữu một nhân vật, để không lỡ tay khoá nhân vật của admin. */
function chuNhan(int $userId): string
{
    try {
        $st = dbAccount()->prepare('SELECT username FROM team_user WHERE id = ? LIMIT 1');
        $st->execute([$userId]);
        return (string)($st->fetchColumn() ?: '');
    } catch (Throwable $ex) {
        return '';
    }
}

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $viec = p('viec');
    $id   = (int)p('id');

    $st = dbGame()->prepare('SELECT id, charname, userId, ban FROM tob_char WHERE id = ? LIMIT 1');
    $st->execute([$id]);
    $nv = $st->fetch();

    if (!$nv) {
        veLai($TRANG, 'error', 'Không tìm thấy nhân vật #' . $id . '.');
    }

    $ten = (string)$nv['charname'];
    $ve  = $TRANG . '?id=' . $id;

    if ($viec !== 'khoa' && $viec !== 'mo_khoa') {
        veLai($TRANG, 'error', 'Không hiểu thao tác.');
    }

    if ($viec === 'khoa') {
        $chu = chuNhan((int)$nv['userId']);
        if ($chu !== '' && laTaiKhoanAdmin($chu)) {
            veLai($ve, 'error', 'Nhân vật này thuộc tài khoản quản trị (' . $chu . ') nên không khoá được.');
        }
    }

    $banMoi = $viec === 'khoa' ? 1 : 0;
    $up = dbGame()->prepare('UPDATE tob_char SET ban = ? WHERE id = ?');
    $up->execute([$banMoi, $id]);

    ghiNhatKy($viec === 'khoa' ? 'khoa_nhan_vat' : 'mo_nhan_vat', $ten,
        'ban: ' . (int)$nv['ban'] . ' -> ' . $banMoi);

    veLai($ve, 'success', $viec === 'khoa'
        ? 'Đã khoá nhân vật ' . $ten . '. Nhân vật sẽ biến mất khỏi màn chọn nhân vật của người chơi.'
        : 'Đã mở khoá nhân vật ' . $ten . '.');
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu
// ---------------------------------------------------------------------------
$q     = g('q');
$xemId = (int)g('id');
$ketQua = [];
$ct     = null;
$chuTk  = '';
$loiDoc = '';

$COT = 'id, charname, userId, lastLv, `class`, xp, gold, luong, luonglock, ban, del,
        lastLog, idserver, idClan, nhack, blacklist, tichluy, topNap';

try {
    if ($xemId > 0) {
        $st = dbGame()->prepare("SELECT $COT FROM tob_char WHERE id = ? LIMIT 1");
        $st->execute([$xemId]);
        $ct = $st->fetch() ?: null;
        if ($ct) {
            $chuTk = chuNhan((int)$ct['userId']);
        }
    } elseif ($q !== '') {
        if (ctype_digit($q)) {
            // Số: hiểu là userId (xem hết nhân vật của một tài khoản) hoặc id nhân vật.
            $st = dbGame()->prepare("SELECT $COT FROM tob_char WHERE userId = ? OR id = ?
                                      ORDER BY lastLv DESC LIMIT 50");
            $st->execute([(int)$q, (int)$q]);
        } else {
            $st = dbGame()->prepare("SELECT $COT FROM tob_char WHERE charname LIKE ?
                                      ORDER BY lastLv DESC LIMIT 50");
            $st->execute(['%' . $q . '%']);
        }
        $ketQua = $st->fetchAll();
    } else {
        $ketQua = dbGame()->query("SELECT $COT FROM tob_char ORDER BY lastLog DESC LIMIT 30")->fetchAll();
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

function nhanNhanVat(array $nv): string
{
    if ((int)$nv['ban'] !== 0) return '<span class="nhan nhan-khoa">Bị khoá</span>';
    if ((int)$nv['del'] !== 1) return '<span class="nhan nhan-mo">Đã xoá</span>';
    return '<span class="nhan nhan-ok">Bình thường</span>';
}

function nutKhoa(array $nv): void
{
    $khoa = (int)$nv['ban'] !== 0;
    ?>
    <form method="post" action="nhanvat.php" class="inline"
          onsubmit="return confirm('<?= $khoa ? 'Mở khoá' : 'Khoá' ?> nhân vật <?= e($nv['charname']) ?>?');">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="id" value="<?= (int)$nv['id'] ?>">
        <input type="hidden" name="viec" value="<?= $khoa ? 'mo_khoa' : 'khoa' ?>">
        <button class="btn <?= $khoa ? 'btn-primary' : 'btn-do' ?> btn-nho"><?= $khoa ? 'Mở khoá' : 'Khoá' ?></button>
    </form>
    <?php
}

adminHead('nhanvat', 'Nhân vật');
?>
<div class="page-head">
    <div>
        <h1>Nhân vật</h1>
        <p>Khoá / mở khoá từng nhân vật. Muốn chặn cả tài khoản thì làm ở trang <a href="taikhoan.php">Tài khoản</a>.</p>
    </div>
    <form method="get" action="nhanvat.php" class="ngang">
        <label class="o" style="min-width:240px;">
            <span>Tìm theo tên nhân vật, hoặc gõ số để tra theo ID tài khoản</span>
            <input type="text" name="q" value="<?= e($q) ?>" placeholder="ví dụ: chienthan">
        </label>
        <button type="submit" class="btn btn-primary btn-nho">Tìm</button>
    </form>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<?php if ($ct): ?>
    <p><a href="nhanvat.php">← Về danh sách</a></p>
    <div class="grid-side">
        <div class="card">
            <h3><?= e($ct['charname']) ?> <?= nhanNhanVat($ct) ?></h3>
            <dl class="dl">
                <dt>ID nhân vật</dt><dd><?= (int)$ct['id'] ?></dd>
                <dt>Tài khoản</dt><dd>
                    <?php if ($chuTk !== ''): ?>
                        <a href="taikhoan.php?id=<?= (int)$ct['userId'] ?>"><?= e($chuTk) ?></a>
                        <span class="muted small">(#<?= (int)$ct['userId'] ?>)</span>
                    <?php else: ?>
                        <span class="muted">không tìm thấy tài khoản #<?= (int)$ct['userId'] ?></span>
                    <?php endif; ?>
                </dd>
                <dt>Nghề</dt><dd><?= e(tenNghe($ct['class'])) ?></dd>
                <dt>Cấp độ</dt><dd><?= (int)$ct['lastLv'] ?> <span class="muted small">(<?= so($ct['xp']) ?> kinh nghiệm)</span></dd>
                <dt>Xu</dt><dd><?= so($ct['gold']) ?></dd>
                <dt>Lượng</dt><dd><?= so($ct['luong']) ?> <span class="muted small">+ <?= so($ct['luonglock']) ?> khoá</span></dd>
                <dt>Tích luỹ</dt><dd><?= so($ct['tichluy']) ?> <span class="muted small">· top nạp <?= so($ct['topNap']) ?></span></dd>
                <dt>Vào lần cuối</dt><dd><?= e((string)$ct['lastLog']) ?></dd>
                <dt>Máy chủ</dt><dd><?= (int)$ct['idserver'] ?></dd>
                <dt>Bang hội</dt><dd><?= (int)$ct['idClan'] === -1 ? '<span class="muted">không có</span>' : (int)$ct['idClan'] ?></dd>
                <dt>Cờ nghi vấn</dt><dd>nhack <?= (int)$ct['nhack'] ?> · blacklist <?= (int)$ct['blacklist'] ?></dd>
            </dl>

            <div class="form-actions"><?php nutKhoa($ct); ?></div>

            <?php if ((int)$ct['ban'] !== 0): ?>
                <div class="luuy" style="margin-top:14px;">
                    <p><strong>Nhân vật này đang bị khoá.</strong></p>
                    <p>Nếu bạn không phải người khoá nó thì rất có thể server tự khoá: khi lưu nhân vật
                       thất bại (ví dụ hành trang quá dài so với sức chứa của cột <code>inven</code>),
                       khối bắt lỗi trong <code>Database.java</code> tự đặt <code>ban = 1</code>.
                       Xem <code>vps/sql-giftcode.md</code>, mục “CẠM BẪY ĐÃ NỔ MỘT LẦN”.</p>
                    <p>Mở khoá lấy lại được nhân vật, nhưng <em>không</em> lấy lại phần dữ liệu đã mất
                       giữa lúc lưu hỏng.</p>
                </div>
            <?php endif; ?>
        </div>

        <div class="card">
            <h3>Khoá nhân vật làm gì</h3>
            <ul class="muted" style="font-size:.9rem;padding-left:18px;margin:0;">
                <li>Nhân vật <strong>biến mất khỏi màn hình chọn nhân vật</strong> — người chơi thường
                    hiểu nhầm là bị xoá.</li>
                <li>Bị loại khỏi mọi bảng xếp hạng.</li>
                <li>Tài khoản <strong>vẫn đăng nhập được</strong> và vẫn chơi được các nhân vật khác.</li>
                <li>Người đang online <strong>không bị đá ra</strong> — trang web không nói chuyện được
                    với tiến trình game. Việc khoá chỉ ăn từ lần vào sau.</li>
            </ul>
        </div>
    </div>

<?php else: ?>

    <div class="card">
        <h3><?= $q === '' ? '30 nhân vật vào game gần nhất' : 'Kết quả cho “' . e($q) . '”' ?></h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr>
                <th>Nhân vật</th><th class="num">Cấp</th><th class="num">Lượng</th>
                <th class="num">Xu</th><th>Vào lần cuối</th><th>Trạng thái</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($ketQua as $r): ?>
                <tr>
                    <td><a href="nhanvat.php?id=<?= (int)$r['id'] ?>"><strong><?= e($r['charname']) ?></strong></a>
                        <div class="muted small"><?= e(tenNghe($r['class'])) ?> · tài khoản #<?= (int)$r['userId'] ?></div></td>
                    <td class="num"><?= (int)$r['lastLv'] ?></td>
                    <td class="num"><?= so($r['luong']) ?></td>
                    <td class="num"><?= so($r['gold']) ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['lastLog']) ?></td>
                    <td><?= nhanNhanVat($r) ?></td>
                    <td class="nowrap"><?php nutKhoa($r); ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$ketQua): ?>
                <tr><td colspan="7" class="muted">Không có nhân vật nào khớp.</td></tr>
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
