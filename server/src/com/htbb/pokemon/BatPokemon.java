package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THU PHUC POKEMON hoang da (de xuat 9 / S2 cua ra soat loi choi; docs/thiet-ke-game.md 7.7). Nguoi choi LA mot
 * Pokemon; quai la Pokemon hoang da. Danh quai yeu di (HP &lt;= nguong) roi nem bong (vat pham tieu hao mua bang xu =
 * ho hut nho): thanh cong thi loai do vao Pokedex "Da bat", Hop PC co them mot con, va nhan Keo loai (dung luyen /
 * tien hoa con trong Hop PC - KHONG cong chi so cho nguoi choi). Lop THUAN (test/KiemBatPokemon): moi so ngau nhien
 * truyen vao tu ngoai nen kiem duoc bang RNG co hat giong.
 *
 * <h3>Ti le (phan nghin)</h3>
 * <pre>
 *   tiLe = goc(loai) x heSoHp x heSoCap x (alpha ? 25% : 100%) x heSoBong + thuongTruot,  kep [SAN, tranBong]
 *   heSoHp  = (3M - 2H) / 3M            (Pokemon chinh ban: 25% HP -> 83%, 1 HP -> ~100%)
 *   heSoCap = quai - nguoi choi &lt;= 0: 100% ; 1..5: 80% ; 6..10: 50% ; &gt; 10: khong nem duoc
 *   thuongTruot = moi lan truot truoc tren CUNG con + min(5%, 25% ti le) (toi da 3 lan, lan truot thu 4 con quai bo chay)
 *   goc theo bac hiem (0.9.8 ban trao doi): Thuong 55% / It gap 25% / Hiem 9% / Rat hiem 3% - loai hiem van HIEM
 * </pre>
 * Boss, boss the gioi, Gym, huyen thoai (bang data_bat_loai bat_duoc = 0) KHONG bat duoc; loai chi_su_kien chi bat duoc
 * khi su kien bat (co bat.su_kien).
 */
public final class BatPokemon {

    /** Nguong HP (%) de nem duoc bong. */
    public static final int NGUONG_HP_PHAN_TRAM = 35;
    /** Ti le toi thieu (phan nghin). */
    public static final int SAN_PHAN_NGHIN = 10;
    /** He so quai tinh anh (alpha), phan nghin. */
    public static final int HE_SO_ALPHA = 250;
    /** Thuong moi lan truot truoc tren cung con (phan nghin). */
    public static final int THUONG_TRUOT = 50;
    /** Thuong moi lan truot tinh theo ti le hien tai (phan nghin cua ti le): 250 = +25%, kep o THUONG_TRUOT. */
    public static final int THUONG_TRUOT_TUONG_DOI = 250;
    /** So lan truot thi quai bo chay. */
    public static final int SO_LAN_BO_CHAY = 4;
    /** Lech cap toi da (quai cao hon nguoi choi). */
    public static final int LECH_CAP_TOI_DA = 10;
    /**
     * Goc theo bac hiem loai (phan nghin): 0 Thuong / 1 It gap / 2 Hiem / 3 Rat hiem (gia huyen thoai). Huyen thoai /
     * boss: khong co dong data_bat_loai (khong bat duoc).
     */
    public static final int[] GOC_THEO_DO_HIEM = {550, 250, 90, 30};
    /** Ten bac hiem (Pokedex, Hop PC). */
    public static final String[] TEN_DO_HIEM = {"Thường", "Ít gặp", "Hiếm", "Rất hiếm"};

    public static String tenDoHiem(final int doHiem) {
        return TEN_DO_HIEM[Math.max(0, Math.min(TEN_DO_HIEM.length - 1, doHiem))];
    }

    private BatPokemon() {
    }

    /** Mot loai bong (dong data_bat_bong). */
    public static final class Bong {
        public final String ma;
        public final String ten;
        public final int thuTu;
        public final int heSoPhanTram;
        public final int tranPhanNghin;
        public final int giaPhanNghinGio;
        public final int capMo;

        public Bong(final String ma, final String ten, final int thuTu, final int heSoPhanTram, final int tranPhanNghin,
                final int giaPhanNghinGio, final int capMo) {
            this.ma = ma == null ? "" : ma;
            this.ten = ten == null ? "" : ten;
            this.thuTu = thuTu;
            // tran he so 2,5 lan va tran ti le 95%: khong bong nao "chac chan" bat duoc
            this.heSoPhanTram = Math.max(10, Math.min(250, heSoPhanTram));
            this.tranPhanNghin = Math.max(SAN_PHAN_NGHIN, Math.min(950, tranPhanNghin));
            this.giaPhanNghinGio = Math.max(1, giaPhanNghinGio);
            this.capMo = Math.max(1, capMo);
        }
    }

