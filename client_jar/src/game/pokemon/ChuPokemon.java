package game.pokemon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * G7.C3: doi chu kiem hiep -&gt; the gioi Pokemon tren client - ban J2ME cua ClientUnity ChuPokemon.cs.
 * Tu dien /pk/chu.bin (u16 n, n x (writeUTF khoa, writeUTF chu moi)) sinh tu ChuPokemonDuLieu.cs
 * boi client_jar/tools/DongGoiPokeJ2me.java. Khoa bat dau bang "=" la so khop phan biet hoa thuong.
 *
 * CLDC khong co regex nen so khop bang tay, giu dung nghia mau cua ban C#:
 *   (?&lt;![\p{L}_])(?:khoa dai truoc | ...)(?!\w)   khong phan biet hoa thuong (tru khoa "=").
 * Doi o MOT cho: Font (ve chu, do rong, ngat dong) goi D(). TField dat KhongDoi khi ve chu
 * nguoi choi dang go.
 */
public final class ChuPokemon {
    /** Dat true khi ve chu nguoi choi dang go (TField) - khong doi. */
    public static boolean KhongDoi;

    private static boolean daNap;
    /** Cac khoa (da bo "=") theo thu tu thu: dai truoc, cung do dai thi theo thu tu tep. */
    private static String[] khoa;
    private static boolean[] phanBietHoa;
    /** chu cai dau (chu thuong) -&gt; int[] chi so trong khoa, giu thu tu. */
    private static Hashtable theoChuDau;
    private static Hashtable phanBiet;
    private static Hashtable khongPhanBiet;

    private static final Hashtable boNho = new Hashtable();
    private static final int BO_NHO_TOI_DA = 600;

    private ChuPokemon() {
    }

    public static String D(String s) {
        if (!PokeBody.CHE_DO_POKEMON || KhongDoi || s == null || s.length() == 0) {
            return s;
        }
        Object o = boNho.get(s);
        if (o != null) {
            return (String) o;
        }
        String kq = doiLuon(s);
        if (boNho.size() >= BO_NHO_TOI_DA) {
            boNho.clear();
        }
        boNho.put(s, kq);
        return kq;
    }

    // ------------------------------------------------------------ chu cai tieng Viet (CLDC chi biet Latin-1)

    public static char thuong(char c) {
        if (c < 0x80) {
            return c >= 'A' && c <= 'Z' ? (char) (c + 32) : c;
        }
        if (c >= 0xC0 && c <= 0xDE && c != 0xD7) {
            return (char) (c + 32);
        }
        if (c >= 0x100 && c <= 0x137 || c >= 0x14A && c <= 0x177) {
            return (c & 1) == 0 ? (char) (c + 1) : c;
        }
        if (c >= 0x139 && c <= 0x148) {
            return (c & 1) == 1 ? (char) (c + 1) : c;
        }
        if (c == 0x1A0 || c == 0x1AF) {
            return (char) (c + 1);
        }
        if (c >= 0x1EA0 && c <= 0x1EF9 || c >= 0x1E00 && c <= 0x1E95) {
            return (c & 1) == 0 ? (char) (c + 1) : c;
        }
        return c;
    }

    public static char hoa(char c) {
        if (c < 0x80) {
            return c >= 'a' && c <= 'z' ? (char) (c - 32) : c;
        }
        if (c >= 0xE0 && c <= 0xFE && c != 0xF7) {
            return (char) (c - 32);
        }
        if (c >= 0x101 && c <= 0x137 || c >= 0x14B && c <= 0x177) {
            return (c & 1) == 1 ? (char) (c - 1) : c;
        }
        if (c >= 0x13A && c <= 0x148) {
            return (c & 1) == 0 ? (char) (c - 1) : c;
        }
        if (c == 0x1A1 || c == 0x1B0) {
            return (char) (c - 1);
        }
        if (c >= 0x1EA1 && c <= 0x1EF9 || c >= 0x1E01 && c <= 0x1E95) {
            return (c & 1) == 1 ? (char) (c - 1) : c;
        }
        return c;
    }

