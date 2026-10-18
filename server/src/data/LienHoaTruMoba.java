/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Message;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import real.Actor;
import real.Char;
import real.CharBeAttack;
import real.CharChienTruong;
import real.CharCopyMoba;
import real.EffectBuff;
import real.LiveActor;
import real.Map;
import real.MapChienTruongMoba;
import real.MessageCreator;
import real.Monster;
import real.MonsterTemplate;
import real.RegionMapMoba;
import server.TeamServer;

public class LienHoaTruMoba extends LienHoaTru {
    public byte team = -1;
    public Vector<Monster> allTru = new Vector<>();
    Hashtable<String, CharBeAttack> allCharBeAtk = new Hashtable<>();
    int findPos = 0;
    private long timeSendMove;

    public LienHoaTruMoba(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
    }

    @Override
    public void setTeam(int team) {
        this.team = (byte)team;
    }

    @Override
    public int getTeam() {
        return this.team;
    }

    @Override
    public int getPointChienTruong() {
        return 10;
    }

    @Override
    public void setpos(int pos) {
        this.posTower = (byte)pos;
    }

    @Override
    public String getName() {
        return this.getMonsterTemplate().name;
    }

    @Override
    public void setTru(Vector<Monster> tru) {
        for (int i = 0; i < tru.size(); i++) {
            this.allTru.add(tru.get(i));
        }
    }

