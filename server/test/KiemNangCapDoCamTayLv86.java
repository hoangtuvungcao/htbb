import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot lai HAI fix rieng biet cho
 * cung MOT trieu chung nguoi choi bao "nang cap vu khi tu lv81 len lv86 khong duoc" (anh chup
 * man hinh vu khi "Anh Vu Cung" - id 464, level 81, item CAO NHAT hang cung).
 *
 * FIX 1 - "Che do" (tao vu khi MOI tu dau qua NPC "Tho Ren Than Bi", npcType -8): he thong
 * nay bi COPY thanh hai ban gan giong het nhau (RealController.doCreateItem() da duoc cap
 * nhat them cap 86 tu truoc; MapCheTao.doCreateItem()/Map.getTemplateItemCreate(), goi qua
 * MapDoMenu.java, la ban CU quen cap nhat - menu chi co 13 muc, bang tra cung chi co 13 phan
 * tu/hang). Da dong bo ca hai. NHUNG day KHONG PHAI tinh nang nguoi choi dang hoi - "che do"
 * la tao mon MOI ngau nhien, khong giu duoc plus/phẩm/he cua mon dang mac.
 *
 * FIX 2 - "Nâng cấp" THAT (nut chuot phai tren item co san, colorName=='Hoan my': "Thang cap
 * thuong"/"Thang cap giu thuoc tinh", client goi doRequestActionCheDo(10/11,...) -> server
 * MapCheTao.doUserCreateItem -> Map.doUserUpLevelItem/doUserUpLevelItemSpecial). Day moi la
 * tinh nang nguoi dung hoi ("nâng cấp vũ khí từ lv81 lên lv86"), va no co HAI loi khac:
 * a) Ca hai ham chan cung "if (item.level >= 81) { ...\"khong the nang cap them\"... }" - tuc
 * CHUA BAO GIO cho nang qua 81 du item level-86 (893-897) da co san trong DB.
 * b) Item.getIdTemplateUpLevel() mac dinh tra ve "id + 1" khi cot idUpLevel trong data_item
 * chua dat (== -1) - cong thuc nay DUNG cho cac bac thap (vd kiem 400->401->...->412, la
 * MOT KHOI id lien tuc 400-412 cho 13 bac 21..81), nhung 412+1=413 lai la DAO (khoi id khac,
 * "Gia cat dao"), khong phai kiem bac 86 (893) - 5 khoi vu khi (kiem/dao/but/bua/cung) deu
 * xep LIEN TIEP nhau (400-412,413-425,426-438,439-451,452-464) roi moi toi khoi rieng 893-897,
 * nen "+1" luon sai dung tai ranh gioi 81->86 cho CA NAM loai. Neu chan (a) khong co, nang
 * cap se am tham tao ra vat pham SAI (vd "Cuoc thuong" id465 thay vi "Phuong Thien Cung" 897).
 *
 * Fix: sua ca hai cho chan tu ">= 81" thanh ">= 86", VA dat idUpLevel=893/894/895/896/897 cho
 * 412/425/438/451/464 trong du_lieu/sql_nguon/gamedb7_clean.sql (05/09/2026).
 */
public final class KiemNangCapDoCamTayLv86 {

    private static final String F_MAP = "src/real/Map.java";
    private static final String F_CHETAO = "src/real/MapCheTao.java";
    private static final String F_REALCTRL = "src/real/RealController.java";
    private static final String F_SQL = "../sql/htbb_day_du.sql";

