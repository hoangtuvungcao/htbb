package real;

import data.Database;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chuyen luong giua nhan vat qua NPC Tong Quan (them 19/09/2026, theo yeu cau nguoi
 * dung). Nguoi nhan PHAI dang ONLINE (giong giao dich/trade) - khong ho tro gui cho
 * nguoi dang offline, tranh phai dung them mot duong ghi truc tiep xuong DB rieng chi
 * cho truong hop nay.
 *
 * HAN MUC: moi nhan vat NHAN toi da Char.MAX_LUONG_NHAN_CHUYEN_NGAY/ngay, dem theo
 * NGUOI NHAN (khong phai theo tung cap gui-nhan) - du nhieu nguoi khac nhau cung gui
 * toi trong ngay, tong nhan van khong vuot han muc. Xem Char.conLaiLuongNhanChuyenNgay/
 * addLuongNhanChuyen.
 *
 * Phia nguoi GUI dung Char.truLuongChuyenKhoan() (KHONG dung subLuong()) de khong bi
 * tinh nham vao cac su kien "diem tieu xai" - day la CHUYEN chu khong phai TIEU.
 */
public final class CharChuyenLuong {

    public static final int INPUT_TEN_NGUOI_NHAN = -32054;
    public static final int INPUT_SO_LUONG = -32055;

    private CharChuyenLuong() {
    }

    private static final class ChoNhapSoLuong {
        String tenNguoiNhan;
    }

    /** Khoa theo charDBID: nguoi choi thoat ra vao lai la doi tuong Char khac, nhung van
     * la NHAN VAT do nen van tiep tuc dung phien dang cho nhap so luong. */
    private static final ConcurrentHashMap<Integer, ChoNhapSoLuong> DANG_CHO =
            new ConcurrentHashMap<Integer, ChoNhapSoLuong>();

    public static void moNhapTen(final Char player) {
        if (chanViNapTien(player)) {
            return;
        }
        player.sendMessage(MessageCreator.createMsgInputText(
                INPUT_TEN_NGUOI_NHAN, 0, "Nhập tên nhân vật muốn chuyển lượng", 0));
    }

    /**
     * nap tien: khi bat, luong (tien cao cap mua bang tien that) KHONG chuyen truc tiep giua
     * nguoi choi - mua ban hang cao cap chi qua Cho trang tri co thue (docs/pokemon/NAP_TIEN.md).
     */
    static boolean chanViNapTien(final Char player) {
        if (!com.htbb.pokemon.NapTien.bat()) {
            return false;
        }
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Chuyển lượng trực tiếp đã tắt. Mua bán đồ trang trí qua Chợ trang trí (NPC Tổng Quản > Cửa hàng & Nạp).", ""));
        return true;
    }

    public static void doNhapTen(final Char player, final String tenGoc) {
        if (chanViNapTien(player)) {
            return;
        }
        final String ten = tenGoc == null ? "" : tenGoc.trim();
        if (ten.isEmpty()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Tên không hợp lệ.", ""));
            return;
        }
        if (ten.equalsIgnoreCase(player.charname)) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Không thể chuyển lượng cho chính mình.", ""));
            return;
        }
        final Char nguoiNhan = CharManager.instance.getCharByCharName(ten);
        if (nguoiNhan == null) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Không tìm thấy nhân vật \"" + ten + "\" đang online.", ""));
            return;
        }
        final ChoNhapSoLuong c = new ChoNhapSoLuong();
        c.tenNguoiNhan = nguoiNhan.charname;
        DANG_CHO.put(Integer.valueOf(player.charDBID), c);
        player.sendMessage(MessageCreator.createMsgInputText(
                INPUT_SO_LUONG, 0, "Nhập số lượng muốn chuyển cho " + nguoiNhan.charname, 1));
    }

    public static void doNhapSoLuong(final Char player, final String soGoc) {
        if (chanViNapTien(player)) {
            return;
        }
        final ChoNhapSoLuong c = DANG_CHO.remove(Integer.valueOf(player.charDBID));
        if (c == null) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    "Phiên chuyển lượng đã hết hạn, hãy mở lại menu \"Chuyển lượng\".", ""));
            return;
        }
        final long soLuong;
        try {
            soLuong = Long.parseLong(soGoc == null ? "" : soGoc.trim());
        } catch (final Exception e) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng không hợp lệ.", ""));
            return;
        }
        if (soLuong <= 0L) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng phải lớn hơn 0.", ""));
            return;
        }
        if (soLuong > (long) Integer.MAX_VALUE) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Số lượng quá lớn.", ""));
            return;
        }
        final Char nguoiNhan = CharManager.instance.getCharByCharName(c.tenNguoiNhan);
        if (nguoiNhan == null) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    c.tenNguoiNhan + " đã thoát game, hãy thử lại.", ""));
            return;
        }
        final int conLai = nguoiNhan.conLaiLuongNhanChuyenNgay();
        if (conLai <= 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    nguoiNhan.charname + " hôm nay đã nhận đủ " + Char.MAX_LUONG_NHAN_CHUYEN_NGAY
                            + " lượng, không thể nhận thêm.", ""));
            return;
        }
        if (soLuong > (long) conLai) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    nguoiNhan.charname + " hôm nay chỉ có thể nhận thêm tối đa " + conLai + " lượng nữa.", ""));
            return;
        }
        // GD2 2.7: phi chuyen luong 5% (dot, nguoi gui tra them) khi bat co sv.lau_dai.
        final long phi = com.htbb.pokemon.LauDai.bat() ? com.htbb.pokemon.KinhTeLauDai.phiChuyenLuong(soLuong) : 0L;
        if (!player.truLuongChuyenKhoan(soLuong + phi)) {
            player.sendMessage(MessageCreator.createServerAlertMessage(
                    phi > 0L ? "Không đủ lượng (cần thêm " + phi + " lượng phí chuyển 5%)." : "Không đủ lượng.", ""));
            return;
        }
        final int thucNhan = nguoiNhan.addLuongNhanChuyen((int) soLuong);
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(
                "Đã chuyển " + thucNhan + " lượng cho " + nguoiNhan.charname, ""));
        nguoiNhan.sendMessage(MessageCreator.createServerAlertMessage(
                "Bạn nhận được " + thucNhan + " lượng từ " + player.charname, ""));
        Database.instance.saveOrtherLog("", player.charname,
                player.charname + " chuyen " + thucNhan + " luong cho " + nguoiNhan.charname, "chuyenluong");
    }
}
