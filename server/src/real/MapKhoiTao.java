package real;


import data.Animal;
import real.plugins.GiftCode;
import data.BossMatQuy;
import data.CharClan;
import data.CharCooking;
import data.CharInboxMessage;
import data.CharInfo;
import data.CharWeaponData;
import data.Cooking;
import data.CreateItemTemplate;
import data.DataGame;
import data.Database;
import data.EventGame;
import data.GameCanvas;
import data.GemItem;
import data.Goverment;
import data.InfoCharLienDau;
import data.InfoItemCreate;
import data.InfoQuestTemplate;
import data.ItemChangeColor;
import data.ItemInfo;
import data.ItemLuckyDraw;
import data.ItemQuest;
import data.MapLocation;
import data.Net;
import data.NewClan;
import data.OtherHandle;
import data.Pet;
import data.PetTemplate;
import data.SeedItem;
import data.SeedTemplate;
import data.Text;
import data.ThungGo;
import data.Weather;
import data.Xoso;
import io.Message;
import io.SessionManager;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import real.cmd.CreateCharHandler;
import real.cmd.LoginHandler;
import real.cmd.RequestNPCInfoHandler;
import server.TeamServer;
import util.Logger;
import data.NewClan;
import ThanKhi.ThoRenThanBi;

/**
 * Than constructor cua Map (476 dong): dung cau hinh ban do luc khoi tao -
 * kich thuoc, vung, quai, NPC, thoi tiet, gioi han nguoi choi.
 *
 * Constructor VAN CON trong Map voi dung chu ky cu, chi uy nhiem sang day:
 *     public Map(int id, ...) { MapKhoiTao.init(this, id, ...); }
 * Constructor goc KHONG co super()/this() nen chuyen duoc toan bo than.
 * Thu tu thuc thi giu nguyen: loi goi nam dung cho than cu.
 */
class MapKhoiTao {

