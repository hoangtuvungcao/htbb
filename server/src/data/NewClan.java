/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Message;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import real.AdminHandler;
import real.Char;
import real.CharManager;
import real.Map;
import real.MessageCreator;
import real.ShopTemplate;
import server.TeamServer;

public class NewClan {
    public Hashtable<String, CharClan> member = new Hashtable<>();
    public Hashtable<String, CharClan> topMem = new Hashtable<>();
    public static byte MAX_LEVEL = 60;
    public long xp = 0L;
    public long money = 0L;
    public long pDevote = 0L;
    public short id = -1;
    public short level = 1;
    public String master = "";
    public String name = "";
    public byte town = 0;
    public byte tax;
    public static String[] titlesClan = new String[]{"Bang chủ", "Phó bang", "Trưởng lão", "Thành viên"};
    public long timeSetTax = 0L;
    public byte[] skillLvClan = new byte[10];
    public short[] skillClan = new short[10];
    public long[] timeUseSkill = new long[10];
    public byte[] shield = new byte[4];
    public String slogan = "";
    public boolean[] offerWages = new boolean[4];
    public byte regGetTown = 0;
    private static Hashtable<Short, NewClan> ALL_CLAN_GAME = new Hashtable<>();
    public byte rankClan = 3;
    public byte country = -1;
    long timeGetTopMember = 0L;
    public Date date;
    public boolean isDel;
    public boolean destroy;
    public long timeStartDel;
    public short allLv = 0;
    static boolean updateXP = false;
    static int[] costClan = new int[]{
        5000000, 10000000, 15000000, 20000000, 25000000, 30000000, 35000000, 40000000, 45000000, 50000000, 55000000, 60000000, 65000000, 70000000
    };
    static int[] mn = new int[]{250000, 250000, 500000, 750000, 1000000, 1250000, 1500000, 1750000, 2000000, 2250000, 2500000, 2750000, 3000000, 3250000};
    static final int[] xpClan = new int[]{
        0,
        1000,
        4000,
        9000,
        16000,
        25000,
        36000,
        49000,
        64000,
        81000,
        100000,
        121000,
        144000,
        169000,
        196000,
        225000,
        256000,
        289000,
        324000,
        361000,
        400000,
        441000,
        484000,
        529000,
        576000,
        625000,
        676000,
        729000,
        784000,
        841000,
        900000,
        961000,
        1024000,
        1089000,
        1156000,
        1225000,
        1296000,
        1369000,
        1444000,
        1521000,
        1600000,
        1681000,
        1764000,
        1849000,
        1936000,
        2025000,
        2116000,
        2209000,
        2304000,
        2401000,
        2500000,
        2601000,
        2704000,
        2809000,
        2916000,
        3025000,
        3136000,
        3249000,
        3364000,
        3481000,
        3600000
    };
    public static byte[] timeDelayOpenShield = new byte[3];
    public static int[] minMoneyClan = new int[]{60000000, 120000000, 180000000};
    static final int[] moneyUpClan = new int[]{
        0,
        250000,
        450000,
        600000,
        800000,
        1250000,
        1800000,
        2450000,
        3200000,
        4050000,
        5000000,
        6050000,
        7200000,
        8450000,
        9800000,
        11250000,
        12800000,
        14450000,
        16200000,
        18050000,
        20000000,
        22050000,
        24200000,
        26450000,
        28800000,
        31250000,
        33800000,
        36450000,
        39200000,
        42050000,
        45000000,
        48050000,
        51200000,
        54450000,
        57800000,
        61250000,
        64800000,
        68450000,
        72200000,
        76050000,
        80000000,
        84050000,
        88200000,
        92450000,
        96800000,
        101250000,
        105800000,
        110450000,
        115200000,
        120050000,
        125000000,
        130050000,
        135200000,
        140450000,
        145800000,
        151250000,
        156800000,
        162450000,
        168200000,
        174050000,
        180000000
    };
    public static byte MAIN_HOUSE = 0;
    public static byte IMBUE_HOUSE = 1;
    public static byte ARMOR_HOUSE = 2;
    public static byte SHOP_HOUSE = 3;
    public static byte BOSS_HOUSE = 4;
    public static byte PET_HOUSE = 5;
    public static byte ANIMAL_HOUSE = 6;
    public static byte TREE_HOUSE = 7;
    public static final String[] NAME_HOUSE_CLAN = new String[]{
        "Nhà quản lý bang", "Nhà thợ rèn", "Nhà trang bị", "Cửa hàng bang", "Nhà nuôi boss", "Nhà luyện thú cưng", "Nhà luyện linh thú", "Nhà cây thần"
    };
    public static byte[] ALL_CARD_UPLEVEL_HOUSE = new byte[]{1, 2, 4, 8};
    public static byte[][] REQUIRE_LEVEL_CLAN = new byte[][]{
        {1, 1, 6, 9, 12, 21, 30, 36, 42, 48},
        {6, 6, 12, 18, 24, 30, 36, 42, 48, 54},
        {9, 9, 12, 15, 21, 27, 33, 39, 45, 51},
        {12, 12, 15, 18, 21, 27, 33, 39, 45, 51},
        {15, 15, 18, 21, 24, 30, 36, 42, 48, 54},
        {18, 18, 24, 27, 33, 39, 45, 51, 54, 58},
        {18, 18, 24, 27, 33, 39, 45, 51, 54, 58},
        {18, 18, 24, 27, 33, 39, 45, 51, 54, 58}
    };
    public static byte[][] ALL_CARD_UPGRADE_HOUSE = new byte[][]{
        {1, 1, 1, 1, 1, 2, 2, 4, 4, 4, 8},
        {1, 1, 1, 2, 2, 2, 4, 4, 4, 8, 8},
        {1, 1, 1, 2, 2, 2, 2, 4, 4, 8, 8},
        {1, 1, 2, 2, 2, 2, 2, 4, 4, 8, 8},
        {1, 1, 2, 2, 2, 4, 4, 8, 8, 8, 8},
        {1, 1, 2, 2, 2, 4, 4, 8, 8, 8, 8},
        {1, 1, 2, 2, 2, 4, 4, 8, 8, 8, 8},
        {1, 1, 2, 2, 2, 4, 4, 8, 8, 8, 8}
    };
    public byte[] levelHouse = new byte[]{1, 0, 0, 1, 0, 0, 0, 0, 0};
    public int[] ALL_CARD = new int[]{1110, 1101, 1110, 1110};
    public int[] ALL_POTION = new int[4];
    public static short DRAGON_CARD = 236;
    public static short UNICORN_CARD = 237;
    public static short TURTLE_CARD = 238;
    public static short PHOENIX_CARD = 239;

