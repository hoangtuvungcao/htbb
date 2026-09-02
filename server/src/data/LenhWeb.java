package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import real.Char;
import real.CharManager;
import real.LevelDetail;
import real.Map;
import server.TeamServer;

/**
 * LENH TU TRANG QUAN TRI WEB -> AP DUNG CHO NHAN VAT TRONG GAME.
 *
 * VI SAO CAN. Truoc 02/09/2026 trang web (web/admin/nhanvat.php, muc "Dat cap do" va
 * "Buff level kem %") ghi THANG lastLv/xp vao tob_char. Cach do KHONG BAO GIO co tac dung:
 *   - nhan vat dang online giu ban cu trong RAM, lan tu luu ke tiep ghi de lai;
 *   - nhan vat offline cung khong an, vi luc dang nhap server doc cap va exp tu cot pInfo
 *     (truong 4 = exp, truong 5 = lastLV, xem CharNapDuLieu.initPinfo), KHONG doc hai cot
 *     lastLv/xp - hai cot do chi phuc vu bang xep hang / man chon nhan vat, va bi ghi lai tu
 *     RAM o moi lan luu.
 * Nguoi choi thoat ra vao lai van thay cap cu, web thi bao "thanh cong".
 *
 * CACH LAM. Web KHONG ghi tob_char nua, chi chen mot dong "lenh" vao bang {@link #TEN_BANG}
 * (trang_thai = CHO). Luong nay doc bang moi giay:
 *   - nhan vat DANG ONLINE  -> cong thang vao RAM bang dung duong len cap tu nhien
 *                              (Map.addXPForChar, y het nut "Buff EXP" cua AdminPanel: co diem
 *                              co ban, diem ky nang, goi tin len cap), roi saveCharAuto ngay de
 *                              CSDL khop RAM.                                 -> XONG_ONLINE
 *   - nhan vat OFFLINE      -> KHONG dong CSDL. Chuyen dong sang CHO_DANG_NHAP; moi vong doc
 *                              lai, he thay nhan vat online (vua vao game) thi ap dung nhu tren.
 *                              Lam vay vua dung (di qua duong len cap that), vua dong han cua
 *                              so dua luc dang nhap: khong con "ghi CSDL giua luc server dang
 *                              nap nhan vat".                                  -> CHO_DANG_NHAP
 *   - loi (sai cap, khong co nhan vat, ha cap...) -> LOI, ket_qua ghi ly do.
 * Web doi toi da vai giay roi doc trang_thai de bao lai cho nguoi bam. Server dang tat thi
 * dong lenh nam cho, luc khoi dong lai se xu ly - khong bao gio mat lenh. Web co the HUY mot
 * dong con cho (trang_thai HUY), server bo qua.
 *
 * CHI NANG CAP, KHONG HA CAP. Exp moi phai LON HON exp hien tai. Ha cap trong RAM (nhu lenh
 * chat "slv") de lai diem co ban / diem ky nang vuot muc cua cap moi; chot chong gian lan luc
 * dang nhap (CharNapDuLieu ~1184-1236) se xoa diem va ky nang cua nguoi choi, con neu khong bi
 * xoa thi len lai cap cu duoc cong diem LAN HAI. Ca hai deu te hon la tu choi ro rang.
 *
 * LUU BANG saveCharAuto, KHONG DUNG saveChar. saveChar la ban luu luc THOAT: no
 * p.animal.remove(0) / p.pet.remove(0) trong luc luu - goi cho nhan vat dang choi la rut sach
 * thu cuoi va linh thu khoi RAM. saveCharAuto la ban tu luu dinh ky, duyet ma khong rut.
 *
 * TIM NHAN VAT ONLINE: quet CharManager.vChars theo charDBID VA session != null. Khong dung
 * getCharByCharDbID() vi linh thue (charHire) mang isBot = -1 va co the trung charDBID voi
 * chu nhan nhung session null - lay nham no la tuong nhan vat "offline" trong khi chu nhan
 * dang choi.
 */
public final class LenhWeb implements Runnable {

    public static final String TEN_BANG = "web_lenh_nhan_vat";

    public static final String LOAI_DAT_CAP_DO = "dat_cap_do";

