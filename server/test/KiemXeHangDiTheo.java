import com.htbb.pokemon.BanDoPokemon;
import com.htbb.pokemon.DinhDangBanDo;
import com.htbb.pokemon.XeHangDiTheo;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BOT CB2 31/10/2026 (sua-xe-hang-ket): nguoi choi thuong lam N-3.36 o lang Pewter - nhan xe o Tổng tiêu đầu (25,11), di bo
 * toi Kecleon (55,40) theo duong vong qua nha, xe hang dung ket o (808,287) (lech mot cot, o duoi bi chan); noi chuyen voi
 * Kecleon 8 lan deu "Xe hàng còn ở xa" -&gt; khong giao duoc. Kiem com.htbb.pokemon.XeHangDiTheo + noi day MonsterVantieu:
 * <ul>
 *   <li>ban do that lang Pewter (cMap/0): nguoi choi di duong BFS tu Tổng tiêu đầu toi Kecleon, luat cu ket xa &gt; 120 px,
 *       luat moi xe toi canh (&lt;= 120 px = TraHang / HoTongNhiemVuGame.traTaiKecleon);</li>
 *   <li>luoi tu ve: tuong chu L, duong chu U; ghi vet (gian 8 px, toi da 256); khong vet = luat cu; xe khong bao gio vao o chan;</li>
 *   <li>noi day: MonsterVantieu.move dung XeHangDiTheo.buoc, update ghi vet, Char.doChangeMapMonster xoa vet.</li>
 * </ul>
 */
public final class KiemXeHangDiTheo {

    private KiemXeHangDiTheo() {
    }

    static final class Luoi implements XeHangDiTheo.DiDuoc {
        final int w, h;
        final int[] type;

        Luoi(final int w, final int h, final int[] type) {
            this.w = w;
            this.h = h;
            this.type = type;
        }

        /** Nhu real.Map.canMove: (type & 2) != 2, ngoai bien = chan. */
        @Override
        public boolean duoc(final int x, final int y) {
            if (x < 0 || y < 0 || (x >> 4) >= w || (y >> 4) >= h) {
                return false;
            }
            return (type[(y >> 4) * w + (x >> 4)] & 2) != 2;
        }

        boolean o(final int tx, final int ty) {
            return duoc(tx * 16 + 8, ty * 16 + 8);
        }
    }

    public static void chay() {
        System.out.println("== " + "Xe hang di theo vet nguoi choi (sua-xe-hang-ket, Pewter N-3.36)");
        try {
            donVi();
            tuVe();
            banDoThat();
            noiDay();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static Luoi tuChuoi(final String... hang) {
        final int w = hang[0].length(), h = hang.length;
        final int[] t = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                t[y * w + x] = hang[y].charAt(x) == '#' ? 2 : 0;
            }
        }
        return new Luoi(w, h, t);
    }

