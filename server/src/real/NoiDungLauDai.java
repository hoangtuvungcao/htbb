package real;

import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LauDai;
import data.Database;
import data.DatabaseGiftCode;
import data.DatabaseLauDai;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Noi bang noi dung cua doi noi dung (tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_noi_dung.sql, data_noi_dung_*) vao game (GD2b,
 * docs/thiet-ke-game.md, 7, 9.5-9.6). Nap luc khoi dong (TeamServer, bat co, sau template).
 *
 *   - Bang roi (data_noi_dung_roi): nguon dai_cap / alpha (theo dai cap quai), vat pham type 28
 *     (item:id) va ngoc (ngoc:id); do co chi so trong bang KHONG tao o day (Item tao thang tu template
 *     se 0 chi so - xem NOI_DUNG 9.8) - van rot qua Map.dropItem cu.
 *   - Alpha (data_noi_dung_quai_tinh_anh): 1/50 lan hoi sinh, HP x4, tan cong x1,3, roi x3, chac 1 da;
 *     thong bao trong ban do.
 *   - Nam 2 (ngay >= 365): quai cap >= 84 hoi sinh co the thanh loai Hoenn cung dai cap
 *     (data_noi_dung_quai_dai_cap.template_nam2, template 139..174) - thay cho ban do Hoenn chua co.
 *   - Boss the gioi: template theo tuan ISO (data_noi_dung_boss_the_gioi, cot template_nam2 tu ngay 365).
 */
public final class NoiDungLauDai {

    /** {nguon, khoa nguon, loai (item|ngoc), id, ppm, slMin, slMax, khoa(1)}. */
    static volatile List<Object[]> ROI = new ArrayList<Object[]>();
    /** id template goc -> {hp, tan cong, nhan roi}. */
    static volatile HashMap<Integer, int[]> ALPHA = new HashMap<Integer, int[]>();
    /** dai cap (vd 86) -> template nam 2 cua dai. */
    static volatile HashMap<Integer, int[]> NAM2 = new HashMap<Integer, int[]>();
    /** tuan -> {template nam 1, template nam 2}. */
    static volatile HashMap<Integer, int[]> BOSS_TUAN = new HashMap<Integer, int[]>();

    private static final WeakHashMap<Monster, Boolean> LA_ALPHA = new WeakHashMap<Monster, Boolean>();
    private static final WeakHashMap<Monster, Integer> GOC_NAM2 = new WeakHashMap<Monster, Integer>();
    public static final int ALPHA_MAU = 50;
    public static final int NGAY_NAM_2 = 365;

    private NoiDungLauDai() {
    }

    public static void sauKhiNapTemplate() {
        try {
            NangCapLauDaiGame.napNguyenLieuPhu();
            ROI = DatabaseLauDai.napRoiNoiDung();
            ALPHA = DatabaseLauDai.napAlpha();
            NAM2 = DatabaseLauDai.napNam2();
            BOSS_TUAN = DatabaseLauDai.napBossTuan();
            GymLauDai.nap();
            ChieuLauDai.apDung();
            NhiemVuEngine.nap();
            DanhHieuGame.nap(); // data_danh_hieu (mau danh hieu + dieu kien)
            LuatPkGame.nap(); // thi tran an toan (data_map_ten.loai = thi_tran)
            BanHuongGame.nap(); // 0.9.8 ngam tay PvP: thong so ban cua chieu (data_chieu_ban)
            SuKienVatPham.nap(); // vat pham that cua tung su kien (data_su_kien_vat_pham)
            XepHangPvpGame.batDau(); // dau xep hang: lich su 24 gio + bo ghep 2 giay
            CuaHangLauDai.nap(); // cua hang & doi vat pham (So tay)
            moRongVatPhamNhiemVu();
            System.out.println("[LAU DAI] noi dung: " + ROI.size() + " dong roi, " + ALPHA.size() + " alpha, "
                    + NAM2.size() + " dai nam 2, " + BOSS_TUAN.size() + " tuan boss");
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI nap noi dung: " + e);
        }
    }

