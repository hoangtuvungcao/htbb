import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.htbb.pokemon.DinhDangBanDo;

/**
 * Giai ma MOI ban do cMap/&lt;id&gt; + luoi quai map/map&lt;id&gt; -> JSON + anh xem truoc ve bang tile PMD
 * hien tai. Dinh dang: docs/kien-truc.md; bo doc/ghi dung chung:
 * src/com/htbb/pokemon/DinhDangBanDo.java (tool kiem doc -> ghi lai trung tung byte).
 *
 * Chay (o thu muc HTBB/; can ban build server de doc bang cong/den - neu thieu thi bo qua phan do):
 *   javac -encoding UTF-8 -cp &lt;lop server&gt; -sourcepath src -d build/tools tools/pokemon/GiaiMaMap.java
 *   java -cp build/tools:&lt;lop server&gt;:lib/htbb-core.jar GiaiMaMap [thuMucCMap] [thuMucRa]
 *     mac dinh: server/cMap -> du_lieu/map_giai (anh &lt;id&gt;.png + &lt;id&gt;.json, khong commit) + du_lieu/bang_den.json
 */
public final class GiaiMaMap {

    /** Ban do GOC ve bang tile goc (map/t*.png); ban moi (VeMapMoi) ve bang map/pokemon_tile/. */
    static final String THU_MUC_TILE = "server/map/";
    static final String THU_MUC_CAY = "server/map/tree/";
    static final String THU_MUC_CAY_GOC = "server/map/tree/";

