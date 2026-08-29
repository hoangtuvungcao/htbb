import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Kiem thu viec khoi tao cac kho VAT THE NAM DUOI DAT cua Map.
 *
 * LOI DA XAY RA THAT (phat hien 29/08/2026, da chay tu rat lau truoc do):
 *
 *   java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
 *       at java.util.Vector.get(Vector.java:753)
 *       at real.Map.deletePotionAndItemOnGround(Map.java:3603)
 *       at real.MapVongLap.updateSand(MapVongLap.java:1007)
 *
 * Log cu con giu 58.869 lan trong mot lan chay va 333.291 lan trong lan truoc nua.
 * Server sinh 124 MB log moi ngay va an 22% CPU du chi co MOT nguoi choi.
 *
 * NGUYEN NHAN: chi MapKhoiTao.init() do day nam kho nay. OfflineMap goi
 * super(id) -> Map(int id), ma ham dung ngan do KHONG di qua MapKhoiTao. Nen moi ban
 * do offline chay voi cac Vector RONG.
 *
 * Cai lo ra chi la deletePotionAndItemOnGround (vi vong lap sand goi no moi nhip).
 * Ba duong con lai IM LANG ma con te hon: addPotion / addItemMap / addGemItem nem loi
 * -> do roi xuong dat tren ban do offline MAT HAN, khong ai biet vi sao.
 *
 * Da co nguoi vap phai dung ho loi nay roi va vao tay tung cho trong constructor cua
 * OfflineMap (allPlayerMessages, ALL_EFFECT_INMAP) — nhung bo sot bon kho con lai. Do
 * la ly do phai co bai kiem nay: khong phai de canh mot dong ma, ma de canh cai bay
 * "hai noi phai nho giong nhau".
 */
public final class KiemBanDoOffline {

    private static final String F_OFFLINE = "src/real/OfflineMap.java";
    private static final String F_KHOI_TAO = "src/real/MapKhoiTao.java";
    private static final String F_MAP = "src/real/Map.java";

    public static void chay() {
        kiemHanhVi();
        kiemCacHamDung();
        kiemLechMotDonVi();
    }

