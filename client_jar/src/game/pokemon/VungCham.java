package game.pokemon;

import game.model.Command;
import game.model.IAction;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * VÙNG CHẠM chung (jar / APK / PC): MỘT nơi duy nhất quyết định cú chạm / bấm chuột trúng điều khiển nào.
 *
 * Trước đây mỗi màn tự so Canvas.px / py với toạ độ đoán lại từ code vẽ, nhiều điều khiển chỉ có phím (ô "Nhớ mật
 * khẩu", ô chọn ở màn đăng nhập...) -> chạm không ăn. Nay điều khiển ĐĂNG KÝ vùng của nó ngay lúc vẽ (cùng toạ độ,
 * cùng g.translate với hình vẽ) kèm hành động; Canvas.run gọi xuLy() một lần mỗi nhịp, TRƯỚC khi màn / hộp thoại /
 * menu cập nhật: cú chạm trúng vùng thì chạy hành động và "nuốt" cú chạm (màn không xử lý chồng lần nữa).
 *
 * - Lớp: 0 = màn, 1 = hộp thoại, 2 = menu (Canvas.paint đặt trước mỗi lớp). Lớp trên đang hiện là MODAL: chỉ vùng
 *   của lớp cao nhất được xét (hộp thoại mở thì chạm "xuyên" xuống màn không ăn).
 * - Trong một lớp, vùng đăng ký sau (vẽ đè lên) thắng; trùng nhau thì vùng có tâm gần điểm chạm hơn.
 * - Cỡ chạm tối thiểu MIN (px logic, nền đặt theo dpi: Android ~44 dp, PC ~32 px thật): vùng nhỏ hơn được nới đều
 *   quanh tâm (chỉ vùng chạm, hình vẽ giữ nguyên).
 * - Vùng TRÒN (themTron, nút tròn HUD): trúng theo khoảng cách tới tâm (bán kính nới tới MIN / 2) - góc vuông của
 *   hộp bao không "ăn" chạm của nút bên cạnh.
 * - Một cú chạm = nhấn (Canvas.pxLast / pyLast) và thả (Canvas.px / py) trong CÙNG một vùng (kéo ra ngoài = huỷ).
 * - Toạ độ: toạ độ jar (Canvas.px / py); APK đã đổi ngược phép phóng cửa sổ HD (HdPhong) trước khi vào game.
 *
 * Hai bộ đệm: vẽ (luồng sự kiện) ghi bộ "dang", hết khung thì chốt sang bộ "chot" mà xuLy (luồng game) đọc.
 */
public final class VungCham {
    /** cỡ chạm tối thiểu (px logic, mỗi cạnh) - nền (NenAndroid / NenPc) đặt theo dpi; 0 = không nới */
    public static int MIN = 0;
    /** khoảng cách tối thiểu khuyến nghị giữa hai vùng (px logic) - kiểm thử dùng */
    public static int CACH = 0;

    private static final int TOI_DA = 160;

    private static final class Bo {
        int n;
        final int[] x = new int[TOI_DA], y = new int[TOI_DA], w = new int[TOI_DA], h = new int[TOI_DA], lop = new int[TOI_DA];
        /** bán kính vùng tròn (0 = hộp chữ nhật) */
        final int[] r = new int[TOI_DA];
        final Object[] viec = new Object[TOI_DA];
        /** 0.9.6.1: vùng KÉO (thanh trượt): nhấn trong vùng thì thả ở đâu cũng chạy viec */
        final boolean[] keo = new boolean[TOI_DA];
        final String[] ten = new String[TOI_DA];
        int lopMax;
    }

    private static Bo dang = new Bo();
    private static Bo chot = new Bo();
    private static int lopHien;
    private static final Object KHOA = new Object();

    /**
     * Hành động đặc biệt cho điều khiển CŨ có phép thử chạm riêng (TField, danh sách...): trúng vùng (đã nới MIN) thì
     * chỉ kéo điểm chạm về TÂM vùng rồi để điều khiển tự xử lý như cũ (không nuốt) - vùng chạm đủ to mà không đổi
     * logic (vd ô nhập: chọn ô này, bỏ chọn ô khác, mở bàn phím hệ thống trên APK).
     */
    public static final Object TAM = new Object();

    /** số cú chạm đã xử lý (kiểm thử) */
    public static int soXuLy;
    /** tên vùng vừa trúng (kiểm thử / nhật ký) */
    public static String vuaTrung;

    private VungCham() {
    }

    /** Đầu Canvas.paint. */
    public static void khungMoi() {
        dang.n = 0;
        dang.lopMax = 0;
        lopHien = 0;
    }

    /** Lớp NỔI (0.9.5): thông báo / biểu ngữ không chặn - trúng thì thắng, không trúng thì cú chạm đi xuống lớp modal. */
    public static final int NOI = -1;

    /** Lớp đang đăng ký (để đặt lại sau lớp nổi). */
    public static int lopDangVe() {
        return lopHien;
    }

