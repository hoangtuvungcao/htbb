import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Chan chuoi tieng Viet bi HONG BANG MA (mojibake) lot vao ma nguon.
 *
 * CHUYEN DA XAY RA THAT: sua file bang mot cong cu doc file kem ":encoding(UTF-8)" roi
 * ghi ra ma khong dat lop ma hoa cho dau ra. Cong cu chi in mot dong canh bao "Wide
 * character in print" roi VAN GHI — toan bo tieng Viet trong file bi ma hoa hai lan.
 * Hai file (DatabaseGiftCode.java, RequestNPCInfoHandler.java) hong 178 dong, va nguoi
 * choi thay hop thoai trong game mat het dau.
 *
 * VI SAO PHAI CHAN O DAY: loi nay AM. File van la UTF-8 HOP LE nen "iconv -f UTF-8
 * -t UTF-8" van bao dat; javac van bien dich troi chay; khong co canh bao nao luc build.
 * Chi mo game len nhin moi biet.
 *
 * CACH NHAN BIET: van ban UTF-8 bi doc nham thanh Latin-1 roi ma hoa lai luon sinh ra
 * cac ky tu duoi day. Tieng Viet viet dung khong bao gio chua chung.
 *
 * Chu y: cac chuoi dau hieu duoi day viet bang \\u de CHINH FILE NAY khong chua mojibake
 * — khong thi no tu bao chinh no hong.
 */
public final class KiemBangMaNguon {

    /**
     * Dau hieu ma hoa hai lan. Viet bang \\u chu KHONG viet thang ky tu, khong thi chinh
     * file nay lai bi phep kiem cua no bao la hong.
     *
     *   U+00C3          = chu A hoa co dau nga. Khong dung trong tieng Viet; no chi sinh
     *                     ra khi byte dau cua mot ky tu UTF-8 bi doc nham thanh Latin-1.
     *   U+00E1 U+00BA   } hai cap sinh ra tu dai ky tu tieng Viet co dau
     *   U+00E1 U+00BB   } (U+1EA0..U+1EFF) khi bi ma hoa hai lan.
     *
     * KHONG dua u-moc (U+01B0) hay d-gach (U+0111) vao day: do la ky tu tieng Viet THAT,
     * dua vao la bao nham toan bo ma nguon.
     */
    /** Byte dan dau cua mot ky tu UTF-8, sau khi bi doc nham thanh Latin-1. */
    private static final char DAN_DAU_TU = 0x00C2;
    private static final char DAN_DAU_DEN = 0x00E3;

    /** Byte tiep theo cua chinh ky tu do, cung sau khi bi doc nham thanh Latin-1. */
    private static final char TIEP_TU = 0x0080;
    private static final char TIEP_DEN = 0x00BF;

    /**
     * Van ban co bi ma hoa hai lan khong.
     *
     * Nhan biet bang CAP ky tu chu khong phai tung ky tu: mot ky tu tieng Viet trong
     * UTF-8 gom mot byte dan dau (0xC2..0xE3) va mot hay hai byte tiep theo (0x80..0xBF).
     * Bi doc nham thanh Latin-1 thi tung byte do thanh mot ky tu rieng, nen trong van ban
     * hong luon co mot ky tu 0xC2..0xE3 DUNG NGAY TRUOC mot ky tu 0x80..0xBF.
     *
     * DUNG bat theo tung ky tu le: chinh toi da mac loi do o ban dau, lay 0x00C3 lam dau
     * hieu — nhung do la chu "A hoa dau nga", mot chu CAI TIENG VIET THAT (trong "MA",
     * "XA", "DA" viet hoa). No bao nham Map.java (mot day ky tu trong bieu thuc chinh
     * quy) va kiem_log_online.php (chu "MA" trong cau chu thich).
     */
    private static boolean coDauHieuHong(final String s) {
        for (int i = 0; i + 1 < s.length(); i++) {
            final char a = s.charAt(i);
            final char b = s.charAt(i + 1);
            if (a >= DAN_DAU_TU && a <= DAN_DAU_DEN && b >= TIEP_TU && b <= TIEP_DEN) {
                return true;
            }
        }
        return false;
    }

    /**
     * File DA CO SAN mojibake tu truoc, khong phai do lan sua nay gay ra.
     *
     * De nguyen chu khong "sua cho dep": phan lon la du lieu (cau SQL, chuoi trong trang
     * admin) ma sua lai co the doi y nghia du lieu that. Liet ke ra day de phep kiem van
     * bat duoc file MOI bi hong.
     */
    private static final List<String> BO_QUA = Arrays.asList(
        // sql/buff_ddsa.sql da xoa ngay 02/09/2026 (chua tung ap dung vao production,
        // noi dung con bi loi bang ma) - xem sql/README.md muc "Da xoa".
        "sql/them_bang_cam_may.sql",
        "sql/gamedb7_clean.sql",
        "vps/db_new_server.sql",
        "vps/sql-giftcode.md",
        "web/admin/chandoan.php",
        "web/admin/cammay.php",
        "web/admin/nhanvat.php",
        "web/admin/inc/layout.php",
        "web/admin/test/kiem_duong_dan.php"
    );

    private static final String[] THU_MUC = { "src", "test", "tools", "web", "sql" };
    private static final String[] DUOI = { ".java", ".php", ".sql", ".md" };

    public static void chay() {
        Kiem.nhom("Bang ma nguon - khong co chuoi hong (mojibake)");

        final List<String> hong = new ArrayList<String>();
        int daQuet = 0;
        for (final String tm : THU_MUC) {
            final Path goc = Paths.get(tm);
            // PHAI bao loi khi thu muc khong con: ban cu chi `continue`, nen doi ten hay
            // di chuyen mot thu muc la pham vi quet AM THAM teo lai ma phep kiem van bao
            // "dat" - tuc lop bao ve chinh chong mojibake bien mat khong ai biet. Chan
            // duoi (daQuet > 50) khong du: chi can src/ con nguyen la da qua nguong do.
            Kiem.dung("thu muc " + tm + " con ton tai (khong thi pham vi quet bi teo am tham)",
                    Files.isDirectory(goc));
            if (!Files.isDirectory(goc)) {
                continue;
            }
            try (Stream<Path> s = Files.walk(goc)) {
                for (final Path p : (Iterable<Path>) s.filter(Files::isRegularFile)::iterator) {
                    final String duong = p.toString().replace('\\', '/');
                    if (!coDuoiQuanTam(duong) || BO_QUA.contains(duong)) {
                        continue;
                    }
                    daQuet++;
                    if (coDauHieuHong(doc(p))) {
                        hong.add(duong);
                    }
                }
            } catch (final IOException e) {
                // Doc khong duoc mot file thi bo qua file do, khong lam hong ca phep kiem.
            }
        }

        Kiem.dung("quet duoc ma nguon (" + daQuet + " file)", daQuet > 50);
        if (!hong.isEmpty()) {
            System.out.println("    File hong bang ma — mo ra xem, tieng Viet trong do da bi ma hoa hai lan:");
            for (final String h : hong) {
                System.out.println("      - " + h);
            }
            System.out.println("    Khoi phuc bang: git checkout <commit-con-sach> -- <file>");
        }
        Kiem.bang("so file hong bang ma", 0, hong.size());
    }

    private static boolean coDuoiQuanTam(final String duong) {
        for (final String d : DUOI) {
            if (duong.endsWith(d)) {
                return true;
            }
        }
        return false;
    }

    private static String doc(final Path p) {
        try {
            return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemBangMaNguon() {
    }
}
