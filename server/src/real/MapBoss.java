/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.GemItem;
import io.Message;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;

public class MapBoss extends Map {
    Hashtable<Short, Monster> monsters = new Hashtable<>();
    Vector<Char> players = new Vector<>();
    short ID_MONSTER = 0;
    int wave = 0;

    @Override
    public boolean isMapTrain() {
        return false;
    }

    @Override
    public boolean isMapBoss() {
        return true;
    }

    public MapBoss(int id, int idXaphu, int magic_physic, int mapload) {
        this.initAllMonsTer();
        this.mapId = id;
        this.mapIDLoadMap = mapload;
        short loadmap = (short)mapload;
        if (loadmap == -1) {
            loadmap = (short)this.mapId;
        }

        this.idXaphu = (byte)idXaphu;
        if (this.mapId != -1) {
            this.loadMap("map/map" + loadmap, magic_physic);
        }

        FileInputStream fis = null;
        DataInputStream dis = null;
        FileInputStream fisMap = null;
        DataInputStream disMap = null;

        try {
            fis = new FileInputStream(loadmap < 110 ? "cMap/t.type" : (loadmap > 200 ? "cMap/t_thanh.type" : "cMap/t_hang.type"));
            dis = new DataInputStream(fis);
            this.typeOfTile = new int[dis.available()];

            for (int i = 0; i < this.typeOfTile.length; i++) {
                this.typeOfTile[i] = dis.read();
            }

            fisMap = new FileInputStream("cMap/" + loadmap);
            disMap = new DataInputStream(fisMap);
            this.w = disMap.read();
            this.h = disMap.read();
            this.map = new short[this.w * this.h];
            this.type = new int[this.w * this.h];

            for (int i = 0; i < this.w * this.h; i++) {
                try {
                    this.map[i] = (short)disMap.read();
                    if (this.map[i] != -1 && this.map[i] != 255 && this.map[i] != 254) {
                        this.type[i] = this.typeOfTile[this.map[i]];
                    }
                } catch (Exception var12) {
                    var12.printStackTrace();
                }
            }

            this.loadTileMap(null, disMap, loadmap);
        } catch (Exception var13) {
        }

        this.gemItem.add(new Vector<>());
        this.gemItem.add(new Vector<>());
        this.gemItem.add(new Vector<>());
        this.items.add(new Vector<>());
        this.items.add(new Vector<>());
        this.items.add(new Vector<>());
        this.potions.add(new Vector<>());
        this.potions.add(new Vector<>());
        this.potions.add(new Vector<>());
        this.doStartThreadUpdatePlayer();
        this.allPlayerMessages.add(new Vector<>());
        this.allPlayerMessages.add(new Vector<>());
        this.allPlayerMessages.add(new Vector<>());
        this.ALL_EFFECT_INMAP.add(new Vector<>());
        this.ALL_EFFECT_INMAP.add(new Vector<>());
        this.ALL_EFFECT_INMAP.add(new Vector<>());
        this.startLeafVilage();
        new Thread(this).start();
    }

    @Override
    protected Item getItem(short id, int country) {
        Vector<Item> items = this.items.get(0);

        for (int i = 0; i < items.size(); i++) {
            try {
                if (items.get(i).id == id) {
                    return items.get(i);
                }
            } catch (Exception var6) {
                break;
            }
        }

        return null;
    }

    @Override
    protected void addItem(Item pt, int country) {
        this.items.get(0).add(pt);
    }

    @Override
    protected void addPotion(Potion pt, int country) {
        this.potions.get(0).add(pt);
    }

    @Override
    protected void removePotion(Potion pt, int country) {
        this.potions.get(0).remove(pt);
    }

    @Override
    protected void removeItem(Item pt, int country) {
        this.items.get(0).remove(pt);
    }

    @Override
    protected Potion getPotion(short id, int country) {
        Vector<Potion> potions = this.potions.get(0);

        for (int i = 0; i < potions.size(); i++) {
            try {
                if (potions.get(i).id == id) {
                    return potions.get(i);
                }
            } catch (Exception var6) {
                break;
            }
        }

        return null;
    }

