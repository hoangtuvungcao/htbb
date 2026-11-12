package com.htbb.pokemon;

/**
 * SO SAT THUONG nhan vao cua MOT nhan vat (bao loi Zalo 12/11/2026: "dang treo Tu dong danh, ro rang con nua cay mau ma
 * khong hieu sao chet"). Truoc do may chu khong ghi gi khi nguoi choi guc (Database.saveOrtherLog "die" bi LocLogVatPham
 * bo, Monster.attack khong ghi) -> khong biet ai / cai gi ha, trong bao lau, HP truoc don cuoi la bao nhieu.
 * <p>
 * Giu {@link #TOI_DA} don cuoi (luc, nguon, sat thuong, HP truoc / sau, HP toi da). Khi guc ({@link #chotGuc}): don cuoi
 * khong dua HP ve 0 (HP mat o cho khong ghi nhan) thi them mot don "nguon khac" tu anh chup HP moi nhip ({@link #nhip}) -
 * khong bao gio bao sai "quai X ha" khi thuc ra la nguon khac. Lop THUAN (khong phu thuoc game), kiem o
 * server/test/KiemGucNguyenNhan.
 */
public final class SoSatThuong {

    /** So don giu lai (dong [GUC] in ca 5). */
    public static final int TOI_DA = 5;
    /** Cua so "may giay cuoi" trong cau bao nguoi choi va dong log. */
    public static final long CUA_SO_MS = 3000L;
    /** Dang giao chien: bi danh trong chung nay (Tu dong danh: hoan "dung an toan"). */
    public static final long GIAO_CHIEN_MS = 5000L;
    /** Nguon khi HP mat o cho khong ghi nhan. */
    public static final String NGUON_KHAC = "nguồn khác (không ghi nhận)";

    /** Mot don sat thuong. */
    public static final class Don {
        public final long luc;
        public final String nguon;
        public final int sat, hpTruoc, hpSau, maxhp;

        public Don(final long luc, final String nguon, final int sat, final int hpTruoc, final int hpSau, final int maxhp) {
            this.luc = luc;
            this.nguon = nguon == null || nguon.isEmpty() ? NGUON_KHAC : nguon;
            this.sat = Math.max(0, sat);
            this.hpTruoc = hpTruoc;
            this.hpSau = hpSau;
            this.maxhp = maxhp;
        }
    }

    private final Don[] vong = new Don[TOI_DA];
    private int vt;
    private int so;
    /** Anh chup HP moi nhip nhan vat (CharVongLap) - -1 = chua co. */
    private volatile int hpNhip = -1;
    private volatile long lucNhip;
    private volatile long lucBiDanh;
    /** Da chot lan guc nay (goi lap lai khi van guc -> null); hoi sinh (nhip HP > 0) mo lai + xoa so cu. */
    private volatile boolean daChot;

    /** Ghi mot don (sat thuong > 0). */
    public synchronized void ghi(final long luc, final String nguon, final int sat, final int hpTruoc, final int hpSau,
            final int maxhp) {
        if (sat <= 0) {
            return;
        }
        vong[vt] = new Don(luc, nguon, sat, hpTruoc, Math.max(0, hpSau), maxhp);
        vt = (vt + 1) % TOI_DA;
        if (so < TOI_DA) {
            so++;
        }
        lucBiDanh = luc;
        hpNhip = Math.max(0, hpSau);
        lucNhip = luc;
    }

    /** Moi nhip nhan vat: anh chup HP (bat duoc HP mat o cho khong ghi nhan khi guc). */
    /** @return true = vua hoi sinh sau lan guc da chot (so da xoa). */
    public boolean nhip(final long luc, final int hp) {
        if (hp <= 0) {
            return false;
        }
        boolean moi = false;
        if (daChot) {
            xoa(); // da hoi sinh: doi moi bat dau so moi
            moi = true;
        }
        hpNhip = hp;
        lucNhip = luc;
        return moi;
    }

    /** Luc bi danh cuoi (ms), 0 = chua. */
    public long lucBiDanh() {
        return lucBiDanh;
    }

    /** Dang giao chien: bi danh trong {@link #GIAO_CHIEN_MS}. */
    public boolean dangGiaoChien(final long bay) {
        final long l = lucBiDanh;
        return l > 0L && bay - l < GIAO_CHIEN_MS;
    }

    /** Cac don giu lai, cu -> moi. */
    public synchronized Don[] cuoi() {
        final Don[] kq = new Don[so];
        for (int i = 0; i < so; i++) {
            kq[i] = vong[(vt - so + i + TOI_DA * 2) % TOI_DA];
        }
        return kq;
    }

    /** Hoi sinh / vao game: xoa so (lan guc sau khong lan don cu). */
    public synchronized void xoa() {
        for (int i = 0; i < TOI_DA; i++) {
            vong[i] = null;
        }
        vt = 0;
        so = 0;
        hpNhip = -1;
        lucNhip = 0L;
        lucBiDanh = 0L;
        daChot = false;
    }

