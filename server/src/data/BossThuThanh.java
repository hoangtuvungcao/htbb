/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Message;
import java.io.IOException;
import java.util.Vector;
import real.Char;
import real.Map;
import real.Monster;
import real.MonsterTemplate;

public class BossThuThanh extends Monster {
    int findPos = 0;
    private long timeSendMove;

    public BossThuThanh(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
        this.isBoss = true;
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

                ahp = this.target.checkHapthuSatThuong(ahp, this);
                ahp = this.target.checkGiamSatThuong(ahp);
                ahp = this.target.checkPassAttack(this, ahp);
                boolean demi = false;
                boolean crit = false;
                if (Map.r.nextInt(100) <= 20) {
                    crit = true;
                }

                if (crit) {
                    ahp *= 3;
                }

                boolean ismiss = this.attackMiss(this.target);
                if (ismiss) {
                    ahp = 0;
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

                damAttack.add(ahp);
                int nplayer = idSkill == 0 ? 20 : 30;

                try {
                    Vector<Char> players = this.map.getAllPlayer(this.inCountry, this.region);
                    if (this.findPos == 0) {
                        for (int i = 0; i < players.size(); i++) {
                            Char c = players.get(i);
                            if (c.hp > 0 && this.near(c, 220) && c.id != this.target.id && !c.isAdmin && c.isBot == -1 && c.myCountry != this.inCountry) {
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
                                    dam = c.hp - 1;
                                }

                                if (!demi) {
                                    dam = (int)(dam - c.checkMagicShield(dam));
                                }

                                if (dam <= 0) {
                                    dam = 10;
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
                    } else if (this.findPos != 1) {
                        if (this.findPos == 2) {
                            for (int i = players.size() - 1; i >= 0; i--) {
                                Char cx = players.get(i);
                                if (cx.hp > 0 && this.near(cx, 220) && cx.id != this.target.id && !cx.isAdmin && cx.isBot == -1 && cx.myCountry != this.inCountry) {
                                    charid.add(cx);
                                    int damx = this.attackDam(cx);
                                    if (idSkill == 1) {
                                        if (damx < 5000) {
                                            damx = 5000 + Map.r.nextInt(100) + 10;
                                        }

                                        damx *= 3;
                                    }

                                    if (crit) {
                                        damx *= 2;
                                    }

                                    damx = cx.checkHapthuSatThuong(damx, this);
                                    damx = cx.checkGiamSatThuong(damx);
                                    damx = cx.checkPassAttack(this, damx);
                                    boolean missx = this.attackMiss(cx);
                                    if (missx) {
                                        damx = 0;
                                    }

                                    if (cx.hp - damx > 0 && demi) {
                                        damx = cx.hp - 1;
                                    }

                                    if (!demi) {
                                        damx = (int)(damx - cx.checkMagicShield(damx));
                                    }

                                    if (damx <= 0) {
                                        damx = 10;
                                    }

                                    damAttack.add(damx);
                                    if (freezz <= 10 && Map.r.nextInt(100) > cx.khamKhangBang()) {
                                        cx.khamEff[4] = System.currentTimeMillis() / 1000L;
                                    }
                                }

                                if (charid.size() >= nplayer) {
                                    break;
                                }
                            }
                        }
                    } else {
                        for (int i = players.size() / 2; i >= 0; i--) {
                            Char cxx = players.get(i);
                            if (cxx.hp > 0 && this.near(cxx, 220) && cxx.id != this.target.id && !cxx.isAdmin && cxx.isBot == -1 && cxx.myCountry != this.inCountry) {
                                charid.add(cxx);
                                int damxx = this.attackDam(cxx);
                                if (idSkill == 1) {
                                    if (damxx < 5000) {
                                        damxx = 5000 + Map.r.nextInt(100) + 10;
                                    }

                                    damxx *= 3;
                                }

                                if (crit) {
                                    damxx *= 2;
                                }

                                damxx = cxx.checkHapthuSatThuong(damxx, this);
                                damxx = cxx.checkGiamSatThuong(damxx);
                                damxx = cxx.checkPassAttack(this, damxx);
                                boolean missxx = this.attackMiss(cxx);
                                if (missxx) {
                                    damxx = 0;
                                }

                                if (cxx.hp - damxx > 0 && demi) {
                                    damxx = cxx.hp - 1;
                                }

                                if (!demi) {
                                    damxx = (int)(damxx - cxx.checkMagicShield(damxx));
                                }

                                if (damxx <= 0) {
                                    damxx = 10;
                                }

                                damAttack.add(damxx);
                                if (freezz <= 10 && Map.r.nextInt(100) > cxx.khamKhangBang()) {
                                    cxx.khamEff[4] = System.currentTimeMillis() / 1000L;
                                }
                            }

                            if (charid.size() >= nplayer) {
                                break;
                            }
                        }

                        if (charid.size() < nplayer) {
                            for (int i = players.size() / 2; i < players.size(); i++) {
                                Char cxxx = players.get(i);
                                if (cxxx.hp > 0
                                    && this.near(cxxx, 220)
                                    && cxxx.id != this.target.id
                                    && !cxxx.isAdmin
                                    && cxxx.isBot == -1
                                    && cxxx.myCountry != this.inCountry) {
                                    charid.add(cxxx);
                                    int damxxx = this.attackDam(cxxx);
                                    if (idSkill == 1) {
                                        if (damxxx < 5000) {
                                            damxxx = 5000 + Map.r.nextInt(100) + 10;
                                        }

                                        damxxx *= 3;
                                    }

                                    if (crit) {
                                        damxxx *= 2;
                                    }

                                    damxxx = cxxx.checkHapthuSatThuong(damxxx, this);
                                    damxxx = cxxx.checkGiamSatThuong(damxxx);
                                    damxxx = cxxx.checkPassAttack(this, damxxx);
                                    boolean missxxx = this.attackMiss(cxxx);
                                    if (missxxx) {
                                        damxxx = 0;
                                    }

                                    if (cxxx.hp - damxxx > 0 && demi) {
                                        damxxx = cxxx.hp - 1;
                                    }

                                    if (!demi) {
                                        damxxx = (int)(damxxx - cxxx.checkMagicShield(damxxx));
                                    }

                                    if (damxxx <= 0) {
                                        damxxx = 10;
                                    }

                                    damAttack.add(damxxx);
                                    if (freezz <= 10 && Map.r.nextInt(100) > cxxx.khamKhangBang()) {
                                        cxxx.khamEff[4] = System.currentTimeMillis() / 1000L;
                                    }
                                }

                                if (charid.size() >= nplayer) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception var21) {
                }

                this.findPos = (this.findPos + 1) % 3;
                m.dos.writeByte(charid.size());

                for (int i = 0; i < charid.size(); i++) {
                    Char cxxxx = charid.get(i);
                    m.dos.writeShort(cxxxx.id);
                    m.dos.writeInt(damAttack.get(i));
                    int hp = cxxxx.hp - damAttack.get(i);
                    if (hp < 0) {
                        hp = 0;
                    }

                    m.dos.writeInt(hp);
                    cxxxx.hp = hp;
                    if (cxxxx.hp <= 0) {
                        Database.instance
                            .saveOrtherLog(
                                "",
                                cxxxx.getName(),
                                cxxxx.hp
                                    + "_"
                                    + damAttack.get(i)
                                    + "_"
                                    + this.getMonsterTemplate().name
                                    + "_"
                                    + Map.getNameMap(this.map.mapId)
                                    + "_"
                                    + this.region
                                    + "_"
                                    + cxxxx.region,
                                "die"
                            );
                    }
                }

                m.dos.writeByte(-1);

                for (int i = 0; i < charid.size(); i++) {
                    Char cxxxxx = charid.get(i);
                    cxxxxx.sendMessage(m);
                    if (cxxxxx.hp <= 0) {
                        cxxxxx.actorDie();
                        cxxxxx.desTroy();

                        try {
                            if (this.map.mapIDLoadMap != 201) {
                                long xp = cxxxxx.lvDetail.getExp();
                                xp -= cxxxxx.lvDetail.getXPLost(cxxxxx.killer, cxxxxx);
                                int currentlv = cxxxxx.lvDetail.lv;
                                cxxxxx.lvDetail.setExp(xp, cxxxxx.oldLv, cxxxxx.getName(), "bossthuthanh");
                                if (cxxxxx.lvDetail.lv <= 1) {
                                    cxxxxx.lvDetail.lv = 1;
                                    cxxxxx.lvDetail.percent = 0;
                                }

                                if (currentlv > cxxxxx.lvDetail.lv) {
                                    cxxxxx.lvDetail.resetExp2Lv(currentlv, cxxxxx.killer);
                                    if (cxxxxx.killer > 0) {
                                        Database.instance.saveOrtherLog("", cxxxxx.getName(), "tut level do dang trong che do ds " + cxxxxx.killer, "downlv");
                                    }
                                }

                                cxxxxx.calculateAttrib();
                                cxxxxx.doSendProperties();
                            }
                        } catch (Exception var20) {
                            var20.printStackTrace();
                        }
                    }

                    for (int j = 0; j < cxxxxx.nearChars.size(); j++) {
                        Char p = this.map.getPlayerByID(cxxxxx.nearChars.get(j));
                        if (p != null && !charid.contains(p)) {
                            p.sendMessage(m);
                            if (cxxxxx.khamEff[4] > 0L) {
                                p.sendMessage(p.sendEffKham(cxxxxx));
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
    public int khamKhangMu() {
        return 90;
    }

    @Override
    public int khamKhangBang() {
        return 90;
    }

    @Override
    public int khamKhangDoc() {
        return 90;
    }

    @Override
    public int khamKhangChoang() {
        return 90;
    }

    @Override
    public int khamKhangHoathach() {
        return 90;
    }

    @Override
    public int khamKhangGiamtoc() {
        return 90;
    }

    @Override
    public boolean haveBackDam() {
        return Map.r.nextInt(100) < 40;
    }

    @Override
    public boolean resistThroughArmor() {
        return Map.r.nextInt(100) < 65;
    }

    @Override
    public boolean haveDodge() {
        return Map.r.nextInt(100) < 40;
    }

    @Override
    public int getBackDam(int dam) {
        int pc = Map.r.nextInt(15) + 25;
        return dam * pc / 100;
    }

    @Override
    public boolean isEnemy(Char p) {
        return p.myCountry == this.inCountry ? false : super.isEnemy(p);
    }

    @Override
    public boolean allWayAdd() {
        return true;
    }

    @Override
    public boolean isBoss() {
        return super.isBoss();
    }
}
