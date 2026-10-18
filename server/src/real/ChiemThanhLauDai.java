package real;

import com.htbb.pokemon.ChiemThanhLogic;
import com.htbb.pokemon.LauDai;
import data.CharClan;
import data.Database;
import data.DatabaseLauDai;
import data.NewClan;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Chiem thanh hang tuan khi bat co sv.lau_dai (yeu cau 17/10/2026): bat lai thuong o
 * NpcReceiveCard (thay ThuongChiemThanh 10.000 luong + 50 trieu xu da tat). So o ChiemThanhLogic.
 * Dong gop = sat thuong PvP trong ban do thanh luc dang chien (Region, Map.nwar). Moi khoan phat
 * ghi tob_chiem_thanh_tuan + log.
 */
public final class ChiemThanhLauDai {

    /** country -> {charDBID -> diem}. */
    private static final List<HashMap<Integer, long[]>> DONG_GOP = new ArrayList<HashMap<Integer, long[]>>();
    private static final short[] CLAN_BUFF = {-1, -1, -1};
    private static final long[] BUFF_DEN = {0L, 0L, 0L};
    /** Thanh vien bang thang luc chiem (nhan danh hieu khi vao game trong 7 ngay) + da nhan. */
    private static final List<HashSet<String>> THANH_VIEN = new ArrayList<HashSet<String>>();
    private static final List<HashSet<Integer>> DA_DANH_HIEU = new ArrayList<HashSet<Integer>>();

    static {
        for (int i = 0; i < 3; i++) {
            DONG_GOP.add(new HashMap<Integer, long[]>());
            THANH_VIEN.add(new HashSet<String>());
            DA_DANH_HIEU.add(new HashSet<Integer>());
        }
    }

    private ChiemThanhLauDai() {
    }

    /** Region PvP: cong dong gop khi dang chien thanh trong ban do thanh. */
    static void dongGop(final Char p, final int dam) {
        if (!LauDai.bat() || p == null || dam <= 0 || p.inCountry < 0 || p.inCountry > 2) {
            return;
        }
        if (!Map.nwar[p.inCountry] || p.map == null || p.map.mapId != Map.idMapTown) {
            return;
        }
        synchronized (DONG_GOP) {
            final long[] d = DONG_GOP.get(p.inCountry).computeIfAbsent(Integer.valueOf(p.charDBID), k -> new long[1]);
            d[0] += dam;
        }
    }

