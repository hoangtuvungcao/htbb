import com.htbb.pokemon.DoHiemSinh;
import com.htbb.pokemon.NgoaiHinh;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mau do hiem MOT nguon (de xuat 4): tools/cauhinh/do_hiem.json -> DoHiemSinh server / client, web doHiem.gen.ts,
 * sql/nang_cap.sql (khoi them_do_hiem.sql), SinhPhuKien. Kiem: ban sinh khop json, thu tu bac theo dac ta, anh xa colorName theo gia tri
 * that, tuong phan doc duoc (nen sang trang / giay da NRO, nen toi), khong con bang mau do hiem rieng o noi khac.
 */
public final class KiemDoHiem {

    private KiemDoHiem() {
    }

    public static void chay() {
        Kiem.nhom("Do hiem: mot nguon tools/cauhinh/do_hiem.json");
        final String json = doc("../tools/cauhinh/do_hiem.json");
        Kiem.dung("doc duoc do_hiem.json", json.length() > 500);
        final List<String[]> bac = new ArrayList<String[]>();
        final Matcher m = Pattern.compile("\\{\"ma\": \"(\\w+)\", \"ten\": \"([^\"]+)\", \"mau\": \"(#[0-9A-F]{6})\", \"nhat\": \"(#[0-9A-F]{6})\", \"sang\": \"(#[0-9A-F]{6})\"")
                .matcher(json);
        while (m.find()) {
            bac.add(new String[]{m.group(1), m.group(2), m.group(3), m.group(4), m.group(5)});
        }
        Kiem.bang("6 bac (Thuong Hiem Quy Su thi Huyen thoai Trang tri)", "Thường,Hiếm,Quý,Sử thi,Huyền thoại,Trang trí",
                String.join(",", DoHiemSinh.TEN));
        Kiem.bang("json cung 6 bac", 6, bac.size());
        int lech = 0;
        for (int i = 0; i < bac.size() && i < DoHiemSinh.SO_BAC; i++) {
            lech += bac.get(i)[1].equals(DoHiemSinh.TEN[i]) && Integer.parseInt(bac.get(i)[2].substring(1), 16) == DoHiemSinh.MAU[i] ? 0 : 1;
        }
        Kiem.bang("DoHiemSinh (server) khop json (chay python3 tools/cauhinh/sinh_do_hiem.py)", 0, lech);
        final String cl = doc("../client_jar/src/game/pokemon/DoHiemSinh.java");
        final String ts = doc("../web/shared/utils/doHiem.gen.ts");
        final String sq = DayDu.phanNangCap("them_do_hiem.sql"); // khoi sinh trong sql/nang_cap.sql phan 11
        int lechCl = 0, lechTs = 0, lechSql = 0;
        for (final String[] b : bac) {
            lechCl += cl.contains("0x" + b[2].substring(1)) && cl.contains("0x" + b[3].substring(1)) && cl.contains("0x" + b[4].substring(1)) ? 0 : 1;
            lechTs += ts.contains("mau: \"" + b[2] + "\", nhat: \"" + b[3] + "\", sang: \"" + b[4] + "\"") ? 0 : 1;
            lechSql += sq.contains("'" + b[0] + "','" + b[1] + "','" + b[2] + "','" + b[3] + "','" + b[4] + "'") ? 0 : 1;
        }
        Kiem.bang("client DoHiemSinh khop json", 0, lechCl);
        Kiem.bang("web doHiem.gen.ts khop json", 0, lechTs);
        Kiem.bang("nang_cap.sql khoi them_do_hiem.sql khop json", 0, lechSql);
        Kiem.dung("sinh_do_hiem.py ghi khoi them_do_hiem.sql cua nang_cap.sql (mot tep SQL, --kiem)", doc("../tools/cauhinh/sinh_do_hiem.py").contains("ghep_nang_cap(")
                && !new java.io.File("../sql/them_do_hiem.sql").exists());
        Kiem.dung("SinhPhuKien.java co khoi sinh tu do_hiem.json", doc("../tools/pokemon/SinhPhuKien.java").contains("SINH TU tools/cauhinh/do_hiem.json: BAT DAU"));
        Kiem.sai("khoi them_do_hiem.sql khong DROP / utf8mb4", sq.contains("DROP TABLE") || sq.contains("utf8mb4"));
        Kiem.dung("them_do_hiem.sql co trong sql/README.md", doc("../sql/README.md").contains("them_do_hiem.sql"));

        Kiem.nhom("Do hiem: anh xa theo gia tri that (thiet_ke_can_bang muc 7)");
        Kiem.bang("colorName 0 Trang -> Thuong", DoHiemSinh.THUONG, DoHiemSinh.bacVatPham(0));
        Kiem.bang("colorName 3 Xanh 110% -> Hiem", DoHiemSinh.HIEM, DoHiemSinh.bacVatPham(3));
        Kiem.bang("colorName 2 Do 120% -> Quy", DoHiemSinh.QUY, DoHiemSinh.bacVatPham(2));
        Kiem.bang("colorName 1 Hoan my 130% -> Su thi", DoHiemSinh.SU_THI, DoHiemSinh.bacVatPham(1));
        Kiem.bang("colorName 4 tim -> Su thi", DoHiemSinh.SU_THI, DoHiemSinh.bacVatPham(4));
        Kiem.bang("colorName 5 vang -> Trang tri", DoHiemSinh.TRANG_TRI, DoHiemSinh.bacVatPham(5));
        Kiem.bang("colorName 6 -> Huyen thoai", DoHiemSinh.HUYEN_THOAI, DoHiemSinh.bacVatPham(6));
        Kiem.bang("colorName 7 -> Hiem", DoHiemSinh.HIEM, DoHiemSinh.bacVatPham(7));
        Kiem.bang("colorName la -> Thuong", DoHiemSinh.THUONG, DoHiemSinh.bacVatPham(42));
        Kiem.bang("danh hieu 0..3 -> Thuong, Hiem, Su thi, Huyen thoai", "0,1,3,4",
                DoHiemSinh.bacDanhHieu(0) + "," + DoHiemSinh.bacDanhHieu(1) + "," + DoHiemSinh.bacDanhHieu(2) + "," + DoHiemSinh.bacDanhHieu(3));
        Kiem.dung("chi Huyen thoai vien dong", DoHiemSinh.VIEN_DONG[DoHiemSinh.HUYEN_THOAI] && !DoHiemSinh.VIEN_DONG[DoHiemSinh.SU_THI]);
        Kiem.bang("phu kien (-102) = bac chung", DoHiemSinh.QUY, NgoaiHinh.hangHiem(2));
        Kiem.bang("ten bac phu kien = DoHiemSinh", DoHiemSinh.TEN, NgoaiHinh.TEN_HIEM);
        Kiem.bang("11 bac trang bi", 11, DoHiemSinh.TEN_BAC_TRANG_BI.length);
        Kiem.bang("bac trang bi 'Huyen thoai' cung mau goc phu kien bac Huyen thoai", Integer.valueOf(0xF08A28),
                Integer.valueOf(DoHiemSinh.MAU_BAC_TRANG_BI[8]));

        Kiem.nhom("Do hiem: doc duoc tren moi nen (WCAG)");
        int kem = 0;
        final StringBuilder ds = new StringBuilder();
        for (final String[] b : bac) {
            final double trang = tuongPhan(b[2], "#FFFFFF"), giay = tuongPhan(b[2], "#F2E0B4"), nhat = tuongPhan(b[2], b[3]);
            final double toi = tuongPhan(b[4], "#101828"), nau = tuongPhan(b[4], "#1B0E06");
            if (trang < 4.5 || giay < 3.0 || nhat < 3.0 || toi < 4.5 || nau < 4.5) {
                kem++;
                ds.append(b[0]).append(' ');
            }
        }
        Kiem.bang("mau >= 4,5 tren trang, >= 3 tren giay da / nen nhat; sang >= 4,5 tren nen toi " + ds, 0, kem);

        Kiem.nhom("Do hiem: khong con bang mau rieng o noi khac");
        final String kieuO = doc("../client_jar/src/game/pokemon/KieuO.java");
        Kiem.dung("KieuO.mauHiem doc DoHiemSinh", kieuO.contains("DoHiemSinh.bacVatPham(c)"));
        Kiem.sai("KieuO khong con 0x48D058 / 0x3FA0F0 (bang cu lech server)", kieuO.contains("0x48D058") || kieuO.contains("0x3FA0F0"));
        final String chuDe = doc("../client_jar/src/game/pokemon/ChuDe.java");
        Kiem.dung("ChuDe.mauDanhHieu / nhanDanhHieu qua bac chung", chuDe.contains("mauHiem(DoHiemSinh.bacDanhHieu(doHiem))")
                && chuDe.contains("sangHiem(DoHiemSinh.bacDanhHieu(doHiem))"));
        Kiem.sai("ChuDe khong con mau danh hieu viet tay (0x8CC8FF / 0xCDB8FF)", chuDe.contains("0x8CC8FF") || chuDe.contains("0xCDB8FF"));
        final String pk = doc("../client_jar/src/game/pokemon/PhuKien.java");
        Kiem.dung("PhuKien dung bang mau DoHiemSinh", pk.contains("hiem = DoHiemSinh.PHU_KIEN") && pk.contains("mauBac = DoHiemSinh.MAU_BAC_TRANG_BI"));
        Kiem.dung("danh hieu SQL cung mau (them_danh_hieu.sql)", DayDu.phanNangCap("them_danh_hieu.sql").contains("(1,'Hiếm','#23803A',1)"));
        Kiem.dung("web xep hang to chip danh hieu theo bac chung", doc("../web/app/pages/ranking.vue").contains("bacDanhHieu(d.danhHieu.doHiem)"));
        Kiem.sai("Item.getNameItemPetEat khong con ten mau cu", doc("src/real/Item.java").contains("{\"\", \"Hoàn mỹ\", \"Đỏ\", \"Xanh\"}"));
        Kiem.sai("Map: ten bac cu 'Hoan my' trong do tieu", doc("src/real/Map.java").contains("new String[]{\"Trắng\", \"Xanh\", \"Đỏ\", \"Hoàn mỹ\"}"));
    }

    static double sang(final String h) {
        final int v = Integer.parseInt(h.substring(1), 16);
        final double[] c = {((v >> 16) & 255) / 255.0, ((v >> 8) & 255) / 255.0, (v & 255) / 255.0};
        for (int i = 0; i < 3; i++) {
            c[i] = c[i] <= 0.03928 ? c[i] / 12.92 : Math.pow((c[i] + 0.055) / 1.055, 2.4);
        }
        return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
    }

    static double tuongPhan(final String a, final String b) {
        final double x = sang(a), y = sang(b);
        return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
    }

    static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }
}
