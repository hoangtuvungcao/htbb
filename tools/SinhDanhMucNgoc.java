import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh shared/data/danh-muc-ngoc.json tu chinh mang real.Map.gemTemplate.
 *
 * VI SAO PHAI SINH CHU KHONG GO TAY, VA CUNG KHONG DOC DUOC TU CSDL:
 *
 * 1. TEN NGOC KHONG CO TRONG CO SO DU LIEU. Vat pham thi co bang data_item, thuoc thi co
 *    data_potion - con 270 vien ngoc thi ghi CUNG trong src/real/MapNapTaiNguyen.java
 *    (loadGemTemplate). Trang web khong co cach nao tra ten ngoc bang mot cau SELECT.
 *
 * 2. MOT NUA TEN NAM TRONG HANG SO. Trong 270 dong, 160 dong lay ten tu hang so
 *    data.Text.* chu khong phai chuoi thang. Nen doc bang regex tu tep .java se ra
 *    "Text.BAO_HIEM_20" thay vi "Bảo hiểm 20%". Chi co Java - voi ca Text tren duong
 *    classpath - moi tra ra chuoi that.
 *
 * 3. KHONG HONG BANG MA. Chuoi tieng Viet di THANG tu hang so Java ra tep UTF-8, khong
 *    qua sed/perl/heredoc - ba cho da tung lam hong 178 dong tieng Viet trong kho nay
 *    (xem test/KiemBangMaNguon).
 *
 * Cach dung (sau khi da bien dich src/ vao build/classes):
 *   java -cp "server/build/classes;server/lib/htbb-core.jar;server/lib/mysql-connector-java-5.1.49.jar" SinhDanhMucNgoc
 *   -> ghi web/shared/data/danh-muc-ngoc.json (tim ../web hoac web trong monorepo, hoac -Dhtbb.web=<thu muc>).
 *   Web doc thang tep JSON do (server/utils/), khong chep tay nua.
 *
 * Chay lai moi khi them/doi ngoc trong MapNapTaiNguyen. test/KiemDanhMucNgoc doi chieu
 * lai lan nua nen quen chay thi bo kiem se do, khong am tham lech.
 *
 * LUU Y ve cot `name` cua game nay: no vua la TEN vua la MO TA - khong co cot mo ta rieng,
 * mo ta duoc nhet vao name va xuong dong bang ky tu xuong dong that. O day xuong dong duoc
 * doi thanh " · " vi tep sinh ra chi dung lam NHAN trong o chon, khong phai du lieu.
 */
public final class SinhDanhMucNgoc {

    private static final String TEP = "shared/data/danh-muc-ngoc.json";

