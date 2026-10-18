package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * Bộ vẽ giao diện kiểu Pokémon cho bản jar (thay khung "kiếm hiệp" vàng/ngọc/xanh rêu của game gốc).
 * Cùng bảng màu với web HTBB-Web: đỏ Poké #E3350D, vàng Pikachu #FFCB05, xanh #3D7DCA / navy #003A70,
 * xanh lá #78C850, viền tối #1E2A44. Vẽ hoàn toàn bằng lệnh Graphics (không thêm ảnh), chỉ số nguyên
 * (CLDC 1.0 không có float).
 *
 * Hai kiểu khung:
 *  - hop(): hộp thoại TRẮNG viền tối như hộp thoại Pokémon cổ điển - chữ TỐI (Font.arialFont / blackFont).
 *  - bang(): bảng thân XANH ĐẬM viền trắng + viền tối - cho các màn của game gốc vẽ chữ TRẮNG
 *    (Font.normalFont[0], arialFontW) để chữ vẫn đọc được.
 * Res.paintDlgFull / paintDlgDragonFull / paintSelected, Menu, MsgDlg, thanh phím mềm... gọi vào đây.
 */
public final class GiaoDien {
    public static final int DO = 0xE3350D;
    public static final int DO_SANG = 0xF4613A;
    public static final int DO_TOI = 0xA5240A;
    public static final int VANG = 0xFFCB05;
    public static final int VANG_NHAT = 0xFFE680;
    public static final int VANG_TOI = 0xB98A00;
    public static final int XANH = 0x3D7DCA;
    public static final int XANH_SANG = 0x6FA6EA;
    public static final int XANH_NHAT = 0xCFE0F5;
    public static final int NAVY = 0x003A70;
    public static final int LA = 0x78C850;
    public static final int LA_TOI = 0x3E8A30;
    public static final int VIEN = 0x1E2A44;
    public static final int TRANG = 0xFFFFFF;
    public static final int KEM = 0xF5F8FC;
    public static final int XAM = 0xB4BDCA;
    public static final int XAM_TOI = 0x5E6A80;
    /** thân bảng tối (chữ trắng đọc được) - gradient trên -> dưới */
    public static final int THAN_TREN = 0x2D62AA;
    public static final int THAN_DUOI = 0x1A3B6E;
    /** màu thanh máu kiểu Pokémon */
    public static final int HP_XANH = 0x48D058;
    public static final int HP_VANG = 0xF8C020;
    public static final int HP_DO = 0xF04830;
    public static final int MP_XANH = 0x3FA0F0;
    public static final int EXP_XANH = 0x40C8F8;

    /** độ thụt từng hàng của góc bo tròn bán kính r (1..8) */
    private static final byte[][] GOC = new byte[9][];
    /** sin(k * 15 độ) * 1024, k = 0..6 */
    private static final int[] SIN15 = {0, 265, 512, 724, 887, 989, 1024};

    static {
        for (int r = 1; r < GOC.length; r++) {
            GOC[r] = new byte[r];
            for (int i = 0; i < r; i++) {
                int d = r - i;
                GOC[r][i] = (byte) (r - canBac2(r * r - d * d));
            }
        }
    }

    private GiaoDien() {
    }

    /** Bỏ vùng cắt (cả màn hình) nhưng giữ gốc tọa độ đang dịch của người gọi. */
    public static void moClip(Graphics g) {
        g.setClip(-g.getTranslateX(), -g.getTranslateY(), Canvas.w, Canvas.h);
    }

    private static int canBac2(int n) {
        if (n <= 0) {
            return 0;
        }
        int x = 0;
        while ((x + 1) * (x + 1) <= n) {
            x++;
        }
        return x;
    }

