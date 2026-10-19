package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HOC CHIEU THEO LOAI (de xuat 1, spec thiet_ke_can_bang 4.1): moi loai Pokemon (so Pokedex) co bang chieu rieng
 * (data_learnset, NGUON DUY NHAT - sql/hoc_chieu_huan_luyen.sql / CSDL). Chieu = o chieu cua dong (so hieu client,
 * icon, CharManager.SKILL_*). Hai nguon:
 * <ul>
 *   <li>{@link #NGUON_CAP}: tu hoc khi du cap (cap chieu 1, KHONG ton diem ky nang);</li>
 *   <li>{@link #NGUON_NPC}: hoc o NPC Blaziken bang xu (gia_xu), cung can du cap va dung loai.</li>
 * </ul>
 * Diem ky nang (1 / cap) chi con de nang cap chieu da hoc tu 1 len 9, moc cap Pokemon theo cot cap_cap_chieu.
 *
 * <p>Ma hoa cap chieu trong Char.skill (giu nguyen voi client cu 906): -1 = phai hoc o NPC, 0 = chua hoc
 * (client hien "Can cap N" theo LEVEL_ADD_SKILL[..][..][0] = cap hoc), 1..9 = da hoc.
 *
 * <p>Lop THUAN (khong Char / CSDL): real.HocChieuGame nap du lieu va noi vao game; test/KiemHocChieu doc thang SQL.
 */
public final class Learnset {

    public static final int NGUON_CAP = 0, NGUON_NPC = 1;
    /** Cap chieu toi da bang diem ky nang (Map.doAddSkillPoint: skill &lt; 9). */
    public static final int CAP_CHIEU_TOI_DA = 9;
    /** So moc cap_cap_chieu (cap chieu 1..10). */
    public static final int SO_MOC = 10;

    /** Mot dong data_learnset. */
    public static final class Dong {
        public final int loai, o, dong, he, nguon, capHoc, giaXu;
        public final String ten, kieu;
        /** moc[k] = cap Pokemon can de chieu len cap k+1 (moc[0] = cap hoc). */
        public final int[] moc;

        public Dong(final int loai, final int o, final int dong, final String ten, final int he, final String kieu,
                final int nguon, final int capHoc, final int giaXu, final int[] moc) {
            this.loai = loai;
            this.o = o;
            this.dong = dong;
            this.ten = ten;
            this.he = he;
            this.kieu = kieu;
            this.nguon = nguon;
            this.capHoc = capHoc;
            this.giaXu = giaXu;
            this.moc = moc;
        }

        public boolean laNpc() {
            return this.nguon == NGUON_NPC;
        }
    }

    private static volatile Map<Integer, Dong[]> THEO_LOAI = Collections.emptyMap();
    private static volatile Map<Integer, List<Dong>> THEO_DONG = Collections.emptyMap();

    private Learnset() {
    }

    // ================================================================== du lieu

    /** "npc" / "cap" -> NGUON_*. */
    public static int nguon(final String s) {
        return s != null && s.trim().equalsIgnoreCase("npc") ? NGUON_NPC : NGUON_CAP;
    }

    /** "4/15/26/..." -> 10 moc (thieu thi keo dai moc cuoi, hong thi null). */
    public static int[] moc(final String s) {
        if (s == null) {
            return null;
        }
        final String[] p = s.trim().split("/");
        final int[] m = new int[SO_MOC];
        int cuoi = 0;
        for (int i = 0; i < SO_MOC; i++) {
            if (i < p.length) {
                try {
                    cuoi = Integer.parseInt(p[i].trim());
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
            m[i] = cuoi;
        }
        return m;
    }

    /** Thay toan bo bang (nap tu CSDL / test). Dong trung (loai, o) thi dong sau thang. */
    public static synchronized void dat(final List<Dong> ds) {
        final Map<Integer, Dong[]> l = new HashMap<Integer, Dong[]>();
        final Map<Integer, List<Dong>> d = new HashMap<Integer, List<Dong>>();
        if (ds != null) {
            for (final Dong r : ds) {
                if (r == null || r.o < 0 || r.o >= 15 || r.moc == null) {
                    continue;
                }
                Dong[] a = l.get(r.loai);
                if (a == null) {
                    a = new Dong[15];
                    l.put(r.loai, a);
                }
                a[r.o] = r;
            }
            for (final Dong[] a : l.values()) {
                for (final Dong r : a) {
                    if (r != null) {
                        List<Dong> x = d.get(r.dong);
                        if (x == null) {
                            x = new ArrayList<Dong>();
                            d.put(r.dong, x);
                        }
                        x.add(r);
                    }
                }
            }
        }
        THEO_LOAI = l;
        THEO_DONG = d;
    }

    public static boolean coDuLieu() {
        return !THEO_LOAI.isEmpty();
    }

    public static boolean coLoai(final int loai) {
        return THEO_LOAI.containsKey(loai);
    }

    public static Dong lay(final int loai, final int o) {
        final Dong[] a = THEO_LOAI.get(loai);
        return a == null || o < 0 || o >= a.length ? null : a[o];
    }

    /** Moi dong cua mot dong Pokemon (moi loai trong ho), thu tu khong co dinh. */
    public static List<Dong> cuaDong(final int dong) {
        final List<Dong> x = THEO_DONG.get(dong);
        return x == null ? Collections.<Dong>emptyList() : x;
    }

    /** Loai hoc duoc chieu o o khi o cap (dung loai + du cap). */
    public static boolean hocDuoc(final int loai, final int o, final int cap) {
        final Dong r = lay(loai, o);
        return r != null && cap >= r.capHoc;
    }

    /** O o cua dong la chieu NPC day (theo bat ky loai nao trong ho). */
    public static boolean laNpc(final int dong, final int o) {
        for (final Dong r : cuaDong(dong)) {
            if (r.o == o && r.laNpc()) {
                return true;
            }
        }
        return false;
    }

    /** Dong som nhat (cap hoc nho nhat) cua o trong ho - dung cho LEVEL_ADD_SKILL / bang gia gui client cu. */
    public static Dong somNhat(final int dong, final int o) {
        Dong t = null;
        for (final Dong r : cuaDong(dong)) {
            if (r.o == o && (t == null || r.capHoc < t.capHoc)) {
                t = r;
            }
        }
        return t;
    }

    /** So o chieu cua dong (o lon nhat + 1), 0 neu chua co du lieu. */
    public static int soO(final int dong) {
        int n = 0;
        for (final Dong r : cuaDong(dong)) {
            n = Math.max(n, r.o + 1);
        }
        return n;
    }

    /** He chieu cua loai (nhanh Eevee doi he o 2/8/10); ngoai bang = ChieuPokemon (theo dong). */
    public static int heChieu(final int loai, final int dong, final int o) {
        final Dong r = lay(loai, o);
        return r != null ? r.he : ChieuPokemon.heChieu(dong, o);
    }

    /** Cap Pokemon can de chieu dang o cap {@code capChieu} len cap ke tiep (capChieu 0 = hoc). */
    public static int capCan(final Dong r, final int capChieu) {
        if (r == null) {
            return Integer.MAX_VALUE;
        }
        return r.moc[Math.max(0, Math.min(SO_MOC - 1, capChieu))];
    }

    // ================================================================== chuan hoa cap chieu cua mot Pokemon

    /** Ket qua chuan hoa (chuanHoa). */
    public static final class KetQua {
        /** o chieu bi go vi loai / cap chua hoc duoc (cap cu > 0 hoac chieu NPC da mua). */
        public final List<Integer> go = new ArrayList<Integer>();
        /** o chieu NPC da mua nhung loai chua hoc duoc (hoan xu). */
        public final List<Integer> hoanNpc = new ArrayList<Integer>();
        /** o vua tu hoc (0 -> 1). */
        public final List<Integer> hocMoi = new ArrayList<Integer>();
        /** diem ky nang con lai sau chuan hoa. */
        public int diemKyNang;
        /** da vuot ngan sach diem (du lieu hong / sua tay) -> dua chieu da hoc ve cap 1. */
        public boolean vuotNganSach;
    }

    /**
     * Dua cap chieu cua mot Pokemon ve luat hoc theo loai. Lam lai bao nhieu lan cung ra cung ket qua (idempotent):
     * <ol>
     *   <li>chieu tu hoc: loai chua hoc duoc -&gt; cap 0 (tu hoc lai khi du cap); du cap ma cap &lt;= 0 -&gt; cap 1
     *       (khong ton diem, vao {@link KetQua#hocMoi});</li>
     *   <li>chieu NPC: "da mua" = cap &gt;= 0, TRU o {@code oLoi} (o NPC dau cua dong: chuoi tao nhan vat cu ghi lech mot
     *       vi tri nen moi nhan vat co san 0 o day, luat cu tu choi mua o da &gt; -1 - nen 0 o o nay la CHUA MUA, cap
     *       &gt;= 1 la diem ky nang cong khong qua NPC). Loai chua hoc duoc -&gt; -1; da mua thi vao {@link KetQua#hoanNpc}
     *       de noi goi hoan xu MOT lan. Du cap: da mua cap 0 -&gt; 1, chua mua cap 0 -&gt; -1 (hoc o NPC); cap &gt;= 1 giu;</li>
     *   <li>diem ky nang = (cap cao nhat - 1) - tong(cap chieu - 1) cua chieu da hoc: diem da tieu de hoc chieu (luat cu:
     *       1 diem moi chieu) va diem tieu vao chieu bi go duoc HOAN tu dong. Vuot ngan sach (va khong phai admin): moi
     *       chieu da hoc ve cap 1.</li>
     * </ol>
     *
     * <p>{@code go = false} (moi lan nap sau lan chuyen doi dau, len cap, tien hoa): KHONG go va KHONG doi chieu NPC nao
     * (chieu da co duoc giu nguyen ke ca khi admin doi bang sau nay - khong bao gio lam mat chieu / xu da tra), chi tu
     * hoc them chieu tu hoc va tinh lai diem ky nang.
     *
     * @param skill       mang cap chieu (sua tai cho)
     * @param n           so o chieu cua dong
     * @param cap         cap hien tai (dieu kien hoc)
     * @param capNganSach cap cao nhat da dat (Char.lastLV): ngan sach diem ky nang = capNganSach - 1
     * @param oLoi        o NPC dau cua dong (Map.idSkill[dong][0]) - xem tren; -1 = khong co
     * @param go          lan chuyen doi dau: go chieu loai chua hoc duoc
     */
    public static KetQua chuanHoa(final byte[] skill, final int n, final int loai, final int dong, final int cap,
            final int capNganSach, final int oLoi, final boolean admin, final boolean go) {
        final KetQua kq = new KetQua();
        final int m = Math.min(n, skill.length);
        for (int o = 0; o < m; o++) {
            final int v = skill[o];
            final boolean duoc = hocDuoc(loai, o, cap);
            if (laNpc(dong, o)) {
                if (!go) {
                    continue;
                }
                final boolean daMua = v >= 0 && o != oLoi;
                if (!duoc) {
                    if (v >= 1 || daMua) {
                        kq.go.add(o);
                    }
                    if (daMua) {
                        kq.hoanNpc.add(o);
                    }
                    skill[o] = -1;
                } else if (v == 0) {
                    skill[o] = (byte) (daMua ? 1 : -1);
                }
            } else if (!duoc) {
                if (!go) {
                    continue;
                }
                if (v >= 1) {
                    kq.go.add(o);
                }
                skill[o] = 0;
            } else if (v <= 0) {
                skill[o] = 1;
                kq.hocMoi.add(o);
            }
            if (skill[o] > CAP_CHIEU_TOI_DA + 1) {
                skill[o] = (byte) (CAP_CHIEU_TOI_DA + 1);
            }
        }
        int daTieu = 0;
        for (int o = 0; o < m; o++) {
            if (skill[o] >= 1) {
                daTieu += skill[o] - 1;
            }
        }
        final int nganSach = Math.max(0, Math.max(cap, capNganSach) - 1);
        if (daTieu > nganSach && !admin) {
            kq.vuotNganSach = true;
            for (int o = 0; o < m; o++) {
                if (skill[o] > 1) {
                    skill[o] = 1;
                }
            }
            daTieu = 0;
        }
        kq.diemKyNang = Math.max(0, nganSach - daTieu);
        return kq;
    }

    /**
     * Ten loai hoc duoc o o som nhat (de nhac "khi tien hoa thanh X") - loai khac {@code loai} trong ho, cap hoc
     * nho nhat; null neu khong co.
     */
    public static Dong loaiHocKhac(final int loai, final int dong, final int o) {
        Dong t = null;
        for (final Dong r : cuaDong(dong)) {
            if (r.o == o && r.loai != loai && (t == null || r.capHoc < t.capHoc)) {
                t = r;
            }
        }
        return t;
    }
}
