package game.pokemon;

import game.render.Canvas;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;

/**
 * CHỦ ĐỀ GIAO DIỆN 0.9.7 "kiểu NRO / NSO" (một nơi duy nhất: bảng màu + số đo + kiểu chữ) cho HUD, tấm, bảng, hộp
 * thoại, thông báo, nút: khung GỖ viền vàng góc Poké Ball, thân GIẤY DA, nút KHỐI nổi 3D (cam chính, xanh phụ, đỏ nguy
 * hiểm, đá tắt), tab gỗ thật, ô đồ hộp gỗ lõm tối, chữ tiêu đề trắng viền nâu - vẽ bằng mảnh pixel 9-slice
 * ({@link KhungNro}, tools/giao_dien_nro/sinh_khung_nro.py). Tên hằng giữ như 0.9.6 (API ổn định cho mọi màn / nhánh),
 * GIÁ TRỊ đổi sang bảng màu NRO: NEN = giấy da sáng, CHINH = cam, CHU = nâu đậm, BONG = bóng cứng nâu đen. Bản PC
 * (htbb.pc.GiaoDienHD / ChuHD) và APK (HdVe / HdChu) đọc cùng hằng số này nên ba bản cùng một kiểu.
 *
 * Chữ giao diện: {@link #chu} / {@link #rong} theo VAI (tiêu đề, nhãn, thường, phụ, số); tiêu đề / chữ trên nút màu:
 * {@link #chuVien} (viền nâu 1 px logic như chữ NRO). Bản PC / APK vẽ bằng phông thật (Noto Sans / Be Vietnam Pro, tiêu
 * đề Baloo 2) qua {@link ChuUI#bo}; máy J2ME dùng font ảnh của game (borderFont đã có viền).
 */
public final class ChuDe {
    private ChuDe() {
    }

    // ------------------------------------------------------------------ bảng màu (RGB)
    /** 0.9.7 thân thẻ / bảng: GIẤY DA sáng -> giấy da (NRO); tên giữ như 0.9.6 */
    public static final int NEN = 0xF2E0B4;
    public static final int NEN_DUOI = 0xE7CF9C;
    /** kem ấm (thẻ thông tin, hộp lời thoại) */
    public static final int KEM = 0xFAEECF;
    /** vùng phụ trong bảng (khung con, rãnh tab, ô trống) */
    public static final int NEN_PHU = 0xE2C994;
    public static final int NEN_PHU_TOI = 0xC9A46A;
    /** viền tối mảnh quanh thẻ / nút */
    public static final int VIEN = 0x1B0E06;
    /** đường kẻ / viền hàng nhạt */
    public static final int KE = 0xC9A46A;
    /** chữ (xám đá đậm, không navy) */
    public static final int CHU = 0x3A2412;
    public static final int CHU_PHU = 0x7A5634;
    public static final int CHU_NHAT = 0xA88A66;
    /** 0.9.7 MÀU CHÍNH = CAM (nút chính, tab chọn, thanh trượt): dải CHINH_SANG -> CHINH (0.9.6: teal) */
    public static final int CHINH = 0xE8861E;
    public static final int CHINH_SANG = 0xF8AE44;
    public static final int CHINH_TOI = 0xA04A08;
    public static final int CHINH_NHAT = 0xFBE3B4;
    /** nền tấm (sheet) toàn màn: dải teal rất nhạt -> gần trắng */
    public static final int TAM_TREN = 0xF2E0B4;
    public static final int TAM_DUOI = 0xE7CF9C;
    /** vàng thưởng đậm (chữ / viền trên nền vàng nhạt) */
    public static final int VANG_TOI = 0x8A5A12;
    /** nhấn: đỏ Poké Ball, xanh, vàng Pikachu */
    public static final int DO = 0xC83220;
    public static final int DO_TOI = 0x8E1A0E;
    public static final int DO_NHAT = 0xF4C0A8;
    public static final int XANH = 0x2F66C4;   // xanh thông tin (biểu tượng tin, chat) - nút chính dùng CHINH
    public static final int XANH_TOI = 0x1E3E86;
    public static final int XANH_NHAT = 0xC8DCF4;
    public static final int VANG = 0xF2C54E;
    public static final int VANG_NHAT = 0xFFE89A;
    public static final int CAM = 0xE8861E;
    public static final int LA = 0x36962E;
    public static final int LA_TOI = 0x1E6A22;
    public static final int TEAL = 0x1FB5A8;
    public static final int XAM = 0x85858E;
    public static final int TIM = 0x8E6BE8;
    public static final int HONG = 0xEF6A93;
    /** bóng đổ CỨNG (pixel) nâu đen lệch 1-2 px - mọi bản */
    public static final int BONG = 0x1B0E06;
    /** phủ mờ thế giới sau bảng / menu: ~45 % */
    public static final int PHU_NEN = 0x7A120804;
    /** 0.9.7: thanh HP ĐỎ (một màu như NRO), PP xanh dương, EXP vàng */
    public static final int HP_XANH = 0xE0301E;
    public static final int HP_VANG = 0xE0301E;
    public static final int HP_DO = 0xE0301E;
    public static final int PP = 0x3A78D8;
    public static final int EXP = 0xF2C21A;
    /** rãnh thanh: nâu đen lõm */
    public static final int RANH = 0x24160C;

