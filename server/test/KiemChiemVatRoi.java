import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * NHAT VAT ROI: moi duong nhat phai CHIEM duoc vat truoc khi cap thuong (vá 03/09/2026).
 *
 * NGUYEN NHAN. Ba loai vat nam tren dat (Item / Potion / GemItem / ItemQuest) duoc giu trong
 * Vector cua Map. Duong nhat cu deu theo mau: getX(id) tim tren dat -> removeX(...) -> cong vao
 * tui. Hai buoc dau KHONG nguyen tu voi nhau, va hai luong khac nhau cung cham vao mot vat:
 *   - goi 18 / 19 / -41 / -65 nam trong RealController.privateCmd -> chay tren LUONG PHIEN,
 *   - vong tu dong nhat (CharVongLap.update doc idItem/idPotion/idGem/idItemQuest do Monster
 *     day vao khi quai chet) -> chay tren LUONG BAN DO.
 * Ca hai deu thay vat con tren dat, ca hai goi removeX (lan hai la no-op va ket qua bi bo qua),
 * roi CA HAI cong vao tui: mot vat roi thanh hai. Sau 5-10 giay belongUser ve 0
 * (deletePotionAndItemOnGround) nen hai NGUOI CHOI khac nhau cung dua duoc.
 *
 * SUA. Them Map.chiemGem/chiemPotion/chiemItem/chiemItemQuest: dua thang ket qua cua
 * Vector.remove(Object) (nguyen tu, chi tra true cho luong that su rut duoc phan tu) ra ngoai,
 * kem kiem bien country. Moi duong nhat doi sang mau "if (!chiemX(...)) return;" TRUOC khi cong.
 * Bon ham removeX (void) GIU NGUYEN vi lop con trong lib/htbb-core.jar con goi chung - doi chu ky
 * la doi descriptor, lop trong jar se khong con ghi de dung.
 *
 * Test nay quet ma nguon (khong dung duoc Map that: khoi static cua Map mo ket noi DB va sinh
 * luong), bao dam khong ai vo tinh dua mau cu tro lai.
 */
public final class KiemChiemVatRoi {

    private KiemChiemVatRoi() {
    }

    public static void chay() {
        Kiem.nhom("Nhat vat roi - chiem nguyen tu truoc khi cap thuong");

        final String map = doc("src/real/Map.java");
        final String mapClan = doc("src/real/MapClan.java");
        final String xuLyLenh = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("doc duoc Map.java", !map.isEmpty());
        Kiem.dung("doc duoc MapClan.java", !mapClan.isEmpty());
        Kiem.dung("doc duoc MapXuLyLenh.java", !xuLyLenh.isEmpty());

        bonHamChiemTonTai(map);
        moiHamChiemTraVeKetQuaRemove(map);
        khongConDuongNhatDungRemoveTrucTiep(map, mapClan, xuLyLenh);
        moiDuongNhatDeuChiemTruoc(map, mapClan, xuLyLenh);
        expToDoiTinhBangLong(map);
        chanExpAm(xuLyLenh);
        suaDoTruTienTruoc(doc("src/real/Char.java"));
        luongVanTieuKhongTuChet(map);
    }

    // ------------------------------------------------------------- cac ham chiem

    private static void bonHamChiemTonTai(final String map) {
        Kiem.dung("Map co chiemGem", map.contains("protected boolean chiemGem(GemItem pt, int country)"));
        Kiem.dung("Map co chiemPotion", map.contains("protected boolean chiemPotion(Potion pt, int country)"));
        Kiem.dung("Map co chiemItem", map.contains("protected boolean chiemItem(Item pt, int country)"));
        Kiem.dung("Map co chiemItemQuest", map.contains("protected boolean chiemItemQuest(ItemQuest pt, int country)"));
        // Bon ham remove* cu phai CON: lop con trong htbb-core.jar goi chung.
        Kiem.dung("van con removeGem (jar goi)", map.contains("protected void removeGem(GemItem pt, int country)"));
        Kiem.dung("van con removePotion (jar goi)", map.contains("protected void removePotion(Potion pt, int country)"));
        Kiem.dung("van con removeItem (jar goi)", map.contains("protected void removeItem(Item pt, int country)"));
        Kiem.dung("van con removeItemQuest (jar goi)", map.contains("protected void removeItemQuest(ItemQuest pt, int country)"));
    }

