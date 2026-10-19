/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class MonsterSumon extends Monster {
    public MonsterSumon(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    public MonsterSumon(byte cat) {
        super(cat);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public boolean isMonsterSumon() {
        return true;
    }
}
