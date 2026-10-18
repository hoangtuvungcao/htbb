import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Kiem VA CHAM phia server bang bot that (giao thuc BotTai) tren server THU (khong bao gio server that).
 *
 * Moi ban do chi dinh: GM "mto" toi tung o di duoc sat vat the (nha, tuong, cay, nuoc, vach, den, hang rao...)
 * roi gui goi di chuyen (lenh 4):
 *   1. vao o vat the ke ben (16 px)               -> server phai TRA vi tri cu (CharCapNhat.doPlayerMove: canMove)
 *   2. NHAY qua tuong 1 o toi o di duoc phia sau  -> phai bi tra ve (Char.checkToPoint, buoc &lt;= 50 px)
 *   3. buoc hop le sang o di duoc ke ben          -> phai duoc nhan (khong bi tra ve)
 * Kieu o lay tu cMap/&lt;id&gt; + &lt;bo&gt;.type (cung tep server doc). Ten tile tu du_lieu/bang_tile_moi.csv.
 *
 * Chay (o HTBB/, server thu co sv.server=0 hoac tai khoan GM):
 *   javac -d build/kiem_tai tools/pokemon/kiem_tai/BotTai.java tools/pokemon/kiem_tai/BotVaCham.java
 *   java -cp build/kiem_tai BotVaCham 127.0.0.1 &lt;cong&gt; &lt;tai_khoan&gt; [so_mau_moi_loai] [map ...]
 * Ma thoat 1 neu co o vat the di xuyen duoc / nhay qua tuong duoc / buoc hop le bi chan.
 */
public class BotVaCham extends BotTai {
    /** Vi tri cua chinh minh do server tra (lenh 4, cat 0) hoac goi vao map (lenh 12). */
    volatile int svX = -1, svY = -1, svMap = -1;
    volatile int soTra, soMap;

    BotVaCham(final String u) {
        super(u, 0, true);
    }

    @Override
    void xuLy(final int cmd, final byte[] d) throws java.io.IOException {
        super.xuLy(cmd, d);
        try {
            final DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            if (cmd == 4) {
                final int cat = r.readByte();
                r.readByte();
                r.readShort();
                final int qx = r.readShort(), qy = r.readShort();
                if (cat == 0) {
                    svX = qx;
                    svY = qy;
                    soTra++;
                }
            } else if (cmd == 12) {
                svMap = r.readShort();
                svX = r.readShort();
                svY = r.readShort();
                soMap++;
            }
        } catch (final Exception ignored) {
        }
    }

    static String[] ten(final String bo) throws Exception {
        final List<String> d = new ArrayList<>();
        for (final String l : Files.readAllLines(new File("du_lieu/bang_tile_moi.csv").toPath())) {
            final String[] p = l.split(",", 5);
            if (p[0].equals(bo)) {
                d.add(p[2]);
            }
        }
        return d.toArray(new String[0]);
    }

    static String loai(final String t) {
        if (t.startsWith("nha_") || t.startsWith("trung_tam") || t.startsWith("cua_hang")) return "nha";
        if (t.startsWith("tuong_") && !t.startsWith("tuong_go") && !t.startsWith("tuong_toi")) return "tuong";
        if (t.startsWith("cay") || t.startsWith("dua")) return "cay";
        if (t.startsWith("W") || t.equals("sen")) return "nuoc";
        if (t.startsWith("R") || t.startsWith("V") || t.contains("_mat_")) return "vach";
        if (t.equals("den")) return "den";
        if (t.startsWith("rao")) return "rao";
        if (t.startsWith("co") && t.contains("_")) return "co";
        return "khac";
    }

    int hong;
    final StringBuilder bao = new StringBuilder();

    /**
     * Gui buoc toi (px,py) roi mot buoc chac chan bi tu choi (0,0: xa hon nguong khoang cach) va doi server tra loi xong
     * (may qua tai: tra loi co the cham vai giay). Vi tri cuoi server bao = vi tri that. Tra ve true neu server KHONG nhan buoc.
     */
    boolean buoc(final int px, final int py) throws Exception {
        final int[] p = guiRoiHoi(px, py);
        return p == null || p[0] != px || p[1] != py;
    }

    int[] guiRoiHoi(final int px, final int py) throws Exception {
        nhanTrong(150);
        final int truoc = soTra;
        if (px >= 0) {
            gui(4, goi((short) px, (short) py));
        }
        gui(4, goi((short) 0, (short) 0));
        final long het = System.currentTimeMillis() + 8000;
        int daThay = soTra;
        long yen = 0;
        while (System.currentTimeMillis() < het) {
            nhanTrong(100);
            if (soTra != daThay) {
                daThay = soTra;
                yen = System.currentTimeMillis();
            }
            if (soTra != truoc && System.currentTimeMillis() - yen > 900) {
                break;
            }
        }
        return soTra != truoc ? new int[]{svX, svY} : null;
    }

