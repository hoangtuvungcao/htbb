package com.htbb.pokemon;

import java.util.List;
import java.util.Random;

/**
 * Sinh thai the gioi (docs/thiet-ke-game.md). Lop THUAN, tat dinh theo ngay.
 *
 *   - Ngay / dem theo gio may chu (19:00-05:59 la dem).
 *   - Thoi tiet theo NGAY cho moi VUNG SINH THAI (loai ban do trong du_lieu/thiet_ke_map.csv):
 *     cung ngay + cung vung = cung thoi tiet tren moi may, khong can luu.
 *   - Mua (xuan/ha/thu/dong) doi trong so thoi tiet; loai "di cu" chi co mat mot so thang (mat na
 *     thang trong data_hst_thay_quai).
 *   - Thay loai khi quai hoi sinh: KHONG doi so luong / vi tri quai, chi doi template theo bang ung
 *     vien (sinh boi tools/luu_tru_sinh_du_lieu/pokemon/sinh_sql_he_sinh_thai.py).
 *   - Bung phat: moi khung gio trong {@link HeSinhThai#GIO_BUNG_PHAT} chon 1 cap (tuyen, loai cua
 *     chinh tuyen do) theo hat giong ngay.
 */
public final class SinhThaiTheGioi {

    private SinhThaiTheGioi() {
    }

    // ------------------------------------------------------------------ vung sinh thai

    public static final int V_THI_TRAN = 0, V_DUONG = 1, V_DONG_CO = 2, V_RUNG = 3, V_HO = 4, V_BIEN = 5,
            V_NUI = 6, V_TUYET = 7, V_SA_MAC = 8, V_HANG = 9, V_TRONG_NHA = 10, V_THANH = 11,
            V_DAU_TRUONG = 12, V_TAU = 13;
    /** Ma vung = cot loai cua thiet_ke_map.csv (cung thu tu). */
    public static final String[] MA_VUNG = {"thi_tran", "duong", "dong_co", "rung", "ho", "bien", "nui", "tuyet",
        "sa_mac", "hang", "trong_nha", "thanh", "dau_truong", "tau"};
    public static final String[] TEN_VUNG = {"Thị trấn", "Tuyến đường", "Đồng cỏ", "Rừng", "Hồ", "Bờ biển", "Núi",
        "Vùng tuyết", "Sa mạc", "Hang động", "Trong nhà", "Thành", "Đấu trường", "Tàu"};

    /** "rung" -> 3; khong biet -> -1. */
    public static int maVung(final String ma) {
        if (ma == null) {
            return -1;
        }
        for (int i = 0; i < MA_VUNG.length; i++) {
            if (MA_VUNG[i].equals(ma.trim())) {
                return i;
            }
        }
        return -1;
    }

    public static String tenVung(final int vung) {
        return vung >= 0 && vung < TEN_VUNG.length ? TEN_VUNG[vung] : "?";
    }

    /** Vung co mat nuoc de cau ca (con can dung gan o nuoc - xem BanDoNuoc). */
    public static boolean vungCoNuoc(final int vung) {
        return vung == V_HO || vung == V_BIEN || vung == V_RUNG || vung == V_DONG_CO || vung == V_TAU
                || vung == V_THI_TRAN || vung == V_DUONG;
    }

    /** Vung co diem dao khao co. */
    public static boolean vungCoDiemDao(final int vung) {
        return vung == V_HANG || vung == V_NUI || vung == V_SA_MAC || vung == V_TUYET;
    }

    // ------------------------------------------------------------------ thoi tiet

    /**
     * GIO (gio manh) them 0.9.7 (de xuat 8): chi lich theo khung gio (ThoiTietLich) ra gio; thoi tiet THEO NGAY cu
     * ({@link #thoiTiet}) van 6 loai dau (khong doi ket qua cu).
     */
    public static final int QUANG = 0, NANG = 1, MUA = 2, TUYET = 3, BAO_CAT = 4, SUONG_MU = 5, GIO = 6;
    public static final int SO_THOI_TIET = 7;
    public static final String[] TEN_THOI_TIET = {"Quang đãng", "Nắng gắt", "Mưa", "Tuyết", "Bão cát", "Sương mù", "Gió mạnh"};
    /**
     * Hieu ung -39 cua client (AnimateEffect: 0 mua, 1 la roi = bao cat, 2 tuyet; 6 suong mu, 7 nang gat - moc
     * client J2ME/Android moi); -1 = tat. Quang: khong hieu ung. Client cu bo qua id la. Gio: la roi (1). Client
     * >= 908 nhan goi -118 (ThoiTietGame) va tu ve hat (game.pokemon.ThoiTietVe), khong nhan -39 cua he sinh thai.
     */
    public static final int[] HIEU_UNG_CLIENT = {-1, 7, 0, 2, 1, 6, 1};

