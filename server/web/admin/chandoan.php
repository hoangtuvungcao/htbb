<?php
/**
 * Chẩn đoán & Lỗi — gom mọi chỗ có thể hỏng vào một trang.
 *
 * Bốn nguồn lỗi của hệ thống này, và trang gom đủ cả bốn:
 *
 *   1. LỖI JAVA        server ghi vết gọi vào errorlog.txt (util.Logger.logError),
 *                      còn stdout/stderr thì systemd đổ vào log/game.out.
 *   2. LỖI TRONG GAME   server tự ghi vào tob_log_all_item / tob_log_all_gem với
 *                      cột aclog mang tên loại: loiinvent, hackc, black, ffbug...
 *   3. DỮ LIỆU LỆCH     không ai báo lỗi cả, phải tự đi soi: giftcode trùng tên,
 *                      nhân vật mồ côi tài khoản, cột inven quá nhỏ...
 *   4. LỖI TRANG WEB    ghi vào web_admin_log với hanh_dong = 'loi_web'.
 *
 * Mỗi phép kiểm tra nằm trong try/catch riêng: một bảng thiếu trên máy chủ này
 * thì phép đó báo "không kiểm được", các phép còn lại vẫn chạy.
 */
require_once __DIR__ . '/inc/layout.php';
require_once __DIR__ . '/inc/thuong.php';
requireAdmin();

$DB_GAME_TEN = preg_replace('/[^A-Za-z0-9_]/', '', DB_GAME);
$DB_ACC_TEN  = preg_replace('/[^A-Za-z0-9_]/', '', DB_ACCOUNT);

/**
 * Danh sách file log của server mà trang này được phép đọc. CỐ ĐỊNH — tên file
 * không bao giờ lấy từ người dùng, chỉ lấy khoá của mảng này. Không có bước đó
 * thì `?file=../../config.local.php` sẽ đọc được mật khẩu database.
 *
 * Đường dẫn tương đối so với SERVER_DIR là đúng: util.Logger ghi bằng
 * `new FileWriter(tên, true)` với đường dẫn tương đối, nên nó rơi vào thư mục
 * làm việc của JVM — `WorkingDirectory=/opt/kpah` trong kpah-game.service.
 *
 * ĐÃ BỎ khỏi danh sách vì đối chiếu với src/ thì chúng KHÔNG BAO GIỜ sinh ra:
 *   hack.txt       chỉ được ghi trong Database.banchar() — hàm này không ai gọi
 *   hackdo.txt     không được ghi ở đâu cả, chỉ được ĐỌC (Database.java:3703)
 *   offline.txt    cũng là file ĐẦU VÀO gõ tay (Database.java:11715), không
 *                  liên quan gì tới "rớt kết nối"
 *   saitenchar4.txt nằm trong giveGemItem() — cũng không ai gọi
 */
function fileLog(): array
{
    return [
        'errorlog.txt'      => 'Vết gọi lỗi Java + cảnh báo chặn lệnh admin trái phép (AdminHandler)',
        'log/game.out'      => 'Toàn bộ stdout/stderr của server (chỉ có trên VPS Linux)',
        'log/login.out'     => 'stdout/stderr của login server — đăng nhập hỏng thì xem đây',
        'log/autodeploy.log' => 'Nhật ký deploy tự động (vps/deploy-auto.sh)',
        'log/don-log.log'   => 'Nhật ký dọn log định kỳ (vps/don-log.sh)',
        'errorAnima.txt'    => 'Lỗi khi LƯU thú nuôi (chỉ đường lưu, không có đường nạp)',
        'errorSellITem.txt' => 'Đếm số lần bán vật phẩm hỏng — chỉ ghi chuỗi cố định, không có giờ / tên / vết gọi',
        'errorquayso.txt'   => 'Lỗi thủ tục sự kiện theo ngày (vòng quay và cả sự kiện khác)',
        'savechar.txt'      => 'Lỗi lưu nhân vật — CHỈ đường lưu tự động; đường lưu chính ghi ra <tên nhân vật>.txt',
        'matketinfo.txt'    => 'Chốt số mặt kê mỗi ngày và mỗi lần tắt server',
        'AdminChat.txt'     => 'Mọi câu chat của tài khoản admin — dấu vết lệnh GM',
        'logtopluong.txt'   => 'Nhân vật nạp từ 10.000 lượng trở lên',
        'updateleaf.txt'    => 'Lỗi vòng lặp AI quái (MapVongLap)',
        'rUpdateleaf.txt'   => 'Lỗi vòng lặp AI quái (Region / RegionVantienTran)',
        'nhantieu.txt'      => 'Sổ vận tiêu: nhận tiêu (ghi lúc sang ngày mới)',
        'doitieu.txt'       => 'Sổ vận tiêu: đổi tiêu',
        'thetieu.txt'       => 'Sổ vận tiêu: thế tiêu',
        'tratieu.txt'       => 'Sổ vận tiêu: trả tiêu',
        'cuoptieu.txt'      => 'Sổ vận tiêu: cướp tiêu',
    ];
}

