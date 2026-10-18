import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh shared/data/bang-cap-do.json tu chinh mang real.LevelDetail.expMain.
 *
 * VI SAO SINH CHU KHONG GO TAY: mang co 91 phan tu, nhieu so dai toi 14 chu so.
 * Go tay mot mang nhu the la tu ruoc loi go nham mot chu so - loi kieu do KHONG
 * AI NHIN RA BANG MAT, va se lam thanh diem kinh nghiem hien sai tren thanh tien
 * do (khong lam sai CAP DO, vi cap do dat thang bang so nguoi van hanh nhap, chi
 * lam LECH so % hien trong thanh kinh nghiem cua nhan vat).
 *
 * MANG NAY CHI DUNG DE GIU THANH KINH NGHIEM NHAT QUAN. Khi trang quan tri dat
 * lai cap do mot nhan vat (tob_char.lastLv), no dat luon tob_char.xp = gia tri
 * dung DAU cua cap do do (expMain[cap - 1]) - giong het trang thai "vua len cap,
 * chua co % nao vao cap tiep theo" cua mot nguoi choi len cap tu nhien. Moi chi
 * so KHAC cua nhan vat (suc manh, nhanh nhen, HP, diem ky nang...) deu duoc may
 * chu tu TINH LAI tu lastLv moi lan chon nhan vat
 * (src/real/cmd/SelectCharHandler.java:206-215, roi goi p.calculateAttrib()) -
 * nen KHONG can va KHONG duoc dong vao cot nao khac.
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "server/build/classes;server/lib/htbb-core.jar;server/lib/mysql-connector-java-5.1.49.jar" SinhBangCapDo
 *   -> ghi HTBB-Web/shared/data/bang-cap-do.json (tim ../HTBB-Web, ../web, hoac -Dhtbb.web=<thu muc>).
 *   Web doc thang tep JSON do (server/utils/), khong chep tay nua.
 *
 * Chay lai neu bang expMain trong LevelDetail.java doi (rat hiem, thuong chi khi
 * mo rong tran cap). test/KiemBangCapDo.java doi chieu tep sinh voi mang Java tung
 * phan tu mot, nen quen chay lai la bo kiem do.
 */
public final class SinhBangCapDo {

    private static final String TEP = "shared/data/bang-cap-do.json";

    public static void main(final String[] thamSo) throws Exception {
        final long[] expMain = real.LevelDetail.expMain;
        final long[] expRequire = real.LevelDetail.expRequire;
        if (expMain == null || expMain.length == 0) {
            System.out.println("[LOI] LevelDetail.expMain rong - khong sinh gi.");
            System.exit(1);
        }
        if (expRequire == null || expRequire.length <= expMain.length) {
            // expRequire duoc doc bang index = CAP DO THAT (khong phai capDo-1 nhu
            // expMain), nen dai hon expMain dung 1 phan tu (index 0 khong dung toi,
            // phan tu cuoi la 0L cho cap toi da - khong co cap ke tiep de tinh %).
            System.out.println("[LOI] LevelDetail.expRequire rong hoac ngan hon du so voi expMain - khong sinh gi.");
            System.exit(1);
        }

        // Tran cap CHO PHAT DUOC BANG TRANG QUAN TRI: lay theo du lieu THAT dang
        // chay (MAX(lastLv) tren CSDL that = 90 luc doi chieu), KHONG lay het do
        // dai mang (91). Ham getXpFromLevel() ben Java co mot loi lech-mot o bien
        // tren cung (dieu kien "level < expMain.length" sai cho dung level bang
        // do dai mang) - tranh dung toi bien do bang cach dat tran thap hon do
        // dai mang mot don vi.
        final int tranCap = expMain.length - 1;
        // Moi so phai doc dung trong JavaScript (number) - lon hon 2^53 thi web lam tron sai.
        for (final long[] mang : new long[][]{expMain, expRequire}) {
            for (final long v : mang) {
                if (v < 0 || v > 9007199254740991L) {
                    System.out.println("[LOI] gia tri " + v + " vuot Number.MAX_SAFE_INTEGER - web khong doc dung duoc.");
                    System.exit(1);
                }
            }
        }

        final StringBuilder s = new StringBuilder();
        s.append("{\n");
        s.append("  \"_ghiChu\": \"TEP SINH TU DONG - DUNG SUA TAY. Bang quy doi cap do -> diem kinh nghiem cho khu quan tri. ")
         .append("expMain[cap-1] = exp LUC VUA LEN cap do (0%); expRequire[cap] = exp de di het cap do (chi so = CHINH cap do). ")
         .append("Web KHONG tu tinh exp de ghi tob_char (lenh di qua web_lenh_nhan_vat, server tinh bang real.LevelDetail); ")
         .append("moi chi so khac do may chu tu tinh lai tu cap luc chon nhan vat (src/real/cmd/SelectCharHandler.java, ")
         .append("p.calculateAttrib()). test/KiemBangCapDo (repo game) doi chieu tung so voi mang Java.\",\n");
        s.append("  \"_nguon\": \"real.LevelDetail.expMain / expRequire (src/real/LevelDetail.java) - repo game HTBB\",\n");
        s.append("  \"_sinhLai\": \"(trong repo game) java -cp build/classes;lib/htbb-core.jar;lib/mysql-connector-java-5.1.49.jar SinhBangCapDo\",\n");
        s.append("  \"capDoToiDa\": ").append(tranCap).append(",\n");
        s.append("  \"expMain\": [\n");
        noiMang(s, expMain);
        s.append("  ],\n");
        s.append("  \"expRequire\": [\n");
        noiMang(s, expRequire);
        s.append("  ]\n}\n");

        final String daGhi = ghiVaoWeb(TEP, s.toString());
        System.out.println("Da sinh " + daGhi + " voi " + expMain.length
                + " muc (tran cap cho phat = " + tranCap + ").");
        System.exit(0);
    }

    /** Moi phan tu mot dong (de diff de doc, va de test/KiemBangCapDo dem tung dong). */
    private static void noiMang(final StringBuilder s, final long[] mang) {
        for (int i = 0; i < mang.length; i++) {
            s.append("    ").append(mang[i]).append(i + 1 < mang.length ? ",\n" : "\n");
        }
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

    private SinhBangCapDo() {
    }
}
