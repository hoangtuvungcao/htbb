package real;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Doc lai goi 5 (thong tin mot nhan vat gui cho nguoi xung quanh) bang DUNG
 * trinh tu client 2.7.6.
 *
 * Vi sao can: goi 5 la duong DUY NHAT con lai co the ghi de idhorse sau khi goi
 * 3 da dat dung. Ben client:
 *
 *     Char charInfo = findCharByID(id);   // co the chinh la nhan vat cua minh
 *     ...
 *     charInfo.setHorse(idhorse2, typehorse2);   // catch -> -1
 *
 * Doan doc idhorse boc trong try/catch dat -1 khi het byte. Nen goi 5 lech la
 * con thu bien mat NGAY CA KHI goi 3 hoan toan dung - dung hien tuong dang thay.
 *
 * Hai cho de lech, deu do server ghi CO DIEU KIEN con client doc VO DIEU KIEN:
 *   - byte rank bang hoi: server chi ghi khi tra cuu duoc bang (clan != null),
 *     client doc bat cu khi nao idClan != -1;
 *   - bang idModel: hai ben phai cung 5 phan tu.
 *
 * Chi in MOT lan cho moi nhan vat.
 */
public final class KiemGoi5 {

    private KiemGoi5() {
    }

    private static final java.util.Set<String> daKiem =
            java.util.Collections.synchronizedSet(new java.util.HashSet<String>());

    public static void kiem(final byte[] khung, final String tenChar,
            final int idMongDoi, final int dxyMongDoi) {
        if (!daKiem.add(tenChar)) {
            return;
        }
        System.out.println("[GOI5] " + tenChar + " " + phanTich(khung, idMongDoi, dxyMongDoi));
    }

    /** Doc goi va tra ve mot dong mo ta. Khong in, khong nho trang thai. */
    public static String phanTich(final byte[] khung, final int idMongDoi, final int dxyMongDoi) {
        final StringBuilder sb = new StringBuilder();
        try {
            if (khung == null || khung.length < 3) {
                return "goi rong hoac qua ngan";
            }
            final int batDau = (khung[0] == -128) ? 6 : 3;
            final DataInputStream d = new DataInputStream(
                    new ByteArrayInputStream(khung, batDau, khung.length - batDau));

            d.readShort();                       // id
            d.readUTF();                         // ten
            d.readShort(); d.readShort();        // x, y
            d.readInt(); d.readInt();            // hp, maxhp
            d.readInt(); d.readInt();            // mp, maxmp
            d.readByte(); d.readByte();          // kieu dau, lop nhan vat
            d.skipBytes(7);                      // bang buff (arrayBuff 7 phan tu)
            d.skipBytes(7 * 2);                  // dem lui cua 7 buff
            d.readShort();                       // sat thu
            d.readByte();                        // kieu pk
            d.readShort(); d.readShort();        // thu vat ly, thu phep
            d.readByte(); d.readByte();          // cap, he
            d.readByte();                        // ngua co dien
            d.readByte();                        // bay
            d.readByte();                        // id anh ngua
            d.readByte();                        // toc do
            final int idClan = d.readShort();
            d.readByte();                        // isBot (client doc vao idBoss)
            if (idClan != -1) {
                d.readByte();                    // rank bang hoi
            }
            d.readByte();                        // idFashion
            d.skipBytes(5 * 2);                  // bang idModel, hai ben deu 5 phan tu
            d.readBoolean();                     // dang dien
            d.readBoolean();                     // la char monster / nguoi tuyet
            d.readByte(); d.readByte();          // quoc gia, dang o quoc gia
            d.readBoolean();                     // co ve mu

            int idhorse = -1;
            int typehorse = -1;
            try {
                idhorse = d.readShort();
                typehorse = d.readByte();
            } catch (final Exception e) {
                idhorse = -1;
                typehorse = -1;
            }
            sb.append("bang=").append(idClan)
              .append(" client doc idhorse=").append(idhorse)
              .append(" typehorse=").append(typehorse)
              .append(" (server gui ").append(idMongDoi).append("/").append(dxyMongDoi).append(")");

            // Ket luan ve con thu phai duoc gan NGAY, truoc khi doc phan duoi -
            // phan duoi cung se dut neu da lech, va khi do van phai noi duoc la lech.
            final boolean thuCuoiDung = (idhorse == idMongDoi && typehorse == dxyMongDoi);
            if (!thuCuoiDung) {
                sb.append(" | LECH - goi 5 se XOA con thu ma goi 3 vua dat dung");
            }
            try {
                d.readShort(); d.readByte(); d.readShort();   // vu khi thoi trang
                d.readShort(); d.readShort(); d.readShort();  // toc, ao, quan
                final int soDanhHieu = d.readByte();
                d.skipBytes(soDanhHieu * 3);
                d.readShort();                       // phi phong
                d.readByte(); d.readShort();         // index + anh lon phi phong
                d.readByte(); d.readByte();          // so khung vu khi / phi phong
                d.readByte(); d.readShort(); d.readByte();    // ao thoi trang
                final int con = d.available();
                if (con != 0) {
                    sb.append(" thua=").append(con).append("B");
                    if (thuCuoiDung) {
                        sb.append(" | id thu cuoi dung nhung doc xong con thua byte");
                    }
                } else if (thuCuoiDung) {
                    sb.append(" | KHOP het, khong thua byte nao");
                }
            } catch (final Exception e) {
                sb.append(" | phan duoi doc dut: ").append(e);
            }
            return sb.toString();
        } catch (final Exception e) {
            return sb.append(" | DOC DUT GIUA CHUNG: ").append(e).toString();
        }
    }
}
