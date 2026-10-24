package network;

import java.io.DataOutputStream;
import java.util.Hashtable;
import javax.microedition.rms.RecordStore;

/**
 * KHO GOI (0.9.15, server io.KhoGoi): giu cac goi du lieu lon gan nhu khong doi (lenh 25 mau vat pham, 26 mau quai,
 * 1, -108, 105 - ~380 KB goc moi lan vao game) trong RMS; lan dang nhap sau khai (lenh, bam, do dai) cac goi da giu,
 * server goi -117 "dung kho" thay vi gui lai.
 *
 * Bo cuc RMS: moi muc mot kho "gc&lt;i&gt;_&lt;o&gt;" (i = vi tri lenh trong LOAI, o = o 0 / 1; J2ME chi o 0): ban ghi 1 =
 * dau [int MAGIC][i8 lenh][i64 bam][i32 do dai][i32 thu tu][i16 so khuc], ban ghi 2.. = du lieu theo khuc &lt;= KHUC
 * byte. Bam = FNV-1a 64 bit cua du lieu goi (giong server). Chi PC / APK (co giai nen); J2ME khong dung kho (coKho).
 *
 * An toan: chi KHAI muc doc lai du va dung bam ngay luc dang nhap. Goi -117 kieu 1 ma doc kho hong -> xoa het kho, tat kho
 * trong phien, ngat ket noi (tu noi lai se nhan du goi). Kho day -> khong luu (lan sau van nhan du).
 */
public final class KhoGoi {
    private KhoGoi() {
    }

    public static final byte LENH = -117;
    public static final int KHA_NANG_KHO = 4;
    static final int[] LOAI = {25, 26, 1, -108, 105};
    static final int MAGIC = 0x48544743;   // "HTGC"
    static final int KHUC = 8192;

    /** tat trong phien (kho hong / khong mo duoc RMS) */
    static boolean tat;
    static int coRms = -1;
    /** khoa (lenh:bam:dai) -> ten kho, cac muc da kiem luc khai */
    private static final Hashtable daKiem = new Hashtable();
    private static int choLenh = 999;
    private static long choBam;
    private static int choDai;
    private static int thuTu;
    /** so goi da dung kho / da luu (kiem thu, do) */
    public static int soDung, soLuu;
    public static long byteDung;

    static boolean laJ2me() {
        return (Session_ME.khaNang() & Session_ME.KHA_NANG_NEN) == 0;
    }

    static int soO() {
        return laJ2me() ? 1 : 2;
    }

    static int viTri(int lenh) {
        for (int i = 0; i < LOAI.length; i++) {
            if ((byte) LOAI[i] == (byte) lenh) {
                return i;
            }
        }
        return -1;
    }

    static String ten(int i, int o) {
        return "gc" + i + "_" + o;
    }

    static String khoa(int lenh, long bam, int dai) {
        return (byte) lenh + ":" + bam + ":" + dai;
    }

    public static long bam(long h, byte[] b, int tu, int n) {
        for (int i = tu; i < tu + n; i++) {
            h ^= (b[i] & 255);
            h *= 0x100000001b3L;
        }
        return h;
    }

    static final long BAM_DAU = 0xcbf29ce484222325L;

    /**
     * Co dung kho khong: co RMS (mo thu mot lan) va KHONG phai J2ME. J2ME (khong nen) khong dung kho: do tren gia lap
     * heap 7 MB (RMS nam trong heap), luu goi 25 / 26 lam 2/3 lan vao game het bo nho; may that khong biet RMS nam o dau -
     * giu dung giao thuc 0.9.14 (nhan du goi) cho chac.
     */
    static boolean coKho() {
        if (tat || laJ2me()) {
            return false;
        }
        if (coRms < 0) {
            try {
                RecordStore r = RecordStore.openRecordStore("gcthu", true);
                r.closeRecordStore();
                coRms = 1;
            } catch (Throwable e) {
                coRms = 0;
            }
        }
        return coRms == 1;
    }

    /** Bit kha nang them vao goi dang nhap. */
    public static int khaNang() {
        return coKho() ? KHA_NANG_KHO : 0;
    }

    /** Dau muc o kho ten: {lenh, dai, thuTu, soKhuc} + bam o out[0]; null neu khong co / hong. */
    static int[] dau(RecordStore r, long[] bamRa) {
        try {
            if (r.getNumRecords() < 1) {
                return null;
            }
            java.io.DataInputStream d = new java.io.DataInputStream(new java.io.ByteArrayInputStream(r.getRecord(1)));
            if (d.readInt() != MAGIC) {
                return null;
            }
            int lenh = d.readByte();
            bamRa[0] = d.readLong();
            int dai = d.readInt();
            int tt = d.readInt();
            int sk = d.readShort();
            if (dai <= 0 || sk <= 0 || r.getNumRecords() < sk + 1) {
                return null;
            }
            return new int[]{lenh, dai, tt, sk};
        } catch (Throwable e) {
            return null;
        }
    }