    /** Trong so thoi tiet [vung][thoi tiet] (QUANG, NANG, MUA, TUYET, BAO_CAT, SUONG_MU). */
    static final int[][] TRONG_SO_THOI_TIET = {
        {40, 30, 25, 0, 0, 5},     // thi tran
        {40, 30, 25, 0, 0, 5},     // duong
        {35, 30, 30, 0, 0, 5},     // dong co
        {30, 15, 30, 0, 0, 25},    // rung
        {30, 15, 40, 0, 0, 15},    // ho
        {30, 25, 40, 0, 0, 5},     // bien
        {35, 20, 15, 10, 0, 20},   // nui
        {20, 0, 0, 70, 0, 10},     // tuyet
        {20, 45, 0, 0, 35, 0},     // sa mac
        {100, 0, 0, 0, 0, 0},      // hang (trong hang khong co thoi tiet)
        {100, 0, 0, 0, 0, 0},      // trong nha
        {40, 30, 25, 0, 0, 5},     // thanh
        {100, 0, 0, 0, 0, 0},      // dau truong
        {30, 25, 40, 0, 0, 5},     // tau
    };

    public static final int XUAN = 0, HA = 1, THU = 2, DONG = 3;
    public static final String[] TEN_MUA = {"Xuân", "Hạ", "Thu", "Đông"};

    /** Thang 1..12 -> mua. Xuan 2-4, Ha 5-7, Thu 8-10, Dong 11-1. */
    public static int mua(final int thang) {
        if (thang >= 2 && thang <= 4) {
            return XUAN;
        }
        if (thang >= 5 && thang <= 7) {
            return HA;
        }
        if (thang >= 8 && thang <= 10) {
            return THU;
        }
        return DONG;
    }

    /** Trong so da chinh theo mua: ha +15 nang; dong: mua o nui/ho chuyen thanh tuyet; thu +10 suong mu. */
    public static int[] trongSoThoiTiet(final int vung, final int thang) {
        final int v = vung >= 0 && vung < TRONG_SO_THOI_TIET.length ? vung : V_DUONG;
        final int[] w = TRONG_SO_THOI_TIET[v].clone();
        if (w[QUANG] == 100) {
            return w;
        }
        switch (mua(thang)) {
            case HA:
                w[NANG] += 15;
                break;
            case THU:
                w[SUONG_MU] += 10;
                break;
            case DONG:
                if (v == V_NUI || v == V_HO) {
                    w[TUYET] += w[MUA] / 2;
                    w[MUA] -= w[MUA] / 2;
                }
                w[NANG] = w[NANG] / 2;
                break;
            default:
                w[MUA] += 10;
                break;
        }
        return w;
    }

    /** Thoi tiet cua vung trong ngay (yyyyMMdd). Tat dinh. */
    public static int thoiTiet(final int ngay, final int vung) {
        final int thang = (ngay / 100) % 100;
        final int[] w = trongSoThoiTiet(vung, thang);
        int tong = 0;
        for (final int x : w) {
            tong += x;
        }
        if (tong <= 0) {
            return QUANG;
        }
        int r = (int) Long.remainderUnsigned(bam(ngay * 131L + vung * 7919L + 17L), tong);
        for (int i = 0; i < w.length; i++) {
            r -= w[i];
            if (r < 0) {
                return i;
            }
        }
        return QUANG;
    }

    public static String tenThoiTiet(final int tt) {
        return tt >= 0 && tt < TEN_THOI_TIET.length ? TEN_THOI_TIET[tt] : "?";
    }

    /** Dem: [19h, 24h) va [0h, 6h). */
    public static boolean laDem(final int gio) {
        return gio >= HeSinhThai.GIO_DEM_TU || gio < HeSinhThai.GIO_DEM_DEN;
    }

    /**
     * He de SINH nhieu hon trong thoi tiet nay (x HE_SO_SINH_HOP_THOI_TIET): bang data_thoi_tiet.he_sinh
     * (ThoiTietLich, mac dinh: nang Lua / Co; mua Nuoc / Dien; tuyet Bang; bao cat Da / Dat / Thep; suong mu Ma / Sieu
     * linh; gio Bay / Rong).
     */
    public static boolean heHopThoiTiet(final int he, final int tt) {
        return ThoiTietLich.heHop(he, tt);
    }

    /** He hoat dong ve dem (ma, bong toi, doc). */
    public static boolean heDem(final int he) {
        return he == HePokemon.MA || he == HePokemon.BONG_TOI || he == HePokemon.DOC;
    }

    /**
     * % sat thuong cua chieu he {@code heChieu} trong thoi tiet {@code tt} (bang data_thoi_tiet qua ThoiTietLich, kep
     * |thay doi| &lt;= ThoiTietLich.TRAN_SAT_THUONG_PHAN_TRAM): mac dinh mua Nuoc +10 / Lua -10, nang Lua +10 / Nuoc -10,
     * tuyet Bang +10, bao cat Da +10, suong mu Ma +10, gio Bay +10.
     */
    public static int satThuongPhanTram(final int heChieu, final int tt) {
        return ThoiTietLich.satThuong(heChieu, tt);
    }

    // ------------------------------------------------------------------ thay loai khi hoi sinh

