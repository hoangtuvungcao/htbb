package game.model;

import game.networklogic.GameService;
import game.render.screen.GameData;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class AnimalInfo {
    public byte idImg;
    public byte level;
    public byte typeAnimal;
    public short id;
    public static Image imgAnimal;
    private static boolean isGetImg = false;
    public long timeStart;
    public int totalTimeCon;
    public String info = "";
    public String name = "";
    public byte typePet = -1;
    public byte idPaint = 0;

    public void paint(Graphics g, int x, int y) {
        GameData.paintIcon(g, (short) (this.idPaint + 7500), x, y);
    }

    public static void getImg() {
        if (!isGetImg) {
            GameService.gI().doGetImage((byte) 4, 0);
        }
        isGetImg = true;
    }
}
