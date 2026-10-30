package real;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * NPC TINH (loai 0..31, ve o client tu danh sach NPC cuoi tep cMap/&lt;id&gt;) theo ban do, doc tu chinh tep cMap
 * ma client nhan (goi 12) - cung mot nguon voi client, khong co bang toa do nao trong Java.
 *
 * Game goc chi cho mua ban / ren do / giu do TRONG NHA (OfflineMap): NPC tren ban do ngoai troi chi de chao, va
 * nhieu lenh dich vu (mua 24, ban 28, sua 72, luyen 71/75-77, kham -34..-36, khoa / nghien -68, kho 68-70...) chi
 * co than o OfflineMap (Map ngoai troi la ham rong, sua do con bi da vi "hack"). Ban do Pokemon dat Chansey,
 * Kecleon, Machamp, Bronzong, Persian, Kangaskhan... ngay tren duong thi tran (Hoenn 43..62, 0/70/80, 201, 281) nen
 * cac NPC do khong dung duoc. Lop nay cho biet nguoi choi co DUNG CANH mot NPC tinh co dich vu hay khong:
 *   - {@link #oNoiDichVu}: thay cho dieu kien cu "player.map.isMapOffline" o cac ham dich vu;
 *   - {@link #banDoDichVu}: MapXuLyLenh chuyen lenh dich vu sang than OfflineMap khi nguoi choi dung canh NPC
 *     phu hop tren ban do ngoai troi (khong canh NPC nao thi giu nguyen luat cu - van chan / da nhu truoc).
 */
public final class NpcTinhBanDo {

    /** Ban kinh (o) quanh NPC tinh ma nguoi choi duoc dung dich vu (client cho bam NPC trong ~3 o, cong do tre). */
    public static final int BAN_KINH_O = 7;

    // Nhom loai NPC theo dich vu (data_npc.type).
    public static final int[] NPC_Y_TA = {0};
    public static final int[] NPC_BAN_THUOC = {0, 1, 26};
    public static final int[] NPC_THO_REN = {2, 28};
    public static final int[] NPC_BAN_GIAP = {3, 27};
    public static final int[] NPC_DA_QUY = {6, 24, 29};
    public static final int[] NPC_GIU_DO = {9, 30};
    public static final int[] NPC_NGAN_HANG = {22, 31};
    /** Gian hang ky gui (Golem / Tauros, Map.npcSell): xem / rao ban / mua cua nguoi choi khac. */
    public static final int[] NPC_GIAN_HANG = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    /** Moi NPC co cua so mua / ban (goi 24 / 28): tap hoa, ren, giap, da quy, gian hang 11..20. */
    public static final int[] NPC_MUA_BAN = {0, 1, 2, 3, 6, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 24, 26, 27, 28, 29};

    private static final java.util.Map<Integer, List<int[]>> CACHE = new HashMap<Integer, List<int[]>>();

    private NpcTinhBanDo() {
    }

    /** Danh sach NPC tinh {x o, y o, loai} cua ban do nap {@code load} (rong neu khong co / loi doc). */
    public static List<int[]> cua(final int load) {
        synchronized (CACHE) {
            final List<int[]> c = CACHE.get(load);
            if (c != null) {
                return c;
            }
        }
        final List<int[]> kq = docTep("cMap/" + load);
        synchronized (CACHE) {
            CACHE.put(load, kq);
        }
        return kq;
    }

    /** Doc khoi NPC cuoi tep cMap (com.htbb.pokemon.DinhDangBanDo - cung dinh dang client doc). */
    static List<int[]> docTep(final String tep) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(tep));
            final byte[] b = new byte[in.available()];
            in.readFully(b);
            return docByte(b);
        } catch (final Exception e) {
            return Collections.emptyList();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }

    static List<int[]> docByte(final byte[] b) {
        try {
            final List<int[]> kq = new ArrayList<int[]>();
            for (final com.htbb.pokemon.DinhDangBanDo.Diem d : com.htbb.pokemon.DinhDangBanDo.doc(b).npc) {
                kq.add(new int[]{d.x, d.y, d.v});
            }
            return Collections.unmodifiableList(kq);
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    private static final java.util.Map<Integer, List<Integer>> BAN_DO_CO = new HashMap<Integer, List<Integer>>();

    /** Cac ban do (id tep cMap, tang dan, chi ban do ngoai troi dang chay tren server) co NPC tinh {@code loai}. */
    public static List<Integer> banDoCo(final int loai) {
        synchronized (BAN_DO_CO) {
            final List<Integer> c = BAN_DO_CO.get(loai);
            if (c != null) {
                return c;
            }
        }
        final List<Integer> kq = new ArrayList<Integer>();
        final java.io.File[] tep = new java.io.File("cMap").listFiles();
        if (tep != null) {
            for (final java.io.File f : tep) {
                if (!f.getName().matches("\\d+")) {
                    continue;
                }
                final int id = Integer.parseInt(f.getName());
                final Map m = RealController.mapList.get(id);
                if (m == null || m.isMapOffline) {
                    continue;
                }
                for (final int[] n : cua(id)) {
                    if (n[2] == loai) {
                        kq.add(id);
                        break;
                    }
                }
            }
        }
        Collections.sort(kq);
        final List<Integer> bat = Collections.unmodifiableList(kq);
        synchronized (BAN_DO_CO) {
            BAN_DO_CO.put(loai, bat);
        }
        return bat;
    }

    /** O dau tien cua NPC tinh {@code loai} tren ban do {@code load} ({x, y}), null neu khong co. */
    public static int[] oNpc(final int load, final int loai) {
        for (final int[] n : cua(load)) {
            if (n[2] == loai) {
                return new int[]{n[0], n[1]};
            }
        }
        return null;
    }

    /** Ban do nap (id tep cMap) cua nguoi choi: trong nha la id nha, ngoai troi la mapIDLoadMap (ban sao theo khu). */
    public static int banDoNap(final Char p) {
        if (p == null || p.map == null) {
            return -1;
        }
        return p.map.isMapOffline ? p.mapID : p.map.mapIDLoadMap;
    }

    /** Nguoi choi dung trong {@link #BAN_KINH_O} o quanh mot NPC tinh thuoc {@code loai} (trong nha: chi can nha co NPC). */
    public static boolean gan(final Char p, final int... loai) {
        final int load = banDoNap(p);
        if (load < 0) {
            return false;
        }
        final int ox = p.x / 16;
        final int oy = p.y / 16;
        for (final int[] n : cua(load)) {
            for (final int l : loai) {
                if (n[2] != l) {
                    continue;
                }
                if (p.map.isMapOffline || (Math.abs(n[0] - ox) <= BAN_KINH_O && Math.abs(n[1] - oy) <= BAN_KINH_O)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Thay "player.map.isMapOffline" o cac ham dich vu NPC: trong nha (luat cu) HOAC dung canh NPC dung loai. */
    public static boolean oNoiDichVu(final Char p, final int... loai) {
        if (p == null || p.map == null) {
            return false;
        }
        return p.map.isMapOffline || gan(p, loai);
    }

    /** Loai NPC can dung canh cho lenh mang {@code cmd}; null = lenh khong phai dich vu NPC tinh. */
    static int[] loaiChoLenh(final int cmd) {
        switch (cmd) {
            case 24: // mua o cua so NPC
            case 28: // ban cho NPC
                return NPC_MUA_BAN;
            case 74: // mua da quy / nguyen lieu
            case 78: // ban da quy / nguyen lieu
                return NPC_DA_QUY;
            case 92: // rao ban do o gian hang
            case 94: // xem nguoi ban o gian hang
            case 95: // mua do cua nguoi choi o gian hang
                return NPC_GIAN_HANG;
            case 68: // cat do vao kho
            case 69: // lay do tu kho
            case 70: // dong kho
                return NPC_GIU_DO;
            case 71: // luyen do
            case 72: // sua do
            case 75:
            case 76:
            case 77:
            case -34: // duc lo / kham
            case -35:
            case -36: // hop thanh ngoc
            case -68: // tach nguyen lieu, them dong, khoa do, nghien bot
            case -69: // luyen do tu dong
                return NPC_THO_REN;
            default:
                return null;
        }
    }

    /**
     * MapXuLyLenh: lenh dich vu NPC tinh tu ban do NGOAI TROI ma nguoi choi dung canh NPC phu hop -> chay bang than
     * OfflineMap (giong het trong nha). null = giu nguyen ban do hien tai (luat cu).
     */
    public static Map banDoDichVu(final Map map, final Char p, final int cmd) {
        if (map == null || map.isMapOffline) {
            return null;
        }
        final int[] loai = loaiChoLenh(cmd);
        if (loai == null || !gan(p, loai)) {
            return null;
        }
        final Object om = RealController.mapList.get(-1);
        return om instanceof OfflineMap ? (Map) om : null;
    }
    // ------------------------------------------------------------------ danh sach ban cua tung NPC (goi 24)

    /** Bronzong ban giap (3) ban do &lt; 200 - khop MessageCreator.createMsgSellArmoOfNPC. */
    static final int[] GIAP_3_THAP = {3, 20, 27, 46, 53, 72, 114, 122, 126, 134, 138, 146, 150, 158, 162, 170};
    /** Bronzong ban giap (3) ban do &gt; 200 (khong co NPC 3 nao o day, giu cho khop createMsgSellArmoOfNPC). */
    static final int[] GIAP_3_CAO = {21, 26, 47, 52, 73, 78, 123, 125, 135, 137, 147, 149, 159, 161, 171, 173, 467, 506};
    /** Bronzong ban giap Indigo (27). */
    static final int[] GIAP_27 = {21, 26, 47, 52, 73, 78, 123, 125, 135, 137, 147, 149, 159, 161, 171, 173, 467, 506, 569, 572};
    /** Machamp tho ren (2) ban do &lt; 200: do cam tay + the mua ban (675) + trang bi thu (createMsgNPCSellWeapone / RequestNPCInfoHandler). */
    static final int[] REN_2_THAP = {79, 113, 465, 466, 675, 675, 509, 568};
    /** Do cam tay cao (2 o ban do &gt; 200, 28). */
    static final int[] REN_CAO = {174, 213};
    /** NPC 24 (RequestNPCInfoHandler case 24). */
    static final int[] NPC_24 = {214, 263};

    /** Cac doan [tu, den] vat pham NPC loai {@code loai} ban o ban do {@code mapID} (rong = NPC khong ban vat pham loai 3). */
    public static int[] dsBan(final int loai, final int mapID) {
        switch (loai) {
            case 2:
                return mapID < 200 ? REN_2_THAP : mapID > 200 ? REN_CAO : new int[0];
            case 28:
                return REN_CAO;
            case 3:
                return mapID < 200 ? GIAP_3_THAP : mapID > 200 ? GIAP_3_CAO : new int[0];
            case 27:
                return GIAP_27;
            case 24:
                return NPC_24;
            default:
                return new int[0];
        }
    }

    static boolean trong(final int[] doan, final int id) {
        for (int k = 0; k + 1 < doan.length; k += 2) {
            if (id >= doan[k] && id <= doan[k + 1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Goi 24 (mua vat pham loai 3): vat pham {@code id} co trong danh sach ban cua mot NPC ma nguoi choi dang dung canh
     * (trong nha: NPC cua nha do; ngoai troi: trong {@link #BAN_KINH_O} o). Truoc 31/10/2026 may chu chi kiem danh
     * sach trang (MessageCreator.ALL_ID_ITEM_NPC_SELL) -> dung canh Machamp / Bronzong Pewter mua duoc giap Indigo
     * (467..506). Ban do / nha khong co danh sach NPC trong tep cMap: giu luat cu (chi danh sach trang).
     */
    public static boolean banTaiCho(final Char p, final int id) {
        final int load = banDoNap(p);
        if (load < 0) {
            return false;
        }
        final List<int[]> ds = cua(load);
        if (ds.isEmpty()) {
            return true;
        }
        final int ox = p.x / 16;
        final int oy = p.y / 16;
        for (final int[] n : ds) {
            if (!p.map.isMapOffline && (Math.abs(n[0] - ox) > BAN_KINH_O || Math.abs(n[1] - oy) > BAN_KINH_O)) {
                continue;
            }
            if (trong(dsBan(n[2], p.mapID), id)) {
                return true;
            }
        }
        return false;
    }
}
