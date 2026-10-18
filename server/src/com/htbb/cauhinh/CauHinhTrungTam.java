package com.htbb.cauhinh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CAU HINH TRUNG TAM (docs/cau-hinh.md) - MOT dich vu cho moi khoa cau hinh chinh duoc luc chay.
 *
 * NGUON: bang {@code cau_hinh} (CSDL) > server.ini > mac dinh trong danh muc (tools/cauhinh/danh_muc.json ->
 * {@link DanhMucSinh}). Kiem kieu / khoang / lua chon ({@link KiemGiaTri}); gia tri sai bi TU CHOI, giu gia
 * tri tot gan nhat ({@link TinhCauHinh}).
 *
 * NAP LAI NONG: luong "CauHinhTrungTam" moi {@link #CHU_KY_MS} hoi 3 truy van re (phien ban, COUNT/MAX(sua_luc)
 * cua cau_hinh, trang thai yeu cau khoi dong lai). Doi -> doc lai ca bang, tinh, bao cac {@link NgheThayDoi}
 * (khoa doi), ghi dong xac nhan {@code cau_hinh_ap_dung} (phien ban + gio ap dung) va {@code cau_hinh_hieu_luc}
 * (gia tri dang chay tung khoa + gia tri cho khoi dong lai). Khong doi gi van doc lai ca bang moi
 * {@link #CHU_KY_DAY_DU_MS} (bat thay doi cua ben ghi cu khong tang phien ban).
 *
 * KHOA KHOI DONG LAI (sv.lau_dai, sv.he_sinh_thai, cong, CSDL...): ap dung MOT lan luc khoi dong
 * ({@link #khoiTao(Properties)} ghi de Properties cua server.ini TRUOC khi TeamServer doc cac co), sau do dong
 * bang. Cong / CSDL KHONG nam trong danh muc (con ga - quả trung), chi doc tu server.ini.
 *
 * KHOI DONG LAI AN TOAN: web ghi {@code cau_hinh_khoi_dong_lai.trang_thai='cho'} (giay, ly do) -> may chu dem
 * nguoc, bao nguoi choi, het gio bat co {@code 5h_systems restart} (luong bao tri luu het du lieu roi thoat),
 * supervisor (systemd Restart=always / chay_lap.sh) bat lai. Huy duoc trong luc dem.
 *
 * API CHO NOI KHAC (Swing AdminPanel, lenh trong game, test):
 *   - doc: {@link #co}, {@link #so}, {@link #chuoi}, {@link #dsChuoi}, {@link #anhChup()}, {@link #danhMuc()}
 *   - ghi: {@link #ghi(Map, String, String, String)} (kiem + ghi + lich su + tang phien ban + nap lai ngay)
 *   - nghe: {@link #dangKyNghe(String[], NgheThayDoi)}
 *   - khoi dong lai: {@link #yeuCauKhoiDongLai(int, String, String)}, {@link #huyKhoiDongLai(String)}
 */
public final class CauHinhTrungTam {

    public static final long CHU_KY_MS = 5000L;
    public static final long CHU_KY_DAY_DU_MS = 60000L;
    public static final long CHU_KY_NHIP_MS = 30000L;
    public static final int KHOI_DONG_LAI_GIAY_MIN = 10;
    public static final int KHOI_DONG_LAI_GIAY_MAX = 1800;
    /** Khoa server.ini (chi ini): cho phep khoa thu nghiem nhan gia tri khac mac dinh tu CSDL. */
    public static final String K_CHO_PHEP_THU_NGHIEM = "sv.cho_phep_thu_nghiem";

    private static final DinhNghiaKhoa[] DM = DanhMucSinh.tatCa();
    private static final Map<String, DinhNghiaKhoa> THEO_KHOA = new LinkedHashMap<String, DinhNghiaKhoa>();

    static {
        for (final DinhNghiaKhoa d : DM) {
            THEO_KHOA.put(d.khoa, d);
        }
    }

    /** Anh chup bat bien cua cau hinh dang chay. */
    public static final class AnhChup {
        public final TinhCauHinh.KetQua kq;
        /** Phien ban (cau_hinh_phien_ban) luc nap; -1 = chua doc duoc. */
        public final long phienBan;
        public final long napLuc;

        AnhChup(final TinhCauHinh.KetQua kq, final long phienBan, final long napLuc) {
            this.kq = kq;
            this.phienBan = phienBan;
            this.napLuc = napLuc;
        }

        public String chuoi(final String k) {
            final String v = kq.hieuLuc.get(k);
            if (v != null) {
                return v;
            }
            final String t = kq.tatCa.get(k);
            if (t != null) {
                return t;
            }
            final DinhNghiaKhoa d = THEO_KHOA.get(k);
            return d == null ? "" : d.macDinh;
        }

        /** Toan bo khoa (Properties) cho ma doc cu nhan Properties. */
        public Properties thanhProperties() {
            final Properties p = new Properties();
            for (final Map.Entry<String, String> e : kq.tatCa.entrySet()) {
                if (e.getValue() != null) {
                    p.setProperty(e.getKey(), e.getValue());
                }
            }
            return p;
        }
    }

    /** Mot nguoi nghe: khoa chinh xac hoac tien to ket thuc bang "." hay "*". */
    private static final class Nghe {
        final String[] mau;
        final NgheThayDoi ham;
        final String ten;

        Nghe(final String[] mau, final NgheThayDoi ham, final String ten) {
            this.mau = mau;
            this.ham = ham;
            this.ten = ten;
        }

        Set<String> loc(final Set<String> doi) {
            if (mau == null || mau.length == 0) {
                return doi;
            }
            final Set<String> ra = new TreeSet<String>();
            for (final String k : doi) {
                for (final String m : mau) {
                    if (m.endsWith("*") ? k.startsWith(m.substring(0, m.length() - 1))
                            : (m.endsWith(".") ? k.startsWith(m) : k.equals(m))) {
                        ra.add(k);
                        break;
                    }
                }
            }
            return ra;
        }
    }

    private static final List<Nghe> NGHE = new CopyOnWriteArrayList<Nghe>();

    private static volatile Map<String, String> ini = Collections.emptyMap();
    private static volatile AnhChup hienTai = null;
    private static volatile boolean choPhepThuNghiem = false;
    /** Dong CSDL lan doc truoc: khoa -> {gia_tri, sua_boi} (de ghi lich su thay doi do ben ngoai ghi). */
    private static Map<String, String[]> dbTruoc = null;
    private static volatile String chuKy = "";
    private static volatile long lanDayDuCuoi = 0L;
    private static volatile long lanNhipCuoi = 0L;
    private static volatile boolean daTaoBang = false;
    private static volatile boolean daBatDau = false;
    private static volatile boolean lanDauNapLai = true;
    private static volatile String mayChu = "sv";
    private static volatile long khoiDongLuc = System.currentTimeMillis();
    private static volatile String thayDoiCuoi = "";
    private static long lanBaoLoi = 0L;
    private static volatile boolean dangDem = false;

    private CauHinhTrungTam() {
    }

    // =================================================================== DOC (moi noi dung)

    public static DinhNghiaKhoa[] danhMuc() {
        return DM.clone();
    }

    public static String[][] nhom() {
        return DanhMucSinh.NHOM.clone();
    }

    public static DinhNghiaKhoa dinhNghia(final String khoa) {
        return THEO_KHOA.get(khoa);
    }

    /** Anh chup dang chay; chua khoi tao (vd test) -> anh chup chi gom mac dinh. */
    public static AnhChup anhChup() {
        final AnhChup h = hienTai;
        if (h != null) {
            return h;
        }
        return new AnhChup(TinhCauHinh.tinh(DM, null, null, null, false), -1L, 0L);
    }

    public static String chuoi(final String khoa) {
        final AnhChup h = hienTai;
        if (h != null) {
            return h.chuoi(khoa);
        }
        final DinhNghiaKhoa d = THEO_KHOA.get(khoa);
        return d == null ? "" : d.macDinh;
    }

    public static boolean co(final String khoa) {
        return "1".equals(chuoi(khoa));
    }

    public static long so(final String khoa, final long macDinh) {
        try {
            return Long.parseLong(chuoi(khoa).trim());
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    public static List<String> dsChuoi(final String khoa) {
        final List<String> ra = new ArrayList<String>();
        for (final String x : chuoi(khoa).split(",")) {
            if (!x.trim().isEmpty()) {
                ra.add(x.trim());
            }
        }
        return ra;
    }

    /** server.ini co sv.cho_phep_thu_nghiem=1 (may thu) khong. */
    public static boolean choPhepThuNghiem() {
        return choPhepThuNghiem;
    }

    public static String mayChu() {
        return mayChu;
    }

    // =================================================================== NGHE

    /**
     * Dang ky ham goi khi khoa doi (sau khi da ap vao anh chup moi). {@code mau}: khoa chinh xac, tien to "sv.spam."
     * hoac "sv.dangKy*"; null/rong = moi khoa. Ham chay tren luong nap lai, PHAI nhanh, khong nem loi ra ngoai.
     */
    public static void dangKyNghe(final String[] mau, final NgheThayDoi ham) {
        dangKyNghe(mau, ham, ham.getClass().getName());
    }

    public static void dangKyNghe(final String[] mau, final NgheThayDoi ham, final String ten) {
        NGHE.add(new Nghe(mau, ham, ten));
    }

    static void baoNghe(final Set<String> doi, final AnhChup moi) {
        if (doi.isEmpty()) {
            return;
        }
        for (final Nghe n : NGHE) {
            final Set<String> cua = n.loc(doi);
            if (cua.isEmpty()) {
                continue;
            }
            try {
                n.ham.khiDoi(cua, moi);
            } catch (final Throwable t) {
                System.out.println("[CAU HINH] loi khi ap dung (" + n.ten + "): " + t);
            }
        }
    }

    // =================================================================== KHOI TAO (TeamServer)

    /**
     * Goi MOT lan trong TeamServer NGAY SAU Database.setLink*(...) va TRUOC khi doc cac co
     * (dai han, nap tien...). Doc bang cau_hinh bang ket noi JDBC rieng co han gio (khong qua pool), tinh cau
     * hinh hieu luc va GHI DE vao {@code p} cac khoa lay tu CSDL - nhu vay moi dong {@code p.getProperty(...)}
     * cu ben duoi tu dong theo thu tu CSDL > ini > mac dinh. Loi CSDL -> chi dung server.ini (kiemTraCSDL ben
     * duoi se dung server neu CSDL hong that).
     */
    public static synchronized void khoiTao(final Properties p) {
        final Map<String, String> m = new HashMap<String, String>();
        for (final String k : p.stringPropertyNames()) {
            m.put(k, p.getProperty(k));
        }
        ini = Collections.unmodifiableMap(m);
        choPhepThuNghiem = "1".equals(String.valueOf(p.getProperty(K_CHO_PHEP_THU_NGHIEM, "0")).trim());
        try {
            mayChu = ("sv" + server.TeamServer.server + ":" + server.TeamServer.PORT);
        } catch (final Throwable ignored) {
        }
        khoiDongLuc = System.currentTimeMillis();
        Map<String, String> db = null;
        try {
            db = docTrucTiep(p);
        } catch (final Exception e) {
            System.out.println("[CAU HINH] khong doc duoc cau_hinh luc khoi dong (" + e.getMessage()
                    + ") - dung server.ini, se thu lai khi nap lai.");
        }
        final TinhCauHinh.KetQua kq = TinhCauHinh.tinh(DM, ini, db, null, choPhepThuNghiem);
        hienTai = new AnhChup(kq, -1L, System.currentTimeMillis());
        int soDb = 0;
        for (final DinhNghiaKhoa d : DM) {
            if (d.trongCauHinh() && TinhCauHinh.NGUON_DB.equals(kq.nguon.get(d.khoa))) {
                p.setProperty(d.khoa, kq.hieuLuc.get(d.khoa));
                soDb++;
                if (!d.apDungNgay) {
                    System.out.println("[CAU HINH] " + d.khoa + "=" + an(d, kq.hieuLuc.get(d.khoa))
                            + " (tu CSDL, de server.ini" + (m.containsKey(d.khoa) ? "=" + an(d, m.get(d.khoa)) : "") + ")");
                }
            }
        }
        for (final Map.Entry<String, String> e : kq.loi.entrySet()) {
            System.out.println("[CAU HINH] CANH BAO " + e.getKey() + ": " + e.getValue());
        }
        System.out.println("[CAU HINH] khoi tao: " + DM.length + " khoa danh muc, " + soDb + " lay tu CSDL, "
                + kq.loi.size() + " canh bao" + (choPhepThuNghiem ? ", CHO PHEP THU NGHIEM" : ""));
        ApDungMacDinh.dangKy();
        if (soDb > 0) {
            // Nhat ky da doc server.ini truoc khi co CSDL: doc lai neu muc nhat ky den tu CSDL.
            for (final String k : kq.nguon.keySet()) {
                if (k.startsWith("sv.log.") && TinhCauHinh.NGUON_DB.equals(kq.nguon.get(k))) {
                    try {
                        com.htbb.log.Nhat.docCauHinh(p, p.getProperty("sv.pathLog"));
                    } catch (final Throwable ignored) {
                    }
                    break;
                }
            }
        }
    }

    /** Doc cau_hinh bang JDBC truc tiep (han gio 5 giay) - chi dung luc khoi dong, truoc khi pool chac chan song. */
    static Map<String, String> docTrucTiep(final Properties p) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (final ClassNotFoundException ignored) {
        }
        final Properties tk = new Properties();
        tk.setProperty("user", String.valueOf(p.getProperty("db.user", "")));
        tk.setProperty("password", String.valueOf(p.getProperty("db.password", "")));
        tk.setProperty("connectTimeout", "5000");
        tk.setProperty("socketTimeout", "10000");
        final Connection c = DriverManager.getConnection(
                data.Database.urlJdbc(p.getProperty("db.host"), p.getProperty("db.name"), p.getProperty("db.thamSoJdbc")), tk);
        try {
            final Map<String, String> ra = new HashMap<String, String>();
            final Statement st = c.createStatement();
            try {
                final ResultSet rs = st.executeQuery("SELECT khoa, gia_tri FROM cau_hinh");
                while (rs.next()) {
                    ra.put(rs.getString(1), rs.getString(2) == null ? "" : rs.getString(2));
                }
                rs.close();
            } catch (final SQLException e) {
                // Chua co bang (CSDL moi): coi nhu rong.
                return ra;
            } finally {
                st.close();
            }
            return ra;
        } finally {
            c.close();
        }
    }

    // =================================================================== NAP LAI

    /** Bat luong nap lai nong (goi MOT lan; lan sau khong lam gi). */
    public static synchronized void batDau() {
        if (daBatDau) {
            return;
        }
        daBatDau = true;
        final Thread t = new Thread(new Runnable() {
            public void run() {
                vongLap();
            }
        }, "CauHinhTrungTam");
        t.setDaemon(true);
        t.start();
    }

    static void vongLap() {
        while (true) {
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                return;
            }
            try {
                motNhip();
            } catch (final Throwable e) {
                baoLoi("nhip nap lai", e);
            }
        }
    }

    static void motNhip() throws SQLException {
        final long bay = System.currentTimeMillis();
        String ky = null;
        String trangThaiKdl = "";
        Connection c = null;
        try {
            c = layKetNoi();
            taoBangNeuThieu(c);
            ky = docChuKy(c);
            trangThaiKdl = docTrangThaiKhoiDongLai(c);
        } finally {
            traKetNoi(c);
        }
        if (!ky.equals(chuKy) || bay - lanDayDuCuoi >= CHU_KY_DAY_DU_MS) {
            napLaiNgay(ky.equals(chuKy) ? "dinh-ky" : "thay-doi");
        } else if (bay - lanNhipCuoi >= CHU_KY_NHIP_MS) {
            ghiNhip();
        }
        if ("cho".equals(trangThaiKdl) && !dangDem) {
            batDauDemNguoc();
        }
    }

    /** Doc lai ngay (lenh "nap lai" trong game / sau khi ghi). Tra mo ta ket qua. */
    public static synchronized String napLaiNgay(final String lyDo) {
        Connection c = null;
        try {
            c = layKetNoi();
            taoBangNeuThieu(c);
            final String ky = docChuKy(c);
            final long pb = docPhienBan(c);
            final Map<String, String[]> dong = docBang(c);
            final Map<String, String> db = new HashMap<String, String>();
            for (final Map.Entry<String, String[]> e : dong.entrySet()) {
                db.put(e.getKey(), e.getValue()[0]);
            }
            final AnhChup cu = hienTai;
            final TinhCauHinh.KetQua kq = TinhCauHinh.tinh(DM, ini, db, cu == null ? null : cu.kq, choPhepThuNghiem);
            final AnhChup moi = new AnhChup(kq, pb, System.currentTimeMillis());
            final Set<String> doi = lanDauNapLai || cu == null ? new TreeSet<String>(kq.tatCa.keySet()) : khacNhau(cu.kq, kq);
            if (lanDauNapLai || cu == null) {
                for (final DinhNghiaKhoa d : DM) {
                    if (d.trongCauHinh()) {
                        doi.add(d.khoa);
                    }
                }
            }
            final Map<String, String[]> truoc = dbTruoc;
            hienTai = moi;
            chuKy = ky;
            lanDayDuCuoi = System.currentTimeMillis();
            final boolean lanDau = lanDauNapLai;
            lanDauNapLai = false;
            baoNghe(doi, moi);
            if (truoc != null) {
                ghiLichSuPhatHien(c, truoc, dong, pb, cu, moi);
            }
            dbTruoc = dong;
            final Set<String> doiDanhMuc = new TreeSet<String>();
            for (final String k : doi) {
                final DinhNghiaKhoa d = THEO_KHOA.get(k);
                if (d != null && d.trongCauHinh()) {
                    doiDanhMuc.add(k);
                }
            }
            if (!lanDau && !doiDanhMuc.isEmpty()) {
                final StringBuilder b = new StringBuilder();
                for (final String k : doiDanhMuc) {
                    final DinhNghiaKhoa d = THEO_KHOA.get(k);
                    final String vCu = cu == null ? "" : cu.chuoi(k);
                    final String vMoi = moi.chuoi(k);
                    if (eq(vCu, vMoi)) {
                        continue; // vd dong moi ghi dung bang gia tri dang chay - khong phai thay doi hieu luc
                    }
                    if (b.length() > 0) {
                        b.append("; ");
                    }
                    b.append(k).append(": ").append(an(d, vCu)).append(" -> ").append(an(d, vMoi));
                    System.out.println("[CAU HINH] AP DUNG " + k + ": " + an(d, vCu) + " -> " + an(d, vMoi)
                            + " (nguon " + kq.nguon.get(k) + ", phien ban " + pb + ")");
                }
                thayDoiCuoi = b.length() > 1900 ? b.substring(0, 1900) + "..." : b.toString();
            }
            for (final Map.Entry<String, String> e : kq.loi.entrySet()) {
                if (cu == null || lanDau || !e.getValue().equals(cu.kq.loi.get(e.getKey()))) {
                    System.out.println("[CAU HINH] TU CHOI / CANH BAO " + e.getKey() + ": " + e.getValue());
                }
            }
            for (final Map.Entry<String, String> e : kq.choKhoiDong.entrySet()) {
                if (cu == null || lanDau || !e.getValue().equals(cu.kq.choKhoiDong.get(e.getKey()))) {
                    System.out.println("[CAU HINH] " + e.getKey() + " = " + an(THEO_KHOA.get(e.getKey()), e.getValue())
                            + " CHO KHOI DONG LAI (dang chay: " + an(THEO_KHOA.get(e.getKey()), kq.hieuLuc.get(e.getKey())) + ")");
                }
            }
            ghiXacNhan(c, moi, cu == null ? null : cu.kq, lanDau);
            return "Đã nạp lại cấu hình (phiên bản " + pb + ", " + (lanDau ? "lần đầu" : doiDanhMuc.size() + " khoá đổi")
                    + (kq.loi.isEmpty() ? "" : ", " + kq.loi.size() + " giá trị bị từ chối") + ").";
        } catch (final Exception e) {
            baoLoi("nap lai (" + lyDo + ")", e);
            return "Không nạp được cấu hình: " + e;
        } finally {
            traKetNoi(c);
        }
    }

    static Set<String> khacNhau(final TinhCauHinh.KetQua a, final TinhCauHinh.KetQua b) {
        final Set<String> ra = new TreeSet<String>();
        final Set<String> moiKhoa = new HashSet<String>(a.tatCa.keySet());
        moiKhoa.addAll(b.tatCa.keySet());
        for (final String k : moiKhoa) {
            final String x = a.tatCa.get(k);
            final String y = b.tatCa.get(k);
            if (x == null ? y != null : !x.equals(y)) {
                ra.add(k);
            }
        }
        for (final String k : b.hieuLuc.keySet()) {
            final String x = a.hieuLuc.get(k);
            if (x == null || !x.equals(b.hieuLuc.get(k))) {
                ra.add(k);
            }
        }
        return ra;
    }

    // =================================================================== GHI (Swing / trong game)

    /** Ket qua ghi. */
    public static final class KetQuaGhi {
        public final boolean ok;
        /** khoa -> loi (rong khi ok). */
        public final Map<String, String> loi;
        public final int soDoi;
        public final long phienBan;
        public final String thongBao;

        KetQuaGhi(final boolean ok, final Map<String, String> loi, final int soDoi, final long pb, final String tb) {
            this.ok = ok;
            this.loi = loi;
            this.soDoi = soDoi;
            this.phienBan = pb;
            this.thongBao = tb;
        }
    }

    /**
     * Ghi nhieu khoa MOT lan (tat ca hoac khong): kiem tung gia tri, ghi bang cau_hinh, ghi
     * cau_hinh_lich_su, tang phien ban roi nap lai ngay. {@code giaTri == null} = xoa dong (ve server.ini /
     * mac dinh). {@code nguon}: "swing" | "game" | "web" ... (de lich su biet ai).
     */
    public static KetQuaGhi ghi(final Map<String, String> thayDoi, final String nguoiSua, final String nguon, final String lyDo) {
        final Map<String, String> loi = new LinkedHashMap<String, String>();
        final Map<String, String> chuan = new LinkedHashMap<String, String>();
        for (final Map.Entry<String, String> e : thayDoi.entrySet()) {
            final DinhNghiaKhoa d = THEO_KHOA.get(e.getKey());
            if (d == null || !d.trongCauHinh() || d.laChiDoc()) {
                loi.put(e.getKey(), "khoá không sửa được ở đây");
                continue;
            }
            if (e.getValue() == null) {
                chuan.put(d.khoa, null);
                continue;
            }
            final KiemGiaTri.KetQua r = KiemGiaTri.kiem(d, e.getValue());
            if (!r.dung()) {
                loi.put(d.khoa, r.loi);
            } else if (d.laKhoaProd() && !choPhepThuNghiem && !r.giaTri.equals(d.macDinh)) {
                loi.put(d.khoa, "khoá thử nghiệm — máy thật chỉ nhận mặc định");
            } else {
                chuan.put(d.khoa, r.giaTri);
            }
        }
        if (!loi.isEmpty()) {
            return new KetQuaGhi(false, loi, 0, -1L, "Có giá trị không hợp lệ — không lưu gì.");
        }
        final String ai = nguoiSua == null || nguoiSua.isEmpty() ? nguon : nguoiSua;
        Connection c = null;
        long pb = -1L;
        int soDoi = 0;
        try {
            c = layKetNoi();
            taoBangNeuThieu(c);
            final Map<String, String[]> dong = docBang(c);
            pb = docPhienBan(c) + 1;
            final Timestamp bay = new Timestamp(System.currentTimeMillis());
            for (final Map.Entry<String, String> e : chuan.entrySet()) {
                final String[] cuD = dong.get(e.getKey());
                final String vCu = cuD == null ? null : cuD[0];
                if (vCu == null ? e.getValue() == null : vCu.equals(e.getValue())) {
                    continue;
                }
                if (e.getValue() == null) {
                    capNhat(c, "DELETE FROM cau_hinh WHERE khoa = ?", e.getKey());
                } else {
                    capNhat(c, "INSERT IGNORE INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, ?, ?)",
                            e.getKey(), e.getValue(), cat(ai, 50), bay);
                    capNhat(c, "UPDATE cau_hinh SET gia_tri = ?, sua_boi = ?, sua_luc = ? WHERE khoa = ?",
                            e.getValue(), cat(ai, 50), bay, e.getKey());
                }
                ghiLichSu(c, e.getKey(), "cau_hinh", vCu, e.getValue(), ai, nguon, lyDo, pb, bay);
                soDoi++;
            }
            if (soDoi > 0) {
                tangPhienBan(c, ai, bay);
            }
        } catch (final Exception e) {
            loi.put("*", String.valueOf(e.getMessage()));
            return new KetQuaGhi(false, loi, 0, -1L, "Lỗi CSDL: " + e.getMessage());
        } finally {
            traKetNoi(c);
        }
        final String nl = soDoi > 0 ? napLaiNgay("ghi:" + nguon) : "Không có gì thay đổi.";
        return new KetQuaGhi(true, loi, soDoi, pb, nl);
    }

    /** Ben ghi CU (CauHinh.luuNhieu, bang setting trong game) bao da ghi: chi tang phien ban de nap lai som. */
    public static void tangPhienBan(final String nguoiSua) {
        Connection c = null;
        try {
            c = layKetNoi();
            taoBangNeuThieu(c);
            tangPhienBan(c, nguoiSua, new Timestamp(System.currentTimeMillis()));
        } catch (final Exception e) {
            baoLoi("tang phien ban", e);
        } finally {
            traKetNoi(c);
        }
    }

    static void tangPhienBan(final Connection c, final String ai, final Timestamp bay) throws SQLException {
        capNhat(c, "INSERT IGNORE INTO cau_hinh_phien_ban (id, phien_ban) VALUES (1, 0)");
        capNhat(c, "UPDATE cau_hinh_phien_ban SET phien_ban = phien_ban + 1, sua_luc = ?, sua_boi = ? WHERE id = 1",
                bay, cat(ai, 50));
    }

    // =================================================================== KHOI DONG LAI AN TOAN

    /** Yeu cau khoi dong lai sau {@code giay} giay (10..1800). false = dang co yeu cau khac / loi. */
    public static boolean yeuCauKhoiDongLai(final int giay, final String lyDo, final String nguoi) {
        final int g = Math.max(KHOI_DONG_LAI_GIAY_MIN, Math.min(KHOI_DONG_LAI_GIAY_MAX, giay));
        Connection c = null;
        try {
            c = layKetNoi();
            taoBangNeuThieu(c);
            return capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'cho', giay = ?, ly_do = ?, yeu_cau_boi = ?,"
                    + " yeu_cau_luc = ?, bat_dau_luc = NULL, thoat_luc = NULL, xong_luc = NULL"
                    + " WHERE id = 1 AND trang_thai NOT IN ('cho', 'dang_dem', 'huy', 'dang_thoat')",
                    Integer.valueOf(g), cat(lyDo == null ? "" : lyDo, 200), cat(nguoi, 50),
                    new Timestamp(System.currentTimeMillis())) > 0;
        } catch (final Exception e) {
            baoLoi("yeu cau khoi dong lai", e);
            return false;
        } finally {
            traKetNoi(c);
        }
    }

    public static boolean huyKhoiDongLai(final String nguoi) {
        Connection c = null;
        try {
            c = layKetNoi();
            taoBangNeuThieu(c);
            final int a = capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'da_huy', xong_luc = ? WHERE id = 1"
                    + " AND trang_thai = 'cho'", new Timestamp(System.currentTimeMillis()));
            final int b = capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'huy' WHERE id = 1 AND trang_thai = 'dang_dem'");
            return a + b > 0;
        } catch (final Exception e) {
            baoLoi("huy khoi dong lai", e);
            return false;
        } finally {
            traKetNoi(c);
        }
    }

    static synchronized void batDauDemNguoc() {
        if (dangDem) {
            return;
        }
        int giay = 60;
        String lyDo = "";
        Connection c = null;
        try {
            c = layKetNoi();
            final Object[] r = docMot(c, "SELECT giay, ly_do FROM cau_hinh_khoi_dong_lai WHERE id = 1 AND trang_thai = 'cho'");
            if (r == null) {
                return;
            }
            giay = Math.max(KHOI_DONG_LAI_GIAY_MIN, Math.min(KHOI_DONG_LAI_GIAY_MAX, ((Number) r[0]).intValue()));
            lyDo = r[1] == null ? "" : String.valueOf(r[1]);
            if (capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'dang_dem', bat_dau_luc = ?, may_chu = ?"
                    + " WHERE id = 1 AND trang_thai = 'cho'", new Timestamp(System.currentTimeMillis()), mayChu) == 0) {
                return;
            }
        } catch (final Exception e) {
            baoLoi("bat dau dem nguoc", e);
            return;
        } finally {
            traKetNoi(c);
        }
        dangDem = true;
        final int tong = giay;
        final String ld = lyDo;
        final Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    demNguoc(tong, ld);
                } finally {
                    dangDem = false;
                }
            }
        }, "CauHinhTrungTam-khoi-dong-lai");
        t.setDaemon(true);
        t.start();
    }

    static void demNguoc(final int tong, final String lyDo) {
        System.out.println("[CAU HINH] YEU CAU KHOI DONG LAI sau " + tong + " giay: " + lyDo);
        ThongBaoKhoiDong.gui(tong, lyDo);
        final long ketThuc = System.currentTimeMillis() + tong * 1000L;
        boolean daBao60 = tong <= 75;
        while (true) {
            final long con = ketThuc - System.currentTimeMillis();
            if (con <= 0) {
                break;
            }
            if (!daBao60 && con <= 60000L) {
                ThongBaoKhoiDong.gui(60, lyDo);
                daBao60 = true;
            }
            try {
                Thread.sleep(Math.min(5000L, con));
            } catch (final InterruptedException e) {
                return;
            }
            if ("huy".equals(trangThaiKhoiDongLai())) {
                capNhatTrangThaiKdl("UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'da_huy', xong_luc = ? WHERE id = 1 AND trang_thai = 'huy'");
                ThongBaoKhoiDong.guiHuy();
                System.out.println("[CAU HINH] DA HUY khoi dong lai.");
                return;
            }
        }
        capNhatTrangThaiKdl("UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'dang_thoat', thoat_luc = ? WHERE id = 1 AND trang_thai = 'dang_dem'");
        System.out.println("[CAU HINH] HET GIO -> luu du lieu + thoat (supervisor se bat lai).");
        ThongBaoKhoiDong.thoat();
    }

    static String trangThaiKhoiDongLai() {
        Connection c = null;
        try {
            c = layKetNoi();
            return docTrangThaiKhoiDongLai(c);
        } catch (final Exception e) {
            return "";
        } finally {
            traKetNoi(c);
        }
    }

    static void capNhatTrangThaiKdl(final String sql) {
        Connection c = null;
        try {
            c = layKetNoi();
            capNhat(c, sql, new Timestamp(System.currentTimeMillis()));
        } catch (final Exception e) {
            baoLoi("cap nhat khoi dong lai", e);
        } finally {
            traKetNoi(c);
        }
    }

    // =================================================================== BANG

    static void taoBangNeuThieu(final Connection c) throws SQLException {
        if (daTaoBang) {
            return;
        }
        final String[] ddl = {
            "CREATE TABLE IF NOT EXISTS `cau_hinh` (`khoa` varchar(64) NOT NULL, `gia_tri` varchar(255) NOT NULL DEFAULT '',"
                + " `sua_boi` varchar(50) DEFAULT NULL, `sua_luc` datetime DEFAULT NULL, PRIMARY KEY (`khoa`))"
                + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS `cau_hinh_phien_ban` (`id` TINYINT NOT NULL, `phien_ban` BIGINT NOT NULL DEFAULT 0,"
                + " `sua_luc` DATETIME NULL, `sua_boi` VARCHAR(50) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS `cau_hinh_lich_su` (`id` BIGINT NOT NULL AUTO_INCREMENT, `khoa` VARCHAR(64) NOT NULL,"
                + " `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `gia_tri_cu` VARCHAR(255) NULL, `gia_tri_moi` VARCHAR(255) NULL,"
                + " `nguoi_sua` VARCHAR(50) NULL, `nguon` VARCHAR(20) NOT NULL DEFAULT '', `ly_do` VARCHAR(255) NOT NULL DEFAULT '',"
                + " `phien_ban` BIGINT NOT NULL DEFAULT 0, `luc` DATETIME NOT NULL, PRIMARY KEY (`id`), KEY `idx_chls_khoa` (`khoa`),"
                + " KEY `idx_chls_luc` (`luc`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS `cau_hinh_ap_dung` (`may_chu` VARCHAR(40) NOT NULL, `phien_ban` BIGINT NOT NULL DEFAULT 0,"
                + " `ap_dung_luc` DATETIME NULL, `khoi_dong_luc` DATETIME NULL, `nhip_luc` DATETIME NULL, `so_khoa` INT NOT NULL DEFAULT 0,"
                + " `so_loi` INT NOT NULL DEFAULT 0, `loi` TEXT NULL, `cho_khoi_dong_lai` TEXT NULL, `thay_doi_cuoi` TEXT NULL,"
                + " PRIMARY KEY (`may_chu`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS `cau_hinh_hieu_luc` (`may_chu` VARCHAR(40) NOT NULL, `khoa` VARCHAR(64) NOT NULL,"
                + " `gia_tri` VARCHAR(255) NULL, `nguon` VARCHAR(12) NOT NULL DEFAULT '', `cho_khoi_dong_lai` VARCHAR(255) NULL,"
                + " `loi` VARCHAR(255) NULL, `cap_nhat_luc` DATETIME NULL, PRIMARY KEY (`may_chu`, `khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
            "CREATE TABLE IF NOT EXISTS `cau_hinh_khoi_dong_lai` (`id` TINYINT NOT NULL, `trang_thai` VARCHAR(12) NOT NULL DEFAULT '',"
                + " `giay` INT NOT NULL DEFAULT 60, `ly_do` VARCHAR(200) NOT NULL DEFAULT '', `yeu_cau_boi` VARCHAR(50) NULL,"
                + " `yeu_cau_luc` DATETIME NULL, `bat_dau_luc` DATETIME NULL, `thoat_luc` DATETIME NULL, `xong_luc` DATETIME NULL,"
                + " `may_chu` VARCHAR(40) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
        };
        final Statement st = c.createStatement();
        try {
            for (final String s : ddl) {
                st.execute(s);
            }
        } finally {
            st.close();
        }
        capNhat(c, "INSERT IGNORE INTO cau_hinh_phien_ban (id, phien_ban) VALUES (1, 1)");
        capNhat(c, "INSERT IGNORE INTO cau_hinh_khoi_dong_lai (id, trang_thai) VALUES (1, '')");
        // Lan khoi dong nay la ket qua cua mot yeu cau khoi dong lai -> danh dau xong.
        final Timestamp bay = new Timestamp(System.currentTimeMillis());
        capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'da_xong', xong_luc = ? WHERE id = 1 AND trang_thai = 'dang_thoat'", bay);
        capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'da_huy', xong_luc = ?, ly_do = ? WHERE id = 1"
                + " AND trang_thai IN ('dang_dem', 'huy')", bay, "(bị gián đoạn: máy chủ khởi động lại giữa lúc đếm)");
        // Yeu cau 'cho' cu hon 10 phut (ghi luc may chu dang tat): KHONG tu khoi dong lai ngay sau khi bat.
        capNhat(c, "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'da_huy', xong_luc = ?, ly_do = ? WHERE id = 1"
                + " AND trang_thai = 'cho' AND yeu_cau_luc < ?", bay, "(quá hạn: gửi lúc máy chủ không chạy)",
                new Timestamp(System.currentTimeMillis() - 600000L));
        daTaoBang = true;
    }

    static String docChuKy(final Connection c) throws SQLException {
        final long pb = docPhienBan(c);
        final Object[] r = docMot(c, "SELECT COUNT(*), MAX(sua_luc) FROM cau_hinh");
        return pb + "|" + (r == null ? "" : r[0] + "|" + r[1]);
    }

    static long docPhienBan(final Connection c) throws SQLException {
        final Object[] r = docMot(c, "SELECT phien_ban FROM cau_hinh_phien_ban WHERE id = 1");
        return r == null || r[0] == null ? 0L : ((Number) r[0]).longValue();
    }

    static String docTrangThaiKhoiDongLai(final Connection c) throws SQLException {
        final Object[] r = docMot(c, "SELECT trang_thai FROM cau_hinh_khoi_dong_lai WHERE id = 1");
        return r == null || r[0] == null ? "" : String.valueOf(r[0]);
    }

    static Map<String, String[]> docBang(final Connection c) throws SQLException {
        final Map<String, String[]> ra = new HashMap<String, String[]>();
        final Statement st = c.createStatement();
        try {
            final ResultSet rs = st.executeQuery("SELECT khoa, gia_tri, sua_boi FROM cau_hinh");
            while (rs.next()) {
                ra.put(rs.getString(1), new String[]{rs.getString(2) == null ? "" : rs.getString(2), rs.getString(3)});
            }
            rs.close();
        } finally {
            st.close();
        }
        return ra;
    }

    /** Dong xac nhan + gia tri hieu luc (chi dong doi, lan dau ghi het). */
    static void ghiXacNhan(final Connection c, final AnhChup moi, final TinhCauHinh.KetQua cu, final boolean lanDau)
            throws SQLException {
        final Timestamp bay = new Timestamp(moi.napLuc);
        final StringBuilder loi = new StringBuilder();
        for (final Map.Entry<String, String> e : moi.kq.loi.entrySet()) {
            loi.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        final StringBuilder cho = new StringBuilder();
        for (final Map.Entry<String, String> e : moi.kq.choKhoiDong.entrySet()) {
            cho.append(e.getKey()).append('=').append(an(THEO_KHOA.get(e.getKey()), e.getValue())).append('\n');
        }
        int soDb = 0;
        for (final String n : moi.kq.nguon.values()) {
            if (TinhCauHinh.NGUON_DB.equals(n)) {
                soDb++;
            }
        }
        capNhat(c, "INSERT IGNORE INTO cau_hinh_ap_dung (may_chu, phien_ban) VALUES (?, 0)", mayChu);
        capNhat(c, "UPDATE cau_hinh_ap_dung SET phien_ban = ?, ap_dung_luc = ?, khoi_dong_luc = ?, nhip_luc = ?, so_khoa = ?,"
                + " so_loi = ?, loi = ?, cho_khoi_dong_lai = ?, thay_doi_cuoi = ? WHERE may_chu = ?",
                Long.valueOf(moi.phienBan), bay, new Timestamp(khoiDongLuc), bay, Integer.valueOf(soDb),
                Integer.valueOf(moi.kq.loi.size()), cat(loi.toString(), 4000), cat(cho.toString(), 4000), cat(thayDoiCuoi, 2000), mayChu);
        lanNhipCuoi = System.currentTimeMillis();
        for (final DinhNghiaKhoa d : DM) {
            if (!d.trongCauHinh()) {
                continue;
            }
            final String k = d.khoa;
            final String v = moi.kq.hieuLuc.get(k);
            final String n = moi.kq.nguon.get(k);
            final String ch = moi.kq.choKhoiDong.get(k);
            final String l = moi.kq.loi.get(k);
            if (!lanDau && cu != null && eq(v, cu.hieuLuc.get(k)) && eq(n, cu.nguon.get(k))
                    && eq(ch, cu.choKhoiDong.get(k)) && eq(l, cu.loi.get(k))) {
                continue;
            }
            capNhat(c, "INSERT IGNORE INTO cau_hinh_hieu_luc (may_chu, khoa) VALUES (?, ?)", mayChu, k);
            capNhat(c, "UPDATE cau_hinh_hieu_luc SET gia_tri = ?, nguon = ?, cho_khoi_dong_lai = ?, loi = ?, cap_nhat_luc = ?"
                    + " WHERE may_chu = ? AND khoa = ?", cat(an(d, v), 255), n, ch == null ? null : cat(an(d, ch), 255),
                    l == null ? null : cat(l, 255), bay, mayChu, k);
        }
    }

    static void ghiNhip() {
        Connection c = null;
        try {
            c = layKetNoi();
            capNhat(c, "UPDATE cau_hinh_ap_dung SET nhip_luc = ? WHERE may_chu = ?", new Timestamp(System.currentTimeMillis()), mayChu);
            lanNhipCuoi = System.currentTimeMillis();
        } catch (final Exception e) {
            baoLoi("nhip", e);
        } finally {
            traKetNoi(c);
        }
    }

    static void ghiLichSu(final Connection c, final String khoa, final String bang, final String cu, final String moi,
            final String ai, final String nguon, final String lyDo, final long pb, final Timestamp luc) throws SQLException {
        final DinhNghiaKhoa d = THEO_KHOA.get(khoa);
        capNhat(c, "INSERT INTO cau_hinh_lich_su (khoa, bang, gia_tri_cu, gia_tri_moi, nguoi_sua, nguon, ly_do, phien_ban, luc)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", khoa, bang, cu == null ? null : cat(an(d, cu), 255),
                moi == null ? null : cat(an(d, moi), 255), cat(ai, 50), cat(nguon, 20), cat(lyDo == null ? "" : lyDo, 255),
                Long.valueOf(pb), luc);
    }

    /**
     * Thay doi do ben ngoai ghi thang vao cau_hinh (PHP cu, bang setting trong game, SQL tay) -> ghi lich su
     * nguon 'phat_hien'. Bo qua neu da co dong lich su cung khoa + gia tri trong 10 phut (do web / ghi() ghi).
     */
    static void ghiLichSuPhatHien(final Connection c, final Map<String, String[]> truoc, final Map<String, String[]> sau,
            final long pb, final AnhChup cu, final AnhChup moi) throws SQLException {
        final Set<String> khoa = new LinkedHashSet<String>(truoc.keySet());
        khoa.addAll(sau.keySet());
        final Timestamp bay = new Timestamp(System.currentTimeMillis());
        final Timestamp tu = new Timestamp(System.currentTimeMillis() - 600000L);
        for (final String k : khoa) {
            final String[] a = truoc.get(k);
            final String[] b = sau.get(k);
            final String va = a == null ? null : a[0];
            final String vb = b == null ? null : b[0];
            if (eq(va, vb)) {
                continue;
            }
            final DinhNghiaKhoa d = THEO_KHOA.get(k);
            if (d == null && (k.startsWith("lau_dai.") || k.startsWith("web."))) {
                continue; // trang thai game / web tu ghi, khong phai cau hinh
            }
            if (d != null && (va == null || vb == null) && cu != null && eq(cu.chuoi(k), moi.chuoi(k))) {
                continue; // them / xoa dong dung bang gia tri dang chay (vd trang Thanh vien ghi lai du 7 khoa)
            }
            final Object[] r = vb == null
                    ? docMot(c, "SELECT id FROM cau_hinh_lich_su WHERE khoa = ? AND gia_tri_moi IS NULL AND luc >= ? LIMIT 1", k, tu)
                    : docMot(c, "SELECT id FROM cau_hinh_lich_su WHERE khoa = ? AND gia_tri_moi = ? AND luc >= ? LIMIT 1", k,
                            cat(an(d, vb), 255), tu);
            if (r != null) {
                continue;
            }
            ghiLichSu(c, k, "cau_hinh", va, vb, b != null && b[1] != null ? b[1] : "?", "phat_hien",
                    "ghi thẳng vào bảng cau_hinh (không qua trang Cấu hình hệ thống)", pb, bay);
        }
    }

    // =================================================================== TIEN ICH

    /** Gia tri hien thi / luu lich su: khoa nhay cam -> "(ẩn)". */
    static String an(final DinhNghiaKhoa d, final String v) {
        if (v == null) {
            return null;
        }
        return d != null && d.laNhay() && !v.isEmpty() ? "(ẩn)" : v;
    }

    static boolean eq(final String a, final String b) {
        return a == null ? b == null : a.equals(b);
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return null;
        }
        return s.length() > n ? s.substring(0, n) : s;
    }

    static Connection layKetNoi() throws SQLException {
        return data.Database.connPool.getConFromPool();
    }

    static void traKetNoi(final Connection c) {
        if (c == null) {
            return;
        }
        try {
            data.Database.connPool.free(c);
        } catch (final Exception ignored) {
        }
    }

    static int capNhat(final Connection c, final String sql, final Object... thamSo) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(sql);
        try {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);
            }
            return ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    static Object[] docMot(final Connection c, final String sql, final Object... thamSo) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(sql);
        try {
            for (int i = 0; i < thamSo.length; i++) {
                ps.setObject(i + 1, thamSo[i]);
            }
            final ResultSet rs = ps.executeQuery();
            try {
                if (!rs.next()) {
                    return null;
                }
                final int n = rs.getMetaData().getColumnCount();
                final Object[] ra = new Object[n];
                for (int i = 0; i < n; i++) {
                    ra[i] = rs.getObject(i + 1);
                }
                return ra;
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    static synchronized void baoLoi(final String viec, final Throwable e) {
        final long bay = System.currentTimeMillis();
        if (bay - lanBaoLoi < 60000L) {
            return;
        }
        lanBaoLoi = bay;
        System.out.println("[CAU HINH] loi " + viec + ": " + e);
    }

    // ---- chi cho test ----

    public static void datChoTest(final Map<String, String> iniTest, final Map<String, String> dbTest, final boolean thuNghiem) {
        ini = iniTest == null ? Collections.<String, String>emptyMap() : iniTest;
        choPhepThuNghiem = thuNghiem;
        hienTai = new AnhChup(TinhCauHinh.tinh(DM, ini, dbTest, null, thuNghiem), 1L, System.currentTimeMillis());
    }

    /** Mo phong mot lan nap lai (khong CSDL) - test hot reload + nghe. Tra tap khoa doi. */
    public static Set<String> napLaiChoTest(final Map<String, String> dbTest, final long pb) {
        final AnhChup cu = hienTai;
        final TinhCauHinh.KetQua kq = TinhCauHinh.tinh(DM, ini, dbTest, cu == null ? null : cu.kq, choPhepThuNghiem);
        final AnhChup moi = new AnhChup(kq, pb, System.currentTimeMillis());
        final Set<String> doi = cu == null ? new TreeSet<String>(kq.tatCa.keySet()) : khacNhau(cu.kq, kq);
        hienTai = moi;
        baoNghe(doi, moi);
        return doi;
    }

    public static void xoaNgheChoTest() {
        NGHE.clear();
    }
}
