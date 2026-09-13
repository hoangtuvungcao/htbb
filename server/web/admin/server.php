<?php
/**
 * Bật/tắt server game (kpah-game) từ xa, không cần SSH/CLI.
 *
 * VÌ SAO CẦN TRANG NÀY: server chạy bằng systemd (Restart=always) — khi đang
 * SỐNG thì có sẵn cơ chế tự khởi động lại qua cờ trong bảng `5h_systems`
 * (server tự poll mỗi 5 giây, xem Database.baotriCMD() và nút "Khởi động lại
 * ngay" bên dưới). Nhưng khi server đã TẮT HẲN thì không còn gì bên trong
 * Java để mà poll DB nữa — trang này KHÔNG tự bật/tắt được, nó chỉ GHI "trạng
 * thái mong muốn" vào bảng `dieu_khien_server`; một tiến trình canh gác chạy
 * NGOÀI JVM game trên VPS (vps/dieu-khien-server.sh, cài qua cron của ROOT vì
 * kpah-game.service chạy bằng user "kpah" không đủ quyền systemctl) đọc bảng
 * đó mỗi phút rồi tự gọi systemctl thay bạn — kể cả lúc server đang tắt hẳn.
 *
 * Vì vậy trang này CẦN CÀI WATCHER MỘT LẦN trên VPS mới hoạt động; nếu chưa
 * cài, các nút vẫn ghi được vào DB nhưng sẽ không có gì xảy ra — mục "nhịp
 * tim" bên dưới tự phát hiện và nhắc cài.
 *
 * AN TOÀN: bấm Tắt/Bật server làm VĂNG TẤT CẢ người chơi đang online, nên nút
 * Tắt bắt gõ đúng cụm xác nhận (giống reset_toanbo.php) và có hộp thoại xác
 * nhận của trình duyệt.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

const XAC_NHAN_TAT_SERVER = 'TAT SERVER';
/** Watcher coi la "con song" neu bao cao trong khoang nay - cron chay moi 60s. */
const NGUONG_WATCHER_IM_LANG_GIAY = 180;

$TRANG = 'server.php';

/**
 * Đổi giá trị ô <input type="datetime-local"> thành 'Y-m-d H:i:s', hoặc null
 * nếu bỏ trống, false nếu không đọc được — giống hệt sukien.php::docMoc().
 *
 * @return string|null|false
 */
