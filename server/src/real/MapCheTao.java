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
 * Che tao va sua chua vat pham: nguoi choi tao item, tao vu khi/giap/loan,
 * sua thuoc tinh, dong dau (seal), khoa item, them thuoc tinh bot.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 */
// Thanh vien static cua Map ma nhom nay dung khong dinh danh khi con nam trong Map.
import static real.Map.ALL_CHAR_VIP;
import static real.Map.ID_THE_MUA_BAN;
import static real.Map.MAX_VIP1;
import static real.Map.MAX_VIP2;
import static real.Map.VIP;
import static real.Map.doAddItemIsNotWearing;
import static real.Map.gemTemplate;
import static real.Map.isModelClothes;
import static real.Map.isWeapone;
import static real.Map.isWearingAnimal;
import static real.Map.itemTemplates;
import static real.Map.okChedo;
import static real.Map.r;

class MapCheTao {

    static void doRequestItemInfo(Map map, Char p, Message message) throws IOException {
        try {
            DataInputStream dis = message.dis;
            short id = dis.readShort();
            short idChar = p.id;

            try {
                idChar = dis.readShort();
            } catch (Exception var15) {
            }

            Char pp = null;
            if (idChar == p.id) {
                pp = p;
            } else {
                pp = map.getChar(idChar);
            }

            boolean isWearing = false;
            if (pp != null) {
                Item[] wItems = pp.getArrayCharWearing();
                Item e = null;

                for (int i = 0; i < wItems.length; i++) {
                    Item e1 = wItems[i];
                    if (e1 != null && e1.id == id) {
                        e = e1;
                        isWearing = true;
                        break;
                    }
                }

                if (e == null) {
                    for (Item e1 : pp.iItems) {
                        if (e1.id == id) {
                            e = e1;
                            break;
                        }
                    }
                }

                if (e == null) {
                    for (Item e1x : pp.tItems) {
                        if (e1x.id == id) {
                            e = e1x;
                            break;
                        }
                    }
                }

                if (e == null) {
                    for (Item e1xx : pp.rItems) {
                        if (e1xx.id == id) {
                            e = e1xx;
                            break;
                        }
                    }
                }

                if (e == null) {
                    for (Item e1xxx : pp.bItem) {
                        if (e1xxx.id == id) {
                            e = e1xxx;
                            break;
                        }
                    }
                }

                if (e == null) {
                    for (Item e1xxxx : pp.awItems) {
                        if (e1xxxx.id == id) {
                            e = e1xxxx;
                            break;
                        }
                    }
                }

                if (e != null) {
                    Message m = new Message(21);
                    m.dos.writeShort(e.id);
                    m.dos.writeShort(e.durable);
                    m.dos.writeByte(e.clazz);
                    m.dos.writeShort(e.getMinuteLoan(true));
                    Vector<InfoItemAttribute> allAtb = e.getInfoAtbItem();
                    m.dos.writeByte(allAtb.size());

                    for (byte j = 0; j < allAtb.size(); j++) {
                        InfoItemAttribute info = (InfoItemAttribute) allAtb.get(j);
                        int x = info.value;
                        if (isWearing) {
                            if (info.id == 66) {
                                x *= p.getXSetLan(0);
                                x += info.value * p.getPCXbangSetDocLuaLan() / 100;
                            } else if (info.id == 67) {
                                x *= p.getXDocLan(0);
                                x += info.value * p.getPCXbangSetDocLuaLan() / 100;
                            } else if (info.id == 68) {
                                x *= p.getXBangLan(0);
                                x += info.value * p.getPCXbangSetDocLuaLan() / 100;
                            } else if (info.id == 89) {
                                x *= p.getXLuaLan(0);
                                x += info.value * p.getPCXbangSetDocLuaLan() / 100;
                            }
                        }

                        m.dos.writeByte(info.id);
                        m.dos.writeShort(x);
                    }

                    m.dos.writeByte(e.plus);
                    m.dos.writeByte(e.lock);
                    p.sendMessage(m);
                    m.cleanup();
                    if (p.id != pp.id) {
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                    }
                }
            }
        } catch (Exception var16) {
            var16.printStackTrace();
        }
    }

    static void doUserLockItem(Map map, Char player, Message message) {
        try {
            Item item = map.getItem(player, message);
            if (item == null) {
                return;
            }

            if (item.isSelling) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang bán.", ""));
                return;
            }

            if (!item.charSeal.toLowerCase().equals(player.charname.toLowerCase())) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thay đổi", ""));
                return;
            }

