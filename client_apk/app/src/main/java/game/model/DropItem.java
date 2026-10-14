package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.GameData;

public class DropItem extends Dropable {
    @Override
    public boolean isItemCanGet() {
        return true;
    }

    @Override
    public void paint(Graphics graphics) {
        GameData.paintIcon(graphics, ((ItemTemplate[]) Res.itemTemplates.elementAt(this.itemClass))[this.item_template_id].idIcon, this.x, this.y - this.deltaH);
    }

    @Override
    public String getDisplayName() {
        return ((ItemTemplate[]) Res.itemTemplates.elementAt(this.itemClass))[this.item_template_id].name;
    }
}
