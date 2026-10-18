/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import io.Message;
import java.io.IOException;
import java.util.Vector;

public class BossTruRongLienDau extends Boss {
    byte wave = 0;
    long cooldown = System.currentTimeMillis();
    long timeHealth = System.currentTimeMillis();
    byte count = 0;
    private long timeSendMove;
    int idClan = -1;
    boolean haveCharKill = false;

    public BossTruRongLienDau(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
        this.attackDelay = 3000L;
        this.isBoss = true;
        this.percentDam = 200;
        this.attack = this.attack + this.attack / 3;
        this.isOpen = true;
    }

    @Override
    public int attackDam(LiveActor actor) {
        int def = actor.defend_physic + actor.percentBuff[0];
        if (actor.cat == 0) {
            if (this.magic_physic == 0) {
                def = actor.defend_magic + actor.getBuffDefCB(1, true);
                def += def * 5 / 100;
                def += def * ((Char)actor).getEffSkillClanMember(2) / 100;
            } else {
                def += def * ((Char)actor).getEffSkillClanMember(1) / 100;
            }
        }

        int dam = this.attack;
        dam -= def;
        long pcmin = 10L;
        long pcmax = 50 + this.wave;
        if (dam < actor.hp * pcmin / 100L) {
            dam = (int)(actor.hp * pcmin / 100L + Map.r.nextInt(200));
        } else if (dam > actor.hp * pcmax / 100L) {
            dam = (int)(actor.hp * pcmax / 100L + Map.r.nextInt(200));
        }

        if (def > dam) {
            def = dam / 2;
        }

        dam -= def * (5 - this.wave / 2) / 100;
        if (this.isLienHoaTru()) {
            dam += dam / 2;
        }

        if (dam <= 0) {
            dam = 1;
        }

        return dam;
    }

    @Override
    public void setTarget(Actor ac) {
    }

