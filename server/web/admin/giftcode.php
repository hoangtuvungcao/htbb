<?php
/**
 * Quản lý mã quà tặng (bảng kpah_db.giftcode + giftcode_log).
 *
 * Bốn điều server làm, quyết định mọi thứ trên trang này
 * (DatabaseGiftCode.java:301-600):
 *   1. Câu tìm mã là `select * from giftcode where giftcode='...' and type = 0`
 *      → type khác 0 là mã vô hình với người chơi.
 *   2. `limit_use` là số lượt CÒN LẠI. Bằng 0 thì báo hết lượt. Chỉ trừ đi khi
 *      `limit >= 1`, nên số âm là không giới hạn thật sự (không trừ tiếp).
 *   3. Chặn nhận lại tính theo TÊN NHÂN VẬT trong giftcode_log, không theo tài
 *      khoản → một tài khoản 3 nhân vật thì nhận được 3 lần cùng một mã.
 *   4. Cột `expire` chưa bao giờ được đọc. Muốn hết hạn thì tự đặt limit_use = 0.
 *
 * VÌ SAO PHẢI LỌC KÝ TỰ CHẶT: server ghép thẳng cả tên mã lẫn chuỗi phần thưởng
 * vào câu SQL (`insert into giftcode_log set ... giftcode = '<mã>', item = '<item>'`),
 * không dùng tham số. Một dấu nháy đơn lọt vào đây là hỏng câu lệnh của server.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/thuong.php';
requireAdmin();

$TRANG = 'giftcode.php';

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);
    $viec = p('viec');

    try {
        switch ($viec) {

            case 'tao':
            case 'sua': {
                $id    = (int)p('id');
                $ma    = p('giftcode');
                $xu    = (int)p('xu', '0');
                $luong = (int)p('luong', '0');
                $lock  = (int)p('luongLock', '0');
                $limit = (int)p('limit_use', '0');
                $type  = (int)p('type', '0');
                [$item, $loiItem] = kiemTraItem(p('item'));

                if (!preg_match('/^[A-Za-z0-9_]{2,50}$/', $ma)) {
                    veLai($TRANG, 'error', 'Tên mã chỉ được dùng chữ, số và gạch dưới, dài 2–50 ký tự.');
                }
                if ($loiItem) {
                    veLai($TRANG, 'error', 'Phần thưởng chưa hợp lệ: ' . implode(' | ', $loiItem));
                }
                if ($xu < 0 || $luong < 0 || $lock < 0) {
                    veLai($TRANG, 'error', 'Xu và lượng không được âm.');
                }

                // Trùng tên mã: server luôn lấy dòng đầu tiên (`limit 1`) nên dòng
                // thứ hai vĩnh viễn không ai nhận được.
                $ck = dbGame()->prepare('SELECT id FROM giftcode WHERE giftcode = ? AND id <> ? LIMIT 1');
                $ck->execute([$ma, $id]);
                if ($ck->fetchColumn()) {
                    veLai($TRANG, 'error', 'Đã có mã tên “' . $ma . '”. Đặt tên khác, vì server chỉ đọc dòng đầu tiên.');
                }

                if ($viec === 'tao') {
                    $st = dbGame()->prepare(
                        'INSERT INTO giftcode (giftcode, xu, luong, luongLock, item, expire, limit_use, type)
                         VALUES (?, ?, ?, ?, ?, 0, ?, ?)'
                    );
                    $st->execute([$ma, $xu, $luong, $lock, $item, $limit, $type]);
                    ghiNhatKy('tao_giftcode', $ma,
                        "xu=$xu luong=$luong luongLock=$lock limit=$limit type=$type item=$item");
                    veLai($TRANG, 'success', 'Đã tạo mã ' . $ma . '.');
                } else {
                    $st = dbGame()->prepare(
                        'UPDATE giftcode SET giftcode = ?, xu = ?, luong = ?, luongLock = ?,
                                             item = ?, limit_use = ?, type = ? WHERE id = ?'
                    );
                    $st->execute([$ma, $xu, $luong, $lock, $item, $limit, $type, $id]);
                    ghiNhatKy('sua_giftcode', $ma,
                        "xu=$xu luong=$luong luongLock=$lock limit=$limit type=$type item=$item");
                    veLai($TRANG, 'success', 'Đã lưu mã ' . $ma . '.');
                }
                break;
            }

            case 'tat': {
                $id = (int)p('id');
                $st = dbGame()->prepare('SELECT giftcode, limit_use FROM giftcode WHERE id = ?');
                $st->execute([$id]);
                $gc = $st->fetch();
                if (!$gc) {
                    veLai($TRANG, 'error', 'Không tìm thấy mã #' . $id . '.');
                }
                dbGame()->prepare('UPDATE giftcode SET limit_use = 0 WHERE id = ?')->execute([$id]);
                ghiNhatKy('tat_giftcode', (string)$gc['giftcode'], 'limit_use: ' . (int)$gc['limit_use'] . ' -> 0');
                veLai($TRANG, 'success', 'Đã tắt mã ' . $gc['giftcode'] . ' (đặt số lượt còn lại về 0).');
                break;
            }

            case 'xoa': {
                $id = (int)p('id');
                $st = dbGame()->prepare('SELECT giftcode FROM giftcode WHERE id = ?');
                $st->execute([$id]);
                $ten = (string)($st->fetchColumn() ?: '');
                if ($ten === '') {
                    veLai($TRANG, 'error', 'Không tìm thấy mã #' . $id . '.');
                }
                dbGame()->prepare('DELETE FROM giftcode WHERE id = ?')->execute([$id]);
                ghiNhatKy('xoa_giftcode', $ten, 'xoá hẳn khỏi bảng giftcode');
                veLai($TRANG, 'success', 'Đã xoá mã ' . $ten . '. Nhật ký ai đã nhận vẫn còn trong giftcode_log.');
                break;
            }

            case 'cho_nhan_lai': {
                $ma     = p('ma');
                $player = p('player');
                $st = dbGame()->prepare('DELETE FROM giftcode_log WHERE giftcode = ? AND player = ?');
                $st->execute([$ma, $player]);
                ghiNhatKy('cho_nhan_lai_giftcode', $ma, 'nhân vật ' . $player . ', xoá ' . $st->rowCount() . ' dòng');
                veLai($TRANG . '?tab=log', 'success',
                    'Nhân vật ' . $player . ' có thể nhận lại mã ' . $ma . ' (' . $st->rowCount() . ' dòng đã xoá).');
                break;
            }

            default:
                veLai($TRANG, 'error', 'Không hiểu thao tác.');
        }
    } catch (Throwable $ex) {
        veLai($TRANG, 'error', 'Lỗi: ' . $ex->getMessage());
    }
    exit;
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu
// ---------------------------------------------------------------------------
$tab   = g('tab') === 'log' ? 'log' : 'ma';
$suaId = (int)g('sua');
$dangSua = null;
$dsMa  = [];
$demNhan = [];
$loiDoc = '';

// Trang nhật ký nhận mã
$logMa     = g('ma');
$logPlayer = g('player');
$logRows   = [];
$logTrang  = max(1, (int)g('trang', '1'));
$logMoi    = 40;
$conTrangSau = false;

try {
    if ($tab === 'ma') {
        $dsMa = dbGame()->query('SELECT * FROM giftcode ORDER BY id DESC')->fetchAll();
        foreach (dbGame()->query('SELECT giftcode, COUNT(*) AS n FROM giftcode_log GROUP BY giftcode')->fetchAll() as $r) {
            $demNhan[strtolower((string)$r['giftcode'])] = (int)$r['n'];
        }
        if ($suaId > 0) {
            $st = dbGame()->prepare('SELECT * FROM giftcode WHERE id = ?');
            $st->execute([$suaId]);
            $dangSua = $st->fetch() ?: null;
        }
    } else {
        $dieuKien = [];
        $thamSo   = [];
        if ($logMa !== '')     { $dieuKien[] = 'giftcode = ?'; $thamSo[] = $logMa; }
        if ($logPlayer !== '') { $dieuKien[] = 'player = ?';   $thamSo[] = $logPlayer; }
        $where = $dieuKien ? 'WHERE ' . implode(' AND ', $dieuKien) : '';

        // Lấy dư 1 dòng để biết có trang sau hay không, thay vì đếm COUNT(*)
        // trên một bảng có thể rất lớn.
        // LIMIT / OFFSET ghép thẳng bằng số nguyên đã ép kiểu: PDO đang tắt giả
        // lập prepare nên tham số gửi đi dạng chuỗi và `LIMIT '41'` là lỗi cú pháp.
        $lay = $logMoi + 1;
        $bo  = ($logTrang - 1) * $logMoi;
        $st = dbGame()->prepare("SELECT id, giftcode, player, item, xu, luong, luongK
                                   FROM giftcode_log $where ORDER BY id DESC LIMIT $lay OFFSET $bo");
        $st->execute($thamSo);
        $logRows = $st->fetchAll();
        if (count($logRows) > $logMoi) {
            $conTrangSau = true;
            array_pop($logRows);
        }
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

$f = static function (?array $r, string $cot, string $macDinh = '') {
    return $r === null ? $macDinh : (string)$r[$cot];
};

adminHead('giftcode', 'Giftcode');
?>
<div class="page-head">
    <div>
        <h1>Giftcode</h1>
        <p>Tạo mã, nạp thêm lượt, tắt mã, và xem ai đã nhận gì.</p>
    </div>
    <div class="ngang">
        <a href="giftcode.php" class="btn <?= $tab === 'ma' ? 'btn-primary' : 'btn-xam' ?> btn-nho">Danh sách mã</a>
        <a href="giftcode.php?tab=log" class="btn <?= $tab === 'log' ? 'btn-primary' : 'btn-xam' ?> btn-nho">Ai đã nhận</a>
    </div>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">Lỗi đọc dữ liệu: <?= e($loiDoc) ?></div>
<?php endif; ?>

<?php if ($tab === 'ma'): ?>

    <div class="card">
        <h3><?= $dangSua ? 'Sửa mã “' . e($dangSua['giftcode']) . '”' : 'Tạo mã mới' ?></h3>
        <form method="post" action="giftcode.php">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="viec" value="<?= $dangSua ? 'sua' : 'tao' ?>">
            <?php if ($dangSua): ?>
                <input type="hidden" name="id" value="<?= (int)$dangSua['id'] ?>">
            <?php endif; ?>

            <div class="form-grid">
                <label class="o"><span>Tên mã</span>
                    <input type="text" name="giftcode" required maxlength="50" pattern="[A-Za-z0-9_]{2,50}"
                           value="<?= e($f($dangSua, 'giftcode')) ?>" placeholder="tanthu2026">
                    <small>Chỉ chữ, số, gạch dưới. Không phân biệt hoa thường khi nhập trong game.</small>
                </label>
                <label class="o"><span>Xu</span>
                    <input type="number" name="xu" value="<?= e($f($dangSua, 'xu', '0')) ?>" min="0">
                </label>
                <label class="o"><span>Lượng</span>
                    <input type="number" name="luong" value="<?= e($f($dangSua, 'luong', '0')) ?>" min="0">
                </label>
                <label class="o"><span>Lượng khoá</span>
                    <input type="number" name="luongLock" value="<?= e($f($dangSua, 'luongLock', '0')) ?>" min="0">
                </label>
                <label class="o"><span>Số lượt còn lại</span>
                    <input type="number" name="limit_use" value="<?= e($f($dangSua, 'limit_use', '100')) ?>">
                    <small><strong>-1 = không giới hạn</strong> (số âm không bị trừ). 0 = mã đã tắt.</small>
                </label>
                <label class="o"><span>Trạng thái</span>
                    <select name="type">
                        <option value="0" <?= (int)$f($dangSua, 'type', '0') === 0 ? 'selected' : '' ?>>Đang bật (type 0)</option>
                        <option value="1" <?= (int)$f($dangSua, 'type', '0') === 1 ? 'selected' : '' ?>>Ẩn — người chơi không dùng được</option>
                    </select>
                </label>
            </div>

            <label class="o" style="margin-top:12px;"><span>Phần thưởng vật phẩm</span>
                <textarea name="item" placeholder="ITEM:848:1000,TRANGPHUC:833:1:30"><?= e($f($dangSua, 'item')) ?></textarea>
                <small>Nhiều phần thưởng ngăn nhau bằng dấu phẩy. Để trống nếu chỉ cho xu / lượng.
                       Cú pháp được kiểm tra trước khi lưu — xem bảng bên dưới.</small>
            </label>

            <div class="form-actions">
                <button class="btn btn-primary btn-nho"><?= $dangSua ? 'Lưu thay đổi' : 'Tạo mã' ?></button>
                <?php if ($dangSua): ?>
                    <a href="giftcode.php" class="btn btn-xam btn-nho">Huỷ</a>
                <?php endif; ?>
            </div>
        </form>

        <div class="luuy">
            <p><strong>Nhớ ba điều:</strong></p>
            <p>· Mỗi <em>nhân vật</em> chỉ nhận một mã một lần — không phải mỗi tài khoản. Một tài khoản
               ba nhân vật thì nhận được ba lần.</p>
            <p>· Cột <code>expire</code> trong bảng không có tác dụng gì cả; muốn hết hạn thì tự bấm “Tắt”.</p>
            <p>· Mã cho quá nhiều món một lúc dễ làm hành trang phình to; trước đây từng có nhân vật
               bị server tự khoá vì lưu hỏng. Xem <code>vps/sql-giftcode.md</code>.</p>
        </div>
    </div>

    <div class="card">
        <h3>Danh sách mã (<?= count($dsMa) ?>)</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr>
                <th class="num">ID</th><th>Mã</th><th class="num">Xu</th><th class="num">Lượng</th>
                <th class="num">Lượng khoá</th><th>Phần thưởng</th><th class="num">Còn lượt</th>
                <th class="num">Đã phát</th><th>Trạng thái</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($dsMa as $gc):
                $conLuot = (int)$gc['limit_use'];
                $batTat  = (int)$gc['type'] === 0;
                $dung    = $batTat && $conLuot !== 0;
            ?>
                <tr>
                    <td class="num"><?= (int)$gc['id'] ?></td>
                    <td><strong><?= e($gc['giftcode']) ?></strong></td>
                    <td class="num"><?= so($gc['xu']) ?></td>
                    <td class="num"><?= so($gc['luong']) ?></td>
                    <td class="num"><?= so($gc['luongLock']) ?></td>
                    <td class="info"><?= e((string)($gc['item'] ?? '')) ?: '<span class="muted">—</span>' ?></td>
                    <td class="num"><?= $conLuot < 0 ? '∞' : so($conLuot) ?></td>
                    <td class="num">
                        <a href="giftcode.php?tab=log&amp;ma=<?= e(urlencode($gc['giftcode'])) ?>">
                            <?= so($demNhan[strtolower((string)$gc['giftcode'])] ?? 0) ?>
                        </a>
                    </td>
                    <td><?php if ($dung): ?><span class="nhan nhan-ok">Dùng được</span>
                        <?php elseif (!$batTat): ?><span class="nhan nhan-mo">Ẩn</span>
                        <?php else: ?><span class="nhan nhan-cho">Hết lượt</span><?php endif; ?></td>
                    <td class="nowrap">
                        <a class="btn btn-xam btn-nho" href="giftcode.php?sua=<?= (int)$gc['id'] ?>">Sửa</a>
                        <?php if ($conLuot !== 0): ?>
                            <form method="post" action="giftcode.php" class="inline"
                                  onsubmit="return confirm('Tắt mã <?= e($gc['giftcode']) ?>?');">
                                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                                <input type="hidden" name="viec" value="tat">
                                <input type="hidden" name="id" value="<?= (int)$gc['id'] ?>">
                                <button class="btn btn-xam btn-nho">Tắt</button>
                            </form>
                        <?php endif; ?>
                        <form method="post" action="giftcode.php" class="inline"
                              onsubmit="return confirm('XOÁ HẲN mã <?= e($gc['giftcode']) ?>? Không hoàn lại được.');">
                            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                            <input type="hidden" name="viec" value="xoa">
                            <input type="hidden" name="id" value="<?= (int)$gc['id'] ?>">
                            <button class="btn btn-do btn-nho">Xoá</button>
                        </form>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$dsMa): ?>
                <tr><td colspan="10" class="muted">Chưa có mã nào.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>

    <div class="card">
        <h3>Các loại phần thưởng server hiểu</h3>
        <p class="muted small">Rút thẳng từ <code>src/data/DatabaseGiftCode.java</code>. Phần ghi <code>_</code>
           là tham số server không dùng tới nhưng <strong>vẫn phải có</strong> — thiếu là server ném lỗi
           giữa chừng, người chơi nhận nửa phần quà mà mã không bị trừ lượt.</p>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Cú pháp</th><th>Là gì</th></tr></thead>
            <tbody>
            <?php foreach (loaiThuong() as $loai => [$n, $cuPhap, $mota]): ?>
                <tr><td class="nowrap"><code><?= e($cuPhap) ?></code></td><td class="muted small"><?= e($mota) ?></td></tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    </div>

<?php else: ?>

    <div class="card">
        <h3>Ai đã nhận mã</h3>
        <form method="get" action="giftcode.php" class="ngang">
            <input type="hidden" name="tab" value="log">
            <label class="o"><span>Mã</span>
                <input type="text" name="ma" value="<?= e($logMa) ?>" placeholder="để trống = tất cả">
            </label>
            <label class="o"><span>Tên nhân vật</span>
                <input type="text" name="player" value="<?= e($logPlayer) ?>" placeholder="để trống = tất cả">
            </label>
            <button class="btn btn-primary btn-nho">Lọc</button>
            <?php if ($logMa !== '' || $logPlayer !== ''): ?>
                <a href="giftcode.php?tab=log" class="btn btn-xam btn-nho">Bỏ lọc</a>
            <?php endif; ?>
        </form>

        <div class="tbl-wrap" style="margin-top:14px;">
        <table class="tbl">
            <thead><tr>
                <th class="num">ID</th><th>Mã</th><th>Nhân vật</th>
                <th class="num">Xu</th><th class="num">Lượng</th><th class="num">Lượng khoá</th>
                <th>Phần thưởng</th><th></th>
            </tr></thead>
            <tbody>
            <?php foreach ($logRows as $r): ?>
                <tr>
                    <td class="num"><?= (int)$r['id'] ?></td>
                    <td><a href="giftcode.php?tab=log&amp;ma=<?= e(urlencode((string)$r['giftcode'])) ?>"><?= e((string)$r['giftcode']) ?></a></td>
                    <td><a href="nhanvat.php?q=<?= e(urlencode((string)$r['player'])) ?>"><?= e((string)$r['player']) ?></a></td>
                    <td class="num"><?= so($r['xu']) ?></td>
                    <td class="num"><?= so($r['luong']) ?></td>
                    <td class="num"><?= so($r['luongK']) ?></td>
                    <td class="info"><?= e((string)($r['item'] ?? '')) ?></td>
                    <td class="nowrap">
                        <form method="post" action="giftcode.php" class="inline"
                              onsubmit="return confirm('Cho <?= e((string)$r['player']) ?> nhận lại mã <?= e((string)$r['giftcode']) ?>?');">
                            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                            <input type="hidden" name="viec" value="cho_nhan_lai">
                            <input type="hidden" name="ma" value="<?= e((string)$r['giftcode']) ?>">
                            <input type="hidden" name="player" value="<?= e((string)$r['player']) ?>">
                            <button class="btn btn-xam btn-nho">Cho nhận lại</button>
                        </form>
                    </td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$logRows): ?>
                <tr><td colspan="8" class="muted">Chưa có ai nhận mã nào khớp bộ lọc.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>

        <div class="phantrang">
            <?php $qs = 'tab=log&ma=' . urlencode($logMa) . '&player=' . urlencode($logPlayer); ?>
            <?php if ($logTrang > 1): ?>
                <a class="btn btn-xam btn-nho" href="giftcode.php?<?= e($qs) ?>&amp;trang=<?= $logTrang - 1 ?>">← Trang trước</a>
            <?php endif; ?>
            <span>Trang <?= $logTrang ?></span>
            <?php if ($conTrangSau): ?>
                <a class="btn btn-xam btn-nho" href="giftcode.php?<?= e($qs) ?>&amp;trang=<?= $logTrang + 1 ?>">Trang sau →</a>
            <?php endif; ?>
        </div>

        <p class="muted small" style="margin-top:10px;">
            “Cho nhận lại” xoá dòng trong <code>giftcode_log</code> — đúng thứ server dùng để chặn nhận
            trùng. Nó <strong>không</strong> cộng lại lượt cho mã.
        </p>
    </div>

<?php endif; ?>
<?php adminFoot(); ?>
