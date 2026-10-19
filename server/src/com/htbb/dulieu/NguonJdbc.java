package com.htbb.dulieu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Doc bang du lieu tu CSDL qua JDBC. Ket noi do noi goi cap (pool chinh {@code data.Database} qua
 * {@code data.DatabaseDuLieuSql}, hoac ket noi truc tiep co han gio cua CauHinhTrungTam luc khoi dong).
 *
 * Ten bang / cot ghep vao SQL chi lay tu danh sach trang trong ma ({@link DuLieuSql}); van kiem them bang
 * regex de mot loi lap trinh khong bao gio thanh SQL ghep chuoi tu do (docs/kien-truc.md muc 11.3, quy tac 10).
 */
public final class NguonJdbc implements NguonDuLieu {

    /** Cap / tra ket noi (pool hoac truc tiep). */
    public interface KetNoi {
        Connection lay() throws Exception;

        void tra(Connection c);
    }

    private final KetNoi ketNoi;
    private final String moTa;

    public NguonJdbc(final KetNoi ketNoi, final String moTa) {
        this.ketNoi = ketNoi;
        this.moTa = moTa;
    }

    /** Dung MOT ket noi co san (noi goi tu dong). */
    public static NguonJdbc mot(final Connection c, final String moTa) {
        return new NguonJdbc(new KetNoi() {
            @Override
            public Connection lay() {
                return c;
            }

            @Override
            public void tra(final Connection x) {
            }
        }, moTa);
    }

    @Override
    public String moTa() {
        return moTa;
    }

    static String ten(final String s) {
        if (s == null || !s.matches("[A-Za-z0-9_]{1,64}")) {
            throw new IllegalArgumentException("ten bang/cot khong hop le: " + s);
        }
        return s;
    }

    @Override
    public BangSql doc(final String bang, final String[] cot) throws Exception {
        final StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < cot.length; i++) {
            sql.append(i == 0 ? "" : ", ").append('`').append(ten(cot[i])).append('`');
        }
        sql.append(" FROM `").append(ten(bang)).append('`');
        final Connection c = ketNoi.lay();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            final List<String[]> dong = new ArrayList<String[]>();
            while (rs.next()) {
                final String[] d = new String[cot.length];
                for (int i = 0; i < cot.length; i++) {
                    d[i] = rs.getString(i + 1);
                }
                dong.add(d);
            }
            return new BangSql(bang, cot, dong);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (final Exception ignored) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (final Exception ignored) {
                }
            }
            ketNoi.tra(c);
        }
    }
}