    public static boolean laChu(char c) {
        if (c < 0x80) {
            return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
        }
        return c == 0xAA || c == 0xB5 || c == 0xBA || c >= 0xC0 && c <= 0x24F && c != 0xD7 && c != 0xF7
                || c >= 0x370 && c <= 0x3FF || c >= 0x400 && c <= 0x4FF || c >= 0x1E00 && c <= 0x1EFF;
    }

    private static boolean laChuWord(char c) {
        return laChu(c) || c >= '0' && c <= '9' || c == '_' || c >= 0x300 && c <= 0x36F;
    }

    public static boolean laHoa(char c) {
        return thuong(c) != c;
    }

    public static boolean laThuong(char c) {
        return hoa(c) != c;
    }

    public static String thuong(String s) {
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append(thuong(s.charAt(i)));
        }
        return sb.toString();
    }

    public static String hoa(String s) {
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append(hoa(s.charAt(i)));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------ nap tu dien

    private static void nap() {
        daNap = true;
        Vector k = new Vector();
        Vector v = new Vector();
        try {
            byte[] b = PokeBody.docTaiNguyen(PokeBody.THU_MUC + "chu.bin");
            if (b != null) {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
                int n = in.readUnsignedShort();
                for (int i = 0; i < n; i++) {
                    k.addElement(in.readUTF());
                    v.addElement(in.readUTF());
                }
            }
        } catch (Throwable e) {
            // tu dien hong: dung phan da doc duoc
        }
        napBang(k, v);
    }

    /** Dung bang tra tu cac cap (khoa, chu moi) - tach rieng de kiem thu. */
    public static void napBang(Vector k, Vector v) {
        daNap = true;
        int n = k.size();
        // sap xep on dinh, khoa dai truoc (giong Unity / Collections.sort ben Java): chen
        int[] thuTu = new int[n];
        for (int i = 0; i < n; i++) {
            thuTu[i] = i;
        }
        for (int i = 1; i < n; i++) {
            int x = thuTu[i];
            int lx = boBang((String) k.elementAt(x)).length();
            int j = i - 1;
            while (j >= 0 && boBang((String) k.elementAt(thuTu[j])).length() < lx) {
                thuTu[j + 1] = thuTu[j];
                j--;
            }
            thuTu[j + 1] = x;
        }
        phanBiet = new Hashtable();
        khongPhanBiet = new Hashtable();
        Vector dsKhoa = new Vector();
        Vector dsPb = new Vector();
        for (int i = 0; i < n; i++) {
            String k0 = (String) k.elementAt(thuTu[i]);
            String moi = (String) v.elementAt(thuTu[i]);
            String kk = boBang(k0);
            if (kk.length() == 0) {
                continue;
            }
            if (k0.startsWith("=")) {
                if (phanBiet.containsKey(kk)) {
                    continue;
                }
                phanBiet.put(kk, moi);
                dsPb.addElement(Boolean.TRUE);
            } else {
                String t = thuong(kk);
                if (khongPhanBiet.containsKey(t)) {
                    continue;
                }
                khongPhanBiet.put(t, moi);
                dsPb.addElement(Boolean.FALSE);
            }
            dsKhoa.addElement(kk);
        }
        int m = dsKhoa.size();
        khoa = new String[m];
        phanBietHoa = new boolean[m];
        Hashtable tam = new Hashtable();
        for (int i = 0; i < m; i++) {
            khoa[i] = (String) dsKhoa.elementAt(i);
            phanBietHoa[i] = ((Boolean) dsPb.elementAt(i)).booleanValue();
            Character c = new Character(thuong(khoa[i].charAt(0)));
            Vector ds = (Vector) tam.get(c);
            if (ds == null) {
                ds = new Vector();
                tam.put(c, ds);
            }
            ds.addElement(new Integer(i));
        }
        theoChuDau = new Hashtable();
        java.util.Enumeration e = tam.keys();
        while (e.hasMoreElements()) {
            Object c = e.nextElement();
            Vector ds = (Vector) tam.get(c);
            int[] a = new int[ds.size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = ((Integer) ds.elementAt(i)).intValue();
            }
            theoChuDau.put(c, a);
        }
        boNho.clear();
    }

    private static String boBang(String s) {
        return s.startsWith("=") ? s.substring(1) : s;
    }

    // ------------------------------------------------------------ doi chu

    private static boolean khop(String s, int i, String k, boolean pb) {
        int n = k.length();
        if (i + n > s.length()) {
            return false;
        }
        for (int j = 0; j < n; j++) {
            char a = s.charAt(i + j), b = k.charAt(j);
            if (a != b && (pb || thuong(a) != thuong(b))) {
                return false;
            }
        }
        return true;
    }

    private static String theoKieu(String goc, String moi) {
        if (moi.length() == 0) {
            return moi;
        }
        // tach theo ' ' nhu string.Split(' ') (giu tu rong)
        boolean moiTuHoa = false;
        int soTu = 1;
        for (int i = 0; i < goc.length(); i++) {
            if (goc.charAt(i) == ' ') {
                soTu++;
            }
        }
        if (soTu >= 2) {
            moiTuHoa = true;
            int dau = 0;
            for (int i = 0; i <= goc.length(); i++) {
                if (i == goc.length() || goc.charAt(i) == ' ') {
                    if (i == dau || !laHoa(goc.charAt(dau))) {
                        moiTuHoa = false;
                    }
                    dau = i + 1;
                }
            }
        }
        boolean coChu = false;
        boolean hetHoa = true;
        for (int i = 0; i < goc.length(); i++) {
            char c = goc.charAt(i);
            if (laChu(c)) {
                coChu = true;
                if (!laHoa(c)) {
                    hetHoa = false;
                }
            }
        }
        if (coChu && hetHoa && goc.length() > 1) {
            return hoa(moi);
        }
        if (moiTuHoa) {
            StringBuffer sb = new StringBuffer(moi.length());
            boolean dauTu = true;
            for (int i = 0; i < moi.length(); i++) {
                char c = moi.charAt(i);
                sb.append(dauTu ? hoa(c) : c);
                dauTu = c == ' ';
            }
            return sb.toString();
        }
        if (laHoa(goc.charAt(0)) && laThuong(moi.charAt(0))) {
            return hoa(moi.charAt(0)) + moi.substring(1);
        }
        return moi;
    }

    /** Doi bat ke co (kiem thu). */
    public static String doiLuon(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (!daNap) {
            nap();
        }
        if (khoa == null || khoa.length == 0) {
            return s;
        }
        StringBuffer sb = null;
        int n = s.length();
        int daChep = 0;
        int i = 0;
        while (i < n) {
            if (i > 0) {
                char tr = s.charAt(i - 1);
                if (laChu(tr) || tr == '_') {
                    i++;
                    continue;
                }
            }
            int[] ds = (int[]) theoChuDau.get(new Character(thuong(s.charAt(i))));
            int het = -1;
            if (ds != null) {
                for (int j = 0; j < ds.length; j++) {
                    String k = khoa[ds[j]];
                    if (khop(s, i, k, phanBietHoa[ds[j]])) {
                        int e = i + k.length();
                        if (e >= n || !laChuWord(s.charAt(e))) {
                            het = e;
                            break;
                        }
                    }
                }
            }
            if (het < 0) {
                i++;
                continue;
            }
            String g = s.substring(i, het);
            String moi = (String) phanBiet.get(g);
            if (moi == null) {
                moi = (String) khongPhanBiet.get(thuong(g));
                moi = moi == null ? g : theoKieu(g, moi);
            }
            if (sb == null) {
                sb = new StringBuffer(n + 16);
            }
            sb.append(s.substring(daChep, i)).append(moi);
            daChep = het;
            i = het;
        }
        if (sb == null) {
            return s;
        }
        sb.append(s.substring(daChep));
        return sb.toString();
    }
}