    /** Doc du lieu muc (khuc), kiem do dai + bam. null neu hong. giu = false: chi kiem (khong giu khuc - J2ME it heap). */
    static byte[][] docKhuc(RecordStore r, int[] dau, long bamMong, boolean giu) {
        try {
            byte[][] ds = giu ? new byte[dau[3]][] : null;
            long h = BAM_DAU;
            int tong = 0;
            for (int k = 0; k < dau[3]; k++) {
                byte[] x = r.getRecord(k + 2);
                if (x == null) {
                    x = new byte[0];
                }
                h = bam(h, x, 0, x.length);
                tong += x.length;
                if (giu) {
                    ds[k] = x;
                }
            }
            if (tong != dau[1] || h != bamMong) {
                return null;
            }
            return giu ? ds : new byte[0][];
        } catch (Throwable e) {
            return null;
        }
    }

    static void xoa(String ten) {
        try {
            RecordStore.deleteRecordStore(ten);
        } catch (Throwable e) {
            // khong co
        }
    }

    /** Xoa moi muc (kho hong). */
    static void xoaHet() {
        for (int i = 0; i < LOAI.length; i++) {
            for (int o = 0; o < 2; o++) {
                xoa(ten(i, o));
            }
        }
        daKiem.clear();
    }

    /**
     * Goi dang nhap (sau so kha nang): u8 n, n x (i8 lenh, i64 bam, i32 do dai) cac muc doc lai du va dung bam.
     * Khong co kho -> u8 0.
     */
    public static void khai(DataOutputStream o) throws java.io.IOException {
        daKiem.clear();
        choLenh = 999;
        java.util.Vector ds = new java.util.Vector();
        if (coKho()) {
            int so = soO();
            for (int i = 0; i < LOAI.length; i++) {
                for (int s = 0; s < 2; s++) {
                    String t = ten(i, s);
                    if (s >= so) {
                        xoa(t);   // J2ME: bo o thu hai (doi tu ban PC cu sang khong xay ra, chan cho chac)
                        continue;
                    }
                    RecordStore r = null;
                    try {
                        r = RecordStore.openRecordStore(t, false);
                        long[] b = new long[1];
                        int[] d = dau(r, b);
                        boolean ok = d != null && d[0] == (byte) LOAI[i] && docKhuc(r, d, b[0], false) != null;
                        if (d != null && d[2] > thuTu) {
                            thuTu = d[2];
                        }
                        r.closeRecordStore();
                        r = null;
                        if (ok) {
                            String k = khoa(d[0], b[0], d[1]);
                            daKiem.put(k, t);
                            ds.addElement(new Object[]{new Integer(d[0]), new Long(b[0]), new Integer(d[1])});
                        } else {
                            xoa(t);
                        }
                    } catch (Throwable e) {
                        // chua co muc
                    } finally {
                        if (r != null) {
                            try {
                                r.closeRecordStore();
                            } catch (Throwable e2) {
                                // bo
                            }
                        }
                    }
                }
            }
        }
        int n = ds.size() > 16 ? 16 : ds.size();
        o.writeByte(n);
        for (int i = 0; i < n; i++) {
            Object[] m = (Object[]) ds.elementAt(i);
            o.writeByte(((Integer) m[0]).intValue());
            o.writeLong(((Long) m[1]).longValue());
            o.writeInt(((Integer) m[2]).intValue());
        }
    }

    /** Dau goi -117 chua xu ly (Session_ME tra ve de bo qua). */
    static final Message BO_QUA = new Message();

    /**
     * Goi -117: kieu 0 = goi ke tiep cung lenh can luu (tra BO_QUA); kieu 1 = dung kho: tra goi goc doc tu kho, null neu
     * kho hong (Session_ME ngat ket noi).
     */
    static Message nhan(Message m) {
        try {
            java.io.DataInputStream d = m.reader();
            int kieu = d.readByte();
            int lenh = d.readByte();
            long b = d.readLong();
            int dai = d.readInt();
            if (kieu == 0) {
                choLenh = lenh;
                choBam = b;
                choDai = dai;
                return BO_QUA;
            }
            Message g = doc(lenh, b, dai);
            if (g == null) {
                game.pokemon.GiamSat.ghi("kho goi: khong doc duoc lenh " + lenh + " - xoa kho, noi lai");
                tat = true;
                xoaHet();
                return null;
            }
            soDung++;
            byteDung += dai;
            return g;
        } catch (Throwable e) {
            tat = true;
            xoaHet();
            return null;
        }
    }

