package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * CHỦ ĐỀ GIAO DIỆN 0.9.5 (một nơi duy nhất: bảng màu + số đo + kiểu chữ) cho HUD, bảng, hộp thoại, thông báo, nút và
 * dòng đáy. Hướng "Pokémon sáng, thân thiện": thẻ TRẮNG / kem, dải đầu đỏ Poké Ball (hoặc xanh), bo góc, bóng mềm,
 * viền tối mảnh; chữ tối trên nền sáng. Bản PC (htbb.pc.GiaoDienHD / ChuHD) và APK (HdVe / HdChu) đọc cùng hằng số này
 * nên ba bản cùng một kiểu.
 *
 * Chữ giao diện: {@link #chu} / {@link #rong} theo VAI (tiêu đề, nhãn, thường, phụ, số). Bản PC / APK vẽ bằng phông
 * thật (Noto Sans / Be Vietnam Pro) đúng cỡ, đúng độ đậm qua {@link ChuUI#bo}; máy J2ME dùng font ảnh của game.
 */
public final class ChuDe {
    private ChuDe() {
    }

    // ------------------------------------------------------------------ bảng màu (RGB)
    /** thân thẻ / bảng: trắng -> trắng xanh rất nhạt */
    public static final int NEN = 0xFFFFFF;
    public static final int NEN_DUOI = 0xF5F7FB;
    /** kem ấm (thẻ thông tin, hộp lời thoại) */
    public static final int KEM = 0xFFF8EA;
    /** vùng phụ trong bảng (khung con, rãnh tab, ô trống) */
    public static final int NEN_PHU = 0xEEF2F8;
    public static final int NEN_PHU_TOI = 0xE2E8F1;
    /** viền tối mảnh quanh thẻ / nút */
    public static final int VIEN = 0x2F3A5A;
    /** đường kẻ / viền hàng nhạt */
    public static final int KE = 0xD6DDE8;
    /** chữ */
    public static final int CHU = 0x1F2740;
    public static final int CHU_PHU = 0x5E6A85;
    public static final int CHU_NHAT = 0x9AA3B8;
    /** nhấn: đỏ Poké Ball, xanh, vàng Pikachu */
    public static final int DO = 0xE8412C;
    public static final int DO_TOI = 0xC9301E;
    public static final int DO_NHAT = 0xFDE7E3;
    public static final int XANH = 0x3B7DD8;
    public static final int XANH_TOI = 0x2B64B8;
    public static final int XANH_NHAT = 0xE3EEFC;
    public static final int VANG = 0xFFC93C;
    public static final int VANG_NHAT = 0xFFF4D2;
    public static final int CAM = 0xF39A2B;
    public static final int LA = 0x43B05C;
    public static final int LA_TOI = 0x2F8F45;
    public static final int TEAL = 0x22A699;
    public static final int XAM = 0x8A94A6;
    public static final int TIM = 0x8E6BE8;
    public static final int HONG = 0xEF6A93;
    /** "bóng đổ" (khối này -> bóng mềm ở bản HD; J2ME: khối tối lệch 1-2 px) */
    public static final int BONG = 0x0A0F2C;
    /** phủ mờ thế giới sau bảng / menu: ~45 % */
    public static final int PHU_NEN = 0x73101828;
    /** thanh máu (xanh lá -> vàng -> đỏ), PP xanh dương, EXP vàng */
    public static final int HP_XANH = 0x3CC45A;
    public static final int HP_VANG = 0xF5B921;
    public static final int HP_DO = 0xE8412C;
    public static final int PP = 0x3B8FE8;
    public static final int EXP = 0xF2B705;
    /** rãnh thanh trên thẻ sáng */
    public static final int RANH = 0xDDE3EC;

    /** Màu nút tròn HUD (mỗi nút một màu): túi cam, Pokémon đỏ, nhiệm vụ vàng, đội xanh lá, bản đồ teal, cài đặt xám. */
    public static final int NUT_TUI = CAM, NUT_POKE = DO, NUT_NV = 0xF2B417, NUT_DOI = LA, NUT_BAN_DO = TEAL, NUT_CAI_DAT = XAM;

    // ------------------------------------------------------------------ số đo (px logic, khung 640x360)
    /** bo góc thẻ lớn / ô / nút */
    public static final int BO = 10, BO_O = 7, BO_NUT = 8;
    /** lề trong thẻ */
    public static final int DEM = 8;
    /** khe giữa ô lưới (~12 px thật trên PC 1280) */
    public static final int KHE = 6;

    /** Đang ở máy cảm ứng (cỡ chạm tối thiểu lớn - điện thoại / máy tính bảng): chữ, nút to hơn một chút. */
    public static boolean dt() {
        return VungCham.MIN >= 24;
    }

    /** Cao đầu bảng mảnh (~40 px thật PC, >= cỡ chạm). */
    public static int caoDau() {
        return Bang.co(dt() ? 30 : 22);
    }

    // ------------------------------------------------------------------ chữ theo vai
    public static final int TIEU_DE = 0, NHAN = 1, THUONG = 2, PHU = 3, SO = 4, NHAN_NHO = 5;
    /** cỡ chữ (px logic) theo vai: PC / máy cảm ứng */
    static final float[] CO_PC = {8.6f, 7.4f, 7.2f, 6.3f, 6.6f, 6.6f};
    static final float[] CO_DT = {9.6f, 8.4f, 8.2f, 7.2f, 7.4f, 7.6f};
    /** 0 thường, 1 đậm vừa, 2 đậm */
    static final int[] DAM = {2, 1, 0, 0, 2, 1};

    public static float co(int vai) {
        return (dt() ? CO_DT : CO_PC)[vai];
    }

    /** Font ảnh J2ME cho vai (khi không có chữ thật). */
    static Font fontAnh(int vai, int mau) {
        boolean sang = GiaoDien.doSang(mau) > 150;
        if (vai == TIEU_DE) {
            return sang ? Font.borderFont : Font.blackFont;
        }
        if (vai == SO || vai == PHU) {
            return sang ? Font.arialFontW : Font.arialFont;
        }
        return sang ? Font.arialFontW : Font.blackFont;
    }

    /** Cao một dòng chữ vai (px logic). */
    public static int cao(int vai) {
        ChuUI b = ChuUI.bo;
        if (b != null) {
            return b.cao(co(vai));
        }
        return fontAnh(vai, CHU).getHeight();
    }

    /** Bề ngang chuỗi (px logic). */
    public static int rong(String s, int vai) {
        if (s == null) {
            return 0;
        }
        ChuUI b = ChuUI.bo;
        if (b != null) {
            return b.rong(s, co(vai), DAM[vai]);
        }
        return fontAnh(vai, CHU).getWidth(s);
    }

    /** Vẽ chữ: y = đỉnh hộp dòng (cao {@link #cao}), canh 0 trái / 1 phải / 2 giữa. */
    public static void chu(Graphics g, String s, int x, int y, int vai, int mau, int canh) {
        if (s == null || s.length() == 0) {
            return;
        }
        ChuUI b = ChuUI.bo;
        if (b != null) {
            b.ve(g, s, x, y, co(vai), DAM[vai], mau, canh);
            return;
        }
        Font f = fontAnh(vai, mau);
        f.drawString(g, s, x, y, canh == 2 ? Font.CENTER : canh == 1 ? Font.RIGHT : Font.LEFT);
    }

    /** Vẽ chữ canh giữa theo chiều dọc trong (y, h). */
    public static void chuGiua(Graphics g, String s, int x, int y, int h, int vai, int mau, int canh) {
        chu(g, s, x, y + (h - cao(vai)) / 2, vai, mau, canh);
    }

    /** Cắt chuỗi cho vừa w (thêm "…"). Có bộ nhớ đệm nhỏ: không cắt lại mỗi khung. */
    public static String cat(String s, int vai, int w) {
        if (s == null || rong(s, vai) <= w) {
            return s;
        }
        String k = CAT_KHOA[vai];
        if (s.equals(k) && CAT_W[vai] == w) {
            return CAT_KQ[vai];
        }
        String ba = ChuUI.bo != null ? "\u2026" : "..";
        int n = s.length();
        while (n > 1 && rong(s.substring(0, n) + ba, vai) > w) {
            n--;
        }
        String r = s.substring(0, n).trim() + ba;
        CAT_KHOA[vai] = s;
        CAT_W[vai] = w;
        CAT_KQ[vai] = r;
        return r;
    }

    private static final String[] CAT_KHOA = new String[6];
    private static final String[] CAT_KQ = new String[6];
    private static final int[] CAT_W = new int[6];

    /** Ngắt dòng theo bề ngang w (tối đa toiDa dòng; dòng cuối thừa thì cắt "…"). */
    public static String[] ngat(String s, int vai, int w, int toiDa) {
        java.util.Vector v = new java.util.Vector();
        if (s != null) {
            String[] doan = tach(s, '\n');
            for (int d = 0; d < doan.length && v.size() < toiDa; d++) {
                String con = doan[d].trim();
                if (con.length() == 0) {
                    v.addElement("");
                    continue;
                }
                while (con.length() > 0 && v.size() < toiDa) {
                    int n = con.length();
                    while (n > 1 && rong(con.substring(0, n), vai) > w) {
                        int sp = con.lastIndexOf(' ', n - 1);
                        n = sp > 0 ? sp : n - 1;
                    }
                    v.addElement(con.substring(0, n).trim());
                    con = con.substring(n).trim();
                    if (con.length() > 0 && v.size() == toiDa) {
                        String cuoi = (String) v.elementAt(toiDa - 1);
                        v.setElementAt(cat(cuoi + " " + con, vai, w), toiDa - 1);
                    }
                }
            }
        }
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }

    static String[] tach(String s, char c) {
        java.util.Vector v = new java.util.Vector();
        int i = 0;
        while (true) {
            int j = s.indexOf(c, i);
            if (j < 0) {
                v.addElement(s.substring(i));
                break;
            }
            v.addElement(s.substring(i, j));
            i = j + 1;
        }
        String[] r = new String[v.size()];
        v.copyInto(r);
        return r;
    }

    // ------------------------------------------------------------------ khối cơ bản
    /** Thẻ sáng: bóng mềm + viền tối mảnh + thân trắng. */
    public static void the(Graphics g, int x, int y, int w, int h, int bo) {
        GiaoDien.theSang(g, x, y, w, h, bo);
        Font.nenSang = true;
    }

    /**
     * Đầu bảng mảnh: dải đỏ (hoặc màu nhấn) bo góc trên, Poké Ball nhỏ, tiêu đề chữ thật trắng. Trả cao đã dùng.
     * Không vẽ nút đóng (Bang.nutDong đặt trên dải).
     */
    public static void dau(Graphics g, int x, int y, int w, int h, String tieuDe, int mau) {
        GiaoDien.khoiDoc(g, x, y, w, h, BO - 1, GiaoDien.pha(mau, 0xFFFFFF, 1, 6), mau);
        // mép dưới vuông (đầu nối liền thân thẻ)
        g.setColor(mau);
        int bo = Math.min(BO - 1, h / 2);
        g.fillRect(x, y + h - bo, w, bo);
        int r = Math.max(4, Math.min(7, h / 2 - 4));
        GiaoDien.pokeBall(g, x + 8 + r, y + h / 2, r, 0);
        if (tieuDe != null) {
            int xt = x + 8 + 2 * r + 7;
            chuGiua(g, cat(tieuDe, TIEU_DE, w - (xt - x) - h - 6), xt, y, h, TIEU_DE, 0xFFFFFF, 0);
        }
    }

    /** Chấm đỏ báo mới (8-10 px thật) tâm (cx, cy). */
    public static void cham(Graphics g, int cx, int cy) {
        // ~8-10 px thật (PC 1280: 5 px logic x 2), điện thoại to hơn chút
        int d = dt() ? 6 : 5;
        g.setColor(0xFFFFFF);
        g.fillArc(cx - d / 2 - 1, cy - d / 2 - 1, d + 2, d + 2, 0, 360);
        g.setColor(DO);
        g.fillArc(cx - d / 2, cy - d / 2, d, d, 0, 360);
    }

    /** Dấu X vẽ nét (không ảnh), tâm (cx, cy), nửa cạnh k. */
    public static void dauX(Graphics g, int cx, int cy, int k, int mau) {
        ChuUI b = ChuUI.bo;
        if (b != null && b.net(g, cx, cy, k, mau, 0)) {
            return;
        }
        g.setColor(mau);
        for (int d = 0; d <= 1; d++) {
            g.drawLine(cx - k + d, cy - k, cx + k + d, cy + k);
            g.drawLine(cx + k + d, cy - k, cx - k + d, cy + k);
        }
    }

    /** Phủ mờ cả màn (thế giới vẫn thấy ~55 %). */
    public static void phuMan(Graphics g) {
        Canvas.resetTrans(g);
        KieuO.phu(g, 0, 0, Canvas.w, Canvas.h, PHU_NEN);
    }
}
