package htbb.pc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * Cài đặt bản PC (lưu <thư mục dữ liệu>/cai_dat.properties) + thư mục dữ liệu người dùng:
 * Windows %APPDATA%\HTBB, macOS ~/Library/Application Support/HTBB, Linux $XDG_DATA_HOME/htbb (~/.local/share/htbb).
 * Ghi đè: --du-lieu <thư mục> / -Dhtbb.duLieu / biến HTBB_DU_LIEU (vd chơi nhiều tài khoản song song).
 */
public final class CaiDat {
    public static int cuaSoRong = 1280;
    public static int cuaSoCao = 720;
    public static boolean toanMan;
    /** cửa sổ đang phóng to (maximize) lúc thoát - mở lại phóng to; cua_so_rong/cao = cỡ cửa sổ thường */
    public static boolean phongTo;
    public static int amNhac = 80;
    public static int amHieuUng = 90;
    public static boolean tatTieng;
    public static boolean chuHD = true;
    public static boolean khungHD = true;
    /** cũ: dải gợi ý phím tắt (đã bỏ - mọi phím ở trang "Phím tắt" của Cài đặt, F1); luôn tắt */
    public static boolean goiYPhim = false;
    public static boolean hienFps;
    /** 0.9.5: bộ đo khung (nhà phát triển): ms từng pha, GC, cấp phát */
    public static boolean doKhung;
    /** cờ nhà phát triển (--fps hoặc -Dhtbb.dev=1): cho lớp FPS / đo khung chạy cả ở bản phát hành */
    public static boolean dev = "1".equals(System.getProperty("htbb.dev"));
    /** Hiệu ứng cao: hiệu ứng HD + trình bày 60 khung/giây; thấp: chỉ khung game (máy yếu) */
    public static boolean hieuUngCao = true;
    /** cũ (F1 bật/tắt dải gợi ý phím; nay F1 mở trang Phím tắt) - giữ để đọc/ghi tệp cũ */
    public static boolean goiYMo = true;
    /** máy chủ ghi trong cài đặt (rỗng = theo bản build) */
    public static String mayChu = "";
    /** Nhanh p2p: ticket Peerova ghi de ban build (p2p_ticket= trong cai_dat.properties), "" = theo ban build. */
    public static String p2pTicket = "";

    /** bản thử (build.sh mặc định: HTBB-Ban-Thu=1) hoặc cờ dev - mới có công tắc / lớp FPS, đo khung */
    public static boolean choDo() {
        return dev || "1".equals(javax.microedition.midlet.MIDlet.thuocTinh("HTBB-Ban-Thu"));
    }

    /** 0.9.6.1: lớp chữ FPS / độ phân giải góc trái dưới - bản phát hành TẮT (kể cả hien_fps=1 lưu từ bản cũ). */
    public static boolean hienLopDo() {
        return (hienFps || doKhung) && choDo();
    }

    private static File thuMuc;
    /** lần chạy đầu (chưa có cai_dat.properties) */
    public static boolean lanDau = true;

    private CaiDat() {
    }

    public static synchronized File thuMucDuLieu() {
        if (thuMuc != null) return thuMuc;
        String d = System.getProperty("htbb.duLieu");
        if (d == null || d.length() == 0) d = System.getenv("HTBB_DU_LIEU");
        File f;
        if (d != null && d.length() > 0) {
            f = new File(d);
        } else {
            String os = System.getProperty("os.name", "").toLowerCase();
            String home = System.getProperty("user.home", ".");
            if (os.indexOf("win") >= 0) {
                String ad = System.getenv("APPDATA");
                f = new File(ad != null && ad.length() > 0 ? ad : home, "HTBB");
            } else if (os.indexOf("mac") >= 0) {
                f = new File(home, "Library/Application Support/HTBB");
            } else {
                String x = System.getenv("XDG_DATA_HOME");
                f = new File(x != null && x.length() > 0 ? x : home + "/.local/share", "htbb");
            }
        }
        if (!f.isDirectory()) f.mkdirs();
        thuMuc = f;
        return f;
    }

    private static File tep() {
        return new File(thuMucDuLieu(), "cai_dat.properties");
    }

