import java.util.ConcurrentModificationException;
import real.Char;
import real.EffectBuff;

/**
 * Tai 315 bot cb2 (31/10/2026): ConcurrentModificationException lap lai trong vong nhip may chu -
 * Char.updateEffectBuff (Vector$Itr.remove khi luong khac them hieu ung) va MapVongLap.updateSand (Hashtable quai doi khi
 * duyet). Kiem: updateEffectBuff duyet ban sao (them hieu ung song song khong nem, hieu ung het han van go), cac vong quai
 * duyet ban sao.
 */
public final class KiemCmeTai {

    private KiemCmeTai() {
    }

    public static void chay() {
        Kiem.nhom("Tai cao: khong ConcurrentModificationException o hieu ung nhan vat / vong quai ban do");
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        try {
            final Char p = new Char(null);
            for (int i = 0; i < 50; i++) {
                final EffectBuff e = new EffectBuff(EffectBuff.LUA_NHO, System.currentTimeMillis() - 1000L);
                p.AllEffBuff.add(e);
            }
            final boolean[] dung = {false};
            final Throwable[] loi = {null};
            final Thread them = new Thread(() -> {
                while (!dung[0]) {
                    p.AllEffBuff.add(new EffectBuff(EffectBuff.LUA_NHO, System.currentTimeMillis() + 60000L));
                    if (p.AllEffBuff.size() > 2000) {
                        p.AllEffBuff.clear();
                    }
                }
            });
            them.start();
            try {
                for (int i = 0; i < 2000; i++) {
                    p.updateEffectBuff();
                }
            } catch (final ConcurrentModificationException e) {
                loi[0] = e;
            } finally {
                dung[0] = true;
                try {
                    them.join(2000);
                } catch (final InterruptedException ignored) {
                }
            }
            Kiem.dung("updateEffectBuff khi luong khac them hieu ung lien tuc: khong ConcurrentModificationException", loi[0] == null);
            final Char q = new Char(null);
            q.AllEffBuff.add(new EffectBuff(EffectBuff.LUA_NHO, System.currentTimeMillis() - 1000L));
            q.AllEffBuff.add(new EffectBuff(EffectBuff.LUA_NHO, System.currentTimeMillis() + 600000L));
            q.updateEffectBuff();
            Kiem.bang("hieu ung het han van bi go, con song giu lai", Integer.valueOf(1), Integer.valueOf(q.AllEffBuff.size()));
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
        }
        for (final String f : new String[]{"src/real/MapVongLap.java", "src/real/RegionNuiChauBau.java", "src/real/MaterialMap.java", "src/real/RegionMapMoba.java", "src/real/Map.java"}) {
            final String s = KiemCanBangDong.doc(f);
            Kiem.dung(f + ": vong quai duyet ban sao (khong duyet thang Hashtable)", !s.contains("for (Monster mt : listmonster) {")
                    && s.contains("new java.util.ArrayList<Monster>(listmonster)"));
        }
    }
}
