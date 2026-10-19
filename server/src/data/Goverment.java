/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.io.IOException;
import real.Char;
import real.CharManager;
import real.MessageCreator;

public class Goverment {
    public static final byte KING = 0;
    public static final byte DAITHAN = 1;
    public static final byte TUONGQUAN = 2;
    public static final byte BODAU = 3;
    public static final String[] NAME = new String[]{"Vua", "Đại thần", "Tướng quân", "Bộ đầu"};
    public String king = "";
    public String[] daithan = new String[]{"", ""};
    public String[] tuongquan = new String[]{"", ""};
    public String[] bodau = new String[]{"", ""};

    public boolean addMember(int pos, String charname) {
        charname = charname.trim();
        switch (pos) {
            case 0:
                if (!this.king.equals("")) {
                    return false;
                }

                this.king = charname;
                break;
            case 1:
                if (!this.daithan[0].equals("") && !this.daithan[1].equals("")) {
                    return false;
                }

                for (int ixx = 0; ixx < 2; ixx++) {
                    if (this.daithan[ixx].equals("")) {
                        this.daithan[ixx] = charname.toLowerCase();
                        return true;
                    }
                }
                break;
            case 2:
                if (!this.tuongquan[0].equals("") && !this.tuongquan[1].equals("")) {
                    return false;
                }

                for (int ix = 0; ix < 2; ix++) {
                    if (this.tuongquan[ix].equals("")) {
                        this.tuongquan[ix] = charname.toLowerCase();
                        return true;
                    }
                }
                break;
            case 3:
                if (!this.bodau[0].equals("") && !this.bodau[1].equals("")) {
                    return false;
                }

                for (int i = 0; i < 2; i++) {
                    if (this.bodau[i].equals("")) {
                        this.bodau[i] = charname.toLowerCase();
                        break;
                    }
                }
        }

        return true;
    }

    public boolean removeMember(int pos, String charname) {
        charname = charname.trim();
        switch (pos) {
            case 0:
                return false;
            case 1:
                for (int ixx = 0; ixx < 2; ixx++) {
                    if (this.daithan[ixx].equals(charname.toLowerCase())) {
                        this.daithan[ixx] = "";
                        return true;
                    }
                }
                break;
            case 2:
                for (int ix = 0; ix < 2; ix++) {
                    if (this.tuongquan[ix].equals(charname.toLowerCase())) {
                        this.tuongquan[ix] = "";
                        return true;
                    }
                }
                break;
            case 3:
                for (int i = 0; i < 2; i++) {
                    if (this.bodau[i].equals(charname.toLowerCase())) {
                        this.bodau[i] = "";
                        return true;
                    }
                }
        }

        return false;
    }

    public void removeAll() {
        this.king = "";
        this.daithan = new String[]{"", ""};
        this.bodau = new String[]{"", ""};
        this.tuongquan = new String[]{"", ""};
    }

    public void reset() {
        Char p = CharManager.instance.getCharByCharName(this.king);
        if (p != null) {
            p.rankGov = -1;
            p.potions[78] = 0;
            p.potions[28] = 0;
            if (p.potions[27] == 1) {
                p.potions[27] = 0;
            }

            p.potions[87] = 0;
            p.potions[85] = 0;
            p.calculateAttrib();

            try {
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            } catch (IOException var8) {
            }

            p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharWearingMessage(p, p));
            p.sendToNearPlayer(MessageCreator.createCharInfo(p));
            MessageCreator.createMsgCharMonster(p, p);

            try {
                p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
            } catch (IOException var7) {
            }

            p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
        }

        for (int i = 0; i < 2; i++) {
            p = CharManager.instance.getCharByCharName(this.daithan[i]);
            this.daithan[i] = "";
            if (p != null) {
                p.rankGov = -1;
                p.potions[78] = 0;
                p.potions[28] = 0;
                if (p.potions[27] == 1) {
                    p.potions[27] = 0;
                }

                p.potions[87] = 0;
                p.potions[85] = 0;

                try {
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } catch (IOException var6) {
                }

                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            }
        }

        for (int ix = 0; ix < 2; ix++) {
            p = CharManager.instance.getCharByCharName(this.tuongquan[ix]);
            this.tuongquan[ix] = "";
            if (p != null) {
                p.rankGov = -1;
                p.potions[78] = 0;
                p.potions[28] = 0;
                if (p.potions[27] == 1) {
                    p.potions[27] = 0;
                }

                p.potions[87] = 0;
                p.potions[85] = 0;

                try {
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } catch (IOException var5) {
                }

                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            }
        }

        for (int ixx = 0; ixx < 2; ixx++) {
            p = CharManager.instance.getCharByCharName(this.bodau[ixx]);
            this.bodau[ixx] = "";
            if (p != null) {
                p.rankGov = -1;
                p.potions[78] = 0;
                p.potions[28] = 0;
                if (p.potions[27] == 1) {
                    p.potions[27] = 0;
                }

                p.potions[87] = 0;
                p.potions[85] = 0;

                try {
                    p.sendMessage(MessageCreator.createMainCharInfoMessage(p));
                } catch (IOException var4) {
                }

                p.sendMessage(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharWearingMessage(p, p));
                p.sendToNearPlayer(MessageCreator.createCharInfo(p));
                p.sendMessage(MessageCreator.createCharInventoryMessage(p, 0));
            }
        }

        this.removeAll();
    }

    public String getInfo() {
        String info = this.king;
        String[] info1 = new String[]{"", ""};

        for (int i = 0; i < 2; i++) {
            info1[i] = this.daithan[i];
        }

        info = info + "," + info1[0] + "," + info1[1];
        info1[0] = "";
        info1[1] = "";

        for (int i = 0; i < 2; i++) {
            info1[i] = this.tuongquan[i];
        }

        info = info + "," + info1[0] + "," + info1[1];
        info1[0] = "";
        info1[1] = "";

        for (int i = 0; i < 2; i++) {
            info1[i] = this.bodau[i];
        }

        return info + "," + info1[0] + "," + info1[1];
    }

    public byte getRankGov(String charname) {
        if (charname.equals(this.king)) {
            return 0;
        } else {
            for (int i = 0; i < 2; i++) {
                if (this.daithan[i].equals(charname.toLowerCase())) {
                    return 1;
                }
            }

            for (int ix = 0; ix < 2; ix++) {
                if (this.tuongquan[ix].equals(charname.toLowerCase())) {
                    return 2;
                }
            }

            for (int ixx = 0; ixx < 2; ixx++) {
                if (this.bodau[ixx].equals(charname.toLowerCase())) {
                    return 3;
                }
            }

            return -1;
        }
    }
}
