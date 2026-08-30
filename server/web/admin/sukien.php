<?php
/**
 * Bật / tắt sự kiện trong game (bảng kpah_db.tob_su_kien).
 *
 * TRƯỚC NGÀY 30/08/2026 KHÔNG CÓ TRANG NÀY, và cũng không thể có: mỗi sự kiện được mở
 * bằng cách so ngày hệ thống với một mốc **ghi cứng** trong `src/real/Char.java`, ví dụ
 *
 *     return getDayTime(0L).compareTo("2020-11-01 00:00:00") < 0;   // Halloween
 *
 * Mốc nào cũng nằm trong 2015–2024 nên đến nay mọi sự kiện đều tắt vĩnh viễn, muốn mở
 * lại phải sửa mã nguồn rồi build lại jar. Nay các mốc đó đã bị bỏ hết: mỗi sự kiện là
 * một dòng trong `tob_su_kien`, bật lúc nào cũng được.
 *
 * AI GHI CỘT NÀO — trang này và game server không đè lên nhau:
 *
 *     Trang này ghi   :  bat, sua_boi, sua_luc
 *     Game server ghi :  ten, nhom, mo_ta, dang_bat, may_chu_luc
 *
 * Nên **danh sách sự kiện không nằm trong file PHP này**. Mỗi lần khởi động, server tự
 * đăng danh mục của nó lên bảng (`data/DatabaseSuKien.ghiAnhChup`). Thêm một sự kiện mới
 * trong Java là nó tự hiện ở đây, không phải sửa trang web.
 *
 * VÌ SAO PHẢI KHỞI ĐỘNG LẠI SERVER: rất nhiều tác dụng của sự kiện được tính **một lần**
 * lúc khởi động chứ không hỏi lại mỗi lần dùng — bảng shop (`Map.loadShop` đặt
 * SELL/NOT_SELL theo `Char.isSuKien*`), `Map.isSale`, danh sách NPC, bảng vật phẩm. Đổi
 * cờ giữa chừng thì được nửa này hỏng nửa kia, khó lần hơn là không đổi gì. So `sua_luc`
 * với `may_chu_luc` là cách trang này biết có cần nhắc hay không.
 */
require_once __DIR__ . '/inc/layout.php';
requireAdmin();

$TRANG = 'sukien.php';

/** Phải khớp hằng số BANG trong src/data/DatabaseSuKien.java. */
const BANG_SU_KIEN = 'tob_su_kien';

/** Mã của cổng tổng — phải khớp SuKien.TAT_CA bên Java. */
const MA_CONG_TONG = 'tatca';

