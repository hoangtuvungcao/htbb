/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import data.GemItem;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;

public class PrivateDataMap extends Thread {
    private static final int DELAY_UPDATE_MAP = 300;
    private Object LOCK1 = new Object();
    public String charMaster = "";
    public int idParty;
    public byte typeParty;
    public boolean isFinish = false;
    public Vector<Monster> monster = new Vector<>();
    public Vector<Char> players = new Vector<>();
    public Vector<Potion> potions = new Vector<>();
    public Vector<Item> items = new Vector<>();
    public Vector<GemItem> gems = new Vector<>();
    public Vector<Integer> idBoss = new Vector<>();
    Vector<PlayerMessage> playerMessages = new Vector<>();
    public static String[] nameParty = new String[]{"Bình thường", "Náo nhiệt", "Hoành tráng"};
    public PrivateMap parrent;
    byte nMonster = 0;
    long tGameOver = 0L;
    short idMonster = -32000;
    private long lastTimeUpdateMap1;
    static int[][] pos_fire_work = new int[][]{
        {8, 29, 12, 25, 12, 19, 12, 13, 13, 8, 26, 8, 27, 13, 27, 19, 27, 25, 31, 29, 17, 8, 22, 8},
        {16, 35, 12, 29, 15, 25, 12, 21, 15, 17, 12, 13, 15, 11, 24, 11, 18, 10, 21, 10, 27, 13, 24, 17, 27, 21, 24, 25, 27, 29, 23, 35},
        {16, 35, 12, 29, 15, 25, 12, 21, 15, 17, 12, 13, 15, 11, 24, 11, 18, 10, 21, 10, 27, 13, 24, 17, 27, 21, 24, 25, 27, 29, 23, 35}
    };

    public PrivateDataMap(int typeParty, PrivateMap map) {
        this.parrent = map;
        this.typeParty = (byte)typeParty;
        this.nMonster = (byte)(typeParty + 1);
        new Thread(this).start();
    }

    public void playerJoin(Char p) {
        this.players.add(p);
    }

    public void playerExit(Char p) {
        this.players.remove(p);
        Message m = new Message(8);

        try {
            m.dos.writeShort(p.id);
            p.sendToNearPlayer(m);
        } catch (IOException var4) {
        }
    }

    public Monster getMonster(short id, int country) {
        for (int i = 0; i < this.monster.size(); i++) {
            if (this.monster.get(i).id == id) {
                return this.monster.get(i);
            }
        }

        return null;
    }

