package game.model;

import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class ItemQuest extends Dropable {
    public static String[] NAME_ITEM = {""};
    public static byte[] ICON_IMG = new byte[1];

    @Override
    public void paint(Graphics g) {
        GameData.paintIcon(g, (short) (ICON_IMG[this.item_template_id] + 8000), this.x, this.y - this.deltaH);
    }

    @Override
    public String getDisplayName() {
        return NAME_ITEM[this.item_template_id];
    }

    @Override
    public boolean isItemCanGet() {
        return true;
    }
}
