/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.HashMap;

public class MonsterManager {
    protected static MonsterManager instance;
    private final HashMap<Short, Monster> monsterID = new HashMap<>();

    protected MonsterManager() {
    }

    public static MonsterManager getInstance() {
        if (instance == null) {
            instance = new MonsterManager();
        }

        return instance;
    }

    public void put(Monster p) {
        this.monsterID.put(p.id, p);
    }

    public void remove(Monster p) {
        this.monsterID.remove(p.id);
    }

    public Monster get(short id) {
        return this.monsterID.get(id);
    }
}
