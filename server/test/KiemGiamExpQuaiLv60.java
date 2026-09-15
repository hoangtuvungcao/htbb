import real.Char;
import real.LevelDetail;
import real.Monster;
import real.MonsterTemplate;

/**
 * Giam exp nhan duoc khi giet quai thuong, theo CAP NHAN VAT dang nhan EXP (doi
 * 15/09/2026 - truoc do giam nham theo cap CON QUAI).
 *
 * Lich su: 11/09/2026 chi giam quai tu lv60 (sau doi nguong xuong lv50). 13/09/2026
 * them nhanh rieng giam 60% cho quai duoi lv50, giu nguyen 40% cho tu lv50 tro len,
 * roi gop lai thanh MOT ti le duy nhat (`Monster.PC_TILE_EXP`) cho moi level - dau
 * con 60% (giam 40%), roi giam tiep con 30% (giam 70%). 14/09/2026 tach LAI thanh
 * hai hang so doc lap PC_TILE_EXP_LV50 / PC_TILE_EXP_DUOI_LV50 - nhung ca giai doan
 * nay deu khoa theo t.level (CAP CUA CON QUAI) trong Monster.rcvXpSauGiam(), tai
 * thoi diem quai SPAWN - sai voi yeu cau that (nguoi dung xac nhan 15/09/2026):
 * phai khoa theo CAP NHAN VAT dang train, bat ke dang danh quai cap nao.
 *
 * 15/09/2026: Monster.rcvXpSauGiam() thoi khong con giam gi (tra thang t.rcvXp);
 * giam chuyen sang Monster.apDungGiamExpTheoCapNV(xp, nguoiNhan), goi o tung diem
 * cap EXP that su cho nguoi choi (7 file real/*.java: Region, RegionVantienTran,
 * MapTown, MapLienDau, MapDanhNhau, MapClan, CharCopy), ap dung DONG NHAT ca Boss
 * (khong con mien tru Boss nhu truoc). Hai hang so PC_TILE_EXP_LV50/DUOI_LV50 va
 * khoa cau_hinh (sv.tileExpLv50/sv.tileExpDuoiLv50) giu nguyen ten, chi doi Y NGHIA.
 */
public final class KiemGiamExpQuaiLv60 {

    private static MonsterTemplate tao(final int level, final int rcvXp) {
        final MonsterTemplate t = new MonsterTemplate();
        t.level = (byte) level;
        t.rcvXp = rcvXp;
        return t;
    }

    private static Char nguoiChoiCapLv(final int lv) {
        final Char c = new Char(null);
        if (c.lvDetail == null) {
            c.lvDetail = new LevelDetail();
        }
        c.lvDetail.lv = lv;
        return c;
    }

    public static void chay() {
        Kiem.nhom("Giam exp giet quai theo CAP NHAN VAT (khong phai cap quai) - doi 15/09/2026");

        Kiem.bang("nhan vat tu cap 50: con lai 10% (giam 90%)",
                Integer.valueOf(10), Integer.valueOf(Monster.PC_TILE_EXP_LV50));

        Kiem.bang("nhan vat duoi cap 50: con lai 50% (giam 50%)",
                Integer.valueOf(50), Integer.valueOf(Monster.PC_TILE_EXP_DUOI_LV50));

        // rcvXpSauGiam() KHONG con giam theo cap quai nua - tra thang rcvXp goc,
        // bat ke quai level bao nhieu.
        Kiem.bang("rcvXpSauGiam: quai level 1 giu nguyen, khong giam",
                Integer.valueOf(1000), Integer.valueOf(Monster.rcvXpSauGiam(tao(1, 1000))));
        Kiem.bang("rcvXpSauGiam: quai level 49 giu nguyen, khong giam",
                Integer.valueOf(1000), Integer.valueOf(Monster.rcvXpSauGiam(tao(49, 1000))));
        Kiem.bang("rcvXpSauGiam: quai level 50 giu nguyen, khong giam",
                Integer.valueOf(1000), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 1000))));
        Kiem.bang("rcvXpSauGiam: quai level 100 giu nguyen, khong giam",
                Integer.valueOf(100), Integer.valueOf(Monster.rcvXpSauGiam(tao(100, 100))));

        // apDungGiamExpTheoCapNV() moi khoa theo CAP NGUOI NHAN, danh quai cap nao cung vay.
        Kiem.bang("nhan vat cap 1 (duoi 50), danh quai cap 100: van giam theo NGUOI (con 50%)",
                Integer.valueOf(500), Integer.valueOf(Monster.apDungGiamExpTheoCapNV(1000, nguoiChoiCapLv(1))));
        Kiem.bang("nhan vat cap 49 (duoi 50): con 50%",
                Integer.valueOf(500), Integer.valueOf(Monster.apDungGiamExpTheoCapNV(1000, nguoiChoiCapLv(49))));
        Kiem.bang("nhan vat cap 50 (moc dau tien tu 50): con 10%",
                Integer.valueOf(100), Integer.valueOf(Monster.apDungGiamExpTheoCapNV(1000, nguoiChoiCapLv(50))));
        Kiem.bang("nhan vat cap 100, danh quai cap 1: van bi giam theo NGUOI (con 10%)",
                Integer.valueOf(10), Integer.valueOf(Monster.apDungGiamExpTheoCapNV(100, nguoiChoiCapLv(100))));
        Kiem.bang("exp le (101) lam tron xuong dung cong thuc tile()",
                Integer.valueOf(10), Integer.valueOf(Monster.apDungGiamExpTheoCapNV(101, nguoiChoiCapLv(50))));
    }

    private KiemGiamExpQuaiLv60() {
    }
}
