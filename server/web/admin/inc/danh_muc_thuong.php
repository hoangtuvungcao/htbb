<?php
/**
 * ============================================================================
 *  DANH MỤC PHẦN THƯỞNG cho bộ chọn của trang tạo giftcode
 * ============================================================================
 *
 *  VÌ SAO CÓ TỆP NÀY. Ô "Phần thưởng vật phẩm" trước đây là một ô văn bản trống,
 *  người tạo mã phải tự nhớ id. Không nhớ được là không tạo được giftcode — và
 *  không nhớ được là chuyện bình thường, vì id nằm rải ở NĂM nguồn khác nhau:
 *
 *      data_item        (914 dòng)  -> ITEM, và cột atb9 lọc ra "thời trang" -> TT
 *      data_potion      (162 dòng)  -> PT
 *      Map.gemTemplate  (270 viên, GHI CỨNG TRONG JAVA, không trong CSDL)   -> GEM
 *      Map.petTemplate  (30 con,   GHI CỨNG TRONG JAVA, id = CHỈ SỐ MẢNG)   -> PET / PETV
 *      DanhHieu.getName (48 tên,   GHI CỨNG TRONG JAVA trong một switch)    -> DH
 *
 *  Riêng chuyện "trứng" đã nằm ở cả ba nguồn đầu: trứng thú là `data_item` type
 *  26, trứng linh thú là `data_item` type 21, còn trứng phượng hoàng / hoả kỳ
 *  lân / đương khang / bạch cốt / lân sư tử lại là `data_potion`. Không ai nhớ
 *  nổi.
 *
 *  MỞ RỘNG (đợt hai). Một đợt rà soát 69 agent kiểm chứng chéo tìm thêm 22 lỗi
 *  ở các loại thưởng còn lại — hầu hết là "id không phải id vật phẩm mà là một
 *  con số có ý nghĩa khác" (chỉ số mảng, chỉ số cấp bậc...) không kiểm biên bên
 *  Java. Đợt này đưa thêm được PET/PETV, TT (thời trang), TK/TKMAX (thần khí),
 *  DH (danh hiệu) vào bộ chọn một cách an toàn. Còn AOTT (chỉ 12 id ghi cứng,
 *  đã có TT thay thế tốt hơn), TKF/TKMAXF (quy tắc dòng ẩn theo màu quá rối để
 *  vội), PPRK/PPVK chọn kháng (chỉ có tác dụng thật với đúng 1 id) — CHƯA đưa
 *  vào, vẫn phải gõ tay.
 *
 *  ------------------------------------------------------------------------
 *   BỘ CHỌN LÀ BỘ SINH CHUỖI, KHÔNG PHẢI ĐƯỜNG ĐI RIÊNG
 *  ------------------------------------------------------------------------
 *  Nó chỉ ghép chuỗi rồi chèn vào chính ô văn bản cũ. Chuỗi đó vẫn đi qua
 *  `kiemTraItem()` ở `inc/thuong.php` y như khi gõ tay. Nghĩa là bộ chọn không
 *  thể tạo ra một loại dữ liệu sai mới, và người quen gõ tay / dán từ mã cũ vẫn
 *  làm được như trước. Đây là lựa chọn có ý: một đường đi thứ hai vào cùng một
 *  cột CSDL là chỗ để hai bên lệch luật kiểm tra.
 *
 *  ------------------------------------------------------------------------
 *   QUÀ GIFTCODE LÀ QUÀ KHOÁ — TỪNG LOẠI LÀM ĐƯỢC ĐẾN ĐÂU
 *  ------------------------------------------------------------------------
 *  Chính sách: vật phẩm nhận từ giftcode không giao dịch được. Ba loại trong bộ
 *  chọn thực hiện việc đó theo ba cách khác nhau, và chỉ một loại cho chọn:
 *
 *    ITEM - khoá sẵn, không có tham số nào để mở. `Char.createItemSoLuong`
 *           (Char.java:12624) đặt thẳng `newItem.lock = 1`.
 *    GEM  - CÓ chọn, mặc định khoá. `doAddGemItem(id, sl, khoa)` với
 *           `khoa = (phần thứ tư < 0)`.
 *    PT   - không có khái niệm khoá. Ô thuốc chỉ là một con số trong mảng
 *           `potions`, không phải vật phẩm có thuộc tính.
 *
 *  ------------------------------------------------------------------------
 *   HAI CÁI BẪY ĐÃ ĐỌC TỪ src/data/DatabaseGiftCode.java
 *  ------------------------------------------------------------------------
 *  1. GEM THIẾU PHẦN THỨ TƯ THÌ HÀNH VI ĐẾN TỪ MỘT KHỐI CATCH.
 *
 *         lockitemgem = (Integer.parseInt(tachTime[3]) < 0 ? true : false);
 *         } catch (Exception e) { lockitemgem = true; }
 *
 *     Viết `GEM:108:5` (ba phần) thì `tachTime[3]` không tồn tại -> rơi vào
 *     catch -> khoá. Kết quả tình cờ trùng với chính sách, nhưng dựa vào một
 *     khối bắt lỗi để ra hành vi mong muốn là thứ đổi nghĩa ngay lần ai đó sửa
 *     chỗ đó. Bộ chọn LUÔN ghi đủ bốn phần để ý định nằm trong chuỗi.
 *
 *  2. TRỨNG PHƯỢNG HOÀNG DÙNG `PT`, KHÔNG DÙNG `EGG` — dù tên loại kia là "trứng".
 *
 *     `EGG:loai:soLuong` gọi `Char.createTrungPhuongHoang()`, mà hàm đó chỉ làm
 *     đúng một việc: `++potions[n]` cho BỐN giá trị loai.
 *
 *         loai 16 -> potions[86]  "Phượng hoàng"
 *         loai 17 -> potions[70]  "Trứng phượng hoàng băng"
 *         loai 18 -> potions[71]  "Trứng Hoả kỳ lân"
 *         loai 19 -> potions[111] "Sách kỹ năng pet"  <-- không phải trứng
 *
 *     Nên `EGG:17:1` và `PT:70:1` làm y hệt nhau. Chọn `PT` vì nó hơn ở ba điểm:
 *       - đủ MỌI id, không chỉ bốn cái;
 *       - `EGG` KHÔNG có nhánh else, nên loai ngoài 16..19 thì người chơi không
 *         nhận gì mà vẫn thấy "Chúc mừng… nhận được trứng phượng hoàng";
 *       - `EGG` không gửi `createCharInventoryMessage`, nên hành trang không tự
 *         làm mới — người chơi phải vào lại game mới thấy quà. `PT` thì gửi.
 *
 *  3. PT GHI THẲNG VÀO MẢNG, KHÔNG KIỂM BIÊN.
 *
 *         player.potions[idGem] = player.potions[idGem] + soluong;
 *
 *     Mảng `potions` có đúng `Char.SO_O_THUOC = 162` ô. Một id ngoài dải đó ném
 *     ArrayIndexOutOfBounds GIỮA lúc phát quà: người chơi đã nhận nửa số quà
 *     trong khi dòng `giftcode_log` và việc trừ lượt đều chưa chạy — nhận lại
 *     được vô hạn. Nên danh mục thuốc ở đây chỉ lấy id 0..161.
 */

require_once __DIR__ . '/danh_muc_ngoc.php';
require_once __DIR__ . '/danh_muc_thu.php';
require_once __DIR__ . '/danh_muc_danh_hieu.php';

/** Số ô thuốc của nhân vật — phải khớp Char.SO_O_THUOC bên Java. */
const DM_SO_O_THUOC = 162;

