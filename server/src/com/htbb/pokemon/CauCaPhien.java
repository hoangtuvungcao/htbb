package com.htbb.pokemon;

import com.htbb.cauhinh.CauHinhTrungTam;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 0.9.6.1 nhip cau ca (server quyet dinh, chong cay): THA CAU -> cho ngau nhien -> "Cá cắn câu!" -> nguoi choi KEO CAN
 * trong khung ngan -> co ti le khong duoc gi -> nghi giua hai lan tha. So lieu: khoa cau_ca.* (tools/cauhinh/danh_muc.json,
 * bang cau_hinh; sql/sua_096_1_logic.sql). Lop THUAN cho phan trang thai + quyet dinh; gui tin / tra thuong o
 * HeSinhThaiMenu. test/KiemCauCaNhip.
 *
 * Moc thoi gian (ms, DongHo.bay()):
 *   thaLuc  - luc tha cau (da tru moi + the luc)
 *   canLuc  - luc ca can (thaLuc + cho ngau nhien); nhip server gui "Cá cắn câu!" ngay khi now >= canLuc
 *   baoLuc  - luc server THAT SU gui dau ca can (khung keo can tinh tu day, khong tu canLuc - nhip lech khong lam hut khung)
 * Keo can hop le: baoLuc <= now <= baoLuc + cua_so_ms + tre_mang_ms. Truoc baoLuc = qua som, sau = qua muon: ca thoat.
 */
public final class CauCaPhien {

    private CauCaPhien() {
    }

    public static final String K_CHO_MIN = "cau_ca.cho_min_giay";
    public static final String K_CHO_MAX = "cau_ca.cho_max_giay";
    public static final String K_CHO_HO_LON = "cau_ca.cho_them_ho_lon_giay";
    public static final String K_CUA_SO = "cau_ca.cua_so_ms";
    public static final String K_TRE = "cau_ca.tre_mang_ms";
    public static final String K_TRONG = "cau_ca.trong_phan_tram";
    public static final String K_HOI = "cau_ca.hoi_giay";
    public static final String K_GIAM_TU = "cau_ca.giam_tu_lan_gio";
    public static final String K_GIAM_MOI = "cau_ca.giam_moi_lan_phan_tram";
    public static final String K_TRONG_TOI_DA = "cau_ca.trong_toi_da_phan_tram";

    /** Ket qua keo can. */
    public static final int SOM = 1, DUNG = 2, MUON = 3, KHONG_CO = 0;

    /** So lieu mot lan (doc tu cau hinh; test dung ban co dinh). */
    public static final class SoLieu {
        public final int choMinMs, choMaxMs, choHoLonMs, cuaSoMs, treMs, trongPhanTram, hoiMs, giamTu, giamMoi, trongToiDa;

        public SoLieu(final int choMinGiay, final int choMaxGiay, final int choHoLonGiay, final int cuaSoMs, final int treMs,
                final int trongPhanTram, final int hoiGiay, final int giamTu, final int giamMoi, final int trongToiDa) {
            this.choMinMs = Math.max(1, choMinGiay) * 1000;
            this.choMaxMs = Math.max(this.choMinMs, choMaxGiay * 1000);
            this.choHoLonMs = Math.max(0, choHoLonGiay) * 1000;
            this.cuaSoMs = Math.max(100, cuaSoMs);
            this.treMs = Math.max(0, treMs);
            this.trongPhanTram = Math.max(0, Math.min(100, trongPhanTram));
            this.hoiMs = Math.max(0, hoiGiay) * 1000;
            this.giamTu = Math.max(1, giamTu);
            this.giamMoi = Math.max(0, giamMoi);
            this.trongToiDa = Math.max(this.trongPhanTram, Math.min(100, trongToiDa));
        }
    }

    /** Mac dinh = macDinh trong danh_muc.json (giu khop: test/KiemCauCaNhip). */
    public static final SoLieu MAC_DINH = new SoLieu(6, 15, 3, 1500, 700, 30, 3, 30, 2, 75);

    public static SoLieu soLieu() {
        try {
            return new SoLieu((int) CauHinhTrungTam.so(K_CHO_MIN, 6), (int) CauHinhTrungTam.so(K_CHO_MAX, 15),
                    (int) CauHinhTrungTam.so(K_CHO_HO_LON, 3), (int) CauHinhTrungTam.so(K_CUA_SO, 1500),
                    (int) CauHinhTrungTam.so(K_TRE, 700), (int) CauHinhTrungTam.so(K_TRONG, 30),
                    (int) CauHinhTrungTam.so(K_HOI, 3), (int) CauHinhTrungTam.so(K_GIAM_TU, 30),
                    (int) CauHinhTrungTam.so(K_GIAM_MOI, 2), (int) CauHinhTrungTam.so(K_TRONG_TOI_DA, 75));
        } catch (final Throwable e) {
            return MAC_DINH;
        }
    }

    /** Mot lan tha cau dang cho. */
    public static final class Phien {
        public final int charId, mapId, x, y, hoId;
        public final boolean hoLon, canKeo;
        public final long thaLuc, canLuc;
        /** 0 = chua bao ca can. */
        public volatile long baoLuc;

        public Phien(final int charId, final int mapId, final int x, final int y, final int hoId, final boolean hoLon,
                final boolean canKeo, final long thaLuc, final long canLuc) {
            this.charId = charId;
            this.mapId = mapId;
            this.x = x;
            this.y = y;
            this.hoId = hoId;
            this.hoLon = hoLon;
            this.canKeo = canKeo;
            this.thaLuc = thaLuc;
            this.canLuc = canLuc;
        }
    }

