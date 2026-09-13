import real.Monster;
import real.MonsterTemplate;

/**
 * Giam exp nhan duoc tu quai thuong (Monster), DONG BO moi level (13/09/2026).
 *
 * Lich su: 11/09/2026 chi giam quai tu lv60 (sau doi nguong xuong lv50). 13/09/2026
 * them nhanh rieng giam 60% cho quai duoi lv50, giu nguyen 40% cho tu lv50 tro len,
 * roi gop lai thanh MOT ti le duy nhat (`Monster.PC_TILE_EXP`) cho moi level - dau
 * con 60% (giam 40%), roi giam tiep con 30% (giam 70%), cung ngay 13/09/2026.
 *
 * Diem chinh: Monster.rcvXpSauGiam(MonsterTemplate), goi tu ca 3 noi Monster gan lai
 * this.xp (constructor, initInfo(), update() luc hoi sinh) nen ap dung cho moi duong
 * spawn/hoi sinh, khong rieng mot map nao.
 *
 * 14/09/2026: giam tiep tu 30% xuong con 5% (giam 95%).
 */
public final class KiemGiamExpQuaiLv60 {

    private static MonsterTemplate tao(final int level, final int rcvXp) {
        final MonsterTemplate t = new MonsterTemplate();
        t.level = (byte) level;
        t.rcvXp = rcvXp;
        return t;
    }

    public static void chay() {
        Kiem.nhom("Giam exp quai dong bo moi level (con 5%)");

        Kiem.bang("ti le con lai la 5% (giam 95%)",
                Integer.valueOf(5), Integer.valueOf(Monster.PC_TILE_EXP));

        Kiem.bang("quai level 1: giam con 5%",
                Integer.valueOf(50), Integer.valueOf(Monster.rcvXpSauGiam(tao(1, 1000))));

        Kiem.bang("quai level 49: giam con 5%",
                Integer.valueOf(50), Integer.valueOf(Monster.rcvXpSauGiam(tao(49, 1000))));

        Kiem.bang("quai level 50: giam con 5%",
                Integer.valueOf(50), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 1000))));

        Kiem.bang("quai level 100: van chi ap dung 1 lan giam (khong giam chong)",
                Integer.valueOf(5), Integer.valueOf(Monster.rcvXpSauGiam(tao(100, 100))));

        Kiem.bang("exp le (101) lam tron xuong dung cong thuc tile()",
                Integer.valueOf(5), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 101))));
    }

    private KiemGiamExpQuaiLv60() {
    }
}
