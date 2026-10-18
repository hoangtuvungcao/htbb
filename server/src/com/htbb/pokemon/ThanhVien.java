package com.htbb.pokemon;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.MessageCreator;

/**
 * Cổng "Thành viên HTBB" trong game (docs/van-hanh.md). MỘT hàm {@link #chan} dùng ở mọi chỗ
 * khoá tính năng. Cấu hình đọc lại từ bảng cau_hinh mỗi 10 giây (CauHinh.napLai -> {@link #apDungCauHinh});
 * trạng thái thành viên đọc bảng tv_thanh_vien, nhớ 30 giây / tài khoản. Lỗi CSDL = cho qua (không khoá
 * nhầm người chơi vì CSDL chập chờn). Tắt (thanh_vien.bat=0, mặc định) = không khoá gì, không đọc CSDL.
 */
public final class ThanhVien {

    private static volatile ThanhVienLogic.CauHinh cauHinh = ThanhVienLogic.TAT;
    /** userID -> {het_han_ms, co_dong (0/1), luc_doc}. */
    private static final ConcurrentHashMap<Integer, long[]> NHO = new ConcurrentHashMap<Integer, long[]>();
    static final long NHO_MS = 30000L;

    private ThanhVien() {
    }

    public static void apDungCauHinh(final Properties p) {
        cauHinh = ThanhVienLogic.docCauHinh(p);
    }

    public static ThanhVienLogic.CauHinh cauHinh() {
        return cauHinh;
    }

    /** Chỉ test: đặt cấu hình + trạng thái giả của một tài khoản. */
    public static void datThu(final ThanhVienLogic.CauHinh c) {
        cauHinh = c == null ? ThanhVienLogic.TAT : c;
        NHO.clear();
        NHO_KH.clear();
    }

    public static void datNhoThu(final int userId, final long hetHan, final boolean coDong) {
        NHO.put(userId, new long[]{hetHan, coDong ? 1L : 0L, Long.MAX_VALUE / 2});
    }

    /** Tài khoản có đang là thành viên không (nhớ 30 giây). */
    public static boolean laThanhVien(final int userId) {
        final long t = System.currentTimeMillis();
        long[] n = NHO.get(userId);
        if (n == null || t - n[2] > NHO_MS) {
            n = docCsdl(userId, t);
            if (n == null) {
                return true; // CSDL lỗi: không khoá nhầm
            }
            NHO.put(userId, n);
        }
        return ThanhVienLogic.conHan(n[0], t, n[1] == 1L);
    }

    static long[] docCsdl(final int userId, final long t) {
        try {
            // Dung chung nguon ket noi cua NapTienDb (pool game; test / harness thay duoc).
            final java.util.List<Object[]> r = NapTienDb.doc("SELECT het_han_ms FROM tv_thanh_vien WHERE user_id = ?", 1, userId);
            return r.isEmpty() ? new long[]{-1L, 0L, t} : new long[]{NapTienDb.so(r.get(0)[0]), 1L, t};
        } catch (final Exception e) {
            return null;
        }
    }

    static int cap(final Char p) {
        try {
            return p.lvDetail == null ? 0 : p.lvDetail.lv;
        } catch (final Exception e) {
            return 0;
        }
    }

    /**
     * Lời nhắn nếu {@code p} KHÔNG được dùng tính năng {@code ma}, null nếu được. Hàm DUY NHẤT mọi cổng
     * gọi. Tắt hệ thống thì trả null ngay (không đọc CSDL).
     */
    public static String chan(final Char p, final String ma) {
        final ThanhVienLogic.CauHinh c = cauHinh;
        final boolean canKh = c.khChucNang.contains(ma);
        final boolean canTv = c.bat && c.chucNang.contains(ma);
        if (p == null || (!canKh && !canTv)) {
            return null;
        }
        if (p.isBot != -1) {
            return null; // bot / lính thuê do server điều khiển
        }
        // Chỉ đọc CSDL khi thật sự cần (kích hoạt / thành viên), mỗi thứ nhớ 30 giây.
        final boolean kh = !canKh || daKichHoat(p.userID);
        final boolean tv = (canTv || (canKh && !kh && c.khTvLaKh)) && laThanhVien(p.userID);
        switch (ThanhVienLogic.quyetDinh(c, ma, kh, tv, cap(p))) {
            case ThanhVienLogic.CAN_KICH_HOAT:
                return ThanhVienLogic.thongBaoKichHoat(c, ma, server.TeamServer.website);
            case ThanhVienLogic.CAN_THANH_VIEN:
                return ThanhVienLogic.thongBao(c, ma, server.TeamServer.website);
            default:
                return null;
        }
    }

    // ------------------------------------------------------------------ kích hoạt (5h_active)

