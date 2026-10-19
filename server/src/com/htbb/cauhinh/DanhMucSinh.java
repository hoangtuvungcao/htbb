package com.htbb.cauhinh;

/**
 * SINH TU tools/cauhinh/danh_muc.json boi tools/cauhinh/sinh_cau_hinh.py - KHONG SUA TAY.
 * Danh muc moi khoa cau hinh trung tam (xem {@link CauHinhTrungTam}).
 */
final class DanhMucSinh {

    private DanhMucSinh() {
    }

    /** {ma, ten} theo thu tu hien thi. */
    static final String[][] NHOM = {
        {"kinh_te", "Kinh tế: tỉ lệ rơi & EXP"},
        {"su_kien", "Sự kiện & giftcode"},
        {"nap_tien", "Nạp tiền"},
        {"thanh_vien", "Thành viên & kích hoạt"},
        {"chong_clone", "Chống clone & chuyển xu"},
        {"tu_dong", "Tự động đánh & hồi sinh"},
        {"bao_tri", "Máy chủ, đăng ký & bảo trì"},
        {"che_do", "Chế độ game (cố định)"},
        {"dang_ky", "Đăng ký (cố định)"},
        {"chong_spam", "Chống spam, DoS & nhật ký (cố định)"},
        {"pvp", "PvP / Gym / Hầm Ngục (luôn bật)"},
        {"chi_so", "Chỉ số & chống lạm phát (cố định)"},
        {"hoi_sinh", "Hồi sinh — luật (cố định)"},
        {"thong_bao", "Thông báo (cố định)"},
        {"quan_tri", "Quản trị & thử nghiệm (cố định)"},
    };