    /** Màu ICON nút tròn HUD (0.9.6: đĩa trắng, icon màu): túi cam, Pokémon đỏ, nhiệm vụ vàng, đội xanh lá, bản đồ teal, cài đặt xám. */
    public static final int NUT_TUI = CAM, NUT_POKE = DO, NUT_NV = 0xF2C54E, NUT_DOI = LA, NUT_BAN_DO = XANH, NUT_CAI_DAT = XAM;

    // ------------------------------------------------------------------ số đo (px logic, khung 640x360)
    /** bo góc thẻ lớn / ô / nút */
    public static final int BO = 14, BO_O = 10, BO_NUT = 8, BO_THE = 12;
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
    /**
     * TIEU_DE = tiêu đề hộp / thẻ, TIEU_DE_TO = tiêu đề tấm (sheet) lớn, SO_TO = số lớn ("Sức mạnh"); hai vai tiêu đề
     * dùng phông HIỂN THỊ tròn đậm (dam 3: PC Baloo 2, APK Baloo 2 / Be Vietnam Pro ExtraBold).
     */
    public static final int TIEU_DE = 0, NHAN = 1, THUONG = 2, PHU = 3, SO = 4, NHAN_NHO = 5, TIEU_DE_TO = 6, SO_TO = 7;
    /** cỡ chữ (px logic) theo vai: PC / máy cảm ứng (tiêu đề tấm ~22-24 px ở màn tham chiếu 1280x720) */
    static final float[] CO_PC = {9.4f, 7.4f, 7.2f, 6.3f, 6.6f, 6.6f, 11.2f, 13.5f};
    static final float[] CO_DT = {10.4f, 8.4f, 8.2f, 7.2f, 7.4f, 7.6f, 12.2f, 14.5f};
    /** 0 thường, 1 đậm vừa, 2 đậm, 3 phông hiển thị (tròn đậm) */
    static final int[] DAM = {3, 1, 0, 0, 2, 1, 3, 3};

    public static float co(int vai) {
        return (dt() ? CO_DT : CO_PC)[vai];
    }

