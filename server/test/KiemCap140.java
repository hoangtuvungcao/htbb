/**
 * Hoi quy cac loi tim ra khi bot choi tu nhien cap 1 -> 40 (nhanh bot-cap1-40, 25/10/2026; tools/pokemon/kiem_tai/
 * KichBanCap140). Moi muc ghi trieu chung nguoi choi thay.
 */
public final class KiemCap140 {

    private KiemCap140() {
    }

    public static void chay() {
        Kiem.nhom("bot-cap1-40: loi tim ra khi choi tu nhien cap 1 -> 40");
        demDangLam();
        huyChinhDungId();
    }

    /**
     * Goi -64 huy (type 2) voi id KHAC nhiem vu chinh dang lam: khong huy (truoc day reset nhiem vu chinh bat ke id - bot
     * thay buoc dia diem #14 da nhan quay ve "co the nhan" sau khi vao lai).
     */
    static void huyChinhDungId() {
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (gemCu == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
        t.id = 14;
        t.type = 0;
        t.name = "Thử huỷ";
        t.main_sub = 0;
        t.idItemgift = new short[]{32000};
        t.potiongift = new short[]{32000};
        t.npotiongift = new short[]{32000};
        final data.InfoQuestTemplate cu = real.Map.allMainQuest.get(Short.valueOf((short) 14));
        try {
            real.Map.allMainQuest.put(Short.valueOf((short) 14), t);
            final real.Char p = new real.Char(null);
            final real.NewCharQuest q = new real.NewCharQuest(true);
            q.id_quest = 14;
            q.main_sub = 0;
            q.state_quest = real.NewCharQuest.WORKING;
            q.monsterKilled = new short[]{7};
            q.nItem = new short[]{0};
            p.mainQuest = q;
            try {
                p.OnQuest((byte) 2, (short) 0, (byte) 0);
            } catch (final RuntimeException e) {
                // gui goi / luu CSDL khong co trong test: trang thai da quyet dinh truoc do
            }
            Kiem.bang("huy id 0 khi dang lam #14: van dang lam", Integer.valueOf(real.NewCharQuest.WORKING), Integer.valueOf(p.mainQuest.state_quest));
            Kiem.bang("huy id 0: tien do quai giu nguyen", Integer.valueOf(7), Integer.valueOf(p.mainQuest.monsterKilled[0]));
            try {
                p.OnQuest((byte) 2, (short) 14, (byte) 0);
            } catch (final RuntimeException e) {
                // nhu tren
            }
            Kiem.bang("huy dung id #14: ve chua nhan", Integer.valueOf(real.NewCharQuest.UN_RECEIVE), Integer.valueOf(p.mainQuest.state_quest));
        } finally {
            if (cu == null) {
                real.Map.allMainQuest.remove(Short.valueOf((short) 14));
            } else {
                real.Map.allMainQuest.put(Short.valueOf((short) 14), cu);
            }
            real.Map.gemTemplate = gemCu;
        }
    }

    /** "Đang làm (0)" ma ben trong co thanh tuu cho nhan thuong: so dem phai cung luat voi danh sach. */
    static void demDangLam() {
        Kiem.dung("dang lam: nhiem vu chua xong hien", real.NhiemVuEngine.hienDangLam(false, 0));
        Kiem.dung("dang lam: nhiem vu xong cho nhan hien", real.NhiemVuEngine.hienDangLam(false, 1));
        Kiem.sai("dang lam: da nhan thuong khong hien", real.NhiemVuEngine.hienDangLam(false, 2));
        Kiem.dung("dang lam: thanh tuu xong cho nhan (Bước chân đầu tiên 10/10) hien VA duoc dem", real.NhiemVuEngine.hienDangLam(true, 1));
        Kiem.sai("dang lam: thanh tuu dang tich luy khong hien", real.NhiemVuEngine.hienDangLam(true, 0));
    }

}
