/*
 * PHUC HOI TU LOI CU (P3): lop nay duoc dich nguoc tu lib/htbb-core.jar - ma loi da bien dich cua
 * game goc - bang Vineflower 1.11.1 roi sua tay toi thieu cho bien dich duoc (--release 8). Giu
 * nguyen ten lop, package va hanh vi; doi ten / don dep de o giai doan sau. Nguon goc: docs/giay-phep.md.
 */
package real;

public class NameAttributeItem {
    public byte id;
    public byte isPercent;
    public byte colorPaint = 0;
    String name = "";

    public NameAttributeItem(byte id, String name, byte isPercent, byte colorpaint) {
        this.id = id;
        this.name = name;
        this.isPercent = isPercent;
        this.colorPaint = colorpaint;
    }

    public boolean isPercent() {
        byte pc = ItemTemplates.ALL_NAME_ATTRIBUTE_ITEM.get(new Byte(this.id)).isPercent;
        return pc == 1 || pc == 2;
    }

    public String getValue(int value) {
        String vl = String.valueOf(this.id != 33 && this.id != 34 ? value : value * 1000);
        if (this.isPercent == 2) {
            vl = value / 10 + "." + value % 10;
        }

        return vl + (this.isPercent() ? "%" : "");
    }
}
