import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import real.KiemGoi3;

/**
 * Kiem tra may do goi 3 (real.KiemGoi3).
 *
 * Dung tay goi 3 theo dung thu tu MessageCreator.createMainCharInfoMessage ghi,
 * cho 4 tinh huong bang hoi khac nhau, roi doi hoi may do phan biet duoc tinh
 * huong nao lam client doc lech id thu cuoi.
 */
public final class KiemGoi3Test {

    private KiemGoi3Test() {
    }

    private static final int ID_THU = 8764;
    private static final int DXY = 2;

    /**
     * @param idClan   -1 la khong co bang
     * @param rank     byte server ghi khi co bang (0 = bang chu hoac thanh vien rank 0)
     * @param laBangChu server chi ghi them boolean isDel khi la bang chu
     */
    private static byte[] dungGoi(final int idClan, final int rank, final boolean laBangChu)
            throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bos);
        d.writeShort(101);              // id
        d.writeUTF("songvedem");        // ten
        d.writeInt(500); d.writeInt(500); d.writeInt(200); d.writeInt(200);
        d.writeByte(1); d.writeByte(2); // kieu dau, lop
        d.writeInt(300); d.writeInt(100); d.writeInt(90);
        d.writeShort(10); d.writeShort(11); d.writeShort(12);
        d.writeByte(3);                 // he
        d.writeByte(60); d.writeShort(45);
        d.writeShort(20); d.writeShort(21); d.writeShort(22); d.writeShort(23); d.writeShort(24);
        d.writeShort(5); d.writeShort(6);
        d.writeInt(1234);               // cong hien bang hoi
        d.writeShort(7);                // bao kich
        d.writeByte(3); d.write(new byte[3]);   // ky nang
        d.writeShort(0);                // sat thu
        d.writeByte(0);                 // gioi tinh
        d.writeByte(0);                 // ngua co dien (thu cuoi tool -> 0)
        d.writeByte(0);                 // bay
        d.writeByte(0);                 // id anh ngua
        d.writeByte(5);                 // toc do
        d.writeShort(idClan);
        if (idClan > -1) {
            d.writeByte(rank);
            if (laBangChu) {
                d.writeBoolean(false);  // isDel - CHI bang chu moi duoc ghi
            }
        }
        d.writeBoolean(false);          // dang dien
        d.writeByte(30);                // gioi han tui do
        d.writeByte(0); d.writeByte(0); // quoc gia
        d.writeShort(0);                // lien tram
        d.writeInt(0);                  // cong trang
        d.writeShort(0);                // hoat dong
        d.writeInt(0);                  // diem dau truong
        d.writeUTF("");                 // ten vo/chong
        d.writeBoolean(true);           // ve mu
        d.writeShort(ID_THU);
        d.writeByte(DXY);
        // Phan duoi, theo dung thu tu server ghi.
        d.writeShort(8954); d.writeByte(1); d.writeShort(800);   // vu khi thoi trang
        d.writeShort(8801); d.writeShort(8802); d.writeShort(8803); // toc, ao, quan
        d.writeByte(2); d.write(new byte[2 * 3]);                // danh hieu
        d.writeUTF("mayChu");
        d.writeShort(8810);                                      // phi phong
        d.writeByte(0); d.writeShort(0);
        d.writeByte(3); d.writeByte(3);
        d.writeByte(0); d.writeShort(0); d.writeByte(3);          // ao thoi trang
        d.flush();
        final byte[] than = bos.toByteArray();

        final ByteArrayOutputStream khung = new ByteArrayOutputStream();
        final DataOutputStream k = new DataOutputStream(khung);
        k.writeByte(3);
        k.writeShort(than.length);
        k.write(than);
        k.flush();
        return khung.toByteArray();
    }

    public static void chay() {
        Kiem.nhom("KiemGoi3 - may do goi 3");
        try {
            final String khongBang = KiemGoi3.phanTich(dungGoi(-1, 0, false), ID_THU, DXY);
            Kiem.dung("khong co bang -> khop het, khong thua byte",
                    khongBang.contains("KHOP het, khong thua byte nao"));
            Kiem.dung("... va doc dung id vu khi thoi trang", khongBang.contains("vukhi=8954"));
            Kiem.dung("... va doc dung id phi phong", khongBang.contains("phiphong=8810"));

            Kiem.dung("la bang chu (rank 0 + isDel) -> khop",
                    KiemGoi3.phanTich(dungGoi(12, 0, true), ID_THU, DXY).contains("KHOP"));

            Kiem.dung("thanh vien rank khac 0 -> khop",
                    KiemGoi3.phanTich(dungGoi(12, 2, false), ID_THU, DXY).contains("KHOP"));

            // Cho nghi ngo: thanh vien thuong nhung rank == 0. Server khong ghi isDel,
            // client van doc vi no chi nhin rank == 0 -> lech mot byte tu do tro di.
            final String lech = KiemGoi3.phanTich(dungGoi(12, 0, false), ID_THU, DXY);
            Kiem.dung("thanh vien rank 0 (khong phai bang chu) -> may do phai bao LECH",
                    lech.contains("LECH"));
            Kiem.sai("... va khong duoc bao KHOP", lech.contains("| KHOP"));
        } catch (final Exception e) {
            Kiem.dung("KiemGoi3 chay khong nem loi: " + e, false);
        }
    }
}