    // -----------------------------------------------------------------------
    //  Chay that tren doi tuong Map, khong doc ma nguon
    // -----------------------------------------------------------------------
    private static void kiemHanhVi() {
        Kiem.nhom("Kho vat the duoi dat - chay that");

        // Map() la ham dung rong, dung y het truong hop OfflineMap sau super(id):
        // moi truong deu la Vector rong.
        final real.Map m = new real.Map();

        Kiem.bang("moi tao: potions rong", 0, m.potions.size());
        Kiem.bang("moi tao: items rong", 0, m.items.size());
        Kiem.bang("moi tao: itemquests rong", 0, m.itemquests.size());
        Kiem.bang("moi tao: gemItem rong", 0, m.gemItem.size());
        Kiem.bang("moi tao: ALL_EFFECT_INMAP rong", 0, m.ALL_EFFECT_INMAP.size());

        // Day CHINH LA loi tren server: chua khoi tao thi ham nay nem ngoai le.
        Kiem.dung("chua khoi tao -> deletePotionAndItemOnGround(0) nem ngoai le"
                        + " (day la loi that tren server)",
                nemNgoaiLe(m));

        m.initVatTheDuoiDat();

        Kiem.bang("sau khoi tao: potions co " + real.Map.SO_NUOC + " ngan",
                real.Map.SO_NUOC, m.potions.size());
        Kiem.bang("sau khoi tao: items", real.Map.SO_NUOC, m.items.size());
        Kiem.bang("sau khoi tao: itemquests", real.Map.SO_NUOC, m.itemquests.size());
        Kiem.bang("sau khoi tao: gemItem", real.Map.SO_NUOC, m.gemItem.size());
        Kiem.bang("sau khoi tao: ALL_EFFECT_INMAP", real.Map.SO_NUOC, m.ALL_EFFECT_INMAP.size());

        Kiem.dung("moi ngan la mot Vector that, khong phai null",
                m.potions.get(0) != null && m.items.get(0) != null
                        && m.itemquests.get(0) != null && m.gemItem.get(0) != null
                        && m.ALL_EFFECT_INMAP.get(0) != null);

        // Ba nuoc: chi so 0, 1, 2 deu phai lay duoc.
        boolean layDuHet = true;
        for (int i = 0; i < real.Map.SO_NUOC; i++) {
            if (m.potions.get(i) == null || m.items.get(i) == null) {
                layDuHet = false;
            }
        }
        Kiem.dung("lay duoc ca ba nuoc (chi so 0..2)", layDuHet);

        // Cai quan trong nhat: loi goc phai het.
        Kiem.sai("sau khoi tao -> deletePotionAndItemOnGround(0) KHONG con nem nua",
                nemNgoaiLe(m));

        // Goi lai khong duoc nhan doi so ngan, vi ca MapKhoiTao lan OfflineMap deu goi.
        m.initVatTheDuoiDat();
        m.initVatTheDuoiDat();
        Kiem.bang("goi ba lan van dung " + real.Map.SO_NUOC + " ngan (potions)",
                real.Map.SO_NUOC, m.potions.size());
        Kiem.bang("goi ba lan van dung so ngan (gemItem)",
                real.Map.SO_NUOC, m.gemItem.size());

        // Bu cho du khi da co san mot phan (dung nhu OfflineMap tung tu them tay
        // ba ngan ALL_EFFECT_INMAP roi moi goi ham nay).
        final real.Map m2 = new real.Map();
        m2.ALL_EFFECT_INMAP.add(new java.util.Vector());
        m2.initVatTheDuoiDat();
        Kiem.bang("da co san 1 ngan -> bu them cho du, khong cong don",
                real.Map.SO_NUOC, m2.ALL_EFFECT_INMAP.size());
        Kiem.bang("ngan da co san van con nguyen (khong bi thay)",
                real.Map.SO_NUOC, m2.potions.size());

        Kiem.bang("SO_NUOC = 3 (chi so 0..2; MAX_COUNTRY=2 la CHI SO lon nhat)",
                3, real.Map.SO_NUOC);

        kiemKhoQuai();
    }

    /**
     * Ho kho thu hai: quai va nguoi choi.
     *
     * Day la cho bi bo sot o lan va DAU TIEN. Va xong loi cu thi lo ra ngay loi thu hai
     * cung goc, o MapVongLap.updateSand:857 — `map.tempMonster.get(0)`. No chi nem khi CO
     * nguoi dung tren ban do (dong 856 la `if (playersx.size() > 0 ...)`) nen truoc do bi
     * che khuat sau loi kia. Log cu co 1.012 lan cua no.
     */
    private static void kiemKhoQuai() {
        Kiem.nhom("Kho quai va nguoi choi - chay that");

        final real.Map m = new real.Map();

        // allPlayerMessages / monsters / temp_monsters KHONG public. Khong noi quyen truy
        // cap cua chung chi de chieu bai kiem — chung duoc doi chieu o phan doc ma nguon
        // ben duoi. Ba truong public con lai da du de bat dung loi that (dong 857 doc
        // tempMonster).
        Kiem.bang("moi tao: allPlayers rong", 0, m.allPlayers.size());
        Kiem.bang("moi tao: tempMonster rong", 0, m.tempMonster.size());
        Kiem.bang("moi tao: tempRemoveMonster rong", 0, m.tempRemoveMonster.size());

        // Chinh la phep truy cap o MapVongLap.java:857.
        Kiem.dung("chua khoi tao -> tempMonster.get(0) nem ngoai le (loi that o dong 857)",
                nemKhiLayTempMonster(m));

        m.initKhoQuaiVaNguoi();

        Kiem.bang("sau khoi tao: allPlayers", real.Map.SO_NUOC, m.allPlayers.size());
        Kiem.bang("sau khoi tao: tempMonster", real.Map.SO_NUOC, m.tempMonster.size());
        Kiem.bang("sau khoi tao: tempRemoveMonster", real.Map.SO_NUOC, m.tempRemoveMonster.size());

        Kiem.sai("sau khoi tao -> tempMonster.get(0) KHONG con nem nua",
                nemKhiLayTempMonster(m));

        // initAllPlayer/initAllMonsTer truoc day cong don mu quang. Ba lop con khac
        // (MapChauBau, MapChienTruongMoba, MapLienDau) co goi initAllMonsTer() rieng, nen
        // chung PHAI khong cong don, khong thi so ngan thanh 6.
        m.initKhoQuaiVaNguoi();
        m.initAllPlayer();
        m.initAllMonsTer();
        Kiem.bang("initAllPlayer goi them khong cong don", real.Map.SO_NUOC, m.allPlayers.size());
        Kiem.bang("tempMonster van dung so ngan", real.Map.SO_NUOC, m.tempMonster.size());
        Kiem.bang("tempRemoveMonster van dung so ngan",
                real.Map.SO_NUOC, m.tempRemoveMonster.size());
    }

