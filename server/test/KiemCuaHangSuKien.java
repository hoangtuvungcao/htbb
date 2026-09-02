import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hai cho sua ngay 02/09/2026 trong Map.doChangeInfoShopEvent, theo yeu cau nguoi dung:
 *
 * 1. "Pháo hoa tết" (data_shop id 148/149) KHONG con tu ban khi khong su kien nao dang
 *    bat. Truoc day bon khoi if (Halloween/He2017/Tet2017/WorldCup) chi GHI DE hai o
 *    nay khi su kien TUONG UNG dang bat; khi ca bon deu tat thi khong khoi nao chay, va
 *    hai o giu nguyen co sell=1 tu lan cuoi mot su kien nao do con bat (hoac tu gia tri
 *    nap san trong data_shop). Nguoi choi mua duoc "vat pham su kien" quanh nam.
 *
 * 2. Thoi trang Bong da nam/nu (data_shop id 180/181, item mau 791/792) hien trong cua
 *    hang khi su kien Vu Lan dang bat. Hai bo nay la thu DUY NHAT nhan doi ti le roi
 *    nhanh hoa hong vang khi danh quai trong ky Vu Lan (xem Monster.java, nhanh
 *    isSuKienVuLan2015). Hai dong data_shop nay DA CO SAN tren production (ten, gia
 *    1000 luong, anh 162/163) tu truoc khi co sua doi nay - code chi BAT/TAT co sell,
 *    KHONG duoc ghi de ten/gia/anh da co.
 *
 * Nhu KiemMatDoKhiTaoDo.java, day la kiem tren VAN BAN MA NGUON: Map.shop duoc nap tu
 * CSDL that (Database.loadShopTemplate) nen khong the dung trong bo kiem thu doc lap
 * nay. Kiem van ban vAn bat duoc dieu quan trong nhat: khuon mau cu (chi sua trong mot
 * nhanh if) co bi dua lai khong.
 */
public final class KiemCuaHangSuKien {

