package com.htbb.pokemon;

import java.util.Random;

/**
 * Li xi Tet (chu game duyet 19/10/2026: "sua 3 nguon in tien con lai"). Lop THUAN, test/KiemLiXi.java; noi vao game:
 * real.Char.doOpenLixi2021 / real.Map.doReceiveLixi (tui li xi, su kien TET_2017) va real.MapNhapText -32051 (ma li
 * xi, su kien TET_123).
 *
 * Truoc: moi tui 10.000 - 20.000 xu + 5 giai 1.000.000 / 1.000.000 / 1.000.000 / 2.000.000 / 5.000.000 xu (tam 30 - 550 gio
 * thu nhap cap 20), mot nguoi nhat duoc ca 100 tui / ngay; ma li xi 10 - 100 LUONG (mo, giao dich duoc) moi Pokemon -
 * nick phu nhap ma roi chuyen luong ve nick chinh. Sau (thiet_ke_can_bang.md 5.4 / 5.9: su kien <= 25% ngan sach bang
 * xu, luong khoa la nguon co tran):
 * <ul>
 *   <li>Tui: xu = 5 - 10% thu nhap 1 gio O CAP NGUOI MO; giai lon = 1 gio thu nhap (thay 1 - 5 trieu), van 5 giai /
 *       dot toan may chu. Moi Pokemon nhan toi da N tui / ngay, mo toi da M tui / ngay va K tui ca dot. Phan xu chi
 *       tra khi qua cong chong clone (cap / tuoi tai khoan / gio choi + toi da N Pokemon / thiet bi / IP / ngay) -
 *       khong qua thi van nhan vat pham, khong nhan xu.</li>
 *   <li>Ma: LUONG KHOA (khong giao dich), 5 - 15; moi TAI KHOAN 1 ma / dot, moi thiet bi / IP 1 tai khoan / dot, tran
 *       tong luong khoa ca may chu / dot; can qua cong chong clone.</li>
 * </ul>
 */
public final class LiXiLogic {

    private LiXiLogic() {
    }

    public static final class CauHinh {
        public final int tuiNhanNgay;
        public final int tuiMoNgay;
        public final int tuiMoDot;
        public final int xuPhanTramGioMin;
        public final int xuPhanTramGioMax;
        public final int giaiLonPhanTramGio;
        public final int maLuongKhoaMin;
        public final int maLuongKhoaMax;
        public final int maTaiKhoanMoiMay;
        public final long maTongLuongKhoaDot;

        public CauHinh(final int tuiNhanNgay, final int tuiMoNgay, final int tuiMoDot, final int xuPhanTramGioMin,
                final int xuPhanTramGioMax, final int giaiLonPhanTramGio, final int maLuongKhoaMin, final int maLuongKhoaMax,
                final int maTaiKhoanMoiMay, final long maTongLuongKhoaDot) {
            this.tuiNhanNgay = Math.max(1, tuiNhanNgay);
            this.tuiMoNgay = Math.max(1, tuiMoNgay);
            this.tuiMoDot = Math.max(this.tuiMoNgay, tuiMoDot);
            this.xuPhanTramGioMin = Math.max(0, Math.min(100, xuPhanTramGioMin));
            this.xuPhanTramGioMax = Math.max(this.xuPhanTramGioMin, Math.min(100, xuPhanTramGioMax));
            // giai lon toi da 3 gio thu nhap: chan cau hinh nham quay lai "trieu xu".
            this.giaiLonPhanTramGio = Math.max(0, Math.min(300, giaiLonPhanTramGio));
            this.maLuongKhoaMin = Math.max(1, maLuongKhoaMin);
            this.maLuongKhoaMax = Math.max(this.maLuongKhoaMin, Math.min(50, maLuongKhoaMax));
            this.maTaiKhoanMoiMay = Math.max(1, maTaiKhoanMoiMay);
            this.maTongLuongKhoaDot = Math.max(0L, maTongLuongKhoaDot);
        }

        /** Mac dinh san xuat (khop tools/cauhinh/danh_muc.json, khoa li_xi.*). */
        public static CauHinh macDinh() {
            return new CauHinh(2, 3, 15, 5, 10, 100, 5, 15, 1, 20000L);
        }
    }

    // ------------------------------------------------------------------ o dem

    public static String maNhanTui(final String dot) {
        return "lixi_tui_nhan_" + dot;
    }

    public static String maMoTui(final String dot) {
        return "lixi_tui_mo_" + dot;
    }

    public static String maMoTuiMay(final String dot) {
        return "lixi_tui_may_" + dot;
    }

    public static String maGiaiLon(final String dot) {
        return "lixi_tui_giai_" + dot;
    }

    public static String maNhapMa(final String dot) {
        return "lixi_ma_" + dot;
    }

    public static String maNhapMaMay(final String dot) {
        return "lixi_ma_may_" + dot;
    }

    public static String maTongLuong(final String dot) {
        return "lixi_ma_luong_" + dot;
    }

    // ------------------------------------------------------------------ tui

