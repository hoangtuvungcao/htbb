import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.BanDoPokemon;
import com.htbb.pokemon.DinhDangBanDo;

/**
 * Ban do ve lai cho che do Pokemon (tools/pokemon/VeMapMoi.java, docs/kien-truc.md).
 *
 * VI SAO: bo cuc moi di qua 3 bo doc (server + Unity + J2ME) va hang chuc toa do viet cung tren
 * server. Lech mot o cong / NPC / diem den la nguoi choi ket trong tuong hoac khong qua map duoc,
 * ma khong co loi nao bao ra. Test nay kiem bang TILE THAT (kieu trong cMap/*.type), khong tin
 * ke hoach cua tool.
 */
public final class KiemMapPokemon {

    public static void chay() {
        Kiem.nhom("Ban do ve lai Pokemon (cMap, map/map<N>)");
        try {
            chayThat();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    private static void chayThat() throws Exception {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final String f : new File("cMap").list()) {
            if (f.matches("\\d+")) {
                ids.add(Integer.parseInt(f));
            }
        }
        java.util.Collections.sort(ids);
        final Map<Integer, List<int[]>> den = docDen();
        final Map<Integer, List<int[]>> coDinh = docCoDinh();
        int docGhiLech = 0, tileLa = 0;
        int quaiChan = 0, khongToi = 0, ro = 0, conVatCu = 0;
        final List<String> loi = new ArrayList<String>();
        for (final int id : ids) {
            final byte[] bMoi = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + id).toPath());
            final DinhDangBanDo m = DinhDangBanDo.doc(bMoi);
            if (!Arrays.equals(bMoi, m.ghi())) {
                docGhiLech++;
            }
            final String bo = DinhDangBanDo.boTile(id);
            final byte[] kieuB = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + bo + ".type").toPath());
            if (!m.cay.isEmpty() || !m.topLe.isEmpty() || !m.khoiTop.isEmpty()) {
                conVatCu++;
                loi.add(id + ": con cay/tileTop (anh game goc)");
            }
            final boolean[] di = new boolean[m.w * m.h];
            for (int i = 0; i < di.length; i++) {
                final int t = m.tile[i];
                if (!DinhDangBanDo.trong(t) && t >= kieuB.length) {
                    tileLa++;
                }
                di[i] = !DinhDangBanDo.trong(t) && t < kieuB.length && (kieuB[t] & 2) == 0;
            }
            final boolean[] cong = new boolean[di.length];
            for (final DinhDangBanDo.Cong c : m.cong) {
                cong[c.y * m.w + c.x] = true;
                di[c.y * m.w + c.x] = true;
            }
            // o phai toi duoc
            final List<Integer> can = new ArrayList<Integer>();
            for (final DinhDangBanDo.Cong c : m.cong) {
                can.add(c.y * m.w + c.x);
            }
            for (final DinhDangBanDo.Diem d : m.npc) {
                can.add(d.y * m.w + d.x);
            }
            for (final int[] d : den.containsKey(id) ? den.get(id) : new ArrayList<int[]>()) {
                if (d[0] < m.w && d[1] < m.h) {
                    can.add(d[1] * m.w + d[0]);
                }
            }
            for (final int[] r : coDinh.containsKey(id) ? coDinh.get(id) : new ArrayList<int[]>()) {
                for (int y = Math.max(0, r[1]); y <= Math.min(m.h - 1, r[3]); y++) {
                    for (int x = Math.max(0, r[0]); x <= Math.min(m.w - 1, r[2]); x++) {
                        can.add(y * m.w + x);
                    }
                }
            }
            // luoi quai: ban moi (neu co) phai giu dung so luong tung mau quai va nam tren o di duoc
            final File fq = new File("map/map" + id);
            if (fq.isFile()) {
                final DinhDangBanDo.LuoiQuai q = DinhDangBanDo.LuoiQuai.doc(Files.readAllBytes(fq.toPath()));
                if (q.w == m.w && q.h == m.h) {
                    for (int y = 0; y < q.h; y++) {
                        for (int x = 0; x < q.w; x++) {
                            if (q.coQuai(x, y)) {
                                if (!di[y * m.w + x]) {
                                    quaiChan++;
                                    loi.add(id + ": quai tren o chan " + x + "," + y);
                                }
                                can.add(y * m.w + x);
                            }
                        }
                    }
                }
            }
            if (can.isEmpty()) {
                continue;
            }
            // thanh phan lien thong chua nhieu o can nhat
            final int[] nhan = new int[di.length];
            Arrays.fill(nhan, -1);
            int soNhan = 0;
            final Map<Integer, Integer> dem = new HashMap<Integer, Integer>();
            for (final int c : can) {
                if (di[c] && nhan[c] < 0) {
                    loang(m.w, m.h, di, c, nhan, soNhan++);
                }
                if (nhan[c] >= 0) {
                    dem.merge(nhan[c], 1, Integer::sum);
                }
            }
            int chinh = -1;
            for (final Map.Entry<Integer, Integer> e : dem.entrySet()) {
                if (chinh < 0 || e.getValue() > dem.get(chinh)) {
                    chinh = e.getKey();
                }
            }
            for (final int c : can) {
                if (nhan[c] != chinh) {
                    khongToi++;
                    if (loi.size() < 40) {
                        loi.add(id + ": khong toi duoc " + (c % m.w) + "," + (c / m.w));
                    }
                }
            }
            // vuc (o trong, kieu 0) ke o nguoi choi toi duoc
            for (int i = 0; i < di.length; i++) {
                if (!DinhDangBanDo.trong(m.tile[i])) {
                    continue;
                }
                final int x = i % m.w, y = i / m.w;
                final int[][] k4 = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (final int[] d : k4) {
                    final int xx = x + d[0], yy = y + d[1];
                    if (xx >= 0 && yy >= 0 && xx < m.w && yy < m.h && nhan[yy * m.w + xx] == chinh && chinh >= 0) {
                        ro++;
                        break;
                    }
                }
            }
        }
        Kiem.dung("co ban do Pokemon (> 100)", ids.size() > 100);
        Kiem.bang("doc -> ghi lai trung tung byte", 0, docGhiLech);
        Kiem.bang("chi so tile nam trong bo tile moi", 0, tileLa);
        Kiem.bang("khong dung cay/tileTop cua game goc (lenh -48)", 0, conVatCu);
        Kiem.bang("moi o quai nam tren o di duoc", 0, quaiChan);
        Kiem.bang("cong/NPC/diem den/diem co dinh/quai deu toi duoc", 0, khongToi);
        Kiem.bang("khong co vuc (o trong) ke o toi duoc", 0, ro);
        if (!loi.isEmpty()) {
            System.out.println("    vi du: " + loi.subList(0, Math.min(8, loi.size())));
        }
        Kiem.dung("gui them map 0 cho client", Arrays.toString(BanDoPokemon.mapGuiThem()).contains("0"));
        Kiem.sai("khong con thu muc ban do cu cMap_pokemon (cMap la ban Pokemon duy nhat)", new File("cMap_pokemon").exists());
        final byte[] m0 = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + "0").toPath());
        Kiem.dung("Unity Resources/poke_map/0.bytes = cMap/0",
                Arrays.equals(m0, Files.readAllBytes(new File("../client_pc_ios/Assets/Resources/poke_map/0.bytes").toPath())));
        Kiem.dung("client_jar/res/0 = cMap/0",
                Arrays.equals(m0, Files.readAllBytes(new File("../client_jar/res/0").toPath())));
    }

    private static void loang(final int w, final int h, final boolean[] di, final int tu, final int[] nhan, final int so) {
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        q.add(tu);
        nhan[tu] = so;
        while (!q.isEmpty()) {
            final int i = q.poll();
            final int x = i % w, y = i / w;
            final int[][] k4 = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (final int[] d : k4) {
                final int xx = x + d[0], yy = y + d[1];
                if (xx >= 0 && yy >= 0 && xx < w && yy < h && di[yy * w + xx] && nhan[yy * w + xx] < 0) {
                    nhan[yy * w + xx] = so;
                    q.add(yy * w + xx);
                }
            }
        }
    }

    /** du_lieu/bang_den.json (GiaiMaMap): diem den qua cong + bay. */
    private static Map<Integer, List<int[]>> docDen() throws Exception {
        final Map<Integer, List<int[]>> m = new HashMap<Integer, List<int[]>>();
        final Pattern ten = Pattern.compile("^\\s*\"(\\d+)\"\\s*:");
        final Pattern cap = Pattern.compile("\\[(\\d+),(\\d+)\\]");
        for (final String l : Files.readAllLines(new File("../du_lieu/bang_den.json").toPath(), StandardCharsets.UTF_8)) {
            final Matcher mt = ten.matcher(l);
            if (!mt.find()) {
                continue;
            }
            final List<int[]> ds = new ArrayList<int[]>();
            final Matcher p = cap.matcher(l);
            while (p.find()) {
                ds.add(new int[]{Integer.parseInt(p.group(1)), Integer.parseInt(p.group(2))});
            }
            m.put(Integer.parseInt(mt.group(1)), ds);
        }
        return m;
    }

    /** du_lieu/diem_co_dinh.csv: map;x0;y0;x1;y1;noi;ghi_chu. */
    private static Map<Integer, List<int[]>> docCoDinh() throws Exception {
        final Map<Integer, List<int[]>> m = new HashMap<Integer, List<int[]>>();
        for (final String l : Files.readAllLines(new File("../du_lieu/diem_co_dinh.csv").toPath(), StandardCharsets.UTF_8)) {
            final String t = l.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            final String[] p = t.split(";", -1);
            final int id = Integer.parseInt(p[0].trim());
            m.computeIfAbsent(id, k -> new ArrayList<int[]>()).add(new int[]{Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()),
                    Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim())});
        }
        return m;
    }
}
