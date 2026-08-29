import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Rut tai nguyen THAT ra tu ban Unity da dung san (KPAH_276_Data/).
 *
 * VI SAO CAN: ban Unity goc dung mot bo tai nguyen RIENG, khong phai bo cua ban J2ME.
 * Khi phai dung tam bo J2ME thi thieu 15 tai nguyen va 45 anh; te hon, nhung anh co
 * that lai la ban KHAC KICH THUOC voi ban ma nguon duoc viet cho - khung "info" cua
 * J2ME cao 34 trong khi ma ve thanh mau o y 23 va 32, nen hai vet den "phan mau da mat"
 * roi han xuong bai co ben duoi khung. Lay dung bo goc thi moi con so trong ma nguon
 * deu tro lai dung, khong phai chinh tay cai nao.
 *
 * HAI TEP, MOI TEP MOT VIEC:
 *   - mainData         : chua ResourceManager, tuc bang "duong dan -> doi tuong".
 *                        Khong co bang nay thi 366 tai nguyen deu mat duong dan, ma ten
 *                        tran thi dung nhau (co rat nhieu tep ten "0", "1", "2" o cac
 *                        thu muc khac nhau) - rut ra chi con 150 tep, sai 216 cai.
 *   - resources.assets : chua noi dung that cua tung tai nguyen.
 *
 * DINH DANG: SerializedFile cua Unity 5 (phien ban 15).
 *   dau tep (big-endian): metadataSize, fileSize, version, dataOffset, thu tu byte
 *   metadata (little-endian khi thu tu byte = 0):
 *       chuoi phien ban Unity ket thuc bang 0
 *       int32 nen tang
 *       bool  co cay kieu       (o hai tep nay = false, nen khoi phai doc cay kieu)
 *       int32 so kieu; moi kieu: int32 classID [+16 byte scriptID neu classID < 0]
 *                                + 16 byte ma bam kieu
 *       int32 so doi tuong; moi cai: can le 4, int64 pathID, uint32 batDau,
 *                                uint32 doDai, int32 classID, uint16 classID,
 *                                uint16 "da huy", 1 byte "bi luoc"
 *       int32 so kieu kich ban; moi cai: int32 chi so tep, can le, int64 dinh danh
 *       int32 so tep ngoai; moi cai: chuoi rong, 16 byte guid, int32 kieu, chuoi ten tep
 *
 * Cach dung:
 *   java RutTaiNguyenUnity thu-muc-KPAH_276_Data thu-muc-ra
 */
public final class RutTaiNguyenUnity {

    private static final int LOP_TEXT_ASSET = 49;
    private static final int LOP_QUAN_LY_TAI_NGUYEN = 147;

    // --- mot tep .assets da doc xong phan dau -------------------------------

    private static final class TepAssets {
        String ten;
        byte[] du;
        int vt;
        final Map<Long, DoiTuong> theoPathID = new LinkedHashMap<Long, DoiTuong>();
        final List<DoiTuong> tatCa = new ArrayList<DoiTuong>();
        final List<String> tepNgoai = new ArrayList<String>();

        int docI32Lon() {
            final int v = ((du[vt] & 0xFF) << 24) | ((du[vt + 1] & 0xFF) << 16)
                    | ((du[vt + 2] & 0xFF) << 8) | (du[vt + 3] & 0xFF);
            vt += 4;
            return v;
        }

