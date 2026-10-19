/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.GemItem;
import io.Message;
import java.util.Vector;

public class MonsterVanTienTran extends Monster {
    static int maxhpTemp = 100000;
    static int defTemp = 5000;
    int wave = 1;

    public MonsterVanTienTran(Map mapLiveIn, MonsterTemplate template, int x, int y, int country) {
        super(mapLiveIn, template, x, y, country);
        this.maxhp = maxhpTemp;
        this.defend_magic = this.defend_physic = defTemp;
        this.typeAttack = 1;
    }

    public void upDefAtkByWave(long wave) {
        this.attack = (int)(this.attack + this.getMonsterTemplate().attack * wave * 10L / 100L);
        this.defend_magic = this.defend_physic = defTemp;
        this.defend_magic = (int)(this.defend_magic + defTemp * wave * 10L / 100L);
        this.defend_physic = (int)(this.defend_physic + defTemp * wave * 10L / 100L);
        this.maxhp = (int)(maxhpTemp + maxhpTemp * wave * 10L / 100L) + this.level * 3000;
        this.hp = this.maxhp;
    }

    public MonsterVanTienTran(byte cat) {
        super(cat);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public int attackDam(LiveActor actor) {
        try {
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
            long pcmin = 2 + this.wave;
            long pcmax = 5 + this.wave;
            if (dam < actor.hp * pcmin / 100L) {
                dam = (int)(actor.hp * pcmin / 100L + Map.r.nextInt(100));
            } else if (dam > actor.hp * pcmax / 100L) {
                dam = (int)(actor.hp * pcmax / 100L + Map.r.nextInt(100));
            }

            if (def > dam) {
                def = dam / 2;
            }

            return dam - def * (5 - this.wave / 2) / 100;
        } catch (Exception var8) {
            var8.printStackTrace();
            return 1;
        }
    }

    @Override
    public void attack() {
        if (!this.freeze()) {
            if (this.target == null || this.target.map.equals(this.map) && this.target.region == this.region) {
                if ((this.idTemplate < 85 || this.idTemplate > 89) && this.idTemplate != 36 && this.idTemplate != 37) {
                    long now = System.currentTimeMillis();
                    if (now - this.lastTimeAttack > this.attackDelay) {
                        this.lastTimeAttack = now;

                        try {
                            Message m = new Message(10);
                            m.dos.writeShort(this.id);
                            m.dos.writeShort(this.target.id);
                            int ahp = this.attackDam(this.target);
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
                                this.target.desTroy();
                                this.target.actorDie();

                                try {
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
    public Vector<Actor> onDropItem(Map m, Char p) {
        Vector<Actor> droplist = new Vector<>();
        if (Map.r.nextInt(100) < 20) {
            return droplist;
        } else {
            Potion pt = null;
            int quantity = 100000 * (this.wave + 1);
            pt = new Potion((short)0, quantity, m);
            pt.id = m.getIDITEM();
            pt.x = this.x;
            pt.y = this.y;
            m.addPotion(pt, this.inCountry);
            pt.time_drop = System.currentTimeMillis();
            droplist.add(pt);
            if (this.wave == 0) {
                for (int i = 0; i < 1; i++) {
                    int gemid = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(2)][Map.r.nextInt(GemTemplate.ID_MATERIAL_LOW[1].length)];
                    GemItem gem = new GemItem(gemid);
                    if (gem != null) {
                        gem.cat = 6;
                        gem.x = this.x + 5;
                        gem.y = this.y + 10;
                        gem.islock = true;
                        gem.id = m.getIDITEM();
                        gem.time_drop = System.currentTimeMillis();
                        gem.belongUser = p.charDBID;
                        m.addGemItem(gem, this.inCountry);
                        droplist.add(gem);
                    }
                }

                byte[] ptType = new byte[]{93, 95};
                pt = new Potion(ptType[Map.r.nextInt(ptType.length)], 1, m);
                pt.id = m.getIDITEM();
                pt.x = this.x - 4;
                pt.y = this.y - 4;
                pt.belongUser = p.charDBID;
                m.addPotion(pt, this.inCountry);
                pt.time_drop = System.currentTimeMillis();
                droplist.add(pt);
            } else if (this.wave == 1) {
                for (int ix = 0; ix < 1; ix++) {
                    int gemid = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(2)][Map.r.nextInt(GemTemplate.ID_MATERIAL_LOW[1].length)];
                    GemItem gem = new GemItem(gemid);
                    if (gem != null) {
                        gem.cat = 6;
                        gem.x = this.x + 5;
                        gem.y = this.y + 10;
                        gem.id = m.getIDITEM();
                        gem.time_drop = System.currentTimeMillis();
                        gem.belongUser = p.charDBID;
                        gem.islock = true;
                        m.addGemItem(gem, this.inCountry);
                        droplist.add(gem);
                    }
                }

                for (int ixx = 0; ixx < 2; ixx++) {
                    byte[] ptType = new byte[]{93, 95};
                    pt = new Potion(ptType[Map.r.nextInt(ptType.length)], 1, m);
                    pt.id = m.getIDITEM();
                    pt.x = this.x - 4;
                    pt.y = this.y - 4;
                    pt.belongUser = p.charDBID;
                    m.addPotion(pt, this.inCountry);
                    pt.time_drop = System.currentTimeMillis();
                    droplist.add(pt);
                }
            } else if (this.wave == 2) {
                for (int ixx = 0; ixx < 1; ixx++) {
                    int gemid = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(2)][Map.r.nextInt(GemTemplate.ID_MATERIAL_LOW[1].length)];
                    GemItem gem = new GemItem(gemid);
                    if (gem != null) {
                        gem.cat = 6;
                        gem.x = this.x + 5;
                        gem.y = this.y + 10;
                        gem.id = m.getIDITEM();
                        gem.time_drop = System.currentTimeMillis();
                        gem.islock = true;
                        gem.belongUser = p.charDBID;
                        m.addGemItem(gem, this.inCountry);
                        droplist.add(gem);
                    }
                }

                for (int ixxx = 0; ixxx < 2; ixxx++) {
                    byte[] ptType = new byte[]{93, 95};
                    pt = new Potion(ptType[Map.r.nextInt(ptType.length)], 1, m);
                    pt.id = m.getIDITEM();
                    pt.x = this.x - 4;
                    pt.y = this.y - 4;
                    pt.belongUser = p.charDBID;
                    m.addPotion(pt, this.inCountry);
                    pt.time_drop = System.currentTimeMillis();
                    droplist.add(pt);
                }
            } else if (this.wave == 3) {
                for (int ixxx = 0; ixxx < 1; ixxx++) {
                    int gemid = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(3)][Map.r.nextInt(GemTemplate.ID_MATERIAL_LOW[2].length)];
                    GemItem gem = new GemItem(gemid);
                    if (gem != null) {
                        gem.cat = 6;
                        gem.islock = true;
                        gem.x = this.x + 5;
                        gem.y = this.y + 10;
                        gem.id = m.getIDITEM();
                        gem.time_drop = System.currentTimeMillis();
                        gem.belongUser = p.charDBID;
                        m.addGemItem(gem, this.inCountry);
                        droplist.add(gem);
                    }
                }

                for (int ixxxx = 0; ixxxx < 3; ixxxx++) {
                    byte[] ptType = new byte[]{94, 96, 82, 97};
                    pt = new Potion(ptType[Map.r.nextInt(ptType.length)], 1, m);
                    pt.id = m.getIDITEM();
                    pt.x = this.x - 4;
                    pt.y = this.y - 4;
                    pt.belongUser = p.charDBID;
                    m.addPotion(pt, this.inCountry);
                    pt.time_drop = System.currentTimeMillis();
                    droplist.add(pt);
                }
            } else if (this.wave == 4) {
                for (int ixxxx = 0; ixxxx < 1; ixxxx++) {
                    int gemid = GemTemplate.ID_MATERIAL_LOW[Map.r.nextInt(3)][Map.r.nextInt(GemTemplate.ID_MATERIAL_LOW[2].length)];
                    GemItem gem = new GemItem(gemid);
                    if (gem != null) {
                        gem.cat = 6;
                        gem.x = this.x + 5;
                        gem.y = this.y + 10;
                        gem.islock = true;
                        gem.id = m.getIDITEM();
                        gem.time_drop = System.currentTimeMillis();
                        gem.belongUser = p.charDBID;
                        m.addGemItem(gem, this.inCountry);
                        droplist.add(gem);
                    }
                }

                for (int ixxxxx = 0; ixxxxx < 3; ixxxxx++) {
                    byte[] ptType = new byte[]{94, 96, 82, 97};
                    pt = new Potion(ptType[Map.r.nextInt(ptType.length)], 1, m);
                    pt.id = m.getIDITEM();
                    pt.x = this.x - 4;
                    pt.y = this.y - 4;
                    pt.belongUser = p.charDBID;
                    m.addPotion(pt, this.inCountry);
                    pt.time_drop = System.currentTimeMillis();
                    droplist.add(pt);
                }
            }

            return droplist;
        }
    }

    @Override
    public void actorDie() {
        try {
            this.isDead = true;
            this.bornTime = System.currentTimeMillis() + 3600000L;
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
    public int getTimeReborn() {
        return -1;
    }

    @Override
    public boolean isMonsterVanTienTran() {
        return true;
    }

    @Override
    public int getWave() {
        return this.wave;
    }

    @Override
    public boolean isLienHoaTru() {
        return super.isLienHoaTru();
    }

    @Override
    public boolean haveBackDam() {
        return Map.r.nextInt(100) < 80;
    }

    @Override
    public boolean resistThroughArmor() {
        return Map.r.nextInt(100) < 80;
    }

    @Override
    public boolean haveDodge() {
        return Map.r.nextInt(100) < 20;
    }

    @Override
    public int getBackDam(int dam) {
        long pc = Map.r.nextInt(11) + 30;
        return (int)(dam * pc / 100L);
    }
}
