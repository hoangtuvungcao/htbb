import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHARACTERIZATION TEST (xem chu thich dau test/Kiem.java): chot lai fix cho loi "nang cap
 * vu khi tu lv81 len lv86 khong duoc". Phat hien 05/09/2026 tu anh chup man hinh cua nguoi
 * choi (vu khi "Anh Vu Cung" - id 464, level 81, la item CAO NHAT hang cung o NPC "Tho Ren
 * Than Bi", npcType -8).
 *
 * GOC RE: he thong "che tao vu khi" (Kiem/Dai dao/But/Bua/Cung) bi COPY thanh HAI ban gan
 * giong het nhau, ca hai deu xu ly cung npcType -8:
 * - RealController.doCreateItem() + RealController.getTemplateItemCreate() (rieng, private) -
 * ban NAY da duoc cap nhat them cap 86 tu truoc (menu 14 muc, bang tra 14 phan tu/hang).
 * - MapCheTao.doCreateItem() (goi qua MapDoMenu.java case -8) + Map.getTemplateItemCreate() -
 * ban NAY la ban CU, quen cap nhat: menu chi co 13 muc (dung lv21..81, KHONG BAO GIO hien
 * "Cap do 86" de nguoi choi chon), va bang tra idTempateItemCreate cho 5 hang vu khi (kiem/
 * dao/but/bua/cung, type 3-7) cung chi co 13 phan tu (thieu id level-86: 893/894/895/896/897).
 * Neu ai do lam menu co 14 muc ma quen sua bang tra (hoac nguoc lai), chon "Cap do 86" se nem
 * ArrayIndexOutOfBoundsException ngay tai idTempateItemCreate[type][13] - loi nay bi nuot boi
 * catch (Exception) bao quanh ca ham (MapCheTao.doCreateItem dong cuoi), nen nguoi choi khong
 * thay thong bao loi nao ca, chi thay khong co phan ung.
 *
 * Fix: dong bo CA HAI cho nay khop voi ban RealController da dung san - menu 14 muc (khong
 * phai 13) va bang tra 14 phan tu/hang voi id level-86 dung o cuoi.
 */
public final class KiemNangCapVuKhiLv86 {

    private static final String F_MAP = "src/real/Map.java";
    private static final String F_CHETAO = "src/real/MapCheTao.java";
    private static final String F_REALCTRL = "src/real/RealController.java";

    public static void chay() {
        Kiem.nhom("Nang cap vu khi lv81 -> lv86 (NPC Tho Ren Than Bi, npcType -8)");

        final String map = doc(F_MAP);
        final String cheTao = doc(F_CHETAO);
        final String realCtrl = doc(F_REALCTRL);
        Kiem.dung("doc duoc " + F_MAP, !map.isEmpty());
        Kiem.dung("doc duoc " + F_CHETAO, !cheTao.isEmpty());
        Kiem.dung("doc duoc " + F_REALCTRL, !realCtrl.isEmpty());
        if (map.isEmpty() || cheTao.isEmpty() || realCtrl.isEmpty()) {
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
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemNangCapVuKhiLv86() {
    }
}
