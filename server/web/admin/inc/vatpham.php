<?php
/**
 * Đọc vật phẩm của nhân vật từ ba cột `equip`, `inven`, `bag` của bảng `tob_char`.
 *
 * KHÔNG CÓ BẢNG tob_item trên server này. Trang bị / hành trang / rương được
 * serialize thẳng vào ba cột nói trên (xem ghi chú ở sql/wipe_player_data.sql).
 *
 * ĐỊNH DẠNG (lấy từ chính bộ ghi và bộ nạp của server, không phải đoán):
 *
 *   cột       = nhiều vật phẩm, ngăn nhau bằng  ">"
 *   vật phẩm  = <phần dbInfo>  "|"  <phần thuộc tính>
 *   dbInfo    = 33 số/chuỗi ngăn nhau bằng ","
 *
 * Nguồn đối chiếu:
 *   ghi  : real/Item.java  getInfoSave() -> getDBInfo() + "|" + getAttribute()
 *   đọc  : real/Item.java  initInfoFromDB()   (thứ tự trường lấy từ đây)
 *   ghép : data/DatabaseNhanVat.java  saveChar*()
 *   tách : real/CharTrangBi.java  setitemInvenBag()
 *
 * HAI ĐIỀU DỄ SAI, đều lấy quy tắc từ bộ nạp của server:
 *
 *   1. Cột `inven` có phần "thời trang đang mặc" NỐI THÊM vào cuối
 *      (DatabaseNhanVat: invent.append(">").append(infomodel)). Chúng cùng định
 *      dạng nên không có ranh giới nào để cắt. Server phân biệt bằng
 *      `place == 4` (CharTrangBi:514) — ở đây làm y hệt.
 *
 *   2. Chuỗi có thể bắt đầu bằng ">" (khi nhân vật không có vật phẩm nào trong
 *      túi nhưng có thời trang). Server tự cắt dấu đó đi trước khi tách.
 */

/** Vị trí các trường trong phần dbInfo — thứ tự lấy từ Item.initInfoFromDB(). */
const VP_CLAZZ         = 0;
const VP_TEMPLATE      = 1;
const VP_PLACE         = 2;
const VP_DURABLE       = 3;
const VP_MDURABLE      = 4;
const VP_PLUS          = 5;
const VP_LEVEL         = 6;
const VP_MINUTE_EXIST  = 10;
const VP_COLOR         = 11;
const VP_LOCK          = 14;
const VP_DATE_CREATE   = 19;
const VP_TINH_LUYEN    = 30;

/** place == 4 nghĩa là thời trang đang mặc, không phải đồ trong túi. */
const VP_PLACE_THOI_TRANG = 4;

/**
 * Tách một cột thành danh sách vật phẩm.
 *
 * Bỏ qua (không làm hỏng cả danh sách) những mẩu không đọc được: dữ liệu cũ từ
 * các phiên bản trước có thể thiếu trường ở cuối, và chính server cũng bọc mỗi
 * vật phẩm trong try/catch riêng khi nạp.
 *
 * @return array<int, array<string, mixed>>
 */
function docChuoiVatPham(?string $chuoi): array
{
    if ($chuoi === null) {
        return [];
    }
    $chuoi = trim($chuoi);
    // Server cắt dấu ">" đầu chuỗi trước khi tách (CharTrangBi:358).
    if ($chuoi !== '' && $chuoi[0] === '>') {
        $chuoi = substr($chuoi, 1);
    }
    if ($chuoi === '') {
        return [];
    }

    $ra = [];
    // '_i' = vi tri mau nay trong mang explode('>', $chuoi) (sau khi da cat dau '>'
    // dau chuoi giong server) - dung de XOA dung mau bang xoaVatPhamTheoViTri(), vi
    // day la CHUOI THO duy nhat con giu du lieu day du (cac truong sau dau '|' bi bo
    // qua o day). Danh so tren TOAN BO mang, ke ca mau bi bo qua ben duoi, de vi tri
    // luon khop voi chuoi goc.
    foreach (explode('>', $chuoi) as $i => $mau) {
        if (trim($mau) === '') {
            continue;
        }
        $phan = explode('|', $mau);
        $t = explode(',', $phan[0]);
        // Thiếu tới cả id template thì không còn gì để hiển thị.
        if (count($t) <= VP_TEMPLATE) {
            continue;
        }
        $lay = static function (int $i, int $macDinh = 0) use ($t): int {
            return isset($t[$i]) && is_numeric(trim($t[$i])) ? (int)trim($t[$i]) : $macDinh;
        };
        $ra[] = [
            '_i'         => $i,
            'template'   => $lay(VP_TEMPLATE, -1),
            'place'      => $lay(VP_PLACE, -1),
            'plus'       => $lay(VP_PLUS),
            'level'      => $lay(VP_LEVEL),
            'mau'        => $lay(VP_COLOR),
            'khoa'       => $lay(VP_LOCK) === 1,
            'tinh_luyen' => $lay(VP_TINH_LUYEN),
            'thue_phut'  => $lay(VP_MINUTE_EXIST),
            'ngay_tao'   => isset($t[VP_DATE_CREATE]) ? trim($t[VP_DATE_CREATE]) : '',
        ];
    }
    return $ra;
}

