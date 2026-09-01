package real;


import data.CharCooking;
import data.CharInfo;
import data.Database;
import data.ItemLuckyDraw;
import io.Message;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;

// Thanh vien static cua Map ma cac method nay dung khong dinh danh khi con
// nam trong Map. Dung static import (giong real/doBuySpecialItem.java) de
// khong phai sua mot dong nao trong than method da tach.
import static real.Map.addXpCharEvent;
import static real.Map.doCreateHuyetLinhThao;
import static real.Map.gemTemplate;
import static real.Map.isTrungHoaKyLan;
import static real.Map.itemTemplates;
import static real.Map.pos_fire_work;
import static real.Map.r;
import static real.Map.sendAllCharServer;
import static real.Map.timeOpenHoaKyLan;
import static real.Map.totalDotPhao;
import static real.Map.x3ExpDotPhao;
import static real.Map.ID_MAT_NA;
import static real.Map.ID_VU_KHI_THOI_TRANG;
import static real.Map.createtrungPet;
import static real.Map.doCreateMatNa;
import static real.Map.doCreateVuKhiThoiTrang;
import static real.Map.doBuyModelClothe;
import static real.Map.doCreateHoVeLenhByLevel;
import static real.Map.getShopTemplate;
import static real.Map.idNPC49;
import static real.Map.isMapLang;
import static real.Map.isTimeBuyChoi;
import static real.Map.openLog;
import static real.Map.totalChoiBan;

/**
 * NPC su kien theo mua: Tet, Noel, Trung Thu, Gio To, Vu Lan, Halloween,
 * Vao He, World Cup + ban phao hoa.
 *
 * Tach nguyen van tu real/Map.java (3020 dong). Deu la method PRIVATE cua Map
 * nen khong the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 *
 * LUU Y: 14 method trong file nay hien KHONG duoc goi tu dau ca (noi dung su
 * kien cac nam truoc chua duoc noi lai). Giu nguyen de nam sau con dung lai,
 * khong xoa. Xem MapSuKien.java trong lich su git neu can doi chieu.
 */
class MapSuKien {

    static void doBanPhaoHoaGioto(Map map, Char p) {
        try {
            addXpCharEvent(p, 20000L, false, "doBanPhaoHoa");
            int typeParty = 1;
            Vector<Char> players = map.getAllPlayer(p.inCountry, p.region);

            for (int i = 0; i < players.size(); i++) {
                Char player = (Char) players.get(i);

                try {
                    if (player.id == p.id || player.getSession().firmWare != 0) {
                        for (int j = 0; j < pos_fire_work[typeParty].length; j += 2) {
                            Message m = MessageCreator.createMsgDynamicEff(
                                    p, pos_fire_work[typeParty][j] * 16, pos_fire_work[typeParty][j + 1] * 16, 0, 0, 0, 0, 45, 5
                            );
                            player.sendMessage(m);
                        }
                    }
                } catch (Exception var16) {
                }
            }

            int[] per = new int[]{10, 10, 500, 50, 25, 500, 500, 200, 250, 70, 70, 70, 70, 70};
            int[] idgift = new int[]{GemTemplate.LKD_30, GemTemplate.LKD_35, 7, 13, 8, 93, 95, 84, -1, -2, -2, -2, -2};
            int[] typegift = new int[]{6, 6, 4, 4, 4, 4, 4, 4, -1, -2, -2, -2, -2};
            int[] soluongmax = new int[]{1, 1, 1, 1, 1, 50, 50, 1, 1, 10000, 11000, 12000, 20000};
            int[] soluongmin = new int[]{1, 1, 1, 1, 1, 100, 100, 1, 1, 10000, 11000, 12000, 20000};
            String info = "Chúc mừng bạn nhận được ";
            int index = Char.getIndexRandom(per);
            if (typegift[index] == -2) {
                p.addXu((long) soluongmax[index], "map 7");
                info = info + soluongmax[index] + " xu";
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else if (typegift[index] == -1) {
                int[] t = new int[]{1440, 2880, 4320};
                Item newItem = ItemLuckyDraw.createItemCoat(p, r.nextInt(2), false, t[r.nextInt(t.length)]);
                p.iItems.add(newItem);
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else if (typegift[index] == -3) {
                int idGem = GemTemplate.ID_MATERIAL_LOW[1][r.nextInt(GemTemplate.ID_MATERIAL_LOW[1].length)];
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.doAddGemItem(idGem, soluong, true);
                info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
            } else if (typegift[index] == -4) {
                int idGem = GemTemplate.ID_MATERIAL_LOW[2][r.nextInt(GemTemplate.ID_MATERIAL_LOW[2].length)];
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.doAddGemItem(idGem, soluong, false);
                info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
            } else if (typegift[index] == -5) {
                p.addLuongLock(soluongmax[index]);
                info = info + soluongmax[index] + " lượng khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == 6) {
                boolean lock = r.nextInt(100) < 50;
                p.doAddGemItem(idgift[index], soluongmax[index], lock);
                info = info + soluongmax[index] + " " + gemTemplate[idgift[index]].name;
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.potions[idgift[index]] = p.potions[idgift[index]] + soluong;
                info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                if (idgift[index] == 9) {
                    try {
                        sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được nhân sâm khi bắn pháo hoa"));
                    } catch (IOException var15) {
                    }
                }
            } else if (typegift[index] == 3) {
                if (p.isFullInventory()) {
                    int xu = 5000 + r.nextInt(100000);
                    p.addXu((long) xu, "map 8");
                    info = info + xu + " xu";
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                } else {
                    ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index]);
                    Item newItem = null;
                    newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                    newItem.id = p.getIDItem();
                    newItem.owner = p.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    newItem.identify = p.charDBID;
                    int time = r.nextInt(48) + 1;
                    int pc = r.nextInt(100);
                    if (pc < 1) {
                        time = 360;
                    } else if (pc < 10) {
                        time = 240;
                    } else if (pc < 50) {
                        time = 72 + r.nextInt(49);
                    }

                    newItem.timeLoan = System.currentTimeMillis();
                    newItem.minuteExist = time * 60;
                    if (newItem.isVukhiThoiTrang()) {
                        newItem.createAttributeVuKhiThoiTrang();
                    }

                    newItem.dateCreate = Char.getDayTime(0L);
                    p.iItems.add(newItem);
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                    info = info + newItem.getName();
                }

                p.sendMessage(MessageCreator.createMsgChat(p.id, info));
            }

            Database.instance.saveOrtherLog("", p.charname, info, "dotphao");
        } catch (Exception var17) {
        }
    }