    static void init(Map map, int id, int idXaphu, int magic_physic, int mapload, int nregion) {
        map.nRegion = (byte) nregion;
        if (!Map.openLog) {
            Map.MAX_MEMBER_ARENA = 10;
            ArenaData.MAX_WIN = 100;
        }

        Map.setTimeCake();
        map.potions.add(new Vector());
        map.potions.add(new Vector());
        map.potions.add(new Vector());
        map.ALL_EFFECT_INMAP.add(new Vector());
        map.ALL_EFFECT_INMAP.add(new Vector());
        map.ALL_EFFECT_INMAP.add(new Vector());
        map.itemquests.add(new Vector());
        map.itemquests.add(new Vector());
        map.itemquests.add(new Vector());
        map.items.add(new Vector());
        map.items.add(new Vector());
        map.items.add(new Vector());
        map.gemItem.add(new Vector());
        map.gemItem.add(new Vector());
        map.gemItem.add(new Vector());
        if (nregion == 0) {
            map.allPlayers.add(new Vector());
            map.allPlayers.add(new Vector());
            map.allPlayers.add(new Vector());
            map.allPlayerMessages.add(new Vector());
            map.allPlayerMessages.add(new Vector());
            map.allPlayerMessages.add(new Vector());
            map.monsters.add(new Hashtable());
            map.monsters.add(new Hashtable());
            map.monsters.add(new Hashtable());
            map.temp_monsters.add(new Hashtable());
            map.temp_monsters.add(new Hashtable());
            map.temp_monsters.add(new Hashtable());
            map.tempMonster.add(new Vector());
            map.tempMonster.add(new Vector());
            map.tempMonster.add(new Vector());
            map.tempRemoveMonster.add(new Vector());
            map.tempRemoveMonster.add(new Vector());
            map.tempRemoveMonster.add(new Vector());
        } else {
            for (int i = 0; i < nregion; i++) {
                map.ALL_REGION.add(new Region(map, i));
            }
        }

        map.mapId = id;
        map.mapIDLoadMap = mapload;
        short loadmap = (short) mapload;
        if (loadmap == -1) {
            loadmap = (short) map.mapId;
        }

        map.idXaphu = (byte) idXaphu;
        if (map.mapId != -1) {
            map.loadMap("map/map" + loadmap, magic_physic);
        }

        FileInputStream fis = null;
        DataInputStream dis = null;
        FileInputStream fisMap = null;
        DataInputStream disMap = null;

        try {
            fis = new FileInputStream(loadmap < 110 ? "cMap/t.type" : (loadmap > 200 ? "cMap/t_thanh.type" : "cMap/t_hang.type"));
            dis = new DataInputStream(fis);
            map.typeOfTile = new int[dis.available()];

            for (short i = 0; i < map.typeOfTile.length; i++) {
                map.typeOfTile[i] = dis.read();
            }

            fisMap = new FileInputStream("cMap/" + loadmap);
            disMap = new DataInputStream(fisMap);
            map.w = disMap.read();
            map.h = disMap.read();
            map.map = new short[map.w * map.h];
            map.type = new int[map.w * map.h];

            for (int i = 0; i < map.w * map.h; i++) {
                try {
                    map.map[i] = (short) disMap.read();
                    if (map.map[i] == 254) {
                        map.map[i] = 255;
                    }

                    if (map.map[i] != 255 && map.map[i] != -1) {
                        map.type[i] = map.typeOfTile[map.map[i]];
                    }
                } catch (Exception var23) {
                    var23.printStackTrace();
                }
            }

            map.loadTileMap(null, disMap, loadmap);
            if (map.mapId == 80 || map.mapId == 1901 || map.mapId == 1902 || map.mapId == 1903 || map.mapId == 1904) {
                if (Map.botChuyenlanhtho == null) {
                    Map.botChuyenlanhtho = new Char(null);
                    Map.botChuyenlanhtho.setInfoChar("chuyenlanhtho", -32, 2, 0, map, 512, 416, -3057, 25, 51, 77);
                    Map.botChuyenlanhtho.id = RealController.intance.idGen.getID(0, "Tao bot");
                }

                map.addPlayer(2, Map.botChuyenlanhtho, 0);
                CharManager.instance.put(Map.botChuyenlanhtho);
            }

            if (map.mapId == 0
                    || map.mapId == 301
                    || map.mapId == 302
                    || map.mapId == 303
                    || map.mapId == 304
                    || map.mapId == 70
                    || map.mapId == 1701
                    || map.mapId == 1702
                    || map.mapId == 1703
                    || map.mapId == 1704) {
                for (short i = 0; i < Map.npcVilage.size(); i++) {
                    map.addPlayer(0, (Char) Map.npcVilage.get(i), 0);
                    map.addPlayer(1, (Char) Map.npcVilage.get(i), 0);
                }

                if (Char.isSuKienTet2017()) 
                {
                }
            }

            if (map.mapId == 17) {
                Char bot = new Char(null);
                bot.setInfoChar("Xa phu", -9, 2, 0, map, 1584, 864, (map.mapId + 2003) * -1, 25, 51, 77);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
            }

            if (map.mapIDLoadMap == 10) {
                Char bot = new Char(null);
                bot.setInfoChar("xa phu", -26, 2, 0, map, 928, 2000, (map.mapId + 2050) * -1, 25, 51, 77);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
            }

            if (map.mapId == 201) {
                Char bot = new Char(null);
                bot.setInfoChar("ta pho thong", -3, 2, 0, map, 1040, 160, (map.mapId + 2018) * -1, 25, 51, 77);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
                bot = new Char(null);
                bot.setInfoChar("ta thong linh", -4, 2, 0, map, 288, 720, (map.mapId + 2020) * -1, 25, 51, 77);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
                bot = new Char(null);
                bot.setInfoChar("huu thong linh", -5, 2, 0, map, 976, 720, (map.mapId + 2022) * -1, 25, 51, 77);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
                bot = new Char(null);
                bot.setInfoChar("tran thong linh", -29, 2, 0, map, 640, 240, (map.mapId + 2036) * -1, 291, 315, 339);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
                if (!TeamServer.isServerIndo()) {
                    bot = new Char(null);
                    bot.setInfoChar("hao duyen", -6, 2, 0, map, 240, 160, (map.mapId + 2024) * -1, 25, 51, 77);
                    bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                    bot.wModel.idModel = 19;
                    map.addPlayer(0, bot, 0);
                    map.addPlayer(1, bot, 0);
                    CharManager.instance.put(bot);
                    bot = new Char(null);
                    bot.setInfoChar("tho san", -16, 2, 0, map, 400, 160, (map.mapId + 2026) * -1, 25, 51, 77);
                    bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                    map.addPlayer(0, bot, 0);
                    map.addPlayer(1, bot, 0);
                    CharManager.instance.put(bot);
                    bot = new Char(null);
                    bot.setInfoChar("le quan", -25, 2, 0, map, 384, 240, (map.mapId + 2060) * -1, 25, 51, 77);
                    bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                    map.addPlayer(0, bot, 0);
                    map.addPlayer(1, bot, 0);
                    CharManager.instance.put(bot);
                    bot = new Char(null);
                    bot.setInfoChar("ky nang bang", -13, 2, 0, map, 1104, 208, (map.mapId + 2014) * -1, 269, 293, 317);
                    bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                    map.addPlayer(0, bot, 0);
                    map.addPlayer(1, bot, 0);
                    CharManager.instance.put(bot);
                    bot = new Char(null);
                    bot.setInfoChar("nguyet lao", -27, 2, 0, map, 560, 160, (map.mapId + 2034) * -1, 25, 51, 77);
                    bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                    map.addPlayer(0, bot, 0);
                    map.addPlayer(1, bot, 0);
                    CharManager.instance.put(bot);
                    bot = new Char(null);
                    bot.setInfoChar("dau truong", -31, 2, 0, map, 720, 160, -3058, 25, 51, 77);
                    bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                    map.addPlayer(0, bot, 0);
                    map.addPlayer(1, bot, 0);
                    CharManager.instance.put(bot);
                }

                bot = new Char(null);
                bot.setInfoChar("market", -37, 2, 0, map, 496, 272, -3062, 269, 293, 317);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
            }

            if (map.mapId == 114 || map.mapId == 2581 || map.mapId == 2582 || map.mapId == 2583 || map.mapId == 2584) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(0), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(0), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(0), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(0), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(0), 0);
                }
            }

            if (map.mapId == 21 || map.mapId == 721 || map.mapId == 722 || map.mapId == 723 || map.mapId == 724) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(1), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(1), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(1), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(1), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(1), 0);
                }
            }

            if (map.mapId == 22 || map.mapId == 741 || map.mapId == 742 || map.mapId == 743 || map.mapId == 744) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(2), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(2), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(2), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(2), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(2), 0);
                }
            }

            if (map.mapIDLoadMap == 11) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(3), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(3), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(3), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(3), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(3), 0);
                }
            }

            if (map.mapIDLoadMap == 13) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(4), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(4), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(4), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(4), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(4), 0);
                }
            }

            if (map.mapIDLoadMap == 14) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(5), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(5), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(5), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(5), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(5), 0);
                }
            }

            if (map.mapIDLoadMap == 15) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(6), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(6), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(6), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(6), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(6), 0);
                }
            }

            if (map.mapIDLoadMap == 18) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(7), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(7), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(7), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(7), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(7), 0);
                }
            }

            if (map.mapIDLoadMap == 19) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(8), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(8), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(8), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(8), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(8), 0);
                }
            }

            if (map.mapIDLoadMap == 20) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(9), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(9), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(9), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(9), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(9), 0);
                }
            }

            if (map.mapIDLoadMap == 23) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(10), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(10), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(10), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(10), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(10), 0);
                }
            }

            if (map.mapIDLoadMap == 24) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(11), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(11), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(11), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(11), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(11), 0);
                }
            }

            if (map.mapIDLoadMap == 10) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(12), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(12), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(12), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(12), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(12), 0);
                }
            }

            if (map.mapIDLoadMap == 114) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(13), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(13), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(13), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(13), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(13), 0);
                }
            }

            if (map.mapIDLoadMap == 25) {
                if (nregion > 0) {
                    for (int i = 0; i < nregion; i++) {
                        map.addPlayer(0, (Char) Map.npcHoatieu.get(15), i);
                        map.addPlayer(1, (Char) Map.npcHoatieu.get(15), i);
                    }
                } else {
                    map.addPlayer(0, (Char) Map.npcHoatieu.get(15), 0);
                    map.addPlayer(1, (Char) Map.npcHoatieu.get(15), 0);
                    map.addPlayer(2, (Char) Map.npcHoatieu.get(15), 0);
                }
            }

            if (map.mapIDLoadMap == 41) {
                Char charCopy = new CharCopy(null);
                byte[][] idquanao = new byte[][]{{2, 28, 54}, {1, 27, 53}};
                int gd = Map.r.nextInt(2);
                charCopy.charDBID = -10000;
                charCopy.setInfoChar("copy1", -1, gd + 1, 0, map, 1088, 1008, -10001, idquanao[gd][0], idquanao[gd][1], -1);
                charCopy.headStyle = (byte) (gd == 0 ? 10 : 9);
                charCopy.id = RealController.intance.idGen.getID(0, "Tao bot");
                charCopy.charClass = 0;
                ((CharCopy) charCopy).isCharChienTruong = true;
                charCopy.lastLV = 60;
                charCopy.x = 144;
                charCopy.y = 96;
                charCopy.setXtoYto(144, 96);
                CharManager.instance.put(charCopy);
                map.addPlayer(0, charCopy, 0);
                map.addPlayer(1, charCopy, 0);
                map.addPlayer(2, charCopy, 0);
            }

            if (map.mapId == 201) {
                for (int i = 0; i < 3; i++) {
                    Monster m = new Monster(map, (MonsterTemplate) Map.monsterTemplates.get(83), 1008, 1769, (byte) i);
                    m.id = 0;
                    ((Hashtable) map.monsters.get(i)).put(m.id, m);
                    m.isDead = true;
                }
            }

            if (map.mapId == 118 && !TeamServer.isServerIndo()) {
                Char bot = new Char(null);
                bot.setInfoChar("dich tram", -51, 2, 0, map, 880, 320, -32078, 25, 51, 77);
                bot.id = RealController.intance.idGen.getID(0, "Tao bot");
                map.addPlayer(0, bot, 0);
                map.addPlayer(1, bot, 0);
                CharManager.instance.put(bot);
            }

            if (map.mapId == 5 || map.mapId == 401 || map.mapId == 402 || map.mapId == 403 || map.mapId == 404) {
                map.addPlayer(0, (Char) Map.npcHoatieu.get(14), 0);
                map.addPlayer(1, (Char) Map.npcHoatieu.get(14), 0);
            }

            map.startWindVilage();
            map.startSandVilage();
            map.startLeafVilage();
            map.doStartThreadUpdatePlayer();
        } catch (Exception var24) {
            System.out.println("LOI KHOI TAO MAP");
            var24.printStackTrace();
        } finally {
            try {
                fis.close();
                dis.close();
                fisMap.close();
                disMap.close();
            } catch (Exception var22) {
            }
        }

        new Thread(map).start();
    }

}