    private static boolean nemKhiLayTempMonster(final real.Map m) {
        try {
            return m.tempMonster.get(0) == null && false;
        } catch (final Throwable ex) {
            return true;
        }
    }

    /** Goi ham gay loi va cho biet no co nem ngoai le hay khong. */
    private static boolean nemNgoaiLe(final real.Map m) {
        try {
            m.deletePotionAndItemOnGround(0);
            return false;
        } catch (final Throwable ex) {
            return true;
        }
    }

    // -----------------------------------------------------------------------
    //  Moi duong tao Map deu phai goi khoi tao
    // -----------------------------------------------------------------------
    private static void kiemCacHamDung() {
        Kiem.nhom("Moi duong tao ban do deu phai khoi tao kho");

        // Chi soi phan MA: chu thich o day co trich nguyen van cac doan can kiem tra.
        final String khoiTao = boChuThich(doc(F_KHOI_TAO));
        final String offline = boChuThich(doc(F_OFFLINE));

        Kiem.dung("doc duoc " + F_KHOI_TAO, khoiTao.length() > 0);
        Kiem.dung("doc duoc " + F_OFFLINE, offline.length() > 0);

        Kiem.dung("MapKhoiTao.init goi initVatTheDuoiDat()",
                khoiTao.contains("initVatTheDuoiDat()"));
        Kiem.dung("MapKhoiTao van chi do kho quai khi nregion == 0 (ban do co Region "
                        + "thi Region tu giu quai)",
                khoiTao.contains("nregion == 0") && khoiTao.contains("initKhoQuaiVaNguoi()"));

        // OfflineMap KHONG duoc tu do tay gi nua: mot noi giu trach nhiem thi moi khong
        // lech duoc. Chinh cho do tay thieu la nguon goc ca hai loi.
        Kiem.sai("OfflineMap khong con tu goi initAllPlayer()",
                offline.contains("initAllPlayer()"));
        Kiem.sai("OfflineMap khong con tu do tay allPlayerMessages",
                offline.contains("allPlayerMessages.add("));

        // initKhoQuaiVaNguoi phai cham du SAU kho, ke ca ba kho khong public.
        final String maMapDayDu = boChuThich(doc(F_MAP));
        final int viTriHam = maMapDayDu.indexOf("public void initKhoQuaiVaNguoi()");
        final String thanKhoQuai = viTriHam < 0 ? ""
                : maMapDayDu.substring(viTriHam, Math.min(maMapDayDu.length(), viTriHam + 900));
        for (final String kho : new String[] {"initAllPlayer", "initAllMonsTer",
                "allPlayerMessages", "temp_monsters", "tempMonster", "tempRemoveMonster"}) {
            Kiem.dung("initKhoQuaiVaNguoi co do day " + kho, thanKhoQuai.contains(kho));
        }

        // Ham dung Map(int id) mo mot thread chay run() -> update(). Thread do bat dau
        // chay ngay khi ham dung ket thuc, tuc la TRUOC khi than ham dung cua lop con
        // chay xong. Nen khoi tao phai nam trong chinh Map(int id) VA truoc loi start().
        final String maMap = boChuThich(doc(F_MAP));
        final int viTriHamDung = maMap.indexOf("public Map(int id) {");
        final String thanHamDung = viTriHamDung < 0 ? ""
                : maMap.substring(viTriHamDung,
                        Math.min(maMap.length(), viTriHamDung + 800));
        final int viTriStart = thanHamDung.indexOf("new Thread(this).start()");
        for (final String ham : new String[] {"initVatTheDuoiDat()", "initKhoQuaiVaNguoi()"}) {
            final int viTri = thanHamDung.indexOf(ham);
            Kiem.dung("Map(int id) tu goi " + ham, viTri >= 0);
            Kiem.dung("... va goi TRUOC new Thread(this).start()",
                    viTri >= 0 && viTriStart >= 0 && viTri < viTriStart);
        }

        // Khong con hai noi cung do tay nam kho nay nua.
        Kiem.sai("MapKhoiTao khong con tu do tay potions",
                khoiTao.contains("potions.add(new Vector())"));
        Kiem.sai("OfflineMap khong con tu do tay ALL_EFFECT_INMAP",
                offline.contains("ALL_EFFECT_INMAP.add(new Vector())"));
    }