function docMocServer(string $tho)
{
    $tho = trim($tho);
    if ($tho === '') {
        return null;
    }
    $tho = str_replace('T', ' ', $tho);
    if (!preg_match('/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})(?::(\d{2}))?$/', $tho, $k)) {
        return false;
    }
    if (!checkdate((int)$k[2], (int)$k[3], (int)$k[1]) || (int)$k[4] > 23 || (int)$k[5] > 59) {
        return false;
    }
    return sprintf('%s-%s-%s %s:%s:%s', $k[1], $k[2], $k[3], $k[4], $k[5], $k[6] ?? '00');
}

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);
    $hanhDong = p('hanh_dong');

    try {
        if ($hanhDong === 'bat_ngay') {
            $st = dbGame()->prepare(
                'UPDATE dieu_khien_server SET muon_chay=1, gio_hen=NULL, da_ap_dung=0,
                 yeu_cau_boi=?, yeu_cau_luc=NOW() WHERE id=1'
            );
            $st->execute([adminName()]);
            ghiNhatKy('server_bat', 'kpah-game', 'bật ngay');
            veLai($TRANG, 'success', 'Đã ghi yêu cầu BẬT server. Watcher trên VPS sẽ áp dụng trong vòng 1 phút.');
        }

        if ($hanhDong === 'tat_ngay') {
            if (p('xac_nhan') !== XAC_NHAN_TAT_SERVER) {
                veLai($TRANG, 'error', 'Gõ đúng cụm "' . XAC_NHAN_TAT_SERVER . '" để xác nhận.');
            }
            $st = dbGame()->prepare(
                'UPDATE dieu_khien_server SET muon_chay=0, gio_hen=NULL, da_ap_dung=0,
                 yeu_cau_boi=?, yeu_cau_luc=NOW() WHERE id=1'
            );
            $st->execute([adminName()]);
            ghiNhatKy('server_tat', 'kpah-game', 'tắt ngay');
            veLai($TRANG, 'success', 'Đã ghi yêu cầu TẮT server. Watcher trên VPS sẽ áp dụng trong vòng 1 phút — mọi người chơi đang online sẽ bị văng ra.');
        }

        if ($hanhDong === 'hen_gio') {
            $gio = docMocServer(p('gio_hen_dat'));
            if ($gio === false) {
                veLai($TRANG, 'error', 'Giờ hẹn không hợp lệ.');
            }
            if ($gio === null) {
                veLai($TRANG, 'error', 'Chưa chọn giờ hẹn bật.');
            }
            $st = dbGame()->prepare(
                'UPDATE dieu_khien_server SET muon_chay=1, gio_hen=?, da_ap_dung=0,
                 yeu_cau_boi=?, yeu_cau_luc=NOW() WHERE id=1'
            );
            $st->execute([$gio, adminName()]);
            ghiNhatKy('server_hen_gio', 'kpah-game', 'hẹn bật lúc ' . $gio);
            veLai($TRANG, 'success', 'Đã đặt lịch: watcher sẽ tự BẬT server lúc ' . e($gio) . ' (sai lệch tối đa khoảng 1 phút, theo giờ hệ thống VPS).');
        }

        if ($hanhDong === 'huy_lenh') {
            $st = dbGame()->prepare('UPDATE dieu_khien_server SET da_ap_dung=1 WHERE id=1');
            $st->execute();
            ghiNhatKy('server_huy_lenh', 'kpah-game', '');
            veLai($TRANG, 'success', 'Đã huỷ yêu cầu/lịch hẹn đang chờ xử lý.');
        }

        if ($hanhDong === 'restart_ngay') {
            // CƠ CHẾ KHÁC HẲN, đã có sẵn từ trước và CHỈ hoạt động khi server đang
            // sống: cờ trong 5h_systems, server tự đọc mỗi 5 giây rồi tự lưu dữ
            // liệu + thoát, systemd (Restart=always) tự bật lại trong ~10-15 giây.
            // Không cần watcher, không cần server đã tắt hẳn.
            $n = dbGame()->exec("UPDATE 5h_systems SET status = 0 WHERE command = 'restart'");
            if ($n > 0) {
                ghiNhatKy('server_restart_ngay', 'kpah-game', '');
                veLai($TRANG, 'success', 'Đã ghi cờ khởi động lại — server sẽ tự lưu dữ liệu và khởi động lại trong khoảng 10-15 giây (chỉ có tác dụng khi server đang chạy).');
            }
            veLai($TRANG, 'error', 'Không tìm thấy dòng command=\'restart\' trong bảng 5h_systems — kiểm tra lại dữ liệu gốc.');
        }
    } catch (Throwable $ex) {
        veLai($TRANG, 'error', 'Không thao tác được (có thể chưa có bảng dieu_khien_server): ' . $ex->getMessage());
    }
}

// ---------------------------------------------------------------------------
//  Đọc trạng thái
// ---------------------------------------------------------------------------
$coBang = true;
$loiDoc = '';
$tt = null;

try {
    $tt = dbGame()->query(
        "SELECT muon_chay, gio_hen, da_ap_dung, trang_thai_thuc_te, lan_kiem_cuoi,
                yeu_cau_boi, yeu_cau_luc,
                TIMESTAMPDIFF(SECOND, lan_kiem_cuoi, NOW()) AS giay_tu_lan_kiem,
                (gio_hen IS NOT NULL AND gio_hen > NOW()) AS dang_hen_gio
         FROM dieu_khien_server WHERE id=1"
    )->fetch();
} catch (Throwable $ex) {
    $coBang = false;
    $loiDoc = $ex->getMessage();
}

