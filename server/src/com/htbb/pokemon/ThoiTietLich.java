package com.htbb.pokemon;

/**
 * LICH THOI TIET THEO KHUNG GIO + PHU MAU MUA (de xuat 8, chu game duyet 19/10/2026). Lop THUAN, tat dinh: cung (ngay,
 * khung, vung, thang, su kien dang bat) = cung ket qua tren moi may, khong can luu. test/KiemThoiTiet.
 *
 * <h3>Lich</h3>
 * Moi ngay 4 khung 6 gio ({@link #GIO_KHUNG}: 00-06, 06-12, 12-18, 18-24). Khung dau ngay quay theo trong so vung + mua
 * ({@link SinhThaiTheGioi#trongSoThoiTiet} + cot gio {@link #TRONG_SO_GIO}) + su kien dang bat (bang
 * data_thoi_tiet_su_kien, vd Halloween them suong mu); moi khung sau giu nguyen khung truoc {@link #GIU_PHAN_TRAM}% (troi
 * khong nhay loan xa), con lai quay lai. Hang / trong nha / dau truong luon quang. Ban do co the CO DINH mot thoi tiet
 * (data_thoi_tiet_map).
 *
 * <h3>Anh huong (nho, co tran)</h3>
 * Bang data_thoi_tiet (mac dinh {@link #MAC_DINH}, khop SinhThaiTheGioi): moi thoi tiet tang / giam % sat thuong chieu
 * cua vai he; {@link #satThuong} KEP |thay doi| &lt;= {@link #TRAN_SAT_THUONG_PHAN_TRAM} du CSDL ghi gi. He hop thoi tiet sinh
 * nhieu hon (x {@link HeSinhThai#HE_SO_SINH_HOP_THOI_TIET}, kep &lt;= {@link #TRAN_HE_SO_SINH}).
 *
 * <h3>Phu mau mua</h3>
 * {@link #mauMua}: su kien dang bat co mau rieng (Halloween hoang hon, Tet, Noel, Trung Thu - uu tien cao nhat thang)
 * hoac mua that (Xuan hoa anh dao 2-4, Ha 5-7, Thu la phong 8-10, Dong tuyet lanh 11-1). Hang / trong nha: khong phu.
 * Client ve lop phu alpha thap (game.pokemon.ThoiTietVe), nguoi choi tat duoc.
 */
public final class ThoiTietLich {

    private ThoiTietLich() {
    }

    // ------------------------------------------------------------------ khung gio

    public static final int SO_KHUNG = 4;
    public static final int GIO_KHUNG = 6;
    /** % khung sau giu nguyen thoi tiet khung truoc. */
    public static final int GIU_PHAN_TRAM = 55;
    /** Tran cung |% sat thuong| moi thoi tiet (du bang CSDL ghi gi). */
    public static final int TRAN_SAT_THUONG_PHAN_TRAM = 10;
    /** Tran cung he so sinh cua loai hop thoi tiet. */
    public static final int TRAN_HE_SO_SINH = 3;

    /** Khung (0..3) cua gio 0..23. */
    public static int khung(final int gio) {
        final int g = ((gio % 24) + 24) % 24;
        return g / GIO_KHUNG;
    }

    /** Gio bat dau khung. */
    public static int gioBatDau(final int khung) {
        return khung * GIO_KHUNG;
    }

    /**
     * Trong so cot gio (gio manh) theo vung (cung thu tu SinhThaiTheGioi.MA_VUNG). Sa mac da co bao cat; hang / trong nha /
     * dau truong khong co thoi tiet.
     */
    static final int[] TRONG_SO_GIO = {5, 8, 12, 4, 8, 15, 15, 6, 0, 0, 0, 5, 0, 12};

