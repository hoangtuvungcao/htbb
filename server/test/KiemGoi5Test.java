import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import real.KiemGoi5;

/**
 * Kiem tra may do goi 5 (real.KiemGoi5).
 *
 * Goi 5 la duong duy nhat con lai co the ghi de idhorse sau goi 3, va no co mot
 * cho ghi CO DIEU KIEN ma client doc VO DIEU KIEN: byte rank bang hoi chi duoc
 * ghi khi server tra cuu duoc bang. Bang bi xoa nhung nhan vat con giu idClan
 * la lech mot byte -> client doc idhorse ra rac hoac roi vao catch (-1) -> con
 * thu bien mat.
 */
public final class KiemGoi5Test {

    private KiemGoi5Test() {
    }

    private static final int ID_THU = 8764;
    private static final int DXY = 2;

    /**
     * @param idClan     -1 la khong co bang
     * @param ghiRank    server co ghi byte rank khong (chi ghi khi clan != null)
     */
    private static byte[] dungGoi(final int idClan, final boolean ghiRank) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bos);
        d.writeShort(101);
        d.writeUTF("songvedem");
        d.writeShort(100); d.writeShort(200);        // x, y
        d.writeInt(500); d.writeInt(500); d.writeInt(200); d.writeInt(200);
        d.writeByte(1); d.writeByte(2);
        d.write(new byte[7]);                        // arrayBuff
        for (int i = 0; i < 7; ++i) {
            d.writeShort(0);                         // dem lui buff
        }
        d.writeShort(0);                             // sat thu
        d.writeByte(0);                              // kieu pk
        d.writeShort(100); d.writeShort(90);         // thu vat ly, thu phep
        d.writeByte(60); d.writeByte(3);             // cap, he
        d.writeByte(0); d.writeByte(0); d.writeByte(0);  // ngua co dien, bay, id anh ngua
        d.writeByte(5);                              // toc do
        d.writeShort(idClan);
        d.writeByte(-1);                             // isBot
        if (idClan > -1 && ghiRank) {
            d.writeByte(2);
        }
        d.writeByte(0);                              // idFashion
        for (int i = 0; i < 5; ++i) {
            d.writeShort(-1);                        // idModel
        }
        d.writeBoolean(false);                       // dang dien
        d.writeBoolean(false);                       // char monster
        d.writeByte(0); d.writeByte(0);              // quoc gia
        d.writeBoolean(true);                        // ve mu
        d.writeShort(ID_THU);
        d.writeByte(DXY);
        d.writeShort(-1); d.writeByte(0); d.writeShort(0);   // vu khi thoi trang
        d.writeShort(-1); d.writeShort(-1); d.writeShort(-1); // toc, ao, quan
        d.writeByte(1); d.writeShort(8788); d.writeByte(0);   // 1 danh hieu
        d.writeShort(-1);                            // phi phong
        d.writeByte(0); d.writeShort(0);
        d.writeByte(3); d.writeByte(3);
        d.writeByte(0); d.writeShort(0); d.writeByte(3);      // ao thoi trang
        d.flush();
        final byte[] than = bos.toByteArray();

        final ByteArrayOutputStream khung = new ByteArrayOutputStream();
        final DataOutputStream k = new DataOutputStream(khung);
        k.writeByte(5);
        k.writeShort(than.length);
        k.write(than);
        k.flush();
        return khung.toByteArray();
    }

    public static void chay() {
        Kiem.nhom("KiemGoi5 - may do goi 5");
        try {
            Kiem.dung("khong co bang -> khop het, khong thua byte",
                    KiemGoi5.phanTich(dungGoi(-1, false), ID_THU, DXY)
                            .contains("KHOP het, khong thua byte nao"));

            Kiem.dung("co bang, server ghi rank -> khop het",
                    KiemGoi5.phanTich(dungGoi(12, true), ID_THU, DXY)
                            .contains("KHOP het, khong thua byte nao"));

            // Bang khong tra cuu duoc: server bo qua byte rank, client van doc.
            final String lech = KiemGoi5.phanTich(dungGoi(12, false), ID_THU, DXY);
            Kiem.dung("co idClan nhung server khong ghi rank -> may do phai bao LECH",
                    lech.contains("LECH"));
            Kiem.sai("... va khong duoc bao KHOP", lech.contains("| KHOP"));
        } catch (final Exception e) {
            Kiem.dung("KiemGoi5 chay khong nem loi: " + e, false);
        }
    }
}
