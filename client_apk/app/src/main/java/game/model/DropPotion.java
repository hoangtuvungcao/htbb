package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.GameData;

public class DropPotion extends Dropable {
    byte startItemQuest = 10;

    @Override
    public boolean isItemCanGet() {
        return true;
    }

    @Override
    public void paint(Graphics graphics) {
        GameData.paintIcon(graphics, (short) (MainChar.listPotion[this.item_template_id].index + 5500), this.x, this.y - this.deltaH);
    }

    @Override
    public String getDisplayName() {
        if (this.item_template_id < 7 || this.item_template_id >= 14) {
            return MainChar.listPotion[this.item_template_id].name;
        }
        if (this.item_template_id > 9 && this.item_template_id < 14) {
            return Res.PORTION_ITEM_NAME;
        }
        return Res.PORTION_ITEM_NAME_CLAN;
    }

    public String getDisplayName2() {
        if (this.item_template_id < 7 || this.item_template_id >= 14) {
            return MainChar.listPotion[this.item_template_id].name2;
        }
        if (this.item_template_id > 9 && this.item_template_id < 14) {
            return Res.PORTION_ITEM_NAME2;
        }
        return Res.PORTION_ITEM_NAME_CLAN;
    }
}