    /** Web vua chen, server chua doc. */
    public static final int CHO = 0;
    /** Da cong cho nhan vat dang online va da luu. */
    public static final int XONG_ONLINE = 1;
    /**
     * Ma cu (truoc khi bo nhanh ghi CSDL offline). Khong con duoc tao moi; giu de dong cu trong
     * bang van doc duoc ten trang thai. Server khong dung ma nay nua.
     */
    public static final int XONG_OFFLINE = 2;
    /** Loi, xem ket_qua. */
    public static final int LOI = 3;
    /** Nhan vat offline: nam cho, server ap dung ngay khi thay nhan vat vao game. */
    public static final int CHO_DANG_NHAP = 4;
    /** Nguoi quan tri huy tren web. Server bo qua. */
    public static final int HUY = 5;

    /** Doc bang moi giay: web dang doi ket qua, khong de nguoi bam cho lau. */
    public static final long CHU_KY_MS = 1000L;

    /** Moi vong xu ly toi da bay nhieu dong, tranh mot loat lenh chan vong lap. */
    static final int MOI_VONG_TOI_DA = 50;

    /**
     * Cap cao nhat cho phep = expMain.length - 1 (= 90). LevelDetail.getXpFromLevel() chi dung
     * khi level < expMain.length, va web (CAPDO_TOI_DA) cung dung tran nay.
     */
    public static final int CAP_TOI_DA = LevelDetail.expMain.length - 1;

    /**
     * Nguon ghi kem khi cong exp. PHAI chua "adminpanel": MapXuLyLenh.addXPForChar chi bo
     * chia 3 (lv >= 60) va bo random khi pos chua chuoi nay.
     */
    public static final String NGUON = "adminpanel-web";

    private static volatile boolean daChay = false;
    private static boolean daBaoLoi = false;

    /** Ket qua xu ly mot dong lenh. */
    public static final class KetQua {
        public final int trangThai;
        public final String moTa;

        public KetQua(final int trangThai, final String moTa) {
            this.trangThai = trangThai;
            this.moTa = moTa == null ? "" : moTa;
        }
    }

