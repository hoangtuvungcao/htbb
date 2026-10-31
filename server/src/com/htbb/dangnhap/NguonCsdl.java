package com.htbb.dangnhap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Properties;

/**
 * {@link XacThuc.Nguon} tren CSDL tai khoan (MariaDB, CSDL trong db.url - thuong la `account`). Cung cau truy van
 * voi ban cu nhung dung PreparedStatement (ban cu noi chuoi; ten/mat khau co dau ' da bi chan truoc, nay khong con
 * phu thuoc vao do). So mat khau van bang PASSWORD(?) cua MariaDB: cung ham, cung bang ma ket noi -> moi tai khoan cu
 * dang nhap y nhu truoc.
 */
public final class NguonCsdl implements XacThuc.Nguon {

    static final String SQL_TAI_KHOAN =
            "SELECT id, ban, provider, phone, fromgame FROM team_user WHERE username = ? AND `password` = PASSWORD(?)";
    static final String SQL_TOKEN = "SELECT id, dateCreate FROM usertoken WHERE username = ? AND `token` = ?";

    private final BeKetNoi be;
    private final int truyVanGiay;
    private final int socketMs;

    public NguonCsdl(final BeKetNoi be, final int truyVanGiay, final int socketMs) {
        this.be = be;
        this.truyVanGiay = truyVanGiay;
        this.socketMs = socketMs;
    }

    /** Nha may ket noi JDBC: them connectTimeout / socketTimeout neu db.url chua tu dat. Khong ghi mat khau ra log. */
    public static BeKetNoi.NhaMay nhaMay(final String url, final String user, final String matKhau, final int ketNoiMs,
            final int socketMs) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("thieu MySQL Connector/J (com.mysql.jdbc.Driver) trong jar", e);
        }
        final Properties p = new Properties();
        if (user != null) {
            p.setProperty("user", user);
        }
        if (matKhau != null) {
            p.setProperty("password", matKhau);
        }
        final String u = url == null ? "" : url;
        if (!u.contains("connectTimeout=") && ketNoiMs > 0) {
            p.setProperty("connectTimeout", String.valueOf(ketNoiMs));
        }
        if (!u.contains("socketTimeout=") && socketMs > 0) {
            p.setProperty("socketTimeout", String.valueOf(socketMs));
        }
        if (!u.contains("useSSL=")) {
            p.setProperty("useSSL", "false"); // Connector/J 5.1 canh bao moi lan mo neu khong dat (CSDL chi 127.0.0.1)
        }
        return new BeKetNoi.NhaMay() {
            @Override
            public Connection mo() throws SQLException {
                return DriverManager.getConnection(u, p);
            }
        };
    }

    /** Mot lan truy van tren mot ket noi da muon. */
    private interface Truy<T> {
        T chay(java.sql.Connection c) throws SQLException;
    }

    /**
     * Muon ket noi, chay, tra. Loi KET NOI (SQLState 08xxx - ket noi bi MariaDB cat, mang rot) xay ra NHANH (duoi
     * {@link #THU_LAI_NEU_DUOI_MS}) thi thu lai MOT lan tren ket noi moi: nguoi choi khong phai nhan "Có lỗi xảy ra"
     * chi vi ket noi cu da chet trong luc ranh. Loi cau truy van / het gio thi khong thu lai.
     */
    private <T> T voiKetNoi(final long hanChot, final Truy<T> t) throws SQLException {
        for (int lan = 0; ; lan++) {
            final long con = hanChot == Long.MAX_VALUE ? Long.MAX_VALUE : hanChot - System.currentTimeMillis();
            if (con < 200L) {
                throw new SQLTimeoutException("het han xu ly truoc khi truy van");
            }
            final BeKetNoi.Muon m = this.be.muon(con);
            boolean hong = true;
            try {
                // Han cho TUNG lan doc socket = phan con lai (toi da socketMs): CSDL treo (mang nua voi, khoa bang lau)
                // thi ket noi nay nem loi dung han, khong giu yeu cau qua han cho cua game.
                final long con2 = hanChot == Long.MAX_VALUE ? this.socketMs : hanChot - System.currentTimeMillis();
                final int mang = (int) Math.max(200L, Math.min(this.socketMs, con2));
                try {
                    m.ketNoi.setNetworkTimeout(CHAY_NGAY, mang);
                } catch (final SQLException | AbstractMethodError | UnsupportedOperationException ignored) {
                    // driver khong ho tro: con socketTimeout trong db.url / nha may
                }
                this.truyVanGiayLanNay.set(Math.max(1, Math.min(this.truyVanGiay, (mang + 999) / 1000)));
                final T r = t.chay(m.ketNoi);
                hong = false;
                return r;
            } catch (final SQLException e) {
                if (e.getErrorCode() == 1146) {
                    hong = false; // bang khong ton tai: loi cau truy van, ket noi van tot
                }
                if (lan == 0 && laLoiKetNoi(e)
                        && (hanChot == Long.MAX_VALUE || hanChot - System.currentTimeMillis() > THU_LAI_KHI_CON_MS)) {
                    this.soThuLai.incrementAndGet();
                    continue;
                }
                throw e;
            } finally {
                this.be.tra(m, hong);
            }
        }
    }

    /** Chi thu lai khi con it nhat chung nay ms truoc han. */
    static final long THU_LAI_KHI_CON_MS = 1000L;
    private static final java.util.concurrent.Executor CHAY_NGAY = new java.util.concurrent.Executor() {
        @Override
        public void execute(final Runnable r) {
            r.run();
        }
    };
    private final ThreadLocal<Integer> truyVanGiayLanNay = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 5;
        }
    };
    public final java.util.concurrent.atomic.AtomicLong soThuLai = new java.util.concurrent.atomic.AtomicLong();

    static boolean laLoiKetNoi(final SQLException e) {
        final String st = e.getSQLState();
        return (st != null && st.startsWith("08")) || e.getClass().getName().contains("CommunicationsException");
    }

    @Override
    public XacThuc.TaiKhoan timTaiKhoan(final String ten, final String matKhau, final long hanChot) throws SQLException {
        return voiKetNoi(hanChot, new Truy<XacThuc.TaiKhoan>() {
            @Override
            public XacThuc.TaiKhoan chay(final Connection c) throws SQLException {
                try (PreparedStatement ps = c.prepareStatement(SQL_TAI_KHOAN)) {
                    ps.setQueryTimeout(NguonCsdl.this.truyVanGiayLanNay.get());
                    ps.setString(1, ten);
                    ps.setString(2, matKhau);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return null;
                        }
                        return new XacThuc.TaiKhoan(rs.getInt("id"), rs.getInt("ban"), rs.getInt("provider"),
                                rs.getString("phone"), rs.getByte("fromgame"));
                    }
                }
            }
        });
    }

    @Override
    public XacThuc.Token timToken(final String ten, final String token, final long hanChot) throws SQLException {
        return voiKetNoi(hanChot, new Truy<XacThuc.Token>() {
            @Override
            public XacThuc.Token chay(final Connection c) throws SQLException {
                try (PreparedStatement ps = c.prepareStatement(SQL_TOKEN)) {
                    ps.setQueryTimeout(NguonCsdl.this.truyVanGiayLanNay.get());
                    ps.setString(1, ten);
                    ps.setString(2, token);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? new XacThuc.Token(rs.getInt("id"), rs.getLong("dateCreate")) : null;
                    }
                }
            }
        });
    }
}
