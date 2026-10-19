package real;

import com.htbb.pokemon.TenHien;
import com.htbb.pokemon.TenMapPokemon;

/**
 * Ten ban do NGUOI CHOI / ADMIN THAY phia server - lay tu bang SQL data_map_ten (com.htbb.pokemon.TenHien) theo ID
 * ban do, khong con ten kiem hiep goc (Map.namMap "Bến bắc", "Hàm tử"...). Ban sao theo khu (2421..2424 cua 106)
 * khong co dong rieng thi ten = TenMapPokemon.tenKhu(ten ban goc, khu) ("Bến tàu phía bắc", "Đông Pallet").
 *
 * {@link #batDau()} goi MOT lan cuoi RealController.add_more_idMapSend2Client (sau khi Map.namMap / Map.nameMap /
 * Map.mapID da gan lai va noi them Hoenn): nap SQL, {@link #ap()} roi bat nap lai tu dong (web sua ten -> ap lai
 * trong 30 giay, khong khoi dong lai). ap() ghi de
 *  - Map.namMap[nhom][khu] (Map.getNameMap: goi 12 vao map, tob_vi_tri_online -> /admin/online, thong bao, log;
 *    goi 105 danh sach xa phu) - o rong giu rong (client an lua chon do);
 *  - Map.nameMap[nhom][cong][khu] (goi 105: ten diem den o cong) theo ID diem den trong Map.locationMap - o rong,
 *    nhan dac biet ("Về làng", "... (Hoenn)", "Về Kanto (Cinnabar)") giu nguyen.
 * Map.getNameMapLocation(0/1) con chen vai ten goc ("Dương đông", "Sơn nam") luc gui - MessageCreator da cho qua
 * TenMapPokemon.doi nen van ra Blackthorn / Pewter.
 */
public final class TenBanDoMayChu {

    private TenBanDoMayChu() {
    }

    /** Nap SQL + ap + bat nap lai tu dong (RealController.add_more_idMapSend2Client). */
    public static void batDau() {
        if (TenHien.nap()) {
            ap();
        }
        TenHien.batNapLaiTuDong(new Runnable() {
            @Override
            public void run() {
                ap();
            }
        });
    }

    /**
     * Nhu {@link #ten} nhung ban sao theo khu khong nam trong nhom Map.mapID (vd 721..724 cua 21 - nhom 21 khong co
     * trong RealController.Map.mapID) lay ten ban goc (mapIDLoadMap) + khu (id % 10). "" neu van khong biet.
     */
    public static String tenDayDu(final int id) {
        final String t = ten(id);
        if (!t.isEmpty()) {
            return t;
        }
        final Map m = RealController.mapList.get(id);
        if (m != null && m.mapIDLoadMap != id) {
            final String goc = ten(m.mapIDLoadMap);
            if (!goc.isEmpty()) {
                return TenMapPokemon.tenKhu(goc, id % 10);
            }
        }
        return "";
    }

    /** Ten theo id ban do bat ky (ban goc hoac ban sao theo khu); "" neu khong biet. */
    public static String ten(final int id) {
        final String rieng = TenHien.tenMap(id);
        if (!rieng.isEmpty()) {
            return rieng;
        }
        final short[][] ds = Map.mapID;
        if (ds != null) {
            for (final short[] nhom : ds) {
                if (nhom == null || nhom.length == 0) {
                    continue;
                }
                for (int k = 0; k < nhom.length; k++) {
                    if (nhom[k] == id) {
                        final String goc = TenHien.tenMap(nhom[0]);
                        return goc.isEmpty() ? "" : TenMapPokemon.tenKhu(goc, k);
                    }
                }
            }
        }
        return "";
    }

    /** Ten de hien cho admin: ten() hoac "Map <id>". */
    public static String tenHien(final int id) {
        final String t = ten(id);
        return t == null || t.isEmpty() ? "Map " + id : t;
    }

    /** Ghi de bang ten trong Map (xem dau lop). Tra so o da doi (log). */
    public static synchronized int ap() {
        int doi = 0;
        final String[][] nam = Map.namMap;
        final short[][] ds = Map.mapID;
        if (nam != null && ds != null) {
            for (int g = 0; g < nam.length && g < ds.length; g++) {
                if (nam[g] == null || ds[g] == null || ds[g].length == 0) {
                    continue;
                }
                final String[] hang = nam[g].clone();
                for (int k = 0; k < hang.length && k < ds[g].length; k++) {
                    if (hang[k] == null || hang[k].isEmpty()) {
                        continue;
                    }
                    final String moi = ten(ds[g][k]);
                    if (!moi.isEmpty() && !moi.equals(hang[k])) {
                        hang[k] = moi;
                        doi++;
                    }
                }
                nam[g] = hang;
            }
        }
        final String[][][] ten = Map.nameMap;
        final short[][][] loc = Map.locationMap;
        if (ten != null && loc != null) {
            for (int i = 0; i < ten.length && i < loc.length; i++) {
                if (ten[i] == null || loc[i] == null) {
                    continue;
                }
                final String[][] nhom = ten[i].clone();
                for (int j = 0; j < nhom.length && j < loc[i].length; j++) {
                    if (nhom[j] == null || loc[i][j] == null) {
                        continue;
                    }
                    final String[] cong = nhom[j].clone();
                    for (int k = 0; k < cong.length && k * 3 < loc[i][j].length; k++) {
                        final String cu = cong[k];
                        if (cu == null || cu.isEmpty() || laNhanRieng(cu)) {
                            continue;
                        }
                        final String moi = ten(loc[i][j][k * 3]);
                        if (!moi.isEmpty() && !moi.equals(cu)) {
                            cong[k] = moi;
                            doi++;
                        }
                    }
                    nhom[j] = cong;
                }
                ten[i] = nhom;
            }
        }
        System.out.println("[TEN BAN DO] doi " + doi + " o ten ban do theo data_map_ten");
        return doi;
    }

    /** Nhan cong khong phai ten ban do: "Về làng", "Về Kanto (Cinnabar)", "Littleroot (Hoenn)". */
    static boolean laNhanRieng(final String s) {
        return s.startsWith("Về ") || s.indexOf('(') >= 0;
    }
}