/**
 * Tìm các file `<tên nhân vật>.txt` do LƯU NHÂN VẬT THẤT BẠI sinh ra.
 *
 * `DatabaseNhanVat.java:770` ghi `Logger.logError(ex, p.charname + ".txt")` mỗi
 * khi đường lưu chính hỏng — tên file là dữ liệu chạy nên không thể liệt kê
 * trước trong fileLog(). Đây thường là vết duy nhất còn lại của sự cố "nhân vật
 * mất đồ / tụt cấp sau khi thoát game".
 *
 * An toàn: glob() chỉ trả về tên có thật trong thư mục, không nhận gì từ người
 * dùng. Lọc bỏ mọi tên đã biết để chỉ còn lại phần dư.
 */
function fileLuuNhanVatHong(): array
{
    $daBiet = array_keys(fileLog());
    // Ba file server ĐỌC ngược lại như dữ liệu đầu vào, không phải log
    $daBiet = array_merge($daBiet, ['trangan.txt', 'offline.txt', 'top.txt', 'hack.txt', 'hackdo.txt']);

    $ra = [];
    foreach (glob(rtrim(SERVER_DIR, "/\\") . '/*.txt') ?: [] as $duongDan) {
        $ten = basename($duongDan);
        if (!in_array($ten, $daBiet, true)) {
            $ra[$ten] = filemtime($duongDan) ?: 0;
        }
    }
    arsort($ra);
    return $ra;
}

/** Đọc phần CUỐI của một file, không nạp cả file vào bộ nhớ (game.out có thể vài trăm MB). */
function doiFile(string $duongDan, int $soByte = 64000): ?array
{
    if (!is_file($duongDan) || !is_readable($duongDan)) {
        return null;
    }
    $co = filesize($duongDan);
    $fh = @fopen($duongDan, 'rb');
    if (!$fh) {
        return null;
    }
    if ($co > $soByte) {
        fseek($fh, -$soByte, SEEK_END);
        fgets($fh); // bỏ dòng đầu vì gần chắc chắn bị cắt giữa chừng
    }
    $noiDung = stream_get_contents($fh);
    fclose($fh);
    return [
        'noi_dung' => $noiDung === false ? '' : $noiDung,
        'co'       => $co,
        'cat'      => $co > $soByte,
        'sua_luc'  => filemtime($duongDan),
    ];
}

// ---------------------------------------------------------------------------
//  Chạy các phép kiểm tra
// ---------------------------------------------------------------------------
$kiemTra = [];

/**
 * @param string $muc  'ok' | 'canhbao' | 'loi' | 'khong_kiem_duoc'
 * @param array  $bang [['nhãn cột' => giá trị], ...] hiện kèm nếu có
 */
function them(string $ten, string $muc, string $mota, string $cachSua = '', array $bang = []): void
{
    global $kiemTra;
    $kiemTra[] = compact('ten', 'muc', 'mota', 'cachSua', 'bang');
}

/** Bọc một phép kiểm tra: hỏng thì báo "không kiểm được" chứ không vỡ trang. */
function thu(string $ten, callable $viec): void
{
    try {
        $viec();
    } catch (Throwable $ex) {
        them($ten, 'khong_kiem_duoc', 'Không chạy được phép kiểm tra này: ' . $ex->getMessage());
    }
}

// --- 1. Sức chứa các cột hành trang -----------------------------------------
$tranInven = null;
thu('Sức chứa cột hành trang', function () use ($DB_GAME_TEN, &$tranInven) {
    $st = dbGame()->prepare(
        'SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH AS toi_da
           FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
            AND COLUMN_NAME IN (?, ?, ?, ?)'
    );
    $st->execute([$DB_GAME_TEN, 'tob_char', 'inven', 'bag', 'tuido', 'equip']);
    $cot = $st->fetchAll();

    if (!$cot) {
        them('Sức chứa cột hành trang', 'khong_kiem_duoc',
            'Không đọc được information_schema — tài khoản MySQL của web thiếu quyền?');
        return;
    }

    $nho = [];
    $bang = [];
    foreach ($cot as $c) {
        $toiDa = (int)$c['toi_da'];
        $bang[] = ['Cột' => $c['COLUMN_NAME'], 'Kiểu' => strtoupper((string)$c['DATA_TYPE']),
                   'Sức chứa' => so($toiDa) . ' byte'];
        if (strtolower((string)$c['DATA_TYPE']) === 'text') {
            $nho[] = (string)$c['COLUMN_NAME'];
        }
        if ($tranInven === null || $toiDa < $tranInven) {
            $tranInven = $toiDa;
        }
    }

    if ($nho) {
        them('Sức chứa cột hành trang', 'loi',
            'Các cột ' . implode(', ', $nho) . ' vẫn là TEXT — trần 65.535 byte, tức khoảng 140 món. '
          . 'Vượt trần thì câu UPDATE lưu nhân vật thất bại, và khối bắt lỗi của server TỰ ĐẶT ban = 1: '
          . 'nhân vật biến mất khỏi màn chọn, người chơi tưởng bị xoá tài khoản. Chuyện này đã xảy ra thật '
          . 'ngày 28/07/2026 với nhân vật admin.',
            "ALTER TABLE " . DB_GAME . ".tob_char MODIFY inven MEDIUMTEXT;\n"
          . "ALTER TABLE " . DB_GAME . ".tob_char MODIFY bag MEDIUMTEXT;\n"
          . "ALTER TABLE " . DB_GAME . ".tob_char MODIFY tuido MEDIUMTEXT;\n"
          . "ALTER TABLE " . DB_GAME . ".tob_char MODIFY equip MEDIUMTEXT;",
            $bang);
    } else {
        them('Sức chứa cột hành trang', 'ok',
            'Cả bốn cột đều đã được nới rộng, không còn nguy cơ tràn ở mức vài trăm món.', '', $bang);
    }
});

