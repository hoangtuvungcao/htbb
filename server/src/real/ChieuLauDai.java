package real;

import com.htbb.pokemon.LauDai;
import data.DatabaseLauDai;
import java.util.List;

/**
 * Nap so lieu chieu moi (data_noi_dung_chieu, NOI_DUNG_DAY_DU.md muc 5) vao CharManager khi bat co
 * (GD2 6.3 / 9.4): % sat thuong cap 1..10 (noi suy tuyen tinh cap1 -> cap10), so don
 * (UP_DAMGE_SKILL) va hoi chieu (SKILL_COOLDOWN, don vi 0,1 giay nhu bang cu). Chi ghi vao o DA CO
 * (khong doi do dai mang, client doc theo o). Tat co: bang cu nguyen ven.
 */
final class ChieuLauDai {

    private ChieuLauDai() {
    }

    static void apDung() {
        if (!LauDai.bat()) {
            return;
        }
        final List<Object[]> ds = DatabaseLauDai.napChieu();
        int n = 0;
        for (final Object[] d : ds) {
            try {
                final int c = so(d[0]);
                final int o = so(d[1]);
                final int p1 = so(d[3]);
                final int p10 = so(d[4]);
                final int don = Math.max(1, so(d[5]));
                final double hoi = d[6] instanceof Number ? ((Number) d[6]).doubleValue() : 0.0;
                if (c < 0 || c >= CharManager.SKILL_DAM_PERCENT.length || o < 0 || o >= CharManager.SKILL_DAM_PERCENT[c].length) {
                    continue;
                }
                final short[] pt = CharManager.SKILL_DAM_PERCENT[c][o];
                for (int lv = 1; lv < pt.length; lv++) {
                    final int cap = Math.min(10, lv);
                    pt[lv] = (short) Math.min(Short.MAX_VALUE, p1 + (p10 - p1) * (cap - 1) / 9);
                }
                if (pt.length > 0) {
                    pt[0] = (short) p1;
                }
                if (c < CharManager.UP_DAMGE_SKILL.length && o < CharManager.UP_DAMGE_SKILL[c].length) {
                    final byte[] u = CharManager.UP_DAMGE_SKILL[c][o];
                    for (int lv = 0; lv < u.length; lv++) {
                        u[lv] = (byte) Math.min(Byte.MAX_VALUE, don);
                    }
                }
                if (hoi > 0 && c < CharManager.SKILL_COOLDOWN.length && o < CharManager.SKILL_COOLDOWN[c].length) {
                    final short[] cd = CharManager.SKILL_COOLDOWN[c][o];
                    for (int lv = 0; lv < cd.length; lv++) {
                        cd[lv] = (short) Math.max(1, Math.round(hoi * 10));
                    }
                }
                n++;
            } catch (final Exception e) {
                // dong hong: bo qua
            }
        }
        System.out.println("[LAU DAI] chieu moi: " + n + "/" + ds.size() + " o da nap vao CharManager");
    }

    static int so(final Object o) {
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }
}