    public void onMosterDie(Char p, Monster mt, byte skill, int ahp, byte effect, byte level) {
        try {
            Message m = new Message(17);
            m.dos.writeShort(p.id);
            m.dos.writeShort(mt.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(0);
            m.dos.writeByte(effect);
            m.dos.writeByte(0);
            m.dos.writeByte(1);
            m.dos.writeByte(0);
            m.dos.writeByte(level);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (Exception var8) {
        }
    }

    public void onMosterDie(Char p, short idMt, byte skill, int ahp, byte effect, byte level) {
        try {
            Message m = new Message(17);
            m.dos.writeShort(p.id);
            m.dos.writeShort(idMt);
            m.dos.writeByte(skill);
            m.dos.writeInt(0);
            m.dos.writeByte(effect);
            m.dos.writeByte(0);
            m.dos.writeByte(1);
            m.dos.writeByte(0);
            m.dos.writeByte(level);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (Exception var8) {
        }
    }

    public void doAttackMonster(Char p, Message message) {
        try {
            if (p.countHit() || p.freeze()) {
                return;
            }

            if (p.hp <= 0) {
                p.actorDie();
                return;
            }

            if (!p.checkDurableWeapone()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Không thể đánh khi vũ khí bị hỏng. Hãy đến Thợ rèn để sửa lại.", ""));
                Message m = new Message(104);

                try {
                    m.dos.writeByte(p.typeConfig);
                    m.dos.writeByte(0);
                    p.sendMessage(m);
                } catch (Exception var34) {
                }

                return;
            }

            p.downDurableWeapone();
            DataInputStream dis = message.dis;
            Monster mt = this.getMonster(dis.readShort(), p.inCountry);
            short skill = dis.readByte();
            byte effect = 0;
            int ahp = p.attackDamage;
            int crit = 0;
            int buffAttack = -1;
            if (buffAttack > 0) {
                return;
            }

            if (buffAttack != -1 && buffAttack == 0 && p.skill[5] + p.addMoreLevelSkill[5] == 0) {
                return;
            }

            if (mt == null || mt.isDead) {
                if (mt != null) {
                    this.onMosterDie(p, mt, (byte)skill, 1, effect, (byte)0);
                }

                return;
            }

            if (!Map.inRangeActor(p, mt, Map.MAX_RANGE_CHAR[p.charClass])) {
                return;
            }

            if (mt.map.mapId != p.mapID) {
                return;
            }

            int _type = (byte)skill;
            int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[_type];
            }

            if (_level == 0 || !Map.inRangeSkill(p, mt, CharManager.getSkillRange((byte)_type, p.charClass))) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - p.timeLastUseSkills[_type] < CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100) {
                return;
            }

            p.timeLastUseSkills[_type] = now;
            buffAttack = p.getBuffEffAttack();
            if (mt.resistThroughArmor()) {
                buffAttack = -1;
            }

            int damage = p.attackDam(mt, _type, _level, buffAttack);
            if (mt.haveDodge()) {
                damage = 0;
                buffAttack = -1;
            }

            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            boolean critSv = p.havecrit();
            if (critSv) {
                damage *= 2;
                effect = 2;
                if (p.petUsing != null) {
                    long pcLienKich = p.petUsing.getLienKich();
                    damage = (int)(damage + damage * pcLienKich / 100L);
                }
            }

            if (_level > p.skill[_type] + p.addMoreLevelSkill[_type]) {
                return;
            }

            int mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
            if (p.mp + p.percentBuff[1] < mplost) {
                return;
            }

            p.mp -= mplost;
            if (p.mp <= 0) {
                p.mp = 0;
            }

            int getXp = mt.getXpReceive(damage);
            ahp = damage / CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            mt.hp -= damage;
            if (damage > 0 && mt.haveBackDam()) {
                int backdam = mt.getBackDam(damage);
                Message mbd = MessageCreator.createMsgBuffEffect(5, mt.cat, p, backdam, 0, -1);
                p.sendMessage(mbd);
                p.sendToNearPlayer(mbd);
            }

            Message m = null;
            int doubleALL = 1;
            if (p.charthanthu != null && mt.hp > 0) {
                Vector<LiveActor> target = new Vector<>();
                target.add(mt);
                p.charthanthu.doAttack(target);
                int damthanthu = p.getDamtThanThu(mt);
                getXp += mt.getXpReceive(damthanthu);
                mt.hp -= damthanthu;
            }

            if (mt.hp > 0) {
                int damNguyetAnh = p.getPCDamNguyetAnh(skill);
                if (mt.hp > 0 && damNguyetAnh > 0) {
                    mt.hp = mt.hp - mt.maxhp * damNguyetAnh / 100;
                    damage += mt.maxhp * damNguyetAnh / 100;
                    p.sendEffectBuff(mt, EffectBuff.EFF_NGUYET_ANH, 1000);
                }
            }

            if (mt.hp > 0) {
                if (mt.target == null) {
                    mt.target = p;
                }

                if (ahp > 0) {
                    p.buffAttackSkill(damage, mt);
                }

                if (getXp > 0) {
                    int x2Player = p.getX2();
                    if (TeamServer.isDouble) {
                        int var49 = 0;
                    }

                    int dxp = Map.rand10(getXp);
                    if (dxp == 0) {
                        dxp = 1;
                    }

                    int[] downPercent = new int[]{1, 5, 10, dxp};
                    int targetLv = p.getLevel();
                    if (targetLv < 40) {
                        downPercent = new int[]{1, 2, 3, dxp};
                    }

                    int delta = targetLv - mt.level;
                    if (delta > 0) {
                        int a = delta / 4;
                        if (targetLv < 40) {
                            a = delta / 6;
                        }

                        if (a > 3) {
                            a = 3;
                        }

                        dxp /= downPercent[a];
                        if (dxp <= 0) {
                            dxp = 1;
                        }
                    }

                    if (dxp > 0) {
                        int newxp = Map.calculatorXpParty(p, dxp);
                        if (newxp != dxp) {
                            int nUser = p.party.userParty.size();
                            if (nUser > 1) {
                                nUser = 5;
                            }

                            int xpReceive = newxp * 80 / (100 * nUser);
                            int maxLv = p.lvDetail.lv;

                            for (int i = 0; i < p.party.userParty.size(); i++) {
                                Char pp = p.party.userParty.get(i);
                                if (pp.id != p.id && p.near(pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region) {
                                    int dlv = Map.abs(maxLv - pp.lvDetail.lv);
                                    int temp = 1;
                                    if (dlv <= 5) {
                                        temp = xpReceive;
                                    } else if (dlv <= 10) {
                                        temp = xpReceive / 5;
                                    } else if (dlv <= 20) {
                                        temp = xpReceive / 10;
                                    } else if (dlv <= 30) {
                                        temp = xpReceive / 15;
                                    } else {
                                        temp = xpReceive / 20;
                                    }

                                    if (temp == 0) {
                                        temp = 1;
                                    }

                                    if (pp.hp > 0) {
                                        temp = pp.expReceive(temp);
                                        Map.addXPForChar(pp, temp + pp.getEffSkillClan(0) * temp / 100, false, "privatedatamap doAttackMonster1");
                                    }
                                }
                            }

                            xpReceive = newxp * 20 / 100 * doubleALL;
                            xpReceive = p.expReceive(xpReceive);
                            Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "privatedatamap doAttackMonster2");
                        } else {
                            int var54 = dxp * doubleALL;
                            var54 = p.expReceive(var54);
                            Map.addXPForChar(p, var54 + p.getEffSkillClan(0) * var54 / 100, false, "privatedatamap doAttackMonster3");
                        }
                    }
                }

                m = new Message(9);
                m.dos.writeShort(p.id);
                m.dos.writeShort(mt.id);
                m.dos.writeByte(skill);
                m.dos.writeInt(ahp);
                m.dos.writeInt(mt.hp);
                m.dos.writeByte(effect);
                m.dos.writeByte(CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0]);
                m.dos.writeByte(buffAttack);
                m.dos.writeByte(_level);
                p.sendMessage(m);
                p.sendToNearPlayer(m);
                p.buffSkillKham(mt);
                p.charHireAttackDam(mt, _type, _level, buffAttack);
            } else {
                try {
                    m = new Message(17);
                    m.dos.writeShort(p.id);
                    m.dos.writeShort(mt.id);
                    m.dos.writeByte(skill);
                    m.dos.writeInt(ahp);
                    m.dos.writeByte(effect);
                    Vector<Monster> droplist = new Vector<>();
                    m.dos.writeByte(droplist.size());
                    if (droplist.size() > 0) {
                        for (Actor e : droplist) {
                            Map.writeActorPos(m, e, p.getSession().isOldVersion);
                        }
                    }

                    int xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0];
                    m.dos.writeByte(xx2);
                    m.dos.writeByte(buffAttack);
                    m.dos.writeByte(_level);
                    p.sendMessage(m);
                    p.sendToNearPlayer(m);
                } catch (Exception var35) {
                    System.out.println("loi gui thong tin monsterdie ");
                }
            }

            if (mt.hp <= 0) {
                this.monster.remove(mt);
                if ((!mt.isCopy() || !mt.isBoss) && mt.isBoss) {
                    mt.bornTime = System.currentTimeMillis() + 86400000L;
                    mt.setTimeReBornInEvent(mt.bornTime);
                }

                mt.isDead = true;
                mt.target = null;
                if (this.monster.size() == 0) {
                    if (this.nMonster == 0) {
                        this.tGameOver = System.currentTimeMillis() + 900000L;
                        this.sendAllCharMap(MessageCreator.createServerAlertAutoOffMessage("Chúc mừng đám cưới đã thành công tốt đẹp."));
                    }

                    this.doAddGift();
                }
            }
        } catch (Exception var36) {
            var36.printStackTrace();
        }
    }

    public void doAttackMultiMonster(Char p, Message message) {
        try {
            if (p.countHit() || p.freeze()) {
                return;
            }

            if (p.hp <= 0) {
                p.actorDie();
                return;
            }

            if (!p.checkDurableWeapone()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Không thể đánh khi vũ khí bị hỏng. Hãy đến Thợ rèn để sửa lại.", ""));
                Message m = new Message(104);

                try {
                    m.dos.writeByte(p.typeConfig);
                    m.dos.writeByte(0);
                    p.sendMessage(m);
                } catch (Exception var38) {
                }

                return;
            }

            DataInputStream dis = message.dis;
            byte skill = dis.readByte();
            byte effect = 0;
            int ahp1 = 0;
            int crit = 0;
            int buffAttack = -1;
            if (buffAttack > 0) {
                return;
            }

            if (buffAttack != -1 && buffAttack == 0 && p.skill[5] + p.addMoreLevelSkill[5] == 0) {
                return;
            }

            int nMonster = dis.readByte();
            Monster firstMonster = null;
            Monster mt = this.getMonster(dis.readShort(), p.inCountry);
            firstMonster = mt;
            if (mt == null || mt.isDead) {
                if (mt != null) {
                    this.onMosterDie(p, mt, skill, 1, effect, (byte)0);
                }

                return;
            }

            if (!Map.inRangeActor(p, mt, Map.MAX_RANGE_CHAR[p.charClass])) {
                return;
            }

            if (mt.map.mapId != p.mapID) {
                return;
            }

            int _type = skill;
            int _level = p.skill[skill] + p.addMoreLevelSkill[skill];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[skill];
            }

            if (_level <= 0 || !Map.inRangeSkill(p, mt, CharManager.getSkillRange((byte)skill, p.charClass))) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - p.timeLastUseSkills[skill] < p.coolDown[skill][_level]) {
                return;
            }

            p.timeLastUseSkills[skill] = now;
            buffAttack = p.getBuffEffAttack();
            if (mt.resistThroughArmor()) {
                buffAttack = -1;
            }

            int damage = p.attackDam(mt, skill, _level, buffAttack);
            if (mt.haveDodge()) {
                damage = 0;
                buffAttack = -1;
            }

            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][skill][_level - 1];
            boolean critSv = p.havecrit();
            if (critSv) {
                damage *= 2;
                effect = 2;
                if (p.petUsing != null) {
                    long pcLienKich = p.petUsing.getLienKich();
                    damage = (int)(damage + damage * pcLienKich / 100L);
                }
            }

            if (_level > p.skill[skill] + p.addMoreLevelSkill[skill]) {
                return;
            }

            int mplost = CharManager.SKILL_MP[p.charClass][skill][_level];
            if (p.mp + p.percentBuff[1] < mplost) {
                return;
            }

            p.mp -= mplost;
            if (p.mp <= 0) {
                p.mp = 0;
            }

            if (damage > 0 && mt.haveBackDam()) {
                int backdam = mt.getBackDam(damage);
                Message mbd = MessageCreator.createMsgBuffEffect(5, mt.cat, p, backdam, 0, -1);
                p.sendMessage(mbd);
                p.sendToNearPlayer(mbd);
            }

            Message m = null;
            int i = 0;
            int allXP = 0;
            Vector<Monster> mst = new Vector<>();
            mst.add(mt);
            byte[] nmonster = new byte[]{5, 5, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10};
            Vector<Message> msgMonsterDie = new Vector<>();
            Vector<LiveActor> muctieu = new Vector<>();
            int damNguyetAnh = p.getPCDamNguyetAnh(skill);

            while (i < nMonster) {
                if (i > 0) {
                    mt = this.getMonster(dis.readShort(), p.inCountry);
                }

                if (mt != null) {
                    if (i > 0) {
                        if (!Map.inRangeActor((LiveActor)firstMonster, (LiveActor)mt, CharManager.getRangeSkillAeo(p.charClass, skill, _level))) {
                            i++;
                            continue;
                        }

                        if (mt.isDead) {
                            this.onMosterDie(p, mt, skill, damage, effect, (byte)0);
                        } else {
                            mst.add(mt);
                        }
                    }

                    int dxp = mt.getXpReceive(damage);
                    if (dxp == 0) {
                        dxp = 1;
                    }

                    int[] downPercent = new int[]{1, 5, 10, dxp};
                    int targetLv = p.getLevel();
                    if (targetLv < 40) {
                        downPercent = new int[]{1, 2, 3, dxp};
                    }

                    int delta = targetLv - mt.level;
                    if (delta > 0) {
                        int a = delta / 4;
                        if (targetLv < 40) {
                            a = delta / 6;
                        }

                        if (a > 3) {
                            a = 3;
                        }

                        dxp /= downPercent[a];
                    }

                    if (dxp <= 0) {
                        dxp = 1;
                    }

                    allXP += dxp;
                    mt.hp -= damage;
                    if (p.charthanthu != null && mt.hp > 0) {
                        muctieu.add(mt);
                        int damthanthu = p.getDamtThanThu(mt);
                        allXP += mt.getXpReceive(damthanthu);
                        mt.hp -= damthanthu;
                    }

                    if (mt.hp > 0 && mt.hp > 0 && damNguyetAnh > 0) {
                        mt.hp = mt.hp - mt.maxhp * damNguyetAnh / 100;
                        damage += mt.maxhp * damNguyetAnh / 100;
                        p.sendEffectBuff(mt, EffectBuff.EFF_NGUYET_ANH, 1000);
                    }

                    if (mt.hp <= 0) {
                        Vector<Actor> droplist = new Vector<>();
                        if (!mt.isMaterialMons()) {
                            mt.hp = 0;
                            m = new Message(17);
                            m.dos.writeShort(p.id);
                            m.dos.writeShort(mt.id);
                            m.dos.writeByte(skill);
                            m.dos.writeInt(damage);
                            m.dos.writeByte(effect);
                            m.dos.writeByte(droplist.size());
                            if (droplist.size() > 0) {
                                for (Actor e : droplist) {
                                    Map.writeActorPos(m, e, p.getSession().isOldVersion);
                                }
                            }

                            int xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0];
                            m.dos.writeByte(xx2);
                            m.dos.writeByte(buffAttack);
                            m.dos.writeByte(_level);
                            msgMonsterDie.add(m);
                            if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                                p.checkFinsishQuest(mt.getType(), -1, -1);
                            }
                        }
                    } else if (mt.target == null) {
                        mt.target = p;
                    }

                    if (mt.hp <= 0) {
                        this.monster.remove(mt);
                        if (!mt.isBoss || !mt.isCopy()) {
                            if (mt.isBoss) {
                                mt.bornTime = System.currentTimeMillis() + 86400000L;
                                mt.setTimeReBornInEvent(mt.bornTime);
                                Database.instance.saveEvent(Map.event.getInfo());
                            } else {
                                mt.setTimeReBorn();
                            }
                        }

                        mt.isDead = true;
                        mt.target = null;
                        if (this.monster.size() == 0) {
                            if (this.nMonster == 0) {
                                this.tGameOver = System.currentTimeMillis() + 900000L;
                                this.sendAllCharMap(MessageCreator.createServerAlertAutoOffMessage("Chúc mừng đám cưới đã thành công tốt đẹp."));
                            }

                            this.doAddGift();
                        }
                    }
                }

                i++;
                if (mst.size() >= nmonster[_level]) {
                    break;
                }
            }

            try {
                if (muctieu.size() > 0) {
                    p.charthanthu.doAttack(muctieu);
                }
            } catch (Exception var40) {
            }

            m = new Message(106);
            m.dos.writeShort(p.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(damage);
            m.dos.writeByte(effect);
            m.dos.writeByte(_level);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(mst.size());

            for (int j = 0; j < mst.size(); j++) {
                Monster ms = mst.elementAt(j);
                m.dos.writeShort(ms.id);
                m.dos.writeInt(ms.hp > 0 ? ms.hp : 0);
            }

            p.sendMessage(m);
            p.sendToNearPlayer(m);

            for (int j = 0; j < msgMonsterDie.size(); j++) {
                try {
                    p.sendMessage(msgMonsterDie.get(j));
                    p.sendToNearPlayer(msgMonsterDie.get(j));
                } catch (Exception var39) {
                }
            }

            int dxpx = Map.rand10(allXP);
            if (dxpx == 0) {
                dxpx = 1;
            }

            if (dxpx > 0) {
                int newxp = Map.calculatorXpParty(p, dxpx);
                if (newxp == dxpx) {
                    int var55 = p.expReceive(dxpx);
                    Map.addXPForChar(p, var55 + p.getEffSkillClan(0) * var55 / 100, false, "privatedatamap doAttackMultiMonster3");
                } else {
                    int nUser = p.party.userParty.size();
                    if (nUser > 1) {
                        nUser = 5;
                    }

                    int xpReceive = newxp * 80 / (nUser * 100);
                    int maxLv = p.lvDetail.lv;

                    for (int k = 0; k < p.party.userParty.size(); k++) {
                        Char pp = p.party.userParty.get(k);
                        if (pp.id != p.id && p.near(pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry && pp.region == p.region) {
                            int dlv = Map.abs(maxLv - pp.lvDetail.lv);
                            int temp = 1;
                            if (dlv <= 5) {
                                temp = xpReceive;
                            } else if (dlv <= 10) {
                                temp = xpReceive / 5;
                            } else if (dlv <= 20) {
                                temp = xpReceive / 10;
                            } else if (dlv <= 30) {
                                temp = xpReceive / 15;
                            } else {
                                temp = xpReceive / 20;
                            }

                            if (temp == 0) {
                                temp = 1;
                            }

                            if (pp.hp > 0) {
                                temp = pp.expReceive(temp);
                                Map.addXPForChar(pp, temp + pp.getEffSkillClan(0) * temp / 100, false, "privatedatamap doAttackMultiMonster1");
                            }
                        }
                    }

                    xpReceive = newxp * 20 / 100;
                    xpReceive = p.expReceive(xpReceive);
                    Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "privatedatamap doAttackMultiMonster2");
                }
            }

            p.charHireAttackMultiMOnster(mst, _type);
        } catch (Exception var41) {
            var41.printStackTrace();
        }
    }

    private void doAddGiftNaoNhiet(Char p, int idGift) {
        short[] material = (short[])null;
        int idGem = 0;
        switch (idGift) {
            case 0:
                int exp = 1000000 + Map.r.nextInt(5000);
                PrivateMap.addXpCharEvent(p, exp, false, "privatedatamap doAddGiftNaoNhiet");
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + exp + " exp", "giftNN0");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + exp + " exp quà cưới", ""));
                break;
            case 1:
                p.addXu(40000L, "PrivateDataMap 1");
                Database.instance.saveOrtherLog("", p.charname, "nhan dc 40k xu", "giftNN1");
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được 40k xu quà cưới", ""));
                break;
            case 2:
                p.doAddGemItem(11, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, "nhan dc LKD 30", "giftNN2");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là luyện kim dược 30%", ""));
                break;
            case 3:
                material = InfoGifLucky.idMaterial[1];
                if (Map.r.nextInt(2) == 1) {
                    material = InfoGifLucky.idMaterial[4];
                }

                int var9 = material[Map.r.nextInt(material.length)];
                p.doAddGemItem(var9, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + Map.gemTemplate[var9].name, "giftNN3");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là " + Map.gemTemplate[var9].name, ""));
                break;
            case 4:
                int idFood = Map.r.nextInt(3) + 112;
                p.potions[idFood]++;
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + LoginHandler.PORTION_NAME[idFood], "giftNN4");
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là " + LoginHandler.PORTION_NAME[idFood], ""));
                break;
            case 5:
                idGem = Map.r.nextInt(18) + 137;
                p.doAddGemItem(idGem, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + Map.gemTemplate[idGem].name, "giftNN5");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là " + Map.gemTemplate[idGem].name, ""));
        }
    }

    private void doAddGiftHoanhTrang(Char p, int idGift) {
        short[] material = (short[])null;
        int idGem = 0;
        switch (idGift) {
            case 0:
                int exp = 1700000 + Map.r.nextInt(500000);
                PrivateMap.addXpCharEvent(p, exp, false, "privatedatamap doAddGiftHoanhTrang");
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + exp + " exp", "giftNN0");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + exp + " exp quà cưới", ""));
                break;
            case 1:
                p.addXu(50000L, "PrivateDataMap 2");
                Database.instance.saveOrtherLog("", p.charname, "nhan dc 50k xu", "giftNN1");
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được 50k xu quà cưới.", ""));
                break;
            case 2:
                p.doAddGemItem(66, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, "nhan dc LKD 35", "giftNN2");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là lkd 35%", ""));
                break;
            case 3:
                material = InfoGifLucky.idMaterial[2];
                if (Map.r.nextInt(2) == 1) {
                    material = InfoGifLucky.idMaterial[5];
                }

                int var9 = material[Map.r.nextInt(material.length)];
                p.doAddGemItem(var9, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + Map.gemTemplate[var9].name, "giftNN3");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là " + Map.gemTemplate[var9].name, ""));
                break;
            case 4:
                int idFood = Map.r.nextInt(3) + 112;
                p.potions[idFood]++;
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + LoginHandler.PORTION_NAME[idFood], "giftNN4");
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là " + LoginHandler.PORTION_NAME[idFood], ""));
                break;
            case 5:
                idGem = Map.r.nextInt(18) + 137;
                p.doAddGemItem(idGem, 1, true);
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, "nhan dc " + Map.gemTemplate[idGem].name, "giftNN5");
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được quà cưới là " + Map.gemTemplate[idGem].name, ""));
        }
    }

    private void doAddGift() {
        int idGif = 0;

        for (int i = 0; i < this.players.size(); i++) {
            try {
                Char p = this.players.get(i);
                switch (this.typeParty) {
                    case 0:
                    default:
                        break;
                    case 1:
                        if (p.idGifWedding.size() > 0) {
                            int var6 = p.idGifWedding.remove(Map.r.nextInt(p.idGifWedding.size()));
                            this.doAddGiftNaoNhiet(p, var6);
                        } else {
                            System.out.println("KO ADD " + p.charname);
                        }
                        break;
                    case 2:
                        if (p.idGifWedding.size() > 0) {
                            int var5 = p.idGifWedding.remove(Map.r.nextInt(p.idGifWedding.size()));
                            this.doAddGiftHoanhTrang(p, var5);
                        }
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }
    }

    public synchronized void doAddMonster(Char p) {
        if (this.nMonster > 0) {
            if (this.monster.size() > 0) {
                p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể gọi thêm boss lúc này."));
                return;
            }

            BossWedding m = null;
            switch (this.typeParty) {
                case 0:
                    if (this.nMonster == 1) {
                        m = new BossWedding(p.map, Map.monsterTemplates.get(93), 320, 320, 0);
                        m.level = m.getMonsterTemplate().level;
                        m.id = this.idMonster++;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m.he = he[Map.r.nextInt(5)];
                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                        m.typeAttack = t[Map.r.nextInt(10)];
                        m.maxhp = m.maxhp + m.maxhp / 3;
                        m.hp = m.maxhp;
                        m.rcvXP = Map.monsterTemplates.get(93).rcvXp * 5;
                        m.defend_magic = m.defend_magic + m.defend_magic / 3;
                        m.defend_physic = m.defend_magic + m.defend_physic / 3;
                        m.attack = m.attack - m.attack / 5;
                        m.bornTime = 120000L;
                        this.monster.add(m);
                    }
                    break;
                case 1:
                    if (this.nMonster == 3) {
                        m = new BossWedding(p.map, Map.monsterTemplates.get(93), 320, 320, 0);
                        m.level = m.getMonsterTemplate().level;
                        m.id = this.idMonster++;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m.he = he[Map.r.nextInt(5)];
                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                        m.typeAttack = t[Map.r.nextInt(10)];
                        m.maxhp = m.maxhp + m.maxhp / 3;
                        m.hp = m.maxhp;
                        m.rcvXP = Map.monsterTemplates.get(93).rcvXp * 5;
                        m.defend_magic = m.defend_magic + m.defend_magic / 3;
                        m.defend_physic = m.defend_magic + m.defend_physic / 3;
                        m.attack = m.attack - m.attack / 5;
                        m.bornTime = 120000L;
                        this.monster.add(m);
                    } else if (this.nMonster == 2) {
                        m = new BossWedding(p.map, Map.monsterTemplates.get(94), 320, 320, 0);
                        m.level = m.getMonsterTemplate().level;
                        m.id = this.idMonster++;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m.he = he[Map.r.nextInt(5)];
                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                        m.typeAttack = t[Map.r.nextInt(10)];
                        m.maxhp = m.maxhp + m.maxhp / 3;
                        m.hp = m.maxhp;
                        m.rcvXP = Map.monsterTemplates.get(94).rcvXp * 5;
                        m.defend_magic = m.defend_magic + m.defend_magic / 3;
                        m.defend_physic = m.defend_magic + m.defend_physic / 3;
                        m.attack = m.attack - m.attack / 5;
                        m.bornTime = 120000L;
                        this.monster.add(m);
                    }
                    break;
                case 2:
                    if (this.nMonster == 3) {
                        m = new BossWedding(p.map, Map.monsterTemplates.get(93), 320, 320, 0);
                        m.level = m.getMonsterTemplate().level;
                        m.id = this.idMonster++;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m.he = he[Map.r.nextInt(5)];
                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                        m.typeAttack = t[Map.r.nextInt(10)];
                        m.maxhp = m.maxhp + m.maxhp / 3;
                        m.hp = m.maxhp;
                        m.rcvXP = Map.monsterTemplates.get(93).rcvXp * 5;
                        m.defend_magic = m.defend_magic + m.defend_magic / 3;
                        m.defend_physic = m.defend_magic + m.defend_physic / 3;
                        m.attack = m.attack - m.attack / 5;
                        m.bornTime = 120000L;
                        this.monster.add(m);
                    } else if (this.nMonster == 2) {
                        m = new BossWedding(p.map, Map.monsterTemplates.get(94), 320, 320, 0);
                        m.level = m.getMonsterTemplate().level;
                        m.id = this.idMonster++;
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m.he = he[Map.r.nextInt(5)];
                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                        m.typeAttack = t[Map.r.nextInt(10)];
                        m.maxhp = m.maxhp + m.maxhp / 3;
                        m.hp = m.maxhp;
                        m.rcvXP = Map.monsterTemplates.get(94).rcvXp * 5;
                        m.defend_magic = m.defend_magic + m.defend_magic / 3;
                        m.defend_physic = m.defend_magic + m.defend_physic / 3;
                        m.attack = m.attack - m.attack / 5;
                        m.bornTime = 120000L;
                        this.monster.add(m);
                    } else if (this.nMonster == 1) {
                        int[] idTemplate = new int[]{91, 92};

                        for (int i = 0; i < 2; i++) {
                            m = new BossWedding(p.map, Map.monsterTemplates.get(idTemplate[i]), (20 + i * 2) * 16, 320, 0);
                            m.level = m.getMonsterTemplate().level;
                            m.id = this.idMonster++;
                            byte[] he = new byte[]{0, 1, 2, 3, 4};
                            m.he = he[Map.r.nextInt(5)];
                            byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                            m.typeAttack = t[Map.r.nextInt(10)];
                            m.maxhp = m.maxhp + m.maxhp / 3;
                            m.hp = m.maxhp;
                            m.rcvXP = Map.monsterTemplates.get(idTemplate[i]).rcvXp * 5;
                            m.defend_magic = m.defend_magic + m.defend_magic / 3;
                            m.defend_physic = m.defend_magic + m.defend_physic / 3;
                            m.attack = m.attack - m.attack / 5;
                            m.bornTime = 120000L;
                            this.monster.add(m);
                        }
                    }
            }

            this.nMonster--;
        } else {
            p.sendMessage(MessageCreator.createMsgChat(p.id, "Không thể gọi thêm boss"));
        }
    }

    @Override
    public void run() {
        while (!this.isFinish) {
            try {
                if (AdminHandler.isStopServer) {
                    break;
                }

                while (RealController.savingChar) {
                    Thread.sleep(100L);
                }

                long l1 = System.currentTimeMillis();
                if (l1 - this.lastTimeUpdateMap1 >= 300L) {
                    this.update();
                    this.lastTimeUpdateMap1 = System.currentTimeMillis();
                }

                l1 = System.currentTimeMillis();

                while (System.currentTimeMillis() - l1 < 500L && this.playerMessages.size() != 0) {
                    PlayerMessage pm = this.playerMessages.remove(0);
                    if (!pm.player.exit) {
                        this.parrent.processMessage(pm.player, pm.message);
                    }
                }

                if (this.timeOut()) {
                    while (this.players.size() > 0) {
                        Char player = this.players.remove(0);
                        if (player.isBot == -1) {
                            int[][] vilage = LangKhoiDau.bang(player); // lang khoi dau theo vung (sua-tuyen)
                            int[] tmap = vilage[player.myCountry];
                            int x = 16 + Map.r.nextInt() % 5;
                            int y = 24 + Map.r.nextInt(20);
                            player.x = x;
                            player.y = y;
                            player.idWedding = -1;
                            player.rcvGiftWedding = 0;
                            this.parrent.move2Map(player, x, y, tmap[Map.r.nextInt(2)], player.myCountry);
                        }
                    }

                    this.parrent.removeArena(this.idParty);
                    Database.instance.updateStatusWedding(this.idParty, 1);
                    Database.instance.saveOrtherLog("", this.idParty + "_" + nameParty[this.typeParty], "wedding ok", "finishWD");
                }

                if (this.playerMessages.size() != 0) {
                    synchronized (this.LOCK1) {
                        this.LOCK1.wait(1L);
                    }
                } else {
                    synchronized (this.LOCK1) {
                        this.LOCK1.wait(300L);
                    }
                }
            } catch (Exception var12) {
                var12.printStackTrace();
                System.out.println("LOI TRONG RUN PRIVATEDATAMAP " + var12.toString());
            }

            try {
                synchronized (this.LOCK1) {
                    this.LOCK1.wait(300L);
                }
            } catch (Exception var9) {
            }
        }
    }

    public void update() {
        if (this.players.size() > 0) {
            for (int i = 0; i < this.players.size(); i++) {
                this.players.get(i).update();
            }

            for (int j = 0; j < this.monster.size(); j++) {
                try {
                    Monster mt = this.monster.get(j);
                    mt.update();
                    if (mt.target != null && mt.target.exit) {
                        mt.target = null;
                    }

                    if (this.players.size() > 0 && !mt.isDead && mt.moved) {
                        Char fp = null;

                        for (int i = 0; i < this.players.size(); i++) {
                            try {
                                Char p = this.players.get(i);
                                if (p.isBot == -1) {
                                    if (Map.isNewVersion) {
                                        if (!p.nearMons.contains(mt.id)) {
                                            p.nearMons.add(mt.id);
                                            p.sendMessage(p.writeActorPos(new Message(4), mt));
                                        }
                                    } else {
                                        p.nearMons.add(mt.id);
                                    }

                                    if (p.near(mt, 110) && mt.typeAttack == 1) {
                                        if (p.hp > 0
                                            && !p.beAttack
                                            && mt.target == null
                                            && mt.getMonsterTemplate().active
                                            && !p.isAdmin
                                            && p.isBot == -1
                                            && mt.isEnemy(p)) {
                                            mt.target = p;
                                            p.beAttack = true;
                                        } else if (p.beAttack && fp == null) {
                                            fp = p;
                                        }
                                    }
                                }
                            } catch (Exception var6) {
                            }
                        }

                        if (fp != null && mt.target == null) {
                            mt.target = fp;
                        }
                    }
                } catch (Exception var7) {
                    System.out.println("UPDATE MAP MT ");
                }
            }
        }
    }

    public boolean timeOut() {
        if (this.tGameOver > 0L && System.currentTimeMillis() - this.tGameOver > 0L) {
            this.isFinish = true;
            return true;
        } else {
            return false;
        }
    }

    public void deletePotionAndItemOnGround(int country) {
        long now = System.currentTimeMillis();

        for (int i = 0; i < this.potions.size(); i++) {
            if (i < this.potions.size()) {
                Potion p = this.potions.elementAt(i);
                if (p.belongUser != 0 && now - p.time_drop > 5000L) {
                    p.belongUser = 0;
                }

                if (now - p.time_drop > (p.getType() < 10 ? 20000 : '\uea60')) {
                    this.potions.remove(p);
                }
            }
        }

        Vector<GemItem> gemItem = this.gems;
        int size = gemItem.size() - 1;

        for (int ix = size; ix >= 0; ix--) {
            try {
                GemItem item = gemItem.elementAt(ix);
                if (now - item.time_drop > 5000L && item.belongUser != 0) {
                    item.belongUser = 0;
                }

                if (now - item.time_drop > 30000L) {
                    gemItem.remove(item);
                }
            } catch (Exception var9) {
            }
        }

        size = this.items.size() - 1;

        for (int ix = size; ix >= 0; ix--) {
            try {
                Item itemx = this.items.elementAt(ix);
                if (now - itemx.time_drop > 5000L && itemx.belongUser != 0) {
                    itemx.belongUser = 0;
                }

                if (now - itemx.time_drop > 20000L) {
                    this.items.remove(itemx);
                    RealController.intance.idGen.putID(itemx.id, 3, "delete item timeout in map");
                }
            } catch (Exception var8) {
            }
        }
    }

    protected Potion getPotion(short id, int country) {
        for (int i = 0; i < this.potions.size(); i++) {
            try {
                if (this.potions.get(i).id == id) {
                    return this.potions.get(i);
                }
            } catch (Exception var5) {
                break;
            }
        }

        return null;
    }

    public void doGetPotion(Char player, short id) throws IOException {
        Potion pt = this.getPotion(id, player.inCountry);
        if (player.isFullInventory() && pt != null && player.potions[pt.getType()] == 0 && pt.getType() > 0) {
            Message m = new Message(27);
            m.dos.writeShort(player.id);
            m.dos.writeUTF("Hành trang đã đầy");
            player.sendMessage(m);
            m.cleanup();
        } else {
            if (pt != null) {
                if (pt.getType() != 7
                    && pt.getType() != 8
                    && pt.getType() != 9
                    && (
                        pt.getType() == 0
                            || Map.isPotionUnlimit(pt.getType())
                            || player.potions[pt.getType()] < 999 && pt.getType() != 85 && pt.getType() != 80 && pt.getType() != 88
                            || pt.getType() == 78
                            || pt.getType() == 80
                            || pt.getType() == 35
                    )
                    && (pt.belongUser == player.charDBID || pt.belongUser == 0)) {
                    this.removePotion(pt, player.inCountry);
                    player.potions[pt.getType()] += pt.quantity;
                    if (pt.getType() == 0) {
                        player.addXu(pt.quantity + pt.quantity * player.getEffSkillClanMember(0) / 100, "PrivateDataMap 3");
                    }

                    if (pt.getType() != 0
                        && pt.getType() != 85
                        && !Map.isPotionUnlimit(pt.getType())
                        && pt.getType() != 80
                        && pt.getType() != 88
                        && player.potions[pt.getType()] > 999) {
                        player.potions[pt.getType()] = 999;
                    }

                    Message m = new Message(19);
                    m.dos.writeShort(player.id);
                    m.dos.writeShort(pt.id);
                    m.dos.writeByte(pt.getType());
                    m.dos.writeShort(pt.quantity);
                    player.sendMessage(m);
                    player.sendToNearPlayer(m);
                    m.cleanup();
                    if (pt.getType() == 85 || pt.getType() == 80 || pt.getType() == 88 || Map.isPotionUnlimit(pt.getType())) {
                        Database.instance
                            .saveOrtherLog(
                                "tob_log_other_potion",
                                player.charname,
                                pt.getType() + " Nhat dc " + LoginHandler.PORTION_NAME[pt.getType()] + " " + pt.quantity,
                                LoginHandler.PORTION_NAME[pt.getType()]
                            );
                    }
                }
            } else {
                Message m = new Message(20);
                m.dos.writeByte(4);
                m.dos.writeShort(id);
                player.sendMessage(m);
                m.cleanup();
            }
        }
    }

    private void doGetPotion(Char player, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = dis.readShort();
        Potion pt = this.getPotion(id, player.inCountry);
        if (player.isFullInventory() && pt != null && player.potions[pt.getType()] == 0 && pt.getType() > 0) {
            Message m = new Message(27);
            m.dos.writeShort(player.id);
            m.dos.writeUTF("Hành trang đã đầy");
            player.sendMessage(m);
            m.cleanup();
        } else {
            if (pt != null) {
                if (pt.getType() != 7
                    && pt.getType() != 8
                    && pt.getType() != 9
                    && (
                        pt.getType() == 0
                            || Map.isPotionUnlimit(pt.getType())
                            || player.potions[pt.getType()] < 999 && pt.getType() != 78 && pt.getType() != 85 && pt.getType() != 80 && pt.getType() != 35
                            || pt.getType() == 78
                            || pt.getType() == 80
                            || pt.getType() == 35
                    )
                    && (pt.belongUser == player.charDBID || pt.belongUser == 0)) {
                    this.removePotion(pt, player.inCountry);
                    player.potions[pt.getType()] += pt.quantity;
                    if (pt.getType() == 0) {
                        player.addXu(pt.quantity + pt.quantity * player.getEffSkillClanMember(0) / 100, "PrivateDataMap 4");
                    }

                    if (pt.getType() != 0
                        && !Map.isPotionUnlimit(pt.getType())
                        && pt.getType() != 85
                        && pt.getType() != 78
                        && pt.getType() != 80
                        && pt.getType() != 35
                        && player.potions[pt.getType()] > 999) {
                        player.potions[pt.getType()] = 999;
                    }

                    Message m = new Message(19);
                    m.dos.writeShort(player.id);
                    m.dos.writeShort(pt.id);
                    m.dos.writeByte(pt.getType());
                    m.dos.writeShort(pt.quantity);
                    player.sendMessage(m);
                    player.sendToNearPlayer(m);
                    m.cleanup();
                    if (pt.getType() == 125 || pt.getType() == 80 || pt.getType() == 88 || Map.isPotionUnlimit(pt.getType())) {
                        Database.instance
                            .saveOrtherLog(
                                "tob_log_other_potion",
                                player.charname,
                                pt.getType() + " Nhat dc " + LoginHandler.PORTION_NAME[pt.getType()] + " " + pt.quantity,
                                LoginHandler.PORTION_NAME[pt.getType()]
                            );
                    }
                }
            } else {
                Message m = new Message(20);
                m.dos.writeByte(4);
                m.dos.writeShort(id);
                player.sendMessage(m);
                m.cleanup();
            }
        }
    }

    protected void removeGem(GemItem pt, int country) {
        this.gems.remove(pt);
    }

    protected void removePotion(Potion pt, int country) {
        this.potions.remove(pt);
    }

    protected void removeItem(Item pt, int country) {
        this.items.remove(pt);
    }

    public void addPlayerMessage(Char p, Message message) {
        this.playerMessages.add(new PlayerMessage(p, message));
    }

    public void sendAllCharMap(Message m) {
        Vector<Char> allcharmap = this.players;

        for (int i = 0; i < allcharmap.size(); i++) {
            try {
                Char p = allcharmap.get(i);
                p.sendMessage(m);
            } catch (Exception var5) {
            }
        }
    }

    public void sendDynamicEff() {
        for (int i = 0; i < this.players.size(); i++) {
            Char player = this.players.get(i);

            for (int j = 0; j < pos_fire_work[this.typeParty].length; j += 2) {
                Message m = MessageCreator.createMsgDynamicEff(
                    player, pos_fire_work[this.typeParty][j] * 16, pos_fire_work[this.typeParty][j + 1] * 16, 0, 0, 1, 0, 45, 5
                );
                player.sendMessage(m);
            }
        }
    }
}
