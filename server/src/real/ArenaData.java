/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.CharInfo;
import data.Database;
import data.GemItem;
import data.ItemInfo;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;

public class ArenaData extends Thread {
    public static final byte TYPE_ARENA = 0;
    public static final byte TYPE_DUN = 1;
    public static final byte TYPE_CHALLENGE_CLAN = 2;
    public static final byte TYPE_CHALLENGE_INDIVIDUAL = 3;
    public static final byte TYPE_CHALLENGE_PARTY = 4;
    private static final int DELAY_UPDATE_MAP = 300;
    private Object LOCK1 = new Object();
    public String charMaster = "";
    public int idArena;
    public int moneyThachdau = 0;
    public byte typeArena;
    public boolean isFinish = false;
    public long timeStart = 0L;
    public Vector<Monster> monster = new Vector<>();
    public Vector<Char> players = new Vector<>();
    public Vector<Potion> potions = new Vector<>();
    public Vector<Item> items = new Vector<>();
    public Vector<GemItem> gems = new Vector<>();
    public Vector<TeamArena> team = new Vector<>();
    Vector<PlayerMessage> playerMessages = new Vector<>();
    public long timeWaitFight = 0L;
    public ArenaMap parrent;
    short idMonster = -32000;
    private long lastTimeUpdateMap1;
    static String[] nameTeam = new String[]{"Đỏ", "Xanh"};
    int sendAlert = 0;
    private long timeDelayfight = 0L;
    public static int timeFight = 5;
    static int[][] pos_fire_work = new int[][]{
        {8, 29, 12, 25, 12, 19, 12, 13, 13, 8, 26, 8, 27, 13, 27, 19, 27, 25, 31, 29, 17, 8, 22, 8},
        {16, 35, 12, 29, 15, 25, 12, 21, 15, 17, 12, 13, 15, 11, 24, 11, 18, 10, 21, 10, 27, 13, 24, 17, 27, 21, 24, 25, 27, 29, 23, 35},
        {16, 35, 12, 29, 15, 25, 12, 21, 15, 17, 12, 13, 15, 11, 24, 11, 18, 10, 21, 10, 27, 13, 24, 17, 27, 21, 24, 25, 27, 29, 23, 35},
        {16, 35, 12, 29, 15, 25, 12, 21, 15, 17, 12, 13, 15, 11, 24, 11, 18, 10, 21, 10, 27, 13, 24, 17, 27, 21, 24, 25, 27, 29, 23, 35}
    };
    Vector<CharInfo> charInfo = new Vector<>();
    String infoResult = "HÒA";
    public static int MAX_WIN = 100;
    byte win = -1;
    String infoWin = "";
    boolean sendInfoMatch = false;

    public ArenaData(int typeParty, ArenaMap map) {
        this.parrent = map;
        this.typeArena = (byte)typeParty;
        new Thread(this).start();
        this.timeStart = System.currentTimeMillis();
    }

    @Override
    public void start() {
        this.timeStart = System.currentTimeMillis();
        new Thread(this).start();
    }

    public ArenaData() {
    }

    public void playerJoin(Char p) {
        this.players.add(p);
    }

    public void playerExit(Char p) {
        this.players.remove(p);

        for (int i = 0; i < this.players.size(); i++) {
            if (this.players.get(i).getName().toLowerCase().equals(p.getName().toLowerCase())) {
                this.players.remove(i);
            }
        }

        if (this.typeArena == 3 && this.players.size() > 0) {
            this.checkWin(p, this.players.get(0));
        }

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
                                if (pp.id != p.id && p.near(pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry) {
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
                                        Map.addXPForChar(pp, temp + pp.getEffSkillClan(0) * temp / 100, false, "ArenaData1");
                                    }
                                }
                            }

