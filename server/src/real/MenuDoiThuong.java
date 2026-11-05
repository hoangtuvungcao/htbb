package real;

import com.htbb.pokemon.DuongMenu;
import com.htbb.pokemon.HeSinhThai;
import com.htbb.pokemon.LauDai;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Menu NPC -90 "Quầy đổi thưởng" (nhanh sua-duong-gym). Truoc day cac muc noi vao CUOI theo vi tri co dinh (Xem doi
 * thuong 0, Pokédex 1, So tay 2, He sinh thai 3, Nhiem vu cua toi cuoi) - client J2ME hien 3 muc / trang nen "Nhiệm vụ của
 * tôi" o trang 2, nguoi choi lam buoc ha Gym khong tim thay duong. Nay xep theo muc dung:
 * <ol>
 *   <li>"Thách đấu Gym Pewter (Golem)" - CHI khi dang lam buoc ha_gym (cua ai): hop 95 xac nhan (Gym danh o dau) roi
 *       GymLauDai.thachDauStt, cung cua voi Sổ tay huấn luyện → Thách đấu Gym.</li>
 *   <li>Nhiệm vụ của tôi, Sổ tay huấn luyện (sv.lau_dai)</li>
 *   <li>Điểm hiện tại … - Xem đổi thưởng, Pokédex, Hệ sinh thái (neu bat)</li>
 * </ol>
 * Lua chon theo KHOA cua danh sach da gui cho nhan vat (doi thu tu khong chon nham muc); chua mo menu thi dung danh sach
 * tinh lai tu trang thai hien tai.
 */
public final class MenuDoiThuong {

    public static final int NPC = -90;
    static final String K_GYM = "gym", K_NHIEM_VU = "nhiem_vu", K_SO_TAY = "so_tay", K_DIEM = "diem", K_POKEDEX = "pokedex",
            K_HE_SINH_THAI = "he_sinh_thai";

    private static final ConcurrentHashMap<Integer, String[]> DA_GUI = new ConcurrentHashMap<Integer, String[]>();

    private MenuDoiThuong() {
    }

    /** {khoa[], nhan[]} theo thu tu hien. {@code bossGym}: ten boss Gym cua buoc dang lam, null = khong. */
    public static String[][] xep(final boolean lauDai, final boolean heSinhThai, final String bossGym, final int diem) {
        final List<String> k = new ArrayList<String>();
        final List<String> n = new ArrayList<String>();
        if (lauDai && bossGym != null && !bossGym.isEmpty()) {
            k.add(K_GYM);
            n.add(DuongMenu.nhanTatGym(bossGym));
        }
        if (lauDai) {
            k.add(K_NHIEM_VU);
            n.add(SoTayLauDai.TEN_NHIEM_VU);
            k.add(K_SO_TAY);
            n.add(SoTayLauDai.TEN_MUC);
        }
        k.add(K_DIEM);
        n.add("Điểm hiện tại: " + diem + " điểm - " + DuongMenu.DIEM);
        k.add(K_POKEDEX);
        n.add(PokedexPokemon.TEN_MUC);
        if (heSinhThai) { // he sinh thai: muc cuoi menu, npc ao HeSinhThaiGame.NPC_ID (-528)
            k.add(K_HE_SINH_THAI);
            n.add(com.htbb.pokemon.HeSinhThaiGame.TEN_MUC);
        }
        return new String[][]{k.toArray(new String[0]), n.toArray(new String[0])};
    }

    static String[][] cua(final Char p) {
        final boolean ld = LauDai.bat();
        return xep(ld, HeSinhThai.bat(), ld ? GymLauDai.tenBossBuoc(p) : null, p.diemDoiThuong);
    }

    /** Mo menu (RequestNPCInfoHandler case -90). */
    public static void mo(final Char p) {
        if (p == null) {
            return;
        }
        final String[][] m = cua(p);
        DA_GUI.put(Integer.valueOf(p.charDBID), m[0]);
        p.sendMessage(MessageCreator.createMsgMenu(m[1], NPC, 0));
    }

    /** Khoa muc thu {@code op} cua danh sach da gui (chua gui: tinh lai); null = ngoai danh sach. */
    static String khoa(final Char p, final int op) {
        String[] k = DA_GUI.get(Integer.valueOf(p.charDBID));
        if (k == null) {
            k = cua(p)[0];
        }
        return op >= 0 && op < k.length ? k[op] : null;
    }