// --- 2. Nhân vật sắp tràn hành trang ----------------------------------------
thu('Nhân vật sắp đầy hành trang', function () use (&$tranInven) {
    $tran = $tranInven ?: 65535;
    $nguong = (int)($tran * 0.80);

    $st = dbGame()->prepare(
        'SELECT charname, LENGTH(inven) AS n, ban FROM tob_char
          WHERE LENGTH(inven) > ? ORDER BY n DESC LIMIT 20'
    );
    $st->execute([$nguong]);
    $dong = $st->fetchAll();

    if (!$dong) {
        them('Nhân vật sắp đầy hành trang', 'ok',
            'Không nhân vật nào dùng quá 80% sức chứa cột inven (' . so($nguong) . ' byte).');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Nhân vật' => $d['charname'],
                   'Đã dùng'  => so($d['n']) . ' / ' . so($tran) . ' byte',
                   'Phần trăm' => round((int)$d['n'] * 100 / $tran, 1) . '%',
                   'Đang khoá' => (int)$d['ban'] !== 0 ? 'CÓ' : ''];
    }
    them('Nhân vật sắp đầy hành trang', 'canhbao',
        count($dong) . ' nhân vật đã dùng quá 80% sức chứa cột inven. Đầy là lần lưu kế tiếp hỏng '
      . 'và server tự khoá nhân vật.', '', $bang);
});

// --- 3. Dấu vết lưu hành trang thất bại -------------------------------------
thu('Lỗi lưu hành trang (loiinvent)', function () {
    $dong = dbGame()->query(
        "SELECT charname, COUNT(*) AS n, MAX(timelog) AS lan_cuoi
           FROM tob_log_all_item WHERE aclog = 'loiinvent'
          GROUP BY charname ORDER BY lan_cuoi DESC LIMIT 20"
    )->fetchAll();

    if (!$dong) {
        them('Lỗi lưu hành trang (loiinvent)', 'ok',
            'Server chưa từng ghi dòng loiinvent nào — chưa nhân vật nào lưu hành trang thất bại.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Nhân vật' => $d['charname'], 'Số lần' => so($d['n']), 'Gần nhất' => $d['lan_cuoi']];
    }
    them('Lỗi lưu hành trang (loiinvent)', 'loi',
        count($dong) . ' nhân vật từng lưu hành trang thất bại. Mỗi lần như vậy là nhân vật lùi về bản '
      . 'lưu thành công cuối cùng — phần chơi ở giữa mất thật, gỡ khoá không lấy lại được. Nhiều khả năng '
      . 'các nhân vật này đang bị ban = 1.',
        'UPDATE ' . DB_GAME . '.tob_char SET ban = 0 WHERE charname = \'ten_nhan_vat\';', $bang);
});

// --- 4. Nhân vật đang bị khoá ------------------------------------------------
thu('Nhân vật đang bị khoá', function () {
    $dong = dbGame()->query(
        'SELECT charname, userId, lastLv, lastLog FROM tob_char
          WHERE ban <> 0 ORDER BY lastLog DESC LIMIT 20'
    )->fetchAll();

    if (!$dong) {
        them('Nhân vật đang bị khoá', 'ok', 'Không nhân vật nào đang bị khoá.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Nhân vật' => $d['charname'], 'Tài khoản' => '#' . (int)$d['userId'],
                   'Cấp' => (int)$d['lastLv'], 'Vào lần cuối' => $d['lastLog']];
    }
    them('Nhân vật đang bị khoá', 'canhbao',
        count($dong) . ' nhân vật đang bị khoá. Nhớ rằng không phải cái nào cũng do admin khoá — '
      . 'server tự đặt ban = 1 khi lưu nhân vật thất bại.', '', $bang);
});

// --- 5. Giftcode trùng tên ---------------------------------------------------
thu('Giftcode trùng tên', function () {
    $dong = dbGame()->query(
        'SELECT giftcode, COUNT(*) AS n FROM giftcode GROUP BY giftcode HAVING n > 1 ORDER BY n DESC'
    )->fetchAll();

    if (!$dong) {
        them('Giftcode trùng tên', 'ok', 'Mỗi mã một tên, không có mã nào bị che khuất.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Mã' => $d['giftcode'], 'Số dòng' => (int)$d['n']];
    }
    them('Giftcode trùng tên', 'loi',
        'Có mã bị đặt trùng tên. Câu truy vấn của server kết thúc bằng LIMIT 1 nên chỉ dòng ĐẦU TIÊN '
      . 'được dùng — những dòng sau vĩnh viễn không ai nhận được, dù bạn có sửa phần thưởng trong đó.',
        'SELECT id, giftcode, item FROM ' . DB_GAME . ".giftcode WHERE giftcode = 'ten_ma' ORDER BY id;",
        $bang);
});

