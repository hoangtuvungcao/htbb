/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import io.Message;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;

public class RuongMaQuai extends Monster {
    int rcvXP = 0;
    int stMapID = 0;
    public Region rg = null;

    public RuongMaQuai(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    public RuongMaQuai(byte cat) {
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
    public boolean isMaterialMons() {
        return false;
    }

    @Override
    public void update() {
        if (this.isDead && (Char.isSuKienNoel() || Char.isSuKienMini())) {
            long now = System.currentTimeMillis();
            if (now > this.bornTime && this.idTemplate != 83) {
                if (TeamServer.isServerLocal()) {
                    System.err.println("RUONGMAQUAI SONG LAI " + this.inCountry);
                }

                this.bornTime = now;
                this.isDead = false;
                this.timeOutPoinson = 0L;
                this.poinson = 0;
                this.hp = this.maxhp;
                this.xp = this.getMonsterTemplate().rcvXp;
                this.tDelay = 0;
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
                if (this.inCountry != 0) {
                    if (this.inCountry == 1) {
                        while (Map.all_ngoc_rong_hac_ho.size() > 0) {
                            Monster ms = Map.all_ngoc_rong_hac_ho.remove(0);
                            ms.actorDie();
                            Map.removeMonsterNgocRong(ms, 1);
                        }

                        Map.all_ngoc_rong_hac_ho.removeAllElements();

                        try {
                            Message m = MessageCreator.createServerAlertAutoOffMessage("Rương ma quái đã xuất hiện");
                            Map.sendAllCharServer(m, 1);
                        } catch (Exception var4) {
                        }
                    }
                } else {
                    while (Map.all_ngoc_rong_thanh_long.size() > 0) {
                        Monster ms = Map.all_ngoc_rong_thanh_long.remove(0);
                        ms.actorDie();
                        Map.removeMonsterNgocRong(ms, 0);
                    }

                    Map.all_ngoc_rong_thanh_long.removeAllElements();

                    try {
                        Message m = MessageCreator.createServerAlertAutoOffMessage("Rương ma quái đã xuất hiện");
                        Map.sendAllCharServer(m, 0);
                    } catch (Exception var5) {
                    }
                }
            }
        }
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public boolean isRuongMaquai() {
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
        if (TeamServer.isServerLocal()) {
            this.bornTime = System.currentTimeMillis() + 300000L;
        }
    }

    @Override
    public void setTimeReBorn() {
        this.bornTime = System.currentTimeMillis() + 3600000L;
        if (TeamServer.isServerLocal()) {
            this.bornTime = System.currentTimeMillis() + 300000L;
        }
    }

    @Override
    public synchronized Vector<Actor> onDropItem(Map m, Char p) {
        this.bornTime = System.currentTimeMillis() + 3600000L;
        if (TeamServer.isServerLocal()) {
            this.bornTime = System.currentTimeMillis() + 300000L;
        }

        this.actorDie();
        return new Vector<>();
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
                this.bornTime = System.currentTimeMillis() + 300000L;
            }

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
            Vector<Map> maptrain = (Vector<Map>)RealController.all_map_train.clone();
            int ngoc = 70;
            if (TeamServer.isServerLocal()) {
                ngoc = 10;
            }

            while (ngoc > 0) {
                ngoc--;
                Map map = maptrain.remove(Map.r.nextInt(maptrain.size()));
                Hashtable<Short, Monster> mons = map.getAllMons(this.inCountry, this.region);
                int idrg = 0;
                if (map.nRegion > 0) {
                    idrg = Map.r.nextInt(map.nRegion);
                }

                if (mons.size() == 0) {
                    System.err.println("map " + Map.getNameMap(map.mapId) + " " + map.mapIDLoadMap + " " + map.mapId + " ko co quai");
                } else {
                    short id = (short)Map.r.nextInt(mons.size() - mons.size() / 4);
                    Monster ms = mons.get(id);
                    map.doAddNgocRong(ms.default_x, ms.default_y, this.inCountry, idrg, 122 + Map.r.nextInt(7));
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }
    }
}
