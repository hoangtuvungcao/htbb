package com.htbb.pokemon;

import java.util.Map;
import java.util.TreeMap;

/**
 * NGOC HE (18 he x cap 1..10) khảm vao lo trang bi - thiet ke can bang 4.4 "Ngoc he, khảm kieu Pokemon". Lop THUAN
 * (test/KiemBoNgocCheTao); phan game o real.BoNgocGame.
 *
 * <ul>
 * <li><b>So lo</b> theo bac hiem cua mon (data_ngoc_he_lo theo colorName: Thuong 1, Hiem 1, Quy 2, Su thi 2, Huyen
 *     thoai 3) - mon che tao "Huyen thoai" mang them lo rieng ({@link Kham#loThem}). Toi da {@link #LO_TOI_DA}.</li>
 * <li><b>Loi</b>: ngoc he A cap n = +chi_so_phan_tram(n)% chi so chinh cua MON (bang data_noi_dung_ngoc_he, = n) va
 *     +n x 1,5 phan nghin sat thuong he A (cong vao ST he, tran chung 20% - ChiSoTrangBi.TRAN_DO_HE_PHAN_TRAM).
 *     Tong % chi so tu ngoc moi mon &lt;= {@link #TRAN_PHAN_TRAM_MON} (10%): lo them cho linh hoat (tron he), khong cho
 *     suc manh.</li>
 * <li><b>Hai</b> (bai hoc NSO "dong loi + hai"): -n phan nghin khang he KHAC A (data_ngoc_he_khac, vd Ngoc Lua ->
 *     -khang Nuoc); "Mai ngoc" giam phan hai 50%.</li>
 * <li><b>Gia</b> (ho hut xu, gia theo gio thu nhap o cap nguoi choi): khảm moi / nang len cap n = gio_thu_nhap(n)
 *     (1,45^(n-1) gio); thao 0,5 gio (ngoc giu cap, vao tui ngoc); mai 0,3 gio. Thay = thao + khảm.</li>
 * </ul>
 *
 * <p>Luu tren mon (real.Item.ngocHe, truong co the "nh..." cuoi chuoi getDBInfo): {@link #maHoa}.
 */
public final class NgocHe {

    public static final int CAP_TOI_DA = ThongSoLauDai.NGOC_HE_CAP_TOI_DA;
    public static final int LO_TOI_DA = 3;
    public static final int TRAN_PHAN_TRAM_MON = 10;
    /** Sat thuong he moi cap ngoc (phan van): 1,5 phan nghin. */
    public static final int ST_HE_PHAN_VAN_MOI_CAP = 15;
    /** Khang he khac bi tru moi cap ngoc (phan nghin). */
    public static final int KHANG_TRU_MOI_CAP = 1;
    /** Gia thao / mai (phan nghin gio thu nhap). */
    public static final int GIA_THAO_PHAN_NGHIN_GIO = 500;
    public static final int GIA_MAI_PHAN_NGHIN_GIO = 300;
    /** Tien to truong luu tren mon. */
    public static final String TIEN_TO = "nh";

    private NgocHe() {
    }

    /** Mot lo: he -1 = trong. */
    public static final class O {
        public int he = -1;
        public int cap;
        public boolean mai;

        public O() {
        }

        public O(final int he, final int cap, final boolean mai) {
            this.he = he;
            this.cap = cap;
            this.mai = mai;
        }

        public boolean trong() {
            return this.he < 0 || this.cap <= 0;
        }

        O sao() {
            return new O(this.he, this.cap, this.mai);
        }
    }

    /** Khảm cua MOT mon. */
    public static final class Kham {
        /** Lo them ngoai so lo theo bac (mon che tao Huyen thoai). */
        public int loThem;
        public final O[] o = new O[LO_TOI_DA];

        public Kham() {
            for (int i = 0; i < LO_TOI_DA; i++) {
                this.o[i] = new O();
            }
        }

        public boolean rong() {
            if (this.loThem > 0) {
                return false;
            }
            for (final O x : this.o) {
                if (!x.trong()) {
                    return false;
                }
            }
            return true;
        }

        public Kham sao() {
            final Kham k = new Kham();
            k.loThem = this.loThem;
            for (int i = 0; i < LO_TOI_DA; i++) {
                k.o[i] = this.o[i].sao();
            }
            return k;
        }
    }

    // ------------------------------------------------------------------ ma hoa (truong tren chuoi luu mon)

