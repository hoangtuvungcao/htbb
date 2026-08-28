import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import real.KiemGoi26;

/**
 * Kiem tra chinh cai may do: real.KiemGoi26 phai doc goi 26 y het client 2.7.6.
 *
 * Neu may do sai thi ket luan no in ra tren server that cung sai, ma minh lai tin
 * theo. Nen o day dung TAY hai goi 26 - mot goi ghi so hieu ung bang SHORT (dung
 * cho client 2.7.6) va mot goi ghi bang BYTE (kieu ban cu) - roi doi hoi may do
 * phan biet duoc hai truong hop.
 */
public final class KiemGoi26Test {

    private KiemGoi26Test() {
    }

    /**
     * Mot khoi du lieu hieu ung hop le co dung soKhung khung, theo dinh dang
     * DataSkillEff.loadData: anh nho, danh sach khung, chuoi phat, bang frame nhan vat.
     */
    private static byte[] khoiHieuUng(final int soKhung) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bos);
        d.writeByte(2);                     // 2 anh nho
        d.write(new byte[2 * 5]);
        d.writeShort(soKhung);
        for (int i = 0; i < soKhung; ++i) {
            d.writeByte(1);                 // moi khung 1 manh
            d.write(new byte[7]);
        }
        d.writeByte(3);                     // chuoi phat: 3 buoc, moi buoc 1 short
        d.write(new byte[3 * 2]);
        d.writeByte(0);                     // indexStartSkill
        for (int i = 0; i < 3; ++i) {       // frameChar[0], [1], [3]
            d.writeByte(2);
            d.write(new byte[2]);
        }
        d.flush();
        return bos.toByteArray();
    }

    /**
     * Dung mot goi 26 thu nho theo dung thu tu server ghi.
     *
     * @param demBangShort ghi so luong hieu ung bang short (client >= 2.7) hay byte
     * @param idHieuUng    danh sach id hieu ung gui trong khoi 5
     */
    private static byte[] dungGoi(final boolean demBangShort, final int[] idHieuUng) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream d = new DataOutputStream(bos);
        d.writeByte(40);            // so monster
        d.writeShort(3);            // EFF_PACK
        d.write(new byte[3]);
        d.writeByte(2);             // hieu ung dong: 2 o
        for (int i = 0; i < 2; ++i) {
            d.writeShort(4);
            d.write(new byte[4]);
        }
        d.writeByte(1);             // pet: 1 o
        d.writeShort(35);
        d.writeShort(4);
        d.write(new byte[4]);
        if (demBangShort) {         // khoi 5
            d.writeShort(idHieuUng.length);
        } else {
            d.writeByte(idHieuUng.length);
        }
        for (int i = 0; i < idHieuUng.length; ++i) {
            final byte[] khoi = khoiHieuUng(21);
            d.writeShort(idHieuUng[i]);
            d.writeShort(khoi.length);
            d.write(khoi);
        }
        d.writeByte(5);             // dx_horse: 5 hang, moi hang 4+4 byte
        d.write(new byte[5 * 8]);
        for (int bang = 0; bang < 3; ++bang) {   // WP, PP, AVT: moi bang 2 hang
            for (int i = 0; i < 2; ++i) {
                d.writeByte(3);
                d.write(new byte[3]);
            }
        }
        d.flush();
        final byte[] than = bos.toByteArray();

        // Dong khung y nhu io.Message.toByteArray(): goi nho thi [lenh][do dai 2 byte].
        final ByteArrayOutputStream khung = new ByteArrayOutputStream();
        final DataOutputStream k = new DataOutputStream(khung);
        k.writeByte(26);
        k.writeShort(than.length);
        k.write(than);
        k.flush();
        return khung.toByteArray();
    }

    public static void chay() {
        Kiem.nhom("KiemGoi26 - may do goi 26");
        final int[] duThuCuoi = { 8756, 8762, 8764, 8765, 8934, 8936, 8975, 8976 };
        final int[] thieuHeo = { 8756, 8762, 8765, 8934, 8936, 8975, 8976 };
        try {
            final String tot = KiemGoi26.phanTich(dungGoi(true, duThuCuoi));
            Kiem.dung("goi dung dinh dang 2.7 -> khop het", tot.contains("khop het"));
            Kiem.dung("goi dung dinh dang 2.7 -> du id thu cuoi", tot.contains("thucuoi=du"));
            Kiem.dung("goi dung dinh dang 2.7 -> dem dung 8 hieu ung", tot.contains("eff=8"));
            Kiem.dung("goi dung dinh dang 2.7 -> giai ma duoc 21 khung cho 8764",
                    tot.contains("8764=21"));

            // Ban cu ghi byte, client 2.7.6 doc short -> hai byte dau gop lai thanh so am.
            final String cu = KiemGoi26.phanTich(dungGoi(false, duThuCuoi));
            Kiem.sai("goi kieu cu (byte) -> KHONG duoc bao khop", cu.contains("khop het"));

            final String thieu = KiemGoi26.phanTich(dungGoi(true, thieuHeo));
            Kiem.dung("thieu 8764 -> may do phai chi ra", thieu.contains("THIEU 8764"));

            // Dem khung: client bo qua khong ve neu Frame2 >= so khung, nen so nay quyet
            // dinh linh thu co hien hay khong. File hong tren VPS van lot qua phep dem id.
            Kiem.bang("dem dung so khung cua khoi du lieu hop le",
                    Integer.valueOf(21), Integer.valueOf(KiemGoi26.soKhung(khoiHieuUng(21))));
            final byte[] cut = khoiHieuUng(21);
            Kiem.bang("khoi du lieu bi cat cut -> -1",
                    Integer.valueOf(-1),
                    Integer.valueOf(KiemGoi26.soKhung(java.util.Arrays.copyOf(cut, cut.length - 5))));

            // Do dai am o khoi 5 (du lieu qua 32767 byte) phai bi bat, khong am tham bo qua.
            final byte[] hong = dungGoi(true, duThuCuoi);
            Kiem.dung("goi cat cut -> bao doc dut, khong bao khop",
                    !KiemGoi26.phanTich(java.util.Arrays.copyOf(hong, hong.length - 10))
                            .contains("khop het"));
        } catch (final Exception e) {
            Kiem.dung("KiemGoi26 chay khong nem loi: " + e, false);
        }
    }
}
