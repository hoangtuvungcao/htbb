package game.pokemon;

import game.model.IAction;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * TIỀN TỆ (jar / APK / PC dùng chung): MỘT biểu tượng cho mỗi loại tiền + cách viết số kiểu Việt Nam, thay chữ viết tắt
 * khó hiểu cũ ("$" = xu, "l" / "lg" = lượng, "lk" = lượng khoá).
 *
 * - Biểu tượng: xu = đồng Poké-xu vàng có huy hiệu Poké Ball; lượng = viên ngọc xanh; lượng khoá = ngọc xám xanh có
 *   ổ khoá nhỏ; điểm sự kiện = vé sao; điểm PvP (vinh dự) = huy chương. Bản HD (APK / PC: có PhongVe) vẽ VECTOR ở
 *   độ phân giải thật (nét ở mọi cỡ); bản J2ME dùng ảnh pixel /tien/<ten>_<16|24|32>.png (client_jar/res/tien, sinh
 *   bởi client_jar/tools/tao_icon_tien.py từ CÙNG thiết kế), thiếu ảnh thì vẽ vector.
 * - Số: so(1250000) = "1.250.000"; gon(1250000) = "1,25 Tr", gon(12500) = "12,5 N" (thẻ / HUD chật), số đầy đủ khi
 *   chạm (hienGoiY).
 * - Chạm biểu tượng (veGiaCham / dangKyGoiY, qua VungCham) -> chú thích nhỏ "Xu: tiền chơi, kiếm từ quái / nhiệm vụ"
 *   kèm số đầy đủ, tự tắt sau ~3 giây; Canvas.paint gọi veGoiY(g) sau lớp thông báo.
 * - Chữ thuần (chat, tin máy chủ, hộp thoại chữ): dùng ten(loai) = tên đầy đủ "xu" / "lượng" / "lượng khoá".
 *
 * CLDC 1.1: không generics / autoboxing.
 */
public final class TienTe {
    public static final int XU = 0;
    public static final int LUONG = 1;
    public static final int LUONG_KHOA = 2;
    /** điểm đổi thưởng sự kiện (diemDoiThuong) */
    public static final int DIEM_SU_KIEN = 3;
    /** điểm vinh dự PvP (honor) */
    public static final int VINH_DU = 4;
    public static final int SO_LOAI = 5;

    private static final String[] TEN = {"xu", "lượng", "lượng khoá", "điểm sự kiện", "vinh dự"};
    private static final String[] TEN_HOA = {"Xu", "Lượng", "Lượng khoá", "Điểm sự kiện", "Vinh dự"};
    private static final String[] GOI_Y = {
        "tiền chơi, kiếm từ quái / nhiệm vụ / bán đồ",
        "tiền nạp / thưởng, mua đồ quý, giao dịch được",
        "lượng thưởng, không giao dịch được",
        "đổi quà trong sự kiện đang diễn ra",
        "kiếm từ PvP / sàn đấu, đổi quà vinh dự"};
    private static final String[] ANH = {"xu", "luong", "luong_khoa", "su_kien", "vinh_du"};
    /** cỡ ảnh /tien/<ten>_<co>.png (0.9.7: thêm 8 / 12 - hàng ví thẻ HUD) */
    private static final int[] CO = {8, 12, 16, 24, 32};

    private static Image[][] anh;
    private static boolean daNap;

    private TienTe() {
    }

    // ------------------------------------------------------------------ chữ / số

    /** Tên đầy đủ (viết thường) để ghép vào chữ thuần: "xu", "lượng", "lượng khoá". */
    public static String ten(int loai) {
        return loai >= 0 && loai < SO_LOAI ? TEN[loai] : "";
    }

    /** Tên viết hoa đầu (đầu câu / chú thích). */
    public static String tenHoa(int loai) {
        return loai >= 0 && loai < SO_LOAI ? TEN_HOA[loai] : "";
    }

