import real.Monster;
import real.MonsterTemplate;

/**
 * Giam exp nhan duoc tu quai thuong (Monster), hai nguong doc lap theo level:
 *   - tu level 50 tro len: giam 40% (con 60%, PC_TILE_EXP_LV50) - yeu cau 11/09/2026.
 *   - duoi level 50: giam 60% (con 40%, PC_TILE_EXP_DUOI_LV50) - yeu cau 13/09/2026.
 * Diem chinh: Monster.rcvXpSauGiam(MonsterTemplate), goi tu ca 3 noi Monster gan lai
 * this.xp (constructor, initInfo(), update() luc hoi sinh) nen ap dung cho moi duong
 * spawn/hoi sinh, khong rieng mot map nao.
 */
public final class KiemGiamExpQuaiLv60 {

    private static MonsterTemplate tao(final int level, final int rcvXp) {
        final MonsterTemplate t = new MonsterTemplate();
        t.level = (byte) level;
        t.rcvXp = rcvXp;
        return t;
    }

    public static void chay() {
        Kiem.nhom("Giam exp quai theo level (duoi 50 va tu 50 tro len)");

        Kiem.bang("tu level 50: ti le con lai la 60% (giam 40%)",
                Integer.valueOf(60), Integer.valueOf(Monster.PC_TILE_EXP_LV50));

        Kiem.bang("duoi level 50: ti le con lai la 40% (giam 60%)",
                Integer.valueOf(40), Integer.valueOf(Monster.PC_TILE_EXP_DUOI_LV50));

        Kiem.bang("quai level 49 (duoi 50): giam con 40%",
                Integer.valueOf(400), Integer.valueOf(Monster.rcvXpSauGiam(tao(49, 1000))));

        Kiem.bang("quai level 1 (duoi 50): giam con 40%",
                Integer.valueOf(400), Integer.valueOf(Monster.rcvXpSauGiam(tao(1, 1000))));

        Kiem.bang("quai level 50 (dung nguong): giam con 60%, khong phai 40%",
                Integer.valueOf(600), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 1000))));

        Kiem.bang("quai level 51 (tren nguong): giam con 60%",
                Integer.valueOf(600), Integer.valueOf(Monster.rcvXpSauGiam(tao(51, 1000))));

        Kiem.bang("quai level 100: van chi ap dung 1 lan giam (khong giam chong)",
                Integer.valueOf(60), Integer.valueOf(Monster.rcvXpSauGiam(tao(100, 100))));

        Kiem.bang("exp le (101) level 50 van lam tron xuong dung cong thuc tile()",
                Integer.valueOf(60), Integer.valueOf(Monster.rcvXpSauGiam(tao(50, 101))));

        Kiem.bang("exp le (101) level 49 lam tron xuong dung cong thuc tile() (giam 60%)",
                Integer.valueOf(40), Integer.valueOf(Monster.rcvXpSauGiam(tao(49, 101))));
    }

    private KiemGiamExpQuaiLv60() {
    }
}
