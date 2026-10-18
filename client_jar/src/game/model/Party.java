package game.model;

import java.util.Vector;

public class Party {
    public short idParty;
    Vector userParty;

    public Party() {
        this.idParty = (short) -1;
        this.userParty = new Vector();
    }

    public Party(short id) {
        this.idParty = (short) -1;
        this.userParty = new Vector();
        this.idParty = id;
    }

    public void addUser(PartyInfo user) {
        if (!this.userParty.contains(user)) {
            this.userParty.addElement(user);
        }
    }

    public void removeUser(PartyInfo user) {
        if (this.userParty.contains(user)) {
            this.userParty.removeElement(user);
        }
    }

    public int getSize() {
        return this.userParty.size();
    }

    public Vector getChar() {
        return this.userParty;
    }
}
