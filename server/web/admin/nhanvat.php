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
 *
 * -----------------------------------------------------------------------
 *  ĐẶT CẤP ĐỘ — RỦI RO KHÁC HẲN KHOÁ NHÂN VẬT, ĐỌC KỸ TRƯỚC KHI SỬA
 * -----------------------------------------------------------------------
 * Khoá nhân vật AN TOÀN khi người chơi đang online vì cột `ban` không nằm
 * trong câu UPDATE lưu nhân vật định kỳ (DatabaseNhanVat.java) — server không
 * bao giờ ghi đè lại cột đó, nên khoá "chỉ ăn từ lần vào sau" đúng nghĩa: chắc
 * chắn ăn, chỉ là chưa ngay lập tức.
 *
 * ĐẶT CẤP ĐỘ THÌ KHÁC HẲN. `lastLv` và `xp` là hai cột nằm NGAY TRONG câu lưu
 * định kỳ đó. Nhân vật đang online giữ một bản `lastLv` cũ trong RAM của tiến
 * trình game; lần server tự lưu tiếp theo (định kỳ, hoặc lúc thoát) sẽ ghi
 * NGUYÊN bản cũ đó đè lên đúng dòng mình vừa sửa — không phải "chậm có hiệu
 * lực" mà là MẤT HẲN thay đổi mà không có cảnh báo nào. Vì trang web không có
 * cách nào biết một nhân vật có đang online hay không (CharManager chỉ giữ
 * trong RAM của tiến trình Java, không ghi ra CSDL), không thể tự động chặn
 * việc này — chỉ có thể cảnh báo rõ và để người vận hành tự xác nhận nhân vật
 * đang offline trước khi bấm.
 *
 * MOI CHI SO KHAC KHONG CAN DONG TAY. `Char.strength/agitity/spirit/health/
 * luck`, điểm kỹ năng, điểm cơ bản... đều được máy chủ TỰ TÍNH LẠI từ `lastLv`
 * ngay lúc chọn nhân vật (SelectCharHandler.java:206-215, rồi gọi
 * p.calculateAttrib()) — giống hệt một người chơi lên cấp tự nhiên. Đặt `xp`
 * đúng bằng mốc "vừa lên cấp" (capDoRaExp(), sinh từ real.LevelDetail.expMain)
 * chỉ để thanh kinh nghiệm không hiện số âm hay tỉ lệ sai, không ảnh hưởng gì
 * đến chỉ số chiến đấu.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/vatpham.php';
require_once __DIR__ . '/inc/capdo.php';
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

    if ($viec === 'dat_cap_do') {
        $capDoMoi = (int)p('cap_do');
        if ($capDoMoi < 1 || $capDoMoi > CAPDO_TOI_DA) {
            veLai($ve, 'error', 'Cấp độ phải từ 1 đến ' . CAPDO_TOI_DA . '.');
        }
        $bangExp = capDoRaExp();
        $expMoi  = $bangExp[$capDoMoi - 1];

        $stCu = dbGame()->prepare('SELECT lastLv, xp FROM tob_char WHERE id = ? LIMIT 1');
        $stCu->execute([$id]);
        $cu = $stCu->fetch();

        $up = dbGame()->prepare('UPDATE tob_char SET lastLv = ?, xp = ? WHERE id = ?');
        $up->execute([$capDoMoi, $expMoi, $id]);

        ghiNhatKy('dat_cap_do_nhan_vat', $ten,
            'lastLv: ' . (int)($cu['lastLv'] ?? '?') . ' -> ' . $capDoMoi
            . ' | xp: ' . (string)($cu['xp'] ?? '?') . ' -> ' . $expMoi);

        veLai($ve, 'success', 'Đã đặt cấp độ ' . $ten . ' thành ' . $capDoMoi
            . '. CHỈ CÓ HIỆU LỰC THẬT nếu nhân vật đang offline lúc bấm — nếu đang '
            . 'online, lần server tự lưu tiếp theo sẽ ghi đè lại cấp cũ mà không báo gì.');
    }

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
$vpRow  = [];
$loiVatPham = '';

$COT = 'id, charname, userId, lastLv, `class`, xp, gold, luong, luonglock, ban, del,
        lastLog, idserver, idClan, nhack, blacklist, tichluy, topNap';

/**
 * Nạp chi tiết một nhân vật, kèm ba cột vật phẩm.
 *
 * Trả về [$ct, $chuTk, $vpRow, $loiVatPham]; $ct = null nếu không có nhân vật đó.
 * Tách thành hàm riêng vì có hai lối vào: mở thẳng bằng ?id=, và tìm ra đúng một
 * nhân vật bằng ?q=.
 */
