package htbb.pc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.microedition.lcdui.Graphics;

/**
 * Giao diện HD của bản PC = CÙNG HỆ THIẾT KẾ với bản APK (client_apk/pokemon/.../hd/HdVe.java): cùng hàm, cùng
 * token màu (đỏ Poké, vàng Pikachu, xanh, navy, kính KINH_TREN / KINH_DUOI), bo góc, bóng đổ 3 lớp, ánh kính nửa
 * trên, viền sáng mảnh - vẽ bằng Java2D khử răng cưa ở độ phân giải thật. port-j2me.py chèn
 * "if (GiaoDienHD.bat && GiaoDienHD.xxx(...)) return;" vào đầu mỗi hàm của game.pokemon.GiaoDien (+ KieuO.phuCat)
 * -> MỌI bảng, menu, hộp thoại, tab, ô chọn, thanh HP/PP/EXP, viên chữ, Poké Ball của mọi màn (hành trang, trang
 * bị, kỹ năng, tiềm năng, cửa hàng, NPC, nhiệm vụ, Gym, PvP, đăng nhập/đăng ký/chọn/tạo, ghi công, menu nhanh,
 * hộp xác nhận...) đổi kiểu mà không đổi bố cục, dữ liệu hay thao tác. Tắt: Cài đặt -> Khung HD.
 */
public final class GiaoDienHD {
    public static volatile boolean bat = true;

    // token màu (= HdVe Android)
    static final int VIEN = 0xFF1E2A44;
    static final int DO = 0xFFE3350D;
    static final int DO_SANG = 0xFFF4613A;
    static final int DO_TOI = 0xFFA5240A;
    static final int VANG = 0xFFFFCB05;
    static final int XANH = 0xFF3D7DCA;
    static final int XANH_SANG = 0xFF6FA6EA;
    static final int KINH_TREN = 0xF2213F6E;
    static final int KINH_DUOI = 0xF40E1D38;

    private GiaoDienHD() {
    }

