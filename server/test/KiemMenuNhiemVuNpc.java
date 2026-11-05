import com.htbb.pokemon.MenuNhiemVuNpcCu;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Bao loi 05/11/2026 (Truong lang Xatu: luoi 7 ten nhiem vu tron + "Thêm" - "khong biet cai nao la nhan, tra, nhet nhieu
 * qua"):
 *  - client moi (game.model.MenuNhiemVuNpc, bien dich tai cho va chay that): "* Trả:" -> "! Nhận:" chinh -> phu (qua 3 gom
 *    "Nhiệm vụ phụ (N)") -> "… Đang làm: ten (x/y)" (tu 2 gom), khong lap tien to may chu;
 *  - may chu cho client cu (MenuNhiemVuNpcCu): chinh truoc, moi NPC toi da 3 phu, ten "! Nhận: ", client >= 932 khong doi;
 *  - moi ky tu nhan co trong font anh J2ME (hoac Font.taoKyTu ve duoc);
 *  - noi day: GameScr.getAllQuestNpc / MessageCreator.createMsgCharQuest.
 */
public final class KiemMenuNhiemVuNpc {

    private KiemMenuNhiemVuNpc() {
    }

    public static void chay() {
        Kiem.nhom("Menu nhiem vu NPC ro trang thai + gon (bao loi Xatu 05/11/2026)");
        try {
            mayChuClientCu();
            client();
            noiDay();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static void mayChuClientCu() {
        Kiem.dung("client 931 / 930 / Unity (-1) la cu", !MenuNhiemVuNpcCu.clientTuGan(931) && !MenuNhiemVuNpcCu.clientTuGan(930)
                && !MenuNhiemVuNpcCu.clientTuGan(-1));
        Kiem.dung("client 932 tu gan nhan", MenuNhiemVuNpcCu.clientTuGan(932) && MenuNhiemVuNpcCu.clientTuGan(940));
        Kiem.bang("ten nhan client cu", "! Nhận: Mật ong ngọt", MenuNhiemVuNpcCu.tenNhan("Mật ong ngọt"));
        Kiem.bang("ten nhan khong lap tien to", "! Nhận: Mật ong ngọt", MenuNhiemVuNpcCu.tenNhan(MenuNhiemVuNpcCu.tenNhan("Mật ong ngọt")));
        // Xatu (npc 5): 7 phu + chinh o cuoi danh sach goc; NPC 9: 2 phu; doi (mainSub 2) giu
        final int[] npc = {5, 5, 5, 9, 5, 5, 5, 5, 9, 5, 30};
        final int[] ms = {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2};
        final List<Integer> giu = MenuNhiemVuNpcCu.loc(npc, ms);
        Kiem.bang("client cu: chinh truoc, Xatu 3 phu gan cap nhat, NPC khac giu, doi giu", Arrays.asList(9, 0, 1, 2, 3, 8, 10), giu);
        Kiem.bang("client cu: danh sach rong", Arrays.asList(), MenuNhiemVuNpcCu.loc(new int[0], new int[0]));
    }

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    static void client() throws Exception {
        final javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            System.out.println("   (bo qua phan chay client MenuNhiemVuNpc: khong co javac)");
            return;
        }
        final File ra = Files.createTempDirectory("kiemmenunv").toFile();
        final int rc = jc.run(null, null, null, "-nowarn", "-encoding", "UTF-8", "-d", ra.getPath(), "../client_jar/src/game/model/MenuNhiemVuNpc.java");
        Kiem.bang("bien dich client MenuNhiemVuNpc.java", 0, rc);
        if (rc != 0) {
            return;
        }
        final java.net.URLClassLoader cl = new java.net.URLClassLoader(new java.net.URL[]{ra.toURI().toURL()}, null);
        final Class<?> c = cl.loadClass("game.model.MenuNhiemVuNpc");
        final Method nhan = c.getMethod("nhan", int.class, String.class, int.class, int.class);
        final Method xep = c.getMethod("xep", int[].class);
        final Method trongNhom = c.getMethod("trongNhom", int[].class, int.class);
        final Method tenNhom = c.getMethod("tenNhom", int.class, int.class);
        final int tra = c.getField("TRA").getInt(null), chinh = c.getField("CHINH").getInt(null), phu = c.getField("PHU").getInt(null),
                dang = c.getField("DANG").getInt(null), nhomPhu = c.getField("NHOM_PHU").getInt(null), nhomDang = c.getField("NHOM_DANG").getInt(null);

        Kiem.bang("tien to nhan client = may chu", MenuNhiemVuNpcCu.NHAN, c.getField("NHAN").get(null));
        Kiem.bang("nhan: tra", "* Trả: Đêm mưa sao băng", nhan.invoke(null, tra, "Đêm mưa sao băng", 0, 0));
        Kiem.bang("nhan: nhan chinh", "! Nhận: Đêm mưa sao băng", nhan.invoke(null, chinh, "Đêm mưa sao băng", 0, 0));
        Kiem.bang("nhan: phu (ten may chu da gan tien to -> khong lap)", "! Nhận: Mật ong ngọt", nhan.invoke(null, phu, "! Nhận: Mật ong ngọt", 0, 0));
        Kiem.bang("nhan: dang lam co tien do", "… Đang làm: Quả mọng tươi (3/10)", nhan.invoke(null, dang, "Quả mọng tươi", 3, 10));
        Kiem.bang("nhan: dang lam khong dem duoc", "… Đang làm: Buổi diễn", nhan.invoke(null, dang, "Buổi diễn", 0, 0));
        Kiem.bang("nhan: dang lam vuot can -> kep", "… Đang làm: Buổi diễn (5/5)", nhan.invoke(null, dang, "Buổi diễn", 7, 5));
        Kiem.bang("ten nhom phu", "Nhiệm vụ phụ (6)", tenNhom.invoke(null, nhomPhu, 6));
        Kiem.bang("ten nhom dang lam", "… Đang làm (2)", tenNhom.invoke(null, nhomDang, 2));

        // man that Xatu: 1 chinh + 6 phu nhan duoc (thu tu goc: phu xen chinh), khong tra / dang lam
        final int[] xatu = {phu, phu, chinh, phu, phu, phu, phu};
        Kiem.bang("Xatu 7 dong: chinh truoc, 6 phu gom mot dong", "[2, " + nhomPhu + "]", Arrays.toString((int[]) xep.invoke(null, (Object) xatu)));
        Kiem.bang("Xatu: nhom phu giu thu tu goc (gan cap)", "[0, 1, 3, 4, 5, 6]", Arrays.toString((int[]) trongNhom.invoke(null, xatu, nhomPhu)));
        // du loai: dang lam o dau, tra o giua - xep Tra -> Chinh -> Phu -> Dang
        final int[] tron = {dang, phu, tra, chinh, phu};
        Kiem.bang("tra -> chinh -> phu (<= 3 hien thang) -> dang lam", "[2, 3, 1, 4, 0]", Arrays.toString((int[]) xep.invoke(null, (Object) tron)));
        final int[] baPhu = {phu, phu, phu};
        Kiem.bang("3 phu: hien thang, khong gom", "[0, 1, 2]", Arrays.toString((int[]) xep.invoke(null, (Object) baPhu)));
        final int[] bonPhu = {phu, phu, phu, phu};
        Kiem.bang("4 phu: gom", "[" + nhomPhu + "]", Arrays.toString((int[]) xep.invoke(null, (Object) bonPhu)));
        final int[] haiDang = {dang, tra, dang};
        Kiem.bang("2 dang lam: gom sau tra", "[1, " + nhomDang + "]", Arrays.toString((int[]) xep.invoke(null, (Object) haiDang)));
        Kiem.bang("nhom dang lam", "[0, 2]", Arrays.toString((int[]) trongNhom.invoke(null, haiDang, nhomDang)));
        Kiem.bang("rong", "[]", Arrays.toString((int[]) xep.invoke(null, (Object) new int[0])));
        // trang dau toi da: 3 o (chinh/phu/doi: tra hoac dang lam) + chinh + nhom phu + nhom dang <= 6, con cho "Nhiệm vụ của
        // tôi" + "Nói chuyện" trong 8 nut / trang (MenuNguCanh.TOI_DA)
        final int[] nang = {tra, tra, dang, chinh, phu, phu, phu, phu, phu};
        Kiem.dung("trang dau <= 6 dong", ((int[]) xep.invoke(null, (Object) nang)).length <= 6);

        // moi ky tu cua nhan: co trong font anh J2ME (normalFont) hoac Font.taoKyTu ve duoc (KY_TU)
        final String f = doc("../client_jar/src/game/render/screen/Font.java");
        final int a = f.indexOf("normalFont[i] = new Font(\"") + "normalFont[i] = new Font(\"".length();
        final String bang = f.substring(a, f.indexOf('"', a));
        final int k = f.indexOf("KY_TU = \"") + "KY_TU = \"".length();
        final String kyTu = f.substring(k, f.indexOf("\";\n", k)).replace("\\u2026", "…");
        final String chu = "* Trả: ! Nhận: … Đang làm: Nhiệm vụ phụ (3/10)";
        String thieu = "";
        for (final char ch : chu.toCharArray()) {
            if (bang.indexOf(ch) < 0 && kyTu.indexOf(ch) < 0) {
                thieu += ch;
            }
        }
        Kiem.bang("ky tu nhan menu co trong font J2ME", "", thieu);
        Kiem.dung("tien to may chu cho client cu chi dung ky tu font anh goc (khong can taoKyTu)", chiTrongBang(MenuNhiemVuNpcCu.NHAN, bang));
    }

    static boolean chiTrongBang(final String s, final String bang) {
        for (final char ch : s.toCharArray()) {
            if (bang.indexOf(ch) < 0) {
                return false;
            }
        }
        return true;
    }

    static void noiDay() throws Exception {
        final String gs = doc("../client_jar/src/game/render/screen/GameScr.java");
        final int a = gs.indexOf("public static Vector getAllQuestNpc(int idNpc)");
        final String ham = a < 0 ? "" : gs.substring(a, gs.indexOf("public static Image getImgQuest", a));
        Kiem.dung("GameScr.getAllQuestNpc dung MenuNhiemVuNpc.xep / nhan / trongNhom", ham.contains("MenuNhiemVuNpc.xep(") && ham.contains("MenuNhiemVuNpc.nhan(")
                && ham.contains("MenuNhiemVuNpc.trongNhom("));
        Kiem.dung("GameScr.getAllQuestNpc khong con \"Trả \" + ten cho nhiem vu dang lam", !ham.contains("\"Trả \""));
        Kiem.dung("nhom con: MenuNguCanh (cham) / menu chu (phim J2ME)", ham.contains("MenuNguCanh.mo(v") && ham.contains("Canvas.menu.startAt(v, 3)"));
        Kiem.bang("\"Nói chuyện\" ca hai duong NPC chen truoc dong dang lam (trang dau may cham)", 2,
                gs.split("chenNoiChuyen\\(allquest, new Command\\(\"Nói chuyện\"", -1).length - 1);
        final String mc = doc("src/real/MessageCreator.java");
        Kiem.dung("MessageCreator -64 loai 0: client cu loc + gan ten", mc.contains("locNhanChoClientCu(allQuest)") && mc.contains("MenuNhiemVuNpcCu.tenNhan(ten)")
                && mc.contains("type == 0 && !com.htbb.pokemon.MenuNhiemVuNpcCu.clientTuGan("));
    }
}