function docChiTiet(int $id, string $cot): array
{
    $st = dbGame()->prepare("SELECT $cot FROM tob_char WHERE id = ? LIMIT 1");
    $st->execute([$id]);
    $ct = $st->fetch() ?: null;
    if (!$ct) {
        return [null, '', [], ''];
    }
    // Ba cột này là TEXT dài (cả hành trang serialize vào đó) nên chỉ lấy ở trang chi
    // tiết một nhân vật, KHÔNG thêm vào $COT — $COT còn dùng cho câu liệt kê 50 dòng.
    try {
        $stV = dbGame()->prepare('SELECT equip, inven, bag FROM tob_char WHERE id = ? LIMIT 1');
        $stV->execute([$id]);
        return [$ct, chuNhan((int)$ct['userId']), $stV->fetch() ?: [], ''];
    } catch (Throwable $ex) {
        return [$ct, chuNhan((int)$ct['userId']), [], $ex->getMessage()];
    }
}

try {
    if ($xemId > 0) {
        list($ct, $chuTk, $vpRow, $loiVatPham) = docChiTiet($xemId, $COT);
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

        // Tìm ra đúng một nhân vật thì mở luôn trang chi tiết. Thiếu bước này thì bấm
        // tên nhân vật ở trang Log (liên kết dạng ?q=ten) sẽ dừng lại ở một bảng đúng
        // một dòng — không có phần vật phẩm — và người xem tưởng trang không hề có.
        if (count($ketQua) === 1) {
            list($ct, $chuTk, $vpRow, $loiVatPham) = docChiTiet((int)$ketQua[0]['id'], $COT);
        }
    } else {
        $ketQua = dbGame()->query("SELECT $COT FROM tob_char ORDER BY lastLog DESC LIMIT 30")->fetchAll();
    }
} catch (Throwable $ex) {
    $loiDoc = $ex->getMessage();
}

// ---- Vật phẩm của nhân vật đang xem ----
$vpMac = docChuoiVatPham($vpRow['equip'] ?? null);
$vpTach = tachTuiVaThoiTrang($vpRow['inven'] ?? null);
$vpTui = $vpTach['tui'];
$vpThoiTrang = $vpTach['thoi_trang'];
$vpRuong = docChuoiVatPham($vpRow['bag'] ?? null);

$tenVP = [];
if ($ct) {
    $moiId = [];
    foreach ([$vpMac, $vpTui, $vpThoiTrang, $vpRuong] as $nhom) {
        foreach ($nhom as $v) {
            $moiId[] = (int)$v['template'];
        }
    }
    try {
        $tenVP = tenVatPhamTheoId(dbGame(), $moiId);
    } catch (Throwable $ex) {
        $tenVP = [];
    }
}

