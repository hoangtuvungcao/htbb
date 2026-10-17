import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import com.htbb.pokemon.DinhDangBanDo;

/**
 * Ve LAI HOAN TOAN moi ban do cho che do Pokemon bang BO TILE MOI (tools/pokemon/DungBoTileMoi.java,
 * nguon CC0 - docs/pokemon/NGUON_ASSET_MAP.md). Khong dung tile, cay, nha hay vat trang tri nao cua
 * game goc: ban moi khong co "cay" (lenh -48) - cay, nha, hang rao deu la tile.
 * Dinh dang + rang buoc: docs/pokemon/DINH_DANG_MAP.md. Thiet ke tung map: docs/pokemon/thiet_ke_map.csv.
 *
 * Hai tang:
 *  1. KE HOACH vat lieu tung o (bo cuc moi) theo loai ban do + hat giong + diem co dinh:
 *       t       : G co, D dat (duong), S cat, W nuoc, T co cao, R vach da, F rung, B cau/boong go
 *       t_hang  : C nen hang, S cat, W nuoc, V vach hang
 *       t_thanh : P san da, G co, A cat dau truong, W nuoc, B cau
 *     duong noi moi cong/NPC/diem den (cay khung nho nhat + A* uon luon), co cao THANH DAM quanh o
 *     quai, ho/rung/vach o cho xa diem co dinh, nha Kenney dat dung o cua (o cong giua ban do).
 *  2. AUTOTILE GOC: o vat lieu A lay tile "A/nen_bit" theo 4 goc; goc la A khi ca 4 o quanh goc deu
 *     la A -> mep tron, khop nhau (bo tile sinh dung 15 kieu goc cho moi cap). Rung = 9 manh theo 4 huong.
 *
 * Giu rang buoc: cung kich thuoc, o cong (toM, xOut, yOut), NPC client, diem den (bang_den.json),
 * diem server viet cung (diem_co_dinh.csv), luoi quai (doi cho chi o bien/ho, ghi map/pokemon_spawn).
 * Kiem bang tile THAT (kieu trong cMap_pokemon/&lt;bo&gt;.type): moi diem phai toi duoc tu cong; sua lap.
 *
 * Chay (o HTBB/, sau GiaiMaMap va DungBoTileMoi):
 *   javac -encoding UTF-8 -sourcepath src -d build/tools tools/pokemon/GiaiMaMap.java tools/pokemon/VeMapMoi.java
 *   java -Djava.awt.headless=true -cp build/tools VeMapMoi [id ...]
 */
public final class VeMapMoi {

    static final String VAO_CMAP = "cMap";
    static final String RA_CMAP = "cMap_pokemon";
    static final String RA_ANH = "docs/pokemon/map_moi";
    static final String RA_QUAI = "map/pokemon_spawn/";
    static final String RA_UNITY = "ClientUnity/Assets/Resources/poke_map/";
    static final String RA_J2ME = "tools/pokemon/map_j2me/";
    static final String THU_MUC_TILE = "map/pokemon_tile/";
    static final int O = 13;
    /** Chi so vat lieu san lat da "K" cua bo t (quang truong) - duong khong ve de len. */
    static final int VL_SAN = 9;
    static final int[] DX4 = {1, -1, 0, 0}, DY4 = {0, 0, 1, -1};

    // =====================================================================================
    // Bo tile moi: ten vat lieu + tra ten tile
    // =====================================================================================
    static final class BoVL {
        final String bo;
        final String[] ten;
        final boolean[] di;
        final Map<String, Integer> id = new HashMap<String, Integer>();
        int[] kieu;

        BoVL(final String bo, final String[] ten, final boolean[] di) {
            this.bo = bo;
            this.ten = ten;
            this.di = di;
        }

        int vl(final String t) {
            for (int i = 0; i < ten.length; i++) {
                if (ten[i].equals(t)) {
                    return i;
                }
            }
            return -1;
        }

        int id(final String t) {
            final Integer i = id.get(t);
            if (i == null) {
                throw new IllegalStateException(bo + ": khong co tile '" + t + "' trong docs/pokemon/bang_tile_moi.csv");
            }
            return i;
        }

        boolean co(final String t) {
            return id.containsKey(t);
        }

        boolean diDuoc(final int tile) {
            return !DinhDangBanDo.trong(tile) && tile < kieu.length && (kieu[tile] & 2) == 0;
        }
    }

    static final Map<String, BoVL> BO = new LinkedHashMap<String, BoVL>();

    static {
        BO.put("t", new BoVL("t", new String[]{"G", "D", "S", "W", "T", "R", "F", "B", "N", "K"},
                new boolean[]{true, true, true, false, true, false, false, true, true, true}));
        BO.put("t_hang", new BoVL("t_hang", new String[]{"C", "S", "W", "V"}, new boolean[]{true, true, false, false}));
        BO.put("t_thanh", new BoVL("t_thanh", new String[]{"P", "G", "A", "W", "B"}, new boolean[]{true, true, true, false, true}));
    }

    static void napBoTile() throws Exception {
        for (final String l : Files.readAllLines(new File("docs/pokemon/bang_tile_moi.csv").toPath(), StandardCharsets.UTF_8)) {
            final String[] p = l.split(",", 5);
            if (p.length < 4 || p[0].equals("bo_tile")) {
                continue;
            }
            BO.get(p[0]).id.put(p[2], Integer.parseInt(p[1]));
        }
        for (final BoVL b : BO.values()) {
            final byte[] k = Files.readAllBytes(new File(RA_CMAP, b.bo + ".type").toPath());
            b.kieu = new int[k.length];
            for (int i = 0; i < k.length; i++) {
                b.kieu[i] = k[i] & 0xFF;
            }
        }
    }

    // =====================================================================================
    // Ke hoach
    // =====================================================================================
    static final class KeHoach {
        final int w, h;
        final int[] vl, khoaTile;
        /** Vat lieu cua o truoc khi bi khoa bang tile co dinh (cay, den...) - dung cho autotile lan can. */
        final int[] vlDuoi;
        final boolean[] khoa, diBat, giu, moc, cong, quai, giuChinh;
        final List<List<int[]>> chuoi = new ArrayList<List<int[]>>();
        int batDau = -1;
        /** Ban do tuyet / sa mac (doi nen co -> tuyet/cat, vat trang tri nen tuong ung). */
        boolean tuyet, saMac;
        /** Duong thang goc vuong (thi tran) thay vi uon luon. */
        boolean thang;

        KeHoach(final int w, final int h) {
            this.w = w;
            this.h = h;
            vl = new int[w * h];
            khoaTile = new int[w * h];
            vlDuoi = new int[w * h];
            Arrays.fill(vlDuoi, -1);
            khoa = new boolean[w * h];
            diBat = new boolean[w * h];
            giu = new boolean[w * h];
            moc = new boolean[w * h];
            cong = new boolean[w * h];
            quai = new boolean[w * h];
            giuChinh = new boolean[w * h];
        }

        boolean trong(final int x, final int y) {
            return x >= 0 && y >= 0 && x < w && y < h;
        }

        boolean diBatQuai(final int i) {
            return quai[i];
        }

        /** Khoa o voi tile co dinh (nha, cay, hang rao...). */
        void dat(final int x, final int y, final int tile) {
            if (!trong(x, y)) {
                return;
            }
            final int i = y * w + x;
            if (vl[i] != O) {
                vlDuoi[i] = vl[i];
            }
            khoa[i] = true;
            khoaTile[i] = tile;
            vl[i] = O;
        }
    }

    static final class Spec {
        int id;
        String loai, ten, moc;
        long hat;
    }

    static final class Moc {
        int x0, y0, x1, y1;
        String noi, ghiChu;
    }

    static final class DauVao {
        Spec spec;
        BoVL b;
        DinhDangBanDo goc;
        DinhDangBanDo.LuoiQuai quai, quaiGoc;
        List<Moc> moc = new ArrayList<Moc>();
        List<int[]> den = new ArrayList<int[]>();
    }

    /** O nay co the dat vat can (khong phai diem co dinh, cong, quai, duong bat buoc, vung giu). */
    static boolean tuDo(final KeHoach k, final int x, final int y) {
        if (!k.trong(x, y)) {
            return false;
        }
        final int i = y * k.w + x;
        return !k.khoa[i] && !k.moc[i] && !k.diBat[i] && !k.cong[i] && !k.giu[i];
    }

    static boolean hcnTuDo(final KeHoach k, final int x0, final int y0, final int x1, final int y1) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (!tuDo(k, x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    static KeHoach khoiTao(final DauVao v, final int nen) {
        final DinhDangBanDo g = v.goc;
        final KeHoach k = new KeHoach(g.w, g.h);
        Arrays.fill(k.vl, nen);
        for (final DinhDangBanDo.Cong c : g.cong) {
            if (k.trong(c.x, c.y)) {
                final int i = c.y * k.w + c.x;
                k.cong[i] = true;
                k.moc[i] = true;
                k.diBat[i] = true;
            }
        }
        return k;
    }

    // =====================================================================================
    // Nha (Ninja Adventure TilesetHouse) - 3 hang, o cua o hang duoi (xem DungBoTileMoi.NHA_NJ)
    // =====================================================================================
    /** {ten, rong, cot cua}. */
    static final Object[][] NHA = {
        {"trung_tam", 4, 1}, {"cua_hang", 3, 1}, {"nha_cam", 4, 1}, {"nha_be", 4, 1}, {"nha_xanh", 4, 1},
    };

    /**
     * Ve nha mau 'loai' rong 'rong' o (lap cot giua cua mau), cua o hang duoi tai cot tuyet doi
     * cua0..cua1 (cua nhieu o = nhieu canh cua, khong ve tuong de len o cong).
     */
    static void veNha(final KeHoach k, final BoVL b, final int loai, final int trai, final int tren, final int rong, final int cua0,
                      final int cua1) {
        final String ten = (String) NHA[loai][0];
        final int mw = (Integer) NHA[loai][1], mc = (Integer) NHA[loai][2];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < rong; c++) {
                final int x = trai + c;
                int cot;
                if (c == 0) {
                    cot = 0;
                } else if (c == rong - 1) {
                    cot = mw - 1;
                } else {
                    cot = mw <= 2 ? 0 : 1 + (c - 1) % (mw - 2);
                }
                if (r == 2) {
                    if (x >= cua0 && x <= cua1) {
                        cot = mc;
                    } else if (cot == mc) {
                        cot = (mc + 1 < mw - 1) ? mc + 1 : Math.max(1, mc - 1);
                        if (cot == mc) {
                            cot = 0;
                        }
                    }
                } else if (r == 1 && x == (cua0 + cua1) / 2 && c > 0 && c < rong - 1) {
                    cot = mc;
                }
                k.dat(x, tren + r, b.id(ten + "_" + cot + "_" + r));
            }
        }
        for (int y = tren - 1; y <= tren + 3; y++) {
            for (int x = trai - 1; x <= trai + rong; x++) {
                if (k.trong(x, y)) {
                    k.giu[y * k.w + x] = true;
                }
            }
        }
    }

    static void veNha(final KeHoach k, final BoVL b, final int loai, final int trai, final int tren) {
        final int cua = trai + (Integer) NHA[loai][2];
        veNha(k, b, loai, trai, tren, (Integer) NHA[loai][1], cua, cua);
    }

    /** Dat nha sao cho cac o cua phu dung hang cong duoi cung cua cum (cua nha phai dung o cong). */
    static boolean datNhaCua(final KeHoach k, final BoVL b, final List<int[]> cum, final int uuTien, final boolean coBien) {
        int cua0 = 999, cua1 = -1, yc = -1;
        for (final int[] c : cum) {
            yc = Math.max(yc, c[1]);
        }
        final Set<Integer> laCong = new HashSet<Integer>();
        for (final int[] c : cum) {
            laCong.add(c[1] * k.w + c[0]);
            if (c[1] == yc) {
                cua0 = Math.min(cua0, c[0]);
                cua1 = Math.max(cua1, c[0]);
            }
        }
        for (int thu = 0; thu < NHA.length * 3; thu++) {
            final int loai = (uuTien + thu) % NHA.length;
            final int them = thu / NHA.length;   // thu rong dan: dung mau, +1 moi ben, lech trai/phai
            final int rongCua = cua1 - cua0 + 1;
            final int rong = Math.max((Integer) NHA[loai][1], rongCua + 2) + (them == 1 ? 1 : 0);
            final int trai = cua0 - Math.max(1, (rong - rongCua) / 2) + (them == 2 ? 1 : 0);
            final int tren = yc - 2;
            if (tren < 0 || trai < 0 || trai + rong > k.w || trai >= cua0 || trai + rong - 1 <= cua1) {
                continue;
            }
            boolean ok = true;
            for (int y = tren; y <= yc && ok; y++) {
                for (int x = trai; x < trai + rong; x++) {
                    final int i = y * k.w + x;
                    if (!laCong.contains(i) && (k.moc[i] || k.cong[i] || k.khoa[i])) {
                        ok = false;
                        break;
                    }
                }
            }
            if (!ok) {
                continue;
            }
            veNha(k, b, loai, trai, tren, rong, cua0, cua1);
            if (coBien && loai <= 1) {
                final int bx = trai + rong, by = yc;
                if (k.trong(bx, by) && tuDo(k, bx, by)) {
                    k.dat(bx, by, b.id(loai == 0 ? "bien_hoi_phuc" : (b.co("bien_cua_hang") ? "bien_cua_hang" : "bien_go")));
                }
            }
            return true;
        }
        return false;
    }

    /** Nha trang tri o cho trong (khong co cong); loi vao truoc cua thanh diem noi duong. */
    static List<int[]> datNhaThem(final KeHoach k, final BoVL b, final Random rd, final int so, final int x0, final int y0, final int x1,
                                  final int y1) {
        final List<int[]> cua = new ArrayList<int[]>();
        for (int thu = 0; thu < 500 && cua.size() < so; thu++) {
            final int loai = 2 + rd.nextInt(NHA.length - 2);
            final int rong = (Integer) NHA[loai][1] + rd.nextInt(2);
            final int x = x0 + rd.nextInt(Math.max(1, x1 - x0 - rong)), y = y0 + rd.nextInt(Math.max(1, y1 - y0 - 4));
            if (!hcnTuDo(k, x - 1, y - 1, x + rong, y + 4)) {
                continue;
            }
            final int cx = x + rong / 2;
            veNha(k, b, loai, x, y, rong, cx, cx);
            k.diBat[(y + 3) * k.w + cx] = true;
            k.moc[(y + 3) * k.w + cx] = true;
            cua.add(new int[]{cx, y + 3});
        }
        return cua;
    }

    /**
     * Nha doc pho (thi tran): cua quay ra mot o duong, sau khi da ve duong. San nho: bon hoa hai ben cua,
     * hom thu canh nha.
     */
    static int datNhaDuong(final KeHoach k, final BoVL b, final Random rd, final int so, final int vlDuong) {
        int dat = 0;
        final List<Integer> oDuong = new ArrayList<Integer>();
        for (int i = k.w; i < k.vl.length; i++) {
            // mep tren cua duong: o phia bac khong phai duong
            if (k.vl[i] == vlDuong && !k.khoa[i] && k.vl[i - k.w] != vlDuong) {
                oDuong.add(i);
            }
        }
        if (oDuong.isEmpty()) {
            return 0;
        }
        for (int thu = 0; thu < 1500 && dat < so; thu++) {
            final int p = oDuong.get(rd.nextInt(oDuong.size()));
            final int px = p % k.w, py = p / k.w;
            final int loai = 2 + rd.nextInt(NHA.length - 2);
            final int rong = 3 + rd.nextInt(3);
            final int trai = px - rong / 2, tren = py - 4, day = py - 2;
            if (tren < 2 || trai < 2 || trai + rong + 1 >= k.w - 1 || !hcnCo(k, trai - 1, tren - 1, trai + rong, day + 1)) {
                continue;
            }
            veNha(k, b, loai, trai, tren, rong, px, px);
            k.vl[(py - 1) * k.w + px] = vlDuong;
            k.diBat[(py - 1) * k.w + px] = true;
            if (tuDo(k, px - 1, py - 1)) {
                k.dat(px - 1, py - 1, b.id("hoa_cam"));
            }
            if (tuDo(k, px + 1, py - 1)) {
                k.dat(px + 1, py - 1, b.id(rd.nextBoolean() ? "hoa_vang" : (b.co("hom_thu") ? "hom_thu" : "hoa_vang")));
            }
            dat++;
        }
        return dat;
    }

