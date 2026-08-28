<?php
/**
 * Xem các bảng log mà game server tự ghi trong kpah_db.
 *
 * Tên bảng và tên cột ở đây đều là hằng số viết sẵn trong mảng $BANG, không bao
 * giờ lấy từ người dùng — nên ghép thẳng vào câu SQL là an toàn. Riêng giá trị
 * lọc thì luôn đi qua tham số.
 *
 * Phân trang cố ý KHÔNG đếm tổng số dòng: mấy bảng này có thể lên hàng triệu
 * dòng và một câu COUNT(*) kèm điều kiện sẽ treo trang. Thay vào đó lấy dư một
 * dòng để biết còn trang sau hay không.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$BANG = [
    'tob_log_all_item' => [
        'ten'  => 'Vật phẩm',
        'mota' => 'Mọi thứ liên quan tới vật phẩm: nhặt, tạo, mất, lỗi lưu hành trang.',
        'cot'  => ['id' => 'ID', 'charname' => 'Nhân vật', 'aclog' => 'Loại',
                   'info' => 'Chi tiết', 'charname2' => 'Liên quan', 'timelog' => 'Lúc'],
        'loc'  => 'charname', 'loc2' => 'aclog', 'sap' => 'id DESC',
    ],
    'tob_log_all_gem' => [
        'ten'  => 'Ngọc',
        'mota' => 'Khảm, gỡ, mua bán ngọc.',
        'cot'  => ['id' => 'ID', 'charname' => 'Nhân vật', 'aclog' => 'Loại',
                   'info' => 'Chi tiết', 'charname2' => 'Liên quan', 'timelog' => 'Lúc'],
        'loc'  => 'charname', 'loc2' => 'aclog', 'sap' => 'id DESC',
    ],
    'tob_log_buy_shop' => [
        'ten'  => 'Mua ở shop',
        'mota' => 'Giao dịch mua trong cửa hàng NPC.',
        'cot'  => ['id' => 'ID', 'charname' => 'Nhân vật', 'info' => 'Món',
                   'price' => 'Giá', 'money' => 'Trả bằng', 'timeBuy' => 'Lúc'],
        'loc'  => 'charname', 'loc2' => null, 'sap' => 'id DESC',
    ],
    'tob_log_buy_store' => [
        'ten'  => 'Mua ở kho',
        'mota' => 'Giao dịch ở cửa hàng người chơi.',
        'cot'  => ['id' => 'ID', 'charname' => 'Nhân vật', 'info' => 'Chi tiết', 'timeBuy' => 'Lúc'],
        'loc'  => 'charname', 'loc2' => null, 'sap' => 'id DESC',
    ],
    'tob_log_clan' => [
        'ten'  => 'Bang hội',
        'mota' => 'Vào / ra bang, đóng góp, thay đổi chức vụ.',
        'cot'  => ['id' => 'ID', 'charname' => 'Nhân vật', 'actioninfo' => 'Việc',
                   'info' => 'Chi tiết', 'timeLog' => 'Lúc'],
        'loc'  => 'charname', 'loc2' => 'actioninfo', 'sap' => 'id DESC',
    ],
    'tob_log_use_luong' => [
        'ten'  => 'Tiêu lượng theo món',
        'mota' => 'Cộng dồn theo tên món — mỗi món một dòng, không phải theo từng lần mua.',
        'cot'  => ['id' => 'ID', 'name' => 'Món', 'idtemplate' => 'ID mẫu', 'soluong' => 'Số lượng',
                   'solanmua' => 'Lượt mua', 'totalmoney' => 'Tổng tiền', 'moneytype' => 'Loại tiền'],
        'loc'  => 'name', 'loc2' => null, 'sap' => 'totalmoney DESC',
    ],
    'tob_nap_xai_luong' => [
        'ten'  => 'Nạp / xài lượng',
        'mota' => 'Tổng đã nạp và đã tiêu của từng nhân vật (mỗi nhân vật một dòng).',
        'cot'  => ['charname' => 'Nhân vật', 'luongnap' => 'Đã nạp', 'luongxai' => 'Đã xài',
                   'tnap' => 'Nạp lần cuối', 'txai' => 'Xài lần cuối'],
        'loc'  => 'charname', 'loc2' => null, 'sap' => 'luongnap DESC',
    ],
    'tob_log_xoso' => [
        'ten'  => 'Xổ số',
        'mota' => 'Kết quả các lượt quay.',
        'cot'  => ['id' => 'ID', 'win' => 'Trúng', 'timelog' => 'Lúc'],
        'loc'  => null, 'loc2' => null, 'sap' => 'id DESC',
    ],
];

$bang = g('bang');
if (!isset($BANG[$bang])) {
    $bang = 'tob_log_all_item';
}
$cauHinh = $BANG[$bang];

$loc   = g('loc');
$loc2  = g('loc2');
$trang = max(1, (int)g('trang', '1'));
$moi   = 50;

$dong = [];
$conTrangSau = false;
$loiDoc = '';

try {
    $dieuKien = [];
    $thamSo   = [];
    if ($cauHinh['loc'] !== null && $loc !== '') {
        $dieuKien[] = '`' . $cauHinh['loc'] . '` = ?';
        $thamSo[]   = $loc;
    }
    if ($cauHinh['loc2'] !== null && $loc2 !== '') {
        $dieuKien[] = '`' . $cauHinh['loc2'] . '` = ?';
        $thamSo[]   = $loc2;
    }
    $where = $dieuKien ? 'WHERE ' . implode(' AND ', $dieuKien) : '';

    $cotSql = implode(', ', array_map(static fn($c) => '`' . $c . '`', array_keys($cauHinh['cot'])));
    $lay = $moi + 1;
    $bo  = ($trang - 1) * $moi;

    $st = dbGame()->prepare(
        "SELECT $cotSql FROM `$bang` $where ORDER BY {$cauHinh['sap']} LIMIT $lay OFFSET $bo"
    );
    $st->execute($thamSo);
    $dong = $st->fetchAll();

    if (count($dong) > $moi) {
        $conTrangSau = true;
        array_pop($dong);
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

/** Cột nào nên hiện dưới dạng số có dấu chấm ngăn cách. */
function cotSo(string $ten): bool
{
    return in_array($ten, ['price', 'soluong', 'solanmua', 'totalmoney',
                           'luongnap', 'luongxai', 'win', 'idtemplate'], true);
}