    @Override
    public void attack() {
        long now = System.currentTimeMillis();
        if (now - this.lastTimeAttack > this.attackDelay) {
            this.lastTimeAttack = now;

            try {
                Message m = new Message(83);
                m.dos.writeShort(this.id);
                int idSkill = Map.r.nextInt(2);
                m.dos.writeByte(idSkill);
                int ahp = this.attackDam(this.target);
                if (idSkill == 1) {
                    if (ahp < 5000) {
                        ahp = 5000 + Map.r.nextInt(100) + 10;
                    }

                    ahp *= 3;
                }

                if (idSkill == 0 && this.idTemplate == 117) {
                    boolean issend = true;
                    if (this.target.addEffBuff(EffectBuff.TREE, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0) != null) {
                        this.target.sendEffToChar(this.target);
                        issend = true;
                    }

                    if (issend) {
                        this.target.sendEffToNearChar();
                    }
                }

                ahp = this.target.checkHapthuSatThuong(ahp, this);
                ahp = this.target.checkGiamSatThuong(ahp);
                ahp = this.target.checkPassAttack(this, ahp);
                boolean demi = false;
                if (Map.r.nextInt(100) <= 5) {
                    demi = true;
                }

                demi = false;
                boolean crit = false;
                if (Map.r.nextInt(100) <= 20) {
                    crit = true;
                }

                if (crit) {
                    ahp *= 2;
                }

                boolean ismiss = this.attackMiss(this.target);
                if (ismiss) {
                    ahp = 0;
                }

                int freezz = Map.r.nextInt(100);
                if (this.idTemplate == 116
                    && idSkill == 0
                    && freezz <= 10
                    && this.idTemplate != 115
                    && !this.target.isAdmin
                    && Map.r.nextInt(100) > this.target.khamKhangBang()) {
                    this.target.khamEff[4] = System.currentTimeMillis() / 1000L;
                }

                int rd = Map.r.nextInt(2);
                Vector<Char> charid = new Vector<>();
                Vector<Integer> damAttack = new Vector<>();
                charid.add(this.target);
                if (this.target.hp - ahp > 0 && demi) {
                    ahp = this.target.hp / 2;
                    if (ahp <= 0) {
                        ahp = 1;
                    }
                }

                this.target.checkNewEffectItem(1, ahp / 10, this);
                if (!demi) {
                    ahp = (int)(ahp - this.target.checkMagicShield(ahp));
                }

                if (ahp <= 0) {
                    ahp = 10;
                }

                damAttack.add(ahp);
                int nplayer = 100;

                try {
                    Vector<Char> players1 = this.map.getAllPlayer(this.inCountry, this.region);
                    Vector<Char> players = new Vector<>();

                    for (int i = 0; i < players1.size(); i++) {
                        Char c = players1.get(i);
                        if (c.hp > 0 && this.near(c, 220) && this.isEnemy(c) && c.id != this.target.id && !c.isAdmin && c.isBot == -1) {
                            players.add(players1.get(i));
                        }
                    }

                    while (players.size() > 0) {
                        Char c = players.remove(Map.r.nextInt(players.size()));
                        if (c.hp > 0 && this.near(c, 220) && c.id != this.target.id && !c.isAdmin && c.isBot == -1) {
                            charid.add(c);
                            int dam = this.attackDam(c);
                            if (idSkill == 1) {
                                if (dam < 5000) {
                                    dam = 5000 + Map.r.nextInt(100) + 10;
                                }

                                dam *= 3;
                            }

                            if (crit) {
                                dam *= 2;
                            }

                            dam = c.checkHapthuSatThuong(dam, this);
                            dam = c.checkGiamSatThuong(dam);
                            dam = c.checkPassAttack(this, dam);
                            boolean miss = this.attackMiss(c);
                            if (miss) {
                                dam = 0;
                            }

                            if (c.hp - dam > 0 && demi) {
                                dam = c.hp / 2;
                                if (dam <= 0) {
                                    dam = 1;
                                }
                            }

                            if (!demi) {
                                dam = (int)(dam - c.checkMagicShield(dam));
                            }

                            if (dam <= 0) {
                                dam = 10;
                            }

                            damAttack.add(dam);
                            if (this.idTemplate == 116 && idSkill == 0 && freezz <= 10 && this.idTemplate == 116 && Map.r.nextInt(100) > c.khamKhangBang()) {
                                c.khamEff[4] = System.currentTimeMillis() / 1000L;
                            }
                        }

                        if (charid.size() >= nplayer) {
                            break;
                        }
                    }
                } catch (Exception var21) {
                }

                m.dos.writeByte(charid.size());

                for (int ix = 0; ix < charid.size(); ix++) {
                    Char cx = charid.get(ix);
                    m.dos.writeShort(cx.id);
                    m.dos.writeInt(damAttack.get(ix));
                    int hp = cx.hp - damAttack.get(ix);
                    if (cx.isAdmin) {
                        hp = cx.hp;
                    }

                    if (hp < 0) {
                        hp = 0;
                    } else if (idSkill != 0
                        && !this.randomMap
                        && Map.r.nextInt(100) < 50
                        && !cx.isAdmin
                        && cx.addEffBuff(EffectBuff.TREE, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0) != null) {
                        cx.sendEffToChar(cx);
                        cx.sendEffToNearChar();
                    }

                    m.dos.writeInt(hp);
                    cx.hp = hp;
                    if (cx.hp <= 0) {
                        Database.instance
                            .saveOrtherLog(
                                "",
                                cx.getName(),
                                cx.hp
                                    + "_"
                                    + damAttack.get(ix)
                                    + "_"
                                    + this.getMonsterTemplate().name
                                    + "_"
                                    + Map.getNameMap(this.map.mapId)
                                    + "_"
                                    + this.region
                                    + "_"
                                    + cx.region,
                                "die"
                            );
                    }
                }

                m.dos.writeByte(16);
                m.dos.writeByte(0);

                for (int ix = 0; ix < charid.size(); ix++) {
                    Char cxx = charid.get(ix);
                    cxx.sendMessage(m);
                    if (cxx.hp <= 0) {
                        cxx.actorDie();
                        cxx.desTroy();

                        try {
                            cxx.calculateAttrib();
                            cxx.doSendProperties();
                            Database.instance
                                .saveOrtherLog(
                                    "",
                                    cxx.charname,
                                    this.getMonsterTemplate().name + "_" + Map.getNameMap(this.map.mapId) + "_" + this.region + "_" + cxx.region,
                                    "die"
                                );
                        } catch (Exception var20) {
                            var20.printStackTrace();
                        }
                    }

                    for (int j = 0; j < cxx.nearChars.size(); j++) {
                        Char p = this.map.getPlayerByID(cxx.nearChars.get(j));
                        if (p != null && !charid.contains(p)) {
                            p.sendMessage(m);
                            if (cxx.khamEff[4] > 0L) {
                                p.sendMessage(p.sendEffKham(cxx));
                            }
                        }
                    }

                    for (int kk = charid.size() - 1; kk >= 0; kk--) {
                        Char p = charid.get(kk);

                        for (int jx = 0; jx < charid.size(); jx++) {
                            Char pp = charid.get(jx);
                            if (p.khamEff[4] > 0L) {
                                pp.sendMessage(p.sendEffKham(p));
                            }
                        }
                    }
                }

                m.cleanup();
            } catch (IOException var22) {
                var22.printStackTrace();
            }
        }
    }

