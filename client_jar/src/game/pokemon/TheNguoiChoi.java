package game.pokemon;

import game.model.MainChar;
import javax.microedition.lcdui.Graphics;

/**
 * THẺ NGƯỜI CHƠI HUD (0.9.7 kiểu NRO) - một mã cho bản PC (htbb.pc.TheTrangThai gọi) và mọi màn rộng: bảng gỗ tối chỉ
 * vàng, chân dung Pokémon trong ô hộp gỗ khung vàng + biển Lv cam, tên chữ trắng viền + ô hệ, % EXP; thanh HP ĐỎ / PP
 * xanh / EXP vàng (rãnh tối pixel) có số; dưới cùng hàng VÍ gọn: Xu + Lượng (+ Lượng khoá nếu có) - chạm = chú thích
 * + số đầy đủ (TienTe). Số đọc thẳng MainChar mỗi khung -> đổi ngay khi gói tiền / HP tới (TrangThaiNguoiChoi).
 */
public final class TheNguoiChoi {
    private TheNguoiChoi() {
    }

    /** cỡ thẻ (px logic) - khớp vùng chạm HudRong.rongThe / caoThe */
    /** 0.9.7: cao 56 - năm hàng tách bạch (tên / HP / PP / EXP / ví), không hàng nào đè hàng khác */
    public static final int RONG = 166, CAO = 56;
    /** đỉnh từng hàng (tính từ đỉnh thẻ) và cao hàng */
    static final int Y_TEN = 2, H_TEN = 11, Y_HP = 14, H_HP = 9, Y_PP = 25, H_PP = 7, Y_EXP = 34, H_EXP = 6, Y_VI = 43, H_VI = 10;

    private static final String[] SO_CU = new String[4];
    private static final long[] SO_GIA = {-1, -1, -1, -1};
    private static final long[] SO_MAX = {-1, -1, -1, -1};

    /** "a/b" nhớ sẵn (không tạo chuỗi mỗi khung). */
    static String phan(int k, long a, long b) {
        if (SO_CU[k] == null || SO_GIA[k] != a || SO_MAX[k] != b) {
            SO_GIA[k] = a;
            SO_MAX[k] = b;
            SO_CU[k] = a + "/" + b;
        }
        return SO_CU[k];
    }

    /** Vẽ thẻ tại (x, y) cỡ RONG x CAO. */
    /** lần cuối thẻ vẽ nhãn 12+ (GameScr.paint12plus không vẽ nhãn trôi nữa) */
    public static volatile long luc12;

    /** true = khung vừa rồi thẻ đã vẽ nhãn 12+. */
    public static boolean daVe12() {
        return System.currentTimeMillis() - luc12 < 400;
    }

