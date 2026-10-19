/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import io.Session;
import real.Char;
import real.Map;
import real.RealController;

public class ChatBot extends Char {
    public static long timeLive = System.currentTimeMillis();
    public static int[] idMap = new int[]{0, 70, 301, 1701};

    public ChatBot(Session conn) {
        super(conn);
    }

    @Override
    public void update() {
        super.update();
        if (System.currentTimeMillis() - timeLive >= 0L) {
            if (this.map != null) {
                this.map.playerExit(this);
                this.map = null;
            } else if (System.currentTimeMillis() - timeLive >= 54000000L) {
                this.setTimeLive();
                this.map = RealController.mapList.get(idMap[Map.r.nextInt(idMap.length)]);
                this.map.playerJoin(this);
            }
        }
    }

    public void setTimeLive() {
        timeLive = System.currentTimeMillis() + 54000000L;
    }
}
