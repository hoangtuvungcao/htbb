import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Che mat khau CSDL trong log, va don xac luong chet cua Region.
 *
 * CHUYEN THAT: data.ConnPool (trong lib/NQSH_5h.jar, khong co ma nguon) in ra
 *     URL: jdbc:mysql://... | USER: kpah | PASS: &lt;mat khau that&gt;
 * moi lan tao pool - bon lan moi khoi dong, thang vao /opt/kpah/log/game.out, ma cac
 * tep log de quyen -rw-r--r--. Khong sua duoc trong jar nen chan o dau ra.
 *
 * Test goi HAM THAT chu khong doi chieu chu trong ma nguon: dung mot PrintStream boc
 * LocBiMat roi kiem byte thuc su di ra.
 */
public final class KiemLocBiMat {

    public static void chay() {
        Kiem.nhom("Che mat khau trong log + don luong chet cua Region");

        // ---- Loc that: bom mot dong y het dong ConnPool in ra ----
        final String matKhau = "mAtKhAuThAt_9x7q";
        final Properties p = new Properties();
        p.setProperty("db.password", matKhau);
        p.setProperty("db.pass", matKhau);
        p.setProperty("db.password1", matKhau);
        server.LocBiMat.xoaHet();
        server.LocBiMat.themBiMat(matKhau);

        final String dongThat = "URL: jdbc:mysql://localhost:3306/kpah_db | USER: kpah | PASS: " + matKhau;
        final String daChe = server.LocBiMat.che(dongThat);
        Kiem.sai("dong ConnPool in ra KHONG con chua mat khau", daChe.contains(matKhau));
        Kiem.dung("phan con lai cua dong van giu nguyen (log van doc duoc)",
                daChe.contains("jdbc:mysql://localhost:3306/kpah_db") && daChe.contains("USER: kpah"));

        // ---- Loc o TANG BYTE: bat duoc ca printf va printStackTrace ----
        // Ghi de println(String) thi khong bat duoc printf; loc theo dong thi bat het.
        Kiem.dung("printf cung bi che", !quaLoc(matKhau, true).contains(matKhau));
        Kiem.dung("println cung bi che", !quaLoc(matKhau, false).contains(matKhau));

        // ---- Chuoi qua ngan thi KHONG che, keo an nham ca log ----
        server.LocBiMat.xoaHet();
        server.LocBiMat.themBiMat("abc");
        Kiem.dung("mat khau ngan hon " + server.LocBiMat.DAI_TOI_THIEU + " ky tu thi bo qua",
                server.LocBiMat.che("abc xyz").equals("abc xyz"));
        server.LocBiMat.xoaHet();

        // ---- Thu tu: phai boc TRUOC khi tao pool ----
        final String sv = doc("src/server/TeamServer.java");
        final int viTriLoc = sv.indexOf("LocBiMat.batDau(p);");
        // Tim dang DAY DU cua loi goi that. Dung "Database.setLink(" khong thi bat nham
        // chinh dong chu thich ngay tren LocBiMat.batDau(p) - dong do co nhac ten ham.
        final int viTriPool = sv.indexOf("Database.setLink(p.getProperty(");
        Kiem.dung("TeamServer co goi LocBiMat.batDau", viTriLoc > 0);
        Kiem.dung("boc log TRUOC Database.setLink (ConnPool in mat khau ngay trong ham dung)",
                viTriLoc > 0 && viTriPool > 0 && viTriLoc < viTriPool);

        // ---- Region: khong con luong chet ----
        final String region = doc("src/real/Region.java");
        Kiem.dung("doc duoc src/real/Region.java", region.length() > 1000);
        Kiem.sai("Region KHONG con nem \"Decompilation failed\"",
                region.contains("throw new IllegalStateException(\"Decompilation failed\")"));
        Kiem.sai("KHONG con luong chet nao trong toan bo ma nguon",
                doc("src/real/Map.java").contains("Decompilation failed"));
        // Giu lai update()/updateWind() de con biet tinh nang nao da mat cung luong do.
        Kiem.dung("van giu Region.update() lam dau vet tinh nang da mat",
                region.contains("public void update()"));
    }

    /** Cho mot chuoi di qua LocBiMat that roi lay lai byte o dau ra. */
    private static String quaLoc(final String matKhau, final boolean dungPrintf) {
        final ByteArrayOutputStream thu = new ByteArrayOutputStream();
        final PrintStream cuOut = System.out;
        try {
            System.setOut(new PrintStream(thu, true, "UTF-8"));
            final Properties p = new Properties();
            p.setProperty("db.password", matKhau);
            server.LocBiMat.xoaHet();
            server.LocBiMat.batDau(p);      // boc System.out dang tro toi "thu"
            if (dungPrintf) {
                System.out.printf("PASS: %s%n", matKhau);
            } else {
                System.out.println("PASS: " + matKhau);
            }
            System.out.flush();
        } catch (final IOException e) {
            return "loi: " + e;
        } finally {
            System.setOut(cuOut);
        }
        return new String(thu.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemLocBiMat() {
    }
}
