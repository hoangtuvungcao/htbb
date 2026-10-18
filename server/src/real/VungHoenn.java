package real;

import java.io.File;
import java.nio.file.Files;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.htbb.pokemon.BanDoHoenn;
import com.htbb.pokemon.CapDoLauDai;
import com.htbb.pokemon.CheDoPokemon;
import com.htbb.pokemon.LauDai;

/**
 * Noi vung Hoenn (com.htbb.pokemon.BanDoHoenn) vao server. Ba diem goi:
 *  - RealController.initMapHTBB (cuoi): {@link #taoMap} - dang ky Map cho 29 ban do;
 *  - RealController.add_more_idMapSend2Client (cuoi): {@link #dangKyBang} - noi them hang vao mapID /
 *    idArrMap / namMap / nameMap / locationMap (goi 105), mapChange, ALL_LOCALTION_MAP, danh sach gui
 *    bo cuc (goi 12) va lua chon "Littleroot (Hoenn)" o cong bac map 9;
 *  - MapXuLyLenh.doChangeMap: {@link #chanVao} - chan qua cong vao Hoenn khi chua toi nam 2.
 * Chi bat khi che do Pokemon bat va du tep cMap_pokemon/&lt;id&gt; (ban do Hoenn khong co ban goc).
 */
public final class VungHoenn {

    private static volatile boolean daTao;

    private VungHoenn() {
    }