    /**
     * Trong so 7 thoi tiet (QUANG..GIO) cua vung trong thang, cong them {@code cong} (mang 7 phan tu hoac null: thien huong
     * cua su kien dang bat). Vung khong thoi tiet (quang 100%) bo qua moi thien huong.
     */
    public static int[] trongSo(final int vung, final int thang, final int[] cong) {
        final int[] goc = SinhThaiTheGioi.trongSoThoiTiet(vung, thang);
        final int[] w = new int[SinhThaiTheGioi.SO_THOI_TIET];
        System.arraycopy(goc, 0, w, 0, goc.length);
        if (goc[SinhThaiTheGioi.QUANG] == 100) {
            return w;
        }
        final int v = vung >= 0 && vung < TRONG_SO_GIO.length ? vung : SinhThaiTheGioi.V_DUONG;
        int gio = TRONG_SO_GIO[v];
        final int mua = SinhThaiTheGioi.mua(thang);
        if (gio > 0 && (mua == SinhThaiTheGioi.XUAN || mua == SinhThaiTheGioi.THU)) {
            gio += 5; // giao mua nhieu gio
        }
        w[SinhThaiTheGioi.GIO] = gio;
        if (cong != null) {
            for (int i = 0; i < w.length && i < cong.length; i++) {
                w[i] = Math.max(0, w[i] + cong[i]);
            }
            // vung khong co tuyet / bao cat tu nhien thi su kien cung khong tao ra (khong tuyet o sa mac...)
            if (goc[SinhThaiTheGioi.TUYET] == 0 && thang >= 3 && thang <= 10) {
                w[SinhThaiTheGioi.TUYET] = 0;
            }
            if (goc[SinhThaiTheGioi.BAO_CAT] == 0) {
                w[SinhThaiTheGioi.BAO_CAT] = 0;
            }
        }
        return w;
    }

    static int quay(final int[] w, final long hat) {
        int tong = 0;
        for (final int x : w) {
            tong += x;
        }
        if (tong <= 0) {
            return SinhThaiTheGioi.QUANG;
        }
        int r = (int) Long.remainderUnsigned(SinhThaiTheGioi.bam(hat), tong);
        for (int i = 0; i < w.length; i++) {
            r -= w[i];
            if (r < 0) {
                return i;
            }
        }
        return SinhThaiTheGioi.QUANG;
    }

    /**
     * Thoi tiet cua vung o khung {@code khung} ngay {@code ngay} (yyyyMMdd). Tat dinh; khung k suy ra tu khung 0..k-1
     * cung ngay (giu {@link #GIU_PHAN_TRAM}%).
     */
    public static int thoiTiet(final int ngay, final int khung, final int vung, final int[] cong) {
        final int thang = (ngay / 100) % 100;
        final int[] w = trongSo(vung, thang, cong);
        if (w[SinhThaiTheGioi.QUANG] >= 100 && tongKhac(w) == 0) {
            return SinhThaiTheGioi.QUANG;
        }
        final long goc = ngay * 131L + vung * 7919L;
        int tt = quay(w, goc + 23L);
        final int k = Math.max(0, Math.min(SO_KHUNG - 1, khung));
        for (int i = 1; i <= k; i++) {
            final int giu = (int) Long.remainderUnsigned(SinhThaiTheGioi.bam(goc + i * 104729L), 100);
            if (giu >= GIU_PHAN_TRAM) {
                tt = quay(w, goc + i * 15485863L + 7L);
            }
        }
        return tt;
    }

    private static int tongKhac(final int[] w) {
        int t = 0;
        for (int i = 1; i < w.length; i++) {
            t += w[i];
        }
        return t;
    }

