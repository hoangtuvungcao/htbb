package game.pokemon;

import game.model.Quest;
import game.render.Canvas;
import game.render.screen.GameScr;

/**
 * GIÁM SÁT LỚP MODAL (0.9.4) - lưới an toàn chống "treo màn hình": Canvas.run gọi tick() mỗi nhịp.
 *
 * - Hộp thoại (Canvas.currentDialog) mà khung hình vẫn đang vẽ nhưng KHÔNG vẽ được hộp đó (paint ném lỗi / không có
 *   chủ) suốt 2 giây -> gỡ hộp, ghi nhật ký. Máy đang ẩn (không có khung nào) thì không xét.
 * - Hội thoại nhiệm vụ (Quest.isShow) không còn gì hiện trên màn (không hộp đáy, không hộp xác nhận, không menu) quá
 *   2 giây -> đóng hội thoại (updateKey của màn chơi thoát sớm khi isShow: người chơi sẽ không đi được).
 * - Menu chữ đang mở mà không có mục nào -> tắt.
 * - Lỗi trong gói tin / nhịp game / vẽ: loi() ghi nhật ký thay vì giết luồng.
 */
public final class GiamSat {
    public static final long CHO = 2000L;

    private static Object hop;
    private static long hopVeLuc;
    private static long khungLuc;
    private static long questLuc;

    /** số lần đã gỡ lớp kẹt (kiểm thử) + lời nhật ký cuối */
    public static int soLanGo;
    public static String nhatKyCuoi;
    public static int soLoi;

    private GiamSat() {
    }

    /** Canvas.paint: hộp thoại d vừa vẽ xong không lỗi. */
    public static void daVeHop(Object d) {
        hop = d;
        hopVeLuc = System.currentTimeMillis();
    }

    /** Canvas.paint: hết một khung hình. */
    public static void xongKhung() {
        khungLuc = System.currentTimeMillis();
    }

    public static void ghi(String s) {
        nhatKyCuoi = s;
        System.out.println("[GiamSat] " + s);
    }

    /** Lỗi bắt được ở luồng mạng / game / vẽ: ghi lại, không ném tiếp. */
    public static void loi(String noi, Throwable e) {
        soLoi++;
        nhatKyCuoi = "loi " + noi + ": " + e;
        if (soLoi <= 20 || soLoi % 200 == 0) {   // không tràn nhật ký khi lỗi lặp mỗi nhịp
            ghi(nhatKyCuoi + " (lan " + soLoi + ")");
            if (soLoi <= 20) {
                e.printStackTrace();
            }
        }
    }

    private static long loiTu;

    /** Canvas.run: một nhịp chạy hết không lỗi. */
    public static void nhipOk() {
        loiTu = 0;
    }

    /**
     * Canvas.run: nhịp ném lỗi. Lỗi liên tục quá 2 giây (thường là update của hộp thoại / menu đang mở hỏng) -> gỡ hộp
     * thoại và menu để người chơi điều khiển lại được.
     */
    public static void loiNhip(Throwable e) {
        loi("nhip game", e);
        long t = System.currentTimeMillis();
        if (loiTu == 0) {
            loiTu = t;
        } else if (t - loiTu > CHO) {
            ghi("nhip loi lien tuc " + (t - loiTu) + " ms: go hop thoai " + ten(Canvas.currentDialog) + " / menu");
            soLanGo++;
            Canvas.currentDialog = null;
            if (Canvas.menu != null) {
                Canvas.menu.showMenu = false;
            }
            loiTu = 0;
        }
    }

    static String ten(Object o) {
        return o == null ? "-" : o.getClass().getName();
    }

    public static void tick() {
        long t = System.currentTimeMillis();
        boolean dangVe = khungLuc > 0 && t - khungLuc < 500;
        Object d = Canvas.currentDialog;
        if (d == null) {
            hop = null;
        } else if (d != hop) {
            hop = d;
            hopVeLuc = t;
        } else if (dangVe && t - hopVeLuc > CHO) {
            ghi("go hop thoai khong ai ve duoc " + CHO + " ms: " + ten(d));
            soLanGo++;
            if (Canvas.currentDialog == d) {
                Canvas.currentDialog = null;
            }
            hop = null;
        }
        if (Canvas.menu != null && Canvas.menu.showMenu && (Canvas.menu.menuItems == null || Canvas.menu.menuItems.size() == 0)) {
            ghi("tat menu rong");
            soLanGo++;
            Canvas.menu.showMenu = false;
        }
        Quest q = GameScr.currQuest;
        boolean moCoi = q != null && q.isShow && Canvas.currentScreen == Canvas.gameScr && Canvas.currentDialog == null
                && (Canvas.menu == null || !Canvas.menu.showMenu) && !HopThoaiDuoi.dangBat();
        if (!moCoi) {
            questLuc = 0;
        } else if (questLuc == 0) {
            questLuc = t;
        } else if (t - questLuc > CHO) {
            ghi("dong hoi thoai nhiem vu khong con hien: " + q.name);
            soLanGo++;
            q.dong();
            questLuc = 0;
        }
    }
}
