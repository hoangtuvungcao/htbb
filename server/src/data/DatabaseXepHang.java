package data;


import real.Map;
import real.*;
import server.TeamServer;
import java.sql.*;

import java.util.Date;
import java.util.*;


/**
 * Than cac method bang xep hang va nap cau hinh cua Database: top level,
 * top giau, top nap luong, nap bang nhiem vu / vat pham / thoi gian x2,
 * doi mon phai, hang cho tao nhan vat va nap tien.
 *
 * Tach tu data/Database.java. Cac method VAN CON trong Database, giu NGUYEN
 * chu ky cu, chi than chuyen sang day.
 *
 * KHONG gom getChar va getInfoChar: chung bi NAP CHONG (2 va 3 ban), tach mot
 * ban de phep viet lai loi goi noi bo tro nham sang ban khac.
 */
class DatabaseXepHang {

    static void loadTimeX2Server(Database db) {
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        if (Database.agent == null) {
            Database.agent = new Vector<>();
        } else {
            Database.agent.removeAll(Database.agent);
        }
        try {
            final String sql = "select * from tob_x2exp";
            conn = db.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            Char.timex2.removeAllElements();
            Char.timex3.removeAllElements();
            Char.timex50.removeAllElements();
            while (rs.next()) {
                try {
                    Map.tradePet = rs.getByte("tradepet");
                } catch (Exception ignored) {
                }
                String d = rs.getString("dayopenx2");
                if (d != null && !d.equals("")) {
                    String[] a = Char.split(d, "|");
                    for (String s : a) {
                        Char.timex2.add(s);
                    }
                }
                d = rs.getString("dayopenx3");
                if (d != null && !d.equals("")) {
                    String[] b = Char.split(d, ">");
                    String[] a2 = Char.split(b[0], "|");
                    for (String s : a2) {
                        Char.timex3.add(s);
                    }
                    Char.Lvx3Exp = Integer.parseInt(b[1]);
                }
                d = rs.getString("dayopenx50");
                if (d != null && !d.equals("")) {
                    String[] a = Char.split(d, "|");
                    for (String s : a) {
                        Char.timex50.add(s);
                    }
                }
                try {
                    Map.totalChoiBan = rs.getByte("banchoi");
                } catch (Exception ignored) {
                }
                try {
                    Map.pause = (rs.getByte("pause") == 1);
                } catch (Exception ignored) {
                }
                try {
                    Map.pauseSellTown = (rs.getByte("pauseT") == 1);
                } catch (Exception ignored) {
                }
                try {
                    Char.isBaotriVxmm = (rs.getByte("vxmm") == 1);
                } catch (Exception ignored) {
                }
                try {
                    Map.stopGiveMoney = (rs.getByte("gopbang") == 1);
                } catch (Exception ignored) {
                }
                try {
                    RealController.maquee2 = rs.getString("infoalert");
                    if (RealController.maquee2 == null) {
                        RealController.maquee2 = "";
                        RealController.dayAlertMarquest2 = new String[]{""};
                    } else {
                        String[] info = Char.split(RealController.maquee2, "|");
                        RealController.dayAlertMarquest2 = new String[info.length - 1];
                        RealController.maquee2 = info[info.length - 1];
                        for (int i = 0; i < info.length - 1; ++i) {
                            RealController.dayAlertMarquest2[i] = info[i];
                        }
                    }
                } catch (Exception ignored) {
                }
                String[] badword = Char.split(rs.getString("badword"), ",");
                if (badword.length > 0) {
                    BadWord.list = badword;
                }
                Char.onOffThamdinh = rs.getByte("thamdinh");
                try {
                    Char.onOffTime180 = rs.getByte("time180");
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    static void getTopNapLuong(Database db) {
        Connection conn = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            conn = Database.connPoolNap.getConnection();
            stm = conn.createStatement();
            final String sql = "select * from tob_get_board_nap where dateGet>='2015-02-14 12:00:00' and dateGet<='2015-03-02 10:00:00' order by dateGet";
            rs = stm.executeQuery(sql);
            Hashtable<String, CharData> allchar = new Hashtable<>();
            Hashtable<String, CharData> allchar2 = new Hashtable<>();
            int count = 0;
            int gold = 0;
            while (rs.next()) {
                ++count;
                String data = rs.getString("info");
                if (data.indexOf("luong") > -1) {
                    String charname = rs.getString("charname").toLowerCase();
                    String[] d = Char.split(data, "luong: ");
                    int value = 0;
                    if (d[1].indexOf("X>") > -1) {
                        value = Integer.parseInt(d[1].substring(0, d[1].indexOf("X>")).trim());
                    } else {
                        value = Integer.parseInt(d[1].trim());
                    }
                    CharData c = allchar.get(charname);
                    if (c == null) {
                        c = new CharData();
                        allchar.put(c.charname = charname, c);
                    }
                    CharData charData = c;
                    charData.gold += value;
                    gold += value;
                    if (c.gold < 10000 || allchar2.get(charname) != null || allchar2.size() >= 50) {
                        continue;
                    }
                    allchar2.put(charname, c);
                }
            }
            System.out.println("TOG SO " + count + " >> " + gold);
            int i = 0;
            for (CharData c2 : allchar2.values()) {
                System.out.println("TOP 50 stt " + i + ": " + c2.charname + " > " + c2.gold);
                ++i;
            }
            i = 0;
            int max = 0;
            CharData topnap = null;
            for (CharData c3 : allchar.values()) {
                if (max < c3.gold) {
                    max = c3.gold;
                    topnap = c3;
                }
            }
            if (topnap != null) {
                System.err.println(topnap.charname + " > " + topnap.gold);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPoolNap.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPoolNap.free(conn);
        } catch (Exception ignored) {
        }
        try {
            Thread.sleep(10000L);
            System.exit(0);
        } catch (Exception ignored) {
        }
    }

    static void getBoardNapTien(Database db, Char player) {
        Connection conn = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            conn = Database.connPoolNap.getConnection();
            stm = conn.createStatement();
            String sql = "select * from board_naptien where username='" + player.charname + "'";
            rs = stm.executeQuery(sql);
            String info = "";
            long oldPotion = 0L;
            int oldluong = 0;
            try {
                oldPotion = player.getxu();
                oldluong = player.getLuong();
            } catch (Exception ignored) {
            }
            if (rs.next()) {
                int xu = rs.getInt("xu");
                player.addXu(xu, "getBoardNapTien db");
                if (xu > 0) {
                    info = "SV Get xu: " + xu + " | ";
                }
                int luong = rs.getInt("luong") * Database.TileNap;
                player.addLuong(luong);
                int luongKhoa = rs.getInt("luongKhoa") * Database.TileNap;
                if (luongKhoa > 0) {
                    player.addLuongLock(luongKhoa);
                }
                
                player.vetangluong += rs.getInt("ve");
                player.luongNap += (luong / Database.TileNap) / 2;

                Database.instance.updateLuongNap(luong, player);
                player.diemNapVip += luong / Database.TileNap;
                 Database.instance.addCharXaiNapLuong(player, 0);
                if (luong > 0) {
                    info = info + "SV Get luong: " + luong;
                }
                if (rs.getInt("ve") > 0) {
                    info = info + "| SV Get ve: " + rs.getInt("ve");
                }
                String sqlUpdate = "DELETE FROM `board_naptien` WHERE `id` ='" + rs.getInt("id") + "'";
                stm.execute(sqlUpdate);
                if (luong >= 10) {
                    Database.instance.saveLogThongKeChiTieu("nap luong", 0, 1, luong, "luong nap");
                }
            }
            try {
                if (!info.equals("")) {
                    sql = "insert tob_get_board_nap set charname='" + player.charname + "',info='" + info + " X>" + oldPotion + " L>" + oldluong + "',dateGet=now()";
                    stm.execute(sql);
                }
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPoolNap.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPoolNap.free(conn);
        } catch (Exception ignored) {
        }
    }

    static void getBoardCreated(Database db, Char player) {
        Connection conn = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            conn = Database.connPoolNap.getConnection();
            stm = conn.createStatement();
            String sql = "select * from board_created where username='" + player.userID + "'";
            rs = stm.executeQuery(sql);
            String info = "";
            long oldPotion = 0L;
            int oldluong = 0;
            try {
                oldPotion = player.getxu();
                oldluong = player.getLuong();
            } catch (Exception ignored) {
            }
            if (rs.next()) {
                int xu = rs.getInt("xu");
                player.addXu(xu, "board_created db");
                if (xu > 0) {
                    info = "SV Get xu: " + xu + " | ";
                }
                int luong = rs.getInt("luong");
                player.addLuong(luong);
                if (true) { // Khuyến mãi lượng khoá.
                    int lk = rs.getInt("luongK");
                    if (lk <= 0) {
                        lk = 1;
                    }
                    player.lockLuong += lk;
                }
                //player.vetangluong += rs.getInt("ve");
                //player.luongNap += luong / 2;

                //Database.instance.updateLuongNap(luong, player);
                //player.diemNapVip += luong;
                //  Database.instance.addCharXaiNapLuong(player, 0);
                if (luong > 0) {
                    info = info + "SV Get luong: " + luong;
                }
                if (rs.getInt("ve") > 0) {
                    info = info + "| SV Get ve: " + rs.getInt("ve");
                }
                String sqlUpdate = "DELETE FROM `board_created` WHERE `id` ='" + rs.getInt("id") + "'";
                stm.execute(sqlUpdate);
//                if (luong >= 10) {
//                    Database.instance.saveLogThongKeChiTieu("nap luong", 0, 1, luong, "luong nap");
//                }
            }
            try {
                if (!info.equals("")) {
                    sql = "insert tob_get_board_nap set charname='" + player.charname + "',info='" + info + " X>" + oldPotion + " L>" + oldluong + "',dateGet=now()";
                    stm.execute(sql);
                }
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                stm.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPoolNap.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            stm.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPoolNap.free(conn);
        } catch (Exception ignored) {
        }
    }

    static void loadAllQuestTemplate(Database db, int type) {
        Connection conn = null;
        Statement pre = null;
        ResultSet rs = null;
        try {
            String sql = "select * from data_quest where mainsub=" + type;
            conn = db.getConnection();
            pre = conn.createStatement();
            rs = pre.executeQuery(sql);
            short id = 0;
            while (rs.next()) {
                InfoQuestTemplate info = new InfoQuestTemplate();
                info.id = id;
                info.lv = rs.getShort("lv");
                if (!TeamServer.isServerIndo()) {
                    info.name = rs.getString("name");
                    info.content = rs.getString("content");
                    info.shortContent = rs.getString("scontent");
                    info.resContent = rs.getString("rescontent");
                    info.infofinish = rs.getString("infofinish");
                    info.infoGift = rs.getString("infogift");
                } else {
                    info.name = rs.getString("namein");
                    info.content = rs.getString("contentin");
                    info.shortContent = rs.getString("scontentin");
                    info.resContent = rs.getString("rescontentin");
                    info.infofinish = rs.getString("infofinishin");
                    info.infoGift = rs.getString("infogiftin");
                }
                info.idNpcRcv = rs.getShort("npcrcv");
                info.idNpcRes = rs.getShort("npcres");
                info.type = rs.getByte("type");
                info.deltaLv = rs.getShort("deltaLv");
                info.pAvtive = rs.getShort("pactive");
                info.main_sub = rs.getByte("mainsub");
                String[] content = Char.split(rs.getString("idItemget"), ",");
                for (String element : content) {
                    info.itemget.add(Short.parseShort(element));
                }
                content = Char.split(rs.getString("idMonskill"), ",");
                for (String item : content) {
                    info.monsterKill.add(Short.parseShort(item));
                }
                content = Char.split(rs.getString("nitemget"), ",");
                for (String value : content) {
                    info.totalitemget.add(Short.parseShort(value));
                }
                content = Char.split(rs.getString("nmonskill"), ",");
                for (String s : content) {
                    info.totalMonsKilled.add(Short.parseShort(s));
                }
                content = Char.split(rs.getString("potiongift"), ",");
                info.potiongift = new short[content.length];
                for (int i = 0; i < content.length; ++i) {
                    info.potiongift[i] = Short.parseShort(content[i]);
                }
                content = Char.split(rs.getString("npotion"), ",");
                info.npotiongift = new short[content.length];
                for (int i = 0; i < content.length; ++i) {
                    info.npotiongift[i] = Short.parseShort(content[i]);
                }
                content = Char.split(rs.getString("itemgift"), ",");
                info.idItemgift = new short[content.length];
                for (int i = 0; i < content.length; ++i) {
                    info.idItemgift[i] = Short.parseShort(content[i]);
                }
                info.gold = rs.getInt("gold");
                info.luong = rs.getInt("luong");
                info.luongLock = rs.getInt("luongLock");
                info.exp = rs.getInt("exp");
                if (type == 0) {
                    Map.allMainQuest.put(id, info);
                } else if (type == 1) {
                    Map.allSubQuest.put(id, info);
                } else if (type == 2) {
                    Map.allClanQuest.put(id, info);
                }
                ++id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
    }

    static Vector<Hashtable<Integer, ItemTemplates>> loadItemTemplate(Database db) {
        Vector<Hashtable<Integer, ItemTemplates>> itemTemplates = new Vector<>();
        Hashtable<Integer, ItemTemplates> itemNhe = new Hashtable<>();
        Hashtable<Integer, ItemTemplates> itemNheCung = new Hashtable<>();
        Hashtable<Integer, ItemTemplates> itemNangCB = new Hashtable<>();
        Hashtable<Integer, ItemTemplates> itemVai = new Hashtable<>();
        Hashtable<Integer, ItemTemplates> itemNangDS = new Hashtable<>();
        Hashtable<Integer, ItemTemplates> itemBasic = new Hashtable<>();
        Connection conn = null;
        Statement stmt = null;
        final int i = 0;
        System.out.println("LOAD TEMPLATE ");
        try {
            conn = db.getConnection();
            stmt = conn.createStatement();
            final String sql = "SELECT * FROM data_item order by id ";
            ResultSet rs = stmt.executeQuery(sql);
            int iii = 0;
            while (rs.next()) {
                ++iii;
                ItemTemplates item = new ItemTemplates();
                item.id = rs.getInt("id");
                if (!TeamServer.isServerIndo()) {
                    item.name = rs.getString("name");
                } else {
                    item.name = rs.getString("namein");
                }
                item.type = rs.getByte("type");
                item.style = rs.getByte("style");
                item.he = rs.getByte("he");
                item.gender = rs.getByte("gender");
                item.level = rs.getByte("level");
                item.durable = rs.getShort("durable");
                item.atb[0] = rs.getShort("atb0");
                item.atb[1] = rs.getShort("atb1");
                item.atb[2] = rs.getShort("atb2");
                item.atb[3] = rs.getShort("atb3");
                item.atb[4] = rs.getShort("atb4");
                item.atb[5] = rs.getShort("atb5");
                item.atb[6] = rs.getShort("atb6");
                item.atb[7] = rs.getShort("atb7");
                item.atb[8] = rs.getShort("atb8");
                item.atb[9] = rs.getShort("atb9");
                item.price = rs.getInt("price");
                item.clazz = rs.getByte("clazz");
                item.idIcon = rs.getShort("xstart");
                item.color = rs.getByte("colortype");
                item.ndayLoan = rs.getShort("nloan");
                item.idItemUpLevel = rs.getShort("idUpLevel");
                item.idEff = rs.getInt("ideff");
                item.idBigImgAvatar = rs.getShort("ystart");
                if (!Map.isModelClothes(item.atb[9])) {
                    item.atb[6] = item.atb[1];
                }
                itemNhe.put(item.id, db.copyItem(item, 0));
                itemNangCB.put(item.id, db.copyItem(item, 1));
                itemVai.put(item.id, db.copyItem(item, 2));
                itemNangDS.put(item.id, db.copyItem(item, 3));
                itemNheCung.put(item.id, db.copyItem(item, 4));
                itemBasic.put(item.id, item);
            }
            itemTemplates.add(itemNhe);
            itemTemplates.add(itemNangCB);
            itemTemplates.add(itemVai);
            itemTemplates.add(itemNangDS);
            itemTemplates.add(itemNheCung);
            itemTemplates.add(itemBasic);
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
       
        return itemTemplates;
    }

    static Vector<CharInfo> getTopLV(Database db) {
        Vector<CharInfo> top = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Statement stm = null;
        ResultSet rss = null;
        try {
            String sql = "select charname,id,gold,lastLv,headStyle,idClan,luong,pInfo,infotop,xp from tob_char where charname <> 'chienthan' and ban=0 order by xp desc limit 100";
            conn = db.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            int count = 0;
            while (rs.next()) {
                ++count;
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.level = rs.getByte("lastLv");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.idDB = rs.getInt("id");
                charinfo.money = rs.getLong("gold");
                charinfo.idClan = rs.getShort("idClan");
                charinfo.luong = rs.getInt("luong");
                charinfo.exp = rs.getLong("xp");
                String info = rs.getString("infotop");
                if (!info.equals("-1")) {
                    try {

                        String[] data = Char.split(info, ",");
                        charinfo.country = Byte.parseByte(data[0]);
                        for (short i = 1; i < data.length; i += 4) {
                            ItemInfo item = new ItemInfo();
                            item.idTemplate = Short.parseShort(data[i]);
                            item.charClass = Byte.parseByte(data[i + 1]);
                            item.level = Byte.parseByte(data[i + 2]);
                            item.plus = Byte.parseByte(data[i + 3]);
                            charinfo.wearingItem.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String[] data = Char.split(rs.getString("pInfo"), ",");
                    try {
                        charinfo.country = Byte.parseByte(data[39]);
                    } catch (Exception ignored) {
                    }
                    try {
                        sql = "select dbbasic from tob_item where owner=" + charinfo.idDB + " and place=1";
                        stm = conn.createStatement();
                        rss = stm.executeQuery(sql);
                        StringBuilder infoWearing = new StringBuilder(String.valueOf(charinfo.country));
                        byte[] iditem = new byte[13];
                        while (rss.next()) {
                            ItemInfo item2 = new ItemInfo();
                            Item it = new Item();
                            it.dbInfo = rss.getString("dbbasic");
                            it.initInfoFromDB();
                            if (it.getType() != 13) {
                                infoWearing.append(",").append(it.getTemplate().id).append(",").append(it.clazz).append(",").append(it.level).append(",").append(it.plus);
                                if (iditem[it.getType()] != 0) {
                                    continue;
                                }
                                item2.charClass = it.clazz;
                                item2.level = (byte) it.level;
                                item2.plus = it.plus;
                                item2.idTemplate = (short) it.getTemplate().id;
                                charinfo.wearingItem.add(item2);
                                short type = it.getType();
                                ++iditem[type];
                            }
                        }
                        db.updateInfoChar(charinfo.name, infoWearing.toString());
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    } finally {
                        try {
                            stm.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            rss.close();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        stm.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rss.close();
                    } catch (Exception ignored) {
                    }
                }
                top.add(charinfo);
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return top;
    }

    static Vector<CharInfo> getTopRich(Database db) {
        Vector<CharInfo> top = new Vector<>();
        Connection conn = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        Statement stm = null;
        ResultSet rss = null;
        try {
            String sql = "select charname,id,gold,lastLv,headStyle,idClan,luong,pInfo,infotop from tob_char where charname <> 'chienthan' and ban=0 order by gold desc limit 100";
            conn = db.getConnection();
            pre = conn.prepareStatement(sql);
            rs = pre.executeQuery();
            int count = 0;
            while (rs.next()) {
                ++count;
                CharInfo charinfo = new CharInfo();
                charinfo.name = rs.getString("charname");
                charinfo.level = rs.getByte("lastLv");
                charinfo.headStyle = rs.getByte("headStyle");
                charinfo.idDB = rs.getInt("id");
                charinfo.money = rs.getLong("gold");
                charinfo.idClan = rs.getShort("idClan");
                charinfo.luong = rs.getInt("luong");
                String info = rs.getString("infotop");
                if (!info.equals("-1")) {
                    try {
                        String[] data = Char.split(info, ",");
                        charinfo.country = Byte.parseByte(data[0]);
                        for (short i = 1; i < data.length; i += 4) {
                            ItemInfo item = new ItemInfo();
                            item.idTemplate = Short.parseShort(data[i]);
                            item.charClass = Byte.parseByte(data[i + 1]);
                            item.level = Byte.parseByte(data[i + 2]);
                            item.plus = Byte.parseByte(data[i + 3]);
                            charinfo.wearingItem.add(item);
                        }
                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        String[] data = Char.split(rs.getString("pInfo"), ",");
                        try {
                            charinfo.country = Byte.parseByte(data[39]);
                        } catch (Exception ignored) {
                        }
                        sql = "select dbbasic from tob_item where owner=" + charinfo.idDB + " and place=1";
                        stm = conn.createStatement();
                        rss = stm.executeQuery(sql);
                        StringBuilder infoWearing = new StringBuilder(String.valueOf(charinfo.country));
                        byte[] iditem = new byte[13];
                        while (rss.next()) {
                            ItemInfo item2 = new ItemInfo();
                            Item it = new Item();
                            it.dbInfo = rss.getString("dbbasic");
                            it.initInfoFromDB();
                            if (it.getType() != 13) {
                                infoWearing.append(",").append(it.getTemplate().id).append(",").append(it.clazz).append(",").append(it.level).append(",").append(it.plus);
                                if (iditem[it.getType()] != 0) {
                                    continue;
                                }
                                item2.charClass = it.clazz;
                                item2.level = (byte) it.level;
                                item2.plus = it.plus;
                                item2.idTemplate = (short) it.getTemplate().id;
                                charinfo.wearingItem.add(item2);
                                short type = it.getType();
                                ++iditem[type];
                            }
                        }
                        db.updateInfoChar(charinfo.name, infoWearing.toString());
                    } catch (Exception ignored) {
                    } finally {
                        try {
                            stm.close();
                        } catch (Exception ignored) {
                        }
                        try {
                            rss.close();
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        stm.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rss.close();
                    } catch (Exception ignored) {
                    }
                }
                top.add(charinfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (Exception ignored) {
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            rs.close();
        } catch (Exception ignored) {
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return top;
    }

    static void updateCharclass(Database db) {
        db.updateMoney();
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT * FROM tob_char where id>18634";
            pre = conn.prepareStatement(sql);
            ResultSet rs = pre.executeQuery();
            int charId = 0;
            int count = 0;
            while (rs.next()) {
                Vector<Date> nt = new Vector<>();
                ++count;
                Char p = new Char(null);
                Vector<Item> pItem = new Vector<>();
                p.charDBID = rs.getInt("id");
                charId = p.charDBID;
                p.setLuong(rs.getInt("luong"));
                p.dbInfo = rs.getString("pInfo");
                p.using = rs.getByte("del");
                p.dbPotion = rs.getString("potion");
                p.dbSkill = rs.getString("skill");
                p.dbBasic = rs.getString("basic");
                p.idClan = rs.getShort("idClan");
                nt.add(rs.getDate("lastlog"));
                db.getFriendlist(p);
                sql = "SELECT * FROM tob_item WHERE owner=? and place=1";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                ResultSet rss = pre.executeQuery();
                int nItem = 0;
                while (rss.next()) {
                    ++nItem;
                    Item item = new Item();
                    item.dbid = rss.getInt("id");
                    int int1 = rss.getInt("owner");
                    item.dbowner = int1;
                    item.owner = int1;
                    item.dbInfo = rss.getString("dbbasic");
                    if (item.dbInfo == null) {
                        int itemTemplate = rss.getInt("item_template");
                        item.setTemplate(itemTemplate, item.clazz = rss.getByte("class"), rss.getByte("class"), itemTemplate);
                        item.place = rss.getByte("place");
                        item.durable = rss.getShort("durable");
                        item.plus = rss.getByte("plus");
                        item.level = rss.getShort("lvItem");
                        if (item.level == -1 || item.level == 0) {
                            item.level = item.getTemplate().level;
                        }
                        item.mDurable = rss.getShort("mDurable");
                    }
                    item.dbAttribute = rss.getString("allAtt");
                    if (item.dbAttribute == null) {
                        item.atb[0] = rss.getShort("atb0");
                        item.atb[1] = rss.getShort("atb1");
                        item.atb[2] = rss.getShort("atb2");
                        item.atb[3] = rss.getShort("atb3");
                        item.atb[4] = rss.getShort("atb4");
                        item.atb[5] = rss.getShort("atb5");
                        item.atb[6] = rss.getShort("atb6");
                        item.atb[7] = rss.getShort("atb7");
                        item.atb[8] = rss.getShort("atb8");
                        item.atb[9] = rss.getShort("atb9");
                    }
                    pItem.add(item);
                    try {
                        Thread.sleep(1L);
                    } catch (Exception ignored) {
                    }
                }
                try {
                    p.initInfoFromDB(pItem, nt);
                    try {
                        Statement preu = conn.createStatement();
                        String sqlupdate = "update tob_char set pInfo='" + p.getDBInfo() + "' where charname='" + p.charname + "'";
                        db.sqlUpdate.add(sqlupdate);
                        System.out.println(count + " update " + p.charname);
                        try {
                            preu.close();
                            continue;
                        } catch (Exception ex3) {
                            continue;
                        }
                    } catch (Exception e) {
                        System.out.println(p.charname);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        System.exit(0);
    }

}
