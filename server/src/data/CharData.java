/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;
import real.Item;

public class CharData {
    public int id;
    public String charname;
    public byte headStyle;
    public byte charClass = 0;
    public byte myCountry = -1;
    public byte gender;
    public int xp;
    public int hp;
    public int mp;
    public int map;
    public int x;
    public int y;
    public int gold;
    public int lv;
    public int using = 1;
    public long timeDell = 0L;
    public int idBigAvtPP = -1;
    public int indexXyPP = 0;
    public int nFramePP = 1;
    public int idBigAvtAo = -1;
    public int indexXyAo = 1;
    public int nFrameAo = 1;
    public int idBigAvtVK = -1;
    public int indexXyVK = 0;
    public int nFrameVK = 1;
    public short nHP1;
    public short nHP2;
    public short nHP3;
    public short nMP1;
    public short nMP2;
    public short nMP3;
    public short nAr1;
    public short nAr2;
    public short nAr3;
    public Vector<Item> item = new Vector<>();
}
