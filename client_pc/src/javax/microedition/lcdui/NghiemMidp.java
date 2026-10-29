package javax.microedition.lcdui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KIỂM NGHIÊM MIDP (0.9.22, Nokia E72 / S60 văng game): máy J2ME thật làm đúng đặc tả MIDP 2.0 - drawRegion /
 * createImage(Image, ...) có vùng nguồn vượt ảnh, getRGB / drawRGB / createRGBImage thiếu mảng thì NÉM
 * (IllegalArgumentException / ArrayIndexOutOfBoundsException), còn lớp PC này (và MicroEmulator, J2ME Loader) cắt im
 * lặng. Bật {@link #bat} (harness -Dchup.j2me, KiemPhimNpc, hoặc -Dhtbb.midpNghiem=true): mỗi lời gọi sai đặc tả ghi lại
 * một lần theo chỗ gọi trong code game (không đổi cách vẽ) - harness báo HỎNG.
 */
public final class NghiemMidp {
    private NghiemMidp() {
    }

    public static volatile boolean bat = Boolean.getBoolean("htbb.midpNghiem");
    private static final Map<String, String> DS = new LinkedHashMap<String, String>();

    /** Vùng (x, y, w, h) trong ảnh rộng W cao H? (w, h &lt;= 0: không vẽ gì - không coi là sai) */
    static void vung(String phep, Image img, int x, int y, int w, int h) {
        if (!bat || img == null || w <= 0 || h <= 0) return;
        int W = img.getWidth(), H = img.getHeight();
        if (x < 0 || y < 0 || (long) x + w > W || (long) y + h > H) {
            ghi(phep + " vung (" + x + "," + y + " " + w + "x" + h + ") ngoai anh " + W + "x" + H);
        }
    }

    /** Mảng rgb đủ cho w x h với offset / scanlength (getRGB / drawRGB). */
    static void mang(String phep, int[] rgb, int offset, int scan, int w, int h) {
        if (!bat || rgb == null || w <= 0 || h <= 0) return;
        long dau = (long) offset + Math.min(0L, (long) scan * (h - 1));
        long cuoi = (long) offset + Math.max(0L, (long) scan * (h - 1)) + w;
        if (offset < 0 || dau < 0 || cuoi > rgb.length || Math.abs((long) scan) < w) {
            ghi(phep + " mang " + rgb.length + " khong du (offset " + offset + ", scan " + scan + ", " + w + "x" + h + ")");
        }
    }

    static synchronized void ghi(String loi) {
        String noi = "?";
        StackTraceElement[] st = new Throwable().getStackTrace();
        for (StackTraceElement e : st) {
            String c = e.getClassName();
            if (!c.startsWith("javax.microedition.") && !c.startsWith("htbb.pc.")) {
                noi = c + "." + e.getMethodName() + ":" + e.getLineNumber();
                break;
            }
        }
        String khoa = noi + " " + loi.replaceAll("[0-9-]+", "#");
        if (DS.containsKey(khoa)) return;
        DS.put(khoa, noi + ": " + loi);
        System.out.println("MIDP NGHIEM: " + noi + ": " + loi);
    }

    /** Các lỗi đặc tả đã gặp (mỗi chỗ gọi + kiểu lỗi một dòng). */
    public static synchronized List<String> ds() {
        return new ArrayList<String>(DS.values());
    }
}