    /** % EXP thuong cho bang dang giu thanh (ExpLauDai.buffPhanTram). */
    static int buffExp(final Char c) {
        if (!LauDai.bat() || c == null || c.idClan < 0) {
            return 0;
        }
        final long bay = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            if (CLAN_BUFF[i] == c.idClan && BUFF_DEN[i] > bay) {
                return ChiemThanhLogic.BUFF_EXP;
            }
        }
        return 0;
    }

    /** NpcReceiveCard: mot bang vua chiem thanh. */
    public static synchronized void batDau(final int country, final int idClan, final String tenBang) {
        if (!LauDai.bat() || country < 0 || country > 2 || idClan < 0) {
            return;
        }
        final long lan = System.currentTimeMillis();
        CLAN_BUFF[country] = (short) idClan;
        BUFF_DEN[country] = lan + ChiemThanhLogic.BUFF_MS;
        final HashSet<String> tv = THANH_VIEN.get(country);
        tv.clear();
        DA_DANH_HIEU.get(country).clear();
        NewClan clan = null;
        try {
            clan = NewClan.getClan((short) idClan);
            if (clan != null) {
                final Vector<CharClan> mems = clan.getAllMember();
                for (int i = 0; i < mems.size(); i++) {
                    if (mems.get(i) != null && mems.get(i).charname != null) {
                        tv.add(mems.get(i).charname.toLowerCase());
                    }
                }
            }
        } catch (final Exception e) {
            System.out.println("ChiemThanhLauDai: " + e);
        }
        // bang dong gop -> danh sach du dieu kien
        final List<long[]> ds = new ArrayList<long[]>();
        synchronized (DONG_GOP) {
            for (final java.util.Map.Entry<Integer, long[]> e : DONG_GOP.get(country).entrySet()) {
                ds.add(new long[]{e.getKey().longValue(), e.getValue()[0]});
            }
            DONG_GOP.get(country).clear();
        }
        ds.sort((a, b) -> Long.compare(b[1], a[1]));
        final long cao = ds.isEmpty() ? 0L : ds.get(0)[1];
        // nguoi online du dieu kien
        final HashMap<Integer, Char> online = new HashMap<Integer, Char>();
        for (int i = 0; i < CharManager.instance.vChars.size(); i++) {
            try {
                final Char c = CharManager.instance.vChars.elementAt(i);
                if (c != null && c.isBot == -1) {
                    online.put(Integer.valueOf(c.charDBID), c);
                }
            } catch (final Exception e) {
                break;
            }
        }
        int soDuThang = 0;
        for (final long[] d : ds) {
            final Char c = online.get(Integer.valueOf((int) d[0]));
            if (c != null && c.idClan == idClan && ChiemThanhLogic.duDongGop(d[1], cao)) {
                soDuThang++;
            }
        }
        final long quy = clan != null ? clan.money : 0L;
        long daChia = 0L;
        for (int h = 0; h < ds.size(); h++) {
            final long[] d = ds.get(h);
            final Char c = online.get(Integer.valueOf((int) d[0]));
            if (c == null) {
                continue; // vang mat luc ket thuc: chi co danh hieu khi vao game (neu la thanh vien)
            }
            final boolean thang = c.idClan == idClan;
            final boolean du = ChiemThanhLogic.duDongGop(d[1], cao);
            final int lk = ChiemThanhLogic.luongKhoa(thang, du, h + 1);
            long xu = 0L;
            if (thang && du && clan != null) {
                xu = ChiemThanhLogic.phanQuy(quy, soDuThang, LauDai.xuMoiGio(c.lvDetail.lv));
            }
            if (lk > 0) {
                c.addLuongLock(lk);
            }
            if (xu > 0L) {
                clan.addMoney2Clan(-xu); // chia lai quy bang, khong in tien moi
                daChia += xu;
                c.addXu(xu, "chia quy bang chiem thanh");
            }
            final String thuong = lk + " lk, " + xu + " xu quy bang";
            DatabaseLauDai.ghiChiemThanh(lan, c.charDBID, c.idClan, d[1], thuong);
            NhiemVuEngine.suKien(c, "tham_gia_gym", "", 1);
            if (du) {
                NhiemVuEngine.suKien(c, "doi_dong_gop", "", 1);
            }
            Database.instance.saveOrtherLog("", c.charname, "chiem thanh lau dai " + country + " hang " + (h + 1) + ": " + thuong, "laudai");
            if (lk > 0 || xu > 0L) {
                LauDaiNhanVat.bao(c, "Chiếm thành: đóng góp hạng " + (h + 1) + ", nhận " + lk + " lượng khoá"
                        + (xu > 0L ? " và " + xu + " xu từ quỹ bang" : "") + ".");
            }
        }
        if (clan != null && daChia > 0L) {
            try {
                clan.updateNewClandata2DB();
            } catch (final Exception ignored) {
            }
        }
        for (final Char c : online.values()) {
            kiemTraKhiVaoGame(c);
        }
        try {
            Database.instance.saveLogClan(tenBang, "thuongct", "chiem thanh lau dai: buff +10% exp 7 ngay, chia quy " + daChia);
        } catch (final Exception ignored) {
        }
        System.out.println("[LAU DAI] chiem thanh " + country + " bang " + tenBang + ": " + ds.size() + " nguoi dong gop, chia quy " + daChia);
    }

    /** Vao game: thanh vien bang thang (luc chiem) nhan danh hieu 7 ngay mot lan. */
    public static synchronized void kiemTraKhiVaoGame(final Char p) {
        if (!LauDai.bat() || p == null || p.idClan < 0 || p.charname == null) {
            return;
        }
        final long bay = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            if (CLAN_BUFF[i] == p.idClan && BUFF_DEN[i] > bay && THANH_VIEN.get(i).contains(p.charname.toLowerCase())
                    && DA_DANH_HIEU.get(i).add(Integer.valueOf(p.charDBID))) {
                try {
                    p.addDanhHieu(LauDai.so("chiem_thanh.danh_hieu", 8935), (long) ChiemThanhLogic.DANH_HIEU_PHUT);
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                    LauDaiNhanVat.bao(p, "Bang của bạn giữ thành: danh hiệu 7 ngày + 10% EXP.");
                } catch (final Exception e) {
                    System.out.println("ChiemThanhLauDai danh hieu: " + e);
                }
            }
        }
    }
}
