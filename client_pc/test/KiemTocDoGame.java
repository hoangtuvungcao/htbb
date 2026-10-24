import javax.microedition.lcdui.NenPc;

/**
 * TỐC ĐỘ GAME SO VỚI THỜI GIAN THẬT (0.9.18, lỗi "game chậm như slow motion" của 0.9.15-0.9.17).
 *
 * Chạy vòng game THẬT (Canvas.run, đường vẽ bản PC, không cửa sổ) trên thế giới giả 24 người, nhân vật đi thẳng
 * (Char.update, đọc vị trí giữa hai khung), giả lập máy chậm: mỗi khung vẽ tốn thêm N ms CPU (NenPc.giaLapVeMs) và với đường APK
 * trình bày chặn tới nhịp quét 60 Hz kế tiếp (NenPc.giaLapVsync, như unlockCanvasAndPost). Đo theo giây thật:
 * nhịp logic / giây (đúng = 25), tốc độ đi px / giây (đúng = speed x 25), khung / giây, khoảng giữa hai khung p50 / p90.
 *
 * Đường: PC (fps 60), APK 60 (fps 60 + vsync, tự hạ 30 khi quá tải), APK 30 (fps 30 + vsync), J2ME (fps 0: vẽ một khung
 * sau logic). Kiểm: tốc độ game = thời gian thật +-3% ở mọi chi phí vẽ tới 70 ms / khung; máy nhanh (0 ms): PC / APK 60
 * >= 50 khung/s, p90 <= 25 ms.
 *
 *   java -Djava.awt.headless=true -cp client_pc/out/build/cls:client_pc/out/build/res:$O KiemTocDoGame [bao-cao] [ms...]
 * bao-cao = chỉ in bảng (đo bản cũ, không kiểm).
 */
