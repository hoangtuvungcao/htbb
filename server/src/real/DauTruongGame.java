package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.CaThePokemon;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.ChongCloneLogic;
import com.htbb.pokemon.CupLogic;
import com.htbb.pokemon.DauTruongDuLieu;
import com.htbb.pokemon.DauTruongLogic;
import com.htbb.pokemon.DauTruongTran;
import com.htbb.pokemon.DongHo;
import com.htbb.pokemon.GoiDauTruong;
import com.htbb.pokemon.HopPc;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThangPvp;
import com.htbb.pokemon.XepHangPvp;
import com.htbb.thongbao.ThongBao;
import data.Database;
import data.DatabaseBoNgoc;
import data.DatabaseLauDai;
import io.Message;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAU TRUONG BAO BOI (D12, he_thong_day_du.md 3.6) - phan GAME: NPC Dau truong (-31) muc "Đấu trường Bảo Bối" mo man
 * (goi -109, client &gt;= 908), luu doi tan cong / phong thu tu Hop PC, ghep doi thu bat dong bo (danh doi phong thu cua
 * nguoi khac, ke ca offline), tran theo luot do may chu mo phong (com.htbb.pokemon.DauTruongTran) - nguoi choi chon chieu
 * moi luot trong {@link DauTruongLogic#GIAY_LUOT} giay, het gio / roi game thi may chon. Logic thuan:
 * com.htbb.pokemon.DauTruongLogic / DauTruongTran / GoiDauTruong.
 *
 * <p>CSDL (CREATE TABLE IF NOT EXISTS o {@link #taoBang} + sql/nang_cap.sql khoi "them_dau_truong.sql"): tob_dau_truong_doi
 * (doi hinh + danh tinh chong nick phu lan cuoi), tob_dau_truong_mua (diem mua), tob_dau_truong_tran (moi tran, luot tinh
 * luc bat dau, luong khoa, nhat ky hanh dong + hat de phat lai), tob_dau_truong_thuong (thuong mua, mot lan).
 */
public final class DauTruongGame {

    private DauTruongGame() {
    }

    // ================================================================== cau hinh

    static long so(final String k, final long md) {
        try {
            return CauHinhTrungTam.so(k, md);
        } catch (final Throwable e) {
            return md;
        }
    }

    public static boolean bat() {
        return so("dau_truong.bat", 1) != 0;
    }

    static boolean coClient(final Char p) {
        return p != null && p.getSession() != null && p.getSession().banClient >= GoiDauTruong.BAN_CLIENT;
    }

    /** NPC Dau truong: hien muc "Đấu trường Bảo Bối" (client du moi + che do bat). */
    public static boolean hienMenu(final Char p) {
        return bat() && coClient(p);
    }

    // ================================================================== du lieu chieu

    private static volatile long lanNap;
    private static final Object KHOA_NAP = new Object();

    static DauTruongDuLieu duLieu() {
        final DauTruongDuLieu d = DauTruongDuLieu.hien();
        final long bay = System.currentTimeMillis();
        if (!d.rong() && bay - lanNap < 10L * 60L * 1000L) {
            return d;
        }
        synchronized (KHOA_NAP) {
            if (!DauTruongDuLieu.hien().rong() && bay - lanNap < 10L * 60L * 1000L) {
                return DauTruongDuLieu.hien();
            }
            lanNap = bay;
            final DauTruongDuLieu moi = napCsdl();
            if (moi != null) {
                DauTruongDuLieu.dat(moi);
            }
            return DauTruongDuLieu.hien();
        }
    }

    static DauTruongDuLieu napCsdl() {
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            com.htbb.pokemon.LoaiDuLieu loai = LoaiQuaiGame.bang();
            if (loai.rong()) {
                // bang loai chua nap (LoaiQuaiGame nap nen): nap ngay, Dau truong can he + chi so nen that
                final com.htbb.pokemon.LoaiDuLieu moi = data.DatabaseLoai.nap();
                if (moi != null) {
                    com.htbb.pokemon.LoaiDuLieu.dat(moi);
                    loai = moi;
                }
            }
            conn = Database.instance.getConnection();
            st = conn.createStatement();
            final Map<String, DauTruongDuLieu.Chieu> chieu = new HashMap<String, DauTruongDuLieu.Chieu>();
            rs = st.executeQuery("SELECT ma, ten, he, kieu, vai_tro, so_don, trang_thai, ti_le_tt_phan_nghin FROM data_chieu");
            while (rs.next()) {
                chieu.put(rs.getString(1), new DauTruongDuLieu.Chieu(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4),
                        rs.getString(5), rs.getInt(6), rs.getString(7), rs.getInt(8)));
            }
            rs.close();
            final Map<Integer, String[]> cl = new HashMap<Integer, String[]>();
            rs = st.executeQuery("SELECT dex, o, ma_chieu FROM data_loai_chieu");
            while (rs.next()) {
                final int o = rs.getInt(2);
                if (o < 0 || o > 3) {
                    continue;
                }
                cl.computeIfAbsent(Integer.valueOf(rs.getInt(1)), k -> new String[4])[o] = rs.getString(3);
            }
            if (chieu.isEmpty() || cl.isEmpty()) {
                return null;
            }
            return new DauTruongDuLieu(loai, chieu, cl);
        } catch (final Exception e) {
            System.out.println("[DAU TRUONG] nap chieu: " + e);
            return null;
        } finally {
            dong(rs, st, conn);
        }
    }

    // ================================================================== CSDL

    public static void taoBang() {
        DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_dau_truong_doi (id_char INT NOT NULL, ten VARCHAR(32) NOT NULL DEFAULT '',"
                + " user_id INT NOT NULL DEFAULT 0, may VARCHAR(64) NOT NULL DEFAULT '', ip VARCHAR(45) NOT NULL DEFAULT '',"
                + " doi_cong VARCHAR(40) NOT NULL DEFAULT '', doi_thu VARCHAR(40) NOT NULL DEFAULT '', suc_cong INT NOT NULL DEFAULT 0,"
                + " suc_thu INT NOT NULL DEFAULT 0, thu_hinh VARCHAR(64) NOT NULL DEFAULT '', sua_luc DATETIME NOT NULL,"
                + " PRIMARY KEY (id_char), KEY idx_dtd_suc (suc_thu)) DEFAULT CHARSET=utf8");
        DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_dau_truong_mua (mua INT NOT NULL, id_char INT NOT NULL,"
                + " diem INT NOT NULL DEFAULT 1000, thang INT NOT NULL DEFAULT 0, thua INT NOT NULL DEFAULT 0,"
                + " thu_thang INT NOT NULL DEFAULT 0, thu_thua INT NOT NULL DEFAULT 0, so_tran INT NOT NULL DEFAULT 0,"
                + " cap_nhat DATETIME NOT NULL, PRIMARY KEY (mua, id_char), KEY idx_dtm_diem (mua, diem)) DEFAULT CHARSET=utf8");
        DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_dau_truong_tran (id BIGINT NOT NULL AUTO_INCREMENT, mua INT NOT NULL,"
                + " tuan INT NOT NULL, ngay INT NOT NULL, cong INT NOT NULL, thu INT NOT NULL, hat BIGINT NOT NULL DEFAULT 0,"
                + " ket_qua TINYINT NOT NULL DEFAULT -1, diem_cong_truoc INT NOT NULL DEFAULT 0, diem_cong_sau INT NOT NULL DEFAULT 0,"
                + " diem_thu_truoc INT NOT NULL DEFAULT 0, diem_thu_sau INT NOT NULL DEFAULT 0, he_so SMALLINT NOT NULL DEFAULT 0,"
                + " luong SMALLINT NOT NULL DEFAULT 0, luot SMALLINT NOT NULL DEFAULT 0, hanh_dong VARCHAR(255) NOT NULL DEFAULT '',"
                + " bat_dau DATETIME NOT NULL, luc DATETIME NULL, PRIMARY KEY (id), KEY idx_dtt_cong_ngay (cong, ngay),"
                + " KEY idx_dtt_cong_tuan (cong, tuan), KEY idx_dtt_tuan (tuan), KEY idx_dtt_thu (thu)) DEFAULT CHARSET=utf8");
        DatabaseLauDai.ghiCong("CREATE TABLE IF NOT EXISTS tob_dau_truong_thuong (mua INT NOT NULL, id_char INT NOT NULL,"
                + " hang INT NOT NULL DEFAULT 0, diem INT NOT NULL DEFAULT 0, so_tran INT NOT NULL DEFAULT 0,"
                + " danh_hieu INT NOT NULL DEFAULT 0, luc DATETIME NOT NULL, PRIMARY KEY (mua, id_char)) DEFAULT CHARSET=utf8");
    }

    static void dong(final ResultSet rs, final Statement pre, final Connection conn) {
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

    interface Dong {
        void doc(ResultSet rs) throws Exception;
    }

    static boolean doc(final String sql, final Dong d, final Object... ts) {
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
            return true;
        } catch (final Exception e) {
            System.out.println("[DAU TRUONG] SQL: " + e);
            return false;
        } finally {
            dong(rs, pre, conn);
        }
    }

    static int soNguyen(final String sql, final int md, final Object... ts) {
        final int[] kq = {md};
        doc(sql, rs -> kq[0] = rs.getInt(1), ts);
        return kq[0];
    }

    static long chenTran(final int mua, final int tuan, final int ngay, final int cong, final int thu, final long hat, final int dCong,
            final int dThu) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        try {
            conn = Database.instance.getConnection();
            pre = conn.prepareStatement("INSERT INTO tob_dau_truong_tran (mua, tuan, ngay, cong, thu, hat, diem_cong_truoc,"
                    + " diem_thu_truoc, bat_dau) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())", Statement.RETURN_GENERATED_KEYS);
            pre.setInt(1, mua);
            pre.setInt(2, tuan);
            pre.setInt(3, ngay);
            pre.setInt(4, cong);
            pre.setInt(5, thu);
            pre.setLong(6, hat);
            pre.setInt(7, dCong);
            pre.setInt(8, dThu);
            pre.executeUpdate();
            rs = pre.getGeneratedKeys();
            return rs.next() ? rs.getLong(1) : -1L;
        } catch (final Exception e) {
            System.out.println("[DAU TRUONG] chen tran: " + e);
            return -1L;
        } finally {
            dong(rs, pre, conn);
        }
    }

    // ================================================================== thoi gian

    static int mua() {
        return ThangPvp.mua(LauDai.ngayHienTai());
    }

    static int ngayConLaiMua() {
        final int ngay = LauDai.ngayHienTai();
        final int dai = ThangPvp.TUAN_MOI_MUA * 7;
        return Math.max(1, (ThangPvp.mua(ngay) + 1) * dai - Math.max(0, ngay));
    }

    static int tuan() {
        return CupLogic.tuan(DongHo.bay());
    }

    static int homNay() {
        return BatPokemonGame.homNay();
    }

    // ================================================================== doc trang thai

    static int diem(final int mua, final int id) {
        return soNguyen("SELECT diem FROM tob_dau_truong_mua WHERE mua = ? AND id_char = ?", DauTruongLogic.DIEM_BAN_DAU,
                Integer.valueOf(mua), Integer.valueOf(id));
    }

    static int daDanhHomNay(final int id) {
        return soNguyen("SELECT COUNT(*) FROM tob_dau_truong_tran WHERE cong = ? AND ngay = ?", 0, Integer.valueOf(id),
                Integer.valueOf(homNay()));
    }

    static int luongTuan(final int id) {
        return soNguyen("SELECT COALESCE(SUM(luong), 0) FROM tob_dau_truong_tran WHERE cong = ? AND tuan = ?", 0, Integer.valueOf(id),
                Integer.valueOf(tuan()));
    }

    static int tran24h(final int cong, final int thu) {
        return soNguyen("SELECT COUNT(*) FROM tob_dau_truong_tran WHERE cong = ? AND thu = ? AND bat_dau >= NOW() - INTERVAL 1 DAY", 0,
                Integer.valueOf(cong), Integer.valueOf(thu));
    }

    /** {doi_cong, doi_thu} cua nhan vat (rong = chua co). */
    static String[] doiHinh(final int id) {
        final String[] kq = {"", ""};
        doc("SELECT doi_cong, doi_thu FROM tob_dau_truong_doi WHERE id_char = ?", rs -> {
            kq[0] = rs.getString(1);
            kq[1] = rs.getString(2);
        }, Integer.valueOf(id));
        return kq;
    }

    static XepHangPvp.Cum cum(final Char p) {
        try {
            final ChongCloneLogic.Ben b = ChongClone.ben(p, false);
            return new XepHangPvp.Cum(b.userId, b.may, b.ip);
        } catch (final Throwable e) {
            return new XepHangPvp.Cum(p.userID, "", "");
        }
    }

    // ================================================================== goi

    static void gui(final Char p, final GhiGoi g) {
        if (!coClient(p)) {
            return;
        }
        try {
            final Message m = new Message(GoiDauTruong.LENH);
            g.ghi(m.dos);
            p.sendMessage(m);
        } catch (final Exception e) {
            System.out.println("[DAU TRUONG] gui: " + e);
        }
    }

    interface GhiGoi {
        void ghi(java.io.DataOutputStream o) throws java.io.IOException;
    }

    static void ghiChu(final Char p, final String chu) {
        gui(p, o -> GoiDauTruong.ghiChu(o, chu));
    }

    // ================================================================== xu ly goi

    public static void xuLy(final Char p, final Message msg) {
        if (p == null || p.charDBID <= 0) {
            return;
        }
        try {
            final int kieu = msg.dis.readByte();
            if (!bat()) {
                ThongBao.loi(p, "Đấu trường Bảo Bối đang tạm đóng.", null);
                return;
            }
            switch (kieu) {
                case GoiDauTruong.C_MO:
                    mo(p, "");
                    break;
                case GoiDauTruong.C_LUU_DOI: {
                    final int loai = msg.dis.readByte();
                    final int n = msg.dis.readByte();
                    final int[] ids = new int[Math.max(0, Math.min(6, n))];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = msg.dis.readInt();
                    }
                    luuDoi(p, loai, ids);
                    break;
                }
                case GoiDauTruong.C_TIM:
                    tim(p, msg.dis.readByte() != 0);
                    break;
                case GoiDauTruong.C_THACH:
                    thach(p, msg.dis.readInt());
                    break;
                case GoiDauTruong.C_HANH_DONG: {
                    final int id = msg.dis.readInt();
                    final int loai = msg.dis.readByte();
                    final int o = msg.dis.readByte();
                    hanhDong(p, id, new DauTruongTran.HanhDong(loai, o));
                    break;
                }
                case GoiDauTruong.C_TU_DONG: {
                    final int id = msg.dis.readInt();
                    tuDong(p, id, msg.dis.readByte() != 0);
                    break;
                }
                case GoiDauTruong.C_DAU_HANG:
                    dauHang(p, msg.dis.readInt());
                    break;
                default:
                    break;
            }
        } catch (final Exception e) {
            // goi hong: bo qua
        }
    }

    // ================================================================== man chinh / doi hinh

    /** NPC Dau truong -&gt; "Đấu trường Bảo Bối". */
    public static void moTuNpc(final Char p) {
        if (!bat()) {
            ThongBao.loi(p, "Đấu trường Bảo Bối đang tạm đóng.", null);
            return;
        }
        if (!coClient(p)) {
            ThongBao.loi(p, "Cập nhật game bản mới để vào Đấu trường Bảo Bối.", null);
            return;
        }
        mo(p, "");
    }

    static GoiDauTruong.KhoCon khoCon(final DauTruongDuLieu d, final HopPc.Con c) {
        final GoiDauTruong.KhoCon k = new GoiDauTruong.KhoCon();
        k.id = c.id;
        k.dex = c.dex;
        k.cap = c.cap;
        k.shiny = c.shiny;
        k.sao = CaThePokemon.sao(c.iv);
        k.ivPt = CaThePokemon.phanTram(c.iv);
        k.suc50 = d.sucManh50(c.dex, c.iv, c.tinhCach);
        k.he1 = d.he1(c.dex);
        k.he2 = d.he2(c.dex);
        k.camLy = d.bac(c.dex) >= com.htbb.pokemon.LoaiDuLieu.T6 ? 1 : 0;
        k.ten = d.ten(c.dex);
        return k;
    }

    /** Gui trang thai + kho (mo = true: client mo man Dau truong). */
    static void mo(final Char p, final String ghiChu) {
        final HopPc h = BatPokemonGame.hop(p);
        final DauTruongDuLieu d = duLieu();
        final int mua = mua();
        final int id = p.charDBID;
        final GoiDauTruong.TrangThai t = new GoiDauTruong.TrangThai();
        t.mo = true;
        t.mua = mua + 1;
        t.ngayConLai = ngayConLaiMua();
        final int[] dm = {DauTruongLogic.DIEM_BAN_DAU, 0, 0, 0};
        final boolean[] co = {false};
        doc("SELECT diem, thang, thua, so_tran FROM tob_dau_truong_mua WHERE mua = ? AND id_char = ?", rs -> {
            dm[0] = rs.getInt(1);
            dm[1] = rs.getInt(2);
            dm[2] = rs.getInt(3);
            dm[3] = rs.getInt(4);
            co[0] = true;
        }, Integer.valueOf(mua), Integer.valueOf(id));
        t.diem = dm[0];
        t.thang = dm[1];
        t.thua = dm[2];
        t.bac = DauTruongLogic.bac(t.diem);
        t.tenBac = DauTruongLogic.TEN_BAC[t.bac];
        t.hang = co[0] && dm[3] > 0 ? 1 + soNguyen("SELECT COUNT(*) FROM tob_dau_truong_mua WHERE mua = ? AND so_tran > 0 AND diem > ?", 0,
                Integer.valueOf(mua), Integer.valueOf(t.diem)) : 0;
        t.luotNgay = DauTruongLogic.LUOT_NGAY;
        t.luotCon = DauTruongLogic.luotConLai(daDanhHomNay(id));
        t.luongTuan = luongTuan(id);
        t.tranLuongTuan = DauTruongLogic.TRAN_LUONG_TUAN;
        t.lamMoiCon = Math.max(0, DauTruongLogic.LAM_MOI_NGAY - lamMoi(id)[1]);
        t.giayLuot = DauTruongLogic.GIAY_LUOT;
        final String[] dh = doiHinh(id);
        t.cong = conHopLe(DauTruongLogic.docIds(dh[0]), h);
        t.thu = conHopLe(DauTruongLogic.docIds(dh[1]), h);
        t.sucCong = DauTruongLogic.sucManhDoi(t.cong, h, d);
        t.sucThu = DauTruongLogic.sucManhDoi(t.thu, h, d);
        final List<GoiDauTruong.KhoCon> kho = new ArrayList<GoiDauTruong.KhoCon>();
        if (h != null) {
            for (final HopPc.Con c : h.danhSach()) {
                kho.add(khoCon(d, c));
            }
            kho.sort((a, b) -> Integer.compare(b.suc50, a.suc50));
        }
        t.kho = kho.toArray(new GoiDauTruong.KhoCon[0]);
        t.ghiChu = ghiChu == null ? "" : ghiChu;
        gui(p, o -> GoiDauTruong.trangThai(o, t));
        // cap nhat danh tinh chong nick phu (doi thu offline van loc dung)
        if (!dh[0].isEmpty() || !dh[1].isEmpty()) {
            final XepHangPvp.Cum c = cum(p);
            DatabaseLauDai.ghiCong("UPDATE tob_dau_truong_doi SET ten = ?, user_id = ?, may = ?, ip = ? WHERE id_char = ?", p.charname,
                    Integer.valueOf(c.userId), c.may, c.ip, Integer.valueOf(id));
        }
    }

    /** Doi luu con hop le (con da roi Hop PC thi bo o do: client thay o trong). */
    static int[] conHopLe(final int[] ids, final HopPc h) {
        if (ids == null || h == null) {
            return new int[0];
        }
        for (final int x : ids) {
            if (h.lay(x) == null) {
                return new int[0];
            }
        }
        return ids;
    }

    static void luuDoi(final Char p, final int loai, final int[] ids) {
        final HopPc h = BatPokemonGame.hop(p);
        final DauTruongDuLieu d = duLieu();
        final String loi = DauTruongLogic.loiDoi(ids, h, d);
        if (loi != null) {
            ThongBao.loi(p, loi, null);
            ghiChu(p, loi);
            return;
        }
        final String s = DauTruongLogic.ghiIds(ids);
        final int suc = DauTruongLogic.sucManhDoi(ids, h, d);
        final StringBuilder hinh = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            final HopPc.Con c = h.lay(ids[i]);
            hinh.append(i > 0 ? "," : "").append(c.dex).append('.').append(c.shiny ? 1 : 0).append('.').append(CaThePokemon.sao(c.iv));
        }
        final XepHangPvp.Cum c = cum(p);
        final boolean thu = loai == 1;
        final int n = DatabaseLauDai.ghiCong("INSERT INTO tob_dau_truong_doi (id_char, ten, user_id, may, ip, doi_cong, doi_thu, suc_cong,"
                + " suc_thu, thu_hinh, sua_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                + " user_id = VALUES(user_id), may = VALUES(may), ip = VALUES(ip), sua_luc = VALUES(sua_luc), "
                + (thu ? "doi_thu = VALUES(doi_thu), suc_thu = VALUES(suc_thu), thu_hinh = VALUES(thu_hinh)"
                        : "doi_cong = VALUES(doi_cong), suc_cong = VALUES(suc_cong)"),
                Integer.valueOf(p.charDBID), p.charname, Integer.valueOf(c.userId), c.may, c.ip, thu ? "" : s, thu ? s : "",
                Integer.valueOf(thu ? 0 : suc), Integer.valueOf(thu ? suc : 0), thu ? hinh.toString() : "");
        if (n < 0) {
            ThongBao.loi(p, "Chưa lưu được đội hình, thử lại sau.", null);
            return;
        }
        mo(p, thu ? "Đã lưu đội phòng thủ (sức mạnh " + suc + "). Người khác sẽ đấu với đội này." : "Đã lưu đội tấn công (sức mạnh " + suc + ").");
    }

    // ================================================================== tim doi thu

    /** id -&gt; {ngay, so lan lam moi hom nay}. */
    static final ConcurrentHashMap<Integer, int[]> LAM_MOI = new ConcurrentHashMap<Integer, int[]>();
    /** id -&gt; danh sach doi thu vua de xuat (chi thach dau duoc nguoi trong danh sach). */
    static final ConcurrentHashMap<Integer, List<Integer>> DE_XUAT = new ConcurrentHashMap<Integer, List<Integer>>();

    static int[] lamMoi(final int id) {
        final int ngay = homNay();
        final int[] l = LAM_MOI.computeIfAbsent(Integer.valueOf(id), k -> new int[]{ngay, 0});
        if (l[0] != ngay) {
            l[0] = ngay;
            l[1] = 0;
        }
        return l;
    }

    static void tim(final Char p, final boolean lamMoiLai) {
        final int id = p.charDBID;
        final HopPc h = BatPokemonGame.hop(p);
        final DauTruongDuLieu d = duLieu();
        final String[] dh = doiHinh(id);
        final int[] cong = DauTruongLogic.docIds(dh[0]);
        if (DauTruongLogic.loiDoi(cong, h, d) != null) {
            ThongBao.loi(p, "Chọn và lưu đội tấn công 3 Pokémon trước.", null);
            ghiChu(p, "Chọn và lưu đội tấn công 3 Pokémon trước (tab Đội hình).");
            return;
        }
        final int[] lm = lamMoi(id);
        if (lamMoiLai) {
            if (lm[1] >= DauTruongLogic.LAM_MOI_NGAY) {
                ghiChu(p, "Hết lượt làm mới hôm nay.");
                return;
            }
            lm[1]++;
        }
        final int mua = mua();
        final int diemToi = diem(mua, id);
        final int sucToi = DauTruongLogic.sucManhDoi(cong, h, d);
        final XepHangPvp.Cum cumToi = cum(p);
        final int cua = DauTruongLogic.cuaSoDiem(lm[1]);
        final Map<Integer, Integer> gap = new HashMap<Integer, Integer>();
        doc("SELECT thu, COUNT(*) FROM tob_dau_truong_tran WHERE cong = ? AND bat_dau >= NOW() - INTERVAL 1 DAY GROUP BY thu",
                rs -> gap.put(Integer.valueOf(rs.getInt(1)), Integer.valueOf(rs.getInt(2))), Integer.valueOf(id));
        final List<DauTruongLogic.UngVien> ds = new ArrayList<DauTruongLogic.UngVien>();
        final Map<Integer, Object[]> phu = new HashMap<Integer, Object[]>();
        doc("SELECT d.id_char, d.ten, d.user_id, d.may, d.ip, d.suc_thu, d.thu_hinh, COALESCE(m.diem, 1000) AS dm FROM tob_dau_truong_doi d"
                + " LEFT JOIN tob_dau_truong_mua m ON m.mua = ? AND m.id_char = d.id_char WHERE d.doi_thu <> '' AND d.id_char <> ?"
                + " AND COALESCE(m.diem, 1000) BETWEEN ? AND ? ORDER BY ABS(COALESCE(m.diem, 1000) - ?) LIMIT 300", rs -> {
                    final int u = rs.getInt(1);
                    final Integer g = gap.get(Integer.valueOf(u));
                    ds.add(new DauTruongLogic.UngVien(u, rs.getInt(8), rs.getInt(6), new XepHangPvp.Cum(rs.getInt(3), rs.getString(4),
                            rs.getString(5)), g == null ? 0 : g.intValue()));
                    phu.put(Integer.valueOf(u), new Object[]{rs.getString(2), rs.getString(7)});
                }, Integer.valueOf(mua), Integer.valueOf(id), Integer.valueOf(diemToi - cua), Integer.valueOf(diemToi + cua),
                Integer.valueOf(diemToi));
        final List<DauTruongLogic.UngVien> chon = DauTruongLogic.deXuat(id, diemToi, sucToi, cumToi, ds, lm[1],
                System.nanoTime() ^ ((long) id << 20));
        final List<GoiDauTruong.DoiThu> kq = new ArrayList<GoiDauTruong.DoiThu>();
        final List<Integer> cho = new ArrayList<Integer>();
        for (final DauTruongLogic.UngVien u : chon) {
            final GoiDauTruong.DoiThu x = new GoiDauTruong.DoiThu();
            final Object[] f = phu.get(Integer.valueOf(u.id));
            x.id = u.id;
            x.ten = f == null ? "" : String.valueOf(f[0]);
            x.diem = u.diem;
            x.bac = DauTruongLogic.bac(u.diem);
            x.suc = u.sucManh;
            x.daGap = u.tran24h;
            final String[] hinh = f == null ? new String[0] : String.valueOf(f[1]).split(",");
            for (int k = 0; k < 3 && k < hinh.length; k++) {
                final String[] q = hinh[k].split("\\.");
                try {
                    x.dex[k] = Integer.parseInt(q[0]);
                    x.shiny[k] = q.length > 1 && "1".equals(q[1]);
                    x.sao[k] = q.length > 2 ? Integer.parseInt(q[2]) : 0;
                } catch (final NumberFormatException ignored) {
                }
            }
            kq.add(x);
            cho.add(Integer.valueOf(u.id));
        }
        DE_XUAT.put(Integer.valueOf(id), cho);
        final String ghi = kq.isEmpty() ? "Chưa có đối thủ hợp tầm (điểm ±" + cua + ", sức mạnh ±" + DauTruongLogic.daiSucManh(lm[1])
                + "%). Làm mới để nới tầm." : "";
        final int con = Math.max(0, DauTruongLogic.LAM_MOI_NGAY - lm[1]);
        gui(p, o -> GoiDauTruong.doiThu(o, kq, con, ghi));
    }

    // ================================================================== tran

    static final class TranDang {
        final long id;
        final int cong, thu;
        final String tenThu;
        final int diemCong, diemThu, mua, tuan, heSo;
        final DauTruongTran t;
        volatile long han;
        volatile boolean tuDong;
        volatile long tuDongSau;
        volatile boolean xong;

        TranDang(final long id, final int cong, final int thu, final String tenThu, final int diemCong, final int diemThu, final int mua,
                final int tuan, final int heSo, final DauTruongTran t) {
            this.id = id;
            this.cong = cong;
            this.thu = thu;
            this.tenThu = tenThu;
            this.diemCong = diemCong;
            this.diemThu = diemThu;
            this.mua = mua;
            this.tuan = tuan;
            this.heSo = heSo;
            this.t = t;
        }

        int giayCon() {
            return (int) Math.max(0L, Math.min(DauTruongLogic.GIAY_LUOT, (this.han - System.currentTimeMillis() + 999L) / 1000L));
        }
    }

    /** charDBID ben tan cong -&gt; tran dang danh. */
    static final ConcurrentHashMap<Integer, TranDang> TRAN = new ConcurrentHashMap<Integer, TranDang>();
    private static volatile Timer hen;

    static synchronized void batHen() {
        if (hen != null) {
            return;
        }
        hen = new Timer("dau-truong", true);
        hen.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    nhip(System.currentTimeMillis());
                } catch (final Throwable e) {
                    System.out.println("[DAU TRUONG] nhip: " + e);
                }
            }
        }, 1000L, 500L);
    }

    static Char online(final int id) {
        try {
            return CharManager.instance.getCharByCharDbID(id);
        } catch (final Exception e) {
            return null;
        }
    }

    /** Doi phong thu cua {@code idThu} (con THAT trong Hop PC luc nay); null = khong con hop le. */
    static HopPc hopCua(final int id) {
        final Char c = online(id);
        if (c != null) {
            return BatPokemonGame.hop(c);
        }
        final String s = DatabaseBoNgoc.napHoSo(DatabaseBoNgoc.BANG_HOP_PC, id);
        return s == null ? null : HopPc.giaiMa(s);
    }

    static DauTruongTran.Con[] taoDoi(final DauTruongDuLieu d, final int[] ids, final HopPc h) {
        final DauTruongTran.Con[] kq = new DauTruongTran.Con[ids.length];
        for (int i = 0; i < ids.length; i++) {
            final HopPc.Con c = h.lay(ids[i]);
            kq[i] = DauTruongTran.taoCon(d, c.dex, c.shiny, c.iv, c.tinhCach);
            kq[i].idHop = c.id;
        }
        return kq;
    }

    static void thach(final Char p, final int idThu) {
        final int id = p.charDBID;
        if (TRAN.containsKey(Integer.valueOf(id))) {
            ThongBao.loi(p, "Bạn đang trong một trận Đấu trường.", null);
            return;
        }
        if (p.lvDetail != null && p.lvDetail.lv < DauTruongLogic.CAP_TOI_THIEU) {
            ThongBao.loi(p, "Cần cấp " + DauTruongLogic.CAP_TOI_THIEU + " để vào Đấu trường Bảo Bối.", null);
            return;
        }
        final List<Integer> dx = DE_XUAT.get(Integer.valueOf(id));
        if (dx == null || !dx.contains(Integer.valueOf(idThu))) {
            ghiChu(p, "Đối thủ này không còn trong danh sách. Tìm lại đối thủ.");
            return;
        }
        if (DauTruongLogic.luotConLai(daDanhHomNay(id)) <= 0) {
            ThongBao.loi(p, "Hết " + DauTruongLogic.LUOT_NGAY + " lượt Đấu trường hôm nay.", "Lượt mới lúc 0 giờ.");
            return;
        }
        final DauTruongDuLieu d = duLieu();
        final HopPc h = BatPokemonGame.hop(p);
        final int[] cong = DauTruongLogic.docIds(doiHinh(id)[0]);
        if (DauTruongLogic.loiDoi(cong, h, d) != null) {
            ThongBao.loi(p, "Đội tấn công không còn hợp lệ: chọn lại đội.", null);
            return;
        }
        final String[] dhThu = doiHinh(idThu);
        final int[] thu = DauTruongLogic.docIds(dhThu[1]);
        final HopPc hThu = thu == null ? null : hopCua(idThu);
        if (DauTruongLogic.loiDoi(thu, hThu, d) != null) {
            dx.remove(Integer.valueOf(idThu));
            ghiChu(p, "Đội phòng thủ của đối thủ vừa thay đổi. Chọn đối thủ khác.");
            return;
        }
        final String[] tenThu = {""};
        doc("SELECT ten FROM tob_dau_truong_doi WHERE id_char = ?", rs -> tenThu[0] = rs.getString(1), Integer.valueOf(idThu));
        final int mua = mua();
        final int diemCong = diem(mua, id), diemThu = diem(mua, idThu);
        final int heSo = DauTruongLogic.heSoLap(tran24h(id, idThu));
        final long hat = new java.security.SecureRandom().nextLong();
        final DauTruongTran t = new DauTruongTran(hat, taoDoi(d, cong, h), taoDoi(d, thu, hThu));
        final long tranId = chenTran(mua, tuan(), homNay(), id, idThu, hat, diemCong, diemThu);
        if (tranId <= 0) {
            ThongBao.loi(p, "Chưa vào trận được, thử lại sau.", null);
            return;
        }
        final TranDang td = new TranDang(tranId, id, idThu, tenThu[0], diemCong, diemThu, mua, tuan(), heSo, t);
        td.han = System.currentTimeMillis() + DauTruongLogic.GIAY_LUOT * 1000L;
        dx.remove(Integer.valueOf(idThu));
        TRAN.put(Integer.valueOf(id), td);
        batHen();
        gui(p, o -> GoiDauTruong.batDau(o, (int) tranId, tenThu[0], diemThu, DauTruongLogic.GIAY_LUOT, t, td.giayCon(), false));
    }

    static TranDang tranCua(final Char p, final int tranId) {
        final TranDang td = TRAN.get(Integer.valueOf(p.charDBID));
        return td != null && (int) td.id == tranId && !td.xong ? td : null;
    }

    static void hanhDong(final Char p, final int tranId, final DauTruongTran.HanhDong h) {
        final TranDang td = tranCua(p, tranId);
        if (td == null) {
            return;
        }
        synchronized (td) {
            if (td.xong) {
                return;
            }
            if (!td.t.hopLe(DauTruongTran.CONG, h)) {
                ghiChu(p, h.loai == DauTruongTran.HanhDong.DOI ? "Không đổi sang Pokémon đó được." : "Chiêu này hết PP.");
                return;
            }
            choiLuot(p, td, h);
        }
    }

    static void tuDong(final Char p, final int tranId, final boolean bat) {
        final TranDang td = tranCua(p, tranId);
        if (td == null) {
            return;
        }
        td.tuDong = bat;
        td.tuDongSau = System.currentTimeMillis() + 600L;
    }

    static void dauHang(final Char p, final int tranId) {
        final TranDang td = tranCua(p, tranId);
        if (td == null) {
            return;
        }
        synchronized (td) {
            if (td.xong) {
                return;
            }
            td.t.thang = DauTruongTran.THU;
            td.t.nhatKy.add("Bạn đã đầu hàng.");
            ketThuc(p, td);
        }
    }

    /** Mot luot (trong khoa td). */
    static void choiLuot(final Char p, final TranDang td, final DauTruongTran.HanhDong h) {
        final List<String> dong = td.t.choi(h);
        td.han = System.currentTimeMillis() + DauTruongLogic.GIAY_LUOT * 1000L;
        td.tuDongSau = System.currentTimeMillis() + 1600L;
        if (p != null) {
            gui(p, o -> GoiDauTruong.luot(o, (int) td.id, td.t, dong, td.giayCon(), td.tuDong));
        }
        if (td.t.xong()) {
            ketThuc(p, td);
        }
    }

    static void nhip(final long bay) {
        for (final TranDang td : new ArrayList<TranDang>(TRAN.values())) {
            synchronized (td) {
                if (td.xong) {
                    TRAN.remove(Integer.valueOf(td.cong), td);
                    continue;
                }
                final Char p = online(td.cong);
                if (p == null) {
                    // roi game: may danh not (khong tron thua bang cach thoat)
                    td.t.chayHet();
                    ketThuc(null, td);
                    continue;
                }
                if (td.tuDong ? bay >= td.tuDongSau : bay >= td.han) {
                    choiLuot(p, td, null);
                }
            }
        }
    }

    static void ketThuc(final Char p, final TranDang td) {
        if (td.xong) {
            return;
        }
        td.xong = true;
        TRAN.remove(Integer.valueOf(td.cong), td);
        final boolean thang = td.t.thang == DauTruongTran.CONG;
        final int diemSau = DauTruongLogic.diemCong(td.diemCong, td.diemThu, thang ? 1 : 0, td.heSo);
        final int thuSau = DauTruongLogic.diemThu(td.diemThu, td.diemCong, thang ? 1 : 0, td.heSo);
        final int luong = DauTruongLogic.luongTran(thang, td.heSo, luongTuan(td.cong));
        final StringBuilder hd = new StringBuilder();
        for (final String s : td.t.hanhDongCong) {
            if (hd.length() + s.length() + 1 > 250) {
                break;
            }
            hd.append(hd.length() > 0 ? "," : "").append(s);
        }
        DatabaseLauDai.ghiCong("UPDATE tob_dau_truong_tran SET ket_qua = ?, diem_cong_sau = ?, diem_thu_sau = ?, he_so = ?, luong = ?,"
                + " luot = ?, hanh_dong = ?, luc = NOW() WHERE id = ?", Integer.valueOf(thang ? 1 : 0), Integer.valueOf(diemSau),
                Integer.valueOf(thuSau), Integer.valueOf(td.heSo), Integer.valueOf(luong), Integer.valueOf(td.t.luot), hd.toString(),
                Long.valueOf(td.id));
        final int dC = diemSau - td.diemCong, dT = thuSau - td.diemThu;
        DatabaseLauDai.ghiCong("INSERT INTO tob_dau_truong_mua (mua, id_char, diem, thang, thua, so_tran, cap_nhat) VALUES (?, ?, ?, ?, ?, 1, NOW())"
                + " ON DUPLICATE KEY UPDATE diem = GREATEST(?, diem + ?), thang = thang + VALUES(thang), thua = thua + VALUES(thua),"
                + " so_tran = so_tran + 1, cap_nhat = VALUES(cap_nhat)", Integer.valueOf(td.mua), Integer.valueOf(td.cong),
                Integer.valueOf(Math.max(DauTruongLogic.DIEM_SAN, DauTruongLogic.DIEM_BAN_DAU + dC)), Integer.valueOf(thang ? 1 : 0),
                Integer.valueOf(thang ? 0 : 1), Integer.valueOf(DauTruongLogic.DIEM_SAN), Integer.valueOf(dC));
        DatabaseLauDai.ghiCong("INSERT INTO tob_dau_truong_mua (mua, id_char, diem, thu_thang, thu_thua, cap_nhat) VALUES (?, ?, ?, ?, ?, NOW())"
                + " ON DUPLICATE KEY UPDATE diem = GREATEST(?, diem + ?), thu_thang = thu_thang + VALUES(thu_thang),"
                + " thu_thua = thu_thua + VALUES(thu_thua), cap_nhat = VALUES(cap_nhat)", Integer.valueOf(td.mua), Integer.valueOf(td.thu),
                Integer.valueOf(Math.max(DauTruongLogic.DIEM_SAN, DauTruongLogic.DIEM_BAN_DAU + dT)), Integer.valueOf(thang ? 0 : 1),
                Integer.valueOf(thang ? 1 : 0), Integer.valueOf(DauTruongLogic.DIEM_SAN), Integer.valueOf(dT));
        final Char nguoi = p != null ? p : online(td.cong);
        if (luong > 0 && nguoi != null) {
            try {
                data.DatabaseGiftCode.applyItemString(nguoi, "LUONGKHOA:" + luong);
                nguoi.sendMessage(MessageCreator.createMainCharInfoMessage(nguoi));
            } catch (final Exception e) {
                System.out.println("[DAU TRUONG] luong khoa " + td.cong + ": " + e);
            }
        } else if (luong > 0) {
            // ben tan cong da roi game truoc khi tran xong: ghi so 0 (khong phat bu offline, tranh ghi de tob_char)
            DatabaseLauDai.ghiCong("UPDATE tob_dau_truong_tran SET luong = 0 WHERE id = ?", Long.valueOf(td.id));
        }
        try {
            Database.instance.saveOrtherLog("", nguoi == null ? String.valueOf(td.cong) : nguoi.charname, "dau truong " + td.id + " vs "
                    + td.thu + (thang ? " THANG" : " THUA") + " diem " + td.diemCong + "->" + diemSau + " he so " + td.heSo + " luong "
                    + luong, "dautruong");
        } catch (final Exception ignored) {
        }
        if (nguoi != null && p != null) {
            final int luotCon = DauTruongLogic.luotConLai(daDanhHomNay(td.cong));
            final String chu = (thang ? "Thắng " : "Thua ") + td.tenThu + " · " + (td.heSo < 1000 ? "gặp lại đối thủ trong 24 giờ: điểm ×"
                    + (td.heSo / 10) + "%" : "điểm đầy đủ") + (luong > 0 ? " · +" + luong + " lượng khoá" : "");
            gui(p, o -> GoiDauTruong.ketQua(o, (int) td.id, thang, td.diemCong, diemSau, dT, luong, td.heSo, luotCon, chu));
        }
        final Char thu = online(td.thu);
        if (thu != null) {
            ThongBao.gui(thu, ThongBao.TIN, "Đấu trường: " + (nguoi == null ? "một người chơi" : nguoi.charname)
                    + (thang ? " đã thắng đội phòng thủ của bạn" : " thua đội phòng thủ của bạn"),
                    "Điểm " + td.diemThu + " thành " + thuSau + ".", ThongBao.HINH_KHONG, 0);
        }
    }

    // ================================================================== thuong mua

    /** Dang nhap: thuong moi mua Dau truong da ket thuc chua phat (danh hieu mua co han; khong luong / xu). Mot lan / mua. */
    public static void thuongMuaCu(final Char p) {
        if (p == null || p.charDBID <= 0 || p.isBot != -1 || !bat()) {
            return;
        }
        final int muaNay = mua();
        final List<int[]> ds = new ArrayList<int[]>();
        final boolean ok = doc("SELECT m.mua, m.diem, m.so_tran FROM tob_dau_truong_mua m WHERE m.id_char = ? AND m.mua < ? AND m.so_tran > 0"
                + " AND NOT EXISTS (SELECT 1 FROM tob_dau_truong_thuong t WHERE t.mua = m.mua AND t.id_char = m.id_char)",
                rs -> ds.add(new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3)}), Integer.valueOf(p.charDBID), Integer.valueOf(muaNay));
        if (!ok) {
            return;
        }
        for (final int[] m : ds) {
            final int hang = 1 + soNguyen("SELECT COUNT(*) FROM tob_dau_truong_mua WHERE mua = ? AND so_tran >= ? AND (diem > ? OR (diem = ?"
                    + " AND id_char < ?))", 0, Integer.valueOf(m[0]), Integer.valueOf(DauTruongLogic.TRAN_TOI_THIEU_MUA),
                    Integer.valueOf(m[1]), Integer.valueOf(m[1]), Integer.valueOf(p.charDBID));
            final int dhId = DauTruongLogic.danhHieuMua(hang, m[1], m[2]);
            final int chen = DatabaseLauDai.ghiCong("INSERT IGNORE INTO tob_dau_truong_thuong (mua, id_char, hang, diem, so_tran, danh_hieu, luc)"
                    + " VALUES (?, ?, ?, ?, ?, ?, NOW())", Integer.valueOf(m[0]), Integer.valueOf(p.charDBID), Integer.valueOf(hang),
                    Integer.valueOf(m[1]), Integer.valueOf(m[2]), Integer.valueOf(dhId));
            if (chen != 1 || dhId == 0) {
                continue;
            }
            final com.htbb.pokemon.DanhHieuLogic.Mau dh = DanhHieuGame.mauTheoMa(DauTruongLogic.maDanhHieu(dhId));
            if (dh != null) {
                DanhHieuGame.trao(p, dh, "dau_truong_mua_" + m[0]);
            }
            Database.instance.saveOrtherLog("", p.charname, "thuong mua dau truong " + m[0] + " hang " + hang + " diem " + m[1]
                    + " danh hieu " + dhId, "dautruong");
            ThongBao.gui(p, ThongBao.HIEM, "Đấu trường Bảo Bối mùa " + (m[0] + 1) + ": hạng " + hang,
                    dh == null ? "Điểm " + m[1] + "." : "Danh hiệu " + dh.ten + " (giữ một mùa).", ThongBao.HINH_KHONG, 0);
        }
    }

    // ================================================================== kiem thu

    public static void xoaHet() {
        TRAN.clear();
        DE_XUAT.clear();
        LAM_MOI.clear();
    }
}
