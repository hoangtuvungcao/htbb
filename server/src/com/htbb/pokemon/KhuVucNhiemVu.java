package com.htbb.pokemon;

import java.util.List;

/**
 * BUOC NHIEM VU DIA DIEM (nhanh sua-tuyen) - luat thuan, khong I/O. Lop noi game: real.KhuVucNhiemVuGame.
 *
 * <pre>
 *  Buoc "Đứng tại điểm đánh dấu..." (dung_toa_do) / "Đến ..." (den_noi) cua chuoi chinh / phu (goi -64, type 3 noi
 *  chuyen) truoc day xong ngay khi nghe NPC noi. Nay moi buoc co VUNG DICH trong CSDL (data_nhiem_vu_khu_vuc: ban do nap +
 *  khung o x1..x2 / y1..y2, co the nhieu dong = vao dong nao cung duoc, vd ba lang khoi dau):
 *    nghe NPC  -> NHAN buoc (dang lam), bao ban do + o + huong; chua toi thi khong tra duoc;
 *    di vao vung (kiem luc di chuyen, chi nhan vat dang lam buoc dia diem) -> XONG + thuong (noi_chuyen_sau = 0), hoac
 *    "da toi" (trang thai du, dau nhiem vu tren dau NPC tra) roi noi chuyen moi xong (noi_chuyen_sau = 1).
 *  Dang dung trong vung luc nghe NPC = xong ngay (nhu cu).
 * </pre>
 * Kiem thu: test/KiemSuaTuyen.
 */
public final class KhuVucNhiemVu {

    /** Mot vung dich: ban do nap + khung o (bao gom ca hai dau). */
    public static final class Vung {
        public final String ma;
        public final int map;
        public final int x1;
        public final int y1;
        public final int x2;
        public final int y2;
        public final boolean noiChuyenSau;
        public final String ten;

        public Vung(final String ma, final int map, final int x1, final int y1, final int x2, final int y2,
                final boolean noiChuyenSau, final String ten) {
            this.ma = ma;
            this.map = map;
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.noiChuyenSau = noiChuyenSau;
            this.ten = ten == null ? "" : ten;
        }

        /** O (ox, oy) tren ban do nap {@code banDo} nam trong vung. */
        public boolean chua(final int banDo, final int ox, final int oy) {
            return banDo == map && ox >= x1 && ox <= x2 && oy >= y1 && oy <= y2;
        }

        /** O tam vung. */
        public int tamX() {
            return (x1 + x2) / 2;
        }

        public int tamY() {
            return (y1 + y2) / 2;
        }

        /** Khoang cach (o, Chebyshev) tu (ox, oy) toi mep vung; 0 = trong vung. */
        public int cach(final int ox, final int oy) {
            final int dx = ox < x1 ? x1 - ox : (ox > x2 ? ox - x2 : 0);
            final int dy = oy < y1 ? y1 - oy : (oy > y2 ? oy - y2 : 0);
            return Math.max(dx, dy);
        }
    }

    private KhuVucNhiemVu() {
    }

    /** Dung trong mot vung bat ky cua danh sach. */
    public static boolean trong(final List<Vung> ds, final int banDo, final int ox, final int oy) {
        if (ds == null) {
            return false;
        }
        for (final Vung v : ds) {
            if (v.chua(banDo, ox, oy)) {
                return true;
            }
        }
        return false;
    }

    /** Buoc can noi chuyen sau khi toi (moi vung cua mot buoc cung co; lay dong dau). */
    public static boolean noiChuyenSau(final List<Vung> ds) {
        return ds != null && !ds.isEmpty() && ds.get(0).noiChuyenSau;
    }

    /** Vung tren ban do {@code banDo} gan (ox, oy) nhat; null = khong co vung tren ban do nay. */
    public static Vung ganNhat(final List<Vung> ds, final int banDo, final int ox, final int oy) {
        Vung kq = null;
        int tot = Integer.MAX_VALUE;
        if (ds != null) {
            for (final Vung v : ds) {
                if (v.map == banDo && v.cach(ox, oy) < tot) {
                    tot = v.cach(ox, oy);
                    kq = v;
                }
            }
        }
        return kq;
    }

    /** "phía Đông Bắc" ... theo vector (dx, dy) o (y tang = xuong = Nam); "" khi trung. */
    public static String huong(final int dx, final int dy) {
        if (dx == 0 && dy == 0) {
            return "";
        }
        final double g = Math.toDegrees(Math.atan2(-dy, dx)); // 0 = Dong, 90 = Bac
        final String[] ten = {"Đông", "Đông Bắc", "Bắc", "Tây Bắc", "Tây", "Tây Nam", "Nam", "Đông Nam"};
        int i = (int) Math.round(((g % 360) + 360) % 360 / 45.0) % 8;
        return "phía " + ten[i];
    }

    /**
     * Dong tien do / chi duong: tren ban do dich -> "còn khoảng N ô về phía X (ô x,y)"; khac ban do -> "tới &lt;ten ban do&gt;".
     * @param tenBanDo ten ban do dich (null = dung ten vung)
     */
    public static String chiDuong(final List<Vung> ds, final int banDo, final int ox, final int oy, final String tenBanDo) {
        if (ds == null || ds.isEmpty()) {
            return "";
        }
        if (trong(ds, banDo, ox, oy)) {
            return "đã tới nơi";
        }
        final Vung v = ganNhat(ds, banDo, ox, oy);
        if (v == null) {
            final Vung d = ds.get(0);
            final String ten = tenBanDo != null && !tenBanDo.isEmpty() ? tenBanDo : d.ten;
            return "hãy tới " + ten + (laCaBanDo(d) ? "" : " (ô " + d.tamX() + "," + d.tamY() + ")");
        }
        if (laCaBanDo(v)) {
            return "đi vào trong " + (v.ten.isEmpty() ? "khu vực" : v.ten);
        }
        return "còn khoảng " + v.cach(ox, oy) + " ô về " + huong(v.tamX() - ox, v.tamY() - oy) + " (ô " + v.tamX() + "," + v.tamY() + ")";
    }

    /** Vung phu ca ban do (den_noi: vao ban do la toi). */
    public static boolean laCaBanDo(final Vung v) {
        return v.x1 <= 0 && v.y1 <= 0 && v.x2 >= 250 && v.y2 >= 250;
    }
}
