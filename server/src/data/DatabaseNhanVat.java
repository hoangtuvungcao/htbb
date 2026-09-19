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

    /** Bon chuoi vat pham ghi xuong tob_char: equip / inven / bag / tuido. */
    static final class ChuoiVatPham {
        final String equip;
        final String inven;
        final String bag;
        final String tuido;

        ChuoiVatPham(final String equip, final String inven, final String bag, final String tuido) {
            this.equip = equip;
            this.inven = inven;
            this.bag = bag;
            this.tuido = tuido;
        }
    }

    /**
     * Ghep bon chuoi vat pham de ghi xuong CSDL. Dung cho ca saveChar va saveCharAuto.
     *
     * HAI DIEU KHAC BAN CU, ca hai deu de tranh MAT DO:
     *
     * 1. Duyet tren BAN CHUP (Vector.toArray la method synchronized nen ban chup la
     *    nguyen tu) chu khong duyet thang Vector. for-each tren Vector dung iterator
     *    fail-fast: neu luong game them mot mon vao iItems trong luc luong luu dang
     *    duyet thi nem ConcurrentModificationException NGAY GIUA vong lap. Chuyen do
     *    xay ra that: che tao them mon vao iItems roi goi saveCharAuto ngay tren luong
     *    doc socket (Map.doCreateNewItem), trong khi luong ban do co the dang autosave
     *    dung nhan vat do (CharVongLap -> ThreadPool).
     *
     * 2. Nem loi ra NGOAI thay vi nuot. Ban cu bao moi vong duyet trong
     *    "catch (Exception ignored) {}", nen khi nem giua vong lap thi StringBuilder
     *    chi con MOT PHAN danh sach - va cau UPDATE VAN CHAY voi chuoi cat cut do, xoa
     *    vinh vien moi mon nam sau diem dut, ke ca phan model o cuoi chuoi inven. Nay
     *    loi day len de ham luu HUY BO ca lan ghi: tha bo mot lan luu (lan sau luu lai)
     *    con hon ghi xuong mot hanh trang thieu.
     */
    private static ChuoiVatPham ghepChuoiVatPham(final Char p) {
        final StringBuilder equip = new StringBuilder();
        ghepDanhSach(equip, chup(p.getCharWearing(0)));
        ghepDanhSach(equip, chup(p.getCharWearing(1)));
        ghepDanhSach(equip, chup(p.awItems));

        final StringBuilder inven = new StringBuilder();
        if (p.itemHoaKyLan != null) {
            inven.append(p.itemHoaKyLan.getInfoSave()).append(">");
        }
        if (p.itemVukhiThoiTrang != null) {
            inven.append(p.itemVukhiThoiTrang.getInfoSave()).append(">");
        }
        if (p.itemThanKhi != null) {
            inven.append(p.itemThanKhi.getInfoSave()).append(">");
        }
        ghepDanhSach(inven, chup(p.iItems));

        final StringBuilder bag = new StringBuilder();
        ghepDanhSach(bag, chup(p.bItem));

        final StringBuilder tuido = new StringBuilder();
        ghepDanhSach(tuido, chup(p.bagItems));

        // Phan model deo tren nguoi nam o CUOI chuoi inven, sau dau ">" - giu dung
        // dinh dang cu de CharTrangBi.setitemInvenBag doc lai khong doi gi.
        String sInven = boDauPhanCachCuoi(inven);
        // wModel duoc tao trong CharKhoiTao nen thuc te khong bao gio null. Van kiem:
        // ban cu bao ca khoi trong "catch (Exception ignored)" nen mot NPE o day chi
        // lam mat phan model; nay loi se HUY CA LAN LUU, ma huy vi mot o khong bao gio
        // null thi qua dat - khong duoc phep chan duong luu vi mot gia dinh.
        final String infomodel = (p.wModel == null) ? "" : p.wModel.getInfoModelWearing();
        if (infomodel != null && !infomodel.equals("")) {
            sInven = sInven + ">" + infomodel;
        }
        return new ChuoiVatPham(boDauPhanCachCuoi(equip), sInven,
                boDauPhanCachCuoi(bag), boDauPhanCachCuoi(tuido));
    }

    /**
     * Ban chup nguyen tu cua mot Vector vat pham.
     *
     * null la KHONG hop le: bon vector nay deu duoc tao trong CharKhoiTao va khong cho
     * nao gan lai null. Neu van gap null thi nhan vat dang o trang thai hong - luc do
     * ghi mot danh sach RONG xuong CSDL la xoa sach o do, nen tha nem loi de ham luu
     * huy ca lan ghi (ban ghi cu con nguyen) va de lai dau vet trong loi-luu-nhan-vat.txt.
     */
    private static Item[] chup(final Vector<Item> v) {
        if (v == null) {
            throw new IllegalStateException("danh sach vat pham null - khong ghi de bang danh sach rong");
        }
        return v.toArray(new Item[0]);
    }

    private static void ghepDanhSach(final StringBuilder sb, final Item[] ds) {
        for (final Item it : ds) {
            if (it != null) {
                sb.append(it.getInfoSave()).append(">");
            }
        }
    }

    private static String boDauPhanCachCuoi(final StringBuilder sb) {
        final int n = sb.length();
        return (n == 0) ? "" : sb.substring(0, n - 1);
    }

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
        // Dung chung ham ghep chuoi voi saveChar/saveCharAuto: duyet tren ban chup nen
        // khong nem giua chung, va ghep loi thi bo ca lan ghi thay vi ghi mot chuoi
        // do dang. Xem chu thich o ghepChuoiVatPham().
        //
        // MOT KHAC BIET so voi ban cu: chuoi inven nay bay gio co ke ca ba o dac biet
        // (hoa ky lan, vu khi thoi trang, than khi) - dung dinh dang ma tob_char dang
        // dung, thay vi mot dinh dang rieng chi co iItems. Ham nay hien khong duoc goi
        // o dau ca, nen thay doi la vo hai; neu sau nay dung lai thi ban sao luu se
        // doc lai duoc bang dung mot duong voi ban chinh.
        final String equip;
        final String invent;
        final String bag;
        try {
            final ChuoiVatPham chuoi = ghepChuoiVatPham(p);
            equip = chuoi.equip;
            invent = chuoi.inven;
            bag = chuoi.bag;
        } catch (Exception exChuoi) {
            exChuoi.printStackTrace();
            try {
                Logger.logString("saveCharBackup " + p.charname + ": " + exChuoi, "loi-luu-nhan-vat.txt");
            } catch (Exception ignored) {
            }
            return;
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
        final String equip;
        final String invent;
        final String bag;
        final String tuido;
        try {
            final ChuoiVatPham chuoi = ghepChuoiVatPham(p);
            equip = chuoi.equip;
            invent = chuoi.inven;
            bag = chuoi.bag;
            tuido = chuoi.tuido;
        } catch (Exception exChuoi) {
            // Ghep chuoi that bai -> HUY CA LAN LUU. Ghi de mot chuoi do dang la xoa
            // vinh vien nhung mon nam sau diem dut. Xem chu thich o ghepChuoiVatPham().
            exChuoi.printStackTrace();
            try {
                Logger.logString("saveChar " + p.charname + ": " + exChuoi, "loi-luu-nhan-vat.txt");
            } catch (Exception ignored) {
            }
            try {
                db.saveOrtherLog("", p.charname, "huy luu vi ghep chuoi vat pham loi - " + exChuoi, "loisave");
            } catch (Exception ignored) {
            }
            return false;
        }
        Connection conn = null;
        PreparedStatement pre = null;
        String sql = "UPDATE tob_char SET pInfo=?,basic=?,skill=?,potion=?,totalTimePlay=totalTimePlay+?, luong=?, lastLv=?,xp=?,gold=?,hp=?, idClan=?, reupclass=?,headStyle=?,ticket=?,x=?,y=?,book=?,lastLog=Now(),infotop=?,equip=?,"
                + "inven=?,bag=?,killme=?,luonglock=?,tuido=?,isPP=?,isMN=?,isCT=?,isCanh=?,isSkinThu=? where id=?";
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
            // Cot isCT luu "DA TAT cai trang" (1 = tat), cung chieu voi isPP va isMN.
            // Chon chieu nay de hang cu — sau ALTER TABLE deu bang 0 — doc ra thanh "dang
            // bat", dung y nhu hanh vi tu truoc den gio. Luu chieu nguoc lai thi ngay khi
            // chay migration, TOAN BO nguoi choi bi tat cai trang.
            pre.setByte(27, (byte) (p.isBatCaiTrang() ? 0 : 1));
            pre.setByte(28, (byte) (p.isTatCanh() ? 1 : 0));
            // Cot isSkinThu cung chieu voi isPP/isMN/isCanh: 1 = DA TAT hien thi skin thu.
            pre.setByte(29, (byte) (p.isTatSkinLinhThu() ? 1 : 0));
            pre.setInt(30, p.charDBID);
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
            // Chi ghi hai bang phu nay khi du lieu THAT SU da co trong RAM (xem daNapCayAnQua
            // va chu thich o saveCharAuto): nhan vat duoi cap, hay Char chua nap xong, co
            // fruit toan null / tubinh rong - ghi xuong la de mot dong that thanh dong trang.
            if (daNapCayAnQua(p)) {
                db.saveCharFruit(p.charDBID, p.getInfoSaveFruit());
            }
            if (!p.tubinh.isEmpty()) {
                db.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());
            }
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
        // Han muc luong nhan tu chuyen khoan trong ngay (chi ghi khi co thay doi)
        try {
            if (p.nhanChuyenLuongDirty) {
                db.saveNhanChuyenLuong(p);
                p.nhanChuyenLuongDirty = false;
            }
        } catch (Exception ignored) {
        }
        ++TeamServer.update;
        return true;
    }

    /**
     * fruit[] chi duoc nap/tao luc dang nhap khi lv >= 34 (CharNapDuLieu: loadCharFruit +
     * createFruit); truoc do toan null va getInfoSaveFruit tra ve toan chuoi rong. Ghi chuoi
     * rong do xuong la xoa sach tien do cay cua mot dong that -> chi luu khi da nap.
     */
    static boolean daNapCayAnQua(Char p) {
        if (p.fruit == null) {
            return false;
        }
        for (int i = 0; i < p.fruit.length; ++i) {
            if (p.fruit[i] != null) {
                return true;
            }
        }
        return false;
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
        final String equip;
        final String invent;
        final String bag;
        final String tuido;
        try {
            final ChuoiVatPham chuoi = ghepChuoiVatPham(p);
            equip = chuoi.equip;
            invent = chuoi.inven;
            bag = chuoi.bag;
            tuido = chuoi.tuido;
        } catch (Exception exChuoi) {
            // Ghep chuoi that bai -> HUY CA LAN LUU. Xem chu thich o ghepChuoiVatPham().
            exChuoi.printStackTrace();
            try {
                Logger.logString("saveCharAuto " + p.charname + ": " + exChuoi, "loi-luu-nhan-vat.txt");
            } catch (Exception ignored) {
            }
            try {
                db.saveOrtherLog("", p.charname, "huy luu tu dong vi ghep chuoi vat pham loi - " + exChuoi, "loisave");
            } catch (Exception ignored) {
            }
            return false;
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
                // lo #5 loi.xlsx: truoc day chi in ra console roi bo qua - khong ai biet
                // du lieu (hanh trang/trang bi...) chua duoc luu, mat dau vet khi debug
                // sau nay. Ghi lai ben vung, cung bang tob_log_other da dung o cho khac.
                try {
                    db.saveOrtherLog("tob_log_other_item", p.charname, "loi luu saveCharAuto - " + e, "loisave");
                } catch (Exception ignored) {
                }
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
                    // Tu binh (Char.doAddManhTubinh ghep manh Mai/Lan/Cuc/Truc) truoc day CHI
                    // duoc luu boi saveChar (ban luu day du luc THOAT that su) - hoan toan vang
                    // mat o saveCharAuto, tuc ban tu luu dinh ky (5-11 phut/lan, xem
                    // CharManager.java) va rat nhieu noi khac goi ngay sau moi hanh dong quan
                    // trong. Manh gem thi DA duoc luu ngay tren (dong truoc), nen neu server
                    // crash/nguoi choi mat ket noi dot ngot GIUA luc ghep va luc dang xuat that
                    // su: manh bi tru THAT (da luu) nhung tien do ghep tranh thi mat (chua tung
                    // duoc luu) - dung nguyen nhan bao cao "dung tranh nhieu khi lai bi mat".
                    //
                    // CHI ghi khi tubinh da co trong RAM. tubinh chi duoc nap luc dang nhap khi
                    // lv >= 40 (CharNapDuLieu); nhan vat duoi 40, hay Char dang nap do (mid-
                    // login), co tubinh RONG -> getInfoSaveTubinh tra ve "" -> ghi xuong la de
                    // dong that thanh trang. Con "dong chua ton tai" (nhan vat len 40 ngay trong
                    // phien roi ghep manh) thi Database.saveCharTubinh da la upsert.
                    if (!p.tubinh.isEmpty()) {
                        db.saveCharTubinh(p.charDBID, p.getInfoSaveTubinh());
                    }
                    // Vat pham dac biet mua trong shop (listSpItem[], bang tob_sp_new) - CUNG
                    // MOT DANG loi voi tu binh o tren: truoc day chi saveChar goi, vang mat o
                    // day. Tien mua (luong) da duoc luu ngay trong cau UPDATE chinh cua ham nay
                    // (cot luong o tren), nen crash giua luc mua va luc dang xuat that su la
                    // mat dung mon da mua du tien da tru that. listSpItem nap ngay trong getChar
                    // (o tren, cung ham nay) va saveCharSP tu chen dong neu thieu -> khong can
                    // dieu kien gi them.
                    StringBuilder soluong3 = new StringBuilder(String.valueOf(p.listSpItem[0]));
                    for (int n = 1; n < p.listSpItem.length; ++n) {
                        soluong3.append(",").append(p.listSpItem[n]);
                    }
                    db.saveCharSP(soluong3.toString(), p.charDBID);
                    // Cay an qua (tien do trong/tuoi/thu hoach, bang tob_char_fruit) - CUNG mot
                    // dang loi, vang mat o day truoc khi sua. Chi ghi khi fruit[] da nap (lv >=
                    // 34 luc dang nhap), xem daNapCayAnQua.
                    if (daNapCayAnQua(p)) {
                        db.saveCharFruit(p.charDBID, p.getInfoSaveFruit());
                    }
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
        // Bat Exception chu KHONG chi SQLException. Cho xin ket noi
        // (ConnPool.getConFromPool) nem java.util.NoSuchElementException - mot
        // RuntimeException - khi pool can; SQLException khong bat duoc no. Truoc day no
        // xuyen qua day roi bi nuot o ben goi (Map.doCreateNewItem boc ca than trong
        // catch (Exception)), nen mot lan luu that bai KHONG de lai bat ky dau vet nao:
        // khong dong log, khong file, va ham van coi nhu da luu. Do dung la thu lam cho
        // chuyen "mat do" khong the chan doan duoc.
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(" LOI SAVE CHAR " + p.charname + " | " + p.lvDetail.getExp());
            String logError = "UPDATE tob_char SET pInfo=" + dbInfo + ",basic=" + basic + ",skill=" + skill + ",potion=" + potion + ",totalTimePlay=totalTimePlay+" + totalPlay + ", luong=" + p.getLuong() + ", lastLv=" + p.lastLV + ",xp=" + p.lvDetail.getExp() + ",gold=" + p.getxu() + ",hp=" + p.hp + ",idClan=" + p.idClan + ",headStyle=" + p.headStyle + " where id=" + p.charDBID;
            Logger.logString(logError, "savechar.txt");
            try {
                db.saveOrtherLog("", p.charname, "luu tu dong that bai - " + ex, "loisave");
            } catch (Exception ignored) {
            }
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
        // Han muc luong nhan tu chuyen khoan trong ngay (chi ghi khi co thay doi)
        try {
            if (p.nhanChuyenLuongDirty) {
                db.saveNhanChuyenLuong(p);
                p.nhanChuyenLuongDirty = false;
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
        PreparedStatement pre = null;
        try {
            conn = db.getConnection();
            String sql = "SELECT * FROM tob_char WHERE charname=?";
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            if (pre.execute()) {
                ResultSet rs = pre.getResultSet();
                boolean trungTen = rs.next();
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (trungTen) {
                    // Khong tu dong dong pre/conn o day, khoi finally ben duoi lo viec do.
                    // Neu dong 2 lan, cung 1 connection se bi tra ve pool 2 lan.
                    return -1;
                }
            }
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            // Chi dem nhan vat con song (del=1), khong bi ban va thuoc dung server dang choi,
            // dung mot server khac cua cung tai khoan lam het slot.
            sql = "SELECT COUNT(*) FROM tob_char WHERE userId=? AND idserver=? AND ban=0 AND del=1";
            int countChar = 0;
            try {
                pre = conn.prepareStatement(sql);
                pre.setInt(1, userID);
                pre.setInt(2, s.idServer);
                ResultSet rs2 = pre.executeQuery();
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
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                pre = conn.prepareStatement("SELECT COUNT(*) FROM tob_char WHERE userId=?");
                pre.setInt(1, userID);
                ResultSet rs3 = pre.executeQuery();
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
            sql = "INSERT INTO tob_char(charname,userId,headStyle,class,pInfo,potion,skill,basic,gender,infotop,gold,equip,idserver) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            pre.setInt(2, userID);
            pre.setInt(3, headStyle);
            pre.setInt(4, classID);
            pre.setString(5, pInfo);
            pre.setString(6, potion.toString());
            pre.setString(7, skill.toString());
            pre.setString(8, basic);
            pre.setInt(9, gender + 1);
            pre.setString(10, infoTop);
            pre.setLong(11, xu);
            pre.setString(12, equip);
            pre.setInt(13, s.idServer);
            pre.execute();
            // Nhan vat dau tien va duy nhat cua tai khoan: tang 40.000.000 xu + 20.000 luong mo
            // + 30.000 luong khoa, cong qua hang doi board_created (nhu Nhan moc nap), Char se
            // nhan khi vao game lan dau qua Database.getBoardCreated(), khong dung sua truc tiep
            // pInfo (fragile, xem [[feedback-kpah-safe-npc-edits]]). So doi 12/09/2026 theo yeu
            // cau nguoi dung (thay the hoan toan bo cu 10tr/5k/100k). Trung Cu/Rong/Yeu Tinh/
            // Doi/Dai bang x3 va Huyet linh thao x4 (khong the nhet vao bang nay - chi co cot
            // so, khong co vat pham) duoc cong THEM ngay trong chinh getBoardCreated() ben duoi,
            // cung mot lan nhan, dung luc con lai Char song de co the them vao tui.
            if (first) {
                try {
                    pre.close();
                } catch (Exception ignored) {
                }
                pre = conn.prepareStatement("INSERT INTO board_created(xu,luong,luongK,username) VALUES(40000000,20000,30000,?)");
                pre.setString(1, String.valueOf(userID));
                pre.execute();
            }
            sql = "SELECT * FROM tob_char WHERE charname=?";
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            if (pre.execute()) {
                ResultSet rs3 = pre.getResultSet();
                if (rs3.next()) {
                    charId = rs3.getInt("id");
                }
                try {
                    rs3.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                // Truoc day noi chuoi truc tiep (charname/pInfo tu client) vao SQL roi
                // dua cho addBackupChar chay bang Statement thuong - SQL injection neu
                // charname/pInfo tung lot qua duoc dau nhay don o buoc tao nhan vat (lop
                // loc duy nhat nam trong CreateCharHandler, khong co source o day). Dung
                // PreparedStatement thay vi tin tuong hoan toan vao lop loc do.
                try (PreparedStatement preCopy = conn.prepareStatement(
                        "INSERT INTO tob_char2(id,charname,userId,headStyle,class,pInfo,potion,skill,basic,gender) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
                    preCopy.setInt(1, charId);
                    preCopy.setString(2, charname);
                    preCopy.setInt(3, userID);
                    preCopy.setInt(4, headStyle);
                    preCopy.setInt(5, classID);
                    preCopy.setString(6, pInfo);
                    preCopy.setString(7, potion.toString());
                    preCopy.setString(8, skill.toString());
                    preCopy.setString(9, basic);
                    preCopy.setInt(10, gender + 1);
                    preCopy.execute();
                } catch (Exception ignored) {
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                pre.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                Database.connPool.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
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
        final String equip;
        final String invent;
        final String bag;
        try {
            final ChuoiVatPham chuoi = ghepChuoiVatPham(p);
            equip = chuoi.equip;
            invent = chuoi.inven;
            bag = chuoi.bag;
        } catch (Exception exChuoi) {
            // Ghep chuoi that bai -> HUY CA LAN LUU, khong ghi de mot chuoi do dang.
            // Xem chu thich o ghepChuoiVatPham().
            exChuoi.printStackTrace();
            try {
                Logger.logString("saveCharLienDau " + p.charname + ": " + exChuoi, "loi-luu-nhan-vat.txt");
            } catch (Exception ignored) {
            }
            return;
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
        PreparedStatement pre = null;
        try {
            conn = db.getConnection1();
            String sql = "SELECT * FROM tob_char WHERE charname=?";
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            if (pre.execute()) {
                ResultSet rs = pre.getResultSet();
                if (rs.next()) {
                    charId = rs.getInt("id");
                    try {
                        pre.close();
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
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            sql = "SELECT * FROM tob_char WHERE userId=?";
            final boolean first = false;
            try {
                pre = conn.prepareStatement(sql);
                pre.setInt(1, userID);
                ResultSet rs2 = pre.executeQuery();
                if (rs2.next()) {
                    charId = rs2.getInt("id");
                    try {
                        pre.close();
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
            sql = "INSERT INTO tob_char(charname,userId,headStyle,class,pInfo,potion,skill,basic,gender,infotop,gold,equip,idserver) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            pre.setInt(2, userID);
            pre.setInt(3, headStyle);
            pre.setInt(4, classID);
            pre.setString(5, pInfo);
            pre.setString(6, potion.toString());
            pre.setString(7, skill.toString());
            pre.setString(8, basic);
            pre.setInt(9, gender + 1);
            pre.setString(10, infoTop);
            pre.setLong(11, xu);
            pre.setString(12, equip);
            pre.setInt(13, s.idServer);
            System.out.println(sql);
            System.out.println("THUC THI CAU LENH: " + pre.execute());
            sql = "SELECT * FROM tob_char WHERE charname=?";
            try {
                pre.close();
            } catch (Exception ignored) {
            }
            pre = conn.prepareStatement(sql);
            pre.setString(1, charname);
            if (pre.execute()) {
                ResultSet rs3 = pre.getResultSet();
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
                pre.close();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            try {
                Database.connPool1.free(conn);
            } catch (Exception ignored) {
            }
        }
        try {
            pre.close();
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