    /**
     * Vat pham nhiem vu moi id 10..50 (data_vat_pham_nhiem_vu, NHIEM_VU_THIET_KE 5.2.10): NOI DAI
     * mang tinh ItemQuest.NAME_ITEM_QUEST / ICON_IMAGE cua lop trong jar (khong de lop - chu ky giu
     * nguyen). Goi TRUOC khi nhan nguoi choi (Char.itemquest cap phat theo do dai mang). Icon dung lai
     * icon 0..9 cu (map/iconquest).
     */
    static void moRongVatPhamNhiemVu() {
        try {
            final java.util.TreeMap<Integer, String> moi = DatabaseLauDai.napVatPhamNhiemVu();
            if (moi.isEmpty()) {
                return;
            }
            final int dai = Math.max(data.ItemQuest.NAME_ITEM_QUEST.length, moi.lastKey().intValue() + 1);
            final String[] ten = java.util.Arrays.copyOf(data.ItemQuest.NAME_ITEM_QUEST, dai);
            final byte[] icon = java.util.Arrays.copyOf(data.ItemQuest.ICON_IMAGE, dai);
            for (int i = 0; i < dai; i++) {
                if (ten[i] == null) {
                    ten[i] = "";
                }
            }
            for (final java.util.Map.Entry<Integer, String> e : moi.entrySet()) {
                final int i = e.getKey().intValue();
                if (i >= data.ItemQuest.NAME_ITEM_QUEST.length) {
                    ten[i] = e.getValue();
                    icon[i] = (byte) (i % 10); // icon cu 0..9
                }
            }
            data.ItemQuest.NAME_ITEM_QUEST = ten;
            data.ItemQuest.ICON_IMAGE = icon;
            System.out.println("[LAU DAI] vat pham nhiem vu: " + ten.length + " ten");
        } catch (final Exception e) {
            System.out.println("[LAU DAI] LOI mo rong ItemQuest: " + e);
        }
    }

    // ------------------------------------------------------------------ hoi sinh (alpha / nam 2)

