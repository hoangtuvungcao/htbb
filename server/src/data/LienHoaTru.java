/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Message;
import java.io.IOException;
import java.util.Vector;
import real.Actor;
import real.Char;
import real.Map;
import real.Monster;
import real.MonsterTemplate;

public class LienHoaTru extends Monster {
    int findPos = 0;
    private long timeSendMove;

    public LienHoaTru(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
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
                Map.r.nextInt(100);
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
                int nplayer = 100;

                try {
                    Vector<Char> players1 = this.map.getAllPlayer(this.inCountry, this.region);
                    Vector<Char> players = new Vector<>();

                    for (int i = 0; i < players1.size(); i++) {
                        Char c = players1.get(i);
                        if (c.hp > 0 && this.near(c, 220) && c.id != this.target.id && !c.isAdmin && c.region == this.region && c.isBot == -1) {
                            players.add(players1.get(i));
                        }
                    }

                    while (players.size() > 0) {
                        Char c = players.remove(Map.r.nextInt(players.size()));
                        if (c.hp > 0 && this.near(c, 220) && c.id != this.target.id && !c.isAdmin && c.region == this.region && c.isBot == -1) {
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
                } catch (Exception var20) {
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
            } catch (IOException var21) {
                var21.printStackTrace();
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

        super.update();
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
    public boolean isEnemy(Char p) {
        if (this.map.isvanTienTran()) {
            return true;
        } else {
            return p.myCountry == this.inCountry ? false : super.isEnemy(p);
        }
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
        return super.isLienHoaTru();
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
}
