package real;

import com.htbb.pokemon.HoTongNhiemVu;
import com.htbb.pokemon.KhuVucNhiemVu;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuMau;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import server.TeamServer;

/**
 * BUOC NHIEM VU HO TONG (nhanh sua-ho-tong) - lop noi game cua com.htbb.pokemon.HoTongNhiemVu (luat + ly do sua o do).
 *
 * <pre>
 *  RequestNPCInfoHandler -50 (Tổng tiêu đầu)   -> {@link #hienMucNhan} / {@link #chuMucNhan}: muc nhan hang cho nguoi dang lam
 *                                                buoc ho_tong ca khi het luot / khac nuoc.
 *  RealController / MapMenuNpc doMenuNpcNhanTieu -> {@link #nhanHang}: bo qua gio / cap / nuoc cho nguoi dang lam, het luot thi
 *                                                MOT luot mien phi / ngay; moi tu choi deu bao ly do (truoc day im lang).
 *  NpcDichVu (Kecleon tạp hoá)                  -> {@link #traTaiKecleon}: giao xe hang nhiem vu (TraHang.giao, nhu Dịch trạm).
 *  NhiemVuEngine.nhan / chonDangLam             -> {@link #duoi}: noi nhan + noi giao + chi duong.
 * </pre>
 */
public final class HoTongNhiemVuGame {

    /** charDBID -> ngay (NhiemVuEngine.homNay) da cap luot mien phi. */
    static final ConcurrentHashMap<Integer, Integer> MIEN_PHI = new ConcurrentHashMap<Integer, Integer>();
    /** Vi tri Tổng tiêu đầu theo inCountry (px) - du phong khi khong tim thay bot (MapNapTaiNguyen posNPCInVilage). */
    static final short[][] VI_TRI_NHAN = {{400, 176}, {464, 320}, {320, 672}};
    static final String[] TEN_LANG = {"Pewter", "Blackthorn", "Cianwood"};

    private HoTongNhiemVuGame() {
    }

    /** Buoc ho_tong dang lam (da nhan, chua xong) dau tien; null = khong co. */
    public static NhiemVuMau buocDangLam(final Char p) {
        if (p == null || !LauDai.bat() || p.charDBID <= 0) {
            return null;
        }
        try {
            final List<NhiemVuMau> ds = NhiemVuEngine.THEO_KIEU.get(HoTongNhiemVu.KIEU);
            if (ds == null || ds.isEmpty()) {
                return null;
            }
            final ConcurrentHashMap<String, long[]> t = NhiemVuEngine.td(p);
            if (t == null) {
                return null;
            }
            for (final NhiemVuMau m : ds) {
                final long[] v = t.get(m.ma);
                if (v != null && v[1] == 0) {
                    return m;
                }
            }
        } catch (final Exception e) {
            System.out.println("HoTongNhiemVuGame.buocDangLam: " + e);
        }
        return null;
    }

    public static boolean dangLam(final Char p) {
        return buocDangLam(p) != null;
    }

    public static boolean hienMucNhan(final Char p) {
        return HoTongNhiemVu.hienMucNhan(p.luot_van_tieu, p.myCountry, p.inCountry, dangLam(p));
    }

    public static String chuMucNhan(final Char p) {
        return HoTongNhiemVu.chuMucNhan(p.luot_van_tieu, dangLam(p));
    }

    static int gio() {
        return java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
    }

    /** null = nhan duoc; khac = cau bao. */
    public static String lyDoKhongNhan(final Char p, final int gio) {
        return HoTongNhiemVu.lyDoKhongNhan(Map.openvantieu, gio, p.lvDetail == null ? 0 : p.lvDetail.lv, p.myCountry == p.inCountry,
                p.map == null ? -1 : p.map.mapIDLoadMap, dangLam(p), TeamServer.server == 100);
    }

    /** Dang lam buoc ho_tong, het luot, chua co xe: cap 1 luot (mot lan / ngay). @return true neu vua cap. */
    public static boolean capMienPhi(final Char p) {
        if (p.monster != null) {
            return false;
        }
        final Integer key = Integer.valueOf(p.charDBID);
        final Integer da = MIEN_PHI.get(key);
        final int homNay = NhiemVuEngine.homNay();
        if (!HoTongNhiemVu.mienPhi(p.luot_van_tieu, true, da == null ? Integer.MIN_VALUE : da.intValue(), homNay)) {
            return false;
        }
        p.luot_van_tieu = 1;
        MIEN_PHI.put(key, Integer.valueOf(homNay));
        return true;
    }

