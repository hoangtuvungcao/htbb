package game.pokemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Hashtable;

/**
 * Goi sprite mot loai (may chu gui qua -126 kieu 0, may chu doc server/.../&lt;khoa&gt;.goi). Lop THUAN.
 *
 * Dinh dang: 'H','T','P','K'; u8 phienBan = 1; i16 dex; u8 shiny; u8 n; n x (UTF tenTep, i32 len, len byte).
 * tenTep giong ten trong jar res/pk: "0016.dl", "0016_0.png" .. "0016_5.png", "0016_c.png".
 */
public final class GoiSprite {
    public static final int PHIEN_BAN = 1;
    /** Mot tep trong goi lon hon muc nay coi la hong (sprite jar &lt; 64 KB / dong tac). */
    static final int TEP_TOI_DA = 262144;

    private GoiSprite() {
    }

    public static boolean dauHopLe(byte[] b) {
        return b != null && b.length >= 9 && b[0] == 'H' && b[1] == 'T' && b[2] == 'P' && b[3] == 'K';
    }

    /** Dex ghi trong goi (-1 neu hong). */
    public static int dex(byte[] b) {
        if (!dauHopLe(b)) {
            return -1;
        }
        return (short) (((b[5] & 0xFF) << 8) | (b[6] & 0xFF));
    }

    /**
     * Tach goi thanh tenTep -&gt; byte[]. null neu sai dau / sai phien ban / sai dex (mongDex &gt; 0) / thieu tep
     * .dl / tran du lieu.
     */
    public static Hashtable doc(byte[] b, int mongDex) {
        if (!dauHopLe(b)) {
            return null;
        }
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(b));
            r.skip(4);
            if (r.readUnsignedByte() != PHIEN_BAN) {
                return null;
            }
            int d = r.readShort();
            r.readUnsignedByte();
            if (mongDex > 0 && d != mongDex) {
                return null;
            }
            int n = r.readUnsignedByte();
            Hashtable kq = new Hashtable();
            boolean coDl = false;
            for (int i = 0; i < n; i++) {
                String ten = r.readUTF();
                int len = r.readInt();
                if (len < 0 || len > TEP_TOI_DA || len > r.available()) {
                    return null;
                }
                byte[] t = new byte[len];
                r.readFully(t);
                kq.put(ten, t);
                if (ten.endsWith(".dl")) {
                    coDl = true;
                }
            }
            return coDl ? kq : null;
        } catch (Throwable e) {
            return null;
        }
    }

    /** Tao goi (cong cu / kiem). */
    public static byte[] tao(int dex, boolean shiny, String[] ten, byte[][] du) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream o = new DataOutputStream(bo);
            o.writeByte('H');
            o.writeByte('T');
            o.writeByte('P');
            o.writeByte('K');
            o.writeByte(PHIEN_BAN);
            o.writeShort(dex);
            o.writeByte(shiny ? 1 : 0);
            o.writeByte(ten.length);
            for (int i = 0; i < ten.length; i++) {
                o.writeUTF(ten[i]);
                o.writeInt(du[i].length);
                o.write(du[i]);
            }
            o.flush();
            return bo.toByteArray();
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Ten tep trong jar ("/pk/0016_0.png") -&gt; khoa loai ("0016"); null neu khong phai sprite loai thuong 4 so
     * (bong, vt_*, hu_*, shiny "0016s" ... khong tai theo nhu cau).
     */
    public static String khoaTuDuongDan(String duongDan) {
        if (duongDan == null || !duongDan.startsWith("/pk/") || duongDan.length() < 8) {
            return null;
        }
        for (int i = 4; i < 8; i++) {
            char c = duongDan.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
        }
        if (duongDan.length() > 8) {
            char c = duongDan.charAt(8);
            if (c != '.' && c != '_') {
                return null;
            }
        }
        return duongDan.substring(4, 8);
    }
}
