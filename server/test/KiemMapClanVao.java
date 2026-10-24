import java.util.Vector;
import real.Char;
import real.MapClan;

/**
 * real.MapClan.playerJoin / removePLayer (cong kiem staging 24/10/2026): vao ban do bang khi bang chua co danh sach
 * nguoi choi (GM "mto" toi ban do bang, nhan vat khong bang / bang chua mo) truoc day nem NullPointerException giua
 * Map.move2Map. Nay: tao danh sach, roi ban do khong loi, lan sau dung lai danh sach.
 */
final class KiemMapClanVao {

    private KiemMapClanVao() {
    }

    static void chay() {
        Kiem.nhom("MapClan: vao / roi ban do bang khi chua co danh sach bang");
        try {
            final MapClan m = new MapClan();
            final Char p = new Char(null);
            p.idClan = (short) 77;
            boolean loi = false;
            try {
                m.playerJoin(p);
            } catch (final RuntimeException e) {
                loi = true;
            }
            Kiem.dung("vao khong NullPointerException", !loi);
            final Vector<Char> ds = m.getAllPlayer(0, 77);
            Kiem.bang("co trong danh sach bang", Boolean.TRUE, Boolean.valueOf(ds != null && ds.contains(p)));
            Kiem.bang("ban do cua nhan vat", m, p.map);
            m.removePLayer(p);
            Kiem.bang("roi: khoi danh sach", Boolean.FALSE, Boolean.valueOf(ds.contains(p)));
            final Char q = new Char(null);
            q.idClan = (short) 78;
            boolean loi2 = false;
            try {
                m.removePLayer(q);
            } catch (final RuntimeException e) {
                loi2 = true;
            }
            Kiem.dung("roi khi chua tung vao: khong loi", !loi2);
        } catch (final Throwable t) {
            t.printStackTrace(System.out);
            Kiem.dung("KiemMapClanVao chay het: " + t, false);
        }
    }
}
