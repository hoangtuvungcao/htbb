/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import io.Message;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import server.TeamServer;

public class Dun extends Map {
    private int deltaXP = 2;

    public Dun(int id) {
        super(id);
    }

    public Dun(int id, int idXaphu, int magic_physic, int mapload, int nregion) {
        super(id, idXaphu, magic_physic, mapload, nregion);
    }

    @Override
    public boolean isMapTrain() {
        return false;
    }

    @Override
    public void doRequestMonterInfo(Char p, Message message) throws IOException {
        DataInputStream dis = message.dis;
        short id = (short)dis.readUnsignedShort();
        Monster monster;
        if (p.party.userParty.size() == 0) {
            monster = p.monsters.get(id);
        } else {
            Char pLeader = CharManager.instance.getByCharID(p.party.idMaster);
            monster = pLeader.monsters.get(id);
        }

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
            p.sendMessage(m);
            m.cleanup();
        }
    }

    @Override
    public void update() {
        boolean exitBoard = false;
        Vector<Char> players = this.getAllPlayer(1, 0);
        int size = players.size();
        int k = 0;

        while (k < size) {
            try {
                Char p = players.get(k);
                if ((p.map == null || p.getSession() == null || p.mapID == -1) && p.isBot == -1) {
                    players.remove(k);
                    CharManager.instance.remove(p);
                } else if (p.map.equals(this) && p.mapID == this.mapId) {
                    if (CharManager.instance.getCharByCharName(p.getName()) == null && p.isBot == -1) {
                        players.remove(k);
                        CharManager.instance.remove(p);
                    } else {
                        if (p.map == this) {
                            p.update();
                            if ((this.mapId == 107 || this.mapId == 2441 || this.mapId == 2442 || this.mapId == 2443 || this.mapId == 2444)
                                && System.currentTimeMillis() - TeamServer.timeOutBoard >= TeamServer.timeDownBoard
                                && TeamServer.timeOutBoard > 0L) {
                                exitBoard = true;
                            }
                        }

                        k++;
                    }
                } else {
                    players.remove(k);
                }
            } catch (Exception var8) {
                break;
            }
        }

        if (exitBoard) {
            size = players.size();

            while (size > 0) {
                try {
                    Char p = players.remove(0);
                    size--;
                    if (p != null) {
                        int id = this.mapId % 2440;
                        if (this.mapId == 107) {
                            id = 10;
                        } else {
                            id += 500;
                        }

                        this.move2Map(p, 62 + random(5), 122 + random(5), id, p.inCountry);
                        if (p.potions[20] > 0) {
                            p.potions[20]--;
                        }
                    }
                } catch (Exception var7) {
                }
            }

            TeamServer.timeOutBoard = 0L;
        }

        this.deletePotionAndItemOnGround(1);
    }
}
