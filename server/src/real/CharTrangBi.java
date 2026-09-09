package real;


import server.TeamServer;
import data.Database;
import io.Message;

import data.ItemLuckyDraw;
import data.ModelWearing;
import java.util.Vector;

/**
 * Than cac method quan ly trang bi va hanh trang cua Char: mac/thao trang bi,
 * kiem tra hieu ung bo do, sap xep tui do.
 *
 * Tach tu real/Char.java. Cac method VAN CON trong Char, giu NGUYEN chu ky
 * cu, chi than chuyen sang day. Tham so "Char self" thay cho "this".
 */
class CharTrangBi {

    static void checkNewEffectItem(Char self, final int cat, long damage, final LiveActor attacker) {
        final int[] sl = {8, 15, 8, 8};
        final int pc = Map.r.nextInt(1000) + 1;
        int id = 0;
        final int setlan = self.setlan * self.getXSetLan(cat) + self.getUpPCBangSetDocLuaLan(0) + self.setlan * self.getPCXbangSetDocLuaLan() / 100;
        final int banglan = self.banglan * self.getXBangLan(cat) + self.getUpPCBangSetDocLuaLan(2) + self.banglan * self.getPCXbangSetDocLuaLan() / 100;
        final int doclan = self.doclan * self.getXDocLan(cat) + self.getUpPCBangSetDocLuaLan(1) + self.doclan * self.getPCXbangSetDocLuaLan() / 100;
        final int lualan = self.lualan * self.getXLuaLan(cat) + self.getUpPCBangSetDocLuaLan(3) + self.lualan * self.getPCXbangSetDocLuaLan() / 100;
        final int[] getindex = {banglan, setlan, doclan, lualan};
        id = Char.getIndexRandom(getindex);
        if (id == -1) {
            return;
        }
        if (pc > getindex[id]) {
            return;
        }

        // tính toán giảm kháng từ pet cưỡi
        int giamKhangLan = self.animalRide == null ? 0 : self.animalRide.getGiamKhangLan(id);

        final String info = String.valueOf(Char.namelan[id]) + ":";
        String infokhang = "";
        final Vector<LiveActor> allnewar = self.map.getAllNearActor(self, cat, sl[id], attacker);
        final Vector<Integer> dam = new Vector<Integer>();
        final Vector<Integer> hpLeft = new Vector<Integer>();
        final Vector<LiveActor> allactordie = new Vector<LiveActor>();
        int totalxp = 0;
        for (int i = 0; i < allnewar.size(); ++i) 
        {
            if (i == 10 ) break; // Chỉ tính 10 mục tiêu
            final LiveActor actor = allnewar.get(i);
            if (!actor.isRuongMaquai() && !actor.isNgocRong() && !actor.isBossSonTinh() && !actor.isBossThuyTinh() ) 
            {
                if (!actor.isBossSonTinhThuyTinh()) 
                {
                    if (!actor.isActiveTienKhi() || !actor.isActive8sRongLon()) 
                    {

                        if (chenhLechCapQuaXaLan(self, actor) || actor.isKhangLan(id, giamKhangLan))
                        {
                            if (actor.cat == 0 && ((Char) actor).timeGiveCardTown > 0L) 
                            {
                                infokhang = String.valueOf(actor.getName()) + " KHANG DC " + Char.namelan[id] + ",bl=" + self.khangbanglan + ",dl=" + self.khangsetlan + ",dl=" + self.khangdoclan + ",ll=" + self.khanglualan;
                                Database.instance.saveOrtherLog("", actor.getName(), infokhang, "khanglan");
                            }
                            dam.add(0);
                            hpLeft.add(actor.hp);
                        } 
                        else if (id == 2)
                        {
                            int d = actor.hp / 2;
                            if (actor.isBoss() || actor.isCongThanh())
                            {
                                d = actor.hp / 100;
                            }
                            if (d <= 0)
                            {
                                d = 1;
                            }
                            d += d * self.getUpDamBangSetDocLuaLan(1) / 100;
                            dam.add(d);
                            if (cat == 1)
                            {
                                totalxp += actor.getXpReceive(d);
                                final Monster ms = (Monster) actor;
                                if (ms.target == null) {
                                    ms.target = self;
                                }
                            }
                            if (!self.isBatTu())
                            {
                                final LiveActor liveActor = actor;
                                liveActor.hp -= d;
                            }
                            hpLeft.add(actor.hp);
                            if (actor.hp <= 0) 
                            {
                                allactordie.add(actor);
                            } 
                            else 
                            {
                                try 
                                {
                                    actor.timeOutPoinson = System.currentTimeMillis();
                                    if (damage > 32768L) 
                                    {
                                        damage = 32760L;
                                    }
                                    damage += damage * self.getUpDamBangSetDocLuaLan(1) / 100L;
                                    if (damage <= 0L) 
                                    {
                                        damage = 1L;
                                    }
                                    if (damage > 32768L) 
                                    {
                                        damage = 32760L;
                                    }
                                    if (actor.cat == 0 && actor.getAnimalRide() != null && actor.getAnimalRide().isNguaXuong() && Map.r.nextInt(100) < 95) 
                                    {
                                        actor.hpDocLan = (short) (damage / 2L);
                                    } 
                                    else 
                                    {
                                        actor.poinson = (short) damage;
                                    }
                                    actor.tDelay = 1;
                                    actor.totalTime = 5;
                                    final Message m = new Message(89);
                                    m.dos.writeShort(actor.id);
                                    m.dos.writeByte(actor.cat);
                                    m.dos.writeByte(1);
                                    m.dos.writeShort((short) damage);
                                    m.dos.writeByte(4);
                                    m.dos.writeByte(4);
                                    m.dos.writeByte(10);
                                    self.sendMessage(m);
                                    self.sendToNearPlayer(m);
                                } catch (final Exception ex) {
                                }
                                if (cat == 1) {
                                    final Monster ms = (Monster) actor;
                                    if (ms.target == null) {
                                        ms.target = self;
                                    }
                                }
                            }
                        } 
                        else if (id == 0) 
                        {
                            int d = actor.hp / 2;
                            if (actor.isBoss() || actor.isCongThanh()) 
                            {
                                d = actor.hp / 100;
                            }
                            if (d <= 0) {
                                d = 1;
                            }
                            d += d * self.getUpDamBangSetDocLuaLan(2) / 100;
                            dam.add(d);
                            if (cat == 1) 
                            {
                                totalxp += actor.getXpReceive(d);
                                final Monster ms = (Monster) actor;
                                if (ms.target == null) 
                                {
                                    ms.target = self;
                                }
                            }
                            if (!self.isBatTu()) 
                            {
                                final LiveActor liveActor2 = actor;
                                liveActor2.hp -= d;
                            }
                            hpLeft.add(actor.hp);
                            if (actor.hp <= 0) 
                            {
                                allactordie.add(actor);
                            } 
                            else {
                                int khangdongbang = 0;
                                if (cat == 0) {
                                    final Char c = (Char) actor;
                                    if (c.animalRide != null && c.animalRide.KHANG_DONG_BANG > 0) {
                                        khangdongbang = c.animalRide.KHANG_DONG_BANG
                                                - (self.animalRide == null ? 0 : self.animalRide.getGiamKhangDongBang());
                                    }
                                }
                                if (Map.r.nextInt(100) >= khangdongbang) {
                                    actor.khamEff[3] = System.currentTimeMillis() / 1000L;
                                    self.sendMessage(self.sendEffKham(actor));
                                    self.sendToNearPlayer(self.sendEffKham(actor));
                                }
                            }
                        } else if (id == 1) {
                            int d = actor.hp / 2;
                            if (actor.isBoss() || actor.isCongThanh()) {
                                d = actor.hp / 100;
                            }
                            d += d * self.getUpDamBangSetDocLuaLan(0) / 100;
                            if (d <= 0) {
                                d = 1;
                            }
                            dam.add(d);
                            if (cat == 1) {
                                totalxp += actor.getXpReceive(d);
                                final Monster ms = (Monster) actor;
                                if (ms.target == null) {
                                    ms.target = self;
                                }
                            }
                            if (!self.isBatTu()) {
                                final LiveActor liveActor3 = actor;
                                liveActor3.hp -= d;
                            }
                            hpLeft.add(actor.hp);
                            if (actor.hp <= 0) {
                                allactordie.add(actor);
                            } else if (actor.cat == 0) {
                                // Ca Duong khang (heo) lan Duong khang Bang Gia (heo bang) deu ghi
                                // "Khang choang lan 95%" tren bang thong tin, truoc day chi heo
                                // thuong duoc tinh.
                                if (actor.getAnimalRide() != null
                                        && (actor.getAnimalRide().isHeo() || actor.getAnimalRide().isHeoBang())
                                        && Map.r.nextInt(100) < 95) {
                                    if (self.addEffBuff(EffectBuff.CHOANG, System.currentTimeMillis() + 3000L, EffectBuff.BY_ACTOR, 0) != null) {
                                        self.sendEffToChar(self);
                                        self.sendEffToNearChar();
                                    }
                                } else if (actor.addEffBuff(EffectBuff.CHOANG, System.currentTimeMillis() + 3000L, EffectBuff.BY_ACTOR, 0) != null && actor.cat == 0) {
                                    actor.sendEffToChar((Char) actor);
                                    actor.sendEffToNearChar();
                                }
                            }
                        } else if (id == 3) {
                            int d = actor.hp / 2;
                            if (actor.isBoss() || actor.isCongThanh()) {
                                d = actor.hp / 100;
                            }
                            if (d <= 0) {
                                d = 1;
                            }
                            d += d * self.getUpDamBangSetDocLuaLan(3) / 100;
                            dam.add(d);
                            if (cat == 1) {
                                totalxp += actor.getXpReceive(d);
                                final Monster ms = (Monster) actor;
                                if (ms.target == null) {
                                    ms.target = self;
                                }
                            }
                            if (!self.isBatTu()) {
                                final LiveActor liveActor4 = actor;
                                liveActor4.hp -= d;
                            }
                            hpLeft.add(actor.hp);
                            if (actor.hp <= 0) {
                                allactordie.add(actor);
                            } else {
                                try {
                                    boolean issend = false;
                                    if (cat == 0) {
                                        final Char c = (Char) actor;
                                        // Lay dung con so cua tung thu thay vi ap cung 95 va chi cho
                                        // hai loai: bang thong tin ghi phuong hoang bang / rong bang
                                        // 95% con su tu bang 50%, ma su tu bang truoc day khong lot
                                        // vao dieu kien nen 50% chi la chu.
                                        int khangluachay = 0;
                                        if (c.animalRide != null && c.animalRide.KHANG_BI_CHAY > 0) {
                                            khangluachay = c.animalRide.KHANG_BI_CHAY;
                                        }
                                        if (Map.r.nextInt(100) > khangluachay) {
                                            if (c.addEffBuff(EffectBuff.TAN_PHE, System.currentTimeMillis() + 3000L, EffectBuff.BY_ACTOR, 0) != null) {
                                                c.sendEffToChar(c);
                                                c.divSpeed = 2;
                                                issend = true;
                                                c.sendMessage(MessageCreator.createMainCharInfoMessage(c));
                                            }
                                            if (c.addEffBuff(EffectBuff.LAM_THINH, System.currentTimeMillis() + 3000L, EffectBuff.BY_ACTOR, 0) != null) {
                                                c.sendEffToChar(c);
                                                issend = true;
                                            }
                                            if (c.addEffBuff(EffectBuff.FIRE_BURN, System.currentTimeMillis() + 5000L, EffectBuff.BY_ACTOR, 0) != null) {
                                                c.sendEffToChar(c);
                                                issend = true;
                                            }
                                        }
                                        if (issend) {
                                            c.sendEffToNearChar();
                                        }
                                    }
                                } catch (final Exception ex2) {
                                }
                            }
                        }
                    }
                }
            }
        }
        // Sat thuong lan (lua/set/bang/doc) van gay sat thuong + giet quai binh thuong,
        // nhung khong cong exp cho nguoi choi/party (truoc day co cong qua "Exp Up lan").

        final Message k = MessageCreator.createMsgNewEffect(self, allnewar, cat, id, dam, hpLeft, 2000);
        self.sendMessage(k);
        self.sendToNearPlayer(k);
        for (int l = 0; l < allactordie.size(); ++l) {
            if (cat == 1) {
                allactordie.get(l).charKillBoss(self);
                Map.onMosterDie(self, allactordie.get(l), (byte) 0, 1, (byte) 0, (byte) 0);
            } else {
                allactordie.get(l).actorDie();
            }
        }
        for (int l = 0; l < allnewar.size(); ++l) {
            if (cat == 0) {
                try {
                    Char p = (Char) allnewar.get(l);
                    p.sendMessage(MessageCreator.createNew_HMP_Message(p, 0));
                    p.sendToNearPlayer(MessageCreator.createNew_HMP_Message(p, 0));
                } catch (final Exception ex4) {
                }
            }
        }
    }