    static Message doc(int lenh, long b, int dai) {
        String t = (String) daKiem.get(khoa(lenh, b, dai));
        if (t == null) {
            return null;
        }
        RecordStore r = null;
        try {
            r = RecordStore.openRecordStore(t, false);
            long[] bb = new long[1];
            int[] d = dau(r, bb);
            if (d == null || d[0] != (byte) lenh || d[1] != dai || bb[0] != b) {
                return null;
            }
            byte[][] ds = docKhuc(r, d, b, true);
            if (ds == null) {
                return null;
            }
            int[] dauK = new int[ds.length];
            // danh dau vua dung (o cu nhat bi thay truoc)
            try {
                ByteArrayOutputStreamHo h = new ByteArrayOutputStreamHo();
                ghiDau(h.o, d[0], b, dai, ++thuTu, ds.length);
                byte[] x = h.b.toByteArray();
                r.setRecord(1, x, 0, x.length);
            } catch (Throwable e) {
                // khong ghi duoc thu tu: bo
            }
            return new Message((byte) lenh, new LuongManh(ds, dauK));
        } catch (Throwable e) {
            return null;
        } finally {
            if (r != null) {
                try {
                    r.closeRecordStore();
                } catch (Throwable e2) {
                    // bo
                }
            }
        }
    }

    static final class ByteArrayOutputStreamHo {
        final java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream(32);
        final DataOutputStream o = new DataOutputStream(b);
    }

    static void ghiDau(DataOutputStream o, int lenh, long b, int dai, int tt, int soKhuc) throws java.io.IOException {
        o.writeInt(MAGIC);
        o.writeByte(lenh);
        o.writeLong(b);
        o.writeInt(dai);
        o.writeInt(tt);
        o.writeShort(soKhuc);
    }

    /** Sau khi xu ly goi: goi dang cho luu (-117 kieu 0) -> ghi vao kho. */
    static void sauXuLy(Message m) {
        if (m == null || choLenh == 999 || (byte) m.command != (byte) choLenh) {
            return;
        }
        int lenh = choLenh;
        long b = choBam;
        int dai = choDai;
        choLenh = 999;
        if (!coKho()) {
            return;
        }
        try {
            luu(m, lenh, b, dai);
        } catch (Throwable e) {
            // khong luu duoc: lan sau van nhan du
        }
    }

    /** Cac doan du lieu cua goi: {mang, tu, so byte}. */
    static Object[] doan(Message m) {
        if (m.tho != null) {
            return new Object[]{new byte[][]{m.tho}, new int[]{0}};
        }
        if (m.manh != null) {
            return new Object[]{m.manh.ds(), m.manh.dau()};
        }
        return null;
    }

    static boolean luu(Message m, int lenh, long b, int dai) throws Exception {
        int i = viTri(lenh);
        Object[] dn = doan(m);
        if (i < 0 || dn == null) {
            return false;
        }
        byte[][] ds = (byte[][]) dn[0];
        int[] tu = (int[]) dn[1];
        long h = BAM_DAU;
        int tong = 0;
        for (int k = 0; k < ds.length; k++) {
            h = bam(h, ds[k], tu[k], ds[k].length - tu[k]);
            tong += ds[k].length - tu[k];
        }
        if (h != b || tong != dai) {
            return false;   // khong dung goi server bao
        }
        // chon o: o trong / hong, khong thi o dung lau nhat
        int so = soO();
        int chon = 0;
        int ttNho = Integer.MAX_VALUE;
        for (int o = 0; o < so; o++) {
            RecordStore r = null;
            int tt = -1;
            try {
                r = RecordStore.openRecordStore(ten(i, o), false);
                int[] d = dau(r, new long[1]);
                tt = d == null ? -1 : d[2];
            } catch (Throwable e) {
                tt = -1;
            } finally {
                if (r != null) {
                    try {
                        r.closeRecordStore();
                    } catch (Throwable e2) {
                        // bo
                    }
                }
            }
            if (tt < 0) {
                chon = o;
                break;
            }
            if (tt < ttNho) {
                ttNho = tt;
                chon = o;
            }
        }
        String t = ten(i, chon);
        xoa(t);
        RecordStore r = null;
        boolean xong = false;
        try {
            r = RecordStore.openRecordStore(t, true);
            int can = dai + 4096;
            if (r.getSizeAvailable() < can) {
                return false;   // kho may con it cho: khong luu
            }
            int soKhuc = 0;
            for (int k = 0; k < ds.length; k++) {
                int n = ds[k].length - tu[k];
                soKhuc += (n + KHUC - 1) / KHUC;
            }
            ByteArrayOutputStreamHo hd = new ByteArrayOutputStreamHo();
            ghiDau(hd.o, lenh, b, dai, ++thuTu, soKhuc);
            byte[] x = hd.b.toByteArray();
            r.addRecord(x, 0, x.length);
            for (int k = 0; k < ds.length; k++) {
                int p = tu[k];
                while (p < ds[k].length) {
                    int n = ds[k].length - p > KHUC ? KHUC : ds[k].length - p;
                    r.addRecord(ds[k], p, n);
                    p += n;
                }
            }
            xong = true;
            daKiem.put(khoa(lenh, b, dai), t);
            soLuu++;
            return true;
        } finally {
            if (r != null) {
                try {
                    r.closeRecordStore();
                } catch (Throwable e2) {
                    // bo
                }
            }
            if (!xong) {
                xoa(t);
            }
        }
    }
}
