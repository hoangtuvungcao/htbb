package com.htbb.cauhinh;

import com.htbb.dulieu.BangSql;
import com.htbb.dulieu.JsonNho;
import com.htbb.dulieu.NguonDuLieu;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Danh muc khoa cau hinh trung tam doc tu SQL: {@code cau_hinh_danh_muc} (moi khoa) + {@code cau_hinh_nhom}
 * (nhom hien thi). Hai bang SINH tu tools/cauhinh/danh_muc.json boi tools/cauhinh/sinh_cau_hinh.py
 * (du_lieu/sql_nguon/pokemon_cau_hinh_trung_tam.sql). Thay ban chep Java DanhMucSinh (da xoa, P1). Lop THUAN.
 *
 * {@code gioi_han} la JSON: min, max, buoc, soPhanTu, mau, doDai, luaChon (ma), luaChonTen (nhan, cho web), hoa.
 */
public final class DanhMucSql {

    public static final String BANG_KHOA = "cau_hinh_danh_muc";
    public static final String BANG_NHOM = "cau_hinh_nhom";
    static final String[] COT_KHOA = {"khoa", "nhom", "ten", "mo_ta", "kieu", "mac_dinh", "gioi_han", "ap_dung", "nhay",
        "khoa_prod", "chi_doc", "bang", "thu_tu"};
    static final String[] COT_NHOM = {"ma", "ten", "mo_ta", "thu_tu"};
    static final Set<String> KIEU = new HashSet<String>(java.util.Arrays.asList(
            "co", "so", "chon", "chuoi", "ds_chuoi", "ds_so", "ngay", "ngay_gio", "nhip"));
    static final Set<String> BANG = new HashSet<String>(java.util.Arrays.asList("cau_hinh", "nap_cau_hinh", "env"));

    private DanhMucSql() {
    }

    /** Ket qua nap: khoa theo thu_tu + nhom {ma, ten} theo thu_tu. */
    public static final class DanhMuc {
        public final DinhNghiaKhoa[] khoa;
        public final String[][] nhom;

        DanhMuc(final DinhNghiaKhoa[] khoa, final String[][] nhom) {
            this.khoa = khoa;
            this.nhom = nhom;
        }
    }

    /** Doc + kiem hai bang tu {@code n}. Bang thieu / rong / sai = loi (danh muc la du lieu quan trong). */
    public static DanhMuc doc(final NguonDuLieu n) throws Exception {
        final String[][] nhom = docNhom(n.doc(BANG_NHOM, COT_NHOM));
        return new DanhMuc(docKhoa(n.doc(BANG_KHOA, COT_KHOA), nhom), nhom);
    }

    static String[][] docNhom(final BangSql b) {
        if (b.soDong() == 0) {
            throw new IllegalStateException(b.ten + " rong");
        }
        final TreeMap<Integer, String[]> m = new TreeMap<Integer, String[]>();
        final Set<String> ma = new HashSet<String>();
        for (int r = 0; r < b.soDong(); r++) {
            final String k = b.chu(r, "ma");
            if (k.isEmpty() || !ma.add(k)) {
                throw new IllegalStateException(b.ten + ": ma nhom rong hoac trung '" + k + "'");
            }
            if (m.put(b.so(r, "thu_tu"), new String[]{k, b.chu(r, "ten")}) != null) {
                throw new IllegalStateException(b.ten + ": trung thu_tu " + b.so(r, "thu_tu"));
            }
        }
        return m.values().toArray(new String[0][]);
    }

