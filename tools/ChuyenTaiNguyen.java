import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chuyen tai nguyen tu client J2ME sang bo cuc ma ban Unity doc duoc.
 *
 * VI SAO CAN: hai ban client dung chung mot bo tai nguyen nhung dong goi khac nhau.
 *   - J2ME  : doc thang tu trong .jar, ten giu nguyen duoi (main.sh, logo.png, c/body/3).
 *   - Unity : doc qua Resources.Load(ten, typeof(TextAsset)) roi lay .bytes. Unity CHI
 *             nhan tep lam TextAsset khi tep co duoi .bytes, va ten dung de tra cuu la
 *             duong dan KHONG co duoi. Nen "res/logo" phai la tep
 *             Assets/Resources/res/logo.bytes.
 *
 * Mot so anh ma Unity nap le (vi du res/bar) lai nam BEN TRONG goi .sh cua ban J2ME,
 * nen cong cu nay vua sao chep tep roi, vua giai goi de lay anh ben trong ra.
 *
 * Danh sach tai nguyen can KHONG viet cung o day ma doc thang tu ma C#, de khi ma doi
 * thi bao cao thieu/du van dung.
 *
 * Cach dung:
 *   java ChuyenTaiNguyen thu-muc-J2ME thu-muc-ma-Csharp thu-muc-ra
 */
public final class ChuyenTaiNguyen {

    /** Khoa xao tron cua dinh dang goi FilePack. Xem FilePack.encode(sbyte[]). */
    private static final byte[] KHOA = "NguyenVanMinh".getBytes(Charset.forName("US-ASCII"));

    /** Duoi tep cua ban J2ME can cat bo khi doi sang ten ma Unity tra cuu. */
    private static final String[] DUOI_CAT = { ".png", ".sh", ".type", ".txt" };

    /** Bat chuoi dang: Main.res + "/ten/nao/do" */
    private static final Pattern MAU_CAN = Pattern.compile("Main\\.res\\s*\\+\\s*\"/([^\"]*)\"");

