package real;

import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.ChongCloneLogic;
import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThangPvp;
import com.htbb.pokemon.XepHangPvp;
import com.htbb.thongbao.ThongBao;
import data.Database;
import data.DatabaseLauDai;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAU XEP HANG PvP (0.9.7) - phan GAME cua com.htbb.pokemon.XepHangPvp: hang cho (NPC Dau truong -31 muc "Đấu xếp
 * hạng"), ghep moi 2 giay, dua hai ben vao Dau truong (202), theo doi tran (sat thuong, thoi gian, het gio), ket qua
 * Elo chi cho tran hop le (ThangPvpGame.ketQua goi {@link #ketQua}), thuong cuoi mua khi dang nhap mua moi.
 *
 * <p>CSDL: tob_pvp_tran (moi tran: hop le / huy + ly do), tob_pvp_thuong (thuong mua da phat, khoa (mua, id_char) =
 * khong phat hai lan ke ca khoi dong lai). Bang tao o {@link #taoBang} va sql/sua_pvp_pk.sql.
 */
public final class XepHangPvpGame {

    private XepHangPvpGame() {
    }

    /** Hai diem xuat phat trong Dau truong 202 (o). */
    static final int[][] DIEM = {{16, 20}, {24, 20}};

    /** Mot tran dang dien ra. */
    static final class Tran {
        final int a, b;
        final long ghepLuc;
        final long batDau;
        long stCuaA, stCuaB;
        volatile boolean xong;

        Tran(final int a, final int b, final long bay) {
            this.a = a;
            this.b = b;
            this.ghepLuc = bay;
            this.batDau = bay + XepHangPvp.GIAY_DEM_NGUOC * 1000L;
        }

        boolean co(final int id) {
            return id == a || id == b;
        }
    }

    static final ConcurrentHashMap<Integer, XepHangPvp.Cho> HANG = new ConcurrentHashMap<Integer, XepHangPvp.Cho>();
    static final ConcurrentHashMap<Integer, Tran> TRAN = new ConcurrentHashMap<Integer, Tran>();
    /** "a-b" (a &lt; b) -&gt; cac moc tran (ms) trong 24 gio (chong cay cung doi thu). */
    static final ConcurrentHashMap<String, List<Long>> LICH_SU = new ConcurrentHashMap<String, List<Long>>();
    private static volatile Timer hen;

    static String cap(final int a, final int b) {
        return Math.min(a, b) + "-" + Math.max(a, b);
    }

    static final XepHangPvp.LichSu LS = new XepHangPvp.LichSu() {
        public long ganNhat(final int a, final int b) {
            final List<Long> l = LICH_SU.get(cap(a, b));
            if (l == null) {
                return 0L;
            }
            synchronized (l) {
                return l.isEmpty() ? 0L : l.get(l.size() - 1).longValue();
            }
        }

        public int soTran24h(final int a, final int b) {
            final List<Long> l = LICH_SU.get(cap(a, b));
            if (l == null) {
                return 0;
            }
            final long tu = System.currentTimeMillis() - XepHangPvp.CUA_SO_LAP_MS;
            int n = 0;
            synchronized (l) {
                for (final Long t : l) {
                    n += t.longValue() >= tu ? 1 : 0;
                }
            }
            return n;
        }
    };

    static void ghiLichSu(final int a, final int b, final long bay) {
        final List<Long> l = LICH_SU.computeIfAbsent(cap(a, b), k -> new ArrayList<Long>());
        synchronized (l) {
            l.add(Long.valueOf(bay));
            l.removeIf(t -> bay - t.longValue() > XepHangPvp.CUA_SO_LAP_MS);
        }
    }

    // ================================================================== CSDL

    public static void taoBang() {
        DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_pvp_tran (id BIGINT NOT NULL AUTO_INCREMENT, mua INT NOT NULL,"
                + " thang INT NOT NULL, thua INT NOT NULL, hop_le TINYINT NOT NULL DEFAULT 0, ly_do VARCHAR(40) NOT NULL DEFAULT '',"
                + " giay INT NOT NULL DEFAULT 0, elo_thang INT NOT NULL DEFAULT 0, elo_thua INT NOT NULL DEFAULT 0,"
                + " luc DATETIME NOT NULL, PRIMARY KEY (id), KEY idx_pvpt_luc (luc), KEY idx_pvpt_thang (thang),"
                + " KEY idx_pvpt_thua (thua)) DEFAULT CHARSET=utf8");
        DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_pvp_thuong (mua INT NOT NULL, id_char INT NOT NULL,"
                + " bac TINYINT NOT NULL DEFAULT 0, elo INT NOT NULL DEFAULT 0, so_tran INT NOT NULL DEFAULT 0,"
                + " luong_khoa INT NOT NULL DEFAULT 0, danh_hieu INT NOT NULL DEFAULT 0, luc DATETIME NOT NULL,"
                + " PRIMARY KEY (mua, id_char)) DEFAULT CHARSET=utf8");
    }

    /** Khoi dong: nap lich su 24 gio (chong cay sau khi khoi dong lai) + bat bo ghep moi 2 giay. */
    public static synchronized void batDau() {
        if (hen != null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT thang, thua, luc FROM tob_pvp_tran WHERE luc >= ?");
            pre.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis() - XepHangPvp.CUA_SO_LAP_MS));
            rs = pre.executeQuery();
            while (rs.next()) {
                ghiLichSu(rs.getInt(1), rs.getInt(2), rs.getTimestamp(3).getTime());
            }
        } catch (final Exception e) {
            System.out.println("[XEP HANG] khong nap lich su: " + e);
        } finally {
            dong(rs, pre, conn);
        }
        hen = new Timer("xep-hang-pvp", true);
        hen.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    nhip(System.currentTimeMillis());
                } catch (final Throwable e) {
                    System.out.println("[XEP HANG] nhip: " + e);
                }
            }
        }, 2000L, 2000L);
    }

    static void dong(final ResultSet rs, final PreparedStatement pre, final Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (pre != null) {
                pre.close();
            }
        } catch (final Exception ignored) {
        }
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    static void ghiTran(final int mua, final int thang, final int thua, final boolean hopLe, final String lyDo, final long giay,
            final int eloThang, final int eloThua) {
        DatabaseLauDai.ghiCong("INSERT INTO tob_pvp_tran (mua, thang, thua, hop_le, ly_do, giay, elo_thang, elo_thua, luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())", Integer.valueOf(mua), Integer.valueOf(thang), Integer.valueOf(thua),
                Integer.valueOf(hopLe ? 1 : 0), lyDo == null ? "" : lyDo, Long.valueOf(giay), Integer.valueOf(eloThang),
                Integer.valueOf(eloThua));
    }

    // ================================================================== hang cho

    static XepHangPvp.Cum cum(final Char p) {
        try {
            final ChongCloneLogic.Ben b = ChongClone.ben(p, false);
            return new XepHangPvp.Cum(b.userId, b.may, b.ip);
        } catch (final Throwable e) {
            return new XepHangPvp.Cum(p.userID, "", "");
        }
    }

    static int eloMua(final Char p) {
        final HoSoLauDai h = LauDaiNhanVat.lay(p);
        if (h == null) {
            return ThangPvp.ELO_BAN_DAU;
        }
        ThangPvpGame.doiMua(h, ThangPvp.mua(LauDai.ngayHienTai()));
        return h.pvpElo;
    }

    /** NPC Dau truong -&gt; "Đấu xếp hạng": vao / roi hang cho. */
    public static void dangKy(final Char p) {
        if (p == null || p.charDBID <= 0 || p.isBot != -1) {
            return;
        }
        if (!LauDai.bat() || !com.htbb.cauhinh.CongTac.pvpTinhDiem()) {
            ThongBao.loi(p, "Đấu xếp hạng đang tạm đóng.", null);
            return;
        }
        final Integer k = Integer.valueOf(p.charDBID);
        if (TRAN.containsKey(k)) {
            ThongBao.loi(p, "Bạn đang trong một trận xếp hạng.", null);
            return;
        }
        if (HANG.remove(k) != null) {
            ThongBao.gui(p, ThongBao.TIN, "Đã rời hàng chờ đấu xếp hạng.", null, ThongBao.HINH_KHONG, 0);
            return;
        }
        if (p.lvDetail.lv < XepHangPvp.CAP_TOI_THIEU) {
            ThongBao.loi(p, "Cần cấp " + XepHangPvp.CAP_TOI_THIEU + " để đấu xếp hạng.", null);
            return;
        }
        final int elo = eloMua(p);
        HANG.put(k, new XepHangPvp.Cho(p.charDBID, p.lvDetail.lv, elo, System.currentTimeMillis(), cum(p)));
        ThongBao.gui(p, ThongBao.TIN, "Đã vào hàng chờ đấu xếp hạng",
                "Elo " + elo + " · " + ThangPvp.TEN_BAC[ThangPvp.bac(elo)] + " · ghép đối thủ cùng tầm cấp. Chọn lại để rời hàng.",
                ThongBao.HINH_KHONG, 0);
    }

    public static boolean dangCho(final Char p) {
        return p != null && HANG.containsKey(Integer.valueOf(p.charDBID));
    }

    /** Nhip 2 giay: bo nguoi offline khoi hang, ghep cap, het gio tran. */
    static void nhip(final long bay) {
        for (final Integer k : new ArrayList<Integer>(HANG.keySet())) {
            final Char c = online(k.intValue());
            if (c == null || c.hp <= 0) {
                HANG.remove(k);
            }
        }
        final List<XepHangPvp.Cho[]> cap = XepHangPvp.ghep(new ArrayList<XepHangPvp.Cho>(HANG.values()), bay, LS);
        for (final XepHangPvp.Cho[] c : cap) {
            batDauTran(c[0], c[1], bay);
        }
        for (final Tran t : new ArrayList<Tran>(new java.util.HashSet<Tran>(TRAN.values()))) {
            if (t.xong) {
                continue;
            }
            final Char a = online(t.a), b = online(t.b);
            final boolean roi = a == null || b == null || a.mapID != Map.idMapDautruong || b.mapID != Map.idMapDautruong;
            if (roi || bay - t.batDau > XepHangPvp.GIAY_TOI_DA * 1000L) {
                ketThuc(t, roi ? "roi_tran" : "het_gio", bay);
                for (final Char c : new Char[]{a, b}) {
                    ThongBao.gui(c, ThongBao.TIN, roi ? "Trận xếp hạng bị huỷ" : "Trận xếp hạng hoà (hết giờ)",
                            "Không cộng / trừ Elo.", ThongBao.HINH_KHONG, 0);
                }
            }
        }
    }

    static Char online(final int charDBID) {
        try {
            return CharManager.instance.getCharByCharDbID(charDBID);
        } catch (final Exception e) {
            return null;
        }
    }

    static void batDauTran(final XepHangPvp.Cho x, final XepHangPvp.Cho y, final long bay) {
        final Char a = online(x.id), b = online(y.id);
        HANG.remove(Integer.valueOf(x.id));
        HANG.remove(Integer.valueOf(y.id));
        if (a == null || b == null) {
            return;
        }
        final Tran t = new Tran(x.id, y.id, bay);
        TRAN.put(Integer.valueOf(x.id), t);
        TRAN.put(Integer.valueOf(y.id), t);
        ghiLichSu(x.id, y.id, bay);
        final Char[] ai = {a, b};
        for (int i = 0; i < 2; i++) {
            final Char c = ai[i];
            final Char d = ai[1 - i];
            try {
                c.map.move2Map(c, DIEM[i][0], DIEM[i][1], Map.idMapDautruong, c.inCountry);
            } catch (final Exception e) {
                System.out.println("[XEP HANG] khong dua vao dau truong: " + e);
            }
            ThongBao.gui(c, ThongBao.HIEM, "Trận xếp hạng: gặp " + d.charname,
                    "Lv " + d.lvDetail.lv + " · Elo " + eloMua(d) + " · bắt đầu sau " + XepHangPvp.GIAY_DEM_NGUOC + " giây, tối đa "
                            + XepHangPvp.GIAY_TOI_DA + " giây.", ThongBao.HINH_KHONG, 0);
        }
    }

    static void ketThuc(final Tran t, final String lyDo, final long bay) {
        if (t.xong) {
            return;
        }
        t.xong = true;
        TRAN.remove(Integer.valueOf(t.a), t);
        TRAN.remove(Integer.valueOf(t.b), t);
        if (lyDo != null) {
            ghiTran(ThangPvp.mua(LauDai.ngayHienTai()), t.a, t.b, false, lyDo, (bay - t.batDau) / 1000L, 0, 0);
        }
    }

    // ================================================================== trong tran

    /** Sat thuong PvP p -&gt; c (MapDanhNhau / Region). Ghi neu hai nguoi cung tran. */
    public static void khiSatThuong(final Char p, final Char c, final long dam) {
        if (p == null || c == null || dam <= 0 || TRAN.isEmpty()) {
            return;
        }
        final Tran t = TRAN.get(Integer.valueOf(p.charDBID));
        if (t == null || t.xong || !t.co(c.charDBID)) {
            return;
        }
        synchronized (t) {
            if (p.charDBID == t.a) {
                t.stCuaA += dam;
            } else {
                t.stCuaB += dam;
            }
        }
    }

    /** Dang trong mot tran xep hang chua xong. */
    public static boolean trongTran(final Char a) {
        if (a == null || TRAN.isEmpty()) {
            return false;
        }
        final Tran t = TRAN.get(Integer.valueOf(a.charDBID));
        return t != null && !t.xong;
    }

    /** Hai nguoi dang trong cung tran xep hang. */
    public static boolean cungTran(final Char a, final Char b) {
        if (a == null || b == null) {
            return false;
        }
        final Tran t = TRAN.get(Integer.valueOf(a.charDBID));
        return t != null && !t.xong && t.co(b.charDBID);
    }

    /**
     * ThangPvpGame.ketQua: thang ha thua. Tra ve he so Elo (phan nghin) neu tran hop le, -1 neu KHONG phai tran xep hang
     * (khong doi Elo), 0 neu huy (da ghi nghi van).
     */
    public static int ketQua(final Char thang, final Char thua) {
        final Tran t = TRAN.get(Integer.valueOf(thang.charDBID));
        if (t == null || t.xong || !t.co(thua.charDBID)) {
            return -1;
        }
        final long bay = System.currentTimeMillis();
        final long giay = (bay - t.batDau) / 1000L;
        final long stThua;
        synchronized (t) {
            stThua = thua.charDBID == t.a ? t.stCuaA : t.stCuaB;
        }
        final String ly = XepHangPvp.lyDoHuy(giay, stThua, thang.maxhp, cum(thang).trung(cum(thua)));
        ketThuc(t, null, bay);
        if (ly != null) {
            ghiTran(ThangPvp.mua(LauDai.ngayHienTai()), thang.charDBID, thua.charDBID, false, ly, giay, 0, 0);
            try {
                Database.instance.saveOrtherLog("", thang.charname, "pvp xep hang HUY (" + ly + ") vs " + thua.charname, "pvpnghi");
            } catch (final Exception ignored) {
            }
            for (final Char c : new Char[]{thang, thua}) {
                ThongBao.gui(c, ThongBao.TIN, "Trận xếp hạng không được tính", lyDoChu(ly), ThongBao.HINH_KHONG, 0);
            }
            return 0;
        }
        // so tran truoc do cua cap trong 24 gio (tran nay da ghi luc ghep)
        return XepHangPvp.heSoLap(Math.max(0, LS.soTran24h(thang.charDBID, thua.charDBID) - 1));
    }

    static String lyDoChu(final String ly) {
        if ("qua_ngan".equals(ly)) {
            return "Trận quá ngắn (dưới " + XepHangPvp.GIAY_TOI_THIEU + " giây). Không cộng / trừ Elo.";
        }
        if ("khong_danh_tra".equals(ly)) {
            return "Bên thua gần như không đánh trả. Không cộng / trừ Elo.";
        }
        return "Hai tài khoản cùng thiết bị / mạng. Không cộng / trừ Elo.";
    }

    // ================================================================== thuong mua

    /**
     * Dang nhap (DanhHieuGame.nhip lan dau): phat thuong moi mua DA KET THUC chua phat. Khoa (mua, id_char) trong
     * tob_pvp_thuong: INSERT IGNORE truoc, chi phat khi chen duoc dong moi -&gt; khong phat hai lan.
     */
    public static void thuongMuaCu(final Char p) {
        if (p == null || p.charDBID <= 0 || p.isBot != -1 || !LauDai.bat()) {
            return;
        }
        final int muaNay = ThangPvp.mua(LauDai.ngayHienTai());
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        final List<int[]> ds = new ArrayList<int[]>();
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("SELECT m.mua, m.elo, m.thang + m.thua FROM tob_pvp_mua m WHERE m.id_char = ? AND m.mua < ?"
                    + " AND NOT EXISTS (SELECT 1 FROM tob_pvp_thuong t WHERE t.mua = m.mua AND t.id_char = m.id_char)");
            pre.setInt(1, p.charDBID);
            pre.setInt(2, muaNay);
            rs = pre.executeQuery();
            while (rs.next()) {
                ds.add(new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3)});
            }
        } catch (final Exception e) {
            System.out.println("[XEP HANG] doc thuong mua " + p.charDBID + ": " + e);
            return;
        } finally {
            dong(rs, pre, conn);
        }
        for (final int[] d : ds) {
            phatMua(p, d[0], d[1], d[2]);
        }
    }

    static void phatMua(final Char p, final int mua, final int elo, final int soTran) {
        final int[] th = XepHangPvp.thuongMua(elo, soTran);
        final com.htbb.pokemon.DanhHieuLogic.Mau dh = th[0] > 0 && th[0] < XepHangPvp.MA_DANH_HIEU_MUA.length
                ? DanhHieuGame.mauTheoMa(XepHangPvp.MA_DANH_HIEU_MUA[th[0]]) : null;
        final int chen = DatabaseLauDai.ghiCong("INSERT IGNORE INTO tob_pvp_thuong (mua, id_char, bac, elo, so_tran, luong_khoa,"
                + " danh_hieu, luc) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())", Integer.valueOf(mua), Integer.valueOf(p.charDBID),
                Integer.valueOf(th[0]), Integer.valueOf(elo), Integer.valueOf(soTran), Integer.valueOf(th[1]),
                Integer.valueOf(dh == null ? 0 : dh.id));
        if (chen != 1) {
            return; // da phat (hoac loi CSDL): khong phat
        }
        if (th[0] <= 0) {
            return;
        }
        if (th[1] > 0) {
            try {
                data.DatabaseGiftCode.applyItemString(p, "LUONGKHOA:" + th[1]);
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            } catch (final Exception e) {
                System.out.println("[XEP HANG] phat luong khoa " + p.charDBID + ": " + e);
            }
        }
        if (dh != null) {
            DanhHieuGame.trao(p, dh, "thuong_mua_" + mua);
        }
        Database.instance.saveOrtherLog("", p.charname, "thuong mua pvp " + mua + " bac " + th[0] + " elo " + elo + " tran "
                + soTran + ": " + th[1] + " luong khoa" + (dh == null ? "" : ", danh hieu " + dh.id), "pvpmua");
        ThongBao.gui(p, ThongBao.HIEM, "Thưởng mùa PvP " + (mua + 1) + ": " + ThangPvp.TEN_BAC[th[0]],
                "+" + th[1] + " lượng khoá" + (dh == null ? "" : " · danh hiệu " + dh.ten), ThongBao.HINH_KHONG, 0);
    }

    // ================================================================== kiem thu

    public static void xoaHet() {
        HANG.clear();
        TRAN.clear();
        LICH_SU.clear();
    }

    /** Kiem thu: dat tran gia giua hai id, bat dau luc batDau. */
    public static void datTran(final int a, final int b, final long batDau) {
        final Tran t = new Tran(a, b, batDau - XepHangPvp.GIAY_DEM_NGUOC * 1000L);
        TRAN.put(Integer.valueOf(a), t);
        TRAN.put(Integer.valueOf(b), t);
        ghiLichSu(a, b, batDau);
    }
}
