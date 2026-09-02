<?php
/**
 * GUI LENH CHO SERVER GAME qua bang kpah_db.web_lenh_nhan_vat (xem src/data/LenhWeb.java).
 *
 * VI SAO KHONG GHI THANG tob_char NUA. Nhan vat dang online giu ban cua no trong RAM cua tien
 * trinh game; lan tu luu ke tiep ghi de lai dong web vua sua - mat hang thay doi, khong bao gi.
 * Rieng cap do con te hon: luc dang nhap server doc cap/exp tu cot pInfo, KHONG doc lastLv/xp,
 * nen ghi hai cot do chua bao gio co tac dung. Web khong nhin thay RAM cua tien trinh game, nen
 * thay vi doan mo, web CHI chen mot dong lenh; server doc moi giay va tu xu ly.
 *
 * HAI LOAI LENH:
 *   dat_cap_do   : cap_do + phan_nghin. Online -> cong exp qua duong len cap tu nhien roi luu
 *                  ngay (1). Offline -> nam cho (4), server cong ngay khi nhan vat vao game.
 *                  Chi NANG cap; ha cap bi tu choi (3).
 *   xoa_vat_pham : tham_so = "<cot>><mau>" (cot equip/inven/bag, mau = dung chuoi tho cua mon
 *                  do trong cot). Online -> tim dung mon khop tung ky tu trong RAM, go ra, luu
 *                  ngay (1). Offline -> server xoa mau khoi cot trong CSDL, roi de dong o
 *                  "cho vao game" (4) de khi nhan vat dang nhap se doi chieu lai RAM mot lan
 *                  (dong cua so dua "dang nap nhan vat thi web sua CSDL").
 * Ham choKetQuaLenh() doi toi da vai giay roi doc lai de bao cho nguoi bam. Server dang tat
 * thi dong lenh van nam do (trang_thai 0) va duoc xu ly khi server chay lai - khong mat lenh.
 * Nguoi quan tri huy duoc dong con cho (trang_thai 5), server bo qua dong da huy.
 *
 * DON VI %: web nhap phan tram (buoc 0,1 - toi da 99,9), bang luu PHAN NGHIN (0..999) - cung
 * don vi voi real.LevelDetail.percent ben Java, de server tinh exp bang dung cong thuc cua no
 * (expMain[cap-1] + expRequire[cap] * phanNghin / 1000). Web KHONG tinh exp nua. Moi cho hien
 * % cho nguoi dung deu lay tu phan nghin da luu, de so bao ra dung bang so server dung.
 */

const LENH_BANG = 'web_lenh_nhan_vat';
const LENH_LOAI_DAT_CAP_DO = 'dat_cap_do';
const LENH_LOAI_XOA_VAT_PHAM = 'xoa_vat_pham';

const LENH_CHO = 0;
const LENH_XONG_ONLINE = 1;
const LENH_XONG_OFFLINE = 2;   // ma cu, server khong tao moi nua; giu de doc dong cu
const LENH_LOI = 3;
const LENH_CHO_DANG_NHAP = 4;
const LENH_HUY = 5;

/** Doi server toi da bay nhieu giay. Server doc bang moi 1 giay nen 4 giay la du roi. */
const LENH_CHO_TOI_DA_GIAY = 4.0;

/** Cot vat pham web duoc phep xoa - ten cot ghep thang vao SQL nen PHAI qua danh sach nay. */
const LENH_COT_VAT_PHAM = ['equip', 'inven', 'bag'];

/** % (0..99.9) -> phan nghin (0..999), lam tron, kep vao bien. */
function phanTramRaPhanNghin(float $phanTram): int
{
    $pn = (int)round($phanTram * 10);
    return max(0, min(999, $pn));
}

/** Phan nghin -> chuoi % gon ("12,5%", "0%") de hien cho nguoi dung. */
function phanNghinRaChuoi(int $phanNghin): string
{
    $pn = max(0, min(999, $phanNghin));
    if ($pn % 10 === 0) {
        return (string)intdiv($pn, 10) . '%';
    }
    return str_replace('.', ',', (string)($pn / 10)) . '%';
}