    /** Font ảnh J2ME cho vai (khi không có chữ thật). */
    static Font fontAnh(int vai, int mau) {
        boolean sang = GiaoDien.doSang(mau) > 150;
        if (vai == TIEU_DE || vai == TIEU_DE_TO || vai == SO_TO) {
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
    /** Ký tự font không có: dấu trừ toán học U+2212 (chữ máy chủ / bảng 0.9.8) -> '-'. */
    static String chuanKyTu(String s) {
        s = s.indexOf('\u2212') >= 0 ? s.replace('\u2212', '-') : s;
        if (ChuUI.bo == null && (s.indexOf('\u00ab') >= 0 || s.indexOf('\u00bb') >= 0)) {
            s = s.replace('\u00ab', '"').replace('\u00bb', '"');   // 0.9.15: font ảnh không có « » (tên cửa ải)
        }
        return s;
    }

    public static int rong(String s, int vai) {
        if (s == null) {
            return 0;
        }
        ChuUI b = ChuUI.bo;
        if (b != null) {
            return b.rong(s, co(vai), DAM[vai]);
        }
        return fontAnh(vai, CHU).getWidth(chuanKyTu(s));
    }

    /** Vẽ chữ: y = đỉnh hộp dòng (cao {@link #cao}), canh 0 trái / 1 phải / 2 giữa. */
    public static void chu(Graphics g, String s, int x, int y, int vai, int mau, int canh) {
        if (s == null || s.length() == 0) {
            return;
        }
        s = chuanKyTu(s);
        if (GhiChu.bat) {
            GhiChu.chu(g, s, x, y, rong(s, vai), cao(vai), canh);
            GhiChu.trongChuDe++;
            try {
                chu0(g, s, x, y, vai, mau, canh);
            } finally {
                GhiChu.trongChuDe--;
            }
            return;
        }
        chu0(g, s, x, y, vai, mau, canh);
    }

    private static void chu0(Graphics g, String s, int x, int y, int vai, int mau, int canh) {
        ChuUI b = ChuUI.bo;
        if (b != null) {
            b.ve(g, s, x, y, co(vai), DAM[vai], mau, canh);
            return;
        }
        Font f = fontAnh(vai, mau);
        if (s.indexOf('\u00b7') >= 0) {
            s = s.replace('\u00b7', '-');   // font ảnh J2ME không có "·" (vẽ thành "?")
        }
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
        // Kiem lag 10/2026: truoc day nho MOT ket qua / vai - HUD cat ten 4-6 o chieu moi khung (cung vai) nen
        // lan nao cung truot va chay lai vong do chu (substring + noi chuoi moi buoc). Nay nho CAT_SO ket qua.
        for (int i = 0; i < CAT_SO; i++) {
            if (CAT_V[i] == vai && CAT_W[i] == w && s.equals(CAT_KHOA[i])) {
                return CAT_KQ[i];
            }
        }
        String ba = ChuUI.bo != null ? "\u2026" : "..";
        String r;
        int p = viTriCap(s);
        if (p > 0) {
            // t\u00ean trang b\u1ecb "<T\u00ean> \u00b7 C\u1ea5p N" kh\u00f4ng v\u1eeba: gi\u1eef c\u1ea5p, g\u1ecdn th\u00e0nh "<T\u00ean> CN", h\u1ebft ch\u1ed7 th\u00ec c\u1eaft ph\u1ea7n t\u00ean
            String goc = s.substring(0, p), duoi = duoiCap(s, p);
            if (rong(goc + duoi, vai) <= w) {
                r = goc + duoi;
            } else {
                int n = goc.length();
                while (n > 1 && rong(goc.substring(0, n).trim() + ba + duoi, vai) > w) {
                    n--;
                }
                r = goc.substring(0, n).trim() + ba + duoi;
            }
        } else {
            int n = s.length();
            while (n > 1 && rong(s.substring(0, n) + ba, vai) > w) {
                n--;
            }
            r = s.substring(0, n).trim() + ba;
        }
        int o = catKe;
        catKe = (o + 1) % CAT_SO;
        CAT_KHOA[o] = s;
        CAT_V[o] = vai;
        CAT_W[o] = w;
        CAT_KQ[o] = r;
        return r;
    }

    /** Hậu tố cấp của tên trang bị (máy chủ: doi_ten_trang_bi_cap.sql, "Băng đô bạc · Cấp 24"). */
    static final String CAP = " · Cấp ";

    /** Vị trí " · Cấp N" (N là số, sau có thể còn " +5"...) trong tên, -1 nếu không có. */
    public static int viTriCap(String s) {
        int p = -1;
        if (s != null) {
            // CLDC 1.1 không có String.lastIndexOf(String): tìm lần xuất hiện cuối bằng indexOf
            for (int i = s.indexOf(CAP); i >= 0; i = s.indexOf(CAP, i + 1)) {
                p = i;
            }
        }
        if (p <= 0) {
            return -1;
        }
        int j = p + CAP.length();
        if (j >= s.length() || s.charAt(j) < '0' || s.charAt(j) > '9') {
            return -1;
        }
        while (j < s.length() && s.charAt(j) >= '0' && s.charAt(j) <= '9') {
            j++;
        }
        // chỉ là HẬU TỐ tên: sau số chỉ được hết chuỗi hoặc " +5" / " x3" (không phải "Cấp 12 - Lông vũ" của dòng phụ)
        if (j < s.length()) {
            if (j + 2 > s.length() || s.charAt(j) != ' ' || (s.charAt(j + 1) != '+' && s.charAt(j + 1) != 'x')) {
                return -1;
            }
            for (int k = j + 2; k < s.length(); k++) {
                if (s.charAt(k) < '0' || s.charAt(k) > '9') {
                    return -1;
                }
            }
        }
        return p;
    }

    /** Tên bỏ hậu tố " · Cấp N" (giữ phần sau như " +5") - cho hàng đã ghi "Cấp N" ở dòng phụ / cột bên. */
    public static String boCap(String s) {
        int p = viTriCap(s);
        if (p < 0) {
            return s;
        }
        int e = p + CAP.length();
        while (e < s.length() && s.charAt(e) >= '0' && s.charAt(e) <= '9') {
            e++;
        }
        return s.substring(0, p) + s.substring(e);
    }

    /** " CN" + phần sau số (vd " +5") của tên có hậu tố cấp ở vị trí p (viTriCap). */
    public static String duoiCap(String s, int p) {
        int j = p + CAP.length(), e = j;
        while (e < s.length() && s.charAt(e) >= '0' && s.charAt(e) <= '9') {
            e++;
        }
        return " C" + s.substring(j, e) + s.substring(e);
    }

    private static final int CAT_SO = 24;
    private static final String[] CAT_KHOA = new String[CAT_SO];
    private static final String[] CAT_KQ = new String[CAT_SO];
    private static final int[] CAT_W = new int[CAT_SO];
    private static final int[] CAT_V = new int[CAT_SO];
    private static int catKe;

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

    /**
     * Chữ VIỀN kiểu NRO (tiêu đề, chữ trên nút màu): viền mauVien 1 px logic 4 hướng + bóng dưới, rồi chữ mau.
     * J2ME (font ảnh): font tiêu đề / borderFont đã có viền sẵn -> vẽ thường.
     */
    /** Bề ngang chữ VẼ bằng chuVien (J2ME: chữ sáng = borderFont, rộng hơn font thường) - 0.9.16. */
    public static int rongVien(String s, int vai, int mau) {
        if (s != null && ChuUI.bo == null && GiaoDien.doSang(mau) > 150 && Font.borderFont != null) {
            return Font.borderFont.getWidth(chuanKyTu(s));
        }
        return rong(s, vai);
    }

    public static void chuVien(Graphics g, String s, int x, int y, int vai, int mau, int mauVien, int canh) {
        if (s == null || s.length() == 0) {
            return;
        }
        s = chuanKyTu(s);
        ChuUI b = ChuUI.bo;
        if (b == null) {
            // J2ME: chữ sáng trên nền màu / gỗ = borderFont (font ảnh có viền sẵn - đọc rõ), chữ tối = font thường
            if (GiaoDien.doSang(mau) > 150 && Font.borderFont != null) {
                GhiChu.chu(g, s, x, y, Font.borderFont.getWidth(s), Font.borderFont.getHeight(), canh);
                GhiChu.trongChuDe++;
                try {
                    Font.borderFont.drawString(g, s, x, y, canh == 2 ? Font.CENTER : canh == 1 ? Font.RIGHT : Font.LEFT);
                } finally {
                    GhiChu.trongChuDe--;
                }
                return;
            }
            chu(g, s, x, y, vai, mau, canh);
            return;
        }
        GhiChu.chu(g, s, x, y, rong(s, vai), cao(vai), canh);
        float c = co(vai);
        int d = DAM[vai];
        b.ve(g, s, x, y + 1, c, d, mauVien, canh);
        b.ve(g, s, x - 1, y, c, d, mauVien, canh);
        b.ve(g, s, x + 1, y, c, d, mauVien, canh);
        b.ve(g, s, x, y - 1, c, d, mauVien, canh);
        b.ve(g, s, x + 1, y + 1, c, d, mauVien, canh);
        b.ve(g, s, x, y, c, d, mau, canh);
    }

    /** Chữ viền canh giữa dọc trong (y, h). */
    public static void chuVienGiua(Graphics g, String s, int x, int y, int h, int vai, int mau, int mauVien, int canh) {
        chuVien(g, s, x, y + (h - cao(vai)) / 2, vai, mau, mauVien, canh);
    }

    // ------------------------------------------------------------------ khối cơ bản
    /** Thẻ: 0.9.7 thẻ giấy da LÕM (mảnh THE) - khối phẳng khi thiếu ảnh. */
    public static void the(Graphics g, int x, int y, int w, int h, int bo) {
        if (!KhungNro.ve(g, KhungNroBang.THE, x, y, w, h)) {
            GiaoDien.theSang(g, x, y, w, h, bo);
        }
        Font.nenSang = true;
    }

    /**
     * Đầu bảng: 0.9.7 BIỂN TIÊU ĐỀ đỏ Poké viền vàng phủ (x, y, w, h), chữ trắng viền nâu canh giữa.
     * Không vẽ nút đóng (Bang.nutDong đặt trên dải). mau: giữ chữ ký cũ (bỏ qua - biển luôn đỏ).
     */
    public static void dau(Graphics g, int x, int y, int w, int h, String tieuDe, int mau) {
        bien(g, x, y, w, h);
        if (tieuDe != null) {
            chuVienGiua(g, cat(tieuDe, TIEU_DE, w - h - 16), x + w / 2, y, h, TIEU_DE, 0xFFFFFF, VIEN, 2);
        }
    }

    /** Biển tiêu đề (mảnh BIEN) + đinh vàng hai đầu khi đủ rộng. */
    public static void bien(Graphics g, int x, int y, int w, int h) {
        if (!KhungNro.ve(g, KhungNroBang.BIEN, x, y, w, h)) {
            GiaoDien.khoi(g, x, y, w, h, 3, VIEN);
            GiaoDien.khoi(g, x + 1, y + 1, w - 2, h - 2, 2, DO);
            return;
        }
        if (w >= 60 && h >= 14) {
            KhungNro.hinhGiua(g, KhungNroBang.DINH, x + 9, y + h / 2);
            KhungNro.hinhGiua(g, KhungNroBang.DINH, x + w - 9, y + h / 2);
        }
    }

    /** Chấm đã vẽ (toạ độ tuyệt đối) - kiểm thử KiemChamMoi đọc (vòng 64 chấm, demCham tăng mãi). */
    public static final int[] CHAM_X = new int[64], CHAM_Y = new int[64];
    public static int demCham;

    /** Chấm đỏ báo mới tâm (cx, cy) - nguồn: ChamMoi. 0.9.7: chấm pixel 7x7 viền trắng + nâu. */
    public static void cham(Graphics g, int cx, int cy) {
        CHAM_X[demCham & 63] = cx + g.getTranslateX();
        CHAM_Y[demCham & 63] = cy + g.getTranslateY();
        demCham++;
        if (KhungNro.hinhGiua(g, KhungNroBang.CHAM, cx, cy)) {
            return;
        }
        int d = dt() ? 6 : 5;
        g.setColor(0xFFFFFF);
        g.fillRect(cx - d / 2 - 1, cy - d / 2 - 1, d + 2, d + 2);
        g.setColor(DO);
        g.fillRect(cx - d / 2, cy - d / 2, d, d);
    }

    /** Dấu X vẽ nét (không ảnh), tâm (cx, cy), nửa cạnh k. */
    public static void dauX(Graphics g, int cx, int cy, int k, int mau) {
        VeTay.ghi("dau X ve net");
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

    // ------------------------------------------------------------------ 0.9.7 độ hiếm (token chủ đề, MỘT nguồn)
    /**
     * Màu độ hiếm chung (vật phẩm, danh hiệu, phụ kiện, bậc trang bị) - MỘT nguồn tools/cauhinh/do_hiem.json sinh ra
     * {@link DoHiemSinh} (cùng giá trị máy chủ và web). Bậc 0 Thường / 1 Hiếm / 2 Quý / 3 Sử thi / 4 Huyền thoại (viền
     * động) / 5 Trang trí. mauHiem = chữ / viền trên nền sáng (bảng, thẻ), nenHiem = nền nhạt cùng tông, sangHiem = chữ
     * trên nền tối / thế giới (nhãn trên đầu). Giao diện khác (NRO) giữ nguyên màu độ hiếm: đây là nhận diện giá trị,
     * không phải màu chủ đề.
     */
    public static int mauHiem(int bac) {
        return DoHiemSinh.MAU[DoHiemSinh.kep(bac)];
    }

    public static int nenHiem(int bac) {
        return DoHiemSinh.NHAT[DoHiemSinh.kep(bac)];
    }

    public static int sangHiem(int bac) {
        return DoHiemSinh.SANG[DoHiemSinh.kep(bac)];
    }

    /** Bậc hiếm của vật phẩm theo colorName (Item.colorName của máy chủ). */
    public static int bacVatPham(int colorName) {
        return DoHiemSinh.bacVatPham(colorName);
    }

    /**
     * Độ hiếm danh hiệu 0 Thường / 1 Hiếm / 2 Sử thi / 3 Huyền thoại (data_danh_hieu.do_hiem) -> bậc chung
     * (DoHiemSinh.BAC_DANH_HIEU). Nhãn trên đầu, viên danh hiệu, hàng bảng Danh hiệu dùng ba hàm dưới.
     */
    public static final int DH_THUONG = DoHiemSinh.MAU[DoHiemSinh.THUONG], DH_HIEM = DoHiemSinh.MAU[DoHiemSinh.HIEM],
            DH_SU_THI = DoHiemSinh.MAU[DoHiemSinh.SU_THI], DH_HUYEN_THOAI = DoHiemSinh.MAU[DoHiemSinh.HUYEN_THOAI];

    public static int mauDanhHieu(int doHiem) {
        return mauHiem(DoHiemSinh.bacDanhHieu(doHiem));
    }

    public static int nenDanhHieu(int doHiem) {
        return nenHiem(DoHiemSinh.bacDanhHieu(doHiem));
    }

    /** Màu chữ nhãn danh hiệu trên đầu (nền thế giới tối / sáng lẫn lộn: tông sáng, có viền tối). */
    public static int nhanDanhHieu(int doHiem) {
        return sangHiem(DoHiemSinh.bacDanhHieu(doHiem));
    }
}
