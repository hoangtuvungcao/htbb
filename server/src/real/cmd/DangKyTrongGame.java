package real.cmd;

import data.CamTheoMay;
import data.Database;
import data.QuyTacDangKy;
import io.Message;
import io.Session;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import real.MessageCreator;
import server.TeamServer;

/**
 * DANG KY TAI KHOAN TRONG GAME (lenh 39) - jar / Android (chung code j2me) / PC Unity.
 *
 * LICH SU: lenh 39 tung tro vao real.RequestRegisterHandler (lib/htbb-core.jar) - tao tai khoan o
 * trang thai chua dang nhap, khong han muc, khong dau vet -> da bi dong (DangKyDaChuyenSangWeb, nay
 * da xoa). Quyet dinh moi (10/2026): dang ky phai chay ca TRONG GAME lan tren web, nen mo lai bang
 * handler nay, voi DUNG cac lop chan cua web:
 *
 *   1. QUY TAC CHUNG: data.QuyTacDangKy + QuyTacDangKyDuLieu, sinh tu
 *      HTBB-Web/shared/data/quy-tac-dang-ky.json - cung tep web dung. Khong co quy tac nao viet
 *      rieng o day (do dai, ky tu, ten quan tri, tu cam, han muc, cau bao loi).
 *   2. HAN MUC GOP voi web: dem bang htbb_db.web_chan_spam (ip, hanh_dong) - web ghi 'dang_ky',
 *      game ghi 'dang_ky_game', ca hai dem IN ('dang_ky','dang_ky_game') trong 1 gio / 24 gio. Them
 *      mot bo dem theo MA MAY (hanh_dong 'dang_ky_game_may', cot ip = "may:" + md5). Thieu bang
 *      hoac loi CSDL thi van con bo dem trong RAM (khong bao gio mo toang).
 *   3. CHONG BOT: phai xin CAU DO truoc (phep tinh nho, so viet bang chu lan so), tra loi sau it
 *      nhat 2 giay va trong 5 phut, dung mot lan; moi phien toi da 5 lan gui; tra loi cham ~0.8 s.
 *      May bi cam (CamTheoMay) khong dang ky duoc.
 *   4. CUNG DONG CSDL voi web: account.team_user (PASSWORD() - khop CheckLoginSocket.jar), Y HET
 *      7 cot dau cua web, ke ca fromgame=0: CheckLoginSocket.jar tra cot fromgame ve lam "bRegion"
 *      va LoginHandler.onLoginOK chi cho vao khi bRegion la 0 hoac 9 - dat fromgame=1 la tai khoan
 *      KHONG BAO GIO dang nhap duoc (da thu 18/10/2026). Nguon "game" ghi o nhat ky + web_chan_spam
 *      (hanh_dong 'dang_ky_game'); kich hoat 5h_active 30 ngay theo chinh sach HTBB_AUTO_ACTIVATE
 *      cua web (web cong bo vao cau_hinh 'web.autoActivate', xem sv.dangKyTuKichHoat).
 *   5. NHAT KY: moi lan thanh cong/bi chan ghi 1 dong vao sv.pathLog/dang_ky_game.log + stdout.
 *
 * TAT: server.ini sv.dangKyTrongGame=0 -> tra loi nhan chi sang web nhu truoc.
 *
 * GIAO THUC (moi, danh dau bang chuoi "#dk2" - khong bao gio la ten tai khoan hop le):
 *   client -> server  39: UTF "#dk2", byte buoc
 *       buoc 0 (xin cau do): UTF maMay
 *       buoc 1 (gui):        UTF ten, UTF matKhau, UTF nhapLai, UTF email, UTF traLoi, UTF maMay
 *   server -> client  39: UTF "#dk2", byte loai
 *       loai 0 cau do:     UTF cauHoi
 *       loai 1 thanh cong: UTF ten, UTF thongBao, boolean daKichHoat
 *       loai 2 loi:        byte n, n x (UTF ma, UTF loi), UTF cauHoiMoi ("" = khong cap moi)
 *   Client cu (goi 39 khong co "#dk2", cho tra loi kieu SMS) chi nhan mot hop thong bao.
 */
public final class DangKyTrongGame implements ICommandHandler {

