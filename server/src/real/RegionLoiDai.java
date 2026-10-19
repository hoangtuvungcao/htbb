/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.CharInfo;
import data.Database;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;

public class RegionLoiDai {
    Vector<Vector<PlayerMessage>> allPlayerMessages = new Vector<>();
    public Vector<Monster> monstertem = new Vector<>();
    public byte total_cuongthi = 0;
    private Object LOCK = new Object();
    private Object LOCK2 = new Object();
    private Object LOCK1 = new Object();
    private long lastTimeUpdateMap;
    private long lastTimeUpdateMap1;
    private long lastTimeUpdateMap2;
    private int map_run_state;
    private int map_run_state1;
    private int map_run_state2;
    short idRegion = 0;
    boolean running = true;
    public boolean isChauBau = false;
    public boolean comback = false;
    public Vector<Monster> allTruTop = new Vector<>();
    public Vector<Monster> allTruMid = new Vector<>();
    public Vector<Monster> allTruBot = new Vector<>();
    public Vector<Monster> allTruMidMain = new Vector<>();
    public Vector<Monster> allTruTop1 = new Vector<>();
    public Vector<Monster> allTruMid1 = new Vector<>();
    public Vector<Monster> allTruBot1 = new Vector<>();
    public Vector<Monster> allTruMidMain1 = new Vector<>();
    public Vector<Monster> allQuaiOcDao = new Vector<>();
    public String infoPlayer = "";
    Map map;
    long timeExist;
    private Vector<Char> allPlayers = new Vector<>();
    private Vector<Char> allPlayersView = new Vector<>();
    private Vector<Vector<EffectBuff>> EFFECT_AUTO = new Vector<>();
    public Vector<Vector<Monster>> tempMonster = new Vector<>();
    public Vector<Vector<Monster>> tempRemoveMonster = new Vector<>();
    private Vector<Hashtable<Short, Monster>> temp_monsters = new Vector<>();
    boolean isStop = false;
    public long tend = System.currentTimeMillis() + 300000L;
    boolean isSkelonton = false;
    long timeCheck = System.currentTimeMillis() + 5000L;
    int teamWin = 0;
    static long timeDropCake = System.currentTimeMillis() + 7200000L;
    public static short[] MAX_RANGE_CHAR = new short[]{120, 120, 140, 120, 160};
    int pointC1 = 0;
    int pointC2 = 0;
    String name1 = "";
    String point_begin1 = "";
    String name2 = "";
    String point_begin2 = "";
    public Char charBoss1;
    public Char charBoss2;
    public boolean isHaveChienthan = false;
    short countT1 = 0;
    short countT2 = 0;
    byte[] countWin = new byte[2];
    byte van = 1;
    boolean checking = false;
    public boolean isFinish = false;
    public long timeChuyenMap = 0L;
    public long timeWaitStart;
    public MatchLoiDai match = null;

    public RegionLoiDai() {
    }

