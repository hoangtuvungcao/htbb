import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Tao bang khung RIENG cho hai anh hoa hinh pet Lan cap thap: map/d_effect/67.png (cap 1-3) va
 * map/d_effect/68.png (cap 4) - ghi ra 67.pet va 68.pet. Cung ly do voi 69.pet
 * (xem tools/TaoPetLan.java): bang mo ta "67"/"68" goc viet cho duong ve hieu ung (danh hieu),
 * pet doc truc tiep se chia cho 0 va bien mat.
 *
 * KHAC VOI 69: anh 69.png co san 3 o DONG KICH THUOC (49x41, o 10/11/12) la 3 tu the noi tiep
 * cua MOT con vat - mot chu ky di that. Anh 67.png (16 o, deu 38x36) va 68.png (16 o, hai nhom
 * deu nhau 41x38 va 40x42) la cac o hieu ung/tia lua DOC LAP, khong co chu ky di. Dung tam 3 o
 * dau (0,1,2, cung nhom kich thuoc) lam 3 "khung" - pet se dung yen nhap nhay hieu ung thay vi
 * di that nhu 69. Da xac nhan voi nguoi yeu cau (xem lich su chat 02/09/2026) va duoc chap nhan
 * tam thoi cho toi khi co anh dang di dung cau truc nhu 69.
 *
 * Chay tu goc repo:
 *   javac --release 8 -encoding UTF-8 -d build/tools-classes tools/TaoPetLanCapThap.java
 *   java -cp build/tools-classes TaoPetLanCapThap
 */
public final class TaoPetLanCapThap {

    static final String MO_TA_49 = "map/d_effect/49";
    /** {id nguon, 3 o dung lam khung, file dich}. */
    static final Object[][] VIEC = {
        {"map/d_effect/67", new int[]{0, 1, 2}, "map/d_effect/67.pet"},
        {"map/d_effect/68", new int[]{0, 1, 2}, "map/d_effect/68.pet"},
    };

    public static void main(final String[] args) throws IOException {
        final byte[] b49 = Files.readAllBytes(new File(MO_TA_49).toPath());
        for (final Object[] viec : VIEC) {
            final String moTaNguon = (String) viec[0];
            final int[] oDung = (int[]) viec[1];
            final String dich = (String) viec[2];
            final byte[] bNguon = Files.readAllBytes(new File(moTaNguon).toPath());
            final byte[] moTa = dungMoTa(b49, docONho(bNguon), oDung);
            Files.write(new File(dich).toPath(), moTa);
            System.out.println("Ghi " + dich + " (" + moTa.length + " byte): 3 khung tu o "
                    + oDung[0] + "," + oDung[1] + "," + oDung[2] + " cua bang " + moTaNguon + ".");
        }
    }

    /** Doc danh sach o nho (id,x,y,w,h - 5 byte khong dau moi o) o dau bang mo ta. */
    static int[][] docONho(final byte[] b) {
        final int n = b[0] & 0xFF;
        final int[][] ra = new int[n][5];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < 5; k++) {
                ra[i][k] = b[1 + i * 5 + k] & 0xFF;
            }
        }
        return ra;
    }

    static byte[] dungMoTa(final byte[] b49, final int[][] oNguon, final int[] oDung) throws IOException {
        if ((b49[0] & 0xFF) != 3) {
            throw new IllegalStateException("Bang 49 phai co 3 o nho, thay " + (b49[0] & 0xFF));
        }
        if (oNguon.length <= oDung[2]) {
            throw new IllegalStateException("Bang nguon chi co " + oNguon.length + " o, can toi o " + oDung[2]);
        }
        final int dauSequence49 = 1 + 3 * 5 + 2 + 3 * 8;
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(bo);
        out.writeByte(3);
        for (int i = 0; i < 3; i++) {
            final int[] o = oNguon[oDung[i]];
            out.writeByte(i);
            out.writeByte(o[1]);
            out.writeByte(o[2]);
            out.writeByte(o[3]);
            out.writeByte(o[4]);
        }
        out.writeShort(3);
        for (int i = 0; i < 3; i++) {
            final int[] o = oNguon[oDung[i]];
            out.writeByte(1);
            out.writeShort(-(o[3] / 2));
            out.writeShort(-o[4]);
            out.writeByte(i);
            out.writeByte(0);
            out.writeByte(0);
        }
        out.write(b49, dauSequence49, b49.length - dauSequence49);
        out.flush();
        return bo.toByteArray();
    }

    private TaoPetLanCapThap() {
    }
}