    private static int so(Properties p, String k, int md, int min, int max) {
        try {
            int v = Integer.parseInt(p.getProperty(k, "").trim());
            return Math.max(min, Math.min(max, v));
        } catch (Exception e) {
            return md;
        }
    }

    private static boolean co(Properties p, String k, boolean md) {
        String v = p.getProperty(k);
        return v == null ? md : "1".equals(v.trim()) || "true".equalsIgnoreCase(v.trim());
    }

    public static synchronized void nap() {
        Properties p = new Properties();
        File f = tep();
        if (f.isFile()) {
            lanDau = false;
            try {
                InputStreamReader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
                try {
                    p.load(r);
                } finally {
                    r.close();
                }
            } catch (Exception e) {
                NhatKy.loi("doc cai dat", e);
            }
        }
        cuaSoRong = so(p, "cua_so_rong", 1280, 640, 7680);
        cuaSoCao = so(p, "cua_so_cao", 720, 400, 4320);
        toanMan = co(p, "toan_man", false);
        phongTo = co(p, "phong_to", false);
        amNhac = so(p, "am_nhac", 80, 0, 100);
        amHieuUng = so(p, "am_hieu_ung", 90, 0, 100);
        tatTieng = co(p, "tat_tieng", false);
        // co_giao_dien / zoom_bu của tệp cũ: bỏ qua (cỡ giao diện + thu phóng đã bỏ)
        chuHD = co(p, "chu_hd", true);
        khungHD = co(p, "khung_hd", true);
        // goi_y_phim của tệp cũ: bỏ qua (dải gợi ý phím đã bỏ)
        hienFps = co(p, "hien_fps", false);
        doKhung = co(p, "do_khung", false);
        hieuUngCao = co(p, "hieu_ung_cao", true);
        goiYMo = co(p, "goi_y_mo", true);
        mayChu = p.getProperty("may_chu", "").trim();
        p2pTicket = p.getProperty("p2p_ticket", "").trim();
        BanPhim.nap(p);
    }

    public static synchronized void luu() {
        Properties p = new Properties();
        p.setProperty("cua_so_rong", String.valueOf(cuaSoRong));
        p.setProperty("cua_so_cao", String.valueOf(cuaSoCao));
        p.setProperty("toan_man", toanMan ? "1" : "0");
        p.setProperty("phong_to", phongTo ? "1" : "0");
        p.setProperty("am_nhac", String.valueOf(amNhac));
        p.setProperty("am_hieu_ung", String.valueOf(amHieuUng));
        p.setProperty("tat_tieng", tatTieng ? "1" : "0");
        p.setProperty("chu_hd", chuHD ? "1" : "0");
        p.setProperty("khung_hd", khungHD ? "1" : "0");
        p.setProperty("goi_y_phim", "0");
        p.setProperty("hien_fps", hienFps ? "1" : "0");
        p.setProperty("do_khung", doKhung ? "1" : "0");
        p.setProperty("hieu_ung_cao", hieuUngCao ? "1" : "0");
        p.setProperty("goi_y_mo", goiYMo ? "1" : "0");
        if (mayChu.length() > 0) p.setProperty("may_chu", mayChu);
        if (p2pTicket.length() > 0) p.setProperty("p2p_ticket", p2pTicket);
        BanPhim.luu(p);
        try {
            File f = tep();
            File tam = new File(f.getPath() + ".tam");
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(tam), "UTF-8");
            try {
                p.store(w, "Cai dat ban PC Huyen Thoai Bao Boi (sua trong game: F10)");
            } finally {
                w.close();
            }
            if (!tam.renameTo(f)) {
                f.delete();
                tam.renameTo(f);
            }
        } catch (Exception e) {
            NhatKy.loi("ghi cai dat", e);
        }
    }

    /** Âm lượng tổng -> lớp MMAPI. */
    public static void apAmThanh() {
        javax.microedition.media.Manager.mucNhac = tatTieng ? 0 : amNhac;
        javax.microedition.media.Manager.mucHieuUng = tatTieng ? 0 : amHieuUng;
        javax.microedition.media.Manager.apAmLuongTong();
    }
}