/** Một bảng vật phẩm. */
function bangVatPham(array $ds, array $ten, string $trong): void
{
    if (!$ds) {
        echo '<p class="muted small">' . e($trong) . '</p>';
        return;
    }
    echo '<div class="tbl-wrap"><table class="tbl"><thead><tr>'
       . '<th>Vật phẩm</th><th class="num">+</th><th>Màu</th><th>Ghi chú</th>'
       . '</tr></thead><tbody>';
    foreach ($ds as $v) {
        $id = (int)$v['template'];
        $ghi = [];
        if ($v['khoa']) {
            $ghi[] = 'khoá';
        }
        if ((int)$v['tinh_luyen'] > 0) {
            $ghi[] = 'tinh luyện ' . (int)$v['tinh_luyen'] . '%';
        }
        if ((int)$v['thue_phut'] > 0) {
            $ghi[] = 'có hạn dùng';
        }
        if ((int)$v['level'] > 0) {
            $ghi[] = 'cấp ' . (int)$v['level'];
        }
        printf(
            '<tr><td>%s <span class="muted small">#%d</span></td>'
            . '<td class="num">%s</td><td>%s</td><td class="muted small">%s</td></tr>',
            e($ten[$id] ?? '(không có trong data_item)'),
            $id,
            (int)$v['plus'] > 0 ? '+' . (int)$v['plus'] : '',
            e(tenMauVatPham((int)$v['mau'])),
            e(implode(' · ', $ghi))
        );
    }
    echo '</tbody></table></div>';
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
            <input type="text" name="q" value="<?= e($q) ?>" placeholder="ví dụ: tennhanvat">
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

            <h4 style="margin:18px 0 10px;">Đặt cấp độ</h4>
            <form method="post" action="nhanvat.php" class="ngang"
                  onsubmit="return confirm('Đặt cấp độ ' + document.getElementById('capDoMoi').value + ' cho <?= e($ct['charname']) ?>?\n\nCHỈ làm việc này khi chắc chắn nhân vật đang OFFLINE — xem cảnh báo bên dưới.');">
                <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                <input type="hidden" name="viec" value="dat_cap_do">
                <input type="hidden" name="id" value="<?= (int)$ct['id'] ?>">
                <label class="o" style="max-width:140px;">
                    <span>Cấp độ mới (hiện tại: <?= (int)$ct['lastLv'] ?>)</span>
                    <input type="number" id="capDoMoi" name="cap_do" min="1" max="<?= CAPDO_TOI_DA ?>"
                           value="<?= (int)$ct['lastLv'] ?>" required>
                </label>
                <button class="btn btn-do btn-nho" style="align-self:flex-end;">Đặt cấp độ</button>
            </form>
            <div class="luuy" style="margin-top:12px;">
                <p><strong>Chỉ làm khi nhân vật đang offline.</strong> Trang web không có cách nào
                   biết một nhân vật có đang online hay không. Nếu đang online, lần server tự lưu
                   tiếp theo (định kỳ, hoặc lúc người chơi thoát) sẽ ghi đè lại đúng cấp cũ đang có
                   trong bộ nhớ của tiến trình game — <strong>không phải chậm có hiệu lực như khoá
                   nhân vật, mà là MẤT HẲN thay đổi mà không có cảnh báo nào.</strong></p>
                <p class="small">Cách chắc chắn: nhờ người chơi thoát game trước, hoặc đặt lại lần
                   nữa sau khi chắc chắn họ đã ngắt kết nối. Mọi chỉ số khác (sức mạnh, HP, điểm kỹ
                   năng…) được máy chủ tự tính lại từ cấp độ ngay lúc chọn nhân vật — không cần và
                   không nên đụng vào cột nào khác.</p>
            </div>
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

    <?php if ($loiVatPham !== ''): ?>
        <div class="flash flash-error">Không đọc được vật phẩm: <?= e($loiVatPham) ?></div>
    <?php endif; ?>

    <div class="card">
        <h3>Vật phẩm</h3>
        <p class="muted small" style="margin:0 0 14px;">
            Máy chủ này <strong>không có bảng <code>tob_item</code></strong> — trang bị, hành trang
            và rương được ghi thẳng vào ba cột <code>equip</code>, <code>inven</code>,
            <code>bag</code> của <code>tob_char</code>. Đây là ảnh chụp <em>lần lưu gần nhất</em>:
            người chơi đang online thì đồ thật nằm trong RAM của server, chỉ khớp sau khi họ
            thoát hoặc server tự lưu.
        </p>

        <div class="grid-2">
            <div>
                <h4>Đang mặc <span class="muted small">(<?= count($vpMac) ?>)</span></h4>
                <?php bangVatPham($vpMac, $tenVP, 'Không mặc gì.'); ?>
            </div>
            <div>
                <h4>Thời trang <span class="muted small">(<?= count($vpThoiTrang) ?>)</span></h4>
                <?php bangVatPham($vpThoiTrang, $tenVP, 'Không có.'); ?>
            </div>
        </div>

        <h4 style="margin-top:18px;">Hành trang <span class="muted small">(<?= count($vpTui) ?>)</span></h4>
        <?php bangVatPham($vpTui, $tenVP, 'Túi rỗng.'); ?>

        <h4 style="margin-top:18px;">Rương <span class="muted small">(<?= count($vpRuong) ?>)</span></h4>
        <?php bangVatPham($vpRuong, $tenVP, 'Rương rỗng.'); ?>

        <p class="muted small" style="margin:14px 0 0;">
            Đá quý và nguyên liệu <em>không</em> nằm trong ba cột này nên không hiện ở đây —
            chúng ở bảng riêng.
        </p>
    </div>

<?php else: ?>

    <div class="card">
        <h3><?= $q === '' ? '30 nhân vật vào game gần nhất' : 'Kết quả cho “' . e($q) . '”' ?></h3>
        <p class="muted small" style="margin:0 0 12px;">
            Bấm <strong>tên nhân vật</strong> để mở trang chi tiết — ở đó mới có
            <strong>đồ đang mặc, thời trang, hành trang và rương</strong> của nhân vật đó.
            Bảng này chỉ liệt kê, không kèm vật phẩm.
        </p>
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