    // -----------------------------------------------------------------------
    //  Hai bo canh cu lech mot don vi
    // -----------------------------------------------------------------------
    private static void kiemLechMotDonVi() {
        Kiem.nhom("Bo canh chi so khong con lech mot don vi");

        final String ma = boChuThich(doc(F_MAP));

        // `country > size()` cho country == size() lot qua roi nem ngoai le ngay dong sau.
        Kiem.sai("khong con dieu kien 'country > this.potions.size()'",
                ma.contains("country > this.potions.size()"));
        Kiem.sai("khong con dieu kien 'country > this.itemquests.size()'",
                ma.contains("country > this.itemquests.size()"));

        Kiem.dung("getPotion canh bang >= tren dung kho cua no",
                ma.contains("country >= this.potions.size()"));
        Kiem.dung("getItemQuest canh tren itemquests chu khong phai potions",
                ma.contains("country >= this.itemquests.size()"));
    }

    /**
     * Bo moi chu thich khoi ma nguon.
     *
     * BAT BUOC PHAI CO: cac chu thich o day co trich nguyen van dieu kien cu
     * ("country > this.potions.size()") va ten ham ("new Thread(this).start()") de giai
     * thich vi sao chung sai. Doi chieu tren ca tep thi bai kiem se khop trung chinh
     * chu thich cua no va bao dat/hong sai hoan toan.
     */
    private static String boChuThich(final String ma) {
        final StringBuilder sb = new StringBuilder(ma.length());
        boolean trongKhoi = false;
        for (final String dong : ma.split("\n", -1)) {
            String t = dong;
            if (trongKhoi) {
                final int het = t.indexOf("*/");
                if (het < 0) {
                    continue;
                }
                t = t.substring(het + 2);
                trongKhoi = false;
            }
            final int mo = t.indexOf("/*");
            if (mo >= 0) {
                final int het = t.indexOf("*/", mo + 2);
                if (het >= 0) {
                    t = t.substring(0, mo) + t.substring(het + 2);
                } else {
                    t = t.substring(0, mo);
                    trongKhoi = true;
                }
            }
            final int hai = t.indexOf("//");
            if (hai >= 0) {
                t = t.substring(0, hai);
            }
            sb.append(t).append('\n');
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder((int) f.length() + 16);
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
        } catch (final Exception ex) {
            return "";
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }

    private KiemBanDoOffline() {
    }
}