/**
 * 12 id mảnh sưu tập Bát Giới / Sa Tăng — `ITEM:id:soLuong` cộng dồn số lượng
 * này vào MỘT item duy nhất (`otherAtt[65]`, trần 30000) thay vì tạo nhiều item
 * rời như id thường (từ 31/08/2026, xem `Char.createItemSoLuong`,
 * Char.java:12617+). Trước đó id ngoài danh sách này bị khoá "Số lượng" về 1 ở
 * bộ chọn vì server chỉ tạo đúng 1 món — lỗi đã sửa, ô khoá đã bỏ. Hàm này giữ
 * lại vì `Item.isManhSuuTap()` (Item.java:3292) vẫn cần đúng 12 id để biết khi
 * nào cộng dồn vào 1 item thay vì tạo item rời.
 */
function dmIdManhSuuTap(): array
{
    return [848, 849, 850, 851, 852, 853, 854, 855, 856, 857, 858, 859];
}

/**
 * Các `data_item.type` bị LOẠI khỏi bộ chọn: đồ người chơi tự chế ở NPC
 * **thợ rèn thần bí**.
 *
 * Vì sao loại: đó là trang bị người chơi làm ra bằng nguyên liệu, không phải thứ
 * đem phát bằng giftcode. Giữ lại thì danh sách phồng từ 183 lên 914 món và mấy
 * món thật sự cần phát bị chìm mất.
 *
 * Ba nhóm NPC đó chế, đọc từ `MapCheTao.doCreateItem` (src/real/MapCheTao.java)
 * và hai hàm phân loại trong `Map`:
 *
 *   Vũ khí        3..7    Map.isWeapone()        — kiếm, đại đao, bút, búa, cung
 *   Trang bị      0,1,2   typeItem = idOptionMenu — áo, quần, nón
 *                 8..11   typeItem = idOption + 7 — nhẫn, dây chuyền, giày, găng
 *                 12      typeItem = 12           — ngọc trang bị
 *   Trang bị thú  14..18  Map.isWearingAnimal()
 *
 * Vẫn phát được nếu thật sự cần: gõ tay `ITEM:<id>:<số lượng>` vào ô phần thưởng.
 * Bộ chọn chỉ là lối đi nhanh, không phải cái cổng duy nhất.
 */
function dmLoaiTypeDoChe(): array
{
    return [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18];
}

/**
 * Các id trong `data_item` mà CỘT `type` KHÔNG KHỚP HÀNH VI THẬT của id đó trong
 * game. Loại khỏi bộ chọn vì nhóm hiện ra (theo type) hứa hẹn sai — người tạo mã
 * chọn đúng cái tên đẹp nhưng người chơi nhận được một thứ khác hẳn.
 *
 * PHÁT HIỆN NHƯ THẾ NÀO. Bị hỏi vì sao "Skin linh thú" lại nằm trong nhóm "Phi
 * phong" — kiểm tra thì đúng: `data_item` type 19 (phi phong) có 5 dòng id
 * 950–954 tên "Skin Phúc Thú (hệ Kim)"... Đi tìm thêm thì lộ ra hai trường hợp
 * cùng dạng lỗi ở hai nhóm khác, nên xử lý chung một chỗ:
 *
 *   950–954  type 19 (Phi phong), tên "Skin ..."
 *     `SkinLinhThu.ID_ITEM` — mảng DUY NHẤT gán id cho tính năng đổi hình thú
 *     cưỡi — chỉ có `{905, 906, 907, 908, 909}`. Nhưng 950–954 có `style = 0`,
 *     Y HỆT style của "Phi phong Lăng Vân" (id 584) — tức chúng KHÔNG "vô dụng"
 *     như tưởng ban đầu, mà là một phi phong THƯỜNG, chạy được, chỉ là tên trên
 *     màn hình hứa hẹn đổi hình thú cưỡi mà nó không làm. Phát ra là lừa dối
 *     người chơi lẫn người tạo mã.
 *
 *   728  type 27 (Thú nuôi), tên "Gậy Giáng Sinh"
 *     Mọi thú nuôi thật trong type 27 đều có `atb0 = 5` (đối chiếu 727, 790,
 *     802, 832, 873 trong CSDL). Riêng 728 có `atb0 = atb1 = 0` — không theo
 *     khuôn của một con thú. `Item.java` có hằng số riêng
 *     `GAY_GIANG_SINH = 728` và một nhánh `case 728: // gậy giáng sinh` xử lý
 *     nó như VŨ KHÍ dịp lễ, không đọc gì từ nhánh xử lý thú nuôi cả.
 *
 * VÌ SAO LOẠI Ở TẦNG `dmVatPham()` CHỨ KHÔNG PHẢI Ở TỪNG NHÓM. Đặt ở đây thì
 * loại được khỏi MỌI nhóm cùng lúc — kể cả nhóm "Vật phẩm khác" bắt hết ở cuối,
 * nơi mà lọc theo type sẽ không chặn được (các id này vẫn mang type hợp lệ với
 * bộ lọc theo type). Không có danh sách này thì đúng type mà vẫn lọt hàng lỗi.
 *
 * Không tự động dò (ví dụ so `atb0` hay tên) vì sẽ bắt nhầm những món hợp lệ đặt
 * giá trị khác thường. Đây là danh sách CHỌN TAY, dựa trên bằng chứng đọc mã
 * nguồn cụ thể ở trên — xem `web/admin/test/kiem_danh_muc_thuong.php`, đối
 * chiếu lại đúng bằng chứng đó.
 *
 * Vẫn phát được nếu thật sự cần: gõ tay `ITEM:<id>:<số lượng>`.
 */
function dmIdRac(): array
{
    return [728, 950, 951, 952, 953, 954];
}

/**
 * Id năm viên "Cánh" — Lam Băng, Bạch Linh, Xích Vũ, Hồng Vân, Tử Ảnh.
 *
 * TỪNG BỊ GỘP NHẦM VÀO NHÓM "MẢNH CẢI TRANG" chỉ vì cùng `data_item.type = 29`.
 * Nhưng Cánh là một hệ thống trang trí RIÊNG — `Item.isCanh()`
 * (`return id >= 898 && id <= 902`) và `Char.wModel.CanhModel` — không ghép
 * được thành bộ cải trang như 24 mảnh Bát Giới / Sa Tăng còn lại của type 29.
 * Xác nhận thêm bằng chính chú thích trong `sql/them_phi_phong_vo_hinh.sql`:
 * "Cánh là type 29 và nằm ở danh sách THỜI TRANG (wModel.CanhModel) chứ không
 * phải danh sách trang bị".
 *
 * Khác với `dmIdRac()`: đây KHÔNG phải hàng lỗi, chỉ là xếp sai nhóm. Nên tách
 * thành nhóm riêng "Cánh" thay vì loại bỏ — vẫn là quà hợp lệ, chỉ cần đúng tên.
 */
function dmIdCanh(): array
{
    return [898, 899, 900, 901, 902];
}

/**
 * Tên các nhóm vật phẩm theo cột `data_item.type`.
 *
 * Lấy từ hằng số `Item.TYPE_*` trong src/real/Item.java, bổ sung mấy type mà Java
 * không đặt hằng số (20, 22, 25, 27, 29, 30) bằng cách đọc chính dữ liệu.
 */
