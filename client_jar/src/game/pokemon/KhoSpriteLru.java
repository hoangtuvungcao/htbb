package game.pokemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;

/**
 * So sach LRU theo DUNG LUONG (byte) cho kho sprite tai theo nhu cau (KhoSprite). Lop THUAN (khong RMS, khong
 * lcdui) - KiemKhoSprite kiem tren JDK.
 *
 * Moi muc: khoa (vd "0016"), kich thuoc (byte), lan dung (bo dem tang dan). them() vuot ngan sach thi bo muc LAU
 * KHONG DUNG nhat (khong bao gio bo muc vua them) va tra danh sach khoa da bo de noi goi xoa kho that.
 * ghi()/doc(): ban ghi chi muc luu trong RMS: u8 phienBan=1, i32 lanDung, u16 n, n x (UTF khoa, i32 kich, i32 lan).
 */
public final class KhoSpriteLru {
    /** Mac dinh 1 MB (tuong duong ~30 loai x 32 KB). */
    public static final int NGAN_SACH_MAC_DINH = 1048576;

    private int nganSach;
    private int tong;
    private int lanDung;
    /** Phan tu: Object[]{String khoa, int[]{kich, lan}} - giu thu tu them (khong can sap xep, n nho). */
    private final Vector muc = new Vector();

    public KhoSpriteLru(int nganSach) {
        this.nganSach = nganSach > 0 ? nganSach : NGAN_SACH_MAC_DINH;
    }

    public int nganSach() {
        return this.nganSach;
    }

    public void datNganSach(int b) {
        if (b > 0) {
            this.nganSach = b;
        }
    }

    public int tong() {
        return this.tong;
    }

    public int soMuc() {
        return this.muc.size();
    }

    private int viTri(String khoa) {
        for (int i = 0; i < this.muc.size(); i++) {
            if (((Object[]) this.muc.elementAt(i))[0].equals(khoa)) {
                return i;
            }
        }
        return -1;
    }

    public boolean co(String khoa) {
        return viTri(khoa) >= 0;
    }

    public int kich(String khoa) {
        int i = viTri(khoa);
        return i < 0 ? 0 : ((int[]) ((Object[]) this.muc.elementAt(i))[1])[0];
    }

    /** Danh dau vua dung. false neu khong co. */
    public boolean dung(String khoa) {
        int i = viTri(khoa);
        if (i < 0) {
            return false;
        }
        ((int[]) ((Object[]) this.muc.elementAt(i))[1])[1] = ++this.lanDung;
        return true;
    }

    /** Khoa lau khong dung nhat, tru {@code tru}; null neu khong co. */
    public String cuNhat(String tru) {
        String kq = null;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < this.muc.size(); i++) {
            Object[] m = (Object[]) this.muc.elementAt(i);
            int lan = ((int[]) m[1])[1];
            if (!m[0].equals(tru) && lan < min) {
                min = lan;
                kq = (String) m[0];
            }
        }
        return kq;
    }

    /**
     * Them (hoac thay) muc {@code khoa} kich {@code kich} byte. Tra ve Vector cac khoa da bi bo de vua ngan sach
     * (rong neu khong bo gi). Muc lon hon ca ngan sach van duoc giu mot minh (bo het muc khac).
     */
    public Vector them(String khoa, int kich) {
        xoa(khoa);
        this.muc.addElement(new Object[]{khoa, new int[]{kich, ++this.lanDung}});
        this.tong += kich;
        Vector bo = new Vector();
        while (this.tong > this.nganSach) {
            String k = cuNhat(khoa);
            if (k == null) {
                break;
            }
            xoa(k);
            bo.addElement(k);
        }
        return bo;
    }

    /** Bo muc (vd kho that bao loi / da xoa). */
    public boolean xoa(String khoa) {
        int i = viTri(khoa);
        if (i < 0) {
            return false;
        }
        this.tong -= ((int[]) ((Object[]) this.muc.elementAt(i))[1])[0];
        this.muc.removeElementAt(i);
        return true;
    }

    /** Moi khoa (de kiem / don kho). */
    public String[] khoa() {
        String[] k = new String[this.muc.size()];
        for (int i = 0; i < k.length; i++) {
            k[i] = (String) ((Object[]) this.muc.elementAt(i))[0];
        }
        return k;
    }

    public byte[] ghi() {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream o = new DataOutputStream(bo);
            o.writeByte(1);
            o.writeInt(this.lanDung);
            o.writeShort(this.muc.size());
            for (int i = 0; i < this.muc.size(); i++) {
                Object[] m = (Object[]) this.muc.elementAt(i);
                int[] v = (int[]) m[1];
                o.writeUTF((String) m[0]);
                o.writeInt(v[0]);
                o.writeInt(v[1]);
            }
            o.flush();
            return bo.toByteArray();
        } catch (Throwable e) {
            return new byte[0];
        }
    }

    /** Nap chi muc da luu (thay noi dung hien tai). false = hong / phien ban la (giu rong). */
    public boolean doc(byte[] b) {
        this.muc.removeAllElements();
        this.tong = 0;
        this.lanDung = 0;
        if (b == null || b.length < 7) {
            return false;
        }
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(b));
            if (r.readUnsignedByte() != 1) {
                return false;
            }
            int lan = r.readInt();
            int m = r.readUnsignedShort();
            for (int i = 0; i < m; i++) {
                String k = r.readUTF();
                int kich = r.readInt();
                int l = r.readInt();
                if (kich > 0 && viTri(k) < 0) {
                    this.muc.addElement(new Object[]{k, new int[]{kich, l}});
                    this.tong += kich;
                }
            }
            this.lanDung = lan;
            return true;
        } catch (Throwable e) {
            this.muc.removeAllElements();
            this.tong = 0;
            return false;
        }
    }
}
