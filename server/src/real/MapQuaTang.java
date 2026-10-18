package real;


import data.Animal;
import real.plugins.GiftCode;
import data.Database;
import data.GemItem;
import data.ItemLuckyDraw;
import data.Net;
import io.Message;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import real.cmd.LoginHandler;
import server.TeamServer;

/**
 * Qua tang va phan thuong: mo tui qua / hop qua cac loai, mo trung, vong quay
 * may man, trang doi thuong, qua cuoi, qua moc nap.
 *
 * Tach nguyen van tu real/Map.java. Deu la method PRIVATE cua Map nen khong
 * the bi goi tu ben ngoai -> chuyen ra day khong doi API cua Map.
 * Tham so "Map map" thay cho "this" truoc day.
 */
// Thanh vien static cua Map ma nhom nay dung khong dinh danh khi con nam trong Map.
import static real.Map.GIA_DOI_THUONG;
import static real.Map.MOC_NAP_GIA;
import static real.Map.TEN_DOI_THUONG;
import static real.Map.doBuyModelClothe;
import static real.Map.doCreateHuyetLinhThao;
import static real.Map.getNumberFromString;
import static real.Map.idMaterial13;
import static real.Map.addXPForChar;
import static real.Map.addXpCharEvent;
import static real.Map.gemTemplate;
import static real.Map.itemTemplates;
import static real.Map.r;
import static real.Map.sendAllCharServer;

class MapQuaTang {

