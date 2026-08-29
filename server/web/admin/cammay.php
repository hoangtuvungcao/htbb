<?php
/**
 * Cấm đăng nhập theo MÃ MÁY (device id).
 *
 * VÌ SAO CẦN TRANG NÀY: cấm tài khoản thì kẻ phá lập tài khoản mới trong 30 giây. Mã máy
 * đi theo thiết bị nên bám dai hơn — và bảng `tob_may_dang_nhap` cho thấy *một máy đã
 * dùng những tài khoản nào*, tức là truy được cả ổ chứ không chỉ cái tài khoản vừa lộ.
 *
 * MÃ MÁY LẤY TỪ ĐÂU: client Unity gửi `SystemInfo.deviceUniqueIdentifier`
 * (Client/Canvas.cs:157), server đọc ở `LoginHandler` rồi ghi qua `data/CamTheoMay.java`.
 * KHÔNG phải địa chỉ MAC — từ Android 10 / iOS 7 ứng dụng không đọc được MAC thật nữa,
 * và máy chủ thì không bao giờ nhìn thấy MAC của người kết nối (nó bị ghi đè ở mỗi chặng
 * router).
 *
 * BA GIỚI HẠN, nói trước để không kỳ vọng sai:
 *   1. Giá trị do CLIENT tự khai → nằm trong tay người dùng. Chặn được người phá lười
 *      (đa số), không chặn được người biết sửa client.
 *   2. Cài lại ứng dụng có thể sinh mã mới → người bị cấm thoát ra được.
 *   3. Máy dùng chung (quán net, máy trong nhà) thì cấm một người là chặn cả nhóm.
 * Và hoàn toàn vô dụng với DDoS: kẻ tấn công không đăng nhập, chỉ mở socket.
 *
 * LỆNH CẤM CÓ HIỆU LỰC CHẬM NHẤT 60 GIÂY: server giữ danh sách cấm trong RAM và làm mới
 * mỗi phút (CamTheoMay.CHU_KY_LAM_MOI_MS). Cố ý như vậy — tra database mỗi lần ai đó
 * đăng nhập thì một trận dò mật khẩu sẽ thành một trận dội database.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$TRANG = 'cammay.php';

/** Tên bảng — phải khớp hằng số trong src/data/CamTheoMay.java. */
const BANG_CAM = 'tob_cam_may';
const BANG_LICH_SU = 'tob_may_dang_nhap';

/**
 * Máy này có tài khoản quản trị nào từng dùng không.
 *
 * Cấm nhầm một máy mà chính bạn đang ngồi thì mất luôn đường vào game — và trang web
 * không gỡ hộ được vì server chỉ đọc lại danh sách sau 60 giây. Chặn ngay tại đây.
 *
 * @return string tên tài khoản admin đầu tiên tìm thấy, '' nếu không có
 */
function adminTungDungMay(string $maMay): string
{
    try {
        $st = dbGame()->prepare('SELECT username FROM ' . BANG_LICH_SU . ' WHERE ma_may = ?');
        $st->execute([$maMay]);
        foreach ($st->fetchAll(PDO::FETCH_COLUMN) as $u) {
            if (laTaiKhoanAdmin((string)$u)) {
                return (string)$u;
            }
        }
    } catch (Throwable $ex) {
        // Bảng chưa có thì coi như không biết — vẫn chặn ở dưới bằng thông báo riêng.
    }
    return '';
}

/**
 * Cắt chuỗi về tối đa $soByte byte mà không để lại ký tự UTF-8 dở dang.
 *
 * KHÔNG DÙNG mb_substr: máy chủ **không cài mbstring** (không có `mbstring.ini`, không có
 * `mbstring.so`) nên gọi hàm `mb_*` là lỗi nghiêm trọng — trắng trang, không thông báo gì.
 * Cả dự án không nơi nào dùng `mb_*`, nên chuyện này chưa từng lộ ra.
 *
 * Cắt theo BYTE vẫn an toàn với cột `VARCHAR(255)` utf8mb4: 255 byte luôn nhỏ hơn hoặc
 * bằng 255 ký tự. Vòng lặp cuối bỏ nốt phần đuôi của một ký tự nhiều byte bị chặt đôi —
 * `preg_match('//u', $s)` trả về false khi $s không phải UTF-8 hợp lệ.
 */
