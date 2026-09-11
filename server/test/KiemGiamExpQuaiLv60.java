import real.Monster;
import real.MonsterTemplate;

/**
 * Giam 40% exp nhan duoc tu quai thuong (Monster) tu level 60 tro len - yeu cau
 * 11/09/2026. Diem chinh: Monster.rcvXpSauGiam(MonsterTemplate), goi tu ca 3 noi
 * Monster gan lai this.xp (constructor, initInfo(), update() luc hoi sinh) nen
 * ap dung cho moi duong spawn/hoi sinh, khong rieng mot map nao.
 */
public final class KiemGiamExpQuaiLv60 {

    private static MonsterTemplate tao(final int level, final int rcvXp) {
        final MonsterTemplate t = new MonsterTemplate();
        t.level = (byte) level;
        t.rcvXp = rcvXp;
        return t;
    }

    public static void chay() {
        Kiem.nhom("Giam 40% exp quai tu level 60 tro len");

        Kiem.bang("ti le con lai la 60% (giam 40%)",
                Integer.valueOf(60), Integer.valueOf(Monster.PC_TILE_EXP_LV60));

        Kiem.bang("quai level 59 (duoi 60): giu nguyen exp goc",
                Integer.valueOf(1000), Integer.valueOf(Monster.rcvXpSauGiam(tao(59, 1000))));

        Kiem.bang("quai level 60 (dung nguong): giam con 60%",
                Integer.valueOf(600), Integer.valueOf(Monster.rcvXpSauGiam(tao(60, 1000))));

        Kiem.bang("quai level 61 (tren nguong): giam con 60%",
                Integer.valueOf(600), Integer.valueOf(Monster.rcvXpSauGiam(tao(61, 1000))));

        Kiem.bang("quai level 100: van chi ap dung 1 lan giam (khong giam chong)",
                Integer.valueOf(60), Integer.valueOf(Monster.rcvXpSauGiam(tao(100, 100))));

        Kiem.bang("exp le (101) level 60 van lam tron xuong dung cong thuc tile()",
                Integer.valueOf(60), Integer.valueOf(Monster.rcvXpSauGiam(tao(60, 101))));
    }

    private KiemGiamExpQuaiLv60() {
    }
}
