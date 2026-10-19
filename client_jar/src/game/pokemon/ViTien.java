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

    /** Lượng khoá có hiện (máy chủ gửi > -1). */
    static boolean coKhoa(MainChar mc) {
        return mc != null && mc.luongKhoa > 0;
    }

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
        int co = Math.max(8, h);
        int vai = ChuDe.NHAN_NHO;
        int xx = x;
        xx += muc(g, TienTe.XU, mc.charXu, xx, y, h, co, vai, 0xFFE89A, true) + 8;
        if (xx < x + w - 20) {
            xx += muc(g, TienTe.LUONG, mc.luong, xx, y, h, co, vai, 0xC8E4FF, true) + 8;
        }
        if (coKhoa(mc) && xx + rongMuc(TienTe.LUONG_KHOA, mc.luongKhoa, co, vai) <= x + w) {
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
        int n = coKhoa(mc) ? 3 : 2;
        int[] loai = {TienTe.XU, TienTe.LUONG, TienTe.LUONG_KHOA};
        long[] gt = {mc.charXu, mc.luong, mc.luongKhoa};
        int[] mau = {0xFFE89A, 0xC8E4FF, 0xD8D8DE};
        int co = Math.min(16, h - 6);
        int wo = (w - 8) / n;
        int k = Bang.cach();
        for (int i = 0; i < n; i++) {
            int xo = x + 4 + i * wo;
            int wm = rongMuc(loai[i], gt[i], co, ChuDe.SO);
            int xm = xo + Math.max(2, (wo - wm) / 2);
            muc(g, loai[i], gt[i], xm, y, h, co, ChuDe.SO, mau[i], false);
            // vùng chạm: cả ô (trừ khe) -> chú thích + số đầy đủ
            TienTe.dangKyGoiY(g, loai[i], gt[i], xo + (i == 0 ? 0 : k / 2), y, wo - k, h);
        }
    }
}
