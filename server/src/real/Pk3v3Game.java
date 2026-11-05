package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.ChongCloneLogic;
import com.htbb.pokemon.CupLogic;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.PhongRieng;
import com.htbb.pokemon.Pk3v3;
import data.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PK 3 VS 3 BUOI TOI - phan GAME cua com.htbb.pokemon.Pk3v3 (luat thuan, so lieu). DUNG LAI san Cup PvP tuan (ban do
 * phong rieng {@link PhongRiengGame#MAP_CUP}, kho khu {@link PhongRiengGame#KHO_CUP}) va duong danh nguoi cua Region
 * (Region.doAttackPlayer goi {@link #choDanh} / {@link #satThuong} / {@link #sauDon} cho khu cua tran 3v3, CupGame cho khu
 * cua tran cup - hai ben muon khu khac nhau cua cung kho nen khong lan). Nhip 1 giay o luong PhongRiengGame.
 *
 * <ul>
 *   <li>Vao: So tay huan luyen -&gt; "PK 3 vs 3 buổi tối" (chi so {@link SoTayLauDai#MUC_PK3V3}; client moi: Menu -&gt; Sự
 *       kiện), NPC Dau truong (-31) muc cuoi, Cup PvP tuan -&gt; muc cuoi (duong cho client cu: Menu -&gt; Sự kiện -&gt; Cúp PvP
 *       tuần).</li>
 *   <li>CSDL ({@link #taoBang} + sql/htbb.sql khoi them_pk3v3.sql): tob_pk3v3_luot (luot / thang co thuong / luong
 *       theo ngay + tai khoan), tob_pk3v3_diem (bang ngay / tuan theo nhan vat), tob_pk3v3_gap (so lan gap nhau trong
 *       ngay), tob_pk3v3_tran (nhat ky tran), tob_pk3v3_chot (moc da chot ngay / tuan), tob_pk3v3_thuong (thuong top
 *       ngay / danh hieu tuan, phat mot lan - nguoi offline nhan khi dang nhap).</li>
 * </ul>
 * Kiem: test/KiemPk3v3 (thay {@link #KHO}, {@link #SAN}, {@link #LOA} bang ban gia).
 */
public final class Pk3v3Game {

    private Pk3v3Game() {
    }

    static final int MENU_MO = 80, MENU_CHO = 81, MENU_DONG = 82, MENU_BO = 83;

    // ================================================================== cau hinh

    static int so(final String k, final int md, final int min, final int max) {
        long v;
        try {
            v = CauHinhTrungTam.so(k, md);
        } catch (final Throwable e) {
            v = md;
        }
        return (int) Math.max(min, Math.min(max, v));
    }

    static boolean batCo() {
        return so(Pk3v3.K_BAT, 1, 0, 1) == 1;
    }

    static boolean bat() {
        return batCo() && LauDai.bat() && SAN.co();
    }

    static int gioMo() {
        return so(Pk3v3.K_GIO_MO, Pk3v3.GIO_MO, 0, 23);
    }

    static int gioDong() {
        return so(Pk3v3.K_GIO_DONG, Pk3v3.GIO_DONG, 1, 24);
    }

    static int tranNgay() {
        return so(Pk3v3.K_TRAN_NGAY, Pk3v3.TRAN_NGAY, 1, 2000);
    }

    static int capToiThieu() {
        return so(Pk3v3.K_CAP, Pk3v3.CAP_TOI_THIEU, 1, 140);
    }

    static int luongThang() {
        return so(Pk3v3.K_LUONG_THANG, Pk3v3.LUONG_THANG, 0, 5);
    }

    static int thangThuongNgay() {
        return so(Pk3v3.K_THANG_THUONG, Pk3v3.THANG_THUONG_NGAY, 0, 20);
    }

    static int tranLuongTuan() {
        return so(Pk3v3.K_TRAN_LUONG_TUAN, Pk3v3.TRAN_LUONG_TUAN, 0, 100);
    }

    static int gapLaiToiDa() {
        return so(Pk3v3.K_GAP_LAI, Pk3v3.GAP_LAI_TOI_DA, 1, 50);
    }

    static int giayToiThieu() {
        return so(Pk3v3.K_GIAY_TOI_THIEU, Pk3v3.GIAY_TOI_THIEU, 0, Pk3v3.GIAY_TRAN - 10);
    }

    /** NPC Dau truong (-31): hien muc "PK 3 vs 3 buổi tối" (ca khi dang dong - xem gio mo / bang xep hang). */
    public static boolean hienNpc() {
        return bat();
    }

    /** Dang trong gio mo (va dang bat). */
    static boolean dangMo(final long bay) {
        return bat() && Pk3v3.dangMo(bay, gioMo(), gioDong());
    }

    static String khung() {
        return Pk3v3.khungGio(gioMo(), gioDong());
    }

    static String luat() {
        return Pk3v3.luatChu(gioMo(), gioDong(), tranNgay(), capToiThieu(), luongThang(), thangThuongNgay(), tranLuongTuan(),
                gapLaiToiDa(), giayToiThieu());
    }

    // ================================================================== diem noi (thay duoc khi kiem)

    /** San dau: ban do + kho khu cua Cup (that) hoac ban gia (kiem). */
    interface San {
        boolean co();

        int mapId();

        int muon();

        void tra(int khu);

        /** Dua c vao khu {@code khu}, ben {@code ben} (0 trai / 1 phai), vi tri thu {@code thuTu} trong doi. */
        void dua(Char c, int khu, int ben, int thuTu);

        void ve(Char c, PhongRiengGame.ViTri v);

        Char online(int charId);
    }

    static final San SAN_THAT = new San() {
        public boolean co() {
            return PhongRiengGame.cup != null;
        }

        public int mapId() {
            return PhongRiengGame.MAP_CUP;
        }

        public int muon() {
            return PhongRiengGame.KHO_CUP.muon();
        }

        public void tra(final int khu) {
            PhongRiengGame.KHO_CUP.tra(khu);
        }

        public void dua(final Char c, final int khu, final int ben, final int thuTu) {
            final MapPhongRieng m = PhongRiengGame.cup;
            if (m == null) {
                return;
            }
            final int[] o = PhongRiengGame.diemDiDuoc(m, m.w * 16 * (ben == 0 ? 3 : 7) / 10, m.h * 16 / 2);
            PhongRiengGame.dua(c, m, khu, o[0], o[1] + (thuTu - 1) * 24, 0);
        }

        public void ve(final Char c, final PhongRiengGame.ViTri v) {
            PhongRiengGame.veCho(c, v);
        }

        public Char online(final int charId) {
            return PhongRiengGame.online(charId);
        }
    };

    static volatile San SAN = SAN_THAT;

    /** To doi dang online cua p (kiem thay ban gia). */
    static volatile java.util.function.Function<Char, List<Char>> DOI = new java.util.function.Function<Char, List<Char>>() {
        public List<Char> apply(final Char p) {
            return PhoBanGame.doiCua(p);
        }
    };
    static volatile java.util.function.Predicate<Char> TRUONG = new java.util.function.Predicate<Char>() {
        public boolean test(final Char p) {
            return PhoBanGame.laTruong(p);
        }
    };

    /** Dong ho (DongHo.bay: gio may chu + lech GM tua) - kiem thay bang gio co dinh. */
    static volatile java.util.function.LongSupplier GIO = new java.util.function.LongSupplier() {
        public long getAsLong() {
            return com.htbb.pokemon.DongHo.bay();
        }
    };

    /** Loa toan may chu. */
    static volatile java.util.function.Consumer<String> LOA = new java.util.function.Consumer<String>() {
        public void accept(final String s) {
            com.htbb.chongspam.ThongBaoGop.dua("pk3v3", s);
        }
    };

    static void loa(final String s) {
        try {
            LOA.accept(s);
        } catch (final Throwable ignored) {
        }
    }

    // ================================================================== kho (CSDL)

    /** Mot dong diem: ngay + tuan + diem cua nhan vat. */
    static final class DongDiem {
        final int ngay, tuan;
        final Pk3v3.Diem d;

        DongDiem(final int ngay, final int tuan, final Pk3v3.Diem d) {
            this.ngay = ngay;
            this.tuan = tuan;
            this.d = d;
        }
    }

    /** Luu tru. Moi ham nuot loi (CSDL loi: hoat dong van chay tren bo nho, chi mat khi khoi dong lai). */
    interface Kho {
        void taoBang();

        /** {so_tran, thang_thuong, luong} cua tai khoan trong ngay. */
        int[] napLuot(int ngay, int userId);

        int napLuongTuan(int tuan, int userId);

        void congLuot(int ngay, int tuan, int userId, int dTran, int dThang, int dLuong);

        List<DongDiem> napDiem(int tuTuan);

        void congDiem(int ngay, int tuan, Pk3v3.Diem delta);

        java.util.Map<String, Integer> napGap(int ngay);

        void congGap(int ngay, int a, int b);

        void ghiTran(int ngay, int tuan, int bac, String doiA, String doiB, int ketQua, int giay, String lyDo);

        /** Ghi moc chot (loai, ky): true = vua ghi (chua ai chot), false = da chot / loi. */
        boolean chot(String loai, int ky);

        void ghiThuong(String loai, int ky, int charId, int hang, int luongKhoa, int danhHieu, int phut);

        /** Thuong cho nhan: {loai, ky, hang, luong_khoa, danh_hieu, phut}. */
        List<Object[]> thuongCho(int charId);

        /** Danh dau da nhan (nguyen tu): true = vua danh dau, duoc phat. */
        boolean daNhan(String loai, int ky, int charId);
    }

    static final String[] SQL_TAO_BANG = {
        "CREATE TABLE IF NOT EXISTS tob_pk3v3_luot (ngay INT NOT NULL, user_id INT NOT NULL, tuan INT NOT NULL DEFAULT 0,"
            + " so_tran INT NOT NULL DEFAULT 0, thang_thuong INT NOT NULL DEFAULT 0, luong INT NOT NULL DEFAULT 0,"
            + " cap_nhat DATETIME NULL, PRIMARY KEY (ngay, user_id), KEY k_tuan (tuan, user_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pk3v3_diem (ngay INT NOT NULL, id_char INT NOT NULL, tuan INT NOT NULL DEFAULT 0,"
            + " user_id INT NOT NULL DEFAULT 0, ten VARCHAR(32) NOT NULL DEFAULT '', la_bot TINYINT NOT NULL DEFAULT 0,"
            + " diem INT NOT NULL DEFAULT 0, thang INT NOT NULL DEFAULT 0, hoa INT NOT NULL DEFAULT 0, thua INT NOT NULL DEFAULT 0,"
            + " hop_le INT NOT NULL DEFAULT 0, cap_nhat DATETIME NULL, PRIMARY KEY (ngay, id_char), KEY k_tuan (tuan),"
            + " KEY k_diem (ngay, diem)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pk3v3_gap (ngay INT NOT NULL, a INT NOT NULL, b INT NOT NULL, so INT NOT NULL DEFAULT 0,"
            + " PRIMARY KEY (ngay, a, b)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pk3v3_tran (id BIGINT NOT NULL AUTO_INCREMENT, ngay INT NOT NULL, tuan INT NOT NULL DEFAULT 0,"
            + " bac TINYINT NOT NULL DEFAULT 0, doi_a VARCHAR(64) NOT NULL DEFAULT '', doi_b VARCHAR(64) NOT NULL DEFAULT '',"
            + " ket_qua TINYINT NOT NULL DEFAULT 0, giay INT NOT NULL DEFAULT 0, ly_do VARCHAR(16) NOT NULL DEFAULT '',"
            + " luc DATETIME NOT NULL, PRIMARY KEY (id), KEY k_ngay (ngay)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pk3v3_chot (loai VARCHAR(8) NOT NULL, ky INT NOT NULL, luc DATETIME NOT NULL,"
            + " PRIMARY KEY (loai, ky)) ENGINE=InnoDB DEFAULT CHARSET=utf8",
        "CREATE TABLE IF NOT EXISTS tob_pk3v3_thuong (loai VARCHAR(8) NOT NULL, ky INT NOT NULL, id_char INT NOT NULL,"
            + " hang INT NOT NULL DEFAULT 0, luong_khoa INT NOT NULL DEFAULT 0, danh_hieu INT NOT NULL DEFAULT 0,"
            + " phut INT NOT NULL DEFAULT 0, da_nhan TINYINT NOT NULL DEFAULT 0, luc DATETIME NOT NULL, nhan_luc DATETIME NULL,"
            + " PRIMARY KEY (loai, ky, id_char), KEY k_cho (id_char, da_nhan)) ENGINE=InnoDB DEFAULT CHARSET=utf8"
    };

    static final Kho KHO_CSDL = new Kho() {
        public void taoBang() {
            for (final String s : SQL_TAO_BANG) {
                ghi(s);
            }
        }

        public int[] napLuot(final int ngay, final int userId) {
            final int[] r = {0, 0, 0};
            doc("SELECT so_tran, thang_thuong, luong FROM tob_pk3v3_luot WHERE ngay = ? AND user_id = ?", rs -> {
                r[0] = rs.getInt(1);
                r[1] = rs.getInt(2);
                r[2] = rs.getInt(3);
            }, Integer.valueOf(ngay), Integer.valueOf(userId));
            return r;
        }

        public int napLuongTuan(final int tuan, final int userId) {
            final int[] r = {0};
            doc("SELECT COALESCE(SUM(luong), 0) FROM tob_pk3v3_luot WHERE tuan = ? AND user_id = ?", rs -> r[0] = rs.getInt(1),
                    Integer.valueOf(tuan), Integer.valueOf(userId));
            return r[0];
        }

        public void congLuot(final int ngay, final int tuan, final int userId, final int dTran, final int dThang, final int dLuong) {
            ghi("INSERT INTO tob_pk3v3_luot (ngay, user_id, tuan, so_tran, thang_thuong, luong, cap_nhat) VALUES (?, ?, ?, ?, ?, ?, NOW())"
                    + " ON DUPLICATE KEY UPDATE so_tran = so_tran + VALUES(so_tran), thang_thuong = thang_thuong + VALUES(thang_thuong),"
                    + " luong = luong + VALUES(luong), cap_nhat = NOW()", Integer.valueOf(ngay), Integer.valueOf(userId), Integer.valueOf(tuan),
                    Integer.valueOf(dTran), Integer.valueOf(dThang), Integer.valueOf(dLuong));
        }

        public List<DongDiem> napDiem(final int tuTuan) {
            final List<DongDiem> ds = new ArrayList<DongDiem>();
            doc("SELECT ngay, tuan, id_char, user_id, ten, la_bot, diem, thang, hoa, thua, hop_le, UNIX_TIMESTAMP(cap_nhat)"
                    + " FROM tob_pk3v3_diem WHERE tuan >= ?", rs -> {
                        final Pk3v3.Diem d = new Pk3v3.Diem(rs.getInt(3));
                        d.userId = rs.getInt(4);
                        d.ten = rs.getString(5) == null ? "" : rs.getString(5);
                        d.bot = rs.getInt(6) == 1;
                        d.diem = rs.getInt(7);
                        d.thang = rs.getInt(8);
                        d.hoa = rs.getInt(9);
                        d.thua = rs.getInt(10);
                        d.hopLe = rs.getInt(11);
                        d.luc = rs.getLong(12) * 1000L;
                        ds.add(new DongDiem(rs.getInt(1), rs.getInt(2), d));
                    }, Integer.valueOf(tuTuan));
            return ds;
        }

        public void congDiem(final int ngay, final int tuan, final Pk3v3.Diem x) {
            ghi("INSERT INTO tob_pk3v3_diem (ngay, id_char, tuan, user_id, ten, la_bot, diem, thang, hoa, thua, hop_le, cap_nhat)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE ten = VALUES(ten), la_bot = VALUES(la_bot),"
                    + " diem = diem + VALUES(diem), thang = thang + VALUES(thang), hoa = hoa + VALUES(hoa), thua = thua + VALUES(thua),"
                    + " hop_le = hop_le + VALUES(hop_le), cap_nhat = NOW()", Integer.valueOf(ngay), Integer.valueOf(x.charId),
                    Integer.valueOf(tuan), Integer.valueOf(x.userId), cat(x.ten, 32), Integer.valueOf(x.bot ? 1 : 0), Integer.valueOf(x.diem),
                    Integer.valueOf(x.thang), Integer.valueOf(x.hoa), Integer.valueOf(x.thua), Integer.valueOf(x.hopLe));
        }

        public java.util.Map<String, Integer> napGap(final int ngay) {
            final java.util.Map<String, Integer> m = new HashMap<String, Integer>();
            doc("SELECT a, b, so FROM tob_pk3v3_gap WHERE ngay = ?", rs -> m.put(cap(rs.getInt(1), rs.getInt(2)), Integer.valueOf(rs.getInt(3))),
                    Integer.valueOf(ngay));
            return m;
        }

        public void congGap(final int ngay, final int a, final int b) {
            ghi("INSERT INTO tob_pk3v3_gap (ngay, a, b, so) VALUES (?, ?, ?, 1) ON DUPLICATE KEY UPDATE so = so + 1",
                    Integer.valueOf(ngay), Integer.valueOf(Math.min(a, b)), Integer.valueOf(Math.max(a, b)));
        }

        public void ghiTran(final int ngay, final int tuan, final int bac, final String doiA, final String doiB, final int ketQua,
                final int giay, final String lyDo) {
            ghi("INSERT INTO tob_pk3v3_tran (ngay, tuan, bac, doi_a, doi_b, ket_qua, giay, ly_do, luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                    Integer.valueOf(ngay), Integer.valueOf(tuan), Integer.valueOf(bac), cat(doiA, 64), cat(doiB, 64), Integer.valueOf(ketQua),
                    Integer.valueOf(giay), cat(lyDo == null ? "" : lyDo, 16));
        }

        public boolean chot(final String loai, final int ky) {
            return ghi("INSERT IGNORE INTO tob_pk3v3_chot (loai, ky, luc) VALUES (?, ?, NOW())", loai, Integer.valueOf(ky)) == 1;
        }

        public void ghiThuong(final String loai, final int ky, final int charId, final int hang, final int luongKhoa, final int danhHieu,
                final int phut) {
            ghi("INSERT IGNORE INTO tob_pk3v3_thuong (loai, ky, id_char, hang, luong_khoa, danh_hieu, phut, da_nhan, luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, 0, NOW())", loai, Integer.valueOf(ky), Integer.valueOf(charId), Integer.valueOf(hang),
                    Integer.valueOf(luongKhoa), Integer.valueOf(danhHieu), Integer.valueOf(phut));
        }

        public List<Object[]> thuongCho(final int charId) {
            final List<Object[]> ds = new ArrayList<Object[]>();
            doc("SELECT loai, ky, hang, luong_khoa, danh_hieu, phut FROM tob_pk3v3_thuong WHERE id_char = ? AND da_nhan = 0",
                    rs -> ds.add(new Object[]{rs.getString(1), Integer.valueOf(rs.getInt(2)), Integer.valueOf(rs.getInt(3)),
                        Integer.valueOf(rs.getInt(4)), Integer.valueOf(rs.getInt(5)), Integer.valueOf(rs.getInt(6))}), Integer.valueOf(charId));
            return ds;
        }

        public boolean daNhan(final String loai, final int ky, final int charId) {
            return ghi("UPDATE tob_pk3v3_thuong SET da_nhan = 1, nhan_luc = NOW() WHERE loai = ? AND ky = ? AND id_char = ? AND da_nhan = 0",
                    loai, Integer.valueOf(ky), Integer.valueOf(charId)) == 1;
        }
    };

    static volatile Kho KHO = KHO_CSDL;

    static String cat(final String s, final int n) {
        return s == null ? "" : s.length() <= n ? s : s.substring(0, n);
    }

    interface Dong {
        void doc(ResultSet rs) throws Exception;
    }

    static void doc(final String sql, final Dong d, final Object... ts) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < ts.length; i++) {
                pre.setObject(i + 1, ts[i]);
            }
            rs = pre.executeQuery();
            while (rs.next()) {
                d.doc(rs);
            }
        } catch (final Throwable e) {
            System.out.println("[PK3V3] doc: " + e);
        } finally {
            DauTruongGame.dong(rs, pre, conn);
        }
    }

    static int ghi(final String sql, final Object... ts) {
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement(sql);
            for (int i = 0; i < ts.length; i++) {
                pre.setObject(i + 1, ts[i]);
            }
            return pre.executeUpdate();
        } catch (final Throwable e) {
            System.out.println("[PK3V3] ghi: " + e);
            return -1;
        } finally {
            DauTruongGame.dong(null, pre, conn);
        }
    }

    /** TeamServer luc khoi dong: tao bang tob_pk3v3_* neu thieu. */
    public static void taoBang() {
        try {
            KHO.taoBang();
        } catch (final Throwable e) {
            System.out.println("[PK3V3] tao bang: " + e);
        }
    }

    // ================================================================== trang thai (bo nho, nap lan dau tu kho)

    static final Object KHOA = new Object();
    /** "ngay:userId" -> {so_tran, thang_thuong, luong}. */
    static final HashMap<String, int[]> LUOT = new HashMap<String, int[]>();
    /** "tuan:userId" -> luong khoa PK 3v3 da nhan trong tuan. */
    static final HashMap<String, int[]> LUONG_TUAN = new HashMap<String, int[]>();
    /** ngay -> (charId -> diem ngay). */
    static final HashMap<Integer, LinkedHashMap<Integer, Pk3v3.Diem>> DIEM = new HashMap<Integer, LinkedHashMap<Integer, Pk3v3.Diem>>();
    /** ngay -> tuan cua ngay do. */
    static final HashMap<Integer, Integer> TUAN_CUA_NGAY = new HashMap<Integer, Integer>();
    static boolean daNapDiem;
    /** ngay -> ("a:b" -> so lan gap). */
    static final HashMap<Integer, java.util.Map<String, Integer>> GAP = new HashMap<Integer, java.util.Map<String, Integer>>();
    /** Moc da chot trong tien trinh nay ("ngay:20261105", "tuan:2netx"). */
    static final Set<String> DA_CHOT = new HashSet<String>();

    static String cap(final int a, final int b) {
        return Math.min(a, b) + ":" + Math.max(a, b);
    }

    static int[] luot(final int ngay, final int userId) {
        synchronized (KHOA) {
            final String k = ngay + ":" + userId;
            int[] r = LUOT.get(k);
            if (r == null) {
                final int[] n = userId > 0 ? KHO.napLuot(ngay, userId) : null;
                r = n == null || n.length < 3 ? new int[3] : n.clone();
                LUOT.put(k, r);
            }
            return r;
        }
    }

    static int[] luongTuan(final int tuan, final int userId) {
        synchronized (KHOA) {
            final String k = tuan + ":" + userId;
            int[] r = LUONG_TUAN.get(k);
            if (r == null) {
                r = new int[]{userId > 0 ? KHO.napLuongTuan(tuan, userId) : 0};
                LUONG_TUAN.put(k, r);
            }
            return r;
        }
    }

    static void napDiem(final long bay) {
        synchronized (KHOA) {
            if (daNapDiem) {
                return;
            }
            daNapDiem = true;
            for (final DongDiem r : KHO.napDiem(Pk3v3.tuan(bay) - 1)) {
                bangNgay(r.ngay, r.tuan).put(Integer.valueOf(r.d.charId), r.d);
            }
        }
    }

    static LinkedHashMap<Integer, Pk3v3.Diem> bangNgay(final int ngay, final int tuan) {
        synchronized (KHOA) {
            LinkedHashMap<Integer, Pk3v3.Diem> m = DIEM.get(Integer.valueOf(ngay));
            if (m == null) {
                m = new LinkedHashMap<Integer, Pk3v3.Diem>();
                DIEM.put(Integer.valueOf(ngay), m);
                TUAN_CUA_NGAY.put(Integer.valueOf(ngay), Integer.valueOf(tuan));
            }
            return m;
        }
    }

    static java.util.Map<String, Integer> gap(final int ngay) {
        synchronized (KHOA) {
            java.util.Map<String, Integer> m = GAP.get(Integer.valueOf(ngay));
            if (m == null) {
                final java.util.Map<String, Integer> n = KHO.napGap(ngay);
                m = n == null ? new HashMap<String, Integer>() : new HashMap<String, Integer>(n);
                GAP.put(Integer.valueOf(ngay), m);
            }
            return m;
        }
    }

    static int lanGap(final int ngay, final int a, final int b) {
        synchronized (KHOA) {
            final Integer n = gap(ngay).get(cap(a, b));
            return n == null ? 0 : n.intValue();
        }
    }

    /** Bang ngay (da xep, bo bot). */
    static List<Pk3v3.Diem> xepNgay(final int ngay) {
        synchronized (KHOA) {
            final LinkedHashMap<Integer, Pk3v3.Diem> m = DIEM.get(Integer.valueOf(ngay));
            return Pk3v3.xepHang(m == null ? new ArrayList<Pk3v3.Diem>() : new ArrayList<Pk3v3.Diem>(m.values()));
        }
    }

    /** Bang tuan = cong moi ngay cua tuan (da xep, bo bot). */
    static List<Pk3v3.Diem> xepTuan(final int tuan) {
        synchronized (KHOA) {
            final LinkedHashMap<Integer, Pk3v3.Diem> gop = new LinkedHashMap<Integer, Pk3v3.Diem>();
            for (final java.util.Map.Entry<Integer, LinkedHashMap<Integer, Pk3v3.Diem>> e : DIEM.entrySet()) {
                final Integer t = TUAN_CUA_NGAY.get(e.getKey());
                if (t == null || t.intValue() != tuan) {
                    continue;
                }
                for (final Pk3v3.Diem d : e.getValue().values()) {
                    Pk3v3.Diem g = gop.get(Integer.valueOf(d.charId));
                    if (g == null) {
                        g = new Pk3v3.Diem(d.charId);
                        gop.put(Integer.valueOf(d.charId), g);
                    }
                    g.cong(d);
                }
            }
            return Pk3v3.xepHang(new ArrayList<Pk3v3.Diem>(gop.values()));
        }
    }

    // ================================================================== nhan vat

    static boolean laBot(final Char c) {
        return HlvAiGame.laBot(c);
    }

    /** Dang ban viec khac (khong vao hang / khong ghep duoc). null = ranh. */
    static String ban(final Char c) {
        if (c == null) {
            return "không online";
        }
        if (c.hp <= 0) {
            return "đang gục";
        }
        try {
            if (PhongRiengGame.dangBan(c) || PhoBanGame.phongCua(c.charDBID) != null) {
                return "đang ở phó bản / phòng riêng hoặc đang giao dịch";
            }
            if (CupGame.dangThiDau(c.charDBID) || XepHangPvpGame.trongTran(c) || XepHangPvpGame.dangCho(c)
                    || DauTruongGame.TRAN.containsKey(Integer.valueOf(c.charDBID))) {
                return "đang ở trận / hàng chờ PvP khác";
            }
        } catch (final Throwable ignored) {
        }
        return null;
    }

    // ================================================================== hang cho

    static final class Cho {
        final Pk3v3.Nhom nhom;
        final String[] ten;

        Cho(final Pk3v3.Nhom nhom, final String[] ten) {
            this.nhom = nhom;
            this.ten = ten;
        }
    }

    /** id nhom (charId nguoi bam) -> nhom dang cho. */
    static final LinkedHashMap<Integer, Cho> HANG = new LinkedHashMap<Integer, Cho>();

    static Cho choCua(final int charId) {
        synchronized (KHOA) {
            for (final Cho c : HANG.values()) {
                if (c.nhom.co(charId)) {
                    return c;
                }
            }
            return null;
        }
    }

    public static boolean dangCho(final Char p) {
        return p != null && choCua(p.charDBID) != null;
    }

    /** Vao hang cho: mot minh ({@code caDoi} false) hoac ca to doi. null = da vao, nguoc lai cau bao. */
    static String vaoHang(final Char p, final boolean caDoi, final long bay) {
        if (p == null || p.charDBID <= 0 || p.isBot != -1) {
            return "Không vào được.";
        }
        if (!bat()) {
            return Pk3v3.TEN + " đang tạm đóng.";
        }
        if (!dangMo(bay)) {
            return Pk3v3.TEN + " chỉ mở " + khung() + " mỗi ngày.";
        }
        if (dangThiDau(p.charDBID)) {
            return "Bạn đang trong một trận 3 vs 3.";
        }
        if (choCua(p.charDBID) != null) {
            return "Bạn đã ở trong hàng chờ.";
        }
        final List<Char> ds = new ArrayList<Char>();
        if (caDoi) {
            if (p.partyID == -1) {
                return "Bạn chưa có tổ đội: hãy chọn «Vào hàng chờ một mình» hoặc lập tổ đội 2–3 người.";
            }
            if (!TRUONG.test(p)) {
                return "Chỉ đội trưởng mới cho cả tổ đội vào hàng chờ.";
            }
            ds.addAll(DOI.apply(p));
            if (ds.size() > Pk3v3.SO_NGUOI) {
                return "Tổ đội tối đa " + Pk3v3.SO_NGUOI + " người đang online mới vào được 3 vs 3.";
            }
            if (ds.size() < 2) {
                return "Tổ đội cần ít nhất 2 người đang online (hoặc vào hàng chờ một mình).";
            }
        } else {
            ds.add(p);
        }
        final int capMin = capToiThieu();
        final int ngay = Pk3v3.ngay(bay);
        int capCao = 0;
        final Set<String> khoaTrong = new HashSet<String>();
        final Set<String> khoaNgoai = new HashSet<String>();
        final int[] ids = new int[ds.size()];
        final String[] ten = new String[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            final Char c = ds.get(i);
            final String ai = c == p ? "Bạn" : c.charname;
            if (c.isBot != -1) {
                return ai + " không vào được.";
            }
            if (c.lvDetail == null || c.lvDetail.lv < capMin) {
                return ai + " cần cấp " + capMin + " để vào " + Pk3v3.TEN + ".";
            }
            final String b = ban(c);
            if (b != null) {
                return ai + " " + b + ".";
            }
            if (choCua(c.charDBID) != null || dangThiDau(c.charDBID)) {
                return ai + " đã ở hàng chờ / trận 3 vs 3.";
            }
            if (luot(ngay, c.userID)[0] >= tranNgay()) {
                return ai + " đã hết " + tranNgay() + " trận hôm nay (tính theo tài khoản).";
            }
            final ChongCloneLogic.Ben be = ChongClone.ben(c, false);
            final Set<String> kt = CupLogic.khoaClone(be.may, "", c.userID);
            if (CupLogic.trungClone(kt, khoaTrong)) {
                return "Hai thành viên của tổ đội không được dùng chung thiết bị / tài khoản.";
            }
            khoaTrong.addAll(kt);
            khoaNgoai.addAll(CupLogic.khoaClone(be.may, be.ip, c.userID));
            capCao = Math.max(capCao, c.lvDetail.lv);
            ids[i] = c.charDBID;
            ten[i] = c.charname;
        }
        final int bac = Pk3v3.bac(capCao, capMin);
        synchronized (KHOA) {
            for (final int id : ids) {
                if (choCua(id) != null) {
                    return "Đã có người của tổ đội trong hàng chờ.";
                }
            }
            HANG.put(Integer.valueOf(p.charDBID), new Cho(new Pk3v3.Nhom(ids, bac, bay, khoaTrong, khoaNgoai), ten));
        }
        final String bacChu = Pk3v3.tenBac(bac, capMin);
        for (final Char c : ds) {
            PhongRiengGame.bao(c, "Đã vào hàng chờ " + Pk3v3.TEN + (ds.size() > 1 ? " (cả tổ đội " + ds.size() + " người)" : ""),
                    "Ghép 3 đấu 3 cùng bậc " + bacChu + ". Chọn lại mục " + Pk3v3.TEN + " để rời hàng.");
        }
        return null;
    }

    /** Rot ca nhom chua {@code charId} khoi hang, bao tung nguoi. */
    static boolean roiHang(final int charId, final String vi) {
        final Cho c;
        synchronized (KHOA) {
            c = choCua(charId);
            if (c == null) {
                return false;
            }
            HANG.values().remove(c);
        }
        for (final int id : c.nhom.ids) {
            final Char x = SAN.online(id);
            if (x != null) {
                PhongRiengGame.bao(x, "Đã rời hàng chờ " + Pk3v3.TEN, vi == null ? "" : vi);
            }
        }
        return true;
    }

    // ================================================================== tran

    static final class Tran {
        final int khu, bac, ngay, tuan;
        final List<Integer> a = new ArrayList<Integer>(), b = new ArrayList<Integer>();
        final java.util.Map<Integer, PhongRiengGame.ViTri> cu = new HashMap<Integer, PhongRiengGame.ViTri>();
        final Set<Integer> loai = ConcurrentHashMap.newKeySet();
        /** Phan nghin sat thuong (theo HP toi da muc tieu) moi nguoi gay ca tran. */
        final ConcurrentHashMap<Integer, Integer> st = new ConcurrentHashMap<Integer, Integer>();
        final java.util.Map<Integer, Integer> gapTruoc = new HashMap<Integer, Integer>();
        final java.util.Map<Integer, Integer> user = new HashMap<Integer, Integer>();
        final java.util.Map<Integer, String> ten = new HashMap<Integer, String>();
        final Set<Integer> bot = new HashSet<Integer>();
        final long batDau;
        volatile int ketQua;
        volatile long ketThucLuc;

        Tran(final int khu, final int bac, final long batDau) {
            this.khu = khu;
            this.bac = bac;
            this.batDau = batDau;
            this.ngay = Pk3v3.ngay(batDau);
            this.tuan = Pk3v3.tuan(batDau);
        }

        boolean coMat(final int id) {
            return a.contains(Integer.valueOf(id)) || b.contains(Integer.valueOf(id));
        }

        boolean cungDoi(final int x, final int y) {
            return a.contains(Integer.valueOf(x)) && a.contains(Integer.valueOf(y)) || b.contains(Integer.valueOf(x)) && b.contains(Integer.valueOf(y));
        }

        String tenDoi(final List<Integer> phe) {
            final StringBuilder s = new StringBuilder();
            for (final Integer id : phe) {
                s.append(s.length() == 0 ? "" : ", ").append(ten.get(id) == null ? "?" : ten.get(id));
            }
            return s.toString();
        }

        String idDoi(final List<Integer> phe) {
            final StringBuilder s = new StringBuilder();
            for (final Integer id : phe) {
                s.append(s.length() == 0 ? "" : ",").append(id);
            }
            return s.toString();
        }
    }

    /** khu -> tran. */
    static final ConcurrentHashMap<Integer, Tran> TRAN = new ConcurrentHashMap<Integer, Tran>();

    static Tran tranCua(final int charId) {
        for (final Tran t : TRAN.values()) {
            if (t.ketQua == 0 && t.coMat(charId)) {
                return t;
            }
        }
        return null;
    }

    public static boolean dangThiDau(final int charId) {
        return tranCua(charId) != null;
    }

    /** p dang dung trong khu cua mot tran 3v3 chua ket thuc (ngu canh PvP ngam tay). */
    public static boolean dangThiDau(final Char p) {
        if (p == null || p.mapID != SAN.mapId()) {
            return false;
        }
        final Tran t = TRAN.get(Integer.valueOf(p.region));
        return t != null && t.ketQua == 0 && t.coMat(p.charDBID);
    }

    /** Khu {@code khu} cua san la tran 3v3 (Region chon nhanh 3v3 thay vi cup). */
    public static boolean laKhu(final Char p) {
        return p != null && p.mapID == SAN.mapId() && TRAN.containsKey(Integer.valueOf(p.region));
    }

    /** HOOK Region.doAttackPlayer (san phong rieng): p danh duoc c (cung tran, khac doi, ca hai chua bi loai, het chuan bi). */
    public static boolean choDanh(final Char p, final Char c) {
        if (p == null || c == null || p.mapID != SAN.mapId() || c.mapID != p.mapID || c.region != p.region) {
            return false;
        }
        final Tran t = TRAN.get(Integer.valueOf(p.region));
        if (t == null || t.ketQua != 0 || GIO.getAsLong() - t.batDau < Pk3v3.GIAY_CHUAN_BI * 1000L) {
            return false;
        }
        return t.coMat(p.charDBID) && t.coMat(c.charDBID) && !t.cungDoi(p.charDBID, c.charDBID)
                && !t.loai.contains(Integer.valueOf(p.charDBID)) && !t.loai.contains(Integer.valueOf(c.charDBID));
    }

    /** HOOK Region.doAttackPlayer: sat thuong chuan hoa (nhu Cup, 10..80 phan nghin HP muc tieu) + ghi sat thuong da gay. */
    public static int satThuong(final Char p, final Char c, final long d) {
        final long tho = d * com.htbb.pokemon.SatThuongLauDai.PVP_PHAN_TRAM / 100L;
        final int dam = CupLogic.satThuong(tho, Math.max(1, p.maxhp), Math.max(1, c.maxhp), Pk3v3.PHAN_NGHIN_MIN, Pk3v3.PHAN_NGHIN_MAX);
        ghiSatThuong(p, c, dam);
        return dam;
    }

    static void ghiSatThuong(final Char p, final Char c, final int dam) {
        final Tran t = TRAN.get(Integer.valueOf(p.region));
        if (t == null || !t.coMat(p.charDBID)) {
            return;
        }
        final int pn = (int) Math.min(1000L, (long) Math.max(0, Math.min(dam, Math.max(0, c.hp))) * 1000L / Math.max(1, c.maxhp));
        t.st.merge(Integer.valueOf(p.charDBID), Integer.valueOf(pn), Integer::sum);
    }

    /**
     * HOOK Region.doAttackPlayer sau khi tru HP: khu cua tran 3v3 thi het HP = bi loai (HP = 1), doi het nguoi thi ket
     * thuc. @return true neu c thuoc mot tran 3v3 (Region khong goi CupGame.sauDon nua).
     */
    public static boolean sauDon(final Char p, final Char c) {
        final Tran t = TRAN.get(Integer.valueOf(c.region));
        if (t == null || c.mapID != SAN.mapId() || !t.coMat(c.charDBID)) {
            return false;
        }
        if (c.hp > 0) {
            return true;
        }
        c.hp = 1;
        if (t.loai.add(Integer.valueOf(c.charDBID))) {
            PhongRiengGame.loi(c, "Bạn đã bị loại khỏi trận 3 vs 3 - đồng đội đánh tiếp");
        }
        kiemHetNguoi(t, GIO.getAsLong());
        return true;
    }

    /** Bo cuoc giua tran: bi loai (doi danh tiep thieu nguoi; het nguoi thi thua). */
    static void boCuoc(final Char p, final long bay) {
        final Tran t = tranCua(p.charDBID);
        if (t == null) {
            return;
        }
        if (t.loai.add(Integer.valueOf(p.charDBID))) {
            PhongRiengGame.loi(p, "Bạn đã bỏ cuộc trận 3 vs 3");
        }
        kiemHetNguoi(t, bay);
    }

    static void kiemHetNguoi(final Tran t, final long bay) {
        boolean hetA = true;
        boolean hetB = true;
        for (final Integer id : t.a) {
            hetA &= t.loai.contains(id);
        }
        for (final Integer id : t.b) {
            hetB &= t.loai.contains(id);
        }
        if (hetA || hetB) {
            ketThuc(t, hetA && hetB ? Pk3v3.HOA : hetA ? Pk3v3.B_THANG : Pk3v3.A_THANG, bay);
        }
    }

    /** Bat dau tran tu hai doi da ghep. false = khong bat dau (nhom khong san sang da bi rut khoi hang / het san). */
    static boolean batDau(final Pk3v3.Doi da, final Pk3v3.Doi db, final long bay) {
        final List<Pk3v3.Doi> hai = Arrays.asList(da, db);
        final int ngay = Pk3v3.ngay(bay);
        final int tran = tranNgay();
        boolean du = true;
        for (final Pk3v3.Doi d : hai) {
            for (final Pk3v3.Nhom g : d.nhom) {
                for (final int id : g.ids) {
                    final Char c = SAN.online(id);
                    final String b = ban(c);
                    String loi = b;
                    if (loi == null && luot(ngay, c.userID)[0] >= tran) {
                        loi = "đã hết " + tran + " trận hôm nay";
                    }
                    if (loi != null) {
                        du = false;
                        roiHang(id, (c != null ? c.charname : "Một thành viên") + " " + loi + " - hãy vào lại hàng chờ.");
                        break;
                    }
                }
            }
        }
        if (!du) {
            return false;
        }
        final int khu = SAN.muon();
        if (khu < 0) {
            System.out.println("[PK3V3] het khu san trong");
            return false;
        }
        final Tran t = new Tran(khu, da.bac, bay);
        synchronized (KHOA) {
            for (final Pk3v3.Doi d : hai) {
                for (final Pk3v3.Nhom g : d.nhom) {
                    final java.util.Iterator<Cho> it = HANG.values().iterator();
                    while (it.hasNext()) {
                        if (it.next().nhom == g) {
                            it.remove();
                        }
                    }
                }
            }
        }
        t.a.addAll(da.ids());
        t.b.addAll(db.ids());
        for (final Integer id : t.a) {
            t.gapTruoc.put(id, Integer.valueOf(Pk3v3.gapNhieuNhat(Arrays.asList(id), t.b, (x, y) -> lanGap(ngay, x, y))));
        }
        for (final Integer id : t.b) {
            t.gapTruoc.put(id, Integer.valueOf(Pk3v3.gapNhieuNhat(Arrays.asList(id), t.a, (x, y) -> lanGap(ngay, x, y))));
        }
        synchronized (KHOA) {
            final java.util.Map<String, Integer> g = gap(ngay);
            for (final Integer x : t.a) {
                for (final Integer y : t.b) {
                    g.merge(cap(x.intValue(), y.intValue()), Integer.valueOf(1), Integer::sum);
                    KHO.congGap(ngay, x.intValue(), y.intValue());
                }
            }
        }
        final String tenA;
        final String tenB;
        for (final List<Integer> phe : Arrays.asList(t.a, t.b)) {
            for (final Integer id : phe) {
                final Char c = SAN.online(id.intValue());
                t.ten.put(id, c == null ? "?" : c.charname);
                if (c != null) {
                    t.user.put(id, Integer.valueOf(c.userID));
                    if (laBot(c)) {
                        t.bot.add(id);
                    }
                    synchronized (KHOA) {
                        luot(ngay, c.userID)[0]++;
                    }
                    KHO.congLuot(ngay, t.tuan, c.userID, 1, 0, 0);
                }
            }
        }
        tenA = t.tenDoi(t.a);
        tenB = t.tenDoi(t.b);
        TRAN.put(Integer.valueOf(khu), t); // sau khi du ten / tai khoan / bot: luong ban do doc an toan
        for (final List<Integer> phe : Arrays.asList(t.a, t.b)) {
            int thuTu = 0;
            for (final Integer id : phe) {
                final Char c = SAN.online(id.intValue());
                if (c == null) {
                    t.loai.add(id);
                    continue;
                }
                t.cu.put(id, new PhongRiengGame.ViTri(c));
                try {
                    TuDongGame.khiPvp(c);
                } catch (final Throwable ignored) {
                }
                c.hp = c.maxhp;
                c.mp = c.maxmp;
                SAN.dua(c, khu, phe == t.a ? 0 : 1, thuTu++);
                try {
                    c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                } catch (final Throwable ignored) {
                }
                final int[] l = luot(ngay, c.userID);
                PhongRiengGame.bao(c, Pk3v3.TEN + ": " + (phe == t.a ? tenA : tenB) + " gặp " + (phe == t.a ? tenB : tenA),
                        "Bắt đầu sau " + Pk3v3.GIAY_CHUAN_BI + " giây, tối đa " + (Pk3v3.GIAY_TRAN / 60) + " phút. Trận hôm nay: " + l[0] + "/" + tran);
                PhongRiengGame.guiThe(c, PhongRieng.CUP, Pk3v3.TEN, tenA + " vs " + tenB, "Hết giờ: đội còn nhiều % HP hơn thắng",
                        Pk3v3.GIAY_TRAN + Pk3v3.GIAY_CHUAN_BI, -1);
            }
        }
        System.out.println("[PK3V3] khu " + khu + " bac " + t.bac + ": " + tenA + " vs " + tenB);
        kiemHetNguoi(t, bay);
        return true;
    }

    static int tongHp(final Tran t, final List<Integer> phe) {
        int s = 0;
        for (final Integer id : phe) {
            final Char c = SAN.online(id.intValue());
            if (c != null && !t.loai.contains(id)) {
                s += CupLogic.phanNghinHp(c.hp, c.maxhp);
            }
        }
        return s;
    }

    static void nhipTran(final Tran t, final long bay) {
        if (t.ketQua != 0) {
            if (bay - t.ketThucLuc >= 4000L) {
                dongTran(t);
            }
            return;
        }
        for (final List<Integer> phe : Arrays.asList(t.a, t.b)) {
            for (final Integer id : phe) {
                if (t.loai.contains(id)) {
                    continue;
                }
                final Char c = SAN.online(id.intValue());
                if (c != null && c.hp <= 0 && c.mapID == SAN.mapId() && c.region == t.khu) {
                    c.hp = 1; // het HP ngoai don danh (hieu ung theo thoi gian...): bi loai nhu sauDon, khong guc
                    t.loai.add(id);
                    PhongRiengGame.loi(c, "Bạn đã bị loại khỏi trận 3 vs 3 - đồng đội đánh tiếp");
                    continue;
                }
                if (c == null || c.mapID != SAN.mapId() || c.region != t.khu) {
                    t.loai.add(id); // rot mang / roi san = bi loai, doi danh tiep thieu nguoi
                    for (final Integer x : phe) {
                        final Char dd = x.equals(id) ? null : SAN.online(x.intValue());
                        if (dd != null && !t.loai.contains(x)) {
                            PhongRiengGame.loi(dd, (t.ten.get(id) == null ? "Đồng đội" : t.ten.get(id)) + " đã rời trận - đội đánh tiếp thiếu người");
                        }
                    }
                }
            }
        }
        kiemHetNguoi(t, bay);
        if (t.ketQua == 0 && bay - t.batDau >= (Pk3v3.GIAY_TRAN + Pk3v3.GIAY_CHUAN_BI) * 1000L) {
            ketThuc(t, CupLogic.ketQuaHetGio(tongHp(t, t.a), tongHp(t, t.b)), bay);
        }
    }

    static int tongSt(final Tran t, final List<Integer> phe) {
        int s = 0;
        for (final Integer id : phe) {
            final Integer v = t.st.get(id);
            s += v == null ? 0 : v.intValue();
        }
        return s;
    }

    static void ketThuc(final Tran t, final int kq, final long bay) {
        synchronized (t) {
            if (t.ketQua != 0) {
                return;
            }
            t.ketQua = kq;
            t.ketThucLuc = bay;
        }
        final int giay = (int) Math.max(0L, (bay - t.batDau) / 1000L - Pk3v3.GIAY_CHUAN_BI);
        final int stThua = kq == Pk3v3.A_THANG ? tongSt(t, t.b) : kq == Pk3v3.B_THANG ? tongSt(t, t.a) : 0;
        final String lyDoTran = Pk3v3.lyDoTran(kq, giay, stThua, giayToiThieu());
        final String chu = kq == Pk3v3.HOA ? "Hoà" : "Đội " + (kq == Pk3v3.A_THANG ? t.tenDoi(t.a) : t.tenDoi(t.b)) + " thắng";
        for (final List<Integer> phe : Arrays.asList(t.a, t.b)) {
            final int kqMinh = kq == Pk3v3.HOA ? 0 : (kq == Pk3v3.A_THANG) == (phe == t.a) ? 1 : -1;
            for (final Integer id : phe) {
                try {
                    xetNguoi(t, id.intValue(), kqMinh, lyDoTran, chu, bay);
                } catch (final Throwable e) {
                    System.out.println("[PK3V3] xet " + id + ": " + e);
                }
            }
        }
        KHO.ghiTran(t.ngay, t.tuan, t.bac, t.idDoi(t.a), t.idDoi(t.b), kq, giay, lyDoTran == null ? "" : lyDoTran);
    }

    static void xetNguoi(final Tran t, final int id, final int kqMinh, final String lyDoTran, final String chu, final long bay) {
        final Integer k = Integer.valueOf(id);
        final Integer uid = t.user.get(k);
        final int userId = uid == null ? 0 : uid.intValue();
        final Integer st = t.st.get(k);
        final Integer g = t.gapTruoc.get(k);
        final Char c = SAN.online(id);
        final boolean bot = t.bot.contains(k);
        final int[] l;
        final int[] lt;
        final Pk3v3.Xet x;
        synchronized (KHOA) {
            l = luot(t.ngay, userId);
            lt = luongTuan(t.tuan, userId);
            x = Pk3v3.xet(kqMinh, lyDoTran, st == null ? 0 : st.intValue(), bot, g == null ? 0 : g.intValue(), l[1], lt[0], luongThang(),
                    thangThuongNgay(), tranLuongTuan(), gapLaiToiDa());
        }
        // diem ngay
        final Pk3v3.Diem delta = new Pk3v3.Diem(id);
        delta.userId = userId;
        delta.ten = t.ten.get(k) == null ? "" : t.ten.get(k);
        delta.bot = bot;
        delta.luc = bay;
        if (x.hopLe()) {
            delta.diem = x.diem;
            delta.hopLe = 1;
            if (kqMinh > 0) {
                delta.thang = 1;
            } else if (kqMinh == 0) {
                delta.hoa = 1;
            } else {
                delta.thua = 1;
            }
        }
        synchronized (KHOA) {
            final LinkedHashMap<Integer, Pk3v3.Diem> m = bangNgay(t.ngay, t.tuan);
            Pk3v3.Diem d = m.get(k);
            if (d == null) {
                d = new Pk3v3.Diem(id);
                m.put(k, d);
            }
            d.cong(delta);
        }
        KHO.congDiem(t.ngay, t.tuan, delta);
        // thuong: chi nguoi con online luc het tran (rot mang = bi loai, khong nhan luong)
        int lk = 0;
        if (x.thangTinhThuong && c != null) {
            lk = x.luong;
            synchronized (KHOA) {
                l[1]++;
                l[2] += lk;
                lt[0] += lk;
            }
            KHO.congLuot(t.ngay, t.tuan, userId, 0, 1, lk);
            if (lk > 0) {
                c.addLuongLock(lk);
                try {
                    Database.instance.saveOrtherLog("", c.charname, "pk3v3 thang +" + lk + " luong khoa", "laudai");
                } catch (final Throwable ignored) {
                }
            }
        }
        if (c == null) {
            return;
        }
        final String ly = Pk3v3.lyDoChu(x.lyDo);
        final String phu = chu + (x.diem > 0 ? " · +" + x.diem + " điểm" : "") + (lk > 0 ? " · +" + lk + " lượng khoá" : "")
                + (ly.isEmpty() ? "" : " · " + ly);
        if (kqMinh > 0) {
            PhongRiengGame.hiem(c, "Thắng trận 3 vs 3!", phu);
        } else if (kqMinh == 0) {
            PhongRiengGame.bao(c, "Trận 3 vs 3: hoà", phu);
        } else {
            PhongRiengGame.loi(c, "Thua trận 3 vs 3 - " + phu);
        }
        PhongRiengGame.guiThe(c, PhongRieng.CUP, Pk3v3.TEN, chu, "Về chỗ cũ sau vài giây", -1, -1);
    }

    static void dongTran(final Tran t) {
        if (TRAN.remove(Integer.valueOf(t.khu)) == null) {
            return;
        }
        for (final List<Integer> phe : Arrays.asList(t.a, t.b)) {
            for (final Integer id : phe) {
                final Char c = SAN.online(id.intValue());
                if (c == null) {
                    continue;
                }
                PhongRiengGame.anThe(c);
                if (c.mapID == SAN.mapId() && c.region == t.khu) {
                    c.hp = c.maxhp;
                    c.mp = c.maxmp;
                    SAN.ve(c, t.cu.get(id));
                }
            }
        }
        SAN.tra(t.khu);
    }

    // ================================================================== nhip

    /** null = chua biet (lan nhip dau sau khoi dong: khong bao mo / dong). */
    static Boolean moTruoc;
    static int ngayTruoc = -1;
    static long ghepLuc;

    /** Nhip 1 giay (luong PhongRiengGame). */
    static void nhip(final long bay) {
        if (!LauDai.bat() || !SAN.co()) {
            return;
        }
        for (final Tran t : TRAN.values()) {
            try {
                nhipTran(t, bay);
            } catch (final Throwable e) {
                System.out.println("[PK3V3] nhipTran: " + e);
            }
        }
        napDiem(bay);
        final int ngay = Pk3v3.ngay(bay);
        if (ngayTruoc != ngay) {
            final int cu = ngayTruoc;
            ngayTruoc = ngay;
            donNgayCu(ngay);
            if (cu > 0) {
                chotNgay(cu);
            } else {
                chotNgay(Pk3v3.ngay(bay - 86400000L)); // khoi dong lai sau khi qua ngay: chot hom qua neu chua
            }
        }
        chotTuan(Pk3v3.tuan(bay) - 1);
        final boolean mo = dangMo(bay);
        if (moTruoc == null) {
            moTruoc = Boolean.valueOf(mo);
        } else if (moTruoc.booleanValue() != mo) {
            moTruoc = Boolean.valueOf(mo);
            if (mo) {
                loa(Pk3v3.TEN + " đã mở (" + khung() + ")! Vào hàng chờ ở Sổ tay huấn luyện / Menu › Sự kiện / NPC Đấu trường - đánh 3 đấu 3 cùng bậc cấp, thắng nhận lượng khoá.");
            } else {
                final List<Integer> ds;
                synchronized (KHOA) {
                    ds = new ArrayList<Integer>(HANG.keySet());
                }
                for (final Integer k : ds) {
                    roiHang(k.intValue(), "Hết giờ " + Pk3v3.TEN + " hôm nay (" + khung() + ").");
                }
                loa(Pk3v3.TEN + " hôm nay đã đóng. Hẹn gặp lại " + khung() + " ngày mai!");
            }
        }
        if (!mo && TRAN.isEmpty() && gioDong() > gioMo() && Pk3v3.gio(bay) >= gioDong()) {
            chotNgay(ngay);
        }
        if (!mo) {
            return;
        }
        // hang cho: bo nguoi offline / ban / cho qua lau
        final List<Cho> ds;
        synchronized (KHOA) {
            ds = new ArrayList<Cho>(HANG.values());
        }
        for (final Cho c : ds) {
            if (bay - c.nhom.vao > Pk3v3.GIAY_CHO_TOI_DA * 1000L) {
                roiHang(c.nhom.ids[0], "Chờ quá " + (Pk3v3.GIAY_CHO_TOI_DA / 60) + " phút chưa đủ người cùng bậc - hãy thử lại sau.");
                continue;
            }
            for (final int id : c.nhom.ids) {
                final Char x = SAN.online(id);
                if (x == null) {
                    roiHang(id, "Một thành viên đã thoát game.");
                    break;
                }
            }
        }
        if (bay - ghepLuc >= 3000L) {
            ghepLuc = bay;
            ghepNgay(bay);
        }
    }

    /** Ghep moi cap du dieu kien trong hang cho ngay bay gio. */
    static int ghepNgay(final long bay) {
        final List<Pk3v3.Nhom> ds = new ArrayList<Pk3v3.Nhom>();
        synchronized (KHOA) {
            for (final Cho c : HANG.values()) {
                ds.add(c.nhom);
            }
        }
        final int ngay = Pk3v3.ngay(bay);
        int n = 0;
        for (final Pk3v3.Doi[] cap : Pk3v3.ghep(ds, (x, y) -> lanGap(ngay, x, y), gapLaiToiDa())) {
            n += batDau(cap[0], cap[1], bay) ? 1 : 0;
        }
        return n;
    }

    /** Bo trang thai ngay cu (luot, gap) - giu diem 2 tuan cho bang tuan. */
    static void donNgayCu(final int ngay) {
        synchronized (KHOA) {
            final String tien = ngay + ":";
            LUOT.keySet().removeIf(k -> !k.startsWith(tien));
            GAP.keySet().removeIf(k -> k.intValue() != ngay);
            final Integer t = TUAN_CUA_NGAY.get(Integer.valueOf(ngay));
            if (t != null) {
                // giu diem tuan nay + tuan truoc (bang tuan, chot tuan sau khoi dong lai)
                DIEM.keySet().removeIf(k -> TUAN_CUA_NGAY.get(k) != null && TUAN_CUA_NGAY.get(k).intValue() < t.intValue() - 1);
            }
        }
    }

    // ================================================================== chot + thuong

    static boolean chot(final String loai, final int ky) {
        synchronized (KHOA) {
            if (!DA_CHOT.add(loai + ":" + ky)) {
                return false;
            }
        }
        return KHO.chot(loai, ky);
    }

    /** Chot bang ngay {@code ngay}: top 3 (du tran) nhan luong khoa (trong tran tuan), bao toan may chu. */
    static void chotNgay(final int ngay) {
        synchronized (KHOA) {
            if (DA_CHOT.contains("ngay:" + ngay)) {
                return;
            }
        }
        final List<Pk3v3.Diem> bang = xepNgay(ngay);
        if (bang.isEmpty()) {
            synchronized (KHOA) {
                DA_CHOT.add("ngay:" + ngay); // khong ai co tran hop le: khong co gi de phat, khong ghi moc CSDL
            }
            return;
        }
        if (!chot("ngay", ngay)) {
            return; // da chot (truoc khi khoi dong lai) / CSDL loi: khong phat lai
        }
        final Integer tuanObj;
        synchronized (KHOA) {
            tuanObj = TUAN_CUA_NGAY.get(Integer.valueOf(ngay));
        }
        final int tuan = tuanObj == null ? 0 : tuanObj.intValue();
        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < Math.min(Pk3v3.LK_TOP_NGAY.length, bang.size()); i++) {
            final Pk3v3.Diem d = bang.get(i);
            final int lk;
            synchronized (KHOA) {
                final int[] lt = luongTuan(tuan, d.userId);
                lk = Pk3v3.luongTopNgay(i + 1, d.hopLe, lt[0], tranLuongTuan());
                lt[0] += lk;
            }
            if (lk > 0) {
                KHO.congLuot(ngay, tuan, d.userId, 0, 0, lk);
                KHO.ghiThuong("ngay", ngay, d.charId, i + 1, lk, 0, 0);
                final Char c = SAN.online(d.charId);
                if (c != null) {
                    nhanThuongCho(c);
                }
            }
            s.append(s.length() == 0 ? "" : ", ").append(i + 1).append(". ").append(d.ten).append(" (").append(d.diem).append(" điểm)");
        }
        loa("Top " + Pk3v3.TEN + " hôm nay: " + s + ".");
    }

    /** Chot tuan {@code tuan}: hang 1 Vua PK 3 vs 3, hang 2-10 Dau si (7 ngay), du tran hop le. */
    static void chotTuan(final int tuan) {
        synchronized (KHOA) {
            if (DA_CHOT.contains("tuan:" + tuan)) {
                return;
            }
        }
        final List<Pk3v3.Diem> bang = xepTuan(tuan);
        if (bang.isEmpty()) {
            synchronized (KHOA) {
                DA_CHOT.add("tuan:" + tuan); // khong co du lieu tuan do trong bo nho (da nap 2 tuan): khong ghi moc CSDL
            }
            return;
        }
        if (!chot("tuan", tuan)) {
            return;
        }
        for (int i = 0; i < Math.min(Pk3v3.TOP_DAU_SI, bang.size()); i++) {
            final Pk3v3.Diem d = bang.get(i);
            final int dh = Pk3v3.danhHieuTuan(i + 1, d.hopLe);
            if (dh <= 0) {
                continue;
            }
            KHO.ghiThuong("tuan", tuan, d.charId, i + 1, 0, dh, Pk3v3.PHUT_DH);
            final Char c = SAN.online(d.charId);
            if (c != null) {
                nhanThuongCho(c);
            }
        }
        if (Pk3v3.danhHieuTuan(1, bang.get(0).hopLe) > 0) {
            loa("Vua " + Pk3v3.TEN.replace(" buổi tối", "") + " tuần này: " + bang.get(0).ten + " (" + bang.get(0).diem + " điểm)!");
        }
    }

    /** Phat moi thuong PK 3v3 con cho cua c (dang nhap / luc chot). Moi dong chi phat MOT lan (da_nhan). */
    static void nhanThuongCho(final Char c) {
        if (c == null || laBot(c)) {
            return;
        }
        for (final Object[] r : KHO.thuongCho(c.charDBID)) {
            final String loai = String.valueOf(r[0]);
            final int ky = ((Integer) r[1]).intValue();
            final int hang = ((Integer) r[2]).intValue();
            final int lk = ((Integer) r[3]).intValue();
            final int dh = ((Integer) r[4]).intValue();
            final int phut = ((Integer) r[5]).intValue();
            if (!KHO.daNhan(loai, ky, c.charDBID)) {
                continue; // da phat (luong khac / lan truoc) hoac CSDL loi: KHONG phat
            }
            if (lk > 0) {
                c.addLuongLock(lk);
            }
            if (dh > 0) {
                PhongRiengGame.traoDanhHieu(c, dh, phut);
            }
            try {
                Database.instance.saveOrtherLog("", c.charname, "pk3v3 thuong " + loai + " " + ky + " hang " + hang + " lk " + lk + " dh " + dh, "laudai");
            } catch (final Throwable ignored) {
            }
            PhongRiengGame.hiem(c, Pk3v3.TEN + ": hạng " + hang + ("tuan".equals(loai) ? " tuần" : " hôm " + (ky % 100) + "/" + (ky / 100 % 100)),
                    (lk > 0 ? "+" + lk + " lượng khoá" : "") + (dh > 0 ? (lk > 0 ? ", " : "") + "danh hiệu mới (7 ngày)" : ""));
        }
    }

    /** CharNapDuLieu sau khi vao game: thuong top ngay / danh hieu tuan chot luc offline. */
    public static void khiDangNhap(final Char c) {
        try {
            nhanThuongCho(c);
        } catch (final Throwable e) {
            System.out.println("Pk3v3Game.khiDangNhap: " + e);
        }
    }

    // ================================================================== menu

    /** Mo hoat dong (So tay muc PK 3v3, NPC Dau truong, Cup). */
    public static void moMenu(final Char p) {
        if (p == null) {
            return;
        }
        final long bay = GIO.getAsLong();
        final int ngay = Pk3v3.ngay(bay);
        final int[] l = luot(ngay, p.userID);
        final int[] lt = luongTuan(Pk3v3.tuan(bay), p.userID);
        final boolean mo = dangMo(bay);
        final StringBuilder b = new StringBuilder(Pk3v3.TEN).append('\n');
        if (!bat()) {
            b.append("Hoạt động đang tạm đóng.\n");
        } else {
            b.append(mo ? "ĐANG MỞ" : "Đang đóng").append(" · mở ").append(khung()).append(" mỗi ngày\n");
        }
        b.append("Trận hôm nay (tài khoản): ").append(l[0]).append("/").append(tranNgay()).append('\n')
                .append("Thắng có thưởng hôm nay: ").append(Math.min(l[1], thangThuongNgay())).append("/").append(thangThuongNgay())
                .append(" · lượng khoá tuần: ").append(lt[0]).append("/").append(tranLuongTuan()).append('\n');
        final List<Pk3v3.Diem> bang = xepNgay(ngay);
        for (int i = 0; i < bang.size(); i++) {
            if (bang.get(i).charId == p.charDBID) {
                b.append("Hạng hôm nay: ").append(i + 1).append(" (").append(bang.get(i).diem).append(" điểm)\n");
            }
        }
        final Tran t = tranCua(p.charDBID);
        if (t != null) {
            b.append("Bạn đang trong một trận.");
            p.sendMessage(MessageCreator.createServerAlertMessage(b.toString(), ""));
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Bỏ cuộc trận này (bị loại)"}, SoTayLauDai.NPC_ID, MENU_BO));
            return;
        }
        final Cho cho = choCua(p.charDBID);
        if (cho != null) {
            b.append("Đang chờ ghép (").append((bay - cho.nhom.vao) / 1000L).append(" giây).");
        } else if (mo) {
            b.append("Chọn vào hàng chờ một mình hoặc cả tổ đội (2–3 người).");
        }
        p.sendMessage(MessageCreator.createServerAlertMessage(b.toString(), ""));
        if (cho != null) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Rời hàng chờ", "Bảng xếp hạng hôm nay", "Bảng xếp hạng tuần",
                "Luật và phần thưởng"}, SoTayLauDai.NPC_ID, MENU_CHO));
        } else if (mo) {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Vào hàng chờ một mình", "Vào hàng chờ cả tổ đội",
                "Bảng xếp hạng hôm nay", "Bảng xếp hạng tuần", "Luật và phần thưởng"}, SoTayLauDai.NPC_ID, MENU_MO));
        } else {
            p.sendMessage(MessageCreator.createMsgMenu(new String[]{"Bảng xếp hạng hôm nay", "Bảng xếp hạng tuần", "Luật và phần thưởng"},
                    SoTayLauDai.NPC_ID, MENU_DONG));
        }
    }

    /** Menu -527 cua hoat dong (MENU_MO..MENU_BO). true = da xu ly. */
    public static boolean doMenu(final Char p, final int idMenu, final int idOption) {
        if (idMenu < MENU_MO || idMenu > MENU_BO) {
            return false;
        }
        final long bay = GIO.getAsLong();
        int lam = -1; // 0 mot minh, 1 ca doi, 2 bxh ngay, 3 bxh tuan, 4 luat, 5 roi hang, 6 bo cuoc
        if (idMenu == MENU_MO) {
            lam = idOption >= 0 && idOption <= 4 ? idOption : -1;
        } else if (idMenu == MENU_CHO) {
            lam = idOption == 0 ? 5 : idOption >= 1 && idOption <= 3 ? idOption + 1 : -1;
        } else if (idMenu == MENU_DONG) {
            lam = idOption >= 0 && idOption <= 2 ? idOption + 2 : -1;
        } else if (idMenu == MENU_BO) {
            lam = idOption == 0 ? 6 : -1;
        }
        switch (lam) {
            case 0:
            case 1: {
                final String loi = vaoHang(p, lam == 1, bay);
                if (loi != null) {
                    PhongRiengGame.loi(p, loi);
                }
                break;
            }
            case 2:
                bangXepHang(p, false, bay);
                break;
            case 3:
                bangXepHang(p, true, bay);
                break;
            case 4:
                p.sendMessage(MessageCreator.createServerAlertMessage(luat(), ""));
                break;
            case 5:
                roiHang(p.charDBID, null);
                break;
            case 6:
                boCuoc(p, bay);
                break;
            default:
                break;
        }
        return true;
    }

    static void bangXepHang(final Char p, final boolean tuan, final long bay) {
        final List<Pk3v3.Diem> bang = tuan ? xepTuan(Pk3v3.tuan(bay)) : xepNgay(Pk3v3.ngay(bay));
        final StringBuilder b = new StringBuilder(Pk3v3.TEN + " - bảng " + (tuan ? "tuần" : "hôm nay") + "\n");
        int minh = -1;
        for (int i = 0; i < bang.size(); i++) {
            final Pk3v3.Diem d = bang.get(i);
            if (i < 10) {
                b.append(i + 1).append(". ").append(d.ten).append(": ").append(d.diem).append(" điểm (").append(d.thang).append("-")
                        .append(d.hoa).append("-").append(d.thua).append(")\n");
            }
            if (d.charId == p.charDBID) {
                minh = i;
            }
        }
        if (bang.isEmpty()) {
            b.append("Chưa có trận hợp lệ nào.\n");
        }
        if (minh >= 10) {
            b.append("...\n").append(minh + 1).append(". Bạn: ").append(bang.get(minh).diem).append(" điểm\n");
        }
        b.append(tuan ? "Top 1 tuần: danh hiệu Vua PK 3 vs 3; top 2–" + Pk3v3.TOP_DAU_SI + ": Tinh anh PK 3 vs 3 (ít nhất " + Pk3v3.TRAN_TOP_TUAN + " trận)."
                : "Top 3 hôm nay: +" + Pk3v3.LK_TOP_NGAY[0] + "/" + Pk3v3.LK_TOP_NGAY[1] + "/" + Pk3v3.LK_TOP_NGAY[2]
                        + " lượng khoá lúc đóng (ít nhất " + Pk3v3.TRAN_TOP_NGAY + " trận).");
        p.sendMessage(MessageCreator.createServerAlertMessage(b.toString(), ""));
    }

    /** Kiem: xoa moi trang thai bo nho. */
    static void xoaHet() {
        synchronized (KHOA) {
            LUOT.clear();
            LUONG_TUAN.clear();
            DIEM.clear();
            TUAN_CUA_NGAY.clear();
            GAP.clear();
            DA_CHOT.clear();
            HANG.clear();
            daNapDiem = false;
        }
        TRAN.clear();
        moTruoc = null;
        ngayTruoc = -1;
        ghepLuc = 0L;
    }
}
