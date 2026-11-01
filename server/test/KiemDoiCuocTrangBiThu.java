import real.Char;
import real.Item;

/**
 * 0.9.29 (chu du an: "APK va JAR khong co may cai do" - khong thay / thao / thay duoc do thu va cuoc): duong lenh 29 THAT
 * (real.DungVatPham.dung -> Map.doUseItemGoc / thaoTrangBi / Map.thaoTrangBiThu) cho
 * <ul>
 *   <li>cuoc (type 13, Char.itemAx, o mac 9): mac cuoc thu hai = THAY (cuoc cu ve tui, ca khi tui day), khong con
 *   "Không thể sử dụng cùng lúc 2 loại cuốc"; itemAx lech (o bo mac khac) cung duoc go; thao = ve tui, itemAx = null;
 *   cuoc cap cao = bao "Cần cấp N để dùng" (truoc: chi in System.out);</li>
 *   <li>do thu cuoi (type 14..18): mac / thay (mon cu ve tui) / thao (bao "Đã tháo") / thao khi tui day = tu choi, mon
 *   giu nguyen; chua cuoi thu = bao ro.</li>
 * </ul>
 * Client cu (>= 0.9.27) gui cung goi 29 - chi doi ket qua phia may chu.
 */
public final class KiemDoiCuocTrangBiThu {

    private KiemDoiCuocTrangBiThu() {
    }

    static final int CUOC_A = 2201, CUOC_B = 2202, CUOC_CAO = 2203, GIAP_A = 2211, GIAP_B = 2212, DO_LOT = 2220;

    public static void chay() {
        Kiem.nhom("Doi cuoc + trang bi thu (0.9.29: thay cuoc, thao / thay do thu qua lenh 29)");
        try {
            chuanBi();
            cuoc();
            cuocLech();
            cuocTuiDay();
            doThu();
            noiVaoGame();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            KiemDungVatPham.traLai();
        }
    }

    static real.GemTemplate[] gemCu;

    static void chuanBi() {
        gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        if (KiemDungVatPham.map == null) {
            KiemDungVatPham.map = new real.Map();
        }
        while (real.Map.itemTemplates.size() < 6) {
            real.Map.itemTemplates.add(new java.util.Hashtable<Integer, real.ItemTemplates>());
        }
        KiemDungVatPham.mau(CUOC_A, 13, "Cuốc sắt", 1).clazz = -1;
        KiemDungVatPham.mau(CUOC_B, 13, "Cuốc vàng", 1).clazz = -1;
        KiemDungVatPham.mau(CUOC_CAO, 13, "Cuốc kim cương", 60).clazz = -1;
        KiemDungVatPham.mau(GIAP_A, 14, "Giáp cưỡi Poké", 1).clazz = -1;
        KiemDungVatPham.mau(GIAP_B, 14, "Giáp cưỡi Great", 1).clazz = -1;
        KiemDungVatPham.mau(DO_LOT, 26, "Trứng lót tay", 1).clazz = -1;
    }

    static Item oCuoc(final Char p) {
        return p.getArrayCharWearing()[Char.getIndexItemWearing(13, 0)];
    }

    static String[] dung(final Char p, final Item it) throws Exception {
        return KiemDungVatPham.dung(p, it.id);
    }

    static boolean trongTui(final Char p, final Item it) {
        return p.iItems.contains(it);
    }

