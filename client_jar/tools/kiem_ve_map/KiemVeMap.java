import game.pokemon.PokeBody;
import game.pokemon.PokeDuLieu;
import game.pokemon.VatThePokemon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Kiem tu dong cach client J2ME VE moi ban do che do Pokemon (khong can may ao).
 *
 * Dung lai dung ma ve cua client: lop game.pokemon.PokeBody / PokeDuLieu / VatThePokemon bien dich
 * cung voi stub javax.microedition.lcdui tren AWT (kiem_ve_map/stub). Phan con lai chep dung phep tinh
 * cua game/model/Tilemap.loadMap + paintTile + TileTop.paint + Tree.paint + NPC.paint (vi tri chan
 * (tx*16+8, ty*16+8), Pokedex theo NPC.POKE_DEX_THEO_TYPE doc thang tu NPC.java).
 *
 * Moi ban do HTBB/cMap/&lt;id&gt;:
 *   1. lop tile ve bang bo tile cua JAR (res/pt) so tung pixel voi anh xem truoc cua generator
 *      (du_lieu/map_moi/&lt;id&gt;.png) - lech = jar/server/anh xem truoc khong cung bo.
 *   2. chi so tile ngoai bo (drawRegion nem loi -> client bo ca lop tile), tileTop/cay (ban moi phai rong).
 *   3. vat the bi CAT: pixel vat the cham mep o ma o ben canh la nen tron (vd tuong quang truong lay sai o).
 *   4. "lo nen": vat the ve tren nen khac nen xung quanh (vd den co nen co dat giua san lat da).
 *   5. va cham: tile vat the (nhieu pixel khac nen) ma di duoc / tile nen tron ma bi chan (tuong vo hinh);
 *      ca hai client va server cung doc &lt;bo&gt;.type nen kiem .type cua jar == .type cua server.
 *   6. NPC: dung o chan, thieu sprite (ve kieu cu), sprite trum xuong hang duoi (sai chieu sau).
 *
 * Chay: sh client_jar/tools/kiem_ve_map/chay.sh [thu_muc_ra]
 * Ra: &lt;ra&gt;/kiem_ve_map.txt, &lt;ra&gt;/ve/&lt;id&gt;.png (ve nhu client, co NPC), &lt;ra&gt;/lech/&lt;id&gt;.png (o loi to do).
 * Ma thoat 1 neu co loi.
 */
public final class KiemVeMap {
    static File htbb, res, ra;
    static final Map<String, String[]> TEN = new HashMap<String, String[]>();
    static final Map<String, int[]> KIEU = new HashMap<String, int[]>();
    static final Map<String, BufferedImage> BO = new HashMap<String, BufferedImage>();
    /** bo -&gt; [tile] -&gt; chi so tile nen (-1 = khong co nen / chinh no la nen). */
    static final Map<String, int[]> NEN = new HashMap<String, int[]>();
    /** bo -&gt; [tile] -&gt; mat na pixel vat the (khac nen). */
    static final Map<String, boolean[][]> MAT = new HashMap<String, boolean[][]>();
    /** bo -&gt; [tile] -&gt; {bang, x, y} nguon Ninja Adventure (null = ve bang code / nen). */
    static final Map<String, String[][]> NGUON = new HashMap<String, String[][]>();
    /**
     * Tile da xem bang mat (map_qa/cat_bo_t.png): sprite tron ven, chi CHAM sprite ke ben trong bang nguon (nha xep sat nhau,
     * hang rao/van go noi lien, do dac, cay rung/bui/da cham la cay ben canh, dua cham la mot dua khac). Them vao day chi sau khi xem anh.
     */
    static final Pattern DA_DUYET = Pattern.compile("(nha_cam|nha_be|trung_tam|cua_hang|nha_xanh)_\\d_\\d|rao_(trai|giua|phai|doc)|tuong_go"
            + "|e\\d+_\\d+_go|cayB_3|cayT_3|bui|cay_mong|dua_[0-3]|co[0-3]_[01]");
    static final Map<String, String[][]> NHOM = new HashMap<String, String[][]>();
    static final Map<String, String[]> CAT_BO = new HashMap<String, String[]>();
    static final Map<String, BufferedImage> BANG = new HashMap<String, BufferedImage>();
    static final StringBuilder bao = new StringBuilder();
    static int loi, canhBao;

    /** Tep cua thu muc server (cMap, map/...) hoac cua goc repo (du_lieu/, tools/) - thu server truoc. */
    static File tim(final String rel) {
        final File f = new File(htbb, rel);
        return f.exists() ? f : new File(htbb.getAbsoluteFile().getParentFile(), rel);
    }

