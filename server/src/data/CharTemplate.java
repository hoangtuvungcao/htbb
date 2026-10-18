/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class CharTemplate {
    int gender;
    int map;
    int weapon;
    int x;
    int y;
    int nHP1;
    int nMP1;
    int hp;
    int mp;
    int skill1;
    int strength;
    int agitity;
    int spirit;
    int dextrity;
    int luck;

    public CharTemplate(
        int gender,
        int map,
        int weapon,
        int x,
        int y,
        int nHP1,
        int nMP1,
        int hp,
        int mp,
        int skill1,
        int strength,
        int agitity,
        int spirit,
        int dextrity,
        int luck
    ) {
        this.gender = gender;
        this.map = map;
        this.weapon = weapon;
        this.x = x;
        this.y = y;
        this.nHP1 = nHP1;
        this.nMP1 = nMP1;
        this.hp = hp;
        this.mp = mp;
        this.skill1 = skill1;
        this.strength = strength;
        this.agitity = agitity;
        this.spirit = spirit;
        this.dextrity = dextrity;
        this.luck = luck;
    }

    public String getDbInfo(int headStyle, int gender, String charname, int charClass, int hb, int mp, int mapID, int myCountry) {
        String pInfo = "" + headStyle;
        pInfo = pInfo + "," + gender;
        pInfo = pInfo + "," + charname;
        pInfo = pInfo + "," + charClass;
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",1";
        pInfo = pInfo + "," + this.hp;
        pInfo = pInfo + "," + mp;
        pInfo = pInfo + "," + mapID;
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",-1";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",1";
        pInfo = pInfo + ",1";
        pInfo = pInfo + ",1";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",20";
        pInfo = pInfo + "," + myCountry;
        pInfo = pInfo + ",0";
        pInfo = pInfo + ",0";
        return pInfo + ",0";
    }
}