/** Ten trang thai de hien trong bang. */
function tenTrangThaiLenh(int $trangThai): string
{
    switch ($trangThai) {
        case LENH_CHO:           return 'chờ server';
        case LENH_XONG_ONLINE:   return 'đã xong (online)';
        case LENH_XONG_OFFLINE:  return 'đã ghi CSDL (cách cũ)';
        case LENH_LOI:           return 'lỗi';
        case LENH_CHO_DANG_NHAP: return 'chờ vào game';
        case LENH_HUY:           return 'đã huỷ';
        default:                 return 'trạng thái ' . $trangThai;
    }
}

/** Ghep tham so cho lenh xoa vat pham. ">" khong bao gio nam trong mot mau (no la dau ngan cac mau). */
function ghepThamSoXoaVatPham(string $cot, string $mau): string
{
    return $cot . '>' . $mau;
}

/** Tach tham so "<cot>><mau>" -> [cot, mau]; sai dinh dang -> null. */
function tachThamSoXoaVatPham(?string $thamSo): ?array
{
    $thamSo = (string)$thamSo;
    $vt = strpos($thamSo, '>');
    if ($vt === false) {
        return null;
    }
    $cot = substr($thamSo, 0, $vt);
    $mau = substr($thamSo, $vt + 1);
    if (!in_array($cot, LENH_COT_VAT_PHAM, true) || $mau === '') {
        return null;
    }
    return [$cot, $mau];
}

/** Mo ta ngan mot dong lenh cho bang "Lenh da gui": "cấp 50 (12,5%)" / "xoá đồ #123 +5 (inven)". */
function moTaYeuCauLenh(array $l): string
{
    if ((string)$l['loai'] === LENH_LOAI_XOA_VAT_PHAM) {
        $ts = tachThamSoXoaVatPham((string)($l['tham_so'] ?? ''));
        if ($ts === null) {
            return 'xoá vật phẩm (tham số hỏng)';
        }
        $moTa = 'xoá vật phẩm';
        if (function_exists('docChuoiVatPham')) {
            foreach (docChuoiVatPham($ts[1]) as $vp) {
                $moTa = 'xoá #' . (int)$vp['template'] . ((int)$vp['plus'] > 0 ? ' +' . (int)$vp['plus'] : '');
                break;
            }
        }
        return $moTa . ' (' . $ts[0] . ')';
    }
    return 'cấp ' . (int)$l['cap_do'] . ((int)$l['phan_nghin'] > 0 ? ' (' . phanNghinRaChuoi((int)$l['phan_nghin']) . ')' : '');
}

/** Bang lenh da co chua? Nho trong mot lan chay. */
function coBangLenh(PDO $db): bool
{
    static $co = null;
    if ($co === null) {
        try {
            $db->query('SELECT 1 FROM web_lenh_nhan_vat LIMIT 1');
            $co = true;
        } catch (Throwable $ex) {
            $co = false;
        }
    }
    return $co;
}

/**
 * Cung dinh nghia voi LenhWeb.taoBang() va sql/them_bang_web_lenh_nhan_vat.sql. Chi CREATE khi
 * bang chua co: nguoi dung MySQL cua web co the khong co quyen CREATE, luc do bang da duoc
 * server tao san thi van phai chay duoc. Bang co san tu ban dau (chua co cot tham_so) thi them
 * cot - server cung tu them luc khoi dong, ai chay truoc cung duoc.
 */
