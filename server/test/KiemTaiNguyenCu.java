import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * P2 - khong con hinh may chu cu (KPAH / NQSH kiem hiep) trong tai nguyen phat hanh.
 *
 * Ban Java cua phan 1 trong tools/pokemon/kiem_tai_nguyen_cu.py (cong cu day du: ca danh sach id tham chieu
 * va kiem thieu tep). du_lieu/tai_nguyen_cu.txt = dau van tay 16 hex SHA-1 cua moi tep / anh trong goi cua may
 * chu cu (sinh bang "kiem_tai_nguyen_cu.py --sinh" tu lich su git). Moi tep hinh (PNG, dinh dang "c", goi
 * FilePack) trong cac thu muc phat hanh ma trung dau van tay -> HONG, tru khi khop mot dong cua
 * du_lieu/tai_nguyen_cu_giu.csv ("tep" hoac "tep#anh-trong-goi", glob * ? [..]).
 */
public final class KiemTaiNguyenCu {

    private static final String[] THU_MUC = {"client_jar/res", "client_jar/res_nhe", "client_jar/shared/brand",
        "client_jar/shared/banner", "client_pc_ios/Assets/Resources", "client_pc_ios/Assets/Icon",
        "client_apk/pokemon/src/main/res", "client_pc/res", "server/map", "web/public"};

    private static final byte[] CODE = {78, 103, 117, 121, 101, 110, 86, 97, 110, 77, 105, 110, 104};

    private static Set<String> cu;
    private static List<Pattern> giu;
    private static int soTep;
    private static int soGiu;
    private static final List<String> SAI = new ArrayList<>();

    private KiemTaiNguyenCu() {
    }

    public static void chay() {
        Kiem.nhom("Tai nguyen P2 - khong con hinh may chu cu (du_lieu/tai_nguyen_cu.txt, giu co ly do: tai_nguyen_cu_giu.csv)");
        try {
            cu = new HashSet<>();
            for (final String d : Files.readAllLines(new File("../du_lieu/tai_nguyen_cu.txt").toPath(), StandardCharsets.UTF_8)) {
                if (!d.startsWith("#") && !d.trim().isEmpty()) {
                    cu.add(d.trim());
                }
            }
            giu = new ArrayList<>();
            final List<String> dong = Files.readAllLines(new File("../du_lieu/tai_nguyen_cu_giu.csv").toPath(), StandardCharsets.UTF_8);
            for (final String d : dong.subList(1, dong.size())) {
                giu.add(glob(d.substring(0, d.indexOf(','))));
            }
            Kiem.dung("danh sach dau van tay cu du lon (" + cu.size() + ")", cu.size() > 5000);
            soTep = 0;
            soGiu = 0;
            SAI.clear();
            for (final String tm : THU_MUC) {
                duyet(new File("../" + tm), tm);
            }
            Kiem.dung("da kiem du tep hinh (" + soTep + ")", soTep > 5000);
            System.out.println("    giu co ly do: " + soGiu + " tep / anh");
            for (int i = 0; i < SAI.size() && i < 30; i++) {
                System.out.println("    HINH CU: " + SAI.get(i));
            }
            Kiem.bang("so tep con hinh may chu cu (khong co dong giu)", Integer.valueOf(0), Integer.valueOf(SAI.size()));
            Kiem.dung("cong cu kiem day du co mat", new File("../tools/pokemon/kiem_tai_nguyen_cu.py").isFile());
        } catch (final Exception e) {
            Kiem.dung("kiem tai nguyen cu: " + e, false);
        }
    }

