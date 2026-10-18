package htbb.pc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Nhật ký bản PC: in ra stderr + tệp <thư mục dữ liệu>/htbb-pc.log (ghi đè mỗi lần mở game). */
public final class NhatKy {
    private static PrintWriter tep;
    private static int soLoi;

    private NhatKy() {
    }

    static synchronized void mo() {
        try {
            File f = new File(CaiDat.thuMucDuLieu(), "htbb-pc.log");
            tep = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"), true);
        } catch (Throwable e) {
            tep = null;
        }
    }

    private static String gio() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    public static synchronized void ghi(String s) {
        String d = gio() + " " + s;
        System.err.println(d);
        if (tep != null) tep.println(d);
    }

    public static synchronized void loi(String s, Throwable e) {
        if (soLoi++ > 500) return;
        ghi("LOI " + s + ": " + e);
        if (e != null) {
            e.printStackTrace();
            if (tep != null) e.printStackTrace(tep);
        }
    }
}
