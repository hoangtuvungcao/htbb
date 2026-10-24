package game.pokemon;

import game.model.Actor;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.util.Vector;

/**
 * NHỊP VẼ + ĐO KHUNG (0.9.5).
 *
 * 1) Vẽ nhiều hơn nhịp logic: game chạy logic 25 nhịp/giây (40 ms, giữ nguyên tốc độ đi / hoạt hình / mạng); nền có
 *    {@link #fps} &gt; 25 (bản PC: 60) thì Canvas.run vẽ thêm khung GIỮA hai nhịp. Vị trí nhân vật / quái / NPC và camera
 *    được NỘI SUY giữa nhịp trước và nhịp này (trễ đúng một nhịp) nên chuyển động mượt 60 khung; dịch chuyển xa
 *    (đổi map, dịch chuyển tức thời) không nội suy. Vẽ thêm chạy trên luồng vẽ trong lúc luồng game CHỜ
 *    (serviceRepaints) - không chạy song song với logic; vị trí tạm đặt lúc vẽ được trả lại ngay sau khung.
 *
 * 2) Bộ đo khung (Cài đặt -> Nhà phát triển): ms nhập (chạm / phím), logic, vẽ thế giới, vẽ giao diện; nền thêm
 *    trình bày, GC, cấp phát. Đo bằng nano() (mili giây x 10^6: CLDC / MIDP KHÔNG có System.nanoTime - 0.9.7.3, CoffeeCat iOS ném lỗi mọi nhịp), không cấp phát.
 */
public final class NhipVe {
    private NhipVe() {
    }

    /** khung vẽ / giây mong muốn; &lt;= 25 = chỉ vẽ mỗi nhịp logic (J2ME, APK) */
    public static int fps;
    /** bật nội suy vị trí khi vẽ thêm */
    public static boolean noiSuy = true;

    static final long NHIP_MS = 40;
    /** thời điểm bắt đầu nhịp logic hiện tại (ms) */
    static long dauNhip;
    /** số nhịp (đánh dấu vị trí trước còn hợp lệ) */
    static int soNhip;
    /** khung vẽ kế tiếp (ms, dạng double để đều 16,67 ms) */
    static double keTiep;

    // ------------------------------------------------------------------ đo
    public static boolean doBat;
    /** pha: 0 nhập, 1 logic, 2 vẽ thế giới, 3 vẽ giao diện */
    public static final int NHAP = 0, LOGIC = 1, VE_THE_GIOI = 2, VE_GIAO_DIEN = 3, SO_PHA = 4;
    /** trung bình trượt (ms * 1000) và cực đại trong cửa sổ 1 giây */
    public static final long[] TB = new long[SO_PHA], MAX = new long[SO_PHA];
    static final long[] CONG = new long[SO_PHA], CUC = new long[SO_PHA], DEM = new long[SO_PHA];
    static long mocVe, mocCua;
    /** khung vẽ / giây đo được của Canvas.paint */
    public static int khungGiay;
    static int demKhung;
    /** tổ chức đồ thời gian cả khung vẽ (bước 0,5 ms, tới 100 ms) -> p99; p50 / p99 của giây trước (ms * 10) */
    static final int[] HIST = new int[201];
    public static int p50x10, p99x10;
    static long dauVe;

    static void cong(int pha, long ns) {
        CONG[pha] += ns;
        DEM[pha]++;
        if (ns > CUC[pha]) {
            CUC[pha] = ns;
        }
    }

