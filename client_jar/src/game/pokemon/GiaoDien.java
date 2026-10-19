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
 *
 * 0.9.7 (kiểu NRO / NSO): hộp, bảng, thẻ, dải tiêu đề, tab, ô chọn, thanh HP/PP/EXP, viên, phím mềm vẽ bằng MẢNH PIXEL
 * (KhungNro: khung gỗ viền vàng, giấy da, nút khối) - cùng một mã cho J2ME / APK / PC (móc HD của PC / APK chỉ còn
 * ghi nền rồi để jar vẽ). Khối bo góc (khoi / khoiDoc) = bậc thang pixel, bóng = khối tối cứng.
 */
public final class GiaoDien {
    public static final int DO = 0xE8312B;
    public static final int DO_SANG = 0xFF6A4D;
    public static final int DO_TOI = 0x9E1520;
    public static final int VANG = 0xFFC824;
    public static final int VANG_NHAT = 0xFFE452;
    /** cam nhấn (thương hiệu: dải vàng -> cam của logo) */
    public static final int CAM = 0xFF9F1C;
    public static final int CAM_TOI = 0xF26A1B;
    public static final int KEM_VANG = 0xFFF7B0;
    public static final int VANG_TOI = 0xC97A10;
    public static final int XANH = 0x2F66C4;   // 0.9.7: xanh dương NRO (0.9.6: teal)
    public static final int XANH_SANG = 0x5A94EA;
    public static final int XANH_NHAT = 0xC8DCF4;
    public static final int NAVY = 0x34434F;   // 0.9.6: xám đá (không còn navy)
    public static final int LA = 0x78C850;
    public static final int LA_TOI = 0x3E8A30;
    public static final int VIEN = 0x26323C;   // 0.9.6: viền tối xám đá
    public static final int TRANG = 0xFFFFFF;
    public static final int KEM = 0xF5F8FC;
    public static final int XAM = 0xB4BDCA;
    public static final int XAM_TOI = 0x5E6A80;
    /** thân bảng tối (chữ trắng đọc được) - gradient trên -> dưới */
    public static final int THAN_TREN = 0x3E4D5A;
    public static final int THAN_DUOI = 0x28343E;
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