    /** "nh&lt;loThem&gt;_&lt;o1&gt;_&lt;o2&gt;_&lt;o3&gt;", moi lo = he*100 + cap (+5000 neu da mai), -1 = trong. "" neu mon khong co gi. */
    public static String maHoa(final Kham k) {
        if (k == null || k.rong()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(TIEN_TO).append(Math.max(0, Math.min(LO_TOI_DA, k.loThem)));
        for (final O x : k.o) {
            sb.append('_').append(x.trong() ? -1 : (x.mai ? 5000 : 0) + x.he * 100 + x.cap);
        }
        return sb.toString();
    }

    /** Giai ma truong luu; chuoi rong / hong -> khảm rong (KHONG nem loi: du lieu cu van nap duoc). */
    public static Kham giaiMa(final String s) {
        final Kham k = new Kham();
        if (s == null || !s.startsWith(TIEN_TO)) {
            return k;
        }
        try {
            final String[] p = s.substring(TIEN_TO.length()).split("_");
            k.loThem = Math.max(0, Math.min(LO_TOI_DA, Integer.parseInt(p[0].trim())));
            for (int i = 1; i < p.length && i <= LO_TOI_DA; i++) {
                final int v = Integer.parseInt(p[i].trim());
                if (v < 0) {
                    continue;
                }
                final boolean mai = v >= 5000;
                final int r = v % 5000;
                final int he = r / 100;
                final int cap = r % 100;
                if (HePokemon.hopLe(he) && cap >= 1 && cap <= CAP_TOI_DA) {
                    k.o[i - 1] = new O(he, cap, mai);
                }
            }
        } catch (final Exception e) {
            return new Kham();
        }
        return k;
    }

    /** So lo dung duoc: max(lo theo bac, lo them), &lt;= LO_TOI_DA. */
    public static int soLo(final int loTheoBac, final int loThem) {
        return Math.max(0, Math.min(LO_TOI_DA, Math.max(loTheoBac, loThem)));
    }

    // ------------------------------------------------------------------ hieu ung

    /** % chi so mon tu ngoc (chi lo dung duoc), kep {@code tranPhanTram}. {@code pctTheoCap[cap]} tu bang (null = cap). */
    public static int phanTramMon(final Kham k, final int soLo, final int[] pctTheoCap, final int tranPhanTram) {
        if (k == null) {
            return 0;
        }
        int t = 0;
        for (int i = 0; i < soLo && i < LO_TOI_DA; i++) {
            final O x = k.o[i];
            if (!x.trong()) {
                t += pctTheoCap != null && x.cap < pctTheoCap.length ? Math.max(0, pctTheoCap[x.cap]) : x.cap;
            }
        }
        return Math.max(0, Math.min(Math.max(0, tranPhanTram), t));
    }

    /** Cong ST he (phan van) cua ngoc trong cac lo dung duoc vao {@code stHe[18]}. */
    public static void congStHe(final Kham k, final int soLo, final int phanVanMoiCap, final int[] stHe) {
        if (k == null || stHe == null) {
            return;
        }
        for (int i = 0; i < soLo && i < LO_TOI_DA; i++) {
            final O x = k.o[i];
            if (!x.trong() && x.he < stHe.length) {
                stHe[x.he] += x.cap * Math.max(0, phanVanMoiCap);
            }
        }
    }

    /**
     * Cong phan tru khang (phan van, de giu 0,5 khi mai) cua ngoc vao {@code khangTru[18]}: he bi tru = heKhac[he ngoc].
     */
    public static void congKhangTru(final Kham k, final int soLo, final int[] heKhac, final int phanNghinMoiCap,
            final int[] khangTruPhanVan) {
        if (k == null || khangTruPhanVan == null || heKhac == null) {
            return;
        }
        for (int i = 0; i < soLo && i < LO_TOI_DA; i++) {
            final O x = k.o[i];
            if (x.trong() || x.he >= heKhac.length) {
                continue;
            }
            final int bi = heKhac[x.he];
            if (bi >= 0 && bi < khangTruPhanVan.length) {
                final int pv = x.cap * Math.max(0, phanNghinMoiCap) * 10;
                khangTruPhanVan[bi] += x.mai ? pv / 2 : pv;
            }
        }
    }

    // ------------------------------------------------------------------ gia

    /** Xu khảm moi (cap 1) / nang len cap {@code cap}: gioThuNhap[cap] x xu moi gio (bang trong -> 1,45^(cap-1)). */
    public static long giaCap(final int cap, final double[] gioThuNhap, final long xuMoiGio) {
        final int c = Math.max(1, Math.min(CAP_TOI_DA, cap));
        double gio;
        if (gioThuNhap != null && c < gioThuNhap.length && gioThuNhap[c] > 0) {
            gio = gioThuNhap[c];
        } else {
            gio = Math.pow(ThongSoLauDai.NGOC_HE_TANG_PHAN_TRAM / 100.0, c - 1) * ThongSoLauDai.NGOC_HE_GIA_GIO_PHAN_TRAM / 100.0;
        }
        return Math.max(1L, Math.round(gio * Math.max(0L, xuMoiGio)));
    }

    /** Xu theo phan nghin gio thu nhap (thao 500, mai 300). */
    public static long giaGio(final int phanNghinGio, final long xuMoiGio) {
        return Math.max(1L, Math.max(0L, xuMoiGio) * Math.max(0, phanNghinGio) / 1000L);
    }

    // ------------------------------------------------------------------ tui ngoc (ngoc da thao, chua khảm)

    /** Tui ngoc + bo dem pity che tao cua MOT nhan vat (luu tob_ho_so_bo_ngoc.du_lieu, "k=v;..."). */
    public static final class Tui {
        /** khoa he*1000 + cap*10 + (mai?1:0) -> so vien. */
        private final Map<Integer, Integer> ngoc = new TreeMap<Integer, Integer>();
        /** So lan che tao lien tiep chua ra Su thi (pity che tao). */
        public int cheTaoPity;
        /** Tong so lan che tao (thong ke). */
        public int cheTaoTong;
        /** Da doi tu lan nap cuoi (can luu). */
        public boolean ban;

        static int khoa(final int he, final int cap, final boolean mai) {
            return he * 1000 + cap * 10 + (mai ? 1 : 0);
        }

        public synchronized int so(final int he, final int cap, final boolean mai) {
            final Integer v = this.ngoc.get(Integer.valueOf(khoa(he, cap, mai)));
            return v == null ? 0 : v.intValue();
        }

        public synchronized void them(final int he, final int cap, final boolean mai, final int n) {
            if (!HePokemon.hopLe(he) || cap < 1 || cap > CAP_TOI_DA || n == 0) {
                return;
            }
            final Integer k = Integer.valueOf(khoa(he, cap, mai));
            final int moi = Math.max(0, so(he, cap, mai) + n);
            if (moi == 0) {
                this.ngoc.remove(k);
            } else {
                this.ngoc.put(k, Integer.valueOf(moi));
            }
            this.ban = true;
        }

        /** Lay 1 vien (tat ca hoac khong). */
        public synchronized boolean lay(final int he, final int cap, final boolean mai) {
            if (so(he, cap, mai) <= 0) {
                return false;
            }
            them(he, cap, mai, -1);
            return true;
        }

        /** {he, cap, mai(0/1), so} moi loai dang co, theo khoa tang dan. */
        public synchronized int[][] danhSach() {
            final int[][] r = new int[this.ngoc.size()][];
            int i = 0;
            for (final Map.Entry<Integer, Integer> e : this.ngoc.entrySet()) {
                final int k = e.getKey().intValue();
                r[i++] = new int[]{k / 1000, (k / 10) % 100, k % 10, e.getValue().intValue()};
            }
            return r;
        }

        public synchronized int tongVien() {
            int t = 0;
            for (final Integer v : this.ngoc.values()) {
                t += v.intValue();
            }
            return t;
        }

        public synchronized String maHoa() {
            final StringBuilder sb = new StringBuilder();
            sb.append("cp=").append(this.cheTaoPity).append(";ct=").append(this.cheTaoTong);
            for (final Map.Entry<Integer, Integer> e : this.ngoc.entrySet()) {
                sb.append(";g").append(e.getKey()).append('=').append(e.getValue());
            }
            return sb.toString();
        }

        public static Tui giaiMa(final String s) {
            final Tui t = new Tui();
            if (s == null || s.isEmpty()) {
                return t;
            }
            for (final String cap : s.split(";")) {
                final int eq = cap.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                try {
                    final String k = cap.substring(0, eq).trim();
                    final int v = Integer.parseInt(cap.substring(eq + 1).trim());
                    if (k.equals("cp")) {
                        t.cheTaoPity = Math.max(0, v);
                    } else if (k.equals("ct")) {
                        t.cheTaoTong = Math.max(0, v);
                    } else if (k.startsWith("g") && v > 0) {
                        final int kk = Integer.parseInt(k.substring(1));
                        final int he = kk / 1000;
                        final int c = (kk / 10) % 100;
                        if (HePokemon.hopLe(he) && c >= 1 && c <= CAP_TOI_DA) {
                            t.ngoc.put(Integer.valueOf(khoa(he, c, kk % 10 == 1)), Integer.valueOf(v));
                        }
                    }
                } catch (final Exception ignored) {
                    // dong hong: bo qua
                }
            }
            return t;
        }
    }
}
