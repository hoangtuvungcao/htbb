package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BO TRANG BI theo bac (thiet ke can bang 4.4 "Bo bac", docs/thiet-ke-game.md 4.1): mac N mon CUNG BAC (bac theo cap
 * mau cua mon, {@link NangCapLauDai#bacTheoCap}) thi duoc thuong bo cua bac do. Du lieu: bang data_noi_dung_bo (33 dong:
 * 11 bac x 4 / 7 / 11 mon -> +5% HP / +5% tan cong / +3% chi mang). Lop THUAN (test/KiemBoNgocCheTao).
 *
 * <p>Luat tran (spec 1.3 / 4.5 "thuong bo &lt;= 5%"): moi chi so lay MUC CAO NHAT trong cac bo dang kich (khong cong
 * don hai bo khac bac), roi kep theo tran phan nghin cua chi so do. Thuong bo nang cap cu (+10/+13/+15 du bo) cong chung
 * vao cung tran o phan game (real.BoNgocGame).
 */
public final class BoTrangBi {

    public static final int HP = 0;
    public static final int TAN_CONG = 1;
    public static final int CHI_MANG = 2;
    public static final int SO_CHI_SO = 3;
    public static final String[] TEN_CHI_SO = {"HP", "tấn công", "chí mạng"};

    /** Tran mac dinh (phan nghin): HP / tan cong 5% (GioiHanChiSo.THUONG_BO_TRAN_PHAN_TRAM), chi mang 3%. */
    public static final int[] TRAN_MAC_DINH = {50, 50, 30};

    /** So bac trang bi (BAC_TB_CAP). */
    public static final int SO_BAC = ThongSoLauDai.BAC_TB_CAP.length;

    /** Mot dong data_noi_dung_bo. */
    public static final class Dong {
        public final int bac;
        public final String ten;
        public final String vung;
        public final int soMon;
        public final int chiSo;
        public final int phanTram;
        public final String moTa;

        public Dong(final int bac, final String ten, final String vung, final int soMon, final int chiSo,
                final int phanTram, final String moTa) {
            this.bac = bac;
            this.ten = ten == null ? "" : ten;
            this.vung = vung == null ? "" : vung;
            this.soMon = soMon;
            this.chiSo = chiSo;
            this.phanTram = phanTram;
            this.moTa = moTa == null ? "" : moTa;
        }
    }

    private final List<Dong> dong;

    public BoTrangBi(final List<Dong> ds) {
        final List<Dong> v = new ArrayList<Dong>();
        if (ds != null) {
            for (final Dong d : ds) {
                if (d != null && d.bac >= 0 && d.bac < SO_BAC && d.soMon > 0 && d.chiSo >= 0 && d.phanTram > 0) {
                    v.add(d);
                }
            }
        }
        Collections.sort(v, (a, b) -> a.bac != b.bac ? Integer.compare(a.bac, b.bac) : Integer.compare(a.soMon, b.soMon));
        this.dong = Collections.unmodifiableList(v);
    }

    public static final BoTrangBi RONG = new BoTrangBi(null);

    public List<Dong> dong() {
        return this.dong;
    }

    public int soDong() {
        return this.dong.size();
    }

    /** Ma chi so tu cot chi_so cua bang ("HP", "Tấn công", "Chí mạng"); -1 neu khong biet (dong bi bo qua). */
    public static int maChiSo(final String s) {
        if (s == null) {
            return -1;
        }
        final String t = s.trim().toLowerCase();
        if (t.equals("hp") || t.startsWith("máu")) {
            return HP;
        }
        if (t.startsWith("tấn công") || t.startsWith("tan cong") || t.equals("atk")) {
            return TAN_CONG;
        }
        if (t.startsWith("chí mạng") || t.startsWith("chi mang") || t.equals("crit")) {
            return CHI_MANG;
        }
        return -1;
    }

    /** So mon dang mac theo bac: {@code bacMon} la bac tung mon (bo qua phan tu &lt; 0). */
    public static int[] dem(final int[] bacMon) {
        final int[] d = new int[SO_BAC];
        if (bacMon != null) {
            for (final int b : bacMon) {
                if (b >= 0 && b < SO_BAC) {
                    d[b]++;
                }
            }
        }
        return d;
    }

    /**
     * Thuong bo (phan nghin) theo chi so {HP, tan cong, chi mang}: moi chi so lay muc CAO NHAT cua cac dong da du so mon
     * (khong cong don hai bac), roi kep {@code tran} (phan nghin; null = TRAN_MAC_DINH).
     */
    public int[] thuong(final int[] dem, final int[] tran) {
        final int[] t = new int[SO_CHI_SO];
        if (dem == null) {
            return t;
        }
        for (final Dong d : this.dong) {
            if (d.bac < dem.length && dem[d.bac] >= d.soMon && d.chiSo < SO_CHI_SO) {
                t[d.chiSo] = Math.max(t[d.chiSo], d.phanTram * 10);
            }
        }
        for (int i = 0; i < SO_CHI_SO; i++) {
            final int tr = tran != null && i < tran.length ? tran[i] : TRAN_MAC_DINH[i];
            t[i] = Math.max(0, Math.min(Math.max(0, tr), t[i]));
        }
        return t;
    }

    /** Cac dong cua bac {@code bac} (theo so mon tang dan). */
    public List<Dong> cuaBac(final int bac) {
        final List<Dong> v = new ArrayList<Dong>();
        for (final Dong d : this.dong) {
            if (d.bac == bac) {
                v.add(d);
            }
        }
        return v;
    }

    /** So mon lon nhat cua bo bac {@code bac} (0 neu bac khong co bo). */
    public int soMonDu(final int bac) {
        int m = 0;
        for (final Dong d : this.dong) {
            if (d.bac == bac) {
                m = Math.max(m, d.soMon);
            }
        }
        return m;
    }

    /** "+5% HP" cho mot dong. */
    public static String chuThuong(final Dong d) {
        return "+" + d.phanTram + "% " + (d.chiSo >= 0 && d.chiSo < SO_CHI_SO ? TEN_CHI_SO[d.chiSo] : "?");
    }
}