$watcherSong = $tt && $tt['lan_kiem_cuoi'] !== null && (int)$tt['giay_tu_lan_kiem'] <= NGUONG_WATCHER_IM_LANG_GIAY;
$dangCho = $tt && (int)$tt['da_ap_dung'] === 0;

adminHead('server', 'Bật/tắt server');
?>
<div class="page-head">
    <div>
        <h1>Bật/tắt server game</h1>
        <p>Điều khiển service <code>kpah-game</code> trên VPS từ xa, không cần SSH/CLI.</p>
    </div>
</div>

<?php if (!$coBang): ?>
    <div class="luuy">
        <p><strong>Chưa có bảng dữ liệu.</strong> Chạy một lần trên máy chủ:</p>
        <p><code>mysql -u root kpah_db &lt; sql/them_bang_dieu_khien_server.sql</code></p>
        <p class="small">Lỗi đọc được: <?= e($loiDoc) ?></p>
    </div>
<?php else: ?>

<div class="card">
    <h3>Tình trạng hiện tại</h3>
    <dl class="dl">
        <dt>Trạng thái mong muốn</dt>
        <dd>
            <?php if ((int)$tt['muon_chay'] === 1): ?>
                <span class="nhan nhan-ok">Đang chạy</span>
            <?php else: ?>
                <span class="nhan nhan-khoa">Đã tắt</span>
            <?php endif; ?>
        </dd>
        <dt>Trạng thái thực tế (watcher báo cáo)</dt>
        <dd>
            <?php if ($tt['trang_thai_thuc_te'] === null): ?>
                <span class="muted">chưa có báo cáo nào</span>
            <?php elseif ($tt['trang_thai_thuc_te'] === 'active'): ?>
                <span class="nhan nhan-ok">active (đang chạy)</span>
            <?php else: ?>
                <span class="nhan nhan-khoa"><?= e((string)$tt['trang_thai_thuc_te']) ?></span>
            <?php endif; ?>
            <?php if ($tt['lan_kiem_cuoi'] !== null): ?>
                <span class="muted small"> — lúc <?= e((string)$tt['lan_kiem_cuoi']) ?> (<?= so(max(0, (int)$tt['giay_tu_lan_kiem'])) ?>s trước)</span>
            <?php endif; ?>
        </dd>
        <dt>Watcher (canh gác trên VPS)</dt>
        <dd>
            <?php if ($watcherSong): ?>
                <span class="nhan nhan-ok">đang sống</span>
            <?php else: ?>
                <span class="nhan nhan-cho">im lặng / chưa cài</span>
            <?php endif; ?>
        </dd>
        <?php if ($dangCho): ?>
            <dt>Đang chờ xử lý</dt>
            <dd>
                <?php if ((int)$tt['dang_hen_gio'] === 1): ?>
                    Sẽ <strong><?= (int)$tt['muon_chay'] === 1 ? 'BẬT' : 'TẮT' ?></strong> lúc <strong><?= e((string)$tt['gio_hen']) ?></strong>
                <?php else: ?>
                    Sẽ <strong><?= (int)$tt['muon_chay'] === 1 ? 'BẬT' : 'TẮT' ?></strong> ngay khi watcher chạy lần tiếp theo
                <?php endif; ?>
                (yêu cầu bởi <?= e((string)($tt['yeu_cau_boi'] ?? '')) ?> lúc <?= e((string)($tt['yeu_cau_luc'] ?? '')) ?>)
                <form method="post" action="<?= e($TRANG) ?>" class="inline" style="margin-top:6px;">
                    <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
                    <input type="hidden" name="hanh_dong" value="huy_lenh">
                    <button class="btn btn-xam btn-nho">Huỷ yêu cầu này</button>
                </form>
            </dd>
        <?php endif; ?>
    </dl>
