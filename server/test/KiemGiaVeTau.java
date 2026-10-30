import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Map;
import real.PotionTemplate2;
import real.cmd.LoginHandler;

/**
 * Bao loi 30/10/2026 (sau khi ha gia ve tau 15.000 -> 5.000, khoi sua_tim_nhiem_vu.sql): "ve tau ghi 15k nhung thuc mua
 * 5k". Menu Lapras "Mua vé (x xu)" cua MOI client (J2ME / PC / Unity) hien GameScr.pTicket, doc bang readShort() ngay sau
 * toc do o goi dang nhap (lenh 1, LoginHandler.createMsgLogin) - may chu viet cung 15000 trong khi Map.doBuyTicket tru
 * gia data_potion 20. Kiem: gia hien = gia that (data_potion 20 sau phan B), khong con so cung, client hien pTicket.
 */
public final class KiemGiaVeTau {

    private KiemGiaVeTau() {
    }

    public static void chay() {
        Kiem.nhom("Gia ve tau hien o Lapras = gia that data_potion 20 (bao loi 30/10/2026: ghi 15k mua 5k)");
        try {
            final int gia = giaSql();
            Kiem.dung("data_potion 20 co gia (sau phan B): " + gia, gia > 0);
            Kiem.dung("gia ve tau vua short co dau cua client (<= 32767)", gia <= Short.MAX_VALUE);
            Kiem.bang("gia ve tau that (phan A + UPDATE phan B)", Integer.valueOf(5000), Integer.valueOf(gia));
            goiDangNhap(gia);
            goiDangNhap(7777);
            ma();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    /** Gia data_potion 20 khi nap sql/htbb.sql: dong phan A roi lan luot cac UPDATE gia o phan B. */
    static int giaSql() {
        final Matcher a = Pattern.compile("\\(20,\\d+,\\d+,\\d+,'Vé tàu','vetau',(\\d+),-?\\d+\\)").matcher(DayDu.sql());
        if (!a.find()) {
            return -1;
        }
        int gia = Integer.parseInt(a.group(1));
        final Matcher b = Pattern.compile("UPDATE `data_potion` SET `price` = (\\d+) WHERE `id` = 20( AND `price` = (\\d+))?;")
                .matcher(DayDu.nangCap());
        while (b.find()) {
            if (b.group(3) == null || Integer.parseInt(b.group(3)) == gia) {
                gia = Integer.parseInt(b.group(1));
            }
        }
        return gia;
    }

    /** Goi dang nhap (lenh 1): [i8 toc do][i16 gia ve] phai mang dung gia data_potion 20. */
    static void goiDangNhap(final int gia) {
        final Vector<PotionTemplate2> cu = Map.potionTemplates2;
        try {
            final Vector<PotionTemplate2> ds = new Vector<PotionTemplate2>();
            for (int i = 0; i <= LoginHandler.VE_TAU; i++) {
                ds.add(new PotionTemplate2((short) i, (short) i, (short) 0, false, "p" + i, "p" + i,
                        i == LoginHandler.VE_TAU ? gia : 100, 0));
            }
            Map.potionTemplates2 = ds;
            Kiem.bang("giaVeTauHienThi = data_potion 20 (" + gia + ")", Integer.valueOf(gia),
                    Integer.valueOf(LoginHandler.giaVeTauHienThi()));
            final java.io.PrintStream err = System.err;
            final byte[] b;
            try {
                // Ban kiem khong nap du du lieu (ky nang, than thu...): phan sau cua goi nem loi va bi bat, phan dau van ghi.
                System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
                b = LoginHandler.createMsgLogin(0).toByteArray();
            } finally {
                System.setErr(err);
            }
            final int dau = b[0] == (byte) -128 ? 6 : 3;
            Kiem.bang("goi dang nhap la lenh 1", Integer.valueOf(1), Integer.valueOf(b[dau == 6 ? 1 : 0]));
            final int ve = (short) (((b[dau + 1] & 255) << 8) | (b[dau + 2] & 255));
            Kiem.bang("goi dang nhap: gia ve tau client hien (GameScr.pTicket) = gia tru that", Integer.valueOf(gia), Integer.valueOf(ve));
            Map.potionTemplates2 = new Vector<PotionTemplate2>();
            Kiem.bang("chua nap data_potion: gia ve 0 (khong nem loi)", Integer.valueOf(0), Integer.valueOf(LoginHandler.giaVeTauHienThi()));
        } finally {
            Map.potionTemplates2 = cu;
        }
    }

    /** Ma nguon: khong con so cung o goi dang nhap; client hien gia tu goi (pTicket), khong ghi so co dinh. */
    static void ma() throws Exception {
        final String lh = doc("src/real/cmd/LoginHandler.java");
        Kiem.dung("LoginHandler: khong con writeShort(15000) (gia ve viet cung)", !lh.contains("writeShort(15000)"));
        Kiem.dung("LoginHandler: goi dang nhap viet giaVeTauHienThi()", lh.contains("writeShort(giaVeTauHienThi())"));
        final String map = doc("src/real/Map.java");
        Kiem.dung("Map.doBuyTicket tru gia data_potion 20", map.contains("player.subXu((long) potionTemplates2.get(20).getPrice()"));
        final String[][] client = {
            {"../client_jar/src/game/networklogic/MessageHandler.java", "GameScr.pTicket = msg.reader().readShort();"},
            {"../client_jar/src/game/render/screen/GameScr.java", "TienTe.XU, pTicket)"},
            {"../client_pc_ios/Assets/Scripts/Assembly-CSharp/MessageHandler.cs", "GameScr.pTicket = msg.reader().readShort();"},
            {"../client_pc_ios/Assets/Scripts/Assembly-CSharp/GameScr.cs", "\"Mua vé (\" + pTicket + \" xu)\""},
        };
        for (final String[] c : client) {
            Kiem.dung(c[0] + ": hien gia ve tu goi dang nhap", doc(c[0]).contains(c[1]));
        }
        final Pattern cung = Pattern.compile("(?i)(vé[^\"\\n]{0,30}\\d{1,3}[.,]?000|\\d{1,3}[.,]?000[^\"\\n]{0,20}vé tàu)");
        final String[] tep = {"../client_jar/src/game/render/screen/GameScr.java", "../client_jar/src/game/render/Res.java",
            "src/real/NpcDichVu.java", "src/com/htbb/pokemon/MoTaVatPham.java", "../client_jar/src/game/pokemon/MoTaVatPham.java"};
        for (final String t : tep) {
            final Matcher m = cung.matcher(doc(t));
            Kiem.dung(t + ": khong ghi cung gia ve tau" + (m.find() ? " (thay: " + m.group() + ")" : ""), !cung.matcher(doc(t)).find());
        }
    }

    static String doc(final String tep) throws Exception {
        return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
    }
}
