/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import io.Message;
import java.util.Vector;

public class NgocRong extends Monster {
    int rcvXP = 0;
    int stMapID = 0;
    public Region rg = null;

    public NgocRong(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    public NgocRong(byte cat) {
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

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public int getTimeReborn() {
        return -1;
    }

    @Override
    public void update() {
        if (this.isDead) {
            Map.removeMonsterNgocRong(this, this.inCountry);
        }
    }

    @Override
    public void move() {
    }

    @Override
    public void moveOld() {
    }

    @Override
    public Vector<Actor> onDropItem(Map m, Char p) {
        try {
            if (this.isDead) {
                Map.removeMonsterNgocRong(this, this.inCountry);
                Message msg = new Message(90);
                msg.dos.writeShort(this.id);
                msg.dos.writeByte(this.cat);
                p.sendMessage(msg);
                if (this.map != null) {
                    this.map.sendAllPlayer(msg, this.inCountry);
                }

                return new Vector<>();
            }

            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + timeReBorn;
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            Message msg = new Message(90);
            msg.dos.writeShort(this.id);
            msg.dos.writeByte(this.cat);
            p.sendMessage(msg);
            if (this.map != null) {
                this.map.sendAllPlayer(msg, this.inCountry);
            }

            this.hashEffBuff.clear();
            this.AllEffBuff.removeAllElements();
            this.map.removeDynamicMonster(this, this.inCountry, this.region);
            Map.removeMonsterNgocRong(this, this.inCountry);
            int idngoc = GemTemplate.ID_NGOC_RONG[this.idTemplate - 122];
            p.doAddGemItem(idngoc, 1, true);
            p.potions[73]--;
            Database.instance.saveOrtherLog("", p.charname, "nhat duoc 1 " + Map.gemTemplate[idngoc].name, "nhatngoc");
            p.sendMessage(MessageCreator.createCharGemItem(p));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (Exception var5) {
        }

        this.actorDie();
        return new Vector<>();
    }

    @Override
    public boolean isNgocRong() {
        return true;
    }

    @Override
    public void actorDie() {
        try {
            if (this.isDead) {
                Map.removeMonsterNgocRong(this, this.inCountry);
                return;
            }

            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + timeReBorn;
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            if (this.map != null) {
                this.map.sendAllPlayer(m, this.inCountry);
            }

            this.hashEffBuff.clear();
            this.AllEffBuff.removeAllElements();
            this.map.removeDynamicMonster(this, this.inCountry, this.region);
            Map.removeMonsterNgocRong(this, this.inCountry);
        } catch (Exception var2) {
        }
    }
}
