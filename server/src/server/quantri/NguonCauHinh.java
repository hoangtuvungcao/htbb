package server.quantri;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * BO CHUYEN (adapter) mong cho trang "Cau hinh" cua bang dieu khien.
 *
 * Uu tien dich vu cau hinh trung tam {@code com.htbb.cauhinh.CauHinhTrungTam} (danh muc co kieu / khoang /
 * lua chon, nap lai nong, khoa "can khoi dong lai", lich su). Goi qua PHAN CHIEU de bang dieu khien khong
 * phu thuoc luc bien dich vao mot lop dang duoc xay dung song song - doi API thi chi sua lop nay.
 *
 * Khong co / loi -> du phong: cac khoa nap lai duoc cua server.CauHinh (bang cau_hinh), ghi bang
 * CauHinh.luuNhieu(..., "panel"), kiem toi thieu (so nguyen giu so nguyen, co 0/1).
 */
public final class NguonCauHinh {

    /** Mot khoa cau hinh de hien / sua. */
    public static final class Muc {
        public String khoa, nhom = "", kieu = "chuoi", ten = "", moTa = "", macDinh = "", giaTri = "", nguon = "", cho;
        public boolean apDungNgay = true, chiDoc, nhay;
        public String[] luaChon = new String[0];
        public long min = Long.MIN_VALUE, max = Long.MAX_VALUE;
        public String suaBoi = "", suaLuc = "";
    }

    private NguonCauHinh() {
    }

    static Class<?> lopTrungTam() {
        try {
            return Class.forName("com.htbb.cauhinh.CauHinhTrungTam");
        } catch (final Throwable t) {
            return null;
        }
    }

    /** "trung tam" khi dung dich vu moi, "du phong" khi dung bang cau_hinh truc tiep. */
    public static String cheDo() {
        try {
            final Class<?> c = lopTrungTam();
            if (c != null) {
                c.getMethod("danhMuc").invoke(null);
                return "dịch vụ cấu hình";
            }
        } catch (final Throwable ignored) {
        }
        return "dự phòng";
    }

    /** Doc danh muc + gia tri hieu luc (chay ngoai EDT). */
    public static List<Muc> doc() {
        final Class<?> c = lopTrungTam();
        if (c != null) {
            try {
                return docTrungTam(c);
            } catch (final Throwable ignored) {
            }
        }
        return docDuPhong();
    }

    @SuppressWarnings("unchecked")
    static List<Muc> docTrungTam(final Class<?> c) throws Exception {
        final Object[] dm = (Object[]) c.getMethod("danhMuc").invoke(null);
        final Object anh = c.getMethod("anhChup").invoke(null);
        final Object kq = anh.getClass().getField("kq").get(anh);
        final Map<String, String> hieuLuc = (Map<String, String>) kq.getClass().getField("hieuLuc").get(kq);
        final Map<String, String> nguon = (Map<String, String>) kq.getClass().getField("nguon").get(kq);
        Map<String, String> cho = Collections.emptyMap();
        try {
            cho = (Map<String, String>) kq.getClass().getField("choKhoiDong").get(kq);
        } catch (final Throwable ignored) {
        }
        final Map<String, String[]> db = docBangCauHinh();
        final Map<String, String> tenNhom = new java.util.HashMap<String, String>();
        try {
            for (final String[] n : (String[][]) c.getMethod("nhom").invoke(null)) {
                tenNhom.put(n[0], n[1]);
            }
        } catch (final Throwable ignored) {
        }
        final List<Muc> l = new ArrayList<Muc>();
        for (final Object d : dm) {
            // Khoa an (co dinh / luon bat, docs/cau-hinh.md): khong hien, giong trang web.
            if (Boolean.TRUE.equals(goi(d, "laAn"))) {
                continue;
            }
            final Muc m = new Muc();
            m.khoa = (String) d.getClass().getField("khoa").get(d);
            m.nhom = str(d.getClass().getField("nhom").get(d));
            if (tenNhom.containsKey(m.nhom)) {
                m.nhom = tenNhom.get(m.nhom);
            }
            m.kieu = str(d.getClass().getField("kieu").get(d));
            m.macDinh = str(d.getClass().getField("macDinh").get(d));
            m.apDungNgay = d.getClass().getField("apDungNgay").getBoolean(d);
            m.ten = str(goi(d, "ten"));
            m.moTa = str(goi(d, "moTa"));
            m.chiDoc = Boolean.TRUE.equals(goi(d, "laChiDoc")) || !Boolean.TRUE.equals(goi(d, "trongCauHinh"));
            m.nhay = Boolean.TRUE.equals(goi(d, "laNhay"));
            final Object lc = goi(d, "luaChon");
            if (lc instanceof String[]) {
                m.luaChon = (String[]) lc;
            }
            final Object mi = goi(d, "min"), ma = goi(d, "max");
            if (mi instanceof Long) {
                m.min = (Long) mi;
            }
            if (ma instanceof Long) {
                m.max = (Long) ma;
            }
            final String v = hieuLuc == null ? null : hieuLuc.get(m.khoa);
            m.giaTri = v == null ? m.macDinh : v;
            m.nguon = nguon == null ? "" : str(nguon.get(m.khoa));
            m.cho = cho == null ? null : cho.get(m.khoa);
            final String[] r = db.get(m.khoa);
            if (r != null) {
                m.suaBoi = str(r[1]);
                m.suaLuc = str(r[2]);
            }
            l.add(m);
        }
        return l;
    }

