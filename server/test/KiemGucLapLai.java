import real.Char;
import real.HoiSinhGame;

/**
 * Cong kiem bot cb2 31/10/2026: nhan vat dang guc ma client van gui di chuyen / danh -> Char.actorDie goi lai moi lan
 * (CharCapNhat.doPlayerMoveThan, Region.doAttackMonster) -> truoc day HoiSinhGame.khiGuc chi bo qua trong 3 giay: moi
 * 3 giay mot "lan guc" moi (phi tai cho x he so lap), dem nguoc hoi sinh tai cho chay lai, su kien nhiem vu "guc" cong
 * lien. Nay: dang guc (chua hoi sinh) thi khong tinh lai; hoi sinh xong guc that lan sau van tinh.
 */
public final class KiemGucLapLai {

    private KiemGucLapLai() {
    }

    public static void chay() {
        Kiem.nhom("Guc lap lai (client guc van gui di chuyen / danh) khong tinh them lan guc");
        final boolean themVung = real.Map.idMapMONSTER.isEmpty();
        if (themVung) {
            real.Map.idMapMONSTER.add(new java.util.Vector<Integer>());
        }
        final Char p = new Char(null);
        try {
            p.charDBID = 987123;
            p.isBot = -1;
            p.cat = 0;
            p.myCountry = 0;
            p.map = new real.Map();
            p.mapID = 3;
            p.charname = "kiemguclap";
            HoiSinhGame.xoaChoKiem(p);
            Kiem.dung("ap dung co che hoi sinh cho nhan vat kiem", HoiSinhGame.apDung(p));
            p.hp = 0;
            HoiSinhGame.khiGuc(p);
            Kiem.bang("guc lan dau: lan 1", Integer.valueOf(1), Integer.valueOf(HoiSinhGame.lanGucChoKiem(p)));
            final long luc = HoiSinhGame.gucLucChoKiem(p);
            // 10 giay sau van guc, client gui di chuyen -> actorDie -> khiGuc lan nua
            HoiSinhGame.luiGucLucChoKiem(p, 10000L);
            HoiSinhGame.khiGuc(p);
            HoiSinhGame.khiGuc(p);
            Kiem.bang("van guc, khiGuc goi lai sau 10 giay: van lan 1", Integer.valueOf(1), Integer.valueOf(HoiSinhGame.lanGucChoKiem(p)));
            Kiem.bang("van guc: luc guc khong bi ghi lai (dem nguoc hoi sinh tai cho khong chay lai)", Long.valueOf(luc - 10000L),
                    Long.valueOf(HoiSinhGame.gucLucChoKiem(p)));
            // hoi sinh (duong khac: nhip thay HP > 0 -> dat lai), guc that lan sau -> lan 2
            p.hp = 100;
            HoiSinhGame.nhip(p);
            HoiSinhGame.luiGucLucChoKiem(p, 10000L);
            p.hp = 0;
            HoiSinhGame.khiGuc(p);
            Kiem.bang("hoi sinh roi guc that lan nua: lan 2", Integer.valueOf(2), Integer.valueOf(HoiSinhGame.lanGucChoKiem(p)));
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            HoiSinhGame.xoaChoKiem(p);
            if (themVung) {
                real.Map.idMapMONSTER.clear();
            }
        }
    }
}
