package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cua cho BANG DIEU KHIEN MAY CHU (server.AdminPanel) vao he sinh thai - goi lai DUNG cac ham lenh
 * GM "hst ..." (HeSinhThaiGame.lenhAdmin) ma KHONG can mot nhan vat GM dang online, va doc trang thai
 * chi-doc (khong nap CSDL). Nam cung goi de dung cac thanh vien package-private, khong sua HeSinhThaiGame.
 */
public final class QuanTriSinhThai {

    private QuanTriSinhThai() {
    }

    /**
     * @param loai dovui | dua | bungphat | giaidau | thoitiet | xoathoitiet | napthegioi
     * @return mo ta ket qua cho admin
     */
    public static String lenh(final String loai, final int vung, final int thoiTiet) {
        if (!HeSinhThaiGame.bat()) {
            return "Hệ sinh thái đang tắt - không có sự kiện thế giới.";
        }
        HeSinhThaiGame.damBaoKhoiDong();
        switch (loai) {
            case "dovui":
                HeSinhThaiGame.DoVui.batDau();
                return HeSinhThaiGame.DoVui.dapAn != null ? "Đã mở câu đố \"Ai là Pokémon?\" (" + HeSinhThai.GIAY_TRA_LOI + " giây)."
                        : "Không chọn được câu đố (chưa nạp loài Pokémon).";
            case "dua":
                if (HeSinhThaiGame.Dua.dangChay) {
                    return "Đua Pokémon đang chạy.";
                }
                HeSinhThaiGame.Dua.moDangKy(true);
                return HeSinhThaiGame.Dua.dangKy ? "Đã mở đăng ký Đua Pokémon - xuất phát sau 1 phút."
                        : "Không mở được đua (thiếu bản đồ đua).";
            case "bungphat": {
                HeSinhThaiGame.batDauBungPhat(-1);
                final int[] bp = HeSinhThaiGame.bungPhat;
                return bp == null ? "Không có loài đủ điều kiện bùng phát." : "Bùng phát " + SuuTamSinhThai.tenLoai(
                        HeSinhThaiGame.dexTemplate(bp[1])) + " ở " + real.Map.getNameMap(bp[0]) + ".";
            }
            case "giaidau":
                HeSinhThaiGame.GiaiDau.congBo();
                return "Đã công bố giải đấu tuần (" + HeSinhThaiGame.GiaiDau.DANG_KY.size() + " người đăng ký).";
            case "thoitiet":
                if (vung < 0 || vung >= SinhThaiTheGioi.MA_VUNG.length || thoiTiet < 0 || thoiTiet >= SinhThaiTheGioi.TEN_THOI_TIET.length) {
                    return "Vùng / thời tiết không hợp lệ.";
                }
                HeSinhThaiGame.ngayGhiDe = HeSinhThaiGame.homNay();
                HeSinhThaiGame.THOI_TIET_GHI_DE.put(vung, thoiTiet);
                HeSinhThaiGame.guiThoiTietMoiNguoi(true);
                return "Thời tiết " + SinhThaiTheGioi.tenVung(vung) + " = " + SinhThaiTheGioi.tenThoiTiet(thoiTiet) + " (hết hôm nay).";
            case "xoathoitiet":
                HeSinhThaiGame.THOI_TIET_GHI_DE.clear();
                HeSinhThaiGame.guiThoiTietMoiNguoi(true);
                return "Đã bỏ mọi thời tiết đặt tay - trở về lịch tự nhiên.";
            case "napthegioi":
                HeSinhThaiGame.napTheGioi();
                return "Đã nạp lại dữ liệu thế giới.";
            default:
                return "Lệnh không rõ: " + loai;
        }
    }

    /** Trang thai hien tai (moi dong mot muc) - chi doc RAM. */
    public static List<String> trangThai() {
        final List<String> l = new ArrayList<String>();
        if (!HeSinhThaiGame.bat()) {
            l.add("Hệ sinh thái: tắt");
            return l;
        }
        final StringBuilder tt = new StringBuilder();
        for (int v = 0; v < SinhThaiTheGioi.MA_VUNG.length; v++) {
            if (tt.length() > 0) {
                tt.append(" · ");
            }
            tt.append(SinhThaiTheGioi.tenVung(v)).append(": ").append(SinhThaiTheGioi.tenThoiTiet(HeSinhThaiGame.thoiTietVung(v)));
        }
        l.add("Thời tiết — " + tt);
        final int[] bp = HeSinhThaiGame.bungPhat;
        l.add(bp == null ? "Bùng phát: không có" : "Bùng phát: " + SuuTamSinhThai.tenLoai(HeSinhThaiGame.dexTemplate(bp[1]))
                + " ở " + real.Map.getNameMap(bp[0]));
        l.add(HeSinhThaiGame.DoVui.dapAn != null ? "Đố vui: đang mở" : "Đố vui: lịch " + HeSinhThai.GIO_DO_VUI[0] + ":"
                + HeSinhThai.PHUT_DO_VUI + " và " + HeSinhThai.GIO_DO_VUI[1] + ":" + HeSinhThai.PHUT_DO_VUI);
        l.add(HeSinhThaiGame.Dua.dangChay ? "Đua Pokémon: đang chạy (" + HeSinhThaiGame.Dua.NGUOI.size() + " người)"
                : HeSinhThaiGame.Dua.dangKy ? "Đua Pokémon: đang mở đăng ký (" + HeSinhThaiGame.Dua.NGUOI.size() + ")"
                : "Đua Pokémon: lịch " + HeSinhThai.GIO_DUA + ":00 hằng ngày");
        l.add("Giải đấu tuần: " + HeSinhThaiGame.GiaiDau.DANG_KY.size() + " đăng ký, công bố thứ Bảy 20:00");
        return l;
    }

    /** Ho so sinh thai cua nhan vat (chi khi da nap trong RAM), null neu chua co. */
    public static String hoSo(final int charDBID) {
        final HoSoSinhThai h = HeSinhThaiGame.HO_SO.get(Integer.valueOf(charDBID));
        if (h == null) {
            return null;
        }
        synchronized (h) {
            int vung = 0;
            for (int i = 0; i < 31; i++) {
                if ((h.vungXong & (1 << i)) != 0) {
                    vung++;
                }
            }
            int vatLieu = 0;
            for (final Map.Entry<Integer, Integer> e : h.tui.entrySet()) {
                vatLieu += e.getValue();
            }
            return "Thể lực " + h.theLuc + " · " + vatLieu + " vật liệu · " + h.trung.size() + " trứng · album "
                    + h.album.size() + " · vùng xong " + vung + " · đố vui đúng " + h.doVuiDung + " · thắng đua " + h.duaThang
                    + (h.danhHieu.isEmpty() ? "" : " · danh hiệu: " + String.join(", ", h.danhHieu));
        }
    }

    public static String[] tenVung() {
        return SinhThaiTheGioi.TEN_VUNG.clone();
    }

    public static String[] tenThoiTiet() {
        return SinhThaiTheGioi.TEN_THOI_TIET.clone();
    }
}
