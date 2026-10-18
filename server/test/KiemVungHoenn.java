import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.htbb.pokemon.BanDoHoenn;
import com.htbb.pokemon.BanDoPokemon;
import com.htbb.pokemon.DinhDangBanDo;
import com.htbb.pokemon.TenMapPokemon;

/**
 * Vung Hoenn (nam 2): 29 ban do MOI chi co o cMap_pokemon (khung o com.htbb.pokemon.BanDoHoenn, ve bang
 * tools/pokemon/VeMapMoi.java). KiemMapPokemon chi duyet cMap goc nen khong thay cac map nay.
 *
 * VI SAO: cong Hoenn do server tu dung bang locationMap tu BanDoHoenn (real.VungHoenn) - lech mot o giua
 * ban ve va bang la nguoi choi dung truoc tuong / qua cong roi ket. Kiem bang TILE THAT.
 */
public final class KiemVungHoenn {

    public static void chay() {
        Kiem.nhom("Vung Hoenn (BanDoHoenn, cMap_pokemon 43..62/160..165/280..282)");
        try {
            chayThat();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    private static boolean[] diDuoc(final DinhDangBanDo m, final int id) throws Exception {
        final byte[] k = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + DinhDangBanDo.boTile(id) + ".type").toPath());
        final boolean[] di = new boolean[m.w * m.h];
        for (int i = 0; i < di.length; i++) {
            final int t = m.tile[i];
            di[i] = !DinhDangBanDo.trong(t) && t < k.length && (k[t] & 2) == 0;
        }
        return di;
    }

