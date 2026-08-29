<?php
/**
 * Đọc thông tin database thẳng từ file cấu hình của game server.
 *
 * Thông tin đó đã nằm sẵn ở hai chỗ mà server dùng hằng ngày:
 *
 *     <SERVER_DIR>/server.ini              db.host, db.name, db.user, db.password
 *     <SERVER_DIR>/loginServer/server.ini  db.url (chứa tên DB tài khoản), db.user, db.password
 *
 * Chép tay sang config.local.php chỉ tạo thêm một bản sao để lệch nhau: đổi mật
 * khẩu database là website chết mà không ai nhớ tại sao.
 *
 * File này CỐ Ý không phụ thuộc gì cả (không config, không session, không PDO)
 * để kiểm thử được — xem web/admin/test/kiem_doc_ini.php.
 */

if (!function_exists('kpahDocIni')) {
    /**
     * Đọc file .ini kiểu `khoa=gia tri` thành mảng.
     * Trả mảng rỗng nếu file không tồn tại hoặc không có quyền đọc — hai file kia
     * thường để chmod 640 chown kpah:kpah, mà PHP có thể chạy dưới người dùng khác.
     */
    function kpahDocIni(string $duongDan): array
    {
        if (!is_file($duongDan) || !is_readable($duongDan)) {
            return [];
        }
        $dong = @file($duongDan, FILE_IGNORE_NEW_LINES);
        if ($dong === false) {
            return [];
        }
        $ra = [];
        foreach ($dong as $d) {
            $d = trim($d);
            if ($d === '' || $d[0] === '#' || $d[0] === ';') {
                continue;
            }
            $viTri = strpos($d, '=');
            if ($viTri === false) {
                continue;
            }
            $ra[trim(substr($d, 0, $viTri))] = trim(substr($d, $viTri + 1));
        }
        return $ra;
    }
}

if (!function_exists('kpahGoiYLoiKetNoi')) {
    /**
     * Đoán vì sao kết nối database hỏng, để thông báo lỗi nói được điều gì đó
     * hữu ích thay vì chỉ ném ra mã 1045.
     *
     * Trường hợp hay gặp nhất, và cũng khó đoán nhất: server.ini CÓ ở đó nhưng
     * PHP không có quyền đọc (file là chmod 640 chown kpah:kpah, còn PHP chạy
     * dưới www-data). Lúc đó config.php lặng lẽ rơi xuống mặc định root / mật
     * khẩu rỗng, và MySQL trả về "Access denied for user 'root'" — một thông
     * báo chẳng liên quan gì tới nguyên nhân thật.
     *
     * @return string chuỗi rỗng nếu không đoán được gì
     */
    function kpahGoiYLoiKetNoi(): string
    {
        if (!defined('SERVER_DIR')) {
            return '';
        }
        $f = rtrim(SERVER_DIR, "/\\") . '/server.ini';

        if (is_file($f) && !is_readable($f)) {
            return 'Có file ' . $f . ' nhưng PHP không có quyền đọc, nên đang dùng '
                 . 'thông tin mặc định. Cho người chạy PHP đọc được file đó '
                 . '(usermod -a -G kpah www-data rồi khởi động lại php-fpm), '
                 . 'hoặc khai DB_USER/DB_PASS thẳng trong web/config.local.php.';
        }
        if (!is_file($f) && defined('DB_USER') && DB_USER === 'root'
            && defined('DB_PASS') && DB_PASS === '') {
            return 'Không thấy ' . $f . ' và config.local.php cũng không khai '
                 . 'DB_USER/DB_PASS, nên đang dùng mặc định root với mật khẩu rỗng. '
                 . 'Khai thông tin thật trong web/config.local.php.';
        }
        return '';
    }
}

if (!function_exists('kpahCauHinhDb')) {
    /**
     * Rút thông tin kết nối từ hai file ini.
     *
     * Khoá nào không tìm thấy thì trả null — bên gọi tự quyết định giá trị thay thế.
     *
     * @return array{host:?string, port:?int, user:?string, pass:?string,
     *               game:?string, account:?string, account_user:?string, account_pass:?string}
     */
    function kpahCauHinhDb(string $serverDir): array
    {
        $goc   = rtrim($serverDir, "/\\");
        $game  = kpahDocIni($goc . '/server.ini');
        $login = kpahDocIni($goc . '/loginServer/server.ini');

        $ra = ['host' => null, 'port' => null, 'user' => null, 'pass' => null,
               'game' => null, 'account' => null, 'account_user' => null, 'account_pass' => null];

        // db.host viết dạng "localhost:3306" — tách cổng ra.
        //
        // GIỮ NGUYÊN chữ "localhost", đừng đổi thành 127.0.0.1: MySQL phân quyền
        // theo máy khách, và 'kpah'@'localhost' với 'kpah'@'127.0.0.1' là HAI
        // quyền khác nhau. Kết nối qua socket luôn được tính là 'localhost' —
        // đúng cái quyền mà game server đang dùng được.
        if (isset($game['db.host']) && $game['db.host'] !== '') {
            $host = $game['db.host'];
            if (strpos($host, ':') !== false) {
                [$host, $cong] = explode(':', $host, 2);
                if (ctype_digit($cong)) {
                    $ra['port'] = (int)$cong;
                }
            }
            if ($host !== '') {
                $ra['host'] = $host;
            }
        }
        if (isset($game['db.user']) && $game['db.user'] !== '') {
            $ra['user'] = $game['db.user'];
        }
        if (isset($game['db.password'])) {
            $ra['pass'] = $game['db.password'];
        }
        if (isset($game['db.name']) && $game['db.name'] !== '') {
            $ra['game'] = $game['db.name'];
        }

        // Login server giữ tên DB tài khoản trong chuỗi JDBC:
        //     jdbc:mysql://localhost:3306/account?useUnicode=true
        if (isset($login['db.url'])
            && preg_match('~/([A-Za-z0-9_]+)(?:\?|$)~', $login['db.url'], $khop)) {
            $ra['account'] = $khop[1];
        }
        if (isset($login['db.user']) && $login['db.user'] !== '') {
            $ra['account_user'] = $login['db.user'];
            $ra['account_pass'] = $login['db.password'] ?? '';
        }

        return $ra;
    }
}