    /** Canvas.paint: 0 màn, 1 hộp thoại, 2 menu, NOI = nổi. */
    public static void lop(int l) {
        lopHien = l;
        if (l > dang.lopMax) {
            dang.lopMax = l;
        }
    }

    /** Cuối Canvas.paint: chốt vùng của khung vừa vẽ cho xuLy. */
    public static void khungXong() {
        synchronized (KHOA) {
            Bo t = chot;
            chot = dang;
            dang = t;
        }
    }

    /**
     * Đăng ký vùng chạm (x, y, w, h theo toạ độ đang vẽ của g - đã cộng g.translate) cho hành động viec:
     * IAction / Command (perform) / Runnable / Integer (mã phím J2ME gửi như bấm phím).
     */
    public static void them(Graphics g, int x, int y, int w, int h, Object viec, String ten) {
        if (viec == null || w <= 0 || h <= 0) {
            return;
        }
        int tx = g == null ? 0 : g.getTranslateX();
        int ty = g == null ? 0 : g.getTranslateY();
        themTuyetDoi(x + tx, y + ty, w, h, viec, ten);
    }

    public static void them(Graphics g, int x, int y, int w, int h, Object viec) {
        them(g, x, y, w, h, viec, null);
    }

    /**
     * 0.9.6.1: vùng KÉO (thanh trượt): giống them, nhưng một cú nhấn BẮT ĐẦU trong vùng thì lúc thả luôn chạy viec
     * (kéo quá đầu / cuối thanh hay lệch lên xuống vẫn tính - viec đọc điểm thả Canvas.px).
     */
    public static void themKeo(Graphics g, int x, int y, int w, int h, Object viec, String ten) {
        int n = dang.n;
        them(g, x, y, w, h, viec, ten);
        if (dang.n > n) {
            dang.keo[n] = true;
        }
    }

    /** Điểm (px, py) nằm trong vùng (x, y, w, h) tuyệt đối đã nới MIN (đúng như vùng chạm thật). */
    public static boolean trongVung(int x, int y, int w, int h, int px, int py) {
        int[] r = noi(x, y, w, h);
        return px >= r[0] && py >= r[1] && px < r[0] + r[2] && py < r[1] + r[3];
    }

    /** Đăng ký vùng theo toạ độ màn jar tuyệt đối. */
    public static void themTuyetDoi(int x, int y, int w, int h, Object viec, String ten) {
        Bo b = dang;
        if (b.n >= TOI_DA) {
            return;
        }
        int i = b.n++;
        b.x[i] = x;
        b.y[i] = y;
        b.w[i] = w;
        b.h[i] = h;
        b.lop[i] = lopHien;
        b.viec[i] = viec;
        b.ten[i] = ten;
        b.r[i] = 0;
        b.keo[i] = false;
    }

    /** Đăng ký vùng TRÒN tâm (cx, cy) bán kính r (toạ độ màn jar tuyệt đối) - hộp bao (cx - r, cy - r, 2r + 1). */
    public static void themTron(int cx, int cy, int r, Object viec, String ten) {
        if (viec == null || r <= 0) {
            return;
        }
        Bo b = dang;
        int i = b.n;
        themTuyetDoi(cx - r, cy - r, 2 * r + 1, 2 * r + 1, viec, ten);
        if (b.n > i) {
            b.r[i] = r;
        }
    }

    /** Bán kính vùng i (đã chốt) như đăng ký - 0 = hộp chữ nhật. */
    public static int banKinh(int i) {
        return chot.r[i];
    }

    /** Bán kính CHẠM thật của vùng tròn i (đã nới MIN / 2) - 0 = hộp. */
    public static int banKinhCham(int i) {
        int r = chot.r[i];
        return r <= 0 ? 0 : Math.max(r, (MIN + 1) / 2);
    }

    /** Vùng chạm thật (đã nới MIN) của vùng i trong bộ đã chốt: {x, y, w, h}. */
    public static int[] vung(int i) {
        Bo b = chot;
        return noi(b.x[i], b.y[i], b.w[i], b.h[i]);
    }

    /** Vùng như đã vẽ (chưa nới) của vùng i: {x, y, w, h, lop}. */
    public static int[] vungVe(int i) {
        Bo b = chot;
        return new int[]{b.x[i], b.y[i], b.w[i], b.h[i], b.lop[i]};
    }

    public static String ten(int i) {
        return chot.ten[i];
    }

    public static int lopVung(int i) {
        return chot.lop[i];
    }

    public static int lopCaoNhat() {
        return chot.lopMax;
    }

    public static int dem() {
        return chot.n;
    }

    private static int[] noi(int x, int y, int w, int h) {
        int m = MIN;
        if (w < m) {
            x -= (m - w) / 2;
            w = m;
        }
        if (h < m) {
            y -= (m - h) / 2;
            h = m;
        }
        return new int[]{x, y, w, h};
    }