    /**
     * Than moi ham chiem phai TRA VE ket qua cua Vector.remove - do moi la phep chiem. Neu ai
     * doi thanh "remove(pt); return true;" thi loi nhan doi tro lai y nguyen ma test khac khong
     * he do.
     */
    private static void moiHamChiemTraVeKetQuaRemove(final String map) {
        final String[][] cap = {
                {"chiemGem", "return ((Vector) this.gemItem.get(country)).remove(pt);"},
                {"chiemPotion", "return ((Vector) this.potions.get(country)).remove(pt);"},
                {"chiemItem", "return ((Vector) this.items.get(country)).remove(pt);"},
                {"chiemItemQuest", "return ((Vector) this.itemquests.get(country)).remove(pt);"},
        };
        for (final String[] c : cap) {
            final String than = thanHam(map, "protected boolean " + c[0] + "(");
            Kiem.dung(c[0] + " tra ve ket qua Vector.remove", than.contains(c[1]));
            Kiem.dung(c[0] + " kiem bien country", than.contains("country < 0 || country >="));
            Kiem.dung(c[0] + " kiem pt null", than.contains("pt == null"));
        }
    }

    // ------------------------------------------------- khong con mau cu o duong nhat

    /**
     * Cac ham NHAT khong duoc goi removeX truc tiep nua. Chi kiem trong than tung ham nhat -
     * removeX van duoc dung o cho khac (don vat het han, xoa khi doi map...) va do la dung.
     */
    private static void khongConDuongNhatDungRemoveTrucTiep(final String map, final String mapClan,
            final String xuLyLenh) {
        final String[][] cacHam = {
                {"Map.doGetGemItem(Message)", thanHam(map, "protected void doGetGemItem(Char player, Message message)"), "removeGem"},
                {"Map.doGetGemItem(cat,id)", thanHam(map, "public void doGetGemItem(Char player, int cat, short id)"), "removeGem"},
                {"Map.doGetItem(Message)", thanHam(map, "protected void doGetItem(Char player, Message message)"), "removeItem"},
                {"Map.doGetPotion(short)", thanHam(map, "public void doGetPotion(Char player, short id)"), "removePotion"},
                {"Map.doGetPotion(Message)", thanHam(map, "protected void doGetPotion(Char player, Message message)"), "removePotion"},
                {"Map.doGetItemQuest(Message)", thanHam(map, "void doGetItemQuest(Char player, Message message)"), "removeItemQuest"},
                {"Map.doGetItemQuest(short)", thanHam(map, "public void doGetItemQuest(Char player, short id)"), "removeItemQuest"},
                {"MapClan.doGetItem(Message)", thanHam(mapClan, "protected void doGetItem(Char player, Message message)"), "removeItem"},
                {"MapClan.doGetPotion(Message)", thanHam(mapClan, "protected void doGetPotion(Char player, Message message)"), "removePotion"},
                {"MapClan.doGetGemItem(Message)", thanHam(mapClan, "protected void doGetGemItem(Char player, Message message)"), "removeGem"},
                {"MapXuLyLenh.doGetItem", thanHam(xuLyLenh, "static void doGetItem(Map map, Char player, short id)"), "removeItem"},
        };
        for (final String[] h : cacHam) {
            Kiem.dung("tim duoc than " + h[0], !h[1].isEmpty());
            Kiem.sai(h[0] + " KHONG con goi " + h[2] + " truc tiep", h[1].contains("." + h[2] + "("));
        }
    }

