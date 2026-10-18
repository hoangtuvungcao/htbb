/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import real.Actor;

public class ItemQuest extends Actor {
    public static String[] NAME_ITEM_QUEST = new String[]{
        "Nọc bọ cạp", "Rắn", "Khúc gỗ", "Rết đỏ", "Lá thư", "Thịt heo", "Lông đuôi Hồng Kê", "Móng cá sấu", "Da ếch", "Cựa gà"
    };
    public static byte[] ICON_IMAGE = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    String name = "";
    public int belongUser = 0;
    public short quantity;
    private short icon;
    public int id_char;

    public ItemQuest(short id, int quantity) {
        super((byte)14);
        this.setType(id);
        this.name = NAME_ITEM_QUEST[id];
        this.quantity = (short)quantity;
        this.icon = id;
    }

    @Override
    public boolean isItemQuest() {
        return true;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public short getQuantity() {
        return this.quantity;
    }

    @Override
    public short getIcon() {
        return this.icon;
    }
}
