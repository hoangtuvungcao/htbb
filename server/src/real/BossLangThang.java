package real;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Boss lang thang cu (MapNapTaiNguyen.loadBoss: Dusknoir / "Gourgeist", Lopunny, Ursaring - BossDusknoir; Salamence,
 * Zapdos - BossDotKich; Gengar - BossMatQuy; Serperior / Servine): 24.000-30.000 tan cong, 100-400 trieu HP, xuat hien
 * ca o ban do cap thap (Fuchsia 8, New Bark 10...). Truoc 31/10/2026 danh lan 220 px moi nguoi choi o gan -> nguoi di qua
 * guc mot don (cong kiem bot cb2). Quyet dinh chu game 31/10/2026: BI DONG - chi danh nguoi da danh no (danh sach thu
 * theo boss, het han {@link #THU_MS} sau don cuoi, bo khi guc / roi ban do), vi tri xuat hien giu nguyen. Lan dau danh
 * (moi {@link #CANH_BAO_MS}) chi canh bao, khong tinh don - danh lan nua la xac nhan.
 */
public final class BossLangThang {

    /** Nguoi danh boss bi boss danh lai toi chung nay sau don cuoi cua ho. */
    public static final long THU_MS = 60000L;
    /** Canh bao lai sau chung nay. */
    public static final long CANH_BAO_MS = 10 * 60000L;

    private static final Map<Monster, Map<Integer, Long>> THU = new WeakHashMap<Monster, Map<Integer, Long>>();
    private static final ConcurrentHashMap<String, Long> DA_CANH_BAO = new ConcurrentHashMap<String, Long>();

    private BossLangThang() {
    }

    /** Boss lang thang cu (bi dong). */
    public static boolean la(final Monster m) {
        return m instanceof BossDusknoir || m instanceof BossDotKich || m instanceof BossSerperior || m instanceof BossServine
                || m instanceof data.BossMatQuy;
    }

    /**
     * Nguoi choi {@code p} ra don vao {@code m} (truoc khi tinh sat thuong). false = chan don nay (lan dau: chi canh bao).
     */
    public static boolean khiDanh(final Monster m, final Char p) {
        if (!la(m) || p == null) {
            return true;
        }
        final long bay = System.currentTimeMillis();
        final String k = p.charDBID + ":" + System.identityHashCode(m);
        final Long cb = DA_CANH_BAO.get(k);
        if (cb == null || bay - cb.longValue() > CANH_BAO_MS) {
            DA_CANH_BAO.put(k, Long.valueOf(bay));
            try {
                p.sendMessage(MessageCreator.createServerAlertMessage(canhBao(m), ""));
            } catch (final Exception ignored) {
            }
            return false;
        }
        ghi(m, p, bay);
        return true;
    }

    static String canhBao(final Monster m) {
        String ten = "Boss";
        try {
            ten = m.getName();
        } catch (final Exception ignored) {
        }
        return ten + " - Rất nguy hiểm: tấn công " + String.format("%,d", m.attack).replace(',', '.') + ", máu "
                + String.format("%,d", m.maxhp).replace(',', '.') + ". Nó chỉ đánh người tấn công nó (đánh lan cả nhóm người đó). "
                + "Đánh lần nữa để xác nhận vào trận.";
    }

    static void ghi(final Monster m, final Char p, final long bay) {
        synchronized (THU) {
            Map<Integer, Long> t = THU.get(m);
            if (t == null) {
                THU.put(m, t = new java.util.HashMap<Integer, Long>());
            }
            t.put(Integer.valueOf(p.charDBID), Long.valueOf(bay));
        }
    }

    /** Boss {@code m} duoc danh {@code c} khong (quai khac: luon duoc). */
    public static boolean duocDanh(final Monster m, final LiveActor c) {
        if (!la(m)) {
            return true;
        }
        if (!(c instanceof Char)) {
            return false;
        }
        final Char p = (Char) c;
        synchronized (THU) {
            final Map<Integer, Long> t = THU.get(m);
            final Long luc = t == null ? null : t.get(Integer.valueOf(p.charDBID));
            if (luc == null) {
                return false;
            }
            if (p.hp <= 0 || p.exit || p.map == null || m.map == null || !p.map.equals(m.map)
                    || System.currentTimeMillis() - luc.longValue() > THU_MS) {
                t.remove(Integer.valueOf(p.charDBID)); // guc / roi ban do / het han: khong con trong danh sach
                return false;
            }
            return true;
        }
    }

    /** Test: xoa trang thai. */
    public static void xoaChoKiem() {
        synchronized (THU) {
            THU.clear();
        }
        DA_CANH_BAO.clear();
    }
}
