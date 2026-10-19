package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Ô kiểu Pokémon dùng chung: ô chiêu theo màu hệ + hình biểu tượng chiêu, ô lưới hành trang /
 * rương / cửa hàng / trang bị (bo góc, có khe, sọc xen kẽ, ô đang chọn viền vàng dày), huy hiệu số
 * lượng, phím, viên hệ, lớp phủ mờ (hồi chiêu). Chỉ dùng MIDP 2.0 / CLDC 1.0 (không float):
 * phủ mờ bằng drawRGB có alpha, hình biểu tượng là ảnh 12x12 (chữ '#') tạo bằng createRGBImage.
 */
public final class KieuO {
    // ------------------------------------------------------------ hệ Pokémon
    /** Màu hệ (giống HTBB-Web MAU_HE), index = số hệ như server. */
    public static final int[] MAU_HE = {
        0xA8A878, 0xF08030, 0x6890F0, 0xF8D030, 0x78C850, 0x98D8D8, 0xC03028, 0xA040A0, 0xE0C068,
        0xA890F0, 0xF85888, 0xA8B820, 0xB8A038, 0x705898, 0x7038F8, 0x705848, 0xB8B8D0, 0xEE99AC
    };
    public static final String[] TEN_HE = {
        "Thường", "Lửa", "Nước", "Điện", "Cỏ", "Băng", "Giác đấu", "Độc", "Đất",
        "Bay", "Siêu linh", "Bọ", "Đá", "Ma", "Rồng", "Bóng tối", "Thép", "Tiên"
    };

    public static int mauHe(int he) {
        return he >= 0 && he < MAU_HE.length ? MAU_HE[he] : MAU_HE[0];
    }

    public static String tenHe(int he) {
        return he >= 0 && he < TEN_HE.length ? TEN_HE[he] : TEN_HE[0];
    }

    // ------------------------------------------------------------ hình biểu tượng chiêu
    public static final byte HINH_VA = 0;       // va chạm (tia tỏa)
    public static final byte HINH_VUOT = 1;     // vết cào
    public static final byte HINH_LUA = 2;      // ngọn lửa
    public static final byte HINH_NANH = 3;     // răng nanh
    public static final byte HINH_LOC = 4;      // lốc xoáy
    public static final byte HINH_MAY = 5;      // mây / khói
    public static final byte HINH_TANG = 6;     // tăng chỉ số (mũi tên kép)
    public static final byte HINH_TIA = 7;      // tia bắn
    public static final byte HINH_NO = 8;       // vụ nổ
    public static final byte HINH_ROI = 9;      // roi dây leo
    public static final byte HINH_LA = 10;      // lá
    public static final byte HINH_BOM = 11;     // quả cầu / bom
    public static final byte HINH_KHIEN = 12;   // khiên
    public static final byte HINH_MAT_TROI = 13;
    public static final byte HINH_NHANH = 14;   // tốc độ (»)
    public static final byte HINH_SAO = 15;
    public static final byte HINH_TIM = 16;
    public static final byte HINH_NGU = 17;     // chữ Z
    public static final byte HINH_CONG = 18;    // dấu cộng hồi phục
    public static final byte HINH_MA = 19;      // bóng ma
    public static final byte HINH_THEP = 20;    // đai ốc thép
    public static final byte HINH_GIOT = 21;    // giọt nước
    public static final byte HINH_BONG = 22;    // bong bóng
    public static final byte HINH_SONG = 23;    // sóng
    public static final byte HINH_SET = 24;     // tia sét
    public static final byte HINH_VONG = 25;    // vòng nạp năng lượng
    public static final byte HINH_LUOI = 26;    // lưới