    static DinhNghiaKhoa[] tatCa() {
        return new DinhNghiaKhoa[]{
            new DinhNghiaKhoa("sv.lau_dai", "che_do", "co", "1", false, "cau_hinh")
                    .nhan("Thiết kế dài hạn", "1 = bật GD2 (bảng EXP mới, chuyển EXP Pokémon một lần có sao lưu). Bật/tắt chạy chuyển đổi EXP lúc khởi động.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("sv.he_sinh_thai", "che_do", "co", "1", false, "cau_hinh")
                    .nhan("Hệ sinh thái", "1 = bật hệ sinh thái thế giới (data_hst_*, tob_hst_*).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("sv.tran_cap_theo_mua", "che_do", "co", "0", false, "cau_hinh")
                    .nhan("Trần cấp theo mùa", "1 = bật lại trần cấp 60→100 theo ngày khai trương (thiết kế cũ). 0 = không trần, lên cấp chậm + vật phẩm tăng tốc.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.ngay_khai_truong", "bao_tri", "ngay", "", false, "cau_hinh")
                    .nhan("Ngày khai trương", "yyyy-MM-dd — ngày 0 của lịch mùa. Để trống = theo lau_dai.ngay_khai_truong game tự ghi."),
            new DinhNghiaKhoa("sv.tua_nhanh_ngay", "che_do", "so", "0", false, "cau_hinh")
                    .nhan("Tua nhanh thử nghiệm (ngày)", "Cộng thêm N ngày vào ngày hiện tại của lịch dài hạn (thử mùa, vé mùa, lịch). chỉ máy thử — máy thật luôn 0.")
                    .khoang(0L, 3650L)
                    .khoaProd()
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tileXu", "kinh_te", "so", "4", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi xu (%)", "% so với tỉ lệ gốc. 100 = như cũ, 0 = tắt.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.tileLuong", "kinh_te", "so", "10", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi lượng (%)", "% so với tỉ lệ gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.tileItemSuKien", "kinh_te", "so", "10", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi đồ sự kiện (%)", "% so với tỉ lệ gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.tilePotion", "kinh_te", "so", "14", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi bình thuốc (%)", "% so với tỉ lệ gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.tileNgoc", "kinh_te", "so", "14", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi ngọc (%)", "% so với tỉ lệ gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.tileItem", "kinh_te", "so", "10", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi đồ thường (%)", "% so với tỉ lệ gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.chiaXuRoi", "kinh_te", "so", "30", true, "cau_hinh")
                    .nhan("Chia số xu rơi", "Xu rơi = xu gốc chia số này (tối thiểu 1).")
                    .khoang(1L, 1000000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tileExpLv50", "kinh_te", "so", "10", true, "cau_hinh")
                    .nhan("EXP quái từ cấp 50 (%)", "% so với EXP gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.tileExpDuoiLv50", "kinh_te", "so", "50", true, "cau_hinh")
                    .nhan("EXP quái dưới cấp 50 (%)", "% so với EXP gốc.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("sv.expvantieu", "kinh_te", "co", "1", true, "cau_hinh")
                    .nhan("Thưởng EXP giao hàng", "1 = bật.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.Luckyexp", "kinh_te", "co", "1", true, "cau_hinh")
                    .nhan("Ô EXP rương may mắn", "1 = bật.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.ExpQua", "kinh_te", "co", "1", true, "cau_hinh")
                    .nhan("EXP cây ăn quả", "1 = bật (cũng khoá nút tăng tốc cây).")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.giaXuBoHoaDoVuLan", "kinh_te", "so", "150000", true, "cau_hinh")
                    .nhan("Giá xu bó hoa Vu Lan", "Số xu.")
                    .khoang(0L, 2000000000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tieuNguongMau", "kinh_te", "ds_so", "60189,89389,99689,99989,99999", true, "cau_hinh")
                    .nhan("Giao hàng: ngưỡng màu khi nhận", "5 ngưỡng cộng dồn trên thang 100000 (Trắng..Vàng, còn lại Tím).")
                    .khoang(0L, 100000L)
                    .soPhanTu(5)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tieuHeSoExp", "kinh_te", "so", "200", true, "cau_hinh")
                    .nhan("Giao hàng: hệ số EXP chung (%)", "200 = ×2.")
                    .khoang(0L, 10000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tieuPcExpTheoMau", "kinh_te", "ds_so", "50,100,150,250,250,500,0", true, "cau_hinh")
                    .nhan("Giao hàng: % EXP theo màu", "7 số (Trắng..Tím, số cuối không dùng).")
                    .khoang(0L, 100000L)
                    .soPhanTu(7)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tieuPermilleCaoCap", "kinh_te", "ds_so", "50,70,90,35,55,75,5,25,45", true, "cau_hinh")
                    .nhan("Giao hàng: phần nghìn EXP cao cấp", "9 số (3 khung cấp × 3 màu).")
                    .khoang(0L, 1000L)
                    .soPhanTu(9)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.doiTieuNguongMauXu", "kinh_te", "ds_so", "50545,85545,98945,99945,99995", true, "cau_hinh")
                    .nhan("Đổi hàng (xu): ngưỡng màu", "5 ngưỡng cộng dồn.")
                    .khoang(0L, 100000L)
                    .soPhanTu(5)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.doiTieuNguongMauLuong", "kinh_te", "ds_so", "22890,52890,96890,99890,99990", true, "cau_hinh")
                    .nhan("Đổi hàng (lượng): ngưỡng màu", "5 ngưỡng cộng dồn.")
                    .khoang(0L, 100000L)
                    .soPhanTu(5)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("roi.xu.phan_tram", "kinh_te", "so", "30", true, "cau_hinh")
                    .nhan("Tỉ lệ một quái rơi xu (%) — dài hạn", "Xác suất rơi xu; số xu mỗi lần tự chia ngược để kỳ vọng xu/quái không đổi.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("roi.da_nang_cap.phan_nghin", "kinh_te", "so", "15", true, "cau_hinh")
                    .nhan("Tỉ lệ rơi đá nâng cấp (‰)", "Phần nghìn mỗi quái khi lệch cấp ≤ 5.")
                    .khoang(0L, 200L),
            new DinhNghiaKhoa("exp.nang_luong_phut", "kinh_te", "so", "90", true, "cau_hinh")
                    .nhan("Phút năng lượng 100% / ngày", "Số phút EXP/rơi đầy đủ mỗi ngày.")
                    .khoang(0L, 1440L),
            new DinhNghiaKhoa("exp.nang_luong_tich_phut", "kinh_te", "so", "180", true, "cau_hinh")
                    .nhan("Năng lượng tích luỹ tối đa (phút)", "Phút năng lượng dồn tối đa khi không chơi.")
                    .khoang(0L, 4320L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("thue.cho", "kinh_te", "so", "6", true, "cau_hinh")
                    .nhan("Thuế chợ ký gửi (%)", "Đốt khỏi nền kinh tế khi bán được.")
                    .khoang(0L, 50L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("thue.dang_ban", "kinh_te", "so", "1", true, "cau_hinh")
                    .nhan("Phí đăng bán (%)", "Đốt, không hoàn; tối thiểu 1 xu.")
                    .khoang(0L, 20L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("thue.chuyen_luong", "kinh_te", "so", "5", true, "cau_hinh")
                    .nhan("Phí chuyển lượng (%)", "Làm tròn lên, tối thiểu 1 khi > 0.")
                    .khoang(0L, 50L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("thue.tran_chuyen_xu_gio", "kinh_te", "so", "5", true, "cau_hinh")
                    .nhan("Trần chuyển xu / ngày (giờ thu nhập)", "Bội số thu nhập 1 giờ ở cấp người gửi.")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("phi.dich_chuyen_phan_tram_gio", "kinh_te", "so", "3", true, "cau_hinh")
                    .nhan("Phí dịch chuyển (% thu nhập 1 giờ)", "Tối thiểu 1 xu.")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("phi.hoi_sinh_phan_tram_gio", "kinh_te", "so", "5", true, "cau_hinh")
                    .nhan("Phí hồi sinh (% thu nhập 1 giờ)", "Tối thiểu 1 xu.")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("phi.ban_npc_phan_tram", "kinh_te", "so", "5", true, "cau_hinh")
                    .nhan("Giá bán đồ cho NPC (% giá)", "Không cộng theo cấp cường hoá.")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tieu_hao.sua_do_phan_tram_thu_nhap", "kinh_te", "so", "8", true, "cau_hinh")
                    .nhan("Trần phí sửa đồ (% thu nhập 1 giờ / món)", "Phí sửa = min(công thức cũ, trần × số món).")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("giftcode.toi_da_thang", "su_kien", "so", "2", true, "cau_hinh")
                    .nhan("Giftcode dài hạn tối đa / tháng", "Số giftcode một Pokémon nhập được mỗi tháng.")
                    .khoang(0L, 20L),
            new DinhNghiaKhoa("giftcode.gia_tri_toi_da", "su_kien", "so", "30", true, "cau_hinh")
                    .nhan("Trần giá trị một giftcode", "Điểm giá trị tối đa của một mã (GiftcodeAnToanLogic).")
                    .khoang(0L, 100000L),
            new DinhNghiaKhoa("giftcode.ngan_sach_thang", "su_kien", "so", "20000", true, "cau_hinh")
                    .nhan("Ngân sách giftcode / tháng", "Tổng giá trị giftcode được phát mỗi tháng.")
                    .khoang(0L, 100000000L),
            new DinhNghiaKhoa("sv.nap_tien", "nap_tien", "co", "0", false, "cau_hinh")
                    .nhan("Hệ thống nạp tiền (máy chủ game)", "Công tắc tổng phía game: 1 = bật (chỉ chạy khi sv.lau_dai=1). Phía web còn cần HTBB_NAP_BAT."),
            new DinhNghiaKhoa("HTBB_NAP_BAT", "nap_tien", "co", "0", false, "env")
                    .nhan("Web: bật nạp tiền (biến môi trường)", "Đặt trong .env của web, không sửa từ trang này. Tắt hoặc cấu hình cổng chưa an toàn = KHOÁ toàn bộ thu tiền.")
                    .chiDoc(),
            new DinhNghiaKhoa("NAP_DA_CO_GIAY_PHEP", "nap_tien", "co", "0", false, "env")
                    .nhan("Xác nhận giấy phép hình ảnh/IP (biến môi trường)", "Chủ máy chủ tự xác nhận đã có giấy phép. Thiếu = không bán Thành viên, cảnh báo đỏ ở trang Nạp tiền.")
                    .chiDoc(),
            new DinhNghiaKhoa("nap.luong.bat", "nap_tien", "co", "1", true, "cau_hinh")
                    .nhan("Bán gói lượng", "0 = web ngừng tạo đơn gói lượng mới (đơn đã thanh toán vẫn được cộng)."),
            new DinhNghiaKhoa("nap.the_thang.bat", "nap_tien", "co", "1", true, "cau_hinh")
                    .nhan("Bán thẻ tháng", "0 = web ngừng tạo đơn thẻ tháng mới. Người đã mua vẫn nhận lượng hằng ngày."),
            new DinhNghiaKhoa("nap.ve_mua.bat", "nap_tien", "co", "1", true, "cau_hinh")
                    .nhan("Bán vé mùa cao cấp", "0 = web ngừng tạo đơn vé mùa mới."),
            new DinhNghiaKhoa("thanh_vien.ban_bat", "nap_tien", "co", "0", true, "cau_hinh")
                    .nhan("Bán Thành viên HTBB", "1 = mở bán (còn cần giấy phép + cổng an toàn). Cùng khoá với trang Thành viên."),
            new DinhNghiaKhoa("nap.tang_toc.bat", "nap_tien", "co", "1", true, "cau_hinh")
                    .nhan("Bán vật phẩm tăng tốc (bằng lượng, trong game)", "0 = cửa hàng lượng trong game ngừng bán vé EXP / bùa thể lực / ủy thác / kẹo hiếm bản cao cấp."),
            new DinhNghiaKhoa("nap.hoan_tien.thu_cong", "nap_tien", "co", "1", true, "cau_hinh")
                    .nhan("Cho phép hoàn tiền thủ công", "0 = khoá nút Hoàn tiền / Chargeback ở trang Nạp tiền.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("nap.hoan_tien.tu_dong", "nap_tien", "co", "1", true, "cau_hinh")
                    .nhan("Tự động thu hồi khi hoàn đơn đã cộng", "1 = máy chủ game tự chuyển lượng thành nợ / trừ thẻ tháng / gỡ vé mùa cho đơn 'chờ thu hồi'. 0 = đơn đứng chờ, xử lý tay.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("tran_tuan_vnd", "nap_tien", "so", "1000000", true, "nap_cau_hinh")
                    .nhan("Trần nạp / tuần / tài khoản (VND)", "0 = mặc định 1.000.000 đ. Bị kẹp ≤ 5.000.000.")
                    .khoang(0L, 5000000L),
            new DinhNghiaKhoa("tran_thang_vnd", "nap_tien", "so", "2000000", true, "nap_cau_hinh")
                    .nhan("Trần nạp / tháng / tài khoản (VND)", "0 = mặc định 2.000.000 đ. Bị kẹp ≤ 5.000.000.")
                    .khoang(0L, 5000000L),
            new DinhNghiaKhoa("thanh_vien.bat", "thanh_vien", "co", "0", true, "cau_hinh")
                    .nhan("Bật yêu cầu Thành viên", "1 = các tính năng trong danh sách dưới chỉ dành cho Thành viên. 0 = không khoá gì."),
            new DinhNghiaKhoa("thanh_vien.gia", "thanh_vien", "so", "20000", true, "cau_hinh")
                    .nhan("Giá Thành viên (VND)", "Đồng bộ sang gói nạp THANH_VIEN khi lưu.")
                    .khoang(10000L, 2000000L)
                    .buoc(1000L),
            new DinhNghiaKhoa("thanh_vien.thoi_han_ngay", "thanh_vien", "so", "0", true, "cau_hinh")
                    .nhan("Thời hạn (ngày)", "0 = vĩnh viễn.")
                    .khoang(0L, 36500L),
            new DinhNghiaKhoa("thanh_vien.chuc_nang", "thanh_vien", "ds_chuoi", "giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode", true, "cau_hinh")
                    .nhan("Tính năng yêu cầu Thành viên", "Chọn các tính năng bị khoá với tài khoản thường.")
                    .luaChon(new String[]{"giao_dich", "nhan_tang", "chat_the_gioi", "cho", "cho_sinh_thai", "chuyen_luong", "bang", "pvp_xep_hang", "giftcode", "qua_online", "gui_thu"}),
            new DinhNghiaKhoa("thanh_vien.mien_cap", "thanh_vien", "so", "0", true, "cau_hinh")
                    .nhan("Miễn yêu cầu dưới cấp", "Người chơi dưới cấp này được dùng thử (0 = không miễn).")
                    .khoang(0L, 200L),
            new DinhNghiaKhoa("thanh_vien.thong_bao", "thanh_vien", "chuoi", "", true, "cau_hinh")
                    .nhan("Câu thông báo khi bị khoá", "Trống = câu mặc định.")
                    .doDai(250),
            new DinhNghiaKhoa("kich_hoat.chan_dang_nhap", "thanh_vien", "co", "0", true, "cau_hinh")
                    .nhan("Chặn đăng nhập tài khoản chưa kích hoạt", "0 (mặc định) = chưa kích hoạt vẫn vào chơi, chỉ bị khoá các tính năng dưới. 1 = hành vi cũ: không vào được game. Chỉ có tác dụng khi sv.EnableAvtiveCheck=1."),
            new DinhNghiaKhoa("kich_hoat.chuc_nang", "thanh_vien", "ds_chuoi", "giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang,giftcode", true, "cau_hinh")
                    .nhan("Tính năng khoá khi chưa kích hoạt", "Cùng cổng với Thành viên: bị chặn nếu (cần kích hoạt và chưa kích hoạt) hoặc (cần Thành viên và chưa là Thành viên).")
                    .luaChon(new String[]{"giao_dich", "nhan_tang", "chat_the_gioi", "cho", "cho_sinh_thai", "chuyen_luong", "bang", "pvp_xep_hang", "giftcode", "qua_online", "gui_thu"}),
            new DinhNghiaKhoa("kich_hoat.thong_bao", "thanh_vien", "chuoi", "", true, "cau_hinh")
                    .nhan("Câu thông báo khi bị khoá vì chưa kích hoạt", "Trống = câu mặc định (chỉ tới /kichhoat). Thay được {ten} {web}.")
                    .doDai(250),
            new DinhNghiaKhoa("kich_hoat.thanh_vien_la_kich_hoat", "thanh_vien", "co", "1", true, "cau_hinh")
                    .nhan("Thành viên được coi như đã kích hoạt", "1 (mặc định) = tài khoản là Thành viên HTBB không bị khoá vì chưa kích hoạt (gộp hai bậc làm một nếu muốn).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("chong_clone.bat", "chong_clone", "co", "1", true, "cau_hinh")
                    .nhan("Bật chống clone", "1 = áp các cổng dưới cho mọi đường chuyển xu / giá trị giữa hai người chơi. 0 = tắt hết (chỉ để khẩn cấp)."),
            new DinhNghiaKhoa("chong_clone.cap_toi_thieu", "chong_clone", "so", "20", true, "cau_hinh")
                    .nhan("Cấp tối thiểu để chuyển / nhận xu", "Cả hai bên của giao dịch, ký gửi, giao dịch thú cưng lấy xu, chợ trang trí / sinh thái, thách đấu cược xu.")
                    .khoang(0L, 200L),
            new DinhNghiaKhoa("chong_clone.tuoi_tai_khoan_gio", "chong_clone", "so", "24", true, "cau_hinh")
                    .nhan("Tuổi tài khoản tối thiểu (giờ)", "Tính từ lúc đăng ký (team_user.regdate). 0 = không xét.")
                    .khoang(0L, 2160L),
            new DinhNghiaKhoa("chong_clone.gio_choi_toi_thieu", "chong_clone", "so", "10", true, "cau_hinh")
                    .nhan("Giờ chơi tối thiểu của Pokémon (giờ)", "tob_char.totalTimePlay + phiên đang chơi. 0 = không xét.")
                    .khoang(0L, 1000L),
            new DinhNghiaKhoa("chong_clone.nv_cung_may_toi_da", "chong_clone", "so", "2", true, "cau_hinh")
                    .nhan("Pokémon cùng thiết bị / IP gửi cho một người / ngày", "Mỗi người nhận, mỗi ngày: tối đa N Pokémon (tài khoản phụ) dùng chung thiết bị hoặc IP được chuyển cho họ; Pokémon thứ N+1 bị chặn. IP proxy tin cậy chỉ tính theo thiết bị.")
                    .khoang(1L, 50L),
            new DinhNghiaKhoa("chong_clone.nghi_so_nguoi_gui", "chong_clone", "so", "3", true, "cau_hinh")
                    .nhan("Ngưỡng \"nghi clone\" (số người gửi / ngày)", "Một người nhận có từ N người gửi khác nhau trong ngày thì đánh dấu nghi (không chặn), hiện ở /admin/kinh-te.")
                    .khoang(2L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chong_clone.ky_gui_gia_toi_da_lan", "chong_clone", "so", "10", true, "cau_hinh")
                    .nhan("Giá ký gửi tối đa (lần giá gốc)", "Giá rao ≤ max(giá gốc × N, thu nhập 1 giờ ở cấp người bán × số giờ bên dưới).")
                    .khoang(1L, 1000L),
            new DinhNghiaKhoa("chong_clone.ky_gui_gia_toi_da_gio", "chong_clone", "so", "200", true, "cau_hinh")
                    .nhan("Giá ký gửi tối đa (giờ thu nhập)", "Trần cho món không có giá gốc (và sàn của trần trên).")
                    .khoang(1L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chong_clone.ky_gui_gia_toi_thieu_phan_tram", "chong_clone", "so", "5", true, "cau_hinh")
                    .nhan("Giá ký gửi tối thiểu (% giá gốc)", "Chặn bán phá giá cho clone.")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chong_clone.ban_npc_do_khoa", "chong_clone", "co", "0", true, "cau_hinh")
                    .nhan("Bán đồ / nguyên liệu khoá cho NPC lấy xu", "0 = cấm (đồ khoá mua bằng lượng khoá không đổi ra xu được). 1 = cho như cũ.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.dangKyTrongGame", "bao_tri", "co", "1", true, "cau_hinh")
                    .nhan("Đăng ký trong game", "0 = nút Đăng ký trong game báo 'đăng ký trên web'."),
            new DinhNghiaKhoa("sv.dangKyTuKichHoat", "bao_tri", "chon", "web", true, "cau_hinh")
                    .nhan("Tự kích hoạt tài khoản đăng ký trong game", "web = theo cấu hình web (web.autoActivate).")
                    .luaChon(new String[]{"web", "1", "0"}),
            new DinhNghiaKhoa("sv.dangKyTreMs", "dang_ky", "so", "800", true, "cau_hinh")
                    .nhan("Trễ phản hồi đăng ký (ms)", "Làm chậm dò tài khoản.")
                    .khoang(0L, 5000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.dangKyCsdlTaiKhoan", "dang_ky", "chuoi", "account", false, "cau_hinh")
                    .nhan("CSDL tài khoản", "Tên database chứa team_user.")
                    .mau("^[A-Za-z0-9_]{1,64}$")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("dang_ky.web.bat", "bao_tri", "co", "1", true, "cau_hinh")
                    .nhan("Đăng ký trên web", "0 = trang Đăng ký từ chối tài khoản mới."),
            new DinhNghiaKhoa("sv.maxCharPerAccount", "bao_tri", "so", "3", true, "cau_hinh")
                    .nhan("Số Pokémon tối đa / tài khoản", "Client hỗ trợ tối đa 3.")
                    .khoang(1L, 3L),
            new DinhNghiaKhoa("sv.suKienTuKhoiDongLai", "su_kien", "co", "1", true, "cau_hinh")
                    .nhan("Tự khởi động lại khi đổi sự kiện", "1 = lệnh bật/tắt sự kiện có hẹn sẽ tự khởi động lại máy chủ.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.suKienGomGiay", "su_kien", "so", "120", true, "cau_hinh")
                    .nhan("Gom lệnh sự kiện (giây)", "Gom nhiều lệnh thành một lần khởi động lại.")
                    .khoang(30L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.suKienBaoTruocGiay", "su_kien", "so", "60", true, "cau_hinh")
                    .nhan("Báo trước khi đổi sự kiện (giây)", "")
                    .khoang(10L, 600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.worldCupBatDau", "su_kien", "ngay_gio", "", true, "cau_hinh")
                    .nhan("World Cup bắt đầu", "yyyy-MM-dd HH:mm:ss. Trống = tắt.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.worldCupKetThuc", "su_kien", "ngay_gio", "", true, "cau_hinh")
                    .nhan("World Cup kết thúc", "Trống = tắt.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.worldCupHanNhanQua", "su_kien", "ngay_gio", "", true, "cau_hinh")
                    .nhan("World Cup hạn nhận quà", "Trống = tắt.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.gioChiemThanh", "su_kien", "so", "-1", true, "cau_hinh")
                    .nhan("Giờ chiếm Gym", "0–23, −1 = lịch chuẩn 21h thứ 3 + thứ 7.")
                    .khoang(-1L, 23L),
            new DinhNghiaKhoa("sv.phutChiemThanh", "su_kien", "so", "-1", true, "cau_hinh")
                    .nhan("Phút chiếm Gym", "0–59.")
                    .khoang(-1L, 59L),
            new DinhNghiaKhoa("sv.ngayChiemThanh", "su_kien", "chuoi", "", true, "cau_hinh")
                    .nhan("Ngày chiếm Gym", "Mon,Tue,... hoặc yyyy-MM-dd; trống = mỗi ngày.")
                    .mau("^$|^((Mon|Tue|Wed|Thu|Fri|Sat|Sun)(,(Mon|Tue|Wed|Thu|Fri|Sat|Sun))*|\\d{4}-\\d{2}-\\d{2})$")
                    .doDai(64),
            new DinhNghiaKhoa("sv.isSuKienTrungThul2016", "su_kien", "co", "0", false, "cau_hinh")
                    .nhan("Sự kiện cũ: Trung thu 2016", "Cờ sự kiện gốc (đọc lúc khởi động).")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.isSuKienTet2017", "su_kien", "co", "0", false, "cau_hinh")
                    .nhan("Sự kiện cũ: Tết 2017", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.isSuKienGioTo2016", "su_kien", "co", "0", false, "cau_hinh")
                    .nhan("Sự kiện cũ: Giỗ Tổ 2016", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.isSuKienNoel2023", "su_kien", "co", "0", false, "cau_hinh")
                    .nhan("Sự kiện cũ: Noel 2023", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.isSuKienTetduonglich2024", "su_kien", "co", "0", false, "cau_hinh")
                    .nhan("Sự kiện cũ: Tết dương lịch 2024", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("bao_tri.bat", "bao_tri", "co", "0", true, "cau_hinh")
                    .nhan("Chế độ bảo trì đăng nhập", "1 = từ chối đăng nhập mới (người đang chơi không bị đá). Tương đương tệp bao_tri.txt — bật một trong hai là bảo trì."),
            new DinhNghiaKhoa("bao_tri.cau", "bao_tri", "chuoi", "", true, "cau_hinh")
                    .nhan("Câu thông báo bảo trì", "Trống = câu mặc định.")
                    .doDai(200),
            new DinhNghiaKhoa("bao_tri.cho", "bao_tri", "ds_chuoi", "", true, "cau_hinh")
                    .nhan("Tài khoản được vào khi bảo trì", "Tên tài khoản (chữ thường), cách nhau dấu phẩy.")
                    .mau("^[a-z0-9_]{3,20}$")
                    .doDai(255),
            new DinhNghiaKhoa("sv.gioBaoTri", "bao_tri", "chuoi", "", true, "cau_hinh")
                    .nhan("Giờ bảo trì hằng ngày", "HH:MM cố định, 'tat' = không tự bảo trì, trống = ngẫu nhiên 4:30–5:30.")
                    .mau("^$|^(tat|off)$|^([01]?\\d|2[0-3]):[0-5]\\d$")
                    .doDai(5),
            new DinhNghiaKhoa("sv.chiSoMayChu", "bao_tri", "co", "1", false, "cau_hinh")
                    .nhan("Ghi chỉ số máy chủ + đọc bao_tri.txt", "0 = tắt luồng chỉ số (tob_chi_so_may_chu).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("sv.spam.bat", "chong_spam", "co", "1", true, "cau_hinh")
                    .nhan("Chống spam chat / thao tác", "0 = tắt toàn bộ.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("sv.spam.chat.gan", "chong_spam", "nhip", "5/10", true, "cau_hinh")
                    .nhan("Nhịp chat gần", "số tin / giây (vd 5/10).")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.chat.rieng", "chong_spam", "nhip", "6/10", true, "cau_hinh")
                    .nhan("Nhịp chat riêng", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.chat.theGioi", "chong_spam", "nhip", "1/30", true, "cau_hinh")
                    .nhan("Nhịp chat thế giới / người", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.theGioiToanServer", "chong_spam", "nhip", "12/60", true, "cau_hinh")
                    .nhan("Nhịp chat thế giới toàn máy chủ", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.chat.doi", "chong_spam", "nhip", "8/10", true, "cau_hinh")
                    .nhan("Nhịp chat đội", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.chat.bang", "chong_spam", "nhip", "8/10", true, "cau_hinh")
                    .nhan("Nhịp chat đội", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.trungLapGiay", "chong_spam", "so", "30", true, "cau_hinh")
                    .nhan("Chặn tin trùng lặp (giây)", "")
                    .khoang(0L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.chanQuangCao", "chong_spam", "co", "1", true, "cau_hinh")
                    .nhan("Chặn quảng cáo / link", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.viPhamCam", "chong_spam", "so", "4", true, "cau_hinh")
                    .nhan("Số vi phạm thì cấm chat", "")
                    .khoang(1L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.spam.camGiay", "chong_spam", "ds_so", "60,300,1800,7200", true, "cau_hinh")
                    .nhan("Thang cấm chat (giây)", "Các bậc cấm tăng dần, cách nhau dấu phẩy.")
                    .khoang(1L, 31536000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.socketTimeout", "chong_spam", "so", "180", true, "cau_hinh")
                    .nhan("Hết hạn socket (giây)", "Áp dụng cho kết nối mới. 0 = tắt.")
                    .khoang(0L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.maxConnPerIp", "chong_spam", "so", "50", true, "cau_hinh")
                    .nhan("Kết nối tối đa / IP", "0 = tắt.")
                    .khoang(0L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.connRateMax", "chong_spam", "so", "60", true, "cau_hinh")
                    .nhan("Số kết nối tối đa / cửa sổ", "0 = tắt.")
                    .khoang(0L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.connRateWindow", "chong_spam", "so", "30", true, "cau_hinh")
                    .nhan("Cửa sổ đếm kết nối (giây)", "0 = tắt.")
                    .khoang(0L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.dosBanSec", "chong_spam", "so", "300", true, "cau_hinh")
                    .nhan("Thời gian chặn IP ngập (giây)", "")
                    .khoang(0L, 86400L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.reloginWaitSec", "chong_spam", "so", "0", true, "cau_hinh")
                    .nhan("Chờ đăng nhập lại (giây)", "")
                    .khoang(0L, 600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.proxyTinCay", "chong_spam", "ds_chuoi", "127.0.0.1,::1", true, "cau_hinh")
                    .nhan("Proxy tin cậy (miễn giới hạn theo IP)", "IP hoặc dải CIDR (IPv4/IPv6) của WAF / Nova agent, cách nhau dấu phẩy. Kết nối từ đây không bị tính sv.maxConnPerIp, sv.connRate*, chặn sv.dosBanSec, chờ đăng nhập theo IP, hạn mức đăng ký theo IP. Giới hạn theo tài khoản / kết nối / thiết bị vẫn giữ.")
                    .mau("^[0-9A-Fa-f:.]{2,45}(/[0-9]{1,3})?$")
                    .doDai(255)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.dangNhapIpNguong", "chong_spam", "so", "10", true, "cau_hinh")
                    .nhan("Ngưỡng đăng nhập dồn dập / IP", "Số lần đăng nhập trong ~1 giây từ một IP (không phải proxy tin cậy) thì bắt đầu chờ + ghi nghi vấn. 0 = tắt.")
                    .khoang(0L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.dangNhapIpChoMs", "chong_spam", "so", "3000", true, "cau_hinh")
                    .nhan("Chờ khi đăng nhập dồn dập (ms)", "Thời gian chờ mỗi lần đăng nhập khi vượt sv.dangNhapIpNguong. 0 = không chờ.")
                    .khoang(0L, 10000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tranGoiThuong", "chong_spam", "so", "300", true, "cau_hinh")
                    .nhan("Trần gói tin thường / cửa sổ", "")
                    .khoang(10L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tranGoiTaiNguyen", "chong_spam", "so", "600", true, "cau_hinh")
                    .nhan("Trần gói tải tài nguyên / cửa sổ", "")
                    .khoang(10L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.tranGoiTraCuu", "chong_spam", "so", "600", true, "cau_hinh")
                    .nhan("Trần gói tra cứu / cửa sổ", "")
                    .khoang(10L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.log.muc", "chong_spam", "chon", "THONG_TIN", true, "cau_hinh")
                    .nhan("Mức nhật ký (màn hình)", "")
                    .luaChon(new String[]{"LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"})
                    .hoa()
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.log.mucTep", "chong_spam", "chon", "", true, "cau_hinh")
                    .nhan("Mức nhật ký (tệp)", "Trống = như màn hình.")
                    .luaChon(new String[]{"", "LOI", "CANH_BAO", "THONG_TIN", "GO_LOI", "ERROR", "WARN", "INFO", "DEBUG"})
                    .hoa()
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.log.tat", "chong_spam", "ds_chuoi", "GOI3,GOI5,GOI26,VKTT,SQL,LUU_CHAR", true, "cau_hinh")
                    .nhan("Loại nhật ký tắt", "Mã loại, cách nhau dấu phẩy.")
                    .mau("^[A-Z0-9_]{1,32}$")
                    .doDai(255)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.log.kiemToan", "chong_spam", "ds_chuoi", "ADMIN,GM,NAP,NAP_TIEN,GIFTCODE,CAM,BAN,KINH_TE,KINHTE,CHONG_SPAM", true, "cau_hinh")
                    .nhan("Loại nhật ký kiểm toán", "Ghi riêng tệp kiem_toan (giữ lâu).")
                    .mau("^[A-Z0-9_]{1,32}$")
                    .doDai(255)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.bat", "chi_so", "co", "1", true, "cau_hinh")
                    .nhan("Bật giới hạn chỉ số", "0 = tắt mọi trần/giảm dần chỉ số (quay về công thức cũ).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("chi_so.kep_trang_bi", "chi_so", "co", "1", true, "cau_hinh")
                    .nhan("Kẹp chỉ số trang bị theo ngân sách", "Mỗi món ≤ chuẩn bậc × 1,30 × nâng cấp; đồ bậc cũ ≤ 130% đồ bậc hiện tại.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("chi_so.tran_chi_mang", "chi_so", "so", "400", true, "cau_hinh")
                    .nhan("Trần tỉ lệ chí mạng (‰)", "Phần nghìn, có giảm dần. PvP cố định 300‰.")
                    .khoang(100L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_st_chi_mang", "chi_so", "so", "500", true, "cau_hinh")
                    .nhan("Trần ST chí mạng cộng thêm (‰)", "Trên nền ×1,5. 500 = đòn chí mạng tối đa ×2,0.")
                    .khoang(0L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_ne", "chi_so", "so", "300", true, "cau_hinh")
                    .nhan("Trần né tránh (‰)", "Phần nghìn, có giảm dần. PvP cố định 200‰.")
                    .khoang(0L, 900L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_hut", "chi_so", "so", "100", true, "cau_hinh")
                    .nhan("Trần hút HP PvE (‰ sát thương)", "Gộp mọi nguồn hút HP.")
                    .khoang(0L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_hut_pvp", "chi_so", "so", "50", true, "cau_hinh")
                    .nhan("Trần hút HP PvP (‰ sát thương thật)", "Gộp mọi nguồn hút HP, tính trên sát thương sau ×0,30.")
                    .khoang(0L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_giam_st", "chi_so", "so", "500", true, "cau_hinh")
                    .nhan("Trần giảm sát thương nhận (‰)", "Gộp mọi nguồn thụ động (PvP cố định 400‰).")
                    .khoang(0L, 900L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_khang_he", "chi_so", "so", "600", true, "cau_hinh")
                    .nhan("Trần kháng một hệ (‰)", "Kháng hệ + kháng mọi hệ + phiến đá.")
                    .khoang(0L, 900L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.san_sat_thuong", "chi_so", "so", "400", true, "cau_hinh")
                    .nhan("Sàn sát thương mỗi đòn (‰)", "Sau mọi lớp giảm, một đòn còn ít nhất chừng này phần nghìn (chống bất tử).")
                    .khoang(0L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("chi_so.tran_phan_tram_cong", "chi_so", "so", "500", true, "cau_hinh")
                    .nhan("Trần tổng % cộng thêm (‰)", "Tổng % tấn công/phòng thủ/HP từ pet, thú cưỡi, thời trang, bộ, VIP… so với chỉ số gốc.")
                    .khoang(0L, 5000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("danh_hieu.bat", "chi_so", "co", "1", true, "cau_hinh")
                    .nhan("Bật danh hiệu thành tựu", "0 = ngừng tự mở khoá danh hiệu và tắt dòng chỉ số của danh hiệu đang đeo (danh hiệu đã có vẫn giữ, vẫn hiện trên đầu). Danh mục + điều kiện ở bảng data_danh_hieu.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("danh_hieu.tran_phan_nghin", "chi_so", "so", "10", true, "cau_hinh")
                    .nhan("Trần chỉ số ô danh hiệu (‰)", "Ngân sách ô danh hiệu (thiết kế cân bằng 4.5, đề xuất 1%): chỉ một danh hiệu đang đeo được tính, giá trị từng danh hiệu (data_danh_hieu.phan_nghin) bị kẹp về trần này và vào trần tổng trang trí. 10 = +1%.")
                    .khoang(0L, 30L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("pvp.tinh_diem.bat", "pvp", "co", "1", true, "cau_hinh")
                    .nhan("Tính điểm thang PvP", "0 = trận PvP không cộng/trừ điểm thang.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("gym.bat", "pvp", "co", "1", true, "cau_hinh")
                    .nhan("Thách đấu Gym", "0 = tạm đóng Gym.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("ham_nguc.bat", "pvp", "co", "1", true, "cau_hinh")
                    .nhan("Hầm Ngục bí ẩn", "0 = không vào tầng mới (vẫn nhận thưởng đã đạt).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("tu_dong.bat", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Bật Tự động đánh", "0 = tạm khoá (người đang dùng bị dừng ở lần kiểm tiếp theo).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("tu_dong.gio_moi_ngay", "tu_dong", "so", "5", true, "cau_hinh")
                    .nhan("Giờ Tự động đánh mỗi ngày", "Miễn phí, chỉ tính lúc online và đang bật. Đặt lại lúc 00:00.")
                    .khoang(0L, 24L),
            new DinhNghiaKhoa("tu_dong.gio_them_the_thang", "tu_dong", "so", "2", true, "cau_hinh")
                    .nhan("Giờ cộng thêm khi có thẻ tháng", "Cộng vào giờ mỗi ngày khi thẻ tháng còn hạn.")
                    .khoang(0L, 24L),
            new DinhNghiaKhoa("tu_dong.chi_map_da_mo", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Chỉ ở bãi luyện tập", "1 = chỉ bật ở bản đồ có Pokémon hoang dã đã mở (không ở thành phố, nhà, sàn đấu, sự kiện).")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.tat_khi_pvp", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Dừng khi PvP / sàn đấu / Gym / boss", "1 = dừng khi bị Pokémon khác tấn công, bật PK, vào sàn đấu / Gym / chiến trường hay đánh boss.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.tu_dung_thuoc", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Cho tự uống thuốc", "0 = Tự động đánh không tự uống thuốc hồi phục (người chơi tự bấm).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("tu_dong.ban_kinh", "tu_dong", "so", "240", true, "cau_hinh")
                    .nhan("Bán kính tối đa (px)", "Pokémon chỉ đánh quanh điểm bật trong bán kính này; ra xa hơn (+64 px) thì dừng. 1 ô = 16 px.")
                    .khoang(48L, 960L)
                    .buoc(16L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nhip_danh", "tu_dong", "nhip", "12/5", true, "cau_hinh")
                    .nhan("Nhịp đánh tối đa khi tự động", "số lệnh đánh / giây (vd 12/5). Vượt thì bỏ lệnh và tính vi phạm.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nhip_thuoc", "tu_dong", "nhip", "4/5", true, "cau_hinh")
                    .nhan("Nhịp uống thuốc tối đa khi tự động", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nhip_nhat", "tu_dong", "nhip", "8/5", true, "cau_hinh")
                    .nhan("Nhịp nhặt đồ tối đa khi tự động", "")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nhip_nguoi", "tu_dong", "nhip", "30/5", true, "cau_hinh")
                    .nhan("Nhịp đánh nghi bot (mọi người chơi)", "Chỉ ghi nghi vấn (hack.txt), không chặn.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.vi_pham_tat", "tu_dong", "so", "20", true, "cau_hinh")
                    .nhan("Số vi phạm nhịp thì dừng", "Dừng Tự động đánh + ghi nghi vấn.")
                    .khoang(1L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.hoi_sinh_moi_gio", "tu_dong", "so", "2", true, "cau_hinh")
                    .nhan("Tự hồi sinh tối đa / giờ", "Khi đang tự động mà Pokémon gục: chỉ dùng vật phẩm hồi sinh nếu người chơi bật \"Tự hồi sinh\", tối đa ngần này lần / 60 phút; còn lại dừng tự động và về Trung tâm Pokémon. 0 = không bao giờ.")
                    .khoang(0L, 20L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.ke_hoach", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Máy chủ điều khiển từng bước", "1 = máy chủ chọn mục tiêu, chiêu, thuốc, nhặt đồ, rút lui cho client 0.9.2+ (client chỉ thực hiện). 0 = client tự chọn như bản 0.9.1 (máy chủ vẫn giới hạn giờ / nhịp / vùng).")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.cap_toi_thieu", "tu_dong", "so", "0", true, "cau_hinh")
                    .nhan("Cấp tối thiểu để bật", "0 = mọi cấp. Dưới cấp này bấm Bắt đầu thì được báo rõ cấp cần.")
                    .khoang(0L, 1000L),
            new DinhNghiaKhoa("tu_dong.chenh_cap_tren", "tu_dong", "so", "5", true, "cau_hinh")
                    .nhan("Bỏ Pokémon cao hơn quá N cấp", "Không đánh Pokémon hoang dã cao hơn cấp mình quá ngần này. -1 = không giới hạn.")
                    .khoang(-1L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.chenh_cap_duoi", "tu_dong", "so", "0", true, "cau_hinh")
                    .nhan("Bỏ Pokémon thấp hơn quá N cấp", "0 = không giới hạn dưới (vẫn đánh Pokémon yếu).")
                    .khoang(0L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.danh_boss", "tu_dong", "co", "0", true, "cau_hinh")
                    .nhan("Cho đánh boss", "0 = không chọn boss / Pokémon huyền thoại làm mục tiêu (đánh trúng boss vẫn dừng theo tu_dong.tat_khi_pvp)."),
            new DinhNghiaKhoa("tu_dong.hp_quai_toi_da_pt", "tu_dong", "so", "500", true, "cau_hinh")
                    .nhan("Bỏ Pokémon quá trâu (% HP mình)", "Bỏ Pokémon có HP tối đa lớn hơn ngần này % HP tối đa của mình. 0 = tắt.")
                    .khoang(0L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.chon_chieu_theo_he", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Chọn chiêu theo khắc hệ", "1 = điểm chiêu = sát thương × khắc hệ × STAB, bỏ chiêu đang hồi / thiếu PP. 0 = chỉ theo sát thương.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nguong_thuoc_hp", "tu_dong", "so", "50", true, "cau_hinh")
                    .nhan("Ngưỡng thuốc HP mặc định (%)", "Uống thuốc HP khi HP dưới ngưỡng người chơi chọn trong bảng; client cũ dùng số này. 0 = máy chủ không cho tự uống thuốc HP.")
                    .khoang(0L, 90L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nguong_thuoc_mp", "tu_dong", "so", "30", true, "cau_hinh")
                    .nhan("Ngưỡng thuốc PP mặc định (%)", "Như trên cho PP. 0 = tắt.")
                    .khoang(0L, 90L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nguong_hp_thap", "tu_dong", "so", "25", true, "cau_hinh")
                    .nhan("HP thấp thì rút lui (%)", "HP dưới ngưỡng mà hết thuốc: rút về điểm bắt đầu nghỉ (hoặc dừng nếu tu_dong.hp_thap_dung=1). 0 = tắt.")
                    .khoang(0L, 90L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.hp_thap_dung", "tu_dong", "co", "0", true, "cau_hinh")
                    .nhan("HP thấp thì dừng hẳn", "1 = dừng Tự động đánh thay vì rút lui nghỉ hồi phục.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.hoi_phuc_toi", "tu_dong", "so", "80", true, "cau_hinh")
                    .nhan("Nghỉ hồi phục tới (%)", "Rút lui nghỉ tới khi HP đạt ngần này % rồi đánh tiếp.")
                    .khoang(10L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.nhat_ban_kinh", "tu_dong", "so", "96", true, "cau_hinh")
                    .nhan("Bán kính nhặt đồ (px)", "Chỉ đi nhặt đồ rơi của mình trong bán kính này quanh Pokémon (và trong vùng). 0 = không nhặt. 1 ô = 16 px.")
                    .khoang(0L, 480L)
                    .buoc(16L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.dung_khi_day_tui", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Dừng khi hành trang đầy", "1 = hành trang đầy thì dừng và báo.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.dung_khi_tu_di", "tu_dong", "co", "1", true, "cau_hinh")
                    .nhan("Dừng khi người chơi tự di chuyển", "1 = bấm phím di chuyển / chạm bản đồ thì dừng Tự động đánh. 0 = nhường 3 giây rồi chạy tiếp.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.tranh_pk_px", "tu_dong", "so", "160", true, "cau_hinh")
                    .nhan("Tránh người bật PK (px)", "Có người chơi khác bật PK trong bán kính này thì lùi về điểm bắt đầu chờ. 0 = tắt.")
                    .khoang(0L, 960L)
                    .buoc(16L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("tu_dong.map_cam", "tu_dong", "chuoi", "", true, "cau_hinh")
                    .nhan("Bản đồ cấm Tự động đánh thêm (id)", "Ngoài sàn đấu / Gym / chiến trường / mê cung / bản đồ boss. Cách nhau dấu phẩy; trống = không.")
                    .mau("^[0-9, ]*$")
                    .doDai(255),
            new DinhNghiaKhoa("tu_dong.hoi_sinh_tra_phi", "tu_dong", "co", "0", true, "cau_hinh")
                    .nhan("Cho tự dùng vật phẩm hồi sinh trả phí", "1 = khi người chơi đồng ý riêng (bảng Tự động: Tự hồi sinh \"cả vật phẩm lượng\") mới được tự dùng Thuốc hồi sinh tối đa (lượng khoá). 0 = chỉ Thuốc hồi sinh thường; không bao giờ tự trả xu / lượng."),
            new DinhNghiaKhoa("hoi_sinh.bat", "hoi_sinh", "co", "1", true, "cau_hinh")
                    .nhan("Bật cơ chế hồi sinh mới", "0 = luồng cũ (popup hồi sinh tại chỗ 5% giờ thu nhập, tự hồi sinh dưới cấp 50).")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("hoi_sinh.xu_phan_tram_gio", "tu_dong", "so", "40", true, "cau_hinh")
                    .nhan("Phí xu tại chỗ (% thu nhập 1 giờ)", "Theo cấp (ThongSoLauDai.xuMoiGio). 40 = 0,4 giờ cày. Nhân hệ số gục lặp.")
                    .khoang(0L, 1000L),
            new DinhNghiaKhoa("hoi_sinh.xu_toi_thieu", "tu_dong", "so", "200", true, "cau_hinh")
                    .nhan("Phí xu tại chỗ tối thiểu", "Xu.")
                    .khoang(0L, 100000000L),
            new DinhNghiaKhoa("hoi_sinh.he_so_lap", "hoi_sinh", "ds_so", "100,150,200,300", true, "cau_hinh")
                    .nhan("Hệ số gục lặp (%)", "Lần gục thứ 1, 2, 3... trong cửa sổ; lần sau dùng số cuối (trần). Áp cho phí xu và lượng khoá.")
                    .khoang(0L, 10000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.cua_so_phut", "hoi_sinh", "so", "30", true, "cau_hinh")
                    .nhan("Cửa sổ gục lặp (phút)", "Đếm số lần gục trong ngần này phút.")
                    .khoang(1L, 1440L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.luong_khoa", "tu_dong", "so", "2", true, "cau_hinh")
                    .nhan("Phí lượng khoá tại chỗ", "0 = tắt lựa chọn trả lượng khoá. Nhân hệ số gục lặp (làm tròn lên).")
                    .khoang(0L, 1000L),
            new DinhNghiaKhoa("hoi_sinh.luong_khoa_toi_da", "tu_dong", "so", "10", true, "cau_hinh")
                    .nhan("Trần phí lượng khoá", "Lượng khoá tối đa một lần.")
                    .khoang(0L, 1000L),
            new DinhNghiaKhoa("hoi_sinh.cho_sau_guc_giay", "hoi_sinh", "so", "5", true, "cau_hinh")
                    .nhan("Chờ sau khi gục (giây)", "Hồi sinh tại chỗ chỉ bấm được sau ngần này giây (đếm ngược trên hộp thoại).")
                    .khoang(0L, 600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.hoi_chieu_giay", "tu_dong", "so", "60", true, "cau_hinh")
                    .nhan("Hồi chiêu hồi sinh tại chỗ (giây)", "Giữa hai lần hồi sinh tại chỗ. Về Trung tâm luôn được.")
                    .khoang(0L, 3600L),
            new DinhNghiaKhoa("hoi_sinh.hp_thuoc", "hoi_sinh", "so", "30", true, "cau_hinh")
                    .nhan("HP khi dùng Thuốc hồi sinh (%)", "Thuốc hồi sinh tối đa lấy % trong data_vat_pham_lau_dai (hoi_sinh:100).")
                    .khoang(1L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.hp_tra_phi", "hoi_sinh", "so", "50", true, "cau_hinh")
                    .nhan("HP khi trả xu / lượng khoá / tân thủ (%)", "")
                    .khoang(1L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.exp_mat_phan_nghin", "hoi_sinh", "so", "20", true, "cau_hinh")
                    .nhan("Mất EXP khi về Trung tâm (phần nghìn)", "Phần nghìn EXP lên cấp hiện tại (20 = 2%). Không bao giờ tụt cấp: chỉ trừ trong tiến độ cấp.")
                    .khoang(0L, 200L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.exp_mat_toi_da_phut", "hoi_sinh", "so", "15", true, "cau_hinh")
                    .nhan("Trần mất EXP (phút cày)", "Không mất quá ngần này phút EXP đánh quái ở cấp đó. 0 = không chặn.")
                    .khoang(0L, 600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.do_ben_mat_phan_tram", "hoi_sinh", "so", "5", true, "cau_hinh")
                    .nhan("Mất độ bền khi về Trung tâm (%)", "% độ bền tối đa của mỗi trang bị đang mặc (không làm vỡ đồ).")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.met_moi_phut", "hoi_sinh", "so", "5", true, "cau_hinh")
                    .nhan("Mệt mỏi (phút)", "Sau khi về Trung tâm. 0 = tắt.")
                    .khoang(0L, 120L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.met_moi_exp", "hoi_sinh", "so", "20", true, "cau_hinh")
                    .nhan("Mệt mỏi: giảm EXP (%)", "EXP hạ quái.")
                    .khoang(0L, 90L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.met_moi_tan_cong", "hoi_sinh", "so", "10", true, "cau_hinh")
                    .nhan("Mệt mỏi: giảm tấn công (%)", "")
                    .khoang(0L, 90L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.tan_thu_cap", "tu_dong", "so", "15", true, "cau_hinh")
                    .nhan("Bảo vệ tân thủ dưới cấp", "Dưới cấp này: không mất EXP / độ bền / Mệt mỏi, hồi sinh tại chỗ miễn phí.")
                    .khoang(0L, 100L),
            new DinhNghiaKhoa("hoi_sinh.tan_thu_hoi_chieu_giay", "hoi_sinh", "so", "30", true, "cau_hinh")
                    .nhan("Hồi chiêu hồi sinh miễn phí tân thủ (giây)", "")
                    .khoang(0L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.dong_doi_bat", "hoi_sinh", "co", "1", true, "cau_hinh")
                    .nhan("Cho chờ đồng đội hồi sinh", "Đồng đội ở gần mang Thuốc hồi sinh được mời cứu.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("hoi_sinh.dong_doi_giay", "hoi_sinh", "so", "60", true, "cau_hinh")
                    .nhan("Thời gian chờ đồng đội (giây)", "")
                    .khoang(10L, 600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.cam_pvp", "hoi_sinh", "co", "1", true, "cau_hinh")
                    .nhan("Cấm tại chỗ ở PvP / sàn đấu", "Sàn đấu, giải Liên minh, chiến trường, lãnh địa đội, chiếm Gym đang đánh, đấu trường + hoi_sinh.map_pvp.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("hoi_sinh.cam_gym", "hoi_sinh", "co", "1", true, "cau_hinh")
                    .nhan("Cấm tại chỗ khi thách đấu Gym", "")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("hoi_sinh.cam_boss", "hoi_sinh", "co", "1", true, "cau_hinh")
                    .nhan("Cấm tại chỗ giữa trận boss", "Đã gây sát thương vào boss thế giới / Gym còn sống cùng bản đồ.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("hoi_sinh.cam_me_cung", "hoi_sinh", "co", "1", true, "cau_hinh")
                    .nhan("Cấm tại chỗ trong Mê cung", "")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("hoi_sinh.map_pvp", "hoi_sinh", "ds_so", "202,203,204,205,227,228", true, "cau_hinh")
                    .nhan("Bản đồ PvP (id)", "Thêm vào luật cấm PvP (so cả id bản đồ lẫn id nạp).")
                    .khoang(0L, 100000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.map_cam_them", "hoi_sinh", "chuoi", "", true, "cau_hinh")
                    .nhan("Bản đồ cấm tại chỗ thêm (id)", "Cách nhau dấu phẩy; trống = không.")
                    .mau("^[0-9, ]*$")
                    .doDai(255)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.auto_ve_map_up", "hoi_sinh", "co", "0", true, "cau_hinh")
                    .nhan("Cho \"Auto về Map Up\" sau khi gục", "0 = gục thì không tự bay lại bãi luyện (chống treo máy vô hạn).")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("hoi_sinh.gia_thuoc_phan_tram_gio", "tu_dong", "so", "30", true, "cau_hinh")
                    .nhan("Giá Thuốc hồi sinh (% thu nhập 1 giờ)", "Giá ở cửa hàng NPC theo cấp người mua (cùng bậc phí xu tại chỗ). 0 = giá gốc data_potion.")
                    .khoang(0L, 1000L),
            new DinhNghiaKhoa("hoi_sinh.gia_thuoc_toi_thieu", "tu_dong", "so", "407", true, "cau_hinh")
                    .nhan("Giá Thuốc hồi sinh tối thiểu", "Xu / viên (không thấp hơn giá gốc).")
                    .khoang(0L, 100000000L),
            new DinhNghiaKhoa("hoi_sinh.thuoc_moi_gio", "hoi_sinh", "so", "6", true, "cau_hinh")
                    .nhan("Hồi sinh bằng vật phẩm tối đa / giờ", "Thuốc hồi sinh / tối đa của chính mình (tay, Tự động đánh, dùng từ túi) trong 60 phút. Đồng đội cứu không tính. 0 = không giới hạn.")
                    .khoang(0L, 100L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.thongBao.bat", "thong_bao", "co", "1", true, "cau_hinh")
                    .nhan("Gộp thông báo toàn máy chủ", "0 = gửi thẳng như cũ.")
                    .lop("luon_bat"),
            new DinhNghiaKhoa("sv.thongBao.toiDa", "thong_bao", "so", "6", true, "cau_hinh")
                    .nhan("Thông báo tối đa / cửa sổ", "")
                    .khoang(1L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.thongBao.cuaSoGiay", "thong_bao", "so", "60", true, "cau_hinh")
                    .nhan("Cửa sổ đếm thông báo (giây)", "")
                    .khoang(1L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.thongBao.moiLoai", "thong_bao", "so", "3", true, "cau_hinh")
                    .nhan("Tối đa mỗi loại / cửa sổ", "")
                    .khoang(1L, 1000L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.thongBao.trungLapGiay", "thong_bao", "so", "120", true, "cau_hinh")
                    .nhan("Bỏ thông báo trùng (giây)", "")
                    .khoang(0L, 86400L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.thongBao.gopGiay", "thong_bao", "so", "15", true, "cau_hinh")
                    .nhan("Gộp mỗi (giây)", "")
                    .khoang(1L, 3600L)
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.website", "bao_tri", "chuoi", "https://huyenthoaibaoboi.fun/", true, "cau_hinh")
                    .nhan("Địa chỉ website (hiện trong game)", "")
                    .doDai(200),
            new DinhNghiaKhoa("sv.linkTaiGame", "bao_tri", "chuoi", "https://huyenthoaibaoboi.fun/", true, "cau_hinh")
                    .nhan("Link tải game (hiện trong game)", "")
                    .doDai(200),
            new DinhNghiaKhoa("sv.adminAccounts", "bao_tri", "ds_chuoi", "", true, "cau_hinh")
                    .nhan("Danh sách GM trong game", "Tên Pokémon GM, cách nhau dấu phẩy.")
                    .mau("^[A-Za-z0-9_]{1,30}$")
                    .doDai(255),
            new DinhNghiaKhoa("sv.gmCanCoPInfo", "quan_tri", "co", "0", true, "cau_hinh")
                    .nhan("GM cần cả cờ pInfo", "1 = quyền GM cần tên trong danh sách VÀ pInfo[16]=1.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.banToiThieu", "bao_tri", "so", "0", true, "cau_hinh")
                    .nhan("Bản client tối thiểu", "> 0 = từ chối client jar/Android cũ hơn.")
                    .khoang(0L, 1000000L),
            new DinhNghiaKhoa("sv.choPhepTaoTenGM", "quan_tri", "co", "0", true, "cau_hinh")
                    .nhan("Cho tạo Pokémon tên GM", "Chỉ bật tạm thời.")
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.testMode", "quan_tri", "co", "0", true, "cau_hinh")
                    .nhan("Chế độ thử (testMode)", "Mã test cấp tài nguyên miễn phí. Khoá ở máy thật.")
                    .khoaProd()
                    .lop("co_dinh"),
            new DinhNghiaKhoa("sv.testCode", "quan_tri", "chuoi", "", true, "cau_hinh")
                    .nhan("Mã test", "Chuỗi gõ trong game khi testMode=1.")
                    .mau("^[ -~]*$")
                    .doDai(64)
                    .nhay()
                    .khoaProd()
                    .lop("co_dinh"),
        };
    }
}
