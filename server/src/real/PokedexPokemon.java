package real;

import com.htbb.pokemon.DaTienHoa;
import com.htbb.pokemon.LoaiPokemon;
import com.htbb.pokemon.Pokedex;
import data.Database;
import data.DatabaseGiftCode;
import data.DatabasePokedex;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pokedex + NPC Pokedex (viec G7.D2/D3). Logic thuan o com.htbb.pokemon.Pokedex (co test),
 * CSDL o data.DatabasePokedex; lop nay noi vao Char/Monster/menu.
 *
 * Diem goi:
 *   - CharChiSo.attackDam  -> {@link #khiDanh}  (gap loai)
 *   - Monster.onDropItem   -> {@link #khiHaQuai} (ha loai)
 *   - Database.saveChar / saveCharAuto -> {@link #luu} (xa lo xuong CSDL)
 *   - NPC "doi thuong" (-90) muc 1 "Pokédex" -> {@link #moMenu}; npc ao {@link #NPC_ID}
 *     xu ly trong MapDoMenu -> {@link #doMenu}.
 *
 */
public final class PokedexPokemon {

    /**
     * Npc ao cho menu Pokedex. Da grep src/ (16/10/2026): -520/-521 (moc nap), -522..-524
     * (KhoXuLuong), -525 (BaiLuyenTap) dang dung; -526 trong.
     */
    public static final int NPC_ID = -526;

    /** Vi tri muc "Pokédex" trong menu NPC doi thuong (-90) - muc goc duy nhat la 0. */
    public static final int VI_TRI_MUC_NPC_DOI_THUONG = 1;

    public static final String TEN_MUC = "Pokédex";

    static final int MENU_GOC = 0;
    static final int MENU_MOC = 1;
    static final int MENU_EEVEE = 2;

    /** charDBID -> Pokedex trong bo nho (nguon dung khi dang chay; CSDL chi la ban luu). */
    private static final ConcurrentHashMap<Integer, Pokedex> CACHE = new ConcurrentHashMap<>();

    private PokedexPokemon() {
    }

    /** Pokedex cua nhan vat (nap tu CSDL lan dau), null neu bot / loi CSDL. */
    static Pokedex lay(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return null;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        Pokedex d = CACHE.get(k);
        if (d != null) {
            return d;
        }
        d = DatabasePokedex.nap(p.charDBID);
        if (d == null) {
            return null; // loi CSDL: khong ghi gi, tranh ghi de so_ha bang so nho hon
        }
        final Pokedex cu = CACHE.putIfAbsent(k, d);
        return cu != null ? cu : d;
    }

    static int dexQuai(final LiveActor a) {
        if (!(a instanceof Monster)) {
            return 0;
        }
        final MonsterTemplate t = Map.monsterTemplates.get(Integer.valueOf(((Monster) a).idTemplate));
        return t != null ? t.dex : 0;
    }

    private static long bayGio() {
        return System.currentTimeMillis() / 1000L;
    }

    /** Nhan vat danh trung mot quai: ghi "da gap". */
    public static void khiDanh(final Char p, final LiveActor actor) {
        try {
            final int dex = dexQuai(actor);
            if (dex <= 0) {
                return;
            }
            final Pokedex d = lay(p);
            if (d != null) {
                d.ghiGap(dex, bayGio());
            }
            NhiemVuEngine.suKien(p, "pokedex_gap", String.valueOf(dex), 1); // GD2b (tat co: khong lam gi)
            BatPokemonGame.khiDanh(p, actor); // [bat-pokemon] ai vua giao chien con nay (moi nem bong duoc)
        } catch (final Exception e) {
            System.out.println("PokedexPokemon.khiDanh: " + e);
        }
    }

    /** Quai chet, {@code p} nhan do: ghi "da ha". */
    public static void khiHaQuai(final Char p, final Monster quai) {
        try {
            final int dex = dexQuai(quai);
            if (dex <= 0) {
                return;
            }
            final Pokedex d = lay(p);
            if (d != null) {
                d.ghiHa(dex, bayGio());
            }
        } catch (final Exception e) {
            System.out.println("PokedexPokemon.khiHaQuai: " + e);
        }
    }

    /** Goi tu Database.saveChar/saveCharAuto: xa cac dong da doi (khong nap neu chua co). */
    public static void luu(final Char p) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        final Pokedex d = CACHE.get(Integer.valueOf(p.charDBID));
        if (d != null) {
            DatabasePokedex.luu(p.charDBID, d);
        }
    }

    // ------------------------------------------------------------------ du lieu hien thi

    /** Dex cua moi template quai (de dem tong so loai). */
    static int[] dexTemplate() {
        final List<Integer> ds = new ArrayList<>();
        for (final MonsterTemplate t : Map.monsterTemplates.values()) {
            if (t != null) {
                ds.add(Integer.valueOf(t.dex));
            }
        }
        final int[] kq = new int[ds.size()];
        for (int i = 0; i < kq.length; i++) {
            kq[i] = ds.get(i).intValue();
        }
        return kq;
    }

    static String tenLoai(final int dex) {
        String ten = LoaiPokemon.ten(dex);
        if (ten == null || ten.isEmpty()) {
            for (final MonsterTemplate t : Map.monsterTemplates.values()) {
                if (t != null && t.dex == dex && t.name != null && !t.name.isEmpty()) {
                    ten = t.name;
                    break;
                }
            }
        }
        return (ten == null || ten.isEmpty()) ? ("#" + dex) : ten;
    }

    // ------------------------------------------------------------------ menu NPC

    /** NPC doi thuong (-90), muc "Pokédex": mo menu goc. */
    public static void moMenu(final Char p) {
        if (p == null) {
            return;
        }
        p.sendMessage(MessageCreator.createMsgMenu(
                new String[]{"Xem Pokédex", "Thưởng mốc Pokédex", "Tiến hoá Eevee", BatPokemonGame.TEN_MUC_POKEDEX}, NPC_ID, MENU_GOC));
    }

    /** MapDoMenu: idNpc == NPC_ID. */
    public static void doMenu(final Char p, final int idMenu, final int idOption) {
        if (p == null) {
            return;
        }
        if (idMenu == MENU_GOC) {
            if (idOption == 0) {
                xem(p);
            } else if (idOption == 1) {
                moMenuMoc(p);
            } else if (idOption == 2) {
                moMenuEevee(p);
            } else if (idOption == 3) {
                BatPokemonGame.moHop(p, 0); // [bat-pokemon]
            }
        } else if (idMenu == MENU_MOC) {
            nhanMoc(p, idOption);
        } else if (idMenu == MENU_EEVEE) {
            chonNhanhEevee(p, idOption);
        }
    }

    private static void baoLoi(final Char p, final String s) {
        p.sendMessage(MessageCreator.createServerAlertMessage(s, ""));
    }

    static void xem(final Char p) {
        final Pokedex d = lay(p);
        if (d == null) {
            baoLoi(p, "Pokédex tạm thời không đọc được, thử lại sau.");
            return;
        }
        final int tong = Pokedex.tongLoai(dexTemplate());
        final StringBuilder sb = new StringBuilder();
        sb.append("Pokédex\n");
        sb.append("Đã gặp: ").append(d.soLoaiDaGap()).append('/').append(tong).append(" loài\n");
        sb.append("Đã hạ: ").append(d.soLoaiDaHa()).append('/').append(tong).append(" loài\n");
        sb.append("Đã bắt: ").append(d.soLoaiDaBat()).append('/').append(tong).append(" loài\n"); // [bat-pokemon]
        final List<int[]> top = d.top(5);
        if (!top.isEmpty()) {
            sb.append("Hạ nhiều nhất:\n");
            for (final int[] t : top) {
                sb.append("- ").append(tenLoai(t[0])).append(" (#").append(t[0]).append("): ").append(t[1]).append('\n');
            }
        }
        baoLoi(p, sb.toString().trim());
    }

    static void moMenuMoc(final Char p) {
        final Pokedex d = lay(p);
        if (d == null) {
            baoLoi(p, "Pokédex tạm thời không đọc được, thử lại sau.");
            return;
        }
        final int daHa = d.soLoaiDaHa();
        final String[] menu = new String[Pokedex.MOC.length];
        for (int i = 0; i < menu.length; i++) {
            final int moc = Pokedex.MOC[i];
            menu[i] = "Mốc " + moc + " loài" + (d.daNhanMoc(moc) ? " (đã nhận)"
                    : (daHa >= moc ? " (nhận ngay)" : " (" + daHa + "/" + moc + ")"));
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, NPC_ID, MENU_MOC));
    }

    /** Nhan thuong moc Pokedex.MOC[idOption]. Kiem tra lai TAT CA dieu kien o day (muc 11). */
    static void nhanMoc(final Char p, final int idOption) {
        if (idOption < 0 || idOption >= Pokedex.MOC.length) {
            return;
        }
        final Pokedex d = lay(p);
        if (d == null) {
            baoLoi(p, "Pokédex tạm thời không đọc được, thử lại sau.");
            return;
        }
        final int moc = Pokedex.MOC[idOption];
        final int daHa = d.soLoaiDaHa();
        if (d.daNhanMoc(moc)) {
            baoLoi(p, "Bạn đã nhận thưởng mốc " + moc + " rồi.");
            return;
        }
        if (!Pokedex.nhanDuoc(moc, daHa, false)) {
            baoLoi(p, "Cần hạ " + moc + " loài Pokémon (hiện " + daHa + ").");
            return;
        }
        if ((Pokedex.canOTrong(moc) || com.htbb.pokemon.LauDai.bat()) && p.countSlotInventory() < 3) {
            baoLoi(p, "Hành trang đã đầy.");
            return;
        }
        // Danh dau TRUOC (INSERT IGNORE), chi phat khi chinh lan nay them duoc dong (muc 5).
        if (!DatabasePokedex.danhDauMoc(p.charDBID, moc)) {
            d.danhDauMoc(moc);
            baoLoi(p, "Bạn đã nhận thưởng mốc " + moc + " rồi.");
            return;
        }
        d.danhDauMoc(moc);
        String thuong = Pokedex.phanThuong(moc, Map.r.nextInt(DaTienHoa.ID_ITEM.length));
        if (com.htbb.pokemon.LauDai.bat()) {
            thuong = Pokedex.doiThuongLauDai(thuong, moc); // keo EXP cu 10..12 ngung dung -> Keo hiem
        }
        try {
            DatabaseGiftCode.applyItemString(p, thuong);
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception e) {
            System.out.println("PokedexPokemon.nhanMoc: " + e);
        }
        baoLoi(p, "Nhận thưởng mốc Pokédex " + moc + " loài thành công!");
        try {
            Database.instance.saveOrtherLog("", p.charname, "pokedex moc " + moc + ": " + thuong, "pokedex");
        } catch (final Exception ignored) {
        }
    }

    static void moMenuEevee(final Char p) {
        final int cap = p.lvDetail != null ? p.lvDetail.lv : 0;
        if (!DaTienHoa.duocDung(p.pokeDex, cap)) {
            baoLoi(p, DaTienHoa.THONG_BAO_KHONG_DUNG_DUOC);
            return;
        }
        final String[] menu = new String[DaTienHoa.ID_ITEM.length];
        for (int i = 0; i < menu.length; i++) {
            menu[i] = tenLoai(DaTienHoa.DEX[i]) + " (" + DaTienHoa.TEN[i]
                    + (timDa(p, DaTienHoa.ID_ITEM[i]) != null ? ")" : " - chưa có)");
        }
        p.sendMessage(MessageCreator.createMsgMenu(menu, NPC_ID, MENU_EEVEE));
    }

    static Item timDa(final Char p, final int idItem) {
        if (p.iItems == null) {
            return null;
        }
        synchronized (p.iItems) {
            for (final Item it : p.iItems) {
                if (it != null && it.getTemplate() != null && it.getTemplate().id == idItem) {
                    return it;
                }
            }
        }
        return null;
    }

    /** Chon nhanh Eevee thu idOption: can co da tuong ung, dung lai TienHoaPokemon.dungDaTienHoa. */
    static void chonNhanhEevee(final Char p, final int idOption) {
        if (idOption < 0 || idOption >= DaTienHoa.ID_ITEM.length) {
            return;
        }
        final int cap = p.lvDetail != null ? p.lvDetail.lv : 0;
        if (!DaTienHoa.duocDung(p.pokeDex, cap)) {
            baoLoi(p, DaTienHoa.THONG_BAO_KHONG_DUNG_DUOC);
            return;
        }
        final Item da = timDa(p, DaTienHoa.ID_ITEM[idOption]);
        if (da == null) {
            baoLoi(p, "Cần có " + DaTienHoa.TEN[idOption] + " trong hành trang.");
            return;
        }
        // Kiem cap/loai lan nua + tru da TRUOC roi moi tien hoa (iItems.remove nguyen tu).
        TienHoaPokemon.dungDaTienHoa(p, da);
    }
}
