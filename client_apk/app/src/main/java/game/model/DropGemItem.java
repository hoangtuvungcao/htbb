package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.GameData;

public class DropGemItem extends Dropable {
    byte startItemQuest = 10;

    @Override
    public boolean isItemCanGet() {
        return true;
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.catagory == 6) {
            Res.paintGem(graphics, Res.getGemByID(this.item_template_id).idImage, this.x, this.y - this.deltaH);
        } else {
            GameData.paintIcon(graphics, (short) (Res.getShopByID((byte) this.item_template_id).idImage + 5500), this.x, this.y - this.deltaH);
        }
    }

    @Override
    public String getDisplayName() {
        if (this.catagory == 6) {
            return Res.getGemByID(this.item_template_id).name;
        }
        return Res.getShopByID(this.item_template_id).name;
    }
}
