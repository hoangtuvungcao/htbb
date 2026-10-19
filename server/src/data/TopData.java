/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;
import real.Char;
import real.Map;

public class TopData {
    public Vector<CharInfo> topRich = new Vector<>();
    public Vector<CharInfo> topLv = new Vector<>();

    public void addMember2Top(Char p) {
        CharInfo c = Map.createCharInfo(p);
        if (this.topRich.size() < 100) {
            this.topRich.add(c);
        } else {
            for (int i = 0; i < this.topRich.size(); i++) {
                CharInfo cin = this.topRich.get(i);
                if (cin.money < p.getxu()) {
                    this.topRich.add(c);
                    break;
                }
            }
        }

        if (this.topLv.size() < 100) {
            this.topLv.add(c);
        } else {
            int ix = 0;

            while (ix < this.topLv.size()) {
                ix++;
            }
        }
    }
}