    /** Ngay ke tiep (yyyyMMdd) - lich Gregory, khong can Calendar. */
    public static int ngaySau(final int ngay) {
        int y = ngay / 10000, m = (ngay / 100) % 100, d = ngay % 100;
        final int[] dai = {31, (y % 4 == 0 && y % 100 != 0) || y % 400 == 0 ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        d++;
        if (d > dai[Math.max(0, Math.min(11, m - 1))]) {
            d = 1;
            m++;
            if (m > 12) {
                m = 1;
                y++;
            }
        }
        return y * 10000 + m * 100 + d;
    }

    /** Du bao n khung ke tiep (khong gom khung hien tai): {thoi tiet...}. */
    public static int[] duBao(final int ngay, final int khung, final int vung, final int[] cong, final int n) {
        final int[] r = new int[Math.max(0, n)];
        int d = ngay, k = khung;
        for (int i = 0; i < r.length; i++) {
            k++;
            if (k >= SO_KHUNG) {
                k = 0;
                d = ngaySau(d);
            }
            r[i] = thoiTiet(d, k, vung, cong);
        }
        return r;
    }

    // ------------------------------------------------------------------ anh huong

    /** Mot dong data_thoi_tiet: he duoc tang / giam sat thuong, he sinh nhieu, %. */
    public static final class AnhHuong {
        public final int thoiTiet;
        public final int[] heTang;
        public final int[] heGiam;
        public final int[] heSinh;
        public final int phanTram;
        public final String goiY;

        public AnhHuong(final int thoiTiet, final int[] heTang, final int[] heGiam, final int[] heSinh, final int phanTram,
                final String goiY) {
            this.thoiTiet = thoiTiet;
            this.heTang = heTang == null ? new int[0] : heTang;
            this.heGiam = heGiam == null ? new int[0] : heGiam;
            this.heSinh = heSinh == null ? new int[0] : heSinh;
            this.phanTram = Math.max(0, Math.min(TRAN_SAT_THUONG_PHAN_TRAM, phanTram));
            this.goiY = goiY == null ? "" : goiY;
        }

        static boolean co(final int[] a, final int he) {
            for (final int x : a) {
                if (x == he) {
                    return true;
                }
            }
            return false;
        }
    }

    private static int[] h(final int... he) {
        return he;
    }

    /** Mac dinh (khop SinhThaiTheGioi + sql/them_thoi_tiet_mua.sql). */
    public static final AnhHuong[] MAC_DINH = {
        new AnhHuong(SinhThaiTheGioi.QUANG, h(), h(), h(), 0, "Trời đẹp, không ảnh hưởng chiêu."),
        new AnhHuong(SinhThaiTheGioi.NANG, h(HePokemon.LUA), h(HePokemon.NUOC), h(HePokemon.LUA, HePokemon.CO), 10,
                "Pokémon hệ Lửa, Cỏ xuất hiện nhiều."),
        new AnhHuong(SinhThaiTheGioi.MUA, h(HePokemon.NUOC), h(HePokemon.LUA), h(HePokemon.NUOC, HePokemon.DIEN), 10,
                "Pokémon hệ Nước, Điện xuất hiện nhiều."),
        new AnhHuong(SinhThaiTheGioi.TUYET, h(HePokemon.BANG), h(), h(HePokemon.BANG), 10, "Pokémon hệ Băng xuất hiện nhiều."),
        new AnhHuong(SinhThaiTheGioi.BAO_CAT, h(HePokemon.DA), h(), h(HePokemon.DA, HePokemon.DAT, HePokemon.THEP), 10,
                "Pokémon hệ Đá, Đất, Thép xuất hiện nhiều."),
        new AnhHuong(SinhThaiTheGioi.SUONG_MU, h(HePokemon.MA), h(), h(HePokemon.MA, HePokemon.SIEU_LINH), 10,
                "Pokémon hệ Ma, Siêu linh xuất hiện nhiều."),
        new AnhHuong(SinhThaiTheGioi.GIO, h(HePokemon.BAY), h(), h(HePokemon.BAY, HePokemon.RONG), 10,
                "Pokémon hệ Bay, Rồng xuất hiện nhiều."),
    };

    /** Bang dang dung (ThoiTietGame nap tu CSDL; thieu dong = mac dinh). */
    static volatile AnhHuong[] bang = MAC_DINH;

    /** Chi ThoiTietGame / test. Dong thieu giu mac dinh; % luon kep tran. */
    public static void datBang(final AnhHuong[] moi) {
        final AnhHuong[] b = MAC_DINH.clone();
        if (moi != null) {
            for (final AnhHuong a : moi) {
                if (a != null && a.thoiTiet >= 0 && a.thoiTiet < b.length) {
                    b[a.thoiTiet] = a;
                }
            }
        }
        bang = b;
    }

    public static AnhHuong anhHuong(final int tt) {
        final AnhHuong[] b = bang;
        return tt >= 0 && tt < b.length ? b[tt] : b[0];
    }

    /** % sat thuong cua chieu he {@code he} trong thoi tiet {@code tt}: 100 +- p, |p| &lt;= TRAN. */
    public static int satThuong(final int he, final int tt) {
        final AnhHuong a = anhHuong(tt);
        int p = 0;
        if (AnhHuong.co(a.heTang, he)) {
            p += a.phanTram;
        }
        if (AnhHuong.co(a.heGiam, he)) {
            p -= a.phanTram;
        }
        p = Math.max(-TRAN_SAT_THUONG_PHAN_TRAM, Math.min(TRAN_SAT_THUONG_PHAN_TRAM, p));
        return 100 + p;
    }

    /** He {@code he} sinh nhieu hon trong thoi tiet {@code tt}. */
    public static boolean heHop(final int he, final int tt) {
        return AnhHuong.co(anhHuong(tt).heSinh, he);
    }

    /** He so sinh (x lan) cua loai hai he trong thoi tiet, kep tran. */
    public static int heSoSinh(final int he1, final int he2, final int tt) {
        return heHop(he1, tt) || heHop(he2, tt) ? Math.min(TRAN_HE_SO_SINH, HeSinhThai.HE_SO_SINH_HOP_THOI_TIET) : 1;
    }

    /**
     * Dong goi y cho HUD / tooltip: "Mưa: hệ Nước +10%, hệ Lửa -10%. Pokémon hệ Nước, Điện xuất hiện nhiều." (chu co dau).
     */
    public static String goiY(final int tt) {
        final AnhHuong a = anhHuong(tt);
        final StringBuilder b = new StringBuilder(SinhThaiTheGioi.tenThoiTiet(tt));
        if (a.phanTram > 0 && (a.heTang.length > 0 || a.heGiam.length > 0)) {
            b.append(": ");
            boolean dau = true;
            for (final int he : a.heTang) {
                b.append(dau ? "" : ", ").append("chiêu hệ ").append(HePokemon.ten(he)).append(" +").append(a.phanTram).append('%');
                dau = false;
            }
            for (final int he : a.heGiam) {
                b.append(dau ? "" : ", ").append("chiêu hệ ").append(HePokemon.ten(he)).append(" −").append(a.phanTram).append('%');
                dau = false;
            }
            b.append('.');
        } else {
            b.append('.');
        }
        if (a.goiY.length() > 0) {
            b.append(' ').append(a.goiY);
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ phu mau mua

    public static final int MAU_KHONG = 0, MAU_XUAN = 1, MAU_HA = 2, MAU_THU = 3, MAU_DONG = 4, MAU_HALLOWEEN = 5,
            MAU_TET = 6, MAU_NOEL = 7, MAU_TRUNG_THU = 8;
    public static final int SO_MAU = 9;
    public static final String[] TEN_MAU = {"", "Xuân hoa anh đào", "Hạ nắng vàng", "Thu lá phong", "Đông tuyết lạnh",
        "Hoàng hôn Halloween", "Sắc Tết", "Đêm Noel", "Đèn Trung Thu"};

    /** Mau theo mua that (thang 1..12). */
    public static int mauMuaThat(final int thang) {
        switch (SinhThaiTheGioi.mua(thang)) {
            case SinhThaiTheGioi.XUAN:
                return MAU_XUAN;
            case SinhThaiTheGioi.HA:
                return MAU_HA;
            case SinhThaiTheGioi.THU:
                return MAU_THU;
            default:
                return MAU_DONG;
        }
    }

    /** Vung co phu mau (ngoai troi). */
    public static boolean coPhuMau(final int vung) {
        return vung != SinhThaiTheGioi.V_HANG && vung != SinhThaiTheGioi.V_TRONG_NHA && vung != SinhThaiTheGioi.V_DAU_TRUONG;
    }

    /**
     * Mau phu cua vung: {@code mauSuKien} (mau cua su kien dang bat uu tien cao nhat, MAU_KHONG neu khong co) thang
     * mua that. Vung trong nha / hang: MAU_KHONG.
     */
    public static int mauMua(final int vung, final int thang, final int mauSuKien) {
        if (!coPhuMau(vung)) {
            return MAU_KHONG;
        }
        if (mauSuKien > MAU_KHONG && mauSuKien < SO_MAU) {
            return mauSuKien;
        }
        return mauMuaThat(thang);
    }

    // ------------------------------------------------------------------ thien huong su kien

    /** Mot dong data_thoi_tiet_su_kien: khi {@code maSuKien} dang bat, vung (-1 moi vung ngoai troi) them trong so. */
    public static final class SuKienThoiTiet {
        public final String maSuKien;
        public final int vung;
        public final int thoiTiet;
        public final int trongSo;
        public final int mauMua;
        public final int uuTien;

        public SuKienThoiTiet(final String maSuKien, final int vung, final int thoiTiet, final int trongSo, final int mauMua,
                final int uuTien) {
            this.maSuKien = maSuKien == null ? "" : maSuKien;
            this.vung = vung;
            this.thoiTiet = thoiTiet;
            this.trongSo = Math.max(0, Math.min(TRAN_TRONG_SO_SU_KIEN, trongSo));
            this.mauMua = mauMua;
            this.uuTien = uuTien;
        }
    }

    /** Tran trong so them cua mot dong su kien (khong de su kien bien ca vung thanh mot thoi tiet). */
    public static final int TRAN_TRONG_SO_SU_KIEN = 60;

    /** Mac dinh (khop sql/them_thoi_tiet_mua.sql). */
    public static final SuKienThoiTiet[] SU_KIEN_MAC_DINH = {
        new SuKienThoiTiet("halloween", -1, SinhThaiTheGioi.SUONG_MU, 30, MAU_HALLOWEEN, 50),
        new SuKienThoiTiet("noel", -1, SinhThaiTheGioi.TUYET, 25, MAU_NOEL, 50),
        new SuKienThoiTiet("tet2017", -1, SinhThaiTheGioi.QUANG, 20, MAU_TET, 60),
        new SuKienThoiTiet("trungthu", -1, SinhThaiTheGioi.QUANG, 20, MAU_TRUNG_THU, 40),
        new SuKienThoiTiet("he2017", -1, SinhThaiTheGioi.NANG, 15, MAU_KHONG, 10),
    };

    /** Cong trong so (mang 7) cua cac dong su kien dang bat ap cho vung; null neu khong co. */
    public static int[] congSuKien(final SuKienThoiTiet[] ds, final java.util.Set<String> dangBat, final int vung) {
        int[] c = null;
        if (ds == null || dangBat == null || dangBat.isEmpty()) {
            return null;
        }
        for (final SuKienThoiTiet s : ds) {
            if (s == null || s.trongSo <= 0 || s.thoiTiet < 0 || s.thoiTiet >= SinhThaiTheGioi.SO_THOI_TIET
                    || !dangBat.contains(s.maSuKien) || s.vung >= 0 && s.vung != vung) {
                continue;
            }
            if (c == null) {
                c = new int[SinhThaiTheGioi.SO_THOI_TIET];
            }
            c[s.thoiTiet] += s.trongSo;
        }
        return c;
    }

    /** Mau cua su kien dang bat uu tien cao nhat (MAU_KHONG neu khong co). */
    public static int mauSuKien(final SuKienThoiTiet[] ds, final java.util.Set<String> dangBat) {
        int mau = MAU_KHONG, uu = Integer.MIN_VALUE;
        if (ds == null || dangBat == null) {
            return MAU_KHONG;
        }
        for (final SuKienThoiTiet s : ds) {
            if (s != null && s.mauMua > MAU_KHONG && s.mauMua < SO_MAU && dangBat.contains(s.maSuKien) && s.uuTien > uu) {
                uu = s.uuTien;
                mau = s.mauMua;
            }
        }
        return mau;
    }
}
