package data;

import real.*;

import java.util.Vector;

public class InfoQuestTemplate {

    public short id;
    public short lv;
    public short deltaLv;
    public byte type;
    public byte main_sub;
    public short idNpcRcv;
    public short idNpcRes;
    public String name;
    public String content;
    public String resContent;
    public String shortContent;
    public String infofinish;
    public String infoGift;
    public short pAvtive;
    public int exp;
    public int gold;
    public int luong;
    public int luongLock;
    public Vector<Short> monsterKill;
    public Vector<Short> totalMonsKilled;
    public Vector<Short> itemget;
    public Vector<Short> totalitemget;
    public short[] potiongift;
    public short[] idItemgift;
    public short[] npotiongift;
    // ---- GD2 buoc 4 (chi dat khi sv.lau_dai=1, nap tu data_quest_lau_dai theo id goc) ----
    /** id goc trong data_quest_moi (= data_quest_lau_dai.id_so); 0 = bang cu. */
    public int idGoc;
    /** Ma nhiem vu (vd C01.1); "" = bang cu. */
    public String ma = "";
    /** Ngay mo (tinh tu khai truong). */
    public int ngayMo;
    /** Buoc 1 cua chuong chinh: can 1 ve chuong. */
    public boolean canVe;
    /** Dau chuong / dau huong dan (de chuyen nhan vat cu). */
    public boolean dauChuong;

    public InfoQuestTemplate() {
        this.id = 0;
        this.lv = 1;
        this.deltaLv = 0;
        this.type = 0;
        this.main_sub = 0;
        this.idNpcRcv = 0;
        this.idNpcRes = 0;
        this.name = "";
        this.content = "";
        this.resContent = "";
        this.shortContent = "";
        this.infofinish = "";
        this.infoGift = "";
        this.pAvtive = 0;
        this.exp = 0;
        this.gold = 0;
        this.luong = 0;
        this.luongLock = 0;
        this.monsterKill = new Vector<>();
        this.totalMonsKilled = new Vector<>();
        this.itemget = new Vector<>();
        this.totalitemget = new Vector<>();
    }

    public String getinfoHelpFinishQuest() {
        if (this.type == 1) {
            return this.shortContent;
        }
        return this.infofinish;
    }

    /**
     * So o hanh trang phan thuong nay can (19/10/2026): 1 vat pham (neu co) + moi loai binh MOI. Char.OnQuest kiem
     * truoc khi danh dau DONE - truoc day addGift them thang vao tui, khong kiem suc chua.
     */
    public int soOCanThuong(final Char p) {
        int can = 0;
        try {
            if (this.idItemgift != null && this.idItemgift.length > 0 && this.idItemgift[0] < 32000) {
                can++;
            }
            if (this.potiongift != null && this.potiongift.length > 0 && this.potiongift[0] < 32000) {
                for (int k = 0; k < this.potiongift.length; ++k) {
                    final int id = this.potiongift[k];
                    if (id > 0 && id < p.potions.length && p.potions[id] <= 0 && this.npotiongift[k] > 0) {
                        can++;
                    }
                }
            }
        } catch (final Exception ignored) {
        }
        return can;
    }

    public void addGift(final Char p) {
        try {
            if (this.type == 4) {
                Database.instance.saveOrtherLog("", p.getName(), String.valueOf(this.exp), "expq");
            }
            Map.addXpCharEvent(p, this.exp, false, "InfoQuestTemplate");
//            if (p.countChar > 1 && this.gold > 10000000) {
//                this.gold = 100000;
//                this.luong = 0;
//                this.luongLock = 0;
//            }
            p.addXu(this.gold, "addGift infoquesttemplate");
            p.addLuong(this.luong);
            p.addLuongLock(this.luongLock);

            if (this.idItemgift[0] < 32000) {
                final ItemTemplates itemTemplate = (ItemTemplates) Map.itemTemplates.get(p.charClass).get((int) this.idItemgift[p.charClass]);
                final Item item = new Item(itemTemplate, false, p.charClass, p.charClass, this.idItemgift[p.charClass]);
                item.id = p.getIDItem();
                item.owner = p.charDBID;
                p.iItems.add(item);
                item.level = itemTemplate.level;
                item.identify = p.charDBID;
                item.clazz = p.charClass;
                item.plus = (byte) this.idItemgift[5];
                if (this.idItemgift[5] > 0) {
                    final int[] plus = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 6, 10};
                    for (int i = 0; i < 10; ++i) {
                        final short[] atb = item.atb;
                        atb[i] += (short) ((i > 0) ? 1 : plus[item.plus]);
                    }
                }
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 1));
            }
            if (this.potiongift != null && this.potiongift[0] < 32000) {
                for (int k = 0; k < this.potiongift.length; ++k) {
                    final int[] potions = p.potions;
                    final short n2 = this.potiongift[k];
                    potions[n2] += this.npotiongift[k];
                }
            }
            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public short[] getMonsterKill() {
        final short[] mons = new short[this.monsterKill.size()];
        for (int i = 0; i < mons.length; ++i) {
            mons[i] = this.monsterKill.get(i);
        }
        return mons;
    }

    public short[] getTotalMonsterKill() {
        final short[] mons = new short[this.totalMonsKilled.size()];
        for (int i = 0; i < mons.length; ++i) {
            mons[i] = this.totalMonsKilled.get(i);
        }
        return mons;
    }

    public short[] getItemGet() {
        final short[] mons = new short[this.itemget.size()];
        for (int i = 0; i < mons.length; ++i) {
            mons[i] = this.itemget.get(i);
        }
        return mons;
    }

    public short[] getTotalItemGet() {
        final short[] mons = new short[this.totalitemget.size()];
        for (int i = 0; i < mons.length; ++i) {
            mons[i] = this.totalitemget.get(i);
        }
        return mons;
    }
}
