<?php
/**
 * Doi chieu MOI bang/cot ma trang admin dung voi cau truc THAT trong cac ban
 * dump schema. Bat loi go nham ten cot ma khong can chay MySQL.
 *
 * Nguon cau truc:
 *   - kpah_db  : sql/gamedb7_clean.sql
 *   - account  : loginServer/account.sql
 *   - them     : sql/them_bang_web_admin_log.sql, sql/them_bang_web_chan_spam.sql,
 *                vps/db_new_server.sql (tob_nap_moc)
 */
// Chỉ chạy bằng dòng lệnh. Thư mục này nằm DƯỚI web root (/var/www/html/kpah
// là symlink tới /opt/kpah/web) nên không chặn thì ai trên Internet cũng gọi
// được: lộ đường dẫn tệp, tên bảng, tên cột, và tốn CPU máy chủ. Apache ở đây
// đặt AllowOverride None nên .htaccess bị bỏ qua — chốt phải nằm trong chính
// tệp PHP thì mới có tác dụng.
if (PHP_SAPI !== 'cli') {
    http_response_code(403);
    exit("File nay chi chay bang dong lenh, khong mo qua trinh duyet.\n");
}

$GOC = __DIR__ . '/../../../';

/** Doc cac khoi CREATE TABLE thanh [ten_bang => [cot,...]] */
function docSchema(string $sql): array
{
    $bang = [];
    // Ten bang co the co tien to database: CREATE TABLE `kpah_db`.`tob_nap_moc` (...)
    if (preg_match_all('/CREATE TABLE (?:IF NOT EXISTS )?(?:`[^`]+`\.)?`([^`]+)`\s*\((.*?)\n\)\s*(?:ENGINE|;)/s', $sql, $m, PREG_SET_ORDER)) {
        foreach ($m as $khop) {
            $ten = $khop[1];
            $than = $khop[2];
            $cot = [];
            foreach (explode("\n", $than) as $dong) {
                if (preg_match('/^\s*`([^`]+)`\s+[a-zA-Z]/', $dong, $c)) {
                    $cot[] = strtolower($c[1]);
                }
            }
            if ($cot) {
                $bang[strtolower($ten)] = $cot;
            }
        }
    }
    return $bang;
}

$schema = [];
foreach ([
    'sql/gamedb7_clean.sql',
    'sql/them_bang_web_admin_log.sql',
    'sql/them_bang_web_chan_spam.sql',
    'sql/them_bang_tob_log_online.sql',  // server game tu tao luc khoi dong, file nay de doi chieu
    'vps/db_new_server.sql',      // tob_nap_moc dinh nghia o PHAN 2I trong file nay
] as $f) {
    $schema += docSchema(file_get_contents($GOC . $f));
}
// CAI BAY: kpah_db CUNG co mot bang ten team_user, nhung no la bang KHAC HAN
// (chi map provider/agent). Trang admin luon doc/ghi team_user o DB `account`,
// nen phai GHI DE chu khong phai `+=` (toan tu + giu nguyen khoa da co).
$schemaAcc = docSchema(file_get_contents($GOC . 'loginServer/account.sql'));
$schema['team_user'] = $schemaAcc['team_user'];

echo "Doc duoc " . count($schema) . " bang tu schema.\n";
foreach (['team_user', 'tob_char', 'giftcode', 'giftcode_log', '5h_active', 'web_admin_log', 'web_chan_spam', 'tob_nap_moc'] as $b) {
    printf("  %-16s %s\n", $b, isset($schema[$b]) ? count($schema[$b]) . ' cot' : '*** KHONG THAY ***');
}

// ---------------------------------------------------------------------------
$TU_KHOA = array_flip(explode(' ', strtolower(
    'select from where and or not in is null as order by group having limit offset '
  . 'insert into values update set delete distinct count max min sum avg case when '
  . 'then else end asc desc left right join on inner outer union all exists like '
  . 'date date_sub date_add date_format timestampdiff round now curdate '
  . 'interval second minute hour day week month year '
  . 'password concat replace length if ifnull coalesce true false between '
  . 'information_schema tables columns table_schema table_name column_name data_type character_maximum_length'
)));