    // ------------------------------------------------------------------ tiện ích Java2D
    private static Graphics2D bd(Graphics g) {
        Graphics2D g2 = g.g2d();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    private static final BasicStroke NET_1 = new BasicStroke(1f);

    private static boolean xong(Graphics g, Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setStroke(NET_1);
        g.setColor(g.getColor());
        return true;
    }

    private static final int[] MAU_K = new int[256];
    private static final Color[] MAU_C = new Color[256];

    /** Màu ARGB -> Color (bảng băm nhỏ, không cấp phát khi đã có). */
    static Color c(int argb) {
        int i = (argb ^ argb >>> 8 ^ argb >>> 16 ^ argb >>> 24) & 255;
        Color m = MAU_C[i];
        if (m != null && MAU_K[i] == argb) return m;
        m = new Color(argb, true);
        MAU_C[i] = m;
        MAU_K[i] = argb;
        return m;
    }

    static int argb(int rgb) {
        return 0xFF000000 | rgb;
    }

    static int pha(int a, int b, float t) {
        int ra = a >> 16 & 255, ga = a >> 8 & 255, ba = a & 255;
        int r = (int) (ra + ((b >> 16 & 255) - ra) * t);
        int gg = (int) (ga + ((b >> 8 & 255) - ga) * t);
        int bb = (int) (ba + ((b & 255) - ba) * t);
        return (a & 0xFF000000) | r << 16 | gg << 8 | bb;
    }

    static RoundRectangle2D.Float rr(float l, float t, float r, float b, float rad) {
        return new RoundRectangle2D.Float(l, t, Math.max(0, r - l), Math.max(0, b - t), 2 * rad, 2 * rad);
    }

    static void tron(Graphics2D g, float l, float t, float r, float b, float rad, int argb) {
        g.setColor(c(argb));
        g.fill(rr(l, t, r, b, rad));
    }

    static void tronDoc(Graphics2D g, float l, float t, float r, float b, float rad, int tren, int duoi) {
        g.setPaint(new GradientPaint(0, t, c(tren), 0, b, c(duoi)));
        g.fill(rr(l, t, r, b, rad));
    }

    static void vienTron(Graphics2D g, float l, float t, float r, float b, float rad, float day, int argb) {
        g.setColor(c(argb));
        g.setStroke(new BasicStroke(day));
        g.draw(rr(l + day / 2, t + day / 2, r - day / 2, b - day / 2, rad));
    }

    /** Bóng đổ mềm (3 lớp alpha thấp). */
    static void bong(Graphics2D g, float l, float t, float r, float b, float rad, float doSau) {
        for (int i = 3; i >= 1; i--) {
            float e = i * 0.9f;
            tron(g, l - e * 0.5f, t - e * 0.2f + doSau, r + e * 0.5f, b + e + doSau, rad + e, 0x1A000000);
        }
    }

    private static float L(Graphics g, int x) {
        return x + g.getTranslateX();
    }

    private static float T(Graphics g, int y) {
        return y + g.getTranslateY();
    }

    // ------------------------------------------------------------------ khung neo cho nút trong ngữ cảnh
    private static int nx, ny, nw, nh, nDt;
    /** khung lớn nhất (toạ độ giao diện) của khung game vừa xong: {x, y, w, h}, null = không có */
    static volatile int[] khungNeo;

    private static void ghiKhung(Graphics g, int x, int y, int w, int h) {
        if (KiemChong.bat) {
            Graphics2D g2 = g.g2d();
            KiemChong.ghiKhung(g2.getTransform().createTransformedShape(
                    new java.awt.Rectangle(x + g.getTranslateX(), y + g.getTranslateY(), w, h)).getBounds());
        }
        if (g.tiLeTheGioi() != 1f || g.heSo() <= 1) return;
        int dt = w * h;
        if (dt > nDt) {
            nDt = dt;
            nx = x + g.getTranslateX();
            ny = y + g.getTranslateY();
            nw = w;
            nh = h;
        }
    }

    /** Cuối khung game (NenPc): chốt khung neo. */
    static void chotKhung() {
        khungNeo = nDt > 0 ? new int[]{nx, ny, nw, nh} : null;
        nDt = 0;
    }

    // ------------------------------------------------------------------ nền dưới chữ (tương phản tự động, như APK HdVe)
    /** các khối đã tô trong khung (toạ độ thật) + độ sáng -> ChuHD chọn chữ sáng / tối cho dễ đọc */
    static final float[] NEN = new float[256 * 5];
    static int soNen;

    /** Đầu khung game (NenPc): xoá danh sách nền. */
    public static void khungMoi() {
        soNen = 0;
    }

    static int doSang(int c) {
        return ((c >> 16 & 255) * 299 + (c >> 8 & 255) * 587 + (c & 255) * 114) / 1000;
    }

    /** Ghi khối nền (toạ độ giao diện) - không cấp phát. */
    static void ghiNen(Graphics g, float x, float y, float w, float h, int sang) {
        if (w < 6 || h < 6 || g.tiLeTheGioi() != 1f) return;
        float k = g.heSo();
        float l = (x + g.getTranslateX()) * k, t = (y + g.getTranslateY()) * k;
        if (soNen >= 256) {
            System.arraycopy(NEN, 5 * 64, NEN, 0, 5 * 192);
            soNen = 192;
        }
        int i = soNen * 5;
        NEN[i] = l;
        NEN[i + 1] = t;
        NEN[i + 2] = l + w * k;
        NEN[i + 3] = t + h * k;
        NEN[i + 4] = sang;
        soNen++;
    }

    /** Độ sáng nền tại điểm (toạ độ thật): khối vẽ sau cùng chứa điểm; -1 = không biết. */
    static int sangTai(float x, float y) {
        for (int i = soNen - 1; i >= 0; i--) {
            int k = i * 5;
            if (x >= NEN[k] && x <= NEN[k + 2] && y >= NEN[k + 1] && y <= NEN[k + 3]) return (int) NEN[k + 4];
        }
        return -1;
    }

    // ------------------------------------------------------------------ thay GiaoDien
    // ------------------------------------------------------------------ 0.9.5: khối dựng sẵn (ảnh nhớ) - không dựng hình mỗi khung
    static final AnhDem DEM = new AnhDem(16L << 20);
    static final int D_KHOI = 1, D_BONG = 2, D_DOC = 3, D_THE = 4;

    static long khoa(int loai, int w, int h, int r, int a, int b, double tl) {
        long k = loai;
        k = k * 1000003L + w;
        k = k * 1000003L + h;
        k = k * 1000003L + r;
        k = k * 1000003L + a;
        k = k * 1000003L + b;
        return k * 31L + Double.doubleToLongBits(tl);
    }

    /** Vẽ khối loại `loai` từ ảnh dựng sẵn; false = quá to / tỉ lệ lẻ -> vẽ thẳng. pad = lề (bóng). */
    static boolean veDem(Graphics g, int loai, int x, int y, int w, int h, int r, int a, int b) {
        double tl = g.tiLeThat();
        if (Math.abs(tl - Math.rint(tl)) > 1e-6 || (long) w * h * tl * tl > 2400000L) return false;
        int pad = loai == D_BONG || loai == D_THE ? 4 : 0;
        long k = khoa(loai, w, h, r, a, b, tl);
        java.awt.image.BufferedImage img = DEM.lay(k);
        if (img == null) {
            int iw = (int) Math.ceil((w + 2 * pad) * tl), ih = (int) Math.ceil((h + 2 * pad) * tl);
            img = new java.awt.image.BufferedImage(Math.max(1, iw), Math.max(1, ih), java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D d = img.createGraphics();
            d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            d.scale(tl, tl);
            float l = pad, t = pad, rad = Math.min(r, Math.min(w, h) / 2f);
            switch (loai) {
                case D_KHOI:
                    tron(d, l, t, l + w, t + h, rad, argb(a));
                    break;
                case D_BONG:
                    bong(d, l, t - 1, l + w - 1, t + h - 2, rad, 0);
                    break;
                case D_DOC:
                    tronDoc(d, l, t, l + w, t + h, rad, argb(a), argb(b));
                    break;
                default:
                    theSang(d, l, t, l + w, t + h, r);
                    break;
            }
            d.dispose();
            DEM.dat(k, img);
        }
        g.veAnhThat(img, x - pad, y - pad);
        return true;
    }

    public static boolean khoi(Graphics g, int x, int y, int w, int h, int r, int mau) {
        if (w <= 0 || h <= 0) return true;
        if (mau != 0x0B1222 && mau != 0x0A0F2C) ghiNen(g, x, y, w, h, doSang(mau));
        if (veDem(g, mau == 0x0B1222 || mau == 0x0A0F2C ? D_BONG : D_KHOI, x, y, w, h, r, mau, 0)) return true;
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        float rad = Math.min(r, Math.min(w, h) / 2f);
        if (mau == 0x0B1222 || mau == 0x0A0F2C) {
            bong(g2, l, t - 1, l + w - 1, t + h - 2, rad, 0);   // "bóng đổ" kiểu jar -> bóng mềm
        } else {
            tron(g2, l, t, l + w, t + h, rad, argb(mau));
        }
        return xong(g, g2);
    }

    public static boolean khoiDoc(Graphics g, int x, int y, int w, int h, int r, int tren, int duoi) {
        if (w <= 0 || h <= 0) return true;
        ghiNen(g, x, y, w, h, (doSang(tren) + doSang(duoi)) / 2);
        if (veDem(g, D_DOC, x, y, w, h, r, tren, duoi)) return true;
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        float rad = Math.min(r, Math.min(w, h) / 2f);
        tronDoc(g2, l, t, l + w, t + h, rad, argb(tren), argb(duoi));
        return xong(g, g2);
    }

    /** Hộp thoại sáng (chữ tối): thẻ trắng ngà, bo 9, viền mảnh, bóng mềm. */
    public static boolean hop(Graphics g, int x, int y, int w, int h) {
        game.pokemon.GiaoDien.moClip(g);
        ghiKhung(g, x, y, w, h);
        ghiNen(g, x, y, w, h, 245);
        game.render.screen.Font.nenSang = true;
        if (veDem(g, D_THE, x, y, w, h, 9, 0, 0)) return true;
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        theSang(g2, l, t, l + w, t + h, 9);
        return xong(g, g2);
    }

    /** Thẻ sáng (GiaoDien.theSang / ChuDe.the) bo r: một ảnh dựng sẵn. */
    public static boolean theSang(Graphics g, int x, int y, int w, int h, int r) {
        ghiNen(g, x, y, w, h, 245);
        if (veDem(g, D_THE, x, y, w, h, r, 0, 0)) return true;
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        theSang(g2, l, t, l + w, t + h, r);
        return xong(g, g2);
    }

    /** Thẻ sáng 0.9.5 (ChuDe): bóng mềm, thân trắng, viền tối mảnh. */
    static void theSang(Graphics2D g, float l, float t, float r, float b, float rad) {
        // 0.9.6 (kiểu GO): bóng mềm rộng, thân trắng, KHÔNG viền tối - chỉ đường kẻ rất nhạt
        bong(g, l, t, r, b, rad, 1.5f);
        tronDoc(g, l, t, r, b, rad, argb(game.pokemon.ChuDe.NEN), argb(game.pokemon.ChuDe.NEN_DUOI));
        vienTron(g, l, t, r, b, rad, 0.6f, 0x1A000000 | game.pokemon.ChuDe.CHU);
    }

    /** Bảng tối kiểu kính: navy trong mờ, viền sáng mảnh, ánh trên, bóng đổ. */
    public static boolean bang(Graphics g, int x, int y, int w, int h) {
        game.pokemon.GiaoDien.moClip(g);
        ghiKhung(g, x, y, w, h);
        ghiNen(g, x, y, w, h, 245);
        game.render.screen.Font.nenSang = true;
        if (veDem(g, D_THE, x, y, w, h, 9, 0, 0)) return true;
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        theSang(g2, l, t, l + w, t + h, 9);
        return xong(g, g2);
    }

    static void bangKinh(Graphics2D g, float l, float t, float r, float b, float rad) {
        bong(g, l, t, r, b, rad, 1.5f);
        tronDoc(g, l, t, r, b, rad, KINH_TREN, KINH_DUOI);
        tronDoc(g, l + 1, t + 1, r - 1, t + Math.min(26, (b - t) * 0.4f), rad - 1, 0x26FFFFFF, 0x00FFFFFF);
        vienTron(g, l, t, r, b, rad, 1f, 0xFF0A1428);
        vienTron(g, l + 1, t + 1, r - 1, b - 1, rad - 1, 0.6f, 0x50A9C8FF);
    }

    public static boolean theMo(Graphics g, int x, int y, int w, int h) {
        game.pokemon.GiaoDien.moClip(g);
        ghiKhung(g, x, y, w, h);
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        bong(g2, l, t, l + w, t + h, 10, 1.5f);
        tronDoc(g2, l, t, l + w, t + h, 10, 0xF2FFFFFF, 0xEAF3F6FB);
        vienTron(g2, l, t, l + w, t + h, 10, 0.6f, 0x1A000000 | game.pokemon.ChuDe.CHU);
        ghiNen(g, x, y, w, h, 240);
        game.render.screen.Font.nenSang = true;
        return xong(g, g2);
    }

    public static boolean bangMo(Graphics g, int x, int y, int w, int h) {
        game.pokemon.GiaoDien.moClip(g);
        ghiKhung(g, x, y, w, h);
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        bong(g2, l, t, l + w, t + h, 10, 1.5f);
        tronDoc(g2, l, t, l + w, t + h, 10, 0xF2FFFFFF, 0xEAF3F6FB);
        vienTron(g2, l, t, l + w, t + h, 10, 0.6f, 0x1A000000 | game.pokemon.ChuDe.CHU);
        ghiNen(g, x, y, w, h, 240);
        game.render.screen.Font.nenSang = true;
        return xong(g, g2);
    }

    /** Dải tiêu đề đỏ 0.9.5: phẳng, Poké Ball nhỏ, tiêu đề chữ giao diện trắng (jar GiaoDien.dai vẽ chữ). */
    public static boolean dai(Graphics g, int x, int y, int w, int h, String tieuDe) {
        return false;   // jar GiaoDien.dai vẽ bằng khoiDoc / pokeBall / ChuDe.chu (đều HD)
    }

    public static boolean vienTab(Graphics g, int x, int y, int w, int h, boolean doiTab) {
        ghiNen(g, x, y, w, h, 110);
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        float rad = h / 2f;
        bong(g2, l, t, l + w, t + h, rad, 1f);
        int m = argb(doiTab ? game.pokemon.ChuDe.CHINH_TOI : game.pokemon.ChuDe.CHINH);
        tronDoc(g2, l, t, l + w, t + h, rad, pha(m, 0xFFFFFFFF, 0.15f), m);
        return xong(g, g2);
    }

    public static boolean chon(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        float rad = Math.min(6, h / 2f);
        ghiNen(g, x, y, w, h, 235);
        tronDoc(g2, l, t, l + w, t + h, rad, 0xFFF4FCFB, argb(game.pokemon.ChuDe.CHINH_NHAT));
        vienTron(g2, l, t, l + w, t + h, rad, 1.1f, argb(game.pokemon.ChuDe.CHINH));
        return xong(g, g2);
    }

    public static boolean chonSang(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        float rad = Math.min(6, h / 2f);
        ghiNen(g, x, y, w, h, 235);
        tronDoc(g2, l, t, l + w, t + h, rad, 0xFFF4FCFB, argb(game.pokemon.ChuDe.CHINH_NHAT));
        vienTron(g2, l, t, l + w, t + h, rad, 1.1f, argb(game.pokemon.ChuDe.CHINH));
        return xong(g, g2);
    }

    /** Thanh HP / PP / EXP: viên bo tròn, nền tối mờ, thân dải màu + ánh. */
    public static boolean thanh(Graphics g, int x, int y, int w, int h, long gt, long max, int loai) {
        if (w < 4 || h < 3) return true;
        if (gt < 0) gt = 0;
        if (max > 0 && gt > max) gt = max;
        Graphics2D g2 = bd(g);
        veThanh(g2, L(g, x), T(g, y), w, h, max <= 0 ? 0f : gt / (float) max, mauThanh(loai, gt, max));
        return xong(g, g2);
    }

    static int mauThanh(int loai, long gt, long max) {
        if (loai == 0) {
            long p = max <= 0 ? 100 : gt * 100 / max;
            return argb(p > 50 ? game.pokemon.ChuDe.HP_XANH : p > 20 ? game.pokemon.ChuDe.HP_VANG : game.pokemon.ChuDe.HP_DO);
        }
        return argb(loai == 1 ? game.pokemon.ChuDe.PP : game.pokemon.ChuDe.EXP);
    }

    static void veThanh(Graphics2D g, float l, float t, float w, float h, float tiLe, int mau) {
        float rad = h / 2f;
        tron(g, l, t, l + w, t + h, rad, 0xE0000000 | game.pokemon.ChuDe.RANH);
        float dai = (w - 1.2f) * Math.max(0f, Math.min(1f, tiLe));
        if (dai > 0) {
            if (dai < h - 1.2f) dai = Math.min(w - 1.2f, h - 1.2f);
            tronDoc(g, l + 0.6f, t + 0.6f, l + 0.6f + dai, t + h - 0.6f, rad - 0.6f, pha(mau, 0xFFFFFFFF, 0.35f), pha(mau, 0xFF000000, 0.12f));
            if (h >= 4) tron(g, l + 1.5f, t + 1f, l + 0.6f + dai - 1f, t + Math.max(1.6f, h * 0.32f), h * 0.2f, 0x50FFFFFF);
        }
        vienTron(g, l, t, l + w, t + h, rad, 0.5f, 0x22000000 | game.pokemon.ChuDe.CHU);
    }

    /** Viên (pill) màu: dải màu + viền + chữ (chữ HD). */
    public static boolean vien(Graphics g, int x, int y, int w, int h, int mau, String chu) {
        Graphics2D g2 = bd(g);
        float l = L(g, x), t = T(g, y);
        float rad = h / 2f;
        bong(g2, l, t, l + w, t + h, rad, 0.8f);
        // 0.9.6 (kiểu GO): viên màu đặc dải nhẹ, không ánh kính / viền tối
        tronDoc(g2, l, t, l + w, t + h, rad, pha(argb(mau), 0xFFFFFFFF, 0.18f), argb(mau));
        xong(g, g2);
        if (chu != null && game.pokemon.ChuUI.bo != null) {
            // chữ giao diện trắng đậm (không viền)
            game.pokemon.ChuDe.chuGiua(g, game.pokemon.ChuDe.cat(chu, game.pokemon.ChuDe.SO, w - 6), x + w / 2, y, h, game.pokemon.ChuDe.SO, 0xFFFFFF, 2);
        } else if (chu != null && game.render.screen.Font.borderFont != null) {
            game.render.screen.Font f = game.render.screen.Font.borderFont;
            f.drawString(g, chu, x + w / 2, y + (h - f.getHeight()) / 2 + 1, game.render.screen.Font.CENTER);
        }
        return true;
    }

    public static boolean muiTab(Graphics g, int x, int yTam, boolean trai) {
        Graphics2D g2 = bd(g);
        float cx = L(g, x), cy = T(g, yTam);
        float d = trai ? 1 : -1;
        float mx = cx + 4.5f * d;
        tron(g2, mx - 7, cy - 7, mx + 7, cy + 7, 7, 0xE01E2A44);
        tronDoc(g2, mx - 6, cy - 6, mx + 6, cy + 6, 6, 0xFFFFE066, 0xFFF0A800);
        Path2D.Float p = new Path2D.Float();
        p.moveTo(mx - 2.2f * d, cy);
        p.lineTo(mx + 2f * d, cy - 3.4f);
        p.lineTo(mx + 2f * d, cy + 3.4f);
        p.closePath();
        g2.setColor(c(VIEN));
        g2.fill(p);
        return xong(g, g2);
    }

    /** Poké Ball vector (dải màu đỏ, ánh sáng, đai, nút giữa). */
    public static boolean pokeBall(Graphics g, int cx, int cy, int r, int goc) {
        if (r < 3) r = 3;
        Graphics2D g2 = bd(g);
        veBong(g2, L(g, cx) + 0.5f, T(g, cy) + 0.5f, r, goc);
        return xong(g, g2);
    }

    static void veBong(Graphics2D g, float x, float y, float r, int goc) {
        g.setColor(c(VIEN));
        g.fill(new Ellipse2D.Float(x - r - 0.9f, y - r - 0.9f, 2 * r + 1.8f, 2 * r + 1.8f));
        java.awt.geom.AffineTransform at = g.getTransform();
        g.rotate(Math.toRadians(-goc), x, y);
        g.setPaint(new GradientPaint(0, y - r, c(0xFFFF6A47), 0, y, c(0xFFC8200A)));
        g.fill(new Arc2D.Float(x - r, y - r, 2 * r, 2 * r, 0, 180, Arc2D.PIE));
        g.setPaint(new GradientPaint(0, y, c(0xFFFFFFFF), 0, y + r, c(0xFFD5DCE6)));
        g.fill(new Arc2D.Float(x - r, y - r, 2 * r, 2 * r, 180, 180, Arc2D.PIE));
        g.setColor(c(0x70FFFFFF));
        g.fill(new Ellipse2D.Float(x - r * 0.38f - r * 0.22f, y - r * 0.45f - r * 0.22f, r * 0.44f, r * 0.44f));
        g.setColor(c(VIEN));
        float dai = Math.max(1.1f, r * 0.2f);
        g.fill(new Rectangle2D.Float(x - r, y - dai / 2, 2 * r, dai));
        float rb = Math.max(1.8f, r * 0.36f);
        g.fill(new Ellipse2D.Float(x - rb - 0.8f, y - rb - 0.8f, 2 * rb + 1.6f, 2 * rb + 1.6f));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(x - rb, y - rb, 2 * rb, 2 * rb));
        g.setColor(c(0xFFB4BDCA));
        g.setStroke(new BasicStroke(0.5f));
        g.draw(new Ellipse2D.Float(x - rb * 0.6f, y - rb * 0.6f, rb * 1.2f, rb * 1.2f));
        g.setTransform(at);
    }

    // ------------------------------------------------------------------ thay KieuO
    /** Phủ màu alpha (bản jar tạo ảnh RGB theo cỡ ô) -> tô thẳng màu ARGB. */
    public static boolean phuCat(Graphics g, int x, int y, int w, int hDu, int h, int argb) {
        if (w <= 0 || h <= 0 || hDu <= 0) return true;
        if (h > hDu) h = hDu;
        Graphics2D g2 = g.g2d();
        g2.setColor(c(argb));
        g2.fill(new Rectangle2D.Float(L(g, x), T(g, y), w, h));
        g.setColor(g.getColor());
        return true;
    }
}