    public static void main(final String[] thamSo) throws Exception {
        if (thamSo.length < 3) {
            System.out.println("Dung: java ChuyenTaiNguyen <thu-muc-J2ME> <thu-muc-ma-C#> <thu-muc-ra>");
            System.exit(2);
        }
        final Path goc = Paths.get(thamSo[0]);
        final Path maCs = Paths.get(thamSo[1]);
        final Path ra = Paths.get(thamSo[2]);

        if (!Files.isDirectory(goc)) { thoat("Khong thay thu muc J2ME: " + goc); }
        if (!Files.isDirectory(maCs)) { thoat("Khong thay thu muc ma C#: " + maCs); }

        // 1. Tap tai nguyen ma Unity yeu cau, doc tu chinh ma nguon.
        final Set<String> can = docTaiNguyenCan(maCs);
        System.out.println("Ma C# yeu cau " + can.size() + " tai nguyen.");

        // 2. Tep roi cua ban J2ME.
        final Map<String, Path> tepRoi = quetTepRoi(goc);
        System.out.println("Ban J2ME co " + tepRoi.size() + " tep tai nguyen.");

        // 3. Anh nam ben trong cac goi .sh.
        final Map<String, byte[]> trongGoi = new LinkedHashMap<String, byte[]>();
        int soGoi = 0;
        for (final Map.Entry<String, Path> e : tepRoi.entrySet()) {
            if (!e.getValue().getFileName().toString().endsWith(".sh")) { continue; }
            final Map<String, byte[]> phan = giaiGoi(Files.readAllBytes(e.getValue()));
            if (phan.isEmpty()) { continue; }
            soGoi++;
            for (final Map.Entry<String, byte[]> p : phan.entrySet()) {
                // Trong goi ten co duoi .png; ten Unity tra cuu thi khong.
                trongGoi.put(catDuoi(p.getKey()), p.getValue());
            }
        }
        System.out.println("Giai " + soGoi + " goi, lay them " + trongGoi.size() + " anh ben trong.");

        // 4. Ghi ra.
        Files.createDirectories(ra);
        int soGhi = 0;

        // 4a. Guong toan bo cay tep roi (giu duong dan, doi duoi thanh .bytes).
        for (final Map.Entry<String, Path> e : tepRoi.entrySet()) {
            ghi(ra.resolve(catDuoi(e.getKey()) + ".bytes"), Files.readAllBytes(e.getValue()));
            soGhi++;
        }

        // 4b. Anh lay tu trong goi, dat o goc res/ vi ma goi Image.createImage(res + "/bar").
        for (final Map.Entry<String, byte[]> e : trongGoi.entrySet()) {
            final Path dich = ra.resolve(e.getKey() + ".bytes");
            if (!Files.exists(dich)) { ghi(dich, e.getValue()); soGhi++; }
        }

        // 4c. Ma C# xep mot so anh vao thu muc con (png/, pngtile/, gc/) trong khi ban
        //     J2ME de o goc. Suy ra tu chinh danh sach can, khong viet cung anh xa.
        int soChep = 0;
        for (final String ten : can) {
            if (ten.endsWith("/") || ten.indexOf('/') < 0) { continue; }
            final Path dich = ra.resolve(ten + ".bytes");
            if (Files.exists(dich)) { continue; }
            final String cuoi = ten.substring(ten.lastIndexOf('/') + 1);
            final byte[] du = timTheoTenCuoi(cuoi, tepRoi, trongGoi);
            if (du != null) { ghi(dich, du); soGhi++; soChep++; }
        }
        System.out.println("Ghi " + soGhi + " tep .bytes (trong do " + soChep
                + " tep chep sang thu muc con theo yeu cau cua ma).");

        // 5. Bao cao thieu.
        final List<String> thieu = new ArrayList<String>();
        for (final String ten : can) {
            if (ten.endsWith("/")) {
                if (!Files.isDirectory(ra.resolve(ten.substring(0, ten.length() - 1)))) {
                    thieu.add(ten);
                }
            } else if (!Files.exists(ra.resolve(ten + ".bytes"))) {
                thieu.add(ten);
            }
        }
        Collections.sort(thieu);
        System.out.println();
        System.out.println("=== KET QUA ===");
        System.out.println("  du   : " + (can.size() - thieu.size()) + "/" + can.size());
        System.out.println("  thieu: " + thieu.size());
        if (!thieu.isEmpty()) {
            System.out.println();
            System.out.println("Cac tai nguyen ban J2ME khong co (ban Unity dung bo moi hon):");
            final StringBuilder sb = new StringBuilder("  ");
            for (final String t : thieu) {
                if (sb.length() > 90) { System.out.println(sb); sb.setLength(0); sb.append("  "); }
                sb.append(t).append(' ');
            }
            if (sb.length() > 2) { System.out.println(sb); }
        }
    }

    // ------------------------------------------------------------------
    /**
     * Tim du lieu cho mot tai nguyen ma ma C# xep trong thu muc con, vi du "png/logo".
     *
     * CHI chap nhan hai nguon: tep nam NGAY GOC cua ban J2ME, hoac anh ben trong goi.
     * KHONG duoc do theo ten cuoi tren khap cay thu muc: ban J2ME co c/body/42 va
     * c/leg/42, nen "newEf/42" se vo tinh nhan duoc anh than nhan vat. Do la du lieu
     * SAI chu khong phai thieu - thieu thi con thay duoc, sai thi khong.
     */
    private static byte[] timTheoTenCuoi(final String cuoi, final Map<String, Path> tepRoi,
                                         final Map<String, byte[]> trongGoi) throws IOException {
        for (final Map.Entry<String, Path> e : tepRoi.entrySet()) {
            if (e.getKey().indexOf('/') >= 0) { continue; }
            if (catDuoi(e.getKey()).equals(cuoi)) { return Files.readAllBytes(e.getValue()); }
        }
        return trongGoi.get(cuoi);
    }

