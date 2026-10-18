package com.htbb.pokemon;

/**
 * Suu tam (docs/pokemon/HE_SINH_THAI.md muc 5, 7). Lop THUAN.
 *
 *   - Nghien cuu Pokedex theo loai: moi loai co cac nhiem vu (bit) gan voi hoat dong khac nhau
 *     - cau ca, ap trung, thoi tiet, ban dem, quai hiem / bung phat, shiny tu trung - de nguoi suu tam
 *     PHAI di qua ca he sinh thai (cau ca, nha tre, thoi tiet...).
 *   - Album anh: moi lan gap su kien hiem (quai hiem, bung phat, ca hiem, trung shiny, hoa thach)
 *     luu 1 "anh" (dex, kieu, thoi tiet, ngay). Moc 10/25/50/100 anh -> danh hieu + luong khoa.
 *   - Hoan thanh vung sinh thai: moi loai cua vung da "nghien cuu xong" -> thuong + chi so vinh vien
 *     nho (+0,5%/vung, tran 3%) - trong ngan sach suc manh (THIET_KE_LAU_DAI 3.6, tinh thong +10%).
 */
public final class SuuTamSinhThai {

    private SuuTamSinhThai() {
    }

    // ------------------------------------------------------------------ nhiem vu nghien cuu (bit)

    public static final int NV_CAU = 1, NV_AP = 2, NV_THOI_TIET = 4, NV_DEM = 8, NV_HIEM = 16, NV_SHINY = 32;
    public static final int[] BIT_NV = {NV_CAU, NV_AP, NV_THOI_TIET, NV_DEM, NV_HIEM, NV_SHINY};
    public static final String[] TEN_NV = {"Câu được", "Cho trứng nở", "Hạ trong thời tiết ưa thích", "Hạ ban đêm",
        "Gặp khi xuất hiện hiếm / bùng phát", "Nở shiny"};

    public static int soBit(final int m) {
        return Integer.bitCount(m);
    }

    /** Loai da "nghien cuu xong": >= 2 nhiem vu (hoac moi nhiem vu ap dung neu loai chi co < 2). */
    public static boolean xongLoai(final int daLam, final int apDung) {
        final int lam = daLam & apDung;
        if (apDung == 0) {
            return false;
        }
        return soBit(lam) >= Math.min(2, soBit(apDung));
    }

    /** Nhiem vu ap dung cho loai: ca -> CAU; trung -> AP + SHINY; quai -> DEM (+ THOI_TIET neu he hop thoi tiet nao do) (+ HIEM). */
    public static int apDung(final boolean laCa, final boolean laTrung, final boolean laQuai, final int he1, final int he2,
            final boolean coHiem) {
        int m = 0;
        if (laCa) {
            m |= NV_CAU;
        }
        if (laTrung) {
            m |= NV_AP | NV_SHINY;
        }
        if (laQuai) {
            m |= NV_DEM;
            for (int tt = 1; tt <= SinhThaiTheGioi.SUONG_MU; tt++) {
                if (SinhThaiTheGioi.heHopThoiTiet(he1, tt) || SinhThaiTheGioi.heHopThoiTiet(he2, tt)) {
                    m |= NV_THOI_TIET;
                }
            }
            if (coHiem) {
                m |= NV_HIEM;
            }
        }
        return m;
    }

    // ------------------------------------------------------------------ album

    public static final int A_HIEM = 0, A_BUNG_PHAT = 1, A_CA_HIEM = 2, A_SHINY_TRUNG = 3, A_HOA_THACH = 4;
    public static final String[] TEN_ANH = {"Quái hiếm", "Bùng phát", "Cá hiếm", "Trứng shiny", "Hoá thạch"};
    public static final int ALBUM_TOI_DA = 200;
    /** Moc album: so anh -> luong khoa; danh hieu chu. */
    public static final int[] MOC_ALBUM = {10, 25, 50, 100};
    public static final int[] THUONG_MOC_ALBUM = {10, 20, 30, 50};
    public static final String[] DANH_HIEU_ALBUM = {"Nhiếp ảnh tập sự", "Thợ săn khoảnh khắc", "Nhà sinh thái học",
        "Huyền thoại ống kính"};

