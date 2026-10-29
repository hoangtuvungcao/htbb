package game.pokemon;

/**
 * Logic thuần (không lcdui) của bảng gán chiêu vào thanh ô nhanh (ChonChieuNhanh): chiêu nào gán được,
 * bố cục bảng, ô nào được chạm. Tách riêng để kiểm thử không cần máy (client_jar/test/KiemOChieuNhanh.java).
 *
 * Thanh ô nhanh chỉ lưu ở máy (RMS "nqshQuickSlot"), server không lưu -> không có gói tin đồng bộ.
 */
public final class OChieuNhanh {
    /** số ô của một bộ ô nhanh (phím 1, 3, 5, 7, 9) */
    public static final int SO_O = 5;
    /** giữ lâu (ms) trên một ô = mở bảng chỉnh ô (đổi chiêu / bỏ trống / đổi chỗ) */
    public static final long GIU_LAU_MS = 500;

    private OChieuNhanh() {
    }

    /** Chiêu bổ trợ (buff) theo dòng - như WindowInfoScr.isBuffSkill. */
    public static boolean laBuff(int lop, int chieu) {
        if (lop == 2) {
            return chieu >= 4 && chieu <= 7;
        }
        return chieu == 4 || chieu == 5;
    }

    /**
     * Danh sách chiêu gán vào ô nhanh được: đã học (cấp > 0), trong bảng chiêu của dòng; chiêu bổ trợ chỉ dùng cho
     * bản thân ngay (SKILL_CAN_BUFF_TO_USER == -1) thì không gán (bảng kỹ năng của jar cũng không cho).
     */
    public static int[] ganDuoc(int lop, byte[] capHoc, byte[][] buffChoMinh, int soChieu) {
        if (capHoc == null || lop < 0) {
            return new int[0];
        }
        int n = Math.min(soChieu, capHoc.length);
        int[] tam = new int[Math.max(0, n)];
        int dem = 0;
        for (int sk = 0; sk < n; sk++) {
            if (capHoc[sk] <= 0) {
                continue;
            }
            if (laBuff(lop, sk) && buffChoMinh != null && lop < buffChoMinh.length) {
                int i = sk - 4;
                if (i >= 0 && i < buffChoMinh[lop].length && buffChoMinh[lop][i] == -1) {
                    continue;
                }
            }
            tam[dem++] = sk;
        }
        int[] kq = new int[dem];
        System.arraycopy(tam, 0, kq, 0, dem);
        return kq;
    }

    /** chừa đáy màn cho thanh phím mềm (Chọn / Đóng) - bảng không đè */
    public static final int DAY_PHIM = 28;

    /**
     * Bố cục bảng (px logic) giữa màn W x H cho n chiêu: {x, y, w, h, soCot, soHang, hHang, yDau, hChan}.
     * Luôn nằm trọn trong màn (lề >= 4 px, không đè thanh phím mềm đáy), canh giữa.
     */
    public static int[] boCuc(int W, int H, int n) {
        int Hd = H - DAY_PHIM;
        int cot = W >= 400 ? 2 : 1;
        int hang = Math.max(1, (n + cot - 1) / cot);
        int hHang = Hd >= 280 ? 30 : 24;
        int hDau = 26;
        int hChan = 30;
        int duoi = 10;
        if (ChonChieuNhanh.moi()) {   // bảng chung (Bang.khung), hàng / nút đủ cỡ chạm - PC / APK / J2ME cảm ứng
            // 0.9.7: hàng đủ cao cho 2 dòng (tên + hệ / cấp) đo theo font đang dùng (font ảnh J2ME cao hơn)
            hHang = Math.max(Math.max(34, VungCham.MIN + Bang.cach() + 2), ChuDe.cao(ChuDe.NHAN) + ChuDe.cao(ChuDe.PHU) + 6 + Bang.cach());
            hDau = ChuDe.caoDau() + ChuDe.DEM;
            hChan = Math.max(30, VungCham.MIN + 8);
            // khe hàng cuối -> nút chân >= khoảng cách chạm (hàng thụt k/2, nút chân lùi 2 px): 0.9.22 APK d3.5 hở 9 < 10
            duoi = Math.max(10, 10 + VungCham.CACH - (4 + Math.max(2, Bang.cach()) / 2));
        }
        int toiDaHang = Math.max(1, (Hd - 8 - hDau - hChan - duoi) / hHang);
        if (hang > toiDaHang) {
            hang = toiDaHang;
        }
        int w = Math.min(W - 8, cot == 2 ? 420 : 300);
        int h = hDau + hang * hHang + hChan + duoi;
        int x = (W - w) / 2;
        int y = Math.max(4, (Hd - h) / 2);
        return new int[]{x, y, w, h, cot, hang, hHang, y + hDau + 4, hChan};
    }

    /** Chỉ số chiêu (trong danh sách) tại điểm chạm (px, py) với cuộn hàng 'cuon'; -1 = không trúng. */
    public static int chiSoTai(int[] bc, int n, int cuon, int px, int py) {
        int x = bc[0], w = bc[2], cot = bc[4], hang = bc[5], hHang = bc[6], yDau = bc[7];
        if (px < x + 6 || px >= x + w - 6 || py < yDau || py >= yDau + hang * hHang) {
            return -1;
        }
        int wCot = (w - 12) / cot;
        int c = Math.min(cot - 1, (px - x - 6) / wCot);
        int r = (py - yDau) / hHang + cuon;
        int i = r * cot + c;
        return i >= 0 && i < n ? i : -1;
    }

    /** Số hàng cuộn tối đa. */
    public static int cuonToiDa(int[] bc, int n) {
        int tong = (n + bc[4] - 1) / bc[4];
        return Math.max(0, tong - bc[5]);
    }

    /** Nút ở chân bảng: {x, y, w, h} của nút thứ i trong k nút (chia đều). */
    public static int[] nutChan(int[] bc, int i, int k) {
        int x = bc[0] + 8, w = bc[2] - 16, y = bc[1] + bc[3] - bc[8] - 2, h = bc[8] - 6;
        int gap = Bang.cach();   // 0.9.10: cả J2ME (trước 4 px cố định: nút chạm sát nhau trên J2ME cảm ứng)
        int wn = (w - gap * (k - 1)) / k;
        return new int[]{x + i * (wn + gap), y, wn, h};
    }

    public static boolean trong(int[] r, int px, int py) {
        return px >= r[0] && px < r[0] + r[2] && py >= r[1] && py < r[1] + r[3];
    }

    /** Đổi chỗ 2 ô (chỉ số) trong mảng kiểu ô / mã - dùng chung cho đổi chỗ QuickSlot. */
    public static void doiCho(Object[] o, int a, int b) {
        if (o == null || a == b || a < 0 || b < 0 || a >= o.length || b >= o.length) {
            return;
        }
        Object t = o[a];
        o[a] = o[b];
        o[b] = t;
    }

    /** Thời gian hồi chiêu dạng ngắn: 800 -> "0.8s", 12000 -> "12s". */
    public static String giay(long ms) {
        if (ms <= 0) {
            return "0s";
        }
        if (ms < 10000) {
            long d = (ms + 50) / 100;
            return (d / 10) + "." + (d % 10) + "s";
        }
        return ((ms + 500) / 1000) + "s";
    }
}