    /** Goi mot lan luc khoi dong server. Goi lai khong tao them thread. */
    public static synchronized void batDau() {
        if (daChay) {
            return;
        }
        daChay = true;
        final Thread t = new Thread(new LenhWeb());
        t.setName("LENH WEB");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        taoBang();
        while (TeamServer.running) {
            try {
                xuLyMotVong();
            } catch (final Exception e) {
                baoLoiMotLan(e);
            }
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ------------------------------------------------------------------ tinh toan thuan

    /** Kep % vao cap (don vi phan nghin, cung don vi LevelDetail.percent) ve 0..999. */
    public static int kepPhanNghin(final int phanNghin) {
        if (phanNghin < 0) {
            return 0;
        }
        return phanNghin > 999 ? 999 : phanNghin;
    }

    /**
     * Tong exp ung voi "cap X, da vao Y phan nghin cua cap do". Cung cong thuc voi
     * LevelDetail.mapExp2NewExp va ButtonEditor.calculateExpToBuff:
     * expMain[cap - 1] + expRequire[cap] * phanNghin / 1000.
     * Cap ngoai 1..CAP_TOI_DA duoc kep vao bien, khong nem loi (nguoi goi da kiem truoc).
     */
    public static long tinhExp(int capDo, final int phanNghin) {
        if (capDo < 1) {
            capDo = 1;
        }
        if (capDo > CAP_TOI_DA) {
            capDo = CAP_TOI_DA;
        }
        return LevelDetail.expMain[capDo - 1]
                + LevelDetail.expRequire[capDo] * (long) kepPhanNghin(phanNghin) / 1000L;
    }

    /** Nhan vat that dang trong game: khong phai bot, va con socket. */
    public static boolean laNguoiChoiThat(final Char p) {
        return p != null && p.isBot == -1 && p.getSession() != null;
    }

    /**
     * Nhan vat dang online co tob_char.id nay, hoac null. Quet tay thay cho
     * CharManager.getCharByCharDbID (xem chu thich dau lop ve linh thue).
     */
    public static Char timNguoiChoiOnline(final int charId) {
        try {
            final Vector<Char> ds = CharManager.instance.vChars;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (c != null && c.charDBID == charId && laNguoiChoiThat(c)) {
                        return c;
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Exception ignored) {
        }
        return null;
    }

    // ------------------------------------------------------------------ xu ly mot lenh

    /**
     * Xu ly mot dong lenh dang o trang thai {@code trangThaiCu} (CHO hoac CHO_DANG_NHAP).
     * Tra ve trang thai moi + mo ta. Nem loi khi du lieu sai -> nguoi goi ghi LOI.
     */
    public static KetQua xuLy(final String loai, final int charId, final int capDo,
            final int phanNghin, final String adminUser, final int trangThaiCu) throws Exception {
        if (!LOAI_DAT_CAP_DO.equals(loai)) {
            throw new IllegalArgumentException("khong hieu loai lenh \"" + loai + "\"");
        }
        if (capDo < 1 || capDo > CAP_TOI_DA) {
            throw new IllegalArgumentException("cap do " + capDo + " ngoai khoang 1.." + CAP_TOI_DA);
        }
        final int pn = kepPhanNghin(phanNghin);

        final Char p = timNguoiChoiOnline(charId);
        if (p != null) {
            return new KetQua(XONG_ONLINE, apDungOnline(p, capDo, pn, adminUser));
        }
        if (trangThaiCu == CHO_DANG_NHAP) {
            // Van chua vao game: giu nguyen, vong sau doc lai.
            return new KetQua(CHO_DANG_NHAP, "");
        }
        if (!coNhanVat(charId)) {
            throw new IllegalStateException("khong co nhan vat id " + charId + " trong tob_char");
        }
        return new KetQua(CHO_DANG_NHAP, "offline: se cong ngay khi nhan vat vao game");
    }

    /**
     * Ap dung cho nhan vat dang online roi luu ngay. Tra ve mo ta ngan de ghi ket_qua.
     * Nem IllegalStateException neu khong phai nang cap (xem chu thich dau lop).
     */
    public static String apDungOnline(final Char p, final int capDo, final int phanNghin,
            final String adminUser) throws Exception {
        final long expMoi = tinhExp(capDo, phanNghin);
        final int lvCu = p.lvDetail.lv;
        final int pcCu = p.lvDetail.percent;
        final long expCu = p.lvDetail.getExp();
        if (expMoi <= expCu) {
            throw new IllegalStateException("chi ho tro NANG cap: nhan vat dang lv " + lvCu + " ("
                    + (pcCu / 10.0) + "%), muc yeu cau lv " + capDo + " (" + (phanNghin / 10.0)
                    + "%) khong cao hon");
        }
        // Duong len cap tu nhien: cong diem, gui goi 33/32, calculateAttrib, hoi day hp/mp.
        Map.addXPForChar(p, expMoi - expCu, true, NGUON + ":" + adminUser);
        // Luu ngay bang ban tu luu dinh ky: lan tu luu ke tiep ghi lai dung ban nay.
        Database.instance.saveCharAuto(p);
        try {
            p.sendMessage(real.MessageCreator.createServerAlertMessage(
                    "Quản trị viên đã đặt cấp độ của bạn thành " + p.lvDetail.lv, ""));
        } catch (final Exception ignored) {
        }
        try {
            Database.instance.saveOrtherLog("", p.charname, adminUser + " (web) dat cap " + capDo
                    + " " + (phanNghin / 10.0) + "%: lv " + lvCu + " -> " + p.lvDetail.lv, "adminset");
        } catch (final Exception ignored) {
        }
        return "online: lv " + lvCu + " (" + (pcCu / 10.0) + "%) -> lv " + p.lvDetail.lv + " ("
                + (p.lvDetail.percent / 10.0) + "%)";
    }

    /** tob_char co dong id nay khong (chi doc). */
    static boolean coNhanVat(final int charId) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT 1 FROM tob_char WHERE id = ? LIMIT 1");
            ps.setInt(1, charId);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
    }

    // ------------------------------------------------------------------ bang lenh

    static void xuLyMotVong() throws Exception {
        final List<Object[]> ds = docLenhCho();
        for (final Object[] r : ds) {
            final long id = (Long) r[0];
            final String loai = (String) r[1];
            final int charId = (Integer) r[2];
            final String charname = (String) r[3];
            final int capDo = (Integer) r[4];
            final int phanNghin = (Integer) r[5];
            final String adminUser = (String) r[6];
            final int trangThaiCu = (Integer) r[7];
            KetQua kq;
            try {
                kq = xuLy(loai, charId, capDo, phanNghin, adminUser, trangThaiCu);
            } catch (final Exception e) {
                kq = new KetQua(LOI, cat("loi: " + e.getMessage(), 250));
            }
            if (kq.trangThai == trangThaiCu && kq.moTa.isEmpty()) {
                continue; // dang cho dang nhap, chua co gi moi
            }
            ghiKetQua(id, trangThaiCu, kq);
            System.out.println("[LENH WEB] #" + id + " " + loai + " " + charname + " (id " + charId
                    + ") cap " + capDo + " " + (kepPhanNghin(phanNghin) / 10.0) + "% boi " + adminUser
                    + " -> " + kq.moTa);
        }
    }

    /** Cac dong dang CHO hoac CHO_DANG_NHAP, cu nhat truoc. */
    static List<Object[]> docLenhCho() throws Exception {
        final List<Object[]> ds = new ArrayList<Object[]>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("SELECT id, loai, char_id, charname, cap_do, phan_nghin, admin_user, trang_thai FROM `"
                    + TEN_BANG + "` WHERE trang_thai IN (?, ?) ORDER BY id LIMIT " + MOI_VONG_TOI_DA);
            ps.setInt(1, CHO);
            ps.setInt(2, CHO_DANG_NHAP);
            rs = ps.executeQuery();
            while (rs.next()) {
                ds.add(new Object[]{rs.getLong("id"), rs.getString("loai"), rs.getInt("char_id"),
                    rs.getString("charname"), rs.getInt("cap_do"), rs.getInt("phan_nghin"),
                    rs.getString("admin_user"), rs.getInt("trang_thai")});
            }
        } finally {
            dong(rs);
            dong(ps);
            tra(conn);
        }
        return ds;
    }

    /**
     * Ghi trang thai moi, CHI khi dong van con o trang thai cu (web co the vua HUY, hoac mot
     * vong khac vua ghi) - khong ghi de ket qua da co.
     */
    static void ghiKetQua(final long id, final int trangThaiCu, final KetQua kq) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Database.instance.getConnection();
            ps = conn.prepareStatement("UPDATE `" + TEN_BANG
                    + "` SET trang_thai = ?, ket_qua = ?, xu_ly_luc = NOW() WHERE id = ? AND trang_thai = ?");
            ps.setInt(1, kq.trangThai);
            ps.setString(2, cat(kq.moTa, 255));
            ps.setLong(3, id);
            ps.setInt(4, trangThaiCu);
            ps.executeUpdate();
        } finally {
            dong(ps);
            tra(conn);
        }
    }

    /**
     * Tao bang neu chua co. Cung dinh nghia voi sql/them_bang_web_lenh_nhan_vat.sql va
     * web/admin/inc/lenh_nhan_vat.php (web cung tu tao, de ai chay truoc cung duoc).
     */
    static void taoBang() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS `" + TEN_BANG + "` ("
                    + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                    + "`loai` VARCHAR(30) NOT NULL,"
                    + "`char_id` INT NOT NULL,"
                    + "`charname` VARCHAR(50) NOT NULL DEFAULT '',"
                    + "`cap_do` INT NOT NULL DEFAULT 0,"
                    + "`phan_nghin` INT NOT NULL DEFAULT 0,"
                    + "`admin_user` VARCHAR(50) NOT NULL DEFAULT '',"
                    + "`trang_thai` TINYINT NOT NULL DEFAULT 0,"
                    + "`ket_qua` VARCHAR(255) NOT NULL DEFAULT '',"
                    + "`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "`xu_ly_luc` DATETIME NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `idx_trang_thai` (`trang_thai`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (final Exception e) {
            baoLoiMotLan(e);
        } finally {
            dong(st);
            tra(conn);
        }
    }

    // ------------------------------------------------------------------ tien ich

    static String cat(final String s, final int toiDa) {
        if (s == null) {
            return "";
        }
        return s.length() <= toiDa ? s : s.substring(0, toiDa);
    }

    /** Chi in mot lan: thread nay chay mai, loi lap lai (vd MySQL tat) se ngap log. */
    private static synchronized void baoLoiMotLan(final Exception e) {
        if (daBaoLoi) {
            return;
        }
        daBaoLoi = true;
        System.out.println("LENH WEB: loi (chi bao mot lan) - " + e);
    }

    private static void dong(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (final Exception ignored) {
        }
    }

    private static void dong(final Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (final Exception ignored) {
        }
    }

    /** Tra ket noi ve pool - dung mot lan, khong free(null) (xem GhiSoOnline). */
    private static void tra(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    private LenhWeb() {
    }
}
