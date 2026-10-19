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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;
import util.Logger;

public class RegionNuiChauBau {
    public Vector<Hashtable<Short, Monster>> monsters = new Vector<>();
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
    private Vector<Vector<Char>> allPlayers = new Vector<>();
    private Vector<Vector<EffectBuff>> EFFECT_AUTO = new Vector<>();
    public Vector<Vector<Monster>> tempMonster = new Vector<>();
    public Vector<Vector<Monster>> tempRemoveMonster = new Vector<>();
    private Vector<Hashtable<Short, Monster>> temp_monsters = new Vector<>();
    boolean isStop = false;
    public long tend = System.currentTimeMillis() + 1200000L;
    boolean isSkelonton = false;
    long timeCheck = System.currentTimeMillis() + 5000L;
    int teamWin = 0;
    static long timeDropCake = System.currentTimeMillis() + 7200000L;
    public static short[] MAX_RANGE_CHAR = new short[]{120, 120, 140, 120, 160};
    public Char charBoss1;
    public Char charBoss2;
    public boolean isHaveChienthan = false;
    short countT1 = 0;
    short countT2 = 0;
    public long timeChuyenMap = 0L;

    public RegionNuiChauBau() {
    }

    public RegionNuiChauBau(Map m, int id) {
        this.map = m;
        this.timeExist = System.currentTimeMillis();
        this.idRegion = (short)id;
        (new Thread() {
            private long lastTimeUpdateMap;

            @Override
            public void run() {
                while (RegionNuiChauBau.this.running && !AdminHandler.isStopServer && !RegionNuiChauBau.this.isStop) {
                    try {
                        while (RealController.savingChar) {
                            Thread.sleep(100L);
                        }

                        long l1 = System.currentTimeMillis();
                        if (l1 - this.lastTimeUpdateMap >= Map.DELAY_UPDATE_MAP) {
                            RegionNuiChauBau.this.update();
                            this.lastTimeUpdateMap = System.currentTimeMillis();
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
                    }

                    try {
                        synchronized (RegionNuiChauBau.this.LOCK) {
                            RegionNuiChauBau.this.LOCK.wait(500L);
                        }
                    } catch (Exception var4) {
                        var4.printStackTrace();
                    }
                }
            }
        }).start();
        this.allPlayers.add(new Vector<>());
        this.allPlayers.add(new Vector<>());
        this.allPlayerMessages.add(new Vector<>());
        this.allPlayerMessages.add(new Vector<>());
        this.monsters.add(new Hashtable<>());
        this.monsters.add(new Hashtable<>());
        this.temp_monsters.add(new Hashtable<>());
        this.temp_monsters.add(new Hashtable<>());
        this.tempMonster.add(new Vector<>());
        this.tempMonster.add(new Vector<>());
        this.tempRemoveMonster.add(new Vector<>());
        this.tempRemoveMonster.add(new Vector<>());
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
                while (!RegionNuiChauBau.this.isStop) {
                    try {
                        if (AdminHandler.isStopServer) {
                            RegionNuiChauBau.this.map_run_state = 0;
                            break;
                        }

                        while (RealController.savingChar) {
                            Thread.sleep(100L);
                        }

                        RegionNuiChauBau.this.map_run_state = 0;
                        long l1 = System.currentTimeMillis();
                        if (l1 - RegionNuiChauBau.this.lastTimeUpdateMap >= Map.DELAY_UPDATE_MAP) {
                            RegionNuiChauBau.this.updateSand();
                            RegionNuiChauBau.this.lastTimeUpdateMap = System.currentTimeMillis();
                        }

                        RegionNuiChauBau.this.map_run_state = 1;
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
                    }

                    try {
                        synchronized (RegionNuiChauBau.this.LOCK) {
                            RegionNuiChauBau.this.LOCK.wait(Map.DELAY_UPDATE_MAP);
                        }
                    } catch (Exception var4) {
                    }
                }
            }
        }).start();
        (new Thread() {
            @Override
            public void run() {
                while (!RegionNuiChauBau.this.isStop) {
                    try {
                        int count = 0;
                        new Vector();
                        Vector<PlayerMessage> playerMessages = RegionNuiChauBau.this.allPlayerMessages.get(0);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                RegionNuiChauBau.this.map.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }

                        synchronized (RegionNuiChauBau.this.LOCK) {
                            RegionNuiChauBau.this.LOCK.wait(Map.timeDelay);
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void update() {
        Vector<Char> players = this.allPlayers.get(1);
        if (players.size() > 0) {
            if (Map.bossCopy.size() > 0) {
                for (byte i = 0; i < Map.bossCopy.size(); i++) {
                    try {
                        Boss bo = Map.bossCopy.get(i);
                        if (bo.hp <= 0 || bo.isDead) {
                            bo.map.removeMonster(bo.id, bo.inCountry, bo.region);
                            Map.bossCopy.remove(i);

                            try {
                                Message m = new Message(90);
                                m.dos.writeShort(bo.id);
                                m.dos.writeByte(bo.cat);
                                bo.map.sendAllPlayer(m, bo.inCountry, this.idRegion);
                            } catch (Exception var8) {
                                var8.printStackTrace();
                            }
                        }
                    } catch (Exception var12) {
                    }
                }
            }

            while (this.tempMonster.get(1).size() > 0) {
                Monster mt = this.tempMonster.get(1).remove(0);
                if (mt.map != null && mt.map.mapId == this.map.mapId && mt.hp > 0) {
                    mt.map.addMonster(mt, 1, mt.region);
                }
            }

            while (this.tempRemoveMonster.get(1).size() > 0) {
                Monster mt = this.tempRemoveMonster.get(1).remove(0);
                if (mt.map != null && mt.map.mapId == this.map.mapId && mt.hp > 0) {
                    mt.map.removeMonster(mt.id, 1, mt.region);
                }
            }

            Collection<Monster> listmonster = this.monsters.get(1).values();
            if (this.isSkelonton) {
                listmonster = this.temp_monsters.get(1).values();
            }

            for (Monster mt : listmonster) {
                try {
                    mt.update();
                    if (mt.target != null && mt.target.exit) {
                        mt.target = null;
                    }

                    if (players.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                        Char fp = null;

                        for (int i = 0; i < players.size(); i++) {
                            try {
                                Char p = players.get(i);
                                if (p.isBot == -1) {
                                    if (p.near(mt, p.rangeAddMonster[0])
                                        || mt.allWayAdd()
                                        || mt.isBoss
                                        || mt.idTemplate == 84
                                        || mt.idTemplate == 90
                                        || mt.idTemplate == 36
                                        || mt.idTemplate == 37
                                        || this.map.mapIDLoadMap == 201) {
                                        if (Map.isNewVersion) {
                                            if (!p.nearMons.contains(mt.id)) {
                                                p.nearMons.add(mt.id);
                                                p.sendMessage(p.writeActorPos(new Message(4), mt));
                                            }
                                        } else {
                                            p.nearMons.add(mt.id);
                                        }
                                    }

                                    if (p.near(mt, 120) && mt.typeAttack == 1) {
                                        if (p.hp > 0
                                            && !p.beAttack
                                            && mt.target == null
                                            && mt.getMonsterTemplate().active
                                            && !p.isAdmin
                                            && p.isBot == -1
                                            && mt.isEnemy(p)
                                            && p.region == mt.region) {
                                            mt.target = p;
                                            p.beAttack = true;
                                        } else if (p.beAttack && fp == null) {
                                            fp = p;
                                        }
                                    }
                                }
                            } catch (Exception var10) {
                            }
                        }

                        if (fp != null && mt.target == null) {
                            mt.target = fp;
                        }
                    }
                } catch (Exception var11) {
                    Logger.logError(var11, "rUpdateleaf.txt");
                }
            }
        }

        int size = players.size();
        int k = 0;

        while (k < size) {
            try {
                Char p = players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    players.remove(k);
                    CharManager.instance.remove(p);
                } else if ((!p.map.equals(this.map) || p.mapID != this.map.mapId) && p.isBot == -1) {
                    players.remove(k);
                } else if (p.exit) {
                    players.remove(k);
                } else {
                    p.update();
                    k++;
                }
            } catch (Exception var9) {
                break;
            }
        }

        this.map.deletePotionAndItemOnGround(1);
    }

    public void updateWind() {
        Vector<Char> players = this.allPlayers.get(2);
        if (players.size() > 0) {
            while (this.tempMonster.get(2).size() > 0) {
                Monster mt = this.tempMonster.get(2).remove(0);
                if (mt.map != null && mt.map.mapId == this.map.mapId && mt.hp > 0) {
                    mt.map.addMonster(mt, 2, mt.region);
                }
            }

            while (this.tempRemoveMonster.get(2).size() > 0) {
                Monster mt = this.tempRemoveMonster.get(2).remove(0);
                if (mt.map != null && mt.map.mapId == this.map.mapId && mt.hp > 0) {
                    mt.map.removeMonster(mt.id, 2, mt.region);
                }
            }

            Collection<Monster> listmonster = this.monsters.get(2).values();
            if (this.isSkelonton) {
                listmonster = this.temp_monsters.get(2).values();
            }

            for (Monster mt : listmonster) {
                try {
                    mt.update();
                    if (mt.target != null && mt.target.exit) {
                        mt.target = null;
                    }

                    if (players.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                        Char fp = null;

                        for (int i = 0; i < players.size(); i++) {
                            try {
                                Char p = players.get(i);
                                if (p.isBot == -1) {
                                    if (p.near(mt, p.rangeAddMonster[0])
                                        || mt.allWayAdd()
                                        || mt.isBoss
                                        || mt.idTemplate == 84
                                        || mt.idTemplate == 90
                                        || mt.idTemplate == 36
                                        || mt.idTemplate == 37
                                        || this.map.mapIDLoadMap == 201) {
                                        if (Map.isNewVersion) {
                                            if (!p.nearMons.contains(mt.id)) {
                                                p.nearMons.add(mt.id);
                                                p.sendMessage(p.writeActorPos(new Message(4), mt));
                                            }
                                        } else {
                                            p.nearMons.add(mt.id);
                                        }
                                    }

                                    if (p.near(mt, 110) && mt.typeAttack == 1) {
                                        if (p.hp > 0
                                            && mt.isEnemy(p)
                                            && !p.beAttack
                                            && mt.target == null
                                            && mt.getMonsterTemplate().active
                                            && !p.isAdmin
                                            && p.isBot == -1
                                            && p.region == mt.region) {
                                            mt.target = p;
                                            p.beAttack = true;
                                        } else if (p.beAttack && fp == null) {
                                            fp = p;
                                        }
                                    }
                                }
                            } catch (Exception var9) {
                            }
                        }

                        if (fp != null && mt.target == null) {
                            mt.target = fp;
                        }
                    }
                } catch (Exception var10) {
                }
            }
        }

        boolean exitBoard = false;
        int size = players.size();
        int k = 0;

        while (k < players.size()) {
            try {
                Char p = players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    players.remove(k);
                    CharManager.instance.remove(p);
                    System.out.println("REMOVE ");
                    continue;
                }

                if ((!p.map.equals(this.map) || p.mapID != this.map.mapId) && p.isBot == -1) {
                    players.remove(k);
                    System.out.println("REMOVE ");
                    continue;
                }

                p.update();
            } catch (Exception var8) {
                break;
            }

            k++;
        }

        this.map.deletePotionAndItemOnGround(2);
    }

    public static void doAddGiftWin(Char p) {
        CharChienTruong c = MapChienTruongMoba.getCharChienTruong(p.charname);
        if (c != null) {
            if (TeamServer.isServerLienDau()) {
                Database.instance.doAddCharWinCT(p.charname);
            }

            p.addLuongLock(10);
            p.potions[123] = p.potions[123] + 5;
            int igdem = GemTemplate.ID_MATERIAL_LOW[5][Map.random(GemTemplate.ID_MATERIAL_LOW[5].length)];
            p.doAddGemItem(igdem, 3, true);
            String info = "Nhận được 10 lượng khoá, 5 thuốc cường hoá siêu cấp và 3 " + Map.gemTemplate[igdem].name + " khoá";
            p.sendMessage(MessageCreator.createMsgChat(p.id, info));
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createCharGemItem(p));
            Database.instance.saveOrtherLog("", p.charname, info, "quakhu");
        }
    }

    public void sendInfoPlayer(Char p) {
    }

    public void updateSand() {
        Vector<Char> players = this.allPlayers.get(0);
        if ((this.timeChuyenMap <= 0L || System.currentTimeMillis() - this.timeChuyenMap < 0L) && System.currentTimeMillis() - this.tend < 0L) {
            int countMonsDie = 0;
            if (players.size() > 0) {
                Collection<Monster> listmonster = this.monsters.get(0).values();

                try {
                    for (Monster mt : listmonster) {
                        try {
                            mt.update();
                            if (mt.isDead) {
                                countMonsDie++;
                            }

                            if (mt.target != null && mt.target.exit) {
                                mt.target = null;
                            }

                            if (players.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                                Char fp = null;

                                for (int i = 0; i < players.size(); i++) {
                                    try {
                                        Char p = players.get(i);
                                        if (p.isBot == -1) {
                                            if (p.near(mt, p.rangeAddMonster[0])
                                                || mt.allWayAdd()
                                                || mt.isBoss
                                                || mt.idTemplate == 84
                                                || mt.idTemplate == 90
                                                || mt.idTemplate == 36
                                                || mt.idTemplate == 37
                                                || this.map.mapIDLoadMap == 201) {
                                                if (Map.isNewVersion) {
                                                    if (!p.nearMons.contains(mt.id)) {
                                                        p.nearMons.add(mt.id);
                                                        p.sendMessage(p.writeActorPos(new Message(4), mt));
                                                    }
                                                } else {
                                                    p.nearMons.add(mt.id);
                                                }
                                            }

                                            if (p.near(mt, 110) && mt.isActive()) {
                                                if (p.hp > 0
                                                    && mt.isEnemy(p)
                                                    && !p.beAttack
                                                    && mt.target == null
                                                    && mt.isActive()
                                                    && !p.isAdmin
                                                    && p.isBot == -1
                                                    && p.region == mt.region) {
                                                    mt.target = p;
                                                    p.beAttack = true;
                                                } else if (p.beAttack && fp == null) {
                                                    fp = p;
                                                }
                                            }
                                        }
                                    } catch (Exception var12) {
                                    }
                                }

                                if (fp != null && mt.target == null) {
                                    mt.target = fp;
                                }
                            }
                        } catch (Exception var13) {
                            Logger.logError(var13, "rUpdateleaf.txt");
                        }
                    }
                } catch (Exception var14) {
                }

                if (countMonsDie >= this.monsters.get(0).size()) {
                    this.setTimeChuyenMap(3, this.isChauBau ? 1 : 0);
                }
            }

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
                } catch (Exception var11) {
                    var11.printStackTrace();
                    break;
                }
            }

            this.map.deletePotionAndItemOnGround(0);

            while (this.tempMonster.get(0).size() > 0) {
                Monster mt = this.tempMonster.get(0).remove(0);
                if (mt.map != null && mt.map.mapId == this.map.mapId && mt.hp > 0) {
                    mt.map.addMonster(mt, 0, mt.region);
                }
            }

            while (this.tempRemoveMonster.get(0).size() > 0) {
                Monster mt = this.tempRemoveMonster.get(0).remove(0);
                if (mt.map != null && mt.map.mapId == this.map.mapId && mt.hp > 0) {
                    mt.map.removeMonster(mt.id, 0, mt.region);
                }
            }
        } else {
            boolean isfinish = false;
            System.out
                .println("timechuyen map regionnuichaubau " + (System.currentTimeMillis() - this.timeChuyenMap) + " >> " + (System.currentTimeMillis() - this.tend));
            if (this.teamWin == 1 || System.currentTimeMillis() - this.tend >= 0L || this.comback) {
                isfinish = true;
            }

            while (players.size() > 0) {
                try {
                    if (isfinish) {
                        this.map.doReturnVillage(players.remove(0));
                    } else {
                        try {
                            Char p = players.remove(0);
                            this.map.move2Map(p, 25, 11, Map.idMapChanNui, p.inCountry);
                        } catch (Exception var9) {
                            var9.printStackTrace();
                        }
                    }
                } catch (Exception var10) {
                }
            }

            this.isStop = true;
        }
    }

    protected void doAttackPlayer(Char p, Message message) {
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
                    p.doAddPointChienTruong(c.getPointChienTruong());
                    if (cct != null && !c.isCharCopy()) {
                        cct.checkLienTram(c.charname);
                    }

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
                if (Map.nwar[p.inCountry] && this.map.mapId == 201) {
                    if (!p.checkLienTram(c.charname)) {
                        p.totalKill++;
                        if (p.totalKill > p.maxKill) {
                            p.maxKill = p.totalKill;
                            Map.doAddTopLienTram(p.myCountry, p);
                        }
                    }

                    c.totalKill = 0;
                }

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
            Vector<Char> players = this.allPlayers.get(i);
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
        synchronized (this.monsters) {
            Vector<Hashtable<Short, Monster>> monsters = this.monsters;
            short i = (short)country;
            this.temp_monsters.get(i).clear();

            for (Monster mt : monsters.get(i).values()) {
                if (!mt.isBoss) {
                    if (Map.random(100) < 50) {
                        Monster_Clone mons = new Monster_Clone((byte)1);
                        mons.map = mt.map;
                        mons.hp = mt.hp;
                        mons.maxhp = mt.maxhp;
                        mons.xp = mt.xp;
                        mons.xpClone = mt.getMonsterTemplate().rcvXp;
                        mons.maxxp = mt.getMonsterTemplate().rcvXp;
                        mons.default_x = mt.default_x;
                        mons.default_y = mt.default_y;
                        mons.defend_magic = mt.defend_magic;
                        mons.defend_physic = mt.defend_physic;
                        mons.attack = mt.attack;
                        mons.attackDelay = mt.attackDelay;
                        mons.idTemplate = 114;
                        mons.he = mt.he;
                        mons.inCountry = mt.inCountry;
                        mons.x = mt.x;
                        mons.y = mt.y;
                        mons.toX = mt.toX;
                        mons.toY = mt.toY;
                        mons.accurate = mt.accurate;
                        mons.dodge = mt.dodge;
                        mons.bornTime = mt.bornTime;
                        mons.dmove = mt.dmove;
                        mons.moveDelay = mt.moveDelay;
                        mons.id = mt.id;
                        mons.setType(mons.getMonsterTemplate().type);
                        mons.level = mt.level;
                        this.temp_monsters.get(i).put(mons.id, mons);
                    } else {
                        this.temp_monsters.get(i).put(mt.id, mt);
                    }
                } else {
                    this.temp_monsters.get(i).put(mt.id, mt);
                }
            }
        }
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
        this.monsters.get(0).put(id, mons);
    }

    public boolean canCreateCharCopyBoss(int team) {
        return false;
    }

    public void addCharChienTruongOffline(CharChienTruong c) {
    }

    public void playerJoin(Char player) {
        this.allPlayers.get(0).add(player);
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
        this.allPlayers.get(0).remove(p);
        this.isStop = true;
        this.resetTimeNuiChauBau(p);
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
        this.allPlayers.get(0).add(p);
    }

    public void onOffSkelonton(boolean onoff) {
        this.isSkelonton = onoff;
    }

    public Vector<Char> getAllPlayer(int inCountry) {
        return this.allPlayers.get(0);
    }

    public Hashtable<Short, Monster> getAllMons(int country) {
        return this.monsters.get(0);
    }

    public Monster getMonster(short id, int country) {
        return this.monsters.get(0).get(id);
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
        this.monsters.get(0).remove((short)idMons);
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
        } else if (cat == 1) {
            for (Monster mons : this.monsters.get(0).values()) {
                if (!mons.isDead && Map.near(from, mons) && !mons.isMonsterVantieu()) {
                    c.add(mons);
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
                for (int i = 0; i < this.allPlayers.get(0).size(); i++) {
                    Char player = this.allPlayers.get(0).get(i);
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
