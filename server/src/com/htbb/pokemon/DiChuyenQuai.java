package com.htbb.pokemon;

import java.util.Random;

/**
 * Di chuyen quai theo CUNG luat va cham voi nguoi choi (bao 0.9.3 "quai di vao tuong / nuoc / ngoai mep map").
 *
 * Loi cu (real/Monster):
 *  - Monster.canMove(x, y) tra "o CHAN" (ten nguoc) va con "&amp;&amp; target == null": luc quai dang duoi nguoi choi
 *    no LUON bao "khong chan" -> quai nhay toi canh nguoi choi (target.x +- 16) xuyen tuong / nuoc / vuc.
 *  - Lang thang (moveOld) chi kiem diem den, khong kiem duong di, ra ngoai mep map bi bat loi tra "chan" sai.
 *  - O trong (tile 254/255, vuc) co type 0 -> server coi la di duoc.
 *
 * Nay: o di duoc cho quai = trong ban do, khong phai o trong, khong phai o cong chuyen map (type >= 2e9),
 * va (type &amp; 2) == 0 (dung luat Map.canMove cua nguoi choi). Duoi muc tieu bang BFS 4 huong (cung thuat toan
 * client_jar/src/game/model/DuongDi.java) gioi han trong vung xich quanh diem sinh; khong co duong = bo muc tieu
 * ve diem sinh (xich). Thuan Java (khong phu thuoc real.*) de kiem tren tep cMap: server/test/KiemQuaiDiChuyen.java.
 */
public final class DiChuyenQuai {
    public static final int CONG = 2000000000;
    /** buoc duoi toi da moi lan di chuyen cua quai (px) */
    public static final int BUOC_DUOI = 96;
    /** ban kinh tim o di duoc gan nhat cho diem sinh nam tren o chan (o) */
    public static final int BAN_KINH_SINH = 8;

    private DiChuyenQuai() {
    }

    /** Luoi va cham theo O (16 px). */
    public interface Luoi {
        int w();

        int h();

        /** O (ox, oy) quai di duoc (da kiem trong ban do). */
        boolean di(int ox, int oy);
    }

    /** Luoi tu du lieu ban do server: type[] (kieu o), tile[] (chi so tile, 255 = trong; null = khong co). */
    public static Luoi luoi(final int[] type, final short[] tile, final int w, final int h) {
        return new Luoi() {
            public int w() {
                return w;
            }

            public int h() {
                return h;
            }

            public boolean di(final int ox, final int oy) {
                return diO(type, tile, w, h, ox, oy);
            }
        };
    }

    public static boolean diO(final int[] type, final short[] tile, final int w, final int h, final int ox, final int oy) {
        if (type == null || ox < 0 || oy < 0 || ox >= w || oy >= h) {
            return false;
        }
        final int i = oy * w + ox;
        if (i >= type.length) {
            return false;
        }
        if (tile != null && i < tile.length && (tile[i] == 255 || tile[i] == 254 || tile[i] < 0)) {
            return false;
        }
        final int t = type[i];
        return t < CONG && (t & 2) == 0;
    }

    /** Diem px di duoc. */
    public static boolean diPx(final Luoi l, final int x, final int y) {
        return x >= 0 && y >= 0 && l.di(x >> 4, y >> 4);
    }

