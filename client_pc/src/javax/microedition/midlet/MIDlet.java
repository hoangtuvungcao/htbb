package javax.microedition.midlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import javax.microedition.io.ConnectionNotFoundException;

/**
 * javax.microedition.midlet.MIDlet trên máy tính. Tài nguyên jar ("/pk/0001_0.png") = tài nguyên trong
 * HTBB-PC.jar (client_jar/res đóng thẳng vào jar) hoặc thư mục -Dhtbb.res=... (thử nhanh không cần đóng gói lại);
 * thuộc tính JAD/MANIFEST (getAppProperty) nạp từ "/htbb-app.properties" (build.sh sinh: MANIFEST.MF của bản
 * jar + HTBB-May-Chu / HTBB-Ban-Thu theo kiểu build) rồi ghi đè bằng tham số dòng lệnh (--may-chu ...).
 */
public abstract class MIDlet {
    private static Hashtable thuocTinh;
    /** ghi đè thuộc tính (htbb.pc.Chay: --may-chu) */
    public static final Hashtable GHI_DE = new Hashtable();
    /** gọi khi notifyDestroyed (đóng cửa sổ game) */
    public static Runnable khiThoat;
    /** mở liên kết (platformRequest) */
    public static java.util.Vector daMoLienKet = new java.util.Vector();

    protected MIDlet() {
    }

    protected abstract void startApp() throws MIDletStateChangeException;

    protected abstract void pauseApp();

    protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    /** Cho lớp nền gọi các hàm vòng đời protected. */
    public static void batDau(MIDlet m) throws MIDletStateChangeException {
        m.startApp();
    }

    public static void tamDung(MIDlet m) {
        m.pauseApp();
    }

    public static void huy(MIDlet m) {
        try {
            m.destroyApp(true);
        } catch (Throwable e) {
            // bỏ qua
        }
    }

    public final void notifyDestroyed() {
        Runnable r = khiThoat;
        if (r != null) r.run();
    }

    public final void notifyPaused() {
    }

    public final void resumeRequest() {
    }

    public final int checkPermission(String p) {
        return 1;
    }

    public final String getAppProperty(String key) {
        return thuocTinh(key);
    }

    /** Thuộc tính ứng dụng (cả ghi đè dòng lệnh). */
    public static String thuocTinh(String key) {
        if (key == null) return null;
        Object v = GHI_DE.get(key);
        if (v != null) return String.valueOf(v);
        return (String) bang().get(key);
    }

    private static synchronized Hashtable bang() {
        if (thuocTinh == null) {
            thuocTinh = new Hashtable();
            InputStream in = moTaiNguyen("/htbb-app.properties");
            if (in != null) {
                try {
                    java.util.Properties p = new java.util.Properties();
                    p.load(new java.io.InputStreamReader(in, "UTF-8"));
                    for (Map.Entry e : p.entrySet()) {
                        String v = String.valueOf(e.getValue()).trim();
                        if (v.length() > 0) thuocTinh.put(String.valueOf(e.getKey()).trim(), v);
                    }
                } catch (IOException e) {
                    System.err.println("doc htbb-app.properties: " + e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // bỏ qua
                    }
                }
            }
        }
        return thuocTinh;
    }

    public final boolean platformRequest(String url) throws ConnectionNotFoundException {
        if (url == null || url.length() == 0) return false;
        try {
            daMoLienKet.addElement(url);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Throwable e) {
            throw new ConnectionNotFoundException(url);
        }
        return false;
    }

    /**
     * Thay cho Class.getResourceAsStream của J2ME (client_pc/port-j2me.py đổi mọi "...getClass().getResourceAsStream("
     * thành hàm này). null nếu không có (như J2ME).
     */
    public static InputStream moTaiNguyen(String ten) {
        if (ten == null) return null;
        String p = ten;
        while (p.startsWith("/")) p = p.substring(1);
        // bản PC: tiếng hiệu ứng 44.1 kHz / 16 bit (client_pc/res/am_hq) thay tiếng 8 bit của bản jar
        if (p.startsWith("am/s_") && p.endsWith(".wav")) {
            InputStream hq = MIDlet.class.getResourceAsStream("/am_hq/" + p.substring(3));
            if (hq != null) return hq;
        }
        String thuMuc = System.getProperty("htbb.res");
        if (thuMuc != null && thuMuc.length() > 0) {
            File f = new File(thuMuc, p);
            if (f.isFile()) {
                try {
                    return new FileInputStream(f);
                } catch (IOException e) {
                    // rơi xuống classpath
                }
            }
        }
        return MIDlet.class.getResourceAsStream("/" + p);
    }
}