    /**
     * Lang nho (cac map mang ten thi tran Pokemon: Pallet, Viridian...): Trung tam + Cua hang + 2 nha quanh
     * mot san dat, rao go, bon hoa, bien go; noi vao duong gan nhat. Tra ve diem noi (giua san) hoac null.
     */
    static int[] datLang(final KeHoach k, final BoVL b, final Random rd, final int vlSan) {
        final int rong = 19, cao = 10;
        for (int thu = 0; thu < 800; thu++) {
            final int x0 = 3 + rd.nextInt(Math.max(1, k.w - rong - 6)), y0 = 3 + rd.nextInt(Math.max(1, k.h - cao - 6));
            if (!hcnTuDo(k, x0 - 1, y0 - 1, x0 + rong, y0 + cao)) {
                continue;
            }
            // san dat chung truoc nha
            for (int y = y0 + 3; y <= y0 + 6; y++) {
                for (int x = x0; x < x0 + rong; x++) {
                    k.vl[y * k.w + x] = vlSan;
                    k.giu[y * k.w + x] = true;
                }
            }
            veNha(k, b, 0, x0, y0);
            veNha(k, b, 1, x0 + 5, y0);
            veNha(k, b, 2 + rd.nextInt(3), x0 + 9, y0);
            veNha(k, b, 2 + rd.nextInt(3), x0 + 14, y0);
            k.dat(x0 + 4, y0 + 2, b.id("bien_hoi_phuc"));
            if (b.co("bien_cua_hang")) {
                k.dat(x0 + 8, y0 + 2, b.id("bien_cua_hang"));
            }
            // bon hoa + rao duoi san
            for (int x = x0; x < x0 + rong; x++) {
                final int y = y0 + 8;
                if (x == x0 + rong / 2 || x == x0 + rong / 2 - 1) {
                    continue;
                }
                k.dat(x, y, b.id(x == x0 ? "rao_trai" : (x == x0 + rong - 1 ? "rao_phai" : "rao_giua")));
                k.dat(x, y - 1, b.id(((x + y) % 3 == 0) ? "hoa_cam" : "hoa_vang"));
            }
            for (int x = x0; x < x0 + rong; x++) {
                k.diBat[(y0 + 5) * k.w + x] = true;
            }
            final int cx = x0 + rong / 2;
            k.moc[(y0 + 5) * k.w + cx] = true;
            return new int[]{cx, y0 + 9};
        }
        return null;
    }

