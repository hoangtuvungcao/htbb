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
 *     Trang này ghi   :  bat, sua_boi, sua_luc, mo_luc, dong_luc
 *     Game server ghi :  ten, nhom, mo_ta, dang_bat, may_chu_luc
 *
 * Ngoại lệ duy nhất: khi một mốc trong `mo_luc`/`dong_luc` tới giờ thì `real/LichSuKien`
 * bên game ghi `bat` thay người, đặt `sua_boi='lich hen'` rồi xoá mốc đã chạy.
 *
 * TỰ KHỞI ĐỘNG LẠI. Đổi `bat` xong không phải tự đi restart nữa: game server gom các thay
 * đổi trong `sv.suKienGomGiay` giây, báo trong game `sv.suKienBaoTruocGiay` giây, rồi bật
 * cờ `5h_systems.restart` để BaoTriDaily lưu dữ liệu và thoát — systemd bật lại sau 10
 * giây. Trang này KHÔNG cần quyền sudo hay ssh, chỉ ghi CSDL. Tắt cơ chế đó bằng
 * `sv.suKienTuKhoiDongLai=0`.
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

/**
 * Giờ HIỆN TẠI THEO ĐỒNG HỒ CỦA CSDL, không phải của PHP.
 *
 * Bắt buộc phải như vậy: `real/LichSuKien` so mốc hẹn bằng `NOW()` của MySQL. Web server
 * và MySQL có thể khác múi giờ (PHP thường lấy UTC khi `date.timezone` chưa đặt), và khi
 * lệch thì trang này sẽ nhận một mốc mà nó tưởng là tương lai còn server thì chạy ngay —
 * hoặc ngược lại, chặn oan một mốc hợp lệ. Lấy chung một đồng hồ là hết chuyện.
 */
function bayGioTheoCsdl(): string
{
    static $bayGio = null;
    if ($bayGio === null) {
        try {
            $bayGio = (string)dbGame()->query('SELECT NOW()')->fetchColumn();
        } catch (Throwable $ex) {
            $bayGio = date('Y-m-d H:i:s');
        }
    }
    return $bayGio;
}

/**
 * Đổi giá trị của ô <input type="datetime-local"> thành 'Y-m-d H:i:s', hoặc null nếu bỏ trống.
 * Trả về false khi không đọc được — người gọi phải phân biệt false với null.
 *
 * @return string|null|false
 */
function docMoc(string $tho)
{
    $tho = trim($tho);
    if ($tho === '') {
        return null;
    }
    // Trình duyệt gửi "2026-10-25T00:00" (có khi kèm ":ss"). Nhận cả dạng có dấu cách
    // để dán tay từ SQL vào cũng chạy.
    $tho = str_replace('T', ' ', $tho);
    if (!preg_match('/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})(?::(\d{2}))?$/', $tho, $k)) {
        return false;
    }
    if (!checkdate((int)$k[2], (int)$k[3], (int)$k[1])
        || (int)$k[4] > 23 || (int)$k[5] > 59) {
        return false;
    }
    return sprintf('%s-%s-%s %s:%s:%s', $k[1], $k[2], $k[3], $k[4], $k[5], $k[6] ?? '00');
}