    /**
     * Chot khi guc: don cuoi da dua HP ve 0 -> giu; khong thi (HP mat o cho khong ghi nhan) them don {@link #NGUON_KHAC}
     * voi sat thuong = HP biet cuoi cung. @return cac don cu -> moi (don cuoi = don ket lieu); null = lan guc nay da chot
     * (actorDie goi lai khi van guc).
     */
    public synchronized Don[] chotGuc(final long bay, final int maxhp) {
        if (daChot) {
            return null;
        }
        daChot = true;
        final Don[] ds = cuoi();
        final Don cuoi = ds.length == 0 ? null : ds[ds.length - 1];
        if (cuoi != null && cuoi.hpSau <= 0) {
            return ds;
        }
        int hpBiet = cuoi != null && cuoi.luc >= lucNhip ? cuoi.hpSau : hpNhip;
        if (hpBiet < 0) {
            hpBiet = cuoi != null ? cuoi.hpSau : 0;
        }
        ghi(bay, NGUON_KHAC, Math.max(1, hpBiet), hpBiet, 0, maxhp);
        return cuoi();
    }

    // ------------------------------------------------------------------ chu

    /** Tong sat thuong cac don trong {@code cuaSo} ms truoc {@code bay}. */
    public static long tong(final Don[] ds, final long bay, final long cuaSo) {
        long t = 0L;
        for (final Don d : ds) {
            if (d != null && bay - d.luc <= cuaSo) {
                t += d.sat;
            }
        }
        return t;
    }

    /** So don trong {@code cuaSo} ms truoc {@code bay}. */
    public static int dem(final Don[] ds, final long bay, final long cuaSo) {
        int n = 0;
        for (final Don d : ds) {
            if (d != null && bay - d.luc <= cuaSo) {
                n++;
            }
        }
        return n;
    }

    static int pt(final int a, final int b) {
        return b <= 0 ? 0 : (int) Math.max(0L, Math.min(999L, (long) a * 100L / b));
    }

    /** Phan don cua dong log [GUC]: "-1234 Croconaw (cap 24) 2500->1266/20000 (13%) @-1.2s | ..." (cu -> moi). */
    public static String dongLog(final Don[] ds, final long bay) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < ds.length; i++) {
            final Don d = ds[i];
            if (d == null) {
                continue;
            }
            if (b.length() > 0) {
                b.append(" | ");
            }
            final long ms = Math.max(0L, bay - d.luc);
            b.append('-').append(d.sat).append(' ').append(d.nguon).append(' ').append(d.hpTruoc).append("->").append(d.hpSau)
                    .append('/').append(d.maxhp).append(" (").append(pt(d.hpTruoc, d.maxhp)).append("%) @-").append(ms / 1000L)
                    .append('.').append(ms % 1000L / 100L).append('s');
        }
        return b.toString();
    }

    /**
     * Cau bao nguoi choi khi guc: don ket lieu (nguon + sat thuong + HP con truoc don) va tong mat trong 3 giay cuoi. Rong khi
     * khong co don nao.
     */
    public static String cauNguoiChoi(final Don[] ds, final long bay) {
        if (ds == null || ds.length == 0) {
            return "";
        }
        final Don k = ds[ds.length - 1];
        final StringBuilder b = new StringBuilder("Gục vì ");
        if (NGUON_KHAC.equals(k.nguon)) {
            b.append("mất ").append(k.sat).append(" HP (nguồn chưa rõ, đã ghi lại để kiểm tra)");
        } else {
            b.append(k.nguon).append(" đánh ").append(k.sat).append(" HP");
        }
        b.append(" khi còn ").append(k.hpTruoc).append('/').append(k.maxhp).append(" HP (").append(pt(k.hpTruoc, k.maxhp))
                .append("%).");
        final int n = dem(ds, k.luc, CUA_SO_MS);
        if (n > 1) {
            b.append(" ").append(CUA_SO_MS / 1000L).append(" giây cuối mất ").append(tong(ds, k.luc, CUA_SO_MS)).append(" HP qua ")
                    .append(n).append(" đòn");
            final String nhieu = nguonNhieuNhat(ds, k.luc);
            if (nhieu != null && !nhieu.equals(k.nguon)) {
                b.append(" (nhiều nhất: ").append(nhieu).append(')');
            }
            b.append('.');
        }
        return b.toString();
    }

    /** Nguon gay nhieu sat thuong nhat trong cua so 3 giay truoc {@code bay}. */
    static String nguonNhieuNhat(final Don[] ds, final long bay) {
        final java.util.LinkedHashMap<String, Long> m = new java.util.LinkedHashMap<String, Long>();
        for (final Don d : ds) {
            if (d != null && bay - d.luc <= CUA_SO_MS) {
                final Long c = m.get(d.nguon);
                m.put(d.nguon, Long.valueOf((c == null ? 0L : c.longValue()) + d.sat));
            }
        }
        String tot = null;
        long max = -1L;
        for (final java.util.Map.Entry<String, Long> e : m.entrySet()) {
            if (e.getValue().longValue() > max) {
                max = e.getValue().longValue();
                tot = e.getKey();
            }
        }
        return tot;
    }
}