function taoBangLenhNhanVat(PDO $db): void
{
    if (coBangLenh($db)) {
        damBaoCotThamSo($db);
        return;
    }
    $db->exec(
        'CREATE TABLE IF NOT EXISTS `' . LENH_BANG . '` ('
        . '`id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,'
        . '`loai`       VARCHAR(30)  NOT NULL,'
        . '`char_id`    INT          NOT NULL,'
        . '`charname`   VARCHAR(50)  NOT NULL DEFAULT \'\','
        . '`cap_do`     INT          NOT NULL DEFAULT 0,'
        . '`phan_nghin` INT          NOT NULL DEFAULT 0,'
        . '`tham_so`    TEXT         NULL,'
        . '`admin_user` VARCHAR(50)  NOT NULL DEFAULT \'\','
        . '`trang_thai` TINYINT      NOT NULL DEFAULT 0,'
        . '`ket_qua`    VARCHAR(255) NOT NULL DEFAULT \'\','
        . '`tao_luc`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,'
        . '`xu_ly_luc`  DATETIME     NULL,'
        . 'PRIMARY KEY (`id`),'
        . 'KEY `idx_trang_thai` (`trang_thai`)'
        . ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
    );
}

/** Bang tao o ban dau chua co cot tham_so -> them. Chi chay ALTER khi that su thieu. */
function damBaoCotThamSo(PDO $db): void
{
    static $daKiem = false;
    if ($daKiem) {
        return;
    }
    $daKiem = true;
    $co = $db->query('SHOW COLUMNS FROM `' . LENH_BANG . '` LIKE \'tham_so\'')->fetch();
    if (!$co) {
        $db->exec('ALTER TABLE `' . LENH_BANG . '` ADD COLUMN `tham_so` TEXT NULL AFTER `phan_nghin`');
    }
}

/** Chen lenh "dat cap do". Tra ve id dong lenh de doi ket qua. */
function guiLenhCapDo(PDO $db, int $charId, string $charname, int $capDo, int $phanNghin, string $adminUser): int
{
    taoBangLenhNhanVat($db);
    $st = $db->prepare(
        'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, phan_nghin, admin_user)
         VALUES (?, ?, ?, ?, ?, ?)'
    );
    $st->execute([
        LENH_LOAI_DAT_CAP_DO,
        $charId,
        substr($charname, 0, 50),
        $capDo,
        max(0, min(999, $phanNghin)),
        substr($adminUser, 0, 50),
    ]);
    return (int)$db->lastInsertId();
}

/** Chen lenh "xoa vat pham" (mau = chuoi tho dung cua mon do trong cot). Tra ve id dong lenh. */
function guiLenhXoaVatPham(PDO $db, int $charId, string $charname, string $cot, string $mau, string $adminUser): int
{
    if (!in_array($cot, LENH_COT_VAT_PHAM, true)) {
        throw new InvalidArgumentException('Cột vật phẩm không hợp lệ.');
    }
    if (trim($mau) === '' || strpos($mau, '>') !== false) {
        throw new InvalidArgumentException('Chuỗi vật phẩm không hợp lệ.');
    }
    taoBangLenhNhanVat($db);
    $st = $db->prepare(
        'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, tham_so, admin_user)
         VALUES (?, ?, ?, ?, ?)'
    );
    $st->execute([
        LENH_LOAI_XOA_VAT_PHAM,
        $charId,
        substr($charname, 0, 50),
        ghepThamSoXoaVatPham($cot, $mau),
        substr($adminUser, 0, 50),
    ]);
    return (int)$db->lastInsertId();
}

/** Mot dong lenh (id, trang_thai, ket_qua), hoac null neu khong co. */
function docLenh(PDO $db, int $id): ?array
{
    $st = $db->prepare('SELECT id, trang_thai, ket_qua FROM web_lenh_nhan_vat WHERE id = ? LIMIT 1');
    $st->execute([$id]);
    $r = $st->fetch();
    return $r ?: null;
}

/**
 * Doi server xu ly, toi da $toiDaGiay giay (hoi lai moi 250 ms). Tra ve dong lenh o trang
 * thai cuoi cung doc duoc - van co the la LENH_CHO neu server khong tra loi kip.
 */
function choKetQuaLenh(PDO $db, int $id, float $toiDaGiay = LENH_CHO_TOI_DA_GIAY): ?array
{
    $han = microtime(true) + $toiDaGiay;
    $r = null;
    do {
        $r = docLenh($db, $id);
        if ($r !== null && (int)$r['trang_thai'] !== LENH_CHO) {
            return $r;
        }
        usleep(250000);
    } while (microtime(true) < $han);
    return $r;
}

