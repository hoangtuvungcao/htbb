package real;

import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.ThangPvp;
import data.DatabaseLauDai;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thang PvP mua (GD2 7.2) - ban toi thieu: moi tran ha nguoi choi CUNG DAI 10 CAP (Region /
 * MapDanhNhau) cap nhat Elo hai ben; cung mot cap doi chi tinh 1 lan / 10 phut (chong cay
 * diem bang acc phu). Mua 13 tuan (ThangPvp.mua), sang mua moi reset mem 50%. Luu
 * tob_pvp_mua (bang xep hang) + ho so.
 */
public final class ThangPvpGame {

    static final long CHAN_LAP_MS = 10L * 60L * 1000L;
    private static final ConcurrentHashMap<String, Long> LAN_CUOI = new ConcurrentHashMap<String, Long>();

    private ThangPvpGame() {
    }

    public static void ketQua(final Char thang, final Char thua) {
        if (!LauDai.bat() || thang == null || thua == null || thang.charDBID == thua.charDBID) {
            return;
        }
        if (!com.htbb.cauhinh.CongTac.pvpTinhDiem()) {
            return; // cau hinh trung tam: pvp.tinh_diem.bat=0
        }
        try {
            if (!ThangPvp.cungDai(thang.lvDetail.lv, thua.lvDetail.lv)) {
                return;
            }
            final String k = Math.min(thang.charDBID, thua.charDBID) + "-" + Math.max(thang.charDBID, thua.charDBID);
            final long bay = com.htbb.pokemon.DongHo.bay();
            final Long cu = LAN_CUOI.get(k);
            if (cu != null && bay - cu.longValue() < CHAN_LAP_MS) {
                return;
            }
            LAN_CUOI.put(k, Long.valueOf(bay));
            final HoSoLauDai a = LauDaiNhanVat.lay(thang);
            final HoSoLauDai b = LauDaiNhanVat.lay(thua);
            if (a == null || b == null) {
                return;
            }
            final int mua = ThangPvp.mua(LauDai.ngayHienTai());
            doiMua(a, mua);
            doiMua(b, mua);
            final int ea = a.pvpElo;
            final int eb = b.pvpElo;
            synchronized (a) {
                a.pvpElo = ThangPvp.eloMoi(ea, eb, 1);
                a.pvpThang++;
                a.ban = true;
            }
            synchronized (b) {
                b.pvpElo = ThangPvp.eloMoi(eb, ea, 0);
                b.pvpThua++;
                b.ban = true;
            }
            DatabaseLauDai.ghiPvp(mua, thang.charDBID, a.pvpElo, a.pvpThang, a.pvpThua);
            NhiemVuEngine.suKien(thang, "thang_pvp", "", 1);
            NhiemVuEngine.suKien(thang, "thanh_tuu_pvp", "", 1);
            DatabaseLauDai.ghiPvp(mua, thua.charDBID, b.pvpElo, b.pvpThang, b.pvpThua);
        } catch (final Exception e) {
            System.out.println("ThangPvpGame.ketQua: " + e);
        }
    }

    static void doiMua(final HoSoLauDai h, final int mua) {
        synchronized (h) {
            if (h.pvpMua != mua) {
                h.pvpElo = ThangPvp.resetMem(h.pvpElo);
                h.pvpThang = 0;
                h.pvpThua = 0;
                h.pvpMua = mua;
                h.ban = true;
            }
        }
    }
}