    /** GM mto roi bao "vao map xong" (lenh 12, -500) nhu client that; tra ve true neu nhan duoc map. */
    boolean den(final int id, final int x, final int y) throws Exception {
        final int truoc = soMap;
        gui(27, goi("mto " + id + " " + x + " " + y));
        final long het = System.currentTimeMillis() + 6000;
        while (soMap == truoc && System.currentTimeMillis() < het) {
            nhanTrong(100);
        }
        if (soMap == truoc) {
            return false;
        }
        gui(12, goi((short) -500, (short) 0, (short) 0));
        nhanTrong(600);
        return svMap == id;
    }

    /** Vi tri server (buoc toi (0,0) luon bi tu choi va tra vi tri). */
    int[] viTri() throws Exception {
        return guiRoiHoi(-1, -1);
    }

    /** BFS 4 huong tren o di duoc trong cua so +-r quanh A: A toi duoc C khong. */
    static boolean noiGan(final java.util.function.IntPredicate di, final int w, final int h, final int ax, final int ay, final int cx,
                          final int cy, final int r) {
        final java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        final java.util.HashSet<Integer> da = new java.util.HashSet<>();
        q.add(new int[]{ax, ay});
        da.add(ay * w + ax);
        while (!q.isEmpty()) {
            final int[] c = q.poll();
            if (c[0] == cx && c[1] == cy) {
                return true;
            }
            for (final int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                final int x = c[0] + d[0], y = c[1] + d[1];
                if (x < 0 || y < 0 || x >= w || y >= h || Math.abs(x - ax) > r || Math.abs(y - ay) > r || !di.test(y * w + x)
                        || !da.add(y * w + x)) {
                    continue;
                }
                q.add(new int[]{x, y});
            }
        }
        return false;
    }

