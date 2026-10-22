package com.htbb.pokemon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DAU TRUONG BAO BOI (he_thong_day_du.md 3.6, D12) - du lieu chieu cua che do: danh muc chieu dung chung
 * (data_chieu) + 4 chieu moi loai (data_loai_chieu), cung bang loai (LoaiDuLieu: he, 6 chi so nen).
 *
 * <p>Uy luc / chinh xac / PP trong Dau truong <b>chuan hoa theo vai tro</b> (he_thong_day_du.md 4.2: "Trong Dau truong
 * thi dung uy luc chuan hoa theo vai tro, khong dung so goc cua chinh ban"): {@link #UY_LUC}, {@link #CHINH_XAC},
 * {@link #PP}. He, loai (vat ly / dac biet / ho tro), trang thai phu va ti le (phan nghin) lay tu data_chieu.
 * Lop THUAN (test/KiemDauTruong).
 */
public final class DauTruongDuLieu {

    /** Vai tro chieu (data_chieu.vai_tro). */
    public static final String[] VAI = {"thuong", "tan_cong", "nhieu_don", "xa", "vung", "toi_thuong", "dac_trung",
        "khong_che", "buff", "khien"};
    public static final int V_THUONG = 0, V_TAN_CONG = 1, V_NHIEU_DON = 2, V_XA = 3, V_VUNG = 4, V_TOI_THUONG = 5,
            V_DAC_TRUNG = 6, V_KHONG_CHE = 7, V_BUFF = 8, V_KHIEN = 9;
    /** Uy luc moi don theo vai tro (nhieu don: moi don, so don tu data_chieu.so_don). */
    public static final int[] UY_LUC = {50, 85, 20, 80, 75, 120, 100, 0, 0, 0};
    /** Chinh xac (%) theo vai tro. */
    public static final int[] CHINH_XAC = {100, 95, 90, 100, 100, 90, 95, 85, 100, 100};
    /** PP trong mot tran theo vai tro. */
    public static final int[] PP = {30, 15, 15, 15, 15, 5, 10, 10, 10, 5};

    public static final int LOAI_VAT_LY = 0, LOAI_DAC_BIET = 1, LOAI_HO_TRO = 2;

    /** Mot chieu (da chuan hoa cho Dau truong). */
    public static final class Chieu {
        public final String ma, ten;
        public final int he, loai, vai, soDon;
        /** Trang thai phu (data_chieu.trang_thai, "" = khong) va ti le (phan nghin) khi trung. */
        public final String trangThai;
        public final int tiLeTrangThai;
        public final int uyLuc, chinhXac, ppToiDa;

        public Chieu(final String ma, final String ten, final int he, final String kieu, final String vaiTro, final int soDon,
                final String trangThai, final int tiLePhanNghin) {
            this.ma = ma == null ? "" : ma;
            this.ten = ten == null || ten.isEmpty() ? this.ma : ten;
            this.he = HePokemon.hopLe(he) ? he : HePokemon.THUONG;
            int v = 0;
            for (int i = 0; i < VAI.length; i++) {
                if (VAI[i].equals(vaiTro)) {
                    v = i;
                }
            }
            this.vai = v;
            final boolean hoTro = v == V_KHONG_CHE || v == V_BUFF || v == V_KHIEN;
            this.loai = hoTro ? LOAI_HO_TRO : "dac_biet".equals(kieu) ? LOAI_DAC_BIET : LOAI_VAT_LY;
            this.soDon = v == V_NHIEU_DON ? Math.max(2, Math.min(5, soDon)) : 1;
            this.trangThai = trangThai == null ? "" : trangThai.trim();
            // khong che: trung la gay trang thai (ti le 1000); chieu danh: ti le tu data (0..1000)
            this.tiLeTrangThai = this.trangThai.isEmpty() ? 0 : v == V_KHONG_CHE ? 1000 : Math.max(0, Math.min(1000, tiLePhanNghin));
            this.uyLuc = UY_LUC[v];
            this.chinhXac = CHINH_XAC[v];
            this.ppToiDa = PP[v];
        }

        public boolean danh() {
            return this.loai != LOAI_HO_TRO;
        }
    }

    /** Chieu du phong (thieu du lieu): "Húc" he Thuong. */
    public static final Chieu MAC_DINH = new Chieu("normal.thuong", "Húc", HePokemon.THUONG, "vat_ly", "thuong", 1, "", 0);
    /** Het PP ca 4 chieu: "Vùng vẫy" (khong he, 50, giat lui 1/4 HP toi da). */
    public static final Chieu VUNG_VAY = new Chieu("dt.vung_vay", "Vùng vẫy", HePokemon.THUONG, "vat_ly", "thuong", 1, "", 0);

    private final Map<String, Chieu> chieu;
    private final Map<Integer, String[]> chieuLoai;
    private final LoaiDuLieu loai;

    public DauTruongDuLieu(final LoaiDuLieu loai, final Map<String, Chieu> chieu, final Map<Integer, String[]> chieuLoai) {
        this.loai = loai == null ? LoaiDuLieu.hien() : loai;
        this.chieu = chieu == null ? new HashMap<String, Chieu>() : chieu;
        this.chieuLoai = chieuLoai == null ? new HashMap<Integer, String[]>() : chieuLoai;
    }

    private static volatile DauTruongDuLieu hien = new DauTruongDuLieu(null, null, null);

    public static DauTruongDuLieu hien() {
        return hien;
    }

    public static void dat(final DauTruongDuLieu d) {
        if (d != null) {
            hien = d;
        }
    }

    public boolean rong() {
        return this.chieuLoai.isEmpty() || this.loai.rong();
    }

    public LoaiDuLieu loai() {
        return this.loai;
    }

    public Map<String, Chieu> chieu() {
        return Collections.unmodifiableMap(this.chieu);
    }

    public Chieu chieu(final String ma) {
        final Chieu c = ma == null ? null : this.chieu.get(ma);
        return c == null ? MAC_DINH : c;
    }

    /** 4 chieu cua loai {@code dex} theo o 0..3 (thieu thi "Húc"). */
    public Chieu[] bonChieu(final int dex) {
        final String[] m = this.chieuLoai.get(Integer.valueOf(dex));
        final Chieu[] kq = new Chieu[4];
        for (int i = 0; i < 4; i++) {
            kq[i] = m != null && i < m.length ? chieu(m[i]) : MAC_DINH;
        }
        return kq;
    }

    /** 6 chi so nen cua loai (thieu = mac dinh CaThePokemon). */
    public int[] nen(final int dex) {
        final LoaiDuLieu.Loai l = this.loai.lay(dex);
        return l == null ? CaThePokemon.GOC_MAC_DINH : l.chiSo;
    }

    public int he1(final int dex) {
        final LoaiDuLieu.Loai l = this.loai.lay(dex);
        return l == null ? LoaiPokemon.he1(dex) : l.he1;
    }

    public int he2(final int dex) {
        final LoaiDuLieu.Loai l = this.loai.lay(dex);
        return l == null ? LoaiPokemon.he2(dex) : l.he2;
    }

    public int bac(final int dex) {
        return this.loai.bac(dex);
    }

    public String ten(final int dex) {
        final String t = this.loai.ten(dex);
        return t == null || t.isEmpty() ? LoaiPokemon.ten(dex) : t;
    }

    /** Suc manh cap 50 (tong 6 chi so, cong thuc chinh ban) - dung cho dai ghep doi. */
    public int sucManh50(final int dex, final int iv, final int tinhCach) {
        return CaThePokemon.sucManh(nen(dex), iv, tinhCach, DauTruongTran.CAP);
    }
}
