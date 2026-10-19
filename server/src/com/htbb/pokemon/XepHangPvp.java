package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.List;

/**
 * DAU XEP HANG PvP (0.9.7) - phan THUAN: hang cho, ghep cap theo dai cap + Elo, chong cay Elo bang nick phu, thuong
 * cuoi mua. Elo mua (ThangPvp) CHI doi trong tran xep hang do may chu ghep (khong con tinh khi giet nguoi ngoai the gioi).
 *
 * <ul>
 * <li><b>Ghep:</b> lech toi da {@link #LECH_CAP} cap ({@link #cungTam}; khong dung dai co dinh 1-10 / 11-20 vi nguoi
 * cap 40 va 41 se khong bao gio gap nhau), |Elo| &lt;= cua so ({@link #cuaSo}: 100 + 50 moi 15 giay
 * cho, toi da 400); khong ghep hai nguoi cung tai khoan / thiet bi / IP ({@link Cum#trung}), khong ghep lai cung doi
 * thu trong {@link #CHAN_LAP_MS}.</li>
 * <li><b>Hop le:</b> tran keo dai &gt;= {@link #GIAY_TOI_THIEU} giay va nguoi thua phai gay &gt;= {@link
 * #PHAN_NGHIN_ST_TOI_THIEU} phan nghin HP toi da cua nguoi thang; khong dat = huy (khong Elo) va ghi nghi van.</li>
 * <li><b>Giam dan:</b> lan dau trong 24 gio voi cung doi thu x1, lan 2 x1/2, lan 3 x1/4, tu lan 4 x0 ({@link #heSoLap}).</li>
 * <li><b>Thuong mua</b> ({@link #thuongMua}): luong khoa theo bac (ThangPvp.LUONG_BAC, khoa) + danh hieu mua co han;
 * can &gt;= {@link #TRAN_TOI_THIEU_MUA} tran trong mua. Khong co chi so ngoai ngan sach o danh hieu.</li>
 * </ul>
 */
public final class XepHangPvp {

    private XepHangPvp() {
    }

    public static final int GIAY_TOI_THIEU = 20;
    public static final int LECH_CAP = 10;
    public static final int PHAN_NGHIN_ST_TOI_THIEU = 100;
    public static final long CHAN_LAP_MS = 10L * 60L * 1000L;
    public static final long CUA_SO_LAP_MS = 24L * 60L * 60L * 1000L;
    public static final int GIAY_TOI_DA = 180;
    public static final int GIAY_DEM_NGUOC = 5;
    public static final int TRAN_TOI_THIEU_MUA = 10;
    public static final int CAP_TOI_THIEU = 20;
    /** Danh hieu thuong mua theo bac 1..3 (data_danh_hieu.ma), giu mot mua (13 tuan). */
    public static final String[] MA_DANH_HIEU_MUA = {"", "thuong_mua_great", "thuong_mua_ultra", "thuong_mua_master"};

    /** Danh tinh chong nick phu cua mot nguoi (ChongClone.ben). */
    public static final class Cum {
        public final int userId;
        public final String may, ip;

        public Cum(final int userId, final String may, final String ip) {
            this.userId = userId;
            this.may = may == null ? "" : may.trim();
            this.ip = ip == null ? "" : ip.trim();
        }

        /** Cung tai khoan, cung thiet bi, hoac cung IP (IP rong = proxy tin cay, khong so). */
        public boolean trung(final Cum o) {
            if (o == null) {
                return false;
            }
            if (userId > 0 && userId == o.userId) {
                return true;
            }
            if (!may.isEmpty() && may.equals(o.may)) {
                return true;
            }
            return !ip.isEmpty() && ip.equals(o.ip);
        }
    }

    /** Mot nguoi trong hang cho. */
    public static final class Cho {
        public final int id, cap, elo;
        public final long vao;
        public final Cum cum;

        public Cho(final int id, final int cap, final int elo, final long vao, final Cum cum) {
            this.id = id;
            this.cap = cap;
            this.elo = elo;
            this.vao = vao;
            this.cum = cum;
        }
    }

    /** Lich su cap doi (de chan ghep lai / giam Elo). */
    public interface LichSu {
        /** Thoi diem tran gan nhat cua cap (0 = chua). */
        long ganNhat(int a, int b);

        /** So tran cua cap trong 24 gio qua. */
        int soTran24h(int a, int b);
    }

    /** Hai cap trong tam ghep (lech &lt;= LECH_CAP). */
    public static boolean cungTam(final int capA, final int capB) {
        return Math.abs(capA - capB) <= LECH_CAP;
    }

