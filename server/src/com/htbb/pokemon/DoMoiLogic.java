package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DO HO MOI (0.9.8): 18 bo he (6 o vai x 5 bac) + 3 bo huy chuong vung (huy hieu / gang / phien da / do cam tay x 11
 * bac). Mon ho la BAN SAO chi so mon goc cung o + bac (sql/nang_cap.sql (khoi them_do_trang_phuc.sql), sinh boi tools/do_trang_phuc/sinh.py)
 * nen KHONG them suc manh doc; thuong bo nam trong tran chung:
 * <ul>
 *   <li>bo he: 2 mon +2% ST he, 4 mon +3% khang he khac, 6 mon them +3% ST he - cong vao ST he (tran chung 20%,
 *       ChiSoTrangBiGame.doHePhanTram) / khang he (tran 60%); ho cung kep {@link #TRAN_ST_HE} / {@link #TRAN_KHANG};</li>
 *   <li>bo vung: 2 / 3 / 4 mon +1% HP / tan cong / chi mang - cong CHUNG voi bo bac vao tran bo 5% / chi mang bo 3%
 *       (BoNgocGame.capNhat), moi chi so lay muc cao nhat giua cac ho.</li>
 * </ul>
 * Khong nguon vao moi: mon ho THAY mon goc khi roi ({@link #bienThe}) - cung so mon, cung do hiem, cung chi so.
 * Lop THUAN (test/KiemDoTrangPhuc). Ban keo CSDL / game: real.DoMoiGame.
 */
public final class DoMoiLogic {

    public static final int LOAI_HE = 0;
    public static final int LOAI_VUNG = 1;

    /** Chi so thuong bo. */
    public static final int CS_ST_HE = 0;
    public static final int CS_KHANG_HE_KHAC = 1;
    public static final int CS_HP = 2;
    public static final int CS_TAN_CONG = 3;
    public static final int CS_CHI_MANG = 4;
    public static final String[] MA_CS = {"st_he", "khang_he_khac", "hp", "tan_cong", "chi_mang"};

    /** Tran mot ho (phan nghin): ST he 5%, khang he 3%; bo vung moi chi so 1%. */
    public static final int TRAN_ST_HE = 50;
    public static final int TRAN_KHANG = 30;
    public static final int TRAN_VUNG = 10;

    /** O (type data_item) co bo he / bo vung. */
    public static final int[] O_BO_HE = {0, 1, 2, 9, 10, 19};
    public static final int[] O_BO_VUNG = {8, 11, 12, 3, 4, 5, 6, 7};
    /** Bac co bo he. */
    public static final int[] BAC_BO_HE = {2, 4, 6, 8, 10};

    /** Ti le (%) mon roi doi sang mon ho (thay, khong them). */
    public static final int TI_LE_HE_QUAI = 40;
    public static final int TI_LE_VUNG_QUAI = 35;
    public static final int TI_LE_BOSS = 60;

    private DoMoiLogic() {
    }

    public static int maCs(final String s) {
        for (int i = 0; i < MA_CS.length; i++) {
            if (MA_CS[i].equals(s == null ? "" : s.trim())) {
                return i;
            }
        }
        return -1;
    }

    public static boolean laOBoHe(final int type) {
        for (final int t : O_BO_HE) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    public static boolean laOBoVung(final int type) {
        for (final int t : O_BO_VUNG) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    /** Mot dong thuong bo. */
    public static final class Thuong {
        public final int soMon;
        public final int chiSo;
        public final int phanNghin;
        public final String moTa;

        public Thuong(final int soMon, final int chiSo, final int phanNghin, final String moTa) {
            this.soMon = soMon;
            this.chiSo = chiSo;
            this.phanNghin = phanNghin;
            this.moTa = moTa == null ? "" : moTa;
        }
    }

    /** Mot ho (bo). */
    public static final class Ho {
        public final String ma;
        public final String ten;
        public final int loai;
        public final int he;
        public final int heKhac;
        public final String vung;
        public final List<Thuong> thuong = new ArrayList<Thuong>();
        public int soO;

        public Ho(final String ma, final String ten, final int loai, final int he, final int heKhac, final String vung) {
            this.ma = ma;
            this.ten = ten == null ? "" : ten;
            this.loai = loai;
            this.he = he;
            this.heKhac = heKhac;
            this.vung = vung == null ? "" : vung;
        }

        public int soMonDu() {
            int m = 0;
            for (final Thuong t : this.thuong) {
                m = Math.max(m, t.soMon);
            }
            return m;
        }
    }

    /** Mot mon ho. */
    public static final class Mon {
        public final int id;
        public final Ho ho;
        public final int type;
        public final int bac;
        public final int idGoc;
        public final String ten;

        public Mon(final int id, final Ho ho, final int type, final int bac, final int idGoc, final String ten) {
            this.id = id;
            this.ho = ho;
            this.type = type;
            this.bac = bac;
            this.idGoc = idGoc;
            this.ten = ten == null ? "" : ten;
        }
    }

    /** Du lieu da nap (bat bien sau khi dung). */
    public static final class DuLieu {
        public final Map<String, Ho> ho = new HashMap<String, Ho>();
        public final Map<Integer, Mon> mon = new HashMap<Integer, Mon>();
        /** (type, bac, he) -&gt; id mon bo he; (type, bac, -1) -&gt; id mon bo vung. */
        final Map<Long, Integer> bien = new HashMap<Long, Integer>();

        public void themHo(final Ho h) {
            this.ho.put(h.ma, h);
        }

        public void themThuong(final String maHo, final Thuong t) {
            final Ho h = this.ho.get(maHo);
            if (h != null && t.chiSo >= 0 && t.soMon > 0 && t.phanNghin > 0) {
                h.thuong.add(t);
                Collections.sort(h.thuong, (a, b) -> Integer.compare(a.soMon, b.soMon));
            }
        }

        public void themMon(final int id, final String maHo, final int type, final int bac, final int idGoc, final String ten) {
            final Ho h = this.ho.get(maHo);
            if (h == null || bac < 0 || bac > 10) {
                return;
            }
            final Mon m = new Mon(id, h, type, bac, idGoc, ten);
            this.mon.put(Integer.valueOf(id), m);
            this.bien.put(Long.valueOf(khoa(type, bac, h.loai == LOAI_HE ? h.he : -1)), Integer.valueOf(id));
            h.soO = 0;
            final java.util.Set<Integer> o = new java.util.HashSet<Integer>();
            for (final Mon x : this.mon.values()) {
                if (x.ho == h) {
                    o.add(Integer.valueOf(x.type == 8 ? 8 : x.type >= 3 && x.type <= 7 ? 3 : x.type));
                }
            }
            h.soO = o.size();
        }

        public Mon mon(final int id) {
            return this.mon.get(Integer.valueOf(id));
        }

        /** id mon ho thay cho (type, bac): he &gt;= 0 = bo he, -1 = bo vung; -1 neu khong co. */
        public int bien(final int type, final int bac, final int he) {
            final Integer v = this.bien.get(Long.valueOf(khoa(type, bac, he)));
            return v == null ? -1 : v.intValue();
        }
    }

    static long khoa(final int type, final int bac, final int he) {
        return ((long) type << 20) | ((long) (bac & 0xFF) << 8) | (he + 1 & 0xFF);
    }

    public static final DuLieu RONG = new DuLieu();

    /**
     * Mon roi (type, bac) co doi sang mon ho khong. {@code xuc} = so ngau nhien 0..99, {@code heQuai} = he Pokemon cua
     * quai / boss (-1 khong ro). Tra id mon ho, hoac -1 = giu mon goc. Thay 1:1: khong bao gio them / bot mon.
     */
    public static int bienThe(final DuLieu d, final int type, final int bac, final int heQuai, final boolean boss, final int xuc) {
        if (d == null || bac < 0 || bac > 10) {
            return -1;
        }
        if (laOBoHe(type)) {
            if (!HePokemon.hopLe(heQuai)) {
                return -1;
            }
            final int id = d.bien(type, bac, heQuai);
            return id > 0 && xuc < (boss ? TI_LE_BOSS : TI_LE_HE_QUAI) ? id : -1;
        }
        if (laOBoVung(type)) {
            final int id = d.bien(type, bac, -1);
            return id > 0 && xuc < (boss ? TI_LE_BOSS : TI_LE_VUNG_QUAI) ? id : -1;
        }
        return -1;
    }

    /** Ket qua thuong bo ho cua mot bo do dang mac. */
    public static final class KetQua {
        /** ST he / khang he (phan nghin, da kep tran ho). */
        public final int[] stHe = new int[HePokemon.SO_HE];
        public final int[] khangHe = new int[HePokemon.SO_HE];
        /** Bo vung: HP / tan cong / chi mang (phan nghin, cao nhat giua cac ho, kep TRAN_VUNG). */
        public int hp;
        public int tanCong;
        public int chiMang;
        /** So mon dang mac theo ma ho. */
        public final Map<String, Integer> dem = new HashMap<String, Integer>();

        public int dem(final String maHo) {
            final Integer v = this.dem.get(maHo);
            return v == null ? 0 : v.intValue();
        }
    }

    /** Thuong bo ho tu danh sach id template dang mac (id khong phai mon ho bi bo qua). */
    public static KetQua tinh(final DuLieu d, final int[] idDangMac) {
        final KetQua k = new KetQua();
        if (d == null || idDangMac == null) {
            return k;
        }
        for (final int id : idDangMac) {
            final Mon m = d.mon(id);
            if (m != null) {
                k.dem.put(m.ho.ma, Integer.valueOf(k.dem(m.ho.ma) + 1));
            }
        }
        for (final Map.Entry<String, Integer> e : k.dem.entrySet()) {
            final Ho h = d.ho.get(e.getKey());
            final int n = e.getValue().intValue();
            int st = 0;
            int kh = 0;
            int hp = 0;
            int tc = 0;
            int cm = 0;
            for (final Thuong t : h.thuong) {
                if (n < t.soMon) {
                    continue;
                }
                switch (t.chiSo) {
                    case CS_ST_HE:
                        st += t.phanNghin;
                        break;
                    case CS_KHANG_HE_KHAC:
                        kh += t.phanNghin;
                        break;
                    case CS_HP:
                        hp += t.phanNghin;
                        break;
                    case CS_TAN_CONG:
                        tc += t.phanNghin;
                        break;
                    case CS_CHI_MANG:
                        cm += t.phanNghin;
                        break;
                    default:
                        break;
                }
            }
            if (h.loai == LOAI_HE) {
                if (HePokemon.hopLe(h.he)) {
                    k.stHe[h.he] = Math.min(TRAN_ST_HE, k.stHe[h.he] + st);
                }
                if (HePokemon.hopLe(h.heKhac)) {
                    k.khangHe[h.heKhac] = Math.min(TRAN_KHANG, k.khangHe[h.heKhac] + kh);
                }
            } else {
                k.hp = Math.max(k.hp, Math.min(TRAN_VUNG, hp));
                k.tanCong = Math.max(k.tanCong, Math.min(TRAN_VUNG, tc));
                k.chiMang = Math.max(k.chiMang, Math.min(TRAN_VUNG, cm));
            }
        }
        return k;
    }

    /** "+2%" / "+1,5%" tu phan nghin. */
    public static String phanTram(final int pn) {
        return pn % 10 == 0 ? "+" + pn / 10 + "%" : "+" + pn / 10 + "," + Math.abs(pn % 10) + "%";
    }

    /** Chu mot dong thuong: "2 món: +2% sát thương hệ Lửa". */
    public static String chuThuong(final Ho h, final Thuong t) {
        final String cs;
        switch (t.chiSo) {
            case CS_ST_HE:
                cs = "sát thương hệ " + (HePokemon.hopLe(h.he) ? HePokemon.TEN[h.he] : "?");
                break;
            case CS_KHANG_HE_KHAC:
                cs = "kháng hệ " + (HePokemon.hopLe(h.heKhac) ? HePokemon.TEN[h.heKhac] : "?");
                break;
            case CS_HP:
                cs = "HP";
                break;
            case CS_TAN_CONG:
                cs = "tấn công";
                break;
            default:
                cs = "chí mạng";
                break;
        }
        return t.soMon + " món: " + phanTram(t.phanNghin) + " " + cs;
    }
}
