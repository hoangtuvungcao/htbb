package htbb.pc;

import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Kiểm tra chồng chữ tự động (-Dhtbb.lenh tệp lệnh "kiem <tệp.txt>"): ghi hình chữ nhật mọi chữ HD của MỘT khung
 * game (toạ độ thật, đã cắt theo vùng clip), rồi báo cặp chữ đè nhau (giao > 25% chữ nhỏ hơn) và chữ ra ngoài
 * cửa sổ / bị cắt mất. Chỉ chữ (ChuHD) - khung / ảnh của game không tính.
 */
public final class KiemChong {
    static volatile boolean bat;
    private static ArrayList<Object[]> ds = new ArrayList<Object[]>();
    private static volatile String raTep;
    private static volatile long khungBatDau = -1;

    private KiemChong() {
    }

    static synchronized void ghi(String s, Rectangle r, Rectangle clip, boolean theGioi) {
        for (Object[] o : ds) {
            if (o[0] != null && o[0].equals(s) && o[1].equals(r)) return;   // cùng chữ vẽ lại (2 khung)
        }
        ds.add(new Object[]{s, r, clip, Boolean.valueOf(theGioi)});
    }

    /** Bảng / hộp (GiaoDienHD) vẽ đè: chữ vẽ TRƯỚC nó mà nằm trong nó coi như bị che (không tính chồng). */
    static synchronized void ghiKhung(Rectangle r) {
        if (bat) ds.add(new Object[]{null, r, null, Boolean.FALSE});
    }

    private static boolean biChe(int i, int j) {
        Rectangle r = (Rectangle) ds.get(i)[1];
        for (int k = i + 1; k < j; k++) {
            if (ds.get(k)[0] == null && ((Rectangle) ds.get(k)[1]).intersects(r)) return true;
        }
        return false;
    }

    /** Yêu cầu kiểm khung kế tiếp, ghi báo cáo vào tep. */
    private static volatile boolean cho;

    static synchronized void yeuCau(String tep) {
        raTep = tep;
        cho = true;   // bắt đầu ghi từ ĐẦU khung kế tiếp (đúng một khung)
        bat = true;
    }

    static boolean dangCho() {
        return cho || bat;
    }

    /** Cuối khung game (NenPc): có yêu cầu thì viết báo cáo. */
    static synchronized void chotKhung() {
        if (cho) {
            cho = false;
            ds = new ArrayList<Object[]>();
            khungBatDau = javax.microedition.lcdui.NenPc.soKhung;
            return;
        }
        if (!bat) return;
        bat = false;
        int W = javax.microedition.lcdui.NenPc.thatW, H = javax.microedition.lcdui.NenPc.thatH;
        StringBuilder b = new StringBuilder();
        int chong = 0, ngoai = 0;
        Rectangle man = new Rectangle(0, 0, W, H);
        int soChu = 0;
        for (int i = 0; i < ds.size(); i++) {
            String s = (String) ds.get(i)[0];
            if (s == null) continue;
            soChu++;
            Rectangle r = (Rectangle) ds.get(i)[1];
            Rectangle c = (Rectangle) ds.get(i)[2];
            if (!r.intersects(man)) continue;                 // ngoài màn hẳn (bị bỏ qua khi vẽ) - không lỗi
            boolean catCoY = c != null && man.contains(c);    // vùng cắt nằm trong màn = cắt có chủ ý (chữ chạy, danh sách cuộn)
            if (!man.contains(r) && !catCoY) {
                ngoai++;
                b.append("NGOAI  ").append(s).append(" ").append(r).append('\n');
            }
            for (int j = i + 1; j < ds.size(); j++) {
                if (ds.get(j)[0] == null) continue;
                if (!ds.get(i)[3].equals(ds.get(j)[3])) continue;   // chữ thế giới (tên, bóng chat) nằm DƯỚI giao diện: không tính
                if (biChe(i, j)) continue;
                Rectangle q = (Rectangle) ds.get(j)[1];
                Rectangle cq = (Rectangle) ds.get(j)[2];
                Rectangle rv = c == null ? r : r.intersection(c);      // phần THẤY được (sau vùng cắt)
                Rectangle qv = cq == null ? q : q.intersection(cq);
                if (rv.isEmpty() || qv.isEmpty()) continue;
                Rectangle x = rv.intersection(qv);
                if (x.isEmpty()) continue;
                int nho = Math.min(r.width * r.height, q.width * q.height);
                if (x.width * x.height * 4 > nho && !s.equals(ds.get(j)[0])) {
                    chong++;
                    b.append("CHONG  '").append(s).append("' x '").append(ds.get(j)[0]).append("' ").append(x).append('\n');
                }
            }
        }
        String tenMan = game.render.Canvas.currentScreen == null ? "?" : game.render.Canvas.currentScreen.getClass().getSimpleName();
        String dau = "man=" + tenMan + " dlg=" + (game.render.Canvas.currentDialog == null ? "-" : game.render.Canvas.currentDialog.getClass().getSimpleName())
                + " menu=" + game.render.Canvas.menu.showMenu + " cua_so=" + W + "x" + H + " chu=" + soChu
                + " chong=" + chong + " ngoai_hoac_cat=" + ngoai + "\n";
        try {
            java.nio.file.Files.write(new java.io.File(raTep).toPath(), (dau + b).getBytes("UTF-8"));
        } catch (Throwable e) {
            NhatKy.loi("kiem chong", e);
        }
        NhatKy.ghi("kiem chong: " + dau.trim());
    }
}
