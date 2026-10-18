package com.htbb.pokemon;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * "Thành viên Huyền Thoại Bảo Bối" — luật THUẦN (docs/pokemon/THANH_VIEN.md). Không CSDL, không Char:
 * test/KiemThanhVien.java gọi thẳng. Phần nối game ở {@link ThanhVien}, web ở HTBB-Web
 * server/utils/thanhVienThuan.ts (bản sao các hằng số — test đối chiếu).
 *
 * Thành viên theo TÀI KHOẢN (mọi nhân vật của tài khoản). KHÔNG cho thêm sức mạnh / tiền tệ gì:
 * chỉ mở các tính năng mà admin chọn khoá (chống tài khoản phụ / clone). Tắt (mặc định) = không khoá gì.
 */
public final class ThanhVienLogic {

    private ThanhVienLogic() {
    }

    // ------------------------------------------------------------------ khoá cau_hinh (đọc lại mỗi 10 giây)

    public static final String K_BAT = "thanh_vien.bat";
    public static final String K_BAN_BAT = "thanh_vien.ban_bat";
    public static final String K_GIA = "thanh_vien.gia";
    public static final String K_THOI_HAN = "thanh_vien.thoi_han_ngay";
    public static final String K_CHUC_NANG = "thanh_vien.chuc_nang";
    public static final String K_MIEN_CAP = "thanh_vien.mien_cap";
    public static final String K_THONG_BAO = "thanh_vien.thong_bao";

    public static final int GIA_MAC_DINH = 20000;

    // ---- Kích hoạt tài khoản (5h_active): CHƯA kích hoạt vẫn đăng nhập + chơi, chỉ bị khoá vài tính năng ----
    /** 1 = chưa kích hoạt KHÔNG vào được game (hành vi cũ). Mặc định 0. Chỉ có tác dụng khi sv.EnableAvtiveCheck=1. */
    public static final String K_KH_CHAN_DANG_NHAP = "kich_hoat.chan_dang_nhap";
    /** Tính năng bị khoá khi tài khoản CHƯA kích hoạt (cùng mã với Thành viên). */
    public static final String K_KH_CHUC_NANG = "kich_hoat.chuc_nang";
    public static final String K_KH_THONG_BAO = "kich_hoat.thong_bao";
    /** 1 = Thành viên HTBB được coi như đã kích hoạt (mặc định 1: tài khoản đã trả tiền không phải clone bỏ đi). */
    public static final String K_KH_TV_LA_KH = "kich_hoat.thanh_vien_la_kich_hoat";
    public static final String KH_CHUC_NANG_MAC_DINH = "giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang,giftcode";

    /** Sổ đăng ký tính năng có thể khoá: {mã, tên hiển thị, chỗ chặn}. */
    public static final String[][] CHUC_NANG = {
        {"giao_dich", "Giao dịch trực tiếp", "mời / nhận lời giao dịch (gói 66)"},
        {"nhan_tang", "Nhận giao dịch / quà từ người khác", "là người được mời giao dịch, người nhận hoa/quà"},
        {"chat_the_gioi", "Chat thế giới (loa)", "dùng loa phát toàn máy chủ"},
        {"cho", "Chợ (ký gửi + chợ trang trí)", "rao bán / mua ở chợ người chơi"},
        {"cho_sinh_thai", "Chợ sinh thái", "rao / mua / đặt đơn ở chợ sinh thái"},
        {"chuyen_luong", "Chuyển lượng", "chuyển lượng cho người khác"},
        {"bang", "Đội", "tạo đội, mời / nhận lời vào đội"},
        {"pvp_xep_hang", "Giải Liên minh xếp hạng", "đăng ký giải Liên minh"},
        {"giftcode", "Giftcode", "nhập giftcode"},
        {"qua_online", "Quà online", "nhận quà thời gian online"},
        {"gui_thu", "Nhắn tin riêng / tặng quà", "gửi tin nhắn riêng, tặng hoa / quà"},
    };

    /** Mặc định khi mở game chính thức (admin sửa được). */
    public static final String CHUC_NANG_MAC_DINH = "giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode";

    public static boolean laMaHopLe(final String ma) {
        for (final String[] c : CHUC_NANG) {
            if (c[0].equals(ma)) {
                return true;
            }
        }
        return false;
    }

    public static String tenChucNang(final String ma) {
        for (final String[] c : CHUC_NANG) {
            if (c[0].equals(ma)) {
                return c[1];
            }
        }
        return ma;
    }

    // ------------------------------------------------------------------ cấu hình