/**
 * Xoá đúng một vật phẩm khỏi chuỗi thô (equip/inven/bag), theo vị trí '_i' trả về
 * từ docChuoiVatPham() — dùng cho tính năng xoá đồ hành trang khi phát hiện hack.
 *
 * Giữ NGUYÊN phần thô của các vật phẩm còn lại (không parse rồi build lại từ các
 * trường đã rút gọn — docChuoiVatPham() bỏ qua thuộc tính sau dấu "|", build lại
 * từ đó sẽ làm hỏng dữ liệu các item khác). Chỉ cắt bỏ đúng một mẩu theo vị trí.
 *
 * QUAN TRỌNG: cột `inven` có phần "thời trang đang mặc" nối thêm ở mẩu cuối cùng
 * (xem ghi chú đầu file) — hàm này không đụng gì tới việc đó, chỉ xoá mẩu ở đúng
 * $viTri, nên vẫn an toàn dùng chung cho cả ba cột.
 */
function xoaVatPhamTheoViTri(?string $chuoi, int $viTri): string
{
    $chuoi = trim((string)$chuoi);
    $coDauGach = ($chuoi !== '' && $chuoi[0] === '>');
    if ($coDauGach) {
        $chuoi = substr($chuoi, 1);
    }
    $mau = explode('>', $chuoi);
    if (!isset($mau[$viTri])) {
        throw new RuntimeException('Không tìm thấy vật phẩm ở vị trí ' . $viTri . ' để xoá.');
    }
    unset($mau[$viTri]);
    // Server tự cắt dấu '>' đầu chuỗi nếu có (CharTrangBi:358) nên không cần giữ
    // lại $coDauGach khi ghép lại - luôn ghi bản không có dấu gạch đầu cho gọn.
    return implode('>', $mau);
}

/**
 * Tách cột `inven` thành hai phần: đồ trong túi và thời trang đang mặc.
 *
 * @return array{tui: array, thoi_trang: array}
 */
function tachTuiVaThoiTrang(?string $inven): array
{
    $tui = [];
    $thoiTrang = [];
    foreach (docChuoiVatPham($inven) as $vp) {
        if ($vp['place'] === VP_PLACE_THOI_TRANG) {
            $thoiTrang[] = $vp;
        } else {
            $tui[] = $vp;
        }
    }
    return ['tui' => $tui, 'thoi_trang' => $thoiTrang];
}

/** Tên màu vật phẩm — hằng số lấy từ real/Item.java (COLOR_WHITE = 0 …). */
function tenMauVatPham(int $mau): string
{
    $ten = [
        0 => 'Trắng',
        1 => 'Xanh lá',
        2 => 'Đỏ',
        3 => 'Xanh dương',
        4 => 'Hồng',
        5 => 'Vàng',
    ];
    return $ten[$mau] ?? ('màu ' . $mau);
}

/**
 * Tra tên vật phẩm theo id template, một câu cho cả danh sách.
 *
 * @param  array<int,int> $ids
 * @return array<int,string>  id => tên
 */
function tenVatPhamTheoId(PDO $game, array $ids): array
{
    $ids = array_values(array_unique(array_filter($ids, static fn($i) => $i >= 0)));
    if (!$ids) {
        return [];
    }
    try {
        $cho = implode(',', array_fill(0, count($ids), '?'));
        $st = $game->prepare("SELECT id, name FROM data_item WHERE id IN ($cho)");
        $st->execute($ids);
        $ra = [];
        foreach ($st->fetchAll() as $r) {
            $ra[(int)$r['id']] = (string)$r['name'];
        }
        return $ra;
    } catch (Throwable $ex) {
        return [];
    }
}