    static final Map<Integer, Phien> DANG = new ConcurrentHashMap<Integer, Phien>();
    /** charId -> luc ket thuc lan cau truoc (ms). */
    static final Map<Integer, Long> XONG_LUC = new ConcurrentHashMap<Integer, Long>();
    /** charId -> {gio (now / 3600000), so lan cau duoc trong gio do}. */
    static final Map<Integer, long[]> DEM_GIO = new ConcurrentHashMap<Integer, long[]>();

    /** Thoi gian cho ca can (ms), ngau nhien deu trong [min, max] (+ them o ho lon). */
    public static long cho(final SoLieu s, final boolean hoLon, final Random r) {
        final int khoang = s.choMaxMs - s.choMinMs;
        return s.choMinMs + (khoang > 0 ? r.nextInt(khoang + 1) : 0) + (hoLon ? s.choHoLonMs : 0);
    }

    /** Con bao nhieu ms nua moi duoc tha cau (0 = duoc). */
    public static long conNghi(final int charId, final long now, final SoLieu s) {
        final Long x = XONG_LUC.get(charId);
        if (x == null) {
            return 0;
        }
        final long con = x + s.hoiMs - now;
        return con > 0 ? con : 0;
    }

    public static Phien dang(final int charId) {
        return DANG.get(charId);
    }

    /** Bat dau mot lan tha; null = dang co phien (tu choi spam). */
    public static Phien tha(final int charId, final int mapId, final int x, final int y, final int hoId, final boolean hoLon,
            final boolean canKeo, final long now, final SoLieu s, final Random r) {
        final Phien p = new Phien(charId, mapId, x, y, hoId, hoLon, canKeo, now, now + cho(s, hoLon, r));
        return DANG.putIfAbsent(charId, p) == null ? p : null;
    }

    /** Nhip: phien den luc ca can ma chua bao -> danh dau baoLuc = now, tra ve de gui tin. */
    public static List<Phien> denLucCan(final long now) {
        final List<Phien> ra = new ArrayList<Phien>();
        for (final Phien p : DANG.values()) {
            if (p.baoLuc == 0 && now >= p.canLuc) {
                p.baoLuc = now;
                ra.add(p);
            }
        }
        return ra;
    }

    /** Nhip: phien da bao ca can ma qua khung keo can -> go bo (ca thoat), tra ve de bao. */
    public static List<Phien> quaKhung(final long now, final SoLieu s) {
        final List<Phien> ra = new ArrayList<Phien>();
        for (final Phien p : new ArrayList<Phien>(DANG.values())) {
            if (p.baoLuc > 0 && now > p.baoLuc + s.cuaSoMs + s.treMs && DANG.remove(p.charId, p)) {
                XONG_LUC.put(p.charId, now);
                ra.add(p);
            }
        }
        return ra;
    }

    /** Phan loai mot lenh keo can luc now (khong doi trang thai). */
    public static int phanLoai(final Phien p, final long now, final SoLieu s) {
        if (p == null) {
            return KHONG_CO;
        }
        if (p.baoLuc == 0 || now < p.baoLuc) {
            return SOM;
        }
        return now <= p.baoLuc + s.cuaSoMs + s.treMs ? DUNG : MUON;
    }

    /** Keo can: go phien (moi ket qua deu ket thuc lan tha) va tra phan loai. */
    public static int keo(final int charId, final long now, final SoLieu s) {
        final Phien p = DANG.get(charId);
        if (p == null) {
            return KHONG_CO;
        }
        final int k = phanLoai(p, now, s);
        if (!DANG.remove(charId, p)) {
            return KHONG_CO;
        }
        XONG_LUC.put(charId, now);
        return k;
    }

    /** Huy phien (roi cho, doi map, thoat) - van tinh nghi. */
    public static Phien huy(final int charId, final long now) {
        final Phien p = DANG.remove(charId);
        if (p != null) {
            XONG_LUC.put(charId, now);
        }
        return p;
    }

    /** So lan cau duoc trong gio dong ho hien tai. */
    public static int demTrongGio(final int charId, final long now) {
        final long[] d = DEM_GIO.get(charId);
        return d == null || d[0] != now / 3600000L ? 0 : (int) d[1];
    }

    public static void ghiCauDuoc(final int charId, final long now) {
        final long gio = now / 3600000L;
        DEM_GIO.compute(charId, (k, d) -> d == null || d[0] != gio ? new long[]{gio, 1} : new long[]{gio, d[1] + 1});
    }

    /** Ti le (%) khong duoc gi khi keo dung luc, voi {@code daCau} lan cau duoc trong gio nay (truoc lan nay). */
    public static int tiLeTrong(final SoLieu s, final int daCau) {
        final int vuot = daCau - s.giamTu;
        final long t = s.trongPhanTram + (vuot >= 0 ? (long) (vuot + 1) * s.giamMoi : 0);
        return (int) Math.min(s.trongToiDa, t);
    }

    public static boolean trong(final SoLieu s, final int daCau, final Random r) {
        return r.nextInt(100) < tiLeTrong(s, daCau);
    }

    /** Test: xoa trang thai. */
    public static void xoaHet() {
        DANG.clear();
        XONG_LUC.clear();
        DEM_GIO.clear();
    }
}
