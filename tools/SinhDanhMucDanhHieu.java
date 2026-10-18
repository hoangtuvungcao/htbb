import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh shared/data/danh-muc-danh-hieu.json tu chinh ham data.DanhHieu.getName().
 *
 * VI SAO PHAI SINH CHU KHONG DOC BANG REGEX. Ten danh hieu nam trong mot khoi
 * switch-case cua ham Java, KHONG co trong CSDL nao. Doc bang regex tren tep
 * .java se de bat nham cac nhanh case-fallthrough (8780..8784 roi thang xuong
 * "default: return \"\";" - nam GIUA hai nhanh co ten that) va se khong chay
 * dung ham NHU MAY CHU CHAY - chi co goi that ham Java moi chac chan dung.
 *
 * HAI TAP ID KHAC NHAU, PHAI GHI RO TRONG BO CHON:
 *
 *   ID_NGAU_NHIEN (34 id) - khop CHINH XAC mang int[] availableTitles ghi cung
 *   trong DatabaseGiftCode.java (nhanh xu ly "DH", id=0 -> chon ngau nhien MOT
 *   trong so nay). Day la tap CO THE ra duoc bang DH:0:...
 *
 *   Cac id CON LAI (785, 8983..8996) co ten that qua getName() nhung KHONG nam
 *   trong availableTitles - "ngau nhien" khong bao gio ra duoc chung, PHAI ghi
 *   id truc tiep (DH:<id>:...). Chon nham thanh "ngau nhien" cho mot danh hieu
 *   trong nhom nay la bo chon noi doi - trong nhu chon duoc ma khong bao gio ra.
 *
 * Id 8780..8784 (rong) va 8999 ("TEST" - danh hieu thu nghiem, khong phai qua
 * that) deu BI LOAI, khong dua vao danh muc.
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "server/build/classes;server/lib/htbb-core.jar;server/lib/mysql-connector-java-5.1.49.jar" SinhDanhMucDanhHieu
 *   -> ghi HTBB-Web/shared/data/danh-muc-danh-hieu.json (tim ../HTBB-Web, ../web, hoac -Dhtbb.web=<thu muc>).
 *   Web doc thang tep JSON do (server/utils/), khong chep tay nua.
 *
 * Chay lai moi khi them/doi danh hieu trong DanhHieu.java. test/KiemDanhMucDanhHieu
 * doi chieu lai lan nua - CA hai tap id lan noi dung ten - nen quen chay thi bo
 * kiem se do, khong am tham lech.
 */
public final class SinhDanhMucDanhHieu {

    private static final String TEP = "shared/data/danh-muc-danh-hieu.json";

    /**
     * CHEP Y HET mang int[] availableTitles trong nhanh "DH" cua
     * src/data/DatabaseGiftCode.java. Day la mot ban sao CO CHU DICH: ghi hai noi
     * de test/KiemDanhMucDanhHieu.java doi chieu duoc tung so mot voi ban that
     * trong DatabaseGiftCode.java, chu khong phai vi khong the doc tu do truc
     * tiep - noi dung nhanh do khong doc duoc tu ben ngoai bang phan xa Java.
     */
    private static final int[] ID_NGAU_NHIEN = {
        8767, 8768, 8769, 8770, 8771, 8772, 8773, 8774, 8775, 8776, 8777,
        8778, 8779, 8785, 8786, 8787, 8788, 8900, 8901, 8902, 8903, 8904,
        8905, 8906, 8907, 8908, 8909, 8910, 8911, 8924, 8925, 8935, 8956, 8957
    };

    /**
     * Sieu tap id de do qua data.DanhHieu.getName(). Gom ca ID_NGAU_NHIEN lan
     * moi id khac tung thay xuat hien trong ma nguon (785 that ra la 8785, da
     * co trong ID_NGAU_NHIEN; day la dai 8983..8996 - danh hieu MOI hon, chua
     * duoc them vao mang ngau nhien). Rong / "TEST" bi loc o vong lap ben duoi
     * bang chinh ket qua getName(), khong can liet ke rieng o day.
     */
    private static int[] taoSieuTapId() {
        final java.util.TreeSet<Integer> tap = new java.util.TreeSet<Integer>();
        for (final int id : ID_NGAU_NHIEN) {
            tap.add(id);
        }
        for (int id = 8983; id <= 8999; id++) {
            tap.add(id);
        }
        final int[] ra = new int[tap.size()];
        int i = 0;
        for (final int id : tap) {
            ra[i++] = id;
        }
        return ra;
    }

    public static void main(final String[] thamSo) throws Exception {
        final java.util.Set<Integer> ngauNhien = new java.util.HashSet<Integer>();
        for (final int id : ID_NGAU_NHIEN) {
            ngauNhien.add(id);
        }

        final StringBuilder s = new StringBuilder();
        s.append("{\n");
        s.append("  \"_ghiChu\": \"TEP SINH TU DONG - DUNG SUA TAY. Danh muc danh hieu cho o chon phan thuong giftcode (loai DH). ")
         .append("ngauNhien = true CHI cho dung ").append(ID_NGAU_NHIEN.length)
         .append(" id trung mang availableTitles ghi cung trong src/data/DatabaseGiftCode.java (nhanh DH, id=0 -> chon ngau nhien ")
         .append("MOT trong so nay); ngauNhien=false thi PHAI ghi id that (DH:<id>:...). ")
         .append("test/KiemDanhMucDanhHieu (repo game) doi chieu tep nay voi getName() va mang availableTitles that.\",\n");
        s.append("  \"_nguon\": \"data.DanhHieu.getName() (src/data/DanhHieu.java) - repo game HTBB\",\n");
        s.append("  \"_sinhLai\": \"(trong repo game) java -cp build/classes;lib/htbb-core.jar;lib/mysql-connector-java-5.1.49.jar SinhDanhMucDanhHieu\",\n");
        s.append("  \"danhHieu\": {\n");

        int soDong = 0;
        int soNgauNhien = 0;
        final StringBuilder dong = new StringBuilder();
        for (final int id : taoSieuTapId()) {
            final String ten = data.DanhHieu.getName(id);
            // getName() tra "" cho id rong (8780..8784) va cho id khong biet - loai
            // het, khong dua vao danh muc. "TEST" (8999) cung loai rieng: la danh
            // hieu thu nghiem, khong phai qua that.
            if (ten == null || ten.isEmpty() || "TEST".equals(ten)) {
                continue;
            }
            final boolean laNgauNhien = ngauNhien.contains(Integer.valueOf(id));
            if (dong.length() > 0) {
                dong.append(",\n");
            }
            dong.append("    \"").append(id).append("\": {\"ten\": ").append(jsonChuoi(ten))
                .append(", \"ngauNhien\": ").append(laNgauNhien ? "true" : "false").append("}");
            soDong++;
            if (laNgauNhien) {
                soNgauNhien++;
            }
        }
        s.append(dong).append("\n  }\n}\n");

        final String daGhi = ghiVaoWeb(TEP, s.toString());
        System.out.println("Da sinh " + daGhi + " voi " + soDong + " danh hieu ("
                + soNgauNhien + " trong so ngau nhien).");
        if (soNgauNhien != ID_NGAU_NHIEN.length) {
            System.out.println("[CANH BAO] ID_NGAU_NHIEN co " + ID_NGAU_NHIEN.length
                    + " id nhung chi " + soNgauNhien + " id ra duoc ten - kiem lai getName().");
        }
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

    private SinhDanhMucDanhHieu() {
    }
}