    private static void chayThat() throws Exception {
        final Map<String, String[]> tk = new HashMap<String, String[]>();
        for (final String l : Files.readAllLines(new File("../docs/pokemon/thiet_ke_map.csv").toPath(), StandardCharsets.UTF_8)) {
            if (!l.isEmpty() && Character.isDigit(l.charAt(0))) {
                final String[] p = l.split(";", -1);
                tk.put(p[0].trim(), p);
            }
        }
        final Map<Integer, List<int[]>> coDinh = new HashMap<Integer, List<int[]>>();
        for (final String l : Files.readAllLines(new File("../docs/pokemon/diem_co_dinh.csv").toPath(), StandardCharsets.UTF_8)) {
            if (!l.isEmpty() && Character.isDigit(l.charAt(0))) {
                final String[] p = l.split(";", -1);
                coDinh.computeIfAbsent(Integer.parseInt(p[0].trim()), z -> new ArrayList<int[]>())
                        .add(new int[]{Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim())});
            }
        }
        final Set<String> goc = new HashSet<String>(Arrays.asList(new File("cMap").list()));
        int sai = 0, thieu = 0, lechKhung = 0, tileLa = 0, khongToi = 0, ro = 0, quaiSai = 0, congSai = 0, denChan = 0, boSai = 0, tkSai = 0;
        final List<String> loi = new ArrayList<String>();
        final Map<Integer, boolean[]> diMap = new HashMap<Integer, boolean[]>();
        final Map<Integer, DinhDangBanDo> banDo = new HashMap<Integer, DinhDangBanDo>();
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            final String bo = b.id < 110 ? "t" : (b.id > 200 ? "t_thanh" : "t_hang");
            final boolean boDung = bo.equals(b.boTile())
                    && (("t".equals(bo) && (b.loai.equals("thi_tran") || !b.loai.equals("hang") && !b.loai.equals("dau_truong")))
                    || ("t_hang".equals(bo) && b.loai.equals("hang")) || ("t_thanh".equals(bo) && (b.loai.equals("dau_truong") || b.loai.equals("thanh"))));
            if (!boDung || goc.contains("" + b.id) || b.id >= 200 && b.id <= 271) {
                boSai++;
                loi.add(b.id + ": id/bo tile khong hop loai");
            }
            final String[] t = tk.get("" + b.id);
            if (t == null || !t[1].trim().equals(b.loai) || !t[4].trim().equals(b.moc)) {
                tkSai++;
                loi.add(b.id + ": thiet_ke_map.csv lech BanDoHoenn (loai/moc)");
            }
            final File f = new File(BanDoPokemon.THU_MUC_CMAP + b.id);
            if (!f.isFile()) {
                thieu++;
                loi.add(b.id + ": thieu cMap_pokemon");
                continue;
            }
            final byte[] by = Files.readAllBytes(f.toPath());
            final DinhDangBanDo m = DinhDangBanDo.doc(by);
            banDo.put(b.id, m);
            if (!Arrays.equals(by, m.ghi())) {
                sai++;
            }
            // khung: kich thuoc, cong (x, y, toM, xOut, yOut), NPC
            final List<int[]> congMoi = new ArrayList<int[]>();
            for (int i = 0; i < b.cong.size(); i++) {
                final int[] den = BanDoHoenn.diemDen(b.id, b.cong.get(i).den);
                for (final int[] o : b.oCong(i)) {
                    congMoi.add(new int[]{o[0], o[1], i, den == null ? -1 : den[0], den == null ? -1 : den[1]});
                }
            }
            boolean khop = m.w == b.w && m.h == b.h && !m.offline && m.cong.size() == congMoi.size() && m.npc.size() == b.npc.size()
                    && m.cay.isEmpty() && m.topLe.isEmpty() && m.khoiTop.isEmpty();
            for (int i = 0; khop && i < congMoi.size(); i++) {
                final DinhDangBanDo.Cong c = m.cong.get(i);
                final int[] e = congMoi.get(i);
                khop = c.x == e[0] && c.y == e[1] && c.toM == e[2] && c.xOut == e[3] && c.yOut == e[4] && c.toM <= 10;
            }
            final List<int[]> npc = b.npcTuyetDoi();
            for (int i = 0; khop && i < npc.size(); i++) {
                khop = m.npc.get(i).x == npc.get(i)[0] && m.npc.get(i).y == npc.get(i)[1] && m.npc.get(i).v == npc.get(i)[2];
            }
            if (!khop) {
                lechKhung++;
                loi.add(b.id + ": lech khung BanDoHoenn");
                continue;
            }
            final byte[] k = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + bo + ".type").toPath());
            for (final int ti : m.tile) {
                if (!DinhDangBanDo.trong(ti) && ti >= k.length) {
                    tileLa++;
                }
            }
            final boolean[] di = diDuoc(m, b.id);
            diMap.put(b.id, di);
            for (final DinhDangBanDo.Cong c : m.cong) {
                di[c.y * m.w + c.x] = true;
            }
            final List<Integer> can = new ArrayList<Integer>();
            for (final DinhDangBanDo.Cong c : m.cong) {
                can.add(c.y * m.w + c.x);
            }
            for (final DinhDangBanDo.Diem d : m.npc) {
                can.add(d.y * m.w + d.x);
            }
            for (int i = 0; i < b.cong.size(); i++) {
                final int[] v = b.diemVao(i);
                can.add(v[1] * m.w + v[0]);
            }
            for (final int[] d : coDinh.containsKey(b.id) ? coDinh.get(b.id) : new ArrayList<int[]>()) {
                can.add(d[1] * m.w + d[0]);
            }
            final File fq = new File(BanDoPokemon.THU_MUC_QUAI + "map" + b.id);
            if (!fq.isFile()) {
                quaiSai++;
                loi.add(b.id + ": thieu luoi quai");
            } else {
                final DinhDangBanDo.LuoiQuai q = DinhDangBanDo.LuoiQuai.doc(Files.readAllBytes(fq.toPath()));
                final Set<Integer> mau = new HashSet<Integer>();
                for (final int x : b.quai) {
                    mau.add(x);
                }
                int dem = 0;
                if (q.w != m.w || q.h != m.h) {
                    quaiSai++;
                    loi.add(b.id + ": luoi quai lech kich thuoc");
                } else {
                    for (int i = 0; i < q.o.length; i++) {
                        if (q.o[i] > 0 && q.o[i] != 255) {
                            dem++;
                            if (!mau.contains(q.o[i]) || q.o[i] < 139 || q.o[i] > 174 || !di[i]) {
                                quaiSai++;
                                loi.add(b.id + ": quai " + q.o[i] + " sai mau/o chan tai " + (i % m.w) + "," + (i / m.w));
                            }
                            can.add(i);
                        }
                    }
                }
                if ((b.quai.length > 0) != (dem >= 8)) {
                    quaiSai++;
                    loi.add(b.id + ": so quai " + dem + " khong khop thiet ke");
                }
            }
            final int[] nhan = new int[di.length];
            Arrays.fill(nhan, -1);
            final int dau = can.get(0);
            if (di[dau]) {
                loang(m.w, m.h, di, dau, nhan);
            }
            for (final int c : can) {
                if (nhan[c] != 0) {
                    khongToi++;
                    loi.add(b.id + ": khong toi duoc " + (c % m.w) + "," + (c / m.w));
                }
            }
            for (int i = 0; i < di.length; i++) {
                if (DinhDangBanDo.trong(m.tile[i])) {
                    final int x = i % m.w, y = i / m.w;
                    for (final int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                        final int xx = x + d[0], yy = y + d[1];
                        if (xx >= 0 && yy >= 0 && xx < m.w && yy < m.h && nhan[yy * m.w + xx] == 0) {
                            ro++;
                            break;
                        }
                    }
                }
            }
        }
        // do thi cong: moi cong co cong chieu nguoc, diem den di duoc o ban do dich
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            for (int i = 0; i < b.cong.size(); i++) {
                final int toi = b.cong.get(i).den;
                final int[] xy = BanDoHoenn.diemDen(b.id, toi);
                if (xy == null) {
                    congSai++;
                    loi.add(b.id + " -> " + toi + ": khong co cong chieu nguoc");
                    continue;
                }
                boolean[] di = diMap.get(toi);
                int w;
                if (di == null) {
                    final DinhDangBanDo m = DinhDangBanDo.doc(Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + toi).toPath()));
                    di = diDuoc(m, toi);
                    w = m.w;
                } else {
                    w = banDo.get(toi).w;
                }
                if (!di[xy[1] * w + xy[0]]) {
                    denChan++;
                    loi.add(b.id + " -> " + toi + ": diem den " + xy[0] + "," + xy[1] + " bi chan");
                }
            }
        }
        // ca vung lien thong qua cong, bat dau tu Littleroot
        final Set<Integer> da = new HashSet<Integer>();
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        q.add(BanDoHoenn.cuaNgo().id);
        da.add(BanDoHoenn.cuaNgo().id);
        boolean veKanto = false;
        while (!q.isEmpty()) {
            for (final BanDoHoenn.Cong c : BanDoHoenn.lay(q.poll()).cong) {
                if (c.den == BanDoHoenn.MAP_CONG_VAO) {
                    veKanto = true;
                } else if (da.add(c.den)) {
                    q.add(c.den);
                }
            }
        }
        Kiem.bang("doc -> ghi lai trung tung byte", 0, sai);
        Kiem.bang("moi ban do Hoenn co cMap_pokemon", 0, thieu);
        Kiem.bang("id ngoai cMap goc, bo tile hop loai, ngoai khoang 200..271", 0, boSai);
        Kiem.bang("thiet_ke_map.csv khop BanDoHoenn (loai, moc)", 0, tkSai);
        Kiem.bang("kich thuoc/cong/NPC khop khung BanDoHoenn, khong cay cu", 0, lechKhung);
        Kiem.bang("chi so tile nam trong bo tile moi", 0, tileLa);
        Kiem.bang("luoi quai: co file, mau Hoenn 139..174 cua thiet ke, tren o di duoc", 0, quaiSai);
        Kiem.bang("cong/NPC/diem vao/diem nhiem vu/quai cung mot vung toi duoc", 0, khongToi);
        Kiem.bang("khong co vuc ke o toi duoc", 0, ro);
        Kiem.bang("moi cong co cong chieu nguoc", 0, congSai);
        Kiem.bang("diem den qua cong di duoc", 0, denChan);
        Kiem.bang("ca vung lien thong tu Littleroot", BanDoHoenn.ds().size(), da.size());
        Kiem.dung("Littleroot co cong ve map 9", veKanto);
        final DinhDangBanDo m9 = DinhDangBanDo.doc(Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + "9").toPath()));
        boolean co1 = false;
        for (final DinhDangBanDo.Cong c : m9.cong) {
            co1 |= c.toM == BanDoHoenn.TOM_CONG_VAO;
        }
        Kiem.dung("map 9 co cum cong toM=" + BanDoHoenn.TOM_CONG_VAO + " (loi vao Hoenn)", co1);
        Kiem.dung("diem ve Kanto (33,1) o map 9 di duoc", diDuoc(m9, 9)[BanDoHoenn.DIEM_VE_KANTO[1] * m9.w + BanDoHoenn.DIEM_VE_KANTO[0]]);
        if (!loi.isEmpty()) {
            System.out.println("    vi du: " + loi.subList(0, Math.min(10, loi.size())));
        }
        // mo cong theo mua
        Kiem.dung("lau dai, tran 90 -> chan", BanDoHoenn.lyDoChan(true, 90, 100) != null);
        Kiem.dung("lau dai, tran 91, cap 84 -> qua", BanDoHoenn.lyDoChan(true, 91, 84) == null);
        Kiem.dung("lau dai, tran 95, cap 83 -> chan (cap)", BanDoHoenn.lyDoChan(true, 95, 83) != null);
        Kiem.dung("khong lau dai, cap 90 -> qua", BanDoHoenn.lyDoChan(false, 0, 90) == null);
        // ten: server TenMapPokemon = BanDoHoenn = client
        final List<String> tenBd = new ArrayList<String>();
        final List<Integer> idBd = new ArrayList<Integer>();
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            tenBd.add(b.ten);
            idBd.add(b.id);
        }
        final List<Integer> idTm = new ArrayList<Integer>();
        for (final int i : TenMapPokemon.HOENN_ID) {
            idTm.add(i);
        }
        Kiem.bang("TenMapPokemon.HOENN_ID = BanDoHoenn", idBd, idTm);
        Kiem.bang("TenMapPokemon.HOENN_TEN = BanDoHoenn", tenBd, Arrays.asList(TenMapPokemon.HOENN_TEN));
        for (final String cs : new String[]{"../client_pc_ios/ClientUnity/Assets/Scripts/Game/TenMapPokemon.cs", "../client_pc_ios/Assets/Scripts/Assembly-CSharp/TenMapPokemon.cs"}) {
            final File f = new File(cs);
            if (!f.isFile()) {
                continue;
            }
            final String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            final StringBuilder ten = new StringBuilder();
            for (final String t : TenMapPokemon.HOENN_TEN) {
                ten.append(ten.length() == 0 ? "" : ", ").append('"').append(t).append('"');
            }
            Kiem.dung(cs + ": HOENN_TEN giong server", s.contains("HOENN_TEN = new string[] { " + ten + " }"));
            Kiem.dung(cs + ": HOENN_ID giong server", s.contains("HOENN_ID = new int[] { " + idBd.toString().replace("[", "").replace("]", "") + " }"));
        }
    }

    private static void loang(final int w, final int h, final boolean[] di, final int tu, final int[] nhan) {
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        q.add(tu);
        nhan[tu] = 0;
        while (!q.isEmpty()) {
            final int i = q.poll();
            final int x = i % w, y = i / w;
            for (final int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                final int xx = x + d[0], yy = y + d[1];
                if (xx >= 0 && yy >= 0 && xx < w && yy < h && di[yy * w + xx] && nhan[yy * w + xx] < 0) {
                    nhan[yy * w + xx] = 0;
                    q.add(yy * w + xx);
                }
            }
        }
    }
}
