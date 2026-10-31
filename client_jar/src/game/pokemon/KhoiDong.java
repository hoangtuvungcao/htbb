package game.pokemon;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * CHẨN ĐOÁN KHỞI ĐỘNG (0.9.7.2, giả lập iOS CoffeeCat kẹt "Đang tải dữ liệu game..." trước khi nối mạng).
 *
 * Ghi lại bước khởi động đang chạy (GameMidlet.load, Canvas), pha của nhịp game (Canvas.run) và lỗi ĐẦU TIÊN (tên
 * bước + lớp ngoại lệ + heap). Màn tải khởi động vẽ một dòng chẩn đoán (phông hệ thống, không phụ thuộc phông ảnh
 * của game) khi có lỗi hoặc khởi động quá 10 giây - người chơi chụp màn hình gửi lại được. Luồng canh gọi repaint()
 * mỗi giây nếu không có khung nào (nhịp game kẹt / chết), để dòng chẩn đoán vẫn hiện. Lỗi gửi về máy chủ khi nối được
 * (Session_ME.baoLoiTai). KHÔNG dùng số thực (CLDC 1.0).
 */
public final class KhoiDong {
    private KhoiDong() {
    }

    public static final long T0 = System.currentTimeMillis();
    /** bước khởi động đang chạy / đã xong */
    public static volatile String buoc = "bat dau";
    public static volatile long tuBuoc = T0;
    /** pha nhịp game đang chạy (Canvas.run) */
    public static volatile String pha = "-";
    public static volatile long nhipCuoi, khungCuoi;
    public static volatile int soNhip, soKhung, soLoi;
    /** lỗi đầu tiên (null = chưa) + lỗi cuối */
    public static volatile String loi, loiCuoi;
    public static volatile boolean xong;
    private static boolean daGui;
    private static Thread canh;

    public static void buoc(String b) {
        buoc = b;
        tuBuoc = System.currentTimeMillis();
    }

    public static void loi(String noi, Throwable e) {
        soLoi++;
        String s = noi + ": " + ten(e) + heap();
        loiCuoi = s;
        if (loi == null) {
            loi = s;
        }
        try {
            System.out.println("[KhoiDong] loi " + s);
        } catch (Throwable t) {
            // bỏ qua
        }
    }

    static String ten(Throwable e) {
        if (e == null) {
            return "?";
        }
        String n = e.getClass().getName();
        int k = n.lastIndexOf('.');
        String m = e.getMessage();
        return (k >= 0 ? n.substring(k + 1) : n) + (m != null && m.length() > 0 ? "(" + (m.length() > 70 ? m.substring(0, 70) : m) + ")" : "");
    }

    static String heap() {
        try {
            Runtime r = Runtime.getRuntime();
            return " [" + (r.freeMemory() >> 10) + "/" + (r.totalMemory() >> 10) + "KB]";
        } catch (Throwable e) {
            return "";
        }
    }

    /** Dòng chẩn đoán (bước + thời gian + pha nhịp + lỗi + heap). */
    public static String dong() {
        long t = System.currentTimeMillis();
        StringBuffer b = new StringBuffer();
        b.append("B:").append(buoc).append(' ').append((t - tuBuoc) / 1000).append("s");
        b.append(" N:").append(pha).append('#').append(soNhip);
        if (nhipCuoi > 0 && t - nhipCuoi > 3000) {
            b.append(" DUNG ").append((t - nhipCuoi) / 1000).append("s");
        }
        b.append(" K").append(soKhung).append(heap());
        if (loi != null) {
            b.append(" | ").append(loi);
            if (soLoi > 1 && loiCuoi != null && loiCuoi != loi) {
                b.append(" | x").append(soLoi).append(' ').append(loiCuoi);
            }
        }
        return b.toString();
    }

    /** Có nên hiện dòng chẩn đoán không: có lỗi, hoặc khởi động chưa xong sau 10 s. */
    public static boolean nenHien() {
        // 0.9.27+: loi da bat (game van chay) KHONG hien cho nguoi choi nua - chi hien khi khoi dong ket > 10 s
        // hoac nhip game dung > 5 s (treo that); loi van ghi vao dong() / System.out de chan doan.
        long t = System.currentTimeMillis();
        return !xong && t - T0 > 10000L || xong && nhipCuoi > 0 && t - nhipCuoi > 5000L;
    }

    /** Vẽ dòng chẩn đoán ở đáy màn (ngắt nhiều dòng), phông hệ thống. */
    public static void ve(Graphics g, int w, int h) {
        soKhung++;
        khungCuoi = System.currentTimeMillis();
        if (!nenHien()) {
            return;
        }
        try {
            g.setClip(0, 0, w, h);
            Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(f);
            String s = dong();
            int fh = f.getHeight();
            java.util.Vector ds = new java.util.Vector();
            int i = 0;
            while (i < s.length() && ds.size() < 8) {
                int j = i + 1;
                while (j < s.length() && f.substringWidth(s, i, j + 1 - i) < w - 6) {
                    j++;
                }
                ds.addElement(s.substring(i, j));
                i = j;
            }
            int cao = ds.size() * fh + 4;
            g.setColor(0x000000);
            g.fillRect(0, h - cao, w, cao);
            g.setColor(loi != null ? 0xFFD040 : 0xFFFFFF);
            for (int k = 0; k < ds.size(); k++) {
                g.drawString((String) ds.elementAt(k), 3, h - cao + 2 + k * fh, Graphics.TOP | Graphics.LEFT);
            }
        } catch (Throwable e) {
            // không vẽ được thì thôi
        }
    }

    /** Luồng canh: khởi động chưa xong mà không có khung nào trong 1,5 s -> repaint() (nhịp game kẹt / chết). */
    public static void batCanh(final javax.microedition.lcdui.Canvas c) {
        if (canh != null) {
            return;
        }
        try {
            canh = new Thread(new Runnable() {
                public void run() {
                    while (!xong || loi != null && System.currentTimeMillis() - T0 < 600000L) {
                        try {
                            Thread.sleep(1000L);
                        } catch (Throwable e) {
                            // bỏ qua
                        }
                        try {
                            if (System.currentTimeMillis() - khungCuoi > 1500L) {
                                c.repaint();
                            }
                        } catch (Throwable e) {
                            // bỏ qua
                        }
                    }
                }
            });
            canh.start();
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    /** Đã nối máy chủ (Session_ME có khoá): gửi lỗi khởi động một lần. */
    public static void guiNeuCo() {
        if (daGui || loi == null) {
            return;
        }
        daGui = true;
        network.Session_ME.baoLoiTai("khoi dong: " + dong());
    }
}
