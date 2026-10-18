package game.model;

import game.render.Res;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class DropGemItem extends Dropable {
    byte startItemQuest = 10;

    @Override
    public void paint(Graphics g) {
        if (this.catagory == 6) {
            Res.paintGem(g, Res.getGemByID(this.item_template_id).idImage, this.x, this.y - this.deltaH);
        } else {
            GameData.paintIcon(g, (short) (Res.getShopByID((byte) this.item_template_id).idImage + 5500), this.x, this.y - this.deltaH);
        }
    }

    @Override
    public String getDisplayName() {
        if (this.catagory == 6) {
            return Res.getGemByID(this.item_template_id).name;
        }
        return Res.getShopByID(this.item_template_id).name;
    }

    @Override
    public boolean isItemCanGet() {
        return true;
    }
}