    /**
     * Đáy thấp nhất (y + h) của các vùng đã chốt (khung trước) có tên bắt đầu bằng tienTo, giao dải ngang [x0, x1) và đỉnh
     * trên yMax; -1 = không có. DungDo dùng để đặt thông báo DƯỚI thẻ trạng thái / nút HUD (không chồng).
     */
    public static int duoiVung(String tienTo, int x0, int x1, int yMax) {
        synchronized (KHOA) {
            Bo b = chot;
            int r = -1;
            for (int i = 0; i < b.n; i++) {
                String t = b.ten[i];
                if (t == null || !t.startsWith(tienTo) || b.y[i] >= yMax) {
                    continue;
                }
                if (b.x[i] < x1 && x0 < b.x[i] + b.w[i]) {
                    r = Math.max(r, b.y[i] + b.h[i]);
                }
            }
            return r;
        }
    }

    /** Chỉ số vùng (bộ đã chốt) trúng điểm (x, y), -1 = không. */
    public static int tim(int px, int py) {
        synchronized (KHOA) {
            return timTrong(chot, px, py);
        }
    }

    private static int timTrong(Bo b, int px, int py) {
        for (int i = b.n - 1; i >= 0; i--) {
            // lớp nổi: chỉ trúng thật (không nới), thắng mọi lớp
            if (b.lop[i] == NOI && px >= b.x[i] && py >= b.y[i] && px < b.x[i] + b.w[i] && py < b.y[i] + b.h[i]) {
                return i;
            }
        }
        int tot = -1;
        long dTot = Long.MAX_VALUE;
        for (int i = b.n - 1; i >= 0; i--) {
            if (b.lop[i] != b.lopMax) {
                continue;
            }
            long d;
            if (b.r[i] > 0) {
                // vùng tròn: khoảng cách tới tâm (bán kính nới tới MIN / 2)
                long dx = px - (b.x[i] + b.r[i]), dy = py - (b.y[i] + b.r[i]);
                d = dx * dx + dy * dy;
                long rn = Math.max(b.r[i], (MIN + 1) / 2);
                if (d > rn * rn) {
                    continue;
                }
                if (d <= (long) b.r[i] * b.r[i]) {
                    return i;   // trúng thật
                }
                d *= 4;   // cùng thang với hộp (toạ độ x2) bên dưới
            } else {
                int[] r = noi(b.x[i], b.y[i], b.w[i], b.h[i]);
                if (px < r[0] || py < r[1] || px >= r[0] + r[2] || py >= r[1] + r[3]) {
                    continue;
                }
                // trúng thật (không cần nới) thắng ngay theo thứ tự vẽ (trên cùng trước)
                if (px >= b.x[i] && py >= b.y[i] && px < b.x[i] + b.w[i] && py < b.y[i] + b.h[i]) {
                    return i;
                }
                long dx = 2 * px - (2 * b.x[i] + b.w[i]);
                long dy = 2 * py - (2 * b.y[i] + b.h[i]);
                d = dx * dx + dy * dy;
            }
            if (d < dTot) {
                dTot = d;
                tot = i;
            }
        }
        return tot;
    }

    /**
     * Canvas.run, mỗi nhịp: có cú thả (isPointerClick) trúng vùng (cả lúc nhấn lẫn lúc thả) -> chạy hành động,
     * nuốt cú chạm, trả true.
     */
    public static boolean xuLy() {
        if (!Canvas.isPointerClick) {
            return false;
        }
        Object viec;
        String ten;
        synchronized (KHOA) {
            Bo b = chot;
            int iNhan = timTrong(b, Canvas.pxLast, Canvas.pyLast);
            int i;
            if (iNhan >= 0 && b.keo[iNhan]) {
                i = iNhan;      // nhấn trong thanh trượt: thả ở đâu cũng là "xong kéo"
            } else {
                i = timTrong(b, Canvas.px, Canvas.py);
                if (i < 0) {
                    return false;
                }
                if (iNhan != i) {
                    return false;   // nhấn ở chỗ khác rồi kéo vào: không phải cú chạm vào điều khiển này
                }
            }
            viec = b.viec[i];
            ten = b.ten[i];
            if (viec == TAM) {
                int cx = b.x[i] + b.w[i] / 2, cy = b.y[i] + b.h[i] / 2;
                Canvas.px = cx;
                Canvas.py = cy;
                Canvas.pxLast = cx;
                Canvas.pyLast = cy;
                vuaTrung = ten;
                soXuLy++;
                return false;
            }
        }
        Canvas.isPointerClick = false;
        Canvas.isPointerRelease = false;
        Canvas.isPointerJustRelease = false;
        Canvas.isPointerDown = false;
        vuaTrung = ten;
        soXuLy++;
        chay(viec);
        return true;
    }

    /** Chạy hành động của vùng. */
    public static void chay(Object viec) {
        if (viec instanceof IAction) {
            ((IAction) viec).perform();
        } else if (viec instanceof Command) {
            Command c = (Command) viec;
            if (c.action != null) {
                c.action.perform();
            }
        } else if (viec instanceof Runnable) {
            ((Runnable) viec).run();
        } else if (viec instanceof Integer) {
            int ma = ((Integer) viec).intValue();
            Canvas.instance.keyPressed(ma);
            Canvas.instance.keyReleased(ma);
        }
    }
}