    /** Chenh lech cap toi da de con dinh hieu ung "lan" - ap dung CA nguoi lan quai. */
    private static final int MUC_CHENH_LECH_CAP_LAN = 10;

    /**
     * True neu chenh lech cap giua nguoi gay lan (self) va muc tieu (actor) vuot qua
     * MUC_CHENH_LECH_CAP_LAN - luc do coi nhu muc tieu khong dinh, GIONG HET nhanh "khang
     * duoc" (dam=0, khong doi hp), du actor la nguoi choi (cat=0) hay quai (cat=1). Thay
     * cho khoi kiem tra da bi comment truoc day (sai: chi ap dung khi khong ai la boss, va
     * nguong lech khac nhau giua nhanh nguoi/quai du cung ghi 10 trong ten bien).
     */
    private static boolean chenhLechCapQuaXaLan(final Char self, final LiveActor actor) {
        final int capActor;
        if (actor.cat == 0) {
            capActor = ((Char) actor).lvDetail.lv;
        } else if (actor.cat == 1) {
            capActor = ((Monster) actor).level;
        } else {
            return false;
        }
        return Math.abs(self.lvDetail.lv - capActor) > MUC_CHENH_LECH_CAP_LAN;
    }

    static void setitemInvenBag(Char self, final int place) {
        String info = (place == 0) ? self.invent : self.bag;
        if (info == null) {
            return;
        }
        try {
            if (info.startsWith(">")) {
                info = info.substring(1, info.length());
            }
        } catch (final Exception ex) {
        }
        if (info.equals("")) {
            return;
        }
        final String[] allitem = Char.split(info, ">");
        int size = allitem.length;
        if (size > Char.MAX_ENTRY_INVEN_LOAD) {
            size = Char.MAX_ENTRY_INVEN_LOAD;
        }
        for (int i = 0; i < size; ++i) {
            try {
                final String[] dtitem = Char.split(allitem[i], "|");
                final Item item = new Item();
                item.dbInfo = dtitem[0];
                item.initInfoFromDB();
                item.owner = self.charDBID;
                item.setTemplate(item.tempateID, item.clazz, item.clazz, item.tempateID);
                if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                    try {
                        self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng " + item.getTemplate().name + " đã hết"));
                        Database.instance.deleteItem(item.dbid);
                        Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                    } catch (final Exception ex2) {
                    }
                } else if (Map.isPhiPhong(item.getTemplate().type) && item.minuteExist == 0 && item.dateCreate.equals("2016-09-15")) {
                    Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "ffbug");
                    data.NghiVanGianLan.ghiVatPham(self, "ffbug",
                            "phi phong loi cu ngay 2016-09-15 van con trong tui: "
                          + item.getTemplate().name + " (dbid " + item.dbid + ")");
                } else if (item.isPhiPhongWordCupHetHan() && item.dateCreate.compareTo("2018-08-22") <= 0) {
                    Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "ffbug");
                    data.NghiVanGianLan.ghiVatPham(self, "ffbug",
                            "phi phong World Cup het han van con trong tui: "
                          + item.getTemplate().name + " (dbid " + item.dbid + ")");
                } else if (item.isGayGiangSinh() && item.dateCreate.compareTo("2020-07-24 11:00:00") < 0) {
                    Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "thugay");
                } else {
                    if (item.isChoiSell() && self.lastLog.compareTo("2019-11-14 10:35:00") < 0) {
                        item.doResetTimeChoiDuaTop();
                    }
                    if (item.isChoi() && self.lastLog.compareTo("2020-05-08 14:35:00") < 0) {
                        Database.instance.saveOrtherLog("", self.charname, String.valueOf(item.getName()) + "_" + item.getInfoSave(), "logchoi");
                    }
                    if (item.isChoiSell() && self.lastLog.compareTo("2019-11-29 10:50:00") < 0 && !self.charname.equals(Char.chartopchoitrungthu) && item.dogetbackChoiDuaTop()) {
                        Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "tichthuchoi");
                    } else if (!item.isHoVeLenh() || self.lastLog.compareTo("2020-01-17 16:30:00") >= 0) {
                        if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2019-02-26 11:30:00") < 0 && item.dateCreate.compareTo("2019-02-26 11:00:00") >= 0) {
                            item.lock = 0;
                        }
                        if (!Map.openLog && TeamServer.server == 5 && self.lastLog.compareTo("2018-03-12 11:25:00") < 0 && Map.isItemThanThu(item.getTemplate().type)) {
                            Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "hklbug");
                        } else {
                            final String[] data = Char.split(dtitem[1], ",");
                            String countnguhanh = "";
                            for (int j = 0; j < data.length; ++j) {
                                try {
                                    if (j < 33) {
                                        item.atb[j] = Short.parseShort(data[j].trim());
                                        if (j >= 28 && item.atb[j] > 0 && j != item.idNguHanh2) {
                                            countnguhanh = String.valueOf(countnguhanh) + j + ",";
                                        }
                                    } else if (j < 43) {
                                        item.newAtb[j - 33] = Byte.parseByte(data[j].trim());
                                    } else if (j < 58) {
                                        item.addMoreLevelSkill[j - 43] = Byte.parseByte(data[j].trim());
                                    } else if (j < 61) {
                                        item.lockAtb[j - 58] = Byte.parseByte(data[j].trim());
                                    } else {
                                        item.otherAtt[j - 61] = Short.parseShort(data[j].trim());
                                    }
                                } catch (final Exception ex3) {
                                }
                            }
                            if (Map.isPhiPhong(item.getTemplate().type) && !Map.openLog && item.magic_physic == 0 && item.atb[1] > item.atb[6]) {
                                final short d = item.atb[1];
                                item.atb[1] = item.atb[6];
                                item.atb[6] = d;
                            }
                            if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2020-02-20 13:45:00") < 0 && item.dateCreate.compareTo("2020-02-20 11:40:00") >= 0) {
                                item.removeOptionHoaNguoiTuyetChoi();
                            }
                            if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2019-02-27 15:45:00") < 0 && item.dateCreate.compareTo("2019-02-26 11:00:00") >= 0) {
                                item.setOptionHoaNguoiTuyetChoi();
                            }
                            if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2019-11-11 11:45:00") < 0 && item.dateCreate.compareTo("2019-10-03 11:00:00") >= 0) {
                                item.setOptionHoaNguoiTuyetChoi();
                            }
                            if (item.istrungPet() && self.lastLog.compareTo("2019-09-19 15:52:00") < 0) {
                                Database.instance.saveOrtherLog("", self.charname, item.getInfoSave(), "");
                            }
                            if (!countnguhanh.equals("")) {
                                try {
                                    final String[] dtnh = Char.split(countnguhanh.substring(0, countnguhanh.length() - 1), ",");
                                    if (dtnh.length > 2) {
                                        final byte idNguhanh = Byte.parseByte(dtnh[Map.r.nextInt(dtnh.length)]);
                                        Database.instance.saveOrtherLog("", self.charname, String.valueOf(item.getName()) + " bi huy dong " + InfoItemAttribute.getNameAtt(idNguhanh) + " chi so " + item.atb[idNguhanh], "huynguhanh");
                                        item.atb[idNguhanh] = 0;
                                    }
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                if (item.resetItem != 5000 || Map.isWearingAnimal(item.getTemplate().type)) {
                                    if (Map.isWearingAnimal(item.getTemplate().type)) {
                                        if (!item.isHaveAttLock() && item.colorName == 1) {
                                            switch (item.hangItem) {
                                                case 1: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 0, 0, 0, 1});
                                                    break;
                                                }
                                                case 2: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 0, 0, 1, 0});
                                                    break;
                                                }
                                                case 3: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 0, 1, 0, 0});
                                                    break;
                                                }
                                                case 4: {
                                                    item.lockItemAnimal(new byte[]{0, 0, 1, 0, 0, 0});
                                                    break;
                                                }
                                                case 5: {
                                                    final Item item2 = item;
                                                    final byte[] material = new byte[6];
                                                    material[1] = 1;
                                                    item2.lockItemAnimal(material);
                                                    break;
                                                }
                                            }
                                        }
                                        item.reFixItemAnimal(self.charname);
                                    }
                                    item.resetItem = 5000;
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                            if (self.charname.indexOf("@") > -1 && ((item.dateCreate.compareTo("2016-08-29") < 0 && TeamServer.server == 3) || (item.dateCreate.compareTo("2016-09-01") < 0 && TeamServer.server == 2)) && item.charSeal.toLowerCase().equals(self.charname.substring(0, self.charname.length() - 3))) {
                                item.charSeal = self.charname;
                            }
                            if (self.charname.indexOf("@dn") > -1 && !Map.openLog && self.lastLog.compareTo("2017-04-26 10:15:00") < 0 && TeamServer.server == 1 && item.charSeal.toLowerCase().indexOf(self.charname) > -1) {
                                item.charSeal = self.charname;
                            }
                            if (TeamServer.isServerLienDau() && self.charname.indexOf("_") > -1 && !item.charSeal.equals(self.charname)) {
                                item.charSeal = self.charname;
                            }
                            if (ItemLuckyDraw.isChoi(item.getTemplate().id)) {
                                item.atb[9] = 2;
                            }
                            item.id = self.getIDItem();
                            if (place == 0) {
                                if (item.place == 4) {
                                    if (self.wModel != null) {
                                        item.place = 0;
                                        self.iItems.add(item);
                                    } else {
                                        (self.wModel = new ModelWearing()).setItemModel(self, item, item.getTemplate().atb[4]);
                                    }
                                } else {
                                    item.place = 0;
                                    self.iItems.add(item);
                                }
                            } else {
                                self.bItem.add(item);
                            }
                        }
                    }
                }
            } catch (final Exception e2) {
                e2.printStackTrace();
            }
        }

    }

    static void setItemWearing(Char self) {
        if (self.equip == null || self.equip.equals("")) {
            final int aoTemplate = (self.gender == 1) ? 2 : 1;
            final int quanTemplate = (self.gender == 1) ? 28 : 27;
            final int[] defaultVK = {79, 86, 93, 100, 107, 79, 86, 93, 100, 107};
            final int vkTemplate = defaultVK[self.charClass];
            final Item ao = new Item((ItemTemplates) Map.itemTemplates.get(self.charClass).get(new Integer(aoTemplate)), false, self.charClass, self.charClass, aoTemplate);
            final Item quan = new Item((ItemTemplates) Map.itemTemplates.get(self.charClass).get(new Integer(quanTemplate)), false, self.charClass, self.charClass, quanTemplate);
            final Item vk = new Item((ItemTemplates) Map.itemTemplates.get(self.charClass).get(vkTemplate), false, self.charClass, self.charClass, vkTemplate);
            ao.place = 1;
            quan.place = 1;
            vk.place = 1;
            ao.id = self.getIDItem();
            quan.id = self.getIDItem();
            vk.id = self.getIDItem();
            int index = Char.getIndexItemWearing(ao.getTemplate().type, 0);
            self.wItems[0][index] = ao;
            index = Char.getIndexItemWearing(quan.getTemplate().type, 0);
            self.wItems[0][index] = quan;
            index = Char.getIndexItemWearing(vk.getTemplate().type, 0);
            self.wItems[0][index] = vk;
            return;
        }
        int posNhan = -1;
        if (!self.equip.equals("")) {
            final String[] allitem = Char.split(self.equip, ">");
            for (int i = 0; i < allitem.length; ++i) {
                try {
                    final String[] dtitem = Char.split(allitem[i], "|");
                    final Item item = new Item();
                    item.dbInfo = dtitem[0];
                    item.initInfoFromDB();
                    item.setTemplate(item.tempateID, item.clazz, item.clazz, item.tempateID);
                    if (item.getMinuteLoan(false) <= 0 && item.minuteExist > 0) {
                        try {
                            self.sendMessage(MessageCreator.createServerAlertAutoOffMessage("Thời gian sử dụng " + item.getTemplate().name + " đã hết"));
                            Database.instance.deleteItem(item.dbid);
                            Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "timeout");
                        } catch (final Exception ex) {
                        }
                    } else if (item.isPhiPhongWordCupHetHan() && item.dateCreate.compareTo("2018-08-22") <= 0) {
                        Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "ffbug");
                        data.NghiVanGianLan.ghiVatPham(self, "ffbug",
                                "phi phong World Cup het han van con trong ruong: "
                              + item.getTemplate().name + " (dbid " + item.dbid + ")");
                    } else if (item.isGayGiangSinh() && item.dateCreate.compareTo("2020-07-24 11:00:00") < 0) {
                        Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "thugay");
                    } else {
                        if (item.isChoiSell() && self.lastLog.compareTo("2019-11-14 10:35:00") < 0) {
                            item.doResetTimeChoiDuaTop();
                        }
                        if (item.isChoi() && self.lastLog.compareTo("2020-05-08 14:35:00") < 0) {
                            Database.instance.saveOrtherLog("", self.charname, String.valueOf(item.getName()) + "_" + item.getInfoSave(), "logchoi");
                        }
                        if (item.isChoiSell() && self.lastLog.compareTo("2019-11-29 10:50:00") < 0 && !self.charname.equals(Char.chartopchoitrungthu) && item.dogetbackChoiDuaTop()) {
                            Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "tichthuchoi");
                        } else if (!item.isHoVeLenh() || Char.getDayTime(0L).compareTo("2019-11-30 00:00:00") < 0) {
                            if (Map.isPhiPhong(item.getTemplate().type) && item.minuteExist == 0 && item.dateCreate.equals("2016-09-15")) {
                                Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "ffbug");
                                data.NghiVanGianLan.ghiVatPham(self, "ffbug",
                                        "phi phong loi cu ngay 2016-09-15 van con trong ruong: "
                                      + item.getTemplate().name + " (dbid " + item.dbid + ")");
                            } else if (!Map.openLog && TeamServer.server == 5 && self.lastLog.compareTo("2018-03-12 11:25:00") < 0 && Map.isItemThanThu(item.getTemplate().type)) {
                                Database.instance.saveOrtherLog("tob_log_other_item", self.charname, String.valueOf(item.getTemplate().name) + "|" + item.getDBInfo() + "|" + item.getAttribute(), "hklbug");
                            } else {
                                String countnguhanh = "";
                                final String[] data = Char.split(dtitem[1], ",");
                                for (int j = 0; j < data.length; ++j) {
                                    try {
                                        if (j < 33) {
                                            item.atb[j] = Short.parseShort(data[j].trim());
                                            if (j >= 28 && item.atb[j] > 0 && j != item.idNguHanh2) {
                                                countnguhanh = String.valueOf(countnguhanh) + j + ",";
                                            }
                                        } else if (j < 43) {
                                            item.newAtb[j - 33] = Byte.parseByte(data[j].trim());
                                        } else if (j < 58) {
                                            item.addMoreLevelSkill[j - 43] = Byte.parseByte(data[j].trim());
                                        } else if (j < 61) {
                                            item.lockAtb[j - 58] = Byte.parseByte(data[j].trim());
                                        } else {
                                            item.otherAtt[j - 61] = Short.parseShort(data[j].trim());
                                        }
                                    } catch (final Exception ex2) {
                                    }
                                }
                                if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2020-02-20 13:45:00") < 0 && item.dateCreate.compareTo("2020-02-20 11:40:00") >= 0) {
                                    item.removeOptionHoaNguoiTuyetChoi();
                                }
                                if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2019-02-27 15:45:00") < 0 && item.dateCreate.compareTo("2019-02-26 11:00:00") >= 0) {
                                    item.setOptionHoaNguoiTuyetChoi();
                                }
                                if (ItemLuckyDraw.isChoi(item.getTemplate().id) && self.lastLog.compareTo("2019-11-11 11:45:00") < 0 && item.dateCreate.compareTo("2019-10-03 11:00:00") >= 0) {
                                    item.setOptionHoaNguoiTuyetChoi();
                                }
                                if (!countnguhanh.equals("")) {
                                    try {
                                        final String[] dtnh = Char.split(countnguhanh.substring(0, countnguhanh.length() - 1), ",");
                                        if (dtnh.length > 2) {
                                            final byte idNguhanh = Byte.parseByte(dtnh[Map.r.nextInt(dtnh.length)]);
                                            Database.instance.saveOrtherLog("", self.charname, String.valueOf(item.getName()) + " bi huy dong " + InfoItemAttribute.getNameAtt(idNguhanh) + " chi so " + item.atb[idNguhanh], "huynguhanh");
                                            item.atb[idNguhanh] = 0;
                                        }
                                    } catch (final Exception ex3) {
                                    }
                                }
                                if (ItemLuckyDraw.isChoi(item.getTemplate().id)) {
                                    item.atb[9] = 2;
                                }
                                if (Map.isPhiPhong(item.getTemplate().type) && !Map.openLog && item.magic_physic == 0 && item.atb[1] > item.atb[6]) {
                                    final short d = item.atb[1];
                                    item.atb[1] = item.atb[6];
                                    item.atb[6] = d;
                                }
                                if (item.getTemplate().type == 13) {
                                    if (self.itemAx == null) {
                                        if (ItemLuckyDraw.isChoi(item.getTemplate().id)) {
                                            item.place = 0;
                                            self.iItems.add(item);
                                        } else if (item.place == 1) {
                                            self.itemAx = item;
                                        }
                                    } else if (ItemLuckyDraw.isChoi(item.getTemplate().id)) {
                                        item.place = 0;
                                        self.iItems.add(item);
                                    } else if (item.place == 1) {
                                        item.place = 0;
                                    }
                                }
                                try {
                                    if (item.resetItem != 5000 || Map.isWearingAnimal(item.getTemplate().type)) {
                                        if (Map.isWearingAnimal(item.getTemplate().type)) {
                                            if (!item.isHaveAttLock() && item.colorName == 1) {
                                                switch (item.hangItem) {
                                                    case 1: {
                                                        item.lockItemAnimal(new byte[]{0, 0, 0, 0, 0, 1});
                                                        break;
                                                    }
                                                    case 2: {
                                                        item.lockItemAnimal(new byte[]{0, 0, 0, 0, 1, 0});
                                                        break;
                                                    }
                                                    case 3: {
                                                        item.lockItemAnimal(new byte[]{0, 0, 0, 1, 0, 0});
                                                        break;
                                                    }
                                                    case 4: {
                                                        item.lockItemAnimal(new byte[]{0, 0, 1, 0, 0, 0});
                                                        break;
                                                    }
                                                    case 5: {
                                                        final Item item2 = item;
                                                        final byte[] material = new byte[6];
                                                        material[1] = 1;
                                                        item2.lockItemAnimal(material);
                                                        break;
                                                    }
                                                }
                                            }
                                            item.reFixItemAnimal(self.charname);
                                        }
                                        item.resetItem = 5000;
                                    }
                                } catch (final Exception ex4) {
                                }
                                if (self.charname.indexOf("@") > -1 && ((item.dateCreate.compareTo("2016-08-29") < 0 && TeamServer.server == 3) || (item.dateCreate.compareTo("2016-09-01") < 0 && TeamServer.server == 2)) && item.charSeal.toLowerCase().equals(self.charname.substring(0, self.charname.length() - 3))) {
                                    item.charSeal = self.charname;
                                }
                                if (self.charname.indexOf("@dn") > -1 && !Map.openLog && self.lastLog.compareTo("2017-04-26 10:15:00") < 0 && TeamServer.server == 1 && item.charSeal.toLowerCase().indexOf(self.charname) > -1) {
                                    item.charSeal = self.charname;
                                }
                                if (TeamServer.isServerLienDau() && self.charname.indexOf("_") > -1 && !item.charSeal.equals(self.charname)) {
                                    item.charSeal = self.charname;
                                }
                                final int ao2 = 0;
                                final int quan2 = 0;
                                final int gang = 0;
                                final int non = 0;
                                final int vukhi = 0;
                                final int nhan = 0;
                                final int daychuyen = 0;
                                final int giay = 0;
                                final int ngoc = 0;
                                final int cuoc = 0;
                                final int aochoang = 0;
                                final int ao3 = 0;
                                final int quan3 = 0;
                                final int gang2 = 0;
                                final int non2 = 0;
                                final int vukhi2 = 0;
                                final int nhan2 = 0;
                                final int daychuyen2 = 0;
                                final int giay2 = 0;
                                final int ngoc2 = 0;
                                final int cuoc2 = 0;
                                final int aochoang2 = 0;
                                final int ring = 0;
                                final int ring2 = 0;
                                item.id = self.getIDItem();
                                final int t = item.getTemplate().type;
                                // Vat pham SKIN LINH THU phai di nhanh trang bi thu.
                                //
                                // Ca do nhan vat lan trang bi thu deu duoc luu vao CUNG
                                // MOT cot equip (Database.getInfoCharSave noi getCharWearing
                                // roi noi tiep awItems), nen luc nap lai chi con type de
                                // phan luong. Skin mang type 30, ma isWearingAnimal chi nhan
                                // 14..18, nen neu khong chan o day thi no roi vao nhanh do nhan
                                // vat. Luc do POS_WEARING_CHAR khong co o 30 -> get() tra null
                                // va getIndexItemWearing nem NullPointerException giua luc nap.
                                // (Ban dau skin mang type 19 = type Phi phong: khi do khong nem
                                // loi ma am tham chiem O PHI PHONG, con te hon.)
                                // Nang hon nua, luc do khong go ra duoc: doUseItem chi quet
                                // iItems, con lenh "boskin" chi quet awItems.
                                if (!Map.isWearingAnimal(t) && !SkinLinhThu.laItemSkin(item.getTemplate().id)) {
                                    boolean vk2 = false;
                                    if (t == 6 || t == 7 || t == 4 || t == 5 || t == 3) {
                                        vk2 = true;
                                    }
                                    if (item.clazz != self.charClass && item.getTemplate().type != 13 && item.getTemplate().type != 19) {
                                        item.place = 0;
                                        self.iItems.add(item);
                                        System.out.println(String.valueOf(item.clazz) + " " + self.charClass + " ADD VAO INVEN 0");
                                    } else if (item.getTemplate().gender != 0 && self.gender != 0 && item.getTemplate().type != 13 && item.getTemplate().type != 19 && item.getTemplate().gender != self.gender) {
                                        item.place = 0;
                                        self.iItems.add(item);
                                    } else if (item.level > self.lastLV) {
                                        item.place = 0;
                                        self.iItems.add(item);
                                    } else {
                                        if (t == 8) {
                                            if (posNhan == -1) {
                                                posNhan = item.pos;
                                            } else if (posNhan == item.pos) {
                                                if (posNhan == 0) {
                                                    posNhan = 1;
                                                } else {
                                                    posNhan = 0;
                                                }
                                            } else {
                                                posNhan = item.pos;
                                            }
                                        }
                                        if (!ItemLuckyDraw.isChoi(item.getTemplate().id)) {
                                            final int index2 = Char.getIndexItemWearing(t, posNhan);
                                            if (index2 > -1 && self.wItems[item.slotWearing][index2] == null) {
                                                self.wItems[item.slotWearing][index2] = item;
                                                item.pos = (byte) posNhan;
                                            } else {
                                                item.slotWearing = 0;
                                                item.place = 0;
                                                self.iItems.add(item);
                                            }
                                        }
                                    }
                                } else {
                                    if (self.charname.indexOf("@dn") > -1 && !Map.openLog && self.lastLog.compareTo("2017-04-26 10:15:00") < 0 && TeamServer.server == 1) {
                                        final Item item3 = item;
                                        item3.idAnimal += 12607;
                                    }
                                    // Skin linh thu gan theo NHAN VAT chu khong theo tung
                                    // con thu, nen khong xet checkExistAnimal: nguoi choi
                                    // ban con thu ma skin dang buoc vao van phai giu duoc
                                    // skin. getAnimalWearing se buoc lai idAnimal vao con
                                    // dang cuoi o lan hien thi ke tiep.
                                    if (SkinLinhThu.laItemSkin(item.getTemplate().id)
                                            || self.checkExistAnimal(item.idAnimal)) {
                                        self.awItems.add(item);
                                    } else {
                                        item.place = 0;
                                        item.slotWearing = 0;
                                        item.place = 0;
                                        self.iItems.add(item);
                                    }
                                }
                            }
                        }
                    }
                } catch (final Exception ex5) {
                }
            }
        }
    }

}