adminHead('log', 'Log game');
?>
<div class="page-head">
    <div>
        <h1>Log game</h1>
        <p><?= e($cauHinh['mota']) ?></p>
    </div>
    <form method="get" action="log.php" class="ngang">
        <label class="o"><span>Bảng</span>
            <select name="bang" onchange="this.form.submit()">
                <?php foreach ($BANG as $k => $v): ?>
                    <option value="<?= e($k) ?>" <?= $k === $bang ? 'selected' : '' ?>><?= e($v['ten']) ?></option>
                <?php endforeach; ?>
            </select>
        </label>
        <?php if ($cauHinh['loc'] !== null): ?>
            <label class="o"><span><?= e($cauHinh['cot'][$cauHinh['loc']] ?? 'Lọc') ?></span>
                <input type="text" name="loc" value="<?= e($loc) ?>" placeholder="tên chính xác">
            </label>
        <?php endif; ?>
        <?php if ($cauHinh['loc2'] !== null): ?>
            <label class="o"><span><?= e($cauHinh['cot'][$cauHinh['loc2']] ?? 'Lọc 2') ?></span>
                <input type="text" name="loc2" value="<?= e($loc2) ?>" placeholder="vd: loiinvent">
            </label>
        <?php endif; ?>
        <button class="btn btn-primary btn-nho">Xem</button>
        <?php if ($loc !== '' || $loc2 !== ''): ?>
            <a class="btn btn-xam btn-nho" href="log.php?bang=<?= e(urlencode($bang)) ?>">Bỏ lọc</a>
        <?php endif; ?>
    </form>
