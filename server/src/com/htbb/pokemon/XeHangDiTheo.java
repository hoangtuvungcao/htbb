package com.htbb.pokemon;

import java.util.ArrayDeque;

/**
 * Xe hang (real.MonsterVantieu) di theo VET nguoi choi (sua-xe-hang-ket, bot cb2 31/10/2026).
 * <p>
 * Truoc day xe duoi THANG: buoc x toi khi lech <= 16 px roi buoc y, gap o chan (Map.canMove) thi dung han. Nguoi choi di
 * vong qua nha / hang rao (lang Pewter: Tổng tiêu đầu o 25,11 -> Kecleon o 55,40, duong di xuong cot 51) thi xe dung lech
 * mot cot (x 808 = cot 50 bi chan o hang 18) va ket mai o (808,287); noi chuyen voi Kecleon chi nhan "Xe hàng còn ở xa"
 * - nhiem vu ho tong (N-x.36 bang ngay, TV-10.3, HT-MEW.5, C73.2) va van tieu thuong khong giao duoc.
 * <p>
 * Nay: may chu ghi cac diem nguoi choi da di qua (moi nhip 300 ms, cach nhau >= 8 px - nguoi choi da dung o do nen o do
 * di duoc), xe di lan luot tung diem; chi khi chua co vet (vua nhan xe) moi duoi thang nhu cu. Giua hai diem vet di theo
 * truc x truoc, chan thi y truoc. Xe dung khi da cach nguoi choi <= {@link #CACH} px moi truc.
 */
public final class XeHangDiTheo {

    /** Khoang cach toi thieu giua hai diem vet (px). */
    public static final int GIAN = 8;
    /** Xe dung khi cach nguoi choi <= so px nay moi truc. */
    public static final int CACH = 16;
    /** So diem vet toi da (nguoi choi bo xe qua xa thi xe dung theo luat cu - khong can giu vet dai). */
    public static final int TOI_DA = 256;

    /** O di duoc (toa do px). */
    public interface DiDuoc {
        boolean duoc(int x, int y);
    }

    /** Vet cua mot xe. */
    public static final class Vet {
        final ArrayDeque<int[]> diem = new ArrayDeque<int[]>();

        public int soDiem() {
            return diem.size();
        }

        public void xoa() {
            diem.clear();
        }
    }

    private XeHangDiTheo() {
    }

    /** Ghi vi tri nguoi choi vao vet (bo qua neu chua di xa diem cuoi >= GIAN px). */
    public static void ghi(final Vet v, final int px, final int py) {
        final int[] cuoi = v.diem.peekLast();
        if (cuoi != null && Math.abs(cuoi[0] - px) + Math.abs(cuoi[1] - py) < GIAN) {
            return;
        }
        v.diem.addLast(new int[]{px, py});
        while (v.diem.size() > TOI_DA) {
            v.diem.pollFirst();
        }
    }

    /**
     * Mot buoc cua xe tai (cx,cy) theo nguoi choi o (tx,ty), di toi da {@code buoc} px.
     * @return {x, y} moi
     */
    public static int[] buoc(final Vet v, final int cx, final int cy, final int tx, final int ty, final int buoc, final DiDuoc dd) {
        int x = cx, y = cy, con = buoc;
        if (Math.abs(tx - x) <= CACH && Math.abs(ty - y) <= CACH) {
            return new int[]{x, y};
        }
        while (con > 0) {
            if (Math.abs(tx - x) <= CACH && Math.abs(ty - y) <= CACH) {
                break;
            }
            final int[] d = v.diem.peekFirst();
            if (d == null) {
                // chua co vet: duoi thang (luat cu)
                final int[] r = thang(x, y, tx, ty, con, dd);
                return r;
            }
            if (d[0] == x && d[1] == y) {
                v.diem.pollFirst();
                continue;
            }
            final int[] r = toi(x, y, d[0], d[1], con, dd);
            final int di = Math.abs(r[0] - x) + Math.abs(r[1] - y);
            if (di == 0) {
                v.diem.pollFirst(); // diem nay khong toi duoc tu day: bo, thu diem sau
                continue;
            }
            x = r[0];
            y = r[1];
            con -= di;
        }
        return new int[]{x, y};
    }

    /** Toi (dx,dy): truc x truoc, chan thi y truoc; toi da n px. */
    static int[] toi(final int x0, final int y0, final int dx, final int dy, final int n, final DiDuoc dd) {
        final int[] a = diTruc(x0, y0, dx, dy, n, dd, true);
        if (a[0] == dx && a[1] == dy || Math.abs(a[0] - x0) + Math.abs(a[1] - y0) == n) {
            return a;
        }
        final int[] b = diTruc(x0, y0, dx, dy, n, dd, false);
        return Math.abs(b[0] - dx) + Math.abs(b[1] - dy) < Math.abs(a[0] - dx) + Math.abs(a[1] - dy) ? b : a;
    }

    static int[] diTruc(final int x0, final int y0, final int dx, final int dy, final int n, final DiDuoc dd, final boolean xTruoc) {
        int x = x0, y = y0, con = n;
        for (int lan = 0; lan < 2; lan++) {
            final boolean truX = (lan == 0) == xTruoc;
            while (con > 0) {
                if (truX) {
                    if (x == dx) {
                        break;
                    }
                    final int nx = x + Integer.signum(dx - x);
                    if (!dd.duoc(nx, y)) {
                        break;
                    }
                    x = nx;
                } else {
                    if (y == dy) {
                        break;
                    }
                    final int ny = y + Integer.signum(dy - y);
                    if (!dd.duoc(x, ny)) {
                        break;
                    }
                    y = ny;
                }
                con--;
            }
        }
        return new int[]{x, y};
    }

    /** Luat cu (MonsterVantieu.move truoc sua): x toi khi lech <= 16, roi y toi khi lech <= 16, chan thi dung. */
    public static int[] thang(final int x0, final int y0, final int tx, final int ty, final int n, final DiDuoc dd) {
        int x = x0, y = y0, con = n;
        while (con > 0 && Math.abs(x - tx) > 16) {
            final int nx = x + (x > tx ? -1 : 1);
            if (!dd.duoc(nx, y)) {
                return new int[]{x, y};
            }
            x = nx;
            con--;
        }
        while (con > 0 && Math.abs(y - ty) > 16) {
            final int ny = y + (y > ty ? -1 : 1);
            if (!dd.duoc(x, ny)) {
                return new int[]{x, y};
            }
            y = ny;
            con--;
        }
        return new int[]{x, y};
    }
}
