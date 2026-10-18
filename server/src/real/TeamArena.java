/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Hashtable;
import java.util.Vector;

public class TeamArena {
    public static String[] NAME_TEAM = new String[]{"Đội Xanh", "Đội Vàng", "Đội Tím"};
    public static int ID_TEAM = 0;
    public int id = 0;
    public int totalPoint = 0;
    public int typeArena = 0;
    public Hashtable<String, String> memTeam = new Hashtable<>();
    public Vector<String> v_memTeam = new Vector<>();
    public int pointArena = 0;
    public boolean isStart = false;

    public TeamArena(int id) {
        this.id = (byte)id;
    }

    public static int genIDTeam() {
        return ID_TEAM++;
    }

    public void addMember(Char p) {
        try {
            this.memTeam.put(p.charname.toLowerCase(), p.charname.toLowerCase());
            boolean isExist = false;

            for (int i = 0; i < this.v_memTeam.size(); i++) {
                if (this.v_memTeam.get(i).equals(p.charname.toLowerCase())) {
                    return;
                }
            }

            this.v_memTeam.add(p.charname.toLowerCase());
        } catch (Exception var4) {
        }
    }

    public void doAddPoint(Char p, int point) {
        this.totalPoint += point;
        if (this.totalPoint < 0) {
            this.totalPoint = 0;
        }
    }
}