    /** Vat nho (tile chan hoac trang tri) rai o cho trong cua vat lieu nen, cach nhau &gt;= 2 o. */
    static void datCayLe(final KeHoach k, final BoVL b, final int vlNen, final double mat, final long hat, final String[] loai) {
        for (int y = 1; y < k.h - 1; y++) {
            for (int x = 1; x < k.w - 1; x++) {
                final int i = y * k.w + x;
                if (k.vl[i] != vlNen || !tuDo(k, x, y) || bam(x, y, hat + 606) >= mat) {
                    continue;
                }
                boolean gan = false;
                for (int dy = -2; dy <= 2 && !gan; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        if (k.trong(x + dx, y + dy) && k.khoa[(y + dy) * k.w + x + dx]) {
                            gan = true;
                            break;
                        }
                    }
                }
                if (!gan) {
                    k.dat(x, y, b.id(loai[(int) (bam(x, y, hat + 607) * loai.length)]));
                }
            }
        }
    }

    /** Cay 2x2 le (cay to) o bai co trong. */
    static void datCayTo(final KeHoach k, final BoVL b, final int vlNen, final double mat, final long hat) {
        final String[] loai = k.tuyet ? new String[]{"cayT"} : (k.saMac ? new String[]{"dua"}
                : (b.co("cayC_0") ? new String[]{"cayA", "cayB", "cayC"} : new String[]{"cayA", "cayB"}));
        for (int y = 2; y < k.h - 3; y++) {
            for (int x = 2; x < k.w - 3; x++) {
                if (bam(x, y, hat + 611) >= mat || !hcnTuDo(k, x - 1, y - 1, x + 2, y + 2)) {
                    continue;
                }
                boolean nen = true;
                for (int dy = 0; dy < 2; dy++) {
                    for (int dx = 0; dx < 2; dx++) {
                        if (k.vl[(y + dy) * k.w + x + dx] != vlNen) {
                            nen = false;
                        }
                    }
                }
                if (!nen) {
                    continue;
                }
                final String t = loai[(int) (bam(x, y, hat + 612) * loai.length)];
                k.dat(x, y, b.id(t + "_0"));
                k.dat(x + 1, y, b.id(t + "_1"));
                k.dat(x, y + 1, b.id(t + "_2"));
                k.dat(x + 1, y + 1, b.id(t + "_3"));
            }
        }
    }

    /** Hinh chu nhat toan co thuong, khong vat can/diem co dinh (cho dat nha, vuon). */
    static boolean hcnCo(final KeHoach k, final int x0, final int y0, final int x1, final int y1) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (!k.trong(x, y)) {
                    return false;
                }
                final int i = y * k.w + x;
                if (k.khoa[i] || k.moc[i] || k.cong[i] || k.vl[i] != 0 || (k.giu[i] && k.diBat[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Vuon hoa rao go (diem nhan) o cho trong. */
    static boolean datVuon(final KeHoach k, final BoVL b, final Random rd, final int rong, final int cao) {
        for (int thu = 0; thu < 300; thu++) {
            final int x0 = 3 + rd.nextInt(Math.max(1, k.w - rong - 6)), y0 = 3 + rd.nextInt(Math.max(1, k.h - cao - 6));
            if (!hcnTuDo(k, x0 - 1, y0 - 1, x0 + rong, y0 + cao) || !hcnCo(k, x0 - 1, y0 - 1, x0 + rong, y0 + cao)) {
                continue;
            }
            final int cuaX = x0 + rong / 2;
            for (int y = y0; y < y0 + cao; y++) {
                for (int x = x0; x < x0 + rong; x++) {
                    final boolean bien = y == y0 || y == y0 + cao - 1 || x == x0 || x == x0 + rong - 1;
                    if (!bien) {
                        final String[] hoa = {"hoa_vang", "hoa_cam", "hoa_trang", "hoa_vang"};
                        k.dat(x, y, b.id(hoa[(x * 3 + y) % hoa.length]));
                    } else if (y == y0 + cao - 1 && x == cuaX) {
                        k.dat(x, y, b.id("G"));
                    } else if (y == y0 || y == y0 + cao - 1) {
                        k.dat(x, y, b.id(x == x0 ? "rao_trai" : (x == x0 + rong - 1 ? "rao_phai" : "rao_giua")));
                    } else {
                        k.dat(x, y, b.id("rao_doc"));
                    }
                }
            }
            return true;
        }
        return false;
    }

    /** Bien go ben trong moi cong mep (canh loi vao). */
    static void datBienBao(final KeHoach k, final BoVL b, final DauVao v) {
        if (!b.co("bien_go")) {
            return;
        }
        for (final List<int[]> cum : cumCong(v.goc)) {
            if (!laMep(cum, k.w, k.h)) {
                continue;
            }
            final int[] hv = huongVao(cum, k.w, k.h);
            final int[] c = tam(cum);
            final int[][] thu = {{hv[1] * 3 + hv[0] * 4, hv[0] * 3 + hv[1] * 4}, {-hv[1] * 3 + hv[0] * 4, -hv[0] * 3 + hv[1] * 4}};
            for (final int[] t : thu) {
                final int x = c[0] + t[0], y = c[1] + t[1];
                if (tuDo(k, x, y) && diVl(b, k.vl[y * k.w + x])) {
                    k.dat(x, y, b.id("bien_go"));
                    break;
                }
            }
        }
    }

    static double bam(final int x, final int y, final long s) {
        long h = s * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (h >>> 11) * (1.0 / (1L << 53));
    }

    static double nhieu(final double x, final double y, final long s) {
        final int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
        final double fx = x - xi, fy = y - yi;
        final double ux = fx * fx * (3 - 2 * fx), uy = fy * fy * (3 - 2 * fy);
        final double a = bam(xi, yi, s), b = bam(xi + 1, yi, s), c = bam(xi, yi + 1, s), d = bam(xi + 1, yi + 1, s);
        return a + (b - a) * ux + (c - a) * uy + (a - b - c + d) * ux * uy;
    }

    static double fbm(final double x, final double y, final long s, final double tl) {
        return 0.55 * nhieu(x / tl, y / tl, s) + 0.3 * nhieu(x * 2 / tl, y * 2 / tl, s + 7) + 0.15 * nhieu(x * 4 / tl, y * 4 / tl, s + 13);
    }

    // ---------- tim duong ----------
    /** Dijkstra tu 'tu' toi o dau tien co dich[] = true. Tra ve danh sach o (ca 2 dau), null neu khong toi. */
    static List<Integer> timDuong(final int w, final int h, final int tu, final boolean[] dich, final double[] chiPhi) {
        final double[] d = new double[w * h];
        final int[] truoc = new int[w * h];
        Arrays.fill(d, Double.MAX_VALUE);
        Arrays.fill(truoc, -1);
        final PriorityQueue<double[]> pq = new PriorityQueue<double[]>((a, b) -> Double.compare(a[0], b[0]));
        d[tu] = 0;
        pq.add(new double[]{0, tu});
        while (!pq.isEmpty()) {
            final double[] e = pq.poll();
            final int i = (int) e[1];
            if (e[0] > d[i]) {
                continue;
            }
            if (dich[i] && i != tu) {
                final List<Integer> p = new ArrayList<Integer>();
                for (int k = i; k != -1; k = truoc[k]) {
                    p.add(k);
                }
                return p;
            }
            final int x = i % w, y = i / w;
            for (int k = 0; k < 4; k++) {
                final int xx = x + DX4[k], yy = y + DY4[k];
                if (xx < 0 || yy < 0 || xx >= w || yy >= h) {
                    continue;
                }
                final int j = yy * w + xx;
                if (chiPhi[j] >= 1e8) {
                    continue;
                }
                final double nd = d[i] + chiPhi[j];
                if (nd < d[j]) {
                    d[j] = nd;
                    truoc[j] = i;
                    pq.add(new double[]{nd, j});
                }
            }
        }
        return null;
    }

    static void veDuong(final KeHoach k, final List<Integer> p, final int rong, final int vl, final boolean loi) {
        final int r0 = (rong - 1) / 2, r1 = rong / 2;
        for (final int i : p) {
            final int x = i % k.w, y = i / k.w;
            for (int dy = -r0; dy <= r1; dy++) {
                for (int dx = -r0; dx <= r1; dx++) {
                    final int xx = x + dx, yy = y + dy;
                    if (k.trong(xx, yy) && !k.khoa[yy * k.w + xx] && k.vl[yy * k.w + xx] != VL_SAN) {
                        k.vl[yy * k.w + xx] = vl;
                        k.giu[yy * k.w + xx] = true;
                    }
                }
            }
            if (loi && !k.khoa[i]) {
                k.diBat[i] = true;
            }
        }
    }

    static double[] chiPhiKeHoach(final KeHoach k, final BoVL b, final long hat, final int vlDuong) {
        final double[] c = new double[k.w * k.h];
        for (int i = 0; i < c.length; i++) {
            final int x = i % k.w, y = i / k.w;
            if (k.khoa[i] && !k.cong[i]) {
                c[i] = 1e9;
                continue;
            }
            final int v = k.vl[i];
            double base = (v >= 0 && v < b.di.length && b.di[v]) ? 1.0 : 5.0;
            if (v == vlDuong) {
                base = 0.45;
            }
            c[i] = base + 2.2 * fbm(x, y, hat + 99, 9);
            if (x < 2 || y < 2 || x >= k.w - 2 || y >= k.h - 2) {
                c[i] += 4;
            }
        }
        return c;
    }

    /** Cay khung nho nhat (Prim) tren cac diem. */
    static List<int[]> khung(final List<int[]> p) {
        final List<int[]> canh = new ArrayList<int[]>();
        if (p.size() < 2) {
            return canh;
        }
        final boolean[] trong = new boolean[p.size()];
        final double[] d = new double[p.size()];
        final int[] tu = new int[p.size()];
        Arrays.fill(d, Double.MAX_VALUE);
        d[0] = 0;
        for (int n = 0; n < p.size(); n++) {
            int b = -1;
            for (int i = 0; i < p.size(); i++) {
                if (!trong[i] && (b < 0 || d[i] < d[b])) {
                    b = i;
                }
            }
            trong[b] = true;
            if (n > 0) {
                canh.add(new int[]{tu[b], b});
            }
            for (int i = 0; i < p.size(); i++) {
                final double dd = Math.hypot(p.get(i)[0] - p.get(b)[0], p.get(i)[1] - p.get(b)[1]);
                if (!trong[i] && dd < d[i]) {
                    d[i] = dd;
                    tu[i] = b;
                }
            }
        }
        return canh;
    }

    /** Khoang cach Chebyshev toi o 'vat' gan nhat (BFS 8 huong). Ngoai mep tinh nhu vat cach 1. */
    static int[] khoangCach(final int w, final int h, final boolean[] vat) {
        final int[] d = new int[w * h];
        Arrays.fill(d, Integer.MAX_VALUE);
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        for (int i = 0; i < d.length; i++) {
            final int x = i % w, y = i / w;
            if (vat[i]) {
                d[i] = 0;
                q.add(i);
            } else if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
                d[i] = 1;
                q.add(i);
            }
        }
        while (!q.isEmpty()) {
            final int i = q.poll();
            final int x = i % w, y = i / w;
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    final int xx = x + dx, yy = y + dy;
                    if (xx >= 0 && yy >= 0 && xx < w && yy < h && d[yy * w + xx] > d[i] + 1) {
                        d[yy * w + xx] = d[i] + 1;
                        q.add(yy * w + xx);
                    }
                }
            }
        }
        return d;
    }

    /** Dat 'soLuong' dam vat lieu o cho xa vung giu nhat. Tra ve so dam da dat. */
    static int datDam(final KeHoach k, final Random rd, final long hat, final int vl, final int soLuong, final int rMin, final int rMax,
                      final boolean[] daDat) {
        int n = 0;
        for (int c = 0; c < soLuong; c++) {
            final boolean[] vat = new boolean[k.w * k.h];
            for (int i = 0; i < vat.length; i++) {
                vat[i] = k.giu[i] || k.khoa[i] || daDat[i];
            }
            final int[] d = khoangCach(k.w, k.h, vat);
            int max = 0;
            for (final int v : d) {
                if (v != Integer.MAX_VALUE) {
                    max = Math.max(max, v);
                }
            }
            if (max < rMin + 2) {
                break;
            }
            final List<Integer> ung = new ArrayList<Integer>();
            for (int i = 0; i < d.length; i++) {
                if (d[i] != Integer.MAX_VALUE && d[i] >= Math.max(rMin + 2, max * 0.75)) {
                    ung.add(i);
                }
            }
            final int tamO = ung.get(rd.nextInt(ung.size()));
            final int cx = tamO % k.w, cy = tamO / k.w;
            final double rr = Math.min(rMax, d[tamO] - 1.5);
            final double kx = 0.75 + rd.nextDouble() * 0.6, ky = 0.75 + rd.nextDouble() * 0.6;
            final long s = hat + 31L * c + vl * 1000L;
            for (int y = (int) (cy - rr * 2); y <= cy + rr * 2; y++) {
                for (int x = (int) (cx - rr * 2); x <= cx + rr * 2; x++) {
                    if (!k.trong(x, y)) {
                        continue;
                    }
                    final int i = y * k.w + x;
                    if (k.giu[i] || k.khoa[i]) {
                        continue;
                    }
                    final double dd = Math.hypot((x - cx) / kx, (y - cy) / ky);
                    if (dd < rr * (0.7 + 0.6 * fbm(x, y, s, 6))) {
                        k.vl[i] = vl;
                        daDat[i] = true;
                    }
                }
            }
            n++;
        }
        return n;
    }

    /** Bo cac dom vat lieu le (o co &lt;= 2 lang gieng cung loai) ngoai vung giu/khoa. */
    static void lamMin(final KeHoach k, final BoVL b, final int lan) {
        for (int l = 0; l < lan; l++) {
            final int[] moi = k.vl.clone();
            for (int y = 0; y < k.h; y++) {
                for (int x = 0; x < k.w; x++) {
                    final int i = y * k.w + x;
                    if (k.khoa[i]) {
                        continue;
                    }
                    final int[] dem = new int[16];
                    int cung = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if ((dx != 0 || dy != 0) && k.trong(x + dx, y + dy)) {
                                final int v = k.vl[(y + dy) * k.w + x + dx];
                                dem[v]++;
                                if (v == k.vl[i]) {
                                    cung++;
                                }
                            }
                        }
                    }
                    boolean khe = false;
                    if (k.trong(x - 1, y) && k.trong(x + 1, y) && k.vl[i - 1] != k.vl[i] && k.vl[i + 1] != k.vl[i]) {
                        khe = true;
                    }
                    if (k.trong(x, y - 1) && k.trong(x, y + 1) && k.vl[i - k.w] != k.vl[i] && k.vl[i + k.w] != k.vl[i]) {
                        khe = true;
                    }
                    if (cung > 2 && !khe) {
                        continue;
                    }
                    int best = -1;
                    for (int v = 0; v < b.di.length; v++) {
                        if (v != k.vl[i] && (best < 0 || dem[v] > dem[best])) {
                            best = v;
                        }
                    }
                    if (best < 0 || dem[best] == 0) {
                        continue;
                    }
                    if (k.diBat[i] && !b.di[best]) {
                        continue;
                    }
                    moi[i] = best;
                }
            }
            System.arraycopy(moi, 0, k.vl, 0, moi.length);
        }
    }

    // =====================================================================================
    // Diem co dinh -> ke hoach
    // =====================================================================================
    static List<List<int[]>> cumCong(final DinhDangBanDo m) {
        final List<List<int[]>> cum = new ArrayList<List<int[]>>();
        final boolean[] da = new boolean[m.cong.size()];
        for (int i = 0; i < m.cong.size(); i++) {
            if (da[i]) {
                continue;
            }
            final List<int[]> c = new ArrayList<int[]>();
            final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
            q.add(i);
            da[i] = true;
            while (!q.isEmpty()) {
                final int a = q.poll();
                final DinhDangBanDo.Cong ca = m.cong.get(a);
                c.add(new int[]{ca.x, ca.y});
                for (int j = 0; j < m.cong.size(); j++) {
                    final DinhDangBanDo.Cong cj = m.cong.get(j);
                    if (!da[j] && Math.abs(cj.x - ca.x) <= 1 && Math.abs(cj.y - ca.y) <= 1) {
                        da[j] = true;
                        q.add(j);
                    }
                }
            }
            cum.add(c);
        }
        return cum;
    }

    static boolean laMep(final List<int[]> cum, final int w, final int h) {
        for (final int[] c : cum) {
            if (c[0] <= 2 || c[1] <= 2 || c[0] >= w - 3 || c[1] >= h - 3) {
                return true;
            }
        }
        return false;
    }

    /** Huong vao trong tu cum cong (dx, dy). */
    static int[] huongVao(final List<int[]> cum, final int w, final int h) {
        final int[] c = tam(cum);
        if (c[0] <= 2) {
            return new int[]{1, 0};
        }
        if (c[0] >= w - 3) {
            return new int[]{-1, 0};
        }
        if (c[1] <= 2) {
            return new int[]{0, 1};
        }
        if (c[1] >= h - 3) {
            return new int[]{0, -1};
        }
        return new int[]{0, 1};
    }

    static int[] tam(final List<int[]> cum) {
        int sx = 0, sy = 0;
        for (final int[] c : cum) {
            sx += c[0];
            sy += c[1];
        }
        int bx = cum.get(0)[0], by = cum.get(0)[1];
        double bd = 1e9;
        for (final int[] c : cum) {
            final double d = Math.hypot(c[0] - sx / (double) cum.size(), c[1] - sy / (double) cum.size());
            if (d < bd) {
                bd = d;
                bx = c[0];
                by = c[1];
            }
        }
        return new int[]{bx, by};
    }

    /** Khoi vat the (nha) quanh cum cong cua trong ban do goc. */
    static List<int[]> danhDauMoc(final KeHoach k, final DauVao v, final int vlDi) {
        final List<int[]> noi = new ArrayList<int[]>();
        // cong: giu rong 3, diem noi o huong vao trong
        for (final List<int[]> cum : cumCong(v.goc)) {
            final int[] hv = huongVao(cum, k.w, k.h);
            for (final int[] c : cum) {
                for (int s = 0; s <= 3; s++) {
                    final int x = c[0] + hv[0] * s, y = c[1] + hv[1] * s;
                    if (k.trong(x, y) && !k.khoa[y * k.w + x]) {
                        k.diBat[y * k.w + x] = true;
                        if (s > 0) {
                            k.vl[y * k.w + x] = vlDi;
                        }
                    }
                }
                tron(k, c[0], c[1], 3, true);
            }
            final int[] t = tam(cum);
            int x = t[0] + hv[0] * 3, y = t[1] + hv[1] * 3;
            x = Math.max(0, Math.min(k.w - 1, x));
            y = Math.max(0, Math.min(k.h - 1, y));
            noi.add(new int[]{x, y});
            if (k.batDau < 0) {
                k.batDau = y * k.w + x;
            }
        }
        final List<int[]> diem = new ArrayList<int[]>();
        for (final int[] d : v.den) {
            diem.add(new int[]{d[0], d[1], d[0], d[1]});
        }
        for (final DinhDangBanDo.Diem d : v.goc.npc) {
            diem.add(new int[]{d.x, d.y, d.x, d.y});
        }
        final Map<String, List<int[]>> chuoi = new LinkedHashMap<String, List<int[]>>();
        for (final Moc m : v.moc) {
            diem.add(new int[]{m.x0, m.y0, m.x1, m.y1});
            if (!m.noi.isEmpty()) {
                List<int[]> l = chuoi.get(m.noi);
                if (l == null) {
                    l = new ArrayList<int[]>();
                    chuoi.put(m.noi, l);
                }
                l.add(new int[]{(m.x0 + m.x1) / 2, (m.y0 + m.y1) / 2});
            }
        }
        k.chuoi.addAll(chuoi.values());
        for (final int[] d : diem) {
            final int x0 = Math.max(0, d[0]), y0 = Math.max(0, d[1]), x1 = Math.min(k.w - 1, d[2]), y1 = Math.min(k.h - 1, d[3]);
            if (x0 > x1 || y0 > y1) {
                continue;
            }
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    final int i = y * k.w + x;
                    if (k.khoa[i]) {
                        continue;
                    }
                    k.moc[i] = true;
                    k.diBat[i] = true;
                    if (!v.b.di[k.vl[i]]) {
                        k.vl[i] = vlDi;
                    }
                }
            }
            tronHcn(k, x0, y0, x1, y1, 2);
            final int cx = (x0 + x1) / 2, cy = (y0 + y1) / 2;
            noi.add(new int[]{cx, cy});
            if (k.batDau < 0) {
                k.batDau = cy * k.w + cx;
            }
        }
        // o quai
        if (v.quai != null && v.quai.w == k.w && v.quai.h == k.h) {
            for (int y = 0; y < k.h; y++) {
                for (int x = 0; x < k.w; x++) {
                    if (v.quai.coQuai(x, y) && !k.khoa[y * k.w + x]) {
                        if (!k.moc[y * k.w + x]) {
                            k.quai[y * k.w + x] = true;
                        }
                        k.moc[y * k.w + x] = true;
                        k.diBat[y * k.w + x] = true;
                        tron(k, x, y, 1, false);
                    }
                }
            }
        }
        if (k.batDau < 0) {
            k.batDau = (k.h / 2) * k.w + k.w / 2;
        }
        return noi;
    }

    static void tron(final KeHoach k, final int cx, final int cy, final int r, final boolean chinh) {
        for (int y = cy - r; y <= cy + r; y++) {
            for (int x = cx - r; x <= cx + r; x++) {
                if (k.trong(x, y)) {
                    k.giu[y * k.w + x] = true;
                    if (chinh) {
                        k.giuChinh[y * k.w + x] = true;
                    }
                }
            }
        }
    }

    static void tronHcn(final KeHoach k, final int x0, final int y0, final int x1, final int y1, final int r) {
        for (int y = y0 - r; y <= y1 + r; y++) {
            for (int x = x0 - r; x <= x1 + r; x++) {
                if (k.trong(x, y)) {
                    k.giu[y * k.w + x] = true;
                    k.giuChinh[y * k.w + x] = true;
                }
            }
        }
    }

    /**
     * Bien/ho: o quai roi vao nuoc (hoac sat nuoc) duoc doi sang o dat gan nhat con trong - cung so
     * luong, cung mau quai. Luoi moi ghi ra map/pokemon_spawn/map&lt;N&gt; (server doc khi bat che do).
     */
    static int doiChoQuai(final KeHoach k, final DauVao v, final int W) {
        if (v.quai == null || v.quai.w != k.w || v.quai.h != k.h) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < k.vl.length; i++) {
            if (!k.quai[i] || !(k.vl[i] == W || keVl(k, i, W))) {
                continue;
            }
            final boolean[] da = new boolean[k.vl.length];
            final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
            q.add(i);
            da[i] = true;
            int dich = -1;
            while (!q.isEmpty()) {
                final int j = q.poll();
                final int x = j % k.w, y = j / k.w;
                if (k.vl[j] != W && !keVl(k, j, W) && !k.moc[j] && !k.khoa[j] && x > 2 && y > 2 && x < k.w - 3 && y < k.h - 3) {
                    dich = j;
                    break;
                }
                for (int d = 0; d < 4; d++) {
                    final int xx = x + DX4[d], yy = y + DY4[d];
                    if (k.trong(xx, yy) && !da[yy * k.w + xx]) {
                        da[yy * k.w + xx] = true;
                        q.add(yy * k.w + xx);
                    }
                }
            }
            if (dich < 0) {
                continue;
            }
            v.quai.o[dich] = v.quai.o[i];
            v.quai.o[i] = 0;
            k.quai[i] = false;
            k.moc[i] = false;
            k.diBat[i] = false;
            k.quai[dich] = true;
            k.moc[dich] = true;
            k.diBat[dich] = true;
            tron(k, dich % k.w, dich / k.w, 1, false);
            n++;
        }
        return n;
    }

    /** Diem dai dien cho cac cum o quai (luoi 14x14). */
    static List<int[]> cumQuai(final DauVao v, final int buoc) {
        final List<int[]> ra = new ArrayList<int[]>();
        if (v.quai == null || v.quai.w != v.goc.w || v.quai.h != v.goc.h) {
            return ra;
        }
        final Map<Integer, List<int[]>> o = new LinkedHashMap<Integer, List<int[]>>();
        for (int y = 0; y < v.quai.h; y++) {
            for (int x = 0; x < v.quai.w; x++) {
                if (v.quai.coQuai(x, y)) {
                    final int key = (y / buoc) * 1000 + x / buoc;
                    List<int[]> l = o.get(key);
                    if (l == null) {
                        l = new ArrayList<int[]>();
                        o.put(key, l);
                    }
                    l.add(new int[]{x, y});
                }
            }
        }
        for (final List<int[]> l : o.values()) {
            ra.add(tam(l));
        }
        return ra;
    }

    /** Noi cac diem bang duong (cay khung + chuoi). */
    static void noiDuong(final KeHoach k, final BoVL b, final long hat, final List<int[]> diem, final int rong, final int vlDuong) {
        for (final int[] c : khung(diem)) {
            noiHai(k, b, hat, diem.get(c[0]), diem.get(c[1]), rong, vlDuong);
        }
        for (final List<int[]> ch : k.chuoi) {
            for (int i = 0; i + 1 < ch.size(); i++) {
                noiHai(k, b, hat, ch.get(i), ch.get(i + 1), rong, vlDuong);
            }
        }
    }

    static void noiHai(final KeHoach k, final BoVL b, final long hat, final int[] a, final int[] c, final int rong, final int vlDuong) {
        final double[] cp = chiPhiKeHoach(k, b, hat, vlDuong);
        final boolean[] dich = new boolean[k.w * k.h];
        dich[c[1] * k.w + c[0]] = true;
        final List<Integer> p = k.thang ? timDuongThang(k, a[1] * k.w + a[0], dich, cp) : timDuong(k.w, k.h, a[1] * k.w + a[0], dich, cp);
        if (p != null) {
            veDuong(k, p, rong, vlDuong, true);
        }
    }

    /**
     * Duong thi tran: Dijkstra tren (o, huong) voi phat re 8 + bo nhieu -> doan thang, goc vuong.
     */
    static List<Integer> timDuongThang(final KeHoach k, final int tu, final boolean[] dich, final double[] chiPhi) {
        final int n = k.w * k.h;
        final double[] d = new double[n * 4];
        final int[] truoc = new int[n * 4];
        Arrays.fill(d, Double.MAX_VALUE);
        Arrays.fill(truoc, -1);
        final PriorityQueue<double[]> pq = new PriorityQueue<double[]>((p, q) -> Double.compare(p[0], q[0]));
        for (int h = 0; h < 4; h++) {
            d[tu * 4 + h] = 0;
            pq.add(new double[]{0, tu * 4 + h});
        }
        while (!pq.isEmpty()) {
            final double[] e = pq.poll();
            final int st = (int) e[1];
            if (e[0] > d[st]) {
                continue;
            }
            final int i = st / 4, h0 = st % 4;
            if (dich[i] && i != tu) {
                final List<Integer> p = new ArrayList<Integer>();
                for (int s = st; s != -1; s = truoc[s]) {
                    p.add(s / 4);
                }
                return p;
            }
            final int x = i % k.w, y = i / k.w;
            for (int h = 0; h < 4; h++) {
                final int xx = x + DX4[h], yy = y + DY4[h];
                if (!k.trong(xx, yy)) {
                    continue;
                }
                final int j = yy * k.w + xx;
                if (chiPhi[j] >= 1e8) {
                    continue;
                }
                final double c = (chiPhi[j] >= 3 ? 6 : 1) + (h != h0 ? 8 : 0);
                final int sj = j * 4 + h;
                if (d[st] + c < d[sj]) {
                    d[sj] = d[st] + c;
                    truoc[sj] = st;
                    pq.add(new double[]{d[sj], sj});
                }
            }
        }
        return null;
    }

    /** Bao dam ke hoach lien thong: o moc khong toi duoc thi dao duong. */
    static void lienThongKeHoach(final KeHoach k, final BoVL b, final long hat, final int vlDuong) {
        for (int lan = 0; lan < 60; lan++) {
            final boolean[] toi = loangKeHoach(k, b);
            int can = -1;
            for (int i = 0; i < k.moc.length; i++) {
                if (k.moc[i] && !toi[i] && !(k.khoa[i] && !k.cong[i])) {
                    can = i;
                    break;
                }
            }
            if (can < 0) {
                return;
            }
            final double[] cp = chiPhiKeHoach(k, b, hat, vlDuong);
            final List<Integer> p = timDuong(k.w, k.h, can, toi, cp);
            if (p == null) {
                k.moc[can] = false;   // khong the noi (bi nha goc bao kin) - bao cao o buoc kiem
                continue;
            }
            for (final int i : p) {
                if (!k.khoa[i] && !b.di[k.vl[i]]) {
                    k.vl[i] = vlDuong;
                }
                if (!k.khoa[i]) {
                    k.diBat[i] = true;
                    k.giu[i] = true;
                }
            }
        }
    }

    static boolean[] loangKeHoach(final KeHoach k, final BoVL b) {
        final boolean[] di = new boolean[k.w * k.h];
        for (int i = 0; i < di.length; i++) {
            if (k.cong[i]) {
                di[i] = true;
            } else if (k.khoa[i]) {
                di[i] = b.diDuoc(k.khoaTile[i]);
            } else {
                di[i] = b.di[k.vl[i]];
            }
        }
        return loang(k.w, k.h, di, k.batDau);
    }

    static boolean[] loang(final int w, final int h, final boolean[] di, final int tu) {
        final boolean[] toi = new boolean[w * h];
        if (tu < 0 || !di[tu]) {
            return toi;
        }
        final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        q.add(tu);
        toi[tu] = true;
        while (!q.isEmpty()) {
            final int i = q.poll();
            final int x = i % w, y = i / w;
            for (int d = 0; d < 4; d++) {
                final int xx = x + DX4[d], yy = y + DY4[d];
                if (xx >= 0 && yy >= 0 && xx < w && yy < h && di[yy * w + xx] && !toi[yy * w + xx]) {
                    toi[yy * w + xx] = true;
                    q.add(yy * w + xx);
                }
            }
        }
        return toi;
    }

    // =====================================================================================
    // Cac loai ban do
    // =====================================================================================
    static boolean diVl(final BoVL b, final int v) {
        return v >= 0 && v < b.di.length && b.di[v];
    }

    static boolean keVl(final KeHoach k, final int i, final int v) {
        final int x = i % k.w, y = i / k.w;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (k.trong(x + dx, y + dy) && k.vl[(y + dy) * k.w + x + dx] == v) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Vung lien thong vat lieu v nho hon toiThieu o -> doi thanh 'thay'. */
    static void boVungNho(final KeHoach k, final int v, final int thay, final int toiThieu) {
        final boolean[] da = new boolean[k.w * k.h];
        for (int s = 0; s < da.length; s++) {
            if (da[s] || k.vl[s] != v || k.khoa[s]) {
                continue;
            }
            final List<Integer> vung = new ArrayList<Integer>();
            final ArrayDeque<Integer> q = new ArrayDeque<Integer>();
            q.add(s);
            da[s] = true;
            while (!q.isEmpty()) {
                final int i = q.poll();
                vung.add(i);
                final int x = i % k.w, y = i / k.w;
                for (int d = 0; d < 4; d++) {
                    final int xx = x + DX4[d], yy = y + DY4[d];
                    if (k.trong(xx, yy) && !da[yy * k.w + xx] && k.vl[yy * k.w + xx] == v && !k.khoa[yy * k.w + xx]) {
                        da[yy * k.w + xx] = true;
                        q.add(yy * k.w + xx);
                    }
                }
            }
            if (vung.size() < toiThieu) {
                for (final int i : vung) {
                    k.vl[i] = thay;
                }
            }
        }
    }

    static void biengioi(final KeHoach k, final DauVao v, final long hat, final Random rd, final int W, final int D) {
        // phia it diem co dinh nhat
        final int[] dem = new int[4];
        for (int i = 0; i < k.moc.length; i++) {
            if (!k.moc[i]) {
                continue;
            }
            final int x = i % k.w, y = i / k.w;
            if (y < k.h / 3) {
                dem[0]++;
            }
            if (y > k.h * 2 / 3) {
                dem[1]++;
            }
            if (x < k.w / 3) {
                dem[2]++;
            }
            if (x > k.w * 2 / 3) {
                dem[3]++;
            }
        }
        int phia = 0;
        for (int i = 1; i < 4; i++) {
            if (dem[i] < dem[phia]) {
                phia = i;
            }
        }
        final int sau = (int) (Math.min(k.w, k.h) * 0.22);
        final boolean[] vat = new boolean[k.w * k.h];
        for (int i = 0; i < vat.length; i++) {
            vat[i] = k.giuChinh[i] || k.khoa[i];
        }
        final int[] dGiu = khoangCach(k.w, k.h, vat);
        final int G = v.b.vl("G");
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                final int i = y * k.w + x;
                if (k.khoa[i]) {
                    continue;
                }
                final int d = phia == 0 ? y : phia == 1 ? k.h - 1 - y : phia == 2 ? x : k.w - 1 - x;
                final int doc = phia < 2 ? x : y;
                final double bo = sau * (0.6 + 0.8 * fbm(doc, 0, hat + 77, 14));
                if (d < bo && !k.giuChinh[i] && dGiu[i] >= 3) {
                    k.vl[i] = W;
                } else if (d < bo + 1.5) {
                    k.vl[i] = G;
                } else if (d < bo + 5 + 3 * fbm(x, y, hat + 78, 6)) {
                    k.vl[i] = D;
                }
            }
        }
        boVungNho(k, W, G, 16);
        doiChoQuai(k, v, W);
    }

    /** Khong de dat/cat sat nuoc (bo go chi co o ben tau): nuoc ke dat -> co. */
    static void vienCo(final KeHoach k, final int W, final int D, final int G) {
        final int[] moi = k.vl.clone();
        for (int i = 0; i < moi.length; i++) {
            if (k.vl[i] == W && !k.khoa[i] && keVl(k, i, D)) {
                moi[i] = G;
            }
        }
        System.arraycopy(moi, 0, k.vl, 0, moi.length);
    }

    static void coCao(final KeHoach k, final DauVao v, final long hat, final int T, final int G, final int r) {
        if (v.quai == null || v.quai.w != k.w || v.quai.h != k.h) {
            return;
        }
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                if (!v.quai.coQuai(x, y)) {
                    continue;
                }
                final int rr = r + (fbm(x, y, hat + 50, 5) > 0.55 ? 1 : 0);
                for (int dy = -rr; dy <= rr; dy++) {
                    for (int dx = -rr; dx <= rr; dx++) {
                        final int xx = x + dx, yy = y + dy;
                        if (k.trong(xx, yy) && !k.khoa[yy * k.w + xx] && k.vl[yy * k.w + xx] == G && dx * dx + dy * dy <= rr * rr + 1) {
                            k.vl[yy * k.w + xx] = T;
                        }
                    }
                }
            }
        }
    }

    /** Thi tran / thanh: tem nha co cua, quang truong quanh NPC, duong rong, ao, nha them. */

    // =====================================================================================
    // Cac loai ban do (bo t ngoai troi)
    // =====================================================================================
    /** Map ma vi tri quai gan voi logic server (chien truong 40, nui 42): KHONG doi cho quai. */
    static final Set<Integer> GIU_VI_TRI_QUAI = new HashSet<Integer>(Arrays.asList(40, 42));
    static final String[] TEN_THI_TRAN = {"Pallet", "Viridian", "Pewter", "Cerulean", "Vermilion", "Lavender", "Celadon", "Saffron",
        "Fuchsia", "Cinnabar", "New Bark", "Cherrygrove", "Violet", "Azalea", "Goldenrod", "Ecruteak", "Olivine", "Mahogany"};

    static boolean laTenThiTran(final String ten) {
        if (ten.startsWith("Rung") || ten.startsWith("Hang") || ten.startsWith("Nui")) {
            return false;
        }
        for (final String t : TEN_THI_TRAN) {
            if (ten.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lap cac khoang co trong lon bang diem nhan thiet ke (lum cay, cum da, hang go dat mot chieu, cay mong,
     * ao sen, dong hoa): moi lan chon o xa moi thu nhat, dat mot diem nhan, lap toi khi khong con khoang &gt;= 6 o.
     */
    static void lapChoTrong(final KeHoach k, final BoVL b, final Random rd, final long hat, final int G, final int W, final int F) {
        final int n = k.w * k.h;
        final boolean[] daLap = new boolean[n];
        for (int lan = 0; lan < 80; lan++) {
            final boolean[] vat = new boolean[n];
            for (int i = 0; i < n; i++) {
                vat[i] = k.vl[i] != G || k.khoa[i] || k.moc[i] || k.diBat[i] || daLap[i];
            }
            final int[] d = khoangCach(k.w, k.h, vat);
            int max = 0;
            for (final int x : d) {
                if (x != Integer.MAX_VALUE) {
                    max = Math.max(max, x);
                }
            }
            if (max < 6) {
                return;
            }
            final List<Integer> ung = new ArrayList<Integer>();
            for (int i = 0; i < n; i++) {
                if (d[i] != Integer.MAX_VALUE && d[i] >= max - 1) {
                    ung.add(i);
                }
            }
            final int c = ung.get(rd.nextInt(ung.size()));
            final int cx = c % k.w, cy = c / k.w;
            final double chon = rd.nextDouble();
            int loai = chon < 0.3 ? 0 : (chon < 0.5 ? 1 : (chon < 0.7 ? 2 : (chon < 0.8 ? 3 : (chon < 0.88 ? 4 : 5))));
            if ((k.tuyet || k.saMac) && (loai == 2 || loai == 3 || loai == 5 || (k.tuyet && loai == 4))) {
                loai = rd.nextBoolean() ? 0 : 1;
            }
            switch (loai) {
                case 0: // lum cay
                    for (int y = cy - 3; y <= cy + 3; y++) {
                        for (int x = cx - 3; x <= cx + 3; x++) {
                            if (k.trong(x, y) && tuDo(k, x, y) && k.vl[y * k.w + x] == G
                                    && Math.hypot(x - cx, y - cy) < 2.6 + 0.8 * fbm(x, y, hat + lan, 3)) {
                                k.vl[y * k.w + x] = F;
                            }
                        }
                    }
                    break;
                case 1: // cum da
                    for (int t = 0; t < 6; t++) {
                        final int x = cx + rd.nextInt(5) - 2, y = cy + rd.nextInt(5) - 2;
                        if (tuDo(k, x, y) && k.vl[y * k.w + x] == G) {
                            k.dat(x, y, b.id(k.tuyet ? "da_tuyet" : (k.saMac ? "da_cat" : (t % 2 == 0 ? "da" : "soi"))));
                        }
                    }
                    break;
                case 2: { // hang go dat (ledge) mot chieu
                    final int dai = 7 + rd.nextInt(7);
                    final int x0 = cx - dai / 2;
                    boolean ok = true;
                    for (int x = x0; x < x0 + dai && ok; x++) {
                        for (int y = cy - 1; y <= cy + 1; y++) {
                            if (!tuDo(k, x, y) || k.vl[y * k.w + x] != G) {
                                ok = false;
                            }
                        }
                    }
                    if (ok) {
                        for (int x = x0; x < x0 + dai; x++) {
                            k.dat(x, cy, b.id(x == x0 ? "goc_l" : (x == x0 + dai - 1 ? "goc_r" : "goc_m")));
                        }
                    }
                    break;
                }
                case 3: // cay mong
                    for (int t = 0; t < 3; t++) {
                        final int x = cx - 2 + t * 2, y = cy + (t % 2);
                        if (tuDo(k, x, y) && k.vl[y * k.w + x] == G) {
                            k.dat(x, y, b.id("cay_mong"));
                        }
                    }
                    break;
                case 4: // ao sen
                    for (int y = cy - 3; y <= cy + 3; y++) {
                        for (int x = cx - 3; x <= cx + 3; x++) {
                            if (k.trong(x, y) && tuDo(k, x, y) && k.vl[y * k.w + x] == G
                                    && Math.hypot(x - cx, (y - cy) * 1.2) < 2.4 + 0.8 * fbm(x, y, hat + 100 + lan, 3)) {
                                k.vl[y * k.w + x] = W;
                            }
                        }
                    }
                    break;
                default: // dong hoa
                    for (int y = cy - 2; y <= cy + 2; y++) {
                        for (int x = cx - 2; x <= cx + 2; x++) {
                            if (tuDo(k, x, y) && k.vl[y * k.w + x] == G && Math.hypot(x - cx, y - cy) < 2.3 && bam(x, y, hat + 33) < 0.8) {
                                k.dat(x, y, b.id(new String[]{"hoa_vang", "hoa_trang", "hoa_cam"}[(x + y * 2) % 3]));
                            }
                        }
                    }
                    break;
            }
            for (int y = cy - 4; y <= cy + 4; y++) {
                for (int x = cx - 4; x <= cx + 4; x++) {
                    if (k.trong(x, y)) {
                        daLap[y * k.w + x] = true;
                    }
                }
            }
        }
    }

    /** Ben go: noi duong dat sat nuoc (cach 1-2 o) -> cau go thang ra 3 o nuoc (toi da 2 ben/map). */
    static void datBenTau(final KeHoach k, final BoVL b, final int D, final int W, final int B) {
        if (W < 0 || B < 0) {
            return;
        }
        int ben = 0;
        for (int i = 0; i < k.vl.length && ben < 2; i++) {
            if (k.vl[i] != D || k.khoa[i]) {
                continue;
            }
            final int x = i % k.w, y = i / k.w;
            for (int h = 0; h < 4 && ben < 2; h++) {
                int buoc = 1;
                while (buoc <= 2 && k.trong(x + DX4[h] * buoc, y + DY4[h] * buoc) && k.vl[(y + DY4[h] * buoc) * k.w + x + DX4[h] * buoc] != W) {
                    buoc++;
                }
                if (buoc > 2) {
                    continue;
                }
                boolean ok = true;
                for (int t = 1; t < buoc + 3 && ok; t++) {
                    final int xx = x + DX4[h] * t, yy = y + DY4[h] * t;
                    if (!k.trong(xx, yy) || k.khoa[yy * k.w + xx] || k.cong[yy * k.w + xx]) {
                        ok = false;
                    } else if (t >= buoc && k.vl[yy * k.w + xx] != W) {
                        ok = false;
                    }
                }
                if (!ok) {
                    continue;
                }
                for (int t = 1; t < buoc + 3; t++) {
                    k.vl[(y + DY4[h] * t) * k.w + x + DX4[h] * t] = B;
                }
                ben++;
            }
        }
    }

    static KeHoach keHoachNgoaiTroi(final DauVao v, final Random rd) {
        final BoVL b = v.b;
        final boolean tuyet = v.spec.loai.equals("tuyet"), saMac = v.spec.loai.equals("sa_mac");
        final String loai = (tuyet || saMac) ? "nui" : v.spec.loai;
        final long hat = v.spec.hat;
        final int G = b.vl("G"), D = b.vl("D"), W = b.vl("W"), F = b.vl("F"), T = b.vl("T"), R = b.vl("R"), S = b.vl("S");
        final KeHoach k = khoiTao(v, G);
        k.tuyet = tuyet && b.co("N");
        k.saMac = saMac;
        // quai: luoi cu duoc xep lai vao cac bai co cao thiet ke (cung so luong, cung mau quai)
        final boolean doiQuai = !GIU_VI_TRI_QUAI.contains(v.spec.id) && v.quai != null && v.quai.w == k.w && v.quai.h == k.h;
        final List<int[]> quaiCu = new ArrayList<int[]>();
        if (doiQuai) {
            for (int i = 0; i < v.quai.o.length; i++) {
                if (v.quai.o[i] > 0 && v.quai.o[i] != 255) {
                    quaiCu.add(new int[]{i % k.w, i / k.w, v.quai.o[i]});
                    v.quai.o[i] = 0;
                }
            }
        }
        final List<int[]> noi = danhDauMoc(k, v, G);
        final int dien = k.w * k.h;
        final boolean nho = k.w <= 24 || k.h <= 24;
        // 1. vien tu nhien day: rung (vach da o nui)
        // tuyet/sa mac: vien bang cay thong tuyet / cay dua (mat vach co mau co, khong hop nen tuyet/cat)
        final int vienVl = (loai.equals("nui") && !tuyet && !saMac) ? R : F;
        final double day = nho ? 2 : (loai.equals("rung") ? 5 : 3.5), bien = nho ? 1 : 3;
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                final int i = y * k.w + x;
                if (k.giu[i] || k.khoa[i]) {
                    continue;
                }
                final int m = Math.min(Math.min(x, y), Math.min(k.w - 1 - x, k.h - 1 - y));
                if (m < day + bien * fbm(x, y, hat + 1, 9)) {
                    k.vl[i] = vienVl;
                }
            }
        }
        // 2. nuoc lon
        if (loai.equals("bien")) {
            biengioi(k, v, hat, rd, W, S);
        }
        if (loai.equals("ho")) {
            final double tl = Math.max(10, Math.min(k.w, k.h) / 3.5);
            final boolean[] vat = new boolean[dien];
            for (int i = 0; i < dien; i++) {
                vat[i] = k.giuChinh[i] || k.khoa[i];
            }
            final int[] dGiu = khoangCach(k.w, k.h, vat);
            for (int y = 0; y < k.h; y++) {
                for (int x = 0; x < k.w; x++) {
                    final int i = y * k.w + x;
                    if (k.giuChinh[i] || k.khoa[i] || dGiu[i] < 3) {
                        continue;
                    }
                    final double tam = 1 - Math.hypot(x - k.w / 2.0, y - k.h / 2.0) / (Math.max(k.w, k.h) * 0.7);
                    if (fbm(x, y, hat + 21, tl) + 0.35 * tam > 0.66) {
                        k.vl[i] = W;
                    }
                }
            }
            boVungNho(k, W, G, 30);
            doiChoQuai(k, v, W);
        }
        // 3. duong dat uon luon noi cong + NPC + diem co dinh
        noiDuong(k, b, hat, noi, nho ? 2 : 3, D);
        // 4. lang nho cho map mang ten thi tran
        if (!nho && laTenThiTran(v.spec.ten)) {
            final int[] p = datLang(k, b, rd, D);
            if (p != null && !noi.isEmpty()) {
                int best = 0;
                for (int i = 1; i < noi.size(); i++) {
                    if (Math.hypot(noi.get(i)[0] - p[0], noi.get(i)[1] - p[1]) < Math.hypot(noi.get(best)[0] - p[0], noi.get(best)[1] - p[1])) {
                        best = i;
                    }
                }
                noiHai(k, b, hat, new int[]{p[0], p[1] - 4}, noi.get(best), 3, D);
            }
        }
        // 5. dia hinh
        final boolean[] daDat = new boolean[dien];
        final int soDam = Math.max(1, dien / 1100);
        if (loai.equals("rung")) {
            datDam(k, rd, hat + 3, F, soDam * 3, 2, 7, daDat);
            datDam(k, rd, hat + 2, W, Math.max(1, dien / 8000), 3, 5, daDat);
        } else if (loai.equals("dong_co")) {
            datDam(k, rd, hat + 2, W, Math.max(1, dien / 4500), 3, 6, daDat);
            datDam(k, rd, hat + 3, F, soDam, 2, 5, daDat);
        } else if (loai.equals("nui") && (tuyet || saMac)) {
            datDam(k, rd, hat + 5, F, soDam * 2, 2, 4, daDat);
        } else if (loai.equals("nui")) {
            datDam(k, rd, hat + 3, R, soDam * 2, 2, 6, daDat);
            datDam(k, rd, hat + 5, F, soDam, 2, 4, daDat);
        } else if (loai.equals("ho") || loai.equals("bien")) {
            datDam(k, rd, hat + 3, F, soDam, 2, 5, daDat);
        } else {
            datDam(k, rd, hat + 3, F, soDam * 2, 2, 5, daDat);
            datDam(k, rd, hat + 4, R, Math.max(1, soDam / 2), 2, 4, daDat);
            datDam(k, rd, hat + 2, W, Math.max(1, dien / 6000), 3, 5, daDat);
        }
        // khong de rung/vach cham nuoc
        for (int i = 0; i < dien; i++) {
            if ((k.vl[i] == F || k.vl[i] == R) && !k.khoa[i] && keVl(k, i, W)) {
                k.vl[i] = G;
            }
        }
        boVungNho(k, W, G, 12);
        lamMin(k, b, 1);
        vienCo(k, W, D, G);
        lamDay(k, b, new int[]{F, R, W, D, S});
        // 6. bai co cao (5-30 o) doc duong + xep quai vao (sau lam min de bai khong bi an mon)
        if (doiQuai) {
            baiCoVaQuai(k, v, quaiCu, hat, rd, T, G, D);
        } else if (!k.tuyet) {
            coCao(k, v, hat, T, G, 1);
        }
        // 7. lap cho trong: lum cay, cum da, go dat, cay mong, ao sen, dong hoa (mat do thay doi)
        if (!nho) {
            lapChoTrong(k, b, rd, hat, G, W, F);
        }
        datBenTau(k, b, D, W, b.vl("B"));
        lienThongKeHoach(k, b, hat, D);
        // 8. diem nhan + chi tiet
        datBienBao(k, b, v);
        if (!nho && !k.tuyet && !k.saMac && (loai.equals("dong_co") || loai.equals("duong"))) {
            datVuon(k, b, rd, 6 + rd.nextInt(3), 4 + rd.nextInt(2));
        }
        datCayTo(k, b, G, loai.equals("rung") ? 0.03 : 0.006, hat);
        datCayLe(k, b, G, loai.equals("nui") ? 0.012 : 0.006, hat,
                k.tuyet ? new String[]{"da_tuyet"} : (k.saMac ? new String[]{"da_cat"} : new String[]{"bui", "da", "goc_cay", "soi"}));
        if (k.tuyet || k.saMac) {
            final int nen = k.tuyet ? b.vl("N") : S;
            for (int i = 0; i < dien; i++) {
                if ((k.vl[i] == G || k.vl[i] == D) && !k.khoa[i]) {
                    k.vl[i] = nen;
                }
            }
        }
        return k;
    }

    /**
     * Bai co cao thiet ke: 5-30 o moi bai, uu tien doc duong (cach duong 2-7 o) va gan cum quai cu, roi xep
     * tung con quai cu vao o co cao trong gan cho cu nhat (giu phan bo khu vuc/cap do). Luoi moi -> map/pokemon_spawn.
     */
    static void baiCoVaQuai(final KeHoach k, final DauVao v, final List<int[]> quaiCu, final long hat, final Random rd, final int T, final int G,
                            final int D) {
        if (quaiCu.isEmpty()) {
            return;
        }
        final int n = k.w * k.h;
        final boolean[] duong = new boolean[n];
        for (int i = 0; i < n; i++) {
            duong[i] = k.vl[i] == D;
        }
        final int[] dDuong = khoangCach(k.w, k.h, duong);
        final boolean[] ungVien = new boolean[n];
        for (int i = 0; i < n; i++) {
            final int x = i % k.w, y = i / k.w;
            ungVien[i] = k.vl[i] == G && !k.khoa[i] && !k.giuChinh[i] && x > 2 && y > 2 && x < k.w - 3 && y < k.h - 3;
        }
        final int canCo = Math.max(quaiCu.size() + 8, (int) (quaiCu.size() * 1.7));
        // hat giong: cum quai cu (luoi 10x10) - keo ve o ung vien gan nhat, uu tien cach duong 2-7
        final List<int[]> hatGiong = new ArrayList<int[]>();
        final List<Integer> soQuaiCum = new ArrayList<Integer>();
        final Map<Integer, List<int[]>> cum = new LinkedHashMap<Integer, List<int[]>>();
        for (final int[] q : quaiCu) {
            cum.computeIfAbsent((q[1] / 16) * 1000 + q[0] / 16, z -> new ArrayList<int[]>()).add(q);
        }
        for (final List<int[]> l : cum.values()) {
            hatGiong.add(tam(l));
            soQuaiCum.add(l.size());
        }
        int coDat = 0;
        final boolean[] laCo = new boolean[n];
        for (int lan = 0; (lan < hatGiong.size() || coDat < canCo) && lan < hatGiong.size() * 3 + 40; lan++) {
            final int[] g0 = lan < hatGiong.size() ? hatGiong.get(lan) : hatGiong.get(rd.nextInt(hatGiong.size()));
            // o bat dau: ung vien gan g0 nhat, dDuong trong [2,7]
            int bat = -1;
            double bd = 1e9;
            for (int i = 0; i < n; i++) {
                if (!ungVien[i] || laCo[i]) {
                    continue;
                }
                final double d = Math.hypot(i % k.w - g0[0], i / k.w - g0[1]) + (dDuong[i] < 2 ? 6 : (dDuong[i] > 12 ? (dDuong[i] - 12) * 0.15 : 0))
                        + (lan >= hatGiong.size() ? rd.nextDouble() * 20 : 0);
                if (d < bd) {
                    bd = d;
                    bat = i;
                }
            }
            if (bat < 0) {
                break;
            }
            // moi cum quai cu co bai rieng, co ~1.8 o co / con quai (5..30 o)
            final int coToi = lan < hatGiong.size() ? Math.max(5, Math.min(30, (int) (soQuaiCum.get(lan) * 1.8) + rd.nextInt(3)))
                    : 8 + rd.nextInt(12);
            final PriorityQueue<double[]> pq = new PriorityQueue<double[]>((p, q) -> Double.compare(p[0], q[0]));
            pq.add(new double[]{0, bat});
            final Set<Integer> da = new HashSet<Integer>();
            int trongBai = 0;
            while (!pq.isEmpty() && trongBai < coToi) {
                final int i = (int) pq.poll()[1];
                if (!da.add(i) || !ungVien[i] || laCo[i] || dDuong[i] < 1) {
                    continue;
                }
                laCo[i] = true;
                k.vl[i] = T;
                trongBai++;
                coDat++;
                final int x = i % k.w, y = i / k.w;
                for (int d = 0; d < 4; d++) {
                    final int xx = x + DX4[d], yy = y + DY4[d];
                    if (k.trong(xx, yy)) {
                        final int j = yy * k.w + xx;
                        pq.add(new double[]{Math.hypot(xx - bat % k.w, (yy - bat / k.w) * 1.3) + 1.5 * bam(xx, yy, hat + 77), j});
                    }
                }
            }
            // bao quanh bai bang 1 vong co thuong (de bai tach roi, khong dinh nhau)
            for (final int i : new ArrayList<Integer>(da)) {
                if (!laCo[i]) {
                    continue;
                }
                final int x = i % k.w, y = i / k.w;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (k.trong(x + dx, y + dy) && !laCo[(y + dy) * k.w + x + dx]) {
                            ungVien[(y + dy) * k.w + x + dx] = false;
                        }
                    }
                }
            }
        }
        // xep quai
        final List<Integer> o = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            if (laCo[i]) {
                o.add(i);
            }
        }
        final boolean[] dung = new boolean[n];
        for (final int[] q : quaiCu) {
            int best = -1;
            double bd = 1e9;
            for (final int i : o) {
                if (dung[i]) {
                    continue;
                }
                final double d = Math.hypot(i % k.w - q[0], i / k.w - q[1]);
                if (d < bd) {
                    bd = d;
                    best = i;
                }
            }
            if (best < 0) {
                // het co cao: o di duoc gan nhat
                for (int i = 0; i < n; i++) {
                    if (!dung[i] && !k.khoa[i] && !k.moc[i] && k.vl[i] == G) {
                        final double d = Math.hypot(i % k.w - q[0], i / k.w - q[1]);
                        if (d < bd) {
                            bd = d;
                            best = i;
                        }
                    }
                }
            }
            if (best < 0) {
                continue;
            }
            dung[best] = true;
            v.quai.o[best] = q[2];
            k.quai[best] = true;
            k.moc[best] = true;
            k.diBat[best] = true;
        }
    }

    /**
     * Vung vat lieu mong hon 2 o (theo hang hoac cot) khong ve duoc bang autotile goc (can 2x2 de co
     * goc trong) -> tra ve vat lieu xung quanh. Lap toi khi on dinh.
     */
    static void lamDay(final KeHoach k, final BoVL b, final int[] dsVl) {
        for (int lan = 0; lan < 6; lan++) {
            boolean doi = false;
            for (int y = 0; y < k.h; y++) {
                for (int x = 0; x < k.w; x++) {
                    final int i = y * k.w + x;
                    final int v = k.vl[i];
                    boolean xet = false;
                    for (final int d : dsVl) {
                        if (d == v) {
                            xet = true;
                        }
                    }
                    if (!xet || k.khoa[i]) {
                        continue;
                    }
                    // co thuoc mot khoi 2x2 cung vat lieu khong
                    boolean coKhoi = false;
                    for (int dy = -1; dy <= 0 && !coKhoi; dy++) {
                        for (int dx = -1; dx <= 0; dx++) {
                            boolean du = true;
                            for (int a = 0; a < 2 && du; a++) {
                                for (int c = 0; c < 2; c++) {
                                    final int xx = x + dx + c, yy = y + dy + a;
                                    if (!k.trong(xx, yy) || k.vl[yy * k.w + xx] != v) {
                                        du = false;
                                        break;
                                    }
                                }
                            }
                            if (du) {
                                coKhoi = true;
                                break;
                            }
                        }
                    }
                    if (coKhoi) {
                        continue;
                    }
                    // thay bang vat lieu pho bien nhat quanh (uu tien di duoc neu o bat buoc di)
                    final int[] dem = new int[16];
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if ((dx != 0 || dy != 0) && k.trong(x + dx, y + dy)) {
                                final int u = k.vl[(y + dy) * k.w + x + dx];
                                if (u != v && u != O) {
                                    dem[u]++;
                                }
                            }
                        }
                    }
                    int best = 0;
                    for (int u = 0; u < b.ten.length; u++) {
                        if (dem[u] > dem[best] && (!k.diBat[i] || b.di[u])) {
                            best = u;
                        }
                    }
                    if (k.diBat[i] && !b.di[best]) {
                        best = 0;
                    }
                    if (best != v) {
                        k.vl[i] = best;
                        doi = true;
                    }
                }
            }
            if (!doi) {
                return;
            }
        }
    }

    /**
     * Thi tran bo t: duong 2 o thang goc vuong, quang truong lat da chu nhat (tuong + den), nha dung o cua,
     * nha doc pho quay ra duong (bon hoa, hom thu), den duong, ao, vuon hoa, vien rung.
     */
    static KeHoach keHoachLang(final DauVao v, final Random rd) {
        final BoVL b = v.b;
        final long hat = v.spec.hat;
        final int G = b.vl("G"), D = b.vl("D"), W = b.vl("W"), F = b.vl("F"), K = b.vl("K");
        final KeHoach k = khoiTao(v, G);
        k.thang = true;
        final List<int[]> noi = danhDauMoc(k, v, G);
        int sx = 0, sy = 0, n = 0;
        for (final DinhDangBanDo.Diem d : v.goc.npc) {
            sx += d.x;
            sy += d.y;
            n++;
        }
        final int cx = n > 0 ? sx / n : k.w / 2, cy = n > 0 ? sy / n : k.h / 2;
        final List<List<int[]>> cua = new ArrayList<List<int[]>>();
        for (final List<int[]> cum : cumCong(v.goc)) {
            if (!laMep(cum, k.w, k.h)) {
                cua.add(cum);
            }
        }
        cua.sort((p, q) -> Double.compare(Math.hypot(tam(p)[0] - cx, tam(p)[1] - cy), Math.hypot(tam(q)[0] - cx, tam(q)[1] - cy)));
        for (int i = 0; i < cua.size(); i++) {
            datNhaCua(k, b, cua.get(i), i == 0 ? 0 : (i == 1 ? 1 : 2 + (i % 3)), i < 2);
        }
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                final int i = y * k.w + x;
                if (k.giu[i] || k.khoa[i]) {
                    continue;
                }
                final int m = Math.min(Math.min(x, y), Math.min(k.w - 1 - x, k.h - 1 - y));
                if (m < 2 + 2.5 * fbm(x, y, hat + 1, 7)) {
                    k.vl[i] = F;
                }
            }
        }
        noiDuong(k, b, hat, noi, 2, D);
        datNhaDuong(k, b, rd, Math.max(2, k.w * k.h / 700), D);
        final boolean[] daDat = new boolean[k.w * k.h];
        datDam(k, rd, hat + 2, W, Math.max(1, k.w * k.h / 3000), 2, 4, daDat);
        datDam(k, rd, hat + 3, F, Math.max(1, k.w * k.h / 1800), 2, 3, daDat);
        lamMin(k, b, 1);
        vienCo(k, W, D, G);
        lamDay(k, b, new int[]{F, W, D});
        // quang truong lat da
        if (n > 0) {
            final int rx = k.w >= 60 ? 5 : 4, ry = 3;
            final int x0 = Math.max(3, cx - rx), x1 = Math.min(k.w - 4, cx + rx), y0 = Math.max(3, cy - ry), y1 = Math.min(k.h - 4, cy + ry);
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    if (!k.khoa[y * k.w + x]) {
                        k.vl[y * k.w + x] = K;
                        k.diBat[y * k.w + x] = true;
                        k.giu[y * k.w + x] = true;
                    }
                }
            }
            boolean trong = true;
            for (int y = cy - 1; y <= cy; y++) {
                for (int x = cx - 1; x <= cx; x++) {
                    final int i = y * k.w + x;
                    if (k.moc[i] || k.khoa[i] || k.cong[i]) {
                        trong = false;
                    }
                }
            }
            if (trong && b.co("tuong_0")) {
                k.dat(cx - 1, cy - 1, b.id("tuong_0"));
                k.dat(cx, cy - 1, b.id("tuong_1"));
                k.dat(cx - 1, cy, b.id("tuong_2"));
                k.dat(cx, cy, b.id("tuong_3"));
            }
            for (final int[] g : new int[][]{{x0 + 1, y0 + 1}, {x1 - 1, y0 + 1}, {x0 + 1, y1 - 1}, {x1 - 1, y1 - 1}}) {
                final int i = g[1] * k.w + g[0];
                if (!k.moc[i] && !k.khoa[i] && !k.cong[i] && b.co("den")) {
                    k.dat(g[0], g[1], b.id("den"));
                }
            }
            // noi 4 canh quang truong ra duong gan nhat
            for (final int[] m : new int[][]{{cx, y0 - 1}, {cx, y1 + 1}, {x0 - 1, cy}, {x1 + 1, cy}}) {
                int best = -1;
                double bd = 1e9;
                for (int i = 0; i < k.vl.length; i++) {
                    if (k.vl[i] == D) {
                        final double dd = Math.hypot(i % k.w - m[0], i / k.w - m[1]);
                        if (dd < bd) {
                            bd = dd;
                            best = i;
                        }
                    }
                }
                if (best >= 0 && bd < 14 && k.trong(m[0], m[1])) {
                    noiHai(k, b, hat, m, new int[]{best % k.w, best / k.w}, 2, D);
                }
            }
        }
        lienThongKeHoach(k, b, hat, D);
        datBienBao(k, b, v);
        datVuon(k, b, rd, 5, 4);
        datDenDuong(k, b, hat, D);
        datCayTo(k, b, G, 0.008, hat);
        datCayLe(k, b, G, 0.008, hat, new String[]{"bui", "cay_mong", "thung", "goc_cay"});
        return k;
    }

    /** Den duong canh duong (khong tren duong), cach nhau &gt;= 6 o. */
    static void datDenDuong(final KeHoach k, final BoVL b, final long hat, final int vlDuong) {
        if (!b.co("den")) {
            return;
        }
        final List<int[]> den = new ArrayList<int[]>();
        for (int y = 2; y < k.h - 2; y++) {
            for (int x = 2; x < k.w - 2; x++) {
                if (k.vl[y * k.w + x] != vlDuong || bam(x, y, hat + 91) > 0.08) {
                    continue;
                }
                for (int d = 0; d < 4; d++) {
                    final int xx = x + DX4[d], yy = y + DY4[d];
                    if (!tuDo(k, xx, yy) || k.vl[yy * k.w + xx] == vlDuong) {
                        continue;
                    }
                    boolean gan = false;
                    for (final int[] e : den) {
                        if (Math.abs(e[0] - xx) + Math.abs(e[1] - yy) < 6) {
                            gan = true;
                        }
                    }
                    if (!gan) {
                        k.dat(xx, yy, b.id("den"));
                        den.add(new int[]{xx, yy});
                    }
                    break;
                }
            }
        }
    }

    /** Thi tran: nha Kenney dung o cua, Trung tam Pokemon + Cua hang o gan quang truong, ao, hang rao, cay. */
    static KeHoach keHoachThiTran(final DauVao v, final Random rd) {
        final BoVL b = v.b;
        final long hat = v.spec.hat;
        final boolean thanh = b.bo.equals("t_thanh");
        final int nen = thanh ? b.vl("P") : b.vl("G");
        final int duong = thanh ? b.vl("A") : b.vl("D");
        final int quangTruong = thanh ? b.vl("A") : b.vl("D");
        final int W = b.vl("W");
        final int F = thanh ? -1 : b.vl("F");
        final int G = b.vl("G");
        final KeHoach k = khoiTao(v, nen);
        final List<int[]> noi = danhDauMoc(k, v, nen);
        // tam quang truong = trung binh NPC
        int sx = 0, sy = 0, n = 0;
        for (final DinhDangBanDo.Diem d : v.goc.npc) {
            sx += d.x;
            sy += d.y;
            n++;
        }
        final int cx = n > 0 ? sx / n : k.w / 2, cy = n > 0 ? sy / n : k.h / 2;
        // nha o cua: gan quang truong nhat = Trung tam Pokemon, ke tiep = Cua hang
        final List<List<int[]>> cua = new ArrayList<List<int[]>>();
        for (final List<int[]> cum : cumCong(v.goc)) {
            if (!laMep(cum, k.w, k.h)) {
                cua.add(cum);
            }
        }
        cua.sort((p, q) -> Double.compare(Math.hypot(tam(p)[0] - cx, tam(p)[1] - cy), Math.hypot(tam(q)[0] - cx, tam(q)[1] - cy)));
        for (int i = 0; i < cua.size(); i++) {
            datNhaCua(k, b, cua.get(i), i == 0 ? 0 : (i == 1 ? 1 : 2 + (i % 4)), i < 2);
        }
        // thanh (Cao nguyen Indigo): dai lo cat doc + ngang, bai co chu nhat co cay - khong dung dam tron
        if (thanh) {
            final int ax = k.w / 2;
            for (int y = 3; y < k.h - 2; y++) {
                for (int x = ax - 2; x <= ax + 1; x++) {
                    if (k.trong(x, y) && !k.khoa[y * k.w + x]) {
                        k.vl[y * k.w + x] = quangTruong;
                    }
                }
            }
            for (int x = 3; x < k.w - 3; x++) {
                for (int y = cy - 1; y <= cy; y++) {
                    if (k.trong(x, y) && !k.khoa[y * k.w + x]) {
                        k.vl[y * k.w + x] = quangTruong;
                    }
                }
            }
            for (int thu = 0, dat = 0; thu < 600 && dat < k.w * k.h / 260; thu++) {
                final int rw = 5 + rd.nextInt(4), rh = 4 + rd.nextInt(3);
                final int x0 = 3 + rd.nextInt(Math.max(1, k.w - rw - 6)), y0 = 3 + rd.nextInt(Math.max(1, k.h - rh - 6));
                if (!hcnTuDo(k, x0 - 1, y0 - 1, x0 + rw, y0 + rh)) {
                    continue;
                }
                boolean tron = true;
                for (int y = y0 - 1; y <= y0 + rh && tron; y++) {
                    for (int x = x0 - 1; x <= x0 + rw; x++) {
                        if (k.vl[y * k.w + x] != nen) {
                            tron = false;
                            break;
                        }
                    }
                }
                if (!tron) {
                    continue;
                }
                for (int y = y0; y < y0 + rh; y++) {
                    for (int x = x0; x < x0 + rw; x++) {
                        k.vl[y * k.w + x] = G;
                    }
                }
                dat++;
            }
        }
        // quang truong
        if (n > 0 && !thanh) {
            int x0 = 999, y0 = 999, x1 = -1, y1 = -1;
            for (final DinhDangBanDo.Diem d : v.goc.npc) {
                x0 = Math.min(x0, d.x);
                y0 = Math.min(y0, d.y);
                x1 = Math.max(x1, d.x);
                y1 = Math.max(y1, d.y);
            }
            final int rx = Math.max(3, Math.min(k.w / 9, (x1 - x0) / 5)), ry = Math.max(3, Math.min(k.h / 10, (y1 - y0) / 5));
            for (int y = cy - ry; y <= cy + ry; y++) {
                for (int x = cx - rx; x <= cx + rx; x++) {
                    if (k.trong(x, y) && !k.khoa[y * k.w + x]
                            && Math.hypot((x - cx) / (double) rx, (y - cy) / (double) ry) < 1.0 + 0.15 * fbm(x, y, hat + 9, 4)) {
                        k.vl[y * k.w + x] = quangTruong;
                        k.giu[y * k.w + x] = true;
                    }
                }
            }
        }
        // vien
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                final int i = y * k.w + x;
                if (k.giu[i] || k.khoa[i]) {
                    continue;
                }
                final int m = Math.min(Math.min(x, y), Math.min(k.w - 1 - x, k.h - 1 - y));
                if (m < 2 + 2.5 * fbm(x, y, hat + 1, 7)) {
                    k.vl[i] = thanh ? W : F;
                }
            }
        }
        final int soNha = Math.max(1, k.w * k.h / (v.spec.ten.toLowerCase().contains("mua ban") ? 450 : 900));
        datNhaThem(k, b, rd, soNha, 3, 3, k.w - 3, k.h - 3);
        final List<int[]> tat = new ArrayList<int[]>(noi);
        for (int i = 0; i < k.moc.length; i++) {
            if (k.moc[i] && !k.quai[i] && !k.cong[i] && k.diBat[i] && k.vl[i] != quangTruong) {
                // loi vao nha them
                boolean da = false;
                for (final int[] p : tat) {
                    if (p[0] == i % k.w && p[1] == i / k.w) {
                        da = true;
                    }
                }
                if (!da && i / k.w > 0 && k.khoa[i - k.w]) {
                    tat.add(new int[]{i % k.w, i / k.w});
                }
            }
        }
        noiDuong(k, b, hat, tat, 3, duong);
        final boolean[] daDat = new boolean[k.w * k.h];
        datDam(k, rd, hat + 2, W, Math.max(1, k.w * k.h / 2500), 3, 5, daDat);
        if (!thanh) {
            datDam(k, rd, hat + 3, F, Math.max(1, k.w * k.h / 1500), 2, 4, daDat);
        }
        lamMin(k, b, 2);
        if (!thanh) {
            vienCo(k, W, duong, G);
        }
        lamDay(k, b, thanh ? new int[]{G, W, quangTruong} : new int[]{F, W, duong});
        lienThongKeHoach(k, b, hat, duong);
        if (!thanh) {
            datBienBao(k, b, v);
            datVuon(k, b, rd, 5, 4);
            datCayTo(k, b, G, 0.006, hat);
            datCayLe(k, b, G, 0.01, hat, new String[]{"bui", "thung", "hom_thu"});
        } else {
            datCayTo(k, b, G, 0.03, hat);
            datCayLe(k, b, G, 0.03, hat, new String[]{"bui", "hoa_vang"});
        }
        return k;
    }

    /** Hang: vach V, phong hang noi bang hanh lang uon, ho ngam, bai cat, da tang. */
    static KeHoach keHoachHang(final DauVao v, final Random rd) {
        final BoVL b = v.b;
        final long hat = v.spec.hat;
        final int V = b.vl("V"), W = b.vl("W"), C = b.vl("C"), S = b.vl("S");
        final boolean cat = v.spec.ten.toLowerCase().contains("victory") || v.spec.ten.toLowerCase().contains("phan no");
        final int nen = cat ? S : C;
        final KeHoach k = khoiTao(v, V);
        final List<int[]> noi = danhDauMoc(k, v, nen);
        final List<int[]> phong = new ArrayList<int[]>(noi);
        phong.addAll(cumQuai(v, 14));
        for (int i = 0; i < k.w * k.h / 450; i++) {
            phong.add(new int[]{6 + rd.nextInt(Math.max(1, k.w - 12)), 6 + rd.nextInt(Math.max(1, k.h - 12))});
        }
        for (final int[] p : phong) {
            final double rr = 3.5 + 5 * bam(p[0], p[1], hat + 3);
            for (int y = (int) (p[1] - rr * 2); y <= p[1] + rr * 2; y++) {
                for (int x = (int) (p[0] - rr * 2); x <= p[0] + rr * 2; x++) {
                    if (!k.trong(x, y) || k.khoa[y * k.w + x]) {
                        continue;
                    }
                    final int m = Math.min(Math.min(x, y), Math.min(k.w - 1 - x, k.h - 1 - y));
                    if (m < 2 && !k.giu[y * k.w + x]) {
                        continue;
                    }
                    if (Math.hypot(x - p[0], (y - p[1]) * 1.2) < rr * (0.7 + 0.6 * fbm(x, y, hat + 4, 5))) {
                        k.vl[y * k.w + x] = nen;
                    }
                }
            }
        }
        for (int i = 0; i < k.vl.length; i++) {
            if (k.giu[i] && !k.khoa[i] && k.vl[i] == V) {
                final int x = i % k.w, y = i / k.w;
                final int m = Math.min(Math.min(x, y), Math.min(k.w - 1 - x, k.h - 1 - y));
                if (k.moc[i] || m >= 2 || k.cong[i]) {
                    k.vl[i] = nen;
                }
            }
        }
        final double[] cp = new double[k.w * k.h];
        for (final int[] c : khung(phong)) {
            for (int i = 0; i < cp.length; i++) {
                final int x = i % k.w, y = i / k.w;
                cp[i] = (k.vl[i] == nen ? 0.6 : 1.0) + 3 * fbm(x, y, hat + 11, 8) + ((x < 3 || y < 3 || x >= k.w - 3 || y >= k.h - 3) ? 6 : 0);
            }
            final boolean[] dich = new boolean[cp.length];
            dich[phong.get(c[1])[1] * k.w + phong.get(c[1])[0]] = true;
            final List<Integer> p = timDuong(k.w, k.h, phong.get(c[0])[1] * k.w + phong.get(c[0])[0], dich, cp);
            if (p != null) {
                veDuong(k, p, 3, nen, true);
            }
        }
        final boolean[] daDat = new boolean[k.w * k.h];
        for (int i = 0; i < daDat.length; i++) {
            daDat[i] = k.vl[i] == V;
        }
        datDam(k, rd, hat + 2, W, Math.max(1, k.w * k.h / 3000), 2, 4, daDat);
        if (!cat) {
            datDam(k, rd, hat + 6, S, Math.max(1, k.w * k.h / 2500), 2, 4, daDat);
        }
        for (int i = 0; i < k.vl.length; i++) {
            if (k.vl[i] == W && keVl(k, i, V)) {
                k.vl[i] = nen;
            }
        }
        lamMin(k, b, 2);
        lamDay(k, b, new int[]{V, W, S, C});
        lienThongKeHoach(k, b, hat, nen);
        datCayLe(k, b, nen, 0.02, hat, new String[]{"da", "da2", "pha_le", "soi", "pha_le2", "soi"});
        return k;
    }

    /**
     * San (bo t_thanh: dau truong, lanh dia doi): bo cuc hinh hoc - ho bao thang, san da P, bai co G
     * hinh chu nhat theo luoi, san cat A giua, dai phun nuoc, cay.
     */
    static KeHoach keHoachSan(final DauVao v, final Random rd) {
        final BoVL b = v.b;
        final long hat = v.spec.hat;
        final int G = b.vl("G"), P = b.vl("P"), A = b.vl("A"), W = b.vl("W");
        final KeHoach k = khoiTao(v, P);
        final List<int[]> noi = danhDauMoc(k, v, P);
        final int ho = k.w >= 40 ? 2 : 1;
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                final int i = y * k.w + x;
                final int m = Math.min(Math.min(x, y), Math.min(k.w - 1 - x, k.h - 1 - y));
                if (m < ho && !k.giu[i] && !k.khoa[i]) {
                    k.vl[i] = W;
                }
            }
        }
        final int n = k.w >= 70 ? 3 : 2, vong = ho + 3;
        final int x0 = vong + 1, y0 = vong + 1, x1 = k.w - vong - 2, y1 = k.h - vong - 2;
        final double cw = (x1 - x0 + 1) / (double) n, ch = (y1 - y0 + 1) / (double) n;
        for (int gy = 0; gy < n; gy++) {
            for (int gx = 0; gx < n; gx++) {
                final int ax = (int) (x0 + gx * cw) + 2, bx = (int) (x0 + (gx + 1) * cw) - 3;
                final int ay = (int) (y0 + gy * ch) + 2, by = (int) (y0 + (gy + 1) * ch) - 3;
                if (bx - ax < 3 || by - ay < 3) {
                    continue;
                }
                final boolean giua = (n == 3 && gx == 1 && gy == 1) || (n == 2 && (gx + gy + hat) % 2 == 0);
                for (int y = ay; y <= by; y++) {
                    for (int x = ax; x <= bx; x++) {
                        if (k.khoa[y * k.w + x]) {
                            continue;
                        }
                        if (giua && b.co("P2")) {
                            // san dau hoa van + vien do (o khoa, di duoc)
                            final boolean vien = x == ax || x == bx || y == ay || y == by;
                            k.dat(x, y, b.id(vien ? "P3" : "P2"));
                        } else {
                            k.vl[y * k.w + x] = giua ? A : G;
                        }
                    }
                }
                if (!giua && bx - ax >= 8 && by - ay >= 8 && (gx + gy) % 2 == 1) {
                    final int cx = (ax + bx) / 2, cy = (ay + by) / 2;
                    if (hcnTuDo(k, cx - 2, cy - 2, cx + 2, cy + 2)) {
                        for (int y = cy - 1; y <= cy + 1; y++) {
                            for (int x = cx - 1; x <= cx + 1; x++) {
                                k.vl[y * k.w + x] = W;
                            }
                        }
                    }
                }
            }
        }
        noiDuong(k, b, hat, noi, 2, P);
        lamDay(k, b, new int[]{G, A, W});
        lienThongKeHoach(k, b, hat, P);
        // co ngu sac doc bo tren, den o goc trong, tuong o giua neu trong
        if (b.co("co_0")) {
            for (int x = ho + 3, j = 0; x < k.w - ho - 3; x += 4, j++) {
                if (tuDo(k, x, ho) && k.vl[ho * k.w + x] == P) {
                    k.dat(x, ho, b.id("co_" + (j % 4)));
                }
            }
        }
        for (final int[] g : new int[][]{{ho + 1, ho + 1}, {k.w - ho - 2, ho + 1}, {ho + 1, k.h - ho - 2}, {k.w - ho - 2, k.h - ho - 2}}) {
            if (b.co("den") && tuDo(k, g[0], g[1]) && k.vl[g[1] * k.w + g[0]] == P) {
                k.dat(g[0], g[1], b.id("den"));
            }
        }
        datCayTo(k, b, G, 0.04, hat);
        datCayLe(k, b, G, 0.03, hat, new String[]{"bui", "hoa_vang"});
        return k;
    }

    /** Tau: boong go (B) giua bien. */
    static KeHoach keHoachTau(final DauVao v, final Random rd) {
        final BoVL b = v.b;
        final int W = b.vl("W"), B = b.vl("B");
        final KeHoach k = khoiTao(v, W);
        final List<int[]> noi = danhDauMoc(k, v, B);
        final int mx = Math.max(2, k.w / 12), my = Math.max(2, k.h / 6);
        for (int y = my; y < k.h - my; y++) {
            for (int x = mx; x < k.w - mx; x++) {
                final double t = (x - (k.w - mx - k.h / 2.0)) / (k.h / 2.0);
                final double nua = (k.h / 2.0 - my) * (t > 0 ? Math.max(0, 1 - t) : 1);
                if (Math.abs(y - k.h / 2.0) <= nua) {
                    k.vl[y * k.w + x] = B;
                }
            }
        }
        for (int i = 0; i < k.vl.length; i++) {
            if (k.giu[i] && !k.khoa[i]) {
                k.vl[i] = B;
            }
        }
        noiDuong(k, b, v.spec.hat, noi, 2, B);
        lienThongKeHoach(k, b, v.spec.hat, B);
        return k;
    }

    // =====================================================================================
    // Autotile
    // =====================================================================================
    /** Nen (vat lieu duoi) cho vat lieu phu. */
    static int nenCua(final BoVL b, final KeHoach k, final int x, final int y, final int v) {
        final String t = b.ten[v];
        if (b.bo.equals("t")) {
            if (t.equals("W")) {
                return lanCan(k, x, y, b.vl("S")) ? b.vl("S") : b.vl("G");
            }
            return b.vl("G");
        }
        if (b.bo.equals("t_hang")) {
            return b.vl("C");
        }
        if (t.equals("W")) {
            return lanCan(k, x, y, b.vl("G")) && !lanCan(k, x, y, b.vl("P")) ? b.vl("G") : b.vl("P");
        }
        return b.vl("P");
    }

    static boolean lanCan(final KeHoach k, final int x, final int y, final int v) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (k.trong(x + dx, y + dy) && k.vl[(y + dy) * k.w + x + dx] == v) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Vat lieu cua o khi tinh goc (cau tinh nhu nuoc; ngoai ban do = chinh o do). */
    static int vlGoc(final BoVL b, final KeHoach k, final int x, final int y, final int macDinh) {
        if (!k.trong(x, y)) {
            return macDinh;
        }
        final int v = k.vl[y * k.w + x];
        if ((k.tuyet || k.saMac) && macDinh == b.vl(k.tuyet ? "N" : "S") && v != b.vl("W") && v != b.vl("B") && v != b.vl("T")) {
            return macDinh;
        }
        if (v == O) {
            final int d = k.vlDuoi[y * k.w + x];
            // vat nho (cay, den, hoa) dat tren nen: tinh nhu nen do; nha/khoi lon (khong co nen) = -1
            return d >= 0 && d != b.vl("W") ? d : -1;
        }
        if (b.ten[v].equals("B") && b.vl("W") >= 0) {
            return b.vl("W");
        }
        return v;
    }

    /** Bit 4 goc (nw ne sw se) cua o (x, y) voi vat lieu vv: goc la vv khi ca 3 o quanh goc deu la vv. */
    static int bitGoc(final BoVL b, final KeHoach k, final int x, final int y, final int vv, final boolean[] la) {
        final int[][] goc = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        int bit = 0;
        for (int c = 0; c < 4; c++) {
            final int gx = goc[c][0], gy = goc[c][1];
            final boolean a;
            if (la != null) {
                a = laO(k, la, x + gx, y) && laO(k, la, x, y + gy) && laO(k, la, x + gx, y + gy);
            } else {
                a = vlGoc(b, k, x + gx, y, vv) == vv && vlGoc(b, k, x, y + gy, vv) == vv && vlGoc(b, k, x + gx, y + gy, vv) == vv;
            }
            if (a) {
                bit |= 8 >> c;
            }
        }
        return bit;
    }

    static boolean laO(final KeHoach k, final boolean[] la, final int x, final int y) {
        return !k.trong(x, y) || la[y * k.w + x];
    }

    static String ma(final int bit) {
        return "" + ((bit >> 3) & 1) + ((bit >> 2) & 1) + ((bit >> 1) & 1) + (bit & 1);
    }

    static int[] veTile(final KeHoach k, final BoVL b, final long hat) {
        final int n = k.w * k.h;
        final int[] tile = new int[n];
        // vung co sang (L) theo nhieu tan so thap tren nen co - mau tu nhien
        final boolean[] sang = new boolean[n];
        final boolean coL = b.co("L");
        for (int i = 0; i < n; i++) {
            final int x = i % k.w, y = i / k.w;
            sang[i] = coL && k.vl[i] == 0 && !k.khoa[i] && fbm(x, y, hat + 31, 8) > 0.63;
        }
        // do sau nuoc: khoang cach toi bo (o khong phai nuoc/cau)
        final int W = b.vl("W"), B = b.vl("B");
        final boolean[] bo = new boolean[n];
        for (int i = 0; i < n; i++) {
            bo[i] = W < 0 || (k.vl[i] != W && k.vl[i] != B) || k.khoa[i];
        }
        final int[] sau = khoangCach(k.w, k.h, bo);
        for (int y = 0; y < k.h; y++) {
            for (int x = 0; x < k.w; x++) {
                final int i = y * k.w + x;
                if (k.khoa[i]) {
                    tile[i] = k.khoaTile[i];
                    continue;
                }
                final int v = k.vl[i];
                final String ten = b.ten[v];
                if (v == 0) {
                    if (sang[i]) {
                        final int bit = bitGoc(b, k, x, y, v, sang);
                        tile[i] = bit == 15 ? b.id(bam(x, y, hat + 5) < 0.12 ? "L1" : (bam(x, y, hat + 6) < 0.08 ? "L2" : "L"))
                                : (bit == 0 ? tileCo(b, x, y, hat) : b.id("L/G_" + ma(bit)));
                    } else {
                        tile[i] = tileCo(b, x, y, hat);
                    }
                    continue;
                }
                if (ten.equals("F")) {
                    tile[i] = rung(k, b, x, y, v, hat);
                    continue;
                }
                if (ten.equals("T")) {
                    tile[i] = b.id(((x * 7 + y * 3) % 5 == 0) ? "T1" : "T");
                    continue;
                }
                if (ten.equals("B")) {
                    final boolean ngang = (k.trong(x - 1, y) && k.vl[i - 1] == v) || (k.trong(x + 1, y) && k.vl[i + 1] == v);
                    final boolean doc = (k.trong(x, y - 1) && k.vl[i - k.w] == v) || (k.trong(x, y + 1) && k.vl[i + k.w] == v);
                    tile[i] = b.id(doc && !ngang ? "cau_doc" : "cau_ngang");
                    continue;
                }
                if (ten.equals("R") || ten.equals("V")) {
                    tile[i] = vach(k, b, x, y, v, ten);
                    continue;
                }
                final int bit = bitGoc(b, k, x, y, v, null);
                if (bit == 15) {
                    if (ten.equals("W")) {
                        tile[i] = nuocTheoSau(b, x, y, sau[i], hat);
                    } else {
                        tile[i] = b.id(ten);
                    }
                    continue;
                }
                final int duoi = nenCua(b, k, x, y, v);
                if (bit == 0) {
                    tile[i] = duoi == 0 ? tileCo(b, x, y, hat) : b.id(b.ten[duoi]);
                    continue;
                }
                final String tt = ten + "/" + b.ten[duoi] + "_" + ma(bit);
                final String tt0 = ten + "/" + b.ten[0] + "_" + ma(bit);
                tile[i] = b.co(tt) ? b.id(tt) : (b.co(tt0) ? b.id(tt0) : b.id(ten));
            }
        }
        return tile;
    }

    /** Nuoc thuan: gan bo = nuoc nong (song lan, la sen thua), xa bo sam dan (2 bac ditter). */
    static int nuocTheoSau(final BoVL b, final int x, final int y, final int sau, final long hat) {
        if (sau >= 6 && b.co("WD")) {
            return b.id("WD");
        }
        if ((sau == 4 || sau == 5) && b.co("WM")) {
            return b.id("WM");
        }
        final double r = bam(x, y, hat + 515);
        if (b.co("sen") && r < 0.07) {
            return b.id("sen");
        }
        if (b.co("W1") && r > 0.8) {
            return b.id("W1");
        }
        return b.id("W");
    }

    /** O nen thuong: co (G + co la, hoa thanh dam nho theo nhieu) / nen hang / san. */
    static int tileCo(final BoVL b, final int x, final int y, final long hat) {
        if (b.bo.equals("t_hang")) {
            final double f = fbm(x, y, hat + 19, 6);
            if (f > 0.7 && b.co("C3")) {
                return b.id("C3");
            }
            if (f > 0.6 && b.co("C2")) {
                return b.id("C2");
            }
            return b.id(bam(x, y, hat + 9) < 0.04 ? "C1" : "C");
        }
        if (b.bo.equals("t_thanh")) {
            return b.id("P");
        }
        if (fbm(x, y, hat + 41, 4) > 0.72) {
            final String[] hoa = {"hoa_vang", "hoa_trang", "hoa_cam", "hoa_vang"};
            final int loai = (int) (nhieu(x / 23.0, y / 23.0, hat + 42) * hoa.length) % hoa.length;
            return b.id(bam(x, y, hat + 43) < 0.7 ? hoa[loai] : "G");
        }
        final double r = bam(x, y, hat + 808);
        if (r < 0.10) {
            return b.id("G" + (1 + (int) (bam(x, y, hat + 809) * 3)));
        }
        return b.id("G");
    }

    /** Rung: khoi cay 2x2 thang luoi chan; o le (khoi chua du) = bui. */
    static int rung(final KeHoach k, final BoVL b, final int x, final int y, final int v, final long hat) {
        // khoi 2x2 xep le hang (hang khoi le lech 1 o) -> rung day tu nhien, khong thanh luoi vuon
        final int y0 = y & ~1;
        final int lech = (y0 / 2) % 2;
        final int x0 = ((x - lech) & ~1) + lech;
        boolean du = true;
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                final int xx = x0 + dx, yy = y0 + dy;
                if (!k.trong(xx, yy) || k.vl[yy * k.w + xx] != v || k.khoa[yy * k.w + xx]) {
                    du = false;
                }
            }
        }
        if (!du) {
            return b.id(k.tuyet ? "da_tuyet" : (k.saMac ? "da_cat" : (bam(x, y, hat + 71) < 0.7 ? "bui" : "cay_mong")));
        }
        final double r = bam(x0, y0, hat + 72);
        final String loai = k.tuyet ? "cayT" : (k.saMac ? "dua" : (r < 0.55 ? "cayA" : (r < 0.85 ? "cayB" : (b.co("cayC_0") ? "cayC" : "cayA"))));
        return b.id(loai + "_" + ((y - y0) * 2 + (x - x0)));
    }

    /**
     * Vach da / vach hang: 2 hang duoi cung cua vung la MAT VACH (Ninja TilesetRelief), phan tren la mat
     * bang (co sang / da toi) vien toi theo goc. Moi o deu chan.
     */
    static int vach(final KeHoach k, final BoVL b, final int x, final int y, final int v, final String p) {
        int dS = 0;
        while (k.trong(x, y + dS) && k.vl[(y + dS) * k.w + x] == v && dS < 3) {
            dS++;
        }
        final boolean trai = k.trong(x - 1, y) && k.vl[y * k.w + x - 1] == v;
        final boolean phai = k.trong(x + 1, y) && k.vl[y * k.w + x + 1] == v;
        final String cot = !trai ? "l" : (!phai ? "r" : "m");
        if (dS == 1 || (dS == 2 && !(k.trong(x, y - 1) && k.vl[(y - 1) * k.w + x] == v))) {
            return b.id(p + "_mat_" + cot + "2");
        }
        if (dS == 2) {
            return b.id(p + "_mat_" + cot + "0");
        }
        final int bit = bitGoc(b, k, x, y, v, null);
        return bit == 15 ? b.id(p + "T") : (bit == 0 ? b.id(p + "_mat_" + cot + "0") : b.id(p + "T/" + b.ten[0] + "_" + ma(bit)));
    }

    // =====================================================================================
    // Trong nha
    // =====================================================================================
    static int[] veTrongNha(final DauVao v, final KeHoach k) {
        final BoVL b = v.b;
        final long hat = v.spec.hat;
        final Random rd = new Random(hat);
        final int w = k.w, h = k.h;
        final int[] tile = new int[w * h];
        final String ten = v.spec.ten.toLowerCase();
        final boolean trungTam = ten.contains("trung tam");
        final boolean cuaHang = ten.contains("mart") || ten.contains("cua hang");
        final String san = trungTam ? "san_sach" : (cuaHang ? "san_tiem" : "san_go");
        final String hauTo = "_go";
        final String tuong = "tuong_go";
        int yCong = h - 1;
        final Set<Integer> cotCua = new HashSet<Integer>();
        for (final DinhDangBanDo.Cong c : v.goc.cong) {
            cotCua.add(c.x);
            yCong = Math.min(yCong, c.y);
        }
        final int yDuoi = Math.max(3, Math.min(h - 1, yCong));
        final int tuongTren = h >= 10 ? 2 : 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                String t;
                if (x == 0 || x == w - 1) {
                    t = "tuong_toi";
                } else if (y < tuongTren) {
                    t = tuong;
                } else if (y >= yDuoi) {
                    t = cotCua.contains(x) ? san : (y == yDuoi ? tuong : "tuong_toi");
                } else {
                    t = san;
                }
                tile[y * w + x] = b.id(t);
            }
        }
        for (final DinhDangBanDo.Cong c : v.goc.cong) {
            for (int y = tuongTren; y < h; y++) {
                if (y >= Math.min(c.y, yDuoi) || !b.diDuoc(tile[y * w + c.x])) {
                    tile[y * w + c.x] = b.id(san);
                }
            }
        }
        for (int i = 0; i < k.moc.length; i++) {
            if (k.moc[i] && !k.cong[i] && !b.diDuoc(tile[i])) {
                tile[i] = b.id(san);
            }
        }
        // do dac: hang sat tuong tren
        final String[] boDo = trungTam ? new String[]{"e6_7", "e8_7", "e0_1"} : (cuaHang ? new String[]{"e8_7", "e9_7", "e6_7", "e3_7"}
                : new String[]{"e3_7", "e0_1", "e8_7", "e6_7"});
        int j = rd.nextInt(boDo.length);
        for (int x = 2; x < w - 2; x += 2 + rd.nextInt(2)) {
            final int y = tuongTren, i = y * w + x;
            if (k.moc[i] || cotCua.contains(x) || (y + 1 < h && k.moc[i + w])) {
                continue;
            }
            tile[i] = b.id(boDo[j++ % boDo.length] + hauTo);
        }
        // quay o giua (trung tam/cua hang): mot hang ban ngang, chua loi o giua
        if ((trungTam || cuaHang) && h >= 9 && w >= 9) {
            final int y = tuongTren + 2;
            for (int x = 3; x < w - 3; x++) {
                final int i = y * w + x;
                if (Math.abs(x - w / 2) <= 1 || k.moc[i] || k.moc[i + w] || k.moc[i - w] || cotCua.contains(x)) {
                    continue;
                }
                tile[i] = b.id("e13_1" + hauTo);
            }
        }
        return tile;
    }


    // =====================================================================================
    // Kiem + sua tren tile that
    // =====================================================================================
    static final class Kiem {
        int khongToi, quaiChan, mocChan;
        List<String> loi = new ArrayList<String>();
    }

    static boolean[] diDuocThat(final KeHoach k, final BoVL b, final int[] tile) {
        final boolean[] di = new boolean[tile.length];
        for (int i = 0; i < di.length; i++) {
            di[i] = k.cong[i] || b.diDuoc(tile[i]);
        }
        return di;
    }

    static int batDauThat(final KeHoach k, final boolean[] di) {
        if (di[k.batDau]) {
            return k.batDau;
        }
        for (int i = 0; i < di.length; i++) {
            if (k.moc[i] && di[i]) {
                return i;
            }
        }
        return k.batDau;
    }

    static Kiem kiem(final KeHoach k, final DauVao v, final int[] tile) {
        final Kiem kq = new Kiem();
        final boolean[] di = diDuocThat(k, v.b, tile);
        final boolean[] toi = loang(k.w, k.h, di, batDauThat(k, di));
        for (int i = 0; i < tile.length; i++) {
            if (!k.moc[i]) {
                continue;
            }
            final boolean laQuai = k.diBatQuai(i);
            if (!di[i]) {
                if (laQuai) {
                    kq.quaiChan++;
                } else {
                    kq.mocChan++;
                }
                kq.loi.add("o chan " + (i % k.w) + "," + (i / k.w) + (laQuai ? " (quai)" : ""));
            } else if (!toi[i]) {
                kq.khongToi++;
                kq.loi.add("khong toi " + (i % k.w) + "," + (i / k.w) + (laQuai ? " (quai)" : ""));
            }
        }
        return kq;
    }

    /** O moc bi chan / khong toi: mo duong tren tile that (go vat can nho, doi vat lieu thanh nen di duoc). */
    static boolean sua(final KeHoach k, final DauVao v, final int[] tile, final int vlDi) {
        final BoVL b = v.b;
        final boolean[] di = diDuocThat(k, b, tile);
        final boolean[] toi = loang(k.w, k.h, di, batDauThat(k, di));
        final double[] cp = new double[tile.length];
        for (int i = 0; i < cp.length; i++) {
            cp[i] = di[i] ? 1 : (k.khoa[i] ? 40 : 10);
        }
        boolean doi = false;
        for (int i = 0; i < tile.length; i++) {
            if (!k.moc[i] || (di[i] && toi[i])) {
                continue;
            }
            List<Integer> p = null;
            boolean coToi = false;
            for (final boolean t : toi) {
                if (t) {
                    coToi = true;
                    break;
                }
            }
            if (coToi) {
                p = timDuong(k.w, k.h, i, toi, cp);
            }
            if (p == null) {
                p = new ArrayList<Integer>();
                p.add(i);
            }
            for (final int j : p) {
                if (di[j] && j != i) {
                    continue;
                }
                if (k.cong[j]) {
                    continue;
                }
                if (k.khoa[j]) {
                    k.khoa[j] = false;
                    k.vl[j] = vlDi;
                }
                if (!b.di[k.vl[j]]) {
                    k.vl[j] = vlDi;
                }
                // lam day o mo de autotile ve duoc o thuan (2x2)
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dx = 0; dx <= 1; dx++) {
                        final int xx = j % k.w + dx, yy = j / k.w + dy;
                        if (k.trong(xx, yy) && !k.khoa[yy * k.w + xx] && !b.di[k.vl[yy * k.w + xx]]) {
                            k.vl[yy * k.w + xx] = vlDi;
                        }
                    }
                }
                k.diBat[j] = true;
                doi = true;
                toi[j] = true;
                di[j] = true;
            }
        }
        return doi;
    }

    // =====================================================================================
    // Chay
    // =====================================================================================
    public static void main(final String[] a) throws Exception {
        final Set<Integer> chi = new HashSet<Integer>();
        for (final String s : a) {
            chi.add(Integer.parseInt(s));
        }
        napBoTile();
        final Map<Integer, Spec> spec = docSpec();
        final Map<Integer, List<Moc>> moc = docMoc();
        final Map<Integer, List<int[]>> den = docDen();
        new File(RA_CMAP).mkdirs();
        new File(RA_ANH).mkdirs();
        final StringBuilder bao = new StringBuilder("# Kiem ban do moi (VeMapMoi) - id loai kich_thuoc cong npc moc quai | ket qua\n");
        final Map<Integer, BufferedImage> nho = new LinkedHashMap<Integer, BufferedImage>();
        int loi = 0;
        for (final int id : GiaiMaMap.danhSachMap(VAO_CMAP)) {
            if (!chi.isEmpty() && !chi.contains(id)) {
                continue;
            }
            final Spec s = spec.get(id);
            if (s == null) {
                throw new IllegalStateException("thieu thiet ke cho map " + id + " trong docs/pokemon/thiet_ke_map.csv");
            }
            final DauVao v = new DauVao();
            v.spec = s;
            v.b = BO.get(DinhDangBanDo.boTile(id));
            v.goc = DinhDangBanDo.doc(Files.readAllBytes(new File(VAO_CMAP, "" + id).toPath()));
            v.quaiGoc = GiaiMaMap.docQuai(id);
            if (v.quaiGoc != null) {
                v.quai = DinhDangBanDo.LuoiQuai.doc(v.quaiGoc.ghi());
            }
            if (moc.containsKey(id)) {
                v.moc = moc.get(id);
            }
            if (den.containsKey(id)) {
                v.den = den.get(id);
            }
            final String kq = veMot(v, nho);
            bao.append(kq).append('\n');
            if (kq.contains("LOI")) {
                loi++;
            }
            System.out.println(kq);
        }
        if (chi.isEmpty()) {
            Files.write(new File(RA_ANH, "kiem_tra.txt").toPath(), bao.toString().getBytes(StandardCharsets.UTF_8));
            veTongHop(nho, spec);
            final byte[] m0 = Files.readAllBytes(new File(RA_CMAP, "0").toPath());
            new File(RA_UNITY).mkdirs();
            Files.write(new File(RA_UNITY, "0.bytes").toPath(), m0);
            new File(RA_J2ME).mkdirs();
            Files.write(new File(RA_J2ME, "0").toPath(), m0);
        }
        System.out.println(loi == 0 ? "XONG: moi ban do dat kiem tra" : ("CO " + loi + " BAN DO LOI"));
        System.exit(loi == 0 ? 0 : 1);
    }

    static String veMot(final DauVao v, final Map<Integer, BufferedImage> nho) throws Exception {
        final int id = v.spec.id;
        final Random rd = new Random(v.spec.hat);
        final String loai = v.spec.loai;
        final BoVL b = v.b;
        KeHoach k;
        int[] tile;
        int vlDi = 0;
        if (loai.equals("trong_nha")) {
            k = khoiTao(v, 0);
            danhDauMoc(k, v, 0);
            tile = veTrongNha(v, k);
        } else {
            if (b.bo.equals("t_hang")) {
                k = keHoachHang(v, rd);
                vlDi = b.vl(v.spec.ten.toLowerCase().contains("victory") || v.spec.ten.toLowerCase().contains("phan no") ? "S" : "C");
            } else if (b.bo.equals("t_thanh") && !(loai.equals("thanh") && id == 201)) {
                k = keHoachSan(v, rd);
                vlDi = b.vl("P");
            } else if (loai.equals("thi_tran") && b.bo.equals("t")) {
                k = keHoachLang(v, rd);
                vlDi = b.vl("D");
            } else if (loai.equals("thi_tran") || loai.equals("thanh")) {
                k = keHoachThiTran(v, rd);
                vlDi = b.bo.equals("t_thanh") ? b.vl("A") : b.vl("D");
            } else if (loai.equals("tau")) {
                k = keHoachTau(v, rd);
                vlDi = b.vl("B");
            } else {
                k = keHoachNgoaiTroi(v, rd);
                vlDi = k.tuyet ? b.vl("N") : (k.saMac ? b.vl("S") : b.vl("G"));
            }
            tile = veTile(k, b, v.spec.hat);
            for (int lan = 0; lan < 12; lan++) {
                final Kiem kt = kiem(k, v, tile);
                if (kt.khongToi + kt.mocChan + kt.quaiChan == 0) {
                    break;
                }
                if (!sua(k, v, tile, vlDi)) {
                    break;
                }
                tile = veTile(k, b, v.spec.hat);
            }
        }
        final Kiem kt = kiem(k, v, tile);
        final DinhDangBanDo m = new DinhDangBanDo(v.goc.w, v.goc.h);
        System.arraycopy(tile, 0, m.tile, 0, m.tile.length);
        m.ketCay = 255;
        m.offline = v.goc.offline;
        m.cong.addAll(v.goc.cong);
        m.npc.addAll(v.goc.npc);
        m.coNpc = v.goc.coNpc;
        m.duoi = v.goc.duoi;
        final byte[] ra = m.ghi();
        final DinhDangBanDo lai = DinhDangBanDo.doc(ra);
        if (!Arrays.equals(lai.tile, m.tile) || lai.cong.size() != m.cong.size() || lai.npc.size() != m.npc.size()) {
            throw new IllegalStateException("ma hoa sai map " + id);
        }
        Files.write(new File(RA_CMAP, "" + id).toPath(), ra);
        final File fq = new File(RA_QUAI, "map" + id);
        boolean doiQuai = false;
        if (v.quai != null && !Arrays.equals(v.quai.ghi(), v.quaiGoc.ghi())) {
            new File(RA_QUAI).mkdirs();
            Files.write(fq.toPath(), v.quai.ghi());
            doiQuai = true;
        } else if (fq.isFile()) {
            fq.delete();
        }
        final GiaiMaMap.BangDen den = new GiaiMaMap.BangDen();
        for (final int[] d : v.den) {
            den.them(id, d[0], d[1]);
        }
        // anh sach (nhu trong game, khong danh dau) + anh kiem (cong/NPC/quai/diem den) rieng
        final BufferedImage anh = GiaiMaMap.ve(id, m, v.quai, den, false, THU_MUC_TILE);
        ImageIO.write(anh, "png", new File(RA_ANH, id + ".png"));
        new File(RA_ANH, "kiem").mkdirs();
        final BufferedImage kiemAnh = GiaiMaMap.ve(id, m, v.quai, den, true, THU_MUC_TILE);
        final Graphics2D g = kiemAnh.createGraphics();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString(v.spec.ten + " [" + loai + "]", 6, 34);
        g.dispose();
        ImageIO.write(kiemAnh, "png", new File(RA_ANH + "/kiem", id + ".png"));
        nho.put(id, thuNho(anh, 240));
        int nMoc = 0, nQuai = 0;
        for (int i = 0; i < k.moc.length; i++) {
            if (k.moc[i]) {
                if (k.diBatQuai(i)) {
                    nQuai++;
                } else {
                    nMoc++;
                }
            }
        }
        final boolean ok = kt.khongToi + kt.mocChan + kt.quaiChan == 0;
        return id + " " + loai + " " + m.w + "x" + m.h + " cong=" + m.cong.size() + " npc=" + m.npc.size() + " moc=" + nMoc + " quai=" + nQuai
                + (doiQuai ? " (luoi quai moi)" : "") + " | " + (ok ? "OK" : ("LOI khongToi=" + kt.khongToi + " mocChan=" + kt.mocChan
                + " quaiChan=" + kt.quaiChan + " " + kt.loi.subList(0, Math.min(5, kt.loi.size()))));
    }

    static BufferedImage thuNho(final BufferedImage a, final int c) {
        final double s = Math.min(c / (double) a.getWidth(), c / (double) a.getHeight());
        final BufferedImage n = new BufferedImage(Math.max(1, (int) (a.getWidth() * s)), Math.max(1, (int) (a.getHeight() * s)),
                BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = n.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(a, 0, 0, n.getWidth(), n.getHeight(), null);
        g.dispose();
        return n;
    }

    static void veTongHop(final Map<Integer, BufferedImage> nho, final Map<Integer, Spec> spec) throws Exception {
        final int cot = 10, o = 250;
        final int hang = (nho.size() + cot - 1) / cot;
        final BufferedImage a = new BufferedImage(cot * o, hang * (o + 16), BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = a.createGraphics();
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, a.getWidth(), a.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int i = 0;
        for (final Map.Entry<Integer, BufferedImage> e : nho.entrySet()) {
            final int x = (i % cot) * o, y = (i / cot) * (o + 16);
            g.drawImage(e.getValue(), x + 5, y + 16, null);
            g.setColor(Color.WHITE);
            g.drawString(e.getKey() + " " + spec.get(e.getKey()).ten, x + 5, y + 12);
            i++;
        }
        g.dispose();
        ImageIO.write(a, "png", new File(RA_ANH, "tong_hop.png"));
    }

    // ---------- doc tep ----------
    static List<String> dong(final String f) throws Exception {
        final List<String> ra = new ArrayList<String>();
        for (final String l : Files.readAllLines(new File(f).toPath(), StandardCharsets.UTF_8)) {
            final String t = l.trim();
            if (!t.isEmpty() && !t.startsWith("#")) {
                ra.add(t);
            }
        }
        return ra;
    }

    static Map<Integer, Spec> docSpec() throws Exception {
        final Map<Integer, Spec> m = new LinkedHashMap<Integer, Spec>();
        for (final String l : dong("docs/pokemon/thiet_ke_map.csv")) {
            if (l.startsWith("id;")) {
                continue;
            }
            final String[] p = l.split(";", -1);
            final Spec s = new Spec();
            s.id = Integer.parseInt(p[0].trim());
            s.loai = p[1].trim();
            s.ten = p[2].trim();
            s.hat = Long.parseLong(p[3].trim());
            s.moc = p.length > 4 ? p[4].trim() : "";
            m.put(s.id, s);
        }
        return m;
    }

    static Map<Integer, List<Moc>> docMoc() throws Exception {
        final Map<Integer, List<Moc>> m = new HashMap<Integer, List<Moc>>();
        for (final String l : dong("docs/pokemon/diem_co_dinh.csv")) {
            final String[] p = l.split(";", -1);
            final Moc c = new Moc();
            final int id = Integer.parseInt(p[0].trim());
            c.x0 = Integer.parseInt(p[1].trim());
            c.y0 = Integer.parseInt(p[2].trim());
            c.x1 = Integer.parseInt(p[3].trim());
            c.y1 = Integer.parseInt(p[4].trim());
            c.noi = p[5].trim();
            c.ghiChu = p.length > 6 ? p[6].trim() : "";
            List<Moc> l2 = m.get(id);
            if (l2 == null) {
                l2 = new ArrayList<Moc>();
                m.put(id, l2);
            }
            l2.add(c);
        }
        return m;
    }

    /** docs/pokemon/map_cu/bang_den.json: {"id": [[x,y],...], ...} (GiaiMaMap sinh). */
    static Map<Integer, List<int[]>> docDen() throws Exception {
        final Map<Integer, List<int[]>> m = new HashMap<Integer, List<int[]>>();
        final File f = new File("docs/pokemon/map_cu/bang_den.json");
        if (!f.isFile()) {
            System.out.println("CANH BAO: thieu " + f + " - chay GiaiMaMap truoc (can lop server)");
            return m;
        }
        final java.util.regex.Pattern ten = java.util.regex.Pattern.compile("^\\s*\"(\\d+)\"\\s*:");
        final java.util.regex.Pattern cap = java.util.regex.Pattern.compile("\\[(\\d+),(\\d+)\\]");
        for (final String l : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
            final java.util.regex.Matcher mt = ten.matcher(l);
            if (!mt.find()) {
                continue;
            }
            final List<int[]> ds = new ArrayList<int[]>();
            final java.util.regex.Matcher p = cap.matcher(l);
            while (p.find()) {
                ds.add(new int[]{Integer.parseInt(p.group(1)), Integer.parseInt(p.group(2))});
            }
            m.put(Integer.parseInt(mt.group(1)), ds);
        }
        return m;
    }
}

