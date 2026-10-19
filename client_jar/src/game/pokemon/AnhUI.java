package game.pokemon;

/**
 * Ảnh PNG giao diện 0.9.5 (res/ui, sinh bởi tools/pokemon/sinh_icon_ui.py): tấm icon {@link #TAM_ICON} (ô i = id
 * BieuTuong, ô 0 = Poké Ball) và da nút 9-slice {@link #TAM_NUT}. Ảnh là độ sáng + alpha; bản PC / APK tô màu lúc chạy
 * ({@link #to}) rồi thu phóng, nhớ đệm. J2ME không dùng (ChuUI.bo == null -> BieuTuong vẽ vector).
 * Lời gọi qua ChuUI.hinh: lệnh = {@link #BONG} / {@link #NUT}[k] là mảng ĐÁNH DẤU (so bằng ==, không đọc nội dung).
 */
public final class AnhUI {
    public static final String TAM_ICON = "/ui/ui_96.png", TAM_NUT = "/ui/nut_96.png";
    /** cạnh ô (px ảnh), số cột tấm icon / tấm nút */
    public static final int O = 96, COT = 8, COT_NUT = 5;
    /** góc 9-slice của da nút (px ảnh) */
    public static final int GOC_NUT = 32;
    public static final int NUT_THUONG = 0, NUT_NHAN = 1, NUT_TAT = 2, NUT_TRON = 3, NUT_TRON_NHAN = 4;

    /**
     * Icon CHIÊU (chỉ giao diện; tools/pokemon/sinh_icon_chieu.py): tấm 8 cột, cạnh ô {@link #CO_CHIEU}[i] px, ô =
     * {@link #oChieu}. Bo góc: chieu_&lt;cỡ&gt;.png; tròn (nút HUD): chieu_tron_&lt;cỡ&gt;.png. Ảnh đã tô màu sẵn (không tô lúc chạy).
     */
    public static final int[] CO_CHIEU = {32, 64, 96};
    public static final int COT_CHIEU = 8;

    public static String tamChieu(boolean tron, int co) {
        return (tron ? "/ui/chieu_tron_" : "/ui/chieu_") + co + ".png";
    }

    /** Ô của chiêu sk dòng lop trong tấm icon chiêu (thứ tự PokeChieu.TEN); -1 = ngoài bảng. */
    public static int oChieu(int lop, int sk) {
        String[][] t = PokeChieu.TEN;
        if (lop < 0 || lop >= t.length || sk < 0 || sk >= t[lop].length) return -1;
        int o = sk;
        for (int i = 0; i < lop; i++) o += t[i].length;
        return o;
    }

    public static final float[] BONG = new float[1];
    public static final float[][] NUT = {new float[1], new float[1], new float[1], new float[1], new float[1]};

    private AnhUI() {
    }

    /** Ô của lệnh BieuTuong trong tấm icon; -1 = không có (vẽ vector). */
    public static int o(float[] h) {
        if (h == BONG) return 0;
        float[][] a = BieuTuong.HINH;
        for (int i = 1; i < a.length; i++) if (a[i] == h) return i;
        return -1;
    }

    /** Kiểu da nút của mảng đánh dấu; -1 = không phải nút. */
    public static int nut(float[] h) {
        for (int k = 0; k < NUT.length; k++) if (NUT[k] == h) return k;
        return -1;
    }

    /** Màu tối (viền) của màu c. */
    public static int toi(int c) {
        return (((c >> 16) & 255) * 45 / 100) << 16 | (((c >> 8) & 255) * 45 / 100) << 8 | (c & 255) * 45 / 100;
    }

    /**
     * Màu viền khi tô icon: icon màu trên nền nhạt -> tối của chính màu đó; icon TRẮNG (nút HUD trên đĩa màu) -> tối của
     * màu nền để viền hoà vào đĩa. Poké Ball luôn đỏ / trắng.
     */
    public static int vien(int o, int mau, int nen) {
        if (o == 0) return 0x5A1A14;
        int s = ((mau >> 16) & 255) * 3 + ((mau >> 8) & 255) * 6 + (mau & 255);
        return s > 2250 ? toi(nen) : toi(mau);
    }

    /** Màu chính khi tô ô o (Poké Ball cố định đỏ). */
    public static int mau(int o, int mau) {
        return o == 0 ? 0xE8412C : mau;
    }

    private static int tron(int a, int b, int t) {
        int r = ((a >> 16) & 255) + ((((b >> 16) & 255) - ((a >> 16) & 255)) * t >> 8);
        int g = ((a >> 8) & 255) + ((((b >> 8) & 255) - ((a >> 8) & 255)) * t >> 8);
        int l = (a & 255) + (((b & 255) - (a & 255)) * t >> 8);
        return r << 16 | g << 8 | l;
    }

    /**
     * Tô màu tại chỗ n điểm ARGB (kênh đỏ = độ sáng): sáng 140 = mau, sáng hơn -> pha trắng, tối hơn -> pha vien.
     */
    public static void to(int[] px, int n, int mau, int vien) {
        for (int i = 0; i < n; i++) {
            int p = px[i];
            int a = p >>> 24;
            if (a == 0) {
                px[i] = 0;
                continue;
            }
            int v = (p >> 16) & 255;
            int c = v < 140 ? tron(vien, mau, v * 256 / 140) : tron(mau, 0xFFFFFF, (v - 140) * 256 / 115);
            px[i] = a << 24 | c;
        }
    }
}