    public static final String DAU = "#dk2";
    public static final String HANH_DONG_IP = "dang_ky_game";
    public static final String HANH_DONG_MAY = "dang_ky_game_may";

    /** server.ini sv.dangKyTrongGame (mac dinh 1). */
    public static volatile boolean batDangKy = true;
    /** server.ini sv.dangKyTuKichHoat: "web" (mac dinh, theo HTBB_AUTO_ACTIVATE), "1", "0". */
    public static volatile String cheDoKichHoat = "web";
    /** server.ini sv.dangKyCsdlTaiKhoan (mac dinh "account"). */
    public static volatile String csdlTaiKhoan = "account";
    /** server.ini sv.dangKyTreMs: tra loi cham chong bot (mac dinh 800). */
    public static volatile int treMs = 800;

    public static final long CAU_DO_SOM_NHAT_MS = 2000L;
    public static final long CAU_DO_HET_HAN_MS = 5L * 60L * 1000L;
    static final int TOI_DA_LAN_GUI_MOI_PHIEN = 5;
    private static final long GIO_MS = 3600L * 1000L;
    private static final long NGAY_MS = 24L * GIO_MS;

    private static final SecureRandom NGAU_NHIEN = new SecureRandom();
    private static final Object KHOA = new Object();

    /** Cau do dang cho theo phien. WeakHashMap: phien dong thi tu roi. */
    private static final Map<Session, CauDo> CAU_DO = new WeakHashMap<Session, CauDo>();
    /** Bo dem du phong trong RAM: khoa -> cac moc thanh cong (ms). */
    private static final Map<String, List<Long>> BO_NHO = new HashMap<String, List<Long>>();

    /** Cau do chong bot (public cho kiem thu). */
    public static final class CauDo {
        public final String cauHoi;
        public final int dapAn;
        public final long luc;
        int soLanGui;

        public CauDo(final String cauHoi, final int dapAn, final long luc) {
            this.cauHoi = cauHoi;
            this.dapAn = dapAn;
            this.luc = luc;
        }
    }

    /** Doc cau hinh tu server.ini (goi trong TeamServer luc khoi dong; null = mac dinh). */
    public static void docCauHinh(final String bat, final String tuKichHoat, final String csdl, final String tre) {
        batDangKy = !"0".equals(bat == null ? "1" : bat.trim());
        final String kh = tuKichHoat == null ? "web" : tuKichHoat.trim().toLowerCase();
        cheDoKichHoat = ("1".equals(kh) || "0".equals(kh)) ? kh : "web";
        final String cs = csdl == null ? "account" : csdl.trim();
        csdlTaiKhoan = cs.matches("[A-Za-z0-9_]{1,64}") ? cs : "account";
        try {
            treMs = tre == null ? 800 : Math.max(0, Math.min(5000, Integer.parseInt(tre.trim())));
        } catch (final NumberFormatException e) {
            treMs = 800;
        }
        System.out.println("DANG KY TRONG GAME: " + (batDangKy ? "BAT" : "TAT") + ", tu kich hoat=" + cheDoKichHoat
                + ", han muc " + data.QuyTacDangKyDuLieu.HAN_MUC_MOI_GIO + "/gio " + data.QuyTacDangKyDuLieu.HAN_MUC_MOI_NGAY + "/ngay");
    }