    /** Monster.update hoi sinh (SAU he sinh thai, TRUOC dat hp). */
    public static void khiHoiSinh(final Monster m) {
        if (!LauDai.bat() || m == null || m.isBoss || m.map == null || m.isMaterialMons() || m.isMonsterVantieu()) {
            return;
        }
        try {
            // tra lai loai goc neu lan truoc da doi sang Hoenn
            final Integer goc = GOC_NAM2.remove(m);
            if (goc != null && goc.intValue() != m.idTemplate) {
                final MonsterTemplate t = Map.monsterTemplates.get(goc);
                if (t != null) {
                    apTemplate(m, t);
                }
            }
            if (LauDai.ngayMoNoiDung() >= NGAY_NAM_2 && m.level >= 84 && Map.random(3) == 0) {
                final int[] ds = NAM2.get(Integer.valueOf(daiCap(m.level + 3)));
                if (ds != null && ds.length > 0) {
                    final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(ds[Map.random(ds.length)]));
                    if (t != null) {
                        GOC_NAM2.put(m, Integer.valueOf(m.idTemplate));
                        apTemplate(m, t);
                    }
                }
            }
            final MonsterTemplate t = m.getMonsterTemplate();
            m.maxhp = t.maxhp;
            m.attack = t.attack;
            LA_ALPHA.remove(m);
            final int[] a = ALPHA.get(Integer.valueOf(m.idTemplate));
            if (a != null && Map.random(ALPHA_MAU) == 0) {
                m.maxhp = a[0] > 0 ? a[0] : t.maxhp * 4;
                m.attack = a[1] > 0 ? a[1] : t.attack * 13 / 10;
                LA_ALPHA.put(m, Boolean.TRUE);
                baoAlpha(m);
            }
        } catch (final Exception e) {
            System.out.println("NoiDungLauDai.khiHoiSinh: " + e);
        }
    }

    static int daiCap(final int cap) {
        final int c = Math.max(1, Math.min(100, cap));
        return ((c - 1) / 5) * 5 + 1;
    }

    static void apTemplate(final Monster m, final MonsterTemplate t) {
        m.idTemplate = t.id;
        m.setType(t.type);
        m.level = t.level;
        m.maxhp = t.maxhp;
        m.maxxp = t.rcvXp;
        m.attack = t.attack;
        m.defend_physic = t.defend;
        m.accurate = t.accurate;
        m.dodge = t.dodge;
        m.dmove = t.dmove;
        m.hePokemon = t.he;
        m.he = (byte) HePokemon.nguHanhCu(t.he);
        m.he2 = t.he2;
    }

    public static boolean laAlpha(final Monster m) {
        synchronized (LA_ALPHA) {
            return Boolean.TRUE.equals(LA_ALPHA.get(m));
        }
    }

    static void baoAlpha(final Monster m) {
        try {
            final io.Message msg = MessageCreator.createServerAlertAutoOffMessage("Alpha " + m.getMonsterTemplate().name
                    + " (cấp " + m.level + ") vừa xuất hiện trong khu vực!");
            for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
                final Char c = CharManager.instance.vChars.elementAt(i);
                if (c != null && c.map == m.map && c.isBot == -1) {
                    c.sendMessage(msg);
                }
            }
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ roi theo bang

    /** RoiLauDai (ha quai): roi theo bang dai_cap (+ alpha: x nhan roi, chac 1-2 da). */
    static void roiBang(final Monster mt, final Char p, final int nangLuong) {
        if (nangLuong <= 0) {
            return;
        }
        final boolean alpha = laAlpha(mt);
        final String dai = daiCap(mt.level) + "-" + (daiCap(mt.level) + 4);
        int nhan = 1;
        if (alpha) {
            final int[] a = ALPHA.get(Integer.valueOf(mt.idTemplate));
            nhan = a != null && a[2] > 0 ? a[2] : 3;
        }
        for (final Object[] r : ROI) {
            final String nguon = (String) r[0];
            if (!dai.equals(r[1]) || !("dai".equals(nguon) || (alpha && "alpha".equals(nguon)))) {
                continue;
            }
            long ppm = ((Integer) r[4]).intValue();
            if ("dai".equals(nguon)) {
                ppm = ppm * nhan * nangLuong / 100;
            }
            if (Map.randomMillion() >= ppm) {
                continue;
            }
            final int sl = ((Integer) r[5]).intValue() + Map.random(Math.max(1, ((Integer) r[6]).intValue() - ((Integer) r[5]).intValue() + 1));
            if (!phatTrangBi(p, r, "alpha".equals(nguon))) {
                phat(p, (String) r[2], ((Integer) r[3]).intValue(), sl, ((Integer) r[7]).intValue() == 1);
            }
        }
    }

    /**
     * Dong roi la TRANG BI (item khong phai type 28): tao bang Map.dropItemByIdTemplate (chi so theo template, nhu
     * Gym / nhiem vu). Ngan sach: "dai" chi phat mon Map.dropItem cu KHONG rot (id ngoai Map.isIdTemplateItemDrop -
     * tranh roi hai lan cung mot mon), ppm cua bang (150 ppm/quai chia deu cac mon cua dai, x nang luong); "alpha"
     * = vu khi Hoan my (mau xanh, 2% tong). Day = bo qua. @return true neu dong la trang bi (da xu ly, ke ca bo qua).
     */
    static boolean phatTrangBi(final Char p, final Object[] r, final boolean alpha) {
        if (!"item".equals(r[2])) {
            return false;
        }
        final int id = ((Integer) r[3]).intValue();
        final ItemTemplates t = Map.itemTemplates.get(5).get(Integer.valueOf(id));
        if (t == null || t.type == 28) {
            return false;
        }
        if (!alpha && Map.isIdTemplateItemDrop(id) || p.isFullInventory()) {
            return true;
        }
        try {
            final Item it = Map.dropItemByIdTemplate(p.inCountry, id);
            if (it != null) {
                it.id = p.getIDItem();
                it.owner = p.charDBID;
                if (alpha) {
                    it.colorName = Item.COLOR_GREEN; // Hoan my (nhu GymLauDai.vuKhiHoanMy)
                }
                it.lock = ((Integer) r[7]).intValue() == 1 ? (byte) 1 : it.lock;
                p.iItems.add(it);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            }
        } catch (final Exception e) {
            System.out.println("NoiDungLauDai.phatTrangBi: " + e);
        }
        return true;
    }

    /** Phat 1 dong roi: ngoc -> doAddGemItem; item type 28 -> createItemSoLuong (khoa); khac: bo qua. */
    static void phat(final Char p, final String loai, final int id, final int sl, final boolean khoa) {
        try {
            if ("ngoc".equals(loai)) {
                p.doAddGemItem(id, sl, khoa);
                p.sendMessage(MessageCreator.createCharGemItem(p));
            } else if ("item".equals(loai)) {
                final ItemTemplates t = Map.itemTemplates.get(5).get(Integer.valueOf(id));
                if (t != null && t.type == 28 && !p.isFullInventory()) {
                    Char.createItemSoLuong(id, sl, p);
                }
            }
        } catch (final Exception e) {
            System.out.println("NoiDungLauDai.phat: " + e);
        }
    }

    // ------------------------------------------------------------------ boss the gioi: hop + tin vat; mo hop

    /** Nam 2 (ngay >= 365): tin vat dot kich nam 1 -> tin vat cua huyen thoai nam 2 cung tuan. */
    static final int[][] TIN_VAT_NAM2 = {{1040, 1041}, {1042, 1043}, {1044, 1045}, {1046, 1047}, {1048, 1049}};

    static int tinVatTheoNam(final int id) {
        if (LauDai.ngayHienTai() >= NGAY_NAM_2) {
            for (final int[] d : TIN_VAT_NAM2) {
                if (d[0] == id) {
                    return d[1];
                }
            }
        }
        return id;
    }

    /**
     * BossLauDai.khiChet: dong roi "boss_the_gioi" cua tuan ISO hien tai (khoa_nguon "tuan_WW:dong_gop>=0.5%" /
     * "tuan_WW:top10"). Bac 1: dong dong_gop (Hop tham gia + tin vat dot kich). Bac 2: Hop vang (thay Hop tham
     * gia) + tin vat. Chi vat pham type 28 / ngoc (phat). @return so dong da phat.
     */
    static int roiBossTheGioi(final Char p, final int bac) {
        final String tuan = String.format("tuan_%02d:", com.htbb.pokemon.SuKienLauDai.tuanIso(com.htbb.pokemon.DongHo.bay()));
        int n = 0;
        for (final Object[] r : ROI) {
            final String k = (String) r[1];
            if (!"boss_the_gioi".equals(r[0]) || !k.startsWith(tuan)) {
                continue;
            }
            final int id = ((Integer) r[3]).intValue();
            final boolean top = k.endsWith(":top10");
            if (top ? bac < 2 : (bac == 2 && id == ID_HOP_THAM_GIA)) {
                continue;
            }
            if (Map.randomMillion() >= ((Integer) r[4]).intValue()) {
                continue;
            }
            final int sl = ((Integer) r[5]).intValue() + Map.random(Math.max(1, ((Integer) r[6]).intValue() - ((Integer) r[5]).intValue() + 1));
            phat(p, (String) r[2], "item".equals(r[2]) ? tinVatTheoNam(id) : id, sl, true);
            n++;
        }
        return n;
    }

    static final int ID_HOP_THAM_GIA = 1050;

    /** VatPhamDungLauDai (nhom 'hop'): mo hop theo dong "mo_hop" khoa_nguon "item:<id>". @return mo ta phan thuong. */
    static String moHop(final Char p, final int idHop) {
        final StringBuilder b = new StringBuilder();
        for (final Object[] r : ROI) {
            if (!"mo_hop".equals(r[0]) || !("item:" + idHop).equals(r[1]) || Map.randomMillion() >= ((Integer) r[4]).intValue()) {
                continue;
            }
            final int id = ((Integer) r[3]).intValue();
            final int sl = ((Integer) r[5]).intValue() + Map.random(Math.max(1, ((Integer) r[6]).intValue() - ((Integer) r[5]).intValue() + 1));
            if ("thuoc".equals(r[2])) {
                if (id > 0 && id < p.potions.length) {
                    p.potions[id] += sl;
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                }
            } else {
                phat(p, (String) r[2], id, sl, true);
            }
            b.append(b.length() > 0 ? ", " : "").append(sl).append(' ').append(tenVatPham((String) r[2], id));
        }
        return b.toString();
    }

    static String tenVatPham(final String loai, final int id) {
        try {
            if ("ngoc".equals(loai)) {
                return Map.gemTemplate[id].name;
            }
            if ("thuoc".equals(loai)) {
                return Map.potionTemplates2.get(id).name;
            }
            return Map.itemTemplates.get(5).get(Integer.valueOf(id)).name;
        } catch (final Exception e) {
            return "vật phẩm";
        }
    }

    // ------------------------------------------------------------------ boss the gioi theo tuan

    /** Template boss the gioi tuan nay (nam 2 doi cot), -1 neu bang rong. */
    public static int bossTuanNay() {
        final int[] b = BOSS_TUAN.get(Integer.valueOf(com.htbb.pokemon.SuKienLauDai.tuanIso(com.htbb.pokemon.DongHo.bay())));
        if (b == null) {
            return -1;
        }
        return LauDai.ngayHienTai() >= NGAY_NAM_2 && b[1] > 0 ? b[1] : b[0];
    }

    /** Phat chuoi thuong (applyItemString) - dung chung Gym. */
    static void phatChuoi(final Char p, final String s, final String lyDo) {
        try {
            DatabaseGiftCode.applyItemString(p, s);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharGemItem(p));
            Database.instance.saveOrtherLog("", p.charname, lyDo + ": " + s, "laudai");
        } catch (final Exception e) {
            System.out.println("NoiDungLauDai.phatChuoi: " + e);
        }
    }
}