    /** Chon muc nhan hang o Tổng tiêu đầu (idMenu 0, muc {@code typetieu}). */
    public static void nhanHang(final Char p, final int idMenu, final int typetieu) {
        if (p == null || idMenu != 0) {
            return;
        }
        try {
            final String ly = lyDoKhongNhan(p, gio());
            if (ly != null) {
                p.sendMessage(MessageCreator.createServerAlertMessage(ly, ""));
                return;
            }
            final boolean dang = dangLam(p);
            final boolean mienPhi = dang && capMienPhi(p);
            final MonsterVantieu truoc = p.monster;
            Char.doInitMonter(p, typetieu);
            if (mienPhi && p.monster != null && p.monster != truoc) {
                p.monster.mienPhiNhiemVu = true; // chu game 31/10: chi thuong nhiem vu
            }
            if (dang && p.monster != null && p.monster != truoc) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Hàng nhiệm vụ. " + chiDan(p), ""));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * NpcDichVu: nguoi choi dang canh Kecleon tạp hoá. Dang lam buoc ho_tong va co xe hang -> giao (hoac bao ly do). @return
     * true = da xu ly (khong mo menu cua hang).
     */
    public static boolean traTaiKecleon(final Char p) {
        if (p == null || p.monster == null || !dangLam(p)) {
            return false;
        }
        try {
            if (p.monster.isFinish()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Xe hàng đã hết giờ hoặc bị phá, hãy nhận lại ở " + HoTongNhiemVu.TEN_NPC_NHAN + ".", ""));
                p.monster.actorDie();
                p.monster = null;
                p.typeTieu = -1;
                return true;
            }
            if (p.monster.map == null || !p.monster.map.equals(p.map) || !Map.inRangeActor((LiveActor) p, (LiveActor) p.monster, 120)) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Xe hàng còn ở xa. Hãy đi cạnh xe hàng tới Kecleon rồi nói chuyện.", ""));
                return true;
            }
            TraHang.giao(p);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // ------------------------------------------------------------------ chu

    static String tenMap(final int id) {
        final String t = MucTieuNhiemVuGame.tenMap(id);
        if (t != null && !t.isEmpty()) {
            return t;
        }
        for (int i = 0; i < HoTongNhiemVu.LANG.length; i++) {
            if (HoTongNhiemVu.LANG[i] == id) {
                return TEN_LANG[i];
            }
        }
        return id == HoTongNhiemVu.BAN_DO_DICH_TRAM ? "Đường Victory" : "";
    }

    /** Lang nen toi: lang dang dung, khong thi lang nha (LangKhoiDau theo vung). */
    static int lang(final Char p) {
        final int nap = NpcTinhBanDo.banDoNap(p);
        if (HoTongNhiemVu.laLang(nap)) {
            return nap;
        }
        try {
            final int[][] b = LangKhoiDau.bang(p);
            final int v = p.myCountry >= 0 && p.myCountry < b.length ? p.myCountry : 0;
            return b[v][0];
        } catch (final Exception e) {
            return HoTongNhiemVu.LANG[0];
        }
    }

    /** O Tổng tiêu đầu ma nguoi choi thay (CharCapNhat: posNPCInVilage[inCountry]). */
    static int[] oNhan(final Char p) {
        final int c = p.inCountry >= 0 && p.inCountry < VI_TRI_NHAN.length ? p.inCountry : 0;
        short[] px = VI_TRI_NHAN[c];
        try {
            if (Map.npcVilage != null) {
                for (final Char b : Map.npcVilage) {
                    if (b != null && b.isBot == -50 && b.posNPCInVilage != null && c < b.posNPCInVilage.length && b.posNPCInVilage[c] != null) {
                        px = b.posNPCInVilage[c];
                        break;
                    }
                }
            }
        } catch (final Exception ignored) {
        }
        return new int[]{px[0] / 16, px[1] / 16};
    }

    /** Duoi chu nhiem vu (rong neu khong phai buoc ho_tong). */
    public static String duoi(final Char p, final NhiemVuMau m) {
        if (p == null || m == null || !HoTongNhiemVu.KIEU.equals(m.kieu)) {
            return "";
        }
        return "\n" + chiDan(p);
    }

    /** Noi nhan + noi giao + chi duong toi diem can toi ke tiep. */
    public static String chiDan(final Char p) {
        final int lang = lang(p);
        final int[] oNhan = oNhan(p);
        final int[] oKec = NpcTinhBanDo.oNpc(lang, VeDichChuyenGame.NPC_KECLEON);
        final boolean coXe = p.monster != null && !p.monster.isFinish();
        final int[] dich = coXe ? oKec : oNhan;
        String duong = "";
        final int nap = NpcTinhBanDo.banDoNap(p);
        if (dich != null && nap == lang) {
            final List<KhuVucNhiemVu.Vung> v = Collections.singletonList(new KhuVucNhiemVu.Vung("", lang, dich[0] - 1, dich[1] - 1,
                    dich[0] + 1, dich[1] + 1, false, ""));
            duong = KhuVucNhiemVu.chiDuong(v, nap, p.x / 16, p.y / 16, "").replace(" (ô " + dich[0] + "," + dich[1] + ")", "");
        } else if (nap != lang) {
            duong = "bạn đang ở " + (nap >= 0 && !tenMap(nap).isEmpty() ? tenMap(nap) : "nơi khác") + ", hãy về " + tenMap(lang);
        }
        return HoTongNhiemVu.chiDan(tenMap(lang), oNhan, oKec, tenMap(HoTongNhiemVu.BAN_DO_DICH_TRAM), coXe, duong);
    }

    /** Test. */
    public static void xoaMienPhi() {
        MIEN_PHI.clear();
    }
}
