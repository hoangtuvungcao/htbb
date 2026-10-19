package com.htbb.dulieu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mot bang du lieu doc tu SQL (CSDL that qua JDBC hoac tep dump .sql trong repo). Lop THUAN.
 *
 * Moi o giu dang CHUOI (null = NULL), giong {@code ResultSet.getString}: hai nguon (MariaDB / NovaDB /
 * tep dump) tra so theo kieu khac nhau (Integer, Long, BigDecimal "2.00"...), nen doi kieu o mot cho
 * duy nhat la cac ham {@link #so}, {@link #soLong}, {@link #thuc}. O sai kieu nem
 * {@link IllegalArgumentException} kem ten bang / cot / dong de log noi ro dong nao hong.
 */
public final class BangSql {

    public final String ten;
    private final String[] cot;
    private final List<String[]> dong;

    public BangSql(final String ten, final String[] cot, final List<String[]> dong) {
        this.ten = ten;
        this.cot = cot.clone();
        this.dong = Collections.unmodifiableList(new ArrayList<String[]>(dong));
    }

    public int soDong() {
        return dong.size();
    }

    public String[] cot() {
        return cot.clone();
    }

    /** Chi so cot theo ten (khong phan biet hoa thuong); -1 neu khong co. */
    public int viTri(final String c) {
        for (int i = 0; i < cot.length; i++) {
            if (cot[i].equalsIgnoreCase(c)) {
                return i;
            }
        }
        return -1;
    }

    public boolean coCot(final String c) {
        return viTri(c) >= 0;
    }

    private int viTriBatBuoc(final String c) {
        final int i = viTri(c);
        if (i < 0) {
            throw new IllegalArgumentException(ten + ": thieu cot " + c);
        }
        return i;
    }

    /** Gia tri tho (null = NULL). */
    public String chuoi(final int r, final String c) {
        return dong.get(r)[viTriBatBuoc(c)];
    }

    /** Chuoi, NULL -> "". */
    public String chu(final int r, final String c) {
        final String s = chuoi(r, c);
        return s == null ? "" : s;
    }

    public long soLong(final int r, final String c) {
        final String s = chuoi(r, c);
        if (s == null) {
            throw new IllegalArgumentException(ten + "." + c + " dong " + (r + 1) + ": NULL");
        }
        final String t = s.trim();
        try {
            return Long.parseLong(t);
        } catch (final NumberFormatException e) {
            // DECIMAL nguyen ("15.00") van nhan; so le thi khong (khong tu lam tron).
            try {
                final java.math.BigDecimal b = new java.math.BigDecimal(t);
                return b.longValueExact();
            } catch (final RuntimeException e2) {
                throw new IllegalArgumentException(ten + "." + c + " dong " + (r + 1) + ": khong phai so nguyen '" + s + "'");
            }
        }
    }

    public int so(final int r, final String c) {
        final long v = soLong(r, c);
        if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(ten + "." + c + " dong " + (r + 1) + ": vuot int " + v);
        }
        return (int) v;
    }

    public double thuc(final int r, final String c) {
        final String s = chuoi(r, c);
        if (s == null) {
            throw new IllegalArgumentException(ten + "." + c + " dong " + (r + 1) + ": NULL");
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(ten + "." + c + " dong " + (r + 1) + ": khong phai so '" + s + "'");
        }
    }

    /** So nguyen hoac {@code macDinh} neu o trong / NULL / khong phai so (cot van ban co the chua so). */
    public long soHoac(final int r, final String c, final long macDinh) {
        final String s = chuoi(r, c);
        if (s == null || s.trim().isEmpty()) {
            return macDinh;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }
}
