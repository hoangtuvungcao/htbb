package game.model;

import game.render.Res;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class DropItem extends Dropable {
    @Override
    public void paint(Graphics g) {
        ItemTemplate template = ((ItemTemplate[]) Res.itemTemplates.elementAt(this.itemClass))[this.item_template_id];
        GameData.paintIcon(g, template.idIcon, this.x, this.y - this.deltaH);
    }

    @Override
    public String getDisplayName() {
        return ((ItemTemplate[]) Res.itemTemplates.elementAt(this.itemClass))[this.item_template_id].name;
    }

    @Override
    public boolean isItemCanGet() {
        return true;
    }
}