    /** Bang bong mac dinh (Poke / Great / Ultra) khi CSDL chua co data_bat_bong. */
    public static List<Bong> bongMacDinh() {
        final List<Bong> v = new ArrayList<Bong>();
        v.add(new Bong("poke", "Poké Ball", 1, 100, 600, 20, 1));
        v.add(new Bong("great", "Great Ball", 2, 150, 750, 50, 20));
        v.add(new Bong("ultra", "Ultra Ball", 3, 200, 900, 120, 40));
        return Collections.unmodifiableList(v);
    }

    /** Mot loai Pokemon co the thu phuc (dong data_bat_loai). */
    public static final class Loai {
        public final int dex;
        public final String ten;
        public final int doHiem;
        public final int tiLePhanNghin;
        public final int tienHoaThanh;
        public final int capTienHoa;
        public final int keoTienHoa;
        public final boolean batDuoc;
        public final boolean chiSuKien;
        /** Tien hoa khi duoc trao doi (Haunter -&gt; Gengar, Dusclops -&gt; Dusknoir): khong can cap / keo. */
        public final boolean tienHoaTraoDoi;
        /** Chi so goc loai {HP, Tan cong, Phong thu, Tan cong dac biet, Phong thu dac biet, Toc do}; null = chua co. */
        public final int[] chiSoGoc;

        public Loai(final int dex, final String ten, final int doHiem, final int tiLePhanNghin, final int tienHoaThanh,
                final int capTienHoa, final int keoTienHoa, final boolean batDuoc, final boolean chiSuKien) {
            this(dex, ten, doHiem, tiLePhanNghin, tienHoaThanh, capTienHoa, keoTienHoa, batDuoc, chiSuKien, false, null);
        }

        public Loai(final int dex, final String ten, final int doHiem, final int tiLePhanNghin, final int tienHoaThanh,
                final int capTienHoa, final int keoTienHoa, final boolean batDuoc, final boolean chiSuKien,
                final boolean tienHoaTraoDoi, final int[] chiSoGoc) {
            this.dex = dex;
            this.ten = ten == null ? "" : ten;
            this.doHiem = Math.max(0, Math.min(GOC_THEO_DO_HIEM.length - 1, doHiem));
            this.tiLePhanNghin = tiLePhanNghin > 0 ? Math.min(1000, tiLePhanNghin) : GOC_THEO_DO_HIEM[this.doHiem];
            this.tienHoaThanh = tienHoaThanh;
            this.capTienHoa = capTienHoa;
            this.keoTienHoa = Math.max(0, keoTienHoa);
            this.batDuoc = batDuoc;
            this.chiSuKien = chiSuKien;
            this.tienHoaTraoDoi = tienHoaTraoDoi && tienHoaThanh > 0;
            this.chiSoGoc = chiSoGoc != null && chiSoGoc.length == 6 ? chiSoGoc.clone() : null;
        }
    }

    // ------------------------------------------------------------------ dieu kien

    public static final int OK = 0;
    public static final int KHONG_PHAI_QUAI = 1;
    public static final int KHONG_BAT_DUOC = 2;
    public static final int CHUA_YEU = 3;
    public static final int QUA_MANH = 4;
    public static final int CHI_SU_KIEN = 5;

    /** Kiem loai + HP + cap. {@code laBoss} gom boss, Gym, boss the gioi, quai su kien dac biet. */
    public static int kiem(final Loai loai, final boolean laBoss, final long hp, final long maxhp, final int capQuai,
            final int capNv, final int nguongHpPhanTram, final boolean suKienBat) {
        if (laBoss) {
            return KHONG_BAT_DUOC;
        }
        if (loai == null) {
            return KHONG_PHAI_QUAI;
        }
        if (!loai.batDuoc) {
            return KHONG_BAT_DUOC;
        }
        if (loai.chiSuKien && !suKienBat) {
            return CHI_SU_KIEN;
        }
        if (hp <= 0 || maxhp <= 0 || hp * 100L > maxhp * Math.max(1, Math.min(100, nguongHpPhanTram))) {
            return CHUA_YEU;
        }
        if (heSoCap(capQuai, capNv) <= 0) {
            return QUA_MANH;
        }
        return OK;
    }

    /** Chu cho ma kiem. */
    public static String chuKiem(final int ma, final int nguongHpPhanTram) {
        switch (ma) {
            case KHONG_PHAI_QUAI:
                return "Chỉ thu phục được Pokémon hoang dã.";
            case KHONG_BAT_DUOC:
                return "Pokémon này không thể thu phục.";
            case CHUA_YEU:
                return "Hãy làm Pokémon yếu đi (HP dưới " + nguongHpPhanTram + "%) rồi mới ném bóng.";
            case QUA_MANH:
                return "Pokémon này mạnh hơn bạn quá " + LECH_CAP_TOI_DA + " cấp, chưa thể thu phục.";
            case CHI_SU_KIEN:
                return "Pokémon này chỉ thu phục được trong sự kiện.";
            default:
                return "";
        }
    }

    // ------------------------------------------------------------------ ti le

    /** (3M - 2H) / 3M, phan nghin (1..1000). */
    public static int heSoHp(final long hp, final long maxhp) {
        if (maxhp <= 0) {
            return 0;
        }
        final long h = Math.max(1L, Math.min(maxhp, hp));
        return (int) Math.max(1L, Math.min(1000L, (3L * maxhp - 2L * h) * 1000L / (3L * maxhp)));
    }