    public static void main(final String[] thamSo) throws Exception {
        // Map.loadGemTemplate() doc TeamServer.language de biet co phai server Indo khong.
        // Mac dinh 0 = ban thuong, dung cai ta can. Khoi static cua Map co goi CSDL nen se
        // in NullPointerException - do la binh thuong khi chay ngoai server, y het cac bo
        // kiem trong test/.
        real.Map.loadGemTemplate();
        final real.GemTemplate[] ds = real.Map.gemTemplate;
        if (ds == null || ds.length == 0) {
            System.out.println("[LOI] Map.gemTemplate rong - khong sinh gi.");
            System.exit(1);
        }

        // Anh xa id ngoc -> nhom nguyen lieu ("NLSC3", "NLCC6"...). Hai mang nay la
        // [cap][he]: 6 cap, moi cap 5 he. Nguoi van hanh goi chung bang "NLSC 6" / "NLCC 6"
        // chu khong ai nho id 73/80/87/94/101 - do la ca ly do co bo chon nay.
        final String[] nhom = new String[300];
        ghiNhom(nhom, real.GemTemplate.ID_MATERIAL_LOW, "NLSC");
        ghiNhom(nhom, real.GemTemplate.ID_MATERIAL_HIGHT, "NLCC");

        final StringBuilder s = new StringBuilder();
        s.append("{\n");
        s.append("  \"_ghiChu\": \"TEP SINH TU DONG - DUNG SUA TAY. Danh muc ngoc cho o chon phan thuong giftcode (loai GEM). ")
         .append("ten = nhan hien thi (xuong dong trong ten goc doi thanh ' \u00b7 '); ban = co ban trong cua hang NPC khong ")
         .append("(false = chi phat duoc bang giftcode); nhom = NLSC1..6 / NLCC1..6 neu la ngoc nguyen lieu. ")
         .append("Ten ngoc KHONG co trong CSDL, mot nua la hang so data.Text.* nen phai sinh bang Java. ")
         .append("test/KiemDanhMucNgoc (repo game) doi chieu tep nay voi mang Java.\",\n");
        s.append("  \"_nguon\": \"real.Map.gemTemplate (src/real/MapNapTaiNguyen.java) - repo game HTBB\",\n");
        s.append("  \"_sinhLai\": \"(trong repo game) java -cp build/classes;lib/htbb-core.jar;lib/mysql-connector-java-5.1.49.jar SinhDanhMucNgoc\",\n");
        s.append("  \"ngoc\": {\n");

        int soDong = 0;
        int soKhongBan = 0;
        final StringBuilder dong = new StringBuilder();
        for (int i = 0; i < ds.length; i++) {
            final real.GemTemplate g = ds[i];
            if (g == null) {
                continue;
            }
            String ten = g.name == null ? "" : g.name;
            // Ten vua la ten vua la mo ta, xuong dong bang ky tu that -> gop lai mot dong.
            ten = ten.replace("\r", "").replace("\n", " \u00b7 ").trim();
            while (ten.contains("  ")) {
                ten = ten.replace("  ", " ");
            }
            if (ten.isEmpty()) {
                ten = "(khong ten)";
            }
            // Map.SELL = 1, Map.NOT_SELL = 0. Doc gia tri o HAM DUNG, chua qua
            // Map.loadShop() - ham do con doi co sell cua mot so ngoc theo su kien dang
            // bat. Nen day la trang thai NEN, dung de loc danh sach cho gon; no khong
            // phai loi hua ve gia ban trong game.
            final boolean ban = g.sell == real.Map.SELL;
            if (!ban) {
                soKhongBan++;
            }
            final String n = (i < nhom.length && nhom[i] != null) ? nhom[i] : "";
            if (dong.length() > 0) {
                dong.append(",\n");
            }
            dong.append("    \"").append(i).append("\": {\"ten\": ").append(jsonChuoi(ten))
                .append(", \"ban\": ").append(ban ? "true" : "false")
                .append(", \"nhom\": ").append(jsonChuoi(n)).append("}");
            soDong++;
        }
        s.append(dong).append("\n  }\n}\n");

        final String daGhi = ghiVaoWeb(TEP, s.toString());
        System.out.println("Da sinh " + daGhi + " voi " + soDong + " vien ngoc ("
                + soKhongBan + " vien khong ban trong cua hang).");

        // Khoi static cua Map tao thread nen JVM khong tu thoat - phai ep thoat, y het
        // test/Kiem.java.
        System.exit(0);
    }

    /**
     * Danh dau nhom nguyen lieu. Mang vao la [cap][he] - cap dem tu 0 trong Java nhung
     * nguoi van hanh goi tu 1 ("NLSC 6" la cap cuoi, tuc chi so 5), nen cong 1 khi dat ten.
     */
    private static void ghiNhom(final String[] nhom, final short[][] mang, final String tienTo) {
        if (mang == null) {
            return;
        }
        for (int cap = 0; cap < mang.length; cap++) {
            if (mang[cap] == null) {
                continue;
            }
            for (final short id : mang[cap]) {
                if (id >= 0 && id < nhom.length) {
                    nhom[id] = tienTo + (cap + 1);
                }
            }
        }
    }

    // ------------------------------------------------------------------ ghi JSON vao HTBB-Web

    /**
     * Thu muc HTBB-Web: -Dhtbb.web=..., bien HTBB_WEB_DIR, ../web (monorepo htbb
     * nhau) hoac ../web (monorepo). Phai co nuxt.config.ts. Website PHP cu (web/ cua repo
     * game) da bo - xem docs/kien-truc.md.
     */
    private static java.io.File thuMucWeb() {
        final String[] ung = {System.getProperty("htbb.web"), System.getenv("HTBB_WEB_DIR"), "../web", "web"};
        for (final String u : ung) {
            if (u != null && !u.isEmpty() && new java.io.File(u, "nuxt.config.ts").isFile()) {
                return new java.io.File(u);
            }
        }
        System.out.println("[LOI] Khong thay thu muc web (../web hoac web, hay -Dhtbb.web=<thu muc>).");
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

    private SinhDanhMucNgoc() {
    }
}