    /** Nhan 1 tui: loi nhan neu vuot tran ngay, null neu duoc (da cong). */
    public static String nhanTui(final CauHinh c, final GioiHanQua.KhoDem kho, final String dot, final String ngay,
            final String ten) {
        if (!kho.thu(maNhanTui(dot), ten.toLowerCase(), 1L, c.tuiNhanNgay, GioiHanQua.VO_HAN, ngay)) {
            return "Hôm nay bạn đã nhận đủ " + c.tuiNhanNgay + " bao lì xì. Hẹn ngày mai.";
        }
        return null;
    }

    /** Mo 1 tui: loi nhan neu vuot tran (ngay / ca dot), null neu duoc (da cong). */
    public static String moTui(final CauHinh c, final GioiHanQua.KhoDem kho, final String dot, final String ngay,
            final String ten) {
        if (!kho.thu(maMoTui(dot), ten.toLowerCase(), 1L, c.tuiMoNgay, c.tuiMoDot, ngay)) {
            return kho.tong(maMoTui(dot), ten.toLowerCase()) >= c.tuiMoDot
                    ? "Bạn đã mở đủ " + c.tuiMoDot + " bao lì xì của đợt Tết này."
                    : "Hôm nay bạn đã mở đủ " + c.tuiMoNgay + " bao lì xì. Hẹn ngày mai.";
        }
        return null;
    }

    /** Xu thuong cua 1 tui: [min%, max%] thu nhap 1 gio o cap nguoi mo, toi thieu 1. */
    public static long xuTui(final CauHinh c, final long xuMoiGio, final Random r) {
        final int pt = c.xuPhanTramGioMin + (c.xuPhanTramGioMax > c.xuPhanTramGioMin
                ? r.nextInt(c.xuPhanTramGioMax - c.xuPhanTramGioMin + 1) : 0);
        return Math.max(1L, Math.max(1L, xuMoiGio) * pt / 100L);
    }

    /** Giai lon: giaiLonPhanTramGio% thu nhap 1 gio o cap nguoi mo. */
    public static long giaiLon(final CauHinh c, final long xuMoiGio) {
        return Math.max(1L, Math.max(1L, xuMoiGio) * c.giaiLonPhanTramGio / 100L);
    }

    // ------------------------------------------------------------------ ma

    /** So luong khoa cua 1 ma. */
    public static int luongKhoaMa(final CauHinh c, final Random r) {
        return c.maLuongKhoaMin + (c.maLuongKhoaMax > c.maLuongKhoaMin ? r.nextInt(c.maLuongKhoaMax - c.maLuongKhoaMin + 1) : 0);
    }

    /**
     * Nhap ma: kiem + giu cho (tai khoan 1 lan / dot, thiet bi + IP toi da maTaiKhoanMoiMay tai khoan / dot, tran tong
     * luong khoa). Null = duoc (da giu cho; neu buoc sau that bai thi goi {@link #hoanMa}). Khoa thiet bi theo tai khoan
     * nen cung tai khoan khong tinh lai.
     */
    public static String giuMa(final CauHinh c, final GioiHanQua.KhoDem kho, final String dot, final String ngay,
            final int userId, final String may, final String ip, final int luong) {
        final String tk = "u" + userId;
        if (!kho.thu(maNhapMa(dot), tk, 1L, GioiHanQua.VO_HAN, 1L, ngay)) {
            return "Mỗi tài khoản chỉ nhập 1 mã lì xì trong đợt Tết này.";
        }
        final String[] khoaMay = {may == null || may.isEmpty() ? null : "m:" + may, ip == null || ip.isEmpty() ? null : "i:" + ip};
        int daGiu = 0;
        for (final String k : khoaMay) {
            if (k == null) {
                daGiu++;
                continue;
            }
            if (!kho.thu(maNhapMaMay(dot), k, 1L, GioiHanQua.VO_HAN, c.maTaiKhoanMoiMay, ngay)) {
                // tra lai cac o da cong
                kho.hoan(maNhapMa(dot), tk, 1L, ngay);
                for (int i = 0; i < daGiu; i++) {
                    if (khoaMay[i] != null) {
                        kho.hoan(maNhapMaMay(dot), khoaMay[i], 1L, ngay);
                    }
                }
                return "Thiết bị / mạng này đã nhập mã lì xì cho " + c.maTaiKhoanMoiMay + " tài khoản trong đợt Tết này.";
            }
            daGiu++;
        }
        if (!kho.thu(maTongLuong(dot), "*", luong, GioiHanQua.VO_HAN, c.maTongLuongKhoaDot, ngay)) {
            hoanMa(c, kho, dot, ngay, userId, may, ip, 0);
            return "Quỹ lì xì đợt Tết này đã phát hết.";
        }
        return null;
    }

    /** Tra lai cho da giu boi {@link #giuMa} (ma sai / da dung). */
    public static void hoanMa(final CauHinh c, final GioiHanQua.KhoDem kho, final String dot, final String ngay, final int userId,
            final String may, final String ip, final int luong) {
        kho.hoan(maNhapMa(dot), "u" + userId, 1L, ngay);
        if (may != null && !may.isEmpty()) {
            kho.hoan(maNhapMaMay(dot), "m:" + may, 1L, ngay);
        }
        if (ip != null && !ip.isEmpty()) {
            kho.hoan(maNhapMaMay(dot), "i:" + ip, 1L, ngay);
        }
        if (luong > 0) {
            kho.hoan(maTongLuong(dot), "*", luong, ngay);
        }
    }
}
