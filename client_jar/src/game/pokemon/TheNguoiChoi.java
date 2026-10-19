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
    public static final int RONG = 166, CAO = 50;

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
        // HP / PP / EXP
        int wN = 13;
        ChuDe.chuVienGiua(g, "HP", x0, y + 14, 8, ChuDe.NHAN_NHO, 0xFF9478, ChuDe.VIEN, 0);
        GiaoDien.thanh(g, x0 + wN, y + 14, wB - wN, 9, Math.max(0, mc.hp), mc.maxhp, 0);
        ChuDe.chuVienGiua(g, phan(0, Math.max(0, mc.hp), mc.maxhp), x0 + wN + (wB - wN) / 2, y + 14, 9, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        ChuDe.chuVienGiua(g, "PP", x0, y + 24, 7, ChuDe.NHAN_NHO, 0xA8CCFF, ChuDe.VIEN, 0);
        GiaoDien.thanh(g, x0 + wN, y + 24, wB - wN, 7, Math.max(0, mc.mp), mc.maxmp, 1);
        ChuDe.chuVienGiua(g, phan(1, Math.max(0, mc.mp), mc.maxmp), x0 + wN + (wB - wN) / 2, y + 24, 7, ChuDe.NHAN_NHO, 0xFFFFFF, ChuDe.VIEN, 2);
        GiaoDien.thanh(g, x0 + wN, y + 32, wB - wN, 6, mc.lvpercent, 1000, 2);
        ChuDe.chuVienGiua(g, "EXP", x0, y + 32, 6, ChuDe.NHAN_NHO, ChuDe.VANG, ChuDe.VIEN, 0);
        // ví gọn: xu + lượng (+ lượng khoá)
        ViTien.hangGon(g, x0, y + 39, x + w - 5 - x0, 9, mc);
    }
}
