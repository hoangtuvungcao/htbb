import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh shared/data/danh-muc-thu.json tu chinh mang real.Map.petTemplate.
 *
 * VI SAO PHAI SINH CHU KHONG GO TAY (cung ly do voi tools/SinhDanhMucNgoc.java):
 * ten 30 con thu KHONG co trong CSDL - ghi cung trong src/real/Map.java
 * (loadPetTemplate). Va id o day KHONG PHAI id vat pham: ca hai loai thuong PET
 * va PETV deu doc thang Map.petTemplate[idpet] - MOT MANG CO DUNG 30 O (0..29),
 * KHONG kiem bien:
 *
 *   public void createPet(final int idpet, final int time) {
 *       final Pet pet = new Pet(Map.petTemplate[idpet].idTemplate, ...);
 *
 * id ngoai 0..29 nem ArrayIndexOutOfBoundsException GIUA vong lap phat qua trong
 * DatabaseGiftCode.newDoGetGiftCode - khong chi mat con thu do, ma con lam mat
 * luon dong ghi giftcode_log VA viec tru limit_use nam SAU vong lap trong CUNG
 * khoi try/catch - nghia la nhan lai duoc vo han. Sinh dung tu mang nay la cach
 * duy nhat dam bao bo chon khong bao gio dua ra id ngoai dai.
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "server/build/classes;server/lib/htbb-core.jar;server/lib/mysql-connector-java-5.1.49.jar" SinhDanhMucThu
 *   -> ghi HTBB-Web/shared/data/danh-muc-thu.json (tim ../HTBB-Web, ../web, hoac -Dhtbb.web=<thu muc>).
 *   Web doc thang tep JSON do (server/utils/), khong chep tay nua.
 *
 * Chay lai moi khi them/doi thu trong Map.loadPetTemplate(). test/KiemDanhMucThu
 * doi chieu lai lan nua nen quen chay thi bo kiem se do, khong am tham lech.
 */
public final class SinhDanhMucThu {

    private static final String TEP = "shared/data/danh-muc-thu.json";

    public static void main(final String[] thamSo) throws Exception {
        real.Map.loadPetTemplate();
        final data.PetTemplate[] ds = real.Map.petTemplate;
        if (ds == null || ds.length == 0) {
            System.out.println("[LOI] Map.petTemplate rong - khong sinh gi.");
            System.exit(1);
        }

        final StringBuilder s = new StringBuilder();
        s.append("{\n");
        s.append("  \"_ghiChu\": \"TEP SINH TU DONG - DUNG SUA TAY. Khoa la CHI SO MANG Map.petTemplate (0..")
         .append(ds.length - 1).append("), KHONG phai id vat pham: Char.createPet()/createPetVinhVien() doc thang ")
         .append("Map.petTemplate[idpet] khong kiem bien. test/KiemDanhMucThu (repo game) doi chieu tep nay voi mang Java.\",\n");
        s.append("  \"_nguon\": \"real.Map.petTemplate (src/real/Map.java, loadPetTemplate) - repo game HTBB\",\n");
        s.append("  \"_sinhLai\": \"(trong repo game) java -cp build/classes;lib/htbb-core.jar;lib/mysql-connector-java-5.1.49.jar SinhDanhMucThu\",\n");
        s.append("  \"thu\": {\n");
        int soDong = 0;
        final StringBuilder dong = new StringBuilder();
        for (int i = 0; i < ds.length; i++) {
            final data.PetTemplate t = ds[i];
            if (t == null) {
                continue;
            }
            String ten = t.name == null ? "" : t.name.trim();
            if (ten.isEmpty()) {
                ten = "(khong ten)";
            }
            if (dong.length() > 0) {
                dong.append(",\n");
            }
            dong.append("    \"").append(i).append("\": ").append(jsonChuoi(ten));
            soDong++;
        }
        s.append(dong).append("\n  }\n}\n");

        final String daGhi = ghiVaoWeb(TEP, s.toString());
        System.out.println("Da sinh " + daGhi + " voi " + soDong + " con thu.");
        System.exit(0);
    }

    // ------------------------------------------------------------------ ghi JSON vao HTBB-Web

    /**
     * Thu muc HTBB-Web: -Dhtbb.web=..., bien HTBB_WEB_DIR, ../HTBB-Web (ba repo dat canh
     * nhau) hoac ../web (monorepo). Phai co nuxt.config.ts. Website PHP cu (web/ cua repo
     * game) da bo - xem docs/kien-truc.md.
     */
    private static java.io.File thuMucWeb() {
        final String[] ung = {System.getProperty("htbb.web"), System.getenv("HTBB_WEB_DIR"), "../HTBB-Web", "../web"};
        for (final String u : ung) {
            if (u != null && !u.isEmpty() && new java.io.File(u, "nuxt.config.ts").isFile()) {
                return new java.io.File(u);
            }
        }
        System.out.println("[LOI] Khong thay HTBB-Web (../HTBB-Web hoac ../web, hay -Dhtbb.web=<thu muc>).");
        System.exit(1);
        return null;
    }

    /** Ghi tep (UTF-8, LF) vao <HTBB-Web>/<tuongDoi>; tra ve duong dan da ghi. */
    private static String ghiVaoWeb(final String tuongDoi, final String noiDung) throws java.io.IOException {
        final java.io.File f = new java.io.File(thuMucWeb(), tuongDoi);
        f.getParentFile().mkdirs();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            w.write(noiDung);
        }
        return f.getPath();
    }

    /** Chuoi JSON co dau nhay kep (thoat ", \ va ky tu dieu khien; giu nguyen chu co dau). */
    private static String jsonChuoi(final String s) {
        final StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                b.append('\\').append(c);
            } else if (c == '\n') {
                b.append("\\n");
            } else if (c < 0x20) {
                b.append(String.format("\\u%04x", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.append('"').toString();
    }

    private SinhDanhMucThu() {
    }
}