// --- 6. Giftcode sai cú pháp phần thưởng -------------------------------------
thu('Cú pháp phần thưởng của các mã đang có', function () {
    $dong = dbGame()->query('SELECT id, giftcode, item, type, limit_use FROM giftcode ORDER BY id')->fetchAll();
    $xau = [];
    foreach ($dong as $d) {
        $item = (string)($d['item'] ?? '');
        if ($item === '' || strtoupper($item) === 'NULL') {
            continue;
        }
        [$chuan, $loi] = kiemTraItem($item);
        if ($loi) {
            $xau[] = ['Mã' => $d['giftcode'],
                      'Còn dùng được' => ((int)$d['type'] === 0 && (int)$d['limit_use'] !== 0) ? 'CÓ' : 'không',
                      'Vấn đề' => implode(' | ', $loi)];
        }
    }
    if (!$xau) {
        them('Cú pháp phần thưởng của các mã đang có', 'ok',
            'Cả ' . count($dong) . ' mã đều dùng loại phần thưởng mà server hiểu, đủ tham số.');
        return;
    }
    them('Cú pháp phần thưởng của các mã đang có', 'loi',
        count($xau) . ' mã có chuỗi phần thưởng sai. Hậu quả tuỳ kiểu sai: loại lạ thì server lặng lẽ bỏ '
      . 'qua (người chơi mất quà, không báo gì); thiếu tham số thì server ném lỗi GIỮA CHỪNG — người chơi '
      . 'đã nhận được nửa số quà nhưng dòng giftcode_log lẫn việc trừ lượt đều không chạy, nên họ nhận lại '
      . 'được vô hạn lần.',
        '', $xau);
});

// --- 7. Kích hoạt trùng dòng -------------------------------------------------
thu('Bảng kích hoạt 5h_active', function () {
    $dong = dbGame()->query(
        'SELECT username, COUNT(*) AS n FROM `5h_active` GROUP BY username HAVING n > 1 ORDER BY n DESC LIMIT 20'
    )->fetchAll();

    if (!$dong) {
        them('Bảng kích hoạt 5h_active', 'ok', 'Không có tài khoản nào bị ghi kích hoạt nhiều lần.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Tài khoản' => $d['username'], 'Số dòng' => (int)$d['n']];
    }
    them('Bảng kích hoạt 5h_active', 'canhbao',
        'Có tài khoản bị ghi kích hoạt nhiều lần. Không gây lỗi (server chỉ hỏi "có dòng nào không") '
      . 'nhưng là dấu hiệu ai đó bấm kích hoạt lặp, và làm số liệu thống kê lệch.',
        "DELETE a1 FROM " . DB_GAME . ".`5h_active` a1\n"
      . "  JOIN " . DB_GAME . ".`5h_active` a2\n"
      . "    ON a1.username = a2.username AND a1.id > a2.id;", $bang);
});

// --- 8. Kích hoạt mồ côi (bắc cầu 2 database) --------------------------------
thu('Kích hoạt mồ côi', function () use ($DB_ACC_TEN) {
    $dong = dbGame()->query(
        "SELECT a.username FROM `5h_active` a
          WHERE NOT EXISTS (SELECT 1 FROM `$DB_ACC_TEN`.team_user t WHERE t.username = a.username)
          LIMIT 20"
    )->fetchAll();

    if (!$dong) {
        them('Kích hoạt mồ côi', 'ok', 'Mọi dòng kích hoạt đều trỏ tới một tài khoản có thật.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Tài khoản không còn tồn tại' => $d['username']];
    }
    them('Kích hoạt mồ côi', 'canhbao',
        count($dong) . ' dòng kích hoạt trỏ tới tài khoản đã bị xoá. Vô hại, chỉ là rác — nhưng nếu sau '
      . 'này có người đăng ký TRÙNG TÊN cũ thì họ được kích hoạt sẵn mà không cần làm gì.',
        "DELETE a FROM " . DB_GAME . ".`5h_active` a\n"
      . " WHERE NOT EXISTS (SELECT 1 FROM " . DB_ACCOUNT . ".team_user t WHERE t.username = a.username);");
});

// --- 9. Nhân vật mồ côi tài khoản --------------------------------------------
thu('Nhân vật mồ côi tài khoản', function () use ($DB_ACC_TEN) {
    $dong = dbGame()->query(
        "SELECT charname, userId, lastLv FROM tob_char c
          WHERE NOT EXISTS (SELECT 1 FROM `$DB_ACC_TEN`.team_user t WHERE t.id = c.userId)
          ORDER BY lastLv DESC LIMIT 20"
    )->fetchAll();

    if (!$dong) {
        them('Nhân vật mồ côi tài khoản', 'ok', 'Mọi nhân vật đều thuộc về một tài khoản có thật.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Nhân vật' => $d['charname'], 'Trỏ tới tài khoản' => '#' . (int)$d['userId'],
                   'Cấp' => (int)$d['lastLv']];
    }
    them('Nhân vật mồ côi tài khoản', 'canhbao',
        count($dong) . ' nhân vật thuộc về tài khoản không còn tồn tại — không ai đăng nhập vào được nữa. '
      . 'Thường là dấu vết của một lần xoá tài khoản mà quên xoá nhân vật.');
});