    @Override
    public void process(final Session session, final Message message) {
        if (session == null) {
            return;
        }
        try {
            final DataInputStream dis = message.dis;
            String dau;
            try {
                dau = dis.readUTF();
            } catch (final Exception e) {
                dau = "";
            }
            if (!batDangKy) {
                guiHop(session, TeamServer.isServerIndo()
                        ? "Registration has moved to our website. Please register there, then log in here."
                        : "Đăng ký trong game đang tạm tắt. Bạn vui lòng đăng ký trên trang web của máy chủ rồi quay lại đây đăng nhập.");
                return;
            }
            if (!DAU.equals(dau)) {
                guiHop(session, "Phiên bản game của bạn đã cũ, chưa đăng ký được trong game. "
                        + "Vui lòng cập nhật game mới hoặc đăng ký trên trang web của máy chủ.");
                return;
            }
            if (session.userID > 0) {
                guiLoi(session, mot("chung", "Bạn đang đăng nhập, không thể đăng ký thêm tài khoản lúc này."), "");
                return;
            }
            // Chong spam goi dang ky theo IP (sv.spam.hd.dangKy, mac dinh 5/60s) - TRUOC moi viec
            // ton kem (cau do, CSDL, tre 800ms). Han muc 3/gio 10/ngay ben duoi van ap.
            if (!com.htbb.chongspam.ChongSpam.choHanhDong(com.htbb.chongspam.ChongSpam.cua("dk:" + session.ip),
                    com.htbb.chongspam.ChongSpam.HanhDong.DANG_KY, System.currentTimeMillis())) {
                server.ChiSoMayChu.THAO_TAC_CHAN.incrementAndGet();
                guiLoi(session, mot("chung", "Thao tác quá nhanh, vui lòng chờ một phút rồi thử lại."), "");
                return;
            }
            final byte buoc = dis.readByte();
            if (buoc == 0) {
                final String maMay = CamTheoMay.lamSach(dis.readUTF());
                if (CamTheoMay.biCam(maMay)) {
                    ghiNhatKy("CHAN may bi cam", session.ip, maMay, "");
                    guiLoi(session, mot("chung", "Thiết bị này đã bị khoá, không thể tạo tài khoản."), "");
                    return;
                }
                guiCauDo(session, capCauDo(session));
                return;
            }
            if (buoc != 1) {
                return;
            }
            final String ten = dis.readUTF().trim().toLowerCase();
            final String matKhau = dis.readUTF();
            final String nhapLai = dis.readUTF();
            final String email = dis.readUTF().trim();
            final String traLoi = dis.readUTF().trim();
            final String maMay = CamTheoMay.lamSach(dis.readUTF());
            xuLyGui(session, ten, matKhau, nhapLai, email, traLoi, maMay);
        } catch (final Exception e) {
            System.out.println("DANG KY TRONG GAME: goi hong tu " + session.ip + ": " + e);
        }
    }

    private void xuLyGui(final Session session, final String ten, final String matKhau, final String nhapLai,
                         final String email, final String traLoi, final String maMay) {
        tre();
        // 1. Cau do: bat buoc, dung mot lan.
        final CauDo cd;
        synchronized (CAU_DO) {
            cd = CAU_DO.remove(session);
        }
        final long bay = System.currentTimeMillis();
        final String loiCauDo = kiemCauDo(cd, traLoi, bay);
        if (loiCauDo != null) {
            if (cd != null && cd.soLanGui + 1 >= TOI_DA_LAN_GUI_MOI_PHIEN) {
                ghiNhatKy("CHAN qua so lan thu", session.ip, maMay, ten);
                guiLoi(session, mot("chung", "Bạn đã thử quá nhiều lần. Vui lòng thoát ra rồi vào lại."), "");
                return;
            }
            final CauDo moi = capCauDo(session);
            if (cd != null) {
                moi.soLanGui = cd.soLanGui + 1;
            }
            guiLoi(session, mot("traLoi", loiCauDo), moi.cauHoi);
            return;
        }
        final int lanGui = cd.soLanGui + 1;
        if (CamTheoMay.biCam(maMay)) {
            guiLoi(session, mot("chung", "Thiết bị này đã bị khoá, không thể tạo tài khoản."), "");
            return;
        }
        // 2. Quy tac chung voi web.
        final List<QuyTacDangKy.Loi> loi = QuyTacDangKy.kiemTra(ten, matKhau, nhapLai, email, QuyTacDangKy.NGUON_GAME);
        if (!loi.isEmpty()) {
            final List<String[]> ds = new ArrayList<String[]>();
            for (final QuyTacDangKy.Loi l : loi) {
                ds.add(new String[]{l.ma, l.loi});
            }
            guiLoi(session, ds, capCauDoTiep(session, lanGui).cauHoi);
            return;
        }
        // 3. Han muc + tao: tuan tu hoa toan bo (dem-roi-ghi khong dua duoc). Dang ky hiem, moi lan vai ms.
        String ketQua;
        int userId = -1;
        synchronized (KHOA) {
            ketQua = kiemHanMuc(session.ip, maMay, bay);
            if (ketQua == null) {
                try {
                    userId = taoTaiKhoan(ten, matKhau, email);
                    if (userId == -2) {
                        ketQua = "trungTen";
                    } else {
                        ghiLuot(session.ip, maMay, bay);
                    }
                } catch (final Exception e) {
                    System.out.println("DANG KY TRONG GAME: loi tao tai khoan " + ten + ": " + e);
                    ketQua = "loiHeThong";
                }
            }
        }
        if (ketQua != null) {
            if (ketQua.startsWith("qua")) {
                ghiNhatKy("CHAN han muc " + ketQua, session.ip, maMay, ten);
            }
            guiLoi(session, mot(ketQua, QuyTacDangKy.thongBao(ketQua)),
                    ketQua.startsWith("qua") ? "" : capCauDoTiep(session, lanGui).cauHoi);
            return;
        }
        final boolean kichHoat = canTuKichHoat() && kichHoat(userId, ten);
        ghiNhatKy("TAO OK id=" + userId + (kichHoat ? " (da kich hoat)" : " (chua kich hoat)")
                + (email.isEmpty() ? "" : " co email"), session.ip, maMay, ten);
        // kich hoat: mac dinh chua kich hoat VAN vao game duoc (chi khoa vai tinh nang - ThanhVien.chan).
        final String tb = kichHoat || !Database.ENABLE_ACTIVE_CHECK
                ? "Tài khoản đã sẵn sàng, bạn có thể đăng nhập ngay."
                : (com.htbb.pokemon.ThanhVien.chanDangNhap()
                        ? "Tài khoản cần được kích hoạt trước khi vào game. Vui lòng liên hệ quản trị viên."
                        : "Tài khoản đã tạo, bạn có thể đăng nhập ngay. Một vài tính năng (giao dịch, chat thế giới, chợ...) mở sau khi kích hoạt.");
        guiThanhCong(session, ten, tb + (email.isEmpty()
                ? " Nên thêm email trên trang web để lấy lại mật khẩu khi quên." : ""), kichHoat);
    }

