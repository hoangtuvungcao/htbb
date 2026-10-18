/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class CharThiDau {
    public String name = "";
    public int charDbId = 0;
    public int point = 0;
    public int level = 1;
    public int rank = 0;
    public int nhom = 0;
    public int lasttimeout = 0;

    public CharThiDau() {
    }

    public CharThiDau(String charname, int lv, int chardbid) {
        this.charDbId = chardbid;
        this.name = charname;
        this.level = lv;
    }

    public void changePoint(int point) {
        this.point += point;
        if (this.point < 0) {
            this.point = 0;
        }
    }

    public int getPoint(int type) {
        return this.point;
    }

    public void setRank(int type, int j) {
        this.rank = j;
    }
}