    @Override
    public boolean canAttack(Char p) {
        if (this.isDead || this.hp <= 0) {
            return false;
        } else if (p.isCharCopy() && ((CharCopyMoba)p).team != this.team) {
            return true;
        } else {
            CharChienTruong c = MapChienTruongMoba.all_char_chien_truong.get(p.charname);
            if (c == null || c.team == this.team) {
                return false;
            } else if (this.posTower == MapChienTruongMoba.P_MID_MAIN) {
                RegionMapMoba rg = this.map.getRegionMoba(this.region);
                if (rg == null) {
                    return false;
                } else {
                    if (this.team == 1) {
                        if (rg.isAllTruDie(rg.allTruBot1) && rg.isAllTruDie(rg.allTruTop1) && rg.isAllTruDie(rg.allTruMid1)) {
                            return true;
                        }
                    } else if (this.team == 0 && rg.isAllTruDie(rg.allTruBot) && rg.isAllTruDie(rg.allTruTop) && rg.isAllTruDie(rg.allTruMid)) {
                        return true;
                    }

                    return false;
                }
            } else if (this.allTru.size() == 0) {
                return true;
            } else {
                for (int i = 0; i < this.allTru.size(); i++) {
                    if (!this.allTru.get(i).isDead || this.allTru.get(i).hp > 0) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    @Override
    public int attackDam(LiveActor actor) {
        return this.attack;
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
                m.dos.writeByte(0);
                int ahp = this.attack;
                CharChienTruong cct = MapChienTruongMoba.getCharChienTruong(this.target.getName());
                if (cct != null) {
                    int pcup = cct.getBuffHeKim();
                    ahp -= ahp * pcup / 100;
                }

                CharBeAttack cbatk = this.allCharBeAtk.get(this.target.getName());
                if (cbatk == null) {
                    cbatk = new CharBeAttack();
                    cbatk.charname = this.target.charname;
                    cbatk.timeBeAttack = System.currentTimeMillis();
                    this.allCharBeAtk.put(cbatk.charname, cbatk);
                } else {
                    if (System.currentTimeMillis() - cbatk.timeBeAttack <= 8000L) {
                        cbatk.dam *= 5;
                    } else {
                        cbatk.dam = 1;
                    }

                    cbatk.timeBeAttack = System.currentTimeMillis();
                }

                long pcdam = cbatk.dam;
                ahp = (int)(ahp + ahp * pcdam / 100L);
                ahp = this.target.checkHapthuSatThuong(ahp, this);
                ahp = this.target.checkGiamSatThuong(ahp);
                ahp = this.target.checkPassAttack(this, ahp);
                boolean demi = false;
                Map.r.nextInt(100);
                boolean crit = false;
                if (Map.r.nextInt(100) <= 20) {
                    crit = true;
                }

                if (crit) {
                    ahp *= 3;
                }

                int freezz = Map.r.nextInt(100);
                if (freezz <= 10 && Map.r.nextInt(100) > this.target.khamKhangBang()) {
                    this.target.khamEff[4] = System.currentTimeMillis() / 1000L;
                }

                int rd = Map.r.nextInt(2);
                Vector<Char> charid = new Vector<>();
                Vector<Integer> damAttack = new Vector<>();
                charid.add(this.target);
                if (this.target.hp - ahp > 0 && demi) {
                    ahp = this.target.hp - 1;
                }

                if (!demi) {
                    ahp = (int)(ahp - this.target.checkMagicShield(ahp));
                }

                if (ahp <= 0) {
                    ahp = 10;
                }

                CharChienTruong cc = MapChienTruongMoba.getCharChienTruong(this.target.getName());
                if (cc != null) {
                    int a = cc.getBuffHeTho();
                    if (a == 1) {
                        ahp = 0;
                    } else if (a == 2) {
                        ahp /= 2;
                    }
                }

                damAttack.add(ahp);
                int nplayer = 100;

                try {
                    Vector<Char> players1 = this.map.getAllPlayer(this.inCountry, this.region);
                    Vector<Char> players = new Vector<>();

                    for (int i = 0; i < players1.size(); i++) {
                        Char c = players1.get(i);
                        cct = MapChienTruongMoba.getCharChienTruong(c.charname);
                        if (c.hp > 0
                            && this.near(c, 220)
                            && c.id != this.target.id
                            && !c.isAdmin
                            && c.region == this.region
                            && c.isBot == -1
                            && cct.team != this.team) {
                            players.add(players1.get(i));
                        }
                    }

                    while (players.size() > 0) {
                        Char c = players.remove(Map.r.nextInt(players.size()));
                        if (c.hp > 0 && this.near(c, 220) && c.id != this.target.id && !c.isAdmin && c.region == this.region && c.isBot == -1) {
                            charid.add(c);
                            int dam = this.attack;
                            cbatk = this.allCharBeAtk.get(c.getName());
                            if (cbatk == null) {
                                cbatk = new CharBeAttack();
                                cbatk.charname = c.charname;
                                cbatk.timeBeAttack = System.currentTimeMillis();
                                this.allCharBeAtk.put(cbatk.charname, cbatk);
                            } else {
                                if (System.currentTimeMillis() - cbatk.timeBeAttack <= 8000L) {
                                    cbatk.dam += 2;
                                } else {
                                    cbatk.dam = 1;
                                }

                                cbatk.timeBeAttack = System.currentTimeMillis();
                            }

                            pcdam = cbatk.dam;
                            dam = (int)(dam + dam * pcdam / 100L);
                            dam = c.checkHapthuSatThuong(dam, this);
                            dam = c.checkGiamSatThuong(dam);
                            dam = c.checkPassAttack(this, dam);
                            boolean miss = this.attackMiss(c);
                            if (miss) {
                                dam = 0;
                            }

                            if (TeamServer.isServerLocal()) {
                                dam = 1;
                            }

                            if (c.hp - dam > 0 && demi) {
                                dam = c.hp - 1;
                            }

                            if (!demi) {
                                dam = (int)(dam - c.checkMagicShield(dam));
                            }

                            if (dam <= 0) {
                                dam = 10;
                            }

                            cc = MapChienTruongMoba.getCharChienTruong(c.getName());
                            if (cc != null) {
                                int a = cc.getBuffHeTho();
                                if (a == 1) {
                                    dam = 0;
                                } else if (a == 2) {
                                    dam /= 2;
                                }
                            }

                            damAttack.add(dam);
                            if (freezz <= 10 && Map.r.nextInt(100) > c.khamKhangBang()) {
                                c.khamEff[4] = System.currentTimeMillis() / 1000L;
                            }
                        }

                        if (charid.size() >= nplayer) {
                            break;
                        }
                    }
                } catch (Exception var24) {
                }

                this.findPos = (this.findPos + 1) % 3;
                m.dos.writeByte(charid.size());

                for (int ix = 0; ix < charid.size(); ix++) {
                    Char cx = charid.get(ix);
                    m.dos.writeShort(cx.id);
                    m.dos.writeInt(damAttack.get(ix));
                    int hp = cx.hp - damAttack.get(ix);
                    if (hp < 0) {
                        hp = 0;
                    }

                    m.dos.writeInt(hp);
                    cx.hp = hp;
                }

                m.dos.writeByte(-1);

                for (int ix = 0; ix < charid.size(); ix++) {
                    Char cx = charid.get(ix);
                    cx.sendMessage(m);
                    if (cx.hp <= 0) {
                        cx.doSetTimeAutoHoiSinhMapMoba();
                        cx.actorDie();
                        cx.desTroy();
                    }

                    for (int j = 0; j < cx.nearChars.size(); j++) {
                        Char p = this.map.getPlayerByID(cx.nearChars.get(j));
                        if (p != null && !charid.contains(p)) {
                            p.sendMessage(m);
                            if (cx.khamEff[4] > 0L) {
                                p.sendMessage(p.sendEffKham(cx));
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
            } catch (IOException var25) {
                var25.printStackTrace();
            }
        }
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

        if (!this.isDead) {
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
                        && this.map != null
                        && !this.map.isMapChienTruongMoba()) {
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
        return Map.r.nextInt(100) < 2;
    }

    @Override
    public boolean resistThroughArmor() {
        return Map.r.nextInt(100) < 20;
    }

    @Override
    public boolean haveDodge() {
        return false;
    }

    @Override
    public int getBackDam(int dam) {
        int pc = Map.r.nextInt(15) + 15;
        return dam * pc / 100;
    }

    @Override
    public boolean isMyMonster(Char p) {
        CharChienTruong c = MapChienTruongMoba.all_char_chien_truong.get(p.charname);
        return c != null && c.team == this.team;
    }

    @Override
    public boolean isEnemy(Char p) {
        CharChienTruong c = MapChienTruongMoba.all_char_chien_truong.get(p.charname);
        return c == null || c.team != this.team;
    }

    @Override
    public int getTimeReborn() {
        return -1;
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
    public boolean isLienHoaTru() {
        return true;
    }

    @Override
    public boolean isTruMoba() {
        return true;
    }

    @Override
    public Vector<Actor> onDropItem(Map m, Char p) {
        return new Vector<>();
    }

    @Override
    public boolean isMonsterMoba() {
        return true;
    }

    @Override
    public boolean isDie() {
        return this.isDead;
    }

    @Override
    public boolean canFocus(Char me) {
        CharChienTruong c = MapChienTruongMoba.getCharChienTruong(me.charname);
        return c != null ? c.team != this.team : false;
    }
}
