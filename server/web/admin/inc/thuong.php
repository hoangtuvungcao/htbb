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
        // loại        => [số phần tối thiểu, cú pháp, mô tả, ví dụ đã kiểm qua kiemTraItem()]
        'ITEM'      => [3, 'ITEM:id:soLuong',              'Vật phẩm theo id template', 'ITEM:848:1'],
        'DB'        => [4, 'DB:id:soLuong:thoiHan',        'Vật phẩm, thời hạn tính bằng phút (0 = vĩnh viễn)', 'DB:905:1:0'],
        'GEM'       => [3, 'GEM:idNgoc:soLuong:khoa',      'Ngọc; phần khoá < 0 là ngọc khoá', 'GEM:73:100:1'],
        'PT'        => [3, 'PT:idBinh:soLuong',            'Bình / vật phẩm trong ô potion', 'PT:9:100'],
        'PET'       => [4, 'PET:id:soLuong:thoiHan',       'Thú nuôi có thời hạn', 'PET:12:1:0'],
        'PETV'      => [2, 'PETV:id',                      'Thú nuôi vĩnh viễn', 'PETV:12'],
        // Tên loại này gây hiểu sai, nên ghi rõ ra đây. createTrungPhuongHoang() chỉ
        // làm đúng một việc: ++potions[n] cho BỐN giá trị loai, và không có nhánh else —
        // loai khác thì người chơi KHÔNG nhận gì mà vẫn thấy "Chúc mừng… nhận được trứng
        // phượng hoàng". Nó cũng không gửi createCharInventoryMessage nên hành trang
        // không tự làm mới. Dùng PT:<id>:<số lượng> thì vừa đủ mọi id vừa làm mới đúng.
        'EGG'       => [3, 'EGG:loai:soLuong',
            'Chỉ 4 loại: 16 = Phượng hoàng, 17 = Trứng PH băng, 18 = Trứng hoả kỳ lân, '
            . '19 = Sách kỹ năng pet (không phải trứng). Loại khác: không nhận gì. '
            . 'Nên dùng PT thay thế', 'EGG:17:1'],
        'DH'        => [4, 'DH:idDanhHieu:soLuong:thoiHan','Danh hiệu; id 0 = ngẫu nhiên', 'DH:8767:1:0'],
        'DANHHIEU2' => [4, 'DANHHIEU2:id:soLuong:thoiHan', 'Danh hiệu (bản 2)', 'DANHHIEU2:8767:1:0'],
        'HLT'       => [3, 'HLT:id:soLuong',               'Huyết linh thảo', 'HLT:0:10'],
        'TRANGPHUC' => [4, 'TRANGPHUC:id:soLuong:soNgay',  'Trang phục thời trang', 'TRANGPHUC:833:1:30'],
        'TT'        => [5, 'TT:id:soLuong:soNgay:khoa',    'Thời trang (0 ngày = vĩnh viễn)', 'TT:833:1:0:0'],
        'CAITRANG'  => [4, 'CAITRANG:id:soLuong:plus',     'Cải trang — 903 Bát Giới, 904 Sa Tăng', 'CAITRANG:903:1:10'],
        'AOTT'      => [4, 'AOTT:id:_:thoiHan',            'Áo thời trang mua ở shop', 'AOTT:265:_:0'],
        'AOSHOP'    => [4, 'AOSHOP:id:_:thoiHan',          'Áo thời trang rơi thẳng vào hành trang', 'AOSHOP:725:1:0'],
        'MATNA'     => [4, 'MATNA:option:_:thoiHan',       'Mặt nạ, option -1 = ngẫu nhiên', 'MATNA:-1:_:43200'],
        'MATNA2'    => [4, 'MATNA2:id:_:thoiHan',          'Mặt nạ theo id, option ngẫu nhiên', 'MATNA2:837:_:43200'],
        'MNT'       => [2, 'MNT:thoiHan',                  'Mặt nạ thỏ (837)', 'MNT:30'],
        'MNC'       => [2, 'MNC:thoiHan',                  'Mặt nạ cáo (838)', 'MNC:30'],
        'VKTT'      => [4, 'VKTT:_:_:thoiHan',             'Vũ khí thời trang', 'VKTT:_:_:0'],
        'VKMAX'     => [4, 'VKMAX:_:_:thoiHan',            'Vũ khí thời trang max chỉ số', 'VKMAX:_:_:0'],
        'VKID'      => [3, 'VKID:id:thoiHan',              'Vũ khí thời trang theo id', 'VKID:715:0'],
        // CANH BAO (05/09/2026): tham so is8x truyen cho createVuKhiThoiTrangTHANBINH_NEW
        // luon la true o CA HAI nhanh VKTB/VKTBMAX, nhung than THAN HAM khong he doc
        // is8x - iditem bi CUNG CO DINH bang "758 + charClass" (bac thap nhat, level 60),
        // bat ke is8x. Hai loai nay hien tai KHONG THE tao duoc than binh bac 70/80 du
        // ten goi "8x". Muon chon dung bac (60/70/80) thi PHAI dung VKTBLV ben duoi -
        // day la duong DUY NHAT con dung dươc. Xem test/KiemGiftcodeVuKhiThanBinh.java.
        'VKTB'      => [4, 'VKTB:_:_:thoiHan',
            'Vũ khí thần binh — LƯU Ý: dù tên gợi ý "8x" nhưng hiện chỉ tạo được bậc thấp '
            . 'nhất (level 60, do is8x bị bỏ quên trong code). Muốn chọn đúng bậc 70/80 '
            . 'thì dùng VKTBLV bên dưới', 'VKTB:_:_:0'],
        'VKTBMAX'   => [4, 'VKTBMAX:_:_:thoiHan',
            'Vũ khí thần binh max chỉ số — CÙNG LỖI như VKTB ở trên: chỉ ra được bậc level '
            . '60. Muốn "8x" (level 80) + max chỉ số thì dùng VKTBLV:80:...', 'VKTBMAX:_:_:0'],
        'VKTBLV'    => [4, 'VKTBLV:level:tinhLuyen:soNgay',
            'Vũ khí thần binh CHỌN ĐÚNG BẬC — level PHẢI đúng 60, 70 hoặc 80 (giá trị khác '
            . 'thì server lặng lẽ không tạo gì cả); tinhLuyen 0-99 (0 = không); LUÔN max chỉ '
            . 'số (isMax cố định true). Đây là cách duy nhất tạo "vũ khí thần binh 8x max '
            . 'chỉ số": level=80', 'VKTBLV:80:0:0'],
        'NLT'       => [4, 'NLT:_:_:thoiHan',              'Nguyệt linh trượng', 'NLT:_:_:0'],
        'NLTMAX'    => [4, 'NLTMAX:_:_:thoiHan',           'Nguyệt linh trượng max', 'NLTMAX:_:_:0'],
        'TK'        => [4, 'TK:id:_:thoiHan',              'Thần khí — id là id vũ khí RIÊNG của từng lớp nhân vật, mã dùng chung nhiều lớp sẽ lỗi giữa chừng và mất phần thưởng phía sau (xem vps/sql-giftcode.md)', 'TK:890:_:0'],
        'TKMAX'     => [4, 'TKMAX:id:_:thoiHan',           'Thần khí max — cùng lưu ý id theo lớp như TK', 'TKMAX:890:_:0'],
        'TKF'       => [5, 'TKF:id:soLuong:mau:khoa',      'Thần khí full — id 890/891/892', 'TKF:890:1:0:0'],
        'TKMAXF'    => [6, 'TKMAXF:id:soLuong:soDong:he:khoa', 'Thần khí max full, số dòng 1–29, hệ -1..4', 'TKMAXF:890:1:9:-1:0'],
        // GHI CHU (05/09/2026), tra loi cau hoi "nhap phi phong Huyen Vu the nao":
        // - PP/PP8X TU CHON id trong danh sach co dinh {584..597,840} (PP8X ep ve 593) -
        //   xem Char.createPhiPhongMaxBaoKich(): KHONG BAO GIO ra id 750, du "8x"/"max".
        //   Muon tang dung Phi Phong Huyen Vu thi PHAI dung PPR/PPV/PPRK/PPVK voi id=750.
        // - Ten "PPV" ("vinh vien") GAY HIEU SAI: doi chieu DatabaseGiftCode.java thi
        //   PPR/PPV goi CUNG mot ham createtemCoat(), khac nhau DUY NHAT o co "ismax"
        //   (chi so max hay ngau nhien) - KHONG lien quan vinh vien/co han. Vinh vien/co
        //   han do rieng tham so thoiHan (phan thu 4) quyet dinh, GIONG HET nhau o ca hai:
        //   thoiHan=0 la vinh vien du dung PPR hay PPV. Cu phap cu ghi "_" o vi tri
        //   thoiHan/he la SAI - ca hai deu duoc Java doc that (Integer.parseInt), khong
        //   phai bo qua.
        // - "khang" (tham so cuoi cua PPRK/PPVK) CHI co tac dung that voi id=750: xem
        //   Item.createAttributeItemCoat(), dong "if (this.getTemplate().id == 750)" la
        //   noi DUY NHAT tham so khang duoc dung de ghi thuoc tinh. Voi bat ky id phi
        //   phong nao khac, khang bi doc nhung khong dong nao dung toi no ca - im lang
        //   khong co gi xay ra. Vi du cu dung id 584 cho PPRK/PPVK la VO NGHIA vi vay.
        'PP'        => [4, 'PP:_:_:thoiHan',
            'Phi phong max bạo kích (cả 2 hệ) — tự chọn 1 trong 15 id cố định theo cấp độ '
            . '(584-597, 840), KHÔNG BAO GIỜ ra Phi Phong Huyền Vũ (id 750). Muốn tặng đúng '
            . 'Huyền Vũ thì dùng PPV ở dưới với id=750', 'PP:_:_:0'],
        'PP8X'      => [4, 'PP8X:_:_:thoiHan',
            'Phi phong 8x max — CỐ ĐỊNH ra id 593, cũng không phải Huyền Vũ. Xem PPV:750 ở dưới',
            'PP8X:_:_:0'],
        'PPR'       => [5, 'PPR:id:soLuong:thoiHan:he',
            'Phi phong theo id, chỉ số NGẪU NHIÊN — he: 0=phép, 1=vật lý; thoiHan 0=vĩnh viễn '
            . '(không phải PPV mới vĩnh viễn — xem PPV). vd id 750 = Phi Phong Huyền Vũ',
            'PPR:750:1:0:0'],
        'PPV'       => [5, 'PPV:id:soLuong:thoiHan:he',
            'Phi phong theo id, chỉ số MAX (khác PPR đúng một chỗ này, KHÔNG phải "vĩnh viễn" — '
            . 'vĩnh viễn do thoiHan=0 quyết định, giống hệt PPR). vd id 750 = Phi Phong Huyền Vũ max chỉ số',
            'PPV:750:1:0:0'],
        'PPRK'      => [6, 'PPRK:id:soLuong:thoiHan:he:khang',
            'Phi phong chọn hệ kháng, chỉ số ngẫu nhiên — LƯU Ý: khang CHỈ có tác dụng thật với '
            . 'id=750 (Phi Phong Huyền Vũ); id khác thì bị bỏ qua hoàn toàn, không có gì xảy ra. '
            . 'khang: 0=hoả,1=thuỷ,2=mộc,3=kim,4=thổ, -1=ngẫu nhiên',
            'PPRK:750:1:0:0:2'],
        'PPVK'      => [6, 'PPVK:id:soLuong:thoiHan:he:khang',
            'Phi phong chọn hệ kháng, chỉ số MAX — cùng lưu ý như PPRK: chỉ có tác dụng thật '
            . 'với id=750 (Phi Phong Huyền Vũ)', 'PPVK:750:1:0:0:2'],
        'PPLUCK'    => [5, 'PPLUCK:id:_:_:_',              'Phi phong may mắn', 'PPLUCK:593:1:1:1'],
        'HOVELAN'   => [4, 'HOVELAN:id:_:thoiHan',         'Hộ vệ lệnh lân', 'HOVELAN:882:5:0'],
        'CHOI'      => [5, 'CHOI:id:_:thoiHan:_',          'Chổi', 'CHOI:1:_:0:_'],
        'HKL'       => [2, 'HKL:thoiHan',                  'Hoả kỳ lân', 'HKL:30'],
        'BT'        => [4, 'BT:_:_:thoiHan',               'Pet bạch thú', 'BT:_:_:0'],
        'RONGBANG'  => [2, 'RONGBANG:thoiHan',             'Rồng băng', 'RONGBANG:0'],
        'SUTU'      => [2, 'SUTU:thoiHan',                 'Sư tử', 'SUTU:0'],
        'SUTUBANG'  => [2, 'SUTUBANG:thoiHan',             'Sư tử băng', 'SUTUBANG:0'],
        'LANT'      => [2, 'LANT:thoiHan',                 'Lân', 'LANT:0'],
        'DBT'       => [2, 'DBT:thoiHan',                  'Poro', 'DBT:0'],
        'DK'        => [1, 'DK',                           'Đường khang', 'DK'],
        'DKB'       => [2, 'DKB:thoiHan',                  'Đường khang băng', 'DKB:0'],
        'DKT'       => [2, 'DKT:thoiHan',                  'Đường khang có thời hạn', 'DKT:0'],
        'TL'        => [2, 'TL:thoiHan',                   'Tuần lộc', 'TL:0'],
        'PHDS'      => [1, 'PHDS',                         'Phượng hoàng đa sắc vĩnh viễn', 'PHDS'],
        'PHT'       => [2, 'PHT:thoiHan',                  'Phượng hoàng', 'PHT:30'],
        'PHDST'     => [2, 'PHDST:thoiHan',                'Phượng hoàng đa sắc', 'PHDST:30'],
        'PHMT'      => [2, 'PHMT:thoiHan',                 'Phượng hoàng mộc', 'PHMT:30'],
        'PHKT'      => [2, 'PHKT:thoiHan',                 'Phượng hoàng kim', 'PHKT:30'],
        'PHTT'      => [2, 'PHTT:thoiHan',                 'Phượng hoàng thổ', 'PHTT:30'],
        'PHBT'      => [2, 'PHBT:thoiHan',                 'Phượng hoàng băng', 'PHBT:30'],
        'DN'        => [3, 'DN:_:diemNap',                 'Cộng điểm nạp VIP', 'DN:500000:500000'],
        'MN'        => [3, 'MN:_:luongNap',                'Cộng lượng nạp', 'MN:_:500000'],
        // Diem doi thuong: cung kho diem ma NPC "doi thuong" tieu (Char.diemDoiThuong),
        // va cung kho ma van tieu / cuop tieu / danh quai cong vao. Tran 1000 diem/ngay
        // chi ap cho nguon DANH QUAI, khong chan phat bang giftcode.
        'DIEMDT'    => [2, 'DIEMDT:soDiem',                'Cộng điểm đổi thưởng (NPC “đổi thưởng”)', 'DIEMDT:500'],
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
        } else {
            // Tung phan sau loai PHAI la so nguyen (co the am, vd GEM:...:khoa). Server doc
            // bang Integer.parseInt(tachTime[i]) - mot phan khong phai so nem
            // NumberFormatException giua luc phat thuong. Truoc day server tu tru han va ghi
            // luot-da-dung TRUOC khi phat thuong (vá 03/09/2026) nen loi o day khong con lam
            // nguoi choi nhan lai vo han duoc nua, nhung van lam CHINH ho mat quyen loi (ma
            // coi nhu da dung ma khong nhan duoc gi) - bao ngay tu luc nhap de admin sua truoc.
            for ($i = 1; $i < count($tach); $i++) {
                if ($tach[$i] === '_') {
                    continue; // cho phep dung "_" lam tham so bo qua, dung o nhieu cu phap tren
                }
                if (!preg_match('/^-?\d+$/', $tach[$i])) {
                    $loi[] = '“' . $mot . '”: phần thứ ' . ($i + 1) . ' ("' . $tach[$i]
                           . '") phải là số nguyên (hoặc "_" để bỏ qua nếu cú pháp cho phép) — đúng phải là '
                           . $cuPhap . '.';
                }
            }
        }
        $tach[0] = $loaiPT;
        $phan[] = implode(':', $tach);
    }
    return [implode(',', $phan), $loi];
}