    private static Set<String> docTaiNguyenCan(final Path maCs) throws IOException {
        final Set<String> can = new TreeSet<String>();
        final List<Path> tep = new ArrayList<Path>();
        liet(maCs, tep, ".cs");
        for (final Path p : tep) {
            final String noi = new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
            final Matcher m = MAU_CAN.matcher(noi);
            while (m.find()) {
                final String t = m.group(1);
                if (!t.isEmpty()) { can.add(t); }
            }
        }
        return can;
    }

    private static Map<String, Path> quetTepRoi(final Path goc) throws IOException {
        final List<Path> tep = new ArrayList<Path>();
        liet(goc, tep, null);
        final Map<String, Path> ra = new LinkedHashMap<String, Path>();
        for (final Path p : tep) {
            if (p.getFileName().toString().endsWith(".class")) { continue; }
            final String tuongDoi = goc.relativize(p).toString().replace(File.separatorChar, '/');
            if (tuongDoi.startsWith("META-INF/")) { continue; }
            ra.put(tuongDoi, p);
        }
        return ra;
    }

    private static void liet(final Path thuMuc, final List<Path> gom, final String duoi) throws IOException {
        final File[] con = thuMuc.toFile().listFiles();
        if (con == null) { return; }
        for (final File f : con) {
            if (f.isDirectory()) { liet(f.toPath(), gom, duoi); }
            else if (duoi == null || f.getName().endsWith(duoi)) { gom.add(f.toPath()); }
        }
    }

    /**
     * Giai goi FilePack. Bo cuc: 1 byte so tep, roi voi moi tep
     * [1 byte do dai ten][ten da XOR][2 byte do dai du lieu], cuoi cung la toan bo du
     * lieu noi lien nhau va cung da XOR. So nguyen KHONG bi xao tron.
     * Tra ve rong neu tep khong phai goi (vi du cac tep .type la du lieu tho).
     */
    private static Map<String, byte[]> giaiGoi(final byte[] tho) {
        final Map<String, byte[]> ra = new LinkedHashMap<String, byte[]>();
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(tho));
        try {
            final int soTep = in.readUnsignedByte();
            if (soTep <= 0) { return ra; }
            final String[] ten = new String[soTep];
            final int[] dai = new int[soTep];
            int tong = 0;
            for (int i = 0; i < soTep; i++) {
                final int nLen = in.readByte();
                if (nLen <= 0 || nLen > 200) { return Collections.emptyMap(); }
                final byte[] t = new byte[nLen];
                in.readFully(t);
                xor(t);
                ten[i] = new String(t, Charset.forName("US-ASCII"));
                dai[i] = in.readUnsignedShort();
                tong += dai[i];
            }
            final byte[] duLieu = new byte[tong];
            in.readFully(duLieu);
            xor(duLieu);
            int vt = 0;
            for (int i = 0; i < soTep; i++) {
                final byte[] mot = new byte[dai[i]];
                System.arraycopy(duLieu, vt, mot, 0, dai[i]);
                vt += dai[i];
                ra.put(ten[i], mot);
            }
        } catch (final Exception ex) {
            return Collections.emptyMap();
        }
        return ra;
    }

    private static void xor(final byte[] b) {
        for (int i = 0; i < b.length; i++) { b[i] ^= KHOA[i % KHOA.length]; }
    }

    private static String catDuoi(final String ten) {
        for (final String d : DUOI_CAT) {
            if (ten.endsWith(d)) { return ten.substring(0, ten.length() - d.length()); }
        }
        return ten;
    }

    private static void ghi(final Path dich, final byte[] du) throws IOException {
        final Path cha = dich.getParent();
        if (cha != null) { Files.createDirectories(cha); }
        Files.write(dich, du);
    }

    private static void thoat(final String thongDiep) {
        System.out.println("LOI: " + thongDiep);
        System.exit(2);
    }

    private ChuyenTaiNguyen() {
    }
}
