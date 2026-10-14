package game.model;

import java.util.Vector;

public class Party {
    public short idParty;
    Vector userParty;

    public Party() {
        this.idParty = (short) -1;
        this.userParty = new Vector();
    }

    public Party(short s) {
        this.idParty = (short) -1;
        this.userParty = new Vector();
        this.idParty = s;
    }

    public void addUser(PartyInfo partyInfo) {
        if (this.userParty.contains(partyInfo)) {
            return;
        }
        this.userParty.addElement(partyInfo);
    }

    public void removeUser(PartyInfo partyInfo) {
        if (this.userParty.contains(partyInfo)) {
            this.userParty.removeElement(partyInfo);
        }
    }

    public int getSize() {
        return this.userParty.size();
    }

    public Vector getChar() {
        return this.userParty;
    }
}
