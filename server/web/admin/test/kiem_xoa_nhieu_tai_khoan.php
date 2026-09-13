<?php
if (PHP_SAPI !== 'cli') { http_response_code(403); exit; }
require_once __DIR__ . '/../inc/xoa_tai_khoan.php';
const LENH_LOAI_XOA_NHAN_VAT = 'xoa_nhan_vat';
function laTaiKhoanAdmin(string $ten): bool { return strtolower($ten) === 'admin'; }
function ghiNhatKy(...$args): void {}
function taoBangLenhNhanVat(PDO $db): void {}
function guiLenhXoaNhanVat(PDO $db, int $id, string $ten, string $admin): int {
    $db->prepare('INSERT INTO web_lenh_nhan_vat (char_id, loai, trang_thai) VALUES (?, ?, 0)')->execute([$id, LENH_LOAI_XOA_NHAN_VAT]);
    return (int)$db->lastInsertId();
}
$n = 0;
function check(bool $ok, string $name): void {
    global $n;
    if (!$ok) throw new RuntimeException($name);
    ++$n;
    echo "OK: $name\n";
}
check(idsTaiKhoanDaChon(['2', '2', '3']) === [2, 3], 'loai ID trung');
foreach ([null, [], ['-1'], ['1 OR 1=1'], [['2']], ['0'], array_fill(0, 51, '1')] as $bad) {
    try { idsTaiKhoanDaChon($bad); throw new RuntimeException('chap nhan ID sai'); }
    catch (InvalidArgumentException $e) { check(true, 'tu choi dau vao sai'); }
}
$a = new PDO('sqlite::memory:');
$g = new PDO('sqlite::memory:');
foreach ([$a, $g] as $db) $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
$a->exec('CREATE TABLE team_user (id INTEGER PRIMARY KEY, username TEXT)');
$a->exec("INSERT INTO team_user VALUES (1,'admin'),(2,'empty'),(3,'playing'),(4,'unselected'),(5,'operator')");
$g->exec('CREATE TABLE tob_char (id INTEGER PRIMARY KEY, charname TEXT, userId INTEGER)');
$g->exec("INSERT INTO tob_char VALUES (30,'hero',3)");
$g->exec('CREATE TABLE web_lenh_nhan_vat (id INTEGER PRIMARY KEY, char_id INTEGER, loai TEXT, trang_thai INTEGER)');
$g->exec('CREATE TABLE `5h_active` (username TEXT, userID INTEGER)');
$g->exec("INSERT INTO `5h_active` VALUES ('empty',2),('playing',3),('unselected',4)");
$r = xoaNhieuTaiKhoan($a, $g, [1,2,3,5,99], 'operator');
check($r['xoa'] === ['empty'], 'xoa tai khoan khong con nhan vat');
check($r['cho'] === ['playing'], 'tai khoan con nhan vat phai cho');
check(count($r['bo_qua']) === 3, 'bo qua admin, chinh minh va ID khong ton tai');
check((int)$a->query('SELECT COUNT(*) FROM team_user WHERE id IN (1,4,5)')->fetchColumn() === 3, 'giu admin va tai khoan khong chon');
check((int)$g->query('SELECT COUNT(*) FROM `5h_active` WHERE userID=2')->fetchColumn() === 0, 'xoa kich hoat cung tai khoan');
xoaNhieuTaiKhoan($a, $g, [3], 'operator');
check((int)$g->query('SELECT COUNT(*) FROM web_lenh_nhan_vat')->fetchColumn() === 1, 'bam lai khong nhan doi lenh dang cho');
check((int)$a->query('SELECT COUNT(*) FROM team_user WHERE id=3')->fetchColumn() === 1, 'giu tai khoan khi server chua xoa nhan vat');
$g->exec('DELETE FROM tob_char WHERE id=30');
$r = xoaNhieuTaiKhoan($a, $g, [3], 'operator');
check($r['xoa'] === ['playing'], 'hoan tat sau khi server xoa nhan vat');
check((int)$a->query('SELECT COUNT(*) FROM team_user WHERE id=3')->fetchColumn() === 0, 'tai khoan da xoa that');
$r = xoaNhieuTaiKhoan($a, $g, [3], 'operator');
check(count($r['bo_qua']) === 1 && !$r['xoa'], 'thu lai tai khoan da xoa');
echo "DAT: $n kiem tra\n";
