import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Kiem thu viec DONG CUA DANG KY TRONG CLIENT (lenh 39).
 *
 * VI SAO: dang ky da chuyen han sang web (web/register.php), nhung server van dang ky
 * handler cu real.RequestRegisterHandler (nam trong lib/NQSH_5h.jar, KHONG co ma nguon).
 * Do la mot diem TAO TAI KHOAN o trang thai CHUA DANG NHAP, va no khong di qua han muc
 * 5 tai khoan / IP / gio ma web/register.php dem bang bang web_chan_spam — tuc la ai noi
 * dung giao thuc deu lap duoc tai khoan hang loat, khong dau vet.
 *
 * Bai kiem nay canh dung mot thu: lenh 39 KHONG duoc tro lai handler cu.
 */
public final class KiemDongDangKyClient {

    private static final String F_CONTROLLER = "src/real/RealController.java";
    private static final String F_HANDLER = "src/real/cmd/DangKyDaChuyenSangWeb.java";

    public static void chay() {
        Kiem.nhom("Dong cua dang ky trong client (lenh 39)");

        final String dieuPhoi = boChuThich(doc(F_CONTROLLER));
        Kiem.dung("doc duoc " + F_CONTROLLER, dieuPhoi.length() > 0);

        // Diem mau chot: khong duoc dung lai handler tao tai khoan cu.
        Kiem.sai("KHONG con dung real.RequestRegisterHandler",
                dieuPhoi.contains("RequestRegisterHandler"));
        Kiem.dung("lenh 39 tro toi handler moi",
                dieuPhoi.contains("commandHandler.put(39, new real.cmd.DangKyDaChuyenSangWeb())"));

        // Van phai co handler cho lenh 39. Bo han thi lenh roi xuong nhanh cuoi cua
        // processMessage, noi `p` la null (chua dang nhap) va khong dieu kien nao khop
        // -> im lang hoan toan, client cu treo o man hinh dang ky khong hieu chuyen gi.
        Kiem.dung("lenh 39 van co handler (khong de roi vao im lang)",
                dieuPhoi.contains("commandHandler.put(39,"));

        final String handler = doc(F_HANDLER);
        Kiem.dung("doc duoc " + F_HANDLER, handler.length() > 0);
        Kiem.dung("handler moi cai dat ICommandHandler",
                handler.contains("implements ICommandHandler"));
        Kiem.dung("co tra ve loi nhan cho nguoi dung",
                handler.contains("createServerAlertMessage"));
        Kiem.dung("loi nhan chi duong sang web",
                handler.contains("trang web"));

        // In log phai CO HAM: mot ke muon lam day o dia chi can lap lai lenh 39.
        // (Vua mat mot dot vi mot vong lap nem ngoai le sinh 124 MB log moi ngay.)
        Kiem.dung("in log co ham, khong in moi lan bi goi",
                boChuThich(handler).contains("CACH_BAO"));
        Kiem.dung("dem so lan bang kieu an toan da luong",
                handler.contains("AtomicLong"));

        // Ma may: gio chi con den tu goi dang nhap, vi duong dang ky da dong.
        final String camMay = doc("src/data/CamTheoMay.java");
        Kiem.dung("CamTheoMay ghi ro ma may chi con den tu goi dang nhap",
                camMay.contains("CHI den tu goi DANG NHAP"));
    }

    // -----------------------------------------------------------------------
    private static String boChuThich(final String ma) {
        final StringBuilder sb = new StringBuilder(ma.length());
        boolean trongKhoi = false;
        for (final String dong : ma.split("\n", -1)) {
            String t = dong;
            if (trongKhoi) {
                final int het = t.indexOf("*/");
                if (het < 0) {
                    continue;
                }
                t = t.substring(het + 2);
                trongKhoi = false;
            }
            final int mo = t.indexOf("/*");
            if (mo >= 0) {
                final int het = t.indexOf("*/", mo + 2);
                if (het >= 0) {
                    t = t.substring(0, mo) + t.substring(het + 2);
                } else {
                    t = t.substring(0, mo);
                    trongKhoi = true;
                }
            }
            final int hai = t.indexOf("//");
            if (hai >= 0) {
                t = t.substring(0, hai);
            }
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder((int) f.length() + 16);
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
        } catch (final Exception ex) {
            return "";
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }

    private KiemDongDangKyClient() {
    }
}
