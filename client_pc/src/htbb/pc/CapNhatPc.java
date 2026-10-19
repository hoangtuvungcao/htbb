package htbb.pc;

import htbb.capnhat.CapNhatChung;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bộ cập nhật bản PC (0.9.7, game.pokemon.CapNhat tìm theo tên lớp): tải tệp bản mới trong game (tiến độ + SHA-256,
 * htbb.capnhat.TaiVe), rồi
 * <ul>
 *   <li><b>Windows</b> (bộ cài Inno Setup HTBB-PC-Setup-*.exe, cùng AppId): chạy bộ cài chế độ /SILENT (nâng cấp tại chỗ,
 *       giữ dữ liệu %APPDATA%\HTBB; Inno tự hỏi quyền quản trị nếu cài ở Program Files) rồi đóng game; bộ cài tự mở lại
 *       HTBB.exe khi xong ([Run] Check: WizardSilent, client_pc/dong_goi_windows.sh);</li>
 *   <li><b>Linux AppImage</b> ($APPIMAGE): tải vào CÙNG thư mục ($APPIMAGE.tai), chmod +x, đổi tên nguyên tử đè lên
 *       $APPIMAGE (bản đang chạy vẫn chạy tiếp từ inode cũ), mở lại AppImage mới rồi đóng game.</li>
 * </ul>
 * Không cài được ở chỗ đang chạy (chạy từ jar / tar.gz, thư mục AppImage không ghi được, macOS, link sai đuôi) -&gt;
 * {@link #hoTro} = false, hộp CapNhat mở trang tải bằng trình duyệt.
 *
 * Kiểm thử (client_pc/test/KiemCapNhat): {@link #heDieuHanh} / {@link #appImage} / {@link #chayLenh} / {@link #thoat}
 * thay được (không chạy tiến trình thật).
 */
public class CapNhatPc extends CapNhatChung {
    /** "windows" / "linux" / khác; null = theo os.name. */
    public static String heDieuHanh;
    /** Đường AppImage đang chạy; null = biến môi trường APPIMAGE. */
    public static String appImage;
    /** Chạy lệnh (null = ProcessBuilder thật). */
    public static java.util.function.Consumer<List<String>> chayLenh;
    /** Đóng game sau khi giao cho bộ cài / bản mới (null = MIDlet.khiThoat rồi System.exit). */
    public static Runnable thoat;
    /** Chờ trước khi đóng game (người chơi kịp đọc câu báo). */
    public static long choThoatMs = 1500;
    /** Thư mục tải của Windows (null = %TEMP%\HTBB-capnhat). */
    public static File thuMucTai;

    static String os() {
        if (heDieuHanh != null) {
            return heDieuHanh;
        }
        String o = System.getProperty("os.name", "").toLowerCase();
        return o.contains("win") ? "windows" : o.contains("linux") ? "linux" : o;
    }

    static File fileAppImage() {
        String a = appImage != null ? appImage : System.getenv("APPIMAGE");
        if (a == null || a.length() == 0) {
            return null;
        }
        File f = new File(a);
        return f.isFile() ? f : null;
    }

    static String duoi(String url) {
        String u = url == null ? "" : url.toLowerCase();
        int q = u.indexOf('?');
        return q >= 0 ? u.substring(0, q) : u;
    }

    protected boolean coTheCai(String url) {
        String o = os();
        String d = duoi(url);
        if ("windows".equals(o)) {
            return d.endsWith(".exe");
        }
        if ("linux".equals(o)) {
            File a = fileAppImage();
            return a != null && d.endsWith(".appimage") && a.getAbsoluteFile().getParentFile() != null
                    && a.getAbsoluteFile().getParentFile().canWrite();
        }
        return false;
    }

    protected File noiTai(String url, String phienBan) throws IOException {
        if ("linux".equals(os())) {
            File a = fileAppImage();
            if (a == null) {
                throw new IOException("Không thấy tệp AppImage đang chạy");
            }
            // cùng thư mục -> đổi tên nguyên tử
            return new File(a.getAbsoluteFile().getParentFile(), "." + a.getName() + ".tai");
        }
        File t = thuMucTai != null ? thuMucTai : new File(System.getProperty("java.io.tmpdir"), "HTBB-capnhat");
        return new File(t, tenTep(url, "HTBB-PC-Setup.exe"));
    }

    protected String cai(File tep) throws Exception {
        if ("linux".equals(os())) {
            File a = fileAppImage();
            if (a == null) {
                throw new IOException("Không thấy tệp AppImage đang chạy");
            }
            if (!tep.setExecutable(true, false) && !tep.canExecute()) {
                throw new IOException("Không cho chạy được tệp mới (chmod +x)");
            }
            if (!tep.getAbsoluteFile().equals(a.getAbsoluteFile())) {
                htbb.capnhat.TaiVe.doiTen(tep, a);   // tepXong = AppImage mới tại chỗ
                tepXong = a;
            }
            List<String> l = new ArrayList<String>();
            l.add(a.getAbsolutePath());
            String[] ts = Chay.thamSo;
            if (ts != null) {
                for (String s : ts) {
                    l.add(s);
                }
            }
            chay(l);
            dongGame();
            return "Đã cập nhật lên " + (phienBan.length() > 0 ? phienBan : "bản mới") + ". Đang mở lại game…";
        }
        List<String> l = new ArrayList<String>();
        l.add(tep.getAbsolutePath());
        // Inno Setup: nâng cấp tại chỗ (cùng AppId), không hỏi lại thư mục; /CLOSEAPPLICATIONS đóng HTBB.exe còn mở
        l.add("/SILENT");
        l.add("/SP-");
        l.add("/SUPPRESSMSGBOXES");
        l.add("/NOCANCEL");
        l.add("/NORESTART");
        l.add("/CLOSEAPPLICATIONS");
        chay(l);
        dongGame();
        return "Đang cài bản " + (phienBan.length() > 0 ? phienBan : "mới") + "… Game sẽ đóng và tự mở lại khi cài xong.";
    }

    static void chay(List<String> lenh) throws IOException {
        java.util.function.Consumer<List<String>> c = chayLenh;
        if (c != null) {
            c.accept(lenh);
            return;
        }
        ProcessBuilder pb = new ProcessBuilder(lenh);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(System.getProperty("java.io.tmpdir"), "htbb-capnhat.log")));
        pb.start();
    }

    /** Đóng game sau một chút (luồng tải; hộp kịp hiện câu báo). */
    static void dongGame() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(choThoatMs);
                } catch (InterruptedException e) {
                    // đóng luôn
                }
                Runnable r = thoat;
                if (r != null) {
                    r.run();
                    return;
                }
                try {
                    Runnable k = javax.microedition.midlet.MIDlet.khiThoat;
                    if (k != null) {
                        k.run();
                    }
                } finally {
                    System.exit(0);
                }
            }
        }, "HTBB-CapNhat-thoat");
        t.setDaemon(false);
        t.start();
    }
}
