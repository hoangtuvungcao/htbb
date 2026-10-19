package game.pokemon;

import network.Message;
import java.util.Hashtable;

/**
 * Khoá bản đồ + Tự động đánh theo bản đồ (server gói -106, real.KhoaBanDoGame). Server gửi các bản đồ KHÁC mặc định:
 * đang khoá với người chơi (kèm điều kiện "Cần … để vào …") và / hoặc không cho Tự động đánh (kèm lý do). Mọi id,
 * gồm cả bản sao theo khu. Không có trong bảng = mở, cho tự động. Server vẫn là nơi quyết (chặn ở mọi đường vào).
 *
 * Gói: byte phiên bản (1), short n nhóm; mỗi nhóm byte cờ (1 khoá, 2 không tự động), UTF yêu cầu, UTF lý do tự động,
 * short k, k × short id bản đồ.
 */
public final class KhoaMap {
    public static final int LENH = -106;
    public static final int CO_KHOA = 1, CO_KHONG_TU_DONG = 2;
    /** Tiền tố chữ của hàng khoá trong menu (server dùng cùng chữ cho menu bay / đò): MenuNguCanh vẽ ổ khoá + xám. */
    public static final String TIEN_TO = "Khoá: ";

    /** id bản đồ -> {Integer cờ, String yêu cầu, String lý do tự động}. */
    private static Hashtable bang = new Hashtable();
    public static int soNhan;

    private KhoaMap() {
    }

    /** true = đã xử lý (lệnh -106). Gọi ở đầu MessageHandler.onMessage. */
    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            msg.reader().readByte();
            int n = msg.reader().readShort();
            Hashtable moi = new Hashtable();
            for (int i = 0; i < n; i++) {
                int co = msg.reader().readByte();
                String yc = msg.reader().readUTF();
                String td = msg.reader().readUTF();
                int k = msg.reader().readShort();
                Object[] v = new Object[]{new Integer(co), yc, td};
                for (int j = 0; j < k; j++) {
                    moi.put(new Integer(msg.reader().readShort()), v);
                }
            }
            bang = moi;
            soNhan++;
        } catch (Throwable e) {
            // gói hỏng: giữ bảng cũ
        }
        return true;
    }

    /** Test / công cụ: đặt một bản đồ. */
    public static void dat(int map, int co, String yeuCau, String lyDoTuDong) {
        bang.put(new Integer(map), new Object[]{new Integer(co), yeuCau, lyDoTuDong});
    }

    public static void xoa() {
        bang = new Hashtable();
    }

    private static Object[] lay(int map) {
        return (Object[]) bang.get(new Integer(map));
    }

    private static int co(int map) {
        Object[] v = lay(map);
        return v == null ? 0 : ((Integer) v[0]).intValue();
    }

    public static boolean khoa(int map) {
        return (co(map) & CO_KHOA) != 0;
    }

    public static boolean tuDongKhoa(int map) {
        return (co(map) & CO_KHONG_TU_DONG) != 0;
    }

    /** Điều kiện vào ("Cần … để vào …"), "" nếu mở. */
    public static String yeuCau(int map) {
        Object[] v = lay(map);
        return v == null || !khoa(map) ? "" : (String) v[1];
    }

    /** Câu báo khi Tự động đánh bị khoá ở bản đồ này, "" nếu cho phép. */
    public static String lyDoTuDong(int map) {
        Object[] v = lay(map);
        if (v == null || !tuDongKhoa(map)) {
            return "";
        }
        String l = (String) v[2];
        return "Bản đồ này không cho phép tự động đánh" + (l == null || l.length() == 0 ? "." : ": " + l);
    }

    /** Nhãn hàng điểm đến (cổng, danh sách bay): khoá -> "Khoá: <tên>", kèm nhãn AUTO. */
    public static String nhan(int map, String ten) {
        return (khoa(map) ? TIEN_TO : "") + ten + (tuDongKhoa(map) ? " · Không AUTO" : " · AUTO");
    }

    public static boolean laHangKhoa(String chu) {
        return chu != null && chu.startsWith(TIEN_TO);
    }

    /** Chạm điểm khoá: báo điều kiện, không gửi đổi map. */
    public static void baoKhoa(int map) {
        String s = yeuCau(map);
        DungDo.bao(s.length() > 0 ? s : "Bản đồ này đang khoá.", DungDo.TIN);
    }

    /** Chạm viên AUTO ở bản đồ không cho tự động: báo lý do. true = đã chặn. */
    public static boolean chanTuDong(int map) {
        if (!tuDongKhoa(map)) {
            return false;
        }
        DungDo.bao(lyDoTuDong(map), DungDo.TIN);
        return true;
    }
}