    private static final String[] HINH = {
        // 0 va chạm
        ".....##....." + ".#...##...#." + "..#..##..#.." + "...#....#..." + "............" + "####....####"
        + "####....####" + "............" + "...#....#..." + "..#..##..#.." + ".#...##...#." + ".....##.....",
        // 1 vết cào
        "#...#...#..." + "##..##..##.." + ".#...#...#.." + ".##..##..##." + "..#...#...#." + "..##..##..##"
        + "...#...#...#" + "...#...#...#" + "....#...#..." + "....#...#..." + "............" + "............",
        // 2 lửa
        ".....#......" + ".....##....." + "....###....." + "....####..#." + "...#####.##." + "..########.."
        + ".##########." + ".####..####." + ".###....###." + ".###....###." + "..###..###.." + "...######...",
        // 3 nanh
        "............" + "############" + "############" + ".##########." + ".####..####." + ".####..####."
        + "..###..###.." + "..###..###.." + "..##....##.." + "...#....#..." + "...#....#..." + "............",
        // 4 lốc
        "############" + "............" + ".##########." + "............" + "..########.." + "............"
        + "...######..." + "............" + "....####...." + "............" + ".....###...." + "......##....",
        // 5 mây
        "............" + "............" + "............" + "....###....." + "...#####.##." + "..#########."
        + ".##########." + "############" + "############" + ".##########." + "............" + "............",
        // 6 tăng
        ".....##....." + "....####...." + "...######..." + "..###..###.." + ".###....###." + "###......###"
        + ".....##....." + "....####...." + "...######..." + "..###..###.." + ".###....###." + "###......###",
        // 7 tia
        "............" + "............" + "........###." + ".......#####" + "############" + "############"
        + "############" + ".......#####" + "........###." + "............" + "............" + "............",
        // 8 nổ
        "#....#.....#" + ".#..###...#." + ".##.####.##." + "..########.." + ".##########." + "############"
        + ".##########." + "..########.." + ".##########." + ".##.####.##." + ".#...###..#." + "#.....#....#",
        // 9 roi
        ".........###" + "........###." + ".......##..." + "......##...." + ".....##....." + "....##......"
        + "....##......" + ".....##....." + "......##...." + ".....##....." + "...###......" + ".###........",
        // 10 lá
        "..........##" + ".......#####" + ".....######." + "....#######." + "...######.#." + "..######.##."
        + "..#####.###." + ".#####.###.." + ".####.####.." + ".###.####..." + ".##.####...." + "##..........",
        // 11 bom
        "....####...." + "..########.." + ".##..######." + ".#...######." + "##..########" + "############"
        + "############" + "############" + ".##########." + ".##########." + "..########.." + "....####....",
        // 12 khiên
        ".##########." + "############" + "############" + "#####..#####" + "###......###" + "#####..#####"
        + "#####..#####" + ".##########." + ".##########." + "..########.." + "...######..." + ".....##.....",
        // 13 mặt trời
        ".....##....." + ".#...##...#." + "..#......#.." + "....####...." + "...######..." + "##.######.##"
        + "##.######.##" + "...######..." + "....####...." + "..#......#.." + ".#...##...#." + ".....##.....",
        // 14 nhanh
        "............" + "##....##...." + ".##....##..." + "..##....##.." + "...##....##." + "....##....##"
        + "....##....##" + "...##....##." + "..##....##.." + ".##....##..." + "##....##...." + "............",
        // 15 sao
        ".....##....." + ".....##....." + "....####...." + "....####...." + "############" + ".##########."
        + "..########.." + "...######..." + "...######..." + "..###..###.." + "..##....##.." + ".##......##.",
        // 16 tim
        "............" + ".###....###." + "#####..#####" + "############" + "############" + "############"
        + ".##########." + "..########.." + "...######..." + "....####...." + ".....##....." + "............",
        // 17 ngủ
        "............" + ".#########.." + ".#########.." + ".......##..." + "......##...." + ".....##....."
        + "....##......" + "...##......." + "..##........" + ".#########.." + ".#########.." + "............",
        // 18 cộng
        "............" + "....####...." + "....####...." + "....####...." + ".##########." + ".##########."
        + ".##########." + ".##########." + "....####...." + "....####...." + "....####...." + "............",
        // 19 ma
        "...######..." + "..########.." + ".##########." + ".##..##..##." + ".##..##..##." + ".##########."
        + ".##########." + ".##########." + ".##########." + ".##########." + ".##.####.##." + ".#...##...#.",
        // 20 thép
        "...######..." + "..########.." + ".##########." + "####....####" + "###......###" + "###......###"
        + "###......###" + "###......###" + "####....####" + ".##########." + "..########.." + "...######...",
        // 21 giọt
        ".....##....." + ".....##....." + "....####...." + "....####...." + "...######..." + "..########.."
        + ".##########." + ".###..#####." + ".##..######." + ".##########." + "..########.." + "...######...",
        // 22 bong bóng
        ".......####." + "......##..##" + "......#....#" + "......##..##" + ".###...####." + "##.##......."
        + "#...#......." + "##.##..###.." + ".###..##.##." + "......#...#." + "......##.##." + ".......###..",
        // 23 sóng
        "............" + "..##....##.." + ".####..####." + "##..####..##" + "#....##....#" + "............"
        + "..##....##.." + ".####..####." + "##..####..##" + "#....##....#" + "............" + "............",
        // 24 sét
        "......####.." + ".....####..." + "....####...." + "...####....." + "..#########." + ".#########.."
        + ".....####..." + "....####...." + "...###......" + "..##........" + ".##........." + "#...........",
        // 25 vòng
        "...######..." + "..##....##.." + ".##......##." + "##...##...##" + "#...####...#" + "#..######..#"
        + "#..######..#" + "#...####...#" + "##...##...##" + ".##......##." + "..##....##.." + "...######...",
        // 26 lưới
        "............" + ".#..#..#..#." + "############" + ".#..#..#..#." + ".#..#..#..#." + "############"
        + ".#..#..#..#." + ".#..#..#..#." + "############" + ".#..#..#..#." + "............" + "............"
    };

