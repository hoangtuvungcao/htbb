package game.render.screen;

import game.model.Main;
import game.render.Canvas;
import javax.microedition.lcdui.Graphics;

public abstract class Dialog extends Main {
    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    public void keyPress(int keyCode) {
    }

    public void update() {
        super.updateKey();
    }

    public void show() {
        Canvas.currentDialog = this;
    }
}
