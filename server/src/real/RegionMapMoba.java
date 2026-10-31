/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;
import util.Logger;

public class RegionMapMoba {
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
    boolean isSkelonton = false;
    long timeCheck = System.currentTimeMillis() + 5000L;
    public long timeCallBossCharCopy0 = System.currentTimeMillis() + 300000L;
    public long timeCallBossCharCopy1 = System.currentTimeMillis() + 300000L;
    public long timeCallRevivalQuaiOcDao = 0L;
    public long timeCallRevivalBossOcDao = 0L;
    static long timeDropCake = System.currentTimeMillis() + 7200000L;
    public static short[] MAX_RANGE_CHAR = new short[]{120, 120, 140, 120, 160};
    public Monster boss1 = null;
    public Monster boss2 = null;
    public Vector<CharChienTruong> team1 = new Vector<>();
    public Vector<CharChienTruong> team2 = new Vector<>();
    public Char charBoss1;
    public Char charBoss2;
    public boolean isHaveChienthan = false;
    short countT1 = 0;
    short countT2 = 0;
    static String[] nameTeam = new String[]{"Đội đỏ", "Đội Xanh"};
    public int teamWin = -1;
    public long timeChuyenMap = 0L;

    public RegionMapMoba() {
    }

    public RegionMapMoba(Map m, int id) {
        this.map = m;
        this.timeExist = System.currentTimeMillis();
        this.idRegion = (short)id;
        (new Thread() {
            private long lastTimeUpdateMap;

            @Override
            public void run() {
                while (RegionMapMoba.this.running && !AdminHandler.isStopServer) {
                    try {
                        while (RealController.savingChar) {
                            Thread.sleep(100L);
                        }

                        long l1 = System.currentTimeMillis();
                        if (l1 - this.lastTimeUpdateMap >= Map.DELAY_UPDATE_MAP) {
                            RegionMapMoba.this.update();
                            this.lastTimeUpdateMap = System.currentTimeMillis();
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
                    }

                    try {
                        synchronized (RegionMapMoba.this.LOCK) {
                            RegionMapMoba.this.LOCK.wait(500L);
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
        (new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("MAP SEND MONSTER 2 player" + RegionMapMoba.this.map.mapId);

                while (!AdminHandler.isStopServer && !RegionMapMoba.this.isStop) {
                    try {
                        int count = 0;
                        new Vector();
                        Vector<PlayerMessage> playerMessages = RegionMapMoba.this.allPlayerMessages.get(1);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                RegionMapMoba.this.map.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }

                        synchronized (RegionMapMoba.this.LOCK1) {
                            RegionMapMoba.this.LOCK1.wait(Map.timeDelay);
                        }
                    } catch (Exception var5) {
                        var5.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void startWindVilage() {
    }

    public void startSandVilage() {
        (new Thread() {
            @Override
            public void run() {
                while (!RegionMapMoba.this.isStop) {
                    try {
                        if (AdminHandler.isStopServer) {
                            RegionMapMoba.this.map_run_state = 0;
                            break;
                        }

                        while (RealController.savingChar) {
                            Thread.sleep(100L);
                        }

                        RegionMapMoba.this.map_run_state = 0;
                        long l1 = System.currentTimeMillis();
                        if (l1 - RegionMapMoba.this.lastTimeUpdateMap >= Map.DELAY_UPDATE_MAP) {
                            RegionMapMoba.this.updateSand();
                            RegionMapMoba.this.lastTimeUpdateMap = System.currentTimeMillis();
                        }

                        RegionMapMoba.this.map_run_state = 1;
                    } catch (Exception var5) {
                        var5.printStackTrace();
                        System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
                    }

                    try {
                        synchronized (RegionMapMoba.this.LOCK) {
                            RegionMapMoba.this.LOCK.wait(Map.DELAY_UPDATE_MAP);
                        }
                    } catch (Exception var4) {
                    }
                }
            }
        }).start();
        (new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        int count = 0;
                        new Vector();
                        Vector<PlayerMessage> playerMessages = RegionMapMoba.this.allPlayerMessages.get(0);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                RegionMapMoba.this.map.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }

                        synchronized (RegionMapMoba.this.LOCK) {
                            RegionMapMoba.this.LOCK.wait(Map.timeDelay);
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

            for (Monster mt : new java.util.ArrayList<Monster>(listmonster)) {   // ban sao: tranh ConcurrentModificationException
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

        if (!this.isSkelonton) {
            if (Map.openSkelonton()) {
                this.isSkelonton = true;
                this.reSetAllMonster(1);
                this.killAllMonster(this.monsters, 1);
            }
        } else if (!Map.openSkelonton()) {
            this.isSkelonton = false;
            this.killAllMonster(this.monsters, 1);
            this.temp_monsters.get(1).clear();
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

            for (Monster mt : new java.util.ArrayList<Monster>(listmonster)) {   // ban sao: tranh ConcurrentModificationException
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

        if (!this.isSkelonton) {
            if (Map.openSkelonton()) {
                this.isSkelonton = true;
                this.reSetAllMonster(2);
                this.killAllMonster(this.monsters, 2);
            }
        } else if (!Map.openSkelonton()) {
            this.isSkelonton = false;
            this.killAllMonster(this.monsters, 2);
            this.temp_monsters.get(2).clear();
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

    public static void doAddGiftTop() {
        Vector<String> name = Database.instance.getTopChienTruongMoba("lientram");

        for (int i = 0; i < name.size(); i++) {
            if (TeamServer.isServerLienDau()) {
                Database.instance.doAddCharTopCT(name.get(i), 0);
            }

            Char p = CharManager.instance.getCharByCharName(name.get(i));
            if (p != null) {
                p.addLuong(100);
                p.potions[35]++;
                int igdem = GemTemplate.ID_MATERIAL_LOW[5][Map.random(GemTemplate.ID_MATERIAL_LOW[5].length)];
                p.doAddGemItem(igdem, 3, false);
                String info = "Nhận được 100 lượng,  1 vé giờ vàng 1h và 3 " + Map.gemTemplate[igdem].name;
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, info, "quatopct");
            } else {
                Database.instance.saveOrtherLog("", name.get(i), "khong online khi trao qua top lien tran", "quatopct");
            }
        }

        name = Database.instance.getTopChienTruongMoba("sttru");

        for (int i = 0; i < name.size(); i++) {
            if (TeamServer.isServerLienDau()) {
                Database.instance.doAddCharTopCT(name.get(i), 1);
            }

            Char p = CharManager.instance.getCharByCharName(name.get(i));
            if (p != null) {
                p.addLuong(100);
                p.potions[35]++;
                int igdem = GemTemplate.ID_MATERIAL_LOW[5][Map.random(GemTemplate.ID_MATERIAL_LOW[5].length)];
                p.doAddGemItem(igdem, 6, false);
                String info = "Nhận được 100 lượng, 1 vktt 24h và 6 " + Map.gemTemplate[igdem].name;
                Map.doCreateVuKhiThoiTrang(p, 1440, Map.ID_VU_KHI_THOI_TRANG[p.charClass]);
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharGemItem(p));
                Database.instance.saveOrtherLog("", p.charname, info, "quatopct");
            } else {
                Database.instance.saveOrtherLog("", p.charname, "khong online khi trao qua top sat thuong tru", "quatopct");
            }
        }
    }

    public boolean isBossCanRevival() {
        if (this.timeCallRevivalBossOcDao != 0L && this.timeCallRevivalQuaiOcDao <= 0L) {
            if (System.currentTimeMillis() - this.timeCallRevivalBossOcDao < 0L) {
                return false;
            } else {
                this.timeCallRevivalBossOcDao = 0L;
                return true;
            }
        } else {
            return false;
        }
    }

    public void allQuaiOcDaoDie() {
        for (int i = 0; i < this.allQuaiOcDao.size(); i++) {
            if (!this.allQuaiOcDao.get(i).isDead) {
                this.timeCallRevivalBossOcDao = 0L;
                return;
            }
        }

        if (this.timeCallRevivalBossOcDao == 0L) {
            this.timeCallRevivalBossOcDao = System.currentTimeMillis() + 10000L;
        }
    }

    public void checkQuaiOcDaoHoiSinh() {
        if (System.currentTimeMillis() - this.timeCallRevivalQuaiOcDao >= 0L && this.timeCallRevivalQuaiOcDao > 0L) {
            for (int i = 0; i < this.allQuaiOcDao.size(); i++) {
                this.allQuaiOcDao.get(i).revivalQuaiOcDao();
            }

            this.timeCallRevivalQuaiOcDao = 0L;
            this.timeCallRevivalBossOcDao = 0L;
        }
    }

    public void sendInfoPlayer(Char p) {
        p.sendMessage(MessageCreator.createMsgTimeCountdown(this.infoPlayer, 5, -1, Char.ID_INFO_PLAYER, Map.NOT_COUNT_DOWN, -1));
    }

    public void updateSand() {
        Vector<Char> players = this.allPlayers.get(0);
        boolean isUpdateInfoPlayer = false;
        String st = "Đỏ " + this.countT1 + " : Xanh " + this.countT2;
        if (!this.infoPlayer.equals(st)) {
            this.infoPlayer = st;
            isUpdateInfoPlayer = true;
        }

        if (this.timeChuyenMap > 0L && System.currentTimeMillis() - this.timeChuyenMap >= 0L) {
            Vector<RegionMapMoba> allRegion = new Vector<>();

            for (int i = 0; i < MapChienTruongMoba.ALL_REGION.size(); i++) {
                RegionMapMoba rg = MapChienTruongMoba.ALL_REGION.get(i);
                if (!rg.isEnd()) {
                    allRegion.add(rg);
                }
            }

            boolean isFinishAll = false;
            if (allRegion.size() == 0) {
                isFinishAll = true;
            }

            int idRegion = 0;
            if (this.teamWin == 0) {
                while (this.team1.size() > 0) {
                    CharChienTruong c = this.team1.remove(0);
                    Char p = CharManager.instance.getCharByCharName(c.name);
                    if (p != null && c != null && !p.exit && p.map.isMapChienTruongMoba()) {
                        doAddGiftWin(p);
                        if (!isFinishAll) {
                            RegionMapMoba rg = allRegion.get(idRegion);
                            c.team = Map.r.nextInt(2);
                            p.x = (MapChienTruongMoba.POS_APPEAR[c.team][0] + Map.r.nextInt() % 5) * 16;
                            p.y = (MapChienTruongMoba.POS_APPEAR[c.team][1] + Map.r.nextInt() % 5) * 16;
                            p.setXtoYto(p.x, p.y);
                            p.region = rg.idRegion;
                            c.region = p.region;
                            p.map.move2Map(p, p.x / 16, p.y / 16, 40, p.inCountry);
                            idRegion = (idRegion + 1) % allRegion.size();

                            try {
                                Message m = new Message(65);
                                m.dos.writeShort(p.id);
                                p.pk_chienTruong = MapChienTruongMoba.idTypepk[c.team];
                                m.dos.writeByte(1);
                                m.dos.writeByte(MapChienTruongMoba.idTypepk[c.team]);
                                p.timeUsePK = System.currentTimeMillis();
                                p.sendMessage(m);
                                p.sendInfoChienTruong(Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA, 0);
                            } catch (Exception var13) {
                            }
                        } else {
                            this.map.doReturnVillage(p);
                        }
                    } else {
                        if (!isFinishAll) {
                            RegionMapMoba rg = allRegion.get(idRegion);
                            c.team = Map.r.nextInt(2);
                            c.region = rg.idRegion;
                            if (!isFinishAll) {
                                rg.addCharChienTruongOffline(c);
                            }
                        }

                        Database.instance.saveOrtherLog("", c.name, "khong co trong chien truong khi add qua", "quakhu");
                    }
                }

                while (this.team2.size() > 0) {
                    CharChienTruong c = this.team2.remove(0);
                    Char p = CharManager.instance.getCharByCharName(c.name);
                    if (p != null) {
                        if (p.isCharChienTruong()) {
                            CharManager.instance.remove(p);
                        } else if (!p.exit && p.map.isMapChienTruongMoba() && !p.isCharChienTruong()) {
                            this.map.doReturnVillage(p);
                        }
                    }
                }
            } else {
                while (this.team2.size() > 0) {
                    CharChienTruong c = this.team2.remove(0);
                    Char p = CharManager.instance.getCharByCharName(c.name);
                    if (p != null && c != null && !p.exit && p.map.isMapChienTruongMoba()) {
                        doAddGiftWin(p);
                        if (!isFinishAll) {
                            RegionMapMoba rg = allRegion.get(idRegion);
                            c.team = Map.r.nextInt(2);
                            p.x = (MapChienTruongMoba.POS_APPEAR[c.team][0] + Map.r.nextInt() % 5) * 16;
                            p.y = (MapChienTruongMoba.POS_APPEAR[c.team][1] + Map.r.nextInt() % 5) * 16;
                            p.setXtoYto(p.x, p.y);
                            p.region = rg.idRegion;
                            c.region = p.region;
                            p.map.move2Map(p, p.x / 16, p.y / 16, 40, p.inCountry);
                            idRegion = (idRegion + 1) % allRegion.size();

                            try {
                                Message m = new Message(65);
                                m.dos.writeShort(p.id);
                                p.pk_chienTruong = MapChienTruongMoba.idTypepk[c.team];
                                m.dos.writeByte(1);
                                m.dos.writeByte(MapChienTruongMoba.idTypepk[c.team]);
                                p.timeUsePK = System.currentTimeMillis();
                                p.sendMessage(m);
                                p.sendInfoChienTruong(Char.ID_ALL_KHU_CHIEN_TRUONG_MOBA, 0);
                            } catch (Exception var12) {
                            }
                        } else {
                            this.map.doReturnVillage(p);
                        }
                    } else {
                        if (!isFinishAll) {
                            RegionMapMoba rg = allRegion.get(idRegion);
                            c.team = Map.r.nextInt(2);
                            c.region = rg.idRegion;
                            if (!isFinishAll) {
                                rg.addCharChienTruongOffline(c);
                            }
                        }

                        Database.instance.saveOrtherLog("", c.name, "khong co trong chien truong khi add qua", "quakhu");
                    }
                }

                while (this.team1.size() > 0) {
                    CharChienTruong c = this.team1.remove(0);
                    Char p = CharManager.instance.getCharByCharName(c.name);
                    if (p != null) {
                        if (p.isCharChienTruong()) {
                            p.putIDChar2IDGen();
                            CharManager.instance.remove(p);
                        } else if (!p.exit && p.map.isMapChienTruongMoba() && !p.isCharChienTruong()) {
                            this.map.doReturnVillage(p);
                        }
                    }
                }
            }

            while (this.allPlayers.get(0).size() > 0) {
                Char p = this.allPlayers.get(0).remove(0);
                if (p.isCharChienTruong()) {
                    p.putIDChar2IDGen();
                    CharManager.instance.remove(p);
                }
            }

            this.isStop = true;
            if (isFinishAll) {
                doAddGiftTop();
                MapChienTruongMoba.isStart = false;

                try {
                    this.map.doResetChienTruong();
                } catch (Exception var11) {
                }
            }
        } else {
            this.checkQuaiOcDaoHoiSinh();
            this.allQuaiOcDaoDie();
            String info = "";
            if (this.timeCallBossCharCopy0 > 0L && System.currentTimeMillis() - this.timeCallBossCharCopy0 >= 0L) {
                this.map.doCreateBossCharCopy(this.idRegion, 0);
                info = "đội đỏ, ";
                this.timeCallBossCharCopy0 = 0L;
            }

            if (this.timeCallBossCharCopy1 > 0L && System.currentTimeMillis() - this.timeCallBossCharCopy1 >= 0L) {
                this.map.doCreateBossCharCopy(this.idRegion, 1);
                info = info + "đội xanh";
                this.timeCallBossCharCopy1 = 0L;
            }

            if (!info.equals("")) {
                try {
                    this.map.sendAllPlayer(MessageCreator.createServerAlertAutoOffMessage("Quái thú " + info + " đã xuất hiện"), 0, this.idRegion);
                } catch (Exception var14) {
                }
            }

            if (this.charBoss1 != null) {
                this.charBoss1.update();
            }

            if (this.charBoss2 != null) {
                this.charBoss2.update();
            }

            if (players.size() > 0) {
                Collection<Monster> listmonster = this.monsters.get(0).values();
                if (this.isSkelonton) {
                    listmonster = this.temp_monsters.get(0).values();
                }

                try {
                    for (Monster mt : new java.util.ArrayList<Monster>(listmonster)) {   // ban sao: tranh ConcurrentModificationException
                        try {
                            mt.update();
                            if (mt.target != null && mt.target.exit) {
                                mt.target = null;
                            }

                            if (players.size() > 0 && (Map.isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                                Char fp = null;

                                for (int ix = 0; ix < players.size(); ix++) {
                                    try {
                                        Char p = players.get(ix);
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
                                    } catch (Exception var16) {
                                    }
                                }

                                if (fp != null && mt.target == null) {
                                    mt.target = fp;
                                }
                            }
                        } catch (Exception var17) {
                            Logger.logError(var17, "rUpdateleaf.txt");
                        }
                    }
                } catch (Exception var18) {
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
                        if (isUpdateInfoPlayer) {
                            this.sendInfoPlayer(p);
                        }

                        k++;
                    }
                } catch (Exception var15) {
                    var15.printStackTrace();
                    break;
                }
            }

            if (!this.isSkelonton) {
                if (Map.openSkelonton()) {
                    this.isSkelonton = true;
                    this.reSetAllMonster(0);
                    this.killAllMonster(this.monsters, 0);
                }
            } else if (!Map.openSkelonton()) {
                this.isSkelonton = false;
                this.killAllMonster(this.monsters, 0);
                this.temp_monsters.get(0).clear();
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
            int damNguyetAnh = p.getPCDamNguyetAnh(skill);
            if (c.hp > 0 && damNguyetAnh > 0) {
                c.hp = c.hp - c.maxhp * damNguyetAnh / 100;
                damage += c.maxhp * damNguyetAnh / 100;
                p.sendEffectBuff(c, EffectBuff.EFF_NGUYET_ANH, 1000);
            }

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
        } catch (Exception var24) {
            var24.printStackTrace();
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
                    } catch (Exception var37) {
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
                                    if (Map.inRangeActor(p, mt, MAX_RANGE_CHAR[p.charClass])) {
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
                                                                                    int var54 = 0;
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
                                                                                    if (newxp == dxp) {
                                                                                        int var65 = dxp * Map.doubleALL;
                                                                                        var65 = p.expReceive(var65);
                                                                                        Map.addXPForChar(p, var65 + p.getEffSkillClan(0) * var65 / 100, false, "Region6");
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
                                                                            } catch (Exception var38) {
                                                                                var38.printStackTrace();
                                                                            }

                                                                            try {
                                                                                int dxpx = Map.rand10(mt.xp);
                                                                                if (dxpx == 0) {
                                                                                    dxpx = 1;
                                                                                }

                                                                                int[] downPercentx = new int[]{1, 5, 10, dxpx};
                                                                                int targetLvx = p.getLevel();
                                                                                if (targetLvx < 40) {
                                                                                    downPercentx = new int[]{1, 2, 3, dxpx};
                                                                                }

                                                                                int deltax = targetLvx - mt.level;
                                                                                if (deltax > 0) {
                                                                                    int ax = deltax / 4;
                                                                                    if (targetLvx < 40) {
                                                                                        ax = deltax / 6;
                                                                                    }

                                                                                    if (ax > 3) {
                                                                                        ax = 3;
                                                                                    }

                                                                                    dxpx /= downPercentx[ax];
                                                                                    if (dxpx <= 0) {
                                                                                        dxpx = 1;
                                                                                    }
                                                                                }

                                                                                if (dxpx > 0) {
                                                                                    int newxp = Map.calculatorXpParty(p, dxpx);
                                                                                    if (newxp == dxpx) {
                                                                                        int var69 = dxpx * Map.doubleALL;
                                                                                        var69 = p.expReceive(var69);
                                                                                        Map.addXPForChar(p, var69 + p.getEffSkillClan(0) * var69 / 100, false, "Region9");
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
                                                                            } catch (Exception var40) {
                                                                                var40.printStackTrace();
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
                                                                            } catch (Exception var39) {
                                                                                var39.printStackTrace();
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
        if (mons.isBossTruRong()) {
            if (mons.getTeam() == 0) {
                this.boss1 = mons;
            } else {
                this.boss2 = mons;
            }
        }

        if (mons.isMonsterOcDao()) {
            this.allQuaiOcDao.add(mons);
        }
    }

    public boolean canCreateCharCopyBoss(int team) {
        for (int i = 0; i < this.allPlayers.get(0).size(); i++) {
            Char p = this.allPlayers.get(0).get(i);
            if (p.isCharCopy() && ((CharCopyMoba)p).team == team) {
                return false;
            }
        }

        return true;
    }

    public void addCharChienTruongOffline(CharChienTruong c) {
        if (c.team == 0) {
            this.team1.add(c);
        } else {
            this.team2.add(c);
        }
    }

    public void playerJoin(Char player) {
        if (player.charname.equals("chienthan")) {
            this.isHaveChienthan = true;
        }

        if (!player.isCharCopy()) {
            CharChienTruong c = MapChienTruongMoba.all_char_chien_truong.get(player.charname);
            if (c.team == 0) {
                if (!this.team1.contains(c)) {
                    this.team1.add(c);
                }

                this.countT1++;
            } else {
                if (!this.team2.contains(c)) {
                    this.team2.add(c);
                }

                this.countT2++;
            }
        }

        this.allPlayers.get(0).add(player);
        player.nNpc = 0;
        player.map = this.map;
        player.region = this.idRegion;
        this.sendInfoPlayer(player);
    }

    public void playerExit(Char p) {
    }

    public void removePlayer(int country, Char p) {
        if (!p.isCharCopy()) {
            CharChienTruong c = MapChienTruongMoba.all_char_chien_truong.get(p.charname);
            if (c.team == 0) {
                if (this.countT1 > 0) {
                    this.countT1--;
                }
            } else if (this.countT2 > 0) {
                this.countT2--;
            }
        }

        this.allPlayers.get(0).remove(p);
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

    public void setTimeChuyenMap(long time, int teamwin) {
        if (this.timeChuyenMap == 0L) {
            System.out.println("doi chien thang " + teamwin);
            this.timeChuyenMap = System.currentTimeMillis() + time;
            this.teamWin = teamwin;

            try {
                for (int i = 0; i < this.allPlayers.get(0).size(); i++) {
                    Char player = this.allPlayers.get(0).get(i);
                    player.sendMessage(
                        MessageCreator.createMsgTimeCountdown(
                            nameTeam[teamwin] + " thắng. Chuyển map sau", 10, -1, Char.ID_CHUYEN_MAP_CHIEN_TRUONG_MOBA, Map.COUNT_DOWN, -1
                        )
                    );
                }
            } catch (Exception var6) {
            }
        }
    }

    public boolean isEnd() {
        return this.timeChuyenMap > 0L;
    }

    public void addTru(int pos, Monster m, int team) {
        if (team == 0) {
            if (pos == MapChienTruongMoba.P_TOP) {
                this.allTruTop.add(m);
            } else if (pos == MapChienTruongMoba.P_MID) {
                this.allTruMid.add(m);
            } else if (pos == MapChienTruongMoba.P_BOT) {
                this.allTruBot.add(m);
            } else if (pos == MapChienTruongMoba.P_MID_MAIN) {
                this.allTruMidMain.add(m);
            }
        } else if (team == 1) {
            if (pos == MapChienTruongMoba.P_TOP) {
                this.allTruTop1.add(m);
            } else if (pos == MapChienTruongMoba.P_MID) {
                this.allTruMid1.add(m);
            } else if (pos == MapChienTruongMoba.P_BOT) {
                this.allTruBot1.add(m);
            } else if (pos == MapChienTruongMoba.P_MID_MAIN) {
                this.allTruMidMain1.add(m);
            }
        }
    }

    public boolean isAllTruDie(Vector<Monster> tru) {
        if (tru.size() == 0) {
            return true;
        } else {
            for (int i = 0; i < tru.size(); i++) {
                if (!tru.get(i).isDead || tru.get(i).hp > 0) {
                    return false;
                }
            }

            return true;
        }
    }

    public Vector<Monster> getTru(int team, int pos) {
        if (team == 0) {
            if (pos == MapChienTruongMoba.P_TOP) {
                return this.allTruTop;
            }

            if (pos == MapChienTruongMoba.P_MID) {
                return this.allTruMid;
            }

            if (pos == MapChienTruongMoba.P_BOT) {
                return this.allTruBot;
            }

            if (pos == MapChienTruongMoba.P_MID_MAIN) {
                return this.allTruMidMain;
            }
        } else if (team == 1) {
            if (pos == MapChienTruongMoba.P_TOP) {
                return this.allTruTop1;
            }

            if (pos == MapChienTruongMoba.P_MID) {
                return this.allTruMid1;
            }

            if (pos == MapChienTruongMoba.P_BOT) {
                return this.allTruBot1;
            }

            if (pos == MapChienTruongMoba.P_MID_MAIN) {
                return this.allTruMidMain1;
            }
        }

        return new Vector<>();
    }
}
