import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * Kiem thu tools/pokemon/sinh_icon_thay_cu.py: icon cu kieu kiem hiep da bi ghi de TAI CHO bang anh Pokemon
 * (map/icon, o anh 1 byte thuoc/ngoc/thu/nhiem vu/trai cay, huy hieu bang, ky nang bang, Tu binh, tap anh goi -8,
 * dau dong cua hang 0.png / 02.bytes, nguhanh + wicon trong goi main). Bang ke du_lieu/bang_icon_thay_cu.csv:
 *  - moi tep trong bang ke co that va doc duoc; dong pmd:n trung DIEM ANH voi map/icon_pokemon/n.png;
 *  - moi tep trong cac thu muc o tren deu nam trong bang ke (khong sot tep cu nao);
 *  - 1344 huy hieu bang 12x12 khong trung nhau; 8 ky nang bang 16x16; 16 poster Tu binh 40x80;
 *  - goi main.sh cua jar = main.bytes cua Unity, co nguhanh 80x16 va wicon 25x25.
 */
public final class KiemIconThayCu {

    private static final byte[] MA = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};

    private KiemIconThayCu() {
    }

    public static void chay() {
        Kiem.nhom("IconThayCu - icon cu ghi de tai cho bang anh Pokemon (sinh_icon_thay_cu.py)");
        try {
            final List<String> dong = Files.readAllLines(new File("../du_lieu/bang_icon_thay_cu.csv").toPath(), StandardCharsets.UTF_8);
            final Map<String, String> ke = new HashMap<String, String>();
            for (final String d : dong.subList(1, dong.size())) {
                final int c = d.lastIndexOf(',');
                ke.put(d.substring(0, c), d.substring(c + 1));
            }
            Kiem.dung("bang ke co > 2600 tep (" + ke.size() + ")", ke.size() > 2600);

            final List<String> thieu = new ArrayList<String>();
            final List<String> lechPmd = new ArrayList<String>();
            final Map<Integer, int[]> pmd = new HashMap<Integer, int[]>();
            for (final Map.Entry<String, String> e : ke.entrySet()) {
                if (e.getKey().contains("#")) {
                    continue;
                }
                final BufferedImage a = docAnh(new File("../" + e.getKey()));
                if (a == null) {
                    thieu.add(e.getKey());
                    continue;
                }
                if (e.getValue().startsWith("pmd:")) {
                    final int n = Integer.parseInt(e.getValue().substring(4));
                    int[] p = pmd.get(Integer.valueOf(n));
                    if (p == null) {
                        final BufferedImage g = docAnh(new File("map/icon_pokemon/" + n + ".png"));
                        p = g == null ? new int[0] : diem(g);
                        pmd.put(Integer.valueOf(n), p);
                    }
                    if (!Arrays.equals(p, diem(a))) {
                        lechPmd.add(e.getKey());
                    }
                }
            }
            Kiem.bang("moi tep trong bang ke co that va doc duoc", "[]", thieu.toString());
            Kiem.bang("dong pmd:n trung diem anh map/icon_pokemon/n.png", "[]", rutGon(lechPmd));

            final List<String> sot = new ArrayList<String>();
            for (final String tm : new String[]{"icon", "images_potion", "images_gem", "images_animal", "iconquest",
                    "fruit", "iconclan", "iconskillclan", "tb"}) {
                final File[] fs = new File("map/" + tm).listFiles();
                for (final File f : fs == null ? new File[0] : fs) {
                    if (!ke.containsKey("server/map/" + tm + "/" + f.getName())) {
                        sot.add(tm + "/" + f.getName());
                    }
                }
            }
            Kiem.bang("khong con tep cu nao ngoai bang ke", "[]", rutGon(sot));

            final Set<String> clan = new HashSet<String>();
            int clanSai = 0;
            for (int i = 0; i < 1344; i++) {
                final BufferedImage a = docAnh(new File("map/iconclan/" + i + ".png"));
                if (a == null || a.getWidth() != 12 || a.getHeight() != 12) {
                    clanSai++;
                } else {
                    clan.add(Arrays.toString(diem(a)));
                }
            }
            Kiem.bang("1344 huy hieu bang 12x12", Integer.valueOf(0), Integer.valueOf(clanSai));
            Kiem.bang("huy hieu bang khong trung nhau", Integer.valueOf(1344), Integer.valueOf(clan.size()));
            Kiem.dung("Map.MAX_CLAN van la 1344", doc("src/real/Map.java").contains("MAX_CLAN = 1344;"));
            Kiem.dung("bang iconclan.png 120x144", kichThuoc("map/iconclan.png", 120, 144));
            boolean kn = true;
            for (int k = 0; k < 8; k++) {
                kn &= kichThuoc("map/iconskillclan/" + k + ".png", 16, 16);
            }
            Kiem.dung("8 ky nang bang 16x16", kn);
            boolean tb = true;
            for (int k = 0; k < 16; k++) {
                tb &= kichThuoc("map/tb/" + k + ".png", 40, 80);
            }
            Kiem.dung("16 poster Tu binh 40x80", tb);
            Kiem.dung("0.png cua hang 12x12", kichThuoc("../client_jar/res/0.png", 12, 12));
            Kiem.dung("02.bytes cua hang 12x12", kichThuoc("../client_pc_ios/Assets/Resources/res/02.bytes", 12, 12));

            final byte[] jar = Files.readAllBytes(new File("../client_jar/res/main.sh").toPath());
            final byte[] unity = Files.readAllBytes(new File("../client_pc_ios/Assets/Resources/res/main.bytes").toPath());
            Kiem.dung("main.sh (jar) = main.bytes (Unity)", Arrays.equals(jar, unity));
            final Map<String, byte[]> goi = docGoi(jar);
            Kiem.dung("goi main co nguhanh 80x16", kichThuoc(goi.get("nguhanh.png"), 80, 16));
            Kiem.dung("goi main co wicon 25x25", kichThuoc(goi.get("wicon.png"), 25, 25));
        } catch (final Exception e) {
            Kiem.dung("doc bang ke / anh: " + e, false);
        }
    }

    private static String rutGon(final List<String> ds) {
        return ds.size() <= 8 ? ds.toString() : ds.subList(0, 8) + " ... (" + ds.size() + ")";
    }

    private static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(new File(f).toPath()), StandardCharsets.UTF_8);
    }

    private static BufferedImage docAnh(final File f) {
        try {
            return f.isFile() ? ImageIO.read(f) : null;
        } catch (final Exception e) {
            return null;
        }
    }

    private static boolean kichThuoc(final String f, final int w, final int h) {
        final BufferedImage a = docAnh(new File(f));
        return a != null && a.getWidth() == w && a.getHeight() == h;
    }

    private static boolean kichThuoc(final byte[] b, final int w, final int h) {
        try {
            final BufferedImage a = b == null ? null : ImageIO.read(new ByteArrayInputStream(b));
            return a != null && a.getWidth() == w && a.getHeight() == h;
        } catch (final Exception e) {
            return false;
        }
    }

    /** ARGB, diem trong suot hoan toan ve 0 (so diem anh, khong so cach ma hoa PNG). */
    private static int[] diem(final BufferedImage a) {
        final int[] p = a.getRGB(0, 0, a.getWidth(), a.getHeight(), null, 0, a.getWidth());
        for (int i = 0; i < p.length; i++) {
            if ((p[i] >>> 24) == 0) {
                p[i] = 0;
            }
        }
        final int[] r = new int[p.length + 2];
        r[0] = a.getWidth();
        r[1] = a.getHeight();
        System.arraycopy(p, 0, r, 2, p.length);
        return r;
    }

    /** Goi .sh (FilePack client): u8 n, n x (u8 do dai ten, ten XOR, u16 do dai), du lieu XOR. */
    private static Map<String, byte[]> docGoi(final byte[] d) {
        final Map<String, byte[]> ra = new HashMap<String, byte[]>();
        int i = 1;
        final int n = d[0] & 255;
        final String[] ten = new String[n];
        final int[] dai = new int[n];
        for (int k = 0; k < n; k++) {
            final int ln = d[i++] & 255;
            final byte[] t = new byte[ln];
            for (int j = 0; j < ln; j++) {
                t[j] = (byte) (d[i + j] ^ MA[j % MA.length]);
            }
            i += ln;
            ten[k] = new String(t, StandardCharsets.ISO_8859_1);
            dai[k] = ((d[i] & 255) << 8) | (d[i + 1] & 255);
            i += 2;
        }
        int q = 0;
        for (int k = 0; k < n; k++) {
            final byte[] b = new byte[dai[k]];
            for (int j = 0; j < dai[k]; j++) {
                b[j] = (byte) (d[i + q + j] ^ MA[(q + j) % MA.length]);
            }
            q += dai[k];
            ra.put(ten[k], b);
        }
        return ra;
    }
}