    /** Pha màu a -> b theo t / n. */
    public static int pha(int a, int b, int t, int n) {
        if (n <= 0 || a == b) {
            return a;
        }
        if (t < 0) {
            t = 0;
        } else if (t > n) {
            t = n;
        }
        int r = (a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t / n;
        int gg = (a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t / n;
        int c = (a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t / n;
        return r << 16 | gg << 8 | c;
    }

    private static int thut(int r, int hang, int h) {
        // hang tính từ mép trên; hàng gần mép dưới đối xứng
        int i = hang < r ? hang : (hang >= h - r ? h - 1 - hang : -1);
        return i < 0 ? 0 : GOC[r][i];
    }

    private static int banKinh(int r, int w, int h) {
        if (r > w / 2) {
            r = w / 2;
        }
        if (r > h / 2) {
            r = h / 2;
        }
        if (r >= GOC.length) {
            r = GOC.length - 1;
        }
        return r < 0 ? 0 : r;
    }

    /** Khối chữ nhật bo góc tô một màu. */
    public static void khoi(Graphics g, int x, int y, int w, int h, int r, int mau) {
        if (w <= 0 || h <= 0) {
            return;
        }
        g.setColor(mau);
        r = banKinh(r, w, h);
        if (r <= 0) {
            g.fillRect(x, y, w, h);
            return;
        }
        g.fillRect(x, y + r, w, h - 2 * r);
        for (int i = 0; i < r; i++) {
            int t = GOC[r][i];
            g.fillRect(x + t, y + i, w - 2 * t, 1);
            g.fillRect(x + t, y + h - 1 - i, w - 2 * t, 1);
        }
    }

    /** Khối bo góc tô dải màu dọc tren -> duoi (vẽ từng dải 2 px). */
    public static void khoiDoc(Graphics g, int x, int y, int w, int h, int r, int tren, int duoi) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (tren == duoi) {
            khoi(g, x, y, w, h, r, tren);
            return;
        }
        r = banKinh(r, w, h);
        for (int i = 0; i < h; i += 2) {
            int t = r > 0 ? thut(r, i, h) : 0;
            int t2 = r > 0 && i + 1 < h ? thut(r, i + 1, h) : t;
            g.setColor(pha(tren, duoi, i, h));
            if (t == t2 && i + 1 < h) {
                g.fillRect(x + t, y + i, w - 2 * t, 2);
            } else {
                g.fillRect(x + t, y + i, w - 2 * t, 1);
                if (i + 1 < h) {
                    g.fillRect(x + t2, y + i + 1, w - 2 * t2, 1);
                }
            }
        }
    }

    /** Hộp thoại trắng viền tối (kiểu hộp thoại Pokémon) - chữ bên trong nên dùng font tối. */
    public static void hop(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        khoi(g, x + 1, y + 2, w, h, 6, 0x0B1222);          // bóng đổ
        khoi(g, x, y, w, h, 6, VIEN);
        khoi(g, x + 2, y + 2, w - 4, h - 4, 4, XANH_NHAT);
        khoi(g, x + 3, y + 3, w - 6, h - 6, 3, TRANG);
    }

    /** Bảng thân xanh đậm, viền trắng + viền tối - chữ trắng của game gốc đọc rõ. */
    public static void bang(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        khoi(g, x + 1, y + 2, w, h, 6, 0x0B1222);
        khoi(g, x, y, w, h, 6, VIEN);
        khoi(g, x + 2, y + 2, w - 4, h - 4, 4, TRANG);
        khoiDoc(g, x + 4, y + 4, w - 8, h - 8, 3, THAN_TREN, THAN_DUOI);
    }

    /**
     * Tiêu đề VIẾT HOA của game gốc ("TOP BANG HỘI") -> "Top bang hội": borderFont (chữ tiêu đề kiểu
     * Pokémon) thiếu nhiều chữ hoa có dấu (Ộ, Ữ...) nên chữ hoa có dấu bị mất nét.
     */
    public static String tieuDe(String s) {
        if (s == null || s.length() < 4 || s.indexOf(' ') < 0) {
            return s; // "PK", "HP", tên một từ: giữ
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (ChuPokemon.hoa(c) != c) {
                return s; // đã có chữ thường: giữ nguyên
            }
        }
        return s.substring(0, 1) + ChuPokemon.thuong(s.substring(1));
    }

    /** Thân bo góc mờ (alpha) w x h: giữa + 2 dải trên/dưới thụt góc 3 px. */
    private static void thanMo(Graphics g, int x, int y, int w, int h, int argb) {
        KieuO.phu(g, x + 3, y, w - 6, 3, argb);
        KieuO.phu(g, x, y + 3, w, h - 6, argb);
        KieuO.phu(g, x + 3, y + h - 3, w - 6, 3, argb);
    }

    /** Thẻ trắng mờ trên nền banner (đăng nhập / máy chủ): viền tối + viền trong trắng, chữ tối. */
    public static void theMo(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        thanMo(g, x + 1, y + 2, w, h, 0x400B1222);
        thanMo(g, x + 2, y + 2, w - 4, h - 4, 0xE0F7FAFF);
        g.setColor(VIEN);
        g.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
        g.setColor(TRANG);
        g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 8, 8);
    }

    /** Bảng xanh đậm mờ trên nền banner (chọn / tạo nhân vật): chữ trắng của game đọc rõ. */
    public static void bangMo(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        thanMo(g, x + 1, y + 2, w, h, 0x500B1222);
        thanMo(g, x + 3, y + 3, w - 6, h - 6, 0xD81A3B6E);
        g.setColor(VIEN);
        g.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
        g.setColor(TRANG);
        g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 8, 8);
        g.setColor(XANH_SANG);
        g.drawRoundRect(x + 2, y + 2, w - 5, h - 5, 6, 6);
    }

