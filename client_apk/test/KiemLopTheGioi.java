import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * KIỂM HỒI QUY 0.9.6.2 (APK): lớp thế giới phóng x2 (HdZoom) phải KẾT THÚC trước mọi lối thoát của GameScr.paint.
 *
 * Lỗi 0.9.5 - 0.9.6.1: Menu chính (ManMenu), bảng Nhân vật, Cửa hàng... vẽ thế giới làm nền bằng GameScr.veNen
 * (laNen = true). GameScr.paint "if (laNen) return;" thoát TRƯỚC HdZoom.ket -> phép phóng x2 còn bật khi bảng vẽ:
 * menu phóng to tràn mép phải / đáy, vùng chạm (VungCham, toạ độ chưa phóng) lệch so với hình -> "không bấm được",
 * "bấm kỹ năng ra cửa hàng". Bản PC đúng (htbb.pc.Zoom.raTheGioi ngay sau SinhThai.veDem).
 *
 * Chạy port-j2me.py của APK vào thư mục tạm rồi đọc GameScr.paint đã vá: sau HdZoom.batDau, không lệnh "return" nào
 * trước HdZoom.ket đầu tiên; HdZoom.ket nằm trước "if (laNen)".
 *
 *   javac -d $O client_apk/test/KiemLopTheGioi.java && java -cp $O KiemLopTheGioi     # chạy ở gốc repo; mã 1 = hỏng
 */
public class KiemLopTheGioi {
    static int dat, hong;

    static void kiem(boolean ok, String ten) {
        if (ok) dat++;
        else {
            hong++;
            System.out.println("HONG: " + ten);
        }
    }

    public static void main(String[] a) throws Exception {
        File ra = Files.createTempDirectory("htbb-port-apk").toFile();
        Process p = new ProcessBuilder("python3", "-I", "client_apk/port-j2me.py", "client_jar/src", "client_jar/shared/mod",
                ra.getPath(), "906").inheritIO().start();
        kiem(p.waitFor() == 0, "port-j2me.py (APK) chạy được");
        File f = new File(ra, "game/render/screen/GameScr.java");
        kiem(f.isFile(), "có GameScr.java đã vá");
        if (!f.isFile()) {
            System.exit(1);
        }
        String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        int dau = s.indexOf("    public void paint(Graphics g) {");
        int het = dau < 0 ? -1 : s.indexOf("\n    }\n", dau);
        kiem(dau >= 0 && het > dau, "tìm thấy GameScr.paint");
        String than = s.substring(Math.max(0, dau), Math.max(dau, het));
        int bd = than.indexOf("HdZoom.batDau(");
        int kt = than.indexOf("HdZoom.ket(", Math.max(0, bd));
        int laNen = than.indexOf("if (laNen)");
        kiem(bd >= 0, "GameScr.paint bật lớp thế giới (HdZoom.batDau)");
        kiem(kt > bd, "GameScr.paint kết lớp thế giới (HdZoom.ket) sau batDau");
        kiem(laNen > 0 && kt > 0 && kt < laNen, "HdZoom.ket TRƯỚC 'if (laNen)' (bảng vẽ thế giới làm nền không bị phóng x2)");
        if (bd >= 0 && kt > bd) {
            String giua = than.substring(bd, kt);
            int r = giua.indexOf("return");
            kiem(r < 0, "không có 'return' giữa HdZoom.batDau và HdZoom.ket" + (r < 0 ? "" : ": ..." + giua.substring(Math.max(0, r - 120), r + 6)));
        }
        System.out.println("KiemLopTheGioi: DAT " + dat + ", HONG " + hong);
        System.exit(hong == 0 ? 0 : 1);
    }
}