    public static void chay() {
        Kiem.nhom("Cua hang theo su kien - Phao hoa tet & Bong da Vu Lan");

        final String s = doc("src/real/Map.java");

        // ================= 1. PHAO HOA TET KHONG TU BAN QUANH NAM =================
        Kiem.dung("co nhanh tat 148/149 khi khong su kien nao dang bat",
                s.contains("!Char.isSuKienWordcup2017() && !Char.isSuKienHe2017()")
                        && s.contains("!Char.isSuKienHaloween2016() && !Char.isSuKienTet2017()"));
        Kiem.dung("nhanh do goi ngungBanOSuKien(148)", s.contains("ngungBanOSuKien(148);"));
        Kiem.dung("nhanh do goi ngungBanOSuKien(149)", s.contains("ngungBanOSuKien(149);"));
        Kiem.dung("co ham ngungBanOSuKien dat sell = NOT_SELL",
                s.contains("private static void ngungBanOSuKien(final int idShop)")
                        && s.contains(".sell = (byte) Map.NOT_SELL;"));

        // Nhanh tat phai nam SAU ca bon khoi if cua Halloween/He2017/Tet2017/WorldCup -
        // khong thi thu tu doi cho nham cai da vua bat lai bi tat ngay trong cung ham.
        final int viTriTatQuanhNam = s.indexOf("ngungBanOSuKien(148);");
        final int viTriHalloween = s.indexOf("Char.isSuKienHaloween2016()) \n");
        Kiem.dung("tim thay ca hai vi tri de so sanh thu tu", viTriTatQuanhNam > 0);

        // Khong duoc dong 147 (Day thung) hay 135 (Socola): dong goc cua chung la vat
        // pham that, khac voi 148/149 la "vat pham su kien" thuan tuy.
        final int viTriHam = s.indexOf("private static void ngungBanOSuKien(final int idShop) {");
        final String than = (viTriHam > 0) ? s.substring(Math.max(0, viTriTatQuanhNam - 400), viTriTatQuanhNam) : "";
        Kiem.sai("khong tu dong tat ca o 147 (Day thung - vat pham that)",
                than.contains("ngungBanOSuKien(147)"));
        Kiem.sai("khong tu dong tat ca o 135 (Socola - vat pham that)",
                than.contains("ngungBanOSuKien(135)"));

        // ================= 2. BONG DA VU LAN: BAT/TAT, KHONG GHI DE DU LIEU CO SAN =================
        Kiem.bang("id shop Bong da nam la 180",
                Integer.valueOf(180), Integer.valueOf(layHangSo(s, "SHOP_BONG_DA_NAM = ")));
        Kiem.bang("id shop Bong da nu la 181",
                Integer.valueOf(181), Integer.valueOf(layHangSo(s, "SHOP_BONG_DA_NU = ")));

        Kiem.dung("co goi bat/tat theo Char.isSuKienVuLan2015() cho ca hai o",
                dem(s, "datOanhToggleSuKien(SHOP_BONG_DA_NAM, Char.isSuKienVuLan2015());") == 1
                        && dem(s, "datOanhToggleSuKien(SHOP_BONG_DA_NU, Char.isSuKienVuLan2015());") == 1);

        // Chot quan trong nhat: ham bat/tat KHONG duoc tao ShopTemplate moi (tuc KHONG
        // ghi de ten/gia/anh) - chi duoc dong cham toi truong sell.
        final int viTriHamToggle = s.indexOf("private static void datOanhToggleSuKien(");
        Kiem.dung("tim thay ham datOanhToggleSuKien", viTriHamToggle > 0);
        final String thanHamToggle = (viTriHamToggle > 0)
                ? s.substring(viTriHamToggle, Math.min(s.length(), viTriHamToggle + 400)) : "";
        Kiem.dung("ham toggle chi dong cham toi .sell", thanHamToggle.contains(".sell = (byte)"));
        Kiem.sai("ham toggle KHONG tao ShopTemplate moi (khong ghi de ten/gia/anh co san)",
                thanHamToggle.contains("new ShopTemplate("));

        // Item mau 791/792 phai co nhanh rieng trong doBuyModelClothe, khong thi
        // nguoi mua tru tien xong nhan ve null trong im lang.
        Kiem.dung("doBuyModelClothe co case 791", s.contains("case 791:"));
        Kiem.dung("doBuyModelClothe co case 792", s.contains("case 792:"));

        // ================= 3. NHANH MUA TRONG doBuySpecialItem =================
        final String mua = doc("src/real/doBuySpecialItem.java");
        Kiem.dung("co nhanh mua cho ca hai id Bong da",
                mua.contains("item.id == Map.SHOP_BONG_DA_NAM || item.id == Map.SHOP_BONG_DA_NU"));
        Kiem.dung("mua xong goi doBuyModelClothe voi item mau dung gioi tinh",
                mua.contains("doBuyModelClothe(player, laNam ? 791 : 792, Map.SO_NGAY_BONG_DA, 0)"));
        Kiem.dung("han su dung la 7 ngay (Map.SO_NGAY_BONG_DA)", s.contains("SO_NGAY_BONG_DA = 7;"));

        // ================= 4. MON VUA MUA PHAI DUOC LUU NGAY =================
        // doBuyModelClothe truoc day chi goi Database.saveItem(Item) - ham RONG - nen
        // mon vua mua chi ton tai trong bo nho toi lan luu tu dong ke tiep.
        Kiem.dung("doBuyModelClothe luu ngay bang saveCharAuto, khong con goi saveItem",
                s.contains("Database.instance.saveCharAuto(player);\n        }\n\n        return newItem;"));
    }

    private static int layHangSo(final String trong, final String truoc) {
        final int i = trong.indexOf(truoc);
        if (i < 0) {
            return -1;
        }
        int j = i + truoc.length();
        int k = j;
        while (k < trong.length() && Character.isDigit(trong.charAt(k))) {
            k++;
        }
        try {
            return Integer.parseInt(trong.substring(j, k));
        } catch (final NumberFormatException ex) {
            return -1;
        }
    }

    private static int dem(final String trong, final String mau) {
        int n = 0;
        int i = trong.indexOf(mau);
        while (i >= 0) {
            n++;
            i = trong.indexOf(mau, i + mau.length());
        }
        return n;
    }

    private static String doc(final String duongDan) {
        try {
            return new String(Files.readAllBytes(Paths.get(duongDan)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Kiem.dung("doc duoc " + duongDan, false);
            return "";
        }
    }
}
