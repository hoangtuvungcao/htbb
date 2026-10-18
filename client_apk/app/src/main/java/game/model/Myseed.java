package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameData;

public class Myseed {
    public String[] arrInfo;
    public int hcell;
    public int id;
    public String info;
    public boolean isfocus;
    public int wcell;
    public int x;
    public int y;

    public void update() {
    }

    public Myseed(int i, int i2, int i3, String str) {
        this.x = i;
        this.y = i2;
        this.id = i3;
        this.info = str;
        String[] strArrSplitFontBStrInLine = Font.normalFont[0].splitFontBStrInLine(str, 130);
        this.arrInfo = strArrSplitFontBStrInLine;
        this.wcell = 150;
        int length = strArrSplitFontBStrInLine.length * 20;
        this.hcell = length;
        if (length > 100) {
            this.hcell = 100;
        }
    }

    public void paint(Graphics graphics) {
        if (this.isfocus) {
            graphics.setColor(11184844);
            graphics.fillRect(this.x, this.y, 18, 18);
        }
        graphics.drawImage(Res.imgKhung, this.x, this.y, 0);
        GameData.paintIcon(graphics, (short) (this.id + 3200), this.x, this.y);
    }
}