    public RegionLoiDai(Map m, int id) {
        this.map = m;
        this.timeExist = System.currentTimeMillis();
        this.idRegion = (short)id;
        (new Thread() {
            private long lastTimeUpdateMap;

            @Override
            public void run() {
                while (RegionLoiDai.this.running && !AdminHandler.isStopServer && !RegionLoiDai.this.isStop) {
                    try {
                        while (RealController.savingChar) {
                            Thread.sleep(100L);
                        }

                        long l1 = System.currentTimeMillis();
                        if (l1 - this.lastTimeUpdateMap >= Map.DELAY_UPDATE_MAP) {
                            RegionLoiDai.this.update();
                            this.lastTimeUpdateMap = System.currentTimeMillis();
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
                    }

                    try {
                        synchronized (RegionLoiDai.this.LOCK) {
                            RegionLoiDai.this.LOCK.wait(500L);
                        }
                    } catch (Exception var4) {
                        var4.printStackTrace();
                    }
                }
            }
        }).start();
        this.allPlayerMessages.add(new Vector<>());
        this.allPlayerMessages.add(new Vector<>());
        this.EFFECT_AUTO.add(new Vector<>());
        this.EFFECT_AUTO.add(new Vector<>());
        this.startLeafVilage();
        this.startSandVilage();
    }

    public void startLeafVilage() {
    }

    public void startWindVilage() {
    }

    public void startSandVilage() {
        (new Thread() {
            @Override
            public void run() {
                while (!RegionLoiDai.this.isStop) {
                    try {
                        if (AdminHandler.isStopServer) {
                            RegionLoiDai.this.map_run_state = 0;
                            break;
                        }

                        while (RealController.savingChar) {
                            Thread.sleep(100L);
                        }

                        RegionLoiDai.this.map_run_state = 0;
                        long l1 = System.currentTimeMillis();
                        if (l1 - RegionLoiDai.this.lastTimeUpdateMap >= Map.DELAY_UPDATE_MAP) {
                            RegionLoiDai.this.updateSand();
                            RegionLoiDai.this.lastTimeUpdateMap = System.currentTimeMillis();
                        }

                        RegionLoiDai.this.map_run_state = 1;
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
                    }

                    try {
                        synchronized (RegionLoiDai.this.LOCK) {
                            RegionLoiDai.this.LOCK.wait(Map.DELAY_UPDATE_MAP);
                        }
                    } catch (Exception var4) {
                    }
                }
            }
        }).start();
        (new Thread() {
            @Override
            public void run() {
                while (!RegionLoiDai.this.isStop) {
                    try {
                        int count = 0;
                        new Vector();
                        Vector<PlayerMessage> playerMessages = RegionLoiDai.this.allPlayerMessages.get(0);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                RegionLoiDai.this.map.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }

                        synchronized (RegionLoiDai.this.LOCK) {
                            RegionLoiDai.this.LOCK.wait(Map.timeDelay);
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void update() {
    }

    public void updateWind() {
    }

    public void sendInfoPlayer(Char p) {
    }

    public void updateSand() {
        Vector<Char> players = this.allPlayers;
        if ((this.timeChuyenMap <= 0L || System.currentTimeMillis() - this.timeChuyenMap < 0L) && System.currentTimeMillis() - this.tend < 0L) {
            int countMonsDie = 0;
            players.size();
            boolean exitBoard = false;
            int size = players.size();
            int k = 0;

            while (k < players.size()) {
                try {
                    Char p = players.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1 && !p.isCharCopy()) {
                        players.remove(k);
                        CharManager.instance.remove(p);
                    } else if ((!p.map.equals(this.map) || p.mapID != this.map.mapId) && p.isBot == -1) {
                        players.remove(k);
                    } else {
                        p.update();
                        k++;
                    }
                } catch (Exception var10) {
                    var10.printStackTrace();
                    break;
                }
            }

            size = this.allPlayersView.size();
            k = 0;

            while (k < this.allPlayersView.size()) {
                try {
                    Char p = this.allPlayersView.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1 && !p.isCharCopy()) {
                        players.remove(k);
                        CharManager.instance.remove(p);
                    } else if ((!p.map.equals(this.map) || p.mapID != this.map.mapId) && p.isBot == -1) {
                        players.remove(k);
                    } else {
                        p.update();
                        k++;
                    }
                } catch (Exception var9) {
                    var9.printStackTrace();
                    break;
                }
            }

            this.map.deletePotionAndItemOnGround(0);
        } else {
            boolean isfinish = true;
            System.out
                .println("timechuyen map regionloi dai " + (System.currentTimeMillis() - this.timeChuyenMap) + " >> " + (System.currentTimeMillis() - this.tend));
            this.doTimeOut();

            while (players.size() > 0) {
                try {
                    if (MapLoiDai.isTimeLoiDai()) {
                        int index = Map.r.nextInt(MapLoiDai.pos_sanh_cho.length / 2);
                        this.map.move2Map(players.remove(0), MapLoiDai.pos_sanh_cho[index * 2], MapLoiDai.pos_sanh_cho[index * 2 + 1], Map.idMapChoLoiDai, 0);
                    } else {
                        this.map.doReturnVillage(players.remove(0));
                    }
                } catch (Exception var8) {
                }
            }

            while (this.allPlayersView.size() > 0) {
                try {
                    this.map.doReturnVillage(this.allPlayersView.remove(0));
                } catch (Exception var7) {
                }
            }

            this.map.removeRegion(this.idRegion);
            this.isStop = true;
            this.isFinish = true;
        }
    }

    protected void doAttackPlayer(Char p, Message message) {
        try {
            if (p.cannotAttackWhenBienhinh()) {
                return;
            }

            if (p.countHit() || p.freeze() || this.isFinish) {
                return;
            }

            if (p.name_char_loi_dai == null) {
                return;
            }

            if (System.currentTimeMillis() - this.timeWaitStart < 0L) {
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

            if (p.checkLamthinh() || p.checkCamLang() || p.checkRuNgu() || p.checkChoang()) {
                return;
            }

            p.downDurableWeapone();
            boolean inArenaP = false;
            DataInputStream dis = message.dis;
            Char c = this.getChar(dis.readShort());
            if (c == null) {
                return;
            }

            if (c.name_char_loi_dai == null) {
                return;
            }

            if (c.hp <= 0) {
                return;
            }

            boolean timeAutoPK = Map.pkAuto;
            boolean haveShield = c.haveShield();
            if (p.mapID != c.mapID) {
                return;
            }

            if (p.region != c.region) {
                return;
            }

            if (!Map.inRangeActor(p, c, MAX_RANGE_CHAR[p.charClass])) {
                return;
            }

            if (p.pk_chienTruong == c.pk_chienTruong && !c.isCharCopy()) {
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

            if (p.haveTanPhe() > 0 && c.addEffBuff(EffectBuff.TAN_PHE, System.currentTimeMillis() + p.haveTanPhe(), EffectBuff.BY_ACTOR, 0) != null) {
                c.sendEffToChar(c);
                c.sendEffToNearChar();
                c.divSpeed = 2;
                c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
            }

            if (p.haveLamThinh() > 0 && c.addEffBuff(EffectBuff.LAM_THINH, System.currentTimeMillis() + p.haveLamThinh(), EffectBuff.BY_ACTOR, 0) != null) {
                c.sendEffToChar(c);
                c.sendEffToNearChar();
            }

            buffAttack = p.getBuffEffAttack();
            int damage = p.attackDam(c, _type, _level, buffAttack);
            damage /= 5;
            damage -= damage / 3;
            damage = p.subDam(c, damage);
            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
            CharChienTruong cct = MapChienTruongMoba.getCharChienTruong(p.charname);
            boolean critSv = p.havecrit();
            boolean baokich = p.haveBaoKich();
            if (baokich) {
                damage *= 4;
                effect = 4;
            } else if (critSv) {
                damage *= 2;
                effect = 2;
                if (p.petUsing != null && cct != null && cct.noi_luc >= 5) {
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

            int hphut = 0;
            if (p.haveHutHp() > 0 && p.hp < p.maxhp) {
                int hp = p.haveHutHp();
                hphut = hp;
                if (c.hp < hp) {
                    hphut = c.hp;
                }

                p.hp += hphut;
                p.calculatorHPMP();
                MessageCreator.createMsgUseHpMP(p, hp, 1);
            }

            damage += hphut;
            damage = c.checkHapthuSatThuong(damage, p);
            damage = c.checkGiamSatThuong(damage);
            damage = c.checkPassAttack(p, damage);
            ahp = damage;
            if (damage < 0) {
                ahp = 1;
            }

            c.hp = (int)(c.hp - (damage - c.checkMagicShield(damage)));
            c.checkNewEffectItem(0, damage / 10, p);
            c.downDuarable();
            if (p.charthanthu != null && c.hp > 0 && cct != null && cct.noi_luc >= 10) {
                Vector<LiveActor> target = new Vector<>();
                target.add(c);
                p.charthanthu.doAttack(target);
                c.hp = c.hp - p.getDamtThanThu(c);
            }

            if (c.hp <= 0) {
                if (c.hp <= 0) {
                    c.hp = 0;
                    if (c.isCharCopy()) {
                        c.actorDie();
                    }
                }

                this.map.checkTrade(c);
            } else {
                if (ahp > 0) {
                    p.buffAttackSkill(damage, c);
                }

                p.buffSkillKham(c);
                p.charHireAttackDam(c, _type, _level, buffAttack);
            }

            if (c.hp <= 0) {
                c.doSetTimeAutoHoiSinhMapMoba();
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
        } catch (Exception var23) {
            var23.printStackTrace();
        }
    }

    public void doAttackMultiTarget(Char p, Message message) {
        try {
            if (p.cannotAttackWhenBienhinh()) {
                return;
            }

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
                } catch (Exception var41) {
                }

                return;
            }

            if (p.checkLamthinh() || p.checkCamLang() || p.checkRuNgu() || p.checkChoang()) {
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
            CharChienTruong c = MapChienTruongMoba.getCharChienTruong(p.charname);
            Monster firstMonster = null;
            Monster mt = this.getMonster(dis.readShort(), p.inCountry);
            firstMonster = mt;
            if (mt == null || mt.isDead) {
                if (mt != null) {
                    Map.onMosterDie(p, mt, skill, 1, effect, (byte)0);
                }

                return;
            }

            if (mt.isThungGoNuiChauBau()) {
                if (Map.inRangeActor(p, mt, 48) && p.monsTerThuThap == null) {
                    p.monsTerThuThap = mt;
                    p.monsTerThuThap.setTimeThuThap(10);
                    p.sendMessage(MessageCreator.createMsgTimeCountdown("Mở", 10, mt.cat, mt.id, Map.COUNT_DOWN, -1));
                }

                return;
            }

            if (mt.getCharID() == p.charDBID) {
                return;
            }

            if (!Map.inRangeActor(p, mt, MAX_RANGE_CHAR[p.charClass])) {
                return;
            }

            if (mt.map.mapId != p.mapID) {
                return;
            }

            if (mt.isMyMonster(p)) {
                return;
            }

            if (!Map.canAttackMonsterVantieu(mt, p)) {
                return;
            }

            int _type = skill;
            int _level = p.skill[skill] + p.addMoreLevelSkill[skill];
            if (_level <= 0) {
                _level = p.addMoreLevelSkill[skill];
            }

            if (_level <= 0 || !Map.inRangeSkill(p, mt, MAX_RANGE_CHAR[p.charClass])) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - p.timeLastUseSkills[skill] < p.coolDown[skill][_level]) {
                return;
            }

            int hphut = 0;
            if (p.haveHutHp() > 0 && p.hp < p.maxhp && !mt.isNgocRong() && !mt.isRuongMaquai()) {
                int hp = p.haveHutHp();
                hphut = hp;
                if (mt.hp < hp) {
                    hphut = mt.hp;
                }

                p.hp += hphut;
                p.calculatorHPMP();
                MessageCreator.createMsgUseHpMP(p, hp, 1);
            }

            p.timeLastUseSkills[skill] = now;
            buffAttack = p.getBuffEffAttack();
            if (mt.resistThroughArmor()) {
                buffAttack = -1;
            }

            int damage = p.attackDam(mt, skill, _level, buffAttack);
            if (mt.isNgocRong()) {
                if (damage == 1) {
                    if (p.potions[73] == 0) {
                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Không có túi nhặt ngọc"));
                    } else {
                        mt.onDropItem(this.map, p);
                    }
                } else if (mt.isDead) {
                    mt.sendActorDie(p);
                }

                return;
            }

            if (mt.haveDodge()) {
                damage = 0;
                buffAttack = -1;
            }

            if (mt.isMonsterVantieu() && damage >= mt.maxhp / 100) {
                damage = mt.maxhp / 100 + Map.r.nextInt() % 100;
            }

            damage *= CharManager.UP_DAMGE_SKILL[p.charClass][skill][_level - 1];
            boolean critSv = p.havecrit();
            boolean baokich = p.haveBaoKich();
            if (baokich) {
                damage *= 4;
                effect = 4;
            } else if (critSv) {
                damage *= 2;
                effect = 2;
                if (p.petUsing != null && c != null && c.noi_luc >= 5) {
                    long pcLienKich = p.petUsing.getLienKich();
                    damage = (int)(damage + damage * pcLienKich / 100L);
                }
            }

            damage += hphut;
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
            int damNguyetAnh = p.getPCDamNguyetAnh(skill);
            Vector<LiveActor> muctieu = new Vector<>();

            while (i < nMonster) {
                if (i > 0) {
                    mt = this.getMonster(dis.readShort(), p.inCountry);
                }

                if (mt != null) {
                    if (i > 0) {
                        if (mt.isMyMonster(p) || !Map.canAttackMonsterVantieu(mt, p) || !Map.inRangeActor(firstMonster, mt, MAX_RANGE_CHAR[p.charClass])) {
                            i++;
                            continue;
                        }

                        if (mt.isDead) {
                            Map.onMosterDie(p, mt, skill, damage, effect, (byte)0);
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
                    if (p.charthanthu != null && mt.hp > 0 && c != null && c.noi_luc >= 10) {
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

                    if (mt.isLienHoaTru()) {
                        Database.instance.addCharChienTruongMoba(p.charname, "sttru", damage);
                    }

                    if (mt.hp > 0) {
                        if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry && p.killer < 200) {
                            p.killer = (short)(p.killer + 200);
                            Message msg = new Message(67);
                            msg.dos.writeShort(p.id);
                            msg.dos.writeByte(1);
                            msg.dos.writeShort(p.killer);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                        }

                        if (mt.target == null) {
                            mt.target = p;
                        }
                    } else {
                        if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry) {
                            p.killer = (short)(p.killer + 200);
                            if (p.killer > 32000) {
                                p.killer = 32000;
                            }

                            Message msg = new Message(67);
                            msg.dos.writeShort(p.id);
                            msg.dos.writeByte(1);
                            msg.dos.writeShort(p.killer);
                            p.sendMessage(msg);
                            p.sendToNearPlayer(msg);
                        }

                        Vector<Actor> droplist = new Vector<>();
                        if (!mt.isMaterialMons()) {
                            droplist = mt.onDropItem(this.map, p);
                        }

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

                        p.doAddPointChienTruong(mt.getPointChienTruong());
                    }

                    if (mt.hp <= 0) {
                        if (!mt.isBoss || !mt.isCopy()) {
                            if (mt.isBoss) {
                                mt.bornTime = System.currentTimeMillis() + 86400000L;
                                mt.setTimeReBornInEvent(mt.bornTime);
                                Database.instance.saveEvent(Map.event.getInfo());
                                Map.removeBossLocation(1);
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
            } catch (Exception var43) {
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
                } catch (Exception var42) {
                }
            }

            int dxpx = Map.rand10(allXP);
            if (dxpx == 0) {
                dxpx = 1;
            }

            if (dxpx > 0) {
                int newxp = Map.calculatorXpParty(p, dxpx);
                if (newxp == dxpx) {
                    int var59 = dxpx * Map.doubleALL;
                    var59 = p.expReceive(var59);
                    Map.addXPForChar(p, var59 + p.getEffSkillClan(0) * var59 / 100, false, "Region3");
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
                                temp *= Map.doubleALL;
                                temp = pp.expReceive(temp);
                                Map.addXPForChar(pp, temp + pp.getEffSkillClan(0) * temp / 100, false, "Region1");
                            }
                        }
                    }

                    xpReceive = newxp * 20 / 100 * Map.doubleALL;
                    xpReceive = p.expReceive(xpReceive);
                    Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "Region2");
                }
            }

            p.charHireAttackMultiMOnster(mst, _type);
        } catch (Exception var44) {
            var44.printStackTrace();
        }
    }

    public static void setTimeCake() {
        int[] tick = new int[]{2};
        timeDropCake = System.currentTimeMillis() + tick[Map.r.nextInt(tick.length)] * 60000;
    }

    protected void doAttackMonster(Char p, Message message) throws IOException {
        if (!p.cannotAttackWhenBienhinh()) {
            if (!p.countHit() && !p.freeze()) {
                if (p.hp <= 0) {
                    p.actorDie();
                } else if (!p.checkDurableWeapone()) {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Không thể đánh khi vũ khí bị hỏng. Hãy đến Thợ rèn để sửa lại.", ""));
                    Message m = new Message(104);

                    try {
                        m.dos.writeByte(p.typeConfig);
                        m.dos.writeByte(0);
                        p.sendMessage(m);
                    } catch (Exception var34) {
                    }
                } else if (!p.checkLamthinh() && !p.checkCamLang() && !p.checkRuNgu() && !p.checkChoang()) {
                    p.downDurableWeapone();
                    DataInputStream dis = message.dis;
                    Monster mt = this.getMonster(dis.readShort(), p.inCountry);
                    short skill = dis.readByte();
                    byte effect = 0;
                    int ahp = p.attackDamage;
                    int crit = 0;
                    int buffAttack = -1;
                    if (buffAttack <= 0) {
                        if (buffAttack == -1 || buffAttack != 0 || p.skill[5] + p.addMoreLevelSkill[5] != 0) {
                            if (mt != null && !mt.isDead) {
                                if (mt.getCharID() != p.charDBID) {
                                    if (mt.isThungGoNuiChauBau()) {
                                        if (Map.inRangeActor(p, mt, 48) && p.monsTerThuThap == null) {
                                            p.monsTerThuThap = mt;
                                            p.monsTerThuThap.setTimeThuThap(10);
                                            p.sendMessage(MessageCreator.createMsgTimeCountdown("Mở", 10, mt.cat, mt.id, Map.COUNT_DOWN, -1));
                                        }
                                    } else if (Map.inRangeActor(p, mt, MAX_RANGE_CHAR[p.charClass])) {
                                        if (!mt.isMyMonster(p)) {
                                            if (mt.map.mapId == p.mapID) {
                                                if (Map.canAttackMonsterVantieu(mt, p)) {
                                                    int _type = (byte)skill;
                                                    int _level = p.skill[_type] + p.addMoreLevelSkill[_type];
                                                    if (_level <= 0) {
                                                        _level = p.addMoreLevelSkill[_type];
                                                    }

                                                    if (_level != 0 && Map.inRangeSkill(p, mt, MAX_RANGE_CHAR[p.charClass])) {
                                                        long now = System.currentTimeMillis();
                                                        if (now - p.timeLastUseSkills[_type] >= CharManager.SKILL_COOLDOWN[p.charClass][_type][_level] * 100) {
                                                            int hphut = 0;
                                                            if (p.haveHutHp() > 0 && p.hp < p.maxhp && !mt.isNgocRong() && !mt.isRuongMaquai()) {
                                                                int hp = p.haveHutHp();
                                                                hphut = hp;
                                                                if (mt.hp < hp) {
                                                                    hphut = mt.hp;
                                                                }

                                                                p.hp += hphut;
                                                                p.calculatorHPMP();
                                                                MessageCreator.createMsgUseHpMP(p, hp, 1);
                                                            }

                                                            p.timeLastUseSkills[_type] = now;
                                                            buffAttack = p.getBuffEffAttack();
                                                            if (mt.resistThroughArmor()) {
                                                                buffAttack = -1;
                                                            }

                                                            int damage = p.attackDam(mt, _type, _level, buffAttack);
                                                            if (mt.isNgocRong()) {
                                                                if (damage == 1) {
                                                                    if (p.potions[73] == 0) {
                                                                        p.sendMessage(MessageCreator.createMsgChat(p.id, "Không có túi nhặt ngọc"));
                                                                    } else {
                                                                        mt.onDropItem(this.map, p);
                                                                    }
                                                                } else if (mt.isDead) {
                                                                    mt.sendActorDie(p);
                                                                }
                                                            } else {
                                                                CharChienTruong c = MapChienTruongMoba.getCharChienTruong(p.charname);
                                                                if (mt.haveDodge()) {
                                                                    damage = 0;
                                                                    buffAttack = -1;
                                                                }

                                                                if (mt.isMonsterVantieu() && damage >= mt.maxhp / 100) {
                                                                    damage = mt.maxhp / 100 + Map.r.nextInt() % 100;
                                                                }

                                                                damage *= CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                                                                boolean critSv = p.havecrit();
                                                                boolean baokich = p.haveBaoKich();
                                                                if (baokich) {
                                                                    damage *= 4;
                                                                    effect = 4;
                                                                } else if (critSv) {
                                                                    damage *= 2;
                                                                    effect = 2;
                                                                    if (p.petUsing != null && c != null && c.noi_luc >= 5) {
                                                                        long pcLienKich = p.petUsing.getLienKich();
                                                                        damage = (int)(damage + damage * pcLienKich / 100L);
                                                                    }
                                                                }

                                                                if (_level <= p.skill[_type] + p.addMoreLevelSkill[_type]) {
                                                                    int mplost = CharManager.SKILL_MP[p.charClass][_type][_level];
                                                                    if (p.mp + p.percentBuff[1] >= mplost) {
                                                                        p.mp -= mplost;
                                                                        if (p.mp <= 0) {
                                                                            p.mp = 0;
                                                                        }

                                                                        int getXp = mt.getXpReceive(damage);
                                                                        ahp = damage / CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1];
                                                                        mt.hp -= damage + hphut;
                                                                        if (damage > 0 && mt.haveBackDam()) {
                                                                            int backdam = mt.getBackDam(damage);
                                                                            Message mbd = MessageCreator.createMsgBuffEffect(5, mt.cat, p, backdam, 0, -1);
                                                                            p.sendMessage(mbd);
                                                                            p.sendToNearPlayer(mbd);
                                                                        }

                                                                        if (mt.isLienHoaTru()) {
                                                                            Database.instance.addCharChienTruongMoba(p.charname, "sttru", damage + hphut);
                                                                        }

                                                                        Message m = null;
                                                                        if (p.charthanthu != null && mt.hp > 0 && c != null && c.noi_luc >= 10) {
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
                                                                            if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry && p.killer < 200) {
                                                                                p.killer = (short)(p.killer + 200);
                                                                                Message msg = new Message(67);
                                                                                msg.dos.writeShort(p.id);
                                                                                msg.dos.writeByte(1);
                                                                                msg.dos.writeShort(p.killer);
                                                                                p.sendMessage(msg);
                                                                                p.sendToNearPlayer(msg);
                                                                            }

                                                                            if (mt.target == null) {
                                                                                mt.target = p;
                                                                            }

                                                                            if (ahp > 0) {
                                                                                p.buffAttackSkill(damage, mt);
                                                                            }

                                                                            if (getXp > 0) {
                                                                                int x2Player = p.getX2();
                                                                                if (TeamServer.isDouble) {
                                                                                    int var51 = 0;
                                                                                }

                                                                                int dxp = Map.rand10(getXp);
                                                                                if (dxp == 0) {
                                                                                    dxp = 1;
                                                                                }

                                                                                if (dxp > 0) {
                                                                                    int newxp = Map.calculatorXpParty(p, dxp);
                                                                                    if (newxp == dxp) {
                                                                                        int e = dxp * Map.doubleALL;
                                                                                        e = p.expReceive(e);
                                                                                        Map.addXPForChar(p, e + p.getEffSkillClan(0) * e / 100, false, "Region6");
                                                                                    } else {
                                                                                        int nUser = p.party.userParty.size();
                                                                                        if (nUser > 1) {
                                                                                            nUser = 5;
                                                                                        }

                                                                                        int xpReceive = newxp * 80 / (100 * nUser);
                                                                                        int maxLv = p.lvDetail.lv;

                                                                                        for (int i = 0; i < p.party.userParty.size(); i++) {
                                                                                            Char pp = p.party.userParty.get(i);
                                                                                            if (pp.id != p.id
                                                                                                && p.near(pp, 320)
                                                                                                && pp.mapID == p.mapID
                                                                                                && pp.inCountry == p.inCountry
                                                                                                && pp.region == p.region) {
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
                                                                                                    temp *= Map.doubleALL;
                                                                                                    temp = pp.expReceive(temp);
                                                                                                    Map.addXPForChar(pp, temp + pp.getEffSkillClan(0) * temp / 100, false, "Region4");
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        xpReceive = newxp * 20 / 100 * Map.doubleALL;
                                                                                        xpReceive = p.expReceive(xpReceive);
                                                                                        Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "Region5");
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
                                                                            if (mt.isMonsterVantieu() && p.myCountry == mt.inCountry) {
                                                                                p.killer = (short)(p.killer + 200);
                                                                                Message msg = new Message(67);
                                                                                msg.dos.writeShort(p.id);
                                                                                msg.dos.writeByte(1);
                                                                                msg.dos.writeShort(p.killer);
                                                                                p.sendMessage(msg);
                                                                                p.sendToNearPlayer(msg);
                                                                            }

                                                                            Vector<Actor> droplist = new Vector<>();
                                                                            int deltlv = Map.abs(p.lvDetail.lv - mt.level);
                                                                            mt.hp = 0;

                                                                            try {
                                                                                droplist = mt.onDropItem(this.map, p);
                                                                            } catch (Exception var35) {
                                                                                var35.printStackTrace();
                                                                            }

                                                                            try {
                                                                                int dxpx = Map.rand10(mt.xp);
                                                                                if (dxpx == 0) {
                                                                                    dxpx = 1;
                                                                                }

                                                                                if (dxpx > 0) {
                                                                                    int newxp = Map.calculatorXpParty(p, dxpx);
                                                                                    if (newxp == dxpx) {
                                                                                        int var60 = dxpx * Map.doubleALL;
                                                                                        var60 = p.expReceive(var60);
                                                                                        Map.addXPForChar(p, var60 + p.getEffSkillClan(0) * var60 / 100, false, "Region9");
                                                                                    } else {
                                                                                        int nUser = p.party.userParty.size();
                                                                                        if (nUser > 1) {
                                                                                            nUser = 5;
                                                                                        }

                                                                                        int xpReceive = newxp * 80 / (nUser * 100);
                                                                                        int maxLv = p.lvDetail.lv;

                                                                                        for (int ix = 0; ix < p.party.userParty.size(); ix++) {
                                                                                            Char pp = p.party.userParty.get(ix);
                                                                                            if (pp.id != p.id
                                                                                                && p.near(pp, 320)
                                                                                                && pp.mapID == p.mapID
                                                                                                && pp.inCountry == p.inCountry
                                                                                                && pp.region == p.region) {
                                                                                                int dlvx = Map.abs(maxLv - pp.lvDetail.lv);
                                                                                                int tempx = 1;
                                                                                                if (dlvx <= 5) {
                                                                                                    tempx = xpReceive;
                                                                                                } else if (dlvx <= 10) {
                                                                                                    tempx = xpReceive / 5;
                                                                                                } else if (dlvx <= 20) {
                                                                                                    tempx = xpReceive / 10;
                                                                                                } else if (dlvx <= 30) {
                                                                                                    tempx = xpReceive / 15;
                                                                                                } else {
                                                                                                    tempx = xpReceive / 20;
                                                                                                }

                                                                                                if (tempx == 0) {
                                                                                                    tempx = 1;
                                                                                                }

                                                                                                if (pp.hp > 0) {
                                                                                                    tempx *= Map.doubleALL;
                                                                                                    tempx = pp.expReceive(tempx);
                                                                                                    Map.addXPForChar(pp, tempx + pp.getEffSkillClan(0) * tempx / 100, false, "Region7");
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        xpReceive = newxp * 20 / 100 * Map.doubleALL;
                                                                                        xpReceive = p.expReceive(xpReceive);
                                                                                        Map.addXPForChar(p, xpReceive + p.getEffSkillClan(0) * xpReceive / 100, false, "Region8");
                                                                                    }
                                                                                }
                                                                            } catch (Exception var37) {
                                                                                var37.printStackTrace();
                                                                            }

                                                                            try {
                                                                                m = new Message(17);
                                                                                m.dos.writeShort(p.id);
                                                                                m.dos.writeShort(mt.id);
                                                                                m.dos.writeByte(skill);
                                                                                m.dos.writeInt(ahp);
                                                                                m.dos.writeByte(effect);
                                                                                m.dos.writeByte(droplist.size());
                                                                                if (droplist.size() > 0) {
                                                                                    for (Actor e : droplist) {
                                                                                        Map.writeActorPos(m, e, (byte)0);
                                                                                    }
                                                                                }

                                                                                int xx2 = CharManager.UP_DAMGE_SKILL[p.charClass][_type][_level - 1 >= 0 ? _level - 1 : 0];
                                                                                m.dos.writeByte(xx2);
                                                                                m.dos.writeByte(buffAttack);
                                                                                m.dos.writeByte(_level);
                                                                                p.sendMessage(m);
                                                                                p.sendToNearPlayer(m);
                                                                                if (p.receiveQuest && QuestTemplate.QUEST_TYPE[p.questID - 1] == 0) {
                                                                                    p.checkFinsishQuest(mt.getType(), -1, -1);
                                                                                }
                                                                            } catch (Exception var36) {
                                                                                var36.printStackTrace();
                                                                            }

                                                                            p.doAddPointChienTruong(mt.getPointChienTruong());
                                                                        }

                                                                        if (mt.hp <= 0) {
                                                                            if (!mt.isCopy() || !mt.isBoss) {
                                                                                if (mt.isBoss) {
                                                                                    mt.bornTime = System.currentTimeMillis() + 86400000L;
                                                                                    mt.setTimeReBornInEvent(mt.bornTime);
                                                                                    Database.instance.saveEvent(Map.event.getInfo());
                                                                                    Map.removeBossLocation(1);
                                                                                } else {
                                                                                    mt.setTimeReBorn();
                                                                                }
                                                                            }

                                                                            mt.isDead = true;
                                                                            mt.target = null;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (mt != null) {
                                    Map.onMosterDie(p, mt, (byte)skill, 1, effect, (byte)0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void killAllMonster(Vector<Hashtable<Short, Monster>> monsters, int country) {
        synchronized (monsters) {
            short i = (short)country;
            Vector<Char> players = this.allPlayers;
            if (players.size() > 0) {
                for (int j = 0; j < players.size(); j++) {
                    try {
                        if (players.get(j).isBot == -1) {
                            this.playerExit(players.get(j));
                            this.map.move2Map(players.get(j), players.get(j).x / 16, players.get(j).y / 16, players.get(j).mapID, players.get(j).inCountry);
                        }
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }
                }
            }
        }
    }

    public void reSetAllMonster(int country) {
    }

    private short getIDITEM() {
        return this.map.getIDITEM();
    }

    public void removeDynamicMonster(Monster m, int country) {
        this.tempRemoveMonster.get(0).add(m);
    }

    public void addMonsterDynamic(Monster m, int country) {
        this.tempMonster.get(0).add(m);
    }

    public void addMonster(short id, Monster mons, int country) {
    }

    public boolean canCreateCharCopyBoss(int team) {
        return false;
    }

    public void addCharChienTruongOffline(CharChienTruong c) {
    }

    public int getPointAddWin(int team) {
        int delta = 0;
        if (team == 0) {
            delta = this.pointC1 - this.pointC2;
            if (delta > 0) {
                if (delta > 15) {
                    delta = 15;
                }

                return 30 - delta;
            }

            if (delta < 0) {
                if (Map.abs(delta) > 15) {
                    delta = 15;
                }

                return 30 + Map.abs(delta);
            }
        } else {
            delta = this.pointC2 - this.pointC1;
            if (delta > 0) {
                if (delta > 15) {
                    delta = 15;
                }

                return 30 - delta;
            }

            if (delta < 0) {
                if (Map.abs(delta) > 15) {
                    delta = 15;
                }

                return 30 + Map.abs(delta);
            }
        }

        return 30;
    }

    public void playerJoin(Char player) {
        if (player.name_char_loi_dai != null) {
            this.allPlayers.add(player);
            if (this.allPlayers.size() == 2) {
                this.name1 = this.allPlayers.get(0).charname;
                this.name2 = this.allPlayers.get(1).charname;
                CharThiDau cthidau1 = Map.ALL_CHAR_LOI_DAI.get(this.allPlayers.get(0).nhomThidau).get(this.allPlayers.get(0).charDBID);
                this.pointC1 = cthidau1.point;
                this.point_begin1 = "" + this.pointC1;
                CharThiDau cthidau2 = Map.ALL_CHAR_LOI_DAI.get(this.allPlayers.get(1).nhomThidau).get(this.allPlayers.get(1).charDBID);
                this.pointC2 = cthidau2.point;
                this.point_begin2 = "" + this.pointC2;
            }
        } else {
            this.allPlayersView.add(player);
        }

        player.nNpc = 0;
        player.map = this.map;
        player.region = this.idRegion;
        this.sendInfoPlayer(player);
    }

    public void playerExit(Char p) {
    }

    public void resetTimeNuiChauBau(Char p) {
        if (this.isChauBau || this.comback) {
            CharInfo c = MapChauBau.all_char_nui_kho_bau.get(p.charname);
            if (c != null) {
                c.timeNuiChaubau = 0L;
                c.fail = 0;
            }
        }
    }

    public void removePlayer(int country, Char p) {
        if (p.name_char_loi_dai != null) {
            if (!this.isFinish) {
                this.isFinish(p, true);
            }

            p.name_char_loi_dai = null;
            this.allPlayers.remove(p);
        } else {
            this.allPlayersView.remove(p);
        }
    }

    public void doTimeOut() {
        if (!this.isFinish && !this.checking) {
            this.checking = true;
            synchronized (this.allPlayers) {
                this.isFinish = true;
                String info = "";
                String nameWin = "";
                Char pWin = null;
                int lvLose = 0;
                String[] point = new String[]{"", ""};
                boolean isDraw = false;
                int pointwin = 0;
                if (this.countWin[0] < 2 && this.countWin[1] < 2) {
                    if (this.countWin[0] == this.countWin[1]) {
                        isDraw = true;
                    } else if (this.allPlayers.size() > 1) {
                        if (this.countWin[0] > this.countWin[1]) {
                            nameWin = this.allPlayers.get(0).charname;
                            pointwin = this.getPointAddWin(0);
                        } else if (this.countWin[0] < this.countWin[1]) {
                            nameWin = this.allPlayers.get(1).charname;
                            pointwin = this.getPointAddWin(1);
                        }
                    }
                }

                for (int i = 0; i < this.allPlayers.size(); i++) {
                    this.allPlayers.get(i).name_char_loi_dai = null;
                    Char p = this.allPlayers.get(i);
                    info = info + p.charname;
                    CharThiDau cthidau = Map.ALL_CHAR_LOI_DAI.get(p.nhomThidau).get(p.charDBID);
                    if (isDraw) {
                        try {
                            p.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Trận đấu của bạn đã hoà"));
                        } catch (Exception var17) {
                        }

                        info = info + " " + cthidau.point + " hoà ";
                    } else if (nameWin != null) {
                        if (p.charname.equals(nameWin)) {
                            cthidau.point += pointwin;
                            Database.instance.doAddCharThachDau(cthidau, cthidau.nhom, true);

                            try {
                                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(p.charname + " chiến thắng"));
                            } catch (Exception var16) {
                            }

                            info = info + " " + cthidau.point + " thắng ";
                        } else {
                            cthidau.point -= pointwin;
                            if (cthidau.point < 0) {
                                cthidau.point = 0;
                            }

                            Database.instance.doAddCharThachDau(cthidau, cthidau.nhom, true);

                            try {
                                p.sendMessage(MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã thua"));
                            } catch (Exception var15) {
                            }
                        }
                    }

                    try {
                        this.allPlayers.get(i).hp = this.allPlayers.get(i).maxhp;
                        this.allPlayers.get(i).mp = this.allPlayers.get(i).maxmp;
                        this.allPlayers.get(i).sendMessage(MessageCreator.createMainCharInfoMessage(this.allPlayers.get(i)));
                        this.allPlayers.get(i).sendToNearPlayer(MessageCreator.createNew_HMP_Message(this.allPlayers.get(i), 0));
                    } catch (IOException var14) {
                    }
                }

                try {
                    this.map.sendAllPlayer(MessageCreator.createServerAlertAutoOffMessage(info), 0, this.idRegion);
                } catch (IOException var13) {
                }

                Database.instance
                    .saveOrtherLog(
                        "",
                        this.name1,
                        this.name1 + "_ " + this.point_begin1 + " vs " + this.name2 + "_ " + this.point_begin2 + ". " + info + " _ " + point[0] + " " + point[1],
                        "ketqualoidai"
                    );
                Database.instance
                    .saveOrtherLog(
                        "",
                        this.name2,
                        this.name1 + "_ " + this.point_begin1 + " vs " + this.name2 + "_ " + this.point_begin2 + ". " + info + " _ " + point[0] + " " + point[1],
                        "ketqualoidai"
                    );
            }
        }
    }

    public void isFinish(LiveActor die, boolean isExit) {
        if (!this.isFinish && !this.checking) {
            this.checking = true;
            this.van++;
            synchronized (this.allPlayers) {
                for (int i = 0; i < this.allPlayers.size(); i++) {
                    Char p = this.allPlayers.get(i);
                    if (p.id != die.id) {
                        if (!isExit) {
                            this.countWin[i]++;
                        } else {
                            this.countWin[i] = 2;
                        }
                        break;
                    }
                }

                if (this.countWin[0] < 2 && this.countWin[1] < 2) {
                    for (int ix = 0; ix < this.allPlayers.size(); ix++) {
                        this.allPlayers
                            .get(ix)
                            .sendMessage(MessageCreator.createMsgTimeCountdown("Ván " + this.van + " bắt đầu sau", 10, -1, Char.ID_DEM_NGUOC, 1, -1));
                    }

                    try {
                        this.allPlayers
                            .get(0)
                            .sendMessage(
                                MessageCreator.createMsgTimeCountdown(
                                    "Tỷ số: " + this.countWin[0] + "-" + this.countWin[1], 180, -1, Char.ID_TY_SO, Map.NOT_COUNT_DOWN, -1
                                )
                            );
                        this.allPlayers
                            .get(1)
                            .sendMessage(
                                MessageCreator.createMsgTimeCountdown(
                                    "Tỷ số: " + this.countWin[0] + "-" + this.countWin[1], 180, -1, Char.ID_TY_SO, Map.NOT_COUNT_DOWN, -1
                                )
                            );
                    } catch (Exception var13) {
                    }

                    this.checking = false;
                    this.timeWaitStart = System.currentTimeMillis() + 10000L;
                    return;
                }

                try {
                    this.allPlayers
                        .get(0)
                        .sendMessage(
                            MessageCreator.createMsgTimeCountdown(
                                "Tỷ số: " + this.countWin[0] + "-" + this.countWin[1], 180, -1, Char.ID_TY_SO, Map.NOT_COUNT_DOWN, -1
                            )
                        );
                    this.allPlayers
                        .get(1)
                        .sendMessage(
                            MessageCreator.createMsgTimeCountdown(
                                "Tỷ số: " + this.countWin[0] + "-" + this.countWin[1], 180, -1, Char.ID_TY_SO, Map.NOT_COUNT_DOWN, -1
                            )
                        );
                } catch (Exception var19) {
                }

                this.setTimeChuyenMap(15, 0);
                this.isFinish = true;
                String info = "";
                Char pWin = null;
                int lvLose = 0;
                String[] point = new String[]{"", ""};
                int pointwin = this.getPointAddWin(this.countWin[0] >= 2 ? 0 : 1);

                for (int ix = 0; ix < this.allPlayers.size(); ix++) {
                    this.allPlayers.get(ix).name_char_loi_dai = null;
                    Char p = this.allPlayers.get(ix);
                    CharThiDau cthidau = Map.ALL_CHAR_LOI_DAI.get(p.nhomThidau).get(p.charDBID);
                    if (this.countWin[ix] >= 2) {
                        info = info + this.allPlayers.get(ix).charname + " chiến thắng";
                        pWin = this.allPlayers.get(ix);

                        try {
                            cthidau.point += pointwin;
                            Database.instance.doAddCharThachDau(cthidau, cthidau.nhom, true);
                        } catch (Exception var18) {
                        }
                    } else {
                        lvLose = this.allPlayers.get(ix).lvDetail.lv;

                        try {
                            cthidau.point -= pointwin;
                            if (cthidau.point < 0) {
                                cthidau.point = 0;
                            }

                            Database.instance.doAddCharThachDau(cthidau, cthidau.nhom, true);
                        } catch (Exception var17) {
                        }
                    }

                    if (p != null && cthidau != null) {
                        try {
                            System.out.println("KET THUC : " + p + " >>> " + cthidau);
                            point[ix] = p.charname + " " + cthidau.point;
                        } catch (Exception var16) {
                        }
                    } else {
                        System.out.println("ISHNIHS CO CAI NULL " + p + " > " + cthidau);
                    }

                    try {
                        this.allPlayers.get(ix).hp = this.allPlayers.get(ix).maxhp;
                        this.allPlayers.get(ix).mp = this.allPlayers.get(ix).maxmp;
                        this.allPlayers.get(ix).sendMessage(MessageCreator.createMainCharInfoMessage(this.allPlayers.get(ix)));
                        this.allPlayers.get(ix).sendToNearPlayer(MessageCreator.createNew_HMP_Message(this.allPlayers.get(ix), 0));
                    } catch (IOException var15) {
                    }
                }

                try {
                    this.map.sendAllPlayer(MessageCreator.createServerAlertAutoOffMessage(info), 0, this.idRegion);
                } catch (IOException var14) {
                }

                Database.instance
                    .saveOrtherLog(
                        "",
                        this.name1,
                        this.name1 + "_ " + this.point_begin1 + " vs " + this.name2 + "_ " + this.point_begin2 + ". " + info + " _ " + point[0] + " " + point[1],
                        "ketqualoidai"
                    );
                Database.instance
                    .saveOrtherLog(
                        "",
                        this.name2,
                        this.name1 + "_ " + this.point_begin1 + " vs " + this.name2 + "_ " + this.point_begin2 + ". " + info + " _ " + point[0] + " " + point[1],
                        "ketqualoidai"
                    );
            }
        }
    }

    public void addPlayerMessage(Char p, Message message) {
        this.timeExist = System.currentTimeMillis();
        this.allPlayerMessages.get(0).add(new PlayerMessage(p, message));
        this.notifyMap();
    }

    public void addEffectAutoInMap(int country, Char p, int region, int x, int y, int idEff, long time) {
        EffectBuff eff = new EffectBuff(idEff, time);
        eff.idCharOwner = p.id;
        eff.x = (short)p.x;
        eff.y = (short)p.y;
        p.ALL_BUFF_INMAP.add(eff);
        this.EFFECT_AUTO.get(0).add(eff);
    }

    public Vector<EffectBuff> getAllEffectAuto(int inCountry) {
        return this.EFFECT_AUTO.get(0);
    }

    public void addPlayer(int country, Char p) {
        this.allPlayers.add(p);
    }

    public void onOffSkelonton(boolean onoff) {
        this.isSkelonton = onoff;
    }

    public Vector<Char> getAllPlayer(int inCountry) {
        Vector<Char> pl = new Vector<>();

        for (int i = 0; i < this.allPlayers.size(); i++) {
            pl.add(this.allPlayers.get(i));
        }

        for (int i = 0; i < this.allPlayersView.size(); i++) {
            pl.add(this.allPlayersView.get(i));
        }

        return pl;
    }

    public Hashtable<Short, Monster> getAllMons(int country) {
        return new Hashtable<>();
    }

    public Monster getMonster(short id, int country) {
        return null;
    }

    public boolean isFull() {
        return false;
    }

    public void stop() {
        this.running = false;
    }

    public void notifyMap() {
        try {
            synchronized (this.LOCK) {
                this.LOCK.notify();
            }
        } catch (Exception var7) {
        }

        try {
            synchronized (this.LOCK1) {
                this.LOCK1.notify();
            }
        } catch (Exception var5) {
        }

        try {
            synchronized (this.LOCK2) {
                this.LOCK2.notify();
            }
        } catch (Exception var3) {
        }
    }

    public boolean timeOut() {
        return this.idRegion > 10 && System.currentTimeMillis() - this.timeExist >= 54000000L;
    }

    public byte getStateRegion() {
        return 1;
    }

    public void removeMonster(int country, int idMons) {
    }

    private Char getChar(short id) {
        return CharManager.instance.getByCharID(id);
    }

    public Vector<LiveActor> getAllNearActor(Char from, int cat, int sl, LiveActor attacker) {
        Vector<LiveActor> ac = new Vector<>();
        Vector<LiveActor> c = new Vector<>();
        if (cat == 0) {
            for (int i = 0; i < from.nearChars.size(); i++) {
                Char pp = this.map.getPlayerByID(from.nearChars.get(i));
                if (pp != null && pp.isBot == -1) {
                    if ((from.mapID != 201 || pp.idClan == from.idClan) && pp.killer <= 0) {
                        if (pp.myCountry != from.myCountry) {
                            c.add(pp);
                        }
                    } else {
                        c.add(pp);
                    }
                }
            }
        }

        if (c.size() < sl) {
            if (attacker.hp > 0) {
                c.add(attacker);
            }

            return c;
        } else {
            while (ac.size() < sl) {
                ac.add(c.remove(Map.r.nextInt(c.size())));
            }

            if (attacker.hp > 0) {
                ac.add(attacker);
            }

            return ac;
        }
    }

    public void setTimeChuyenMap(int time, int teamwin) {
        if (this.timeChuyenMap == 0L) {
            this.teamWin = teamwin;
            this.timeChuyenMap = System.currentTimeMillis() + time * 1000;

            try {
                for (int i = 0; i < this.allPlayers.size(); i++) {
                    Char player = this.allPlayers.get(i);
                    player.sendMessage(MessageCreator.createMsgTimeCountdown("Chuyển map sau", time, -1, Char.ID_CHUYEN_MAP_CHIEN_TRUONG_MOBA, Map.COUNT_DOWN, -1));
                }
            } catch (Exception var5) {
            }
        }
    }

    public boolean isEnd() {
        return this.isStop;
    }
}
