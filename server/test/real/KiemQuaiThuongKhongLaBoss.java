package real;

import com.htbb.pokemon.GoiPokemon;

/**
 * QUAI THUONG KHONG LA BOSS (bao loi kazuhas4 05/11/2026, "Violet 6": Arcanine / Caterpie hoang da hien nhan boss
 * "P3 4:58" nhu truong Gym). Goc: GoiPokemon.TEMPLATE_BOSS chua 48 (ban BossTrungThu cu) - nay 48 la Arcanine quai
 * thuong (isuse 1) nen BossLauDai.laBoss coi moi Arcanine la boss: pha, goi de, dem nguoc no 5 phut (mac dinh Gym).
 * Goi tu Kiem.java qua {@link #BAO}.
 */
public final class KiemQuaiThuongKhongLaBoss {

    private KiemQuaiThuongKhongLaBoss() {
    }

    public interface Bao {
        void bang(String moTa, Object mongDoi, Object thucTe);
    }

    public static Bao BAO;

    static void bang(final String m, final Object a, final Object b) {
        BAO.bang(m, a, b);
    }

    static MonsterTemplate mau(final int id, final String ten, final int hp, final int isuse) {
        final MonsterTemplate t = new MonsterTemplate();
        t.id = id;
        t.name = ten;
        t.level = 55;
        t.maxhp = hp;
        t.attack = 100;
        t.defend = 10;
        t.accurate = 600;
        t.typeMons = (byte) isuse;
        return t;
    }

    public static void chay() {
        final MonsterTemplate cu48 = Map.monsterTemplates.get(Integer.valueOf(48));
        final MonsterTemplate cu93 = Map.monsterTemplates.get(Integer.valueOf(93));
        try {
            final MonsterTemplate arcanine = mau(48, "Arcanine", 193_830, 1);
            final MonsterTemplate gengarCu = mau(93, "Gengar", 100_000, 2);
            Map.monsterTemplates.put(Integer.valueOf(48), arcanine);
            Map.monsterTemplates.put(Integer.valueOf(93), gengarCu);

            bang("template 48 (Arcanine quai thuong) khong con trong TEMPLATE_BOSS", Boolean.FALSE,
                    Boolean.valueOf(GoiPokemon.laTemplateBoss(48, 193_830)));

            final Monster hoangDa = new Monster(null, arcanine, 0, 0, 0);
            hoangDa.maxhp = 193_830;
            bang("Arcanine hoang da (isuse 1) khong phai boss -> khong pha / no / goi de", Boolean.FALSE,
                    Boolean.valueOf(BossLauDai.laBoss(hoangDa)));

            final Monster bossGym = new Monster(null, arcanine, 0, 0, 0);
            bossGym.isBoss = true;
            bang("cung template nhung dat isBoss (boss Gym / su kien) van la boss", Boolean.TRUE,
                    Boolean.valueOf(BossLauDai.laBoss(bossGym)));

            final Monster bossCu = new Monster(null, gengarCu, 0, 0, 0);
            bang("template boss cu khong phai quai thuong (isuse 2) van la boss", Boolean.TRUE,
                    Boolean.valueOf(BossLauDai.laBoss(bossCu)));

            final MonsterTemplate gengarHoangDa = mau(93, "Gengar", 100_000, 1);
            Map.monsterTemplates.put(Integer.valueOf(93), gengarHoangDa);
            final Monster gengarThuong = new Monster(null, gengarHoangDa, 0, 0, 0);
            bang("template trong TEMPLATE_BOSS nhung moc nhu quai thuong (isuse 1) -> khong phai boss", Boolean.FALSE,
                    Boolean.valueOf(BossLauDai.laBoss(gengarThuong)));

            final Monster to = new Monster(null, mau(9_902, "Quai to", GoiPokemon.HP_BOSS, 1), 0, 0, 0);
            to.maxhp = GoiPokemon.HP_BOSS;
            bang("maxhp >= HP_BOSS van la boss", Boolean.TRUE, Boolean.valueOf(BossLauDai.laBoss(to)));
        } catch (final Exception e) {
            bang("KiemQuaiThuongKhongLaBoss khong nem loi", "", String.valueOf(e));
        } finally {
            datLai(48, cu48);
            datLai(93, cu93);
        }
    }

    static void datLai(final int id, final MonsterTemplate t) {
        if (t == null) {
            Map.monsterTemplates.remove(Integer.valueOf(id));
        } else {
            Map.monsterTemplates.put(Integer.valueOf(id), t);
        }
    }
}
