/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;

public class InfoItemBug {
    public int idsellMarket = -1;
    public int count = 0;
    public Vector<String> listCharname = new Vector<>();

    public InfoItemBug(int id) {
        this.idsellMarket = id;
    }

    public void setCharName(String name) {
        if (!this.listCharname.contains(name)) {
            this.listCharname.add(name);
        }
    }

    public String getListChar() {
        String st = "";

        for (int i = 0; i < this.listCharname.size(); i++) {
            st = st + this.listCharname.get(i) + ",";
        }

        return st;
    }
}