    private static final int CO_HINH = 12;
    /** Ảnh hình biểu tượng đã tạo: [hình * 2 + (phóng 2 lần ? 1 : 0)]. */
    private static final Image[] anhHinh = new Image[HINH.length * 2];

    private KieuO() {
    }

    /** Ảnh hình biểu tượng (chữ trắng viền tối), phong = 1 hoặc 2; null nếu lỗi. */
    public static Image anhHinh(int hinh, int phong) {
        if (hinh < 0 || hinh >= HINH.length) {
            hinh = 0;
        }
        int k = phong >= 2 ? 2 : 1;
        int i = hinh * 2 + (k - 1);
        if (anhHinh[i] == null) {
            try {
                String s = HINH[hinh];
                int c = CO_HINH * k + 2;
                int[] p = new int[c * c];
                for (int y = 0; y < CO_HINH; y++) {
                    for (int x = 0; x < CO_HINH; x++) {
                        if (s.charAt(y * CO_HINH + x) != '#') {
                            continue;
                        }
                        for (int dy = 0; dy < k; dy++) {
                            for (int dx = 0; dx < k; dx++) {
                                p[(1 + y * k + dy) * c + 1 + x * k + dx] = 0xFFFFFFFF;
                            }
                        }
                    }
                }
                // viền tối 1 px quanh nét trắng (đọc rõ trên mọi màu hệ)
                int[] q = new int[c * c];
                for (int y = 0; y < c; y++) {
                    for (int x = 0; x < c; x++) {
                        if (p[y * c + x] != 0) {
                            q[y * c + x] = p[y * c + x];
                            continue;
                        }
                        boolean ke = false;
                        for (int dy = -1; dy <= 1 && !ke; dy++) {
                            for (int dx = -1; dx <= 1; dx++) {
                                int xx = x + dx;
                                int yy = y + dy;
                                if (xx >= 0 && yy >= 0 && xx < c && yy < c && p[yy * c + xx] != 0) {
                                    ke = true;
                                    break;
                                }
                            }
                        }
                        if (ke) {
                            q[y * c + x] = 0xC0141C30;
                        }
                    }
                }
                anhHinh[i] = Image.createRGBImage(q, c, c, true);
            } catch (Throwable e) {
                return null;
            }
        }
        return anhHinh[i];
    }

    // ------------------------------------------------------------ phủ mờ (alpha)
    /**
     * Ảnh phủ mờ đã tạo (khóa "rộng x cao x màu"). Dùng ảnh createRGBImage + drawImage thay vì
     * drawRGB vì vài máy / MicroEmulator vẽ drawRGB bỏ qua gốc tọa độ đã translate.
     */
    private static final java.util.Hashtable anhMo = new java.util.Hashtable();

    private static Image anhPhu(int w, int h, int argb) {
        String k = new StringBuffer().append(w).append('x').append(h).append('x').append(argb).toString();
        Image a = (Image) anhMo.get(k);
        if (a == null) {
            if (anhMo.size() > 40) {
                anhMo.clear();
            }
            int[] p = new int[w * h];
            for (int i = 0; i < p.length; i++) {
                p[i] = argb;
            }
            a = Image.createRGBImage(p, w, h, true);
            anhMo.put(k, a);
        }
        return a;
    }

    /** Phủ màu argb (có alpha) lên vùng w x h. */
    public static void phu(Graphics g, int x, int y, int w, int h, int argb) {
        phuCat(g, x, y, w, h, h, argb);
    }