    // ------------------------------------------------------------------ cau do

    /** null = dung. */
    public static String kiemCauDo(final CauDo cd, final String traLoi, final long bay) {
        if (cd == null) {
            return "Phiên đăng ký đã hết hạn. Vui lòng trả lời câu hỏi mới.";
        }
        if (bay - cd.luc > CAU_DO_HET_HAN_MS) {
            return "Câu hỏi đã hết hạn. Vui lòng trả lời câu hỏi mới.";
        }
        if (bay - cd.luc < CAU_DO_SOM_NHAT_MS) {
            return "Bạn thao tác quá nhanh. Vui lòng trả lời câu hỏi mới.";
        }
        int so;
        try {
            so = Integer.parseInt(traLoi);
        } catch (final NumberFormatException e) {
            return "Câu trả lời phải là một số. Vui lòng trả lời câu hỏi mới.";
        }
        return so == cd.dapAn ? null : "Câu trả lời chưa đúng. Vui lòng trả lời câu hỏi mới.";
    }

    private static final String[] CHU_SO = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười"};

    /** Phep tinh nho, so viet ngau nhien bang chu hoac bang so. */
    public static CauDo taoCauDo(final long luc) {
        final int a = 1 + NGAU_NHIEN.nextInt(9);
        final int b = 1 + NGAU_NHIEN.nextInt(9);
        final boolean cong = NGAU_NHIEN.nextBoolean() || a == b;
        final int lon = Math.max(a, b);
        final int nho = Math.min(a, b);
        final int x = cong ? a : lon;
        final int y = cong ? b : nho;
        final String cauHoi = viet(x) + (cong ? " cộng " : " trừ ") + viet(y) + " bằng mấy?";
        return new CauDo(Character.toUpperCase(cauHoi.charAt(0)) + cauHoi.substring(1), cong ? x + y : x - y, luc);
    }

    private static String viet(final int n) {
        return NGAU_NHIEN.nextBoolean() ? CHU_SO[n] : String.valueOf(n);
    }

    private static CauDo capCauDo(final Session s) {
        final CauDo cd = taoCauDo(System.currentTimeMillis());
        synchronized (CAU_DO) {
            CAU_DO.put(s, cd);
        }
        return cd;
    }

