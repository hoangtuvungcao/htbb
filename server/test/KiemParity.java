import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 0.9.8: DOI CHIEU tinh nang 0.9.7 -> ban hien tai (khong de mat tinh nang khi lam lai giao dien / he thong).
 * Tep ../du_lieu/parity_097.txt (sinh tu main 1a705be6): moi muc o mot muc [loai] tep -
 *   case  : nhan "case N:" cua MapXuLyLenh / MapDoMenu (goi tin C->S, menu NPC)
 *   gm    : lenh GM trong MapChat ("ten")
 *   khoa  : khoa cau hinh trong sql/htbb_day_du.sql ('khoa')
 *   bang  : bang CSDL (CREATE TABLE `bang`)
 *   hud   : ten vung cham HUD PC / J2ME ("hud-...")
 *   chuoi : chuoi giao dien (ten o Menu, ma phim PC) phai con trong tep
 * Dong "muc = thay bang: ly do" = co y bo / doi (chu game duyet) - khong kiem. Muc moi mat ma khong co dong do -> HONG.
 */
public final class KiemParity {

    private KiemParity() {
    }

    static final Map<String, String> DOC = new HashMap<String, String>();

    static String doc(final String tep) {
        String s = DOC.get(tep);
        if (s == null) {
            try {
                s = new String(Files.readAllBytes(Paths.get("..", tep)), StandardCharsets.UTF_8);
            } catch (final Exception e) {
                s = "";
            }
            DOC.put(tep, s);
        }
        return s;
    }

    static boolean con(final String loai, final String tep, final String muc) {
        final String s = doc(tep);
        if ("case".equals(loai)) {
            return s.contains("case " + muc + ":");
        }
        if ("khoa".equals(loai)) {
            return s.contains("'" + muc + "'");
        }
        if ("bang".equals(loai)) {
            return s.contains("CREATE TABLE `" + muc + "`") || s.contains("CREATE TABLE IF NOT EXISTS `" + muc + "`");
        }
        return s.contains("\"" + muc);
    }

    public static void chay() {
        Kiem.nhom("Doi chieu 0.9.7 -> 0.9.8 (du_lieu/parity_097.txt): goi tin, menu NPC, lenh GM, khoa, bang, HUD, menu, phim");
        List<String> dong;
        try {
            dong = Files.readAllLines(Paths.get("..", "du_lieu", "parity_097.txt"), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            Kiem.dung("doc du_lieu/parity_097.txt", false);
            return;
        }
        String loai = null, tep = null;
        int tong = 0, thay = 0;
        final StringBuilder mat = new StringBuilder();
        final Map<String, Integer> demLoai = new HashMap<String, Integer>();
        for (final String d0 : dong) {
            final String d = d0.trim();
            if (d.isEmpty() || d.startsWith("#")) {
                continue;
            }
            if (d.startsWith("[")) {
                loai = d.substring(1, d.indexOf(']'));
                tep = d.substring(d.indexOf(']') + 1).trim();
                Kiem.dung("tep doi chieu ton tai: " + tep, !doc(tep).isEmpty());
                continue;
            }
            if (loai == null) {
                continue;
            }
            if (d.contains(" = thay bang:")) {
                thay++;
                continue;
            }
            tong++;
            demLoai.put(loai, demLoai.containsKey(loai) ? demLoai.get(loai) + 1 : 1);
            if (!con(loai, tep, d)) {
                mat.append("\n      [").append(loai).append("] ").append(tep).append(": ").append(d);
            }
        }
        Kiem.dung("du lieu doi chieu du lon (" + tong + " muc " + demLoai + ")", tong > 700);
        Kiem.dung("khong muc 0.9.7 nao mat ngoai y muon (" + thay + " muc co y thay bang)" + mat, mat.length() == 0);
    }
}
