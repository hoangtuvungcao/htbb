package game.model;

import game.core.j2me.Graphics;
import game.render.Res;
import game.render.screen.GameData;

public class ItemLuckyDraw {
    public byte cat;
    public String[] decript;
    public int id;
    public byte isSelect = 1;
    public String name;

    public void paint(Graphics graphics, int i, int i2) {
        try {
            byte b = this.cat;
            if (b == 3) {
                GameData.paintIcon(graphics, Res.getItem(0, this.id).idIcon, i, i2);
            } else if (b == 4) {
                Res.paintPotion(graphics, MainChar.listPotion[this.id].index, i, i2, 3);
            } else if (b == 6) {
                Res.paintGemKhoa(graphics, Res.getGemByID((short) this.id).idImage, i, i2);
            } else if (b == 12) {
                GameData.paintIcon(graphics, (short) (this.id + 7500), i, i2);
            }
        } catch (Exception unused) {
            Cout.println(((int) this.cat) + " ::::ASD " + this.id);
        }
    }

    public String getName() {
        return this.name;
    }

    public String[] getDecript() {
        String[] strArr = this.decript;
        return strArr == null ? new String[]{this.name} : strArr;
    }
}
