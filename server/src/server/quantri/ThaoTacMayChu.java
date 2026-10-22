package server.quantri;

import data.Database;
import io.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import real.AdminHandler;
import real.Char;
import real.CharManager;
import real.MessageCreator;
import real.cmd.LoginHandler;

/**
 * NGHIEP VU cua bang dieu khien may chu - KHONG co giao dien, chay tren luong nen (SwingWorker),
 * moi ham tra ve mot cau mo ta ket qua (tieng Viet) va KHONG nem loi ra ngoai.
 *
 * Moi ham THAY DOI trang thai deu ghi {@link KiemToanQuanTri}. Chi dung lai cac duong da co san
 * phia server (CharManager, MessageCreator, Database, bao_tri.txt, CauHinh, tob_su_kien,
 * GiftcodeAnToanLogic...) - khong phat minh co che moi khi da co.
 */
public final class ThaoTacMayChu {

    /** Diem an toan "ve thi tran" (sua ket nhan vat): diem xuat hien cua nhan vat moi (ban do 105, o 11,17). */
    public static int MAP_AN_TOAN = 105;
    public static int X_AN_TOAN = 11;
    public static int Y_AN_TOAN = 17;

    public static final String BANG_CAM = "tob_cam_quan_tri";

    static final Set<String> TEN_NPC = new HashSet<String>();

    static {
        for (final String n : new String[]{
            "phat lo", "tho ren than bi", "tong quan", "thohopthanhsocap", "thohopthanhcaocap", "thay ngu hanh",
            "dau truong", "hoa tieu", "doi tieu", "tong tieu dau", "chuyenlanhtho", "xa phu", "ta pho thong",
            "ta thong linh", "huu thong linh", "tran thong linh", "hao duyen", "tho san", "le quan", "ky nang bang",
            "nguyet lao", "market", "dich tram", "nguyetlao", "quanly", "thoren", "trangbi", "cuahang", "nhaboss",
            "nhapet", "nhathu", "nhacaythan", "xaphu", "lequan", "admin"}) {
            TEN_NPC.add(n);
        }
    }

    private ThaoTacMayChu() {
    }

    // ================================================================== nguoi choi online

    /** Nguoi choi THAT dang online (bo bot, linh thue, NPC, ten co @ / dau cach). */
    public static List<Char> nguoiChoiOnline() {
        final List<Char> kq = new ArrayList<Char>();
        try {
            final Vector<Char> ds = CharManager.instance.vChars;
            for (int i = ds.size() - 1; i >= 0; --i) {
                try {
                    final Char c = ds.get(i);
                    if (laNguoiChoi(c)) {
                        kq.add(c);
                    }
                } catch (final Exception ignored) {
                }
            }
        } catch (final Throwable ignored) {
        }
        return kq;
    }

    static boolean laNguoiChoi(final Char c) {
        if (!data.LenhWeb.laNguoiChoiThat(c) || c.charname == null) {
            return false;
        }
        final String t = c.charname;
        return !t.contains("@") && !t.contains(" ") && !TEN_NPC.contains(t.toLowerCase(Locale.ROOT));
    }

