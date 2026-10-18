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
import java.util.Vector;

public class DungeonMap extends Map {
    Vector<Char> players = new Vector<>();

    public DungeonMap() {
    }

    @Override
    public boolean isMapTrain() {
        return false;
    }

    public DungeonMap(int id, int idXaphu, int magic_physic, int mapload) {
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
            Char bot = new Char(null);
            bot.setInfoChar("lequan", -23, 2, 0, this, 688, 448, (this.mapId + 2027) * -1, 25, 51, 77);
            bot.id = RealController.intance.idGen.getID(0, "Tao bot");
            this.players.add(bot);
            CharManager.instance.put(bot);
        } catch (Exception var13) {
        }

        new Thread(this).start();
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
    public void removePlayer(int country, Char p) {
        this.players.remove(p);
    }

    @Override
    public void removePLayer(Char player) {
        for (int i = 0; i < this.players.size(); i++) {
            try {
                if (this.players.get(i).getName().toLowerCase().equals(player.getName().toLowerCase())) {
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
                this.removePlayer(player.inCountry, player);
                player.sendToNearPlayer(m);
            } catch (Exception var5) {
                System.out.println("LOI REMOVE PLAYER KHOI MAP DUGEON");
            }
        }

        m.cleanup();
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
    public void update() {
        int size = this.players.size();
        int k = 0;

        while (k < size) {
            try {
                Char p = this.players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    this.players.remove(k);
                    CharManager.instance.remove(p);
                } else if ((!p.map.equals(this) || p.mapID != this.mapId) && p.isBot == -1) {
                    this.players.remove(k);
                } else if (CharManager.instance.getCharByCharName(p.getName()) == null && p.isBot == -1) {
                    this.players.remove(k);
                    CharManager.instance.remove(p);
                } else {
                    if (p.map == this) {
                        p.update();
                    }

                    k++;
                }
            } catch (Exception var4) {
                break;
            }
        }
    }

    @Override
    public void addPlayerMessage(Char p, Message message) {
        this.playerMessages.add(new PlayerMessage(p, message));
    }

    @Override
    public boolean isPublicMap() {
        return true;
    }
}