    void kiemMap(final int id, final int soMau) throws Exception {
        final String bo = id > 200 ? "t_thanh" : id < 110 ? "t" : "t_hang";
        final String[] tn = ten(bo);
        final byte[] k = Files.readAllBytes(new File("server/cMap/" + bo + ".type").toPath());
        final byte[] b = Files.readAllBytes(new File("server/cMap/" + id).toPath());
        final int w = b[0] & 255, h = b[1] & 255;
        final int[] t = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            t[i] = b[2 + i] & 255;
        }
        final java.util.function.IntPredicate di = i -> t[i] < 254 && t[i] < k.length && (k[t[i]] & 2) == 0;
        // mau: {loai -> [ax, ay, dx, dy]} o di duoc A, huong d toi o vat the B; C = A + 2d (nhay qua tuong neu di duoc)
        final Map<String, List<int[]>> mau = new TreeMap<>();
        final int[][] H = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int y = 2; y < h - 2; y++) {
            for (int x = 2; x < w - 2; x++) {
                if (!di.test(y * w + x)) continue;
                for (final int[] d : H) {
                    final int bi = (y + d[1]) * w + x + d[0];
                    if (di.test(bi) || t[bi] >= tn.length) continue;
                    final String l = loai(tn[t[bi]]);
                    final List<int[]> ds = mau.computeIfAbsent(l, z -> new ArrayList<>());
                    if (ds.size() < 400) ds.add(new int[]{x, y, d[0], d[1]});
                }
            }
        }
        bao.append("map ").append(id).append(':');
        int nMau = 0;
        for (final Map.Entry<String, List<int[]>> e : mau.entrySet()) {
            final List<int[]> ds = e.getValue();
            int ok = 0, n = 0;
            for (int j = 0; j < soMau && j < ds.size(); j++) {
                final int[] a = ds.get((int) ((long) j * ds.size() / Math.min(soMau, ds.size())));
                den(id, a[0], a[1]);
                final int[] p = viTri();
                if (System.getProperty("go") != null) {
                    System.out.println("  mto " + id + " " + a[0] + "," + a[1] + " -> server " + (p == null ? "khong tra" : p[0] + "," + p[1]) + " map " + svMap);
                }
                if (p == null || p[0] / 16 != a[0] || p[1] / 16 != a[1]) {
                    // map co diem hoi sinh co dinh (0, 70...): mto bo qua toa do - bo mau
                    continue;
                }
                n++;
                final int ax = p[0], ay = p[1];
                boolean dung = true;
                // 1. vao o vat the
                if (!buoc(ax + a[2] * 16, ay + a[3] * 16)) {
                    dung = false;
                    hong++;
                    bao.append("\n  HONG ").append(e.getKey()).append(": di VAO o vat the (").append(a[0] + a[2]).append(',').append(a[1] + a[3])
                            .append(") ").append(tn[t[(a[1] + a[3]) * w + a[0] + a[2]]]);
                    den(id, a[0], a[1]);
                    viTri();
                }
                // 2. nhay qua: o C = A + 2d di duoc va B chan -> phai bi tu choi
                final int cx = a[0] + 2 * a[2], cy = a[1] + 2 * a[3];
                // (server cho phep vong qua dau vat can - Char.checkMoreLine*: khong phai xuyen tuong; chi kiem khi A va C khong
                // noi nhau trong cua so +-12 o)
                if (cx >= 0 && cy >= 0 && cx < w && cy < h && di.test(cy * w + cx) && !noiGan(di, w, h, a[0], a[1], cx, cy, 12)) {
                    if (!buoc(svX + a[2] * 32, svY + a[3] * 32)) {
                        dung = false;
                        hong++;
                        bao.append("\n  HONG ").append(e.getKey()).append(": NHAY qua o (").append(a[0] + a[2]).append(',').append(a[1] + a[3])
                                .append(") toi (").append(cx).append(',').append(cy).append(')');
                        den(id, a[0], a[1]);
                        viTri();
                    }
                }
                // 3. buoc hop le sang o di duoc ke ben (vuong goc voi huong vat the) -> khong bi tra
                for (final int[] d : H) {
                    final int qx = a[0] + d[0], qy = a[1] + d[1];
                    if ((d[0] != a[2] || d[1] != a[3]) && di.test(qy * w + qx) && t[qy * w + qx] < tn.length) {
                        if (buoc(svX + d[0] * 16, svY + d[1] * 16)) {
                            dung = false;
                            hong++;
                            bao.append("\n  HONG ").append(e.getKey()).append(": buoc hop le (").append(a[0]).append(',').append(a[1]).append(") -> (")
                                    .append(qx).append(',').append(qy).append(") ").append(tn[t[qy * w + qx]]).append(" bi chan");
                        }
                        break;
                    }
                }
                if (dung) ok++;
            }
            nMau += n;
            bao.append(' ').append(e.getKey()).append(' ').append(ok).append('/').append(n);
        }
        bao.append(nMau == 0 ? " (khong lay duoc mau: mto bo qua toa do)" : "").append('\n');
        System.out.print(bao);
        bao.setLength(0);
    }

    public static void main(final String[] a) throws Exception {
        HOST = a[0];
        PORT = Integer.parseInt(a[1]);
        final BotVaCham bot = new BotVaCham(a[2]);
        final int soMau = a.length > 3 ? Integer.parseInt(a[3]) : 3;
        bot.s = new java.net.Socket();
        bot.s.connect(new java.net.InetSocketAddress(HOST, PORT), 10000);
        bot.in = new DataInputStream(new java.io.BufferedInputStream(bot.s.getInputStream()));
        bot.out = bot.s.getOutputStream();
        bot.gui(-40, new byte[0]);
        final Object[] k = bot.doc();
        bot.xuLy((Integer) k[0], (byte[]) k[1]);
        bot.gui(1, goi(bot.user, "123456", "2.7.6", "nokia/1/1", "0", "0", "0", (byte) 4, (short) 800, (byte) 0, (byte) 0, "vacham-" + bot.user));
        bot.nhanTrong(3000);
        if (bot.charId < 0) {
            bot.gui(14, goi(bot.user, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0));
            bot.nhanTrong(4000);
        }
        if (bot.charId < 0) {
            System.out.println("khong vao duoc game");
            System.exit(2);
        }
        bot.gui(13, goi((byte) 0, bot.charId));
        bot.nhanTrong(3000);
        bot.gui(12, goi((short) -500, (short) 0, (short) 0)); // client bao vao map xong
        bot.nhanTrong(1500);
        bot.gui(27, goi("slv " + bot.user + " 90 0"));
        bot.nhanTrong(1500);
        for (int i = 4; i < a.length; i++) {
            bot.kiemMap(Integer.parseInt(a[i]), soMau);
        }
        System.out.println("BotVaCham: HONG " + bot.hong);
        bot.s.close();
        System.exit(bot.hong > 0 ? 1 : 0);
    }
}
