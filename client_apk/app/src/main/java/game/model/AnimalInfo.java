package game.model;

import android.graphics.Bitmap;
import game.core.j2me.Graphics;
import game.networklogic.GameService;
import game.render.screen.GameData;

public class AnimalInfo {
    public static Bitmap imgAnimal = null;
    private static boolean isGetImg = false;
    public short id;
    public byte idImg;
    public byte level;
    public long timeStart;
    public int totalTimeCon;
    public byte typeAnimal;
    public String info = "";
    public String name = "";
    public byte typePet = -1;
    public byte idPaint = 0;

    public void paint(Graphics graphics, int i, int i2) {
        GameData.paintIcon(graphics, (short) (this.idPaint + 7500), i, i2);
    }

    public static void getImg() {
        if (!isGetImg) {
            GameService.gI().doGetImage((byte) 4, 0);
        }
        isGetImg = true;
    }
}
