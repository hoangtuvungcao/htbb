import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Doi mot (hay nhieu) chuoi hang CONSTANT_Utf8 trong mot tep .class ma KHONG dich nguoc -
 * dung de tro client J2ME da obfuscate (vd Downloads/kpah275555, class danh sach server yv)
 * ve server khac: IP server la chuoi hang trong constant pool.
 *
 *   java -cp build/tools-classes DoiChuoiClass <tep.class> <cu> <moi> [<cu2> <moi2> ...]
 *
 * Vi sao an toan khi chuoi moi khac do dai: constant pool duoc doc TUAN TU
 * ([tag][u2 do dai][bytes] voi Utf8), khong co gi trong tep .class tro theo offset tuyet
 * doi vao constant pool, nen chi can ghi lai dung u2 do dai. Moi noi khac (ldc, Code...) tro
 * theo CHI SO muc trong pool, khong doi. Xem docs/client-test-276.md.
 *
 * Tep bi ghi de tai cho - chay tren BAN SAO. Thoat ma 1 neu co chuoi <cu> khong tim thay
 * (khong ghi gi ca), de khong lam nua chung.
 */
public final class DoiChuoiClass {

    public static void main(final String[] a) throws Exception {
        if (a.length < 3 || a.length % 2 == 0) {
            System.out.println("Dung: java DoiChuoiClass <tep.class> <cu> <moi> [<cu2> <moi2> ...]");
            System.exit(2);
        }
        final File tep = new File(a[0]);
        final byte[] b = doc(tep);
        final DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(b));
        if (in.readInt() != 0xCAFEBABE) {
            System.out.println(tep + ": khong phai tep .class");
            System.exit(1);
        }
        in.readUnsignedShort(); // minor
        in.readUnsignedShort(); // major
        final int soMuc = in.readUnsignedShort();

        // Vi tri (offset, do dai ke ca tag) cua tung muc Utf8, de cat-ghep.
        final List<int[]> utf8 = new ArrayList<int[]>();
        final List<String> chuoi = new ArrayList<String>();
        int vt = 10;
        for (int i = 1; i < soMuc; i++) {
            final int tag = b[vt] & 0xFF;
            int dai;
            switch (tag) {
                case 1: { // Utf8
                    final int len = ((b[vt + 1] & 0xFF) << 8) | (b[vt + 2] & 0xFF);
                    dai = 3 + len;
                    final DataInputStream d = new DataInputStream(new java.io.ByteArrayInputStream(b, vt + 1, 2 + len));
                    chuoi.add(d.readUTF());
                    utf8.add(new int[] {vt, dai});
                    break;
                }
                case 3: case 4: dai = 5; break;              // Integer, Float
                case 5: case 6: dai = 9; i++; break;         // Long, Double (chiem 2 muc)
                case 7: case 8: case 16: case 19: case 20: dai = 3; break; // Class, String, MethodType, Module, Package
                case 9: case 10: case 11: case 12: case 17: case 18: dai = 5; break; // refs, NameAndType, (Invoke)Dynamic
                case 15: dai = 4; break;                     // MethodHandle
                default:
                    System.out.println("Tag constant pool la " + tag + " o offset " + vt + " - dung lai.");
                    System.exit(1);
                    return;
            }
            vt += dai;
        }
        final int hetPool = vt;

        // Ghep lai: [dau tep .. het pool] voi cac muc Utf8 khop duoc thay, roi phan con lai.
        final ByteArrayOutputStream ra = new ByteArrayOutputStream(b.length + 64);
        int daChep = 0;
        boolean loi = false;
        final int[] soLan = new int[(a.length - 1) / 2];
        for (int k = 0; k < utf8.size(); k++) {
            final int[] m = utf8.get(k);
            String moi = null;
            for (int p = 1; p < a.length; p += 2) {
                if (chuoi.get(k).equals(a[p])) {
                    moi = a[p + 1];
                    soLan[(p - 1) / 2]++;
                }
            }
            if (moi == null) {
                continue;
            }
            ra.write(b, daChep, m[0] - daChep);
            ra.write(1);
            final DataOutputStream dos = new DataOutputStream(ra);
            dos.writeUTF(moi); // u2 do dai + modified UTF-8, dung dinh dang CONSTANT_Utf8
            daChep = m[0] + m[1];
        }
        ra.write(b, daChep, b.length - daChep);

        for (int p = 1; p < a.length; p += 2) {
            System.out.println("  \"" + a[p] + "\" -> \"" + a[p + 1] + "\": " + soLan[(p - 1) / 2] + " muc");
            if (soLan[(p - 1) / 2] == 0) {
                loi = true;
            }
        }
        if (loi) {
            System.out.println(tep + ": co chuoi khong tim thay - KHONG ghi gi.");
            System.exit(1);
        }
        final FileOutputStream fo = new FileOutputStream(tep);
        fo.write(ra.toByteArray());
        fo.close();
        System.out.println(tep + ": " + b.length + " -> " + ra.size() + " byte (constant pool " + soMuc + " muc, het o offset " + hetPool + ").");
    }

    private static byte[] doc(final File f) throws Exception {
        final FileInputStream fi = new FileInputStream(f);
        final byte[] b = new byte[(int) f.length()];
        int n = 0;
        while (n < b.length) {
            final int r = fi.read(b, n, b.length - n);
            if (r < 0) {
                break;
            }
            n += r;
        }
        fi.close();
        return b;
    }

    private DoiChuoiClass() {
    }
}