    @Override
    public void loadMap(String filename, int magic_physic) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            DataInputStream dis = new DataInputStream(fis);
            int w = dis.readUnsignedByte();
            int h = dis.readUnsignedByte();
            this.mapWidth = w * 16;
            this.mapHeight = h * 16;
            Monster m0 = null;
            int totalMonster = 0;

            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int value = dis.readUnsignedByte();
                    if (value > 0 && value != 255) {
                        if (this.mapId == 17 && value > 81) {
                            value += 3;
                        }

                        m0 = new Monster(this, monsterTemplates.get(value), j * 16, i * 16, 1);
                        m0.level = m0.getMonsterTemplate().level;
                        m0.id = RealController.intance.idGen.getID(1, "new monster");
                        byte[] he = new byte[]{0, 1, 2, 3, 4};
                        m0.he = he[r.nextInt(5)];
                        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
                        m0.typeAttack = t[r.nextInt(10)];
                        this.monsters.put(m0.id, m0);
                        m0.magic_physic = (byte)magic_physic;
                        totalMonster++;
                    }
                }
            }

            dis.close();
            fis.close();
            DataInputStream var20 = null;
            FileInputStream var17 = null;
            System.out.println("TONG SO NG LIEU " + totalMonster);
        } catch (FileNotFoundException var15) {
            System.out.println("File not found: " + filename);
            var15.printStackTrace();
        } catch (IOException var16) {
            System.out.println("Load map error: " + filename);
            var16.printStackTrace();
        }

        try {
            if (this.mapIDLoadMap == 111) {
                if (Char.isSuKienHalowwen2015()) {
                    Boss bo = new BossDracula(this, monsterTemplates.get(116), 352, 400, 0);
                    bo.id = RealController.intance.idGen.getID(1, "new monster");
                    byte[] he = new byte[]{0, 1, 2, 3, 4};
                    bo.he = he[r.nextInt(5)];
                    bo.level = bo.getMonsterTemplate().level;
                    bo.randomMap = false;
                    bo.isDead = true;
                    bo.moveDelay = 1000L;
                    this.monsters.put(bo.id, bo);
                } else if (Char.isSuKienHe2017()) {
                    Boss bo = new BossNguoiTuyet(this, monsterTemplates.get(90), 352, 400, 0);
                    bo.id = RealController.intance.idGen.getID(1, "new monster");
                    byte[] he = new byte[]{0, 1, 2, 3, 4};
                    bo.he = he[r.nextInt(5)];
                    bo.level = bo.getMonsterTemplate().level;
                    bo.randomMap = false;
                    bo.isDead = true;
                    bo.moveDelay = 1000L;
                    if (TeamServer.isServerLocal()) {
                        bo.maxhp = 10;
                        bo.hp = 10;
                    } else {
                        bo.maxhp = 5000000;
                        bo.hp = 5000000;
                        bo.maxxp = 1000000;
                    }

                    bo.xp = 1000000;
                    this.monsters.put(bo.id, bo);
                }
            }
        } catch (Exception var14) {
        }
    }

    @Override
    public void sendAllPlayer(Message m, int country) {
        try {
            Vector<Char> players = this.players;

            for (int i = 0; i < players.size(); i++) {
                players.elementAt(i).sendMessage(m);
            }

            m.cleanup();
        } catch (Exception var5) {
        }
    }

    @Override
    public Vector<Char> getAllPlayer(int inCountry, int region) {
        return this.players;
    }

    @Override
    public void playerJoin(Char player) {
        this.players.add(player);
        player.map = this;
        player.mapID = this.mapId;
    }

    @Override
    protected void removeGem(GemItem pt, int country) {
        this.gemItem.get(0).remove(pt);
    }

    @Override
    public void removePlayer(int country, Char p) {
        this.players.remove(p);
    }

    @Override
    public void removePLayer(Char player) {
        for (int i = 0; i < this.players.size(); i++) {
            try {
                if (this.players.get(i).charname.toLowerCase().equals(player.charname.toLowerCase())) {
                    this.players.remove(i);
                    break;
                }
            } catch (Exception var4) {
            }
        }
    }

    @Override
    public void playerExit(Char player) {
        Message m = new Message(8);

        try {
            m.dos.writeShort(player.id);
        } catch (IOException var6) {
        }

        int kk = 1;
        if (kk != -1) {
            try {
                this.removePLayer(player);
                player.sendToNearPlayer(m);
            } catch (Exception var5) {
                System.out.println("LOI REMOVE PLAYER KHOI MAP DUGEON");
            }
        }

        m.cleanup();
    }

    @Override
    public void startLeafVilage() {
        (new Thread() {
            @Override
            public void run() {
                while (!AdminHandler.isStopServer) {
                    try {
                        int count = 0;
                        new Vector();
                        Vector<PlayerMessage> playerMessages = MapBoss.this.allPlayerMessages.get(1);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                MapBoss.this.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }

                    try {
                        synchronized (MapBoss.this.LOCK) {
                            MapBoss.this.LOCK.wait(MapBoss.timeDelay);
                        }
                    } catch (Exception var5) {
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
                        Vector<PlayerMessage> playerMessages = MapBoss.this.allPlayerMessages.get(2);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                MapBoss.this.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }

                    try {
                        synchronized (MapBoss.this.LOCK1) {
                            MapBoss.this.LOCK1.wait(MapBoss.timeDelay);
                        }
                    } catch (Exception var5) {
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
                        Vector<PlayerMessage> playerMessages = MapBoss.this.allPlayerMessages.get(0);

                        while (playerMessages.size() > 0) {
                            PlayerMessage pm = (PlayerMessage)playerMessages.remove(0);
                            if (!pm.player.exit) {
                                MapBoss.this.processMessage(pm.player, pm.message);
                            }

                            if (++count == 500) {
                                count = 0;
                                Thread.sleep(5L);
                            }
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }

                    try {
                        synchronized (MapBoss.this.LOCK2) {
                            MapBoss.this.LOCK2.wait(MapBoss.timeDelay);
                        }
                    } catch (Exception var5) {
                    }
                }
            }
        }).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.currentThread().setName("MAP " + this.mapId);
                if (AdminHandler.isStopServer) {
                    this.map_run_state = 0;
                    return;
                }

                this.map_run_state = 0;
                long l1 = System.currentTimeMillis();
                if (l1 - this.lastTimeUpdateMap > DELAY_UPDATE_MAP) {
                    this.lastTimeUpdateMap = l1;
                    this.update();
                }

                this.map_run_state = 1;
            } catch (Exception var5) {
                System.out.println("LOI TRONG HAM RUN MAP material ");
            }

            synchronized (this.LOCK) {
                try {
                    this.LOCK.wait(DELAY_UPDATE_MAP);
                } catch (Exception var3) {
                }
            }
        }
    }

    @Override
    public Monster getMonster(short id, int country, int region) {
        return this.monsters.get(id);
    }

    @Override
    public void doStartThreadUpdatePlayer() {
    }

    @Override
    public void update() {
        if (this.players.size() > 1 || this.monsters.size() > 0) {
            for (Monster mt : this.monsters.values()) {
                try {
                    mt.update();
                    if (mt.target != null && mt.target.exit) {
                        mt.target = null;
                    }

                    if (this.players.size() > 0 && (isNewVersion ? !mt.isDead : !mt.isDead && mt.moved)) {
                        for (int i = 0; i < this.players.size(); i++) {
                            try {
                                Char p = this.players.get(i);
                                if (p.isBot == -1 && p.near(mt, p.rangeAddMonster[0])) {
                                    if (isNewVersion) {
                                        if (!p.nearMons.contains(mt.id)) {
                                            p.nearMons.add(mt.id);
                                            p.sendMessage(p.writeActorPos(new Message(4), mt));
                                        }
                                    } else {
                                        p.nearMons.add(mt.id);
                                    }
                                }
                            } catch (Exception var6) {
                            }
                        }
                    }
                } catch (Exception var8) {
                    System.out.println("UPDATE MAP MT KHOANG");
                }
            }

            int k = 0;

            while (k < this.players.size()) {
                try {
                    Char p = this.players.get(k);
                    if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                        this.players.remove(k);
                        CharManager.instance.remove(p);
                    } else if ((!p.map.equals(this) || p.mapID != this.mapId) && p.isBot == -1) {
                        this.players.remove(k);
                    } else {
                        p.update();
                        k++;
                    }
                } catch (Exception var7) {
                    break;
                }
            }
        }
    }

    @Override
    public void addPlayerMessage(Char p, Message message) {
        this.allPlayerMessages.get(p.myCountry).add(new PlayerMessage(p, message));
    }

    @Override
    public GemItem doCreateGemItemMaterial(int idTemplateMonster) {
        GemItem gem = null;
        int[][] template = new int[][]{{81, 116}, {67, 102}, {88, 123}, {95, 130}, {74, 109}};
        int index = 0;
        if (r.nextInt(1000) == 1) {
            index = 1;
        }

        return new GemItem(template[idTemplateMonster - 85][index]);
    }

    @Override
    protected void addGemItem(GemItem pt, int country) {
        this.gemItem.get(0).add(pt);
    }

    @Override
    protected GemItem getGem(short id, int country) {
        Vector<GemItem> gemItem = this.gemItem.get(0);

        for (int i = 0; i < gemItem.size(); i++) {
            try {
                if (gemItem.get(i).id == id) {
                    return gemItem.get(i);
                }
            } catch (Exception var6) {
                break;
            }
        }

        return null;
    }

    @Override
    public void deletePotionAndItemOnGround(int country) {
        long now = System.currentTimeMillis();
        int size = this.gemItem.get(0).size() - 1;

        for (int i = size; i >= 0; i--) {
            try {
                GemItem item = this.gemItem.get(0).elementAt(i);
                if (now - item.time_drop > 30000L) {
                    this.gemItem.get(0).remove(item);
                }
            } catch (Exception var7) {
            }
        }
    }

    @Override
    public void addMonsterDun() {
    }

    @Override
    public boolean checkBossLive(int country) {
        for (Monster mons : this.monsters.values()) {
            if (mons.isBoss) {
                return !mons.isDead;
            }
        }

        return false;
    }

    @Override
    public boolean isPublicMap() {
        return true;
    }
}
