import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Sinh web/admin/inc/danh_muc_ngoc.php tu chinh mang real.Map.gemTemplate.
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
 *   java -cp "build/classes;lib/NQSH_5h.jar;lib/mysql-connector-java-5.1.49.jar" SinhDanhMucNgoc
 *
 * Chay lai moi khi them/doi ngoc trong MapNapTaiNguyen. test/KiemDanhMucNgoc doi chieu
 * lai lan nua nen quen chay thi bo kiem se do, khong am tham lech.
 *
 * LUU Y ve cot `name` cua game nay: no vua la TEN vua la MO TA - khong co cot mo ta rieng,
 * mo ta duoc nhet vao name va xuong dong bang ky tu xuong dong that. O day xuong dong duoc
 * doi thanh " · " vi tep sinh ra chi dung lam NHAN trong o chon, khong phai du lieu.
 */
public final class SinhDanhMucNgoc {

    private static final String TEP = "web/admin/inc/danh_muc_ngoc.php";

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

        final StringBuilder s = new StringBuilder();
        s.append("<?php\n");
        s.append("/**\n");
        s.append(" * DANH MUC NGOC cho o chon phan thuong giftcode (loai GEM).\n");
        s.append(" *\n");
        s.append(" * TEP NAY DUOC SINH RA, DUNG SUA TAY.\n");
        s.append(" *   Nguon   : mang real.Map.gemTemplate (src/real/MapNapTaiNguyen.java)\n");
        s.append(" *   Sinh lai: java -cp \"build/classes;lib/NQSH_5h.jar;");
        s.append("lib/mysql-connector-java-5.1.49.jar\" SinhDanhMucNgoc\n");
        s.append(" *\n");
        s.append(" * Vi sao phai sinh: ten ngoc KHONG co trong CSDL (khac vat pham va thuoc),\n");
        s.append(" * va mot nua so ten nam trong hang so data.Text.* nen doc bang regex tu tep\n");
        s.append(" * .java se ra ten hang so thay vi chuoi that. Xem tools/SinhDanhMucNgoc.java.\n");
        s.append(" *\n");
        s.append(" * Chuoi o day la NHAN de hien trong o chon: xuong dong trong ten goc da duoc\n");
        s.append(" * doi thanh \" · \" cho gon mot dong.\n");
        s.append(" *\n");
        s.append(" * test/KiemDanhMucNgoc.java doi chieu tep nay voi mang Java, nen tep lech la\n");
        s.append(" * bo kiem do chu khong am tham sai.\n");
        s.append(" */\n\n");
        // Anh xa id ngoc -> nhom nguyen lieu ("NLSC3", "NLCC6"...). Hai mang nay la
        // [cap][he]: 6 cap, moi cap 5 he. Nguoi van hanh goi chung bang "NLSC 6" / "NLCC 6"
        // chu khong ai nho id 73/80/87/94/101 - do la ca ly do co bo chon nay.
        final String[] nhom = new String[300];
        ghiNhom(nhom, real.GemTemplate.ID_MATERIAL_LOW, "NLSC");
        ghiNhom(nhom, real.GemTemplate.ID_MATERIAL_HIGHT, "NLCC");

        s.append("/**\n");
        s.append(" * @return array<int,array{ten:string,ban:bool,nhom:string}>\n");
        s.append(" *   ten  = nhan hien thi\n");
        s.append(" *   ban  = co ban trong cua hang NPC khong (false = chi phat duoc bang giftcode)\n");
        s.append(" *   nhom = 'NLSC1'..'NLSC6' / 'NLCC1'..'NLCC6' neu la ngoc nguyen lieu, con lai la ''\n");
        s.append(" */\n");
        s.append("function danhMucNgoc(): array\n");
        s.append("{\n");
        s.append("    return [\n");

        int soDong = 0;
        int soKhongBan = 0;
        for (int i = 0; i < ds.length; i++) {
            final real.GemTemplate g = ds[i];
            if (g == null) {
                continue;
            }
            String ten = g.name == null ? "" : g.name;
            // Ten vua la ten vua la mo ta, xuong dong bang ky tu that -> gop lai mot dong.
            ten = ten.replace("\r", "").replace("\n", " · ").trim();
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
            s.append("        ").append(i).append(" => ['ten' => '").append(thoatChuoiPhp(ten))
             .append("', 'ban' => ").append(ban ? "true" : "false")
             .append(", 'nhom' => '").append(n).append("'],\n");
            soDong++;
        }

        s.append("    ];\n");
        s.append("}\n");

        try (Writer w = new OutputStreamWriter(new FileOutputStream(TEP), StandardCharsets.UTF_8)) {
            w.write(s.toString());
        }
        System.out.println("Da sinh " + TEP + " voi " + soDong + " vien ngoc ("
                + soKhongBan + " vien khong ban trong cua hang).");

        // Khoi static cua Map tao thread nen JVM khong tu thoat - phai ep thoat, y het
        // test/Kiem.java.
        System.exit(0);
    }

    /**
     * Thoat chuoi cho PHP nhay DON. Chi hai ky tu can thoat: dau nhay don va dau gach
     * cheo nguoc. Dung nhay don chu khong nhay kep de khong phai lo dau $ trong ten
     * (PHP noi bien trong nhay kep).
     */
    private static String thoatChuoiPhp(final String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
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

    private SinhDanhMucNgoc() {
    }
}
