/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;
import real.Item;

public class CharItem {
    public boolean haveDupItem = false;
    public int citem = 0;
    public Vector<CharItem.checkItem> allItem = new Vector<>();

    public void setItem(int idTemplate, Item it) {
        this.allItem.add(new CharItem.checkItem(it));
    }

    public void checkItem(Item it, String charname) {
        if (this.allItem.size() == 0) {
            this.setItem(0, it);
        } else {
            boolean isExist = false;

            for (int i = 0; i < this.allItem.size(); i++) {
                CharItem.checkItem cit = this.allItem.get(i);
                if (cit.it.compareItem(it)) {
                    cit.count++;
                    this.citem++;
                    this.haveDupItem = true;
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                this.setItem(0, it);
            }
        }
    }

    class checkItem {
        Item it;
        int count = 1;

        public checkItem(Item it) {
            this.it = it;
        }
    }
}
