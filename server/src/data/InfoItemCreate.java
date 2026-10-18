/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package data;

import real.Item;

public class InfoItemCreate {
    public static String[] nameGender = new String[]{"", "nam", "nữ"};
    public String name = "";
    public byte gender = 0;
    public byte level = 0;
    public byte charClass = -1;
    public byte typeItem = -1;
    public byte magic_physic = 2;
    public short idTemplate = 0;
    public short idOptionMenu;
    public byte materialUse = 0;
    public byte typeOption;
    public Item item = null;
    public static String[] nameMagic_physic = new String[]{"Ma", "Vật", ""};
    public static String[] nameAmor = new String[]{"áo ", "Quần ", "Nón ", "Giày ", "Găng ", "Nhẫn ", "Dây chuyền ", "Ngọc "};
    public static String[] nameJewelry = new String[]{"Nhẫn ", "Dây chuyền ", "Ngọc "};
}