    public static Char tim(final String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            return null;
        }
        try {
            final Char c = CharManager.instance.getCharByCharName(ten.trim());
            return c != null && c.getSession() != null ? c : null;
        } catch (final Throwable t) {
            return null;
        }
    }

    /** Ten loai Pokemon cua nhan vat, rong neu khong co. */
    public static String tenLoai(final Char c) {
        try {
            final String t = com.htbb.pokemon.LoaiPokemon.ten(c.pokeDex);
            return t == null ? "" : t;
        } catch (final Throwable e) {
            return "";
        }
    }

    public static String tenMap(final int id) {
        try {
            return real.TenBanDoMayChu.tenHien(id);
        } catch (final Throwable e) {
            return "Map " + id;
        }
    }

    // ================================================================== thong bao

    public static final String[] KIEU_THONG_BAO = {"Chữ chạy trên đỉnh", "Hộp thoại giữa màn hình", "Thông báo tự tắt (dưới)"};

    /** Gui thong bao toan may chu - giu 3 kieu cua nut "Thong Bao" cu. */
    public static String thongBao(final int kieu, final String noiDung) {
        final String nd = noiDung == null ? "" : noiDung.trim();
        if (nd.isEmpty()) {
            return "Nội dung trống - không gửi.";
        }
        try {
            final Message msg;
            if (kieu == 0) {
                msg = MessageCreator.createthongbao(nd);
            } else if (kieu == 2) {
                msg = MessageCreator.createServerAlertAutoOffMessage(nd);
            } else {
                msg = MessageCreator.createServerAlertMessage(nd, "");
            }
            int n = 0;
            for (int j = 0; j < CharManager.instance.vChars.size(); ++j) {
                try {
                    CharManager.instance.vChars.elementAt(j).sendMessage(msg);
                    n++;
                } catch (final Exception ignored) {
                }
            }
            KiemToanQuanTri.ghi("THONG_BAO", KIEU_THONG_BAO[Math.max(0, Math.min(2, kieu))], nd);
            return "Đã gửi \"" + KIEU_THONG_BAO[Math.max(0, Math.min(2, kieu))] + "\" tới " + n + " Pokémon.";
        } catch (final Exception e) {
            return "Lỗi gửi thông báo: " + e.getMessage();
        }
    }

    public static String thongBaoRieng(final String ten, final String noiDung) {
        final Char p = tim(ten);
        if (p == null) {
            return "Pokémon " + ten + " không online.";
        }
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(noiDung, ""));
            KiemToanQuanTri.ghi("THONG_BAO_RIENG", p.charname, noiDung);
            return "Đã gửi thông báo riêng cho " + p.charname + ".";
        } catch (final Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    // ================================================================== kick / cam / cam chat

    public static String kick(final String ten, final String lyDo) {
        final Char p = tim(ten);
        if (p == null) {
            return "Không tìm thấy Pokémon online: " + ten;
        }
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(
                    lyDo == null || lyDo.trim().isEmpty()
                            ? "Tài khoản của bạn tạm bị kick trong giây lát để admin giải quyết. Xin đăng nhập sau ít phút nữa"
                            : "Bạn bị đưa ra khỏi máy chủ: " + lyDo.trim(), ""));
            p.getSession().disconnect(8);
            KiemToanQuanTri.ghi("KICK", p.charname, lyDo);
            return "Đã kick " + p.charname + ".";
        } catch (final Exception e) {
            return "Lỗi khi kick: " + e.getMessage();
        }
    }

    static void taoBangCam(final Connection c) throws Exception {
        try (Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS " + BANG_CAM + " (charname VARCHAR(30) NOT NULL, ly_do VARCHAR(255) NOT NULL DEFAULT '',"
                    + " het_han DATETIME NULL, boi VARCHAR(50) NOT NULL DEFAULT '', luc DATETIME NULL, PRIMARY KEY (charname))"
                    + " DEFAULT CHARSET=utf8");
        }
    }

    /**
     * Cam nhan vat (tob_char.ban=1 - dung cot cu ma dang nhap da kiem). phut &lt;= 0 = vinh vien.
     * Ly do + han ghi bang tob_cam_quan_tri; bang dieu khien tu mo khi het han (moCamHetHan).
     */
    public static String cam(final String ten, final String lyDo, final int phut) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Chưa nhập tên Pokémon.";
        }
        final String t = ten.trim();
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            int n;
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_char SET ban=1 WHERE charname=?")) {
                ps.setString(1, t);
                n = ps.executeUpdate();
            }
            if (n == 0) {
                return "Không có Pokémon " + t + " trong CSDL.";
            }
            taoBangCam(conn);
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + BANG_CAM + " WHERE charname=?")) {
                ps.setString(1, t);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + BANG_CAM
                    + " (charname, ly_do, het_han, boi, luc) VALUES (?, ?, ?, ?, NOW())")) {
                ps.setString(1, t);
                ps.setString(2, catChuoi(lyDo, 250));
                ps.setString(3, phut > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                        new Date(System.currentTimeMillis() + phut * 60000L)) : null);
                ps.setString(4, "panel");
                ps.executeUpdate();
            }
        } catch (final Exception e) {
            return "Lỗi khi cấm: " + e.getMessage();
        } finally {
            tra(conn);
        }
        final Char p = tim(t);
        if (p != null) {
            try {
                p.sendMessage(MessageCreator.createServerAlertMessage(phut > 0
                        ? "Tài khoản của bạn bị khoá " + DinhDang.thoiLuong(phut * 60000L) + ". Lý do: " + lyDo
                        : "Tài khoản của bạn đã bị khóa vĩnh viễn. Lý do: " + lyDo, ""));
                p.getSession().disconnect(8);
            } catch (final Exception ignored) {
            }
        }
        KiemToanQuanTri.ghi("CAM", t, (phut > 0 ? phut + " phut" : "vinh vien") + " - " + lyDo);
        return "Đã khoá " + t + (phut > 0 ? " trong " + DinhDang.thoiLuong(phut * 60000L) : " vĩnh viễn") + (p != null ? " (đã kick)." : ".");
    }

    public static String moCam(final String ten) {
        if (ten == null || ten.trim().isEmpty()) {
            return "Chưa nhập tên Pokémon.";
        }
        final String t = ten.trim();
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            int n;
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_char SET ban=0 WHERE charname=?")) {
                ps.setString(1, t);
                n = ps.executeUpdate();
            }
            try {
                taoBangCam(conn);
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + BANG_CAM + " WHERE charname=?")) {
                    ps.setString(1, t);
                    ps.executeUpdate();
                }
            } catch (final Exception ignored) {
            }
            KiemToanQuanTri.ghi("MO_CAM", t, n > 0 ? "ok" : "khong co nhan vat");
            return n > 0 ? "Đã mở khoá " + t + "." : "Không có Pokémon " + t + " trong CSDL.";
        } catch (final Exception e) {
            return "Lỗi khi mở khoá: " + e.getMessage();
        } finally {
            tra(conn);
        }
    }

    /** Mo cac lenh cam co thoi han da het (bang dieu khien goi moi phut). Tra ve so da mo. */
    public static int moCamHetHan() {
        final List<String> het = new ArrayList<String>();
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            taoBangCam(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT charname FROM " + BANG_CAM
                    + " WHERE het_han IS NOT NULL AND het_han <= NOW()");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    het.add(rs.getString(1));
                }
            }
        } catch (final Exception e) {
            return 0;
        } finally {
            tra(conn);
        }
        for (final String t : het) {
            moCam(t);
        }
        return het.size();
    }

    /** Danh sach dang bi cam (bang dieu khien): ten, ly do, het han. */
    public static List<Object[]> dsCam() {
        final List<Object[]> l = new ArrayList<Object[]>();
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            taoBangCam(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT charname, ly_do, het_han, luc FROM " + BANG_CAM + " ORDER BY luc DESC");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    l.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3) == null ? "Vĩnh viễn" : rs.getString(3),
                        rs.getString(4)});
                }
            }
        } catch (final Exception ignored) {
        } finally {
            tra(conn);
        }
        return l;
    }

    /** Cam chat (dung co dayCamChat cua lenh GM "camchat", luu theo pInfo). phut &lt;= 0 -> mo. */
    public static String camChat(final String ten, final int phut) {
        final Char p = tim(ten);
        if (p == null) {
            return "Cấm chat chỉ áp dụng cho Pokémon đang online: " + ten;
        }
        if (p.isAdmin) {
            return "Không cấm chat tài khoản admin.";
        }
        if (phut <= 0) {
            p.dayCamChat = "";
            p.timecamchat = 0L;
            KiemToanQuanTri.ghi("MO_CAM_CHAT", p.charname, "");
            try {
                p.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã được mở chat.", ""));
            } catch (final Exception ignored) {
            }
            return "Đã mở chat cho " + p.charname + ".";
        }
        p.dayCamChat = Char.getDayTime(phut * 60000L);
        try {
            Database.instance.saveOrtherLog("", p.charname, "bi panel cấm chat " + phut + " phút", "scamchat");
            p.sendMessage(MessageCreator.createServerAlertMessage("Bạn bị cấm chat " + DinhDang.thoiLuong(phut * 60000L) + ".", ""));
        } catch (final Exception ignored) {
        }
        KiemToanQuanTri.ghi("CAM_CHAT", p.charname, phut + " phut");
        return "Đã cấm chat " + p.charname + " trong " + DinhDang.thoiLuong(phut * 60000L) + ".";
    }

    public static boolean dangCamChat(final Char p) {
        try {
            return p.dayCamChat != null && !p.dayCamChat.isEmpty() && Char.getDayTime(0L).compareTo(p.dayCamChat) < 0;
        } catch (final Throwable t) {
            return false;
        }
    }

    // ================================================================== dich chuyen

    public static String dichChuyen(final String ten, final int mapId, final int x, final int y) {
        final Char p = tim(ten);
        if (p == null) {
            return "Không tìm thấy Pokémon online: " + ten;
        }
        if (p.map == null) {
            return "Pokémon chưa vào bản đồ.";
        }
        try {
            if (real.RealController.mapList.get(Integer.valueOf(mapId)) == null) {
                return "Không có bản đồ " + mapId + ".";
            }
        } catch (final Throwable ignored) {
        }
        try {
            p.map.move2Map(p, x, y, mapId, p.inCountry);
            KiemToanQuanTri.ghi("DICH_CHUYEN", p.charname, "map=" + mapId + " x=" + x + " y=" + y);
            return "Đã dịch chuyển " + p.charname + " tới " + tenMap(mapId) + " (" + x + ", " + y + ").";
        } catch (final Exception e) {
            return "Lỗi dịch chuyển: " + e.getMessage();
        }
    }

    /** Sua ket: dua ve diem an toan. */
    public static String veAnToan(final String ten) {
        return dichChuyen(ten, MAP_AN_TOAN, X_AN_TOAN, Y_AN_TOAN);
    }

    // ================================================================== thu / mat khau

    /** Gui thu (tob_inbox - hop thu doc luc mo) + bao ngay neu dang online. */
    public static String guiThu(final String ten, final String noiDung) {
        if (ten == null || ten.trim().isEmpty() || noiDung == null || noiDung.trim().isEmpty()) {
            return "Thiếu người nhận hoặc nội dung.";
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tob_char WHERE charname=?")) {
                ps.setString(1, ten.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        return "Không có Pokémon " + ten.trim() + ".";
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tob_inbox (charname, sender, info, dateSend) VALUES (?, ?, ?, NOW())")) {
                ps.setString(1, ten.trim());
                ps.setString(2, "Admin");
                ps.setString(3, catChuoi(noiDung.trim().replace("'", " "), 790));
                ps.executeUpdate();
            }
        } catch (final Exception e) {
            return "Lỗi gửi thư: " + e.getMessage();
        } finally {
            tra(conn);
        }
        final Char p = tim(ten);
        if (p != null) {
            try {
                com.htbb.thongbao.ThongBao.rieng(p, "Bạn có thư mới từ Admin - mở hộp thư để đọc.");
            } catch (final Exception ignored) {
            }
        }
        KiemToanQuanTri.ghi("GUI_THU", ten.trim(), noiDung);
        return "Đã gửi thư cho " + ten.trim() + (p != null ? " (đang online, đã báo)." : ".");
    }

    /** Ma hoa mat khau giong PASSWORD() cua MySQL (giu nguyen ham cu cua AdminPanel). */
    public static String matKhauSql(final String input) throws Exception {
        final java.security.MessageDigest sha1 = java.security.MessageDigest.getInstance("SHA-1");
        final byte[] first = sha1.digest(input.getBytes("UTF-8"));
        final byte[] second = sha1.digest(first);
        final StringBuilder sb = new StringBuilder("*");
        for (final byte b : second) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /** Doi mat khau tai khoan (account.team_user) - giu cach doi catalog cua ban cu (NovaDB). */
    public static String doiMatKhau(final String taiKhoan, final String matKhau) {
        if (taiKhoan == null || taiKhoan.trim().isEmpty() || matKhau == null || matKhau.length() < 4) {
            return "Tài khoản trống hoặc mật khẩu dưới 4 ký tự.";
        }
        try {
            final String hash = matKhauSql(matKhau);
            final Connection conn = Database.instance.getConnection();
            final String catalogCu = conn.getCatalog();
            int n;
            try {
                conn.setCatalog("account");
                try (PreparedStatement pre = conn.prepareStatement("UPDATE team_user SET password=? WHERE username=?")) {
                    pre.setString(1, hash);
                    pre.setString(2, taiKhoan.trim());
                    n = pre.executeUpdate();
                }
            } finally {
                try {
                    conn.setCatalog(catalogCu);
                } finally {
                    Database.connPool.free(conn);
                }
            }
            KiemToanQuanTri.ghi("DOI_MAT_KHAU", taiKhoan.trim(), n > 0 ? "ok" : "khong co tai khoan");
            return n > 0 ? "Đã đổi mật khẩu tài khoản " + taiKhoan.trim() + "." : "Không tìm thấy tài khoản " + taiKhoan.trim() + ".";
        } catch (final Exception e) {
            return "Lỗi đổi mật khẩu: " + e.getMessage();
        }
    }

    // ================================================================== phat qua (duong giftcode an toan)

    /** Kiem phan thuong theo DUNG luat giftcode an toan; null = hop le, nguoc lai ly do. */
    public static String kiemQua(final String phanThuong, final int capNguoiNhan) {
        final int tran = tranQua();
        final String loi = com.htbb.pokemon.GiftcodeAnToanLogic.kiem(phanThuong, tran);
        if (loi != null) {
            return loi;
        }
        final String chuan = com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong(phanThuong);
        if (chuan == null) {
            return "Phần thưởng không hợp lệ (chỉ LUONGKHOA:n, GEM:id:n, ITEM:id:n).";
        }
        try {
            return com.htbb.pokemon.GiftcodeAnToanLogic.kiemVatPham(chuan, capNguoiNhan, id -> {
                final real.ItemTemplates t = real.Map.itemTemplates.get(5).get(Integer.valueOf(id));
                return t == null ? -1 : t.level;
            });
        } catch (final Throwable t) {
            return "Không kiểm được danh sách vật phẩm: " + t;
        }
    }

    public static int tranQua() {
        try {
            return com.htbb.pokemon.LauDai.so("giftcode.gia_tri_toi_da", com.htbb.pokemon.GiftcodeAnToanLogic.GIA_TRI_TOI_DA);
        } catch (final Throwable t) {
            return com.htbb.pokemon.GiftcodeAnToanLogic.GIA_TRI_TOI_DA;
        }
    }

    /** Phat qua cho nhan vat ONLINE qua dung bo loc giftcode an toan (vat pham khoa / luong khoa, tran gia tri). */
    public static String tangQua(final String ten, final String phanThuong, final String lyDo) {
        final Char p = tim(ten);
        if (p == null) {
            return "Tặng quà chỉ cho Pokémon đang online: " + ten;
        }
        final String loi = kiemQua(phanThuong, p.lvDetail.lv);
        if (loi != null) {
            return "Từ chối: " + loi;
        }
        final String chuan = com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong(phanThuong);
        try {
            synchronized (p.KHOA_LUU) {
                data.DatabaseGiftCode.applyItemString(p, chuan);
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharGemItem(p));
            Database.instance.saveOrtherLog("", p.charname, "panel tang qua an toan: " + chuan + " (" + lyDo + ")", "putGiftCode");
            p.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được quà từ Admin" + (lyDo == null || lyDo.isEmpty()
                    ? "." : ": " + lyDo), ""));
        } catch (final Exception e) {
            return "Lỗi phát quà: " + e.getMessage();
        }
        KiemToanQuanTri.ghi("TANG_QUA", p.charname, chuan + " gia_tri=" + com.htbb.pokemon.GiftcodeAnToanLogic.giaTri(chuan)
                + " ly_do=" + lyDo);
        return "Đã tặng " + chuan + " cho " + p.charname + ".";
    }

    // ================================================================== giam level (giu nguyen ham cu)

    public static String giamLevel(final String name, final int amount) {
        boolean saved = false;
        String result = "";
        try {
            final Char player = CharManager.instance.getCharByCharName(name);
            if (player == null || player.getSession() == null || !player.getSession().isConnected()) {
                return "Bỏ qua " + name + ": Pokémon không online.";
            }
            synchronized (player.KHOA_LUU) {
                synchronized (player.lvDetail) {
                    if (CharManager.instance.getCharByCharName(name) != player
                            || player.getSession() == null || !player.getSession().isConnected()) {
                        return "Bỏ qua " + name + ": Pokémon đã ngắt kết nối.";
                    }
                    final int oldLevel = player.lvDetail.lv;
                    if (amount >= oldLevel) {
                        return "Bỏ qua " + name + ": cấp hiện tại " + oldLevel
                                + ", chỉ giảm được tối đa " + Math.max(0, oldLevel - 1) + " level.";
                    }
                    final long oldExp = player.lvDetail.getExp();
                    final int newLevel = oldLevel - amount;
                    player.lvDetail.setExpNew(real.LevelDetail.expMain[newLevel - 1]);
                    // Giu lastLV: diem co ban / ky nang da nhan khong duoc cong lai lan hai.
                    try {
                        saved = Database.instance.saveCharAuto(player);
                    } finally {
                        if (!saved) {
                            player.lvDetail.setExpNew(oldExp);
                        }
                    }
                    if (!saved) {
                        return "Thất bại " + name + ": không lưu được, đã khôi phục EXP.";
                    }
                    result = "Đã giảm " + amount + " level của " + player.charname
                            + ": " + oldLevel + " → " + newLevel + " (0% EXP).";
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                    player.sendMessage(MessageCreator.createServerAlertMessage(result, ""));
                }
            }
            KiemToanQuanTri.ghi("GIAM_LEVEL", name, result);
            return result;
        } catch (final Exception ex) {
            if (saved) {
                KiemToanQuanTri.ghi("GIAM_LEVEL", name, result + " (loi hien thi " + ex.getMessage() + ")");
                return result + " Đã lưu nhưng lỗi cập nhật hiển thị: " + ex.getMessage()
                        + ". Đăng nhập lại để cập nhật; không giảm lại.";
            }
            return "Lỗi giảm level của " + name + ": " + ex.getMessage() + ". Kiểm tra cấp Pokémon trước khi thử lại.";
        }
    }

    // ================================================================== van hanh

    /** Luu tat ca nhan vat dang choi bang saveCharAuto (KHONG dung saveChar - xem LenhWeb). */
    public static String luuTatCa() {
        int ok = 0, hong = 0;
        final long t0 = System.currentTimeMillis();
        for (final Char c : nguoiChoiOnline()) {
            try {
                synchronized (c.KHOA_LUU) {
                    if (Database.instance.saveCharAuto(c)) {
                        ok++;
                    } else {
                        hong++;
                    }
                }
            } catch (final Throwable e) {
                hong++;
            }
        }
        try {
            data.NewClan.updateAllClan();
        } catch (final Throwable ignored) {
        }
        final String kq = "Đã lưu " + ok + " Pokémon" + (hong > 0 ? ", " + hong + " lỗi" : "") + " + đội trong "
                + (System.currentTimeMillis() - t0) + " ms.";
        KiemToanQuanTri.ghi("LUU_TAT_CA", "", kq);
        return kq;
    }

    public static String donBoNho() {
        final Runtime r = Runtime.getRuntime();
        final long truoc = r.totalMemory() - r.freeMemory();
        System.gc();
        final long sau = r.totalMemory() - r.freeMemory();
        final String kq = "Đã dọn bộ nhớ: " + DinhDang.mb(truoc) + " → " + DinhDang.mb(sau) + ".";
        KiemToanQuanTri.ghi("DON_BO_NHO", "", kq);
        return kq;
    }

    public static String datDungDangNhap(final boolean dung) {
        LoginHandler.stopLogin = dung;
        KiemToanQuanTri.ghi(dung ? "DUNG_DANG_NHAP" : "MO_DANG_NHAP", "", "");
        return dung ? "Đã tắt đăng nhập mới." : "Đã bật lại đăng nhập mới.";
    }

    public static String napLaiCauHinh() {
        String kq;
        try {
            kq = server.CauHinh.napLai();
        } catch (final Throwable t) {
            kq = "Lỗi nạp lại: " + t;
        }
        KiemToanQuanTri.ghi("NAP_LAI_CAU_HINH", "", kq);
        return kq;
    }

    // ------------------------------------------------------------------ bao tri bang tep

    public static File tepBaoTri() {
        return new File(server.ChiSoMayChu.TEP_BAO_TRI);
    }

    /** Bat che do bao tri: tao bao_tri.txt (cau thong bao + dong "cho:" danh sach tai khoan van vao duoc). */
    public static String batBaoTri(final String cau, final List<String> choPhep) {
        final StringBuilder b = new StringBuilder();
        b.append(cau == null ? "" : cau.replace('\n', ' ').trim()).append('\n');
        if (choPhep != null && !choPhep.isEmpty()) {
            b.append("cho:");
            for (final String t : choPhep) {
                if (!t.trim().isEmpty()) {
                    b.append(' ').append(t.trim().toLowerCase(Locale.ROOT));
                }
            }
            b.append('\n');
        }
        try {
            Files.write(tepBaoTri().toPath(), b.toString().getBytes(StandardCharsets.UTF_8));
            KiemToanQuanTri.ghi("BAO_TRI_BAT", "", b.toString().replace('\n', '|'));
            return "Đã bật bảo trì - trong vài giây máy chủ từ chối đăng nhập mới.";
        } catch (final Exception e) {
            return "Không bật được bảo trì: " + e.getMessage();
        }
    }

    public static String tatBaoTri() {
        final File f = tepBaoTri();
        if (!f.exists()) {
            return "Máy chủ không ở chế độ bảo trì.";
        }
        if (!f.delete()) {
            return "Không tắt được bảo trì (không xoá được tệp).";
        }
        KiemToanQuanTri.ghi("BAO_TRI_TAT", "", "");
        return "Đã tắt bảo trì - mở lại đăng nhập trong 5 giây.";
    }

    /** {cau thong bao, danh sach cho phep} cua bao_tri.txt, null neu khong co tep. */
    public static String[] docBaoTri() {
        final File f = tepBaoTri();
        if (!f.isFile()) {
            return null;
        }
        try {
            final List<String> dong = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            String cau = "";
            final StringBuilder cho = new StringBuilder();
            for (final String d : dong) {
                final String t = d.trim();
                if (t.toLowerCase(Locale.ROOT).startsWith("cho:")) {
                    cho.append(cho.length() > 0 ? " " : "").append(t.substring(4).trim());
                } else if (cau.isEmpty()) {
                    cau = t;
                }
            }
            return new String[]{cau, cho.toString()};
        } catch (final Exception e) {
            return new String[]{"", ""};
        }
    }

    /** Gio bao tri tu dong hang ngay (BaoTriDaily) - doc qua phan chieu, khong sua lop do. */
    public static String gioBaoTriHangNgay() {
        try {
            final Class<?> c = real.BaoTriDaily.class;
            final java.lang.reflect.Field tat = c.getDeclaredField("tatBaoTri");
            tat.setAccessible(true);
            if (tat.getBoolean(null)) {
                return "Đang tắt";
            }
            final java.lang.reflect.Field h = c.getDeclaredField("maintenanceHour");
            final java.lang.reflect.Field m = c.getDeclaredField("maintenanceMinute");
            h.setAccessible(true);
            m.setAccessible(true);
            return String.format("%02d:%02d hằng ngày", h.getInt(null), m.getInt(null));
        } catch (final Throwable t) {
            return "Không rõ";
        }
    }

    // ------------------------------------------------------------------ sao luu

    /** Sao luu CSDL: vps/backup_db.sh neu chay duoc (VPS), khong thi mysqldump vao thu muc sao_luu/. */
    public static String saoLuu() {
        final File vps = new File("/opt/htbb/vps/backup_db.sh");
        final File vpsCuc = new File("vps/backup_db.sh");
        try {
            if (vps.isFile() && new File("/root/.my.cnf").canRead()) {
                return chayLenh(new String[]{"bash", vps.getAbsolutePath()}, null, "SAO_LUU");
            }
            final Properties p = docServerIni();
            String bin = timLenh("mysqldump");
            if (bin == null) {
                return "Máy này chưa có công cụ sao lưu CSDL (mysqldump)" + (vpsCuc.isFile() ? " (vps/backup_db.sh cần chạy bằng root trên VPS)" : "") + ".";
            }
            final String host = p.getProperty("db.host", "127.0.0.1:3306").trim();
            final String[] hp = host.split(":");
            final String ten = p.getProperty("db.name", "htbb_db").trim();
            final File thuMuc = new File("sao_luu");
            thuMuc.mkdirs();
            final File ra = new File(thuMuc, ten + "-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".sql");
            final java.util.Map<String, String> env = new java.util.HashMap<String, String>();
            env.put("MYSQL_PWD", p.getProperty("db.password", ""));
            String kq = "";
            // Lan 1: chuan MariaDB/MySQL. Lan 2 (CSDL kieu NovaDB khong co giao dich nhat quan): bo --single-transaction.
            for (final boolean giaoDich : new boolean[]{true, false}) {
                final List<String> lenh = new ArrayList<String>(java.util.Arrays.asList(bin, "--no-defaults", "-h", hp[0], "-P",
                        hp.length > 1 ? hp[1] : "3306", "-u", p.getProperty("db.user", "root").trim(), "--no-tablespaces",
                        "--skip-lock-tables", "--result-file=" + ra.getAbsolutePath(), ten));
                if (giaoDich) {
                    lenh.add(3, "--single-transaction");
                }
                kq = chayLenh(lenh.toArray(new String[0]), env, "SAO_LUU");
                if (kq.startsWith("Mã thoát 0") && ra.isFile() && ra.length() > 0) {
                    return "Đã sao lưu " + ten + " → " + ra.getPath() + " (" + DinhDang.so(ra.length() / 1024) + " KB).";
                }
            }
            ra.delete();
            return "Sao lưu thất bại. " + kq;
        } catch (final Throwable t) {
            return "Lỗi sao lưu: " + t;
        }
    }

    static String timLenh(final String ten) {
        final String path = System.getenv("PATH");
        final List<String> ds = new ArrayList<String>();
        if (path != null) {
            Collections.addAll(ds, path.split(File.pathSeparator));
        }
        ds.add("/usr/bin");
        ds.add("/usr/local/bin");
        ds.add("/opt/lampp/bin");
        ds.add("C:\\xampp\\mysql\\bin");
        for (final String d : ds) {
            for (final String duoi : new String[]{"", ".exe"}) {
                final File f = new File(d, ten + duoi);
                if (f.isFile() && f.canExecute()) {
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }

    static String chayLenh(final String[] lenh, final java.util.Map<String, String> env, final String nhan) throws Exception {
        final ProcessBuilder pb = new ProcessBuilder(lenh);
        pb.redirectErrorStream(true);
        if (env != null) {
            pb.environment().putAll(env);
        }
        final Process pr = pb.start();
        final byte[] ra = docHet(pr.getInputStream(), 8000);
        final boolean xong = pr.waitFor(10, java.util.concurrent.TimeUnit.MINUTES);
        final int ma = xong ? pr.exitValue() : -1;
        if (!xong) {
            pr.destroyForcibly();
        }
        final String out = new String(ra, StandardCharsets.UTF_8).trim();
        KiemToanQuanTri.ghi(nhan, lenh[0], "ma=" + ma + " " + catChuoi(out, 300));
        return "Mã thoát " + ma + (out.isEmpty() ? "" : ": " + catChuoi(out, 300));
    }

    static byte[] docHet(final InputStream in, final int toiDa) throws Exception {
        final java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        final byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) {
            if (b.size() < toiDa) {
                b.write(buf, 0, Math.min(n, toiDa - b.size()));
            }
        }
        return b.toByteArray();
    }

    public static Properties docServerIni() {
        final Properties p = new Properties();
        try (InputStream in = new FileInputStream("server.ini")) {
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (final Exception ignored) {
        }
        return p;
    }

    // ------------------------------------------------------------------ tat / khoi dong lai co dem nguoc

    /** Tat co trat tu: luu het (AdminHandler.stopServer giong nut bao tri cu) roi thoat JVM. */
    public static void tatMayChu(final boolean khoiDongLai) {
        KiemToanQuanTri.ghi(khoiDongLai ? "KHOI_DONG_LAI" : "TAT_MAY", "", "bat dau luu va thoat");
        try {
            Thread.sleep(300); // cho tep kiem toan ghi xong
        } catch (final InterruptedException ignored) {
        }
        if (khoiDongLai) {
            // Windows: mo lai run.bat nhu AdminHandler; Linux/systemd: Restart=always tu bat lai.
            try {
                final File run = new File("run.bat");
                if (run.isFile() && System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
                    java.awt.Desktop.getDesktop().open(run);
                }
            } catch (final Throwable ignored) {
            }
        }
        new AdminHandler().stopServer();
    }

    // ================================================================== truy van CSDL (doc)

    /** Chay SELECT tren htbb_db, tra ve {tenCot[], dong...}. Gioi han 500 dong. */
    public static List<Object[]> truyVan(final String sql, final Object... thamSo) {
        final List<Object[]> l = new ArrayList<Object[]>();
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            truyVan(conn, l, sql, thamSo);
        } catch (final Exception e) {
            l.clear();
            l.add(new Object[]{"__loi__", e.getMessage()});
        } finally {
            tra(conn);
        }
        return l;
    }

    static void truyVan(final Connection conn, final List<Object[]> l, final String sql, final Object... thamSo) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);
            }
            ps.setMaxRows(500);
            try (ResultSet rs = ps.executeQuery()) {
                final ResultSetMetaData md = rs.getMetaData();
                final Object[] ten = new Object[md.getColumnCount()];
                for (int i = 0; i < ten.length; i++) {
                    ten[i] = md.getColumnLabel(i + 1);
                }
                l.add(ten);
                while (rs.next()) {
                    final Object[] d = new Object[ten.length];
                    for (int i = 0; i < ten.length; i++) {
                        d[i] = rs.getObject(i + 1);
                    }
                    l.add(d);
                }
            }
        }
    }

    /** Ket qua truy van loi? (dong dau la "__loi__"). */
    public static String loiTruyVan(final List<Object[]> kq) {
        return !kq.isEmpty() && kq.get(0).length == 2 && "__loi__".equals(kq.get(0)[0]) ? String.valueOf(kq.get(0)[1]) : null;
    }

    /** Truy van tren CSDL account (doi catalog, tra lai truoc khi free - NovaDB). */
    public static List<Object[]> truyVanTaiKhoan(final String sql, final Object... thamSo) {
        final List<Object[]> l = new ArrayList<Object[]>();
        Connection conn = null;
        String cu = null;
        try {
            conn = Database.instance.getConnection();
            cu = conn.getCatalog();
            conn.setCatalog("account");
            truyVan(conn, l, sql, thamSo);
        } catch (final Exception e) {
            l.clear();
            l.add(new Object[]{"__loi__", e.getMessage()});
        } finally {
            if (conn != null) {
                try {
                    if (cu != null) {
                        conn.setCatalog(cu);
                    }
                } catch (final Exception ignored) {
                }
            }
            tra(conn);
        }
        return l;
    }

    /** Tim nhan vat offline theo ten nhan vat HOAC ten tai khoan (LIKE). */
    public static List<Object[]> timOffline(final String chuoi) {
        final String k = "%" + (chuoi == null ? "" : chuoi.trim()) + "%";
        final List<Object[]> tk = truyVanTaiKhoan("SELECT id, username FROM team_user WHERE username LIKE ? LIMIT 50", k);
        final StringBuilder ids = new StringBuilder();
        final java.util.Map<Object, Object> tenTk = new java.util.HashMap<Object, Object>();
        if (loiTruyVan(tk) == null) {
            for (int i = 1; i < tk.size(); i++) {
                ids.append(ids.length() > 0 ? "," : "").append(((Number) tk.get(i)[0]).intValue());
                tenTk.put(((Number) tk.get(i)[0]).intValue(), tk.get(i)[1]);
            }
        }
        final List<Object[]> kq = truyVan("SELECT id, charname, userId, lastLv, gold, luong, luonglock, ban, lastLog, dex FROM tob_char"
                + " WHERE charname LIKE ?" + (ids.length() > 0 ? " OR userId IN (" + ids + ")" : "") + " ORDER BY lastLog DESC LIMIT 100", k);
        if (loiTruyVan(kq) == null && kq.size() > 1) {
            // dien ten tai khoan cho dong thieu
            final StringBuilder thieu = new StringBuilder();
            for (int i = 1; i < kq.size(); i++) {
                final int u = ((Number) kq.get(i)[2]).intValue();
                if (!tenTk.containsKey(u)) {
                    thieu.append(thieu.length() > 0 ? "," : "").append(u);
                }
            }
            if (thieu.length() > 0) {
                final List<Object[]> them = truyVanTaiKhoan("SELECT id, username FROM team_user WHERE id IN (" + thieu + ")");
                if (loiTruyVan(them) == null) {
                    for (int i = 1; i < them.size(); i++) {
                        tenTk.put(((Number) them.get(i)[0]).intValue(), them.get(i)[1]);
                    }
                }
            }
            for (int i = 1; i < kq.size(); i++) {
                final Object u = tenTk.get(((Number) kq.get(i)[2]).intValue());
                kq.get(i)[2] = u == null ? kq.get(i)[2] : u;
            }
        }
        return kq;
    }

    /** Lich su dang nhap theo tai khoan (tob_may_dang_nhap: may, ip, lan dau/cuoi, so lan). */
    /** Cac nhan vat cua tai khoan + lan vao game cuoi (tob_char.lastLog). */
    public static List<Object[]> nhanVatCuaTaiKhoan(final String taiKhoan) {
        final List<Object[]> tk = truyVanTaiKhoan("SELECT id FROM team_user WHERE username=?", taiKhoan == null ? "" : taiKhoan.trim());
        if (loiTruyVan(tk) != null || tk.size() <= 1) {
            return tk;
        }
        return truyVan("SELECT charname, lastLv, lastLog, ban, dex FROM tob_char WHERE userId=? ORDER BY lastLog DESC", tk.get(1)[0]);
    }

    public static List<Object[]> lichSuDangNhap(final String taiKhoan) {
        return truyVan("SELECT ma_may, ip, lan_dau, lan_cuoi, so_lan FROM tob_may_dang_nhap WHERE username=? ORDER BY lan_cuoi DESC LIMIT 100",
                taiKhoan == null ? "" : taiKhoan.trim());
    }

    // ================================================================== su kien (tob_su_kien)

    public static List<Object[]> dsSuKien() {
        try {
            data.DatabaseSuKien.taoBang();
        } catch (final Throwable ignored) {
        }
        return truyVan("SELECT ma, ten, nhom, bat, dang_bat, sua_boi, sua_luc, mo_luc, dong_luc, mo_ta FROM tob_su_kien ORDER BY nhom, ma");
    }

    /** Doi cot `bat` (giong trang web sukien.php) - co hieu luc sau khi khoi dong lai (LichSuKien lo). */
    public static String datSuKien(final String ma, final boolean bat) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_su_kien SET bat=?, sua_boi=?, sua_luc=NOW() WHERE ma=?")) {
                ps.setInt(1, bat ? 1 : 0);
                ps.setString(2, "panel");
                ps.setString(3, ma);
                if (ps.executeUpdate() == 0) {
                    return "Không có sự kiện " + ma + ".";
                }
            }
            KiemToanQuanTri.ghi(bat ? "SU_KIEN_BAT" : "SU_KIEN_TAT", ma, "");
            return "Đã " + (bat ? "bật" : "tắt") + " sự kiện " + ma + " - có hiệu lực ở lần khởi động lại kế tiếp"
                    + (real.LichSuKien.tuKhoiDongLai ? " (máy chủ sẽ tự khởi động lại sau vài phút)." : ".");
        } catch (final Exception e) {
            return "Lỗi: " + e.getMessage();
        } finally {
            tra(conn);
        }
    }

    /** Hen gio mo/dong (mo_luc / dong_luc, dinh dang yyyy-MM-dd HH:mm; rong = xoa hen). */
    public static String henSuKien(final String ma, final String moLuc, final String dongLuc) {
        final String mo = chuanThoiGian(moLuc), dong = chuanThoiGian(dongLuc);
        if ((moLuc != null && !moLuc.trim().isEmpty() && mo == null) || (dongLuc != null && !dongLuc.trim().isEmpty() && dong == null)) {
            return "Sai định dạng thời gian (cần yyyy-MM-dd HH:mm).";
        }
        if (mo != null && dong != null && dong.compareTo(mo) <= 0) {
            return "Giờ đóng phải sau giờ mở.";
        }
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_su_kien SET mo_luc=?, dong_luc=? WHERE ma=?")) {
                ps.setString(1, mo);
                ps.setString(2, dong);
                ps.setString(3, ma);
                if (ps.executeUpdate() == 0) {
                    return "Không có sự kiện " + ma + ".";
                }
            }
            KiemToanQuanTri.ghi("SU_KIEN_HEN", ma, "mo=" + mo + " dong=" + dong);
            return "Đã hẹn " + ma + ": mở " + (mo == null ? "-" : mo) + ", đóng " + (dong == null ? "-" : dong) + ".";
        } catch (final Exception e) {
            return "Lỗi: " + e.getMessage();
        } finally {
            tra(conn);
        }
    }

    public static String chuanThoiGian(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        for (final String mau : new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "dd/MM/yyyy HH:mm"}) {
            try {
                final SimpleDateFormat f = new SimpleDateFormat(mau);
                f.setLenient(false);
                final Date d = f.parse(s.trim());
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    // ================================================================== boss the gioi

    public static List<Object[]> dsBoss() {
        final List<Object[]> l = new ArrayList<Object[]>();
        try {
            final Vector<real.Boss> b = real.Map.boss;
            for (int i = 0; b != null && i < b.size(); i++) {
                final real.Boss x = b.get(i);
                String ten = "?";
                try {
                    ten = x.getMonsterTemplate().name;
                } catch (final Throwable ignored) {
                }
                l.add(new Object[]{i + 1, ten, x.isOpen ? "Bật" : "Tắt", x.isDie() ? "Chờ hồi sinh" : "Đang sống",
                    x.isDie() && x.bornTime > System.currentTimeMillis() ? DinhDang.thoiLuong(x.bornTime - System.currentTimeMillis()) : "-"});
            }
        } catch (final Throwable ignored) {
        }
        return l;
    }

    /** Mo boss the gioi thu i (0..) - y het lenh GM "openbossN": bat + hoi sinh sau 30 giay. */
    public static String moBoss(final int i, final boolean mo) {
        try {
            final real.Boss b = real.Map.boss.get(i);
            b.isOpen = mo;
            if (mo) {
                b.bornTime = System.currentTimeMillis() + 30000L;
            }
            KiemToanQuanTri.ghi(mo ? "BOSS_MO" : "BOSS_TAT", String.valueOf(i + 1), "");
            return mo ? "Boss #" + (i + 1) + " sẽ xuất hiện sau 30 giây." : "Đã tắt boss #" + (i + 1) + ".";
        } catch (final Throwable t) {
            return "Không có boss #" + (i + 1) + ".";
        }
    }

    // ================================================================== giftcode an toan

    public static List<Object[]> dsGiftcode() {
        return truyVan("SELECT ma, phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat, ghi_chu FROM tob_giftcode ORDER BY tao_luc DESC");
    }

    public static String datGiftcode(final String ma, final boolean bat) {
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tob_giftcode SET bat=? WHERE ma=?")) {
                ps.setInt(1, bat ? 1 : 0);
                ps.setString(2, ma);
                ps.executeUpdate();
            }
            KiemToanQuanTri.ghi(bat ? "GIFTCODE_BAT" : "GIFTCODE_TAT", ma, "");
            return "Đã " + (bat ? "bật" : "tắt") + " mã " + ma + ".";
        } catch (final Exception e) {
            return "Lỗi: " + e.getMessage();
        } finally {
            tra(conn);
        }
    }

    /** Tao ma moi - phan thuong qua dung bo loc an toan (chi vat pham khoa / luong khoa, tran gia tri). */
    public static String taoGiftcode(final String maGoc, final String phanThuong, final int luot, final int cap, final int soNgay,
            final String ghiChu) {
        final String ma = com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaMa(maGoc);
        if (ma == null) {
            return "Mã không hợp lệ (3-32 ký tự a-z 0-9 _ -).";
        }
        final String loi = kiemQua(phanThuong, Math.max(1, cap));
        if (loi != null) {
            return "Từ chối: " + loi;
        }
        final String chuan = com.htbb.pokemon.GiftcodeAnToanLogic.chuanHoaThuong(phanThuong);
        Connection conn = null;
        try {
            conn = Database.instance.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tob_giftcode (ma, phan_thuong, han, tong_luot, da_dung,"
                    + " cap_toi_thieu, bat, ghi_chu, tao_boi, tao_luc) VALUES (?, ?, ?, ?, 0, ?, 1, ?, 'panel', NOW())")) {
                ps.setString(1, ma);
                ps.setString(2, chuan);
                ps.setString(3, soNgay > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                        new Date(System.currentTimeMillis() + soNgay * 86400000L)) : null);
                ps.setInt(4, Math.max(1, luot));
                ps.setInt(5, Math.max(1, cap));
                ps.setString(6, catChuoi(ghiChu, 250));
                ps.executeUpdate();
            }
            KiemToanQuanTri.ghi("GIFTCODE_TAO", ma, chuan + " luot=" + luot + " cap=" + cap + " ngay=" + soNgay);
            return "Đã tạo mã " + ma + " (" + chuan + ", " + Math.max(1, luot) + " lượt).";
        } catch (final Exception e) {
            return "Lỗi tạo mã (trùng mã?): " + e.getMessage();
        } finally {
            tra(conn);
        }
    }

    /** {da dung thang nay, ngan sach thang}. */
    public static long[] nganSachGiftcode() {
        final int thang = Integer.parseInt(new SimpleDateFormat("yyyyMM").format(new Date()));
        long dung = 0;
        final List<Object[]> r = truyVan("SELECT COALESCE(SUM(gia_tri),0) FROM tob_giftcode_log WHERE thang=?", thang);
        if (loiTruyVan(r) == null && r.size() > 1 && r.get(1)[0] != null) {
            dung = ((Number) r.get(1)[0]).longValue();
        }
        int ns;
        try {
            ns = com.htbb.pokemon.LauDai.so("giftcode.ngan_sach_thang", com.htbb.pokemon.GiftcodeAnToanLogic.NGAN_SACH_THANG);
        } catch (final Throwable t) {
            ns = com.htbb.pokemon.GiftcodeAnToanLogic.NGAN_SACH_THANG;
        }
        return new long[]{dung, ns};
    }

    // ================================================================== co tinh nang

    /** Ten co -> bat? (thu tu hien thi). Thanh vien doc qua phan chieu (lop co the chua co). */
    public static LinkedHashMap<String, Boolean> coTinhNang() {
        final LinkedHashMap<String, Boolean> m = new LinkedHashMap<String, Boolean>();
        m.put("lau_dai", an(() -> com.htbb.pokemon.LauDai.bat()));
        m.put("he_sinh_thai", an(() -> com.htbb.pokemon.HeSinhThai.bat()));
        m.put("nap_tien", an(() -> com.htbb.pokemon.NapTien.bat()));
        Boolean tv = null;
        try {
            final Class<?> c = Class.forName("com.htbb.pokemon.ThanhVien");
            final Object ch = c.getMethod("cauHinh").invoke(null);
            tv = ch.getClass().getField("bat").getBoolean(ch);
        } catch (final Throwable ignored) {
        }
        m.put("thanh_vien", tv);
        return m;
    }

    /** Nhan tieng Viet cho co tinh nang (khong hien ten khoa ky thuat). */
    public static String tenCo(final String ma) {
        switch (ma) {
            case "lau_dai":
                return "Thiết kế dài hạn";
            case "he_sinh_thai":
                return "Hệ sinh thái";
            case "nap_tien":
                return "Nạp tiền";
            case "thanh_vien":
                return "Thành viên";
            default:
                return ma;
        }
    }

    interface Doc {
        boolean doc() throws Throwable;
    }

    static Boolean an(final Doc d) {
        try {
            return d.doc();
        } catch (final Throwable t) {
            return null;
        }
    }

    /** Tai khoan co la thanh vien (phan chieu), null = khong ro / chua co tinh nang. */
    public static Boolean laThanhVien(final int userId) {
        try {
            final Class<?> c = Class.forName("com.htbb.pokemon.ThanhVien");
            return (Boolean) c.getMethod("laThanhVien", int.class).invoke(null, userId);
        } catch (final Throwable t) {
            return null;
        }
    }

    // ================================================================== tien ich

    static String catChuoi(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() <= n ? s : s.substring(0, n);
    }

    static void tra(final Connection conn) {
        if (conn != null) {
            try {
                Database.connPool.free(conn);
            } catch (final Exception ignored) {
            }
        }
    }
}
