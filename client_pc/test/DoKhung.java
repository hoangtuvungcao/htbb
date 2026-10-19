import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import javax.microedition.lcdui.NenPc;

/**
 * ĐO KHUNG HÌNH (0.9.5, không cửa sổ / máy chủ): đường vẽ bản PC thật (Canvas.paint của game + HD + LopPhu) ở 1280x720
 * và 1920x1080, các cảnh: thế giới có 24 nhân vật khác, Menu, Nhân vật + Hành trang, Cửa hàng, ô chat mở. Mỗi cảnh:
 * p50 / p99 / max thời gian vẽ một khung (ms), byte cấp phát mỗi khung trên luồng vẽ, số lần GC trong lúc đo.
 * Thêm "logic": thời gian GameScr.update một nhịp.
 *
 *   javac -cp client_pc/out/build/cls -d $O client_pc/test/ChupTrongGame.java client_pc/test/DoKhung.java
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O DoKhung <thư mục> [--j2me]
 * --j2me: tắt chữ / khung HD (đường vẽ gần bản APK / jar: ảnh font, khối GiaoDien).
 * Mã thoát 0; in bảng kết quả.
 */
public class DoKhung {
    static final com.sun.management.ThreadMXBean TM = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    static long gc() {
        long n = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) n += Math.max(0, b.getCollectionCount());
        return n;
    }

    static void themNguoi(int n) {
        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        java.util.Random r = new java.util.Random(7);
        int[] dex = {1, 4, 7, 25, 16, 19, 133, 52, 39, 54, 60, 63};
        for (int i = 0; i < n; i++) {
            game.model.Char c = new game.model.Char();
            c.ID = (short) (1000 + i);
            c.name = "NguoiChoi" + i;
            c.catagory = 0;
            c.level = (byte) (5 + i);
            c.pokeDex = (short) dex[i % dex.length];
            c.hp = 100;
            c.maxhp = 100;
            c.x = (short) (gs.mainChar.x - 140 + r.nextInt(280));
            c.y = (short) (gs.mainChar.y - 70 + r.nextInt(140));
            c.xTo = c.x;
            c.yTo = c.y;
            gs.actors.addElement(c);
        }
    }

    public static void main(String[] a) throws Exception {
        boolean j2me = a.length > 1 && "--j2me".equals(a[1]);
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = new java.io.File(a.length > 0 ? a[0] : "do_khung");
        ra.mkdirs();
        System.setProperty("htbb.duLieu", new java.io.File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = !j2me;
        htbb.pc.GiaoDienHD.bat = !j2me;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                game.render.screen.TField.isQwerty = true;
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) Thread.sleep(100);
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ChupTrongGame.chay(() -> themNguoi(24));
        Thread.sleep(500);
        String[] canh = {"the_gioi", "menu", "nv_hanh_trang", "cua_hang_thuoc", "chat"};
        int[][] co = {{1280, 720}, {1920, 1080}};
        System.out.printf("%-10s %-15s %8s %8s %8s %12s %5s%n", "co", "canh", "p50ms", "p99ms", "maxms", "KB/khung", "GC");
        for (int[] c : co) {
            NenPc.tinhKichThuoc(c[0], c[1]);
            ChupTrongGame.chay(() -> game.render.Canvas.instance.sizeChanged(NenPc.rong, NenPc.cao));
            for (String ten : canh) {
                ChupTrongGame.chay(() -> {
                    if ("chat".equals(ten)) {
                        ChupTrongGame.moMan("the_gioi");
                        game.render.screen.GameScr gs = game.render.Canvas.gameScr;
                        gs.chatMode = true;
                        gs.tfChat.isFocus = true;
                    } else {
                        game.render.Canvas.gameScr.chatMode = false;
                        ChupTrongGame.moMan(ten);
                    }
                });
                Thread.sleep(300);
                ChupTrongGame.chay(() -> {
                    Object d = game.render.Canvas.currentDialog;
                    if (d instanceof game.render.screen.MsgDlg || d instanceof game.pokemon.HopThongBao) game.render.Canvas.endDlg();
                });
                for (int i = 0; i < 30; i++) ChupTrongGame.ve(c[0], c[1]);
                int n = 200;
                final long[] t = new long[n];
                final long[] cap = new long[1];
                long gc0 = gc();
                for (int i = 0; i < n; i++) {
                    final int k = i;
                    ChupTrongGame.chay(() -> {
                        long id = Thread.currentThread().getId();
                        long b0 = TM.getThreadAllocatedBytes(id);
                        long s = System.nanoTime();
                        ChupTrongGame.veThang();
                        t[k] = System.nanoTime() - s;
                        cap[0] += TM.getThreadAllocatedBytes(id) - b0;
                    });
                }
                long dGc = gc() - gc0;
                java.util.Arrays.sort(t);
                System.out.printf("%-10s %-15s %8.2f %8.2f %8.2f %12.1f %5d%n", c[0] + "x" + c[1], ten, t[n / 2] / 1e6, t[n * 99 / 100] / 1e6,
                        t[n - 1] / 1e6, cap[0] / 1024.0 / n, dGc);
            }
        }
        // logic một nhịp (GameScr.update) với 24 nhân vật
        ChupTrongGame.chay(() -> ChupTrongGame.moMan("the_gioi"));
        long[] t = new long[200];
        for (int i = 0; i < t.length; i++) {
            final int k = i;
            ChupTrongGame.chay(() -> {
                long s = System.nanoTime();
                try {
                    game.render.Canvas.gameScr.update();
                } catch (Throwable e) {
                    // bỏ
                }
                t[k] = System.nanoTime() - s;
            });
        }
        java.util.Arrays.sort(t);
        System.out.printf("logic GameScr.update p50=%.2f ms p99=%.2f ms%n", t[100] / 1e6, t[198] / 1e6);
        System.exit(0);
    }
}
