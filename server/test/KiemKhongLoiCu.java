import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * P3 (10/2026): server build KHONG con lib/htbb-core.jar (lop loi da bien dich cua game goc).
 *
 * VI SAO: truoc P3, 169 lop loi (io.SessionManager, real.Actor, real.cmd.CreateCharHandler,
 * real.ItemTemplates, data.NewClan, cho, loi dai, MOBA...) chi co dang bytecode trong jar do va goi
 * nguoc lop cua src/ theo ten, nen khong doi ten duoc lop nao. P3 dich nguoc chung thanh ma nguon
 * (dau tep "PHUC HOI TU LOI CU", nguon goc: docs/giay-phep.md) va bo jar khoi build + Class-Path.
 * Phep kiem nay chan jar quay lai qua bat ky script build / CI / manifest nao, va chan lop phuc hoi
 * bi xoa mat (lop con lai trong src/ goi chung - thieu la loi bien dich, nhung lop chi nap bang
 * Class.forName thi khong).
 */
public final class KiemKhongLoiCu {

    /** So lop phuc hoi o P3 (scratchpad jaronly.txt: 169 lop chi co trong jar). */
    static final int SO_LOP_PHUC_HOI = 169;

    private static final String[] TEP_BUILD = {
        "build.bat", "test.bat", "../.github/workflows/server-test.yml", "../docs/build.md",
        "../tools/pokemon/chay_tren_novadb.sh", "../vps/htbb-game.service",
    };

    private static final String[] LOP_LOI = {
        "io.SessionManager", "server.ServerController", "real.Actor", "real.cmd.CreateCharHandler",
        "real.ItemTemplates", "real.QuestTemplate", "data.NewClan", "data.ConnPool", "real.Market", "real.UtilKPAH",
    };

    public static void chay() {
        Kiem.nhom("P3: khong con lib/htbb-core.jar - loi cu da thanh ma nguon src/");
        Kiem.dung("lib/htbb-core.jar da xoa", !new File("lib/htbb-core.jar").exists());
        Kiem.dung("bo do classpath bat duoc dong cu (tu kiem)",
                coTrenClasspath("set \"LIBCP=lib\\jxl-2.6.jar;lib\\htbb-core.jar\"") && !coTrenClasspath("REM lib\\htbb-core.jar da bo"));
        for (final String t : TEP_BUILD) {
            final String s = doc(t);
            Kiem.dung(t + " khong dua htbb-core.jar vao classpath", s != null && !coTrenClasspath(s));
        }
        try (JarFile j = new JarFile("htbb-server.jar")) {
            final String cp = j.getManifest().getMainAttributes().getValue("Class-Path");
            Kiem.dung("MANIFEST htbb-server.jar: Class-Path khong co htbb-core (" + cp + ")",
                    cp != null && !cp.contains("htbb-core") && cp.contains("mysql-connector"));
            for (final String l : LOP_LOI) {
                Kiem.dung("htbb-server.jar co lop phuc hoi " + l, j.getEntry(l.replace('.', '/') + ".class") != null);
            }
        } catch (final IOException e) {
            Kiem.dung("doc duoc htbb-server.jar: " + e, false);
        }
        int n = 0;
        try (Stream<Path> ds = Files.walk(Paths.get("src"))) {
            for (final Path p : (Iterable<Path>) ds.filter(x -> x.toString().endsWith(".java"))::iterator) {
                final String s = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                if (s.substring(0, Math.min(400, s.length())).contains(KiemThuatNgu.DAU_PHUC_HOI)) {
                    n++;
                }
            }
        } catch (final IOException e) {
            Kiem.dung("duyet duoc src/", false);
        }
        Kiem.bang("so lop phuc hoi tu jar cu trong src/ (dau PHUC HOI TU LOI CU)", Integer.valueOf(SO_LOP_PHUC_HOI), Integer.valueOf(n));
        for (final String l : LOP_LOI) {
            Kiem.dung("co ma nguon src/" + l.replace('.', '/') + ".java", new File("src/" + l.replace('.', '/') + ".java").isFile());
        }
    }

    /** Dong classpath (-cp, LIBCP, CP=, Class-Path) con nhac htbb-core.jar. Chu thich lich su thi duoc. */
    static boolean coTrenClasspath(final String s) {
        for (final String d : s.split("\n")) {
            if (d.contains("htbb-core.jar") && (d.contains("-cp") || d.contains("LIBCP") || d.contains("CP=")
                    || d.contains("Class-Path") || d.contains("-classpath"))) {
                return true;
            }
        }
        return false;
    }

    private static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return null;
        }
    }

    private KiemKhongLoiCu() {
    }
}