    static Object goi(final Object o, final String ten) {
        try {
            final Method m = o.getClass().getMethod(ten);
            return m.invoke(o);
        } catch (final Throwable t) {
            return null;
        }
    }

    static String str(final Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /** khoa -> {gia_tri, sua_boi, sua_luc}. */
    static Map<String, String[]> docBangCauHinh() {
        final Map<String, String[]> m = new LinkedHashMap<String, String[]>();
        final List<Object[]> r = ThaoTacMayChu.truyVan("SELECT khoa, gia_tri, sua_boi, sua_luc FROM cau_hinh ORDER BY khoa");
        if (ThaoTacMayChu.loiTruyVan(r) == null) {
            for (int i = 1; i < r.size(); i++) {
                m.put(str(r.get(i)[0]), new String[]{str(r.get(i)[1]), str(r.get(i)[2]), str(r.get(i)[3])});
            }
        }
        return m;
    }

    /** Khoa nap lai duoc cua CauHinh cu (doc mang rieng qua phan chieu de khong lap danh sach). */
    static String[] khoaCu() {
        try {
            final java.lang.reflect.Field f = server.CauHinh.class.getDeclaredField("TAT_CA_KHOA");
            f.setAccessible(true);
            return ((String[]) f.get(null)).clone();
        } catch (final Throwable t) {
            return new String[]{server.CauHinh.K_TILE_XU, server.CauHinh.K_TILE_LUONG, server.CauHinh.K_TILE_ITEM};
        }
    }

    static final java.util.Set<String> CHI_DOC_DU_PHONG = new java.util.HashSet<String>(java.util.Arrays.asList(
            "sv.adminAccounts", "sv.testCode"));

    static List<Muc> docDuPhong() {
        final Map<String, String[]> db = docBangCauHinh();
        final Properties ini = ThaoTacMayChu.docServerIni();
        final List<Muc> l = new ArrayList<Muc>();
        for (final String k : khoaCu()) {
            final Muc m = new Muc();
            m.khoa = k;
            m.nhom = k.contains("tieu") || k.contains("Tieu") ? "Giao hàng" : k.contains("tile") || k.contains("chia") ? "Tỉ lệ rơi"
                    : k.contains("ChiemThanh") ? "Chiếm Gym" : k.contains("suKien") || k.contains("worldCup") ? "Sự kiện" : "Chung";
            final String[] r = db.get(k);
            m.giaTri = r != null ? r[0] : ini.getProperty(k, "");
            m.nguon = r != null ? "db" : ini.getProperty(k) != null ? "ini" : "";
            m.suaBoi = r == null ? "" : r[1];
            m.suaLuc = r == null ? "" : r[2];
            m.kieu = m.giaTri.matches("-?\\d+") ? (m.giaTri.equals("0") || m.giaTri.equals("1") ? "co" : "so")
                    : m.giaTri.matches("-?\\d+(,-?\\d+)+") ? "ds_so" : "chuoi";
            m.chiDoc = CHI_DOC_DU_PHONG.contains(k);
            m.nhay = "sv.testCode".equals(k);
            m.apDungNgay = true;
            l.add(m);
        }
        return l;
    }

    /** Kiem gia tri moi (du phong; trung tam tu kiem lai khi ghi). null = hop le. */
    public static String kiem(final Muc m, final String v) {
        final String g = v == null ? "" : v.trim();
        if ("co".equals(m.kieu) && !g.equals("0") && !g.equals("1")) {
            return "Chỉ nhận 0 hoặc 1.";
        }
        if ("so".equals(m.kieu)) {
            try {
                final long n = Long.parseLong(g);
                if (n < m.min || n > m.max) {
                    return "Ngoài khoảng " + m.min + " … " + m.max + ".";
                }
            } catch (final NumberFormatException e) {
                return "Phải là số nguyên.";
            }
        }
        if ("ds_so".equals(m.kieu) && !g.matches("-?\\d+(\\s*,\\s*-?\\d+)*")) {
            return "Danh sách số ngăn bằng dấu phẩy.";
        }
        if (m.luaChon.length > 0) {
            boolean co = false;
            for (final String x : m.luaChon) {
                co |= x.equals(g);
            }
            if (!co) {
                return "Chỉ nhận: " + String.join(", ", m.luaChon);
            }
        }
        if (g.length() > 255) {
            return "Dài quá 255 ký tự.";
        }
        return null;
    }

    /** Co dich vu trung tam (ghi duoc qua no) khong. */
    public static boolean coTrungTam() {
        final Class<?> c = lopTrungTam();
        try {
            return c != null && c.getMethod("ghi", Map.class, String.class, String.class, String.class) != null;
        } catch (final Throwable t) {
            return false;
        }
    }

    static String nguoi() {
        return "panel:" + System.getProperty("user.name", "?");
    }

    /**
     * Ghi nhieu khoa MOT lan qua dich vu trung tam (kiem + lich su + nap lai ngay). null = khong co dich vu;
     * nguoc lai cau ket qua (bat dau "OK" khi thanh cong).
     */
    public static String ghiNhieu(final Map<String, String> doi, final String lyDo) {
        final Class<?> c = lopTrungTam();
        if (c == null) {
            return null;
        }
        try {
            final Object r = c.getMethod("ghi", Map.class, String.class, String.class, String.class).invoke(null, doi, nguoi(), "swing", lyDo);
            final boolean ok = r.getClass().getField("ok").getBoolean(r);
            final String kq = (ok ? "OK " : "Từ chối: ") + str(r.getClass().getField("thongBao").get(r))
                    + (ok ? "" : " " + str(r.getClass().getField("loi").get(r)));
            KiemToanQuanTri.ghi("CAU_HINH", String.valueOf(doi.keySet()), doi + " | " + lyDo + " | " + kq);
            return kq;
        } catch (final Throwable t) {
            return null;
        }
    }

    /** Khoi dong lai AN TOAN qua dich vu (dem nguoc, bao nguoi choi, luu het, supervisor bat lai). null = khong co. */
    public static Boolean yeuCauKhoiDongLai(final int giay, final String lyDo) {
        final Class<?> c = lopTrungTam();
        if (c == null) {
            return null;
        }
        try {
            final Boolean ok = (Boolean) c.getMethod("yeuCauKhoiDongLai", int.class, String.class, String.class).invoke(null, giay, lyDo, nguoi());
            KiemToanQuanTri.ghi("KHOI_DONG_LAI_YEU_CAU", "", giay + "s " + lyDo + " -> " + ok);
            return ok;
        } catch (final Throwable t) {
            return null;
        }
    }

    public static Boolean huyKhoiDongLai() {
        final Class<?> c = lopTrungTam();
        if (c == null) {
            return null;
        }
        try {
            final Boolean ok = (Boolean) c.getMethod("huyKhoiDongLai", String.class).invoke(null, nguoi());
            KiemToanQuanTri.ghi("KHOI_DONG_LAI_HUY", "", String.valueOf(ok));
            return ok;
        } catch (final Throwable t) {
            return null;
        }
    }

    /** {trang_thai, giay, ly_do, bat_dau_luc(ms hoac 0)} cua yeu cau khoi dong lai, null neu khong doc duoc. */
    public static Object[] trangThaiKhoiDongLai() {
        final List<Object[]> r = ThaoTacMayChu.truyVan("SELECT trang_thai, giay, ly_do, bat_dau_luc FROM cau_hinh_khoi_dong_lai WHERE id = 1");
        if (ThaoTacMayChu.loiTruyVan(r) != null || r.size() <= 1) {
            return null;
        }
        final Object[] d = r.get(1);
        long bd = 0;
        if (d[3] instanceof java.util.Date) {
            bd = ((java.util.Date) d[3]).getTime();
        }
        return new Object[]{str(d[0]), d[1] instanceof Number ? ((Number) d[1]).intValue() : 0, str(d[2]), bd};
    }

    /** Ghi (chay ngoai EDT). Tra ve cau ket qua; ghi kiem toan. */
    public static String ghi(final Muc m, final String giaTri, final String lyDo) {
        final String loi = kiem(m, giaTri);
        if (loi != null) {
            return "Không hợp lệ: " + loi;
        }
        final String v = giaTri.trim();
        final Class<?> c = lopTrungTam();
        String kq = null;
        if (c != null) {
            try {
                final Map<String, String> doi = new LinkedHashMap<String, String>();
                doi.put(m.khoa, v);
                final Object r = c.getMethod("ghi", Map.class, String.class, String.class, String.class)
                        .invoke(null, doi, "panel:" + System.getProperty("user.name", "?"), "swing", lyDo);
                final boolean ok = r.getClass().getField("ok").getBoolean(r);
                final Object tb = r.getClass().getField("thongBao").get(r);
                final Object ls = r.getClass().getField("loi").get(r);
                kq = ok ? "Đã lưu " + m.khoa + " = " + (m.nhay ? "***" : v) + ". " + str(tb)
                        : "Từ chối: " + str(tb) + " " + str(ls);
            } catch (final Throwable t) {
                kq = null;
            }
        }
        if (kq == null) {
            final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put(m.khoa, v);
            final boolean ok = server.CauHinh.luuNhieu(map, "panel");
            kq = ok ? "Đã lưu " + m.khoa + " = " + (m.nhay ? "***" : v) + " (máy chủ áp dụng trong ≤ 10 giây)."
                    : "Không lưu được cấu hình.";
        }
        KiemToanQuanTri.ghi("CAU_HINH", m.khoa, (m.nhay ? "***" : m.giaTri + " -> " + v) + " | " + lyDo + " | " + kq);
        return kq;
    }
}
