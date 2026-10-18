/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Message;
import java.util.Vector;
import real.Actor;
import real.Char;
import real.Map;
import real.MonsterTemplate;

public class DragonMoba extends DragonTower {
    private long timeSendMove;

    public DragonMoba(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    @Override
    public int khamKhangMu() {
        return 50;
    }

    @Override
    public int khamKhangBang() {
        return 50;
    }

    @Override
    public int khamKhangDoc() {
        return 50;
    }

    @Override
    public int khamKhangChoang() {
        return 50;
    }

    @Override
    public int khamKhangHoathach() {
        return 50;
    }

    @Override
    public int khamKhangGiamtoc() {
        return 50;
    }

    @Override
    public boolean haveBackDam() {
        return Map.r.nextInt(100) < 20;
    }

    @Override
    public boolean resistThroughArmor() {
        return Map.r.nextInt(100) < 25;
    }

    @Override
    public boolean haveDodge() {
        return Map.r.nextInt(100) < 20;
    }

    @Override
    public int getBackDam(int dam) {
        int pc = Map.r.nextInt(25) + 15;
        return dam * pc / 100;
    }

    @Override
    public boolean isEnemy(Char p) {
        return true;
    }

    @Override
    public boolean allWayAdd() {
        return true;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public Vector<Actor> onDropItem(Map m, Char p) {
        try {
            if (Map.r.nextInt(1000) < 5) {
                Map.doCreateBookSkillPet(p, 0);
            }
        } catch (Exception var4) {
        }

        return super.onDropItem(m, p);
    }

    @Override
    public void update() {
        if (!this.isDead) {
            try {
                if (System.currentTimeMillis() - this.timeSendMove >= 0L) {
                    this.timeSendMove = System.currentTimeMillis() + 5000L;

                    try {
                        Vector<Char> players = this.map.getAllPlayer(!Map.openLog ? this.inCountry : 0, this.region);

                        for (int i = 0; i < players.size(); i++) {
                            Char p = players.get(i);
                            p.sendMessage(p.writeActorPos(new Message(4), this));
                        }
                    } catch (Exception var4) {
                    }
                }
            } catch (Exception var5) {
            }
        }

        super.update();
    }
}
