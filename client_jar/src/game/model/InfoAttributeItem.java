package game.model;

import game.model.skill.SkillManager;

public class InfoAttributeItem {
    private short id;
    private short value;

    public InfoAttributeItem(short id, short value) {
        this.id = id;
        this.value = value;
    }

    public byte getColorPaint(boolean kick) {
        return ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(new StringBuffer(String.valueOf((int) this.id)).toString())).getColorPaint(kick);
    }

    public String getName(int clazz) {
        String name = ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(new StringBuffer(String.valueOf((int) this.id)).toString())).name;
        if (this.id >= 43 && this.id <= 57) {
            name = SkillManager.SKILL_NAME[clazz][this.id - 43];
        }
        return new StringBuffer(String.valueOf(name)).append(": ").toString();
    }

    public boolean isPercent() {
        byte pc = ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(new StringBuffer(String.valueOf((int) this.id)).toString())).ispercent;
        return pc == 1 || pc == 2;
    }

    public String getValue() {
        String vl = new StringBuffer(String.valueOf((int) this.value)).toString();
        if (((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(new StringBuffer(String.valueOf((int) this.id)).toString())).ispercent == 2) {
            vl = new StringBuffer(String.valueOf(this.value / 10)).append(".").append(this.value % 10).toString();
        }
        return vl;
    }

    public int getValueAtt() {
        return this.value;
    }

    public short getID() {
        return this.id;
    }
}