    /** Hết mỗi giây: chốt số liệu. */
    static void chot(long nowMs) {
        if (nowMs - mocCua < 1000) {
            return;
        }
        for (int i = 0; i < SO_PHA; i++) {
            TB[i] = DEM[i] == 0 ? 0 : CONG[i] / DEM[i] / 1000;
            MAX[i] = CUC[i] / 1000;
            CONG[i] = 0;
            CUC[i] = 0;
            DEM[i] = 0;
        }
        khungGiay = (int) (demKhung * 1000L / Math.max(1, nowMs - mocCua));
        int tong = 0;
        for (int i = 0; i < HIST.length; i++) {
            tong += HIST[i];
        }
        int c = 0;
        p50x10 = 0;
        p99x10 = 0;
        boolean co50 = false;
        for (int i = 0; i < HIST.length && tong > 0; i++) {
            c += HIST[i];
            if (!co50 && c * 2 >= tong) {
                p50x10 = i * 5;
                co50 = true;
            }
            if (c * 100 >= tong * 99) {
                p99x10 = i * 5;
                break;
            }
        }
        for (int i = 0; i < HIST.length; i++) {
            HIST[i] = 0;
        }
        demKhung = 0;
        mocCua = nowMs;
    }

    private static long t0;

    /** "nano giây" từ đồng hồ mili giây (CLDC 1.1 / MIDP 2.0 không có System.nanoTime). */
    static long nano() {
        return System.currentTimeMillis() * 1000000L;
    }

    /** Canvas.run: bắt đầu pha. */
    public static void batDau() {
        t0 = nano();
    }

    /** Canvas.run: xong pha. */
    public static void xong(int pha) {
        if (doBat) {
            cong(pha, nano() - t0);
        }
        t0 = nano();
    }

    /** GameScr.paint: hết lớp thế giới (bắt đầu vẽ giao diện). */
    public static void hetTheGioi() {
        if (doBat && mocVe != 0) {
            long n = nano();
            cong(VE_THE_GIOI, n - mocVe);
            mocVe = -n;   // âm = đang đo giao diện từ đây
        }
    }

