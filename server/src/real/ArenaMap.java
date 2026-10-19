/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import io.Message;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class ArenaMap extends Map {
    public static Hashtable<Integer, ArenaData> allArena = new Hashtable<>();
    public Vector<Integer> idRemoveArena = new Vector<>();
    static Char bot = null;
    Vector<Char> players = new Vector<>();

    public ArenaMap(int id, int idXaphu, int magic_physic, int mapload) {
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

        this.mapId = id;
        new Thread(this).start();
    }

    public ArenaMap(int id) {
        super(id);
    }

    public ArenaMap() {
    }

    @Override
    public void doUseGoldKey(Char player) {
        player.potions[142]++;
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Không thể sử dụng sao vàng tại vị trí này", ""));
    }

    @Override
    public void loadMap(String filename, int magic_physic) {
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.currentThread().setName("MAP " + this.mapId);

                while (RealController.savingChar) {
                    Thread.sleep(100L);
                }

                this.map_run_state = 0;
                long l1 = System.currentTimeMillis();
                if (l1 - this.lastTimeUpdateMap > DELAY_UPDATE_MAP) {
                    this.lastTimeUpdateMap = l1;
                    this.update();
                }

                l1 = System.currentTimeMillis();

                while (System.currentTimeMillis() - l1 < 500L && this.playerMessages.size() != 0) {
                    PlayerMessage pm = this.playerMessages.remove(0);
                    if (!pm.player.exit) {
                        this.processMessage(pm.player, pm.message);
                    }
                }

                if (this.playerMessages.size() != 0) {
                    synchronized (this.LOCK) {
                        this.LOCK.wait(1L);
                    }
                } else {
                    this.map_run_state = 1;
                    synchronized (this.LOCK) {
                        this.LOCK.wait(DELAY_UPDATE_MAP);
                    }
                }
            } catch (Exception var6) {
                var6.printStackTrace();
                System.out.println("LOI TRONG HAM RUN MAP OFFLINE ");
            }
        }
    }

    @Override
    public void addPlayerMessage(Char p, Message message) {
        ArenaData pri = allArena.get(p.idArena);
        if (pri != null) {
            pri.addPlayerMessage(p, message);
        }
    }

    @Override
    public void update() {
        for (ArenaData pri : allArena.values()) {
            if (pri.isFinish) {
                this.idRemoveArena.add(pri.idArena);
            }
        }

        for (int i = 0; i < this.idRemoveArena.size(); i++) {
            allArena.remove(this.idRemoveArena.get(i));
        }

        this.idRemoveArena.removeAllElements();
    }

    public void doAddArena(ArenaData arena) {
        arena.parrent = this;
        allArena.put(arena.idArena, arena);
    }

    public void doAddArena(TeamArena wd) {
        ArenaData pri = new ArenaData(wd.typeArena, this);
        pri.idArena = wd.id;
        allArena.put(pri.idArena, pri);
    }

    @Override
    public void doStartArena() {
    }

    @Override
    public void doStart(Char p, int type) {
        ArenaData pri = new ArenaData(type, this);
        pri.charMaster = p.getName();
        pri.idArena = p.idWedding;
        allArena.put(pri.idArena, pri);
    }

    @Override
    public void doAddMonster(Char p) {
        ArenaData pri = allArena.get(p.idArena);
        if (pri != null) {
            pri.doAddMonster(p);
        }
    }

    @Override
    protected void doAttackPlayer(Char p, Message message) {
        if (!p.isHoangSo()) {
            ArenaData pri = allArena.get(p.idArena);
            if (pri != null) {
                pri.doAttackPlayer(p, message);
            }
        }
    }

    @Override
    protected void doAttackMonster(Char p, Message message) throws IOException {
        ArenaData pri = allArena.get(p.idArena);
        if (pri != null) {
            pri.doAttackMonster(p, message);
        }
    }

    @Override
    public void doAttackMultiTarget(Char p, Message message) {
        ArenaData pri = allArena.get(p.idArena);
        if (pri != null) {
            pri.doAttackMultiMonster(p, message);
        }
    }

    @Override
    public void playerJoin(Char player) {
        ArenaData pri = allArena.get(player.idArena);
        if (pri != null) {
            pri.playerJoin(player);
            player.map = this;
            player.mapID = this.mapId;
        }
    }

    public static ArenaData getArenaData(Char p) {
        return allArena.get(p.idArena);
    }

    @Override
    public void playerExit(Char player) {
        ArenaData pri = allArena.get(player.idArena);
        if (pri != null) {
            pri.playerExit(player);
        }
    }

    @Override
    public void removeArena(int idArena) {
        allArena.remove(idArena);
    }

    @Override
    public ArenaData getArena(int id) {
        return allArena.get(id);
    }

    @Override
    public boolean isPublicMap() {
        return true;
    }

    @Override
    public Vector<Char> getAllPlayer(int inCountry, int region) {
        ArenaData pri = allArena.get(inCountry);
        return pri != null ? pri.players : this.players;
    }

    @Override
    public void removePlayer(int country, Char p) {
    }

    @Override
    public Monster getMonster(short id, int idArena, int region) {
        ArenaData pri = allArena.get(idArena);
        return pri != null ? pri.getMonster(id, idArena) : null;
    }

    @Override
    public void sendDynamicEff(Char player) {
        ArenaData pri = allArena.get(player.idArena);
        if (pri != null) {
            pri.sendDynamicEff();
        }
    }

    @Override
    public void doSendKiller(Char player) {
    }

    @Override
    public void onActorDie(LiveActor ac) {
        ArenaData pri = allArena.get(((Char)ac).idArena);
        if (pri != null) {
            pri.onActorDie(ac);
        }
    }

    @Override
    public boolean isMapTrain() {
        return false;
    }
}
