package javax.microedition.lcdui;

/**
 * Khung hình CỐ ĐỊNH (quyết định chủ game - chống lợi thế màn to): mọi máy thấy cùng một vùng thế giới,
 * không phóng to / thu nhỏ.
 *
 * - Giao diện logic luôn 640x360 (LOGIC_W x LOGIC_H).
 * - Thế giới phóng đúng THE_GIOI_Z = 2 so với logic -> vùng nhìn 320x180 px thế giới (20 x 11,25 ô 16 px).
 * - Cả khung phóng hệ số NGUYÊN lên màn thật khi hệ số nguyên còn lấp >= NGUONG_NGUYEN cạnh vừa nhất
 *   (720p x2, 1080p x3, 1440p x4 ...); không thì hệ số lẻ (vẽ láng giềng gần nhất). Phần dư = viền
 *   theo chủ đề (letterbox / pillarbox), không phải đen.
 * - Chạm: toạ độ màn thật -> logic qua cùng phép biến đổi (logic = (that - lech) / tiLe).
 *
 * Thuần Java (không phụ thuộc Android) để kiểm thử không cần máy: client_apk/test/KiemKhungCoDinh.java.
 */
public final class KhungCoDinh {
    public static final int LOGIC_W = 640;
    public static final int LOGIC_H = 360;
    /** phóng thế giới so với giao diện logic: vùng nhìn = LOGIC / 2 = 320 x 180 px thế giới */
    public static final int THE_GIOI_Z = 2;
    public static final int NHIN_W = LOGIC_W / THE_GIOI_Z;
    public static final int NHIN_H = LOGIC_H / THE_GIOI_Z;
    /** hệ số nguyên được dùng khi lấp ít nhất tỉ lệ này của hệ số vừa nhất (còn lại: hệ số lẻ) */
    public static final float NGUONG_NGUYEN = 0.8f;

    /** hệ số màn thật / px logic */
    public final float tiLe;
    /** hệ số nguyên (true) hay lẻ */
    public final boolean nguyen;
    /** lệch khung trên màn thật (px thật) */
    public final int lechX, lechY;
    /** cỡ khung trên màn thật (px thật) */
    public final int khungW, khungH;
    public final int thatW, thatH;

    private KhungCoDinh(int w, int h, float tiLe, boolean nguyen) {
        this.thatW = w;
        this.thatH = h;
        this.tiLe = tiLe;
        this.nguyen = nguyen;
        this.khungW = Math.round(LOGIC_W * tiLe);
        this.khungH = Math.round(LOGIC_H * tiLe);
        this.lechX = (w - khungW) / 2;
        this.lechY = (h - khungH) / 2;
    }

    /** Khung cho vùng vẽ thật w x h (px). */
    public static KhungCoDinh tinh(int w, int h) {
        if (w <= 0 || h <= 0) return new KhungCoDinh(LOGIC_W, LOGIC_H, 1f, true);
        float vua = Math.min(w / (float) LOGIC_W, h / (float) LOGIC_H);
        int k = (int) Math.floor(vua + 1e-4f);
        if (k >= 1 && k >= vua * NGUONG_NGUYEN) return new KhungCoDinh(w, h, k, true);
        return new KhungCoDinh(w, h, vua, false);
    }

    /** Hệ số nguyên gần nhất (>= 1) - cho chỗ cũ cần số nguyên (làm tròn điểm ảnh, cỡ nút theo dpi). */
    public int heSoNguyen() {
        return Math.max(1, Math.round(tiLe));
    }

    /** Toạ độ màn thật -> logic (float, không kẹp). */
    public float logicX(float x) {
        return (x - lechX) / tiLe;
    }

    public float logicY(float y) {
        return (y - lechY) / tiLe;
    }

    /** Toạ độ màn thật -> logic nguyên, kẹp trong [0, LOGIC). */
    public int logicXKep(float x) {
        return kep((int) Math.floor(logicX(x)), LOGIC_W);
    }

    public int logicYKep(float y) {
        return kep((int) Math.floor(logicY(y)), LOGIC_H);
    }

    /** true = điểm màn thật nằm trong khung game (không phải viền). */
    public boolean trongKhung(float x, float y) {
        return x >= lechX && y >= lechY && x < lechX + khungW && y < lechY + khungH;
    }

    /** Vùng an toàn thật (px màn, tính từ mép màn) -> px logic tính từ mép KHUNG (phần nằm trong viền bỏ đi). */
    public int[] anToanLogic(int trai, int tren, int phai, int duoi) {
        return new int[]{
                tran(Math.max(0, trai - lechX)),
                tran(Math.max(0, tren - lechY)),
                tran(Math.max(0, phai - (thatW - lechX - khungW))),
                tran(Math.max(0, duoi - (thatH - lechY - khungH)))};
    }

    private int tran(int px) {
        return px <= 0 ? 0 : (int) Math.ceil(px / tiLe - 1e-4f);
    }

    private static int kep(int v, int max) {
        return v < 0 ? 0 : v >= max ? max - 1 : v;
    }

    /** Vùng nhìn thế giới (px thế giới) cho bề rộng / cao giao diện logic: luôn 320 x 180 khi logic 640 x 360. */
    public static int nhinW(int canvasW) {
        return (canvasW + THE_GIOI_Z - 1) / THE_GIOI_Z;
    }

    public static int nhinH(int canvasH) {
        return (canvasH + THE_GIOI_Z - 1) / THE_GIOI_Z;
    }

    /** Toạ độ chạm logic -> lệch thế giới so với camera (cmx + theGioi(px)). */
    public static int theGioi(int p) {
        return p >= 0 ? p / THE_GIOI_Z : -((-p + THE_GIOI_Z - 1) / THE_GIOI_Z);
    }
}