    /** Phủ mờ ô w x hDu nhưng chỉ phần trên cao h (thanh hồi chiêu) - ảnh theo cỡ ô, cắt bằng clip. */
    public static void phuCat(Graphics g, int x, int y, int w, int hDu, int h, int argb) {
        if (w <= 0 || h <= 0 || hDu <= 0) {
            return;
        }
        if (h > hDu) {
            h = hDu;
        }
        try {
            Image a = anhPhu(w, hDu, argb);
            int cx = g.getClipX();
            int cy = g.getClipY();
            int cw = g.getClipWidth();
            int ch = g.getClipHeight();
            g.clipRect(x, y, w, h);
            g.drawImage(a, x, y, Graphics.TOP | Graphics.LEFT);
            g.setClip(cx, cy, cw, ch);
        } catch (Throwable e) {
            // không tạo được ảnh alpha: tô sọc thưa thay mờ
            g.setColor(argb & 0xFFFFFF);
            for (int i = 0; i < h; i += 2) {
                g.fillRect(x, y + i, w, 1);
            }
        }
    }

    // ------------------------------------------------------------ ô chiêu
    /**
     * Ô chiêu w x h tại (x, y): viền tối, thân dải màu hệ, hình biểu tượng trắng ở giữa.
     * khoa = chưa học (xám).
     */
    public static void oChieu(Graphics g, int x, int y, int w, int h, int he, int hinh, boolean khoa) {
        int mau = khoa ? 0x8A93A3 : mauHe(he);
        GiaoDien.khoi(g, x, y, w, h, 4, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 3, GiaoDien.pha(mau, GiaoDien.TRANG, 2, 5), GiaoDien.pha(mau, 0x000000, 1, 4));
        if (w >= 10 && h >= 10) {
            g.setColor(GiaoDien.pha(mau, GiaoDien.TRANG, 3, 5));
            g.fillRect(x + 4, y + 2, w - 8, 1);
        }
        int k = w >= 30 && h >= 30 ? 2 : 1;
        Image a = anhHinh(hinh, k);
        if (a != null) {
            g.drawImage(a, x + w / 2, y + h / 2, Graphics.HCENTER | Graphics.VCENTER);
        }
        if (khoa) {
            phu(g, x + 1, y + 1, w - 2, h - 2, 0x70202838);
        }
    }

    /**
     * Ô chiêu sk của dòng lop: icon PNG riêng từng chiêu (bản PC / APK, AnhUI.oChieu) canh giữa ô w x h; J2ME / thiếu
     * ảnh -> ô màu hệ + hình 12x12 cũ.
     */
    public static void oChieuSk(Graphics g, int x, int y, int w, int h, int lop, int sk, boolean khoa) {
        int s = w < h ? w : h;
        int o = AnhUI.oChieu(lop, sk);
        if (o >= 0 && ChuUI.bo != null && ChuUI.bo.chieu(g, o, false, x + (w - s) / 2, y + (h - s) / 2, s, khoa)) {
            return;
        }
        oChieu(g, x, y, w, h, PokeChieu.he(lop, sk), PokeChieu.hinh(lop, sk), khoa);
    }