    public static void main(final String[] a) throws Exception {
        final String vao = a.length > 0 ? a[0] : "server/cMap";
        final String ra = a.length > 1 ? a[1] : "du_lieu/map_giai";
        new File(ra).mkdirs();
        final List<Integer> ids = danhSachMap(vao);
        final BangDen den = BangDen.doc();
        int loi = 0;
        for (final int id : ids) {
            final byte[] b = Files.readAllBytes(new File(vao, "" + id).toPath());
            final DinhDangBanDo m = DinhDangBanDo.doc(b);
            if (!Arrays.equals(b, m.ghi())) {
                System.out.println("LOI: map " + id + " ghi lai khong trung byte");
                loi++;
            }
            final DinhDangBanDo.LuoiQuai q = docQuai(id);
            final int[] kieu = kieuTile(DinhDangBanDo.boTile(id));
            Files.write(new File(ra, id + ".json").toPath(), json(id, m, q, kieu, den).getBytes(StandardCharsets.UTF_8));
            ImageIO.write(ve(id, m, q, den, true), "png", new File(ra, id + ".png"));
        }
        if (den.co) {
            Files.write(new File("du_lieu/bang_den.json").toPath(), den.json().getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("da giai ma " + ids.size() + " ban do -> " + ra + (loi > 0 ? (", LOI " + loi) : ", doc->ghi trung tung byte")
                + (den.co ? "" : " (KHONG doc duoc bang cong/den cua server - thieu lop real.*)"));
        // lop server (real.Map) mo luong khi nap - phai thoat han
        System.exit(loi > 0 ? 1 : 0);
    }

    static List<Integer> danhSachMap(final String thuMuc) {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final String f : new File(thuMuc).list()) {
            if (f.matches("\\d+")) {
                ids.add(Integer.parseInt(f));
            }
        }
        java.util.Collections.sort(ids);
        return ids;
    }

    static DinhDangBanDo.LuoiQuai docQuai(final int id) throws IOException {
        final File f = new File("server/map/map" + id);
        return f.isFile() ? DinhDangBanDo.LuoiQuai.doc(Files.readAllBytes(f.toPath())) : null;
    }

    static final Map<String, int[]> KIEU = new HashMap<String, int[]>();

    /** cMap/&lt;bo&gt;.type: 1 byte / tile; 0 di duoc, 2 chan (client: (type &amp; 2) == 2 la chan). */
    static int[] kieuTile(final String bo) throws IOException {
        int[] k = KIEU.get(bo);
        if (k == null) {
            final byte[] b = Files.readAllBytes(new File("server/cMap/" + bo + ".type").toPath());
            k = new int[b.length];
            for (int i = 0; i < b.length; i++) {
                k[i] = b[i] & 0xFF;
            }
            KIEU.put(bo, k);
        }
        return k;
    }

    static final Map<String, BufferedImage[]> TILE = new HashMap<String, BufferedImage[]>();

    static BufferedImage[] tile(final String thuMuc, final String bo) throws IOException {
        BufferedImage[] t = TILE.get(thuMuc + bo);
        if (t == null) {
            final BufferedImage anh = ImageIO.read(new File(thuMuc + bo + ".png"));
            t = new BufferedImage[anh.getHeight() / 16];
            for (int i = 0; i < t.length; i++) {
                t[i] = anh.getSubimage(0, i * 16, 16, 16);
            }
            TILE.put(thuMuc + bo, t);
        }
        return t;
    }

    /** Anh vat trang tri map/tree/&lt;id&gt; (u16 dai, PNG, u16 dai, dx, dy, startx, starty, endx, endy). */
    static final class Cay {
        BufferedImage anh;
        int dx, dy;
    }

    static final Map<Integer, Cay> CAY = new HashMap<Integer, Cay>();

    static Cay cay(final int id) {
        if (CAY.containsKey(id)) {
            return CAY.get(id);
        }
        Cay c = null;
        File f = new File(THU_MUC_CAY + id);
        if (!f.isFile()) {
            f = new File(THU_MUC_CAY_GOC + id);
        }
        try {
            final byte[] b = Files.readAllBytes(f.toPath());
            final int n = ((b[0] & 0xFF) << 8) | (b[1] & 0xFF);
            c = new Cay();
            c.anh = ImageIO.read(new ByteArrayInputStream(b, 2, n));
            c.dx = b[2 + n + 2];
            c.dy = b[2 + n + 3];
        } catch (final Exception e) {
            c = null;
        }
        CAY.put(id, c);
        return c;
    }

    /** Ve ban do nhu client: tile nen -> cay (sap theo chan) -> tileTop; ghiChu = danh dau cong/NPC/quai/diem den. */
    static BufferedImage ve(final int id, final DinhDangBanDo m, final DinhDangBanDo.LuoiQuai q, final BangDen den, final boolean ghiChu)
            throws IOException {
        return ve(id, m, q, den, ghiChu, THU_MUC_TILE);
    }

    static BufferedImage ve(final int id, final DinhDangBanDo m, final DinhDangBanDo.LuoiQuai q, final BangDen den, final boolean ghiChu,
                            final String thuMucTile) throws IOException {
        final BufferedImage[] t = tile(thuMucTile, DinhDangBanDo.boTile(id));
        final BufferedImage anh = new BufferedImage(m.w * 16, m.h * 16, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = anh.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, anh.getWidth(), anh.getHeight());
        for (int y = 0; y < m.h; y++) {
            for (int x = 0; x < m.w; x++) {
                final int v = m.o(x, y);
                if (!DinhDangBanDo.trong(v) && v < t.length) {
                    g.drawImage(t[v], x * 16, y * 16, null);
                }
            }
        }
        final List<DinhDangBanDo.Diem> cs = new ArrayList<DinhDangBanDo.Diem>(m.cay);
        cs.sort((p, r) -> Integer.compare(p.y, r.y));
        for (final DinhDangBanDo.Diem d : cs) {
            final Cay c = cay(d.v);
            if (c != null && c.anh != null) {
                g.drawImage(c.anh, (d.x << 4) + c.dx, (d.y << 4) + c.dy, null);
            }
        }
        for (final DinhDangBanDo.Khoi k : m.khoiTop) {
            for (int yy = 0; yy < k.h; yy++) {
                for (int xx = 0; xx < k.w; xx++) {
                    final int v = k.tile[yy * k.w + xx];
                    if (v != 255 && v < t.length) {
                        g.drawImage(t[v], (k.x + xx) * 16, (k.y + yy) * 16, null);
                    }
                }
            }
        }
        for (final DinhDangBanDo.Diem d : m.topLe) {
            if (d.v != 255 && d.v < t.length) {
                g.drawImage(t[d.v], d.x * 16, d.y * 16, null);
            }
        }
        if (ghiChu) {
            ghiChu(g, id, m, q, den);
        }
        g.dispose();
        return anh;
    }

    static void ghiChu(final Graphics2D g, final int id, final DinhDangBanDo m, final DinhDangBanDo.LuoiQuai q, final BangDen den) {
        if (q != null && q.w == m.w && q.h == m.h) {
            g.setColor(new Color(255, 40, 40));
            for (int y = 0; y < q.h; y++) {
                for (int x = 0; x < q.w; x++) {
                    if (q.coQuai(x, y)) {
                        g.fillOval(x * 16 + 4, y * 16 + 4, 8, 8);
                    }
                }
            }
        }
        g.setStroke(new BasicStroke(2));
        g.setColor(new Color(255, 0, 255));
        for (final DinhDangBanDo.Cong c : m.cong) {
            g.drawRect(c.x * 16 + 1, c.y * 16 + 1, 14, 14);
        }
        g.setColor(new Color(255, 230, 0));
        for (final DinhDangBanDo.Diem d : m.npc) {
            g.fillRect(d.x * 16 + 3, d.y * 16 + 3, 10, 10);
        }
        g.setColor(new Color(0, 220, 255));
        for (final int[] d : den.diem(id)) {
            g.drawOval(d[0] * 16 + 1, d[1] * 16 + 1, 14, 14);
        }
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("map " + id + " (" + DinhDangBanDo.boTile(id) + ", " + m.w + "x" + m.h + ")", 6, 16);
    }

    static String json(final int id, final DinhDangBanDo m, final DinhDangBanDo.LuoiQuai q, final int[] kieu, final BangDen den) {
        final StringBuilder s = new StringBuilder();
        s.append("{\n \"id\": ").append(id).append(",\n \"boTile\": \"").append(DinhDangBanDo.boTile(id)).append("\",\n \"w\": ")
                .append(m.w).append(", \"h\": ").append(m.h).append(", \"offline\": ").append(m.offline).append(",\n");
        s.append(" \"tile\": ").append(hangLuoi(m.tile, m.w)).append(",\n");
        final int[] k = new int[m.tile.length];
        for (int i = 0; i < k.length; i++) {
            k[i] = DinhDangBanDo.trong(m.tile[i]) || m.tile[i] >= kieu.length ? 0 : kieu[m.tile[i]];
        }
        s.append(" \"kieu\": ").append(hangLuoi(k, m.w)).append(",\n");
        s.append(" \"khoiTop\": [");
        for (int i = 0; i < m.khoiTop.size(); i++) {
            final DinhDangBanDo.Khoi b = m.khoiTop.get(i);
            s.append(i > 0 ? "," : "").append("\n  {\"x\":").append(b.x).append(",\"y\":").append(b.y).append(",\"w\":").append(b.w)
                    .append(",\"h\":").append(b.h).append(",\"tile\":").append(Arrays.toString(b.tile).replace(" ", "")).append("}");
        }
        s.append("],\n \"topLe\": ").append(diem(m.topLe, "tile")).append(",\n \"cay\": ").append(diem(m.cay, "id"));
        s.append(",\n \"cong\": [");
        for (int i = 0; i < m.cong.size(); i++) {
            final DinhDangBanDo.Cong c = m.cong.get(i);
            s.append(i > 0 ? "," : "").append("\n  {\"x\":").append(c.x).append(",\"y\":").append(c.y).append(",\"toM\":").append(c.toM)
                    .append(",\"xOut\":").append(c.xOut).append(",\"yOut\":").append(c.yOut).append("}");
        }
        s.append("],\n \"npc\": ").append(diem(m.npc, "loai"));
        s.append(",\n \"diemDen\": [");
        boolean dau = true;
        for (final int[] d : den.diem(id)) {
            s.append(dau ? "" : ",").append("[").append(d[0]).append(",").append(d[1]).append("]");
            dau = false;
        }
        s.append("]");
        if (q != null) {
            s.append(",\n \"quai\": {\"w\":").append(q.w).append(",\"h\":").append(q.h).append(",\"o\":").append(hangLuoi(q.o, q.w)).append("}");
        }
        s.append("\n}\n");
        return s.toString();
    }

    static String hangLuoi(final int[] v, final int w) {
        final StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < v.length; i += w) {
            s.append(i > 0 ? ",\n  " : "\n  ").append(Arrays.toString(Arrays.copyOfRange(v, i, Math.min(v.length, i + w))).replace(" ", ""));
        }
        return s.append("]").toString();
    }

