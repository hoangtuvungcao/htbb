import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import real.Char;
import real.CharManager;

/**
 * NPC "hoa tieu" / "xa phu" bien mat (su co 02/09/2026) - nguyen nhan goc, sau hai lan sua sai
 * (vung/region roi break/continue - ca hai la loi that nhung khong phai loi dang gay ra trieu
 * chung nay).
 *
 * CHUOI SU KIEN. Goi vi tri Map.writeActorPos chi mang cat/type/id/x/y. Client (client_pc_ios
 * Char.cs) thay actor chua ve duoc thi cu 10 giay goi requestCharInfo(id). Server tra loi trong
 * Map.doSendWearingInfo bang CharManager.instance.getByCharID(id) - null thi return, KHONG gui gi.
 * Ma CharManager.put() cu tu choi CA CUM khi charName da ton tai: 14 bot cung ten "hoa tieu"
 * (MapNapTaiNguyen.loadNpcServer) va 5 bot "xa phu" cua 5 map Loc tri (10/501/502/503/504,
 * MapKhoiTao mapIDLoadMap == 10) thi chi con DAU TIEN moi ten duoc dang ky. Cac con sau nam
 * trong allPlayers cua ban do (da xac nhan bang log luc chay that) nhung khong tra duoc theo id
 * -> client khong bao gio ve. Khop moi trieu chung: xa phu hien o map 10 goc nhung mat o khu
 * Dong/Tay/Nam/Bac (map 501-504), hoa tieu mat o ca 5.
 *
 * SUA: put() voi bot (isBot != -1) van dang ky charID/userID/vChars khi trung ten, chi bo qua
 * o charName. Nguoi choi that giu nguyen luat cu (ten la khoa duy nhat).
 */
public final class KiemCharManagerBotTrungTen {

    private KiemCharManagerBotTrungTen() {
    }

    public static void chay() {
        Kiem.nhom("CharManager.put - bot trung ten hien thi van phai tra duoc theo id");

        quetVanBanNguon();
        kiemHanhVi();
    }

    private static void quetVanBanNguon() {
        final String cm = doc("src/real/CharManager.java");
        Kiem.dung("doc duoc CharManager.java", !cm.isEmpty());
        final String than = doan(cm, "public boolean put(Char p) {", "\n    public void remove(int userID) {");
        Kiem.dung("tim thay than put()", !than.isEmpty());
        Kiem.dung("trung id/userID van tu choi (khoa that su duy nhat)",
                than.contains("if (this.userID.containsKey(p.userID) || this.charID.containsKey(p.id)) {"));
        Kiem.dung("trung ten: chi tu choi NGUOI CHOI THAT (isBot == -1)",
                than.contains("if (tenDaCo && p.isBot == -1) {"));
        Kiem.dung("bot trung ten: van dang ky charID", than.contains("this.charID.put(p.id, p);"));
        Kiem.dung("bot trung ten: van dang ky userID", than.contains("this.userID.put(p.userID, p);"));
        Kiem.dung("bot trung ten: van vao vChars", than.contains("this.vChars.add(p);"));
        Kiem.dung("bot trung ten: KHONG ghi de charName cua con da co",
                than.contains("if (!tenDaCo) {\n                this.charName.put(p.getName(), p);\n            }"));
        Kiem.sai("khong con nhanh tu choi ca cum theo ten cho moi loai",
                than.contains("|| this.charName.containsKey(p.getName())) {\n                return false;"));

        final String map = doc("src/real/Map.java");
        final String thanWearing = doan(map, "public void doSendWearingInfo(Char player, int me, int id) {", "\n    }");
        Kiem.dung("doSendWearingInfo tra theo CharManager.getByCharID - day la duong client hoi 'id nay la ai'",
                thanWearing.contains("CharManager.instance.getByCharID((short) id)"));
    }

    /**
     * Hai bot cung ten, khac id/userID: ca hai phai tra duoc theo id va theo userID; charName giu
     * con dau. Mot nguoi choi that trung ten van bi tu choi nhu cu. Dung id/userID am lon de khong
     * dam vao bat ky doi tuong nao khac trong bo kiem thu; don sach sau khi xong.
     */
    private static void kiemHanhVi() {
        Char botA = null;
        Char botB = null;
        Char nguoiThat = null;
        try {
            botA = new Char(null);
            botB = new Char(null);
            nguoiThat = new Char(null);
        } catch (final Throwable t) {
            Kiem.dung("tao duoc Char(null) trong bo kiem thu (" + t + ") - bo qua phan hanh vi", false);
            return;
        }
        final CharManager cm = CharManager.instance;
        try {
            botA.charname = "kiem hoa tieu";
            botA.isBot = -19;
            botA.id = (short) -30001;
            botA.userID = -930001;
            botB.charname = "kiem hoa tieu";
            botB.isBot = -20;
            botB.id = (short) -30002;
            botB.userID = -930002;
            nguoiThat.charname = "kiem hoa tieu";
            nguoiThat.isBot = -1;
            nguoiThat.id = (short) -30003;
            nguoiThat.userID = -930003;

            Kiem.dung("bot A (ten moi) dang ky duoc", cm.put(botA));
            Kiem.dung("bot B (TRUNG ten A, khac id/userID) van dang ky duoc", cm.put(botB));
            Kiem.dung("getByCharID(A) tra ve A", cm.getByCharID(botA.id) == botA);
            Kiem.dung("getByCharID(B) tra ve B (truoc day null -> client khong ve duoc)", cm.getByCharID(botB.id) == botB);
            Kiem.dung("getByUserID(B) tra ve B", cm.getByUserID(botB.userID) == botB);
            Kiem.dung("charName van tro ve con DAU (A), khong bi B ghi de", cm.charName.get("kiem hoa tieu") == botA);
            Kiem.dung("vChars chua ca A va B", cm.vChars.contains(botA) && cm.vChars.contains(botB));
            Kiem.sai("nguoi choi THAT trung ten van bi tu choi nhu cu", cm.put(nguoiThat));
            Kiem.dung("nguoi choi that bi tu choi thi khong ro ri khoa nao",
                    cm.getByCharID(nguoiThat.id) == null && cm.getByUserID(nguoiThat.userID) == null && !cm.vChars.contains(nguoiThat));

            final Char botTrungId = new Char(null);
            botTrungId.charname = "kiem hoa tieu khac";
            botTrungId.isBot = -21;
            botTrungId.id = botA.id;
            botTrungId.userID = -930004;
            Kiem.sai("bot trung ID voi bot khac van bi tu choi (id la khoa duy nhat that su)", cm.put(botTrungId));
        } finally {
            cm.remove(botA);
            cm.remove(botB);
            cm.remove(nguoiThat);
        }
        Kiem.dung("don sach sau kiem: khong con A", cm.getByCharID((short) -30001) == null);
        Kiem.dung("don sach sau kiem: khong con B", cm.getByCharID((short) -30002) == null);
    }

    private static String doan(final String ma, final String tu, final String den) {
        final int bd = ma.indexOf(tu);
        if (bd < 0) {
            return "";
        }
        final int kt = ma.indexOf(den, bd + tu.length());
        return kt < 0 ? ma.substring(bd) : ma.substring(bd, kt);
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