    static void cuoc() throws Exception {
        final Char p = KiemDungVatPham.nhanVat();
        final Item a = KiemDungVatPham.cho(p, CUOC_A);
        final Item b = KiemDungVatPham.cho(p, CUOC_B);
        String[] r = dung(p, a);
        Kiem.dung("mac cuoc A: itemAx = A, o mac 9 = A, roi tui (" + KiemDungVatPham.chu(r) + ")",
                p.itemAx == a && oCuoc(p) == a && !trongTui(p, a));
        final int tui = p.iItems.size();
        r = dung(p, b);
        Kiem.dung("mac cuoc B khi dang mac A: KHONG con bao '2 loai cuoc' (" + KiemDungVatPham.chu(r) + ")",
                r == null || r[1].indexOf("cùng lúc 2 loại cuốc") < 0);
        Kiem.dung("thay cuoc: itemAx = B, o mac 9 = B", p.itemAx == b && oCuoc(p) == b);
        Kiem.dung("thay cuoc: A ve tui, B roi tui", trongTui(p, a) && !trongTui(p, b) && a.place == 0);
        Kiem.bang("thay cuoc: so mon trong tui khong doi", tui, p.iItems.size());
        Kiem.dung("thay cuoc: bao da trang bi (" + KiemDungVatPham.chu(r) + ")", r != null && "OK".equals(r[0]));
        r = dung(p, b);
        Kiem.dung("thao cuoc B (id mon dang mac): bao da thao (" + KiemDungVatPham.chu(r) + ")", r != null && r[1].startsWith("Đã tháo"));
        Kiem.dung("thao cuoc: itemAx = null, o 9 trong, B ve tui", p.itemAx == null && oCuoc(p) == null && trongTui(p, b));
        final Item cao = KiemDungVatPham.cho(p, CUOC_CAO);
        r = dung(p, cao);
        Kiem.dung("cuoc cap 60 (nhan vat 50): bao 'Cần cấp 60 để dùng' (" + KiemDungVatPham.chu(r) + ")",
                r != null && "LOI".equals(r[0]) && r[1].startsWith("Cần cấp 60 để dùng"));
        Kiem.dung("cuoc cap cao: van trong tui, itemAx khong doi", trongTui(p, cao) && p.itemAx == null);
        // client cu (khong -104): van nhan hop thoai 37 voi cung cau
        p.getSession().banClient = -1;
        KiemDungVatPham.GOI.clear();
        real.DungVatPham.dung(KiemDungVatPham.map, p, KiemDungVatPham.goi(29, true, cao.id));
        Kiem.dung("client cu: cuoc cap cao -> hop thoai 37", KiemDungVatPham.coGoi(37) && !KiemDungVatPham.coGoi(real.DungVatPham.LENH));
    }

    /** itemAx tro vao cuoc o bo mac KHAC (du lieu cu): mac cuoc moi van go cuoc cu ve tui. */
    static void cuocLech() throws Exception {
        final Char p = KiemDungVatPham.nhanVat();
        final Item a = KiemDungVatPham.cho(p, CUOC_A);
        final Item b = KiemDungVatPham.cho(p, CUOC_B);
        p.iItems.remove(a);
        final int khac = p.slotWearing == 0 ? 1 : 0;
        p.wItems[khac][Char.getIndexItemWearing(13, 0)] = a;
        a.place = 1;
        p.itemAx = a;
        final String[] r = dung(p, b);
        Kiem.dung("itemAx lech: mac B thanh cong (" + KiemDungVatPham.chu(r) + ")", p.itemAx == b && oCuoc(p) == b);
        Kiem.dung("itemAx lech: A go khoi bo mac khac, ve tui", p.wItems[khac][Char.getIndexItemWearing(13, 0)] == null && trongTui(p, a));
    }

    /** Thay cuoc khi tui day: doi cho (B ra, A vao) - khong can o trong. */
    static void cuocTuiDay() throws Exception {
        final Char p = KiemDungVatPham.nhanVat();
        final Item a = KiemDungVatPham.cho(p, CUOC_A);
        dung(p, a);
        final Item b = KiemDungVatPham.cho(p, CUOC_B);
        while (!p.isFullInventory()) {
            KiemDungVatPham.cho(p, DO_LOT);
        }
        final String[] r = dung(p, b);
        Kiem.dung("tui day: thay cuoc van duoc (" + KiemDungVatPham.chu(r) + ")", p.itemAx == b && trongTui(p, a) && !trongTui(p, b));
        final String[] t = dung(p, b);
        Kiem.dung("tui day: thao cuoc bi tu choi, giu nguyen (" + KiemDungVatPham.chu(t) + ")",
                t != null && t[1].indexOf("Hành trang đầy") >= 0 && p.itemAx == b);
    }