    private static Pattern glob(final String g) {
        final StringBuilder b = new StringBuilder();
        for (final char c : g.toCharArray()) {
            if (c == '*') {
                b.append(".*");
            } else if (c == '?') {
                b.append('.');
            } else if (c == '[' || c == ']' || c == '-') {
                b.append(c);
            } else {
                b.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return Pattern.compile(b.toString());
    }

    private static boolean khop(final String s) {
        for (final Pattern p : giu) {
            if (p.matcher(s).matches()) {
                return true;
            }
        }
        return false;
    }

    private static void duyet(final File f, final String rel) throws Exception {
        if (f.isDirectory()) {
            final String[] con = f.list();
            if (con == null) {
                return;
            }
            java.util.Arrays.sort(con);
            for (final String c : con) {
                duyet(new File(f, c), rel + "/" + c);
            }
            return;
        }
        if (rel.endsWith(".meta")) {
            return;
        }
        final byte[] b = Files.readAllBytes(f.toPath());
        final List<Object[]> khoi = anh(b);
        if (khoi.isEmpty()) {
            return;
        }
        soTep++;
        final boolean caTep = cu.contains(dauBlob(b));
        final List<String> cuTrong = new ArrayList<>();
        for (final Object[] k : khoi) {
            if (cu.contains(dau((byte[]) k[1]))) {
                cuTrong.add((String) k[0]);
            }
        }
        if (!caTep && cuTrong.isEmpty()) {
            return;
        }
        if (khop(rel)) {
            soGiu++;
            return;
        }
        if (caTep && khoi.size() == 1) {
            SAI.add(rel);
            return;
        }
        final List<String> ds = cuTrong.isEmpty() ? new ArrayList<String>() : cuTrong;
        if (ds.isEmpty()) {
            for (final Object[] k : khoi) {
                ds.add((String) k[0]);
            }
        }
        final List<String> sai = new ArrayList<>();
        for (final String k : ds) {
            if (khop(rel + "#" + k)) {
                soGiu++;
            } else {
                sai.add(k);
            }
        }
        if (!sai.isEmpty()) {
            SAI.add(rel + " " + sai);
        }
    }

    /** Cac khoi hinh trong tep: {ten, byte[]}; rong = khong phai tep hinh. Giong anh_trong() cua ban Python. */
    private static List<Object[]> anh(final byte[] b) {
        final List<Object[]> kq = new ArrayList<>();
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            kq.add(new Object[]{"", b});
            return kq;
        }
        if (b.length > 8 && (b[2] & 0xFF) == 0x89 && b[3] == 'P' && b[4] == 'N' && b[5] == 'G') {
            final int n = ((b[0] & 0xFF) << 8) | (b[1] & 0xFF);
            kq.add(new Object[]{"c", java.util.Arrays.copyOfRange(b, 2, Math.min(b.length, 2 + n))});
            return kq;
        }
        final List<Object[]> g = goi(b);
        if (g == null) {
            return kq;
        }
        byte[] data = null;
        final List<String> hs = new ArrayList<>();
        for (final Object[] e : g) {
            final String t = (String) e[0];
            final byte[] x = (byte[]) e[1];
            if (t.endsWith(".png") || (x.length > 4 && (x[0] & 0xFF) == 0x89 && x[1] == 'P')) {
                kq.add(e);
            }
            if (t.endsWith("_h")) {
                hs.add(t);
            }
            if (t.equals("data")) {
                data = x;
            }
        }
        if (data != null) {
            java.util.Collections.sort(hs);
            for (final String h : hs) {
                for (final Object[] e : g) {
                    if (h.equals(e[0])) {
                        final byte[] x = (byte[]) e[1];
                        final byte[] tong = new byte[x.length + data.length];
                        System.arraycopy(x, 0, tong, 0, x.length);
                        System.arraycopy(data, 0, tong, x.length, data.length);
                        kq.add(new Object[]{h, tong});
                    }
                }
            }
        }
        return kq;
    }

    /** FilePack (xem client_jar FilePack): u8 n, n x (u8 len, ten xor, u16 do dai), du lieu xor; null neu khong phai. */
    private static List<Object[]> goi(final byte[] b) {
        try {
            int i = 0;
            final int n = b[i++] & 0xFF;
            if (n == 0) {
                return null;
            }
            final String[] ten = new String[n];
            final int[] dai = new int[n];
            int tong = 0;
            for (int k = 0; k < n; k++) {
                final int ln = b[i++] & 0xFF;
                final byte[] t = new byte[ln];
                for (int j = 0; j < ln; j++) {
                    t[j] = (byte) (b[i + j] ^ CODE[j % CODE.length]);
                }
                i += ln;
                ten[k] = new String(t, StandardCharsets.ISO_8859_1);
                if (!ten[k].matches("[\\w./ -]+")) {
                    return null;
                }
                dai[k] = ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
                i += 2;
                tong += dai[k];
            }
            if (i + tong != b.length) {
                return null;
            }
            final List<Object[]> kq = new ArrayList<>();
            int q = 0;
            for (int k = 0; k < n; k++) {
                final byte[] x = new byte[dai[k]];
                for (int j = 0; j < dai[k]; j++) {
                    x[j] = (byte) (b[i + q + j] ^ CODE[(q + j) % CODE.length]);
                }
                q += dai[k];
                kq.add(new Object[]{ten[k], x});
            }
            return kq;
        } catch (final RuntimeException e) {
            return null;
        }
    }

    private static String hex16(final byte[] h) {
        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            s.append(String.format("%02x", Integer.valueOf(h[i] & 0xFF)));
        }
        return s.toString();
    }

    private static String dau(final byte[] b) throws Exception {
        return hex16(MessageDigest.getInstance("SHA-1").digest(b));
    }

    private static String dauBlob(final byte[] b) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(("blob " + b.length + "\0").getBytes(StandardCharsets.ISO_8859_1));
        return hex16(md.digest(b));
    }
}