    /** Doan thang px (x0,y0) -> (x1,y1) khong cat o chan (lay mau moi 4 px). */
    public static boolean thong(final Luoi l, final int x0, final int y0, final int x1, final int y1) {
        final int dx = x1 - x0, dy = y1 - y0;
        final int n = Math.max(Math.abs(dx), Math.abs(dy)) / 4 + 1;
        for (int i = 0; i <= n; i++) {
            if (!diPx(l, x0 + dx * i / n, y0 + dy * i / n)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Diem sinh: (x, y) neu di duoc, khong thi o di duoc gan nhat (vong vuong, ban kinh toi da r o), giu lech
     * trong o. null = khong co o di duoc nao gan.
     */
    public static int[] oGan(final Luoi l, final int x, final int y, final int r) {
        if (diPx(l, x, y)) {
            return new int[]{x, y};
        }
        final int ox = x >> 4, oy = y >> 4;
        for (int k = 1; k <= r; k++) {
            int tot = -1, dTot = Integer.MAX_VALUE;
            for (int yy = oy - k; yy <= oy + k; yy++) {
                for (int xx = ox - k; xx <= ox + k; xx++) {
                    if (Math.max(Math.abs(xx - ox), Math.abs(yy - oy)) != k || !l.di(xx, yy)) {
                        continue;
                    }
                    final int d = (xx - ox) * (xx - ox) + (yy - oy) * (yy - oy);
                    if (d < dTot) {
                        dTot = d;
                        tot = yy * l.w() + xx;
                    }
                }
            }
            if (tot >= 0) {
                return new int[]{(tot % l.w()) * 16 + 8, (tot / l.w()) * 16 + 8};
            }
        }
        return null;
    }

    /**
     * Mot buoc lang thang: diem ngau nhien trong +-dmove quanh (x, y), trong xich quanh diem sinh, di duoc va
     * duong thang toi do thong. null = dung yen.
     */
    public static int[] langThang(final Luoi l, final int x, final int y, final int nhaX, final int nhaY, final int dmove,
            final int xich, final Random r) {
        final int dm = Math.max(1, dmove);
        for (int lan = 0; lan < 6; lan++) {
            final int nx = x + r.nextInt(2 * dm + 1) - dm;
            final int ny = y + r.nextInt(2 * dm + 1) - dm;
            if (Math.abs(nx - nhaX) >= xich || Math.abs(ny - nhaY) >= xich) {
                continue;
            }
            if (diPx(l, nx, ny) && thong(l, x, y, nx, ny)) {
                return new int[]{nx, ny};
            }
        }
        return null;
    }

    /** Ket qua duoi: vi tri moi; DA_TOI = dang sat muc tieu (khong can di); null = khong co duong trong xich. */
    public static final int[] DA_TOI = new int[0];

    private static int[] truoc = new int[0];
    private static int[] hang = new int[0];

    /**
     * Buoc duoi muc tieu (tx, ty) tu (x, y): duong ngan nhat (BFS 4 huong) qua o di duoc NAM TRONG xich quanh diem
     * sinh, toi o cach o muc tieu <= 1 (Chebyshev); di theo duong toi da buoc px, dung o diem xa nhat ma doan thang
     * tu vi tri hien tai toi do con thong (client noi suy thang, khong cat goc tuong).
     */
    public static synchronized int[] duoi(final Luoi l, final int x, final int y, final int tx, final int ty, final int nhaX,
            final int nhaY, final int xich, final int buoc) {
        final int w = l.w(), h = l.h();
        final int sx = x >> 4, sy = y >> 4, mx = tx >> 4, my = ty >> 4;
        if (Math.max(Math.abs(sx - mx), Math.abs(sy - my)) <= 1) {
            return DA_TOI;
        }
        final int r = (xich + 15) / 16;
        final int x0 = Math.max(0, Math.min((nhaX >> 4) - r, sx)), y0 = Math.max(0, Math.min((nhaY >> 4) - r, sy));
        final int x1 = Math.min(w - 1, Math.max((nhaX >> 4) + r, sx)), y1 = Math.min(h - 1, Math.max((nhaY >> 4) + r, sy));
        if (x1 < x0 || y1 < y0 || sx < x0 || sy < y0 || sx > x1 || sy > y1) {
            return null;
        }
        final int kw = x1 - x0 + 1, kh = y1 - y0 + 1, n = kw * kh;
        if (truoc.length < n) {
            truoc = new int[n];
            hang = new int[n];
        }
        for (int i = 0; i < n; i++) {
            truoc[i] = -1;
        }
        final int s = (sy - y0) * kw + (sx - x0);
        truoc[s] = s;
        int dau = 0, cuoi = 0, dich = -1;
        hang[cuoi++] = s;
        while (dau < cuoi) {
            final int c = hang[dau++];
            final int cx = c % kw + x0, cy = c / kw + y0;
            if (Math.max(Math.abs(cx - mx), Math.abs(cy - my)) <= 1) {
                dich = c;
                break;
            }
            for (int k = 0; k < 4; k++) {
                final int nx = cx + (k == 0 ? 1 : k == 1 ? -1 : 0), ny = cy + (k == 2 ? 1 : k == 3 ? -1 : 0);
                if (nx < x0 || ny < y0 || nx > x1 || ny > y1) {
                    continue;
                }
                final int ni = (ny - y0) * kw + (nx - x0);
                if (truoc[ni] >= 0 || !l.di(nx, ny)) {
                    continue;
                }
                truoc[ni] = c;
                hang[cuoi++] = ni;
            }
        }
        if (dich < 0) {
            return null;
        }
        // duong: dich -> ... -> s (nguoc); dem so o
        int len = 0;
        for (int c = dich; c != s; c = truoc[c]) {
            len++;
        }
        final int[] duong = new int[len];
        int i = len - 1;
        for (int c = dich; c != s; c = truoc[c]) {
            duong[i--] = c;
        }
        final int toiDa = Math.max(1, buoc / 16);
        int[] tot = null;
        for (int k = 0; k < len && k < toiDa; k++) {
            final int px = (duong[k] % kw + x0) * 16 + 8, py = (duong[k] / kw + y0) * 16 + 8;
            if (thong(l, x, y, px, py)) {
                tot = new int[]{px, py};
            } else if (tot != null) {
                break;
            } else {
                // goc tuong sat ben: buoc mot o (luon thong theo canh o)
                tot = new int[]{px, py};
                break;
            }
        }
        return tot == null ? DA_TOI : tot;
    }
}