    /** He so lech cap (phan nghin); 0 = qua manh, khong nem duoc. */
    public static int heSoCap(final int capQuai, final int capNv) {
        final int d = capQuai - capNv;
        if (d <= 0) {
            return 1000;
        }
        if (d <= 5) {
            return 800;
        }
        if (d <= LECH_CAP_TOI_DA) {
            return 500;
        }
        return 0;
    }

    /**
     * Ti le bat (phan nghin) cho mot lan nem.
     *
     * @param lanTruot so lan da nem truot vao CUNG con nay (0..3)
     */
    public static int tiLe(final Loai loai, final long hp, final long maxhp, final int capQuai, final int capNv,
            final boolean alpha, final Bong bong, final int lanTruot) {
        if (loai == null || bong == null) {
            return 0;
        }
        final int hsCap = heSoCap(capQuai, capNv);
        if (hsCap <= 0) {
            return 0;
        }
        long t = loai.tiLePhanNghin;
        t = t * heSoHp(hp, maxhp) / 1000L;
        t = t * hsCap / 1000L;
        if (alpha) {
            t = t * HE_SO_ALPHA / 1000L;
        }
        t = t * bong.heSoPhanTram / 100L;
        // thuong truot TUONG DOI: +25% ti le hien tai moi lan truot, toi da +5% tuyet doi - loai hiem khong thanh "de" vi thuong
        final long moiLan = Math.min(THUONG_TRUOT, t * THUONG_TRUOT_TUONG_DOI / 1000L);
        t += moiLan * Math.max(0, Math.min(SO_LAN_BO_CHAY - 1, lanTruot));
        return (int) Math.max(SAN_PHAN_NGHIN, Math.min(bong.tranPhanNghin, t));
    }

    /** Thanh cong khi roll (0..999) &lt; tiLe. */
    public static boolean thanhCong(final int roll1000, final int tiLe) {
        return roll1000 >= 0 && roll1000 < tiLe;
    }

    /**
     * So lan bong lac (1..3, chi de dien hoat): bat duoc luon lac 3; truot thi roll cang gan ti le cang lac nhieu
     * (giong cam giac "suyt bat duoc" cua game chinh ban).
     */
    public static int soLanLac(final int roll1000, final int tiLe, final boolean duoc) {
        if (duoc) {
            return 3;
        }
        final int du = roll1000 - tiLe;
        if (du < 100) {
            return 2;
        }
        if (du < 300) {
            return 1;
        }
        return 0;
    }

    /** Quai bo chay sau lan truot nay (lanTruotSau = so lan truot ke ca lan nay). */
    public static boolean boChay(final int lanTruotSau, final int soLanBoChay) {
        return lanTruotSau >= Math.max(1, soLanBoChay);
    }

    /** Keo loai nhan khi bat: 3 (shiny x2). */
    public static int keoKhiBat(final boolean shiny) {
        return shiny ? 6 : 3;
    }

    /** Keo loai nhan khi tha mot con ve tu nhien. */
    public static final int KEO_KHI_THA = 1;

    /** Gia mot vien bong (xu) o cap nguoi choi: phan nghin gio thu nhap. */
    public static long giaBong(final Bong b, final long xuMoiGio) {
        return Math.max(1L, Math.max(0L, xuMoiGio) * b.giaPhanNghinGio / 1000L);
    }

    // ------------------------------------------------------------------ ho loai (keo theo ho tien hoa)

    /**
     * Dex goc cua ho tien hoa (di nguoc tienHoaThanh): Keo loai dung chung ca ho (Torchic / Combusken / Blaziken cung
     * mot loai keo). Vong lap / du lieu hong: tra chinh dex.
     */
    public static int goc(final int dex, final Map<Integer, Loai> bang) {
        if (bang == null) {
            return dex;
        }
        final Map<Integer, Integer> nguoc = new HashMap<Integer, Integer>();
        for (final Loai l : bang.values()) {
            if (l.tienHoaThanh > 0 && !nguoc.containsKey(Integer.valueOf(l.tienHoaThanh))) {
                nguoc.put(Integer.valueOf(l.tienHoaThanh), Integer.valueOf(l.dex));
            }
        }
        int d = dex;
        for (int i = 0; i < 8; i++) {
            final Integer t = nguoc.get(Integer.valueOf(d));
            if (t == null || t.intValue() == dex) {
                break;
            }
            d = t.intValue();
        }
        return d;
    }

    /** Keo de luyen mot con tu cap {@code cap} len cap + 1: 1 + cap/10. */
    public static int keoLuyen(final int cap) {
        return 1 + Math.max(0, cap) / 10;
    }

    /** Cap toi da con trong Hop PC: &lt;= cap nguoi choi, &lt;= 100. */
    public static int capToiDa(final int capNv) {
        return Math.max(1, Math.min(100, capNv));
    }
}
