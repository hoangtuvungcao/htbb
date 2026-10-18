import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Tao bang khung RIENG cho duong ve PET cua anh map/d_effect/69.png (pet Lan hoa hinh):
 * ghi ra map/d_effect/69.pet.
 *
 * VI SAO CAN. Nguoi yeu cau muon pet Lan dung DUNG anh 69.png. Anh dung duoc, nhung bang khung
 * "69" thi KHONG: no viet cho duong ve hieu ung / danh hieu (id 8769) - 13 o nho, cac anim dai
 * 48/1/0 khung. Duong ve pet (client Pet.paint) lay bang khung tu Pet.ALL_PET_DATA (goi 26,
 * khoi id_eff_pet_tool) roi goi getAnim(action, huong).frame.length va chia lay du - anim 0
 * khung la chia cho 0, pet bien mat (su co 01/09/2026). Bang "69" dang song cho danh hieu nen
 * khong sua duoc. Hai bang di hai duong khac nhau tren client (ALL_PET_DATA cho pet,
 * ALL_DATA_EFF cho hieu ung), chi CUNG anh - nen gui cho pet mot bang khac la du.
 *
 * CACH LAM. Lay dung cau truc bang "49" (bang pet chay on: 3 o nho, 3 khung 1 part, 12
 * sequence, 12 anim, bong), chi doi:
 *   - 3 o nho -> o 10, 11, 12 cua luoi 69 (chinh la vong lap mac dinh trong "sequence" cua
 *     bang 69, moi o 49x41);
 *   - dx = -w/2 (can giua), dy = -h (chan Lan cham moc ve, khong bay nhu cac pet khac).
 * Phan sequence / anim / bong chep nguyen tu "49" vi chung chi tro toi khung 0..2.
 *
 * DataGame nap <id>.pet vao dataPetEffect; MessageCreator (goi 26) gui DataGame.moTaPet(id)
 * cho khoi id_eff_pet_tool. Xem them Pet.getIdEffPet va test/KiemHoaHinhPet.java.
 *
 * Chay tu goc repo:
 *   javac --release 8 -encoding UTF-8 -d build/tools-classes tools/TaoPetLan.java
 *   java -cp build/tools-classes TaoPetLan
 */
public final class TaoPetLan {

    static final String MO_TA_69 = "server/map/d_effect/69";
    static final String MO_TA_49 = "server/map/d_effect/49";
    static final String DICH = "server/map/d_effect/69.pet";
    /** Chi so 3 o nho trong bang 69 dung lam 3 khung cua pet. */
    static final int[] O_DUNG = {10, 11, 12};
    /** Vi tri bat dau phan sequence trong bang 49: 1 + 3 o x 5 byte + 2 byte so khung + 3 khung x 8 byte. */
    static final int DAU_SEQUENCE_49 = 1 + 3 * 5 + 2 + 3 * 8;

    public static void main(final String[] args) throws IOException {
        final byte[] b69 = Files.readAllBytes(new File(MO_TA_69).toPath());
        final byte[] b49 = Files.readAllBytes(new File(MO_TA_49).toPath());
        final byte[] moTa = dungMoTa(b49, docONho(b69));
        Files.write(new File(DICH).toPath(), moTa);
        System.out.println("Ghi " + DICH + " (" + moTa.length + " byte): 3 khung tu o "
                + O_DUNG[0] + "," + O_DUNG[1] + "," + O_DUNG[2] + " cua bang 69, dung tren dat.");
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

    static byte[] dungMoTa(final byte[] b49, final int[][] o69) throws IOException {
        if ((b49[0] & 0xFF) != 3) {
            throw new IllegalStateException("Bang 49 phai co 3 o nho, thay " + (b49[0] & 0xFF));
        }
        if (o69.length <= O_DUNG[2]) {
            throw new IllegalStateException("Bang 69 chi co " + o69.length + " o, can toi o " + O_DUNG[2]);
        }
        final ByteArrayOutputStream bo = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(bo);
        out.writeByte(3);
        for (int i = 0; i < 3; i++) {
            final int[] o = o69[O_DUNG[i]];
            out.writeByte(i);
            out.writeByte(o[1]);
            out.writeByte(o[2]);
            out.writeByte(o[3]);
            out.writeByte(o[4]);
        }
        out.writeShort(3);
        for (int i = 0; i < 3; i++) {
            final int[] o = o69[O_DUNG[i]];
            out.writeByte(1);
            out.writeShort(-(o[3] / 2));
            out.writeShort(-o[4]);
            out.writeByte(i);
            out.writeByte(0);
            out.writeByte(0);
        }
        out.write(b49, DAU_SEQUENCE_49, b49.length - DAU_SEQUENCE_49);
        out.flush();
        return bo.toByteArray();
    }

    private TaoPetLan() {
    }
}
