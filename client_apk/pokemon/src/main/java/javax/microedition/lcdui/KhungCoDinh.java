package javax.microedition.lcdui;

/**
 * Khung hình PHỦ KÍN màn (0.9.3, thay hộp 16:9 + viền của 0.9.2) mà vẫn CÔNG BẰNG (chống lợi thế màn to / màn dài):
 *
 * - Tham chiếu 16:9: giao diện logic 640x360, thế giới phóng THE_GIOI_Z = 2 / NHIN_THEM (0.9.7: NHIN_THEM = 1,15 -
 *   camera "cao hơn", thấy thêm ~15% mỗi chiều) -> vùng nhìn ~368x207 px thế giới (~23 x 13 ô 16 px).
 * - Phóng PHỦ (cover) đều hai chiều: tiLe = max(w / 640, h / 360). Cỡ logic = ceil(w / tiLe) x ceil(h / tiLe), luôn
 *   <= 640 x 360 ở MỖI chiều -> không ai thấy nhiều thế giới hơn tham chiếu 16:9 theo bất kỳ chiều nào:
 *     20:9 (2400x1080)  -> tiLe 3,75, logic 640x288 (vùng nhìn 20 x 9 ô),
 *     4:3  (2048x1536)  -> tiLe 4,27, logic 480x360 (15 x 11,25 ô),
 *     16:9 (1920x1080)  -> tiLe 3, logic 640x360 đúng tham chiếu.
 * - Không viền / pillarbox: khung = cả màn (lechX = lechY = 0). Hàng px logic cuối có thể tràn mép < 1 px logic.
 * - Máy chủ vẫn kẹp cứng vùng nhìn (VungNhinCoDinh) - đây chỉ là trần phía client.
 * - Chạm: toạ độ màn thật -> logic qua cùng phép biến đổi (logic = that / tiLe).
 *
 * Thuần Java (không phụ thuộc Android) để kiểm thử không cần máy: client_apk/test/KiemKhungCoDinh.java.
 */
public final class KhungCoDinh {
    /** cỡ logic THAM CHIẾU (16:9) - trần mỗi chiều */
    public static final int LOGIC_W = 640;
    public static final int LOGIC_H = 360;
    /**
     * HẰNG SỐ DUY NHẤT của camera (0.9.7): vùng nhìn thế giới lớn hơn mốc cũ (320 x 180) bao nhiêu lần mỗi chiều. 1,15 =
     * thấy thêm ~15% bản đồ (chủ dự án: "nâng camera lên một chút"). Giao diện / vùng chạm không đổi.
     */
    public static final float NHIN_THEM = 1.15f;
    /** phóng thế giới so với giao diện logic (float; vẽ láng giềng gần nhất như trước - tiLe x 2 vốn không nguyên) */
    public static final float THE_GIOI_Z = 2f / NHIN_THEM;
    /** vùng nhìn tối đa (px thế giới) ở logic 640 x 360 */
    public static final int NHIN_W = nhinW(LOGIC_W);
    public static final int NHIN_H = nhinH(LOGIC_H);

    /** hệ số màn thật / px logic (đều hai chiều) */
    public final float tiLe;
    /** tiLe là số nguyên (ảnh pixel đều cạnh) */
    public final boolean nguyen;
    /** lệch khung trên màn thật: luôn 0 (phủ kín) - giữ trường cho chỗ cũ */
    public final int lechX, lechY;
    /** cỡ khung trên màn thật = cả màn */
    public final int khungW, khungH;
    public final int thatW, thatH;
    /** cỡ giao diện logic thực (<= LOGIC_W x LOGIC_H) */
    public final int logicW, logicH;

    private KhungCoDinh(int w, int h, float tiLe, int lw, int lh) {
        this.thatW = w;
        this.thatH = h;
        this.tiLe = tiLe;
        this.nguyen = Math.abs(tiLe - Math.round(tiLe)) < 1e-4f;
        this.logicW = lw;
        this.logicH = lh;
        this.khungW = w;
        this.khungH = h;
        this.lechX = 0;
        this.lechY = 0;
    }

    /** Khung cho vùng vẽ thật w x h (px). */
    public static KhungCoDinh tinh(int w, int h) {
        if (w <= 0 || h <= 0) return new KhungCoDinh(LOGIC_W, LOGIC_H, 1f, LOGIC_W, LOGIC_H);
        float t = Math.max(w / (float) LOGIC_W, h / (float) LOGIC_H);
        int lw = Math.min(LOGIC_W, Math.max(1, (int) Math.ceil(w / t - 1e-3f)));
        int lh = Math.min(LOGIC_H, Math.max(1, (int) Math.ceil(h / t - 1e-3f)));
        return new KhungCoDinh(w, h, t, lw, lh);
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

    /** Toạ độ màn thật -> logic nguyên, kẹp trong [0, logicW / logicH). */
    public int logicXKep(float x) {
        return kep((int) Math.floor(logicX(x)), logicW);
    }

    public int logicYKep(float y) {
        return kep((int) Math.floor(logicY(y)), logicH);
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

    /** Vùng nhìn thế giới (px thế giới) cho bề rộng / cao giao diện logic (logic <= 640 x 360 -> <= NHIN_W x NHIN_H). */
    public static int nhinW(int canvasW) {
        return (int) Math.ceil(canvasW / THE_GIOI_Z - 1e-3f);
    }

    public static int nhinH(int canvasH) {
        return (int) Math.ceil(canvasH / THE_GIOI_Z - 1e-3f);
    }

    /** Toạ độ chạm logic -> lệch thế giới so với camera (cmx + theGioi(px)), làm tròn xuống (cả số âm). */
    public static int theGioi(int p) {
        return (int) Math.floor(p / THE_GIOI_Z);
    }
}
