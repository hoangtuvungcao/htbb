/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class CharQuest {
    public int id_char;
    public int id_quest;
    public byte receive;
    public byte finish;
    public short[] nMonster = new short[3];
    public short currentNpc;
    public short[] nItem = new short[3];
}
