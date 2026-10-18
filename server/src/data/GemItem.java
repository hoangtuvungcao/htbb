/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import real.Actor;
import real.GemTemplate;
import real.Map;

public class GemItem extends Actor {
    public byte place;
    public short idGemTemplate;
    public short realId;
    public short idOffGemPlayer;
    public int belongUser = 0;
    public boolean isBoss = false;
    public boolean islock;
    public int belongParty = 0;
    public int ownerId;
    public int dbownerId;
    public int dbid;
    public int prizeSell = 0;
    public String charCanBuy = "";

    public GemItem(int idtemplate) {
        this.idGemTemplate = (short)idtemplate;
        this.cat = 6;
    }

    public GemTemplate getTemplate() {
        return Map.gemTemplate[this.idGemTemplate];
    }

    @Override
    public String getCharcanBuy() {
        return this.charCanBuy;
    }

    @Override
    public void setCharcanBuy(String charname) {
        this.charCanBuy = charname;
    }
}