    /** Che do Pokemon bat va moi ban do Hoenn co bo cuc. */
    public static boolean coVung() {
        if (!CheDoPokemon.bat()) {
            return false;
        }
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            if (!new File(com.htbb.pokemon.BanDoPokemon.THU_MUC_CMAP + b.id).isFile()) {
                return false;
            }
        }
        return true;
    }

    public static void taoMap(final Hashtable<Integer, Map> mapList) {
        if (!coVung()) {
            System.out.println("[HOENN] khong bat (che do Pokemon tat hoac thieu cMap_pokemon) - bo qua vung Hoenn");
            return;
        }
        for (final BanDoHoenn.BanDo b : BanDoHoenn.ds()) {
            mapList.put(Integer.valueOf(b.id), new Map(b.id, -1, 1, b.id, 0));
        }
        daTao = true;
        System.out.println("[HOENN] da tao " + BanDoHoenn.ds().size() + " ban do vung Hoenn");
    }

    /** Ten hien (goi 105 / goi 12) cua ban do Hoenn. */
    public static String ten(final int id) {
        final BanDoHoenn.BanDo b = BanDoHoenn.lay(id);
        return b == null ? "" : b.ten;
    }

    private static String[] namTen(final String t) {
        return new String[]{t, "", "", "", ""};
    }

    private static short[] namDiem(final int map, final int[] xy) {
        final short[] r = new short[MessageCreator.nclone * 3];
        // chi khe 0 co ten -> menu cong chi hien mot lua chon; cac khe con lai lap lai cung diem den
        for (int k = 0; k < MessageCreator.nclone; k++) {
            r[k * 3] = (short) map;
            r[k * 3 + 1] = (short) xy[0];
            r[k * 3 + 2] = (short) xy[1];
        }
        return r;
    }

    private static String[][][] noi(final String[][][] a, final int n) {
        final String[][][] r = new String[Math.max(a.length, n)][][];
        System.arraycopy(a, 0, r, 0, a.length);
        for (int i = a.length; i < r.length; i++) {
            r[i] = new String[0][];
        }
        return r;
    }

    private static short[][][] noi(final short[][][] a, final int n) {
        final short[][][] r = new short[Math.max(a.length, n)][][];
        System.arraycopy(a, 0, r, 0, a.length);
        for (int i = a.length; i < r.length; i++) {
            r[i] = new short[0][];
        }
        return r;
    }

    public static void dangKyBang() {
        if (!daTao) {
            return;
        }
        final List<BanDoHoenn.BanDo> ds = BanDoHoenn.ds();
        final int hang0 = Map.mapID.length;
        final int nhom0 = Math.max(Map.locationMap.length, Map.nameMap.length);
        final int soHang = hang0 + ds.size();
        final short[][] mapID = new short[soHang][];
        System.arraycopy(Map.mapID, 0, mapID, 0, hang0);
        final short[] idArr = new short[soHang];
        System.arraycopy(Map.idArrMap, 0, idArr, 0, Math.min(hang0, Map.idArrMap.length));
        final String[][] namMap = new String[Math.max(soHang, Map.namMap.length)][];
        System.arraycopy(Map.namMap, 0, namMap, 0, Map.namMap.length);
        for (int i = Map.namMap.length; i < hang0; i++) {
            namMap[i] = namTen("");
        }
        final String[][][] nameMap = noi(Map.nameMap, nhom0 + ds.size());
        final short[][][] locationMap = noi(Map.locationMap, nhom0 + ds.size());
        for (int i = 0; i < ds.size(); i++) {
            final BanDoHoenn.BanDo b = ds.get(i);
            final int hang = hang0 + i, nhom = nhom0 + i;
            mapID[hang] = new short[]{(short) b.id, (short) b.id, (short) b.id, (short) b.id, (short) b.id};
            idArr[hang] = (short) nhom;
            namMap[hang] = namTen(b.ten);
            final String[][] ten = new String[b.cong.size()][];
            final short[][] den = new short[b.cong.size()][];
            final short[] cho = new short[b.cong.size() + 1];
            for (int c = 0; c < b.cong.size(); c++) {
                final int toi = b.cong.get(c).den;
                final int[] xy = BanDoHoenn.diemDen(b.id, toi);
                ten[c] = namTen(toi == BanDoHoenn.MAP_CONG_VAO && !BanDoHoenn.laHoenn(toi) ? "Về Kanto (Cinnabar)" : ten(toi));
                den[c] = namDiem(toi, xy);
                cho[c] = (short) toi;
                themViTri(b.id, b.oCong(c), toi, xy);
            }
            cho[b.cong.size()] = (short) b.id;
            nameMap[nhom] = ten;
            locationMap[nhom] = den;
            Map.mapChange.put((short) b.id, cho);
            try {
                Map.idMapSend2Client.put((short) b.id,
                        Files.readAllBytes(new File(com.htbb.pokemon.BanDoPokemon.duong("cMap/" + b.id)).toPath()));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        // loi vao: cong bac map 9 (nhom 9, toM 1) them lua chon thu 3 -> Littleroot
        final int g = idArrCua(BanDoHoenn.MAP_CONG_VAO);
        final int[] vao = BanDoHoenn.diemVaoTuKanto();
        if (g >= 0 && g < locationMap.length && BanDoHoenn.TOM_CONG_VAO < locationMap[g].length) {
            final short[] l = locationMap[g][BanDoHoenn.TOM_CONG_VAO].clone();
            final int k = BanDoHoenn.KHE_CONG_VAO;
            l[k * 3] = (short) BanDoHoenn.cuaNgo().id;
            l[k * 3 + 1] = (short) vao[0];
            l[k * 3 + 2] = (short) vao[1];
            locationMap[g] = locationMap[g].clone();
            locationMap[g][BanDoHoenn.TOM_CONG_VAO] = l;
            final String[] t = nameMap[g][BanDoHoenn.TOM_CONG_VAO].clone();
            t[k] = BanDoHoenn.cuaNgo().ten + " (Hoenn)";
            nameMap[g] = nameMap[g].clone();
            nameMap[g][BanDoHoenn.TOM_CONG_VAO] = t;
        } else {
            System.out.println("[HOENN] KHONG tim thay cong bac map 9 trong locationMap - khong co loi vao Hoenn");
        }
        final short[] cu = Map.mapChange.get((short) BanDoHoenn.MAP_CONG_VAO);
        if (cu != null) {
            final short[] moi = java.util.Arrays.copyOf(cu, cu.length + 1);
            moi[cu.length] = (short) BanDoHoenn.cuaNgo().id;
            Map.mapChange.put((short) BanDoHoenn.MAP_CONG_VAO, moi);
        }
        final Vector<LocationMap> v9 = Map.ALL_LOCALTION_MAP.get(Integer.valueOf(BanDoHoenn.MAP_CONG_VAO));
        if (v9 != null) {
            // cung vung chu nhat voi cong Cao nguyen Indigo (MapNapTaiNguyen.loadAllLocaltionMap: {29,0,10,10})
            v9.add(new LocationMap(29, 0, 10, 10, BanDoHoenn.cuaNgo().id, vao[0], vao[1]));
        }
        Map.mapID = mapID;
        Map.idArrMap = idArr;
        Map.namMap = namMap;
        Map.nameMap = nameMap;
        Map.locationMap = locationMap;
        System.out.println("[HOENN] dang ky bang: mapID " + hang0 + "->" + soHang + ", locationMap " + nhom0 + "->" + locationMap.length);
    }

    /** Vung chu nhat (o) quanh cum cong: nguoi choi phai dung trong vung nay moi qua cong (chong hack). */
    private static void themViTri(final int id, final List<int[]> o, final int toi, final int[] xy) {
        int x0 = 999, y0 = 999, x1 = -1, y1 = -1;
        for (final int[] c : o) {
            x0 = Math.min(x0, c[0]);
            y0 = Math.min(y0, c[1]);
            x1 = Math.max(x1, c[0]);
            y1 = Math.max(y1, c[1]);
        }
        x0 = Math.max(0, x0 - 4);
        y0 = Math.max(0, y0 - 4);
        Vector<LocationMap> v = Map.ALL_LOCALTION_MAP.get(Integer.valueOf(id));
        if (v == null) {
            v = new Vector<LocationMap>();
            Map.ALL_LOCALTION_MAP.put(Integer.valueOf(id), v);
        }
        v.add(new LocationMap(x0, y0, x1 + 5 - x0, y1 + 5 - y0, toi, xy[0], xy[1]));
    }

    private static int idArrCua(final int map) {
        for (int i = 0; i < Map.mapID.length; i++) {
            for (final short m : Map.mapID[i]) {
                if (m == map) {
                    return i < Map.idArrMap.length ? Map.idArrMap[i] : -1;
                }
            }
        }
        return -1;
    }

    /**
     * Chan qua cong VAO Hoenn (tu ban do ngoai vung sang ban do Hoenn) khi chua du dieu kien; gui
     * thong bao. Tra ve true = da chan (nguoi goi giu nguoi choi o cho cu).
     */
    public static boolean chanVao(final Char p, final Map tu, final int toMapID) {
        try {
            if (!daTao || tu == null || BanDoHoenn.laHoenn(tu.mapIDLoadMap)) {
                return false;
            }
            final Map den = RealController.mapList.get(Integer.valueOf(toMapID));
            if (den == null || !BanDoHoenn.laHoenn(den.mapIDLoadMap)) {
                return false;
            }
            final boolean lauDai = LauDai.bat();
            final int tran = lauDai ? CapDoLauDai.tranCap(LauDai.ngayHienTai(), LevelDetail.expMain.length) : 0;
            final String ly = BanDoHoenn.lyDoChan(lauDai, tran, p.lvDetail.lv,
                    !lauDai || NhiemVuEngine.daXongMa(p, BanDoHoenn.MA_KET_NAM_1));
            if (ly == null) {
                return false;
            }
            p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(ly));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
