/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;

public class CharInfo {
    public String name = "";
    public byte level = 0;
    public byte headStyle = 0;
    public byte titlesClan = 3;
    public byte country = -1;
    public Vector<ItemInfo> wearingItem = new Vector<>();
    public int luong = 0;
    public int idClan;
    public int idDB;
    public int lastXu;
    public int lastLuong;
    public int userid;
    public int honor;
    public int pk;
    public int nKillInArena;
    public int bekill;
    public long money = 0L;
    public long exp = 0L;
    public long timeNuiChaubau = 0L;
    public boolean isLockGift = false;
    public byte fail = 0;

    public long getTimeNuiChauBau() {
        if (this.timeNuiChaubau > 0L) {
            long time = (this.timeNuiChaubau - System.currentTimeMillis()) / 1000L;
            if (time > 0L) {
                return time;
            }
        }

        return 0L;
    }
}