    /** Ảnh chụp cấu hình (bất biến). */
    public static final class CauHinh {
        public final boolean bat, banBat;
        public final int gia, thoiHanNgay, mienCap;
        public final Set<String> chucNang;
        public final String thongBao;
        /** Kích hoạt: chặn đăng nhập, tính năng khoá khi chưa kích hoạt, lời nhắn, thành viên = đã kích hoạt. */
        public final boolean khChanDangNhap, khTvLaKh;
        public final Set<String> khChucNang;
        public final String khThongBao;

        public CauHinh(final boolean bat, final boolean banBat, final int gia, final int thoiHanNgay, final Set<String> chucNang,
                final int mienCap, final String thongBao) {
            this(bat, banBat, gia, thoiHanNgay, chucNang, mienCap, thongBao, false, new LinkedHashSet<String>(), "", true);
        }

        public CauHinh(final boolean bat, final boolean banBat, final int gia, final int thoiHanNgay, final Set<String> chucNang,
                final int mienCap, final String thongBao, final boolean khChanDangNhap, final Set<String> khChucNang,
                final String khThongBao, final boolean khTvLaKh) {
            this.khChanDangNhap = khChanDangNhap;
            this.khChucNang = Collections.unmodifiableSet(khChucNang);
            this.khThongBao = khThongBao == null ? "" : khThongBao;
            this.khTvLaKh = khTvLaKh;
            this.bat = bat;
            this.banBat = banBat;
            this.gia = gia;
            this.thoiHanNgay = thoiHanNgay;
            this.chucNang = Collections.unmodifiableSet(chucNang);
            this.mienCap = mienCap;
            this.thongBao = thongBao == null ? "" : thongBao;
        }
    }

    /** Mặc định = TẮT (không khoá gì) — thiếu dòng cau_hinh là hành vi cũ. */
    public static final CauHinh TAT = docCauHinh(new Properties());

    static int soDuong(final String s, final int macDinh, final int toiDa) {
        try {
            final int v = Integer.parseInt(s.trim());
            return v < 0 ? macDinh : Math.min(v, toiDa);
        } catch (final Exception e) {
            return macDinh;
        }
    }

    /** Đọc từ bảng cau_hinh (CauHinh.napLai đưa nguyên Properties). Mã lạ trong danh sách bị bỏ qua. */
    public static CauHinh docCauHinh(final Properties p) {
        final boolean bat = "1".equals(chu(p.getProperty(K_BAT)));
        final boolean banBat = "1".equals(chu(p.getProperty(K_BAN_BAT)));
        final int gia = soDuong(chu(p.getProperty(K_GIA)), GIA_MAC_DINH, 10000000);
        final int han = soDuong(chu(p.getProperty(K_THOI_HAN)), 0, 36500);
        final int mien = soDuong(chu(p.getProperty(K_MIEN_CAP)), 0, 200);
        final Set<String> cn = docDanhSach(p.getProperty(K_CHUC_NANG), CHUC_NANG_MAC_DINH);
        final Set<String> kh = docDanhSach(p.getProperty(K_KH_CHUC_NANG), KH_CHUC_NANG_MAC_DINH);
        return new CauHinh(bat, banBat, gia, han, cn, mien, chu(p.getProperty(K_THONG_BAO)),
                "1".equals(chu(p.getProperty(K_KH_CHAN_DANG_NHAP))), kh, chu(p.getProperty(K_KH_THONG_BAO)),
                !"0".equals(chu(p.getProperty(K_KH_TV_LA_KH))));
    }

    static Set<String> docDanhSach(final String ds, final String macDinh) {
        final Set<String> cn = new LinkedHashSet<String>();
        for (final String m : (ds == null ? macDinh : ds).split("[,;\\s]+")) {
            final String x = m.trim().toLowerCase(Locale.ROOT);
            if (laMaHopLe(x)) {
                cn.add(x);
            }
        }
        return cn;
    }

    static String chu(final String s) {
        return s == null ? "" : s.trim();
    }

    // ------------------------------------------------------------------ quyết định

    /**
     * Có được dùng tính năng {@code ma} không. Tắt hệ thống / tính năng không bị khoá / cấp dưới
     * mien_cap (người mới chơi thử) / là thành viên còn hạn -> được.
     */
    public static boolean coQuyen(final CauHinh c, final String ma, final boolean laThanhVien, final int cap) {
        if (c == null || !c.bat || !c.chucNang.contains(ma)) {
            return true;
        }
        if (c.mienCap > 0 && cap > 0 && cap < c.mienCap) {
            return true;
        }
        return laThanhVien;
    }

