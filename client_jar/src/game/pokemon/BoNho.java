package game.pokemon;

/**
 * BỘ NHỚ (0.9.7.1, sự cố "kẹt tải dữ liệu" của bản J2ME trên giả lập iOS).
 *
 * Bản J2ME chạy trên máy / giả lập có heap nhỏ và cố định. Đo bằng client_jar/tools/kiem_tai_j2me (jar thật trong
 * MicroEmulator, heap giới hạn): vào thế giới cần ~4,7 MB dữ liệu sống (chưa kể ảnh giải nén ARGB trên máy thật),
 * cộng gói đăng nhập lớn (lệnh 26 ~159 KB, 25 ~111 KB) lúc đang tải. Thiếu bộ nhớ thì OutOfMemoryError: trước đây
 * gói bị bỏ im lặng (hoặc luồng mạng chết) -> màn tải quay mãi.
 *
 * Ở đây:
 * - {@link #giai} bỏ mọi thứ tạo lại được (banner màn đăng nhập ~0,9 MB ARGB, ảnh phủ mờ, ảnh phụ của font, bộ đệm
 *   sprite Pokémon) rồi gom rác; gọi khi gặp OutOfMemoryError (Session_ME: cấp phát gói / xử lý gói - rồi thử lại)
 *   và trước khi nhận gói lớn mà heap còn trống ít.
 * - Từ lần thiếu bộ nhớ đầu tiên (hoặc máy J2ME heap &lt; 12 MB) vào chế độ {@link #tietKiem}: không nạp lại banner, bộ
 *   đệm sprite Pokémon nhỏ.
 * - {@link #vaoTheGioi}: vào thế giới thì bỏ banner (chỉ màn đăng nhập dùng; trước đây nằm lại cả phiên).
 */
public final class BoNho {
    private BoNho() {
    }

    /** Máy J2ME thật / giả lập (không có java.util.zip như PC / Android). */
    public static final boolean laJ2me = (network.Session_ME.khaNang() & network.Session_ME.KHA_NANG_NEN) == 0;
    /** Chế độ tiết kiệm bộ nhớ: không banner, bộ đệm sprite nhỏ. */
    public static volatile boolean tietKiem;
    /** số lần đã phải giải phóng vì thiếu bộ nhớ (kiểm thử / nhật ký) */
    public static int soLanGiai;
    public static String lyDoCuoi;
    /** Ngưỡng heap của máy J2ME coi là nhỏ ngay từ đầu. */
    static final long HEAP_NHO = 12L << 20;
    /** Bộ đệm sprite Pokémon ở chế độ tiết kiệm (điểm ảnh, ~480 KB ARGB). */
    static final int PIXEL_TIET_KIEM = 120000;

    static {
        try {
            if (laJ2me && Runtime.getRuntime().totalMemory() < HEAP_NHO) {
                batTietKiem();
            }
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    private static void batTietKiem() {
        tietKiem = true;
        if (PokeBody.ToiDaPixel > PIXEL_TIET_KIEM) {
            PokeBody.ToiDaPixel = PIXEL_TIET_KIEM;
        }
    }

    /** Bỏ mọi thứ tạo lại được + gom rác (thiếu bộ nhớ). */
    public static void giai(String lyDo) {
        soLanGiai++;
        lyDoCuoi = lyDo;
        try {
            batTietKiem();
            Banner.giaiPhong();
            KieuO.giaiPhong();
            game.render.screen.Font.giaiPhong();
            PokeBody.xoaBoDem();
        } catch (Throwable e) {
            // giải phóng không được thì thôi
        }
        System.gc();
        try {
            GiamSat.ghi("thieu bo nho (" + lyDo + "): da giai phong, con trong " + (Runtime.getRuntime().freeMemory() >> 10) + "/"
                    + (Runtime.getRuntime().totalMemory() >> 10) + " KB");
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    /**
     * Trước khi cấp phát gói n byte (luồng mạng): heap J2ME còn trống ít hơn ~3 lần gói (gói + dữ liệu dựng ra từ gói
     * + dư) thì giải phóng trước - đỡ một lần OutOfMemoryError giữa chừng.
     */
    public static void truocGoi(int n) {
        if (!laJ2me || n < 32768) {
            return;
        }
        try {
            Runtime r = Runtime.getRuntime();
            if (r.freeMemory() < 3L * n) {
                System.gc();
                if (r.freeMemory() < 3L * n) {
                    giai("truoc goi " + (n >> 10) + " KB");
                }
            }
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    /** Đã vào thế giới: bỏ banner màn đăng nhập (máy J2ME). */
    public static void vaoTheGioi() {
        if (laJ2me) {
            try {
                Banner.giaiPhong();
            } catch (Throwable e) {
                // bỏ qua
            }
        }
    }
}