            if (!map.enoughMaterial(
                    player,
                    item.getType(),
                    isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                    item.magic_physic,
                    3,
                    player.infoItemCreate.materialUse
            )) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                return;
            }

            int n = 2;
            int[][] material = new int[n][];
            material[1] = new int[6];
            boolean fail = false;

            for (int i = 0; i < 6; i++) {
                material[1][i] = message.dis.readByte();
                if (i < 3 && material[1][i] != 0 && !fail) {
                    fail = true;
                }
            }

            if (fail) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể dùng nguyên liệu cấp 4 trở lên.", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return;
            }

            if (!map.enoughMaterial(
                    player,
                    item.getType(),
                    isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                    item.magic_physic,
                    material[0],
                    material[1],
                    3,
                    player.infoItemCreate.materialUse
            )) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                return;
            }

            item.lockItem(material[1]);
            item.lock = 1;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            player.sendMessage(MessageCreator.createServerAlertMessage("Khoá thành công.", ""));
            String gemUse = MapEpNgoc.deleteGemItem(map, 
                    player, CreateItemTemplate.getIDMAterial(item.getType(), item.magic_physic, 3), material, player.infoItemCreate.materialUse
            );
            Database.instance
                    .saveOrtherLog(
                            "tob_log_other_item", player.charname, gemUse + "_" + item.getTemplate().name + "_" + item.getAttribute() + "|" + item.getDBInfo(), "lock"
                    );
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.infoItemCreate = null;
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    static void doUserMakeSeal(Map map, Char player, Message message) {
        try {
            try {
                Item item = map.getItem(player, message);
                if (item == null) {
                    return;
                }

                if (item.isSelling) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang bán.", ""));
                    return;
                }

                if (item.charSeal.toLowerCase().equals(player.charname.toLowerCase())) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể đóng dấu lại", ""));
                    return;
                }

                if (!map.enoughMaterial(
                        player,
                        item.getType(),
                        isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                        item.magic_physic,
                        5,
                        player.infoItemCreate.materialUse
                )) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                    return;
                }

                int n = 2;
                int[][] material = new int[n][];
                material[1] = new int[6];
                boolean fail = false;

                for (int i = 0; i < 6; i++) {
                    material[1][i] = message.dis.readByte();
                    if (i < 3 && material[1][i] != 0 && !fail) {
                        fail = true;
                    }
                }

                if (fail) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể dùng nguyên liệu cấp 4 trở lên.", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                if (!map.enoughMaterial(
                        player,
                        item.getType(),
                        isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                        item.magic_physic,
                        material[0],
                        material[1],
                        5,
                        player.infoItemCreate.materialUse
                )) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                item.charSeal = player.charname;
                player.sendMessage(MessageCreator.createServerAlertMessage("Thay đổi thành công", ""));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                String gemUse = MapEpNgoc.deleteGemItem(map, 
                        player, CreateItemTemplate.getIDMAterial(item.getType(), item.magic_physic, 5), material, player.infoItemCreate.materialUse
                );
                Database.instance
                        .saveOrtherLog(
                                "tob_log_other_item", player.charname, gemUse + "_" + item.getTemplate().name + "_" + item.getAttribute() + "|" + item.getDBInfo(), "seal"
                        );
                player.infoItemCreate = null;
            } catch (Exception var8) {
            }
        } catch (Exception var9) {
        }
    }

    static void doUserRepairAtt(Map map, Char player, Message message) {
        try {
            try {
                Item item = map.getItem(player, message);
                if (item == null) {
                    return;
                }

                if (item.isSelling) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thực hiện khi vật phẩm đang bán.", ""));
                    return;
                }

                if (!item.charSeal.toLowerCase().equals(player.charname.toLowerCase()) || item.lock == 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể thay đổi", ""));
                    System.out.println("KHONG THE THAY DOI 1");
                    return;
                }

                if (!map.enoughMaterial(
                        player,
                        item.getType(),
                        isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                        item.magic_physic,
                        4,
                        player.infoItemCreate.materialUse
                )) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu", ""));
                    return;
                }

                int n = 2;
                int[][] material = new int[n][];
                material[1] = new int[6];
                boolean fail = false;

                for (int i = 0; i < 6; i++) {
                    material[1][i] = message.dis.readByte();
                    if (i < 3 && material[1][i] != 0 && !fail) {
                        fail = true;
                    }
                }

                if (fail) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể dùng nguyên liệu cấp 4 trở lên.", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                if (!map.enoughMaterial(
                        player,
                        item.getType(),
                        isWeapone(item.getType()) ? (item.level - 21) / 5 : (item.level - 20) / 5,
                        item.magic_physic,
                        material[0],
                        material[1],
                        4,
                        player.infoItemCreate.materialUse
                )) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ nguyên liệu2", ""));
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    return;
                }

                item.initAtt(material[1]);
                player.sendMessage(MessageCreator.createServerAlertMessage("Thay đổi thành công", ""));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                String gemUse = MapEpNgoc.deleteGemItem(map, 
                        player, CreateItemTemplate.getIDMAterial(item.getType(), item.magic_physic, 4), material, player.infoItemCreate.materialUse
                );
                Database.instance
                        .saveOrtherLog(
                                "tob_log_other_item", player.charname, gemUse + "_" + item.getTemplate().name + "_" + item.getAttribute() + "|" + item.getDBInfo(), "ratt"
                        );
                player.infoItemCreate = null;
            } catch (Exception var8) {
            }
        } catch (Exception var9) {
        }
    }

    static void doUserCreateItem(Map map, Char player, Message message) {
  
        if (!player.isCorrectPass && !player.passWord.equals("")) {
            player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
        } else {
            try {
                if (player.getxu() < 5000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có ít nhất 5000xu trong hành trang.", ""));
                    return;
                }

                byte option = message.dis.readByte();
              
                if (option == 1) {
                    map.doUserUpLevelItem(player, message);
                    player.subXu(5000L, true, "map 17");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    return;
                }

                if (option == 2) {
                    map.doUserUpLevelItemSpecial(player, message);
                    player.subXu(5000L, true, "map 18");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    return;
                }

                if (option == 3) {
                    doUserLockItem(map, player, message);
                    player.subXu(5000L, true, "map 19");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    return;
                }

                if (option == 4) {
                    doUserRepairAtt(map, player, message);
                    player.subXu(5000L, true, "map 20");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    return;
                }

                if (option == 5) {
                    doUserMakeSeal(map, player, message);
                    player.subXu(5000L, true, "map 21");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    return;
                }

                if (option == 6) {
                    map.doChangeLevelSkill(player, message);
                    player.subXu(5000L, true, "map 22");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    return;
                }

                if (option >= 10) {
                    map.doSendInfoMaterial2Client(player, message, option % 10);
                    return;
                }

                if (player.infoItemCreate == null) {
                    return;
                }

                map.doCreateNewItem(
                        player,
                        message,
                        player.infoItemCreate.idTemplate,
                        player.infoItemCreate.gender,
                        player.infoItemCreate.charClass,
                        player.infoItemCreate.typeItem,
                        player.infoItemCreate.level,
                        player.infoItemCreate.magic_physic,
                        0,
                        0,
                        null,
                        false
                );
                player.subXu(5000L, true, "map 23");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }
    }

    static Item createItemWeapone(Map map, Char player, int type, int idTemplate, int charClass, int level, int plusItem) {
        Item item = null;
        int[] templateBasic = new int[]{
            83, 84, 85, 174, 175, 176, 177, 178, 179, 180, 181, 598, 599, 893, // kiem
            90, 91, 92, 182, 183, 184, 185, 186, 187, 188, 189, 600, 601, 894, //dao
            97, 98, 99, 190, 191, 192, 193, 194, 195, 196, 197, 602, 603, 895, // but
            104, 105, 106, 198, 199, 200, 201, 202, 203, 204, 205, 604, 605, 896,// bua
            111, 112, 113, 206, 207, 208, 209, 210, 211, 212, 213, 606, 607, 897 // cung
        };
        int[] pcItem = new int[]{10, 20, 30, 40};
        ItemTemplates mTemplate = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(Integer.valueOf(idTemplate));
        ItemTemplates subTemplate = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(Integer.valueOf(templateBasic[charClass * 14 + (level - 21) / 5]));
        short[] att = new short[33];
        int[] plus = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};
        int bonusPercent = level > 85 ? 25 : 10; // chỉ số vk 86
        for (int i = 0; i < 10; i++) {
            att[i] = (short) (subTemplate.atb[i] + subTemplate.atb[i] * bonusPercent / 100);
        }


        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 10; j++) {
                if (att[j] > 0) {
                    att[j] = (short) (att[j] + (j > 0 ? 2 : plus[i + 1]));
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            att[i] = (short) (att[i] + att[i] * pcItem[type] / 100);
        }

        item = new Item(mTemplate, false, mTemplate.clazz, mTemplate.clazz, idTemplate);
        if (plusItem > 0) {
            for (int i = 0; i < plusItem - 1; i++) {
                for (int jx = 0; jx < 10; jx++) {
                    att[jx] = (short) (att[jx] + (jx > 0 ? 1 : plus[i + 1]));
                }
            }

            item.plus = (byte) (plusItem - 1);
        }

        item.atb = att;
        item.durable = subTemplate.durable;
        item.mDurable = (short) (item.durable * 10);
        return item;
    }

    static Item createItemArmor(Map map, Char player, int type, int idTemplate, int charClass, int level, int plusItem, int typeItem, int gender) {

        

        Item item = null;
        // thêm item template trong database để item mới được tạo sẽ có chỉ số = chỉ số của item đó + 20%
        // các item 85 mới copy i hịt item 80 vì chưa tìm được item có chỉ số cao hơn, <tự tìm chỉ số hoặc là if else đến chết để tạo chỉ số ngon hơn>
        int[][] templateBasic = new int[][]{
            {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 467, 468, 469, 470, 471, 472, 473, 474, 641, 642},//áo
            {35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 475, 476, 477, 478, 479, 480, 481, 482, 646, 649},//quần
            {61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 483, 484, 485, 486, 487, 488, 489, 490, 520, 520},//nón
            {83, 84, 85, 174, 175, 176, 177, 178, 179, 180, 181, 598, 599, 893},//kiếm
            {90, 91, 92, 182, 183, 184, 185, 186, 187, 188, 189, 600, 601, 894},//đao
            {97, 98, 99, 190, 191, 192, 193, 194, 195, 196, 197, 602, 603, 895},//but
            {104, 105, 106, 198, 199, 200, 201, 202, 203, 204, 205, 604, 605, 896},//bua
            {111, 112, 113, 206, 207, 208, 209, 210, 211, 212, 213, 606, 607, 897},//cung
            {117, 118, 119, 120, 121, 122, 123, 124, 125, 499, 500, 501, 502, 652},//nhan
            {129, 130, 131, 132, 133, 134, 135, 136, 137, 503, 504, 505, 506, 656},//day chuyen
            {141, 142, 143, 144, 145, 146, 147, 148, 149, 491, 492, 493, 494, 660},//giay
            {153, 154, 155, 156, 157, 158, 159, 160, 161, 495, 496, 497, 498, 664},//gang
            {165, 166, 167, 168, 169, 170, 171, 172, 173, 569, 570, 571, 572, 668}//ngoc
        };
        int[][] var10000 = new int[][]{
            {1, 40, 5},
            {1, 37, 5},
            {1, 35, 5},
            new int[0],
            new int[0],
            new int[0],
            new int[0],
            new int[0],
            {0, 30, 5},
            {0, 32, 5},
            {1, 37, 5},
            {1, 35, 5},
            {5, 6, 1}
        };

      // Logger2.DebugLogic("level chế item: " + level);

        int[] pcItem = new int[]{10, 20, 30, 40};
        ItemTemplates mTemplate = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(Integer.valueOf(idTemplate));
        ItemTemplates subTemplate = (ItemTemplates) ((Hashtable) itemTemplates.get(5))
                .get(Integer.valueOf(templateBasic[typeItem][typeItem < 3 ? (level - 20) * 2 / 5 + (gender == 1 ? 1 : 0) : (level - 20) / 5]));
        short[] att = new short[33];
        int[] plus = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 10, 20, 30, 40};

        for (int i = 0; i < 10; i++) {
            int baseValue = subTemplate.atb[i];
            // Tăng chỉ số cho level 85
            if (level >= 85) {
                baseValue = (int)(baseValue * 1.5); // Tăng 50% chỉ số cơ bản               
            }
            att[i] = (short) (baseValue + baseValue * (level >= 85 ? 15 : 10) / 100);
        }

        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 10; j++) {
                if (j != 4 && j != 2) {
                    int congthem = 8;
                    att[j] = (short) (att[j] + (j > 0 ? congthem : plus[i + 1] - 2));
                } else if (i % 2 == 0 && j == 2) {
                    att[j]++;
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            att[i] = (short) (att[i] + att[i] * pcItem[type] / 100);
        }

        item = new Item(mTemplate, false, charClass, charClass, idTemplate);
        if (plusItem > 0) {
            plus = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};

            for (int i = 0; i < plusItem - 1; i++) {
                for (int jx = 0; jx < 10; jx++) {
                    att[jx] = (short) (att[jx] + (jx > 0 ? 1 : plus[i + 1]));
                }
            }

            item.plus = (byte) (plusItem - 1);
        }

        item.atb = att;
        item.durable = subTemplate.durable;
        item.mDurable = (short) (item.durable * 10);
        return item;
    }

    static Item createItemLoan(Map map, Char player, int clazz, int lv) {
        Item item = null;
        int[][] idTemplate = new int[][]{{214, 223}, {224, 233}, {234, 243}, {244, 253}, {254, 263}};
        int id = 0;
        if (id < 26) {
            id = 0;
        } else {
            int[] mlv = new int[]{21, 26, 31, 36, 41, 46, 51, 56, 61, 66};

            for (int i = 0; i < 10; i++) {
                if (mlv[i] > lv) {
                    id = i - 1;
                    break;
                }
            }
        }

        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(0)).get(idTemplate[clazz][0] + id);
        Item newItem = new Item(template, false, template.clazz, template.clazz, idTemplate[clazz][0] + id);
        newItem.id = player.getIDItem();
        newItem.owner = player.charDBID;
        newItem.level = newItem.getTemplate().level;
        player.iItems.add(newItem);
        newItem.clazz = template.clazz;
        if (template.id > 213) {
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = template.ndayLoan;
        }

        player.sendMessage(MessageCreator.createMsgChat(player.id, "Chúc mừng, bạn vừa nhận được " + newItem.getTemplate().name));
        return item;
    }

    static void doAddNewAttBot(Map map, Char player, Message msg) {
        try {
            if (!player.map.isMapOffline) {
                return;
            }

            if (!player.isCorrectPass && !player.passWord.equals("")) {
                player.sendMessage(MessageCreator.createMsgInputText(player.id, 4, "Nhập mật khẩu", 0));
                return;
            }

            short idItem = msg.dis.readShort();
            short idMaterial = msg.dis.readShort();
            byte lock = msg.dis.readByte();
            short iddatiengiai = msg.dis.readShort();
            byte lockda = msg.dis.readByte();
            Item item = player.getItemFormVector(player.iItems, idItem);
            if (item != null) {
                if (!player.checkEnoughtGem(idMaterial, 1, lock) || gemTemplate[idMaterial].type != GemTemplate.TYPE_BOT) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không có đủ bột", ""));
                    return;
                }

                if (!player.checkEnoughtGem(iddatiengiai, 1, lockda) || gemTemplate[iddatiengiai].type != GemTemplate.TYPE_DA_TIEN_GIAI) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn không có đủ đá", ""));
                    return;
                }

                if (isModelClothes(item.getTemplate().atb[9]) || isWearingAnimal(item.getTemplate().type)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cộng thuộc tính cho vật phẩm này.", ""));
                    return;
                }

                if (item.colorName != Item.COLOR_GREEN || item.lock == 0 || item.colorName == Item.COLOR_GREEN && item.hangItem != 1 || item.level < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể cộng thuộc tính cho vật phẩm này.", ""));
                    return;
                }

                int[] result = item.doAddNewAttributeUseBot();
                player.sendMessage(MessageCreator.createMsgItemInfo(item));
                String info = "";

                for (int i = 0; i < result.length; i += 2) {
                    String name = InfoItemAttribute.getNameAtt(result[i]);
                    String value = InfoItemAttribute.getInfoValue(result[i], result[i + 1]);
                    info = info + name + " " + value + ",";
                }

                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã tạo được thuộc tính " + info, ""));
                player.delGem(idMaterial, 1, lock == 1);
                player.delGem(iddatiengiai, 1, lockda == 1);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                Database.instance.saveOrtherLog("", player.charname, item.getName() + "_" + item.getDBInfo() + "_" + item.getAttribute() + " > " + info, "themtt");
            }

            if (r.nextInt(100) < 1 && !player.isFullInventory()) {
                int[] idthe = new int[]{678, 679, 680};
                doAddItemIsNotWearing(player, idthe[r.nextInt(idthe.length)], 0);
            }

            if (Char.isSuKienTrungThul2016() && r.nextInt(100) < 40) {
                player.potions[136]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.getName(), "nhan dc nen khi tien giai", "nen");
            }

            if (Char.isSuKienHaloween2016() && r.nextInt(100) < 10) {
                player.potions[144]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.getName(), "nhan dc sao vang khi tien giai", "saovang");
            }

            Char.isSuKienHe2017();
            if (Char.isSuKienGioTo2016() && r.nextInt(100) < 50) {
                player.potions[117]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.getName(), "nhan dc dua hau khi add them thuoc tinh", "trungdua");
            }
        } catch (Exception var14) {
            var14.printStackTrace();
        }
    }

    static void doCreateItem(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        try {
            if (idMenu == 0 && idOptionMenu == 3) {
                if (UtilKPAH.getDayOfMonth() == Char.dayEndVip && UtilKPAH.getHour() >= Char.timeEndVip) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không thể nhận thành tích trong thời gian này", ""));
                    return;
                }

                int[] diemxai = new int[]{0, 300};
                int[] diemnap = new int[]{50, 99};
                int allDaigia = Database.instance.countCharVip(2);
                int allCaCung = Database.instance.countCharVip(1);
                if (player.vip == 0) {
                    if (player.diemxaiVip >= diemxai[1] && player.diemNapVip >= diemnap[1] && allDaigia < MAX_VIP2) {
                        player.vip = 2;
                        player.potions[100] = player.potions[100] + Char.soluong_loa_vip[player.vip];
                        player.buy_luot_van_tieu = (byte) (player.buy_luot_van_tieu + 2);
                        Item newItem = new ItemShop((ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(ID_THE_MUA_BAN), false, 0, 0, ID_THE_MUA_BAN);
                        newItem.id = player.getIDItem();
                        newItem.owner = player.charDBID;
                        newItem.level = newItem.getTemplate().level;
                        player.iItems.add(newItem);
                        newItem.identify = player.charDBID;
                        newItem.clazz = 0;
                        newItem.isItemShop = true;
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = 43200;
                        newItem.dateCreate = Char.getDayTime(0L);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(
                                MessageCreator.createMsgChat(player.id, "Nhận được 2 loa và 1 thẻ mua bán thời hạn 30 ngày và thêm 2 lượt mua thẻ vận tiêu.")
                        );
                        Database.instance.saveOrtherLog("", player.charname, "nhan loa vip va the mua ban 30 ngay va them 2 luot mua the van tieu", "loavip");
                        if (ALL_CHAR_VIP.get(player.charname) == null) {
                            Database.instance.doAddCharVip(player.charname, player.vip);
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                        } else {
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            Database.instance.doUpdateCharVip(player.charname, player.vip);
                        }
                    } else if (player.diemxaiVip >= diemxai[0] && player.diemNapVip >= diemnap[0] && allCaCung < MAX_VIP1) {
                        player.vip = 1;
                        player.buy_luot_van_tieu++;
                        player.potions[100] = player.potions[100] + Char.soluong_loa_vip[player.vip];
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được 1 loa và thêm 1 lượt mua thẻ vận tiêu."));
                        Database.instance.saveOrtherLog("", player.charname, "nhan loa vip, them 1 luot mua the van tieu", "loavip");
                        if (ALL_CHAR_VIP.get(player.charname) == null) {
                            Database.instance.doAddCharVip(player.charname, player.vip);
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                        } else {
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            Database.instance.doUpdateCharVip(player.charname, player.vip);
                        }
                    } else {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, VIP[1] + " và " + VIP[2] + " đã hết slot hoặc không đủ điều kiện"));
                    }
                } else if (player.vip == 1) {
                    if (player.diemxaiVip >= diemxai[1] && player.diemNapVip >= diemnap[1] && allDaigia < MAX_VIP2) {
                        player.vip = 2;
                        player.buy_luot_van_tieu = (byte) (player.buy_luot_van_tieu + 2);
                        player.potions[100] = player.potions[100] + Char.soluong_loa_vip[player.vip];
                        Item newItem = new ItemShop((ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(ID_THE_MUA_BAN), false, 0, 0, ID_THE_MUA_BAN);
                        newItem.id = player.getIDItem();
                        newItem.owner = player.charDBID;
                        newItem.level = newItem.getTemplate().level;
                        player.iItems.add(newItem);
                        newItem.identify = player.charDBID;
                        newItem.clazz = 0;
                        newItem.isItemShop = true;
                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = 43200;
                        newItem.dateCreate = Char.getDayTime(0L);
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                        player.sendMessage(
                                MessageCreator.createMsgChat(player.id, "Nhận được 2 loa và 1 thẻ mua bán thời hạn 30 ngày và thêm 2 lượt mua thẻ vận tiêu.")
                        );
                        Database.instance.saveOrtherLog("", player.charname, "nhan loa vip va the mua ban 30 ngay, them 2 luot mua the van tieu", "loavip");
                        if (ALL_CHAR_VIP.get(player.charname) == null) {
                            Database.instance.doAddCharVip(player.charname, player.vip);
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                        } else {
                            ALL_CHAR_VIP.put(player.charname, Integer.valueOf(player.vip));
                            Database.instance.doUpdateCharVip(player.charname, player.vip);
                        }
                    } else {
                        player.sendMessage(MessageCreator.createMsgChat(player.id, VIP[2] + " đã hết slot hoặc không đủ điều kiện"));
                    }
                }

                if (player.vip > 0) {
                    player.calculateAttrib();
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đạt thành tích " + VIP[player.vip], ""));
                    player.sendMessage(MessageCreator.createMainCharInfoMessage(player));
                    player.sendToNearPlayer(MessageCreator.createCharInfo(player));
                }

                return;
            }

            if (!okChedo) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì.", ""));
                return;
            }

            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đã đầy"));
                return;
            }

            if (idMenu == 0) {
                player.infoItemCreate = null;
                if (idOptionMenu == 0) {
                    String[] menu = new String[]{"Kiếm", "Đại đao", "Bút", "Búa", "Cung"};
                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
                } else if (idOptionMenu == 1) {
                    String[] menu = new String[]{"Trang bị ma pháp", "Trang bị vât lý"};
                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 3));
                } else if (idOptionMenu == 2) {
                    String[] menu = new String[]{
                        "Cấp độ 30", "Cấp độ 35", "Cấp độ 40", "Cấp độ 45", "Cấp độ 50", "Cấp độ 55", "Cấp độ 60", "Cấp độ 65", "Cấp độ 70", "Cấp độ 75", "Cấp độ 80"
                    };
                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 7));
                }
            } else if (idMenu == 1) {
                player.infoItemCreate = new InfoItemCreate();
                String nameItem = "";
                switch (idOptionMenu) {
                    case 0:
                        nameItem = "Kiếm cấp độ ";
                        player.infoItemCreate.charClass = 0;
                        player.infoItemCreate.typeItem = 3;
                        break;
                    case 1:
                        nameItem = "Đại đao cấp độ ";
                        player.infoItemCreate.charClass = 1;
                        player.infoItemCreate.typeItem = 4;
                        break;
                    case 2:
                        nameItem = "Bút cấp độ ";
                        player.infoItemCreate.charClass = 2;
                        player.infoItemCreate.typeItem = 5;
                        break;
                    case 3:
                        nameItem = "Búa cấp độ ";
                        player.infoItemCreate.charClass = 3;
                        player.infoItemCreate.typeItem = 6;
                        break;
                    case 4:
                        nameItem = "Cung cấp độ ";
                        player.infoItemCreate.charClass = 4;
                        player.infoItemCreate.typeItem = 7;
                }

                if (!nameItem.equals("")) {
                    String[] menu = new String[13];

                    for (int i = 0; i < 13; i++) {
                        byte[] id = CreateItemTemplate.getIDMAterial(player.infoItemCreate.typeItem, player.infoItemCreate.magic_physic, 0);
                        Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(i, 0);
                        String infoMaterial = "";

                        for (int j = 0; j < soluong.size(); j++) {
                            infoMaterial = infoMaterial + CreateItemTemplate.nameMaterial[id[j]] + " " + soluong.get(j) + " ";
                        }

                        menu[i] = nameItem + (i * 5 + 21) + "(" + infoMaterial + ")";
                    }

                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
                }
            } else if (idMenu == 2) {
                player.infoItemCreate.level = (byte) (idOptionMenu * 5 + 21);
                player.infoItemCreate.idOptionMenu = (short) idOptionMenu;
                player.infoItemCreate.gender = 0;
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}, idNpc, 11));
            } else if (idMenu == 3) {
                player.infoItemCreate = new InfoItemCreate();
                String[] menu = new String[]{"Kiếm khách", "Chiến binh", "Pháp sư", "Đấu sĩ", "Cung thủ"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 4));
                player.infoItemCreate.magic_physic = (byte) idOptionMenu;
            } else if (idMenu == 4) {
                player.infoItemCreate.charClass = (byte) idOptionMenu;
                String[] className = new String[]{"Kiếm khách", "Chiến binh", "Pháp sư", "Đấu sĩ", "Cung thủ"};
                String[] menu = new String[]{
                    "áo " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Quần " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Nón " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Giày " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Găng " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Nhẫn " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Dây chuyền " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu],
                    "Ngọc " + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic] + " " + className[idOptionMenu]
                };
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 5));
            } else if (idMenu == 5) {
                if (idOptionMenu < 3) {
                    player.infoItemCreate.typeItem = (byte) idOptionMenu;
                    String[] menu = new String[28];

                    for (int i = 0; i < 28; i++) {
                        byte[] id = CreateItemTemplate.getIDMAterial(player.infoItemCreate.typeItem, player.infoItemCreate.magic_physic, 0);
                        Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(i / 2, 0);
                        String infoMaterial = "";

                        for (int j = 0; j < soluong.size(); j++) {
                            infoMaterial = infoMaterial + CreateItemTemplate.nameMaterial[id[j]] + " " + soluong.get(j) + " ";
                        }

                        menu[i] = InfoItemCreate.nameAmor[idOptionMenu]
                                + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic]
                                + " "
                                + (i % 2 == 0 ? "Nữ" : "Nam")
                                + " lv"
                                + (20 + i / 2 * 5)
                                + "("
                                + infoMaterial
                                + ")";
                    }

                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 6));
                } else {
                    if (idOptionMenu < 5) {
                        player.infoItemCreate.typeItem = (byte) (idOptionMenu + 7);
                    } else if (idOptionMenu < 7) {
                        player.infoItemCreate.typeItem = (byte) (idOptionMenu + 3);
                    } else {
                        player.infoItemCreate.typeItem = 12;
                    }

                    String[] menu = new String[15];

                    for (int i = 0; i < 15; i++) {
                        byte[] id = CreateItemTemplate.getIDMAterial(player.infoItemCreate.typeItem, player.infoItemCreate.magic_physic, 0);
                        Vector<Byte> soluong = CreateItemTemplate.getTotalMaterial(i, 0);
                        String infoMaterial = "";

                        for (int j = 0; j < soluong.size(); j++) {
                            infoMaterial = infoMaterial + CreateItemTemplate.nameMaterial[id[j]] + " " + soluong.get(j) + " ";
                        }

                        menu[i] = InfoItemCreate.nameAmor[idOptionMenu]
                                + InfoItemCreate.nameMagic_physic[player.infoItemCreate.magic_physic]
                                + " "
                                + " lv"
                                + (20 + i * 5)
                                + "("
                                + infoMaterial
                                + ")";
                    }

                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 6));
                }
            } else if (idMenu == 6) {
                if (player.infoItemCreate.typeItem < 3) {
                    player.infoItemCreate.gender = (byte) (idOptionMenu % 2 == 0 ? 2 : 1);
                    player.infoItemCreate.level = (byte) (idOptionMenu / 2 * 5 + 20);
                } else {
                    player.infoItemCreate.level = (byte) (idOptionMenu * 5 + 20);
                }

                player.infoItemCreate.idOptionMenu = (short) (player.infoItemCreate.typeItem < 3 ? idOptionMenu / 2 : idOptionMenu);
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}, idNpc, 11));
            } else if (idMenu == 7) {
                player.itChangeColor = new ItemChangeColor(idOptionMenu);
                String[] menu = new String[]{"Ma pháp", "Vật lý"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 8));
            } else if (idMenu == 8) {
                player.itChangeColor.magic_physic = (byte) idOptionMenu;
                String[] menu = new String[]{"Trang bị xanh dương", "Trang bị đỏ", "Trang bị hoàn mỹ"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 9));
            } else if (idMenu == 9) {
                if (player.itChangeColor == null) {
                    return;
                }

                player.itChangeColor.idColor = (byte) idOptionMenu;
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"nguyên liệu thường", "Nguyên liệu khóa", "Nguyên liệu tổng hợp"}, idNpc, 10));
            } else if (idMenu == 10) {
                player.itChangeColor.materialUse = (byte) idOptionMenu;
                ItemChangeColor.createMsgChangColorItem(player, player.itChangeColor.level, player.itChangeColor.idColor);
            } else if (idMenu == 11) {
                player.infoItemCreate.materialUse = (byte) idOptionMenu;
                player.sendMessage(
                        MessageCreator.createMsgCreateItemClothing(
                                (ItemTemplates) ((Hashtable) itemTemplates.get(5))
                                        .get(
                                                new Integer(
                                                        player.infoItemCreate.idTemplate = map.getTemplateItemCreate(
                                                                player.infoItemCreate.level, player.infoItemCreate.typeItem, player.infoItemCreate.charClass, player.infoItemCreate.gender
                                                        )
                                                )
                                        ),
                                player.infoItemCreate.typeItem,
                                player.infoItemCreate.idOptionMenu,
                                player.infoItemCreate.magic_physic,
                                player.infoItemCreate.materialUse
                        )
                );
            }
        } catch (Exception var12) {
        }
    }

}