                            xpReceive = newxp * 20 / 100 * doubleALL;
                            xpReceive = p.expReceive(xpReceive);
                            Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "ArenaData2");
                        } else {
                            int var54 = dxp * doubleALL;
                            var54 = p.expReceive(var54);
                            Map.addXPForChar(p, var54 + p.getEffSkillClan(0) * var54 / 100, false, "ArenaData3");
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
                    }
                }

                i++;
                if (mst.size() >= nmonster[_level]) {
                    break;
                }
            }

            try {
                if (p.charthanthu != null && muctieu.size() > 0) {
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
                    Map.addXPForChar(p, var55 + p.getEffSkillClan(0) * var55 / 100, false, "ArenaData6");
                } else {
                    int nUser = p.party.userParty.size();
                    if (nUser > 1) {
                        nUser = 5;
                    }

                    int xpReceive = newxp * 80 / (nUser * 100);
                    int maxLv = p.lvDetail.lv;

                    for (int k = 0; k < p.party.userParty.size(); k++) {
                        Char pp = p.party.userParty.get(k);
                        if (pp.id != p.id && p.near(pp, 320) && pp.mapID == p.mapID && pp.inCountry == p.inCountry) {
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
                                Map.addXPForChar(pp, temp + pp.getEffSkillClan(0) * temp / 100, false, "ArenaData4");
                            }
                        }
                    }

                    xpReceive = newxp * 20 / 100;
                    xpReceive = p.expReceive(xpReceive);
                    Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "ArenaData5");
                }
            }

            p.charHireAttackMultiMOnster(mst, _type);
        } catch (Exception var41) {
            var41.printStackTrace();
        }
    }

    public synchronized void doAddMonster(Char p) {
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

                this.startMatch();
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

                int teamwin = this.checkWin();
                if (this.timeOut()) {
                    if (this.sendAlert == 0) {
                        this.sendAlert = 1;
                        if (teamwin == 2 || teamwin == -1) {
                            this.sendAllCharMap(MessageCreator.createServerAlertMessage("Trận đấu kêt thúc.Hai đội hòa nhau.", ""));
                            this.onDraw();
                        } else if (teamwin > -1) {
                            if (this.typeArena != 3) {
                                this.sendAllCharMap(MessageCreator.createServerAlertMessage("Chúc mừng đội " + nameTeam[teamwin] + " đã chiến thắng.", ""));
                            } else {
                                this.sendAllCharMap(MessageCreator.createServerAlertMessage("Chúc mừng " + this.infoWin, ""));
                            }

                            this.addGift(teamwin);
                        }

                        this.resetAll();
                    } else if (this.sendAlert == 1 && System.currentTimeMillis() - this.timeStart > timeFight + 60000) {
                        this.doSendResult(0);
                        this.sendAlert = 2;
                    }

                    if (System.currentTimeMillis() - this.timeStart > (timeFight + 1) * 60000) {
                        while (this.players.size() > 0) {
                            Char player = this.players.remove(0);
                            if (player.isBot == -1) {
                                int x = 40 + Map.r.nextInt() % 5;
                                int y = 23 + Map.r.nextInt(20);
                                player.x = x;
                                player.y = y;
                                this.parrent.move2Map(player, x, y, 201, player.myCountry);
                                player.idArena = -1;
                            }
                        }

                        this.isFinish = true;
                        this.parrent.removeArena(this.idArena);
                        Database.instance.saveOrtherLog("", "dau_truong", "ket thuc " + teamwin, "arena");
                    }
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
            } catch (Exception var11) {
                var11.printStackTrace();
                System.out.println("LOI TRONG RUN PRIVATEDATAMAP " + var11.toString());
            }

            try {
                synchronized (this.LOCK1) {
                    this.LOCK1.wait(300L);
                }
            } catch (Exception var8) {
            }
        }
    }

    public void onDraw() {
        String info = "";
        if (this.typeArena == 3) {
            Char c1 = null;
            Char c2 = null;
            TeamArena t = this.team.get(0);

            for (String name : t.memTeam.values()) {
                info = name;
                c1 = CharManager.instance.getCharByCharName(name);

                try {
                    c1.addXu(this.moneyThachdau / 2, "onDraw arenadata");
                    c1.sendMessage(MessageCreator.createCharInventoryMessage(c1, 0));
                } catch (Exception var11) {
                }
            }

            info = info + " hòa : ";
            t = this.team.get(1);

            for (String name : t.memTeam.values()) {
                info = info + name;
                c2 = CharManager.instance.getCharByCharName(name);

                try {
                    c2.addXu(this.moneyThachdau / 2, "onDraw arenadata");
                    c2.sendMessage(MessageCreator.createCharInventoryMessage(c2, 0));
                } catch (Exception var10) {
                }
            }

            info = info + "_" + this.moneyThachdau / 2;

            try {
                Database.instance.saveOrtherLog("", c1.getName(), info, "darena");
            } catch (Exception var9) {
            }

            try {
                Database.instance.saveOrtherLog("", c2.getName(), info, "darena");
            } catch (Exception var8) {
            }
        }
    }

    public void update() {
        if (System.currentTimeMillis() - this.timeDelayfight > 0L && this.timeDelayfight > 0L) {
            this.timeDelayfight = 0L;
            Message m = MessageCreator.createServerAlertMessage("Trận đấu bắt đầu", "");
            this.sendAllCharMap(m);
        }

        if (this.players.size() > 0) {
            for (int i = 0; i < this.players.size(); i++) {
                this.players.get(i).update();
                this.checkKhan(this.players.get(i));
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
        return System.currentTimeMillis() - this.timeStart >= timeFight * 60000;
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
                if ((
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
                        player.addXu(pt.quantity + pt.quantity * player.getEffSkillClanMember(0) / 100, "doGetPotion arenadata");
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
                                player.getName(),
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
                if ((
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
                        player.addXu(pt.quantity + pt.quantity * player.getEffSkillClanMember(0) / 100, "doGetPotion arenadata");
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
                    if (pt.getType() == 80 || pt.getType() == 88 || Map.isPotionUnlimit(pt.getType())) {
                        Database.instance
                            .saveOrtherLog(
                                "tob_log_other_potion",
                                player.getName(),
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

    public void doSendKiller(Char player) {
    }

    public void doAttackPlayer(Char p, Message message) {
        try {
            if (System.currentTimeMillis() - this.timeDelayfight < 0L && this.timeDelayfight > 0L) {
                long time = (this.timeDelayfight - System.currentTimeMillis()) / 1000L;
                p.sendMessage(MessageCreator.createServerAlertMessage("Còn " + time + " giây nữa trận đấu mới bắt đầu", ""));
                return;
            }

            if (p.countHit() || p.freeze()) {
                return;
            }

            if (p.myCountry == -1) {
                return;
            }

            if (this.timeOut()) {
                return;
            }

            if (p.hp <= 0) {
                p.actorDie();
                return;
            }

            if (!p.checkDurableWeapone()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Không thể tấn công khi vũ khí bị hư hại. Hãy đến Thợ rèn để sửa lại.", ""));
                return;
            }

            if (this.timeOut()) {
                return;
            }

            p.downDurableWeapone();
            DataInputStream dis = message.dis;
            Char c = this.getChar(dis.readShort());
            if (c == null) {
                return;
            }

            if (c.isBot != -1) {
                return;
            }

            if (p.mapID != c.mapID) {
                return;
            }

            if (p.pk == c.pk || c.hp <= 0) {
                return;
            }

            short skill = dis.readByte();
            byte effect = 0;
            int ahp = p.attackDamage;
            int buffAttack = -1;
            int _type = (byte)skill;
            int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[_type];
            }

            long now = System.currentTimeMillis();
            if (now - p.timeLastUseSkills[_type] < CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100) {
                return;
            }

            p.timeLastUseSkills[_type] = now;
            if (_level <= 0 || !Map.inRangeSkill(p, c, CharManager.getSkillRange((byte)_type, p.charClass))) {
                return;
            }

            buffAttack = p.getBuffEffAttack();
            int damage = p.attackDam(c, _type, _level, buffAttack);
            if (this.typeArena == 3) {
                damage /= 10;
            } else {
                damage /= 5;
                damage -= damage / 3;
            }

            damage = p.subDam(c, damage);
            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            boolean critSv = p.havecrit();
            boolean baokich = p.haveBaoKich();
            if (baokich) {
                damage *= 4;
                effect = 4;
            } else if (critSv) {
                damage *= 2;
                effect = 2;
                if (p.petUsing != null) {
                    long pcLienKich = p.petUsing.getLienKich();
                    damage = (int)(damage + damage * pcLienKich / 100L);
                }
            }

            if (damage > 50000) {
                damage = 50000 + Map.r.nextInt(100);
            }

            int mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
            if (p.mp + p.percentBuff[1] < mplost) {
                return;
            }

            p.mp -= mplost;
            if (p.mp <= 0) {
                p.mp = 0;
            }

            damage = c.checkHapthuSatThuong(damage, p);
            damage = c.checkGiamSatThuong(damage);
            damage = c.checkPassAttack(p, damage);
            ahp = damage / CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            if (ahp < 0) {
                ahp = 1;
            }

            c.hp = (int)(c.hp - (damage - c.checkMagicShield(damage)));
            int damNguyetAnh = p.getPCDamNguyetAnh(skill);
            if (c.hp > 0 && damNguyetAnh > 0) {
                c.hp = c.hp - c.maxhp * damNguyetAnh / 100;
                damage += c.maxhp * damNguyetAnh / 100;
                p.sendEffectBuff(c, EffectBuff.EFF_NGUYET_ANH, 1000);
            }

            c.downDuarable();
            if (p.charthanthu != null && c.hp > 0) {
                Vector<LiveActor> target = new Vector<>();
                target.add(c);
                p.charthanthu.doAttack(target);
                c.hp = c.hp - p.getDamtThanThu(c);
            }

            if (c.hp <= 0) {
                c.hp = 0;
                if (!c.isCharCopy()) {
                    c.timedie = System.currentTimeMillis();
                    c.timeWaitComeHome = c.timedie;
                    p.pArena += 10;
                    c.pArena -= 5;
                    if (c.pArena < 0) {
                        c.pArena = 0;
                    }

                    p.totalKillInArena++;
                    c.nBekill++;
                    this.doAddPointTeam(p, c);
                    if (this.typeArena == 3) {
                        this.checkWin(c, p);
                    }
                }

                if (c.isCharCopy()) {
                    c.actorDie();
                }
            } else {
                if (ahp > 0) {
                    p.buffAttackSkill(damage, c);
                }

                p.buffSkillKham(c);
                p.charHireAttackDam(c, _type, _level, buffAttack);
            }

            Message m = new Message(6);
            m.dos.writeShort(p.id);
            m.dos.writeShort(c.id);
            m.dos.writeByte(skill);
            m.dos.writeInt(ahp);
            m.dos.writeInt(c.hp);
            m.dos.writeByte(effect);
            m.dos.writeByte(CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1]);
            m.dos.writeByte(buffAttack);
            m.dos.writeByte(_level);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            m.cleanup();
        } catch (Exception var19) {
            var19.printStackTrace();
        }
    }

    private void doAddPointTeam(Char p, Char c) {
        if (p.pk == 14) {
            this.team.get(0).totalPoint += 10;
        } else if (p.pk == 15) {
            this.team.get(1).totalPoint += 10;
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

    public void notifyMap() {
        try {
            synchronized (this.LOCK1) {
                this.LOCK1.notify();
            }
        } catch (Exception var3) {
        }
    }

    public void addPlayerMessage(Char p, Message message) {
        this.playerMessages.add(new PlayerMessage(p, message));
        this.notifyAll();
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

    private Char getChar(short id) {
        return CharManager.instance.getByCharID(id);
    }

    public void sendDynamicEff() {
        for (int i = 0; i < this.players.size(); i++) {
            Char player = this.players.get(i);

            for (int j = 0; j < pos_fire_work[this.typeArena].length; j += 2) {
                Message m = MessageCreator.createMsgDynamicEff(
                    player, pos_fire_work[this.typeArena][j] * 16, pos_fire_work[this.typeArena][j + 1] * 16, 0, 0, 1, 0, 45, 5
                );
                player.sendMessage(m);
            }
        }
    }

    public void addTeam(TeamArena team) {
        this.team.add(team);
    }

    public void doSendResult(int team) {
        Message m = new Message(-19);

        try {
            m.dos.writeByte(4);
            m.dos.writeUTF(this.infoResult);
            Vector<CharInfo> info = new Vector<>();
            Hashtable<String, String> t1 = this.team.get(0).memTeam;

            for (String name : t1.values()) {
                CharInfo charinfo = Database.instance.getInfoChar(name);
                charinfo.name = charinfo.name + " - đỏ";
                Char p = CharManager.instance.getCharByCharName(name);
                if (p != null) {
                    charinfo.nKillInArena = p.totalKillInArena;
                    charinfo.bekill = p.nBekill;
                }

                info.add(charinfo);
            }

            t1 = this.team.get(1).memTeam;

            for (String name : t1.values()) {
                CharInfo charinfo = Database.instance.getInfoChar(name);
                charinfo.name = charinfo.name + " - xanh";
                Char p = CharManager.instance.getCharByCharName(name);
                if (p != null) {
                    charinfo.nKillInArena = p.totalKillInArena;
                    charinfo.bekill = p.nBekill;
                }

                info.add(charinfo);
            }

            m.dos.writeShort(info.size());

            for (int i = 0; i < info.size(); i++) {
                CharInfo charinfo = info.get(i);
                String online = " - giết " + charinfo.nKillInArena + " - chết " + charinfo.bekill;
                m.dos.writeUTF(charinfo.name + online);
                m.dos.writeByte(charinfo.headStyle);
                m.dos.writeByte(charinfo.level);
                m.dos.writeByte(charinfo.wearingItem.size());

                for (int j = 0; j < charinfo.wearingItem.size(); j++) {
                    ItemInfo item = charinfo.wearingItem.get(j);
                    m.dos.writeByte(item.charClass);
                    m.dos.writeShort(item.idTemplate);
                    m.dos.writeByte(item.level);
                    m.dos.writeByte(item.plus);
                }

                m.dos.writeShort(charinfo.idClan);
                m.dos.writeByte(charinfo.titlesClan);
                m.dos.writeLong(charinfo.money);
                m.dos.writeInt(charinfo.luong);
                m.dos.writeByte(charinfo.country);
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        this.sendAllCharMap(m);
    }

    public void setTeam(Char player) {
        TeamArena t = this.team.get(0);

        for (String name : t.memTeam.values()) {
            Char p = CharManager.instance.getCharByCharName(name);
            if (p != null && p.getName().toLowerCase().equals(player.getName().toLowerCase())) {
                player.pk = 14;

                try {
                    Message m = new Message(65);
                    m.dos.writeShort(player.id);
                    m.dos.writeByte(1);
                    m.dos.writeByte(14);
                    player.sendMessage(m);
                    player.timeUsePK = System.currentTimeMillis();
                } catch (Exception var8) {
                }

                return;
            }
        }

        t = this.team.get(1);

        for (String namex : t.memTeam.values()) {
            Char p = CharManager.instance.getCharByCharName(namex);
            if (p != null && p.getName().toLowerCase().equals(player.getName().toLowerCase())) {
                player.pk = 15;

                try {
                    Message m = new Message(65);
                    m.dos.writeShort(player.id);
                    m.dos.writeByte(1);
                    m.dos.writeByte(15);
                    player.timeUsePK = System.currentTimeMillis();
                    player.sendMessage(m);
                } catch (Exception var7) {
                }

                return;
            }
        }
    }

    public int checkWin() {
        int p1 = this.team.get(0).totalPoint;
        int p2 = this.team.get(1).totalPoint;
        if (this.typeArena == 0) {
            if (this.timeOut()) {
                if (p1 > p2) {
                    this.infoResult = "ĐỘI ĐỎ THẮNG";
                    return 0;
                }

                if (p1 < p2) {
                    this.infoResult = "ĐỘI XANH THẮNG";
                    return 1;
                }

                this.infoResult = "HÒA";
                return 2;
            }

            int d1 = p1 / 10;
            int d2 = p2 / 10;
            if (d1 >= MAX_WIN && d2 < MAX_WIN) {
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;
                this.infoResult = "ĐỘI ĐỎ THẮNG";
                return 0;
            }

            if (d1 < MAX_WIN && d2 >= MAX_WIN) {
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;
                this.infoResult = "ĐỘI XANH THẮNG";
                return 1;
            }

            if (d1 >= MAX_WIN && d2 >= MAX_WIN) {
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;
                return 2;
            }
        } else if (this.typeArena == 3) {
            return this.checkWin(null, null);
        }

        return -1;
    }

    public byte checkWin(Char plose, Char pWin) {
        if (plose != null) {
            if (plose.pk == 14) {
                this.win = 1;
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;
            } else {
                this.win = 0;
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;
            }

            this.infoResult = pWin.getName() + " chiến thắng";
            this.infoWin = pWin.getName() + " đã chiến thắng " + plose.getName();
            Database.instance.saveOrtherLog("", pWin.getName(), this.infoWin, "winarena");
            Database.instance.saveOrtherLog("", plose.getName(), this.infoWin, "losearena");
        }

        return this.win;
    }

    public void addGift(int twin) {
        if (twin == 0 || twin == 1) {
            TeamArena t = this.team.get(twin);
            String pLose = "";

            for (int i = 0; i < t.v_memTeam.size(); i++) {
                String name = t.v_memTeam.get(i);
                Char p = CharManager.instance.getCharByCharName(name);

                try {
                    if (this.typeArena != 3) {
                        String info = twin + "_" + p.pArena;
                        p.pArena += 20;
                        p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                        info = info + "_" + p.pArena;
                        Database.instance.saveOrtherLog("", name, info, "garena");
                    } else if (p != null) {
                        if (twin == 0) {
                            Iterator var8 = this.team.get(1).memTeam.values().iterator();
                            if (var8.hasNext()) {
                                String name1 = (String)var8.next();
                                pLose = name1;
                            }
                        } else {
                            Iterator var14 = this.team.get(0).memTeam.values().iterator();
                            if (var14.hasNext()) {
                                String name1 = (String)var14.next();
                                pLose = name1;
                            }
                        }

                        String info = p.getName() + " chien thang " + pLose + " va nhan dc " + this.moneyThachdau + " xu";
                        p.addXu(this.moneyThachdau, "addGift arenadata");
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                        Database.instance.saveOrtherLog("", name, info, "garena");
                    } else {
                        Database.instance.saveOrtherLog("", name, name + " ko online khi add gift " + this.moneyThachdau, "garenaf");
                    }
                } catch (Exception var9) {
                }
            }
        }
    }

    public void checkKhan(Char p) {
        if (this.sendAlert == 0) {
            try {
                TeamArena t = this.team.get(0);
                if (t.memTeam.get(p.getName()) != null && p.pk != 14) {
                    p.pk = 14;
                    Message m = new Message(65);
                    m.dos.writeShort(p.id);
                    m.dos.writeByte(1);
                    m.dos.writeByte(14);
                    p.sendMessage(m);
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                    p.sendToNearPlayer(m);
                    return;
                }
            } catch (Exception var5) {
            }

            try {
                TeamArena t = this.team.get(1);
                if (t.memTeam.get(p.getName()) != null && p.pk != 15) {
                    p.pk = 15;
                    Message m = new Message(65);
                    m.dos.writeShort(p.id);
                    m.dos.writeByte(1);
                    m.dos.writeByte(15);
                    p.sendMessage(m);
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                    p.sendToNearPlayer(m);
                }
            } catch (Exception var4) {
            }
        }
    }

    public void resetAll() {
        TeamArena t = this.team.get(0);

        for (String name : t.memTeam.values()) {
            Char p = CharManager.instance.getCharByCharName(name);

            try {
                p.idArena = -1;
                Message m = new Message(65);
                m.dos.writeShort(p.id);
                p.pk = 0;
                m.dos.writeByte(0);
                m.dos.writeByte(14);
                p.sendMessage(m);
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            } catch (Exception var7) {
            }
        }

        t = this.team.get(1);

        for (String name : t.memTeam.values()) {
            Char p = CharManager.instance.getCharByCharName(name);

            try {
                p.idArena = -1;
                Message m = new Message(65);
                m.dos.writeShort(p.id);
                p.pk = 0;
                m.dos.writeByte(0);
                m.dos.writeByte(15);
                p.sendMessage(m);
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            } catch (Exception var6) {
            }
        }
    }

    public void startMatch() {
        if (this.sendInfoMatch) {
            TeamArena t = this.team.get(0);

            for (String name : t.memTeam.values()) {
                Char p = CharManager.instance.getCharByCharName(name);

                try {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Trận đấu của bạn sắp bắt đầu. Hãy sẵn sàng để tham gia nhé.", ""));
                } catch (Exception var9) {
                }
            }

            t = this.team.get(1);

            for (String name : t.memTeam.values()) {
                Char p = CharManager.instance.getCharByCharName(name);

                try {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Trận đấu của bạn sắp bắt đầu. Hãy sẵn sàng để tham gia nhé.", ""));
                } catch (Exception var8) {
                }
            }

            this.sendInfoMatch = false;
        } else if (System.currentTimeMillis() - this.timeWaitFight > 0L && this.timeWaitFight > 0L) {
            this.timeDelayfight = System.currentTimeMillis() + 30000L;
            TeamArena t = this.team.get(0);

            for (String name : t.memTeam.values()) {
                Char c1 = CharManager.instance.getCharByCharName(name);

                try {
                    if (this.typeArena != 3) {
                        c1.map.move2Map(c1, 24 + Map.r.nextInt() % 3, 10 + Map.r.nextInt() % 3, 204, c1.myCountry);
                    } else {
                        c1.map.move2Map(c1, 22, 9, 205, c1.myCountry);
                    }
                } catch (Exception var7) {
                }
            }

            t = this.team.get(1);

            for (String name : t.memTeam.values()) {
                Char c2 = CharManager.instance.getCharByCharName(name);

                try {
                    if (this.typeArena != 3) {
                        c2.map.move2Map(c2, 24 + Map.r.nextInt() % 3, 36 + Map.r.nextInt() % 3, 204, c2.myCountry);
                    } else {
                        c2.map.move2Map(c2, 22, 37, 205, c2.myCountry);
                    }
                } catch (Exception var6) {
                }
            }

            this.timeWaitFight = 0L;
        }
    }

    public void onActorDie(LiveActor ac) {
        if (this.typeArena == 3) {
            Char plose = (Char)ac;
            Char pWin = null;
            String nameWin = "";
            if (plose.pk == 7) {
                this.win = 1;
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;

                for (String name : this.team.get(1).memTeam.values()) {
                    nameWin = name;
                    pWin = CharManager.instance.getCharByCharName(name);
                }
            } else {
                this.win = 0;
                this.timeStart = System.currentTimeMillis() - timeFight * 60000;

                for (String name : this.team.get(0).memTeam.values()) {
                    nameWin = name;
                    pWin = CharManager.instance.getCharByCharName(name);
                }
            }

            this.infoResult = nameWin + " chiến thắng";
            this.infoWin = nameWin + " đã chiến thắng " + plose.getName();
            Database.instance.saveOrtherLog("", nameWin, this.infoWin, pWin != null ? "winarena" : "winarena1");
            Database.instance.saveOrtherLog("", plose.getName(), this.infoWin, "losearena");
        }
    }
}