function dmTenLoaiVatPham(): array
{
    return [
        0  => 'Áo',                 1  => 'Quần',              2  => 'Nón / khăn',
        3  => 'Kiếm',               4  => 'Đại đao',           5  => 'Bút',
        6  => 'Búa',                7  => 'Cung',              8  => 'Nhẫn',
        9  => 'Dây chuyền',         10 => 'Giày',              11 => 'Găng',
        12 => 'Ngọc trang bị',      13 => 'Cuốc / chổi',       14 => 'Giáp linh thú',
        15 => 'Hộ uyển linh thú',   16 => 'Nón linh thú',      17 => 'Bàn đạp linh thú',
        18 => 'Yên cương linh thú', 19 => 'Phi phong',         20 => 'Rương vận tiêu',
        21 => 'Hộ vệ lệnh / trứng linh thú',                   22 => 'Thẻ mua bán',
        23 => 'Xa tiêu lệnh',       24 => 'Lệnh bài trấn yêu',  25 => 'Hoả kỳ lân',
        26 => 'Trứng thú',          27 => 'Thú nuôi',          28 => 'Đan / thảo dược',
        29 => 'Mảnh cải trang',     30 => 'Skin linh thú',
    ];
}

/**
 * Tên có chứa từ này không, bỏ qua hoa/thường — KHÔNG dùng `mb_*`.
 *
 * Máy chủ không cài extension `mbstring`, và `web/admin/test/kiem_duong_dan.php` có
 * một phép kiểm cấm mọi hàm `mb_*` trong `web/` vì lý do đó. Bản đầu tiên của tệp này
 * dùng `mb_stripos()` và phép kiểm đó đã bắt được.
 *
 * `stripos()` chỉ gập được hoa/thường của chữ ASCII, không gập chữ có dấu. Nên cách
 * dùng ở đây là truyền từ khoá BỎ CHỮ ĐẦU: `dmCoTu($ten, 'rứng')` khớp cả "Trứng" và
 * "trứng", còn phần có dấu thì giống nhau ở mọi cách viết. Toàn bộ tên trong
 * `data_item` / `data_potion` đều viết "Trứng", đã đối chiếu dữ liệu thật.
 */
function dmCoTu(string $ten, string $tu): bool
{
    return $tu !== '' && stripos($ten, $tu) !== false;
}

/**
 * Bỏ xuống dòng trong tên để hiện gọn một dòng trong ô chọn.
 *
 * Cột `name` của game này vừa là TÊN vừa là MÔ TẢ — không có cột mô tả riêng, mô
 * tả được nhét vào `name` và xuống dòng bằng ký tự xuống dòng thật (xem
 * "Áo Nghê Thường" trong bản dump). Để nguyên thì thẻ <option> đứt làm nhiều dòng.
 */
function dmMotDong(?string $ten): string
{
    $ten = str_replace(["\r\n", "\r", "\n"], ' · ', (string)$ten);
    $ten = preg_replace('/\s+/u', ' ', $ten) ?? $ten;
    return trim($ten);
}

/**
 * Đọc vật phẩm từ data_item. Trả về [type => [id => tên]], giữ thứ tự type rồi id.
 *
 * @param int[] $chiLay Chỉ lấy các type này; rỗng = lấy hết.
 */
function dmVatPham(PDO $game, array $chiLay = []): array
{
    $sql = 'SELECT id, name, type, level FROM data_item';
    $args = [];
    if ($chiLay) {
        // Ép về số nguyên rồi nhúng thẳng: đây là mảng do MÃ NGUỒN tạo (hằng số
        // type), không phải dữ liệu người gửi. Dùng placeholder cho mảng độ dài
        // thay đổi thì phải ghép chuỗi placeholder, rối hơn mà không an toàn thêm.
        $so = array_map('intval', $chiLay);
        $sql .= ' WHERE type IN (' . implode(',', $so) . ')';
    }
    $sql .= ' ORDER BY type, id';

    // Loại rác NGAY TẠI ĐÂY — hàm này là điểm chung của mọi nhóm vật phẩm (từng
    // nhóm riêng lẫn nhóm bắt hết "Vật phẩm khác"), nên lọc một lần ở đây là chặn
    // được ở mọi nơi. Lọc trong PHP chứ không thêm vào WHERE: danh sách rác đến từ
    // đối chiếu mã nguồn, không phải một điều kiện suy ra được từ cột nào của bảng.
    $idRac = array_flip(dmIdRac());

    $ra = [];
    try {
        $st = $game->prepare($sql);
        $st->execute($args);
        $dong = $st->fetchAll();

        // Đếm trước xem tên nào TRÙNG THẬT trong đúng tập đang lấy — vd
        // "Phi phong Lăng Vân" có ba id (584/596/597) khác nhau về level (35/95/
        // 100), không có cột nào đánh dấu "bậc chính thức". KHÔNG thể tự đoán id
        // nào "đúng"; cách trung thực là cho người tạo mã thấy đủ để tự chọn.
        //
        // CHỈ gắn cấp cho tên bị trùng, không gắn tràn lan cho mọi món có
        // level > 0: phần lớn vật phẩm khác (trứng, skin, cánh, mảnh sưu tập) đều
        // có level 1 hoặc dùng chung một level cho cả nhóm — gắn "(Lv 1)" vào đó
        // chỉ là nhiễu, không giúp phân biệt gì cả. Đã đối chiếu CSDL đang chạy:
        // trong các type mà bộ chọn dùng, chỉ đúng "Phi phong Lăng Vân" trùng tên.
        $demTen = [];
        foreach ($dong as $d) {
            $ten = dmMotDong((string)$d['name']);
            $demTen[$ten] = ($demTen[$ten] ?? 0) + 1;
        }

        foreach ($dong as $d) {
            $id = (int)$d['id'];
            if (isset($idRac[$id])) {
                continue;
            }
            $ten = dmMotDong((string)$d['name']);
            if ($demTen[$ten] > 1) {
                $level = (int)$d['level'];
                $ten .= $level > 0 ? ' (Lv ' . $level . ')' : ' (id ' . $id . ')';
            }
            $ra[(int)$d['type']][$id] = $ten;
        }
    } catch (Throwable $ex) {
        return [];
    }
    return $ra;
}

/**
 * Đọc thuốc / bình từ data_potion, CHỈ id trong dải hợp lệ và có tên.
 *
 * Dòng không tên là ô trống trong bảng — phát cho người chơi thì họ nhận được một
 * món không tên, không mở được chi tiết. Xem LoginHandler.POTION_SHOP.
 */