    public static String anh(final int dex, final int kieu, final int thoiTiet, final int ngay) {
        return dex + "." + kieu + "." + thoiTiet + "." + ngay;
    }

    public static String moTaAnh(final String a) {
        final String[] p = a == null ? new String[0] : a.split("\\.");
        if (p.length < 4) {
            return "?";
        }
        try {
            final int dex = Integer.parseInt(p[0]);
            final int kieu = Integer.parseInt(p[1]);
            final int tt = Integer.parseInt(p[2]);
            final String ngay = p[3];
            return tenLoai(dex) + " - " + (kieu >= 0 && kieu < TEN_ANH.length ? TEN_ANH[kieu] : "?") + " - "
                    + SinhThaiTheGioi.tenThoiTiet(tt) + " - " + (ngay.length() == 8
                    ? ngay.substring(6) + "/" + ngay.substring(4, 6) + "/" + ngay.substring(0, 4) : ngay);
        } catch (final NumberFormatException e) {
            return "?";
        }
    }

    /** Moc album vua dat khi so anh tang tu {@code truoc} len {@code sau}; -1 = khong. */
    public static int mocVuaDat(final int truoc, final int sau) {
        for (int i = 0; i < MOC_ALBUM.length; i++) {
            if (truoc < MOC_ALBUM[i] && sau >= MOC_ALBUM[i]) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------ vung

    /** Chi so vinh vien (phan nghin) theo so vung da hoan thanh, co tran. */
    public static int chiSoVinhVien(final int soVungXong) {
        return Math.min(HeSinhThai.CHI_SO_VUNG_TRAN_PHAN_NGHIN, Math.max(0, soVungXong) * HeSinhThai.CHI_SO_VUNG_PHAN_NGHIN);
    }

    // ------------------------------------------------------------------ ten loai

    /** Ten cac loai chi xuat hien trong he sinh thai (ca, trung) - loai khac lay tu LoaiPokemon / template quai. */
    static final Object[][] TEN = {
        {129, "Magikarp"}, {118, "Goldeen"}, {60, "Poliwag"}, {72, "Tentacool"}, {98, "Krabby"},
        {223, "Remoraid"}, {116, "Horsea"}, {90, "Shellder"}, {120, "Staryu"}, {170, "Chinchou"},
        {211, "Qwilfish"}, {222, "Corsola"}, {349, "Feebas"}, {147, "Dratini"}, {131, "Lapras"},
        {163, "Hoothoot"}, {173, "Cleffa"}, {41, "Zubat"}, {627, "Rufflet"}, {77, "Ponyta"},
        {417, "Pachirisu"}, {225, "Delibird"}, {667, "Litleo"}, {58, "Growlithe"}, {585, "Deerling"},
        {399, "Bidoof"},
    };

    /** Bo tra ten bo sung (glue nap tu template quai luc khoi dong); null = khong co. */
    public static volatile java.util.function.IntFunction<String> tenBoSung;

    public static String tenLoai(final int dex) {
        for (final Object[] t : TEN) {
            if (((Integer) t[0]).intValue() == dex) {
                return (String) t[1];
            }
        }
        final String l = LoaiPokemon.ten(dex);
        if (l != null && !l.isEmpty()) {
            return l;
        }
        final java.util.function.IntFunction<String> f = tenBoSung;
        if (f != null) {
            final String s = f.apply(dex);
            if (s != null && !s.isEmpty()) {
                return s;
            }
        }
        return "#" + dex;
    }

    /** Dex cac loai ca trong bang cau ca. */
    public static int[] dexCa() {
        int n = 0;
        for (final int[] d : KyNangSong.BANG_CAU_CA) {
            if (d[1] > 0) {
                n++;
            }
        }
        final int[] r = new int[n];
        n = 0;
        for (final int[] d : KyNangSong.BANG_CAU_CA) {
            if (d[1] > 0) {
                r[n++] = d[1];
            }
        }
        return r;
    }
}