    /** Va moi than ham nhat do phai co dung mau "if (!chiem...)" it nhat mot lan. */
    private static void moiDuongNhatDeuChiemTruoc(final String map, final String mapClan,
            final String xuLyLenh) {
        final String[][] cacHam = {
                {"Map.doGetGemItem(Message)", thanHam(map, "protected void doGetGemItem(Char player, Message message)"), "chiemGem", "2"},
                {"Map.doGetGemItem(cat,id)", thanHam(map, "public void doGetGemItem(Char player, int cat, short id)"), "chiemGem", "1"},
                {"Map.doGetItem(Message)", thanHam(map, "protected void doGetItem(Char player, Message message)"), "chiemItem", "2"},
                {"Map.doGetPotion(short)", thanHam(map, "public void doGetPotion(Char player, short id)"), "chiemPotion", "1"},
                {"Map.doGetPotion(Message)", thanHam(map, "protected void doGetPotion(Char player, Message message)"), "chiemPotion", "1"},
                {"Map.doGetItemQuest(Message)", thanHam(map, "void doGetItemQuest(Char player, Message message)"), "chiemItemQuest", "1"},
                {"Map.doGetItemQuest(short)", thanHam(map, "public void doGetItemQuest(Char player, short id)"), "chiemItemQuest", "1"},
                {"MapClan.doGetItem(Message)", thanHam(mapClan, "protected void doGetItem(Char player, Message message)"), "chiemItem", "1"},
                {"MapClan.doGetPotion(Message)", thanHam(mapClan, "protected void doGetPotion(Char player, Message message)"), "chiemPotion", "1"},
                {"MapClan.doGetGemItem(Message)", thanHam(mapClan, "protected void doGetGemItem(Char player, Message message)"), "chiemGem", "2"},
                {"MapXuLyLenh.doGetItem", thanHam(xuLyLenh, "static void doGetItem(Map map, Char player, short id)"), "chiemItem", "1"},
        };
        for (final String[] h : cacHam) {
            final int soLan = demSoLan(h[1], "!" + (h[1].contains("map.chiem") ? "map." : "this.") + h[2] + "(");
            Kiem.dung(h[0] + " chiem truoc khi cap (" + h[3] + " nhanh)",
                    soLan >= Integer.parseInt(h[3]));
        }
    }

    // ------------------------------------------------------------- exp

    /**
     * Quai co rcvxp toi 20.000.000 (Dracula, Bi ngo). xp * 400 tran int ngay tu ~5,4 trieu ->
     * increaXp AM -> addXPForChar nhan dxp am (ban cu chi chan dxp == 0) -> CA TO DOI tut cap,
     * exp co the am roi LevelDetail nem loi ngay giua khoi synchronized.
     */
    private static void expToDoiTinhBangLong(final String map) {
        final String than = thanHam(map, "public static int calculatorXpParty(Char p, int xp)");
        Kiem.dung("tim duoc calculatorXpParty", !than.isEmpty());
        Kiem.dung("calculatorXpParty tinh bang long", than.contains("long tong = (long) xp + (long) xp * percent / 100L;"));
        Kiem.dung("calculatorXpParty chan tran", than.contains("Math.min((long) Integer.MAX_VALUE, tong)"));
        Kiem.sai("khong con phep nhan int tran", than.contains("int increaXp = xp * percent / 100;"));
    }

    private static void chanExpAm(final String xuLyLenh) {
        final String eventXp = thanHam(xuLyLenh, "static void addXpCharEvent(Char p, long dxp");
        final String forChar = thanHam(xuLyLenh, "static void addXPForChar(Char p, long dxp");
        Kiem.dung("addXpCharEvent chan dxp am", eventXp.contains("if (dxp < 0L)"));
        Kiem.dung("addXPForChar chan dxp am", forChar.contains("if (dxp < 0L)"));
        Kiem.dung("cho exp am co ghi log de truy nguyen", eventXp.contains("\"expam\"") && forChar.contains("\"expam\""));
    }

    // ------------------------------------------------------------- sua do

