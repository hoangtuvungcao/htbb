<?php
/**
 * Bảng các loại phần thưởng của giftcode và bộ kiểm tra cú pháp.
 *
 * Tách riêng khỏi giftcode.php để kiểm thử được mà không cần database: file này
 * thuần logic, không đụng tới PDO hay session.
 *
 * Nguồn sự thật là src/data/DatabaseGiftCode.java (khối if-else từ dòng ~333).
 * Sửa chuỗi phần thưởng ở đó thì nhớ sửa cả bảng dưới đây.
 */

/**
 * Các loại phần thưởng server hiểu, kèm số phần tối thiểu (kể cả tên loại).
 * Thiếu phần là server ném lỗi giữa chừng: người chơi đã nhận được nửa số quà
 * mà dòng giftcode_log lẫn việc trừ lượt đều không chạy — nhận lại được vô hạn.
 */
function loaiThuong(): array
{
    return [
        // loại        => [số phần tối thiểu, cú pháp, mô tả]
        'ITEM'      => [3, 'ITEM:id:soLuong',              'Vật phẩm theo id template'],
        'DB'        => [4, 'DB:id:soLuong:thoiHan',        'Vật phẩm, thời hạn tính bằng phút (0 = vĩnh viễn)'],
        'GEM'       => [3, 'GEM:idNgoc:soLuong:khoa',      'Ngọc; phần khoá < 0 là ngọc khoá'],
        'PT'        => [3, 'PT:idBinh:soLuong',            'Bình / vật phẩm trong ô potion'],
        'PET'       => [4, 'PET:id:soLuong:thoiHan',       'Thú nuôi có thời hạn'],
        'PETV'      => [2, 'PETV:id',                      'Thú nuôi vĩnh viễn'],
        'EGG'       => [3, 'EGG:loai:soLuong',             'Trứng phượng hoàng'],
        'DH'        => [4, 'DH:idDanhHieu:soLuong:thoiHan','Danh hiệu; id 0 = ngẫu nhiên'],
        'DANHHIEU2' => [4, 'DANHHIEU2:id:soLuong:thoiHan', 'Danh hiệu (bản 2)'],
        'HLT'       => [3, 'HLT:id:soLuong',               'Huyết linh thảo'],
        'TRANGPHUC' => [4, 'TRANGPHUC:id:soLuong:soNgay',  'Trang phục thời trang'],
        'TT'        => [5, 'TT:id:soLuong:soNgay:khoa',    'Thời trang (0 ngày = vĩnh viễn)'],
        'CAITRANG'  => [4, 'CAITRANG:id:soLuong:plus',     'Cải trang — 903 Bát Giới, 904 Sa Tăng'],
        'AOTT'      => [4, 'AOTT:id:_:thoiHan',            'Áo thời trang mua ở shop'],
        'AOSHOP'    => [4, 'AOSHOP:id:_:thoiHan',          'Áo thời trang rơi thẳng vào hành trang'],
        'MATNA'     => [4, 'MATNA:option:_:thoiHan',       'Mặt nạ, option -1 = ngẫu nhiên'],
        'MATNA2'    => [4, 'MATNA2:id:_:thoiHan',          'Mặt nạ theo id, option ngẫu nhiên'],
        'MNT'       => [2, 'MNT:thoiHan',                  'Mặt nạ thỏ (837)'],
        'MNC'       => [2, 'MNC:thoiHan',                  'Mặt nạ cáo (838)'],
        'VKTT'      => [4, 'VKTT:_:_:thoiHan',             'Vũ khí thời trang'],
        'VKMAX'     => [4, 'VKMAX:_:_:thoiHan',            'Vũ khí thời trang max chỉ số'],
        'VKID'      => [3, 'VKID:id:thoiHan',              'Vũ khí thời trang theo id'],
        'VKTB'      => [4, 'VKTB:_:_:thoiHan',             'Vũ khí thần binh'],
        'VKTBMAX'   => [4, 'VKTBMAX:_:_:thoiHan',          'Vũ khí thần binh max'],
        'VKTBLV'    => [4, 'VKTBLV:level:option:soNgay',   'Vũ khí thần binh theo cấp'],
        'NLT'       => [4, 'NLT:_:_:thoiHan',              'Nguyệt linh trượng'],
        'NLTMAX'    => [4, 'NLTMAX:_:_:thoiHan',           'Nguyệt linh trượng max'],
        'TK'        => [4, 'TK:id:_:thoiHan',              'Thần khí'],
        'TKMAX'     => [4, 'TKMAX:id:_:thoiHan',           'Thần khí max'],
        'TKF'       => [5, 'TKF:id:soLuong:mau:khoa',      'Thần khí full — id 890/891/892'],
        'TKMAXF'    => [6, 'TKMAXF:id:soLuong:soDong:he:khoa', 'Thần khí max full, số dòng 1–29, hệ -1..4'],
        'PP'        => [4, 'PP:_:_:thoiHan',               'Phi phong max bạo kích (cả 2 hệ)'],
        'PP8X'      => [4, 'PP8X:_:_:thoiHan',             'Phi phong 8x max'],
        'PPR'       => [5, 'PPR:id:soLuong:_:_',           'Phi phong thường'],
        'PPV'       => [5, 'PPV:id:soLuong:_:_',           'Phi phong vĩnh viễn'],
        'PPRK'      => [6, 'PPRK:id:soLuong:_:_:khang',    'Phi phong chọn kháng'],
        'PPVK'      => [6, 'PPVK:id:soLuong:_:_:khang',    'Phi phong vĩnh viễn chọn kháng'],
        'PPLUCK'    => [5, 'PPLUCK:id:_:_:_',              'Phi phong may mắn'],
        'HOVELAN'   => [4, 'HOVELAN:id:_:thoiHan',         'Hộ vệ lệnh lân'],
        'CHOI'      => [5, 'CHOI:id:_:thoiHan:_',          'Chổi'],
        'HKL'       => [2, 'HKL:thoiHan',                  'Hoả kỳ lân'],
        'BT'        => [4, 'BT:_:_:thoiHan',               'Pet bạch thú'],
        'RONGBANG'  => [2, 'RONGBANG:thoiHan',             'Rồng băng'],
        'SUTU'      => [2, 'SUTU:thoiHan',                 'Sư tử'],
        'SUTUBANG'  => [2, 'SUTUBANG:thoiHan',             'Sư tử băng'],
        'LANT'      => [2, 'LANT:thoiHan',                 'Lân'],
        'DBT'       => [2, 'DBT:thoiHan',                  'Poro'],
        'DK'        => [1, 'DK',                           'Đường khang'],
        'DKB'       => [2, 'DKB:thoiHan',                  'Đường khang băng'],
        'DKT'       => [2, 'DKT:thoiHan',                  'Đường khang có thời hạn'],
        'TL'        => [2, 'TL:thoiHan',                   'Tuần lộc'],
        'PHDS'      => [1, 'PHDS',                         'Phượng hoàng đa sắc vĩnh viễn'],
        'PHT'       => [2, 'PHT:thoiHan',                  'Phượng hoàng'],
        'PHDST'     => [2, 'PHDST:thoiHan',                'Phượng hoàng đa sắc'],
        'PHMT'      => [2, 'PHMT:thoiHan',                 'Phượng hoàng mộc'],
        'PHKT'      => [2, 'PHKT:thoiHan',                 'Phượng hoàng kim'],
        'PHTT'      => [2, 'PHTT:thoiHan',                 'Phượng hoàng thổ'],
        'PHBT'      => [2, 'PHBT:thoiHan',                 'Phượng hoàng băng'],
        'DN'        => [3, 'DN:_:diemNap',                 'Cộng điểm nạp VIP'],
        'MN'        => [3, 'MN:_:luongNap',                'Cộng lượng nạp'],
        // Diem doi thuong: cung kho diem ma NPC "doi thuong" tieu (Char.diemDoiThuong),
        // va cung kho ma van tieu / cuop tieu / danh quai cong vao. Tran 1000 diem/ngay
        // chi ap cho nguon DANH QUAI, khong chan phat bang giftcode.
        'DIEMDT'    => [2, 'DIEMDT:soDiem',                'Cộng điểm đổi thưởng (NPC “đổi thưởng”)'],
    ];
}