    private static CauDo capCauDoTiep(final Session s, final int lanGui) {
        final CauDo cd = capCauDo(s);
        cd.soLanGui = lanGui;
        return cd;
    }

    // ------------------------------------------------------------------ han muc

    /** "quaGio" | "quaNgay" | null. Lay MAX cua dem CSDL (gop web+game) va dem RAM. */
    private static String kiemHanMuc(final String ip, final String maMay, final long bay) {
        final int[] demIp = demCsdl(ip, "'dang_ky','" + HANH_DONG_IP + "'");
        final int[] ramIp = demRam("ip|" + ip, bay);
        String kq = QuyTacDangKy.vuotHanMuc(Math.max(demIp[0], ramIp[0]), Math.max(demIp[1], ramIp[1]), 0);
        if (kq != null || maMay.isEmpty()) {
            return kq;
        }
        final int[] demMay = demCsdl(khoaMay(maMay), "'" + HANH_DONG_MAY + "'");
        final int[] ramMay = demRam("may|" + maMay, bay);
        return QuyTacDangKy.vuotHanMuc(Math.max(demMay[0], ramMay[0]), Math.max(demMay[1], ramMay[1]), 0);
    }

    public static int[] demRam(final String khoa, final long bay) {
        synchronized (BO_NHO) {
            final List<Long> ds = BO_NHO.get(khoa);
            if (ds == null) {
                return new int[]{0, 0};
            }
            int gio = 0;
            int ngay = 0;
            for (final Iterator<Long> it = ds.iterator(); it.hasNext(); ) {
                final long t = it.next();
                if (bay - t > NGAY_MS) {
                    it.remove();
                    continue;
                }
                ngay++;
                if (bay - t <= GIO_MS) {
                    gio++;
                }
            }
            return new int[]{gio, ngay};
        }
    }

    public static void ghiRam(final String khoa, final long bay) {
        synchronized (BO_NHO) {
            List<Long> ds = BO_NHO.get(khoa);
            if (ds == null) {
                ds = new ArrayList<Long>();
                BO_NHO.put(khoa, ds);
            }
            ds.add(bay);
            if (BO_NHO.size() > 5000) {
                for (final Iterator<Map.Entry<String, List<Long>>> it = BO_NHO.entrySet().iterator(); it.hasNext(); ) {
                    final List<Long> v = it.next().getValue();
                    if (v.isEmpty() || bay - v.get(v.size() - 1) > NGAY_MS) {
                        it.remove();
                    }
                }
            }
        }
    }

    /** Xoa bo dem RAM (chi dung trong kiem thu). */
    public static void xoaRam() {
        synchronized (BO_NHO) {
            BO_NHO.clear();
        }
    }