        int docI32() {
            final int v = ByteBuffer.wrap(du, vt, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            vt += 4;
            return v;
        }

        long docI64() {
            final long v = ByteBuffer.wrap(du, vt, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
            vt += 8;
            return v;
        }

        void canLe() {
            vt = (vt + 3) & ~3;
        }

        String docChuoiDenSo0() {
            final int dau = vt;
            while (du[vt] != 0) {
                vt++;
            }
            final String s = new String(du, dau, vt - dau, Charset.forName("US-ASCII"));
            vt++;
            return s;
        }

        /** Chuoi trong Unity: int32 do dai + byte noi dung, roi can le 4. */
        String docChuoi() {
            final int n = docI32();
            if (n < 0 || n > 4096) {
                throw new IllegalStateException("do dai chuoi la: " + n);
            }
            final String s = new String(du, vt, n, Charset.forName("UTF-8"));
            vt += n;
            canLe();
            return s;
        }

        byte[] docMangByte() {
            final int n = docI32();
            if (n < 0 || vt + n > du.length) {
                throw new IllegalStateException("do dai mang la: " + n);
            }
            final byte[] b = new byte[n];
            System.arraycopy(du, vt, b, 0, n);
            vt += n;
            canLe();
            return b;
        }
    }

    private static final class DoiTuong {
        long pathID;
        int batDau;
        int lop;
    }

    /** Muc trong bang duong dan cua ResourceManager. */
    private static final class Muc {
        String duong;
        int chiSoTep;
        long pathID;
    }

    private static TepAssets doc(final Path tep) throws Exception {
        final TepAssets t = new TepAssets();
        t.ten = tep.getFileName().toString();
        t.du = Files.readAllBytes(tep);
        t.vt = 0;
        t.docI32Lon();
        t.docI32Lon();
        final int phienBan = t.docI32Lon();
        final int mocDuLieu = t.docI32Lon();
        final int thuTuByte = t.du[t.vt] & 0xFF;
        t.vt += 4;
        if (phienBan != 15) {
            throw new IllegalStateException(t.ten + ": chi doc duoc SerializedFile phien ban 15,"
                    + " tep nay la " + phienBan);
        }
        if (thuTuByte != 0) {
            throw new IllegalStateException(t.ten + ": tep ghi kieu big-endian, chua doc duoc.");
        }

        t.docChuoiDenSo0();
        t.docI32();
        final boolean coCayKieu = t.du[t.vt] != 0;
        t.vt++;
        if (coCayKieu) {
            throw new IllegalStateException(t.ten + ": tep co cay kieu, chua doc duoc.");
        }

        final int soKieu = t.docI32();
        for (int i = 0; i < soKieu; i++) {
            final int lop = t.docI32();
            if (lop < 0) {
                t.vt += 16;
            }
            t.vt += 16;
        }

        final int soDoiTuong = t.docI32();
        for (int i = 0; i < soDoiTuong; i++) {
            t.canLe();
            final DoiTuong o = new DoiTuong();
            o.pathID = t.docI64();
            o.batDau = t.docI32() + mocDuLieu;
            t.docI32();
            // O phien ban duoi 16, truong nay CHINH LA classID chu khong phai chi so vao
            // bang kieu, va sau no con uint16 classID + uint16 "da huy" + 1 byte "bi luoc".
            // Doc thieu ba truong do la lech het bang: da bi mot lan, ca 384 doi tuong
            // deu ra lop -1 hoac 21.
            o.lop = t.docI32();
            t.vt += 5;
            t.theoPathID.put(Long.valueOf(o.pathID), o);
            t.tatCa.add(o);
        }

        final int soKichBan = t.docI32();
        for (int i = 0; i < soKichBan; i++) {
            t.docI32();
            t.canLe();
            t.docI64();
        }

        final int soTepNgoai = t.docI32();
        for (int i = 0; i < soTepNgoai; i++) {
            t.docChuoiDenSo0();
            t.vt += 16;
            t.docI32();
            t.tepNgoai.add(t.docChuoiDenSo0());
        }
        return t;
    }

    /** Bat chuoi dang: Main.res + "/ten/nao/do" */
    private static final java.util.regex.Pattern MAU_CAN =
            java.util.regex.Pattern.compile("Main\\.res\\s*\\+\\s*\"/([^\"]*)\"");

    /**
     * Doc tu chinh ma C# xem no hoi xin nhung ten nao, VA VIET HOA THE NAO.
     *
     * VI SAO CAN: ResourceManager cua Unity luu duong dan da ha het ve chu thuong, nen
     * rut ra se duoc "res/newef/42" trong khi ma nguon hoi "res/newEf/42". Neu cu de
     * vay thi tren may Windows co the van chay (he tep khong phan biet hoa thuong)
     * nhung tren Linux/WebGL thi mat sach 31 anh hieu ung. Lay dung cach viet tu ma
     * nguon thi khong phai doan, va ma nguon doi ten luc nao cong cu cung theo kip.
     */
    private static Map<String, String> docCachViet(final Path maCs) throws Exception {
        final Map<String, String> chuanHoa = new LinkedHashMap<String, String>();
        final List<Path> tep = new ArrayList<Path>();
        Files.walk(maCs).filter(new java.util.function.Predicate<Path>() {
            public boolean test(final Path p) {
                return p.toString().endsWith(".cs");
            }
        }).forEach(new java.util.function.Consumer<Path>() {
            public void accept(final Path p) {
                tep.add(p);
            }
        });
        for (final Path p : tep) {
            final String noi = new String(Files.readAllBytes(p), Charset.forName("UTF-8"));
            final java.util.regex.Matcher m = MAU_CAN.matcher(noi);
            while (m.find()) {
                final String ten = m.group(1);
                if (ten.length() > 0) {
                    chuanHoa.put(ten.toLowerCase(java.util.Locale.ROOT), ten);
                }
            }
        }
        return chuanHoa;
    }

    public static void main(final String[] thamSo) throws Exception {
        if (thamSo.length < 2) {
            System.out.println("Dung: java RutTaiNguyenUnity thu-muc-KPAH_276_Data thu-muc-ra"
                    + " [thu-muc-ma-C#]");
            System.exit(2);
        }
        final Path goc = Paths.get(thamSo[0]);
        final Path ra = Paths.get(thamSo[1]);
        final Map<String, String> chuanHoa = (thamSo.length > 2)
                ? docCachViet(Paths.get(thamSo[2])) : new LinkedHashMap<String, String>();
        if (!chuanHoa.isEmpty()) {
            System.out.println("Ma C# hoi xin " + chuanHoa.size() + " ten tai nguyen.");
        }

        final TepAssets chinh = doc(goc.resolve("mainData"));
        System.out.println("mainData: " + chinh.tatCa.size() + " doi tuong, "
                + chinh.tepNgoai.size() + " tep ngoai " + chinh.tepNgoai);

        // Doc moi tep .assets nam canh, de con tra pathID.
        final Map<String, TepAssets> kho = new LinkedHashMap<String, TepAssets>();
        kho.put("mainData", chinh);
        for (final String ten : new String[] { "resources.assets", "sharedassets0.assets" }) {
            final Path p = goc.resolve(ten);
            if (Files.exists(p)) {
                final TepAssets t = doc(p);
                kho.put(ten, t);
                System.out.println(ten + ": " + t.tatCa.size() + " doi tuong.");
            }
        }

        // Bang duong dan cua ResourceManager.
        final List<Muc> bang = new ArrayList<Muc>();
        for (final DoiTuong o : chinh.tatCa) {
            if (o.lop != LOP_QUAN_LY_TAI_NGUYEN) {
                continue;
            }
            chinh.vt = o.batDau;
            final int soMuc = chinh.docI32();
            for (int i = 0; i < soMuc; i++) {
                final Muc m = new Muc();
                m.duong = chinh.docChuoi();
                m.chiSoTep = chinh.docI32();
                m.pathID = chinh.docI64();
                bang.add(m);
            }
        }
        System.out.println("ResourceManager: " + bang.size() + " duong dan.");
        if (bang.isEmpty()) {
            System.out.println("LOI: khong tim thay bang duong dan, dung lai de khoi ghi sai.");
            System.exit(2);
        }

        int soGhi = 0;
        int soKhongPhaiText = 0;
        int soKhongThay = 0;
        final Map<String, Integer> demLop = new TreeMap<String, Integer>();
        for (final Muc m : bang) {
            // chiSoTep 0 la chinh tep nay; lon hon 0 la tep thu (chiSoTep - 1) trong
            // danh sach tep ngoai.
            String tenTep = "mainData";
            if (m.chiSoTep > 0) {
                final int k = m.chiSoTep - 1;
                if (k >= chinh.tepNgoai.size()) {
                    soKhongThay++;
                    continue;
                }
                tenTep = chinh.tepNgoai.get(k);
                final int gach = Math.max(tenTep.lastIndexOf('/'), tenTep.lastIndexOf('\\'));
                tenTep = tenTep.substring(gach + 1);
            }
            final TepAssets t = kho.get(tenTep);
            final DoiTuong o = (t == null) ? null : t.theoPathID.get(Long.valueOf(m.pathID));
            if (o == null) {
                soKhongThay++;
                continue;
            }
            final String khoaLop = "lop" + o.lop;
            final Integer cu = demLop.get(khoaLop);
            demLop.put(khoaLop, Integer.valueOf((cu == null ? 0 : cu.intValue()) + 1));
            if (o.lop != LOP_TEXT_ASSET) {
                soKhongPhaiText++;
                System.out.println("  bo qua (lop " + o.lop + "): " + m.duong);
                continue;
            }
            t.vt = o.batDau;
            t.docChuoi();
            final byte[] noi = t.docMangByte();
            final Path dich = ra.resolve(theoCachVietCuaMa(m.duong, chuanHoa) + ".bytes");
            if (dich.getParent() != null) {
                Files.createDirectories(dich.getParent());
            }
            Files.write(dich, noi);
            soGhi++;
        }
        System.out.println("Ghi " + soGhi + " tai nguyen. Bo qua " + soKhongPhaiText
                + " cai khong phai TextAsset, " + soKhongThay + " cai khong tra ra doi tuong.");
        System.out.println("Cac lop trong bang duong dan: " + demLop);
    }

    /**
     * Doi duong dan da ha chu thuong cua ResourceManager ve dung cach viet ma nguon dung.
     * Khong tra ra ten nao thi giu nguyen - tai nguyen do ma nguon khong hoi xin.
     */
    private static String theoCachVietCuaMa(final String duong, final Map<String, String> chuanHoa) {
        if (!duong.startsWith("res/")) {
            return duong;
        }
        final String con = duong.substring("res/".length());
        final String dung = chuanHoa.get(con.toLowerCase(java.util.Locale.ROOT));
        return (dung == null) ? duong : ("res/" + dung);
    }

    private RutTaiNguyenUnity() {
    }
}
