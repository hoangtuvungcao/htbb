/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import java.util.Vector;

public class TopMemClan {
    public Vector<CharClan> topmem = new Vector<>();
    long timeGet = 0L;

    public Vector<CharClan> getTopMemClan(int idClan) {
        if (System.currentTimeMillis() - this.timeGet >= 900000L) {
            this.timeGet = System.currentTimeMillis();
            this.topmem = Database.instance.getTopMemberClan(idClan);
        }

        return this.topmem;
    }
}
