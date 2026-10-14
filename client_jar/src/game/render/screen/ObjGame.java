package game.render.screen;

import game.model.IAction;
import javax.microedition.lcdui.Graphics;

public class ObjGame {
    public boolean isTfield;
    public int x;
    public int y;
    public int width;
    public int height;
    public IAction acPopup;
    public String title = "";

    public void paint(Graphics g) {
    }

    public void update() {
    }

    public void updateKey() {
    }

    public boolean keyPressed(int keyCode) {
        return false;
    }
}