    public static void ve(Graphics g, MainChar mc, int x, int y) {
        if (mc == null) {
            return;
        }
        int w = RONG, h = CAO;
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, 0x2B1A10);
        }
        // chân dung trong ô hộp gỗ khung vàng
        int ps = 38, px = x + 3, py = y + 3;
        if (!KhungNro.ve(g, KhungNroBang.O_CHON, px, py, ps, ps)) {
            GiaoDien.khoi(g, px, py, ps, ps, 3, ChuDe.VANG);
        }
        int tx = g.getTranslateX(), ty = g.getTranslateY();
        int kx = g.getClipX(), ky = g.getClipY(), kw = g.getClipWidth(), kh = g.getClipHeight();
        g.clipRect(px + 4, py + 4, ps - 8, ps - 8);
        try {
            boolean ok = mc.pokeDex > 0 && PokeBody.paintChanDung(g, mc.pokeDex, mc.pokeShiny, px + ps / 2, py + ps / 2 + 1, ps - 8);
            if (!ok) {
                game.render.Res.getCharPartInfo(2, mc.currentHead).paintStatic(g, (short) (px + ps / 2), (short) (py + ps / 2 + 2), 0, mc.frame);
            }
        } catch (Throwable e) {
            // chưa có ảnh
        }
        g.translate(tx - g.getTranslateX(), ty - g.getTranslateY());
        g.setClip(kx, ky, kw, kh);
        // biển Lv cam dưới chân dung
        String lv = "Lv " + (int) mc.level;
        int wl = Math.max(26, ChuDe.rong(lv, ChuDe.NHAN_NHO) + 8), hl = 10;
        int xl = px + ps / 2 - wl / 2, yl = py + ps - 4;
        GiaoDien.khoiPixel(g, xl, yl, wl, hl, ChuDe.CHINH);
        ChuDe.chuVienGiua(g, lv, px + ps / 2, yl, hl, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        if ((mc.basePointLeft > 0 || mc.skillPointLeft > 0) && System.currentTimeMillis() % 1000 < 650) {
            ChuDe.cham(g, px + 4, py + 4);   // điểm tiềm năng / kỹ năng chưa dùng
        }
        // tên + ô hệ + % EXP
        int x0 = x + 45, wB = x + w - 5 - x0;
        String pc = mc.getPercent();
        int wPc = pc == null ? 0 : ChuDe.rong(pc, ChuDe.PHU);
        String he = null;
        int mauHe = 0;
        try {
            if (mc.pokeDex > 0) {
                int h1 = HePoke.he1(mc.pokeDex);
                he = KieuO.tenHe(h1);
                mauHe = KieuO.mauHe(h1);
            }
        } catch (Throwable e) {
            he = null;
        }
        int wHe = he == null || he.length() == 0 ? 0 : ChuDe.rong(he, ChuDe.NHAN_NHO) + 8;
        String ten = ChuDe.cat(mc.name == null ? "" : mc.name, ChuDe.NHAN, wB - wPc - wHe - 6);
        int hTen = 11;
        ChuDe.chuVienGiua(g, ten, x0, y + 2, hTen, ChuDe.NHAN, 0xFFFFFF, ChuDe.VIEN, 0);
        if (wHe > 0) {
            int xh = x0 + ChuDe.rong(ten, ChuDe.NHAN) + 4;
            GiaoDien.khoiPixel(g, xh, y + 3, wHe, 9, mauHe);
            ChuDe.chuVienGiua(g, he, xh + wHe / 2, y + 3, 9, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        }
        if (pc != null) {
            ChuDe.chuGiua(g, pc, x + w - 5, y + 2, hTen, ChuDe.PHU, ChuDe.VANG, 1);
        }
        // HP / PP / EXP: nhãn trái (cột wN), thanh + số giữa thanh; mỗi hàng một dải riêng
        int wN = Math.max(13, ChuDe.rong("EXP", ChuDe.NHAN_NHO) + 2);
        ChuDe.chuVienGiua(g, "HP", x0, y + Y_HP, H_HP, ChuDe.NHAN_NHO, 0xFF9478, ChuDe.VIEN, 0);
        GiaoDien.thanh(g, x0 + wN, y + Y_HP, wB - wN, H_HP, Math.max(0, mc.hp), mc.maxhp, 0);
        ChuDe.chuVienGiua(g, phan(0, Math.max(0, mc.hp), mc.maxhp), x0 + wN + (wB - wN) / 2, y + Y_HP, H_HP, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        ChuDe.chuVienGiua(g, "PP", x0, y + Y_PP, H_PP, ChuDe.NHAN_NHO, 0xA8CCFF, ChuDe.VIEN, 0);
        GiaoDien.thanh(g, x0 + wN, y + Y_PP, wB - wN, H_PP, Math.max(0, mc.mp), mc.maxmp, 1);
        ChuDe.chuVienGiua(g, phan(1, Math.max(0, mc.mp), mc.maxmp), x0 + wN + (wB - wN) / 2, y + Y_PP, H_PP, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        ChuDe.chuVienGiua(g, "EXP", x0, y + Y_EXP, H_EXP, ChuDe.NHAN_NHO, ChuDe.VANG, ChuDe.VIEN, 0);
        GiaoDien.thanh(g, x0 + wN, y + Y_EXP, wB - wN, H_EXP, mc.lvpercent, 1000, 2);
        // ví gọn: xu + lượng (+ lượng khoá) - biểu tượng cao bằng chữ (ảnh 8 px), hàng riêng dưới EXP
        int w12 = 20;
        ViTien.hangGon(g, x0, y + Y_VI, x + w - 5 - x0 - w12 - 4, H_VI, mc);
        // nhãn độ tuổi 12+ (bắt buộc hiện trong game): chỗ cố định cuối hàng ví, trong thẻ - không trôi đè thế giới / thẻ
        int x12 = x + w - 5 - w12;
        GiaoDien.khoi(g, x12, y + Y_VI, w12, H_VI, 3, 0xE8412C);
        ChuDe.chuGiua(g, "12+", x12 + w12 / 2, y + Y_VI, H_VI, ChuDe.NHAN_NHO, 0xFFFFFF, 2);
        luc12 = System.currentTimeMillis();
    }
}