    /** MapDoMenu: NPC -90, menu 0, muc {@code op}. Muc la / ngoai danh sach -> bang diem doi thuong (nhu cu). */
    public static void chon(final Char p, final int op, final Map map) {
        if (p == null) {
            return;
        }
        final String k = khoa(p, op);
        if (K_GYM.equals(k)) {
            final Object[] g = GymLauDai.buocGym(p);
            if (g != null) {
                GymLauDai.hoi(p, g, "");
            } else {
                SoTayLauDai.bao(p, "Bước hạ Gym đã xong. Gym khác: " + DuongMenu.duong(DuongMenu.QUAY, DuongMenu.SO_TAY, DuongMenu.GYM) + ".");
            }
        } else if (K_NHIEM_VU.equals(k) && LauDai.bat()) {
            NhiemVuEngine.moMenu(p);
        } else if (K_SO_TAY.equals(k) && LauDai.bat()) {
            SoTayLauDai.moMenu(p);
        } else if (K_POKEDEX.equals(k)) {
            PokedexPokemon.moMenu(p);
        } else if (K_HE_SINH_THAI.equals(k) && HeSinhThai.bat()) {
            com.htbb.pokemon.HeSinhThaiMenu.moMenu(p);
        } else if (map != null) {
            map.showMenuDiemDoiThuong(p);
        }
    }

    public static void khiThoat(final Char p) {
        if (p != null) {
            DA_GUI.remove(Integer.valueOf(p.charDBID));
            GymLauDai.khiThoat(p);
        }
    }

    // ------------------------------------------------------------------ cay menu that (test/KiemDuongMenu)

    /**
     * Cay nhan menu that cho DuongMenu.kiem, dung tu chinh cac mang menu: goc "Quầy đổi thưởng" (muc cua {@link #xep} khi
     * moi co deu bat + muc tat Gym cua tung Gym), So tay (SoTayLauDai.MUC_GOC + muc huong dan tan thu), "Nhiệm vụ của
     * tôi" (NhiemVuEngine.MUC_GOC), "Thách đấu Gym" (dong menu cua tung Gym trong {@code bossGym}: {ten boss, cap}).
     * Menu con khac danh dau mo (khong kiem buoc sau).
     */
    public static DuongMenu.Nut cay(final List<Object[]> gym, final List<String> mucHuongDan) {
        final DuongMenu.Nut quay = new DuongMenu.Nut(DuongMenu.QUAY);
        final DuongMenu.Nut nv = new DuongMenu.Nut(DuongMenu.NHIEM_VU);
        for (final String m : NhiemVuEngine.MUC_GOC) {
            nv.them(m).mo = true;
        }
        final DuongMenu.Nut dsGym = new DuongMenu.Nut(DuongMenu.GYM);
        for (final Object[] g : gym) {
            dsGym.them(DuongMenu.nhanGym(String.valueOf(g[0]), ((Number) g[1]).intValue(), false)).mo = true;
        }
        final DuongMenu.Nut soTay = new DuongMenu.Nut(DuongMenu.SO_TAY);
        for (final String m : SoTayLauDai.MUC_GOC) {
            if (DuongMenu.GYM.equals(m)) {
                soTay.them(dsGym);
            } else if (DuongMenu.NHIEM_VU.equals(m)) {
                soTay.them(nv);
            } else {
                soTay.them(m).mo = true;
            }
        }
        soTay.them(com.htbb.pokemon.Pk3v3.TEN).mo = true; // SoTayLauDai.MUC_PK3V3 (sau Về làng, truoc huong dan)
        for (final String m : mucHuongDan) {
            soTay.them(m).mo = true;
        }
        final String[] nhan = xep(true, true, null, 0)[1];
        final String[] khoa = xep(true, true, null, 0)[0];
        for (final Object[] g : gym) {
            quay.them(DuongMenu.nhanTatGym(String.valueOf(g[0]))).mo = true;
        }
        for (int i = 0; i < khoa.length; i++) {
            if (K_NHIEM_VU.equals(khoa[i])) {
                quay.them(nv);
            } else if (K_SO_TAY.equals(khoa[i])) {
                quay.them(soTay);
            } else if (K_DIEM.equals(khoa[i])) {
                quay.them(DuongMenu.DIEM).mo = true; // "Điểm hiện tại: N điểm - Xem đổi thưởng"
            } else {
                quay.them(nhan[i]).mo = true;
            }
        }
        return quay;
    }
}
