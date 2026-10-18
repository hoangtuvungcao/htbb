/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import data.GemItem;
import io.Message;
import java.util.Vector;

public class MonsterMoba extends Monster {
    public boolean isQuaiOcdao = false;
    long timeSendMove = 0L;
    public static int timeReBorn = 6000;

    public MonsterMoba(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    public MonsterMoba(byte cat) {
        super(cat);
    }

    @Override
    public int getPointChienTruong() {
        return this.isQuaiOcdao ? 3 : 1;
    }

    @Override
    public int getTimeReborn() {
        return -1;
    }

    @Override
    public void moveOld() {
        if (!this.isMaterialMons() && !this.isCongThanh() && !this.isLienHoaTru()) {
            if (this.target != null) {
                int xx = this.x;
                int yy = this.y;
                if (Math.abs(this.x - this.target.x) > 32) {
                    if (this.x < this.target.x) {
                        xx = this.x + 32;
                    } else {
                        xx = this.x - 32;
                    }
                }

                if (Math.abs(this.y - this.target.y) > 32) {
                    if (this.y < this.target.y) {
                        yy = this.y + 16;
                    } else {
                        yy = this.y - 32;
                    }
                }

                if (!this.canMove(xx, yy)) {
                    this.x = xx;
                    this.y = yy;
                } else {
                    this.target = null;
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
            } else {
                int xxx = this.x + r.nextInt() % this.dmove;
                int yyx = this.y + r.nextInt() % this.dmove;
                if (!this.canMove(xxx, yyx)) {
                    this.x = xxx;
                    this.y = yyx;
                } else {
                    if (xxx < this.x) {
                        xxx = this.x + Math.abs(this.x - xxx) + 48;
                    } else {
                        xxx = this.x - Math.abs(this.x - xxx) - 48;
                    }

                    if (yyx < this.y) {
                        yyx = this.y + Math.abs(this.y - yyx) + 48;
                    } else {
                        yyx = this.y - Math.abs(this.y - yyx) - 48;
                    }

                    if (!this.canMove(xxx, yyx)) {
                        this.x = xxx;
                        this.y = yyx;
                    } else {
                        this.target = null;
                        this.x = this.default_x;
                        this.y = this.default_y;
                    }
                }
            }

            if (this.isBoss) {
                if (Math.abs(this.x - this.default_x) > 320 || Math.abs(this.y - this.default_y) > 320) {
                    this.target = null;
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
            } else {
                int range = 120;
                if (this.getMonsterTemplate().id == 46 && (Math.abs(this.x - this.default_x) >= 96 || Math.abs(this.y - this.default_y) >= 96)) {
                    this.x = this.default_x;
                    this.y = this.default_y;
                    this.target = null;
                }

                if (Math.abs(this.x - this.default_x) >= range || Math.abs(this.y - this.default_y) >= range) {
                    this.target = null;
                    this.x = this.default_x;
                    this.y = this.default_y;
                }
            }
        }
    }

    @Override
    public void move() {
        long now = System.currentTimeMillis();
        this.moved = false;
        if (now - this.lastTimeMove > this.moveDelay) {
            if (this.getMonsterTemplate().move == 1) {
                if (!Map.isNewVersion || this.isBoss || this.getMonsterTemplate().id == Map.idGhost || this.getMonsterTemplate().move == 0 || this.isMaterialMons()
                    )
                 {
                    this.moveOld();
                } else if (this.target != null) {
                    int xx = this.x;
                    int yy = this.y;
                    if (Math.abs(this.x - this.target.x) > 16) {
                        if (this.x > this.target.x) {
                            xx = this.target.x + 16;
                        } else {
                            xx = this.target.x - 16;
                        }
                    }

                    if (Math.abs(this.y - this.target.y) > 16) {
                        if (this.y > this.target.y) {
                            yy = this.target.y + 16;
                        } else {
                            yy = this.target.y - 16;
                        }
                    }

                    if (this.canMove(xx, yy)) {
                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.x = this.default_x;
                        this.y = this.default_y;
                        Message msg = new Message(4);
                        this.target.writeActorPos(msg, this);
                        this.target.sendMessage(msg);
                        this.target.sendToNearPlayer(msg);
                        this.target = null;
                        return;
                    }

                    this.x = xx;
                    this.y = yy;
                    short var11 = 120;
                    if (this.getMonsterTemplate().id == 46 && (Math.abs(this.x - this.default_x) >= 96 || Math.abs(this.y - this.default_y) >= 96)) {
                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.x = this.default_x;
                        this.y = this.default_y;
                        Message msg = new Message(4);
                        this.target.writeActorPos(msg, this);
                        this.target.sendMessage(msg);
                        this.target.sendToNearPlayer(msg);
                        this.target = null;
                    }

                    if (this.isBoss) {
                        var11 = 1000;
                    }

                    if (Math.abs(this.x - this.default_x) >= var11 || Math.abs(this.y - this.default_y) >= var11) {
                        this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                        this.x = this.default_x;
                        this.y = this.default_y;
                        Message msg = new Message(4);
                        this.target.writeActorPos(msg, this);
                        this.target.sendMessage(msg);
                        this.target.sendToNearPlayer(msg);
                        this.target = null;
                    }
                } else {
                    try {
                        if (System.currentTimeMillis() - this.timeSendMove >= 0L) {
                            this.timeSendMove = System.currentTimeMillis() + 5000L;

                            try {
                                Vector<Char> players = this.map.getAllPlayer(0, this.region);

                                for (int i = 0; i < players.size(); i++) {
                                    Char p = players.get(i);
                                    p.sendMessage(p.writeActorPos(new Message(4), this));
                                }
                            } catch (Exception var7) {
                            }
                        }
                    } catch (Exception var8) {
                    }
                }
            }

            this.lastTimeMove = now;
            this.moved = true;
        }
    }

    @Override
    public boolean canMove(int x, int y) {
        try {
            return (this.map.type[(y >> 4) * this.map.w + (x >> 4)] & 2) == 2 && this.target == null;
        } catch (Exception var4) {
            return true;
        }
    }

    @Override
    public int attackDam(LiveActor actor) {
        return this.attack;
    }

    @Override
    public void attack() {
        if (!this.freeze()) {
            if (this.target == null || this.target.map.equals(this.map) && this.target.region == this.region && this.target.inCountry == this.inCountry) {
                if ((this.idTemplate < 85 || this.idTemplate > 89) && this.idTemplate != 36 && this.idTemplate != 37) {
                    long now = System.currentTimeMillis();
                    if (now - this.lastTimeAttack > this.attackDelay) {
                        this.lastTimeAttack = now;

                        try {
                            Message m = new Message(10);
                            m.dos.writeShort(this.id);
                            m.dos.writeShort(this.target.id);
                            int ahp = this.attack;
                            ahp = this.target.subDam(this, ahp);
                            ahp = this.target.checkHapthuSatThuong(ahp, this);
                            ahp = this.target.checkGiamSatThuong(ahp);
                            ahp = this.target.checkPassAttack(this, ahp);
                            ahp = (int)(ahp - this.target.checkMagicShield(ahp));
                            if (ahp <= 0) {
                                ahp = 10;
                            }

                            boolean ismiss = this.attackMiss(this.target);
                            if (ismiss) {
                                ahp = 0;
                            } else {
                                this.target.buffAttackSkill(this, ahp);
                                if (this.hp <= 0) {
                                    this.actorDie();
                                }

                                this.target.downDuarable();
                            }

                            int realdam = 0;
                            if (ahp >= 32000) {
                                int defvat = this.target.defend_physic + this.target.percentBuff[0];
                                defvat += defvat * 5 / 100;
                                defvat += defvat * this.target.getEffSkillClanMember(1) / 100;
                                int defMa = this.target.defend_magic + this.target.getBuffDefCB(1, true);
                                defMa += defMa * 5 / 100;
                                defMa += defMa * this.target.getEffSkillClanMember(2) / 100;
                                int def = 0;
                                if (defMa < defvat) {
                                    def = defvat;
                                } else if (defMa > defvat) {
                                    def = defMa;
                                } else {
                                    def = defvat;
                                }

                                int dam = this.attack;
                                int deltaLV = this.level - this.target.lvDetail.lv;
                                if (deltaLV > 0) {
                                    dam += dam * Map.abs(deltaLV / 5);
                                    if (Map.abs(deltaLV) > 5 && Map.abs(deltaLV) <= 10) {
                                        def /= 2;
                                    } else if (Map.abs(deltaLV) > 10 && Map.abs(deltaLV) <= 15) {
                                        def /= 3;
                                    } else if (Map.abs(deltaLV) > 15) {
                                        def /= 10;
                                    }

                                    dam += 70 * deltaLV;
                                    def -= 20 * deltaLV;
                                } else if (deltaLV < 0) {
                                    dam += 70 * deltaLV;
                                }

                                if (dam <= 0) {
                                    dam = 5;
                                }

                                realdam = dam - def;
                                realdam = realdam * 120 / 100;
                                if (realdam <= 0) {
                                    realdam = 5;
                                }

                                if (ahp > realdam) {
                                    ahp = 10;
                                }
                            }

                            if (this.target.isAdmin) {
                                ahp = 0;
                            }

                            this.target.checkNewEffectItem(1, ahp / 10, this);
                            m.dos.writeInt(ahp);
                            this.target.hp = this.target.hp - Map.abs(ahp);
                            if (this.target.map.isMapTrain() && this.target.isHieuUngCoLongDuongQua() && this.target.hp <= 0) {
                                this.target.hp = 1;
                            }

                            if (this.target.hp < 0) {
                                this.target.hp = 0;
                            }

                            m.dos.writeInt(this.target.hp);

                            for (int i = 0; i < this.target.nearChars.size(); i++) {
                                Char p2 = this.target.map.getPlayerByID(this.target.nearChars.get(i));
                                if (p2 != null) {
                                    p2.sendMessage(m);
                                }
                            }

                            if (this.target.hp <= 0) {
                                this.target.doSetTimeAutoHoiSinhMapMoba();
                                this.target.desTroy();
                                this.target.actorDie();

                                try {
                                    long xp = this.target.lvDetail.getExp();
                                    long xpLost = this.target.lvDetail.getXPLost(this.target.killer, this.target);
                                    if (!this.target.isKiller) {
                                        xpLost = 0L;
                                    }

                                    Database.instance
                                        .saveOrtherLog(
                                            "",
                                            this.target.charname,
                                            realdam
                                                + "_"
                                                + ahp
                                                + "_"
                                                + this.getMonsterTemplate().name
                                                + "_"
                                                + Map.getNameMap(this.map.mapId)
                                                + "_"
                                                + this.region
                                                + "_"
                                                + this.target.region,
                                            "die"
                                        );
                                    xp -= xpLost;
                                    this.target.xpLost += xpLost;
                                    int currentlv = this.target.lvDetail.lv;
                                    this.target.lvDetail.setExp(xp, this.target.oldLv, this.target.getName(), "monster");
                                    if (this.target.lvDetail.lv <= 1) {
                                        this.target.lvDetail.lv = 1;
                                        this.target.lvDetail.percent = 0;
                                    }

                                    if (currentlv > this.target.lvDetail.lv) {
                                        this.target.lvDetail.resetExp2Lv(currentlv, this.target.killer);
                                        if (this.target.killer > 0) {
                                            Database.instance.saveOrtherLog("", this.target.charname, "tut level do dang trong che do ds " + this.target.killer, "downlv");
                                        }
                                    }

                                    this.target.actorDie();
                                    this.target.calculateAttrib();
                                    this.target.doSendProperties();
                                } catch (Exception var12) {
                                }
                            }

                            this.target.sendMessage(m);
                            m.cleanup();
                        } catch (Exception var13) {
                        }
                    }
                }
            } else {
                this.target = null;
            }
        }
    }

    @Override
    public int getXpReceive(int hpLost) {
        if (this.hp > 0 && hpLost > 0) {
            int xpGet = 0;
            int pc = hpLost * 100 / this.hp;
            if (pc >= 100) {
                return this.xp;
            } else {
                xpGet = pc * this.xp / 100;
                if (xpGet <= 0) {
                    xpGet = 1;
                }

                this.xp -= xpGet;
                if (this.xp <= 0) {
                    this.xp = 1;
                }

                return xpGet;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void addXp2Char() {
    }

    @Override
    public void initInfo() {
        this.bornTime = System.currentTimeMillis();
        this.isDead = false;
        this.timeOutPoinson = 0L;
        this.poinson = 0;
        this.maxhp = 1;
        this.hp = this.maxhp;

        try {
            this.xp = this.getMonsterTemplate().rcvXp;
        } catch (Exception var2) {
            var2.printStackTrace();
            System.out.println(this.idTemplate + " >>> TEMPLATE GATE");
        }

        this.tDelay = 0;
        this.target = null;
        this.x = this.default_x;
        this.y = this.default_y;
    }

    @Override
    public void revivalQuaiOcDao() {
        if (this.isDead) {
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

    @Override
    public void update() {
        if (this.isDead) {
            long now = System.currentTimeMillis();
            if (now > this.bornTime && this.idTemplate != 83 && !this.isQuaiOcdao) {
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
            }
        } else {
            if (System.currentTimeMillis() - this.timeOutPoinson >= this.tDelay * 1000 && this.tDelay > 0) {
                this.getXpReceive(this.poinson);
                this.hp = this.hp - this.poinson;
                this.totalTime = (byte)(this.totalTime - this.tDelay);
                this.timeOutPoinson = System.currentTimeMillis();
                if (this.totalTime == 0) {
                    this.tDelay = 0;
                    this.totalTime = 36;
                }

                if (this.hp <= 0) {
                    this.actorDie();
                    this.totalTime = 36;
                    this.tDelay = 0;
                }
            }

            EffectBuff ef = this.hashEffBuff.get(EffectBuff.TRUNG_DOC);
            if (ef != null) {
                this.getXpReceive(ef.dam);
                this.hp = this.hp - ef.dam;
                if (this.hp <= 0) {
                    this.actorDie();
                }
            }

            if (this.isCongThanh()) {
                this.target = null;
            }

            if (this.beStune) {
                if (System.currentTimeMillis() > this.timeBeStune) {
                    this.beStune = false;
                }
            } else {
                if (this.target == null) {
                    if (!this.beStune && !this.freeze()) {
                        this.move();
                    }
                } else {
                    if (this.target.getSession() == null || this.target.getSession() != null && this.target.getSession().exit) {
                        if (Map.isNewVersion) {
                            this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.x = this.default_x;
                            this.y = this.default_y;
                        }

                        this.target = null;
                        return;
                    }

                    if ((!this.target.map.equals(this.map) || this.target.region != this.region || this.target.inCountry != this.inCountry)
                        && !this.map.isvanTienTran()) {
                        if (Map.isNewVersion) {
                            this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.x = this.default_x;
                            this.y = this.default_y;
                        }

                        this.target = null;
                        return;
                    }

                    if (this.target.hp <= 0) {
                        if (Map.isNewVersion) {
                            this.target.sendMessage(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.target.sendToNearPlayer(MessageCreator.createMsgMonsterRemoveTarget(this.id));
                            this.x = this.default_x;
                            this.y = this.default_y;
                        }

                        this.target = null;
                        return;
                    }

                    if (this.target != null && this.target.hp > 0) {
                        this.move();
                        if (this.target != null
                            && (
                                this.getMonsterTemplate().move == 1 && Math.abs(this.target.x - this.x) <= 32 && Math.abs(this.target.y - this.y) <= 32
                                    || this.getMonsterTemplate().move == 0 && Math.abs(this.target.x - this.x) <= 96 && Math.abs(this.target.y - this.y) <= 96
                            )) {
                            this.target.isCheckActiveBuffGiamSatThuong();
                            this.attack();
                        }
                    } else {
                        this.move();
                    }
                }

                this.updateEffKham();
            }
        }
    }

    @Override
    public boolean isMonsterStand() {
        return MonsterTemplate.info[this.idTemplate][0] == 4;
    }

    @Override
    public boolean isMaterialMons() {
        return this.idTemplate >= 85 && this.idTemplate <= 89 || this.isMonsterStand();
    }

    @Override
    public void sendActorDie(Char p) {
        try {
            Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
        } catch (Exception var3) {
        }
    }

    @Override
    public void actorDie() {
        try {
            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + timeReBorn;
            if (this.isQuaiOcdao) {
                this.bornTime = System.currentTimeMillis() + 259200000L;
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
        } catch (Exception var2) {
        }
    }

    @Override
    public short getLevel() {
        return (short)this.level;
    }

    @Override
    public String getName() {
        return this.getMonsterTemplate().name;
    }

    @Override
    public Item dropItemAnimal(int lvChar, int cl) {
        int color = cl;
        Vector<ItemTemplates> a = new Vector<>();

        for (ItemTemplates e : Map.itemTemplateCollection.get(5)) {
            int lvfrom = lvChar - 10;
            int lvTo = lvChar + 1;
            if (e.level <= lvTo && e.level >= lvfrom && Map.isWearingAnimal(e.type)) {
                a.add(e);
            }
        }

        if (a.size() == 0) {
            return null;
        } else {
            ItemTemplates itemtemplate = a.get(Map.random(a.size()));
            Item it = null;
            it = new Item(itemtemplate, true, 0, 0, itemtemplate.id);
            if (itemtemplate.type != 15 && itemtemplate.type != 17) {
                it.magic_physic = (byte)r.nextInt(2);
                if (it.magic_physic == 0) {
                    it.atb[6] = it.atb[1];
                    it.atb[1] = (short)(it.atb[1] / 10);
                } else if (it.magic_physic == 1) {
                    it.atb[6] = (short)(it.atb[1] / 10);
                }
            }

            it.colorName = (byte)cl;
            byte[] pc = new byte[]{0, 40, 30, 20};

            for (byte i = 0; i < 9; i++) {
                it.atb[i] = (short)(it.atb[i] + it.atb[i] * pc[color] / 100);
            }

            it.level = itemtemplate.level;
            it.durable = itemtemplate.durable;
            it.mDurable = (short)(it.durable * 10);
            it.lockItem(null);
            return it;
        }
    }

    @Override
    public Vector<Item> dropItem() {
        return null;
    }

    @Override
    public Vector<GemItem> dropAllGemItem() {
        return null;
    }

    @Override
    public void checkReceivePotion(Char p) {
    }

    @Override
    public Vector<Potion> dropPotion(Char p) {
        return null;
    }

    @Override
    public GemItem dropGemItem() {
        return null;
    }

    @Override
    public Vector<GemItem> dropListGemItem() {
        return null;
    }

    @Override
    public void setTimeReBornInEvent(long time) {
    }

    @Override
    public void setTimeReBorn() {
        if (!this.isQuaiOcdao) {
            this.bornTime = System.currentTimeMillis() + 60000L;
        }

        this.isDead = true;
    }

    @Override
    public boolean isCopy() {
        return false;
    }

    @Override
    public boolean isEnemy(Char p) {
        return true;
    }

    @Override
    public boolean allWayAdd() {
        return this.getMonsterTemplate().id == 37 || this.getMonsterTemplate().id == 36 || this.isMonsterVantieu();
    }

    @Override
    public int getGoldRcv() {
        return this.getMonsterTemplate().rcvGold;
    }

    @Override
    public Vector<Actor> onDropItem(Map m, Char p) {
        if (!this.isLienHoaTru() && !this.isQuaiOcdao) {
            p.doRegentHpByBattle();
        }

        Vector<Actor> droplist = new Vector<>();
        if (this.hp <= 0) {
            if (!this.isCopy() || !this.isBoss) {
                if (this.isBoss) {
                    this.bornTime = System.currentTimeMillis() + 86400000L;
                    this.setTimeReBornInEvent(this.bornTime);
                    Database.instance.saveEvent(Map.event.getInfo());
                    Map.removeBossLocation(1);
                } else {
                    this.setTimeReBorn();
                }
            }

            this.isDead = true;
            this.target = null;
            this.actorDie();
        }

        return droplist;
    }

    @Override
    public boolean isMonsterMoba() {
        return true;
    }

    @Override
    public boolean isMonsterOcDao() {
        return this.isQuaiOcdao;
    }
}
