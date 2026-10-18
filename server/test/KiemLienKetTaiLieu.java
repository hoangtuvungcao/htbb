import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Moi lien ket trong tai lieu .md (docs/, README goc va README tung thanh phan) phai tro toi mot file CO THAT.
 *
 * VI SAO CAN. Lien ket markdown hong KHONG bao gio bao loi - no chi im lang dan nguoi doc
 * vao hu khong. Da xay ra that: doi thu muc Client/ -> ClientUnity/ hoi truoc lam chet 7
 * lien ket trong docs/, va tuy da co test/KiemClientUnity.java khang dinh thu muc Client
 * khong con ton tai, KHONG AI kiem tai lieu, nen 7 lien ket do chet am tham tu do.
 *
 * Cung bat luon mot loi de dinh khac: lien ket viet tu GOC REPO ("src/real/Item.java")
 * thay vi tuong doi voi chinh tep tai lieu ("../src/real/Item.java"). Ca hai trinh xem
 * (VS Code) va GitHub deu giai duong dan tuong doi voi tep chua no, nen dang viet tu goc
 * repo trong docs/ luon tro sai - ma nhin bang mat thi rat giong dung.
 *
 * Chi kiem phan DUONG DAN, khong kiem so dong sau dau #L: so dong roi theo moi lan sua ma
 * nguon, ghim lai o day chi tao ra bao dong gia.
 */
public final class KiemLienKetTaiLieu {

    /** [chu](duong/dan) - bo qua lien ket ngoai (http, mailto) va neo trong tep (#muc). */
    private static final Pattern LIEN_KET = Pattern.compile("\\[[^\\]]*\\]\\(([^)\\s]+)\\)");

    public static void chay() {
        Kiem.nhom("Lien ket trong tai lieu .md phai tro toi file co that");

        final List<Path> tep = new ArrayList<Path>();
        themTep(tep, Paths.get("README.md"));
        themThuMuc(tep, Paths.get("../docs"));
        // README goc va README tung thanh phan - bat buoc phai co (xoa / doi ten la bao).
        final String[] readme = {
            "../README.md", "../sql/README.md", "../tools/README.md", "../du_lieu/README.md",
            "../vps/README.md", "../vps/win/README.md", "../web/README.md", "../client_jar/README.md",
            "../client_apk/README.md", "../client_pc/README.md", "../client_pc_ios/README.md",
            "../tools/phat-hanh/README.md",
        };
        for (final String r : readme) {
            Kiem.dung("co tai lieu " + r, Files.isRegularFile(Paths.get(r)));
            themTep(tep, Paths.get(r));
        }
        // Bo tai lieu docs/ chuan (README goc tro vao tung tep).
        for (final String d : new String[]{"kien-truc", "build", "trien-khai", "van-hanh", "cau-hinh",
                "thiet-ke-game", "giay-phep"}) {
            Kiem.dung("co docs/" + d + ".md", Files.isRegularFile(Paths.get("../docs/" + d + ".md")));
        }

        Kiem.dung("tim thay tai lieu de kiem (tren 15 tep)", tep.size() > 15);

        int tongLienKet = 0;
        final List<String> hong = new ArrayList<String>();
        for (final Path p : tep) {
            final String noiDung = doc(p);
            if (noiDung.isEmpty()) {
                continue;
            }
            final Matcher m = LIEN_KET.matcher(noiDung);
            while (m.find()) {
                final String dich = m.group(1);
                if (boQua(dich)) {
                    continue;
                }
                tongLienKet++;
                // Cat phan neo #L123 / #muc-nao-do truoc khi kiem su ton tai.
                String duong = dich;
                final int dauThang = duong.indexOf('#');
                if (dauThang >= 0) {
                    duong = duong.substring(0, dauThang);
                }
                if (duong.isEmpty()) {
                    continue;
                }
                duong = giaiMaPhanTram(duong);
                // Giai tuong doi voi THU MUC CHUA tep tai lieu - dung nhu VS Code va GitHub.
                final Path chaMe = p.getParent() == null ? Paths.get(".") : p.getParent();
                final File dichThuc = chaMe.resolve(duong).normalize().toFile();
                if (!dichThuc.exists()) {
                    hong.add(p + " -> " + dich);
                }
            }
        }

        Kiem.dung("co doc duoc lien ket (tren 50)", tongLienKet > 50);

        // In ra tung cai de sua duoc ngay, khong phai do lai bang tay.
        for (final String h : hong) {
            System.out.println("        hong: " + h);
        }
        Kiem.bang("khong co lien ket nao tro vao hu khong",
                Integer.valueOf(0), Integer.valueOf(hong.size()));
    }

    /** Lien ket ngoai / neo trong tep / cho trong kieu <...> thi khong kiem. */
    private static boolean boQua(final String dich) {
        final String d = dich.trim().toLowerCase();
        return d.startsWith("http://") || d.startsWith("https://") || d.startsWith("mailto:")
                || d.startsWith("#") || d.startsWith("<") || d.startsWith("data:");
    }

    /** %20 trong duong dan markdown -> dau cach. */
    private static String giaiMaPhanTram(final String s) {
        return s.replace("%20", " ");
    }

    private static void themTep(final List<Path> ra, final Path p) {
        if (Files.isRegularFile(p)) {
            ra.add(p);
        }
    }

    private static void themThuMuc(final List<Path> ra, final Path thuMuc) {
        if (!Files.isDirectory(thuMuc)) {
            return;
        }
        final File[] ds = thuMuc.toFile().listFiles();
        if (ds == null) {
            return;
        }
        for (final File f : ds) {
            if (f.isFile() && f.getName().endsWith(".md")) {
                ra.add(f.toPath());
            }
        }
    }

    private static String doc(final Path p) {
        try {
            return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemLienKetTaiLieu() {
    }
}
