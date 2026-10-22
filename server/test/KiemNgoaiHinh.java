import com.htbb.pokemon.HoSoLauDai;
import com.htbb.pokemon.NgoaiHinh;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/** Kiem com.htbb.pokemon.NgoaiHinh (ngoai hinh trang bi, goi -102) - docs/thiet-ke-game.md. */
public final class KiemNgoaiHinh {

    private KiemNgoaiHinh() {
    }

    public static void chay() {
        Kiem.nhom("NgoaiHinh: phu kien trang bi tren than Pokemon (goi -102)");

        // 0.9.7 do hiem chung (tools/cauhinh/do_hiem.json, DoHiemSinh): hang phu kien = bac chung
        Kiem.bang("Trang -> thuong", 0, NgoaiHinh.hangHiem(0));
        Kiem.bang("Xanh 110% (3) -> hiem", 1, NgoaiHinh.hangHiem(3));
        Kiem.bang("Do 120% (2) -> quy", 2, NgoaiHinh.hangHiem(2));
        Kiem.bang("Hoan my 130% (1) -> su thi", 3, NgoaiHinh.hangHiem(1));
        Kiem.bang("Tim (4) -> su thi", 3, NgoaiHinh.hangHiem(4));
        Kiem.bang("6 -> huyen thoai", 4, NgoaiHinh.hangHiem(6));
        Kiem.bang("Vang (5) -> trang tri", 5, NgoaiHinh.hangHiem(5));

        Kiem.bang("cap 1 -> bac 0", 0, NgoaiHinh.bacTheoCap(1));
        Kiem.bang("cap 35 -> bac 3", 3, NgoaiHinh.bacTheoCap(35));
        Kiem.bang("cap 95 -> bac 9", 9, NgoaiHinh.bacTheoCap(95));
        Kiem.bang("cap 98 -> bac 10", 10, NgoaiHinh.bacTheoCap(98));

        Kiem.bang("bang do -> o mu", NgoaiHinh.MU, NgoaiHinh.oTheoLoai(2));
        Kiem.bang("ruy bang -> o lung", NgoaiHinh.LUNG, NgoaiHinh.oTheoLoai(19));
        Kiem.bang("nam cham (7) -> tay kieu 5", 5, NgoaiHinh.kieu(NgoaiHinh.TAY, 7, 0));
        Kiem.bang("dai khong ve len than", -1, NgoaiHinh.oTheoLoai(1));
        Kiem.bang("mu bac 8 = vuong mien (5)", 5, NgoaiHinh.kieu(NgoaiHinh.MU, 2, 8));

        // 11 mon bac 7 (cap 70), mon cam tay +13 hoan my, khan +10 do
        final int[] type = {0, 1, 2, 3, 8, 8, 9, 10, 11, 12, 19};
        final int[] cap = {70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70};
        final int[] mau = {2, 0, 3, 1, 0, 0, 0, 0, 0, 0, 5};
        final int[] plus = {10, 3, 0, 13, 0, 0, 0, 0, 0, 0, 7};
        NgoaiHinh.Du d = NgoaiHinh.tinh(type, cap, mau, plus, 0, 0, 0, 0, 0);
        Kiem.bang("bo du 11 mon cung bac", 11, d.boCungBac);
        Kiem.bang("bac tot = 7", 7, d.bacTot);
        Kiem.bang("cong tot = 13", 13, d.congTot);
        Kiem.bang("tay: hoan my cu = su thi", 3, d.hiem[NgoaiHinh.TAY]);
        Kiem.bang("khan: do = quy +10", 2 * 100 + 10, d.hiem[NgoaiHinh.KHAN] * 100 + d.cong[NgoaiHinh.KHAN]);
        Kiem.bang("lung: ao choang (bac 7)", 2, d.kieu[NgoaiHinh.LUNG]);
        Kiem.bang("co o: mu khan vong tay lung da", 0x3F, d.coO());
        Kiem.bang("khong trang phuc", 0, d.trangPhuc);

        // an mu + trang phuc: hinh mat, vang sang van theo do that
        NgoaiHinh.Du an = NgoaiHinh.tinh(type, cap, mau, plus, 801, 0, 0, 0, 1 | NgoaiHinh.AN_TRANG_PHUC);
        Kiem.bang("an mu", 0, an.kieu[NgoaiHinh.MU]);
        Kiem.bang("an trang phuc", 0, an.trangPhuc);
        Kiem.bang("an chi an hinh: cong tot giu", 13, an.congTot);
        Kiem.dung("khoa doi khi an", an.khoa() != d.khoa());

        // thoi trang: chi doi hinh
        NgoaiHinh.Du tt = NgoaiHinh.tinh(type, cap, mau, plus, 801, 900, 77, 55, 0);
        Kiem.bang("mu thoi trang", NgoaiHinh.KIEU_MU_THOI_TRANG, tt.kieu[NgoaiHinh.MU]);
        Kiem.bang("canh thoi trang thay ao choang", NgoaiHinh.KIEU_CANH, tt.kieu[NgoaiHinh.LUNG]);
        Kiem.dung("kinh tu mat na", tt.kieu[NgoaiHinh.KINH] > 0);
        Kiem.dung("trang phuc 1.." + NgoaiHinh.SO_TRANG_PHUC,
                tt.trangPhuc >= 1 && tt.trangPhuc <= NgoaiHinh.SO_TRANG_PHUC);
        Kiem.bang("bac mu giu theo do that", 7, tt.bac[NgoaiHinh.MU]);

        // khong mac gi
        NgoaiHinh.Du rong = NgoaiHinh.tinh(new int[0], new int[0], new int[0], new int[0], 0, 0, 0, 0, 0);
        Kiem.bang("khong mac: bac tot -1", -1, rong.bacTot);

        // goi -102: doc lai dung trinh tu client (PokeMang.docNgoaiHinh)
        try {
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            NgoaiHinh.ghi(new DataOutputStream(bo), 1234, d);
            final byte[] b = bo.toByteArray();
            Kiem.bang("do dai goi (6 o x 3 + 9 + 6 duoi trang tri 0.9.8)", 6 * 3 + 9 + 6, b.length);
            final DataInputStream r = new DataInputStream(new ByteArrayInputStream(b));
            Kiem.bang("charId", 1234, (int) r.readShort());
            Kiem.bang("phien ban", 1, r.readUnsignedByte());
            final int co = r.readUnsignedByte();
            int kieuTay = -1, hiemTay = -1, congTay = -1;
            for (int o = 0; o < NgoaiHinh.SO_O; o++) {
                if ((co & (1 << o)) == 0) {
                    continue;
                }
                final int k = r.readUnsignedByte();
                final int hb = r.readUnsignedByte();
                final int c = r.readUnsignedByte();
                if (o == NgoaiHinh.TAY) {
                    kieuTay = k;
                    hiemTay = hb >> 4;
                    congTay = c;
                }
            }
            Kiem.bang("tay doc lai (hoan my cu = su thi 3)", "1/3/13", kieuTay + "/" + hiemTay + "/" + congTay);
            Kiem.bang("bacTot", 7, r.readUnsignedByte());
            Kiem.bang("congTot", 13, r.readUnsignedByte());
            Kiem.bang("bo", 11, r.readUnsignedByte());
            Kiem.bang("trang phuc", 0, r.readUnsignedByte());
            Kiem.bang("an", 0, r.readUnsignedByte());
            Kiem.bang("con lai dung duoi trang tri (client cu bo qua)", 6, r.available());
            Kiem.bang("duoi trang tri: 'T' v1, khong mac gi", "84 1 0 0 0 0", r.readUnsignedByte() + " " + r.readUnsignedByte() + " "
                    + r.readUnsignedByte() + " " + r.readUnsignedByte() + " " + r.readUnsignedByte() + " " + r.readUnsignedByte());
            Kiem.bang("het goi", 0, r.available());
            final ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
            NgoaiHinh.ghi(new DataOutputStream(bo2), 1, rong);
            final byte[] b2 = bo2.toByteArray();
            Kiem.bang("khong mac: bacTot 255", 255, b2[4] & 0xFF);
        } catch (final Exception e) {
            Kiem.dung("ghi/doc goi -102: " + e, false);
        }

        // ho so dai han luu mat na an
        final HoSoLauDai h = new HoSoLauDai();
        h.ngoaiHinhAn = 0x85;
        Kiem.bang("HoSoLauDai luu 'nh'", 0x85, HoSoLauDai.giaiMa(h.maHoa()).ngoaiHinhAn);
    }
}