</div>

<?php if ($loiDoc !== ''): ?>
    <div class="flash flash-error">
        Không đọc được bảng <code><?= e($bang) ?></code>: <?= e($loiDoc) ?>
        <br><span class="small">Bảng này có thể chưa tồn tại trên máy chủ của bạn — không sao, chọn bảng khác.</span>
    </div>
<?php endif; ?>

<?php if ($bang === 'tob_log_all_item'): ?>
    <div class="luuy">
        <p>Cột <strong>Loại</strong> (<code>aclog</code>) là chỗ đáng soi nhất. Gõ <code>loiinvent</code>
           vào ô lọc để tìm những nhân vật lưu hành trang thất bại — đó là nguyên nhân khiến server
           <em>tự</em> đặt <code>ban = 1</code> và nhân vật biến mất khỏi màn chọn.</p>
    </div>
<?php endif; ?>

<div class="card">
    <h3><?= e($cauHinh['ten']) ?> · <code><?= e($bang) ?></code></h3>
    <div class="tbl-wrap">
    <table class="tbl">
        <thead><tr>
            <?php foreach ($cauHinh['cot'] as $cot => $nhan): ?>
                <th class="<?= cotSo($cot) ? 'num' : '' ?>"><?= e($nhan) ?></th>
            <?php endforeach; ?>
        </tr></thead>
        <tbody>
        <?php foreach ($dong as $r): ?>
            <tr>
                <?php foreach ($cauHinh['cot'] as $cot => $nhan):
                    $gt = (string)($r[$cot] ?? '');
                    if (cotSo($cot)) {
                        echo '<td class="num">' . e(so($gt)) . '</td>';
                    } elseif ($cot === 'charname' || $cot === 'charname2') {
                        echo $gt === '' || $gt === '""'
                            ? '<td class="muted">—</td>'
                            : '<td class="nowrap"><a href="nhanvat.php?q=' . e(urlencode($gt)) . '">' . e($gt) . '</a></td>';
                    } elseif ($cot === 'info') {
                        echo '<td class="info">' . e($gt) . '</td>';
                    } else {
                        echo '<td class="nowrap">' . e($gt) . '</td>';
                    }
                endforeach; ?>
            </tr>
        <?php endforeach; ?>
        <?php if (!$dong && $loiDoc === ''): ?>
            <tr><td colspan="<?= count($cauHinh['cot']) ?>" class="muted">Không có dòng nào khớp.</td></tr>
        <?php endif; ?>
        </tbody>
    </table>
    </div>

    <div class="phantrang">
        <?php $qs = 'bang=' . urlencode($bang) . '&loc=' . urlencode($loc) . '&loc2=' . urlencode($loc2); ?>
        <?php if ($trang > 1): ?>
            <a class="btn btn-xam btn-nho" href="log.php?<?= e($qs) ?>&amp;trang=<?= $trang - 1 ?>">← Trang trước</a>
        <?php endif; ?>
        <span>Trang <?= $trang ?> · <?= count($dong) ?> dòng</span>
        <?php if ($conTrangSau): ?>
            <a class="btn btn-xam btn-nho" href="log.php?<?= e($qs) ?>&amp;trang=<?= $trang + 1 ?>">Trang sau →</a>
        <?php endif; ?>
    </div>
</div>

<p class="muted small">
    Nhật ký nhận giftcode nằm ở trang <a href="giftcode.php?tab=log">Giftcode → Ai đã nhận</a>.
    Thao tác của chính các admin nằm ở <a href="nhatky.php">Nhật ký admin</a>.
</p>
<?php adminFoot(); ?>