    static data.Animal thu(final int dbid) throws Exception {
        final java.lang.reflect.Constructor<data.Animal> ct = data.Animal.class.getDeclaredConstructor();
        ct.setAccessible(true);
        final data.Animal a = ct.newInstance();
        a.dbid = dbid;
        return a;
    }

    static void doThu() throws Exception {
        final Char p = KiemDungVatPham.nhanVat();
        final Item a = KiemDungVatPham.cho(p, GIAP_A);
        String[] r = dung(p, a);
        Kiem.dung("chua cuoi thu: bao phai cuoi (" + KiemDungVatPham.chu(r) + ")", r != null && r[1].indexOf("cưỡi") >= 0 && trongTui(p, a));
        p.animalRide = thu(77);
        r = dung(p, a);
        Kiem.dung("mac giap cuoi: vao do thu cua con dang cuoi (" + KiemDungVatPham.chu(r) + ")",
                p.awItems.contains(a) && a.idAnimal == 77 && !trongTui(p, a));
        final Item b = KiemDungVatPham.cho(p, GIAP_B);
        final int tui = p.iItems.size();
        r = dung(p, b);
        Kiem.dung("thay giap cuoi: B mac, A ve tui (" + KiemDungVatPham.chu(r) + ")",
                p.awItems.contains(b) && !p.awItems.contains(a) && trongTui(p, a) && a.idAnimal == -1);
        Kiem.bang("thay giap cuoi: so mon trong tui khong doi", tui, p.iItems.size());
        r = dung(p, b);
        Kiem.dung("thao giap cuoi (id mon dang mac): bao da thao (" + KiemDungVatPham.chu(r) + ")",
                r != null && r[1].startsWith("Đã tháo") && trongTui(p, b) && !p.awItems.contains(b));
        Kiem.dung("thao giap cuoi: gui lai bang do thu (15) + tui (16)", KiemDungVatPham.coGoi(15) && KiemDungVatPham.coGoi(16));
        dung(p, a);
        while (!p.isFullInventory()) {
            KiemDungVatPham.cho(p, DO_LOT);
        }
        r = dung(p, a);
        Kiem.dung("tui day: thao giap bi tu choi, giu nguyen (" + KiemDungVatPham.chu(r) + ")",
                r != null && r[1].indexOf("Hành trang đầy") >= 0 && p.awItems.contains(a));
    }

    static void noiVaoGame() {
        final String m = KiemDungVatPham.doc("src/real/Map.java");
        Kiem.dung("Map.doUseItemGoc: khong con chan '2 loai cuoc'", m.indexOf("Không thể sử dụng cùng lúc 2 loại cuốc") < 0);
        Kiem.dung("Map.doUseItemGoc: cuoc goi DungVatPham.truocDoiCuoc truoc exchangeItem", m.indexOf("DungVatPham.truocDoiCuoc(player);") > 0);
        Kiem.dung("Map.doUseItemGoc: cap cao bao DungVatPham.canCap", m.indexOf("DungVatPham.loi(player, DungVatPham.canCap(item));") > 0);
        final String c = KiemDungVatPham.doc("../client_jar/src/game/pokemon/TrangBiThu.java");
        Kiem.dung("client: bang Trang bi thu (Tháo / Thay qua lenh 29)", c.indexOf("DungDo.thao(it);") > 0 && c.indexOf("DungDo.dungItem(it);") > 0);
        Kiem.dung("client: J2ME Ban than > Trang bi thu mo bang moi",
                KiemDungVatPham.doc("../client_jar/shared/mod/game/render/screen/PauseMenu.java").indexOf("game.pokemon.TrangBiThu.mo();") > 0);
        Kiem.dung("client: o Cuoc trong man Nhan vat (type 13)",
                KiemDungVatPham.doc("../client_jar/src/game/pokemon/ManNhanVat.java").indexOf("{}, {13}};") > 0);
    }
}