    /** Chú thích ngắn khi chạm biểu tượng: "Xu: tiền chơi, kiếm từ quái / nhiệm vụ / bán đồ". */
    public static String goiY(int loai) {
        if (loai < 0 || loai >= SO_LOAI) {
            return "";
        }
        return TEN_HOA[loai] + ": " + GOI_Y[loai];
    }

    /** Số đầy đủ kiểu Việt Nam: 1250000 -> "1.250.000", -5300 -> "-5.300". */
    public static String so(long v) {
        boolean am = v < 0;
        String s = String.valueOf(am ? -v : v);
        if (s.startsWith("-")) {
            // Long.MIN_VALUE
            return String.valueOf(v);
        }
        StringBuffer b = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            if (i > 0 && (n - i) % 3 == 0) {
                b.append('.');
            }
            b.append(s.charAt(i));
        }
        return am ? "-" + b.toString() : b.toString();
    }

    /**
     * Số gọn cho chỗ chật (HUD / thẻ): < 10.000 viết đủ ("9.999"), nghìn = "N", triệu = "Tr", tỉ = "Tỉ", tối đa 3 chữ số
     * có nghĩa, dấu phẩy thập phân, bỏ số 0 thừa: 12500 -> "12,5 N", 1250000 -> "1,25 Tr", 250000000 -> "250 Tr".
     */
    public static String gon(long v) {
        boolean am = v < 0;
        long a = am ? -v : v;
        if (a < 0) {
            return String.valueOf(v);
        }
        String r;
        if (a < 10000) {
            r = so(a);
        } else if (a < 1000000L) {
            r = phan(a, 1000L) + " N";
        } else if (a < 1000000000L) {
            r = phan(a, 1000000L) + " Tr";
        } else {
            r = phan(a, 1000000000L) + " Tỉ";
        }
        return am ? "-" + r : r;
    }

    /** a / don vi với 3 chữ số có nghĩa (cắt, không làm tròn lên - không bao giờ hiện nhiều hơn số thật). */
    private static String phan(long a, long donVi) {
        long nguyen = a / donVi;
        int le;
        long du = a % donVi;
        if (nguyen >= 100) {
            return so(nguyen);
        }
        if (nguyen >= 10) {
            le = (int) (du * 10 / donVi);
            return le == 0 ? String.valueOf(nguyen) : nguyen + "," + le;
        }
        le = (int) (du * 100 / donVi);
        if (le == 0) {
            return String.valueOf(nguyen);
        }
        if (le % 10 == 0) {
            return nguyen + "," + (le / 10);
        }
        return nguyen + "," + (le < 10 ? "0" : "") + le;
    }

    /** Chữ thuần (hộp thoại / mô tả / chat): "1.250.000 xu", "5 lượng", "20 lượng khoá". */
    public static String chu(int loai, long v) {
        return so(v) + " " + ten(loai);
    }

    /** Giá hai loại (bỏ loại = 0): "1.000 xu - 5 lượng"; cả hai 0 -> "". */
    public static String chuGia(long xu, long luong) {
        StringBuffer b = new StringBuffer();
        if (xu > 0) {
            b.append(chu(XU, xu));
        }
        if (luong > 0) {
            if (b.length() > 0) {
                b.append(" - ");
            }
            b.append(chu(LUONG, luong));
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ vẽ biểu tượng

    private static void nap() {
        if (daNap) {
            return;
        }
        daNap = true;
        anh = new Image[SO_LOAI][CO.length];
        for (int i = 0; i < SO_LOAI; i++) {
            for (int j = 0; j < CO.length; j++) {
                try {
                    anh[i][j] = Image.createImage("/tien/" + ANH[i] + "_" + CO[j] + ".png");
                } catch (Throwable e) {
                    anh[i][j] = null;
                }
            }
        }
    }

    /** Bề ngang biểu tượng cỡ co (px logic). */
    public static int rong(int co) {
        return co < 8 ? 8 : co;
    }

    /** Vẽ biểu tượng loại tiền, góc trái trên (x, y), cỡ co px logic (16 / 24 / 32; cỡ khác: vector đúng cỡ). */
    public static void ve(Graphics g, int loai, int x, int y, int co) {
        if (co < 8) {
            co = 8;
        }
        // 0.9.7 (quy tắc: icon = ảnh PNG): mọi bản dùng ảnh pixel /tien/<ten>_<16|24|32>.png cỡ gần nhất, canh giữa ô
        nap();
        // 0.9.7: ảnh LỚN NHẤT không vượt ô (trước: < 24 luôn ảnh 16 -> tràn ô 9 px của hàng ví, đè thanh EXP)
        int j = 0;
        for (int k = 0; k < CO.length; k++) {
            if (CO[k] <= co) {
                j = k;
            }
        }
        Image a = loai >= 0 && loai < SO_LOAI ? anh[loai][j] : null;
        if (a != null) {
            int d = (co - CO[j]) / 2;
            g.drawImage(a, x + d, y + d, Graphics.TOP | Graphics.LEFT);
            if (GhiChu.bat) GhiChu.bieu(g, "tien-" + ANH[loai], x + d, y + d, CO[j], CO[j]);
            return;
        }
        veVector(g, loai, x, y, co);
    }

    /** Thiết kế vector chung (ảnh pixel /tien/*.png sinh từ đúng hình này). */
    static void veVector(Graphics g, int loai, int x, int y, int s) {
        VeTay.ghi("tien vector");
        switch (loai) {
            case XU:
                veXu(g, x, y, s);
                break;
            case LUONG:
                veNgoc(g, x, y, s, 0x0E3A78, 0x2F8FE6, 0x8FDBFF, 0xE6F8FF);
                break;
            case LUONG_KHOA:
                veNgoc(g, x, y, s, 0x2E3F57, 0x6E86A6, 0xB4C6DC, 0xEEF3FA);
                veKhoa(g, x + s * 9 / 20, y + s * 9 / 20, s * 11 / 20);
                break;
            case DIEM_SU_KIEN:
                veVe(g, x, y, s);
                break;
            case VINH_DU:
                veHuyChuong(g, x, y, s);
                break;
            default:
                g.setColor(0x9AA4B5);
                g.fillArc(x, y, s, s, 0, 360);
                break;
        }
    }

    /** Đồng Poké-xu: viền vàng sẫm, mặt vàng, vành sáng, huy hiệu Poké Ball dập nổi giữa đồng. */
    private static void veXu(Graphics g, int x, int y, int s) {
        g.setColor(0x7A4A00);
        g.fillArc(x, y, s, s, 0, 360);
        int v = s / 12 + 1;
        g.setColor(0xF2B11E);
        g.fillArc(x + v, y + v, s - 2 * v, s - 2 * v, 0, 360);
        g.setColor(0xFFDB63);
        g.fillArc(x + v, y + v, s - 2 * v, s - 2 * v, 60, 150);
        int r = s * 9 / 40;
        int cx = x + s / 2, cy = y + s / 2;
        // huy hiệu Poké Ball dập nổi (tông vàng): vòng sẫm, nửa trên vàng sẫm, nửa dưới vàng nhạt, đai + nút giữa
        g.setColor(0x7A4A00);
        g.fillArc(cx - r - 1, cy - r - 1, 2 * r + 2, 2 * r + 2, 0, 360);
        g.setColor(0xD08A10);
        g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 0, 180);
        g.setColor(0xFFE58A);
        g.fillArc(cx - r, cy - r, 2 * r, 2 * r, 180, 180);
        int d = s / 16 + 1;
        g.setColor(0x7A4A00);
        g.fillRect(cx - r, cy - d / 2, 2 * r, d);
        int n = r * 2 / 3 + 1;
        g.fillArc(cx - n / 2 - 1, cy - n / 2 - 1, n + 2, n + 2, 0, 360);
        g.setColor(0xFFF8E0);
        g.fillArc(cx - n / 2, cy - n / 2, n, n, 0, 360);
    }

    /** Viên ngọc cắt mặt: mép trên phẳng, hai vai, đáy nhọn; mặt trên sáng, hai mặt bên đậm dần, vệt lấp lánh. */
    private static void veNgoc(Graphics g, int x, int y, int s, int vien, int dam, int sang, int lap) {
        int t = y + s * 3 / 20, vai = y + s * 8 / 20, day = y + s * 19 / 20;
        int l = x + s / 20, r = x + s * 19 / 20;
        int tl = x + s * 6 / 20, tr = x + s * 14 / 20, cx = x + s / 2;
        // viền: vẽ hình to hơn 1 px bằng màu viền
        g.setColor(vien);
        g.fillTriangle(tl, t - 1, tr, t - 1, r + 1, vai);
        g.fillTriangle(tl, t - 1, l - 1, vai, r + 1, vai);
        g.fillTriangle(l - 1, vai, r + 1, vai, cx, day + 1);
        // thân
        g.setColor(dam);
        g.fillTriangle(l + 1, vai, r - 1, vai, cx, day - 1);
        // mặt trên (sáng)
        g.setColor(sang);
        g.fillTriangle(tl + 1, t, tr - 1, t, r - 1, vai);
        g.fillTriangle(tl + 1, t, l + 1, vai, r - 1, vai);
        // mặt giữa dưới (sáng vừa)
        g.setColor(pha(dam, sang));
        g.fillTriangle(tl + s / 20, vai, tr - s / 20, vai, cx, day - 2);
        // lấp lánh
        g.setColor(lap);
        g.fillTriangle(tl + 1, t + 1, cx, t + 1, tl + s / 10, vai - 1);
    }

    /** Ổ khoá nhỏ (huy hiệu góc phải dưới của lượng khoá): quai bạc + thân vàng sẫm + lỗ khoá. */
    private static void veKhoa(Graphics g, int x, int y, int s) {
        int bw = s * 7 / 10, bh = s * 5 / 10;
        int bx = x + (s - bw) / 2, by = y + s - bh;
        int qw = bw * 6 / 10, qh = s * 6 / 10;
        int qx = bx + (bw - qw) / 2;
        int day = s / 8 + 1;
        g.setColor(0x1D2633);
        g.fillArc(qx - 1, by - qh / 2 - 1, qw + 2, qh + 2, 0, 180);
        g.setColor(0xD9DEE6);
        g.fillArc(qx, by - qh / 2, qw, qh, 0, 180);
        g.setColor(0x1D2633);
        g.fillArc(qx + day, by - qh / 2 + day, qw - 2 * day, qh - 2 * day, 0, 180);
        g.fillRect(bx - 1, by - 1, bw + 2, bh + 2);
        g.setColor(0xF2B11E);
        g.fillRect(bx, by, bw, bh);
        g.setColor(0xFFDB63);
        g.fillRect(bx, by, bw, bh / 3 > 0 ? bh / 3 : 1);
        g.setColor(0x5A3A00);
        int k = bw / 5 + 1;
        g.fillRect(bx + bw / 2 - k / 2, by + bh / 3, k, bh / 2 > 0 ? bh / 2 : 1);
    }

    /** Vé sự kiện: vé cam có sao vàng. */
    private static void veVe(Graphics g, int x, int y, int s) {
        int t = y + s / 5, h = s * 3 / 5;
        g.setColor(0x7A2E00);
        g.fillRect(x, t - 1, s, h + 2);
        g.setColor(0xFF8A2A);
        g.fillRect(x + 1, t, s - 2, h);
        g.setColor(0xFFB566);
        g.fillRect(x + 1, t, s - 2, h / 3);
        // lỗ bấm hai bên
        g.setColor(0x7A2E00);
        g.fillArc(x - s / 10, y + s / 2 - s / 10, s / 5, s / 5, 0, 360);
        g.fillArc(x + s - s / 10, y + s / 2 - s / 10, s / 5, s / 5, 0, 360);
        sao(g, x + s / 2, y + s / 2, s * 2 / 7, 0xFFE14D);
    }

    /** Huy chương vinh dự: dải đỏ + đồng bạc có sao. */
    private static void veHuyChuong(Graphics g, int x, int y, int s) {
        g.setColor(0xB3261E);
        g.fillTriangle(x + s / 4, y, x + s / 2, y, x + s / 2 - 1, y + s / 2);
        g.setColor(0x2B5DBF);
        g.fillTriangle(x + s / 2, y, x + s * 3 / 4, y, x + s / 2 + 1, y + s / 2);
        int d = s * 13 / 20;
        int cx = x + s / 2, cy = y + s - d / 2 - 1;
        g.setColor(0x4A5466);
        g.fillArc(cx - d / 2 - 1, cy - d / 2 - 1, d + 2, d + 2, 0, 360);
        g.setColor(0xD9DEE6);
        g.fillArc(cx - d / 2, cy - d / 2, d, d, 0, 360);
        sao(g, cx, cy, d * 2 / 5, 0xF2B11E);
    }

    /** Sao 5 cánh (3 tam giác chồng). */
    private static void sao(Graphics g, int cx, int cy, int r, int mau) {
        g.setColor(mau);
        int c = r * 31 / 100, s2 = r * 95 / 100, c2 = r * 81 / 100, s3 = r * 59 / 100;
        // đỉnh trên + hai chân dưới
        g.fillTriangle(cx, cy - r, cx - s3, cy + c2, cx + s3 / 3, cy + c / 3);
        g.fillTriangle(cx, cy - r, cx + s3, cy + c2, cx - s3 / 3, cy + c / 3);
        // hai cánh ngang
        g.fillTriangle(cx - s2, cy - c, cx + s2, cy - c, cx, cy + c);
    }

    private static int pha(int a, int b) {
        int r = ((a >> 16 & 255) + (b >> 16 & 255)) / 2;
        int gg = ((a >> 8 & 255) + (b >> 8 & 255)) / 2;
        int bb = ((a & 255) + (b & 255)) / 2;
        return r << 16 | gg << 8 | bb;
    }

    // ------------------------------------------------------------------ biểu tượng + số

    /** Cỡ biểu tượng hợp với font (cao chữ + 2, tối thiểu 10). */
    public static int coTheoChu(game.render.screen.Font f) {
        int h = f == null ? 12 : f.getHeight();
        return h + 2 < 10 ? 10 : h + 2;
    }

    /**
     * Vẽ biểu tượng + số (góc trái trên x, y), gon = số gọn ("1,25 Tr") hay đầy đủ ("1.250.000"). Trả bề ngang đã vẽ.
     * KHÔNG đăng ký vùng chạm (dùng trong dòng danh sách: chạm dòng = chọn dòng); chỗ cần chú thích dùng veGiaCham.
     */
    public static int veGia(Graphics g, int loai, long v, int x, int y, game.render.screen.Font f, boolean gon) {
        f = fontSo(f);
        int co = coTheoChu(f);
        ve(g, loai, x, y, co);
        String s = gon ? gon(v) : so(v);
        int fh = f == null ? co : f.getHeight();
        int xs = x + co + 2;
        if (f != null) {
            f.drawString(g, s, xs, y + (co - fh) / 2, 0);
            return co + 2 + f.getWidth(s);
        }
        return co + 2;
    }

    /** Bề ngang veGia sẽ vẽ (bố cục canh phải / giữa). */
    public static int rongGia(int loai, long v, game.render.screen.Font f, boolean gon) {
        f = fontSo(f);
        int co = coTheoChu(f);
        return co + 2 + (f == null ? 0 : f.getWidth(gon ? gon(v) : so(v)));
    }

    /**
     * Font ảnh nhỏ của J2ME (smallFontColor) không có dấu "." "," cách và chữ hoa -> số kiểu Việt Nam vẽ bằng arialFont.
     * Bản HD (APK / PC) vẽ chữ vector: giữ font gọi.
     */
    static game.render.screen.Font fontSo(game.render.screen.Font f) {
        if (f == null || PhongVe.co()) {
            return f;
        }
        game.render.screen.Font[] nho = game.render.screen.Font.smallFontColor;
        for (int i = 0; nho != null && i < nho.length; i++) {
            if (nho[i] == f) {
                return game.render.screen.Font.arialFont != null ? game.render.screen.Font.arialFont : f;
            }
        }
        return f;
    }

    /** veGia + đăng ký vùng chạm: chạm biểu tượng / số -> chú thích loại tiền + số đầy đủ. */
    public static int veGiaCham(Graphics g, int loai, long v, int x, int y, game.render.screen.Font f, boolean gon) {
        int w = veGia(g, loai, v, x, y, f, gon);
        dangKyGoiY(g, loai, v, x, y, w, coTheoChu(fontSo(f)));
        return w;
    }

    /** Đăng ký vùng (x, y, w, h theo g) bấm vào = hiện chú thích loại tiền (kèm số đầy đủ nếu v >= 0). */
    public static void dangKyGoiY(Graphics g, final int loai, final long v, int x, int y, int w, int h) {
        final int ax = x + (g == null ? 0 : g.getTranslateX());
        final int ay = y + (g == null ? 0 : g.getTranslateY());
        final int ah = h;
        // 31/10/2026: chữ số nhỏ (chợ cũ - font nhỏ 10 px): vùng chạm nới tới cỡ chạm tối thiểu quanh tâm
        if (h < VungCham.MIN) {
            y -= (VungCham.MIN - h) / 2;
            h = VungCham.MIN;
        }
        if (w < VungCham.MIN) {
            x -= (VungCham.MIN - w) / 2;
            w = VungCham.MIN;
        }
        VungCham.them(g, x, y, w, h, new IAction() {
            public void perform() {
                hienGoiY(loai, v, ax, ay + ah);
            }
        }, "tien-" + (loai >= 0 && loai < SO_LOAI ? ANH[loai] : "?"));
    }

    /**
     * Hàng tiền của nhân vật chính (đầu / chân hành trang, cửa hàng): lượng canh trái tại xTrai, lượng khoá canh giữa
     * tại xGiua (nếu >= 0), xu canh phải tại xPhai; số gọn, chạm = chú thích + số đầy đủ.
     */
    public static void veHangTien(Graphics g, int xTrai, int xGiua, int xPhai, int y, game.render.screen.Font f) {
        game.model.MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        if (mc == null) {
            return;
        }
        veGiaCham(g, LUONG, mc.luong, xTrai, y, f, true);
        if (mc.luongKhoa > -1 && xGiua >= 0) {
            int w = rongGia(LUONG_KHOA, mc.luongKhoa, f, true);
            veGiaCham(g, LUONG_KHOA, mc.luongKhoa, xGiua - w / 2, y, f, true);
        }
        int w = rongGia(XU, mc.charXu, f, true);
        veGiaCham(g, XU, mc.charXu, xPhai - w, y, f, true);
    }

    /** Bề ngang viên tiền (vePill) cao h. */
    public static int rongPill(int loai, long v, int h, game.render.screen.Font f) {
        f = fontSo(f);
        int co = Math.min(h - 6, coTheoChu(f) + 2);
        int w = 6 + co + 3 + (f == null ? 0 : f.getWidth(gon(v))) + 8;
        return w < h ? h : w;
    }

    /**
     * Viên tiền (đầu hành trang / cửa hàng giao diện mới): nền viên sáng + biểu tượng + số gọn, cao h (>= cỡ chạm tối
     * thiểu), cả viên là vùng chạm -> chú thích + số đầy đủ. Trả bề ngang.
     */
    public static int vePill(Graphics g, int loai, long v, int x, int y, int h, game.render.screen.Font f) {
        f = fontSo(f);
        int w = rongPill(loai, v, h, f);
        int co = Math.min(h - 6, coTheoChu(f) + 2);
        int ph = co + 6;
        int py = y + (h - ph) / 2;
        g.setColor(0x9AA8C0);
        g.fillRoundRect(x, py, w, ph, ph, ph);
        g.setColor(0xFFFFFF);
        g.fillRoundRect(x + 1, py + 1, w - 2, ph - 2, ph - 2, ph - 2);
        ve(g, loai, x + 4, py + 3, co);
        if (f != null) {
            f.drawString(g, gon(v), x + 4 + co + 3, py + (ph - f.getHeight()) / 2, 0);
        }
        dangKyGoiY(g, loai, v, x, y, w, h);
        return w;
    }

    // ------------------------------------------------------------------ chú thích (tooltip)

    private static int gyLoai = -1;
    private static long gyGiaTri = -1;
    private static int gyX, gyY;
    private static long gyLuc;
    /** thời gian hiện chú thích (ms) */
    public static final long GOI_Y_MS = 3000;

    /** Hiện chú thích loại tiền gần (x, y) (toạ độ màn jar). */
    public static void hienGoiY(int loai, int x, int y) {
        hienGoiY(loai, -1, x, y);
    }

    /** Hiện chú thích + số đầy đủ v (v < 0: không hiện số). */
    public static void hienGoiY(int loai, long v, int x, int y) {
        gyLoai = loai;
        gyGiaTri = v;
        gyX = x;
        gyY = y;
        gyLuc = System.currentTimeMillis();
    }

    public static boolean dangGoiY() {
        return gyLoai >= 0 && System.currentTimeMillis() - gyLuc < GOI_Y_MS;
    }

    /** Dòng chữ của chú thích đang hiện (kiểm thử). */
    public static String chuGoiY() {
        if (gyLoai < 0) {
            return null;
        }
        String s = goiY(gyLoai);
        return gyGiaTri >= 0 ? tenHoa(gyLoai) + ": " + so(gyGiaTri) + " - " + GOI_Y[gyLoai] : s;
    }

    /** Canvas.paint (sau lớp thông báo): vẽ chú thích đang hiện. */
    public static void veGoiY(Graphics g) {
        if (!dangGoiY()) {
            return;
        }
        game.render.screen.Font f = game.render.screen.Font.borderFont;
        if (f == null) {
            return;
        }
        Canvas.resetTrans(g);
        String s = chuGoiY();
        int co = coTheoChu(f);
        int w = co + 6 + f.getWidth(s) + 8;
        int maxW = Canvas.w - 8;
        if (w > maxW) {
            s = GiaoDien.catChu(f, s, maxW - co - 14);
            w = maxW;
        }
        int h = co + 6;
        int x = gyX;
        int y = gyY + 2;
        if (x + w > Canvas.w - 4) {
            x = Canvas.w - 4 - w;
        }
        if (x < 4) {
            x = 4;
        }
        if (y + h > Canvas.h - 4) {
            y = gyY - h - 20;
        }
        if (y < 4) {
            y = 4;
        }
        g.setColor(0x0B1630);
        g.fillRoundRect(x, y, w, h, 8, 8);
        g.setColor(0xF2B11E);
        g.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
        ve(g, gyLoai, x + 4, y + 3, co);
        f.drawString(g, s, x + co + 8, y + (h - f.getHeight()) / 2, 0);
    }
}