    /**
     * Goi 72 (sua do) chay tren luong phien; mot duong tieu xu khac (mua o Phu ong) chay tren
     * luong ban do. Ban cu kiem "xu < price" roi phuc hoi do ben TRUOC, cuoi ham moi subXu ->
     * hai luong deu qua kiem, subXu chan ve 0 -> sua do gan nhu mien phi.
     */
    private static void suaDoTruTienTruoc(final String charJava) {
        Kiem.dung("doc duoc Char.java", !charJava.isEmpty());
        Kiem.dung("Char co truXuNeuDu (kiem-va-tru nguyen tu)",
                charJava.contains("public boolean truXuNeuDu(final long xu, final boolean isCountTop, final String vitri)"));
        Kiem.dung("Char co truLuongNeuDu", charJava.contains("public boolean truLuongNeuDu(final long l)"));
        Kiem.dung("Char co truLuongKhoaNeuDu", charJava.contains("public boolean truLuongKhoaNeuDu(final long l)"));

        final String than = thanHam(charJava, "public void repairItem(final int id)");
        Kiem.dung("tim duoc repairItem", !than.isEmpty());
        Kiem.dung("repairItem tru tien qua truXuNeuDu", than.contains("truXuNeuDu(price, true, \"sua do \" + id)"));
        Kiem.sai("repairItem khong con subXu cuoi ham", than.contains("this.subXu(price, true,"));
        // Tru tien phai dung TRUOC vong phuc hoi do ben.
        final int viTriTru = than.indexOf("truXuNeuDu(");
        final int viTriHoi = than.indexOf("item.durable = item.getTemplate().durable;");
        Kiem.dung("repairItem: tru tien truoc khi phuc hoi do ben",
                viTriTru > 0 && viTriHoi > 0 && viTriTru < viTriHoi);
    }

    // ------------------------------------------------------------- luong van tieu

    /**
     * Luong tinh cua van tieu duyet ALL_MONSTER_VANTIEU theo chi so trong khi luong ban do
     * them/bot xe: Vector rut phan tu giua size() va get(i) la AIOOBE, va truoc day than vong
     * lap khong co try/catch nen luong CHET vinh vien -> khong ai tra duoc tieu cho toi khi
     * restart.
     */
    private static void luongVanTieuKhongTuChet(final String map) {
        final int viTri = map.indexOf("while (!AdminHandler.isStopServer)");
        Kiem.dung("tim duoc luong van tieu trong khoi static", viTri > 0);
        final String doan = map.substring(viTri, Math.min(map.length(), viTri + 5000));
        Kiem.dung("duyet tren ban sao (toArray) chu khong theo chi so Vector",
                doan.contains("MonsterVantieu.ALL_MONSTER_VANTIEU.toArray()"));
        Kiem.dung("co try/catch Throwable de luong khong chet", doan.contains("catch (Throwable"));
        Kiem.dung("chi tra id monster ve kho khi that su go duoc", doan.contains("conTrongDanhSach"));
    }

    // ------------------------------------------------------------- tien ich

    /**
     * Cat than mot ham theo dau ngoac nhon can bang. Tra ve "" neu khong tim thay chu ky.
     * (Du dung cho ma nguon HTBB: khong co chuoi nao chua dau ngoac nhon le trong cac ham nay.)
     */
    private static String thanHam(final String nguon, final String chuKy) {
        final int batDau = nguon.indexOf(chuKy);
        if (batDau < 0) {
            return "";
        }
        final int moNgoac = nguon.indexOf('{', batDau + chuKy.length() - 1);
        if (moNgoac < 0) {
            return "";
        }
        int sau = 0;
        for (int i = moNgoac; i < nguon.length(); i++) {
            final char c = nguon.charAt(i);
            if (c == '{') {
                sau++;
            } else if (c == '}') {
                sau--;
                if (sau == 0) {
                    return nguon.substring(moNgoac, i + 1);
                }
            }
        }
        return "";
    }

    private static int demSoLan(final String nguon, final String mau) {
        int n = 0;
        int i = 0;
        while (true) {
            final int v = nguon.indexOf(mau, i);
            if (v < 0) {
                return n;
            }
            n++;
            i = v + mau.length();
        }
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
