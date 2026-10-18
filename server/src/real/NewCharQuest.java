/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import data.Database;
import data.InfoQuestTemplate;
import data.NewClan;

public class NewCharQuest {
    public byte main_sub = 0;
    public static byte FINISH = 1;
    public static byte WORKING = 2;
    public static byte UN_RECEIVE = 0;
    public static byte DONE = 3;
    public short id_quest = 0;
    public short[] nItem = null;
    public short[] monsterKilled = null;
    public short state_quest = 0;

    public NewCharQuest(boolean isSub) {
        if (isSub) {
            this.id_quest = -1;
        }
    }

    public NewCharQuest(short id, byte main_sub) {
        this.main_sub = main_sub;
        this.id_quest = id;
        InfoQuestTemplate info = this.getTemplate();
        this.nItem = new short[info.totalitemget.size()];
        this.monsterKilled = new short[info.totalMonsKilled.size()];
    }

    public InfoQuestTemplate getTemplate() {
        if (this.main_sub == 0) {
            return Map.allMainQuest.get(this.id_quest);
        } else {
            return this.main_sub == 2 ? Map.allClanQuest.get(this.id_quest) : Map.allSubQuest.get(this.id_quest);
        }
    }

    public void initQuest(short idQuest, String[] nITemGet, String[] nMonsKilled, String finish) {
        this.id_quest = idQuest;
        this.nItem = new short[nITemGet.length];

        for (int i = 0; i < nITemGet.length; i++) {
            try {
                this.nItem[i] = Short.parseShort(nITemGet[i]);
            } catch (Exception var9) {
            }
        }

        this.monsterKilled = new short[nMonsKilled.length];

        for (int i = 0; i < nMonsKilled.length; i++) {
            try {
                this.monsterKilled[i] = Short.parseShort(nMonsKilled[i]);
            } catch (Exception var8) {
            }
        }

        try {
            this.state_quest = Byte.parseByte(finish);
        } catch (Exception var7) {
        }
    }

    public String getInfoItemGet() {
        String info = "";
        if (this.nItem != null) {
            info = String.valueOf(this.nItem[0]);

            for (int i = 1; i < this.nItem.length; i++) {
                info = info + "," + this.nItem[i];
            }
        }

        return info;
    }

    public String getInfoMonsKilled() {
        String info = "";
        if (this.monsterKilled != null) {
            info = String.valueOf(this.monsterKilled[0]);

            for (int i = 1; i < this.monsterKilled.length; i++) {
                info = info + "," + this.monsterKilled[i];
            }
        }

        return info;
    }

    public void desTroy() {
        this.state_quest = -1;
    }

    public void reset() {
        this.state_quest = 0;
        if (this.nItem != null) {
            for (int i = 0; i < this.nItem.length; i++) {
                this.nItem[i] = 0;
            }
        }

        if (this.monsterKilled != null) {
            for (int i = 0; i < this.monsterKilled.length; i++) {
                this.monsterKilled[i] = 0;
            }
        }
    }

    public void addGift(Char p) {
        try {
            this.getTemplate().addGift(p);
            if (this.main_sub == 0 && this.id_quest == 7) {
                p.skill[0] = 1;
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }
    }

    public void addGiftClan(Char p) {
        try {
            if (p.idClan == -1) {
                return;
            }

            InfoQuestTemplate info = this.getTemplate();
            NewClan clan = NewClan.getClan(p.idClan);
            NewClan.addXpClan(clan, info.exp);
            int pdevote = 40 + Map.r.nextInt(11);
            clan.pDevote += pdevote;
            p.pointCongHienClan += pdevote;
            p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            Database.instance.saveOrtherLog("", p.charname, info.name + "_" + info.exp, "qclan");
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }
}
