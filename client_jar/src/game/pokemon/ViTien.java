package game.pokemon;

import game.model.MainChar;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * HÀNG VÍ (0.9.7, yêu cầu chủ dự án "không thấy xu / lượng ở đâu"): Xu, Lượng, Lượng khoá của nhân vật chính với biểu
 * tượng TienTe, số gọn ("1,25 Tr"); chạm một mục = chú thích + số đầy đủ (TienTe.hienGoiY). Đọc thẳng MainChar mỗi
 * khung -> đổi ngay khi gói tiền tới (gói -105 TrangThaiNguoiChoi, gói thông tin 3...). Dùng dưới lưới hành trang, ở
 * màn Nhân vật, cửa hàng; bản gọn {@link #hangGon} trong thẻ người chơi HUD (không vùng chạm riêng: chạm thẻ = hồ sơ).
 */
public final class ViTien {
    private ViTien() {
    }

    /**
     * Lượng khoá có hiện ở hàng ví đầy đủ (máy chủ gửi > -1): cả khi 0 - cửa hàng đặc biệt có món trả bằng lượng khoá,
     * người chơi cần thấy số đang có (trước chỉ hiện khi > 0).
     */
    static boolean coKhoa(MainChar mc) {
        return !boKhoa && mc != null && mc.luongKhoa >= 0;
    }

    /** LuoiTui: bảng quá thấp cho ví hai dòng - tạm bỏ mục lượng khoá (đặt lại sau khi vẽ). */
    static boolean boKhoa;

    /** Một mục: biểu tượng cỡ co + số; trả bề ngang. */
    static int muc(Graphics g, int loai, long v, int x, int y, int h, int co, int vai, int mau, boolean vien) {
        TienTe.ve(g, loai, x, y + (h - co) / 2, co);
        String s = TienTe.gon(v);
        int xs = x + co + 3;
        if (vien) {
            ChuDe.chuVienGiua(g, s, xs, y, h, vai, mau, ChuDe.VIEN, 0);
        } else {
            ChuDe.chuGiua(g, s, xs, y, h, vai, mau, 0);
        }
        return co + 3 + ChuDe.rong(s, vai);
    }

    static int rongMuc(int loai, long v, int co, int vai) {
        return co + 3 + ChuDe.rong(TienTe.gon(v), vai);
    }

    /** Bản gọn (thẻ HUD): xu rồi lượng (+ lượng khoá) trong (x, y, w, h), chữ trắng viền. */
    public static void hangGon(Graphics g, int x, int y, int w, int h, MainChar mc) {
        if (mc == null) {
            return;
        }
        // 0.9.7: biểu tượng = cao chữ (không to hơn hàng): ảnh 8 px ở hàng 10 px
        int co = Math.max(8, Math.min(h - 2, ChuDe.cao(ChuDe.NHAN_NHO)));
        int vai = ChuDe.NHAN_NHO;
        int xx = x;
        xx += muc(g, TienTe.XU, mc.charXu, xx, y, h, co, vai, 0xFFE89A, true) + 8;
        if (xx < x + w - 20) {
            xx += muc(g, TienTe.LUONG, mc.luong, xx, y, h, co, vai, 0xC8E4FF, true) + 8;
        }
        if (mc.luongKhoa > 0 && xx + rongMuc(TienTe.LUONG_KHOA, mc.luongKhoa, co, vai) <= x + w) {
            muc(g, TienTe.LUONG_KHOA, mc.luongKhoa, xx, y, h, co, vai, 0xD8D8DE, true);
        }
    }

    /** Cao hàng ví đầy đủ (>= cỡ chạm). */
    public static int cao() {
        return Bang.co(ChuDe.dt() ? 24 : 20);
    }

    /**
     * Hàng ví đầy đủ (dưới lưới hành trang / màn Nhân vật / cửa hàng): thẻ tối lõm, 3 mục chia đều, mỗi mục là vùng
     * chạm (chú thích + số đầy đủ).
     */
    public static void hang(Graphics g, int x, int y, int w, int h) {
        MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        if (!KhungNro.ve(g, KhungNroBang.THE_TOI, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, 0x2A1C12);
        }
        if (mc == null) {
            return;
        }
        boolean nenCu = game.render.screen.Font.nenSang;
        game.render.screen.Font.nenSang = false;   // nền tối: font ảnh J2ME giữ bản chữ sáng
        try {
            hang0(g, x, y, w, h, mc);
        } finally {
            game.render.screen.Font.nenSang = nenCu;
        }
    }

    /**
     * 0.9.10: số dòng hàng ví bề ngang w cần (font ảnh J2ME rộng hơn chữ HD: "1,25 Tr" + lượng không vừa một dòng ở
     * 4:3 -> chữ đè biểu tượng mục sau). Xếp tham từng mục, đủ dòng thì xuống dòng.
     */
    public static int soDong(int w) {
        MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        if (mc == null) {
            return 1;
        }
        return dong(mc, w, coIcon(mc, w), ChuDe.SO, null);
    }

    /**
     * Cỡ biểu tượng hàng ví: 16 (cao hàng - 6); một dòng không vừa mà biểu tượng 12 px vừa thì 12 (font ảnh J2ME không có
     * cỡ chữ nhỏ hơn - giữ một dòng để lưới hành trang đủ cao).
     */
    static int coIcon(MainChar mc, int w) {
        int co = Math.min(16, cao() - 6);
        if (co > 12 && dong(mc, w, co, ChuDe.SO, null) > 1 && dong(mc, w, 12, ChuDe.SO, null) == 1) {
            return 12;
        }
        return co;
    }

    /** Chia mục vào dòng (ghi chỉ số dòng từng mục vào ra nếu != null); trả số dòng. */
    static int dong(MainChar mc, int w, int co, int vai, int[] ra) {
        int n = coKhoa(mc) ? 3 : 2;
        int[] loai = {TienTe.XU, TienTe.LUONG, TienTe.LUONG_KHOA};
        long[] gt = {mc.charXu, mc.luong, mc.luongKhoa};
        int[] wm = new int[3];
        for (int i = 0; i < n; i++) {
            wm[i] = rongMuc(loai[i], gt[i], co, vai);
        }
        int d = 0, dau = 0;
        for (int i = 0; i < n; i++) {
            if (i > dau && !vua(wm, dau, i - dau + 1, w - 8)) {
                d++;
                dau = i;
            }
            if (ra != null) {
                ra[i] = d;
            }
        }
        return d + 1;
    }

    /** so mục từ dau vừa một dòng rộng wTrong: ô chia đều vừa từng mục, hoặc xếp tham (mỗi mục + khe) vừa. */
    /** khe chữ tối thiểu giữa hai mục (số mục trước - biểu tượng mục sau) khi xếp tham */
    static final int KHE = 4;

    static boolean vua(int[] wm, int dau, int so, int wTrong) {
        // 0.9.20: mỗi mục là vùng chạm >= cỡ chạm (bảng hẹp 3 cột ô ở điện thoại mật độ cao: "320" chỉ 33 px) - không đủ
        // chỗ cho so vùng thì xuống dòng
        if (so > 1 && wTrong < so * (VungCham.MIN + Bang.cach())) {
            return false;
        }
        boolean deu = true;
        int tong = 0;
        for (int i = dau; i < dau + so; i++) {
            deu &= wm[i] <= wTrong / so - 2;
            tong += wm[i];
        }
        return deu || (wTrong - tong) / so >= KHE;
    }

    static void hang0(Graphics g, int x, int y, int w, int h, MainChar mc) {
        int n = coKhoa(mc) ? 3 : 2;
        int[] loai = {TienTe.XU, TienTe.LUONG, TienTe.LUONG_KHOA};
        long[] gt = {mc.charXu, mc.luong, mc.luongKhoa};
        int[] mau = {0xFFE89A, 0xC8E4FF, 0xD8D8DE};
        int vai = ChuDe.SO;
        int co0 = coIcon(mc, w);
        int soD = dong(mc, w, co0, vai, null);
        int kD = Bang.cach();
        int hD = (h - (soD - 1) * kD) / soD;
        int co = Math.min(co0, hD - 6);
        int[] dongCua = new int[3];
        dong(mc, w, co, vai, dongCua);
        int k = Bang.cach();
        int wTrong = w - 8;
        for (int d = 0; d < soD; d++) {
            int yd = y + d * (hD + kD);
            int dau = -1, so = 0, tong = 0;
            int[] wm = new int[3];
            for (int i = 0; i < n; i++) {
                if (dongCua[i] != d) {
                    continue;
                }
                if (dau < 0) {
                    dau = i;
                }
                so++;
                wm[i] = rongMuc(loai[i], gt[i], co, vai);
                tong += wm[i];
            }
            if (so == 0) {
                continue;
            }
            // ô chia đều (như trước) khi mọi mục vừa ô; không thì ô = bề ngang thật từng mục + phần dư chia đều (khe chữ >= KHE)
            int wo = wTrong / so;
            boolean deu = true;
            for (int i = dau; i < dau + so; i++) {
                deu &= wm[i] <= wo - 2;
            }
            int du = deu ? 0 : Math.max(0, wTrong - tong) / so;
            int xL = x + 4, xR = x + 4 + wTrong;
            int xo = xL;
            int[] bien = new int[so + 1];   // ranh giới ô (vẽ) -> vùng chạm
            bien[0] = xL;
            for (int i = dau; i < dau + so; i++) {
                int j = i - dau;
                int wO = deu ? wo : wm[i] + du;
                if (j == so - 1 && !deu) {
                    wO = xR - xo;
                }
                int xm = xo + Math.max(2, (wO - wm[i]) / 2);
                muc(g, loai[i], gt[i], xm, yd, hD, co, vai, mau[i], false);
                xo += wO;
                bien[j + 1] = xo;
            }
            // vùng chạm: cả ô (trừ khe) -> chú thích + số đầy đủ; ô xếp tham hẹp thì dời ranh giới cho mỗi vùng >= cỡ chạm
            int min = VungCham.MIN;
            for (int j = 1; j < so; j++) {
                int lo = xL + j * (min + k), hi = xR - (so - j) * (min + k);
                if (lo <= hi) {
                    bien[j] = Math.max(lo, Math.min(hi, bien[j]));
                }
            }
            for (int i = dau; i < dau + so; i++) {
                int j = i - dau;
                TienTe.dangKyGoiY(g, loai[i], gt[i], bien[j] + (j == 0 ? 0 : k / 2), yd, bien[j + 1] - bien[j] - k, hD);
            }
        }
    }
}
