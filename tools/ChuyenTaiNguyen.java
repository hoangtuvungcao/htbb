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

    /**
     * Duoi tep cua ban J2ME can cat bo khi doi sang ten ma Unity tra cuu.
     *
     * PHAI DAY DU. Bo sot mot duoi la tai nguyen do "bien mat" doi voi ban Unity du no
     * co that: da tung sot ".wp", lam Res.GetWPSplashInfo nem NullReference vi no tim
     * res/wpsplash/0 trong khi tep duoc ghi ra thanh res/wpsplash/0.wp.bytes.
     * Toan bo duoi co mat trong ban J2ME: .png .sh .wps .wp .type .txt
     */
    private static final String[] DUOI_CAT = { ".png", ".sh", ".wps", ".wp", ".type", ".txt" };

    /** Duoi tep la goi FilePack (co the giai ra nhieu anh ben trong). */
    private static final String[] DUOI_GOI = { ".sh", ".wps", ".wp" };

    /** Bat chuoi dang: Main.res + "/ten/nao/do" */
    private static final Pattern MAU_CAN = Pattern.compile("Main\\.res\\s*\\+\\s*\"/([^\"]*)\"");

    /** Bat hang duong dan goi trong FilePack: public static string main = Main.res + "/main"; */
    private static final Pattern MAU_HANG =
            Pattern.compile("public\\s+static\\s+string\\s+(\\w+)\\s*=\\s*Main\\.res\\s*\\+\\s*\"/([^\"]*)\"");

    /** Bat cho nap goi: FilePack.init(FilePack.main) hoac FilePack.init(Main.res + "/other") */
    private static final Pattern MAU_NAP = Pattern.compile("FilePack\\.init\\(([^;]*?)\\)\\s*;");

    /** Bat ten anh viet thang: FilePack.getImg("bg_popup") */
    private static final Pattern MAU_ANH = Pattern.compile("getImg\\(\"([^\"]+)\"\\)");

    /** Bat: X.y = Image.createImage(Main.res + "/ten"); */
    private static final Pattern MAU_ANH_LE = Pattern.compile(
            "([A-Za-z0-9_.\\[\\]]+)\\s*=\\s*Image\\.createImage\\(Main\\.res\\s*\\+\\s*\"/([^\"]+)\"\\)");

    /** Bat phep so: tenTruong == null (ke ca dang mang: imgX[0] == null) */
    private static final Pattern MAU_SO_NULL =
            Pattern.compile("(\\w+)\\s*(?:\\[[^\\]]*\\])?\\s*==\\s*null");

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
            if (!laGoi(e.getValue().getFileName().toString())) { continue; }
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

        // 4d. Dung cac GOI ma nay nap nhung ban J2ME khong co.
        //     Khong co goi la client CHET, khong phai chi xau: Res.loadRes goi thang
        //     imgBgPopup.getWidth() ngay sau khi nap goi "other", nen anh null la nem
        //     NullReferenceException, va vi loadRes chay tu FixedUpdate nen loi lap lai
        //     30 lan moi giay. Dung goi thay the thi client khoi dong duoc de con thu.
        final int soDung = dungGoiThieu(maCs, ra, trongGoi, tepRoi);
        dungAnhThieu(maCs, ra);

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
     * Dat anh giu cho (1x1 trong suot) cho cac anh NAP LE ma ban J2ME khong co.
     *
     * VI SAO CAN: nhieu cho dung thang ket qua ma khong kiem null, vi du
     * GameScr.setDataForButton() goi Res.imgNewBt[4].getWidth(). Thieu anh la nem
     * NullReferenceException va client dung o do.
     *
     * VI SAO PHAI THAN TRONG: co 49 cho trong ma re nhanh theo "anh == null" - vi du
     * imgHorse == null nghia la "khong co ngua thi khoi ve". Dat anh giu cho vao nhung
     * cho do la doi hanh vi. Nen quy tac o day la: neu ten truong co xuat hien trong mot
     * phep so == null o BAT KY dau, thi KHONG dat anh giu cho, de nguyen null cho nhanh
     * du phong chay dung. Quy tac nay thua con hon thieu: bo sot mot anh dang le dat
     * duoc thi chi la thieu anh, con dat nham vao cho co nhanh null la doi logic.
     */
    private static int dungAnhThieu(final Path maCs, final Path ra) throws IOException {
        final List<Path> tep = new ArrayList<Path>();
        liet(maCs, tep, ".cs");

        final Map<String, String> anhVaTruong = new LinkedHashMap<String, String>();
        final Set<String> coNhanhNull = new TreeSet<String>();

        for (final Path p : tep) {
            final String noi = new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
            Matcher m = MAU_ANH_LE.matcher(noi);
            while (m.find()) {
                final String truong = m.group(1);
                anhVaTruong.put(m.group(2), truong.substring(truong.lastIndexOf('.') + 1));
            }
            m = MAU_SO_NULL.matcher(noi);
            while (m.find()) {
                coNhanhNull.add(m.group(1));
            }
        }

        int soDat = 0;
        int soBoQua = 0;
        for (final Map.Entry<String, String> e : anhVaTruong.entrySet()) {
            final Path dich = ra.resolve(e.getKey() + ".bytes");
            if (Files.exists(dich)) {
                continue;
            }
            if (coNhanhNull.contains(e.getValue())) {
                soBoQua++;
                continue;
            }
            ghi(dich, anhTrongSuot());
            soDat++;
        }
        if (soDat > 0 || soBoQua > 0) {
            System.out.println("  anh giu cho: dat " + soDat + ", bo qua " + soBoQua
                    + " (truong co nhanh re theo null)");
        }
        return soDat;
    }

    /**
     * Dung cac goi tai nguyen ma ma nguon co nap nhung ban J2ME khong co.
     *
     * Cach lam: doc tu ma nguon xem co nhung goi nao duoc nap va nhung ten anh nao duoc
     * lay ra tu goi, roi dung mot goi dung dinh dang FilePack chua tat ca cac ten do.
     * Moi ten lay anh THAT neu tim duoc o dau do trong ban J2ME; khong tim duoc thi dat
     * anh trong suot 1x1.
     *
     * Vi sao dung anh trong suot chu khong muon tam mot anh khac cho do trong: dat nham
     * anh la du lieu SAI, ma sai thi khong ai nhin ra. Trong suot thi ro rang la thieu.
     *
     * Goi dung ra chi de client KHOI DONG DUOC ma thu. No KHONG thay the duoc bo tai
     * nguyen that cua du an Unity goc.
     */
    private static int dungGoiThieu(final Path maCs, final Path ra,
                                    final Map<String, byte[]> trongGoi,
                                    final Map<String, Path> tepRoi) throws IOException {
        final List<Path> tep = new ArrayList<Path>();
        liet(maCs, tep, ".cs");

        final Map<String, String> hang = new LinkedHashMap<String, String>();
        final Set<String> tenAnh = new TreeSet<String>();
        final Set<String> goiCanNap = new TreeSet<String>();

        for (final Path p : tep) {
            final String noi = new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
            Matcher m = MAU_HANG.matcher(noi);
            while (m.find()) {
                hang.put(m.group(1), catDauGach(m.group(2)));
            }
            m = MAU_ANH.matcher(noi);
            while (m.find()) {
                tenAnh.add(m.group(1));
            }
        }
        for (final Path p : tep) {
            final String noi = new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
            final Matcher m = MAU_NAP.matcher(noi);
            while (m.find()) {
                final String duong = giaiBieuThucGoi(m.group(1).trim(), hang);
                if (duong != null) {
                    goiCanNap.add(duong);
                }
            }
        }

        if (tenAnh.isEmpty() || tenAnh.size() > 255) {
            System.out.println("Bo qua buoc dung goi: so ten anh = " + tenAnh.size()
                    + " (dinh dang goi chi chua toi da 255 muc).");
            return 0;
        }

        int soDung = 0;
        for (final String goi : goiCanNap) {
            final Path dich = ra.resolve(goi + ".bytes");
            if (Files.exists(dich)) {
                continue;
            }
            final Map<String, byte[]> muc = new LinkedHashMap<String, byte[]>();
            int soThat = 0;
            for (final String ten : tenAnh) {
                byte[] anh = trongGoi.get(ten);
                if (anh == null) {
                    anh = timTheoTenCuoi(ten, tepRoi, trongGoi);
                }
                if (anh != null) {
                    soThat++;
                } else {
                    anh = anhTrongSuot();
                }
                muc.put(ten + ".png", anh);
            }
            final byte[] goiMoi = dongGoi(muc);

            // Tu kiem ngay tren du lieu that: giai lai goi vua dong va doi chieu.
            // dongGoi la nghich dao viet tay cua giaiGoi; lech mot byte o phan dau la
            // client doc ra rac ma KHONG bao loi gi - chi la moi anh deu sai.
            final Map<String, byte[]> doc = giaiGoi(goiMoi);
            if (!doc.keySet().equals(muc.keySet())) {
                throw new IOException("Goi vua dong khong giai lai dung: " + goi
                        + " (dong " + muc.size() + " muc, giai ra " + doc.size() + ")");
            }
            for (final Map.Entry<String, byte[]> e : muc.entrySet()) {
                if (!java.util.Arrays.equals(e.getValue(), doc.get(e.getKey()))) {
                    throw new IOException("Goi vua dong sai du lieu o muc: " + e.getKey());
                }
            }

            ghi(dich, goiMoi);
            soDung++;
            System.out.println("  dung goi thay the: " + goi + " (" + muc.size() + " muc, "
                    + soThat + " anh that, " + (muc.size() - soThat) + " anh trong suot)");
        }
        return soDung;
    }

    /** Doi FilePack.main hoac Main.res + "/other" thanh duong dan trong res/. */
    private static String giaiBieuThucGoi(final String bieuThuc, final Map<String, String> hang) {
        final Matcher m = MAU_CAN.matcher(bieuThuc);
        if (m.find() && bieuThuc.indexOf('+', m.end()) < 0) {
            return catDauGach(m.group(1));
        }
        if (bieuThuc.startsWith("FilePack.")) {
            final String ten = bieuThuc.substring("FilePack.".length());
            // Chi nhan ten hang tron. Bieu thuc ghep (vi du wps + array[i] + "/" + j)
            // tro toi nhieu goi khac nhau, khong the dung san duoc.
            if (ten.matches("\\w+")) {
                return hang.get(ten);
            }
        }
        return null;
    }

    private static String catDauGach(final String s) {
        return s.startsWith("/") ? s.substring(1) : s;
    }

    /** Anh PNG 1x1 hoan toan trong suot. */
    private static byte[] anhTrongSuot() throws IOException {
        final java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", bos);
        return bos.toByteArray();
    }

    /** Nghich dao cua giaiGoi: dong cac muc thanh mot goi dung dinh dang FilePack. */
    private static byte[] dongGoi(final Map<String, byte[]> muc) throws IOException {
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        final java.io.DataOutputStream out = new java.io.DataOutputStream(bos);
        out.writeByte(muc.size());
        for (final Map.Entry<String, byte[]> e : muc.entrySet()) {
            final byte[] ten = e.getKey().getBytes(Charset.forName("US-ASCII"));
            if (ten.length > 120) {
                throw new IOException("Ten qua dai: " + e.getKey());
            }
            if (e.getValue().length > 65535) {
                throw new IOException("Anh qua lon: " + e.getKey());
            }
            out.writeByte(ten.length);
            final byte[] tenXao = ten.clone();
            xor(tenXao);                            // ten bi XOR, chi so tinh lai tu 0
            out.write(tenXao);
            out.writeShort(e.getValue().length);    // so nguyen KHONG bi xao tron
        }
        // Toan bo du lieu noi lien nhau, XOR mot lan voi chi so bat dau tu 0.
        final java.io.ByteArrayOutputStream duLieu = new java.io.ByteArrayOutputStream();
        for (final byte[] v : muc.values()) {
            duLieu.write(v);
        }
        final byte[] tatCa = duLieu.toByteArray();
        xor(tatCa);
        out.write(tatCa);
        out.flush();
        return bos.toByteArray();
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

    private static boolean laGoi(final String ten) {
        for (final String d : DUOI_GOI) {
            if (ten.endsWith(d)) { return true; }
        }
        return false;
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
