/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.GemItem;
import data.LienHoaTru;
import io.Message;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import server.TeamServer;

public class MapVanTienTran extends Map {
    Vector<Char> players = new Vector<>();
    Hashtable<Short, Monster> monsters = new Hashtable<>();
    Hashtable<Short, RegionVantienTran> ALL_REGION = new Hashtable<>();
    Vector<RegionVantienTran> V_ALL_REGION = new Vector<>();
    short ID_REGION = 0;
    static int[][] xyboss = new int[][]{{13, 13}, {24, 12}, {35, 12}, {35, 23}, {35, 37}, {24, 39}, {35, 38}, {15, 25}};
    static int[][] xytrulienhoa = new int[][]{{19, 11}, {30, 21}, {19, 31}, {32, 42}};
    public static int[][] idTemplateMonster = new int[][]{{21, 23, 24}, {30, 32, 40}, {41, 48, 49}, {51, 74, 76}, {77, 80, 81}};
    static short idMonster = -32000;
    boolean started = false;
    public static boolean startByAdmin = false;

    @Override
    public boolean isMapTrain() {
        return false;
    }

    @Override
    public void doAddLienHoaTru(Char player, int pos) {
        Monster m = new LienHoaTru(this, monsterTemplates.get(43), xytrulienhoa[pos][0] * 16, xytrulienhoa[pos][1] * 16, 0);
        m.level = m.getMonsterTemplate().level;
        m.id = idMonster++;
        byte[] he = new byte[]{0, 1, 2, 3, 4};
        m.he = he[r.nextInt(5)];
        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
        m.typeAttack = t[r.nextInt(10)];
        m.bornTime = 120000L;
        this.monsters.put(m.id, m);
    }

    @Override
    public void doAddLienHoaTru(int pos, int idClan, int myCountry) {
        Monster m = new LienHoaTru(this, monsterTemplates.get(43), xytrulienhoa[pos][0] * 16, xytrulienhoa[pos][1] * 16, 0);
        m.level = m.getMonsterTemplate().level;
        m.id = idMonster++;
        byte[] he = new byte[]{0, 1, 2, 3, 4};
        m.he = he[r.nextInt(5)];
        byte[] t = new byte[]{0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0};
        m.typeAttack = t[r.nextInt(10)];
        m.bornTime = 120000L;
        this.monsters.put(m.id, m);
    }