    static void doOpenTrung(Map map, Char player) {
        addXpCharEvent(player, 100000L, false, "doopentrung");
        int[] per = new int[]{500, 1, 500, 500, 200, 200, 20, 500, 500, 100, 100, 100, 100, 100, 500, 500, 500, 200, 500, 500, 500, 500, 500, 10};
        int[] idgift = new int[]{
            82,
            9,
            93,
            95,
            GemTemplate.NGOC_HUYEN_MINH_1,
            GemTemplate.NGOC_HUYEN_MINH_2,
            GemTemplate.NGOC_HUYEN_MINH_3,
            676,
            677,
            -1,
            -1,
            -1,
            -1,
            -1,
            GemTemplate.BOT_TRANG,
            GemTemplate.BOT_XANH,
            GemTemplate.BOT_DO,
            GemTemplate.BOT_XANH_LA,
            116,
            100,
            114,
            113,
            112,
            721
        };
        int[] typegift = new int[]{4, 4, 4, 4, 6, 6, 6, 3, 3, -1, -1, -1, -1, -1, 6, 6, 6, 6, 4, 4, 4, 4, 4, 3};
        int[] soluongmax = new int[]{1, 1, 10, 10, 3, 3, 3, 1, 1, 10000, 20000, 30000, 40000, 50000, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 10000, 20000, 30000, 40000, 50000, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        int index = Char.getIndexRandom(per);
        if (typegift[index] == -1) {
            long aaa = (long) soluongmax[index];
            player.addXu(aaa, "map 5");
            info = info + soluongmax[index] + " xu";
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.doAddGemItem(idgift[index], soluong, r.nextInt(100) < 50);
            info = info + soluong + " " + gemTemplate[idgift[index]].name;
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            if (idgift[index] == 9) {
                try {
                    sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được nhân sâm khi sử dụng trứng"));
                } catch (IOException var13) {
                }
            }
        } else if (typegift[index] == 3) {
            if (player.isFullInventory()) {
                int xu = 5000 + r.nextInt(100000);
                player.addXu((long) xu, "map 6");
                info = info + xu + " xu";
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } else {
                ItemTemplates template = (ItemTemplates) ((Hashtable) itemTemplates.get(5)).get(idgift[index]);
                Item newItem = null;
                newItem = new Item(template, false, 0, 0, template.id);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                player.iItems.add(newItem);
                newItem.identify = player.charDBID;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                info = info + newItem.getName();

                try {
                    if (idgift[index] == 721) {
                        sendAllCharServer(-1, MessageCreator.createServerAlertAutoOffMessage(player.charname + " đã nhận được Huyết bồ đề khi sử dụng trứng vàng."));
                    }
                } catch (IOException var12) {
                }
            }

            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        }

        Database.instance.saveOrtherLog("", player.charname, info, "trung");
    }

    static void doOpenHopQuaSinhNhatNgude(Map map, Char player) {
        try {
            int ran = r.nextInt(100);
            String info = "";
            if (ran >= 3) {
                if (r.nextInt(100) < 90) {
                    player.potions[69] = player.potions[69] + 5;
                    player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được 5 vé quay số", ""));
                    info = "Nhan 5 ve quay so";
                } else {
                    int[] idgem = new int[]{250, 228, 154};
                    int id = idgem[r.nextInt(idgem.length)];
                    player.doAddGemItem(id, 1, true);
                    player.sendMessage(MessageCreator.createCharGemItem(player));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được 1 " + gemTemplate[id].name + " khoá", ""));
                    info = "nhận được 1 " + gemTemplate[id].name + " khoá";
                }
            } else {
                for (int i = 0; i < GemTemplate.ID_MATERIAL_HIGHT[5].length; i++) {
                    player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], 1, true);
                }

                player.sendMessage(MessageCreator.createCharGemItem(player));
                player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được nguyên liệu cao cấp cấp 6 khoá mỗi loại 1 viên", ""));
                info = "Nhan nlcc khoa moi loai 1 vien";
            }

            Database.instance.saveOrtherLog("", player.charname, info, "snngude");
        } catch (Exception var6) {
        }
    }

    static void doOpenHopMutNho(Map map, Char player) {
        try {
            String info = "";
            if (r.nextInt(100) < 70) {
                int[] id = new int[]{35, 13, 97, 94, 96};
                int[] sl = new int[]{1, 1, 1, 50, 50};
                int idpt = r.nextInt(id.length);
                int soluong = sl[idpt];
                if (idpt > 2) {
                    soluong += r.nextInt(50);
                }

                player.potions[id[idpt]] = player.potions[id[idpt]] + soluong;
                info = "Bạn nhận được " + soluong + " " + LoginHandler.PORTION_NAME[id[idpt]];
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } else {
                int pc = r.nextInt(100);
                int soluong = 1;
                if (pc < 50) {
                    int[] id = new int[]{235, 236, 237};
                    int idgem = id[r.nextInt(id.length)];
                    player.doAddGemItem(idgem, soluong, true);
                    info = "Bạn nhận được " + soluong + " " + gemTemplate[idgem].name;
                } else {
                    if (pc < 95) {
                        String[] menu = new String[]{"", "", "", "", ""};

                        for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                            menu[i] = gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
                        }

                        player.potions[125]++;
                        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                        player.idNglieuDoi = 0;
                        player.sendMessage(MessageCreator.createMsgMenu(menu, -511, 0));
                        return;
                    }

                    player.doAddGemItem(11, soluong, true);
                    info = "Bạn nhận được " + soluong + " " + gemTemplate[11].name;
                }

                player.sendMessage(MessageCreator.createCharGemItem(player));
            }

            Database.instance.saveOrtherLog("", player.charname, info, "hopmutnho");
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }

    static boolean doOpenTuiQuaLon(Map map, Char player) {
        int rd = r.nextInt(100);
        String info = "";
        int sl = 1;
        int nqua = r.nextInt(3) + 1;
        if (rd < 1) {
            int[] idgem = new int[]{238, 239, 240};
            int pc = r.nextInt(100);
            int id = idgem[0];
            sl += r.nextInt(3);
            if (pc < 3) {
                id = idgem[2];
                sl = 1;
            } else if (pc < 10) {
                id = idgem[1];
                sl = 1;
            }

            player.doAddGemItem(id, sl, r.nextInt(100) >= 2);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            info = sl + " " + gemTemplate[id].name;
            if (nqua > 1) {
                nqua--;
                Vector<Potion> pt = new Vector();
                pt.add(new Potion((short) 8, 1, null));
                pt.add(new Potion((short) 75, 1, null));
                if (player.potions[94] < 30000) {
                    pt.add(new Potion((short) 94, r.nextInt(51) + 50, null));
                }

                if (player.potions[96] < 30000) {
                    pt.add(new Potion((short) 96, r.nextInt(51) + 50, null));
                }

                pt.add(new Potion((short) 116, r.nextInt(8) + 3, null));

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
                        info = info + "," + po.quantity + " " + LoginHandler.PORTION_NAME[po.getType()];
                    }
                }
            }

            player.sendMessage(MessageCreator.createCharGemItem(player));
        } else {
            if (rd < 20) {
                player.slNgLieuDoi = (byte) (r.nextInt(4) + 2);
                String[] stmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < stmenu.length; i++) {
                    stmenu[i] = player.slNgLieuDoi + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
                }

                player.sendMessage(MessageCreator.createMsgMenu(stmenu, -49, 2));
                player.potions[118]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                return false;
            }

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
                    info = info + "," + po.quantity + " " + LoginHandler.PORTION_NAME[po.getType()];
                }
            }
        }

        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được " + sl + " " + info, ""));
        Database.instance.saveOrtherLog("", player.charname, "Chúc mừng bạn nhận được " + sl + " " + info, "otuilon");
        return true;
    }

    static boolean doOpenTuiQuaNho(Map map, Char player) {
        String info = "";
        int sl = 1;
        int rd = r.nextInt(100);
        int nqua = r.nextInt(3) + 1;
        if (rd < 1) {
            int[] idgem = new int[]{235, 236, 237};
            int id = idgem[r.nextInt(idgem.length)];
            if (id == 235) {
                sl = r.nextInt(26) + 25;
            } else if (id == 236) {
                sl = r.nextInt(11) + 10;
            } else {
                sl = r.nextInt(3) + 3;
            }

            player.doAddGemItem(id, sl, r.nextInt(100) >= 3);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            info = sl + " " + gemTemplate[id].name;
            if (nqua > 1) {
                nqua--;
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
                        info = info + "," + po.quantity + " " + LoginHandler.PORTION_NAME[po.getType()];
                    }
                }
            }

            player.sendMessage(MessageCreator.createCharGemItem(player));
        } else {
            if (rd < 20) {
                player.slNgLieuDoi = (byte) (r.nextInt(2) + 1);
                String[] stmenu = new String[GemTemplate.ID_MATERIAL_LOW[5].length];

                for (int i = 0; i < stmenu.length; i++) {
                    stmenu[i] = player.slNgLieuDoi + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[5][i]].name;
                }

                player.sendMessage(MessageCreator.createMsgMenu(stmenu, -49, 1));
                player.potions[117]++;
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                return false;
            }

            Vector<Potion> ptx = new Vector();
            ptx.add(new Potion((short) 13, 1, null));
            ptx.add(new Potion((short) 35, 1, null));
            if (player.potions[94] < 30000) {
                ptx.add(new Potion((short) 94, r.nextInt(51) + 50, null));
            }

            if (player.potions[96] < 30000) {
                ptx.add(new Potion((short) 96, r.nextInt(51) + 50, null));
            }

            ptx.add(new Potion((short) 97, r.nextInt(8) + 3, null));

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
                    info = info + "," + po.quantity + " " + LoginHandler.PORTION_NAME[po.getType()];
                }
            }
        }

        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn nhận được " + info, ""));
        Database.instance.saveOrtherLog("", player.charname, "Chúc mừng bạn nhận được " + info, "otuinho");
        return true;
    }

    static void doOpenHopQuaCuongthi(Map map, Char player) {
        try {
            String info = "Chúc mừng bạn nhận được ";
            if (r.nextInt(100) < 50) {
                int soluong = 3;
                int lv = r.nextInt(3) + 3;
                int idngl = GemTemplate.ID_MATERIAL_HIGHT[lv][r.nextInt(GemTemplate.ID_MATERIAL_HIGHT[lv].length)];
                player.doAddGemItem(idngl, soluong, true);
                info = info + soluong + " " + gemTemplate[idngl].name;
            } else {
                int[] idbot = new int[]{246, 247, 248, 249};
                int[] slmin = new int[]{50, 5, 1, 1};
                int[] slrandom = new int[]{51, 21, 4, 1};
                int rd = r.nextInt(idbot.length);
                int sl = slmin[rd];
                if (slmin[rd] != slrandom[rd]) {
                    sl += r.nextInt(slrandom[rd]);
                }

                boolean lock = r.nextInt(2) == 1;
                info = info + "," + sl + " " + gemTemplate[idbot[rd]].name + " " + (lock ? "khoá" : "");
                player.doAddGemItem(idbot[rd], sl, lock);
            }

            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharGemItem(player));
            Database.instance.saveOrtherLog("", player.charname, info, "hopquact");
        } catch (Exception var9) {
        }
    }

    static void doOpenHopQuaHe2015(Map map, Char player) {
        try {
            String info = "Chúc mừng bạn nhận được ";
            int ran = r.nextInt(100);
            if (ran == 0) {
                info = info + "nhân sâm";
                player.potions[9]++;
            } else if (r.nextInt(100) < 50) {
                int soluong = 3;
                int lv = r.nextInt(3) + 3;
                int idngl = GemTemplate.ID_XUONG[lv];
                player.doAddGemItem(idngl, soluong, r.nextInt(100) >= 5);
                info = info + soluong + " " + gemTemplate[idngl].name;
            } else {
                int[] idbot = new int[]{246, 247, 248, 249};
                int[] slmin = new int[]{50, 5, 1, 1};
                int[] slrandom = new int[]{51, 21, 4, 1};
                int rd = r.nextInt(idbot.length);
                int sl = slmin[rd];
                if (slmin[rd] != slrandom[rd]) {
                    sl += r.nextInt(slrandom[rd]);
                }

                boolean lock = r.nextInt(2) == 1;
                info = info + "," + sl + " " + gemTemplate[idbot[rd]].name + " " + (lock ? "khoá" : "");
                player.doAddGemItem(idbot[rd], sl, lock);
            }

            int[] xu = new int[]{100000, 200000, 300000, 400000, 500000};
            ran = r.nextInt(xu.length);
            info = info + " " + xu[ran] + " xu";
            player.addXu((long) xu[ran], "map 14");
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage(info, ""));
            player.sendMessage(MessageCreator.createCharGemItem(player));
            Database.instance.saveOrtherLog("", player.charname, info, "hopquaboss");
        } catch (Exception var10) {
        }
    }

    static void openBox(Map map, Char player, int type) {
        short[] idGem = new short[]{105, 112, 119, 126, 133};
        short[] var10000 = new short[]{11, 66};
        int percent = r.nextInt(1000);
        GemItem gem = null;
        int soluong = 0;
        if (type == 0) {
            if (percent <= 0) {
                gem = MapEpNgoc.createGemItem(map, 66);
                soluong = 1;
            } else if (percent <= 50) {
                gem = MapEpNgoc.createGemItem(map, idGem[r.nextInt(idGem.length)]);
                soluong = 5 + r.nextInt(6);
            } else if (percent < 500) {
                if (r.nextInt(2) == 0) {
                    player.potions[94] = player.potions[94] + (soluong = 10 + r.nextInt(11));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " HP 15k", ""));
                } else {
                    player.potions[96] = player.potions[96] + (soluong = 10 + r.nextInt(11));
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " MP 15k", ""));
                }

                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            } else if (percent < 700) {
                try {
                    addXPForChar(player, (long) (soluong = 1000000 + r.nextInt(500001)), false, "map openBox1");
                    player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " xp", ""));
                } catch (Exception var10) {
                }
            } else {
                player.addXu((long) (soluong = 50000 + r.nextInt(50001)), "map 15");
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " xu", ""));
            }
        } else if (percent <= 1) {
            gem = MapEpNgoc.createGemItem(map, 11);
            soluong = 1;
        } else if (percent <= 50) {
            gem = MapEpNgoc.createGemItem(map, idGem[r.nextInt(idGem.length)]);
            soluong = 3 + r.nextInt(3);
        } else if (percent < 500) {
            if (r.nextInt(2) == 0) {
                player.potions[94] = player.potions[94] + (soluong = 5 + r.nextInt(6));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " HP 15k", ""));
            } else {
                player.potions[96] = player.potions[96] + (soluong = 5 + r.nextInt(6));
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " MP 15k", ""));
            }

            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        } else if (percent < 700) {
            try {
                addXPForChar(player, (long) (soluong = 500000 + r.nextInt(250001)), false, "map openBox2");
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " xp", ""));
            } catch (Exception var9) {
            }
        } else {
            player.addXu((long) (soluong = 25000 + r.nextInt(25001)), "map 16");
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(MessageCreator.createServerAlertMessage("Bạn nhận được " + soluong + " xu", ""));
        }

        if (gem != null) {
            player.doAddGemItem(gem.idGemTemplate, soluong, false);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng, bạn vừa nhận được " + gemTemplate[gem.idGemTemplate].name, ""));
            Database.instance.saveOrtherLog("tob_log_other_gem", player.charname, gemTemplate[gem.idGemTemplate].name + "_" + soluong, "ob");
            player.allGemGet[gem.idGemTemplate] = player.allGemGet[gem.idGemTemplate] + soluong;
        }
    }

    static void doOpenLuckyBox(Map map, Char player, Message message) {
        try {
            if (player.gifLuckyBox.size() <= 2) {
                player.gifLuckyBox.removeAllElements();
                player.idOpen.removeAllElements();
                player.nSpecialBox = 2;
                return;
            }

            byte type = message.dis.readByte();
            if (type == 1) {
                byte idBox = message.dis.readByte();
                if (player.idOpen.contains(idBox)) {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Xin chọn hộp khác", ""));
                    return;
                }

                String infoGif = player.doOpenLuckyBox();
                if (infoGif.equals("")) {
                    infoGif = "Chúc bạn may mắn lần sau.";
                }

                player.idOpen.add(idBox);
                Vector<String> v = player.getInfoGifLucky();
                v.insertElementAt(infoGif, 0);
                player.sendMessage(MessageCreator.createLuckyBox(idBox, v, 1));
                if (player.gifLuckyBox.size() <= 2) {
                    player.sendMessage(MessageCreator.createLuckyBox(idBox, v, 2));
                }
            } else if (type == 2) {
                player.doOpenAllLuckyBox();
            }
        } catch (Exception var7) {
        }
    }

    static void doVongQuayMayMan(Map map, Char player, String money, int hight_low) {
        player.doXoso(Integer.parseInt(money.trim()), 0, hight_low);
    }

    static void doSetGiftNguDeDaiNam(Map map, Char player, String code) {
        // SECURITY: disabled external call to third-party (teamobi) server.
        player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng này hiện không khả dụng.", ""));
        if (true) {
            return;
        }
        String url = "http://my.teamobi.com/app/?for=event&do=game&g=kpah&s=" + TeamServer.server + "&c=" + player.charname + "&code=" + code;
        String vl = Net.getHttp(url);
        String result = "";
        String codeuse = "";
        if (TeamServer.server != 3) {
            result = getNumberFromString(vl);
        } else {
            vl = BadWord.replaceStringWithSpace(vl).trim();
            if (vl.startsWith("3")) {
                result = "3";
                codeuse = vl.substring(1, vl.length());
            } else {
                result = getNumberFromString(vl);
            }
        }

        int id = Integer.parseInt(result.trim());
        if (id == 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Mã này đã được sử dụng", ""));
        } else if (id == 1) {
            player.doAddSpecialItem(0, 1);
            player.potions[82]++;
            int slsc = 1;
            int slcc = 0;
            int var15 = 1;
            int var16 = 5;

            for (int i = 0; i < GemTemplate.ID_MATERIAL_LOW[5].length; i++) {
                player.doAddGemItem(GemTemplate.ID_MATERIAL_HIGHT[5][i], var16, true);
            }

            int[][] iditem = new int[][]{{723}, {724}};
            Item it = doBuyModelClothe(player, player.isCharNam() ? iditem[0][r.nextInt(2)] : iditem[1][r.nextInt(2)], 7, 1);
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Chúc mừng bạn đã nhận được nguyên liệu cao cấp mỗi loại " + var16 + " viên và 1 bộ thời trang thời hạn 7 ngày", ""
                    )
            );
            Database.instance
                    .saveOrtherLog(
                            "",
                            player.charname,
                            code + " Chúc mừng bạn đã nhận được nguyên liệu cao cấp mỗi loại " + var16 + " viên và 1 bộ thời trang thời hạn 7 ngày",
                            "gcode"
                    );
        } else if (id == 3) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Một nhân vật chỉ có thể nhận được 1 phần quà.", ""));
        }
    }

    static void doSetGiftOfflineHtbb(Map map, Char player, String code) {
        // SECURITY: disabled external call to third-party (teamobi) server.
        player.sendMessage(MessageCreator.createServerAlertMessage("Chức năng này hiện không khả dụng.", ""));
        if (true) {
            return;
        }
        String url = "http://my.teamobi.com/app/?for=event&do=offline&g=kpah&s=" + TeamServer.server + "&c=" + player.charname + "&code=" + code;
        String result = getNumberFromString(Net.getHttp(url));
        int id = Integer.parseInt(result.trim());
        if (id == 0) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Có lỗi xảy ra.", ""));
        } else if (id == 1 || id == 4) {
            for (byte i = 0; i < 5; i++) {
                player.doAddGemItem(GemTemplate.ID_MATERIAL_LOW[5][i], 10, true);
            }

            player.doAddGemItem(GemTemplate.LKD_40, 5, true);
            Item newItem = ItemLuckyDraw.createMaxItemCoatLucky(616, player, 0);
            newItem.dateCreate = Char.getDayTime(0L);
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = 43200;
            player.iItems.add(newItem);
            newItem = ItemLuckyDraw.createMaxItemCoatLucky(616, player, 1);
            newItem.dateCreate = Char.getDayTime(0L);
            newItem.timeLoan = System.currentTimeMillis();
            newItem.minuteExist = 43200;
            player.iItems.add(newItem);
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(
                    MessageCreator.createServerAlertMessage(
                            "Chúc mừng bạn đã nhận được 10 nguyên liệu sơ cấp cấp 6 khoá mỗi loại, 10 luyện kim dược 30% và 1 phi phong lông thú 30 ngày.", ""
                    )
            );
            Database.instance.saveOrtherLog("", player.charname, "gif offline", "offline");
        } else if (id == 2) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Mã code không hợp lệ", ""));
        } else if (id != 3 && id == -1) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Một nhân vật chỉ có thể nhận được 1 phần quà.", ""));
        }
    }

    static void doOpenHopGo(Map map, Char p) {
        int ran = r.nextInt(100);
        if (ran < 50) {
            addXpCharEvent(p, 50000L, false, "doOpenHopGo");
        } else if (ran < 80) {
            short a = (short) 0;
            p.potions[a = (short) (r.nextInt(100) < 50 ? 93 : 95)] += 10;
            Database.instance.saveOrtherLog("", p.charname, LoginHandler.PORTION_NAME[a], "hopgoP");
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được 10 " + LoginHandler.PORTION_NAME[a], ""));
        } else {
            ran = r.nextInt(4) + 1;

            for (byte i = 0; i < ran; i++) {
                p.doAddGemItem(60);
            }

            Database.instance.saveOrtherLog("", p.charname, "Đục 1", "hopgoG");
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + ran + " đục cấp 1", ""));
            p.sendMessage(MessageCreator.createCharGemItem(p));
        }
    }

    static void doOpenHopBac(Map map, Char p) {
        int ran = r.nextInt(100);
        if (ran < 80) {
            short a = (short) 0;
            p.potions[a = (short) (r.nextInt(100) < 50 ? 113 : 112)]++;
            Database.instance.saveOrtherLog("", p.charname, LoginHandler.PORTION_NAME[a], "hopbacF");
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + LoginHandler.PORTION_NAME[a], ""));
        } else if (ran < 90) {
            p.potions[84]++;
            Database.instance.saveOrtherLog("", p.charname, "thuoc cuong hoa", "hopbacP");
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được thuốc cường hóa.", ""));
        } else if (ran <= 98) {
            if (r.nextInt(100) < 50) {
                byte[] temp = new byte[]{3, 3, 3, 3, 3, 3, 3, 4, 4, 5};
                int id = idMaterial13[temp[r.nextInt(temp.length)]][r.nextInt(5)];
                p.doAddGemItem(id);
                Database.instance.saveOrtherLog("", p.charname, gemTemplate[id].name, "hopbacG");
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + gemTemplate[id].name, ""));
            } else {
                byte[] temp = new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 2};
                int id = idMaterial13[temp[r.nextInt(temp.length)]][r.nextInt(5)];
                p.doAddGemItem(id);
                Database.instance.saveOrtherLog("", p.charname, gemTemplate[id].name, "hopbacG");
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + gemTemplate[id].name, ""));
            }
        } else if (ran == 99) {
            p.doAddGemItem(11);
            Database.instance.saveOrtherLog("", p.charname, gemTemplate[11].name, "hopbacG");
            p.sendMessage(MessageCreator.createCharGemItem(p));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + gemTemplate[11].name, ""));
        }
    }

    static void doOpenHopVang(Map map, Char p) {
        int ran = r.nextInt(100);
        if (ran < 65) {
            p.potions[114]++;
            Database.instance.saveOrtherLog("", p.charname, LoginHandler.PORTION_NAME[114], "hopvangF");
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + LoginHandler.PORTION_NAME[114], ""));
        } else if (ran < 80) {
            p.potions[84]++;
            Database.instance.saveOrtherLog("", p.charname, LoginHandler.PORTION_NAME[84], "hopvangP");
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + LoginHandler.PORTION_NAME[84], ""));
        } else if (ran < 92) {
            if (r.nextInt(100) < 50) {
                byte[] temp = new byte[]{3, 3, 3, 3, 3, 3, 3, 4, 4, 5};
                int id = InfoGifLucky.idMaterial[temp[r.nextInt(temp.length)]][r.nextInt(5)];
                p.doAddGemItem(id);
                Database.instance.saveOrtherLog("", p.charname, gemTemplate[id].name, "hopvangG");
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + gemTemplate[id].name, ""));
            } else {
                byte[] temp = new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 2};
                int id = InfoGifLucky.idMaterial[temp[r.nextInt(temp.length)]][r.nextInt(5)];
                p.doAddGemItem(id);
                Database.instance.saveOrtherLog("", p.charname, gemTemplate[id].name, "hopvangG");
                p.sendMessage(MessageCreator.createCharGemItem(p));
                p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + gemTemplate[id].name, ""));
            }
        } else if (ran < 97) {
            p.doAddGemItem(66);
            Database.instance.saveOrtherLog("", p.charname, gemTemplate[66].name, "hopvangG");
            p.sendMessage(MessageCreator.createCharGemItem(p));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + gemTemplate[66].name, ""));
        } else {
            p.potions[119]++;
            Database.instance.saveOrtherLog("", p.charname, LoginHandler.PORTION_NAME[119], "hopvangN");
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            p.sendMessage(MessageCreator.createServerAlertMessage("Nhận được " + LoginHandler.PORTION_NAME[119], ""));
        }
    }

    static void grantMocNapReward(Map map, Char player, int moc, int phoenixChoice) {
        // GD2 2.11 (Q10): khong co nap -> khong co qua moc nap khi bat co sv.lau_dai.
        if (com.htbb.pokemon.LauDai.bat()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Quà mốc nạp đã ngừng.", ""));
            return;
        }
        player.addLuong(100000);
        player.addLuongLock(200000);
        if (moc == 2) {
            GiftCode.AddItem(player, 686, 0, 0, -1); // Trứng Yêu Tinh
            GiftCode.AddItem(player, 684, 0, 0, -1); // Trứng Cú
            doCreateHuyetLinhThao(player, 0);
            doCreateHuyetLinhThao(player, 0);
        } else if (moc == 3) {
            GiftCode.AddItem(player, 688, 0, 0, -1); // Trứng đại bàng
            GiftCode.AddItem(player, 685, 0, 0, -1); // Trứng Rồng
            doCreateHuyetLinhThao(player, 0);
            doCreateHuyetLinhThao(player, 0);
            player.createPhuongHoang(0, phoenixChoice == 1 ? Animal.PHUONG_HOANG_BANG : Animal.PHUONG_HOANG, true);
            Char.createVuKhiThoiTrang(player, 0, 10080, null, true, true, 1, false);
        }
        player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));
        player.sendMessage(MessageCreator.createServerAlertMessage("Chúc mừng bạn đã nhận quà Mốc nạp " + moc + " (" + (MOC_NAP_GIA[moc - 1] / 1000) + "k)!", ""));
        Database.instance.saveOrtherLog("", player.charname, "Nhan qua moc nap " + moc, "mocnap");
    }

    static int soMucMoiTrangDoiThuong(Map map) {
        return (TEN_DOI_THUONG.length + 1) / 2;
    }

    static void showTrangDoiThuong(Map map, Char player, int trang) {
        final int moiTrang = soMucMoiTrangDoiThuong(map);
        int tuVitri = trang * moiTrang;
        int denVitri = Math.min(tuVitri + moiTrang, TEN_DOI_THUONG.length);
        String[] menu = new String[denVitri - tuVitri + 1];
        for (int i = tuVitri; i < denVitri; i++) {
            menu[i - tuVitri] = TEN_DOI_THUONG[i] + " - " + GIA_DOI_THUONG[i] + " điểm";
        }
        menu[menu.length - 1] = (trang == 0) ? "Trang sau »" : "« Trang trước";
        player.sendMessage(MessageCreator.createMsgMenu(menu, trang == 0 ? -91 : -92, 0));
    }

    static boolean traoVatPhamDoiThuong(Map map, Char player, int idx) {
        final int PHUT_7_NGAY = 10080;
        if (player.isFullInventory()) {
            player.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy, không nhận được quà.", ""));
            return false;
        }
        switch (idx) {
            case 0: // Phi phong Huyen Vu ma phap random khang 7 ngay
                Char.createtemCoat(750, player, 0, false, PHUT_7_NGAY);
                break;
            case 1: // Phi phong Huyen Vu vat ly random khang 7 ngay
                Char.createtemCoat(750, player, 1, false, PHUT_7_NGAY);
                break;
            case 2: // Vu khi thoi trang random chi so 7 ngay
                Char.createVuKhiThoiTrang(player, Map.r.nextInt(2), PHUT_7_NGAY, null, false, true, 1, false);
                break;
            case 3: // Hoa thach cap 2 x20
                player.doAddGemItem(GemTemplate.HOA_THACH_C2, 20, true);
                break;
            case 4: // Nghe Thuong 7 ngay
                Char.doDropModelClothe(player, 797, 7);
                break;
            case 5: // Xich Cuoc 7 ngay
                Char.doDropModelClothe(player, 799, 7);
                break;
            case 6: // Nguyet Linh Truong random chi so 7 ngay
                Char.createNguyetLinhTruong(player, Map.r.nextInt(2), PHUT_7_NGAY, null, false, true, 1, false);
                break;
            case 7: { // Canh ngau nhien: random CA he lan chi so, TRU tier cao nhat
                      // (tier cao nhat danh rieng cho quay so may man - xem
                      // Item.createOptionCanhNgauNhien).
                final int[] idsCanh = {898, 900, 901, 902};
                final int idTemplate = idsCanh[Map.r.nextInt(idsCanh.length)];
                final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idTemplate), false, 0, 0, idTemplate);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                newItem.identify = player.charDBID;
                newItem.clazz = player.charClass;
                newItem.createOptionCanhNgauNhien(idTemplate - 898, true);
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = PHUT_7_NGAY;
                newItem.dateCreate = Char.getDayTime(0L);
                player.iItems.add(newItem);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                break;
            }
            case 8: // Mat na Bi Ngo 7 ngay
                Map.doCreateMatNa3(player, PHUT_7_NGAY, Char.ID_MAT_NA[0], 0, 5);
                break;
            case 9: // Mat na Da Xoa 7 ngay
                Map.doCreateMatNa3(player, PHUT_7_NGAY, Char.ID_MAT_NA[4], 4, 50);
                break;
            case 10: // Mat na Thi Khien 7 ngay
                Map.doCreateMatNa3(player, PHUT_7_NGAY, Char.ID_MAT_NA[3], 3, 30);
                break;
            case 11: // Mat na Nga Khuyen 7 ngay
                Map.doCreateMatNa3(player, PHUT_7_NGAY, Char.ID_MAT_NA[2], 2, 10);
                break;
            case 12: { // Choi ngau nhien: random CA he lan chi so, TRU tier cao nhat
                       // (tier cao nhat danh rieng cho quay so may man - xem
                       // Item.createAttChoiNgauNhien).
                final int[] idsChoi = {617, 618, 619, 729};
                final int idTemplate = idsChoi[Map.r.nextInt(idsChoi.length)];
                final Item newItem = new Item((ItemTemplates) Map.itemTemplates.get(5).get(idTemplate), false, 0, 0, idTemplate);
                newItem.id = player.getIDItem();
                newItem.owner = player.charDBID;
                newItem.level = newItem.getTemplate().level;
                newItem.identify = player.charDBID;
                newItem.clazz = player.charClass;
                newItem.createAttChoiNgauNhien(true);
                newItem.lock = 1;
                newItem.timeLoan = System.currentTimeMillis();
                newItem.minuteExist = PHUT_7_NGAY;
                newItem.dateCreate = Char.getDayTime(0L);
                player.iItems.add(newItem);
                player.sendMessage(MessageCreator.createCharInventoryMessage(player, 1));
                break;
            }
            case 13: // Dau Bi Ngo 7 ngay
                Char.doDropModelClothe(player, 577, 7);
                break;
            case 14: // Non Phu Thuy 7 ngay
                Char.doDropModelClothe(player, 578, 7);
                break;
            case 15: // Skin linh thu Phuc Thu
            case 16: // Skin linh thu Gau Truc
            case 17: // Skin linh thu Tien Loc
            case 18: // Skin linh thu Xich Duong
            case 19: // Skin linh thu Chien Lang
                // 7 ngay (truoc day la 0 = vinh vien). Vat pham skin la trang bi thu
                // (type 30), mac vao o dau tien ben phai bang Trang bi thu.
                GiftCode.AddItem(player, SkinLinhThu.ID_ITEM[idx - 15], 0, 0, PHUT_7_NGAY);
                break;
            case 20: // Ho ve lenh 5x random lv1-3, 7 ngay
                map.doCreateHoVeLenh(player, PHUT_7_NGAY, new int[]{633, 634, 635}[r.nextInt(3)]);
                break;
            case 21: // Ho ve lenh 6x random lv1-3, 7 ngay
                map.doCreateHoVeLenh(player, PHUT_7_NGAY, new int[]{636, 637, 638}[r.nextInt(3)]);
                break;
            case 22: // Tu hon dan khoa x100 (Potion.TYPE_TU_HON_DAN_C1=83, xep chong qua
                     // potions[], isTrade=0 nen da khoa san - xem chu thich TEN_DOI_THUONG)
                player.potions[Potion.TYPE_TU_HON_DAN_C1] += 100;
                break;
            default:
                return false;
        }
        return true;
    }

    static void doReceiveGiftEvent5Star(Map map, Char p, int idNpc, int idMenu, int idOptionMenu) {
        try {
            String info = "";
            String infoAlert = "";
            if (p.isFullInventory()) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Hành trang đầy", ""));
                return;
            }

            if (idMenu == 0) {
                String[] menu = new String[]{"Chổi băng", "Chổi độc", "Chổi điện"};
                p.sendMessage(MessageCreator.createMsgMenu(menu, idNpc, 20));
            } else if (idMenu == 20) {
                if (idOptionMenu == 0) {
                    Char.createChoi(617, p, 10080, 1, false);
                    info = "nhan choi bang";
                    infoAlert = "Chúc mừng bạn nhận được 1 chổi băng";
                } else if (idOptionMenu == 1) {
                    Char.createChoi(619, p, 10080, 1, false);
                    info = "nhan doc bang";
                    infoAlert = "Chúc mừng bạn nhận được 1 chổi độc";
                } else if (idOptionMenu == 2) {
                    Char.createChoi(618, p, 10080, 1, false);
                    info = "nhan dien bang";
                    infoAlert = "Chúc mừng bạn nhận được 1 chổi điện";
                }

                if (r.nextInt(100) < 50) {
                    p.createNguaXuong(7L, false);
                    infoAlert = infoAlert + " và 1 linh thú Bạch Cốt";
                } else {
                    p.createHeo(7L, false);
                    infoAlert = infoAlert + " và 1 linh thú Đương Khang";
                }

                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
                p.receiveGiftEvent = 1;
                Database.instance.saveOrtherLog("", p.charname, "nhan qua event 5 sao " + p.receiveGiftEvent + " " + info, "qua5sao");
                p.sendMessage(MessageCreator.createServerAlertMessage(infoAlert, ""));
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }
    }

    static void doHopQuaTangHaloween2016(Map map, Char player) {
        long exp = 300000L;
        addXpCharEvent(player, exp, false, "doHopQuaTangHaloween2016");
        player.idNglieuDoi = -1;
        int[] per = new int[]{
            200, 200, 500, 500, 500, 500, player.lvDetail.lv >= 20 ? 500 : -1, player.lvDetail.lv >= 20 ? 500 : -1, player.lvDetail.lv >= 20 ? 500 : -1, 400
        };
        int[] idgift = new int[]{118, 118, 123, 84, 82, 100, GemTemplate.XUONG_C6, GemTemplate.DA_TIEN_GIAI, GemTemplate.LKD_40, 144};
        int[] typegift = new int[]{4, 4, 4, 4, 4, 4, 6, 6, 6, 4};
        int[] soluongmax = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] soluongmin = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        String info = "Chúc mừng bạn nhận được ";
        boolean lock = true;
        int index = Char.getIndexRandom(per);
        if (typegift[index] == -1) {
            short[] idGem = GemTemplate.ID_MATERIAL_LOW[r.nextInt(3) + 1];
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            int idmt = idGem[r.nextInt(idGem.length)];
            player.doAddGemItem(idmt, soluong, lock);
            info = info + soluong + " " + gemTemplate[idmt].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        } else if (typegift[index] == -2) {
            short[] idGem = GemTemplate.ID_MATERIAL_HIGHT[r.nextInt(3) + 1];
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            int idmt = idGem[r.nextInt(idGem.length)];
            player.doAddGemItem(idmt, soluong, lock);
            info = info + soluong + " " + gemTemplate[idmt].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        } else if (typegift[index] == 4) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.potions[idgift[index]] = player.potions[idgift[index]] + soluong;
            info = info + soluong + " " + LoginHandler.PORTION_NAME[idgift[index]];
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
            player.sendMessage(MessageCreator.createCharInventoryMessage(player, 0));

            try {
                if (idgift[index] == 70 || idgift[index] == 86) {
                    sendAllCharServer(
                            -1,
                            MessageCreator.createServerAlertAutoOffMessage(
                                    player.charname + " đã nhận được " + LoginHandler.PORTION_NAME[idgift[index]] + " khi mở rương quà haloween"
                            )
                    );
                }
            } catch (IOException var15) {
            }
        } else if (typegift[index] == 6) {
            int soluong = soluongmin[index];
            if (soluongmin[index] < soluongmax[index]) {
                soluong += r.nextInt(soluongmax[index] - soluongmin[index] + 1);
            }

            player.doAddGemItem(idgift[index], soluong, lock);
            info = info + soluong + " " + gemTemplate[idgift[index]].name + (lock ? " khoá" : "");
            player.sendMessage(MessageCreator.createCharGemItem(player));
            player.sendMessage(MessageCreator.createMsgChat(player.id, info));
        }

        Database.instance.saveOrtherLog("", player.charname, info, "ohopquat");
    }

    static void doReceiveGiftGroup(Map map, Char player) {
        try {
            byte checkRcv = Database.instance.checkReceiveGiftGroup(player);
            if (checkRcv >= 3 || checkRcv == -1) {
                player.sendMessage(MessageCreator.createServerAlertMessage("Bạn đã nhận được quà hoặc bạn không nằm trong top 3.", ""));
                return;
            }

            byte[][] soluongNgl = new byte[][]{{10, 6, 3}, {20, 14, 7}, {30, 20, 10}, {40, 26, 13}};
            String[] menu = new String[GemTemplate.ID_MATERIAL_LOW[2].length];
            int sl = soluongNgl[(player.lvDetail.lv - 40) / 10][checkRcv];

            for (byte i = 0; i < GemTemplate.ID_MATERIAL_LOW[2].length; i++) {
                menu[i] = sl + " " + gemTemplate[GemTemplate.ID_MATERIAL_LOW[2][i]].name;
            }

            player.sendMessage(MessageCreator.createMsgMenu(menu, -508, 0));
        } catch (Exception var7) {
        }
    }

    static void doAddGiftWedding(Map map, Char p1, Char p2, int typeWedding) {
        try {
            short[] materialsc = InfoGifLucky.idMaterial[2];
            short[] materialcc = InfoGifLucky.idMaterial[5];
            p1.potions[88] = 0;
            p1.potions[89] = 0;
            p1.potions[90] = 0;
            p2.potions[88] = 0;
            p2.potions[89] = 0;
            p2.potions[90] = 0;
            p1.potions[typeWedding + 88] = 1;
            p1.potions[98] = 5;
            p2.potions[typeWedding + 88] = 1;
            p2.potions[98] = 5;
            p1.rcvGiftWedding = 1;
            p2.rcvGiftWedding = 1;
            p1.calculateAttrib();
            p2.calculateAttrib();
            p1.sendMessage(MessageCreator.createMainCharInfoMessage(p1));
            p2.sendMessage(MessageCreator.createMainCharInfoMessage(p2));
            String info = "";
            switch (typeWedding) {
                case 0:
                    p1.potions[99] = 5;
                    p2.potions[99] = 5;
                    p1.sendMessage(MessageCreator.createCharInventoryMessage(p1, 0));
                    p2.sendMessage(MessageCreator.createCharInventoryMessage(p2, 0));
                    break;
                case 1:
                    p1.potions[99] = 10;
                    p2.potions[99] = 10;

                    for (int i = 0; i < 2; i++) {
                        int id1 = materialsc[r.nextInt(materialcc.length)];
                        int id2 = materialcc[r.nextInt(materialcc.length)];
                        info = info + gemTemplate[id1].name + "_" + gemTemplate[id2].name;
                        p1.doAddGemItem(id1, 1, true);
                        p1.doAddGemItem(id2, 1, true);
                    }

                    Database.instance.saveOrtherLog("", p1.charname, info, "giftWD1");
                    info = "";

                    for (int i = 0; i < 2; i++) {
                        int id1 = materialsc[r.nextInt(materialcc.length)];
                        int id2 = materialcc[r.nextInt(materialcc.length)];
                        info = info + gemTemplate[id1].name + "_" + gemTemplate[id2].name;
                        p2.doAddGemItem(id1, 1, true);
                        p2.doAddGemItem(id2, 1, true);
                    }

                    p1.potions[100] = p1.potions[100] + 5;
                    p2.potions[100] = p2.potions[100] + 5;
                    p1.potions[114] = p1.potions[114] + 5;
                    p2.potions[114] = p2.potions[114] + 5;
                    p1.sendMessage(MessageCreator.createCharInventoryMessage(p1, 0));
                    p1.sendMessage(MessageCreator.createCharGemItem(p1));
                    p2.sendMessage(MessageCreator.createCharInventoryMessage(p2, 0));
                    p2.sendMessage(MessageCreator.createCharGemItem(p2));
                    Database.instance.saveOrtherLog("", p2.charname, info, "giftWD1");
                    break;
                case 2:
                    p1.potions[99] = 15;
                    p2.potions[99] = 15;

                    for (int i = 0; i < 4; i++) {
                        int id1 = materialsc[r.nextInt(materialcc.length)];
                        int id2 = materialcc[r.nextInt(materialcc.length)];
                        info = info + gemTemplate[id1].name + "_" + gemTemplate[id2].name;
                        p1.doAddGemItem(id1, 1, true);
                        p1.doAddGemItem(id2, 1, true);
                    }

                    Database.instance.saveOrtherLog("", p1.charname, info, "giftWD1");
                    info = "";

                    for (int i = 0; i < 4; i++) {
                        int id1 = materialsc[r.nextInt(materialcc.length)];
                        int id2 = materialcc[r.nextInt(materialcc.length)];
                        info = info + gemTemplate[id1].name + "_" + gemTemplate[id2].name;
                        p2.doAddGemItem(id1, 1, true);
                        p2.doAddGemItem(id2, 1, true);
                    }

                    p1.potions[100] = p1.potions[100] + 10;
                    p2.potions[100] = p2.potions[100] + 10;
                    p1.potions[114] = p1.potions[114] + 10;
                    p2.potions[114] = p2.potions[114] + 10;
                    p1.sendMessage(MessageCreator.createCharInventoryMessage(p1, 0));
                    p1.sendMessage(MessageCreator.createCharGemItem(p1));
                    p2.sendMessage(MessageCreator.createCharInventoryMessage(p2, 0));
                    p2.sendMessage(MessageCreator.createCharGemItem(p2));
                    Database.instance.saveOrtherLog("", p2.charname, info, "giftWD1");
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }

    static void doQuayso(Map map, Char player, int idNpc, int idMenu, int idOptionMenu) {
        if (idMenu == 0) {
            doQuayso(map, player, idOptionMenu);
        }
    }

    static void doQuayso(Map map, Char player, int option) {
        try {
            if (option == 0) {
                int moneyQuay = 500;
                if (player.getxu() > (long) moneyQuay) {
                    player.subXu((long) moneyQuay, true, "map 85");
                    int random = new Random().nextInt(3);
                    switch (random) {
                        case 0:
                            player.sendItem(5, 0);
                            break;
                        case 1:
                            player.sendItem(5, 1);
                            break;
                        case 2:
                            player.sendItem(5, 2);
                    }
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ xu", ""));
                }
            }

            if (option == 1) {
                int moneyQuay = 50;
                if (player.getLuong() > moneyQuay) {
                    player.subLuong((long) moneyQuay);
                    int random = new Random().nextInt(3);
                    switch (random) {
                        case 0:
                            player.sendItem(4, 0);
                            break;
                        case 1:
                            player.sendItem(4, 1);
                            break;
                        case 2:
                            player.sendItem(4, 2);
                    }
                } else {
                    player.sendMessage(MessageCreator.createServerAlertMessage("Không đủ lượng", ""));
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }

}
