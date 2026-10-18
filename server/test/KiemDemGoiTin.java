import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bo dem goi tin chan doan (io.DemGoiTin) - xem chu thich dau lop do ve ly do ton tai.
 *
 * Kiem hai lop: (1) logic dem/dinh dang chay dung va KHONG lam gi khi khong theo doi ai;
 * (2) hai diem moc trong io.Session co mat va dat dung cho, de bo dem that su thay moi goi.
 */
public final class KiemDemGoiTin {

    private KiemDemGoiTin() {
    }

    public static void chay() {
        Kiem.nhom("Dem goi tin chan doan (DemGoiTin) - bat/tat bang tep, chi dem nguoi duoc chon");

        kiemDocTep();
        kiemDemVaDinhDang();
        kiemDiemMoc();
    }

    private static void kiemDocTep() {
        // Khong co tep -> rong, khong nem loi.
        final Path khongCo = Paths.get("khong_ton_tai_" + System.nanoTime() + ".txt");
        Kiem.dung("khong co tep -> danh sach rong", io.DemGoiTin.docTep(khongCo).isEmpty());
        Kiem.dung("duong dan null -> danh sach rong", io.DemGoiTin.docTep(null).isEmpty());

        // Tep that: ten viet hoa, dong trong, dong chu thich, khoang trang thua.
        Path tam = null;
        try {
            tam = Files.createTempFile("kiem_dem_goi_tin_", ".txt");
            Files.write(tam, Arrays.asList(
                    "# day la chu thich",
                    "  TenNhanVat  ",
                    "",
                    "nguoi_thu_hai"), StandardCharsets.UTF_8);
            final Set<String> ds = io.DemGoiTin.docTep(tam);
            Kiem.bang("doc duoc dung 2 ten (bo # va dong trong)", Integer.valueOf(2), Integer.valueOf(ds.size()));
            Kiem.dung("ten ve chu thuong va cat khoang trang", ds.contains("tennhanvat"));
            Kiem.dung("ten thu hai giu nguyen", ds.contains("nguoi_thu_hai"));
        } catch (final IOException e) {
            Kiem.dung("tao duoc tep tam de test", false);
        } finally {
            if (tam != null) {
                try {
                    Files.deleteIfExists(tam);
                } catch (final IOException e) {
                }
            }
        }
    }

    private static void kiemDemVaDinhDang() {
        // Khong theo doi ai -> ghi la khong lam gi, khong nem loi.
        io.DemGoiTin.datDanhSachTheoDoi(Collections.<String>emptySet());
        io.DemGoiTin.ghiRa("abc", 4);
        io.DemGoiTin.ghiVao("abc", 3);
        io.DemGoiTin.ghiRa(null, 4);
        io.DemGoiTin.ghiRa("", 4);
        Kiem.dung("khong theo doi -> khong dem gi (RA(0))",
                io.DemGoiTin.tomTatHienTai("abc").contains("RA(0):"));

        // Theo doi "ABC" - khong phan biet hoa/thuong o ca hai phia.
        io.DemGoiTin.datDanhSachTheoDoi(new HashSet<String>(Arrays.asList("ABC")));
        io.DemGoiTin.ghiRa("Abc", 4);
        io.DemGoiTin.ghiRa("abc", 4);
        io.DemGoiTin.ghiRa("ABC", 4);
        io.DemGoiTin.ghiRa("abc", 8);
        io.DemGoiTin.ghiRa("abc", 8);
        io.DemGoiTin.ghiRa("abc", -71);
        io.DemGoiTin.ghiVao("abc", 3);
        io.DemGoiTin.ghiRa("xyz", 4); // khong theo doi
        io.DemGoiTin.ghiRa("xyz", 4);

        final String t = io.DemGoiTin.tomTatHienTai("abc");
        Kiem.dung("dong tom tat bat dau bang [GOI] va ten", t.startsWith("[GOI] abc "));
        Kiem.dung("RA tong = 6", t.contains("RA(6):"));
        Kiem.dung("sap giam dan: 4x3 truoc 8x2 truoc -71x1",
                t.indexOf("4x3") > 0
                && t.indexOf("4x3") < t.indexOf("8x2")
                && t.indexOf("8x2") < t.indexOf("-71x1"));
        Kiem.dung("VAO(1): 3x1", t.contains("VAO(1): 3x1"));
        Kiem.dung("nguoi khong theo doi (xyz) khong duoc dem",
                io.DemGoiTin.tomTatHienTai("xyz").contains("RA(0):"));

        // Cung so lan thi sap theo ma lenh tang - dinh dang on dinh de doc bang mat.
        io.DemGoiTin.datDanhSachTheoDoi(new HashSet<String>(Arrays.asList("d")));
        io.DemGoiTin.ghiRa("d", 60);
        io.DemGoiTin.ghiRa("d", 4);
        io.DemGoiTin.ghiRa("d", -44);
        final String t2 = io.DemGoiTin.tomTatHienTai("d");
        Kiem.dung("cung so lan -> theo ma tang: -44x1 4x1 60x1",
                t2.contains("RA(3): -44x1 4x1 60x1"));

        // Don sach cho cac test khac (neu co) - khong de danh sach theo doi dinh lai.
        io.DemGoiTin.datDanhSachTheoDoi(Collections.<String>emptySet());
    }

    private static void kiemDiemMoc() {
        final String s = doc("src/io/Session.java");
        Kiem.dung("doc duoc src/io/Session.java", !s.isEmpty());
        if (s.isEmpty()) {
            return;
        }
        // Chieu RA: moc trong sendMessage, TRUOC vong chon nhom gui (moi goi deu qua day).
        final int mocRa = s.indexOf("DemGoiTin.ghiRa(this.charname, m.command)");
        final int vongNhom = s.indexOf("for(int i = 0; i < hasGroup.size(); ++i)");
        Kiem.dung("Session.sendMessage goi DemGoiTin.ghiRa(this.charname, m.command)", mocRa >= 0);
        Kiem.dung("moc RA nam truoc vong chon nhom gui", mocRa >= 0 && vongNhom > mocRa);

        // Chieu VAO: moc ngay TRUOC processMessage - va chi co mot cho processMessage.
        final int mocVao = s.indexOf("DemGoiTin.ghiVao(this.charname, message.command)");
        final int xuLy = s.indexOf("this.messageHandler.processMessage(this, message)");
        Kiem.dung("Session goi DemGoiTin.ghiVao(this.charname, message.command)", mocVao >= 0);
        Kiem.dung("moc VAO nam ngay truoc processMessage", mocVao >= 0 && xuLy > mocVao && xuLy - mocVao < 200);
        Kiem.bang("chi co MOT cho processMessage (moc phu het duong vao)",
                Integer.valueOf(1), Integer.valueOf(dem(s, "this.messageHandler.processMessage(this, message)")));

        // Tep co va jar thu khong duoc commit.
        final String gi = doc("../.gitignore");
        Kiem.dung(".gitignore bo qua debug_goi_tin.txt", gi.contains("debug_goi_tin.txt"));
        Kiem.dung(".gitignore bo qua jar thu htbb-server_TRUOC_2480c47_de_thu.jar", gi.contains("htbb-server_TRUOC_2480c47_de_thu.jar"));
    }

    private static int dem(final String s, final String mau) {
        int n = 0;
        int i = 0;
        while ((i = s.indexOf(mau, i)) >= 0) {
            n++;
            i += mau.length();
        }
        return n;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
