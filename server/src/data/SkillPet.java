/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

public class SkillPet {
    public String name = "";
    public String decript = "";
    public byte id;
    public int value = 0;
    public int time = 0;
    public int time2 = 0;
    public int value2 = 0;

    public SkillPet(String name, String decript, int id, int value1, int value2) {
        this.name = name;
        this.id = (byte)id;
        this.decript = decript;
        this.value = value1;
        this.value2 = value2;
    }

    public SkillPet(String name, String decript, int id, int value1, int value2, int time1, int time2) {
        this.name = name;
        this.id = (byte)id;
        this.decript = decript;
        this.value = value1;
        this.value2 = value2;
        this.time = time1;
        this.time2 = time2;
    }

    public String getDecript(int level) {
        if (level == 1) {
            return this.id == Pet.ID_SKILL_TIEN_KHI
                ? "\n- " + this.decript.replace("#", String.valueOf(this.value)).replace("@", String.valueOf(this.time))
                : "\n- " + this.decript.replace("#", String.valueOf(this.value));
        } else if (level == 2) {
            return this.id == Pet.ID_SKILL_TIEN_KHI
                ? "\n- " + this.decript.replace("#", String.valueOf(this.value2)).replace("@", String.valueOf(this.time2))
                : "\n- " + this.decript.replace("#", String.valueOf(this.value2));
        } else {
            return null;
        }
    }
}
