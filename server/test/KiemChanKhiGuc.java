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
        Kiem.nhom("Dang guc: chan NPC / di chuyen bang NPC / mua ban / giao dich / qua cong, moi duong hoi sinh van chay");
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
        Kiem.dung("chan menu NPC -30 (Arcanine 7, So tay -527 Ve lang mien phi)", ChanKhiGuc.chan(-30, 7) && ChanKhiGuc.chan(-30, -527));
        Kiem.dung("chan hop nhap so NPC -31, warp -33", ChanKhiGuc.chan(-31, 0) && ChanKhiGuc.chan(-33, 0));
        Kiem.dung("chan mua / ban / cua hang / gian hang / kho / giao dich", ChanKhiGuc.chan(24, 0) && ChanKhiGuc.chan(28, 0)
                && ChanKhiGuc.chan(-76, 0) && ChanKhiGuc.chan(85, 0) && ChanKhiGuc.chan(95, 0) && ChanKhiGuc.chan(68, 0) && ChanKhiGuc.chan(66, 0));
        Kiem.dung("chan qua cong 12 sang ban do khac", ChanKhiGuc.chan(12, 9));
        Kiem.dung("chan popup -32 cua NPC / hop 95 (id khac minh: Gym, cua ai)", ChanKhiGuc.chan(-32, -30381, 123) && ChanKhiGuc.chan(-32, 7, 123));
        // ---- moi duong hoi sinh / ve, tung phien ban client
        Kiem.dung("hoi sinh: hop moi -101 (client >= -101: XIN, CHON tan thu / thuoc / xu / luong khoa / Trung tam / dong doi)", !ChanKhiGuc.chan(-101, 0, 123));
        Kiem.dung("hoi sinh: lenh 31 'Về làng' (client cu, 0.9.21-0.9.23 nut ve lang)", !ChanKhiGuc.chan(31, 0, 123));
        Kiem.dung("hoi sinh: menu cu -803 (tan thu / thuoc / xu / luong khoa / Trung tam cho client khong co -101)", !ChanKhiGuc.chan(-30, -803, 123));
        Kiem.dung("hoi sinh: menu -804 dong doi cuu (nguoi cuu)", !ChanKhiGuc.chan(-30, -804, 123));
        Kiem.dung("hoi sinh: menu -805 mua Thuoc hoi sinh", !ChanKhiGuc.chan(-30, -805, 123));
        Kiem.dung("hoi sinh: menu cu -802 'Dùng cỏ hồi sinh / Hs bằng xu' (-> HoiSinhGame.chanCu)", !ChanKhiGuc.chan(-30, -802, 123));
        Kiem.dung("hoi sinh: popup cu -1 / 1 cua chinh minh (-32 idActor = minh -> chanCu)", !ChanKhiGuc.chan(-32, 123, 123));
        Kiem.dung("hoi sinh: popup cu cua minh voi id am (short)", !ChanKhiGuc.chan(-32, -5, -5));
        Kiem.dung("hoi sinh: dung Co / Thuoc hoi sinh tu tui (29) - DungVatPham.khiGuc tu loc vat pham", !ChanKhiGuc.chan(29, 0, 123));
        Kiem.dung("hoi sinh: uong Thuoc hoi sinh 119 (22) - DungVatPham.dungThuoc tu loc", !ChanKhiGuc.chan(22, 0, 123));
        Kiem.dung("hoi sinh: 12 -500 nap xong ban do sau ve Trung tam (ca tu ve theo gio)", !ChanKhiGuc.chan(12, -500, 123));
        Kiem.dung("cho chat 27 / ping 11 / di chuyen 4 / dang nhap 1", !ChanKhiGuc.chan(27, 0) && !ChanKhiGuc.chan(11, 0)
                && !ChanKhiGuc.chan(4, 0) && !ChanKhiGuc.chan(1, 0));
        final Message m = goi(-30, -803);
        Kiem.bang("docDau doc short dau", Integer.valueOf(-803), Integer.valueOf(ChanKhiGuc.docDau(m)));
        Kiem.bang("docDau khong lam mat vi tri doc", Integer.valueOf(-803), Integer.valueOf(m.dis.readShort()));
        Kiem.bang("docDau goi rong", Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf(ChanKhiGuc.docDau(new Message(12, new byte[0]))));
        // bo xu ly 29 / 22 khi guc chi cho vat pham hoi sinh (khong chan o cong nen phai con dung)
        final String dvp = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/DungVatPham.java")), java.nio.charset.StandardCharsets.UTF_8);
        Kiem.dung("DungVatPham.dung: guc -> khiGuc (chi vat pham hpVatPham > 0)", dvp.contains("if (p.hp <= 0) {\n            khiGuc(map, p, id, message);")
                && dvp.contains("if (HoiSinhGame.hpVatPham(mon) <= 0) {"));
        Kiem.dung("DungVatPham.dungThuoc: guc -> chi THUOC_HOI_SINH", dvp.contains("if (t == com.htbb.pokemon.HoiSinh.THUOC_HOI_SINH && HoiSinhGame.apDung(p)) {"));
        final String mxl = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/MapXuLyLenh.java")), java.nio.charset.StandardCharsets.UTF_8);
        Kiem.dung("MapXuLyLenh: 29 -> doUseItem, 31 -> doComeHomeWhenDie, -32 -> doPopup", mxl.contains("case 29:\n                map.doUseItem(player, message);")
                && mxl.contains("case 31:\n                map.doComeHomeWhenDie(player);") && mxl.contains("case -32:\n                map.doPopup(player, message);"));
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
            p.id = (short) 4321;
            Kiem.dung("guc: RealController cho popup cu cua minh (-32 idActor 4321)", !RealController.chanKhiGuc(s, goi(-32, 4321)));
            Kiem.dung("guc: RealController chan popup NPC (-32 idActor -30381)", RealController.chanKhiGuc(s, goi(-32, -30381)));
            Kiem.dung("guc: RealController cho -101, 29, 22, menu -802", !RealController.chanKhiGuc(s, goi(-101, 1))
                    && !RealController.chanKhiGuc(s, goi(29, 5)) && !RealController.chanKhiGuc(s, goi(22, 119)) && !RealController.chanKhiGuc(s, goi(-30, -802)));
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