    /** Cua so Elo theo thoi gian cho. */
    public static int cuaSo(final long choMs) {
        final long buoc = Math.max(0L, choMs) / 15000L;
        return (int) Math.min(400L, 100L + 50L * buoc);
    }

    public static boolean ghepDuoc(final Cho a, final Cho b, final long bay, final LichSu ls) {
        if (a == null || b == null || a.id == b.id) {
            return false;
        }
        if (!cungTam(a.cap, b.cap)) {
            return false;
        }
        if (a.cum != null && a.cum.trung(b.cum)) {
            return false;
        }
        final int w = Math.min(cuaSo(bay - a.vao), cuaSo(bay - b.vao));
        if (Math.abs(a.elo - b.elo) > w) {
            return false;
        }
        if (ls != null) {
            final long g = ls.ganNhat(a.id, b.id);
            if (g > 0 && bay - g < CHAN_LAP_MS) {
                return false;
            }
            if (heSoLap(ls.soTran24h(a.id, b.id)) == 0) {
                return false; // da het he so voi doi thu nay hom nay: khong ghep (cho nguoi khac)
            }
        }
        return true;
    }

    /** Ghep cap tham lam theo thu tu cho lau nhat truoc. Tra ve cac cap {a, b}. */
    public static List<Cho[]> ghep(final List<Cho> hang, final long bay, final LichSu ls) {
        final List<Cho> ds = new ArrayList<Cho>(hang);
        ds.sort((x, y) -> Long.compare(x.vao, y.vao));
        final List<Cho[]> kq = new ArrayList<Cho[]>();
        final boolean[] da = new boolean[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            if (da[i]) {
                continue;
            }
            int tot = -1;
            int lech = Integer.MAX_VALUE;
            for (int j = i + 1; j < ds.size(); j++) {
                if (!da[j] && ghepDuoc(ds.get(i), ds.get(j), bay, ls)) {
                    final int l = Math.abs(ds.get(i).elo - ds.get(j).elo);
                    if (l < lech) {
                        lech = l;
                        tot = j;
                    }
                }
            }
            if (tot >= 0) {
                da[i] = true;
                da[tot] = true;
                kq.add(new Cho[]{ds.get(i), ds.get(tot)});
            }
        }
        return kq;
    }

    /** He so Elo (phan nghin) cho tran thu n+1 cua cung cap trong 24 gio (n = so tran truoc do). */
    public static int heSoLap(final int nTruoc) {
        switch (Math.max(0, nTruoc)) {
            case 0:
                return 1000;
            case 1:
                return 500;
            case 2:
                return 250;
            default:
                return 0;
        }
    }

    /**
     * Ly do HUY tran (khong Elo, ghi nghi van), null = hop le.
     *
     * @param giay      so giay tu luc bat dau danh
     * @param stThuaGay sat thuong nguoi thua gay cho nguoi thang
     * @param hpThang   HP toi da nguoi thang
     */
    public static String lyDoHuy(final long giay, final long stThuaGay, final int hpThang, final boolean cungCum) {
        if (cungCum) {
            return "cung_tai_khoan_thiet_bi_ip";
        }
        if (giay < GIAY_TOI_THIEU) {
            return "qua_ngan";
        }
        if (hpThang > 0 && stThuaGay * 1000L < (long) hpThang * PHAN_NGHIN_ST_TOI_THIEU) {
            return "khong_danh_tra";
        }
        return null;
    }

    /** Elo moi voi he so lap (phan nghin): delta goc ThangPvp.eloMoi nhan he so, lam tron ve 0. */
    public static int eloMoi(final int eloA, final int eloB, final int ketQua, final int heSoPhanNghin) {
        final int goc = ThangPvp.eloMoi(eloA, eloB, ketQua);
        final long delta = (long) (goc - eloA) * Math.max(0, Math.min(1000, heSoPhanNghin)) / 1000L;
        return (int) (eloA + delta);
    }

    /** Thuong cuoi mua: {bac, luong khoa}; bac 0 / chua du tran = {0, 0}. */
    public static int[] thuongMua(final int elo, final int soTran) {
        if (soTran < TRAN_TOI_THIEU_MUA) {
            return new int[]{0, 0};
        }
        final int bac = ThangPvp.bac(elo);
        final int luong = bac >= 0 && bac < ThangPvp.LUONG_BAC.length ? ThangPvp.LUONG_BAC[bac] : 0;
        return new int[]{bac, luong};
    }
}