function dmThuoc(PDO $game): array
{
    $ra = [];
    try {
        $st = $game->query('SELECT id, name FROM data_potion
                             WHERE name IS NOT NULL AND name <> \'\'
                               AND id >= 0 AND id < ' . DM_SO_O_THUOC . '
                             ORDER BY id');
        foreach ($st->fetchAll() as $d) {
            $ra[(int)$d['id']] = dmMotDong((string)$d['name']);
        }
    } catch (Throwable $ex) {
        return [];
    }
    return $ra;
}

/**
 * "Thời trang" KHÔNG PHẢI một `data_item.type` — nó là cột `atb9` (giá trị 1
 * hoặc 2, `Map.isModelClothes()`), rải trên NHIỀU type khác nhau: 0 (áo), 1
 * (mặt nạ/nón), 2 (nón), 13 (chổi bay). Lọc theo type như các nhóm khác sẽ
 * không tách được thời trang khỏi áo/quần/nón THẬT — đây là lý do trước đợt
 * này bộ chọn hoàn toàn không có nhóm thời trang, dù có thể là loại được phát
 * nhiều nhất trong game.
 *
 * CỐ Ý BỎ type 22/23/24 dù chúng cũng có atb9=1 (Thẻ mua bán, Xa tiêu lệnh,
 * Lệnh bài trấn yêu — 6 dòng). Đối chiếu dữ liệu thật: atb9=1 ở đó không có
 * nghĩa "thời trang", chỉ là cách né bị nhân hệ số theo class — xếp chung vào
 * đây sẽ làm người chọn "Thời trang" nhận nhầm một thẻ giao dịch.
 *
 * CỐ Ý BỎ type 29 (Cánh, atb9=1) — Cánh đã có nhóm riêng ở trên (dmIdCanh()),
 * lấy đây nữa sẽ lặp lại.
 *
 * Loại thưởng: `TT:id:soLuong:soNgay:khoa` -> `Char.createThoiTrang()`
 * (Char.java:6295-6348). Đã đọc toàn bộ thân hàm để xác nhận AN TOÀN cho mọi id
 * trong nhóm này — có null-guard (`if (template == null) return null`), và có
 * nhánh xử lý riêng đúng cho Cánh / mặt nạ / chổi nếu lỡ id rơi vào (không xảy
 * ra ở đây vì đã lọc loại type, nhưng hàm tự nó đã an toàn với mọi id thật).
 */
function dmThoiTrang(PDO $game): array
{
    $ra = [];
    try {
        $st = $game->query('SELECT id, name FROM data_item
                             WHERE atb9 IN (1, 2) AND type IN (0, 1, 2, 13)
                             ORDER BY type, id');
        foreach ($st->fetchAll() as $d) {
            $ra[(int)$d['id']] = dmMotDong((string)$d['name']);
        }
    } catch (Throwable $ex) {
        return [];
    }
    return $ra;
}

/**
 * Dựng toàn bộ danh mục cho bộ chọn.
 *
 * Mỗi nhóm có:
 *   ten     - nhãn hiện trong ô "Nhóm"
 *   loai    - mã loại phần thưởng sẽ sinh ra (ITEM / PT / GEM / TT / PET / PETV / TK / TKMAX / DH)
 *   ghiChu  - câu giải thích hiện dưới ô chọn
 *   muc     - [id => nhãn]
 *
 * Thứ tự ở đây là thứ tự hiện ra: các nhóm hay dùng lên trước, "tất cả" xuống cuối.
 */
function dmDanhMuc(PDO $game): array
{
    $ngoc = danhMucNgoc();
    $nhomNgoc = [];
    foreach ($ngoc as $id => $g) {
        if ($g['nhom'] !== '') {
            $nhomNgoc[$g['nhom']][$id] = $g['ten'];
        }
    }

    $dm = [];

    // ---- Ngọc nguyên liệu: NLSC / NLCC theo cấp ----
    // Đây là nhóm được hỏi tên đầu tiên. Người vận hành gọi chúng là "NLSC 6" /
    // "NLCC 6"; id thật là 73/80/87/94/101 và 108/115/122/129/136 — không ai nhớ.
    foreach (['NLSC' => 'Nguyên liệu sơ cấp', 'NLCC' => 'Nguyên liệu cao cấp'] as $tt => $tenDai) {
        for ($cap = 1; $cap <= 6; $cap++) {
            $ma = $tt . $cap;
            if (empty($nhomNgoc[$ma])) {
                continue;
            }
            $dm[strtolower($ma)] = [
                'ten'    => $tenDai . ' cấp ' . $cap . ' (' . $ma . ')',
                'loai'   => 'GEM',
                'ghiChu' => 'Năm hệ của ' . $ma . '. Không bán trong cửa hàng NPC.',
                'muc'    => $nhomNgoc[$ma],
            ];
        }
    }

    // ---- Ngọc khác không bán trong cửa hàng ----
    $ngocKhac = [];
    foreach ($ngoc as $id => $g) {
        if (!$g['ban'] && $g['nhom'] === '') {
            $ngocKhac[$id] = $g['ten'];
        }
    }
    if ($ngocKhac) {
        $dm['ngoc_khong_ban'] = [
            'ten'    => 'Ngọc khác — không bán trong cửa hàng',
            'loai'   => 'GEM',
            'ghiChu' => 'Những viên người chơi không mua được, chỉ nhận qua sự kiện hoặc giftcode.',
            'muc'    => $ngocKhac,
        ];
    }

    // ---- Trứng ----
    // Ba họ trứng nằm ở ba nguồn khác nhau, nên tách thành ba nhóm rõ ràng thay vì
    // gộp lại: gộp thì không biết món đang chọn sẽ sinh ra ITEM hay PT.
    $vp = dmVatPham($game, [21, 26, 29, 30, 19, 27, 25]);

    if (!empty($vp[26])) {
        $dm['trung_thu'] = [
            'ten'    => 'Trứng thú',
            'loai'   => 'ITEM',
            'ghiChu' => 'data_item type 26 — trứng nở ra thú nuôi.',
            'muc'    => $vp[26],
        ];
    }

    // Trứng linh thú nằm lẫn trong type 21 (Hộ vệ lệnh). Lọc theo tên chứ không ghi
    // cứng dải id 883-887: thêm trứng mới là nó tự hiện, không phải sửa tệp này.
    $trungLinh = [];
    foreach ($vp[21] ?? [] as $id => $ten) {
        if (dmCoTu($ten, 'rứng')) {
            $trungLinh[$id] = $ten;
        }
    }
    if ($trungLinh) {
        $dm['trung_linh'] = [
            'ten'    => 'Trứng linh thú',
            'loai'   => 'ITEM',
            'ghiChu' => 'Nằm lẫn trong data_item type 21, lọc theo tên.',
            'muc'    => $trungLinh,
        ];
    }

    $thuoc = dmThuoc($game);
    $trungThuoc = [];
    foreach ($thuoc as $id => $ten) {
        if (dmCoTu($ten, 'rứng')) {
            $trungThuoc[$id] = $ten;
        }
    }
    if ($trungThuoc) {
        $dm['trung_thuoc'] = [
            'ten'    => 'Trứng phượng hoàng / hoả kỳ lân / đương khang…',
            'loai'   => 'PT',
            'ghiChu' => 'Mấy loại trứng này KHÔNG phải vật phẩm trong hành trang — trong '
                      . 'game chúng là ô thuốc (data_item không có dòng nào tên phượng '
                      . 'hoàng cả). Nên phải dùng loại PT, không phải ITEM.',
            'muc'    => $trungThuoc,
        ];
    }

    // ---- Cánh: 5 món, TÁCH RIÊNG khỏi type 29 ----
    // Cùng type 29 với 24 mảnh cải trang Bát Giới/Sa Tăng, nhưng Cánh là một hệ
    // trang trí RIÊNG (Item.isCanh(), Char.wModel.CanhModel) — không ghép được vào
    // bộ cải trang. Xem dmIdCanh(). Lấy đúng 5 id thay vì lọc theo type để không
    // lẫn lại vào "Mảnh cải trang" bên dưới.
    $idCanh = array_flip(dmIdCanh());
    $muaCanh = [];
    foreach ($vp[29] ?? [] as $id => $ten) {
        if (isset($idCanh[$id])) {
            $muaCanh[$id] = $ten;
        }
    }
    if ($muaCanh) {
        $dm['canh'] = [
            // ĐÃ SỬA: loại này TỪNG là 'ITEM' — sai. Cánh không có chỉ số trong cột
            // atb (chú thích Char.java:6311 "canh 898-902 co atb rong trong data_item,
            // chi so nam o createOptionCanh") — chỉ số THẬT (lockAtb/otherAtt) chỉ
            // được gán khi tạo qua `Char.createThoiTrang()` (loại TT), KHÔNG được gán
            // khi tạo qua `Char.createItemSoLuong()` (loại ITEM, không hề gọi
            // createOptionCanh). Phát Cánh bằng ITEM cho ra một món 0 chỉ số.
            'ten'    => 'Cánh',
            'loai'   => 'TT',
            'ghiChu' => 'Trang trí cánh — hệ riêng (Char.wModel.CanhModel), không phải mảnh '
                      . 'cải trang dù cùng nằm chung data_item type 29. Dùng TT (không phải '
                      . 'ITEM) vì chỉ số của cánh chỉ được gán đúng qua đường này.',
            'muc'    => $muaCanh,
        ];
    }

    // ---- Thời trang: atb9, không phải type — xem dmThoiTrang() ----
    $thoiTrang = dmThoiTrang($game);
    if ($thoiTrang) {
        $dm['thoi_trang'] = [
            'ten'    => 'Thời trang (' . count($thoiTrang) . ')',
            'loai'   => 'TT',
            'ghiChu' => 'Áo / mặt nạ / nón / chổi bay thời trang — cột atb9, không phải type. '
                      . 'Gõ vào ô tìm để lọc theo tên hoặc id.',
            'muc'    => $thoiTrang,
        ];
    }

    // ---- Thú nuôi vĩnh viễn / có thời hạn (Map.petTemplate, KHONG PHAI data_item) ----
    // Xem canh bao dau tools/SinhDanhMucThu.java: id o day la CHI SO MANG (0..29),
    // Char.createPet()/createPetVinhVien() doc thang khong kiem bien.
    $thu = danhMucThu();
    if ($thu) {
        $dm['thu_vinh_vien'] = [
            'ten'    => 'Thú nuôi — vĩnh viễn',
            'loai'   => 'PETV',
            'ghiChu' => 'Chỉ số ở đây là THỨ TỰ trong danh sách (0–' . (count($thu) - 1)
                      . '), không phải id vật phẩm. Chỉ số 5 và 28 trùng tên '
                      . '"Lồng đèn hoa sen" — đúng là hai con khác nhau trong game, không '
                      . 'phải lỗi hiển thị.',
            'muc'    => $thu,
        ];
        $dm['thu_thoi_han'] = [
            'ten'    => 'Thú nuôi — có thời hạn',
            'loai'   => 'PET',
            'ghiChu' => 'Cùng danh sách với "Thú nuôi — vĩnh viễn", nhưng có thêm số ngày.',
            'muc'    => $thu,
        ];
    }

    // ---- Thần khí: chỉ đúng 3 id (890/891/892), xem src/ThanKhi/ThoRenThanBi.java ----
    // Đọc trực tiếp bằng id — không lọc từ $vp (chỉ query các type 19/21/25/26/27/29/30
    // ở trên; 890/891/892 mang type 4, không nằm trong đó).
    $thanKhi = [];
    try {
        $st = $game->prepare('SELECT id, name FROM data_item WHERE id IN (890, 891, 892)');
        $st->execute();
        foreach ($st->fetchAll() as $d) {
            $thanKhi[(int)$d['id']] = dmMotDong((string)$d['name']);
        }
    } catch (Throwable $ex) {
        $thanKhi = [];
    }
    if ($thanKhi) {
        // CẢNH BÁO CHÍNH SÁCH: `Char.createThanKhi()` đặt CỨNG `newItem.lock = 0`,
        // bỏ qua hoàn toàn tham số khoá được truyền vào (Char.java, ngay sau dòng
        // "ItemTemplates template = ..."). Thần khí phát qua TK/TKMAX vì vậy LUÔN
        // giao dịch được — không có cách nào khoá bằng cú pháp giftcode. Đây là hành
        // vi của máy chủ, không phải thiếu sót của bộ chọn.
        $ghiChuThanKhi = 'Chỉ đúng 3 id này được hệ thần khí nhận diện. '
                       . 'LƯU Ý: khác mọi loại khác trong bộ chọn, thần khí phát qua giftcode '
                       . 'KHÔNG BAO GIỜ bị khoá — máy chủ luôn tạo ở trạng thái giao dịch được, '
                       . 'không có cách nào đặt khoá qua giftcode.';
        $dm['than_khi'] = [
            'ten' => 'Thần khí', 'loai' => 'TK', 'ghiChu' => $ghiChuThanKhi, 'muc' => $thanKhi,
        ];
        $dm['than_khi_max'] = [
            'ten' => 'Thần khí — tối đa chỉ số', 'loai' => 'TKMAX',
            'ghiChu' => $ghiChuThanKhi, 'muc' => $thanKhi,
        ];
    }

    // ---- Danh hiệu ----
    $danhHieu = danhMucDanhHieu();
    if ($danhHieu) {
        // id 0 KHÔNG có trong danhMucDanhHieu() (không phải một danh hiệu thật) —
        // thêm tay làm mục đầu tiên, ghi rõ nó là gì thay vì để trống nghĩa.
        $mucDanhHieu = [0 => 'Ngẫu nhiên — chỉ ra một trong 34 danh hiệu KHÔNG đánh dấu '
            . '"(không random được)" bên dưới'];
        foreach ($danhHieu as $id => $d) {
            $mucDanhHieu[$id] = $d['ten'] . ($d['ngauNhien'] ? '' : ' (không random được)');
        }
        $dm['danh_hieu'] = [
            'ten'    => 'Danh hiệu',
            'loai'   => 'DH',
            'ghiChu' => 'Chỉ 34/48 danh hiệu random được bằng cách gõ id 0 — 14 danh hiệu '
                      . 'đánh dấu "(không random được)" PHẢI chọn đúng tên ở đây, gõ id=0 sẽ '
                      . 'không bao giờ ra chúng.',
            'muc'    => $mucDanhHieu,
        ];
    }

    // ---- Các nhóm hay phát khác ----
    foreach ([
        29 => ['manh_caitrang', 'Mảnh cải trang', 'Ghép thành bộ cải trang Bát Giới / Sa Tăng.'],
        30 => ['skin_thu',      'Skin linh thú',  'Đổi hình thú cưỡi, không đổi chỉ số.'],
        19 => ['phi_phong',     'Phi phong',      'Loại thưởng ITEM cho phi phong thường; '
                                                . 'muốn chọn kháng thì dùng PPRK và gõ tay.'],
        27 => ['thu_nuoi',      'Thú nuôi',       'Thú nuôi dạng vật phẩm trong hành trang.'],
        25 => ['hoa_ky_lan',    'Hoả kỳ lân',     ''],
    ] as $type => [$ma, $ten, $ghiChu]) {
        $muc = $vp[$type] ?? [];
        if ($type === 29) {
            // Bỏ 5 món Cánh đã tách ra nhóm riêng ở trên — còn lại đúng 24 mảnh
            // cải trang thật (848–871).
            foreach ($idCanh as $id => $_) {
                unset($muc[$id]);
            }
        }
        if ($muc) {
            $dm[$ma] = ['ten' => $ten, 'loai' => 'ITEM', 'ghiChu' => $ghiChu, 'muc' => $muc];
        }
    }

    // ---- Thuốc / bình ----
    if ($thuoc) {
        $dm['thuoc'] = [
            'ten'    => 'Thuốc / bình (tất cả)',
            'loai'   => 'PT',
            'ghiChu' => 'Chỉ id 0–' . (DM_SO_O_THUOC - 1) . ' và có tên. '
                      . 'Id ngoài dải làm server ném lỗi giữa lúc phát quà.',
            'muc'    => $thuoc,
        ];
    }

    // ---- Bắt hết: mọi vật phẩm KHÔNG PHẢI đồ chế ----
    // Để cuối vì dài, nhưng phải có: không thể liệt kê trước mọi nhóm người vận hành
    // sẽ cần, và thiếu nó thì họ lại quay về gõ id bằng tay.
    $boDiType = dmLoaiTypeDoChe();
    $tenLoai = dmTenLoaiVatPham();
    $canhTrongCatchAll = array_flip(dmIdCanh());
    $mucVp = [];
    foreach (dmVatPham($game) as $type => $ds) {
        if (in_array($type, $boDiType, true)) {
            continue;
        }
        foreach ($ds as $id => $ten) {
            // Cánh mượn chung type 29 với mảnh cải trang — dùng nhãn riêng ở đây
            // để không lặp lại đúng lỗi vừa sửa bên trên trong nhóm bắt hết này.
            $nhan = isset($canhTrongCatchAll[$id]) ? 'Cánh' : ($tenLoai[$type] ?? ('type ' . $type));
            $mucVp[$id] = '[' . $nhan . '] ' . $ten;
        }
    }
    if ($mucVp) {
        $dm['vat_pham'] = [
            'ten'    => 'Vật phẩm khác (' . count($mucVp) . ')',
            'loai'   => 'ITEM',
            'ghiChu' => 'Đã bỏ đồ chế ở thợ rèn thần bí (vũ khí, trang bị, trang bị thú, '
                      . 'ngọc trang bị). Cần phát mấy món đó thì gõ tay ITEM:id:số lượng. '
                      . 'Gõ vào ô tìm để lọc theo tên hoặc id.',
            'muc'    => $mucVp,
        ];
    }

    $mucNgoc = [];
    foreach ($ngoc as $id => $g) {
        $mucNgoc[$id] = $g['ten'] . ($g['ban'] ? '' : ' — không bán');
    }
    $dm['ngoc'] = [
        'ten'    => 'Tất cả ngọc (' . count($mucNgoc) . ')',
        'loai'   => 'GEM',
        'ghiChu' => 'Tên ngọc không có trong CSDL — bảng này sinh từ mã nguồn, xem '
                  . 'tools/SinhDanhMucNgoc.java.',
        'muc'    => $mucNgoc,
    ];

    return $dm;
}

/**
 * Các trường cần điền cho từng loại phần thưởng bộ chọn sinh ra, theo đúng thứ tự
 * tham số của server.
 *
 * Nguồn sự thật là src/data/DatabaseGiftCode.java. Mỗi trường:
 *   ma       - tên trường
 *   nhan     - nhãn hiện ra
 *   macDinh  - giá trị mặc định
 *   chon     - nếu có, là danh sách [giá trị => nhãn] thay cho ô số
 */
function dmTruongCuaLoai(string $loai): array
{
    switch ($loai) {
        case 'ITEM':
            // ITEM:id:soLuong -> Char.createItemSoLuong(id, soLuong, player)
            //
            // KHOÁ SẴN, KHÔNG CẦN CHỌN. Char.java:12624 đặt `newItem.lock = 1` cho mọi
            // vật phẩm sinh bằng đường này, nên quà ITEM luôn không giao dịch được —
            // đúng chính sách "quà giftcode là quà khoá". Không có tham số nào để mở.
            return [['ma' => 'sl', 'nhan' => 'Số lượng', 'macDinh' => '1']];

        case 'PT':
            // PT:id:soLuong -> player.potions[id] += soLuong
            //
            // Không có khái niệm khoá: ô thuốc chỉ là một con số đếm trong mảng
            // `potions`, không phải vật phẩm có thuộc tính. Nên cũng không giao dịch
            // được theo cách của vật phẩm.
            return [['ma' => 'sl', 'nhan' => 'Số lượng', 'macDinh' => '1']];

        case 'GEM':
            // GEM:id:soLuong:khoa -> doAddGemItem(id, soLuong, khoa < 0)
            //
            // Đây là loại DUY NHẤT trong bộ chọn cho chọn khoá hay không, và mặc định
            // là KHOÁ cho khớp chính sách quà giftcode.
            //
            // Ô này KHÔNG được để trống. Thiếu phần thứ tư thì parseInt ném lỗi và khối
            // catch bên Java đặt khoá = true. Kết quả tình cờ đúng, nhưng dựa vào một
            // khối catch để ra hành vi mong muốn là thứ sẽ đổi nghĩa ngay lần ai đó sửa
            // chỗ đó — nên bộ chọn luôn ghi đủ bốn phần.
            return [
                ['ma' => 'sl', 'nhan' => 'Số lượng', 'macDinh' => '1'],
                ['ma' => 'khoa', 'nhan' => 'Khoá', 'macDinh' => '-1', 'chon' => [
                    '-1' => 'Khoá — không giao dịch được (nên dùng)',
                    '0'  => 'Không khoá',
                ]],
            ];

        case 'TT':
            // TT:id:soLuong:soNgay:khoa -> Char.createThoiTrang(p, id, soNgay, khoa)
            // (Char.java:6295). Vòng lặp soLuong lần TẠO THẬT soLuong món riêng biệt —
            // khác hẳn ITEM (chỉ tạo đúng 1 bất kể soLuong). soNgay tính bằng NGÀY
            // (nhân 1440 bên trong), khác PET/TK dùng PHÚT — đừng nhầm đơn vị.
            // `khoa` ở đây là gán THẲNG vào newItem.lock (1 = khoá), KHÁC quy ước
            // "âm là khoá" của GEM — hai loại dùng hai chiều dấu khác nhau.
            return [
                ['ma' => 'sl', 'nhan' => 'Số lượng', 'macDinh' => '1'],
                ['ma' => 'ngay', 'nhan' => 'Số ngày (0 = vĩnh viễn)', 'macDinh' => '0', 'minSo' => '0'],
                ['ma' => 'khoa', 'nhan' => 'Khoá', 'macDinh' => '1', 'chon' => [
                    '1' => 'Khoá — không giao dịch được (nên dùng)',
                    '0' => 'Không khoá',
                ]],
            ];

        case 'PET':
            // PET:id:soLuong:thoiHan(phút) -> vòng lặp soLuong lần Char.createPet(id, phút).
            // id ở đây là CHỈ SỐ MẢNG Map.petTemplate (0..29), không phải id vật phẩm —
            // xem danhMucThu(). Mặc định 10080 phút (7 ngày): 0 rơi vào một nhánh hiển
            // thị đặc biệt của game (Pet.getTimeExpire) không hẳn là "vĩnh viễn" thật sự
            // như TT/GEM — muốn vĩnh viễn rõ ràng thì dùng nhóm "Thú nuôi — vĩnh viễn".
            return [
                ['ma' => 'sl', 'nhan' => 'Số lượng', 'macDinh' => '1'],
                ['ma' => 'phut', 'nhan' => 'Thời hạn (phút)', 'macDinh' => '10080'],
            ];

        case 'PETV':
            // PETV:id -> Char.createPetVinhVien(id). Đúng 2 phần, không tham số nào
            // khác — kể cả nếu người gõ tay thêm số lượng, server cũng CHỈ đọc
            // tachTime[1] rồi tạo đúng 1 con (không có vòng lặp như PET).
            return [];

        case 'TK':
        case 'TKMAX':
            // TK:id:_:thoiHan(phút, 0=vĩnh viễn) -> Char.createThanKhi(..., 0, thoiHan, ...).
            // Phần thứ hai ("_") là CHỖ TRỐNG CHƯA DÙNG trong chính mã nguồn — server
            // gọi createThanKhi với ma_vat=0 GHI CỨNG ngay tại lời gọi, không hề đọc
            // tachTime[2] — nhưng phần đó vẫn phải CÓ MẶT vì server truy cập tới
            // tachTime[3] theo chỉ số mảng. Trường 'an' (ẩn) dưới đây tự điền "_" mà
            // không hiện ô nhập, để không ai phải đoán "điền gì vào chỗ này".
            return [
                ['ma' => '_', 'an' => true, 'macDinh' => '_'],
                ['ma' => 'phut', 'nhan' => 'Thời hạn (phút, 0 = vĩnh viễn)', 'macDinh' => '0', 'minSo' => '0'],
            ];

        case 'DH':
            // DH:id:soLuong:thoiHan(phút, 0=vĩnh viễn) -> player.addDanhHieu(id, phút)
            // lặp soLuong lần (Char.java:14545). id=0 là "ngẫu nhiên" nhưng CHỈ ngẫu
            // nhiên trong 34/48 danh hiệu — xem danhMucDanhHieu() và mục "ngẫu nhiên"
            // được thêm sẵn trong danh sách chọn.
            return [
                ['ma' => 'sl', 'nhan' => 'Số lượng', 'macDinh' => '1'],
                ['ma' => 'phut', 'nhan' => 'Thời hạn (phút, 0 = vĩnh viễn)', 'macDinh' => '0', 'minSo' => '0'],
            ];
    }
    return [];
}

// ============================================================================
//  GIẢI THÍCH CHUỖI PHẦN THƯỞNG THÀNH VĂN BẢN ĐỌC ĐƯỢC
// ============================================================================
//
// Trang danh sách giftcode trước đây chỉ in nguyên chuỗi kiểu
// "ITEM:848:1000,TRANGPHUC:833:1:30" — đúng với những gì server đọc, nhưng
// không ai nhớ id 848/833 là gì mà không mở lại bộ chọn. Cột "Chi tiết" dịch
// từng đoạn ra tên thật và nhãn tham số, dùng CHÍNH các danh mục đã dựng cho
// bộ chọn (data_item / data_potion / Map.gemTemplate / Map.petTemplate /
// DanhHieu) — nên tên hiện ra ở đây LUÔN khớp với tên hiện trong bộ chọn.
//
// CHỈ ĐỂ HIỂN THỊ, KHÔNG DÙNG ĐỂ KIỂM TRA HAY GHI XUỐNG. Đây thuần tuý là một
// bộ giải thích một chiều: đọc chuỗi đã lưu trong CSDL, tra tên, in ra. Nó
// không validate gì (kiemTraItem() vẫn là nơi duy nhất chặn dữ liệu sai), và
// đọc sai một đoạn khó hiểu chỉ làm dòng đó hiển thị xấu chứ không ảnh hưởng gì
// đến quà thật đã phát.
//
// PHỦ HẾT 61 LOẠI trong loaiThuong(), nhưng độ chi tiết không đều — vì bằng
// chứng có sẵn không đều:
//   - Loại có danh mục riêng (ITEM/PT/GEM/TT/PET/PETV/DH/DANHHIEU2/MATNA/MATNA2/
//     EGG/CAITRANG/TKF/TKMAXF và các loại phi phong PPR/PPV/PPRK/PPVK/PPLUCK/
//     HOVELAN/CHOI/AOTT/AOSHOP/TRANGPHUC/HLT/DB/VKID/TK/TKMAX) -> TRA RA TÊN THẬT.
//   - Loại chỉ nhận thời hạn, không có id (RONGBANG/SUTU/HKL/PHT/...) -> chỉ in
//     lại mô tả có sẵn trong loaiThuong() kèm số ngày/phút, ĐÃ XÁC MINH đơn vị
//     bằng cách đọc thẳng Char.java cho từng hàm (createRongLon/createSuTu/
//     createHeoBang/createTuanLoc/createPhuongHoang dùng tham số "day" nhân
//     24*60*60000 = NGÀY; doCreateHoaKyLanTime dùng "minute" nhân 60000 = PHÚT).
//   - Loại còn lại (VKTT/VKMAX/NLT/PP/PP8X/VKTBLV...) không tra được đơn vị thời
//     hạn trong đợt rà soát này -> in số thô, KHÔNG bịa đơn vị chưa xác minh.

/**
 * Đơn vị của tham số tên "thoiHan" trong cú pháp — ĐÃ XÁC MINH bằng cách đọc
 * thẳng hàm Java xử lý từng loại (không đoán theo tên tham số).
 *
 * Tham số tên "soNgay" thì KHÔNG cần bảng này: chính cái tên đã nói rõ đơn vị,
 * và mọi chỗ đã đọc code đều dùng đúng nghĩa đó (nhân 24*60*60000 ra mili-giây).
 */
function dmDonViThoiHanLoai(): array
{
    return [
        'HKL' => 'phút', 'PET' => 'phút', 'TK' => 'phút', 'TKMAX' => 'phút',
        'DH' => 'phút', 'DANHHIEU2' => 'phút', 'MATNA' => 'phút', 'MATNA2' => 'phút',
        'RONGBANG' => 'ngày', 'SUTU' => 'ngày', 'SUTUBANG' => 'ngày',
        'DKB' => 'ngày', 'DKT' => 'ngày', 'TL' => 'ngày',
        'PHT' => 'ngày', 'PHDST' => 'ngày', 'PHMT' => 'ngày',
        'PHKT' => 'ngày', 'PHTT' => 'ngày', 'PHBT' => 'ngày',
    ];
}

/**
 * Tra một id ra tên đọc được, theo đúng nguồn của từng loại thưởng. Trả về
 * null nếu loại đó không có id để tra (vẫn hợp lệ — nhiều loại chỉ nhận thời
 * hạn), hoặc id không tìm thấy trong danh mục.
 */
function dmTenTheoLoai(PDO $game, string $loai, string $idRaw): ?string
{
    if (!ctype_digit(ltrim($idRaw, '-')) || $idRaw === '' || $idRaw === '-') {
        return null;
    }
    $id = (int)$idRaw;

    // Bộ nhớ trong một lần tải trang: trang danh sách giftcode gọi hàm này cho
    // MỖI đoạn của MỖI mã, và cùng một vật phẩm (ví dụ mảnh sưu tập 848) thường
    // lặp lại ở nhiều mã khác nhau. Không cache thì mỗi lần lặp lại là một câu
    // SELECT data_item mới — vô hại với chín mã hiện có, nhưng vô ích khi số mã
    // tăng lên. `null` cũng được nhớ (bằng cờ isset riêng) để không lặp lại tra
    // một id không tồn tại nhiều lần trong cùng một lượt tải trang.
    static $boNho = [];
    $khoa = $loai . ':' . $id;
    if (array_key_exists($khoa, $boNho)) {
        return $boNho[$khoa];
    }
    return $boNho[$khoa] = dmTenTheoLoaiKhongCache($game, $loai, $id);
}

/** Thân thật của dmTenTheoLoai() — tách riêng để hàm bọc ngoài lo việc cache. */
function dmTenTheoLoaiKhongCache(PDO $game, string $loai, int $id): ?string
{
    switch ($loai) {
        // ---- data_item, tra thang bang id that ----
        case 'ITEM': case 'DB': case 'TT': case 'AOTT': case 'AOSHOP':
        case 'TRANGPHUC': case 'MATNA2': case 'HLT': case 'VKID':
        case 'PPR': case 'PPV': case 'PPRK': case 'PPVK': case 'PPLUCK':
        case 'HOVELAN': case 'CHOI': case 'TK': case 'TKMAX':
        case 'TKF': case 'TKMAXF':
            try {
                $st = $game->prepare('SELECT name FROM data_item WHERE id = ?');
                $st->execute([$id]);
                $ten = $st->fetchColumn();
                return $ten === false ? null : dmMotDong((string)$ten);
            } catch (Throwable $ex) {
                return null;
            }

        case 'PT':
            $thuoc = dmThuoc($game);
            return $thuoc[$id] ?? null;

        case 'GEM':
            $ngoc = danhMucNgoc();
            return isset($ngoc[$id]) ? $ngoc[$id]['ten'] : null;

        case 'PET': case 'PETV':
            // Chi so MANG, khong phai id vat pham - xem dmIdManhSuuTap()/danhMucThu().
            $thu = danhMucThu();
            return $thu[$id] ?? null;

        case 'DH':
            if ($id === 0) {
                return 'Ngẫu nhiên (một trong 34 danh hiệu cũ)';
            }
            $dh = danhMucDanhHieu();
            return isset($dh[$id]) ? $dh[$id]['ten'] : null;

        case 'DANHHIEU2':
            $dh2 = danhMucDanhHieu();
            return isset($dh2[$id]) ? $dh2[$id]['ten'] : null;

        case 'CAITRANG':
            // Chi dung 903/904 lam viec - xem doUsePotion/Char.createItemCaiTrang.
            if ($id === 903) { return 'Cải Trang Bát Giới'; }
            if ($id === 904) { return 'Cải Trang Sa Tăng'; }
            return null;

        case 'MATNA':
            // option la CHI SO 0..4 vao Char.ID_MAT_NA = {745,746,747,748,749},
            // KHONG PHAI id vat pham - xem thuong.php.
            if ($id === -1) {
                return 'Ngẫu nhiên';
            }
            if ($id >= 0 && $id <= 4) {
                try {
                    $st = $game->prepare('SELECT name FROM data_item WHERE id = ?');
                    $st->execute([745 + $id]);
                    $ten = $st->fetchColumn();
                    return $ten === false ? null : dmMotDong((string)$ten);
                } catch (Throwable $ex) {
                    return null;
                }
            }
            return null;

        case 'EGG':
            // Char.createTrungPhuongHoang() - CHI 4 gia tri nay co nghia, xem
            // ghi chu 'EGG' trong loaiThuong().
            $anhXa = [16 => 'Phượng hoàng', 17 => 'Trứng phượng hoàng băng',
                18 => 'Trứng Hoả kỳ lân', 19 => 'Sách kỹ năng pet (KHÔNG phải trứng)'];
            return $anhXa[$id] ?? null;
    }
    return null;
}

/**
 * Tách một chuỗi phần thưởng đầy đủ (một hoặc nhiều đoạn nối bằng dấu phẩy)
 * thành các dòng mô tả đọc được. Không bao giờ ném lỗi — dữ liệu cũ có thể có
 * đoạn hỏng hoặc loại không còn dùng, gặp đoạn không hiểu thì in nguyên văn
 * kèm dấu hỏi thay vì làm vỡ cả trang danh sách.
 *
 * @return string[] mỗi phần tử là một dòng mô tả cho một phần thưởng
 */
function dmMoTaChuoiThuong(PDO $game, string $chuoiDayDu): array
{
    $chuoiDayDu = trim($chuoiDayDu);
    if ($chuoiDayDu === '') {
        return [];
    }

    $bangLoai = loaiThuong();
    $donViThoiHan = dmDonViThoiHanLoai();
    $ra = [];

    foreach (explode(',', $chuoiDayDu) as $doan) {
        $doan = trim($doan);
        if ($doan === '') {
            continue;
        }
        $tach = array_map('trim', explode(':', $doan));
        $loai = strtoupper($tach[0]);

        if (!isset($bangLoai[$loai])) {
            $ra[] = '? "' . $doan . '" (không rõ loại)';
            continue;
        }

        [, $cuPhap, $moTa] = $bangLoai[$loai];
        // Tên tham số theo đúng thứ tự ghi trong cú pháp, bỏ phần đầu (tên loại).
        $tenThamSo = array_slice(explode(':', $cuPhap), 1);

        // Tham vào vị trí đầu tiên NẾU cú pháp gọi nó là một biến thể của "id" —
        // đây là quy ước đặt tên xuyên suốt bảng loaiThuong(), không phải đoán mò.
        $tenThamSoDau = $tenThamSo[0] ?? '';
        $laThamSoId = $tenThamSoDau !== '' && $tenThamSoDau !== '_'
            && stripos($tenThamSoDau, 'id') !== false;
        // MATNA dùng tên "option" (chỉ số 0..4, không phải chữ "id") nhưng vẫn cần
        // tra tên; EGG dùng tên "loai" (giá trị 16..19) — cả hai không lọt qua được
        // kiểm tra chứa chữ "id" ở trên nên phải liệt kê thẳng theo tên loại.
        $laThamSoId = $laThamSoId || $tenThamSoDau === 'option' || $loai === 'EGG';

        $tenThat = null;
        if ($laThamSoId && isset($tach[1])) {
            $tenThat = dmTenTheoLoai($game, $loai, $tach[1]);
        }

        // Có tên thật thì DÙNG TÊN LÀM TIÊU ĐỀ DÒNG — mô tả trong loaiThuong() được
        // viết cho bảng tham khảo cú pháp (nhiều loại dài cả một đoạn cảnh báo,
        // như EGG), nhét vào đây thành một dòng chi tiết dài dằng dặc không ai đọc.
        // Chỉ giữ mô tả làm tiêu đề khi KHÔNG có tên thật để hiện — id lạ, hoặc
        // loại này vốn không có id nào để tra (mô tả của nhóm đó vốn đã ngắn:
        // "Rồng băng", "Hoả kỳ lân"...).
        if ($tenThat !== null) {
            $dong = $tenThat;
        } elseif ($laThamSoId && isset($tach[1])) {
            $dong = $moTa . ' (id ' . $tach[1] . ' — không tìm thấy tên)';
        } else {
            $dong = $moTa;
        }

        // Các tham số còn lại: in kèm nhãn, bỏ qua "_" (chỗ trống chưa dùng) và
        // vị trí đã dùng làm id ở trên.
        $phanConLai = [];
        foreach ($tenThamSo as $i => $ten) {
            $viTri = $i + 1; // +1 vì tach[0] là tên loại
            if ($ten === '_' || !isset($tach[$viTri])) {
                continue;
            }
            if ($i === 0 && $laThamSoId) {
                continue; // da dung o tren
            }
            $giaTri = $tach[$viTri];
            if ($ten === 'thoiHan' && isset($donViThoiHan[$loai])) {
                $donVi = $donViThoiHan[$loai];
                $phanConLai[] = $giaTri === '0'
                    ? 'vĩnh viễn'
                    : $giaTri . ' ' . $donVi;
            } elseif ($ten === 'soNgay') {
                // Chinh cai ten "so ngay" da noi ro don vi - moi noi da doc code deu
                // dung dung nghia do (nhan 24*60*60000 ra mili-giay), khong can tra
                // bang rieng nhu "thoiHan" (ten tham so mo hon, don vi lech theo tung
                // loai).
                $phanConLai[] = $giaTri === '0' ? 'vĩnh viễn' : $giaTri . ' ngày';
            } elseif ($ten === 'soLuong') {
                $phanConLai[] = 'số lượng ' . $giaTri;
            } elseif ($ten === 'khoa') {
                // Quy uoc dau khac nhau giua cac loai (GEM: am la khoa; TT: 1 la
                // khoa) - khong doan, chi in nguyen gia tri kem nhan.
                $phanConLai[] = 'khoá=' . $giaTri;
            } else {
                $phanConLai[] = $ten . '=' . $giaTri;
            }
        }
        if ($phanConLai) {
            $dong .= ' (' . implode(', ', $phanConLai) . ')';
        }

        $ra[] = $dong;
    }

    return $ra;
}