    /** Bảng có dải tiêu đề đỏ cao hTieuDe (tính từ mép trên khung). */
    public static void bangTieuDe(Graphics g, int x, int y, int w, int h, int hTieuDe, String tieuDe) {
        bang(g, x, y, w, h);
        dai(g, x + 4, y + 4, w - 8, hTieuDe - 6, tieuDe);
    }

    /** Dải tiêu đề đỏ bo góc + Poké Ball nhỏ bên trái + chữ trắng viền. */
    public static void dai(Graphics g, int x, int y, int w, int h, String tieuDe) {
        if (h < 6) {
            return;
        }
        khoiDoc(g, x, y, w, h, 3, DO_SANG, DO_TOI);
        g.setColor(0xFF9070);
        g.fillRect(x + 3, y + 1, w - 6, 1);
        g.setColor(VIEN);
        g.fillRect(x, y + h, w, 1);
        boolean coBong = h >= 12 && w > 60;
        if (tieuDe != null && tieuDe.length() > 0 && Font.borderFont != null) {
            tieuDe = tieuDe(tieuDe);
            int fh = Font.borderFont.getHeight();
            int fw = Font.borderFont.getWidth(tieuDe);
            int r = h / 2 - 2 > 6 ? 6 : h / 2 - 2;
            int choBong = 2 * r + 8;
            int xTam = x + w / 2;
            if (coBong && fw + 2 * choBong > w) {
                // chữ dài: dời chữ sang phải tránh Poké Ball; vẫn không đủ thì bỏ Poké Ball
                if (fw + choBong + 4 <= w) {
                    xTam = x + choBong + (w - choBong) / 2;
                } else {
                    coBong = false;
                }
            }
            Font.borderFont.drawString(g, tieuDe, xTam, y + (h - fh) / 2 + 1, Font.CENTER);
        }
        if (coBong) {
            int r = h / 2 - 2;
            if (r > 6) {
                r = 6;
            }
            pokeBall(g, x + 4 + r, y + h / 2, r, 0);
        }
    }