    public static void main(final String[] a) throws Exception {
        htbb = new File(a[0]);
        res = new File(a[1]);
        ra = new File(a[2]);
        new File(ra, "ve").mkdirs();
        new File(ra, "lech").mkdirs();
        docBangTile();
        final short[] dexNpc = docDexNpc(new File(System.getProperty("j2me.src", new File(res.getParentFile(), "src").getPath()), "game/model/NPC.java"));
        for (final String bo : new String[]{"t", "t_hang", "t_thanh"}) {
            kiemBo(bo);
        }
        kiemVatThe();
        final TreeMap<Integer, File> ds = new TreeMap<Integer, File>();
        for (final File f : tim("cMap").listFiles()) {
            if (f.getName().matches("\\d+")) {
                ds.put(Integer.parseInt(f.getName()), f);
            }
        }
        final List<String> chiDinh = new ArrayList<String>();
        for (int i = 3; i < a.length; i++) {
            chiDinh.add(a[i]);
        }
        final StringBuilder tom = new StringBuilder();
        for (final Map.Entry<Integer, File> e : ds.entrySet()) {
            if (!chiDinh.isEmpty() && !chiDinh.contains("" + e.getKey())) {
                continue;
            }
            kiemMap(e.getKey(), Files.readAllBytes(e.getValue().toPath()), dexNpc, tom);
        }
        bao.insert(0, "KiemVeMap: " + ds.size() + " ban do, LOI " + loi + ", canh bao " + canhBao + "\n" + tom + "\n");
        Files.write(new File(ra, "kiem_ve_map.txt").toPath(), bao.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println(bao.length() > 6000 ? bao.substring(0, 6000) + "\n..." : bao);
        System.out.println("KiemVeMap: LOI " + loi + ", canh bao " + canhBao + " -> " + new File(ra, "kiem_ve_map.txt"));
        System.exit(loi > 0 ? 1 : 0);
    }

    static String boCua(final int id) {
        return id > 200 ? "t_thanh" : id < 110 ? "t" : "t_hang";
    }

    static void docBangTile() throws Exception {
        final Map<String, List<String>> ten = new HashMap<String, List<String>>();
        for (final String l : Files.readAllLines(tim("du_lieu/bang_tile_moi.csv").toPath(), StandardCharsets.UTF_8)) {
            final String[] p = l.split(",", 5);
            if (p.length < 4 || p[0].equals("bo_tile")) {
                continue;
            }
            List<String> d = ten.get(p[0]);
            if (d == null) {
                ten.put(p[0], d = new ArrayList<String>());
            }
            d.add(p[2]);
            List<String[]> ng = NG_TAM.get(p[0]);
            if (ng == null) {
                NG_TAM.put(p[0], ng = new ArrayList<String[]>());
            }
            final Matcher mm = Pattern.compile("^Ninja Adventure (Tileset\\w+) \\((\\d+),(\\d+)\\)$").matcher(p.length > 4 ? p[4] : "");
            ng.add(mm.find() ? new String[]{mm.group(1), mm.group(2), mm.group(3)} : null);
        }
        for (final Map.Entry<String, List<String[]>> e : NG_TAM.entrySet()) {
            NGUON.put(e.getKey(), e.getValue().toArray(new String[0][]));
        }
        for (final Map.Entry<String, List<String>> e : ten.entrySet()) {
            TEN.put(e.getKey(), e.getValue().toArray(new String[0]));
        }
    }

    static short[] docDexNpc(final File f) throws Exception {
        final String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        final Matcher m = Pattern.compile("POKE_DEX_THEO_TYPE\\s*=\\s*\\{([^}]*)\\}").matcher(s);
        if (!m.find()) {
            throw new IllegalStateException("khong doc duoc NPC.POKE_DEX_THEO_TYPE");
        }
        final String[] p = m.group(1).split(",");
        final short[] r = new short[p.length];
        for (int i = 0; i < p.length; i++) {
            r[i] = Short.parseShort(p[i].trim());
        }
        return r;
    }

    static void loi(final String s) {
        loi++;
        bao.append("  LOI ").append(s).append('\n');
    }

    static void canh(final String s) {
        canhBao++;
        bao.append("  canh bao ").append(s).append('\n');
    }

    static boolean laNuocVach(final String t) {
        return t.equals("X") || t.startsWith("W") || t.startsWith("R") || t.startsWith("V") || t.contains("_mat_") || t.contains("/")
                && (t.startsWith("W") || t.startsWith("RT") || t.startsWith("VT")) || t.equals("sen") || t.startsWith("tuong_toi")
                || t.startsWith("tuong_go");
    }

    /** Nen co ban: tile di duoc, ten 1-2 ky tu (G, G1, D, S, N, K, P, A, C, C1...). */
    static boolean laNen(final String t) {
        return t.matches("[A-Z][0-9]?") && !t.equals("X") && !t.startsWith("T");
    }

    static String hoNen(final String t) {
        return t.substring(0, 1);
    }

    /** Bo tile cua jar (res/pt) so voi server (map/pokemon_tile), .type jar so voi server; tim nen + mat na tung tile. */
    static void kiemBo(final String bo) throws Exception {
        final BufferedImage jar = ImageIO.read(new File(res, "pt/" + bo + ".png"));
        final BufferedImage sv = ImageIO.read(tim("map/pokemon_tile/" + bo + ".png"));
        if (!java.util.Arrays.equals(Files.readAllBytes(new File(res, "pt/" + bo + ".png").toPath()),
                Files.readAllBytes(tim("map/pokemon_tile/" + bo + ".png").toPath()))) {
            loi("bo " + bo + ": res/pt/" + bo + ".png cua jar khac map/pokemon_tile/" + bo + ".png cua server (chay cai_map_j2me.sh)");
        }
        final byte[] tj = Files.readAllBytes(new File(res, bo + ".type").toPath());
        final byte[] ts = Files.readAllBytes(tim("cMap/" + bo + ".type").toPath());
        if (!java.util.Arrays.equals(tj, ts)) {
            loi("bo " + bo + ": res/" + bo + ".type cua jar khac cMap/" + bo + ".type cua server (va cham client != server)");
        }
        final BufferedImage anh = new BufferedImage(16, jar.getHeight(), BufferedImage.TYPE_INT_ARGB);
        anh.createGraphics().drawImage(jar, 0, 0, null);
        BO.put(bo, anh);
        final int[] k = new int[ts.length];
        for (int i = 0; i < ts.length; i++) {
            k[i] = ts[i] & 255;
        }
        KIEU.put(bo, k);
        final String[] ten = TEN.get(bo);
        final int n = anh.getHeight() / 16;
        if (ten.length != n || ts.length != n) {
            loi("bo " + bo + ": " + n + " tile trong anh, " + ten.length + " ten trong bang_tile_moi.csv, " + ts.length + " trong .type");
        }
        final int[] nen = new int[n];
        final boolean[][] mat = new boolean[n][];
        final String[][] ngB = NGUON.get(bo);
        for (int t = 0; t < n; t++) {
            nen[t] = -1;
            if (laNen(ten[t])) {
                continue;
            }
            // pixel nen = pixel trong suot cua o sprite nguon (tren(nen, sprite)); tile ve bang code: moi pixel
            boolean[] trong = null;
            if (ngB[t] != null) {
                final BufferedImage sh = bang(ngB[t][0]);
                final int cx = Integer.parseInt(ngB[t][1]), cy = Integer.parseInt(ngB[t][2]);
                trong = new boolean[256];
                for (int i = 0; i < 256; i++) {
                    trong[i] = !dac(sh, cx * 16 + i % 16, cy * 16 + i / 16) && (sh.getRGB(cx * 16 + i % 16, cy * 16 + i / 16) >>> 24) == 0;
                }
            }
            int tot = -1, khop = 0, tong = 0;
            for (int b = 0; b < n; b++) {
                if (!laNen(ten[b])) {
                    continue;
                }
                int c = 0, tg = 0;
                for (int i = 0; i < 256; i++) {
                    if (trong != null && !trong[i]) {
                        continue;
                    }
                    tg++;
                    if (anh.getRGB(i % 16, t * 16 + i / 16) == anh.getRGB(i % 16, b * 16 + i / 16)) {
                        c++;
                    }
                }
                if (c > khop) {
                    khop = c;
                    tot = b;
                    tong = tg;
                }
            }
            // sprite nguon: >= 90% pixel trong suot trung nen; ve bang code: >= 1/4 so pixel
            if (tot >= 0 && (trong != null ? tong >= 16 && khop * 10 >= tong * 9 : khop >= 64)) {
                nen[t] = tot;
                mat[t] = new boolean[256];
                for (int i = 0; i < 256; i++) {
                    mat[t][i] = anh.getRGB(i % 16, t * 16 + i / 16) != anh.getRGB(i % 16, tot * 16 + i / 16);
                }
            }
        }
        NEN.put(bo, nen);
        // nhom vat the theo ten (bo hau to _so / _so_so) + cung bang sprite
        final String[][] ng = NGUON.get(bo);
        final Map<String, StringBuilder> nhom = new HashMap<String, StringBuilder>();
        final String[] khoa = new String[n];
        for (int t = 0; t < n; t++) {
            if (ng[t] == null || !laVatTheNguon(ng[t][0]) || !ten[t].matches(".*_\\d(_\\d)?")) {
                continue;
            }
            khoa[t] = ten[t].replaceAll("(_\\d)+$", "") + "@" + ng[t][0];
            StringBuilder sb = nhom.get(khoa[t]);
            if (sb == null) {
                nhom.put(khoa[t], sb = new StringBuilder(";"));
            }
            sb.append(ng[t][0]).append(',').append(ng[t][1]).append(',').append(ng[t][2]).append(';');
        }
        final String[][] nh = new String[n][];
        final String[] catBo = new String[n];
        for (int t = 0; t < n; t++) {
            final String tap = khoa[t] == null ? null : nhom.get(khoa[t]).toString();
            if (tap != null) {
                nh[t] = new String[]{khoa[t], tap};
            }
            if (ng[t] == null || !laVatTheNguon(ng[t][0]) || laDiDuocHopLe(ten[t]) && !ten[t].matches("(nha|trung|cua).*")) {
                continue;
            }
            final int cx = Integer.parseInt(ng[t][1]), cy = Integer.parseInt(ng[t][2]);
            for (int d = 0; d < 4; d++) {
                final String ben = ng[t][0] + "," + (cx + (d == 0 ? 1 : d == 1 ? -1 : 0)) + "," + (cy + (d == 2 ? 1 : d == 3 ? -1 : 0));
                if ((tap == null || !tap.contains(";" + ben + ";")) && noiLien(ng[t][0], cx, cy, d)) {
                    catBo[t] = (catBo[t] == null ? "" : catBo[t] + "; ") + "sprite " + ng[t][0] + " (" + cx + "," + cy + ") con tiep sang "
                            + "PTDL".charAt(d) + " (o " + ben + ") ma bo tile khong co";
                }
            }
            if (catBo[t] != null && DA_DUYET.matcher(ten[t]).matches()) {
                bao.append("  bo ").append(bo).append(" tile ").append(t).append(' ').append(ten[t]).append(": da duyet (chi cham sprite ke ben trong bang)\n");
                catBo[t] = null;
            } else if (catBo[t] != null) {
                bao.append("  bo ").append(bo).append(" tile ").append(t).append(' ').append(ten[t]).append(": ").append(catBo[t]).append('\n');
            }
        }
        NHOM.put(bo, nh);
        CAT_BO.put(bo, catBo);
        MAT.put(bo, mat);
    }

    static final class BanDo {
        int w, h;
        short[] map;
        int[] type;
        final List<int[]> top = new ArrayList<int[]>(), cay = new ArrayList<int[]>(), cong = new ArrayList<int[]>(), npc = new ArrayList<int[]>();
    }

    /** Chep Tilemap.loadMap (J2ME): cung thu tu doc, cung quy uoc 254/255, cong -&gt; type 2000000000+i. */
    static BanDo doc(final byte[] b, final int[] typeOfTile) {
        final BanDo m = new BanDo();
        final java.io.DataInputStream is = new java.io.DataInputStream(new java.io.ByteArrayInputStream(b));
        try {
            m.w = is.read();
            m.h = is.read();
            m.map = new short[m.w * m.h];
            m.type = new int[m.w * m.h];
            for (int i = 0; i < m.w * m.h; i++) {
                m.map[i] = (short) is.read();
                if (m.map[i] == 254) {
                    m.map[i] = 255;
                }
                if (m.map[i] != 255) {
                    m.type[i] = m.map[i] < typeOfTile.length ? typeOfTile[m.map[i]] : 0;
                }
            }
            while (true) {
                final int x = is.read();
                if (x == 255) {
                    break;
                }
                final int y = is.read(), ww = is.read(), hh = is.read();
                for (int yy = y; yy < y + hh; yy++) {
                    for (int xx = x; xx < x + ww; xx++) {
                        final int v = is.read();
                        if (v != 255) {
                            m.top.add(new int[]{xx, yy, v});
                        }
                    }
                }
            }
            while (true) {
                final int x = is.read();
                if (x == 255) {
                    break;
                }
                final int y = is.read(), v = is.read();
                if (v != 255) {
                    m.top.add(new int[]{x, y, v});
                }
            }
            while (true) {
                int x = is.read();
                if (x == 254) {
                    x = 255;
                }
                if (x == 255) {
                    break;
                }
                m.cay.add(new int[]{x, is.read(), is.read()});
            }
            is.read();
            final int n = is.read();
            for (int i = 0; i < n; i++) {
                final int x = is.read(), y = is.read();
                is.read();
                is.read();
                is.read();
                is.read();
                m.type[y * m.w + x] = 2000000000 + i;
                m.cong.add(new int[]{x, y, i});
            }
            final int nn = is.read();
            for (int i = 0; i < nn; i++) {
                m.npc.add(new int[]{is.read(), is.read(), is.read()});
            }
        } catch (final java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        return m;
    }

    static void kiemMap(final int id, final byte[] du, final short[] dexNpc, final StringBuilder tom) throws Exception {
        final String bo = boCua(id);
        final int[] kieu = KIEU.get(bo);
        final String[] ten = TEN.get(bo);
        final int[] nen = NEN.get(bo);
        final boolean[][] mat = MAT.get(bo);
        final BufferedImage boAnh = BO.get(bo);
        final BanDo m = doc(du, kieu);
        final int loi0 = loi, canh0 = canhBao;
        bao.append("map ").append(id).append(" (").append(bo).append(' ').append(m.w).append('x').append(m.h).append(")\n");
        final int W = m.w * 16, H = m.h * 16;
        final BufferedImage ve = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = new Graphics(ve);
        g.setColor(0);
        g.fillRect(0, 0, W, H);
        final Image imgTile = Image.createImage("/pt/" + bo + ".png");
        final boolean[] o = new boolean[m.w * m.h]; // o loi -> to do trong anh lech
        // ---- Tilemap.paintTile (mot o loi nem IllegalArgumentException -> client bo CA lop tile)
        int ngoaiBo = 0;
        for (int a = 0; a < m.w; a++) {
            for (int b = 0; b < m.h; b++) {
                final short s = m.map[b * m.w + a];
                if (s != 255) {
                    try {
                        g.drawRegion(imgTile, 0, s << 4, 16, 16, 0, a << 4, b << 4, 0);
                    } catch (final IllegalArgumentException e) {
                        ngoaiBo++;
                        o[b * m.w + a] = true;
                    }
                }
            }
        }
        if (ngoaiBo > 0) {
            loi("chi so tile ngoai bo " + bo + " o " + ngoaiBo + " o -> paintTile nem loi, client khong ve lop tile");
        }
        // ---- so lop tile voi anh xem truoc cua generator
        final File xt = tim("du_lieu/map_moi/" + id + ".png");
        int lech = 0;
        if (!xt.isFile()) {
            loi("thieu anh xem truoc " + xt);
        } else {
            final BufferedImage p = ImageIO.read(xt);
            if (p.getWidth() != W || p.getHeight() != H) {
                loi("anh xem truoc " + p.getWidth() + "x" + p.getHeight() + " != " + W + "x" + H);
            } else {
                for (int y = 0; y < H; y++) {
                    for (int x = 0; x < W; x++) {
                        if ((p.getRGB(x, y) & 0xFFFFFF) != (ve.getRGB(x, y) & 0xFFFFFF)) {
                            lech++;
                            o[(y >> 4) * m.w + (x >> 4)] = true;
                        }
                    }
                }
                if (lech > 0) {
                    loi("ve client lech anh xem truoc " + lech + " pixel (bo tile jar/server/anh xem truoc khong khop)");
                }
            }
        }
        // ---- tileTop / cay (ban moi khong dung)
        if (!m.top.isEmpty()) {
            canh("co " + m.top.size() + " tileTop (ve de len nhan vat) - ban moi khong dung, xem lai");
            for (final int[] t : m.top) {
                if (t[2] >= kieu.length) {
                    loi("tileTop (" + t[0] + "," + t[1] + ") chi so " + t[2] + " ngoai bo");
                }
            }
        }
        if (!m.cay.isEmpty()) {
            canh("co " + m.cay.size() + " cay map/tree (khong chan, ve theo hang) - ban moi khong dung");
        }
        // ---- vat the cat / lo nen / va cham
        int cat = 0, catBo = 0, loNen = 0, loNenNhom = 0, viHinh = 0, diXuyen = 0;
        final StringBuilder vdCat = new StringBuilder(), vdNen = new StringBuilder(), vdVc = new StringBuilder();
        final String[][] ng = NGUON.get(bo);
        for (int y = 0; y < m.h; y++) {
            for (int x = 0; x < m.w; x++) {
                final int i = y * m.w + x, s = m.map[i];
                if (s >= ten.length) {
                    continue;
                }
                final boolean chan = (kieu[s] & 2) == 2;
                // tuong vo hinh: nen tron (G, D, K...) bi chan
                if (laNen(ten[s]) && chan && !ten[s].startsWith("W") && !ten[s].equals("X")) {
                    viHinh++;
                    o[i] = true;
                    them(vdVc, "tuong vo hinh " + ten[s] + " (" + x + "," + y + ")");
                }
                final String[] src = ng[s] != null ? ng[s] : new String[]{"-", "0", "0"};
                // vat the tu bang sprite (Nature/House/Element/Desert) ma di duoc
                if (!chan && laVatTheNguon(src[0]) && !laDiDuocHopLe(ten[s]) && !laCong(m, x, y)) {
                    diXuyen++;
                    o[i] = true;
                    them(vdVc, "vat the di xuyen " + ten[s] + " (" + x + "," + y + ")");
                }
                // nhom vat the (cay 2x2, tuong, nha w x 3): manh ke ben trong nhom phai dung o tren map
                final String[] nh = NHOM.get(bo)[s];
                if (nh != null && ng[s] != null) {
                    for (int d = 0; d < 4; d++) {
                        final int dx = d == 0 ? 1 : d == 1 ? -1 : 0, dy = d == 2 ? 1 : d == 3 ? -1 : 0;
                        final String muon = src[0] + "," + (Integer.parseInt(src[1]) + dx) + "," + (Integer.parseInt(src[2]) + dy);
                        if (!nh[1].contains(";" + muon + ";")) {
                            continue;
                        }
                        if (laCotGiuaNha(ten, m, x + dx, y + dy, s, d < 2)) {
                            continue;
                        }
                        final int xx = x + dx, yy = y + dy;
                        String co = "ngoai ban do";
                        if (xx >= 0 && yy >= 0 && xx < m.w && yy < m.h) {
                            final int s2 = m.map[yy * m.w + xx];
                            final String[] src2 = s2 < ng.length ? ng[s2] : null;
                            if (src2 != null && (src2[0] + "," + src2[1] + "," + src2[2]).equals(muon)) {
                                continue;
                            }
                            co = s2 < ten.length ? ten[s2] : "#" + s2;
                        }
                        cat++;
                        o[i] = true;
                        them(vdCat, "nhom " + nh[0] + " thieu manh: " + ten[s] + " (" + x + "," + y + ") huong " + "PTDL".charAt(d) + " co " + co);
                    }
                }
                if (CAT_BO.get(bo)[s] != null) {
                    catBo++;
                    o[i] = true;
                    them(vdCat, "dat tile sprite bi cat " + ten[s] + " (" + x + "," + y + "): " + CAT_BO.get(bo)[s]);
                }
                // lo nen: vat the tren nen ho khac nen tron ke ben (vd den nen co giua san lat da)
                final boolean[] mk = mat[s];
                if (mk == null || nen[s] < 0 || ten[s].contains("/")) {
                    continue;
                }
                int benKhac = 0, benCung = 0;
                String vdBen = "";
                for (int d = 0; d < 4; d++) {
                    final int xx = x + (d == 0 ? 1 : d == 1 ? -1 : 0), yy = y + (d == 2 ? 1 : d == 3 ? -1 : 0);
                    if (xx < 0 || yy < 0 || xx >= m.w || yy >= m.h) {
                        continue;
                    }
                    final int s2 = m.map[yy * m.w + xx];
                    if (s2 < ten.length && laNen(ten[s2])) {
                        if (hoNen(ten[s2]).equals(hoNen(ten[nen[s]]))) {
                            benCung++;
                        } else {
                            benKhac++;
                            vdBen = ten[s2];
                        }
                    }
                }
                if (benKhac >= 2 && benCung == 0 && NHOM.get(bo)[s] != null) {
                    // goc cay 2x2 / nha o mep bai co: goc vuong thay vi bo tron - chi canh bao
                    loNenNhom++;
                    them(vdNen, "goc vuong " + ten[s] + " (nen " + ten[nen[s]] + ") tai (" + x + "," + y + ") giua nen " + vdBen);
                } else if (benKhac >= 2 && benCung == 0) {
                    loNen++;
                    o[i] = true;
                    them(vdNen, "lo nen " + ten[s] + " (nen " + ten[nen[s]] + ") tai (" + x + "," + y + ") giua nen " + vdBen);
                }
            }
        }
        if (cat > 0) {
            loi(cat + " canh nhom vat the thieu manh ke ben (ve cat ngang)");
        }
        if (catBo > 0) {
            loi(catBo + " o dung tile ma sprite nguon bi cat (xem dau bao cao: bo tile)");
        }
        if (loNen > 0) {
            loi(loNen + " vat the co nen khac nen xung quanh (o vuong mau la)");
        }
        if (loNenNhom > 0) {
            canh(loNenNhom + " o cay/nha nhieu o o mep vung nen khac (goc vuong)");
        }
        if (viHinh > 0) {
            loi(viHinh + " o nen tron bi chan (tuong vo hinh)");
        }
        if (diXuyen > 0) {
            loi(diXuyen + " o vat the di xuyen duoc (client + server cung doc .type)");
        }
        bao.append(vdCat).append(vdNen).append(vdVc);
        // ---- NPC (NPC.paint: bong + PokeBody tai chan (tx*16+8, ty*16+8)); sap theo y nhu Util.quickSort
        m.npc.sort((p, q) -> p[1] - q[1]);
        int npcLoi = 0;
        final BufferedImage nenVe = copy(ve);
        for (final int[] n : m.npc) {
            final int x = n[0] * 16 + 8, y = n[1] * 16 + 8, i = n[1] * m.w + n[0];
            if (n[0] >= m.w || n[1] >= m.h) {
                loi("NPC loai " + n[2] + " ngoai ban do (" + n[0] + "," + n[1] + ")");
                continue;
            }
            final int s = m.map[i];
            if (s < kieu.length && (kieu[s] & 2) == 2) {
                loi("NPC loai " + n[2] + " dung tren o chan " + ten[s] + " (" + n[0] + "," + n[1] + ")");
                o[i] = true;
            }
            final int dex = n[2] >= 0 && n[2] < dexNpc.length ? dexNpc[n[2]] : 0;
            boolean daVe = false;
            final BufferedImage truoc = copy(ve);
            try {
                daVe = dex > 0 && PokeBody.coSprite(dex, false) && PokeBody.paint(g, dex, false, x, y, 0, PokeDuLieu.DUNG, 0, 100);
            } catch (final IllegalArgumentException e) {
                loi("NPC loai " + n[2] + " dex " + dex + ": drawRegion ngoai anh " + e.getMessage());
            }
            if (!daVe) {
                npcLoi++;
                canh("NPC loai " + n[2] + " (" + n[0] + "," + n[1] + ") dex " + dex + " khong co sprite -> ve anh NPC cu");
                continue;
            }
            // sprite trum xuong hang duoi (nhan vat dung TREN vat the ma ve de len vat the o hang duoi)
            int duoi = 0;
            for (int yy = (n[1] + 1) * 16; yy < Math.min(H, (n[1] + 4) * 16); yy++) {
                for (int xx = Math.max(0, x - 40); xx < Math.min(W, x + 40); xx++) {
                    if (ve.getRGB(xx, yy) != truoc.getRGB(xx, yy)) {
                        final int s2 = m.map[(yy >> 4) * m.w + (xx >> 4)];
                        if (s2 < kieu.length && (kieu[s2] & 2) == 2 && mat[s2] != null && mat[s2][(yy & 15) * 16 + (xx & 15)]) {
                            duoi++;
                        }
                    }
                }
            }
            if (duoi > 24) {
                canh("NPC loai " + n[2] + " (" + n[0] + "," + n[1] + ") ve de len vat the hang duoi " + duoi + " px");
            }
        }
        // VatThePokemon (quai la do vat): kiem anh/khung (khong co vi tri tren map - quai do server gui)
        ImageIO.write(ve, "png", new File(ra, "ve/" + id + ".png"));
        if (loi > loi0 || canhBao > canh0) {
            final BufferedImage d = copy(nenVe);
            final java.awt.Graphics2D gd = d.createGraphics();
            gd.setColor(new java.awt.Color(255, 0, 0, 110));
            for (int i = 0; i < o.length; i++) {
                if (o[i]) {
                    gd.fillRect((i % m.w) * 16, (i / m.w) * 16, 16, 16);
                }
            }
            ImageIO.write(d, "png", new File(ra, "lech/" + id + ".png"));
            tom.append(String.format("  map %-4d loi %-4d canh bao %-3d lech anh %d px%n", id, loi - loi0, canhBao - canh0, lech));
        }
    }

    static int demMep(final boolean[] mk, final int d) {
        int c = 0;
        for (int k = 0; k < 16; k++) {
            final int px = d == 0 ? 15 : d == 1 ? 0 : k, py = d == 2 ? 15 : d == 3 ? 0 : k;
            if (mk[py * 16 + px]) {
                c++;
            }
        }
        return c;
    }

    static final Map<String, List<String[]>> NG_TAM = new HashMap<String, List<String[]>>();

    static boolean laVatTheNguon(final String bang) {
        return bang.equals("TilesetNature") || bang.equals("TilesetHouse") || bang.equals("TilesetElement") || bang.equals("TilesetDesert");
    }

    static BufferedImage bang(final String ten) {
        BufferedImage b = BANG.get(ten);
        if (b == null) {
            try {
                b = ImageIO.read(tim("tools/pokemon/pmd_raw_tile_moi/ninja_full/" + ten + ".png"));
            } catch (final java.io.IOException e) {
                throw new IllegalStateException(e);
            }
            BANG.put(ten, b);
        }
        return b;
    }

    static boolean dac(final BufferedImage b, final int x, final int y) {
        return x >= 0 && y >= 0 && x < b.getWidth() && y < b.getHeight() && (b.getRGB(x, y) >>> 24) > 128;
    }

    /** Trong bang sprite, o (cx,cy) noi lien sang o ke ben huong d: >= 3 cap pixel dac sat hai ben mep. */
    static boolean noiLien(final String bangTen, final int cx, final int cy, final int d) {
        if (!laVatTheNguon(bangTen)) {
            return false;
        }
        final BufferedImage b = bang(bangTen);
        int c = 0;
        for (int k = 0; k < 16; k++) {
            final int ax, ay, bx, by;
            if (d < 2) {
                ax = cx * 16 + (d == 0 ? 15 : 0);
                bx = ax + (d == 0 ? 1 : -1);
                ay = by = cy * 16 + k;
            } else {
                ay = cy * 16 + (d == 2 ? 15 : 0);
                by = ay + (d == 2 ? 1 : -1);
                ax = bx = cx * 16 + k;
            }
            if (dac(b, ax, ay) && dac(b, bx, by)) {
                c++;
            }
        }
        return c >= 3;
    }

    /**
     * Nha rong/hep hon mau (VeMapMoi.veNha) lap/bo cot giua va doi cot cua: canh ngang cung hang ma mot ben la cot giua, hay canh doc
     * giua hai cot giua cua cung loai nha, la hop le.
     */
    static boolean laCotGiuaNha(final String[] ten, final BanDo m, final int xx, final int yy, final int s, final boolean ngang) {
        if (xx < 0 || yy < 0 || xx >= m.w || yy >= m.h) {
            return false;
        }
        final int s2 = m.map[yy * m.w + xx];
        if (s2 >= ten.length) {
            return false;
        }
        final Matcher a = NHA.matcher(ten[s]), b = NHA.matcher(ten[s2]);
        if (!a.matches() || !b.matches() || !a.group(1).equals(b.group(1))) {
            return false;
        }
        final int n = SO_COT.get(a.group(1)), ca = Integer.parseInt(a.group(2)), cb = Integer.parseInt(b.group(2));
        final boolean giuaA = ca > 0 && ca < n - 1, giuaB = cb > 0 && cb < n - 1;
        return ngang ? a.group(3).equals(b.group(3)) && (giuaA || giuaB) : giuaA && giuaB;
    }

    static final Pattern NHA = Pattern.compile("(nha_cam|nha_be|trung_tam|cua_hang|nha_xanh)_(\\d)_(\\d)");
    static final Map<String, Integer> SO_COT = new HashMap<String, Integer>();

    static {
        SO_COT.put("nha_cam", 4);
        SO_COT.put("nha_be", 4);
        SO_COT.put("trung_tam", 4);
        SO_COT.put("cua_hang", 3);
        SO_COT.put("nha_xanh", 4);
    }

    static boolean laDiDuocHopLe(final String t) {
        return t.startsWith("T") || t.startsWith("hoa") || t.startsWith("soi") || t.startsWith("cau_") || t.startsWith("san")
                || t.startsWith("P") || t.startsWith("C") || t.matches("(nha_cam|nha_be|trung_tam|cua_hang|nha_xanh)_\\d_2");
    }

    static boolean laCong(final BanDo m, final int x, final int y) {
        for (final int[] c : m.cong) {
            if (c[0] == x && c[1] == y) {
                return true;
            }
        }
        return false;
    }

    static void them(final StringBuilder vd, final String s) {
        if (vd.length() < 1500) {
            vd.append("    ").append(s).append('\n');
        }
    }

    static BufferedImage copy(final BufferedImage a) {
        final BufferedImage r = new BufferedImage(a.getWidth(), a.getHeight(), BufferedImage.TYPE_INT_ARGB);
        r.createGraphics().drawImage(a, 0, 0, null);
        return r;
    }

    /** Anh vat the (vt_*.png) cua VatThePokemon: co tep, rong chia het so khung (ve day giua tai chan actor). */
    static void kiemVatThe() throws Exception {
        for (int b = 0; b < 2; b++) {
            for (final short[] d : b == 0 ? VatThePokemon.QUAI : VatThePokemon.NPC) {
                final File f = new File(res, "pk/vt_" + (b == 0 ? "" : "npc") + d[0] + ".png");
                if (!f.isFile()) {
                    canh("vat the " + f.getName() + " thieu tep (ve kieu cu)");
                    continue;
                }
                final BufferedImage a = ImageIO.read(f);
                if (a.getWidth() % Math.max(1, d[2]) != 0) {
                    loi("vat the " + f.getName() + ": rong " + a.getWidth() + " khong chia het " + d[2] + " khung");
                }
                final Graphics g = new Graphics(new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB));
                try {
                    if (!(b == 0 ? VatThePokemon.veQuai(g, d[0], 200, 300, 7) : VatThePokemon.veNpc(g, d[0], 200, 300, 7))) {
                        loi("vat the " + f.getName() + ": VatThePokemon khong ve duoc");
                    }
                } catch (final IllegalArgumentException e) {
                    loi("vat the " + f.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
