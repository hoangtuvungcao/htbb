package game.model;

import game.core.j2me.Graphics;
import game.render.screen.GameData;

public class ItemQuest extends Dropable {
    public static String[] NAME_ITEM = {""};
    public static byte[] ICON_IMG = {0};

    @Override
    public boolean isItemCanGet() {
        return true;
    }

    @Override
    public void paint(Graphics graphics) {
        GameData.paintIcon(graphics, (short) (ICON_IMG[this.item_template_id] + 8000), this.x, this.y - this.deltaH);
    }

    @Override
    public String getDisplayName() {
        return NAME_ITEM[this.item_template_id];
    }
}