    /** Viên tên tab (giống bảng nhân vật): đỏ khi đang ở nội dung, xanh khi đang chọn tab. */
    public static void vienTab(Graphics g, int x, int y, int w, int h, boolean doiTab) {
        khoi(g, x, y, w, h, 6, VIEN);
        khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 5, doiTab ? XANH_SANG : DO_SANG, doiTab ? XANH : DO_TOI);
    }

    /** Mũi tên đổi tab vàng viền tối, đỉnh tại (x, yTam); trai = chỉ sang trái. */
    public static void muiTab(Graphics g, int x, int yTam, boolean trai) {
        int d = trai ? 1 : -1;
        g.setColor(VIEN);
        g.fillTriangle(x, yTam, x + 9 * d, yTam - 6, x + 9 * d, yTam + 6);
        g.setColor(VANG);
        g.fillTriangle(x + 2 * d, yTam, x + 8 * d, yTam - 4, x + 8 * d, yTam + 4);
    }

    /** Cắt chuỗi cho vừa bề ngang w (thêm ".."), dùng cho dòng mô tả trên máy hẹp. */
    public static String catChu(Font f, String s, int w) {
        if (s == null || f == null || f.getWidth(s) <= w) {
            return s;
        }
        int n = s.length();
        while (n > 1 && f.getWidth(s.substring(0, n) + "..") > w) {
            n--;
        }
        return s.substring(0, n) + "..";
    }

    /** Ô đang chọn trên nền tối: thân xanh sáng, viền vàng. */
    public static void chon(Graphics g, int x, int y, int w, int h) {
        khoi(g, x, y, w, h, 3, VANG);
        khoiDoc(g, x + 2, y + 2, w - 4, h - 4, 2, XANH_SANG, XANH);
    }

    /** Ô đang chọn trên nền trắng: vàng nhạt viền vàng đậm (chữ tối). */
    public static void chonSang(Graphics g, int x, int y, int w, int h) {
        khoi(g, x, y, w, h, 3, VANG_TOI);
        khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 2, VANG_NHAT, VANG);
    }

    /** Mũi tên chọn kiểu Pokémon (tam giác chỉ sang phải), đỉnh trái ở (x, y), cao h. */
    public static void muiTen(Graphics g, int x, int y, int h, int mau) {
        g.setColor(mau);
        g.fillTriangle(x, y, x, y + h, x + h / 2 + 1, y + h / 2);
    }

    /** Tam giác ▼ nhấp nháy ở góc hộp thoại (còn chữ / bấm để tiếp). */
    public static void muiXuong(Graphics g, int xTam, int y) {
        if (Canvas.gameTick % 10 < 6) {
            g.setColor(DO);
            g.fillTriangle(xTam - 4, y, xTam + 4, y, xTam, y + 4);
        }
    }

    public static int mauHp(long gt, long max) {
        if (max <= 0) {
            return HP_XANH;
        }
        long p = gt * 100 / max;
        return p > 50 ? HP_XANH : (p > 20 ? HP_VANG : HP_DO);
    }

    /** Thanh HP / MP / EXP: loai 0 = HP (xanh -> vàng -> đỏ), 1 = MP, 2 = EXP. */
    public static void thanh(Graphics g, int x, int y, int w, int h, long gt, long max, int loai) {
        if (w < 4 || h < 3) {
            return;
        }
        if (gt < 0) {
            gt = 0;
        }
        if (max > 0 && gt > max) {
            gt = max;
        }
        g.setColor(VIEN);
        g.fillRect(x, y + 1, w, h - 2);
        g.fillRect(x + 1, y, w - 2, h);
        g.setColor(0x3A4258);
        g.fillRect(x + 1, y + 1, w - 2, h - 2);
        int dai = max <= 0 ? 0 : (int) ((w - 2) * gt / max);
        if (gt > 0 && dai < 1) {
            dai = 1;
        }
        if (dai <= 0) {
            return;
        }
        int mau = loai == 0 ? mauHp(gt, max) : (loai == 1 ? MP_XANH : EXP_XANH);
        g.setColor(mau);
        g.fillRect(x + 1, y + 1, dai, h - 2);
        if (h >= 5) {
            g.setColor(pha(mau, TRANG, 1, 2));
            g.fillRect(x + 1, y + 1, dai, 1);
        }
    }

    private static int sin(int goc) {
        goc = ((goc % 360) + 360) % 360;
        int k = (goc + 7) / 15 % 24;
        int q = k / 6;
        int m = k % 6;
        switch (q) {
            case 0:
                return SIN15[m];
            case 1:
                return SIN15[6 - m];
            case 2:
                return -SIN15[m];
            default:
                return -SIN15[6 - m];
        }
    }

    private static int cos(int goc) {
        return sin(goc + 90);
    }

    /** Poké Ball tâm (cx, cy) bán kính r, quay goc độ (0 = đai ngang, nửa đỏ ở trên). */
    public static void pokeBall(Graphics g, int cx, int cy, int r, int goc) {
        if (r < 3) {
            r = 3;
        }
        int d = r * 2;
        g.setColor(VIEN);
        g.fillArc(cx - r - 1, cy - r - 1, d + 3, d + 3, 0, 360);
        g.setColor(DO);
        g.fillArc(cx - r, cy - r, d + 1, d + 1, goc, 180);
        g.setColor(TRANG);
        g.fillArc(cx - r, cy - r, d + 1, d + 1, goc + 180, 180);
        if (r >= 6) {
            // ánh sáng trên nửa đỏ
            int hx = cx + (cos(goc + 125) * (r * 5 / 10)) / 1024;
            int hy = cy - (sin(goc + 125) * (r * 5 / 10)) / 1024;
            g.setColor(DO_SANG);
            g.fillArc(hx - r / 4, hy - r / 4, r / 2 + 1, r / 2 + 1, 0, 360);
        }
        // đai đen qua tâm
        int c = cos(goc);
        int s = sin(goc);
        int ux = c * r / 1024;
        int uy = -s * r / 1024;
        int t = r >= 9 ? 2 : 1;
        int vx = s * t / 1024;
        int vy = c * t / 1024;
        if (vx == 0 && vy == 0) {
            vy = 1;
        }
        g.setColor(VIEN);
        g.fillTriangle(cx + ux + vx, cy + uy + vy, cx + ux - vx, cy + uy - vy, cx - ux - vx, cy - uy - vy);
        g.fillTriangle(cx + ux + vx, cy + uy + vy, cx - ux - vx, cy - uy - vy, cx - ux + vx, cy - uy + vy);
        g.drawLine(cx + ux, cy + uy, cx - ux, cy - uy);
        // nút giữa
        int rb = r / 3;
        if (rb < 2) {
            rb = 2;
        }
        g.setColor(VIEN);
        g.fillArc(cx - rb - 1, cy - rb - 1, rb * 2 + 3, rb * 2 + 3, 0, 360);
        g.setColor(TRANG);
        g.fillArc(cx - rb, cy - rb, rb * 2 + 1, rb * 2 + 1, 0, 360);
        if (rb >= 3) {
            g.setColor(XAM);
            g.drawArc(cx - rb + 1, cy - rb + 1, rb * 2 - 2, rb * 2 - 2, 0, 360);
        }
    }

    /** Poké Ball đang quay (chờ tải) - lắc qua lại rồi quay tròn như lúc bắt Pokémon. */
    public static void dangTai(Graphics g, int cx, int cy, int r) {
        int t = Canvas.gameTick % 40;
        int goc;
        if (t < 24) {
            goc = -t * 15;                       // quay một vòng
        } else {
            int l = t - 24;                       // lắc: 0, 15, 30, 15, 0, -15...
            int[] lac = {0, 15, 30, 15, 0, -15, -30, -15, 0, 15, 30, 15, 0, -15, -30, -15};
            goc = lac[l % lac.length];
        }
        g.setColor(0x0B1222);
        g.fillArc(cx - r + 2, cy + r - 1, r * 2 - 4, 4, 0, 360);   // bóng dưới bóng
        pokeBall(g, cx, cy, r, goc);
    }

    /** Thanh phím mềm (ảnh Res.imgCmdBar w x h): navy, mép trên đỏ - chữ trắng viền đọc rõ. */
    public static void thanhPhim(Graphics g, int w, int h) {
        g.setColor(VIEN);
        g.fillRect(0, 0, w, h);
        g.setColor(DO);
        g.fillRect(0, 3, w, 2);
        g.setColor(VIEN);
        g.fillRect(0, 5, w, 1);
        for (int i = 6; i < h; i++) {
            g.setColor(pha(NAVY, VIEN, i - 6, h - 6));
            g.fillRect(0, i, w, 1);
        }
        g.setColor(0x24508A);
        g.fillRect(0, 6, w, 1);
    }

    /** Viên thuốc trạng thái (pill): khối bo tròn màu mau, chữ trắng viền ở giữa. */
    public static void vien(Graphics g, int x, int y, int w, int h, int mau, String chu) {
        khoi(g, x, y, w, h, h / 2, VIEN);
        khoiDoc(g, x + 1, y + 1, w - 2, h - 2, (h - 2) / 2, pha(mau, TRANG, 1, 3), mau);
        if (chu != null && Font.borderFont != null) {
            Font.borderFont.drawString(g, chu, x + w / 2, y + (h - Font.borderFont.getHeight()) / 2 + 1, Font.CENTER);
        }
    }

    /** Màu huy hiệu hệ Pokémon theo chuỗi hệ ("Hệ Lửa", "Hệ Cỏ/Độc"...). */
    public static int mauHe(String he) {
        if (he == null) {
            return 0xA8A878;
        }
        if (he.indexOf("Lửa") >= 0) {
            return 0xF08030;
        }
        if (he.indexOf("Nước") >= 0) {
            return 0x6890F0;
        }
        if (he.indexOf("Cỏ") >= 0) {
            return 0x78C850;
        }
        if (he.indexOf("Điện") >= 0) {
            return 0xE8B800;
        }
        if (he.indexOf("Độc") >= 0) {
            return 0xA040A0;
        }
        return 0xA8A878;   // Thường
    }

    /** Huy hiệu hệ: chữ hệ (bỏ "Hệ ") trên viên màu hệ, trả về bề ngang đã vẽ. */
    public static int huyHieuHe(Graphics g, int x, int y, String he) {
        String chu = he == null ? "" : (he.startsWith("Hệ ") ? he.substring(3) : he);
        int w = Font.borderFont.getWidth(chu) + 12;
        vien(g, x, y, w, Font.borderFont.getHeight() + 3, mauHe(he), chu);
        return w;
    }

    /**
     * Nền trời + đồi + cỏ (màn tải, chưa có bản đồ): trời xanh dải màu, mây trắng, đồi xanh lá, cỏ.
     */
    public static void nenTroi(Graphics g, int w, int h) {
        moClip(g);
        int hTroi = h * 62 / 100;
        for (int i = 0; i < hTroi; i += 2) {
            g.setColor(pha(0x5FA8E8, 0xCDE8FA, i, hTroi));
            g.fillRect(0, i, w, 2);
        }
        // mây
        int dx = Canvas.gameTick / 3 % (w + 80);
        may(g, (dx + w / 5) % (w + 80) - 40, hTroi / 4, 0);
        may(g, (dx / 2 + w * 3 / 4) % (w + 80) - 40, hTroi / 2, 1);
        // đồi xa
        g.setColor(0x58A848);
        g.fillArc(-w / 4, hTroi - h / 10, w, h / 4, 0, 180);
        g.setColor(0x4E9A40);
        g.fillArc(w / 3, hTroi - h / 12, w, h / 4, 0, 180);
        // cỏ
        g.setColor(LA);
        g.fillRect(0, hTroi, w, h - hTroi);
        g.setColor(0x68B840);
        for (int y = hTroi + 6; y < h; y += 10) {
            for (int x = (y / 10 % 2) * 9; x < w; x += 18) {
                g.fillRect(x, y, 2, 4);
                g.fillRect(x + 3, y + 1, 2, 3);
            }
        }
        g.setColor(0x9AD870);
        g.fillRect(0, hTroi, w, 2);
    }

    private static void may(Graphics g, int x, int y, int kieu) {
        g.setColor(TRANG);
        int s = kieu == 0 ? 10 : 8;
        g.fillArc(x, y, s * 2, s * 2 - 4, 0, 360);
        g.fillArc(x + s, y - s / 2, s * 2 + 2, s * 2, 0, 360);
        g.fillArc(x + s * 2 + 2, y + 1, s * 2 - 2, s * 2 - 6, 0, 360);
        g.fillRect(x + s / 2, y + s, s * 3, s - 3);
    }

    /** Biển tên game: hộp trắng viền tối + dải đỏ phía trên (cho màn tiêu đề / chờ). */
    public static void bienTen(Graphics g, int x, int y, int w, int h) {
        hop(g, x, y, w, h);
        khoiDoc(g, x + 3, y + 3, w - 6, 20, 3, DO_SANG, DO);
        g.setColor(VIEN);
        g.fillRect(x + 3, y + 23, w - 6, 2);
        g.setColor(TRANG);
        g.fillRect(x + 3, y + 25, w - 6, 1);
    }
}