    /** userID -> {0/1 đã kích hoạt, lúc đọc}. */
    private static final ConcurrentHashMap<Integer, long[]> NHO_KH = new ConcurrentHashMap<Integer, long[]>();

    /**
     * Đăng nhập: chặn tài khoản chưa kích hoạt KHÔNG? Mặc định KHÔNG (kich_hoat.chan_dang_nhap=0) — chưa kích
     * hoạt vẫn vào chơi, chỉ bị khoá tính năng trong kich_hoat.chuc_nang. LoginHandler / SelectCharHandler gọi.
     */
    public static boolean chanDangNhap() {
        return cauHinh.khChanDangNhap;
    }

    /** Như trên, nhưng Thành viên được coi như đã kích hoạt (kich_hoat.thanh_vien_la_kich_hoat=1) thì không chặn. */
    public static boolean chanDangNhap(final int userId) {
        final ThanhVienLogic.CauHinh c = cauHinh;
        return c.khChanDangNhap && !(c.khTvLaKh && userId > 0 && laThanhVien(userId));
    }

    /** Tài khoản đã kích hoạt chưa (sv.EnableAvtiveCheck=0 = coi như đã kích hoạt; lỗi CSDL = coi như rồi). */
    public static boolean daKichHoat(final int userId) {
        if (!data.Database.ENABLE_ACTIVE_CHECK) {
            return true;
        }
        final long t = System.currentTimeMillis();
        final long[] n = NHO_KH.get(userId);
        if (n != null && t - n[1] < NHO_MS) {
            return n[0] == 1L;
        }
        boolean kq;
        try {
            kq = data.Database.check_active(userId);
        } catch (final Throwable e) {
            kq = true;
        }
        NHO_KH.put(userId, new long[]{kq ? 1L : 0L, t});
        return kq;
    }

    /** Vào game: tài khoản chưa kích hoạt (và không bị chặn đăng nhập) nhận một dòng nhắc tính năng bị khoá. */
    public static void baoKhiVaoGame(final Char p) {
        final ThanhVienLogic.CauHinh c = cauHinh;
        if (p == null || p.isBot != -1 || c.khChucNang.isEmpty() || daKichHoat(p.userID)) {
            return;
        }
        if (c.khTvLaKh && laThanhVien(p.userID)) {
            return;
        }
        final StringBuilder ds = new StringBuilder();
        for (final String m : c.khChucNang) {
            ds.append(ds.length() == 0 ? "" : ", ").append(ThanhVienLogic.tenChucNang(m).toLowerCase(java.util.Locale.ROOT));
        }
        final String web = server.TeamServer.website == null || server.TeamServer.website.trim().isEmpty() ? "trang web"
                : server.TeamServer.website.trim().replaceAll("/+$", "");
        bao(p, "Tài khoản chưa kích hoạt: vẫn chơi bình thường, chỉ tạm khoá " + ds + ". Kích hoạt tại " + web + "/kichhoat.");
    }

    /** Chỉ test: đặt trạng thái kích hoạt giả. */
    public static void datKichHoatThu(final int userId, final boolean daKh) {
        NHO_KH.put(userId, new long[]{daKh ? 1L : 0L, Long.MAX_VALUE / 2});
    }

    /** Chặn + báo bằng hộp thoại có sẵn. true = đã chặn (người gọi return). */
    public static boolean chanVaBao(final Char p, final String ma) {
        final String m = chan(p, ma);
        if (m == null) {
            return false;
        }
        bao(p, m);
        return true;
    }

    /** Hai người (giao dịch / tặng): người làm cần {@code ma}, người kia cần {@code maNguoiKia}. */
    public static boolean chanHaiNguoi(final Char p, final Char nguoiKia, final String ma, final String maNguoiKia) {
        if (chanVaBao(p, ma)) {
            return true;
        }
        final String m = nguoiKia == null ? null : chan(nguoiKia, maNguoiKia);
        if (m != null) {
            bao(p, m.contains("/kichhoat") ? ThanhVienLogic.thongBaoNguoiKiaKichHoat(maNguoiKia) : ThanhVienLogic.thongBaoNguoiKia(maNguoiKia));
            return true;
        }
        return false;
    }

    static void bao(final Char p, final String m) {
        try {
            p.sendMessage(MessageCreator.createServerAlertMessage(m, ""));
        } catch (final Exception ignored) {
        }
    }

    /** Dòng trạng thái cho menu (null khi hệ thống tắt). */
    public static String dongTrangThai(final Char p) {
        if (p == null || !cauHinh.bat) {
            return null;
        }
        return laThanhVien(p.userID) ? "Thành viên HTBB: đang hiệu lực" : "Thành viên HTBB: chưa có (mua trên website)";
    }
}
