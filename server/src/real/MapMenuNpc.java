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
 * Menu hoi thoai cua cac NPC rieng le: Hang Nga, Vua Hung, Mua He, Dracula,
 * Nhan Tieu, Halloween 2015, sinh nhat Ngu De, event mini.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 *
 * Khac real/MapSuKien.java: file do gom NPC su kien theo mua (Tet, Noel,
 * Trung Thu, Gio To, Vu Lan) tach o buoc truoc.
 */
// Thanh vien static cua Map ma nhom nay dung khong dinh danh khi con nam trong Map.
import static real.Map.addXpCharEvent;
import static real.Map.doBuyModelClothe;
import static real.Map.gemTemplate;
import static real.Map.getShopTemplate;
import static real.Map.nameTopNapLuong;
import static real.Map.openLog;
import static real.Map.openvantieu;
import static real.Map.r;

class MapMenuNpc {

    static void doMenuEventMini(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (Char.isFinishAllSuKienMini()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Sự kiện đã kết thúc.", ""));
        } else {
            if (idMenu == 0) {
                if (idOptionMenu == 3) {
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"1 Ngày", "2 Ngày", "3 Ngày", "4 Ngày", "5 Ngày", "6 Ngày", "7 Ngày"}, idNpc, 10));
                } else if (idOptionMenu == 4) {
                    player.sendMessage(MessageCreator.createMsgMenu(new String[]{"1 Ngày", "2 Ngày", "3 Ngày", "4 Ngày", "5 Ngày", "6 Ngày", "7 Ngày"}, idNpc, 11));
                } else if (idOptionMenu == 5) {
                    for (int i = 0; i < GemTemplate.ID_NGOC_RONG.length; i++) {
                        if (player.listGemitemLock[GemTemplate.ID_NGOC_RONG[i]] <= 0) {
                            player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngọc", ""));
                            return;
                        }
                    }

                    player.doDoiQuaDacBietNoel();
                }
            } else if (idMenu == 10) {
                if (player.listGemitemLock[GemTemplate.ID_NGOC_RONG[idOptionMenu]] <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngọc", ""));
                    return;
                }

                player.delGem(GemTemplate.ID_NGOC_RONG[idOptionMenu], 1, true);
                Item it = doBuyModelClothe(player, 725, idOptionMenu + 1, 1);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi 1 " + it.getName(), ""));
            } else if (idMenu == 11) {
                if (player.listGemitemLock[GemTemplate.ID_NGOC_RONG[idOptionMenu]] <= 0) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ ngọc", ""));
                    return;
                }

                player.delGem(GemTemplate.ID_NGOC_RONG[idOptionMenu], 1, true);
                Item it = doBuyModelClothe(player, 726, idOptionMenu + 1, 1);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi 1 " + it.getName(), ""));
            }
        }
    }

    static void doMenuSinhNhatNgude(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, 0, "Số lượng bánh", 1));
            } else if (idOptionMenu == 1 || idOptionMenu == 2) {
                if (player.potions[77] < 50) {
                    return;
                }

                if (player.getxu() < 500000L) {
                    return;
                }

                player.subXu(500000L, true, "map 49");
                player.potions[77] = player.potions[77] - 50;
                doBuyModelClothe(player, getShopTemplate(idOptionMenu == 1 ? 139 : 140), 30);
                Database.instance.saveOrtherLog("", player.charname, "doi do thoi trang " + idOptionMenu, "doitt");
            }
        }
    }

    static void doMenuHaloween2015(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.luongNap < 200L) {
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                    return;
                }

                player.luongNap -= 200L;
                Item newItem = ItemLuckyDraw.createMaxItemCoatLucky(616, player, 0);
                newItem.dateCreate = Char.getDayTime(0L);
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = 10080;
                player.iItems.add(newItem);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được phi phong lông thú thời hạn 10080 phút", ""));
                Database.instance.saveOrtherLog("", player.charname, "doi pp long thu ma", "pp");
            } else if (idOptionMenu == 1) {
                if (player.luongNap < 200L) {
                    return;
                }

                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                    return;
                }

                player.luongNap -= 200L;
                Item newItem = ItemLuckyDraw.createMaxItemCoatLucky(616, player, 1);
                newItem.dateCreate = Char.getDayTime(0L);
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = 10080;
                player.iItems.add(newItem);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được phi phong lông thú thời hạn 10080 phút", ""));
                Database.instance.saveOrtherLog("", player.charname, "doi pp long thu vat", "pp");
            } else if (idOptionMenu == 2) {
                if (player.luongNap < 20L) {
                    return;
                }

                player.sendMessage(MessageCreator.createMsgInputText(-32015, idMenu, "Nhập số vé(" + player.luongNap + ")", 1));
            } else if (idOptionMenu == 3) {
                String[] menu = new String[]{"Freddy", "Jason", "Harry potter", "Voldemort"};
                player.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 1));
            }
        } else if (idMenu == 1) {
            if (player.luongNap < 200L) {
                return;
            }

            if (player.potions[74] < 5) {
                return;
            }

            ShopTemplate item = getShopTemplate(142 + idOptionMenu);
            if (item != null) {
                player.luongNap -= 200L;
                player.potions[74] = player.potions[74] - 5;
                doBuyModelClothe(player, item, 10);
                Database.instance.saveOrtherLog("", player.charname, "doi do thoi trang " + idOptionMenu, "dothoitrang");
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã đổi thành công đồ thời trang " + item.name, ""));
            }
        }
    }

    static void domenuNpcMuaHe(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.potions[72] < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kem", ""));
                    return;
                }

                if (player.getLuong() < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"áo băng giá nam", "áo băng giá nữ"}, idNpc, 1));
            } else if (idOptionMenu == 1) {
                if (player.potions[72] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kem", ""));
                    return;
                }

                if (player.getLuong() < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                player.potions[72] = player.potions[72] - 50;
                player.subLuong(100L);
                player.createPet(15, 43200);
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 2));
            } else if (idOptionMenu == 3) {
                if (nameTopNapLuong.equals("")) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hiện tại chưa có nhân vật nào nạp đủ " + (openLog ? "10k" : "8k") + " lượng.", ""));
                } else {
                    player.sendMessage(
                            MessageCreator.createServerAlertMessage("Nhân vật nạp đủ " + (openLog ? "10k" : "8k") + " lượng nhanh nhất là " + nameTopNapLuong + ".", "")
                    );
                }
            } else if (idOptionMenu == 4) {
                int nve = player.diemdoivePH / 70;
                if (nve > 0) {
                    Database.instance.saveOrtherLog("", player.charname, player.diemdoivePH + " doi dc " + nve + " phuong hoang", "veph");
                    player.potions[118] = player.potions[118] + nve;
                    player.diemdoivePH -= nve * 70;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đổi được " + nve + " vé phượng hoàng", ""));
                }
            }
        } else if (idMenu == 1) {
            if (player.potions[72] < 100) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh", ""));
                return;
            }

            if (player.getLuong() < 100) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                return;
            }

            player.potions[72] = player.potions[72] - 100;
            player.subLuong(100L);
            doBuyModelClothe(player, getShopTemplate(idOptionMenu == 0 ? 137 : 138), 30);
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi được 1 bộ đồ thời trang", ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        } else if (idMenu == 2) {
            int[] sl = new int[]{3, 5, 7, 10};
            int[] slCaudoi = new int[]{5, 10, 20, 40};
            int[] var10000 = new int[]{70, 100, 140, 180};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoi[idOptionMenu] + " dưa hấu)";
                mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 3));
        } else if (idMenu == 3 || idMenu == 4 || idMenu == 5 || idMenu == 6) {
            int[] sl = new int[]{0, 0, 0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 0, 0, 5, 10, 20, 40};
            int[] price = new int[]{0, 0, 0, 70, 100, 140, 180};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[74] < slCaudoi[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slCaudoi[idMenu] + " dưa hấu mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            player.subLuong((long) price[idMenu]);
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

    static void domenuNpcVuaHung(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.potions[74] < 5 || player.potions[72] < 5 || player.potions[77] < 1) {
                    return;
                }

                player.sendMessage(MessageCreator.createMsgInputText(-32017, 0, "Số bánh chưng", 1));
            } else if (idOptionMenu == 1) {
                if (player.potions[74] < 5) {
                    return;
                }

                if (player.getxu() < 1000L) {
                    return;
                }

                player.sendMessage(MessageCreator.createMsgInputText(-32018, 0, "Số bánh giày", 1));
            } else if (idOptionMenu == 2) {
                if (player.potions[120] < 50 || player.potions[118] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh", ""));
                    return;
                }

                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"áo lạc long quân", "áo yếm đào"}, idNpc, 1));
            } else if (idOptionMenu == 3) {
                if (player.potions[120] < 50 || player.potions[118] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh", ""));
                    return;
                }

                player.potions[120] = player.potions[120] - 50;
                player.potions[118] = player.potions[118] - 50;
                player.createPet(6, 43200);
            } else if (idOptionMenu == 4) {
                player.sendMessage(MessageCreator.createMsgMenu(new String[]{"3 ng.liệu sc 6", "5 ng.liệu sc 6", "7 ng.liệu sc 6", "10 ng.liệu sc 6"}, idNpc, 2));
            } else if (idOptionMenu == 5) {
                if (player.potions[117] < 1) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ sao bạc", ""));
                    return;
                }

                if (player.getLuong() < 15) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                    return;
                }

                player.potions[117]--;
                player.subLuong(15L);
                int idng = GemTemplate.ID_MATERIAL_LOW[5][r.nextInt(GemTemplate.ID_MATERIAL_LOW[5].length)];
                int sl = r.nextInt(3) + 1;
                player.doAddGemItem(idng, sl, true);
                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + sl + " " + gemTemplate[idng].name + " khóa", ""));
                Database.instance.saveOrtherLog("", player.charname, sl + "_" + gemTemplate[idng].name, "redeem");
            }
        } else if (idMenu == 1) {
            if (player.potions[120] < 50 || player.potions[118] < 50) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ bánh", ""));
                return;
            }

            player.potions[118] = player.potions[118] - 50;
            player.potions[120] = player.potions[120] - 50;
            doBuyModelClothe(player, getShopTemplate(idOptionMenu == 0 ? 51 : 56), 30);
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã đổi được 1 bộ đồ thời trang", ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
        } else if (idMenu == 2) {
            int[] sl = new int[]{3, 5, 7, 10};
            int[] slCaudoi = new int[]{1, 1, 1, 1};
            int[] var10000 = new int[]{70, 100, 140, 180};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slCaudoi[idOptionMenu] + " sao bạc)";
                mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 3));
        } else if (idMenu == 3 || idMenu == 4 || idMenu == 5 || idMenu == 6) {
            int[] sl = new int[]{0, 0, 0, 3, 5, 7, 10};
            int[] slCaudoi = new int[]{0, 0, 0, 1, 1, 1, 1};
            int[] price = new int[]{0, 0, 0, 70, 100, 140, 180};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[117] < slCaudoi[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slCaudoi[idMenu] + " bột mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            player.subLuong((long) price[idMenu]);
            player.potions[117] = player.potions[117] - slCaudoi[idMenu];
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

    static void doMenuNpcNhanTieu(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (!openvantieu) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng đang bảo trì", ""));
        } else {
            try {
                Calendar cal = Calendar.getInstance();
                int ihour = cal.get(11);
                if ((ihour < 9 || ihour >= 21) && TeamServer.server != 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có thể nhận tiêu từ 9h đến 21h hàng ngày", ""));
                    return;
                }

                if (player.lvDetail.lv < 40) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng chỉ dành cho nhân vật từ cấp độ 40 trở lên", ""));
                    return;
                }

                if (player.myCountry != player.inCountry) {
                    return;
                }

                if (player.map.mapIDLoadMap != 0 && player.map.mapIDLoadMap != 70 && player.map.mapIDLoadMap != 80) {
                    return;
                }

                if (idMenu == 0) {
                    Char.doInitMonter(player, idOptionMenu);
                }
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }
    }

    static void doMenuNpcDracula(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                if (player.potions[71] < 10) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kẹo", ""));
                    return;
                }

                if (player.getxu() < 10000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    return;
                }

                addXpCharEvent(player, 50000L, false, "doMenuNpcDracula1");
                player.subXu(10000L, true, "map 63");
                player.potions[71] = player.potions[71] - 10;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "Nhan 50k exp", "50kexp");
            } else if (idOptionMenu == 1) {
                if (player.potions[71] < 20) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kẹo", ""));
                    return;
                }

                if (player.getxu() < 20000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    return;
                }

                addXpCharEvent(player, 120000L, false, "doMenuNpcDracula2");
                player.subXu(20000L, true, "map 59");
                player.potions[71] = player.potions[71] - 20;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "Nhan 120k exp", "120kexp");
            } else if (idOptionMenu == 2) {
                if (player.potions[71] < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ kẹo", ""));
                    return;
                }

                if (player.getxu() < 200000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                    return;
                }

                addXpCharEvent(player, 1500000L, false, "doMenuNpcDracula3");
                player.subXu(200000L, true, "map 60");
                player.potions[71] = player.potions[71] - 50;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                Database.instance.saveOrtherLog("", player.charname, "Nhan 1t5k exp", "1t5kexp");
            } else if (idOptionMenu == 3) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, idMenu, "Nhập số rương ma", 1));
            } else if (idOptionMenu == 4) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, idMenu, "Nhập số rương quỷ", 1));
            } else if (idOptionMenu == 5) {
                if (!player.gifMoonFestival) {
                    return;
                }

                player.gifMoonFestival = false;
                player.doReceiveGif35Offline(0);
            } else if (idOptionMenu == 6) {
                if (!player.gifMoonFestival) {
                    return;
                }

                player.gifMoonFestival = false;
                player.doReceiveGif35Offline(1);
            }
        }
    }

    static void doMenuNpcHangNga(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            if (idOptionMenu == 0) {
                player.sendMessage(MessageCreator.createMsgInputText(-32015, idMenu, "Nhập số bánh dẻo muốn đổi", 1));
            } else if (idOptionMenu == 1) {
                player.sendMessage(MessageCreator.createMsgInputText(-32016, idMenu, "Nhập số bánh nướng muốn đổi", 1));
            } else if (idOptionMenu == 2) {
                player.sendMessage(MessageCreator.createMsgInputText(-32017, idMenu, "Nhập số bánh nướng chay muốn đổi", 1));
            } else if (idOptionMenu == 3) {
                player.sendMessage(MessageCreator.createMsgInputText(-32018, idMenu, "Nhập số bánh nướng thập cẩm muốn đổi", 1));
            } else if (idOptionMenu == 4) {
                player.sendMessage(MessageCreator.createMsgInputText(-32019, idMenu, "Nhập số bánh nướng đặc biệt muốn đổi", 1));
            } else if (idOptionMenu == 5) {
                int[] sl = new int[]{3, 5, 7, 10};
                int[] var10000 = new int[]{20, 30, 40, 50};
                var10000 = new int[]{70, 100, 140, 180};
                String[] mmenu = new String[4];

                for (int i = 0; i < sl.length; i++) {
                    mmenu[i] = "Đổi " + sl[i] + " nguyên liệu";
                }

                player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, 1));
            } else if (idOptionMenu == 6) {
                if (player.potions[82] < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 100 tre mới có thể đổi lồng đèn", ""));
                    return;
                }

                if (player.potions[35] < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 100 giấy mới có thể đổi lồng đèn", ""));
                    return;
                }

                if (player.getLuong() < 50) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 50 lượng mới có thể đổi được lồng đèn cá chép", ""));
                    return;
                }

                player.subLuong(50L);
                player.potions[82] = player.potions[82] - 100;
                player.potions[35] = player.potions[35] - 100;
                player.createPet(12, 21600);
            } else if (idOptionMenu == 7) {
                if (player.potions[82] < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 100 tre mới có thể đổi lồng đèn", ""));
                    return;
                }

                if (player.potions[35] < 100) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 100 giấy mới có thể đổi lồng đèn", ""));
                    return;
                }

                if (player.getxu() < 5000000L) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn phải có đủ 5t xu mới có thể đổi được lồng đèn ngôi sao", ""));
                    return;
                }

                player.subXu(5000000L, true, "map 64");
                player.potions[82] = player.potions[82] - 100;
                player.potions[35] = player.potions[35] - 100;
                player.createPet(6, 7200);
            } else if (idOptionMenu == 8) {
                if (player.isFullInventory()) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đã đầy", ""));
                    return;
                }

                if (player.nBinhTra != 1) {
                    return;
                }

                player.nBinhTra = 0;
                player.potions[142]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận được hộp quà trung thu", ""));
            }
        } else if (idMenu == 1) {
            int[] sl = new int[]{3, 5, 7, 10};
            int[] slkem = new int[]{20, 30, 40, 50};
            int[] price = new int[]{70, 100, 140, 180};
            String[] mmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                String info = "(" + slkem[idOptionMenu] + " bánh dẻo " + price[idOptionMenu] + "l)";
                mmenu[i] = sl[idOptionMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name + " khóa" + info;
            }

            player.sendMessage(MessageCreator.createMsgMenu(mmenu, idNpc, idOptionMenu + 2));
        } else if (idMenu == 2 || idMenu == 3 || idMenu == 4 || idMenu == 5) {
            int[] sl = new int[]{0, 0, 3, 5, 7, 10};
            int[] slkem = new int[]{0, 0, 20, 30, 40, 50};
            int[] price = new int[]{0, 0, 70, 100, 140, 180};
            if (player.getLuong() < price[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + price[idMenu] + " lượng mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            if (player.potions[119] < slkem[idMenu]) {
                player.sendMessage(
                        MessageCreator.createServerAlertMessage("Bạn phải có đủ " + slkem[idMenu] + " bánh dẻo mới có thể đổi được " + sl[idMenu] + " nguyên liệu", "")
                );
                return;
            }

            player.potions[119] = player.potions[119] - slkem[idMenu];
            player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu], sl[idMenu], true);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.subLuong((long) price[idMenu]);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Bạn nhận được " + sl[idMenu] + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name + " khóa", ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, sl[idMenu] + "_" + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][idOptionMenu]].name, "redeem");
        }
    }

}
