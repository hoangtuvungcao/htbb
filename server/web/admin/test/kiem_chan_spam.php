<?php
/**
 * Kiểm thử bộ đếm chặn làm hàng loạt (web/inc/functions.php).
 *
 * Chạy KHÔNG cần MySQL: không có bảng `web_chan_spam` thì hàm tự lùi về đếm
 * trong session, và đó chính là nhánh được kiểm ở đây. Nhánh dùng database có
 * cùng ý nghĩa nhưng đếm theo địa chỉ IP thay vì theo phiên.
 *
 * Chạy: php web/admin/test/kiem_chan_spam.php
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

require_once __DIR__ . '/../../inc/functions.php';

$dat = 0;
$hong = 0;

function kt(string $ten, $thucTe, $mongDoi): void
{
    global $dat, $hong;
    if ($thucTe === $mongDoi) {
        $dat++;
        echo "  OK   $ten\n";
    } else {
        $hong++;
        echo "  HONG $ten: duoc " . var_export($thucTe, true)
           . ", mong doi " . var_export($mongDoi, true) . "\n";
    }
}

if (coBangChanSpam()) {
    echo "LUU Y: dang co bang web_chan_spam va ket noi duoc database.\n";
    echo "       Test nay chi kiem nhanh du phong (session), nen bo qua.\n";
    echo "       Xoa bang hoac tat MySQL de chay day du.\n";
    exit(0);
}
echo "Khong co bang web_chan_spam -> dang kiem nhanh du phong bang session.\n\n";

$_SESSION['chan_spam'] = [];

echo "== Chua lam gi thi khong bi chan ==\n";
kt('lan dau', biChanSpam('thu', 3, 60), false);

echo "\n== Chan dung khi cham han muc ==\n";
ghiLanLam('thu');
kt('sau 1 lan  (toi da 3)', biChanSpam('thu', 3, 60), false);
ghiLanLam('thu');
kt('sau 2 lan  (toi da 3)', biChanSpam('thu', 3, 60), false);
ghiLanLam('thu');
kt('sau 3 lan  (toi da 3) -> CHAN', biChanSpam('thu', 3, 60), true);
ghiLanLam('thu');
kt('sau 4 lan  -> van chan', biChanSpam('thu', 3, 60), true);

echo "\n== Han muc rong hon thi chua chan ==\n";
kt('cung 4 lan nhung toi da 10', biChanSpam('thu', 10, 60), false);

echo "\n== Cac viec khac nhau dem rieng ==\n";
kt('viec khac chua bi chan', biChanSpam('viec_khac', 3, 60), false);
ghiLanLam('viec_khac');
ghiLanLam('viec_khac');
ghiLanLam('viec_khac');
kt('viec khac -> chan', biChanSpam('viec_khac', 3, 60), true);
kt('viec dau van chan doc lap', biChanSpam('thu', 3, 60), true);

echo "\n== Qua thoi gian thi mo lai ==\n";
// Day lui moc thoi gian 2 tieng de gia lap "da lau roi".
foreach ($_SESSION['chan_spam']['thu'] as $i => $t) {
    $_SESSION['chan_spam']['thu'][$i] = $t - 7200;
}
kt('sau 2 tieng, cua so 60 giay', biChanSpam('thu', 3, 60), false);
kt("cua so 3 tieng thi van con nho", biChanSpam("thu", 3, 10800), true);

echo "\n== Kiem tra KHONG duoc lam thay doi lich su ==\n";
// Loi that da bat duoc bang test nay: ban dau biChanSpam don luon cac moc nam
// ngoai cua so, nen goi voi cua so ngan la xoa mat lich su ma lan kiem tra sau
// (cua so dai hon) con can.
$_SESSION['chan_spam']['nguyen_ven'] = [time() - 7200, time() - 7200, time()];
biChanSpam('nguyen_ven', 3, 60);
kt('sau khi kiem, so moc giu nguyen', count($_SESSION['chan_spam']['nguyen_ven']), 3);
kt('cua so 3 tieng van thay du 3', biChanSpam('nguyen_ven', 3, 10800), true);

echo "\n== Don rac nam o ghiLanLam, theo han giu co dinh ==\n";
$_SESSION['chan_spam']['don'] = [time() - 172800, time() - 172800, time() - 60];
ghiLanLam('don');   // don rac roi them 1 moc moi
kt('2 moc qua 24 gio bi loai, con 1 cu + 1 moi', count($_SESSION['chan_spam']['don']), 2);

echo "\n== Han muc 0 = chan tat ca ==\n";
kt('toi da 0', biChanSpam('chua_lam_bao_gio', 0, 60), true);

echo "\n===================================\n";
echo "DAT: $dat   HONG: $hong\n";
exit($hong > 0 ? 1 : 0);