// --- 10. Giá trị ban lạ ------------------------------------------------------
thu('Giá trị cột ban của tài khoản', function () {
    $dong = dbAccount()->query(
        'SELECT ban, COUNT(*) AS n FROM team_user WHERE ban NOT IN (0, 1, 3) GROUP BY ban'
    )->fetchAll();

    if (!$dong) {
        them('Giá trị cột ban của tài khoản', 'ok',
            'Chỉ có ba giá trị login server hiểu: 0 bình thường, 1 bị khoá, 3 chờ xác nhận email.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['ban =' => (int)$d['ban'], 'Số tài khoản' => so($d['n'])];
    }
    them('Giá trị cột ban của tài khoản', 'canhbao',
        'Có tài khoản mang giá trị ban mà login server không xử lý riêng. Nó rơi vào nhánh cuối và được '
      . 'coi như BÌNH THƯỜNG — nếu bạn đặt giá trị đó với ý định khoá thì tài khoản vẫn đăng nhập được.',
        'UPDATE ' . DB_ACCOUNT . '.team_user SET ban = 1 WHERE ban NOT IN (0, 1, 3);', $bang);
});

// --- 11. Bảng phụ có đủ không -----------------------------------------------
thu('Các bảng phụ', function () {
    $can = [
        'web_admin_log' => 'nhật ký trang quản trị — thiếu thì không ghi lại được ai làm gì, '
                         . 'và việc chặn dò mật khẩu chỉ đếm trong session',
        'tob_nap_moc'   => 'theo dõi mốc nạp của naptien.php — thiếu thì trang nạp tiền hỏng',
        'web_chan_spam' => 'đếm số lần đăng ký / đăng nhập sai / tạo đơn theo IP — thiếu thì '
                         . 'chỉ đếm trong session, xoá cookie là đếm lại từ đầu',
    ];
    $thieu = [];
    foreach ($can as $bang => $viLy) {
        try {
            dbGame()->query("SELECT 1 FROM `$bang` LIMIT 1");
        } catch (Throwable $ex) {
            $thieu[] = ['Bảng' => $bang, 'Dùng để' => $viLy];
        }
    }
    if (!$thieu) {
        them('Các bảng phụ', 'ok', 'Có đủ web_admin_log và tob_nap_moc.');
        return;
    }
    them('Các bảng phụ', 'canhbao', 'Thiếu ' . count($thieu) . ' bảng.',
        "mysql -u root " . DB_GAME . " < sql/them_bang_web_admin_log.sql\n"
      . "# tob_nap_moc nằm ở PHẦN 2I của vps/db_new_server.sql", $thieu);
});

// --- 12. Cờ nghi vấn gian lận ------------------------------------------------
thu('Cờ nghi vấn trên nhân vật', function () {
    $dong = dbGame()->query(
        'SELECT charname, nhack, blacklist, lastLv, lastLog FROM tob_char
          WHERE nhack > 0 OR blacklist > 0 ORDER BY lastLog DESC LIMIT 20'
    )->fetchAll();

    if (!$dong) {
        them('Cờ nghi vấn trên nhân vật', 'ok', 'Không nhân vật nào bị server đánh dấu nghi vấn.');
        return;
    }
    $bang = [];
    foreach ($dong as $d) {
        $bang[] = ['Nhân vật' => $d['charname'], 'nhack' => (int)$d['nhack'],
                   'blacklist' => (int)$d['blacklist'], 'Vào lần cuối' => $d['lastLog']];
    }
    them('Cờ nghi vấn trên nhân vật', 'canhbao',
        count($dong) . ' nhân vật bị server đánh dấu. Đây chỉ là nghi vấn máy tự đặt, không phải kết luận '
      . '— soi thêm ở Log game trước khi xử lý.', '', $bang);
});

// --- 13b. Thông tin database lấy từ đâu --------------------------------------
thu('Nguồn thông tin database', function () {
    $goc  = rtrim(SERVER_DIR, '/\\');
    $fGame  = $goc . '/server.ini';
    $fLogin = $goc . '/loginServer/server.ini';

    $trangThai = static function (string $f): string {
        if (!is_file($f))     return 'không có file';
        if (!is_readable($f)) return 'CÓ nhưng PHP KHÔNG đọc được';
        return 'đọc được';
    };

    $bang = [
        ['Nguồn' => 'server.ini', 'Đường dẫn' => $fGame, 'Tình trạng' => $trangThai($fGame)],
        ['Nguồn' => 'loginServer/server.ini', 'Đường dẫn' => $fLogin, 'Tình trạng' => $trangThai($fLogin)],
    ];

    // Giá trị đang thật sự dùng. KHÔNG in mật khẩu, chỉ in độ dài — vừa đủ để
    // biết nó có rỗng hay không mà không phơi ra màn hình.
    $bang[] = ['Nguồn' => 'ĐANG DÙNG', 'Đường dẫn' => DB_USER . '@' . DB_HOST . ':' . DB_PORT
                   . ' · mật khẩu ' . strlen(DB_PASS) . ' ký tự',
               'Tình trạng' => 'DB game: ' . DB_GAME . ' · DB tài khoản: ' . DB_ACCOUNT
                   . (defined('DB_ACCOUNT_USER') ? ' (dùng riêng ' . DB_ACCOUNT_USER . ')' : '')];

    $khongDoc = (is_file($fGame) && !is_readable($fGame))
             || (is_file($fLogin) && !is_readable($fLogin));

    if ($khongDoc) {
        them('Nguồn thông tin database', 'canhbao',
            'File cấu hình của game server CÓ ở đó nhưng PHP không có quyền đọc, nên web đang dùng giá trị '
          . 'khai trong config.local.php (hoặc mặc định). Hai bên lệch nhau lúc nào không hay: đổi mật khẩu '
          . 'database cho game server là website chết mà không rõ vì sao.',
            "usermod -a -G kpah www-data\n"
          . "chmod 640 " . SERVER_DIR . "/server.ini " . SERVER_DIR . "/loginServer/server.ini\n"
          . "systemctl restart php*-fpm apache2", $bang);
        return;
    }
    if (!is_file($fGame) && !is_file($fLogin)) {
        them('Nguồn thông tin database', 'ok',
            'Không thấy server.ini nào — website không nằm cùng cây thư mục với game server, nên nó dùng '
          . 'giá trị khai trong config.local.php. Bình thường nếu đây là máy chạy thử.', '', $bang);
        return;
    }
    them('Nguồn thông tin database', 'ok',
        'Đọc được cấu hình của chính game server, nên web và server luôn dùng cùng một thông tin '
      . 'database — đổi mật khẩu ở server.ini là web theo ngay, không phải sửa hai chỗ.', '', $bang);
});

// --- 13. Đọc được thư mục server không ---------------------------------------
thu('Đọc file log của server', function () {
    $coFile = 0;
    $bang = [];
    foreach (fileLog() as $ten => $mota) {
        $duongDan = rtrim(SERVER_DIR, '/\\') . '/' . $ten;
        if (is_file($duongDan)) {
            $coFile++;
            $bang[] = ['File' => $ten,
                       'Cỡ' => so(filesize($duongDan)) . ' byte',
                       'Sửa lần cuối' => date('Y-m-d H:i:s', filemtime($duongDan)),
                       'Đọc được' => is_readable($duongDan) ? 'có' : 'KHÔNG'];
        }
    }
    if ($coFile === 0) {
        them('Đọc file log của server', 'canhbao',
            'Không thấy file log nào trong ' . SERVER_DIR . '. Nếu website không nằm cùng máy với game '
          . 'server thì đúng như vậy — đặt lại SERVER_DIR trong config.local.php nếu muốn đọc được.');
        return;
    }
    them('Đọc file log của server', 'ok',
        'Thấy ' . $coFile . ' file log trong ' . SERVER_DIR . '.', '', $bang);
});

// ---------------------------------------------------------------------------
//  Thống kê lỗi trong log game
// ---------------------------------------------------------------------------
$LOAI_LOI = [
    'loiinvent'     => 'Lưu hành trang thất bại — server tự khoá nhân vật',
    'hackc'         => 'Nghi vấn gian lận',
    'hackR'         => 'Nghi vấn gian lận',
    'hackbitem'     => 'Nghi vấn gian lận vật phẩm',
    'hackchonchar'  => 'Nghi vấn gian lận lúc chọn nhân vật',
    'black'         => 'Vào danh sách đen',
    'ffbug'         => 'Lỗi đã biết bị khai thác',
    'timeout'       => 'Hết giờ chờ',
];

$thongKeLoi = [];
foreach (['tob_log_all_item', 'tob_log_all_gem'] as $bangLog) {
    try {
        $ph = implode(',', array_fill(0, count($LOAI_LOI), '?'));
        $st = dbGame()->prepare(
            "SELECT aclog, COUNT(*) AS n, MAX(timelog) AS lan_cuoi
               FROM `$bangLog`
              WHERE aclog IN ($ph) AND timelog > DATE_SUB(NOW(), INTERVAL 90 DAY)
              GROUP BY aclog ORDER BY n DESC"
        );
        $st->execute(array_keys($LOAI_LOI));
        foreach ($st->fetchAll() as $r) {
            $thongKeLoi[] = ['bang' => $bangLog, 'aclog' => $r['aclog'],
                             'n' => (int)$r['n'], 'lan_cuoi' => $r['lan_cuoi']];
        }
    } catch (Throwable $ex) {
        // bảng không có trên máy chủ này -> bỏ qua
    }
}

// ---------------------------------------------------------------------------
//  Lỗi của chính trang web
// ---------------------------------------------------------------------------
$loiWeb = [];
if (coBangNhatKy()) {
    try {
        $loiWeb = dbGame()->query(
            "SELECT thoi_gian, admin_user, doi_tuong, chi_tiet FROM web_admin_log
              WHERE hanh_dong = 'loi_web' ORDER BY id DESC LIMIT 20"
        )->fetchAll();
    } catch (Throwable $ex) {
    }
}

// ---------------------------------------------------------------------------
//  Xem một file log
// ---------------------------------------------------------------------------
$xemFile = g('file');
$noiDungFile = null;
$luuHong = fileLuuNhanVatHong();
// Cho xem cả hai nhóm: danh sách cố định, và các file <tên nhân vật>.txt tìm
// thấy thật trong thư mục. Cả hai đều KHÔNG lấy tên từ người dùng — tên phải
// khớp một khoá có sẵn thì mới đọc, nên "../../config.local.php" không lọt.
if ($xemFile !== '' && (array_key_exists($xemFile, fileLog()) || array_key_exists($xemFile, $luuHong))) {
    $noiDungFile = doiFile(rtrim(SERVER_DIR, '/\\') . '/' . $xemFile);
} else {
    $xemFile = '';
}

$demMuc = ['loi' => 0, 'canhbao' => 0, 'ok' => 0, 'khong_kiem_duoc' => 0];
foreach ($kiemTra as $k) {
    $demMuc[$k['muc']] = ($demMuc[$k['muc']] ?? 0) + 1;
}

$NHAN_MUC = [
    'loi'             => ['Cần xử lý', 'nhan-khoa'],
    'canhbao'         => ['Nên xem',   'nhan-cho'],
    'ok'              => ['Bình thường', 'nhan-ok'],
    'khong_kiem_duoc' => ['Không kiểm được', 'nhan-mo'],
];

adminHead('chandoan', 'Chẩn đoán & Lỗi');
?>
<div class="page-head">
    <div>
        <h1>Chẩn đoán &amp; Lỗi</h1>
        <p>Bốn nguồn: lỗi Java trong file, lỗi server ghi vào bảng log, dữ liệu lệch, và lỗi của chính trang web.</p>
    </div>
</div>

<div class="stat-grid">
    <?php theSo('Cần xử lý', so($demMuc['loi']), 'gây hỏng thật, nên sửa', $demMuc['loi'] > 0 ? 'nguy-hiem' : 'tot'); ?>
    <?php theSo('Nên xem', so($demMuc['canhbao']), 'chưa hỏng nhưng đáng để mắt', $demMuc['canhbao'] > 0 ? 'canh-bao' : 'tot'); ?>
    <?php theSo('Bình thường', so($demMuc['ok']), 'đã kiểm, không có vấn đề', 'tot'); ?>
    <?php if ($demMuc['khong_kiem_duoc'] > 0) { theSo('Không kiểm được', so($demMuc['khong_kiem_duoc']), 'thiếu bảng hoặc thiếu quyền'); } ?>
</div>

<?php
// Xếp mức nặng lên trước — đọc từ trên xuống là đúng thứ tự ưu tiên.
$thuTu = ['loi' => 0, 'canhbao' => 1, 'khong_kiem_duoc' => 2, 'ok' => 3];
usort($kiemTra, static fn($a, $b) => $thuTu[$a['muc']] <=> $thuTu[$b['muc']]);
?>

<?php foreach ($kiemTra as $k):
    [$nhan, $lop] = $NHAN_MUC[$k['muc']]; ?>
    <div class="card">
        <h3><?= e($k['ten']) ?> <span class="nhan <?= e($lop) ?>"><?= e($nhan) ?></span></h3>
        <p class="muted" style="font-size:.9rem;margin-top:0;"><?= e($k['mota']) ?></p>

        <?php if ($k['bang']): ?>
            <div class="tbl-wrap">
            <table class="tbl">
                <thead><tr>
                    <?php foreach (array_keys($k['bang'][0]) as $c): ?><th><?= e($c) ?></th><?php endforeach; ?>
                </tr></thead>
                <tbody>
                <?php foreach ($k['bang'] as $d): ?>
                    <tr><?php foreach ($d as $gt): ?><td><?= e((string)$gt) ?></td><?php endforeach; ?></tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            </div>
        <?php endif; ?>

        <?php if ($k['cachSua'] !== ''): ?>
            <p class="muted small" style="margin:12px 0 4px;">Cách sửa:</p>
            <pre class="ma-sql"><?= e($k['cachSua']) ?></pre>
        <?php endif; ?>
    </div>
<?php endforeach; ?>

<div class="card">
    <h3>Lỗi server ghi vào bảng log · 90 ngày gần nhất</h3>
    <?php if (!$thongKeLoi): ?>
        <p class="muted">Không có dòng lỗi nào trong 90 ngày qua. (Hoặc máy chủ này chưa có các bảng log đó.)</p>
    <?php else: ?>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Loại</th><th>Nghĩa là gì</th><th class="num">Số dòng</th><th>Gần nhất</th><th></th></tr></thead>
            <tbody>
            <?php foreach ($thongKeLoi as $t): ?>
                <tr>
                    <td><code><?= e((string)$t['aclog']) ?></code></td>
                    <td class="muted small"><?= e($LOAI_LOI[(string)$t['aclog']] ?? '') ?></td>
                    <td class="num"><?= so($t['n']) ?></td>
                    <td class="nowrap muted small"><?= e((string)$t['lan_cuoi']) ?></td>
                    <td><a class="btn btn-xam btn-nho"
                           href="log.php?bang=<?= e(urlencode((string)$t['bang'])) ?>&amp;loc2=<?= e(urlencode((string)$t['aclog'])) ?>">Xem</a></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
</div>

<div class="card">
    <h3>File log của server</h3>
    <p class="muted small" style="margin-top:0;">
        Đọc từ <code><?= e(SERVER_DIR) ?></code>. Chỉ đọc đúng danh sách tên file viết sẵn trong mã nguồn —
        trang không nhận tên file từ đường dẫn, nên không ai lần ra <code>config.local.php</code> được.
    </p>
    <div class="tbl-wrap">
    <table class="tbl">
        <thead><tr><th>File</th><th>Nội dung</th><th class="num">Cỡ</th><th>Sửa lần cuối</th><th></th></tr></thead>
        <tbody>
        <?php foreach (fileLog() as $ten => $mota):
            $dd = rtrim(SERVER_DIR, '/\\') . '/' . $ten;
            $co = is_file($dd); ?>
            <tr>
                <td class="nowrap"><code><?= e($ten) ?></code></td>
                <td class="muted small"><?= e($mota) ?></td>
                <td class="num"><?= $co ? e(so(filesize($dd))) : '<span class="muted">—</span>' ?></td>
                <td class="nowrap muted small"><?= $co ? e(date('Y-m-d H:i:s', filemtime($dd))) : '' ?></td>
                <td><?php if ($co && is_readable($dd)): ?>
                        <a class="btn btn-xam btn-nho" href="chandoan.php?file=<?= e(urlencode($ten)) ?>#xemfile">Xem cuối file</a>
                    <?php elseif ($co): ?>
                        <span class="muted small">không có quyền đọc</span>
                    <?php else: ?>
                        <span class="muted small">chưa có</span>
                    <?php endif; ?></td>
            </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
    </div>

    <?php if ($luuHong): ?>
        <h3 style="margin-top:22px;">Vết lưu nhân vật thất bại</h3>
        <p class="muted small" style="margin-top:0;">
            Mỗi file dưới đây mang đúng <strong>tên một nhân vật</strong>. Server tạo nó khi
            đường lưu chính của nhân vật đó ném lỗi (<code>DatabaseNhanVat.java:770</code>) —
            thường là vết duy nhất còn lại của sự cố mất đồ / tụt cấp sau khi thoát game.
            <code>savechar.txt</code> ở bảng trên <em>không</em> phủ đường lưu này.
        </p>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Nhân vật</th><th class="num">Cỡ</th><th>Sửa lần cuối</th><th></th></tr></thead>
            <tbody>
            <?php foreach ($luuHong as $ten => $suaLuc):
                $dd = rtrim(SERVER_DIR, '/\\') . '/' . $ten; ?>
                <tr>
                    <td class="nowrap"><code><?= e($ten) ?></code></td>
                    <td class="num"><?= e(so(filesize($dd) ?: 0)) ?></td>
                    <td class="nowrap muted small"><?= e(date('Y-m-d H:i:s', $suaLuc)) ?></td>
                    <td><?php if (is_readable($dd)): ?>
                            <a class="btn btn-xam btn-nho" href="chandoan.php?file=<?= e(urlencode($ten)) ?>#xemfile">Xem cuối file</a>
                        <?php else: ?>
                            <span class="muted small">không có quyền đọc</span>
                        <?php endif; ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
</div>

<?php if ($noiDungFile !== null): ?>
    <div class="card" id="xemfile">
        <h3>Cuối file <code><?= e($xemFile) ?></code></h3>
        <p class="muted small" style="margin-top:0;">
            Cỡ <?= e(so($noiDungFile['co'])) ?> byte · sửa lần cuối <?= e(date('Y-m-d H:i:s', $noiDungFile['sua_luc'])) ?>
            <?= $noiDungFile['cat'] ? ' · chỉ hiện khoảng 64 KB cuối' : '' ?>
        </p>
        <pre class="ma-sql" style="max-height:520px;"><?= e($noiDungFile['noi_dung']) ?></pre>
    </div>
<?php elseif ($xemFile !== ''): ?>
    <div class="flash flash-error">Không đọc được file <?= e($xemFile) ?>.</div>
<?php endif; ?>

<div class="card">
    <h3>Lỗi của chính trang quản trị</h3>
    <?php if (!coBangNhatKy()): ?>
        <p class="muted">Chưa có bảng <code>web_admin_log</code> nên lỗi web chỉ vào log lỗi của PHP.</p>
    <?php elseif (!$loiWeb): ?>
        <p class="muted">Chưa có lỗi nào được ghi. Tốt.</p>
    <?php else: ?>
        <div class="tbl-wrap">
        <table class="tbl">
            <thead><tr><th>Lúc</th><th>Ai gặp</th><th>Ở đâu</th><th>Lỗi</th></tr></thead>
            <tbody>
            <?php foreach ($loiWeb as $r): ?>
                <tr>
                    <td class="nowrap muted small"><?= e((string)$r['thoi_gian']) ?></td>
                    <td class="nowrap"><?= e((string)$r['admin_user']) ?: '<span class="muted">chưa đăng nhập</span>' ?></td>
                    <td class="nowrap"><code><?= e((string)$r['doi_tuong']) ?></code></td>
                    <td class="info"><?= e((string)$r['chi_tiet']) ?></td>
                </tr>
            <?php endforeach; ?>
            </tbody>
        </table>
        </div>
    <?php endif; ?>
</div>
<?php adminFoot(); ?>
