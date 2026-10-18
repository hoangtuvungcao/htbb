package game.pokemon;

/**
 * Bang hieu ung chieu kieu Pokemon (particle PMD, viec K3) cua ban jar - doc tu /pk/hu.bin.
 * Sinh boi client_jar/tools/DongGoiPokeJ2me.java tu Resources/poke/hu_bang.bytes + hu_*.bytes cua
 * Unity (HTBB). Dinh dang (big-endian):
 * <pre>
 *   u8 phienBan = 1, u8 n
 *   n lan: u8 lop, u8 chieu, UTF ten (anh /pk/&lt;ten&gt;.png), u16 rong, u16 cao (khung DA CAT sat hinh),
 *          u8 soKhung, u8 soHuong (1 hoac 4), u8 soCot, u8 kieuVe ('D' 'B' 'T'),
 *          u8 tickMoiKhung (don vi 1/50 s nhu Unity), i16 dx, i16 dy
 * </pre>
 * Khung f ve voi goc tren-trai = (x + dx, y + dy), (x, y) = tam hieu ung nhu ban Unity
 * (dx = xCat - rongGoc/2). soHuong = 4: hang = huong (Char.DOWN/UP/LEFT/RIGHT), cot = khung;
 * soHuong = 1: khung f o cot f % soCot, hang f / soCot.
 *
 * Lop nay KHONG dung lcdui de kiem thu duoc tren JDK (KiemPokeDuLieu).
 */
public final class HieuUngBang {
    public static final char VE_TAI_DICH = 'D';
    public static final char VE_BAY = 'B';
    public static final char VE_TREN_NGUOI = 'T';

    public int lop;
    public int chieu;
    public String ten;
    public int rong;
    public int cao;
    public int soKhung;
    public int soHuong;
    public int soCot;
    public char kieuVe;
    public int tickMoiKhung;
    public int dx;
    public int dy;

    /** Doc ca bang; null neu tep hong. */
    public static HieuUngBang[] doc(byte[] b) {
        if (b == null || b.length < 2 || b[0] != 1) {
            return null;
        }
        try {
            java.io.DataInputStream in = new java.io.DataInputStream(new java.io.ByteArrayInputStream(b));
            in.readUnsignedByte();
            int n = in.readUnsignedByte();
            HieuUngBang[] kq = new HieuUngBang[n];
            for (int i = 0; i < n; i++) {
                HieuUngBang h = new HieuUngBang();
                h.lop = in.readUnsignedByte();
                h.chieu = in.readUnsignedByte();
                h.ten = in.readUTF();
                h.rong = in.readUnsignedShort();
                h.cao = in.readUnsignedShort();
                h.soKhung = in.readUnsignedByte();
                h.soHuong = in.readUnsignedByte();
                h.soCot = in.readUnsignedByte();
                h.kieuVe = (char) in.readUnsignedByte();
                h.tickMoiKhung = in.readUnsignedByte();
                h.dx = in.readShort();
                h.dy = in.readShort();
                if (h.rong <= 0 || h.cao <= 0 || h.soKhung <= 0 || h.soCot <= 0 || h.tickMoiKhung <= 0) {
                    return null;
                }
                kq[i] = h;
            }
            return kq;
        } catch (Throwable e) {
            return null;
        }
    }

    /** So tick game (25/giay o ban jar) de chay het mot luot. */
    public int tongTick() {
        int t = (soKhung * tickMoiKhung + 1) / 2;
        return t < 1 ? 1 : t;
    }

    /** Khung can ve sau tick tick game (25/giay); lap = vong lai (dan dang bay). */
    public int khungTai(int tick, boolean lap) {
        int f = tick * 2 / tickMoiKhung;
        if (lap) {
            return f % soKhung;
        }
        return f >= soKhung ? soKhung - 1 : (f < 0 ? 0 : f);
    }

    /** Goc tren-trai cua khung f (huong dir) trong anh: {x, y}. */
    public int[] nguon(int f, int dir) {
        if (f < 0) {
            f = 0;
        }
        if (f >= soKhung) {
            f = soKhung - 1;
        }
        if (soHuong >= 4) {
            if (dir < 0 || dir > 3) {
                dir = 0;
            }
            return new int[]{f * rong, dir * cao};
        }
        return new int[]{f % soCot * rong, f / soCot * cao};
    }

    /** Kich thuoc anh can co: {rong, cao}. */
    public int[] kichThuocAnh() {
        if (soHuong >= 4) {
            return new int[]{soKhung * rong, 4 * cao};
        }
        return new int[]{soCot * rong, ((soKhung + soCot - 1) / soCot) * cao};
    }
}
