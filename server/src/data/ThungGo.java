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
import real.Monster;
import real.MonsterTemplate;
import real.Region;
import server.TeamServer;

public class ThungGo extends Monster {
    int rcvXP = 0;
    int stMapID = 0;
    public Region rg = null;

    public ThungGo(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    public ThungGo(byte cat) {
        super(cat);
    }

    public void setInfo(int level, int maxHp, int rcvXP) {
        this.level = level;
        this.maxhp = maxHp;
        this.hp = maxHp;
        this.stMapID = this.map.getMapLoad(this.map.mapId);
        this.dmove = 96;
        this.attackDelay = 7000L;
    }

    public long getTimeLifeRuong() {
        long time = (this.bornTime - System.currentTimeMillis()) / 1000L;
        if (time < 0L) {
            time = 0L;
        }

        return time;
    }

    @Override
    public void update() {
        if (this.isDead && (Char.isSuKienHe2017() || Char.isSuKienMiniChucNu())) {
            long now = System.currentTimeMillis();
            if (now > this.bornTime && this.idTemplate != 83) {
                this.bornTime = now + 3600000L;
                this.isDead = false;
                this.timeOutPoinson = 0L;
                this.poinson = 0;
                this.hp = this.maxhp;
                this.xp = this.getMonsterTemplate().rcvXp;
                this.tDelay = 0;
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
            }
        }
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public boolean isThungGo() {
        return true;
    }

    @Override
    public int getTimeReborn() {
        return -1;
    }

    @Override
    public void move() {
    }

    @Override
    public void moveOld() {
    }

    @Override
    public void setTimeReBornInEvent(long time) {
        this.bornTime = System.currentTimeMillis() + 3600000L;
    }

    @Override
    public void setTimeReBorn() {
        this.bornTime = System.currentTimeMillis() + 3600000L;
    }

    @Override
    public synchronized Vector<Actor> onDropItem(Map m, Char p) {
        this.bornTime = System.currentTimeMillis() + 3600000L;
        if (TeamServer.isServerLocal()) {
            this.bornTime = System.currentTimeMillis() + 3600000L;
        }

        if (this.isDead) {
            return new Vector<>();
        } else {
            try {
                if (Map.r.nextInt(100) < 20) {
                    p.hp = 0;
                    p.actorDie();
                } else {
                    p.doAddGiftThungGo();
                }
            } catch (Exception var4) {
            }

            this.actorDie();
            return new Vector<>();
        }
    }

    @Override
    public synchronized void actorDie() {
        try {
            if (this.isDead) {
                return;
            }

            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + 3600000L;
            if (TeamServer.isServerLocal()) {
                this.bornTime = System.currentTimeMillis() + 3600000L;
            }

            this.timeOutPoinson = 0L;
            this.poinson = 0;
            Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            if (this.map != null) {
                this.map.sendAllPlayer(m, this.inCountry);
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }
}
