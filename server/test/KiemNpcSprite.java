import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Anh NPC = Pokemon (P2 tai nguyen). Doi chieu MOT bang anh xa NPC -> sprite o moi noi:
 *  - sql/pokemon_npc_sprite.sql (data_npc.dex/sprite, data_npc_su_kien_sprite),
 *  - client jar NPC.POKE_DEX_THEO_TYPE, Npc_Server.POKE_DEX_THEO_IDIMG, VatThePokemon.NPC,
 *  - Unity NPC.cs, Npc_Server.cs, VatThePokemon.cs,
 *  - du_lieu/bang_npc.csv, du_lieu/bang_npc_su_kien.csv.
 * Moi dex / vat the phai co sprite dong goi trong jar day du, jar nhe va Unity. Anh du phong
 * npc.sh (= Unity npc.bytes) khong con la goi nguoi kiem hiep cu (tools/pokemon/sinh_npc_sh.py).
 */
public final class KiemNpcSprite {

    /** git blob sha1 cua client_jar/res/npc.sh kiem hiep cu (d4e61c35 va moi ban truoc). */
    static final String NPC_SH_CU = "f564fd147537258c34c8a1150cb1291b21334de4";
    static final byte[] KHOA = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};

    private KiemNpcSprite() {
    }

    public static void chay() {
        Kiem.nhom("Anh NPC Pokemon - SQL, jar, Unity, csv khop; sprite co that; npc.sh khong con anh cu");
        try {
            kiem();
        } catch (final Exception e) {
            Kiem.dung("doc du lieu NPC: " + e, false);
        }
    }

    private static void kiem() throws Exception {
        final String sql = doc("../sql/pokemon_npc_sprite.sql");
        final Map<Integer, Integer> sqlType = new TreeMap<>();
        final Matcher mu = Pattern.compile("SET `dex`=(\\d+), `sprite`='pk:(\\d{4})' WHERE `type`=(\\d+);").matcher(sql);
        while (mu.find()) {
            Kiem.bang("sprite = pk:<dex> type " + mu.group(3), Integer.valueOf(mu.group(1)), Integer.valueOf(Integer.parseInt(mu.group(2))));
            sqlType.put(Integer.valueOf(mu.group(3)), Integer.valueOf(mu.group(1)));
        }
        Kiem.dung("migration co ADD COLUMN IF NOT EXISTS (chay lai duoc)", sql.contains("ADD COLUMN IF NOT EXISTS `sprite`"));
        Kiem.sai("migration khong dung INSERT...SELECT...ON DUPLICATE", sql.replaceAll("(?m)^--.*$", "").matches("(?s).*INSERT[^;]*SELECT[^;]*ON DUPLICATE.*"));

        final int[] jar = mang(doc("../client_jar/src/game/model/NPC.java"), "POKE_DEX_THEO_TYPE");
        final int[] uni = mang(doc("../client_pc_ios/Assets/Scripts/Assembly-CSharp/NPC.cs"), "POKE_DEX_THEO_TYPE");
        Kiem.bang("so NPC tinh trong SQL = jar", Integer.valueOf(jar.length), Integer.valueOf(sqlType.size()));
        Kiem.bang("jar = Unity POKE_DEX_THEO_TYPE", java.util.Arrays.toString(jar), java.util.Arrays.toString(uni));
        final Map<Integer, Integer> csvType = new TreeMap<>();
        for (final Map<String, String> r : csv("../du_lieu/bang_npc.csv")) {
            csvType.put(Integer.valueOf(r.get("type")), Integer.valueOf(r.get("dex")));
        }
        for (int t = 0; t < jar.length; t++) {
            final Integer k = Integer.valueOf(t);
            Kiem.bang("type " + t + " SQL = jar", Integer.valueOf(jar[t]), sqlType.get(k));
            Kiem.bang("type " + t + " csv = jar", Integer.valueOf(jar[t]), csvType.get(k));
            coSprite("NPC tinh type " + t, jar[t]);
        }

        // NPC su kien (lenh -50) theo idImg: dex Pokemon hoac vat the.
        final Map<Integer, String> sqlSk = new TreeMap<>();
        final Matcher mr = Pattern.compile("\\((\\d+),(\\d+),'([^']*)','").matcher(sql.substring(sql.indexOf("REPLACE INTO")));
        while (mr.find()) {
            sqlSk.put(Integer.valueOf(mr.group(1)), mr.group(2) + "|" + mr.group(3));
        }
        final Map<Integer, String> csvSk = new TreeMap<>();
        for (final Map<String, String> r : csv("../du_lieu/bang_npc_su_kien.csv")) {
            csvSk.put(Integer.valueOf(r.get("id_img")), (r.get("dex").isEmpty() ? "0" : r.get("dex")) + "|" + r.get("vat_the"));
        }
        Kiem.bang("data_npc_su_kien_sprite = bang_npc_su_kien.csv", csvSk.toString(), sqlSk.toString());
        Kiem.bang("jar Npc_Server + VatThePokemon = csv", csvSk.toString(), suKien(
                doc("../client_jar/src/game/model/Npc_Server.java"), doc("../client_jar/src/game/pokemon/VatThePokemon.java")).toString());
        Kiem.bang("Unity Npc_Server + VatThePokemon = csv", csvSk.toString(), suKien(
                doc("../client_pc_ios/Assets/Scripts/Assembly-CSharp/Npc_Server.cs"),
                doc("../client_pc_ios/Assets/Scripts/Assembly-CSharp/VatThePokemon.cs")).toString());
        for (final Map.Entry<Integer, String> e : csvSk.entrySet()) {
            final String[] p = e.getValue().split("\\|", -1);
            final int dex = Integer.parseInt(p[0]);
            if (dex > 0) {
                coSprite("NPC su kien idImg " + e.getKey(), dex);
            } else {
                Kiem.dung("NPC su kien idImg " + e.getKey() + " co vat the", !p[1].isEmpty()
                        && new File("../client_jar/res/pk/" + p[1] + ".png").isFile()
                        && new File("../client_jar/res_nhe/pk/" + p[1] + ".png").isFile()
                        && new File("../client_pc_ios/Assets/Resources/poke/" + p[1] + ".bytes").isFile());
            }
        }

        // Anh du phong npc.sh.
        final byte[] sh = Files.readAllBytes(new File("../client_jar/res/npc.sh").toPath());
        Kiem.sai("npc.sh khong con la goi NPC kiem hiep cu", NPC_SH_CU.equals(blobSha1(sh)));
        Kiem.dung("Unity npc.bytes = jar npc.sh",
                java.util.Arrays.equals(sh, Files.readAllBytes(new File("../client_pc_ios/Assets/Resources/res/npc.bytes").toPath())));
        final Map<String, byte[]> goi = docGoi(sh);
        final int[] idImg = mang(doc("../client_jar/src/game/model/NPC.java"), "idImg");
        for (int t = 0; t < idImg.length; t++) {
            final byte[] b = goi.get("npc" + idImg[t] + "1.png");
            Kiem.dung("npc.sh co anh than PNG cho type " + t, b != null && laPng(b) && rongPng(b) > 1);
        }
        int png = 0;
        for (final byte[] b : goi.values()) {
            png += laPng(b) ? 1 : 0;
        }
        Kiem.bang("moi muc npc.sh la PNG", Integer.valueOf(goi.size()), Integer.valueOf(png));
    }

    private static Map<Integer, String> suKien(final String npcServer, final String vatThe) {
        final Map<Integer, String> kq = new TreeMap<>();
        final Matcher a = Pattern.compile("\\{\\s*(\\d+),\\s*(\\d+)\\s*\\}").matcher(khoi(npcServer, "POKE_DEX_THEO_IDIMG"));
        while (a.find()) {
            kq.put(Integer.valueOf(a.group(1)), a.group(2) + "|");
        }
        final Matcher b = Pattern.compile("\\{\\s*(\\d+),\\s*\\d+,\\s*\\d+\\s*\\}").matcher(khoi(vatThe, " NPC ="));
        while (b.find()) {
            kq.put(Integer.valueOf(b.group(1)), "0|vt_npc" + b.group(1));
        }
        return kq;
    }

    /** Doan ma tu ten mang toi dau ';' dau tien (khoi khoi tao). */
    private static String khoi(final String s, final String ten) {
        final int i = s.indexOf(ten);
        return i < 0 ? "" : s.substring(i, s.indexOf(';', i));
    }

    private static int[] mang(final String s, final String ten) {
        final String k = khoi(s, ten + " =").replaceAll("//[^\\n]*", "");
        final String trong = k.substring(k.lastIndexOf('{') + 1, k.indexOf('}'));
        final List<Integer> l = new ArrayList<>();
        for (final String x : trong.split(",")) {
            if (!x.trim().isEmpty()) {
                l.add(Integer.valueOf(x.trim()));
            }
        }
        final int[] kq = new int[l.size()];
        for (int i = 0; i < kq.length; i++) {
            kq[i] = l.get(i).intValue();
        }
        return kq;
    }

    private static void coSprite(final String ai, final int dex) {
        final String d = String.format("%04d", Integer.valueOf(dex));
        Kiem.dung(ai + " dex " + dex + " co sprite (jar, jar nhe, Unity)",
                dex > 0
                        && new File("../client_jar/res/pk/" + d + ".dl").isFile()
                        && new File("../client_jar/res/pk/" + d + "_0.png").isFile()
                        && new File("../client_jar/res_nhe/pk/" + d + ".dl").isFile()
                        && new File("../client_pc_ios/Assets/Resources/poke/" + d + "_dl.bytes").isFile());
    }

    private static Map<String, byte[]> docGoi(final byte[] d) {
        final Map<String, byte[]> kq = new LinkedHashMap<>();
        int i = 1;
        final String[] ten = new String[d[0] & 0xff];
        final int[] dai = new int[ten.length];
        for (int k = 0; k < ten.length; k++) {
            final int n = d[i++] & 0xff;
            final byte[] t = new byte[n];
            for (int j = 0; j < n; j++) {
                t[j] = (byte) (d[i + j] ^ KHOA[j % KHOA.length]);
            }
            i += n;
            ten[k] = new String(t, StandardCharsets.ISO_8859_1);
            dai[k] = ((d[i] & 0xff) << 8) | (d[i + 1] & 0xff);
            i += 2;
        }
        int p = 0;
        for (int k = 0; k < ten.length; k++) {
            final byte[] b = new byte[dai[k]];
            for (int j = 0; j < dai[k]; j++, p++) {
                b[j] = (byte) (d[i + p] ^ KHOA[p % KHOA.length]);
            }
            kq.put(ten[k], b);
        }
        return kq;
    }

    private static boolean laPng(final byte[] b) {
        return b.length > 24 && (b[0] & 0xff) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static int rongPng(final byte[] b) {
        return ((b[16] & 0xff) << 24) | ((b[17] & 0xff) << 16) | ((b[18] & 0xff) << 8) | (b[19] & 0xff);
    }

    static String blobSha1(final byte[] b) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(("blob " + b.length + "\0").getBytes(StandardCharsets.US_ASCII));
        md.update(b);
        final StringBuilder sb = new StringBuilder();
        for (final byte x : md.digest()) {
            sb.append(String.format("%02x", Integer.valueOf(x & 0xff)));
        }
        return sb.toString();
    }

    private static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    /** CSV co dau ngoac kep (bang_npc.csv co dau phay trong cot vai_tro). */
    private static List<Map<String, String>> csv(final String p) throws Exception {
        final List<Map<String, String>> kq = new ArrayList<>();
        String[] dau = null;
        for (final String dong : Files.readAllLines(new File(p).toPath(), StandardCharsets.UTF_8)) {
            if (dong.trim().isEmpty()) {
                continue;
            }
            final List<String> o = new ArrayList<>();
            final StringBuilder sb = new StringBuilder();
            boolean ngoac = false;
            for (int i = 0; i < dong.length(); i++) {
                final char c = dong.charAt(i);
                if (c == '"') {
                    ngoac = !ngoac;
                } else if (c == ',' && !ngoac) {
                    o.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            o.add(sb.toString());
            if (dau == null) {
                dau = o.toArray(new String[0]);
                continue;
            }
            final Map<String, String> r = new LinkedHashMap<>();
            for (int i = 0; i < dau.length; i++) {
                r.put(dau[i], i < o.size() ? o.get(i) : "");
            }
            kq.add(r);
        }
        return kq;
    }
}