    static void doBanPhaoHoaTet2020(Map map, Char p) {
        try {
            p.banphaohoa++;
            totalDotPhao++;
            if (totalDotPhao >= 15000) {
                x3ExpDotPhao = 1;
                totalDotPhao = 0;
            }

            Database.instance.addCharEventTet(p);
            addXpCharEvent(p, 100000L, false, "doBanPhaoHoa");
            int typeParty = 1;
            Vector<Char> players = map.getAllPlayer(p.inCountry, p.region);

            for (int i = 0; i < players.size(); i++) {
                Char player = (Char) players.get(i);

                try {
                    if (player.id == p.id || player.getSession().firmWare != 0) {
                        for (int j = 0; j < pos_fire_work[typeParty].length; j += 2) {
                            Message m = MessageCreator.createMsgDynamicEff(
                                    p, pos_fire_work[typeParty][j] * 16, pos_fire_work[typeParty][j + 1] * 16, 0, 0, 0, 0, 45, 5
                            );
                            player.sendMessage(m);
                        }
                    }
                } catch (Exception var17) {
                }
            }

            int[] per = new int[]{500, 500, 500, 1, 10, 10, 10, 10, 10, 10};
            int[] idgift = new int[]{19, 100, 58, 118, 76, 715, 716, 717, 718, 719};
            int[] typegift = new int[]{4, 4, 4, 4, 4, 3, 3, 3, 3, 3};
            int[] soluongmax = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            String info = "Chúc mừng bạn nhận được ";
            int index = Char.getIndexRandom(per);
            if (typegift[index] == -1) {
                p.addXu((long) soluongmax[index], "map 9");
                info = info + soluongmax[index] + " xu";
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            } else if (typegift[index] == -2) {
                int idGem = GemTemplate.ID_MATERIAL_LOW[0][r.nextInt(GemTemplate.ID_MATERIAL_LOW[0].length)];
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.doAddGemItem(idGem, soluong, true);
                info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == -3) {
                int idGem = GemTemplate.ID_MATERIAL_LOW[1][r.nextInt(GemTemplate.ID_MATERIAL_LOW[1].length)];
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.doAddGemItem(idGem, soluong, true);
                info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == -4) {
                int idGem = GemTemplate.ID_MATERIAL_LOW[2][r.nextInt(GemTemplate.ID_MATERIAL_LOW[2].length)];
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.doAddGemItem(idGem, soluong, false);
                info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == -5) {
                p.addLuongLock(soluongmax[index]);
                info = info + soluongmax[index] + " lượng khoá";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (typegift[index] == 6) {
                boolean lock = r.nextInt(100) < 50;
                p.doAddGemItem(idgift[index], soluongmax[index], lock);
                info = info + soluongmax[index] + " " + gemTemplate[idgift[index]].name;
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_30) {
                    try {
                        sendAllCharServer(
                                -1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " khi bắn pháo hoa")
                        );
                    } catch (IOException var16) {
                    }
                }
            } else if (typegift[index] == 4) {
                int soluong = soluongmin[index];
                if (soluongmin[index] < soluongmax[index]) {
                    soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                }

                p.potions[idgift[index]] = p.potions[idgift[index]] + soluong;
                info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                if (idgift[index] == 9) {
                    try {
                        sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được nhân sâm khi bắn pháo hoa"));
                    } catch (IOException var15) {
                    }
                }
            } else if (typegift[index] == 3) {
                if (p.isFullInventory()) {
                    int xu = 5000 + r.nextInt(100000);
                    p.addXu((long) xu, "map 10");
                    info = info + xu + " xu";
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                } else {
                    ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index]);
                    Item newItem = null;
                    newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                    newItem.id = p.getIDItem();
                    newItem.owner = p.charDBID;
                    newItem.level = newItem.getTemplate().level;
                    newItem.identify = p.charDBID;
                    int time = r.nextInt(48) + 1;
                    int pc = r.nextInt(100);
                    if (pc < 1) {
                        time = 360;
                    } else if (pc < 10) {
                        time = 240;
                    } else if (pc < 50) {
                        time = 72 + r.nextInt(49);
                    }

                    newItem.timeLoan = System.currentTimeMillis();
                    newItem.minuteExist = time * 60;
                    if (newItem.isVukhiThoiTrang()) {
                        newItem.createAttributeVuKhiThoiTrang();
                    }

                    newItem.dateCreate = Char.getDayTime(0L);
                    p.iItems.add(newItem);
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                    info = info + newItem.getName();
                }

                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            }

            Database.instance.saveOrtherLog("", p.charname, info, "dotphao");
            Database.instance.saveCharAuto(p);
        } catch (Exception var18) {
        }
    }

    static void doBanPhaoHoa(Map map, Char p) {
        try {
            p.banphaohoa++;
            totalDotPhao++;
            if (totalDotPhao >= 15000) {
                x3ExpDotPhao = 1;
                totalDotPhao = 0;
            }

            addXpCharEvent(p, 300000L, false, "doBanPhaoHoa");
            int typeParty = 1;
            Vector<Char> players = map.getAllPlayer(p.inCountry, p.region);

            for (int i = 0; i < players.size(); i++) {
                Char player = (Char) players.get(i);

                try {
                    if (player.id == p.id || player.getSession().firmWare != 0) {
                        for (int j = 0; j < pos_fire_work[typeParty].length; j += 2) {
                            Message m = MessageCreator.createMsgDynamicEff(
                                    p, pos_fire_work[typeParty][j] * 16, pos_fire_work[typeParty][j + 1] * 16, 0, 0, 0, 0, 45, 5
                            );
                            player.sendMessage(m);
                        }
                    }
                } catch (Exception var20) {
                }
            }

            int[] per = new int[]{500, 500, 500, 500, 500, 500, 500, 500, 500, 0, 0, 0, 0, 0, 200, 200, 200, 100, 80, 50, 100, 100, 100, 100, 100, 200, 200};
            int[] idgift = new int[]{
                123,
                35,
                119,
                117,
                120,
                GemTemplate.BOT_TRANG,
                GemTemplate.BOT_XANH,
                GemTemplate.BOT_DO,
                GemTemplate.BOT_XANH_LA,
                -1,
                -1,
                -1,
                -1,
                -1,
                -2,
                -3,
                -4,
                -5,
                -5,
                -5,
                715,
                716,
                717,
                718,
                719,
                118,
                76
            };
            int[] typegift = new int[]{4, 4, 4, 4, 4, 6, 6, 6, 6, -1, -1, -1, -1, -1, -2, -3, -4, -5, -5, -5, 3, 3, 3, 3, 3, 4, 4};
            int[] soluongmax = new int[]{1, 1, 1, 1, 1, 40, 32, 20, 8, 20000, 30000, 50000, 100000, 250000, 3, 3, 3, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1};
            int[] soluongmin = new int[]{1, 1, 1, 1, 1, 40, 32, 20, 8, 20000, 30000, 50000, 100000, 250000, 1, 1, 1, 1, 2, 3, 1, 1, 1, 1, 1, 1, 1};
            String info = "Chúc mừng bạn nhận được ";
            if (!isTrungHoaKyLan && Char.getDayTime(0L).compareTo(timeOpenHoaKyLan[TeamServer.server]) >= 0) {
                isTrungHoaKyLan = true;
                p.potions[71]++;
                info = info + "1 " + LoginHandler.PORTION_NAME[71];
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));

                try {
                    sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được Hoả kỳ lân khi bắn pháo hoa"));
                } catch (IOException var19) {
                }
            } else {
                int index = Char.getIndexRandom(per);
                if (typegift[index] == -1) {
                    p.addXu((long) soluongmax[index], "map 12");
                    info = info + soluongmax[index] + " xu";
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                } else if (typegift[index] == -2) {
                    int idGem = GemTemplate.ID_MATERIAL_LOW[0][r.nextInt(GemTemplate.ID_MATERIAL_LOW[0].length)];
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }

                    p.doAddGemItem(idGem, soluong, true);
                    info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                } else if (typegift[index] == -3) {
                    int idGem = GemTemplate.ID_MATERIAL_LOW[1][r.nextInt(GemTemplate.ID_MATERIAL_LOW[1].length)];
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }

                    p.doAddGemItem(idGem, soluong, true);
                    info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                } else if (typegift[index] == -4) {
                    int idGem = GemTemplate.ID_MATERIAL_LOW[2][r.nextInt(GemTemplate.ID_MATERIAL_LOW[2].length)];
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }

                    p.doAddGemItem(idGem, soluong, false);
                    info = info + soluongmax[index] + " " + gemTemplate[idGem].name + " khoá";
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                } else if (typegift[index] == -5) {
                    p.addLuongLock(soluongmax[index]);
                    info = info + soluongmax[index] + " lượng khoá";
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                } else if (typegift[index] == 6) {
                    boolean lock = r.nextInt(100) < 50;
                    p.doAddGemItem(idgift[index], soluongmax[index], lock);
                    info = info + soluongmax[index] + " " + gemTemplate[idgift[index]].name;
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_30) {
                        try {
                            sendAllCharServer(
                                    -1,
                                    MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " khi bắn pháo hoa")
                            );
                        } catch (IOException var18) {
                        }
                    }
                } else if (typegift[index] == 4) {
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }

                    p.potions[idgift[index]] = p.potions[idgift[index]] + soluong;
                    info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    if (idgift[index] == 9) {
                        try {
                            sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được nhân sâm khi bắn pháo hoa"));
                        } catch (IOException var17) {
                        }
                    }
                } else if (typegift[index] == 3) {
                    if (p.isFullInventory()) {
                        int xu = 5000 + r.nextInt(100000);
                        p.addXu((long) xu, "map 13");
                        info = info + xu + " xu";
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    } else {
                        ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index]);
                        Item newItem = null;
                        newItem = new Item(template, false, template.clazz, template.clazz, template.id);
                        newItem.id = p.getIDItem();
                        newItem.owner = p.charDBID;
                        newItem.level = newItem.getTemplate().level;
                        newItem.identify = p.charDBID;
                        int time = r.nextInt(48) + 1;
                        int pc = r.nextInt(100);
                        if (pc < 1) {
                            time = 360;
                        } else if (pc < 10) {
                            time = 240;
                        } else if (pc < 50) {
                            time = 72 + r.nextInt(49);
                        }

                        newItem.timeLoan = System.currentTimeMillis();
                        newItem.minuteExist = time * 60;
                        if (newItem.isVukhiThoiTrang()) {
                            newItem.createAttributeVuKhiThoiTrang();
                        }

                        newItem.dateCreate = Char.getDayTime(0L);
                        p.iItems.add(newItem);
                        p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                        info = info + newItem.getName();

                        try {
                            sendAllCharServer(
                                    -1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được " + newItem.getName() + " khi bắn pháo hoa")
                            );
                        } catch (IOException var16) {
                        }
                    }

                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                }
            }

            Database.instance.saveOrtherLog("", p.charname, info, "dotphao");
        } catch (Exception var21) {
        }
    }

    static void domenuWorldcup2017(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
        } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
            int[] sl = new int[]{0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 10, 10, 10, 10};
            int[] price = new int[]{0, 50, 70, 100, 120};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[72] < slCaudoi[idMenu]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ nguyên liệu", ""));
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[72] = player.potions[72] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            boolean istuhondan = false;
            int[] pc = new int[]{0, 0, 0, 5, 10, 20, 30};
            if (r.nextInt(100) < pc[idMenu]) {
                player.potions[83]++;
                istuhondan = true;
            }

            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được "
                            + sl[idMenu]
                            + " "
                            + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                            + " khóa"
                            + (istuhondan ? " và 1 Tụ hồn đan" : ""),
                            ""
                    )
            );
            Database.instance
                    .saveOrtherLog(
                            "",
                            player.charname,
                            sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + (istuhondan ? " và 1 Tụ hồn đan" : ""),
                            "redeem"
                    );
        } else if (idMenu == 5) {
            int[] slx = new int[]{3, 5, 7, 10};
            int[] slCaudoix = new int[]{10, 10, 10, 10};
            int[] pricex = new int[]{50, 70, 100, 120};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoix[idOptionMenu] + " bt + " + pricex[idOptionMenu] + " lượng)";
                mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
        } else if (idMenu == 6) {
            player.sendMessage(MessageCreator.createMsgInputText(-32016 - idOptionMenu, 0, "Số pháo hoa", 1));
        }
    }

    static void domenuTet2017(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (!Char.isFinishAllSuKienTet2017()) {
            if (idMenu == 0) {
                if (idOptionMenu == 0) {
                    CharCooking c = (CharCooking) Char.nauBanhChung.listCharGift.get(player.charname);
                    if (c == null || c.nbanh == 0) {
                        return;
                    }

                    player.potions[151] = player.potions[151] + c.nbanh;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Database.instance.saveOrtherLog("", player.charname, "Nhan " + c.nbanh + " bánh chưng", "rbc");
                    c.nbanh = 0;
                    Database.instance.saveCooking(Char.nauBanhChung, 1);
                } else if (idOptionMenu == 1) {
                    CharCooking c = (CharCooking) Char.nauBanhTet.listCharGift.get(player.charname);
                    if (c == null || c.nbanh == 0) {
                        return;
                    }

                    player.potions[152] = player.potions[152] + c.nbanh;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Database.instance.saveOrtherLog("", player.charname, "Nhan " + c.nbanh + " bánh tet", "rbt");
                    c.nbanh = 0;
                    Database.instance.saveCooking(Char.nauBanhTet, 2);
                } else if (idOptionMenu == 2) {
                    if (player.potions[151] < 1 || player.potions[152] < 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh", ""));
                        return;
                    }

                    player.potions[151]--;
                    player.potions[152]--;
                    doDangBanhChung(map, player);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                } else if (idOptionMenu == 3) {
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
                } else if (idOptionMenu == 4) {
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Làm pháo (10tr xu)", "Làm pháo (6 lượng)"}, idNpc, 6));
                } else if (idOptionMenu == 5) {
                    if (Char.nauBanhChung.listCharCooking.get(player.charname) != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đăng ký rồi", ""));
                        return;
                    }

                    if (player.getLuong() < 5) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    player.subLuong(5L);
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Char.nauBanhChung.addCharCook(player);
                    Database.instance.saveCooking(Char.nauBanhChung, 1);
                    Database.instance.saveOrtherLog("", player.charname, "dang ky nau banh chung", "rnbc");
                    player.sendMessage(MessageCreator.createServerAlertMessage("Đăng ký thành công", ""));
                    Database.instance.saveLogThongKeChiTieu("DANG KY NAU BANH CHUNG", 0, 1, 5L, "luong");
                } else if (idOptionMenu == 6) {
                    if (Char.nauBanhTet.listCharCooking.get(player.charname) != null) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đăng ký rồi", ""));
                        return;
                    }

                    if (player.getxu() < 500000L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                        return;
                    }

                    player.subXu(500000L, true, "map 46");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Char.nauBanhTet.addCharCook(player);
                    Database.instance.saveCooking(Char.nauBanhTet, 2);
                    Database.instance.saveOrtherLog("", player.charname, "dang ky nau banh tet", "rnbt");
                    player.sendMessage(MessageCreator.createServerAlertMessage("Đăng ký thành công", ""));
                    Database.instance.saveLogThongKeChiTieu("DANG KY NAU BANH TET", 0, 1, 500000L, "xu");
                } else if (idOptionMenu == 7) {
                    Vector<CharInfo> top = Database.instance.getTopEvent();
                    String[] menu = new String[top.size()];

                    for (int i = 0; i < top.size(); i++) {
                        CharInfo c = (CharInfo) top.get(i);
                        menu[i] = c.name;
                    }

                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, -1));
                }
            } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
                int[] sl = new int[]{0, 3, 5, 7, 10};
                int[] slCaudoi = new int[]{0, 5, 5, 5, 5};
                int[] price = new int[]{0, 50, 70, 100, 120};
                if (player.getLuong() < price[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                if (player.potions[152] < slCaudoi[idMenu]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ nguyên liệu", ""));
                    return;
                }

                player.subLuong((long) price[idMenu]);
                player.potions[152] = player.potions[152] - slCaudoi[idMenu];
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                        )
                );
                Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
            } else if (idMenu == 5) {
                int[] slx = new int[]{3, 5, 7, 10};
                int[] slCaudoix = new int[]{5, 5, 5, 5};
                int[] pricex = new int[]{50, 70, 100, 120};
                String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                    String info = "(" + slCaudoix[idOptionMenu] + " bt + " + pricex[idOptionMenu] + " lượng)";
                    mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
            } else if (idMenu == 6) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016 - idOptionMenu, 0, "Số pháo hoa", 1));
            } else if (idMenu == 7) {
                return;
            }
        }
    }

    static void doDangBanhChung(Map map, Char p) {
        try {
            int[] per = new int[]{
                500, 500, 500, 120, 150, 100, 100, 100, 400, 400, 400, 150, 30, 15, 20, 200, 70, 70, 70, 0, 50, 70, 400, 400, 400, 400, 400, 400
            };
            int[] idgift = new int[]{
                114,
                113,
                112,
                35,
                100,
                7,
                13,
                8,
                GemTemplate.BOT_TRANG,
                GemTemplate.BOT_XANH,
                GemTemplate.BOT_DO,
                GemTemplate.BOT_XANH_LA,
                GemTemplate.DA_TIEN_GIAI,
                GemTemplate.LKD_35,
                GemTemplate.XUONG_C6,
                -1,
                -2,
                -3,
                -4,
                -5,
                -6,
                82,
                58,
                156,
                157,
                158,
                51,
                55
            };
            int[] typegift = new int[]{4, 4, 4, 4, 4, 4, 4, 4, 6, 6, 6, 6, 6, 6, 6, -1, -2, -3, -4, -5, -6, 4, 4, 4, 4, 4, 4, 4};
            int[] soluongmax = new int[]{3, 3, 3, 1, 10, 1, 1, 1, 100, 80, 30, 10, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2};
            int[] soluongmin = new int[]{3, 3, 3, 1, 10, 1, 1, 1, 100, 80, 30, 10, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            Hashtable<Integer, Integer> indexReceive = new Hashtable();
            String info = "Chúc mừng bạn nhận được ";
            int slg = r.nextInt(3) + 1;

            for (int i = 0; i < slg; i++) {
                int index = Char.getIndexRandom(per);
                int count = 0;

                while (count++ < 100 && indexReceive.get(index) != null) {
                    index = Char.getIndexRandom(per);
                }

                indexReceive.put(index, index);
                if (typegift[index] == -1) {
                    int[] t = new int[]{60, 120, 180, 300, 480, 720, 1440, 2160, 2880, 4320};
                    Item newItem = ItemLuckyDraw.createItemCoat(p, r.nextInt(2), false, t[r.nextInt(t.length)]);
                    p.iItems.add(newItem);
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                    info = info + newItem.getName() + " thời hạn " + newItem.minuteExist + " phút";
                    p.sendMessage(MessageCreator.createMsgChat(p.id, info));
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                } else if (typegift[index] == -2) {
                    p.createHeo((long) (r.nextInt(3) + 1), true);
                    info = info + " thú cưỡi Đương Khang";
                } else if (typegift[index] == -3) {
                    p.createNguaXuong((long) (r.nextInt(3) + 1), true);
                    info = info + " thú cưỡi Bạch Cốt Mã";
                } else if (typegift[index] == -4) {
                    p.doCreatePetBachThu((r.nextInt(2) + 2) * 24 * 60, true);
                    info = info + " pet Bạch Thử";
                } else if (typegift[index] == -5) {
                    if (map.doCreateHoVeLenh(p, 4320) != null) {
                        info = info + " Hộ vệ lệnh";
                        p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    } else {
                        addXpCharEvent(p, 50000L, false, "dangbanhchung");
                        info = info + "50k exp";
                    }
                } else if (typegift[index] == -6) {
                    doCreateHuyetLinhThao(p, 0);
                    info = info + " Huyết linh thảo";
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                } else if (typegift[index] == 6) {
                    boolean lock = r.nextInt(100) < 50;
                    p.doAddGemItem(idgift[index], soluongmax[index], lock);
                    info = info + soluongmax[index] + " " + gemTemplate[idgift[index]].name;
                    p.sendMessage(MessageCreator.createCharGemItem(p));
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    if (idgift[index] == GemTemplate.LKD_35 || idgift[index] == GemTemplate.LKD_30) {
                        try {
                            sendAllCharServer(
                                    -1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được " + gemTemplate[idgift[index]].name + " khi dâng bánh")
                            );
                        } catch (IOException var16) {
                        }
                    }

                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                } else if (typegift[index] == 4) {
                    int soluong = soluongmin[index];
                    if (soluongmin[index] < soluongmax[index]) {
                        soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
                    }

                    p.potions[idgift[index]] = p.potions[idgift[index]] + soluong;
                    info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    if (idgift[index] == 9) {
                        try {
                            sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(p.charname + " đã nhận được nhân sâm khi dâng bánh"));
                        } catch (IOException var15) {
                        }
                    }

                    p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                }
            }

            Database.instance.saveOrtherLog("", p.charname, info, "dangbanh");
        } catch (Exception var17) {
            var17.printStackTrace();
        }
    }

    static void doMenuNoel2016(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số kem", 1));
            } else if (idOptionMenu == 1) {
                if (player.getLuong() < 120) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 120 lượng mới có thể đổi được linh thú Tuần Lộc", ""));
                    return;
                }

                if (player.potions[146] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có kem", ""));
                    return;
                }

                player.subLuong(120L);
                player.potions[146] = player.potions[146] - 50;
                player.createTuanLoc(30L);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 2));
                Database.instance.saveOrtherLog("", player.charname, "da doi linh thu tuan loc", "tuanloc");
                Database.instance.saveLogThongKeChiTieu("TUAN LOC", 0, 1, 120L, "luong");
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
            } else if (idOptionMenu == 3) {
                player.sendMessage(MessageCreator.createMsgInputText(-32017, 0, "Số túi quà", 1));
            } else if (idOptionMenu == 4) {
                if (!player.isCharNam()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể đổi đồ nam"));
                    return;
                }

                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"1 Ngày", "2 Ngày", "3 Ngày", "4 Ngày", "5 Ngày", "6 Ngày", "7 Ngày"}, idNpc, 10));
            } else if (idOptionMenu == 5) {
                if (!player.isCharNu()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể đổi đồ nữ"));
                    return;
                }

                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"1 Ngày", "2 Ngày", "3 Ngày", "4 Ngày", "5 Ngày", "6 Ngày", "7 Ngày"}, idNpc, 11));
            } else if (idOptionMenu == 6) {
                for (int i = 0; i < GemTemplate.ID_NGOC_RONG.length; i++) {
                    if (player.listGemitemLock[GemTemplate.ID_NGOC_RONG[i]] <= 0) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngọc", ""));
                        return;
                    }
                }

                player.doDoiQuaDacBietNoel2020();
            } else if (idOptionMenu == 7) {
                if (player.lvDetail.lv < 20) {
                    return;
                }

                if (!Char.isDemNoel()) {
                    return;
                }

                if (player.receiveGiftEvent == 1) {
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đầy"));
                    return;
                }

                player.receiveGiftEvent = 1;
                player.createPoro(3L, true);
                Database.instance.saveLogThongKeChiTieu("DAI BACH THU", 0, 1, 1L, "nhanqua");
            }
        } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
            int[] sl = new int[]{0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 20, 30, 40, 50};
            int[] price = new int[]{0, 50, 70, 100, 120};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[146] < slCaudoi[idMenu]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ nguyên liệu", ""));
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[146] = player.potions[146] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
            Database.instance
                    .saveLogThongKeChiTieu(gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + "_" + sl[idMenu], 0, 1, (long) price[idMenu], "luong");
        } else if (idMenu == 5) {
            int[] slx = new int[]{3, 5, 7, 10};
            int[] slCaudoix = new int[]{20, 30, 40, 50};
            int[] pricex = new int[]{50, 70, 100, 120};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int ix = 0; ix < GemTemplate.ID_MATERIAL_LOW[5].length; ix++) {
                String info = "(" + slCaudoix[idOptionMenu] + " kem + " + pricex[idOptionMenu] + " lượng)";
                mmenu[ix] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][ix]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
        } else if (idMenu != 6) {
            if (idMenu == 10) {
                if (player.listGemitemLock[GemTemplate.ID_NGOC_RONG[idOptionMenu]] <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngọc", ""));
                    return;
                }

                player.delGem(GemTemplate.ID_NGOC_RONG[idOptionMenu], 1, true);
                Item it = doBuyModelClothe(player, 725, idOptionMenu + 1, 1);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi 1 " + it.getName(), ""));
                Database.instance.saveLogThongKeChiTieu("BACH MI " + (idOptionMenu + 1), 0, 1, 1L, "ngoc");
            } else if (idMenu == 11) {
                if (player.listGemitemLock[GemTemplate.ID_NGOC_RONG[idOptionMenu]] <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngọc", ""));
                    return;
                }

                player.delGem(GemTemplate.ID_NGOC_RONG[idOptionMenu], 1, true);
                Item it = doBuyModelClothe(player, 726, idOptionMenu + 1, 1);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi 1 " + it.getName(), ""));
                Database.instance.saveLogThongKeChiTieu("THUY VU HONG SAM " + (idOptionMenu + 1), 0, 1, 1L, "ngoc");
            }
        }
    }

    static void doReceiveTopEventTet2019(Map map, Char p, int idNpc, int idMenu, int idOptionMenu) {
        try {
            int[] idTrung = new int[]{684, 685, 686, 687, 688};
            String info = "";
            String infoAlert = "";
            if (idMenu == 0) {
                String[] menu = new String[]{"Chổi băng", "Chổi độc", "Chổi điện"};
                p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
            } else if (idMenu == 20) {
                String[] menu = new String[]{"Trứng Cú", "Trứng Rồng", "Trứng Yêu Tinh", "Trứng Dơi", "Trứng Đại bàng"};
                p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, idOptionMenu + 22));
            } else if (idMenu == 22) {
                Item it = createtrungPet(idTrung[idOptionMenu]);
                p.iItems.add(it);
                Char.createChoi(617, p);
                info = "nhan choi bang + " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi băng, 1 " + it.getName();
            } else if (idMenu == 23) {
                Item it = createtrungPet(idTrung[idOptionMenu]);
                p.iItems.add(it);
                Char.createChoi(619, p);
                info = "nhan doc bang + " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi độc, 1 " + it.getName();
            } else if (idMenu == 24) {
                Item it = createtrungPet(idTrung[idOptionMenu]);
                p.iItems.add(it);
                Char.createChoi(618, p);
                info = "nhan dien bang + " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi điện, 1 " + it.getName();
            }

            if (idMenu >= 22) {
                if (p.canReceiveGiftOffline == 1) {
                    p.createPetVinhVien(28);
                    info = info + " và 1 long den vinh vien";
                    infoAlert = infoAlert + " và 1 lồng đèn vĩnh viễn";
                } else {
                    p.createPet(28, 43200);
                    info = info + " và 1 long den 30 ngay";
                    infoAlert = infoAlert + " và 1 lồng đèn 30 ngày";
                }

                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                Database.instance.saveOrtherLog("", p.charname, "nhan qua top " + p.canReceiveGiftOffline + " " + info, "quatop");
                p.canReceiveGiftOffline = -1;
                Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                p.sendMessage(MessageCreator.createServerAlertMessage(infoAlert, ""));
            }
        } catch (Exception var9) {
        }
    }

    static void doReceiveTopEventTet2020(Map map, Char p, int idNpc, int idMenu, int idOptionMenu) {
        try {
            if (p.isFullInventory()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                return;
            }

            int[] idgift = new int[]{632, 635, 638};
            int idhove = idgift[0];
            if (p.lvDetail.lv >= 60) {
                idhove = idgift[2];
            } else if (p.lvDetail.lv >= 50) {
                idhove = idgift[1];
            }

            int[] var10000 = new int[]{684, 685, 686, 687, 688};
            int[] idtrungThu = new int[]{74, 72};
            String info = "";
            String infoAlert = "";
            Item it = null;
            if (idMenu == 0) {
                String[] menu = new String[]{"Chổi băng", "Chổi độc", "Chổi điện", "Chổi lửa"};
                p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
            } else if (idMenu == 20) {
                if (p.canReceiveGiftOffline > 2) {
                    if (idOptionMenu == 0) {
                        Char.createChoi(617, p, 129600, 1, false);
                        it = map.doCreateHoVeLenh(p, 43200, idhove);
                        info = "nhan choi bang  và " + it.getName();
                        infoAlert = "Chúc mừng bạn nhận được 1 chổi băng và " + it.getName();
                    } else if (idOptionMenu == 1) {
                        Char.createChoi(619, p, 129600, 1, false);
                        it = map.doCreateHoVeLenh(p, 43200, idhove);
                        info = "nhan choi doc  và " + it.getName();
                        infoAlert = "Chúc mừng bạn nhận được 1 chổi độc và " + it.getName();
                    } else if (idOptionMenu == 2) {
                        Char.createChoi(618, p, 129600, 1, false);
                        info = "nhan choi bang ";
                        it = map.doCreateHoVeLenh(p, 43200, idhove);
                        infoAlert = "Chúc mừng bạn nhận được 1 chổi điện và " + it.getName();
                    } else if (idOptionMenu == 3) {
                        Char.createChoi(729, p, 129600, 1, false);
                        it = map.doCreateHoVeLenh(p, 43200, idhove);
                        info = "nhan choi lua " + it.getName();
                        infoAlert = "Chúc mừng bạn nhận được 1 chổi lửa và " + it.getName();
                    }
                } else {
                    String[] menu = new String[]{"Trứng Bạch Cốt", "Trứng Đương Khang"};
                    p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, idOptionMenu + 22));
                }
            } else if (idMenu == 22) {
                if (p.canReceiveGiftOffline > 2) {
                    return;
                }

                Char.createChoi(617, p, -1, 0, true);
                it = map.doCreateHoVeLenh(p, 43200, idhove);
                p.potions[idtrungThu[idOptionMenu]]++;
                info = "nhan choi bang " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi băng, 1 " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
            } else if (idMenu == 23) {
                if (p.canReceiveGiftOffline > 2) {
                    return;
                }

                Char.createChoi(619, p, -1, 0, true);
                it = map.doCreateHoVeLenh(p, 43200, idhove);
                p.potions[idtrungThu[idOptionMenu]]++;
                info = "nhan choi doc + " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi độc, 1 " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
            } else if (idMenu == 24) {
                if (p.canReceiveGiftOffline > 2) {
                    return;
                }

                Char.createChoi(618, p, -1, 0, true);
                it = map.doCreateHoVeLenh(p, 43200, idhove);
                p.potions[idtrungThu[idOptionMenu]]++;
                info = "nhan choi dien + " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi điện, 1 " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
            } else if (idMenu == 25) {
                if (p.canReceiveGiftOffline > 2) {
                    return;
                }

                Char.createChoi(729, p, -1, 0, true);
                it = map.doCreateHoVeLenh(p, 43200, idhove);
                p.potions[idtrungThu[idOptionMenu]]++;
                info = "nhan choi lua + " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
                infoAlert = "Chúc mừng bạn nhận được 1 chổi lửa, 1 " + LoginHandler.PORTION_NAME[idtrungThu[idOptionMenu]] + " và " + it.getName();
            }

            if (idMenu >= 22 || idMenu == 20 && p.canReceiveGiftOffline > 2) {
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                Database.instance.saveOrtherLog("", p.charname, "nhan qua top " + p.canReceiveGiftOffline + " " + info, "quatop");
                p.canReceiveGiftOffline = -1;
                Database.instance.updateStateReceiveGiftEventOutGame(p.charname);
                p.sendMessage(MessageCreator.createServerAlertMessage(infoAlert, ""));
            }
        } catch (Exception var13) {
        }
    }

    static void doMenuHalloween2016(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số lượng hộp quà", 1));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
            } else if (idOptionMenu == 2) {
                player.sendMessage(
                        MessageCreator.createMsgMenu(new String[]{"Đổi mặt nạ 7 ngày(150đ)", "Đổi mặt nạ 30 ngày(500đ)", "Đổi Hộp quà(1000đ)"}, idNpc, 10)
                );
            } else if (idOptionMenu == 3) {
                if (!Char.isSuKien2011l2016()) {
                    return;
                }

                if (!Char.isBanLongDenHoaSen()) {
                    return;
                }

                if (player.buylongden == 1) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể mua thêm"));
                    return;
                }

                if (player.getLuong() < 1000) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đầy"));
                    return;
                }

                player.buylongden = 1;
                player.subLuong(1000L);
                player.createPetVinhVien(28);
            } else if (idOptionMenu == 4) {
                if (!isMapLang(player.map)) {
                    return;
                }

                if (!Char.isSuKien2011l2016()) {
                    return;
                }

                if (player.buychoi == 1) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể mua thêm"));
                    return;
                }

                if (player.getLuong() < 3000) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đầy"));
                    return;
                }

                if (!isTimeBuyChoi()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Chưa đến giờ mở bán chổi hoặc đã bán hết chổi."));
                    return;
                }

                player.subLuong(3000L);
                totalChoiBan--;
                Database.instance.updateSoLuongChoiBan();
                Char.createChoi(617, player);
            } else if (idOptionMenu == 5) {
                if (!isMapLang(player.map)) {
                    return;
                }

                if (!Char.isSuKien2011l2016()) {
                    return;
                }

                if (player.buychoi == 1) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể mua thêm"));
                    return;
                }

                if (player.getLuong() < 3000) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đầy"));
                    return;
                }

                if (!isTimeBuyChoi()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Chưa đến giờ mở bán chổi hoặc đã bán hết chổi."));
                    return;
                }

                player.subLuong(3000L);
                totalChoiBan--;
                Database.instance.updateSoLuongChoiBan();
                Char.createChoi(619, player);
            } else if (idOptionMenu == 6) {
                if (!isMapLang(player.map)) {
                    return;
                }

                if (!Char.isSuKien2011l2016()) {
                    return;
                }

                if (player.buychoi == 1) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không thể mua thêm"));
                    return;
                }

                if (player.getLuong() < 3000) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Không đủ lượng"));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Hành trang đầy"));
                    return;
                }

                if (!isTimeBuyChoi()) {
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Chưa đến giờ mở bán chổi hoặc đã bán hết chổi."));
                    return;
                }

                player.subLuong(3000L);
                totalChoiBan--;
                Database.instance.updateSoLuongChoiBan();
                Char.createChoi(618, player);
            }
        } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
            int[] sl = new int[]{0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 50, 50, 50, 50};
            int[] price = new int[]{0, 50, 70, 100, 120};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[137] < slCaudoi[idMenu] || player.potions[138] < slCaudoi[idMenu]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ nguyên liệu", ""));
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[137] = player.potions[137] - slCaudoi[idMenu];
            player.potions[138] = player.potions[138] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            Item newitem = null;
            int idmanh = -1;
            if (Char.isSuKienHaloween2016()) {
                int[] pc = new int[]{0, 1, 2, 3, 5};
                if (r.nextInt(1000) < pc[idMenu]) {
                    int[] ptManhBando = new int[]{77, 105, 117};
                    idmanh = ptManhBando[r.nextInt(ptManhBando.length)];
                    player.potions[idmanh]++;
                }
            }

            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            String info = "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa";
            if (newitem != null) {
                info = info + " và " + newitem.getName();
            }

            if (idmanh > -1) {
                info = info + " và " + LoginHandler.PORTION_NAME[idmanh];
            }

            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            Database.instance
                    .saveOrtherLog(
                            "",
                            player.charname,
                            sl[idMenu]
                            + "_"
                            + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                            + " "
                            + (newitem != null ? newitem.getName() : "")
                            + " "
                            + (idmanh > -1 ? LoginHandler.PORTION_NAME[idmanh] : ""),
                            "redeem"
                    );
        } else if (idMenu == 5) {
            int[] slx = new int[]{3, 5, 7, 10};
            int[] slCaudoix = new int[]{50, 50, 50, 50};
            int[] pricex = new int[]{50, 70, 100, 120};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String infox = "(" + slCaudoix[idOptionMenu] + " kẹo+bí+" + pricex[idOptionMenu] + " lượng)";
                mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + infox;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
        } else if (idMenu == 10) {
            if (idOptionMenu == 0) {
                player.sendMessage(
                        MessageCreator.createMsgMenu(
                                new String[]{"Mặt nạ bí ngô", "Mặt nạ ngạ quỷ", "Mặt nạ ngạ khuyển", "Mặt nạ thi khiển", "Mặt nạ dạ xoa"}, idNpc, 11
                        )
                );
            } else if (idOptionMenu == 1) {
                player.sendMessage(
                        MessageCreator.createMsgMenu(
                                new String[]{"Mặt nạ bí ngô", "Mặt nạ ngạ quỷ", "Mặt nạ ngạ khuyển", "Mặt nạ thi khiển", "Mặt nạ dạ xoa"}, idNpc, 12
                        )
                );
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgInputText(-32017, 0, "Số lượng hộp quà", 1));
            }
        } else if (idMenu == 11) {
            if (player.luongxainhanRuong < 150L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điểm", ""));
                return;
            }

            player.sendMessage(
                    MessageCreator.createMsgMenu(new String[]{"Tăng 5% dam", "Tăng 5% thủ", "Tăng 10% exp", "Bạo kích 3%", "Tăng 5% lan"}, idNpc, 13 + idOptionMenu)
            );
        } else if (idMenu == 12) {
            if (player.luongxainhanRuong < 500L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điểm", ""));
                return;
            }

            player.sendMessage(
                    MessageCreator.createMsgMenu(new String[]{"Tăng 5% dam", "Tăng 5% thủ", "Tăng 10% exp", "Bạo kích 3%", "Tăng 5% lan"}, idNpc, 20 + idOptionMenu)
            );
        } else if (idMenu >= 13 && idMenu <= 17) {
            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                return;
            }

            if (player.luongxainhanRuong < 150L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điểm", ""));
                return;
            }

            String[] noption = new String[]{"tang dam", "tang thu", "tang exp", "tang bk", "tang lan"};
            player.luongxainhanRuong -= 150L;
            int[] valuemn = new int[]{5, 5, 10, 30, 50};
            Item newitemx = doCreateMatNa(player, 10080, ID_MAT_NA[idMenu - 13], idOptionMenu, valuemn[idOptionMenu]);
            player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newitemx.getName() + " 7 ngày", ""));
            Database.instance
                    .saveOrtherLog("", player.charname, player.luongxainhanRuong + " doi mn 7 ngay: " + newitemx.getName() + "|" + newitemx.getInfoSave(), "redeem");
            Database.instance.saveLogThongKeChiTieu(newitemx.getName() + "_" + noption[idOptionMenu], 0, 1, 150L, "matna7");
        } else if (idMenu >= 20 && idMenu <= 24) {
            if (player.isFullInventory()) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                return;
            }

            if (player.luongxainhanRuong < 500L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điểm", ""));
                return;
            }

            String[] noption = new String[]{"tang dam", "tang thu", "tang exp", "tang bk", "tang lan"};
            player.luongxainhanRuong -= 500L;
            int[] valuemn = new int[]{5, 5, 10, 30, 50};
            Item newitemx = doCreateMatNa(player, 43200, ID_MAT_NA[idMenu - 20], idOptionMenu, valuemn[idOptionMenu]);
            player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + newitemx.getName() + " 30 ngày", ""));
            Database.instance
                    .saveOrtherLog("", player.charname, player.luongxainhanRuong + " doi mn 30 ngay: " + newitemx.getName() + "|" + newitemx.getInfoSave(), "redeem");
            Database.instance.saveLogThongKeChiTieu(newitemx.getName() + "_" + noption[idOptionMenu], 0, 1, 500L, "matna30");
        }
    }

    static void doMenuGioTo2020(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(
                        MessageCreator.createMsgMenu(new String[]{"Pet Bạch thử", "áo Lạc long quân", "áo yếm", "Vũ khí thời trang", "Pháo hoa"}, idNpc, 6)
                );
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
            } else if (idOptionMenu != 2) {
                if (idOptionMenu == 3) {
                    if (player.isFullInventory()) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                        return;
                    }

                    if (player.luongxainhanRuong < player.luongxai) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ điểm", ""));
                        return;
                    }

                    String info = player.luongxainhanRuong
                            + " :doi "
                            + player.luongxai
                            + " nhan 1 ruong bat bao. diem con lai la "
                            + (player.luongxainhanRuong - player.luongxai);
                    player.potions[120]++;
                    player.luongxainhanRuong = player.luongxainhanRuong - player.luongxai;
                    player.luongxai += 100L;
                    if (player.luongxai > 2000L) {
                        player.luongxai = 2000L;
                    }

                    info = info + ". sau doi co: " + player.potions[120] + " ruong bat bao";
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được 1 rương bát bảo"));
                    Database.instance.saveOrtherLog("", player.charname, info, "doiruong");
                } else if (idOptionMenu == 4) {
                    if (player.luongxainhanRuong == 0L) {
                        return;
                    }

                    if (player.luongxainhanRuong > player.luongxai) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Điểm xài lượng phải nhỏ hơn " + player.luongxai + " mới có thể đổi qua xu", ""));
                        return;
                    }

                    player.addXu(player.luongxainhanRuong * 10000L, "map 50");
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createMsgChat(player.id, "Nhận được " + player.luongxainhanRuong * 10000L + " xu"));
                    Database.instance
                            .saveOrtherLog(
                                    "",
                                    player.charname,
                                    "đổi " + player.luongxainhanRuong + " qua " + player.luongxainhanRuong * 10000L + " xu. diem yc doi ruong la: " + player.luongxai,
                                    "doixu"
                            );
                    player.luongxainhanRuong = 0L;
                }
            }
        } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
            int[] sl = new int[]{0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 50, 50, 50, 50};
            int[] price = new int[]{0, 50, 70, 100, 120};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[117] < slCaudoi[idMenu]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ nguyên liệu", ""));
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[117] = player.potions[117] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            boolean receiveda = false;
            int[] tlnhanhvl = new int[]{0, 1, 1, 3, 10};
            if (r.nextInt(100) < tlnhanhvl[idMenu] && map.doCreateHoVeLenh(player, 10080) != null) {
                receiveda = true;
            }

            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + (receiveda ? " và 1 Hộ vệ lệnh" : ""), ""
                    )
            );
            Database.instance
                    .saveOrtherLog(
                            "",
                            player.charname,
                            sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " " + (receiveda ? "và 1 ho ve lenh " : ""),
                            "redeem"
                    );
        } else if (idMenu == 5) {
            int[] slx = new int[]{3, 5, 7, 10};
            int[] slCaudoix = new int[]{50, 50, 50, 50};
            int[] var10000 = new int[]{50, 70, 100, 120};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoix[idOptionMenu] + " dưa)";
                mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
        } else if (idMenu == 6) {
            int[] slx = new int[]{100, 100, 100, 50, 50};
            int[] money = new int[]{50, 50, 50, 50, 5};
            if (idOptionMenu < 4) {
                if (player.potions[77] < slx[idOptionMenu]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + slx[idOptionMenu] + " " + LoginHandler.PORTION_NAME[77], ""));
                    return;
                }

                if (player.potions[105] < slx[idOptionMenu]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + slx[idOptionMenu] + " " + LoginHandler.PORTION_NAME[105], ""));
                    return;
                }

                if (player.potions[142] < slx[idOptionMenu]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + slx[idOptionMenu] + " " + LoginHandler.PORTION_NAME[142], ""));
                    return;
                }

                if (player.getLuong() < money[idOptionMenu]) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ " + money[idOptionMenu] + " lượng.", ""));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                    return;
                }

                player.subLuong((long) money[idOptionMenu]);
                player.potions[77] = player.potions[77] - slx[idOptionMenu];
                player.potions[105] = player.potions[105] - slx[idOptionMenu];
                player.potions[142] = player.potions[142] - slx[idOptionMenu];
                String info = "Đổi";
                if (idOptionMenu == 0) {
                    int[] time = new int[]{30};
                    player.doCreatePetBachThu(time[r.nextInt(time.length)] * 24 * 60, false);
                    info = info + " pet Bạch thử 30 ngày";
                } else if (idOptionMenu == 1) {
                    ShopTemplate item = getShopTemplate(51);
                    doBuyModelClothe(player, item, 30);
                    info = info + " áo Lạc long quân 30 ngày";
                } else if (idOptionMenu == 2) {
                    ShopTemplate item = getShopTemplate(56);
                    doBuyModelClothe(player, item, 30);
                    info = info + " áo yếm đào 30 ngày";
                } else if (idOptionMenu == 3) {
                    Item it = doCreateVuKhiThoiTrang(player, 10080, ID_VU_KHI_THOI_TRANG[player.charClass]);
                    info = info + " " + it.getName() + " 7 ngày.";
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            } else if (idOptionMenu == 4) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số Pháo", 1));
            }
        }
    }

    static void doMenuGioTo2016(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, 0, "Số bánh chưng", 1));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số bánh giày", 1));
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
            }
        } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
            int[] sl = new int[]{0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 25, 25, 25, 25};
            int[] price = new int[]{0, 50, 70, 100, 120};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[83] < slCaudoi[idMenu] || player.potions[105] < slCaudoi[idMenu]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ nguyên liệu", ""));
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[83] = player.potions[83] - slCaudoi[idMenu];
            player.potions[105] = player.potions[105] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            boolean receiveda = false;
            if (idMenu == 3 || idMenu == 4) {
                int tyle = 10;
                if (idMenu == 4) {
                    tyle = 15;
                }

                if (r.nextInt(100) < tyle) {
                    player.doAddGemItem(GemTemplate.DA_TIEN_GIAI, 1, true);
                    receiveda = true;
                }
            }

            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được "
                            + sl[idMenu]
                            + " "
                            + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                            + (receiveda ? " và 1 đá tiến giai" : "")
                            + " khóa",
                            ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
        } else if (idMenu == 5) {
            int[] slx = new int[]{3, 5, 7, 10};
            int[] slCaudoix = new int[]{25, 25, 25, 25};
            int[] var10000 = new int[]{50, 70, 100, 120};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoix[idOptionMenu] + " kẹo)";
                mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
        }
    }

    static void doMenuTet2016(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, 0, "Số pháo", 1));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số bao lì xì", 1));
            } else if (idOptionMenu == 3) {
                if (player.getLuong() < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng.", ""));
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy.", ""));
                    return;
                }

                player.subLuong(100L);
                player.createPet(22, 10080);
            } else {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
            }
        } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
            int[] sl = new int[]{0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 1, 1, 1, 1};
            int[] price = new int[]{0, 50, 70, 100, 140};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[74] < slCaudoi[idMenu] || player.potions[120] < slCaudoi[idMenu] || player.potions[83] < slCaudoi[idMenu]) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa có đủ các chữ Phúc Lộc Thọ", ""));
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[74] = player.potions[74] - slCaudoi[idMenu];
            player.potions[120] = player.potions[120] - slCaudoi[idMenu];
            player.potions[83] = player.potions[83] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            boolean receiveda = false;
            if (idMenu == 3 || idMenu == 4) {
                int tyle = 10;
                if (idMenu == 4) {
                    tyle = 15;
                }

                if (r.nextInt(100) < tyle) {
                    player.doAddGemItem(GemTemplate.DA_TIEN_GIAI, 1, true);
                    receiveda = true;
                }
            }

            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được "
                            + sl[idMenu]
                            + " "
                            + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                            + (receiveda ? " và 1 đá tiến giai" : "")
                            + " khóa",
                            ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
        } else if (idMenu == 5) {
            int[] slx = new int[]{3, 5, 7, 10};
            int[] slCaudoix = new int[]{1, 1, 1, 1};
            int[] var10000 = new int[]{50, 70, 100, 140};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoix[idOptionMenu] + " PLT)";
                mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
        }
    }

    static void doMenuNoel2015(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (!Char.isFinishAllSuKienNoel()) {
            if (idMenu == 0) {
                if (idOptionMenu == 0) {
                    player.sendMessage(MessageCreator.createMsgInputText(-32015, 0, "Số túi quà", 1));
                } else if (idOptionMenu == 1) {
                    player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số hộp quà", 1));
                } else if (idOptionMenu == 2) {
                    player.sendMessage(MessageCreator.createMsgInputText(-32017, 0, "Số sao vàng", 1));
                } else {
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 5));
                }
            } else if (idMenu == 1 || idMenu == 2 || idMenu == 3 || idMenu == 4) {
                int[] sl = new int[]{0, 3, 5, 7, 10};
                int[] slCaudoi = new int[]{0, 10, 10, 10, 10};
                int[] price = new int[]{0, 50, 70, 100, 140};
                if (player.getLuong() < price[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                if (player.potions[77] < slCaudoi[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage(
                                    "Bạn phải có đủ " + slCaudoi[idMenu] + " sao vàng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", ""
                            )
                    );
                    return;
                }

                player.subLuong((long) price[idMenu]);
                player.potions[77] = player.potions[77] - slCaudoi[idMenu];
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                        )
                );
                Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
            } else if (idMenu == 5) {
                int[] slx = new int[]{3, 5, 7, 10};
                int[] slCaudoix = new int[]{10, 10, 10, 10};
                int[] var10000 = new int[]{50, 70, 100, 140};
                String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                    String info = "(" + slCaudoix[idOptionMenu] + " sao vàng)";
                    mmenu[i] = slx[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 1));
            }
        }
    }

    static void doMenuTrungThu2016(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số bánh nướng", 1));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, 0, "Số bánh dẻo", 1));
            } else if (idOptionMenu == 2) {
                String[] menu = new String[]{"Hộp bánh thập cẩm", "Hộp bánh đặc biệt"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
            } else if (idOptionMenu == 3) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 3));
            } else if (idOptionMenu == 4) {
                String[] mmenu = new String[]{"Lồng đèn Cá chép", "Lồng đèn Ngôi sao", "Lồng đèn Trăng khuyết"};
                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, 8));
            } else if (idOptionMenu == 5) {
                if (player.potions[124] < 20) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 20 bánh dẻo", ""));
                    return;
                }

                if (player.getLuong() < 70) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ 70 lượng", ""));
                    return;
                }

                player.subLuong(70L);
                player.potions[124] = player.potions[124] - 20;
                player.doCreatePetBachThu(43200, true);
                Database.instance.saveOrtherLog("", player.charname, "doi pet bach thu " + idOptionMenu, "petbachthu");
                Database.instance.saveLogThongKeChiTieu("pet bach thu", 0, 1, 70L, "luong");
            } else if (idOptionMenu == 6) {
                Vector<CharInfo> top = Database.instance.getTopEvent();
                String[] menu = new String[top.size()];

                for (int i = 0; i < top.size(); i++) {
                    CharInfo c = (CharInfo) top.get(i);
                    menu[i] = c.name;
                }

                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 9));
            }
        } else if (idMenu == 1) {
            player.sendMessage(MessageCreator.createMsgInputText(-32017 - idOptionMenu, 0, "Số hộp bánh", 1));
        } else if (idMenu != 2) {
            if (idMenu == 3) {
                int[] sl = new int[]{3, 5, 7, 10};
                int[] slCaudoi = new int[]{20, 20, 20, 20};
                int[] var10000 = new int[]{20, 20, 20, 20};
                var10000 = new int[]{50, 70, 100, 120};
                String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                    String info = "(" + slCaudoi[idOptionMenu] + " bd)";
                    mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 4));
            } else if (idMenu == 7 || idMenu == 4 || idMenu == 5 || idMenu == 6) {
                int[] sl = new int[]{0, 0, 0, 0, 3, 5, 7, 10};
                int[] slCaudoi = new int[]{0, 0, 0, 0, 20, 20, 20, 20};
                int[] slCaudoi1 = new int[]{0, 0, 0, 0, 20, 20, 20, 20};
                int[] price = new int[]{0, 0, 0, 0, 50, 70, 100, 120};
                if (player.getLuong() < price[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                if (player.potions[124] < slCaudoi[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage(
                                    "Bạn phải có đủ " + slCaudoi[idMenu] + " bánh dẻo mới có thể đổi được " + sl[idMenu] + " nguyên liệu", ""
                            )
                    );
                    return;
                }

                boolean nhantuhondan = false;
                if (r.nextInt(100) < 10 && doCreateHoVeLenhByLevel(player, 4320) != null) {
                    nhantuhondan = true;
                }

                player.subLuong((long) price[idMenu]);
                player.potions[124] = player.potions[124] - slCaudoi1[idMenu];
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn nhận được "
                                + sl[idMenu]
                                + " "
                                + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                                + " khóa"
                                + (nhantuhondan ? " và hộ vệ lệnh" : ""),
                                ""
                        )
                );
                Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
                Database.instance
                        .saveLogThongKeChiTieu(gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " " + price[idMenu], 0, 1, (long) price[idMenu], "luong");
            } else if (idMenu == 8) {
                if (player.receiveGiftEvent == 1) {
                    return;
                }

                int[] id = new int[]{12, 19, 20};
                player.createPet(id[idOptionMenu], 180);
                player.receiveGiftEvent = 1;
                Database.instance.saveOrtherLog("", player.charname, "doi long den " + idOptionMenu, "qualongden");
            }
        }
    }

    static void doMenuTrungThu2015(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                String[] menu = new String[]{"Bánh dẻo(10bột-10trứng-10đx-3k xu)", "Bánh nướng(10bột-10trứng-10đx-10thịt-5k xu)"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
            } else if (idOptionMenu == 1) {
                String[] menu = new String[]{"Lửa nhỏ(2 củi)", "Lửa vừa(5 lửa nhỏ)", "Lửa to(5 lửa vừa)"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 2));
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 3));
            } else if (idOptionMenu == 3) {
                if (player.potions[120] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh dẻo", ""));
                    return;
                }

                if (player.getLuong() < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                player.potions[120] = player.potions[120] - 50;
                player.subLuong(100L);
                player.createPet(21, 10080);
                Database.instance.saveOrtherLog("", player.charname, "doi long den rong xanh", "rongxanh");
            } else if (idOptionMenu == 4) {
                String[] mmenu = new String[]{"Lồng đèn Cá chép", "Lồng đèn Ngôi sao", "Lồng đèn Trăng khuyết"};
                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, 8));
            }
        } else if (idMenu == 1) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, 0, "Số bánh dẻo", 1));
            } else {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số bánh nướng", 1));
            }
        } else if (idMenu == 2) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32017, 0, "Số lửa nhỏ", 1));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgInputText(-32018, 0, "Số lửa vừa", 1));
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgInputText(-32019, 0, "Số lửa to", 1));
            }
        } else if (idMenu == 3) {
            int[] sl = new int[]{3, 5, 7, 10};
            int[] slCaudoi = new int[]{50, 50, 50, 50};
            int[] slCaudoi1 = new int[]{5, 10, 15, 20};
            int[] var10000 = new int[]{70, 100, 140, 180};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoi[idOptionMenu] + " bd, " + slCaudoi1[idOptionMenu] + " bn)";
                mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 4));
        } else if (idMenu == 7 || idMenu == 4 || idMenu == 5 || idMenu == 6) {
            int[] sl = new int[]{0, 0, 0, 0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 0, 0, 0, 50, 50, 50, 50};
            int[] slCaudoi1 = new int[]{0, 0, 0, 0, 5, 10, 15, 20};
            int[] price = new int[]{0, 0, 0, 0, 70, 100, 140, 180};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[120] < slCaudoi[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn phải có đủ " + slCaudoi[idMenu] + " bánh dẻo mới có thể đổi được " + sl[idMenu] + " nguyên liệu", ""
                        )
                );
                return;
            }

            if (player.potions[105] < slCaudoi1[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn phải có đủ " + slCaudoi1[idMenu] + " bánh nướng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", ""
                        )
                );
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[120] = player.potions[120] - slCaudoi1[idMenu];
            player.potions[105] = player.potions[105] - slCaudoi1[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
        } else if (idMenu == 8) {
            if (player.receiveGiftEvent == 1) {
                return;
            }

            int[] id = new int[]{12, 19, 20};
            player.createPet(id[idOptionMenu], 1440);
            player.receiveGiftEvent = 1;
            Database.instance.saveOrtherLog("", player.charname, "doi long den " + idOptionMenu, "qualongden");
        }
    }

    static void doMenuSuKienMiniChucNu(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.lvDetail.lv < 10) {
                    player.sendMessage(MessageCreator.createMsgChat(idNPC49, "Hãy đi luyện đến cấp độ 10 rồi quay về gặp ta."));
                    return;
                }

                if (player.receiveQuestVulan == 0) {
                    if (player.countnvVulan >= 100) {
                        player.sendMessage(MessageCreator.createMsgChat(idNPC49, "Ta đã tìm thấy Chức Nữ rồi. Chúc ngươi may mắn"));
                        return;
                    }

                    if (player.potions[77] <= 0) {
                        player.sendMessage(
                                MessageCreator.createMsgChat(idNPC49, "Hay đi tìm lá thư của Ngưu Lang rồi đến gặp ta. Lá thư của Ngưu Lang có bán trong cửa hàng")
                        );
                        return;
                    }

                    player.sendMessage(
                            MessageCreator.createMsgPopUp(player.id, 14, "Chức Nữ đi dạo đã lâu rồi mà chưa thấy về. Ngươi có thể giúp ta tìm Nàng không?")
                    );
                } else if (player.receiveQuestVulan == 1) {
                    if (player.charCopy == null
                            || player.map.mapId != player.charCopy.map.mapId
                            || player.map.mapIDLoadMap != 0 && player.map.mapIDLoadMap != 70 && player.map.mapIDLoadMap != 80) {
                        if (player.charCopy == null) {
                            player.receiveQuestVulan = 0;
                        }

                        player.sendMessage(MessageCreator.createMsgChat(idNPC49, "Hãy tranh thủ tìm giúp ta Chức Nữ về đây nha."));
                    } else {
                        map.doGiftQuestChucNu(player);
                        player.receiveQuestVulan = 0;
                        player.charCopyDissapear();
                    }
                }
            } else if (idOptionMenu == 1) {
                player.sendMessage(
                        MessageCreator.createMsgMenu(
                                new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6", "1 ng.liệu sc 6(1s+10l)"}, idNpc, 2
                        )
                );
            }
        } else if (idMenu != 1) {
            if (idMenu == 2) {
                if (idOptionMenu == 4) {
                    if (player.getLuong() < 10) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 10 lượng mới có thể đổi được 1 nguyên liệu", ""));
                        return;
                    }

                    if (player.potions[105] < 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 1 sao mới có thể đổi được 1 nguyên liệu", ""));
                        return;
                    }

                    int idgem = r.nextInt(GemTemplate.ID_MATERIAL_LOW[5].length);
                    Database.instance.saveLogThongKeChiTieu(GemTemplate.ID_MATERIAL_LOW[5][idgem] + "_doi_" + 1, 0, 1, 10L, "luong");
                    player.subLuong(10L);
                    player.potions[105]--;
                    player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idgem], 1, true);
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn nhận được 1 " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idgem]].name + " khóa", "")
                    );
                    Database.instance.saveOrtherLog("", player.charname, "1_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idgem]].name, "redeem");
                    return;
                }

                int[] sl = new int[]{3, 5, 7, 10};
                int[] slCaudoi = new int[]{5, 5, 5, 5};
                int[] var10000 = new int[]{50, 70, 100, 120};
                String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                    String info = "( " + slCaudoi[idOptionMenu] + " sao vàng)";
                    mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 3));
            } else if (idMenu == 3 || idMenu == 4 || idMenu == 5 || idMenu == 6) {
                int[] sl = new int[]{0, 0, 0, 3, 5, 7, 10};
                int[] slCaudoi = new int[]{0, 0, 0, 5, 5, 5, 5};
                int[] price = new int[]{0, 0, 0, 50, 70, 100, 120};
                if (player.getLuong() < price[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                if (player.potions[105] < slCaudoi[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slCaudoi[idMenu] + " sao mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                Database.instance.saveLogThongKeChiTieu(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu] + "_doi_" + sl[idMenu], 0, 1, (long) price[idMenu], "luong");
                player.subLuong((long) price[idMenu]);
                player.potions[105] = player.potions[105] - slCaudoi[idMenu];
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                int[] var15 = new int[]{0, 0, 0, 1, 2, 5, 10};
                boolean istuhondan = false;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn nhận được "
                                + sl[idMenu]
                                + " "
                                + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                                + " khóa"
                                + (istuhondan ? " và 1 Tiên đan" : ""),
                                ""
                        )
                );
                Database.instance
                        .saveOrtherLog(
                                "",
                                player.charname,
                                sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + (istuhondan ? " và 1 Tiên đan" : ""),
                                "redeem"
                        );
            }
        }
    }

    static void doMenuSuKienHe2017(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.lvDetail.lv < 10) {
                    player.sendMessage(MessageCreator.createMsgChat(-49, "Hãy đi luyện đến cấp độ 10 rồi quay về gặp ta."));
                    return;
                }

                if (player.receiveQuestVulan == 0) {
                    if (player.countnvVulan >= 100) {
                        player.sendMessage(MessageCreator.createMsgChat(-49, "Con ta đã về nhà rồi. Chúc ngươi may mắn"));
                        return;
                    }

                    if (player.potions[142] <= 0) {
                        player.sendMessage(MessageCreator.createMsgChat(-49, "Hãy đi tìm trẻ con rồi đến gặp ta nhé."));
                        return;
                    }

                    player.sendMessage(
                            MessageCreator.createMsgPopUp(player.id, 13, "Con ta đi tìm thuốc cho ta đã lâu rồi mà chưa thấy về. Ngươi có thể giúp ta tìm con không?")
                    );
                } else if (player.receiveQuestVulan == 1) {
                    if (player.charCopy == null
                            || player.map.mapId != player.charCopy.map.mapId
                            || player.map.mapIDLoadMap != 0 && player.map.mapIDLoadMap != 70 && player.map.mapIDLoadMap != 80) {
                        if (player.charCopy == null) {
                            player.receiveQuestVulan = 0;
                        }

                        player.sendMessage(MessageCreator.createMsgChat(-49, "Hãy tranh thủ tìm giúp con ta về đây nha."));
                    } else {
                        map.doGiftQuestDuaTre2020(player);
                        player.receiveQuestVulan = 0;
                        player.charCopyDissapear();
                    }
                }
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 2));
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, 0, "Nhập số lượng", 1));
            }
        } else if (idMenu != 1) {
            if (idMenu == 2) {
                int[] sl = new int[]{3, 5, 7, 10};
                int[] slCaudoi = new int[]{10, 10, 10, 10};
                int[] var10000 = new int[]{50, 70, 100, 120};
                String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                    String info = "( " + slCaudoi[idOptionMenu] + " kem )";
                    mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 3));
            } else if (idMenu == 3 || idMenu == 4 || idMenu == 5 || idMenu == 6) {
                int[] sl = new int[]{0, 0, 0, 3, 5, 7, 10};
                int[] slCaudoi = new int[]{0, 0, 0, 10, 10, 10, 10};
                int[] price = new int[]{0, 0, 0, 50, 70, 100, 120};
                if (player.getLuong() < price[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                if (player.potions[105] < slCaudoi[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slCaudoi[idMenu] + " kem mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                Database.instance.saveLogThongKeChiTieu(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu] + "_doi_" + sl[idMenu], 0, 1, (long) price[idMenu], "luong");
                player.subLuong((long) price[idMenu]);
                player.potions[105] = player.potions[105] - slCaudoi[idMenu];
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                int[] pc = new int[]{0, 0, 0, 1, 2, 5, 10};
                boolean istuhondan = false;
                if (r.nextInt(100) < pc[idMenu]) {
                    player.potions[119]++;
                    istuhondan = true;
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn nhận được "
                                + sl[idMenu]
                                + " "
                                + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name
                                + " khóa"
                                + (istuhondan ? " và 1 Tiên đan" : ""),
                                ""
                        )
                );
                Database.instance
                        .saveOrtherLog(
                                "",
                                player.charname,
                                sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + (istuhondan ? " và 1 Tiên đan" : ""),
                                "redeem"
                        );
            }
        }
    }

    // ============================ VU LAN (viet lai) - NPC Dau Truong (npcType -31) ============================
    //
    // Doi cong hoan toan noi dung cu (doMenuVuLan2015 - nhiem vu tim con Me hien 2015, da
    // XOA). NPC Dau Truong that su (-31, xem RequestNPCInfoHandler/MapDoMenu case -31) noi
    // them 1 muc "Su kien Vu Lan" o CUOI menu goc khi su kien bat (khong dung cham gi toi
    // logic dau truong/tran yeu tran/nui chau bau cu). Tat ca cac idMenu duoi day chi dung
    // RIENG trong pham vi npcType -31, phai nam trong doan [MENU_VU_LAN_DAU_TRUONG_GOC..
    // MENU_VU_LAN_MAX] de MapDoMenu.doMenu nhan dung va chan TRUOC bang remap
    // VI_TRI_MENU_DAU_TRUONG (xem chu thich o do).
    static final int MENU_VU_LAN_DAU_TRUONG_GOC = 60;
    static final int MENU_VU_LAN_DOI_HOA = 61;
    static final int MENU_VU_LAN_DOI_NGUYEN_LIEU = 62;
    static final int MENU_VU_LAN_DOI_NGUYEN_LIEU_LOAI_BASE = 63; // +tier(0..3) = 63..66
    static final int MENU_VU_LAN_MAX = 66;

    static void doMenuDauTruongVuLan(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == MENU_VU_LAN_DAU_TRUONG_GOC) {
            if (idOptionMenu < 0) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Đổi bó hoa", "Đổi nguyên liệu"}, idNpc, MENU_VU_LAN_DAU_TRUONG_GOC));
            } else if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgMenu(
                        new String[]{
                                "10 nhánh hoa hồng vàng + 15 lượng -> 1 bó hoa vàng",
                                "50 nhánh hoa hồng vàng + " + TeamServer.giaXuBoHoaDoVuLan + " xu -> 1 bó hoa đỏ",
                                "30 hoa hồng đỏ -> 1 bó hoa đỏ"
                        },
                        idNpc, MENU_VU_LAN_DOI_HOA
                ));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgMenu(
                        new String[]{
                                "30 hoa hồng vàng + 60 lượng -> 3 NLSC 6 tự chọn",
                                "30 hoa hồng vàng + 80 lượng -> 5 NLSC 6 tự chọn",
                                "30 hoa hồng vàng + 110 lượng -> 7 NLSC 6 tự chọn",
                                "30 hoa hồng vàng + 130 lượng -> 10 NLSC 6 tự chọn"
                        },
                        idNpc, MENU_VU_LAN_DOI_NGUYEN_LIEU
                ));
            }
            return;
        }
        if (idMenu == MENU_VU_LAN_DOI_HOA) {
            doVuLanDoiHoa(player, idOptionMenu);
            return;
        }
        if (idMenu == MENU_VU_LAN_DOI_NGUYEN_LIEU) {
            if (idOptionMenu < 0 || idOptionMenu > 3) {
                return;
            }
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];
            for (int i = 0; i < mmenu.length; i++) {
                mmenu[i] = gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
            }
            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, MENU_VU_LAN_DOI_NGUYEN_LIEU_LOAI_BASE + idOptionMenu));
            return;
        }
        if (idMenu >= MENU_VU_LAN_DOI_NGUYEN_LIEU_LOAI_BASE && idMenu <= MENU_VU_LAN_MAX) {
            int tier = idMenu - MENU_VU_LAN_DOI_NGUYEN_LIEU_LOAI_BASE;
            doVuLanDoiNguyenLieu(player, tier, idOptionMenu);
        }
    }

    private static void doVuLanDoiHoa(Char player, int idOptionMenu) {
        if (idOptionMenu == 0) {
            if (player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG] < 10) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần đủ 10 nhánh hoa hồng vàng", ""));
                return;
            }
            if (player.getLuong() < 15L) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần đủ 15 lượng", ""));
                return;
            }
            player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG] -= 10;
            player.subLuong(15L);
            player.potions[Potion.TYPE_BO_HOA_VANG]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được 1 bó hoa vàng", ""));
            Database.instance.saveOrtherLog("", player.charname, "doi 10 nhanh hoa hong vang + 15 luong lay 1 bo hoa vang", "vulan");
        } else if (idOptionMenu == 1) {
            long gia = TeamServer.giaXuBoHoaDoVuLan;
            if (player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG] < 50) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần đủ 50 nhánh hoa hồng vàng", ""));
                return;
            }
            if (player.getxu() < gia) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần đủ " + gia + " xu", ""));
                return;
            }
            player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG] -= 50;
            player.subXu(gia, true, "map dautruong vulan");
            player.potions[Potion.TYPE_BO_HOA_DO]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được 1 bó hoa đỏ", ""));
            Database.instance.saveOrtherLog("", player.charname, "doi 50 nhanh hoa hong vang + " + gia + " xu lay 1 bo hoa do", "vulan");
        } else if (idOptionMenu == 2) {
            if (player.potions[Potion.TYPE_HOA_HONG_DO] < 30) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn cần đủ 30 hoa hồng đỏ", ""));
                return;
            }
            player.potions[Potion.TYPE_HOA_HONG_DO] -= 30;
            player.potions[Potion.TYPE_BO_HOA_DO]++;
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được 1 bó hoa đỏ", ""));
            Database.instance.saveOrtherLog("", player.charname, "doi 30 hoa hong do lay 1 bo hoa do", "vulan");
        }
    }

    private static void doVuLanDoiNguyenLieu(Char player, int tier, int idOptionMenu) {
        if (idOptionMenu < 0 || idOptionMenu >= GemTemplate.ID_MATERIAL_LOW[5].length) {
            return;
        }
        int[] sl = new int[]{3, 5, 7, 10};
        int[] price = new int[]{60, 80, 110, 130};
        final int caudoiHoaHongVang = 30;
        if (player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG] < caudoiHoaHongVang) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ " + caudoiHoaHongVang + " hoa hồng vàng mới có thể đổi được " + sl[tier] + " nguyên liệu", ""));
            return;
        }
        if (player.getLuong() < price[tier]) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[tier] + " lượng mới có thể đổi được " + sl[tier] + " nguyên liệu", ""));
            return;
        }
        player.potions[Potion.TYPE_NHANH_HOA_HONG_VANG] -= caudoiHoaHongVang;
        player.subLuong((long) price[tier]);
        int idNlsc = GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu];
        player.doAddGemItem(idNlsc, sl[tier], true);
        String info = "Bạn nhận được " + sl[tier] + " " + gemTemplate[idNlsc].name + " khóa";
        // Ngau nhien them 1 manh Bat Gioi/Sa Tang (thay cho Ngo Khong/Duong Tang - chua co
        // trong DB, theo quyet dinh khi lam yeu cau), ti le tang dan theo moc doi cao hon.
        int[] tlManh = new int[]{10, 20, 30, 50};
        if (r.nextInt(100) < tlManh[tier]) {
            int idManh = 848 + r.nextInt(871 - 848 + 1);
            player.doAddGemItem(idManh, 1, true);
            info = info + ", và ngẫu nhiên 1 " + gemTemplate[idManh].name + " khóa";
        }
        player.sendMessage(MessageCreator.createCharGemItem(player));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        Database.instance.saveOrtherLog("", player.charname, info, "vulan_doinl");
    }

    static void doMenuNpcTet2015(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.potions[117] < 1) {
                    return;
                }

                if (player.getxu() < 3000L) {
                    return;
                }

                player.sendMessage(MessageCreator.createMsgInputText(-32017, 0, "Số bánh tét", 1));
            } else if (idOptionMenu == 1) {
                if (player.potions[117] < 2) {
                    return;
                }

                if (player.getxu() < 6000L) {
                    return;
                }

                player.sendMessage(MessageCreator.createMsgInputText(-32018, 0, "Số bánh chưng", 1));
            } else if (idOptionMenu == 2) {
                if (player.potions[118] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh tét", ""));
                    return;
                }

                if (player.getLuong() < 80) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"áo dài nam", "áo dài nữ"}, idNpc, 1));
            } else if (idOptionMenu == 3) {
                if (player.potions[120] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh chưng", ""));
                    return;
                }

                if (player.getLuong() < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                player.subLuong(100L);
                player.potions[120] = player.potions[120] - 50;
                player.createPet(14, 43200);
            } else if (idOptionMenu == 4) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"Hộp mứt nhỏ", "Hộp mứt to"}, idNpc, 2));
            } else if (idOptionMenu == 5) {
                if (player.potions[121] < 1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ thỏi vàng", ""));
                    return;
                }

                player.potions[142]++;
                player.potions[121]--;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi được 1 hộp quà tết", ""));
            } else if (idOptionMenu == 6) {
                player.sendMessage(MessageCreator.createMsgInputText(-32019, 0, "Số câu đối", 1));
            } else if (idOptionMenu == 7) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 3));
            } else if (idOptionMenu == 8) {
                String[] menu = Char.getTopListCharNapLuong();
                if (!menu.equals("")) {
                    player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1000));
                }
            }
        } else if (idMenu == 1) {
            if (player.potions[118] < 50) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh tét", ""));
                return;
            }

            if (player.getLuong() < 80) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                return;
            }

            player.potions[118]--;
            player.subLuong(80L);
            doBuyModelClothe(player, getShopTemplate(idOptionMenu == 0 ? 106 : 105), 30);
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi được 1 bộ đồ thời trang", ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        } else if (idMenu == 2) {
            if (player.potions[122] < (idOptionMenu == 0 ? 200 : 350)) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ dưa hấu", ""));
                return;
            }

            player.potions[idOptionMenu == 0 ? 125 : 119]++;
            player.potions[122] = player.potions[122] - (idOptionMenu == 0 ? 200 : 350);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đổi được một " + (idOptionMenu == 0 ? "hộp mứt nhỏ" : "Hộp mứt to"), ""));
        } else if (idMenu == 3) {
            int[] sl = new int[]{3, 5, 7, 10};
            int[] slCaudoi = new int[]{7, 10, 14, 18};
            int[] var10000 = new int[]{70, 100, 140, 180};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoi[idOptionMenu] + " câu đối)";
                mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 4));
        } else if (idMenu == 4 || idMenu == 5 || idMenu == 6 || idMenu == 7) {
            int[] sl = new int[]{0, 0, 0, 0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 0, 0, 0, 7, 10, 14, 18};
            int[] var14 = new int[]{0, 0, 0, 0, 70, 100, 140, 180};
            if (player.potions[74] < slCaudoi[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slCaudoi[idMenu] + " câu đối mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            player.potions[74] = player.potions[74] - slCaudoi[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
        }
    }

    static void doMenuNpcNoel(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (!Char.isFinishAllSuKienNoel()) {
            if (idMenu == 0) {
                if (idOptionMenu == 0) {
                    if (player.potions[71] < 10) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kẹo", ""));
                        return;
                    }

                    if (player.getxu() < 3000L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                        return;
                    }

                    addXpCharEvent(player, 50000L, false, "doMenuNpcNoel1");
                    player.subXu(3000L, true, "map 62");
                    player.potions[71] = player.potions[71] - 10;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Database.instance.saveOrtherLog("", player.charname, "Nhan 50k exp", "50kexp");
                } else if (idOptionMenu == 1) {
                    if (player.potions[71] < 20) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kẹo", ""));
                        return;
                    }

                    if (player.getxu() < 6000L) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                        return;
                    }

                    addXpCharEvent(player, 120000L, false, "doMenuNpcNoel2");
                    player.subXu(6000L, true, "map 61");
                    player.potions[71] = player.potions[71] - 20;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    Database.instance.saveOrtherLog("", player.charname, "Nhan 120k exp", "120kexp");
                } else if (idOptionMenu == 2) {
                    player.sendMessage(MessageCreator.createMsgInputText(-32015, idMenu, "Nhập số túi đổi", 1));
                } else if (idOptionMenu == 3) {
                    player.sendMessage(MessageCreator.createMsgInputText(-32016, idMenu, "Nhập số túi đổi", 1));
                } else if (idOptionMenu == 4) {
                    if (player.getLuong() < 300) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    player.subLuong(300L);
                    player.createTuanLoc(15L);
                } else if (idOptionMenu == 5) {
                    if (player.getLuong() < 500) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                        return;
                    }

                    player.subLuong(500L);
                    player.createTuanLoc(30L);
                } else if (idOptionMenu == 6) {
                    if (!player.gifMoonFestival) {
                        return;
                    }

                    player.gifMoonFestival = false;
                    player.doReceiveGif35Offline(0);
                } else if (idOptionMenu == 7) {
                    if (!player.gifMoonFestival) {
                        return;
                    }

                    player.gifMoonFestival = false;
                    player.doReceiveGif35Offline(1);
                }
            } else if (idMenu == 1) {
                if (player.potions[117] <= 0) {
                    return;
                }

                int nqua = r.nextInt(3);
                String info = "";
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], player.slNgLieuDoi, true);
                info = player.slNgLieuDoi + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name;
                if (nqua > 0) {
                    Vector<Potion> pt = new Vector();
                    pt.add(new Potion((short) 13, 1, null));
                    pt.add(new Potion((short) 35, 1, null));
                    if (player.potions[94] < 30000) {
                        pt.add(new Potion((short) 94, r.nextInt(51) + 50, null));
                    }

                    if (player.potions[96] < 30000) {
                        pt.add(new Potion((short) 96, r.nextInt(51) + 50, null));
                    }

                    pt.add(new Potion((short) 97, r.nextInt(8) + 3, null));

                    for (boolean isok = false; nqua > 0 && pt.size() > 0; nqua--) {
                        if ((r.nextInt(100) >= 5 || isok) && pt.size() != 1) {
                            int index = r.nextInt(pt.size() - 1) + 1;
                            if (isok) {
                                index = r.nextInt(pt.size());
                            }

                            Potion po = (Potion) pt.remove(index);
                            player.potions[po.getType()] += po.quantity;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            info = info + "," + po.quantity + " " + LoginHandler.PORTION_NAME[po.getType()];
                        } else {
                            isok = true;
                            Potion po = (Potion) pt.remove(0);
                            player.potions[po.getType()] += po.quantity;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            info = info + "," + LoginHandler.PORTION_NAME[po.getType()];
                        }
                    }
                }

                player.slNgLieuDoi = 0;
                player.potions[117]--;
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được " + info, ""));
                Database.instance.saveOrtherLog("", player.charname, "Chúc mừng bạn nhận được " + info, "otuinho");
            } else if (idMenu == 2) {
                if (player.potions[118] <= 0) {
                    return;
                }

                int nqua = r.nextInt(3);
                String info = "";
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], player.slNgLieuDoi, true);
                info = gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name;
                if (nqua > 0) {
                    Vector<Potion> ptx = new Vector();
                    ptx.add(new Potion((short) 8, 1, null));
                    ptx.add(new Potion((short) 75, 1, null));
                    if (player.potions[94] < 30000) {
                        ptx.add(new Potion((short) 94, r.nextInt(51) + 50, null));
                    }

                    if (player.potions[96] < 30000) {
                        ptx.add(new Potion((short) 96, r.nextInt(51) + 50, null));
                    }

                    ptx.add(new Potion((short) 116, r.nextInt(8) + 3, null));

                    for (boolean isokx = false; nqua > 0 && ptx.size() > 0; nqua--) {
                        if ((r.nextInt(100) >= 5 || isokx) && ptx.size() != 1) {
                            int index = r.nextInt(ptx.size() - 1) + 1;
                            if (isokx) {
                                index = r.nextInt(ptx.size());
                            }

                            Potion po = (Potion) ptx.remove(index);
                            player.potions[po.getType()] += po.quantity;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            info = info + "," + po.quantity + " " + LoginHandler.PORTION_NAME[po.getType()];
                        } else {
                            isokx = true;
                            Potion po = (Potion) ptx.remove(0);
                            player.potions[po.getType()] += po.quantity;
                            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                            info = info + "," + LoginHandler.PORTION_NAME[po.getType()];
                        }
                    }
                }

                player.slNgLieuDoi = 0;
                player.potions[118]--;
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được " + info, ""));
                Database.instance.saveOrtherLog("", player.charname, "Chúc mừng bạn nhận được " + info, "otuilon");
            }
        }
    }


    static void doMenuNpcVaoHa(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        try {
            if (idMenu == 0) {
                if (idOptionMenu == 0) {
                    player.sendMessage(MessageCreator.createMsgInputText(-32015, idMenu, "Nhập số lượng đổi", 1));
                } else if (idOptionMenu < 5) {
                    int[] sl = new int[]{0, 3, 5, 7, 10};
                    int[] slkem = new int[]{0, 20, 30, 40, 50};
                    int[] price = new int[]{0, 70, 100, 130, 180};
                    if (player.getLuong() < price[idOptionMenu]) {
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn phải có đủ " + price[idOptionMenu] + " lượng mới có thể đổi được " + sl[idOptionMenu] + " nguyên liệu", ""
                                )
                        );
                        return;
                    }

                    if (player.potions[116] < slkem[idOptionMenu]) {
                        player.sendMessage(
                                MessageCreator.createServerAlertMessage(
                                        "Bạn phải có đủ " + slkem[idOptionMenu] + " kem mới có thể đổi được " + sl[idOptionMenu] + " nguyên liệu", ""
                                )
                        );
                        return;
                    }

                    String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                    for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                        mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa";
                    }

                    player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu));
                } else if (idOptionMenu == 5) {
                    if (player.potions[117] < 1
                            || player.potions[118] < 1
                            || player.potions[119] < 1
                            || player.potions[125] < 1
                            || player.potions[121] < 1
                            || player.potions[122] < 1) {
                        player.sendMessage(MessageCreator.createServerAlertMessage("Bạn chưa tập hợp đủ chữ HE 2014", ""));
                        return;
                    }

                    player.potions[35]++;
                    player.potions[117]--;
                    player.potions[118]--;
                    player.potions[119]--;
                    player.potions[125]--;
                    player.potions[121]--;
                    player.potions[122]--;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi được 1 hộp quà", ""));
                    Database.instance.saveOrtherLog("", player.charname, player.potions[35] + " doi duoc 1 hop qua", "quahe");
                } else if (idOptionMenu == 6) {
                    if (player.giftTop == 0) {
                        return;
                    }

                    if (player.giftTop != 3) {
                        String[] menu = new String[]{"Phi phong ma pháp", "Phi phong vật lý"};
                        player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 10));
                    } else {
                        player.selectPhiphong = (byte) idOptionMenu;
                        String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                            mmenu[i] = "5 " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
                        }

                        player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, 11));
                    }
                }
            } else if (idMenu < 5) {
                int[] slx = new int[]{0, 3, 5, 7, 10};
                int[] slkemx = new int[]{0, 20, 30, 40, 50};
                int[] pricex = new int[]{0, 70, 100, 130, 180};
                if (player.getLuong() < pricex[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + pricex[idMenu] + " lượng mới có thể đổi được " + slx[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                if (player.potions[116] < slkemx[idMenu]) {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slkemx[idMenu] + " kem mới có thể đổi được " + slx[idMenu] + " nguyên liệu", "")
                    );
                    return;
                }

                player.potions[116] = player.potions[116] - slkemx[idMenu];
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], slx[idMenu], true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.subLuong((long) pricex[idMenu]);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(
                        MessageCreator.createServerAlertMessage(
                                "Bạn nhận được " + slx[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                        )
                );
                Database.instance.saveOrtherLog("", player.charname, slx[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
            } else if (idMenu == 10) {
                if (player.giftTop == 0) {
                    return;
                }

                player.selectPhiphong = (byte) idOptionMenu;
                String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                    mmenu[i] = "5 " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, 11));
            } else if (idMenu == 11) {
                if (player.giftTop == 0) {
                    return;
                }

                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], 5, false);
                player.doAddGemItem(178, 5, false);
                String info = player.giftTop + "_" + "Nhan " + 5 + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " 5 đá tách đặc biệt";
                if (player.giftTop == 1 || player.giftTop == 2 || player.giftTop == 4) {
                    Item it = ItemLuckyDraw.createMaxItemCoatLucky(608, player, player.selectPhiphong, true);
                    it.dateCreate = Char.getDayOpen(0L);
                    player.iItems.add(it);
                    Database.instance.saveOrtherLog("", player.charname, it.getName() + "_" + it.getAttribute() + "|" + it.getDBInfo(), "quatop");
                }

                if (player.giftTop == 1 || player.giftTop == 4) {
                    player.doAddGemItem(66, 5, false);
                    info = info + ", 5 lkd 35";
                }

                if (player.giftTop == 4) {
                    info = info + ", 1 truong phuong hoang";
                    player.potions[86]++;
                }

                Database.instance.saveOrtherLog("", player.charname, info, "quatop");
                player.giftTop = 0;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createCharGemItem(player));
                Database.instance.updateRcvGiftTop(player.charname);
                player.sendMessage(MessageCreator.createServerAlertMessage("Đã nhận quà" + info, ""));
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }

    static void addGifEventGioTo(Map map, Char p) {
        int percent = r.nextInt(100);
        String info = "";
        if (percent >= 0) {
            if (percent < 80) {
                int xpRan = 0;
                int totalXp = 0;
                if (percent < 90) {
                    xpRan = r.nextInt(450000) + 1000;
                    addXpCharEvent(p, (long) (50000 + xpRan), false, "addGifEventGioTo1");
                    totalXp = 50000 + xpRan;
                } else if (percent < 95) {
                    xpRan = r.nextInt(1000000) + 1000;
                    addXpCharEvent(p, (long) (500000 + xpRan), false, "addGifEventGioTo2");
                    totalXp = 500000 + xpRan;
                } else if (percent <= 97) {
                    xpRan = r.nextInt(1500000) + 1000;
                    addXpCharEvent(p, (long) (1500000 + xpRan), false, "addGifEventGioTo3");
                    totalXp = 1500000 + xpRan;
                } else if (percent < 100) {
                    xpRan = r.nextInt(2000000) + 1000;
                    addXpCharEvent(p, (long) (3000000 + xpRan), false, "addGifEventGioTo3");
                    totalXp = 3000000 + xpRan;
                }

                Database.instance.saveOrtherLog("", p.charname, "nhận được " + totalXp + " exp", "giftgioto3");
                p.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + totalXp + " exp", ""));
            } else if (percent < 90) {
                giveItemAnimalGioto(map, p, (byte) r.nextInt(2));
            } else if (percent <= 95) {
                int idgem = r.nextInt(18) + 137;
                int n = r.nextInt(100) < 3 ? 2 : 1;
                p.doAddGemItem(idgem, n, true);
                info = "Nhận được " + n + " " + gemTemplate[idgem].name + " khóa.";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                Database.instance.saveOrtherLog("", p.charname, info, "giftgioto2");
            } else if (percent < 100) {
                int[] idGemTemplate = new int[]{
                    68, 69, 70, 71, 72, 73, 75, 76, 77, 78, 79, 80, 82, 83, 84, 85, 86, 87, 89, 90, 91, 92, 93, 94, 96, 97, 98, 99, 100, 101
                };
                int idgem = idGemTemplate[r.nextInt(idGemTemplate.length)];
                int n = r.nextInt(100) < 3 ? 2 : 1;
                p.doAddGemItem(idgem, n, true);
                info = "Nhận được " + n + " " + gemTemplate[idgem].name + " khóa.";
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
                Database.instance.saveOrtherLog("", p.charname, info, "giftgioto2");
            }
        }
    }

    static void giveItemAnimalGioto(Map map, Char player, byte magic_physic) {
        short[] idTemplate = new short[]{509, 521, 533, 545, 557};
        int lv = player.lvDetail.lv / 5 * 5;
        if (player.lvDetail.lv < 30) {
            lv = 30;
        }

        int start = (lv - 30) / 5;
        byte kindItem = 0;
        int i = r.nextInt(idTemplate.length);

        for (byte j = 0; j < 1; j++) {
            ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(new Integer(idTemplate[i] + start));
            Item newItem = new Item(template, false, kindItem == -1 ? template.clazz : kindItem, kindItem == -1 ? template.clazz : kindItem, idTemplate[i] + start);
            newItem.id = player.getIDItem();
            newItem.owner = player.charDBID;
            newItem.level = newItem.getTemplate().level;
            player.iItems.add(newItem);
            newItem.identify = player.charDBID;
            newItem.clazz = kindItem;
            newItem.magic_physic = magic_physic;
            if (newItem.magic_physic < 0) {
                newItem.magic_physic = 2;
            }

            if (magic_physic == 0) {
                newItem.atb[6] = newItem.atb[1];
                newItem.atb[1] = (short) (newItem.atb[1] / 10);
            } else if (magic_physic == 1) {
                newItem.atb[6] = (short) (newItem.atb[1] / 10);
            }

            newItem.dateCreate = Char.getDayTime(0L);
        }

        player.sendMessage(MessageCreator.createServerAlertMessage("Nhận được trang bị linh thú", ""));
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        Database.instance.saveOrtherLog("", player.charname, "lv : " + lv + "_" + start, "giftGioto1");
    }

}