    @Override
    public int getCharID() {
        return 0;
    }

    @Override
    public void update() {
        this.updateEffectBuff();
        if (!((MapLienDau)this.map).isStart) {
            this.actorDie();
            this.map.removeDynamicMonster(this, this.inCountry, 0);
        } else if (this.isDead) {
            this.idClan = -1;
            this.hp = this.maxhp;
            this.target = null;
            if (System.currentTimeMillis() > this.bornTime) {
                this.haveCharKill = false;
                this.bornTime = System.currentTimeMillis();
                this.timeLife = System.currentTimeMillis();
                this.isDead = false;
                this.hp = this.maxhp;
                this.xp = this.getMonsterTemplate().rcvXp;
                this.target = null;
                this.x = this.default_x;
                this.y = this.default_y;
                this.toX = this.x;
                this.toY = this.y;
                NpcReceiveCardLienDau npc1 = ((MapLienDau)this.map).npcReceiveCard.get(this.posTower);
                this.idClan = npc1.idClan;
                this.wave++;
                if (this.wave > 30) {
                    this.wave = 30;
                }

                this.count = 0;
            } else {
                this.count++;
                if (this.count == 5) {
                    try {
                        Message m = new Message(90);
                        m.dos.writeShort(this.id);
                        m.dos.writeByte(this.cat);
                        if (this.map != null) {
                            this.map.sendAllPlayer(m, this.inCountry, this.idregion);
                        }
                    } catch (Exception var4) {
                    }

                    this.count = 0;
                }
            }
        } else {
            NpcReceiveCardLienDau npc1 = ((MapLienDau)this.map).npcReceiveCard.get(this.posTower);
            if (this.idClan != npc1.idClan) {
                this.idClan = -1;
            }

            if (System.currentTimeMillis() - this.timeSendMove >= 0L) {
                this.timeSendMove = System.currentTimeMillis() + 5000L;

                try {
                    Vector<Char> players = this.map.getAllPlayer(!Map.openLog ? this.inCountry : 0, this.region);

                    for (int i = 0; i < players.size(); i++) {
                        Char p = players.get(i);
                        p.sendMessage(p.writeActorPos(new Message(4), this));
                    }
                } catch (Exception var5) {
                }
            }

            this.updateEffKham();
            if (!this.freeze()) {
                if (System.currentTimeMillis() - this.timeHealth > 20000L && this.hp > 0 && !this.isLienHoaTru() && this.hp < this.maxhp) {
                    this.hp = this.hp + this.maxhp * 15 / 100;
                    if (this.hp > this.maxhp) {
                        this.hp = this.maxhp;
                    }

                    this.timeHealth = System.currentTimeMillis();
                }

                if (this.target != null) {
                    if (!this.target.map.equals(this.map) || this.target.region != this.region) {
                        this.target = null;
                        return;
                    }

                    if (this.target.hp <= 0) {
                        this.target = null;
                        return;
                    }

                    if (this.target.hp > 0) {
                        if (Math.abs(this.target.x - this.x) <= (this.isLienHoaTru() ? 180 : 120)
                            && Math.abs(this.target.y - this.y) <= (this.isLienHoaTru() ? 180 : 120)) {
                            this.attack();
                            if (Map.r.nextInt(1) == 1) {
                                this.lastTimeMove = 0L;
                            }
                        } else {
                            this.lastTimeMove = 0L;
                        }
                    } else {
                        this.target = null;
                    }
                }
            }
        }
    }