$loi = 0;
$soCau = 0;
$FILE = [
    'web/admin/index.php', 'web/admin/taikhoan.php', 'web/admin/nhanvat.php',
    'web/admin/giftcode.php', 'web/admin/log.php', 'web/admin/nhatky.php',
    'web/admin/chandoan.php', 'web/admin/inc/boot.php', 'web/admin/inc/vatpham.php',
    'web/inc/functions.php', 'web/register.php', 'web/login.php', 'web/account.php',
];

foreach ($FILE as $f) {
    $php = file_get_contents($GOC . $f);

    // Lay cac chuoi co chua tu khoa SQL (ca nhay don, nhay kep, va heredoc kieu chuoi nhieu dong)
    preg_match_all("/'((?:[^'\\\\]|\\\\.)*)'|\"((?:[^\"\\\\]|\\\\.)*)\"/s", $php, $m, PREG_SET_ORDER);
    foreach ($m as $k) {
        $s = $k[1] !== '' ? $k[1] : ($k[2] ?? '');
        if (!preg_match('/\b(SELECT|UPDATE|INSERT INTO|DELETE FROM)\b/i', $s)) {
            continue;
        }
        // Cau SQL that o day deu thuan ASCII. Chuoi co dau tieng Viet la van xuoi
        // giai thich cho nguoi doc, vo tinh chua chu "UPDATE" hay "SELECT".
        if (preg_match('/[^\x00-\x7F]/', $s)) {
            continue;
        }
        // Bang he thong cua MySQL, khong co trong ban dump schema.
        if (stripos($s, 'information_schema') !== false) {
            continue;
        }
        $soCau++;

        // Bang duoc nhac toi trong cau nay
        preg_match_all('/\b(?:FROM|JOIN|UPDATE|INSERT INTO|DELETE FROM)\s+`?([A-Za-z0-9_]+)`?(?:\.`?([A-Za-z0-9_]+)`?)?/i', $s, $tb, PREG_SET_ORDER);
        $bangCau = [];
        foreach ($tb as $t) {
            // dang db.bang -> lay ve phai
            $ten = strtolower((isset($t[2]) && $t[2] !== '') ? $t[2] : $t[1]);
            if ($ten === 'kpah_db' || $ten === 'account') continue;
            $bangCau[] = $ten;
        }
        // bien PHP thay cho ten bang (log.php) -> bo qua cau nay
        if (preg_match('/(FROM|UPDATE|INTO)\s+`?\$/i', $s)) {
            continue;
        }

        $cotHopLe = [];
        $bangThieu = [];
        foreach ($bangCau as $b) {
            if (!isset($schema[$b])) {
                $bangThieu[] = $b;
            } else {
                $cotHopLe = array_merge($cotHopLe, $schema[$b]);
            }
        }
        if ($bangThieu) {
            $loi++;
            echo "\n*** $f: bang khong co trong schema: " . implode(', ', $bangThieu) . "\n    $s\n";
            continue;
        }
        if (!$cotHopLe) {
            continue;
        }
        $cotHopLe = array_flip($cotHopLe);

        // Bo hai thu khong phai dinh danh truoc khi soi:
        //   - chuoi ky tu trong SQL   ('dang_nhap_that_bai')
        //   - bien PHP duoc noi vao   ($COT, $where, $lay...)
        $soi = preg_replace("/'[^']*'/", ' ', $s);
        $soi = preg_replace('/\$[A-Za-z_][A-Za-z0-9_]*/', ' ', $soi);

        // Cac dinh danh xuat hien trong cau
        preg_match_all('/`([A-Za-z0-9_]+)`|\b([A-Za-z_][A-Za-z0-9_]*)\b/', $soi, $id, PREG_SET_ORDER);
        foreach ($id as $i) {
            $ten = strtolower($i[1] !== '' ? $i[1] : $i[2]);
            if (isset($TU_KHOA[$ten]) || in_array($ten, $bangCau, true)) continue;
            if (ctype_digit($ten)) continue;
            if (isset($cotHopLe[$ten])) continue;
            // bi danh (AS x) -> bo qua
            if (preg_match('/\bAS\s+`?' . preg_quote($ten, '/') . '`?\b/i', $s)) continue;
            $loi++;
            echo "\n*** $f: '$ten' khong phai cot cua [" . implode(', ', $bangCau) . "]\n    $s\n";
        }
    }
}

echo "\n===================================\n";
echo "Da soi $soCau cau SQL. So van de: $loi\n";
exit($loi > 0 ? 1 : 0);
