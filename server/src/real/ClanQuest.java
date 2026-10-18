/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class ClanQuest {
    public byte idQuest = 0;
    public byte count = 0;
    public short[] item_mons = new short[6];
    public byte finish = 0;
    public byte rcv = 0;
    public long timeEndQClan = 0L;
}