    // ------------------------------------------------------------------ nội suy
    /**
     * Đầu nhịp logic (trước update): ghi vị trí "trước" của mọi actor + camera.
     */
    public static void dauNhip(long nowMs) {
        dauNhip = nowMs;
        soNhip++;
        if (fps <= 25 || !noiSuy) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs == null) {
            return;
        }
        Vector v = gs.actors;
        for (int i = v.size() - 1; i >= 0; i--) {
            Actor a = (Actor) v.elementAt(i);
            a.xTruoc = a.x;
            a.yTruoc = a.y;
            a.nhipTruoc = soNhip;
        }
        cmxTruoc = GameScr.cmx;
        cmyTruoc = GameScr.cmy;
        camNhip = soNhip;
    }

    static int cmxTruoc, cmyTruoc, camNhip;
    /** đang vẽ khung nội suy: giá trị đã đặt tạm (để trả lại) */
    static int soTam;
    static Actor[] tam = new Actor[64];
    static short[] tamX = new short[64], tamY = new short[64], datX = new short[64], datY = new short[64];
    static int camX, camY, datCamX, datCamY;
    static boolean camTam;

    static int lerp(int a, int b, int t1024) {
        return a + ((b - a) * t1024 >> 10);
    }

    /** Canvas.paint đầu khung: đặt vị trí nội suy (alpha theo thời gian từ đầu nhịp). */
    public static void dauKhung() {
        demKhung++;
        if (doBat) {
            mocVe = nano();
            dauVe = mocVe;
        }
        soTam = 0;
        camTam = false;
        if (fps <= 25 || !noiSuy) {
            return;
        }
        GameScr gs = Canvas.gameScr;
        if (gs == null || gs.mainChar == null) {
            return;
        }
        long d = System.currentTimeMillis() - dauNhip;
        int t = (int) Math.max(0, Math.min(1024, d * 1024 / NHIP_MS));
        if (t >= 1024) {
            return;
        }
        Vector v = gs.actors;
        int n = v.size();
        if (n > tam.length) {
            int m = n + 32;
            tam = new Actor[m];
            tamX = new short[m];
            tamY = new short[m];
            datX = new short[m];
            datY = new short[m];
        }
        for (int i = 0; i < n; i++) {
            Actor a = (Actor) v.elementAt(i);
            if (a.nhipTruoc != soNhip) {
                continue;
            }
            int dx = a.x - a.xTruoc, dy = a.y - a.yTruoc;
            if (dx == 0 && dy == 0 || dx > 48 || dx < -48 || dy > 48 || dy < -48) {
                continue;
            }
            int k = soTam++;
            tam[k] = a;
            tamX[k] = a.x;
            tamY[k] = a.y;
            a.x = (short) lerp(a.xTruoc, a.x, t);
            a.y = (short) lerp(a.yTruoc, a.y, t);
            datX[k] = a.x;
            datY[k] = a.y;
        }
        if (camNhip == soNhip) {
            int dx = GameScr.cmx - cmxTruoc, dy = GameScr.cmy - cmyTruoc;
            if ((dx != 0 || dy != 0) && dx <= 64 && dx >= -64 && dy <= 64 && dy >= -64) {
                camTam = true;
                camX = GameScr.cmx;
                camY = GameScr.cmy;
                GameScr.cmx = lerp(cmxTruoc, GameScr.cmx, t);
                GameScr.cmy = lerp(cmyTruoc, GameScr.cmy, t);
                datCamX = GameScr.cmx;
                datCamY = GameScr.cmy;
            }
        }
    }

    /** Canvas.paint cuối khung: trả vị trí thật (chỉ khi chưa ai đổi - an toàn nếu logic lỡ chạy song song). */
    public static void cuoiKhung() {
        for (int i = 0; i < soTam; i++) {
            Actor a = tam[i];
            if (a.x == datX[i]) {
                a.x = tamX[i];
            }
            if (a.y == datY[i]) {
                a.y = tamY[i];
            }
            tam[i] = null;
        }
        soTam = 0;
        if (camTam) {
            if (GameScr.cmx == datCamX) {
                GameScr.cmx = camX;
            }
            if (GameScr.cmy == datCamY) {
                GameScr.cmy = camY;
            }
            camTam = false;
        }
        if (doBat) {
            long n = nano();
            if (mocVe < 0) {
                cong(VE_GIAO_DIEN, n + mocVe);
            } else if (mocVe > 0) {
                cong(VE_GIAO_DIEN, n - mocVe);   // màn không có thế giới: cả khung là giao diện
            }
            mocVe = 0;
            int k = (int) Math.min(200, (n - dauVe) / 500000L);
            HIST[k]++;
            chot(System.currentTimeMillis());
        }
    }

    // ------------------------------------------------------------------ nhịp vẽ thêm (Canvas.run)
    /**
     * Sau khi xong nhịp logic: vẽ các khung theo lưới đều 1000/fps tới hết nhịp (nếu fps &gt; 25; 0.9.15: kể cả khung đầu
     * nhịp - Canvas.run không vẽ riêng nữa), rồi ngủ phần còn lại.
     * Trả true nếu đã tự ngủ hết nhịp.
     */
    /** Vẽ > 25 khung/giây: Canvas.run KHÔNG vẽ khung ngay sau logic, mọi khung do veThem vẽ theo lưới đều. */
    public static boolean veTheoLuoi() {
        return fps > 25;
    }

    public static boolean veThem(Canvas c, long dauMs) {
        if (fps <= 25) {
            return false;
        }
        double buoc = 1000.0 / fps;
        long het = dauMs + NHIP_MS;
        long now = System.currentTimeMillis();
        if (keTiep < now - 4 * buoc || keTiep > now + 4 * buoc) {
            keTiep = now;
        }
        while (true) {
            now = System.currentTimeMillis();
            if (keTiep >= het) {
                break;
            }
            long cho = (long) keTiep - now;
            if (cho > 0) {
                try {
                    Thread.sleep(cho);
                } catch (InterruptedException e) {
                    return true;
                }
            }
            c.repaint();
            c.serviceRepaints();
            keTiep += buoc;
        }
        now = System.currentTimeMillis();
        if (het > now) {
            try {
                Thread.sleep(het - now);
            } catch (InterruptedException e) {
                // bỏ
            }
        } else {
            Thread.yield();
        }
        return true;
    }
}
