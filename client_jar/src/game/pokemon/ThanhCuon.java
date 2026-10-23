package game.pokemon;

/**
 * Kéo NÚM thanh cuộn dọc (0.9.11) - phép tính chung cho mọi danh sách có thanh cuộn (Cuon / Bang.danhSach, menu cũ,
 * bảng gán chiêu, màn ghi công ...).
 *
 * Lỗi 0.9.10: nhấn lên núm / rãnh thanh cuộn rồi kéo bị coi như kéo NỘI DUNG (vuốt ngón): kéo núm lên thì danh sách
 * cuộn xuống, kéo xuống thì cuộn lên (ngược), lại không tỉ lệ với rãnh. Đúng: núm đi theo con trỏ, nội dung theo tỉ lệ
 * (núm xuống = cuộn xuống = hàng sau); chạm rãnh ngoài núm = nhảy để tâm núm tới chỗ chạm rồi kéo tiếp như núm.
 * Kéo trên nội dung giữ chiều tự nhiên (theo ngón), lăn chuột không đổi.
 */
public final class ThanhCuon {
    /** nới vùng chạm sang trái / phải hình thanh (px logic) - thanh chỉ rộng 3..6 px */
    public static final int NOI_TRAI = 6, NOI_PHAI = 4;

    private ThanhCuon() {
    }

    /** Cao núm: rãnh hRanh, thấy hThay trên tổng tong (tối thiểu toiThieu). */
    public static int caoNum(int hRanh, int hThay, int tong, int toiThieu) {
        int hb = (int) ((long) hRanh * hThay / Math.max(1, tong));
        if (hb < toiThieu) {
            hb = toiThieu;
        }
        return Math.min(hb, hRanh);
    }

    /** Đỉnh núm khi đang cuộn cuon / max. */
    public static int yNum(int yRanh, int hRanh, int hNum, int cuon, int max) {
        if (max <= 0) {
            return yRanh;
        }
        int c = Math.max(0, Math.min(max, cuon));
        return yRanh + (int) ((long) (hRanh - hNum) * c / max);
    }

    /** (px, py) trúng thanh vẽ ở cột x..x+w, rãnh y..y+h (vùng chạm nới NOI_TRAI / NOI_PHAI ngang, 2 px dọc). */
    public static boolean trung(int px, int py, int x, int w, int y, int h) {
        return h > 0 && px >= x - NOI_TRAI && px < x + w + NOI_PHAI && py >= y - 2 && py < y + h + 2;
    }

    /** py nằm trên núm (không phải rãnh). */
    public static boolean trenNum(int py, int yRanh, int hRanh, int hNum, int cuon, int max) {
        int yn = yNum(yRanh, hRanh, hNum, cuon, max);
        return py >= yn && py < yn + hNum;
    }

    /**
     * Kéo núm: bắt đầu ở pyDau khi cuộn cuonDau, con trỏ giờ ở py -> cuộn mới (kẹp 0..max). Núm đi đúng py - pyDau px,
     * nội dung đi (py - pyDau) * max / (hRanh - hNum) px - CÙNG chiều con trỏ.
     */
    public static int keo(int cuonDau, int pyDau, int py, int hRanh, int hNum, int max) {
        int du = hRanh - hNum;
        if (max <= 0 || du <= 0) {
            return Math.max(0, Math.min(max, cuonDau));
        }
        long c = cuonDau + (long) (py - pyDau) * max / du;
        return (int) Math.max(0, Math.min(max, c));
    }

    /** Chạm rãnh ở py: cuộn để tâm núm tới py (kẹp 0..max). */
    public static int toi(int py, int yRanh, int hRanh, int hNum, int max) {
        int du = hRanh - hNum;
        if (max <= 0 || du <= 0) {
            return 0;
        }
        long c = (long) (py - yRanh - hNum / 2) * max / du;
        return (int) Math.max(0, Math.min(max, c));
    }
}