/**
 * Kiểm tra chuỗi phần thưởng. Trả về [chuoiDaChuan, mangLoi].
 */
function kiemTraItem(string $item): array
{
    $item = trim($item);
    if ($item === '') {
        return ['', []];
    }
    $loi = [];
    if (!preg_match('/^[A-Za-z0-9:,_\- ]+$/', $item)) {
        $loi[] = 'Chuỗi phần thưởng chỉ được chứa chữ, số, dấu hai chấm, dấu phẩy, gạch dưới và dấu trừ. '
               . 'Dấu nháy hay ký tự lạ sẽ làm hỏng câu SQL mà server tự ghép khi ghi nhật ký nhận mã.';
        return [$item, $loi];
    }

    $bang = loaiThuong();
    $phan = [];
    foreach (explode(',', $item) as $mot) {
        $mot = trim($mot);
        if ($mot === '') {
            continue;
        }
        $tach = array_map('trim', explode(':', $mot));
        $loaiPT = strtoupper($tach[0]);

        if (!isset($bang[$loaiPT])) {
            $loi[] = 'Không có loại phần thưởng “' . $loaiPT . '”. Server sẽ bỏ qua nó, người chơi mất quà mà không báo lỗi.';
            $phan[] = $mot;
            continue;
        }
        [$toiThieu, $cuPhap, ] = $bang[$loaiPT];
        if (count($tach) < $toiThieu) {
            $loi[] = '“' . $mot . '” thiếu tham số — đúng phải là ' . $cuPhap
                   . ' (' . $toiThieu . ' phần, đang có ' . count($tach) . ').';
        }
        $tach[0] = $loaiPT;
        $phan[] = implode(':', $tach);
    }
    return [implode(',', $phan), $loi];
}