    public boolean addMember(Char p) {
        if (this.member.get(p.getName().toLowerCase()) == null) {
            CharClan c = new CharClan(p.getName().toLowerCase(), p.lvDetail.lv);
            c.rankClan = p.rankClan;
            if (c.rankClan < 0) {
                c.rankClan = 3;
                p.rankClan = 3;
            }

            if (c.rankClan == 1 && isEnoughtSubLeader(this.id, 1)) {
                c.rankClan = 3;
                p.rankClan = 3;
            }

            if (c.rankClan == 2 && isEnoughtSubLeader(this.id, 2)) {
                c.rankClan = 3;
                p.rankClan = 3;
            }

            if (p.getName().equals(this.master.toLowerCase())) {
                c.rankClan = 0;
            }

            this.member.put(p.getName().toLowerCase(), c);
            this.allLv = (short)(this.allLv + p.lvDetail.lv);
            return true;
        } else {
            return false;
        }
    }

    public boolean addMember(String name, int lv) {
        if (this.member.get(name.toLowerCase()) == null) {
            this.member.put(name, new CharClan(name.toLowerCase(), lv));
            this.allLv = (short)(this.allLv + lv);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeMember(String name) {
        if (this.member.get(name.toLowerCase()) != null) {
            if (this.member.size() == 10) {
                this.timeStartDel = System.currentTimeMillis();
            }

            CharClan c = this.member.remove(name.toLowerCase());
            this.allLv = (short)(this.allLv - c.lv);
            return true;
        } else {
            return false;
        }
    }

    public String getSkillClan() {
        String st = String.valueOf(this.skillClan[0]);

        for (int i = 1; i < this.skillClan.length; i++) {
            st = st + "," + this.skillClan[i];
        }

        for (int i = 0; i < this.skillClan.length; i++) {
            st = st + "," + this.skillLvClan[i];
        }

        return st;
    }

    public String setRankMem(String name, int rank) {
        CharClan c = this.member.get(name.toLowerCase());
        if (c == null) {
            return "Thành viên chưa tham gia bang " + name;
        } else if (rank >= c.rankClan) {
            return "Không thể phong chức thành viên này";
        } else {
            c.rankClan = (byte)rank;
            return name + " đã được phong làm " + titlesClan[rank] + " bang " + name;
        }
    }

    public String downRankMem(String name, int rank) {
        CharClan c = this.member.get(name.toLowerCase());
        if (c == null) {
            return "Thành viên chưa tham gia bang " + name;
        } else if (rank <= c.rankClan) {
            return "Không thể giáng chức thành viên này";
        } else {
            c.rankClan = (byte)rank;
            return name + " đã bị giáng chức làm " + titlesClan[rank] + " bang " + name;
        }
    }

    public void initSkillClan(String info) {
        String[] data = Char.split(info, ",");

        for (int i = 0; i < data.length; i++) {
            try {
                if (i < 10) {
                    this.skillClan[i] = Short.parseShort(data[i]);
                    this.timeUseSkill[i] = System.currentTimeMillis();
                } else {
                    this.skillLvClan[i - 10] = Byte.parseByte(data[i]);
                }
            } catch (Exception var5) {
            }
        }
    }

    public static void getTopMemberClan(Char p, int idClan) {
        try {
            Vector<CharClan> topMem = new Vector<>();
            NewClan clan = ALL_CLAN_GAME.get((short)idClan);
            if (clan != null) {
                int rank1 = 0;
                int rank2 = 0;
                synchronized (clan.member) {
                    for (CharClan c : clan.member.values()) {
                        if (c.rankClan < 3) {
                            if (c.rankClan == 1 && rank1 == 2) {
                                c.rankClan = 3;
                                Char pp = CharManager.instance.getCharByCharName(c.charname);
                                if (pp != null) {
                                    pp.rankClan = 3;
                                    pp.sendMessage(MessageCreator.createMainCharInfoMessage(pp));
                                    pp.sendMessage(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createMainCharInfoMessage(pp));
                                    MessageCreator.createMsgCharMonster(pp, pp);
                                }

                                Database.instance.updateRankClan(c.charname, 3);
                            } else if (c.rankClan == 2 && rank2 == 7) {
                                c.rankClan = 3;
                                Char pp = CharManager.instance.getCharByCharName(c.charname);
                                if (pp != null) {
                                    pp.rankClan = 3;
                                    pp.sendMessage(MessageCreator.createMainCharInfoMessage(pp));
                                    pp.sendMessage(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createCharInfo(pp));
                                    MessageCreator.createMsgCharMonster(pp, pp);
                                    pp.sendToNearPlayer(MessageCreator.createMainCharInfoMessage(pp));
                                }

                                Database.instance.updateRankClan(c.charname, 3);
                            } else {
                                if (c.rankClan == 1) {
                                    rank1++;
                                } else if (c.rankClan == 2) {
                                    rank2++;
                                }

                                topMem.add(c);
                            }
                        }
                    }
                }

                p.sendMessage(MessageCreator.createMsgTopMemClan(topMem, clan.name, idClan));
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }
    }

    public static Vector<CharClan> getSubLeader(int idClan, int rank) {
        Vector<CharClan> cl = new Vector<>();

        try {
            NewClan clan = ALL_CLAN_GAME.get((short)idClan);
            if (clan != null) {
                synchronized (clan.member) {
                    for (CharClan c : clan.member.values()) {
                        if (c.rankClan == rank) {
                            if (rank == 1 && cl.size() == 2) {
                                c.rankClan = 3;
                                Char pp = CharManager.instance.getCharByCharName(c.charname);
                                if (pp != null) {
                                    pp.rankClan = 3;
                                    pp.sendMessage(MessageCreator.createMainCharInfoMessage(pp));
                                    pp.sendMessage(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createMainCharInfoMessage(pp));
                                    MessageCreator.createMsgCharMonster(pp, pp);
                                }

                                Database.instance.updateRankClan(c.charname, 3);
                            } else if (rank == 2 && cl.size() == 7) {
                                c.rankClan = 3;
                                Char pp = CharManager.instance.getCharByCharName(c.charname);
                                if (pp != null) {
                                    pp.rankClan = 3;
                                    pp.sendMessage(MessageCreator.createMainCharInfoMessage(pp));
                                    pp.sendMessage(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createCharInfo(pp));
                                    pp.sendToNearPlayer(MessageCreator.createMainCharInfoMessage(pp));
                                    MessageCreator.createMsgCharMonster(pp, pp);
                                }

                                Database.instance.updateRankClan(c.charname, 3);
                            } else {
                                cl.add(c);
                            }
                        }
                    }
                }
            }
        } catch (Exception var9) {
        }

        return cl;
    }

    public int getDCHPhatLuongNhanh() {
        if (this.level < 21) {
            return 120;
        } else {
            return this.level < 41 ? 90 : 60;
        }
    }

    public int getDCHPhatLuong() {
        if (this.level < 21) {
            return 60;
        } else {
            return this.level < 41 ? 45 : 30;
        }
    }

    public int getTimePhatLuong() {
        if (this.level < 21) {
            return 24;
        } else {
            return this.level < 41 ? 5 : 3;
        }
    }

    public static NewClan getClan(short id) {
        return ALL_CLAN_GAME.get(id);
    }

    public static void removeClan(short id) {
        ALL_CLAN_GAME.remove(id);
    }

    public static void addClan(NewClan cl) {
        ALL_CLAN_GAME.put(cl.id, cl);
    }

    public boolean isFullMem() {
        return this.member.size() >= 100;
    }

    public String getMem() {
        String mem = "";
        synchronized (this.member) {
            for (CharClan c : this.member.values()) {
                mem = mem + c.charname + ",";
            }
        }

        return !mem.equals("") ? mem.substring(0, mem.length() - 1) : "";
    }

    public String getOtherInfo() {
        String st = "" + (this.offerWages[0] ? 1 : 0);
        st = st + "," + (this.offerWages[1] ? 1 : 0);
        st = st + "," + (this.offerWages[2] ? 1 : 0);
        st = st + "," + (this.offerWages[3] ? 1 : 0);
        return st + "," + this.regGetTown;
    }

    public static boolean isTimeOfferWages() {
        return true;
    }

    public void initOtherInfo(String info) {
        try {
            String[] data = Char.split(info, ",");
            if (isTimeOfferWages()) {
                this.offerWages = new boolean[4];
            } else {
                for (int i = 0; i < this.offerWages.length; i++) {
                    this.offerWages[i] = Byte.parseByte(data[i]) == 1;
                }
            }

            this.regGetTown = Byte.parseByte(data[data.length - 1]);
        } catch (Exception var4) {
        }
    }

    public void addMoney2Clan(long money) {
        this.money += money;
    }

    public boolean isPhoBang(Char p) {
        if (p.idClan != this.id) {
            this.member.remove(p.getName().toLowerCase());
            return false;
        } else {
            CharClan c = this.member.get(p.getName().toLowerCase());
            if (c != null) {
                Vector<CharClan> vc = getSubLeader(this.id, 1);

                for (int i = 0; i < vc.size(); i++) {
                    if (vc.get(i).charname.toLowerCase().equals(p.getName().toLowerCase())) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public Vector<CharClan> getAllMember() {
        Vector<CharClan> mem = new Vector<>();
        Vector<CharClan> remove = new Vector<>();
        synchronized (this.member) {
            for (CharClan c : this.member.values()) {
                Char p = CharManager.instance.getCharByCharName(c.charname.toLowerCase());
                if (p != null && p.idClan != this.id) {
                    remove.add(c);
                } else {
                    if (c.rankClan == 0 && !c.charname.equals(this.master)) {
                        c.rankClan = 3;
                        if (p != null) {
                            p.rankClan = 3;
                        }
                    } else if (c.charname.equals(this.master)) {
                        c.rankClan = 0;
                    }

                    mem.add(c);
                }
            }

            for (int i = 0; i < remove.size(); i++) {
                this.member.remove(remove.get(i).charname.toLowerCase());
            }

            return mem;
        }
    }

    public void updateNewClandata2DB() {
        Vector<NewClan> cl = new Vector<>();
        cl.add(this);
        Database.instance.updateNewClanData(cl);
    }

    public Vector<String> getMember() {
        Vector<String> mem = new Vector<>();
        synchronized (this.member) {
            for (CharClan c : this.member.values()) {
                if (c.rankClan == 3) {
                    mem.add(c.charname);
                }
            }

            return mem;
        }
    }

    public Vector<String> getMemberReceiveMoney() {
        Vector<String> mem = new Vector<>();
        synchronized (this.member) {
            for (CharClan c : this.member.values()) {
                if (c.rankClan == 3) {
                    Char p = CharManager.instance.getCharByCharName(c.charname);
                    if (p != null && p.rankClan == 3 && p.checkNhanLuongBang() == null) {
                        mem.add(c.charname);
                    }
                }
            }

            return mem;
        }
    }

    public String getListMemberClan() {
        String info = "";
        synchronized (this.member) {
            for (CharClan c : this.member.values()) {
                info = info + c.charname + ",";
            }

            return info;
        }
    }

    public void giveGif() {
        synchronized (this.member) {
            for (CharClan c : this.member.values()) {
                Char p = CharManager.instance.getCharByCharName(c.charname.toLowerCase());
                if (p != null) {
                    p.potions[84] = p.potions[84] + 5;
                    p.potions[91]++;
                    Database.instance.saveOrtherLog("tob_log_other_potion", p.getName(), "mo, crazy", "mo,CRAZY");
                    ShopTemplate item = Map.getShopTemplate(0);
                    if (p.listSpItem[item.id] == 0) {
                        ShopTemplate spItem = new ShopTemplate();
                        spItem.coppy(spItem, item);
                        spItem.ownerId = p.charDBID;
                        spItem.realId = p.getIDItem();
                        p.specialItem.add(spItem);
                    }

                    p.listSpItem[item.id]++;
                    p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
                    p.sendMessage(MessageCreator.createCharSpecialItem(p));
                    Database.instance.saveOrtherLog("tob_log_other_potion", p.getName(), "ve 100", "ve 100");
                }
            }
        }
    }

    public long getMoneyDistributeSalary(int idClan) {
        long salary = 0L;
        int index = 0;
        int nMember = this.getAllMember().size();
        if (nMember > 30 && nMember <= 60) {
            index = 1;
        } else if (nMember > 60) {
            index = 2;
        }

        if (this.money >= minMoneyClan[index]) {
            salary = this.money - minMoneyClan[index];
        }

        return salary;
    }

    public int canSetTax() {
        return this.timeSetTax == 0L ? 0 : 30 - (int)(System.currentTimeMillis() - this.timeSetTax) / 60000;
    }

    public static void updateTown(int id, int idtown, int country) {
        Vector<NewClan> clandata = new Vector<>();
        synchronized (ALL_CLAN_GAME) {
            for (NewClan cl : ALL_CLAN_GAME.values()) {
                if (cl.country == country) {
                    clandata.add(cl);
                }
            }

            for (int i = 0; i < clandata.size(); i++) {
                if (clandata.get(i).id == id) {
                    clandata.get(i).town = (byte)idtown;
                } else {
                    clandata.get(i).town = 0;
                }

                clandata.get(i).tax = 0;
            }
        }
    }

    public static synchronized void addXpClan(NewClan cl, int xp) {
        cl.xp += xp;
        if (cl.xp < 0L) {
            cl.xp = 0L;
        }

        calLevelClan(cl);
    }

    public static void calLevelClan(NewClan c) {
        byte level = 1;

        for (int i = 0; i < xpClan.length && xpClan[i] < c.xp; i++) {
            level++;
        }

        c.level = level;
    }

    public static long getMoneyClan(int id) {
        NewClan clan = ALL_CLAN_GAME.get((short)id);
        return clan != null ? clan.money : 0L;
    }

    public static int getMoneyUplevelClan(int id) {
        NewClan clan = ALL_CLAN_GAME.get((short)id);
        return clan != null && clan.level < 61 ? moneyUpClan[clan.level] : 1000000000;
    }

    public static int getXPUplevelClan(int id) {
        NewClan clan = ALL_CLAN_GAME.get((short)id);
        return clan != null && clan.level < 61 ? xpClan[clan.level] : 1000000000;
    }

    public static boolean enoughtXP(int id) {
        NewClan clan = ALL_CLAN_GAME.get((short)id);
        return clan != null && clan.level < 61 ? clan.xp >= getXPUplevelClan(id) : false;
    }

    public static void setTax(int id, int tax) {
        NewClan clan = ALL_CLAN_GAME.get((short)id);
        clan.tax = (byte)tax;
        clan.timeSetTax = System.currentTimeMillis();
    }

    public synchronized long timeDel() {
        return 4320L - (System.currentTimeMillis() - this.timeStartDel) / 60000L;
    }

    public synchronized long timeExpire() {
        return 4320L - (System.currentTimeMillis() - this.timeStartDel) / 60000L;
    }

    public static boolean checkNewDay() {
        Calendar cl = Calendar.getInstance();
        int iHour = cl.get(11);
        if (iHour == 0 && !updateXP) {
            updateXP = true;
            Map.resetAllShield();
            Database.instance.saveEvent(Map.event.getInfo());
            return true;
        } else {
            if (iHour != 0 && updateXP) {
                updateXP = false;
            }

            return false;
        }
    }

    public void checkTimeSkill() {
        for (int i = 0; i < this.timeUseSkill.length; i++) {
            if (this.skillClan[i] > 0 && System.currentTimeMillis() - this.timeUseSkill[i] >= 60000L) {
                this.skillClan[i]--;
                this.timeUseSkill[i] = System.currentTimeMillis();
            }
        }
    }

    public static void startThreadSaveClan() {
        (new Thread() {
            @Override
            public void run() {
                Thread.currentThread().setName("CAP NHAT THOG TIN LV CLAN");

                while (TeamServer.running) {
                    try {
                        if (!AdminHandler.isStopServer) {
                            NewClan.updateAllClan();
                            Thread.sleep(300000L);
                        }
                    } catch (Exception var2) {
                    }
                }
            }
        }).start();
        (new Thread() {
                @Override
                public void run() {
                    Thread.currentThread().setName("kiem tra bang");

                    while (TeamServer.running) {
                        try {
                            if (!AdminHandler.isStopServer) {
                                NewClan.checkTimeClanExpired();

                                try {
                                    for (NewClan clan : NewClan.ALL_CLAN_GAME.values()) {
                                        clan.checkTimeSkill();
                                    }
                                } catch (Exception var4) {
                                }

                                for (byte i = 0; i < 3; i++) {
                                    if (Map.idClanTown[i] > -1) {
                                        int ihour = Char.getIdOpenShield(Calendar.getInstance().get(11));
                                        if (Map.SHIELD[i][ihour] == 0 && Calendar.getInstance().get(11) >= 6) {
                                            NewClan.timeDelayOpenShield[i]++;
                                            if (NewClan.timeDelayOpenShield[i] % 15 == 0) {
                                                Map.sendAllCharServer(
                                                    -1,
                                                    MessageCreator.createServerAlertAutoOffMessage(
                                                        "Lãnh thổ " + Map.nameCountry[i] + " đã không được bảo hộ trong " + NewClan.timeDelayOpenShield[i] + " phút."
                                                    )
                                                );
                                                NewClan clan = NewClan.getClan(Map.idClanTown[i]);
                                                if (NewClan.timeDelayOpenShield[i] < 120) {
                                                    Map.sendAllCharServer(
                                                        -1,
                                                        MessageCreator.createServerAlertAutoOffMessage(
                                                            "Bang "
                                                                + clan.name
                                                                + " sẽ mất quyền sở hữu thành nếu không bật bảo hộ cho lãnh thổ của mình sau "
                                                                + (120 - NewClan.timeDelayOpenShield[i])
                                                                + " phút nữa"
                                                        )
                                                    );
                                                } else {
                                                    Map.sendAllCharServer(
                                                        -1,
                                                        MessageCreator.createServerAlertAutoOffMessage(
                                                            "Bang "
                                                                + clan.name
                                                                + " đã mất quyền sở hữu thành do không bật bảo hộ cho lãnh thổ của mình sau "
                                                                + 120
                                                                + " phút. Lãnh thổ "
                                                                + Map.nameCountry[i]
                                                                + " sẽ được bảo hộ miễn phí"
                                                        )
                                                    );
                                                    Map.idClanTown[i] = -1;
                                                    clan.town = 0;
                                                    clan.tax = 0;
                                                    Map.taxOfClan[i] = 0;
                                                    clan.updateNewClandata2DB();
                                                    Map.SHIELD[i][0] = 0;
                                                    Map.SHIELD[i][1] = 0;
                                                    Map.SHIELD[i][2] = 0;
                                                    NewClan.timeDelayOpenShield[i] = 0;
                                                    Database.instance.saveOrtherLog("", clan.name, "mat quyen so huu bang", "lostTown");
                                                    Char.gov[i].reset();
                                                    Database.instance.saveEvent(Map.event.getInfo());
                                                }
                                            }
                                        } else {
                                            NewClan.timeDelayOpenShield[i] = 0;
                                        }
                                    }
                                }

                                Thread.sleep(60000L);
                            }
                        } catch (Exception var5) {
                            var5.printStackTrace();
                        }
                    }
                }
            })
            .start();
    }

    public static void checkTimeClanExpired() {
        try {
            boolean newday = checkNewDay();
            Vector<NewClan> delClan = new Vector<>();
            synchronized (ALL_CLAN_GAME) {
                for (NewClan cif : ALL_CLAN_GAME.values()) {
                    if (cif.timeDel() <= 0L && cif.isDel && cif.timeStartDel > 0L
                        || cif.getAllMember().size() < 10 && cif.timeExpire() <= 0L && cif.timeStartDel > 0L) {
                        Vector<CharClan> allmem = cif.getAllMember();
                        Database.instance
                            .saveLogClan(
                                "clandata",
                                "cdel",
                                "giải tán bang " + cif.name + " | " + cif.id + " | " + allmem.size() + " | " + cif.isDel + "|" + cif.getListMemberClan()
                            );

                        for (int i = 0; i < cif.getAllMember().size(); i++) {
                            Char p = CharManager.instance.getCharByCharName(allmem.get(i).charname);
                            if (p != null) {
                                p.idClan = -1;
                                p.sendMessage(MessageCreator.createServerAlertMessage("Bang " + cif.name + " đã bị giải tán", ""));
                                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                                p.sendMessage(MessageCreator.createSkillClan(p));
                                MessageCreator.createMsgCharMonster(p, p);
                            } else {
                                Database.instance.updateCharIDClan(allmem.get(i).charname, -1);
                                CharInboxMessage newMsg = new CharInboxMessage();
                                newMsg.sender = cif.master;
                                newMsg.info = "Bang " + cif.name + " đã giải tán";
                                newMsg.idMsg = Database.instance.addNewMessage(allmem.get(i).charname, newMsg);
                            }
                        }

                        Map.putId2IdClan(cif.id);
                        delClan.add(cif);
                    } else if (newday) {
                        int xp = cif.getAllMember().size() * 24;
                        cif.addMoney2Clan(-mn[cif.level / 6]);
                        Database.instance
                            .saveLogClan(
                                cif.name,
                                "submoney",
                                mn[cif.level / 6]
                                    + "_"
                                    + cif.name
                                    + " | "
                                    + cif.id
                                    + " | "
                                    + cif.getAllMember().size()
                                    + " | "
                                    + cif.isDel
                                    + "|"
                                    + cif.getListMemberClan()
                            );
                        if (cif.money < costClan[cif.level / 6]) {
                            addXpClan(cif, -xp / 4);
                            Database.instance
                                .saveLogClan(
                                    cif.name,
                                    "subexp",
                                    xp / 4 + "_" + cif.name + " | " + cif.id + " | " + cif.getAllMember().size() + " | " + cif.isDel + "|" + cif.getListMemberClan()
                                );
                        } else {
                            addXpClan(cif, xp);
                            Database.instance
                                .saveLogClan(
                                    cif.name,
                                    "addExp",
                                    xp + "_" + cif.name + " | " + cif.id + " | " + cif.getAllMember().size() + " | " + cif.isDel + "|" + cif.getListMemberClan()
                                );
                        }
                    }
                }

                while (delClan.size() > 0) {
                    NewClan cifx = delClan.remove(0);
                    ALL_CLAN_GAME.remove(cifx.id);
                    Database.instance.delClan(cifx.id);
                }
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }

    public static boolean checkDelClanAuto() {
        try {
            Vector<NewClan> delClan = new Vector<>();

            for (NewClan cif : ALL_CLAN_GAME.values()) {
                if (cif.timeDel() <= 0L && cif.isDel && cif.timeStartDel > 0L || cif.getAllMember().size() < 10 && cif.timeExpire() <= 0L && cif.timeStartDel > 0L) {
                    Database.instance
                        .saveLogClan(
                            "clandata",
                            "cdel",
                            "giải tán bang " + cif.name + " | " + cif.id + " | " + cif.getAllMember().size() + " | " + cif.isDel + "|" + cif.getListMemberClan()
                        );
                    Message m = MessageCreator.createServerAlertMessage("Bang " + cif.name + " đã bị giải tán", "");
                    Vector<CharClan> allmem = cif.getAllMember();

                    for (int i = 0; i < allmem.size(); i++) {
                        Char p = CharManager.instance.getCharByCharName(allmem.get(i).charname);
                        if (p != null) {
                            p.idClan = -1;
                            p.sendMessage(m);
                            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                            p.sendMessage(MessageCreator.createSkillClan(p));
                            MessageCreator.createMsgCharMonster(p, p);
                        } else {
                            Database.instance.updateCharIDClan(allmem.get(i).charname, -1);
                            CharInboxMessage newMsg = new CharInboxMessage();
                            newMsg.sender = cif.master;
                            newMsg.info = "Bang " + cif.name + " đã giải tán";
                            newMsg.idMsg = Database.instance.addNewMessage(cif.getAllMember().get(i).charname, newMsg);
                        }
                    }

                    Map.putId2IdClan(cif.id);
                    delClan.add(cif);
                }
            }

            while (delClan.size() > 0) {
                NewClan cifx = delClan.remove(0);
                ALL_CLAN_GAME.remove(cifx.id);
                Database.instance.delClan(cifx.id);
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return false;
    }

    public void setSkillClan(int idSkill, short time, int devoteBuy, int lvSkill) {
        this.skillClan[idSkill] = time;
        this.timeUseSkill[idSkill] = System.currentTimeMillis();
        this.skillLvClan[idSkill] = (byte)lvSkill;
        this.pDevote -= devoteBuy;

        for (CharClan c : this.member.values()) {
            Char p = CharManager.instance.getCharByCharName(c.charname);
            if (p != null) {
                p.skillClan[idSkill] = time;
                p.timeUseSkillClan[idSkill] = System.currentTimeMillis();
                p.skillLvClan[idSkill] = (byte)lvSkill;
                p.sendMessage(MessageCreator.createSkillClan(p));
            }
        }

        this.updateNewClandata2DB();
    }

    public static boolean isEnoughtSubLeader(int idClan, int rank) {
        Vector<CharClan> info = getSubLeader(idClan, rank);
        if (rank == 1) {
            return info.size() == 2;
        } else {
            return rank == 2 ? info.size() == 7 : true;
        }
    }

    public static void setRankClan(String name, int titles, int idClan) {
        Database.instance.updateRankClan(name, titles);
        Char p = CharManager.instance.getCharByCharName(name);
        if (p != null) {
            p.rankClan = (byte)titles;
            Message m = MessageCreator.createCharInfo(p);
            p.sendMessage(m);
            p.sendToNearPlayer(m);
            MessageCreator.createMsgCharMonster(p, p);
        }
    }

    public static void quickSortMem(Vector<CharClan> actors) {
        recQuickSortMem(actors, 0, actors.size() - 1);
    }

    private static void recQuickSortMem(Vector<CharClan> actors, int left, int right) {
        try {
            if (right - left <= 0) {
                return;
            }

            String pivot = actors.elementAt(right).charname.toLowerCase();
            int partition = partitionItMem(actors, left, right, pivot);
            recQuickSortMem(actors, left, partition - 1);
            recQuickSortMem(actors, partition + 1, right);
        } catch (Exception var5) {
        }
    }

    private static int partitionItMem(Vector<CharClan> actors, int left, int right, String pivot) {
        int leftPtr = left - 1;
        int rightPtr = right;

        try {
            while (true) {
                while (actors.elementAt(++leftPtr).charname.toLowerCase().compareTo(pivot) < 0) {
                }

                while (rightPtr > 0) {
                    if (actors.elementAt(--rightPtr).charname.toLowerCase().compareTo(pivot) <= 0) {
                        break;
                    }
                }

                if (leftPtr >= rightPtr) {
                    swapMem(actors, leftPtr, right);
                    break;
                }

                swapMem(actors, leftPtr, rightPtr);
            }
        } catch (Exception var7) {
            System.out.println("LOI PAINT partitionIt TRONG UTIL");
        }

        return leftPtr;
    }

    private static void swapMem(Vector<CharClan> actors, int dex1, int dex2) {
        CharClan temp = actors.elementAt(dex2);
        if (!actors.elementAt(dex2).charname.toLowerCase().equals(actors.elementAt(dex1).charname.toLowerCase())) {
            actors.setElementAt(actors.elementAt(dex1), dex2);
            actors.setElementAt(temp, dex1);
        }
    }

    public static void updateAllClan() {
        Vector<NewClan> allclan = new Vector<>();
        synchronized (ALL_CLAN_GAME) {
            for (NewClan clan : ALL_CLAN_GAME.values()) {
                allclan.add(clan);
            }
        }

        Database.instance.updateNewClanData(allclan);
    }

    public String doUpgradeHouse(int idHouse, Char p) {
        if (p.rankClan != 0) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Chỉ có bang chủ mới có thể nâng cấp nhà", ""));
            return "";
        } else if (this.levelHouse[idHouse] >= 10) {
            p.sendMessage(MessageCreator.createServerAlertMessage("Không thể nâng cấp thêm", ""));
            return "";
        } else {
            int requireLvClan = REQUIRE_LEVEL_CLAN[idHouse][this.levelHouse[idHouse]];
            if (this.level < requireLvClan) {
                p.sendMessage(MessageCreator.createServerAlertMessage("Bang phải đạt cấp độ " + requireLvClan + " mới có thể nâng cấp.", ""));
                return "";
            } else {
                int sl = ALL_CARD_UPGRADE_HOUSE[idHouse][this.levelHouse[idHouse]];
                if (this.ALL_CARD[DRAGON_CARD - 236] >= sl
                    && this.ALL_CARD[UNICORN_CARD - 236] >= sl
                    && this.ALL_CARD[TURTLE_CARD - 236] >= sl
                    && this.ALL_CARD[PHOENIX_CARD - 236] >= sl) {
                    this.levelHouse[idHouse]++;
                    this.ALL_CARD[DRAGON_CARD - 236] = this.ALL_CARD[DRAGON_CARD - 236] - sl;
                    this.ALL_CARD[UNICORN_CARD - 236] = this.ALL_CARD[UNICORN_CARD - 236] - sl;
                    this.ALL_CARD[TURTLE_CARD - 236] = this.ALL_CARD[TURTLE_CARD - 236] - sl;
                    this.ALL_CARD[PHOENIX_CARD - 236] = this.ALL_CARD[PHOENIX_CARD - 236] - sl;
                    Database.instance.saveOrtherLog("", p.getName(), "Nang cap nha " + idHouse + " lên lv " + this.levelHouse[idHouse], "upgradehouse");
                    p.sendMessage(
                        MessageCreator.createServerAlertMessage("Nâng cấp thành công " + NAME_HOUSE_CLAN[idHouse] + " lên cấp " + this.levelHouse[idHouse], "")
                    );
                    return "";
                } else {
                    p.sendMessage(MessageCreator.createServerAlertMessage("Bang phải có đủ " + sl + " các loại thẻ Long, Lân, Quy, Phụng mới có thể nâng cấp.", ""));
                    return "";
                }
            }
        }
    }
}
