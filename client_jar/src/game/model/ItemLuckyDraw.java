package game.model;

import game.render.Res;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;

public class ItemLuckyDraw {
    public byte cat;
    public byte isSelect = 1;
    public int id;
    public String name;
    public String[] decript;

    public void paint(Graphics g, int x, int y) {
        try {
            switch (this.cat) {
                case 3:
                    ItemTemplate item = Res.getItem(0, this.id);
                    GameData.paintIcon(g, item.idIcon, x, y);
                    return;
                case 4:
                    Res.paintPotion(g, MainChar.listPotion[this.id].index, x, y, 3);
                    return;
                case 6:
                    Res.paintGemKhoa(g, Res.getGemByID((short) this.id).idImage, x, y);
                    break;
                case 12:
                    GameData.paintIcon(g, (short) (this.id + 7500), x, y);
                    return;
            }
        } catch (Exception e) {
            Cout.println(new StringBuffer(String.valueOf((int) this.cat)).append(" ::::ASD ").append(this.id).toString());
        }
    }

    public String getName() {
        return this.name;
    }

    public String[] getDecript() {
        return this.decript == null ? new String[]{this.name} : this.decript;
    }
}