/** Đổi 'Y-m-d H:i:s' của CSDL về dạng ô datetime-local ('Y-m-dTH:i'). */
function mocChoO(?string $db): string
{
    if ($db === null || $db === '') {
        return '';
    }
    return substr(str_replace(' ', 'T', $db), 0, 16);
}

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

        if ($viec === 'luu_lich' || $viec === 'xoa_lich') {
            $st = dbGame()->prepare('SELECT ten FROM ' . BANG_SU_KIEN . ' WHERE ma = ?');
            $st->execute([$ma]);
            $ten = $st->fetchColumn();
            if ($ten === false) {
                veLai($TRANG, 'error', 'Không có sự kiện mã "' . $ma . '" trong bảng.');
            }

            if ($viec === 'xoa_lich') {
                dbGame()->prepare('UPDATE ' . BANG_SU_KIEN . '
                                      SET mo_luc = NULL, dong_luc = NULL
                                    WHERE ma = ?')->execute([$ma]);
                ghiNhatKy('su_kien_xoa_lich', $ma, (string)$ten);
                veLai($TRANG, 'success', 'Đã xoá lịch hẹn của ' . $ten . '.');
            }

            $mo   = docMoc((string)p('mo_luc'));
            $dong = docMoc((string)p('dong_luc'));
            if ($mo === false || $dong === false) {
                veLai($TRANG, 'error', 'Mốc thời gian không đọc được. Định dạng: 2026-10-25 00:00.');
            }

            // Chặn mốc đã qua. Không phải để cầu toàn: mốc quá khứ sẽ chạy ngay ở nhịp
            // kiểm tra kế tiếp (LichSuKien so `<= NOW()`), nên đặt nhầm ngày năm ngoái là
            // sự kiện bật/tắt tức thì kèm một lần khởi động lại không ai ngờ tới.
            $bayGio = bayGioTheoCsdl();
            if ($mo !== null && $mo <= $bayGio) {
                veLai($TRANG, 'error', 'Mốc mở đã qua (' . $mo . ' ≤ ' . $bayGio
                    . '). Muốn bật ngay thì dùng nút Bật.');
            }
            if ($dong !== null && $dong <= $bayGio) {
                veLai($TRANG, 'error', 'Mốc đóng đã qua (' . $dong . ' ≤ ' . $bayGio
                    . '). Muốn tắt ngay thì dùng nút Tắt.');
            }
            // Đóng phải sau mở. Bên Java chạy mở trước rồi mới chạy đóng, nên nếu để
            // đóng ≤ mở thì khi cả hai cùng quá hạn kết quả luôn là TẮT — đúng luật
            // nhưng chắc chắn không phải ý người đặt. Chặn ngay từ đây cho rõ.
            if ($mo !== null && $dong !== null && $dong <= $mo) {
                veLai($TRANG, 'error', 'Mốc đóng phải sau mốc mở.');
            }

            dbGame()->prepare('UPDATE ' . BANG_SU_KIEN . '
                                  SET mo_luc = ?, dong_luc = ?
                                WHERE ma = ?')->execute([$mo, $dong, $ma]);
            ghiNhatKy('su_kien_lich', $ma,
                (string)$ten . ' | mở: ' . ($mo ?? '—') . ' | đóng: ' . ($dong ?? '—'));
            veLai($TRANG, 'success', 'Đã đặt lịch cho ' . $ten
                . '. Tới giờ server sẽ tự đổi rồi tự khởi động lại.');
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
$coBang    = true;
$coCotLich = true;
$loiDoc    = '';
$dong      = [];
$mayChuLuc = null;

try {
    try {
        $dong = dbGame()->query('SELECT ma, bat, ten, nhom, mo_ta, dang_bat, sua_boi, sua_luc,
                                        may_chu_luc, mo_luc, dong_luc
                                 FROM ' . BANG_SU_KIEN)->fetchAll();
    } catch (Throwable $exLich) {
        // Bảng có từ trước 30/08/2026 thì chưa có hai cột lịch. Server tự ALTER lúc khởi
        // động (DatabaseSuKien.themCotThieu), nhưng cho tới lúc đó trang vẫn phải dùng
        // được — không thì cập nhật jar xong là mất luôn cả nút bật/tắt đang chạy tốt.
        $coCotLich = false;
        $dong = dbGame()->query('SELECT ma, bat, ten, nhom, mo_ta, dang_bat, sua_boi, sua_luc,
                                        may_chu_luc, NULL AS mo_luc, NULL AS dong_luc
                                 FROM ' . BANG_SU_KIEN)->fetchAll();
    }
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

/**
 * Sự kiện nào đang chờ khởi động lại.
 *
 * Điều kiện phải khớp TỪNG CHỮ với `DatabaseSuKien.soDongChoKhoiDongLai()` bên Java:
 * lệch, **và** thay đổi xảy ra sau lần khởi động gần nhất. Nếu ở đây chỉ so `bat<>dang_bat`
 * thì khi ảnh chụp lúc khởi động bị hỏng (server ghi `dang_bat` thất bại), trang sẽ báo
 * "chờ khởi động lại" mãi mãi trong khi server cố tình không làm gì — và không ai hiểu vì sao.
 */
$choKhoiDong = [];
$anhChupHong = [];
foreach ($dong as $d) {
    if ((int)$d['bat'] === (int)$d['dang_bat']) {
        continue;
    }
    $doAiSua = $d['sua_luc'] !== null
        && ($d['may_chu_luc'] === null || $d['sua_luc'] > $d['may_chu_luc']);
    if ($doAiSua) {
        $choKhoiDong[] = $d;
    } else {
        $anhChupHong[] = $d;
    }
}

/** Các sự kiện có mốc hẹn, sắp theo mốc gần nhất. */
$lichSapToi = [];
foreach ($dong as $d) {
    foreach ([['mo', $d['mo_luc']], ['dong', $d['dong_luc']]] as [$ve, $moc]) {
        if ($moc !== null && $moc !== '') {
            $lichSapToi[] = ['ten' => (string)$d['ten'], 've' => $ve, 'moc' => (string)$moc];
        }
    }
}
usort($lichSapToi, fn($a, $b) => strcmp($a['moc'], $b['moc']));

/**
 * Cấu hình tự khởi động lại, đọc thẳng từ server.ini của game server.
 * Web có thể nằm khác máy với game server — lúc đó không đọc được và ta nói thẳng là
 * không biết, chứ không đoán bừa là "đang bật".
 */
$tuKhoiDongLai = null;   // null = không đọc được
$gomGiay = 120;
$baoTruocGiay = 60;
if (defined('SERVER_DIR') && function_exists('kpahDocIni')) {
    $ini = kpahDocIni(rtrim(SERVER_DIR, "/\\") . '/server.ini');
    if ($ini) {
        $tuKhoiDongLai = ($ini['sv.suKienTuKhoiDongLai'] ?? '1') !== '0';
        // Kẹp y hệt CauHinh.apDungLichSuKien để con số hiện ra đúng cái server dùng.
        $gomGiay = max(30, min(3600, (int)($ini['sv.suKienGomGiay'] ?? 120)));
        $baoTruocGiay = max(10, min(600, (int)($ini['sv.suKienBaoTruocGiay'] ?? 60)));
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

/**
 * Ô đặt lịch hẹn của một dòng: hai mốc + nút lưu, và nút xoá khi đã có mốc.
 *
 * Một form cho cả hai mốc chứ không tách hai form: đặt mốc mở mà mốc đóng bị bỏ trống
 * do form kia không gửi kèm thì hoá ra vừa đặt vừa xoá.
 */
function oLich(array $d): void
{
    $coMoc = ($d['mo_luc'] ?? null) !== null || ($d['dong_luc'] ?? null) !== null;
    ?>
    <form method="post" action="sukien.php" class="lich-form">
        <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
        <input type="hidden" name="ma" value="<?= e((string)$d['ma']) ?>">
        <input type="hidden" name="viec" value="luu_lich">
        <label class="lich-o">
            <span class="muted small">mở</span>
            <input type="datetime-local" name="mo_luc"
                   value="<?= e(mocChoO($d['mo_luc'] ?? null)) ?>">
        </label>
        <label class="lich-o">
            <span class="muted small">đóng</span>
            <input type="datetime-local" name="dong_luc"
                   value="<?= e(mocChoO($d['dong_luc'] ?? null)) ?>">
        </label>
        <button class="btn btn-nho">Lưu lịch</button>
    </form>
    <?php if ($coMoc): ?>
        <form method="post" action="sukien.php" class="inline"
              onsubmit="return confirm('Xoá lịch hẹn của sự kiện này?');">
            <input type="hidden" name="csrf" value="<?= e(csrfToken()) ?>">
            <input type="hidden" name="ma" value="<?= e((string)$d['ma']) ?>">
            <input type="hidden" name="viec" value="xoa_lich">
            <button class="btn btn-nho btn-xam">Xoá lịch</button>
        </form>
    <?php endif;
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
        <p>Bật / tắt từng sự kiện trong game, hoặc <strong>hẹn giờ</strong> mở và đóng.
           Trước đây mốc thời gian của mọi sự kiện được ghi cứng trong mã nguồn (toàn năm
           2015–2024, nên đều đã tắt vĩnh viễn); nay chúng đọc thẳng từ bảng này nên
           <strong>bật lúc nào cũng được</strong>. Đổi xong server tự khởi động lại.</p>
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
        theSo('Đang hẹn lịch', so(count($lichSapToi)),
            $lichSapToi ? ('gần nhất ' . $lichSapToi[0]['moc']) : 'chưa hẹn mốc nào');
        ?>
    </div>

    <?php if (!$coCotLich): ?>
        <div class="luuy">
            <p><strong>Bảng chưa có hai cột lịch hẹn.</strong> Bảng này được tạo trước ngày
               30/08/2026 nên còn thiếu <code>mo_luc</code> và <code>dong_luc</code>. Phần
               bật/tắt bên dưới vẫn dùng bình thường, chỉ chưa hẹn giờ được.</p>
            <p class="small">Khởi động lại server một lần là xong — server tự thêm cột
               (<code>DatabaseSuKien.themCotThieu</code>). Hoặc chạy tay:
               <code>ALTER TABLE tob_su_kien ADD COLUMN mo_luc DATETIME NULL;</code>
               <code>ALTER TABLE tob_su_kien ADD COLUMN dong_luc DATETIME NULL;</code></p>
        </div>
    <?php endif; ?>

    <?php if ($choKhoiDong): ?>
        <div class="luuy">
            <p><strong>Có <?= so(count($choKhoiDong)) ?> sự kiện đang chờ vào game:</strong></p>
            <p>
                <?php foreach ($choKhoiDong as $d): ?>
                    <code><?= e((string)$d['ten']) ?>
                        <?= (int)$d['bat'] === 1 ? '→ bật' : '→ tắt' ?></code>
                <?php endforeach; ?>
            </p>
            <?php if ($tuKhoiDongLai === true): ?>
                <p><strong>Server sẽ tự khởi động lại.</strong> Nó đợi
                   <?= so($gomGiay) ?> giây không còn thay đổi nào nữa, báo trong game
                   <?= so($baoTruocGiay) ?> giây, rồi lưu dữ liệu và khởi động lại.
                   Bạn không phải làm gì thêm — cứ đặt hết những gì cần đặt rồi để đó.</p>
                <p class="small">Đổi thêm một sự kiện nữa là đồng hồ gom chạy lại từ đầu, nên
                   cả chùm thay đổi chỉ tốn <em>một</em> lần khởi động lại. Sau khi bật máy,
                   server im 3 phút đầu mới bắt đầu tính — chỗ này cố ý, vì systemd chỉ cho
                   khởi động 5 lần trong 300 giây, quá thì unit chết hẳn và không tự bật lại.</p>
            <?php elseif ($tuKhoiDongLai === false): ?>
                <p><strong>Tự khởi động lại đang TẮT</strong>
                   (<code>sv.suKienTuKhoiDongLai=0</code> trong <code>server.ini</code>).
                   Phải tự khởi động lại: <code>systemctl restart kpah-game</code>.</p>
            <?php else: ?>
                <p class="small">Không đọc được <code>server.ini</code> (web có thể nằm khác máy
                   với game server) nên không biết tự khởi động lại đang bật hay tắt. Nếu tắt
                   thì chạy tay: <code>systemctl restart kpah-game</code>.</p>
            <?php endif; ?>
            <p class="small">Vì sao phải khởi động lại: bảng shop, <code>Map.isSale</code> và
               danh sách NPC được dựng <em>một lần</em> lúc bật máy theo trạng thái sự kiện,
               đổi giữa chừng thì được nửa này hỏng nửa kia.</p>
        </div>
    <?php endif; ?>

    <?php if ($anhChupHong): ?>
        <div class="luuy">
            <p><strong>Có <?= so(count($anhChupHong)) ?> dòng lệch mà không phải do ai sửa.</strong>
               Cột <code>dang_bat</code> khác <code>bat</code> nhưng <code>sua_luc</code> lại
               cũ hơn lần khởi động gần nhất — nghĩa là lần bật máy vừa rồi server không ghi
               được ảnh chụp trạng thái.</p>
            <p>
                <?php foreach ($anhChupHong as $d): ?>
                    <code><?= e((string)$d['ten']) ?></code>
                <?php endforeach; ?>
            </p>
            <p class="small">Server <strong>cố tình không</strong> tự khởi động lại vì chuyện này:
               lệch kiểu đó không tự hết, nên nếu lấy nó làm cớ thì server sẽ khởi động lại vô
               hạn cho tới khi systemd bỏ cuộc và nằm luôn. Xem log <code>[SUKIEN]</code> lúc bật
               máy trong <a href="chandoan.php">Chẩn đoán</a>, thường là lỗi kết nối CSDL.</p>
        </div>
    <?php endif; ?>

    <?php if ($lichSapToi): ?>
        <div class="card">
            <div class="card-head"><h3>Lịch sắp tới</h3>
                <span class="muted small">giờ của CSDL, hiện là <?= e(bayGioTheoCsdl()) ?></span>
            </div>
            <div class="tbl-wrap">
                <table class="tbl">
                    <thead><tr><th>Lúc</th><th>Việc</th><th>Sự kiện</th></tr></thead>
                    <tbody>
                    <?php foreach ($lichSapToi as $l): ?>
                        <tr>
                            <td class="nowrap"><?= e($l['moc']) ?></td>
                            <td class="nowrap">
                                <span class="nhan <?= $l['ve'] === 'mo' ? 'nhan-ok' : 'nhan-mo' ?>">
                                    <?= $l['ve'] === 'mo' ? 'mở' : 'đóng' ?>
                                </span>
                            </td>
                            <td><?= e($l['ten']) ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
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
                        <?php if ($coCotLich): ?><th class="nowrap">Lịch hẹn</th><?php endif; ?>
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
                            <?php if ($coCotLich): ?>
                                <td class="nowrap"><?php oLich($d); ?></td>
                            <?php endif; ?>
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

            <dt>Lịch hẹn chỉ chạy một lần</dt>
            <dd>Mốc nào chạy xong thì bị xoá khỏi bảng, không lặp lại năm sau. Sự kiện theo
                mùa thì mỗi năm đặt lại một lần. Mốc để trống nghĩa là không tự làm gì —
                hẹn mở mà không hẹn đóng thì sự kiện chạy tới khi bạn tự tắt.</dd>

            <dt>Giờ ở đây là giờ của máy chủ CSDL</dt>
            <dd>Không phải giờ trên máy bạn. Đang là <code><?= e(bayGioTheoCsdl()) ?></code>.
                Lệch giờ thì mốc hẹn sẽ chạy sớm hoặc muộn đúng bằng khoảng lệch đó.</dd>

            <dt>Server tự khởi động lại, nhưng không tức thì</dt>
            <dd>Sau khi đổi, server đợi <?= so($gomGiay) ?> giây không còn thay đổi nào nữa
                rồi mới báo trong game <?= so($baoTruocGiay) ?> giây và tắt. Bật liền mấy sự
                kiện vẫn chỉ tốn một lần khởi động lại. Ba phút đầu sau khi bật máy thì nó
                không làm gì cả — để hai lần khởi động lại luôn cách nhau trên 5 phút, là
                cửa sổ mà systemd đếm số lần khởi động.</dd>

            <dt>Mọi thao tác ở đây đều vào nhật ký</dt>
            <dd>Xem lại ở mục <a href="nhatky.php">Nhật ký admin</a>.</dd>
        </dl>
    </div>

<?php endif; ?>
<?php adminFoot(); ?>
