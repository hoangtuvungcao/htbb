<?php
/**
 * ============================================================================
 *  DANH MỤC PHẦN THƯỞNG cho bộ chọn của trang tạo giftcode
 * ============================================================================
 *
 *  VÌ SAO CÓ TỆP NÀY. Ô "Phần thưởng vật phẩm" trước đây là một ô văn bản trống,
 *  người tạo mã phải tự nhớ id. Không nhớ được là không tạo được giftcode — và
 *  không nhớ được là chuyện bình thường, vì id nằm rải ở BA nguồn khác nhau:
 *
 *      data_item     (914 dòng)  -> loại thưởng ITEM / TRANGPHUC
 *      data_potion   (162 dòng)  -> loại thưởng PT
 *      Map.gemTemplate (270 viên, GHI CỨNG TRONG JAVA, không có trong CSDL)
 *                                -> loại thưởng GEM
 *
 *  Riêng chuyện "trứng" đã nằm ở cả ba nguồn: trứng thú là `data_item` type 26,
 *  trứng linh thú là `data_item` type 21, còn trứng phượng hoàng / hoả kỳ lân /
 *  đương khang / bạch cốt / lân sư tử lại là `data_potion`. Không ai nhớ nổi.
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

/** Số ô thuốc của nhân vật — phải khớp Char.SO_O_THUOC bên Java. */
const DM_SO_O_THUOC = 162;

/**
 * `ITEM:id:soLuong` CHỈ THẬT SỰ CỘNG DỒN SỐ LƯỢNG VỚI ĐÚNG 12 ID NÀY — mảnh sưu
 * tập Bát Giới / Sa Tăng. Mọi id khác, dù ghi số lượng bao nhiêu, server cũng chỉ
 * tạo ĐÚNG MỘT món.
 *
 * ĐỌC THẲNG TỪ `Char.createItemSoLuong` (Char.java:12617-12648):
 *
 *     if (newItem.isManhSuuTap()) {           // Item.java:3292 — id 848..859
 *         ... cộng dồn otherAtt[65] += soLuong, trần 30000 ...
 *     } else {
 *         p.iItems.add(newItem);              // <-- LUÔN thêm ĐÚNG MỘT món
 *     }
 *     ...
 *     "Chúc mừng. Bạn đã nhận được " + soLuong + " " + newItem.getName()
 *
 * Dòng thông báo cuối vẫn in nguyên `soLuong` NGƯỜI TẠO MÃ GÕ, bất kể nhánh nào
 * chạy. Nghĩa là bộ chọn cho id ngoài dải này chọn "Số lượng: 5" thì người chơi
 * nhận ĐÚNG 1 món trong khi màn hình báo "nhận được 5" — và dòng ghi trong
 * `giftcode_log`/textarea cũng ghi số 5, nên sau này xem lại còn tưởng đã đúng.
 *
 * Vì vậy: với id KHÔNG nằm trong danh sách này, JavaScript khoá ô "Số lượng" về
 * 1 và giải thích lý do — không cấm gõ tay `ITEM:id:5`, chỉ là bộ chọn không giúp
 * tạo ra một chuỗi trông đúng mà làm sai với id thường.
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
    $sql = 'SELECT id, name, type FROM data_item';
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
        foreach ($st->fetchAll() as $d) {
            $id = (int)$d['id'];
            if (isset($idRac[$id])) {
                continue;
            }
            $ra[(int)$d['type']][$id] = dmMotDong((string)$d['name']);
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
 * Dựng toàn bộ danh mục cho bộ chọn.
 *
 * Mỗi nhóm có:
 *   ten     - nhãn hiện trong ô "Nhóm"
 *   loai    - mã loại phần thưởng sẽ sinh ra (ITEM / PT / GEM / TRANGPHUC)
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
            'ten'    => 'Cánh',
            'loai'   => 'ITEM',
            'ghiChu' => 'Trang trí cánh — hệ riêng (Char.wModel.CanhModel), không phải mảnh '
                      . 'cải trang dù cùng nằm chung data_item type 29.',
            'muc'    => $muaCanh,
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
    }
    return [];
}
