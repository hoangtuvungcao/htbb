import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

/**
 * 0.9.6.1 menu "Hệ sinh thái" (NPC ao -528, menu goc): KHONG con muc cau ca (cau ca chi bat dau tu nut ngu canh canh ho
 * = lenh chat "cauca" -> HeSinhThaiMenu.cauCa); nhan menu va xu ly cung doc mot mang khoa (HeSinhThaiMenu.GOC) nen chi
 * so khong lech; client khong con dac cach chi so 1 = cau ca.
 */
public final class KiemMenu096 {

    private KiemMenu096() {
    }

    public static void chay() {
        Kiem.nhom("0.9.6.1 menu He sinh thai: khong muc cau ca, chi so menu goc khop xu ly");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    static void chayThat() throws Exception {
        final List<String> m = com.htbb.pokemon.HeSinhThaiMenu.mucGoc("Nắng", false, 2, 50);
        Kiem.bang("so muc menu goc = so khoa", Integer.valueOf(com.htbb.pokemon.HeSinhThaiMenu.GOC.length),
                Integer.valueOf(m.size()));
        final HashSet<String> da = new HashSet<String>();
        for (final String s : m) {
            Kiem.dung("khong muc cau ca: " + s, !s.toLowerCase().contains("câu cá") && !s.toLowerCase().contains("cau ca"));
            Kiem.dung("muc khong trung: " + s, da.add(s));
        }
        final HashSet<String> khoa = new HashSet<String>();
        for (final String k : com.htbb.pokemon.HeSinhThaiMenu.GOC) {
            Kiem.dung("khoa khong trung: " + k, khoa.add(k));
        }
        // chi so op -> khoa -> nhan dung muc da gui
        final String[][] mongDoi = {
            {"thoiTiet", "Thời tiết: Nắng (ngày)"},
            {"khaoCo", "Khảo cổ: đào tại đây"},
            {"cheTac", "Nấu ăn, chế thuốc, chế tác"},
            {"tui", "Túi sinh thái"},
            {"nhaTre", "Nhà trẻ và trứng (2)"},
            {"cho", "Chợ sinh thái"},
            {"vuon", "Vườn nhà và bạn bè"},
            {"suuTam", "Sưu tầm và nghiên cứu"},
            {"giaiTri", "Giải trí: đố vui, đua, giải đấu"},
            {"kyNang", "Kỹ năng và thể lực (50)"},
        };
        Kiem.bang("so muc mong doi", Integer.valueOf(mongDoi.length), Integer.valueOf(m.size()));
        for (int op = 0; op < mongDoi.length && op < m.size(); op++) {
            Kiem.bang("op " + op + " -> khoa", mongDoi[op][0], com.htbb.pokemon.HeSinhThaiMenu.khoaGoc(op));
            Kiem.bang("op " + op + " -> nhan", mongDoi[op][1], m.get(op));
        }
        Kiem.dung("op ngoai danh sach -> null", com.htbb.pokemon.HeSinhThaiMenu.khoaGoc(m.size()) == null
                && com.htbb.pokemon.HeSinhThaiMenu.khoaGoc(-1) == null);
        // goc(): moi khoa co nhanh xu ly; khong con nhanh cau ca trong menu goc
        final String src = doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        final int a = src.indexOf("static void goc(final Char p, final int op)");
        final int b = src.indexOf("// ================================================================== the gioi", a);
        Kiem.dung("tim thay goc()", a > 0 && b > a);
        final String goc = src.substring(a, b);
        Kiem.dung("goc() khong goi cauCa", !goc.contains("cauCa("));
        for (final String k : new String[]{"G_THOI_TIET", "G_KHAO_CO", "G_CHE_TAC", "G_TUI", "G_NHA_TRE", "G_CHO", "G_VUON",
            "G_SUU_TAM", "G_GIAI_TRI", "G_KY_NANG"}) {
            Kiem.dung("goc() xu ly " + k, goc.contains("case " + k + ":"));
        }
        // cau ca van con cho lenh chat "cauca" (nut ngu canh)
        Kiem.dung("HeSinhThaiMenu.cauCa con", src.contains("public static void cauCa(final Char p)"));
        Kiem.dung("lenh chat cauca con", doc("src/com/htbb/pokemon/HeSinhThaiGame.java").contains("\"cauca\""));
        // client: khong con dac cach chi so 1 (= cau ca) trong menu NPC -528
        final String gs = doc("../client_jar/src/game/render/screen/GameScr.java");
        Kiem.dung("client GameScr.onMenuOption khong goi batDauCau", !gs.contains("SinhThai.batDauCau()"));
        Kiem.dung("client GameScr khong loc muc \"Câu cá\"", !gs.contains("startsWith(\"Câu cá\")"));
    }
}
