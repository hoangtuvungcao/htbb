import javax.microedition.lcdui.NenPc;

/**
 * NHỊP KHUNG HÌNH (0.9.15, APK 60 / 30 khung): chạy vòng game THẬT (Canvas.run, đường vẽ bản PC, không cửa sổ) trên thế
 * giới giả có 24 người, đo số khung vẽ / giây và khoảng cách giữa hai khung (đếm ở Canvas.paint qua NhipVe.dauKhung) với
 * NhipVe.fps = 25 (cũ của APK / J2ME), 30 (APK tiết kiệm pin), 60 (APK mặc định, như PC). Kiểm: 60 -> >= 50 khung/s,
 * khoảng p90 <= 25 ms; 30 -> 26..34 khung/s, p90 <= 40 ms; 25 -> 22..27 khung/s; nhịp logic giữ 25 / giây ở mọi mức.
 *
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemNhipKhung
 */
public class KiemNhipKhung {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    /** {khung/s, logic/s, p50 ms, p90 ms, max ms} trong ms mili giây. */
    static double[] do1(int fps, int ms) throws Exception {
        game.pokemon.NhipVe.fps = fps;
        Thread.sleep(600);
        DemKhung.bat = true;
        DemKhung.n = 0;
        int l0 = game.pokemon.KhoiDong.soNhip;
        long t0 = System.currentTimeMillis();
        Thread.sleep(ms);
        DemKhung.bat = false;
        long t1 = System.currentTimeMillis();
        int n = DemKhung.n;
        int logic = game.pokemon.KhoiDong.soNhip - l0;
        long[] k = new long[Math.max(0, n - 1)];
        for (int i = 1; i < n; i++) {
            k[i - 1] = DemKhung.t[i] - DemKhung.t[i - 1];
        }
        java.util.Arrays.sort(k);
        double s = (t1 - t0) / 1000.0;
        return new double[]{n / s, logic / s, k.length > 0 ? k[k.length / 2] : 0, k.length > 0 ? k[k.length * 9 / 10] : 0, k.length > 0 ? k[k.length - 1] : 0};
    }

    /** Ghi thời điểm mỗi khung (lớp phủ PC gọi mỗi lần vẽ). */
    static final class DemKhung {
        static volatile boolean bat;
        static volatile int n;
        static final long[] t = new long[20000];
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = java.nio.file.Files.createTempDirectory("kiem_nhip").toFile();
        System.setProperty("htbb.duLieu", new java.io.File(ra, "du_lieu").getAbsolutePath());
        System.setProperty("microedition.platform", "HTBB-PC/kiem");
        htbb.pc.CaiDat.thuMucDuLieu();
        htbb.pc.CaiDat.nap();
        htbb.pc.ChuHD.bat = false;
        htbb.pc.GiaoDienHD.bat = false;
        htbb.pc.CaiDat.tatTieng = true;
        htbb.pc.LopPhu.nap();
        javax.microedition.midlet.MIDlet.GHI_DE.put("HTBB-May-Chu", "127.0.0.1:1");
        NenPc.tinhKichThuoc(1280, 720);
        ChupTrongGame.chay(() -> {
            try {
                javax.microedition.midlet.MIDlet.batDau((javax.microedition.midlet.MIDlet) Class.forName("game.GameMidlet").newInstance());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < 100 && (game.render.Canvas.instance == null || game.render.Canvas.currentScreen == null); i++) {
            Thread.sleep(100);
        }
        Thread.sleep(2500);
        ChupTrongGame.chay(ChupTrongGame::giaLap);
        ChupTrongGame.chay(() -> DoKhung.themNguoi(24));
        ChupTrongGame.chay(() -> ChupTrongGame.moMan("the_gioi"));
        // bề mặt không cửa sổ: NenPc vẽ thật mỗi lần repaint (Canvas.paint -> NenPc.soKhung++), không có luồng trình bày
        // (LopPhu không ghi đè NhipVe.fps). Luồng lấy mẫu 1 ms ghi thời điểm mỗi lần soKhung tăng.
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                htbb.pc.ManHinh.khongCuaSo();
            }
        });
        Thread mau = new Thread(new Runnable() {
            public void run() {
                long cu = NenPc.soKhung;
                while (true) {
                    long k = NenPc.soKhung;
                    if (k != cu) {
                        cu = k;
                        if (DemKhung.bat && DemKhung.n < DemKhung.t.length) {
                            DemKhung.t[DemKhung.n++] = System.currentTimeMillis();
                        }
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        mau.setDaemon(true);
        mau.start();
        int[] muc = {25, 30, 60};
        double[][] kq = new double[muc.length][];
        for (int i = 0; i < muc.length; i++) {
            kq[i] = do1(muc[i], 3000);
            System.out.printf("fps=%d: %.1f khung/s, logic %.1f nhip/s, khoang p50 %.0f ms p90 %.0f ms max %.0f ms%n",
                    muc[i], kq[i][0], kq[i][1], kq[i][2], kq[i][3], kq[i][4]);
            kiem(kq[i][1] >= 22 && kq[i][1] <= 27, "fps=" + muc[i] + ": logic giữ 25 nhịp/s (" + kq[i][1] + ")");
        }
        kiem(kq[0][0] >= 22 && kq[0][0] <= 27, "fps=25: 22..27 khung/s (" + kq[0][0] + ")");
        kiem(kq[1][0] >= 26 && kq[1][0] <= 34 && kq[1][3] <= 40, "fps=30: 26..34 khung/s, p90 <= 40 ms");
        kiem(kq[2][0] >= 50 && kq[2][3] <= 25, "fps=60: >= 50 khung/s, p90 <= 25 ms");
        System.out.println("KiemNhipKhung: " + dat + " dat, " + hong + " hong");
        System.exit(hong == 0 ? 0 : 1);
    }
}
