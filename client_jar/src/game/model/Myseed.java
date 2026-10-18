package game.model;

import game.render.Res;
import game.render.screen.Font;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class Myseed {
    public int x;
    public int y;
    public int wcell = 150;
    public int hcell;
    public int id;
    public String info;
    public String[] arrInfo;
    public boolean isfocus;

    public Myseed(int x, int y, int id, String info) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.info = info;
        this.arrInfo = Font.normalFont[0].splitFontBStrInLine(info, 130);
        this.hcell = this.arrInfo.length * 20;
        if (this.hcell > 100) {
            this.hcell = 100;
        }
    }

    public void paint(Graphics g) {
        if (this.isfocus) {
            g.setColor(11184844);
            g.fillRect(this.x, this.y, 18, 18);
        }
        g.drawImage(Res.imgKhung, this.x, this.y, 0);
        GameData.paintIcon(g, (short) (this.id + 3200), this.x, this.y);
    }

    public void update() {
    }
}