    static String diem(final List<DinhDangBanDo.Diem> ds, final String ten) {
        final StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < ds.size(); i++) {
            final DinhDangBanDo.Diem d = ds.get(i);
            s.append(i > 0 ? "," : "").append("{\"x\":").append(d.x).append(",\"y\":").append(d.y).append(",\"").append(ten).append("\":")
                    .append(d.v).append("}");
        }
        return s.append("]").toString();
    }

    /**
     * Diem den co dinh do server viet cung (o tile): locationMap (qua cong), localXaphu (bay).
     * Doc bang phan chieu tu lop server da build (real.RealController.add_more_idMapSend2Client dat
     * gia tri that luc chay). Id ban do ban sao (301.., 1701..) quy ve ban do nap qua Map.mapID.
     */
    static final class BangDen {
        boolean co;
        final Map<Integer, List<int[]>> den = new TreeMap<Integer, List<int[]>>();

        List<int[]> diem(final int id) {
            final List<int[]> d = den.get(id);
            return d == null ? new ArrayList<int[]>() : d;
        }

        void them(final int id, final int x, final int y) {
            if (x <= 0 && y <= 0) {
                return;
            }
            List<int[]> d = den.get(id);
            if (d == null) {
                d = new ArrayList<int[]>();
                den.put(id, d);
            }
            for (final int[] c : d) {
                if (c[0] == x && c[1] == y) {
                    return;
                }
            }
            d.add(new int[]{x, y});
        }

        static BangDen doc() {
            final BangDen b = new BangDen();
            try {
                final Class<?> rc = Class.forName("real.RealController");
                final Class<?> mc = Class.forName("real.Map");
                final java.io.PrintStream cu = System.out;
                final java.io.PrintStream cuLoi = System.err;
                try {
                    System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
                    System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
                    final Method mm = rc.getMethod("add_more_idMapSend2Client");
                    mm.invoke(null);
                } catch (final Throwable e) {
                    // bang van duoc gan truoc khi doc cMap/28 hong - bo qua
                } finally {
                    System.setOut(cu);
                    System.setErr(cuLoi);
                }
                final short[][][] lm = (short[][][]) truong(mc, "locationMap");
                final short[][] xp = (short[][]) truong(mc, "localXaphu");
                final short[][] ids = (short[][]) truong(mc, "mapID");
                final Map<Integer, Integer> veGoc = new HashMap<Integer, Integer>();
                for (final short[] hang : ids) {
                    for (final short v : hang) {
                        veGoc.put((int) v, (int) hang[0]);
                    }
                }
                for (final short[][] nhom : lm) {
                    for (final short[] l : nhom) {
                        for (int i = 0; i + 2 < l.length; i += 3) {
                            final Integer g = veGoc.get((int) l[i]);
                            b.them(g == null ? l[i] : g, l[i + 1], l[i + 2]);
                        }
                    }
                }
                for (final short[] l : xp) {
                    for (int i = 0; i + 2 < l.length; i += 3) {
                        final Integer g = veGoc.get((int) l[i]);
                        b.them(g == null ? l[i] : g, l[i + 1], l[i + 2]);
                    }
                }
                b.co = true;
            } catch (final Throwable e) {
                b.co = false;
            }
            return b;
        }

        static Object truong(final Class<?> c, final String ten) throws Exception {
            final Field f = c.getDeclaredField(ten);
            f.setAccessible(true);
            return f.get(null);
        }

        String json() {
            final StringBuilder s = new StringBuilder("{\n");
            boolean dau = true;
            for (final Map.Entry<Integer, List<int[]>> e : den.entrySet()) {
                s.append(dau ? " " : ",\n ").append("\"").append(e.getKey()).append("\": [");
                for (int i = 0; i < e.getValue().size(); i++) {
                    s.append(i > 0 ? "," : "").append("[").append(e.getValue().get(i)[0]).append(",").append(e.getValue().get(i)[1]).append("]");
                }
                s.append("]");
                dau = false;
            }
            return s.append("\n}\n").toString();
        }
    }
}