    static Luoi banDo(final int id) throws Exception {
        final DinhDangBanDo m = DinhDangBanDo.doc(Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + id).toPath()));
        final byte[] k = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + DinhDangBanDo.boTile(id) + ".type").toPath());
        final int[] type = new int[m.w * m.h];
        for (int i = 0; i < type.length; i++) {
            final int t = m.tile[i] == 254 ? 255 : m.tile[i];
            if (t != 255 && t < k.length) {
                type[i] = k[t] & 0xFF;
            }
        }
        return new Luoi(m.w, m.h, type);
    }

    /** Duong BFS 4 huong theo o (nhu bot / client di tung o). */
    static List<int[]> duong(final Luoi l, final int sx, final int sy, final int dx, final int dy) {
        final int[] truoc = new int[l.w * l.h];
        java.util.Arrays.fill(truoc, -1);
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        final int bd = sy * l.w + sx, dich = dy * l.w + dx;
        truoc[bd] = bd;
        q.add(bd);
        final int[][] hg = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty() && truoc[dich] < 0) {
            final int c = q.poll();
            for (final int[] g : hg) {
                final int nx = c % l.w + g[0], ny = c / l.w + g[1];
                if (nx < 0 || ny < 0 || nx >= l.w || ny >= l.h) {
                    continue;
                }
                final int n = ny * l.w + nx;
                if (truoc[n] >= 0 || !l.o(nx, ny)) {
                    continue;
                }
                truoc[n] = c;
                q.add(n);
            }
        }
        if (truoc[dich] < 0) {
            return null;
        }
        final List<int[]> d = new ArrayList<int[]>();
        for (int c = dich; c != bd; c = truoc[c]) {
            d.add(new int[]{c % l.w, c / l.w});
        }
        Collections.reverse(d);
        return d;
    }

    /**
     * Mo phong: nguoi choi di tung o (tam o) {@code buocNguoi} px / nhip 300 ms; moi nhip xe ghi vet + di 30 px (moi
     * {@code cu} = luat cu MonsterVantieu: duoi thang). Xe chi di khi nguoi choi trong 80 px moi truc (luat cu giu nguyen).
     * Tra ve {xe x, xe y, nguoi x, nguoi y, so lan xe vao o chan}.
     */
    static int[] moPhong(final Luoi l, final int sx, final int sy, final List<int[]> d, final boolean cu, final int buocNguoi) {
        final List<int[]> px = new ArrayList<int[]>();
        int ax = sx * 16 + 8, ay = sy * 16 + 8;
        for (final int[] o : d) {
            final int bx = o[0] * 16 + 8, by = o[1] * 16 + 8;
            while (ax != bx || ay != by) {
                ax += Integer.signum(bx - ax);
                ay += Integer.signum(by - ay);
                px.add(new int[]{ax, ay});
            }
        }
        final XeHangDiTheo.Vet v = new XeHangDiTheo.Vet();
        int nx = sx * 16 + 8, ny = sy * 16 + 8, cx = nx, cy = ny, i = 0, vaoChan = 0;
        for (int nhip = 0; nhip < px.size() / Math.max(1, buocNguoi) + 60; nhip++) {
            // nguoi choi doi xe (nhu bot: di tung doan roi doi) - khong bo xa qua 64 px
            if (Math.abs(nx - cx) <= 64 && Math.abs(ny - cy) <= 64 || cu && nhip % 4 == 0) {
                i = Math.min(px.size() - 1, i + buocNguoi);
                if (!px.isEmpty()) {
                    nx = px.get(i)[0];
                    ny = px.get(i)[1];
                }
            }
            if (Math.abs(nx - cx) <= 80 && Math.abs(ny - cy) <= 80) {
                final int[] r;
                if (cu) {
                    r = XeHangDiTheo.thang(cx, cy, nx, ny, 30, l);
                } else {
                    XeHangDiTheo.ghi(v, nx, ny);
                    r = XeHangDiTheo.buoc(v, cx, cy, nx, ny, 30, l);
                }
                cx = r[0];
                cy = r[1];
                if (!l.duoc(cx, cy)) {
                    vaoChan++;
                }
            }
        }
        return new int[]{cx, cy, nx, ny, vaoChan};
    }

    static boolean gan(final int[] r) {
        return Math.abs(r[0] - r[2]) <= 120 && Math.abs(r[1] - r[3]) <= 120;
    }

    static void donVi() {
        final XeHangDiTheo.Vet v = new XeHangDiTheo.Vet();
        XeHangDiTheo.ghi(v, 100, 100);
        XeHangDiTheo.ghi(v, 103, 102);
        Kiem.bang("vet: diem cach < 8 px bo qua", 1, v.soDiem());
        XeHangDiTheo.ghi(v, 108, 100);
        Kiem.bang("vet: diem cach >= 8 px ghi", 2, v.soDiem());
        for (int i = 0; i < 1000; i++) {
            XeHangDiTheo.ghi(v, 200 + i * 10, 100);
        }
        Kiem.bang("vet: toi da " + XeHangDiTheo.TOI_DA + " diem", XeHangDiTheo.TOI_DA, v.soDiem());
        v.xoa();
        Kiem.bang("vet: xoa", 0, v.soDiem());
        final Luoi trong = tuChuoi("..........", "..........", "..........");
        final int[] a = XeHangDiTheo.buoc(new XeHangDiTheo.Vet(), 8, 8, 150, 40, 30, trong);
        final int[] b = XeHangDiTheo.thang(8, 8, 150, 40, 30, trong);
        Kiem.bang("khong co vet (vua nhan xe): giong luat cu", b[0] + "," + b[1], a[0] + "," + a[1]);
        final int[] c = XeHangDiTheo.buoc(new XeHangDiTheo.Vet(), 100, 100, 110, 112, 30, trong);
        Kiem.bang("da canh nguoi choi (<= 16 px): dung yen", "100,100", c[0] + "," + c[1]);
        final XeHangDiTheo.Vet v2 = new XeHangDiTheo.Vet();
        XeHangDiTheo.ghi(v2, 40, 8);
        final int[] d = XeHangDiTheo.buoc(v2, 8, 8, 120, 8, 30, trong);
        Kiem.dung("moi nhip di toi da 30 px (" + d[0] + "," + d[1] + ")", Math.abs(d[0] - 8) + Math.abs(d[1] - 8) <= 30);
    }

    static void tuVe() {
        // tuong chu L: nguoi choi di xuong roi sang phai vong qua goc; luat cu dam vao tuong
        final Luoi l = tuChuoi(
                "..............",
                ".########.....",
                ".#............",
                ".#.######.....",
                ".#.#..........",
                "...#..........",
                "...#..........");
        final List<int[]> d = duong(l, 2, 2, 12, 6);
        Kiem.dung("luoi chu L: co duong", d != null);
        final int[] cu = moPhong(l, 2, 2, d, true, 8);
        final int[] moi = moPhong(l, 2, 2, d, false, 8);
        System.out.println("   (chu L, luat cu: xe " + cu[0] + "," + cu[1] + " nguoi " + cu[2] + "," + cu[3] + " - tai hien loi o ban do that Pewter ben duoi)");
        Kiem.dung("chu L: luat moi toi canh nguoi choi (" + moi[0] + "," + moi[1] + " vs " + moi[2] + "," + moi[3] + ")", gan(moi));
        Kiem.bang("chu L: xe khong vao o chan", 0, moi[4]);
        // duong chu U: xuong, sang, len
        final Luoi u = tuChuoi(
                "..#......#..",
                "..#......#..",
                "..#..##..#..",
                "..#..##..#..",
                "............",
                "............");
        final List<int[]> du = duong(u, 0, 0, 11, 0);
        final int[] mu = moPhong(u, 0, 0, du, false, 8);
        Kiem.dung("chu U: luat moi toi canh (" + mu[0] + "," + mu[1] + " vs " + mu[2] + "," + mu[3] + ")", gan(mu));
        Kiem.bang("chu U: xe khong vao o chan", 0, mu[4]);
        final int[] mu2 = moPhong(u, 0, 0, du, false, 23);
        Kiem.dung("chu U, nguoi choi nhanh (23 px / nhip = di bo): toi canh", gan(mu2));
    }

    static void banDoThat() throws Exception {
        if (!new File(BanDoPokemon.THU_MUC_CMAP + "0").isFile()) {
            Kiem.dung("co cMap/0 (chay o server/)", false);
            return;
        }
        final Luoi l = banDo(0);
        // Tổng tiêu đầu Pewter (real.HoTongNhiemVuGame.VI_TRI_NHAN {400,176} -> o 25,11), Kecleon (55,40): bot dung o 25,12 / 55,41
        Kiem.dung("Pewter: o nhan 25,12 va o giao 55,41 di duoc", l.o(25, 12) && l.o(55, 41));
        final List<int[]> d = duong(l, 25, 12, 55, 41);
        Kiem.dung("Pewter: co duong Tổng tiêu đầu -> Kecleon", d != null);
        if (d == null) {
            return;
        }
        final int[] cu = moPhong(l, 25, 12, d, true, 8);
        Kiem.dung("Pewter: luat cu ket (xe " + cu[0] + "," + cu[1] + ", nguoi " + cu[2] + "," + cu[3] + ") - tai hien loi bot", !gan(cu));
        for (final int buocNguoi : new int[]{8, 16, 23}) {
            final int[] moi = moPhong(l, 25, 12, d, false, buocNguoi);
            Kiem.dung("Pewter: luat moi, nguoi " + buocNguoi + " px / nhip: xe toi Kecleon (xe " + moi[0] + "," + moi[1] + ", nguoi " + moi[2] + ","
                    + moi[3] + ")", gan(moi) && Math.abs(moi[2] - (55 * 16 + 8)) <= 16 && Math.abs(moi[3] - (41 * 16 + 8)) <= 16);
            Kiem.bang("Pewter: xe khong vao o chan (" + buocNguoi + " px)", 0, moi[4]);
        }
        // moi lang ho tong: tu Tổng tiêu đầu toi Kecleon (Blackthorn 70: 29,21 -> 6,11)
        if (new File(BanDoPokemon.THU_MUC_CMAP + "70").isFile()) {
            final Luoi b = banDo(70);
            final List<int[]> db = duong(b, 29, 21, 6, 11);
            if (db != null) {
                Kiem.dung("Blackthorn: luat moi toi Kecleon", gan(moPhong(b, 29, 21, db, false, 16)));
            }
        }
    }

    static void noiDay() throws Exception {
        final String mv = new String(Files.readAllBytes(Paths.get("src/real/MonsterVantieu.java")), StandardCharsets.UTF_8);
        Kiem.dung("MonsterVantieu.move dung XeHangDiTheo.buoc", mv.contains("XeHangDiTheo.buoc(this.vet"));
        Kiem.dung("MonsterVantieu.update ghi vet nguoi choi", mv.contains("XeHangDiTheo.ghi(this.vet, this.target.x, this.target.y)"));
        // sua-xe-lac-map 07/11/2026: Char.doChangeMapMonster keo xe qua XeHangChuyenMap.keoXe
        final String ch = new String(Files.readAllBytes(Paths.get("src/real/XeHangChuyenMap.java")), StandardCharsets.UTF_8);
        Kiem.dung("XeHangChuyenMap.keoXe xoa vet khi xe sang ban do moi", ch.contains("m.vet.xoa()"));
    }
}
