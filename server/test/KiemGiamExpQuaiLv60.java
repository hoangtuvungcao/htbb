import real.Monster;
import real.MonsterTemplate;

/**
 * Giam exp nhan duoc tu quai thuong (Monster), theo TUNG MOC level (13-14/09/2026).
 *
 * Lich su: 11/09/2026 chi giam quai tu lv60 (sau doi nguong xuong lv50). 13/09/2026
 * them nhanh rieng giam 60% cho quai duoi lv50, giu nguyen 40% cho tu lv50 tro len,
 * roi gop lai thanh MOT ti le duy nhat (`Monster.PC_TILE_EXP`) cho moi level - dau
 * con 60% (giam 40%), roi giam tiep con 30% (giam 70%).
 *
 * 14/09/2026: tach LAI thanh hai hang so doc lap - `PC_TILE_EXP_LV50` (quai tu cap 50,
 * hien con 10% - giam 90%) va `PC_TILE_EXP_DUOI_LV50` (quai duoi cap 50, hien con 50% -
 * giam 50%). Ten hang so cu `Monster.PC_TILE_EXP` KHONG CON TON TAI tu day - file test
 * nay tung goi thang no nen bi hong bien dich, chan ca test.bat; sua lai theo dung hai
 * hang so hien hanh. Ten class/file van giu nguyen (Kiem.java goi thang bang ten) du
 * "Lv60" khong con khop nguong that (50) - doi ten se phai sua ca noi goi, khong dang
 * lam trong pham vi sua loi bien dich nay.
 */
public final class KiemGiamExpQuaiLv60 {

    private static MonsterTemplate tao(final int level, final int rcvXp) {
        final MonsterTemplate t = new MonsterTemplate();
        t.level = (byte) level;
        t.rcvXp = rcvXp;
        return t;
    }

    public static void chay() {
        Kiem.nhom("Giam exp quai theo tung moc level (>=50 con 10%, <50 con 50%)");

        Kiem.bang("quai tu cap 50: con lai 10% (giam 90%)",
                Integer.valueOf(10), Integer.valueOf(Monster.PC_TILE_EXP_LV50));

        Kiem.bang("quai duoi cap 50: con lai 50% (giam 50%)",
                Integer.valueOf(50), Integer.valueOf(Monster.PC_TILE_EXP_DUOI_LV50));

        Kiem.bang("quai level 1 (duoi 50): giam con 50%",
                Integer.valueOf(500), Integer.valueOf(Monster.rcvXpSauGiam(tao(1, 1000))));

        Kiem.bang("quai level 49 (duoi 50): giam con 50%",
                Integer.valueOf(500), Integer.valueOf(Monster.rcvXpSauGiam(tao(49, 1000))));

        Kiem.bang("quai level 50 (moc dau tien tu 50): giam con 10%",
                Integer.valueOf(100), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 1000))));

        Kiem.bang("quai level 100: van chi ap dung 1 lan giam (khong giam chong)",
                Integer.valueOf(10), Integer.valueOf(Monster.rcvXpSauGiam(tao(100, 100))));

        Kiem.bang("exp le (101) lam tron xuong dung cong thuc tile()",
                Integer.valueOf(10), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 101))));
    }

    private KiemGiamExpQuaiLv60() {
    }
}