</div>

<?php if (!$watcherSong): ?>
    <div class="luuy">
        <p><strong>Watcher chưa cài hoặc đã dừng.</strong> Nút Bật/Tắt bên dưới vẫn ghi được yêu cầu vào
           database, nhưng sẽ KHÔNG có gì xảy ra cho tới khi cài xong tiến trình canh gác này trên VPS
           (chỉ cần làm một lần):</p>
        <p><code>sudo crontab -e</code></p>
        <p>Thêm dòng:</p>
        <p><code>* * * * * /opt/kpah/vps/dieu-khien-server.sh &gt;&gt;/opt/kpah/log/dieu-khien-server.log 2&gt;&amp;1</code></p>
        <p class="small">Chi tiết/an toàn: xem chú thích đầu file <code>vps/dieu-khien-server.sh</code>.</p>
    </div>
<?php endif; ?>

<div class="grid-2" style="margin-top:16px;">
    <div class="card">
        <h3>Bật server</h3>
        <p class="muted small">Áp dụng ngay lần watcher chạy tiếp theo (chậm nhất ~1 phút).</p>
        <form method="post" action="<?= e($TRANG) ?>">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="hanh_dong" value="bat_ngay">
            <button class="btn btn-primary">Bật server ngay</button>
        </form>

        <h3 style="margin-top:20px;">Đặt giờ tự bật</h3>
        <p class="muted small">Dùng khi bạn biết trước sẽ không có mặt đúng lúc cần bật.</p>
        <form method="post" action="<?= e($TRANG) ?>" class="ngang">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="hanh_dong" value="hen_gio">
            <label class="o">
                <span>Giờ bật (theo giờ hệ thống VPS)</span>
                <input type="datetime-local" name="gio_hen_dat" required>
            </label>
            <button type="submit" class="btn btn-primary btn-nho" style="align-self:flex-end;">Đặt lịch</button>
        </form>
    </div>

    <div class="card">
        <h3 style="color:#c0392b;">Tắt server</h3>
        <div class="luuy">
            <p><strong>Văng TẤT CẢ người chơi đang online.</strong> Server sẽ ở trạng thái tắt hẳn cho tới
               khi có ai đó bấm Bật lại (ở đây hoặc bằng CLI) — khác với Khởi động lại, KHÔNG tự bật lại.</p>
        </div>
        <form method="post" action="<?= e($TRANG) ?>"
              onsubmit="return confirm('TẮT SERVER — văng tất cả người chơi đang online và giữ tắt cho tới khi bật lại?');">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="hanh_dong" value="tat_ngay">
            <label class="o" style="min-width:260px;">
                <span>Gõ đúng cụm "<?= e(XAC_NHAN_TAT_SERVER) ?>" để xác nhận</span>
                <input type="text" name="xac_nhan" autocomplete="off"
                       oninput="document.getElementById('nutTatServer').disabled = this.value !== <?= e(json_encode(XAC_NHAN_TAT_SERVER)) ?>;">
            </label>
            <button type="submit" id="nutTatServer" class="btn btn-do" disabled style="margin-top:10px;">Tắt server ngay</button>
        </form>

        <h3 style="margin-top:20px;">Khởi động lại ngay (không tắt hẳn)</h3>
        <p class="muted small">Cơ chế khác, có sẵn từ trước, KHÔNG cần watcher — chỉ dùng được khi server
           đang chạy: server tự lưu dữ liệu rồi tự khởi động lại (~10-15 giây).</p>
        <form method="post" action="<?= e($TRANG) ?>"
              onsubmit="return confirm('Khởi động lại server ngay? Người chơi đang online sẽ bị văng ra trong ít giây.');">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="hanh_dong" value="restart_ngay">
            <button class="btn btn-xam">Khởi động lại ngay</button>
        </form>
    </div>
</div>

<?php endif; ?>
<?php adminFoot(); ?>