    @Override
    public void move() {
    }

    @Override
    public void moveOld() {
    }

    @Override
    public String getName() {
        return this.idClan > -1 ? NpcReceiveCard.nameCountry[this.idClan] : super.getName();
    }

    @Override
    public Vector<Actor> onDropItem(Map m, Char p) {
        Vector<Actor> droplist = new Vector<>();
        if ((!this.isBoss || this.isCopy()) && this.idTemplate == Map.idGhost) {
        }

        if (this.hp <= 0) {
            this.bornTime = System.currentTimeMillis() + 70000L;
            this.isDead = true;
            this.target = null;
            this.actorDie();
        }

        this.idClan = -1;
        this.charKillBoss(p);
        return droplist;
    }

    @Override
    public synchronized void charKillBoss(Char p) {
        if (!this.haveCharKill) {
            this.bornTime = System.currentTimeMillis() + 70000L;
            this.isDead = true;
            this.haveCharKill = true;
            p.canGiveCard = this.posTower;
            Vector<Char> players = this.map.getAllPlayer(this.inCountry, this.region);

            for (int i = 0; i < players.size(); i++) {
                try {
                    Char pp = players.get(i);
                    if (pp.id != p.id && pp.myCountry != p.myCountry) {
                        pp.sendMessage(
                            MessageCreator.createMsgChat(pp.id, "Chúc mừng lãnh thổ " + NpcReceiveCard.nameCountry[p.myCountry] + " đã giành được quyền giao thẻ")
                        );
                    } else {
                        pp.sendMessage(MessageCreator.createMsgChat(pp.id, "Chúc mừng " + p.getName() + " đã giành được quyền giao thẻ"));
                    }
                } catch (Exception var5) {
                }
            }

            this.map.sendInfoChiemThanh();
        }
    }

    @Override
    public void actorDie() {
        try {
            this.isDead = true;
            this.idClan = -1;
            Database.instance
                .saveOrtherLog("", this.getName(), this.idTemplate + " chet sau " + (System.currentTimeMillis() - this.timeLife) / 1000L + " giay", "bossdie");
            this.bornTime = System.currentTimeMillis() + 70000L;
            this.timeOutPoinson = 0L;
            this.poinson = 0;
            Message m = new Message(90);
            m.dos.writeShort(this.id);
            m.dos.writeByte(this.cat);
            if (this.map != null) {
                this.map.sendAllPlayer(m, this.inCountry, this.idregion);
            }
        } catch (Exception var2) {
        }
    }

    @Override
    public boolean isBossTruRong() {
        return true;
    }

    @Override
    public boolean isEnemy(Char p) {
        return this.idClan > -1 ? p.myCountry != this.idClan : p.getIdCharThanThu() <= -1;
    }

    @Override
    public int getIDClan() {
        return this.idClan;
    }

    @Override
    public int khamKhangMu() {
        return 90;
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
        return Map.r.nextInt(100) < 20;
    }

    @Override
    public boolean haveDodge() {
        return Map.r.nextInt(100) < 25;
    }

    @Override
    public int getBackDam(int dam) {
        int pc = Map.r.nextInt(15) + 15;
        return dam * pc / 100;
    }

    @Override
    public boolean isBoss() {
        return true;
    }
}
