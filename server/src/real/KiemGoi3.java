package real;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 * Doc lai goi 3 (thong tin nhan vat CUA CHINH MINH) bang DUNG trinh tu cua
 * client 2.7.6, roi so id thu cuoi ma client thuc su lay duoc voi id server
 * dinh gui.
 *
 * Vi sao can: id thu cuoi tool nam gan CUOI goi 3, sau hon 40 truong. Lech mot
 * byte o bat ky truong nao phia truoc la client doc ra id khac, hoac roi vao
 * catch va dat idhorse = -1 (goi 3 boc doan do trong try/catch rong). Ca hai
 * deu cho ra cung mot hien tuong: khong hien linh thu, khong xin anh, khong bao
 * loi. Server thi van in ra id dung nen nhin log khong the biet.
 *
 * Cho de lech nhat la khoi bang hoi:
 *     server: writeShort(idClan); neu co bang thi writeByte(rankClan);
 *             CHI KHI la bang chu moi writeBoolean(isDel)
 *     client: doc isMaster khi idClan != -1; roi doc boolean KHI isMaster == 0
 * Thanh vien thuong co rankClan == 0 se lam client doc them mot boolean khong
 * ai gui -> lech mot byte tu do tro di.
 *
 * Chi in MOT lan cho moi nhan vat, de khong lam day log.
 */
public final class KiemGoi3 {

    private KiemGoi3() {
    }

    private static final java.util.Set<String> daKiem =
            java.util.Collections.synchronizedSet(new java.util.HashSet<String>());

    /**
     * @param khung      mang byte DA DONG KHUNG cua Message (co byte lenh + do dai)
     * @param tenChar    ten nhan vat, dung de khong in trung lap
     * @param idMongDoi  id anh thu cuoi server vua ghi vao goi
     * @param dxyMongDoi index bang toa do server vua ghi vao goi
     */
    public static void kiem(final byte[] khung, final String tenChar,
            final int idMongDoi, final int dxyMongDoi) {
        if (!daKiem.add(tenChar)) {
            return;
        }
        System.out.println("[GOI3] " + tenChar + " " + phanTich(khung, idMongDoi, dxyMongDoi));
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
            d.readInt(); d.readInt();            // hp, maxhp
            d.readInt(); d.readInt();            // mp, maxmp
            d.readByte(); d.readByte();          // kieu dau, lop nhan vat
            d.readInt(); d.readInt(); d.readInt();// cong, thu vat ly, thu phep
            d.readShort(); d.readShort(); d.readShort(); // chinh xac, ne, chi mang
            d.readByte();                        // he
            d.readByte(); d.readShort();         // cap, phan tram cap
            d.readShort(); d.readShort(); d.readShort(); d.readShort(); d.readShort(); // 5 chi so
            d.readShort(); d.readShort();        // diem tiem nang, diem ky nang
            d.readInt();                         // cong hien bang hoi
            d.readShort();                       // bao kich
            final int soKyNang = d.readByte();
            d.skipBytes(soKyNang);
            d.readShort();                       // sat thu
            d.readByte();                        // gioi tinh
            final int nguaCoDien = d.readByte();
            d.readByte();                        // bay
            d.readByte();                        // id anh ngua
            d.readByte();                        // toc do

            final int idClan = d.readShort();
            // Client khoi tao isMaster = 3 va CHI ghi de khi idClan != -1.
            int isMaster = 3;
            if (idClan != -1) {
                isMaster = d.readByte();
            }
            boolean docThemBoolean = false;
            if (isMaster == 0) {
                d.readBoolean();
                docThemBoolean = true;
            }
            sb.append("bang=").append(idClan).append(" rank=").append(isMaster);
            if (docThemBoolean) {
                sb.append(" (doc them 1 boolean)");
            }

            d.readBoolean();                     // dang dien
            d.readByte();                        // gioi han tui do
            d.readByte(); d.readByte();          // quoc gia, dang o quoc gia
            d.readShort();                       // lien tram
            d.readInt();                         // cong trang
            d.readShort();                       // hoat dong
            d.readInt();                         // diem dau truong
            d.readUTF();                         // ten vo/chong
            d.readBoolean();                     // co ve mu

            int idhorse = -1;
            int typehorse = -1;
            try {
                idhorse = d.readShort();
                typehorse = d.readByte();
            } catch (final Exception e) {
                // Y HET client: het byte thi bo qua, thu cuoi coi nhu khong co.
                idhorse = -1;
                typehorse = -1;
            }
            sb.append(" nguaCoDien=").append(nguaCoDien)
              .append(" client doc idhorse=").append(idhorse)
              .append(" typehorse=").append(typehorse)
              .append(" (server gui ").append(idMongDoi).append("/").append(dxyMongDoi).append(")");

            if (idhorse == idMongDoi && typehorse == dxyMongDoi) {
                sb.append(" | KHOP");
            } else {
                sb.append(" | LECH - client KHONG ve duoc linh thu");
            }
            return sb.toString();
        } catch (final Exception e) {
            return sb.append(" | DOC DUT GIUA CHUNG: ").append(e).toString();
        }
    }
}
