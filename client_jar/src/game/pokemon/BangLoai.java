package game.pokemon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * BANG LOAI (1.025 loai, lenh -126 kieu 1) + co Pokedex cua nguoi choi (lenh -126 kieu 3). Lop THUAN (khong lcdui,
 * khong RMS) de KiemKhoSprite kiem tren JDK; luu RMS o LoaiMang.
 *
 * Than kieu 1 (sau byte kieu): i32 bam, u16 n, n x (i16 dex, UTF ten, i8 he1, i8 he2 (-1 khong), u8 bac 1..6,
 * u8 theHe 1..9, u8 co: bit0 da mo, bit1 co sprite tren may chu, bit2 co chan dung).
 * Than kieu 3: u16 n, n x (i16 dex, u8 co: bit0 da gap, bit1 da ha, bit2 da bat).
 */
public final class BangLoai {
    public static final int CO_DA_MO = 1, CO_SPRITE = 2, CO_CHAN_DUNG = 4;
    public static final int DEX_GAP = 1, DEX_HA = 2, DEX_BAT = 4, DEX_BAT_SHINY = 8;
    /** Dex lon nhat client giu chi so (Gen 9 = 1025, de du cho dang moi). */
    public static final int DEX_TOI_DA = 1200;

    /** Mau bac (vong duoi chan / chip): T1 trang, T2 xanh la, T3 xanh duong, T4 tim, T5 cam, T6 do. */
    public static final int[] MAU_BAC = {0xFFFFFF, 0xFFFFFF, 0x6CC04A, 0x3C8CE6, 0xA555E0, 0xF08A24, 0xE03C3C};
    public static final String[] TEN_BAC = {"", "Phổ biến", "Không phổ biến", "Hiếm", "Rất hiếm", "Cực hiếm", "Huyền thoại"};

    public static int bam;
    public static int n;
    public static short[] dex = new short[0];
    public static String[] ten = new String[0];
    public static byte[] he1 = new byte[0], he2 = new byte[0], bac = new byte[0], theHe = new byte[0], co = new byte[0];
    /** dex -> chi so trong bang + 1 (0 = khong co). */
    static short[] viTri = new short[DEX_TOI_DA + 1];

    /** Co Pokedex theo dex (DEX_GAP / DEX_HA / DEX_BAT). */
    public static byte[] pokedex = new byte[DEX_TOI_DA + 1];
    public static boolean coPokedex;
    /** Tang moi lan bang / co doi (man Pokedex tinh lai danh sach). */
    public static int phienBan;

    private BangLoai() {
    }

    public static boolean coBang() {
        return n > 0;
    }

    /** Doc than kieu 1 (sau byte kieu). false = hong (giu bang cu). */
    public static synchronized boolean docBang(byte[] than) {
        if (than == null || than.length < 6) {
            return false;
        }
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(than));
            int b = r.readInt();
            int m = r.readUnsignedShort();
            short[] d = new short[m];
            String[] t = new String[m];
            byte[] a1 = new byte[m], a2 = new byte[m], bc = new byte[m], th = new byte[m], c = new byte[m];
            short[] vt = new short[DEX_TOI_DA + 1];
            for (int i = 0; i < m; i++) {
                d[i] = r.readShort();
                t[i] = r.readUTF();
                a1[i] = r.readByte();
                a2[i] = r.readByte();
                bc[i] = (byte) r.readUnsignedByte();
                th[i] = (byte) r.readUnsignedByte();
                c[i] = (byte) r.readUnsignedByte();
                if (d[i] > 0 && d[i] <= DEX_TOI_DA) {
                    vt[d[i]] = (short) (i + 1);
                }
            }
            bam = b;
            dex = d;
            ten = t;
            he1 = a1;
            he2 = a2;
            bac = bc;
            theHe = th;
            co = c;
            viTri = vt;
            n = m;
            phienBan++;
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /** Doc than kieu 3 (sau byte kieu). */
    public static synchronized boolean docPokedex(DataInputStream r) {
        try {
            int m = r.readUnsignedShort();
            byte[] p = new byte[DEX_TOI_DA + 1];
            for (int i = 0; i < m; i++) {
                int d = r.readShort();
                int c = r.readUnsignedByte();
                if (d > 0 && d <= DEX_TOI_DA) {
                    p[d] = (byte) c;
                }
            }
            pokedex = p;
            coPokedex = true;
            phienBan++;
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /** Chi so trong bang, -1 neu khong co. */
    public static int tim(int d) {
        if (d <= 0 || d > DEX_TOI_DA) {
            return -1;
        }
        return viTri[d] - 1;
    }

    /** Ten loai ("" neu bang chua co). */
    public static String ten(int d) {
        int i = tim(d);
        return i < 0 ? "" : ten[i];
    }

    /** He 1 (-1 neu khong biet). */
    public static int he1(int d) {
        int i = tim(d);
        return i < 0 ? -1 : he1[i];
    }

    public static int he2(int d) {
        int i = tim(d);
        return i < 0 ? -1 : he2[i];
    }

    public static int bac(int d) {
        int i = tim(d);
        return i < 0 ? 0 : bac[i];
    }

    public static int theHe(int d) {
        int i = tim(d);
        return i < 0 ? 0 : theHe[i];
    }

    public static boolean daMo(int d) {
        int i = tim(d);
        return i >= 0 && (co[i] & CO_DA_MO) != 0;
    }

    public static int coPokedex(int d) {
        return d <= 0 || d > DEX_TOI_DA ? 0 : pokedex[d];
    }

    public static int mauBac(int b) {
        return b < 0 || b >= MAU_BAC.length ? 0xFFFFFF : MAU_BAC[b];
    }

    /** Xoa co Pokedex (dang xuat / doi nhan vat). Bang loai giu nguyen (chung cho moi nhan vat). */
    public static synchronized void xoaPokedex() {
        pokedex = new byte[DEX_TOI_DA + 1];
        coPokedex = false;
        phienBan++;
    }
}