// ---------------------------------------------------------------------------
//  Thao tác
// ---------------------------------------------------------------------------
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    chanCsrf($TRANG);

    $viec = p('viec');
    $ma   = p('ma');

    try {
        if ($viec === 'bat' || $viec === 'tat') {
            // Chỉ nhận mã ĐÃ CÓ trong bảng. Bảng do server tự đăng lên, nên đây cũng là
            // cách chặn người gửi form tay tạo ra dòng rác mà game không bao giờ đọc.
            $st = dbGame()->prepare('SELECT ten FROM ' . BANG_SU_KIEN . ' WHERE ma = ?');
            $st->execute([$ma]);
            $ten = $st->fetchColumn();
            if ($ten === false) {
                veLai($TRANG, 'error', 'Không có sự kiện mã "' . $ma . '" trong bảng.');
            }

            $st = dbGame()->prepare('UPDATE ' . BANG_SU_KIEN . '
                                        SET bat = ?, sua_boi = ?, sua_luc = NOW()
                                      WHERE ma = ?');
            $st->execute([$viec === 'bat' ? 1 : 0, adminName(), $ma]);
            ghiNhatKy('su_kien_' . $viec, $ma, (string)$ten);
            veLai($TRANG, 'success', ($viec === 'bat' ? 'Đã bật ' : 'Đã tắt ') . $ten
                . '. Phải khởi động lại server thì mới có hiệu lực.');
        }

        if ($viec === 'tat_het') {
            dbGame()->prepare('UPDATE ' . BANG_SU_KIEN . '
                                  SET bat = 0, sua_boi = ?, sua_luc = NOW()
                                WHERE bat = 1')->execute([adminName()]);
            ghiNhatKy('su_kien_tat_het', '', '');
            veLai($TRANG, 'success', 'Đã tắt toàn bộ sự kiện. Phải khởi động lại server.');
        }

        veLai($TRANG, 'error', 'Không hiểu thao tác.');
    } catch (Throwable $ex) {
        veLai($TRANG, 'error', 'Không thao tác được: ' . $ex->getMessage());
    }
}

// ---------------------------------------------------------------------------
//  Đọc dữ liệu
// ---------------------------------------------------------------------------
$coBang   = true;
$loiDoc   = '';
$dong     = [];
$mayChuLuc = null;

try {
    $dong = dbGame()->query('SELECT ma, bat, ten, nhom, mo_ta, dang_bat, sua_boi, sua_luc, may_chu_luc
                             FROM ' . BANG_SU_KIEN)->fetchAll();
    foreach ($dong as $d) {
        if ($d['may_chu_luc'] !== null && ($mayChuLuc === null || $d['may_chu_luc'] > $mayChuLuc)) {
            $mayChuLuc = $d['may_chu_luc'];
        }
    }
} catch (Throwable $ex) {
    $coBang = false;
    $loiDoc = $ex->getMessage();
}

/**
 * Gom theo nhóm, GIỮ NGUYÊN thứ tự server đăng lên.
 *
 * Không sắp xếp lại theo bảng chữ cái: thứ tự trong `src/real/SuKien.java` là thứ tự có
 * ý nghĩa (cổng tổng lên đầu, rồi theo mùa). Nhưng SELECT không có ORDER BY thì MySQL
 * trả theo thứ tự khoá chính, nên ở đây sắp lại theo nhóm rồi theo tên cho ổn định.
 */
$theoNhom = [];
foreach ($dong as $d) {
    $theoNhom[(string)$d['nhom']][] = $d;
}
ksort($theoNhom);

/** Sự kiện nào đang chờ khởi động lại: thiết lập khác trạng thái server đang chạy. */
$choKhoiDong = [];
foreach ($dong as $d) {
    if ((int)$d['bat'] !== (int)$d['dang_bat']) {
        $choKhoiDong[] = $d;
    }
}

$soDangBat = 0;
foreach ($dong as $d) {
    $soDangBat += (int)$d['dang_bat'] === 1 ? 1 : 0;
}
$soSeBat = 0;
foreach ($dong as $d) {
    $soSeBat += (int)$d['bat'] === 1 ? 1 : 0;
}

/** Cổng tổng đang tắt trong khi có sự kiện khác được bật — cảnh báo riêng. */
$congTongTat = false;
foreach ($dong as $d) {
    if ($d['ma'] === MA_CONG_TONG) {
        $congTongTat = (int)$d['bat'] !== 1;
    }
}

/** Nút bật/tắt một dòng. */
function nutSuKien(array $d): void
{
    $dangBat = (int)$d['bat'] === 1;
    ?>
    <form method="post" action="sukien.php" class="inline">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="ma" value="<?= e((string)$d['ma']) ?>">
        <input type="hidden" name="viec" value="<?= $dangBat ? 'tat' : 'bat' ?>">
        <button class="btn btn-nho <?= $dangBat ? 'btn-xam' : 'btn-primary' ?>">
            <?= $dangBat ? 'Tắt' : 'Bật' ?>
        </button>
    </form>
    <?php
}

adminHead('sukien', 'Sự kiện');
?>
<div class="page-head">
    <div>
        <h1>Sự kiện</h1>
        <p>Bật / tắt từng sự kiện trong game. Trước đây mốc thời gian của mọi sự kiện được
           ghi cứng trong mã nguồn (toàn năm 2015–2024, nên đều đã tắt vĩnh viễn); nay
           chúng đọc thẳng từ bảng này nên <strong>bật lúc nào cũng được</strong>.</p>
    </div>
    <?php if ($coBang && $soSeBat > 0): ?>
        <form method="post" action="sukien.php"
              onsubmit="return confirm('Tắt toàn bộ sự kiện đang đặt bật?');">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="viec" value="tat_het">
            <button class="btn btn-do btn-nho">Tắt hết</button>
        </form>
    <?php endif; ?>
</div>

<?php if (!$coBang): ?>
    <div class="luuy">
        <p><strong>Chưa đọc được bảng <code><?= e(BANG_SU_KIEN) ?></code>.</strong>
           Server tự tạo bảng này lần đầu khởi động sau khi cập nhật. Muốn tạo trước thì chạy:</p>
        <p><code>mysql -u root kpah_db &lt; sql/them_bang_su_kien.sql</code></p>
        <p class="small">Lỗi đọc được: <?= e($loiDoc) ?></p>
    </div>
<?php elseif (!$dong): ?>
    <div class="luuy">
        <p><strong>Bảng đã có nhưng chưa có dòng nào.</strong> Danh sách sự kiện do
           <em>game server</em> đăng lên mỗi lần khởi động, không phải do trang web tạo.
           Hãy khởi động lại server một lần rồi quay lại đây.</p>
        <p class="small">Nếu khởi động lại rồi mà vẫn trống thì server đang chạy bản jar cũ
           (chưa có <code>real/SuKien</code>) — build và cập nhật lại jar.</p>
    </div>
<?php else: ?>

    <div class="stat-grid">
        <?php
        theSo('Đang chạy trên server', so($soDangBat) . ' / ' . so(count($dong)),
            $mayChuLuc ? ('tính lúc server khởi động ' . $mayChuLuc) : '');
        theSo('Đang đặt bật', so($soSeBat) . ' / ' . so(count($dong)),
            'sẽ có hiệu lực sau lần khởi động tới');
        theSo('Chờ khởi động lại', so(count($choKhoiDong)),
            count($choKhoiDong) > 0 ? 'thiết lập chưa vào game' : 'không có gì lệch',
            count($choKhoiDong) > 0 ? 'canh-bao' : 'tot');
        ?>
    </div>

    <?php if ($choKhoiDong): ?>
        <div class="luuy">
            <p><strong>Cần khởi động lại server.</strong>
               <?= so(count($choKhoiDong)) ?> sự kiện đang đặt khác với trạng thái server đang chạy:</p>
            <p>
                <?php foreach ($choKhoiDong as $d): ?>
                    <code><?= e((string)$d['ten']) ?>
                        <?= (int)$d['bat'] === 1 ? '→ bật' : '→ tắt' ?></code>
                <?php endforeach; ?>
            </p>
            <p class="small">Trên VPS: <code>systemctl restart kpah-game</code>. Người chơi
               đang online sẽ bị ngắt, nên chọn giờ vắng. Lý do phải khởi động lại: bảng
               shop, <code>Map.isSale</code> và danh sách NPC được dựng <em>một lần</em>
               lúc bật máy theo trạng thái sự kiện, đổi giữa chừng thì được nửa này hỏng nửa kia.</p>
        </div>
    <?php endif; ?>

    <?php if ($congTongTat && $soSeBat > 0): ?>
        <div class="luuy">
            <p><strong>Cổng tổng đang tắt.</strong> Hơn 70 chỗ trong mã nguồn hỏi
               <code>isFinishAllSuKien()</code> <em>trước</em> khi hỏi từng sự kiện. Để cổng
               tổng tắt thì nhiều nội dung của sự kiện bạn vừa bật sẽ không hiện ra.</p>
            <p class="small">Muốn chạy sự kiện cũ thì bật <strong>“Cổng tổng cho mọi sự kiện”</strong> ở nhóm đầu tiên.</p>
        </div>
    <?php endif; ?>

    <?php foreach ($theoNhom as $tenNhom => $ds): ?>
        <div class="card">
            <div class="card-head"><h3><?= e($tenNhom === '' ? 'Khác' : $tenNhom) ?></h3></div>
            <div class="tbl-wrap">
                <table class="tbl">
                    <thead>
                    <tr>
                        <th>Sự kiện</th>
                        <th class="nowrap">Trên server</th>
                        <th class="nowrap">Đặt</th>
                        <th class="nowrap">Sửa lần cuối</th>
                        <th class="nowrap"></th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php foreach ($ds as $d):
                        $dangBat = (int)$d['dang_bat'] === 1;
                        $seBat   = (int)$d['bat'] === 1;
                        $lech    = $dangBat !== $seBat;
                        ?>
                        <tr>
                            <td>
                                <strong><?= e((string)$d['ten']) ?></strong>
                                <div class="muted small"><?= e((string)$d['mo_ta']) ?></div>
                                <div class="muted small"><code><?= e((string)$d['ma']) ?></code></div>
                            </td>
                            <td class="nowrap">
                                <span class="nhan <?= $dangBat ? 'nhan-ok' : 'nhan-mo' ?>">
                                    <?= $dangBat ? 'đang bật' : 'tắt' ?>
                                </span>
                            </td>
                            <td class="nowrap">
                                <span class="nhan <?= $seBat ? ($lech ? 'nhan-cho' : 'nhan-ok') : ($lech ? 'nhan-cho' : 'nhan-mo') ?>">
                                    <?= $seBat ? 'bật' : 'tắt' ?>
                                </span>
                                <?php if ($lech): ?>
                                    <div class="muted small">chờ khởi động lại</div>
                                <?php endif; ?>
                            </td>
                            <td class="nowrap muted small">
                                <?php if ($d['sua_luc']): ?>
                                    <?= e((string)$d['sua_luc']) ?><br>
                                    <?= e((string)$d['sua_boi']) ?>
                                <?php else: ?>
                                    —
                                <?php endif; ?>
                            </td>
                            <td class="nowrap"><?php nutSuKien($d); ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        </div>
    <?php endforeach; ?>

    <div class="card">
        <div class="card-head"><h3>Nên biết trước khi bật một sự kiện cũ</h3></div>
        <dl class="dl">
            <dt>Bật cổng tổng trước</dt>
            <dd>Rất nhiều nội dung sự kiện nằm sau <code>if (!Char.isFinishAllSuKien())</code>.
                Bỏ qua bước này là bật xong mà không thấy gì.</dd>

            <dt>Sự kiện cũ có thể cần vật phẩm không còn trong bảng dữ liệu</dt>
            <dd>Các bản sự kiện 2015–2016 tham chiếu id vật phẩm, NPC và bản đồ của thời đó.
                Bật xong nên tự vào game kiểm tra NPC tương ứng trước khi thông báo cho người chơi.</dd>

            <dt>Tỉ lệ rơi đồ sự kiện đặt riêng</dt>
            <dd><code>sv.tileItemSuKien</code> trong <code>server.ini</code> nhân vào mọi tỉ lệ
                rơi vật phẩm sự kiện. Đang để thấp thì bật sự kiện lên người chơi vẫn gần như
                không nhặt được gì.</dd>

            <dt>Sự kiện World Cup có cửa sổ thời gian riêng</dt>
            <dd>Hai dòng <em>World Cup</em> ở đây đè lên cửa sổ <code>sv.worldCup*</code> trong
                <code>server.ini</code>. Lần đầu chạy sự kiện này còn phải chạy
                <code>sql/them_su_kien_world_cup.sql</code> một lần.</dd>

            <dt>Mọi thao tác ở đây đều vào nhật ký</dt>
            <dd>Xem lại ở mục <a href="nhatky.php">Nhật ký admin</a>.</dd>
        </dl>
    </div>

<?php endif; ?>
<?php adminFoot(); ?>