public class KiemTocDoGame {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) {
            dat++;
        } else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    static final class Duong {
        final String ten;
        final int fps;
        final boolean vsync, tuHa;

        Duong(String ten, int fps, boolean vsync, boolean tuHa) {
            this.ten = ten;
            this.fps = fps;
            this.vsync = vsync;
            this.tuHa = tuHa;
        }
    }

    static volatile boolean demBat;
    static volatile int demKhung;
    static final long[] T = new long[40000];

    /** Đặt NhipVe.tuHa (chỉ có từ 0.9.18 - bản cũ bỏ qua). */
    static void datTuHa(boolean b) {
        try {
            game.pokemon.NhipVe.class.getField("tuHa").setBoolean(null, b);
            game.pokemon.NhipVe.class.getMethod("datLaiTuHa").invoke(null);
        } catch (Throwable e) {
            // bản cũ
        }
    }

    /** {nhịp logic/s, đi px/s, khung/s, p50, p90, fps cuối} */
    static double[] do1(Duong d, int veMs, int ms) throws Exception {
        game.pokemon.NhipVe.fps = d.fps;
        datTuHa(d.tuHa);
        NenPc.giaLapVsync = d.vsync;
        NenPc.giaLapVeMs = veMs;
        final game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
        ChupTrongGame.chay(() -> {
            mc.x = 200;
            mc.moveTo((short) 6000, mc.y);   // đi thẳng sang phải: Char.update đi speed px mỗi nhịp logic
        });
        Thread.sleep(800);
        final int[] x = new int[4];
        final long[] t = new long[2];
        // đọc vị trí + số nhịp + giờ trên luồng vẽ (giữa hai khung = vị trí logic thật, không phải vị trí nội suy lúc vẽ)
        ChupTrongGame.chay(() -> {
            x[0] = mc.x;
            x[2] = game.pokemon.KhoiDong.soNhip;
            t[0] = System.currentTimeMillis();
        });
        demKhung = 0;
        demBat = true;
        Thread.sleep(ms);
        ChupTrongGame.chay(() -> {
            x[1] = mc.x;
            x[3] = game.pokemon.KhoiDong.soNhip;
            t[1] = System.currentTimeMillis();
        });
        demBat = false;
        long t0 = t[0], t1 = t[1];
        int logic = x[3] - x[2];
        long qd = x[1] - x[0];
        int n = demKhung;
        long[] k = new long[Math.max(0, n - 1)];
        for (int i = 1; i < n; i++) {
            k[i - 1] = T[i] - T[i - 1];
        }
        java.util.Arrays.sort(k);
        double s = (t1 - t0) / 1000.0;
        if (d.tuHa) {
            Thread.sleep(1500);   // tự hạ cần 3 cửa sổ 1 giây liền
        }
        int fpsCuoi = game.pokemon.NhipVe.fps;
        NenPc.giaLapVeMs = 0;
        NenPc.giaLapVsync = false;
        datTuHa(false);
        return new double[]{logic / s, qd / s, n / s, k.length > 0 ? k[k.length / 2] : 0, k.length > 0 ? k[k.length * 9 / 10] : 0, fpsCuoi};
    }

    public static void main(String[] a) throws Exception {
        boolean baoCao = a.length > 0 && "bao-cao".equals(a[0]);
        int[] chiPhi = {0, 10, 20, 30, 45, 70};
        int vt = baoCao ? 1 : 0;
        if (a.length > vt) {
            chiPhi = new int[a.length - vt];
            for (int i = vt; i < a.length; i++) chiPhi[i - vt] = Integer.parseInt(a[i]);
        }
        System.setProperty("java.awt.headless", "true");
        java.io.File ra = java.nio.file.Files.createTempDirectory("kiem_toc_do").toFile();
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
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                htbb.pc.ManHinh.khongCuaSo();
            }
        });
        final game.model.MainChar mc = game.render.Canvas.gameScr.mainChar;
        final int tocDo = mc.speed;
        // thế giới giả: nhân vật chính như sau Tilemap.loadMap (trong actors để GameScr.update gọi update) và không mang
        // ID mặc định 32001 (Char.update bỏ qua ID đó = nhân vật chưa vào game)
        final game.render.screen.GameScr gs = game.render.Canvas.gameScr;
        ChupTrongGame.chay(() -> {
            if (mc.ID == 32001) mc.ID = 4242;
            if (!gs.actors.contains(mc)) gs.actors.addElement(mc);
        });
        // lấy mẫu 1 ms: thời điểm mỗi khung (NenPc.soKhung tăng)
        Thread mau = new Thread(new Runnable() {
            public void run() {
                long cu = NenPc.soKhung;
                while (true) {
                    long k = NenPc.soKhung;
                    if (k != cu) {
                        cu = k;
                        if (demBat && demKhung < T.length) {
                            T[demKhung++] = System.currentTimeMillis();
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
        Duong[] duong = {
            new Duong("PC 60", 60, false, false),
            new Duong("APK 60", 60, true, true),
            new Duong("APK 30", 30, true, false),
            new Duong("J2ME / APK<=0.9.14", 0, true, false),
        };
        double diChuan = tocDo * 25.0;
        System.out.printf("toc do di chuan = %d px/nhip x 25 = %.0f px/s%n", tocDo, diChuan);
        System.out.println("duong                | ve ms | logic/s | toc do game | di px/s | khung/s | p50 | p90 | fps cuoi");
        for (Duong d : duong) {
            for (int ms : chiPhi) {
                double[] r = do1(d, ms, ms >= 45 ? 6000 : 4000);
                double tl = r[0] / 25.0 * 100;
                System.out.printf("%-20s | %5d | %7.1f | %9.1f %% | %7.1f | %7.1f | %3.0f | %3.0f | %d%n",
                        d.ten, ms, r[0], tl, r[1], r[2], r[3], r[4], (int) r[5]);
                if (!baoCao) {
                    kiem(Math.abs(r[0] - 25) <= 0.75, d.ten + " ve " + ms + " ms: logic 25 nhip/s +-3% (" + r[0] + ")");
                    kiem(r[1] >= diChuan * 0.97 && r[1] <= diChuan * 1.03,
                            d.ten + " ve " + ms + " ms: di " + diChuan + " px/s +-3% (" + r[1] + ")");
                    if (ms == 0 && d.fps == 60) {
                        kiem(r[2] >= 50 && r[4] <= 25, d.ten + " may nhanh: >= 50 khung/s, p90 <= 25 ms (" + r[2] + ", " + r[4] + ")");
                    }
                    if (ms == 0 && d.fps == 60 && d.tuHa) {
                        kiem((int) r[5] == 60, d.ten + " may nhanh: khong tu ha 30 (" + r[5] + ")");
                    }
                    if (ms >= 30 && d.tuHa) {
                        kiem((int) r[5] == 30, d.ten + " ve " + ms + " ms: tu ha 60 -> 30 (" + r[5] + ")");
                    }
                }
            }
        }
        if (!baoCao) {
            System.out.println("KiemTocDoGame: " + dat + " dat, " + hong + " hong");
        }
        System.exit(hong == 0 ? 0 : 1);
    }
}