    /** Độ sáng (0..255) màu RGB. */
    public static int doSang(int c) {
        return ((c >> 16 & 255) * 299 + (c >> 8 & 255) * 587 + (c & 255) * 114) / 1000;
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

    /** Thẻ sáng 0.9.5 (ChuDe.the): bóng mềm + viền tối mảnh + thân trắng, bo r. Bản HD vẽ một ảnh dựng sẵn. */
    public static void theSang(Graphics g, int x, int y, int w, int h, int r) {
        // 0.9.7: thẻ giấy da lõm (mảnh THE)
        if (KhungNro.ve(g, KhungNroBang.THE, x, y, w, h)) {
            return;
        }
        khoi(g, x, y + 2, w, h, r, ChuDe.BONG);
        khoi(g, x, y, w, h, r, ChuDe.KE);
        khoiDoc(g, x + 1, y + 1, w - 2, h - 2, r - 1, ChuDe.NEN, ChuDe.NEN_DUOI);
    }

    /** Hộp thoại trắng viền tối (kiểu hộp thoại Pokémon) - chữ bên trong nên dùng font tối. */
    public static void hop(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        // 0.9.7: hộp thoại khung gỗ mảnh viền vàng + giấy da (mảnh HOP), bóng cứng
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, KhungNroBang.HOP, x, y, w, h)) {
            khoi(g, x, y, w, h, 4, ChuDe.VIEN);
            khoi(g, x + 2, y + 2, w - 4, h - 4, 3, ChuDe.NEN);
        }
        Font.nenSang = true;
    }

    /** Bảng thân xanh đậm, viền trắng + viền tối - chữ trắng của game gốc đọc rõ. */
    public static void bang(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        // 0.9.7: bảng của game gốc = khung gỗ lớn (mảnh TAM nếu đủ chỗ cho góc trang trí, nhỏ thì HOP)
        KhungNro.bong(g, x, y, w, h);
        if (!KhungNro.ve(g, w >= 90 && h >= 70 ? KhungNroBang.TAM : KhungNroBang.HOP, x, y, w, h)) {
            khoi(g, x, y, w, h, 4, ChuDe.VIEN);
            khoi(g, x + 2, y + 2, w - 4, h - 4, 3, ChuDe.NEN);
        }
        Font.nenSang = true;
    }

    /** thân bảng sáng (kem) */
    public static final int NEN_SANG = 0xFCFCF4;
    public static final int NEN_SANG_DUOI = 0xEEF0E4;
    /** hàng xen kẽ trên bảng sáng */
    public static final int HANG_CHAN = 0xF4F6FB;
    public static final int HANG_LE = 0xE6EAF4;

    /** Màu ô / khung con theo nền đang vẽ: bảng sáng -> trắng ngà, nền tối -> navy (khung mô tả, ô chọn cũ). */
    public static int mauKhungCon() {
        return Font.nenSang ? HANG_CHAN : ChuDe.XANH_TOI;
    }

    /**
     * Trạng thái trống thân thiện: Poké Ball lớn đung đưa + dòng chính (đậm) + dòng phụ, giữa (cx, cy),
     * chữ ngắt theo rong. Dùng cho danh sách / kênh / bộ sưu tập chưa có gì.
     */
    public static void veTrong(Graphics g, int cx, int cy, int rong, String chinh, String phu) {
        int goc = (Canvas.gameTick % 40) < 20 ? (Canvas.gameTick % 20) - 10 : 10 - (Canvas.gameTick % 20);
        khoi(g, cx - 16, cy - 30 + 26, 32, 6, 3, pha(NEN_SANG_DUOI, VIEN, 1, 6));   // bóng
        pokeBall(g, cx, cy - 16, 12, goc);
        Font f = Font.normalFont[0];
        if (chinh != null) {
            f.drawString(g, catChu(f, chinh, rong), cx, cy + 2, Font.CENTER);
        }
        if (phu != null) {
            String[] d = Font.arialFontW.splitFontBStrInLine(phu, rong);
            for (int i = 0; i < d.length && i < 3; i++) {
                Font.arialFontW.drawString(g, d[i], cx, cy + 18 + i * 13, Font.CENTER);
            }
        }
    }

    /** Bảng tối (giữ chữ trắng) - cho thẻ / khung con cần nền đậm. */
    public static void bangToi(Graphics g, int x, int y, int w, int h) {
        moClip(g);
        // 0.9.7: thẻ tối lõm (chữ trắng của game đọc rõ)
        if (!KhungNro.ve(g, KhungNroBang.THE_TOI, x, y, w, h)) {
            khoi(g, x, y, w, h, 3, ChuDe.VIEN);
            khoi(g, x + 1, y + 1, w - 2, h - 2, 2, 0x2A1C12);
        }
        Font.nenSang = false;
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
        // 0.9.7: thẻ trên nền banner (đăng nhập / máy chủ) = hộp gỗ giấy da (chữ tối)
        hop(g, x, y, w, h);
    }

    /** Bảng xanh đậm mờ trên nền banner (chọn / tạo nhân vật): chữ trắng của game đọc rõ. */
    public static void bangMo(Graphics g, int x, int y, int w, int h) {
        hop(g, x, y, w, h);
    }

    /** Bảng có dải tiêu đề đỏ cao hTieuDe (tính từ mép trên khung). */
    public static void bangTieuDe(Graphics g, int x, int y, int w, int h, int hTieuDe, String tieuDe) {
        bang(g, x, y, w, h);
        dai(g, x + 4, y + 4, w - 8, hTieuDe - 6, tieuDe);
    }

    /**
     * Đầu bảng 0.9.7 (kiểu NRO): BIỂN tiêu đề đỏ viền vàng canh giữa, chữ trắng viền nâu. Cùng chữ ký cũ (màn cũ / J2ME
     * gọi "dải tiêu đề").
     */
    public static void dai(Graphics g, int x, int y, int w, int h, String tieuDe) {
        if (h < 6) {
            return;
        }
        if (tieuDe != null && tieuDe.length() > 0) {
            tieuDe = tieuDe(tieuDe);
            tieuDe = ChuDe.cat(tieuDe, ChuDe.TIEU_DE, w - 2 * Math.min(h, 16) - 12);   // chừa chỗ nút X nhỏ hai bên
            int wb = Math.min(w, ChuDe.rong(tieuDe, ChuDe.TIEU_DE) + 40);
            ChuDe.bien(g, x + (w - wb) / 2, y, wb, h);
            ChuDe.chuVienGiua(g, tieuDe, x + w / 2, y, h, ChuDe.TIEU_DE, 0xFFFFFF, ChuDe.VIEN, 2);
        }
    }


    /** Viên tên tab (giống bảng nhân vật): đỏ khi đang ở nội dung, xanh khi đang chọn tab. */
    public static void vienTab(Graphics g, int x, int y, int w, int h, boolean doiTab) {
        // 0.9.7: tab gỗ (doiTab = đang chọn tab -> giấy da)
        if (!KhungNro.ve(g, doiTab ? KhungNroBang.TAB_CHON : KhungNroBang.TAB, x, y, w, h)) {
            khoi(g, x, y, w, h, 3, ChuDe.VIEN);
            khoi(g, x + 1, y + 1, w - 2, h - 2, 2, doiTab ? ChuDe.NEN : 0x8E4A1E);
        }
    }

    /** Mũi tên đổi tab vàng viền tối, đỉnh tại (x, yTam); trai = chỉ sang trái. */
    public static void muiTab(Graphics g, int x, int yTam, boolean trai) {
        // 0.9.7: mũi tên đồng pixel
        if (KhungNro.co()) {
            int w = KhungNro.rong(KhungNroBang.MUI_PHAI), h = KhungNro.cao(KhungNroBang.MUI_PHAI);
            if (trai) {
                KhungNro.hinhLat(g, KhungNroBang.MUI_PHAI, x, yTam - h / 2);
            } else {
                KhungNro.hinh(g, KhungNroBang.MUI_PHAI, x - w, yTam - h / 2);
            }
            return;
        }
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

    // ------------------------------------------------------------------ biểu tượng mục menu
    /** từ khoá (chữ thường, sau khi đổi từ điển) -> hình KieuO + hệ (màu); dòng đầu khớp trước. */
    private static final String[] MUC_KHOA = {
        "hành trang", "túi", "kỹ năng", "chiêu", "tiềm năng", "trang bị", "thông tin", "nhiệm vụ", "sổ tay",
        "cài đặt", "cấu hình", "âm thanh", "nhạc", "bản đồ", "vùng", "bạn bè", "tìm bạn", "nhóm", "đội",
        "top", "bảng", "cửa hàng", "shop", "mua", "bán", "chat", "tin nhắn", "kênh", "thế giới",
        "pk", "đấu", "gym", "dungeon", "hang", "hệ sinh thái", "câu cá", "đào", "nấu", "vườn", "trứng",
        "pokédex", "pet", "thú", "cây", "cổ vật", "nạp", "xu", "lượng", "đăng xuất", "thoát", "về", "hồi sinh",
        "giao hàng", "luyện", "auto", "tự", "dừng", "menu nhanh", "hướng dẫn", "ghi công", "đổi", "nhận", "quà",
    };
    private static final byte[][] MUC_HINH = {
        {KieuO.HINH_BOM, 1}, {KieuO.HINH_BOM, 1}, {KieuO.HINH_SET, 3}, {KieuO.HINH_SET, 3}, {KieuO.HINH_TANG, 6},
        {KieuO.HINH_KHIEN, 16}, {KieuO.HINH_SAO, 10}, {KieuO.HINH_LA, 4}, {KieuO.HINH_LA, 4},
        {KieuO.HINH_LOC, 0}, {KieuO.HINH_LOC, 0}, {KieuO.HINH_SONG, 2}, {KieuO.HINH_SONG, 2}, {KieuO.HINH_NHANH, 9},
        {KieuO.HINH_NHANH, 9}, {KieuO.HINH_TIM, 17}, {KieuO.HINH_TIM, 17}, {KieuO.HINH_TIM, 17}, {KieuO.HINH_KHIEN, 6},
        {KieuO.HINH_SAO, 3}, {KieuO.HINH_SAO, 3}, {KieuO.HINH_BONG, 2}, {KieuO.HINH_BONG, 2}, {KieuO.HINH_BONG, 2},
        {KieuO.HINH_BONG, 2}, {KieuO.HINH_MAY, 0}, {KieuO.HINH_MAY, 0}, {KieuO.HINH_MAY, 0}, {KieuO.HINH_MAY, 0},
        {KieuO.HINH_VA, 6}, {KieuO.HINH_VA, 6}, {KieuO.HINH_KHIEN, 12}, {KieuO.HINH_MA, 13}, {KieuO.HINH_MA, 13},
        {KieuO.HINH_LA, 4}, {KieuO.HINH_GIOT, 2}, {KieuO.HINH_THEP, 8}, {KieuO.HINH_LUA, 1}, {KieuO.HINH_LA, 4},
        {KieuO.HINH_TIM, 17}, {KieuO.HINH_SAO, 10}, {KieuO.HINH_TIM, 17}, {KieuO.HINH_TIM, 17}, {KieuO.HINH_LA, 4},
        {KieuO.HINH_THEP, 12}, {KieuO.HINH_SAO, 3}, {KieuO.HINH_SAO, 3}, {KieuO.HINH_SAO, 3}, {KieuO.HINH_NHANH, 15},
        {KieuO.HINH_NHANH, 15}, {KieuO.HINH_NHANH, 9}, {KieuO.HINH_CONG, 4}, {KieuO.HINH_NHANH, 1}, {KieuO.HINH_TANG, 6},
        {KieuO.HINH_VONG, 14}, {KieuO.HINH_VONG, 14}, {KieuO.HINH_KHIEN, 0}, {KieuO.HINH_SAO, 1}, {KieuO.HINH_LA, 4},
        {KieuO.HINH_SAO, 17}, {KieuO.HINH_LOC, 10}, {KieuO.HINH_BOM, 1}, {KieuO.HINH_BOM, 1},
    };
    private static final java.util.Hashtable boHinhMuc = new java.util.Hashtable();

    /** Hình + hệ cho mục menu theo chữ (null = không có biểu tượng). */
    public static byte[] hinhMuc(String caption) {
        if (caption == null || caption.length() == 0) {
            return null;
        }
        Object o = boHinhMuc.get(caption);
        if (o != null) {
            return o instanceof byte[] ? (byte[]) o : null;
        }
        String t = ChuPokemon.thuong(ChuPokemon.D(caption));
        byte[] kq = null;
        int dai = 0;
        for (int i = 0; i < MUC_KHOA.length && i < MUC_HINH.length; i++) {
            if (MUC_KHOA[i].length() > dai && t.indexOf(MUC_KHOA[i]) >= 0) {
                kq = MUC_HINH[i];               // khoá dài nhất thắng ("đăng xuất" hơn "xu")
                dai = MUC_KHOA[i].length();
            }
        }
        if (boHinhMuc.size() > 300) {
            boHinhMuc.clear();
        }
        boHinhMuc.put(caption, kq == null ? (Object) "" : kq);
        return kq;
    }

    /** Ô biểu tượng mục menu 12x12 tại (x, y); trả bề ngang đã dùng (0 nếu không có). */
    public static int veHinhMuc(Graphics g, int x, int y, String caption) {
        byte[] h = hinhMuc(caption);
        if (h == null) {
            return 0;
        }
        int mau = KieuO.mauHe(h[1]);
        khoi(g, x, y, 13, 13, 4, VIEN);
        khoiDoc(g, x + 1, y + 1, 11, 11, 3, pha(mau, TRANG, 1, 3), pha(mau, 0x000000, 1, 3));
        javax.microedition.lcdui.Image a = KieuO.anhHinh(h[0], 1);
        if (a != null) {
            int cx = g.getClipX(), cy = g.getClipY(), cw = g.getClipWidth(), ch = g.getClipHeight();
            g.clipRect(x + 1, y + 1, 11, 11);
            g.drawImage(a, x + 7, y + 7, javax.microedition.lcdui.Graphics.HCENTER | javax.microedition.lcdui.Graphics.VCENTER);
            g.setClip(cx, cy, cw, ch);
        }
        return 16;
    }

    /** Ô đang chọn trên nền tối: thân xanh sáng, viền vàng. */
    public static void chon(Graphics g, int x, int y, int w, int h) {
        // 0.9.7: ô chọn = thanh giấy da viền vàng (mảnh HANG_CHON)
        if (!KhungNro.ve(g, KhungNroBang.HANG_CHON, x, y, w, h)) {
            khoi(g, x, y, w, h, 2, ChuDe.VANG);
            khoi(g, x + 1, y + 1, w - 2, h - 2, 1, ChuDe.CHINH_NHAT);
        }
    }

    /** Con trỏ ▶ đen kiểu menu Pokémon, đỉnh trái (x, y), cao h. */
    public static void conTro(Graphics g, int x, int y, int h) {
        g.setColor(VIEN);
        g.fillTriangle(x, y, x, y + h, x + h / 2 + 1, y + h / 2);
    }

    /** Ô đang chọn trên nền trắng: vàng nhạt viền vàng đậm (chữ tối). */
    public static void chonSang(Graphics g, int x, int y, int w, int h) {
        chon(g, x, y, w, h);
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
        return p > 50 ? ChuDe.HP_XANH : (p > 20 ? ChuDe.HP_VANG : ChuDe.HP_DO);
    }

    /** Mẫu chữ 3x5 "HP" cho thẻ thanh máu kiểu Pokémon. */
    private static final String[] CHU_HP = {"#.#.##.", "#.#.#.#", "###.##.", "#.#.#..", "#.#.#.."};

    /** Thanh máu kiểu Pokémon: thẻ "HP" vàng trên nền tối + thanh xanh -> vàng -> đỏ. */
    public static void thanhHP(Graphics g, int x, int y, int w, int h, long gt, long max) {
        khoi(g, x, y - 1, 11, h + 2 > 7 ? h + 2 : 7, 2, VIEN);
        g.setColor(VANG);
        int yc = y - 1 + ((h + 2 > 7 ? h + 2 : 7) - 5) / 2;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 7; c++) {
                if (CHU_HP[r].charAt(c) == '#') {
                    g.fillRect(x + 2 + c, yc + r, 1, 1);
                }
            }
        }
        thanh(g, x + 11, y, w - 11, h, gt, max, 0);
    }

    /**
     * Thanh HP / PP / EXP (0.9.7 NRO): rãnh tối lõm viền nâu (mảnh RANH) + dải đổ đầy có ánh: loai 0 = HP ĐỎ,
     * 1 = PP xanh dương, 2 = EXP vàng.
     */
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
        int dai = max <= 0 ? 0 : (int) ((w - 2) * gt / max);
        if (gt > 0 && dai < 1) {
            dai = 1;
        }
        if (h >= 6 && KhungNro.ve(g, KhungNroBang.RANH, x, y, w, h)) {
            if (dai > 0) {
                int m = loai == 0 ? KhungNroBang.DAY_HP : (loai == 1 ? KhungNroBang.DAY_PP : KhungNroBang.DAY_EXP);
                KhungNro.ve(g, m, x + 1, y + 1, Math.min(dai, w - 2), h - 2);
            }
            return;
        }
        g.setColor(ChuDe.VIEN);
        g.fillRect(x, y, w, h);
        g.setColor(ChuDe.RANH);
        g.fillRect(x + 1, y + 1, w - 2, h - 2);
        if (dai <= 0) {
            return;
        }
        int mau = loai == 0 ? mauHp(gt, max) : (loai == 1 ? ChuDe.PP : ChuDe.EXP);
        g.setColor(mau);
        g.fillRect(x + 1, y + 1, Math.min(dai, w - 2), h - 2);
        if (h >= 5) {
            g.setColor(pha(mau, TRANG, 1, 2));
            g.fillRect(x + 1, y + 1, Math.min(dai, w - 2), 1);
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

    /** Poké Ball tâm (cx, cy) bán kính r, quay goc độ. 0.9.7: icon pixel (quay theo bước 90 độ) khi có ảnh. */
    public static void pokeBall(Graphics g, int cx, int cy, int r, int goc) {
        if (r < 3) {
            r = 3;
        }
        if (r >= 6 && KhungNro.coIcon(BieuTuong.POKEBALL)) {
            int k = Math.max(1, (2 * r + 4) / 16);
            if (goc % 360 == 0 || k > 1) {
                if (KhungNro.iconGiua(g, BieuTuong.POKEBALL, cx, cy, k, -1)) {
                    return;
                }
            } else {
                int xoay = (((-goc % 360) + 360 + 45) % 360) / 90;
                if (KhungNro.iconXoay(g, BieuTuong.POKEBALL, cx, cy, xoay)) {
                    return;
                }
            }
        }
        pokeBallVe(g, cx, cy, r, goc);
    }

    /** Poké Ball vẽ bằng lệnh (cung, tam giác) - ô nhỏ / thiếu ảnh. */
    static void pokeBallVe(Graphics g, int cx, int cy, int r, int goc) {
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
        g.setColor(0x1B0E06);
        g.fillArc(cx - r + 2, cy + r - 1, r * 2 - 4, 4, 0, 360);   // bóng dưới bóng
        pokeBall(g, cx, cy, r, goc);
    }

    /** Thanh phím mềm (ảnh Res.imgCmdBar w x h) - 0.9.7: dải GỖ tối, mép trên chỉ vàng (chữ phím mềm trắng viền). */
    public static void thanhPhim(Graphics g, int w, int h) {
        g.setColor(0x2A1206);
        g.fillRect(0, 0, w, h);
        g.setColor(0x1B0E06);
        g.fillRect(0, 2, w, 1);
        g.setColor(0xF2C54E);
        g.fillRect(0, 3, w, 1);
        g.setColor(0x8A5A12);
        g.fillRect(0, 4, w, 1);
        for (int i = 5; i < h; i++) {
            g.setColor(pha(0x7A3E18, 0x4E260E, i - 5, h - 5));
            g.fillRect(0, i, w, 1);
        }
    }


    /**
     * Phím mềm dạng viên: vt 0 trái (xanh), 1 giữa (vàng, chấm phím giữa), 2 phải (đỏ); nhan = đang
     * bấm (lún 1 px, màu đậm). Bề ngang theo chữ, tối đa 1/3 màn.
     */
    public static void nutMem(Graphics g, String s, int vt, boolean nhan) {
        nutMem(g, s, vt, nhan, vt == 1 ? Canvas.w * 2 / 5 : Canvas.w * 3 / 10 - 2);
    }

    /** Bề ngang viên phím mềm (chưa cắt) cho chuỗi s. */
    public static int rongNutMem(String s, int vt) {
        return s == null || s.length() == 0 || Font.borderFont == null ? 0 : Font.borderFont.getWidth(s) + 12 + (vt == 1 ? 9 : 0);
    }

    public static void nutMem(Graphics g, String s, int vt, boolean nhan, int toiDa) {
        if (s == null || s.length() == 0 || Font.borderFont == null) {
            return;
        }
        Font f = Font.borderFont;
        int goi = vt == 1 ? 9 : 0;
        s = catChu(f, s, toiDa - 12 - goi);
        int w = f.getWidth(s) + 12 + goi;
        int h = 13;
        int y = Canvas.h - 15 + (nhan ? 1 : 0);
        int x = vt == 0 ? 2 : (vt == 2 ? Canvas.w - 2 - w : Canvas.hw - w / 2);
        // 0.9.7 (NRO): nút khối - trái = cam (chính), giữa = xanh lá, phải = gỗ (đóng / lùi)
        int manh = vt == 0 ? KhungNroBang.NUT_CAM : (vt == 1 ? KhungNroBang.NUT_LA : KhungNroBang.NUT_GO);
        if (KhungNro.ve(g, nhan ? manh + 1 : manh, x, y, w, h)) {
            f.drawString(g, s, x + goi + (w - goi) / 2, y + (nhan ? 1 : 0), Font.CENTER);
            return;
        }
        int tren = vt == 0 ? ChuDe.CHINH_SANG : (vt == 1 ? VANG : 0x8C99A5);
        int duoi = vt == 0 ? ChuDe.CHINH_TOI : (vt == 1 ? ChuDe.VANG_TOI : 0x55626E);
        if (nhan) {
            int t = tren;
            tren = duoi;
            duoi = t;
        }
        khoi(g, x, y, w, h, 6, pha(duoi, 0, 1, 3));
        khoiDoc(g, x + 1, y + 1, w - 2, h - 2, 5, tren, duoi);
        if (!nhan) {
            g.setColor(pha(tren, TRANG, 1, 2));
            g.fillRect(x + 5, y + 2, w - 10, 1);
        }
        if (goi > 0) {
            // gợi ý phím giữa: chấm tròn (phím chọn / 5)
            khoi(g, x + 4, y + 4, 6, 6, 3, VIEN);
            khoi(g, x + 5, y + 5, 4, 4, 2, TRANG);
        }
        f.drawString(g, s, x + goi + (w - goi) / 2, y, Font.CENTER);
    }

    /**
     * Viên trạng thái (0.9.7: khối pixel màu mau - viền nâu đen, ánh trên, tối dưới), chữ trắng viền ở giữa.
     */
    public static void vien(Graphics g, int x, int y, int w, int h, int mau, String chu) {
        khoiPixel(g, x, y, w, h, mau);
        if (chu != null) {
            if (ChuUI.bo != null) {
                ChuDe.chuVienGiua(g, ChuDe.cat(chu, ChuDe.SO, w - 6), x + w / 2, y, h, ChuDe.SO, 0xFFFFFF, ChuDe.VIEN, 2);
            } else if (Font.borderFont != null) {
                Font.borderFont.drawString(g, chu, x + w / 2, y + (h - Font.borderFont.getHeight()) / 2 + 1, Font.CENTER);
            }
        }
    }

    /** Khối pixel màu bất kỳ: viền nâu đen 1 px (góc cắt), hàng ánh trên, 1 hàng tối dưới - nhãn hệ, huy hiệu. */
    public static void khoiPixel(Graphics g, int x, int y, int w, int h, int mau) {
        if (w < 3 || h < 3) {
            return;
        }
        g.setColor(ChuDe.VIEN);
        g.fillRect(x + 1, y, w - 2, h);
        g.fillRect(x, y + 1, w, h - 2);
        g.setColor(mau);
        g.fillRect(x + 1, y + 1, w - 2, h - 2);
        g.setColor(pha(mau, TRANG, 2, 5));
        g.fillRect(x + 1, y + 1, w - 2, 1);
        g.setColor(pha(mau, 0, 1, 3));
        g.fillRect(x + 1, y + h - 2, w - 2, 1);
        ChuUI c = ChuUI.bo;
        if (c != null) {
            c.nen(g, x, y, w, h, doSang(mau));
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
        ChuDe.bien(g, x + 6, y + 5, w - 12, 18);
    }
}
