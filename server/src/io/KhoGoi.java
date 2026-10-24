package io;

import java.io.DataInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * KHO GOI PHIA CLIENT (0.9.15, kiem lag "vao game cham"): moi lan dang nhap server gui lai cac goi du lieu lon gan nhu
 * khong doi - lenh 25 (mau vat pham ~156 KB), 26 (mau quai + hieu ung ~154 KB), 1 (~17 KB, hai lan), -108 (ban do khoa
 * ~33 KB), 105 (~19 KB): ~380 KB goc, ~90 KB sau nen; qua mang 40 KB/s = ~2-3 s moi lan vao game (J2ME khong nen: ~9 s).
 *
 * Client tu bao co kho (bit KHA_NANG_KHO trong so nguyen kha nang, so ban >= BAN_TOI_THIEU) va KHAI cac goi dang giu
 * ngay sau so kha nang o goi dang nhap: u8 n, n x (i8 lenh, i64 bam, i32 do dai). Bam = FNV-1a 64 bit cua DU LIEU goi
 * (khong tinh dau khung), tinh tren chinh byte se gui - goi phu thuoc phien (vd lenh 1 theo vsTile) van dung.
 *
 * Gui goi thuoc LOAI cho phien co kho:
 *  - (lenh, bam, do dai) co trong danh sach client khai: gui goi LENH [u8 1][i8 lenh][i64 bam][i32 do dai] THAY goi goc
 *    (client doc goi tu kho, xu ly dung vi tri do trong luong goi). Goi kem (Message.kemTheo) van gui ngay sau.
 *  - khong co (moi / da doi): gui LENH [u8 0][i8 lenh][i64 bam][i32 do dai] ("luu goi ke tiep") roi goi goc nguyen ven.
 * Client khong bao bit / ban cu / Unity: KHONG doi mot byte nao. Client doc kho hong luc dung -> tu ngat ket noi, xoa
 * kho, vao lai nhan du goi (kho chi khai goi da kiem bam luc dang nhap nen hiem khi xay ra).
 */
public final class KhoGoi {
    private KhoGoi() {
    }

    /** Lenh S->C cua kho goi (khong lenh nao khac dung -117). */
    public static final int LENH = -117;
    /** Bit trong Session.khaNang: client co kho goi. */
    public static final int KHA_NANG_KHO = 4;
    /** So ban client toi thieu (0.9.15). */
    public static final int BAN_TOI_THIEU = 915;
    /** Toi da so muc client khai (goi dang nhap < 1024 byte). */
    public static final int KHAI_TOI_DA = 16;
    /** sv.khoGoi: 0 = tat (moi client nhan du goi nhu cu). */
    public static volatile boolean BAT = true;
    /** Goi nho hon nguong nay khong dang dung kho. */
    public static final int NGUONG = 2048;

    /** Lenh duoc dung kho: du lieu thiet ke lon gui luc dang nhap / chon nhan vat. */
    static final int[] LOAI = {25, 26, 1, -108, 105};

    public static boolean laLoai(final int lenh) {
        for (int i = 0; i < LOAI.length; i++) {
            if (LOAI[i] == lenh) {
                return true;
            }
        }
        return false;
    }

    /** FNV-1a 64 bit (client network.KhoGoi.bam cung cong thuc). */
    public static long bam(final byte[] b, final int tu, final int n) {
        long h = 0xcbf29ce484222325L;
        for (int i = tu; i < tu + n; i++) {
            h ^= (b[i] & 255);
            h *= 0x100000001b3L;
        }
        return h;
    }

    static String khoa(final int lenh, final long bam, final int dai) {
        return (byte) lenh + ":" + Long.toHexString(bam) + ":" + dai;
    }

    /**
     * Doc danh sach client khai (sau so kha nang o goi dang nhap). Chi goi khi ban >= BAN_TOI_THIEU va co bit
     * KHA_NANG_KHO. Thieu / hong -> danh sach rong (van luu goi moi cho lan sau).
     */
    public static Set<String> docKhai(final DataInputStream dis) {
        final Set<String> s = new HashSet<String>();
        try {
            if (dis.available() < 1) {
                return s;
            }
            final int n = dis.readUnsignedByte();
            for (int i = 0; i < n && i < KHAI_TOI_DA; i++) {
                final int lenh = dis.readByte();
                final long bam = dis.readLong();
                final int dai = dis.readInt();
                if (laLoai(lenh) && dai > 0) {
                    s.add(khoa(lenh, bam, dai));
                }
            }
        } catch (Exception e) {
            // khai cut ngang: giu phan da doc
        }
        return s;
    }

    /** Phien nay dung kho goi khong (ban, bit, cong tac). */
    public static boolean dung(final Session s) {
        return BAT && s != null && s.khoGoi != null && s.banClient >= BAN_TOI_THIEU && (s.khaNang & KHA_NANG_KHO) != 0;
    }

    /** {dau khung, do dai du lieu} cua khung Message.toByteArray. */
    static int[] viTri(final byte[] khung) {
        if (khung.length >= 6 && khung[0] == -128) {
            final int dai = ((khung[2] & 255) << 24) | ((khung[3] & 255) << 16) | ((khung[4] & 255) << 8) | (khung[5] & 255);
            return new int[]{6, dai};
        }
        return new int[]{3, ((khung[1] & 255) << 8) | (khung[2] & 255)};
    }

    public static final java.util.concurrent.atomic.AtomicLong SO_DUNG_KHO = new java.util.concurrent.atomic.AtomicLong();
    public static final java.util.concurrent.atomic.AtomicLong BYTE_TIET_KIEM = new java.util.concurrent.atomic.AtomicLong();

    /**
     * Goi thay the cho m (dung trong Session.sendMessage): m neu khong thuoc kho; goi "dung kho" (kem cac goi kem cua m)
     * neu client da co; goi "luu" kem m neu chua co. Khong sua m (goi phat chung nhieu phien).
     */
    public static Message thayThe(final Session s, final Message m) {
        if (m == null || !laLoai(m.command) || !dung(s)) {
            return m;
        }
        final byte[] khung = m.toByteArray();
        if (khung == null || khung.length < NGUONG) {
            return m;
        }
        final int[] vt = viTri(khung);
        if (vt[0] + vt[1] != khung.length) {
            return m;
        }
        final long h = m.bamKho(vt[0], vt[1]);
        final boolean co = s.khoGoi.contains(khoa(m.command, h, vt[1]));
        final Message t = new Message(LENH);
        try {
            t.dos.writeByte(co ? 1 : 0);
            t.dos.writeByte(m.command);
            t.dos.writeLong(h);
            t.dos.writeInt(vt[1]);
        } catch (Exception e) {
            return m;
        }
        if (co) {
            t.kemTheo = m.kemTheo;
            SO_DUNG_KHO.incrementAndGet();
            BYTE_TIET_KIEM.addAndGet(khung.length);
        } else {
            t.kemTheo = m;   // "luu goi ke tiep" + goi goc (+ goi kem cua no) lien tiep duoi khoa Sender
        }
        return t;
    }
}
