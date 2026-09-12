import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Sua loi: giftcode loai ITEM (vd "Trung thu" - data_item type 26) chi tao
 * dung 1 vat pham du ghi so luong bao nhieu, nhung dong thong bao van in
 * nguyen so da go.
 *
 * Char.createItemSoLuong() KHONG the goi truc tiep o day: can mot Char that
 * (iItems, getIDItem(), isFullInventory(), sendMessage()...) ma khong test
 * nao trong bo nay tung dung toi (Char qua nang de dung rieng). Nen quet van
 * ban nguon de xac nhan cau truc sua dung, giong cach cac Kiem* khac lam voi
 * RealController/Animal.doHoaHinh.
 */
public final class KiemSoLuongItemGiftcode {

    private static final String TEP = "src/real/Char.java";

    private KiemSoLuongItemGiftcode() {
    }

    public static void chay() {
        Kiem.nhom("Giftcode ITEM:id:soLuong - phai tao dung so luong, khong con luon la 1");

        final String noiDung = doc(TEP);
        Kiem.dung("doc duoc " + TEP, !noiDung.isEmpty());
        if (noiDung.isEmpty()) {
            return;
        }

        final int dauHam = noiDung.indexOf("public static Item createItemSoLuong(");
        final int cuoiHam = noiDung.indexOf("public static Item createItemCaiTrang(");
        Kiem.dung("tim thay than ham createItemSoLuong()", dauHam >= 0 && cuoiHam > dauHam);
        if (dauHam < 0 || cuoiHam <= dauHam) {
            return;
        }
        final String than = noiDung.substring(dauHam, cuoiHam);

        // 1) Nhanh manh suu tap (isManhSuuTap()) PHAI giu nguyen - da dung tu
        // truoc, khong lien quan loi nay.
        Kiem.dung("nhanh manh suu tap van cong don qua otherAtt[65]", than.contains("otherAtt[65] += soLuong;"));
        Kiem.dung("nhanh manh suu tap van co tran 30000", than.contains("otherAtt[65] = 30000;"));

        // 2) Nhanh con lai (else) PHAI tao vong lap tao THEM item, khong con
        // chi mot dong "p.iItems.add(newItem);" don doc nhu truoc.
        final int viTriElse = than.indexOf("} else {");
        Kiem.dung("tim thay nhanh else (vat pham thuong)", viTriElse >= 0);
        final String nhanhElse = viTriElse >= 0 ? than.substring(viTriElse) : "";
        Kiem.dung("co vong lap tao them item (for i = 1; i < soLuong)",
                Pattern.compile("for\\s*\\(int i = 1; i < soLuong; \\+\\+i\\)|for\\s*\\(int i = 1; i < soLuong; i\\+\\+\\)").matcher(nhanhElse).find());
        Kiem.dung("moi item them co id rieng qua p.getIDItem() (khong dung chung id voi newItem goc)",
                countSoLan(than, "p.getIDItem()") >= 2);
        Kiem.dung("co kiem tra day hanh trang giua chung khi tao nhieu item (isFullInventory)",
                nhanhElse.contains("p.isFullInventory()"));
        Kiem.dung("dem so item THAT SU tao duoc (daTao), khong gia dinh luon dung soLuong",
                nhanhElse.contains("int daTao = 1;") && nhanhElse.contains("daTao++;"));
        Kiem.dung("gan lai soLuong = daTao truoc khi bao 'Chuc mung' - thong bao khop dung so THAT tao ra",
                nhanhElse.contains("soLuong = daTao;"));

        // 2b) Skin Kim Cang Long (911) qua duong ITEM: phai duoc roll chi so nhu duong DB:
        // (GiftCode.AddItem). Truoc 12/09/2026 bo chon thuong tren web sinh "ITEM:911:1" cho
        // nhom skin -> mon ve tay 0 chi so. Ca ban goc lan tung ban sao trong vong lap deu
        // phai goi (moi ban roll rieng), tuc >= 2 lan trong than ham.
        Kiem.dung("createItemSoLuong goi createAttributeSkinKimCangLong() cho ca ban goc va ban sao (>= 2 lan)",
                countSoLan(than, ".createAttributeSkinKimCangLong();") >= 2);
        Kiem.dung("ban sao trong vong lap cung roll (themItem.createAttributeSkinKimCangLong)",
                nhanhElse.contains("themItem.createAttributeSkinKimCangLong();"));

        // 3) Thong bao thanh cong phai dat SAU khi soLuong da duoc gan lai
        // dung (nam ngoai if/else, sau ca hai nhanh).
        final int viTriGanLaiSoLuong = than.indexOf("soLuong = daTao;");
        final int viTriThongBao = than.indexOf("\"Chúc mừng. Bạn đã nhận được \" + soLuong");
        Kiem.dung("dong bao 'Chuc mung' nam SAU dong gan lai soLuong = daTao",
                viTriGanLaiSoLuong >= 0 && viTriThongBao > viTriGanLaiSoLuong);
    }

    private static int countSoLan(final String s, final String tim) {
        int dem = 0;
        int i = 0;
        while ((i = s.indexOf(tim, i)) != -1) {
            dem++;
            i += tim.length();
        }
        return dem;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
