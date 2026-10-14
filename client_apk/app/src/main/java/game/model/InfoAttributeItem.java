package game.model;

import game.model.skill.SkillManager;

public class InfoAttributeItem {
    private short id;
    private short value;

    public InfoAttributeItem(short s, short s2) {
        this.id = s;
        this.value = s2;
    }

    public byte getColorPaint(boolean z) {
        return ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(((int) this.id) + "")).getColorPaint(z);
    }

    public String getName(int i) {
        String str = ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(((int) this.id) + "")).name;
        short s = this.id;
        if (s >= 43 && s <= 57) {
            str = SkillManager.SKILL_NAME[i][this.id - 43];
        }
        return str + ": ";
    }

    public boolean isPercent() {
        byte b = ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(((int) this.id) + "")).ispercent;
        return b == 1 || b == 2;
    }

    public String getValue() {
        return ((NameAttributeItem) ItemTemplate.ALL_NAME_ATTRIBUTE_ITEM.get(new StringBuilder().append((int) this.id).append("").toString())).ispercent == 2 ? (this.value / 10) + "." + (this.value % 10) : ((int) this.value) + "";
    }

    public int getValueAtt() {
        return this.value;
    }

    public short getID() {
        return this.id;
    }
}
