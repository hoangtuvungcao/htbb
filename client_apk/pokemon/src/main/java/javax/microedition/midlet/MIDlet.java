package javax.microedition.midlet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.NenAndroid;

/**
 * javax.microedition.midlet.MIDlet trên Android. Tài nguyên jar ("/pk/0001_0.png") = assets của APK
 * ("pk/0001_0.png"); thuộc tính JAD/MANIFEST (getAppProperty) nạp từ assets "htbb-app.properties"
 * (Gradle sinh: MANIFEST.MF của bản jar + HTBB-May-Chu / HTBB-Ban-Thu theo kiểu build).
 */
public abstract class MIDlet {
    private static Hashtable thuocTinh;
    /** gọi khi notifyDestroyed (Activity đóng app) */
    public static Runnable khiThoat;

    protected MIDlet() {
    }

    protected abstract void startApp() throws MIDletStateChangeException;

    protected abstract void pauseApp();

    protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    /** Cho lớp nền Android gọi các hàm vòng đời protected. */
    public static void batDau(MIDlet m) throws MIDletStateChangeException {
        m.startApp();
    }

    public static void tamDung(MIDlet m) {
        m.pauseApp();
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
        Hashtable t = thuocTinh();
        return key == null ? null : (String) t.get(key);
    }

    /** Thuộc tính ứng dụng (htbb-app.properties) - cùng tên với bản PC, cho mã dùng chung (htbb.p2p.KetNoiP2P). */
    public static String thuocTinh(String key) {
        return key == null ? null : (String) thuocTinh().get(key);
    }

    private static synchronized Hashtable thuocTinh() {
        if (thuocTinh == null) {
            thuocTinh = new Hashtable();
            InputStream in = moTaiNguyen("/htbb-app.properties");
            if (in != null) {
                try {
                    java.util.Properties p = new java.util.Properties();
                    p.load(new java.io.InputStreamReader(in, "UTF-8"));
                    for (java.util.Map.Entry e : p.entrySet()) {
                        String v = String.valueOf(e.getValue()).trim();
                        if (v.length() > 0) thuocTinh.put(String.valueOf(e.getKey()).trim(), v);
                    }
                } catch (IOException e) {
                    Log.e(NenAndroid.TAG, "doc htbb-app.properties", e);
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
        Activity a = NenAndroid.hoatDong;
        if (a == null || url == null || url.length() == 0) return false;
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            a.startActivity(i);
        } catch (Throwable e) {
            throw new ConnectionNotFoundException(url);
        }
        return false;
    }

    /**
     * Thay cho Class.getResourceAsStream của J2ME (port-j2me.py đổi mọi "...getClass().getResourceAsStream("
     * thành hàm này): đọc từ assets của APK. null nếu không có (như J2ME).
     */
    public static InputStream moTaiNguyen(String ten) {
        if (ten == null) return null;
        android.content.Context a = NenAndroid.ungDung;
        if (a == null) return null;
        String p = ten;
        while (p.startsWith("/")) p = p.substring(1);
        try {
            return a.getAssets().open(p);
        } catch (IOException e) {
            return null;
        }
    }
}
