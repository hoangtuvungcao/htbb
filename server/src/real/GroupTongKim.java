/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

import java.util.Vector;

public class GroupTongKim {
    public static int MAX_MEMBER_TEAM = 50;
    public Vector<Vector<String>> team = new Vector<>();
    public short totalMemTeam1 = 0;
    public short totalMemTeam2 = 0;

    public GroupTongKim() {
        this.team.add(new Vector<>());
        this.team.add(new Vector<>());
    }

    public int addMem(String name, int group) {
        if (Map.abs(this.team.get(0).size() - this.team.get(1).size()) < 5
            || (this.team.get(0).size() >= this.team.get(1).size() || group != 1) && (this.team.get(0).size() <= this.team.get(1).size() || group != 0)) {
            if (group == 0 && this.totalMemTeam1 == MAX_MEMBER_TEAM) {
                group = 1;
            }

            if (group == 1 && this.totalMemTeam2 == MAX_MEMBER_TEAM) {
                group = 0;
            }

            this.team.get(group).add(name);
            return 0;
        } else {
            return Map.abs(this.team.get(0).size() - this.team.get(1).size());
        }
    }

    public int registerToGroup(String name, int idGroup) {
        if (Map.abs(this.totalMemTeam1 - this.totalMemTeam2) < 5
            || (this.totalMemTeam1 >= this.totalMemTeam2 || idGroup != 1) && (this.totalMemTeam1 <= this.totalMemTeam2 || idGroup != 0)) {
            if (idGroup == 0 && this.totalMemTeam1 == MAX_MEMBER_TEAM) {
                idGroup = 1;
            }

            if (idGroup == 1 && this.totalMemTeam2 == MAX_MEMBER_TEAM) {
                idGroup = 0;
            }

            if (idGroup == 0) {
                this.totalMemTeam1++;
            } else {
                this.totalMemTeam2++;
            }

            return idGroup == 0 ? -1 : -2;
        } else {
            return Map.abs(this.totalMemTeam1 - this.totalMemTeam2);
        }
    }

    public short getTotalMemTeam1() {
        return this.totalMemTeam1;
    }

    public short getTotalMemTeam2() {
        return this.totalMemTeam2;
    }

    public boolean isFullGroup() {
        return this.totalMemTeam1 == 50 && this.totalMemTeam2 == 50;
    }
}