    /** Viên hệ (pill) màu hệ, chữ trắng; trả về bề ngang. */
    public static int vienHe(Graphics g, int x, int y, int he) {
        String chu = tenHe(he);
        Font f = Font.borderFont;
        int w = rongVienHe(he);
        int h = 15;
        GiaoDien.khoi(g, x, y, w, h, 7, GiaoDien.VIEN);
        int mau = mauHe(he);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 6, mau, GiaoDien.pha(mau, 0x000000, 2, 5));
        f.drawString(g, chu, x + w / 2, y + 1, Font.CENTER);
        return w;
    }

    public static int rongVienHe(int he) {
        return Font.borderFont.getWidth(tenHe(he)) + 10;
    }

    // ------------------------------------------------------------ huy hiệu
    /** Chuỗi số gọn: 1234 -> "1k", 25000 -> "25k". */
    public static String soGon(long n) {
        if (n >= 100000000L) {
            return String.valueOf(n / 1000000L) + "m";
        }
        if (n >= 10000L) {
            return String.valueOf(n / 1000L) + "k";
        }
        return String.valueOf(n);
    }

    /** Huy hiệu số lượng, góc dưới-phải tại (xPhai, yDuoi) - nền tối bo góc, số trắng. */
    public static void soLuong(Graphics g, int xPhai, int yDuoi, String s) {
        if (s == null || s.length() == 0) {
            return;
        }
        Font f = Font.smallFontColor[0];
        int w = f.getWidth(s) + 4;
        int h = 9;
        GiaoDien.khoi(g, xPhai - w, yDuoi - h, w, h, 2, 0x0B1222);
        f.drawString(g, s, xPhai - w + 2, yDuoi - h + 1, Font.LEFT);
    }

    /** Nhãn phím nhỏ (góc trên-trái ô): nền vàng, số tối. */
    public static void nhanPhim(Graphics g, int x, int y, String s) {
        Font f = Font.smallFontColor[0];
        int w = f.getWidth(s) + 6;
        int h = 10;
        GiaoDien.khoi(g, x, y, w, h, 3, GiaoDien.VIEN);
        GiaoDien.khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 2, GiaoDien.DO_SANG, GiaoDien.DO_TOI);
        f.drawString(g, s, x + 3, y + 1, Font.LEFT);
    }

    // ------------------------------------------------------------ ô lưới đồ
    /** Nền ô (chẵn/lẻ xen kẽ) */
    public static final int O_CHAN = 0x28397E;
    public static final int O_LE = 0x213070;
    public static final int O_TRONG = 0x1A2658;

    /**
     * Ô lưới đồ s x s tại (x, y) (gồm khe 1 px bên phải/dưới): co = có đồ, le = sọc xen kẽ.
     */
    public static void oDo(Graphics g, int x, int y, int s, boolean co, boolean le) {
        oDoMau(g, x, y, s, co, co ? (le ? O_LE : O_CHAN) : O_TRONG);
    }

    /** Ô rương (nâu) - phân biệt với ô hành trang khi hai lưới cạnh nhau. */
    public static void oRuong(Graphics g, int x, int y, int s, boolean co, boolean le) {
        oDoMau(g, x, y, s, co, co ? (le ? 0x6A4A1E : 0x7A5726) : 0x4A3414);
    }

    public static void oDoMau(Graphics g, int x, int y, int s, boolean co, int nen) {
        int c = s - 1;
        GiaoDien.khoi(g, x, y, c, c, 3, 0x0E1A30);
        GiaoDien.khoi(g, x + 1, y + 1, c - 2, c - 2, 2, nen);
        if (co) {
            g.setColor(GiaoDien.pha(nen, GiaoDien.TRANG, 1, 5));
            g.fillRect(x + 3, y + 1, c - 6, 1);
        } else {
            // ô trống: chấm nhỏ ở giữa cho thấy chỗ trống
            g.setColor(0x2C4A78);
            g.fillRect(x + c / 2 - 1, y + c / 2 - 1, 2, 2);
        }
    }

    /**
     * Lưới đồ: numW cột, hàng tu..den-1, ô s x s; soCo = số ô có đồ (đếm từ ô 0), chon = ô đang
     * chọn (-1: không) - vẽ nền sáng cho ô chọn. Gọi chonO SAU khi vẽ đồ để có viền vàng.
     */
    public static void luoi(Graphics g, int numW, int tu, int den, int s, int soCo, int chon) {
        for (int i = tu; i < den; i++) {
            for (int j = 0; j < numW; j++) {
                int o = i * numW + j;
                oDo(g, j * s, i * s, s, o < soCo, ((i + j) & 1) == 1);
                if (o == chon) {
                    nenChon(g, j * s, i * s, s);
                }
            }
        }
    }

    /** Viền chọn vàng dày 2 px quanh ô s x s tại (x, y) (vẽ SAU đồ). Nhấp nháy nhẹ. */
    public static void chonO(Graphics g, int x, int y, int s) {
        int c = s - 1;
        int mau = Canvas.gameTick % 10 < 5 ? GiaoDien.VANG : 0xFFE24A;
        g.setColor(GiaoDien.VIEN);
        g.drawRect(x - 2, y - 2, c + 3, c + 3);
        g.setColor(mau);
        g.drawRect(x - 1, y - 1, c + 1, c + 1);
        g.drawRect(x, y, c - 1, c - 1);
        g.setColor(GiaoDien.TRANG);
        g.fillRect(x + 1, y + 1, 2, 1);
        g.fillRect(x + 1, y + 1, 1, 2);
    }

    /**
     * Ảnh ô lưới s x s (thay ảnh "grid" kiếm hiệp cho mọi chỗ còn vẽ Res.imgGrid): ô bo góc
     * (s-1) x (s-1) viền tối, thân xanh, vệt sáng trên; góc + khe trong suốt. null nếu lỗi.
     */
    public static Image anhO(int s) {
        try {
            int c = s - 1;
            int r = 3;
            int[] p = new int[s * s];
            for (int y = 0; y < c; y++) {
                for (int x = 0; x < c; x++) {
                    int dx = x < r ? r - x : (x > c - 1 - r ? x - (c - 1 - r) : 0);
                    int dy = y < r ? r - y : (y > c - 1 - r ? y - (c - 1 - r) : 0);
                    int d = dx * dx + dy * dy;
                    if (d > r * r + 1) {
                        continue;
                    }
                    boolean vien = x == 0 || y == 0 || x == c - 1 || y == c - 1 || d >= (r - 1) * (r - 1) + 1 && (dx > 0 && dy > 0);
                    // thân trong mờ: ô chọn tô phía dưới (code gốc tô trước rồi vẽ ô) vẫn thấy
                    p[y * s + x] = vien ? 0xFF0E1A30 : (y == 1 ? 0x80FFFFFF : 0x50000000);
                }
            }
            return Image.createRGBImage(p, s, s, true);
        } catch (Throwable e) {
            return null;
        }
    }

    /** Nền sáng cho ô đang chọn (vẽ TRƯỚC đồ). */
    public static void nenChon(Graphics g, int x, int y, int s) {
        int c = s - 1;
        GiaoDien.khoiDoc(g, x + 1, y + 1, c - 2, c - 2, 2, GiaoDien.XANH_SANG, GiaoDien.XANH);
    }

    /** Màu khung theo độ hiếm (colorName của đồ: 0 thường ... ). */
    public static int mauHiem(int c) {
        switch (c) {
            case 1:
                return 0x48D058;   // xanh lá
            case 2:
                return 0x3FA0F0;   // xanh dương
            case 3:
                return 0xC060F0;   // tím
            case 4:
                return 0xFFCB05;   // vàng
            case 5:
                return 0xF04830;   // đỏ
            default:
                return -1;
        }
    }

    /** Khung độ hiếm 1 px bên trong ô s x s. */
    public static void khungHiem(Graphics g, int x, int y, int s, int mau) {
        if (mau < 0) {
            return;
        }
        int c = s - 1;
        g.setColor(mau);
        g.drawRect(x + 1, y + 1, c - 3, c - 3);
    }

    /** "+N" cấp cường hóa góc trên-trái. */
    public static void capCuong(Graphics g, int x, int y, int n) {
        if (n <= 0) {
            return;
        }
        String s = "+" + n;
        Font f = Font.smallFontColor[1];
        int w = f.getWidth(s) + 2;
        GiaoDien.khoi(g, x, y, w, 8, 2, 0x0B1222);
        f.drawString(g, s, x + 1, y, Font.LEFT);
    }

    /** Thanh cuộn dọc mảnh: tổng tong, đang thấy tu..tu+thay. */
    public static void thanhCuon(Graphics g, int x, int y, int h, int tong, int tu, int thay) {
        if (tong <= thay || tong <= 0 || h < 8) {
            return;
        }
        GiaoDien.khoi(g, x, y, 3, h, 1, 0x0E1A30);
        int dai = h * thay / tong;
        if (dai < 6) {
            dai = 6;
        }
        int vt = (h - dai) * tu / (tong - thay);
        if (vt < 0) {
            vt = 0;
        }
        if (vt > h - dai) {
            vt = h - dai;
        }
        GiaoDien.khoi(g, x, y + vt, 3, dai, 1, GiaoDien.VANG);
    }

    /** Chấm trang: n trang, đang ở trang i, căn giữa tại xTam. */
    public static void chamTrang(Graphics g, int xTam, int y, int n, int i) {
        if (n <= 1) {
            return;
        }
        if (n > 12) {
            n = 12;
        }
        int x = xTam - (n * 6 - 2) / 2;
        for (int k = 0; k < n; k++) {
            g.setColor(GiaoDien.VIEN);
            g.fillArc(x + k * 6 - 1, y - 1, 6, 6, 0, 360);
            g.setColor(k == i ? GiaoDien.VANG : 0x5E7AA8);
            g.fillArc(x + k * 6, y, 4, 4, 0, 360);
        }
    }
}
