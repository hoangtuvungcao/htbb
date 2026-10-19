/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Vector;

public class Party {
    public static short MAX_MEMBER = 5;
    public short idParty = -1;
    public short idMaster = -1;
    public Vector<Char> userParty = new Vector<>();

    public Party() {
        this.idMaster = -1;
        this.idParty = -1;
    }

    public Party(short id) {
        this.idParty = id;
    }

    public void addUser(Char user) {
        for (int i = 0; i < this.userParty.size(); i++) {
            if (this.userParty.elementAt(i).id == user.id) {
                return;
            }
        }

        this.userParty.addElement(user);
    }

    public Char remoUser(short idUser) {
        for (int i = 0; i < this.userParty.size(); i++) {
            Char ch = this.userParty.elementAt(i);
            if (ch.id == idUser) {
                this.userParty.removeElementAt(i);
                return ch;
            }
        }

        return null;
    }

    public boolean checkConditionLydi() {
        return this.userParty.size() == 2
            && this.userParty.get(0).gender != this.userParty.get(1).gender
            && this.userParty.get(0).nameHusband_wife.equals(this.userParty.get(1).charname);
    }

    public boolean checkConditionMarry() {
        return this.userParty.size() > 0
            && this.userParty.size() <= 2
            && this.userParty.get(0).gender != this.userParty.get(1).gender
            && this.userParty.get(0).married == 0
            && this.userParty.get(1).married == 0
            && this.userParty.get(0).lvDetail.lv >= 40
            && this.userParty.get(1).lvDetail.lv >= 40;
    }

    public void partyRemove() {
        this.userParty.removeAllElements();
        this.idParty = -1;
        this.idMaster = -1;
    }
}