/** Cac lenh gan day cua mot nhan vat, moi nhat truoc. Bang chua co thi tra ve rong. */
function docLenhCuaNhanVat(PDO $db, int $charId, int $toiDa = 10): array
{
    if (!coBangLenh($db)) {
        return [];
    }
    damBaoCotThamSo($db);
    $st = $db->prepare(
        'SELECT id, loai, cap_do, phan_nghin, tham_so, admin_user, trang_thai, ket_qua, tao_luc, xu_ly_luc
           FROM web_lenh_nhan_vat WHERE char_id = ? ORDER BY id DESC LIMIT ' . max(1, $toiDa)
    );
    $st->execute([$charId]);
    return $st->fetchAll() ?: [];
}

/**
 * Huy mot dong con cho (LENH_CHO / LENH_CHO_DANG_NHAP). Tra ve true neu huy duoc; false neu
 * server da xu ly xong truoc do (khong huy duoc ket qua da co).
 */
function huyLenh(PDO $db, int $id, string $adminUser): bool
{
    $st = $db->prepare(
        'UPDATE web_lenh_nhan_vat SET trang_thai = ?, ket_qua = ?, xu_ly_luc = NOW()
          WHERE id = ? AND trang_thai IN (?, ?)'
    );
    $st->execute([LENH_HUY, substr('huy boi ' . $adminUser, 0, 255), $id, LENH_CHO, LENH_CHO_DANG_NHAP]);
    return $st->rowCount() > 0;
}

/**
 * Cau bao cho nguoi bam, kem loai thong bao ('success' / 'warning' / 'error').
 * $muc = mo ta viec vua yeu cau, vd 'cấp 50 (12,5%)' hay 'xoá "Hoả kỳ lân +3"'.
 * Thuan tuy, khong dong CSDL - de kiem thu duoc khong can MySQL.
 */
function moTaKetQuaLenh(?array $r, string $ten, string $muc): array
{
    if ($r === null) {
        return ['error', 'Không đọc lại được lệnh vừa gửi cho ' . $ten . '. Kiểm tra bảng web_lenh_nhan_vat.'];
    }
    $kq = trim((string)($r['ket_qua'] ?? ''));
    switch ((int)$r['trang_thai']) {
        case LENH_XONG_ONLINE:
            return ['success', 'Đã thực hiện NGAY cho ' . $ten . ' đang online: ' . $muc
                . ($kq !== '' ? ' (' . $kq . ')' : '') . '. Server đã lưu lại, không bị ghi đè nữa.'];
        case LENH_CHO_DANG_NHAP:
            return ['success', $ten . ' đang offline. ' . ($kq !== '' ? ucfirst($kq) . '. ' : '')
                . 'Lệnh ' . $muc . ' sẽ được server đối chiếu/thực hiện NGAY KHI nhân vật vào game. '
                . 'Không cần bấm lại — có thể huỷ ở bảng "Lệnh đã gửi" bên dưới nếu đổi ý.'];
        case LENH_XONG_OFFLINE:
            return ['success', $ten . ' đang offline, đã ghi ' . $muc . ' vào CSDL (cách cũ).'];
        case LENH_LOI:
            return ['error', 'Server từ chối lệnh ' . $muc . ' cho ' . $ten . ': ' . ($kq !== '' ? $kq : 'không rõ lý do') . '.'];
        case LENH_HUY:
            return ['warning', 'Lệnh ' . $muc . ' cho ' . $ten . ' đã bị huỷ.'];
        default:
            return ['warning', 'Đã gửi lệnh ' . $muc . ' cho ' . $ten . ' (lệnh #' . (int)$r['id']
                . ') nhưng server game chưa trả lời sau ' . LENH_CHO_TOI_DA_GIAY . ' giây — có thể server đang tắt. '
                . 'Lệnh vẫn nằm chờ và sẽ được thực hiện ngay khi server chạy lại; KHÔNG cần bấm lại. '
                . 'Theo dõi hoặc huỷ ở bảng "Lệnh đã gửi" bên dưới.'];
    }
}