    public static void chay() {
        Kiem.nhom("Nang cap vu khi lv81 -> lv86 (NPC Tho Ren Than Bi, npcType -8)");

        final String map = doc(F_MAP);
        final String cheTao = doc(F_CHETAO);
        final String realCtrl = doc(F_REALCTRL);
        final String sql = doc(F_SQL);
        Kiem.dung("doc duoc " + F_MAP, !map.isEmpty());
        Kiem.dung("doc duoc " + F_CHETAO, !cheTao.isEmpty());
        Kiem.dung("doc duoc " + F_REALCTRL, !realCtrl.isEmpty());
        Kiem.dung("doc duoc " + F_SQL, !sql.isEmpty());
        if (map.isEmpty() || cheTao.isEmpty() || realCtrl.isEmpty() || sql.isEmpty()) {
            return;
        }

        // ---- 1. Map.getTemplateItemCreate(): 5 hang vu khi (kiem/dao/but/bua/cung) phai
        // co du id level-86 o cuoi hang, dung nhu ban RealController da co san. ----
        final String[][] hangVuKhi = {
            {"kiếm", "411, 412, 893}"},
            {"đao", "424, 425, 894}"},
            {"but", "437, 438, 895}"},
            {"bua", "450, 451, 896}"},
            {"cung", "463, 464, 897}"},
        };
        final int mocHamMap = map.indexOf("short getTemplateItemCreate(int level, int type, int charClass, int gender)");
        Kiem.dung("tim thay Map.getTemplateItemCreate", mocHamMap > 0);
        if (mocHamMap > 0) {
            final int mocKet = map.indexOf("private Char getBot(int type)", mocHamMap);
            final String than = map.substring(mocHamMap, mocKet > mocHamMap ? mocKet : map.length());
            for (final String[] hang : hangVuKhi) {
                Kiem.dung("Map.java: hang " + hang[0] + " co id level-86 o cuoi (" + hang[1] + ")",
                        than.contains(hang[1]));
            }
        }

        // ---- 2. RealController.getTemplateItemCreate(): doi chieu lai CHINH ban da dung -
        // van phai giu du 5 id level-86 nay (neu test nay do, nghia la ban RealController
        // bi sua lui, can dong bo lai ca Map.java). ----
        final int mocHamRC = realCtrl.indexOf("private short getTemplateItemCreate(int level, int type, int charClass, int gender)");
        Kiem.dung("tim thay RealController.getTemplateItemCreate", mocHamRC > 0);
        if (mocHamRC > 0) {
            final int mocKetRC = realCtrl.indexOf("public void onDisconnectedFromLocalPlace", mocHamRC);
            final String than = realCtrl.substring(mocHamRC, mocKetRC > mocHamRC ? mocKetRC : realCtrl.length());
            for (final String[] hang : hangVuKhi) {
                Kiem.dung("RealController.java: hang " + hang[0] + " co id level-86 o cuoi (" + hang[1] + ")",
                        than.contains(hang[1]));
            }
        }

        // ---- 3. MapCheTao.java: menu chon cap do vu khi phai co 14 muc (khong phai 13) -
        // thieu muc nay thi "Cap do 86" khong bao gio hien ra de chon, du bang tra o Map.java
        // co du du lieu. ----
        final int mocMenu = cheTao.indexOf("if (!nameItem.equals(\"\")) {");
        Kiem.dung("tim thay khoi menu chon cap do vu khi trong MapCheTao", mocMenu > 0);
        if (mocMenu > 0) {
            final String doan = cheTao.substring(mocMenu, Math.min(cheTao.length(), mocMenu + 600));
            Kiem.dung("MapCheTao.java: menu cap do vu khi la new String[14] (khong phai 13)",
                    doan.contains("new String[14]"));
            Kiem.dung("MapCheTao.java: vong lap dien menu la i < 14 (khong phai 13)",
                    doan.contains("i < 14"));
            Kiem.sai("MapCheTao.java: KHONG con new String[13] o day (ban cu, thieu muc lv86)",
                    doan.contains("new String[13]"));
        }

        // Hai ham doi tu void sang boolean (commit 4645536): MapCheTao chi tru 5000 xu khi ham
        // tra ve true - nen tim theo chu ky "boolean ...".
        // ---- 4. Map.java: doUserUpLevelItem/doUserUpLevelItemSpecial (nut "nâng cấp" THAT
        // tren item co san) phai chan o ">= 86", khong con ">= 81". ----
        final int mocUpLevel = map.indexOf("boolean doUserUpLevelItem(Char player, Message message)");
        Kiem.dung("tim thay doUserUpLevelItem", mocUpLevel > 0);
        if (mocUpLevel > 0) {
            final int mocKetUpLevel = map.indexOf("boolean doUserUpLevelItemSpecial(Char player, Message message)", mocUpLevel);
            final String than = map.substring(mocUpLevel, mocKetUpLevel > mocUpLevel ? mocKetUpLevel : map.length());
            Kiem.dung("doUserUpLevelItem: chan o item.level >= 86 (khong phai 81)",
                    than.contains("item.level >= 86"));
            Kiem.sai("doUserUpLevelItem: KHONG con chan o item.level >= 81",
                    than.contains("item.level >= 81"));
        }
        final int mocUpLevelSp = map.indexOf("boolean doUserUpLevelItemSpecial(Char player, Message message)");
        Kiem.dung("tim thay doUserUpLevelItemSpecial", mocUpLevelSp > 0);
        if (mocUpLevelSp > 0) {
            final int mocKetSp = map.indexOf("void doSendIfoMaterial2Client(Char player, int type)", mocUpLevelSp);
            final String than = map.substring(mocUpLevelSp, mocKetSp > mocUpLevelSp ? mocKetSp : map.length());
            Kiem.dung("doUserUpLevelItemSpecial: chan o item.level >= 86 (khong phai 81)",
                    than.contains("item.level >= 86"));
            Kiem.sai("doUserUpLevelItemSpecial: KHONG con chan o item.level >= 81",
                    than.contains("item.level >= 81"));
        }

        // ---- 4b. CHOT AN TOAN: phai KIEM tra bac tren truoc khi thang cap, khong duoc
        // trong cay vao cot idUpLevel trong CSDL.
        //
        // Neu CSDL that chua chay lenh cap nhat o MUC 5 (hoac chay roi ma chua khoi dong
        // lai may chu - data_item chi doc mot lan luc khoi dong), getIdTemplateUpLevel()
        // doan bang "id + 1" va tra ve mon cua MON PHAI KHAC:
        //     412 kiem -> 413 DAO lv21 | 425 dao -> 426 BUT lv21 | 438 but -> 439 BUA lv21
        //     451 bua  -> 452 CUNG lv21 | 464 cung -> 465 CUOC THUONG lv1
        // Bon dong dau AN MAT vu khi lv81 va tra lai mon lv21 sai mon phai (mat do that);
        // dong cuoi lam vo chi so mang (-3) roi treo client o "Xin chờ...".
        Kiem.dung("Map.java: co ham chot bac tren (bacTrenKhongHopLe)",
                map.contains("private boolean bacTrenKhongHopLe("));
        for (final String ham : new String[]{
                "boolean doUserUpLevelItem(Char player, Message message)",
                "boolean doUserUpLevelItemSpecial(Char player, Message message)"}) {
            final int mocH = map.indexOf(ham);
            Kiem.dung("tim thay " + ham, mocH > 0);
            if (mocH > 0) {
                final int mocKetH = map.indexOf("doCreateNewItem(", mocH);
                final String thanH = map.substring(mocH, mocKetH > mocH ? mocKetH : map.length());
                Kiem.dung(ham + ": goi bacTrenKhongHopLe TRUOC khi tao mon moi",
                        thanH.contains("bacTrenKhongHopLe(player, item)"));
            }
        }
        final int mocChot = map.indexOf("private boolean bacTrenKhongHopLe(");
        if (mocChot > 0) {
            final int mocKetChot = map.indexOf("boolean doUserUpLevelItemSpecial(", mocChot);
            final String thanChot = map.substring(mocChot, mocKetChot > mocChot ? mocKetChot : map.length());
            Kiem.dung("chot: doi chieu DUNG LOAI do (type)", thanChot.contains("bacTren.type == typeCu"));
            Kiem.dung("chot: doi chieu DUNG mot bac (level + 5)",
                    thanChot.contains("bacTren.level == item.level + 5"));
            Kiem.dung("chot: chan ca truong hop khong tim thay template (null)",
                    thanChot.contains("bacTren != null"));
        }

        // ---- 5. sql/htbb_day_du.sql: 5 vu khi bac 81 (kiem/dao/but/bua/cung) phai co
        // idUpLevel tro DUNG toi id level-86 tuong ung, khong con -1 (mac dinh id+1 SAI). ----
        final String[][] chuoiNangCap = {{"412", "893"}, {"425", "894"}, {"438", "895"}, {"451", "896"}, {"464", "897"}};
        for (final String[] dong : chuoiNangCap) {
            Kiem.bang("data_item " + dong[0] + " (vu khi bac 81) co idUpLevel=" + dong[1] + " (khong con -1)", dong[1],
                    DayDu.giaTri("data_item", "id", dong[0], "iduplevel"));
            Kiem.bang("data_item " + dong[1] + " la bac 86", "86", DayDu.giaTri("data_item", "id", dong[1], "level"));
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemNangCapDoCamTayLv86() {
    }
}