    /** Lời nhắn khi bị chặn ({ten}, {gia}, {web} thay được trong thong_bao). */
    public static String thongBao(final CauHinh c, final String ma, final String website) {
        final String web = (website == null || website.trim().isEmpty() ? "trang web" : website.trim().replaceAll("/+$", ""))
                + "/thanh-vien";
        String m = c == null || c.thongBao.isEmpty()
                ? "Tính năng {ten} dành cho Thành viên HTBB. Mở tại {web} ({gia})."
                : c.thongBao;
        m = m.replace("{ten}", tenChucNang(ma)).replace("{web}", web)
                .replace("{gia}", String.format(Locale.ROOT, "%,d đ", c == null ? GIA_MAC_DINH : c.gia).replace(',', '.'));
        return m;
    }

    /** Kết quả của MỘT cổng: được / cần kích hoạt / cần thành viên. */
    public static final int DUOC = 0, CAN_KICH_HOAT = 1, CAN_THANH_VIEN = 2;

    /**
     * Cổng DUY NHẤT: tính năng {@code ma} bị chặn nếu (cần kích hoạt và CHƯA kích hoạt) hoặc (cần thành viên và
     * KHÔNG là thành viên). Thành viên được coi như đã kích hoạt khi khTvLaKh. Miễn cấp chỉ áp cho thành viên.
     */
    public static int quyetDinh(final CauHinh c, final String ma, final boolean daKichHoat, final boolean laThanhVien, final int cap) {
        if (c == null) {
            return DUOC;
        }
        if (c.khChucNang.contains(ma) && !daKichHoat && !(c.khTvLaKh && laThanhVien)) {
            return CAN_KICH_HOAT;
        }
        return coQuyen(c, ma, laThanhVien, cap) ? DUOC : CAN_THANH_VIEN;
    }

    /** Lời nhắn khi tài khoản CHƯA kích hoạt ({ten}, {web} thay được trong kich_hoat.thong_bao). */
    public static String thongBaoKichHoat(final CauHinh c, final String ma, final String website) {
        final String web = (website == null || website.trim().isEmpty() ? "trang web" : website.trim().replaceAll("/+$", ""))
                + "/kichhoat";
        final String m = c == null || c.khThongBao.isEmpty()
                ? "Tính năng {ten} cần tài khoản đã kích hoạt. Xem cách kích hoạt tại {web}."
                : c.khThongBao;
        return m.replace("{ten}", tenChucNang(ma)).replace("{web}", web);
    }

    /** Chặn giữa hai người (giao dịch / tặng): người kia chưa là thành viên. */
    public static String thongBaoNguoiKia(final String ma) {
        return "Người chơi kia chưa là Thành viên HTBB nên không thể " + tenChucNang(ma).toLowerCase(Locale.ROOT) + ".";
    }

    public static String thongBaoNguoiKiaKichHoat(final String ma) {
        return "Tài khoản của người chơi kia chưa kích hoạt nên không thể " + tenChucNang(ma).toLowerCase(Locale.ROOT) + ".";
    }

    // ------------------------------------------------------------------ hạn

    public static final long NGAY_MS = 86400000L;

    /** het_han_ms: 0 = vĩnh viễn, &lt; 0 = đã thu hồi, &gt; 0 = còn hạn tới lúc đó. */
    public static boolean conHan(final long hetHanMs, final long bayGioMs, final boolean coDong) {
        return coDong && (hetHanMs == 0L || hetHanMs > bayGioMs);
    }

    /**
     * Hạn mới khi cấp thêm {@code ngay} ngày (0 = vĩnh viễn). Đang vĩnh viễn giữ vĩnh viễn; còn hạn thì
     * cộng nối tiếp; hết / thu hồi thì tính từ bây giờ.
     */
    public static long hanMoi(final boolean coDong, final long hetHanCu, final long bayGioMs, final int ngay) {
        if (coDong && hetHanCu == 0L) {
            return 0L;
        }
        if (ngay <= 0) {
            return 0L;
        }
        final long goc = coDong && hetHanCu > bayGioMs ? hetHanCu : bayGioMs;
        return goc + ngay * NGAY_MS;
    }

    /** Thu hồi một lần cấp (hoàn tiền): trừ đúng số ngày đã cấp; cấp vĩnh viễn -> thu hồi hẳn (-1). */
    public static long hanSauThuHoi(final long hetHanCu, final long bayGioMs, final int ngayDaCap) {
        if (hetHanCu == 0L) {
            // Đang vĩnh viễn: lần cấp bị hoàn là vĩnh viễn -> thu hồi; là có hạn -> vẫn còn lần cấp vĩnh viễn khác.
            return ngayDaCap <= 0 ? -1L : 0L;
        }
        if (ngayDaCap <= 0) {
            return -1L;
        }
        if (hetHanCu < 0L) {
            return hetHanCu;
        }
        final long moi = hetHanCu - ngayDaCap * NGAY_MS;
        return moi <= bayGioMs ? -1L : moi;
    }
}
