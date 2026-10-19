package com.htbb.cauhinh;

import java.util.regex.Pattern;

/**
 * Dinh nghia MOT khoa cau hinh (sinh tu tools/cauhinh/danh_muc.json -> {@link DanhMucSinh}).
 * Bat bien sau khi dung xong (cac ham dat chi goi trong DanhMucSinh / test).
 */
public final class DinhNghiaKhoa {

    public final String khoa;
    public final String nhom;
    /** co | so | chon | chuoi | ds_chuoi | ds_so | ngay | ngay_gio | nhip */
    public final String kieu;
    public final String macDinh;
    /** true = ap dung trong <= 10 giay; false = chi doi khi may chu khoi dong lai. */
    public final boolean apDungNgay;
    /** cau_hinh | nap_cau_hinh | env. May chu game chi doc khoa nam trong bang cau_hinh. */
    public final String bang;

    private String ten = "";
    private String moTa = "";
    private long min = Long.MIN_VALUE;
    private long max = Long.MAX_VALUE;
    private long buoc = 0L;
    private String[] luaChon = null;
    private int soPhanTu = 0;
    private Pattern mau = null;
    private String mauChu = null;
    private int doDai = 255;
    private boolean hoa = false;
    private boolean nhay = false;
    private boolean khoaProd = false;
    private boolean chiDoc = false;
    /** chinh | co_dinh | luon_bat (docs/cau-hinh.md, truong "lop" trong danh_muc.json). */
    private String lop = LOP_CHINH;

    public static final String LOP_CHINH = "chinh";
    public static final String LOP_CO_DINH = "co_dinh";
    public static final String LOP_LUON_BAT = "luon_bat";

    public DinhNghiaKhoa(final String khoa, final String nhom, final String kieu, final String macDinh,
            final boolean apDungNgay, final String bang) {
        this.khoa = khoa;
        this.nhom = nhom;
        this.kieu = kieu;
        this.macDinh = macDinh == null ? "" : macDinh;
        this.apDungNgay = apDungNgay;
        this.bang = bang;
    }

    DinhNghiaKhoa nhan(final String t, final String m) {
        this.ten = t;
        this.moTa = m;
        return this;
    }

    DinhNghiaKhoa khoang(final long nho, final long lon) {
        this.min = nho;
        this.max = lon;
        return this;
    }

    DinhNghiaKhoa buoc(final long b) {
        this.buoc = b;
        return this;
    }

    DinhNghiaKhoa luaChon(final String[] ds) {
        this.luaChon = ds;
        return this;
    }

    DinhNghiaKhoa soPhanTu(final int n) {
        this.soPhanTu = n;
        return this;
    }

    DinhNghiaKhoa mau(final String regex) {
        this.mauChu = regex;
        this.mau = Pattern.compile(regex);
        return this;
    }

    DinhNghiaKhoa doDai(final int n) {
        this.doDai = n;
        return this;
    }

    DinhNghiaKhoa hoa() {
        this.hoa = true;
        return this;
    }

    DinhNghiaKhoa nhay() {
        this.nhay = true;
        return this;
    }

    DinhNghiaKhoa khoaProd() {
        this.khoaProd = true;
        return this;
    }

    DinhNghiaKhoa chiDoc() {
        this.chiDoc = true;
        return this;
    }

    DinhNghiaKhoa lop(final String l) {
        this.lop = l;
        return this;
    }

    public String ten() {
        return ten;
    }

    public String moTa() {
        return moTa;
    }

    public long min() {
        return min;
    }

    public long max() {
        return max;
    }

    public long buoc() {
        return buoc;
    }

    public String[] luaChon() {
        return luaChon == null ? new String[0] : luaChon.clone();
    }

    boolean coLuaChon() {
        return luaChon != null && luaChon.length > 0;
    }

    boolean trongLuaChon(final String v) {
        if (luaChon == null) {
            return true;
        }
        for (final String x : luaChon) {
            if (x.equals(v)) {
                return true;
            }
        }
        return false;
    }

    public int soPhanTu() {
        return soPhanTu;
    }

    Pattern mauRegex() {
        return mau;
    }

    public String mauChu() {
        return mauChu;
    }

    public int doDai() {
        return doDai;
    }

    public boolean chuHoa() {
        return hoa;
    }

    /** Gia tri nhay cam: an trong bang hieu_luc / nhat ky. */
    public boolean laNhay() {
        return nhay;
    }

    /** Khoa thu nghiem: may that (khong co sv.cho_phep_thu_nghiem=1) chi nhan mac dinh tu CSDL. */
    public boolean laKhoaProd() {
        return khoaProd;
    }

    /** Khong sua duoc tu web / Swing (vd bien moi truong). */
    public boolean laChiDoc() {
        return chiDoc;
    }

    /** chinh | co_dinh | luon_bat. */
    public String lop() {
        return lop;
    }

    /**
     * Khoa AN (co dinh / luon bat): khong hien tren trang quan tri; may that (khong co sv.cho_phep_thu_nghiem=1)
     * BO QUA dong CSDL, chi nhan server.ini (khan cap) roi gia tri co dinh {@link #macDinh}.
     */
    public boolean laAn() {
        return !LOP_CHINH.equals(lop);
    }

    /** Khoa nam trong bang cau_hinh (may chu game doc + nap lai). */
    public boolean trongCauHinh() {
        return "cau_hinh".equals(bang);
    }
}
