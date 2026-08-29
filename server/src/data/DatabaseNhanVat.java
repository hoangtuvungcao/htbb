package data;

import io.Session;

import real.Map;
import real.*;
import server.TeamServer;
import util.Logger;
import java.sql.*;

import java.util.Date;
import java.util.*;


/**
 * Than cac method doc/ghi nhan vat trong CSDL: nap nhan vat luc dang nhap,
 * luu dinh ky, luu tu dong, luu ban lien dau, sao luu, tao nhan vat moi,
 * danh sach nhan vat cua tai khoan.
 *
 * Tach tu data/Database.java. Cac method VAN CON trong Database, giu NGUYEN
 * chu ky cu, chi than chuyen sang day - nen bytecode trong lib/NQSH_5h.jar
 * goi Database.getChar/saveChar... van chay binh thuong.
 * Tham so "Database db" thay cho "this" truoc day.
 */
class DatabaseNhanVat {

    static List<CharData> getCharList(Database db, int userID, Session s) {
        List<CharData> list = null;
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pre = null;
        StringBuilder listChar = new StringBuilder();
        try {
            conn = db.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM tob_char WHERE userId=? and idserver=" + s.idServer;
            if (TeamServer.isServerLienDau()) {
                sql = "SELECT * FROM tob_char WHERE userId=?";
            }
            pre = conn.prepareStatement(sql);
            pre.setInt(1, userID);
            ResultSet rs = pre.executeQuery();
            ResultSet rsItem = null;
            list = new Vector<>();
            while (rs.next()) {
                CharData ch = new CharData();
                if (rs.getInt("ban") == 0) {
                    ch.id = rs.getInt("id");
                    ch.charname = rs.getString("charname");
                    ch.headStyle = rs.getByte("headStyle");
                    ch.lv = rs.getShort("lastLv");
                    if (ch.lv == 0) {
                        ch.lv = 1;
                    }
                    ch.using = rs.getInt("del");
                    ch.timeDell = rs.getLong("tDelChar");
                    ch.charClass = rs.getByte("class");
                    if (ch.using == 0 && 7 - (int) ((System.currentTimeMillis() - ch.timeDell) / 1000L / 60L / 60L / 24L) <= 0) {
                        db.delChar(ch.id);
                    } else {
                        byte slot = 0;
                        int timeXP = rs.getShort("y");
                        try {
                            String[] data = Char.split(rs.getString("pInfo"), ",");
                            ch.gender = Byte.parseByte(data[1]);
                            slot = Byte.parseByte(data[36]);
                            ch.myCountry = Byte.parseByte(data[39]);
                            String dayLogin = data[43].trim();
                            if (!dayLogin.equals(Char.getDayOpen(0L))) {
                                timeXP = 0;
                            }
                        } catch (Exception ignored) {
                        }
                        if (s.timeXp < timeXP) {
                            s.timeXp = timeXP;
                        }
                        listChar.append(ch.charname).append("_").append(ch.id).append(" >> ");
                        String info = rs.getString("infotop");
                        try {
                            String[] data2 = Char.split(info, ",");
                            for (short i = 1; i < data2.length; i += 4) {
                                Item item = new Item();
                                item.setTemplate(Integer.parseInt(data2[i]), Integer.parseInt(data2[i + 1]), Integer.parseInt(data2[i + 1]), Integer.parseInt(data2[i]));
                                ch.item.add(item);
                                // Man chon nhan vat truoc day chi biet HAI phi phong, nen ai
                                // mang bong da hoac tim thi client thay idImgPP == -1 va
                                // roi ve ao choang thuong (Res.getCharPartInfo(4, ...)) —
                                // tuc hien sai phi phong. Bo sung cho du bon loai, lay dung
                                // bo so cua Char.getIdBigPhiPhong().
                                if (item.isPhiPhongKhongTuoc()) {
                                    ch.idBigAvtPP = Char.ID_BIG_AVT_PP_KHONG_TUOC;
                                    ch.indexXyPP = 0;
                                    ch.nFramePP = 3;
                                } else if (item.isPhiPhongHuyenVu()) {
                                    ch.idBigAvtPP = Char.ID_BIG_AVT_PP_HUYEN_VU;
                                    ch.indexXyPP = 1;
                                    ch.nFramePP = 3;
                                } else if (item.isPhiPhongBongDa()) {
                                    ch.idBigAvtPP = Char.ID_BIG_AVT_PP_BONG_DA;
                                    ch.indexXyPP = 1;
                                    ch.nFramePP = 3;
                                } else if (item.isPhiPhongTim()) {
                                    ch.idBigAvtPP = 714;
                                    ch.indexXyPP = 1;
                                    ch.nFramePP = 3;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        list.add(ch);
                    }
                }
            }
            try {
                if (rsItem != null) { rsItem.close(); }
            } catch (Exception ignored) {
            }
            try {
                rs.close();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            System.out.print("LOI KHI LOAD CHARLIST");
            s.sendMessage(MessageCreator.createServerAlertMessage("Máy chủ đang quá tải. Xin quay lại sau ít phút. Xin cảm ơn", ""));
        } finally {
            try {
                stmt.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
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
            stmt.close();
        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
       
        return list;
    }

    static Vector<Item> getChar(Database db, Char p, String charname, Vector<Date> lastlog) {
        Vector<Item> pItem = new Vector<>();
        Connection conn = null;
        int charId = 0;
        PreparedStatement pre = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT * FROM tob_char WHERE charname=? and ban=0";
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                charId = rs.getInt("userid");

                

                p.charDBID = charId;
                p.userID = charId;
                p.setxu(rs.getLong("gold"));
                p.setLuong(rs.getInt("luong"));
                p.tichluy = rs.getInt("tichluy");
                p.qua_npc = rs.getInt("qua_npc");
                p.tichluy_tuan = rs.getInt("tichluy_tuan");
                p.tichluy_bosung = rs.getInt("tichluy_bosung");
                p.dbInfo = rs.getString("pInfo");
                p.using = rs.getByte("del");
                p.dbPotion = rs.getString("potion");
                p.dbSkill = rs.getString("skill");
                p.dbBasic = rs.getString("basic");
                p.idClan = rs.getShort("idClan");
                p.reclass = rs.getByte("reupclass");
                p.flower = rs.getShort("ticket");
                p.gotWages = (rs.getShort("x") == 1);
                p.timeXP = rs.getShort("y");
                p.gifMoonFestival = (rs.getShort("book") == 1);
                lastlog.add(rs.getDate("lastlog"));
                p.giftTop = rs.getByte("nAr1");
                if (!NewClan.isTimeOfferWages()) {
                    p.gotWages = false;
                }
                try {
                    p.rankClan = rs.getByte("basepoint");
                    if (p.rankClan < 0) {
                        p.rankClan = 3;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p.initListKillMe(rs.getString("killme"));
                db.getFriendlist(p);
                p.equip = rs.getString("equip");
                p.invent = rs.getString("inven");
                p.bag = rs.getString("bag");
                p.tuido = rs.getString("tuido");
                if (p.equip == null) {
                    sql = "SELECT * FROM tob_item WHERE owner=?";
                    try {
                        pre.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rs.close();
                    } catch (Exception ignored) {
                    }
                    pre = conn.prepareStatement(sql);
                    pre.setInt(1, charId);
                    rs = pre.executeQuery();
                    int nItem = 0;
                    while (rs.next()) {
                        ++nItem;
                        Item item = new Item();
                        item.dbid = rs.getInt("id");
                        int int1 = rs.getInt("owner");
                        item.dbowner = int1;
                        item.owner = int1;
                        item.dbInfo = rs.getString("dbbasic");
                        item.dbAttribute = rs.getString("allAtt");
                        item.timeLoan = rs.getLong("timeLoan");
                        try {
                            item.identify = rs.getInt("atb0");
                            item.switchAttNguhanh = rs.getShort("atb1");
                            item.resetItem = rs.getShort("atb2");
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                        pItem.add(item);
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
                boolean haveNew = false;
                sql = "SELECT * FROM tob_gem_new WHERE owner=?";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                rs = pre.executeQuery();
                if (rs.next()) {
                    haveNew = true;
                    String[] soluong = Char.split(rs.getString("soluong"), ",");
                    int chardbid = rs.getInt("owner");
                    for (int i = 0; i < p.listGemitem.length; ++i) {
                        try {
                            int temsl = Integer.parseInt(soluong[i]);
                            if (temsl > 0) {
                                GemItem gem = new GemItem(i);
                                gem.ownerId = chardbid;
                                gem.realId = p.getIDItem();
                                p.gemItem.add(gem);
                                p.listGemitem[i] = temsl;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    String[] slock = Char.split(rs.getString("slock"), ",");
                    for (int j = 0; j < p.listGemitemLock.length; ++j) {
                        try {
                            int temsl2 = Integer.parseInt(slock[j]);
                            if (temsl2 > 0) {
                                p.addLockGem(j, temsl2);
                            }
                        } catch (Exception ignored) {
                        }
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
                boolean haveSpNew = false;
                sql = "SELECT * FROM tob_sp_new WHERE owner=?";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                rs = pre.executeQuery();
                if (rs.next()) {
                    haveSpNew = true;
                    String[] soluong2 = Char.split(rs.getString("soluong"), ",");
                    int chardbid2 = rs.getInt("owner");
                    for (int j = 0; j < soluong2.length; ++j) {
                        try {
                            int temsl2 = Integer.parseInt(soluong2[j]);
                            if (temsl2 > 0) {
                                ShopTemplate gem2 = new ShopTemplate();
                                ShopTemplate g = Map.getShopTemplate(j);
                                gem2.coppy(gem2, g);
                                gem2.ownerId = chardbid2;
                                gem2.realId = p.getIDItem();
                                p.specialItem.add(gem2);
                                p.listSpItem[j] = temsl2;
                            }
                        } catch (Exception ignored) {
                        }
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
                String lastlogout = null;
                try {
                    lastlogout = Char.split(p.lastLog.trim(), ".")[0].trim();
                } catch (Exception ignored) {
                }
                sql = "SELECT * FROM tob_animal WHERE owner=? and del=0";
                pre = conn.prepareStatement(sql);
                pre.setInt(1, charId);
                rs = pre.executeQuery();
                while (rs.next()) {
                    Animal animal = new Animal();
                    animal.place = rs.getByte("place");
                    animal.ownerId = rs.getInt("owner");
                    animal.dbid = rs.getInt("id");
                    animal.idImg = rs.getByte("id_img");
                    animal.dbownerId = animal.ownerId;
                    animal.initAtt(rs.getString("att"));
                    animal.id = p.getIDItem();
                    animal.level = rs.getByte("lv");
                    animal.name = rs.getString("name");
                    animal.texpire = rs.getLong("texpire");
                    animal.isHoaHinh = rs.getInt("isHoaHinh");
                    animal.timeHoaHinh =rs.getLong("timeHoaHinh");
                    if (lastlogout != null && lastlogout.compareTo("2017-12-28 11:40:00") < 0 && animal.idImg == Animal.TUAN_LOC) {
                        animal.texpire += 1296000000L;
                    }
                    if (TeamServer.isServerHoaLu() && lastlogout != null && lastlogout.compareTo("2020-04-13 10:25:00") < 0 && (animal.idImg == Animal.PHUONG_HOANG || animal.idImg == Animal.PHUONG_HOANG_7MAU || animal.idImg == Animal.PHUONG_HOANG_BANG || animal.idImg == Animal.HEO || animal.idImg == Animal.NGUA_XUONG) && animal.texpire > 0L) {
                        long[] day = {3L, 5L, 7L};
                        animal.texpire = System.currentTimeMillis() + day[Map.r.nextInt(day.length)] * 24L * 60L * 60000L;
                        db.saveAnimal(animal);
                    }

                    if (animal.timeExpire()) {
                        db.delAnimal(animal.dbid);
                    } else {
                        p.animal.add(animal);
                        if (animal.place != 1) {
                            continue;
                        }
                        p.animalRide = animal;
                    }
                }
                db.getInfoCharClan(p);
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            } else {
                System.out.println("FAIL");
            }
            db.getCharPet(p, conn);
            db.getVeso(p, conn);
            db.getIdGroupRaceChar(conn, p);
            db.getCharQuest(p);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
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
        return pItem;
    }

    static void saveCharBackup(Database db, Connection conn, Char p) {
        StringBuilder equip = new StringBuilder();
        StringBuilder invent = new StringBuilder();
        StringBuilder bag = new StringBuilder();
        try {
            Vector<Item> wearing = new Vector<>();
            wearing = p.getCharWearing(0);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.getCharWearing(1);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.awItems;
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            if (!equip.toString().equals("")) {
                equip = new StringBuilder(equip.substring(0, equip.length() - 1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            for (Item item2 : p.iItems) {
                invent.append(item2.getInfoSave()).append(">");
            }
            if (!invent.toString().equals("")) {
                invent = new StringBuilder(invent.substring(0, invent.length() - 1));
            }
            String infomodel = p.wModel.getInfoModelWearing();
            if (!infomodel.equals("")) {
                invent.append(">").append(infomodel);
            }
        } catch (Exception ignored) {
        }
        try {
            for (Item item2 : p.bItem) {
                bag.append(item2.getInfoSave()).append(">");
            }
            if (!bag.toString().equals("")) {
                bag = new StringBuilder(bag.substring(0, bag.length() - 1));
            }
        } catch (Exception ignored) {
        }
        PreparedStatement pre = null;
        PreparedStatement pre2 = null;
        try {
            String sql = "INSERT INTO tob_char2(pInfo,basic,skill,potion,totalTimePlay,luong,lastLv,xp,gold,hp, idClan, reupclass,headStyle,ticket,x,y,book,infotop,id,charname,userid,class,gender,equip,inven,bag) values(?,?,?,?,totalTimePlay+?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            StringBuilder potion = new StringBuilder(String.valueOf(p.getxu()));
            StringBuilder skill = new StringBuilder(String.valueOf(p.skill[0]));
            String basic = String.valueOf(p.strength) + "," + p.agitity + "," + p.spirit + "," + p.health + "," + p.luck + "," + p.basepoint + "," + p.skillpoint;
            for (int j = 1; j < p.potions.length; ++j) {
                potion.append(",").append(p.potions[j]);
            }
            for (int j = 1; j < p.skill.length; ++j) {
                skill.append(",").append(p.skill[j]);
            }
            for (int j = 0; j < p.skillClan.length; ++j) {
                skill.append(",").append(p.skillClan[j]);
            }
            for (int j = 0; j < p.skillLvClan.length; ++j) {
                skill.append(",").append(p.skillLvClan[j]);
            }
            String dbInfo = p.getDBInfo();
            int totalPlay = (int) (System.currentTimeMillis() - p.timePlay) / 60000;
            pre2 = conn.prepareStatement(sql);
            pre2.setString(1, dbInfo);
            pre2.setString(2, basic);
            pre2.setString(3, skill.toString());
            pre2.setString(4, potion.toString());
            pre2.setLong(5, totalPlay);
            pre2.setInt(6, Math.max(p.getLuong(), 0));
            pre2.setInt(7, p.lastLV);
            pre2.setLong(8, p.lvDetail.getExp());
            try {
                pre2.setLong(9, p.getxu());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            pre2.setInt(10, p.health);
            pre2.setInt(11, p.idClan);
            pre2.setInt(12, p.reclass);
            pre2.setInt(13, p.headStyle);
            pre2.setInt(14, p.flower);
            pre2.setInt(15, p.gotWages ? 1 : 0);
            pre2.setInt(16, p.timeXP);
            pre2.setInt(17, p.gifMoonFestival ? 1 : 0);
            pre2.setString(18, p.infoWearing);
            pre2.setInt(19, p.charDBID);
            pre2.setString(20, p.charname);
            pre2.setInt(21, p.userID);
            pre2.setInt(22, p.charClass);
            pre2.setInt(23, p.gender);
            pre2.setString(24, equip.toString());
            pre2.setString(25, invent.toString());
            pre2.setString(26, bag.toString());
            try {
                pre2.execute();
            } catch (Exception e2) {
                sql = "UPDATE tob_char2 SET pInfo=?,basic=?,skill=?,potion=?,totalTimePlay=totalTimePlay+?, luong=?, lastLv=?,xp=?,gold=?,hp=?, idClan=?, reupclass=?,headStyle=?,ticket=?,x=?,y=?,book=?,lastLog=Now(),infotop=?,equip=?,inven=?,bag=?,killme=? where charname=?";
                pre = conn.prepareStatement(sql);
                pre.setString(1, dbInfo);
                pre.setString(2, basic);
                pre.setString(3, skill.toString());
                pre.setString(4, potion.toString());
                pre.setLong(5, totalPlay);
                pre.setInt(6, Math.max(p.getLuong(), 0));
                pre.setInt(7, p.lastLV);
                pre.setLong(8, p.lvDetail.getExp());
                try {
                    pre.setLong(9, p.getxu());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                pre.setInt(10, p.health);
                pre.setInt(11, p.idClan);
                pre.setInt(12, p.reclass);
                pre.setInt(13, p.headStyle);
                pre.setInt(14, p.flower);
                pre.setInt(15, p.gotWages ? 1 : 0);
                pre.setInt(16, p.timeXP);
                pre.setInt(17, p.gifMoonFestival ? 1 : 0);
                pre.setString(18, p.infoWearing);
                pre.setString(19, equip.toString());
                pre.setString(20, invent.toString());
                pre.setString(21, bag.toString());
                pre.setString(22, p.getSaveListKillMe());
                pre.setString(23, p.charname);
                pre.execute();
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                pre2.close();
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            pre2.close();
        } catch (Exception ignored) {
        }
    }

    static boolean saveChar(Database db, Char p, boolean admindExit) {
        if (p == null) {
            return false;
        }
        if (p.isBot != -1) {
            return false;
        }
        if (p.receiveQuest) {
            p.createCharQuest();
            db.saveCharQuest(p.charDBID, p.char_quest);
        }
        try {
            NewClan clanInfo = Map.getClaninfoByID(p.idClan);
            if (clanInfo != null && clanInfo.master.equalsIgnoreCase(p.charname)) {
                clanInfo.updateNewClandata2DB();
            }
        } catch (Exception ignored) {
        }
        int totalPlay = (int) (System.currentTimeMillis() - p.timePlay) / 60000;
        StringBuilder potion = new StringBuilder(String.valueOf(p.getxu()));
        StringBuilder skill = new StringBuilder(String.valueOf(p.skill[0]));
        String basic = String.valueOf(p.strength) + "," + p.agitity + "," + p.spirit + "," + p.health + "," + p.luck + "," + p.basepoint + "," + p.skillpoint;
        for (int i = 1; i < p.potions.length; ++i) {
            potion.append(",").append(p.potions[i]);
        }
        for (int i = 1; i < p.skill.length; ++i) {
            skill.append(",").append(p.skill[i]);
        }
        for (int i = 0; i < p.skillClan.length; ++i) {
            skill.append(",").append(p.skillClan[i]);
        }
        for (int i = 0; i < p.skillLvClan.length; ++i) {
            skill.append(",").append(p.skillLvClan[i]);
        }
        String dbInfo = p.getDBInfo();
        StringBuilder equip = new StringBuilder();
        StringBuilder invent = new StringBuilder();
        StringBuilder bag = new StringBuilder();
        StringBuilder tuido = new StringBuilder();
        try {
            Vector<Item> wearing = new Vector<>();
            wearing = p.getCharWearing(0);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.getCharWearing(1);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.awItems;
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            if (!equip.toString().equals("")) {
                equip = new StringBuilder(equip.substring(0, equip.length() - 1));
            }
        } catch (Exception ignored) {
        }
        try {
            if (p.itemHoaKyLan != null) {
                invent.append(p.itemHoaKyLan.getInfoSave()).append(">");
            }
            if (p.itemVukhiThoiTrang != null) {
                invent.append(p.itemVukhiThoiTrang.getInfoSave()).append(">");
            }
            if (p.itemThanKhi != null) {
                invent.append(p.itemThanKhi.getInfoSave()).append(">");
            }
            for (Item item2 : p.iItems) {
                invent.append(item2.getInfoSave()).append(">");
            }
            if (!invent.toString().equals("")) {
                invent = new StringBuilder(invent.substring(0, invent.length() - 1));
            }
            String infomodel = p.wModel.getInfoModelWearing();
            if (!infomodel.equals("")) {
                invent.append(">").append(infomodel);
            }
        } catch (Exception ignored) {
        }
        try {
            for (Item item2 : p.bItem) {
                bag.append(item2.getInfoSave()).append(">");
            }
            if (!bag.toString().equals("")) {
                bag = new StringBuilder(bag.substring(0, bag.length() - 1));
            }
        } catch (Exception ignored) {
        }
        try {
            for (int k = 0; k < p.bagItems.size(); ++k) {
                tuido.append(p.bagItems.get(k).getInfoSave()).append(">");
            }
            if (!tuido.toString().equals("")) {
                tuido = new StringBuilder(tuido.substring(0, tuido.length() - 1));
            }
        } catch (Exception ignored) {
        }
        Connection conn = null;
        PreparedStatement pre = null;
        String sql = "UPDATE tob_char SET pInfo=?,basic=?,skill=?,potion=?,totalTimePlay=totalTimePlay+?, luong=?, lastLv=?,xp=?,gold=?,hp=?, idClan=?, reupclass=?,headStyle=?,ticket=?,x=?,y=?,book=?,lastLog=Now(),infotop=?,equip=?,"
                + "inven=?,bag=?,killme=?,luonglock=?,tuido=?,isPP=?,isMN=? where id=?";
        try {
            conn = db.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, dbInfo);
            pre.setString(2, basic);
            pre.setString(3, skill.toString());
            pre.setString(4, potion.toString());
            pre.setLong(5, totalPlay);
            pre.setInt(6, Math.max(p.getLuong(), 0));
            pre.setInt(7, p.lastLV);
            pre.setLong(8, p.lvDetail.getExp());
            try {
                pre.setLong(9, p.getxu());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pre.setInt(10, p.health);
            pre.setInt(11, p.idClan);
            pre.setInt(12, p.reclass);
            pre.setInt(13, p.headStyle);
            pre.setInt(14, p.flower);
            pre.setInt(15, p.gotWages ? 1 : 0);
            pre.setInt(16, p.timeXP);
            pre.setInt(17, p.gifMoonFestival ? 20 : 0);
            pre.setString(18, p.infoWearing);
            pre.setString(19, equip.toString());
            pre.setString(20, invent.toString());
            pre.setString(21, bag.toString());
            pre.setString(22, p.getSaveListKillMe());
            pre.setInt(23, p.getLuongLock());
            pre.setString(24, tuido.toString());
            pre.setByte(25, (byte) (p.isTatPhiPhong() ? 1 : 0));
            pre.setByte(26, (byte) (p.isTatMatNa() ? 1 : 0));
            pre.setInt(27, p.charDBID);
            try {
                pre.execute();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    db.saveOrtherLog("", p.charname, "banchar khi loi invent", "loiinvent");
                    // Hanh trang khong luu duoc -> server TU KHOA nhan vat ngay ben duoi.
                    // Thuong la cot inven khong du cho chu khong phai gian lan, nhung van
                    // phai vao hack.txt: day la luc nguoi choi mat do va can co dau vet.
                    NghiVanGianLan.ghiChung(p, "loiinvent",
                            "luu hanh trang that bai nen nhan vat bi khoa - " + e);
                    sql = "UPDATE tob_char SET ban=1 where charname='" + p.charname + "'";
                    conn = db.getConnection();
                    Statement pree = conn.createStatement();
                    pree.execute(sql);
                    pree.close();
                } catch (Exception ignored) {
                }
                System.out.println("LOI excute char " + e);
            }
            try {
                if (p.charDBID > 0) {
                    StringBuilder soluong = new StringBuilder(String.valueOf(p.listGemitem[0]));
                    for (int l = 1; l < p.listGemitem.length; ++l) {
                        soluong.append(",").append(p.listGemitem[l]);
                    }
                    StringBuilder soluong2 = new StringBuilder(String.valueOf(p.listGemitemLock[0]));
                    for (int m = 1; m < p.listGemitemLock.length; ++m) {
                        soluong2.append(",").append(p.listGemitemLock[m]);
                    }
                    db.saveCharGem(soluong.toString(), p.charDBID, soluong2.toString());
                }
            } catch (Exception ignored) {
            }
            try {
                do {
                    Animal gem = p.animal.remove(0);
                    if (gem.ownerId != p.charDBID) {
                        continue;
                    }
                    db.saveAnimal(conn, gem);
                } while (p.animal.size() > 0);
            } catch (Exception ignored) {
            }
            try {
                if (p.charDBID > 0) {
                    StringBuilder soluong = new StringBuilder(String.valueOf(p.listSpItem[0]));
                    for (int l = 1; l < p.listSpItem.length; ++l) {
                        soluong.append(",").append(p.listSpItem[l]);
                    }
                    db.saveCharSP(soluong.toString(), p.charDBID);
                }
            } catch (Exception ignored) {
            }
            try {
                for (int i2 = 0; i2 < p.seedsItem.size(); ++i2) {
                    db.saveFarm(p.seedsItem.get(i2), conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                do {
                    Pet gem2 = p.pet.remove(0);
                    if (gem2.ownerid != p.charDBID) {
                        continue;
                    }
                    db.savePet(gem2);
                } while (p.pet.size() > 0);
            } catch (Exception ignored) {
            }
            db.savecharLuong(conn, p);
            if (p.idGropRace > -1 && Database.createRace < 2) {
                db.updateCharRace(conn, p);
            }
            db.saveCharQuest(p.charDBID, p.getInfoQuestSave());
            db.saveCharFruit(p.charDBID, p.getInfoSaveFruit());
            db.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.logError(ex, p.charname + ".txt");
            return false;
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
        // Han muc luong nhat tu train quai trong ngay (chi ghi khi co thay doi)
        try {
            if (p.trainLuongDirty) {
                db.saveTrainLuong(p);
                p.trainLuongDirty = false;
            }
        } catch (Exception ignored) {
        }
        ++TeamServer.update;
        return true;
    }

    static boolean saveCharAuto(Database db, Char p) {
        if (p == null) {
            return false;
        }
        if (p.isBot != -1) {
            return false;
        }
        if (p.receiveQuest) {
            p.createCharQuest();
            db.saveCharQuest(p.charDBID, p.char_quest);
        }
        try {
            NewClan clanInfo = Map.getClaninfoByID(p.idClan);
            if (clanInfo != null && clanInfo.master.equalsIgnoreCase(p.charname)) {
                clanInfo.updateNewClandata2DB();
            }
        } catch (Exception ignored) {
        }
        int totalPlay = (int) (System.currentTimeMillis() - p.timePlay) / 60000;
        StringBuilder potion = new StringBuilder(String.valueOf(p.getxu()));
        StringBuilder skill = new StringBuilder(String.valueOf(p.skill[0]));
        String basic = String.valueOf(p.strength) + "," + p.agitity + "," + p.spirit + "," + p.health + "," + p.luck + "," + p.basepoint + "," + p.skillpoint;
        for (int i = 1; i < p.potions.length; ++i) {
            potion.append(",").append(p.potions[i]);
        }
        for (int i = 1; i < p.skill.length; ++i) {
            skill.append(",").append(p.skill[i]);
        }
        for (int i = 0; i < p.skillClan.length; ++i) {
            skill.append(",").append(p.skillClan[i]);
        }
        for (int i = 0; i < p.skillLvClan.length; ++i) {
            skill.append(",").append(p.skillLvClan[i]);
        }
        String dbInfo = p.getDBInfo();
        StringBuilder equip = new StringBuilder();
        StringBuilder invent = new StringBuilder();
        StringBuilder bag = new StringBuilder();
        StringBuilder tuido = new StringBuilder();
        try {
            Vector<Item> wearing = new Vector<>();
            wearing = p.getCharWearing(0);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.getCharWearing(1);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.awItems;
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            if (!equip.toString().equals("")) {
                equip = new StringBuilder(equip.substring(0, equip.length() - 1));
            }
        } catch (Exception ignored) {
        }
        try {
            if (p.itemHoaKyLan != null) {
                invent.append(p.itemHoaKyLan.getInfoSave()).append(">");
            }
            if (p.itemVukhiThoiTrang != null) {
                invent.append(p.itemVukhiThoiTrang.getInfoSave()).append(">");
            } 
            if (p.itemThanKhi != null) {
                invent.append(p.itemThanKhi.getInfoSave()).append(">");
            }
            for (Item item2 : p.iItems) {
                invent.append(item2.getInfoSave()).append(">");
            }
            if (!invent.toString().equals("")) {
                invent = new StringBuilder(invent.substring(0, invent.length() - 1));
            }
            String infomodel = p.wModel.getInfoModelWearing();
            if (!infomodel.equals("")) {
                invent.append(">").append(infomodel);
            }
        } catch (Exception ignored) {
        }
        try {
            for (Item item2 : p.bItem) {
                bag.append(item2.getInfoSave()).append(">");
            }
            if (!bag.toString().equals("")) {
                bag = new StringBuilder(bag.substring(0, bag.length() - 1));
            }
        } catch (Exception ignored) {
        }
        try {
            for (int k = 0; k < p.bagItems.size(); ++k) {
                tuido.append(p.bagItems.get(k).getInfoSave()).append(">");
            }
            if (!tuido.toString().equals("")) {
                tuido = new StringBuilder(tuido.substring(0, tuido.length() - 1));
            }
        } catch (Exception ignored) {
        }
        final String sql = "UPDATE tob_char SET pInfo=?,basic=?,skill=?,potion=?,totalTimePlay=totalTimePlay+?, luong=?, lastLv=?,xp=?,gold=?,hp=?, idClan=?, reupclass=?,headStyle=?,ticket=?,x=?,y=?,book=?,lastLog=Now(),infotop=?,equip=?,inven=?,bag=?,luonglock=?,tuido=? where id=?";
        Connection conn = null;
        PreparedStatement pre = null;
        try {
            conn = db.getConnection();
            pre = conn.prepareStatement(sql);
            pre.setString(1, dbInfo);
            pre.setString(2, basic);
            pre.setString(3, skill.toString());
            pre.setString(4, potion.toString());
            pre.setLong(5, totalPlay);
            pre.setInt(6, Math.max(p.getLuong(), 0));
            pre.setInt(7, p.lastLV);
            pre.setLong(8, p.lvDetail.getExp());
            try {
                pre.setLong(9, p.getxu());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pre.setInt(10, p.health);
            pre.setInt(11, p.idClan);
            pre.setInt(12, p.reclass);
            pre.setInt(13, p.headStyle);
            pre.setInt(14, p.flower);
            pre.setInt(15, p.gotWages ? 1 : 0);
            pre.setInt(16, p.timeXP);
            pre.setInt(17, p.gifMoonFestival ? 1 : 0);
            pre.setString(18, p.infoWearing);
            pre.setString(19, equip.toString());
            pre.setString(20, invent.toString());
            pre.setString(21, bag.toString());
            pre.setInt(22, p.getLuongLock());
            pre.setString(23, tuido.toString());
            pre.setInt(24, p.charDBID);
            try {
                pre.execute();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("LOI excute char " + e);
            }
            try {
                if (p.charDBID > 0) {
                    StringBuilder soluong = new StringBuilder(String.valueOf(p.listGemitem[0]));
                    for (int l = 1; l < p.listGemitem.length; ++l) {
                        soluong.append(",").append(p.listGemitem[l]);
                    }
                    StringBuilder soluong2 = new StringBuilder(String.valueOf(p.listGemitemLock[0]));
                    for (int m = 1; m < p.listGemitemLock.length; ++m) {
                        soluong2.append(",").append(p.listGemitemLock[m]);
                    }
                    db.saveCharGem(soluong.toString(), p.charDBID, soluong2.toString());
                }
            } catch (Exception ignored) {
            }
            try {
                int size = p.animal.size();
                for (short i2 = 0; i2 < size; ++i2) {
                    Animal gem = p.animal.get(i2);
                    if (gem.ownerId == p.charDBID) {
                        if (gem.dbownerId == 0) {
                            db.saveAnimal(conn, gem);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                for (int i3 = 0; i3 < p.seedsItem.size(); ++i3) {
                    db.saveFarm(p.seedsItem.get(i3), conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println(" LOI SAVE CHAR " + p.charname + " | " + p.lvDetail.getExp());
            String logError = "UPDATE tob_char SET pInfo=" + dbInfo + ",basic=" + basic + ",skill=" + skill + ",potion=" + potion + ",totalTimePlay=totalTimePlay+" + totalPlay + ", luong=" + p.getLuong() + ", lastLv=" + p.lastLV + ",xp=" + p.lvDetail.getExp() + ",gold=" + p.getxu() + ",hp=" + p.hp + ",idClan=" + p.idClan + ",headStyle=" + p.headStyle + " where id=" + p.charDBID;
            Logger.logString(logError, "savechar.txt");
            return false;
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
        // Han muc luong nhat tu train quai trong ngay (chi ghi khi co thay doi)
        try {
            if (p.trainLuongDirty) {
                db.saveTrainLuong(p);
                p.trainLuongDirty = false;
            }
        } catch (Exception ignored) {
        }
        ++TeamServer.update;
        return true;
    }

    static int addChar(Database db, Session s, String charname, int headStyle, int classID, int gender, int nation) {
        int userID = s.userID;
        if (gender + 1 < 1 || gender + 1 > 2) {
            return -1;
        }
        // Ten danh rieng: khong cho nguoi choi tao. Ly do: sv.adminAccounts khop
        // theo charname, nen ai dat trung mot trong cac ten do se thanh admin.
        if (Database.laTenDanhRieng(charname)) {
            return -1;
        }
        int charId = -1;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = db.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM tob_char WHERE charname='" + charname + "'";
            if (stmt.execute(sql)) {
                ResultSet rs = stmt.getResultSet();
                boolean trungTen = rs.next();
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (trungTen) {
                    // Khong tu dong dong stmt/conn o day, khoi finally ben duoi lo viec do.
                    // Neu dong 2 lan, cung 1 connection se bi tra ve pool 2 lan.
                    return -1;
                }
            }
            // Chi dem nhan vat con song (del=1), khong bi ban va thuoc dung server dang choi,
            // dung mot server khac cua cung tai khoan lam het slot.
            sql = "SELECT COUNT(*) FROM tob_char WHERE userId=" + userID
                    + " AND idserver=" + s.idServer + " AND ban=0 AND del=1";
            int countChar = 0;
            try {
                ResultSet rs2 = stmt.executeQuery(sql);
                if (rs2.next()) {
                    countChar = rs2.getInt(1);
                }
                try {
                    rs2.close();
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
            // "first" chi de tang xu cho nhan vat dau tien: dem het moi nhan vat tung tao
            // (ke ca da xoa / server khac) de khong the xoa char roi tao lai de an xu.
            boolean first = true;
            try {
                ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM tob_char WHERE userId=" + userID);
                if (rs3.next()) {
                    first = rs3.getInt(1) == 0;
                }
                try {
                    rs3.close();
                } catch (Exception ignored) {
                }
            } catch (Exception ignored) {
            }
            CharTemplate ct;
            if (gender != -1) {
                ct = Database.charTemplates[classID][gender];
            } else {
                ct = Database.charTemplatesOld[classID];
            }
            int[] tempPotion = new int[162];
            tempPotion[4] = (tempPotion[1] = 1);
            long xu = 0L;
            StringBuilder potion = new StringBuilder("0," + ct.nHP1 + ",0,0," + ct.nMP1 + ",0,0");
            if (first) {
                if (s.privateProvider.equals("1") && s.bigProvider.equals("1")) {
                    xu = 100000L;
                } else if (s.bigProvider.equals("0")) {
                    xu = 100000L;
                }
            }
            if (countChar >= Database.MAX_CHAR_PER_ACCOUNT) { // tạo nhân vật
                return -1;
            }
            potion = new StringBuilder(String.valueOf(xu));
            for (int i = 1; i < tempPotion.length; ++i) {
                potion.append(",").append(tempPotion[i]);
            }
            final String stSkill = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
            int[] cSkill = {6, 6, 8, 6, 6};
            String[] data = Char.split(stSkill, ",");
            int[] mainSkill = new int[15];
            for (int j = 0; j < data.length; ++j) {
                mainSkill[j] = -1;
                try {
                    if (j < cSkill[classID]) {
                        mainSkill[j] = Byte.parseByte(data[j]);
                    }
                } catch (Exception ignored) {
                }
            }
            StringBuilder skill = new StringBuilder(String.valueOf(mainSkill[0]));
            ct.strength = Database.basicPoint[classID][0];
            ct.agitity = Database.basicPoint[classID][1];
            ct.spirit = Database.basicPoint[classID][2];
            ct.dextrity = Database.basicPoint[classID][3];
            ct.luck = Database.basicPoint[classID][4];
            String basic = ct.strength + "," + ct.agitity + "," + ct.spirit + "," + ct.dextrity + "," + ct.luck + ",0,0";
            for (int k = 7; k < 25; ++k) {
                potion.append(",0");
                if (k < 21) {
                    skill.append(",").append(mainSkill[k - 7]);
                }
            }
            potion.append(",0,0,0,0,0,0,0,0,0,0,0,0,0");
            int maxhp = 0;
            int maxmp = 0;
            switch (classID) {
                case 0: {
                    maxhp = ct.dextrity * 80;
                    maxmp = ct.spirit * 20;
                    break;
                }
                case 1:
                case 4:
                case 3: {
                    maxhp = ct.dextrity * 60;
                    maxmp = ct.spirit * 20;
                    break;
                }
                case 2: {
                    maxhp = ct.dextrity * 60;
                    maxmp = ct.spirit * 50;
                    break;
                }
            }
      
            ct.hp = maxhp;
            ct.mp = maxmp;
            int aoTemplate = (ct.gender == 1) ? 1 : 2;
            int quanTemplate = (ct.gender == 1) ? 27 : 28;
            int[] defaultVK = {79, 86, 93, 100, 107, 79, 86, 93, 100, 107};
            int vkhi = defaultVK[classID];
            String pInfo = ct.getDbInfo(headStyle, gender + 1, charname, classID, maxhp, maxmp, 105, nation);
            int idAo = Map.itemTemplates.get(classID).get(aoTemplate).id;
            int quanB = Map.itemTemplates.get(classID).get(quanTemplate).id;
            int vukhi = Map.itemTemplates.get(classID).get(vkhi).id;
            int vkTemplate = defaultVK[classID];
            Item ao = new Item(Map.itemTemplates.get(classID).get(aoTemplate), false, classID, classID, aoTemplate);
            Item quan = new Item(Map.itemTemplates.get(classID).get(quanTemplate), false, classID, classID, quanTemplate);
            Item vk = new Item(Map.itemTemplates.get(classID).get(vkTemplate), false, classID, classID, vkTemplate);
            ao.place = 1;
            quan.place = 1;
            vk.place = 1;
            String equip = ao.getInfoSave() + ">" + quan.getInfoSave() + ">" + vk.getInfoSave();
            String infoTop = nation + "," + idAo + "," + classID + ",1,0," + quanB + "," + classID + ",1,0," + vukhi + "," + classID + ",1,0";
            sql = "INSERT INTO tob_char(charname,userId,headStyle,class,pInfo,potion,skill,basic,gender,infotop,gold,equip,idserver) VALUES('" + charname + "'," + userID + "," + headStyle + "," + classID + ",'" + pInfo + "','" + potion + "','" + skill + "','" + basic + "'," + (gender + 1) + ",'" + infoTop + "'," + xu + ",'" + equip + "'," + s.idServer + ")";
            stmt.execute(sql);
            // Nhan vat dau tien va duy nhat cua tai khoan: tang 50000 luong mo + 250000 luong khoa,
            // cong qua hang doi board_created (nhu Nhan moc nap), Char se nhan khi vao game lan dau
            // qua Database.getBoardCreated(), khong dung sua truc tiep pInfo (fragile, xem [[feedback-kpah-safe-npc-edits]]).
            if (first) {
                stmt.execute("INSERT INTO board_created(xu,luong,luongK,username) VALUES(50000000,50000,250000,'" + userID + "')");
            }
            sql = "SELECT * FROM tob_char WHERE charname='" + charname + "'";
            if (stmt.execute(sql)) {
                ResultSet rs3 = stmt.getResultSet();
                if (rs3.next()) {
                    charId = rs3.getInt("id");
                }
                try {
                    rs3.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                String sqlCopy = "INSERT INTO tob_char2(id,charname,userId,headStyle,class,pInfo,potion,skill,basic,gender) VALUES(" + charId + ",'" + charname + "'," + userID + "," + headStyle + "," + classID + ",'" + pInfo + "','" + potion + "','" + skill + "','" + basic + "'," + (gender + 1) + ")";
                db.addBackupChar(conn, sqlCopy);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            Database.connPool.free(conn);
        } catch (Exception ignored) {
        }
        return charId;
    }

    static void saveCharLienDau(Database db, Char p, int[] infoCharLiendau) {
        if (p == null) {
            return;
        }
        if (p.isBot != -1) {
            return;
        }
        int charid = infoCharLiendau[0];
        int totalPlay = (int) (System.currentTimeMillis() - p.timePlay) / 60000;
        StringBuilder potion = new StringBuilder(String.valueOf(p.getxu()));
        StringBuilder skill = new StringBuilder(String.valueOf(p.skill[0]));
        String basic = String.valueOf(p.strength) + "," + p.agitity + "," + p.spirit + "," + p.health + "," + p.luck + "," + p.basepoint + "," + p.skillpoint;
        for (int i = 1; i < p.potions.length; ++i) {
            potion.append(",").append(p.potions[i]);
        }
        for (int i = 1; i < p.skill.length; ++i) {
            skill.append(",").append(p.skill[i]);
        }
        for (int i = 0; i < p.skillClan.length; ++i) {
            skill.append(",").append(p.skillClan[i]);
        }
        for (int i = 0; i < p.skillLvClan.length; ++i) {
            skill.append(",").append(p.skillLvClan[i]);
        }
        String dbInfo = p.getDBInfo();
        StringBuilder equip = new StringBuilder();
        StringBuilder invent = new StringBuilder();
        StringBuilder bag = new StringBuilder();
        try {
            Vector<Item> wearing = new Vector<>();
            wearing = p.getCharWearing(0);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.getCharWearing(1);
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            wearing = p.awItems;
            for (Item item : wearing) {
                equip.append(item.getInfoSave()).append(">");
            }
            if (!equip.toString().equals("")) {
                equip = new StringBuilder(equip.substring(0, equip.length() - 1));
            }
        } catch (Exception ignored) {
        }
        try {
            if (p.itemHoaKyLan != null) {
                invent.append(p.itemHoaKyLan.getInfoSave()).append(">");
            }
            if (p.itemVukhiThoiTrang != null) {
                invent.append(p.itemVukhiThoiTrang.getInfoSave()).append(">");
            } 
            if (p.itemThanKhi != null) {
                invent.append(p.itemThanKhi.getInfoSave()).append(">");
            }
            for (Item item2 : p.iItems) {
                invent.append(item2.getInfoSave()).append(">");
            }
            if (!invent.toString().equals("")) {
                invent = new StringBuilder(invent.substring(0, invent.length() - 1));
            }
            String infomodel = p.wModel.getInfoModelWearing();
            if (!infomodel.equals("")) {
                invent.append(">").append(infomodel);
            }
        } catch (Exception ignored) {
        }
        try {
            for (Item item2 : p.bItem) {
                bag.append(item2.getInfoSave()).append(">");
            }
            if (!bag.toString().equals("")) {
                bag = new StringBuilder(bag.substring(0, bag.length() - 1));
            }
        } catch (Exception ignored) {
        }
        Connection conn = null;
        PreparedStatement pre = null;
        final String sql = "UPDATE tob_char SET pInfo=?,basic=?,skill=?,potion=?,totalTimePlay=totalTimePlay+?, luong=?, lastLv=?,xp=?,gold=?,hp=?, idClan=?, reupclass=?,headStyle=?,ticket=?,x=?,y=?,book=?,lastLog=Now(),infotop=?,equip=?,inven=?,bag=?,killme=?,luonglock=? where id=?";
        try {
            conn = db.getConnection1();
            pre = conn.prepareStatement(sql);
            pre.setString(1, dbInfo);
            pre.setString(2, basic);
            pre.setString(3, skill.toString());
            pre.setString(4, potion.toString());
            pre.setLong(5, totalPlay);
            pre.setInt(6, Math.max(p.getLuong(), 0));
            pre.setInt(7, p.lastLV);
            pre.setLong(8, p.lvDetail.getExp());
            try {
                pre.setLong(9, p.getxu());
            } catch (Exception e) {
                e.printStackTrace();
            }
            pre.setInt(10, p.health);
            pre.setInt(11, p.idClan);
            pre.setInt(12, p.reclass);
            pre.setInt(13, p.headStyle);
            pre.setInt(14, p.flower);
            pre.setInt(15, p.gotWages ? 1 : 0);
            pre.setInt(16, p.timeXP);
            pre.setInt(17, p.gifMoonFestival ? 20 : 0);
            pre.setString(18, p.infoWearing);
            pre.setString(19, equip.toString());
            pre.setString(20, invent.toString());
            pre.setString(21, bag.toString());
            pre.setString(22, p.getSaveListKillMe());
            pre.setInt(23, p.getLuongLock());
            pre.setInt(24, charid);
            try {
                pre.execute();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("LOI excute char " + e);
            }
            try {
                if (charid > 0) {
                    StringBuilder soluong = new StringBuilder(String.valueOf(p.listGemitem[0]));
                    for (int k = 1; k < p.listGemitem.length; ++k) {
                        soluong.append(",").append(p.listGemitem[k]);
                    }
                    StringBuilder soluong2 = new StringBuilder(String.valueOf(p.listGemitemLock[0]));
                    for (int l = 1; l < p.listGemitemLock.length; ++l) {
                        soluong2.append(",").append(p.listGemitemLock[l]);
                    }
                    db.saveCharGem1(soluong.toString(), charid, soluong2.toString());
                }
            } catch (Exception ignored) {
            }
            try {
                for (int m = 0; m < p.animal.size(); ++m) {
                    Animal gem = p.animal.get(m);
                    if (gem.ownerId == p.charDBID) {
                        gem = gem.clone(charid);
                        if (infoCharLiendau[1] > -1) {
                            gem.dbownerId = gem.ownerId;
                        }
                        db.saveAnimal1(conn, gem, p.animal.get(m).dbid + "_" + Database.nameServer[TeamServer.server]);
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                if (charid > 0) {
                    StringBuilder soluong = new StringBuilder(String.valueOf(p.listSpItem[0]));
                    for (int k = 1; k < p.listSpItem.length; ++k) {
                        soluong.append(",").append(p.listSpItem[k]);
                    }
                    db.saveCharSP1(soluong.toString(), charid);
                }
            } catch (Exception ignored) {
            }
            try {
                for (int m = 0; m < p.pet.size(); ++m) {
                    Pet gem2 = p.pet.get(m);
                    if (gem2.ownerid == p.charDBID) {
                        gem2 = gem2.clone(charid);
                        db.savePet1(gem2, p.pet.get(m).idDB + "_" + Database.nameServer[TeamServer.server]);
                    }
                }
            } catch (Exception ignored) {
            }
            db.saveCharQuest1(charid, p.getInfoQuestSave());
            db.saveCharFruit1(charid, p.getInfoSaveFruit());
            db.saveCharTubinh1(charid, p.getInfoSaveTubinh());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
        } catch (Exception ignored) {
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        ++TeamServer.update;
    }

    static InfoCharLienDau addChar1(Database db, Session s, String charname, int headStyle, int classID, int gender, int nation) {
        InfoCharLienDau info = new InfoCharLienDau();
        charname = charname + "_" + Database.nameServer[TeamServer.server];
        info.name = charname;
        int userID = s.userID;
        --gender;
        System.out.println("CHUAN BI DANG KY LIEN DAU " + charname);
        int charId = -1;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = db.getConnection1();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM tob_char WHERE charname='" + charname + "'";
            if (stmt.execute(sql)) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    charId = rs.getInt("id");
                    try {
                        stmt.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rs.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        Database.connPool1.free(conn);
                    } catch (Exception ignored) {
                    }
                    System.out.println("DA CO ROI");
                    info.data = new int[]{charId, charId};
                    return info;
                }
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sql = "SELECT * FROM tob_char WHERE userId=" + userID;
            final boolean first = false;
            try {
                ResultSet rs2 = stmt.executeQuery(sql);
                if (rs2.next()) {
                    charId = rs2.getInt("id");
                    try {
                        stmt.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        rs2.close();
                    } catch (Exception ignored) {
                    }
                    try {
                        Database.connPool1.free(conn);
                    } catch (Exception ignored) {
                    }
                    System.out.println("DA CO USERID ROi: " + charId);
                    info.data = new int[]{charId, charId};
                    return info;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            CharTemplate ct;
            if (gender != -1) {
                ct = Database.charTemplates[classID][gender];
            } else {
                ct = Database.charTemplatesOld[classID];
            }
            int[] tempPotion = new int[162];
            tempPotion[4] = (tempPotion[1] = 1);
            long xu = 0L;
            StringBuilder potion = new StringBuilder("0," + ct.nHP1 + ",0,0," + ct.nMP1 + ",0,0");
            if (first) {
                if (s.privateProvider.equals("1") && s.bigProvider.equals("1")) {
                    xu = 100000L;
                } else if (s.bigProvider.equals("0")) {
                    xu = 100000L;
                }
            }
            potion = new StringBuilder(String.valueOf(xu));
            for (int i = 1; i < tempPotion.length; ++i) {
                potion.append(",").append(tempPotion[i]);
            }
            final String stSkill = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
            int[] cSkill = {6, 6, 8, 6, 6};
            String[] data = Char.split(stSkill, ",");
            int[] mainSkill = new int[15];
            for (int j = 0; j < data.length; ++j) {
                mainSkill[j] = -1;
                try {
                    if (j < cSkill[classID]) {
                        mainSkill[j] = Byte.parseByte(data[j]);
                    }
                } catch (Exception ignored) {
                }
            }
            StringBuilder skill = new StringBuilder(String.valueOf(mainSkill[0]));
            ct.strength = Database.basicPoint[classID][0];
            ct.agitity = Database.basicPoint[classID][1];
            ct.spirit = Database.basicPoint[classID][2];
            ct.dextrity = Database.basicPoint[classID][3];
            ct.luck = Database.basicPoint[classID][4];
            String basic = ct.strength + "," + ct.agitity + "," + ct.spirit + "," + ct.dextrity + "," + ct.luck + ",0,0";
            for (int k = 7; k < 25; ++k) {
                potion.append(",0");
                if (k < 21) {
                    skill.append(",").append(mainSkill[k - 7]);
                }
            }
            potion.append(",0,0,0,0,0,0,0,0,0,0,0,0,0");
            int maxhp = 0;
            int maxmp = 0;
            switch (classID) {
                case 0: {
                    maxhp = ct.dextrity * 80;
                    maxmp = ct.spirit * 20;
                    break;
                }
                case 1:
                case 4:
                case 3: {
                    maxhp = ct.dextrity * 60;
                    maxmp = ct.spirit * 20;
                    break;
                }
                case 2: {
                    maxhp = ct.dextrity * 60;
                    maxmp = ct.spirit * 50;
                    break;
                }
            }
 
            ct.hp = maxhp;
            ct.mp = maxmp;
            int aoTemplate = (ct.gender == 1) ? 1 : 2;
            int quanTemplate = (ct.gender == 1) ? 27 : 28;
            int[] defaultVK = {79, 86, 93, 100, 107, 79, 86, 93, 100, 107};
            int vkhi = defaultVK[classID];
            String pInfo = ct.getDbInfo(headStyle, gender + 1, charname, classID, maxhp, maxmp, 105, nation);
            int idAo = Map.itemTemplates.get(classID).get(aoTemplate).id;
            int quanB = Map.itemTemplates.get(classID).get(quanTemplate).id;
            int vukhi = Map.itemTemplates.get(classID).get(vkhi).id;
            int vkTemplate = defaultVK[classID];
            Item ao = new Item(Map.itemTemplates.get(classID).get(aoTemplate), false, classID, classID, aoTemplate);
            Item quan = new Item(Map.itemTemplates.get(classID).get(quanTemplate), false, classID, classID, quanTemplate);
            Item vk = new Item(Map.itemTemplates.get(classID).get(vkTemplate), false, classID, classID, vkTemplate);
            ao.place = 1;
            quan.place = 1;
            vk.place = 1;
            String equip = ao.getInfoSave() + ">" + quan.getInfoSave() + ">" + vk.getInfoSave();
            String infoTop = nation + "," + idAo + "," + classID + ",1,0," + quanB + "," + classID + ",1,0," + vukhi + "," + classID + ",1,0";
            sql = "INSERT INTO tob_char(charname,userId,headStyle,class,pInfo,potion,skill,basic,gender,infotop,gold,equip,idserver) VALUES('" + charname + "'," + userID + "," + headStyle + "," + classID + ",'" + pInfo + "','" + potion + "','" + skill + "','" + basic + "'," + (gender + 1) + ",'" + infoTop + "'," + xu + ",'" + equip + "'," + s.idServer + ")";
            System.out.println(sql);
            System.out.println("THUC THI CAU LENH: " + stmt.execute(sql));
            sql = "SELECT * FROM tob_char WHERE charname='" + charname + "'";
            if (stmt.execute(sql)) {
                ResultSet rs3 = stmt.getResultSet();
                if (rs3.next()) {
                    charId = rs3.getInt("id");
                    System.out.println("CHAR DANG KY THANH CONG: " + charId);
                }
                try {
                    rs3.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            stmt.close();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        try {
            Database.connPool1.free(conn);
        } catch (Exception ignored) {
        }
        info.data = new int[]{charId, -1};
        return info;
    }

}