    static DinhNghiaKhoa[] docKhoa(final BangSql b, final String[][] nhom) {
        if (b.soDong() == 0) {
            throw new IllegalStateException(b.ten + " rong");
        }
        final Set<String> maNhom = new HashSet<String>();
        for (final String[] x : nhom) {
            maNhom.add(x[0]);
        }
        final TreeMap<Integer, DinhNghiaKhoa> m = new TreeMap<Integer, DinhNghiaKhoa>();
        final Set<String> khoa = new HashSet<String>();
        for (int r = 0; r < b.soDong(); r++) {
            final String k = b.chu(r, "khoa");
            final String dong = b.ten + " khoa '" + k + "': ";
            if (!k.matches("[A-Za-z0-9_.]{1,64}") || !khoa.add(k)) {
                throw new IllegalStateException(dong + "ten sai hoac trung");
            }
            final String kieu = b.chu(r, "kieu");
            final String ap = b.chu(r, "ap_dung");
            final String bang = b.chu(r, "bang");
            if (!KIEU.contains(kieu) || !BANG.contains(bang) || !("ngay".equals(ap) || "khoi_dong_lai".equals(ap))) {
                throw new IllegalStateException(dong + "kieu/bang/ap_dung sai (" + kieu + ", " + bang + ", " + ap + ")");
            }
            if (!maNhom.contains(b.chu(r, "nhom"))) {
                throw new IllegalStateException(dong + "nhom '" + b.chu(r, "nhom") + "' khong co trong " + BANG_NHOM);
            }
            final DinhNghiaKhoa d = new DinhNghiaKhoa(k, b.chu(r, "nhom"), kieu, b.chu(r, "mac_dinh"), "ngay".equals(ap), bang)
                    .nhan(b.chu(r, "ten"), b.chu(r, "mo_ta"));
            final Map<String, Object> g;
            try {
                g = JsonNho.docObject(b.chu(r, "gioi_han"));
            } catch (final IllegalArgumentException e) {
                throw new IllegalStateException(dong + "gioi_han khong phai JSON: " + e.getMessage());
            }
            if (g.containsKey("min") || g.containsKey("max")) {
                d.khoang(g.containsKey("min") ? soLong(g.get("min"), dong) : Long.MIN_VALUE,
                        g.containsKey("max") ? soLong(g.get("max"), dong) : Long.MAX_VALUE);
            }
            if (g.containsKey("buoc") && soLong(g.get("buoc"), dong) != 0L) {
                d.buoc(soLong(g.get("buoc"), dong));
            }
            if (g.get("luaChon") instanceof List && !((List<?>) g.get("luaChon")).isEmpty()) {
                final List<String> ds = new ArrayList<String>();
                for (final Object o : (List<?>) g.get("luaChon")) {
                    ds.add(String.valueOf(o));
                }
                d.luaChon(ds.toArray(new String[0]));
            }
            if (g.containsKey("soPhanTu") && soLong(g.get("soPhanTu"), dong) != 0L) {
                d.soPhanTu((int) soLong(g.get("soPhanTu"), dong));
            }
            if (g.get("mau") instanceof String && !((String) g.get("mau")).isEmpty()) {
                d.mau((String) g.get("mau"));
            }
            if (g.containsKey("doDai") && soLong(g.get("doDai"), dong) != 0L) {
                d.doDai((int) soLong(g.get("doDai"), dong));
            }
            if (Boolean.TRUE.equals(g.get("hoa"))) {
                d.hoa();
            }
            if (b.so(r, "nhay") != 0) {
                d.nhay();
            }
            if (b.so(r, "khoa_prod") != 0) {
                d.khoaProd();
            }
            if (b.so(r, "chi_doc") != 0) {
                d.chiDoc();
            }
            if (m.put(b.so(r, "thu_tu"), d) != null) {
                throw new IllegalStateException(dong + "trung thu_tu " + b.so(r, "thu_tu"));
            }
        }
        return m.values().toArray(new DinhNghiaKhoa[0]);
    }

    private static long soLong(final Object o, final String dong) {
        if (o instanceof Long) {
            return ((Long) o).longValue();
        }
        if (o instanceof Double && ((Double) o).doubleValue() == Math.rint((Double) o)) {
            return ((Double) o).longValue();
        }
        throw new IllegalStateException(dong + "gioi_han co so khong nguyen: " + o);
    }
}
