package real;

import com.htbb.pokemon.ChenhCapNhiemVu;
import com.htbb.pokemon.KhoaBanDo;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Noi {@link ChenhCapNhiemVu} vao may chu: nhiem vu kieu 4 (Char.checkFinishQuest) va su kien giet_lech_cap
 * (NhiemVuEngine.khiHaQuai). Cap quai thuong cao nhat cua tung ban do ghi luc nap luoi quai (MapNapTaiNguyen.loadMap);
 * "ban do luyen" = ban do co luat khoa cho Tu dong danh (data_ban_do_mo_khoa.cho_phep_tu_dong = 1); "da mo" =
 * KhoaBanDoGame.lyDoKhoa(p, id) == null (khoa tat = moi ban do da mo).
 */
public final class ChenhCapNhiemVuGame {

    private ChenhCapNhiemVuGame() {
    }

    /** mapId (moi ban sao) -> cap quai thuong cao nhat trong luoi sinh. */
    static final ConcurrentHashMap<Integer, Integer> CAP_BAN_DO = new ConcurrentHashMap<Integer, Integer>();
    /** charDBID -> {luc ms, cap cao nhat da mo} (giu 30 giay: chi tinh khi quai lech qua N cap). */
    private static final ConcurrentHashMap<Integer, long[]> DEM = new ConcurrentHashMap<Integer, long[]>();
    static final long GIU_MS = 30000L;

    /** MapNapTaiNguyen.loadMap: mot o quai mau {@code t} o ban do {@code mapId}. Chi quai thuong (isuse 1). */
    static void ghiQuai(final int mapId, final MonsterTemplate t) {
        if (t == null || t.typeMons != 1 || t.level <= 0) {
            return;
        }
        final Integer k = Integer.valueOf(mapId);
        final Integer cu = CAP_BAN_DO.get(k);
        if (cu == null || cu.intValue() < t.level) {
            CAP_BAN_DO.put(k, Integer.valueOf(t.level));
        }
    }

    static boolean laBaiLuyen(final int mapId) {
        final KhoaBanDo.Luat l = KhoaBanDoGame.luatCua(mapId);
        return l != null && l.tuDong;
    }

    /** Cap quai thuong cao nhat cua ban do {@code mapId} neu la bai luyen, 0 neu khong. */
    static int capBanDo(final int mapId) {
        final Integer c = CAP_BAN_DO.get(Integer.valueOf(mapId));
        return c == null || !laBaiLuyen(mapId) ? 0 : c.intValue();
    }

    /** Cap quai thuong cao nhat trong moi bai luyen {@code p} dang vao duoc. */
    static int capCaoNhatDaMo(final Char p) {
        final long bay = System.currentTimeMillis();
        final long[] d = DEM.get(Integer.valueOf(p.charDBID));
        if (d != null && bay - d[0] < GIU_MS) {
            return (int) d[1];
        }
        int max = 0;
        for (final java.util.Map.Entry<Integer, Integer> e : CAP_BAN_DO.entrySet()) {
            final int id = e.getKey().intValue();
            if (e.getValue().intValue() <= max || !laBaiLuyen(id)) {
                continue;
            }
            try {
                if (KhoaBanDoGame.lyDoKhoa(p, id) == null) {
                    max = e.getValue().intValue();
                }
            } catch (final Exception ignored) {
            }
        }
        DEM.put(Integer.valueOf(p.charDBID), new long[]{bay, max});
        return max;
    }

    /** Quai cap {@code capQuai} vua bi {@code p} ha (o ban do p dang dung) co tinh cho muc tieu "chenh <= N cap" khong. */
    public static boolean tinh(final Char p, final int capQuai, final int chenh) {
        if (p == null) {
            return false;
        }
        final int lv = p.lvDetail.lv;
        if (Math.abs(lv - capQuai) <= chenh) {
            return true;
        }
        final int noi = capBanDo(p.mapID);
        return noi > 0 && ChenhCapNhiemVu.tinh(lv, capQuai, chenh, noi, capCaoNhatDaMo(p));
    }
}