    public MapVanTienTran(int id, int idXaphu, int magic_physic, int mapload, int nregion) {
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
                } catch (Exception var13) {
                    var13.printStackTrace();
                }
            }

            this.loadTileMap(null, disMap, loadmap);
        } catch (Exception var14) {
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
        new Thread(this).start();
    }

    @Override
    public void checkStartVanTienTran() {
        if (!TeamServer.isServerIndo()) {
            Calendar cl = Calendar.getInstance();
            int iHour = cl.get(11);
            int iMinute = cl.get(12);

            try {
                if (!this.started) {
                    if (iHour == 22 || startByAdmin || Char.getDayOpen(0L).equals("2015-12-30") && iHour == 15) {
                        this.started = true;
                        int size = 5;
                        if (Char.getDayOpen(0L).equals("2015-12-24")) {
                            size = 20;
                        }

                        for (int i = 0; i < size; i++) {
                            int level = 50;
                            if (this.mapId == 209) {
                                level = 60;
                            } else if (this.mapId == 210) {
                                level = 70;
                            }

                            RegionVantienTran rg = new RegionVantienTran(this, this.ID_REGION, level);
                            this.ALL_REGION.put(rg.id, rg);
                            this.V_ALL_REGION.add(rg);
                            this.ID_REGION++;
                            if (this.ID_REGION < 0) {
                                this.ID_REGION = 0;
                            }
                        }

                        try {
                            if (this.mapId == 208) {
                                sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage("Trấn yêu trận đã mở"));
                            }
                        } catch (Exception var8) {
                        }

                        System.out.println("VAN TIEN TRAN BAT DAU " + this.mapId);
                    }
                } else if (iHour != 22 && iMinute >= 5 && !startByAdmin && iHour != 15) {
                    this.started = false;
                    this.ALL_REGION.clear();
                    this.V_ALL_REGION.removeAllElements();
                    this.ID_REGION = 0;
                    System.out.println("VAN TIEN TRAN KET THUC " + this.mapId);
                }
            } catch (Exception var9) {
                var9.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName("MAP " + this.mapId);

        while (true) {
            try {
                if (AdminHandler.isStopServer) {
                    this.map_run_state = 0;
                    return;
                }

                while (RealController.savingChar) {
                    Thread.sleep(100L);
                }

                this.checkStartVanTienTran();
                this.map_run_state1 = 0;
                long l1 = System.currentTimeMillis();
                if (l1 - this.lastTimeUpdateMap1 >= DELAY_UPDATE_MAP) {
                    this.update();
                    this.lastTimeUpdateMap1 = System.currentTimeMillis();
                }

                this.map_run_state1 = 1;
            } catch (Exception var5) {
                var5.printStackTrace();
                System.out.println("LOI TRONG HAM RUN MAP " + var5.toString());
            }

            try {
                synchronized (this.LOCK1) {
                    this.LOCK1.wait(DELAY_UPDATE_MAP);
                }
            } catch (Exception var4) {
            }
        }
    }

    @Override
    public void update() {
        this.deletePotionAndItemOnGround(1);
    }

    public MapVanTienTran(int id) {
        super(id);
    }

    public MapVanTienTran() {
    }

    public void loadMonster(int wave) {
    }

    @Override
    public void addPlayerMessage(Char p, Message message) {
        this.ALL_REGION.get((short)p.region).addPlayerMessage(p, message);
    }

    @Override
    public void playerJoin(Char player) {
        RegionVantienTran rg = this.ALL_REGION.get((short)player.region);
        if (rg == null && !this.ALL_REGION.isEmpty()) {
            rg = this.ALL_REGION.values().iterator().next(); // khu khong ton tai (vao bang GM / dich chuyen): khu dau tien
        }
        if (rg == null) {
            return; // ban do su kien chua mo khu nao: khong vao (truoc: NullPointerException)
        }
        player.region = rg.id;
        player.map = this;
        player.mapID = this.mapId;
        rg.playerJoin(player);
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
    protected void removeGem(GemItem pt, int country) {
        this.gemItem.get(0).remove(pt);
    }

    @Override
    public void removePlayer(int country, Char p) {
        this.ALL_REGION.get((short)p.region).removePlayer(0, p);
    }

    @Override
    public void removeMonster(int idMons, int country, int region) {
        try {
            this.ALL_REGION.get(region).removeMonster(country, idMons);
        } catch (Exception var5) {
        }
    }

    @Override
    public void sendAllPlayer(Message m, int country, int region) {
        try {
            Vector<Char> players = this.getAllPlayer(country, region);

            for (int i = 0; i < players.size(); i++) {
                players.get(i).sendMessage(m);
            }

            m.cleanup();
        } catch (Exception var6) {
        }
    }

    @Override
    public void sendAllPlayer(Message m, int country) {
        try {
            for (int j = 0; j < this.V_ALL_REGION.size(); j++) {
                Vector<Char> players = this.V_ALL_REGION.get(j).getAllPlayer(0);

                for (int i = 0; i < players.size(); i++) {
                    players.elementAt(i).sendMessage(m);
                }
            }

            m.cleanup();
        } catch (Exception var6) {
        }
    }

    @Override
    public void removeArena(int idParty) {
        try {
            RegionVantienTran rg = this.ALL_REGION.remove((short)idParty);
            this.V_ALL_REGION.remove(rg);
        } catch (Exception var3) {
        }
    }

    @Override
    public void removePLayer(Char p) {
        this.ALL_REGION.get((short)p.region).removePlayer(0, p);
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
    public Vector<Char> getAllPlayer(int inCountry, int region) {
        return this.ALL_REGION.get((short)region).getAllPlayer(0);
    }

    @Override
    public Monster getMonster(short id, int country, int region) {
        return this.ALL_REGION.get((short)region).getMonster(id, 0);
    }

    @Override
    protected void doAttackMonster(Char p, Message message) throws IOException {
        this.ALL_REGION.get((short)p.region).doAttackMonster(p, message);
    }

    @Override
    public void doAttackMultiTarget(Char p, Message message) {
        this.ALL_REGION.get((short)p.region).doAttackMultiTarget(p, message);
    }

    @Override
    protected void doAttackPlayer(Char p, Message message) {
        this.ALL_REGION.get((short)p.region).doAttackPlayer(p, message);
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
    public boolean isvanTienTran() {
        return true;
    }

    @Override
    public void doRequestMonterInfo(Char p, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = dis.readShort();
        Monster monster = this.getMonster(id, p.inCountry, p.region);
        if (monster != null) {
            Message m = new Message(7);
            m.dos.writeShort(monster.id);
            m.dos.writeByte(monster.getType());
            m.dos.writeShort(monster.x);
            m.dos.writeShort(monster.y);
            m.dos.writeInt(monster.hp);
            m.dos.writeByte(monster.level);
            m.dos.writeByte(monster.he);
            m.dos.writeInt(monster.maxhp);
            m.dos.writeInt(monster.getTimeReborn());
            p.sendMessage(m);
        }
    }

    @Override
    public synchronized RegionVantienTran getRegionJoinMapVanTienTran(Char p) {
        if (p.lvDetail.lv < 50) {
            return null;
        } else {
            for (int i = 0; i < this.V_ALL_REGION.size(); i++) {
                RegionVantienTran rg = this.V_ALL_REGION.get(i);
                if (!rg.isFull() && !rg.isFinish() || p.isAdmin && !rg.isFinish()) {
                    return rg;
                }
            }

            return null;
        }
    }

    @Override
    public void setDieAll(Char player) {
        if (player.isAdmin) {
            System.out.println("SET DIEALL " + player.region);
            this.ALL_REGION.get((short)player.region).setdie();
        }
    }

    @Override
    public Vector<LiveActor> getAllNearActor(Char from, int cat, int sl, LiveActor attacker) {
        return this.ALL_REGION.get((short)from.region).getAllNearActor(from, cat, sl, attacker);
    }
}