    /** Mot dong data_hst_thay_quai: ung vien thay cho mot template goc. */
    public static final class UngVien {
        public final int idQuai, he1, he2, trongSo;
        public final boolean hiem;
        /** -1 = moi thoi tiet; khac: chi xuat hien trong thoi tiet nay (dung cho quai hiem). */
        public final int thoiTiet;
        /** -1 = ca ngay; 0 = chi ban ngay; 1 = chi ban dem. */
        public final int ngayDem;
        /** Bit (thang - 1) = co mat thang do (loai di cu). 0xFFF = quanh nam. */
        public final int matNaThang;

        public UngVien(final int idQuai, final int he1, final int he2, final int trongSo, final boolean hiem,
                final int thoiTiet, final int ngayDem, final int matNaThang) {
            this.idQuai = idQuai;
            this.he1 = he1;
            this.he2 = he2;
            this.trongSo = trongSo;
            this.hiem = hiem;
            this.thoiTiet = thoiTiet;
            this.ngayDem = ngayDem;
            this.matNaThang = matNaThang;
        }
    }

    /** Trong so hieu dung cua ung vien trong boi canh hien tai (0 = khong the xuat hien). */
    public static int trongSoHieuDung(final UngVien u, final int tt, final int gio, final int thang) {
        if (u == null || u.trongSo <= 0) {
            return 0;
        }
        if (thang >= 1 && thang <= 12 && (u.matNaThang & (1 << (thang - 1))) == 0) {
            return 0;
        }
        final boolean dem = laDem(gio);
        if (u.ngayDem == 0 && dem || u.ngayDem == 1 && !dem) {
            return 0;
        }
        if (u.thoiTiet >= 0 && u.thoiTiet != tt) {
            return 0;
        }
        if (u.hiem) {
            return u.trongSo; // hiem: khong nhan he so (da bi rang buoc thoi tiet / gio)
        }
        int w = u.trongSo;
        if (heHopThoiTiet(u.he1, tt) || heHopThoiTiet(u.he2, tt)) {
            w *= HeSinhThai.HE_SO_SINH_HOP_THOI_TIET;
        }
        if (dem && (heDem(u.he1) || heDem(u.he2))) {
            w *= HeSinhThai.HE_SO_SINH_HOP_THOI_TIET;
        }
        return w;
    }

    /**
     * Chon template thay the. Tra -1 = giu nguyen template goc.
     * Goi 1 lan moi lan quai hoi sinh. {@code r} chi dung trong ham nay.
     *
     * @param trongSoGoc trong so cua chinh loai goc (giu nguyen loai goc co ti le tuong ung)
     */
    public static int chonThayThe(final List<UngVien> ds, final int trongSoGoc, final int tt, final int gio,
            final int thang, final Random r) {
        if (ds == null || ds.isEmpty() || r.nextInt(100) >= HeSinhThai.TI_LE_THAY_PHAN_TRAM) {
            return -1;
        }
        long tong = Math.max(0, trongSoGoc);
        for (final UngVien u : ds) {
            tong += trongSoHieuDung(u, tt, gio, thang);
        }
        if (tong <= 0) {
            return -1;
        }
        long x = (long) (r.nextDouble() * tong);
        x -= Math.max(0, trongSoGoc);
        if (x < 0) {
            return -1;
        }
        for (final UngVien u : ds) {
            x -= trongSoHieuDung(u, tt, gio, thang);
            if (x < 0) {
                return u.idQuai;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------ bung phat

    /** Khung bung phat dang mo o gio nay (chi so trong GIO_BUNG_PHAT), -1 = khong. */
    public static int khungBungPhat(final int gio) {
        for (int i = 0; i < HeSinhThai.GIO_BUNG_PHAT.length; i++) {
            if (HeSinhThai.GIO_BUNG_PHAT[i] == gio) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Chon cap (map, quai) bung phat cho (ngay, khung) tu danh sach du dieu kien {map, idQuai}.
     * Tat dinh: moi may chu cung ngay ra cung ket qua. null = danh sach rong.
     */
    public static int[] bungPhat(final int ngay, final int khung, final List<int[]> duDieuKien) {
        if (duDieuKien == null || duDieuKien.isEmpty() || khung < 0) {
            return null;
        }
        final int i = (int) Long.remainderUnsigned(bam(ngay * 97L + khung * 1009L + 3L), duDieuKien.size());
        return duDieuKien.get(i);
    }

    /** Hoi sinh tren map dang bung phat loai {@code idBungPhat}: co doi thanh loai do khong. */
    public static boolean doiThanhBungPhat(final int idGoc, final int idBungPhat, final Random r) {
        return idBungPhat > 0 && idGoc != idBungPhat && r.nextInt(100) < HeSinhThai.TI_LE_BUNG_PHAT_PHAN_TRAM;
    }

    // ------------------------------------------------------------------ tien ich

    /** SplitMix64 - bam tat dinh. */
    public static long bam(final long x) {
        long z = x + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
