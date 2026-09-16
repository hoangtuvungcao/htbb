import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Tao bang khung RIENG cho pet trung TRANG THAI 1 (chua tien hoa): map/d_effect/24.png (Cu),
 * 25 (Rong), 26 (Yeu tinh), 27 (Doi), 28 (Dai bang) - ghi ra 24.pet .. 28.pet.
 *
 * VI SAO (16/09/2026): nam anh nay von la HIEU UNG buff dan vao nhan vat (EffectBuff.PET_1..5), con
 * actor pet cua trang thai 1 la o giu cho; tu 05/09 o giu cho muon bo anh mau khac (con de, canh
 * cut...) nen nguoi choi thay 2 con. Nay Pet.getType() coi trang thai 1 la pet-tool: server gui
 * type=5 + imgID = 24..28, client ve bang lop Pet - lop nay can bang khung DANG PET (part 7 byte,
 * duoi sequence/anim nhu bang 49); bang "24".."28" co san viet cho hieu ung (part 5 byte), pet doc
 * se lech byte. Cung cach lam voi 67/68 (tools/TaoPetLanCapThap.java): lay dung 3 o nho cua bang
 * hieu ung lam 3 khung, duoi chep tu bang pet 49.
 *
 * KHAC 67/68: nam con nay BAY (cu, rong, yeu tinh co canh, doi, dai bang) nen dy = -h - DO_CAO_BAY.
 * Client (mDataEffect.setDataType1) coi khung 0 co dy + h <= -5 la pet bay (isFly): bong ve o chan
 * nhan vat, con thu lo lung phia tren. Bang cua chinh cac pet-tool 35-44 (trang thai 2/3 cua cung
 * nam loai) deu bay o do cao 25-33 diem, nen chon 30 cho khop.
 *
 * Chay tu goc repo:
 *   javac --release 8 -encoding UTF-8 -d build/tools-classes tools/TaoPetTrangThai1.java
 *   java -cp build/tools-classes TaoPetTrangThai1
 * Khoa bang test/KiemPetTrangThai1LaPetTool.java.
 */
public final class TaoPetTrangThai1 {

    static final String MO_TA_49 = "map/d_effect/49";
    /** Do cao bay (diem anh) - khop dai 25-33 cua bang pet-tool 35-44. */
    static final int DO_CAO_BAY = 30;
    /** {id nguon, 3 o dung lam khung, file dich}. */
    static final Object[][] VIEC = {
        {"map/d_effect/24", new int[]{0, 1, 2}, "map/d_effect/24.pet"},   // Cu
        {"map/d_effect/25", new int[]{0, 1, 2}, "map/d_effect/25.pet"},   // Rong
        {"map/d_effect/26", new int[]{0, 1, 2}, "map/d_effect/26.pet"},   // Yeu tinh
        {"map/d_effect/27", new int[]{0, 1, 2}, "map/d_effect/27.pet"},   // Doi
        {"map/d_effect/28", new int[]{0, 1, 2}, "map/d_effect/28.pet"},   // Dai bang
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
                    + oDung[0] + "," + oDung[1] + "," + oDung[2] + " cua bang " + moTaNguon
                    + ", bay cao " + DO_CAO_BAY + ".");
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
            out.writeShort(-(o[3] / 2));            // dx: can giua
            out.writeShort(-o[4] - DO_CAO_BAY);      // dy: bay cao DO_CAO_BAY tren mat dat
            out.writeByte(i);
            out.writeByte(0);
            out.writeByte(0);
        }
        out.write(b49, dauSequence49, b49.length - dauSequence49);
        out.flush();
        return bo.toByteArray();
    }

    private TaoPetTrangThai1() {
    }
}