    /** {gio, ngay} tu web_chan_spam; loi/thieu bang -> {0,0} (con bo dem RAM). */
    private static int[] demCsdl(final String ip, final String dsHanhDong) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            final int[] kq = new int[2];
            final int[] giay = {3600, 86400};
            for (int i = 0; i < 2; i++) {
                final PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM web_chan_spam WHERE ip = ? AND hanh_dong IN ("
                        + dsHanhDong + ") AND thoi_gian > DATE_SUB(NOW(), INTERVAL " + giay[i] + " SECOND)");
                try {
                    ps.setString(1, ip);
                    final ResultSet rs = ps.executeQuery();
                    kq[i] = rs.next() ? rs.getInt(1) : 0;
                    rs.close();
                } finally {
                    ps.close();
                }
            }
            return kq;
        } catch (final Exception e) {
            return new int[]{0, 0};
        } finally {
            traKetNoi(conn);
        }
    }

    private static void ghiLuot(final String ip, final String maMay, final long bay) {
        ghiRam("ip|" + ip, bay);
        if (!maMay.isEmpty()) {
            ghiRam("may|" + maMay, bay);
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            chenLuot(conn, ip, HANH_DONG_IP);
            if (!maMay.isEmpty()) {
                chenLuot(conn, khoaMay(maMay), HANH_DONG_MAY);
            }
        } catch (final Exception ignored) {
            // thieu bang web_chan_spam: con bo dem RAM
        } finally {
            traKetNoi(conn);
        }
    }

    private static void chenLuot(final Connection conn, final String ip, final String hd) throws Exception {
        final PreparedStatement ps = conn.prepareStatement("INSERT INTO web_chan_spam (ip, hanh_dong) VALUES (?, ?)");
        try {
            ps.setString(1, ip.length() > 45 ? ip.substring(0, 45) : ip);
            ps.setString(2, hd);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    /** "may:" + md5(maMay) - vua cot ip varchar(45), khong luu ma may tho. */
    public static String khoaMay(final String maMay) {
        try {
            final byte[] h = MessageDigest.getInstance("MD5").digest(maMay.getBytes("UTF-8"));
            final StringBuilder sb = new StringBuilder("may:");
            for (final byte b : h) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (final Exception e) {
            return "may:" + Integer.toHexString(maMay.hashCode());
        }
    }

    // ------------------------------------------------------------------ CSDL

    /** id moi; -2 = trung ten. Nem ngoai le khi loi CSDL. Y het DANG_KY_INSERT_SQL cua web (fromgame=0 - xem dau tep). */
    private static int taoTaiKhoan(final String ten, final String matKhau, final String email) throws Exception {
        Connection conn = null;
        String catalogCu = null;
        try {
            conn = Database.instance.getConnection();
            catalogCu = conn.getCatalog();
            // NovaDB: moi ket noi chi thay MOT CSDL -> doi catalog (nhu AdminPanel doi mat khau), tra lai truoc khi free.
            conn.setCatalog(csdlTaiKhoan);
            PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM team_user WHERE username = ? LIMIT 1");
            try {
                ps.setString(1, ten);
                final ResultSet rs = ps.executeQuery();
                final boolean co = rs.next();
                rs.close();
                if (co) {
                    return -2;
                }
            } finally {
                ps.close();
            }
            ps = conn.prepareStatement("INSERT INTO team_user (`username`, `password`, `phone`, `regdate`, `ban`, `provider`, `fromgame`, `email`)"
                    + " VALUES (?, PASSWORD(?), '', NOW(), 0, 0, 0, ?)");
            try {
                ps.setString(1, ten);
                ps.setString(2, matKhau);
                if (email.isEmpty()) {
                    ps.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(3, email);
                }
                ps.executeUpdate();
            } catch (final java.sql.SQLException e) {
                final String m = String.valueOf(e.getMessage()).toLowerCase();
                if ("23000".equals(e.getSQLState()) || e.getErrorCode() == 1062 || m.contains("duplicate") || m.contains("unique")) {
                    return -2;
                }
                throw e;
            } finally {
                ps.close();
            }
            ps = conn.prepareStatement("SELECT id FROM team_user WHERE username = ? LIMIT 1");
            try {
                ps.setString(1, ten);
                final ResultSet rs = ps.executeQuery();
                final int id = rs.next() ? rs.getInt(1) : -1;
                rs.close();
                if (id <= 0) {
                    throw new IllegalStateException("khong doc lai duoc id");
                }
                return id;
            } finally {
                ps.close();
            }
        } finally {
            if (conn != null) {
                try {
                    if (catalogCu != null) {
                        conn.setCatalog(catalogCu);
                    }
                } catch (final Exception ignored) {
                } finally {
                    traKetNoi(conn);
                }
            }
        }
    }

    /** sv.dangKyTuKichHoat: "1"/"0" co dinh, "web" = doc cau_hinh 'web.autoActivate' do web cong bo. */
    public static boolean canTuKichHoat() {
        if ("1".equals(cheDoKichHoat)) {
            return true;
        }
        if ("0".equals(cheDoKichHoat)) {
            return false;
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            final PreparedStatement ps = conn.prepareStatement("SELECT gia_tri FROM cau_hinh WHERE khoa = 'web.autoActivate' LIMIT 1");
            try {
                final ResultSet rs = ps.executeQuery();
                final boolean v = rs.next() && "1".equals(String.valueOf(rs.getString(1)).trim());
                rs.close();
                return v;
            } finally {
                ps.close();
            }
        } catch (final Exception e) {
            return false; // nhu mac dinh HTBB_AUTO_ACTIVATE=false cua web
        } finally {
            traKetNoi(conn);
        }
    }

    /** Gia han 30 ngay - y het activateAccount() cua web (server/utils/auth.ts). */
    private static boolean kichHoat(final int userId, final String ten) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            final long timeEnd = System.currentTimeMillis() / 1000L + 30L * 86400L;
            PreparedStatement ps = conn.prepareStatement("DELETE FROM 5h_active WHERE userID = ? OR username = ?");
            try {
                ps.setInt(1, userId);
                ps.setString(2, ten);
                ps.executeUpdate();
            } finally {
                ps.close();
            }
            ps = conn.prepareStatement("INSERT INTO 5h_active (userID, username, time_end) VALUES (?, ?, ?)");
            try {
                ps.setInt(1, userId);
                ps.setString(2, ten);
                ps.setString(3, String.valueOf(timeEnd));
                return ps.executeUpdate() > 0;
            } finally {
                ps.close();
            }
        } catch (final Exception e) {
            System.out.println("DANG KY TRONG GAME: kich hoat hong " + ten + ": " + e);
            return false;
        } finally {
            traKetNoi(conn);
        }
    }

    private static void traKetNoi(final Connection conn) {
        try {
            if (conn != null) {
                Database.connPool.free(conn);
            }
        } catch (final Exception ignored) {
        }
    }

    // ------------------------------------------------------------------ gui goi / nhat ky

    private static List<String[]> mot(final String ma, final String loi) {
        final List<String[]> ds = new ArrayList<String[]>();
        ds.add(new String[]{ma, loi});
        return ds;
    }

    private static void tre() {
        if (treMs > 0) {
            try {
                Thread.sleep(treMs);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static Message dau(final int loai) throws Exception {
        final Message m = new Message(39);
        final DataOutputStream d = m.dos;
        d.writeUTF(DAU);
        d.writeByte(loai);
        return m;
    }

    private static void gui(final Session s, final Message m) {
        s.sendMessage(m);
        m.cleanup();
    }

    private static void guiCauDo(final Session s, final CauDo cd) {
        try {
            final Message m = dau(0);
            m.dos.writeUTF(cd.cauHoi);
            gui(s, m);
        } catch (final Exception ignored) {
        }
    }

    private static void guiThanhCong(final Session s, final String ten, final String tb, final boolean kichHoat) {
        try {
            final Message m = dau(1);
            m.dos.writeUTF(ten);
            m.dos.writeUTF(tb);
            m.dos.writeBoolean(kichHoat);
            gui(s, m);
        } catch (final Exception ignored) {
        }
    }

    private static void guiLoi(final Session s, final List<String[]> ds, final String cauHoiMoi) {
        try {
            final Message m = dau(2);
            m.dos.writeByte(ds.size());
            for (final String[] l : ds) {
                m.dos.writeUTF(l[0]);
                m.dos.writeUTF(l[1]);
            }
            m.dos.writeUTF(cauHoiMoi == null ? "" : cauHoiMoi);
            gui(s, m);
        } catch (final Exception ignored) {
        }
    }

    private static void guiHop(final Session s, final String loiNhan) {
        final Message m = MessageCreator.createServerAlertMessage(loiNhan, "");
        s.sendMessage(m);
        m.cleanup();
    }

    private static final Object KHOA_LOG = new Object();

    private static void ghiNhatKy(final String viec, final String ip, final String maMay, final String ten) {
        final String dong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + viec
                + " ten=" + ten + " ip=" + ip + " may=" + (maMay.isEmpty() ? "-" : khoaMay(maMay));
        System.out.println("DANG KY TRONG GAME: " + dong);
        final String thuMuc = data.UserLogger.PATH;
        if (thuMuc == null || thuMuc.isEmpty()) {
            return;
        }
        synchronized (KHOA_LOG) {
            Writer w = null;
            try {
                w = new OutputStreamWriter(new FileOutputStream(thuMuc + "dang_ky_game.log", true), "UTF-8");
                w.write(dong + "\n");
            } catch (final Exception ignored) {
            } finally {
                try {
                    if (w != null) {
                        w.close();
                    }
                } catch (final Exception ignored) {
                }
            }
        }
    }
}
