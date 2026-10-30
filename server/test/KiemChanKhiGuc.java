import com.htbb.pokemon.ChanKhiGuc;
import io.Message;
import io.Session;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import real.Char;
import real.RealController;

/**
 * Nhan vat dang guc (HP &lt;= 0) khong dung NPC / di chuyen bang NPC / mua ban / giao dich / dung vat pham / qua cong
 * (chu game 31/10/2026, sau bot cb2: guc van cuoi Arcanine ve lang, mua do, qua cong). Van hoi sinh / chat / nap ban do.
 */
public final class KiemChanKhiGuc {

    private KiemChanKhiGuc() {
    }

    public static void chay() {
        Kiem.nhom("Dang guc: chan NPC / di chuyen bang NPC / mua ban / giao dich / dung do / qua cong, van hoi sinh");
        try {
            logic();
            tichHop();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static Message goi(final int cmd, final int dau) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(dau);
        dos.writeByte(1);
        dos.writeByte(2);
        dos.flush();
        return new Message(cmd, bos.toByteArray());
    }

    static void logic() throws Exception {
        Kiem.dung("chan noi chuyen NPC (23 / 53 / 55)", ChanKhiGuc.chan(23, 0) && ChanKhiGuc.chan(53, 0) && ChanKhiGuc.chan(55, 0));
        Kiem.dung("chan menu NPC -30 (Arcanine 7, So tay -527 Ve lang)", ChanKhiGuc.chan(-30, 7) && ChanKhiGuc.chan(-30, -527));
        Kiem.dung("cho menu hoi sinh cu -803 / -804 / -805", !ChanKhiGuc.chan(-30, -803) && !ChanKhiGuc.chan(-30, -804) && !ChanKhiGuc.chan(-30, -805));
        Kiem.dung("chan mua / ban / cua hang / gian hang / kho / giao dich", ChanKhiGuc.chan(24, 0) && ChanKhiGuc.chan(28, 0)
                && ChanKhiGuc.chan(-76, 0) && ChanKhiGuc.chan(85, 0) && ChanKhiGuc.chan(95, 0) && ChanKhiGuc.chan(68, 0) && ChanKhiGuc.chan(66, 0));
        Kiem.dung("chan dung vat pham (ve dich chuyen) 29", ChanKhiGuc.chan(29, 0));
        Kiem.dung("chan qua cong 12 sang ban do khac", ChanKhiGuc.chan(12, 9));
        Kiem.dung("cho 12 -500 (nap xong ban do sau hoi sinh)", !ChanKhiGuc.chan(12, -500));
        Kiem.dung("cho hoi sinh -101 / ve lang 31 / chat 27 / ping 11 / di chuyen 4 / dang nhap 1", !ChanKhiGuc.chan(-101, 0) && !ChanKhiGuc.chan(31, 0)
                && !ChanKhiGuc.chan(27, 0) && !ChanKhiGuc.chan(11, 0) && !ChanKhiGuc.chan(4, 0) && !ChanKhiGuc.chan(1, 0));
        final Message m = goi(-30, -803);
        Kiem.bang("docDau doc short dau", Integer.valueOf(-803), Integer.valueOf(ChanKhiGuc.docDau(m)));
        Kiem.bang("docDau khong lam mat vi tri doc", Integer.valueOf(-803), Integer.valueOf(m.dis.readShort()));
        Kiem.bang("docDau goi rong", Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf(ChanKhiGuc.docDau(new Message(12, new byte[0]))));
    }

    static void tichHop() throws Exception {
        final int uid = 987650;
        final Char p = new Char(null);
        final Session s = new Session(null, null, true);
        s.userID = uid;
        p.map = new real.Map();
        real.CharManager.instance.userID.put(Integer.valueOf(uid), p);
        try {
            p.hp = 0;
            Kiem.dung("guc: RealController chan menu Arcanine (-30 npc 7)", RealController.chanKhiGuc(s, goi(-30, 7)));
            Kiem.dung("guc: RealController chan mua (24)", RealController.chanKhiGuc(s, goi(24, 1)));
            Kiem.dung("guc: RealController cho menu hoi sinh (-30 -803)", !RealController.chanKhiGuc(s, goi(-30, -803)));
            Kiem.dung("guc: RealController cho ve lang (31)", !RealController.chanKhiGuc(s, goi(31, 0)));
            p.hp = 100;
            Kiem.dung("con song: khong chan gi (-30 npc 7, 24, 12)", !RealController.chanKhiGuc(s, goi(-30, 7))
                    && !RealController.chanKhiGuc(s, goi(24, 1)) && !RealController.chanKhiGuc(s, goi(12, 9)));
            p.hp = 0;
            p.isAdmin = true;
            Kiem.dung("admin guc: khong chan", !RealController.chanKhiGuc(s, goi(24, 1)));
            p.isAdmin = false;
            p.map = null;
            Kiem.dung("chua vao ban do (dang nhap, HP chua nap): khong chan", !RealController.chanKhiGuc(s, goi(12, 9)));
        } finally {
            real.CharManager.instance.userID.remove(Integer.valueOf(uid));
        }
    }
}