function catBot(string $s, int $soByte): string
{
    if (strlen($s) <= $soByte) {
        return $s;
    }
    $s = substr($s, 0, $soByte);
    while ($s !== '' && preg_match('//u', $s) !== 1) {
        $s = substr($s, 0, -1);
    }
    return $s;
}

/** Mã máy hợp lệ hay không — cùng luật với CamTheoMay.lamSach() bên Java. */
function maMayHopLe(string $s): bool
{
    return $s !== '' && strlen($s) <= 64 && preg_match('/^[A-Za-z0-9_-]+$/', $s) === 1;
}

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $viec  = p('viec');
    $maMay = trim(p('ma_may'));
    $ve    = $TRANG . '?q=' . urlencode($maMay);

    if (!maMayHopLe($maMay)) {
        veLai($TRANG, 'error', 'Mã máy không hợp lệ.');
    }
    if ($viec !== 'cam' && $viec !== 'bo_cam') {
        veLai($TRANG, 'error', 'Không hiểu thao tác.');
    }

    try {
        if ($viec === 'cam') {
            $chuAdmin = adminTungDungMay($maMay);
            if ($chuAdmin !== '') {
                veLai($ve, 'error', 'Máy này đã từng đăng nhập tài khoản quản trị ('
                    . $chuAdmin . ') nên không cấm được — cấm nhầm là bạn tự khoá mình.');
            }
            $lyDo = catBot(trim(p('ly_do')), 255);
            $st = dbGame()->prepare('INSERT INTO ' . BANG_CAM . ' (ma_may, ly_do, boi_ai)
                                     VALUES (?, ?, ?)
                                     ON DUPLICATE KEY UPDATE ly_do = VALUES(ly_do), boi_ai = VALUES(boi_ai)');
            $st->execute([$maMay, $lyDo, adminUser()['username']]);
            ghiNhatKy('cam_may', $maMay, $lyDo);
            veLai($ve, 'success', 'Đã cấm máy ' . $maMay . '. Có hiệu lực trong vòng 60 giây.');
        }

        $st = dbGame()->prepare('DELETE FROM ' . BANG_CAM . ' WHERE ma_may = ?');
        $st->execute([$maMay]);
        ghiNhatKy('bo_cam_may', $maMay, '');
        veLai($ve, 'success', 'Đã bỏ cấm máy ' . $maMay . '. Có hiệu lực trong vòng 60 giây.');
    } catch (Throwable $ex) {
        veLai($TRANG, 'error', 'Không thao tác được: ' . $ex->getMessage());
    }
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu
// ---------------------------------------------------------------------------
$q = trim(g('q'));
$coBang = true;
$loiDoc = '';

$dsCam = [];
$mayNhieuTk = [];
$tkCuaMay = [];
$mayCuaTk = [];
$dangBiCam = false;
$tong = ['may' => 0, 'cam' => 0, 'nhieu_tk' => 0];

try {
    $dsCam = dbGame()->query('SELECT ma_may, ly_do, boi_ai, luc FROM ' . BANG_CAM
        . ' ORDER BY luc DESC LIMIT 200')->fetchAll();

    $tong['may'] = (int)dbGame()->query('SELECT COUNT(DISTINCT ma_may) FROM ' . BANG_LICH_SU)->fetchColumn();
    $tong['cam'] = count($dsCam);
    $tong['nhieu_tk'] = (int)dbGame()->query(
        'SELECT COUNT(*) FROM (SELECT ma_may FROM ' . BANG_LICH_SU
        . ' GROUP BY ma_may HAVING COUNT(*) >= 5) t')->fetchColumn();

    // Máy dùng nhiều tài khoản nhất — đây là chỗ kẻ lập hàng loạt tài khoản lộ ra.
    $mayNhieuTk = dbGame()->query(
        'SELECT ma_may, COUNT(*) so_tk, MAX(lan_cuoi) gan_nhat, MAX(ip) ip
         FROM ' . BANG_LICH_SU . '
         GROUP BY ma_may
         HAVING so_tk >= 2
         ORDER BY so_tk DESC, gan_nhat DESC
         LIMIT 50')->fetchAll();

    if ($q !== '') {
        // Một ô tìm cho cả hai chiều: gõ mã máy ra danh sách tài khoản, gõ tên tài khoản
        // ra danh sách máy. Không bắt người dùng phải biết mình đang gõ loại nào.
        $st = dbGame()->prepare('SELECT username, ip, so_lan, lan_dau, lan_cuoi
                                 FROM ' . BANG_LICH_SU . ' WHERE ma_may = ?
                                 ORDER BY lan_cuoi DESC LIMIT 200');
        $st->execute([$q]);
        $tkCuaMay = $st->fetchAll();

        $st = dbGame()->prepare('SELECT ma_may, ip, so_lan, lan_dau, lan_cuoi
                                 FROM ' . BANG_LICH_SU . ' WHERE username = ?
                                 ORDER BY lan_cuoi DESC LIMIT 200');
        $st->execute([$q]);
        $mayCuaTk = $st->fetchAll();

        $st = dbGame()->prepare('SELECT 1 FROM ' . BANG_CAM . ' WHERE ma_may = ? LIMIT 1');
        $st->execute([$q]);
        $dangBiCam = (bool)$st->fetchColumn();
    }
} catch (Throwable $ex) {
    $coBang = false;
    $loiDoc = $ex->getMessage();
}

/** Nút cấm / bỏ cấm cho một mã máy. */
function nutCam(string $maMay, bool $dangCam): void
{
    ?>
    <form method="post" action="cammay.php" class="inline"
          onsubmit="return confirm('<?= $dangCam ? 'Bỏ cấm' : 'Cấm' ?> máy <?= e($maMay) ?>?');">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="ma_may" value="<?= e($maMay) ?>">
        <input type="hidden" name="viec" value="<?= $dangCam ? 'bo_cam' : 'cam' ?>">
        <?php if (!$dangCam): ?>
            <input type="hidden" name="ly_do" value="cấm từ danh sách">
        <?php endif; ?>
        <button class="btn <?= $dangCam ? 'btn-xam' : 'btn-do' ?> btn-nho">
            <?= $dangCam ? 'Bỏ cấm' : 'Cấm' ?>
        </button>
    </form>
    <?php
}

adminHead('cammay', 'Cấm theo máy');
?>
<div class="page-head">
    <div>
        <h1>Cấm theo máy</h1>
        <p>Cấm theo <strong>mã máy</strong> thay vì theo tài khoản. Gõ mã máy để xem nó đã
           dùng những tài khoản nào, hoặc gõ tên tài khoản để xem nó đăng nhập từ những máy nào.</p>
    </div>
    <form method="get" action="cammay.php" class="ngang">
        <label class="o" style="min-width:280px;"><span>Mã máy hoặc tên tài khoản</span>
            <input type="text" name="q" value="<?= e($q) ?>" placeholder="dán mã máy, hoặc gõ tên tài khoản">
        </label>
        <button class="btn btn-primary btn-nho">Tìm</button>
        <?php if ($q !== ''): ?>
            <a class="btn btn-xam btn-nho" href="cammay.php">Bỏ lọc</a>
        <?php endif; ?>
    </form>
</div>

<?php if (!$coBang): ?>
    <div class="luuy">
        <p><strong>Chưa có bảng dữ liệu.</strong> Chạy một lần trên máy chủ:</p>
        <p><code>mysql -u root kpah_db &lt; sql/them_bang_cam_may.sql</code></p>
        <p class="small">Lỗi đọc được: <?= e($loiDoc) ?></p>
    </div>
<?php else: ?>

<div class="stat-grid">
    <?php
    theSo('Máy đã ghi nhận', so($tong['may']));
    theSo('Đang bị cấm', so($tong['cam']), '', $tong['cam'] > 0 ? 'nguy-hiem' : '');
    theSo('Máy dùng ≥ 5 tài khoản', so($tong['nhieu_tk']), 'đáng soi', $tong['nhieu_tk'] > 0 ? 'canh-bao' : '');
    ?>
</div>

<?php if ($tong['may'] === 0): ?>
    <div class="luuy">
        <p><strong>Bảng đã có nhưng chưa ghi nhận máy nào.</strong> Hai nguyên nhân thường gặp:</p>
        <p>1. Máy chủ chưa chạy bản jar có <code>CamTheoMay</code> — kiểm bằng:
           <code>jar tf KPAH2_patched.jar | grep CamTheoMay</code></p>
        <p>2. <strong>Client chưa gửi mã máy</strong> — đây là nguyên nhân gần như chắc chắn.
           Client hiện chỉ gửi mã máy kèm gói <em>đăng ký</em>, mà đăng ký đã chuyển sang web
           và đường đó đã bị đóng. Server đọc mã máy ở <em>trường cuối cùng</em> của gói
           đăng nhập; client cũ không gửi thì bỏ qua (cố ý — để không ép ai cập nhật cùng lúc).
           Cần thêm một dòng vào cuối gói đăng nhập bên client rồi build lại:
           <code>message.writer().writeUTF(Canvas.IMEI);</code></p>
    </div>
<?php endif; ?>

<?php if ($q !== ''): ?>
    <div class="card">
        <h3>Kết quả cho <code><?= e($q) ?></code>
            <?php if ($dangBiCam): ?><span class="nhan nhan-khoa">Đang bị cấm</span><?php endif; ?>
        </h3>

        <?php if ($tkCuaMay): ?>
            <h4>Máy này đã dùng <?= count($tkCuaMay) ?> tài khoản</h4>
            <div class="form-actions" style="margin-top:0;margin-bottom:12px;">
                <?php nutCam($q, $dangBiCam); ?>
            </div>
            <div class="tbl-wrap">
            <table class="tbl">
                <thead><tr><th>Tài khoản</th><th>IP</th><th class="num">Số lần</th>
                    <th>Lần đầu</th><th>Lần cuối</th></tr></thead>
                <tbody>
                <?php foreach ($tkCuaMay as $r): ?>
                    <tr>
                        <td><a href="taikhoan.php?q=<?= e(urlencode((string)$r['username'])) ?>"><?= e((string)$r['username']) ?></a></td>
                        <td class="nowrap muted small"><?= e((string)$r['ip']) ?></td>
                        <td class="num"><?= so($r['so_lan']) ?></td>
                        <td class="nowrap muted small"><?= e((string)$r['lan_dau']) ?></td>
                        <td class="nowrap muted small"><?= e((string)$r['lan_cuoi']) ?></td>
                    </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            </div>
        <?php endif; ?>

        <?php if ($mayCuaTk): ?>
            <h4 style="margin-top:18px;">Tài khoản này đăng nhập từ <?= count($mayCuaTk) ?> máy</h4>
            <div class="tbl-wrap">
            <table class="tbl">
                <thead><tr><th>Mã máy</th><th>IP</th><th class="num">Số lần</th>
                    <th>Lần cuối</th><th></th></tr></thead>
                <tbody>
                <?php foreach ($mayCuaTk as $r): ?>
                    <tr>
                        <td><a href="cammay.php?q=<?= e(urlencode((string)$r['ma_may'])) ?>"><code><?= e((string)$r['ma_may']) ?></code></a></td>
                        <td class="nowrap muted small"><?= e((string)$r['ip']) ?></td>
                        <td class="num"><?= so($r['so_lan']) ?></td>
                        <td class="nowrap muted small"><?= e((string)$r['lan_cuoi']) ?></td>
                        <td class="nowrap"><?php nutCam((string)$r['ma_may'], false); ?></td>
                    </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            </div>
        <?php endif; ?>

        <?php if (!$tkCuaMay && !$mayCuaTk): ?>
            <p class="muted">Không có máy hay tài khoản nào khớp <code><?= e($q) ?></code>.</p>
        <?php endif; ?>
    </div>
<?php endif; ?>

<div class="grid-2" style="margin-top:16px;">
    <div class="card">
        <h3>Máy dùng nhiều tài khoản nhất</h3>
        <p class="muted small" style="margin:0 0 12px;">
            Một máy dùng nhiều tài khoản chưa chắc là xấu — anh em trong nhà chung máy là
            chuyện thường. Nhưng đây là chỗ kẻ lập hàng loạt tài khoản lộ ra.
        </p>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Mã máy</th><th class="num">Tài khoản</th><th>Lần cuối</th><th></th></tr></thead>
            <tbody>
            <?php foreach ($mayNhieuTk as $r): ?>
                <tr>
                    <td><a href="cammay.php?q=<?= e(urlencode((string)$r['ma_may'])) ?>"><code><?= e((string)$r['ma_may']) ?></code></a></td>
                    <td class="num"><?= so($r['so_tk']) ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['gan_nhat']) ?></td>
                    <td class="nowrap"><?php nutCam((string)$r['ma_may'], false); ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$mayNhieuTk): ?>
                <tr><td colspan="4" class="muted">Chưa có máy nào dùng từ 2 tài khoản trở lên.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>

    <div class="card">
        <h3>Đang bị cấm</h3>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Mã máy</th><th>Lý do</th><th>Bởi</th><th>Lúc</th><th></th></tr></thead>
            <tbody>
            <?php foreach ($dsCam as $r): ?>
                <tr>
                    <td><a href="cammay.php?q=<?= e(urlencode((string)$r['ma_may'])) ?>"><code><?= e((string)$r['ma_may']) ?></code></a></td>
                    <td class="info"><?= e((string)$r['ly_do']) ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['boi_ai']) ?></td>
                    <td class="nowrap muted small"><?= e((string)$r['luc']) ?></td>
                    <td class="nowrap"><?php nutCam((string)$r['ma_may'], true); ?></td>
                </tr>
            <?php endforeach; ?>
            <?php if (!$dsCam): ?>
                <tr><td colspan="5" class="muted">Chưa cấm máy nào.</td></tr>
            <?php endif; ?>
            </tbody>
        </table>
        </div>
    </div>
</div>

<div class="luuy" style="margin-top:16px;">
    <p><strong>Đọc trước khi cấm.</strong> Mã máy do <em>client tự khai</em>, nên nằm trong
       tay người dùng: chặn được người phá lười (đa số), không chặn được người biết sửa client.
       Cài lại ứng dụng có thể sinh mã mới. Và máy dùng chung — quán net, máy trong nhà — thì
       cấm một người là chặn cả nhóm.</p>
    <p>Lệnh cấm có hiệu lực <strong>chậm nhất 60 giây</strong>: máy chủ giữ danh sách trong
       RAM và làm mới mỗi phút, cố ý như vậy để một trận dò mật khẩu không thành một trận
       dội database.</p>
    <p>Máy nào đã từng đăng nhập tài khoản quản trị thì <strong>không cấm được</strong> —
       chặn sẵn để bạn không tự khoá mình.</p>
</div>

<?php endif; ?>
<?php adminFoot(); ?>
